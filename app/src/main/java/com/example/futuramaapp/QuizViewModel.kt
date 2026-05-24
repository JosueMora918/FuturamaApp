package com.example.futuramaapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futuramaapp.data.FuturamaRepository
import com.example.futuramaapp.data.TriviaQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Pantallas posibles de la app; el ViewModel decide cuál mostrar
enum class Screen { WELCOME, LOADING, QUIZ, RESULTS }

// Estado de la UI
// data class inmutable que representa toda la información que la UI necesita
// para renderizarse. Cada cambio genera una nueva copia (patrón UDF).
data class QuizUiState(
    val screen:    Screen               = Screen.WELCOME,  // pantalla actualmente visible
    val questions: List<TriviaQuestion> = emptyList(),     // preguntas cargadas de la API/fallback
    val currentIdx: Int                 = 0,               // índice de la pregunta actual (0-based)
    val score:     Int                  = 0,               // puntos acumulados
    val selected:  Int?                 = null,            // índice de la opción elegida (null = sin responder)
    val answered:  Boolean              = false,           // true cuando el usuario ya eligió una opción
    val isLoading: Boolean              = false,           // true mientras se carga la API
    val error:     String?              = null,            // mensaje de error (null = sin error)
)

// ViewModel del quiz
// Gestiona el estado del juego y la lógica de negocio.
// Sobrevive a los cambios de configuración (rotación de pantalla, etc.).
class QuizViewModel : ViewModel() {

    // StateFlow privado y mutable — solo el ViewModel puede modificarlo
    private val _uiState = MutableStateFlow(QuizUiState())

    // StateFlow público e inmutable — la UI solo puede leerlo
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // Atajo de solo lectura para obtener la pregunta actual sin exponer el estado completo
    val currentQuestion get() = _uiState.value.run {
        questions.getOrNull(currentIdx)
    }

    // Inicia una nueva partida
    // Muestra la pantalla de carga, llama al repositorio en una corrutina
    // y transiciona a QUIZ con las preguntas cargadas (o muestra error si falla).
    fun startGame() {
        // Reiniciamos el estado completo y mostramos el indicador de carga
        _uiState.value = QuizUiState(screen = Screen.LOADING, isLoading = true)

        // viewModelScope cancela la corrutina automáticamente si el ViewModel se destruye
        viewModelScope.launch {
            try {
                val questions = FuturamaRepository.loadQuestions()
                _uiState.value = QuizUiState(
                    screen    = Screen.QUIZ,
                    questions = questions,
                )
            } catch (e: Exception) {
                // Si la carga falla volvemos a WELCOME con un mensaje de error
                _uiState.value = QuizUiState(
                    screen = Screen.WELCOME,
                    error  = "Error al cargar preguntas. Intenta de nuevo.",
                )
            }
        }
    }

    // Registra la respuesta del usuario
    // Ignora llamadas duplicadas (si ya respondió no hace nada).
    // Incrementa el score si la opción elegida es la correcta.
    fun selectAnswer(idx: Int) {
        val state = _uiState.value
        if (state.answered) return                      // protección contra doble toque
        val correct = currentQuestion?.correct ?: return // null-safety

        _uiState.value = state.copy(
            selected = idx,
            answered = true,
            score    = if (idx == correct) state.score + 1 else state.score,
        )
    }

    // Avanza a la siguiente pregunta o termina la partida según corresponda
    // Si ya se respondieron todas las preguntas transiciona a RESULTS
    // de lo contrario incrementa el índice y limpia la selección anterior
    fun nextQuestion() {
        val state = _uiState.value

        if (state.currentIdx + 1 >= state.questions.size) {
            // Última pregunta para ir a resultados (conservamos el score)
            _uiState.value = state.copy(screen = Screen.RESULTS)
        } else {
            // Pregunta intermedia para avanzar y limpiar respuesta anterior
            _uiState.value = state.copy(
                currentIdx = state.currentIdx + 1,
                selected   = null,   // ninguna opción seleccionada
                answered   = false,  // habilita los botones de nuevo
            )
        }
    }
}