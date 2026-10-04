package io.github.rbroderi.zblocks.internal;
import org.bukkit.inventory.ItemStack;
public final class ItemModels{private ItemModels(){} public static ItemStack apply(ItemStack old,ModelKey key){var item=NexoIntegration.item(key);item.setAmount(old.getAmount());return item;}}
