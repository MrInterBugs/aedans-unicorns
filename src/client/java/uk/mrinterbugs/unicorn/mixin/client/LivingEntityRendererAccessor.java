package uk.mrinterbugs.unicorn.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor {
    @Accessor
    ItemModelResolver getItemModelResolver();
}
