# ColoredSigns2

**ColoredSigns2** is a lightweight, modern Paper plugin that brings full hex color support to Minecraft signs and allows effortless in-game sign editing.

---

### ✨ Features

* **Hex & Classic Colors:** Use custom hex colors (`&#RRGGBB`) and standard Minecraft formatting codes (`&a`, `&l`, etc.) on all signs.
* **Color Preservation:** Editing signs via the vanilla 1.20+ right-click GUI preserves your existing colors so you don't lose formatting.
* **Quick Line Editing:** Edit or clear any line of a sign you are looking at with `/editsign <1-4> [text]` without breaking the sign.
* **Smart Tab-Completion:** Pressing Tab after `/editsign <line> ` suggests the sign's current text, making quick fixes effortless.
* **Land Claim & Anti-Grief Safe:** Fully compatible with protection plugins (WorldGuard, GriefPrevention, Towny, Lands) and logs edits with CoreProtect.
* **Waxed Sign Protection:** Respects honeycomb-waxed signs so players cannot tamper with locked signs.
* **Fully Customizable:** Easily customize messages, prefix, colors, and edit distance in `config.yml`.
* **High Performance:** Designed specifically for Paper and Folia servers (supporting versions 1.21 through 26.x).

---

### 🎨 Color Code Guide

Simply write color codes on signs or in the `/editsign` command:

| Type | Syntax | Description |
|---|---|---|
| **Hex Colors** | `&#RRGGBB` | Any custom RGB color (e.g. `&#FF5555Red`, `&#00FFAACyan`) |
| **Standard Colors** | `&0` – `&f` | Classic Minecraft colors (e.g. `&aLight Green`, `&eYellow`) |
| **Text Formats** | `&l`, `&o`, `&n`, `&m`, `&r` | **Bold** (`&l`), *Italic* (`&o`), <u>Underline</u> (`&n`), ~~Strikethrough~~ (`&m`), Reset (`&r`) |

---

### 📋 Commands & Permissions

| Command | Description | Permission | Default |
|---|---|---|---|
| `/editsign <1-4> <text>` | Sets the text of the targeted sign line | `coloredsigns2.use` | `op` |
| `/editsign <1-4>` | Clears the targeted sign line | `coloredsigns2.use` | `op` |
| `/coloredsigns reload` | Reloads the configuration file | `coloredsigns2.reload` | `op` |

* `coloredsigns2.use`: Allows placing colored signs and using `/editsign`.
* `coloredsigns2.bypass.waxed`: Allows editing signs locked with honeycomb (waxed).
* `coloredsigns2.reload`: Allows reloading the configuration file.
