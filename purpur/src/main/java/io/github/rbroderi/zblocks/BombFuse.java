package io.github.rbroderi.zblocks;
/** The model wick tilts 22.5 degrees around its Z pivot and loses half a pixel per stage. */
final class BombFuse {
    private BombFuse() {}
    static double[] tip(int fuse){
        double x=6.21176-9.96176,y=8.50637+Math.clamp(fuse,0,8)*.5-12.50637;
        double angle=Math.toRadians(22.5),c=Math.cos(angle),s=Math.sin(angle);
        return new double[]{(9.96176+x*c-y*s)/16,(12.50637+x*s+y*c)/16,8.23235/16};
    }
}
