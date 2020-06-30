package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.otro.utilidad.Doble
import java.sql.SQLException
import java.util.*

class DatosRecetas(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * from crafts")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val m = ArrayList<Doble<Int, Int>>()
                var cont = true
                for (str in rs.getString("craft").split(";".toRegex()).toTypedArray()) {
                    try {
                        val tID = str.split("\\*".toRegex()).toTypedArray()[0].toInt()
                        val qua = str.split("\\*".toRegex()).toTypedArray()[1].toInt()
                        m.add(Doble(tID, qua))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        cont = false
                    }
                }
                if (!cont) // S'il y a eu une erreur de parsing, on ignore cette recette
                    continue
                Mundo.mundo.addCraft(rs.getInt("id"), m)
            }
        } catch (e: SQLException) {
            super.sendError("CraftData load", e)
        } finally {
            close(result)
        }
    }
}