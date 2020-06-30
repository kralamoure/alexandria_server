package org.alexandria.comunes.gestorsql.estaticos

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.Database.tryConnection
import org.alexandria.comunes.gestorsql.estaticos.datos.*
import org.alexandria.configuracion.Configuracion.hostDB
import org.alexandria.configuracion.Configuracion.nameDB
import org.alexandria.configuracion.Configuracion.passDB
import org.alexandria.configuracion.Configuracion.portDB
import org.alexandria.configuracion.Configuracion.userDB
import org.alexandria.configuracion.MainServidor.stop
import org.slf4j.LoggerFactory

class Estaticos {
    //connection
    var dataSource: HikariDataSource? = null
        private set

    //data
    var areaData: DatosArea? = null
        private set
    var gangsterData: DatosBandido? = null
        private set
    var bankData: DatosBancos? = null
        private set
    var trunkData: DatosCofres? = null
        private set
    var guildMemberData: DatosMiembrosGremio? = null
        private set
    var hdvObjectData: DatosObjetosMercadillos? = null
        private set
    var houseData: DatosCasas? = null
        private set
    var mountParkData: DatosCercados? = null
        private set
    var collectorData: DatosRecaudadores? = null
        private set
    var prismData: DatosPrismas? = null
        private set
    var subAreaData: DatosSubArea? = null
        private set
    var animationData: DatosAnimaciones? = null
        private set
    var challengeData: DatosRetos? = null
        private set
    var craftData: DatosRecetas? = null
        private set
    var dungeonData: DatosMazmorras? = null
        private set
    var dropData: DatosDrops? = null
        private set
    var endFightActionData: DatosFinAccionCombate? = null
        private set
    var experienceData: DatosExperiencia? = null
        private set
    var extraMonsterData: DatosMonstruosExtra? = null
        private set
    var fullMorphData: DatosTransformaciones? = null
        private set
    var giftData: DatosRegalos? = null
        private set
    var hdvData: DatosMercadillos? = null
        private set
    var interactiveDoorData: DatosPuertasInteractivas? = null
        private set
    var interactiveObjectData: DatosObjetosInteractivos? = null
        private set
    var objectTemplateData: DatosObjetoModelo? = null
        private set
    var objectSetData: DatosSets? = null
        private set
    var jobData: DatosOficios? = null
        private set
    var mapData: DatosMapas? = null
        private set
    var monsterData: DatosMonstruos? = null
        private set
    var npcQuestionData: DatosPreguntasNPC? = null
        private set
    var npcAnswerData: DatosRespuestasNPC? = null
        private set
    var npcTemplateData: DatosModeloNPC? = null
        private set
    var npcData: DatosNPC? = null
        private set
    var objectActionData: DatosObjetoAccion? = null
        private set
    var petTemplateData: DatosMascotasModelo? = null
        private set
    var questData: DatosMisiones? = null
        private set
    var questStepData: DatosMisionesEtapas? = null
        private set
    var questObjectiveData: DatosMisionesObjetivos? = null
        private set
    var runeData: DatosRunas? = null
        private set
    var scriptedCellData: DatosCeldas? = null
        private set
    var spellData: DatosHechizos? = null
        private set
    var tutorialData: DatosTutoriales? = null
        private set
    var zaapData: DatosZaap? = null
        private set
    var zaapiData: DatosZaapi? = null
        private set
    var heroicMobsGroups: DatosMobsHeroico? = null
        private set

    private fun initializeData() {
        areaData = DatosArea(dataSource)
        gangsterData = DatosBandido(dataSource)
        bankData = DatosBancos(dataSource)
        trunkData = DatosCofres(dataSource)
        guildMemberData = DatosMiembrosGremio(dataSource)
        hdvObjectData = DatosObjetosMercadillos(dataSource)
        houseData = DatosCasas(dataSource)
        mountParkData = DatosCercados(dataSource)
        collectorData = DatosRecaudadores(dataSource)
        prismData = DatosPrismas(dataSource)
        subAreaData = DatosSubArea(dataSource)
        animationData = DatosAnimaciones(dataSource)
        areaData = DatosArea(dataSource)
        challengeData = DatosRetos(dataSource)
        trunkData = DatosCofres(dataSource)
        craftData = DatosRecetas(dataSource)
        dungeonData = DatosMazmorras(dataSource)
        dropData = DatosDrops(dataSource)
        endFightActionData = DatosFinAccionCombate(dataSource)
        experienceData = DatosExperiencia(dataSource)
        extraMonsterData = DatosMonstruosExtra(dataSource)
        fullMorphData = DatosTransformaciones(dataSource)
        giftData = DatosRegalos(dataSource)
        hdvData = DatosMercadillos(dataSource)
        houseData = DatosCasas(dataSource)
        interactiveDoorData = DatosPuertasInteractivas(dataSource)
        interactiveObjectData = DatosObjetosInteractivos(dataSource)
        objectTemplateData = DatosObjetoModelo(dataSource)
        objectSetData = DatosSets(dataSource)
        jobData = DatosOficios(dataSource)
        mapData = DatosMapas(dataSource)
        monsterData = DatosMonstruos(dataSource)
        mountParkData = DatosCercados(dataSource)
        npcQuestionData = DatosPreguntasNPC(dataSource)
        npcAnswerData = DatosRespuestasNPC(dataSource)
        npcTemplateData = DatosModeloNPC(dataSource)
        npcData = DatosNPC(dataSource)
        objectActionData = DatosObjetoAccion(dataSource)
        petTemplateData = DatosMascotasModelo(dataSource)
        questData = DatosMisiones(dataSource)
        questStepData = DatosMisionesEtapas(dataSource)
        questObjectiveData = DatosMisionesObjetivos(dataSource)
        runeData = DatosRunas(dataSource)
        scriptedCellData = DatosCeldas(dataSource)
        subAreaData = DatosSubArea(dataSource)
        spellData = DatosHechizos(dataSource)
        tutorialData = DatosTutoriales(dataSource)
        zaapData = DatosZaap(dataSource)
        zaapiData = DatosZaapi(dataSource)
        heroicMobsGroups = DatosMobsHeroico(dataSource)
    }

    fun inicializarconexion(): Boolean {
        try {
            logger.level = Level.ALL
            logger.trace("Reading database config")
            val config = HikariConfig()
            config.dataSourceClassName = "org.mariadb.jdbc.MySQLDataSource"
            config.addDataSourceProperty("serverName", hostDB)
            config.addDataSourceProperty("port", portDB)
            config.addDataSourceProperty("databaseName", nameDB)
            config.addDataSourceProperty("user", userDB)
            config.addDataSourceProperty("password", passDB)
            config.isAutoCommit = true // AutoCommit, c'est cool
            config.maximumPoolSize = 50
            //config.setMinimumIdle(1);
            dataSource = HikariDataSource(config)
            if (tryConnection(dataSource!!)) {
                logger.error("Please verify your username and password and database connection")
                stop("try database connection failed")
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

    companion object {
        private val logger =
            LoggerFactory.getLogger(Estaticos::class.java) as Logger
    }
}