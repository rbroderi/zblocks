package zblocks.Blocks;

import net.minecraft.block.BlockTNT;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class Bomb extends BlockTNT {
	public static final PropertyInteger time = PropertyInteger.create("time", 0, 7);

	public Bomb() {
		this.setDefaultState(this.blockState.getBaseState().withProperty(EXPLODE, false).withProperty(time, 8));
	}

	@Override
	public void explode(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase igniter) {
		return;
	}

	@Override
	public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn) {
		explode(worldIn, pos, worldIn.getBlockState(pos), (EntityLivingBase) null);
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		// return this.getDefaultState().withProperty(EXPLODE, Boolean.valueOf((meta & 1) > 0)).withProperty(time, value);
		return null;
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(EXPLODE).booleanValue() ? 1 : 0;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { EXPLODE });
	}

}
