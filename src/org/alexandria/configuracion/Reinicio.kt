package org.alexandria.configuracion

import java.text.SimpleDateFormat
import java.util.*

class Reinicio {
    fun initialize() {
        check()
    }

    companion object {
        private var horasrestantes: Byte = 0
        private var minutosrestantes: Byte = 0

        @JvmStatic
        fun check(): Boolean {
            val fecha = Calendar.getInstance().time
            val horaactual = SimpleDateFormat("HH").format(fecha).toInt()
            val minutoactual = SimpleDateFormat("mm").format(fecha).toInt()
            val total = horaactual * 60 + minutoactual
            val restante = 24 * 60 - (total - 5 * 60).toDouble()
            val hora = (restante / 60).toByte()
            val minuto = ((restante / 60 - hora) * 60).toByte()
            horasrestantes = hora
            minutosrestantes = minuto
            when (horaactual) {
                0, 1, 2, 3, 4 -> horasrestantes = (horasrestantes - 24).toByte()
            }
            return hora.toInt() == 0 && minuto.toInt() == 0 || horaactual == 4 && minutoactual == 59
        }

        @JvmStatic
        fun toStr(): String {
            var im = "Im115;"
            if (horasrestantes.toInt() == 0) {
                im += minutosrestantes.toString() + if (minutosrestantes > 1) " minutos" else " minuto"
            } else {
                im += horasrestantes.toString() + if (horasrestantes > 1) " horas y " else " hora y "
                im += minutosrestantes.toString() + if (minutosrestantes > 1) " minutos" else " minuto"
            }
            return im
        }
    }
}