package zblocks.init;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zblocks.Reference;
import zblocks.Blocks.DepressPuzzleBlock;
import zblocks.Blocks.PushPuzzleBlock;
import zblocks.Blocks.PushPuzzleBlock.ColorEnum;

@Mod.EventBusSubscriber(modid=Reference.MODID)
public class ModBlocks {

	static List<Block> blockList = new ArrayList<Block>();
	static List<Item> itemList = new ArrayList<Item>();
	
	public static void init() {
		for(ColorEnum c: ColorEnum.values()) {
		blockList.add(new PushPuzzleBlock("push_block"+(c!=ColorEnum.BASE?"_"+c.getName():""), Material.ROCK,c).setHardness(100f).setCreativeTab(CreativeTabs.REDSTONE).setLightLevel(2.0f/15f));
		}
		//blockList.add(new PushPuzzleBlock("push_block_blue", Material.ROCK,ColorEnum.BLUE).setHardness(100f).setCreativeTab(CreativeTabs.REDSTONE).setLightLevel(2.0f/15f));
		blockList.add(new DepressPuzzleBlock("depress_block", Material.ROCK).setHardness(100f).setCreativeTab(CreativeTabs.REDSTONE).setLightLevel(2.0f/15f));
	}	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		  event.getRegistry().registerAll(blockList.toArray(new Block[0]));
	}
	
	@SubscribeEvent
	public static void registerItemBlocks(RegistryEvent.Register<Item> event) { 
		for(Block block: blockList) {
			itemList.add(new ItemBlock(block).setRegistryName(block.getRegistryName()));
		}
     event.getRegistry().registerAll(itemList.toArray(new Item[0]));
	}
	
	@SubscribeEvent
	public static void registerRenders(ModelRegistryEvent event) {
		for(Item item: itemList) {
			 registerRender(item);
		}
	}
	
	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event){
		final SoundEvent[] soundEvents = {
				new SoundEvent(new ResourceLocation(Reference.MODID, "thud_delay")).setRegistryName("thud_delay"),
				new SoundEvent(new ResourceLocation(Reference.MODID, "scrape")).setRegistryName("scrape")
			};
			event.getRegistry().registerAll(soundEvents);
	}
	
	public static void registerRender(Item item) {
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation( item.getRegistryName(), "inventory"));
	}
}