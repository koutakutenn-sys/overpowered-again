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

## Advancement

The mod adds one advancement:

- **Title:** Overpowered Again
- **Description:** Eat an enchanted golden apple
- Unlocked when you eat an enchanted golden apple

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
```

This starts a real development server and checks the effects, the vanilla values being replaced, the
custom advancement and its item predicate. Each invocation uses a fresh directory printed by Gradle;
the result is written to `run-test/run-<timestamp>/test-result.txt`, and older results are retained.

The test requests a normal server shutdown with `halt(false)` after the checks, returning to the
server loop instead of calling `System.exit` from the server thread.

## License

The mod is released into the public domain under the [Unlicense](https://unlicense.org), which the
Fabric metadata declares. You may alternatively use it under the
[MIT License](https://opensource.org/license/mit) if you prefer.

See [LICENSE](LICENSE) and [LICENSE-MIT](LICENSE-MIT).
