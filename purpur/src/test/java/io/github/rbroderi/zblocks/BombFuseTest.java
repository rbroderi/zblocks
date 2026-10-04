package io.github.rbroderi.zblocks;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BombFuseTest {
    @Test void tipTracksShorteningWickAndStaysInsideBombCell(){
        double previous=-1;
        for(int fuse=0;fuse<=8;fuse++){
            var tip=BombFuse.tip(fuse);for(double value:tip)assertTrue(value>=0&&value<=1);
            assertTrue(tip[1]>previous);previous=tip[1];
            if(fuse>0){var last=BombFuse.tip(fuse-1);assertEquals(.5/16,Math.hypot(tip[0]-last[0],tip[1]-last[1]),1e-9);}
        }
    }
}
