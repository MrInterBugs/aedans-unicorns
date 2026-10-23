package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import uk.mrinterbugs.unicorn.UnicornHornHolder;
import uk.mrinterbugs.unicorn.UnicornMod;

@Mixin(HorseInventoryMenu.class)
public abstract class HorseScreenHandlerMixin extends AbstractContainerMenu {
    @Unique private static final int HORN_SLOT_X = 8;
    @Unique private static final int HORN_SLOT_Y = 54;
    @Unique private static final int MAX_HORN_COUNT = 1;

    protected HorseScreenHandlerMixin(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void unicorn$addHornSlot(
            int syncId,
            Inventory playerInventory,
            Container inventory,
            AbstractHorse entity,
            int inventoryColumns,
            CallbackInfo ci) {
        if (entity == null) return;

        var hornInventory = ((UnicornHornHolder) entity).unicorn$getHornInventory();
        this.addSlot(
                new Slot(hornInventory, 0, HORN_SLOT_X, HORN_SLOT_Y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return stack.getItem() == UnicornMod.UNICORN_HORN;
                    }

                    @Override
                    public @Nullable Identifier getNoItemIcon() {
                        return Identifier.fromNamespaceAndPath(
                                UnicornMod.MOD_ID, "item/unicorn_horn_outline");
                    }

                    @Override
                    public int getMaxStackSize() {
                        return MAX_HORN_COUNT;
                    }
                });
    }
}
