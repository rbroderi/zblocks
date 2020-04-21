package zblocks.Blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class DepressPuzzleBlock extends Block {
	private boolean isActivated = false;
	public static IProperty<Boolean> activated =  PropertyBool.create("activated");
	public static final int iACTIVATED=1,iDISABLED=0;
	public DepressPuzzleBlock(String name, Material material) {
		super(material);
		setUnlocalizedName(name);
		setRegistryName(name);
		this.useNeighborBrightness = true; // workaround for lighting issue - culling face not working with insets
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
	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getWeakPower(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		if (isActivated) {
			return 15;
		}
		return super.getWeakPower(state, blockAccess, pos, side);
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos neighbor) {
		super.neighborChanged(state, world, pos, block, neighbor);
		// world.getBlockState(neighbor).getBlock()
		// if(pos.up().getX() == neighbor.getX() && pos.up().getY() == neighbor.getY()&
		// & pos.up().getZ() == neighbor.getZ() && block instanceof PushPuzzleBlock) {
		if (world.getBlockState(pos.up()).getBlock() instanceof PushPuzzleBlock) {
			isActivated = true;
			world.setBlockState(pos, this.getDefaultState().withProperty(activated, true),3);
		} else {
			if (isActivated == true) {
				isActivated = false;
				world.setBlockState(pos, this.getDefaultState().withProperty(activated, false),3);
				for (EnumFacing enumfacing : EnumFacing.VALUES) {
					world.notifyNeighborsOfStateChange(pos.offset(enumfacing), this, true);
				}
			}
		}
	}
}