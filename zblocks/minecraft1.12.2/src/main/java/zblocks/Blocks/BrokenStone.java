package zblocks.Blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zblocks.Blocks.Interfaces.Destroyable;
import zblocks.Blocks.Interfaces.Resettable;
import zblocks.Utility.StaticUtils;

public class BrokenStone extends Block implements Destroyable, Resettable {
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
		/*
		 * if (state == this.blockState.getBaseState().withProperty(broken, true)) { return false; } else { return true; } for some reason this is true when inside of the broken stone block?? returning false always for now
		 */
		return false;
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		if (state == this.blockState.getBaseState().withProperty(broken, true)) {
			return false;
		} else {
			return true;
		}
	}

	// For rendering of block underneath
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		if (state == this.blockState.getBaseState().withProperty(broken, true)) {
			return false;
		} else {
			return true;
		}
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public void Destroy(World world, BlockPos pos) {
		if (world.getBlockState(pos) == this.getDefaultState().withProperty(broken, false)) {
			world.setBlockState(pos, this.getDefaultState().withProperty(broken, true));
			StaticUtils.spawnParticleServer(world, EnumParticleTypes.BLOCK_DUST, pos, 0.01);
			StaticUtils.spawnParticleServer(world, EnumParticleTypes.BLOCK_DUST, pos, 0.01);
			StaticUtils.spawnParticleServer(world, EnumParticleTypes.BLOCK_DUST, pos, 0.01);
			StaticUtils.spawnParticleServer(world, EnumParticleTypes.BLOCK_DUST, pos, 0.01);
		}

	}

	@SuppressWarnings("deprecation")
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn,
			boolean isActualState) {
		if (entityIn instanceof EntityLiving && worldIn.getBlockState(pos) == this.blockState.getBaseState().withProperty(broken, false)) {
			return;
		}
		super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		if (worldIn.getBlockState(pos) == this.blockState.getBaseState().withProperty(broken, true)) {
			return null;// new AxisAlignedBB(0,0,0,0,0,0);
		} else {
			return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		if (state == this.blockState.getBaseState().withProperty(broken, true)) {
			return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
		} else {
			return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
		}
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
		return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	}

	@Override
	public void reset(World world, BlockPos pos) {
		world.setBlockState(pos, this.getDefaultState());

	}

	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
	}

}