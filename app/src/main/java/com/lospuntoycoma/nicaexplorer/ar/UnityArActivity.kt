package com.lospuntoycoma.nicaexplorer.ar

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.window.OnBackInvokedDispatcher
import com.unity3d.player.UnityPlayer
import com.unity3d.player.UnityPlayerGameActivity

/**
 * Actividad que aloja la experiencia Unity/AR.
 *
 * Ciclo de vida "vivir para volver a entrar":
 * - Vive en su propia tarea (taskAffinity propia + singleTask). Al pulsar "Salir"
 *   (o el Back del sistema), se envía la tarea AR al fondo con `moveTaskToBack(true)`
 *   y queda visible la tarea de MainActivity (catálogo), cuyo estado Compose/NavHost
 *   nunca se destruye porque la reentrada a AR ya no toca su pila.
 * - Al volver a entrar desde el catálogo, `startActivity()` con NEW_TASK trae al
 *   frente la tarea AR (singleTask) y dispara `onNewIntent()`. La clase base ya hace
 *   `setIntent(intent)` y `mUnityPlayer.newIntent(intent)`. El reinicio de ARCore
 *   (`ReiniciarExperiencia`) se envía desde `onResume()`, cuando Unity ya se reanudó,
 *   para que la cámara se inicialice correctamente.
 * - `notificarUnityListo()` (llamado desde C# cuando Unity terminó de arrancar)
 *   confirma que el motor está listo; un reinicio solicitado antes de tiempo se
 *   reenvía en cuanto llega esa señal.
 */
class UnityArActivity : UnityPlayerGameActivity() {

    private var unityListo = false
    private var reinicioPendiente = false
    private var reanudado = false
    private var salirEnProceso = false
    private var ultimoIntentReiniciado: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val priority = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                OnBackInvokedDispatcher.PRIORITY_OVERLAY
            } else {
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            }
            // Gestos/teclas de Back: nunca finalizar la actividad (lo rompería el
            // teardown de Unity), siempre enviar la tarea AR al fondo como "Salir".
            onBackInvokedDispatcher.registerOnBackInvokedCallback(priority) {
                salirExperiencia()
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            salirExperiencia()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        salirExperiencia()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Evita reenviar un reinicio si el mismo Intent se entrega dos veces.
        if (intent === ultimoIntentReiniciado) {
            return
        }
        ultimoIntentReiniciado = intent
        salirEnProceso = false

        if (reanudado) {
            // La actividad ya está en primer plano: Unity está reanudado, se envía ya.
            enviarReinicio()
        } else {
            // La actividad está en camino de reanudarse: se envía en onResume.
            reinicioPendiente = true
            Log.d(TAG, "reinicio solicitado; se enviará en onResume")
        }
    }

    override fun onResume() {
        super.onResume() // La clase base llama a mUnityPlayer.onResume()
        reanudado = true
        if (reinicioPendiente) {
            enviarReinicio()
        }
    }

    override fun onPause() {
        reanudado = false
        super.onPause() // La clase base llama a mUnityPlayer.onPause()
    }

    /**
     * Pide a C# que reinicie la experiencia AR. Si Unity aún no está listo,
     * deja el reinicio pendiente para cuando llegue [notificarUnityListo].
     */
    private fun enviarReinicio() {
        if (unityListo) {
            reinicioPendiente = false
            UnityPlayer.UnitySendMessage("ARManager", "ReiniciarExperiencia", "")
            Log.d(TAG, "reinicio enviado a Unity")
        } else {
            reinicioPendiente = true
            Log.d(TAG, "Unity aún no listo; reinicio pendiente")
        }
    }

    /**
     * Llamado desde C# al final de ARManager.Start() para confirmar que el motor
     * está inicializado. Si hay un reinicio pendiente, lo reenvía.
     */
    fun notificarUnityListo() {
        unityListo = true
        if (reinicioPendiente) {
            reinicioPendiente = false
            UnityPlayer.UnitySendMessage("ARManager", "ReiniciarExperiencia", "")
            Log.d(TAG, "Unity listo; reinicio pendiente enviado")
        }
    }

    /**
     * Llamado desde C# (botón Salir) para devolver la app al catálogo sin destruir
     * la actividad ni el motor: la tarea AR se envía al fondo y queda visible la
     * tarea de MainActivity (catálogo) con su estado intacto.
     */
    fun moverAlFondo() {
        runOnUiThread {
            moveTaskToBack(true)
        }
    }

    /**
     * Salida desde el Back del sistema (gesto o tecla). Reproduce el mismo camino
     * que el botón "Salir": se pide a C# que detenga ARCore y llame a [moverAlFondo].
     */
    private fun salirExperiencia() {
        if (salirEnProceso) {
            return
        }
        salirEnProceso = true
        if (unityListo) {
            UnityPlayer.UnitySendMessage("ARManager", "SalirExperiencia", "")
        } else {
            moverAlFondo()
        }
    }

    override fun onUnityPlayerUnloaded() {
        Log.d(TAG, "onUnityPlayerUnloaded: se devuelve el control a MainActivity")
    }

    override fun onUnityPlayerQuitted() {
        Log.d(TAG, "onUnityPlayerQuitted: no esperado con el ciclo de vida actual")
    }

    companion object {
        private const val TAG = "UnityArActivity"
    }
}
