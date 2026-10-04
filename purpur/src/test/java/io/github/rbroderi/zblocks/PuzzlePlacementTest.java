package io.github.rbroderi.zblocks;
import java.util.*;
import io.github.rbroderi.zblocks.internal.Position;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PuzzlePlacementTest {
    @Test void nexoProtectionEventCannotRegisterASecondPuzzleForAnyFurnitureState(){
        var definitions=YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(getClass().getResourceAsStream("/nexo-items.yml")));
        int states=0;for(String id:definitions.getKeys(false))if(definitions.isConfigurationSection(id+".Mechanics.furniture")){assertFalse(PuzzlePlacement.nativeEvent(id,true),id);states++;}
        assertEquals(69,states);
    }
    @Test void actualNativeAndLegacyPlacementsStillRegister(){assertTrue(PuzzlePlacement.nativeEvent(null,false));assertTrue(PuzzlePlacement.nativeEvent("zblocks_jukebox",false));assertTrue(PuzzlePlacement.nativeEvent("otherplugin_chair",true));}
    @Test void cleanupSelectsOnlyUnmanagedDuplicatesMatchingTheSavedPuzzle(){
        var pos=new Position(UUID.randomUUID(),0,64,0);var p=new PuzzleBlock(UUID.randomUUID(),PuzzleBlock.Kind.PUSH,PuzzleBlock.Color.BLUE,pos,pos,"minecraft:air","minecraft:barrier");
        assertTrue(PuzzlePlacement.staleFurniture("zblocks_push_blue_off_normal",false,p));
        assertFalse(PuzzlePlacement.staleFurniture("zblocks_push_blue_off_normal",true,p));
        assertFalse(PuzzlePlacement.staleFurniture("zblocks_push_red_off_normal",false,p));
        assertFalse(PuzzlePlacement.staleFurniture("zblocks_push_blue_off_normal",false,null));
        assertFalse(PuzzlePlacement.staleFurniture("otherplugin_chair",false,p));
        assertFalse(PuzzlePlacement.staleFurniture("zblocks_push_invalid",false,p));
    }
    @Test void onlyInvalidLiveBackingRecordsAreEligibleForRepair(){
        var pos=new Position(UUID.randomUUID(),0,64,0);var p=new PuzzleBlock(UUID.randomUUID(),PuzzleBlock.Kind.PUSH,PuzzleBlock.Color.BASE,pos,pos,"minecraft:air","minecraft:air");
        assertTrue(PuzzlePlacement.invalidBacking(p));p.destroyed=true;assertFalse(PuzzlePlacement.invalidBacking(p));p.destroyed=false;p.visualData="minecraft:white_concrete";assertFalse(PuzzlePlacement.invalidBacking(p));
    }
}
