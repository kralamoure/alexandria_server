package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.Mercadillo
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.SQLException

class DatosMercadillos(dataSource: HikariDataSource?) :
    AbstractDAO<Mercadillo>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Mercadillo): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `hdvs` ORDER BY id ASC")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                Mundo.mundo.addHdv(
                    Mercadillo(
                        rs.getInt("map"),
                        rs.getFloat("sellTaxe"),
                        rs.getShort("sellTime"),
                        rs.getShort("accountItem"),
                        rs.getShort("lvlMax"),
                        rs.getString("categories")
                    )
                )
            }
            close(result)
        } catch (e: SQLException) {
            super.sendError("HdvData load", e)
        } finally {
            close(result)
        }
    }
}