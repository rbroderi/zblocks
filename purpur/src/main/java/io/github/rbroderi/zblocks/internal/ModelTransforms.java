package io.github.rbroderi.zblocks.internal;
import org.bukkit.util.Transformation;
import org.joml.*;
/** Block coordinates are centered by the item renderer, which also rotates about Y. */
public final class ModelTransforms {
    private ModelTransforms(){}
    public static Transformation block(float scale,boolean water){
        return new Transformation(new Vector3f(0,water?0:.5f,0),new Quaternionf(0,1,0,0),new Vector3f(scale),new Quaternionf());
    }
}
