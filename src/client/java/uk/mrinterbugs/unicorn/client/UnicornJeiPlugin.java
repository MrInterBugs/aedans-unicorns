package uk.mrinterbugs.unicorn.client;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import uk.mrinterbugs.unicorn.UnicornMod;
import uk.mrinterbugs.unicorn.UnicornPotions;

import java.util.List;

@JeiPlugin
public class UnicornJeiPlugin implements IModPlugin {
    private static final Identifier PLUGIN_UID =
            Identifier.fromNamespaceAndPath(UnicornMod.MOD_ID, "jei_plugin");

    private java.util.Set<Identifier> registeredRecipes = java.util.Set.of();

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        for (var item : UnicornPotions.UNICORN_POTION_ITEMS) {
            registration.registerFromDataComponentTypes(item, DataComponents.POTION_CONTENTS);
        }
    }

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IVanillaRecipeFactory factory = registration.getVanillaRecipeFactory();

        ItemStack awkward = PotionContents.createItemStack(Items.POTION, Potions.AWKWARD);
        ItemStack horn = new ItemStack(UnicornMod.UNICORN_HORN);
        ItemStack potion =
                UnicornPotions.createUnicornPotionStack(UnicornMod.UNICORN_HEART_POTION_ITEM);
        ItemStack splash =
                UnicornPotions.createUnicornPotionStack(
                        UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM);
        ItemStack lingering =
                UnicornPotions.createUnicornPotionStack(
                        UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM);

        List<IJeiBrewingRecipe> recipes =
                List.of(
                        factory.createBrewingRecipe(
                                List.of(horn), awkward, potion, id("brew_potion")),
                        factory.createBrewingRecipe(
                                List.of(horn),
                                PotionContents.createItemStack(
                                        Items.SPLASH_POTION, Potions.AWKWARD),
                                splash,
                                id("brew_awkward_splash")),
                        factory.createBrewingRecipe(
                                List.of(horn),
                                PotionContents.createItemStack(
                                        Items.LINGERING_POTION, Potions.AWKWARD),
                                lingering,
                                id("brew_awkward_lingering")),
                        factory.createBrewingRecipe(
                                List.of(new ItemStack(Items.GUNPOWDER)),
                                potion,
                                splash,
                                id("brew_splash")),
                        factory.createBrewingRecipe(
                                List.of(new ItemStack(Items.DRAGON_BREATH)),
                                splash,
                                lingering,
                                id("brew_lingering")));

        registeredRecipes =
                recipes.stream()
                        .map(IJeiBrewingRecipe::getUid)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet());
        registration.addRecipes(RecipeTypes.BREWING, recipes);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        var manager = runtime.getRecipeManager();
        var misleading =
                manager.createRecipeLookup(RecipeTypes.BREWING)
                        .includeHidden()
                        .get()
                        .filter(recipe -> !registeredRecipes.contains(recipe.getUid()))
                        .filter(
                                recipe ->
                                        touchesUnicorn(recipe.getPotionOutput())
                                                || recipe.getPotionInputs().stream()
                                                        .anyMatch(UnicornJeiPlugin::touchesUnicorn)
                                                || recipe.getIngredients().stream()
                                                        .anyMatch(UnicornJeiPlugin::touchesUnicorn))
                        .toList();
        if (!misleading.isEmpty()) manager.hideRecipes(RecipeTypes.BREWING, misleading);
        var ingredients = runtime.getIngredientManager();
        var invalidStacks =
                ingredients.getAllIngredients(VanillaTypes.ITEM_STACK).stream()
                        .filter(
                                stack ->
                                        (UnicornPotions.isUnicornPotionItem(stack.getItem())
                                                        && !UnicornPotions.matchesUnicornPotion(
                                                                stack))
                                                || (UnicornPotions.UNICORN_CONTAINER_SWAP
                                                                .containsKey(stack.getItem())
                                                        && UnicornPotions.matchesUnicornPotion(
                                                                stack)))
                        .toList();
        if (!invalidStacks.isEmpty())
            ingredients.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, invalidStacks);
        var visible = manager.createRecipeLookup(RecipeTypes.BREWING).get().toList();
        org.slf4j.LoggerFactory.getLogger(UnicornJeiPlugin.class)
                .info(
                        "Unicorn JEI brewing recipes: drink={}, splash={}, lingering={}; hid {}"
                                + " generated recipes",
                        visible.stream()
                                .filter(
                                        r ->
                                                r.getPotionOutput()
                                                        .is(UnicornMod.UNICORN_HEART_POTION_ITEM))
                                .count(),
                        visible.stream()
                                .filter(
                                        r ->
                                                r.getPotionOutput()
                                                        .is(
                                                                UnicornMod
                                                                        .UNICORN_HEART_SPLASH_POTION_ITEM))
                                .count(),
                        visible.stream()
                                .filter(
                                        r ->
                                                r.getPotionOutput()
                                                        .is(
                                                                UnicornMod
                                                                        .UNICORN_HEART_LINGERING_POTION_ITEM))
                                .count(),
                        misleading.size());
    }

    private static boolean touchesUnicorn(ItemStack stack) {
        return UnicornPotions.isUnicornPotionItem(stack.getItem())
                || UnicornPotions.matchesUnicornPotion(stack);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(UnicornMod.MOD_ID, path);
    }
}
