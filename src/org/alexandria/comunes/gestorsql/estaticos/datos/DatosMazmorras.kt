package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.otro.Dopeul
import org.alexandria.otro.utilidad.Doble
import java.sql.SQLException

class DatosMazmorras(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM donjons")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                Dopeul.getDonjons()[rs.getInt("map")] = Doble(rs.getInt("npc"), rs.getInt("key"))
            }
        } catch (e: SQLException) {
            super.sendError("DonjonData load", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }

    fun get_all_keys(): String {
        var result: Result? = null
        try {
            result = getData("SELECT key FROM donjons")
            val rs = result!!.resultSet
            val keys = StringBuilder()
            while (rs!!.next()) {
                val key = Integer.toHexString(rs.getInt("key"))
                keys.append(if (keys.length == 0) key else ",$key")
            }
            return keys.toString()
        } catch (e: SQLException) {
            super.sendError("DonjonData get_all_keys", e)
        } finally {
            close(result)
        }
        return ""
    }
}