package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.SQLException

class DatosRetos(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * from challenge")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                Mundo.mundo.addChallenge(
                    rs.getInt("id")
                        .toString() + "," + rs.getInt("gainXP") + "," + rs.getInt("gainDrop") + "," + rs.getInt("gainParMob") + "," + rs.getInt(
                        "conditions"
                    )
                )
            }
        } catch (e: SQLException) {
            super.sendError("ChallengeData load", e)
        } finally {
            close(result)
        }
    }
}