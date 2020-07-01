package org.alexandria.otro.utilidad

import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

class Temporizador {

        internal class DaemonFactory : ThreadFactory {
            override fun newThread(r: Runnable): Thread {
                val t = Thread(r)
                t.isDaemon = true
                return t
            }
        }

        enum class DataType {
            MAPA, CLIENTE, PELEA
        }

        companion object {
            private val tf: ThreadFactory = DaemonFactory()
            private val ejecutador = Executors.newSingleThreadScheduledExecutor(tf)

        @JvmStatic
        fun addSiguiente(ejecutar: Runnable, tiempo: Long, unit: TimeUnit, scheduler: DataType?) {
            ejecutador.schedule(ejecutar, tiempo, unit)
        }

        @JvmStatic
        fun addSiguiente(ejecutar: Runnable, tiempo: Long, scheduler: DataType?) {
            addSiguiente(ejecutar, tiempo, TimeUnit.MILLISECONDS, scheduler)
        }
    }
}