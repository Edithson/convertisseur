# Convertisseur sonore GZ 🎵⚡

**Convertisseur sonore GZ** est une application Linux moderne, intuitive et ultra-performante dédiée à la conversion de fichiers audio et vidéo. Conçue avec **JavaFX 13** et propulsée par un wrapper Java orienté objet de **FFmpeg** (`JAVE2`), elle intègre une interface sombre *Glassmorphic*, le glisser-déposer de médias, un explorateur de fichiers personnalisé avec barre d'adresse et un moteur de conversion autonome 100 % opérationnel sur 12 formats.

---

## 📌 Sommaire

- [✨ Fonctionnalités Principales](#-fonctionnalités-principales)
- [💻 Prérequis Système](#-prérequis-système)
- [📥 Installation & Lancement Rapide (Code Source)](#-installation--lancement-rapide-code-source)
- [⚙️ Compilation en Exécutables Autonomes](#-compilation-en-exécutables-autonomes)
  - [1. Format AppImage Portable (Recommandé - Sans installation)](#1-format-appimage-portable-recommandé---sans-installation)
  - [2. Paquet Debian (.deb)](#2-paquet-debian-deb)
- [🎯 Guide d'Utilisation & Prise en Main](#-guide-dutilisation--prise-en-main)
- [🎵 Formats Média Pris en Charge](#-formats-média-pris-en-charge)
- [📁 Structure du Projet](#-structure-du-projet)
- [❓ Résolution des Problèmes (FAQ)](#-résolution-des-problèmes-faq)

---

## ✨ Fonctionnalités Principales

- 🎨 **Design Sombre Glassmorphic** : Interface réactive et esthétique basée sur des gradients HSL sombres (`#0f172a`, `#1e1b4b`, accents indigo et cyan néon).
- 📥 **Glisser-Déposer (Drag & Drop)** : Glissez directement vos fichiers audio et vidéo au centre de l'application.
- 📂 **Explorateur de Fichiers Avancé Intégré** :
  - Barre d'adresse éditable avec autocomplétion pour saisir directement le chemin d'un dossier.
  - Raccourcis d'accès rapide (Dossier Personnel, Musique, Vidéos, Téléchargements).
  - Multi-sélection de fichiers via arborescence et cases à cocher.
- ⚡ **Gestion des Formats Individuelle & Globale** :
  - Définissez un format spécifique pour chaque fichier ou appliquez un format global à toute la liste en un clic.
- 📊 **Suivi de Progression Avancé** :
  - Barres de progression individuelles par fichier.
  - Carte de progression globale calculant le pourcentage moyen et le temps restant estimé (ETA).
- 🛡️ **Sécurité & Intégrité** :
  - Contrôle automatique d'existence et de lisibilité de la source avant lancement.
  - Nettoyage automatique des fichiers de 0 octet en cas d'erreur ou d'annulation.
- 📦 **Packaging Autonome (35 Mo)** :
  - Pas besoin d'installer FFmpeg séparément : les binaires natifs sont directement intégrés dans l'AppImage et le paquet Debian.

---

## 💻 Prérequis Système

Pour compiler ou exécuter le projet depuis les sources, vous devez disposer de :

- **OS** : Linux (Debian 11/12/13, Ubuntu 20.04+, Linux Mint, Arch, Fedora) ou Windows / macOS.
- **Java JDK** : Version 11 ou supérieure (ex: OpenJDK 17 ou 21).
- **Apache Maven** : Version 3.6 ou plus récente.

### Installation des dépendances sur Debian / Ubuntu :
```bash
sudo apt update
sudo apt install default-jdk maven git
```

---

## 📥 Installation & Lancement Rapide (Code Source)

### 1. Cloner le projet
```bash
git clone https://github.com/votre-compte/convertisseur-sonore-gz.git
cd convertisseur-sonore-gz
```

### 2. Lancer l'application en mode développement
Exécutez la commande Maven suivante pour compiler et lancer l'application immédiatement :

```bash
mvn clean javafx:run
```

---

## ⚙️ Compilation en Exécutables Autonomes

Le projet inclut des scripts d'assemblage automatisés qui génèrent des paquets autonomes et optimisés (~35 Mo).

### 1. Format AppImage Portable (Recommandé - Sans installation)
L'AppImage est un binaire autonome qui s'exécute directement sur n'importe quel système Linux (Debian, Ubuntu, Arch, Fedora) sans modifier votre système.

#### Étape de génération :
```bash
chmod +x build-appimage.sh
./build-appimage.sh
```

#### Résultat & Lancement :
Le fichier **`ConvertisseurSonoreGZ.AppImage`** (35 Mo) est créé à la racine du projet.

- **Depuis le terminal** :
  ```bash
  ./ConvertisseurSonoreGZ.AppImage
  ```
- **Depuis l'explorateur graphique** :
  Faites un **double-clic** sur `ConvertisseurSonoreGZ.AppImage`.

---

### 2. Paquet Debian (`.deb`)
Pour installer l'application de façon permanente dans le système Debian avec raccourci dans le menu des applications.

#### Étape de génération :
```bash
chmod +x build-deb.sh
./build-deb.sh
```

#### Installation du paquet généré :
```bash
sudo apt install ./convertisseur-sonore-gz_1.0.0_amd64.deb
```

#### Lancement après installation :
- Dans le terminal : `convertisseur-sonore-gz`
- Dans le menu des applications : Cherchez **Convertisseur sonore GZ**.

#### Désinstallation propre :
```bash
sudo apt purge convertisseur-sonore-gz
```

---

## 🎯 Guide d'Utilisation & Prise en Main

1. **Ajouter des fichiers audio ou vidéo** :
   - Glissez-déposez vos médias directement dans la grande zone au centre.
   - Ou cliquez sur **`+ Parcourir des fichiers...`** pour utiliser le sélecteur standard.
   - Ou cliquez sur **`📂 Saisir un Chemin / Explorateur...`** pour utiliser l'explorateur avec barre d'adresse et sélection multiple par cases à cocher.

2. **Choisir les formats de sortie** :
   - Par défaut, chaque média a sa propre liste déroulante de formats.
   - Pour appliquer le même format à tous les fichiers : sélectionnez le format dans la barre supérieure (**Format global**) puis cliquez sur **`Appliquer à tous`**.

3. **Définir le répertoire de destination** :
   - Par défaut, les fichiers convertis sont sauvegardés dans `~/ConvertedMedia`.
   - Cliquez sur **`Changer...`** pour choisir un autre dossier ou sur l'icône 📁 pour l'ouvrir.

4. **Lancer la conversion** :
   - Cliquez sur **`Convertir`** sur la ligne d'un fichier pour une conversion individuelle.
   - Ou cliquez sur **`⚡ Tout Convertir`** en bas à droite pour démarrer la conversion par lots.

---

## 🎵 Formats Média Pris en Charge

| Type | Extension | Codec Audio / Vidéo | Description |
|---|---|---|---|
| **Audio** | `.mp3` | MP3 (`libmp3lame`) | Standard audio universel comprimé |
| **Audio** | `.wav` | PCM S16LE (`pcm_s16le`) | Audio brut non compressé haute fidélité |
| **Audio** | `.ogg` | Ogg Vorbis (`libvorbis`) | Audio libre haute qualité |
| **Audio** | `.opus` | OPUS (`libopus` @ 48kHz) | Codec audio moderne ultra-performant |
| **Audio** | `.aac` | AAC (`aac`) | Audio haute qualité Apple / Android |
| **Audio** | `.flac` | FLAC (`flac`) | Lossless (sans perte de qualité) |
| **Audio** | `.m4a` | AAC / MPEG-4 (`aac`) | Conteneur audio Apple |
| **Vidéo** | `.mp4` | H.264 / AAC | Vidéo universelle haute compatibilité |
| **Vidéo** | `.mkv` | H.264 / AAC | Conteneur Matroska multimédia |
| **Vidéo** | `.webm` | VP9 / OPUS | Format web ouvert Google |
| **Vidéo** | `.avi` | MPEG-4 / MP3 | Conteneur vidéo classique |
| **Vidéo** | `.mov` | H.264 / AAC | Format QuickTime Apple |

---

## 📁 Structure du Projet

```text
convertisseur/
├── src/
│   ├── main/
│   │   ├── java/com/moafogaus/
│   │   │   ├── App.java                 # Classe principale JavaFX
│   │   │   ├── MainLauncher.java        # Lanceur neutre (Bypass Classpath JavaFX)
│   │   │   ├── controller/              # Contrôleurs JavaFX (Main, MediaRow, FileBrowser)
│   │   │   ├── model/                   # Modèles (MediaItem, TargetFormat, Settings)
│   │   │   └── service/                 # Services (FFmpegService, ConversionTask)
│   │   └── resources/com/moafogaus/     # Vues FXML et styles CSS
│   └── test/                            # Tests d'intégration et de conversion FFmpeg
├── build-appimage.sh                    # Script de génération du binaire AppImage (35 Mo)
├── build-deb.sh                         # Script de génération du paquet Debian .deb (34 Mo)
├── pom.xml                              # Fichier de configuration Maven
├── .gitignore                           # Exclusions Git (target/, dist/, executables)
└── README.md                            # Documentation générale
```

---

## ❓ Résolution des Problèmes (FAQ)

### 1. L'AppImage indique "Permission denied" lors du lancement ?
Rendez le fichier exécutable avec la commande suivante :
```bash
chmod +x ConvertisseurSonoreGZ.AppImage
```

### 2. Erreur `JavaFX runtime components are missing` ?
Cette erreur survient si vous tentez de lancer `com.moafogaus.App` directement sans passer par le lanceur wrapper.  
**Solution** : Utilisez toujours `com.moafogaus.MainLauncher` ou lancez via `mvn javafx:run` / `./ConvertisseurSonoreGZ.AppImage`.

### 3. Les scripts `.sh` ne s'exécutent pas ?
Assurez-vous d'avoir les permissions d'exécution :
```bash
chmod +x build-appimage.sh build-deb.sh
```

---

## 📜 Licence & Crédits

- Développé avec **JavaFX**, **Maven** et **JAVE2** (Java Audio Video Encoder).
- Incorpore le moteur **FFmpeg** sous licence LGPL/GPL.
