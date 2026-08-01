# VanillaWorldEdit

VanillaWorldEdit lets builders use `/fill` and `/setblock` through FastAsyncWorldEdit (FAWE). It executes bulk edits through one FAWE edit session without changing the player's selection.

## Requirements

- Paper/Purpur (or another Paper-compatible server)
- Java 21
- FastAsyncWorldEdit 2.13.x

## Behavior

`/fill` supports the vanilla-style `destroy`, `hollow`, `keep`, `outline`, and `replace [filter]` modes.
`/setblock` supports `destroy`, `keep`, and `replace`.
Absolute, relative (`~`, `~-2.5`), and arithmetic coordinates (`~(2+3)`) are accepted.

The default `destroy.mode: throttled` preserves natural drops while enforcing global per-tick block and time budgets. Set it to `fawe` for the lowest load; that mode intentionally does not create natural drops. Throttled destroys do not create FAWE undo history because every block is broken and replaced through Bukkit.

## Configuration

```yaml
max-volume: 16777216
destroy:
  mode: throttled
  blocks-per-tick: 128
  max-millis-per-tick: 4
  max-queued-jobs: 8
```

FAWE's own region, history, memory, and block-change limits still apply. Players must have the corresponding `minecraft.command.fill` or `minecraft.command.setblock` permission and the relevant FAWE edit permissions.

## Build And Test

```bash
mvn clean package
```

The plugin JAR is written to `target/VanillaWorldEdit-1.0.0.jar`. Unit tests run as part of the build.
