package org.alexandria.comunes.gestorsql.dinamicos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.estaticos.cliente.Jugador
import org.alexandria.estaticos.comandos.administracion.GrupoADM
import org.alexandria.comunes.gestorsql.Database
import org.alexandria.comunes.gestorsql.dinamicos.AbstractDAO
import org.alexandria.configuracion.Configuracion.SERVER_ID
import org.alexandria.configuracion.Constantes
import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.estaticos.Mision.MisionJugador
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*

class DatosPersonajes(dataSource: HikariDataSource?) :
    AbstractDAO<Jugador>(dataSource!!) {
    val nextId: Int
        get() {
            var result: Result? = null
            var guid = 0
            try {
                result = getData("SELECT id FROM players ORDER BY id DESC LIMIT 1")
                val rs = result!!.resultSet
                guid = if (!rs!!.first()) 1 else rs.getInt("id") + 1
            } catch (e: SQLException) {
                super.sendError("PlayerData getNextId", e)
            } finally {
                close(result)
            }
            return guid
        }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM players")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                if (rs.getInt("server") != SERVER_ID) continue
                val stats = HashMap<Int, Int>()
                stats[Constantes.STATS_ADD_VITA] = rs.getInt("vitalite")
                stats[Constantes.STATS_ADD_FORC] = rs.getInt("force")
                stats[Constantes.STATS_ADD_SAGE] = rs.getInt("sagesse")
                stats[Constantes.STATS_ADD_INTE] = rs.getInt("intelligence")
                stats[Constantes.STATS_ADD_CHAN] = rs.getInt("chance")
                stats[Constantes.STATS_ADD_AGIL] = rs.getInt("agilite")
                val perso = Jugador(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("groupe"),
                    rs.getInt("sexe"),
                    rs.getInt("class"),
                    rs.getInt("color1"),
                    rs.getInt("color2"),
                    rs.getInt("color3"),
                    rs.getLong("kamas"),
                    rs.getInt("spellboost"),
                    rs.getInt("capital"),
                    rs.getInt("energy"),
                    rs.getInt("level"),
                    rs.getLong("xp"),
                    rs.getInt("size"),
                    rs.getInt("gfx"),
                    rs.getByte("alignement"),
                    rs.getInt("account"),
                    stats,
                    rs.getByte("seeFriend"),
                    rs.getByte("seeAlign"),
                    rs.getByte("seeSeller"),
                    rs.getString("canaux"),
                    rs.getShort("map"),
                    rs.getInt("cell"),
                    rs.getString("objets"),
                    rs.getString("storeObjets"),
                    rs.getInt("pdvper"),
                    rs.getString("spells"),
                    rs.getString("savepos"),
                    rs.getString("jobs"),
                    rs.getInt("mountxpgive"),
                    rs.getInt("mount"),
                    rs.getInt("honor"),
                    rs.getInt("deshonor"),
                    rs.getInt("alvl"),
                    rs.getString("zaaps"),
                    rs.getByte("title"),
                    rs.getInt("wife"),
                    rs.getString("morphMode"),
                    rs.getString("allTitle"),
                    rs.getString("emotes"),
                    rs.getLong("prison"),
                    false,
                    rs.getString("parcho"),
                    rs.getLong("timeDeblo"),
                    rs.getBoolean("noall"),
                    rs.getString("deadInformation"),
                    rs.getByte("deathCount"),
                    rs.getLong("totalKills")
                )
                perso.VerifAndChangeItemPlace()
                Mundo.mundo.addPlayer(perso)
                if (perso.isShowSeller) Mundo.mundo.addSeller(perso)
            }
        } catch (e: SQLException) {
            super.sendError("PlayerData load", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }

    fun load(obj: Int) {
        var result: Result? = null
        var player: Jugador? = null
        try {
            result = getData("SELECT * FROM players WHERE id = '$obj'")
            val RS = result!!.resultSet
            while (RS!!.next()) {
                if (RS.getInt("server") != SERVER_ID) continue
                val stats = HashMap<Int, Int>()
                stats[Constantes.STATS_ADD_VITA] = RS.getInt("vitalite")
                stats[Constantes.STATS_ADD_FORC] = RS.getInt("force")
                stats[Constantes.STATS_ADD_SAGE] = RS.getInt("sagesse")
                stats[Constantes.STATS_ADD_INTE] = RS.getInt("intelligence")
                stats[Constantes.STATS_ADD_CHAN] = RS.getInt("chance")
                stats[Constantes.STATS_ADD_AGIL] = RS.getInt("agilite")
                val oldPlayer = Mundo.mundo.getPlayer(obj)
                player = Jugador(
                    RS.getInt("id"),
                    RS.getString("name"),
                    RS.getInt("groupe"),
                    RS.getInt("sexe"),
                    RS.getInt("class"),
                    RS.getInt("color1"),
                    RS.getInt("color2"),
                    RS.getInt("color3"),
                    RS.getLong("kamas"),
                    RS.getInt("spellboost"),
                    RS.getInt("capital"),
                    RS.getInt("energy"),
                    RS.getInt("level"),
                    RS.getLong("xp"),
                    RS.getInt("size"),
                    RS.getInt("gfx"),
                    RS.getByte("alignement"),
                    RS.getInt("account"),
                    stats,
                    RS.getByte("seeFriend"),
                    RS.getByte("seeAlign"),
                    RS.getByte("seeSeller"),
                    RS.getString("canaux"),
                    RS.getShort("map"),
                    RS.getInt("cell"),
                    RS.getString("objets"),
                    RS.getString("storeObjets"),
                    RS.getInt("pdvper"),
                    RS.getString("spells"),
                    RS.getString("savepos"),
                    RS.getString("jobs"),
                    RS.getInt("mountxpgive"),
                    RS.getInt("mount"),
                    RS.getInt("honor"),
                    RS.getInt("deshonor"),
                    RS.getInt("alvl"),
                    RS.getString("zaaps"),
                    RS.getByte("title"),
                    RS.getInt("wife"),
                    RS.getString("morphMode"),
                    RS.getString("allTitle"),
                    RS.getString("emotes"),
                    RS.getLong("prison"),
                    false,
                    RS.getString("parcho"),
                    RS.getLong("timeDeblo"),
                    RS.getBoolean("noall"),
                    RS.getString("deadInformation"),
                    RS.getByte("deathCount"),
                    RS.getLong("totalKills")
                )
                if (oldPlayer != null) player.setNeededEndFight(oldPlayer.needEndFight(), oldPlayer.hasMobGroup())
                player.VerifAndChangeItemPlace()
                Mundo.mundo.addPlayer(player)
                val guild = Database.estaticos.guildMemberData!!.isPersoInGuild(RS.getInt("id"))
                if (guild >= 0) player.guildMember = Mundo.mundo.getGuild(guild).getMember(RS.getInt("id"))
            }
        } catch (e: SQLException) {
            super.sendError("PlayerData load id", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }

    fun loadByAccountId(id: Int) {
        try {
            val account = Mundo.mundo.getAccount(id)
            if (account != null) if (account.players != null) account.players.values.stream()
                .filter { obj: Jugador? -> Objects.nonNull(obj) }.forEach { p: Jugador? -> Mundo.mundo.verifyClone(p) }
        } catch (e: Exception) {
            super.sendError("PlayerData loadByAccountId clone", e)
        }
        var result: Result? = null
        try {
            result = getData("SELECT * FROM players WHERE account = '$id'")
            val RS = result!!.resultSet
            while (RS!!.next()) {
                if (RS.getInt("server") != SERVER_ID) continue
                val p = Mundo.mundo.getPlayer(RS.getInt("id"))
                if (p != null) {
                    if (p.pelea != null) {
                        continue
                    }
                }
                val stats = HashMap<Int, Int>()
                stats[Constantes.STATS_ADD_VITA] = RS.getInt("vitalite")
                stats[Constantes.STATS_ADD_FORC] = RS.getInt("force")
                stats[Constantes.STATS_ADD_SAGE] = RS.getInt("sagesse")
                stats[Constantes.STATS_ADD_INTE] = RS.getInt("intelligence")
                stats[Constantes.STATS_ADD_CHAN] = RS.getInt("chance")
                stats[Constantes.STATS_ADD_AGIL] = RS.getInt("agilite")
                val player = Jugador(
                    RS.getInt("id"),
                    RS.getString("name"),
                    RS.getInt("groupe"),
                    RS.getInt("sexe"),
                    RS.getInt("class"),
                    RS.getInt("color1"),
                    RS.getInt("color2"),
                    RS.getInt("color3"),
                    RS.getLong("kamas"),
                    RS.getInt("spellboost"),
                    RS.getInt("capital"),
                    RS.getInt("energy"),
                    RS.getInt("level"),
                    RS.getLong("xp"),
                    RS.getInt("size"),
                    RS.getInt("gfx"),
                    RS.getByte("alignement"),
                    RS.getInt("account"),
                    stats,
                    RS.getByte("seeFriend"),
                    RS.getByte("seeAlign"),
                    RS.getByte("seeSeller"),
                    RS.getString("canaux"),
                    RS.getShort("map"),
                    RS.getInt("cell"),
                    RS.getString("objets"),
                    RS.getString("storeObjets"),
                    RS.getInt("pdvper"),
                    RS.getString("spells"),
                    RS.getString("savepos"),
                    RS.getString("jobs"),
                    RS.getInt("mountxpgive"),
                    RS.getInt("mount"),
                    RS.getInt("honor"),
                    RS.getInt("deshonor"),
                    RS.getInt("alvl"),
                    RS.getString("zaaps"),
                    RS.getByte("title"),
                    RS.getInt("wife"),
                    RS.getString("morphMode"),
                    RS.getString("allTitle"),
                    RS.getString("emotes"),
                    RS.getLong("prison"),
                    false,
                    RS.getString("parcho"),
                    RS.getLong("timeDeblo"),
                    RS.getBoolean("noall"),
                    RS.getString("deadInformation"),
                    RS.getByte("deathCount"),
                    RS.getLong("totalKills")
                )
                if (p != null) player.setNeededEndFight(p.needEndFight(), p.hasMobGroup())
                player.VerifAndChangeItemPlace()
                Mundo.mundo.addPlayer(player)
                val guild = Database.estaticos.guildMemberData!!.isPersoInGuild(RS.getInt("id"))
                if (guild >= 0) player.guildMember = Mundo.mundo.getGuild(guild).getMember(RS.getInt("id"))
            }
        } catch (e: SQLException) {
            super.sendError("PlayerData loadByAccountId", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }

    fun loadTitles(guid: Int): String {
        var result: Result? = null
        var title = ""
        try {
            result = getData("SELECT * FROM players WHERE id = '$guid';")
            val RS = result!!.resultSet
            if (RS!!.next()) {
                title = RS.getString("allTitle")
            }
        } catch (e: SQLException) {
            super.sendError("PlayerData loadTitles", e)
        } finally {
            close(result)
        }
        return title
    }

    fun add(perso: Jugador): Boolean {
        var p: PreparedStatement? = null
        try {
            p =
                getPreparedStatement("INSERT INTO players(`id`, `name`, `sexe`, `class`, `color1`, `color2`, `color3`, `kamas`, `spellboost`, `capital`, `energy`, `level`, `xp`, `size`, `gfx`, `account`, `cell`, `map`, `spells`, `objets`, `storeObjets`, `morphMode`, `server`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'','','0',?)")
            p!!.setInt(1, perso.id)
            p.setString(2, perso.name)
            p.setInt(3, perso.sexe)
            p.setInt(4, perso.classe)
            p.setInt(5, perso.color1)
            p.setInt(6, perso.color2)
            p.setInt(7, perso.color3)
            p.setLong(8, perso.kamas)
            p.setInt(9, perso._spellPts)
            p.setInt(10, perso._capital)
            p.setInt(11, perso.energy)
            p.setInt(12, perso.level)
            p.setLong(13, perso.exp)
            p.setInt(14, perso._size)
            p.setInt(15, perso.gfxId)
            p.setInt(16, perso.accID)
            p.setInt(17, perso.curCell.id)
            p.setInt(18, perso.curMap.id.toInt())
            p.setString(19, perso.parseSpellToDB())
            p.setInt(20, SERVER_ID)
            execute(p)
            return true
        } catch (e: SQLException) {
            super.sendError("PlayerData add", e)
        } finally {
            close(p)
        }
        return false
    }

    fun delete(perso: Jugador) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("DELETE FROM players WHERE id = ?")
            p!!.setInt(1, perso.id)
            execute(p)
            if (perso.getItemsIDSplitByChar(",") != "") for (id in perso.getItemsIDSplitByChar(",")
                .split(",".toRegex()).toTypedArray()) Database.dinamicos.objectData!!.delete(id.toInt())
            if (perso.getStoreItemsIDSplitByChar(",") != "") for (id in perso.getStoreItemsIDSplitByChar(
                ","
            ).split(",".toRegex()).toTypedArray()) Database.dinamicos.objectData!!.delete(id.toInt())
            if (perso.mount != null) Database.dinamicos.mountData!!.update(perso.mount)
        } catch (e: SQLException) {
            super.sendError("PlayerData delete", e)
        } finally {
            close(p)
        }
    }

    override fun load(obj: Any?) {}
    override fun update(player: Jugador): Boolean {
        if (player == null) {
            super.sendError("PlayerData update", Exception("perso is null"))
            return false
        }
        var p: PreparedStatement? = null
        try {
            p =
                getPreparedStatement("UPDATE `players` SET `kamas`= ?, `spellboost`= ?, `capital`= ?, `energy`= ?, `level`= ?, `xp`= ?, `size` = ?, `gfx`= ?, `alignement`= ?, `honor`= ?, `deshonor`= ?, `alvl`= ?, `vitalite`= ?, `force`= ?, `sagesse`= ?, `intelligence`= ?, `chance`= ?, `agilite`= ?, `seeFriend`= ?, `seeAlign`= ?, `seeSeller`= ?, `canaux`= ?, `map`= ?, `cell`= ?, `pdvper`= ?, `spells`= ?, `objets`= ?, `storeObjets`= ?, `savepos`= ?, `zaaps`= ?, `jobs`= ?, `mountxpgive`= ?, `mount`= ?, `title`= ?, `wife`= ?, `morphMode`= ?, `allTitle` = ?, `emotes` = ?, `prison` = ?, `parcho` = ?, `timeDeblo` = ?, `noall` = ?, `deadInformation` = ?, `deathCount` = ?, `totalKills` = ? WHERE `players`.`id` = ? LIMIT 1")
            p!!.setLong(1, player.kamas)
            p.setInt(2, player._spellPts)
            p.setInt(3, player._capital)
            p.setInt(4, player.energy)
            p.setInt(5, player.level)
            p.setLong(6, player.exp)
            p.setInt(7, player._size)
            p.setInt(8, player.gfxId)
            p.setInt(9, player._align.toInt())
            p.setInt(10, player._honor)
            p.setInt(11, player.deshonor)
            p.setInt(12, player.aLvl)
            p.setInt(13, player.caracteristicas.getEffect(Constantes.STATS_ADD_VITA))
            p.setInt(14, player.caracteristicas.getEffect(Constantes.STATS_ADD_FORC))
            p.setInt(15, player.caracteristicas.getEffect(Constantes.STATS_ADD_SAGE))
            p.setInt(16, player.caracteristicas.getEffect(Constantes.STATS_ADD_INTE))
            p.setInt(17, player.caracteristicas.getEffect(Constantes.STATS_ADD_CHAN))
            p.setInt(18, player.caracteristicas.getEffect(Constantes.STATS_ADD_AGIL))
            p.setInt(19, if (player.is_showFriendConnection) 1 else 0)
            p.setInt(20, if (player.is_showWings) 1 else 0)
            p.setInt(21, if (player.isShowSeller) 1 else 0)
            p.setString(22, player._canaux)
            if (player.curMap != null) p.setInt(23, player.curMap.id.toInt()) else p.setInt(23, 7411)
            if (player.curCell != null) p.setInt(24, player.curCell.id) else p.setInt(24, 311)
            p.setInt(25, player._pdvper)
            p.setString(26, player.parseSpellToDB())
            p.setString(27, player.parseObjetsToDB())
            p.setString(28, player.parseStoreItemstoBD())
            p.setString(29, player.savePosition)
            p.setString(30, player.parseZaaps())
            p.setString(31, player.parseJobData())
            p.setInt(32, player.mountXpGive)
            p.setInt(33, if (player.mount != null) player.mount.id else -1)
            p.setByte(34, player._title)
            p.setInt(35, player.wife)
            p.setString(
                36, (if (player.morphMode) 1 else 0).toString() + ";"
                        + player.morphId
            )
            p.setString(37, player.allTitle)
            p.setString(38, player.parseEmoteToDB())
            p.setLong(39, if (player.isInEnnemyFaction) player.enteredOnEnnemyFaction else 0)
            p.setString(40, player.parseStatsParcho())
            p.setLong(41, player.timeTaverne)
            p.setBoolean(42, player.noall)
            p.setString(43, player.deathInformation)
            p.setByte(44, player.deathCount)
            p.setLong(45, player.totalKills)
            p.setInt(46, player.id)
            execute(p)
            if (player.guildMember != null) Database.estaticos.guildMemberData!!.update(player)
            if (player.mount != null) Database.dinamicos.mountData!!.update(player.mount)
        } catch (e: Exception) {
            super.sendError("PlayerData update", e)
        } finally {
            close(p)
        }
        if (player.questPerso != null && !player.questPerso.isEmpty()) player.questPerso.values.stream()
            .filter { obj: MisionJugador? -> Objects.nonNull(obj) }.forEach { QP: MisionJugador ->
                Database.dinamicos.questPlayerData!!.update(
                    QP,
                    player
                )
            }
        return true
    }

    fun updateInfos(perso: Jugador) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `players` SET `name` = ?, `sexe`=?, `class`= ?, `spells`= ? WHERE `id`= ?")
            p!!.setString(1, perso.name)
            p.setInt(2, perso.sexe)
            p.setInt(3, perso.classe)
            p.setString(4, perso.parseSpellToDB())
            p.setInt(5, perso.id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("PlayerData updateInfos", e)
        } finally {
            close(p)
        }
    }

    fun updateGroupe(group: Int, name: String?) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `players` SET `groupe` = ? WHERE `name` = ?;")
            p!!.setInt(1, group)
            p.setString(2, name)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("PlayerData updateGroupe", e)
        } finally {
            close(p)
        }
    }

    fun updateGroupe(perso: Jugador) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `players` SET `groupe` = ? WHERE `id`= ?")
            val id = if (perso.groupe != null) perso.groupe.id else -1
            p!!.setInt(1, id)
            p.setInt(2, perso.id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("PlayerData updateGroupe", e)
        } finally {
            close(p)
        }
    }

    fun updateTimeTaverne(player: Jugador) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE players SET `timeDeblo` = ? WHERE `id` = ?")
            p!!.setLong(1, player.timeTaverne)
            p.setInt(2, player.id)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("PlayerData updateTimeDeblo", e)
        } finally {
            close(p)
        }
    }

    fun updateTitles(guid: Int, title: String?) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE players SET `allTitle` = ? WHERE `id` = ?")
            p!!.setString(1, title)
            p.setInt(2, guid)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("PlayerData updateTitles", e)
        } finally {
            close(p)
        }
    }

    fun updateLogged(guid: Int, logged: Int) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE players SET `logged` = ? WHERE `id` = ?")
            p!!.setInt(1, logged)
            p.setInt(2, guid)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("PlayerData updateLogged", e)
        } finally {
            close(p)
        }
    }

    fun updateAllLogged(guid: Int, logged: Int) {
        var p: PreparedStatement? = null
        try {
            p = getPreparedStatement("UPDATE `players` SET `logged` = ? WHERE `account` = ?")
            p!!.setInt(1, logged)
            p.setInt(2, guid)
            execute(p)
        } catch (e: SQLException) {
            super.sendError("PlayerData updateAllLogged", e)
        } finally {
            close(p)
        }
    }

    fun exist(name: String): Boolean {
        var result: Result? = null
        var exist = false
        try {
            result = getData("SELECT COUNT(*) AS exist FROM players WHERE name LIKE '$name';")
            val RS = result!!.resultSet
            if (RS!!.next()) {
                if (RS.getInt("exist") > 0) exist = true
            }
        } catch (e: SQLException) {
            super.sendError("PlayerData exist", e)
        } finally {
            close(result)
        }
        return exist
    }

    fun haveOtherPlayer(account: Int): String {
        var result: Result? = null
        val servers = StringBuilder()
        try {
            result = getData(
                "SELECT server FROM players WHERE account = '"
                        + account + "' AND NOT server = '" + SERVER_ID + "'"
            )
            val RS = result!!.resultSet
            while (RS!!.next()) {
                servers.append(
                    if (servers.length == 0) RS.getInt("server") else ","
                            + RS.getInt("server")
                )
            }
        } catch (e: SQLException) {
            super.sendError("PlayerData haveOtherPlayer", e)
        } finally {
            close(result)
        }
        return servers.toString()
    }

    fun reloadGroup(p: Jugador) {
        var result: Result? = null
        try {
            result = getData(
                "SELECT groupe FROM players WHERE id = '"
                        + p.id + "'"
            )
            val RS = result!!.resultSet
            if (RS!!.next()) {
                val group = RS.getInt("groupe")
                val g = GrupoADM.getGrupoID(group)
                p.setGroupe(g, false)
            }
        } catch (e: SQLException) {
            super.sendError("PlayerData reloadGroup", e)
        } finally {
            close(result)
        }
    }

    fun canRevive(player: Jugador): Byte {
        var result: Result? = null
        var revive: Byte = 0
        try {
            result = getData(
                "SELECT id, revive FROM players WHERE `id` = '"
                        + player.id + "';"
            )
            val RS = result!!.resultSet
            while (RS!!.next()) revive = RS.getByte("revive")
        } catch (e: SQLException) {
            super.sendError("PlayerData canRevive", e)
        } finally {
            close(result)
        }
        return revive
    }

    fun setRevive(player: Jugador) {
        try {
            val p =
                getPreparedStatement("UPDATE players SET `revive` = 0 WHERE `id` = '" + player.id + "';")
            execute(p!!)
            close(p)
        } catch (e: SQLException) {
            super.sendError("PlayerData setRevive", e)
        }
    }
}