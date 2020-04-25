package zblocks.Blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import zblocks.Blocks.Interfaces.Colored;

public class StartPuzzleBlock extends Block implements Colored {

	private ColorEnum color;
	public StartPuzzleBlock(String name,Material material, ColorEnum color) {
		super(material);
		setUnlocalizedName(color == ColorEnum.BASE ? name : name + "_" + color.getName());
		setRegistryName(color == ColorEnum.BASE ? name : name + "_" + color.getName());
		this.color = color;
	}

	@Override
	public ColorEnum getColor() {
	return this.color;
	}

}