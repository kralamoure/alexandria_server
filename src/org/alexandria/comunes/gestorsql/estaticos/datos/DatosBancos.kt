package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.cliente.Cuenta
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosBancos(dataSource: HikariDataSource?) :
    AbstractDAO<Any>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Any): Boolean {
        return false
    }

    fun add(guid: Int) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("INSERT INTO banks(`id`, `kamas`, `items`) VALUES (?, 0, '')")
            p!!.setInt(1, guid)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("BankData add", e)
        } finally {
            close(p)
        }
    }

    fun update(acc: Cuenta) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `banks` SET `kamas` = ?, `items` = ? WHERE `id` = ?")
            p!!.setLong(1, acc.bankKamas)
            p.setString(2, acc.pasar_objeto_al_banco())
            p.setInt(3, acc.id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("BankData update", e)
        } finally {
            close(p)
        }
    }

    operator fun get(guid: Int): String? {
        var get: String? = null
        var result: Result? = null
        try {
            result = getData("SELECT * FROM `banks` WHERE id = '$guid'")
            val rs = result!!.resultSet
            if (rs!!.next()) {
                get = rs.getInt("kamas").toString() + "@" + rs.getString("items")
            }
        } catch (e: SQLException) {
            super.sendError("BankData getWaitingAccount", e)
        } finally {
            super.close(result)
        }
        return get
    }
}