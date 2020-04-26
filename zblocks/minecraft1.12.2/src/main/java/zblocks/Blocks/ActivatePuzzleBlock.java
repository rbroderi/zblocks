package zblocks.Blocks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zblocks.Blocks.Interfaces.Activatable;
import zblocks.Blocks.Interfaces.Matchable;
import zblocks.Utility.StaticUtils;

public class ActivatePuzzleBlock extends Block implements Matchable {
	private Class<TransientPuzzleBlock> matchType = TransientPuzzleBlock.class;
	public static IProperty<Boolean> activated = PropertyBool.create("activated");
	public static final int iACTIVATED = 1, iDISABLED = 0;
	private Queue<Entity> ignoreList = new LinkedList<Entity>();

	public ActivatePuzzleBlock(String name, Material material) {
		super(material);
		setUnlocalizedName(name);
		setRegistryName(name);
		this.setDefaultState(this.blockState.getBaseState().withProperty(activated, false));
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
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		//return new AxisAlignedBB(0.188, 0, 0.125, 0.812, 0.900, 0.812);
		return new AxisAlignedBB(0.2, 0, 0.2, 0.78, 0.900, 0.78);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		// TODO Auto-generated method stub
		return new AxisAlignedBB(0.2, 0, 0.2, 0.78, 0.900, 0.78);
	}

	/*
	 * @Override public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) { // TODO Auto-generated method stub return new AxisAlignedBB(0.1, 0, 0.1, 0.9, 0.9, 0.9); }
	 */

	/**
	 * toggle activation with hits by arrow
	 */
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		if (!worldIn.isRemote) {
			if (entityIn instanceof IProjectile) {
				boolean isNew = !ignoreList.contains(entityIn);
				ignoreList.add(entityIn);
				if (ignoreList.size() > 1000) {
					ignoreList.poll();
				}
				if (isNew && state == this.blockState.getBaseState().withProperty(activated, false)) {
					// ejectEntityLiving(world,player, pos);
					worldIn.setBlockState(pos, state.getBlock().getDefaultState().withProperty(activated, true));
					setNearbyMatchesActivation(worldIn, pos, true);
					StaticUtils.playSound(worldIn, pos, "glass_ting", SoundCategory.BLOCKS, 2f);
				} else if (isNew) {
					// worldIn.removeEntity(entityIn);
					worldIn.setBlockState(pos, state.getBlock().getDefaultState().withProperty(activated, false));
					setNearbyMatchesActivation(worldIn, pos, false);
					StaticUtils.playSound(worldIn, pos, "glass_ting", SoundCategory.BLOCKS, 2f);
				}
			}
			// worldIn.removeEntity(entityIn);
		}
	}


	/**
	 * When left-clicked toggle activation
	 */
	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (!world.isRemote) {
			// super.onBlockClicked(world, pos, player);
			if (StaticUtils.isNextToAndNoYMotion(player, pos, 1.94f)) {
				if (world.getBlockState(pos) == this.blockState.getBaseState().withProperty(activated, false)) {
					// ejectEntityLiving(world,player, pos);
					world.setBlockState(pos, world.getBlockState(pos).getBlock().getDefaultState().withProperty(activated, true));
					setNearbyMatchesActivation(world, pos, true);
					StaticUtils.playSound(world, pos, "glass_ting", SoundCategory.BLOCKS, 2f);
				} else {
					world.setBlockState(pos, world.getBlockState(pos).getBlock().getDefaultState().withProperty(activated, false));
					setNearbyMatchesActivation(world, pos, false);
					StaticUtils.playSound(world, pos, "glass_ting", SoundCategory.BLOCKS, 2f);

				}
			}
		}
	}

	@Override
	public int getLightValue(IBlockState state) {
		if (state == this.blockState.getBaseState().withProperty(activated, true)) {
			return 15;
		}
		return 0;
	}

//searches 100x50x100 area
	private void setNearbyMatchesActivation(World world, BlockPos pos, boolean value) {
		BlockPos northWest = pos.north(50).west(50).up(25);
		BlockPos southEast = pos.south(50).east(50).down(25);
		for (BlockPos bPos : BlockPos.getAllInBoxMutable(northWest, southEast)) {
			Block block = world.getBlockState(bPos).getBlock();
			if (block instanceof Matchable && this.matches((Matchable) block) && block instanceof Activatable) {
				bPos = bPos.toImmutable();
				Matchable match = ((Matchable) block);
				Activatable act = (Activatable) match;
				world.setBlockState(bPos, block.getDefaultState().withProperty(act.getActivatedIProperty(), value));
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
		return other.getClass() == getMatchType();
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