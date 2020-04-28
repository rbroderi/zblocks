package zblocks.Blocks;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import zblocks.Reference;
import zblocks.TileEntities.AreaJukeboxTileEntity;

public class AreaJukeboxGui extends GuiContainer {

	private static final ResourceLocation TEXTURES = new ResourceLocation(Reference.MODID + ":textures/gui/area_jukebox.png");
	private final InventoryPlayer player;
	private final AreaJukeboxTileEntity tileEntity;
	private int DEFAULTTEXTCOLOR = 4210752;

	public AreaJukeboxGui(InventoryPlayer player, AreaJukeboxTileEntity tileEntity) {
		super(new AreaJukeboxContainer(player, tileEntity));
		this.player = player;
		this.tileEntity = tileEntity;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String tilename = this.tileEntity.getDisplayName().getUnformattedText();
		this.fontRenderer.drawString(tilename, this.getXSize() / 2 - this.fontRenderer.getStringWidth(tilename) / 2, 8, DEFAULTTEXTCOLOR);
		this.fontRenderer.drawString(player.getDisplayName().getUnformattedText(), 122, this.ySize - 96 + 2, DEFAULTTEXTCOLOR);
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0f, 1.0f, 1.0f);
		this.mc.getTextureManager().bindTexture(TEXTURES);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

	}

}
