package zblocks.Blocks;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
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

	public static IProperty<Boolean> activated =  PropertyBool.create("activated");
	public static final int iACTIVATED=1,iDISABLED=0;
	private boolean isActivated=false;
	public PushPuzzleBlock(String name, Material material) {
		super(material);
		setUnlocalizedName(name);
		setRegistryName(name);
		this.useNeighborBrightness=true; //workaround for lighting issue -  culling face not working with insets
		this.setDefaultState(this.blockState.getBaseState().withProperty(activated, false));
	}
	@Override
	public BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, activated);
	}
	@Override
	public int getMetaFromState(IBlockState state) {
		if(state==this.blockState.getBaseState().withProperty(activated, true)) {
			return iACTIVATED;
		}
		else {
			return iDISABLED;
		}
	}
	@Override
	public IBlockState getStateFromMeta(int i) {
		if(i==iACTIVATED) {
			return this.blockState.getBaseState().withProperty(activated, true);
		}
		else {
			return this.blockState.getBaseState().withProperty(activated, false);
		}
	}
	@Override
	public boolean canProvidePower(IBlockState state){
		return true;
	}
	@SuppressWarnings("deprecation")
	@Override
    public int getStrongPower(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		if(isActivated) {
			return 15;
		}
       return super.getStrongPower(state,blockAccess,pos,side);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public int getWeakPower(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		if(isActivated) {
			return 15;
		}
       return super.getWeakPower(state, blockAccess, pos, side);
 	}
	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		setActivated(world,pos);
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
			if(hit.getBlock().equals(this) && Utils.isNextTo(player,pos) && world.isAirBlock(pos.offset(EnumFacing.UP)) && world.isAirBlock(posMoveToHere) && world.isBlockModifiable(player, pos)) 
			{
				if(!world.isRemote) 
				{ 
					world.destroyBlock(pos, false);
					world.setBlockState(posMoveToHere, hit);//pushes the block
					setActivated(world,posMoveToHere);
				} 
				ret=true;
			} 
			return ret;
		}
		private void setActivated(World world,BlockPos pos) {
			if(world.getBlockState(pos.offset(EnumFacing.DOWN)).getBlock() instanceof DepressPuzzleBlock) {
				world.setBlockState(pos, this.getDefaultState().withProperty(activated, true),3);
				this.isActivated=true;
				Utils.spawnParticle(world, EnumParticleTypes.CRIT_MAGIC, pos);
			}
			else {
				world.setBlockState(pos, this.getDefaultState().withProperty(activated, false),3);
				this.isActivated=false;
			}
			
		}
}