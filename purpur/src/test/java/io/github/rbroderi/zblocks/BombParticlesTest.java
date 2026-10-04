package io.github.rbroderi.zblocks;
import org.bukkit.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BombParticlesTest {
    @Test void particlePayloadsMatchTheTargetApi(){
        assertEquals(Color.class,Particle.FLASH.getDataType());
        assertTrue(Particle.FLASH.getDataType().isInstance(Color.fromRGB(255,235,190)));
        assertEquals(org.bukkit.block.data.BlockData.class,Particle.BLOCK.getDataType());
        for(var p:new Particle[]{Particle.EXPLOSION_EMITTER,Particle.LARGE_SMOKE,Particle.FLAME,Particle.SMALL_FLAME,Particle.SMOKE,Particle.FIREWORK})assertEquals(Void.class,p.getDataType(),p.name());
    }
}
