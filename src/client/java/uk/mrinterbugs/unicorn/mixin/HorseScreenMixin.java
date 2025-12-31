package uk.mrinterbugs.unicorn.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Renders a slot background for the unicorn horn so it looks like a real slot.
 */
@Environment(EnvType.CLIENT)
@Mixin(HorseScreen.class)
public abstract class HorseScreenMixin extends HandledScreen<HorseScreenHandler> {
    private static final Identifier UNICORN_HORN_SLOT_TEXTURE = Identifier.ofVanilla("container/slot");

    protected HorseScreenMixin(HorseScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    /**
     * Draws the horn slot background alongside the horse equipment slots at the 18x18 slot position.
     */
    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void unicorn$drawHornSlot(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        int slotX = this.x + 25;
        int slotY = this.y + 17;
        context.drawGuiTexture(UNICORN_HORN_SLOT_TEXTURE, slotX, slotY, 18, 18);
    }
}
