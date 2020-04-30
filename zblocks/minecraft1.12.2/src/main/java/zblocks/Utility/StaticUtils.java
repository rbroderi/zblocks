package zblocks.Utility;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
		return Math.abs(player.motionY) < .1 && player.getDistanceSqToCenter(pos) <= CLOSE;
	}

	public static boolean isNextToAndNoYMotion(EntityPlayer player, BlockPos pos, float distance) {
		return Math.abs(player.motionY) < .1 && player.getDistanceSqToCenter(pos) <= distance;
	}

	private static void spawnParticle(EntityPlayer player, EnumParticleTypes type, double x, double y, double z) {
		// http://www.minecraftforge.net/forum/index.php?topic=9744.0
		for (int countparticles = 0; countparticles <= 10; ++countparticles) {
			player.world.spawnParticle(type, x + (player.world.rand.nextDouble() - 0.5D) * 0.8, y + player.world.rand.nextDouble() * 1.5 - 0.1,
					z + (player.world.rand.nextDouble() - 0.5D) * 0.8, 0.0D, 0.0D, 0.0D);
		}
	}

	public static void spawnParticle(EntityPlayer player, EnumParticleTypes type, BlockPos pos) {
		spawnParticle(player, type, pos.getX(), pos.getY(), pos.getZ());
	}

	public static void playSound(World world, BlockPos pos, String sound, SoundCategory soundCat, float volume) {
		ResourceLocation location = new ResourceLocation("zblock", sound);
		world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvent.REGISTRY.getObject(location), soundCat,
				volume, 1f);
	}
}
