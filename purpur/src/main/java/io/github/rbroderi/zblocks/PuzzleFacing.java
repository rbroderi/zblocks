package io.github.rbroderi.zblocks;
import org.bukkit.block.BlockFace;
final class PuzzleFacing {
    private PuzzleFacing() {}
    record Orientation(int yaw,int tilt) {}
    static Orientation clicked(BlockFace face){
        return switch(face){case UP->new Orientation(0,0);case DOWN->new Orientation(0,180);case NORTH->new Orientation(180,90);case EAST->new Orientation(270,90);case WEST->new Orientation(90,90);default->new Orientation(0,90);};
    }
    // The original edge model's raised ridge is tilted from the top onto its south face.
    static int placement(PuzzleBlock.Kind kind,BlockFace looking){
        if(kind!=PuzzleBlock.Kind.BROKEN_EDGE)return 0;
        return switch(looking.getOppositeFace()){case NORTH->180;case EAST->270;case WEST->90;default->0;};
    }
}
