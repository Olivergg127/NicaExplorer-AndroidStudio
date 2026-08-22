package com.lospuntoycoma.nicaexplorer.data

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.lospuntoycoma.nicaexplorer.model.Comercio

/**
 * Repositorio para interactuar con Gemini a través de Firebase AI Logic.
 */
object GeminiRepository {

    // Instrucciones de sistema para definir la personalidad y conocimiento del asistente
    private val systemInstructions = content {
        text("""
            Eres Itzae, el Asistente de NicaExplorer.
            
            REGLAS CRÍTICAS DE ESTILO:
            1. Sé ultra-concreto y directo. 
            2. NO TE PRESENTES NI SALUDES en cada mensaje. No digas "Hola, soy Itzae" ni frases similares. Ve directo a la respuesta.
            3. NO USES ASTERISCOS ni formato negrita (markdown). Escribe exclusivamente en texto plano.
            
            PRIORIDAD DE CONOCIMIENTO:
            1. Tu prioridad son Juigalpa, Managua y León. Monumentos clave:
               - Juigalpa: Puma Itzae, Toro Chontaleño, Cacique Chontal.
               - Managua: Árbol de la Vida, Huellas de Acahualinca, Estatua de Rubén Darío.
               - León: Tumba de Rubén Darío.
            2. COMERCIOS Y RESTAURANTES: Si el usuario pide recomendaciones de comercios, restaurantes u hoteles en Nicaragua, usa tu conocimiento general para dar opciones reales y específicas de estas ciudades. Respeta la cultura local y no inventes datos inexistentes.
            3. Si el usuario pregunta de forma general, prioriza siempre el turismo en Nicaragua.
            4. SOLO responde sobre otros países si el usuario lo pide explícitamente.
            5. Tono amable y orgulloso de Nicaragua.
        """.trimIndent())
    }

    // Inicialización del modelo con instrucciones de sistema
    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(
            modelName = "gemini-3.5-flash-lite",
            systemInstruction = systemInstructions
        )

    /**
     * Genera una respuesta basada en un mensaje de texto.
     * @param prompt El mensaje del usuario.
     * @param comercios Lista de comercios obtenidos de Firestore para dar contexto.
     */
    suspend fun generateContent(prompt: String, comercios: List<Comercio> = emptyList()): String? {
        val fullPrompt = if (comercios.isNotEmpty()) {
            val contextoComercios = comercios.joinToString("\n") { c ->
                "- ${c.nombre} (${c.categoria}) en ${c.ciudad}. Dirección: ${c.direccion}. Desc: ${c.descripcion}"
            }
            "CONTEXTO DE COMERCIOS REALES EN LA BASE DE DATOS:\n$contextoComercios\n\nPREGUNTA DEL USUARIO: $prompt"
        } else {
            prompt
        }

        return try {
            val response = model.generateContent(fullPrompt)
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            "Error [v2] (${e.javaClass.simpleName}): ${e.localizedMessage}"
        }
    }
}
