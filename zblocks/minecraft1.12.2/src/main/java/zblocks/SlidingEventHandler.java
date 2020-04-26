package zblocks;

import java.util.HashSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import zblocks.Blocks.PushPuzzleBlock;
import zblocks.Utility.StaticUtils;

public class SlidingEventHandler {
	private static int tickCount = 0;
	private static final int DELAY = 3;

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
					if (isSlidingAndFrontIsClear(world,from,to)) {
						spawnParticleAroundPosition(world,from);
						world.setBlockState(from, Blocks.AIR.getDefaultState());
						world.setBlockState(to, slideEvent.block);// pushes the block
						PushPuzzleBlock.getTileEntity(world, to).setStartPos(slideEvent.resetPos);
						from = to;
						to = from.offset(facing);
					}
					if (isSlidingAndFrontIsClear(world,from,to)) {
						// block is still sliding
						PushPuzzleBlock.currentlySlidingBlocks.enqueue(new SlidingEventData(world, from, facing, slideEvent.block, slideEvent.resetPos));
					}
					// stopped sliding test if interrupted or ice came to natural end
					else {
						if (isSliding(world,from,to)) {
							StaticUtils.playSound(world, from, "clink", SoundCategory.BLOCKS, 2.5f);
						}
					}
			}
		}
	}
	
	private static void spawnParticleAroundPosition(World world, BlockPos pos) {
		List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class,
				new AxisAlignedBB(pos.getX() - 10, pos.getY() - 10, pos.getZ() - 10, pos.getX() + 10, pos.getY() + 10, pos.getZ() + 10));
		for (EntityLivingBase ent : entities) {
			if (ent instanceof EntityPlayer) {
				StaticUtils.spawnParticle((EntityPlayer) ent, EnumParticleTypes.CLOUD, pos);
			}
		}
	}
	
	public static boolean isSlidingAndFrontIsClear(World world, BlockPos from, BlockPos to) {
		return isSliding(world,from,to) && world.isAirBlock(to);
	}
	
	public static boolean isSliding(World world, BlockPos from, BlockPos to) {
		HashSet<Block> blocks = new HashSet<Block>();
		blocks.add(Blocks.ICE);
		blocks.add(Blocks.FROSTED_ICE);
		blocks.add(Blocks.PACKED_ICE);
		Block blue_ice = Block.getBlockFromName("futuremc:blue_ice");
		if(blue_ice !=null) {
			blocks.add(blue_ice);
		}
		return blocks.contains(world.getBlockState(from.down()).getBlock()) &&
				blocks.contains(world.getBlockState(to.down()).getBlock());
	}
}
