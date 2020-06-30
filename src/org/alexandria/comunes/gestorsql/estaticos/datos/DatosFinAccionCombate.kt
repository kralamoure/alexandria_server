package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.otro.Accion
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosFinAccionCombate(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        var nbr = 0
        try {
            result = getData("SELECT * FROM endfight_action")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val map = Mundo.mundo.getMap(rs.getShort("map")) ?: continue
                map.addEndFightAction(
                    rs.getInt("fighttype"),
                    Accion(rs.getInt("action"), rs.getString("args"), rs.getString("cond"), null)
                )
                nbr++
            }
        } catch (e: SQLException) {
            super.sendError("Endfight_actionData load", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }

    fun reload() {
        var result: Result? = null
        var nbr = 0
        try {
            result = getData("SELECT * FROM endfight_action")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val map = Mundo.mundo.getMap(rs.getShort("map")) ?: continue
                map.delAllEndFightAction()
                map.addEndFightAction(
                    rs.getInt("fighttype"),
                    Accion(rs.getInt("action"), rs.getString("args"), rs.getString("cond"), null)
                )
                nbr++
            }
        } catch (e: SQLException) {
            super.sendError("Endfight_actionData reload", e)
        } finally {
            close(result)
        }
    }

    fun add(mapID: Int, type: Int, Aid: Int, args: String?, cond: String?): Boolean {
        if (!delete(mapID, type, Aid)) return false
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("INSERT INTO `endfight_action` VALUES (?,?,?,?,?)")
            p!!.setInt(1, mapID)
            p.setInt(2, type)
            p.setInt(3, Aid)
            p.setString(4, args)
            p.setString(5, cond)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("Endfight_actionData add", e)
        } finally {
            close(p)
        }
        return false
    }

    fun delete(mapID: Int, type: Int, aid: Int): Boolean {
        var p: PreparedStatement? = null
        try {
            p =
                getPreparedStatement("DELETE FROM `endfight_action` WHERE map = ? AND " + "fighttype = ? AND " + "action = ?")
            p!!.setInt(1, mapID)
            p.setInt(2, type)
            p.setInt(3, aid)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("Endfight_actionData delete", e)
        } finally {
            close(p)
        }
        return false
    }
}