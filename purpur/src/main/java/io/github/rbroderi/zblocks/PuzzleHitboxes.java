package io.github.rbroderi.zblocks;
import io.github.rbroderi.zblocks.internal.HitboxGeometry.Box;
import java.util.List;

/** Bounded approximations of the original model shapes; full cubes stay native. */
final class PuzzleHitboxes {
    static org.bukkit.util.RayTraceResult intersect(PuzzleBlock p,org.bukkit.util.Vector start,org.bukkit.util.Vector direction,double length){
        org.bukkit.util.RayTraceResult nearest=null;double best=length+1;
        for(var shape:boxes(p)){
            var pos=p.position;var box=new org.bukkit.util.BoundingBox(pos.x()+shape.x(),pos.y()+shape.y(),pos.z()+shape.z(),pos.x()+shape.x()+shape.width(),pos.y()+shape.y()+shape.height(),pos.z()+shape.z()+shape.depth());
            var hit=box.rayTrace(start,direction,length);if(hit!=null&&start.distance(hit.getHitPosition())<best){best=start.distance(hit.getHitPosition());nearest=hit;}
        }return nearest;
    }
    static boolean custom(PuzzleBlock p){return p.kind==PuzzleBlock.Kind.BOMB||p.kind==PuzzleBlock.Kind.ACTIVATE;}
    static List<Box> boxes(PuzzleBlock p){return switch(p.kind){
        case BOMB -> List.of(new Box(.25,0,.25,.5,.5,.5));
        case ACTIVATE -> List.of(new Box(.0625,0,.0625,.875,.3125,.875),new Box(.25,.3125,.25,.5,.5625,.5));
        default -> List.of();
    };}
}
