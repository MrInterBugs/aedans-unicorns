package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.mrinterbugs.unicorn.UnicornHornHolder;

/**
 * Adds a unicorn horn slot to horse inventories and keeps horn state synced between server and client.
 */
@Mixin(AbstractHorseEntity.class)
public abstract class AbstractHorseEntityMixin implements UnicornHornHolder {
    @Unique
    private static final TrackedData<ItemStack> unicorn$HORN_TRACKER = DataTracker.registerData(AbstractHorseEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    @Shadow protected SimpleInventory items;
    @Unique
    private boolean unicorn$syncingHorn;

    /**
     * Adds a dedicated unicorn horn slot to every horse-style inventory.
     */
    @Inject(method = "getInventorySize(I)I", at = @At("RETURN"), cancellable = true)
    private static void unicorn$addHornSlot(int inventoryColumns, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValueI() + 1);
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
        ItemStack horn = unicorn$getHornStack();
        if (!horn.isEmpty()) {
            AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
            nbt.put("UnicornHornItem", horn.encode(self.getRegistryManager()));
        }
    }

    /**
     * Restores the horn item from entity NBT when loading.
     */
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void unicorn$loadHorn(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("UnicornHornItem", NbtCompound.COMPOUND_TYPE)) {
            AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
            ItemStack.fromNbt(self.getRegistryManager(), nbt.getCompound("UnicornHornItem"))
                    .ifPresent(this::unicorn$setHornStack);
        }
    }

    /**
     * Returns the horn slot index within the horse inventory or -1 if unavailable.
     */
    @Unique
    private int unicorn$getHornInventoryIndex() {
        if (items == null || items.size() == 0) {
            return -1;
        }
        return items.size() - 1;
    }

    /**
     * Returns the horn stack, preferring tracked data on the client where the inventory can be stale.
     */
    @Override
    public ItemStack unicorn$getHornStack() {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        ItemStack tracked = self.getDataTracker().get(unicorn$HORN_TRACKER);
        if (self.getWorld().isClient()) {
            return tracked;
        }

        int hornIndex = unicorn$getHornInventoryIndex();
        if (hornIndex < 0) {
            return ItemStack.EMPTY;
        }
        ItemStack inventoryStack = items.getStack(hornIndex);
        if (!inventoryStack.isEmpty()) {
            return inventoryStack;
        }
        return tracked;
    }

    /**
     * Updates the horn stack, syncing both tracker data and the backing inventory slot.
     */
    @Override
    public void unicorn$setHornStack(ItemStack stack) {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        
        if (!ItemStack.areEqual(self.getDataTracker().get(unicorn$HORN_TRACKER), stack)) {
            self.getDataTracker().set(unicorn$HORN_TRACKER, stack.copy());
        }

        int hornIndex = unicorn$getHornInventoryIndex();
        if (hornIndex < 0 || unicorn$syncingHorn) {
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
        if (hornIndex >= 0) {
            ItemStack inventoryStack = items.getStack(hornIndex);
            unicorn$setHornStack(inventoryStack);
        }
    }
}