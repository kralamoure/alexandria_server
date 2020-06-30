package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.Mision
import java.sql.SQLException

class DatosMisiones(dataSource: HikariDataSource?) :
    AbstractDAO<Mision>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Mision): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `quest_data`;")
            val rs = result!!.resultSet
            Mision.getQuestList().clear()
            while (rs!!.next()) {
                val quest = Mision(
                    rs.getInt("id"),
                    rs.getString("etapes"),
                    rs.getString("objectif"),
                    rs.getInt("npc"),
                    rs.getString("action"),
                    rs.getString("args"),
                    rs.getInt("deleteFinish") == 1,
                    rs.getString("condition")
                )
                if (quest.npcTemplate != null) {
                    quest.npcTemplate.quest = quest
                    quest.npcTemplate.extraClip = 4
                }
                Mision.addQuest(quest)
            }
        } catch (e: SQLException) {
            super.sendError("Quest_dataData load", e)
        } finally {
            close(result)
        }
    }
}