package org.alexandria.estaticos.juego.filtro

import java.time.Instant

internal class IpInstance {

    var connections = 0
        private set
    var lastConnection: Long = 0
        private set
    var isBanned = false
        private set

    fun addConnection() {
        connections++
    }

    fun resetConnections() {
        connections = 0
    }

    fun updateLastConnection() {
        lastConnection = Instant.now().toEpochMilli()
    }

    fun ban() {
        isBanned = true
    }

}