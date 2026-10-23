package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import uk.mrinterbugs.unicorn.UnicornHornHolder;

/** Keep the horn separate from saddle, armor, and resizable chest cargo. */
@Mixin(AbstractHorseEntity.class)
public abstract class AbstractHorseEntityMixin implements UnicornHornHolder {
    @Unique
    private static final TrackedData<ItemStack> unicorn$HORN_TRACKER =
            DataTracker.registerData(
                    AbstractHorseEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    @Unique private SimpleInventory unicorn$hornInventory;

    @Override
    public SimpleInventory unicorn$getHornInventory() {
        if (unicorn$hornInventory == null) {
            unicorn$hornInventory = new SimpleInventory(1);
            unicorn$hornInventory.addListener(
                    inventory -> {
                        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
                        if (!self.getWorld().isClient())
                            self.getDataTracker()
                                    .set(unicorn$HORN_TRACKER, inventory.getStack(0).copy());
                    });
        }
        return unicorn$hornInventory;
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void unicorn$trackHornData(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(unicorn$HORN_TRACKER, ItemStack.EMPTY);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void unicorn$saveHorn(NbtCompound nbt, CallbackInfo ci) {
        ItemStack horn = unicorn$getHornStack();
        if (!horn.isEmpty()) {
            AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
            nbt.put("UnicornHornItem", horn.encode(self.getRegistryManager()));
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void unicorn$loadHorn(NbtCompound nbt, CallbackInfo ci) {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        unicorn$setHornStack(
                ItemStack.fromNbt(self.getRegistryManager(), nbt.getCompound("UnicornHornItem"))
                        .orElse(ItemStack.EMPTY));
    }

    @Inject(method = "dropInventory", at = @At("TAIL"))
    private void unicorn$dropHorn(CallbackInfo ci) {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        ItemStack horn = unicorn$getHornInventory().removeStack(0);
        if (!horn.isEmpty()) self.dropStack(horn);
    }

    @Override
    public ItemStack unicorn$getHornStack() {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        return self.getWorld().isClient()
                ? self.getDataTracker().get(unicorn$HORN_TRACKER)
                : unicorn$getHornInventory().getStack(0);
    }

    @Override
    public void unicorn$setHornStack(ItemStack stack) {
        unicorn$getHornInventory().setStack(0, stack.copy());
    }
}
