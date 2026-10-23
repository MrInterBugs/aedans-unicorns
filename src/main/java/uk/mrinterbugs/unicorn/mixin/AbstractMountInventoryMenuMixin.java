package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractMountInventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.mrinterbugs.unicorn.UnicornMod;

@Mixin(AbstractMountInventoryMenu.class)
public abstract class AbstractMountInventoryMenuMixin extends AbstractContainerMenu {
    @Unique private static final int PLAYER_INVENTORY_SIZE = 36;
    @Unique private static final int INVALID_SLOT_INDEX = -1;
    @Unique private static final int SINGLE_SLOT_SIZE = 1;

    protected AbstractMountInventoryMenuMixin() {
        super(null, 0);
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void unicorn$quickMoveHorn(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        int hornSlotIndex = unicorn$getHornSlotIndex();
        if (hornSlotIndex == INVALID_SLOT_INDEX) return;

        Slot fromSlot = this.getSlot(index);
        if (!fromSlot.hasItem()) return;

        ItemStack stack = fromSlot.getItem();
        ItemStack original = stack.copy();

        if (index == hornSlotIndex) {
            int playerInvStart = this.slots.size() - PLAYER_INVENTORY_SIZE;
            if (!this.moveItemStackTo(stack, playerInvStart, this.slots.size(), true)) return;
        } else if (stack.getItem() == UnicornMod.UNICORN_HORN) {
            Slot hornSlot = this.getSlot(hornSlotIndex);
            if (hornSlot.hasItem() || !hornSlot.mayPlace(stack)) return;
            if (!this.moveItemStackTo(stack, hornSlotIndex, hornSlotIndex + SINGLE_SLOT_SIZE, false)) return;
        } else {
            return;
        }

        if (stack.isEmpty()) {
            fromSlot.set(ItemStack.EMPTY);
        } else {
            fromSlot.setChanged();
        }

        cir.setReturnValue(original);
    }

    @Unique
    private int unicorn$getHornSlotIndex() {
        Container mountContainer = ((AbstractMountInventoryMenuAccessor) this).getMountContainer();
        int hornInventoryIndex = mountContainer.getContainerSize() - SINGLE_SLOT_SIZE;
        for (int i = 0; i < this.slots.size(); i++) {
            Slot slot = this.slots.get(i);
            if (slot.container == mountContainer && slot.getContainerSlot() == hornInventoryIndex) {
                return i;
            }
        }
        return INVALID_SLOT_INDEX;
    }
}
