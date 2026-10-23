package uk.mrinterbugs.unicorn.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.mrinterbugs.unicorn.UnicornEquineState;
import uk.mrinterbugs.unicorn.UnicornHornHolder;
import uk.mrinterbugs.unicorn.UnicornMod;

@Environment(EnvType.CLIENT)
@Mixin(AbstractHorseRenderer.class)
public abstract class AbstractHorseEntityRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/animal/equine/AbstractHorse;Lnet/minecraft/client/renderer/entity/state/EquineRenderState;F)V",
            at = @At("TAIL"))
    private void unicorn$extractHornState(AbstractHorse entity, EquineRenderState state, float partialTick, CallbackInfo ci) {
        if (!(state instanceof UnicornEquineState unicornState)) return;
        if (!(entity instanceof UnicornHornHolder hornHolder)) return;

        ItemStack hornStack = hornHolder.unicorn$getHornStack();
        unicornState.unicorn$getHornItem().clear();

        if (!hornStack.isEmpty() && hornStack.getItem() == UnicornMod.UNICORN_HORN) {
            ((LivingEntityRendererAccessor) this).getItemModelResolver().updateForLiving(
                    unicornState.unicorn$getHornItem(), hornStack, ItemDisplayContext.FIXED, entity);
        }
    }
}
