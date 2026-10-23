package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import uk.mrinterbugs.unicorn.UnicornHornHolder;
import uk.mrinterbugs.unicorn.UnicornHornInventory;

/** Keeps the horn separate from cargo, whose size changes when a chest is added. */
@Mixin(AbstractHorse.class)
public abstract class AbstractHorseEntityMixin implements UnicornHornHolder {
    @Unique
    private static final EntityDataAccessor<ItemStack> unicorn$HORN_TRACKER =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.ITEM_STACK);

    @Unique private SimpleContainer unicorn$hornInventory;

    @Override
    public SimpleContainer unicorn$getHornInventory() {
        if (unicorn$hornInventory == null) {
            unicorn$hornInventory =
                    new UnicornHornInventory(
                            stack -> {
                                AbstractHorse self = (AbstractHorse) (Object) this;
                                if (!self.level().isClientSide()) {
                                    self.getEntityData().set(unicorn$HORN_TRACKER, stack.copy());
                                }
                            });
        }
        return unicorn$hornInventory;
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void unicorn$trackHornData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(unicorn$HORN_TRACKER, ItemStack.EMPTY);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void unicorn$saveHorn(ValueOutput output, CallbackInfo ci) {
        ItemStack horn = unicorn$getHornStack();
        if (!horn.isEmpty()) output.store("UnicornHornItem", ItemStack.OPTIONAL_CODEC, horn);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void unicorn$loadHorn(ValueInput input, CallbackInfo ci) {
        unicorn$setHornStack(
                input.read("UnicornHornItem", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
    }

    @Inject(method = "dropEquipment", at = @At("TAIL"))
    private void unicorn$dropHorn(ServerLevel level, CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        ItemStack horn = unicorn$getHornInventory().removeItemNoUpdate(0);
        if (!horn.isEmpty()) self.spawnAtLocation(level, horn);
        unicorn$getHornInventory().setChanged();
    }

    @Override
    public ItemStack unicorn$getHornStack() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        return self.level().isClientSide()
                ? self.getEntityData().get(unicorn$HORN_TRACKER)
                : unicorn$getHornInventory().getItem(0);
    }

    @Override
    public void unicorn$setHornStack(ItemStack stack) {
        unicorn$getHornInventory().setItem(0, stack.copy());
    }
}
