package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.mrinterbugs.unicorn.UnicornHornHolder;
import uk.mrinterbugs.unicorn.UnicornMod;

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

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void unicorn$tryHornTotem(DamageSource source, CallbackInfo ci) {
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) return;

        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof AbstractHorse horse && horse instanceof UnicornHornHolder hornHolder) {
            ItemStack hornStack = hornHolder.unicorn$getHornStack();

            if (!hornStack.isEmpty() && hornStack.getItem() == UnicornMod.UNICORN_HORN) {
                hornStack.shrink(HORN_DECREMENT_AMOUNT);
                hornHolder.unicorn$setHornStack(hornStack);

                entity.setHealth(POST_TOTEM_HEALTH);
                entity.removeAllEffects();

                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGEN_DURATION, REGEN_AMPLIFIER));
                entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, ABSORPTION_DURATION, ABSORPTION_AMPLIFIER));
                entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, FIRE_RES_DURATION, FIRE_RES_AMPLIFIER));

                entity.level().broadcastEntityEvent(entity, (byte) 35);

                ci.cancel();
            }
        }
    }
}
