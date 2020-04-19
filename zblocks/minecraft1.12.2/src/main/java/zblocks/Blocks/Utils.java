package zblocks.Blocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Utils {
	   public static double getDistanceToEntity(EntityPlayer entity, BlockPos pos) {
			double deltaX = entity.posX - pos.getX();
			double deltaY = entity.posY - pos.getY();
			double deltaZ = entity.posZ - pos.getZ();
				
			return Math.sqrt((deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ));
	   }
	   
	   public static boolean isNextTo(EntityPlayer entity, BlockPos pos) {
			double deltaX = Math.floor(entity.posX - pos.getX());
			int deltaY = (int) Math.floor(entity.posY - pos.getY());
			double deltaZ = Math.floor(entity.posZ - pos.getZ());
			return deltaY==0 && ((deltaZ==0 && Math.abs(deltaX) <1.7 ) || (deltaX==0 && Math.abs(deltaZ) <1.7));
		}
	   
		private static void spawnParticle(World world, EnumParticleTypes type, double x, double y, double z)
		{ 
			//http://www.minecraftforge.net/forum/index.php?topic=9744.0
			for(int countparticles = 0; countparticles <= 10; ++countparticles)
			{
				world.spawnParticle(type, x + (world.rand.nextDouble() - 0.5D) * (double)0.8, y + world.rand.nextDouble() * (double)1.5 - (double)0.1, z + (world.rand.nextDouble() - 0.5D) * (double)0.8, 0.0D, 0.0D, 0.0D);
			} 
	    }
		public static void spawnParticle(World world, EnumParticleTypes type, BlockPos pos)
		{
			spawnParticle(world,type,pos.getX(),pos.getY(),pos.getZ());
		}
}
