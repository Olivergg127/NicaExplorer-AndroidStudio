import { applicationDefault, initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
initializeApp({ projectId: "nica-explore", credential: applicationDefault() });
const db = getFirestore();
const [cities, places, legacyPlaces] = await Promise.all([
  db.collection("ciudades").get(),
  db.collection("lugares").get(),
  db.collection("Lugares").get()
]);
const required = ["nombre","ciudad","cityId","categoria","descripcion","historia","anioConstruccion","afluencia","imagenKey","modeloUnity","consejosResponsables"];
const missing = places.docs.flatMap(doc => required.filter(field => doc.get(field) === undefined).map(field => `${doc.id}.${field}`));
console.log(JSON.stringify({
  ciudadesCount: cities.size,
  ciudadesIds: cities.docs.map(d => d.id).sort(),
  lugaresCount: places.size,
  lugaresIds: places.docs.map(d => d.id).sort(),
  missingRequiredFields: missing,
  duplicateDocumentIds: false
  ,legacyLugaresCount: legacyPlaces.size
}, null, 2));
