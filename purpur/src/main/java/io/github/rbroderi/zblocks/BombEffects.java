package io.github.rbroderi.zblocks;
import org.bukkit.*;
final class BombEffects {
    private BombEffects() {}
    static void explode(World world,Location center){
        world.spawnParticle(Particle.EXPLOSION_EMITTER,center,1,0,0,0,0);
        world.spawnParticle(Particle.FLASH,center,1,0,0,0,0,Color.fromRGB(255,235,190));
        world.spawnParticle(Particle.LARGE_SMOKE,center,36,.65,.4,.65,.07);
        world.spawnParticle(Particle.FLAME,center,24,.4,.25,.4,.08);
        world.spawnParticle(Particle.BLOCK,center,40,.7,.4,.7,.12,Material.STONE.createBlockData());
    }
}
