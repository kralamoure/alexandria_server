package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.estaticos.oficio.Oficio
import java.sql.SQLException

class DatosOficios(dataSource: HikariDataSource?) :
    AbstractDAO<Oficio>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Oficio): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * from jobs_data")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                var skills: String? = ""
                if (rs.getString("skills") != null) skills = rs.getString("skills")
                Mundo.mundo.addJob(Oficio(rs.getInt("id"), rs.getString("tools"), rs.getString("crafts"), skills))
            }
        } catch (e: SQLException) {
            super.sendError("Jobs_dataData load", e)
        } finally {
            close(result)
        }
    }
}