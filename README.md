# Aedans Unicorns (Fabric)

A Fabric 1.21.1 mod that gives horses a dedicated **Unicorn Horn** slot, lets the horn render on their head, and introduces the **Potion of Unicorn Vitality** line.

## Gameplay

- **Unicorn Horn**: rare loot in bastions, nether fortresses, smith/village chests, desert pyramids, dungeons, end/ancient cities, and woodland mansions. Lives in the Combat tab.
- **Horse horn slot**: appears under the armor slot. Shift-click a horn to equip; it renders on the horse’s head.
- **Totem effect for horses**: a slotted horn saves the horse from death, clearing effects and granting Regeneration, Absorption, and Fire Resistance.
- **Potion of Unicorn Vitality** (Absorption II for 2:00): brew Awkward + Unicorn Horn. Convert to splash (Gunpowder), lingering (Dragon’s Breath or Awkward Lingering + Horn), and tipped arrows (lingering potion + arrows for 8). Custom containers show in Food & Drink; vanilla potion variants are hidden.
- **REI integration**: supplies custom brewing and arrow recipes while hiding misleading vanilla permutations (optional but supported).

## Building

Prereqs: JDK 21, bundled Gradle 9.2.1, and one-time internet to grab deps.

```sh
./gradlew build
```

Outputs go to `build/libs`.


## Backport validation

This branch includes the applicable 26.1.2 fixes adapted to Minecraft 1.21.1. See [CHANGELOG-1.21.1.md](CHANGELOG-1.21.1.md) for changes and version differences. `./gradlew build` also runs the server GameTests and writes `build/reports/gametest/results.xml`. Potions are brewed; only tipped arrows use the crafting table. This version retains REI and optional dynamic-light support.

## Releasing supported Minecraft versions

Maintain `1.21.1` and `26.1.2` independently. Merge the backport into `1.21.1`;
keep the newer Minecraft APIs on `26.1.2`. Port individual fixes between them.
Do not merge the whole newer-version branch into the older-version branch.

Pushes and pull requests to either release branch build and run GameTests.
Publishing requires a tag on the corresponding branch, for example:

```sh
# On the clean, tested 1.21.1 branch:
git tag -a 'v1.2.3+mc1.21.1' -m 'Release 1.2.3 for Minecraft 1.21.1'
git push origin 1.21.1 'v1.2.3+mc1.21.1'

# Separately, on the clean, tested 26.1.2 branch:
git tag -a 'v1.2.3+mc26.1.2' -m 'Release 1.2.3 for Minecraft 26.1.2'
git push origin 26.1.2 'v1.2.3+mc26.1.2'
```

The workflow validates the tag, release branch, jar version, Minecraft dependency,
and Java bytecode before publishing. It performs a clean build and GameTests,
then publishes only the verified jar and sources to GitHub and the exact main jar
to Modrinth. Minecraft compatibility is restricted to that branch's version.
Existing release tags and assets are not modified. The old Minecraft line does
not replace GitHub's latest-release marker. Both branches need the repository's
`MODRINTH_TOKEN` Actions secret to publish to Modrinth.
