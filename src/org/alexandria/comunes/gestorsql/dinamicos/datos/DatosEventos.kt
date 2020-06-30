package org.alexandria.comunes.gestorsql.dinamicos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.cliente.Cuenta
import org.alexandria.comunes.gestorsql.dinamicos.AbstractDAO
import org.alexandria.estaticos.evento.RecompensaEvento
import org.alexandria.estaticos.evento.tipo.Evento
import org.alexandria.estaticos.evento.tipo.EventoEncontrarme.FindMeRow
import org.alexandria.estaticos.evento.tipo.EventoSonreir
import java.sql.ResultSet
import java.sql.SQLException

class DatosEventos(dataSource: HikariDataSource?) :
    AbstractDAO<Cuenta?>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Cuenta?): Boolean {
        return false
    }

    fun load(): Array<Evento?> {
        var result: Result? = null
        val events = arrayOfNulls<Evento>(numberOfEvent.toInt())
        try {
            result = getData("SELECT * FROM `world.event.type`;")
            if (result != null) {
                val rs = result.resultSet
                var i: Byte = 0
                while (rs!!.next()) {
                    val event = getEventById(rs.getByte("id"), rs)
                    if (event != null) {
                        events[i.toInt()] = event
                        i++
                    }
                }
            }
        } catch (e: SQLException) {
            super.sendError("EventData load", e)
        } finally {
            close(result)
        }
        return events
    }

    private val numberOfEvent: Byte
        get() {
            var result: Result? = null
            var numbers: Byte = 0
            try {
                result = getData("SELECT COUNT(id) AS numbers FROM `world.event.type`;")
                if (result != null) {
                    val rs = result.resultSet
                    rs!!.next()
                    numbers = rs.getByte("numbers")
                }
            } catch (e: SQLException) {
                super.sendError("EventData getNumberOfEvent", e)
            } finally {
                close(result)
            }
            return numbers
        }

    private fun loadFindMeRow(): Byte {
        var result: Result? = null
        var numbers: Byte = 0
        try {
            result = getData("SELECT COUNT(id) AS numbers FROM `world.event.findme`;")
            if (result != null) {
                val rs = result.resultSet
                while (rs!!.next()) {
                    val row = FindMeRow(
                        rs.getShort("map"),
                        rs.getShort("cell"),
                        rs.getString("indices").split("\\|".toRegex()).toTypedArray()
                    )
                }
                numbers = rs.getByte("numbers")
            }
        } catch (e: SQLException) {
            super.sendError("EventData getNumberOfEvent", e)
        } finally {
            close(result)
        }
        return numbers
    }

    @Throws(SQLException::class)
    private fun getEventById(id: Byte, result: ResultSet?): Evento? {
        return when (id) {
            1.toByte() -> EventoSonreir(
                id,
                result!!.getByte("maxPlayers"),
                result.getString("name"),
                result.getString("description"),
                RecompensaEvento.parse(result.getString("firstWinner"))
            )
            else -> null
        }
    }
}