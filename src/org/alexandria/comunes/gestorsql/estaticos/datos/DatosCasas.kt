package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.Casas
import org.alexandria.estaticos.cliente.Jugador
import org.alexandria.comunes.gestorsql.Database
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosCasas(dataSource: HikariDataSource?) :
    AbstractDAO<Casas>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(h: Casas): Boolean {
        var p: PreparedStatement? = null
        try {
            p =
                getPreparedStatement("UPDATE `houses` SET `owner_id` = ?,`sale` = ?,`guild_id` = ?,`access` = ?,`key` = ?,`guild_rights` = ? WHERE id = ?")
            p!!.setInt(1, h.ownerId)
            p.setInt(2, h.sale)
            p.setInt(3, h.guildId)
            p.setInt(4, h.access)
            p.setString(5, h.key)
            p.setInt(6, h.guildRights)
            p.setInt(7, h.id)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("HouseData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun update(id: Int, price: Long): Boolean {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `houses` SET `sale` = ? WHERE id = ?")
            p!!.setLong(1, price)
            p.setInt(2, id)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("HouseData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun load() {
        var result: Result? = null
        var nbr = 0
        try {
            result = getData("SELECT * from houses")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val id = rs.getInt("id")
                val owner = rs.getInt("owner_id")
                val sale = rs.getInt("sale")
                val guild = rs.getInt("guild_id")
                val access = rs.getInt("access")
                val key = rs.getString("key")
                val guildRights = rs.getInt("guild_rights")
                val house = Mundo.mundo.getHouse(id) ?: continue
                if (owner != 0 && Mundo.mundo.getAccount(owner) == null) {
                    Exception(
                        "La maison " + id
                                + " a un propriétaire inexistant."
                    ).printStackTrace()
                }
                house.ownerId = owner
                house.sale = sale
                house.guildId = guild
                house.access = access
                house.key = key
                house.setGuildRightsWithParse(guildRights)
                nbr++
            }
        } catch (e: SQLException) {
            super.sendError("HouseData load", e)
            nbr = 0
        } finally {
            close(result)
        }
    }

    fun buy(P: Jugador, h: Casas) {
        var p: PreparedStatement? = null
        try {
            p =
                getPreparedStatement("UPDATE `houses` SET `sale`='0', `owner_id`=?, `guild_id`='0', `access`='0', `key`='-', `guild_rights`='0' WHERE `id`=?")
            p!!.setInt(1, P.accID)
            p.setInt(2, h.id)
            execute(p)
            h.sale = 0
            h.ownerId = P.accID
            h.guildId = 0
            h.access = 0
            h.key = "-"
            h.guildRights = 0
            Database.estaticos.trunkData!!.update(P, h)
        } catch (e: SQLException) {
            super.sendError("HouseData buy", e)
        } finally {
            close(p)
        }
    }

    fun sell(h: Casas, price: Int) {
        h.sale = price
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `houses` SET `sale`=? WHERE `id`=?")
            p!!.setInt(1, price)
            p.setInt(2, h.id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("HouseData sell", e)
        } finally {
            close(p)
        }
    }

    fun updateCode(P: Jugador, h: Casas, packet: String?) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `houses` SET `key`=? WHERE `id`=? AND owner_id=?")
            p!!.setString(1, packet)
            p.setInt(2, h.id)
            p.setInt(3, P.accID)
            execute(p)
            h.key = packet
        } catch (e: SQLException) {
            super.sendError("HouseData updateCode", e)
        } finally {
            close(p)
        }
    }

    fun updateGuild(h: Casas, GuildID: Int, GuildRights: Int) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `houses` SET `guild_id`=?, `guild_rights`=? WHERE `id`=?")
            p!!.setInt(1, GuildID)
            p.setInt(2, GuildRights)
            p.setInt(3, h.id)
            execute(p)
            h.guildId = GuildID
            h.guildRights = GuildRights
        } catch (e: SQLException) {
            super.sendError("HouseData updateGuild", e)
        } finally {
            close(p)
        }
    }

    fun removeGuild(GuildID: Int) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `houses` SET `guild_rights`='0', `guild_id`='0' WHERE `guild_id`=?")
            p!!.setInt(1, GuildID)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("HouseData removeGuild", e)
        } finally {
            close(p)
        }
    }
}