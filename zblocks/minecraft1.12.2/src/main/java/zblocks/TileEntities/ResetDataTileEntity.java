package zblocks.TileEntities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

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
}