# Solicitudes de comercios: configuración de Firebase

El proyecto Android guarda las solicitudes en `solicitudes_comercios`, pero este
repositorio no contiene `firestore.rules`, `firebase.json`, `.firebaserc` ni un
directorio `functions/`. Por eso, los siguientes bloques deben fusionarse con la
configuración que ya está desplegada; no reemplaces las reglas completas sin
revisarlas primero.

## Reglas de Firestore

Inserta estos `match` dentro del bloque existente
`match /databases/{database}/documents`. Si existe una regla comodín permisiva,
por ejemplo `allow read, write: if true`, elimínala o restríngela porque las
reglas coincidentes se evalúan como OR.

```rules
match /solicitudes_comercios/{solicitudId} {
  function requiredText(value, maxSize) {
    return value is string
      && value.trim().size() > 0
      && value.size() <= maxSize;
  }

  function validPhone(value) {
    return value is string
      && value.size() >= 7
      && value.size() <= 25
      && value.matches('[0-9+() .-]+')
      && value.replace('[^0-9]', '').size() >= 7
      && value.replace('[^0-9]', '').size() <= 15;
  }

  function validSolicitud() {
    return request.resource.data.keys().hasAll([
      'nombreNegocio', 'ciudad', 'cityId', 'categoria', 'direccion',
      'descripcion', 'telefono', 'whatsapp', 'horario',
      'nombreResponsable', 'correoResponsable', 'redesSociales',
      'estado', 'fechaSolicitud', 'userId'
    ])
    && request.resource.data.keys().hasOnly([
      'nombreNegocio', 'ciudad', 'cityId', 'categoria', 'direccion',
      'descripcion', 'telefono', 'whatsapp', 'horario',
      'nombreResponsable', 'correoResponsable', 'redesSociales',
      'estado', 'fechaSolicitud', 'userId'
    ])
    && requiredText(request.resource.data.nombreNegocio, 120)
    && requiredText(request.resource.data.ciudad, 80)
    && requiredText(request.resource.data.cityId, 80)
    && request.resource.data.cityId.matches('[a-z0-9_]+')
    && requiredText(request.resource.data.categoria, 80)
    && requiredText(request.resource.data.direccion, 240)
    && requiredText(request.resource.data.descripcion, 2000)
    && validPhone(request.resource.data.telefono)
    && validPhone(request.resource.data.whatsapp)
    && requiredText(request.resource.data.horario, 240)
    && requiredText(request.resource.data.nombreResponsable, 120)
    && request.resource.data.correoResponsable is string
    && request.resource.data.correoResponsable.size() <= 254
    && request.resource.data.correoResponsable.matches('[^@\\s]+@[^@\\s]+[.][^@\\s]+')
    && request.resource.data.redesSociales is string
    && request.resource.data.redesSociales.size() <= 500
    && request.resource.data.estado == 'pendiente'
    && request.resource.data.userId is string
    && request.resource.data.userId == request.auth.uid
    && request.resource.data.fechaSolicitud is timestamp
    && request.resource.data.fechaSolicitud == request.time;
  }

  allow create: if request.auth != null && validSolicitud();
  allow read, update, delete: if false;
}

// La aplicación cliente no puede crear ni leer correos.
match /mail/{mailId} {
  allow read, write: if false;
}
```

Las revisiones y cambios de `estado` deben hacerse desde Firebase Console o un
backend con Admin SDK. Si después se agrega administración desde Android, debe
autorizarse mediante custom claims; no se debe abrir `update` a usuarios
normales.

## Notificación al correo administrativo

La opción recomendada es una Cloud Function v2 que escuche la creación de una
solicitud y cree un documento protegido en `mail`. La extensión oficial Trigger
Email escucha esa segunda colección y realiza el envío. El destinatario queda
fijo en código de servidor y el cliente nunca controla direcciones ni contenido
de correo.

Agrega este código a `functions/src/index.ts` después de inicializar un proyecto
de Functions con TypeScript. Si ya existe `initializeApp()`, no lo dupliques.
Ajusta la región para que coincida o quede cerca de la ubicación de Firestore.

```ts
import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { onDocumentCreated } from "firebase-functions/v2/firestore";

initializeApp();
const db = getFirestore();

export const notificarSolicitudComercio = onDocumentCreated(
  {
    document: "solicitudes_comercios/{solicitudId}",
    region: "us-central1",
  },
  async (event) => {
    const solicitud = event.data?.data();
    if (!solicitud) return;

    const solicitudId = event.params.solicitudId;
    const mailRef = db.collection("mail").doc(solicitudId);

    await db.runTransaction(async (tx) => {
      if ((await tx.get(mailRef)).exists) return;

      const subjectName = String(solicitud.nombreNegocio ?? "")
        .replace(/[\r\n]+/g, " ")
        .slice(0, 120);
      const value = (name: string) => String(solicitud[name] ?? "");

      tx.set(mailRef, {
        to: ["oliverkj10@gmail.com"],
        message: {
          subject: `Nueva solicitud de comercio: ${subjectName}`,
          text: [
            `Solicitud: ${solicitudId}`,
            `Negocio: ${value("nombreNegocio")}`,
            `Ciudad: ${value("ciudad")} (${value("cityId")})`,
            `Categoría: ${value("categoria")}`,
            `Dirección: ${value("direccion")}`,
            `Descripción: ${value("descripcion")}`,
            `Teléfono: ${value("telefono")}`,
            `WhatsApp: ${value("whatsapp")}`,
            `Horario: ${value("horario")}`,
            `Responsable: ${value("nombreResponsable")}`,
            `Correo: ${value("correoResponsable")}`,
            `Redes: ${value("redesSociales")}`,
            `Usuario: ${value("userId")}`,
          ].join("\n"),
        },
      });
    });
  },
);
```

El ID del documento de correo coincide con el ID de la solicitud y se crea en
una transacción, evitando correos duplicados si el trigger se reintenta.

## Pasos manuales

1. En Firebase Console, abre Firestore Database > Rules, conserva una copia de
   las reglas actuales, fusiona los dos bloques anteriores y publica.
2. Comprueba que no exista una regla comodín permisiva que también coincida con
   `solicitudes_comercios` o `mail`.
3. Activa el plan Blaze y configura alertas de presupuesto para usar Cloud
   Functions.
4. Instala Firebase CLI o usa Cloud Shell, inicia sesión y ejecuta
   `firebase init functions` para el proyecto `nica-explore`. Usa Node.js 20 o
   22 y TypeScript.
5. Agrega la Function anterior y despliega con
   `firebase deploy --only functions:notificarSolicitudComercio`.
6. En Firebase Console > Extensions, instala `Trigger Email
   (firestore-send-email)` y configura `mail` como colección de correo.
7. Configura un proveedor SMTP transaccional y un remitente/dominio verificado.
   Guarda las credenciales únicamente en la configuración segura de la
   extensión/Secret Manager, nunca en Android.
8. Envía una solicitud de prueba y comprueba que
   `mail/{solicitudId}.delivery.state` llegue a `SUCCESS`.

Documentación oficial:

- https://firebase.google.com/docs/firestore/security/rules-fields
- https://firebase.google.com/docs/reference/rules/rules.firestore.Request
- https://firebase.google.com/docs/functions/firestore-events
- https://firebase.google.com/docs/extensions/official/firestore-send-email
- https://firebase.google.com/docs/extensions/official/firestore-send-email/delivery-status
