package io.github.rbroderi.zblocks;
import io.github.rbroderi.zblocks.internal.NexoStartupGate;
import com.nexomc.nexo.api.NexoItems;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.InputStreamReader;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
/** Executes the supplied Nexo API in its pre-load state, without a Minecraft server. */
public final class NexoQueuedRegistrationSmoke {
    public static void main(String[] args){
        Plugin owner=(Plugin)Proxy.newProxyInstance(Plugin.class.getClassLoader(),new Class<?>[]{Plugin.class},(proxy,method,values)->{
            if(method.getName().equals("getName"))return "ZBlocks";
            throw new UnsupportedOperationException(method.getName());
        });
        var definitions=YamlConfiguration.loadConfiguration(new InputStreamReader(NexoQueuedRegistrationSmoke.class.getResourceAsStream("/nexo-items.yml")));
        int count=0;
        for(String id:definitions.getKeys(false)){
            var item=NexoItems.registerExternalItem(owner,definitions.getConfigurationSection(id));
            if(item!=null||NexoItems.exists(id))throw new AssertionError("Expected the pre-load API to queue "+id);
            count++;
        }
        var started=new AtomicInteger();var gate=new NexoStartupGate(()->{
            var missing=new ArrayList<String>();for(String id:definitions.getKeys(false))if(!NexoItems.exists(id))missing.add(id);return missing;
        },started::incrementAndGet,missing->{throw new AssertionError("Queued registration was incorrectly rejected");});
        gate.check(false,false);
        if(gate.state()!=NexoStartupGate.State.WAITING||started.get()!=0)throw new AssertionError("Started gameplay before Nexo loaded");
        System.out.println("Exact Nexo API: "+count+" external definitions returned null while queued; consumer waits without failing.");
    }
}
