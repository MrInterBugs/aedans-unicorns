# 1.21.1 backport of the 26.1.2 fixes

Based on `origin/1.21.1` (`74277a7`), in branch `MrInterBugs/backport-1.21.1`.

## Backported fixes

- Store the horn in a dedicated one-slot inventory, separate from the saddle, armor, and chest cargo. Adding a chest no longer moves or unequips the horn.
- Synchronize horn state on inventory mutations and use the inventory as the authoritative server value. Removing a horn immediately removes its protection.
- Correct shift-click boundaries for equipment, cargo, all 36 player slots, and the horn slot.
- Save horns under `UnicornHornItem` for every horse type. Recover horns from the extra chest-inventory slot used by previous 1.21.1 versions, without duplicating them into cargo.
- Drop separately stored horns once on forced death. Respect `bypasses_invulnerability` damage, including `/kill` and the void.
- Filter vanilla unicorn potion and arrow variants out of both creative-tab and creative-search lists. Supply fallback translations for vanilla variants already in inventories.
- Keep potion creation and conversions exclusively in the brewing stand. Remove the obsolete potion-crafting definitions and invalid horn recipe. Migrate only the eight tipped arrows recipe and its unlock to the current resource directory/format, using a component ingredient to require the correct potion.
- Restrict unicorn bottles to the valid gunpowder/dragon-breath conversions. Reject unrelated vanilla effect ingredients and avoid turning no-op mixes of legacy vanilla unicorn bottles into fake conversion recipes.
- Let REI discover the real arrow recipe instead of adding a duplicate manual display. Keep all five valid brewing paths visible, including Awkward splash/lingering potion + horn.
- Render a tapered 3D horn on the forehead while retaining the inventory icon. On 1.21.1 this uses a custom-model-data override on a temporary render-only stack; saved/equipped stacks are unchanged.
- Position the horn slot below the other equipment and move its background to match.
- Register both environment source sets with Loom and remove the duplicate unremapped Fabric API client dependency so the development/GameTest runtime loads remapped dependencies correctly.

## Version-specific behavior retained

Minecraft 1.21.1 stays on Java 21 and Yarn mappings. The existing REI and optional RyoamicLights integrations remain enabled; it does not use the 26.1.2 JEI integration or client-item JSON format. Native 1.21.1 potion items already handle drinking, bottle returns, and lingering/arrow duration scaling, so the newer consumable/duration components are not copied into this version.

The release workflow already selects Java 21 and Minecraft 1.21.1 and retains those values.

## Validation

`./gradlew build` compiles the common/client code, packages remapped release/source jars, and runs 14 Minecraft GameTests on Java 21. Tests cover lethal damage, immediate removal, chest attachment, horse/donkey saves, old chest-slot migration, horn and ordinary shift-clicking, brewing, rejection of crafting-table potion conversions, eight-arrow crafting, drinking/bottle returns, `/kill`/death drops, creative-search duplicates, and item translation keys.

The report is `build/reports/gametest/results.xml`; logs are in `build/run/gameTest/logs/`. Client rendering has been compiled against 1.21.1 but has not been interactively checked in a 1.21.1 Prism instance. The existing 26.1.2 test installation is unaffected by the backport.
