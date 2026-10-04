package io.github.rbroderi.zblocks;
import io.github.rbroderi.zblocks.internal.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SlideMotionTest {
    @Test void adjacentMovesAcrossChunkBordersAnimateButResetsGravityAndWorldChangesDoNot(){
        var p=new Position(UUID.randomUUID(),15,64,-1);
        assertEquals(6,SlideMotion.duration(p,p.offset(1,0,0),6));
        assertEquals(6,SlideMotion.duration(p,p.offset(0,0,1),6));
        assertEquals(0,SlideMotion.duration(p,p.offset(0,-1,0),6));
        assertEquals(0,SlideMotion.duration(p,p.offset(2,0,0),6));
        assertEquals(0,SlideMotion.duration(p,p.offset(1,0,1),6));
        assertEquals(0,SlideMotion.duration(p,new Position(UUID.randomUUID(),16,64,-1),6));
        assertEquals(20,SlideMotion.duration(p,p.offset(-1,0,0),500));
        assertEquals(0,SlideMotion.duration(p,p.offset(-1,0,0),-2));
    }
}
