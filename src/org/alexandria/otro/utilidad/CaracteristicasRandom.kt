package org.alexandria.otro.utilidad

import org.alexandria.comunes.Formulas
import java.util.*

class CaracteristicasRandom<Stats> {
    private val randoms: ArrayList<Stats> = ArrayList()
    fun add(pct: Int, `object`: Stats) {
        for (i in 0 until pct) randoms.add(`object`)
    }

    fun size(): Int {
        return randoms.size
    }

    fun get(): Stats {
        return randoms[Formulas.random.nextInt(randoms.size)]
    }

}