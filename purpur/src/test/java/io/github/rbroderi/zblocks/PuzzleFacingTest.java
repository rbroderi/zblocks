package io.github.rbroderi.zblocks;
import org.junit.jupiter.api.Test;
import org.bukkit.block.BlockFace;
import static org.junit.jupiter.api.Assertions.*;
class PuzzleFacingTest {
    @Test void edgeFacesPlayerOnAllFourGridDirections(){
        assertEquals(180,PuzzleFacing.placement(PuzzleBlock.Kind.BROKEN_EDGE,BlockFace.SOUTH));
        assertEquals(270,PuzzleFacing.placement(PuzzleBlock.Kind.BROKEN_EDGE,BlockFace.WEST));
        assertEquals(0,PuzzleFacing.placement(PuzzleBlock.Kind.BROKEN_EDGE,BlockFace.NORTH));
        assertEquals(90,PuzzleFacing.placement(PuzzleBlock.Kind.BROKEN_EDGE,BlockFace.EAST));
        assertEquals(0,PuzzleFacing.placement(PuzzleBlock.Kind.PUSH,BlockFace.NORTH));
    }
    @Test void neutralItemsDoNotShowBaseAndColoredItemsKeepColor(){
        assertEquals("ZBlocks push block",PuzzleNames.item(PuzzleBlock.Kind.PUSH,PuzzleBlock.Color.BASE));
        assertEquals("ZBlocks push block blue",PuzzleNames.item(PuzzleBlock.Kind.PUSH,PuzzleBlock.Color.BLUE));
    }
    @Test void sixClickedFacesProduceGridAlignedDistinctOrientations(){
        var orientations=new java.util.HashSet<PuzzleFacing.Orientation>();
        for(var face:new BlockFace[]{BlockFace.UP,BlockFace.DOWN,BlockFace.NORTH,BlockFace.SOUTH,BlockFace.EAST,BlockFace.WEST}){
            var o=PuzzleFacing.clicked(face);assertEquals(0,o.yaw()%90);assertEquals(0,o.tilt()%90);orientations.add(o);
        }
        assertEquals(6,orientations.size());assertEquals(0,PuzzleFacing.clicked(BlockFace.UP).tilt());assertEquals(180,PuzzleFacing.clicked(BlockFace.DOWN).tilt());
    }
    @Test void renderedRidgeNormalMatchesClickedFaceAndOrientationPersists(@org.junit.jupiter.api.io.TempDir java.nio.file.Path dir) throws Exception {
        var index=new io.github.rbroderi.zblocks.internal.BlockIndex<PuzzleBlock>(p->p.position);var world=java.util.UUID.randomUUID();int x=0;
        for(var face:new BlockFace[]{BlockFace.UP,BlockFace.DOWN,BlockFace.NORTH,BlockFace.SOUTH,BlockFace.EAST,BlockFace.WEST}){
            var o=PuzzleFacing.clicked(face);
            var normal=new org.joml.Vector3f(0,1,0).rotateX((float)Math.toRadians(o.tilt())).rotateY((float)Math.toRadians(-o.yaw()));
            assertEquals(face.getModX(),normal.x,1e-5);assertEquals(face.getModY(),normal.y,1e-5);assertEquals(face.getModZ(),normal.z,1e-5);
            var pos=new io.github.rbroderi.zblocks.internal.Position(world,x++,0,0);var p=new PuzzleBlock(java.util.UUID.randomUUID(),PuzzleBlock.Kind.BROKEN_EDGE,PuzzleBlock.Color.BASE,pos,pos,"minecraft:air","minecraft:stone");p.yaw=o.yaw();p.edgeTilt=o.tilt();index.add(p);
        }
        var file=dir.resolve("puzzles.yml");PuzzleStore.save(index,file);var loaded=PuzzleStore.load(file);
        for(var p:index.snapshot()){var restored=loaded.at(p.position);assertEquals(p.yaw,restored.yaw);assertEquals(p.edgeTilt,restored.edgeTilt);}
    }
}
