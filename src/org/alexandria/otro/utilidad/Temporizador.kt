package org.alexandria.otro.utilidad

import org.alexandria.estaticos.juego.mundo.Mundo
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object Temporizador {
    private var numerodethreads = 15 + 1
    private var ejecutador = Executors.newScheduledThreadPool(numerodethreads)
    fun update() {
        numerodethreads = Mundo.mundo.numberOfThread + 20
        ejecutador.shutdownNow()
        ejecutador = Executors.newScheduledThreadPool(numerodethreads)
    }

    @JvmStatic
    fun addSiguiente(ejecutar: Runnable?, tiempo: Long, unit: TimeUnit?, scheduler: DataType?) {
        ejecutador.schedule(ejecutar, tiempo, unit)
    }

    @JvmStatic
    fun addSiguiente(ejecutar: Runnable?, tiempo: Long, scheduler: DataType?) {
        addSiguiente(ejecutar, tiempo, TimeUnit.MILLISECONDS, scheduler)
    }

    enum class DataType {
        MAPA, CLIENTE, PELEA
    }
}