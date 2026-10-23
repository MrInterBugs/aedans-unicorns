package uk.mrinterbugs.unicorn.client;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
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
    private static final Identifier PLUGIN_UID = Identifier.fromNamespaceAndPath(UnicornMod.MOD_ID, "jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IVanillaRecipeFactory factory = registration.getVanillaRecipeFactory();

        ItemStack awkward = PotionContents.createItemStack(Items.POTION, Potions.AWKWARD);
        ItemStack horn = new ItemStack(UnicornMod.UNICORN_HORN);
        ItemStack potion = UnicornPotions.createUnicornPotionStack(UnicornMod.UNICORN_HEART_POTION_ITEM);
        ItemStack splash = UnicornPotions.createUnicornPotionStack(UnicornMod.UNICORN_HEART_SPLASH_POTION_ITEM);
        ItemStack lingering = UnicornPotions.createUnicornPotionStack(UnicornMod.UNICORN_HEART_LINGERING_POTION_ITEM);

        List<IJeiBrewingRecipe> recipes = List.of(
            factory.createBrewingRecipe(List.of(horn), awkward, potion,
                id("brew_potion")),
            factory.createBrewingRecipe(List.of(new ItemStack(Items.GUNPOWDER)), potion, splash,
                id("brew_splash")),
            factory.createBrewingRecipe(List.of(new ItemStack(Items.DRAGON_BREATH)), splash, lingering,
                id("brew_lingering"))
        );

        registration.addRecipes(RecipeTypes.BREWING, recipes);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(UnicornMod.MOD_ID, path);
    }
}
