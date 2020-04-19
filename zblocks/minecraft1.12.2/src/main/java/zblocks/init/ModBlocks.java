package zblocks.init;

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
import zblocks.Blocks.PushPuzzleBlock;

@Mod.EventBusSubscriber(modid=Reference.MODID)
public class ModBlocks {

	static Block PushPuzzleBlock;
	
	public static void init() {
		  PushPuzzleBlock = new PushPuzzleBlock("pblock", Material.ROCK).setHardness(100f).setCreativeTab(CreativeTabs.REDSTONE).setLightLevel(2.0f/15f);
	}	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		  event.getRegistry().registerAll(PushPuzzleBlock);
	}
	
	@SubscribeEvent
	public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
     event.getRegistry().registerAll(new ItemBlock(PushPuzzleBlock).setRegistryName(PushPuzzleBlock.getRegistryName()));
	}
	
	@SubscribeEvent
	public static void registerRenders(ModelRegistryEvent event) {
		 registerRender(Item.getItemFromBlock(PushPuzzleBlock));
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