package bm.b0b0b0.soulBuyer.model;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public record SellLimitSplit(List<ItemStack> sellStacks, List<ItemStack> returnStacks) {
}
