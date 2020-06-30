package org.alexandria.comunes.gestorsql.dinamicos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.Cofres
import org.alexandria.comunes.gestorsql.Database
import org.alexandria.comunes.gestorsql.dinamicos.AbstractDAO
import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosCofres(dataSource: HikariDataSource?) :
    AbstractDAO<Cofres>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(t: Cofres): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        var nbr = 0
        try {
            result = getData("SELECT * from coffres")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val trunk =
                    Cofres(
                        rs.getInt("id"),
                        rs.getInt("id_house"),
                        rs.getShort("mapid"),
                        rs.getInt("cellid")
                    )
                Mundo.mundo.addTrunk(trunk)
                Database.estaticos.trunkData!!.exist(trunk)
                nbr++
            }
        } catch (e: SQLException) {
            super.sendError("CoffreData load", e)
        } finally {
            close(result)
        }
    }

    fun insert(trunk: Cofres) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement(
                "INSERT INTO `coffres` (`id`, `id_house`, `mapid`, `cellid`) " +
                        "VALUES (?, ?, ?, ?)"
            )
            p!!.setInt(1, trunk.id)
            p.setInt(2, trunk.houseId)
            p.setInt(3, trunk.mapId.toInt())
            p.setInt(4, trunk.cellId)
            execute(p)
            Database.estaticos.trunkData!!.insert(trunk)
        } catch (e: SQLException) {
            super.sendError("Coffre insert", e)
        } finally {
            close(p)
        }
    }

    val nextId: Int
        get() {
            var result: Result? = null
            var guid = -1
            try {
                result = getData("SELECT MAX(id) AS max FROM `coffres`")
                val rs = result!!.resultSet
                val found = rs!!.first()
                if (found) guid = rs.getInt("max") + 1
            } catch (e: SQLException) {
                super.sendError("CoffreData getNextId", e)
                stop("unknown")
            } finally {
                close(result)
            }
            return guid
        }
}