package org.alexandria.estaticos.juego.filtro

import java.time.Instant
import java.util.*

class FiltroPaquete {
    private val maxConnections = 16
    private val restrictedTime = 1000
    private val ipInstances: MutableMap<String, IpInstance> =
        HashMap()
    private var safe = false

    @Synchronized
    private fun safeCheck(ip: String): Boolean {
        return unSafeCheck(ip)
    }

    private fun unSafeCheck(ip: String): Boolean {
        val ipInstance = find(ip)
        return if (ipInstance.isBanned) {
            false
        } else {
            ipInstance.addConnection()
            if (ipInstance.lastConnection + restrictedTime >= Instant.now().toEpochMilli()) {
                return if (ipInstance.connections < maxConnections) true else {
                    ipInstance.ban()
                    false
                }
            } else {
                ipInstance.updateLastConnection()
                ipInstance.resetConnections()
            }
            true
        }
    }

    fun authorizes(ip: String): Boolean {
        return if (safe) safeCheck(ip) else unSafeCheck(ip)
    }

    fun activeSafeMode(): FiltroPaquete {
        safe = true
        return this
    }

    private fun find(ip: String): IpInstance {
        var ip = ip
        ip = clearIp(ip)
        var result = ipInstances[ip]
        if (result != null) return result
        result = IpInstance()
        ipInstances[ip] = result
        return result
    }

    private fun clearIp(ip: String): String {
        return if (ip.contains(":")) ip.split(":".toRegex()).toTypedArray()[0] else ip
    }

}