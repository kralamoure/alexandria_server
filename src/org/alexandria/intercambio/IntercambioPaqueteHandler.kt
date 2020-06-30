package org.alexandria.intercambio

import org.alexandria.comunes.gestorsql.Database
import org.alexandria.configuracion.Configuracion.SERVER_ID
import org.alexandria.configuracion.Configuracion.SERVER_KEY
import org.alexandria.configuracion.Configuracion.gamePort
import org.alexandria.configuracion.Configuracion.ip
import org.alexandria.configuracion.Configuracion.maxonline
import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.estaticos.cliente.Jugador
import org.alexandria.estaticos.comandos.ComandosJugadores
import org.alexandria.estaticos.juego.JuegoServidor
import org.alexandria.estaticos.juego.JuegoServidor.Companion.addWaitingAccount
import org.alexandria.estaticos.juego.JuegoServidor.Companion.clients
import org.alexandria.estaticos.juego.mundo.Mundo
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

object IntercambioPaqueteHandler {
    fun parser(recv: String) {
        for (packet in recv.split("#".toRegex()).toTypedArray()) {
            if (packet.isEmpty()) continue
            try {
                when (packet[0]) {
                    'F' -> if (packet[1] == '?') { //Requerido
                        val i = maxonline - Mundo.mundo.onlinePlayers.size
                        IntercambioCliente.INSTANCE!!.send("F$i")
                    }
                    'S' -> when (packet[1]) {
                        'H' -> if (packet[2] == 'K') { //Aceptada
                            IntercambioCliente.logger.info("El multi ha validado la conexion.")
                            JuegoServidor.INSTANCE.setState(1)
                        }
                        'K' -> when (packet[2]) {
                            '?' -> {
                                val i = 50000 - clients.size
                                IntercambioCliente.INSTANCE!!.send("SK$SERVER_ID;$SERVER_KEY;$i")
                            }
                            'K' -> {
                                IntercambioCliente.logger.info("El multi ha aceptado la conexion.")
                                IntercambioCliente.INSTANCE!!.send("SH$ip;$gamePort")
                            }
                            'R' -> {
                                IntercambioCliente.logger.info("El multi ha rechazado la conexion.")
                                stop("Conexion rechazada por el multi")
                            }
                        }
                    }
                    'W' -> when (packet[1]) {
                        'A' -> {
                            val id = packet.substring(2).toInt()
                            var account = Mundo.mundo.getAccount(id)
                            if (account == null) {
                                Database.dinamicos.accountData!!.load(id)
                                account = Mundo.mundo.getAccount(id)
                            }
                            if (account != null) {
                                if (account.currentPlayer != null) account.gameClient.kick()
                                account.setSubscribe()
                                addWaitingAccount(account)
                            }
                        }
                        'K' -> {
                            val id = packet.substring(2).toInt()
                            Database.dinamicos.playerData!!.updateAllLogged(id, 0)
                            Database.dinamicos.accountData!!.setLogged(id, 0)
                            val account = Mundo.mundo.getAccount(id)
                            if (account != null) if (account.gameClient != null) account.gameClient.kick()
                        }
                    }
                    'D' -> if (packet[1] == 'M') { // Message
                        val split =
                            packet.substring(2).split("\\|".toRegex()).toTypedArray()
                        if (split.size > 1) {
                            val prefix = "<font color='#C35617'>[" + SimpleDateFormat("HH:mm").format(
                                Date(
                                    Instant.now().toEpochMilli()
                                )
                            ) + "] (" + ComandosJugadores.canal + ") (" + split[1] + ") <b>" + split[0] + "</b>"
                            val message = "Im116;" + prefix + "~" + split[2] + "</font>"
                            Mundo.mundo.onlinePlayers.stream()
                                .filter { p: Jugador? -> p != null && !p.noall }
                                .forEach { p: Jugador ->
                                    p.send(
                                        message.replace(
                                            "%20",
                                            " "
                                        )
                                    )
                                }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}