package io.github.rbroderi.zblocks.internal;
import java.util.List;
import java.util.function.*;
/** External items can be queued until Nexo publishes its registry. */
public final class NexoStartupGate {
    public enum State {WAITING,READY,FAILED}
    private State state=State.WAITING;
    private final Supplier<List<String>> missing;
    private final Runnable ready;
    private final Consumer<List<String>> failed;
    public NexoStartupGate(Supplier<List<String>> missing,Runnable ready,Consumer<List<String>> failed){this.missing=missing;this.ready=ready;this.failed=failed;}
    public State state(){return state;}
    public void check(boolean loadingFinished,boolean expired){
        if(state!=State.WAITING)return;
        var pending=List.copyOf(missing.get());
        if(pending.isEmpty()){state=State.READY;ready.run();}
        else if(loadingFinished||expired){state=State.FAILED;failed.accept(pending);}
    }
}
