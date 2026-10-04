package io.github.rbroderi.zblocks.internal;
/** Accept both a local sound name and an already qualified resource key. */
public final class SoundKeys {
    private SoundKeys() {}
    public static String custom(String sound){return sound.indexOf(':')<0?"zblocks:"+sound:sound;}
}
