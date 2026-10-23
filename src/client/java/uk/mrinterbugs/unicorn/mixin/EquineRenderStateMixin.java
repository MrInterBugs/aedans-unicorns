package uk.mrinterbugs.unicorn.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import uk.mrinterbugs.unicorn.UnicornEquineState;

@Environment(EnvType.CLIENT)
@Mixin(EquineRenderState.class)
public class EquineRenderStateMixin implements UnicornEquineState {
    @Unique
    private final ItemStackRenderState unicorn$hornItem = new ItemStackRenderState();

    @Override
    public ItemStackRenderState unicorn$getHornItem() {
        return unicorn$hornItem;
    }
}
