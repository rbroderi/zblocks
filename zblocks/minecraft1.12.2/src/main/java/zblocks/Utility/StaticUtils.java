package zblocks.Utility;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class StaticUtils {
	private final static float CLOSE = 0.93f;

	public static boolean isOdd(int i) {
		return (i & 1) != 0;
	}

	public static boolean isEven(int i) {
		return !isOdd(i);
	}

	public static double getDistanceToEntity(EntityPlayer entity, BlockPos pos) {
		double deltaX = entity.posX - pos.getX();
		double deltaY = entity.posY - pos.getY();
		double deltaZ = entity.posZ - pos.getZ();

		return Math.sqrt((deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ));
	}

	public static boolean isNextTo(EntityPlayer player, BlockPos pos, float distance) {
		return player.getDistanceSqToCenter(pos) <= distance;
	}

	public static boolean isNextToAndNoYMotion(EntityPlayer player, BlockPos pos) {
		return Math.abs(player.motionY) < .1 && player.getDistanceSqToCenter(pos) <= CLOSE && !player.world.isAirBlock(player.getPosition().down());
	}

	public static boolean isNextToAndNoYMotion(EntityPlayer player, BlockPos pos, float distance) {
		return Math.abs(player.motionY) < .1 && player.getDistanceSqToCenter(pos) <= distance;
	}

	/*
	 * private static void spawnParticleClient(EntityPlayer player, EnumParticleTypes type, double x, double y, double z) { // http://www.minecraftforge.net/forum/index.php?topic=9744.0 for (int countparticles = 0; countparticles <= 10; ++countparticles) { player.world.spawnParticle(type, x + (player.world.rand.nextDouble() - 0.5D) * 0.8, y + player.world.rand.nextDouble() * 1.5 - 0.1, z + (player.world.rand.nextDouble() - 0.5D) * 0.8, 0.0D, 0.0D, 0.0D); } }
	 * 
	 * public static void spawnParticleClient(EntityPlayer player, EnumParticleTypes type, BlockPos pos) { spawnParticleClient(player, type, pos.getX(), pos.getY(), pos.getZ()); }
	 */

	public static void spawnParticleServer(World world, EnumParticleTypes type, BlockPos pos, double speed) {
		if (world instanceof WorldServer) {
			if (type == EnumParticleTypes.BLOCK_DUST) {
				world.getBlockState(pos).getBlock();
				((WorldServer) world).spawnParticle(type,
						pos.getX() + (world.rand.nextDouble() - 0.5D) * 0.8,
						pos.getY() + (world.rand.nextDouble() - 0.5D) * 0.8,
						pos.getZ() + (world.rand.nextDouble() - 0.5D) * 0.8, 10, 0, -1, 0, speed, Block.getStateId(world.getBlockState(pos)));
			} else {
				((WorldServer) world).spawnParticle(type,
						pos.getX() + (world.rand.nextDouble() - 0.5D) * 0.8,
						pos.getY() + (world.rand.nextDouble() - 0.5D) * 0.8,
						pos.getZ() + (world.rand.nextDouble() - 0.5D) * 0.8, 10, 0, -1, 0, speed);
			}
		}
	}

	public static void spawnParticleServer(World world, EnumParticleTypes type, BlockPos pos, double speed, double xOffset, double yOffset, double zOffset) {
		if (world instanceof WorldServer) {
			if (type == EnumParticleTypes.BLOCK_DUST) {
				world.getBlockState(pos).getBlock();
				((WorldServer) world).spawnParticle(type,
						pos.getX() + (world.rand.nextDouble() - 0.5D) * 0.8,
						pos.getY() + (world.rand.nextDouble() - 0.5D) * 0.8,
						pos.getZ() + (world.rand.nextDouble() - 0.5D) * 0.8, 10, xOffset, yOffset, zOffset, speed, Block.getStateId(world.getBlockState(pos)));
			} else {
				((WorldServer) world).spawnParticle(type,
						pos.getX() + (world.rand.nextDouble() - 0.5D) * 0.8,
						pos.getY() + (world.rand.nextDouble() - 0.5D) * 0.8,
						pos.getZ() + (world.rand.nextDouble() - 0.5D) * 0.8, 10, xOffset, yOffset, zOffset, speed);
			}
		}
	}

	public static void playSound(World world, BlockPos pos, String sound, SoundCategory soundCat, float volume) {
		ResourceLocation location = new ResourceLocation("zblock", sound);
		world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvent.REGISTRY.getObject(location), soundCat,
				volume, 1f);
	}
}
