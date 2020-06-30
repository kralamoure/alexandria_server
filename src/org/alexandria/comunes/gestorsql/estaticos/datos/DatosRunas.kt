package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.oficio.magueo.Runas
import java.sql.SQLException

class DatosRunas(dataSource: HikariDataSource?) :
    AbstractDAO<Runas>(dataSource!!) {
    override fun load(obj: Any?) {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM runes")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                Runas(rs.getShort("id"), rs.getFloat("weight"), rs.getByte("bonus"))
            }
        } catch (e: SQLException) {
            super.sendError("RuneData load", e)
        } finally {
            close(result)
        }
    }

    override fun update(obj: Runas): Boolean {
        return false
    }
}