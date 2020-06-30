package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.Database
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.Mercadillo.MercadilloEntrada
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosObjetosMercadillos(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `hdvs_items`")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val tempHdv = Mundo.mundo.getHdv(rs.getInt("map")) ?: continue
                if (Mundo.getGameObject(rs.getInt("itemID")) == null) {
                    Database.estaticos.hdvObjectData!!.delete(rs.getInt("id"))
                    continue
                }
                tempHdv.addEntry(
                    MercadilloEntrada(
                        rs.getInt("id"),
                        rs.getInt("price"),
                        rs.getByte("count"),
                        rs.getInt("ownerGuid"),
                        Mundo.getGameObject(rs.getInt("itemID"))
                    ), true
                )
                Mundo.mundo.nextObjectHdvId = rs.getInt("id")
            }
        } catch (e: SQLException) {
            super.sendError("Hdvs_itemsData load", e)
        } finally {
            close(result)
        }
    }

    fun add(toAdd: MercadilloEntrada) {
        var p: PreparedStatement? = null
        try {
            p =
                getPreparedStatement("INSERT INTO `hdvs_items` (`map`,`ownerGuid`,`price`,`count`,`itemID`) VALUES(?,?,?,?,?)")
            p!!.setInt(1, toAdd.hdvId)
            p.setInt(2, toAdd.owner)
            p.setInt(3, toAdd.price)
            p.setInt(4, toAdd.getAmount(false).toInt())
            p.setInt(5, toAdd.gameObject.id)
            execute(p)
            Database.estaticos.objectTemplateData!!.saveAvgprice(toAdd.gameObject.modelo)
        } catch (e: SQLException) {
            super.sendError("Hdvs_itemsData add", e)
        } finally {
            close(p)
        }
    }

    fun delete(id: Int) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("DELETE FROM hdvs_items WHERE itemID = ?")
            p!!.setInt(1, id)
            execute(p)
            if (Mundo.getGameObject(id) != null) Database.estaticos.objectTemplateData!!.saveAvgprice(
                Mundo.getGameObject(
                    id
                ).modelo
            )
        } catch (e: SQLException) {
            super.sendError("Hdvs_itemsData delete", e)
        } finally {
            close(p)
        }
    }
}