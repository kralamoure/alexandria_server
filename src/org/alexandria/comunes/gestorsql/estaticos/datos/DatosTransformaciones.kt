package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.SQLException

class DatosTransformaciones(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `full_morphs`")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                var args: Array<String?>? = null
                if (rs.getString("args") != "0") {
                    args = rs.getString("args").split("@".toRegex()).toTypedArray()[1].split(",".toRegex())
                        .toTypedArray()
                }
                Mundo.mundo.addFullMorph(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("gfxId"),
                    rs.getString("spells"),
                    args
                )
            }
        } catch (e: SQLException) {
            super.sendError("Full_morphData load", e)
        } finally {
            close(result)
        }
    }
}