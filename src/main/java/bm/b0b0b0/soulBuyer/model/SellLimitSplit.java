package bm.b0b0b0.soulBuyer.model;

import java.util.List;
import org.bukkit.inventory.ItemStack;

public record SellLimitSplit(List<ItemStack> sellStacks, List<ItemStack> returnStacks) {
}
