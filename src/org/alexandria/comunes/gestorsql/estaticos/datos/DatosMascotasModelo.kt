package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.Mascota
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.SQLException

class DatosMascotasModelo(dataSource: HikariDataSource?) :
    AbstractDAO<Mascota>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Mascota): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        var i = 0
        try {
            result = getData("SELECT * from pets")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                i++
                Mundo.mundo.addPets(
                    Mascota(
                        rs.getInt("TemplateID"),
                        rs.getInt("Type"),
                        rs.getString("Gap"),
                        rs.getString("StatsUp"),
                        rs.getInt("Max"),
                        rs.getInt("Gain"),
                        rs.getInt("DeadTemplate"),
                        rs.getInt("Epo"),
                        rs.getString("jet")
                    )
                )
            }
        } catch (e: SQLException) {
            super.sendError("PetData load", e)
        } finally {
            close(result)
        }
    }
}