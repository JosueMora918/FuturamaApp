package com.example.futuramaapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontStyle
import com.example.futuramaapp.R

// Paleta de colores tomada directamente de Figma
val Background      = Color(0xFF05051A)
val Foreground      = Color(0xFFD4F1FF)
val CardBg          = Color(0xFF0C1133)
val Primary         = Color(0xFF00E5A0)
val PrimaryFg       = Color(0xFF05051A)
val Secondary       = Color(0xFF1A0A42)
val Muted           = Color(0xFF0A0F2E)
val MutedFg         = Color(0xFF5A7A9A)
val Accent          = Color(0xFFFF6325)
val Destructive     = Color(0xFFFF3D5A)
val BorderColor     = Color(0x26E5A000)
val InputBg         = Color(0xFF0C1133)

val Green400        = Color(0xFF4ADE80)
val Red400          = Color(0xFFF87171)
val Red500          = Color(0xFFEF4444)

// Aqui usamos las diferentes fuentes de la app
// Orbitron para el display y Exo 2 para el cuerpo
// Si las fuentes no se encuentran, usa la fuente default
// Como son fuentes variables, hay que especificar el estilo
val OrbitronFamily = FontFamily(
    Font(R.font.orbitron, FontWeight.Normal),
    Font(R.font.orbitron, FontWeight.Bold),
    Font(R.font.orbitron, FontWeight.Black),
)

val Exo2Family = FontFamily(
    Font(R.font.exo2, FontWeight.Normal),
    Font(R.font.exo2, FontWeight.Medium),
    Font(R.font.exo2, FontWeight.Bold),
    Font(R.font.exo2_italic, FontWeight.Normal, FontStyle.Italic),
)

// Esquema de colores
private val DarkColors = darkColorScheme(
    background      = Background,
    surface         = CardBg,
    primary         = Primary,
    onPrimary       = PrimaryFg,
    onBackground    = Foreground,
    onSurface       = Foreground,
    secondary       = Secondary,
    onSecondary     = Foreground,
    error           = Destructive,
)

// Ayudantes para la tipografia
object AppType {
    val displayLarge = TextStyle(
        fontFamily = OrbitronFamily,
        fontWeight = FontWeight.Black,
        fontSize   = 56.sp,
        lineHeight = 60.sp,
        letterSpacing = 0.sp,
    )
    val displayMedium = TextStyle(
        fontFamily = OrbitronFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 22.sp,
        letterSpacing = 4.sp,
    )
    val labelTracked = TextStyle(
        fontFamily  = Exo2Family,
        fontWeight  = FontWeight.Medium,
        fontSize    = 11.sp,
        letterSpacing = 4.sp,
    )
    val bodySmall = TextStyle(
        fontFamily = Exo2Family,
        fontWeight = FontWeight.Normal,
        fontSize   = 13.sp,
        lineHeight = 20.sp,
    )
    val bodyMedium = TextStyle(
        fontFamily = Exo2Family,
        fontWeight = FontWeight.Normal,
        fontSize   = 15.sp,
        lineHeight = 22.sp,
    )
    val button = TextStyle(
        fontFamily  = OrbitronFamily,
        fontWeight  = FontWeight.Bold,
        fontSize    = 14.sp,
        letterSpacing = 3.sp,
    )
    val scoreDisplay = TextStyle(
        fontFamily = OrbitronFamily,
        fontWeight = FontWeight.Black,
        fontSize   = 34.sp,
    )
}

// El wrapper para aplicar el tema en toda la aplicacion
@Composable
fun FuturamaTriviaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content     = content,
    )
}
