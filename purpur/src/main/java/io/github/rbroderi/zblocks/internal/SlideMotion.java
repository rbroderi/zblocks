package io.github.rbroderi.zblocks.internal;
/** Only adjacent horizontal grid moves animate; resets, gravity and worlds snap. */
public final class SlideMotion {
    private SlideMotion(){}
    public static int duration(Position from,Position to,int ticks){
        return from.world().equals(to.world())&&from.y()==to.y()&&Math.abs((long)from.x()-to.x())+Math.abs((long)from.z()-to.z())==1?Math.clamp(ticks,0,20):0;
    }
}
