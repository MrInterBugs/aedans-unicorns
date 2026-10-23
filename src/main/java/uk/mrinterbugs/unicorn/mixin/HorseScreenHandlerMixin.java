package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
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
import uk.mrinterbugs.unicorn.UnicornMod;

@Mixin(HorseInventoryMenu.class)
public abstract class HorseScreenHandlerMixin extends AbstractContainerMenu {
    @Unique private static final int HORN_SLOT_X = 26;
    @Unique private static final int HORN_SLOT_Y = 18;
    @Unique private static final int MAX_HORN_COUNT = 1;
    @Unique private static final int SINGLE_SLOT_SIZE = 1;

    protected HorseScreenHandlerMixin(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void unicorn$addHornSlot(int syncId, Inventory playerInventory, Container inventory,
            AbstractHorse entity, int inventoryColumns, CallbackInfo ci) {
        if (entity == null) return;

        // Use the entity's own inventory rather than mountContainer.
        // On the client, mountContainer is a synthetic SimpleContainer(0) created from
        // inventoryColumns*3, which gives index -1 for a regular horse. The entity's
        // inventory is correctly sized (getInventorySize() includes our +1 horn slot),
        // and on the server mountContainer IS the entity's inventory so nothing changes.
        SimpleContainer entityInventory = ((AbstractHorseInventoryAccessor) entity).getInventory();
        if (entityInventory == null) return;

        int hornInventoryIndex = entityInventory.getContainerSize() - SINGLE_SLOT_SIZE;
        if (hornInventoryIndex < 0) return;

        this.addSlot(new Slot(entityInventory, hornInventoryIndex, HORN_SLOT_X, HORN_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() == UnicornMod.UNICORN_HORN;
            }

            @Override
            public @Nullable Identifier getNoItemIcon() {
                return Identifier.fromNamespaceAndPath(UnicornMod.MOD_ID, "item/unicorn_horn_outline");
            }

            @Override
            public int getMaxStackSize() {
                return MAX_HORN_COUNT;
            }
        });
    }

}
