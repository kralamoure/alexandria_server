package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.Cercados
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosCercados(dataSource: HikariDataSource?) :
    AbstractDAO<Cercados>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Cercados): Boolean {
        var p: PreparedStatement? = null
        try {
            p =
                getPreparedStatement("UPDATE `mountpark_data` SET  `owner`=?, `guild`=?, `price` =?, `data` =?, `enclos` =?, `ObjetPlacer`=?, `durabilite`=? WHERE `mapid`=?")
            p!!.setInt(1, obj.owner)
            p.setInt(2, if (obj.guild != null) obj.guild.id else -1)
            p.setInt(3, obj.price)
            p.setString(4, obj.parseEtableToString())
            p.setString(5, obj.parseRaisingToString())
            p.setString(6, obj.stringObject)
            p.setString(7, obj.stringObjDurab)
            p.setInt(8, obj.map.id.toInt())
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("Mountpark_dataData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun load() {
        var result: Result? = null
        var nbr = 0
        try {
            result = getData("SELECT * from mountpark_data")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val map = Mundo.mundo.getMap(rs.getShort("mapid")) ?: continue
                val MP = Mundo.mundo.mountPark[map.id] ?: continue
                val owner = rs.getInt("owner")
                var guild = rs.getInt("guild")
                guild = if (Mundo.mundo.getGuild(guild) != null) guild else -1
                val price = rs.getInt("price")
                val data = rs.getString("data")
                var enclos = rs.getString("enclos")
                var objetPlacer = rs.getString("ObjetPlacer")
                var durabilite = rs.getString("durabilite")
                enclos = if (enclos == " ") "" else enclos
                objetPlacer = if (objetPlacer == " ") "" else objetPlacer
                durabilite = if (durabilite == " ") "" else durabilite
                MP.setData(owner, guild, price, data, objetPlacer, durabilite, enclos)
                nbr++
            }
        } catch (e: SQLException) {
            super.sendError("Mountpark_dataData load", e)
        } finally {
            close(result)
        }
    }

    fun exist(mountPark: Cercados) {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `mountpark_data` WHERE `mapid` = '" + mountPark.map.id + "';")
            val rs = result!!.resultSet
            if (!rs!!.next()) {
                insert(mountPark)
            }
        } catch (e: SQLException) {
            super.sendError("Mountpark_dataData load", e)
        } finally {
            close(result)
        }
    }

    fun insert(mountPark: Cercados) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement(
                "INSERT INTO `mountpark_data` (`mapid`, `owner`, `guild`, `price`, `data`, `enclos`, `ObjetPlacer`, `durabilite`) " +
                        "VALUES (?, ?, ?, ?, '', '', '', '')"
            )
            p!!.setInt(1, mountPark.map.id.toInt())
            p.setInt(2, 0)
            p.setInt(3, -1)
            p.setInt(4, mountPark.priceBase)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("Mountpark insert", e)
        } finally {
            close(p)
        }
    }

    fun reload(i: Int) {
        var result: Result? = null
        try {
            result = getData("SELECT * from mountpark_data")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val map = Mundo.mundo.getMap(rs.getShort("mapid")) ?: continue
                if (rs.getShort("mapid").toInt() != i) continue
                val MP = Mundo.mundo.mountPark[map.id] ?: continue
                val owner = rs.getInt("owner")
                val guild = rs.getInt("guild")
                val price = rs.getInt("price")
                val data = rs.getString("data")
                val enclos = rs.getString("enclos")
                val objetPlacer = rs.getString("ObjetPlacer")
                val durabilite = rs.getString("durabilite")
                MP.setData(owner, guild, price, data, objetPlacer, durabilite, enclos)
            }
        } catch (e: SQLException) {
            super.sendError("Mountpark_dataData reload", e)
        } finally {
            close(result)
        }
    }
}