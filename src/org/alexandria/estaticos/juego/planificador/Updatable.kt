package org.alexandria.estaticos.juego.planificador

import java.time.Instant

abstract class Updatable(wait: Int) : IUpdatable {
    private val wait: Long
    @JvmField
    protected var lastTime = Instant.now().toEpochMilli()
    protected fun verify(): Boolean {
        if (Instant.now().toEpochMilli() - lastTime > wait) {
            lastTime = Instant.now().toEpochMilli()
            return true
        }
        return false
    }

    abstract fun update()

    init {
        this.wait = wait.toLong()
    }
}