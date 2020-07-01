package org.alexandria.estaticos.objeto;

import org.alexandria.estaticos.area.Area;
import org.alexandria.estaticos.area.SubArea;
import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.area.mapa.entrada.Animaciones;
import org.alexandria.estaticos.Casas;
import org.alexandria.estaticos.Cercados;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Constantes;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.dinamicos.Nawidad;
import org.alexandria.estaticos.Mascota.MascotaEntrada;
import org.alexandria.estaticos.Prisma;
import org.alexandria.estaticos.Gremio;
import org.alexandria.estaticos.juego.accion.AccionIntercambiar;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.objeto.entrada.FragmentosMagicos;
import org.alexandria.estaticos.objeto.entrada.PiedraAlma;
import org.alexandria.estaticos.oficio.OficioCaracteristicas;
import org.alexandria.otro.Accion;
import org.alexandria.estaticos.pelea.hechizo.EfectoHechizo;
import org.alexandria.otro.utilidad.Doble;

public class ObjetoAccion {

    private final String type;
    private final String args;
    private final String cond;
    private boolean send = true;

    public ObjetoAccion(String type, String args, String cond) {
        this.type = type;
        this.args = args;
        this.cond = cond;
    }

    public void apply(Jugador player0, Jugador target, int objet, int cellid) {
        if (player0 == null || !player0.isOnline() || player0.getDoAction() || player0.getGameClient() == null)
            return;
        if (!this.cond.equalsIgnoreCase("") && !this.cond.equalsIgnoreCase("-1") && !Mundo.mundo.getConditionManager().validConditions(player0, this.cond)) {
            GestorSalida.GAME_SEND_Im_PACKET(player0, "119");
            return;
        }
        if (player0.getLevel() < Mundo.getGameObject(objet).getModelo().getLevel()) {
            GestorSalida.GAME_SEND_Im_PACKET(player0, "119");
            return;
        }

        Jugador player = target != null ? target : player0;

        if (Mundo.getGameObject(objet) == null) {
            GestorSalida.GAME_SEND_MESSAGE(player, "Error object null. Merci de prévenir un administrateur est d'indiquer le message.");
            return;
        }

        boolean sureIsOk = false, isOk = true;
        int turn = 0;
        String arg = "";
        try {
            for (String type : this.type.split(";")) {
                String[] split = args.split("\\|", 2);
                if (!this.args.isEmpty() && split.length > turn)
                    arg = split[turn];

                switch (Integer.parseInt(type)) {
                    case -1:
                        if (player0.getPelea() != null) return;
                        isOk = true;
                        send = false;
                        break;

                    case 0://T�l�portation.
                        if (player0.getPelea() != null) return;
                        short mapId = Short.parseShort(arg.split(",", 2)[0]);
                        int cellId = Integer.parseInt(arg.split(",", 2)[1]);
                        if(mapId == 8978) {
                            isOk = false;
                            send = false;
                            return;
                        }
                        if (!player.cantTP())
                            player.teleport(mapId, cellId);
                        else if (player.getCurCell().getId() == 268)
                            player.teleport(mapId, cellId);
                        break;

                    case 1://T�l�portation au point de sauvegarde.
                        if (player0.getPelea() != null) return;
                        if (!player.cantTP())
                            player.warpToSavePos();
                        break;

                    case 2://Don de Kamas.
                        if (player0.getPelea() != null) return;
                        int count = Integer.parseInt(arg);
                        long curKamas = player.getKamas();
                        long newKamas = curKamas + count;
                        if (newKamas < 0)
                            newKamas = 0;
                        player.setKamas(newKamas);
                        if (player.isOnline())
                            GestorSalida.GAME_SEND_STATS_PACKET(player);
                        break;

                    case 3://Don de vie.
                        if(this.type.split(";").length > 1 && player.getPelea() != null) return;
                        boolean isOk1 = true,
                                isOk2 = true;
                        for (String arg0 : arg.split(",")) {
                            int val, statId1;
                            if (arg.contains(";")) {
                                statId1 = Integer.parseInt(arg.split(";")[0]);
                                val = Mundo.getGameObject(objet).getRandomValue(Mundo.getGameObject(objet).parseStatsString(), Integer.parseInt(arg.split(";")[0]));
                            } else {
                                statId1 = Integer.parseInt(arg0);
                                val = Mundo.getGameObject(objet).getRandomValue(Mundo.getGameObject(objet).parseStatsString(), Integer.parseInt(arg0));
                            }
                            switch (statId1) {
//Vie.
                                case 110 -> {
                                    if (player.getCurPdv() == player.getMaxPdv()) {
                                        isOk1 = false;
                                        continue;
                                    }
                                    if (player.getCurPdv() + val > player.getMaxPdv())
                                        val = player.getMaxPdv() - player.getCurPdv();
                                    player.setPdv(player.getCurPdv() + val);
                                    if (player.getPelea() != null)
                                        player.getPelea().getFighterByPerso(player).setPdv(player.getCurPdv());
                                    GestorSalida.GAME_SEND_STATS_PACKET(player);
                                    GestorSalida.GAME_SEND_Im_PACKET(player, "01;" + val);
                                    sureIsOk = true;
                                }
//Energie.
                                case 139 -> {
                                    if (player.getEnergy() == 10000) {
                                        isOk2 = false;
                                        continue;
                                    }
                                    if (player.getEnergy() + val > 10000)
                                        val = 10000 - player.getEnergy();
                                    player.setEnergy(player.getEnergy() + val);
                                    GestorSalida.GAME_SEND_STATS_PACKET(player);
                                    GestorSalida.GAME_SEND_Im_PACKET(player, "07;" + val);
                                    sureIsOk = true;
                                }
//Exp�rience.
                                case 605 -> {
                                    player.addXp(val);
                                    GestorSalida.GAME_SEND_STATS_PACKET(player);
                                    GestorSalida.GAME_SEND_Im_PACKET(player, "08;" + val);
                                }
//Exp�rience m�tier.
                                case 614 -> {
                                    OficioCaracteristicas job = player.getMetierByID(Integer.parseInt(arg0.split(";")[1]));
                                    if (job == null) {
                                        isOk1 = false;
                                        isOk2 = false;
                                        continue;
                                    }
                                    job.addXp(player, val);
                                    GestorSalida.GAME_SEND_Im_PACKET(player, "017;" + val + "~" + Integer.parseInt(arg0.split(";")[1]));
                                    sureIsOk = true;
                                }
                            }
                        }
                        if (arg.split(",").length == 1)
                            if (!isOk1 || !isOk2)
                                isOk = false;
                            else if (!isOk1 && !isOk2)
                                isOk = false;
                        send = false;
                        break;

                    case 4://Don de Stats.
                        if (player0.getPelea() != null) return;
                        for (String arg0 : arg.split(",")) {
                            int statId = Integer.parseInt(arg0.split(";")[0]);
                            int val = Integer.parseInt(arg0.split(";")[1]);
                            switch (statId) {
                                case 1://Vitalit�.
                                    for (int i = 0; i < val; i++) {
                                        player.boostStat(11, false);
                                        player.getStatsParcho().addOneStat(Constantes.STATS_ADD_VITA, 1);
                                    }
                                    break;
                                case 2://Sagesse.
                                    for (int i = 0; i < val; i++) {
                                        player.getStatsParcho().addOneStat(Constantes.STATS_ADD_SAGE, 1);
                                        player.boostStat(12, false);
                                    }
                                    break;
                                case 3://Force.
                                    for (int i = 0; i < val; i++) {
                                        player.boostStat(10, false);
                                        player.getStatsParcho().addOneStat(Constantes.STATS_ADD_FORC, 1);
                                    }
                                    break;
                                case 4://Intelligence.
                                    for (int i = 0; i < val; i++) {
                                        player.boostStat(15, false);
                                        player.getStatsParcho().addOneStat(Constantes.STATS_ADD_INTE, 1);
                                    }
                                    break;
                                case 5://Chance.
                                    for (int i = 0; i < val; i++) {
                                        player.boostStat(13, false);
                                        player.getStatsParcho().addOneStat(Constantes.STATS_ADD_CHAN, 1);
                                    }
                                    break;
                                case 6://Agilit�.
                                    for (int i = 0; i < val; i++) {
                                        player.boostStat(14, false);
                                        player.getStatsParcho().addOneStat(Constantes.STATS_ADD_AGIL, 1);
                                    }
                                    break;
                                case 7://Point de Sort.
                                    player.set_spellPts(player.get_spellPts()
                                            + val);
                                    break;
                            }
                        }
                        sureIsOk = true;
                        GestorSalida.GAME_SEND_STATS_PACKET(player);
                        break;

                    case 5://F�e d'artifice.
                        if (player0.getPelea() != null) return;
                        int id0 = Integer.parseInt(arg);
                        Animaciones anim = Mundo.mundo.getAnimation(id0);
                        if (player.getPelea() != null)
                            return;
                        player.changeOrientation(1);
                        GestorSalida.GAME_SEND_GA_PACKET_TO_MAP(player.getCurMap(), "0", 228, player.getId() + ";" + cellid + "," + anim.prepareToGA(), "");
                        break;

                    case 6://Apprendre un sort.
                        if (player0.getPelea() != null) return;
                        id0 = Integer.parseInt(arg);
                        if (Mundo.mundo.getSort(id0) == null)
                            return;
                        if (!player.learnSpell(id0, 1, true, true, true))
                            return;
                        send = false;
                        break;

                    case 7://D�sapprendre un sort.
                        if (player0.getPelea() != null) return;
                        id0 = Integer.parseInt(arg);
                        int oldLevel = player.getSortStatBySortIfHas(id0).getLevel();
                        if (player.getSortStatBySortIfHas(id0) == null)
                            return;
                        if (oldLevel <= 1)
                            return;
                        player.unlearnSpell(player, id0, 1, oldLevel, true, true);
                        break;

                    case 8://D�sapprendre un sort � un percepteur.
                        final Gremio guild = player0.getGuild();
                        if (player0.getPelea() != null || guild == null || player0.getGuildMember() == null) return;

                        ObjetoJuego obj = Mundo.getGameObject(objet);

                        if(obj != null) {
                            int spell = obj.getCaracteristicas().get(Constantes.STATS_FORGET_ONE_LEVEL_SPELL);

                            if(spell != 0) {
                                if (spell <= 4) {
                                    int quantity = -1;
                                    switch (spell) {
// Pods
                                        case 1 -> quantity = guild.resetStats(158);
// Nb collectors
                                        case 2 -> {
                                            quantity = guild.getNbCollectors();
                                            guild.setNbCollectors(0);
                                            guild.setCapital(guild.getCapital() + quantity * 10);
                                            quantity = -1;
                                        }
// Prospection
                                        case 3 -> quantity = guild.resetStats(176);
// Sagesse
                                        case 4 -> quantity = guild.resetStats(124);
                                    }
                                    if (quantity != -1) {
                                        guild.setCapital(guild.getCapital() + quantity);
                                    }
                                } else {
                                    guild.unBoostSpell(spell);
                                }
                                isOk = true;
                                send = true;
                                Database.dinamicos.getGuildData().update(guild);
                                GestorSalida.GAME_SEND_gIB_PACKET(player0, guild.parseCollectorToGuild());
                                break;
                            }
                        }
                        isOk = false;
                        send = false;
                        break;

                    case 9://Oubli� un m�tier.
                        if (player0.getPelea() != null) return;
                        int job = Integer.parseInt(arg);
                        OficioCaracteristicas jobStats = player.getMetierByID(job);

                        if (jobStats == null) {
                            player.send("Im149" + job);
                            return;
                        }

                        player.unlearnJob(jobStats.getId());
                        GestorSalida.GAME_SEND_STATS_PACKET(player);
                        Database.dinamicos.getPlayerData().update(player);
                        player.send("JR" + job);
                        break;

                    case 10://EPO.
                        if (player0.getPelea() != null) return;
                        obj = Mundo.getGameObject(objet);
                        if (obj == null)
                            return;
                        ObjetoJuego pets = player.getObjetByPos(Constantes.ITEM_POS_FAMILIER);
                        if (pets == null)
                            return;
                        MascotaEntrada MyPets = Mundo.mundo.getPetsEntry(pets.getId());
                        if (MyPets == null)
                            return;
                        if (obj.getModelo().getConditions().contains(pets.getModelo().getId() + ""))
                            MyPets.giveEpo(player);
                        break;

                    case 11://Chang� de Sexe.
                        if (player0.getPelea() != null) return;
                        if (player.getSexe() == 0)
                            player.setSexe(1);
                        else
                            player.setSexe(0);

                        GestorSalida.GAME_SEND_ALTER_GM_PACKET(player.getCurMap(), player);
                        Database.dinamicos.getPlayerData().updateInfos(player);
                        break;

                    case 12://Chang� de nom.
                        if (player0.getPelea() != null) return;
                        player.setChangeName(true);
                        isOk = false;
                        send = false;
                        break;

                    case 13://Apprendre une �mote.
                        if (player0.getPelea() != null) return;
                        int emote = Integer.parseInt(arg);

                        if (player.getEmotes().contains(emote)) {
                            GestorSalida.GAME_SEND_MESSAGE(player, "Tu connais déjà cet aptitude !");
                            return;
                        }

                        player.addStaticEmote(emote);
                        break;

                    case 14://Apprendre un m�tier.
                        if (player0.getPelea() != null) return;
                        job = Integer.parseInt(arg);
                        if (Mundo.mundo.getMetier(job) == null)
                            return;
                        if (player.getMetierByID(job) != null)//M�tier d�j� appris
                        {
                            GestorSalida.GAME_SEND_Im_PACKET(player, "111");
                            return;
                        }
                        if (player.getMetierByID(2) != null
                                && player.getMetierByID(2).get_lvl() < 30
                                || player.getMetierByID(11) != null
                                && player.getMetierByID(11).get_lvl() < 30
                                || player.getMetierByID(13) != null
                                && player.getMetierByID(13).get_lvl() < 30
                                || player.getMetierByID(14) != null
                                && player.getMetierByID(14).get_lvl() < 30
                                || player.getMetierByID(15) != null
                                && player.getMetierByID(15).get_lvl() < 30
                                || player.getMetierByID(16) != null
                                && player.getMetierByID(16).get_lvl() < 30
                                || player.getMetierByID(17) != null
                                && player.getMetierByID(17).get_lvl() < 30
                                || player.getMetierByID(18) != null
                                && player.getMetierByID(18).get_lvl() < 30
                                || player.getMetierByID(19) != null
                                && player.getMetierByID(19).get_lvl() < 30
                                || player.getMetierByID(20) != null
                                && player.getMetierByID(20).get_lvl() < 30
                                || player.getMetierByID(24) != null
                                && player.getMetierByID(24).get_lvl() < 30
                                || player.getMetierByID(25) != null
                                && player.getMetierByID(25).get_lvl() < 30
                                || player.getMetierByID(26) != null
                                && player.getMetierByID(26).get_lvl() < 30
                                || player.getMetierByID(27) != null
                                && player.getMetierByID(27).get_lvl() < 30
                                || player.getMetierByID(28) != null
                                && player.getMetierByID(28).get_lvl() < 30
                                || player.getMetierByID(31) != null
                                && player.getMetierByID(31).get_lvl() < 30
                                || player.getMetierByID(36) != null
                                && player.getMetierByID(36).get_lvl() < 30
                                || player.getMetierByID(41) != null
                                && player.getMetierByID(41).get_lvl() < 30
                                || player.getMetierByID(56) != null
                                && player.getMetierByID(56).get_lvl() < 30
                                || player.getMetierByID(58) != null
                                && player.getMetierByID(58).get_lvl() < 30
                                || player.getMetierByID(60) != null
                                && player.getMetierByID(60).get_lvl() < 30
                                || player.getMetierByID(65) != null
                                && player.getMetierByID(65).get_lvl() < 30) {
                            GestorSalida.GAME_SEND_Im_PACKET(player, "18;30");
                            return;
                        }
                        if (player.totalJobBasic() > 2) {
                            GestorSalida.GAME_SEND_Im_PACKET(player, "19");
                            return;
                        } else {
                            if (job == 27) {
                                if (!player.hasItemTemplate(966, 1))
                                    return;
                                GestorSalida.GAME_SEND_Im_PACKET(player, "022;"
                                        + 966 + "~" + 1);
                                player.learnJob(Mundo.mundo.getMetier(job));
                            } else {
                                player.learnJob(Mundo.mundo.getMetier(job));
                            }
                        }
                        break;

                    case 15://TP au foyer.
                        if (player0.getPelea() != null) return;
                        boolean tp = false;
                        for (Casas i : Mundo.mundo.getHouses().values()) {
                            if (i.getOwnerId() == player.getAccount().getId()) {
                                player.teleport((short) i.getHouseMapId(), i.getHouseCellId());
                                tp = true;
                                break;
                            }
                        }
                        if(!tp) {
                            player.send("Im161");
                            return;
                        }
                        break;

                    case 16://Pnj Follower.
                        if (player0.getPelea() != null) return;
                        // Petite larve dor�e = 7425
                        player.setMascotte(Integer.parseInt(this.args));
                        break;

                    case 17://B�n�diction.
                        if (player0.getPelea() != null) return;
                        player.setBenediction(Mundo.getGameObject(objet).getModelo().getId());
                        break;

                    case 18://Mal�diction.
                        if (player0.getPelea() != null) return;
                        player.setMalediction(Mundo.getGameObject(objet).getModelo().getId());
                        break;

                    case 19://RolePlay Buff.
                        if (player0.getPelea() != null) return;
                        player.setRoleplayBuff(Mundo.getGameObject(objet).getModelo().getId());
                        break;

                    case 20://Bonbon.
                        if (player0.getPelea() != null) return;
                        player.setCandy(Mundo.getGameObject(objet).getModelo().getId());
                        break;

                    case 21://Poser un objet d'�levage.
                        if (player0.getPelea() != null) return;
                        Mapa map0 = player.getCurMap();
                        ObjetoJuego object = Mundo.getGameObject(objet);
                        id0 = object.getModelo().getId();

                        int resist = object.getResistance(object.parseStatsString());
                        int resistMax = object.getResistanceMax(object.getModelo().getStrTemplate());
                        if (map0.getMountPark() == null)
                            return;
                        Cercados MP = map0.getMountPark();
                        if (player.getGuild() == null) {
                            GestorSalida.GAME_SEND_BN(player);
                            return;
                        }
                        if (!player.getGuildMember().canDo(Constantes.G_AMENCLOS)) {
                            GestorSalida.GAME_SEND_Im_PACKET(player, "193");
                            return;
                        }
                        if (MP.getCellOfObject().size() == 0 || !MP.getCellOfObject().contains(cellid) || MP.getCellAndObject().containsKey(cellid)) {
                            GestorSalida.GAME_SEND_BN(player);
                            return;
                        }
                        if (MP.getObject().size() < MP.getMaxObject()) {
                            MP.addObject(cellid, id0, player.getId(), resistMax, resist);
                            GestorSalida.SEND_GDO_PUT_OBJECT_MOUNT(map0, cellid + ";" + id0 + ";1;" + resist + ";" + resistMax);
                        } else {
                            GestorSalida.GAME_SEND_Im_PACKET(player, "1107");
                            return;
                        }
                        break;

                    case 22://Poser un prisme.
                        if (player0.getPelea() != null) return;
                        map0 = player.getCurMap();
                        int cellId1 = player.getCurCell().getId();
                        SubArea subArea = map0.getSubArea();
                        Area area = subArea.area;
                        int alignement = player.get_align();
                        if (cellId1 <= 0)
                            return;
                        if (alignement == 0 || alignement == 3) {
                            GestorSalida.GAME_SEND_MESSAGE(player, "No posees la alineación necesaria para poner un prisma.");
                            return;
                        }
                        if (!player.is_showWings()) {
                            GestorSalida.GAME_SEND_Im_PACKET(player, "1148");
                            return;
                        }
                        if (map0.noPrism || (subArea != null && (subArea.getId() == 9 || subArea.getId() == 95)) || map0.haveMobFix() || map0.getMobGroups().isEmpty()) {
                            GestorSalida.GAME_SEND_Im_PACKET(player, "1146");
                            return;
                        }
                        if (subArea.getAlignement() != 0 || !subArea.getConquistable()) {
                            GestorSalida.GAME_SEND_Im_PACKET(player, "1147");
                            return;
                        }
                        Prisma Prisme = new Prisma(Mundo.mundo.getNextIDPrisme(), alignement, 1, map0.getId(), cellId1, player.get_honor(), -1);
                        subArea.setAlignement(alignement);
                        subArea.prismId = Prisme.getId();
                        for (Jugador z : Mundo.mundo.getOnlinePlayers()) {
                            if (z == null)
                                continue;
                            if (z.get_align() == 0) {
                                GestorSalida.GAME_SEND_am_ALIGN_PACKET_TO_SUBAREA(z, subArea.getId() + "|" + alignement + "|1");
                                if (area.getAlignement() == 0)
                                    GestorSalida.GAME_SEND_aM_ALIGN_PACKET_TO_AREA(z, area.getId() + "|" + alignement);
                                continue;
                            }
                            GestorSalida.GAME_SEND_am_ALIGN_PACKET_TO_SUBAREA(z, subArea.getId()
                                    + "|" + alignement + "|0");
                            if (area.getAlignement() == 0)
                                GestorSalida.GAME_SEND_aM_ALIGN_PACKET_TO_AREA(z, area.getId()
                                        + "|" + alignement);
                        }
                        if (area.getAlignement() == 0) {
                            area.prismId = Prisme.getId();
                            area.setAlignement(alignement);
                            Prisme.setConquestArea(area.getId());
                        }
                        Mundo.mundo.addPrisme(Prisme);
                        Database.estaticos.getPrismData().add(Prisme);
                        player.getCurMap().getSubArea().setAlignement(player.get_align());
                        Database.estaticos.getSubAreaData().update(player.getCurMap().getSubArea());
                        GestorSalida.GAME_SEND_PRISME_TO_MAP(map0, Prisme);
                        break;

                    case 23://Rappel Prismatique.
                        if (player0.getPelea() != null) return;
                        int dist = 99999, alea;
                        mapId = 0;
                        cellId = 0;
                        for (Prisma i : Mundo.mundo.AllPrisme()) {
                            if (i.getAlignement() != player.get_align())
                                continue;
                            alea = (Mundo.mundo.getMap(i.getMap()).getX() - player.getCurMap().getX())
                                    * (Mundo.mundo.getMap(i.getMap()).getX() - player.getCurMap().getX())
                                    + (Mundo.mundo.getMap(i.getMap()).getY() - player.getCurMap().getY())
                                    * (Mundo.mundo.getMap(i.getMap()).getY() - player.getCurMap().getY());
                            if (alea < dist) {
                                dist = alea;
                                mapId = i.getMap();
                                cellId = i.getCell();
                            }
                        }
                        if (mapId != 0)
                            player.teleport(mapId, cellId);
                        break;

                    case 24://TP Village align�.
                        if (player0.getPelea() != null) return;
                        mapId = (short) Integer.parseInt(arg.split(",")[0]);
                        cellId = Integer.parseInt(arg.split(",")[1]);
                        if (Mundo.mundo.getMap(mapId).getSubArea().getAlignement() == player.get_align())
                            player.teleport(mapId, cellId);
                        break;

                    case 25://Spawn groupe.
                        if (player0.getPelea() != null || player0.getCurMap().haveMobFix()) return;
                        boolean inArena = arg.split(";")[0].equals("true");
                        String groupData = "";
                        if (inArena && !PiedraAlma.isInArenaMap(player.getCurMap().getId()))
                            return;
                        if (arg.split(";")[1].equals("1")) {
                            groupData = arg.split(";")[2];
                        } else {
                            PiedraAlma soulStone = (PiedraAlma) Mundo.getGameObject(objet);
                            groupData = soulStone.parseGroupData();
                        }
                        String condition = "MiS = " + player.getId();
                        player.getCurMap().spawnNewGroup(true, player.getCurCell().getId(), groupData, condition);
                        break;

                    case 26://Ajout d'objet.
                        if (player0.getPelea() != null) return;
                        for (String i : arg.split(";")) {
                            obj = Mundo.mundo.getObjetoModelo(Integer.parseInt(i.split(",")[0])).createNewItem(Integer.parseInt(i.split(",")[1]), false);
                            if (player.addObjet(obj, true))
                                Mundo.addGameObject(obj, true);
                        }
                        GestorSalida.GAME_SEND_Ow_PACKET(player);
                        break;

                    case 27://Ajout de titre.
                        if (player0.getPelea() != null) return;
                        player.setAllTitle(arg);
                        break;

                    case 28://Ajout de zaap.
                        if (player0.getPelea() != null) return;
                        player.verifAndAddZaap((short) Integer.parseInt(arg));
                        break;

                    case 29://Panel d'oubli de sort.
                        if (player0.getPelea() != null) return;
                        player.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.FORGETTING_SPELL, 0));
                        GestorSalida.GAME_SEND_FORGETSPELL_INTERFACE('+', player);
                        break;

                    case 31://Cadeau bworker.
                        if (player0.getPelea() != null) return;
                        new Accion(511, "", "", null).apply(player, null, objet, -1);
                        break;

                    case 32://G�oposition traque.
                        if (player0.getPelea() != null) return;
                        String traque = Mundo.getGameObject(objet).getTraquedName();

                        if (traque == null)
                            break;

                        Jugador cible = Mundo.mundo.getPlayerByName(traque);

                        if (cible == null)
                            break;

                        if (!cible.isOnline()) {
                            GestorSalida.GAME_SEND_Im_PACKET(player, "1198");
                            break;
                        }

                        GestorSalida.GAME_SEND_FLAG_PACKET(player, cible);
                        break;

                    case 33://Ajout de points boutique.
                        if (player0.getPelea() != null) return;
                        player.getAccount().setPoints(player.getAccount().getPoints() + Integer.parseInt(arg));
                        break;

                    case 34://Fm cac
                        ObjetoJuego gameObject = player.getObjetByPos(Constantes.ITEM_POS_ARME);

                        if(gameObject == null) {
                            player.sendMessage("Vous ne portez pas de corps-à-corps.");
                            isOk = false;
                            send = false;
                            return;
                        }

                        boolean containNeutre = false;

                        for(EfectoHechizo effect : gameObject.getEffects())
                            if (effect.getEffectID() == 100 || effect.getEffectID() == 95) {
                                containNeutre = true;
                                break;
                            }

                        if(containNeutre) {
                            for(int i = 0; i < gameObject.getEffects().size(); i++) {
                                if(gameObject.getEffects().get(i).getEffectID() == 100) {
                                    switch (this.args.toUpperCase()) {
                                        case "EAU" -> gameObject.getEffects().get(i).setEffectID(96);
                                        case "TERRE" -> gameObject.getEffects().get(i).setEffectID(97);
                                        case "AIR" -> gameObject.getEffects().get(i).setEffectID(98);
                                        case "FEU" -> gameObject.getEffects().get(i).setEffectID(99);
                                    }
                                }
                                if(gameObject.getEffects().get(i).getEffectID() == 95) {
                                    switch (this.args.toUpperCase()) {
                                        case "EAU" -> gameObject.getEffects().get(i).setEffectID(91);
                                        case "TERRE" -> gameObject.getEffects().get(i).setEffectID(92);
                                        case "AIR" -> gameObject.getEffects().get(i).setEffectID(93);
                                        case "FEU" -> gameObject.getEffects().get(i).setEffectID(94);
                                    }
                                }
                            }

                            GestorSalida.GAME_SEND_STATS_PACKET(player);
                            GestorSalida.GAME_SEND_UPDATE_ITEM(player, gameObject);
                            player.sendMessage("Votre corps-corps a été modifier avec succès.");
                        } else {
                            player.sendMessage("Votre corps-à-corps ne contient aucun dégât de type neutre.");
                            isOk = false;
                            send = false;
                        }
                        break;

                    case 35: // Mount cameleon
                        if(player.getMount() != null) {
                            player.getMount().getCapacitys().add(9);
                            if(player.isOnMount()) {
                                GestorSalida.GAME_SEND_ALTER_GM_PACKET(player.getCurMap(), player);
                            }
                            GestorSalida.GAME_SEND_MOUNT_DESCRIPTION_PACKET(player, player.getMount());
                            player.sendMessage("Votre monture est désormais caméleone.");
                            sureIsOk = true;
                            send = true;
                        } else {
                            player.sendMessage("Vous n'avez pas de monture.");
                            return;
                        }
                        break;
                }
                turn++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean effect = this.haveEffect(Mundo.getGameObject(objet).getModelo().getId(), Mundo.getGameObject(objet), player);
        if (effect)
            isOk = true;
        if (isOk)
            effect = true;
        if (this.type.split(";").length > 1)
            isOk = true;
        if (objet != -1) {
            if (send)
                GestorSalida.GAME_SEND_Im_PACKET(player, "022;" + 1 + "~" + Mundo.getGameObject(objet).getModelo().getId());
            if (sureIsOk || (isOk && effect && Mundo.getGameObject(objet).getModelo().getId() != 7799)) {
                if (Mundo.getGameObject(objet) != null) {
                    player0.removeItem(objet, 1, true, true);
                }
            }
        }
    }

    private boolean haveEffect(int id, ObjetoJuego gameObject, Jugador player) {
        if (player.getPelea() != null) return true;
        switch (id) {
            case 8378://Fragment magique.
                for (Doble<Integer, Integer> couple : ((FragmentosMagicos) gameObject).getRunes()) {
                    ObjetoModelo objectTemplate = Mundo.mundo.getObjetoModelo(couple.getPrimero());

                    if (objectTemplate == null)
                        continue;

                    ObjetoJuego newGameObject = objectTemplate.createNewItem(couple.getSegundo(), true);

                    if (newGameObject == null)
                        continue;

                    if (!player.addObjetSimiler(newGameObject, true, -1)) {
                        Mundo.addGameObject(newGameObject, true);
                        player.addObjet(newGameObject);
                    }
                }
                send = true;
                return true;
            case 7799://Le Saut Sifflard
                player.toogleOnMount();
                send = false;
                return false;

            case 10832://Craqueloroche
                if (player.getPelea() != null || player.getCurMap().haveMobFix()) return false;
                player.getCurMap().spawnNewGroup(true, player.getCurCell().getId(), "483,1,1000", "MiS="
                        + player.getId());
                return true;

            case 10664://Abragland
                if (player.getPelea() != null || player.getCurMap().haveMobFix()) return false;
                player.getCurMap().spawnNewGroup(true, player.getCurCell().getId(), "47,1,1000", "MiS="
                        + player.getId());
                return true;

            case 10665://Coffre de Jorbak
                player.setCandy(10688);
                return true;

            case 10670://Parchemin de persimol
                player.setBenediction(10682);
                return true;

            case 8435://Ballon Rouge Magique
                GestorSalida.sendPacketToMap(player.getCurMap(), "GA;208;"
                        + player.getId() + ";" + player.getCurCell().getId()
                        + ",2906,11,8,1");
                return true;

            case 8624://Ballon Bleu Magique
                GestorSalida.sendPacketToMap(player.getCurMap(), "GA;208;"
                        + player.getId() + ";" + player.getCurCell().getId()
                        + ",2907,11,8,1");
                return true;

            case 8625://Ballon Vert Magique
                GestorSalida.sendPacketToMap(player.getCurMap(), "GA;208;"
                        + player.getId() + ";" + player.getCurCell().getId()
                        + ",2908,11,8,1");
                return true;

            case 8430://Ballon Jaune Magique
                GestorSalida.sendPacketToMap(player.getCurMap(), "GA;208;"
                        + player.getId() + ";" + player.getCurCell().getId()
                        + ",2909,11,8,1");
                return true;

            case 8621://Cawotte Maudite
                player.setGfxId(1109);
                GestorSalida.GAME_SEND_ALTER_GM_PACKET(player.getCurMap(), player);
                return true;

            case 8626://Nisitik Miditik
                player.setGfxId(1046);
                GestorSalida.GAME_SEND_ALTER_GM_PACKET(player.getCurMap(), player);
                return true;

            case 10833://Chapain
                player.setGfxId(9001);
                GestorSalida.GAME_SEND_ALTER_GM_PACKET(player.getCurMap(), player);
                return true;

            case 10839://Monstre Pain
                if (player.getPelea() != null || player.getCurMap().haveMobFix()) return false;
                player.getCurMap().spawnNewGroup(true, player.getCurCell().getId(), "2787,1,1000", "MiS="
                        + player.getId());
                return true;

            case 8335://Cadeau 1
                Nawidad.getRandomObjectOne(player);
                return true;
            case 8336://Cadeau 2
                Nawidad.getRandomObjectTwo(player);
                return true;
            case 8337://Cadeau 3
                Nawidad.getRandomObjectTree(player);
                return true;
            case 8339://Cadeau 4
                Nawidad.getRandomObjectFour(player);
                return true;
            case 8340://Cadeau 5
                Nawidad.getRandomObjectFive(player);
                return true;
            case 10912://Cadeau nowel 1
                return false;
            case 10913://Cadeau nowel 2
                return false;
            case 10914://Cadeau nowel 3
                return false;

        }
        return false;
    }
}
