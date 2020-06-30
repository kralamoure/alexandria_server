package org.alexandria.estaticos.area.mapa.entrada

class Animaciones(
    val id: Int,
    private val animacionID: Int,
    val nombre: String,
    val area: Int,
    val accion: Int,
    val size: Int
) {

    fun prepareToGA(): String {
        return "$animacionID,$area,$accion,$size"
    }

}