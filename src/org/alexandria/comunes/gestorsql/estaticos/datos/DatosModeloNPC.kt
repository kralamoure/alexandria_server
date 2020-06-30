package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.estaticos.Mision
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.estaticos.Npc.*
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosModeloNPC(dataSource: HikariDataSource?) :
    AbstractDAO<NpcModelo>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: NpcModelo): Boolean {
        val i = StringBuilder()
        var first = true
        for (obj in obj.allItem) {
            if (first) i.append(obj.id) else i.append(",").append(obj.id)
            first = false
        }
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE npc_template SET `ventes` = ? WHERE `id` = ?")
            p!!.setString(1, i.toString())
            p.setInt(2, obj.id)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("Npc_templateData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun updateAperiencia(npc: NpcModelo): Boolean {
        var p: PreparedStatement? = null
        return try {
            p =
                getPreparedStatement("UPDATE npc_template SET `gfxID` = ?,`scaleX` = ?,`scaleY` = ?,`sex` = ?,`color1` = ?,`color2` = ?,`color3` = ?,`accessories` = ? WHERE `id` = ?")
            p!!.setInt(1, npc.gfxId)
            p.setInt(2, npc.scaleX)
            p.setInt(3, npc.scaleY)
            p.setInt(4, npc.sex)
            p.setInt(5, npc.color1)
            p.setInt(6, npc.color2)
            p.setInt(7, npc.color3)
            p.setString(8, npc.accessories)
            p.setInt(9, npc.id)
            execute(p)
            close(p)
            true
        } catch (e: SQLException) {
            super.sendError("Npc_templateData update", e)
            false
        }
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM npc_template")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val id = rs.getInt("id")
                val bonusValue = rs.getInt("bonusValue")
                val gfxID = rs.getInt("gfxID")
                val scaleX = rs.getInt("scaleX")
                val scaleY = rs.getInt("scaleY")
                val sex = rs.getInt("sex")
                val color1 = rs.getInt("color1")
                val color2 = rs.getInt("color2")
                val color3 = rs.getInt("color3")
                val access = rs.getString("accessories")
                val extraClip = rs.getInt("extraClip")
                val customArtWork = rs.getInt("customArtWork")
                val initQId = rs.getString("initQuestion")
                val ventes = rs.getString("ventes")
                val quests = rs.getString("quests")
                val exchanges = rs.getString("exchanges")
                Mundo.mundo.addNpcTemplate(
                    NpcModelo(
                        id,
                        bonusValue,
                        gfxID,
                        scaleX,
                        scaleY,
                        sex,
                        color1,
                        color2,
                        color3,
                        access,
                        extraClip,
                        customArtWork,
                        initQId,
                        ventes,
                        quests,
                        exchanges,
                        rs.getString("path"),
                        rs.getByte("informations"),
                        rs.getString("NombreNpc")
                    )
                )
            }
        } catch (e: SQLException) {
            super.sendError("Npc_templateData load", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }

    fun loadQuest() {
        var result: Result? = null
        try {
            result = getData("SELECT id, quests FROM npc_template")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val id = rs.getInt("id")
                val quests = rs.getString("quests")
                if (quests.equals("", ignoreCase = true)) continue
                val nt = Mundo.mundo.getNPCTemplate(id) ?: continue
                val q = Mision.getQuestById(quests.toInt()) ?: continue
                nt.quest = q
            }
        } catch (e: Exception) {
            super.sendError("Npc_templateData loadQuest", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }

    fun reload() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM npc_template")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                if (Mundo.mundo.getNPCTemplate(rs.getInt("id")) == null) {
                    val id = rs.getInt("id")
                    val bonusValue = rs.getInt("bonusValue")
                    val gfxID = rs.getInt("gfxID")
                    val scaleX = rs.getInt("scaleX")
                    val scaleY = rs.getInt("scaleY")
                    val sex = rs.getInt("sex")
                    val color1 = rs.getInt("color1")
                    val color2 = rs.getInt("color2")
                    val color3 = rs.getInt("color3")
                    val access = rs.getString("accessories")
                    val extraClip = rs.getInt("extraClip")
                    val customArtWork = rs.getInt("customArtWork")
                    val initQId = rs.getString("initQuestion")
                    val ventes = rs.getString("ventes")
                    val quests = rs.getString("quests")
                    val exchanges = rs.getString("exchanges")
                    Mundo.mundo.addNpcTemplate(
                        NpcModelo(
                            id,
                            bonusValue,
                            gfxID,
                            scaleX,
                            scaleY,
                            sex,
                            color1,
                            color2,
                            color3,
                            access,
                            extraClip,
                            customArtWork,
                            initQId,
                            ventes,
                            quests,
                            exchanges,
                            rs.getString("path"),
                            rs.getByte("informations"),
                            rs.getString("NombreNpc")
                        )
                    )
                } else {
                    val id = rs.getInt("id")
                    val bonusValue = rs.getInt("bonusValue")
                    val gfxID = rs.getInt("gfxID")
                    val scaleX = rs.getInt("scaleX")
                    val scaleY = rs.getInt("scaleY")
                    val sex = rs.getInt("sex")
                    val color1 = rs.getInt("color1")
                    val color2 = rs.getInt("color2")
                    val color3 = rs.getInt("color3")
                    val access = rs.getString("accessories")
                    val extraClip = rs.getInt("extraClip")
                    val customArtWork = rs.getInt("customArtWork")
                    val initQId = rs.getString("initQuestion")
                    val ventes = rs.getString("ventes")
                    val quests = rs.getString("quests")
                    val exchanges = rs.getString("exchanges")
                    Mundo.mundo.getNPCTemplate(rs.getInt("id")).setInfos(
                        id,
                        bonusValue,
                        gfxID,
                        scaleX,
                        scaleY,
                        sex,
                        color1,
                        color2,
                        color3,
                        access,
                        extraClip,
                        customArtWork,
                        initQId,
                        ventes,
                        quests,
                        exchanges,
                        rs.getString("path"),
                        rs.getByte("informations")
                    )
                }
            }
        } catch (e: SQLException) {
            super.sendError("Npc_templateData reload", e)
        } finally {
            close(result)
        }
    }
}