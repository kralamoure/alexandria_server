package org.alexandria.comunes.gestorsql.dinamicos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.comandos.administracion.Comandos
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.MainServidor.stop
import java.sql.SQLException

class DatosComandos(dataSource: HikariDataSource?) :
    AbstractDAO<Comandos?>(dataSource!!) {
    override fun load(obj: Any?) {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `administration.commands`;")
            val rs = result!!.resultSet
            while (rs!!.next()) Comandos(
                rs.getInt("id"),
                rs.getString("command"),
                rs.getString("args"),
                rs.getString("description")
            )
        } catch (e: SQLException) {
            super.sendError("CommandData load", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }

    override fun update(obj: Comandos?): Boolean {
        return false
    }
}