package org.alexandria.configuracion

import com.natpryce.konfig.*
import java.io.File

object LeerConfiguracion {
    lateinit var datos: Configuration

    init {
        reload()
    }

    fun reload() {
        datos = EnvironmentVariables() overriding ConfigurationProperties.fromFile(File("config.properties"))
    }

    object servidor : PropertyGroup() {
        val id by intType
        val key by stringType
        val host by stringType
        val port by intType
    }

    object intercambio : PropertyGroup() {
        val host by stringType
        val port by intType
    }

    object database : PropertyGroup() {
        object login : PropertyGroup() {
            val host by stringType
            val port by intType
            val user by stringType
            val pass by stringType
            val name by stringType
        }

        object game : PropertyGroup() {
            val host by stringType
            val port by intType
            val user by stringType
            val pass by stringType
            val name by stringType
        }
    }

    object rate : PropertyGroup() {
        val xp by doubleType
        val job by intType
        val farm by intType
        val honor by intType
        val kamas by intType
    }

    object mode : PropertyGroup() {
        val halloween by booleanType
        val christmas by booleanType
        val heroic by booleanType
    }

    object options : PropertyGroup() {
        object start : PropertyGroup() {
            val map by intType
            val cell by intType
            val kamas by longType
            val level by intType
        }
        object event : PropertyGroup() {
            val active by booleanType
            val timePerEvent by intType
        }
        val nombreserver by stringType
        val url by stringType
        val colormensaje by stringType
        val OficiosDelay by intType
        val erosion by intType
        val podbase by intType
        val resetincarnam by intType
        val autoReboot by booleanType
        val encryptPacket by booleanType
        val deathMatch by booleanType
        val teamMatch by booleanType
        val astrub by booleanType
        val pvp by booleanType
        val azra by booleanType
        val banco by booleanType
        val allZaap by booleanType
        val allEmote by booleanType
        val subscription by booleanType

        val armabonusbase by intType
        val primerarmabonus by intType
        val segundaarmabonus by intType
        val daganerf by intType

        val maxonline by intType
        val mostrarenviados by booleanType
        val mostrarrecibidos by booleanType
        val gestacionmontura by intType
    }
}