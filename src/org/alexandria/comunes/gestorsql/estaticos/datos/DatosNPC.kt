package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.Configuracion.NOEL
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosNPC(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        var nbr = 0
        try {
            result = getData("SELECT * from npcs")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val map = Mundo.mundo.getMap(rs.getShort("mapid")) ?: continue
                if (!NOEL && rs.getInt("npcid") == 795) continue
                map.addNpc(rs.getInt("npcid"), rs.getShort("cellid").toInt(), rs.getInt("orientation"))
                nbr++
            }
        } catch (e: SQLException) {
            super.sendError("NpcData load", e)
        } finally {
            close(result)
        }
    }

    fun delete(m: Int, c: Int): Boolean {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("DELETE FROM npcs WHERE mapid = ? AND cellid = ?")
            p!!.setInt(1, m)
            p.setInt(2, c)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("NpcData delete", e)
        } finally {
            close(p)
        }
        return false
    }

    fun addOnMap(m: Int, id: Int, c: Int, o: Int, mo: Boolean): Boolean {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("INSERT INTO `npcs` VALUES (?,?,?,?,?)")
            p!!.setInt(1, m)
            p.setInt(2, id)
            p.setInt(3, c)
            p.setInt(4, o)
            p.setBoolean(5, mo)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("NpcData addOnMap", e)
        } finally {
            close(p)
        }
        return false
    }
}