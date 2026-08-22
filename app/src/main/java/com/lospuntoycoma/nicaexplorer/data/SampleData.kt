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
            monumentCount = 2,
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
                icon = Icons.Filled.AccountBalance,
                consejosResponsables = listOf(
                    "No alimente a los animales del zoológico.",
                    "Evite usar flash al tomar fotografías para no estresar a la fauna.",
                    "Respete las barreras y señalizaciones de seguridad.",
                    "Mantenga la distancia recomendada de los recintos animales.",
                    "No haga ruidos fuertes ni moleste a los animales."
                )
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
                icon = Icons.Filled.Landscape,
                consejosResponsables = listOf(
                    "Al visitar zonas ganaderas, respete los cercados y no cruce sin autorización.",
                    "No se acerque a los animales de trabajo sin guía local.",
                    "Los caminos rurales pueden estar en mal estado; use transporte adecuado.",
                    "Apoye a los productores locales comprando productos artesanales de la zona."
                )
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
                icon = Icons.Filled.AccountBalance,
                consejosResponsables = listOf(
                    "Este monumento representa una cultura viva; actúe con respeto.",
                    "No suba, trepe ni se siente sobre la escultura.",
                    "No graffiti ni marque con pintura la estructura.",
                    "Infórmese sobre la historia Chontal antes de visitar para apreciar su valor."
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
                description = "Estructura decorativa ampliamente reconocida en Managua.",
                history = "Uno de los elementos urbanos más conocidos de la capital.",
                yearBuilt = "—",
                modeloUnity = "ArbolVida",
                gradientStart = 0xFF00897B,
                gradientEnd = 0xFF4DB6AC,
                icon = Icons.Filled.Park,
                consejosResponsables = listOf(
                    "No pinte, marque ni dañe la estructura del monumento.",
                    "Respete las áreas verdes circundantes y no deje residuos.",
                    "Use las zonas designadas para tomar fotografías.",
                    "Mantenga limpio el área al retirarse."
                )
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
                icon = Icons.Filled.Landscape,
                consejosResponsables = listOf(
                    "No toque las huellas fósiles; son irremplazables.",
                    "Manténgase dentro de los senderos y pasarelas habilitados.",
                    "No recoja ni mueva rocas o fragmentos del sitio.",
                    "Siga las indicaciones de los guías y la señalización del museo.",
                    "Evite pisar fuera de las zonas autorizadas para preservar el yacimiento."
                )
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
                icon = Icons.Filled.AccountBalance,
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
                description = "Monumento histórico ubicado en la ciudad de León, dedicado al poeta Rubén Darío.",
                history = "Rubén Darío, máximo exponente de la poesía modernista, descansa en la Catedral de León.",
                yearBuilt = "1916",
                modeloUnity = "TumbaRubenDario",
                gradientStart = 0xFFD4A017,
                gradientEnd = 0xFFFFD700,
                icon = Icons.Filled.Church,
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

    val allMonuments: List<Monument>
        get() = monumentsByCity.values.flatten()

    val recommendedMonuments: List<Monument>
        get() = allMonuments.take(5)
}
