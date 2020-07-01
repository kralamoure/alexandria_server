package org.alexandria.estaticos.area

import org.alexandria.estaticos.area.mapa.Mapa
import java.util.*

class Area(val id: Int, val superArea: Int) {
    var alignement = 0
        set(alignement) {
            if (this.alignement == 1 && alignement == -1) bontarians-- else if (this.alignement == 2 && alignement == -1) brakmarians-- else if (this.alignement == -1 && alignement == 1) bontarians++ else if (this.alignement == -1 && alignement == 2) brakmarians++
            field = alignement
        }
    @JvmField
    var prismId = 0
    @JvmField
    val subAreas = ArrayList<SubArea>()

    fun addSubArea(subArea: SubArea) {
        subAreas.add(subArea)
    }

    val maps: ArrayList<Mapa>
        get() {
            val maps = ArrayList<Mapa>()
            for (subArea in subAreas) maps.addAll(subArea.maps)
            return maps
        }

    companion object {
        @JvmField
        var bontarians = 0
        @JvmField
        var brakmarians = 0
    }

}