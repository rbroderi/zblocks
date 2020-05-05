package zblocks.Blocks;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zblocks.Blocks.Interfaces.Destroyable;
import zblocks.Utility.StaticUtils;

public class Bomb extends Block {
	public static final PropertyInteger time = PropertyInteger.create("time", 0, 8);

	private static final AxisAlignedBB CENTER = new AxisAlignedBB(0.341, 0.094, 0.343, 0.654, 0.407, 0.655);
	private static final AxisAlignedBB ELEMENT1 = new AxisAlignedBB(0.31, 0.125, 0.374, 0.341, 0.375, 0.624);
	private static final AxisAlignedBB ELEMENT2 = new AxisAlignedBB(0.279, 0.157, 0.405, 0.31, 0.344, 0.593);
	private static final AxisAlignedBB ELEMENT3 = new AxisAlignedBB(0.341, 0.407, 0.374, 0.373, 0.438, 0.624);
	private static final AxisAlignedBB ELEMENT4 = new AxisAlignedBB(0.279, 0.313, 0.374, 0.31, 0.344, 0.405);
	private static final AxisAlignedBB ELEMENT5 = new AxisAlignedBB(0.654, 0.375, 0.374, 0.685, 0.407, 0.468);
	private static final AxisAlignedBB ELEMENT6 = new AxisAlignedBB(0.623, 0.407, 0.374, 0.654, 0.438, 0.624);
	private static final AxisAlignedBB ELEMENT7 = new AxisAlignedBB(0.654, 0.375, 0.53, 0.685, 0.407, 0.624);
	private static final AxisAlignedBB ELEMENT8 = new AxisAlignedBB(0.529, 0.375, 0.655, 0.623, 0.407, 0.686);
	private static final AxisAlignedBB ELEMENT9 = new AxisAlignedBB(0.373, 0.375, 0.655, 0.466, 0.407, 0.686);
	private static final AxisAlignedBB ELEMENT10 = new AxisAlignedBB(0.373, 0.407, 0.374, 0.623, 0.438, 0.624);
	private static final AxisAlignedBB ELEMENT11 = new AxisAlignedBB(0.435, 0.469, 0.436, 0.56, 0.5, 0.561);
	private static final AxisAlignedBB ELEMENT12 = new AxisAlignedBB(0.404, 0.438, 0.405, 0.591, 0.469, 0.593);
	private static final AxisAlignedBB ELEMENT13 = new AxisAlignedBB(0.435, 0.407, 0.655, 0.56, 0.438, 0.686);
	private static final AxisAlignedBB ELEMENT14 = new AxisAlignedBB(0.529, 0.407, 0.624, 0.623, 0.438, 0.655);
	private static final AxisAlignedBB ELEMENT15 = new AxisAlignedBB(0.404, 0.438, 0.593, 0.591, 0.469, 0.624);
	private static final AxisAlignedBB ELEMENT16 = new AxisAlignedBB(0.466, 0.438, 0.624, 0.529, 0.469, 0.655);
	private static final AxisAlignedBB ELEMENT17 = new AxisAlignedBB(0.466, 0.375, 0.686, 0.529, 0.407, 0.718);
	private static final AxisAlignedBB ELEMENT18 = new AxisAlignedBB(0.373, 0.407, 0.624, 0.466, 0.438, 0.655);
	private static final AxisAlignedBB ELEMENT19 = new AxisAlignedBB(0.466, 0.469, 0.561, 0.529, 0.5, 0.593);
	private static final AxisAlignedBB ELEMENT20 = new AxisAlignedBB(0.373, 0.438, 0.561, 0.404, 0.469, 0.593);
	private static final AxisAlignedBB ELEMENT21 = new AxisAlignedBB(0.591, 0.438, 0.561, 0.623, 0.469, 0.593);
	private static final AxisAlignedBB ELEMENT22 = new AxisAlignedBB(0.435, 0.407, 0.311, 0.56, 0.438, 0.343);
	private static final AxisAlignedBB ELEMENT23 = new AxisAlignedBB(0.529, 0.407, 0.343, 0.623, 0.438, 0.374);
	private static final AxisAlignedBB ELEMENT24 = new AxisAlignedBB(0.404, 0.438, 0.374, 0.591, 0.469, 0.405);
	private static final AxisAlignedBB ELEMENT25 = new AxisAlignedBB(0.466, 0.438, 0.343, 0.529, 0.469, 0.374);
	private static final AxisAlignedBB ELEMENT26 = new AxisAlignedBB(0.466, 0.375, 0.28, 0.529, 0.407, 0.311);
	private static final AxisAlignedBB ELEMENT27 = new AxisAlignedBB(0.373, 0.375, 0.311, 0.623, 0.407, 0.343);
	private static final AxisAlignedBB ELEMENT28 = new AxisAlignedBB(0.373, 0.407, 0.343, 0.466, 0.438, 0.374);
	private static final AxisAlignedBB ELEMENT29 = new AxisAlignedBB(0.466, 0.469, 0.405, 0.529, 0.5, 0.436);
	private static final AxisAlignedBB ELEMENT30 = new AxisAlignedBB(0.373, 0.438, 0.405, 0.404, 0.469, 0.436);
	private static final AxisAlignedBB ELEMENT31 = new AxisAlignedBB(0.591, 0.438, 0.405, 0.623, 0.469, 0.436);
	private static final AxisAlignedBB ELEMENT32 = new AxisAlignedBB(0.654, 0.407, 0.436, 0.685, 0.438, 0.561);
	private static final AxisAlignedBB ELEMENT33 = new AxisAlignedBB(0.591, 0.438, 0.436, 0.623, 0.469, 0.561);
	private static final AxisAlignedBB ELEMENT34 = new AxisAlignedBB(0.623, 0.438, 0.468, 0.654, 0.469, 0.53);
	private static final AxisAlignedBB ELEMENT35 = new AxisAlignedBB(0.685, 0.375, 0.468, 0.716, 0.407, 0.53);
	private static final AxisAlignedBB ELEMENT36 = new AxisAlignedBB(0.56, 0.469, 0.468, 0.591, 0.5, 0.53);
	private static final AxisAlignedBB ELEMENT37 = new AxisAlignedBB(0.373, 0.438, 0.436, 0.404, 0.469, 0.561);
	private static final AxisAlignedBB ELEMENT38 = new AxisAlignedBB(0.341, 0.438, 0.468, 0.373, 0.469, 0.53);
	private static final AxisAlignedBB ELEMENT39 = new AxisAlignedBB(0.279, 0.375, 0.468, 0.31, 0.407, 0.53);
	private static final AxisAlignedBB ELEMENT40 = new AxisAlignedBB(0.31, 0.375, 0.374, 0.341, 0.407, 0.624);
	private static final AxisAlignedBB ELEMENT41 = new AxisAlignedBB(0.404, 0.469, 0.468, 0.435, 0.5, 0.53);
	private static final AxisAlignedBB BOTTOM = new AxisAlignedBB(0.25, 0, 0.25, 0.748, 0.375, 0.749);
	/**
	 * AxisAlignedBBs and methods getBoundingBox, collisionRayTrace, and collisionRayTrace generated using MrCrayfish's Model Creator <a href="https://mrcrayfish.com/tools?id=mc">https://mrcrayfish.com/tools?id=mc</a>
	 */
	private static final List<AxisAlignedBB> COLLISION_BOXES = Lists.newArrayList(CENTER, ELEMENT1, ELEMENT2, ELEMENT3, ELEMENT4, ELEMENT5, ELEMENT6, ELEMENT7, ELEMENT8, ELEMENT9,
			ELEMENT10, ELEMENT11, ELEMENT12, ELEMENT13, ELEMENT14, ELEMENT15, ELEMENT16, ELEMENT17,
			ELEMENT18, ELEMENT19, ELEMENT20, ELEMENT21, ELEMENT22, ELEMENT23, ELEMENT24, ELEMENT25,
			ELEMENT26, ELEMENT27, ELEMENT28, ELEMENT29, ELEMENT30, ELEMENT31, ELEMENT32, ELEMENT33,
			ELEMENT34, ELEMENT35, ELEMENT36, ELEMENT37, ELEMENT38, ELEMENT39, ELEMENT40, ELEMENT41, BOTTOM);
	private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.25, 0, 0.25, 0.748, 0.5, 0.749);

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BOUNDING_BOX;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity,
			boolean isActualState) {
		entityBox = entityBox.offset(-pos.getX(), -pos.getY(), -pos.getZ());
		for (AxisAlignedBB box : COLLISION_BOXES) {
			if (entityBox.intersects(box))
				collidingBoxes.add(box.offset(pos));
		}
	}

	@Override
	@Nullable
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		double distanceSq;
		double distanceSqShortest = Double.POSITIVE_INFINITY;
		RayTraceResult resultClosest = null;
		RayTraceResult result;
		start = start.subtract(pos.getX(), pos.getY(), pos.getZ());
		end = end.subtract(pos.getX(), pos.getY(), pos.getZ());
		for (AxisAlignedBB box : COLLISION_BOXES) {
			result = box.calculateIntercept(start, end);
			if (result == null)
				continue;

			distanceSq = result.hitVec.squareDistanceTo(start);
			if (distanceSq < distanceSqShortest) {
				distanceSqShortest = distanceSq;
				resultClosest = result;
			}
		}
		return resultClosest == null ? null
				: new RayTraceResult(RayTraceResult.Type.BLOCK, resultClosest.hitVec.addVector(pos.getX(), pos.getY(), pos.getZ()), resultClosest.sideHit, pos);
	}

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
				StaticUtils.spawnParticleServer(worldIn, EnumParticleTypes.SMOKE_NORMAL, pos.up(), 0.001, -0.5, -2, -0.5);

			}
			worldIn.scheduleBlockUpdate(pos, worldIn.getBlockState(pos).getBlock(), 15, 1);
		} else if (itime == 1) {
			StaticUtils.spawnParticleServer(worldIn, EnumParticleTypes.EXPLOSION_HUGE, pos, 0.8);
			StaticUtils.playSound(worldIn, pos, "explosion", SoundCategory.BLOCKS, 3f);
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
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

}
