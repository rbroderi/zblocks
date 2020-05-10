package zblocks.Blocks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BrokenStoneEdge extends Block {

	public enum BrokenStoneFacingEnum implements IStringSerializable {
		UP_NORTH(1, EnumFacing.NORTH), UP_SOUTH(2, EnumFacing.SOUTH), UP_WEST(3, EnumFacing.WEST), UP_EAST(4, EnumFacing.EAST),
		NORTH(5, EnumFacing.NORTH), SOUTH(6, EnumFacing.SOUTH), WEST(7, EnumFacing.WEST), EAST(8, EnumFacing.EAST),
		DOWN_NORTH(9, EnumFacing.NORTH), DOWN_SOUTH(10, EnumFacing.SOUTH), DOWN_WEST(11, EnumFacing.WEST), DOWN_EAST(12, EnumFacing.EAST);

		private final int value;
		private final static Map<Integer, BrokenStoneFacingEnum> lookup;
		private EnumFacing facing;
		static {
			Map<Integer, BrokenStoneFacingEnum> lookupTemp = new HashMap<Integer, BrokenStoneFacingEnum>();
			for (BrokenStoneFacingEnum e : BrokenStoneFacingEnum.values()) {
				lookupTemp.put(e.getValue(), e);
			}

			lookup = Collections.unmodifiableMap(lookupTemp);
		}

		private BrokenStoneFacingEnum(int value, EnumFacing facing) {
			this.value = value;
			this.facing = facing;
		}

		public int getValue() {
			return value;
		}

		public static BrokenStoneFacingEnum getByValue(int i) {

			return lookup.get(i);
		}

		public static BrokenStoneFacingEnum getByFacing(String up_down, EnumFacing facing) {
			// ------ 0 1 2 3 4 5
			// facing D-U-N-S-W-E 0-5
			if (up_down.equals("up")) {
				return BrokenStoneFacingEnum.getByValue(facing.getIndex() - 1);
			} else if (up_down.equals("down")) {
				return BrokenStoneFacingEnum.getByValue(facing.getIndex() + 7);
			} else if (up_down.equals("none")) {
				return BrokenStoneFacingEnum.getByValue(facing.getIndex() + 3);
			} else {
				throw new RuntimeException("string must be up or down or none; got" + up_down);
			}
		}

		@Override
		// Property names (in your case probably returned by IStringSerializable::getName) must be all lowercase, in fact they must match
		// the regular expression [a-z0-9_]+.
		public String getName() {
			return this.name().toLowerCase();
		}
	}

	// public static IProperty<Boolean> broken = PropertyBool.create("broken");
	// public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static IProperty<BrokenStoneFacingEnum> FACING = PropertyEnum.create("facing", BrokenStoneFacingEnum.class);

	public BrokenStoneEdge(String name) {
		super(Material.ROCK);
		setUnlocalizedName(name);
		setRegistryName(name);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, BrokenStoneFacingEnum.NORTH));
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getValue();
	}

	@Override
	public IBlockState getStateFromMeta(int i) {
		return this.getDefaultState().withProperty(FACING, BrokenStoneFacingEnum.getByValue(i));
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
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	/**
	 * Called after the block is set in the Chunk data, but before the Tile Entity is set
	 */
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		this.setDefaultFacing(worldIn, pos, state);
	}

	private void setDefaultFacing(World worldIn, BlockPos pos, IBlockState state) {
		if (!worldIn.isRemote) {

			IBlockState north = worldIn.getBlockState(pos.north());
			IBlockState south = worldIn.getBlockState(pos.south());
			IBlockState west = worldIn.getBlockState(pos.west());
			IBlockState east = worldIn.getBlockState(pos.east());

			BrokenStoneFacingEnum enumfacing = state.getValue(FACING);

			// start up
			if (enumfacing == BrokenStoneFacingEnum.UP_NORTH && north.isFullBlock() && !south.isFullBlock()) {
				enumfacing = BrokenStoneFacingEnum.UP_SOUTH;
			} else if (enumfacing == BrokenStoneFacingEnum.UP_SOUTH && south.isFullBlock() && !north.isFullBlock()) {
				enumfacing = BrokenStoneFacingEnum.UP_NORTH;
			} else if (enumfacing == BrokenStoneFacingEnum.UP_WEST && west.isFullBlock() && !east.isFullBlock()) {
				enumfacing = BrokenStoneFacingEnum.UP_EAST;
			} else if (enumfacing == BrokenStoneFacingEnum.UP_EAST && east.isFullBlock() && !west.isFullBlock()) {
				enumfacing = BrokenStoneFacingEnum.UP_WEST;// start horizontal
			} else if (enumfacing == BrokenStoneFacingEnum.NORTH && north.isFullBlock() && !south.isFullBlock()) {
				enumfacing = BrokenStoneFacingEnum.SOUTH;
			} else if (enumfacing == BrokenStoneFacingEnum.SOUTH && south.isFullBlock() && !north.isFullBlock()) {
				enumfacing = BrokenStoneFacingEnum.NORTH;
			} else if (enumfacing == BrokenStoneFacingEnum.WEST && west.isFullBlock() && !east.isFullBlock()) {
				enumfacing = BrokenStoneFacingEnum.EAST;
			} else if (enumfacing == BrokenStoneFacingEnum.EAST && east.isFullBlock() && !west.isFullBlock()) {
				enumfacing = BrokenStoneFacingEnum.WEST;// start down
			} else if (enumfacing == BrokenStoneFacingEnum.DOWN_NORTH && north.isFullBlock() && !south.isFullBlock()) {
				enumfacing = BrokenStoneFacingEnum.DOWN_SOUTH;
			} else if (enumfacing == BrokenStoneFacingEnum.DOWN_SOUTH && south.isFullBlock() && !north.isFullBlock()) {
				enumfacing = BrokenStoneFacingEnum.DOWN_NORTH;
			} else if (enumfacing == BrokenStoneFacingEnum.DOWN_WEST && west.isFullBlock() && !east.isFullBlock()) {
				enumfacing = BrokenStoneFacingEnum.DOWN_EAST;
			} else if (enumfacing == BrokenStoneFacingEnum.DOWN_EAST && east.isFullBlock() && !west.isFullBlock()) {
				enumfacing = BrokenStoneFacingEnum.DOWN_WEST;
			}

			worldIn.setBlockState(pos, state.withProperty(FACING, enumfacing), 2);
		}
	}

	/**
	 * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the IBlockstate
	 */
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		// return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
		boolean looking_down = Math.abs(placer.getLookVec().y) >= 0.8d;
		if (facing == EnumFacing.UP && (looking_down)) {
			return this.getDefaultState().withProperty(FACING, BrokenStoneFacingEnum.getByFacing("up", placer.getHorizontalFacing().getOpposite()));
		} else if (facing == EnumFacing.DOWN) {
			return this.getDefaultState().withProperty(FACING, BrokenStoneFacingEnum.getByFacing("down", placer.getHorizontalFacing().getOpposite()));
		} else {
			return this.getDefaultState().withProperty(FACING, BrokenStoneFacingEnum.getByFacing("none", placer.getHorizontalFacing().getOpposite()));
		}
	}

}