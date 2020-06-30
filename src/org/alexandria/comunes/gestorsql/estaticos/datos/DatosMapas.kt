package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.area.mapa.Mapa
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.Constantes
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosMapas(dataSource: HikariDataSource?) :
    AbstractDAO<Mapa>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Mapa): Boolean {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `maps` SET `places` = ?, `numgroup` = ? WHERE id = ?")
            p!!.setString(1, obj.places)
            p.setInt(2, obj.maxGroupNumb)
            p.setInt(3, obj.id.toInt())
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("MapData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun updateGs(map: Mapa) {
        var p: PreparedStatement? = null
        try {
            p =
                getPreparedStatement("UPDATE `maps` SET `numgroup` = ?, `minSize` = ?, `fixSize` = ?, `maxSize` = ? WHERE id = ?")
            p!!.setInt(1, map.maxGroupNumb)
            p.setInt(2, map.minSize.toInt())
            p.setInt(3, map.fixSize.toInt())
            p.setInt(4, map.maxSize.toInt())
            p.setInt(5, map.id.toInt())
            execute(p)
        } catch (e: SQLException) {
            super.sendError("MapData updateGs", e)
        } finally {
            close(p)
        }
    }

    fun updateMonster(map: Mapa, monsters: String?) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `maps` SET `monsters` = ? WHERE id = ?")
            p!!.setString(1, monsters)
            p.setInt(2, map.id.toInt())
            execute(p)
        } catch (e: SQLException) {
            super.sendError("MapData updateMonster", e)
        } finally {
            close(p)
        }
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM maps LIMIT " + Constantes.LIMITE_DE_MAPAS)
            var rs = result!!.resultSet
            while (rs!!.next()) {
                Mundo.mundo.addMap(
                    Mapa(
                        rs.getShort("id"),
                        rs.getString("date"),
                        rs.getByte("width"),
                        rs.getByte("heigth"),
                        rs.getString("key"),
                        rs.getString("places"),
                        rs.getString("mapData"),
                        rs.getString("monsters"),
                        rs.getString("mappos"),
                        rs.getByte("numgroup"),
                        rs.getByte("fixSize"),
                        rs.getByte("minSize"),
                        rs.getByte("maxSize"),
                        rs.getString("forbidden"),
                        rs.getByte("sniffed")
                    )
                )
            }
            close(result)
            result = getData("SELECT * FROM mobgroups_fix")
            rs = result!!.resultSet
            while (rs!!.next()) {
                val c = Mundo.mundo.getMap(rs.getShort("mapid")) ?: continue
                if (c.getCase(rs.getInt("cellid")) == null) continue
                c.addStaticGroup(rs.getInt("cellid"), rs.getString("groupData"), false)
                Mundo.mundo.addGroupFix(
                    rs.getInt("mapid").toString() + ";" + rs.getInt("cellid"),
                    rs.getString("groupData"),
                    rs.getInt("Timer")
                )
            }
        } catch (e: SQLException) {
            super.sendError("MapData load", e)
        } finally {
            close(result)
        }
    }

    fun reload() {
        var result: Result? = null
        try {
            result = getData(
                "SELECT * FROM maps LIMIT "
                        + Constantes.LIMITE_DE_MAPAS
            )
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val map = Mundo.mundo.getMap(rs.getShort("id"))
                if (map == null) {
                    Mundo.mundo.addMap(
                        Mapa(
                            rs.getShort("id"),
                            rs.getString("date"),
                            rs.getByte("width"),
                            rs.getByte("heigth"),
                            rs.getString("key"),
                            rs.getString("places"),
                            rs.getString("mapData"),
                            rs.getString("monsters"),
                            rs.getString("mappos"),
                            rs.getByte("numgroup"),
                            rs.getByte("fixSize"),
                            rs.getByte("minSize"),
                            rs.getByte("maxSize"),
                            rs.getString("forbidden"),
                            rs.getByte("sniffed")
                        )
                    )
                    continue
                }
                map.setInfos(
                    rs.getString("date"),
                    rs.getString("monsters"),
                    rs.getString("mappos"),
                    rs.getByte("numgroup"),
                    rs.getByte("fixSize"),
                    rs.getByte("minSize"),
                    rs.getByte("maxSize"),
                    rs.getString("forbidden")
                )
            }
        } catch (e: SQLException) {
            super.sendError("MapData reload", e)
        } finally {
            close(result)
        }
    }
}