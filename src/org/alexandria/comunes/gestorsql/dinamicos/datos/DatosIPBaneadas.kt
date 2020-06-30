package org.alexandria.comunes.gestorsql.dinamicos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.dinamicos.AbstractDAO
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosIPBaneadas(dataSource: HikariDataSource?) :
    AbstractDAO<Any?>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any?): Boolean {
        return false
    }

    fun add(ip: String?): Boolean {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("INSERT INTO `banip` VALUES (?)")
            p!!.setString(1, ip)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("BanipData add", e)
        } finally {
            close(p)
        }
        return false
    }

    fun delete(ip: String?): Boolean {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("DELETE FROM `banip` WHERE `ip` = ?")
            p!!.setString(1, ip)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("BanipData delete", e)
        } finally {
            close(p)
        }
        return false
    }
}