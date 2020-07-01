package org.alexandria.estaticos.evento.tipo

import org.alexandria.estaticos.area.mapa.Mapa.GameCase
import org.alexandria.estaticos.cliente.Jugador
import org.alexandria.estaticos.evento.GestorEvento
import org.alexandria.estaticos.evento.RecompensaEvento
import java.util.*

class EventoEncontrarme(
    id: Byte,
    maxPlayers: Byte,
    name: String?,
    description: String?,
    first: Array<RecompensaEvento>
) : Evento(id, maxPlayers, name!!, description, first) {
    override fun prepare() {
        val animator = map!!.addNpc(16000, 221, 1)
    }

    override fun perform() {}
    override fun execute() {}
    override fun close() {}

    @Throws(Exception::class)
    override fun onReceivePacket(manager: GestorEvento?, player: Jugador?, packet: String?): Boolean {
        return false
    }

    override fun getEmptyCellForPlayer(player: Jugador?): GameCase? {
        return null
    }

    override fun kickPlayer(player: Jugador?) {}
    class FindMeRow(val map: Short, val cell: Short, private val indices: Array<String>) {
        private var actual: Byte = 0

        val nextIndice: String?
            get() {
                if (actual > this.indices.size - 1) return null
                val indice = this.indices[actual.toInt()]
                actual++
                return indice
            }

        init {
            findMeRows.add(this)
        }
    }

    companion object {
        private val findMeRows: MutableList<FindMeRow> = ArrayList()
    }
}