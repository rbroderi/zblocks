package zblocks.Blocks;

import net.minecraft.block.material.Material;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SecretTransientPuzzleBlock extends TransientPuzzleBlock {

	public SecretTransientPuzzleBlock(String name, Material material) {
		super(name,material);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
}