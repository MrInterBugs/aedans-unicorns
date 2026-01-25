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
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import uk.mrinterbugs.unicorn.UnicornHornHolder;
import uk.mrinterbugs.unicorn.UnicornMod;

/**
 * Renders the unicorn horn item as a feature on the horse's head.
 */
@Environment(EnvType.CLIENT)
public class UnicornHornFeatureRenderer<T extends AbstractHorseEntity> extends FeatureRenderer<T, HorseEntityModel<T>> {

    private static final float HORN_TRANSLATE_X = 0.00F;
    private static final float HORN_TRANSLATE_Y = -0.88F;
    private static final float HORN_TRANSLATE_Z = 0.16F;

    private static final float HORN_ROTATION_DEGREES = 180.0F;
    private static final float HORN_SCALE = 0.60F;

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
            float headPitch) {
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

        matrices.translate(HORN_TRANSLATE_X, HORN_TRANSLATE_Y, HORN_TRANSLATE_Z);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(HORN_ROTATION_DEGREES));
        matrices.scale(HORN_SCALE, HORN_SCALE, HORN_SCALE);

        itemRenderer.renderItem(
                entity,
                hornStack,
                ModelTransformationMode.FIXED,
                false,
                matrices,
                vertexConsumers,
                entity.getWorld(),
                LightmapTextureManager.MAX_LIGHT_COORDINATE,
                OverlayTexture.DEFAULT_UV,
                entity.getId());

        matrices.pop();
    }

    /**
     * Retrieves the first head part from the horse model for positioning.
     */
    private ModelPart getHeadPart() {
        for (ModelPart part : this.getContextModel().getHeadParts()) {
            return part;
        }
        return null;
    }
}
