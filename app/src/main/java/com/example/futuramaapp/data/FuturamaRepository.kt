package com.example.futuramaapp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

data class TriviaQuestion(
    val question: String,
    val options:  List<String>,
    val correct:  Int,
)

// Aqui tenemoos preguntas hardcodeadas para cuando la API no responde
val FALLBACK_QUESTIONS = listOf(
    TriviaQuestion("¿Cómo se llama la empresa de entregas donde trabaja Fry?",
        listOf("Omicron Express","Planet Express","Spaceway Delivery","FutureCorp"), 1),
    TriviaQuestion("¿En qué año despertó Fry tras ser criocongelado?",
        listOf("2999","2500","3000","3501"), 2),
    TriviaQuestion("¿Cuál es el nombre completo de Bender?",
        listOf("Bender Rodriguez","Bender James Rodriguez","Bender B. Rodriguez","Bender Bending Rodriguez"), 3),
    TriviaQuestion("¿Cuántos ojos tiene Leela?",
        listOf("Dos","Tres","Ninguno","Uno"), 3),
    TriviaQuestion("¿Quién dirige Planet Express?",
        listOf("Zapp Brannigan","El Profesor Farnsworth","Mom","Nixon"), 1),
    TriviaQuestion("¿Cuál es la bebida gaseosa más popular del año 3000?",
        listOf("Coca-Fuela","Slurm","Nuka-Cola","Fizzbang"), 1),
    TriviaQuestion("¿Cómo se llama la megacorporación de 'Mom'?",
        listOf("Omnicorp","MomCorp","Glomex","FutureTech"), 1),
    TriviaQuestion("¿Qué especie es el Dr. Zoidberg?",
        listOf("Omicroniano","Neptuniano","Decapodiano","Anfibio"), 2),
    TriviaQuestion("¿Quién dice frecuentemente '¡Buenas noticias a todos!'?",
        listOf("Fry","Bender","Leela","El Profesor Farnsworth"), 3),
    TriviaQuestion("¿En qué ciudad está ubicada Planet Express?",
        listOf("Vieja Nueva York","Nueva Los Ángeles","Neo Chicago","Nueva Nueva York"), 3),
    TriviaQuestion("¿Cuál era el trabajo de Fry antes de ser congelado?",
        listOf("Mecánico","Repartidor de pizzas","Programador","Chef"), 1),
    TriviaQuestion("¿Cómo se llama el diablo robot en Futurama?",
        listOf("Rob Bot","Satan 2.0","Beelzebot","Metalbot"), 2),
    TriviaQuestion("¿Cuál es el apellido de Leela?",
        listOf("Fry","Wong","Rodriguez","Turanga"), 3),
    TriviaQuestion("¿De qué planeta son los Omicronians?",
        listOf("Omicron Persei 7","Omicron Persei 8","Decapod 10","Vergon 6"), 1),
    TriviaQuestion("¿Cuál es el nombre completo del Profesor?",
        listOf("Hubert J. Farnsworth","Herbert J. Farnsworth","Hubert B. Farnsworth","Herbert B. Farnsworth"), 0),
)

object FuturamaRepository {
// Aqui llamamos a la API y parseamos la respuesta
    private const val API_URL = "https://api.sampleapis.com/futurama/info"

    suspend fun loadQuestions(): List<TriviaQuestion> = withContext(Dispatchers.IO) {
        try {
            val json  = URL(API_URL).readText()
            val array = JSONArray(json)
            val apiQs = mutableListOf<TriviaQuestion>()

            // El detalle es que la API esta llena de datos basura y solo los que tienen el ID 1 son reales (que son 5)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                if (obj.optInt("id") != 1) continue

                // Pregunta: años al aire
                val yearsAired = obj.optString("yearsAired", "")
                if (yearsAired.isNotBlank()) {
                    val opts = listOf(yearsAired, "1993–2001", "2001–2005", "2010–2015").shuffled()
                    apiQs.add(TriviaQuestion(
                        question = "¿En qué años se emitió Futurama originalmente?",
                        options  = opts,
                        correct  = opts.indexOf(yearsAired),
                    ))
                }

                //  Preguntas: creadores
                val creators = obj.optJSONArray("creators")
                if (creators != null) {
                    val names = (0 until creators.length()).map {
                        creators.getJSONObject(it).optString("name", "")
                    }.filter { it.isNotBlank() }

                    if (names.isNotEmpty()) {
                        val correct = names[0]   // David X. Cohen
                        val distractors = listOf("J.J. Abrams", "Seth MacFarlane", "Trey Parker", "Dan Harmon")
                        val opts = (listOf(correct) + distractors.take(3)).shuffled()
                        apiQs.add(TriviaQuestion(
                            question = "¿Quién co-creó Futurama junto a Matt Groening?",
                            options  = opts,
                            correct  = opts.indexOf(correct),
                        ))
                    }

                    if (names.size >= 2) {
                        val correct = names[1]   // Matt Groening
                        val distractors = listOf("James L. Brooks", "Al Jean", "Mike Reiss", "John Swartzwelder")
                        val opts = (listOf(correct) + distractors.take(3)).shuffled()
                        apiQs.add(TriviaQuestion(
                            question = "¿Quién creó Los Simpsons Y Futurama?",
                            options  = opts,
                            correct  = opts.indexOf(correct),
                        ))
                    }
                }

                // Pregunta: ciudad de la sinopsis
                // La sinopsis menciona "New New York" — usamos eso
                val synopsis = obj.optString("synopsis", "")
                if (synopsis.contains("New New York")) {
                    val opts = listOf("Nueva Nueva York", "Nueva Los Ángeles", "Neo Chicago", "Nuevo Houston").shuffled()
                    val correct = "Nueva Nueva York"
                    apiQs.add(TriviaQuestion(
                        question = "¿En qué ciudad transcurre Futurama en el año 3000?",
                        options  = opts,
                        correct  = opts.indexOf(correct),
                    ))
                }

                // Pregunta: año de congelación de Fry
                if (synopsis.contains("1999")) {
                    val opts = listOf("1999", "2000", "1995", "1985").shuffled()
                    apiQs.add(TriviaQuestion(
                        question = "¿En qué año fue criocongelado Philip J. Fry?",
                        options  = opts,
                        correct  = opts.indexOf("1999"),
                    ))
                }

                // Pregunta: año en que despierta Fry
                if (synopsis.contains("2999")) {
                    val opts = listOf("2999", "3000", "2500", "3500").shuffled()
                    apiQs.add(TriviaQuestion(
                        question = "¿En qué fecha despierta Fry después de ser criocongelado?",
                        options  = opts,
                        correct  = opts.indexOf("2999"),
                    ))
                }

                break // solo necesitamos id=1
            }

            // Mezclar preguntas de la API con el banco hardcodeado y tomar 10
            (apiQs + FALLBACK_QUESTIONS)
                .distinctBy { it.question }
                .shuffled()
                .take(10)

        } catch (e: Exception) {
            FALLBACK_QUESTIONS.shuffled().take(10)
        }
    }
}
