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
import zblocks.Blocks.Interfaces.Colored;
import zblocks.Blocks.Interfaces.Matchable;

public class DepressPuzzleBlock extends Block implements Colored, Matchable {
	// private boolean isActivated = false;
	public static IProperty<Boolean> activated = PropertyBool.create("activated");
	public static final int iACTIVATED = 1, iDISABLED = 0;
	private ColorEnum color;
	private Class<PushPuzzleBlock> matchType = PushPuzzleBlock.class;

	public DepressPuzzleBlock(String name, Material material, ColorEnum color) {
		super(material);
		setUnlocalizedName(color == ColorEnum.BASE ? name : name + "_" + color.getName());
		setRegistryName(color == ColorEnum.BASE ? name : name + "_" + color.getName());
		this.useNeighborBrightness = true; // workaround for lighting issue - culling face not working with insets
		this.setDefaultState(this.blockState.getBaseState().withProperty(activated, false));
		this.color = color;
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, activated);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		if (state.equals(this.blockState.getBaseState().withProperty(activated, true))) {
			return iACTIVATED;
		} else {
			return iDISABLED;
		}
	}

	@Override
	public IBlockState getStateFromMeta(int i) {
		if (i == iACTIVATED) {
			return this.blockState.getBaseState().withProperty(activated, true);
		} else {
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
		if (state == this.getDefaultState().withProperty(activated, true)) {
			return 15;
		}
		return super.getWeakPower(state, blockAccess, pos, side);
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	// TODO needs cleanup
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos neighbor) {
		Block up = world.getBlockState(pos.up()).getBlock();
		if (pos.up().equals(neighbor)) {
			if (up instanceof Matchable && this.matches((Matchable) up)) {
				world.setBlockState(pos, this.getDefaultState().withProperty(activated, true), 3);
			} else {
				world.setBlockState(pos, this.getDefaultState().withProperty(activated, false), 3);
			}
			for (EnumFacing enumfacing : EnumFacing.VALUES) {
				if (enumfacing == EnumFacing.UP) {
					continue;
				}
				world.notifyNeighborsOfStateChange(pos.offset(enumfacing), this, true);
			}
		}

	}

	// For correct lighting around the block
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean matches(Matchable other) {
		if (!this.getClass().equals(other.getMatchType())) {
			return false;
		}
		if (!(other.getTrait() instanceof ColorEnum)) {
			return false;
		}
		return ((ColorEnum) this.getTrait()).compare(((ColorEnum) other.getTrait()));
	}

	@Override
	public Object getTrait() {
		return this.color;
	}

	@Override
	public Class<?> getMatchType() {
		return this.matchType;
	}

	@Override
	public ColorEnum getColor() {
		return this.color;
	}
}