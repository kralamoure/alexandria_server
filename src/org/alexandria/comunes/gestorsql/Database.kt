package org.alexandria.comunes.gestorsql

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.dinamicos.Dinamicos
import org.alexandria.comunes.gestorsql.estaticos.Estaticos

object Database {
    @JvmField
    val estaticos = Estaticos()
    @JvmField
    val dinamicos = Dinamicos()
    fun launchDatabase(): Boolean {
        return !(dinamicos.inicializarconexion() || estaticos.inicializarconexion())
    }

    @JvmStatic
    fun tryConnection(dataSource: HikariDataSource): Boolean {
        return try {
            val connection = dataSource.connection
            connection.close()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }
}