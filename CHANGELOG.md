# Changelog

## 2.0.0

Multi-version, multi-loader rewrite (Fabric + NeoForge + Forge from one Stonecutter build).

### Supported versions

- Minecraft 26.3 – Fabric, NeoForge
- Minecraft 1.21.1 – Fabric, NeoForge
- Minecraft 1.20.1 – Fabric, Forge
- Minecraft 1.16.5 – Forge (on the `1.16.5` branch of `Brilliafy/fairexperience`)

### Features

- Fair enchanting: raw XP equivalent of the enchantment requirement instead of flat levels.
- Fair anvils: flat level costs converted to fair raw XP costs.
- Fair anvil combining: order-independent, prior-work-penalty-free enchantment costs based on a
  potential function, so applying books in any order costs the same.
- Configurable removal of the vanilla 40-level "Too Expensive!" anvil limit.
- Detection of direct `experienceLevel` modifications by other mods.

### Technical

- Built with Stonecutter 0.9.8, Gradle 9.8, Fabric Loom 1.18 and ModDevGradle 2.0.148 (including the
  legacy Forge plugin, which reobfuscates 1.20.1 to SRG).
- MixinExtras (`@WrapOperation`) removes the prior-work penalty on the vanilla code path as well.
- Parchment mappings for 1.20.1/1.21.1 dev names.
- Automated Modrinth/CurseForge publishing via mod-publish-plugin on `v*` tags.
- Unit tests for the XP maths run on every target.
