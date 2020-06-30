package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.estaticos.Prisma
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException

class DatosPrismas(dataSource: HikariDataSource?) :
    AbstractDAO<Prisma>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Prisma): Boolean {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE prismes SET `level` = ?, `honor` = ?, `area`= ? WHERE `id` = ?")
            p!!.setInt(1, obj.level)
            p.setInt(2, obj.honor)
            p.setInt(3, obj.conquestArea)
            p.setInt(4, obj.id)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("PrismeData update", e)
        } finally {
            close(p)
        }
        return false
    }

    fun load() {
        var result: Result? = null
        var numero = 0
        try {
            result = getData("SELECT * from prismes")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                Mundo.mundo.addPrisme(
                    Prisma(
                        rs.getInt("id"),
                        rs.getInt("alignement"),
                        rs.getInt("level"),
                        rs.getShort("carte"),
                        rs.getInt("celda"),
                        rs.getInt("honor"),
                        rs.getInt("area")
                    )
                )
                numero++
            }
        } catch (e: SQLException) {
            super.sendError("PrismeData load", e)
        } finally {
            close(result)
        }
    }

    fun add(Prisme: Prisma) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("REPLACE INTO `prismes` VALUES(?,?,?,?,?,?,?)")
            p!!.setInt(1, Prisme.id)
            p.setInt(2, Prisme.alignement)
            p.setInt(3, Prisme.level)
            p.setInt(4, Prisme.map.toInt())
            p.setInt(5, Prisme.cell)
            p.setInt(6, Prisme.conquestArea)
            p.setInt(7, Prisme.honor)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("PrismeData add", e)
        } finally {
            close(p)
        }
    }

    fun delete(id: Int) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("DELETE FROM prismes WHERE id = ?")
            p!!.setInt(1, id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("PrismeData delete", e)
        } finally {
            close(p)
        }
    }
}