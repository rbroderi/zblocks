package zblocks.Blocks;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import zblocks.SlidingEventData;
import zblocks.Blocks.Interfaces.Colored;
import zblocks.Blocks.Interfaces.Matchable;
import zblocks.Blocks.Interfaces.Resettable;
import zblocks.Handlers.SlidingEventHandler;
import zblocks.TileEntities.ResetDataTileEntity;
import zblocks.Utility.EphemeralQueue;
import zblocks.Utility.StaticUtils;

public class PushPuzzleBlock extends BlockFalling implements Colored, Matchable, Resettable {

	public static IProperty<Boolean> activated = PropertyBool.create("activated");
	public static IProperty<Boolean> frozen = PropertyBool.create("frozen");
	public static final int iACTIVATED = 1, iDISABLED = 0;
	public static final int iFROZEN = 2;
	private ColorEnum color;
	private Class<DepressPuzzleBlock> matchType = DepressPuzzleBlock.class;
	// public static CopyOnWriteArrayList<SlidingEventData> currentlySlidingBlocks = new CopyOnWriteArrayList<SlidingEventData>();
	public static EphemeralQueue<SlidingEventData> currentlySlidingBlocks = new EphemeralQueue<SlidingEventData>();

	// this needs to be stored in tileentity private BlockPos startPos;
	public PushPuzzleBlock(String name, Material material, ColorEnum color) {
		super(material);
		setUnlocalizedName(color == ColorEnum.BASE ? name : name + "_" + color.getName());
		setRegistryName(color == ColorEnum.BASE ? name : name + "_" + color.getName());
		this.useNeighborBrightness = true; // workaround for lighting issue - culling face not working with insets
		this.setDefaultState(this.blockState.getBaseState().withProperty(activated, false).withProperty(frozen, false));
		this.color = color;
	}

	public PushPuzzleBlock() {

	}

	// ******************************** Public Overrides ****************************************************/

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	public static ResetDataTileEntity getTileEntity(IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof ResetDataTileEntity) {
			return (ResetDataTileEntity) tile;
		}
		if (tile != null) {
			System.err.println("Warning invalid tile entity returned\nExpected ResetDataTileEnity got:" + tile.getClass());
		}
		return null;
	}

	@Nullable
	@Override
	public ResetDataTileEntity createTileEntity(World world, IBlockState state) {
		return new ResetDataTileEntity();
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, activated, frozen);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		if (state == this.blockState.getBaseState().withProperty(activated, true).withProperty(frozen, true)) {
			return iACTIVATED + iFROZEN; // 3
		} else if (state == this.blockState.getBaseState().withProperty(activated, false).withProperty(frozen, true)) {
			return iFROZEN; // 2
		} else if (state == this.blockState.getBaseState().withProperty(activated, true).withProperty(frozen, false)) {
			return iACTIVATED; // 1
		} else {
			return iDISABLED; // 0
		}
	}

	@Override
	public IBlockState getStateFromMeta(int i) {
		if (i == iACTIVATED + iFROZEN) { // 3
			return this.blockState.getBaseState().withProperty(activated, true).withProperty(frozen, true);
		} else if (i == iFROZEN) { // 2
			return this.blockState.getBaseState().withProperty(activated, false).withProperty(frozen, true);
		} else if (i == iACTIVATED) { // 1
			return this.blockState.getBaseState().withProperty(activated, true).withProperty(frozen, false);
		} else { // 0
			return this.blockState.getBaseState().withProperty(activated, false).withProperty(frozen, false);
		}
	}

	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getStrongPower(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		// if(isActivated) {
		// return 15;
		// }
		return super.getStrongPower(state, blockAccess, pos, side);
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getWeakPower(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		// if(isActivated) {
		// return 15;
		// }
		return super.getWeakPower(state, blockAccess, pos, side);
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		setActivated(world, pos);
		ResetDataTileEntity tile = getTileEntity(world, pos);
		tile.setStartPos(pos);

		if (world.getBlockState(pos.down()).getBlock() == Blocks.ICE) {
			world.setBlockState(pos, world.getBlockState(pos).withProperty(frozen, true));
		} else {
			world.setBlockState(pos, world.getBlockState(pos).withProperty(frozen, false));
		}

		// this.startPos = pos;
		for (EnumFacing enumfacing : EnumFacing.VALUES) {
			world.notifyNeighborsOfStateChange(pos.offset(enumfacing), this, true);
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos neighbor) {
		super.neighborChanged(state, world, pos, block, neighbor);
		if (pos.down() == neighbor) {
			if (SlidingEventHandler.SLIDEBLOCKS.contains(world.getBlockState(neighbor).getBlock())) {
				world.setBlockState(pos, world.getBlockState(pos).withProperty(frozen, true));
			} else {
				world.setBlockState(pos, world.getBlockState(pos).withProperty(frozen, false));
			}
		}
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {
		super.onBlockDestroyedByPlayer(world, pos, state);
		for (EnumFacing enumfacing : EnumFacing.VALUES) {
			world.notifyNeighborsOfStateChange(pos.offset(enumfacing), this, true);
		}
	}

	@Override
	public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion ex) {
		super.onBlockDestroyedByExplosion(world, pos, ex);
		for (EnumFacing enumfacing : EnumFacing.VALUES) {
			world.notifyNeighborsOfStateChange(pos.offset(enumfacing), this, true);
		}
	}

	@Override
	public void onEndFalling(World world, BlockPos pos, IBlockState s1, IBlockState s2) {
		super.onEndFalling(world, pos, s1, s2);

		if (!world.isRemote && world.getTileEntity(pos) instanceof ResetDataTileEntity) {
			// TODO there must be a better way to do this?
			String startPosString = world.getEntitiesWithinAABB(EntityFallingBlock.class, new AxisAlignedBB(pos)).get(0).getTags().toArray()[0].toString();
			String[] startPosStringArray = startPosString.split(",");
			((ResetDataTileEntity) world.getTileEntity(pos)).setStartPos(
					new BlockPos(Integer.parseInt(startPosStringArray[0]),
							Integer.parseInt(startPosStringArray[1]),
							Integer.parseInt(startPosStringArray[2])));
			StaticUtils.playSound(world, pos, "thud_delay", SoundCategory.BLOCKS, 1f);
		}
	}

	// When left-clicked
	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (moveBlockTo(player.world, player, pos, pos.offset(player.getHorizontalFacing()))) { // .worldObj
			if (!world.isRemote) {
				// don't play scrape if block will fall
				if (!world.isAirBlock((pos.offset(player.getHorizontalFacing()).down()))) {
					StaticUtils.playSound(world, pos, "scrape", SoundCategory.BLOCKS, 1f);
				}
			}
		}
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (!worldIn.isRemote) {
			this.checkFallable(worldIn, pos);
		}
	}

	// changes default checkFallable logic
	private void checkFallable(World worldIn, BlockPos pos) {
		if ((worldIn.isAirBlock(pos.down()) || canFallThrough(worldIn.getBlockState(pos.down()))) && pos.getY() >= 0) {
			// int i = 32;

			if (!fallInstantly && worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32))) {
				if (!worldIn.isRemote && worldIn.getTileEntity(pos) instanceof ResetDataTileEntity) {
					EntityFallingBlock entityfallingblock = new EntityFallingBlock(worldIn, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, worldIn.getBlockState(pos));
					// TODO see on end falling, there must be a better way to track startPos through fall
					entityfallingblock.addTag(((ResetDataTileEntity) worldIn.getTileEntity(pos)).getStartPosAsString());
					this.onStartFalling(entityfallingblock);
					worldIn.spawnEntity(entityfallingblock);
				}
			} else {
				IBlockState state = worldIn.getBlockState(pos);
				worldIn.setBlockToAir(pos);
				BlockPos blockpos;

				for (blockpos = pos.down(); (worldIn.isAirBlock(blockpos) || canFallThrough(worldIn.getBlockState(blockpos))) && blockpos.getY() > 0; blockpos = blockpos.down()) {
					;
				}

				if (blockpos.getY() > 0) {
					worldIn.setBlockState(blockpos.up(), state); // Forge: Fix loss of state information during world gen.
				}
			}
		}
	}

	/**
	 * Get the geometry of the queried face at the given position and state. This is used to decide whether things like buttons are allowed to be placed on the face, or how glass panes connect to the face, among other things.
	 * <p>
	 * Common values are {@code SOLID}, which is the default, and {@code UNDEFINED}, which represents something that does not fit the other descriptions and will generally cause other things not to connect to the face.
	 * 
	 * @return an approximation of the form of the given face
	 */
	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	// ******************************** Public ****************************************************/

	public boolean moveBlockTo(World world, EntityPlayer player, BlockPos pos, BlockPos posMoveToHere) {
		boolean ret = false;
		IBlockState hit = world.getBlockState(pos);
		// player has hit block, is next to this block, the block does not have anything on top of it, and has a space to slide into
		if (hit.getBlock().equals(this) && StaticUtils.isNextToAndNoYMotion(player, pos) && world.isAirBlock(pos.offset(EnumFacing.UP))
				&& world.isAirBlock(posMoveToHere) && world.isBlockModifiable(player, pos)) {
			if (!world.isRemote) {
				// world.destroyBlock(pos, false);
				ResetDataTileEntity tile = getTileEntity(world, pos);
				BlockPos startPos = tile.getStartPos();
				world.setBlockState(pos, Blocks.AIR.getDefaultState());
				world.setBlockState(posMoveToHere, hit);// pushes the block

				EnumFacing facing = player.getHorizontalFacing();
				if (SlidingEventHandler.isSlidingAndFrontIsClear(world, posMoveToHere, posMoveToHere.offset(facing))) {
					currentlySlidingBlocks.enqueue(
							new SlidingEventData(world, posMoveToHere, player.getHorizontalFacing(), hit, startPos, SlidingEventData.DEFAULT_MOMENTUM));
				}
				setActivated(world, player, posMoveToHere);
				if (SlidingEventHandler.SLIDEBLOCKS.contains(world.getBlockState(posMoveToHere.down()).getBlock())) {
					world.setBlockState(posMoveToHere, world.getBlockState(posMoveToHere).withProperty(frozen, true));
				}
				world.notifyBlockUpdate(posMoveToHere, hit, world.getBlockState(posMoveToHere), 3);
				tile = getTileEntity(world, posMoveToHere);
				tile.setStartPos(startPos);
			}
			ret = true;
		}
		return ret;
	}

	@Override
	public ColorEnum getColor() {
		return this.color;
	}

	// For correct lighting around the block
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

//************************************* Private **************************************************************************

	private void setActivated(World world, EntityPlayer player, BlockPos pos) {
		Block downBlock = world.getBlockState(pos.down()).getBlock();

		if (downBlock instanceof Matchable && this.matches((Matchable) downBlock)) {
			world.setBlockState(pos, this.getDefaultState().withProperty(activated, true), 3);
			StaticUtils.spawnParticle(player, EnumParticleTypes.CRIT_MAGIC, pos);

			// world.notifyNeighborsOfStateChange(pos.down(), this, true);
		} else {
			world.setBlockState(pos, this.getDefaultState().withProperty(activated, false), 3);
			world.notifyNeighborsOfStateChange(pos.down(), this, true);
		}
	}

	private void setActivated(World world, BlockPos pos) {
		Block downBlock = world.getBlockState(pos.down()).getBlock();

		if (downBlock instanceof Matchable) {
			if (this.matches((Matchable) downBlock)) {
				world.setBlockState(pos, this.getDefaultState().withProperty(activated, true), 3);
				// StaticUtils.spawnParticle(player, EnumParticleTypes.CRIT_MAGIC, pos); //TODO find out how to spawn this particle without player - tried world.search
				world.notifyNeighborsOfStateChange(pos.down(), this, true);
			} else {
				world.setBlockState(pos, this.getDefaultState().withProperty(activated, false), 3);
				world.notifyNeighborsOfStateChange(pos.down(), this, true);
			}
		}
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
	public void resetPosition(World world, BlockPos pos) {
		if (!world.isRemote) {
			// System.out.println("reseting");
			ResetDataTileEntity tile = getTileEntity(world, pos);
			BlockPos startPos = tile.getStartPos();
			if (startPos != null) { // perhaps set startPos to 0,0,0 as default?
				// System.out.println("x: " + pos.getX() + "," + "y: " + pos.getY() + "," + "z: " + pos.getZ());
				// System.out.println("x: " + startPos.getX() + "," + "y: " + startPos.getY() + "," + "z: " + startPos.getZ());
				if (pos != startPos) {
					world.setBlockState(pos, Blocks.AIR.getDefaultState());
					world.setBlockState(startPos, this.getDefaultState());
				}
			}
		}
	}
}