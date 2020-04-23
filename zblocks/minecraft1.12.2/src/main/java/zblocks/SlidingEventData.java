package zblocks;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SlidingEventData {

	public World world;
	public BlockPos from;
	public EnumFacing facing;
	
	public SlidingEventData(World world,BlockPos from, EnumFacing facing) {
		this.world = world;
		this.from=from;
		this.facing=facing;
	}
}
