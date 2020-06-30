package org.alexandria.estaticos

import org.alexandria.estaticos.cliente.Jugador
import org.alexandria.comunes.GestorSalida
import org.alexandria.comunes.gestorsql.Database
import org.alexandria.configuracion.Configuracion.mostrarenviados
import org.alexandria.configuracion.Constantes
import org.alexandria.configuracion.Logging
import org.alexandria.estaticos.juego.mundo.Mundo
import org.alexandria.estaticos.objeto.ObjetoJuego
import org.alexandria.otro.utilidad.Doble
import java.text.DecimalFormat
import java.util.*
import kotlin.math.pow

class Mercadillo(
    val hdvId: Int,
    val taxe: Float,
    sellTime: Short,
    val maxAccountItem: Short,
    val lvlMax: Short,
    val strCategory: String) {
    val sellTime: Short = 0
    private val categorys: MutableMap<Int, MercadilloCategoria?> = HashMap()
    private val path: MutableMap<Int, Doble<Int, Int>?>? = HashMap() //<LigneID,<CategID,TemplateID>>
    private val pattern = DecimalFormat("0.0")

    private fun getCategorys(): Map<Int, MercadilloCategoria?>? {
        return categorys
    }

    fun haveCategory(categ: Int): Boolean {
        return categorys.containsKey(categ)
    }

    fun getLine(lineId: Int): MercadilloLinea? {
        if (path == null || path[lineId] == null || getCategorys() == null) return null
        val categoryId = path[lineId]!!.getPrimero()
        val templateId = path[lineId]!!.getSegundo()
        val category = getCategorys()!![categoryId] ?: return null
        val template = category.getTemplate(templateId) ?: return null
        return template.getLine(lineId)
    }

    fun addEntry(toAdd: MercadilloEntrada, load: Boolean) {
        toAdd.hdvId = hdvId
        val categoryId = toAdd.gameObject.modelo.type
        val templateId = toAdd.gameObject.modelo.id
        if (getCategorys()!![categoryId] == null) return
        (getCategorys()!![categoryId] ?: error("")).addEntry(toAdd)
        path!![toAdd.lineId] = Doble(categoryId, templateId)
        if (!load) {
            Database.estaticos.hdvObjectData?.add(toAdd)
        }
        Mundo.mundo.addHdvItem(toAdd.owner, hdvId, toAdd)
    }

    fun delEntry(toDel: MercadilloEntrada?) {
        val toReturn = (getCategorys()!![toDel!!.gameObject.modelo.type] ?: error("")).delEntry(toDel)
        if (toReturn) {
            path!!.remove(toDel.lineId)
            Mundo.mundo.removeHdvItem(toDel.owner, toDel.hdvId, toDel)
        }
    }

    val allEntry: ArrayList<MercadilloEntrada?>
        get() {
            val toReturn = ArrayList<MercadilloEntrada?>()
            for (curCat in getCategorys()!!.values) toReturn.addAll(curCat!!.allEntry)
            return toReturn
        }

    @Synchronized
    fun buyItem(ligneID: Int, amount: Byte, price: Int, newOwner: Jugador): Boolean {
        var toReturn = true
        try {
            if (newOwner.kamas < price) return false
            val ligne = getLine(ligneID)
            val toBuy = ligne!!.doYouHave(amount.toInt(), price)
            if (toBuy!!.buy) return false
            toBuy.buy = true
            newOwner.addKamas(price * (-1).toLong()) //Retire l'argent � l'acheteur (prix et taxe de vente)
            if (toBuy.owner != -1) {
                val c = Mundo.mundo.getAccount(toBuy.owner)
                if (c != null) c.bankKamas = c.bankKamas + toBuy.price //Ajoute l'argent au vendeur
            }
            GestorSalida.GAME_SEND_STATS_PACKET(newOwner) //Met a jour les kamas de l'acheteur
            toBuy.gameObject.posicion = Constantes.ITEM_POS_NO_EQUIPED
            newOwner.addObjet(toBuy.gameObject, true) //Ajoute l'objet au nouveau propri�taire
            toBuy.gameObject.modelo
                .newSold(toBuy.getAmount(true).toInt(), price) //Ajoute la ventes au statistiques
            try {
                var name = "undefined"
                if (Mundo.mundo.getAccount(toBuy.owner) != null) name =
                    Mundo.mundo.getAccount(toBuy.owner).name
                if (mostrarenviados) {
                    Logging.objetos.info(
                        "Compra en mercadillo: " + newOwner.name + " : achat de " + toBuy.gameObject
                            .modelo.name + " x" + toBuy.gameObject
                            .cantidad + " venant du compte " + name
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            delEntry(toBuy) //Retire l'item de l'HDV ainsi que de la liste du vendeur
            Database.estaticos.hdvObjectData?.delete(toBuy.gameObject.id)
            if (Mundo.mundo.getAccount(toBuy.owner) != null
                && Mundo.mundo.getAccount(toBuy.owner).currentPlayer != null
            ) GestorSalida.GAME_SEND_Im_PACKET(
                Mundo.mundo.getAccount(toBuy.owner).currentPlayer, "065;"
                        + price
                        + "~"
                        + toBuy.gameObject.modelo.id
                        + "~" + toBuy.gameObject.modelo.id + "~1"
            )
            //Si le vendeur est connecter, envoie du packet qui lui annonce la vente de son objet
            Database.dinamicos.playerData?.update(newOwner)
        } catch (e: NullPointerException) {
            e.printStackTrace()
            toReturn = false
        }
        return toReturn
    }

    fun parseToEHl(templateID: Int): String {
        try {
            val objetomodelo = Mundo.mundo.getObjetoModelo(templateID)
            val mercadillo = getCategorys()!![objetomodelo.type]
            val mercadillomodelo = mercadillo!!.getTemplate(templateID)
                ?: // Il a pu �tre achet� avant et supprim� de l'HDV. getTemplate devient null.
                return ""
            return mercadillomodelo.parseToEHl()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
        //return this.getCategorys().getWaitingAccount(World.world.getObjTemplate(templateID).getType()).getTemplate(templateID).parseToEHl();
    }

    fun parseTemplate(categID: Int): String {
        return (getCategorys()!![categID] ?: error("")).parseTemplate()
    }

    fun parseTaxe(): String {
        return pattern.format(taxe.toDouble()).replace(",", ".")
    }

    inner class MercadilloCategoria(categoryId: Int) {
        private var categoryId = 0
        private val templates: MutableMap<Int, MercadilloModelo> =
            HashMap() //Dans le format <templateID,Template>

        fun getTemplate(templateId: Int): MercadilloModelo? {
            return templates[templateId]
        }

        private fun addTemplate(templateId: Int, toAdd: MercadilloEntrada) {
            templates[templateId] = MercadilloModelo(templateId, toAdd)
        }

        private fun delTemplate(templateId: Int) {
            templates.remove(templateId)
        }

        fun addEntry(toAdd: MercadilloEntrada) {
            val templateId = toAdd.gameObject.modelo.id
            if (templates[templateId] == null) addTemplate(templateId, toAdd) else templates[templateId]!!.addEntry(
                toAdd
            )
        }

        fun delEntry(toDel: MercadilloEntrada?): Boolean {
            var toReturn = false
            templates[toDel!!.gameObject.modelo.id]!!.delEntry(toDel)
            if (templates[toDel.gameObject.modelo.id]!!.isEmpty
                    .also { toReturn = it }
            ) delTemplate(toDel.gameObject.modelo.id)
            return toReturn
        }

        val allEntry: ArrayList<MercadilloEntrada?>
            get() {
                val toReturn = ArrayList<MercadilloEntrada?>()
                for (template in templates.values) toReturn.addAll(template.allEntry)
                return toReturn
            }

        fun parseTemplate(): String {
            var isFirst = true
            val strTemplate = StringBuilder()
            for (templateId in templates.keys) {
                if (!isFirst) strTemplate.append(";")
                strTemplate.append(templateId)
                isFirst = false
            }
            return strTemplate.toString()
        }

        init {
            this.categoryId = categoryId
        }
    }

    class MercadilloEntrada(id: Int, price: Int, amount: Byte, owner: Int, gameObject: ObjetoJuego) :
        Comparable<MercadilloEntrada> {
        @kotlin.jvm.JvmField
        var buy = false
        var id = 0
        var hdvId = 0
        var lineId = 0
        val owner: Int
        val price: Int
        private val amount //Dans le format : 1=1 2=10 3=100
                : Byte
        val gameObject: ObjetoJuego

        fun getAmount(ok: Boolean): Byte {
            return if (ok) (10.0.pow(amount.toDouble()) / 10).toByte() else amount
        }

        fun parseToEL(): String {
            val toReturn = StringBuilder()
            val count =
                getAmount(true).toInt() //Transf�re dans le format (1,10,100) le montant qui etait dans le format (1,2,3)
            toReturn.append(lineId).append(";").append(count).append(";")
                .append(gameObject.modelo.id).append(";")
                .append(gameObject.parseStatsString()).append(";").append(price)
                .append(";350") //350 = temps restant
            return toReturn.toString()
        }

        fun parseToEmK(): String {
            val toReturn = StringBuilder()
            val count =
                getAmount(true).toInt() //Transf�re dans le format (1,10,100) le montant qui etait dans le format (1,2,3)
            toReturn.append(gameObject.id).append("|").append(count).append("|")
                .append(gameObject.modelo.id).append("|")
                .append(gameObject.parseStatsString()).append("|").append(price)
                .append("|350") //350 = temps restant
            return toReturn.toString()
        }

        override fun compareTo(o: MercadilloEntrada): Int {
            val celuiCi = price
            val autre = o.price
            if (autre > celuiCi) return -1
            if (autre == celuiCi) return 0
            return if (autre < celuiCi) 1 else 0
        }

        init {
            this.id = id
            this.price = price
            this.amount = amount
            this.gameObject = gameObject
            this.owner = owner
        }
    }

    inner class MercadilloLinea(private val lineId: Int, toAdd: MercadilloEntrada) {
        val templateId: Int = toAdd.gameObject.modelo.id
        private val entries =
            ArrayList<ArrayList<MercadilloEntrada>>(3) //La premi�re ArrayList est un tableau de 3 (0=1 1=10 2=100 de quantit�)
        private val strStats: String = toAdd.gameObject.parseStatsString()

        private fun haveSameStats(toAdd: MercadilloEntrada): Boolean {
            return (strStats.equals(toAdd.gameObject.parseStatsStringSansUserObvi(), ignoreCase = true)
                    && toAdd.gameObject.modelo
                .type != 85 //R�cup�re les stats de l'objet et compare avec ceux de la ligne
                    )
        }

        fun sort(index: Byte) {
            entries[index.toInt()].sort()
        }

        fun addEntry(toAdd: MercadilloEntrada): Boolean {
            if (!haveSameStats(toAdd) && !isEmpty) return false
            toAdd.lineId = lineId
            val index = (toAdd.getAmount(false) - 1).toByte()
            entries[index.toInt()].add(toAdd)
            this.sort(index)
            return true //Anonce que l'objet � �t� accept�
        }

        fun delEntry(toDel: MercadilloEntrada?): Boolean {
            val index = (toDel!!.getAmount(false) - 1).toByte()
            val toReturn = entries[index.toInt()].remove(toDel)
            this.sort(index)
            return toReturn
        }

        fun doYouHave(amount: Int, price: Int): MercadilloEntrada? {
            val index = amount - 1
            for (i in entries[index].indices) if (entries[index][i].price == price
            ) return entries[index][i]
            return null
        }// ok

        //R�cup�re le premier objet de chaque liste
        private val firsts: IntArray
            get() {
                val toReturn = IntArray(3)
                for (i in entries.indices) {
                    try {
                        toReturn[i] =
                            entries[i][0].price //R�cup�re le premier objet de chaque liste
                    } catch (e: IndexOutOfBoundsException) {
                        // ok
                        toReturn[i] = 0
                    }
                }
                return toReturn
            }//Boucler dans les quantit�

        //Additionne le nombre d'objet de chaque quantit�
        val all: ArrayList<MercadilloEntrada?>
            get() {
                val totalSize = (entries[0].size
                        + entries[1].size
                        + entries[2].size) //Additionne le nombre d'objet de chaque quantit�
                val toReturn = ArrayList<MercadilloEntrada?>(totalSize)
                for (qte in entries.indices)  //Boucler dans les quantit�
                    toReturn.addAll(entries[qte])
                return toReturn
            }

        //V�rifie s'il existe un objet dans chacune des 3 quantit�
        val isEmpty: Boolean
            get() {
                if (entries.isEmpty()) return true
                for (i in entries.indices) {
                    try {
                        if (entries[i].isEmpty()) continue
                        if (entries[i][0] != null
                        ) //V�rifie s'il existe un objet dans chacune des 3 quantit�
                            return false
                    } catch (e: IndexOutOfBoundsException) {
                        e.printStackTrace()
                    }
                }
                return true
            }

        fun parseToEHl(): String {
            val toReturn = StringBuilder()
            val price = firsts
            toReturn.append(lineId).append(";").append(strStats).append(";")
                .append(if (price[0] == 0) "" else price[0]).append(";")
                .append(if (price[1] == 0) "" else price[1]).append(";")
                .append(if (price[2] == 0) "" else price[2])
            return toReturn.toString()
        }

        fun parseToEHm(): String {
            val toReturn = StringBuilder()
            val prix = firsts
            toReturn.append(lineId).append("|").append(templateId).append("|")
                .append(strStats).append("|").append(if (prix[0] == 0) "" else prix[0]).append("|")
                .append(if (prix[1] == 0) "" else prix[1]).append("|")
                .append(if (prix[2] == 0) "" else prix[2])
            return toReturn.toString()
        }

        init {
            for (i in 0..2)  //Boucle 3 fois pour ajouter 3 List vide dans la SuperList
                entries.add(ArrayList())
            addEntry(toAdd)
        }
    }

    inner class MercadilloModelo(private val templateId: Int, toAdd: MercadilloEntrada) {
        private val lines: MutableMap<Int, MercadilloLinea> = HashMap()

        fun getLine(lineId: Int): MercadilloLinea? {
            return lines[lineId]
        }

        fun addEntry(toAdd: MercadilloEntrada) {
            for (line in lines.values)  //Boucle dans toutes les lignes pour essayer de trouver des objets de m�mes stats
                if (line.addEntry(toAdd)) //Si une ligne l'accepte, arr�te la m�thode.
                    return
            val lineId = Mundo.mundo.nextLineHdvId
            lines[lineId] = MercadilloLinea(lineId, toAdd)
        }

        fun delEntry(toDel: MercadilloEntrada?) {
            val toReturn = lines[toDel!!.lineId]!!.delEntry(toDel)
            if (lines[toDel.lineId]!!.isEmpty) //Si la ligne est devenue vide
                lines.remove(toDel.lineId)
        }

        val allEntry: ArrayList<MercadilloEntrada?>
            get() {
                val toReturn = ArrayList<MercadilloEntrada?>()
                for (line in lines.values) toReturn.addAll(line.all)
                return toReturn
            }

        val isEmpty: Boolean
            get() = lines.isEmpty()

        fun parseToEHl(): String {
            val toReturn = StringBuilder("$templateId|")
            var isFirst = true
            for (line in lines.values) {
                if (!isFirst) toReturn.append("|")
                toReturn.append(line.parseToEHl())
                isFirst = false
            }
            return toReturn.toString()
        }

        init {
            addEntry(toAdd)
        }
    }

    init {
        var categId: Int
        for (strCategID in strCategory.split(",".toRegex()).toTypedArray()) {
            categId = strCategID.toInt()
            categorys[categId] = MercadilloCategoria(categId)
        }
    }
}