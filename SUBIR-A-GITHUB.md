# Cómo subir Cookly-KMP a GitHub (ramas por integrante)

La idea: cada uno sube **su capa** desde **su propia cuenta**, en **su rama**, y se une a `main` con pull request.
Así en GitHub se ven los commits de los dos.

## 0. Una sola vez, cada uno en su computador

```bash
git config --global user.name  "Su nombre"
git config --global user.email "su-correo@unicauca.edu.co"
```

## 1. María: crear el repositorio y el esqueleto (rama main)

1. En GitHub: **New repository** → nombre `Cookly-KMP` → **Public** → sin README → *Create*.
2. En la terminal:

```bash
git clone https://github.com/MariaAlejandraS/Cookly-KMP.git
cd Cookly-KMP
```

3. Copiar desde el zip **solo la configuración**:
   `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/` (carpeta completa),
   `gradlew`, `gradlew.bat`, `.gitignore`, `README.md`, `SUBIR-A-GITHUB.md` y `composeApp/build.gradle.kts`.

```bash
git add .
git commit -m "Crea el proyecto Kotlin Multiplataforma (Android + Desktop)"
git push origin main

git branch Maria && git push origin Maria
git branch Santiago && git push origin Santiago
```

4. En GitHub: **Settings → Collaborators → Add people** → agregar a Santiago.

## 2. Santiago: capa de datos (rama Santiago)

```bash
git clone https://github.com/MariaAlejandraS/Cookly-KMP.git
cd Cookly-KMP
git checkout Santiago
```

Copiar desde el zip estos archivos, respetando las carpetas, y hacer un commit por grupo:

```bash
# Commit 1: modelos y base de datos
#   composeApp/src/commonMain/kotlin/com/cookly/data/model/
#   composeApp/src/commonMain/kotlin/com/cookly/data/db/
git add composeApp/src/commonMain/kotlin/com/cookly/data/model composeApp/src/commonMain/kotlin/com/cookly/data/db
git commit -m "Agrega modelos y base de datos Room (SQLite)"

# Commit 2: red con Ktor
#   composeApp/src/commonMain/kotlin/com/cookly/data/network/
git add composeApp/src/commonMain/kotlin/com/cookly/data/network
git commit -m "Agrega consumo de Open Food Facts con Ktor"

# Commit 3: repositorios, preferencias, seguridad y contenedor
#   composeApp/src/commonMain/kotlin/com/cookly/data/repository/
#   composeApp/src/commonMain/kotlin/com/cookly/data/prefs/
#   composeApp/src/commonMain/kotlin/com/cookly/data/auth/
#   composeApp/src/commonMain/kotlin/com/cookly/data/AppContainer.kt
git add composeApp/src/commonMain/kotlin/com/cookly/data
git commit -m "Agrega repositorios, preferencias y contraseñas con hash"

# Commit 4: expect/actual por plataforma
#   composeApp/src/commonMain/kotlin/com/cookly/Platform.kt
#   composeApp/src/androidMain/kotlin/com/cookly/Platform.android.kt
#   composeApp/src/androidMain/kotlin/com/cookly/Database.android.kt
#   composeApp/src/desktopMain/kotlin/com/cookly/Platform.desktop.kt
#   composeApp/src/desktopMain/kotlin/com/cookly/Database.desktop.kt
git add composeApp/src
git commit -m "Agrega expect/actual de base de datos y seguridad para Android y Desktop"

git push origin Santiago
```

En GitHub: **Pull requests → New** → base `main` ← compare `Santiago` → *Create* → *Merge pull request*.

## 3. María: capa de presentación (rama Maria)

```bash
git checkout Maria
git pull origin main          # trae la capa de datos de Santiago
```

Copiar desde el zip el resto de archivos y hacer un commit por grupo:

```bash
# Commit 1: tema y componentes
git add composeApp/src/commonMain/kotlin/com/cookly/ui/theme composeApp/src/commonMain/kotlin/com/cookly/ui/components
git commit -m "Agrega tema de Cookly y componentes reutilizables"

# Commit 2: ViewModels
git add composeApp/src/commonMain/kotlin/com/cookly/viewmodel
git commit -m "Agrega los ViewModels de cada pantalla"

# Commit 3: pantallas
git add composeApp/src/commonMain/kotlin/com/cookly/ui/screens
git commit -m "Agrega las 8 pantallas"

# Commit 4: navegación compartida y puntos de entrada
#   commonMain/.../navigation/, commonMain/.../App.kt,
#   androidMain/.../MainActivity.kt, androidMain/AndroidManifest.xml, desktopMain/.../main.kt
git add composeApp/src
git commit -m "Agrega navegación compartida y entradas de Android y Desktop"

git push origin Maria
```

En GitHub: **Pull request** base `main` ← compare `Maria` → *Merge pull request*.

## 4. Probar

Abrir el proyecto (rama `main`) en Android Studio y correr Android y Desktop (ver README).
Si algo no compila, corregirlo en la rama de quien corresponda y repetir el pull request.
