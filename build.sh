#!/usr/bin/env bash
# CISK Spawn Mod — build helper.
# Requires JDK 21. Will bootstrap the Gradle wrapper on first run.
set -e
cd "$(dirname "$0")"

# --- Java 21 check ---
if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: Java not found. Install Temurin 21 from https://adoptium.net/"
  exit 1
fi
JV=$(java -version 2>&1 | head -1 | sed -E 's/.*"([0-9]+)\.?.*/\1/')
if [ "$JV" -lt 21 ]; then
  echo "ERROR: Java $JV detected. NeoForge 1.21.1 requires Java 21+."
  echo "Install Temurin 21 from https://adoptium.net/, set JAVA_HOME, retry."
  exit 1
fi
echo "Java $JV OK."

# --- Gradle wrapper bootstrap ---
if [ ! -f gradlew ]; then
  echo "Gradle wrapper not found — bootstrapping..."
  if ! command -v gradle >/dev/null 2>&1; then
    echo "Gradle not found and wrapper missing. Two options:"
    echo "  1. Install Gradle via SDKMAN:  curl -s https://get.sdkman.io | bash && sdk install gradle 8.10"
    echo "  2. Install via brew (macOS):   brew install gradle"
    echo "  3. Download manually:          https://gradle.org/install/"
    exit 1
  fi
  gradle wrapper --gradle-version 8.10
  echo "Wrapper generated."
fi

# --- Build ---
echo "Building..."
./gradlew --no-daemon build

JAR=$(ls build/libs/ciskspawn-*.jar 2>/dev/null | head -1)
if [ -z "$JAR" ]; then
  echo "Build finished but no jar found in build/libs/. Something's off."
  exit 1
fi
echo
echo "===================================================="
echo "  Build OK. Mod jar:"
echo "  $(pwd)/$JAR"
echo "===================================================="
echo "Drop this jar into BOTH:"
echo "  - your server's mods/ folder"
echo "  - your CurseForge client instance's mods/ folder"
echo "Also ensure GeckoLib 4.7.1+ for NeoForge 1.21.1 is in both mods/ folders."
