package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.area.mapa.entrada.Tutoriales
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.SQLException

class DatosTutoriales(dataSource: HikariDataSource?) :
    AbstractDAO<Tutoriales>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Tutoriales): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM tutoriel")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val id = rs.getInt("id")
                val start = rs.getString("start")
                val reward = (rs.getString("reward1") + "$"
                        + rs.getString("reward2") + "$"
                        + rs.getString("reward3") + "$"
                        + rs.getString("reward4"))
                val end = rs.getString("end")
                Mundo.mundo.addTutorial(Tutoriales(id, reward, start, end))
            }
        } catch (e: SQLException) {
            super.sendError("TutorielData load", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }
}