package uk.mrinterbugs.unicorn;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public interface UnicornHornHolder {
    Container unicorn$getHornInventory();

    ItemStack unicorn$getHornStack();

    void unicorn$setHornStack(ItemStack stack);
}
