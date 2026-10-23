package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractMountInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractMountInventoryMenu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import uk.mrinterbugs.unicorn.UnicornHornHolder;

/** Draw the slot frame as well as Slot.getNoItemIcon's empty horn outline. */
@Mixin(AbstractMountInventoryScreen.class)
public abstract class HorseScreenMixin extends AbstractContainerScreen<AbstractMountInventoryMenu> {
    protected HorseScreenMixin(
            AbstractMountInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void unicorn$drawHornSlot(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo ci) {
        var mount = ((AbstractMountInventoryMenuAccessor) this.menu).getMount();
        if (!(mount instanceof UnicornHornHolder holder)) return;
        var slot = this.menu.slots.get(this.menu.slots.size() - 1);
        if (slot.container != holder.unicorn$getHornInventory()) return;
        graphics.blitSprite(
                net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                net.minecraft.resources.Identifier.withDefaultNamespace("container/slot"),
                this.leftPos + slot.x - 1,
                this.topPos + slot.y - 1,
                18,
                18);
    }
}
