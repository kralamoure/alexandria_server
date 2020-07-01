package org.alexandria.estaticos.evento.tipo

import org.alexandria.comunes.Camino
import org.alexandria.comunes.Formulas
import org.alexandria.comunes.GestorSalida
import org.alexandria.estaticos.Npc
import org.alexandria.estaticos.area.mapa.Mapa.GameCase
import org.alexandria.estaticos.cliente.Jugador
import org.alexandria.estaticos.evento.GestorEvento
import org.alexandria.estaticos.evento.RecompensaEvento
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.otro.utilidad.Doble
import org.alexandria.otro.utilidad.Temporizador
import org.alexandria.otro.utilidad.Temporizador.Companion.addSiguiente
import java.util.*
import java.util.concurrent.TimeUnit

class EventoSonreir(
    id: Byte,
    maxPlayers: Byte,
    name: String?,
    description: String?,
    first: Array<RecompensaEvento>
) : Evento(id, maxPlayers, name!!, description, first) {
    private val emotes: MutableList<Byte> = ArrayList()
    private val answers: MutableList<Doble<Jugador, MutableList<Byte>>> =
        ArrayList()
    private var state: Byte = 0
    private var count: Byte = 0
    private val cells = shortArrayOf(239, 253, 225, 267, 211, 281, 197, 295, 183, 309, 169)
    private var animator: Npc? = null
    override fun prepare() {
        answers.clear()
        emotes.clear()
        count = 0
        state = count
        animator = map!!.addNpc(GestorEvento.NPC, 221, 1)
        if (map!!.players.isNotEmpty()) {
            GestorSalida.GAME_SEND_ADD_NPC_TO_MAP(map, animator)
        }
        addSiguiente(Runnable {
            var ok = true
            while (GestorEvento.instance.state == GestorEvento.State.INITIALIZE || GestorEvento.instance
                    .state == GestorEvento.State.PROCESSED
            ) {
                moveAnimatorToCellId(if (ok) 137 else 221)
                wait(2500)
                ok = !ok
            }
        }, 0, TimeUnit.SECONDS, Temporizador.DataType.CLIENTE)
    }

    override fun perform() {
        moveAnimatorToCellId(179)
        wait(1500)
        animator!!.setOrientation(1.toByte())
        GestorSalida.GAME_SEND_eD_PACKET_TO_MAP(map, animator!!.id, 1)
        wait(1000)
        GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(
            map,
            "",
            animator!!.id,
            "Event",
            "Bonjour à tous et bienvenue à l'évent Smiley !"
        )
        wait(3000)
        GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(
            map,
            "",
            animator!!.id,
            "Event",
            "Avant de commencer, laissez moi vous expliquer les règles du jeu."
        )
        wait(4000)
        GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(
            map,
            "",
            animator!!.id,
            "Event",
            "L'objectif est de reproduire les smileys que j'utiliserai."
        )
        wait(5000)
        GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(
            map,
            "",
            animator!!.id,
            "Event",
            "Par exemple, si j'utilise ce smiley :"
        )
        wait(1500)
        GestorSalida.GAME_SEND_EMOTICONE_TO_MAP(map, animator!!.id, 10)
        wait(2000)
        GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(
            map,
            "",
            animator!!.id,
            "Event",
            "Vous devrez utiliser ce smiley aussi."
        )
        wait(3500)
        GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(
            map,
            "",
            animator!!.id,
            "Event",
            "Attention, si vous vous trompez de smiley, vous serez éliminé(e)."
        )
        wait(4000)
        GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(
            map,
            "",
            animator!!.id,
            "Event",
            "N'oubliez pas d'attendre que le chronomètre démarre avant d'utiliser un smiley."
        )
        wait(5500)
        GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(
            map,
            "",
            animator!!.id,
            "Event",
            "Vous êtes prêt(e)s ? C'est parti !"
        )
        execute()
    }

    override fun execute() {
        count = 0
        val participants: List<Jugador> =
            ArrayList(GestorEvento.instance.getParticipants())
        var nbPlayers = participants.size
        for (player in participants) if (player != null && player.isOnline) answers.add(
            Doble(
                player,
                ArrayList()
            )
        )

        //239 cell emote pnj, 179 cell non emote
        while (nbPlayers > 1) {
            count++
            moveAnimatorToCellId(134)
            wait(2000)
            emotes.add((Formulas.random.nextInt(14) + 1).toByte())
            for (e in emotes) {
                GestorSalida.GAME_SEND_EMOTICONE_TO_MAP(map, animator!!.id, e.toInt())
                wait(1500 - 100)
            }
            wait(1500)
            moveAnimatorToCellId(179)
            wait(1500)
            GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(map, "", animator!!.id, "Event", "Faites vos jeux !")
            wait(750)
            initializeTurn((3000 + 1000 * count).toShort())
            wait(1500 + 650 * count)
            GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(map, "", animator!!.id, "Event", "Rien ne va plus !")
            wait(1500 + 650 * count)
            GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(
                map,
                "",
                animator!!.id,
                "Event",
                "Les jeux sont faits !"
            )
            state = 0
            for (pair in ArrayList(
                answers
            )) {
                if (pair.getSegundo().size == count.toInt()) {
                    var c: Byte = 0
                    var kick = false
                    for (b1 in pair.getSegundo()) {
                        val b2 = emotes[c.toInt()]
                        if (b2 == null) {
                            kick = true
                            break
                        } else if (b1 != b2) {
                            kick = true
                            break
                        }
                        c++
                    }
                    if (kick) {
                        kickPlayer(pair.getPrimero())
                        nbPlayers--
                    } else {
                        pair.getSegundo().clear()
                        pair.getPrimero().sendMessage("(<b>Infos</b>) : Bien joué camarade !")
                    }
                } else {
                    kickPlayer(pair.getPrimero())
                    nbPlayers--
                }
            }
            wait(1000)
            if (GestorEvento.instance.getParticipants().size > 1) {
                GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(
                    map,
                    "",
                    animator!!.id,
                    "Event",
                    "C'est parti pour le " + (count + 1) + "éme tours !"
                )
            }
            wait(2000)
        }
        close()
    }

    override fun close() {
        if (GestorEvento.instance.getParticipants().isNotEmpty()) {
            val winner = GestorEvento.instance.getParticipants()[0]
            GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(
                map,
                "",
                animator!!.id,
                "Event",
                "Félicitations à " + winner.name + " pour ça victoire !"
            )
            winner.sendMessage("(<b>Infos</b>) : Vous venez de remporter 1 jeton !")
            val template = Mundo.mundo.getObjetoModelo(GestorEvento.TOKEN)
            if (template != null) {
                val `object` = template.createNewItem(1, false)
                if (`object` != null && winner.addObjet(`object`, true)) {
                    Mundo.addGameObject(`object`, true)
                }
            }
        } else {
            GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(
                map,
                "",
                animator!!.id,
                "Event",
                "Personne n'a gagnez, une autre fois.. peut être !"
            )
        }
        wait(2000)
        moveAnimatorToCellId(344)
        wait(2500)
        map!!.removeNpcOrMobGroup(animator!!.id)
        map!!.send("GM|-" + animator!!.id)
        map!!.send("GV")
        GestorEvento.instance.finishCurrentEvent()
    }

    override fun getEmptyCellForPlayer(player: Jugador?): GameCase? {
        return map!!.getCase(cells[count++.toInt()].toInt())
    }

    override fun kickPlayer(player: Jugador?) {
        GestorEvento.instance.getParticipants().remove(player)
        val iterator =
            answers.iterator()
        while (iterator.hasNext()) {
            val pair = iterator.next()
            if (pair.getPrimero().id == player!!.id) {
                map!!.send("GA;208;" + player.id + ";" + player.curCell.id + ",2916,11,8,1")
                player.sendMessage("(<b>Infos</b>) : Vous avez perdu.. Peut-être une autre fois !")
                player.teleportOldMap()
                player.blockMovement = false
                iterator.remove()
                break
            }
        }
    }

    @Throws(Exception::class)
    override fun onReceivePacket(manager: GestorEvento?, player: Jugador?, packet: String?): Boolean {
        if (packet?.startsWith("BS")!! && state.toInt() == 1) {
            val emote = packet.substring(2).toByte()
            for (pair in answers) {
                if (pair.getPrimero().id == player?.id) {
                    pair.getSegundo().add(emote)
                    if (pair.getSegundo().size == count.toInt()) player.sendMessage("(<b>Infos</b>) : Le compte est bon !")
                    break
                }
            }
        }
        return false
    }

    private fun initializeTurn(time: Short) {
        state = 1
        for (player in GestorEvento.instance.getParticipants()) {
            player.send("GTS" + player.id + "|" + time)
        }
    }

    private fun moveAnimatorToCellId(cellId: Int) {
        val path: String
        path = try {
            Camino.getShortestStringPathBetween(map, animator!!.cellId, cellId, 20)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        if (path != null) {
            animator!!.cellId = cellId
            GestorSalida.GAME_SEND_GA_PACKET_TO_MAP(map, "0", 1, animator!!.id.toString(), path)
        }
    }

    init {
        map = Mundo.mundo.getMap(9862.toShort())
    }
}