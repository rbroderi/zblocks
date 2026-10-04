package io.github.rbroderi.zblocks;
import io.github.rbroderi.zblocks.internal.ModelKey;
import java.util.Locale;
import static io.github.rbroderi.zblocks.PuzzleBlock.Kind.*;

final class PuzzleModels {
    static PuzzleBlock.Kind kindFromNexoId(String id){
        if(id==null||!id.startsWith("zblocks_"))return null;String name=id.substring(8);
        return java.util.Arrays.stream(PuzzleBlock.Kind.values()).sorted(java.util.Comparator.comparingInt((PuzzleBlock.Kind k)->k.name().length()).reversed()).filter(k->{String v=k.name().toLowerCase(Locale.ROOT);return name.equals(v)||name.startsWith(v+"_");}).findFirst().orElse(null);
    }
    static PuzzleBlock.Color colorFromNexoId(String id){
        var kind=kindFromNexoId(id);if(kind!=PUSH&&kind!=DEPRESS&&kind!=START)return PuzzleBlock.Color.BASE;
        String rest=id.substring(8+kind.name().length()+1);String name=rest.split("_")[0];return PuzzleBlock.parseColor(name);
    }
    static ModelKey key(PuzzleBlock p,boolean frozen,boolean matched){
        String color=p.color.name().toLowerCase(Locale.ROOT);
        String name=switch(p.kind){
            case PUSH -> "push_"+color+"_"+(matched?"on":"off")+"_"+(frozen?"ice":"normal");
            case DEPRESS -> "depress_"+color;
            case START -> "start_"+color;
            case ACTIVATE -> "activate_"+(p.active?(p.powered?"powered":"on"):"off");
            case TRANSIENT,SECRET_TRANSIENT -> p.kind.name().toLowerCase(Locale.ROOT)+"_"+(p.active?"on":"off");
            case BOMB -> "bomb_"+Math.clamp(p.fuse<0?8:p.fuse,0,8);
            case SWITCH_OFF -> "switch_off_"+(p.active?"on":"off");
            case SWITCH_ON -> "switch_on_"+(p.active?"on":"off");
            default -> p.kind.name().toLowerCase(Locale.ROOT);
        };
        return new ModelKey("zblocks",name);
    }
    static ModelKey inventoryKey(PuzzleBlock p){
        if(p.transientBlock())return new ModelKey("zblocks",p.kind.name().toLowerCase(Locale.ROOT)+"_on");
        return key(p,false,false);
    }
    static boolean worldModel(PuzzleBlock p){return !p.switchBlock();}
}
