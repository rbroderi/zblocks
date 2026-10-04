package io.github.rbroderi.zblocks;
import io.github.rbroderi.zblocks.internal.Position;
import java.util.*;
import com.google.gson.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PuzzleModelsTest {
    @Test void everyRuntimeModelKeyExistsInBuiltResourcePack()throws Exception{
        Set<String> keys=new HashSet<>();
        try(var input=getClass().getResourceAsStream("/model-keys.json")){
            assertNotNull(input);JsonParser.parseString(new String(input.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("models").forEach(e->keys.add(e.getAsString()));
        }
        Position pos=new Position(UUID.randomUUID(),0,0,0);
        for(var kind:PuzzleBlock.Kind.values())for(var color:PuzzleBlock.Color.values())for(boolean active:new boolean[]{false,true})for(boolean powered:new boolean[]{false,true})for(boolean frozen:new boolean[]{false,true})for(boolean matched:new boolean[]{false,true})for(int fuse=-1;fuse<=8;fuse++){
            var p=new PuzzleBlock(UUID.randomUUID(),kind,color,pos,pos,"minecraft:air","minecraft:air");p.active=active;p.powered=powered;p.fuse=fuse;
            assertTrue(keys.contains(PuzzleModels.key(p,frozen,matched).path()),()->"Missing model for "+kind+" "+color);
        }
        assertEquals(69,keys.size());
    }
}
