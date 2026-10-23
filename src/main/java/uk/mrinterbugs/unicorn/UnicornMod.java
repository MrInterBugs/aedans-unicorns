package uk.mrinterbugs.unicorn;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.alchemy.Potion;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnicornMod implements ModInitializer {
    public static final String MOD_ID = "unicorn";

    private static final int HORN_MAX_COUNT = 1;
    private static final int POTION_MAX_COUNT = 1;
    private static final int POTION_DURATION_TICKS = 2400;
    private static final int POTION_AMPLIFIER = 1;

    private static final ResourceKey<CreativeModeTab> COMBAT_TAB =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("combat"));
    private static final ResourceKey<CreativeModeTab> FOOD_AND_DRINK_TAB =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("food_and_drink"));

    public static final Item UNICORN_HORN = registerItem("unicorn_horn",
            props -> new Item(props.stacksTo(HORN_MAX_COUNT)));
    public static final Item UNICORN_HEART_POTION_ITEM = registerItem("unicorn_heart_potion_item",
            props -> new PotionItem(props.stacksTo(POTION_MAX_COUNT)));
    public static final Item UNICORN_HEART_SPLASH_POTION_ITEM = registerItem("unicorn_heart_splash_potion_item",
            props -> new SplashPotionItem(props.stacksTo(POTION_MAX_COUNT)));
    public static final Item UNICORN_HEART_LINGERING_POTION_ITEM = registerItem("unicorn_heart_lingering_potion_item",
            props -> new LingeringPotionItem(props.stacksTo(POTION_MAX_COUNT)));
    public static final Item UNICORN_HEART_TIPPED_ARROW = registerItem("unicorn_heart_tipped_arrow",
            props -> new TippedArrowItem(props));

    public static final Potion UNICORN_HEART_POTION = registerPotion(
            "unicorn_heart_potion",
            new Potion("unicorn_heart_potion", new MobEffectInstance(MobEffects.ABSORPTION, POTION_DURATION_TICKS, POTION_AMPLIFIER)));
    public static final Holder<Potion> UNICORN_HEART_POTION_ENTRY = BuiltInRegistries.POTION
            .wrapAsHolder(UNICORN_HEART_POTION);

    private static Item registerItem(String name, Function<Item.Properties, Item> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Item item = factory.apply(new Item.Properties().setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    private static Potion registerPotion(String name, Potion potion) {
        return Registry.register(BuiltInRegistries.POTION, Identifier.fromNamespaceAndPath(MOD_ID, name), potion);
    }

    @Override
    public void onInitialize() {
        CreativeModeTabEvents.modifyOutputEvent(COMBAT_TAB).register(entries -> {
            entries.getDisplayStacks().removeIf(UnicornMod::isVanillaUnicornPotionStack);
            entries.accept(new ItemStack(UNICORN_HORN), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            entries.accept(UnicornPotions.createUnicornPotionStack(UNICORN_HEART_TIPPED_ARROW),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        });
        CreativeModeTabEvents.modifyOutputEvent(FOOD_AND_DRINK_TAB).register(entries -> {
            entries.getDisplayStacks().removeIf(UnicornMod::isVanillaUnicornPotionStack);
            entries.accept(UnicornPotions.createUnicornPotionStack(UNICORN_HEART_POTION_ITEM),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            entries.accept(UnicornPotions.createUnicornPotionStack(UNICORN_HEART_SPLASH_POTION_ITEM),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            entries.accept(UnicornPotions.createUnicornPotionStack(UNICORN_HEART_LINGERING_POTION_ITEM),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        });

        UnicornLootTables.register();
        log.info("Unicorn mod loaded. Registered item: {} and potion: {}",
                BuiltInRegistries.ITEM.getKey(UNICORN_HORN),
                BuiltInRegistries.POTION.getKey(UNICORN_HEART_POTION));
    }

    private static boolean isVanillaUnicornPotionStack(ItemStack stack) {
        if (!UnicornPotions.matchesUnicornPotion(stack)) {
            return false;
        }
        Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return Identifier.DEFAULT_NAMESPACE.equals(key.getNamespace());
    }
}
