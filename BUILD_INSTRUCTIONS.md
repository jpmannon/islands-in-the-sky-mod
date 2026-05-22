# CISK Spawn Mod — Build Instructions

## What's already done in this repo

- ✅ All Java source (13 files, 4 packages)
- ✅ `build.gradle`, `settings.gradle`, `gradle.properties`, `neoforge.mods.toml`, `pack.mcmeta`
- ✅ `storykeeper.geo.json` (your model) placed at `src/main/resources/assets/ciskspawn/geo/`
- ✅ `storykeeper.animation.json` (your animations) placed at `src/main/resources/assets/ciskspawn/animations/`
- ✅ `storykeeper.png` (your texture) placed at `src/main/resources/assets/ciskspawn/textures/entity/`
- ✅ `StorykeeperEntity.java` patched to use your exact animation names (`Walk`, `Idle`)
- ✅ `build.sh` / `build.bat` helper that bootstraps the Gradle wrapper

## What you still need to drop in (optional)

1. **Starter island NBT files** → `src/main/resources/data/ciskspawn/structure/`
   Named `starter_island_01.nbt`, `_02.nbt`, ... up to `_50`. Mod auto-discovers at startup.

2. **Cutscene frames + manifest** →
   `src/main/resources/assets/ciskspawn/textures/cutscene/intro/frame_NNN.png`
   `src/main/resources/assets/ciskspawn/textures/cutscene/intro.json` → `{"fps":10,"length":100}`

## Build it (one command)

Prereq: **Java 21 JDK** installed and on PATH (https://adoptium.net/).

macOS / Linux:
```
./build.sh
```

Windows:
```
build.bat
```

First run will:
1. Verify Java 21.
2. If Gradle wrapper is missing, bootstrap it using your system Gradle (or print
   instructions to install Gradle 8.10).
3. Download NeoForge 21.1.228, Parchment mappings, and GeckoLib 4.7.1 (~3-5 min,
   one time).
4. Compile and package.

Output: `build/libs/ciskspawn-1.0.0.jar`

Drop into:
- `<server>/mods/`
- `<your CurseForge instance>/mods/`

Also ensure `geckolib-neoforge-1.21.1-4.7.1.jar` (or newer) is in both `mods/` folders.

## If the build fails

Common issues:

| Symptom | Fix |
|---|---|
| `Unsupported class file major version` | Java is not 21. Check `java -version`, install Temurin 21, set `JAVA_HOME`. |
| Gradle distribution download timeout | Network. Retry with `./gradlew --no-daemon build --refresh-dependencies` |
| GeckoLib not found | The cloudsmith maven URL in `build.gradle` is occasionally slow. Add `https://maven.geckolib.com/` as a fallback. |
| `Cannot resolve symbol` in IDE | IDE indexing. After first successful `./build.sh`, IntelliJ/Eclipse can import the project — File → Open → select `build.gradle`. |
| Build succeeds but mod doesn't load on server | Check the server log for the `[CISK] Mod constructor complete.` line. If missing, check GeckoLib is also in `mods/`. |

Send the build log if something else breaks — happy to debug.
