package org.alexandria.comunes.gestorsql.dinamicos.datos

import ch.qos.logback.classic.Level
import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.cliente.Cuenta
import org.alexandria.comunes.gestorsql.Database
import org.alexandria.comunes.gestorsql.dinamicos.AbstractDAO
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosCuentas(source: HikariDataSource?) :
    AbstractDAO<Cuenta?>(source!!) {
    override fun load(obj: Any?) {
        var result: Result? = null
        try {
            result = super.getData("SELECT * FROM accounts WHERE guid = " + obj.toString())
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val a = Mundo.mundo.getAccount(rs.getInt("guid"))
                if (a != null && a.isOnline) continue
                val c = Cuenta(
                    rs.getInt("guid"),
                    rs.getString("account").toLowerCase(),
                    rs.getString("pseudo"),
                    rs.getString("reponse"),
                    rs.getInt("banned") == 1,
                    rs.getString("lastIP"),
                    rs.getString("lastConnectionDate"),
                    rs.getString("friends"),
                    rs.getString("enemy"),
                    rs.getInt("points"),
                    rs.getLong("subscribe"),
                    rs.getLong("muteTime"),
                    rs.getString("mutePseudo"),
                    rs.getString("lastVoteIP"),
                    rs.getString("heurevote")
                )
                Mundo.mundo.addAccount(c)
                Mundo.mundo.ReassignAccountToChar(c)
            }
        } catch (e: Exception) {
            super.sendError("AccountData load id", e)
        } finally {
            close(result)
        }
    }

    fun load() {
        var result: Result? = null
        try {
            result = super.getData("SELECT * from accounts")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                if (rs.getString("pseudo").isEmpty()) continue
                val a = Cuenta(
                    rs.getInt("guid"),
                    rs.getString("account").toLowerCase(),
                    rs.getString("pseudo"),
                    rs.getString("reponse"),
                    rs.getInt("banned") == 1,
                    rs.getString("lastIP"),
                    rs.getString("lastConnectionDate"),
                    rs.getString("friends"),
                    rs.getString("enemy"),
                    rs.getInt("points"),
                    rs.getLong("subscribe"),
                    rs.getLong("muteTime"),
                    rs.getString("mutePseudo"),
                    rs.getString("lastVoteIP"),
                    rs.getString("heurevote")
                )
                Mundo.mundo.addAccount(a)
            }
        } catch (e: Exception) {
            super.sendError("AccountData load", e)
        } finally {
            close(result)
        }
    }

    fun getSubscribe(id: Int): Long {
        var subscribe: Long = 0
        var result: Result? = null
        try {
            result = super.getData("SELECT guid, subscribe FROM accounts WHERE guid = $id")
            if (result != null) {
                val rs = result.resultSet
                while (rs!!.next()) {
                    subscribe = rs.getLong("subscribe")
                }
            }
        } catch (e: Exception) {
            super.sendError("AccountData load id", e)
        } finally {
            close(result)
        }
        return subscribe
    }

    fun updateVoteAll() {
        var result: Result? = null
        var a: Cuenta? = null
        try {
            result = super.getData("SELECT guid, heurevote, lastVoteIP from accounts")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                a = Mundo.mundo.getAccount(rs.getInt("guid"))
                if (a == null) continue
                a.updateVote(rs.getString("heurevote"), rs.getString("lastVoteIP"))
            }
        } catch (e: SQLException) {
            super.sendError("AccountData updateVoteAll", e)
        } finally {
            close(result)
        }
    }

    override fun update(obj: Cuenta?): Boolean {
        var statement: PreparedStatement? = null
        try {
            if (obj != null) {
                statement = getPreparedStatement(
                    "UPDATE accounts SET banned = '"
                            + (if (obj.isBanned) 1 else 0) + "', friends = '"
                            + obj.parseFriendListToDB() + "', enemy = '"
                            + obj.parseEnemyListToDB() + "', muteTime = '"
                            + obj.muteTime + "', mutePseudo = '"
                            + obj.mutePseudo + "' WHERE guid = '" + obj.id
                            + "'"
                )
            }
            execute(statement!!)
            return true
        } catch (e: Exception) {
            super.sendError("AccountData update", e)
        } finally {
            close(statement)
        }
        return false
    }

    fun updateLastConnection(compte: Cuenta) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE accounts SET `lastIP` = ?, `lastConnectionDate` = ? WHERE `guid` = ?")
            p!!.setString(1, compte.currentIp)
            p.setString(2, compte.lastConnectionDate)
            p.setInt(3, compte.id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("AccountData updateLastConnection", e)
        } finally {
            close(p)
        }
    }

    fun setLogged(id: Int, logged: Int) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `accounts` SET `logged` = ? WHERE `guid` = ?;")
            p!!.setInt(1, logged)
            p.setInt(2, id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("AccountData setLogged", e)
        } finally {
            close(p)
        }
    }

    fun updateBannedTime(acc: Cuenta, time: Long) {
        var statement: PreparedStatement? = null
        try {
            statement = getPreparedStatement(
                "UPDATE accounts SET banned = '"
                        + (if (acc.isBanned) 1 else 0) + "', bannedTime = '"
                        + time + "' WHERE guid = '" + acc.id
                        + "'"
            )
            execute(statement!!)
        } catch (e: Exception) {
            super.sendError("AccountData update", e)
        } finally {
            close(statement)
        }
    }

    /** Points  */
    fun loadPoints(user: String): Int {
        return Database.dinamicos.accountData!!.loadPointsWithoutUsersDb(user)
    }

    fun updatePoints(id: Int, points: Int) {
        Database.dinamicos.accountData!!.updatePointsWithoutUsersDb(id, points)
    }

    private fun loadPointsWithoutUsersDb(user: String): Int {
        var result: Result? = null
        var points = 0
        try {
            result = super.getData(
                "SELECT * from accounts WHERE `account` LIKE '"
                        + user + "'"
            )
            val rs = result!!.resultSet
            if (rs!!.next()) {
                points = rs.getInt("points")
            }
        } catch (e: SQLException) {
            super.sendError("AccountData loadPoints", e)
        } finally {
            close(result)
        }
        return points
    }

    private fun updatePointsWithoutUsersDb(id: Int, points: Int) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE accounts SET `points` = ? WHERE `guid` = ?")
            p!!.setInt(1, points)
            p.setInt(2, id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("AccountData updatePoints", e)
        } finally {
            close(p)
        }
    }

    fun loadPointsWithUsersDb(account: String): Int {
        var result: Result? = null
        var points = 0
        var user = -1
        try {
            result = super.getData("SELECT account, users FROM `accounts` WHERE `account` LIKE '$account'")
            var rs = result!!.resultSet
            if (rs!!.next()) user = rs.getInt("users")
            close(result)
            if (user == -1) {
                result = super.getData("SELECT id, points FROM `users` WHERE `id` = $user;")
                rs = result!!.resultSet
                if (rs!!.next()) points = rs.getInt("users")
            }
        } catch (e: SQLException) {
            super.sendError("AccountData loadPoints", e)
        } finally {
            close(result)
        }
        return points
    }

    fun updatePointsWithUsersDb(id: Int, points: Int) {
        var p: PreparedStatement? = null
        var user = -1
        try {
            val result =
                super.getData("SELECT guid, users FROM `accounts` WHERE `guid` LIKE '$id'")
            val rs = result!!.resultSet
            if (rs!!.next()) user = rs.getInt("users")
            close(result)
            if (user != -1) {
                p = getPreparedStatement("UPDATE `users` SET `points` = ? WHERE `id` = ?;")
                p!!.setInt(1, points)
                p.setInt(2, id)
                execute(p)
            }
        } catch (e: SQLException) {
            super.sendError("AccountData updatePoints", e)
        } finally {
            close(p)
        }
    }

    init {
        logger.level = Level.ALL
    }
}