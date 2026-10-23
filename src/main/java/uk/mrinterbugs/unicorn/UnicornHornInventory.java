package uk.mrinterbugs.unicorn;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/** SimpleContainer no longer has listeners in 26.1; notify the tracker on mutation. */
public final class UnicornHornInventory extends SimpleContainer {
    private final Consumer<ItemStack> onChanged;

    public UnicornHornInventory(Consumer<ItemStack> onChanged) {
        super(1);
        this.onChanged = onChanged;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        onChanged.accept(getItem(0));
    }
}
