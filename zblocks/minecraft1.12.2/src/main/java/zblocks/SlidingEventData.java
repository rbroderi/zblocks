package zblocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SlidingEventData {

	public World world;
	public BlockPos from;
	public EnumFacing facing;
	public IBlockState block;
	public BlockPos resetPos;
	
	public SlidingEventData(World world,BlockPos from, EnumFacing facing,IBlockState block,BlockPos resetPos) {
		this.world = world;
		this.from=from;
		this.facing=facing;
		this.block=block;
		this.resetPos = resetPos;
	}
}
