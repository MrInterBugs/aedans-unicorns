package uk.mrinterbugs.unicorn.client;

import dev.architectury.event.EventResult;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.visibility.DisplayVisibilityPredicate;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomShapedDisplay;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import uk.mrinterbugs.unicorn.UnicornMod;
import uk.mrinterbugs.unicorn.UnicornPotions;

/**
 * Hides vanilla brewing permutations that REI generates for unicorn potion containers.
 * Our items are only craftable via the custom recipes, not through potion mixing.
 */
public class UnicornReiPlugin implements REIClientPlugin {
    private static final CategoryIdentifier<?> BREWING = BuiltinPlugin.BREWING;

    /**
     * Registers visibility predicates and custom displays for unicorn brewing.
     */
    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerVisibilityPredicate(hideUnicornBrewing());
        registerCustomDisplays(registry);
    }

    /**
     * Hides default brewing displays that use unicorn potion contents to avoid misleading recipes.
     */
    private DisplayVisibilityPredicate hideUnicornBrewing() {
        return (category, display) -> {
            if (!BREWING.equals(category.getCategoryIdentifier())) {
                return EventResult.pass();
            }
            if (display instanceof UnicornBrewingDisplay) {
                return EventResult.pass();
            }
            if (hasUnicornPotionStack(display) || hasUnicornPotionContents(display.getOutputEntries())) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        };
    }

    /**
     * Checks whether any input or output entries include unicorn potion containers.
     */
    private boolean hasUnicornPotionStack(Display display) {
        return hasUnicornPotionStack(display.getInputEntries()) || hasUnicornPotionStack(display.getOutputEntries());
    }

    /**
     * Checks a list of entry ingredients for unicorn potion container stacks.
     */
    private boolean hasUnicornPotionStack(Iterable<EntryIngredient> ingredients) {
        for (EntryIngredient ingredient : ingredients) {
            for (EntryStack<?> stack : ingredient) {
                Object value = stack.getValue();
                if (value instanceof ItemStack itemStack && UnicornPotions.isUnicornPotionItem(itemStack.getItem())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Registers custom REI displays for unicorn brewing steps and the tipped arrow recipe.
     */
    private void registerCustomDisplays(DisplayRegistry registry) {
        registry.add(new UnicornBrewingDisplay(
                EntryIngredients.ofItemStacks(java.util.List.of(UnicornPotions.createUnicornPotionStack(Items.POTION))),
                EntryIngredients.of(UnicornMod.UNICORN_HORN),
                EntryStacks.of(UnicornPotions.createUnicornPotionStack(UnicornMod.UNICORN_HEART_POTION_ITEM))
        ));

        registry.add(new UnicornBrewingDisplay(
                EntryIngredients.ofItemStacks(java.util.List.of(UnicornPotions.createUnicornPotionStack(UnicornMod.UNICORN_HEART_POTION_ITEM))),
                EntryIngredients.of(Items.GUNPOWDER),
                EntryStacks.of(UnicornPotions.createUnicornPotionStack(UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM))
        ));

        registry.add(new UnicornBrewingDisplay(
                EntryIngredients.ofItemStacks(java.util.List.of(UnicornPotions.createUnicornPotionStack(UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM))),
                EntryIngredients.of(Items.DRAGON_BREATH),
                EntryStacks.of(UnicornPotions.createUnicornPotionStack(UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM))
        ));

        registry.add(new UnicornBrewingDisplay(
                EntryIngredients.ofItemStacks(java.util.List.of(UnicornPotions.createUnicornPotionStack(Items.LINGERING_POTION))),
                EntryIngredients.of(UnicornMod.UNICORN_HORN),
                EntryStacks.of(UnicornPotions.createUnicornPotionStack(UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM))
        ));

        registry.add(DefaultCustomShapedDisplay.simple(
                buildArrowInputs(),
                java.util.List.of(EntryIngredients.ofItemStacks(java.util.List.of(UnicornPotions.createUnicornPotionStack(UnicornMod.UNICORN_HEART_TIPPED_ARROW)))),
                3,
                3,
                java.util.Optional.of(Identifier.of(UnicornMod.MOD_ID, "unicorn_heart_tipped_arrow"))
        ));
    }

    /**
     * Builds the shaped crafting inputs for the tipped arrow recipe.
     */
    private java.util.List<EntryIngredient> buildArrowInputs() {
        java.util.List<EntryIngredient> inputs = new java.util.ArrayList<>(9);
        EntryIngredient arrow = EntryIngredients.of(Items.ARROW);
        for (int i = 0; i < 9; i++) {
            inputs.add(i == 4
                    ? EntryIngredients.ofItemStacks(java.util.List.of(UnicornPotions.createUnicornPotionStack(UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM)))
                    : arrow);
        }
        return inputs;
    }

    /**
     * Detects unicorn potion contents within entry ingredients regardless of container type.
     */
    private boolean hasUnicornPotionContents(Iterable<EntryIngredient> ingredients) {
        for (EntryIngredient ingredient : ingredients) {
            for (EntryStack<?> stack : ingredient) {
                Object value = stack.getValue();
                if (value instanceof ItemStack itemStack) {
                    if (UnicornPotions.matchesUnicornPotion(itemStack)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static class UnicornBrewingDisplay extends me.shedaniel.rei.plugin.common.displays.brewing.DefaultBrewingDisplay {
        /**
         * Constructs a brewing display for unicorn-specific recipes.
         */
        private UnicornBrewingDisplay(EntryIngredient input, EntryIngredient reactant, EntryStack<?> output) {
            super(input, reactant, output);
        }
    }
}
