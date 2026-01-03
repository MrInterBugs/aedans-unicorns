package uk.mrinterbugs.unicorn;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnicornMod implements ModInitializer {
    public static final String MOD_ID = "unicorn";

    public static final Item UNICORN_HORN = registerItem("unicorn_horn", new Item(new Item.Settings().maxCount(1)));
    public static final Item UNICORN_HEART_POTION_ITEM = registerItem("unicorn_heart_potion_item", new PotionItem(new Item.Settings().maxCount(1)));
    public static final Item UNICORN_HEART_SPLASH_POTION_ITEM = registerItem("unicorn_heart_splash_potion_item", new SplashPotionItem(new Item.Settings().maxCount(1)));
    public static final Item UNICORN_HEART_LINGERING_POTION_ITEM = registerItem("unicorn_heart_lingering_potion_item", new LingeringPotionItem(new Item.Settings().maxCount(1)));
    public static final Item UNICORN_HEART_TIPPED_ARROW = registerItem("unicorn_heart_tipped_arrow", new TippedArrowItem(new Item.Settings()));
    public static final Potion UNICORN_HEART_POTION = registerPotion(
            "unicorn_heart_potion",
            new Potion(new StatusEffectInstance(StatusEffects.ABSORPTION, 2400, 1))
    );
    public static final RegistryEntry<Potion> UNICORN_HEART_POTION_ENTRY = Registries.POTION.getEntry(UNICORN_HEART_POTION);

    /**
     * Registers an item under the mod namespace.
     */
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name), item);
    }

    /**
     * Registers a potion under the mod namespace.
     */
    private static Potion registerPotion(String name, Potion potion) {
        return Registry.register(Registries.POTION, Identifier.of(MOD_ID, name), potion);
    }

    /**
     * Registers items, potions, and item group entries during mod initialization.
     */
    @Override
    public void onInitialize() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.registerPotionRecipe(Potions.AWKWARD, UnicornMod.UNICORN_HORN, UnicornMod.UNICORN_HEART_POTION_ENTRY);
            builder.registerPotionType(UNICORN_HEART_POTION_ITEM);
            builder.registerPotionType(UNICORN_HEART_SPLASH_POTION_ITEM);
            builder.registerPotionType(UNICORN_HEART_LINGERING_POTION_ITEM);
            builder.registerItemRecipe(UNICORN_HEART_POTION_ITEM, Items.GUNPOWDER, UNICORN_HEART_SPLASH_POTION_ITEM);
            builder.registerItemRecipe(UNICORN_HEART_SPLASH_POTION_ITEM, Items.DRAGON_BREATH, UNICORN_HEART_LINGERING_POTION_ITEM);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.getDisplayStacks().removeIf(UnicornMod::isVanillaUnicornPotionStack);
            entries.add(UNICORN_HORN);
            entries.add(UnicornPotions.createUnicornPotionStack(UNICORN_HEART_TIPPED_ARROW));
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.getDisplayStacks().removeIf(UnicornMod::isVanillaUnicornPotionStack);
            entries.add(UnicornPotions.createUnicornPotionStack(UNICORN_HEART_POTION_ITEM));
            entries.add(UnicornPotions.createUnicornPotionStack(UNICORN_HEART_SPLASH_POTION_ITEM));
            entries.add(UnicornPotions.createUnicornPotionStack(UNICORN_HEART_LINGERING_POTION_ITEM));
        });

        UnicornLootTables.register();
        log.info("Unicorn mod loaded. Registered item: {} and potion: {}", Registries.ITEM.getId(UNICORN_HORN), Registries.POTION.getId(UNICORN_HEART_POTION));
    }

    /**
     * Filters out vanilla-generated unicorn potion or tipped arrow stacks so only custom containers remain visible.
     */
    private static boolean isVanillaUnicornPotionStack(ItemStack stack) {
        if (!UnicornPotions.matchesUnicornPotion(stack)) {
            return false;
        }

        return Identifier.DEFAULT_NAMESPACE.equals(Registries.ITEM.getId(stack.getItem()).getNamespace());
    }
}
