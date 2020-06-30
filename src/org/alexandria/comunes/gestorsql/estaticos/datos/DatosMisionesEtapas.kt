package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.Mision.MisionEtapa
import java.sql.SQLException

class DatosMisionesEtapas(dataSource: HikariDataSource?) :
    AbstractDAO<MisionEtapa>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: MisionEtapa): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `quest_etapes`")
            val rs = result!!.resultSet
            MisionEtapa.getQuestStepList().clear()
            while (rs!!.next()) {
                val mision = MisionEtapa(
                    rs.getInt("id"),
                    rs.getInt("type"),
                    rs.getInt("objectif"),
                    rs.getString("item"),
                    rs.getInt("npc"),
                    rs.getString("monster"),
                    rs.getString("conditions"),
                    rs.getInt("validationType")
                )
                MisionEtapa.addQuestStep(mision)
            }
        } catch (e: SQLException) {
            super.sendError("Quest_etapeData load", e)
        } finally {
            close(result)
        }
    }
}