package org.alexandria.estaticos.juego.accion

class AccionIntercambiar<T>(val type: Byte, val value: T) {

    companion object {
        const val TALKING_WITH: Byte = 0
        const val TRADING_WITH_ME: Byte = 1
        const val TRADING_WITH_PLAYER: Byte = 2
        const val TRADING_WITH_OFFLINE_PLAYER: Byte = 3
        const val TRADING_WITH_NPC: Byte = 4
        const val TRADING_WITH_NPC_EXCHANGE: Byte = 5
        const val TRADING_WITH_NPC_PETS: Byte = 6
        const val TRADING_WITH_COLLECTOR: Byte = 7
        const val TRADING_WITH_NPC_PETS_RESURRECTION: Byte = 8
        const val CRAFTING: Byte = 9
        const val BREAKING_OBJECTS: Byte = 10
        const val CRAFTING_SECURE_WITH: Byte = 11
        const val AUCTION_HOUSE_BUYING: Byte = 12
        const val AUCTION_HOUSE_SELLING: Byte = 13
        const val IN_MOUNT: Byte = 14
        const val IN_MOUNTPARK: Byte = 15
        const val IN_TRUNK: Byte = 16
        const val IN_BANK: Byte = 17
        const val IN_ZAAPING: Byte = 18
        const val IN_ZAPPI: Byte = 19
        const val IN_PRISM: Byte = 20
        const val IN_TUTORIAL: Byte = 21
        const val FORGETTING_SPELL: Byte = 22
        const val CRAFTING_BOOK: Byte = 23
    }

}