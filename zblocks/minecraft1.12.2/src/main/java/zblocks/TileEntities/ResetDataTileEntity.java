package zblocks.TileEntities;

import java.util.HashSet;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import zblocks.Blocks.PushPuzzleBlock;
import zblocks.Blocks.Interfaces.Colored.ColorEnum;

public class ResetDataTileEntity extends TileEntity {

	private BlockPos startPos;
	private static final HashSet<IBlockState> allowed;
	static {
		PushPuzzleBlock temp = new PushPuzzleBlock("temp", Material.ROCK, ColorEnum.BASE);
		allowed = new HashSet<IBlockState>();
		allowed.add(temp.getStateFromMeta(PushPuzzleBlock.iDISABLED));
		allowed.add(temp.getStateFromMeta(PushPuzzleBlock.iFROZEN));
		allowed.add(temp.getStateFromMeta(PushPuzzleBlock.iACTIVATED));
		allowed.add(temp.getStateFromMeta(PushPuzzleBlock.iACTIVATED + PushPuzzleBlock.iFROZEN));
	}

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
		if (world.getBlockState(pos).getBlock() instanceof PushPuzzleBlock && allowed.contains(oldState) && allowed.contains(newState)) {
			return false;
		}
		return true;

	}

}