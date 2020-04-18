package zblocks.Blocks;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class PushPuzzleBlock extends BlockFalling {

	public PushPuzzleBlock(String name, Material material) {
		super(material);
		setUnlocalizedName(name);
		setRegistryName(name);
		this.useNeighborBrightness=true; //workaround for lighting issue -  culling face not working with insets
	}
	@Override
	public boolean canProvidePower(IBlockState state){
		return true;
	}
	@Override
    public int getStrongPower(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
       return 15;
	}
	
	@Override
	public int getWeakPower(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return 15;
 	}
	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		for (EnumFacing enumfacing : EnumFacing.VALUES) {
		world.notifyNeighborsOfStateChange(pos.offset(enumfacing),this,true);
		}
	}
	
	@Override
	public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {
		super.onBlockDestroyedByPlayer(world, pos, state);
		for (EnumFacing enumfacing : EnumFacing.VALUES) {
		world.notifyNeighborsOfStateChange(pos.offset(enumfacing),this,true);
		}
	}
	
	@Override
	public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion ex) {
		super.onBlockDestroyedByExplosion(world, pos, ex);
		for (EnumFacing enumfacing : EnumFacing.VALUES) {
		world.notifyNeighborsOfStateChange(pos.offset(enumfacing),this,true);
		}
	}
	
	@Override
	public void onEndFalling(World world,BlockPos pos,IBlockState s1,IBlockState s2) {
		super.onEndFalling(world,pos,s1,s2);
		
		if(!world.isRemote) // world.isRemote means it's the client and there is no WorldServer
		{
		ResourceLocation location = new ResourceLocation("zblock", "thud");
		//SoundEvent event = new SoundEvent(location);
		world.playSound(null,pos.getX(),pos.getY(),pos.getZ(), SoundEvent.REGISTRY.getObject(location), SoundCategory.BLOCKS, 1f, 1f);
		}
	}
  //When left-clicked
	   @Override
	    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player)
	    {
		   super.onBlockClicked(world, pos, player);
		   if(moveBlockTo(player.world, player, pos, pos.offset(player.getHorizontalFacing()))) { //.worldObj
			if(!world.isRemote) // world.isRemote means it's the client and there is no WorldServer
			{
			ResourceLocation location = new ResourceLocation("zblock", "scrape");
			//SoundEvent event = new SoundEvent(location);
			world.playSound(null,pos.getX(),pos.getY(),pos.getZ(), SoundEvent.REGISTRY.getObject(location), SoundCategory.BLOCKS, 1f, 1f);
			}
	    }
	    }
	  
	   
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
	   
		public boolean moveBlockTo(World world, EntityPlayer player,BlockPos pos, BlockPos posMoveToHere)
		{
			boolean ret=false;
			IBlockState hit = world.getBlockState(pos);

			if(hit.getBlock()==this && isNextTo(player,pos) && world.isAirBlock(new BlockPos(pos.getX(),pos.getY()+1,pos.getZ())) && world.isAirBlock(posMoveToHere) && world.isBlockModifiable(player, pos)) 
			{
				if(!world.isRemote) 
				{ 
					world.destroyBlock(pos, false);
					world.setBlockState(posMoveToHere, hit);//pushes the block
				} 
				ret=true;
			} 
			return ret;
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