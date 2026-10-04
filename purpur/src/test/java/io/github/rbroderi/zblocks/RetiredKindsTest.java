package io.github.rbroderi.zblocks;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RetiredKindsTest {
    @Test void removedKindsCannotBeCreatedButLegacyNamesRemainReadable(){
        for(String name:new String[]{"jukebox","cactus"}){
            assertThrows(IllegalArgumentException.class,()->PuzzleBlock.parseKind(name));
            assertTrue(PuzzleBlock.retired(PuzzleBlock.Kind.valueOf(name.toUpperCase(java.util.Locale.ROOT))));
        }
        assertEquals(PuzzleBlock.Kind.PUSH,PuzzleBlock.parseKind("push"));
    }
}
