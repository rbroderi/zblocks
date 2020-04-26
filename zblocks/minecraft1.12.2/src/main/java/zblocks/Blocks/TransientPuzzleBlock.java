package zblocks.Blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zblocks.Blocks.Interfaces.Activatable;
import zblocks.Blocks.Interfaces.Matchable;

public class TransientPuzzleBlock extends Block implements Matchable,Activatable {
	private Class<ActivatePuzzleBlock> matchType = ActivatePuzzleBlock.class;
	public static IProperty<Boolean> activated = PropertyBool.create("activated");
	public static final int iACTIVATED = 1, iDISABLED = 0;

	public TransientPuzzleBlock(String name, Material material) {
		super(material);
		setUnlocalizedName(name);
		setRegistryName(name);
		this.setDefaultState(this.blockState.getBaseState().withProperty(activated, false));
	}

	/*
	 * @SuppressWarnings("deprecation")
	 * 
	 */


	@SuppressWarnings("deprecation")
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn,
			boolean isActualState) {
		if (entityIn instanceof EntityLiving && worldIn.getBlockState(pos) == this.blockState.getBaseState().withProperty(activated, true)) {
			return;
		}
		super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
	}
	

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		if (worldIn.getBlockState(pos) == this.blockState.getBaseState().withProperty(activated, false)) {
			return new AxisAlignedBB(0,0,0,0,0,0);
		}
		else {
		return new AxisAlignedBB(0,0,0,1,1,1);
		}
	}
	
	
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		if (state == this.blockState.getBaseState().withProperty(activated, false)) {
			return new AxisAlignedBB(0,0,0,1,1,1);
		}
		else {
		return new AxisAlignedBB(0,0,0,1,1,1);
		}
	}


	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
		return new AxisAlignedBB(0,0,0,1,1,1);
	}

	// For correct lighting around the block
	@Override
	public boolean isFullCube(IBlockState state) {
		if (state == this.blockState.getBaseState().withProperty(activated, false)) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		if (state == this.blockState.getBaseState().withProperty(activated, false)) {
			return false;
		} else {
			return true;
		}
	}

	// For rendering of block underneath
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		if (state == this.blockState.getBaseState().withProperty(activated, false)) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

//don't render side block face if it is also a transient block in deactivated state
	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		if (state == this.blockState.getBaseState().withProperty(activated, false) &&
				world.getBlockState(pos.offset(face)) == this.blockState.getBaseState().withProperty(activated, false)) {
			return false;
		}
		return super.doesSideBlockRendering(state, world, pos, face);
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
	public boolean matches(Matchable other) {
		return other.getClass() == getMatchType();
	}

	@Override
	public Class<?> getMatchType() {
		return this.matchType;
	}

	@Override
	public Object getTrait() {
		return null;
	}

	@Override
	public IProperty<Boolean> getActivatedIProperty() {
		return activated;
	}

}