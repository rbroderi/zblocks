package zblocks;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Reference.MODID)
public class ZblockConfig {

	@Config.Name("Enable Hourglass")
	@RequiresMcRestart
	public static boolean ENABLE_HOURGLASS = true;

	@Config.Name("Enable Transient Block")
	@RequiresMcRestart
	public static boolean ENABLE_TRANSIENT = true;

	@Config.Name("Enable Secret Transient Block")
	@RequiresMcRestart
	public static boolean ENABLE_SECRET_TRANSIENT = true;

	@Config.Name("Enable Activate Block")
	@RequiresMcRestart
	public static boolean ENABLE_ACTIVATE = true;

	@Config.Name("Bomb")
	@RequiresMcRestart
	public static boolean ENABLE_BOMB = true;

	@Config.Name("Enable Broken Wall Edging")
	@RequiresMcRestart
	public static boolean ENABLE_BROKEN_EDGING = true;

	@Config.Name("Enable Breakable Wall")
	@RequiresMcRestart
	public static boolean ENABLE_BREAKABLE_WALL = true;

	@Config.Name("Enable Subtle Breakable Wall")
	@RequiresMcRestart
	public static boolean ENABLE_BREAKABLE_WALL_SUBTLE = true;

	@Config.Name("Enable Secret Transient Block")
	@RequiresMcRestart
	public static boolean ENABLE_BREAKABLE_WALL_SECRET = true;

	@Config.Name("Enable Resettable Switches")
	@RequiresMcRestart
	public static boolean ENABLE_SWITCHES = true;

	@Config.Name("Enable Pushable Block")
	@RequiresMcRestart
	public static boolean ENABLE_PUSH = true;

	@Config.Name("Enable Start Block Marker")
	@RequiresMcRestart
	public static boolean ENABLE_START = true;

	@Config.Name("Enable Depressable Block")
	@RequiresMcRestart
	public static boolean ENABLE_DEPRESS = true;

	@Config.Name("Enable Color Varients")
	@RequiresMcRestart
	public static boolean ENABLE_COLORS = true;

	@Config.Name("Enable Decorative Cactus")
	@RequiresMcRestart
	public static boolean ENABLE_CACTUS = false;

	@Config.Name("Reset Range")
	@RequiresMcRestart
	public static int RESET_RANGE = 100;

	@Mod.EventBusSubscriber(modid = Reference.MODID)
	private static class EventHandler {

		/**
		 * Inject the new values and save to the config file when the config has been changed from the GUI.
		 *
		 * @param event The event
		 */
		@SubscribeEvent
		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
			if (event.getModID().equals(Reference.MODID)) {
				ConfigManager.sync(Reference.MODID, Config.Type.INSTANCE);
			}
		}
	}
}
