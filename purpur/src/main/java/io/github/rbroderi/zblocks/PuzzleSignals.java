package io.github.rbroderi.zblocks;
/** Native redstone blocks own their output; only controlled levers need event overrides. */
final class PuzzleSignals {
    private PuzzleSignals() {}
    static int output(PuzzleBlock p){return p!=null&&!p.destroyed&&p.kind==PuzzleBlock.Kind.DEPRESS&&p.active?15:0;}
    static int dustCurrent(int vanillaCurrent,boolean directlyPowered){return directlyPowered?15:vanillaCurrent;}
    static boolean overrideIncoming(PuzzleBlock p){return p!=null&&!p.destroyed&&p.switchBlock();}
}
