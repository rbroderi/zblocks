package io.github.rbroderi.zblocks;
import java.util.UUID;
import io.github.rbroderi.zblocks.internal.Position;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TransientInventoryTest {
    @Test void bothTransientItemsHaveVisibleIconsWhileWorldStatesStayIndependent(){
        var pos=new Position(UUID.randomUUID(),0,0,0);
        for(var kind:new PuzzleBlock.Kind[]{PuzzleBlock.Kind.TRANSIENT,PuzzleBlock.Kind.SECRET_TRANSIENT}){
            var p=new PuzzleBlock(UUID.randomUUID(),kind,PuzzleBlock.Color.BASE,pos,pos,"minecraft:air","minecraft:glass");
            String prefix=kind.name().toLowerCase(java.util.Locale.ROOT);
            assertEquals(prefix+"_on",PuzzleModels.inventoryKey(p).path());
            assertEquals(prefix+"_off",PuzzleModels.key(p,false,false).path());assertFalse(p.active);
            p.active=true;assertEquals(prefix+"_on",PuzzleModels.key(p,false,false).path());
        }
    }
}
