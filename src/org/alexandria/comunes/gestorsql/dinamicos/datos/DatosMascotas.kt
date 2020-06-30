package org.alexandria.comunes.gestorsql.dinamicos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.dinamicos.AbstractDAO
import org.alexandria.estaticos.Mascota.MascotaEntrada
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosMascotas(dataSource: HikariDataSource?) :
    AbstractDAO<MascotaEntrada>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: MascotaEntrada): Boolean {
        var p: PreparedStatement? = null
        try {
            p =
                getPreparedStatement("UPDATE `world.entity.pets` SET `lastEatDate` = ?, `quantityEat` = ?, `pdv` = ?, `corpulence` = ?, `isEPO` = ? WHERE `id` = ?;")
            p!!.setLong(1, obj.lastEatDate)
            p.setInt(2, obj.cantidadcomida)
            p.setInt(3, obj.pdv)
            p.setInt(4, obj.corpulencia)
            p.setInt(5, if (obj.isEupeoh) 1 else 0)
            p.setInt(6, obj.objetoid)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("PetData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun load() {
        var result: Result? = null
        var i = 0
        try {
            result = getData("SELECT * FROM `world.entity.pets`;")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                i++
                Mundo.mundo.addPetsEntry(
                    MascotaEntrada(
                        rs.getInt("id"),
                        rs.getInt("template"),
                        rs.getLong("lastEatDate"),
                        rs.getInt("quantityEat"),
                        rs.getInt("pdv"),
                        rs.getInt("corpulence"),
                        rs.getInt("isEPO") == 1
                    )
                )
            }
        } catch (e: SQLException) {
            super.sendError("PetData load", e)
        } finally {
            close(result)
        }
    }

    fun add(id: Int, lastEatDate: Long, template: Int) {
        var p: PreparedStatement? = null
        try {
            p =
                getPreparedStatement("INSERT INTO `world.entity.pets`(`id`, `template`, `lastEatDate`, `quantityEat`, `pdv`, `corpulence`, `isEPO`) VALUES (?, ?, ?, ?, ?, ?, ?);")
            p!!.setInt(1, id)
            p.setInt(2, template)
            p.setLong(3, lastEatDate)
            p.setInt(4, 0)
            p.setInt(5, 10)
            p.setInt(6, 0)
            p.setInt(7, 0)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("PetData add", e)
        } finally {
            close(p)
        }
    }

    fun delete(id: Int) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("DELETE FROM `world.entity.pets` WHERE `id` = ?")
            p!!.setInt(1, id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("PetData delete", e)
        } finally {
            close(p)
        }
    }

    val nextId: Int
        get() = DatosGenerales.Companion.getNextPetId()
}