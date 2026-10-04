package io.github.rbroderi.zblocks.internal;
import java.io.*;
import java.util.*;

import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import com.nexomc.nexo.api.*;
import com.nexomc.nexo.api.events.resourcepack.NexoPrePackGenerateEvent;
/** Public Nexo API integration. No Nexo implementation is bundled. */
public final class NexoIntegration implements Listener {
    private static final NamespacedKey POSITION=new NamespacedKey("nexo_consumers","position");
    private static final NamespacedKey OWNER=new NamespacedKey("nexo_consumers","owner");
    private final JavaPlugin plugin;
    private final File assets;
    private final YamlConfiguration definitions;
    private final NexoStartupGate gate;
    private org.bukkit.scheduler.BukkitTask waiting;
    private int elapsed;
    public NexoIntegration(JavaPlugin plugin,Runnable onReady) {this(plugin,onReady,definitions->{});}
    public NexoIntegration(JavaPlugin plugin,Runnable onReady,java.util.function.Consumer<YamlConfiguration> configure) {
        this.plugin=plugin;
        assets=new File(plugin.getDataFolder(),"nexo-assets.zip");
        try(var input=Objects.requireNonNull(plugin.getResource("nexo-assets.zip"))) {
            java.nio.file.Files.createDirectories(plugin.getDataFolder().toPath());
            java.nio.file.Files.copy(input,assets.toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }catch(IOException ex){throw new IllegalStateException("Cannot prepare bundled Nexo assets",ex);}
        definitions=YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource("nexo-items.yml")),java.nio.charset.StandardCharsets.UTF_8));
        configure.accept(definitions);
        gate=new NexoStartupGate(this::missing,()->{
            if(waiting!=null)waiting.cancel();
            try{onReady.run();}
            catch(RuntimeException ex){plugin.getLogger().log(java.util.logging.Level.SEVERE,"Gameplay initialization failed after Nexo loaded",ex);Bukkit.getPluginManager().disablePlugin(plugin);}
        },pending->{
            if(waiting!=null)waiting.cancel();
            plugin.getLogger().severe("Nexo items did not become ready: "+pending+". Check preceding Nexo item/mechanic errors; saved data is preserved.");
            Bukkit.getPluginManager().disablePlugin(plugin);
        });
        Bukkit.getPluginManager().registerEvents(this,plugin);
        // A null return during Nexo's asynchronous load means queued, not rejected.
        for(String id:definitions.getKeys(false))NexoItems.registerExternalItem(plugin,Objects.requireNonNull(definitions.getConfigurationSection(id)));
        plugin.getLogger().info("Waiting for Nexo item and furniture registration...");
        waiting=Bukkit.getScheduler().runTaskTimer(plugin,()->{elapsed+=20;gate.check(false,elapsed>=3600);},1,20);
    }
    private List<String> missing(){
        var pending=new ArrayList<String>();
        for(String id:definitions.getKeys(false)){
            var section=Objects.requireNonNull(definitions.getConfigurationSection(id));
            if(!NexoItems.exists(id))pending.add(id+" (item)");
            else if(section.isConfigurationSection("Mechanics.furniture")&&!NexoFurniture.isFurniture(id))pending.add(id+" (furniture)");
        }return pending;
    }
    @EventHandler public void loaded(com.nexomc.nexo.api.events.NexoItemsLoadedEvent event){
        Bukkit.getScheduler().runTask(plugin,()->{
            if(gate.state()!=NexoStartupGate.State.WAITING)return;
            // Also covers registration arriving after Nexo's initial external-item snapshot.
            for(String id:definitions.getKeys(false)){
                var section=Objects.requireNonNull(definitions.getConfigurationSection(id));
                if(!NexoItems.exists(id)||section.isConfigurationSection("Mechanics.furniture")&&!NexoFurniture.isFurniture(id))NexoItems.registerExternalItem(plugin,section);
            }
            gate.check(true,false);
        });
    }
    // Nexo generates its startup pack after loading items, and owns reload generation.
    // Contribute assets below; never start a second writer from a readiness callback.
    @EventHandler public void pack(NexoPrePackGenerateEvent event){if(!event.addResourcePack(assets))throw new IllegalStateException("Nexo could not merge "+assets);}
    public static String id(ModelKey key){return key.namespace()+"_"+key.path().replace('/','_');}
    public static ItemStack item(ModelKey key){var builder=NexoItems.itemFromId(id(key));if(builder==null)throw new IllegalStateException("Missing Nexo item "+id(key));return builder.build().clone();}
    public static ItemDisplay place(JavaPlugin plugin,String id,Position p,String entry){
        var b=LoadedBlocks.at(p);if(b==null)return null;
        // Grid centered, at the cell floor. Config translation lifts block models by half a block.
        ItemDisplay entity=createDisplay(id,b.getLocation().add(.5,0,.5));
        if(entity==null)throw new IllegalStateException("Nexo rejected furniture "+id);
        entity.setPersistent(false);entity.getPersistentDataContainer().set(POSITION,PersistentDataType.STRING,p.encode());
        entity.getPersistentDataContainer().set(OWNER,PersistentDataType.STRING,plugin.getName()+":"+entry);
        return entity;
    }
    public static ItemDisplay createDisplay(String id,Location anchor){
        var mechanic=NexoFurniture.furnitureMechanic(id);
        if(mechanic==null)throw new IllegalStateException("Missing Nexo furniture mechanic "+id);
        if(!mechanic.getHitbox().getBarriers().isEmpty())throw new IllegalStateException("Owned display must not replace backing blocks: "+id);
        // Gameplay has already validated and owns this cell. Nexo's normal placement
        // rejects its solid backing block; use the public check-space=false overload.
        var display=mechanic.place(anchor.clone(),0f,org.bukkit.block.BlockFace.UP,false);
        if(display==null)throw new IllegalStateException("Nexo rejected owned display "+id);
        try{
            Location exact=anchor.clone();exact.setYaw(0);exact.setPitch(0);
            if(!display.teleport(exact))throw new IllegalStateException("Cannot anchor Nexo display "+id);
            display.setRotation(0,0);display.setBillboard(Display.Billboard.FIXED);
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
            boolean water=id.equals("writtenages_stargate_water");
            float scale=id.startsWith("zblocks_")&&!id.startsWith("zblocks_shape_")?1.002f:1f;
            // Minecraft's NONE item-display renderer rotates models 180 degrees about Y.
            // Undo that so the generated cable's directions match its collision geometry.
            display.setTransformation(ModelTransforms.block(scale,water));
            if(id.startsWith("writtenages_wire_")||id.equals("writtenages_superconductor_wire")||water)display.setBrightness(new Display.Brightness(15,15));
            // Nexo initially centers furniture in the block. Rebuild its packet hitboxes
            // after anchoring the base at the cell floor, including quarter-height wire.
            mechanic.getHitbox().refreshHitboxes(display,mechanic);
            return display;
        }catch(RuntimeException ex){remove(display);throw ex;}
    }
    public static void setPosition(Entity entity,Position p){entity.getPersistentDataContainer().set(POSITION,PersistentDataType.STRING,p.encode());}
    public static Position position(Entity entity){String v=entity.getPersistentDataContainer().get(POSITION,PersistentDataType.STRING);if(v==null)return null;try{return Position.decode(v);}catch(IllegalArgumentException ex){return null;}}
    public static boolean owned(Entity entity,JavaPlugin plugin){String s=entity.getPersistentDataContainer().get(OWNER,PersistentDataType.STRING);return s!=null&&s.startsWith(plugin.getName()+":");}
    public static boolean internal(Entity entity){return entity.getPersistentDataContainer().has(OWNER,PersistentDataType.STRING)||NexoFurniture.isFurniture(entity);}
    public static void remove(Entity entity){if(entity==null)return;if(NexoFurniture.isFurniture(entity))NexoFurniture.remove(entity,null,com.nexomc.nexo.utils.drops.Drop.emptyDrop());else entity.remove();}
}
