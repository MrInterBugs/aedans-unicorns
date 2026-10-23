package uk.mrinterbugs.unicorn.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import uk.mrinterbugs.unicorn.UnicornEquineState;
import uk.mrinterbugs.unicorn.mixin.AbstractEquineModelAccessor;

@Environment(EnvType.CLIENT)
public class UnicornHornFeatureRenderer<S extends EquineRenderState, M extends AbstractEquineModel<S>>
        extends RenderLayer<S, M> {

    private static final float HORN_TRANSLATE_X = 0.00F;
    private static final float HORN_TRANSLATE_Y = -0.88F;
    private static final float HORN_TRANSLATE_Z = 0.16F;
    private static final float HORN_ROTATION_DEGREES = 180.0F;
    private static final float HORN_SCALE = 0.60F;

    public UnicornHornFeatureRenderer(RenderLayerParent<S, M> context) {
        super(context);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, S state, float yRot, float xRot) {
        if (!(state instanceof UnicornEquineState unicornState)) return;

        ItemStackRenderState hornRenderState = unicornState.unicorn$getHornItem();
        if (hornRenderState.isEmpty()) return;

        ModelPart head = ((AbstractEquineModelAccessor) getParentModel()).getHeadParts();
        if (head == null) return;

        poseStack.pushPose();
        head.translateAndRotate(poseStack);
        poseStack.translate(HORN_TRANSLATE_X, HORN_TRANSLATE_Y, HORN_TRANSLATE_Z);
        poseStack.mulPose(Axis.ZP.rotationDegrees(HORN_ROTATION_DEGREES));
        poseStack.scale(HORN_SCALE, HORN_SCALE, HORN_SCALE);

        hornRenderState.submit(poseStack, collector, packedLight, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }
}
