package org.alexandria.estaticos.pelea.inteligencia.utilidad

class Node(cellId: Int, parent: Node?) {
    @JvmField
    var countG = 0
    @JvmField
    var countF = 0
    @JvmField
    var heristic = 0
    @JvmField
    var cellId = 0
    @JvmField
    var parent: Node? = null

    fun setChild(child: Node?) {}

    init {
        this.cellId = cellId
        this.parent = parent
    }
}