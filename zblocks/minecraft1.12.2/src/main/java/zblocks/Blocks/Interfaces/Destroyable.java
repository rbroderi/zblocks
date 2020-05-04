package zblocks.Blocks.Interfaces;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface Destroyable {

	public void Destroy(World world, BlockPos pos);
}
