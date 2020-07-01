package org.alexandria.estaticos.evento

import org.alexandria.comunes.Formulas
import org.alexandria.comunes.GestorSalida
import org.alexandria.comunes.gestorsql.Database
import org.alexandria.configuracion.Configuracion.AUTO_EVENT
import org.alexandria.configuracion.Configuracion.TIME_PER_EVENT
import org.alexandria.estaticos.cliente.Jugador
import org.alexandria.estaticos.evento.tipo.Evento
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.estaticos.juego.planificador.Updatable
import org.alexandria.otro.utilidad.Temporizador
import org.alexandria.otro.utilidad.Temporizador.Companion.addSiguiente
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class GestorEvento private constructor(private val events: Array<Evento?>) : Updatable(60000) {
    enum class State {
        WAITING, INITIALIZE, PROCESSED, STARTED, FINISHED
    }

    var state =
        State.WAITING
        private set
    var currentEvent: Evento? = null
        private set
    private var lastest: Evento? = null
    private var count: Short = 0
    private val participants: MutableList<Jugador> = ArrayList()

    fun getParticipants(): MutableList<Jugador> {
        return participants
    }

    private fun startNewEvent() {
        val event = events[Formulas.random.nextInt(events.size)]
        if (event != null) {
            if (events.size > 1 && lastest != null && event.eventId == lastest!!.eventId) {
                startNewEvent()
                return
            }
            event.prepare()
            lastTime = Instant.now().toEpochMilli()
            currentEvent = event
            state = State.PROCESSED
            //Mundo.mundo.sendMessageToAll("(<b>Infos</b>) : L'événement '<b>" + event.getEventName() + "</b>' vient de démarrer, <b>.event</b> pour vous inscrire.");
        } else {
            startNewEvent()
        }
    }

    @Synchronized
    private fun startCurrentEvent() {
        if (state == State.STARTED) return
        state = State.STARTED
        if (!hasEnoughPlayers()) {
            count = 0
            lastTime = Instant.now().toEpochMilli()
            state = State.PROCESSED
        } else if (moveAllPlayersToEventMap(true)) {
            lastTime = Instant.now().toEpochMilli()
            addSiguiente(
                Runnable { currentEvent!!.perform() },
                0,
                TimeUnit.SECONDS,
                Temporizador.DataType.CLIENTE
            )
        }
    }

    fun finishCurrentEvent() {
        participants.stream()
            .filter { obj: Jugador? -> Objects.nonNull(obj) }.forEach { player: Jugador ->
                player.teleportOldMap()
                player.blockMovement = false
            }
        currentEvent!!.interrupt()
        lastest = currentEvent
        currentEvent = null
        lastTime = Instant.now().toEpochMilli()
        count = 0
        state = State.WAITING
    }

    @Synchronized
    fun subscribe(player: Jugador): Byte {
        if (currentEvent == null || state == State.WAITING) {
            return 0
        } else {
            if (state == State.PROCESSED) {
                for (p in getParticipants()) {
                    if (player.account != null && p != null && p.account != null) {
                        if (player.account.currentIp.compareTo(p.account.currentIp) == 0) {
                            GestorSalida.GAME_SEND_MESSAGE(
                                player,
                                "Impossible de rejoindre ce combat, vous êtes déjà dans le combat avec une même IP !"
                            )
                            return 1
                        }
                    }
                }
                if (participants.size >= currentEvent!!.maxPlayers) {
                    player.sendMessage("(<b>Infos</b>) : L'événement '<b>" + currentEvent!!.eventName + "</b>' est déjà au complet.")
                } else if (participants.contains(player)) {
                    participants.remove(player)
                    player.sendMessage("(<b>Infos</b>) : Vous venez de vous désinscrire de l'événement '<b>" + currentEvent!!.eventName + "</b>'.")
                } else if (hasSameIP(player)) {
                    player.sendMessage("(<b>Infos</b>) : Vous avez déjà un membre de votre réseaux internet en jeu sur l'événement.")
                } else if (player.party != null && player.party.master != null) {
                    player.sendMessage("(<b>Infos</b>) : Vous ne pouvez pas rejoindre un événement en étant en mode maître.")
                } else {
                    participants.add(player)
                    player.sendMessage("(<b>Infos</b>) : Vous venez de vous inscrire à l'événement '<b>" + currentEvent!!.eventName + "</b>'.")
                    if (participants.size >= currentEvent!!.maxPlayers) {
                        startCurrentEvent()
                    } else {
                        participants.forEach(Consumer { target: Jugador ->
                            target.sendMessage(
                                "(<b>Infos</b>) : En attente de " +
                                        (currentEvent!!.maxPlayers - participants.size) + " joueur(s)."
                            )
                        })
                    }
                }
            } else {
                player.sendMessage("(<b>Infos</b>) : L'événement '<b>" + currentEvent!!.eventName + "</b>' a déjà démarrer.")
            }
        }
        return 1
    }

    private fun hasSameIP(player: Jugador?): Boolean {
        if (player != null && player.account != null) {
            val ip = player.account.currentIp
            if (ip == "127.0.0.1") return false
            for (target in participants) {
                if (target != null && target.account != null) {
                    return ip == target.account.currentIp
                }
            }
        }
        return false
    }

    private fun hasEnoughPlayers(): Boolean {
        if (currentEvent == null) return false
        val percent = (100 * participants.size / currentEvent!!.maxPlayers).toShort()
        return percent >= 30
    }

    override fun update() {
        if (AUTO_EVENT && verify()) {
            if (state == State.WAITING) {
                val result = (TIME_PER_EVENT - ++count).toShort()
                if (result.toInt() == 0) {
                    count = 0
                    lastTime = Instant.now().toEpochMilli()
                    state = State.INITIALIZE
                    addSiguiente(
                        Runnable { startNewEvent() },
                        0,
                        TimeUnit.SECONDS,
                        Temporizador.DataType.CLIENTE
                    )
                } else if (result.toInt() == 60 || result.toInt() == 30 || result.toInt() == 15 || result.toInt() == 5) {
                    //Mundo.world.sendMessageToAll("(<b>Infos</b>) : Un <b>événement</b> va démarrer dans " + result + " minutes.");
                }
            } else if (state == State.PROCESSED) {
                val result = ((if (hasEnoughPlayers()) 5 else 10) - ++count).toShort()
                moveAllPlayersToEventMap(false)
                if (result <= 0) {
                    startCurrentEvent()
                } else if (result.toInt() == 1 && hasEnoughPlayers()) {
                    for (player in participants) {
                        player.sendMessage("(<b>Infos</b>) : L'événement va commencer dans 1 minute.")
                    }
                }
            }
        }
    }

    override fun get(): Any? {
        return lastTime
    }

    private fun moveAllPlayersToEventMap(teleport: Boolean): Boolean {
        var ok = true
        val afk = if (teleport) StringBuilder("") else null
        for (player in participants) {
            if (player.pelea != null || !player.isOnline || player.isGhost || player.doAction) {
                ok = false
                participants.remove(player)
                player.sendMessage("La prochaine fois tâchez d'être disponible !")
                player.sendMessage("(<b>Infos</b>) : Vous venez d'être expulsé du jeu pour indisponibilité.")
                if (teleport) {
                    afk!!.append(if (afk.isEmpty()) "<b>" + player.name + "</b>" else ", <b>" + player.name + "</b>")
                }
            }
        }
        if (!ok || !teleport) {
            if (teleport) {
                participants.forEach(Consumer { player: Jugador -> player.sendMessage("(<b>Infos</b> : Merci à " + afk.toString() + " expulsé pour inactivité.") })
                Mundo.mundo.onlinePlayers.stream()
                    .filter { target: Jugador ->
                        !afk.toString().contains(target.name)
                    }
                    .forEach { target: Jugador -> target.sendMessage("(<b>Infos</b> : Il vous reste 30 secondes pour vous inscrire à l'événement '<b>" + currentEvent!!.eventName + "</b>' (<b>.event</b>).") }
            }
            return false
        }
        for (player in participants) {
            if (player.pelea == null && player.isOnline && !player.isGhost && !player.doAction) {
                player.setOldPosition()
                player.blockMovement = true
                player.teleport(currentEvent!!.map!!.id, currentEvent!!.getEmptyCellForPlayer(player).id)
                GestorSalida.GAME_SEND_eD_PACKET_TO_MAP(currentEvent!!.map, player.id, 4)
            } else {
                ok = false
                participants.remove(player)
                player.sendMessage("La prochaine fois tâchez d'être disponible !")
                player.sendMessage("(<b>Infos</b>) : Vous venez d'être expulsé du jeu pour indisponibilité.")
            }
        }
        return ok
    }

    companion object {
        const val TOKEN = 50007
        const val NPC = 16000
        @JvmStatic
        val instance = GestorEvento(Database.dinamicos.eventData!!.load())

        @JvmStatic
        fun isInEvent(player: Jugador): Boolean {
            if (AUTO_EVENT && instance
                    .state == State.STARTED
            ) for (target in instance
                .getParticipants()) if (target.id == player.id) return true
            return false
        }
    }

}