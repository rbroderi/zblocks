package io.github.rbroderi.zblocks;
import io.github.rbroderi.zblocks.internal.*;
import java.io.*;
import java.util.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class NexoDefinitionsTest {
    @Test void everyPuzzleStateHasANexoDefinitionAndBundledModel()throws Exception{
        var config=YamlConfiguration.loadConfiguration(new InputStreamReader(getClass().getResourceAsStream("/nexo-items.yml")));
        Set<String> files=new HashSet<>();try(var zip=new java.util.zip.ZipInputStream(getClass().getResourceAsStream("/nexo-assets.zip"))){for(java.util.zip.ZipEntry e;(e=zip.getNextEntry())!=null;)files.add(e.getName());}
        for(String id:config.getKeys(false)){
            String furniture=id+".Mechanics.furniture";
            assertEquals("VERY_STRICT",config.getString(furniture+".restricted_rotation"),id);
            assertFalse(config.getBoolean(furniture+".rotatable",true),id);
            assertFalse(config.getBoolean(furniture+".properties.offset_against_blocks",true),id);
            assertTrue(config.getStringList(furniture+".hitbox.barriers").isEmpty(),id);
            assertEquals("0,1,0,0",config.getString(furniture+".properties.left_rotation"),id);
        }
        Position pos=new Position(new UUID(0,0),0,0,0);
        for(var kind:java.util.Arrays.stream(PuzzleBlock.Kind.values()).filter(k->!PuzzleBlock.retired(k)).toList())for(var color:PuzzleBlock.Color.values())for(int state=0;state<4;state++)for(int fuse=-1;fuse<=8;fuse++){
            var p=new PuzzleBlock(UUID.randomUUID(),kind,color,pos,pos,"minecraft:air","minecraft:stone");p.active=(state&1)!=0;p.powered=(state&2)!=0;p.fuse=fuse;
            for(boolean frozen:new boolean[]{false,true})for(boolean matched:new boolean[]{false,true}){
                var key=PuzzleModels.key(p,frozen,matched);String id=NexoIntegration.id(key);assertTrue(config.isConfigurationSection(id),id);
                assertEquals(kind,PuzzleModels.kindFromNexoId(id));assertEquals(kind==PuzzleBlock.Kind.PUSH||kind==PuzzleBlock.Kind.DEPRESS||kind==PuzzleBlock.Kind.START?color:PuzzleBlock.Color.BASE,PuzzleModels.colorFromNexoId(id));
                String model=config.getString(id+".Pack.model");var parts=model.split(":");assertTrue(files.contains("assets/"+parts[0]+"/models/"+parts[1]+".json"),model);
            }
            if(PuzzleHitboxes.custom(p))assertTrue(config.isConfigurationSection("zblocks_shape_"+EntityHitboxes.shapeKey(PuzzleHitboxes.boxes(p))));
        }
    }
}
