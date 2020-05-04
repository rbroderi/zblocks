package zblocks.Blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zblocks.Blocks.Interfaces.Destroyable;
import zblocks.Utility.StaticUtils;

public class Bomb extends Block {
	public static final PropertyInteger time = PropertyInteger.create("time", 0, 8);

	public Bomb(String name) {
		super(Material.ROCK);
		setUnlocalizedName(name);
		setRegistryName(name);
		this.setDefaultState(this.blockState.getBaseState().withProperty(time, 8));
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		worldIn.setBlockState(pos, this.getDefaultState().withProperty(time, 8));
		worldIn.scheduleBlockUpdate(pos, worldIn.getBlockState(pos).getBlock(), 15, 1);
		StaticUtils.spawnParticleServer(worldIn, EnumParticleTypes.FIREWORKS_SPARK, pos.up(), 0.05);
	}

	@Override
	public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn) {
		//
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(time, meta);
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(time).intValue();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, time);
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

	/*
	 * @Override public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) { int itime = world.getBlockState(pos).getValue(time); if (!world.isRemote) { world.setBlockState(pos, getStateFromMeta(itime > 0 ? itime - 1 : 0)); } if (itime == 0) { explode(world, pos, player); } }
	 */

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		int itime = worldIn.getBlockState(pos).getValue(time);
		if (itime == 8) {
			StaticUtils.playSound(worldIn, pos, "fuse", SoundCategory.BLOCKS, 1f);
		}
		if (itime <= 8 && itime > 1) {
			if (!worldIn.isRemote) {
				worldIn.setBlockState(pos, getStateFromMeta(itime > 0 ? itime - 1 : 0));
				StaticUtils.spawnParticleServer(worldIn, EnumParticleTypes.SMOKE_NORMAL, pos.up(), 0.001);
			}
			worldIn.scheduleBlockUpdate(pos, worldIn.getBlockState(pos).getBlock(), 15, 1);
		} else if (itime == 1) {
			StaticUtils.spawnParticleServer(worldIn, EnumParticleTypes.EXPLOSION_HUGE, pos, 0.8);
			worldIn.setBlockState(pos, getStateFromMeta(0));
			worldIn.scheduleBlockUpdate(pos, worldIn.getBlockState(pos).getBlock(), 15, 1);
		} else if (itime == 0) {
			explode(worldIn, pos, worldIn.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 10, false));
		}
	}

	private void explode(World world, BlockPos pos, EntityPlayer player) {

		if (!world.isRemote) {
			BlockPos northWest = pos.north(3).west(3).up(5);
			BlockPos southEast = pos.south(3).east(3).down(5);
			for (BlockPos bPos : BlockPos.getAllInBoxMutable(northWest, southEast)) {
				IBlockState old = world.getBlockState(bPos);
				Block block = old.getBlock();
				if (block instanceof Destroyable) {
					bPos = bPos.toImmutable();
					((Destroyable) block).Destroy(world, bPos);
				}
			}
			world.setBlockToAir(pos);
		}
		// StaticUtils.spawnParticle(player, EnumParticleTypes.SMOKE_LARGE, pos.east());
		// StaticUtils.spawnParticle(player, EnumParticleTypes.SMOKE_LARGE, pos.west());
		// StaticUtils.spawnParticle(player, EnumParticleTypes.SMOKE_LARGE, pos.north());
		// StaticUtils.spawnParticle(player, EnumParticleTypes.SMOKE_LARGE, pos.south());
		// StaticUtils.spawnParticle(player, EnumParticleTypes.SMOKE_LARGE, pos.down());
		StaticUtils.playSound(world, pos, "explosion", SoundCategory.BLOCKS, 3f);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

}
