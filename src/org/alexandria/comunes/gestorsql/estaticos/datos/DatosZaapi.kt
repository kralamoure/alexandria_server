package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.Constantes
import java.sql.SQLException

class DatosZaapi(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        var i = 0
        val bonta = StringBuilder()
        val brak = StringBuilder()
        val neutral = StringBuilder()
        try {
            result = getData("SELECT mapid, align from zaapi")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                if (rs.getInt("align") == Constantes.ALINEAMIENTO_BONTARIANO) {
                    bonta.append(rs.getString("mapid"))
                    if (!rs.isLast) bonta.append(",")
                } else if (rs.getInt("align") == Constantes.ALINEAMIENTO_BRAKMARIANO) {
                    brak.append(rs.getString("mapid"))
                    if (!rs.isLast) brak.append(",")
                } else {
                    neutral.append(rs.getString("mapid"))
                    if (!rs.isLast) neutral.append(",")
                }
                i++
            }
            Constantes.ZAAPI[Constantes.ALINEAMIENTO_BONTARIANO] = bonta.toString()
            Constantes.ZAAPI[Constantes.ALINEAMIENTO_BRAKMARIANO] = brak.toString()
            Constantes.ZAAPI[Constantes.ALINEAMIENTO_NEUTRAL] = neutral.toString()
        } catch (e: SQLException) {
            super.sendError("ZaapiData load", e)
        } finally {
            close(result)
        }
    }
}