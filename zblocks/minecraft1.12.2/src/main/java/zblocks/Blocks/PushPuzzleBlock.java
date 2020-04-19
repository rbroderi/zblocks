package zblocks.Blocks;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
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
		ResourceLocation location = new ResourceLocation("zblock", "thud_delay");
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
				//don't play scrape if block will fall
				if(!world.isAirBlock( (pos.offset(player.getHorizontalFacing()).offset(EnumFacing.DOWN) )))
				{
				ResourceLocation location = new ResourceLocation("zblock", "scrape");
			//SoundEvent event = new SoundEvent(location);
			world.playSound(null,pos.getX(),pos.getY(),pos.getZ(), SoundEvent.REGISTRY.getObject(location), SoundCategory.BLOCKS, 1f, 1f);
				}
			}
	    }
	    }
	  
		public boolean moveBlockTo(World world, EntityPlayer player,BlockPos pos, BlockPos posMoveToHere)
		{
			boolean ret=false;
			IBlockState hit = world.getBlockState(pos);
			//player has hit block, is next to this block, the block does not have anything on top of it, and has a space to slide into
			if(hit.getBlock()==this && Utils.isNextTo(player,pos) && world.isAirBlock(pos.offset(EnumFacing.UP)) && world.isAirBlock(posMoveToHere) && world.isBlockModifiable(player, pos)) 
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
}