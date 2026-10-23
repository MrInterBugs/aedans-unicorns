package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import uk.mrinterbugs.unicorn.UnicornMod;
import uk.mrinterbugs.unicorn.UnicornPotions;

@Mixin(PotionBrewing.class)
public class BrewingRecipeRegistryMixin {
    @Inject(method = "addVanillaMixes", at = @At("TAIL"))
    private static void unicorn$addUnicornBrew(PotionBrewing.Builder builder, CallbackInfo ci) {
        builder.addMix(
                net.minecraft.world.item.alchemy.Potions.AWKWARD,
                UnicornMod.UNICORN_HORN,
                UnicornMod.UNICORN_HEART_POTION_ENTRY);
        builder.addContainer(UnicornMod.UNICORN_HEART_POTION_ITEM);
        builder.addContainer(UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM);
        builder.addContainer(UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM);
        builder.addContainerRecipe(
                UnicornMod.UNICORN_HEART_POTION_ITEM,
                Items.GUNPOWDER,
                UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM);
        builder.addContainerRecipe(
                UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM,
                Items.DRAGON_BREATH,
                UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM);
    }

    @Inject(
            method = {"hasMix", "hasContainerMix", "hasPotionMix"},
            at = @At("HEAD"),
            cancellable = true)
    private void unicorn$restrictBrewing(
            ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (!UnicornPotions.allowsBrewing(input, ingredient)) cir.setReturnValue(false);
    }

    @Inject(method = "mix", at = @At("HEAD"), cancellable = true)
    private void unicorn$rejectInvalidMix(
            ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        if (!UnicornPotions.allowsBrewing(input, ingredient)) cir.setReturnValue(input);
    }

    @Inject(method = "mix", at = @At("RETURN"), cancellable = true)
    private void unicorn$swapPotionContainer(
            ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack output = cir.getReturnValue();
        if (output.isEmpty() || ItemStack.matches(output, input)) {
            return;
        }

        if (!UnicornPotions.matchesUnicornPotion(output)) {
            return;
        }

        Item currentItem = output.getItem();
        Item targetItem = UnicornPotions.UNICORN_CONTAINER_SWAP.get(currentItem);

        if (targetItem != null && targetItem != currentItem) {
            ItemStack swapped =
                    new ItemStack(
                            net.minecraft.core.registries.BuiltInRegistries.ITEM.wrapAsHolder(
                                    targetItem),
                            output.getCount(),
                            output.getComponentsPatch());
            cir.setReturnValue(swapped);
        }
    }
}
