package uk.mrinterbugs.unicorn;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;

import java.util.Set;

/**
 * Adds unicorn horns to a handful of naturally generated loot tables.
 */
public final class UnicornLootTables {
    private static final float CHANCE_MIN_ZERO = 0.0f;
    private static final float CHANCE_MAX_ONE = 1.0f;
    private static final float CHANCE_MAX_TWO = 2.0f;
    private static final float GUARANTEED_ROLL = 1.0f;
    private static final int DEFAULT_WEIGHT = 1;

    private static final Set<RegistryKey<LootTable>> BASTION_CHESTS = Set.of(
            LootTables.BASTION_BRIDGE_CHEST,
            LootTables.BASTION_HOGLIN_STABLE_CHEST,
            LootTables.BASTION_OTHER_CHEST,
            LootTables.BASTION_TREASURE_CHEST);

    private static final Set<RegistryKey<LootTable>> VILLAGE_CHESTS = Set.of(
            LootTables.VILLAGE_ARMORER_CHEST,
            LootTables.VILLAGE_WEAPONSMITH_CHEST,
            LootTables.VILLAGE_TOOLSMITH_CHEST);

    private UnicornLootTables() {
    }

    public static void register() {
        LootTableEvents.MODIFY.register(UnicornLootTables::addUnicornHornLoot);
    }

    private static void addUnicornHornLoot(RegistryKey<LootTable> key, LootTable.Builder tableBuilder,
            LootTableSource source, RegistryWrapper.WrapperLookup registries) {
        if (!source.isBuiltin()) {
            return;
        }

        if (BASTION_CHESTS.contains(key) || key.equals(LootTables.NETHER_BRIDGE_CHEST)) {
            tableBuilder
                    .pool(hornPool(UniformLootNumberProvider.create(CHANCE_MIN_ZERO, CHANCE_MAX_ONE), DEFAULT_WEIGHT));
            return;
        }

        if (VILLAGE_CHESTS.contains(key)) {
            tableBuilder
                    .pool(hornPool(UniformLootNumberProvider.create(CHANCE_MIN_ZERO, CHANCE_MAX_ONE), DEFAULT_WEIGHT));
            return;
        }

        if (key.equals(LootTables.DESERT_PYRAMID_CHEST)) {
            tableBuilder
                    .pool(hornPool(UniformLootNumberProvider.create(CHANCE_MIN_ZERO, CHANCE_MAX_ONE), DEFAULT_WEIGHT));
            return;
        }

        if (key.equals(LootTables.SIMPLE_DUNGEON_CHEST)) {
            tableBuilder.pool(hornPool(ConstantLootNumberProvider.create(GUARANTEED_ROLL), DEFAULT_WEIGHT));
            return;
        }

        if (key.equals(LootTables.END_CITY_TREASURE_CHEST)) {
            tableBuilder
                    .pool(hornPool(UniformLootNumberProvider.create(CHANCE_MIN_ZERO, CHANCE_MAX_ONE), DEFAULT_WEIGHT));
            return;
        }

        if (key.equals(LootTables.ANCIENT_CITY_CHEST)) {
            tableBuilder
                    .pool(hornPool(UniformLootNumberProvider.create(CHANCE_MIN_ZERO, CHANCE_MAX_TWO), DEFAULT_WEIGHT));
            return;
        }

        if (key.equals(LootTables.WOODLAND_MANSION_CHEST)) {
            tableBuilder
                    .pool(hornPool(UniformLootNumberProvider.create(CHANCE_MIN_ZERO, CHANCE_MAX_ONE), DEFAULT_WEIGHT));
        }
    }

    private static LootPool.Builder hornPool(LootNumberProvider rolls, int weight) {
        return LootPool.builder()
                .rolls(rolls)
                .with(ItemEntry.builder(UnicornMod.UNICORN_HORN).weight(weight));
    }
}