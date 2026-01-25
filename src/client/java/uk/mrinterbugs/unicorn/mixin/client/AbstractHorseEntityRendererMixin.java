package uk.mrinterbugs.unicorn.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.AbstractHorseEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.mrinterbugs.unicorn.client.render.UnicornHornFeatureRenderer;

@Environment(EnvType.CLIENT)
@Mixin(AbstractHorseEntityRenderer.class)
public abstract class AbstractHorseEntityRendererMixin<T extends AbstractHorseEntity>
        extends MobEntityRenderer<T, HorseEntityModel<T>> {
    protected AbstractHorseEntityRendererMixin(EntityRendererFactory.Context context, HorseEntityModel<T> model,
            float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void unicorn$addHornRenderer(EntityRendererFactory.Context ctx, HorseEntityModel<T> model, float scale,
            CallbackInfo ci) {
        this.addFeature(new UnicornHornFeatureRenderer<>(this));
    }
}
