package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractMountInventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import uk.mrinterbugs.unicorn.UnicornHornHolder;
import uk.mrinterbugs.unicorn.UnicornMod;

@Mixin(AbstractMountInventoryMenu.class)
public abstract class AbstractMountInventoryMenuMixin extends AbstractContainerMenu {
    protected AbstractMountInventoryMenuMixin() {
        super(null, 0);
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void unicorn$quickMove(
            Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (!(((AbstractMountInventoryMenuAccessor) this).getMount()
                instanceof UnicornHornHolder holder)) return;
        int hornSlot = this.slots.size() - 1;
        if (hornSlot < 36
                || this.slots.get(hornSlot).container != holder.unicorn$getHornInventory()) return;

        // The horn is appended after the 36 player slots, outside vanilla's ranges.
        int playerStart = hornSlot - 36;
        cir.setReturnValue(ItemStack.EMPTY);
        if (index < 0 || index >= this.slots.size()) return;
        Slot from = this.slots.get(index);
        if (!from.hasItem() || !from.mayPickup(player)) return;
        ItemStack stack = from.getItem();
        ItemStack original = stack.copy();
        boolean moved;
        if (index == hornSlot || index < playerStart) {
            moved = this.moveItemStackTo(stack, playerStart, hornSlot, true);
        } else if (stack.is(UnicornMod.UNICORN_HORN) && !this.slots.get(hornSlot).hasItem()) {
            moved = this.moveItemStackTo(stack, hornSlot, hornSlot + 1, false);
        } else if (this.slots.get(1).mayPlace(stack) && !this.slots.get(1).hasItem()) {
            moved = this.moveItemStackTo(stack, 1, 2, false);
        } else if (this.slots.get(0).mayPlace(stack) && !this.slots.get(0).hasItem()) {
            moved = this.moveItemStackTo(stack, 0, 1, false);
        } else {
            moved = playerStart > 2 && this.moveItemStackTo(stack, 2, playerStart, false);
            if (!moved) {
                int hotbarStart = playerStart + 27;
                moved =
                        index < hotbarStart
                                ? this.moveItemStackTo(stack, hotbarStart, hornSlot, false)
                                : this.moveItemStackTo(stack, playerStart, hotbarStart, false);
            }
        }
        if (!moved) return;
        if (stack.isEmpty()) from.setByPlayer(ItemStack.EMPTY);
        else from.setChanged();
        cir.setReturnValue(original);
    }
}
