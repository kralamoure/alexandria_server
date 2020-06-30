package org.alexandria.estaticos.juego.mundo

import org.alexandria.comunes.Formulas
import org.alexandria.comunes.GestorSalida
import org.alexandria.estaticos.juego.planificador.Updatable
import java.util.*

class MundoPublicidad(wait: Int) : Updatable(wait) {
    override fun update() {
        if (pubs.isNotEmpty()) {
            if (verify()) {
                val pub = pubs[Formulas.getRandomValue(
                    0,
                    pubs.size - 1
                )]
                GestorSalida.GAME_SEND_MESSAGE_TO_ALL("Publicidad: $pub", "046380")
            }
        }
    }

    override fun get(): Any? {
        return null
    }

    companion object {
        @JvmField
        val pubs = ArrayList<String>()
        val updatable: Updatable = MundoPublicidad(30000)
    }
}