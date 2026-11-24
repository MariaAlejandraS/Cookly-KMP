# Cookly — Microproyecto KMP (Taller 4)

Cookly es una nevera digital: registra tus alimentos con su fecha de vencimiento, te avisa con colores cuáles vencen primero, busca productos por código de barras en Open Food Facts y guarda recordatorios de comidas.

Esta versión está hecha con **Kotlin Multiplataforma (KMP)** y **Compose Multiplatform**: la interfaz, la navegación, los ViewModels, la base de datos y la red se escriben **una sola vez** en `commonMain` y corren en **Android** y en **Desktop** (Windows, macOS o Linux).

- **Asignatura:** Electiva Desarrollo de Aplicaciones Móviles — Universidad del Cauca
- **Profesor:** Ph.D. Cristhian Figueroa
- **Integrantes:** María Alejandra Solarte · Ronal Santiago Valdez Jurado

## Tecnologías

| Para qué | Librería |
| --- | --- |
| Código compartido | Kotlin Multiplatform 2.2.10 |
| Interfaz compartida | Compose Multiplatform 1.8.2 (Material 3) |
| Navegación compartida | Navigation Compose multiplataforma (JetBrains) |
| ViewModels | Lifecycle ViewModel multiplataforma (JetBrains) |
| Persistencia | **Room KMP sobre SQLite** (BundledSQLiteDriver) |
| Preferencias | Multiplatform Settings |
| Red | Ktor Client + kotlinx.serialization |
| Fechas | kotlinx-datetime |

## Cómo correrlo

**Requisitos:** Android Studio (Ladybug o más nuevo) con el plugin **Kotlin Multiplatform**, y JDK 17 o superior.

1. Abrir la carpeta `Cookly-KMP` en Android Studio y esperar a que termine el *Gradle Sync* (la primera vez descarga las librerías, tarda varios minutos).
2. **Android:** elegir la configuración `composeApp`, un emulador o un celular, y darle *Run*.
3. **Desktop:** en la terminal de Android Studio:
   ```bash
   ./gradlew :composeApp:run          # Windows: gradlew.bat :composeApp:run
   ```
   Se abre una ventana con la misma app.

> Si Android Studio ofrece actualizar el *Android Gradle Plugin* a 9.x, **no lo acepten**: AGP 9 cambia la forma de configurar un proyecto KMP con Android y el proyecto dejaría de compilar.

## Estructura

```
composeApp/src/
├── commonMain/kotlin/com/cookly/        ← COMPARTIDO (Android + Desktop)
│   ├── App.kt                           Punto de entrada de la UI: tema + login o pestañas
│   ├── Platform.kt                      expect: platformName, sha256, secureRandomBytes
│   ├── data/
│   │   ├── model/                       Product, Reminder, User (tablas Room), FoodCategory, Dates
│   │   ├── db/                          AppDatabase + ProductDao, ReminderDao, UserDao
│   │   ├── network/                     FoodApi (Ktor) + OffModels (JSON de Open Food Facts)
│   │   ├── repository/                  AuthRepository, ProductRepository, ReminderRepository
│   │   ├── prefs/                       UserPrefs (modo oscuro, días de aviso, sesión)
│   │   ├── auth/                        PasswordHasher (SHA-256 × 50.000 con sal), Validators
│   │   └── AppContainer.kt              Crea una sola vez la base, la red y los repositorios
│   ├── viewmodel/                       Auth, Nevera, AddProduct, Search, Reminders, Profile
│   ├── navigation/                      Screen (rutas) + CooklyNavigation (NavHost y pestañas)
│   └── ui/
│       ├── theme/                       Colores y tema claro/oscuro
│       ├── components/                  Botones, campos, tarjetas, filtros, cuadrícula de categorías
│       └── screens/                     Las 8 pantallas
├── androidMain/kotlin/com/cookly/       ← SOLO ANDROID
│   ├── MainActivity.kt                  setContent { App(container) }
│   ├── Database.android.kt              Dónde se crea el archivo SQLite en Android
│   └── Platform.android.kt              actual de platformName, sha256, secureRandomBytes
└── desktopMain/kotlin/com/cookly/       ← SOLO DESKTOP
    ├── main.kt                          Window { App(container) }
    ├── Database.desktop.kt              Dónde se crea el archivo SQLite en Desktop (~/.cookly)
    └── Platform.desktop.kt              actual de platformName, sha256, secureRandomBytes
```

## Pantallas

| # | Pantalla | Archivo | Qué demuestra |
| --- | --- | --- | --- |
| 1 | Inicio de sesión | `AuthScreens.kt` | Campos, validación, bloqueo tras 5 intentos |
| 2 | Registro | `AuthScreens.kt` | Formulario con reglas de contraseña |
| 3 | Nevera | `NeveraScreen.kt` | Lista por vencimiento, colores, búsqueda y filtros |
| 4 | Agregar / editar producto | `AddProductScreen.kt` | Categorías, contador, interruptores, selector de fecha |
| 5 | Buscar por código | `SearchScreen.kt` | **Red**: Open Food Facts con Ktor |
| 6 | Recordatorios | `RemindersScreen.kt` | Hora, días de la semana, una vez o repetido |
| 7 | Perfil | `ProfileScreen.kt` | Modo oscuro y días de aviso (Settings), cerrar sesión |
| 8 | Acerca de | `AboutScreen.kt` | Descripción y créditos del equipo |

## Cómo se cumple cada requisito del taller

| Requisito | Dónde |
| --- | --- |
| Dos plataformas | `androidMain` (Android) y `desktopMain` (Desktop); la UI es la misma de `commonMain` |
| Cinco pantallas o más | Las 8 de la tabla anterior |
| Persistencia entre sesiones | Room sobre SQLite: `data/db/`. Preferencias: `data/prefs/UserPrefs.kt` |
| Navegación en la capa compartida | `navigation/CooklyNavigation.kt` está en `commonMain` |
| Pantalla de créditos | `ui/screens/AboutScreen.kt` |
| Arquitectura modular (MVVM) | Pantalla → ViewModel → Repositorio → Room / Ktor |

## API usada

Open Food Facts, pública y sin llave: `GET https://world.openfoodfacts.org/api/v2/product/{codigo}.json`. Se envía un `User-Agent` propio, como pide el servicio. Datos bajo licencia ODbL. Códigos para probar: `3017624010701` (Nutella) y `5449000000996` (Coca-Cola).

## Ramas

- `main`: versión estable.
- `Maria`: capa de presentación (pantallas, tema, componentes, navegación, ViewModels, puntos de entrada).
- `Santiago`: capa de datos (modelos, Room, repositorios, red, preferencias, seguridad, `expect/actual`).

Cada integrante trabaja en su rama y se une a `main` con pull request.
