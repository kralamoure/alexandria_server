package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.estaticos.objeto.ObjetoModelo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosObjetoModelo(dataSource: HikariDataSource?) :
    AbstractDAO<ObjetoModelo>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: ObjetoModelo): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM item_template;")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                if (Mundo.mundo.getObjetoModelo(rs.getInt("id")) != null) {
                    Mundo.mundo.getObjetoModelo(rs.getInt("id")).setInfos(
                        rs.getString("statsTemplate"),
                        rs.getString("name"),
                        rs.getInt("type"),
                        rs.getInt("level"),
                        rs.getInt("pod"),
                        rs.getInt("prix"),
                        rs.getInt("panoplie"),
                        rs.getString("conditions"),
                        rs.getString("armesInfos"),
                        rs.getInt("sold"),
                        rs.getInt("avgPrice"),
                        rs.getInt("points"),
                        rs.getInt("newPrice")
                    )
                } else {
                    Mundo.mundo.addObjTemplate(
                        ObjetoModelo(
                            rs.getInt("id"),
                            rs.getString("statsTemplate"),
                            rs.getString("name"),
                            rs.getInt("type"),
                            rs.getInt("level"),
                            rs.getInt("pod"),
                            rs.getInt("prix"),
                            rs.getInt("panoplie"),
                            rs.getString("conditions"),
                            rs.getString("armesInfos"),
                            rs.getInt("sold"),
                            rs.getInt("avgPrice"),
                            rs.getInt("points"),
                            rs.getInt("newPrice")
                        )
                    )
                }
            }
        } catch (e: SQLException) {
            super.sendError("Item_templateData load", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }

    fun saveAvgprice(template: ObjetoModelo?) {
        if (template == null) return
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `item_template` SET sold = ?,avgPrice = ? WHERE id = ?")
            p!!.setLong(1, template.sold)
            p.setInt(2, template.avgPrice)
            p.setInt(3, template.id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("Item_templateData saveAvgprice", e)
        } finally {
            close(p)
        }
    }
}