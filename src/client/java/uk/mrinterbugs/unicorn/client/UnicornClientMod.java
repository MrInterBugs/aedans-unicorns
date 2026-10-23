package uk.mrinterbugs.unicorn.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import uk.mrinterbugs.unicorn.client.render.UnicornHornFeatureRenderer;

public class UnicornClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
            if (renderer instanceof AbstractHorseRenderer<?, ?, ?>) {
                registerHornLayer(renderer, helper);
            }
        });
        // RyoamicLights integration disabled — awaiting release for MC 26.1.2
    }

    @SuppressWarnings("unchecked")
    private static <S extends EquineRenderState, M extends AbstractEquineModel<S>> void registerHornLayer(
            Object renderer,
            LivingEntityRenderLayerRegistrationCallback.RegistrationHelper helper) {
        RenderLayerParent<S, M> parent = (RenderLayerParent<S, M>) renderer;
        helper.register(new UnicornHornFeatureRenderer<>(parent));
    }
}
