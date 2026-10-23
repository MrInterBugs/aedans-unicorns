package uk.mrinterbugs.unicorn;

import java.util.Map;
import java.util.Set;
import lombok.experimental.UtilityClass;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

@UtilityClass
public class UnicornPotions {
    public static final Map<Item, Item> UNICORN_CONTAINER_SWAP = Map.of(
            Items.POTION, UnicornMod.UNICORN_HEART_POTION_ITEM,
            Items.SPLASH_POTION, UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM,
            Items.LINGERING_POTION, UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM,
            Items.TIPPED_ARROW, UnicornMod.UNICORN_HEART_TIPPED_ARROW);
    public static final Set<Item> UNICORN_POTION_ITEMS = Set.copyOf(UNICORN_CONTAINER_SWAP.values());

    public ItemStack createUnicornPotionStack(Item baseItem) {
        return PotionContents.createItemStack(baseItem, UnicornMod.UNICORN_HEART_POTION_ENTRY);
    }

    public ItemStack createPotionStack(Item baseItem, Holder<Potion> potion) {
        return PotionContents.createItemStack(baseItem, potion);
    }

    public boolean matchesUnicornPotion(ItemStack stack) {
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.is(UnicornMod.UNICORN_HEART_POTION_ENTRY);
    }

    public boolean isUnicornPotionItem(Item item) {
        return UNICORN_POTION_ITEMS.contains(item);
    }
}
