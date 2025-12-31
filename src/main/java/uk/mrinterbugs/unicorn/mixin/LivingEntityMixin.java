package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.mrinterbugs.unicorn.UnicornMod;
import uk.mrinterbugs.unicorn.UnicornHornHolder;

/**
 * Adds unicorn horn totem behavior for horse entities.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow public abstract void setHealth(float health);
    @Shadow public abstract boolean clearStatusEffects();
    @Shadow public abstract boolean addStatusEffect(StatusEffectInstance effect);

    /**
     * Consumes a slotted unicorn horn as a totem when a horse would die.
     */
    @Inject(method = "tryUseTotem", at = @At("HEAD"), cancellable = true)
    private void unicorn$tryHornTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof AbstractHorseEntity horse)) {
            return;
        }
        if (source.isOf(DamageTypes.OUT_OF_WORLD) || !(horse instanceof UnicornHornHolder hornHolder)) {
            return;
        }

        ItemStack hornStack = hornHolder.unicorn$getHornStack();
        if (hornStack.isEmpty() || !hornStack.isOf(UnicornMod.UNICORN_HORN)) {
            return;
        }

        hornHolder.unicorn$setHornStack(ItemStack.EMPTY);
        self.setHealth(1.0F);
        self.clearStatusEffects();
        self.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
        self.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
        self.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
        self.getWorld().sendEntityStatus(self, EntityStatuses.USE_TOTEM_OF_UNDYING);
        cir.setReturnValue(true);
    }
}
