package org.alexandria.estaticos.area.mapa.entrada

import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.otro.Accion
import java.util.*

class Tutoriales(id: Int, reward: String, start: String, end: String?) {
    @JvmField
    val id: Int
    @JvmField
    val reward = ArrayList<Accion?>(4)
    @JvmField
    var start: Accion? = null
    @JvmField
    var end: Accion? = null

    init {
        var string = end
        this.id = id
        try {
            for (str in reward.split("\\$".toRegex()).toTypedArray()) {
                if (str.isEmpty()) {
                    this.reward.add(null)
                } else {
                    val split = str.split("@".toRegex()).toTypedArray()
                    if (split.size >= 2) {
                        this.reward.add(Accion(split[0].toInt(), split[1], "", null))
                    } else {
                        this.reward.add(Accion(split[0].toInt(), "", "", null))
                    }
                }
            }
            if (start.isEmpty()) {
                this.start = null
            } else {
                val split = start.split("@".toRegex()).toTypedArray()
                if (split.size >= 2) this.start = Accion(split[0].toInt(), split[1], "", null) else this.start =
                    Accion(split[0].toInt(), "", "", null)
            }
            if (string!!.isEmpty()) {
                string = null
            } else {
                val split = string.split("@".toRegex()).toTypedArray()
                if (split.size >= 2) this.end = Accion(split[0].toInt(), split[1], "", null) else this.end =
                    Accion(split[0].toInt(), "", "", null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stop("Tutorial")
        }
    }
}