package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.SQLException

class DatosMonstruosExtra(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * from extra_monster")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                Mundo.mundo.addExtraMonster(
                    rs.getInt("idMob"),
                    rs.getString("superArea"),
                    rs.getString("subArea"),
                    rs.getInt("chances")
                )
            }
        } catch (e: SQLException) {
            super.sendError("Extra_monsterData load", e)
        } finally {
            close(result)
        }
    }
}