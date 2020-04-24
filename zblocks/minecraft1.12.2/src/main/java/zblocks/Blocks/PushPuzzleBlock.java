package zblocks.Blocks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import zblocks.SlidingEventData;
import zblocks.Utils;
import zblocks.Blocks.Interfaces.Colored;
import zblocks.Blocks.Interfaces.Matchable;
import zblocks.Blocks.Interfaces.Resettable;
import zblocks.TileEntities.ResetDataTileEntity;

public class PushPuzzleBlock extends BlockFalling implements Colored, Matchable, Resettable {

	public static IProperty<Boolean> activated = PropertyBool.create("activated");
	public static final int iACTIVATED = 1, iDISABLED = 0;
	private ColorEnum color;
	private Class<DepressPuzzleBlock> matchType = DepressPuzzleBlock.class;
	public static CopyOnWriteArrayList<SlidingEventData> currentlySlidingBlocks = new CopyOnWriteArrayList<SlidingEventData>();

	// this needs to be stored in tileentity private BlockPos startPos;
	public PushPuzzleBlock(String name, Material material, ColorEnum color) {
		super(material);
		setUnlocalizedName(color == ColorEnum.BASE ? name : name + "_" + color.getName());
		setRegistryName(color == ColorEnum.BASE ? name : name + "_" + color.getName());
		this.useNeighborBrightness = true; // workaround for lighting issue - culling face not working with insets
		this.setDefaultState(this.blockState.getBaseState().withProperty(activated, false));
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
		if(tile !=null) {
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
		//world.getEntitiesWithinAABB(EntityLivingBase.class, bb);
		setActivated(world, pos);
		ResetDataTileEntity tile = getTileEntity(world, pos);
		// first time we have placed block
		if (tile.getStartPos() == null) {
			tile.setStartPos(pos);
		}
		// this.startPos = pos;
		for (EnumFacing enumfacing : EnumFacing.VALUES) {
			world.notifyNeighborsOfStateChange(pos.offset(enumfacing), this, true);
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

		if (!world.isRemote) // world.isRemote means it's the client and there is no WorldServer
		{
			ResourceLocation location = new ResourceLocation("zblock", "thud_delay");
			// SoundEvent event = new SoundEvent(location);
			world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvent.REGISTRY.getObject(location),
					SoundCategory.BLOCKS, 1f, 1f);
		}
	}

	// When left-clicked
	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		super.onBlockClicked(world, pos, player);
		if (moveBlockTo(player.world, player, pos, pos.offset(player.getHorizontalFacing()))) { // .worldObj
			if (!world.isRemote) // world.isRemote means it's the client and there is no WorldServer
			{
				// don't play scrape if block will fall
				if (!world.isAirBlock((pos.offset(player.getHorizontalFacing()).offset(EnumFacing.DOWN)))) {
					ResourceLocation location = new ResourceLocation("zblock", "scrape");
					// SoundEvent event = new SoundEvent(location);
					world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvent.REGISTRY.getObject(location), SoundCategory.BLOCKS,
							1f, 1f);
				}
			}
		}
	}

	// ******************************** Public ****************************************************/

	public boolean moveBlockTo(World world, EntityPlayer player, BlockPos pos, BlockPos posMoveToHere) {
		boolean ret = false;
		IBlockState hit = world.getBlockState(pos);
		// player has hit block, is next to this block, the block does not have anything on top of it, and has a space to slide into
		if (hit.getBlock().equals(this) && Utils.isNextTo(player, pos) && world.isAirBlock(pos.offset(EnumFacing.UP))
				&& world.isAirBlock(posMoveToHere) && world.isBlockModifiable(player, pos)) {
			if (!world.isRemote) {
				// world.destroyBlock(pos, false);
				BlockPos startPos = getTileEntity(world, pos).getStartPos();
				world.setBlockState(pos, Blocks.AIR.getDefaultState());
				world.setBlockState(posMoveToHere, hit);// pushes the block
				getTileEntity(world, posMoveToHere).setStartPos(startPos);

				// causes block to slide across ice, TODO implement sliding block state (ice on bottom of block?) as well as noise when it comes to stop
				EnumFacing facing = player.getHorizontalFacing();
				if (world.getBlockState(posMoveToHere.down()).getBlock() == Blocks.ICE &&
						world.getBlockState(posMoveToHere.offset(facing).down()).getBlock() == Blocks.ICE &&
						world.isAirBlock(posMoveToHere.offset(facing))) {
				currentlySlidingBlocks.add(new SlidingEventData(world, posMoveToHere, player.getHorizontalFacing(),hit,startPos));
				}
			}
			setActivated(world,player, posMoveToHere);
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

		if (downBlock instanceof Matchable) {
			if (this.matches((Matchable) downBlock)) {
				world.setBlockState(pos, this.getDefaultState().withProperty(activated, true), 3);
				Utils.spawnParticle(player, EnumParticleTypes.CRIT_MAGIC, pos);
				world.notifyNeighborsOfStateChange(pos.down(), this, true);
			} else {
				world.setBlockState(pos, this.getDefaultState().withProperty(activated, false), 3);
				world.notifyNeighborsOfStateChange(pos.down(), this, true);
			}
		}

	}

	private void setActivated(World world, BlockPos pos) {
		Block downBlock = world.getBlockState(pos.down()).getBlock();

		if (downBlock instanceof Matchable) {
			if (this.matches((Matchable) downBlock)) {
				world.setBlockState(pos, this.getDefaultState().withProperty(activated, true), 3);
				//Utils.spawnParticle(player, EnumParticleTypes.CRIT_MAGIC, pos);
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
			ResetDataTileEntity tile = getTileEntity(world, pos);
			BlockPos startPos = tile.getStartPos();
			if (startPos != null) { // perhaps set startPos to 0,0,0 as default?
				// System.out.println("x: " + pos.getX() + "," + "y: " + pos.getY() + "," + "z: " + pos.getZ());
				// System.out.println("x: " + startPos.getX() + "," + "y: " + startPos.getY() + "," + "z: " + startPos.getZ());
				world.setBlockState(pos, Blocks.AIR.getDefaultState());
				world.setBlockState(startPos, this.getDefaultState());
			}
		}
	}
}