package io.github.rbroderi.zblocks;
import java.util.UUID;
import io.github.rbroderi.zblocks.internal.Position;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BombBreakableTest {
    @Test void bombsDestroyWallsButPreserveEdgesAndOtherPuzzleKinds(){
        var pos=new Position(UUID.randomUUID(),0,0,0);
        for(var kind:PuzzleBlock.Kind.values()){
            var p=new PuzzleBlock(UUID.randomUUID(),kind,PuzzleBlock.Color.BASE,pos,pos,"minecraft:air","minecraft:stone");
            assertEquals(kind==PuzzleBlock.Kind.BROKEN||kind==PuzzleBlock.Kind.SUBTLE_BROKEN||kind==PuzzleBlock.Kind.SECRET_BROKEN,p.bombBreakable());
            if(kind==PuzzleBlock.Kind.BROKEN_EDGE)assertTrue(p.breakable(),"Retain reset compatibility for previously destroyed edges");
        }
    }
}
