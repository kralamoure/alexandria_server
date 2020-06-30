package org.alexandria.comunes.gestorsql.dinamicos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.cliente.Jugador
import org.alexandria.comunes.gestorsql.dinamicos.AbstractDAO
import org.alexandria.estaticos.Mision.MisionJugador
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosMisionesPersonaje(dataSource: HikariDataSource?) :
    AbstractDAO<MisionJugador>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: MisionJugador): Boolean {
        var p: PreparedStatement? = null
        try {
            p =
                getPreparedStatement("UPDATE `world.entity.players.quests` SET `finish` = ?, `stepsValidation` = ? WHERE `id` = ?;")
            p!!.setInt(1, if (obj.isFinish) 1 else 0)
            p.setString(2, obj.questStepString)
            p.setInt(3, obj.id)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("QuestPlayerData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun update(questPlayer: MisionJugador, player: Jugador) {
        var p: PreparedStatement? = null
        try {
            p =
                getPreparedStatement("UPDATE `world.entity.players.quests` SET `quest`= ?, `finish`= ?, `player` = ?, `stepsValidation` = ? WHERE `id` = ?;")
            p!!.setInt(1, questPlayer.quest.id)
            p.setInt(2, if (questPlayer.isFinish) 1 else 0)
            p.setInt(3, player.id)
            p.setString(4, questPlayer.questStepString)
            p.setInt(5, questPlayer.id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("QuestPlayerData update", e)
        } finally {
            close(p)
        }
    }

    fun loadPerso(player: Jugador) {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `world.entity.players.quests` WHERE `player` = " + player.id + ";")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                player.addQuestPerso(
                    MisionJugador(
                        rs.getInt("id"),
                        rs.getInt("quest"),
                        rs.getInt("finish") == 1,
                        rs.getInt("player"),
                        rs.getString("stepsValidation")
                    )
                )
            }
        } catch (e: SQLException) {
            super.sendError("QuestPlayerData loadPerso", e)
        } finally {
            close(result)
        }
    }

    fun delete(id: Int): Boolean {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("DELETE FROM `world.entity.players.quests` WHERE `id` = ?;")
            p!!.setInt(1, id)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("QuestPlayerData delete", e)
        } finally {
            close(p)
        }
        return false
    }

    fun add(questPlayer: MisionJugador) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("INSERT INTO `world.entity.players.quests` VALUES (?, ?, ?, ?, ?);")
            p!!.setInt(1, questPlayer.id)
            p.setInt(2, questPlayer.quest.id)
            p.setInt(3, if (questPlayer.isFinish) 1 else 0)
            p.setInt(4, questPlayer.player.id)
            p.setString(5, questPlayer.questStepString)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("QuestPlayerData add", e)
        } finally {
            close(p)
        }
    }

    val nextId: Int
        get() = DatosGenerales.Companion.getNextQuestPlayerId()
}