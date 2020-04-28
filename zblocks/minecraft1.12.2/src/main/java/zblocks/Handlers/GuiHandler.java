package zblocks.Handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import zblocks.Reference;
import zblocks.Blocks.AreaJukeboxContainer;
import zblocks.Blocks.AreaJukeboxGui;
import zblocks.TileEntities.AreaJukeboxTileEntity;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == Reference.AREA_JUKEBOX_GUI && world.getTileEntity(new BlockPos(x, y, z)) instanceof AreaJukeboxTileEntity) {
			return new AreaJukeboxContainer(player.inventory, (AreaJukeboxTileEntity) world.getTileEntity(new BlockPos(x, y, z)));
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == Reference.AREA_JUKEBOX_GUI && world.getTileEntity(new BlockPos(x, y, z)) instanceof AreaJukeboxTileEntity) {
			return new AreaJukeboxGui(player.inventory, (AreaJukeboxTileEntity) world.getTileEntity(new BlockPos(x, y, z)));
		}
		return null;
	}

}
