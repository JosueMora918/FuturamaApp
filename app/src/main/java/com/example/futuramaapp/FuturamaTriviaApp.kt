package com.example.futuramaapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.futuramaapp.ui.theme.Background
import com.example.futuramaapp.ui.theme.Primary
import kotlin.math.*

// Funciones que controlan las estrellas del fondo
// Cada estrella tiene una posición (topFraction, leftFraction) expresada como
// porcentaje de la pantalla, un tamaño en dp y una transparencia base.
private data class Star(
    val topFraction:  Float,
    val leftFraction: Float,
    val sizeDp:       Float,
    val baseAlpha:    Float,
)

// Generamos 120 estrellas con posiciones y tamaños aleatorios usando
// funciones seno/coseno
private val STARS: List<Star> = List(120) { i ->
    Star(
        topFraction  = ((sin(i * 7.31 + 1.2) * 0.5 + 0.5) * 100).toFloat(),
        leftFraction = ((cos(i * 5.17 + 0.8) * 0.5 + 0.5) * 100).toFloat(),
        sizeDp       = ((sin(i * 3.73) * 0.5 + 0.5) * 2.5 + 0.5).toFloat(),
        baseAlpha    = ((cos(i * 2.9) * 0.5 + 0.5) * 0.6 + 0.2).toFloat(),
    )
}

// Observa el estado del ViewModel y muestra la pantalla correspondiente.
// Las capas de fondo (nebulosa + estrellas) siempre están visibles.
@Composable
fun FuturamaTriviaApp(viewModel: QuizViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        // Brillo de la nebulosa
        NebulaLayer()

        // Campo de estrellas
        StarFieldLayer()

        // Contenido principal con scroll vertical
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {  // Pantalla de bienvenida
            when (uiState.screen) {
                Screen.WELCOME -> WelcomeScreen(
                    onStart = { viewModel.startGame() },
                    errorMsg = uiState.error,
                )
                Screen.LOADING -> CircularProgressIndicator(color = Primary)
                Screen.QUIZ -> { // Pantalla de preguntas con la pregunta y las respuestas
                    val q = uiState.questions.getOrNull(uiState.currentIdx)
                    if (q != null) {
                        QuizScreen(
                            question = q,
                            questionNumber = uiState.currentIdx + 1,
                            total = uiState.questions.size,
                            score = uiState.score,
                            selected = uiState.selected,
                            answered = uiState.answered,
                            isLast = uiState.currentIdx + 1 == uiState.questions.size,
                            onSelect = viewModel::selectAnswer,
                            onNext = viewModel::nextQuestion,
                        )
                    }
                } // Pantalla de resultados
                Screen.RESULTS -> ResultsScreen(
                    score = uiState.score,
                    total = uiState.questions.size,
                    onRestart = { viewModel.startGame() },
                )
            }
        }
    }
}

// Campo de estrellas animado
// Dibuja 120 círculos blancos con alpha pulsante usando Canvas de Compose
// La animación hace que todas las estrellas brillen y se apaguen en ciclo de 3 segundos
@Composable
private fun StarFieldLayer() {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val pulse by infiniteTransition.animateFloat(
        initialValue   = 0f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        STARS.forEach { star ->
            val x      = star.leftFraction / 100f * size.width
            val y      = star.topFraction  / 100f * size.height
            val radius = star.sizeDp.dp.toPx() / 2f
            val alpha  = (star.baseAlpha * (0.6f + 0.4f * pulse)).coerceIn(0f, 1f)
            drawCircle(
                color  = Color.White.copy(alpha = alpha),
                radius = radius,
                center = Offset(x, y),
            )
        }
    }
}

// Capa de nebulosa
// Dibuja 3 focos de luz difusa con un efecto borroso para simular el fondo
@Composable
private fun NebulaLayer() {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        // Brillo morado arriba a la izquierda
        drawCircle(
            brush  = Brush.radialGradient(
                colors      = listOf(Color(0x1F7C3AED), Color.Transparent),
                center      = Offset(size.width * 0.30f, -size.height * 0.10f),
                radius      = 600f,
            ),
            radius = 600f,
            center = Offset(size.width * 0.30f, -size.height * 0.10f),
        )
        // Brillo verde abajo a la derecha
        drawCircle(
            brush  = Brush.radialGradient(
                colors      = listOf(Color(0x1400E5A0), Color.Transparent),
                center      = Offset(size.width * 1.05f, size.height * 0.95f),
                radius      = 500f,
            ),
            radius = 500f,
            center = Offset(size.width * 1.05f, size.height * 0.95f),
        )
        // Brillo naranja en medio a la izquierda
        drawCircle(
            brush  = Brush.radialGradient(
                colors      = listOf(Color(0x12FF6325), Color.Transparent),
                center      = Offset(-size.width * 0.05f, size.height * 0.50f),
                radius      = 400f,
            ),
            radius = 400f,
            center = Offset(-size.width * 0.05f, size.height * 0.50f),
        )
    }
}
