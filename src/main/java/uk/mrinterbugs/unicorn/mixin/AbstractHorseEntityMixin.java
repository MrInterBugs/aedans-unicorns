package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.mrinterbugs.unicorn.UnicornHornHolder;
import uk.mrinterbugs.unicorn.UnicornMod;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseEntityMixin implements UnicornHornHolder {
    @Unique
    private static final EntityDataAccessor<ItemStack> unicorn$HORN_TRACKER = SynchedEntityData
            .defineId(AbstractHorse.class, EntityDataSerializers.ITEM_STACK);

    @Unique
    private static final String HORN_NBT_KEY = "UnicornHornItem";

    @Unique
    private static final int ADDITIONAL_SLOT_COUNT = 1;

    @Unique
    private static final int INVALID_SLOT_INDEX = -1;

    @Unique
    private static final int EMPTY_INVENTORY_SIZE = 0;

    @Shadow
    protected SimpleContainer inventory;

    @Unique
    private boolean unicorn$syncingHorn;

    @Inject(method = "getInventorySize()I", at = @At("RETURN"), cancellable = true)
    private void unicorn$addHornSlot(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValueI() + ADDITIONAL_SLOT_COUNT);
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void unicorn$syncHornOnTick(CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (self.level().isClientSide()) return;

        int hornIndex = unicorn$getHornInventoryIndex();
        if (hornIndex == INVALID_SLOT_INDEX) return;

        ItemStack inventoryStack = inventory.getItem(hornIndex);
        if ((inventoryStack.isEmpty() || inventoryStack.getItem() == UnicornMod.UNICORN_HORN)
                && !ItemStack.matches(inventoryStack, self.getEntityData().get(unicorn$HORN_TRACKER))) {
            unicorn$setHornStack(inventoryStack);
        }
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void unicorn$trackHornData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(unicorn$HORN_TRACKER, ItemStack.EMPTY);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void unicorn$saveHorn(ValueOutput output, CallbackInfo ci) {
        if (unicorn$hasChest()) {
            return;
        }
        ItemStack horn = unicorn$getHornStack();
        if (!horn.isEmpty()) {
            output.store(HORN_NBT_KEY, ItemStack.OPTIONAL_CODEC, horn);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void unicorn$loadHorn(ValueInput input, CallbackInfo ci) {
        if (unicorn$hasChest()) {
            int index = unicorn$getHornInventoryIndex();
            if (index != INVALID_SLOT_INDEX) {
                unicorn$setHornStack(inventory.getItem(index));
            }
            return;
        }
        input.read(HORN_NBT_KEY, ItemStack.OPTIONAL_CODEC)
                .ifPresent(horn -> { if (!horn.isEmpty()) unicorn$setHornStack(horn); });
    }

    @Unique
    private boolean unicorn$hasChest() {
        if ((AbstractHorse) (Object) this instanceof AbstractChestedHorse donkey) {
            return donkey.hasChest();
        }
        return false;
    }

    @Unique
    private int unicorn$getHornInventoryIndex() {
        if (inventory == null || inventory.getContainerSize() == EMPTY_INVENTORY_SIZE) {
            return INVALID_SLOT_INDEX;
        }
        return inventory.getContainerSize() - ADDITIONAL_SLOT_COUNT;
    }

    @Override
    public ItemStack unicorn$getHornStack() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        ItemStack tracked = self.getEntityData().get(unicorn$HORN_TRACKER);
        if (self.level().isClientSide()) {
            return tracked;
        }

        int hornIndex = unicorn$getHornInventoryIndex();
        if (hornIndex == INVALID_SLOT_INDEX) {
            return ItemStack.EMPTY;
        }
        ItemStack inventoryStack = inventory.getItem(hornIndex);
        if (!inventoryStack.isEmpty()) {
            return inventoryStack;
        }
        return tracked;
    }

    @Override
    public void unicorn$setHornStack(ItemStack stack) {
        AbstractHorse self = (AbstractHorse) (Object) this;

        if (!ItemStack.matches(self.getEntityData().get(unicorn$HORN_TRACKER), stack)) {
            self.getEntityData().set(unicorn$HORN_TRACKER, stack.copy());
        }

        int hornIndex = unicorn$getHornInventoryIndex();
        if (hornIndex == INVALID_SLOT_INDEX || unicorn$syncingHorn) {
            return;
        }
        try {
            unicorn$syncingHorn = true;
            if (!ItemStack.matches(inventory.getItem(hornIndex), stack)) {
                inventory.setItem(hornIndex, stack.copy());
            }
        } finally {
            unicorn$syncingHorn = false;
        }
    }
}
