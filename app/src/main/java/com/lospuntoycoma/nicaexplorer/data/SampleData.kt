package com.lospuntoycoma.nicaexplorer.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Park
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.lospuntoycoma.nicaexplorer.R
import com.lospuntoycoma.nicaexplorer.model.Afluencia
import com.lospuntoycoma.nicaexplorer.model.City
import com.lospuntoycoma.nicaexplorer.model.Monument

object SampleData {

    private val fallbackCities = listOf(
        City(
            id = "juigalpa",
            name = "Juigalpa",
            description = "Corazón de la cultura chontaleña",
            monumentCount = 3,
            gradientStart = 0xFF7B2D8E,
            gradientEnd = 0xFF4A1A5C,
            icon = Icons.Filled.LocationCity,
            imageRes = R.drawable.juigalpa
        ),
        City(
            id = "leon",
            name = "León",
            description = "Ciudad colonial y cuna de la poesía nicaragüense",
            monumentCount = 2,
            gradientStart = 0xFFD4A017,
            gradientEnd = 0xFF8B4513,
            icon = Icons.Filled.LocationCity,
            imageRes = R.drawable.leon
        ),
        City(
            id = "managua",
            name = "Managua",
            description = "Capital llena de historia y modernidad",
            monumentCount = 3,
            gradientStart = 0xFF00897B,
            gradientEnd = 0xFF005B4F,
            icon = Icons.Filled.LocationCity,
            imageRes = R.drawable.managua
        )
    )

    // Firestore es la fuente principal; estos valores locales permanecen como fallback.
    var cities by mutableStateOf(fallbackCities)

    // Estimaciones orientativas del prototipo; no representan datos de afluencia en tiempo real.
    private val fallbackMonumentsByCity = mapOf(
        "juigalpa" to listOf(
            Monument(
                id = "homenaje_madre_juigalpina",
                name = "Homenaje a la Madre Juigalpina",
                city = "Juigalpa",
                cityId = "juigalpa",
                category = "Monumento cultural",
                afluencia = Afluencia.MODERADA,
                description = "Monumento ubicado en el Parque Central de Juigalpa como homenaje a las madres juigalpinas. " +
                        "Representa el reconocimiento a la maternidad, la familia y el papel de la mujer en la identidad social de la ciudad.",
                history = "La Estatua a la Madre forma parte de los elementos culturales del Parque Central de Juigalpa " +
                        "y fue erigida como homenaje a la madre juigalpina.",
                yearBuilt = "30 de mayo de 1993",
                modeloUnity = "HomenajeMadreJuigalpina",
                gradientStart = 0xFF7B2D8E,
                gradientEnd = 0xFFB47CC9,
                icon = Icons.Filled.AccountBalance,
                imageRes = R.drawable.homenajealamadrejuigalpina,
                consejosResponsables = listOf(
                    "Este monumento honra a las madres de la ciudad; mantén un comportamiento respetuoso.",
                    "No trepes ni te sientes sobre el pedestal o la escultura.",
                    "No rayes ni apliques pintura sobre el monumento.",
                    "Deposita flores u ofrendas únicamente en las áreas permitidas."
                )
            ),
            Monument(
                id = "toro_chontaleno",
                name = "Toro Chontaleño",
                city = "Juigalpa",
                cityId = "juigalpa",
                category = "Tradición ganadera",
                afluencia = Afluencia.ALTA,
                description = "Monumento dedicado a la tradición ganadera de Chontales, una de las actividades económicas y culturales más representativas del departamento.",
                history = "Fue inaugurado en Juigalpa en agosto de 2018 como homenaje al trabajo de los productores y ganaderos chontaleños.",
                yearBuilt = "2018",
                modeloUnity = "ToroChontaleno",
                gradientStart = 0xFF4A1A5C,
                gradientEnd = 0xFF7B2D8E,
                icon = Icons.Filled.Landscape,
                imageRes = R.drawable.torochontaleno,
                consejosResponsables = listOf(
                    "Al visitar zonas ganaderas, respeta los cercados y no cruce sin autorización.",
                    "No se acerque a los animales de trabajo sin guía local.",
                    "Los caminos rurales pueden estar en mal estado; use transporte adecuado.",
                    "Apoye a los productores locales comprando productos artesanales de la zona."
                )
            ),
            Monument(
                id = "estatua_museo_juigalpa",
                name = "Estatua del Museo de Juigalpa",
                city = "Juigalpa",
                cityId = "juigalpa",
                category = "Patrimonio arqueológico",
                afluencia = Afluencia.BAJA,
                description = "Escultura inspirada en la estatuaria precolombina conservada en el Museo Arqueológico Gregorio Aguilar Barea, patrimonio arqueológico representativo de Chontales.",
                history = "La colección del museo reúne esculturas monumentales en piedra asociadas a antiguas poblaciones de Chontales, con representaciones humanas, animales y seres simbólicos.",
                yearBuilt = "800–1500 d.C. aprox.",
                modeloUnity = "EstatuaMuseoJuigalpa",
                gradientStart = 0xFF4A1A5C,
                gradientEnd = 0xFFB47CC9,
                icon = Icons.Filled.AccountBalance,
                imageRes = R.drawable.museojuigalpa,
                consejosResponsables = listOf(
                    "En el museo, sigue las indicaciones del personal y la señalización.",
                    "No toques las esculturas originales; el aceite de la piel las daña.",
                    "No uses flash al fotografiar las piezas de la colección.",
                    "Respeta las barreras de protección del patrimonio arqueológico."
                )
            )
        ),
        "managua" to listOf(
            Monument(
                id = "arbol_vida",
                name = "Árbol de la Vida",
                city = "Managua",
                cityId = "managua",
                category = "Monumento urbano",
                afluencia = Afluencia.ALTA,
                description = "Estructura decorativa ampliamente reconocida en Managua.",
                history = "Uno de los elementos urbanos más conocidos de la capital.",
                yearBuilt = "julio de 2013",
                modeloUnity = "ArbolDeLaVida",
                gradientStart = 0xFF00897B,
                gradientEnd = 0xFF4DB6AC,
                icon = Icons.Filled.Park,
                imageRes = R.drawable.arboldelavida,
                consejosResponsables = listOf(
                    "No pinte, marque ni dañe la estructura del monumento.",
                    "Respete las áreas verdes circundantes y no deje residuos.",
                    "Use las zonas designadas para tomar fotografías.",
                    "Mantenga limpio el área al retirarse."
                )
            ),
            Monument(
                id = "campana_de_la_paz",
                name = "Campana de la Paz",
                city = "Managua",
                cityId = "managua",
                category = "Monumento conmemorativo",
                afluencia = Afluencia.BAJA,
                description = "Monumento ubicado en el centro histórico de Managua, sobre la Avenida de Bolívar a Chávez. Su campanario, de aproximadamente 20 metros de altura, fue concebido como un símbolo de paz, entendimiento, reconciliación y armonía para las familias nicaragüenses.",
                history = "La Campana de la Paz fue construida e inaugurada en 2020 en Managua. El monumento forma parte del conjunto urbano del centro histórico y su campana fue instalada como un símbolo dedicado a la paz y la convivencia.",
                yearBuilt = "2020",
                modeloUnity = "CampanaDeLaPaz",
                gradientStart = 0xFF7B2D8E,
                gradientEnd = 0xFF4A1A5C,
                icon = Icons.Filled.Landscape,
                imageRes = R.drawable.campanadelapaz
            ),
            Monument(
                id = "ruben_dario",
                name = "Estatua de Rubén Darío",
                city = "Managua",
                cityId = "managua",
                category = "Monumento histórico",
                afluencia = Afluencia.MODERADA,
                description = "Escultura dedicada al poeta Rubén Darío.",
                history = "Figura representativa de la literatura nicaragüense.",
                yearBuilt = "24 de septiembre de 1933",
                modeloUnity = "EstatuaRubenDario",
                gradientStart = 0xFF2E7D32,
                gradientEnd = 0xFF81C784,
                icon = Icons.Filled.AccountBalance,
                imageRes = R.drawable.estatuarubendario,
                consejosResponsables = listOf(
                    "No trepe ni se siente sobre la base de la escultura.",
                    "No vierta pintura ni sustancias sobre la estatua.",
                    "Respete el mobiliario urbano del parque o plaza donde se ubica.",
                    "Mantenga el área limpia al retirarse."
                )
            )
        ),
        "leon" to listOf(
            Monument(
                id = "tumba_ruben_dario",
                name = "Tumba de Rubén Darío",
                city = "León",
                cityId = "leon",
                category = "Monumento histórico",
                afluencia = Afluencia.ALTA,
                description = "Monumento histórico ubicado en la ciudad de León, dedicado al poeta Rubén Darío.",
                history = "Rubén Darío, máximo exponente de la poesía modernista, descansa en la Catedral de León.",
                yearBuilt = "1916",
                modeloUnity = "TumbaRubenDario",
                gradientStart = 0xFFD4A017,
                gradientEnd = 0xFFFFD700,
                icon = Icons.Filled.Church,
                imageRes = R.drawable.tumbarubendario,
                consejosResponsables = listOf(
                    "Este es un lugar de descanso; mantenga un comportamiento respetuoso.",
                    "Evite ruidos excesivos dentro y alrededor de la catedral.",
                    "Respete los horarios de apertura y cierre del recinto.",
                    "Use vestimenta adecuada al entrar a la catedral.",
                    "No deje ofrendas ni objetos personales sobre la tumba."
                )
            ),
            Monument(
                id = "estatua_san_benito",
                name = "Estatua de San Benito",
                city = "León",
                cityId = "leon",
                category = "Patrimonio religioso",
                afluencia = Afluencia.MODERADA,
                description = "Imagen religiosa de San Benito de Palermo resguardada en el Santuario Diocesano San Francisco de Asís de León. " +
                        "Es una de las expresiones de devoción más representativas de la ciudad y cada Lunes Santo reúne a numerosos fieles y promesantes.",
                history = "La devoción a San Benito de Palermo en León fue promovida por los frailes franciscanos y se convirtió en una de las " +
                        "tradiciones religiosas más importantes de la ciudad. Durante el Lunes Santo, la imagen sale en procesión y los devotos " +
                        "cumplen promesas, entre ellas vestir de blanco, barrer el templo y compartir la tradicional chicha de maíz.",
                yearBuilt = "1919",
                modeloUnity = "SanBenito",
                gradientStart = 0xFF8B6914,
                gradientEnd = 0xFFD4A017,
                icon = Icons.Filled.Church,
                imageRes = R.drawable.sanbenito,
                consejosResponsables = listOf(
                    "Respeta el carácter religioso y espiritual del santuario.",
                    "Evita tocar la imagen y otros elementos patrimoniales.",
                    "Mantén silencio durante misas, oraciones y actividades religiosas.",
                    "No dejes residuos dentro del templo ni durante las procesiones.",
                    "Respeta las tradiciones y a los promesantes que participan en las celebraciones."
                )
            )
        )
    )

    var monumentsByCity by mutableStateOf(fallbackMonumentsByCity)

    val allMonuments: List<Monument>
        get() = monumentsByCity.values.flatten()

    val recommendedMonuments: List<Monument>
        get() = allMonuments.take(5)

    /** Carga el catálogo publicado. Un error o una colección vacía conserva SampleData. */
    suspend fun loadFromFirestore(): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            val citySnapshot = db.collection("ciudades").get().await()
            val placeSnapshot = db.collection("lugares").get().await()
            if (citySnapshot.isEmpty || placeSnapshot.isEmpty) return false

            val localCities = fallbackCities.associateBy { it.id }
            val validCityIds = fallbackCities.map { it.id }.toSet()
            val loadedCities = citySnapshot.documents.filter { it.id in validCityIds }.map { doc ->
                val id = doc.id
                val fallback = localCities[id]
                City(
                    id = id,
                    name = doc.getString("nombre") ?: doc.getString("name") ?: fallback?.name ?: id,
                    description = doc.getString("descripcion") ?: doc.getString("description") ?: fallback?.description.orEmpty(),
                    monumentCount = (doc.getLong("monumentCount") ?: fallback?.monumentCount?.toLong() ?: 0L).toInt(),
                    gradientStart = doc.getLong("gradientStart") ?: fallback?.gradientStart ?: 0xFF4A1A5C,
                    gradientEnd = doc.getLong("gradientEnd") ?: fallback?.gradientEnd ?: 0xFF7B2D8E,
                    icon = fallback?.icon,
                    imageRes = drawableForKey(doc.getString("imagenKey"), fallback?.imageRes),
                    imageKey = doc.getString("imagenKey") ?: fallback?.imageKey
                )
            }
            val localMonuments = fallbackMonumentsByCity.values.flatten().associateBy { it.id }
            val loaded = placeSnapshot.documents.filter { (it.getString("cityId") ?: "") in validCityIds }.mapNotNull { doc ->
                val id = doc.id
                val fallback = localMonuments[id] ?: return@mapNotNull null
                val cityId = doc.getString("cityId") ?: fallback.cityId
                val afluencia = doc.getString("afluencia")?.uppercase()
                    ?.let { value -> runCatching { Afluencia.valueOf(value) }.getOrNull() }
                    ?: fallback.afluencia
                Monument(
                    id = id,
                    name = doc.getString("nombre") ?: doc.getString("name") ?: fallback.name,
                    city = doc.getString("ciudad") ?: doc.getString("city") ?: fallback.city,
                    cityId = cityId,
                    category = doc.getString("categoria") ?: doc.getString("category") ?: fallback.category,
                    afluencia = afluencia,
                    description = doc.getString("descripcion") ?: fallback.description,
                    history = doc.getString("historia") ?: doc.getString("history") ?: fallback.history,
                    yearBuilt = doc.getString("anioConstruccion") ?: doc.getString("yearBuilt") ?: fallback.yearBuilt,
                    modeloUnity = doc.getString("modeloUnity") ?: fallback.modeloUnity,
                    gradientStart = doc.getLong("gradientStart") ?: fallback.gradientStart,
                    gradientEnd = doc.getLong("gradientEnd") ?: fallback.gradientEnd,
                    icon = fallback.icon,
                    imageRes = drawableForKey(doc.getString("imagenKey"), fallback.imageRes),
                    imageKey = doc.getString("imagenKey") ?: fallback.imageKey,
                    consejosResponsables = (doc.get("consejosResponsables") as? List<*>)
                        ?.filterIsInstance<String>() ?: fallback.consejosResponsables
                )
            }
            if (loaded.isEmpty()) return false
            cities = loadedCities.ifEmpty { fallbackCities }
            monumentsByCity = loaded.groupBy { it.cityId }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun drawableForKey(imageKey: String?, fallback: Int?): Int? {
        return when (imageKey?.trim()?.lowercase()) {
            "juigalpa" -> R.drawable.juigalpa
            "leon" -> R.drawable.leon
            "managua" -> R.drawable.managua
            "homenajealamadrejuigalpina" -> R.drawable.homenajealamadrejuigalpina
            "torochontaleno" -> R.drawable.torochontaleno
            "museojuigalpa" -> R.drawable.museojuigalpa
            "arboldelavida" -> R.drawable.arboldelavida
            "campanadelapaz" -> R.drawable.campanadelapaz
            "estatuarubendario" -> R.drawable.estatuarubendario
            "tumbarubendario" -> R.drawable.tumbarubendario
            "sanbenito" -> R.drawable.sanbenito
            else -> fallback
        }
    }
}
