package zblocks.Blocks.Interfaces;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;

public interface Activatable {
	public IProperty<Boolean> getActivatedIProperty();
}
