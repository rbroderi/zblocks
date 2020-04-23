package zblocks.Blocks;

import net.minecraft.util.IStringSerializable;

public interface Colored {
	public enum ColorEnum implements IStringSerializable {
		BASE(0), RED(2), GREEN(4), BLUE(6), CYAN(8), MAGENTA(10), YELLOW(12);

		private final int value;

		private ColorEnum(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
		
		public boolean compare(ColorEnum other) {
			return this==BASE || other == BASE || this == other;
		}

		@Override
		// Property names (in your case probably returned by IStringSerializable::getName) must be all lowercase, in fact they must match
		// the regular expression [a-z0-9_]+.
		public String getName() {
			return this.name().toLowerCase();
		}
	}
	public ColorEnum getColor();
}
