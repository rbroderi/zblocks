package io.github.rbroderi.zblocks;
import io.github.rbroderi.zblocks.internal.SoundKeys;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SoundKeysTest {
    @Test void everyBundledSoundHasExactlyOneNamespace(){
        for(String name:new String[]{"thud_delay","scrape","warp","clink","activate","glass_ting","fuse","explosion"}){
            String key="zblocks:"+name;
            assertEquals(key,SoundKeys.custom(name));assertEquals(key,SoundKeys.custom(key));
            assertTrue(SoundKeys.custom(key).matches("[a-z0-9._-]+:[a-z0-9/._-]+"));
        }
        assertEquals("minecraft:entity.generic.explode",SoundKeys.custom("minecraft:entity.generic.explode"));
    }
}
