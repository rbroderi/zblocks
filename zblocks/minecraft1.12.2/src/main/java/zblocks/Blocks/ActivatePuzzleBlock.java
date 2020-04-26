package zblocks.Blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zblocks.Blocks.Interfaces.Matchable;
import zblocks.Utility.StaticUtils;

public class ActivatePuzzleBlock extends Block implements Matchable {
	private Class<TransientPuzzleBlock> matchType = TransientPuzzleBlock.class;
	public static IProperty<Boolean> activated = PropertyBool.create("activated");
	public static final int iACTIVATED = 1, iDISABLED = 0;

	public ActivatePuzzleBlock(String name, Material material) {
		super(material);
		setUnlocalizedName(name);
		setRegistryName(name);
		this.setDefaultState(this.blockState.getBaseState().withProperty(activated, false));
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

	/**
	 * toggle activation with hits by arrow
	 */
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		// super.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
		if (entityIn instanceof IProjectile) {
			if (state == this.blockState.getBaseState().withProperty(activated, false)) {
				//ejectEntityLiving(world,player, pos);
				worldIn.setBlockState(pos, state.getBlock().getDefaultState().withProperty(activated, true));
			} else {
				worldIn.removeEntity(entityIn);
				worldIn.setBlockState(pos, state.getBlock().getDefaultState().withProperty(activated, false));
			}
		}
		//worldIn.removeEntity(entityIn);
	}

	/**
	 * When left-clicked knockback all living entities inside block and toggle activation
	 */
	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		// super.onBlockClicked(world, pos, player);
		if (StaticUtils.isNextToAndNoYMotion(player, pos, 1.94f)) {
			if (world.getBlockState(pos) == this.blockState.getBaseState().withProperty(activated, false)) {
				//ejectEntityLiving(world,player, pos);
				world.setBlockState(pos, world.getBlockState(pos).getBlock().getDefaultState().withProperty(activated, true));
			} else {
				world.setBlockState(pos, world.getBlockState(pos).getBlock().getDefaultState().withProperty(activated, false));
			}
		}
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

	/*
	 * // causes crash
	 * 
	 * @SuppressWarnings("deprecation")
	 * 
	 * @Override public int getWeakPower(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) { int ret = 0; for (EnumFacing facing : EnumFacing.VALUES) { if (getWeakPower(state, blockAccess, pos.offset(facing), facing) > 0) { if (blockAccess instanceof World) { ((World) blockAccess).setBlockState(pos, this.getDefaultState().withProperty(activated, true), 3); ret = 15; } } } return ret == 15 ? ret : super.getWeakPower(state, blockAccess, pos, side); }
	 */

	@Override
	public boolean matches(Matchable other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class<?> getMatchType() {
		return this.matchType;
	}

	@Override
	public Object getTrait() {
		// TODO Auto-generated method stub
		return null;
	}

}