package uk.mrinterbugs.unicorn.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import uk.mrinterbugs.unicorn.UnicornHornHolder;
import uk.mrinterbugs.unicorn.UnicornMod;

@Environment(EnvType.CLIENT)
public class UnicornHornFeatureRenderer<T extends AbstractHorseEntity> extends FeatureRenderer<T, HorseEntityModel<T>> {
    private static final Identifier HORN_TEXTURE = Identifier.of(UnicornMod.MOD_ID, "textures/item/unicorn_horn.png");
    private final ItemRenderer itemRenderer;

    public UnicornHornFeatureRenderer(FeatureRendererContext<T, HorseEntityModel<T>> context) {
        super(context);
        this.itemRenderer = MinecraftClient.getInstance().getItemRenderer();
    }

    @Override
    public void render(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            T entity,
            float limbAngle,
            float limbDistance,
            float tickDelta,
            float animationProgress,
            float headYaw,
            float headPitch
    ) {
        if (!(entity instanceof UnicornHornHolder hornHolder)) {
            return;
        }

        ItemStack hornStack = hornHolder.unicorn$getHornStack();
        if (!hornStack.isOf(UnicornMod.UNICORN_HORN)) {
            return;
        }

        ModelPart head = getHeadPart();
        if (head == null) {
            return;
        }

        matrices.push();
        head.rotate(matrices);
        matrices.translate(0.00F, -1.2F, 0.16F);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        matrices.translate(0.0F, -0.32F, 0.0F);
        matrices.scale(0.60F, 0.60F, 0.60F);

        itemRenderer.renderItem(entity, hornStack, ModelTransformationMode.FIXED, false, matrices, vertexConsumers, entity.getWorld(), light, OverlayTexture.DEFAULT_UV, entity.getId());
        matrices.pop();
    }

    private ModelPart getHeadPart() {
        for (ModelPart part : this.getContextModel().getHeadParts()) {
            return part;
        }
        return null;
    }
}
