package com.example.futuramaapp

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futuramaapp.ui.theme.AppType
import com.example.futuramaapp.ui.theme.Destructive
import com.example.futuramaapp.ui.theme.Foreground
import com.example.futuramaapp.ui.theme.MutedFg
import com.example.futuramaapp.ui.theme.Primary
import com.example.futuramaapp.ui.theme.PrimaryFg
import java.util.Calendar

// Pantalla de bienvenida
// Primera pantalla que ve el usuario. Es stateless: recibe un callback para
// iniciar el juego y opcionalmente un mensaje de error de la carga anterior.
@Composable
fun WelcomeScreen(
    onStart:  () -> Unit,      // callback que dispara startGame() en el ViewModel
    errorMsg: String? = null,  // mensaje de error de un intento fallido (null = sin error)
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))

        // Badge circular con cohete
        // Box circular con borde verde del tema; el shadow con elevation 0
        // se usa solo para aplicar la forma al recorte de sombra sin elevar el elemento.
        Box(
            modifier = Modifier
                .size(96.dp)
                .border(2.dp, Primary, CircleShape)
                .shadow(elevation = 0.dp, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("🚀", fontSize = 40.sp)
        }

        Spacer(Modifier.height(20.dp))

        // Etiqueta superior con espaciado de letras amplio (estilo "label tracked")
        Text(
            text      = "PLANET EXPRESS — AÑO 3000",
            style     = AppType.labelTracked,
            color     = Primary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        // Título principal "FUTURAMA" con la fuente display grande (Orbitron Black)
        Text(
            text      = "FUTURAMA",
            style     = AppType.displayLarge,
            color     = Foreground,
            textAlign = TextAlign.Center,
        )

        // Subtítulo "TRIVIA" en color verde primario, fuente display mediana
        Text(
            text      = "TRIVIA",
            style     = AppType.displayMedium,
            color     = Primary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(20.dp))

        // Descripción humorística que hace referencia al origen de Fry
        Text(
            text      = "Demuestra que eres más que un simple repartidor de pizzas del año 1999.",
            style     = AppType.bodySmall,
            color     = MutedFg,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(6.dp))

        // Línea secundaria con el número de preguntas y referencia al Profesor
        Text(
            text      = "10 preguntas aleatorias · ¡Good news, everyone!",
            style     = AppType.bodySmall.copy(fontSize = 11.sp, letterSpacing = 1.sp),
            color     = MutedFg,
            textAlign = TextAlign.Center,
        )

        // Mensaje de error (solo visible si el intento anterior falló)
        // Usa el operador let para mostrar el bloque solo cuando errorMsg != null
        errorMsg?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                text      = it,
                style     = AppType.bodySmall,
                color     = Destructive, // rojo del tema (#FF3D5A)
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(32.dp))

        // Botón principal "INICIAR MISIÓN" (CTA)
        // fillMaxWidth(0.75f) lo centra con márgenes laterales sin necesitar padding extra
        Button(
            onClick  = onStart,
            colors   = ButtonDefaults.buttonColors(
                containerColor = Primary,   // fondo verde neón
                contentColor   = PrimaryFg, // texto oscuro para contraste
            ),
            shape    = RoundedCornerShape(4.dp), // esquinas casi rectas, estilo "futurista"
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(52.dp),
        ) {
            Text("INICIAR MISIÓN", style = AppType.button, color = PrimaryFg)
        }

        Spacer(Modifier.height(32.dp))

        // Pie de página con el año actual calculado dinámicamente
        // Calendar.getInstance() evita hardcodear el año
        Text(
            text  = "© ${Calendar.getInstance().get(Calendar.YEAR)} PLANET EXPRESS INC.",
            style = AppType.bodySmall.copy(fontSize = 10.sp, letterSpacing = 3.sp),
            color = MutedFg.copy(alpha = 0.4f), // muy tenue, casi invisible
            textAlign = TextAlign.Center,
        )
    }
}