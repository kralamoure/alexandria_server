package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.area.mapa.entrada.Animaciones
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.SQLException

class DatosAnimaciones(dataSource: HikariDataSource?) :
    AbstractDAO<Animaciones>(dataSource!!) {
    override fun load(obj: Any?) {}
    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM animations")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                Mundo.mundo.addAnimation(
                    Animaciones(
                        rs.getInt("guid"),
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getInt("area"),
                        rs.getInt("action"),
                        rs.getInt("size")
                    )
                )
            }
        } catch (e: SQLException) {
            super.sendError("AnimationData load", e)
        } finally {
            close(result)
        }
    }

    override fun update(obj: Animaciones): Boolean {
        return false
    }
}