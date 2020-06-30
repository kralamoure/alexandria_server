package org.alexandria.estaticos.cliente.otros

import java.util.*

class Restricciones {
    //region
    @JvmField
    var aggros: Map<String, Long> = HashMap()
    var command = true

    companion object {
        private var restrictions: Map<Int, Restricciones> = HashMap()
        @JvmStatic
        operator fun get(id: Int): Restricciones? {
            return if (restrictions[id] != null) restrictions[id] else Restricciones()
        } //endregion
    }
}