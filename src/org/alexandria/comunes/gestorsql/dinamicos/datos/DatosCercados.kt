package org.alexandria.comunes.gestorsql.dinamicos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.Cercados
import org.alexandria.comunes.gestorsql.Database
import org.alexandria.comunes.gestorsql.dinamicos.AbstractDAO
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosCercados(dataSource: HikariDataSource?) :
    AbstractDAO<Cercados>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Cercados): Boolean {
        var p: PreparedStatement? = null
        try {
            p =
                getPreparedStatement("UPDATE `mountpark_data` SET `cellMount` =?, `cellPorte`=?, `cellEnclos`=? WHERE `mapid`=?")
            p!!.setInt(1, obj.mountcell)
            p.setInt(2, obj.door)
            p.setString(3, obj.parseStringCellObject())
            p.setInt(4, obj.map.id.toInt())
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("Mountpark_dataData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun load() {
        var nbr = 0
        var result: Result? = null
        try {
            result = getData("SELECT * from mountpark_data")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val map = Mundo.mundo.getMap(rs.getShort("mapid")) ?: continue
                val cercado = Cercados(
                    map,
                    rs.getInt("cellid"),
                    rs.getInt("size"),
                    rs.getInt("priceBase"),
                    rs.getInt("cellMount"),
                    rs.getInt("cellporte"),
                    rs.getString("cellEnclos"),
                    rs.getInt("sizeObj")
                )
                Mundo.mundo.addMountPark(cercado)
                Database.estaticos.mountParkData!!.exist(cercado)
                nbr++
            }
        } catch (e: SQLException) {
            super.sendError("Mountpark_dataData load", e)
        } finally {
            close(result)
        }
    }

    fun reload(i: Int) {
        var result: Result? = null
        try {
            result = getData("SELECT * from mountpark_data")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val map = Mundo.mundo.getMap(rs.getShort("mapid")) ?: continue
                if (rs.getShort("mapid").toInt() != i) continue
                if (!Mundo.mundo.mountPark.containsKey(rs.getShort("mapid"))) {
                    val cercado = Cercados(
                        map,
                        rs.getInt("cellid"),
                        rs.getInt("size"),
                        rs.getInt("priceBase"),
                        rs.getInt("cellMount"),
                        rs.getInt("cellporte"),
                        rs.getString("cellEnclos"),
                        rs.getInt("sizeObj")
                    )
                    Mundo.mundo.addMountPark(cercado)
                } else {
                    Mundo.mundo.mountPark[rs.getShort("mapid")]!!.setInfos(
                        map,
                        rs.getInt("cellid"),
                        rs.getInt("size"),
                        rs.getInt("cellMount"),
                        rs.getInt("cellporte"),
                        rs.getString("cellEnclos"),
                        rs.getInt("sizeObj")
                    )
                }
            }
        } catch (e: SQLException) {
            super.sendError("Mountpark_dataData reload", e)
        } finally {
            close(result)
        }
    }
}