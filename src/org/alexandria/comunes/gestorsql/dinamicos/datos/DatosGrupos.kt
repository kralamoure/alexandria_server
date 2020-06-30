package org.alexandria.comunes.gestorsql.dinamicos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.comandos.administracion.GrupoADM
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.MainServidor.stop
import java.sql.SQLException

class DatosGrupos(dataSource: HikariDataSource?) :
    AbstractDAO<GrupoADM?>(dataSource!!) {
    override fun load(obj: Any?) {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `administration.groups`;")
            val rs = result!!.resultSet
            while (rs!!.next()) GrupoADM(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getBoolean("isPlayer"),
                rs.getString("commands")
            )
        } catch (e: SQLException) {
            super.sendError("GroupData load", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }

    override fun update(obj: GrupoADM?): Boolean {
        return false
    }
}