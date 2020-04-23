package zblocks.Blocks;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface Resettable {

	public void resetPosition(World world, BlockPos block);
}
