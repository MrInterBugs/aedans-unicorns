package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.mrinterbugs.unicorn.UnicornHornHolder;

/**
 * Adds a unicorn horn slot to horse inventories and keeps horn state synced
 * between server and client.
 */
@Mixin(AbstractHorseEntity.class)
public abstract class AbstractHorseEntityMixin implements UnicornHornHolder {
    @Unique
    private static final TrackedData<ItemStack> unicorn$HORN_TRACKER = DataTracker
            .registerData(AbstractHorseEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    @Unique
    private static final String HORN_NBT_KEY = "UnicornHornItem";

    @Unique
    private static final int ADDITIONAL_SLOT_COUNT = 1;

    @Unique
    private static final int INVALID_SLOT_INDEX = -1;

    @Unique
    private static final int EMPTY_INVENTORY_SIZE = 0;

    @Shadow
    protected SimpleInventory items;

    @Unique
    private boolean unicorn$syncingHorn;

    /**
     * Adds a dedicated unicorn horn slot to every horse-style inventory.
     */
    @Inject(method = "getInventorySize(I)I", at = @At("RETURN"), cancellable = true)
    private static void unicorn$addHornSlot(int inventoryColumns, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValueI() + ADDITIONAL_SLOT_COUNT);
    }

    /**
     * Registers a tracked data entry for the unicorn horn item.
     */
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void unicorn$trackHornData(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(unicorn$HORN_TRACKER, ItemStack.EMPTY);
    }

    /**
     * Persists the horn item into entity NBT when saving.
     */
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void unicorn$saveHorn(NbtCompound nbt, CallbackInfo ci) {
        if (unicorn$hasChest()) {
            return;
        }

        ItemStack horn = unicorn$getHornStack();
        if (!horn.isEmpty()) {
            AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
            nbt.put(HORN_NBT_KEY, horn.encode(self.getRegistryManager()));
        }
    }

    /**
     * Restores the horn item from entity NBT when loading.
     */
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void unicorn$loadHorn(NbtCompound nbt, CallbackInfo ci) {
        if (unicorn$hasChest()) {
            int index = unicorn$getHornInventoryIndex();
            if (index != INVALID_SLOT_INDEX) {
                unicorn$setHornStack(items.getStack(index));
            }
            return;
        }

        if (nbt.contains(HORN_NBT_KEY, NbtElement.COMPOUND_TYPE)) {
            AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
            ItemStack.fromNbt(self.getRegistryManager(), nbt.getCompound(HORN_NBT_KEY))
                    .ifPresent(this::unicorn$setHornStack);
        }
    }

    /**
     * Checks if the entity is a donkey with a chest to prevent duplication.
     */
    @Unique
    private boolean unicorn$hasChest() {
        if ((AbstractHorseEntity) (Object) this instanceof AbstractDonkeyEntity donkey) {
            return donkey.hasChest();
        }
        return false;
    }

    /**
     * Returns the horn slot index within the horse inventory or -1 if unavailable.
     */
    @Unique
    private int unicorn$getHornInventoryIndex() {
        if (items == null || items.size() == EMPTY_INVENTORY_SIZE) {
            return INVALID_SLOT_INDEX;
        }
        return items.size() - ADDITIONAL_SLOT_COUNT;
    }

    /**
     * Returns the horn stack, preferring tracked data on the client where the
     * inventory can be stale.
     */
    @Override
    public ItemStack unicorn$getHornStack() {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        ItemStack tracked = self.getDataTracker().get(unicorn$HORN_TRACKER);
        if (self.getWorld().isClient()) {
            return tracked;
        }

        int hornIndex = unicorn$getHornInventoryIndex();
        if (hornIndex == INVALID_SLOT_INDEX) {
            return ItemStack.EMPTY;
        }
        ItemStack inventoryStack = items.getStack(hornIndex);
        if (!inventoryStack.isEmpty()) {
            return inventoryStack;
        }
        return tracked;
    }

    /**
     * Updates the horn stack, syncing both tracker data and the backing inventory
     * slot.
     */
    @Override
    public void unicorn$setHornStack(ItemStack stack) {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;

        if (!ItemStack.areEqual(self.getDataTracker().get(unicorn$HORN_TRACKER), stack)) {
            self.getDataTracker().set(unicorn$HORN_TRACKER, stack.copy());
        }

        int hornIndex = unicorn$getHornInventoryIndex();
        if (hornIndex == INVALID_SLOT_INDEX || unicorn$syncingHorn) {
            return;
        }
        try {
            unicorn$syncingHorn = true;
            if (!ItemStack.areEqual(items.getStack(hornIndex), stack)) {
                items.setStack(hornIndex, stack.copy());
            }
        } finally {
            unicorn$syncingHorn = false;
        }
    }

    /**
     * Keeps tracked horn data aligned when the horse inventory mutates.
     */
    @Inject(method = "onInventoryChanged", at = @At("TAIL"))
    private void unicorn$syncHornTracker(Inventory inventory, CallbackInfo ci) {
        if (unicorn$syncingHorn || inventory != this.items) {
            return;
        }
        int hornIndex = unicorn$getHornInventoryIndex();
        if (hornIndex != INVALID_SLOT_INDEX) {
            ItemStack inventoryStack = items.getStack(hornIndex);
            unicorn$setHornStack(inventoryStack);
        }
    }
}