package org.alexandria.estaticos.comandos.administracion

import org.alexandria.comunes.GestorSalida
import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.estaticos.cliente.Cuenta
import org.alexandria.estaticos.cliente.Jugador
import org.alexandria.estaticos.juego.JuegoCliente
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.Timer

abstract class UsuarioAdministrador(jugador: Jugador) {

    @JvmField
    val cuenta: Cuenta = jugador.account
    @JvmField
    val jugador: Jugador = jugador
    @JvmField
    val cliente: JuegoCliente = jugador.account.gameClient
    @JvmField
    var isTimerStart: Boolean = false
    @JvmField
    var timer: Timer? = null

    fun createTimer(timer: Int): Timer {
        val action: ActionListener = object : ActionListener {
            var time = timer
            override fun actionPerformed(event: ActionEvent) {
                time -= 1
                if (time == 1) GestorSalida.GAME_SEND_Im_PACKET_TO_ALL("115;$time minute") else GestorSalida.GAME_SEND_Im_PACKET_TO_ALL(
                    "115;$time minutes"
                )
                if (time <= 0) stop("Shutdown by an administrator")
            }
        }
        return Timer(60000, action)
    }

    fun sendMessage(message: String) {
        jugador.send("BAT0$message")
    }

    fun sendErrorMessage(message: String) {
        jugador.send("BAT1$message")
    }

    fun sendSuccessMessage(message: String) {
        jugador.send("BAT2$message")
    }

    abstract fun apply(packet: String?)

}