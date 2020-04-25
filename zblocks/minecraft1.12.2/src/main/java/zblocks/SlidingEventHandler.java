package zblocks;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import zblocks.Blocks.PushPuzzleBlock;

public class SlidingEventHandler {
	private static int tickCount = 0;

	/*
	 * @SubscribeEvent public static void onServerTick(TickEvent.ServerTickEvent event) { if (event.phase == TickEvent.Phase.START && ++tickCount == 3) { tickCount = 0; int initialSlidingEventDataArrayListLength = PushPuzzleBlock.currentlySlidingBlocks.size(); for (int i = initialSlidingEventDataArrayListLength - 1; i >= 0; i--) { try { SlidingEventData slideEvent = PushPuzzleBlock.currentlySlidingBlocks.get(i); PushPuzzleBlock.currentlySlidingBlocks.remove(i); World world = slideEvent.world; BlockPos from = slideEvent.from; EnumFacing facing = slideEvent.facing; BlockPos to = from.offset(facing); // slide block forward 1 if (world.getBlockState(from.down()).getBlock() == Blocks.ICE && world.getBlockState(to.down()).getBlock() == Blocks.ICE && world.isAirBlock(to)) { List<EntityLivingBase>
	 * entities = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(from.getX()-10, from.getY()-10, from.getZ()-10, from.getX()+10, from.getY()+10, from.getZ()+10)); for(EntityLivingBase ent : entities) { //System.out.println("found entities:"+ ent); if(ent instanceof EntityPlayer) { System.out.println("spawning particle?"); Utils.spawnParticle((EntityPlayer)ent, EnumParticleTypes.CLOUD, from); } } world.setBlockState(from, Blocks.AIR.getDefaultState()); world.setBlockState(to, slideEvent.block);// pushes the block PushPuzzleBlock.getTileEntity(world, to).setStartPos(slideEvent.resetPos); from = to; to = from.offset(facing); } if (world.getBlockState(from.down()).getBlock() == Blocks.ICE && world.getBlockState(to.down()).getBlock() == Blocks.ICE && world.isAirBlock(to)) {
	 * // block is still sliding PushPuzzleBlock.currentlySlidingBlocks.add(new SlidingEventData(world, from, facing, slideEvent.block, slideEvent.resetPos)); } // stopped sliding test if interrupted or ice came to natural end else { if (world.getBlockState(from.down()).getBlock() == Blocks.ICE && world.getBlockState(to.down()).getBlock() == Blocks.ICE) { ResourceLocation location = new ResourceLocation("zblock", "clink"); world.playSound(null, from.getX(), from.getY(), from.getZ(), SoundEvent.REGISTRY.getObject(location), SoundCategory.BLOCKS, 3f, 1f); } } } catch (Exception e) { e.printStackTrace(); } } } }
	 */

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START && ++tickCount == 3) {
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
							ResourceLocation location = new ResourceLocation("zblock", "clink");
							world.playSound(null, from.getX(), from.getY(), from.getZ(), SoundEvent.REGISTRY.getObject(location), SoundCategory.BLOCKS,
									3f, 1f);
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
				Utils.spawnParticle((EntityPlayer) ent, EnumParticleTypes.CLOUD, pos);
			}
		}
	}
	
	public static boolean isSlidingAndFrontIsClear(World world, BlockPos from, BlockPos to) {
		return world.getBlockState(from.down()).getBlock() == Blocks.ICE &&
				world.getBlockState(to.down()).getBlock() == Blocks.ICE &&
				world.isAirBlock(to);
	}
	
	public static boolean isSliding(World world, BlockPos from, BlockPos to) {
		return world.getBlockState(from.down()).getBlock() == Blocks.ICE &&
				world.getBlockState(to.down()).getBlock() == Blocks.ICE;
	}
}
