# VanillaWorldEdit

VanillaWorldEdit lets builders keep their existing muscle memory for `/fill` and `/setblock` while enjoying the speed and reliability of FastAsyncWorldEdit (FAWE). The plugin intercepts the vanilla commands, translates the arguments (including relative coordinates and modes such as `destroy`, `hollow`, `keep`, `outline`, and `replace`), and then replays them as FAWE selections and operations. This keeps your workflow vanilla-simple without sacrificing asynchronous WorldEdit performance.

## Features

- Converts `/fill` and `/setblock` invocations into the closest FAWE command sequence automatically.
- Honors vanilla-style relative (`~`) coordinates, arithmetic in coordinate arguments, and fill/set modes.
- Uses the player's FAWE selection so that undo/redo history and region visualizations continue to work as expected.
- Breaks blocks naturally when `destroy` is specified and restricts replacements when `keep` or `replace` are used, matching vanilla semantics.

## Requirements

- Paper/Purpur (or another Paper-compatible) server on 1.13+; developed against 1.21 builds.
- FastAsyncWorldEdit installed on the server (Bukkit & Core components).
- Java 21 runtime (matches the target specified in `pom.xml`).
- Players must already have permission to execute `/fill` and `/setblock`; the plugin does not add tab completion or permission nodes.

## Installation

1. Download the compiled jar from the Releases page or build it yourself (see below).
2. Drop the jar into your server's `plugins` directory alongside FAWE.
3. Restart or reload the server to register the listener that hooks `PlayerCommandPreprocessEvent`.

## Usage

### `/fill`

```text
/fill <x1> <y1> <z1> <x2> <y2> <z2> <block> [mode]
```

The plugin converts the two corners into a FAWE region selection and dispatches bulk operations:

| Mode      | Performed FAWE actions |
|-----------|------------------------|
| (none)    | `/set <block>` |
| `destroy` | Breaks blocks naturally, then `/set <block>` |
| `hollow`  | `/set air` then `/outline <block>` |
| `keep`    | `/replace air <block>` |
| `outline` | `/outline <block>` |
| `replace` | `/replace <filter> <block>` (only when a target block is provided) |

All coordinates may use `~`, `~-5`, or even expressions such as `~(2+3)`; the helper converts them before calculating the FAWE selection.

### `/setblock`

```text
/setblock <x> <y> <z> <block> [mode]
```

The plugin selects a single-block FAWE region before dispatching a FAWE command:

| Mode      | Behavior |
|-----------|----------|
| (none)    | `/set <block>` |
| `destroy` | Breaks the target block naturally, then `/set <block>` |
| `keep`    | `/replace air <block>` |
| `replace` | `/replace <block>` |

### Notes & Limitations

- No tab completion is provided; rely on the client-side vanilla helper or add your own command aliases if needed.
- Only `/fill` and `/setblock` are intercepted—other vanilla structure commands (e.g., `/clone`) remain untouched.
- Because FAWE executes the heavy lifting, ensure your FAWE configuration allows the commands you expect (e.g., `//set`, `//replace`).

## Building from Source

```powershell
mvn clean package
```

The shaded artifact will be placed in `target/`. Copy it to your server when you need a custom build.

## Credits

- Original concept by The_Dumbledodo.
- Maintained by TWME-TW. See `LICENSE` for redistribution details.
