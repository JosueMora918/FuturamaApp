package com.example.futuramaapp

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futuramaapp.data.TriviaQuestion
import com.example.futuramaapp.ui.theme.AppType
import com.example.futuramaapp.ui.theme.BorderColor
import com.example.futuramaapp.ui.theme.CardBg
import com.example.futuramaapp.ui.theme.Foreground
import com.example.futuramaapp.ui.theme.Green400
import com.example.futuramaapp.ui.theme.Muted
import com.example.futuramaapp.ui.theme.MutedFg
import com.example.futuramaapp.ui.theme.Primary
import com.example.futuramaapp.ui.theme.PrimaryFg
import com.example.futuramaapp.ui.theme.Red400
import com.example.futuramaapp.ui.theme.Red500

// Letras que se muestran en el badge de cada opción
private val LETTERS = listOf("A", "B", "C", "D")

// Estados visuales posibles para cada opción de respuesta:
// IDLE porque el usuario todavía no ha respondido
// CORRECT es la opción correcta (se resalta en verde)
// WRONG porque el usuario eligió esta opción pero es incorrecta (se resalta en rojo)
// DIMMED porque ésta opción que no fue elegida ni es la correcta (se atenúa)
enum class OptionState { IDLE, CORRECT, WRONG, DIMMED }

// Pantalla de pregunta
// Composable "sin estado" (stateless): recibe todos los datos del ViewModel
// y notifica las acciones del usuario mediante callbacks (onSelect, onNext).
@Composable
fun QuizScreen(
    question:       TriviaQuestion, // datos de la pregunta actual
    questionNumber: Int,            // posición actual, ej. 3 (para mostrar "3 / 10")
    total:          Int,            // total de preguntas de la partida
    score:          Int,            // puntos acumulados hasta ahora
    selected:       Int?,           // índice de la opción tocada (null = sin responder)
    answered:       Boolean,        // true cuando el usuario ya eligió una opción
    isLast:         Boolean,        // true si esta es la última pregunta
    onSelect:       (Int) -> Unit,  // se invoca con el índice de la opción al tocarla
    onNext:         () -> Unit,     // se invoca al pulsar "Siguiente" o "Ver resultados"
) {
    // Animamos el valor de progreso: al cambiar de pregunta avanza suavemente en 600ms
    val progress by animateFloatAsState(
        targetValue   = (questionNumber - 1).toFloat() / total,
        animationSpec = tween(600),
        label         = "progress",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Fila superior: contador de preguntas y puntaje
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // "PREGUNTA 3 / 10"
            Text(
                text  = "PREGUNTA $questionNumber / $total",
                style = AppType.labelTracked.copy(fontSize = 10.sp),
                color = MutedFg,
            )
            // Puntaje con estrella decorativa a la derecha
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("★", color = Primary, fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    text  = score.toString(),
                    style = AppType.button.copy(fontSize = 14.sp),
                    color = Foreground,
                )
                Spacer(Modifier.width(3.dp))
                Text("pts", style = AppType.bodySmall.copy(fontSize = 11.sp), color = MutedFg)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Barra de progreso
        // Valor entre 0f y 1f; usa el progreso para evitar recomposiciones innecesarias del indicador cuando el valor cambia.
        LinearProgressIndicator(
            progress      = { progress },
            modifier      = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(RoundedCornerShape(2.dp)),
            color         = Primary,      // color del progreso completado
            trackColor    = Muted,        // color del fondo de la barra
            strokeCap     = StrokeCap.Round,
        )

        Spacer(Modifier.height(24.dp))

        // Tarjeta con el enunciado de la pregunta
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(CardBg)
                .border(1.dp, BorderColor, RoundedCornerShape(4.dp))
                .padding(20.dp),
        ) {
            Text(
                text      = question.question,
                style     = AppType.bodyMedium.copy(fontSize = 17.sp),
                color     = Foreground,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(16.dp))

        // Opciones de respuesta
        // Iteramos las 4 opciones y calculamos el estado visual de cada una.
        // Antes de responder todas son IDLE; después se asignan según el resultado.
        question.options.forEachIndexed { idx, opt ->
            val state = when {
                !answered               -> OptionState.IDLE    // aún no respondió
                idx == question.correct -> OptionState.CORRECT // siempre se muestra la correcta
                idx == selected         -> OptionState.WRONG   // la que eligió el usuario (incorrecta)
                else                    -> OptionState.DIMMED  // las demás se atenúan
            }
            OptionButton(
                letter  = LETTERS[idx],   // "A", "B", "C" o "D"
                text    = opt,
                state   = state,
                onClick = { onSelect(idx) },
                enabled = !answered,      // se deshabilitan tras responder
            )
            Spacer(Modifier.height(10.dp))
        }

        // Feedback y el botón de avance (aparece solo después de responder)
        if (answered) {
            val isCorrect = selected == question.correct

            Spacer(Modifier.height(4.dp))

            // Mensaje de acierto o fallo con referencia a Bender si falla
            Text(
                text  = if (isCorrect) "¡Correcto! +1 punto" else "¡Incorrecto! Bite my shiny metal…",
                style = AppType.button.copy(fontSize = 12.sp),
                color = if (isCorrect) Green400 else Red400,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(16.dp))

            // En la última pregunta el texto cambia a "VER RESULTADOS"
            Button(
                onClick  = onNext,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor   = PrimaryFg,
                ),
                shape    = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp),
            ) {
                Text(
                    if (isLast) "VER RESULTADOS →" else "SIGUIENTE →",
                    style = AppType.button.copy(fontSize = 12.sp),
                    color = PrimaryFg,
                )
            }
        }
    }
}

// Botón de opción individual
// Composable privado que renderiza una sola opción de respuesta.
// Todo su aspecto visual (colores, borde, opacidad) depende del OptionState recibido.
@Composable
private fun OptionButton(
    letter:  String,      // letra del badge: "A", "B", "C" o "D"
    text:    String,      // texto de la opción
    state:   OptionState, // define la apariencia visual
    onClick: () -> Unit,
    enabled: Boolean,     // false después de que el usuario ya respondió
) {
    // Cada propiedad visual tiene un valor distinto por estado

    // Color del borde
    val borderColor = when (state) {
        OptionState.CORRECT -> Green400                      // verde brillante
        OptionState.WRONG   -> Red500                        // rojo intenso
        OptionState.DIMMED  -> BorderColor.copy(alpha = 0.2f) // casi invisible
        OptionState.IDLE    -> BorderColor                   // verde semi-transparente del tema
    }

    // Color de fondo del botón
    val bgColor = when (state) {
        OptionState.CORRECT -> Green400.copy(alpha = 0.10f)  // verde muy suave
        OptionState.WRONG   -> Red500.copy(alpha = 0.10f)    // rojo muy suave
        OptionState.DIMMED  -> CardBg.copy(alpha = 0.20f)    // casi transparente
        OptionState.IDLE    -> CardBg                        // azul oscuro del tema
    }

    // Color del texto de la opción
    val textColor = when (state) {
        OptionState.CORRECT -> Color(0xFFBBF7D0)  // verde muy claro
        OptionState.WRONG   -> Red400
        OptionState.DIMMED  -> MutedFg
        OptionState.IDLE    -> Foreground
    }

    // Color de fondo del badge circular con la letra
    val badgeBg = when (state) {
        OptionState.CORRECT -> Green400
        OptionState.WRONG   -> Red500
        else                -> Muted  // gris oscuro en IDLE y DIMMED
    }

    // Color del texto dentro del badge
    val badgeFg = when (state) {
        OptionState.CORRECT -> Color.Black
        OptionState.WRONG   -> Color.White
        else                -> MutedFg
    }

    // Layout: [badge] [texto] [palomita o cruz]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .alpha(if (state == OptionState.DIMMED) 0.4f else 1f) // atenúa las opciones DIMMED
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Badge circular con la letra (A / B / C / D)
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(badgeBg, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(letter, style = AppType.button.copy(fontSize = 11.sp), color = badgeFg)
        }

        Spacer(Modifier.width(12.dp))

        // Texto de la opción — toma el espacio restante de la fila
        Text(
            text     = text,
            style    = AppType.bodyMedium,
            color    = textColor,
            modifier = Modifier.weight(1f),
        )

        // Icono al final: palomita verde si correcta, cruz roja si incorrecta, nada en los demás casos
        when (state) {
            OptionState.CORRECT -> Text("✓", color = Green400, fontSize = 18.sp)
            OptionState.WRONG   -> Text("✗", color = Red400,   fontSize = 18.sp)
            else                -> {}
        }
    }
}