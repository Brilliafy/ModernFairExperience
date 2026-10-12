[![Support my work](https://img.shields.io/badge/Support_my_work-F16061?style=for-the-badge&logo=ko-fi&logoColor=white)](https://ko-fi.com/S2X12424XK)
[![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?style=for-the-badge&logo=modrinth&logoColor=white)](https://modrinth.com/mod/fair-experience)
[![CurseForge](https://img.shields.io/badge/CurseForge-F44336?style=for-the-badge&logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/fair-experience)

# Fair Experience (multi-version)

**Fair Experience** converts flat XP level costs into **fair raw XP costs** for Enchanting Tables,
Anvils, and level-deducting mechanics.

In vanilla, higher levels cost exponentially more raw XP per level. If you are Level 46 and use a
Level 30 enchantment slot, vanilla subtracts a flat 3 levels (taking you to Level 43) and costs you
**900+ raw XP**. At Level 30 those same 3 levels cost only **306 raw XP**. Fair Experience fixes that
imbalance.

This repository is the multi-version, multi-loader rewrite. The original 1.12.2 codebase stays in
[`Brilliafy/fairexperience`](https://github.com/Brilliafy/fairexperience); the 1.16.5 port lives on
that repository's `1.16.5` branch (ForgeGradle 5, Java 8).

## Supported versions

| Minecraft | Fabric | NeoForge | Forge |
| :-- | :--: | :--: | :--: |
| 26.3 | ✅ | ✅ | – |
| 1.21.1 | ✅ | ✅ | – |
| 1.20.1 | ✅ | – | ✅ |
| 1.16.5 | – | – | ✅ (legacy branch) |

Seven jars per release (six from this repo, one from the 1.16.5 branch), all produced from a single
Stonecutter build.

## Features

- **Fair Enchanting** – subtracts the raw XP value of the enchantment requirement instead of flat levels.
- **Fair Anvils** – converts anvil level costs to fair raw XP, saving high-level players from huge penalties.
- **Fair Anvil Combining** – removes the Prior Work Penalty and order dependency. Combining books or
  applying them in any order always costs the same, and books upgraded by combining cost exactly the
  same to apply as raw books from villagers or creative mode.
- **Configurable 40-level limit** – removes vanilla's "Too Expensive!" anvil cap by default.
- **Mod compatibility** – works with modded enchanting tables (Apotheosis and friends), modded anvils
  and mods that modify levels directly.

The fair anvil cost model is a potential function:

```
Phi(I) = sum(multiplier * level) + count * (count - 1) / 2
```

The cost of moving an item from state A to state B is `Phi(B) - Phi(A)`, which makes every path cost
the same while still making each additional enchantment more expensive.

## Configuration

| Loader | File |
| :-- | :-- |
| Fabric | `config/fairexperience.json` |
| NeoForge 1.21.1 / 26.3 | `config/fairexperience-common.toml` |
| Forge 1.20.1 | `config/fairexperience-common.toml` |

All backends expose the same options:

```toml
[general]
    enableFairEnchanting = true
    enableFairAnvils = true
    enableFairAnvilCombining = true
    removeTooExpensiveLimit = true
    enableFairDirectModifications = true
    debugLogging = false
```

## Development

Toolchain notes:

- The Gradle daemon needs **Java 25** (Fabric Loom 1.18 requires it). Java 17/21/25 compile
  toolchains are provisioned automatically by the foojay resolver.
- Builds are intentionally **sequential** (`org.gradle.parallel=false`, `org.gradle.workers.max=1`).
  Do not enable parallel builds: the Minecraft toolchains will happily eat all of your RAM.
- `stonecutter active` points at the unused `indev-fabric` node. Every real version therefore builds
  from its own preprocessed copy of `src`; the active node compiles the shared root `src` as-is.

Build a single target:

```bash
./gradlew :1.20.1-forge:build
./gradlew :26.3-fabric:build
```

Run a dev client/server:

```bash
./gradlew :1.21.1-neoforge:runClient
./gradlew :1.20.1-forge:runServer
```

Version and loader specific settings live in `stonecutter.properties.toml`. Shared code lives in
`src/main/java/com/brilliafy/fairexperience` and uses Stonecutter comment guards
(`//? if fabric { ... }`) plus the multi-loader split inspired by Jared's MultiLoader-Template.

### Stack

- [Stonecutter](https://stonecutter.kikugie.dev/) for the Minecraft-version axis
  (`stonecutter-template-multiloader` layout).
- [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template) style loader split
  (common code + Fabric / NeoForge / Forge entry points).
- [MixinExtras](https://github.com/LlamaLad7/MixinExtras) – bundled via Jar-in-Jar on 1.20.1 Forge,
  provided by Fabric Loader and NeoForge elsewhere. Used for `@WrapOperation` prior-work-penalty removal.
- [Parchment](https://parchmentmc.org/) mappings for readable parameter names where available.
- [mod-publish-plugin](https://github.com/modmuss50/mod-publish-plugin) for Modrinth/CurseForge releases.

## Releasing

Push a tag like `v2.0.0`. `.github/workflows/build.yml` then:

1. builds all six jars sequentially (one matrix job each),
2. runs `publishMods` per project, publishing to Modrinth and CurseForge with the right loader and
   game-version tags,
3. creates a GitHub Release containing every jar.

Required repository secrets: `MODRINTH_TOKEN`, `CURSEFORGE_TOKEN`.

## License

MIT. See [LICENSE](LICENSE).
