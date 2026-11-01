# Aedans Unicorns (Fabric)

A Fabric mod for Minecraft **1.21.1 and 26.1.2** that gives horses a dedicated **Unicorn Horn** slot, lets the horn render on their head, and introduces the **Potion of Unicorn Vitality** line.

## Gameplay

- **Unicorn Horn**: rare loot in bastions, nether fortresses, smith/village chests, desert pyramids, dungeons, end/ancient cities, and woodland mansions. Lives in the Combat tab.
- **Horse horn slot**: appears beside the saddle slot on 1.21.1 and under the armor slot on 26.1.2. Shift-click a horn to equip; it renders on the horse’s head.
- **Totem effect for horses**: a slotted horn saves the horse from death, clearing effects and granting Regeneration, Absorption, and Fire Resistance.
- **Potion of Unicorn Vitality** (Absorption II for 2:00): brew Awkward + Unicorn Horn in a brewing stand. Convert to splash (Gunpowder), lingering (Dragon’s Breath or Awkward Lingering + Horn). Craft 8 tipped arrows using a lingering potion surrounded by arrows. Custom containers show in Food & Drink; vanilla potion variants are hidden.
- **Recipe viewers**: REI on 1.21.1 and JEI on 26.1.2 show the brewing and tipped-arrow recipes; optional at runtime.

## Building

Use the matching `1.21.1` or `26.1.2` branch to build. Prereqs: JDK 21 for 1.21.1 or JDK 25 for 26.1.2, bundled Gradle, and one-time internet to grab deps.

```sh
./gradlew build
```

Outputs go to `build/libs`.

