package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.area.mapa.Mapa.ObjetosInteractivos.InteractiveObjectTemplate
import org.alexandria.estaticos.area.mapa.Mapa.PuertasInteractivas
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import java.sql.SQLException

class DatosPuertasInteractivas(dataSource: HikariDataSource?) :
    AbstractDAO<InteractiveObjectTemplate>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: InteractiveObjectTemplate): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * from interactive_doors")
            val rs = result!!.resultSet
            while (rs!!.next()) PuertasInteractivas(
                rs.getString("maps"),
                rs.getString("doorsEnable"),
                rs.getString("doorsDisable"),
                rs.getString("cellsEnable"),
                rs.getString("cellsDisable"),
                rs.getString("requiredCells"),
                rs.getString("button"),
                rs.getShort("time")
            )
        } catch (e: SQLException) {
            super.sendError("interactive_doors load", e)
        } finally {
            close(result)
        }
    }
}