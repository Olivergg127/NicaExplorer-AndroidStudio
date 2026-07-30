package com.lospuntoycoma.nicaexplorer.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Park
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.lospuntoycoma.nicaexplorer.model.City
import com.lospuntoycoma.nicaexplorer.model.Monument

object SampleData {

    val cities = listOf(
        City(
            id = "leon",
            name = "León",
            description = "Ciudad colonial y cuna de la poesía nicaragüense",
            monumentCount = 1,
            gradientStart = 0xFFD4A017,
            gradientEnd = 0xFF8B4513,
            icon = Icons.Filled.LocationCity
        ),
        City(
            id = "juigalpa",
            name = "Juigalpa",
            description = "Corazón de la cultura chontaleña",
            monumentCount = 3,
            gradientStart = 0xFF7B2D8E,
            gradientEnd = 0xFF4A1A5C,
            icon = Icons.Filled.LocationCity
        ),
        City(
            id = "managua",
            name = "Managua",
            description = "Capital llena de historia y modernidad",
            monumentCount = 3,
            gradientStart = 0xFF00897B,
            gradientEnd = 0xFF005B4F,
            icon = Icons.Filled.LocationCity
        )
    )

    val monumentsByCity = mapOf(
        "juigalpa" to listOf(
            Monument(
                id = "puma_itzae",
                name = "Puma Itzae",
                city = "Juigalpa",
                cityId = "juigalpa",
                category = "Fauna Silvestre",
                description = "Itzae es una especie rara de puma albina hembra nacida en cautiverio" +
                        " en el Zoologico Thomas Belt en Juigalpa, Nicaragua.",
                history = "Símbolo representativo de la ciudad.",
                yearBuilt = "—",
                modeloUnity = "PumaItzae",
                gradientStart = 0xFF7B2D8E,
                gradientEnd = 0xFFB47CC9,
                icon = Icons.Filled.AccountBalance
            ),
            Monument(
                id = "toro_chontaleño",
                name = "Toro Chontaleño",
                city = "Juigalpa",
                cityId = "juigalpa",
                category = "Tradición ganadera",
                description = "Representa la tradición ganadera del departamento de Chontales.",
                history = "Inspirado en uno de los principales símbolos económicos de la región.",
                yearBuilt = "—",
                modeloUnity = "ToroChontaleno",
                gradientStart = 0xFF4A1A5C,
                gradientEnd = 0xFF7B2D8E,
                icon = Icons.Filled.Landscape
            ),
            Monument(
                id = "cacique_chontal",
                name = "Cacique Chontal",
                city = "Juigalpa",
                cityId = "juigalpa",
                category = "Personaje histórico",
                description = "Representación del legado histórico de los pueblos originarios de Chontales.",
                history = "Homenaje a la cultura indígena del departamento.",
                yearBuilt = "—",
                modeloUnity = "CaciqueChontal",
                gradientStart = 0xFF00897B,
                gradientEnd = 0xFF4DB6AC,
                icon = Icons.Filled.AccountBalance
            )
        ),
        "managua" to listOf(
            Monument(
                id = "arbol_vida",
                name = "Árbol de la Vida",
                city = "Managua",
                cityId = "managua",
                category = "Monumento urbano",
                description = "Estructura decorativa ampliamente reconocida en Managua.",
                history = "Uno de los elementos urbanos más conocidos de la capital.",
                yearBuilt = "—",
                modeloUnity = "ArbolVida",
                gradientStart = 0xFF00897B,
                gradientEnd = 0xFF4DB6AC,
                icon = Icons.Filled.Park
            ),
            Monument(
                id = "huellas_acahualinca",
                name = "Huellas de Acahualinca",
                city = "Managua",
                cityId = "managua",
                category = "Sitio arqueológico",
                description = "Huellas humanas prehistóricas conservadas en Managua.",
                history = "Importante patrimonio arqueológico de Nicaragua.",
                yearBuilt = "—",
                modeloUnity = "HuellasAcahualinca",
                gradientStart = 0xFF7B2D8E,
                gradientEnd = 0xFF4A1A5C,
                icon = Icons.Filled.Landscape
            ),
            Monument(
                id = "ruben_dario",
                name = "Estatua de Rubén Darío",
                city = "Managua",
                cityId = "managua",
                category = "Monumento histórico",
                description = "Escultura dedicada al poeta Rubén Darío.",
                history = "Figura representativa de la literatura nicaragüense.",
                yearBuilt = "—",
                modeloUnity = "RubenDario",
                gradientStart = 0xFF2E7D32,
                gradientEnd = 0xFF81C784,
                icon = Icons.Filled.AccountBalance
            )
        ),
        "leon" to listOf(
            Monument(
                id = "tumba_ruben_dario",
                name = "Tumba de Rubén Darío",
                city = "León",
                cityId = "leon",
                category = "Monumento histórico",
                description = "Monumento histórico ubicado en la ciudad de León, dedicado al poeta Rubén Darío.",
                history = "Rubén Darío, máximo exponente de la poesía modernista, descansa en la Catedral de León.",
                yearBuilt = "1916",
                modeloUnity = "TumbaRubenDario",
                gradientStart = 0xFFD4A017,
                gradientEnd = 0xFFFFD700,
                icon = Icons.Filled.Church
            )
        )
    )

    val allMonuments: List<Monument>
        get() = monumentsByCity.values.flatten()

    val recommendedMonuments: List<Monument>
        get() = allMonuments.take(5)
}
