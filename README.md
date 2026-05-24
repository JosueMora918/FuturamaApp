# Documentación Técnica — Futurama Trivia App

**Versión:** 1.0  
**Plataforma:** Android (Jetpack Compose)  
**Lenguaje:** Kotlin  
**Paquete:** `com.example.futuramaapp`

---

## Tabla de contenidos

1. [Descripción general](#1-descripción-general)
2. [Arquitectura](#2-arquitectura)
3. [Estructura del proyecto](#3-estructura-del-proyecto)
4. [Capas de la aplicación](#4-capas-de-la-aplicación)
   - 4.1 [Capa de datos](#41-capa-de-datos-data)
   - 4.2 [Capa de lógica](#42-capa-de-lógica-viewmodel)
   - 4.3 [Capa de UI](#43-capa-de-ui-composables)
5. [Pantallas](#5-pantallas)
6. [Sistema de diseño](#6-sistema-de-diseño)
7. [Flujo de navegación](#7-flujo-de-navegación)
8. [API externa](#8-api-externa)
9. [Dependencias](#9-dependencias)
10. [Configuración del proyecto](#10-configuración-del-proyecto)
11. [Instalación y requisitos](#11-instalación-y-requisitos)

---

## 1. Descripción general

**Futurama Trivia** es una aplicación de preguntas y respuestas temática basada en la serie animada *Futurama*. El usuario responde 10 preguntas de opción múltiple (4 opciones cada una), obtiene retroalimentación inmediata por respuesta y recibe un resultado final con una calificación y una cita de un personaje de la serie.

Las preguntas se cargan desde la API pública `api.sampleapis.com/futurama/info` al iniciar cada partida. Si la API no está disponible, la app utiliza un banco de 15 preguntas locales como respaldo.

---

## 2. Arquitectura

La app sigue el patrón **MVVM (Model-View-ViewModel)** con **UDF (Unidirectional Data Flow)**:

```
[ API / Datos locales ]
         ↓
  FuturamaRepository        ← Capa de datos
         ↓
    QuizViewModel            ← Lógica de negocio + estado
         ↓ (StateFlow)
  Composables (UI)           ← Solo renderizan, nunca modifican estado directamente
         ↓ (callbacks)
    QuizViewModel            ← Recibe eventos del usuario
```

**Principios aplicados:**
- Los composables son **stateless**: reciben datos y emiten eventos, nunca guardan estado propio.
- El ViewModel es la **única fuente de verdad** del estado del juego.
- El estado es **inmutable**: cada cambio produce una nueva copia del `QuizUiState`.

---

## 3. Estructura del proyecto

```
FuturamaApp/
├── app/
│   ├── build.gradle.kts                  ← Dependencias y configuración del módulo
│   └── src/main/
│       ├── AndroidManifest.xml           ← Permisos y declaración de actividad
│       ├── res/
│       │   ├── font/
│       │   │   ├── orbitron.ttf          ← Fuente variable (títulos)
│       │   │   ├── exo2.ttf              ← Fuente variable (cuerpo)
│       │   │   └── exo2_italic.ttf       ← Fuente variable (cuerpo itálica)
│       │   └── values/
│       │       └── themes.xml            ← Tema base XML (fondo oscuro, sin ActionBar)
│       └── java/com/example/futuramaapp/
│           ├── MainActivity.kt           ← Punto de entrada de la app
│           ├── FuturamaTriviaApp.kt      ← Composable raíz + efectos visuales de fondo
│           ├── QuizViewModel.kt          ← Estado del juego y lógica de negocio
│           ├── WelcomeScreen.kt          ← Pantalla de bienvenida
│           ├── QuizScreen.kt             ← Pantalla de preguntas
│           ├── ResultsScreen.kt          ← Pantalla de resultados
│           └── data/
│               └── FuturamaRepository.kt ← Acceso a la API y banco de preguntas
├── gradle/
│   └── libs.versions.toml               ← Versiones centralizadas de dependencias
```

---

## 4. Capas de la aplicación

### 4.1 Capa de datos (`data/`)

#### `FuturamaRepository.kt`

Objeto singleton que provee las preguntas del juego. Implementa dos estrategias:

**Estrategia primaria — API REST:**
- Realiza una petición HTTP GET a `https://api.sampleapis.com/futurama/info`
- Parsea la respuesta JSON con `org.json.JSONArray`
- Filtra únicamente el objeto con `id == 1` (el único con datos reales de Futurama)
- Extrae hasta 5 preguntas a partir de los campos: `yearsAired`, `creators[]` y `synopsis`
- Combina las preguntas de la API con el banco local y devuelve 10 mezcladas aleatoriamente

**Estrategia de respaldo — Banco local:**
- 15 preguntas hardcodeadas sobre personajes, lugares y datos de la serie
- Se usa automáticamente si la petición HTTP falla por cualquier motivo (sin conexión, timeout, error de parseo)

**Ejecución asíncrona:**
```kotlin
suspend fun loadQuestions(): List<TriviaQuestion> = withContext(Dispatchers.IO) { ... }
```
La función es `suspend` y corre en el hilo de I/O para no bloquear la UI.

#### `TriviaQuestion`

```kotlin
data class TriviaQuestion(
    val question: String,       // enunciado de la pregunta
    val options:  List<String>, // lista de 4 opciones de respuesta
    val correct:  Int,          // índice (0-based) de la opción correcta
)
```

---

### 4.2 Capa de lógica (ViewModel)

#### `QuizViewModel.kt`

Extiende `ViewModel` de Jetpack. Sobrevive a cambios de configuración (rotación de pantalla).

**Estado expuesto:**
```kotlin
val uiState: StateFlow<QuizUiState>
```
`StateFlow` inmutable que la UI observa. Cada actualización del estado provoca recomposición solo en los composables que consumen los datos modificados.

**`QuizUiState`** — modelo de estado completo:

| Campo | Tipo | Descripción |
|---|---|---|
| `screen` | `Screen` | Pantalla actualmente visible |
| `questions` | `List<TriviaQuestion>` | Preguntas de la partida actual |
| `currentIdx` | `Int` | Índice de la pregunta en curso (0-based) |
| `score` | `Int` | Puntos acumulados |
| `selected` | `Int?` | Opción elegida por el usuario (`null` = sin responder) |
| `answered` | `Boolean` | `true` cuando ya se eligió una opción |
| `isLoading` | `Boolean` | `true` durante la carga de la API |
| `error` | `String?` | Mensaje de error (`null` = sin error) |

**`Screen`** — enum de pantallas:
```kotlin
enum class Screen { WELCOME, LOADING, QUIZ, RESULTS }
```

**Métodos públicos:**

| Método | Descripción |
|---|---|
| `startGame()` | Reinicia el estado, muestra LOADING y carga preguntas desde el repositorio |
| `selectAnswer(idx: Int)` | Registra la respuesta, actualiza el score, activa el feedback visual |
| `nextQuestion()` | Avanza al siguiente índice o transiciona a RESULTS si era la última pregunta |

---

### 4.3 Capa de UI (Composables)

#### `MainActivity.kt`

Punto de entrada de la app. Extiende `ComponentActivity`. Llama a `enableEdgeToEdge()` para que el contenido ocupe toda la pantalla incluyendo las barras del sistema, y establece `FuturamaTriviaApp` como contenido raíz dentro del tema.

#### `FuturamaTriviaApp.kt`

Composable raíz que:
1. Observa el `uiState` del ViewModel con `collectAsStateWithLifecycle()`
2. Dibuja las capas de fondo (nebulosa + estrellas)
3. Navega entre pantallas con un `when` sobre `uiState.screen`

**Efectos visuales de fondo:**

| Capa | Técnica | Descripción |
|---|---|---|
| `NebulaLayer` | `Canvas` + `Brush.radialGradient` | 3 focos de luz difusa (morado, verde, naranja) estáticos |
| `StarFieldLayer` | `Canvas` + `InfiniteTransition` | 120 estrellas con brillo pulsante cada 3 segundos |

Las 120 estrellas se generan con funciones trigonométricas (`sin`, `cos`) para distribuirlas de forma pseudoaleatoria sin repetir patrones de grid.

---

## 5. Pantallas

### `WelcomeScreen`

Pantalla inicial. Muestra la identidad visual del juego y el botón de inicio.

**Elementos:**
- Badge circular con emoji 🚀 y borde verde
- Logotipo "FUTURAMA TRIVIA" con fuentes Orbitron
- Descripción y subtítulo en Exo 2
- Mensaje de error en rojo (visible solo si el intento anterior falló)
- Botón "INICIAR MISIÓN" → llama a `onStart()`
- Copyright con año dinámico vía `Calendar.getInstance()`

---

### `QuizScreen`

Pantalla principal del juego. Se recompone con cada cambio de pregunta o respuesta.

**Elementos:**
- Contador "PREGUNTA X / Y" y puntaje actual
- `LinearProgressIndicator` animado (avanza en 600ms)
- Tarjeta con el enunciado de la pregunta
- 4 botones de opción (`OptionButton`) con letras A-D
- Feedback de acierto/fallo tras responder
- Botón "SIGUIENTE →" o "VER RESULTADOS →"

**`OptionState`** — controla la apariencia de cada opción:

| Estado | Color de fondo | Borde | Opacidad | Icono |
|---|---|---|---|---|
| `IDLE` | Azul oscuro (CardBg) | Verde tenue | 100% | — |
| `CORRECT` | Verde 10% | Verde brillante | 100% | ✓ |
| `WRONG` | Rojo 10% | Rojo intenso | 100% | ✗ |
| `DIMMED` | Casi transparente | Muy tenue | 40% | — |

---

### `ResultsScreen`

Pantalla final. Muestra el puntaje y un mensaje personalizado según el porcentaje de aciertos.

**Niveles de resultado:**

| Umbral | Emoji | Personaje |
|---|---|---|
| ≥ 90% | 🤖 | Bender |
| ≥ 70% | 🧪 | Profesor Farnsworth |
| ≥ 40% | 🍕 | Fry |
| < 40% | 🦞 | Zoidberg |

**Elementos:**
- Emoji grande del nivel alcanzado
- Título en mayúsculas
- Círculo con `score/total` y porcentaje
- Descripción humorística
- Cita del personaje con acento de borde izquierdo (blockquote)
- Botón "JUGAR DE NUEVO" → llama a `onRestart()`

---

## 6. Sistema de diseño

#### `Theme.kt`

Define la paleta de colores, familias tipográficas y estilos de texto. Replica fielmente el archivo `theme.css` del proyecto web original.

**Paleta de colores:**

| Token | Valor hex | Uso |
|---|---|---|
| `Background` | `#05051A` | Fondo principal |
| `Foreground` | `#D4F1FF` | Texto principal |
| `CardBg` | `#0C1133` | Fondo de tarjetas y botones |
| `Primary` | `#00E5A0` | Verde neón — elementos principales |
| `PrimaryFg` | `#05051A` | Texto sobre fondo verde |
| `Secondary` | `#1A0A42` | Fondos secundarios |
| `Muted` | `#0A0F2E` | Fondos atenuados |
| `MutedFg` | `#5A7A9A` | Texto secundario y placeholders |
| `Accent` | `#FF6325` | Naranja — acentos decorativos |
| `Destructive` | `#FF3D5A` | Rojo — errores y respuestas incorrectas |
| `BorderColor` | `rgba(0,229,160,0.15)` | Bordes de tarjetas |
| `Green400` | `#4ADE80` | Retroalimentación de acierto |
| `Red400` / `Red500` | `#F87171` / `#EF4444` | Retroalimentación de error |

**Tipografía (`AppType`):**

| Estilo | Fuente | Peso | Tamaño | Uso |
|---|---|---|---|---|
| `displayLarge` | Orbitron | Black (900) | 56sp | Título "FUTURAMA" |
| `displayMedium` | Orbitron | Bold (700) | 22sp | Subtítulos y títulos de resultado |
| `labelTracked` | Exo 2 | Medium (500) | 11sp | Etiquetas con letterSpacing amplio |
| `bodySmall` | Exo 2 | Normal (400) | 13sp | Descripciones y textos secundarios |
| `bodyMedium` | Exo 2 | Normal (400) | 15sp | Texto de preguntas y opciones |
| `button` | Orbitron | Bold (700) | 14sp | Texto de botones CTA |
| `scoreDisplay` | Orbitron | Black (900) | 34sp | Puntaje en pantalla de resultados |

**Fuentes:**
- **Orbitron** (variable) — títulos y botones. Estética futurista/tecnológica.
- **Exo 2** (variable + itálica) — textos de cuerpo. Legible y moderna.

Ambas fuentes son variables (un solo archivo `.ttf` cubre todos los pesos).

---

## 7. Flujo de navegación

```
┌─────────────┐
│   WELCOME   │ ← estado inicial al abrir la app
└──────┬──────┘
       │ onStart()
       ▼
┌─────────────┐
│   LOADING   │ ← mientras FuturamaRepository.loadQuestions() corre
└──────┬──────┘
       │ preguntas cargadas
       ▼
┌─────────────┐
│    QUIZ     │ ← preguntas 1..10
│  (bucle)    │   selectAnswer() → nextQuestion() → repite
└──────┬──────┘
       │ última pregunta respondida
       ▼
┌─────────────┐
│   RESULTS   │
└──────┬──────┘
       │ onRestart() → vuelve a LOADING
       └──────────────────────────────┐
                                      ▼
                               (nueva partida)
```

Si la carga falla (excepción en el repositorio), el flujo regresa a **WELCOME** con un mensaje de error visible.

---

## 8. API externa

**Endpoint:** `GET https://api.sampleapis.com/futurama/info`  
**Autenticación:** ninguna  
**Formato de respuesta:** JSON array

La API devuelve un array de objetos. Solo el objeto con `"id": 1` contiene datos reales de Futurama. Los demás son datos generados aleatoriamente y se ignoran.

**Campos utilizados del objeto `id=1`:**

| Campo | Tipo | Pregunta generada |
|---|---|---|
| `yearsAired` | `String` | ¿En qué años se emitió Futurama? |
| `creators[0].name` | `String` | ¿Quién co-creó Futurama junto a Matt Groening? |
| `creators[1].name` | `String` | ¿Quién creó Los Simpsons Y Futurama? |
| `synopsis` | `String` | Detecta "New New York" y "1999"/"2999" para generar preguntas |

**Manejo de errores:**
- Cualquier excepción (`IOException`, `JSONException`, etc.) es capturada silenciosamente
- La app cae automáticamente al banco de 15 preguntas locales
- El usuario no ve un mensaje de error durante la carga (solo si la excepción llega al ViewModel)

---

## 9. Dependencias

| Dependencia | Versión | Uso |
|---|---|---|
| `androidx.core:core-ktx` | 1.18.0 | Extensiones Kotlin para Android |
| `androidx.activity:activity-compose` | 1.10.1 | `ComponentActivity` con soporte Compose |
| `androidx.compose:compose-bom` | 2025.02.00 | BOM para versiones de Compose |
| `androidx.compose.ui:ui` | (BOM) | Motor de renderizado Compose |
| `androidx.compose.material3:material3` | (BOM) | Componentes Material 3 |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | 2.8.7 | `viewModel()` en composables |
| `androidx.lifecycle:lifecycle-runtime-compose` | 2.8.7 | `collectAsStateWithLifecycle()` |
| `kotlinx-coroutines-android` | 1.9.0 | Corrutinas para llamadas asíncronas |

---

## 10. Configuración del proyecto

**`app/build.gradle.kts`**

```kotlin
android {
    namespace      = "com.example.futuramaapp"
    compileSdk     = 35
    defaultConfig {
        applicationId = "com.example.futuramaapp"
        minSdk        = 26   // Android 8.0 Oreo
        targetSdk     = 35
    }
    buildFeatures { compose = true }
}
```

**`gradle/libs.versions.toml`** — versiones clave:

```toml
agp    = "9.0.1"
kotlin = "2.0.21"
```

**`AndroidManifest.xml`** — permiso requerido:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Sin este permiso, todas las partidas usarán el banco local de preguntas.

---

## 11. Instalación y requisitos

**Requisitos mínimos:**
- Android Studio Hedgehog o superior
- JDK 11
- Android SDK 35
- Dispositivo o emulador con Android 8.0 (API 26) o superior

**Pasos:**
1. Clonar o copiar los archivos al proyecto en Android Studio
2. Copiar las fuentes `orbitron.ttf`, `exo2.ttf` y `exo2_italic.ttf` a `app/src/main/res/font/`
3. Verificar que `AndroidManifest.xml` declara el permiso `INTERNET`
4. Ejecutar **File → Sync Project with Gradle Files**
5. Ejecutar en dispositivo o emulador con **Run → Run 'app'**

**Notas:**
- La primera vez que se inicia una partida, la app hace una petición de red. Si el dispositivo no tiene conexión a internet, la app funciona igualmente con las preguntas locales.
- Las fuentes son variables: un solo archivo `.ttf` cubre todos los pesos declarados en `Theme.kt`.
