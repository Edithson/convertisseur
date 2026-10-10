#!/bin/bash
set -e

echo "========================================================================="
echo "=== Construction du paquet AppImage pour Convertisseur sonore GZ ==="
echo "========================================================================="

APP_NAME="ConvertisseurSonoreGZ"
APP_DISPLAY_NAME="Convertisseur sonore GZ"
BUILD_DIR="target"
APP_DIR="${BUILD_DIR}/AppDir"

# Step 1: Clean build directory and package JAR
echo "1. Compilation et packaging Maven..."
mvn clean package -DskipTests
mkdir -p "${APP_DIR}/usr/lib"
mvn dependency:copy-dependencies -DoutputDirectory="${APP_DIR}/usr/lib" -DskipTests

# Remove non-Linux native binaries
echo "2. Optimisation des dépendances pour Linux x86_64..."
rm -f ${APP_DIR}/usr/lib/jave-nativebin-win*.jar
rm -f ${APP_DIR}/usr/lib/jave-nativebin-osx*.jar
rm -f ${APP_DIR}/usr/lib/jave-nativebin-linux32*.jar
rm -f ${APP_DIR}/usr/lib/jave-nativebin-linux-arm*.jar

# Copy main application jar
cp "${BUILD_DIR}/convertisseur-1.0-SNAPSHOT.jar" "${APP_DIR}/usr/lib/"

# Step 3: Create AppRun entrypoint
echo "3. Génération du script d'exécution AppRun..."
cat << 'EOF' > "${APP_DIR}/AppRun"
#!/bin/bash
HERE="$(dirname "$(readlink -f "${0}")")"

JAVA_BIN="$(which java 2>/dev/null || true)"
if [ -z "$JAVA_BIN" ]; then
    if [ -d "/usr/lib/jvm/default-java/bin" ]; then
        JAVA_BIN="/usr/lib/jvm/default-java/bin/java"
    elif [ -d "/usr/lib/jvm/java-17-openjdk-amd64/bin" ]; then
        JAVA_BIN="/usr/lib/jvm/java-17-openjdk-amd64/bin/java"
    elif [ -d "/usr/lib/jvm/java-21-openjdk-amd64/bin" ]; then
        JAVA_BIN="/usr/lib/jvm/java-21-openjdk-amd64/bin/java"
    fi
fi

if [ -z "$JAVA_BIN" ]; then
    echo "Erreur : Aucun environnement d'exécution Java (JRE/JDK) n'a été trouvé."
    echo "Veuillez installer Java sur Debian : sudo apt install default-jre"
    exit 1
fi

CP="${HERE}/usr/lib/*"

exec "$JAVA_BIN" -cp "$CP" com.moafogaus.MainLauncher "$@"
EOF

chmod +x "${APP_DIR}/AppRun"

# Step 4: Desktop entry and Icon
echo "4. Création des métadonnées Desktop et Icône..."
cat << EOF > "${APP_DIR}/${APP_NAME}.desktop"
[Desktop Entry]
Name=${APP_DISPLAY_NAME}
Comment=Application de conversion multimédia audio et vidéo
Exec=AppRun
Icon=${APP_NAME}
Terminal=false
Type=Application
Categories=AudioVideo;Audio;Video;Encoder;
EOF

cat << 'EOF' > "${APP_DIR}/${APP_NAME}.svg"
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 128 128" width="128" height="128">
    <rect width="128" height="128" rx="28" fill="#0f172a"/>
    <circle cx="64" cy="64" r="48" fill="#1e1b4b" stroke="#6366f1" stroke-width="4"/>
    <path d="M40 64 L50 40 L60 88 L70 30 L80 94 L90 64" fill="none" stroke="#06b6d4" stroke-width="6" stroke-linecap="round" stroke-linejoin="round"/>
</svg>
EOF

cp "${APP_DIR}/${APP_NAME}.svg" "${APP_DIR}/.DirIcon"

# Step 5: Packaging into self-contained .AppImage binary
echo "5. Assemblage du binaire autonome AppImage..."
OUT_APPIMAGE="${APP_NAME}.AppImage"
rm -f "${OUT_APPIMAGE}"

TAR_PAYLOAD="/tmp/${APP_NAME}_payload.tar.gz"
tar -czf "${TAR_PAYLOAD}" -C "${APP_DIR}" .

CHECKSUM=$(md5sum "${TAR_PAYLOAD}" | cut -d' ' -f1)

cat << EOF > "${OUT_APPIMAGE}"
#!/bin/bash
# ${APP_DISPLAY_NAME} Portable AppImage (Debian 13 / Linux)
# Executable sans installation

APP_NAME="${APP_NAME}"
CHECKSUM="${CHECKSUM}"
TARGET_DIR="\${TMPDIR:-/tmp}/.appimage_\${APP_NAME}_\${CHECKSUM}"

if [ ! -d "\$TARGET_DIR" ]; then
    mkdir -p "\$TARGET_DIR"
    ARCHIVE_LINE=\$(grep -a -n "^__ARCHIVE_PAYLOAD_BELOW__" "\$0" | cut -d: -f1)
    tail -n +\$((ARCHIVE_LINE + 1)) "\$0" | tar -xzf - -C "\$TARGET_DIR"
fi

exec "\$TARGET_DIR/AppRun" "\$@"
exit 0
__ARCHIVE_PAYLOAD_BELOW__
EOF

cat "${TAR_PAYLOAD}" >> "${OUT_APPIMAGE}"
rm -f "${TAR_PAYLOAD}"
chmod +x "${OUT_APPIMAGE}"

echo ""
echo "========================================================================="
echo "=== SUCCÈS ! Binaire portable prêt : ./${OUT_APPIMAGE} ==="
echo "=== Taille du fichier : $(du -h "${OUT_APPIMAGE}" | cut -f1) ==="
echo "=== Pour lancer : ./${OUT_APPIMAGE} ou double-cliquez dessus ==="
echo "========================================================================="
