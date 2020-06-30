package org.alexandria.comunes.gestorsql.dinamicos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.dinamicos.AbstractDAO
import java.sql.SQLException

class DatosGenerales(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {
        var result: Result? = null
        try {
            result = getData("SELECT MIN(id) AS min FROM `world.entity.mounts`;")
            val rs = result!!.resultSet
            val found = rs!!.first()
            nextMountId = if (found) rs.getInt("min") else -1
        } catch (e: SQLException) {
            logger.error("WorldEntityData load", e)
        } finally {
            close(result)
        }
        try {
            result = getData("SELECT MAX(id) AS max FROM `world.entity.objects`;")
            val rs = result!!.resultSet
            val found = rs!!.first()
            nextObjectId = if (found) rs.getInt("max") else 1
        } catch (e: SQLException) {
            logger.error("WorldEntityData load", e)
        } finally {
            close(result)
        }
        try {
            result = getData("SELECT MAX(id) AS max FROM `world.entity.players.quests`;")
            val rs = result!!.resultSet
            val found = rs!!.first()
            nextQuestId = if (found) rs.getInt("max") else 1
        } catch (e: SQLException) {
            logger.error("WorldEntityData load", e)
        } finally {
            close(result)
        }
        try {
            result = getData("SELECT MAX(id) AS max FROM `world.entity.guilds`;")
            val rs = result!!.resultSet
            val found = rs!!.first()
            nextGuildId = if (found) rs.getInt("max") else 1
        } catch (e: SQLException) {
            logger.error("WorldEntityData load", e)
        } finally {
            close(result)
        }
        try {
            result = getData("SELECT MAX(id) AS max FROM `world.entity.pets`;")
            val rs = result!!.resultSet
            val found = rs!!.first()
            nextPetId = if (found) rs.getInt("max") else 1
        } catch (e: SQLException) {
            logger.error("WorldEntityData load", e)
        } finally {
            close(result)
        }
    }

    override fun update(obj: Any): Boolean {
        return false
    }

    companion object {
        private var nextMountId = 0
        private var nextObjectId = 0
        private var nextQuestId = 0
        private var nextGuildId = 0
        private var nextPetId = 0
        fun getNextMountId(): Int {
            return --nextMountId
        }

        fun getNextObjectId(): Int {
            return ++nextObjectId
        }

        fun getNextQuestPlayerId(): Int {
            return ++nextQuestId
        }

        fun getNextGuildId(): Int {
            return ++nextGuildId
        }

        fun getNextPetId(): Int {
            return ++nextPetId
        }
    }

    init {
        load(null)
    }
}