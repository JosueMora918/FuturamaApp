package com.example.futuramaapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futuramaapp.ui.theme.AppType
import com.example.futuramaapp.ui.theme.Foreground
import com.example.futuramaapp.ui.theme.MutedFg
import com.example.futuramaapp.ui.theme.Primary
import com.example.futuramaapp.ui.theme.PrimaryFg

// Datos del resultado: emoji, título, descripción y cita de un personaje.
// Agrupa el contenido dinámico que cambia según el puntaje obtenido.
data class ResultData(
    val emoji:    String, // emoji representativo del nivel alcanzado
    val title:    String, // título principal en mayúsculas
    val subtitle: String, // descripción humorística del resultado
    val quote:    String, // cita de un personaje de Futurama relacionada al nivel
)

// Lógica de resultado según puntaje
// Convierte el puntaje a porcentaje y devuelve el ResultData correspondiente.
// Umbrales: ≥90% perfecto | ≥70% excelente | ≥40% regular | <40% malo
fun getResult(score: Int, total: Int): ResultData {
    val pct = score.toFloat() / total  // valor entre 0f y 1f
    return when {
        pct >= 0.9f -> ResultData(
            "🤖",
            "¡PERFECTO, CARNE HUMANA!",
            "Incluso Bender está impresionado… aunque nunca lo admitirá en público.",
            "\"¡Soy magnífico! Y esta vez, tú también.\" — Bender Bending Rodriguez",
        )
        pct >= 0.7f -> ResultData(
            "🧪",
            "¡EXCELENTE, BUEN CHICO!",
            "El Profesor confirma que tu inteligencia supera a la de un robot promedio.",
            "\"¡Buenas noticias a todos!\" — Profesor Hubert J. Farnsworth",
        )
        pct >= 0.4f -> ResultData(
            "🍕",
            "NO ESTÁ MAL, HOMBRE DEL PASADO",
            "Fry lo haría igual de bien. Y él es de 1999, así que eso dice algo.",
            "\"Soy lo suficientemente bueno para esto.\" — Philip J. Fry",
        )
        else -> ResultData(
            "🦞",
            "¡HOORAY, SOY INÚTIL!",
            "Hasta Zoidberg sabe más sobre el año 3000 que tú. Posiblemente.",
            "\"¡Hooray, I'm useful! I'm helping!\" — Dr. John A. Zoidberg",
        )
    }
}

// Pantalla de resultados
// Muestra el puntaje final, un mensaje personalizado según el nivel y
// un botón para reiniciar la partida. No tiene estado propio (stateless).
@Composable
fun ResultsScreen(
    score:     Int,        // puntos obtenidos en la partida
    total:     Int,        // total de preguntas respondidas
    onRestart: () -> Unit, // callback que inicia una nueva partida
) {
    // Calculamos el resultado y el porcentaje una sola vez
    val result = getResult(score, total)
    val pct    = ((score.toFloat() / total) * 100).toInt() // ej. 70 para 7/10

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))

        // Emoji grande que representa el nivel del resultado
        Text(result.emoji, fontSize = 56.sp)

        Spacer(Modifier.height(16.dp))

        // Título del resultado en mayúsculas con fuente display
        Text(
            text      = result.title,
            style     = AppType.displayMedium.copy(fontSize = 22.sp, letterSpacing = 2.sp),
            color     = Foreground,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))

        // Círculo de puntaje con borde verde
        // Box circular con borde verde que muestra "score/total" y el porcentaje.
        // El borde usa CircleShape para que sea perfectamente redondo.
        Box(
            modifier = Modifier
                .size(140.dp)
                .border(2.dp, Primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Puntaje principal: "7/10"
                Text(
                    text  = "$score/$total",
                    style = AppType.scoreDisplay,
                    color = Primary,
                )
                Spacer(Modifier.height(2.dp))
                // Porcentaje secundario: "70%"
                Text(
                    text  = "$pct%",
                    style = AppType.labelTracked.copy(fontSize = 11.sp),
                    color = MutedFg,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Descripción humorística del nivel obtenido
        Text(
            text      = result.subtitle,
            style     = AppType.bodySmall,
            color     = MutedFg,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(16.dp))

        // Cita de personaje con borde izquierdo
        // Patrón común para blockquotes: línea vertical de color + texto indentado.
        // El Canvas dibuja un rectángulo de 2dp de ancho como acento decorativo.
        Row(modifier = Modifier.fillMaxWidth()) {
            // Línea vertical verde semi-transparente (acento de cita)
            Box(
                Modifier
                    .width(2.dp)
                    .height(48.dp)
                    .padding(top = 2.dp)
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    drawRect(Primary.copy(alpha = 0.4f))
                }
            }
            Spacer(Modifier.width(12.dp))
            // Texto de la cita del personaje
            Text(
                text  = result.quote,
                style = AppType.bodySmall.copy(fontSize = 12.sp),
                color = MutedFg,
            )
        }

        Spacer(Modifier.height(32.dp))

        // Botón para iniciar una nueva partida
        Button(
            onClick  = onRestart,
            colors   = ButtonDefaults.buttonColors(
                containerColor = Primary,
                contentColor   = PrimaryFg,
            ),
            shape    = RoundedCornerShape(4.dp),
            modifier = Modifier
                .fillMaxWidth(0.80f)
                .height(52.dp),
        ) {
            Text("JUGAR DE NUEVO", style = AppType.button, color = PrimaryFg)
        }
    }
}