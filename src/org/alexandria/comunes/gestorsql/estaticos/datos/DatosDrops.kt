package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.Monstruos
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.estaticos.juego.mundo.Mundo.Drop
import java.sql.SQLException
import java.util.*

class DatosDrops(dataSource: HikariDataSource?) :
    AbstractDAO<Drop>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Drop): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * from drops")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val monstruo = Mundo.mundo.getMonstre(rs.getInt("monsterId"))
                if (Mundo.mundo.getObjetoModelo(rs.getInt("objectId")) != null && monstruo != null) {
                    var action = rs.getString("action")
                    var condition = ""
                    if (action != "-1" && action != "1"
                        && action.contains(":")
                    ) {
                        condition = action.split(":".toRegex()).toTypedArray()[1]
                        action = action.split(":".toRegex()).toTypedArray()[0]
                    }
                    val percents = ArrayList<Double>()
                    percents.add(rs.getDouble("percentGrade1"))
                    percents.add(rs.getDouble("percentGrade2"))
                    percents.add(rs.getDouble("percentGrade3"))
                    percents.add(rs.getDouble("percentGrade4"))
                    percents.add(rs.getDouble("percentGrade5"))
                    monstruo.addDrop(
                        Drop(
                            rs.getInt("objectId"),
                            percents,
                            rs.getInt("ceil"),
                            action.toInt(),
                            rs.getInt("level"),
                            condition
                        )
                    )
                } else {
                    if (monstruo == null && rs.getInt("monsterId") == 0) {
                        var action = rs.getString("action")
                        var condition = ""
                        if (action != "-1" && action != "1"
                            && action.contains(":")
                        ) {
                            condition = action.split(":".toRegex()).toTypedArray()[1]
                            action = action.split(":".toRegex()).toTypedArray()[0]
                        }
                        val percents = ArrayList<Double>()
                        percents.add(rs.getDouble("percentGrade1"))
                        percents.add(rs.getDouble("percentGrade2"))
                        percents.add(rs.getDouble("percentGrade3"))
                        percents.add(rs.getDouble("percentGrade4"))
                        percents.add(rs.getDouble("percentGrade5"))
                        val drop = Drop(
                            rs.getInt("objectId"),
                            percents,
                            rs.getInt("ceil"),
                            action.toInt(),
                            rs.getInt("level"),
                            condition
                        )
                        Mundo.mundo.monstres.stream()
                            .filter { obj: Monstruos? -> Objects.nonNull(obj) }
                            .forEach { monster: Monstruos -> monster.addDrop(drop) }
                    }
                }
            }
        } catch (e: SQLException) {
            super.sendError("DropData load", e)
        } finally {
            close(result)
        }
    }

    fun reload() {
        Mundo.mundo.monstres.stream()
            .filter { obj: Monstruos? -> Objects.nonNull(obj) }.filter { m: Monstruos -> m.drops != null }
            .forEach { m: Monstruos -> m.drops.clear() }
        load()
    }
}