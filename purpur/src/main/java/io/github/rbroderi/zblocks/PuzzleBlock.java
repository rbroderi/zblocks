package io.github.rbroderi.zblocks;

import java.util.Locale;
import java.util.UUID;
import io.github.rbroderi.zblocks.internal.Position;

final class PuzzleBlock {
    enum Kind { PUSH, DEPRESS, ACTIVATE, TRANSIENT, SECRET_TRANSIENT, HOURGLASS, START,
        BROKEN, SUBTLE_BROKEN, SECRET_BROKEN, BROKEN_EDGE, SWITCH_OFF, SWITCH_ON, BOMB, JUKEBOX, CACTUS }
    enum Color {
        BASE, BLUE, CYAN, GREEN, MAGENTA, RED, YELLOW;
        boolean matches(Color other) { return this == BASE || other == BASE || this == other; }
        String prefix() { return this == BASE ? "WHITE" : name(); }
    }
    final UUID id;
    final Kind kind;
    final Color color;
    final Position origin;
    // BlockData restored when an administrator removes the registration.
    final String originalData;
    Position position;
    String visualData;
    boolean active;
    boolean manualActive;
    boolean powered;
    boolean destroyed;
    int yaw;
    int edgeTilt=90;
    int fuse = -1;
    int dx, dz, momentum;

    PuzzleBlock(UUID id, Kind kind, Color color, Position origin, Position position, String originalData, String visualData) {
        this.id = id; this.kind = kind; this.color = color; this.origin = origin;
        this.position = position; this.originalData = originalData; this.visualData = visualData;
    }
    boolean transientBlock() { return kind == Kind.TRANSIENT || kind == Kind.SECRET_TRANSIENT; }
    boolean bombBreakable(){return breakable()&&kind!=Kind.BROKEN_EDGE;}
    boolean breakable() { return kind == Kind.BROKEN || kind == Kind.SUBTLE_BROKEN || kind == Kind.SECRET_BROKEN || kind == Kind.BROKEN_EDGE; }
    boolean switchBlock() { return kind == Kind.SWITCH_OFF || kind == Kind.SWITCH_ON; }
    static boolean retired(Kind kind){return kind==Kind.JUKEBOX||kind==Kind.CACTUS;}
    static Kind parseKind(String value) { Kind kind=Kind.valueOf(value.toUpperCase(Locale.ROOT));if(retired(kind))throw new IllegalArgumentException("Removed puzzle type");return kind; }
    static Color parseColor(String value) { return Color.valueOf(value.toUpperCase(Locale.ROOT)); }
}
