package zblocks.Blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BrokenStone extends Block {
	public static IProperty<Boolean> broken = PropertyBool.create("broken");
	public static final int iBROKEN = 1, iSOLID = 0;

	public BrokenStone(String name) {
		super(Material.ROCK);
		setUnlocalizedName(name);
		setRegistryName(name);
		this.setDefaultState(this.blockState.getBaseState().withProperty(broken, false));
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, broken);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		if (state == this.blockState.getBaseState().withProperty(broken, true)) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public IBlockState getStateFromMeta(int i) {
		if (i == iBROKEN) { // 1
			return this.blockState.getBaseState().withProperty(broken, true);
		} else {
			return this.blockState.getBaseState().withProperty(broken, false);
		}
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	// For correct lighting around the block
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	// For rendering of block underneath
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

}