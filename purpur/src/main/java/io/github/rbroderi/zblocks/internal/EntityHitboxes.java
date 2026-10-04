package io.github.rbroderi.zblocks.internal;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.api.events.furniture.*;
/** Nexo owns collision/interaction entities and their version-specific implementation. */
public final class EntityHitboxes implements Listener,AutoCloseable {
    public enum Action{USE,ATTACK,BREAK}
    public record Click(String id,Position position,Player player,EquipmentSlot hand,Action action,org.bukkit.block.BlockFace face){}
    @FunctionalInterface public interface Handler{void clicked(Click click);}
    private record Entry(Position p,String model,ItemDisplay entity){}
    private java.util.function.BiPredicate<Position,Player> mining=(p,player)->false;
    public void mining(java.util.function.BiPredicate<Position,Player> policy){mining=policy;}
    private final JavaPlugin plugin;private final Handler handler;private final Map<String,Entry> entries=new HashMap<>();
    private final Map<UUID,Integer> lastClick=new HashMap<>();
    public EntityHitboxes(JavaPlugin plugin,String name,Handler handler,java.util.function.Consumer<RuntimeException> failure){this.plugin=plugin;this.handler=handler;Bukkit.getPluginManager().registerEvents(this,plugin);}
    public static boolean internal(Entity entity){return NexoIntegration.internal(entity);}
    public static String shapeKey(List<HitboxGeometry.Box> shape){
        StringBuilder s=new StringBuilder();for(var b:shape)for(double v:new double[]{b.x(),b.y(),b.z(),b.width(),b.height(),b.depth()})s.append(Math.round(v*1000000)).append(',');
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.toString().getBytes(StandardCharsets.UTF_8))).substring(0,12);}catch(NoSuchAlgorithmException ex){throw new AssertionError(ex);}
    }
    public void upsert(String id,Position p,List<HitboxGeometry.Box> clickable,List<HitboxGeometry.Box> solid){
        String model=(plugin.getName().equals("ZBlocks")?"zblocks_shape_":"writtenages_wire_")+shapeKey(solid);
        Entry old=entries.get(id);if(old!=null&&old.p.equals(p)&&old.model.equals(model)&&old.entity.isValid())return;
        remove(id);if(LoadedBlocks.at(p)!=null)entries.put(id,new Entry(p,model,NexoIntegration.place(plugin,model,p,id)));
    }
    public void remove(String id){var e=entries.remove(id);if(e!=null)NexoIntegration.remove(e.entity);}
    public Position target(Player player,double range){var e=NexoFurniture.findTargetFurniture(player);return e==null||!NexoIntegration.owned(e,plugin)||e.getLocation().distanceSquared(player.getEyeLocation())>range*range?null:NexoIntegration.position(e);}
    private void click(Entity entity,Player player,EquipmentSlot hand,Action action,org.bukkit.block.BlockFace face){
        if(!entity.isValid()||!NexoIntegration.owned(entity,plugin)||hand!=EquipmentSlot.HAND||player.getGameMode()==GameMode.SPECTATOR)return;
        Position p=NexoIntegration.position(entity);if(p==null||LoadedBlocks.at(p)==null)return;
        int tick=Bukkit.getCurrentTick();if(Objects.equals(lastClick.put(player.getUniqueId(),tick),tick))return;
        handler.clicked(new Click("",p,player,hand,action,face==null?player.getFacing().getOppositeFace():face));
    }
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void use(NexoFurnitureInteractEvent e){if(!NexoIntegration.owned(e.getBaseEntity(),plugin))return;e.setCanRotate(Event.Result.DENY);e.setCancelled(true);click(e.getBaseEntity(),e.getPlayer(),e.getHand(),Action.USE,e.getBlockFace());}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void damage(NexoFurnitureDamageEvent e){if(!NexoIntegration.owned(e.getBaseEntity(),plugin))return;if(mining.test(NexoIntegration.position(e.getBaseEntity()),e.getPlayer()))return;e.setCancelled(true);click(e.getBaseEntity(),e.getPlayer(),EquipmentSlot.HAND,Action.ATTACK,null);}
    @EventHandler(priority=EventPriority.HIGHEST) public void breaking(NexoFurnitureBreakEvent e){if(!NexoIntegration.owned(e.getBaseEntity(),plugin))return;boolean allowed=!e.isCancelled();e.setCancelled(true);if(allowed)click(e.getBaseEntity(),e.getPlayer(),EquipmentSlot.HAND,Action.BREAK,null);}
    @EventHandler public void unload(ChunkUnloadEvent e){for(String id:List.copyOf(entries.keySet())){var p=entries.get(id).p;if(p.world().equals(e.getWorld().getUID())&&(p.x()>>4)==e.getChunk().getX()&&(p.z()>>4)==e.getChunk().getZ())remove(id);}}
    public void close(){List.copyOf(entries.keySet()).forEach(this::remove);HandlerList.unregisterAll(this);lastClick.clear();}
}
