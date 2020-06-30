package org.alexandria.comunes.gestorsql.dinamicos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.Casas
import org.alexandria.comunes.gestorsql.dinamicos.AbstractDAO
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.SQLException

class DatosCasas(dataSource: HikariDataSource?) :
    AbstractDAO<Casas?>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Casas?): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        var nbr = 0
        try {
            result = getData("SELECT * from houses")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val map = Mundo.mundo.getMap(rs.getShort("map_id")) ?: continue
                Mundo.mundo.addHouse(
                    Casas(
                        rs.getInt("id"),
                        rs.getShort("map_id"),
                        rs.getInt("cell_id"),
                        rs.getInt("mapid"),
                        rs.getInt("caseid")
                    )
                )
                /*long saleBase = RS.getLong("saleBase");
				Database.getDynamics().getHouseData().update(RS.getInt("id"), saleBase);*/nbr++
            }
        } catch (e: SQLException) {
            super.sendError("HouseData load", e)
            nbr = 0
        } finally {
            close(result)
        }
    }
}