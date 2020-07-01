package org.alexandria.estaticos.comandos.administracion

import org.alexandria.comunes.gestorsql.Database
import java.util.*

class Comandos(val id: Int, comando: String?, argumento: String?, descripcion: String?) {
    @JvmField
    val argumento = arrayOfNulls<String>(3)

    companion object {
        @JvmField
        var comandos: MutableList<Comandos> = ArrayList()
        @JvmStatic
        fun getComandobyID(id: Int): Comandos? {
            for (command in comandos) if (command.id == id) return command
            return null
        }

        @JvmStatic
        fun reload() {
            comandos.clear()
            Database.dinamicos.commandData!!.load(null)
        }
    }

    init {
        this.argumento[0] = comando
        this.argumento[1] = argumento ?: ""
        this.argumento[2] = descripcion ?: ""
        comandos.add(this)
    }
}