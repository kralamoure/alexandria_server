package org.alexandria.estaticos.juego.mundo

import org.alexandria.estaticos.area.Area
import org.alexandria.estaticos.area.SubArea
import org.alexandria.estaticos.area.mapa.Mapa
import org.alexandria.estaticos.Casas
import org.alexandria.estaticos.Cercados
import org.alexandria.estaticos.Cofres
import org.alexandria.estaticos.cliente.Cuenta
import org.alexandria.estaticos.cliente.Jugador
import org.alexandria.comunes.GestorSalida
import org.alexandria.comunes.gestorsql.Database
import org.alexandria.configuracion.Configuracion.isSaving
import org.alexandria.estaticos.Monstruos.MobGroup
import org.alexandria.estaticos.Gremio
import org.alexandria.estaticos.Montura
import org.alexandria.estaticos.Recaudador
import org.alexandria.estaticos.juego.JuegoServidor
import org.alexandria.estaticos.juego.planificador.Updatable
import org.alexandria.estaticos.objeto.ObjetoJuego
import java.util.*
import java.util.function.Consumer

class MundoGuardado private constructor() : Updatable(1800000) {
    override fun update() {
        if (verify()) if (!isSaving) {
            thread = Thread(Runnable { cast(1) })
            thread!!.name = MundoGuardado::class.java.name
            thread!!.isDaemon = true
            thread!!.start()
        }
    }

    override fun get(): ObjetoJuego? {
        return null
    }

    companion object {
        val updatable: Updatable = MundoGuardado()
        private var thread: Thread? = null
        @JvmStatic
        fun cast(trys: Int) {
            if (trys != 0) JuegoServidor.INSTANCE.setState(2)
            try {
                Mundo.mundo.logger.debug("Empezando guardado del mundo..")
                GestorSalida.GAME_SEND_Im_PACKET_TO_ALL("1164;")
                isSaving = true

                //Datos de guardado
                Mundo.mundo.logger.info("-> cuentas.")
                Mundo.mundo.cuenta.stream()
                    .filter { obj: Cuenta? -> Objects.nonNull(obj) }.forEach { account: Cuenta? ->
                        Database.dinamicos.accountData?.update(account)
                    }
                Mundo.mundo.logger.info("-> jugadores.")
                Mundo.mundo.logger.info("-> miembros de gremios.")
                Mundo.mundo.jugador.stream()
                    .filter { obj: Jugador? -> Objects.nonNull(obj) }.filter { obj: Jugador -> obj.isOnline }
                    .forEach { player: Jugador ->
                        Database.dinamicos.playerData?.update(player)
                        if (player.guildMember != null) Database.estaticos.guildMemberData?.update(player)
                    }
                Mundo.mundo.logger.info("-> prismas.")
                for (prism in Mundo.mundo.prisms.values) if (Mundo.mundo.getMap(prism.map)
                        .subArea.prismId != prism.id
                ) Database.estaticos.prismData?.delete(prism.id) else Database.estaticos
                    .prismData?.update(prism)
                Mundo.mundo.logger.info("-> gremios.")
                Mundo.mundo.guilds.values.forEach(Consumer { guild: Gremio ->
                    Database.dinamicos.guildData?.update(guild)
                })
                Mundo.mundo.logger.info("-> recaudadores.")
                Mundo.mundo.collectors.values.stream()
                    .filter { collector: Recaudador -> collector.inFight <= 0 }
                    .forEach { collector: Recaudador? ->
                            Database.estaticos.collectorData?.update(collector!!)
                    }
                Mundo.mundo.logger.info("-> casas.")
                Mundo.mundo.houses.values.stream()
                    .filter { house: Casas -> house.ownerId > 0 }.forEach { house: Casas? ->
                        Database.estaticos.houseData?.update(house!!)
                    }
                Mundo.mundo.logger.info("-> cofres.")
                Mundo.mundo.trunks.values.forEach(Consumer { trunk: Cofres? ->
                    Database.estaticos.trunkData?.update(trunk!!)
                })
                Mundo.mundo.logger.info("-> cercados.")
                Mundo.mundo.mountparks.values.stream()
                    .filter { mp: Cercados -> mp.owner > 0 || mp.owner == -1 }
                    .forEach { mp: Cercados? ->
                        Database.estaticos.mountParkData?.update(mp!!)
                    }
                Mundo.mundo.logger.info("-> monturas.")
                Mundo.mundo.mounts.values.forEach(Consumer { mount: Montura? ->
                    Database.dinamicos.mountData?.update(mount!!)
                })
                Mundo.mundo.logger.info("-> areas.")
                Mundo.mundo.areas.values.forEach(Consumer { area: Area? ->
                    Database.estaticos.areaData?.update(area!!)
                })
                Mundo.mundo.subAreas.values.forEach(Consumer { subArea: SubArea? ->
                    Database.estaticos.subAreaData?.update(subArea!!)
                })
                Mundo.mundo.logger.info("-> objetos.")
                try {
                    for (`object` in ArrayList(Mundo.mundo.gameObjects)) {
                        if (`object` == null) continue
                        if (`object`.modificaciones.toInt() == 0) Database.dinamicos.objectData
                            ?.insert(`object`) else if (`object`.modificaciones.toInt() == 1) Database.dinamicos
                            .objectData?.update(`object`)
                        `object`.modificaciones = -1
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                //Fin datos de guardado
                Mundo.mundo.logger.debug("The save has been doing successfully !")
                GestorSalida.GAME_SEND_Im_PACKET_TO_ALL("1165;")
            } catch (exception: Exception) {
                exception.printStackTrace()
                Mundo.mundo.logger.error("Error when trying save of the world : " + exception.message)
                if (trys < 10) {
                    Mundo.mundo.logger.error("Fail of the save, num of try : " + (trys + 1) + ".")
                    cast(trys + 1)
                    return
                }
                isSaving = false
            } finally {
                isSaving = false
            }
            if (trys != 0) JuegoServidor.INSTANCE.setState(1)
            if (thread != null) {
                Mundo.mundo.mapa.stream()
                    .filter { map: Mapa? -> map != null && map.mobGroups != null }
                    .forEach { map: Mapa ->
                        map.mobGroups.values
                            .stream()
                            .filter { obj: MobGroup? -> Objects.nonNull(obj) }
                            .forEach {  }
                    }
                val copy = thread
                thread = null
                copy!!.interrupt()
            }
        }
    }
}