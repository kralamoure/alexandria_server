package org.alexandria.comunes.gestorsql.dinamicos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.dinamicos.AbstractDAO
import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.estaticos.objeto.ObjetoJuego
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosObjetos(dataSource: HikariDataSource?) :
    AbstractDAO<ObjetoJuego>(dataSource!!) {
    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `world.entity.objects`;")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val id = rs.getInt("id")
                val template = rs.getInt("template")
                val quantity = rs.getInt("quantity")
                val position = rs.getInt("position")
                val stats = rs.getString("stats")
                val puit = rs.getInt("puit")
                if (quantity == 0) continue
                Mundo.addGameObject(Mundo.mundo.newObjet(id, template, quantity, position, stats, puit), false)
            }
        } catch (e: SQLException) {
            super.sendError("ObjectData load", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }

    override fun load(obj: Any?) {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `world.entity.objects` WHERE `id` IN ($obj);")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val id = rs.getInt("id")
                val template = rs.getInt("template")
                val quantity = rs.getInt("quantity")
                val position = rs.getInt("position")
                val stats = rs.getString("stats")
                val puit = rs.getInt("puit")
                if (quantity == 0) continue
                Mundo.addGameObject(Mundo.mundo.newObjet(id, template, quantity, position, stats, puit), false)
            }
        } catch (e: SQLException) {
            super.sendError("ObjectData load", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }

    override fun update(obj: ObjetoJuego): Boolean {
        if (obj == null) return false
        if (obj.modelo == null) return false
        var p: PreparedStatement? = null
        try {
            p =
                getPreparedStatement("UPDATE `world.entity.objects` SET `template` = ?, `quantity` = ?, `position` = ?, `puit` = ?, `stats` = ? WHERE `id` = ?;")
            p!!.setInt(1, obj.modelo.id)
            p.setInt(2, obj.cantidad)
            p.setInt(3, obj.posicion)
            p.setInt(4, obj.puit)
            p.setString(5, obj.parseToSave())
            p.setInt(6, obj.id)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("ObjectData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun insert(`object`: ObjetoJuego?) {
        if (`object` == null) {
            super.sendError("ObjectData insert", Exception("Object null"))
            return
        } else if (`object`.modelo == null) {
            super.sendError("ObjectData insert", Exception("Template null"))
            return
        }
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("REPLACE INTO `world.entity.objects` VALUES (?, ?, ?, ?, ?, ?);")
            p!!.setInt(1, `object`.id)
            p.setInt(2, `object`.modelo.id)
            p.setInt(3, `object`.cantidad)
            p.setInt(4, `object`.posicion)
            p.setString(5, `object`.parseToSave())
            p.setInt(6, `object`.puit)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("ObjectData insert", e)
        } finally {
            close(p)
        }
    }

    fun delete(id: Int) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("DELETE FROM `world.entity.objects` WHERE id = ?;")
            p!!.setInt(1, id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("ObjectData delete", e)
        } finally {
            close(p)
        }
    }

    val nextId: Int
        get() = DatosGenerales.Companion.getNextObjectId()
}