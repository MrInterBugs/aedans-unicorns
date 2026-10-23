# 26.1.2 migration fixes

This records the fixes applied after reviewing `cb05322` on the `26.1.2` branch.

## Fixed

- **Horn inventory and chest resizing:** moved the horn into a dedicated one-slot inventory. Adding a chest to a donkey/mule no longer moves the horn into cargo, and adding a horn no longer changes vanilla cargo sizes or player-inventory boundaries.
- **Immediate synchronization:** inventory mutations update tracked horn data immediately. Removing a horn no longer leaves a stale server-side horn available for death protection until the next tick.
- **Shift-click transfers:** explicitly handle equipment, cargo, main inventory, hotbar, and the appended horn slot. Ordinary items move to the correct inventory section; horn removal includes all 36 player slots and does not target the horn slot itself.
- **Save compatibility and death drops:** save all equipped horns under `UnicornHornItem`; migrate the extra chest-inventory slot used by this branch's earlier 26.1.2 build. Forced death drops the separately stored horn once.
- **Death protection:** intercept `checkTotemDeathProtection` instead of cancelling `die` after death sounds have played. Respect damage tagged `bypasses_invulnerability`, including `/kill` and the void.
- **Drinkable potions:** register the consumable and glass-bottle remainder components. Drinking applies Absorption II and returns an empty bottle.
- **Potion duration scales:** restore vanilla duration scaling for lingering potions (one quarter) and tipped arrows (one eighth).
- **Brewing versus crafting:** potion creation and conversions belong exclusively to the brewing stand. Remove the obsolete shapeless potion-crafting recipes, including Awkward Lingering Potion + Horn, and their crafting unlocks. Keep only the eight tipped arrows recipe in the crafting table, migrated to the current resource directory/format with a potion-component ingredient. JEI lists the valid brewing paths, including Awkward splash/lingering potion + horn. Remove the invalid horn recipe containing only a result.
- **Horse screen:** restore the horn slot frame and empty-slot outline, placing the slot below the horse's other equipment.
- **Horn appearance:** restore full-bright rendering and use a tapered 3D model for equipped horns. The flat inventory icon stays unchanged; the equipped horn remains visible from the side and is placed on the forehead.
- **Held horn appearance:** restore the original texture after the alpha-only approach caused an invisible held item. Use the tapered 3D horn model with dedicated first-/third-person transforms for hands; keep the original flat inventory icon and the horse's equipped model. Checked existing and fresh horn stacks in the 26.1.2 client.
- **Creative search duplicates and names:** remove vanilla unicorn potion/arrow variants from both normal-tab and search-tab lists, keeping exactly one custom entry per variant. Correct the Food & Drinks tab identifier so custom potions are actually added there. Add vanilla unicorn-effect name translations for items already in inventories.
- **JEI brewing duplicates and false ingredients:** distinguish custom potion items by their potion-content component. Hide auto-generated unicorn brewing permutations in favor of the five explicit valid recipes. Reject unrelated vanilla effect ingredients in unicorn bottles and keep invalid/no-op legacy mixes unchanged; standard vanilla brewing is preserved.
- **Portable build:** replace the hard-coded desktop JEI jar path with Maven API/runtime dependencies. Remove the disabled REI entrypoint and outdated optional-mod recommendations.
- **Release workflow:** build with Java 25 and publish Minecraft 26.1.2 compatibility instead of 1.21.1.

## Automated checks

`./gradlew build` runs 19 mod GameTests plus Minecraft's built-in sanity test. They cover lethal damage, immediate horn removal, chest attachment, ordinary and horn shift-clicks, cargo transfers, brewing, horse/donkey saving and reloading, legacy chest saves, drinking and bottle remainders, duration scales, arrow crafting and rejection of crafting-table potion conversions, `/kill`, and death drops.

The GameTest report is written to `build/reports/gametest/results.xml`. Logs are in `build/run/gameTest/logs/`.

## Client validation

Loaded the built jar in the existing Prism Launcher 26.1.2 instance with Fabric Loader 0.20.1 and its installed Sodium, Iris, Lithium, EntityCulling, ImmediatelyFast, and Xaero mods. Also launched with JEI 29.6.2.31. The live JEI recipe lookup confirmed one regular-potion recipe, two splash recipes, and two lingering recipes after filtering seven auto-generated permutations. Checked the horse inventory frame/icon and the equipped horn's appearance from the front and side. Testing used a copy of `Test World` named `Unicorn Regression`; the temporary visual fixture is not part of the mod.

## Limits

Dynamic lighting from the optional RyoamicLights integration remains disabled. Full-bright rendering makes the horn itself bright; it does not illuminate nearby blocks. This change does not claim that all gameplay or third-party-mod combinations have been tested.
