package uk.mrinterbugs.unicorn.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.entity.AbstractHorseEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.thinkingstudio.ryoamiclights.api.DynamicLightHandlers;
import uk.mrinterbugs.unicorn.UnicornHornHolder;
import uk.mrinterbugs.unicorn.UnicornMod;
import uk.mrinterbugs.unicorn.client.render.UnicornHornFeatureRenderer;

public class UnicornClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, renderer, registrationHelper, context) -> {
            if (!(renderer instanceof AbstractHorseEntityRenderer<?, ?> horseRenderer)) {
                return;
            }
            @SuppressWarnings("unchecked")
            FeatureRendererContext<AbstractHorseEntity, HorseEntityModel<AbstractHorseEntity>> ctx =
                    (FeatureRendererContext<AbstractHorseEntity, HorseEntityModel<AbstractHorseEntity>>) (FeatureRendererContext<?, ?>) horseRenderer;
            registrationHelper.register(new UnicornHornFeatureRenderer<>(ctx));
        });

        if (FabricLoader.getInstance().isModLoaded("ryoamiclights")) {
            registerDynamicLights();
        }
    }

    private void registerDynamicLights() {
        DynamicLightHandlers.registerDynamicLightHandler(EntityType.HORSE, (entity) -> {
            if (entity instanceof UnicornHornHolder hornHolder) {
                if (hornHolder.unicorn$getHornStack().isOf(UnicornMod.UNICORN_HORN)) {
                    return 13;
                }
            }
            return 0;
        });
    }
}