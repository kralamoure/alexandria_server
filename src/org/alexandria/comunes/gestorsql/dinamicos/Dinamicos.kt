package org.alexandria.comunes.gestorsql.dinamicos

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.Database.tryConnection
import org.alexandria.comunes.gestorsql.dinamicos.datos.*
import org.alexandria.configuracion.Configuracion.loginHostDB
import org.alexandria.configuracion.Configuracion.loginNameDB
import org.alexandria.configuracion.Configuracion.loginPassDB
import org.alexandria.configuracion.Configuracion.loginPortDB
import org.alexandria.configuracion.Configuracion.loginUserDB
import org.alexandria.configuracion.MainServidor.stop
import org.slf4j.LoggerFactory

class Dinamicos {
    //connection
    var dataSource: HikariDataSource? = null
        private set
    private val logger =
        LoggerFactory.getLogger(Dinamicos::class.java) as Logger

    //data
    var accountData: DatosCuentas? = null
        private set
    var banIpData: DatosIPBaneadas? = null
        private set
    var commandData: DatosComandos? = null
        private set
    var eventData: DatosEventos? = null
        private set
    var groupData: DatosGrupos? = null
        private set
    var guildData: DatosGremio? = null
        private set
    var houseData: DatosCasas? = null
        private set
    var mountData: DatosMonturas? = null
        private set
    var mountParkData: DatosCercados? = null
        private set
    var objectData: DatosObjetos? = null
        private set
    var obvejivanData: DatosObjevivos? = null
        private set
    var petData: DatosMascotas? = null
        private set
    var playerData: DatosPersonajes? = null
        private set
    var pubData: DatosPublicidad? = null
        private set
    var questPlayerData: DatosMisionesPersonaje? = null
        private set
    var serverData: DatosServidor? = null
        private set
    var trunkData: DatosCofres? = null
        private set
    var worldEntityData: DatosGenerales? = null
        private set

    private fun initializeData() {
        accountData = DatosCuentas(dataSource)
        commandData = DatosComandos(dataSource)
        eventData = DatosEventos(dataSource)
        playerData = DatosPersonajes(dataSource)
        serverData = DatosServidor(dataSource)
        banIpData = DatosIPBaneadas(dataSource)
        guildData = DatosGremio(dataSource)
        groupData = DatosGrupos(dataSource)
        houseData = DatosCasas(dataSource)
        trunkData = DatosCofres(dataSource)
        mountData = DatosMonturas(dataSource)
        mountParkData = DatosCercados(dataSource)
        objectData = DatosObjetos(dataSource)
        obvejivanData = DatosObjevivos(dataSource)
        pubData = DatosPublicidad(dataSource)
        petData = DatosMascotas(dataSource)
        questPlayerData = DatosMisionesPersonaje(dataSource)
        worldEntityData = DatosGenerales(dataSource)
    }

    fun inicializarconexion(): Boolean {
        try {
            logger.level = Level.ALL
            logger.trace("Reading database config")
            val config = HikariConfig()
            config.dataSourceClassName = "org.mariadb.jdbc.MySQLDataSource"
            config.addDataSourceProperty("serverName", loginHostDB)
            config.addDataSourceProperty("port", loginPortDB)
            config.addDataSourceProperty("databaseName", loginNameDB)
            config.addDataSourceProperty("user", loginUserDB)
            config.addDataSourceProperty("password", loginPassDB)
            config.isAutoCommit = true // AutoCommit, c'est cool
            config.maximumPoolSize = 50
            //config.setMinimumIdle(1);
            dataSource = HikariDataSource(config)
            if (tryConnection(dataSource!!)) {
                logger.error("Please verify your username and password and database connection")
                stop("statics try connection failed")
                return true
            }
            logger.info("Database connection established")
            initializeData()
            logger.info("Database data loaded")
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }
        return false
    }

}