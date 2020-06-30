package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.estaticos.juego.JuegoServidor
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosCeldas(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        var nbr = 0
        try {
            result = getData("SELECT * FROM `scripted_cells`")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val mapId = rs.getShort("MapID")
                val cellId = rs.getInt("CellID")
                val map = Mundo.mundo.getMap(mapId) ?: continue
                val cell = map.getCase(cellId) ?: continue

                // Stop sur la case (triggers)
                if (rs.getInt("EventID") == 1) {
                    cell.addOnCellStopAction(
                        rs.getInt("ActionID"),
                        rs.getString("ActionsArgs"),
                        rs.getString("Conditions"),
                        null
                    )
                } else {
                    JuegoServidor.a()
                }
                nbr++
            }
        } catch (e: SQLException) {
            super.sendError("Scripted_cellData load", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }

    fun update(
        mapID1: Int, cellID1: Int, action: Int, event: Int,
        args: String?, cond: String?
    ): Boolean {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("REPLACE INTO `scripted_cells` VALUES (?,?,?,?,?,?)")
            p!!.setInt(1, mapID1)
            p.setInt(2, cellID1)
            p.setInt(3, action)
            p.setInt(4, event)
            p.setString(5, args)
            p.setString(6, cond)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("Scripted_cellData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun delete(mapID: Int, cellID: Int): Boolean {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("DELETE FROM `scripted_cells` WHERE `MapID` = ? AND `CellID` = ?")
            p!!.setInt(1, mapID)
            p.setInt(2, cellID)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("Scripted_cellData delete", e)
        } finally {
            close(p)
        }
        return false
    }
}