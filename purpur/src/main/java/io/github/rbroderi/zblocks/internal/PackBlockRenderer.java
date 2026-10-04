package io.github.rbroderi.zblocks.internal;
import java.util.*;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
/** Puzzle visuals are Nexo furniture; gameplay continues to own its saved backing cells. */
public final class PackBlockRenderer implements AutoCloseable,org.bukkit.event.Listener {
    private record Entry(Position p,ModelKey key,ItemDisplay entity) {}
    private record Retry(Position p,ModelKey key,int tick) {}
    private final JavaPlugin plugin;private final Map<String,Entry> entries=new HashMap<>();
    private final Map<String,Retry> retries=new HashMap<>();
    public PackBlockRenderer(JavaPlugin plugin,String tag,UUID ignored){this.plugin=plugin;Bukkit.getPluginManager().registerEvents(this,plugin);}
    public boolean ready(Player player){return true;}
    public void upsert(String id,Position p,ModelKey key,Material ignored,BlockData mask,List<HitboxGeometry.Box> shape){
        Entry old=entries.get(id);if(old!=null&&old.p.equals(p)&&old.key.equals(key)&&old.entity.isValid())return;
        if(LoadedBlocks.at(p)==null){remove(id);return;}
        Retry retry=retries.get(id);if(retry!=null&&retry.p.equals(p)&&retry.key.equals(key)&&Bukkit.getCurrentTick()<retry.tick)return;
        try{
            if(old!=null&&old.entity.isValid()){
                var entity=old.entity;
                if(!old.key.equals(key))entity.setItemStack(NexoIntegration.item(key));
                if(!old.p.equals(p)){
                    entity.setTeleportDuration(SlideMotion.duration(old.p,p,plugin.getConfig().getInt("push-slide-ticks",6)));
                    var destination=LoadedBlocks.at(p).getLocation().add(.5,0,.5);
                    if(!entity.teleport(destination))throw new IllegalStateException("Cannot slide puzzle display");
                    NexoIntegration.setPosition(entity,p);
                }
                entries.put(id,new Entry(p,key,entity));retries.remove(id);return;
            }
            var entity=NexoIntegration.place(plugin,NexoIntegration.id(key),p,id);
            if(old!=null)NexoIntegration.remove(old.entity);
            entries.put(id,new Entry(p,key,entity));retries.remove(id);
        }catch(RuntimeException ex){
            if(retry==null)plugin.getLogger().log(java.util.logging.Level.WARNING,"Cannot render Nexo model "+key+" at "+p.encode()+"; retaining any existing display and retrying later",ex);
            retries.put(id,new Retry(p,key,Bukkit.getCurrentTick()+200));
        }
    }
    public void rotation(String id,int yaw,boolean edge,int tilt){
        Entry e=entries.get(id);if(e==null||!e.entity.isValid())return;
        if(edge)e.entity.setTransformation(new org.bukkit.util.Transformation(new org.joml.Vector3f(0,.5f,0),new org.joml.Quaternionf().rotateX((float)Math.toRadians(tilt)).rotateY((float)Math.PI),new org.joml.Vector3f(1.002f),new org.joml.Quaternionf()));
        if(e.entity.getLocation().getYaw()!=yaw)e.entity.setRotation(yaw,0);
    }
    public void brightness(String id,boolean fullBright){
        Entry entry=entries.get(id);if(entry==null||!entry.entity.isValid())return;
        var brightness=fullBright?new Display.Brightness(15,15):null;
        if(!Objects.equals(entry.entity.getBrightness(),brightness))entry.entity.setBrightness(brightness);
    }
    public void remove(String id){retries.remove(id);Entry e=entries.remove(id);if(e!=null)NexoIntegration.remove(e.entity);}
    public void sound(Position p,String sound,Sound fallback,float volume,float pitch){var b=LoadedBlocks.at(p);if(b!=null)b.getWorld().playSound(b.getLocation().add(.5,.5,.5),SoundKeys.custom(sound),volume,pitch);}
    @org.bukkit.event.EventHandler public void unload(org.bukkit.event.world.ChunkUnloadEvent e){for(String id:List.copyOf(entries.keySet())){var p=entries.get(id).p;if(p.world().equals(e.getWorld().getUID())&&(p.x()>>4)==e.getChunk().getX()&&(p.z()>>4)==e.getChunk().getZ())remove(id);}}
    public void close(){List.copyOf(entries.keySet()).forEach(this::remove);retries.clear();org.bukkit.event.HandlerList.unregisterAll(this);}
}
