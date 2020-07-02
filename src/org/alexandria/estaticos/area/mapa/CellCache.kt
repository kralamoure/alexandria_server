/*
 * Decompiled with CFR 0_123.
 */
package org.alexandria.estaticos.area.mapa

import kotlin.math.abs
import kotlin.math.roundToInt

interface CellCache {
    fun isWalkable(var1: Short): Boolean
    fun getDirectWalkable(var1: Short): List<Short?>?
    fun isOutOfMap(var1: Short): Boolean
    fun getHeight(var1: Short): Float
    fun blockLineOfSight(var1: Short): Boolean
    fun getX(var1: Int): Short
    fun getY(var1: Int): Short
    fun getCellId(var1: Int, var2: Int): Short
    fun getOrthX(cellID: Int): Int {
        val x = getX(cellID)
        return abs(getOrthY(cellID) - x) + x + getY(cellID) % 2
    }

    fun getOrthY(cellID: Int): Int {
        return ((-getY(cellID)).toFloat() / 2.0f).roundToInt() + getX(cellID)
    }

    fun getOrthCellID(var1: Int, var2: Int): Int
    fun getCellsDistance(c1: Short, c2: Short): Short {
        return (abs(getOrthX(c1.toInt()) - getOrthX(c2.toInt())) + abs(
            getOrthY(
                c1.toInt()
            ) - getOrthY(c2.toInt())
        )).toShort()
    }
}