package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.Monstruos.Bandidos
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosBandido(dataSource: HikariDataSource?) :
    AbstractDAO<Bandidos>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Bandidos): Boolean {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `bandits` SET `time` = ?")
            p!!.setLong(1, obj.time)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("BanditData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM bandits")
            val rs = result!!.resultSet
            if (rs!!.next()) {
                Bandidos(rs.getString("mobs"), rs.getString("maps"), rs.getLong("time"))
            }
        } catch (e: SQLException) {
            super.sendError("BanditData load", e)
        } finally {
            close(result)
        }
    }
}