package com.lospuntoycoma.nicaexplorer.data

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.lospuntoycoma.nicaexplorer.model.Comercio
import com.lospuntoycoma.nicaexplorer.model.Monument

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
            1. Tu prioridad son Juigalpa, Managua y León. El catálogo local incluido en cada solicitud es la fuente de verdad para sus monumentos.
            2. COMERCIOS Y RESTAURANTES: Si el usuario pide recomendaciones de comercios, restaurantes u hoteles en Nicaragua, usa tu conocimiento general para dar opciones reales y específicas de estas ciudades. Respeta la cultura local y no inventes datos inexistentes.
            3. Si el usuario pregunta de forma general, prioriza siempre el turismo en Nicaragua.
            4. SOLO responde sobre otros países si el usuario lo pide explícitamente.
            5. Tono amable y orgulloso de Nicaragua.

            REGLAS DE AFLUENCIA ESTIMADA:
            1. Los niveles BAJA, MODERADA y ALTA son estimaciones orientativas del prototipo; no son datos de afluencia en tiempo real.
            2. Llámalos siempre "afluencia estimada". Nunca afirmes "actualmente hay mucha gente", "en este momento está lleno" ni equivalentes.
            3. Para evitar mucha gente o encontrar tranquilidad, recomienda monumentos de la misma ciudad y prioriza BAJA, después MODERADA y por último ALTA.
            4. Si el usuario pregunta por un monumento con nivel ALTA, indícalo como estimación y ofrece una alternativa BAJA o MODERADA de la misma ciudad cuando exista.
            5. Si la pregunta no nombra una ciudad y hay un monumento actual, usa la ciudad de ese monumento.
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
     * @param monument Monumento desde el que se abrió Itzae, si existe.
     * @param monuments Catálogo local usado para comparar afluencia por ciudad.
     */
    suspend fun generateContent(
        prompt: String,
        comercios: List<Comercio> = emptyList(),
        monument: Monument? = null,
        monuments: List<Monument> = emptyList()
    ): String? {
        val contextSections = mutableListOf<String>()

        monument?.let { currentMonument ->
            contextSections += """
                CONTEXTO DEL MONUMENTO ACTUAL:
                - monumentId: ${currentMonument.id}
                - cityId: ${currentMonument.cityId}
                - Nombre: ${currentMonument.name}
                - Ciudad: ${currentMonument.city}
                - Categoría: ${currentMonument.category}
                - Afluencia estimada: ${currentMonument.afluencia.name}
                - Descripción: ${currentMonument.description}
                - Historia: ${currentMonument.history}

                El usuario abrió Itzae desde este monumento. Usa estos datos como referencia
                cuando haga preguntas breves como "Cuéntame más", "¿Dónde está?",
                "¿Por qué es importante?" o mencione "este monumento".
            """.trimIndent()
        }

        if (monuments.isNotEmpty()) {
            val cityCrowdingContext = monuments
                .distinctBy { it.id }
                .groupBy { it.city }
                .entries
                .joinToString("\n\n") { (city, cityMonuments) ->
                    val monumentLines = cityMonuments.joinToString("\n") { cityMonument ->
                        "- ${cityMonument.name} — afluencia ${cityMonument.afluencia.name}"
                    }
                    "Ciudad: $city\nMonumentos:\n$monumentLines"
                }

            contextSections += """
                CONTEXTO LOCAL DE AFLUENCIA ESTIMADA:
                $cityCrowdingContext

                Estos niveles son estimaciones orientativas del prototipo y no datos de afluencia en tiempo real.
                Para recomendar tranquilidad, conserva la ciudad solicitada y prioriza BAJA, luego MODERADA y finalmente ALTA.
            """.trimIndent()
        }

        if (comercios.isNotEmpty()) {
            val contextoComercios = comercios.joinToString("\n") { c ->
                "- ${c.nombre} (${c.categoria}) en ${c.ciudad}. Dirección: ${c.direccion}. Desc: ${c.descripcion}"
            }
            contextSections += "CONTEXTO DE COMERCIOS REALES EN LA BASE DE DATOS:\n$contextoComercios"
        }

        val fullPrompt = if (contextSections.isNotEmpty()) {
            "${contextSections.joinToString("\n\n")}\n\nPREGUNTA DEL USUARIO: $prompt"
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
