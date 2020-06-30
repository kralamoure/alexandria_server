package org.alexandria.estaticos.juego

import org.alexandria.estaticos.cliente.Cuenta
import org.alexandria.configuracion.Configuracion.gamePort
import org.alexandria.configuracion.Configuracion.ip
import org.alexandria.configuracion.Configuracion.mostrarenviados
import org.alexandria.intercambio.IntercambioCliente
import org.alexandria.estaticos.juego.mundo.Mundo
import org.apache.mina.core.service.IoAcceptor
import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.filter.codec.textline.LineDelimiter
import org.apache.mina.filter.codec.textline.TextLineCodecFactory
import org.apache.mina.transport.socket.nio.NioSocketAcceptor
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.stream.Collectors

class JuegoServidor private constructor() {
    private val acceptor: IoAcceptor
    fun start(): Boolean {
        if (acceptor.isActive) {
            log.warn("Error already start but try to launch again")
            return false
        }
        return try {
            acceptor.bind(InetSocketAddress(gamePort))
            log.info(
                "Game server started on address : {}:{}",
                ip,
                gamePort
            )
            true
        } catch (e: IOException) {
            log.error("Error while starting game server", e)
            try {
                Thread.sleep(10000L)
            } catch (interruptedException: InterruptedException) {
                interruptedException.printStackTrace()
            }
            start()
        }
    }

    fun stop() {
        //if (!acceptor.isActive()) {
        acceptor.managedSessions.values.stream()
            .filter { session: IoSession -> session.isConnected || !session.isClosing }
            .forEach { obj: IoSession -> obj.closeNow() }
        acceptor.dispose()
        acceptor.unbind()
        //}
        log.error("The game server was stopped.")
    }

    fun setState(state: Int) {
        IntercambioCliente.INSTANCE!!.send("SS$state")
    }

    fun kickAll(kickGm: Boolean) {
        for (player in Mundo.mundo.onlinePlayers) {
            if (player != null && player.gameClient != null) {
                if (player.groupe != null && !player.groupe.isJugador && kickGm) continue
                player.send("M04")
                player.gameClient.kick()
            }
        }
    }

    companion object {
        @JvmField
        var INSTANCE = JuegoServidor()
        private val waitingClients = ArrayList<Cuenta>()
        private val log = LoggerFactory.getLogger(JuegoServidor::class.java)
        @JvmStatic
        val clients: List<JuegoCliente>
            get() = INSTANCE.acceptor.managedSessions.values.stream()
                .filter { session: IoSession -> session.attachment != null }
                .map { session: IoSession -> session.attachment as JuegoCliente }
                .collect(Collectors.toList())

        //Correccion numero verdaderos online
        @JvmStatic
        val playersNumberByIp: Int
            get() {
                val ips = ArrayList<String>()
                for (player in Mundo.mundo.onlinePlayers) if (player.gameClient != null) {
                    if (!ips.contains(player.gameClient.account.currentIp)) ips.add(
                        player.gameClient.account.currentIp
                    )
                }
                return ips.size
            }

        @JvmStatic
        fun getAndDeleteWaitingAccount(id: Int): Cuenta? {
            val it: MutableIterator<Cuenta> = waitingClients.listIterator()
            while (it.hasNext()) {
                val account = it.next()
                if (account.id == id) {
                    it.remove()
                    return account
                }
            }
            return null
        }

        @JvmStatic
        fun addWaitingAccount(account: Cuenta) {
            if (!waitingClients.contains(account)) waitingClients.add(
                account
            )
        }

        @JvmStatic
        fun a() {
            if (mostrarenviados) {
                log.warn("Unexpected behaviour detected")
            }
        }
    }

    init {
        acceptor = NioSocketAcceptor()
        acceptor.filterChain.addLast(
            "codec",
            ProtocolCodecFilter(
                TextLineCodecFactory(
                    StandardCharsets.UTF_8,
                    LineDelimiter.NUL,
                    LineDelimiter("\n\u0000")
                )
            )
        )
        acceptor.handler = JuegoHandler()
    }
}