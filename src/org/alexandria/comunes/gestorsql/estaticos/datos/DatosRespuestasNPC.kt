package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.estaticos.Npc.*
import org.alexandria.otro.Accion
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosRespuestasNPC(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM npc_reponses_actions")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val id = rs.getInt("ID")
                val type = rs.getInt("type")
                val args = rs.getString("args")
                if (Mundo.mundo.getNpcAnswer(id) == null) Mundo.mundo.addNpcAnswer(NpcRespuesta(id))
                Mundo.mundo.getNpcAnswer(id).addAction(Accion(type, args, "", null))
            }
        } catch (e: SQLException) {
            super.sendError("Npc_reponses_actionData load", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }

    fun add(repID: Int, type: Int, args: String?): Boolean {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("DELETE FROM `npc_reponses_actions` WHERE `ID` = ? AND `type` = ?")
            p!!.setInt(1, repID)
            p.setInt(2, type)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("Npc_reponses_actionData add", e)
        } finally {
            close(p)
        }
        try {
            p = getPreparedStatement("INSERT INTO `npc_reponses_actions` VALUES (?,?,?)")
            p!!.setInt(1, repID)
            p.setInt(2, type)
            p.setString(3, args)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("Npc_reponses_actionData add", e)
        } finally {
            close(p)
        }
        return false
    }
}