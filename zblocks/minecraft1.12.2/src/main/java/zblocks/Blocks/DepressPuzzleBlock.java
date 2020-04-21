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
import zblocks.Blocks.Colored.ColorEnum;

public class DepressPuzzleBlock extends Block implements Colored{
	private boolean isActivated = false;
	public static IProperty<Boolean> activated = PropertyBool.create("activated");
	public static final int iACTIVATED = 1, iDISABLED = 0;
	private ColorEnum color;

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
		if (state == this.blockState.getBaseState().withProperty(activated, true)) {
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
		
		if(pos.up() != neighbor)
		{
			// neighbor isn't above me no further checks needed
			return;
		}
		
		if (block instanceof PushPuzzleBlock) {
			if (this.compareColors(ColorEnum.BASE) || // uncolored depress block activate with any color push block
					((Colored) block).compareColors(ColorEnum.BASE) || // colored bases activated with uncolored push blocks
					((Colored) block).compareColors(this)) { // colored base activates with matching color push block
				//System.out.println("my color: " + this.color + " neighbor color:" + ((PushPuzzleBlock) block).color);
				isActivated = true;
				world.setBlockState(pos, this.getDefaultState().withProperty(activated, true), 3);
			} else {
				if (isActivated == true) {
					isActivated = false;
					world.setBlockState(pos, this.getDefaultState().withProperty(activated, false), 3);
					for (EnumFacing enumfacing : EnumFacing.VALUES) {
						world.notifyNeighborsOfStateChange(pos.offset(enumfacing), this, true);
					}
				}
			}
		}
	}
	
	@Override
	public boolean compareColors(Colored other) {
		return compareColors(other.getColor());
	}
	
	@Override
	public boolean compareColors(ColorEnum other) {
		return this.color == other;
	}

	@Override
	public ColorEnum getColor() {
		return this.color;
	}
}