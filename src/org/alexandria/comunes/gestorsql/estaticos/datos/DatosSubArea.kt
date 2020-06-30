package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.area.SubArea
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosSubArea(dataSource: HikariDataSource?) :
    AbstractDAO<SubArea>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: SubArea): Boolean {
        var p: PreparedStatement? = null
        try {
            p =
                getPreparedStatement("UPDATE `subarea_data` SET `alignement` = ?, `prisme` = ?, `conquistable` = ? WHERE `id` = ?")
            p!!.setInt(1, obj.alignement)
            p.setInt(2, obj.prismId)
            p.setInt(3, if (obj.conquistable) 0 else 1)
            p.setInt(4, obj.id)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("Subarea_dataData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `subarea_data`;")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                //Ex subareadata dinamicos
                val sa2 = SubArea(rs.getInt("id"), rs.getInt("area"))
                Mundo.mundo.addSubArea(sa2)
                if (sa2.area != null) sa2.area.addSubArea(sa2) //on ajoute la sous zone a la zone
                val id = rs.getInt("id")
                val alignement = rs.getInt("alignement")
                val conquistable = rs.getInt("conquistable")
                val prisme = rs.getInt("Prisme")
                val sa = Mundo.mundo.getSubArea(id)
                if (sa == null) {
                    println("Error null subarea$id")
                    continue
                }
                sa.alignement = alignement
                sa.prismId = prisme
                sa.setConquistable(conquistable)
            }
        } catch (e: SQLException) {
            super.sendError("Subarea_dataData load", e)
        } finally {
            close(result)
        }
    }
}