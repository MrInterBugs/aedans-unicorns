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

    /**
     * Consumes a slotted unicorn horn as a totem when a horse would die.
     */
    @Inject(method = "tryUseTotem", at = @At("HEAD"), cancellable = true)
    private void unicorn$tryHornTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (source.isOf(DamageTypes.OUT_OF_WORLD)) return;

        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof AbstractHorseEntity horse && horse instanceof UnicornHornHolder hornHolder) {
            ItemStack hornStack = hornHolder.unicorn$getHornStack();

            if (!hornStack.isEmpty() && hornStack.isOf(UnicornMod.UNICORN_HORN)) {
                hornStack.decrement(1); 
                hornHolder.unicorn$setHornStack(hornStack);
                
                entity.setHealth(1.0F);
                entity.clearStatusEffects();
                
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));

                entity.getWorld().sendEntityStatus(entity, EntityStatuses.USE_TOTEM_OF_UNDYING);
                
                cir.setReturnValue(true);
            }
        }
    }
}
