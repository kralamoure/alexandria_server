package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.Mision.MisionObjetivo
import java.sql.SQLException

class DatosMisionesObjetivos(dataSource: HikariDataSource?) :
    AbstractDAO<MisionObjetivo>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: MisionObjetivo): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `quest_objectifs`;")
            val loc1 = result!!.resultSet
            MisionObjetivo.getQuestObjectifList().clear()
            while (loc1!!.next()) {
                val qObjectif = MisionObjetivo(
                    loc1.getInt("id"),
                    loc1.getInt("xp"),
                    loc1.getInt("kamas"),
                    loc1.getString("item"),
                    loc1.getString("action")
                )
                MisionObjetivo.addQuestObjectif(qObjectif)
            }
            close(result)
        } catch (e: SQLException) {
            super.sendError("QuestObjectifData load", e)
        } finally {
            close(result)
        }
    }
}