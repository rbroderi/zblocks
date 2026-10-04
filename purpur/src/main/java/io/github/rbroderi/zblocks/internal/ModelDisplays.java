package io.github.rbroderi.zblocks.internal;
import org.bukkit.*;import org.bukkit.entity.ItemDisplay;import org.bukkit.inventory.ItemStack;import com.nexomc.nexo.api.*;
public final class ModelDisplays{private ModelDisplays(){} public static ItemDisplay spawn(Location l,ItemStack item,NamespacedKey tag,boolean visible){var d=NexoIntegration.createDisplay(NexoItems.idFromItem(item),l);if(d==null)throw new IllegalStateException("Nexo rejected display");d.setPersistent(false);d.setVisibleByDefault(visible);return d;}}
