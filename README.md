# Overpowered Again

**English** · [简体中文](README.zh-CN.md) · [繁體中文](README.zh-HK.md)

A small Fabric mod for **Minecraft 26.2** that reworks the enchanted golden apple.

Eating an enchanted golden apple now grants:

| Effect | Level | Duration |
| --- | --- | --- |
| Regeneration | IV | 30s |
| Absorption | IV | 2m |
| Resistance | I | 5m |
| Fire Resistance | I | 5m |

In vanilla 26.2 the last three are already exactly these values; the change is Regeneration,
which goes from level II for 20s to level IV for 30s. The full list is written out explicitly so the
behaviour does not silently drift if vanilla numbers change.

## Crafting, advancements and settings

By default, eight gold blocks around one apple craft one enchanted golden apple. Obtaining a gold
block unlocks this mod's recipe in the recipe book.

| Gold block | Gold block | Gold block |
| --- | --- | --- |
| Gold block | Apple | Gold block |
| Gold block | Gold block | Gold block |

### Automatic advancement selection

The recipes actually loaded by the game determine which advancement is enabled, including recipes
from other mods and data packs:

| Loaded recipes | Enabled advancement | Trigger | Disabled advancement |
| --- | --- | --- | --- |
| An enchanted golden apple recipe exists | Overpowered (`"frame": "challenge"`) | Craft an enchanted golden apple | Overpowered Again |
| No enchanted golden apple recipe exists | Overpowered Again | Eat an enchanted golden apple | Overpowered |

Normal and shift-click crafting recognize the crafted item, including other mods’ recipe IDs.

### Recipe compatibility and duplicate prevention

The recipe toggle only controls this mod’s own recipe; it does not remove other mods’ recipes.
If another provider already supplies the same shaped recipe (same ingredients, grid and output),
this mod suppresses its own recipe and recipe-book unlock, so only the other provider’s recipe appears.
If that provider is removed and this mod’s recipe remains enabled, the next recipe load restores this mod’s recipe.

### Mod Menu settings and legacy texture

Mod Menu (optional, 20.0.2 or newer) exposes two settings:

1. Enable/disable the legacy enchanted golden apple recipe.
2. Switch the enchanted golden apple between legacy and modern textures.

Both legacy options default to enabled. The texture switches immediately and preserves enchantment
glint; ordinary golden apples are unaffected. The legacy sprite comes from Mojang's built-in
Programmer Art pack and is also used as the mod icon. See [texture provenance](docs/legacy-texture.md).
Settings are saved in `config/overpowered_again.json` (`legacyRecipe`, `legacyTexture`).
Changing the recipe in singleplayer reloads recipes and advancements when the settings screen closes.
On multiplayer servers the recipe option is disabled in the client menu: edit the server config and
restart the server. The texture option always belongs to the client.

## How it works

Since 1.21.2 a consumable's effects live on the `minecraft:consumable` item component as a list of
consume effects, and `Consumable#onConsume` applies them through `onConsumeEffects.forEach(...)`.
`ConsumableMixin` redirects that one call site for the enchanted golden apple, which leaves every
other part of eating untouched: particles, sounds, statistics, the `consume_item` advancement
trigger and the item consumption itself.

Target values live in one place:
`src/main/java/com/koutakutenn/overpoweredagain/OverpoweredEffects.java`.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.5 or newer
- Java 25 or newer
- No separate Fabric API installation required: the jar bundles Fabric Resource Loader v1 and
  Fabric API Base, which load the mod's data and language resources.

## Installation

Put the jar in the instance's `mods` folder and restart the game. The mod is not hot-loaded, so a
full restart is required.

## Building

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
./gradlew build
```

The jar lands in `build/libs/`.

## Testing

```sh
./gradlew runIntegrationTest -PacceptMinecraftEula=true
./gradlew runIntegrationTest -PacceptMinecraftEula=true -PintegrationExternalRecipe=true
./gradlew runIntegrationTest -PacceptMinecraftEula=true -PintegrationLegacyRecipe=false
./gradlew runIntegrationTest -PacceptMinecraftEula=true -PintegrationLegacyRecipe=false -PintegrationExternalRecipe=true
```

The four commands cover this mod’s recipe alone, duplicate recipes from two providers, no recipe,
and an external recipe alone. Tests run in a real development server and check duplicate prevention,
advancement selection, normal and shift-click crafting, effect refreshes, independent timers, and
safe recipe detection during initial world resource loading. A test data pack simulates the external
recipe; these tests do not cover the client settings screen or texture rendering.
Each invocation uses a fresh directory printed by Gradle;
the result is written to `run-test/run-<timestamp>/test-result.txt`, and older results are retained.

The test requests a normal server shutdown with `halt(false)` after the checks, returning to the
server loop instead of calling `System.exit` from the server thread.

## License

The mod is released into the public domain under the [Unlicense](https://unlicense.org), which the
Fabric metadata declares. You may alternatively use it under the
[MIT License](https://opensource.org/license/mit) if you prefer.

See [LICENSE](LICENSE) and [LICENSE-MIT](LICENSE-MIT).

The bundled Mojang texture is not covered by the mod code license; see [texture provenance](docs/legacy-texture.md).
