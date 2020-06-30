package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.area.Area
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosArea(dataSource: HikariDataSource?) :
    AbstractDAO<Area>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Area): Boolean {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `area_data` SET `alignement` = ?, `Prisme` = ? WHERE id = ?")
            p!!.setInt(1, obj.alignement)
            p.setInt(2, obj.prismId)
            p.setInt(3, obj.id)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("Area_dataData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * from area_data")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                //Ex area data dinamicos
                val a2 = Area(rs.getInt("id"), rs.getInt("superarea"))
                Mundo.mundo.addArea(a2)
                val id = rs.getInt("id")
                val alignement = rs.getInt("alignement")
                val prisme = rs.getInt("Prisme")
                val a = Mundo.mundo.getArea(id)
                if (a != null) {
                    a.alignement = alignement
                    a.prismId = prisme
                }
            }
        } catch (e: SQLException) {
            super.sendError("Area_dataData load", e)
        } finally {
            close(result)
        }
    }
}