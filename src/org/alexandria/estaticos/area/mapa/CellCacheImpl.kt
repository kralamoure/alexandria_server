package org.alexandria.estaticos.area.mapa

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt

class CellCacheImpl(private val lineBlocker: List<Short>, private val width: Short, private val height: Short) :
    CellCache {
    private val orthogonalProjection: Int2IntArrayMap
    override fun isWalkable(cellId: Short): Boolean {
        return false
    }

    override fun getDirectWalkable(cellId: Short): List<Short?>? {
        val s = LinkedList<Short?>()
        if (isWalkable((cellId + 1).toShort())) {
            s.add((cellId + 1).toShort())
        }
        if (isWalkable((cellId - 1).toShort())) {
            s.add((cellId - 1).toShort())
        }
        if (isWalkable((cellId + width).toShort())) {
            s.add((cellId + width).toShort())
        }
        if (isWalkable((cellId - width).toShort())) {
            s.add((cellId - width).toShort())
        }
        if (isWalkable((cellId + width).toShort())) {
            s.add((cellId + width - 1).toShort())
        }
        if (isWalkable((cellId - width).toShort())) {
            s.add((cellId - width + 1).toShort())
        }
        if (isWalkable((cellId + width * 2 - 1).toShort())) {
            s.add((cellId + width * 2 - 1).toShort())
        }
        if (isWalkable((cellId - width * 2 + 1).toShort())) {
            s.add((cellId - width * 2 + 1).toShort())
        }
        return s
    }

    override fun isOutOfMap(cellId: Short): Boolean {
        val doubleWidthMinusOne = (width * 2 - 1).toShort()
        if (cellId < width) {
            return true
        }
        if (cellId >= (height - 1) * doubleWidthMinusOne) {
            return true
        }
        val mod = (cellId % doubleWidthMinusOne).toShort()
        if (mod.toInt() == 0) {
            return true
        }
        return if (mod.toInt() == width - 1) {
            true
        } else false
    }

    override fun getHeight(cell: Short): Float {
        val res = false
        return if (res) (1).toFloat() else (0).toFloat()
    }

    override fun blockLineOfSight(c: Short): Boolean {
        return !lineBlocker.contains(c)
    }

    override fun getX(c: Int): Short {
        return floor(c.toDouble() % (width.toDouble() - 0.5)).toShort()
    }

    override fun getY(c: Int): Short {
        return (c.toDouble() / (width.toDouble() - 0.5)).toShort()
    }

    override fun getCellId(x: Int, y: Int): Short {
        return (x.toDouble() + y.toDouble() * (width.toDouble() - 0.5)).roundToInt().toShort()
    }

    override fun getOrthCellID(x: Int, y: Int): Int {
        return orthogonalProjection[getProjectionMapKey(x, y)]
    }

    private fun getProjectionMapKey(x: Int, y: Int): Int {
        return y * (width + height - 2) + x
    }

    companion object {
        private val orthogonalProjections =
            HashMap<String, Int2IntArrayMap>()
    }

    init {
        val projKey = "" + width + "_" + height
        var proj = orthogonalProjections.getOrDefault(projKey, null)
        if (proj == null) {
            proj = Int2IntArrayMap()
            for (c in 0 until width * height + (width - 1) * (height - 1)) {
                proj[getProjectionMapKey(getOrthX(c), getOrthY(c))] = c
            }
            orthogonalProjections[projKey] = proj
        }
        orthogonalProjection = proj
    }
}