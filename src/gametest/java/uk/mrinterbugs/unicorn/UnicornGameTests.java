package uk.mrinterbugs.unicorn;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.world.GameMode;

import uk.mrinterbugs.unicorn.mixin.AbstractHorseInventoryAccessor;

import java.util.ArrayList;
import java.util.List;

public class UnicornGameTests implements FabricGameTest {
    private static void equip(AbstractHorseEntity horse) {
        ((UnicornHornHolder) horse).unicorn$setHornStack(new ItemStack(UnicornMod.UNICORN_HORN));
    }

    private static Inventory cargo(AbstractHorseEntity horse) {
        return ((AbstractHorseInventoryAccessor) horse).getInventory();
    }

    private static void resize(AbstractHorseEntity horse) throws Exception {
        var method = AbstractHorseEntity.class.getDeclaredMethod("onChestedStatusChanged");
        method.setAccessible(true);
        method.invoke(horse);
    }

    private static ItemStack horn(AbstractHorseEntity horse) {
        return ((UnicornHornHolder) horse).unicorn$getHornStack();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void equippedHornSavesHorse(TestContext h) {
        var horse = h.spawnEntity(EntityType.HORSE, 1, 2, 1);
        equip(horse);
        horse.damage(horse.getDamageSources().generic(), 1000);
        h.assertTrue(horse.isAlive(), "Horn failed to save horse");
        h.assertTrue(horn(horse).isEmpty(), "Horn not consumed");
        h.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void removedHornStopsProtectingImmediately(TestContext h) {
        var horse = h.spawnEntity(EntityType.HORSE, 1, 2, 1);
        equip(horse);
        ((UnicornHornHolder) horse).unicorn$getHornInventory().setStack(0, ItemStack.EMPTY);
        h.assertTrue(horn(horse).isEmpty(), "Removed horn remains in server tracker");
        horse.damage(horse.getDamageSources().generic(), 1000);
        h.assertTrue(!horse.isAlive(), "Removed horn still protects horse");
        h.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void chestAttachmentPreservesHorn(TestContext h) throws Exception {
        var donkey = h.spawnEntity(EntityType.DONKEY, 1, 2, 1);
        equip(donkey);
        donkey.setHasChest(true);
        resize(donkey);
        h.assertTrue(horn(donkey).isOf(UnicornMod.UNICORN_HORN), "Adding chest unequipped horn");
        for (int i = 0; i < cargo(donkey).size(); i++)
            h.assertTrue(cargo(donkey).getStack(i).isEmpty(), "Horn moved into cargo");
        h.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void horseAndChestedDonkeySaveReload(TestContext h) throws Exception {
        var horse = h.spawnEntity(EntityType.HORSE, 1, 2, 1);
        equip(horse);
        var horseClone = EntityType.HORSE.create(h.getWorld());
        horseClone.readNbt(horse.writeNbt(new NbtCompound()));
        h.assertTrue(horn(horseClone).isOf(UnicornMod.UNICORN_HORN), "Horse lost horn on reload");
        var donkey = h.spawnEntity(EntityType.DONKEY, 2, 2, 1);
        donkey.setHasChest(true);
        resize(donkey);
        equip(donkey);
        var clone = EntityType.DONKEY.create(h.getWorld());
        clone.readNbt(donkey.writeNbt(new NbtCompound()));
        h.assertTrue(horn(clone).isOf(UnicornMod.UNICORN_HORN), "Donkey lost horn on reload");
        for (int i = 0; i < cargo(clone).size(); i++)
            h.assertTrue(
                    !cargo(clone).getStack(i).isOf(UnicornMod.UNICORN_HORN),
                    "Horn duplicated into cargo");
        h.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void legacyChestedHornMigrates(TestContext h) throws Exception {
        var donkey = h.spawnEntity(EntityType.DONKEY, 1, 2, 1);
        donkey.setHasChest(true);
        resize(donkey);
        var saved = donkey.writeNbt(new NbtCompound());
        var entry =
                (NbtCompound)
                        new ItemStack(UnicornMod.UNICORN_HORN)
                                .encode(h.getWorld().getRegistryManager());
        entry.putByte("Slot", (byte) cargo(donkey).size());
        var list = new NbtList();
        list.add(entry);
        saved.put("Items", list);
        var clone = EntityType.DONKEY.create(h.getWorld());
        clone.readNbt(saved);
        h.assertTrue(
                horn(clone).isOf(UnicornMod.UNICORN_HORN), "Old chest-slot horn did not migrate");
        h.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void shiftClickHornBothWays(TestContext h) {
        var horse = h.spawnEntity(EntityType.HORSE, 1, 2, 1);
        var player = h.createMockPlayer(GameMode.SURVIVAL);
        var menu = new HorseScreenHandler(1, player.getInventory(), cargo(horse), horse, 0);
        player.getInventory().setStack(9, new ItemStack(UnicornMod.UNICORN_HORN));
        menu.quickMove(player, menu.slots.size() - 1 - 36);
        h.assertTrue(horn(horse).isOf(UnicornMod.UNICORN_HORN), "Shift-click failed to equip horn");
        for (int i = 0; i < 36; i++)
            player.getInventory().setStack(i, new ItemStack(Items.DIRT, 64));
        player.getInventory().setStack(9, ItemStack.EMPTY);
        menu.quickMove(player, menu.slots.size() - 1);
        h.assertTrue(horn(horse).isEmpty(), "Shift-click failed to remove horn");
        h.assertTrue(
                player.getInventory().getStack(9).isOf(UnicornMod.UNICORN_HORN),
                "First player slot was excluded");
        h.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void ordinaryShiftClickUsesHotbar(TestContext h) {
        var horse = h.spawnEntity(EntityType.HORSE, 1, 2, 1);
        var player = h.createMockPlayer(GameMode.SURVIVAL);
        var menu = new HorseScreenHandler(1, player.getInventory(), cargo(horse), horse, 0);
        player.getInventory().setStack(10, new ItemStack(Items.DIRT));
        menu.quickMove(player, menu.slots.size() - 1 - 36 + 1);
        boolean moved = false;
        for (int i = 0; i < 9; i++) moved |= player.getInventory().getStack(i).isOf(Items.DIRT);
        h.assertTrue(moved, "Ordinary shift-click did not move to hotbar");
        h.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void brewingAllVariants(TestContext h) {
        var brewing = h.getWorld().getBrewingRecipeRegistry();
        var awkward = UnicornPotions.createPotionStack(Items.POTION, Potions.AWKWARD);
        var ingredient = new ItemStack(UnicornMod.UNICORN_HORN);
        h.assertTrue(brewing.hasRecipe(awkward, ingredient), "Horn brewing recipe absent");
        var potion = brewing.craft(ingredient, awkward);
        h.assertTrue(potion.isOf(UnicornMod.UNICORN_HEART_POTION_ITEM), "Wrong potion output");
        var splash = brewing.craft(new ItemStack(Items.GUNPOWDER), potion);
        h.assertTrue(
                splash.isOf(UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM), "Wrong splash output");
        var lingering = brewing.craft(new ItemStack(Items.DRAGON_BREATH), splash);
        h.assertTrue(
                lingering.isOf(UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM),
                "Wrong lingering output");
        h.assertTrue(UnicornPotions.matchesUnicornPotion(lingering), "Brewing lost potion effects");
        var awkwardLingering =
                UnicornPotions.createPotionStack(Items.LINGERING_POTION, Potions.AWKWARD);
        h.assertTrue(
                brewing.craft(ingredient, awkwardLingering)
                        .isOf(UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM),
                "Awkward lingering horn brew failed");
        h.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void potionsAreBrewedAndOnlyArrowsCrafted(TestContext h) {
        var recipes = h.getWorld().getRecipeManager();
        for (var pair :
                List.of(
                        List.of(
                                new ItemStack(UnicornMod.UNICORN_HORN),
                                UnicornPotions.createPotionStack(Items.POTION, Potions.AWKWARD)),
                        List.of(
                                new ItemStack(UnicornMod.UNICORN_HORN),
                                UnicornPotions.createPotionStack(
                                        Items.LINGERING_POTION, Potions.AWKWARD)),
                        List.of(
                                new ItemStack(Items.GUNPOWDER),
                                UnicornPotions.createUnicornPotionStack(
                                        UnicornMod.UNICORN_HEART_POTION_ITEM)),
                        List.of(
                                new ItemStack(Items.DRAGON_BREATH),
                                UnicornPotions.createUnicornPotionStack(
                                        UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM)))) {
            h.assertTrue(
                    recipes.getFirstMatch(
                                    RecipeType.CRAFTING,
                                    CraftingRecipeInput.create(2, 1, pair),
                                    h.getWorld())
                            .isEmpty(),
                    "Potion conversion leaked into crafting");
        }
        var items = new ArrayList<ItemStack>();
        for (int i = 0; i < 9; i++)
            items.add(
                    i == 4
                            ? UnicornPotions.createUnicornPotionStack(
                                    UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM)
                            : new ItemStack(Items.ARROW));
        var input = CraftingRecipeInput.create(3, 3, items);
        var recipe = recipes.getFirstMatch(RecipeType.CRAFTING, input, h.getWorld());
        h.assertTrue(recipe.isPresent(), "Arrow crafting recipe absent");
        var arrows = recipe.orElseThrow().value().craft(input, h.getWorld().getRegistryManager());
        h.assertTrue(
                arrows.isOf(UnicornMod.UNICORN_HEART_TIPPED_ARROW)
                        && arrows.getCount() == 8
                        && UnicornPotions.matchesUnicornPotion(arrows),
                "Wrong arrow craft output");
        h.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void potionDrinksAndReturnsBottle(TestContext h) {
        var player = h.createMockPlayer(GameMode.SURVIVAL);
        var potion = UnicornPotions.createUnicornPotionStack(UnicornMod.UNICORN_HEART_POTION_ITEM);
        var bottle = potion.finishUsing(h.getWorld(), player);
        h.assertTrue(bottle.isOf(Items.GLASS_BOTTLE), "Drinking did not return bottle");
        h.assertTrue(
                player.hasStatusEffect(StatusEffects.ABSORPTION),
                "Drinking did not apply absorption");
        h.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void killBypassesProtectionAndDropsHorn(TestContext h) {
        var horse = h.spawnEntity(EntityType.HORSE, 1, 2, 1);
        equip(horse);
        horse.damage(horse.getDamageSources().genericKill(), 1000);
        h.assertTrue(!horse.isAlive(), "Kill damage must bypass horn");
        var drops =
                h.getWorld()
                        .getEntitiesByClass(
                                net.minecraft.entity.ItemEntity.class,
                                horse.getBoundingBox().expand(2),
                                e -> e.getStack().isOf(UnicornMod.UNICORN_HORN));
        h.assertTrue(
                drops.stream().mapToInt(e -> e.getStack().getCount()).sum() == 1,
                "Forced death must drop exactly one horn");
        h.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void creativeSearchContainsOnlyCustomVariants(TestContext h) {
        ItemGroups.updateDisplayContext(
                h.getWorld().getEnabledFeatures(), true, h.getWorld().getRegistryManager());
        var search = Registries.ITEM_GROUP.get(ItemGroups.SEARCH);
        for (Item custom : UnicornPotions.UNICORN_POTION_ITEMS) {
            h.assertTrue(
                    search.getSearchTabStacks().stream()
                                    .filter(
                                            stack ->
                                                    stack.isOf(custom)
                                                            && UnicornPotions.matchesUnicornPotion(
                                                                    stack))
                                    .count()
                            == 1,
                    "Duplicate or absent custom creative item");
        }
        for (var group : Registries.ITEM_GROUP) {
            h.assertTrue(
                    java.util.stream.Stream.concat(
                                    group.getDisplayStacks().stream(),
                                    group.getSearchTabStacks().stream())
                            .noneMatch(
                                    stack ->
                                            UnicornPotions.matchesUnicornPotion(stack)
                                                    && UnicornPotions.UNICORN_CONTAINER_SWAP
                                                            .containsKey(stack.getItem())),
                    "Vanilla unicorn variant leaked into creative search");
        }
        h.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void unicornNamesHaveTranslations(TestContext h) throws Exception {
        try (var input =
                UnicornGameTests.class.getResourceAsStream("/assets/unicorn/lang/en_us.json")) {
            var translations =
                    com.google.gson.JsonParser.parseReader(
                                    new java.io.InputStreamReader(
                                            java.util.Objects.requireNonNull(input),
                                            java.nio.charset.StandardCharsets.UTF_8))
                            .getAsJsonObject();
            var items = new ArrayList<Item>(UnicornPotions.UNICORN_CONTAINER_SWAP.keySet());
            items.addAll(UnicornPotions.UNICORN_POTION_ITEMS);
            for (var item : items) {
                var text = UnicornPotions.createUnicornPotionStack(item).getName().getContent();
                h.assertTrue(
                        text instanceof net.minecraft.text.TranslatableTextContent,
                        "Unicorn item name must be translatable");
                var key = ((net.minecraft.text.TranslatableTextContent) text).getKey();
                h.assertTrue(translations.has(key), "Missing translation: " + key);
            }
        }
        h.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void rejectUnrelatedBrewingAndNoOpConversions(TestContext h) {
        var brewing = h.getWorld().getBrewingRecipeRegistry();
        for (var container :
                List.of(
                        UnicornMod.UNICORN_HEART_POTION_ITEM,
                        UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM,
                        UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM)) {
            for (var effect :
                    List.of(
                            Potions.WATER,
                            Potions.AWKWARD,
                            UnicornMod.UNICORN_HEART_POTION_ENTRY)) {
                var input = UnicornPotions.createPotionStack(container, effect);
                for (var item :
                        List.of(
                                Items.GLISTERING_MELON_SLICE,
                                Items.SLIME_BLOCK,
                                Items.REDSTONE,
                                Items.GLOWSTONE_DUST,
                                UnicornMod.UNICORN_HORN)) {
                    var ingredient = new ItemStack(item);
                    h.assertTrue(
                            !brewing.hasRecipe(input, ingredient),
                            "Unrelated unicorn-bottle recipe accepted");
                    h.assertTrue(
                            ItemStack.areEqual(input, brewing.craft(ingredient, input)),
                            "Rejected recipe changed bottle");
                }
            }
        }
        var legacy = UnicornPotions.createUnicornPotionStack(Items.POTION);
        var slime = new ItemStack(Items.SLIME_BLOCK);
        h.assertTrue(
                !brewing.hasRecipe(legacy, slime)
                        && ItemStack.areEqual(legacy, brewing.craft(slime, legacy)),
                "Invalid legacy-bottle mix created a recipe");
        var ordinary = UnicornPotions.createPotionStack(Items.POTION, Potions.AWKWARD);
        h.assertTrue(
                brewing.hasRecipe(ordinary, new ItemStack(Items.GLISTERING_MELON_SLICE)),
                "Ordinary vanilla brewing changed");
        h.complete();
    }
}
