package org.alexandria.comunes.gestorsql.dinamicos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.cliente.Jugador
import org.alexandria.comunes.gestorsql.Database
import org.alexandria.comunes.gestorsql.dinamicos.AbstractDAO
import org.alexandria.estaticos.Montura
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosMonturas(dataSource: HikariDataSource?) :
    AbstractDAO<Montura>(dataSource!!) {
    override fun load(obj: Any?) {
        var result: Result? = null
        try {
            result = getData("SELECT * from `world.entity.mounts` WHERE `id` = " + obj as Int + ";")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                Mundo.mundo.addMount(
                    Montura(
                        rs.getInt("id"),
                        rs.getInt("color"),
                        rs.getInt("sex"),
                        rs.getInt("amour"),
                        rs.getInt("endurance"),
                        rs.getInt("level"),
                        rs.getLong("xp"),
                        rs.getString("name"),
                        rs.getInt("fatigue"),
                        rs.getInt("energy"),
                        rs.getInt("reproductions"),
                        rs.getInt("maturity"),
                        rs.getInt("serenity"),
                        rs.getString("objects"),
                        rs.getString("ancestors"),
                        rs.getString("capacitys"),
                        rs.getInt("size"),
                        rs.getInt("cell"),
                        rs.getShort("map"),
                        rs.getInt("owner"),
                        rs.getInt("orientation"),
                        rs.getLong("fecundatedDate"),
                        rs.getInt("couple"),
                        rs.getInt("savage")
                    )
                )
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            super.sendError("MountData load", e)
        } finally {
            close(result)
        }
    }

    override fun update(obj: Montura): Boolean {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement(
                "UPDATE `world.entity.mounts` SET `name` = ?, `xp` = ?, `level` = ?, `endurance` = ?, `amour` = ?, `maturity` = ?, `serenity` = ?, `reproductions` = ?," +
                        "`fatigue` = ?, `energy` = ?, `ancestors` = ?, `objects` = ?, `owner` = ?, `capacitys` = ?, `size` = ?, `cell` = ?, `map` = ?," +
                        " `orientation` = ?, `fecundatedDate` = ?, `couple` = ? WHERE `id` = ?;"
            )
            p!!.setString(1, obj.name)
            p.setLong(2, obj.exp)
            p.setInt(3, obj.level)
            p.setInt(4, obj.endurance)
            p.setInt(5, obj.amour)
            p.setInt(6, obj.maturity)
            p.setInt(7, obj.state)
            p.setInt(8, obj.reproduction)
            p.setInt(9, obj.fatigue)
            p.setInt(10, obj.energy)
            p.setString(11, obj.ancestors)
            p.setString(12, obj.parseObjectsToString())
            p.setInt(13, obj.owner)
            p.setString(14, obj.parseCapacitysToString())
            p.setInt(15, obj.size)
            p.setInt(16, obj.cellId)
            p.setInt(17, obj.mapId.toInt())
            p.setInt(18, obj.orientation)
            p.setLong(19, obj.fecundatedDate)
            p.setInt(20, obj.couple)
            p.setInt(21, obj.id)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("MountData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun delete(id: Int) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("DELETE FROM `world.entity.mounts` WHERE `id` = ?;")
            p!!.setInt(1, id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("MountData delete", e)
        } finally {
            close(p)
        }
    }

    fun delete(player: Jugador) {
        this.delete(player.mount.id)
        Mundo.mundo.delDragoByID(player.mount.id)
        player.setMountGiveXp(0)
        player.mount = null
        Database.dinamicos.playerData!!.update(player)
    }

    fun add(mount: Montura) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement(
                "INSERT INTO `world.entity.mounts`(`id`, `color`, `sex`, `name`, `xp`, `level`, `endurance`, `amour`, `maturity`, `serenity`, `reproductions`, `fatigue`, `energy`," +
                        "`objects`, `ancestors`, `capacitys`, `size`, `map`, `cell`, `owner`, `orientation`, `fecundatedDate`, `couple`, `savage`) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            )
            p!!.setInt(1, mount.id)
            p.setInt(2, mount.color)
            p.setInt(3, mount.sex)
            p.setString(4, mount.name)
            p.setLong(5, mount.exp)
            p.setInt(6, mount.level)
            p.setInt(7, mount.endurance)
            p.setInt(8, mount.amour)
            p.setInt(9, mount.maturity)
            p.setInt(10, mount.state)
            p.setInt(11, mount.reproduction)
            p.setInt(12, mount.fatigue)
            p.setInt(13, mount.energy)
            p.setString(14, mount.parseObjectsToString())
            p.setString(15, mount.ancestors)
            p.setString(16, mount.parseCapacitysToString())
            p.setInt(17, mount.size)
            p.setInt(18, mount.mapId.toInt())
            p.setInt(19, mount.cellId)
            p.setInt(20, mount.owner)
            p.setInt(21, mount.orientation)
            p.setLong(22, mount.fecundatedDate)
            p.setInt(23, mount.couple)
            p.setInt(24, mount.savage)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("MountData add", e)
        } finally {
            close(p)
        }
    }

    val nextId: Int
        get() = DatosGenerales.Companion.getNextMountId()
}