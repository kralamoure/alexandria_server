package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.estaticos.objeto.ObjetoAccion
import java.sql.SQLException

class DatosObjetoAccion(dataSource: HikariDataSource?) :
    AbstractDAO<ObjetoAccion>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: ObjetoAccion): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        var nbr = 0
        try {
            result = getData("SELECT * FROM objectsactions")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val id = rs.getInt("template")
                val type = rs.getString("type")
                val args = rs.getString("args")
                if (Mundo.mundo.getObjetoModelo(id) == null) continue
                Mundo.mundo.getObjetoModelo(id).addAction(ObjetoAccion(type, args, ""))
                nbr++
            }
        } catch (e: SQLException) {
            super.sendError("ObjectsactionData load", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }

    fun reload() {
        var result: Result? = null
        var nbr = 0
        try {
            result = getData("SELECT * FROM objectsactions")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val id = rs.getInt("template")
                val type = rs.getString("type")
                val args = rs.getString("args")
                if (Mundo.mundo.getObjetoModelo(id) == null) continue
                Mundo.mundo.getObjetoModelo(id).onUseActions.clear()
                Mundo.mundo.getObjetoModelo(id).addAction(ObjetoAccion(type, args, ""))
                nbr++
            }
            close(result)
        } catch (e: SQLException) {
            super.sendError("ObjectsactionData reload", e)
        } finally {
            close(result)
        }
    }
}