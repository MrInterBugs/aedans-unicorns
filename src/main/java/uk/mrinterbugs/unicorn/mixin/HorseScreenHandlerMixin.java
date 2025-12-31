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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.mrinterbugs.unicorn.UnicornMod;

/**
 * Adds a unicorn horn slot to horse screens and handles quick-move logic for horns.
 */
@Mixin(HorseScreenHandler.class)
public abstract class HorseScreenHandlerMixin extends ScreenHandler {
    @Shadow @Final private Inventory inventory;
    @Shadow @Final private AbstractHorseEntity entity;

    /**
     * Base constructor passthrough for mixin extension.
     */
    protected HorseScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    /**
     * Places the unicorn horn slot under the armor slot so it remains grouped with equipment.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void unicorn$addHornSlot(int syncId, PlayerInventory playerInventory, Inventory inventory, AbstractHorseEntity entity, int inventoryColumns, CallbackInfo ci) {
        int hornInventoryIndex = this.inventory.size() - 1;
        int hornSlotX = 26;
        int hornSlotY = 18;

        this.addSlot(new Slot(this.inventory, hornInventoryIndex, hornSlotX, hornSlotY) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(UnicornMod.UNICORN_HORN);
            }

            /**
             * Uses an armor slot sprite that is always present to avoid missing-texture rendering.
             */
            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier.of(UnicornMod.MOD_ID, "item/unicorn_horn_outline"));
            }

            @Override
            public int getMaxItemCount() {
                return 1;
            }
        });
    }

    /**
     * Moves a unicorn horn into the dedicated slot when shift-clicked.
     */
    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void unicorn$quickMoveHorn(PlayerEntity player, int index, CallbackInfoReturnable<ItemStack> cir) {
        int hornSlotIndex = unicorn$getHornSlotIndex();
        if (hornSlotIndex == -1 || index == hornSlotIndex) {
            return;
        }

        Slot fromSlot = this.getSlot(index);
        if (fromSlot == null || !fromSlot.hasStack()) {
            return;
        }

        ItemStack stack = fromSlot.getStack();
        if (!stack.isOf(UnicornMod.UNICORN_HORN)) {
            return;
        }

        Slot hornSlot = this.getSlot(hornSlotIndex);
        if (hornSlot.hasStack() || !hornSlot.canInsert(stack)) {
            return;
        }

        ItemStack original = stack.copy();
        if (!this.insertItem(stack, hornSlotIndex, hornSlotIndex + 1, false)) {
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
        int hornInventoryIndex = this.inventory.size() - 1;
        for (int i = 0; i < this.slots.size(); i++) {
            Slot slot = this.slots.get(i);
            if (slot.inventory == this.inventory && slot.getIndex() == hornInventoryIndex) {
                return i;
            }
        }
        return -1;
    }
}
