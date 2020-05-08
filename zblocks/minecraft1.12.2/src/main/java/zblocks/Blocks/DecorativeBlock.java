package zblocks.Blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class DecorativeBlock extends Block {

	public DecorativeBlock(String name, Material mat) {
		super(mat);
		setUnlocalizedName(name);
		setRegistryName(name);
	}
}
