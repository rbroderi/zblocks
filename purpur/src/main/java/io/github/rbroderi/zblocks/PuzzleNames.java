package io.github.rbroderi.zblocks;
import java.util.Locale;
final class PuzzleNames {
    private PuzzleNames() {}
    static String item(PuzzleBlock.Kind kind,PuzzleBlock.Color color){
        String name=kind==PuzzleBlock.Kind.PUSH?"push block":kind.name().toLowerCase(Locale.ROOT).replace('_',' ');
        return "ZBlocks "+name+(color==PuzzleBlock.Color.BASE?"":" "+color.name().toLowerCase(Locale.ROOT));
    }
}
