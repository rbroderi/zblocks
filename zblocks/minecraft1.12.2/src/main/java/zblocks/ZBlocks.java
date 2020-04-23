package zblocks;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import zblocks.Blocks.PushPuzzleBlock;
import zblocks.init.ModBlocks;

@Mod(modid=Reference.MODID, name=Reference.MODNAME, version=Reference.VERSION, acceptedMinecraftVersions=Reference.ACCEPTED_MINECRAFT_VERSIONS)
public class ZBlocks {
	@Instance
	public static ZBlocks instance;
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ModBlocks.init();
		//event.getModLog();
		//LogManager.getLogger();
		//System.out.println(Reference.MODID + ":preInit");
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		//System.out.println(Reference.MODID + ":init");
		MinecraftForge.EVENT_BUS.register(new PushPuzzleBlock());
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		//System.out.println(Reference.MODID + ":postInit");
	}
}