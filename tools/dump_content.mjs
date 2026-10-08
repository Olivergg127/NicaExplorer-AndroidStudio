// Lee el contenido en español de Firestore (solo lectura) para preparar las traducciones.
// Uso: node tools/dump_content.mjs > contenido.json
import { applicationDefault, initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";

initializeApp({ projectId: "nica-explore", credential: applicationDefault() });
const db = getFirestore();

const FIELDS = {
  ciudades: ["nombre", "lema", "descripcion", "historia"],
  lugares: ["nombre", "categoria", "descripcion", "historia"],
  rutas: ["nombre", "descripcion", "duracionEstimada", "notaDuracion", "objetivos"],
  comercios: ["nombre", "categoria", "categoriaPadre", "descripcion", "infoAdicional", "servicios", "productos"],
  categorias_lugares: ["nombre"],
  categorias_comercios: ["nombre"],
};

const out = {};
for (const [col, fields] of Object.entries(FIELDS)) {
  const snap = await db.collection(col).get();
  out[col] = snap.docs.map((d) => {
    const data = d.data();
    const picked = { id: d.id };
    for (const f of fields) {
      if (data[f] !== undefined && data[f] !== null && data[f] !== "") picked[f] = data[f];
    }
    // Marca si ya tiene los campos en inglés.
    const yaEn = Object.keys(data).filter((k) => k.endsWith("En") && data[k]);
    if (yaEn.length) picked._yaEn = yaEn;
    return picked;
  });
}

console.log(JSON.stringify(out, null, 2));
