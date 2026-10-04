package io.github.rbroderi.zblocks;

import io.github.rbroderi.zblocks.internal.*;
import java.nio.file.Path;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

final class PuzzleStore {
    private PuzzleStore() {}
    static BlockIndex<PuzzleBlock> load(Path file) throws IOException, InvalidConfigurationException {
        YamlConfiguration y=AtomicYaml.load(file);
        if(y.contains("schema")&&y.getInt("schema")!=1)throw new IOException("Unsupported puzzle schema");
        BlockIndex<PuzzleBlock> index=new BlockIndex<>(p->p.position);
        var section=y.getConfigurationSection("blocks");
        if(section==null)return index;
        // Fail the entire load, preserving the original file, rather than quietly losing bad records.
        try { for(String id:section.getKeys(false)) {
            String key="blocks."+id+".";
            PuzzleBlock p=new PuzzleBlock(UUID.fromString(id),PuzzleBlock.Kind.valueOf(y.getString(key+"kind","").toUpperCase(java.util.Locale.ROOT)),
                PuzzleBlock.parseColor(y.getString(key+"color","")),Position.decode(y.getString(key+"origin","")),
                Position.decode(y.getString(key+"position","")),y.getString(key+"original","minecraft:air"),y.getString(key+"visual","minecraft:stone"));
            if(!p.position.world().equals(p.origin.world()))throw new IllegalArgumentException("Cross-world origin");
            p.active=y.getBoolean(key+"active");p.manualActive=y.getBoolean(key+"manual");p.powered=y.getBoolean(key+"powered");p.destroyed=y.getBoolean(key+"destroyed");
            p.edgeTilt=y.getInt(key+"edge-tilt",90);if(p.edgeTilt!=0&&p.edgeTilt!=90&&p.edgeTilt!=180)throw new IllegalArgumentException("Invalid edge tilt");
            p.yaw=y.getInt(key+"yaw",0);if(p.yaw<0||p.yaw>270||p.yaw%90!=0)throw new IllegalArgumentException("Invalid orientation");
            p.fuse=y.getInt(key+"fuse",-1);p.dx=y.getInt(key+"dx");p.dz=y.getInt(key+"dz");p.momentum=y.getInt(key+"momentum");
            if(p.fuse < -1 || p.fuse > 120 || Math.abs(p.dx)+Math.abs(p.dz)>1 || p.momentum<0 || p.momentum>1)throw new IllegalArgumentException("Invalid motion/fuse state");
            index.add(p);
        }} catch(RuntimeException e) {throw new IOException("Invalid puzzle record",e);}
        return index;
    }
    static void save(BlockIndex<PuzzleBlock> index, Path file) throws IOException {
        YamlConfiguration y=new YamlConfiguration();y.set("schema",1);
        for(PuzzleBlock p:index.snapshot()) {
            String key="blocks."+p.id+".";
            y.set(key+"kind",p.kind.name());y.set(key+"color",p.color.name());y.set(key+"origin",p.origin.encode());y.set(key+"position",p.position.encode());
            y.set(key+"original",p.originalData);y.set(key+"visual",p.visualData);y.set(key+"active",p.active);y.set(key+"manual",p.manualActive);y.set(key+"powered",p.powered);y.set(key+"destroyed",p.destroyed);
            y.set(key+"edge-tilt",p.edgeTilt);y.set(key+"yaw",p.yaw);y.set(key+"fuse",p.fuse);y.set(key+"dx",p.dx);y.set(key+"dz",p.dz);y.set(key+"momentum",p.momentum);
        } AtomicYaml.save(y,file);
    }
}
