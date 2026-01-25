package uk.mrinterbugs.unicorn;

import java.util.Map;
import java.util.Set;
import lombok.experimental.UtilityClass;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * Helpers and constants for unicorn potion items and contents.
 */
@UtilityClass
public class UnicornPotions {
    public static final Map<Item, Item> UNICORN_CONTAINER_SWAP = Map.of(
            Items.POTION, UnicornMod.UNICORN_HEART_POTION_ITEM,
            Items.SPLASH_POTION, UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM,
            Items.LINGERING_POTION, UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM,
            Items.TIPPED_ARROW, UnicornMod.UNICORN_HEART_TIPPED_ARROW);
    public static final Set<Item> UNICORN_POTION_ITEMS = Set.copyOf(UNICORN_CONTAINER_SWAP.values());

    /**
     * Builds a potion stack with unicorn heart contents for the given container
     * item.
     */
    public ItemStack createUnicornPotionStack(Item baseItem) {
        return PotionContentsComponent.createStack(baseItem, UnicornMod.UNICORN_HEART_POTION_ENTRY);
    }

    /**
     * Builds a potion stack with the given potion entry for the given container
     * item.
     */
    public ItemStack createPotionStack(Item baseItem, RegistryEntry<Potion> potion) {
        return PotionContentsComponent.createStack(baseItem, potion);
    }

    /**
     * Determines whether a stack carries the unicorn heart potion contents.
     */
    public boolean matchesUnicornPotion(ItemStack stack) {
        PotionContentsComponent contents = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS,
                PotionContentsComponent.DEFAULT);
        return contents.matches(UnicornMod.UNICORN_HEART_POTION_ENTRY);
    }

    /**
     * Checks if an item is one of the unicorn potion containers.
     */
    public boolean isUnicornPotionItem(Item item) {
        return UNICORN_POTION_ITEMS.contains(item);
    }
}
