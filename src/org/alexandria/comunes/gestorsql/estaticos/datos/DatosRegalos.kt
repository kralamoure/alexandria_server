package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosRegalos(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any): Boolean {
        return false
    }

    fun create(guid: Int) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement(
                "INSERT INTO gifts(`id`, `objects`) VALUES ('"
                        + guid + "', '');"
            )
            execute(p!!)
        } catch (e: SQLException) {
            super.sendError("GiftData create", e)
        } finally {
            close(p)
        }
    }

    fun existByAccount(guid: Int): Boolean {
        var exist = false
        var result: Result? = null
        try {
            result = getData(
                "SELECT * FROM gifts WHERE id = '" + guid
                        + "'"
            )
            val rs = result!!.resultSet
            if (rs!!.next()) {
                exist = rs.getInt("id") > 0
            }
        } catch (e: SQLException) {
            super.sendError("GiftData existByAccount", e)
        } finally {
            super.close(result)
        }
        return exist
    }

    fun getByAccount(guid: Int): String? {
        var result: Result? = null
        var gift: String? = null
        try {
            result = getData(
                "SELECT * FROM gifts WHERE id = '" + guid
                        + "';"
            )
            val rs = result!!.resultSet
            if (rs!!.next()) {
                gift = rs.getString("objects")
            }
        } catch (e: SQLException) {
            super.sendError("GiftData getByAccount", e)
        } finally {
            super.close(result)
        }
        return gift
    }

    fun update(acc: Int, objects: String?) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `gifts` SET `objects` = ? WHERE `id` = ?")
            p!!.setString(1, objects)
            p.setInt(2, acc)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("GiftData update", e)
        } finally {
            close(p)
        }
    }
}