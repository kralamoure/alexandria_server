package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.area.mapa.Mapa
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.Monstruos.MobGrade
import org.alexandria.estaticos.Monstruos.MobGroup
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.estaticos.objeto.ObjetoJuego
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*

class DatosMobsHeroico(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `heroic_mobs_groups`;")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val group = MobGroup(
                    rs.getInt("id"),
                    rs.getInt("cell"),
                    rs.getString("group"),
                    rs.getString("objects"),
                    rs.getShort("stars")
                )
                val map = Mundo.mundo.getMap(rs.getShort("map"))
                map?.respawnGroup(group)
            }
        } catch (e: SQLException) {
            super.sendError("HeroicMobsGroups load", e)
        } finally {
            close(result)
        }
    }

    fun insert(map: Short, group: MobGroup, array: ArrayList<ObjetoJuego>?) {
        var prepare: PreparedStatement? = null
        try {
            val objects = StringBuilder()
            val groups = StringBuilder()
            array?.stream()?.filter { obj: ObjetoJuego? -> Objects.nonNull(obj) }?.forEach { `object`: ObjetoJuego ->
                objects.append(
                    if (objects.toString().isEmpty()) "" else ","
                ).append(`object`.id)
            }
            group.mobs.values.stream()
                .filter { obj: MobGrade? -> Objects.nonNull(obj) }.forEach { monster: MobGrade ->
                    groups.append(
                        if (groups.toString().isEmpty()) "" else ";"
                    ).append(monster.template.id).append(",").append(monster.level).append(",")
                        .append(monster.level)
                }
            prepare = getPreparedStatement("INSERT INTO `heroic_mobs_groups` VALUES (?, ?, ?, ?, ?, ?);")
            prepare!!.setInt(1, group.id)
            prepare.setInt(2, map.toInt())
            prepare.setInt(3, group.cellId)
            prepare.setString(4, groups.toString())
            prepare.setString(5, objects.toString())
            prepare.setInt(6, group.starBonus)
            execute(prepare)
        } catch (e: SQLException) {
            super.sendError("HeroicMobsGroups insert", e)
        } finally {
            close(prepare)
        }
    }

    fun update(map: Short, group: MobGroup) {
        var prepare: PreparedStatement? = null
        try {
            val objects = StringBuilder()
            val groups = StringBuilder()
            group.objects.stream()
                .filter { obj: ObjetoJuego? -> Objects.nonNull(obj) }
                .forEach { `object`: ObjetoJuego ->
                    objects.append(
                        if (objects.toString().isEmpty()) "" else ","
                    ).append(`object`.id)
                }
            group.mobs.values.stream()
                .filter { obj: MobGrade? -> Objects.nonNull(obj) }.forEach { monster: MobGrade ->
                    groups.append(
                        if (groups.toString().isEmpty()) "" else ";"
                    ).append(monster.template.id).append(",").append(monster.level).append(",")
                        .append(monster.level)
                }
            prepare =
                getPreparedStatement("UPDATE `heroic_mobs_groups` SET `objects` = ? WHERE `id` = ? AND `map` = ? AND `group` = ?;")
            prepare!!.setString(1, objects.toString())
            prepare.setLong(2, group.id.toLong())
            prepare.setInt(3, map.toInt())
            prepare.setString(4, groups.toString())
            execute(prepare)
        } catch (e: SQLException) {
            super.sendError("HeroicMobsGroups update", e)
        } finally {
            close(prepare)
        }
    }

    fun delete(map: Short, group: MobGroup) {
        var prepare: PreparedStatement? = null
        try {
            val groups = StringBuilder()
            group.mobs.values.stream()
                .filter { obj: MobGrade? -> Objects.nonNull(obj) }.forEach { monster: MobGrade ->
                    groups.append(
                        if (groups.toString().isEmpty()) "" else ";"
                    ).append(monster.template.id).append(",").append(monster.level).append(",")
                        .append(monster.level)
                }
            prepare =
                getPreparedStatement("DELETE FROM `heroic_mobs_groups` WHERE `id` = ? AND `map` = ? AND `group` = ?;")
            prepare!!.setLong(1, group.id.toLong())
            prepare.setInt(2, map.toInt())
            prepare.setString(3, groups.toString())
            execute(prepare)
        } catch (e: SQLException) {
            super.sendError("HeroicMobsGroups delete", e)
        } finally {
            close(prepare)
        }
    }

    fun deleteAll() {
        var prepare: PreparedStatement? = null
        try {
            prepare = getPreparedStatement("DELETE FROM `heroic_mobs_groups`;")
            execute(prepare!!)
        } catch (e: SQLException) {
            super.sendError("HeroicMobsGroups deleteAll", e)
        } finally {
            close(prepare)
        }
    }

    fun loadFix() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `heroic_mobs_groups_fix`;")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val objects = ArrayList<ObjetoJuego>()
                for (value in rs.getString("objects").split(",".toRegex()).toTypedArray()) {
                    val `object` = Mundo.getGameObject(value.toInt())
                    if (`object` != null) objects.add(`object`)
                }
                Mapa.fixMobGroupObjects[rs.getInt("map").toString() + "," + rs.getInt("cell")] = objects
            }
        } catch (e: SQLException) {
            super.sendError("HeroicMobsGroups loadFix", e)
        } finally {
            close(result)
        }
    }

    fun insertFix(map: Short, group: MobGroup, array: ArrayList<ObjetoJuego>) {
        var prepare: PreparedStatement? = null
        try {
            val objects = StringBuilder()
            array.stream()
                .filter { obj: ObjetoJuego? -> Objects.nonNull(obj) }
                .forEach { `object`: ObjetoJuego ->
                    objects.append(
                        if (objects.toString().isEmpty()) "" else ","
                    ).append(`object`.id)
                }
            prepare = getPreparedStatement("INSERT INTO `heroic_mobs_groups_fix` VALUES (?, ?, ?, ?)")
            prepare!!.setInt(1, map.toInt())
            prepare.setInt(2, group.cellId)
            prepare.setString(3, Mundo.mundo.getGroupFix(map.toInt(), group.cellId)["groupData"])
            prepare.setString(4, objects.toString())
            execute(prepare)
        } catch (e: SQLException) {
            super.sendError("HeroicMobsGroups insertFix", e)
        } finally {
            close(prepare)
        }
    }

    fun updateFix() {
        var prepare: PreparedStatement? = null
        try {
            for ((key, value) in Mapa.fixMobGroupObjects) {
                val split = key.split(",".toRegex()).toTypedArray()
                val objects = StringBuilder()
                value.stream()
                    .filter { obj: ObjetoJuego? -> Objects.nonNull(obj) }
                    .forEach { `object`: ObjetoJuego ->
                        objects.append(
                            if (objects.toString().isEmpty()) "" else ","
                        ).append(`object`.id)
                    }
                prepare =
                    getPreparedStatement("UPDATE `heroic_mobs_groups_fix` SET `objects` = ? WHERE `map` = ? AND `cell` = ? AND `group` = ?;")
                prepare!!.setString(1, objects.toString())
                prepare.setLong(2, split[0].toInt().toLong())
                prepare.setInt(3, split[1].toInt())
                prepare.setString(
                    4,
                    Mundo.mundo.getGroupFix(split[0].toInt(), split[1].toInt())["groupData"]
                )
                execute(prepare)
            }
        } catch (e: SQLException) {
            super.sendError("HeroicMobsGroups updateFix", e)
        } finally {
            close(prepare)
        }
    }

    fun deleteAllFix() {
        var prepare: PreparedStatement? = null
        try {
            prepare = getPreparedStatement("DELETE FROM `heroic_mobs_groups_fix`;")
            execute(prepare!!)
        } catch (e: SQLException) {
            super.sendError("HeroicMobsGroups deleteAllFix", e)
        } finally {
            close(prepare)
        }
    }
}