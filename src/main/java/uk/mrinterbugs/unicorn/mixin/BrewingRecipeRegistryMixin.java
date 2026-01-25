package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.mrinterbugs.unicorn.UnicornMod;
import uk.mrinterbugs.unicorn.UnicornPotions;

/**
 * Registers unicorn brewing recipes and swaps vanilla potion containers with
 * custom unicorn variants.
 */
@Mixin(BrewingRecipeRegistry.class)
public class BrewingRecipeRegistryMixin {
    /**
     * Adds unicorn heart potion recipes and container types to the default brewing
     * registry.
     */
    @Inject(method = "registerDefaults", at = @At("TAIL"))
    private static void unicorn$addUnicornBrew(BrewingRecipeRegistry.Builder builder, CallbackInfo ci) {
        builder.registerPotionRecipe(Potions.AWKWARD, UnicornMod.UNICORN_HORN, UnicornMod.UNICORN_HEART_POTION_ENTRY);
        builder.registerPotionType(UnicornMod.UNICORN_HEART_POTION_ITEM);
        builder.registerPotionType(UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM);
        builder.registerPotionType(UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM);
        builder.registerItemRecipe(UnicornMod.UNICORN_HEART_POTION_ITEM, Items.GUNPOWDER,
                UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM);
        builder.registerItemRecipe(UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM, Items.DRAGON_BREATH,
                UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM);
    }

    /**
     * Replaces the vanilla potion container in the brewing output with the matching
     * unicorn item variant.
     */
    @Inject(method = "craft", at = @At("RETURN"), cancellable = true)
    private void unicorn$swapPotionContainer(ItemStack ingredient, ItemStack input,
            CallbackInfoReturnable<ItemStack> cir) {
        ItemStack output = cir.getReturnValue();
        if (output.isEmpty()) {
            return;
        }

        if (!UnicornPotions.matchesUnicornPotion(output)) {
            return;
        }

        Item currentItem = output.getItem();
        Item targetItem = UnicornPotions.UNICORN_CONTAINER_SWAP.get(currentItem);

        if (targetItem != null && targetItem != currentItem) {
            ItemStack swapped = UnicornPotions.createUnicornPotionStack(targetItem);
            swapped.applyComponentsFrom(output.getComponents());
            swapped.setCount(output.getCount());
            cir.setReturnValue(swapped);
        }
    }
}
