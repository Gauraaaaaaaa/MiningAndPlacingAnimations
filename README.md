# Mining & Placing Animations — unofficial Minecraft 26.2 port (Fabric)

This is an **unofficial port** of [Mining & Placing Animations](https://github.com/Gauraaaaaaaa/MiningAndPlacingAnimations) to Minecraft **26.2** on **Fabric**.

> **All credit for the mod goes to its original author, [Gaura](https://github.com/Gauraaaaaaaa).**
> The idea, design, animation system, default animations, icon and name are theirs.
> This fork only updates the code so it runs on a newer Minecraft version.
>
> - Original source: https://github.com/Gauraaaaaaaa/MiningAndPlacingAnimations
> - Official downloads: https://modrinth.com/mod/mining_and_placing_animations
>
> This port is **not made, endorsed or supported by Gaura**. Please **do not report problems with this
> port to the original project**. Use this fork's [issue tracker](https://github.com/signup098ezxs/MiningAndPlacingAnimations/issues) instead.
> If an official 26.2 release becomes available, prefer that one.

## Requirements

- Minecraft 26.2 with Java 25
- Fabric Loader 0.19.5 or newer
- [Fabric API](https://modrinth.com/mod/fabric-api) 0.161.0+26.2 or newer
- Optional: Sodium 0.9 and Iris 1.11.4 for 26.2 are supported

## Reporting bugs

Found a problem with this 26.2 port? Please report it **here, on this fork**, not on the original project:

1. Go to this fork's [**Issues** tab](https://github.com/signup098ezxs/MiningAndPlacingAnimations/issues)
   (you need a free GitHub account).
2. Click **New issue** and choose **Bug report (26.2 port)**.
3. Fill in the form: what happened, how to make it happen again, your versions and other mods.
4. Attach `logs/latest.log` from your Minecraft folder, or the file from `crash-reports/` if the game crashed.
   Screenshots or a short video help a lot for animation problems.

Before reporting, it helps to check whether the bug still happens with **only this mod and Fabric API**
installed, and whether someone has already reported it in the Issues list.

## Building

```sh
./gradlew build
```

The mod jar is written to `build/libs/`.

**Downloads:** ready-made jars are on this fork's [Releases page](https://github.com/signup098ezxs/MiningAndPlacingAnimations/releases).
A new release is published automatically when `mod_version` in `gradle.properties` is bumped and pushed.

## Changes from the original (GPL-3.0 §5 notice)

This is a modified version of Mining & Placing Animations. Modified by signup098ezxs, with code changes
made by Claude (an AI assistant made by Anthropic), starting **2026-10-19**:

- Ported from Minecraft 1.21.11 to 26.2 (Fabric only): new Fabric Loom without remapping, Java 25,
  Fabric API 0.161.0+26.2. The NeoForge module and the Architectury setup were removed in this fork.
- Rendering rewritten for 26.2's new submit-based rendering: animated blocks are submitted as moving blocks;
  block hiding now hooks Fabric's Indigo `AltModelBlockRendererImpl` (Sodium hooks kept);
  block entities, breaking cracks and the block outline follow the animation through new hook points;
  Iris shadow-pass hook updated for Iris 1.11.
- Compat mixins (Sodium, Iris, Indigo) only load when those mods are installed.
- Smoother animations: keyframes are now joined by smooth curves by default; a new optional `easing`
  setting was added (the default placing animations use `ease_out`); mining animations loop seamlessly
  while you keep mining instead of briefly restoring the real block on every loop.
- Built-in resource pack metadata updated for 26.2.

### New animation file options

Both are optional, so existing animation packs keep working:

| Field | Values | Default |
|---|---|---|
| `interpolation` | `smooth`, `linear` (the original behaviour) | `smooth` |
| `easing` | `linear`, `ease_in`, `ease_out`, `ease_in_out` | `linear` |

## License

Licensed under the **GNU General Public License v3.0 or later**, the same license as the original:
see [LICENSE.txt](LICENSE.txt). Copyright (c) 2026 Gaura; modifications are released under the same license.
If you share builds of this port, you must keep this license and notice, and make the source available.

## Disclaimer

NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.
Minecraft is a trademark of Mojang AB. Fabric, Sodium and Iris are the work of their respective authors.
