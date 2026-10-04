package io.github.rbroderi.zblocks;
import io.github.rbroderi.zblocks.internal.*;
import java.lang.reflect.Proxy;
import java.util.UUID;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CreativeProtectionTest {
    @SuppressWarnings("unchecked") private static <T>T proxy(Class<T> type,java.util.function.Function<String,Object> values){return (T)Proxy.newProxyInstance(type.getClassLoader(),new Class<?>[]{type},(p,m,a)->values.apply(m.getName()));}
    @Test void creativeEditingBypassOnlyAllowsAuthorizedPlayers(){
        UUID worldId=UUID.randomUUID();World world=proxy(World.class,name->worldId);
        Block block=proxy(Block.class,name->switch(name){case "getWorld"->world;case "getX","getY","getZ"->0;default->null;});
        Player editor=proxy(Player.class,name->null),other=proxy(Player.class,name->null);
        var protection=new BlockProtection(position->true,player->player==editor);
        var allowed=new BlockBreakEvent(block,editor);protection.breakBlock(allowed);assertFalse(allowed.isCancelled());
        var denied=new BlockBreakEvent(block,other);protection.breakBlock(denied);assertTrue(denied.isCancelled());
        var defaultProtection=new BlockProtection(position->true);var normal=new BlockBreakEvent(block,editor);defaultProtection.breakBlock(normal);assertTrue(normal.isCancelled());
    }
}
