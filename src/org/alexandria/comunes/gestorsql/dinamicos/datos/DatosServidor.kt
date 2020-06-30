package org.alexandria.comunes.gestorsql.dinamicos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.dinamicos.AbstractDAO
import org.alexandria.configuracion.Configuracion.SERVER_ID
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosServidor(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any): Boolean {
        return false
    }

    fun updateTime(time: Long) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE servers SET `uptime` = ? WHERE `id` = ?")
            p!!.setLong(1, time)
            p.setInt(2, SERVER_ID)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("ServerData updateTime", e)
        } finally {
            close(p)
        }
    }

    fun loggedZero() {
        var p: PreparedStatement? = null
        try {
            p =
                getPreparedStatement("UPDATE players SET `logged` = 0 WHERE `server` = '$SERVER_ID'")
            execute(p!!)
        } catch (e: SQLException) {
            super.sendError("ServerData loggedZero", e)
        } finally {
            close(p)
        }
    }
}