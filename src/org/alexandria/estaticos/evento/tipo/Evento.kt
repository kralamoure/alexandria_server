package org.alexandria.estaticos.evento.tipo

import org.alexandria.estaticos.area.mapa.Mapa
import org.alexandria.estaticos.cliente.Jugador
import org.alexandria.estaticos.evento.IEvento
import org.alexandria.estaticos.evento.RecompensaEvento
import java.time.Instant

abstract class Evento(
    id: Byte,
    maxPlayers: Byte,
    name: String,
    description: String?,
    first: Array<RecompensaEvento>
) : Thread(), IEvento {
    val eventId: Byte
    val maxPlayers: Byte
    val eventName: String
    private val description: String?
    var map: Mapa? = null
        protected set
    var first: Array<RecompensaEvento>
        protected set
    lateinit var second: Array<RecompensaEvento>
        protected set
    lateinit var third: Array<RecompensaEvento>
        protected set

    constructor(
        id: Byte,
        maxPlayers: Byte,
        name: String,
        description: String?,
        first: Array<RecompensaEvento>,
        second: Array<RecompensaEvento>,
        third: Array<RecompensaEvento>
    ) : this(id, maxPlayers, name, description, first) {
        this.second = second
        this.third = third
    }

    abstract fun kickPlayer(player: Jugador?)

    companion object {
        @JvmStatic
        fun wait(time: Int) {
            val newTime = Instant.now().toEpochMilli() + time
            while (Instant.now().toEpochMilli() < newTime) {
                try {
                    sleep(50)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    init {
        super.setName("Event-$name")
        super.setDaemon(true)
        super.start()
        eventId = id
        this.maxPlayers = maxPlayers
        eventName = name
        this.description = description
        this.first = first
    }
}