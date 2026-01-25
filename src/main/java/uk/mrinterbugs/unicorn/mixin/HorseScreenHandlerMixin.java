package uk.mrinterbugs.unicorn.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.mrinterbugs.unicorn.UnicornMod;

/**
 * Adds a unicorn horn slot to horse screens and handles quick-move logic for
 * horns.
 */
@Mixin(HorseScreenHandler.class)
public abstract class HorseScreenHandlerMixin extends ScreenHandler {
    @Unique
    private static final int HORN_SLOT_X = 26;
    @Unique
    private static final int HORN_SLOT_Y = 18;
    @Unique
    private static final int MAX_HORN_COUNT = 1;
    @Unique
    private static final int PLAYER_INVENTORY_SIZE = 36;
    @Unique
    private static final int INVALID_SLOT_INDEX = -1;
    @Unique
    private static final int SINGLE_SLOT_SIZE = 1;

    @Shadow
    @Final
    private Inventory inventory;
    @Shadow
    @Final
    private AbstractHorseEntity entity;

    /**
     * Base constructor passthrough for mixin extension.
     */
    protected HorseScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    /**
     * Places the unicorn horn slot under the armor slot so it remains grouped with
     * equipment.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void unicorn$addHornSlot(int syncId, PlayerInventory playerInventory, Inventory inventory,
            AbstractHorseEntity entity, int inventoryColumns, CallbackInfo ci) {
        int hornInventoryIndex = this.inventory.size() - SINGLE_SLOT_SIZE;

        this.addSlot(new Slot(this.inventory, hornInventoryIndex, HORN_SLOT_X, HORN_SLOT_Y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(UnicornMod.UNICORN_HORN);
            }

            /**
             * Uses an armor slot sprite that is always present to avoid missing-texture
             * rendering.
             */
            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
                        Identifier.of(UnicornMod.MOD_ID, "item/unicorn_horn_outline"));
            }

            @Override
            public int getMaxItemCount() {
                return MAX_HORN_COUNT;
            }
        });
    }

    /**
     * Moves a unicorn horn into the dedicated slot when shift-clicked,
     * OR moves it back to the inventory if shift-clicked from the horn slot.
     */
    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void unicorn$quickMoveHorn(PlayerEntity player, int index, CallbackInfoReturnable<ItemStack> cir) {
        int hornSlotIndex = unicorn$getHornSlotIndex();
        if (hornSlotIndex == INVALID_SLOT_INDEX)
            return;

        Slot fromSlot = this.getSlot(index);
        if (fromSlot == null || !fromSlot.hasStack())
            return;

        ItemStack stack = fromSlot.getStack();
        ItemStack original = stack.copy();

        if (index == hornSlotIndex) {
            int playerInvStart = this.slots.size() - PLAYER_INVENTORY_SIZE;
            int playerInvEnd = this.slots.size();

            if (!this.insertItem(stack, playerInvStart, playerInvEnd, true)) {
                return;
            }
        } else if (stack.isOf(UnicornMod.UNICORN_HORN)) {
            Slot hornSlot = this.getSlot(hornSlotIndex);

            if (hornSlot.hasStack() || !hornSlot.canInsert(stack)) {
                return;
            }

            if (!this.insertItem(stack, hornSlotIndex, hornSlotIndex + SINGLE_SLOT_SIZE, false)) {
                return;
            }
        } else {
            return;
        }

        if (stack.isEmpty()) {
            fromSlot.setStack(ItemStack.EMPTY);
        } else {
            fromSlot.markDirty();
        }

        cir.setReturnValue(original);
    }

    /**
     * Locates the horn slot index within the screen handler's slot list.
     */
    private int unicorn$getHornSlotIndex() {
        int hornInventoryIndex = this.inventory.size() - SINGLE_SLOT_SIZE;
        for (int i = 0; i < this.slots.size(); i++) {
            Slot slot = this.slots.get(i);
            if (slot.inventory == this.inventory && slot.getIndex() == hornInventoryIndex) {
                return i;
            }
        }
        return INVALID_SLOT_INDEX;
    }
}