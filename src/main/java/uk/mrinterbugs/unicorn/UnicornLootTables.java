package uk.mrinterbugs.unicorn;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.Set;

public final class UnicornLootTables {
    private static final float CHANCE_MIN_ZERO = 0.0f;
    private static final float CHANCE_MAX_ONE = 1.0f;
    private static final float CHANCE_MAX_TWO = 2.0f;
    private static final float GUARANTEED_ROLL = 1.0f;
    private static final int DEFAULT_WEIGHT = 1;

    private static final Set<ResourceKey<LootTable>> BASTION_CHESTS = Set.of(
            BuiltInLootTables.BASTION_BRIDGE,
            BuiltInLootTables.BASTION_HOGLIN_STABLE,
            BuiltInLootTables.BASTION_OTHER,
            BuiltInLootTables.BASTION_TREASURE);

    private static final Set<ResourceKey<LootTable>> VILLAGE_CHESTS = Set.of(
            BuiltInLootTables.VILLAGE_ARMORER,
            BuiltInLootTables.VILLAGE_WEAPONSMITH,
            BuiltInLootTables.VILLAGE_TOOLSMITH);

    private UnicornLootTables() {
    }

    public static void register() {
        LootTableEvents.MODIFY.register(UnicornLootTables::addUnicornHornLoot);
    }

    private static void addUnicornHornLoot(ResourceKey<LootTable> key, LootTable.Builder tableBuilder,
            LootTableSource source, HolderLookup.Provider registries) {
        if (!source.isBuiltin()) {
            return;
        }

        if (BASTION_CHESTS.contains(key) || key.equals(BuiltInLootTables.NETHER_BRIDGE)) {
            tableBuilder.withPool(hornPool(UniformGenerator.between(CHANCE_MIN_ZERO, CHANCE_MAX_ONE), DEFAULT_WEIGHT));
            return;
        }

        if (VILLAGE_CHESTS.contains(key)) {
            tableBuilder.withPool(hornPool(UniformGenerator.between(CHANCE_MIN_ZERO, CHANCE_MAX_ONE), DEFAULT_WEIGHT));
            return;
        }

        if (key.equals(BuiltInLootTables.DESERT_PYRAMID)) {
            tableBuilder.withPool(hornPool(UniformGenerator.between(CHANCE_MIN_ZERO, CHANCE_MAX_ONE), DEFAULT_WEIGHT));
            return;
        }

        if (key.equals(BuiltInLootTables.SIMPLE_DUNGEON)) {
            tableBuilder.withPool(hornPool(ConstantValue.exactly(GUARANTEED_ROLL), DEFAULT_WEIGHT));
            return;
        }

        if (key.equals(BuiltInLootTables.END_CITY_TREASURE)) {
            tableBuilder.withPool(hornPool(UniformGenerator.between(CHANCE_MIN_ZERO, CHANCE_MAX_ONE), DEFAULT_WEIGHT));
            return;
        }

        if (key.equals(BuiltInLootTables.ANCIENT_CITY)) {
            tableBuilder.withPool(hornPool(UniformGenerator.between(CHANCE_MIN_ZERO, CHANCE_MAX_TWO), DEFAULT_WEIGHT));
            return;
        }

        if (key.equals(BuiltInLootTables.WOODLAND_MANSION)) {
            tableBuilder.withPool(hornPool(UniformGenerator.between(CHANCE_MIN_ZERO, CHANCE_MAX_ONE), DEFAULT_WEIGHT));
        }
    }

    private static LootPool.Builder hornPool(NumberProvider rolls, int weight) {
        return LootPool.lootPool()
                .setRolls(rolls)
                .add(LootItem.lootTableItem(UnicornMod.UNICORN_HORN).setWeight(weight));
    }
}
