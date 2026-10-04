package io.github.rbroderi.zblocks;
import java.util.Set;
/** Nexo emits a protection-only block placement before its furniture event. */
final class PuzzlePlacement {
    private PuzzlePlacement(){}
    static boolean nativeEvent(String nexoId,boolean furniture){return !(furniture&&nexoId!=null&&nexoId.startsWith("zblocks_"));}
    static boolean invalidBacking(PuzzleBlock p){return !p.destroyed&&Set.of("minecraft:air","minecraft:cave_air","minecraft:void_air").contains(p.visualData);}
    static boolean staleFurniture(String nexoId,boolean managed,PuzzleBlock registered){
        if(managed||registered==null||PuzzleModels.kindFromNexoId(nexoId)!=registered.kind)return false;
        try{return PuzzleModels.colorFromNexoId(nexoId)==registered.color;}catch(IllegalArgumentException ex){return false;}
    }
}
