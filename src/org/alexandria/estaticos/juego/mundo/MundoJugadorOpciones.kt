package org.alexandria.estaticos.juego.mundo

import org.alexandria.comunes.gestorsql.Database
import org.alexandria.estaticos.juego.planificador.Updatable

class MundoJugadorOpciones(wait: Int) : Updatable(wait) {
    override fun update() {
        if (verify()) {
            Database.dinamicos.accountData?.updateVoteAll()
        }
    }

    override fun get(): Any? {
        return null
    }

    companion object {
        val updatable: Updatable = MundoPublicidad(300000)
    }
}