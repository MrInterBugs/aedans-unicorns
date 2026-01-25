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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Renders a slot background for the unicorn horn so it looks like a real slot.
 */
@Environment(EnvType.CLIENT)
@Mixin(HorseScreen.class)
public abstract class HorseScreenMixin extends HandledScreen<HorseScreenHandler> {

    @Unique
    private static final Identifier UNICORN_HORN_SLOT_TEXTURE = Identifier.ofVanilla("container/slot");

    @Unique
    private static final int SLOT_SIZE = 18;

    @Unique
    private static final int HORN_SLOT_X_OFFSET = 25;

    @Unique
    private static final int HORN_SLOT_Y_OFFSET = 17;

    protected HorseScreenMixin(HorseScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    /**
     * Draws the horn slot background alongside the horse equipment slots
     * at the defined offset position.
     */
    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void unicorn$drawHornSlot(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        int slotX = this.x + HORN_SLOT_X_OFFSET;
        int slotY = this.y + HORN_SLOT_Y_OFFSET;
        context.drawGuiTexture(UNICORN_HORN_SLOT_TEXTURE, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
    }
}