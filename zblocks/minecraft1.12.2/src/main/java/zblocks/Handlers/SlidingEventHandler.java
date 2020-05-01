package zblocks.Handlers;

import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import zblocks.SlidingEventData;
import zblocks.Blocks.PushPuzzleBlock;
import zblocks.Utility.StaticUtils;

public class SlidingEventHandler {
	private static int tickCount = 0;
	private static final int DELAY = 2;
	public static final HashSet<Block> SLIDEBLOCKS = new HashSet<Block>();
	static {
		SLIDEBLOCKS.add(Blocks.ICE);
		SLIDEBLOCKS.add(Blocks.FROSTED_ICE);
		SLIDEBLOCKS.add(Blocks.PACKED_ICE);
		Block blue_ice = Block.getBlockFromName("futuremc:blue_ice");
		if (blue_ice != null) {
			SLIDEBLOCKS.add(blue_ice);
		}
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START && ++tickCount == DELAY) {
			tickCount = 0;
			for (SlidingEventData slideEvent : PushPuzzleBlock.currentlySlidingBlocks) {
				World world = slideEvent.world;
				BlockPos from = slideEvent.from;
				EnumFacing facing = slideEvent.facing;
				BlockPos to = from.offset(facing);
				// slide block forward 1
				if (isSlidingAndFrontIsClear(world, from, to) || (slideEvent.momentum-- > 0 && world.isAirBlock(to))) {
					world.setBlockState(from, Blocks.AIR.getDefaultState());
					world.setBlockState(to, slideEvent.block);// pushes the block
					PushPuzzleBlock.getTileEntity(world, to).setStartPos(slideEvent.resetPos);
					from = to;
					to = from.offset(facing);
				}
				if (isSlidingAndFrontIsClear(world, from, to) || (slideEvent.momentum > 0 && world.isAirBlock(to))) {
					// block is still sliding
					PushPuzzleBlock.currentlySlidingBlocks.enqueue(new SlidingEventData(world, from, facing, slideEvent.block, slideEvent.resetPos, slideEvent.momentum));
				} // stopped sliding test if interrupted or ice came to natural end and no momentum left
				else {
					if (isSliding(world, from, to)) {
						StaticUtils.playSound(world, from, "clink", SoundCategory.BLOCKS, 2.5f);
					}
				}
			}
		}
	}

	public static boolean isSlidingAndFrontIsClear(World world, BlockPos from, BlockPos to) {
		return isSliding(world, from, to) && world.isAirBlock(to);
	}

	public static boolean isSliding(World world, BlockPos from, BlockPos to) {
		return SLIDEBLOCKS.contains(world.getBlockState(from.down()).getBlock()) &&
				SLIDEBLOCKS.contains(world.getBlockState(to.down()).getBlock());
	}
}
