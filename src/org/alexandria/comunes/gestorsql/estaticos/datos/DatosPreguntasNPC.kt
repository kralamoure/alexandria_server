package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.estaticos.Npc.*
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosPreguntasNPC(dataSource: HikariDataSource?) :
    AbstractDAO<NpcPregunta>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: NpcPregunta): Boolean {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `npc_questions` SET `responses` = ?WHERE `ID` = ?")
            p!!.setString(1, obj.anwsers)
            p.setInt(2, obj.id)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("Npc_questionData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun updateLot() {
        val lot = Mundo.mundo.getNPCQuestion(1646).args.toInt() + 50
        Mundo.mundo.getNPCQuestion(1646).args = lot.toString() + ""
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement(
                "UPDATE `npc_questions` SET params='"
                        + lot + "' WHERE `id`=1646"
            )
            execute(p!!)
        } catch (e: SQLException) {
            super.sendError("Npc_questionData updateLot", e)
        } finally {
            close(p)
        }
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM npc_questions")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                Mundo.mundo.addNPCQuestion(
                    NpcPregunta(
                        rs.getInt("ID"),
                        rs.getString("responses"),
                        rs.getString("params"),
                        rs.getString("cond"),
                        rs.getString("ifFalse")
                    )
                )
            }
        } catch (e: SQLException) {
            super.sendError("Npc_questionData load", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }
}