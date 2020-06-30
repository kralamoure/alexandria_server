package org.alexandria.comunes.gestorsql.dinamicos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.dinamicos.AbstractDAO
import org.alexandria.estaticos.objeto.ObjetoJuego
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosObjevivos(dataSource: HikariDataSource?) :
    AbstractDAO<ObjetoJuego>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: ObjetoJuego): Boolean {
        return false
    }

    fun add(obvijevan: ObjetoJuego, `object`: ObjetoJuego) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("INSERT INTO `world.entity.obvijevans`(`id`, `template`) VALUES(?, ?);")
            p!!.setInt(1, `object`.id)
            p.setInt(2, obvijevan.modelo.id)
            execute(p)
        } catch (e: Exception) {
            super.sendError("ObvejivanData add", e)
        } finally {
            close(p)
        }
    }

    fun getAndDelete(`object`: ObjetoJuego, delete: Boolean): Int {
        var result: Result? = null
        var template = -1
        try {
            result = getData("SELECT * FROM `world.entity.obvijevans` WHERE `id` = '" + `object`.id + "';")
            val resultSet = result!!.resultSet
            if (resultSet!!.next()) {
                template = resultSet.getInt("template")
                if (delete) {
                    val ps =
                        getPreparedStatement("DELETE FROM `world.entity.obvijevans` WHERE id = '" + `object`.id + "';")
                    execute(ps!!)
                }
            }
        } catch (e: SQLException) {
            super.sendError("ObvejivanData getAndDelete", e)
        } finally {
            close(result)
        }
        return template
    }
}