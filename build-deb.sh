#!/bin/bash
set -e

echo "========================================================================="
echo "=== Construction du paquet Debian (.deb) pour Convertisseur sonore GZ ==="
echo "========================================================================="

PACKAGE_NAME="convertisseur-sonore-gz"
DISPLAY_NAME="Convertisseur sonore GZ"
VERSION="1.0.0"
ARCH="amd64"
DEB_DIR="target/${PACKAGE_NAME}_${VERSION}_${ARCH}"

# Step 1: Ensure compilation
echo "1. Compilation Maven et préparation du paquet..."
mvn clean package -DskipTests

# Create directory structure AFTER mvn clean
mkdir -p "${DEB_DIR}/DEBIAN"
mkdir -p "${DEB_DIR}/usr/bin"
mkdir -p "${DEB_DIR}/usr/share/${PACKAGE_NAME}/lib"
mkdir -p "${DEB_DIR}/usr/share/applications"
mkdir -p "${DEB_DIR}/usr/share/icons/hicolor/scalable/apps"

mvn dependency:copy-dependencies -DoutputDirectory="${DEB_DIR}/usr/share/${PACKAGE_NAME}/lib" -DskipTests

# Optimize dependencies for Linux
echo "2. Optimisation des dépendances pour Debian amd64..."
rm -f ${DEB_DIR}/usr/share/${PACKAGE_NAME}/lib/jave-nativebin-win*.jar
rm -f ${DEB_DIR}/usr/share/${PACKAGE_NAME}/lib/jave-nativebin-osx*.jar
rm -f ${DEB_DIR}/usr/share/${PACKAGE_NAME}/lib/jave-nativebin-linux32*.jar
rm -f ${DEB_DIR}/usr/share/${PACKAGE_NAME}/lib/jave-nativebin-linux-arm*.jar

cp "target/convertisseur-1.0-SNAPSHOT.jar" "${DEB_DIR}/usr/share/${PACKAGE_NAME}/lib/"

# Step 2: Create DEBIAN/control
echo "3. Création des métadonnées du paquet Debian..."
cat << EOF > "${DEB_DIR}/DEBIAN/control"
Package: ${PACKAGE_NAME}
Version: ${VERSION}
Architecture: ${ARCH}
Maintainer: Moafogaus <contact@example.com>
Depends: default-jre | openjdk-17-jre | openjdk-21-jre
Section: utils
Priority: optional
Description: ${DISPLAY_NAME} - Application de conversion multimédia audio et vidéo
 Convertisseur moderne audio et vidéo développé avec JavaFX et FFmpeg.
EOF

# Step 3: Create executable script /usr/bin/convertisseur-sonore-gz
echo "4. Génération de l'exécutable /usr/bin/${PACKAGE_NAME}..."
cat << 'EOF' > "${DEB_DIR}/usr/bin/${PACKAGE_NAME}"
#!/bin/bash
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
    echo "Installez Java avec : sudo apt install default-jre"
    exit 1
fi

CP="/usr/share/convertisseur-sonore-gz/lib/*"
exec "$JAVA_BIN" -cp "$CP" com.moafogaus.MainLauncher "$@"
EOF
chmod +x "${DEB_DIR}/usr/bin/${PACKAGE_NAME}"

# Step 4: Create Desktop entry & Icon
echo "5. Création des raccourcis système et icône..."
cat << EOF > "${DEB_DIR}/usr/share/applications/${PACKAGE_NAME}.desktop"
[Desktop Entry]
Name=${DISPLAY_NAME}
Comment=Application de conversion multimédia audio et vidéo
Exec=${PACKAGE_NAME}
Icon=${PACKAGE_NAME}
Terminal=false
Type=Application
Categories=AudioVideo;Audio;Video;Encoder;
EOF

cat << 'EOF' > "${DEB_DIR}/usr/share/icons/hicolor/scalable/apps/${PACKAGE_NAME}.svg"
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 128 128" width="128" height="128">
    <rect width="128" height="128" rx="28" fill="#0f172a"/>
    <circle cx="64" cy="64" r="48" fill="#1e1b4b" stroke="#6366f1" stroke-width="4"/>
    <path d="M40 64 L50 40 L60 88 L70 30 L80 94 L90 64" fill="none" stroke="#06b6d4" stroke-width="6" stroke-linecap="round" stroke-linejoin="round"/>
</svg>
EOF

# Step 5: Build .deb package with dpkg-deb
echo "6. Compilation du paquet .deb via dpkg-deb..."
OUT_DEB="${PACKAGE_NAME}_${VERSION}_${ARCH}.deb"
dpkg-deb --root-owner-group --build "${DEB_DIR}" "${OUT_DEB}"

echo ""
echo "========================================================================="
echo "=== SUCCÈS ! Paquet Debian généré : ./${OUT_DEB} ==="
echo "=== Taille : $(du -h "${OUT_DEB}" | cut -f1) ==="
echo "=== Installation : sudo dpkg -i ./${OUT_DEB} ou sudo apt install ./${OUT_DEB} ==="
echo "========================================================================="
