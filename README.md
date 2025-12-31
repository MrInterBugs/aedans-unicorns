# Aedans Unicorns (Fabric)

A Fabric 1.21.1 mod that gives horses a dedicated **Unicorn Horn** slot, lets the horn render on their head, and introduces the **Potion of Unicorn Vitality** line.

## Gameplay

- **Unicorn Horn**: rare loot in bastions, nether fortresses, smith/village chests, desert pyramids, dungeons, end/ancient cities, and woodland mansions. Lives in the Combat tab.
- **Horse horn slot**: appears under the armor slot. Shift-click a horn to equip; it renders on the horse’s head.
- **Totem effect for horses**: a slotted horn saves the horse from death (except void), clearing effects and granting Regeneration, Absorption, and Fire Resistance.
- **Potion of Unicorn Vitality** (Absorption II for 2:00): brew Awkward + Unicorn Horn. Convert to splash (Gunpowder), lingering (Dragon’s Breath or Awkward Lingering + Horn), and tipped arrows (lingering potion + arrows for 8). Custom containers show in Food & Drink; vanilla potion variants are hidden.
- **REI integration**: supplies custom brewing and arrow recipes while hiding misleading vanilla permutations (optional but supported).

## Building

Prereqs: JDK 21, bundled Gradle 9.2.1, and one-time internet to grab deps.

```sh
./gradlew build
```

Outputs go to `build/libs`.

## Source map

- Main entrypoint and registries: `src/main/java/uk/mrinterbugs/unicorn/UnicornMod.java`
- Horse horn slot/totem logic: mixins under `src/main/java/uk/mrinterbugs/unicorn/mixin/`
- Client rendering + UI slot background: `src/client/java/uk/mrinterbugs/unicorn/client/` and `.../mixin/`
- REI plugin: `src/main/java/uk/mrinterbugs/unicorn/client/UnicornReiPlugin.java`
- Data/assets (recipes, models, lang): `src/main/resources/assets/unicorn/` and `src/main/resources/data/unicorn/`

## Tweaking

- Version bumps: edit `gradle.properties`.
- Mod id/name/metadata: `src/main/resources/fabric.mod.json`.
- Replace the horn texture at `src/main/resources/assets/unicorn/textures/item/unicorn_horn.png`.
