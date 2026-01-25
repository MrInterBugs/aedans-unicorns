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
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
    private static final int HORN_DECREMENT_AMOUNT = 1;
    @Unique
    private static final float POST_TOTEM_HEALTH = 1.0F;

    @Unique
    private static final int REGEN_DURATION = 900;
    @Unique
    private static final int REGEN_AMPLIFIER = 1;

    @Unique
    private static final int ABSORPTION_DURATION = 100;
    @Unique
    private static final int ABSORPTION_AMPLIFIER = 1;

    @Unique
    private static final int FIRE_RES_DURATION = 800;
    @Unique
    private static final int FIRE_RES_AMPLIFIER = 0;

    /**
     * Consumes a slotted unicorn horn as a totem when a horse would die.
     */
    @Inject(method = "tryUseTotem", at = @At("HEAD"), cancellable = true)
    private void unicorn$tryHornTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (source.isOf(DamageTypes.OUT_OF_WORLD))
            return;

        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof AbstractHorseEntity horse && horse instanceof UnicornHornHolder hornHolder) {
            ItemStack hornStack = hornHolder.unicorn$getHornStack();

            if (!hornStack.isEmpty() && hornStack.isOf(UnicornMod.UNICORN_HORN)) {
                hornStack.decrement(HORN_DECREMENT_AMOUNT);
                hornHolder.unicorn$setHornStack(hornStack);

                entity.setHealth(POST_TOTEM_HEALTH);
                entity.clearStatusEffects();

                entity.addStatusEffect(
                        new StatusEffectInstance(StatusEffects.REGENERATION, REGEN_DURATION, REGEN_AMPLIFIER));
                entity.addStatusEffect(
                        new StatusEffectInstance(StatusEffects.ABSORPTION, ABSORPTION_DURATION, ABSORPTION_AMPLIFIER));
                entity.addStatusEffect(
                        new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, FIRE_RES_DURATION, FIRE_RES_AMPLIFIER));

                entity.getWorld().sendEntityStatus(entity, EntityStatuses.USE_TOTEM_OF_UNDYING);

                cir.setReturnValue(true);
            }
        }
    }
}