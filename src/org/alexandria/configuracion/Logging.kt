package org.alexandria.configuracion

import org.slf4j.LoggerFactory

object Logging {
    @JvmField
    val objetos = LoggerFactory.getLogger("Objeto")
    @JvmField
    val mensajeglobal = LoggerFactory.getLogger("MensajeGlobal")
    @JvmField
    val comandos = LoggerFactory.getLogger("Comandos")
    @JvmField
    val chats = LoggerFactory.getLogger("Chat")
    @JvmField
    val crafeo = LoggerFactory.getLogger("Crafeo")
    @JvmField
    val juego = LoggerFactory.getLogger("Juego")
    @JvmField
    val error = LoggerFactory.getLogger("Error")
}