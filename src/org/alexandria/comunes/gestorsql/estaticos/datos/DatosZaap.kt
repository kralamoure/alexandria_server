package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.Constantes
import java.sql.SQLException

class DatosZaap(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        var i = 0
        try {
            result = getData("SELECT mapID, cellID from zaaps")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                Constantes.ZAAPS[rs.getInt("mapID")] = rs.getInt("cellID")
                i++
            }
        } catch (e: SQLException) {
            super.sendError("ZaapData load", e)
        } finally {
            close(result)
        }
    }
}