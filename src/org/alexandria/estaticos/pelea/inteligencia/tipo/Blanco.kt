package org.alexandria.estaticos.pelea.inteligencia.tipo

import org.alexandria.estaticos.pelea.Pelea
import org.alexandria.estaticos.pelea.Peleador
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta

class Blanco(fight: Pelea?, fighter: Peleador?) : InteligenciaAbstracta(fight, fighter, 1.toByte()) {
    override fun apply() {
        stop = true
    }
}