package zblocks.TileEntities;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import zblocks.Blocks.Interfaces.Resettable;

public class ResetDataTileEntity extends TileEntity {

	private BlockPos startPos;

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		// compound.setInteger("count", count);
		if (startPos != null) {
			compound.merge(NBTUtil.createPosTag(startPos));
		}
		return super.writeToNBT(compound);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		startPos = NBTUtil.getPosFromTag(compound);
		super.readFromNBT(compound);
	}

	public BlockPos getStartPos() {
		return startPos;
	}

	public void setStartPos(BlockPos pos) {
		this.startPos = pos;
		markDirty();
	}

	public String getStartPosAsString() {
		return startPos.getX() + "," + startPos.getY() + "," + startPos.getZ();
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		if (world.getBlockState(pos).getBlock() instanceof Resettable && (oldState != Blocks.AIR.getDefaultState() || newState != Blocks.AIR.getDefaultState())) {
			return false;
		}
		return true;

	}

}