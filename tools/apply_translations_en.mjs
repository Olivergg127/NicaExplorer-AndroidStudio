// Aplica las traducciones al inglés (campos *En) en Firestore.
// Uso:  node apply_translations_en.mjs            -> dry-run (no escribe)
//       node apply_translations_en.mjs --apply    -> escribe en Firestore
import { applicationDefault, initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { readFileSync } from "node:fs";

initializeApp({ projectId: "nica-explore", credential: applicationDefault() });
const db = getFirestore();

const apply = process.argv.includes("--apply");
const translations = JSON.parse(readFileSync(new URL("./translations_en.json", import.meta.url), "utf8"));

let totalDocs = 0;
let totalFields = 0;

for (const [col, docs] of Object.entries(translations)) {
  for (const [id, fields] of Object.entries(docs)) {
    totalDocs++;
    totalFields += Object.keys(fields).length;
    if (apply) {
      await db.collection(col).doc(id).set(fields, { merge: true });
    }
  }
}

console.log(
  `${apply ? "APLICADO" : "DRY-RUN"}: ${totalDocs} documentos, ${totalFields} campos en ${Object.keys(translations).length} colecciones.`
);
if (!apply) console.log("Vuelve a ejecutar con --apply para escribir en Firestore.");
