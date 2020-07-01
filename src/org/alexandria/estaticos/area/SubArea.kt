package org.alexandria.estaticos.area

import org.alexandria.estaticos.area.mapa.Mapa
import org.alexandria.estaticos.juego.mundo.Mundo
import java.util.*

class SubArea(val id: Int, area: Int) {
    @JvmField
    val area: Area = Mundo.mundo.getArea(area)
    var alignement = 0
        set(alignement) {
            if (this.alignement == 1 && alignement == -1) bontarians-- else if (this.alignement == 2 && alignement == -1) brakmarians-- else if (this.alignement == -1 && alignement == 1) bontarians++ else if (this.alignement == -1 && alignement == 2) brakmarians++
            field = alignement
        }
    @JvmField
    var prismId = 0
    var conquistable = false
        private set
    @JvmField
    val maps = ArrayList<Mapa>()

    fun setConquistable(conquistable: Int) {
        this.conquistable = conquistable == 0
    }

    fun addMap(Map: Mapa) {
        maps.add(Map)
    }

    companion object {
        @JvmField
        var bontarians = 0
        @JvmField
        var brakmarians = 0
    }

}