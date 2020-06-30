package org.alexandria.dinamicos

import org.alexandria.comunes.Camino
import org.alexandria.comunes.GestorSalida
import org.alexandria.estaticos.Npc
import org.alexandria.estaticos.area.mapa.Mapa
import org.alexandria.estaticos.cliente.Jugador
import org.alexandria.estaticos.juego.mundo.Mundo
import java.util.*

class Inicio(private var player: Jugador?) {
    private var helper: Npc? = null
    @kotlin.jvm.JvmField
	var leave = false
    private var map: Mapa? = null
    private val mapUse: MutableMap<Int, Mapa> = HashMap()
    private val thread: Thread

    private inner class verifyIsOnline : Runnable {
        override fun run() {
            while (player!!.isOnline) try {
                Thread.sleep(250)
            } catch (ignored: Exception) {
            }
            player = null
            helper = null
            map = null
            mapUse.clear()
            thread.interrupt()
            thread.stop()
        }
    }

    private inner class starting : Runnable {
        override fun run() {
            /* START : Construction de l'nvironement **/
            try {
                mapUse[1] = Mundo.mundo.getMap(6824.toShort()).mapCopyIdentic
                mapUse[2] = Mundo.mundo.getMap(6826.toShort()).mapCopyIdentic
                mapUse[3] = Mundo.mundo.getMap(6828.toShort()).mapCopyIdentic
                mapUse[1]!!.getCase(329).addOnCellStopAction(999, "192", "-1", mapUse[2])
                mapUse[1]!!.getCase(325).addOnCellStopAction(999, "224", "-1", Mundo.mundo.getMap(1863.toShort()))
                mapUse[3]!!.getCase(192).addOnCellStopAction(999, "389", "-1", Mundo.mundo.getMap(6829.toShort()))

                /* MAP 1 : Talk & Walk to start Fight **/try {
                    Thread.sleep(2000)
                } catch (ignored: InterruptedException) {
                }
                map = mapUse[1]
                helper = map!!.addNpc(15020, 179, 3)
                player!!.setSpellsPlace(false)
                player!!.unlearnSpell(661)
                player!!.teleport(map, 224)
                player!!.blockMovement = true
                GestorSalida.GAME_SEND_ADD_NPC(player, helper)
                try {
                    Thread.sleep(5000)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Gardien Amakna",
                    "Bienvenue " + player!!.name + ", je suis le gardien d'Amakna."
                )
                try {
                    Thread.sleep(4000)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Gardien Amakna",
                    "Je vais te proposer de faire un choix..."
                )
                try {
                    Thread.sleep(4000)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Gardien Amakna",
                    "Si tu désires que l'on t'aide à faire tes premiers pas dans ce monde..."
                )
                try {
                    Thread.sleep(4000)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Gardien Amakna",
                    "... rejoins mon ami dans la salle suivante, il doit t'attendre..."
                )
                try {
                    Thread.sleep(4000)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Gardien Amakna",
                    "... pour cela, marche sur ce plot de transfert."
                )
                player!!.send("Gf-1|329")
                try {
                    Thread.sleep(4000)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Gardien Amakna",
                    "Autrement, si tu désires découvrir le monde par toi même, marche sur l'autre plot."
                )
                player!!.send("Gf-1|325")
                try {
                    Thread.sleep(4000)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Gardien Amakna",
                    "... rejoins mon ami dans la salle suivante, il doit t'attendre..."
                )
                player!!.blockMovement = false
                try {
                    Thread.sleep(3000)
                } catch (ignored: InterruptedException) {
                }
                var trying = 0
                while (player!!.curMap.id.toInt() == 6824 && !leave) {
                    try {
                        Thread.sleep(2000)
                    } catch (ignored: InterruptedException) {
                    }
                    if (trying % 10 == 1) GestorSalida.GAME_SEND_cMK_PACKET(
                        player,
                        "",
                        helper!!.id,
                        "Gardien Amakna",
                        "Il faut que tu marches sur l'un des deux plots de transfert pour passer à la suite."
                    )
                    trying++
                }
                if (leave) {
                    GestorSalida.GAME_SEND_cMK_PACKET(
                        player,
                        "",
                        helper!!.id,
                        "Gardien Amakna",
                        "Si tu es certain de ne vouloir aucune aide, clique une nouvelle fois sur le plot."
                    )
                    try {
                        Thread.sleep(3000)
                    } catch (ignored: InterruptedException) {
                    }
                    GestorSalida.GAME_SEND_cMK_PACKET(player, "", helper!!.id, "Gardien Amakna", "Bonne chance !")
                    player = null
                    helper = null
                    map = null
                    mapUse.clear()
                    thread.interrupt()
                    thread.stop()
                    return
                }
                map!!.RemoveNpc(helper!!.id)
                map = mapUse[2]
                helper = map!!.addNpc(50000, 210, 3)
                GestorSalida.GAME_SEND_ADD_NPC_TO_MAP(map, helper)
                trying = 0
                while (player!!.curMap.id.toInt() == 6824) {
                    try {
                        Thread.sleep(250)
                    } catch (ignored: InterruptedException) {
                    }
                    if (trying % 80 == 1) GestorSalida.GAME_SEND_cMK_PACKET(
                        player,
                        "",
                        helper!!.id,
                        "Gardien Amakna",
                        "Il faut que tu marches sur l'un des deux plots de transfert pour passer à la suite."
                    )
                    trying++
                }
                GestorSalida.GAME_SEND_ADD_NPC_TO_MAP(map, helper)
                player!!.blockMovement = true
                try {
                    Thread.sleep(3000)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Ganymede",
                    "Approche dans la lumière que je vois quel guerrier tu es !"
                )
                try {
                    Thread.sleep(2000)
                } catch (ignored: InterruptedException) {
                }
                var pathstr: String = try {
                    Camino.getShortestStringPathBetween(map, player!!.curCell.id, 238, 0)
                } catch (e: Exception) {
                    return
                }
                if (pathstr == null) return
                GestorSalida.GAME_SEND_GA_PACKET(
                    player!!.gameClient,
                    "0",
                    "1",
                    player!!.id.toString() + "",
                    pathstr
                )
                player!!.curCell.removePlayer(player)
                player!!.curCell = map!!.getCase(238)
                map!!.addPlayer(player)
                try {
                    Thread.sleep(3000)
                } catch (ignored: InterruptedException) {
                }
                player!!._orientation = 7
                GestorSalida.GAME_SEND_eD_PACKET_TO_MAP(map, player!!.id, 7)
                try {
                    Thread.sleep(1000)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Ganymede",
                    "Bien, voyons maintenant comment lancer un sort."
                )
                try {
                    Thread.sleep(4000)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Ganymede",
                    "Pour t'exercer, je te prête mon sort d'entrainement."
                )
                try {
                    Thread.sleep(1500)
                } catch (ignored: InterruptedException) {
                }
                player!!.learnSpell(661, 1, 'b')
                player!!.blockMovement = false
                try {
                    Thread.sleep(2500)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Ganymede",
                    "Prenons notre ami l'épouvantail ..."
                )
                try {
                    Thread.sleep(500)
                } catch (ignored: InterruptedException) {
                }
                map!!.spawnGroupOnCommand(224, "1003,1,1;", false)
                GestorSalida.GAME_SEND_MAP_MOBS_GMS_PACKETS_TO_MAP(map, player)
                try {
                    Thread.sleep(2500)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(player, "", helper!!.id, "Ganymede", "... Attaque l'épouvantail !")
                try {
                    Thread.sleep(3000)
                } catch (ignored: InterruptedException) {
                }
                var say = false
                while (map!!.mobGroups.isNotEmpty()) {
                    if (!say) GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(
                        map,
                        "",
                        -1,
                        "Ganymede",
                        "Pour entrer en combat, clique sur l'épouvantail."
                    )
                    try {
                        Thread.sleep(1000)
                    } catch (ignored: InterruptedException) {
                    }
                    say = true
                }
                say = true
                while (player!!.pelea != null && say) {
                    if (player!!.pelea.isBegin) say = false
                    try {
                        Thread.sleep(2000)
                    } catch (ignored: InterruptedException) {
                    }
                }
                try {
                    Thread.sleep(1000)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "#",
                    helper!!.id,
                    "Ganymede",
                    "Voyons ce que donne ce petit sort ..."
                )
                try {
                    Thread.sleep(3000)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "#",
                    helper!!.id,
                    "Ganymede",
                    "Pour cela clique sur le sort que je t'ai donné."
                )
                while (player!!.pelea != null) try {
                    Thread.sleep(1500)
                } catch (ignored: InterruptedException) {
                }
                player!!.blockMovement = true
                try {
                    Thread.sleep(5000)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Ganymede",
                    "Tu es maintenant prêt à faire ton premier combat."
                )
                try {
                    Thread.sleep(3000)
                } catch (ignored: InterruptedException) {
                }
                player!!.unlearnSpell(661)
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Ganymede",
                    "Je vais donc reprendre mon sort d'entrainement."
                )
                try {
                    Thread.sleep(3000)
                } catch (ignored: InterruptedException) {
                }
                mapUse[2]!!.getCase(177).addOnCellStopAction(999, "388", "-1", mapUse[3])
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Ganymede",
                    "Suis-moi dans la prochaine salle, tu auras 3 nouveaux sorts."
                )
                try {
                    Thread.sleep(2000)
                } catch (ignored: InterruptedException) {
                }
                player!!.blockMovement = false
                pathstr = try {
                    Camino.getShortestStringPathBetween(map, helper!!.cellId, 177, 0)
                } catch (e: Exception) {
                    return
                }
                if (pathstr == null) return
                GestorSalida.GAME_SEND_GA_PACKET(
                    player!!.gameClient,
                    "0",
                    "1",
                    helper!!.id.toString() + "",
                    pathstr
                )
                try {
                    Thread.sleep(2000)
                } catch (ignored: InterruptedException) {
                }
                map!!.RemoveNpc(helper!!.id)
                GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(map, helper!!.id)
                map = mapUse[3]
                map!!.addNpc(50001, 299, 1)
                map!!.spawnGroupOnCommand(311, "432,1,1;", false)
                while (!map!!.players.contains(player)) try {
                    Thread.sleep(250)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_MAP_MOBS_GMS_PACKETS_TO_MAP(map, player)
                player!!.blockMovement = true
                try {
                    Thread.sleep(3000)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Ganymede",
                    "Bien, tu sais désormais comment te battre contre un ennemi."
                )
                try {
                    Thread.sleep(3000)
                } catch (ignored: InterruptedException) {
                }
                player!!.setSpellsPlace(true)
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Ganymede",
                    "Je viens de te donner tes trois premiers sorts."
                )
                try {
                    Thread.sleep(3000)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Ganymede",
                    "Utilise les pour combattre l'Arakne qui se trouve dans cette pièce."
                )
                try {
                    Thread.sleep(3000)
                } catch (ignored: InterruptedException) {
                }
                GestorSalida.GAME_SEND_cMK_PACKET(
                    player,
                    "",
                    helper!!.id,
                    "Ganymede",
                    "Si tu arrives à la vaincre, tu gagneras un niveau. Reviens me voir dès que tu seras niveau 2."
                )
                player!!.blockMovement = false
                thread.interrupt()
            } catch (ignored: Exception) {
            }
        }
    }

    init {
        player!!.start = this
        thread = Thread(starting())
        thread.start()
        Thread(verifyIsOnline()).start()
    }
}