package io.github.rbroderi.zblocks;

import io.github.rbroderi.zblocks.internal.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;
import static io.github.rbroderi.zblocks.PuzzleBlock.Kind.*;

/** Main-thread gameplay with Nexo furniture and item APIs. */
public final class ZBlocksPlugin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    private BlockIndex<PuzzleBlock> blocks;
    private RemovalSettings removal;
    private JadeSupport jade;
    private final Map<UUID,Integer> slidingUntil=new HashMap<>();
    private final Map<UUID,PuzzleBlock> legacyBackingRepairs=new HashMap<>();
    private static final class NativeMining {final Position position;final UUID block;double progress;NativeMining(PuzzleBlock p){position=p.position;block=p.id;}}
    private final Map<UUID,NativeMining> nativeMining=new HashMap<>();
    private final Map<UUID,UUID> pendingMining=new HashMap<>();
    private Path dataFile;
    private boolean dirty, ready, gameplayReady;
    private int tick, resetRange, maxBlocks, activationHorizontal, activationUp, activationDown, saveInterval;
    private final Map<UUID,Integer> lastInteraction=new HashMap<>();
    private NamespacedKey itemKind,itemColor;
    private PackBlockRenderer models;
    private EntityHitboxes hitboxes;
    private boolean dispatchingHitbox;
    private boolean dispatchingCreativeBreak;
    private boolean dispatchingStack;
    private record PlacementFace(int tick,org.bukkit.block.BlockFace face) {}
    private final Map<UUID,PlacementFace> placementFaces=new HashMap<>();
    private final Map<UUID,Integer> shapeSwings=new HashMap<>();
    private boolean dispatchingArrow;
    private final Map<UUID,Location> arrows=new HashMap<>();
    private static final Set<Material> ICE=EnumSet.of(Material.ICE,Material.PACKED_ICE,Material.BLUE_ICE,Material.FROSTED_ICE);

    @Override public void onEnable() {
        saveDefaultConfig();if(!getConfig().isSet("removal-hardness")){getConfig().options().copyDefaults(true);saveConfig();}removal=RemovalSettings.load(getConfig(),getLogger()::warning);dataFile=getDataFolder().toPath().resolve("blocks.yml");
        itemKind=new NamespacedKey(this,"block_kind");itemColor=new NamespacedKey(this,"block_color");
        resetRange=bounded("reset-range",100,1,256);maxBlocks=bounded("max-blocks",10000,1,100000);
        activationHorizontal=bounded("activation-horizontal",25,1,128);activationUp=bounded("activation-up",12,1,128);activationDown=bounded("activation-down",25,1,128);
        saveInterval=bounded("save-interval-ticks",100,20,1200);
        try {
            blocks=PuzzleStore.load(dataFile);
            Set<Position> origins=new HashSet<>();
            for(PuzzleBlock p:blocks.snapshot()) {
                if(PuzzlePlacement.invalidBacking(p)){
                    legacyBackingRepairs.put(p.id,p);
                }
                Bukkit.createBlockData(p.visualData);Bukkit.createBlockData(p.originalData);
                if(p.kind==PUSH&&!origins.add(p.origin))throw new IOException("Duplicate push origin");
            }
        } catch(Exception ex) {getLogger().severe("Cannot load puzzles; data preserved: "+ex);Bukkit.getPluginManager().disablePlugin(this);return;}
        ready=true;
        Bukkit.getPluginManager().registerEvents(new BlockProtection(blocks::contains,player->dispatchingCreativeBreak),this);
        new NexoIntegration(this,this::startGameplay,removal::configure);
    }
    private void startGameplay(){
        models=new PackBlockRenderer(this,"model_visual",null);
        hitboxes=new EntityHitboxes(this,"puzzle_hitbox",this::hitboxClick,this::disableHitboxes);
        hitboxes.mining((position,player)->{var p=position==null?null:blocks.at(position);return false;});
        jade=new JadeSupport(this,blocks::snapshot,this::intact);jade.install();
        Objects.requireNonNull(getCommand("zblocks")).setExecutor(this);getCommand("zblocks").setTabCompleter(this);
        Bukkit.getPluginManager().registerEvents(this,this);
        Bukkit.getScheduler().runTaskTimer(this,this::tick,1,1);
        gameplayReady=true;
        for(var world:Bukkit.getWorlds())for(var chunk:world.getLoadedChunks())cleanPlacementDuplicates(chunk);
        getLogger().info("Loaded "+blocks.size()+" puzzle blocks for Purpur 26.3.");
    }
    @Override public void onDisable() {for(var player:Bukkit.getOnlinePlayers())stopNativeMining(player);if(hitboxes!=null)hitboxes.close();if(models!=null)models.close();if(ready){save();for(PuzzleBlock p:blocks.snapshot())if(p.kind!=JUKEBOX&&intact(p)){Block b=worldBlock(p);if(b!=null)b.setBlockData(nativeExpected(p),false);}}ready=false;lastInteraction.clear();arrows.clear();com.nexomc.nexo.api.NexoItems.unregisterExternalItems(this);}
    private int bounded(String key,int fallback,int min,int max){return Math.clamp(getConfig().getInt(key,fallback),min,max);}
    private boolean save(){try{PuzzleStore.save(blocks,dataFile);dirty=false;return true;}catch(IOException ex){getLogger().severe("Could not save puzzles: "+ex);return false;}}
    private boolean allowed(String reason,Position from,Position to,Player actor){
        BlockMutationEvent event=new BlockMutationEvent("zblocks",reason,from,to,actor);Bukkit.getPluginManager().callEvent(event);return !event.isCancelled();
    }
    private Block worldBlock(PuzzleBlock p){return LoadedBlocks.at(p.position);}
    private Material material(PuzzleBlock.Kind kind,PuzzleBlock.Color color){return switch(kind){
        case PUSH -> Material.valueOf(color.prefix()+"_CONCRETE");
        case DEPRESS,START -> Material.valueOf(color.prefix()+"_GLAZED_TERRACOTTA");
        case ACTIVATE -> Material.AMETHYST_BLOCK;
        case TRANSIENT -> Material.GLASS;
        case SECRET_TRANSIENT,SECRET_BROKEN,SUBTLE_BROKEN -> Material.STONE;
        case HOURGLASS -> Material.CHISELED_QUARTZ_BLOCK;
        case BROKEN,BROKEN_EDGE -> Material.CRACKED_STONE_BRICKS;
        case SWITCH_OFF,SWITCH_ON -> Material.LEVER;
        case BOMB -> Material.TNT;
        case JUKEBOX -> Material.JUKEBOX;
        case CACTUS -> Material.GREEN_CONCRETE;
    };}
    private BlockData expected(PuzzleBlock p) {
        if(hitboxes!=null&&PuzzleHitboxes.custom(p)&&!p.destroyed)return Bukkit.createBlockData("minecraft:light[level=0]");
        if(!p.destroyed&&!(p.transientBlock()&&!p.active)&&p.kind!=JUKEBOX&&PuzzleModels.worldModel(p)&&!(p.kind==DEPRESS&&p.active))return Material.BARRIER.createBlockData();
        return nativeExpected(p);
    }
    private BlockData nativeExpected(PuzzleBlock p) {
        if(p.destroyed||p.transientBlock()&&!p.active)return Material.AIR.createBlockData();
        if(p.kind==DEPRESS&&p.active)return Material.REDSTONE_BLOCK.createBlockData();
        if(p.kind==ACTIVATE&&p.active)return Material.SEA_LANTERN.createBlockData();
        BlockData data=Bukkit.createBlockData(p.visualData);
        if(p.switchBlock()&&data instanceof Powerable powered)powered.setPowered(p.active);
        return data;
    }
    private boolean intact(PuzzleBlock p){Block b=worldBlock(p);return b!=null&&(p.kind==JUKEBOX?b.getType()==Material.JUKEBOX:b.getBlockData().matches(expected(p)));}
    private void render(PuzzleBlock p){Block b=worldBlock(p);if(p.kind==JUKEBOX&&b!=null&&b.getType()==Material.JUKEBOX){syncModel(p);return;}if(b!=null&&!b.getBlockData().matches(expected(p)))b.setBlockData(expected(p),true);syncModel(p);}
    private void syncModel(PuzzleBlock p){
        if(models==null||PuzzleBlock.retired(p.kind))return;
        if(p.destroyed||!PuzzleModels.worldModel(p)||!intact(p)){models.remove(p.id.toString());if(hitboxes!=null)hitboxes.remove(p.id.toString());return;}
        boolean shaped=hitboxes!=null&&PuzzleHitboxes.custom(p);
        if(shaped)try{hitboxes.upsert(p.id.toString(),p.position,PuzzleHitboxes.boxes(p),PuzzleHitboxes.boxes(p));}catch(RuntimeException ex){disableHitboxes(ex);return;}
        PuzzleBlock below=blocks.at(p.position.offset(0,-1,0));
        boolean matched=below!=null&&below.kind==DEPRESS&&p.color.matches(below.color)&&intact(below);
        BlockData mask=(shaped||worldBlock(p).getType().isAir()?Material.AIR:Material.BARRIER).createBlockData();
        models.upsert(p.id.toString(),p.position,PuzzleModels.key(p,ice(p),matched),material(p.kind,p.color),mask,shaped?PuzzleHitboxes.boxes(p):List.of());
        // The active base has an opaque redstone backing; native display lighting
        // can sample that occupied cell as dark even though the model is outside it.
        models.brightness(p.id.toString(),PuzzleLighting.fullBright(p,matched));
        models.rotation(p.id.toString(),p.yaw,p.kind==BROKEN_EDGE,p.edgeTilt);
    }
    private void hitboxClick(EntityHitboxes.Click click){
        if(dispatchingCreativeBreak&&click.action()!=EntityHitboxes.Action.USE)return;
        Block b=LoadedBlocks.at(click.position());if(b==null)return;
        if(click.action()!=EntityHitboxes.Action.USE){var p=blocks.at(click.position());if(p==null)return;
            if(p.kind==ACTIVATE&&click.action()==EntityHitboxes.Action.ATTACK&&PuzzleInteraction.weapon(click.player().getInventory().getItemInMainHand().getType())){stopNativeMining(click.player());operate(p,click.player());return;}
            if(p.kind==PUSH&&!holdingPickaxe(click.player())){operate(p,click.player());return;}
            if(!PuzzleHitboxes.custom(p))return;
            if(click.action()==EntityHitboxes.Action.ATTACK&&canMine(p.position,click.player())){startShapeMining(p,click.player());return;}
            if(instantRemoval(p,click.player())||click.action()==EntityHitboxes.Action.BREAK&&canMine(p.position,click.player())){removePlayer(p,click.player(),false);return;}
            return;
        }
        // Nexo already checked claims and dispatched its cancellable furniture event.
        // Re-dispatching a Bukkit click re-enters Nexo's placement path and can clear
        // the backing cell. Operate once through the existing mutation permission hook.
        PuzzleBlock p=blocks.at(click.position());if(p!=null&&p.kind!=PUSH)operate(p,click.player());
    }
    private void cleanPlacementDuplicates(org.bukkit.Chunk chunk){
        for(var p:blocks.snapshot()){
            if(!PuzzleBlock.retired(p.kind)||!p.position.world().equals(chunk.getWorld().getUID())||(p.position.x()>>4)!=chunk.getX()||(p.position.z()>>4)!=chunk.getZ())continue;
            var block=worldBlock(p);
            // Release old entries without deleting existing world blocks or discs.
            if(p.kind==CACTUS&&block!=null&&block.getType()==Material.BARRIER)block.setType(Material.GREEN_CONCRETE,true);
            if(models!=null)models.remove(p.id.toString());if(hitboxes!=null)hitboxes.remove(p.id.toString());
            blocks.remove(p);legacyBackingRepairs.remove(p.id);dirty=true;
        }
        repairLegacyBacking(chunk);
        int removed=0;
        for(var entity:chunk.getEntities()){
            if(!(entity instanceof ItemDisplay)||NexoIntegration.owned(entity,this)||!com.nexomc.nexo.api.NexoFurniture.isFurniture(entity))continue;
            var mechanic=com.nexomc.nexo.api.NexoFurniture.furnitureMechanic(entity);if(mechanic==null)continue;
            var location=entity.getLocation();var position=new Position(chunk.getWorld().getUID(),location.getBlockX(),location.getBlockY(),location.getBlockZ());
            if(PuzzlePlacement.staleFurniture(mechanic.getItemID(),false,blocks.at(position))){NexoIntegration.remove(entity);removed++;}
        }
        if(removed>0)getLogger().info("Removed "+removed+" duplicate temporary puzzle displays in loaded chunk "+chunk.getX()+","+chunk.getZ());
    }
    private void repairLegacyBacking(org.bukkit.Chunk chunk){
        for(var p:List.copyOf(legacyBackingRepairs.values())){
            if(!p.position.world().equals(chunk.getWorld().getUID())||(p.position.x()>>4)!=chunk.getX()||(p.position.z()>>4)!=chunk.getZ())continue;
            if(blocks.at(p.position)!=p){legacyBackingRepairs.remove(p.id);continue;}
            var b=LoadedBlocks.at(p.position);if(b==null)continue;
            String old=p.visualData;
            p.visualData=p.switchBlock()?"minecraft:lever[face=floor,facing=north,powered=false]":material(p.kind,p.color).createBlockData().getAsString();
            if(intact(p)){dirty=true;legacyBackingRepairs.remove(p.id);continue;}
            if(b.getType().isAir()){
                if(LoadedBlocks.livingOccupant(p.position)){p.visualData=old;continue;}
                render(p);dirty=true;legacyBackingRepairs.remove(p.id);getLogger().info("Repaired legacy puzzle backing at "+p.position.encode());
            }else{p.visualData=old;legacyBackingRepairs.remove(p.id);getLogger().warning("Legacy puzzle backing differs at "+p.position.encode()+"; leaving the existing block intact. Inspect or replace this puzzle.");}
        }
    }
    @EventHandler public void loadedPuzzleChunk(org.bukkit.event.world.ChunkLoadEvent event){cleanPlacementDuplicates(event.getChunk());if(jade!=null)jade.refreshChunk(event.getChunk());}
    private boolean canMine(Position position,Player player){var p=position==null?null:blocks.at(position);return p!=null&&(p.kind!=PUSH||holdingPickaxe(player))&&!player.isDead()&&removal.rule(p.kind,p.color).canMine(player.getGameMode(),player.hasPermission("zblocks.use"),player.hasPermission("zblocks.admin"));}
    private boolean holdingPickaxe(Player player){return PuzzleInteraction.pickaxe(player.getInventory().getItemInMainHand().getType());}
    private boolean instantRemoval(PuzzleBlock p,Player player){return (p.kind!=PUSH||holdingPickaxe(player))&&removal.rule(p.kind,p.color).instantRemoval(player.getGameMode(),player.hasPermission("zblocks.admin"),player.hasPermission("zblocks.use"));}
    private boolean creativeRemovalAllowed(Player player){return player.getGameMode()==GameMode.CREATIVE&&player.hasPermission("zblocks.admin");}
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true) public void nativeDamage(BlockDamageEvent event){
        var p=blocks.at(LoadedBlocks.position(event.getBlock()));if(p==null||PuzzleHitboxes.custom(p)||!canMine(p.position,event.getPlayer()))return;
        var active=nativeMining.get(event.getPlayer().getUniqueId());if(active!=null&&active.block.equals(p.id)&&active.position.equals(p.position))return;
        UUID token=UUID.randomUUID();pendingMining.put(event.getPlayer().getUniqueId(),token);
        // Vanilla can emit a cancelled instant break for levers in the same tick.
        // Start after that event, which otherwise stops Nexo's newly created timer.
        Bukkit.getScheduler().runTask(this,()->{
        if(!token.equals(pendingMining.remove(event.getPlayer().getUniqueId()))||event.isCancelled()||!event.getPlayer().isOnline()||blocks.at(p.position)!=p||!intact(p)||!canMine(p.position,event.getPlayer()))return;
        var target=event.getPlayer().getTargetBlockExact(6);if(target==null||!LoadedBlocks.position(target).equals(p.position))return;
        stopNativeMining(event.getPlayer());nativeMining.put(event.getPlayer().getUniqueId(),new NativeMining(p));
        });
    }
    private void stopNativeMining(Player player){pendingMining.remove(player.getUniqueId());var mining=nativeMining.remove(player.getUniqueId());if(mining!=null&&player.getWorld().getUID().equals(mining.position.world())){var b=LoadedBlocks.at(mining.position);if(b!=null)player.sendBlockDamage(b.getLocation(),0);}}
    @EventHandler public void miningAbort(BlockDamageAbortEvent event){stopNativeMining(event.getPlayer());}
    @EventHandler public void miningSlot(PlayerItemHeldEvent event){stopNativeMining(event.getPlayer());}
    @EventHandler public void miningDrop(PlayerDropItemEvent event){stopNativeMining(event.getPlayer());}
    @EventHandler public void miningSwap(PlayerSwapHandItemsEvent event){stopNativeMining(event.getPlayer());}
    private boolean removePlayer(PuzzleBlock p,Player player,boolean dispatchEvent){
        if(!(instantRemoval(p,player)||canMine(p.position,player))||!allowed("player-remove",p.position,p.position,player))return false;
        Block b=worldBlock(p);if(b==null)return false;
        if(dispatchEvent){var event=new BlockBreakEvent(b,player);dispatchingCreativeBreak=true;try{Bukkit.getPluginManager().callEvent(event);}finally{dispatchingCreativeBreak=false;}if(event.isCancelled())return false;}
        blocks.remove(p);dirty=true;if(!save()){blocks.add(p);dirty=true;player.sendMessage("Could not save removal; puzzle retained. Check server storage.");return false;}
        if(models!=null)models.remove(p.id.toString());if(hitboxes!=null)hitboxes.remove(p.id.toString());
        if(b.getState() instanceof org.bukkit.block.Jukebox jukebox&&!jukebox.getRecord().getType().isAir())b.getWorld().dropItemNaturally(b.getLocation(),jukebox.getRecord().clone());
        b.setType(Material.AIR,false);stopNativeMining(player);updateControls();return true;
    }
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true) public void creativeNativeBreak(BlockBreakEvent event){
        if(dispatchingCreativeBreak)return;
        var p=blocks.at(LoadedBlocks.position(event.getBlock()));if(p==null||!instantRemoval(p,event.getPlayer()))return;
        var before=event.getBlock().getState();if(p.kind!=JUKEBOX)event.setDropItems(false);
        Bukkit.getScheduler().runTask(this,()->{if(!event.isCancelled()&&blocks.at(p.position)==p&&!removePlayer(p,event.getPlayer(),false)){before.update(true,false);syncModel(p);}});
    }
    private void disableHitboxes(RuntimeException ex){
        getLogger().severe("Custom collision stopped; restoring native puzzle blocks: "+ex.getMessage());
        if(hitboxes!=null)hitboxes.close();hitboxes=null;
        for(PuzzleBlock p:blocks.snapshot())if(PuzzleHitboxes.custom(p)&&!p.destroyed){Block b=worldBlock(p);if(b!=null&&b.getType()==Material.LIGHT)b.setBlockData(nativeExpected(p),false);syncModel(p);}
    }
    private void sound(Position p,String custom,Sound vanilla,float volume,float pitch){if(models==null){var b=LoadedBlocks.at(p);if(b!=null)b.getWorld().playSound(b.getLocation(),vanilla,volume,pitch);}else models.sound(p,"zblocks:"+custom,vanilla,volume,pitch);}
    private boolean room(Position to){Block b=LoadedBlocks.at(to);return b!=null&&!blocks.contains(to)&&b.getType().isAir()&&!LoadedBlocks.livingOccupant(to);}
    private boolean ice(PuzzleBlock p){Block b=LoadedBlocks.at(p.position.offset(0,-1,0));return b!=null&&ICE.contains(b.getType());}
    private boolean move(PuzzleBlock p,Position to,Player actor,String reason){
        if(!intact(p)||!room(to)||!allowed(reason,p.position,to,actor))return false;
        Position from=p.position;
        blocks.move(p,from,to);p.position=to;
        LoadedBlocks.at(from).setType(Material.AIR,true);render(p);
        int duration=SlideMotion.duration(from,to,getConfig().getInt("push-slide-ticks",6));if(duration>0)slidingUntil.put(p.id,tick+duration);else slidingUntil.remove(p.id);dirty=true;return true;
    }
    private void pushMessage(Player player,String message){player.sendActionBar(Component.text(message));}
    private void push(PuzzleBlock p,Player player){
        if(tick<slidingUntil.getOrDefault(p.id,0)||p.dx!=0||p.dz!=0){pushMessage(player,"Push block is still moving.");return;}
        var loc=player.getLocation();var block=worldBlock(p);if(block==null)return;
        if(!player.getWorld().getUID().equals(p.position.world())||player.getEyeLocation().distanceSquared(block.getLocation().add(.5,.5,.5))>36){pushMessage(player,"Move closer to the push block.");return;}
        if(Math.abs(loc.getY()-p.position.y())>1.25){pushMessage(player,"Stand beside the push block at its height.");return;}
        var direction=PuzzleInteraction.pushSide(loc.getX(),loc.getZ(),p.position.x(),p.position.z());
        if(direction==null){pushMessage(player,"Stand beside the push block.");return;}int dx=direction[0],dz=direction[1];
        Block above=LoadedBlocks.at(p.position.offset(0,1,0));
        if(above==null||!above.getType().isAir()||blocks.contains(p.position.offset(0,1,0))){pushMessage(player,"Clear the block above before pushing.");return;}
        var destination=p.position.offset(dx,0,dz);
        if(!room(destination)){pushMessage(player,"Push destination is blocked or occupied.");return;}
        if(move(p,destination,player,"push")){
            if(ice(p)){p.dx=dx;p.dz=dz;p.momentum=1;}
            sound(p.position,"scrape",Sound.BLOCK_STONE_PLACE,1,.7f);
        }else pushMessage(player,"Push denied by protection or a changed puzzle cell.");
    }
    private PuzzleBlock shapeTarget(Player player){
        var eye=player.getEyeLocation();var start=eye.toVector();var direction=eye.getDirection();
        var wall=player.rayTraceBlocks(6,FluidCollisionMode.NEVER);double best=wall==null?6:start.distance(wall.getHitPosition());PuzzleBlock found=null;
        for(var p:blocks.snapshot())if(PuzzleHitboxes.custom(p)&&intact(p)&&p.position.world().equals(player.getWorld().getUID())){
            var hit=PuzzleHitboxes.intersect(p,start,direction,6);if(hit==null)continue;
            double distance=start.distance(hit.getHitPosition());if(distance<best){best=distance;found=p;}
        }
        return found;
    }
    private void startShapeMining(PuzzleBlock p,Player player){
        shapeSwings.put(player.getUniqueId(),tick);
        var mining=nativeMining.get(player.getUniqueId());if(mining!=null&&mining.block.equals(p.id))return;
        if(canMine(p.position,player)&&allowed("player-mine",p.position,p.position,player))nativeMining.put(player.getUniqueId(),new NativeMining(p));
    }
    @EventHandler(priority=EventPriority.MONITOR) public void swing(PlayerArmSwingEvent event){
        var player=event.getPlayer();if(event.isCancelled()||event.getHand()!=EquipmentSlot.HAND||player.getGameMode()==GameMode.SPECTATOR)return;
        var shaped=shapeTarget(player);
        if(shaped!=null){
            var click=new PlayerInteractEvent(player,Action.LEFT_CLICK_BLOCK,player.getInventory().getItemInMainHand(),worldBlock(shaped),player.getFacing().getOppositeFace(),EquipmentSlot.HAND);
            Bukkit.getPluginManager().callEvent(click);
            if(click.useInteractedBlock()!=Event.Result.DENY&&canMine(shaped.position,player))startShapeMining(shaped,player);
            return;
        }
        if(holdingPickaxe(player))return;
        var hit=player.rayTraceBlocks(6,FluidCollisionMode.NEVER);if(hit==null||hit.getHitBlock()==null)return;
        var p=blocks.at(LoadedBlocks.position(hit.getHitBlock()));if(p==null||p.kind!=PUSH)return;
        int swingTick=tick;Position position=p.position;
        Bukkit.getScheduler().runTask(this,()->{
            // Some clients do not send a usable block interaction for unbreakable
            // carriers. A swing still arrives. Reuse normal cancellable left-click
            // dispatch, once, so claim/protection listeners can deny this fallback.
            if(!player.isOnline()||holdingPickaxe(player)||blocks.at(position)!=p||lastInteraction.getOrDefault(player.getUniqueId(),-100)>=swingTick)return;
            var block=LoadedBlocks.at(position);if(block==null)return;
            var click=new PlayerInteractEvent(player,Action.LEFT_CLICK_BLOCK,player.getInventory().getItemInMainHand(),block,hit.getHitBlockFace(),EquipmentSlot.HAND);
            Bukkit.getPluginManager().callEvent(click);
            if(lastInteraction.getOrDefault(player.getUniqueId(),-100)<swingTick)pushMessage(player,"Push click was blocked; check interaction permissions.");
        });
    }
    private void tick(){
        slidingUntil.entrySet().removeIf(entry->entry.getValue()<=tick);
        for(var id:List.copyOf(nativeMining.keySet())){
            var player=Bukkit.getPlayer(id);var mining=nativeMining.get(id);var p=blocks.at(mining.position);var shaped=player==null?null:shapeTarget(player);var target=player==null?null:shaped!=null?worldBlock(shaped):player.getTargetBlockExact(6);
            if(player==null){nativeMining.remove(id);continue;}
            if(p==null||PuzzleHitboxes.custom(p)&&tick-shapeSwings.getOrDefault(id,-100)>8||!p.id.equals(mining.block)||!intact(p)||target==null||!LoadedBlocks.position(target).equals(mining.position)||!canMine(mining.position,player)){stopNativeMining(player);continue;}
            var timing=new com.nexomc.nexo.mechanics.breakable.Breakable(removal.rule(p.kind,p.color).miningHardness(),com.nexomc.nexo.utils.drops.Drop.emptyDrop());
            mining.progress+=1d/Math.max(1,timing.breakTime(player));
            if(mining.progress>=1){stopNativeMining(player);removePlayer(p,player,!PuzzleHitboxes.custom(p));}else player.sendBlockDamage(target.getLocation(),(float)mining.progress);
        }
        if(tick%20==0&&jade!=null)jade.refresh();
        if(tick%20==0&&!legacyBackingRepairs.isEmpty())for(var world:Bukkit.getWorlds())for(var chunk:world.getLoadedChunks())repairLegacyBacking(chunk);
        tick++;
        tickArrows();
        if(tick%20==1)for(PuzzleBlock p:blocks.snapshot())if(!p.destroyed){
            Block b=worldBlock(p);if(b!=null&&!b.getBlockData().matches(expected(p))&&(b.getBlockData().matches(nativeExpected(p))||b.getType()==Material.LIGHT||b.getType()==Material.BARRIER))render(p);
        }
        if(tick%2==0){for(PuzzleBlock p:blocks.snapshot()){
            if(p.kind!=PUSH||!intact(p)||tick<slidingUntil.getOrDefault(p.id,0))continue;
            Block below=LoadedBlocks.at(p.position.offset(0,-1,0));
            if(below==null)continue;
            if(below.getType().isAir()&&!blocks.contains(p.position.offset(0,-1,0))){move(p,p.position.offset(0,-1,0),null,"gravity");continue;}
            if(p.dx==0&&p.dz==0)continue;
            boolean onIce=ice(p);
            if(onIce||p.momentum>0){
                if(move(p,p.position.offset(p.dx,0,p.dz),null,"slide")){if(!onIce)p.momentum--;continue;}
            }
            p.dx=0;p.dz=0;p.momentum=0;dirty=true;
        }}
        if(tick%4==0)updateControls();
        if(tick%20==0&&models!=null)for(PuzzleBlock p:blocks.snapshot())syncModel(p);
        if(tick%3==0)for(PuzzleBlock p:blocks.snapshot())if(p.kind==BOMB&&p.fuse>=0&&!p.destroyed&&intact(p)){
            var world=Bukkit.getWorld(p.position.world());if(world==null)continue;
            var tip=BombFuse.tip(p.fuse);var location=new Location(world,p.position.x()+tip[0],p.position.y()+tip[1],p.position.z()+tip[2]);
            world.spawnParticle(Particle.SMALL_FLAME,location,1,.012,.012,.012,0);
            world.spawnParticle(Particle.SMOKE,location,1,.018,.025,.018,.012);
            world.spawnParticle(Particle.FIREWORK,location,2,.018,.018,.018,.025);
            if(tick%6==0)world.playSound(location,Sound.BLOCK_FIRE_AMBIENT,.2f,1.6f);
        }
        if(tick%15==0)for(PuzzleBlock p:blocks.snapshot())if(p.kind==BOMB&&p.fuse>=0&&intact(p)){
            if(p.fuse>0){p.fuse--;dirty=true;syncModel(p);}else explode(p);
        }
        if(tick%saveInterval==0&&dirty)save();
    }
    private void updateControls(){
        for(PuzzleBlock p:blocks.snapshot()){
            if(!intact(p))continue;
            if(p.kind==DEPRESS){
                PuzzleBlock above=blocks.at(p.position.offset(0,1,0));
                boolean value=above!=null&&above.kind==PUSH&&intact(above)&&p.color.matches(above.color);
                if(p.active!=value&&allowed("depress",p.position,p.position,null)){p.active=value;render(p);dirty=true;}
            }else if(p.kind==ACTIVATE){
                Block b=worldBlock(p);boolean value=b.isBlockPowered()||b.isBlockIndirectlyPowered();
                if(value!=p.powered){p.powered=value;dirty=true;}
                boolean active=p.manualActive||p.powered;
                if(active!=p.active&&allowed("activate",p.position,p.position,null)){p.active=active;render(p);dirty=true;}
            }
        }
        // Aggregate switches: one inactive control must not undo another active control.
        List<PuzzleBlock> controls=blocks.snapshot().stream().filter(p->p.kind==ACTIVATE&&p.active&&intact(p)).toList();
        for(PuzzleBlock p:blocks.snapshot())if(p.transientBlock()&&intact(p)){
            boolean value=controls.stream().anyMatch(c->p.position.within(c.position,activationHorizontal,activationUp,activationDown));
            if(value!=p.active&&(!value||!LoadedBlocks.livingOccupant(p.position))&&allowed("transient",p.position,p.position,null)){
                p.active=value;render(p);dirty=true;
            }
        }
    }
    private void operate(PuzzleBlock p,Player player){
        if(!intact(p)){if(p.kind==PUSH)pushMessage(player,"Puzzle backing changed; ask an admin to inspect this block.");return;}
        if(!player.hasPermission("zblocks.use")||player.getGameMode()==GameMode.SPECTATOR){if(p.kind==PUSH)pushMessage(player,"You need zblocks.use to push.");return;}
        Integer last=lastInteraction.get(player.getUniqueId());if(last!=null&&tick-last<4)return;
        lastInteraction.put(player.getUniqueId(),tick);
        switch(p.kind){
            case PUSH -> push(p,player);
            case ACTIVATE -> {if(allowed("toggle",p.position,p.position,player)){p.manualActive=!p.manualActive;dirty=true;updateControls();sound(p.position,"glass_ting",Sound.BLOCK_AMETHYST_BLOCK_HIT,1,1);}}
            case HOURGLASS -> {if(!reset(p.position,player))player.sendMessage("Reset blocked: load the whole puzzle, clear occupied origins and remove obstructions.");else sound(p.position,"warp",Sound.ENTITY_ENDERMAN_TELEPORT,1,1);}
            case SWITCH_OFF,SWITCH_ON -> {if(allowed("switch",p.position,p.position,player)){p.active=!p.active;render(p);dirty=true;}}
            case BOMB -> {if(p.fuse<0&&!p.destroyed&&allowed("arm",p.position,p.position,player)){p.fuse=8;dirty=true;sound(p.position,"fuse",Sound.ENTITY_TNT_PRIMED,1,1);}}
            case JUKEBOX -> {} // Vanilla disc insertion/ejection is handled by the server.
            default -> {}
        }
    }
    @EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true) public void stackClick(PlayerInteractEvent event){
        if(event.getAction()!=Action.RIGHT_CLICK_BLOCK||event.getHand()!=EquipmentSlot.HAND||event.getClickedBlock()==null||puzzleKind(event.getItem())==null)return;
        var support=blocks.at(LoadedBlocks.position(event.getClickedBlock()));if(support==null||!intact(support))return;
        var face=event.getBlockFace();var pos=support.position.offset(face.getModX(),face.getModY(),face.getModZ());var player=event.getPlayer();var hand=event.getItem().clone();
        event.setUseInteractedBlock(Event.Result.DENY);event.setUseItemInHand(Event.Result.DENY);
        Bukkit.getScheduler().runTask(this,()->stackPuzzle(player,support,pos,face,hand));
    }
    private void stackPuzzle(Player player,PuzzleBlock support,Position pos,org.bukkit.block.BlockFace face,ItemStack held){
        if(!player.isOnline()||!player.hasPermission("zblocks.admin")||blocks.size()>=maxBlocks||!intact(support)||!room(pos))return;
        var inHand=player.getInventory().getItemInMainHand();if(!inHand.isSimilar(held)||inHand.getAmount()<1)return;
        PuzzleBlock.Kind kind;PuzzleBlock.Color color;try{kind=PuzzleBlock.parseKind(puzzleKind(held));color=puzzleColor(held);}catch(IllegalArgumentException ex){return;}
        if(kind==PUSH&&blocks.snapshot().stream().anyMatch(existing->existing.kind==PUSH&&existing.origin.equals(pos)))return;
        if((kind==SWITCH_OFF||kind==SWITCH_ON)&&!worldBlock(support).getRelative(org.bukkit.block.BlockFace.DOWN).getType().isSolid()&&face!=org.bukkit.block.BlockFace.UP)return;
        if(!allowed("place",support.position,pos,player))return;
        var target=LoadedBlocks.at(pos);var before=target.getState();
        target.setType(material(kind,color),false);
        var event=new BlockPlaceEvent(target,before,worldBlock(support),held,player,true,EquipmentSlot.HAND);
        dispatchingStack=true;try{Bukkit.getPluginManager().callEvent(event);}finally{dispatchingStack=false;}
        before.update(true,false);if(event.isCancelled()||!event.canBuild()||blocks.contains(pos))return;
        String visual=material(kind,color).createBlockData().getAsString();
        if(kind==SWITCH_OFF||kind==SWITCH_ON)visual="minecraft:lever[face=floor,facing=north,powered=false]";
        var p=new PuzzleBlock(UUID.randomUUID(),kind,color,pos,pos,before.getBlockData().getAsString(),visual);orientEdge(p,face);p.active=kind==SWITCH_ON;if(kind==BOMB)p.fuse=8;
        blocks.add(p);dirty=true;if(!save()){blocks.remove(p);dirty=true;player.sendMessage("Puzzle could not be saved; item retained.");return;}
        render(p);if(kind==BOMB)sound(p.position,"fuse",Sound.ENTITY_TNT_PRIMED,1,1);
        if(player.getGameMode()!=GameMode.CREATIVE)inHand.setAmount(inHand.getAmount()-1);
    }
    @EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true) public void capturePlacementFace(PlayerInteractEvent event){
        if(event.getAction()==Action.RIGHT_CLICK_BLOCK&&event.getHand()==EquipmentSlot.HAND&&puzzleKind(event.getItem())!=null)placementFaces.put(event.getPlayer().getUniqueId(),new PlacementFace(Bukkit.getCurrentTick(),event.getBlockFace()));
    }
    private org.bukkit.block.BlockFace placementFace(Player player){
        var captured=placementFaces.remove(player.getUniqueId());
        if(captured!=null&&captured.tick==Bukkit.getCurrentTick())return captured.face;
        var hit=player.rayTraceBlocks(6,FluidCollisionMode.NEVER);return hit==null?player.getFacing().getOppositeFace():hit.getHitBlockFace();
    }
    private void orientEdge(PuzzleBlock p,org.bukkit.block.BlockFace face){
        if(p.kind!=BROKEN_EDGE)return;var facing=PuzzleFacing.clicked(face);p.yaw=facing.yaw();p.edgeTilt=facing.tilt();
    }
    @EventHandler(priority=EventPriority.HIGH) public void interact(PlayerInteractEvent e){
        if(dispatchingHitbox)return;
        if(e.getHand()!=EquipmentSlot.HAND||e.getClickedBlock()==null)return;
        if(e.getAction()!=Action.LEFT_CLICK_BLOCK&&e.getAction()!=Action.RIGHT_CLICK_BLOCK)return;
        // Respect explicit denials from protection plugins; adventure-mode item denial is independent.
        if(e.useInteractedBlock()==Event.Result.DENY)return;
        PuzzleBlock p=blocks.at(LoadedBlocks.position(e.getClickedBlock()));if(p==null)return;
        if(p.kind==ACTIVATE&&e.getAction()==Action.LEFT_CLICK_BLOCK&&PuzzleInteraction.weapon(e.getPlayer().getInventory().getItemInMainHand().getType())){e.setUseInteractedBlock(Event.Result.DENY);stopNativeMining(e.getPlayer());operate(p,e.getPlayer());return;}
        if(p.kind==PUSH&&e.getAction()==Action.RIGHT_CLICK_BLOCK)return;
        if(p.kind==PUSH&&e.getAction()==Action.LEFT_CLICK_BLOCK&&!holdingPickaxe(e.getPlayer())){
            e.setUseInteractedBlock(Event.Result.DENY);e.setUseItemInHand(Event.Result.DENY);stopNativeMining(e.getPlayer());operate(p,e.getPlayer());return;
        }
        if(e.getAction()==Action.LEFT_CLICK_BLOCK&&instantRemoval(p,e.getPlayer())){e.setUseInteractedBlock(Event.Result.DENY);removePlayer(p,e.getPlayer(),!PuzzleHitboxes.custom(p));return;}
        if(e.getAction()==Action.LEFT_CLICK_BLOCK){
            if(!canMine(p.position,e.getPlayer())){e.setUseInteractedBlock(Event.Result.DENY);e.setUseItemInHand(Event.Result.DENY);}return;
        }
        if(p.kind==JUKEBOX||PuzzleInteraction.building(e.getPlayer().isSneaking(),e.getItem()!=null&&e.getItem().getType().isBlock()))return;
        e.setUseInteractedBlock(Event.Result.DENY);e.setUseItemInHand(Event.Result.DENY);
        operate(p,e.getPlayer());
    }
    @EventHandler public void quit(PlayerQuitEvent e){lastInteraction.remove(e.getPlayer().getUniqueId());stopNativeMining(e.getPlayer());}
    private String puzzleKind(ItemStack item){
        String tag=TaggedItems.kind(item,itemKind);if(tag!=null)return tag;
        var kind=item==null?null:PuzzleModels.kindFromNexoId(com.nexomc.nexo.api.NexoItems.idFromItem(item));return kind==null?null:kind.name();
    }
    private PuzzleBlock.Color puzzleColor(ItemStack item){
        String tag=item.getItemMeta().getPersistentDataContainer().get(itemColor,PersistentDataType.STRING);
        return tag==null?PuzzleModels.colorFromNexoId(com.nexomc.nexo.api.NexoItems.idFromItem(item)):PuzzleBlock.parseColor(tag);
    }
    private void upgradeInventory(org.bukkit.inventory.Inventory inventory){
        for(int slot=0;slot<inventory.getSize();slot++){
            var old=inventory.getItem(slot);String tag=puzzleKind(old);if(tag==null)continue;
            try{
                var kind=PuzzleBlock.parseKind(tag);var color=puzzleColor(old);
                Position temp=new Position(new UUID(0,0),0,0,0);var sample=new PuzzleBlock(UUID.randomUUID(),kind,color,temp,temp,"minecraft:air","minecraft:air");sample.active=kind==SWITCH_ON;
                var key=PuzzleModels.inventoryKey(sample);if(NexoIntegration.id(key).equals(com.nexomc.nexo.api.NexoItems.idFromItem(old))){var meta=old.getItemMeta();meta.displayName(Component.text(PuzzleNames.item(kind,color)));old.setItemMeta(meta);continue;}
                var updated=NexoIntegration.item(key);updated.setAmount(old.getAmount());TaggedItems.tag(updated,itemKind,kind.name());var meta=updated.getItemMeta();meta.getPersistentDataContainer().set(itemColor,PersistentDataType.STRING,color.name());meta.displayName(Component.text(PuzzleNames.item(kind,color)));updated.setItemMeta(meta);inventory.setItem(slot,updated);
            }catch(IllegalArgumentException ex){getLogger().warning("Unrecognized legacy puzzle item left unchanged: "+tag);}
        }
    }
    @EventHandler public void join(PlayerJoinEvent e){upgradeInventory(e.getPlayer().getInventory());}
    @EventHandler public void inventory(org.bukkit.event.inventory.InventoryOpenEvent e){upgradeInventory(e.getInventory());upgradeInventory(e.getPlayer().getInventory());}
    @EventHandler(priority=EventPriority.LOWEST) public void retainPlacementBacking(BlockPlaceEvent event){
        if(dispatchingCreativeBreak)return;
        var p=blocks.at(LoadedBlocks.position(event.getBlockAgainst()));if(p==null||PuzzleHitboxes.custom(p)||!intact(p))return;
        Position before=p.position;
        Bukkit.getScheduler().runTask(this,()->{
            // Restore only the carrier which existed before this placement. Never
            // overwrite the newly placed neighbor or resurrect a removed puzzle.
            if(blocks.at(before)!=p||!p.position.equals(before)||p.destroyed)return;
            var backing=LoadedBlocks.at(before);if(backing!=null&&backing.getType().isAir())render(p);
        });
    }
    @EventHandler(priority=EventPriority.HIGH,ignoreCancelled=true) public void placementPermission(BlockPlaceEvent e){
        if(puzzleKind(e.getItemInHand())!=null&&(!e.getPlayer().hasPermission("zblocks.admin")||blocks.size()>=maxBlocks))e.setCancelled(true);
    }
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true) public void placed(BlockPlaceEvent e){
        if(dispatchingStack)return;
        String nexoId=com.nexomc.nexo.api.NexoItems.idFromItem(e.getItemInHand());
        if(!PuzzlePlacement.nativeEvent(nexoId,nexoId!=null&&com.nexomc.nexo.api.NexoFurniture.isFurniture(nexoId)))return;
        String tag=puzzleKind(e.getItemInHand());if(tag==null)return;
        PuzzleBlock.Kind kind;PuzzleBlock.Color color;
        try{kind=PuzzleBlock.parseKind(tag);color=puzzleColor(e.getItemInHand());}
        catch(IllegalArgumentException ex){return;}
        Position pos=LoadedBlocks.position(e.getBlockPlaced());String visual=e.getBlockPlaced().getBlockData().getAsString();
        // Placement is finalized before registration; original is air, not a consumed placement snapshot.
        Bukkit.getScheduler().runTask(this,()->{
            Block b=LoadedBlocks.at(pos);if(b==null||blocks.contains(pos)||blocks.size()>=maxBlocks||!b.getBlockData().matches(Bukkit.createBlockData(visual)))return;
            if(kind==PUSH&&blocks.snapshot().stream().anyMatch(p->p.kind==PUSH&&p.origin.equals(pos)))return;
            if(!allowed("place",pos,pos,e.getPlayer()))return;
            PuzzleBlock p=new PuzzleBlock(UUID.randomUUID(),kind,color,pos,pos,"minecraft:air",visual);
            orientEdge(p,e.getBlockAgainst().getFace(e.getBlockPlaced()));p.active=kind==SWITCH_ON;if(kind==BOMB){p.fuse=8;sound(p.position,"fuse",Sound.ENTITY_TNT_PRIMED,1,1);}blocks.add(p);render(p);dirty=true;save();
        });
    }
    @EventHandler(priority=EventPriority.HIGH,ignoreCancelled=true) public void nexoPlace(com.nexomc.nexo.api.events.furniture.NexoFurniturePlaceEvent e){
        String id=com.nexomc.nexo.api.NexoItems.idFromItem(e.getItemInHand());if(id==null||!id.startsWith("zblocks_"))return;
        String tag=puzzleKind(e.getItemInHand());if(tag==null){e.setCancelled(true);return;}
        Position pos=LoadedBlocks.position(e.getBlock());
        if(!e.getPlayer().hasPermission("zblocks.admin")||blocks.contains(pos)||blocks.size()>=maxBlocks||LoadedBlocks.livingOccupant(pos)){e.setCancelled(true);return;}
        var kind=PuzzleBlock.parseKind(tag);var color=puzzleColor(e.getItemInHand());
        if((kind==SWITCH_OFF||kind==SWITCH_ON)&&!e.getBlock().getRelative(org.bukkit.block.BlockFace.DOWN).getType().isSolid()){e.setCancelled(true);return;}
        if(kind==PUSH&&blocks.snapshot().stream().anyMatch(p->p.kind==PUSH&&p.origin.equals(pos))||!allowed("place",pos,pos,e.getPlayer())){e.setCancelled(true);return;}
        var clickedFace=placementFace(e.getPlayer());
        e.getBaseEntity().setPersistent(false);
        Bukkit.getScheduler().runTask(this,()->{if(e.isCancelled()||!e.getBaseEntity().isValid())return;
            // Always retire Nexo's temporary placement, even if a prior handler registered this cell.
            NexoIntegration.remove(e.getBaseEntity());
            if(LoadedBlocks.at(pos)==null||blocks.contains(pos))return;
            String visual=material(kind,color).createBlockData().getAsString();
            if(kind==SWITCH_OFF||kind==SWITCH_ON)visual="minecraft:lever[face=floor,facing=north,powered=false]";
            var p=new PuzzleBlock(UUID.randomUUID(),kind,color,pos,pos,"minecraft:air",visual);orientEdge(p,clickedFace);p.active=kind==SWITCH_ON;if(kind==BOMB){p.fuse=8;sound(p.position,"fuse",Sound.ENTITY_TNT_PRIMED,1,1);}blocks.add(p);dirty=true;
            if(!save()){blocks.remove(p);if(e.getPlayer().getGameMode()!=GameMode.CREATIVE){var refund=e.getItemInHand().clone();refund.setAmount(1);e.getPlayer().getInventory().addItem(refund).values().forEach(item->e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(),item));}e.getPlayer().sendMessage("Puzzle could not be saved; item returned. Check server storage.");return;}render(p);
        });
    }
    @EventHandler(priority=EventPriority.HIGH,ignoreCancelled=true) public void projectile(ProjectileHitEvent e){
        if(dispatchingArrow)return;

        Position hit=e.getHitBlock()!=null?LoadedBlocks.position(e.getHitBlock()):e.getHitEntity()!=null&&EntityHitboxes.internal(e.getHitEntity())?new Position(e.getHitEntity().getWorld().getUID(),e.getHitEntity().getLocation().getBlockX(),e.getHitEntity().getLocation().getBlockY(),e.getHitEntity().getLocation().getBlockZ()):null;
        PuzzleBlock p=hit==null?null:blocks.at(hit);if(p==null||p.kind!=ACTIVATE||!intact(p))return;
        if(!(e.getEntity().getShooter() instanceof Player player)||!player.hasPermission("zblocks.use"))return;
        if(allowed("arrow",p.position,p.position,player)){p.manualActive=!p.manualActive;dirty=true;updateControls();e.getEntity().remove();}
    }
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true) public void launch(org.bukkit.event.entity.ProjectileLaunchEvent e){
        if(arrows.size()<512&&e.getEntity() instanceof Projectile arrow&&arrow.getShooter() instanceof Player player&&player.hasPermission("zblocks.use"))arrows.put(arrow.getUniqueId(),arrow.getLocation());
    }
    private void tickArrows(){
        for(UUID id:List.copyOf(arrows.keySet())){
            var entity=Bukkit.getEntity(id);if(!(entity instanceof Projectile arrow)||!arrow.isValid()||!(arrow.getShooter() instanceof Player player)){arrows.remove(id);continue;}
            Location from=arrows.put(id,arrow.getLocation()),to=arrow.getLocation();if(!from.getWorld().equals(to.getWorld())){arrows.remove(id);continue;}
            var start=from.toVector();var end=to.toVector();double length=start.distance(end);if(length<1e-8){if(arrow instanceof AbstractArrow stuck&&stuck.isInBlock())arrows.remove(id);continue;}if(length>16){arrows.remove(id);continue;}
            var direction=end.clone().subtract(start).normalize();var world=from.getWorld();boolean loaded=Math.min(from.getY(),to.getY())>=world.getMinHeight()&&Math.max(from.getY(),to.getY())<world.getMaxHeight();
            // Check the whole horizontal envelope, including brief corner crossings.
            for(int cx=Math.min(from.getBlockX(),to.getBlockX())>>4;cx<=(Math.max(from.getBlockX(),to.getBlockX())>>4);cx++)for(int cz=Math.min(from.getBlockZ(),to.getBlockZ())>>4;cz<=(Math.max(from.getBlockZ(),to.getBlockZ())>>4);cz++)if(!world.isChunkLoaded(cx,cz))loaded=false;
            if(!loaded){arrows.remove(id);continue;}
            PuzzleBlock target=null;org.bukkit.util.RayTraceResult nearest=null;double best=length+1;
            for(int x=Math.min(from.getBlockX(),to.getBlockX());x<=Math.max(from.getBlockX(),to.getBlockX());x++)
            for(int y=Math.min(from.getBlockY(),to.getBlockY());y<=Math.max(from.getBlockY(),to.getBlockY());y++)
            for(int z=Math.min(from.getBlockZ(),to.getBlockZ());z<=Math.max(from.getBlockZ(),to.getBlockZ());z++){
                var p=blocks.at(new Position(from.getWorld().getUID(),x,y,z));if(p==null||p.kind!=ACTIVATE||!intact(p))continue;
                var hit=PuzzleHitboxes.intersect(p,start,direction,length);if(hit!=null&&start.distance(hit.getHitPosition())<best){best=start.distance(hit.getHitPosition());nearest=hit;target=p;}
            }
            if(target==null)continue;
            var obstruction=from.getWorld().rayTraceBlocks(from,direction,best,FluidCollisionMode.NEVER,true);if(obstruction!=null&&start.distance(obstruction.getHitPosition())<best-1e-5)continue;
            var event=new ProjectileHitEvent(arrow,null,worldBlock(target),nearest.getHitBlockFace());dispatchingArrow=true;try{Bukkit.getPluginManager().callEvent(event);}finally{dispatchingArrow=false;}
            if(!event.isCancelled()&&player.hasPermission("zblocks.use")&&allowed("arrow",target.position,target.position,player)){target.manualActive=!target.manualActive;dirty=true;updateControls();arrow.remove();}
            arrows.remove(id);
        }
    }
    @EventHandler(priority=EventPriority.HIGHEST) public void redstone(BlockRedstoneEvent e){
        PuzzleBlock p=blocks.at(LoadedBlocks.position(e.getBlock()));
        // Depress bases use a native redstone block for output. Overriding their
        // incoming redstone events feeds power back into neighbor recalculation.
        if(PuzzleSignals.overrideIncoming(p))e.setNewCurrent(p.active?15:0);
        if(e.getBlock().getType()==Material.REDSTONE_WIRE){
            // Hold directly powered dust at the native source strength. Do not latch
            // the dust: once the base switches off, vanilla calculates its decay.
            Position dust=LoadedBlocks.position(e.getBlock());
            for(var face:new org.bukkit.block.BlockFace[]{org.bukkit.block.BlockFace.NORTH,org.bukkit.block.BlockFace.SOUTH,org.bukkit.block.BlockFace.EAST,org.bukkit.block.BlockFace.WEST,org.bukkit.block.BlockFace.DOWN}){
                var source=blocks.at(dust.offset(face.getModX(),face.getModY(),face.getModZ()));
                if(PuzzleSignals.output(source)>0&&intact(source)){
                    e.setNewCurrent(PuzzleSignals.dustCurrent(e.getNewCurrent(),true));break;
                }
            }
        }
    }
    private void explode(PuzzleBlock bomb){
        if(!allowed("bomb",bomb.position,bomb.position,null)){bomb.fuse=-1;dirty=true;return;}
        for(PuzzleBlock p:blocks.within(bomb.position,3,5,5))if(p.breakable()&&!p.destroyed&&intact(p)&&allowed("bomb-break",p.position,p.position,null)){
            p.destroyed=true;render(p);
        }
        bomb.fuse=-1;bomb.destroyed=true;render(bomb);dirty=true;
        World world=Bukkit.getWorld(bomb.position.world());
        if(world!=null){
            var center=new Location(world,bomb.position.x()+.5,bomb.position.y()+.25,bomb.position.z()+.5);
            BombEffects.explode(world,center);
        }
        sound(bomb.position,"explosion",Sound.ENTITY_GENERIC_EXPLODE,2,1);
        // Deliberately no vanilla TNT explosion: only registered breakable walls are destroyed.
    }
    private boolean reset(Position center,Player player){
        List<PuzzleBlock> selected=blocks.snapshot().stream().filter(p->p.position.within(center,resetRange,resetRange/2,resetRange/2)||p.kind==PUSH&&p.origin.within(center,resetRange,resetRange/2,resetRange/2)).toList();
        Map<Position,Position> moves=new LinkedHashMap<>();
        for(PuzzleBlock p:selected){
            if(!intact(p)||LoadedBlocks.at(p.origin)==null)return false;
            if(p.kind==PUSH){moves.put(p.position,p.origin);if(LoadedBlocks.livingOccupant(p.origin))return false;}
            if((p.breakable()&&p.destroyed||p.kind==BOMB&&p.destroyed)&&LoadedBlocks.livingOccupant(p.position))return false;
        }
        if(!ResetPlanner.canReset(moves,this::room))return false;
        for(PuzzleBlock p:selected)if(!allowed("reset",p.position,p.kind==PUSH?p.origin:p.position,player))return false;
        // All validation precedes mutation. Clearing every source first supports swaps/cycles.
        for(PuzzleBlock p:selected)if(p.kind==PUSH){blocks.remove(p);worldBlock(p).setType(Material.AIR,true);}
        for(PuzzleBlock p:selected){
            if(p.kind==PUSH){slidingUntil.remove(p.id);p.position=p.origin;p.dx=0;p.dz=0;p.momentum=0;blocks.add(p);}
            if(p.kind==ACTIVATE){p.manualActive=false;p.powered=false;p.active=false;}
            if(p.switchBlock())p.active=p.kind==SWITCH_ON;
            if(p.breakable())p.destroyed=false;
            if(p.kind==BOMB){p.fuse=-1;p.destroyed=false;}
            render(p);
        }
        dirty=true;updateControls();save();return true;
    }
    @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
        if(!gameplayReady){sender.sendMessage("ZBlocks is waiting for Nexo items to finish loading.");return true;}
        if(!sender.hasPermission("zblocks.admin")){sender.sendMessage("You need zblocks.admin.");return true;}
        if(args.length==0||args[0].equalsIgnoreCase("help")){
            sender.sendMessage("/zblocks mark|give <type> [base|blue|cyan|green|magenta|red|yellow] — mark a placeholder or get a tagged item.");
            sender.sendMessage("Types: "+Arrays.toString(PuzzleBlock.Kind.values()).toLowerCase(Locale.ROOT));
            sender.sendMessage("/zblocks diagnostics | inspect | unmark | reset | save. Unmark restores the replaced block; push blocks must be at their origin.");return true;
        }
        if(args[0].equalsIgnoreCase("save")){sender.sendMessage(save()?"Puzzle state saved.":"Save failed; see server log.");return true;}
        if(!(sender instanceof Player player)){sender.sendMessage("Use this command in game.");return true;}
        if(args[0].equalsIgnoreCase("diagnostics")){player.sendMessage("ZBlocks "+getPluginMeta().getVersion()+" | pickaxe="+holdingPickaxe(player)+" | Jade: "+jade.status(player));return true;}
        if(args[0].equalsIgnoreCase("give")){
            try{
                if(args.length<2){sender.sendMessage("Usage: /zblocks give <type> [color]");return true;}
                var kind=PuzzleBlock.parseKind(args[1]);var color=args.length>2?PuzzleBlock.parseColor(args[2]):PuzzleBlock.Color.BASE;
                ItemStack item=new ItemStack(material(kind,color));TaggedItems.tag(item,itemKind,kind.name());
                if(models!=null){
                    Position temp=new Position(player.getWorld().getUID(),0,0,0);
                    PuzzleBlock sample=new PuzzleBlock(UUID.randomUUID(),kind,color,temp,temp,"minecraft:air","minecraft:air");sample.active=kind==SWITCH_ON;
                    item=ItemModels.apply(item,PuzzleModels.inventoryKey(sample));TaggedItems.tag(item,itemKind,kind.name());
                }
                var meta=item.getItemMeta();meta.getPersistentDataContainer().set(itemColor,PersistentDataType.STRING,color.name());
                meta.displayName(Component.text(PuzzleNames.item(kind,color)));item.setItemMeta(meta);
                if(!player.getInventory().addItem(item).isEmpty())sender.sendMessage("Inventory full; make space and retry.");
            }catch(IllegalArgumentException ex){sender.sendMessage("Unknown type/color; use /zblocks help.");}
            return true;
        }
        Block target=player.getTargetBlockExact(6);
        if(hitboxes!=null){Position shaped=hitboxes.target(player,6);if(shaped!=null)target=LoadedBlocks.at(shaped);}
        // Air-backed transient/broken cells cannot be ray-targeted; coordinate form recovers them.
        if(args.length==4&&!args[0].equalsIgnoreCase("mark"))try{
            Position specified=new Position(player.getWorld().getUID(),Integer.parseInt(args[1]),Integer.parseInt(args[2]),Integer.parseInt(args[3]));
            if(!specified.within(new Position(player.getWorld().getUID(),player.getLocation().getBlockX(),player.getLocation().getBlockY(),player.getLocation().getBlockZ()),6,6,6)){sender.sendMessage("Coordinates must be within six blocks.");return true;}
            target=LoadedBlocks.at(specified);
        }catch(NumberFormatException ex){sender.sendMessage("Use integer x y z coordinates.");return true;}
        if(target==null){sender.sendMessage("Look at a block within six blocks, or use /zblocks inspect|unmark|forget <x> <y> <z>.");return true;}
        Position pos=LoadedBlocks.position(target);PuzzleBlock p=blocks.at(pos);
        try{switch(args[0].toLowerCase(Locale.ROOT)){
            case "mark" -> {
                if(args.length<2){sender.sendMessage("Usage: /zblocks mark <type> [color]");break;}
                if(blocks.size()>=maxBlocks||p!=null){sender.sendMessage("Already registered or block limit reached.");break;}
                if(target.getState() instanceof TileState){sender.sendMessage("Use a plain placeholder block; containers and tile data cannot be replaced.");break;}
                PuzzleBlock.Kind kind=PuzzleBlock.parseKind(args[1]);PuzzleBlock.Color color=args.length>2?PuzzleBlock.parseColor(args[2]):PuzzleBlock.Color.BASE;
                if(kind==PUSH&&blocks.snapshot().stream().anyMatch(b->b.kind==PUSH&&b.origin.equals(pos))){sender.sendMessage("This origin belongs to another push block.");break;}
                if(!allowed("mark",pos,pos,player))break;
                String visual=material(kind,color).createBlockData().getAsString();
                if(kind==SWITCH_OFF||kind==SWITCH_ON){
                    Block support=LoadedBlocks.at(pos.offset(0,-1,0));if(support==null||!support.getType().isSolid()){sender.sendMessage("A switch needs a solid block underneath.");break;}
                    visual="minecraft:lever[face=floor,facing=north,powered=false]";
                }
                p=new PuzzleBlock(UUID.randomUUID(),kind,color,pos,pos,target.getBlockData().getAsString(),visual);
                orientEdge(p,placementFace(player));p.active=kind==SWITCH_ON;if(kind==BOMB){p.fuse=8;sound(p.position,"fuse",Sound.ENTITY_TNT_PRIMED,1,1);}blocks.add(p);render(p);dirty=true;save();sender.sendMessage("Registered "+kind.name().toLowerCase(Locale.ROOT)+".");
            }
            case "unmark" -> {
                if(p==null){sender.sendMessage("No registered block here.");break;}
                if(p.kind==PUSH&&!p.position.equals(p.origin)){sender.sendMessage("Reset the push block before removing it.");break;}
                if(target.getState() instanceof org.bukkit.block.Jukebox jukebox&&!jukebox.getRecord().getType().isAir()){sender.sendMessage("Eject the music disc before removing this jukebox.");break;}
                if(!intact(p)){sender.sendMessage("World block was changed externally; use /zblocks forget to remove only its registration.");break;}
                if(!allowed("unmark",pos,pos,player))break;
                BlockData original=Bukkit.createBlockData(p.originalData);
                if(original.getMaterial().isSolid()&&LoadedBlocks.livingOccupant(pos)){sender.sendMessage("Clear this cell before restoring the original block.");break;}
                target.setBlockData(original,true);blocks.remove(p);if(models!=null)models.remove(p.id.toString());if(hitboxes!=null)hitboxes.remove(p.id.toString());dirty=true;updateControls();save();sender.sendMessage("Registration removed and original block restored.");
            }
            case "forget" -> {if(p!=null&&allowed("forget",pos,pos,player)){blocks.remove(p);if(models!=null)models.remove(p.id.toString());if(hitboxes!=null)hitboxes.remove(p.id.toString());dirty=true;updateControls();save();sender.sendMessage("Registration removed; world block retained.");}}
            case "inspect" -> sender.sendMessage(p==null?"No registered block here.":p.kind+" "+p.color+" id="+p.id+" origin="+p.origin.encode()+" intact="+intact(p));
            case "reset" -> sender.sendMessage(reset(pos,player)?"Puzzle reset.":"Reset blocked by unloaded chunks, changed blocks, occupied origins or a protection veto.");
            default -> sender.sendMessage("Use /zblocks help.");
        }}catch(IllegalArgumentException ex){sender.sendMessage("Unknown type/color; use /zblocks help.");}
        return true;
    }
    @Override public List<String> onTabComplete(CommandSender sender,Command command,String alias,String[] args){
        if(!sender.hasPermission("zblocks.admin"))return List.of();
        boolean creation=args.length>1&&(args[0].equalsIgnoreCase("mark")||args[0].equalsIgnoreCase("give"));
        List<String> options=args.length==1?List.of("help","mark","give","unmark","forget","inspect","reset","save","diagnostics"):
            args.length==2&&creation?Arrays.stream(PuzzleBlock.Kind.values()).filter(k->!PuzzleBlock.retired(k)).map(k->k.name().toLowerCase(Locale.ROOT)).toList():
            args.length==3&&creation?Arrays.stream(PuzzleBlock.Color.values()).map(c->c.name().toLowerCase(Locale.ROOT)).toList():List.of();
        return options.stream().filter(s->s.startsWith(args[args.length-1].toLowerCase(Locale.ROOT))).toList();
    }
}
