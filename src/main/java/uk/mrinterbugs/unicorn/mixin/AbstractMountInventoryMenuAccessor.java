package uk.mrinterbugs.unicorn.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractMountInventoryMenu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractMountInventoryMenu.class)
public interface AbstractMountInventoryMenuAccessor {
    @Accessor("mount")
    net.minecraft.world.entity.LivingEntity getMount();

    @Accessor
    Container getMountContainer();
}
