package zblocks.Blocks.Interfaces;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface Resettable {

	public void reset(World world, BlockPos pos);
}
