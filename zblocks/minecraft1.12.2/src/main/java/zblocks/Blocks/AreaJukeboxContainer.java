package zblocks.Blocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import zblocks.Blocks.Slots.AreaJukeboxSlot;
import zblocks.TileEntities.AreaJukeboxTileEntity;

public class AreaJukeboxContainer extends Container {

	private final AreaJukeboxTileEntity tileEntity;

	public AreaJukeboxContainer(InventoryPlayer player, AreaJukeboxTileEntity areajukeboxTE) {
		this.tileEntity = areajukeboxTE;
		this.addSlotToContainer(new AreaJukeboxSlot(areajukeboxTE, 0, 79, 28));
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				this.addSlotToContainer(new Slot(player, (y * 9) + x + 9, (x * 18) + 8, (y * 18) + 84));
			}
		}

		for (int x = 0; x < 9; x++) {
			this.addSlotToContainer(new Slot(player, x, (x * 18) + 8, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return this.tileEntity.isUsableByPlayer(playerIn);
	}

}
