package org.alexandria.configuracion

import ch.qos.logback.classic.Logger
import org.alexandria.estaticos.area.mapa.Mapa
import org.alexandria.estaticos.area.mapa.Mapa.ObjetosInteractivos
import org.alexandria.comunes.gestorsql.Database
import org.alexandria.estaticos.Montura
import org.alexandria.estaticos.evento.GestorEvento
import org.alexandria.intercambio.IntercambioCliente
import org.alexandria.estaticos.juego.JuegoServidor
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.estaticos.juego.mundo.MundoGuardado
import org.alexandria.estaticos.juego.mundo.MundoJugadorOpciones
import org.alexandria.estaticos.juego.mundo.MundoPublicidad
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.system.exitProcess

object MainServidor {

    val runnables: MutableList<Runnable> = LinkedList()

    var mapAsBlocked = false
    var fightAsBlocked = false
    var tradeAsBlocked = false

    private val logger = LoggerFactory.getLogger(MainServidor::class.java) as Logger
    private val shutdownThread = Thread { closeServer() }

    @Throws(SQLException::class)
    @JvmStatic fun main(args: Array<String>) {
        Runtime.getRuntime().addShutdownHook(shutdownThread)
        start()
    }

    private fun start() {
        logger.info("Usted usa ${System.getProperty("java.vendor")} en la version ${System.getProperty("java.version")}")
        logger.debug("Inicio del servidor : ${SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.FRANCE).format(Date())}")
        logger.debug("Current timestamp ms : ${Instant.now().toEpochMilli()}")
        logger.debug("Current timestamp ns : ${System.nanoTime()}")

        if (!Database.launchDatabase()) {
            logger.error("An error occurred when the server have try a connection on the Mysql server. Please verify your identification.")
            return
        }

        Configuracion.isRunning = true


        Mundo.mundo.createWorld()
        if(!JuegoServidor.INSTANCE.start()) {
            stop("Can't init game server", 2)
            return
        }

        if(!IntercambioCliente.INSTANCE?.start()!!) {
            stop("Can't init discussion with login", 3)
            return
        }

        logger.info("Servidor iniciado, esperando conexiones..\n")

        while (Configuracion.isRunning) {
            try {
                MundoGuardado.updatable.update()
                Mapa.updatable.update()
                ObjetosInteractivos.updatable.update()
                Montura.updatable.update()
                MundoJugadorOpciones.updatable.update()
                MundoPublicidad.updatable.update()
                GestorEvento.instance.update()

                if (runnables.isNotEmpty()) {
                    for (runnable in LinkedList(runnables)) {
                        try {
                            if (runnable != null) {
                                runnable.run()
                                runnables.remove(runnable)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }

                Thread.sleep(100)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun closeServer() {
        if (Configuracion.isRunning) {
            Configuracion.isRunning = false

            JuegoServidor.INSTANCE.setState(0)
            MundoGuardado.cast(0)
            if (!Configuracion.HEROIC) {
                Database.estaticos.heroicMobsGroups?.deleteAll()
                for (map in Mundo.mundo.mapa) {
                    map.mobGroups.values.filterNot { it.isFix }.forEach { Database.estaticos.heroicMobsGroups?.insert(map.id, it, null) }
                }
            }
            JuegoServidor.INSTANCE.setState(0)

            JuegoServidor.INSTANCE.kickAll(true)
            Database.dinamicos.serverData?.loggedZero()
        }
        JuegoServidor.INSTANCE.stop()
        IntercambioCliente.INSTANCE?.stop()
        logger.info("The server is now closed.")
    }

    @JvmOverloads
    fun stop(reason: String, exitCode : Int = 0) {
        logger.error("Start closing server : {}", reason)
        Runtime.getRuntime().removeShutdownHook(shutdownThread)
        closeServer()
        exitProcess(exitCode)
    }

}
