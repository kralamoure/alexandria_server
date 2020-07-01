package org.alexandria.estaticos.comandos.administracion

import org.alexandria.comunes.gestorsql.Database
import org.alexandria.estaticos.comandos.administracion.Comandos.Companion.getComandobyID
import java.util.*

class GrupoADM(val id: Int, val nombre: String, val isJugador: Boolean, comandos: String) {
    private var comandos: MutableList<Comandos?> = ArrayList()

    fun getComandos(): List<Comandos?> {
        return comandos
    }

    fun haveCommand(name: String?): Boolean {
        for (command in comandos) if (command!!.argumento[0]
                .equals(name, ignoreCase = true)
        ) return true
        return false
    }

    companion object {
        private val grupo: MutableList<GrupoADM> = ArrayList()
        @JvmStatic
        fun reload() {
            grupo.clear()
            Database.dinamicos.groupData!!.load(null)
        }

        @JvmStatic
        fun getGrupoID(id: Int): GrupoADM? {
            for (group in grupo) if (group.id == id) return group
            return null
        }

        @JvmStatic
        fun getGrupo(): List<GrupoADM> {
            return grupo
        }
    }

    init {
        if (comandos.equals("all", ignoreCase = true)) {
            this.comandos = Comandos.comandos.toMutableList()
        } else {
            if (comandos.contains(",")) {
                for (str in comandos.split(",".toRegex())
                    .toTypedArray()) this.comandos.add(getComandobyID(str.toInt()))
            } else {
                this.comandos.add(getComandobyID(comandos.toInt()))
            }
        }
        grupo.add(this)
    }
}