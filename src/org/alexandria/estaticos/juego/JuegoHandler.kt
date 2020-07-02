package org.alexandria.estaticos.juego

import lombok.extern.slf4j.Slf4j
import org.alexandria.configuracion.Configuracion.ENCRYPT_PACKET
import org.alexandria.configuracion.Configuracion.mostrarenviados
import org.alexandria.configuracion.Configuracion.mostrarrecibidos
import org.alexandria.intercambio.IntercambioCliente
import org.alexandria.estaticos.juego.filtro.FiltroPaquete
import org.alexandria.estaticos.juego.mundo.Mundo
import org.apache.mina.core.service.IoHandler
import org.apache.mina.core.session.IdleStatus
import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.FilterEvent
import org.apache.mina.filter.codec.RecoverableProtocolDecoderException

@Slf4j
class JuegoHandler : IoHandler {
    @Throws(Exception::class)
    override fun sessionCreated(arg0: IoSession) {
        if (!filter.authorizes(
                arg0.remoteAddress.toString().substring(1).split(":".toRegex()).toTypedArray()[0]
            )) {
            arg0.closeNow()
        } else {
            if (mostrarenviados) {
                IntercambioCliente.logger.info("Session " + arg0.id + " created")
            }
            arg0.attachment = JuegoCliente(arg0)
        }
    }

    @Throws(Exception::class)
    override fun messageReceived(arg0: IoSession, arg1: Any) {
        val client = arg0.attachment as JuegoCliente
        var packet: String? = arg1 as String
        if (ENCRYPT_PACKET && !packet!!.startsWith("AT") && !packet.startsWith("Ak")) {
            packet = Mundo.mundo.cryptManager.decryptMessage(packet, client.preparedKeys)
            packet = packet.replace("\n", "") ?: arg1
        }
        val s = packet!!.split("\n".toRegex()).toTypedArray()
        for (str in s) {
            client.parsePacket(str)
            if (mostrarrecibidos) {
                client.logger.info(" <-- $str")
            }
        }
    }

    @Throws(Exception::class)
    override fun sessionClosed(arg0: IoSession) {
        val client = arg0.attachment as JuegoCliente?
        client?.disconnect()
        if (mostrarenviados) {
            Mundo.mundo.logger.info("Session " + arg0.id + " closed")
        }
    }

    @Throws(Exception::class)
    override fun exceptionCaught(arg0: IoSession, arg1: Throwable) {
        if (arg1 == null) return
        if (arg1.message != null && (arg1 is RecoverableProtocolDecoderException || arg1.message!!.startsWith("Une connexion ") ||
                    arg1.message!!.startsWith("Connection reset by peer") || arg1.message!!.startsWith("Connection timed out"))
        ) return
        arg1.printStackTrace()
        val client = arg0.attachment as JuegoCliente?
        if (mostrarenviados) {
            client?.logger?.info("Exception connexion client : " + arg1.message)
        }
        kick(arg0)
    }

    @Throws(Exception::class)
    override fun messageSent(arg0: IoSession, arg1: Any) {
        val client = arg0.attachment as JuegoCliente
        if (client != null) {
            var packet = arg1 as String
            if (ENCRYPT_PACKET && !packet.startsWith("AT") && !packet.startsWith("HG")) packet =
                Mundo.mundo.cryptManager.decryptMessage(packet, client.preparedKeys).replace("\n", "")
            if (packet.startsWith("am")) return
            if (mostrarenviados) {
                client.logger.info(" --> $packet")
            }
        }
    }

    @Throws(Exception::class)
    override fun inputClosed(ioSession: IoSession) {
        ioSession.closeNow()
    }

    @Throws(Exception::class)
    override fun event(ioSession: IoSession, filterEvent: FilterEvent) {
    }

    @Throws(Exception::class)
    override fun sessionIdle(arg0: IoSession, arg1: IdleStatus) {
        if (mostrarenviados) {
            IntercambioCliente.logger.info("Session " + arg0.id + " idle")
        }
    }

    @Throws(Exception::class)
    override fun sessionOpened(arg0: IoSession) {
        if (mostrarenviados) {
            IntercambioCliente.logger.info("Session " + arg0.id + " opened")
        }
    }

    private fun kick(arg0: IoSession) {
        val client = arg0.attachment as JuegoCliente?
        if (client != null) {
            client.kick()
            arg0.attachment = null
        }
    }

    companion object {
        private val filter = FiltroPaquete().activeSafeMode()
    }
}