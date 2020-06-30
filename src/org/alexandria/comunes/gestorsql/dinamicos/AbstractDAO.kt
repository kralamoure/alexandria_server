package org.alexandria.comunes.gestorsql.dinamicos

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.DAO
import org.alexandria.comunes.gestorsql.Database
import org.alexandria.configuracion.MainServidor.stop
import org.slf4j.LoggerFactory
import java.sql.*

abstract class AbstractDAO<T>(protected var dataSource: HikariDataSource) : DAO<T> {
    private val locker = Any()
    @JvmField
    protected var logger =
        LoggerFactory.getLogger(AbstractDAO::class.java.toString() + " - [S]") as Logger

    protected fun execute(query: String) {
        synchronized(locker) {
            var connection: Connection? = null
            var statement: Statement? = null
            try {
                connection = dataSource.connection
                statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)
                statement.execute(query)
                logger.debug("SQL request executed successfully {}", query)
            } catch (e: SQLException) {
                logger.error("Can't execute SQL Request :$query", e)
            } finally {
                close(statement)
                close(connection)
            }
        }
    }

    protected fun execute(statement: PreparedStatement) {
        synchronized(locker) {
            var connection: Connection? = null
            try {
                connection = statement.connection
                statement.execute()
                logger.debug("SQL request executed successfully {}", statement.toString())
            } catch (e: SQLException) {
                e.printStackTrace()
                logger.error("Can't execute SQL Request :$statement", e)
            } finally {
                close(statement)
                close(connection)
            }
        }
    }

    protected fun getData(query: String): Result? {
        var query = query
        synchronized(locker) {
            var connection: Connection? = null
            try {
                if (!query.endsWith(";")) query = "$query;"
                connection = dataSource.connection
                val statement =
                    connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)
                val result =
                    Result(
                        connection,
                        statement.executeQuery(query)
                    )
                logger.debug("SQL request executed successfully {}", query)
                return result
            } catch (e: SQLException) {
                logger.error("Can't execute SQL Request :$query", e)
            }
            return null
        }
    }

    @Throws(SQLException::class)
    protected fun getPreparedStatement(query: String?): PreparedStatement? {
        return try {
            val connection = dataSource.connection
            connection.prepareStatement(query)
        } catch (e: SQLException) {
            e.printStackTrace()
            logger.error("Can't getWaitingAccount datasource connection", e)
            dataSource.close()
            if (Database.dinamicos.inicializarconexion()) stop("statics prepared statement failed")
            null
        }
    }

    protected fun close(statement: PreparedStatement?) {
        if (statement == null) return
        try {
            if (!statement.isClosed) {
                statement.clearParameters()
                statement.close()
            }
        } catch (e: Exception) {
            logger.error("Can't stop statement", e)
        }
    }

    protected fun close(connection: Connection?) {
        if (connection == null) return
        try {
            if (!connection.isClosed) {
                connection.close()
                logger.trace("{} released", connection)
            }
        } catch (e: Exception) {
            logger.error("Can't stop connection", e)
        }
    }

    protected fun close(statement: Statement?) {
        if (statement == null) return
        try {
            if (!statement.isClosed) statement.close()
        } catch (e: Exception) {
            logger.error("Can't stop statement", e)
        }
    }

    protected fun close(resultSet: ResultSet?) {
        if (resultSet == null) return
        try {
            if (!resultSet.isClosed) resultSet.close()
        } catch (e: Exception) {
            logger.error("Can't stop resultSet", e)
        }
    }

    protected fun close(result: Result?) {
        if (result != null) {
            if (result.resultSet != null) close(result.resultSet)
            if (result.connection != null) close(result.connection)
            logger.trace("Connection {} has been released", result.connection)
        }
    }

    protected fun sendError(msg: String, e: Exception) {
        e.printStackTrace()
        logger.error("Error statics database " + msg + " : " + e.message)
    }

    protected class Result(val connection: Connection?, val resultSet: ResultSet?)

    init {
        logger.level = Level.OFF
    }
}