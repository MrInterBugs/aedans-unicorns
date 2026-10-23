package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import uk.mrinterbugs.unicorn.UnicornHornHolder;
import uk.mrinterbugs.unicorn.UnicornMod;

@Mixin(AbstractChestedHorse.class)
public abstract class AbstractChestedHorseMixin {
    /** Read the extra cargo slot written by the earlier 26.1.2 implementation once. */
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void unicorn$migrateLegacyHorn(ValueInput input, CallbackInfo ci) {
        AbstractChestedHorse self = (AbstractChestedHorse) (Object) this;
        if (!self.hasChest() || input.read("UnicornHornItem", ItemStack.OPTIONAL_CODEC).isPresent())
            return;
        int legacySlot = ((AbstractHorseInventoryAccessor) self).getInventory().getContainerSize();
        for (ItemStackWithSlot entry : input.listOrEmpty("Items", ItemStackWithSlot.CODEC)) {
            if (entry.slot() == legacySlot && entry.stack().is(UnicornMod.UNICORN_HORN)) {
                ((UnicornHornHolder) self).unicorn$setHornStack(entry.stack());
                break;
            }
        }
    }
}
