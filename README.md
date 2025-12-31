# Unicorn Fabric Mod

A minimal Fabric mod for Minecraft 1.21.1 that adds a new item, the **Unicorn Horn**, and puts it in the Ingredients creative tab.

## Prerequisites

- JDK 21
- Gradle wrapper included (Gradle 9.2.1)
- Internet access the first time you build (to download Fabric/Minecraft dependencies; or point the wrapper at a local distribution zip).

## Building

```sh
./gradlew build
```

The mod JAR will appear under `build/libs`.

## Where to look

- Mod entrypoint: `src/main/java/com/example/unicorn/UnicornMod.java`
- Mod metadata: `src/main/resources/fabric.mod.json`
- Assets (lang, models, textures): `src/main/resources/assets/unicorn/`

## Customizing

- Gradle wrapper is pinned to 9.2.1. Update `gradle/wrapper/gradle-wrapper.properties` if you want a different version or to point at the local `gradle-9.2.1-bin.zip` file instead of downloading.
- Update versions in `gradle.properties` if you bump Minecraft/Fabric.
- Change the mod id/name in `fabric.mod.json` and `UnicornMod.MOD_ID`.
- Swap out the Unicorn Horn texture at `src/main/resources/assets/unicorn/textures/item/unicorn_horn.png`.
