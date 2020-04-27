package zblocks.Blocks;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zblocks.Blocks.Interfaces.Activatable;
import zblocks.Blocks.Interfaces.Matchable;
import zblocks.Utility.StaticUtils;

public class ActivatePuzzleBlock extends Block implements Matchable {

	public enum ActivationEnum implements IStringSerializable {
		DEACTIVATED(0), HIT(1), REDSTONE(3);

		private final int value;
		private final static Map<Integer, ActivationEnum> lookup;
		static {
			Map<Integer, ActivationEnum> lookupTemp = new HashMap<Integer, ActivationEnum>();
			for (ActivationEnum e : ActivationEnum.values()) {
				lookupTemp.put(e.getValue(), e);
			}

			lookup = Collections.unmodifiableMap(lookupTemp);
		}

		private ActivationEnum(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static ActivationEnum getByValue(int i) {
			/*
			 * switch(i) { case 0: return DEACTIVATED; case 1: return HIT; case 2: return REDSTONE; default: return DEACTIVATED; }
			 */
			return lookup.get(i);
		}

		@Override
		// Property names (in your case probably returned by IStringSerializable::getName) must be all lowercase, in fact they must match
		// the regular expression [a-z0-9_]+.
		public String getName() {
			return this.name().toLowerCase();
		}
	}

	private Class<TransientPuzzleBlock> matchType = TransientPuzzleBlock.class;
	public static IProperty<ActivationEnum> activated = PropertyEnum.create("activated", ActivationEnum.class);
	// private Queue<Entity> ignoreList = new LinkedList<Entity>();
	private static final int IGNORE_LIST_LIMIT = 1000;
	Queue<Entity> ignoreList = EvictingQueue.create(IGNORE_LIST_LIMIT);

	private static final AxisAlignedBB BASE_TOP_UPPER = new AxisAlignedBB(0.312, 0.375, 0.312, 0.688, 0.438, 0.688);
	private static final AxisAlignedBB BASE_TOP_LOWER = new AxisAlignedBB(0.375, 0.312, 0.375, 0.625, 0.375, 0.625);
	private static final AxisAlignedBB BASE = new AxisAlignedBB(0.062, 0, 0.062, 0.938, 0.312, 0.938);
	private static final AxisAlignedBB CRYSTAL = new AxisAlignedBB(0.312, 0.438, 0.312, 0.688, 0.812, 0.688);
	private static final AxisAlignedBB TOP = new AxisAlignedBB(0.312, 0.812, 0.25, 0.688, 0.875, 0.688);
	private static final AxisAlignedBB WEST = new AxisAlignedBB(0.25, 0.438, 0.25, 0.312, 0.875, 0.75);
	private static final AxisAlignedBB EAST = new AxisAlignedBB(0.688, 0.438, 0.25, 0.75, 0.875, 0.75);
	private static final AxisAlignedBB SOUTH = new AxisAlignedBB(0.312, 0.438, 0.688, 0.688, 0.875, 0.75);
	private static final AxisAlignedBB NORTH = new AxisAlignedBB(0.312, 0.438, 0.25, 0.688, 0.812, 0.312);

	/**
	 * AxisAlignedBBs and methods getBoundingBox, collisionRayTrace, and collisionRayTrace generated using MrCrayfish's Model Creator <a href="https://mrcrayfish.com/tools?id=mc">https://mrcrayfish.com/tools?id=mc</a>
	 */
	private static final List<AxisAlignedBB> COLLISION_BOXES = Lists.newArrayList(BASE_TOP_UPPER, BASE_TOP_LOWER, BASE, CRYSTAL, TOP, WEST, EAST, SOUTH, NORTH);
	private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.062, 0, 0.062, 0.938, 0.875, 0.938);

	public ActivatePuzzleBlock(String name, Material material) {
		super(material);
		setUnlocalizedName(name);
		setRegistryName(name);
		this.setDefaultState(this.blockState.getBaseState().withProperty(activated, ActivationEnum.DEACTIVATED));
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

	/**
	 * toggle activation with hits by arrow
	 */
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		if (!worldIn.isRemote) {
			if (entityIn instanceof IProjectile) {
				boolean isNew = !ignoreList.contains(entityIn);
				ignoreList.add(entityIn);
				if (isNew && state == this.blockState.getBaseState().withProperty(activated, ActivationEnum.DEACTIVATED)) {
					// ejectEntityLiving(world,player, pos);
					worldIn.setBlockState(pos, state.getBlock().getDefaultState().withProperty(activated, ActivationEnum.HIT));
					setNearbyMatchesActivation(worldIn, pos, true);
					StaticUtils.playSound(worldIn, pos, "glass_ting", SoundCategory.BLOCKS, 2f);
				} else if (isNew) {
					// worldIn.removeEntity(entityIn);
					worldIn.setBlockState(pos, state.getBlock().getDefaultState().withProperty(activated, ActivationEnum.DEACTIVATED));
					setNearbyMatchesActivation(worldIn, pos, false);
					StaticUtils.playSound(worldIn, pos, "glass_ting", SoundCategory.BLOCKS, 2f);
				}
			}
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
				if (world.getBlockState(pos) == this.blockState.getBaseState().withProperty(activated, ActivationEnum.DEACTIVATED)) {
					// ejectEntityLiving(world,player, pos);
					world.setBlockState(pos, world.getBlockState(pos).getBlock().getDefaultState().withProperty(activated, ActivationEnum.HIT));
					setNearbyMatchesActivation(world, pos, true);
					StaticUtils.playSound(world, pos, "glass_ting", SoundCategory.BLOCKS, 2f);
				} else {
					world.setBlockState(pos, world.getBlockState(pos).getBlock().getDefaultState().withProperty(activated, ActivationEnum.HIT));
					setNearbyMatchesActivation(world, pos, false);
					StaticUtils.playSound(world, pos, "glass_ting", SoundCategory.BLOCKS, 2f);

				}
			}
		}
	}

	@Override
	public int getLightValue(IBlockState state) {
		if (state == this.blockState.getBaseState().withProperty(activated, ActivationEnum.HIT) ||
				state == this.blockState.getBaseState().withProperty(activated, ActivationEnum.REDSTONE)) {
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
		if (state == this.blockState.getBaseState().withProperty(activated, ActivationEnum.HIT)) {
			return ActivationEnum.HIT.getValue();
		} else if (state == this.blockState.getBaseState().withProperty(activated, ActivationEnum.REDSTONE)) {
			return ActivationEnum.REDSTONE.getValue();
		} else {
			return ActivationEnum.DEACTIVATED.getValue();
		}
	}

	@Override
	public IBlockState getStateFromMeta(int i) {
		return this.blockState.getBaseState().withProperty(activated, ActivationEnum.getByValue(i));
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (!worldIn.isRemote) {
			Boolean isAct = (state == worldIn.getBlockState(pos).withProperty(activated, ActivationEnum.HIT) ||
					state == worldIn.getBlockState(pos).withProperty(activated, ActivationEnum.REDSTONE));
			if (isAct && !worldIn.isBlockPowered(pos)) {
				worldIn.setBlockState(pos, this.getDefaultState().withProperty(activated, ActivationEnum.DEACTIVATED), 2);
				setNearbyMatchesActivation(worldIn, pos, false);
				StaticUtils.playSound(worldIn, pos, "glass_ting", SoundCategory.BLOCKS, 2f);
			} else if (!isAct && worldIn.isBlockPowered(pos)) {
				setNearbyMatchesActivation(worldIn, pos, true);
				StaticUtils.playSound(worldIn, pos, "glass_ting", SoundCategory.BLOCKS, 2f);
				worldIn.setBlockState(pos, this.getDefaultState().withProperty(activated, ActivationEnum.REDSTONE), 2);
			}
		}
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (!worldIn.isRemote) {
			Boolean isAct = (state == worldIn.getBlockState(pos).withProperty(activated, ActivationEnum.REDSTONE));
			if (isAct && !worldIn.isBlockPowered(pos)) {
				worldIn.setBlockState(pos, this.getDefaultState().withProperty(activated, ActivationEnum.DEACTIVATED), 2);
				setNearbyMatchesActivation(worldIn, pos, false);
				StaticUtils.playSound(worldIn, pos, "glass_ting", SoundCategory.BLOCKS, 2f);
			}
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
		// TODO Auto-generated method stub
		return null;
	}

}