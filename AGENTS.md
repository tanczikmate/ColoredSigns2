# AGENTS.md

Welcome to the **ColoredSigns2** repository. This document provides contextual guidelines, architectural details, and operational instructions for AI agents working within this codebase.

---

## 1. Project Overview

**ColoredSigns2** is a lightweight, modern Minecraft server plugin built for the **Paper** ecosystem (supporting versions **1.21** through **26.x**) with native **Folia** support.

It enables players to use hex colors (`&#RRGGBB`) and legacy Minecraft formatting codes (`&0`-`&f`, `&l`, `&o`, etc.) on signs, provides an in-game `/editsign <1-4> [text]` command with smart tab-completion, seamlessly preserves existing colors during vanilla 1.20+ sign GUI editing, and provides customizable messages and settings via `config.yml`.

---

## 2. Technology Stack

* **Language:** Java 21 (Toolchain configured for Java 21)
* **Target Platforms:** Paper, Purpur, Folia (Minecraft 1.21+ up to 26.x)
* **Build System:** Gradle with Kotlin DSL (`build.gradle.kts`)
* **Libraries:**
  * `io.papermc.paper:paper-api` (Paper plugin API)
  * `net.kyori.adventure` (Adventure text and component library, including MiniMessage)
  * `com.mojang:brigadier` (Brigadier command engine via Paper's lifecycle events)

---

## 3. Architecture & File Structure

```
ColoredSigns2/
├── .github/
│   └── workflows/
│       ├── build.yml                  # CI build and verification workflow
│       └── release.yml                # Automated Modrinth publishing workflow
├── gradle/wrapper/                    # Gradle wrapper binaries & configuration
├── src/
│   └── main/
│       ├── java/me/tanczikmate/coloredSigns2/
│       │   ├── ColoredSigns2.java     # Main plugin class (onEnable, Brigadier setup, formatHexAndLegacy)
│       │   ├── ConfigManager.java     # Loads & manages config.yml, MiniMessage message formatting
│       │   ├── EditsignCommand.java   # /editsign command logic, claim/protection verification & execution
│       │   └── SignManager.java       # SignChangeEvent listener (vanilla GUI color preservation & hex parsing)
│       └── resources/
│           ├── config.yml             # Default configuration and localized messages
│           └── paper-plugin.yml       # Modern Paper plugin metadata & permission definitions
├── build.gradle.kts                   # Gradle build script (dependencies, versioning, copy task)
├── settings.gradle.kts                # Project root settings
├── gradle.properties                  # Project group, version & Gradle optimizations
├── AGENTS.md                          # AI Agent guide (this file)
├── README.md                          # User-facing plugin documentation (Modrinth-ready)
└── LICENSE                            # MIT License
```

### Component Details

1. **`ColoredSigns2.java`**
   * Extends `JavaPlugin`.
   * Initializes `ConfigManager` and saves default config.
   * Registers `SignManager` listener during `onEnable()`.
   * Registers Brigadier command trees for `/editsign` and `/coloredsigns reload` via `LifecycleEvents.COMMANDS`.
   * Houses `formatHexAndLegacy(String text)`: Converts `&#RRGGBB` into Adventure-compatible legacy hex format (`&x&r&r&g&g&b&b`), respects `formatting.allow-magic-codes`, and deserializes it via `LegacyComponentSerializer`.
   * Provides `getSignLineSuggestion(...)`: Supplies asynchronous tab-completion suggestions for the current sign line.

2. **`ConfigManager.java`**
   * Manages `config.yml` loading and reloading.
   * Provides getters for `max-distance`, `preserve-vanilla-gui-colors`, `enable-tab-suggestions`, and `allow-magic-codes`.
   * Houses `sendMessage(CommandSender sender, String key, TagResolver... tags)`: renders messages with `MiniMessage` using the configured prefix.

3. **`EditsignCommand.java`**
   * Implements `BasicCommand` for executing sign line updates and clearing.
   * Enforces player-only execution, distance raycasting (`max-distance`), and side detection (`sign.getInteractableSideFor(...)`).
   * **Waxed Signs Protection:** Checks `sign.isWaxed()` and blocks modification unless `coloredsigns2.bypass.waxed` is present.
   * **Anti-Grief & Protection:** Fires a native `SignChangeEvent` prior to modifying the block state. This ensures claim plugins (WorldGuard, GriefPrevention, Towny, Lands) can cancel unauthorized edits, and logging plugins (CoreProtect) can record the change.
   * Uses `ThreadLocal<Boolean> EDITING_INTERNAL` to signal `SignManager` to bypass duplicate formatting during command execution.

4. **`SignManager.java`**
   * Listens to `SignChangeEvent`.
   * Bypasses internal events fired by `EditsignCommand`.
   * Preserves colors when players edit existing signs in the vanilla 1.20+ GUI if enabled in config.
   * Parses hex and legacy colors using `ColoredSigns2.formatHexAndLegacy(...)`.

5. **`paper-plugin.yml`**
   * Sets `api-version: '1.21'` to ensure maximum compatibility across 1.21, 1.21.x, and Paper 26.x.
   * Declares `folia-supported: true`.
   * Declares permissions: `coloredsigns2.use`, `coloredsigns2.bypass.waxed`, and `coloredsigns2.reload`.

---

## 4. Key Rules & Best Practices for Agents

1. **Do Not Commit Automatically:** Do not run `git commit` unless explicitly instructed by the user.
2. **Conventional Commits:** When committing, always follow the Conventional Commits specification: `<type>(<scope>): <description>` (e.g. `feat:`, `fix:`, `docs:`, `refactor:`, `perf:`, `ci:`, `chore:`).
3. **Never Bypass Area Protections:** Always ensure sign modifications can be intercepted by claim protection plugins and logged by CoreProtect via `SignChangeEvent`.
4. **Preserve Compatibility:** Avoid introducing APIs exclusive to minor subversions. Maintain compatibility across 1.21 - 26.x.
5. **Folia Awareness:** Respect thread locality when interacting with block states. Do not run cross-region block modifications off the region thread.
6. **Documentation Integrity:** Keep existing comments, JavaDocs, and metadata intact when updating code.

---

## 5. Build & Verification Commands

* **Compile Java:**
  ```powershell
  ./gradlew compileJava
  ```
* **Assemble & Build Plugin JAR:**
  ```powershell
  ./gradlew build
  ```
* **Run Server (via run-paper plugin):**
  ```powershell
  ./gradlew runServer
  ```
