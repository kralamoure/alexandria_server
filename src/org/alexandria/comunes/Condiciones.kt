package org.alexandria.comunes

import org.alexandria.configuracion.Constantes
import org.alexandria.estaticos.Mision
import org.alexandria.estaticos.Mision.MisionEtapa
import org.alexandria.estaticos.cliente.Jugador
import org.alexandria.estaticos.objeto.ObjetoJuego
import org.alexandria.otro.Accion
import org.nfunk.jep.JEP
import org.nfunk.jep.ParseException
import java.time.Instant
import java.util.*

class Condiciones {
    fun validConditions(perso: Jugador?, req: String?): Boolean {
        var req = req
        if (req == null || req == "") return true
        if (req.contains("BI")) return false
        if (perso == null) return false
        val jep = JEP()
        req = req.replace("&", "&&").replace("=", "==").replace("|", "||").replace("!", "!=").replace("~", "==")
        if (req.contains("Sc")) return true
        if (req.contains("Pg")) // C'est les dons que l'on gagne lors des qu�tes d'alignement, connaissance des potions etc ... ce n'est pas encore cod� !
            return false
        if (req.contains("RA")) return haveRA(req, perso)
        if (req.contains("RO")) return haveRO(req, perso)
        if (req.contains("Mph")) return haveMorph(req, perso)
        if (req.contains("PO")) req = havePO(req, perso)
        if (req.contains("PN")) req = canPN(req, perso)
        if (req.contains("PJ")) req = canPJ(req, perso)
        if (req.contains("JOB")) req = haveJOB(req, perso)
        if (req.contains("NPC")) return haveNPC(req, perso)
        if (req.contains("QEt")) return haveQEt(req, perso)
        if (req.contains("QE")) return haveQE(req, perso)
        if (req.contains("QT")) return haveQT(req, perso)
        if (req.contains("Ce")) return haveCe(req, perso)
        if (req.contains("TiT")) return haveTiT(req, perso)
        if (req.contains("Ti")) return haveTi(req, perso)
        if (req.contains("Qa")) return haveQa(req, perso)
        if (req.contains("Pj")) return havePj(req, perso)
        if (req.contains("AM")) return haveMetier(req, perso)
        try {
            //Stats stuff compris
            jep.addVariable("CI", perso.totalStats.getEffect(Constantes.STATS_ADD_INTE).toDouble())
            jep.addVariable("CV", perso.totalStats.getEffect(Constantes.STATS_ADD_VITA).toDouble())
            jep.addVariable("CA", perso.totalStats.getEffect(Constantes.STATS_ADD_AGIL).toDouble())
            jep.addVariable("CW", perso.totalStats.getEffect(Constantes.STATS_ADD_SAGE).toDouble())
            jep.addVariable("CC", perso.totalStats.getEffect(Constantes.STATS_ADD_CHAN).toDouble())
            jep.addVariable("CS", perso.totalStats.getEffect(Constantes.STATS_ADD_FORC).toDouble())
            jep.addVariable("CM", perso.totalStats.getEffect(Constantes.STATS_ADD_PM).toDouble())
            //Stats de bases
            jep.addVariable("Ci", perso.getCaracteristicas().getEffect(Constantes.STATS_ADD_INTE).toDouble())
            jep.addVariable("Cs", perso.getCaracteristicas().getEffect(Constantes.STATS_ADD_FORC).toDouble())
            jep.addVariable("Cv", perso.getCaracteristicas().getEffect(Constantes.STATS_ADD_VITA).toDouble())
            jep.addVariable("Ca", perso.getCaracteristicas().getEffect(Constantes.STATS_ADD_AGIL).toDouble())
            jep.addVariable("Cw", perso.getCaracteristicas().getEffect(Constantes.STATS_ADD_SAGE).toDouble())
            jep.addVariable("Cc", perso.getCaracteristicas().getEffect(Constantes.STATS_ADD_CHAN).toDouble())
            //Autre
            jep.addVariable("Ps", perso._align.toDouble()) //Alignement
            jep.addVariable("Pa", perso.aLvl.toDouble())
            jep.addVariable("PP", perso.grade.toDouble()) //Grade
            jep.addVariable("PL", perso.level.toDouble()) //Niveau
            jep.addVariable("PK", perso.kamas.toDouble()) //Kamas
            jep.addVariable("PG", perso.classe.toDouble()) //Classe
            jep.addVariable("PS", perso.sexe.toDouble()) //Sexe
            jep.addVariable("PZ", 1.0) //Abonnement
            jep.addVariable("PX", perso.groupe != null) //Niveau GM
            jep.addVariable("PW", perso.maximosPods.toDouble()) //MaxPod
            if (perso.curMap.subArea != null) jep.addVariable(
                "PB",
                perso.curMap.subArea.id.toDouble()
            ) //SubArea
            jep.addVariable("PR", (if (perso.wife > 0) 1 else 0).toDouble()) //Mari� ou pas
            jep.addVariable("SI", perso.curMap.id.toDouble()) //Mapid
            jep.addVariable(
                "MiS",
                perso.id.toDouble()
            ) //Les pierres d'ames sont lancables uniquement par le lanceur.
            jep.addVariable("MA", perso.alignMap.toDouble()) //Pandala
            jep.addVariable("PSB", perso.account.points.toDouble()) //Points Boutique
            jep.addVariable(
                "CF",
                (if (perso.getObjetByPos(Constantes.ITEM_POS_PNJ_SUIVEUR) == null) -1 else perso.getObjetByPos(
                    Constantes.ITEM_POS_PNJ_SUIVEUR
                ).modelo.id).toDouble()
            ) //Personnage suiveur
            val node = jep.parse(req)
            val result = jep.evaluate(node) as Double
            return result == 1.0
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }

    private fun haveMorph(c: String, p: Jugador): Boolean {
        if (c.equals("", ignoreCase = true)) return false
        var morph = -1
        try {
            morph = (if (c.contains("==")) c.split("==".toRegex()).toTypedArray()[1] else c.split("!=".toRegex())
                .toTypedArray()[1]).toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return if (p.morphId == morph) c.contains("==") else !c.contains("==")
    }

    private fun haveMetier(c: String?, p: Jugador): Boolean {
        if (p.metiers == null || p.metiers.isEmpty()) return false
        for ((_, value) in p.metiers) {
            if (value != null) return true
        }
        return false
    }

    private fun havePj(c: String?, p: Jugador): Boolean {
        if (c.equals("", ignoreCase = true)) return false
        for (s in c!!.split("\\|\\|".toRegex()).toTypedArray()) {
            val k = s.split("==".toRegex()).toTypedArray()
            var id: Int
            id = try {
                k[1].toInt()
            } catch (e: Exception) {
                e.printStackTrace()
                continue
            }
            if (p.getMetierByID(id) != null) return true
        }
        return false
    }

    //Avoir la qu�te en cours
    private fun haveQa(req: String?, player: Jugador): Boolean {
        val id =
            (if (req!!.contains("==")) req.split("==".toRegex()).toTypedArray()[1] else req.split("!=".toRegex())
                .toTypedArray()[1]).toInt()
        val q = Mision.getQuestById(id) ?: return !req.contains("==")
        val qp = player.getQuestPersoByQuest(q) ?: return !req.contains("==")
        return !qp.isFinish || !req.contains("==")
    }

    // �tre � l'�tape id. Elle ne doit pas �tre valid� et celle d'avant doivent l'�tre.
    private fun haveQEt(req: String?, player: Jugador): Boolean {
        val id =
            (if (req!!.contains("==")) req.split("==".toRegex()).toTypedArray()[1] else req.split("!=".toRegex())
                .toTypedArray()[1]).toInt()
        val qe = MisionEtapa.getQuestStepById(id)
        if (qe != null) {
            val q = qe.questData
            if (q != null) {
                val qp = player.getQuestPersoByQuest(q)
                if (qp != null) {
                    val current = q.getCurrentQuestStep(qp) ?: return false
                    if (current.id == qe.id) return req.contains("==")
                }
            }
        }
        return false
    }

    private fun haveTiT(req: String?, player: Jugador): Boolean {
        if (req!!.contains("==")) {
            val split = req.split("==".toRegex()).toTypedArray()[1]
            if (split.contains("&&")) {
                val item = split.split("&&".toRegex()).toTypedArray()[0].toInt()
                val time = split.split("&&".toRegex()).toTypedArray()[1].toInt()
                val item2 = split.split("&&".toRegex()).toTypedArray()[2].toInt()
                if (player.hasItemTemplate(item2, 1)
                    && player.hasItemTemplate(item, 1)
                ) {
                    val timeStamp =
                        player.getItemTemplate(item, 1).txtStat[Constantes.STATS_DATE]!!.toLong()
                    return Instant.now().toEpochMilli() - timeStamp <= time
                }
            }
        }
        return false
    }

    private fun haveTi(req: String?, player: Jugador): Boolean {
        if (req!!.contains("==")) {
            val split = req.split("==".toRegex()).toTypedArray()[1]
            if (split.contains(",")) {
                val split2 = split.split(",".toRegex()).toTypedArray()
                val item = split2[0].toInt()
                val time = split2[1].toInt() * 60 * 1000
                if (player.hasItemTemplate(item, 1)) {
                    val timeStamp =
                        player.getItemTemplate(item, 1).txtStat[Constantes.STATS_DATE]!!.toLong()
                    return Instant.now().toEpochMilli() - timeStamp > time
                }
            }
        }
        return false
    }

    private fun haveCe(req: String?, player: Jugador): Boolean {
        val dopeuls = Accion.getDopeul()
        val map = player.curMap
        if (dopeuls.containsKey(map.id.toInt())) {
            val couple = dopeuls[map.id.toInt()] ?: return false
            val idmonstruo = couple.getPrimero()
            val certificat = Constantes.getCertificatByDopeuls(idmonstruo)
            if (certificat == -1) return false
            return if (player.hasItemTemplate(certificat, 1)) {
                var txt = player.getItemTemplate(certificat, 1).txtStat[Constantes.STATS_DATE]
                if (txt!!.contains("#")) txt = txt.split("#".toRegex()).toTypedArray()[3]
                val timeStamp = txt.toLong()
                Instant.now().toEpochMilli() - timeStamp > 86400000
            } else true
        }
        return false
    }

    // Avoir la qu�te en cours.
    private fun haveQE(req: String?, player: Jugador?): Boolean {
        if (player == null) return false
        val id =
            (if (req!!.contains("==")) req.split("==".toRegex()).toTypedArray()[1] else req.split("!=".toRegex())
                .toTypedArray()[1]).toInt()
        val qp = player.getQuestPersoByQuestId(id)
        return if (req.contains("==")) {
            qp != null && !qp.isFinish
        } else {
            qp == null || qp.isFinish
        }
    }

    private fun haveQT(req: String?, player: Jugador): Boolean {
        val id =
            (if (req!!.contains("==")) req.split("==".toRegex()).toTypedArray()[1] else req.split("!=".toRegex())
                .toTypedArray()[1]).toInt()
        val quest = player.getQuestPersoByQuestId(id)
        return if (req.contains("==")) quest != null && quest.isFinish else quest == null || !quest.isFinish
    }

    private fun haveNPC(req: String?, perso: Jugador): Boolean {
        when (perso.curMap.id) {
            9052.toShort() -> {
                if (perso.curCell.id == 268
                    && perso._orientation == 7
                ) //TODO
                    return true
                val cell = ArrayList<Int>()
                for (i in "168,197,212,227,242,183,213,214,229,244,245,259".split(",".toRegex())
                    .toTypedArray()) cell.add(i.toInt())
                if (cell.contains(perso.curCell.id)) return true
            }
            8905.toShort() -> {
                val cell = ArrayList<Int>()
                for (i in "168,197,212,227,242,183,213,214,229,244,245,259".split(",".toRegex())
                    .toTypedArray()) cell.add(i.toInt())
                if (cell.contains(perso.curCell.id)) return true
            }
        }
        return false
    }

    private fun haveRO(condition: String, player: Jugador): Boolean {
        try {
            for (cond in condition.split("&&".toRegex()).toTypedArray()) {
                val split =
                    cond.split("==".toRegex()).toTypedArray()[1].split(",".toRegex()).toTypedArray()
                val id = split[0].toInt()
                val qua = split[1].toInt()
                return if (player.hasItemTemplate(id, qua)) {
                    player.removeByTemplateID(id, qua)
                    true
                } else {
                    GestorSalida.GAME_SEND_Im_PACKET(player, "14")
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun haveRA(condition: String, player: Jugador): Boolean {
        try {
            for (cond in condition.split("&&".toRegex()).toTypedArray()) {
                val split =
                    cond.split("==".toRegex()).toTypedArray()[1].split(",".toRegex()).toTypedArray()
                val id = split[0].toInt()
                val qua = split[1].toInt()
                if (!player.hasItemTemplate(id, qua)) return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    private fun havePO(
        cond: String,
        perso: Jugador
    ): String //On remplace les PO par leurs valeurs si possession de l'item
    {
        var Jump = false
        var ContainsPO = false
        var CutFinalLenght = true
        var copyCond = StringBuilder()
        var finalLength = 0
        if (cond.contains("&&")) {
            for (cur in cond.split("&&".toRegex()).toTypedArray()) {
                if (cond.contains("==")) {
                    for (cur2 in cur.split("==".toRegex()).toTypedArray()) {
                        if (cur2.contains("PO")) {
                            ContainsPO = true
                            continue
                        }
                        if (Jump) {
                            copyCond.append(cur2)
                            Jump = false
                            continue
                        }
                        if (!cur2.contains("PO") && !ContainsPO) {
                            copyCond.append(cur2).append("==")
                            Jump = true
                            continue
                        }
                        if (cur2.contains("!=")) continue
                        ContainsPO = false
                        if (perso.hasItemTemplate(cur2.toInt(), 1)) {
                            copyCond.append(cur2.toInt()).append("==").append(cur2.toInt())
                        } else {
                            copyCond.append(cur2.toInt()).append("==").append(0)
                        }
                    }
                }
                if (cond.contains("!=")) {
                    for (cur2 in cur.split("!=".toRegex()).toTypedArray()) {
                        if (cur2.contains("PO")) {
                            ContainsPO = true
                            continue
                        }
                        if (Jump) {
                            copyCond.append(cur2)
                            Jump = false
                            continue
                        }
                        if (!cur2.contains("PO") && !ContainsPO) {
                            copyCond.append(cur2).append("!=")
                            Jump = true
                            continue
                        }
                        if (cur2.contains("==")) continue
                        ContainsPO = false
                        if (perso.hasItemTemplate(cur2.toInt(), 1)) {
                            copyCond.append(cur2.toInt()).append("!=").append(cur2.toInt())
                        } else {
                            copyCond.append(cur2.toInt()).append("!=").append(0)
                        }
                    }
                }
                copyCond.append("&&")
            }
        } else if (cond.contains("||")) {
            for (cur in cond.split("\\|\\|".toRegex()).toTypedArray()) {
                if (cond.contains("==")) {
                    for (cur2 in cur.split("==".toRegex()).toTypedArray()) {
                        if (cur2.contains("PO")) {
                            ContainsPO = true
                            continue
                        }
                        if (Jump) {
                            copyCond.append(cur2)
                            Jump = false
                            continue
                        }
                        if (!cur2.contains("PO") && !ContainsPO) {
                            copyCond.append(cur2).append("==")
                            Jump = true
                            continue
                        }
                        if (cur2.contains("!=")) continue
                        ContainsPO = false
                        if (perso.hasItemTemplate(cur2.toInt(), 1)) {
                            copyCond.append(cur2.toInt()).append("==").append(cur2.toInt())
                        } else {
                            copyCond.append(cur2.toInt()).append("==").append(0)
                        }
                    }
                }
                if (cond.contains("!=")) {
                    for (cur2 in cur.split("!=".toRegex()).toTypedArray()) {
                        if (cur2.contains("PO")) {
                            ContainsPO = true
                            continue
                        }
                        if (Jump) {
                            copyCond.append(cur2)
                            Jump = false
                            continue
                        }
                        if (!cur2.contains("PO") && !ContainsPO) {
                            copyCond.append(cur2).append("!=")
                            Jump = true
                            continue
                        }
                        if (cur2.contains("==")) continue
                        ContainsPO = false
                        if (perso.hasItemTemplate(cur2.toInt(), 1)) {
                            copyCond.append(cur2.toInt()).append("!=").append(cur2.toInt())
                        } else {
                            copyCond.append(cur2.toInt()).append("!=").append(0)
                        }
                    }
                }
                copyCond.append("||")
            }
        } else {
            CutFinalLenght = false
            if (cond.contains("==")) {
                for (cur in cond.split("==".toRegex()).toTypedArray()) {
                    if (cur.contains("PO")) continue
                    if (cur.contains("!=")) continue
                    if (perso.hasItemTemplate(cur.toInt(), 1)) copyCond.append(cur.toInt()).append("==")
                        .append(cur.toInt()) else copyCond.append(cur.toInt()).append("==").append(0)
                }
            }
            if (cond.contains("!=")) {
                for (cur in cond.split("!=".toRegex()).toTypedArray()) {
                    if (cur.contains("PO")) continue
                    if (cur.contains("==")) continue
                    if (perso.hasItemTemplate(cur.toInt(), 1)) copyCond.append(cur.toInt()).append("!=")
                        .append(cur.toInt()) else copyCond.append(cur.toInt()).append("!=").append(0)
                }
            }
        }
        if (CutFinalLenght) {
            finalLength = copyCond.length - 2 //On retire les deux derniers carract�res (|| ou &&)
            copyCond = StringBuilder(copyCond.substring(0, finalLength))
        }
        return copyCond.toString()
    }

    private fun canPN(
        cond: String?,
        perso: Jugador
    ): String //On remplace le PN par 1 et si le nom correspond == 1 sinon == 0
    {
        val copyCond = StringBuilder()
        for (cur in cond!!.split("==".toRegex()).toTypedArray()) {
            if (cur.contains("PN")) {
                copyCond.append("1==")
                continue
            }
            if (perso.name.toLowerCase().compareTo(cur) == 0) copyCond.append("1") else copyCond.append("0")
        }
        return copyCond.toString()
    }

    private fun canPJ(
        cond: String?,
        perso: Jugador
    ): String //On remplace le PJ par 1 et si le metier correspond == 1 sinon == 0
    {
        var copyCond = StringBuilder()
        if (cond!!.contains("==")) {
            val cur = cond.split("==".toRegex()).toTypedArray()
            copyCond = if (perso.getMetierByID(cur[1].toInt()) != null) StringBuilder("1==1") else StringBuilder("1==0")
        } else if (cond.contains(">")) {
            if (cond.contains("||")) {
                for (cur in cond.split("\\|\\|".toRegex()).toTypedArray()) {
                    if (!cur.contains(">")) continue
                    val _cur = cur.split(">".toRegex()).toTypedArray()
                    if (!_cur[1].contains(",")) continue
                    val m = _cur[1].split(",".toRegex()).toTypedArray()
                    val js = perso.getMetierByID(m[0].toInt())
                    if (!copyCond.toString().equals("", ignoreCase = true)) copyCond.append("||")
                    if (js != null) copyCond.append(js._lvl).append(">")
                        .append(m[1]) else copyCond.append("1==0")
                }
            } else {
                val cur = cond.split(">".toRegex()).toTypedArray()
                val m = cur[1].split(",".toRegex()).toTypedArray()
                val js = perso.getMetierByID(m[0].toInt())
                copyCond = if (js != null) StringBuilder(js._lvl.toString() + ">" + m[1]) else StringBuilder("1==0")
            }
        }
        return copyCond.toString()
    }

    private fun haveJOB(cond: String?, perso: Jugador): String {
        var copyCond = ""
        copyCond =
            if (perso.getMetierByID(cond!!.split("==".toRegex()).toTypedArray()[1].toInt()) != null) "1==1" else "0==1"
        return copyCond
    }

    fun stackIfSimilar(obj: ObjetoJuego, newObj: ObjetoJuego, stackIfSimilar: Boolean): Boolean {
        when (obj.modelo.id) {
            10275, 8378 -> if (obj.modelo.id == newObj.modelo.id) return false
        }
        return (obj.modelo.id == newObj.modelo
            .id && stackIfSimilar && obj.isSameStats(newObj) && !Constantes.isIncarnationWeapon(
            newObj.modelo.id
        )
                && newObj.modelo.type != Constantes.ITEM_TYPE_CERTIFICAT_CHANIL && newObj.modelo.type != Constantes.ITEM_TYPE_PIERRE_AME_PLEINE && newObj.modelo.type != Constantes.ITEM_TYPE_OBJET_ELEVAGE && newObj.modelo.type != Constantes.ITEM_TYPE_CERTIF_MONTURE && newObj.modelo.type != Constantes.ITEM_TYPE_OBJET_VIVANT && (newObj.modelo.type != Constantes.ITEM_TYPE_QUETES || Constantes.isFlacGelee(
            obj.modelo.id
        ) || Constantes.isDoplon(obj.modelo.id))
                && obj.posicion == Constantes.ITEM_POS_NO_EQUIPED)
    }
}