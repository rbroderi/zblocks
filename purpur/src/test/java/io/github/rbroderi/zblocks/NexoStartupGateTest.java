package io.github.rbroderi.zblocks;
import io.github.rbroderi.zblocks.internal.NexoStartupGate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class NexoStartupGateTest {
    @Test void queuedItemsWaitForPublishedItemsAndMechanics(){
        var pending=new ArrayList<>(List.of("activate (item)","wire (furniture)"));var started=new AtomicInteger();var failed=new AtomicInteger();
        var gate=new NexoStartupGate(()->pending,started::incrementAndGet,missing->failed.incrementAndGet());
        gate.check(false,false);assertEquals(NexoStartupGate.State.WAITING,gate.state());assertEquals(0,failed.get());
        pending.removeFirst();gate.check(false,false);assertEquals(0,started.get());
        pending.clear();gate.check(true,false);assertEquals(NexoStartupGate.State.READY,gate.state());assertEquals(1,started.get());assertEquals(0,failed.get());
    }
    @Test void anAlreadyLoadedNexoStartsOnceDespiteRepeatedSignals(){
        var started=new AtomicInteger();var gate=new NexoStartupGate(List::of,started::incrementAndGet,missing->fail());
        gate.check(false,false);gate.check(true,false);gate.check(false,true);assertEquals(1,started.get());
    }
    @Test void aRealParsingFailureIsReportedAfterLoadingCompletes(){
        var errors=new ArrayList<List<String>>();var gate=new NexoStartupGate(()->List.of("wire (furniture)"),()->fail(),errors::add);
        gate.check(true,false);gate.check(true,false);assertEquals(NexoStartupGate.State.FAILED,gate.state());assertEquals(List.of(List.of("wire (furniture)")),errors);
    }
    @Test void aLoaderThatNeverCompletesTimesOutWithoutStartingGameplay(){
        var errors=new AtomicInteger();var gate=new NexoStartupGate(()->List.of("activate (item)"),()->fail(),missing->errors.incrementAndGet());
        gate.check(false,false);assertEquals(0,errors.get());gate.check(false,true);assertEquals(NexoStartupGate.State.FAILED,gate.state());assertEquals(1,errors.get());
    }
}
