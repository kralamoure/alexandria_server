package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.cliente.Jugador
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosMiembrosGremio(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `guild_members`")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                try {
                    val g = Mundo.mundo.getGuild(rs.getInt("guild"))
                    g?.addMember(
                        rs.getInt("guid"),
                        rs.getInt("rank"),
                        rs.getByte("pxp"),
                        rs.getLong("xpdone"),
                        rs.getInt("rights")
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: SQLException) {
            super.sendError("Guild_memberData load", e)
        } finally {
            close(result)
        }
    }

    fun delete(id: Int) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("DELETE FROM `guild_members` WHERE `guid` = ?")
            p!!.setInt(1, id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("Guild_memberData delete", e)
        } finally {
            close(p)
        }
    }

    fun deleteAll(id: Int) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("DELETE FROM `guild_members` WHERE `guild` = ?")
            p!!.setInt(1, id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("Guild_memberData deleteAll", e)
        } finally {
            close(p)
        }
    }

    fun update(player: Jugador) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("REPLACE INTO `guild_members` VALUES(?,?,?,?,?,?,?,?,?,?)")
            val gm = player.guildMember ?: return
            p!!.setInt(1, gm.playerId)
            p.setInt(2, gm.guild.id)
            p.setString(3, player.name)
            p.setInt(4, gm.lvl)
            var gfx = gm.gfx
            if (gfx > 121 || gfx < 10) gfx = player.classe * 10 + player.sexe
            p.setInt(5, gfx)
            p.setInt(6, gm.rank)
            p.setLong(7, gm.xpGave)
            p.setInt(8, gm.xpGive)
            p.setInt(9, gm.rights)
            p.setInt(10, gm.align)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("Guild_memberData update", e)
        } finally {
            close(p)
        }
    }

    fun isPersoInGuild(guid: Int): Int {
        var result: Result? = null
        var guildId = -1
        try {
            result = getData(
                "SELECT `guild` FROM `guild_members` WHERE `guid` ="
                        + guid
            )
            val consultaGremios = result!!.resultSet
            val found = consultaGremios!!.first()
            if (found) guildId = consultaGremios.getInt("guild")
        } catch (e: SQLException) {
            super.sendError("Guild_memberData isPersoInGuild", e)
        } finally {
            close(result)
        }
        return guildId
    }

    fun isPersoInGuild(name: String): IntArray {
        var result: Result? = null
        var guildId = -1
        var guid = -1
        try {
            result = getData(
                "SELECT `guild`,`guid` FROM `guild_members` WHERE name='"
                        + name + "'"
            )
            val consultaGremios = result!!.resultSet
            val found = consultaGremios!!.first()
            if (found) {
                guildId = consultaGremios.getInt("guild")
                guid = consultaGremios.getInt("guid")
            }
        } catch (e: SQLException) {
            super.sendError("Guild_memberData isPersoInGuild", e)
        } finally {
            close(result)
        }
        return intArrayOf(guid, guildId)
    }
}