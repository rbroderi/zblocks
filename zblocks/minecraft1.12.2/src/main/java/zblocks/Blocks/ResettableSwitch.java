package zblocks.Blocks;

import net.minecraft.block.BlockLever;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import zblocks.Blocks.Interfaces.Resettable;

public class ResettableSwitch extends BlockLever implements Resettable {
	private final boolean DEFAULTPOWER;

	public ResettableSwitch(String name, boolean defaultpower) {
		super();
		setUnlocalizedName(name);
		setRegistryName(name);
		this.DEFAULTPOWER = defaultpower;
		this.setDefaultState(super.getDefaultState().withProperty(POWERED, defaultpower));
	}

	@Override
	public void reset(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		world.setBlockState(pos, state.withProperty(POWERED, DEFAULTPOWER));
	}

	/**
	 * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the IBlockstate
	 * override default behavior to use DEFAULTPOWER instead of false
	 */
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		IBlockState iblockstate = this.getDefaultState().withProperty(POWERED, Boolean.valueOf(DEFAULTPOWER));

		if (canAttachTo(worldIn, pos, facing)) {
			return iblockstate.withProperty(FACING, BlockLever.EnumOrientation.forFacings(facing, placer.getHorizontalFacing()));
		} else {
			for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
				if (enumfacing != facing && canAttachTo(worldIn, pos, enumfacing)) {
					return iblockstate.withProperty(FACING, BlockLever.EnumOrientation.forFacings(enumfacing, placer.getHorizontalFacing()));
				}
			}

			if (worldIn.getBlockState(pos.down()).isTopSolid()) {
				return iblockstate.withProperty(FACING, BlockLever.EnumOrientation.forFacings(EnumFacing.UP, placer.getHorizontalFacing()));
			} else {
				return iblockstate;
			}
		}
	}

}
