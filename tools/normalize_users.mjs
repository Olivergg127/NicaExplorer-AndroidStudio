import { applicationDefault, initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { FieldValue, getFirestore, Timestamp } from "firebase-admin/firestore";

initializeApp({ projectId: "nica-explore", credential: applicationDefault() });
const db = getFirestore();
const auth = getAuth();
const apply = process.argv.includes("--apply");

const required = ["uid", "nombre", "correo", "rol", "fechaRegistro"];
const usersSnapshot = await db.collection("usuarios").get();
const authByUid = new Map();
let authPage = await auth.listUsers(1000);
while (true) {
  for (const record of authPage.users) authByUid.set(record.uid, record);
  if (!authPage.pageToken) break;
  authPage = await auth.listUsers(1000, authPage.pageToken);
}

const report = [];
for (const doc of usersSnapshot.docs) {
  const data = doc.data();
  const authUser = authByUid.get(doc.id);
  const missing = required.filter((field) => data[field] === undefined || data[field] === null || data[field] === "");
  const additions = {};
  if (missing.includes("uid")) additions.uid = doc.id;
  if (missing.includes("correo") && authUser?.email) additions.correo = authUser.email;
  if (missing.includes("rol")) additions.rol = "USUARIO";
  if (missing.includes("fechaRegistro") && authUser?.metadata?.creationTime) {
    additions.fechaRegistro = Timestamp.fromDate(new Date(authUser.metadata.creationTime));
  }
  report.push({
    uid: doc.id,
    nombre: data.nombre ?? null,
    missing,
    authFound: Boolean(authUser),
    authCorreo: authUser?.email ?? null,
    additions: Object.fromEntries(Object.entries(additions).map(([k, v]) => [k, v instanceof Timestamp ? v.toDate().toISOString() : v]))
  });
  if (apply && Object.keys(additions).length > 0) {
    await db.collection("usuarios").doc(doc.id).set(additions, { merge: true });
  }
}

const unresolved = report.filter((row) => row.missing.some((field) => !(field in row.additions)));
console.log(JSON.stringify({ mode: apply ? "apply" : "dry-run", total: report.length, incomplete: report.filter((r) => r.missing.length > 0), complete: report.filter((r) => r.missing.length === 0), unresolved }, null, 2));

if (apply) {
  const after = await db.collection("usuarios").get();
  const stillMissing = after.docs.flatMap((doc) => required.filter((field) => doc.data()[field] === undefined || doc.data()[field] === null || doc.data()[field] === "").map((field) => `${doc.id}.${field}`));
  console.log(JSON.stringify({ normalizedTotal: after.size, stillMissing }, null, 2));
}
