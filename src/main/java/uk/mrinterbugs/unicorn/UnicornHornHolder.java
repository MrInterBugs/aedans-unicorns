package uk.mrinterbugs.unicorn;

import net.minecraft.item.ItemStack;

/**
 * Accessors for entities that can hold a unicorn horn item.
 */
public interface UnicornHornHolder {
    /**
     * Retrieves the held unicorn horn stack.
     */
    ItemStack unicorn$getHornStack();

    /**
     * Sets the held unicorn horn stack.
     */
    void unicorn$setHornStack(ItemStack stack);
}
