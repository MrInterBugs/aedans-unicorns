package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import uk.mrinterbugs.unicorn.UnicornHornHolder;
import uk.mrinterbugs.unicorn.UnicornMod;

@Mixin(AbstractDonkeyEntity.class)
public abstract class AbstractDonkeyEntityMixin {
    /** Recover the extra inventory slot saved by earlier 1.21.1 releases. */
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void unicorn$migrateLegacyHorn(NbtCompound nbt, CallbackInfo ci) {
        AbstractDonkeyEntity self = (AbstractDonkeyEntity) (Object) this;
        if (!self.hasChest() || nbt.contains("UnicornHornItem", NbtElement.COMPOUND_TYPE)) return;
        int legacySlot = ((AbstractHorseInventoryAccessor) self).getInventory().size();
        var entries = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < entries.size(); i++) {
            NbtCompound entry = entries.getCompound(i);
            if ((entry.getByte("Slot") & 255) != legacySlot) continue;
            ItemStack.fromNbt(self.getRegistryManager(), entry)
                    .filter(stack -> stack.isOf(UnicornMod.UNICORN_HORN))
                    .ifPresent(((UnicornHornHolder) self)::unicorn$setHornStack);
        }
    }
}
