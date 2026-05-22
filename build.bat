@echo off
REM CISK Spawn Mod — build helper (Windows). Requires JDK 21.
cd /d "%~dp0"
where java >nul 2>nul || (echo ERROR: Java not found. Install Temurin 21 from https://adoptium.net/ & pause & exit /b 1)
if not exist gradlew.bat (
  where gradle >nul 2>nul || (echo ERROR: Gradle wrapper missing and gradle not installed. Install gradle ^>= 8.10 from https://gradle.org/install/ then retry. & pause & exit /b 1)
  echo Bootstrapping Gradle wrapper...
  call gradle wrapper --gradle-version 8.10
)
echo Building...
call gradlew.bat --no-daemon build
if errorlevel 1 (echo Build failed. & pause & exit /b 1)
echo.
echo ===================================================
echo   Build OK. Look in build\libs\ for the jar.
echo ===================================================
pause
