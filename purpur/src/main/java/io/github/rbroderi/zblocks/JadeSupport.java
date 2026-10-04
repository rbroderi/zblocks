package io.github.rbroderi.zblocks;

import io.github.rbroderi.zblocks.internal.Position;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/** Publishes exact registered positions using the existing Nexo Jade bridge protocol. */
final class JadeSupport {
    private final JavaPlugin plugin;
    private final Supplier<List<PuzzleBlock>> puzzles;
    private final Predicate<PuzzleBlock> intact;
    JadeSupport(JavaPlugin plugin,Supplier<List<PuzzleBlock>> puzzles,Predicate<PuzzleBlock> intact){this.plugin=plugin;this.puzzles=puzzles;this.intact=intact;}
    static int packed(Position p,int minY){return ((p.y()-minY)<<8)|((p.z()&15)<<4)|(p.x()&15);}
    static String identity(PuzzleBlock p){return p.kind.name().toLowerCase(Locale.ROOT)+"_"+((p.kind==PuzzleBlock.Kind.PUSH||p.kind==PuzzleBlock.Kind.DEPRESS||p.kind==PuzzleBlock.Kind.START)?p.color.name().toLowerCase(Locale.ROOT):"base");}
    void install(){
        var bundled=YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource("jade-rules.yml")),java.nio.charset.StandardCharsets.UTF_8));
        // Nexo may assign its own item-model component. Send that exact identifier
        // too; never change a display's model merely to make its tooltip match.
        for(var rule:bundled.getMapList("rules")){
            @SuppressWarnings("unchecked") var match=(Map<String,Object>)rule.get("match");
            if(!"item-model".equals(match.get("type")))continue;
            @SuppressWarnings("unchecked") var configured=new ArrayList<>((List<String>)match.get("models"));var models=new ArrayList<String>();
            for(String model:configured){
                var builder=com.nexomc.nexo.api.NexoItems.itemFromId("zblocks_"+model.substring(model.indexOf(':')+1));
                var key=builder==null?null:builder.build().getItemMeta().getItemModel();
                String resolved=key==null?model:key.toString();if(!models.contains(resolved))models.add(resolved);
            }
            match.put("models",models);
        }
        // getMapList returns a new list containing the mutable rule maps.
        try{bundled.save(new File(plugin.getDataFolder(),"jade-rules.yml"));}catch(IOException ex){plugin.getLogger().warning("Cannot export Jade rules: "+ex.getMessage());}
        var bridge=Bukkit.getPluginManager().getPlugin("NexoJadeBridge");
        if(bridge==null){plugin.getLogger().info("Jade identity data enabled; install NexoJadeBridge and its Fabric client companion for tooltips.");refresh();return;}
        try{
            var target=new File(bridge.getDataFolder(),"config.yml");
            var config=new YamlConfiguration();config.load(target);
            if(config.getInt("schema-version")!=1)throw new IOException("Unsupported Jade config schema");
            var existing=config.getMapList("rules");var merged=new ArrayList<Map<?,?>>();
            for(var rule:existing)if(!String.valueOf(rule.get("id")).startsWith("zblocks-auto-"))merged.add(rule);
            // Specific position rules precede any generic material overrides.
            merged.addAll(0,bundled.getMapList("rules"));
            if(merged.size()>128)throw new IOException("Combined Jade config would exceed 128 rules; merge the exported jade-rules.yml selectively");
            if(!merged.equals(existing)){
                Files.copy(target.toPath(),target.toPath().resolveSibling("config-before-zblocks-"+System.currentTimeMillis()+".yml"));
                config.set("rules",merged);config.save(target);
                if(bridge.isEnabled())Bukkit.getScheduler().runTask(plugin,()->Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"nexojade:nexojade reload"));
                plugin.getLogger().info("Installed ZBlocks Jade rules; existing unrelated rules preserved and config backed up.");
            }
        }catch(Exception ex){plugin.getLogger().warning("Could not install Jade rules automatically: "+ex.getMessage()+". Merge plugins/ZBlocks/jade-rules.yml into the bridge config, then /nexojade reload.");}
        refresh();
    }
    String status(org.bukkit.entity.Player player){
        var bridge=Bukkit.getPluginManager().getPlugin("NexoJadeBridge");
        return "server="+(bridge==null?"MISSING":bridge.getPluginMeta().getVersion()+" enabled="+bridge.isEnabled())
            +" clientQuery="+player.getListeningPluginChannels().contains("nexo_jade:reply_v2")
            +" clientRules="+player.getListeningPluginChannels().contains("nexo_jade:rules_v2");
    }
    private record CellChunk(UUID world,int x,int z){}
    void refresh(){
        var grouped=new HashMap<CellChunk,List<PuzzleBlock>>();
        for(var p:puzzles.get())grouped.computeIfAbsent(new CellChunk(p.position.world(),p.position.x()>>4,p.position.z()>>4),k->new ArrayList<>()).add(p);
        for(var world:Bukkit.getWorlds())for(var chunk:world.getLoadedChunks())write(chunk,grouped.getOrDefault(new CellChunk(world.getUID(),chunk.getX(),chunk.getZ()),List.of()));
    }
    void refreshChunk(Chunk chunk){write(chunk,puzzles.get().stream().filter(p->p.position.world().equals(chunk.getWorld().getUID())&&(p.position.x()>>4)==chunk.getX()&&(p.position.z()>>4)==chunk.getZ()).toList());}
    private void write(Chunk chunk,List<PuzzleBlock> entries){
        var positions=new HashMap<NamespacedKey,List<Integer>>();
        for(var p:entries)if(!p.destroyed&&!(p.transientBlock()&&!p.active)&&intact.test(p))positions.computeIfAbsent(new NamespacedKey(plugin,"jade_"+identity(p)),k->new ArrayList<>()).add(packed(p.position,chunk.getWorld().getMinHeight()));
        var data=chunk.getPersistentDataContainer();
        for(var key:Set.copyOf(data.getKeys()))if(key.getNamespace().equals("zblocks")&&key.getKey().startsWith("jade_")&&!positions.containsKey(key))data.remove(key);
        positions.forEach((key,list)->{int[] value=list.stream().mapToInt(Integer::intValue).sorted().toArray();if(!Arrays.equals(value,data.get(key,PersistentDataType.INTEGER_ARRAY)))data.set(key,PersistentDataType.INTEGER_ARRAY,value);});
    }
}
