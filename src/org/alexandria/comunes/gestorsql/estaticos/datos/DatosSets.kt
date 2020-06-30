package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.estaticos.objeto.ObjetoSet
import java.sql.SQLException

class DatosSets(dataSource: HikariDataSource?) :
    AbstractDAO<ObjetoSet>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: ObjetoSet): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * from itemsets")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                Mundo.mundo.addItemSet(ObjetoSet(rs.getInt("id"), rs.getString("items"), rs.getString("bonus")))
            }
            close(result)
        } catch (e: SQLException) {
            super.sendError("ItemsetData load", e)
        } finally {
            close(result)
        }
    }
}