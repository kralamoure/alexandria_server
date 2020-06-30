package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.area.mapa.Mapa.ObjetosInteractivos.InteractiveObjectTemplate
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.SQLException

class DatosObjetosInteractivos(dataSource: HikariDataSource?) :
    AbstractDAO<InteractiveObjectTemplate>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: InteractiveObjectTemplate): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * from interactive_objects_data")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                Mundo.mundo.addIOTemplate(
                    InteractiveObjectTemplate(
                        rs.getInt("id"),
                        rs.getInt("respawn"),
                        rs.getInt("duration"),
                        rs.getInt("unknow"),
                        rs.getInt("walkable") == 1
                    )
                )
            }
        } catch (e: SQLException) {
            super.sendError("Interactive_objects_dataData load", e)
        } finally {
            close(result)
        }
    }
}