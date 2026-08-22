package com.lospuntoycoma.nicaexplorer.data

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content

/**
 * Repositorio para interactuar con Gemini a través de Firebase AI Logic.
 */
object GeminiRepository {

    // Instrucciones de sistema para definir la personalidad y conocimiento del asistente
    private val systemInstructions = content {
        text("""
            Eres Itzae, el Asistente Oficial de NicaExplorer, una aplicación dedicada a promover el turismo en Nicaragua.
            
            PRIORIDAD ABSOLUTA:
            1. Siempre debes recomendar lugares y cultura de Nicaragua primero.
            2. Tu conocimiento principal se basa en estas ciudades y monumentos:
               - Juigalpa: Puma Itzae (puma albina única), Toro Chontaleño (tradición ganadera), Cacique Chontal.
               - Managua: Árbol de la Vida, Huellas de Acahualinca (arqueología prehistórica), Estatua de Rubén Darío.
                - León: Tumba de Rubén Darío (Monumento histórico en la Catedral de León).
            3. Si el usuario pregunta de forma general "¿Qué visitar?", responde con estas opciones de Nicaragua.
            4. SOLO si el usuario especifica explícitamente que quiere información de otros países o temas generales, puedes responder de forma global.
            5. Mantén un tono amable, servicial y orgulloso de la cultura nicaragüense.
        """.trimIndent())
    }

    // Inicialización del modelo con instrucciones de sistema
    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(
            modelName = "gemini-3.6-flash",
            systemInstruction = systemInstructions
        )

    /**
     * Genera una respuesta basada en un mensaje de texto.
     */
    suspend fun generateContent(prompt: String): String? {
        return try {
            val response = model.generateContent(prompt)
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            "Error [v2] (${e.javaClass.simpleName}): ${e.localizedMessage}"
        }
    }
}
