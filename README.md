# Overpowered Again

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

The mod also adds a custom advancement:

- **Title:** Overpowered Again
- **Description:** Eat an enchanted golden apple
- Unlocked when you eat an enchanted golden apple

## How it works

Since 1.21.2 a consumable's effects live on the `minecraft:consumable` item component as a list of
consume effects, and `Consumable#onConsume` applies them through `onConsumeEffects.forEach(...)`.
`ConsumableMixin` redirects that one call site for the enchanted golden apple, which leaves every
other part of eating untouched: particles, sounds, statistics, the `consume_item` advancement
trigger and the item consumption itself.

Target values live in one place: `src/main/java/com/koutakutenn/overpoweredagain/OverpoweredEffects.java`.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.5 or newer
- Java 25 or newer
- No Fabric API required

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

This starts a real development server and checks the effects, the vanilla values being replaced and
the custom advancement. The result is written to `run-test/test-result.txt`.

Known issue: the development server does not shut down after the checks, so the Gradle task hangs
and has to be stopped with `pkill -9 -f overpowered-again`. See
[HANDOFF-advancement-not-loaded.md](HANDOFF-advancement-not-loaded.md).

## Status

- Verified in a development server: the four effects, and the vanilla values being replaced.
- **Not working yet:** the custom advancement is not loaded by the server. The mod data pack is not
  discovered at all (`PackRepository` only lists `vanilla` and the three built-in experimental
  packs). Details, evidence and suggested investigation are in
  [HANDOFF-advancement-not-loaded.md](HANDOFF-advancement-not-loaded.md).
- Not tested in a real client (`runClient`) or installed in a normal game instance yet.

## License

MIT, see [LICENSE](LICENSE).
