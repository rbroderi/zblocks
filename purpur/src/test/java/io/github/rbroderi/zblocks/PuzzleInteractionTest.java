package io.github.rbroderi.zblocks;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PuzzleInteractionTest {
    @Test void allFourAdjacentSidesPushAwayWithoutDependingOnCameraYaw(){
        assertArrayEquals(new int[]{1,0},PuzzleInteraction.pushDirection(9,20,10,20));
        assertArrayEquals(new int[]{-1,0},PuzzleInteraction.pushDirection(11,20,10,20));
        assertArrayEquals(new int[]{0,1},PuzzleInteraction.pushDirection(10,19,10,20));
        assertArrayEquals(new int[]{0,-1},PuzzleInteraction.pushDirection(10,21,10,20));
    }
    @Test void remoteDiagonalAndOverlappingPlayersCannotPush(){assertNull(PuzzleInteraction.pushDirection(8,20,10,20));assertNull(PuzzleInteraction.pushDirection(9,19,10,20));assertNull(PuzzleInteraction.pushDirection(10,20,10,20));}
    @Test void fractionalPositionsAndObliqueApproachesStillMoveOnlyOneCardinalCell(){
        assertArrayEquals(new int[]{1,0},PuzzleInteraction.pushSide(9.99,20.99,10,20));
        assertArrayEquals(new int[]{0,-1},PuzzleInteraction.pushSide(10.1,21.9,10,20));
        assertArrayEquals(new int[]{-1,0},PuzzleInteraction.pushSide(-8.1,-9.2,-10,-10));
        assertNull(PuzzleInteraction.pushSide(10.5,20.5,10,20));
    }
    @Test void allPickaxeMaterialsMineAndOtherItemsPush(){for(var material:org.bukkit.Material.values())assertEquals(material.name().endsWith("_PICKAXE"),PuzzleInteraction.pickaxe(material));assertFalse(PuzzleInteraction.pickaxe(null));}
    @Test void holdingABlockAllowsPlacementWithoutSneaking(){assertTrue(PuzzleInteraction.building(true,true));assertTrue(PuzzleInteraction.building(false,true));assertFalse(PuzzleInteraction.building(true,false));}
    @Test void weaponsActivateButPickaxesAndBuildingItemsDoNot(){
        for(String n:new String[]{"DIAMOND_SWORD","IRON_AXE","MACE","TRIDENT"})assertTrue(PuzzleInteraction.weapon(org.bukkit.Material.valueOf(n)));
        for(String n:new String[]{"DIAMOND_PICKAXE","STONE","AIR","BOW"})assertFalse(PuzzleInteraction.weapon(org.bukkit.Material.valueOf(n)));
        assertFalse(PuzzleInteraction.weapon(null));
    }
}
