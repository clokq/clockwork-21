@echo off
rem Builds clockwork-1.0.0.jar WITHOUT Gradle. Needs JDK 25 on PATH and a Fabric 26.2 install.
rem Usage: build.bat [path-to-.minecraft] [path-to-fabric-api.jar]
setlocal enabledelayedexpansion
set "MC_DIR=%~1"
if "%MC_DIR%"=="" set "MC_DIR=%APPDATA%\.minecraft"
set "MC_VER=26.2"
set "FABRIC_API=%~2"
if "%FABRIC_API%"=="" for %%f in ("%MC_DIR%\mods\fabric-api-*.jar") do set "FABRIC_API=%%f"
set "VANILLA=%MC_DIR%\versions\%MC_VER%\%MC_VER%.jar"

if not exist "%VANILLA%" (echo Missing %VANILLA% - launch 26.2 once & exit /b 1)
if not exist "%FABRIC_API%" (echo Fabric API jar not found - pass it as 2nd argument & exit /b 1)

set "CP=%VANILLA%;%FABRIC_API%"
for /r "%MC_DIR%\libraries" %%j in (*.jar) do set "CP=!CP!;%%j"

if exist out rmdir /s /q out
mkdir out\classes
dir /s /b src\main\java\*.java > out\sources.txt
javac --release 25 -nowarn -cp "%CP%" -d out\classes @out\sources.txt || exit /b 1
xcopy /s /e /y /q src\main\resources\* out\classes\ >nul
powershell -Command "(Get-Content out\classes\fabric.mod.json) -replace '\$\{version\}','1.0.0' | Set-Content out\classes\fabric.mod.json"
jar --create --file clockwork-1.0.0.jar -C out\classes .
echo Built clockwork-1.0.0.jar - put it in %MC_DIR%\mods together with Fabric API
