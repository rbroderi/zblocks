package io.github.rbroderi.zblocks;
final class PuzzleLighting {
    private PuzzleLighting() {}
    static boolean fullBright(PuzzleBlock p,boolean matched){
        return !p.destroyed&&(p.kind==PuzzleBlock.Kind.DEPRESS&&p.active||p.kind==PuzzleBlock.Kind.PUSH&&matched);
    }
}
