package org.alexandria.comunes

import org.alexandria.estaticos.area.mapa.Mapa
import org.alexandria.estaticos.area.mapa.Mapa.GameCase
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*

class GestorEncriptador {
    private val caracteresHexadecimales =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    fun idceldaCodigo(cellID: Int): String {
        val char1 = cellID / 64
        val char2 = cellID % 64
        return HASH[char1].toString() + "" + HASH[char2]
    }

    fun codigoceldaID(cellCode: String): Int {
        val char1 = cellCode[0]
        val char2 = cellCode[1]
        var code1 = 0
        var code2 = 0
        var a = 0
        while (a < HASH.size) {
            if (HASH[a] == char1) code1 = a * 64
            if (HASH[a] == char2) code2 = a
            a++
        }
        return code1 + code2
    }

    fun getIntByHashedValue(c: Char): Int {
        for (a in HASH.indices) if (HASH[a] == c) return a
        return -1
    }

    fun getHashedValueByInt(c: Int): Char {
        return HASH[c]
    }

    fun parseStartCell(map: Mapa, num: Int): ArrayList<GameCase?>? {
        var list: ArrayList<GameCase?>? = null
        val infos: String
        if (!map.places.equals("-1", ignoreCase = true)) {
            infos = map.places.split("\\|".toRegex()).toTypedArray()[num]
            var a = 0
            list = ArrayList()
            while (a < infos.length) {
                list.add(
                    map.getCase(
                        (getIntByHashedValue(infos[a]) shl 6)
                                + getIntByHashedValue(infos[a + 1])
                    )
                )
                a += 2
            }
        }
        return list
    }

    fun decompileMapData(map: Mapa?, data: String, sniffed: Byte): List<GameCase> {
        val cells: MutableList<GameCase> = ArrayList()
        var a = 0
        var f = 0
        while (f < data.length) {
            val mapData = data.substring(f, f + 10)
            val cellInfos: MutableList<Byte> = ArrayList()
            for (element in mapData) cellInfos.add(getIntByHashedValue(element).toByte())
            val walkable: Int = (cellInfos[2]).toInt() and 56 shr 3
            val los = (cellInfos[0]).toInt() and 1 != 0
            val layerObject2: Int =
                ((cellInfos[0]).toInt() and 2 shl 12) + ((cellInfos[7]).toInt() and 1 shl 12) + ((cellInfos[8]).toInt() shl 6) + cellInfos[9]
            val layerObject2Interactive = (cellInfos[7]).toInt() and 2 shr 1 != 0
            val `object` = if (layerObject2Interactive && sniffed.toInt() == 0) layerObject2 else -1
            if (walkable != 0 && !mapData.equals("bhGaeaaaaa", ignoreCase = true) && !mapData.equals(
                    "Hhaaeaaaaa",
                    ignoreCase = true
                )
            ) a++
            cells.add(
                GameCase(
                    map,
                    (f / 10).toShort().toInt(),
                    walkable != 0 && !mapData.equals("bhGaeaaaaa", ignoreCase = true) && !mapData.equals(
                        "Hhaaeaaaaa",
                        ignoreCase = true
                    ),
                    los,
                    `object`
                )
            )
            f += 10
        }
        return cells
    }

    // prepareData
    fun cryptMessage(message: String, key: String): String {
        val str = StringBuilder()
        // Append keyId
        str.append(caracteresHexadecimales[1])
        // Append checksum
        val checksum = checksum(message)
        str.append(caracteresHexadecimales[checksum])
        // Prepare key cause it's hexa form
        val c = checksum * 2
        val data = encode(message)
        val keyLength = key.length
        for (i in data.indices) str.append(decimalToHexadecimal((data[i]).toInt() xor key[(i + c) % keyLength].toInt()))
        return str.toString()
    }

    fun decryptMessage(message: String, key: String): String {
        val c = message[1].toString().toInt(16) * 2
        val str = StringBuilder()
        var j = 0
        val keyLength = key.length
        var i = 2
        while (i < message.length) {
            str.append(
                (message.substring(i, i + 2).toInt(16) xor key[(j++ + c) % keyLength].toInt()).toChar()
            )
            i += 2
        }
        var data = str.toString()
        data = data.replace("%(?![0-9a-fA-F]{2})".toRegex(), "%25")
        data = data.replace("\\+".toRegex(), "%2B")
        return URLDecoder.decode(data, StandardCharsets.UTF_8)
    }

    fun prepareKey(key: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < key.length) {
            sb.append(key.substring(i, i + 2).toInt(16).toChar())
            i += 2
        }
        return URLDecoder.decode(sb.toString(), StandardCharsets.UTF_8)
    }

    private fun checksum(data: String): Int {
        var result = 0
        for (c in data.toCharArray()) result += c.toInt() % 16
        return result % 16
    }

    private fun decimalToHexadecimal(cc: Int): String {
        var c = cc
        if (c > 255) c = 255
        return caracteresHexadecimales[c / 16].toString() + "" + caracteresHexadecimales[c % 16]
    }

    private fun encode(input: String): String {
        val resultStr = StringBuilder()
        for (ch in input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%')
                resultStr.append(toHex(ch.toInt() / 16))
                resultStr.append(toHex(ch.toInt() % 16))
            } else {
                resultStr.append(ch)
            }
        }
        return resultStr.toString()
    }

    private fun toHex(ch: Int): Char {
        return (if (ch < 10) '0'.toInt() + ch else 'A'.toInt() + ch - 10).toChar()
    }

    private fun isUnsafe(ch: Char): Boolean {
        return ch.toInt() > 255 || "+%".indexOf(ch) >= 0
    }

    companion object {
        val HASH = charArrayOf(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
            'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
            'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'
        )
    }
}