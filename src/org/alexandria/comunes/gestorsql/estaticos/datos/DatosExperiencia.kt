package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.estaticos.juego.mundo.Mundo.ExpLevel
import java.sql.SQLException

class DatosExperiencia(dataSource: HikariDataSource?) :
    AbstractDAO<ExpLevel>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: ExpLevel): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * from experience")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                Mundo.mundo.addExpLevel(
                    rs.getInt("lvl"),
                    ExpLevel(
                        rs.getLong("perso"),
                        rs.getInt("metier"),
                        rs.getInt("dinde"),
                        rs.getInt("pvp"),
                        rs.getLong("tourmenteurs"),
                        rs.getLong("bandits")
                    )
                )
            }
        } catch (e: SQLException) {
            super.sendError("ExperienceData load", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }
}