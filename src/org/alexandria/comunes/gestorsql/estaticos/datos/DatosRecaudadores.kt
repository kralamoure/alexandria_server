package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.cliente.Jugador
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.Recaudador
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosRecaudadores(dataSource: HikariDataSource?) :
    AbstractDAO<Recaudador>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(P: Recaudador): Boolean {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `percepteurs` SET `objets` = ?,`kamas` = ?,`xp` = ? WHERE guid = ?")
            p!!.setString(1, P.parseItemCollector())
            p.setLong(2, P.kamas)
            p.setLong(3, P.xp)
            p.setInt(4, P.id)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("PercepteurData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun load() {
        var result: Result? = null
        var nbr = 0
        try {
            result = getData("SELECT * from percepteurs")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val map = Mundo.mundo.getMap(rs.getShort("mapid")) ?: continue
                var perso: Jugador? = null
                val idpropietario = rs.getInt("poseur_id")
                if (idpropietario > 0) {
                    perso = Mundo.mundo.getPlayer(idpropietario)
                }
                val date = rs.getString("date")
                var time: Long = 0
                if (date != null && date != "") {
                    time = date.toLong()
                }
                Mundo.mundo.addCollector(
                    Recaudador(
                        rs.getInt("guid"),
                        rs.getShort("mapid"),
                        rs.getInt("cellid"),
                        rs.getByte("orientation"),
                        rs.getInt("guild_id"),
                        rs.getShort("N1"),
                        rs.getShort("N2"),
                        perso,
                        time,
                        rs.getString("objets"),
                        rs.getLong("kamas"),
                        rs.getLong("xp")
                    )
                )
                nbr++
            }
        } catch (e: SQLException) {
            super.sendError("PercepteurData load", e)
        } finally {
            close(result)
        }
    }

    fun delete(id: Int) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("DELETE FROM percepteurs WHERE guid = ?")
            p!!.setInt(1, id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("PercepteurData delete", e)
        } finally {
            close(p)
        }
    }

    fun add(
        guid: Int, mapid: Int, guildID: Int, poseur_id: Int,
        date: Long, cellid: Int, o: Int, N1: Short, N2: Short
    ) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("INSERT INTO `percepteurs` VALUES (?,?,?,?,?,?,?,?,?,?,?,?)")
            p!!.setInt(1, guid)
            p.setInt(2, mapid)
            p.setInt(3, cellid)
            p.setInt(4, o)
            p.setInt(5, guildID)
            p.setInt(6, poseur_id)
            p.setString(7, date.toString())
            p.setShort(8, N1)
            p.setShort(9, N2)
            p.setString(10, "")
            p.setLong(11, 0)
            p.setLong(12, 0)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("PercepteurData add", e)
        } finally {
            close(p)
        }
    }

    //Pour �viter les conflits avec touts autre NPC
    val id: Int
        get() {
            var result: Result? = null
            var i = -50 //Pour �viter les conflits avec touts autre NPC
            try {
                result = getData("SELECT `guid` FROM `percepteurs` ORDER BY `guid` ASC LIMIT 0 , 1")
                val rs = result!!.resultSet
                while (rs!!.next()) {
                    i = rs.getInt("guid") - 1
                }
            } catch (e: SQLException) {
                super.sendError("PercepteurData getId", e)
            } finally {
                close(result)
            }
            if (i >= -9999) i = -10000
            return i
        }
}