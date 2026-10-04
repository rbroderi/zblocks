package io.github.rbroderi.zblocks;
import io.github.rbroderi.zblocks.internal.Position;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class JadeSupportTest {
    private static YamlConfiguration resource(String name){return YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(Objects.requireNonNull(JadeSupportTest.class.getResourceAsStream(name))));}
    @Test void tagsEncodeNegativeCoordinatesRelativeToWorldFloor(){assertEquals((64<<8)|255,JadeSupport.packed(new Position(UUID.randomUUID(),-1,0,-1),-64));assertEquals(0,JadeSupport.packed(new Position(UUID.randomUUID(),16,-64,32),-64));}
    @Test void everyVisibleStateHasAnExactModelRuleAndPositionRule(){
        var definitions=resource("/nexo-items.yml");var rules=resource("/jade-rules.yml").getMapList("rules");var models=new HashSet<String>();var keys=new HashSet<String>();var ruleIds=new HashSet<String>();
        assertTrue(rules.size()<128);
        for(var r:rules){assertTrue(ruleIds.add((String)r.get("id")));var match=(Map<?,?>)r.get("match");assertEquals("ZBlocks",match.get("plugin"));
            if(match.get("type").equals("item-model")){for(var model:(List<?>)match.get("models"))assertTrue(models.add((String)model));}
            else{assertEquals("chunk-position-tag",match.get("type"));assertEquals("packed-yzx-min-height",match.get("encoding"));keys.add((String)match.get("key"));}
        }
        for(String id:definitions.getKeys(false))if(!id.startsWith("zblocks_shape_")){
            assertTrue(models.contains("zblocks:"+id.substring(8)),id);
            var kind=PuzzleModels.kindFromNexoId(id);var color=PuzzleModels.colorFromNexoId(id);var pos=new Position(UUID.randomUUID(),0,64,0);
            var p=new PuzzleBlock(UUID.randomUUID(),kind,color,pos,pos,"minecraft:air","minecraft:barrier");assertTrue(keys.contains("zblocks:jade_"+JadeSupport.identity(p)),id);
        }
        assertEquals(67,models.size());
    }
    @Test void nativeCubesHaveNoPacketInteractionHitboxesButPartialShapesKeepTheirs(){
        var defs=resource("/nexo-items.yml");var removal=RemovalSettings.load(new YamlConfiguration(),name->50,msg->fail(msg));removal.configure(defs);
        for(String id:defs.getKeys(false))if(!id.startsWith("zblocks_shape_"))assertTrue(defs.getStringList(id+".Mechanics.furniture.hitbox.interactions").isEmpty(),id);
        assertTrue(defs.getKeys(false).stream().filter(id->id.startsWith("zblocks_shape_")).allMatch(id->!defs.getStringList(id+".Mechanics.furniture.hitbox.interactions").isEmpty()));
    }
}
