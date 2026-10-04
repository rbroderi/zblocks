package io.github.rbroderi.zblocks;
import io.github.rbroderi.zblocks.internal.Position;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PuzzleLightingTest {
    @Test void onlyMatchingPushAndActiveBaseRemainLit(){
        var pos=new Position(UUID.randomUUID(),0,0,0);
        for(var kind:PuzzleBlock.Kind.values())for(boolean active:new boolean[]{false,true})for(boolean matched:new boolean[]{false,true}){
            var p=new PuzzleBlock(UUID.randomUUID(),kind,PuzzleBlock.Color.BASE,pos,pos,"minecraft:air","minecraft:air");p.active=active;
            assertEquals(kind==PuzzleBlock.Kind.DEPRESS&&active||kind==PuzzleBlock.Kind.PUSH&&matched,PuzzleLighting.fullBright(p,matched));
            p.destroyed=true;assertFalse(PuzzleLighting.fullBright(p,matched));
        }
    }
}
