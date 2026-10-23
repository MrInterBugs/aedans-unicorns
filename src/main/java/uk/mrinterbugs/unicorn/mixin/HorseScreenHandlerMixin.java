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

import uk.mrinterbugs.unicorn.UnicornHornHolder;
import uk.mrinterbugs.unicorn.UnicornMod;

/** Adds a unicorn horn slot to horse screens and handles quick-move logic for horns. */
@Mixin(HorseScreenHandler.class)
public abstract class HorseScreenHandlerMixin extends ScreenHandler {
    @Unique private static final int HORN_SLOT_X = 8;
    @Unique private static final int HORN_SLOT_Y = 54;
    @Unique private static final int MAX_HORN_COUNT = 1;
    @Shadow @Final private AbstractHorseEntity entity;

    /** Base constructor passthrough for mixin extension. */
    protected HorseScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    /** Places the unicorn horn slot under the armor slot so it remains grouped with equipment. */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void unicorn$addHornSlot(
            int syncId,
            PlayerInventory playerInventory,
            Inventory inventory,
            AbstractHorseEntity entity,
            int inventoryColumns,
            CallbackInfo ci) {
        Inventory hornInventory = ((UnicornHornHolder) entity).unicorn$getHornInventory();
        this.addSlot(
                new Slot(hornInventory, 0, HORN_SLOT_X, HORN_SLOT_Y) {
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
                        return Pair.of(
                                PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
                                Identifier.of(UnicornMod.MOD_ID, "item/unicorn_horn_outline"));
                    }

                    @Override
                    public int getMaxItemCount() {
                        return MAX_HORN_COUNT;
                    }
                });
    }

    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void unicorn$quickMove(
            PlayerEntity player, int index, CallbackInfoReturnable<ItemStack> cir) {
        UnicornHornHolder holder = (UnicornHornHolder) this.entity;
        int hornSlot = this.slots.size() - 1;
        if (hornSlot < 36
                || this.slots.get(hornSlot).inventory != holder.unicorn$getHornInventory()) return;

        // The horn is appended after the 36 player slots, outside vanilla's ranges.
        int playerStart = hornSlot - 36;
        cir.setReturnValue(ItemStack.EMPTY);
        if (index < 0 || index >= this.slots.size()) return;
        Slot from = this.slots.get(index);
        if (!from.hasStack() || !from.canTakeItems(player)) return;
        ItemStack stack = from.getStack();
        ItemStack original = stack.copy();
        boolean moved;
        if (index == hornSlot || index < playerStart) {
            moved = this.insertItem(stack, playerStart, hornSlot, true);
        } else if (stack.isOf(UnicornMod.UNICORN_HORN) && !this.slots.get(hornSlot).hasStack()) {
            moved = this.insertItem(stack, hornSlot, hornSlot + 1, false);
        } else if (this.slots.get(1).canInsert(stack) && !this.slots.get(1).hasStack()) {
            moved = this.insertItem(stack, 1, 2, false);
        } else if (this.slots.get(0).canInsert(stack) && !this.slots.get(0).hasStack()) {
            moved = this.insertItem(stack, 0, 1, false);
        } else {
            moved = playerStart > 2 && this.insertItem(stack, 2, playerStart, false);
            if (!moved) {
                int hotbarStart = playerStart + 27;
                moved =
                        index < hotbarStart
                                ? this.insertItem(stack, hotbarStart, hornSlot, false)
                                : this.insertItem(stack, playerStart, hotbarStart, false);
            }
        }
        if (!moved) return;
        if (stack.isEmpty()) from.setStack(ItemStack.EMPTY);
        else from.markDirty();
        cir.setReturnValue(original);
    }
}
