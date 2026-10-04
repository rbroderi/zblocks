package io.github.rbroderi.zblocks;
import io.github.rbroderi.zblocks.internal.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PuzzleHitboxesTest {
    @Test void arrowDetectionUsesTheControllerShapeAndRespectsSegmentLength(){
        Position pos=new Position(UUID.randomUUID(),10,20,30);var p=new PuzzleBlock(UUID.randomUUID(),PuzzleBlock.Kind.ACTIVATE,PuzzleBlock.Color.BASE,pos,pos,"minecraft:air","minecraft:stone");
        var direction=new org.bukkit.util.Vector(1,0,0);
        assertEquals(10.0625,PuzzleHitboxes.intersect(p,new org.bukkit.util.Vector(9,20.1,30.5),direction,3).getHitPosition().getX(),1e-9);
        assertEquals(10.25,PuzzleHitboxes.intersect(p,new org.bukkit.util.Vector(9,20.8,30.5),direction,3).getHitPosition().getX(),1e-9);
        assertNull(PuzzleHitboxes.intersect(p,new org.bukkit.util.Vector(9,20.1,30.5),direction,1));
        assertNull(PuzzleHitboxes.intersect(p,new org.bukkit.util.Vector(9,20.95,30.5),direction,3));
        assertNull(PuzzleHitboxes.intersect(p,new org.bukkit.util.Vector(9,20.8,30.1),direction,3));
    }
    @Test void bombAndControllerAreBoundedWhileFullCubesKeepNativeCollision(){
        Position pos=new Position(UUID.randomUUID(),0,0,0);
        for(var kind:PuzzleBlock.Kind.values()){
            var p=new PuzzleBlock(UUID.randomUUID(),kind,PuzzleBlock.Color.BASE,pos,pos,"minecraft:air","minecraft:stone");
            assertEquals(kind==PuzzleBlock.Kind.BOMB||kind==PuzzleBlock.Kind.ACTIVATE,PuzzleHitboxes.custom(p));
            var cubes=HitboxGeometry.collision(PuzzleHitboxes.boxes(p));assertTrue(cubes.size()<=12);
            for(var b:cubes){assertTrue(b.x()>=0&&b.y()>=0&&b.z()>=0);assertTrue(b.x()+b.size()<=1.00001&&b.y()+b.size()<=1.00001&&b.z()+b.size()<=1.00001);}
        }
    }
}
