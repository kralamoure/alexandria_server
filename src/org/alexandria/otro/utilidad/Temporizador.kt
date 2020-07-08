package org.alexandria.otro.utilidad

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

object Temporizador {
    private var tf: ThreadFactory = DaemonFactory()
    private val executor = Executors.newSingleThreadScheduledExecutor(tf)

    @JvmStatic
    fun addSiguiente(run: Runnable, time: Long, unit: TimeUnit): ScheduledFuture<*> {
        return executor.schedule(catchRunnable(run), time, unit)
    }

    @JvmStatic
    fun addSiguiente(run: Runnable, time: Long): ScheduledFuture<*> {
        return addSiguiente(run, time, TimeUnit.MILLISECONDS)
    }

    @JvmStatic
    fun addSiguiente(run: Runnable, time: Long, unit: TimeUnit, scheduler: DataType?) {
        executor.schedule(run, time, unit)
    }

    @JvmStatic
    fun addSiguiente(run: Runnable, time: Long, scheduler: DataType?) {
        addSiguiente(run, time, TimeUnit.MILLISECONDS, scheduler)
    }

    fun update() {}
    @JvmStatic
    fun catchRunnable(run: Runnable): Runnable {
        return Runnable {
            try {
                run.run()
            } catch (e: Exception) {
                e.printStackTrace()
                System.err.println(e.cause!!.message)
            }
        }
    }

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
}