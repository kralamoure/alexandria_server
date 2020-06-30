package org.alexandria.estaticos.juego.accion

class AccionJuego(var id: Int, var actionId: Int, var packet: String) {
    @JvmField
    var args: String? = null
    @JvmField
    var tp = false

}