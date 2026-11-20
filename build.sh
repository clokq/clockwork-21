#!/usr/bin/env bash
# Builds clockwork.jar WITHOUT Gradle. Needs JDK 25 and a Fabric 26.2 install.
# Usage: ./build.sh [path-to-.minecraft] [path-to-fabric-api.jar]
set -e
MC_DIR="${1:-$HOME/.minecraft}"
MC_VER="26.2"
FABRIC_API="${2:-$(ls "$MC_DIR"/mods/fabric-api-*.jar 2>/dev/null | head -1)}"
VANILLA="$MC_DIR/versions/$MC_VER/$MC_VER.jar"

[ -f "$VANILLA" ]    || { echo "Missing $VANILLA (launch 26.2 once)"; exit 1; }
[ -f "$FABRIC_API" ] || { echo "Fabric API jar not found: pass it as 2nd argument"; exit 1; }

CP="$VANILLA:$FABRIC_API"
while IFS= read -r j; do CP="$CP:$j"; done < <(find "$MC_DIR/libraries" -name '*.jar')

rm -rf out && mkdir -p out/classes
javac --release 25 -nowarn -cp "$CP" -d out/classes $(find src/main/java -name '*.java')
cp -r src/main/resources/* out/classes/
sed -i 's/\${version}/1.0.0/' out/classes/fabric.mod.json
jar --create --file clockwork-1.0.0.jar -C out/classes .
echo "Built clockwork-1.0.0.jar -> put it in $MC_DIR/mods (with Fabric API)"
