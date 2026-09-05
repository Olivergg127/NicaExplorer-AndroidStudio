/**
 * Seed idempotente del catálogo local de NicaExplorer.
 *
 * Requisitos fuera del repositorio:
 *   npm install firebase-admin
 *   (o usa ADC: gcloud auth application-default login)
 *   node tools/seed_firestore.mjs
 *
 * Nunca guardes el JSON de la cuenta de servicio en Git.
 */
import { getApps, initializeApp, applicationDefault } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";

initializeApp({ projectId: "nica-explore", credential: applicationDefault() });
const db = getFirestore();

const cities = {
  juigalpa: { nombre: "Juigalpa", descripcion: "Corazón de la cultura chontaleña", monumentCount: 3, imagenKey: "juigalpa" },
  leon: { nombre: "León", descripcion: "Ciudad colonial y cuna de la poesía nicaragüense", monumentCount: 2, imagenKey: "leon" },
  managua: { nombre: "Managua", descripcion: "Capital llena de historia y modernidad", monumentCount: 3, imagenKey: "managua" }
};

const lugares = [
  ["homenaje_madre_juigalpina", { nombre: "Homenaje a la Madre Juigalpina", ciudad: "Juigalpa", cityId: "juigalpa", categoria: "Monumento cultural", afluencia: "MODERADA", descripcion: "Monumento ubicado en el Parque Central de Juigalpa como homenaje a las madres juigalpinas.", historia: "La Estatua a la Madre forma parte de los elementos culturales del Parque Central de Juigalpa.", anioConstruccion: "30 de mayo de 1993", modeloUnity: "HomenajeMadreJuigalpina", imagenKey: "homenajealamadrejuigalpina" }],
  ["toro_chontaleno", { nombre: "Toro Chontaleño", ciudad: "Juigalpa", cityId: "juigalpa", categoria: "Tradición ganadera", afluencia: "ALTA", descripcion: "Monumento dedicado a la tradición ganadera de Chontales.", historia: "Fue inaugurado en Juigalpa en agosto de 2018.", anioConstruccion: "2018", modeloUnity: "ToroChontaleno", imagenKey: "torochontaleno" }],
  ["estatua_museo_juigalpa", { nombre: "Estatua del Museo de Juigalpa", ciudad: "Juigalpa", cityId: "juigalpa", categoria: "Patrimonio arqueológico", afluencia: "BAJA", descripcion: "Escultura inspirada en la estatuaria precolombina del Museo Arqueológico Gregorio Aguilar Barea.", historia: "La colección reúne esculturas monumentales en piedra asociadas a antiguas poblaciones de Chontales.", anioConstruccion: "800–1500 d.C. aprox.", modeloUnity: "EstatuaMuseoJuigalpa", imagenKey: "museojuigalpa" }],
  ["arbol_vida", { nombre: "Árbol de la Vida", ciudad: "Managua", cityId: "managua", categoria: "Monumento urbano", afluencia: "ALTA", descripcion: "Estructura decorativa ampliamente reconocida en Managua.", historia: "Uno de los elementos urbanos más conocidos de la capital.", anioConstruccion: "julio de 2013", modeloUnity: "ArbolDeLaVida", imagenKey: "arboldelavida" }],
  ["campana_de_la_paz", { nombre: "Campana de la Paz", ciudad: "Managua", cityId: "managua", categoria: "Monumento conmemorativo", afluencia: "BAJA", descripcion: "Monumento ubicado en el centro histórico de Managua, sobre la Avenida de Bolívar a Chávez. Su campanario tiene aproximadamente 20 metros.", historia: "Construida e inaugurada en 2020 como símbolo dedicado a la paz y la convivencia.", anioConstruccion: "2020", modeloUnity: "CampanaDeLaPaz", imagenKey: "campanadelapaz" }],
  ["ruben_dario", { nombre: "Estatua de Rubén Darío", ciudad: "Managua", cityId: "managua", categoria: "Monumento histórico", afluencia: "MODERADA", descripcion: "Escultura dedicada al poeta Rubén Darío.", historia: "Figura representativa de la literatura nicaragüense.", anioConstruccion: "24 de septiembre de 1933", modeloUnity: "EstatuaRubenDario", imagenKey: "estatuarubendario" }],
  ["tumba_ruben_dario", { nombre: "Tumba de Rubén Darío", ciudad: "León", cityId: "leon", categoria: "Monumento histórico", afluencia: "ALTA", descripcion: "Monumento histórico ubicado en León, dedicado al poeta Rubén Darío.", historia: "Rubén Darío descansa en la Catedral de León.", anioConstruccion: "1916", modeloUnity: "TumbaRubenDario", imagenKey: "tumbarubendario" }],
  ["estatua_san_benito", { nombre: "Estatua de San Benito", ciudad: "León", cityId: "leon", categoria: "Patrimonio religioso", afluencia: "MODERADA", descripcion: "Imagen religiosa de San Benito de Palermo resguardada en León.", historia: "La devoción a San Benito de Palermo es una tradición religiosa importante de la ciudad.", anioConstruccion: "1919", modeloUnity: "SanBenito", imagenKey: "sanbenito" }]
];

const consejosResponsables = {
  homenaje_madre_juigalpina: ["Mantén un comportamiento respetuoso.", "No trepes ni te sientes sobre el pedestal.", "No rayes ni apliques pintura sobre el monumento."],
  toro_chontaleno: ["Respeta los cercados y no cruces sin autorización.", "No te acerques a animales de trabajo sin guía local.", "Apoya a productores locales."],
  estatua_museo_juigalpa: ["Sigue las indicaciones del personal.", "No toques las esculturas originales.", "Respeta las barreras de protección patrimonial."],
  arbol_vida: ["No dañes la estructura.", "Respeta las áreas verdes y no dejes residuos.", "Mantén limpio el área."],
  ruben_dario: ["No trepes ni te sientes sobre la base.", "Respeta el mobiliario urbano.", "Mantén el área limpia."],
  tumba_ruben_dario: ["Mantén un comportamiento respetuoso.", "Evita ruidos excesivos.", "Respeta los horarios del recinto."],
  estatua_san_benito: ["Respeta el carácter religioso del santuario.", "Mantén silencio durante las actividades religiosas.", "No dejes residuos."],
  campana_de_la_paz: ["No dañes la estructura.", "Respeta las áreas públicas.", "Mantén limpio el entorno."]
};

for (const [id, data] of Object.entries(cities)) {
  await db.collection("ciudades").doc(id).set({ id, ...data }, { merge: true });
}
for (const [id, data] of lugares) {
  await db.collection("lugares").doc(id).set({ id, ...data, consejosResponsables: consejosResponsables[id] ?? [] }, { merge: true });
}
console.log(`Seed completado: ${Object.keys(cities).length} ciudades y ${lugares.length} lugares.`);
