package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.Casas
import org.alexandria.estaticos.Cofres
import org.alexandria.estaticos.cliente.Jugador
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosCofres(dataSource: HikariDataSource?) :
    AbstractDAO<Cofres>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Cofres): Boolean {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `coffres` SET `kamas`=?, `object`=? WHERE `id`=?")
            p!!.setLong(1, obj.kamas)
            p.setString(2, obj.parseTrunkObjetsToDB())
            p.setInt(3, obj.id)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("CoffreData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun load() {
        var result: Result? = null
        var nbr = 0
        try {
            result = getData("SELECT * from coffres")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val id = rs.getInt("id")
                var objects = rs.getString("object")
                objects = if (objects == null || objects == " ") "" else objects
                val kamas = rs.getInt("kamas")
                val owner_id = rs.getInt("owner_id")
                val key = rs.getString("key")
                val t = Mundo.mundo.getTrunk(id) ?: continue
                t.setObjects(objects)
                t.kamas = kamas.toLong()
                t.ownerId = owner_id
                t.key = key
                nbr++
            }
        } catch (e: SQLException) {
            super.sendError("CoffreData load", e)
        } finally {
            close(result)
        }
    }

    fun exist(trunk: Cofres) {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `coffres` WHERE `id` = '" + trunk.id + "';")
            val rs = result!!.resultSet
            if (!rs!!.next()) {
                insert(trunk)
            }
        } catch (e: SQLException) {
            super.sendError("CoffreData load", e)
        } finally {
            close(result)
        }
    }

    fun update(player: Jugador, house: Casas?) {
        var p: PreparedStatement? = null
        for (trunk in Cofres.getTrunksByHouse(house)) {
            if (trunk.ownerId != player.accID) {
                trunk.ownerId = player.accID
                trunk.key = "-"
                try {
                    p = getPreparedStatement("UPDATE `coffres` SET `owner_id`=?, `key`='-' WHERE `id`=?")
                    p!!.setInt(1, player.accID)
                    p.setInt(2, trunk.id)
                    execute(p)
                } catch (e: SQLException) {
                    super.sendError("CoffreData update", e)
                } finally {
                    close(p)
                }
            }
        }
    }

    fun insert(trunk: Cofres) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement(
                "INSERT INTO `coffres` (`id`, `object`, `kamas`, `key`, `owner_id`) " +
                        "VALUES (?, ?, ?, ?, ?)"
            )
            p!!.setInt(1, trunk.id)
            p.setString(2, "")
            p.setInt(3, 0)
            p.setString(4, "-")
            p.setInt(5, trunk.ownerId)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("Coffre insert", e)
        } finally {
            close(p)
        }
    }

    fun updateCode(P: Jugador, t: Cofres, packet: String?) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `coffres` SET `key`=? WHERE `id`=? AND owner_id=?")
            p!!.setString(1, packet)
            p.setInt(2, t.id)
            p.setInt(3, P.accID)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("CoffreData updateCode", e)
        } finally {
            close(p)
        }
    }
}