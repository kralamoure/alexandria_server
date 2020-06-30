package org.alexandria.intercambio

import org.apache.mina.core.buffer.IoBuffer
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.session.IoSession
import java.nio.charset.CharacterCodingException
import java.nio.charset.StandardCharsets

class IntercambioHandler : IoHandlerAdapter() {
    @Throws(Exception::class)
    override fun sessionCreated(arg0: IoSession) {
        IntercambioCliente.INSTANCE!!.setIoSession(arg0)
    }

    @Throws(Exception::class)
    override fun messageReceived(arg0: IoSession, arg1: Any) {
        val packet = ioBufferToString(arg1)
        IntercambioCliente.logger.info(packet)
        IntercambioPaqueteHandler.parser(packet)
    }

    @Throws(Exception::class)
    override fun messageSent(arg0: IoSession, arg1: Any) {
        IntercambioCliente.logger.info(ioBufferToString(arg1))
    }

    @Throws(Exception::class)
    override fun sessionClosed(arg0: IoSession) {
        IntercambioCliente.INSTANCE!!.restart()
    }

    @Throws(Exception::class)
    override fun exceptionCaught(arg0: IoSession, arg1: Throwable) {
        arg1.printStackTrace()
    }

    companion object {
        private fun ioBufferToString(o: Any): String {
            val ioBuffer = IoBuffer.allocate((o as IoBuffer).capacity())
            ioBuffer.put(o)
            ioBuffer.flip()
            try {
                return ioBuffer.getString(StandardCharsets.UTF_8.newDecoder())
            } catch (e: CharacterCodingException) {
                e.printStackTrace()
            }
            return "undefined"
        }
    }
}