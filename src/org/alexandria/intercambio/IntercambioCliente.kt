package org.alexandria.intercambio

import ch.qos.logback.classic.Logger
import org.apache.mina.core.buffer.IoBuffer
import org.apache.mina.core.future.ConnectFuture
import org.apache.mina.core.service.IoConnector
import org.apache.mina.core.session.IoSession
import org.apache.mina.transport.socket.nio.NioSocketConnector
import org.alexandria.configuracion.Configuracion.exchangeIp
import org.alexandria.configuracion.Configuracion.exchangePort
import org.alexandria.configuracion.Configuracion.isRunning
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class IntercambioCliente private constructor() {
    private var ioSession: IoSession? = null
    private var connectFuture: ConnectFuture? = null
    private var ioConnector: IoConnector? = null

    companion object {
        @kotlin.jvm.JvmField
        var logger = LoggerFactory.getLogger(IntercambioCliente::class.java) as Logger
        @kotlin.jvm.JvmField
        var INSTANCE: IntercambioCliente? = null
        private fun stringtoIObuffer(packet: String): IoBuffer {
            val ioBuffer: IoBuffer = IoBuffer.allocate(30000)
            ioBuffer.put(packet.toByteArray())
            return ioBuffer.flip()
        }

        init {
            INSTANCE = IntercambioCliente()
        }
    }

    fun setIoSession(ioSession: IoSession?) {
        this.ioSession = ioSession
    }

    private fun init() {
        ioConnector = NioSocketConnector()
        (ioConnector as NioSocketConnector).handler = IntercambioHandler()
        (ioConnector as NioSocketConnector).connectTimeoutMillis = 1000
    }

    fun start(): Boolean {
        if (!isRunning) return true
        connectFuture = try {
            ioConnector?.connect(InetSocketAddress(exchangeIp, exchangePort))
        } catch (e: Exception) {
            //logger.error("Can't find login server on address {}:{}: ", Configuracion.INSTANCE.getExchangeIp(), Configuracion.INSTANCE.getExchangePort(),e);
            return false
        }
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        if (!connectFuture?.isConnected!!) {
            logger.error("Can't connect to login server on address {}:{}",
                    exchangeIp, exchangePort)
            return false
        }
        logger.info("Exchange client connected on address : {},{}", exchangeIp, exchangePort)
        return true
    }

    fun stop() {
        if (ioSession != null) ioSession!!.closeNow()
        if (connectFuture != null) connectFuture!!.cancel()
        connectFuture = null
        ioConnector?.dispose()
        logger.info("Exchange client was stopped.")
    }

    fun restart() {
        if (isRunning) {
            stop()
            init()
            while (!INSTANCE!!.start()) {
                try {
                    Thread.sleep(5000)
                } catch (ignored: InterruptedException) {
                }
            }
        }
    }

    fun send(packet: String) {
        if (ioSession != null && !ioSession!!.isClosing && ioSession!!.isConnected) ioSession!!.write(stringtoIObuffer(packet))
    }

    init {
        init()
    }
}