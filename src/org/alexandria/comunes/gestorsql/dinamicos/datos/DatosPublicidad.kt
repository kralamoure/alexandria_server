package org.alexandria.comunes.gestorsql.dinamicos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.dinamicos.AbstractDAO
import org.alexandria.configuracion.Configuracion.SERVER_ID
import org.alexandria.estaticos.juego.mundo.MundoPublicidad
import java.sql.SQLException

class DatosPublicidad(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `pubs` WHERE `server` LIKE '$SERVER_ID|';")
            val RS = result!!.resultSet
            while (RS!!.next()) MundoPublicidad.pubs.add(RS.getString("data"))
        } catch (e: SQLException) {
            super.sendError("PubData load", e)
        } finally {
            close(result)
        }
    }

    override fun update(obj: Any): Boolean {
        return false
    }
}