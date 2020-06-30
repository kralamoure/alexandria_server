package org.alexandria.configuracion

import java.time.Instant

object Configuracion {

    val startTime = Instant.now().toEpochMilli()
    var HALLOWEEN = LeerConfiguracion.datos[LeerConfiguracion.mode.halloween]
    var NOEL = LeerConfiguracion.datos[LeerConfiguracion.mode.christmas]
    var HEROIC = LeerConfiguracion.datos[LeerConfiguracion.mode.heroic]
    var TEAM_MATCH = LeerConfiguracion.datos[LeerConfiguracion.options.teamMatch]
    var DEATH_MATCH = LeerConfiguracion.datos[LeerConfiguracion.options.deathMatch]
    var AUTO_EVENT = LeerConfiguracion.datos[LeerConfiguracion.options.event.active]
    var AUTO_REBOOT = LeerConfiguracion.datos[LeerConfiguracion.options.autoReboot]
    var ALL_ZAAP = LeerConfiguracion.datos[LeerConfiguracion.options.allZaap]
    var ALL_EMOTE = LeerConfiguracion.datos[LeerConfiguracion.options.allEmote]

    var isSaving = false
    var isRunning = false

    var ENCRYPT_PACKET = LeerConfiguracion.datos[LeerConfiguracion.options.encryptPacket]
    var TIME_PER_EVENT: Short = LeerConfiguracion.datos[LeerConfiguracion.options.event.timePerEvent].toShort()

    var NAME: String = LeerConfiguracion.datos[LeerConfiguracion.options.nombreserver]
    var url: String = LeerConfiguracion.datos[LeerConfiguracion.options.url]
    var colorMessage = LeerConfiguracion.datos[LeerConfiguracion.options.colormensaje]
    var maxonline = LeerConfiguracion.datos[LeerConfiguracion.options.maxonline]

    var OficiosDelay = LeerConfiguracion.datos[LeerConfiguracion.options.OficiosDelay]
    var erosion = LeerConfiguracion.datos[LeerConfiguracion.options.erosion]
    var podbase = LeerConfiguracion.datos[LeerConfiguracion.options.podbase]
    var resetincarnam = LeerConfiguracion.datos[LeerConfiguracion.options.resetincarnam]
    var astrub = LeerConfiguracion.datos[LeerConfiguracion.options.astrub]
    var pvp = LeerConfiguracion.datos[LeerConfiguracion.options.pvp]
    var azra = LeerConfiguracion.datos[LeerConfiguracion.options.azra]
    var banco = LeerConfiguracion.datos[LeerConfiguracion.options.banco]

    //AFK y jugadores fantasmas
    var idletiempo =  1000 * 60 * 15

    //Mostrar enviador y recibidos
    var mostrarenviados = LeerConfiguracion.datos[LeerConfiguracion.options.mostrarenviados]
    var mostrarrecibidos = LeerConfiguracion.datos[LeerConfiguracion.options.mostrarrecibidos]

    //Gestacion montura
    var gestacionmontura = LeerConfiguracion.datos[LeerConfiguracion.options.gestacionmontura]

    var START_MAP = LeerConfiguracion.datos[LeerConfiguracion.options.start.map]
    var START_CELL = LeerConfiguracion.datos[LeerConfiguracion.options.start.cell]
    var RATE_KAMAS = LeerConfiguracion.datos[LeerConfiguracion.rate.kamas]
    var RATE_DROP = LeerConfiguracion.datos[LeerConfiguracion.rate.farm]
    var RATE_HONOR = LeerConfiguracion.datos[LeerConfiguracion.rate.honor]
    var RATE_JOB = LeerConfiguracion.datos[LeerConfiguracion.rate.job]
    var RATE_XP = LeerConfiguracion.datos[LeerConfiguracion.rate.xp]

    var exchangePort: Int = LeerConfiguracion.datos[LeerConfiguracion.intercambio.port]
    var gamePort: Int = LeerConfiguracion.datos[LeerConfiguracion.servidor.port]
    var exchangeIp: String = LeerConfiguracion.datos[LeerConfiguracion.intercambio.host]
    var loginHostDB: String = LeerConfiguracion.datos[LeerConfiguracion.database.login.host]
    var loginPortDB: Int = LeerConfiguracion.datos[LeerConfiguracion.database.login.port]
    var loginNameDB: String = LeerConfiguracion.datos[LeerConfiguracion.database.login.name]
    var loginUserDB: String = LeerConfiguracion.datos[LeerConfiguracion.database.login.user]
    var loginPassDB: String = LeerConfiguracion.datos[LeerConfiguracion.database.login.pass]
    var hostDB: String? = LeerConfiguracion.datos[LeerConfiguracion.database.game.host]
    var portDB: Int = LeerConfiguracion.datos[LeerConfiguracion.database.game.port]
    var nameDB: String? = LeerConfiguracion.datos[LeerConfiguracion.database.game.name]
    var userDB: String? = LeerConfiguracion.datos[LeerConfiguracion.database.game.user]
    var passDB: String? = LeerConfiguracion.datos[LeerConfiguracion.database.game.pass]
    var ip: String? = LeerConfiguracion.datos[LeerConfiguracion.servidor.host]

    //Armas de clase
    var armabonusbase = LeerConfiguracion.datos[LeerConfiguracion.options.armabonusbase]
    var primerarmabonus = LeerConfiguracion.datos[LeerConfiguracion.options.primerarmabonus]
    var segundaarmabonus = LeerConfiguracion.datos[LeerConfiguracion.options.segundaarmabonus]
    var daganerf = LeerConfiguracion.datos[LeerConfiguracion.options.daganerf]

    var SERVER_ID: Int = LeerConfiguracion.datos[LeerConfiguracion.servidor.id]
    var SERVER_KEY: String = LeerConfiguracion.datos[LeerConfiguracion.servidor.key]
    var subscription = LeerConfiguracion.datos[LeerConfiguracion.options.subscription]

    var startKamas: Long = LeerConfiguracion.datos[LeerConfiguracion.options.start.kamas]
    var startLevel: Int = LeerConfiguracion.datos[LeerConfiguracion.options.start.level]

}