package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.mrinterbugs.unicorn.UnicornPotions;

/**
 * Swaps vanilla potion containers with custom unicorn variants during brewing.
 */
@Mixin(BrewingRecipeRegistry.class)
public class BrewingRecipeRegistryMixin {
    /**
     * Replaces the vanilla potion container in the brewing output with the matching unicorn item variant.
     */
    @Inject(method = "craft", at = @At("RETURN"), cancellable = true)
    private void unicorn$swapPotionContainer(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
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
