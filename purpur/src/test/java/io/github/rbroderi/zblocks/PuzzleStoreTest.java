package io.github.rbroderi.zblocks;

import io.github.rbroderi.zblocks.internal.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class PuzzleStoreTest {
    @TempDir Path folder;
    @Test void restartPreservesOriginsMotionAndEveryPuzzleType()throws Exception{
        UUID world=UUID.randomUUID();BlockIndex<PuzzleBlock> index=new BlockIndex<>(b->b.position);int x=0;
        for(var kind:PuzzleBlock.Kind.values()){
            Position origin=new Position(world,x++,30,40),current=origin.offset(0,-15,2);
            PuzzleBlock p=new PuzzleBlock(UUID.randomUUID(),kind,PuzzleBlock.Color.CYAN,origin,current,"minecraft:dirt","minecraft:stone");
            p.active=true;p.manualActive=true;p.powered=true;p.destroyed=true;p.dx=1;p.momentum=1;p.fuse=7;index.add(p);
        }
        Path file=folder.resolve("blocks.yml");PuzzleStore.save(index,file);var loaded=PuzzleStore.load(file);assertEquals(index.size(),loaded.size());
        for(PuzzleBlock p:index.snapshot()){
            PuzzleBlock b=loaded.at(p.position);assertEquals(p.origin,b.origin);assertEquals(p.id,b.id);assertEquals(p.kind,b.kind);assertEquals(p.color,b.color);
            assertTrue(b.active&&b.manualActive&&b.powered&&b.destroyed);assertEquals(1,b.dx);assertEquals(1,b.momentum);assertEquals(7,b.fuse);assertEquals(p.originalData,b.originalData);
        }
    }
    @Test void unknownSchemaAndInvalidRecordsDoNotBecomeEmptyState()throws Exception{
        Path file=folder.resolve("blocks.yml");Files.writeString(file,"schema: 999\n");assertThrows(java.io.IOException.class,()->PuzzleStore.load(file));
        Files.writeString(file,"schema: 1\nblocks:\n  not-a-uuid:\n    kind: PUSH\n");assertThrows(java.io.IOException.class,()->PuzzleStore.load(file));
        assertTrue(Files.readString(file).contains("not-a-uuid"));
    }
    @Test void uncoloredMatchingIsSymmetricAndOtherColorsDoNotMatch(){
        for(var color:PuzzleBlock.Color.values()){assertTrue(PuzzleBlock.Color.BASE.matches(color));assertTrue(color.matches(PuzzleBlock.Color.BASE));}
        assertFalse(PuzzleBlock.Color.BLUE.matches(PuzzleBlock.Color.RED));assertTrue(PuzzleBlock.Color.RED.matches(PuzzleBlock.Color.RED));
    }
}
