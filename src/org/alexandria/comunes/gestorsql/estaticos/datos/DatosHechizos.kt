package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.estaticos.Monstruos.MobGrade
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.estaticos.pelea.hechizo.Hechizo
import org.alexandria.estaticos.pelea.hechizo.Hechizo.SortStats
import java.sql.SQLException
import java.util.function.Consumer

class DatosHechizos(dataSource: HikariDataSource?) :
    AbstractDAO<Hechizo>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Hechizo): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT  * from sorts")
            val rs = result!!.resultSet
            var modif = false
            while (rs!!.next()) {
                val id = rs.getInt("id")
                if (Mundo.mundo.getSort(id) != null) {
                    val spell = Mundo.mundo.getSort(id)
                    spell.setInfos(
                        rs.getInt("sprite"),
                        rs.getString("spriteInfos"),
                        rs.getString("effectTarget"),
                        rs.getInt("type"),
                        rs.getInt("duration")
                    )
                    val l1 = parseSortStats(id, 1, rs.getString("lvl1"))
                    val l2 = parseSortStats(id, 2, rs.getString("lvl2"))
                    val l3 = parseSortStats(id, 3, rs.getString("lvl3"))
                    val l4 = parseSortStats(id, 4, rs.getString("lvl4"))
                    var l5: SortStats? = null
                    if (!rs.getString("lvl5").equals("-1", ignoreCase = true)) l5 =
                        parseSortStats(id, 5, rs.getString("lvl5"))
                    var l6: SortStats? = null
                    if (!rs.getString("lvl6").equals("-1", ignoreCase = true)) l6 =
                        parseSortStats(id, 6, rs.getString("lvl6"))
                    spell.sortsStats.clear()
                    spell.addSortStats(1, l1)
                    spell.addSortStats(2, l2)
                    spell.addSortStats(3, l3)
                    spell.addSortStats(4, l4)
                    spell.addSortStats(5, l5)
                    spell.addSortStats(6, l6)
                    modif = true
                } else {
                    val sort = Hechizo(
                        id,
                        rs.getString("nom"),
                        rs.getInt("sprite"),
                        rs.getString("spriteInfos"),
                        rs.getString("effectTarget"),
                        rs.getInt("type"),
                        rs.getInt("duration")
                    )
                    val l1 = parseSortStats(id, 1, rs.getString("lvl1"))
                    val l2 = parseSortStats(id, 2, rs.getString("lvl2"))
                    val l3 = parseSortStats(id, 3, rs.getString("lvl3"))
                    val l4 = parseSortStats(id, 4, rs.getString("lvl4"))
                    var l5: SortStats? = null
                    if (!rs.getString("lvl5").equals("-1", ignoreCase = true)) l5 =
                        parseSortStats(id, 5, rs.getString("lvl5"))
                    var l6: SortStats? = null
                    if (!rs.getString("lvl6").equals("-1", ignoreCase = true)) l6 =
                        parseSortStats(id, 6, rs.getString("lvl6"))
                    sort.addSortStats(1, l1)
                    sort.addSortStats(2, l2)
                    sort.addSortStats(3, l3)
                    sort.addSortStats(4, l4)
                    sort.addSortStats(5, l5)
                    sort.addSortStats(6, l6)
                    Mundo.mundo.addSort(sort)
                }
            }
            if (modif) for (monster in Mundo.mundo.monstres) monster.grades.values.forEach(
                Consumer { MobGrade::refresh }
            )
        } catch (e: SQLException) {
            super.sendError("SortData load", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }

    private fun parseSortStats(id: Int, lvl: Int, str: String): SortStats? {
        return try {
            val stat = str.split(",".toRegex()).toTypedArray()
            val effets = stat[0]
            val CCeffets = stat[1]
            var PACOST = 6
            try {
                PACOST = stat[2].trim { it <= ' ' }.toInt()
            } catch (ignored: NumberFormatException) {
            }
            val POm = stat[3].trim { it <= ' ' }.toInt()
            val POM = stat[4].trim { it <= ' ' }.toInt()
            val TCC = stat[5].trim { it <= ' ' }.toInt()
            val TEC = stat[6].trim { it <= ' ' }.toInt()
            val line = stat[7].trim { it <= ' ' }.equals("true", ignoreCase = true)
            val LDV = stat[8].trim { it <= ' ' }.equals("true", ignoreCase = true)
            val emptyCell = stat[9].trim { it <= ' ' }.equals("true", ignoreCase = true)
            val MODPO = stat[10].trim { it <= ' ' }.equals("true", ignoreCase = true)
            val MaxByTurn = stat[12].trim { it <= ' ' }.toInt()
            val MaxByTarget = stat[13].trim { it <= ' ' }.toInt()
            val CoolDown = stat[14].trim { it <= ' ' }.toInt()
            val type = stat[15].trim { it <= ' ' }
            val level = stat[stat.size - 2].trim { it <= ' ' }.toInt()
            val endTurn = stat[19].trim { it <= ' ' }.equals("true", ignoreCase = true)
            SortStats(
                id,
                lvl,
                PACOST,
                POm,
                POM,
                TCC,
                TEC,
                line,
                LDV,
                emptyCell,
                MODPO,
                MaxByTurn,
                MaxByTarget,
                CoolDown,
                level,
                endTurn,
                effets,
                CCeffets,
                type
            )
        } catch (e: Exception) {
            super.sendError("SortData parseSortStats", e)
            null
        }
    }
}