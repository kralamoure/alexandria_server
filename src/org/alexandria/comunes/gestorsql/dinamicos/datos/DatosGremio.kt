package org.alexandria.comunes.gestorsql.dinamicos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.Gremio
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosGremio(dataSource: HikariDataSource?) :
    AbstractDAO<Gremio?>(dataSource!!) {
    override fun load(obj: Any?) {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `world.entity.guilds` WHERE `id` = $obj;")
            val rs = result!!.resultSet
            while (rs!!.next()) Mundo.mundo.addGuild(
                Gremio(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("emblem"),
                    rs.getInt("lvl"),
                    rs.getLong("xp"),
                    rs.getInt("capital"),
                    rs.getInt("maxCollectors"),
                    rs.getString("spells"),
                    rs.getString("stats"),
                    rs.getLong("date")
                ), false
            )
        } catch (e: SQLException) {
            super.sendError("GuildData load", e)
        } finally {
            close(result)
        }
    }

    override fun update(obj: Gremio?): Boolean {
        var p: PreparedStatement? = null
        try {
            p =
                getPreparedStatement("UPDATE `world.entity.guilds` SET `lvl` = ?, `xp` = ?, `capital` = ?, `maxCollectors` = ?, `spells` = ?, `stats` = ? WHERE id = ?;")
            if (obj != null) {
                p!!.setInt(1, obj.lvl)
            }
            if (obj != null) {
                p?.setLong(2, obj.xp)
            }
            if (obj != null) {
                p?.setInt(3, obj.capital)
            }
            if (obj != null) {
                p?.setInt(4, obj.nbCollectors)
            }
            if (obj != null) {
                p?.setString(5, obj.compileSpell())
            }
            if (obj != null) {
                p?.setString(6, obj.compileStats())
            }
            if (obj != null) {
                p?.setInt(7, obj.id)
            }
            if (p != null) {
                execute(p)
            }
            return true
        } catch (e: SQLException) {
            super.sendError("GuildData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun add(guild: Gremio) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("INSERT INTO `world.entity.guilds` VALUES (?,?,?,1,0,0,0,?,?,?);")
            p!!.setInt(1, guild.id)
            p.setString(2, guild.name)
            p.setString(3, guild.emblem)
            p.setString(4, "462;0|461;0|460;0|459;0|458;0|457;0|456;0|455;0|454;0|453;0|452;0|451;0|")
            p.setString(5, "176;100|158;1000|124;100|")
            p.setLong(6, guild.date)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("GuildData add", e)
        } finally {
            close(p)
        }
    }

    fun delete(id: Int) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("DELETE FROM `world.entity.guilds` WHERE `id` = ?;")
            p!!.setInt(1, id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("GuildData delete", e)
        } finally {
            close(p)
        }
    }

    val nextId: Int
        get() = DatosGenerales.Companion.getNextGuildId()
}