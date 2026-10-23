package uk.mrinterbugs.unicorn;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import uk.mrinterbugs.unicorn.mixin.AbstractHorseInventoryAccessor;

public class UnicornGameTests {
    @GameTest
    public void equippedHornSavesHorse(GameTestHelper h) {
        Horse horse = h.spawn(EntityType.HORSE, 1, 2, 1);
        UnicornHornHolder holder = (UnicornHornHolder) horse;
        holder.unicorn$setHornStack(new ItemStack(UnicornMod.UNICORN_HORN));
        horse.hurtServer(h.getLevel(), horse.damageSources().generic(), 1000);
        h.assertTrue(horse.isAlive(), "Horse should survive lethal damage");
        h.assertTrue(holder.unicorn$getHornStack().isEmpty(), "Horn should be consumed");
        h.succeed();
    }

    @GameTest
    public void removedHornStopsProtectingImmediately(GameTestHelper h) {
        Horse horse = h.spawn(EntityType.HORSE, 1, 2, 1);
        UnicornHornHolder holder = (UnicornHornHolder) horse;
        holder.unicorn$setHornStack(new ItemStack(UnicornMod.UNICORN_HORN));
        var inventory = holder.unicorn$getHornInventory();
        inventory.setItem(0, ItemStack.EMPTY);
        h.assertTrue(
                holder.unicorn$getHornStack().isEmpty(),
                "Removed horn is still returned by server getter until next aiStep");
        h.succeed();
    }

    @GameTest
    public void addingChestKeepsHornEquipped(GameTestHelper h) throws Exception {
        var donkey = h.spawn(EntityType.DONKEY, 1, 2, 1);
        var holder = (UnicornHornHolder) donkey;
        holder.unicorn$setHornStack(new ItemStack(UnicornMod.UNICORN_HORN));
        donkey.setChest(true);
        var method = AbstractHorse.class.getDeclaredMethod("createInventory");
        method.setAccessible(true);
        method.invoke(donkey);
        var inventory = ((AbstractHorseInventoryAccessor) donkey).getInventory();
        h.assertTrue(
                holder.unicorn$getHornStack().is(UnicornMod.UNICORN_HORN),
                "Adding chest unequipped the horn");
        h.assertTrue(inventory.getItem(0).isEmpty(), "Adding chest moved horn into cargo");
        h.succeed();
    }

    @GameTest
    public void ordinaryShiftClickMovesToHotbar(GameTestHelper h) {
        var horse = h.spawn(EntityType.HORSE, 1, 2, 1);
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var inventory = ((AbstractHorseInventoryAccessor) horse).getInventory();
        var menu = new HorseInventoryMenu(1, player.getInventory(), inventory, horse, 0);
        player.getInventory().setItem(10, new ItemStack(Items.DIRT));
        int menuIndex = -1;
        for (int i = 0; i < menu.slots.size(); i++)
            if (menu.slots.get(i).container == player.getInventory()
                    && menu.slots.get(i).getContainerSlot() == 10) menuIndex = i;
        menu.quickMoveStack(player, menuIndex);
        boolean inHotbar = false;
        for (int i = 0; i < 9; i++)
            if (player.getInventory().getItem(i).is(Items.DIRT)) inHotbar = true;
        h.assertTrue(
                inHotbar, "Shift-click dirt goes into first main-inventory slot instead of hotbar");
        h.succeed();
    }

    @GameTest
    public void brewingAllVariants(GameTestHelper h) {
        var brewing = h.getLevel().potionBrewing();
        var awkward =
                net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                        Items.POTION, net.minecraft.world.item.alchemy.Potions.AWKWARD);
        var horn = new ItemStack(UnicornMod.UNICORN_HORN);
        h.assertTrue(brewing.hasMix(awkward, horn), "Awkward + horn should be brewable");
        var potion = brewing.mix(horn, awkward);
        h.assertTrue(potion.is(UnicornMod.UNICORN_HEART_POTION_ITEM), "Wrong potion output");
        var powder = new ItemStack(Items.GUNPOWDER);
        h.assertTrue(brewing.hasMix(potion, powder), "Potion + gunpowder should be brewable");
        var splash = brewing.mix(powder, potion);
        h.assertTrue(splash.is(UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM), "Wrong splash output");
        var breath = new ItemStack(Items.DRAGON_BREATH);
        h.assertTrue(brewing.hasMix(splash, breath), "Splash + breath should be brewable");
        var lingering = brewing.mix(breath, splash);
        h.assertTrue(
                lingering.is(UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM),
                "Wrong lingering output");
        h.assertTrue(
                uk.mrinterbugs.unicorn.UnicornPotions.matchesUnicornPotion(lingering),
                "Lost potion contents");
        h.succeed();
    }

    @GameTest
    public void horseSaveReload(GameTestHelper h) {
        var horse = h.spawn(EntityType.HORSE, 1, 2, 1);
        ((UnicornHornHolder) horse).unicorn$setHornStack(new ItemStack(UnicornMod.UNICORN_HORN));
        var output =
                net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                        net.minecraft.util.ProblemReporter.DISCARDING,
                        h.getLevel().registryAccess());
        horse.saveWithoutId(output);
        var clone =
                EntityType.HORSE.create(
                        h.getLevel(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        clone.load(
                net.minecraft.world.level.storage.TagValueInput.create(
                        net.minecraft.util.ProblemReporter.DISCARDING,
                        h.getLevel().registryAccess(),
                        output.buildResult()));
        h.assertTrue(
                ((UnicornHornHolder) clone).unicorn$getHornStack().is(UnicornMod.UNICORN_HORN),
                "Horse lost horn on reload");
        h.succeed();
    }

    @GameTest
    public void chestedDonkeySaveReload(GameTestHelper h) throws Exception {
        var donkey = h.spawn(EntityType.DONKEY, 1, 2, 1);
        donkey.setChest(true);
        var method = AbstractHorse.class.getDeclaredMethod("createInventory");
        method.setAccessible(true);
        method.invoke(donkey);
        ((UnicornHornHolder) donkey).unicorn$setHornStack(new ItemStack(UnicornMod.UNICORN_HORN));
        var output =
                net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                        net.minecraft.util.ProblemReporter.DISCARDING,
                        h.getLevel().registryAccess());
        donkey.saveWithoutId(output);
        var clone =
                EntityType.DONKEY.create(
                        h.getLevel(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        clone.load(
                net.minecraft.world.level.storage.TagValueInput.create(
                        net.minecraft.util.ProblemReporter.DISCARDING,
                        h.getLevel().registryAccess(),
                        output.buildResult()));
        h.assertTrue(
                ((UnicornHornHolder) clone).unicorn$getHornStack().is(UnicornMod.UNICORN_HORN),
                "Donkey lost horn on reload");
        var inventory = ((UnicornHornHolder) clone).unicorn$getHornInventory();
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++)
            if (inventory.getItem(i).is(UnicornMod.UNICORN_HORN))
                count += inventory.getItem(i).getCount();
        h.assertTrue(count == 1, "Donkey duplicated horn on reload");
        h.succeed();
    }

    @GameTest
    public void drinkAppliesEffectAndReturnsBottle(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var potion = UnicornPotions.createUnicornPotionStack(UnicornMod.UNICORN_HEART_POTION_ITEM);
        h.assertTrue(
                potion.has(net.minecraft.core.component.DataComponents.CONSUMABLE),
                "Potion cannot be drunk");
        var bottle = potion.finishUsingItem(h.getLevel(), player);
        h.assertTrue(bottle.is(Items.GLASS_BOTTLE), "Drinking must return a bottle");
        h.assertTrue(
                player.hasEffect(net.minecraft.world.effect.MobEffects.ABSORPTION),
                "Drinking did not apply absorption");
        h.succeed();
    }

    @GameTest
    public void potionDurationScales(GameTestHelper h) {
        var arrow = UnicornPotions.createUnicornPotionStack(UnicornMod.UNICORN_HEART_TIPPED_ARROW);
        var lingering =
                UnicornPotions.createUnicornPotionStack(
                        UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM);
        h.assertTrue(
                arrow.getOrDefault(
                                net.minecraft.core.component.DataComponents.POTION_DURATION_SCALE,
                                1F)
                        == 0.125F,
                "Arrow duration must match vanilla");
        h.assertTrue(
                lingering.getOrDefault(
                                net.minecraft.core.component.DataComponents.POTION_DURATION_SCALE,
                                1F)
                        == 0.25F,
                "Lingering duration must match vanilla");
        h.succeed();
    }

    @GameTest
    public void killBypassesHorn(GameTestHelper h) {
        var horse = h.spawn(EntityType.HORSE, 1, 2, 1);
        var holder = (UnicornHornHolder) horse;
        holder.unicorn$setHornStack(new ItemStack(UnicornMod.UNICORN_HORN));
        horse.hurtServer(h.getLevel(), horse.damageSources().genericKill(), 1000);
        h.assertTrue(horse.isDeadOrDying(), "Kill damage must bypass horn protection");
        h.succeed();
    }

    @GameTest
    public void onlyArrowsAreCraftedAndPotionsMustBeBrewed(GameTestHelper h) {
        var recipes = h.getLevel().getServer().getRecipeManager();
        var awkward =
                net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                        Items.POTION, net.minecraft.world.item.alchemy.Potions.AWKWARD);
        var awkwardLingering =
                net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                        Items.LINGERING_POTION, net.minecraft.world.item.alchemy.Potions.AWKWARD);
        var pairs =
                java.util.List.of(
                        java.util.List.of(new ItemStack(UnicornMod.UNICORN_HORN), awkward),
                        java.util.List.of(new ItemStack(UnicornMod.UNICORN_HORN), awkwardLingering),
                        java.util.List.of(
                                new ItemStack(Items.GUNPOWDER),
                                UnicornPotions.createUnicornPotionStack(
                                        UnicornMod.UNICORN_HEART_POTION_ITEM)),
                        java.util.List.of(
                                new ItemStack(Items.DRAGON_BREATH),
                                UnicornPotions.createUnicornPotionStack(
                                        UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM)));
        for (var pair : pairs) {
            var input = net.minecraft.world.item.crafting.CraftingInput.of(2, 1, pair);
            h.assertTrue(
                    recipes.getRecipeFor(
                                    net.minecraft.world.item.crafting.RecipeType.CRAFTING,
                                    input,
                                    h.getLevel())
                            .isEmpty(),
                    "Potions must be brewed, not made in a crafting grid");
        }
        var arrowItems = new java.util.ArrayList<ItemStack>();
        for (int i = 0; i < 9; i++)
            arrowItems.add(
                    i == 4
                            ? UnicornPotions.createUnicornPotionStack(
                                    UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM)
                            : new ItemStack(Items.ARROW));
        var arrows = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, arrowItems);
        var arrowRecipe =
                recipes.getRecipeFor(
                        net.minecraft.world.item.crafting.RecipeType.CRAFTING,
                        arrows,
                        h.getLevel());
        h.assertTrue(arrowRecipe.isPresent(), "Tipped arrow crafting recipe missing");
        var arrowResult = arrowRecipe.orElseThrow().value().assemble(arrows);
        h.assertTrue(
                arrowResult.is(UnicornMod.UNICORN_HEART_TIPPED_ARROW)
                        && arrowResult.getCount() == 8
                        && UnicornPotions.matchesUnicornPotion(arrowResult),
                "Arrow craft must produce eight unicorn arrows with effects");
        h.succeed();
    }

    @GameTest
    public void legacyChestHornMigrates(GameTestHelper h) throws Exception {
        var donkey = h.spawn(EntityType.DONKEY, 1, 2, 1);
        donkey.setChest(true);
        var method = AbstractHorse.class.getDeclaredMethod("createInventory");
        method.setAccessible(true);
        method.invoke(donkey);
        var output =
                net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                        net.minecraft.util.ProblemReporter.DISCARDING,
                        h.getLevel().registryAccess());
        donkey.saveWithoutId(output);
        int legacySlot =
                ((AbstractHorseInventoryAccessor) donkey).getInventory().getContainerSize();
        output.list("Items", net.minecraft.world.ItemStackWithSlot.CODEC)
                .add(
                        new net.minecraft.world.ItemStackWithSlot(
                                legacySlot, new ItemStack(UnicornMod.UNICORN_HORN)));
        var clone =
                EntityType.DONKEY.create(
                        h.getLevel(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        clone.load(
                net.minecraft.world.level.storage.TagValueInput.create(
                        net.minecraft.util.ProblemReporter.DISCARDING,
                        h.getLevel().registryAccess(),
                        output.buildResult()));
        h.assertTrue(
                ((UnicornHornHolder) clone).unicorn$getHornStack().is(UnicornMod.UNICORN_HORN),
                "Legacy horn was lost");
        h.succeed();
    }

    @GameTest
    public void shiftClickHornBothWays(GameTestHelper h) {
        var horse = h.spawn(EntityType.HORSE, 1, 2, 1);
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var menu =
                new HorseInventoryMenu(
                        1,
                        player.getInventory(),
                        ((AbstractHorseInventoryAccessor) horse).getInventory(),
                        horse,
                        0);
        player.getInventory().setItem(9, new ItemStack(UnicornMod.UNICORN_HORN));
        menu.quickMoveStack(player, 2);
        h.assertTrue(
                ((UnicornHornHolder) horse).unicorn$getHornStack().is(UnicornMod.UNICORN_HORN),
                "Shift-click did not equip horn");
        for (int i = 0; i < 36; i++)
            player.getInventory().setItem(i, new ItemStack(Items.DIRT, 64));
        player.getInventory().setItem(9, ItemStack.EMPTY);
        menu.quickMoveStack(player, menu.slots.size() - 1);
        h.assertTrue(
                ((UnicornHornHolder) horse).unicorn$getHornStack().isEmpty(),
                "Shift-click did not remove horn");
        h.assertTrue(
                player.getInventory().getItem(9).is(UnicornMod.UNICORN_HORN),
                "First player slot was excluded from transfer");
        h.succeed();
    }

    @GameTest
    public void ordinaryChestTransfersPreserveCargoAndHorn(GameTestHelper h) throws Exception {
        var donkey = h.spawn(EntityType.DONKEY, 1, 2, 1);
        donkey.setChest(true);
        var method = AbstractHorse.class.getDeclaredMethod("createInventory");
        method.setAccessible(true);
        method.invoke(donkey);
        var holder = (UnicornHornHolder) donkey;
        holder.unicorn$setHornStack(new ItemStack(UnicornMod.UNICORN_HORN));
        var inventory = ((AbstractHorseInventoryAccessor) donkey).getInventory();
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var menu = new HorseInventoryMenu(1, player.getInventory(), inventory, donkey, 5);
        player.getInventory().setItem(9, new ItemStack(Items.DIRT, 32));
        menu.quickMoveStack(player, 17);
        h.assertTrue(
                inventory.getItem(0).is(Items.DIRT) && inventory.getItem(0).getCount() == 32,
                "Cargo transfer failed");
        menu.quickMoveStack(player, 2);
        h.assertTrue(inventory.getItem(0).isEmpty(), "Cargo removal failed");
        h.assertTrue(
                holder.unicorn$getHornStack().is(UnicornMod.UNICORN_HORN),
                "Cargo transfer touched horn");
        h.succeed();
    }

    @GameTest
    public void forcedDeathDropsExactlyOneHorn(GameTestHelper h) {
        var horse = h.spawn(EntityType.HORSE, 1, 2, 1);
        ((UnicornHornHolder) horse).unicorn$setHornStack(new ItemStack(UnicornMod.UNICORN_HORN));
        horse.hurtServer(h.getLevel(), horse.damageSources().genericKill(), 1000);
        var drops =
                h.getLevel()
                        .getEntitiesOfClass(
                                net.minecraft.world.entity.item.ItemEntity.class,
                                horse.getBoundingBox().inflate(2),
                                e -> e.getItem().is(UnicornMod.UNICORN_HORN));
        h.assertTrue(
                drops.stream().mapToInt(e -> e.getItem().getCount()).sum() == 1,
                "Death must drop exactly one horn");
        h.succeed();
    }

    @GameTest
    public void creativeSearchHasOnlyCustomUnicornVariants(GameTestHelper h) {
        net.minecraft.world.item.CreativeModeTabs.tryRebuildTabContents(
                h.getLevel().enabledFeatures(), true, h.getLevel().registryAccess());
        var search =
                net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB.getValue(
                        net.minecraft.resources.Identifier.withDefaultNamespace("search"));
        var items = search.getSearchTabDisplayItems();
        for (var custom :
                java.util.List.of(
                        UnicornMod.UNICORN_HEART_TIPPED_ARROW,
                        UnicornMod.UNICORN_HEART_POTION_ITEM,
                        UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM,
                        UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM)) {
            h.assertTrue(
                    items.stream()
                                    .filter(
                                            stack ->
                                                    stack.is(custom)
                                                            && UnicornPotions.matchesUnicornPotion(
                                                                    stack))
                                    .count()
                            == 1,
                    "Creative search must contain exactly one "
                            + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(custom)
                            + "; found "
                            + items.stream()
                                    .filter(
                                            stack ->
                                                    stack.is(custom)
                                                            && UnicornPotions.matchesUnicornPotion(
                                                                    stack))
                                    .count()
                            + " in "
                            + items.size()
                            + " search entries");
        }
        for (var tab : net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB) {
            h.assertTrue(
                    java.util.stream.Stream.concat(
                                    tab.getDisplayItems().stream(),
                                    tab.getSearchTabDisplayItems().stream())
                            .noneMatch(
                                    stack ->
                                            UnicornPotions.matchesUnicornPotion(stack)
                                                    && UnicornPotions.UNICORN_CONTAINER_SWAP
                                                            .containsKey(stack.getItem())),
                    "Vanilla unicorn potion or arrow leaked into a creative tab/search list");
        }
        h.succeed();
    }

    @GameTest
    public void vanillaAndCustomUnicornItemsHaveTranslations(GameTestHelper h) throws Exception {
        try (var input =
                UnicornGameTests.class.getResourceAsStream("/assets/unicorn/lang/en_us.json")) {
            var translations =
                    com.google.gson.JsonParser.parseReader(
                                    new java.io.InputStreamReader(
                                            java.util.Objects.requireNonNull(input),
                                            java.nio.charset.StandardCharsets.UTF_8))
                            .getAsJsonObject();
            var items =
                    new java.util.ArrayList<net.minecraft.world.item.Item>(
                            UnicornPotions.UNICORN_CONTAINER_SWAP.keySet());
            items.addAll(UnicornPotions.UNICORN_CONTAINER_SWAP.values());
            for (var item : items) {
                var name =
                        UnicornPotions.createUnicornPotionStack(item).getHoverName().getContents();
                h.assertTrue(
                        name instanceof net.minecraft.network.chat.contents.TranslatableContents,
                        "Unicorn item must have a translatable name");
                var key =
                        ((net.minecraft.network.chat.contents.TranslatableContents) name).getKey();
                h.assertTrue(translations.has(key), "Missing unicorn item translation: " + key);
            }
        }
        h.succeed();
    }

    @GameTest
    public void unrelatedIngredientsCannotBrewUnicornBottles(GameTestHelper h) {
        var brewing = h.getLevel().potionBrewing();
        for (var container :
                java.util.List.of(
                        UnicornMod.UNICORN_HEART_POTION_ITEM,
                        UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM,
                        UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM)) {
            for (var effect :
                    java.util.List.of(
                            net.minecraft.world.item.alchemy.Potions.WATER,
                            net.minecraft.world.item.alchemy.Potions.AWKWARD,
                            UnicornMod.UNICORN_HEART_POTION_ENTRY)) {
                var input =
                        net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                                container, effect);
                for (var item :
                        java.util.List.of(
                                Items.GLISTERING_MELON_SLICE,
                                Items.SLIME_BLOCK,
                                Items.REDSTONE,
                                Items.GLOWSTONE_DUST,
                                UnicornMod.UNICORN_HORN)) {
                    var ingredient = new ItemStack(item);
                    h.assertTrue(
                            !brewing.hasMix(input, ingredient),
                            "Unrelated unicorn-bottle recipe was accepted");
                    h.assertTrue(
                            ItemStack.matches(input, brewing.mix(ingredient, input)),
                            "Rejected recipe still changed the bottle");
                }
            }
        }
        h.succeed();
    }

    @GameTest
    public void failedVanillaMixDoesNotCreateUnicornRecipe(GameTestHelper h) {
        var input = UnicornPotions.createUnicornPotionStack(Items.POTION);
        var ingredient = new ItemStack(Items.SLIME_BLOCK);
        var brewing = h.getLevel().potionBrewing();
        h.assertTrue(!brewing.hasMix(input, ingredient), "Slime must not brew Unicorn Vitality");
        h.assertTrue(
                ItemStack.matches(input, brewing.mix(ingredient, input)),
                "A no-op mix changed a legacy vanilla bottle into a unicorn bottle");
        var awkwardSplash =
                net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                        Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.AWKWARD);
        var horn = new ItemStack(UnicornMod.UNICORN_HORN);
        h.assertTrue(brewing.hasMix(awkwardSplash, horn), "Awkward splash + horn should work");
        h.assertTrue(
                brewing.mix(horn, awkwardSplash).is(UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM),
                "Wrong awkward splash output");
        var awkwardLingering =
                net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                        Items.LINGERING_POTION, net.minecraft.world.item.alchemy.Potions.AWKWARD);
        h.assertTrue(
                brewing.hasMix(awkwardLingering, horn), "Awkward lingering + horn should work");
        h.assertTrue(
                brewing.mix(horn, awkwardLingering)
                        .is(UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM),
                "Wrong awkward lingering output");
        var ordinary =
                net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                        Items.POTION, net.minecraft.world.item.alchemy.Potions.AWKWARD);
        h.assertTrue(
                brewing.hasMix(ordinary, new ItemStack(Items.GLISTERING_MELON_SLICE)),
                "Ordinary vanilla brewing was changed");
        h.succeed();
    }
}
