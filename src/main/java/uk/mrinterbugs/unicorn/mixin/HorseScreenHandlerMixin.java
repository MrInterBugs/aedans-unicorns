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
// Handle transfers before equipment mods with fixed slot-index assumptions.
@Mixin(value = HorseScreenHandler.class, priority = 900)
public abstract class HorseScreenHandlerMixin extends ScreenHandler {
    @Unique private static final int HORN_SLOT_X = 26;
    @Unique private static final int HORN_SLOT_Y = 18;
    @Unique private static final int MAX_HORN_COUNT = 1;
    @Shadow @Final private AbstractHorseEntity entity;
    @Shadow @Final private Inventory inventory;

    /** Base constructor passthrough for mixin extension. */
    protected HorseScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    /** Places the horn beside the saddle, leaving the lower equipment slot to other mods. */
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
        Inventory hornInventory = ((UnicornHornHolder) this.entity).unicorn$getHornInventory();
        int hornSlot = -1;
        int playerStart = -1;
        int playerEnd = -1;
        for (int i = 0; i < this.slots.size(); i++) {
            Slot slot = this.slots.get(i);
            if (slot.inventory == hornInventory) hornSlot = i;
            if (slot.inventory == player.getInventory()) {
                if (playerStart < 0) playerStart = i;
                playerEnd = i + 1;
            }
        }
        if (hornSlot < 0 || playerStart < 0) return;

        // Equipment mods can append slots before or after ours. Identify inventories,
        // rather than treating the last slot as the horn or as a player-slot boundary.
        cir.setReturnValue(ItemStack.EMPTY);
        if (index < 0 || index >= this.slots.size()) return;
        Slot from = this.slots.get(index);
        if (!from.hasStack() || !from.canTakeItems(player)) return;
        ItemStack stack = from.getStack();
        ItemStack original = stack.copy();
        boolean moved = false;
        if (from.inventory != player.getInventory()) {
            moved = this.insertItem(stack, playerStart, playerEnd, true);
        } else {
            // Try the horn and all other equipment before general donkey/mule cargo.
            if (stack.isOf(UnicornMod.UNICORN_HORN)) {
                moved = this.insertItem(stack, hornSlot, hornSlot + 1, false);
            }
            if (!moved) {
                for (int i = 0; i < this.slots.size(); i++) {
                    Slot target = this.slots.get(i);
                    if (i == hornSlot || target.inventory == player.getInventory()) continue;
                    if (i >= 2 && target.inventory == this.inventory) continue;
                    if (target.canInsert(stack) && this.insertItem(stack, i, i + 1, false)) {
                        moved = true;
                        break;
                    }
                }
            }
            if (!moved && playerStart > 2) {
                moved = this.insertItem(stack, 2, playerStart, false);
            }
            if (!moved) {
                int hotbarStart = playerEnd - 9;
                moved =
                        index < hotbarStart
                                ? this.insertItem(stack, hotbarStart, playerEnd, false)
                                : this.insertItem(stack, playerStart, hotbarStart, false);
            }
        }
        if (!moved) return;
        if (stack.isEmpty()) from.setStack(ItemStack.EMPTY);
        else from.markDirty();
        cir.setReturnValue(original);
    }
}
