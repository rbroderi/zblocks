package io.github.rbroderi.zblocks;
import io.github.rbroderi.zblocks.internal.Position;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PuzzleSignalsTest {
    @Test void nativeOutputNeverOverridesIncomingCurrent(){
        var pos=new Position(UUID.randomUUID(),0,0,0);
        for(var kind:PuzzleBlock.Kind.values())for(boolean active:new boolean[]{false,true}){
            var p=new PuzzleBlock(UUID.randomUUID(),kind,PuzzleBlock.Color.BASE,pos,pos,"minecraft:air","minecraft:air");p.active=active;
            assertEquals(p.switchBlock(),PuzzleSignals.overrideIncoming(p),kind.name());
            p.destroyed=true;assertFalse(PuzzleSignals.overrideIncoming(p));
        }
        assertFalse(PuzzleSignals.overrideIncoming(null));
    }
    @Test void stationaryMatchingBlockKeepsFullPowerAndRemovalReleasesDust(){
        var pos=new Position(UUID.randomUUID(),0,0,0);
        var base=new PuzzleBlock(UUID.randomUUID(),PuzzleBlock.Kind.DEPRESS,PuzzleBlock.Color.BLUE,pos,pos,"minecraft:air","minecraft:air");
        base.active=true;
        for(int tick=0;tick<100;tick++)for(int current=0;current<=15;current++){
            assertEquals(15,PuzzleSignals.output(base));
            assertEquals(15,PuzzleSignals.dustCurrent(current,PuzzleSignals.output(base)>0));
        }
        base.active=false;assertEquals(0,PuzzleSignals.output(base));
        for(int current=0;current<=15;current++)assertEquals(current,PuzzleSignals.dustCurrent(current,false));
        base.active=true;base.destroyed=true;assertEquals(0,PuzzleSignals.output(base));
    }
}
