package org.alexandria.comunes;

import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.Mercadillo.*;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;
import org.alexandria.estaticos.Cercados;
import org.alexandria.estaticos.Cofres;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.estaticos.cliente.Jugador.Grupo;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.configuracion.Constantes;
import org.alexandria.estaticos.Prisma;
import org.alexandria.estaticos.Recaudador;
import org.alexandria.estaticos.Monstruos.MobGroup;
import org.alexandria.estaticos.Montura;
import org.alexandria.estaticos.Npc;
import org.alexandria.estaticos.Gremio;
import org.alexandria.estaticos.Gremio.GremioMiembros;
import org.alexandria.estaticos.juego.JuegoCliente;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.Mercadillo;
import org.alexandria.estaticos.Mision;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.objeto.ObjetoModelo;
import org.alexandria.estaticos.objeto.ObjetoSet;
import org.alexandria.estaticos.oficio.OficioCaracteristicas;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Objects;

public class GestorSalida {

    public static void send(Jugador player, String packet) {
        if (player != null && player.getAccount() != null && player.getGameClient()!=null)
            GestorSalida.send(player.getGameClient(), packet);
    }

    public static void send(JuegoCliente client, String packet) {
        if (client != null && client.getSession() != null && !client.getSession().isClosing() && client.getSession().isConnected())
            client.send(packet);
    }

    public static void GAME_SEND_UPDATE_ITEM(Jugador P, ObjetoJuego obj) {
        send(P, "OC" + "|" + obj.parseItem());
    }

    public static void MULTI_SEND_Af_PACKET(JuegoCliente out, int position, int totalAbo, int totalNonAbo, int button) {
        send(out, "Af" + position + "|" + totalAbo + "|" + totalNonAbo + "|" + button + "|" + Configuracion.INSTANCE.getSERVER_ID());
    }

    public static void GAME_SEND_ATTRIBUTE_FAILED(JuegoCliente out) {
        send(out, "ATE");
    }

    public static void GAME_SEND_AV0(JuegoCliente out) {
        send(out, "AV0");
    }

    //TODO: Abonement
    public static void GAME_SEND_PERSO_LIST(JuegoCliente out, java.util.Map<Integer, Jugador> persos, long subscriber) {
        StringBuilder packet = new StringBuilder();

        packet.append("ALK");
        if (Configuracion.INSTANCE.getSubscription())
            packet.append(subscriber);
        else
            packet.append("86400000");
        packet.append("|").append(persos.size());
        for (Entry<Integer, Jugador> entry : persos.entrySet())
            packet.append(entry.getValue().parseALK());
        send(out, packet.toString());
    }

    public static void GAME_SEND_NAME_ALREADY_EXIST(JuegoCliente out) {
        String packet = "AAEa";
        send(out, packet);
    }

    public static void GAME_SEND_CREATE_PERSO_FULL(JuegoCliente out) {
        String packet = "AAEf";
        send(out, packet);
    }

    public static void GAME_SEND_CREATE_OK(JuegoCliente out) {
        String packet = "AAK";
        send(out, packet);
    }

    public static void GAME_SEND_DELETE_PERSO_FAILED(JuegoCliente out) {
        String packet = "ADE";
        send(out, packet);
    }

    public static void GAME_SEND_CREATE_FAILED(JuegoCliente out) {
        String packet = "AAEF";
        send(out, packet);

    }

    public static void GAME_SEND_PERSO_SELECTION_FAILED(JuegoCliente out) {
        String packet = "ASE";
        send(out, packet);

    }

    public static void GAME_SEND_STATS_PACKET(Jugador perso) {
        GestorSalida.GAME_SEND_Ow_PACKET(perso);
        send(perso, perso.getAsPacket());

    }

    public static void GAME_SEND_Rx_PACKET(Jugador out) {
        send(out, "Rx" + out.getMountXpGive());

    }

    public static void GAME_SEND_Rn_PACKET(Jugador out, String name) {
        send(out, "Rn" + name);

    }

    public static void GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(Jugador perso, ObjetoJuego item) {
        send(perso, "OCO" + item.parseItem());

    }

    public static void GAME_SEND_Re_PACKET(Jugador out, String sign, Montura DD) {
        StringBuilder packet = new StringBuilder();
        packet.append("Re").append(sign);
        if (sign.equals("+"))
            packet.append(DD.parse());
        send(out, packet.toString());

    }

    public static void GAME_SEND_ASK(JuegoCliente out, Jugador perso) {
        try {
            StringBuilder packet = new StringBuilder();
            int color1 = perso.getColor1(), color2 = perso.getColor2(), color3 = perso.getColor3();
            if (perso.getObjetByPos(Constantes.ITEM_POS_MALEDICTION) != null) {
                if (perso.getObjetByPos(Constantes.ITEM_POS_MALEDICTION).getModelo().getId() == 10838) {
                    color1 = 16342021;
                    color2 = 16342021;
                    color3 = 16342021;
                }
            }
            packet.append("ASK|").append(perso.getId()).append("|").append(perso.getName()).append("|");
            packet.append(perso.getLevel()).append("|").append(perso.getMorphMode() ? -1 : perso.getClasse()).append("|").append(perso.getSexe());
            packet.append("|").append(perso.getGfxId()).append("|").append((color1 == -1 ? "-1" : Integer.toHexString(color1)));
            packet.append("|").append((color2 == -1 ? "-1" : Integer.toHexString(color2))).append("|");
            packet.append((color3 == -1 ? "-1" : Integer.toHexString(color3))).append("|");
            packet.append(perso.parseItemToASK());
            send(out, packet.toString());
        } catch(Exception e) { e.printStackTrace(); System.out.println("Error occured : " + e.getMessage());}
    }

    public static void GAME_SEND_ALIGNEMENT(JuegoCliente out, int alliID) {
        String packet = "ZS" + alliID;
        send(out, packet);

    }

    public static void GAME_SEND_ADD_CANAL(JuegoCliente out, String chans) {
        String packet = "cC+" + chans;
        send(out, packet);

    }

    public static void GAME_SEND_ZONE_ALLIGN_STATUT(JuegoCliente out) {
        String packet = "al|" + Mundo.mundo.getSousZoneStateString();
        send(out, packet);

    }

    public static void GAME_SEND_RESTRICTIONS(JuegoCliente out) {
        String packet = "AR6bk";
        send(out, packet);

    }

    public static void GAME_SEND_Ow_PACKET(Jugador perso) {
        send(perso, "Ow" + perso.getPodUsed() + "|" + perso.getMaximosPods());

    }

    public static void GAME_SEND_OT_PACKET(JuegoCliente out, int id) {
        StringBuilder packet = new StringBuilder();
        packet.append("OT");
        if (id > 0)
            packet.append(id);
        send(out, packet.toString());

    }

    public static void GAME_SEND_SEE_FRIEND_CONNEXION(JuegoCliente out, boolean see) {
        String packet = "FO" + (see ? "+" : "-");
        send(out, packet);

    }

    public static void GAME_SEND_GAME_CREATE(JuegoCliente out, String _name) {
        send(out, "GCK" + "|" + "1" + "|" + _name);

    }

    public static void GAME_SEND_MAPDATA(JuegoCliente out, int id, String date, String key) {
        send(out, "GDM" + "|" + id + "|" + date + "|" + key);

    }

    public static void GAME_SEND_GDK_PACKET(JuegoCliente out) {
        send(out, "GDK");
    }

    public static void GAME_SEND_MAP_MOBS_GMS_PACKETS(JuegoCliente out, Mapa Map) {
        StringBuilder packet = new StringBuilder();
        packet.append(Map.getMobGroupGMsPackets());
        if (Objects.equals(packet, "")) return;
        send(out, packet.toString());

    }

    public static void GAME_SEND_MAP_OBJECTS_GDS_PACKETS(JuegoCliente out, Mapa Map) {
        StringBuilder packet = new StringBuilder();
        packet.append(Map.getObjectsGDsPackets());
        if (Objects.equals(packet, "")) return;
        send(out, packet.toString());
    }

    public static void GAME_SEND_MAP_NPCS_GMS_PACKETS(JuegoCliente out, Mapa Map) {
        StringBuilder packet = new StringBuilder();
        packet.append(Map.getNpcsGMsPackets(out.getPlayer()));
        if (Objects.equals(packet, "") && packet.length() < 4) return;
        send(out, packet.toString());
    }

    public static void GAME_SEND_MAP_PERCO_GMS_PACKETS(JuegoCliente out, Mapa Map) {
        StringBuilder packet = new StringBuilder();
        packet.append(Recaudador.parseGM(Map));
        if (packet.length() < 5) return;
        send(out, packet.toString());
    }

    //z == null soluciona "Mapa Carcel" - NullPoint
    public static void GAME_SEND_ERASE_ON_MAP_TO_MAP(Mapa map, int guid) {
        if (map == null) return;
        StringBuilder packet = new StringBuilder();
        packet.append("GM").append("|").append("-").append(guid);
        for (Jugador z : map.getPlayers()) {
            if (z == null) continue;
            if (z.getGameClient() == null) continue;
            send(z.getGameClient(), packet.toString());
        }
    }

    public static void GAME_SEND_ON_FIGHTER_KICK(Pelea f, int guid, int team) {
        StringBuilder packet = new StringBuilder();
        packet.append("GM").append("|").append("-").append(guid);
        for (Peleador F : f.getFighters(team)) {
            if (F.getPlayer() == null || F.getPlayer().getGameClient() == null || F.getPlayer().getId() == guid)
                continue;
            send(F.getPlayer().getGameClient(), packet.toString());
        }
    }

    public static void GAME_SEND_ALTER_FIGHTER_MOUNT(Pelea fight, Peleador fighter, int guid, int team, int otherteam) {
        StringBuilder packet = new StringBuilder();
        packet.append("GM").append("|").append("-").append(guid).append((char) 0x00).append(fighter.getGmPacket('+', true));
        for (Peleador F : fight.getFighters(team)) {
            if (F.getPlayer() == null
                    || F.getPlayer().getGameClient() == null
                    || !F.getPlayer().isOnline())
                continue;
            send(F.getPlayer().getGameClient(), packet.toString());
        }
        if (otherteam > -1) {
            for (Peleador F : fight.getFighters(otherteam)) {
                if (F.getPlayer() == null
                        || F.getPlayer().getGameClient() == null
                        || !F.getPlayer().isOnline())
                    continue;
                send(F.getPlayer().getGameClient(), packet.toString());
            }
        }
    }

    public static void GAME_SEND_ADD_PLAYER_TO_MAP(Mapa map, Jugador perso) {
        StringBuilder packet = new StringBuilder();
        packet.append("GM").append("|").append("+").append(perso.parseToGM());
        for (Jugador z : map.getPlayers()) {
            if (perso.get_size() > 0)
                send(z, packet.toString());
            else if (z.getGroupe() != null)
                send(z, packet.toString());
        }
    }

    public static void GAME_SEND_DUEL_Y_AWAY(JuegoCliente out, int guid) {
        String packet = "GA;903;" + guid + ";o";
        send(out, packet);

    }

    public static void GAME_SEND_DUEL_E_AWAY(JuegoCliente out, int guid) {
        String packet = "GA;903;" + guid + ";z";
        send(out, packet);

    }

    public static void GAME_SEND_MAP_NEW_DUEL_TO_MAP(Mapa map, int guid, int guid2) {
        StringBuilder packet = new StringBuilder();
        packet.append("GA").append(";").append("900").append(";").append(guid).append(";").append(guid2);
        for (Jugador z : map.getPlayers())
            send(z, packet.toString());
    }

    public static void GAME_SEND_CANCEL_DUEL_TO_MAP(Mapa map, int guid, int guid2) {
        String packet = "GA;902;" + guid + ";" + guid2;
        for (Jugador z : map.getPlayers())
            send(z, packet);

    }

    public static void GAME_SEND_MAP_START_DUEL_TO_MAP(Mapa map, int guid,
                                                       int guid2) {
        String packet = "GA;901;" + guid + ";" + guid2;
        for (Jugador z : map.getPlayers())
            send(z, packet);

    }

    public static void GAME_SEND_MAP_FIGHT_COUNT(JuegoCliente out, Mapa map) {
        send(out, "fC" + map.getNbrFight());
    }

    public static void GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(Pelea fight, int teams, int state, int cancelBtn, int duel, int spec, int time, int type) {
        StringBuilder packet = new StringBuilder();
        packet.append("GJK").append(state).append("|").append(cancelBtn).append("|").append(duel).append("|").append(spec).append("|").append(time).append("|").append(type);
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft())
                continue;
            send(f.getPlayer(), packet.toString());
        }

    }

    public static void GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(Pelea fight, int teams, String places, int team) {
        StringBuilder packet = new StringBuilder();
        packet.append("GP").append(places).append("|").append(team);
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft())
                continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            send(f.getPlayer(), packet.toString());
        }

    }

    public static void GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(Mapa map) {
        String packet = "fC" + map.getNbrFight();
        for (Jugador z : map.getPlayers())
            send(z, packet);

    }

    public static void GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(Mapa map, int arg1, int guid1, int guid2, int cell1, String str1, int cell2, String str2) {
        StringBuilder packet = new StringBuilder();
        packet.append("Gc").append("+").append(guid1).append(";").append(arg1).append("|").append(guid1).append(";").append(cell1).append(";").append(str1).append("|").append(guid2).append(";").append(cell2).append(";").append(str2);
        for (Jugador z : map.getPlayers())
            send(z, packet.toString());

    }

    public static void GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(Jugador p, Mapa map, int arg1, int guid1, int guid2, int cell1, String str1, int cell2, String str2) {
        send(p, "Gc" + "+" + guid1 + ";" + arg1 + "|" + guid1 + ";" + cell1 + ";" + str1 + "|" + guid2 + ";" + cell2 + ";" + str2);

    }

    public static void GAME_SEND_GAME_REMFLAG_PACKET_TO_MAP(Mapa map, int guid) {
        String packet = "Gc-" + guid;
        for (Jugador z : map.getPlayers())
            send(z, packet);

    }

    public static void GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(Mapa map, int teamID, Peleador perso) {
        StringBuilder packet = new StringBuilder();
        packet.append("Gt").append(teamID).append("|").append("+").append(perso.getId()).append(";").append(perso.getPacketsName()).append(";").append(perso.getLvl());
        for (Jugador z : map.getPlayers()) {
            send(z, packet.toString());
        }

    }

    public static void GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(Jugador p, Mapa map, int teamID, Peleador perso) {
        send(p, "Gt" + teamID + "|" + "+" + perso.getId() + ";" + perso.getPacketsName() + ";" + perso.getLvl());

    }

    public static void GAME_SEND_REMOVE_IN_TEAM_PACKET_TO_MAP(Mapa map, int teamID, Peleador perso) {
        StringBuilder packet = new StringBuilder();
        packet.append("Gt").append(teamID).append("|-").append(perso.getId()).append(";").append(perso.getPacketsName()).append(";").append(perso.getLvl());
        for (Jugador z : map.getPlayers())
            send(z, packet.toString());

    }

    public static void GAME_SEND_MAP_MOBS_GMS_PACKETS_TO_MAP(Mapa map) {
        StringBuilder packet = new StringBuilder();
        packet.append(map.getMobGroupGMsPackets()); // Un par un comme sa lors du respawn :)
        for (Jugador z : map.getPlayers())
            send(z, packet.toString());

    }

    public static void GAME_SEND_MAP_MOBS_GMS_PACKETS_TO_MAP(Mapa map, Jugador perso) {
        String packet = map.getMobGroupGMsPackets(); // Un par un comme sa lors du respawn :)
        send(perso, packet);

    }

    public static void GAME_SEND_MAP_MOBS_GM_PACKET(Mapa map, MobGroup current_Mobs) {
        StringBuilder packet = new StringBuilder();
        packet.append("GM").append("|");
        packet.append(current_Mobs.parseGM()); // Un par un comme sa lors du respawn :)
        for (Jugador z : map.getPlayers())
            send(z, packet.toString());

    }

    public static void GAME_SEND_MAP_GMS_PACKETS(Mapa map, Jugador _perso) {
        String packet = (_perso
                .getPelea() != null ?
                map
                        .getFightersGMsPackets(_perso.getPelea()) :
                map
                        .getGMsPackets());
        send(_perso, packet);

    }

    public static void GAME_SEND_ON_EQUIP_ITEM(Mapa map, Jugador _perso) {
        String packet = _perso.parseToOa();
        for (Jugador z : map.getPlayers())
            send(z, packet);

    }

    public static void GAME_SEND_ON_EQUIP_ITEM_FIGHT(Jugador _perso, Peleador f, Pelea F) {
        String packet = _perso.parseToOa();
        for (Peleador z : F.getFighters(f.getTeam2())) {
            if (z.getPlayer() == null)
                continue;
            send(z.getPlayer(), packet);
        }
        for (Peleador z : F.getFighters(f.getOtherTeam())) {
            if (z.getPlayer() == null)
                continue;
            send(z.getPlayer(), packet);
        }

    }

    public static void GAME_SEND_FIGHT_CHANGE_PLACE_PACKET_TO_FIGHT(Pelea fight, int teams, Mapa map, int guid, int cell) {
        StringBuilder packet = new StringBuilder();
        packet.append("GIC").append("|").append(guid).append(";").append(cell).append(";").append("1");
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft()) continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline()) continue;
            send(f.getPlayer(), packet.toString());
        }

    }

    public static void GAME_SEND_Ew_PACKET(Jugador perso, int pods, int podsMax) { //Pods de la dinde
        String packet = "Ew" + pods + ";" + podsMax + "";
        send(perso, packet);

    }

    public static void GAME_SEND_EL_MOUNT_PACKET(Jugador out, Montura drago) { // Inventaire dinde : Liste des objets
        send(out, "EL" + drago.parseToMountObjects());
    }

    public static void GAME_SEND_GM_MOUNT_TO_MAP(Mapa map, Montura dd) {
        String packet = dd.parseToGM();
        for (Jugador z : map.getPlayers())
            send(z, packet);

    }

    public static void GAME_SEND_GDO_OBJECT_TO_MAP(JuegoCliente out, Mapa map) {// Actualisation d'une cellule
        String packet = map.getObjects();
        if (packet.equals(""))
            return;
        send(out, packet);
    }

    public static void GAME_SEND_GM_MOUNT(JuegoCliente out, Mapa map, boolean ok) {
        String packet = map.getGMOfMount(ok);
        if (packet.equals(""))
            return;
        send(out, packet);

    }

    public static void GAME_SEND_Ef_MOUNT_TO_ETABLE(Jugador perso, char c,
                                                    String s) {
        String packet = "Ef" + c + s;
        send(perso, packet);

    }

    public static void GAME_SEND_GA_ACTION_TO_MAP(Mapa mapa, String idUnique,
                                                  int idAction, String s1, String s2) {
        String packet = "GA" + idUnique + ";" + idAction + ";" + s1;
        if (!s2.equals(""))
            packet += ";" + s2;
        for (Jugador z : mapa.getPlayers())
            send(z, packet);

    }

    public static void SEND_GDO_PUT_OBJECT_MOUNT(Mapa map, String str) {
        String packet = "GDO+" + str;
        for (Jugador z : map.getPlayers())
            send(z, packet);

    }

    public static void SEND_GDE_FRAME_OBJECT_EXTERNAL(Mapa map, String str) {
        String packet = "GDE|" + str;
        for (Jugador z : map.getPlayers())
            send(z, packet);

    }

    public static void SEND_GDE_FRAME_OBJECT_EXTERNAL(Jugador perso, String str) {
        String packet = "GDE|" + str;
        send(perso, packet);

    }

    public static void GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(Mapa map, char s, char option, int guid) {
        StringBuilder packet = new StringBuilder();
        packet.append("Go").append(s).append(option).append(guid);
        for (Jugador z : map.getPlayers())
            send(z, packet.toString());

    }

    public static void GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(Pelea fight, int teams, int guid, boolean b) {
        StringBuilder packet = new StringBuilder();
        packet.append("GR").append((b ? "1" : "0")).append(guid);
        if (fight.getState() != 2)
            return;
        for (Peleador f : fight.getFighters(teams)) {
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            if (f.hasLeft())
                continue;
            send(f.getPlayer(), packet.toString());
        }

    }

    public static void GAME_SEND_GJK_PACKET(Jugador out, int state, int cancelBtn, int duel, int spec, int time, int unknown) {
        send(out, "GJK" + state + "|" + cancelBtn + "|" + duel + "|" + spec + "|" + time + "|" + unknown);

    }

    public static void GAME_SEND_FIGHT_PLACES_PACKET(JuegoCliente out, String places, int team) {
        String packet = "GP" + places + "|" + team;

        send(out, packet);

    }

    public static void GAME_SEND_Im_PACKET_TO_ALL(String str) {
        String packet = "Im" + str;
        for (Jugador perso : Mundo.mundo.getOnlinePlayers())
            send(perso, packet);

    }

    public static void GAME_SEND_Im_PACKET(Jugador out, String str) {
        String packet = "Im" + str;
        send(out, packet);

    }

    public static void GAME_SEND_Im_PACKET_TO_MAP(Mapa map, String id) {
        String packet = "Im" + id;
        for (Jugador z : map.getPlayers())
            send(z, packet);
    }

    public static void GAME_SEND_eUK_PACKET_TO_MAP(Mapa map, int guid, int emote) {
        String packet = "eUK" + guid + "|" + emote;
        for (Jugador z : map.getPlayers())
            send(z, packet);

    }

    public static void GAME_SEND_Im_PACKET_TO_FIGHT(Pelea fight, int teams,
                                                    String id) {
        String packet = "Im" + id;
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft())
                continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            send(f.getPlayer(), packet);
        }

    }

    public static void GAME_SEND_MESSAGE(Jugador out, String mess, String color) {
        String packet = "cs<font color='#" + color + "'>" + mess + "</font>";
        send(out, packet);

    }

    public static void GAME_SEND_MESSAGE(Jugador out, String mess) {
        String packet = "cs<font color='#" + Configuracion.INSTANCE.getColorMessage()
                + "'>" + mess + "</font>";
        send(out, packet);
    }

    public static void GAME_SEND_MESSAGE_TO_MAP(Mapa map, String mess,
                                                String color) {
        String packet = "cs<font color='#" + color + "'>" + mess + "</font>";
        for (Jugador z : map.getPlayers())
            send(z, packet);

    }

    public static void GAME_SEND_GA903_ERROR_PACKET(JuegoCliente out, char c,
                                                    int guid) {
        String packet = "GA;903;" + guid + ";" + c;
        send(out, packet);

    }

    public static void GAME_SEND_GIC_PACKETS_TO_FIGHT(Pelea fight, int teams) {
        StringBuilder packet = new StringBuilder();
        packet.append("GIC").append("|");
        for (Peleador p : fight.getFighters(3)) {
            if (p.getCell() == null)
                continue;
            packet.append(p.getId()).append(";").append(p.getCell().getId()).append(";").append("1").append("|");
        }
        for (Peleador perso : fight.getFighters(teams)) {
            if (perso.hasLeft())
                continue;
            if (perso.getPlayer() == null
                    || !perso.getPlayer().isOnline())
                continue;
            send(perso.getPlayer(), packet.toString());
        }

    }

    public static void GAME_SEND_GIC_PACKET_TO_FIGHT(Pelea fight, int teams, Peleador f) {
        StringBuilder packet = new StringBuilder();
        packet.append("GIC").append("|").append(f.getId()).append(";").append(f.getCell().getId()).append(";").append("1").append("|");

        for (Peleador perso : fight.getFighters(teams)) {
            if (perso.hasLeft())
                continue;
            if (perso.getPlayer() == null
                    || !perso.getPlayer().isOnline())
                continue;
            send(perso.getPlayer(), packet.toString());
        }

    }

    public static void GAME_SEND_GIC_PACKETS(Pelea fight, Jugador out) {
        StringBuilder packet = new StringBuilder();
        packet.append("GIC").append("|");
        for (Peleador p : fight.getFighters(3)) {
            if (p.getCell() == null)
                continue;
            packet.append(p.getId()).append(";").append(p.getCell().getId()).append(";").append("1").append("|");
        }
        send(out, packet.toString());

    }

    public static void GAME_SEND_GS_PACKET_TO_FIGHT(Pelea fight, int teams) {
        StringBuilder packet = new StringBuilder();
        packet.append("GS");
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft()) continue;
            f.initBuffStats();
            if (f.getPlayer() == null || !f.getPlayer().isOnline()) continue;
            send(f.getPlayer(), packet.toString());
        }

    }

    public static void GAME_SEND_GS_PACKET(Jugador out) {
        send(out, "GS");

    }

    public static void GAME_SEND_GTL_PACKET_TO_FIGHT(Pelea fight, int teams) {
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft())
                continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            send(f.getPlayer(), fight.getGTL());
        }
    }

    public static void GAME_SEND_GTL_PACKET(Jugador out, Pelea fight) {
        String packet = fight.getGTL();
        send(out, packet);

    }

    public static void GAME_SEND_GTM_PACKET_TO_FIGHT(Pelea fight, int teams) {
        StringBuilder packet = new StringBuilder();
        packet.append("GTM");
        for (Peleador f : fight.getFighters(3)) {
            packet.append("|").append(f.getId()).append(";");
            if (f.isDead()) {
                packet.append("1");
                continue;
            }
            packet.append("0").append(";").append(f.getPdv()).append(";").append(f.getPa()).append(";").append(f.getPm()).append(";");
            packet.append((f.isHide() ? "-1" : f.getCell().getId())).append(";");//On envoie pas la cell d'un invisible :p
            packet.append(";");//??
            packet.append(f.getPdvMax());
        }
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft())
                continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            send(f.getPlayer(), packet.toString());
        }

    }

    public static void GAME_SEND_GTM_PACKET(Jugador out, Pelea fight) {
        StringBuilder packet = new StringBuilder();
        packet.append("GTM");
        for (Peleador f : fight.getFighters(3)) {
            packet.append("|").append(f.getId()).append(";");
            if (f.isDead()) {
                packet.append("1");
                continue;
            } else
                packet.append("0;").append(f.getPdv()).append(";").append(f.getPa()).append(";").append(f.getPm()).append(";");
            packet.append((f.isHide() ? "-1" : f.getCell().getId())).append(";");//On envoie pas la cell d'un invisible :p
            packet.append(";");//??
            packet.append(f.getPdvMax());
        }
        send(out, packet.toString());
    }

    public static void GAME_SEND_GAMETURNSTART_PACKET_TO_FIGHT(Pelea fight, int teams, int guid, int time) {
        StringBuilder packet = new StringBuilder();
        packet.append("GTS").append(guid).append("|").append(time);
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft()) continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline()) continue;
            send(f.getPlayer(), packet.toString());
        }

    }

    public static void GAME_SEND_GAMETURNSTART_PACKET(Jugador P, int guid, int time) {
        send(P, "GTS" + guid + "|" + time);

    }

    public static void GAME_SEND_GV_PACKET(Jugador P) {
        String packet = "GV";
        send(P, packet);

    }

    public static void GAME_SEND_PONG(JuegoCliente out) {
        String packet = "pong";
        send(out, packet);

    }

    public static void GAME_SEND_QPONG(JuegoCliente out) {
        String packet = "qpong";
        send(out, packet);

    }

    public static void GAME_SEND_GAS_PACKET_TO_FIGHT(Pelea fight, int teams, int guid) {
        StringBuilder packet = new StringBuilder();
        packet.append("GAS").append(guid);
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft())
                continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            send(f.getPlayer(), packet.toString());
        }

    }

    //String a StringBuilder para aumentar la velocidad de acciones en pelea
    public static void GAME_SEND_GA_PACKET_TO_FIGHT(Pelea fight, int teams, int actionID, String s1, String s2) {
        StringBuilder packet = new StringBuilder();
        packet.append("GA").append(";").append(actionID).append(";").append(s1);
        if (!s2.equals(""))
            packet.append(";").append(s2);
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft())
                continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            send(f.getPlayer(), packet.toString());
        }
    }

    public static void GAME_SEND_GA_PACKET(Pelea fight, Jugador perso, int actionID, String s1, String s2) {
        StringBuilder packet = new StringBuilder();
        packet.append("GA").append(";").append(actionID).append(";").append(s1);
        if (!s2.equals(""))
            packet.append(";").append(s2);
        send(perso, packet.toString());
    }

    public static void SEND_SB_SPELL_BOOST(Jugador perso, String modif) {
        String packet = "SB" + modif;
        send(perso, packet);
    }

    public static void GAME_SEND_GA_PACKET(JuegoCliente out, String actionID,
                                           String s0, String s1, String s2) {
        String packet = "GA" + actionID + ";" + s0;
        if (!s1.equals(""))
            packet += ";" + s1;
        if (!s2.equals(""))
            packet += ";" + s2;

        send(out, packet);

    }

    public static void GAME_SEND_GA_PACKET_TO_FIGHT(Pelea fight, int teams,
                                                    int gameActionID, String s1, String s2, String s3) {
        String packet = "GA" + gameActionID + ";" + s1 + ";" + s2 + ";" + s3;
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft())
                continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            send(f.getPlayer(), packet);
        }

    }

    public static void GAME_SEND_GAMEACTION_TO_FIGHT(Pelea fight, int teams,
                                                     String packet) {
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft())
                continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            send(f.getPlayer(), packet);
        }

    }

    public static void GAME_SEND_GAF_PACKET_TO_FIGHT(Pelea fight, int teams,
                                                     int i1, int guid) {
        String packet = "GAF" + i1 + "|" + guid;
        for (Peleador f : fight.getFighters(teams)) {
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            send(f.getPlayer(), packet);
        }

    }

    public static void GAME_SEND_BN(Jugador out) {
        String packet = "BN";
        send(out, packet);

    }

    public static void GAME_SEND_BN(JuegoCliente out) {
        String packet = "BN";
        send(out, packet);

    }

    public static void GAME_SEND_GAMETURNSTOP_PACKET_TO_FIGHT(Pelea fight,
                                                              int teams, int guid) {
        String packet = "GTF" + guid;
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft())
                continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;

            send(f.getPlayer(), packet);
        }

    }

    public static void GAME_SEND_GTR_PACKET_TO_FIGHT(Pelea fight, int teams, int guid) {
        StringBuilder packet = new StringBuilder();
        packet.append("GTR").append(guid);
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft())
                continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            send(f.getPlayer(), packet.toString());
        }

    }

    public static void GAME_SEND_EMOTICONE_TO_MAP(Mapa map, int guid, int id) {
        String packet = "cS" + guid + "|" + id;
        for (Jugador z : map.getPlayers())
            send(z, packet);

    }

    public static void GAME_SEND_SPELL_UPGRADE_FAILED(JuegoCliente _out) {
        String packet = "SUE";
        send(_out, packet);

    }

    public static void GAME_SEND_SPELL_UPGRADE_SUCCED(JuegoCliente _out,
                                                      int spellID, int level) {
        String packet = "SUK" + spellID + "~" + level;
        send(_out, packet);

    }

    public static void GAME_SEND_SPELL_LIST(Jugador perso) {
        String packet = perso.parseSpellList();
        send(perso, packet);

    }

    public static void GAME_SEND_FIGHT_PLAYER_DIE_TO_FIGHT(Pelea fight,
                                                           int teams, int guid) {
        String packet = "GA;103;" + guid + ";" + guid;
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft() || f.getPlayer() == null)
                continue;
            if (f.getPlayer().isOnline())
                send(f.getPlayer(), packet);
        }
    }

    public static void GAME_SEND_FIGHT_GIE_TO_FIGHT(Pelea fight, int teams,
                                                    int mType, int cible, int value, String mParam2, String mParam3,
                                                    String mParam4, int turn, int spellID) {
        StringBuilder packet = new StringBuilder();
        packet.append("GIE").append(mType).append(";").append(cible).append(";").append(value).append(";").append(mParam2).append(";").append(mParam3).append(";").append(mParam4).append(";").append(turn).append(";").append(spellID);
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft() || f.getPlayer() == null)
                continue;
            if (f.getPlayer().isOnline())
                send(f.getPlayer(), packet.toString());
        }

    }

    public static void GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(Pelea fight, int teams, Mapa map) {
        StringBuilder packet = new StringBuilder();
        packet.append(map.getFightersGMsPackets(fight));
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft())
                continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            send(f.getPlayer(), packet.toString());
        }

    }

    public static void GAME_SEND_MAP_FIGHT_GMS_PACKETS(Pelea fight, Mapa map,
                                                       Jugador _perso) {
        String packet = map.getFightersGMsPackets(fight);
        send(_perso, packet);

    }

    public static void GAME_SEND_FIGHT_PLAYER_JOIN(Pelea fight, int teams,
                                                   Peleador _fighter) {
        String packet = _fighter.getGmPacket('+', true);

        for (Peleador f : fight.getFighters(teams)) {
            if (f != _fighter) {
                if (f.getPlayer() == null || !f.getPlayer().isOnline())
                    continue;
                if (f.getPlayer() != null
                        && f.getPlayer().getGameClient() != null) {
                    send(f.getPlayer(), packet);
                }
            }
        }

    }

    public static void GAME_SEND_cMK_PACKET(Jugador perso, String suffix,
                                            int guid, String name, String msg) {
        String packet = "cMK" + suffix + "|" + guid + "|" + name + "|" + msg;
        send(perso, packet);

    }

    public static void GAME_SEND_FIGHT_LIST_PACKET(JuegoCliente out, Mapa map) {
        StringBuilder packet = new StringBuilder();
        packet.append("fL");
        for (Pelea entry : map.getFights()) {
            if (packet.length() > 2)
                packet.append("|");
            packet.append(entry.parseFightInfos());
        }
        send(out, packet.toString());

    }

    public static void GAME_SEND_cMK_PACKET_TO_MAP(Mapa map, String suffix,
                                                   int guid, String name, String msg) {
        String packet = "cMK" + suffix + "|" + guid + "|" + name + "|" + msg;
        for (Jugador z : map.getPlayers())
            send(z, packet);
    }

    public static void GAME_SEND_cMK_PACKET_TO_GUILD(Gremio g, String suffix,
                                                     int guid, String name, String msg) {
        String packet = "cMK" + suffix + "|" + guid + "|" + name + "|" + msg;
        for (Jugador perso : g.getPlayers()) {
            if (perso == null || !perso.isOnline())
                continue;
            send(perso, packet);
        }
    }

    public static void GAME_SEND_cMK_PACKET_TO_ALL(Jugador perso, String suffix,
                                                   int guid, String name, String msg) {
        String packet = "cMK" + suffix + "|" + guid + "|" + name + "|" + msg;
        if (perso.getLevel() < 6) {
            GestorSalida.GAME_SEND_MESSAGE(perso, "Ce canal n'est accessible qu'à partir du niveau <b>6</b>.");
            GAME_SEND_BN(perso);
            return;
        }
        for (Jugador perso1 : Mundo.mundo.getOnlinePlayers())
            send(perso1, packet);
    }

    public static void GAME_SEND_cMK_PACKET_TO_ALIGN(String suffix, int guid,
                                                     String name, String msg, Jugador _perso) {
        String packet = "cMK" + suffix + "|" + guid + "|" + name + "|" + msg;
        for (Jugador perso : Mundo.mundo.getOnlinePlayers()) {
            if (perso.get_align() == _perso.get_align()) {
                send(perso, packet);
            }
        }
    }

    public static void GAME_SEND_cMK_PACKET_TO_ADMIN(String suffix, int guid,
                                                     String name, String msg) {
        String packet = "cMK" + suffix + "|" + guid + "|" + name + "|" + msg;
        for (Jugador perso : Mundo.mundo.getOnlinePlayers())
            if (perso.isOnline())
                if (perso.getAccount() != null)
                    if (perso.getGroupe() != null)
                        send(perso, packet);
    }

    public static void GAME_SEND_cMK_PACKET_TO_FIGHT(Pelea fight, int teams,
                                                     String suffix, int guid, String name, String msg) {
        String packet = "cMK" + suffix + "|" + guid + "|" + name + "|" + msg;
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft())
                continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            send(f.getPlayer(), packet);
        }

    }

    public static void GAME_SEND_GDZ_PACKET_TO_FIGHT(Pelea fight, int teams,
                                                     String suffix, int cell, int size, int unk) {
        String packet = "GDZ" + suffix + cell + ";" + size + ";" + unk;

        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft())
                continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            send(f.getPlayer(), packet);
        }

    }

    public static void GAME_SEND_GDC_PACKET_TO_FIGHT(Pelea fight, int teams,
                                                     int cell) {
        String packet = "GDC" + cell;

        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft())
                continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            send(f.getPlayer(), packet);
        }

    }

    public static void GAME_SEND_GA2_PACKET(JuegoCliente out, int guid) {
        String packet = "GA;2;" + guid + ";";
        send(out, packet);

    }

    public static void GAME_SEND_CHAT_ERROR_PACKET(JuegoCliente out, String name) {
        String packet = "cMEf" + name;
        send(out, packet);

    }

    public static void GAME_SEND_eD_PACKET_TO_MAP(Mapa map, int guid, int dir) {
        String packet = "eD" + guid + "|" + dir;
        for (Jugador z : map.getPlayers())
            send(z, packet);

    }

    public static void GAME_SEND_ECK_PACKET(Jugador out, int type, String str) {
        String packet = "ECK" + type;
        if (!str.equals(""))
            packet += "|" + str;
        send(out, packet);

    }

    public static void GAME_SEND_ECK_PACKET(JuegoCliente out, int type, String str) {
        String packet = "ECK" + type;
        if (!str.equals(""))
            packet += "|" + str;
        send(out, packet);

    }

    public static void GAME_SEND_ITEM_VENDOR_LIST_PACKET(JuegoCliente out, Npc npc) {
        send(out, "EL" + npc.getTemplate().getItemVendorList());
    }

    public static void GAME_SEND_ITEM_LIST_PACKET_PERCEPTEUR(JuegoCliente out, Recaudador perco) {
        send(out, "EL" + perco.getItemCollectorList());
    }

    public static void GAME_SEND_ITEM_LIST_PACKET_SELLER(Jugador p, Jugador out) {
        send(out, "EL" + p.parseStoreItemsList());
    }

    public static void GAME_SEND_EV_PACKET(JuegoCliente out) {
        String packet = "EV";
        send(out, packet);

    }

    public static void GAME_SEND_DCK_PACKET(JuegoCliente out, int id) {
        String packet = "DCK" + id;
        send(out, packet);

    }

    public static void GAME_SEND_QUESTION_PACKET(JuegoCliente out, String str) {
        String packet = "DQ" + str;
        send(out, packet);

    }

    public static void GAME_SEND_END_DIALOG_PACKET(JuegoCliente out) {
        String packet = "DV";
        send(out, packet);

    }

    public static void GAME_SEND_BUY_ERROR_PACKET(JuegoCliente out) {
        String packet = "EBE";
        send(out, packet);

    }

    public static void GAME_SEND_SELL_ERROR_PACKET(JuegoCliente out) {
        String packet = "ESE";
        send(out, packet);

    }

    public static void GAME_SEND_BUY_OK_PACKET(JuegoCliente out) {
        String packet = "EBK";
        send(out, packet);

    }

    public static void GAME_SEND_OBJECT_QUANTITY_PACKET(Jugador out, ObjetoJuego obj) {
        send(out, "OQ" + obj.getId() + "|" + obj.getCantidad());

    }

    public static void GAME_SEND_OAKO_PACKET(Jugador out, ObjetoJuego obj) {
        String packet = "OAKO" + obj.parseItem();
        send(out, packet);
    }

    public static void GAME_SEND_ESK_PACKEt(Jugador out) {
        String packet = "ESK";
        send(out, packet);

    }

    public static void GAME_SEND_REMOVE_ITEM_PACKET(Jugador out, int guid) {
        String packet = "OR" + guid;
        send(out, packet);

    }

    public static void GAME_SEND_DELETE_OBJECT_FAILED_PACKET(JuegoCliente out) {
        String packet = "OdE";
        send(out, packet);

    }

    public static void GAME_SEND_OBJET_MOVE_PACKET(Jugador out, ObjetoJuego obj) {
        String packet = "OM" + obj.getId() + "|";
        if (obj.getPosicion() != Constantes.ITEM_POS_NO_EQUIPED)
            packet += obj.getPosicion();

        send(out, packet);

    }

    public static void GAME_SEND_DELETE_STATS_ITEM_FM(Jugador perso, int id) {
        String packet = "OR" + id;
        send(perso, packet);

    }

    public static void GAME_SEND_EMOTICONE_TO_FIGHT(Pelea fight, int teams,
                                                    int guid, int id) {
        String packet = "cS" + guid + "|" + id;
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft())
                continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            send(f.getPlayer(), packet);
        }

    }

    public static void GAME_SEND_OAEL_PACKET(JuegoCliente out) {
        String packet = "OAEL";
        send(out, packet);

    }

    public static void GAME_SEND_NEW_LVL_PACKET(JuegoCliente out, int lvl) {
        String packet = "AN" + lvl;
        send(out, packet);

    }

    public static void GAME_SEND_MESSAGE_TO_ALL(String msg, String color) {
        String packet = "cs<font color='#" + color + "'>" + msg + "</font>";
        for (Jugador P : Mundo.mundo.getOnlinePlayers())
            send(P, packet);
    }

    public static void GAME_SEND_EXCHANGE_REQUEST_OK(JuegoCliente out, int guid,
                                                     int guidT, int msgID) {
        String packet = "ERK" + guid + "|" + guidT + "|" + msgID;
        send(out, packet);

    }

    public static void GAME_SEND_EXCHANGE_REQUEST_ERROR(JuegoCliente out, char c) {
        String packet = "ERE" + c;
        send(out, packet);

    }

    public static void GAME_SEND_EXCHANGE_CONFIRM_OK(JuegoCliente out, int type) {
        String packet = "ECK" + type;
        send(out, packet);

    }

    public static void GAME_SEND_EXCHANGE_MOVE_OK(Jugador out, char type,
                                                  String signe, String s1) {
        String packet = "EMK" + type + signe;
        if (!s1.equals(""))
            packet += s1;
        send(out, packet);

    }

    public static void GAME_SEND_EXCHANGE_MOVE_OK_FM(Jugador out, char type,
                                                     String signe, String s1) {
        String packet = "EmK" + type + signe;
        if (!s1.equals(""))
            packet += s1;
        send(out, packet);

    }

    public static void GAME_SEND_EXCHANGE_OTHER_MOVE_OK(JuegoCliente out,
                                                        char type, String signe, String s1) {
        String packet = "EmK" + type + signe;
        if (!s1.equals(""))
            packet += s1;
        send(out, packet);

    }

    public static void GAME_SEND_EXCHANGE_OTHER_MOVE_OK_FM(JuegoCliente out,
                                                           char type, String signe, String s1) {
        String packet = "EMK" + type + signe;
        if (!s1.equals(""))
            packet += s1;
        send(out, packet);

    }

    public static void GAME_SEND_EXCHANGE_OK(JuegoCliente out, boolean ok,
                                             int guid) {
        String packet = "EK" + (ok ? "1" : "0") + guid;
        send(out, packet);

    }

    public static void GAME_SEND_EXCHANGE_OK(JuegoCliente out, boolean ok) {
        String str = "EK" + (ok ? "1" : "0");
        send(out, str);
    }

    public static void GAME_SEND_EXCHANGE_VALID(JuegoCliente out, char c) {
        String packet = "EV" + c;
        send(out, packet);

    }

    public static void GAME_SEND_GROUP_INVITATION_ERROR(JuegoCliente out, String s) {
        String packet = "PIE" + s;
        send(out, packet);

    }

    public static void GAME_SEND_GROUP_INVITATION(JuegoCliente out, String n1,
                                                  String n2) {
        String packet = "PIK" + n1 + "|" + n2;
        send(out, packet);

    }

    public static void GAME_SEND_GROUP_CREATE(JuegoCliente out,
                                              Grupo g) {
        String packet = "PCK" + g.getChief().getName();
        send(out, packet);

    }

    public static void GAME_SEND_PL_PACKET(JuegoCliente out,
                                           Grupo g) {
        String packet = "PL" + g.getChief().getId();
        send(out, packet);

    }

    public static void GAME_SEND_PR_PACKET(Jugador out) {
        String packet = "PR";
        send(out, packet);

    }

    public static void GAME_SEND_PV_PACKET(JuegoCliente out, String s) {
        String packet = "PV" + s;
        send(out, packet);

    }

    public static void GAME_SEND_ALL_PM_ADD_PACKET(JuegoCliente out,
                                                   Grupo g) {
        StringBuilder packet = new StringBuilder();
        packet.append("PM+");
        boolean first = true;
        for (Jugador p : g.getPlayers()) {
            if (!first)
                packet.append("|");
            packet.append(p.parseToPM());
            first = false;
        }
        send(out, packet.toString());

    }/*
17:44:58.337 [NioProcessor-6] TRACE org.starloco.locos.game.world.World - Comette --> OAKO1acab0~3a6~1~~7d#11#0#0#0d0+17;
17:44:58.337 [NioProcessor-6] TRACE org.starloco.locos.game.world.World - Comette --> ErKO+1755824|1|934|7d#11#0#0#0d0+17
17:44:58.337 [NioProcessor-6] TRACE org.starloco.locos.game.world.World - Comette --> EcK;934;BRoulbab;7d#11#0#0#0d0+17
17:44:58.338 [NioProcessor-6] TRACE org.starloco.locos.game.world.World - Comette --> IO298|+934
17:44:58.343 [NioProcessor-6] TRACE org.starloco.locos.game.world.World - Comette --> Ow110|6755
17:44:58.343 [NioProcessor-3] TRACE org.starloco.locos.game.world.World - Roulbab <-- EK
17:44:58.343 [NioProcessor-6] TRACE org.starloco.locos.game.world.World - Comette --> As1563767088,1534506000,1616294000|665800|2|46|1~1,1,1,0,0,0|970,970|1000,10000|479|100|7,0,0,0,7|3,0,0,0,3|251,0,0,0|0,0,0,0|155,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|1,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|38,0,0,0,0|38,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|
17:44:58.343 [NioProcessor-3] TRACE org.starloco.locos.game.world.World - Roulbab --> EK1298
17:44:58.343 [NioProcessor-6] TRACE org.starloco.locos.game.world.World - Comette --> EK0298
17:44:58.343 [NioProcessor-3] TRACE org.starloco.locos.game.world.World - Roulbab --> OQ1755788|30
17:44:58.343 [NioProcessor-6] TRACE org.starloco.locos.game.world.World - Comette --> EK01157
17:44:58.343 [NioProcessor-3] TRACE org.starloco.locos.game.world.World - Roulbab --> OQ1755762|29
17:44:58.343 [NioProcessor-3] TRACE org.starloco.locos.game.world.World - Roulbab --> OQ1755782|27
17:44:58.343 [NioProcessor-3] TRACE org.starloco.locos.game.world.World - Roulbab --> Ow2057|12557
17:44:58.344 [NioProcessor-3] TRACE org.starloco.locos.game.world.World - Roulbab --> ErKO+1755824|1|934|7d#11#0#0#0d0+17
17:44:58.344 [NioProcessor-3] TRACE org.starloco.locos.game.world.World - Roulbab --> EcK;934;TComette;7d#11#0#0#0d0+17
17:44:58.344 [NioProcessor-3] TRACE org.starloco.locos.game.world.World - Roulbab --> Ow2057|12557
17:44:58.344 [NioProcessor-3] TRACE org.starloco.locos.game.world.World - Roulbab --> IO298|+934
17:44:58.344 [NioProcessor-3] TRACE org.starloco.locos.game.world.World - Roulbab --> JX|27;100;581687;500000000;581687;
17:44:58.344 [NioProcessor-3] TRACE org.starloco.locos.game.world.World - Roulbab --> Ow2057|12557
17:44:58.344 [NioProcessor-3] TRACE org.starloco.locos.game.world.World - Roulbab --> As7411963754,7407232000,7407232000|12588|964|68|0~0,0,1,0,0,0|101095,101095|2000,10000|29284|220|7,1,0,0,8|3,0,0,0,3|1000,12,0,0|100000,50,0,0|1000,0,0,0|1000,0,0,0|1000,0,0,0|1000,12,0,0|0,0,0,0|1,0,0,0|0,2,0,0|0,0,0,0|0,0,0,0|0,14,0,0|0,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|0,0,0,0|250,0,0,0,0|250,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|0,0,0,0,0|
17:44:58.344 [NioProcessor-3] TRACE org.starloco.locos.game.world.World - Roulbab --> EK0298
17:44:58.344 [NioProcessor-3] TRACE org.starloco.locos.game.world.World - Roulbab --> EK01157
*/
    public static void GAME_SEND_PM_ADD_PACKET_TO_GROUP(
            Grupo g, Jugador p) {
        String packet = "PM+" + p.parseToPM();
        for (Jugador P : g.getPlayers())
            send(P, packet);

    }

    public static void GAME_SEND_PM_MOD_PACKET_TO_GROUP(
            Grupo g, Jugador p) {
        String packet = "PM~" + p.parseToPM();
        for (Jugador P : g.getPlayers())
            send(P, packet);

    }

    public static void GAME_SEND_PM_DEL_PACKET_TO_GROUP(
            Grupo party, int guid) {
        String packet = "PM-" + guid;
        for (Jugador P : party.getPlayers())
            send(P, packet);

    }

    public static void GAME_SEND_cMK_PACKET_TO_GROUP(
            Grupo g, String s, int guid, String name,
            String msg) {
        String packet = "cMK" + s + "|" + guid + "|" + name + "|" + msg + "|";
        for (Jugador P : g.getPlayers())
            send(P, packet);

    }

    public static void GAME_SEND_FIGHT_DETAILS(JuegoCliente out, Pelea fight) {
        if (fight == null)
            return;
        StringBuilder packet = new StringBuilder();
        packet.append("fD").append(fight.getId()).append("|");
        fight.getFighters(1).stream().filter(f -> !f.isInvocation()).forEach(f -> packet.append(f.getPacketsName()).append("~").append(f.getLvl()).append(";"));
        packet.append("|");
        fight.getFighters(2).stream().filter(f -> !f.isInvocation()).forEach(f -> packet.append(f.getPacketsName()).append("~").append(f.getLvl()).append(";"));
        send(out, packet.toString());

    }

    public static void GAME_SEND_IQ_PACKET(Jugador perso, int guid, int qua) {
        String packet = "IQ" + guid + "|" + qua;
        send(perso, packet);

    }

    public static void GAME_SEND_JN_PACKET(Jugador perso, int jobID, int lvl) {
        String packet = "JN" + jobID + "|" + lvl;
        send(perso, packet);

    }

    public static void GAME_SEND_GDF_PACKET_TO_MAP(Mapa map, GameCase cell) {
        int cellID = cell.getId();
        Mapa.ObjetosInteractivos object = cell.getObject();
        String packet = "GDF|" + cellID + ";" + object.getState() + ";"
                + (object.isInteractive() ? "1" : "0");
        for (Jugador z : map.getPlayers())
            send(z, packet);

    }

    public static void GAME_SEND_GDF_PACKET_TO_FIGHT(Jugador player, Collection<GameCase> collection) {
        StringBuilder packet = new StringBuilder();
        packet.append("GDF").append("|");
        for (GameCase cell : collection) {
            if (cell.getObject() == null)
                continue;
            if (cell.getObject().getTemplate() == null)
                continue;

            switch (cell.getObject().getTemplate().getId()) {
                case 7515, 7511, 7517, 7512, 7513, 7516, 7550, 7518, 7534, 7535, 7533, 7551, 7500, 7536, 7501, 7502, 7503, 7542, 7541, 7504, 7553, 7505, 7506, 7507, 7557, 7554, 7508, 7509, 7552 -> packet.append(cell.getId()).append(";").append("1").append(";").append("0").append("|");
            }
        }
        send(player, packet.toString());
    }

    public static void GAME_SEND_GA_PACKET_TO_MAP(Mapa map, String gameActionID, int actionID, String s1, String s2) {
        String packet = "GA" + gameActionID + ";" + actionID + ";" + s1;
        if (!s2.equals(""))
            packet += ";" + s2;

        for (Jugador z : map.getPlayers())
            send(z, packet);

    }

    public static void GAME_SEND_EL_BANK_PACKET(Jugador perso) {
        send(perso, "EL" + perso.parseBankPacket());
    }

    public static void GAME_SEND_EL_TRUNK_PACKET(Jugador perso, Cofres t) {
        send(perso, "EL" + t.parseToTrunkPacket());
    }

    public static void GAME_SEND_JX_PACKET(Jugador perso, ArrayList<OficioCaracteristicas> SMs) {
        StringBuilder packet = new StringBuilder();
        packet.append("JX");
        for (OficioCaracteristicas sm : SMs)
            packet.append("|").append(sm.getTemplate().getId()).append(";").append(sm.get_lvl()).append(";").append(sm.getXpString(";")).append(";");
        send(perso, packet.toString());

    }

    public static void GAME_SEND_JO_PACKET(Jugador perso,
                                           ArrayList<OficioCaracteristicas> JobStats) {
        for (OficioCaracteristicas SM : JobStats) {
            String packet = "JO" + SM.getPosition() + "|" + SM.getOptBinValue()
                    + "|" + SM.getSlotsPublic();
            send(perso, packet);
        }
    }

    public static void GAME_SEND_JO_PACKET(Jugador perso, OficioCaracteristicas SM) {
        String packet = "JO" + SM.getPosition() + "|" + SM.getOptBinValue()
                + "|" + SM.getSlotsPublic();
        send(perso, packet);
    }

    public static void GAME_SEND_JS_PACKET(Jugador perso, ArrayList<OficioCaracteristicas> SMs) {
        StringBuilder packet = new StringBuilder("JS");
        for (OficioCaracteristicas sm : SMs) {
            packet.append(sm.parseJS());
        }
        send(perso, packet.toString());

    }

    public static void GAME_SEND_EsK_PACKET(Jugador perso, String str) {
        String packet = "EsK" + str;
        send(perso, packet);

    }

    public static void GAME_SEND_FIGHT_SHOW_CASE(ArrayList<JuegoCliente> PWs,
                                                 int guid, int cellID) {
        String packet = "Gf" + guid + "|" + cellID;
        for (JuegoCliente PW : PWs) {
            send(PW, packet);
        }

    }

    public static void GAME_SEND_Ea_PACKET(Jugador perso, String str) {
        send(perso, "Ea" + str);
    }

    public static void GAME_SEND_EA_PACKET(Jugador perso, String str) {
        send(perso, "EA" + str);
    }

    public static void GAME_SEND_Ec_PACKET(Jugador perso, String str) {
        String packet = "Ec" + str;
        send(perso, packet);

    }

    public static void GAME_SEND_Em_PACKET(Jugador perso, String str) {
        String packet = "Em" + str;
        send(perso, packet);

    }

    public static void GAME_SEND_IO_PACKET_TO_MAP(Mapa map, int guid, String str) {
        String packet = "IO" + guid + "|" + str;
        for (Jugador z : map.getPlayers())
            send(z, packet);

    }

    public static void GAME_SEND_FRIENDLIST_PACKET(Jugador perso) {
        String packet = "FL" + perso.getAccount().parseFriendList();
        send(perso, packet);
        if (perso.getWife() != 0) {
            String packet2 = "FS" + perso.get_wife_friendlist();
            send(perso, packet2);
        }
    }

    public static void GAME_SEND_FRIEND_ONLINE(Jugador friend, Jugador perso) {
        String packet = "Im0143;" + friend.getAccount().getPseudo()
                + " (<b><a href='asfunction:onHref,ShowPlayerPopupMenu,"
                + friend.getName() + "'>" + friend.getName() + "</a></b>)";
        send(perso, packet);

    }

    public static void GAME_SEND_FA_PACKET(Jugador perso, String str) {
        String packet = "FA" + str;
        send(perso, packet);

    }

    public static void GAME_SEND_FD_PACKET(Jugador perso, String str) {
        String packet = "FD" + str;
        send(perso, packet);

    }

    public static void GAME_SEND_Rp_PACKET(Jugador perso, Cercados MP) {
        StringBuilder packet = new StringBuilder();
        if (MP == null)
            return;

        packet.append("Rp").append(MP.getOwner()).append(";").append(MP.getPrice()).append(";").append(MP.getSize()).append(";").append(MP.getMaxObject()).append(";");

        Gremio G = MP.getGuild();
        //Si une guilde est definie
        if (G != null) {
            packet.append(G.getName()).append(";").append(G.getEmblem());
        } else {
            packet.append(";");
        }

        send(perso, packet.toString());
    }

    public static void GAME_SEND_OS_PACKET(Jugador perso, int pano) {
        StringBuilder packet = new StringBuilder();
        packet.append("OS");
        int num = perso.getNumbEquipedItemOfPanoplie(pano);
        if (num <= 0)
            packet.append("-").append(pano);
        else {
            packet.append("+").append(pano).append("|");
            ObjetoSet IS = Mundo.mundo.getItemSet(pano);
            if (IS != null) {
                StringBuilder items = new StringBuilder();
                //Pour chaque objet de la pano
                for (ObjetoModelo OT : IS.getItemTemplates()) {
                    //Si le joueur l'a �quip�
                    if (perso.hasEquiped(OT.getId())) {
                        //On l'ajoute au packet
                        if (items.length() > 0)
                            items.append(";");
                        items.append(OT.getId());
                    }
                }
                packet.append(items.toString()).append("|").append(IS.getBonusStatByItemNumb(num).parseToItemSetStats());
            }
        }
        send(perso, packet.toString());

    }

    public static void GAME_SEND_MOUNT_DESCRIPTION_PACKET(Jugador perso,
                                                          Montura DD) {
        String packet = "Rd" + DD.parse();

        send(perso, packet);

    }

    public static void GAME_SEND_Rr_PACKET(Jugador perso, String str) {
        String packet = "Rr" + str;
        send(perso, packet);

    }

    public static void GAME_SEND_ALTER_GM_PACKET(Mapa map, Jugador perso) {
        StringBuilder packet = new StringBuilder();
        packet.append("GM").append("|").append("~").append(perso.parseToGM());
        for (Jugador z : map.getPlayers()) {
            if (perso.get_size() > 0)
                send(z, packet.toString());
            else if (z.getGroupe() != null)
                send(z, packet.toString());
        }
    }

    public static void MESSAGE_BOX(JuegoCliente out, String args) {
        String packet = "M"+args;
        send(out, packet);
    }

    public static void GAME_SEND_Ee_PACKET(Jugador perso, char c, String s) {
        String packet = "Ee" + c + s;
        send(perso, packet);

    }

    public static void GAME_SEND_Ee_PACKET_WAIT(Jugador perso, char c, String s) {
        String packet = "Ee" + c + s;
        send(perso, packet);

    }
    public static void GAME_SEND_cC_PACKET(Jugador perso, char c, String s) {
        String packet = "cC" + c + s;
        send(perso, packet);

    }

    public static void GAME_SEND_ADD_NPC_TO_MAP(Mapa map, Npc npc) {
        for (Jugador z : map.getPlayers())
            send(z, "GM|" + npc.parse(false, z));
    }

    public static void GAME_SEND_ADD_NPC(Jugador player, Npc npc) {
        send(player, "GM|" + npc.parse(false, player));
    }

    public static void GAME_SEND_ADD_PERCO_TO_MAP(Mapa map) {
        String packet = "GM|" + Recaudador.parseGM(map);
        for (Jugador z : map.getPlayers())
            send(z, packet);

    }

    public static void GAME_SEND_GDO_PACKET_TO_MAP(Mapa map, char c, int cell,
                                                   int itm, int i) {
        String packet = "GDO" + c + cell + ";" + itm + ";" + i;
        for (Jugador z : map.getPlayers())
            send(z, packet);

    }

    public static void GAME_SEND_GDO_PACKET(Jugador p, char c, int cell,
                                            int itm, int i) {
        String packet = "GDO" + c + cell + ";" + itm + ";" + i;
        send(p, packet);

    }

    public static void GAME_SEND_ZC_PACKET(Jugador p, int a) {
        String packet = "ZC" + a;
        send(p, packet);

    }

    public static void GAME_SEND_GIP_PACKET(Jugador p, int a) {
        String packet = "GIP" + a;
        send(p, packet);

    }

    public static void GAME_SEND_gn_PACKET(Jugador p) {
        String packet = "gn";
        send(p, packet);

    }

    public static void GAME_SEND_gC_PACKET(Jugador p, String s) {
        String packet = "gC" + s;
        send(p, packet);

    }

    public static void GAME_SEND_gV_PACKET(Jugador p) {
        String packet = "gV";
        send(p, packet);

    }

    public static void GAME_SEND_gIM_PACKET(Jugador p, Gremio g, char c) {
        String packet = "gIM" + c;
        if (c == '+') {
            packet += g.parseMembersToGM();
        }
        send(p, packet);

    }

    public static void GAME_SEND_gIB_PACKET(Jugador p, String infos) {
        String packet = "gIB" + infos;
        send(p, packet);

    }

    public static void GAME_SEND_gIH_PACKET(Jugador p, String infos) {
        String packet = "gIH" + infos;
        send(p, packet);

    }

    public static void GAME_SEND_gS_PACKET(Jugador p, GremioMiembros gm) {
        send(p, "gS" + gm.getGuild().getName() + "|" + gm.getGuild().getEmblem().replace(',', '|') + "|" + gm.parseRights());

    }

    public static void GAME_SEND_gJ_PACKET(Jugador p, String str) {
        String packet = "gJ" + str;
        send(p, packet);

    }

    public static void GAME_SEND_gK_PACKET(Jugador p, String str) {
        String packet = "gK" + str;
        send(p, packet);

    }

    public static void GAME_SEND_gIG_PACKET(Jugador p, Gremio g) {
        long xpMin = Mundo.mundo.getExpLevel(g.getLvl()).guilde;
        long xpMax;
        if (Mundo.mundo.getExpLevel(g.getLvl() + 1) == null) {
            xpMax = -1;
        } else {
            xpMax = Mundo.mundo.getExpLevel(g.getLvl() + 1).guilde;
        }
        send(p, "gIG" + (g.haveTenMembers() ? 1 : 0) + "|" + g.getLvl() + "|" + xpMin + "|" + g.getXp() + "|" + xpMax);

    }

    public static void REALM_SEND_MESSAGE(JuegoCliente out, String args) {
        String packet = "M" + args;
        send(out, packet);

    }

    public static void GAME_SEND_WC_PACKET(Jugador perso) {
        String packet = "WC" + perso.parseZaapList();
        send(perso.getGameClient(), packet);

    }

    public static void GAME_SEND_WV_PACKET(Jugador out) {
        String packet = "WV";
        send(out, packet);

    }

    public static void GAME_SEND_ZAAPI_PACKET(Jugador perso, String list) {
        String packet = "Wc" + perso.getCurMap().getId() + "|" + list;
        send(perso, packet);

    }

    public static void GAME_SEND_CLOSE_ZAAPI_PACKET(Jugador out) {
        String packet = "Wv";
        send(out, packet);

    }

    public static void GAME_SEND_WUE_PACKET(Jugador out) {
        String packet = "WUE";
        send(out, packet);

    }

    public static void GAME_SEND_EMOTE_LIST(Jugador perso, String s) {
        send(perso, "eL" + s);
    }

    public static void GAME_SEND_NO_EMOTE(Jugador out) {
        String packet = "eUE";
        send(out, packet);

    }

    public static void REALM_SEND_TOO_MANY_PLAYER_ERROR(JuegoCliente out) {
        String packet = "AlEw";
        send(out, packet);

    }

    public static void REALM_SEND_REQUIRED_APK(JuegoCliente out)//FIXME:G�n�rateur de nom
    {
        String pass = "";
        String noms = "fantasy;mr;beau;fort;dark;knight;sword;big;boss;chuck;norris;wood;rick;roll;food;play;volt;rick;ven;bana;sam;ron;fou;pui;to;fu;lo;rien;bank;cap;chap;fort;dou;soleil;gentil;mechant;bad;killer;fight;gra;evil;dark;jerry;fatal;haut;bas;arc;epe;cac;ec;mai;invo;tro;com;koi;bou;let;top;fun;fai;sony;kani;meulou;faur;asus;choa;chau;cho;miel;beur;pain;cry;big;sma;to;day;bi;cih;geni;bou;che;scania;dave;swi;cas;que;chi;er;de;nul;do;a;b;c;d;e;f;g;h;i;j;k;l;m;n;o;p;q;r;s;t;u;v;w;x;y;z;a;e;i;o;u;y";
        String[] str = noms.split(";");
        StringBuilder rep = new StringBuilder();
        int tiree = 0;
        int maxi = (int) Math.floor(Math.random() * 4D) + 2;
        for (int x = 0; x < maxi; x++) {
            rep.append(str[(int) Math.floor(Math.random()
                    * str.length)]);
            if (maxi >= 3 && x == 0 && tiree == 0
                    && (int) Math.floor(Math.random() * 2D) == 1) {
                rep.append("-");
                tiree = 1;
            }
        }

        rep = new StringBuilder(rep.substring(0, 1).toUpperCase() + rep.substring(1));
        pass = rep.toString();
        String packet = "APK" + pass;
        send(out, packet);
    }

    public static void GAME_SEND_ADD_ENEMY(Jugador out, Jugador pr) {

        String packet = "iAK" + pr.getAccount().getName() + ";2;"
                + pr.getName() + ";36;10;0;100.FL.";
        send(out, packet);

    }

    public static void GAME_SEND_iAEA_PACKET(Jugador out) {

        String packet = "iAEA.";
        send(out, packet);

    }

    public static void GAME_SEND_ENEMY_LIST(Jugador perso) {

        String packet = "iL" + perso.getAccount().parseEnemyList();
        send(perso, packet);

    }

    public static void GAME_SEND_iD_COMMANDE(Jugador perso, String str) {
        String packet = "iD" + str;
        send(perso, packet);

    }

    public static void GAME_SEND_BWK(Jugador perso, String str) {
        String packet = "BWK" + str;
        send(perso, packet);

    }

    public static void GAME_SEND_KODE(Jugador perso, String str) {
        String packet = "K" + str;
        send(perso, packet);

    }

    public static void GAME_SEND_hOUSE(Jugador perso, String str) {
        send(perso, "h" + str);
    }

    public static void GAME_SEND_FORGETSPELL_INTERFACE(char sign, Jugador perso) {
        String packet = "SF" + sign;
        send(perso, packet);

    }

    public static void GAME_SEND_R_PACKET(Jugador perso, String str) {
        String packet = "R" + str;
        send(perso, packet);

    }

    public static void GAME_SEND_gIF_PACKET(Jugador perso, String str) {
        String packet = "gIF" + str;
        send(perso, packet);
    }

    public static void GAME_SEND_gITM_PACKET(Jugador perso, String str) {
        String packet = "gITM" + str;
        send(perso, packet);

    }

    public static void GAME_SEND_gITp_PACKET(Jugador perso, String str) {
        String packet = "gITp" + str;
        send(perso, packet);

    }

    public static void GAME_SEND_gITP_PACKET(Jugador perso, String str) {
        String packet = "gITP" + str;
        send(perso, packet);

    }

    public static void GAME_SEND_IH_PACKET(Jugador perso, String str) {
        send(perso, "IH" + str);

    }

    public static void GAME_SEND_FLAG_PACKET(Jugador perso, Jugador cible) {
        String packet = "IC" + cible.getCurMap().getX() + "|"
                + cible.getCurMap().getY();
        send(perso, packet);

    }

    public static void GAME_SEND_FLAG_PACKET(Jugador perso, Mapa CurMap) {
        String packet = "IC" + CurMap.getX() + "|" + CurMap.getY();
        send(perso, packet);

    }

    public static void GAME_SEND_DELETE_FLAG_PACKET(Jugador perso) {
        String packet = "IC|";
        send(perso, packet);

    }

    public static void GAME_SEND_gT_PACKET(Jugador perso, String str) {
        String packet = "gT" + str;
        send(perso, packet);

    }

    public static void GAME_SEND_GUILDHOUSE_PACKET(Jugador perso) {
        String packet = "gUT";
        send(perso, packet);

    }

    public static void GAME_SEND_GUILDENCLO_PACKET(Jugador perso) {
        String packet = "gUF";
        send(perso, packet);

    }

    /**
     * HDV *
     */
    public static void GAME_SEND_EHm_PACKET(Jugador out, String sign, String str) {
        String packet = "EHm" + sign + str;

        send(out, packet);

    }

    public static void GAME_SEND_EHM_PACKET(Jugador out, String sign, String str) {
        String packet = "EHM" + sign + str;

        send(out, packet);

    }

    public static void GAME_SEND_EHP_PACKET(Jugador out, int templateID) //Packet d'envoie du prix moyen du template (En r�ponse a un packet EHP)
    {

        String packet = "EHP" + templateID + "|"
                + Mundo.mundo.getObjetoModelo(templateID).getAvgPrice();

        send(out, packet);

    }

    public static void GAME_SEND_EHl(Jugador out, Mercadillo seller, int templateID) {
        if(seller == null) return;
        String packet = "EHl" + seller.parseToEHl(templateID);
        send(out, packet);
    }

    public static void GAME_SEND_EHL_PACKET(Jugador out, int categ,
                                            String templates) //Packet de listage des templates dans une cat�gorie (En r�ponse au packet EHT)
    {
        String packet = "EHL" + categ + "|" + templates;

        send(out, packet);

    }

    public static void GAME_SEND_EHL_PACKET(Jugador out, String items) //Packet de listage des objets en vente
    {
        String packet = "EHL" + items;

        send(out, packet);

    }

    public static void GAME_SEND_HDVITEM_SELLING(Jugador perso) {
        StringBuilder packet = new StringBuilder();
        packet.append("EL");
        MercadilloEntrada[] entries = perso.getAccount().getHdvEntries(Math.abs(((Integer) perso.getExchangeAction().getValue()))); //R�cup�re un tableau de tout les items que le personnage � en vente dans l'HDV o� il est
        boolean isFirst = true;
        for (MercadilloEntrada curEntry : entries) {
            if (curEntry == null)
                break;
            if (curEntry.buy)
                continue;
            if (!isFirst)
                packet.append("|");
            packet.append(curEntry.parseToEL());
            isFirst = false;
        }
        send(perso, packet.toString());
    }

    public static void GAME_SEND_WEDDING(Mapa c, int action, int homme,
                                         int femme, int parlant) {
        String packet = "GA;" + action + ";" + homme + ";" + homme + ","
                + femme + "," + parlant;
        Jugador Homme = Mundo.mundo.getPlayer(homme);
        send(Homme, packet);

    }

    public static void GAME_SEND_PF(Jugador perso, String str) {
        String packet = "PF" + str;
        send(perso, packet);

    }

    public static void GAME_SEND_MERCHANT_LIST(Jugador P, short mapID) {
        StringBuilder packet = new StringBuilder();
        packet.append("GM").append("|");
        if (Mundo.mundo.getSeller(P.getCurMap().getId()) == null)
            return;
        for (Integer pID : Mundo.mundo.getSeller(P.getCurMap().getId())) {
            if (!Mundo.mundo.getPlayer(pID).isOnline()
                    && Mundo.mundo.getPlayer(pID).isShowSeller()) {
                packet.append("~").append(Mundo.mundo.getPlayer(pID).parseToMerchant()).append("|");
            }
        }
        if (packet.length() < 5)
            return;
        send(P, packet.toString());
    }

    public static void GAME_SEND_PACKET_TO_FIGHT(Pelea fight, int i,
                                                 String packet) {
        for (Peleador f : fight.getFighters(i)) {
            if (f.hasLeft())
                continue;
            if (f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            send(f.getPlayer(), packet);
        }

    }

    public static void GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(Pelea fight, int teams, int state, int cancelBtn, int duel, int spec, long time, int type) {
        StringBuilder packet = new StringBuilder();
        packet.append("GJK").append(state).append("|");
        packet.append(cancelBtn).append("|").append(duel).append("|");
        packet.append(spec).append("|").append(time).append("|").append(type);
        for (Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft())
                continue;
            send(f.getPlayer(), packet.toString());
        }

    }

    public static void GAME_SEND_GJK_PACKET(Jugador out, int state,
                                            int cancelBtn, int duel, int spec, long time, int unknown) {
        send(out, "GJK" + state + "|" + cancelBtn + "|" + duel + "|" + spec + "|" + time + "|" + unknown);

    }

    public static void GAME_SEND_cMK_PACKET_INCARNAM_CHAT(Jugador perso,
                                                          String suffix, int guid, String name, String msg) {
        String packet = "cMK" + suffix + "|" + guid + "|" + name + "|" + msg;
        if (perso.getLevel() > 15 && perso.getGroupe() == null) {
            GAME_SEND_BN(perso);
            return;
        }
        for (Jugador player : Mundo.mundo.getOnlinePlayers())
            if (player.getCurMap() != null && player.getCurMap().getSubArea() != null && player.getCurMap().getSubArea().getArea() != null && player.getCurMap().getSubArea().getArea().getId() == 45)
                send(player, packet);
    }

    public static void GAME_SEND_Ag_PACKET(JuegoCliente out, int idObjet,
                                           String codObjet) {
        String packet = "Ag1|"
                + idObjet
                + "|Cadeau Dofus| Voilà un joli cadeau pour vous ! "
                + "Un jeune aventurier comme vous sera sans servir de la meilleur façon ! "
                + "Bonne continuation avec ceci ! |DOFUS|" + codObjet;
        send(out, packet);

    }

    public static void SEND_Ej_LIVRE(Jugador pj, String str) {
        String packet = "Ej" + str;
        send(pj, packet);

    }

    public static void SEND_EW_METIER_PUBLIC(Jugador pj, String str) {
        String packet = "EW" + str;
        send(pj, packet);

    }

    public static void SEND_EJ_LIVRE(Jugador pj, String str) {
        String packet = "EJ" + str;
        send(pj, packet);

    }

    public static void SEND_GDF_PERSO(Jugador perso, int celda, int frame, int esInteractivo) {
        String packet = "GDF|" + celda + ";" + frame + ";" + esInteractivo;
        send(perso, packet);

    }

    public static void SEND_EMK_MOVE_ITEM(JuegoCliente out, char tipoOG, String signo, String s1) {
        String packet = "EMK" + tipoOG + signo;
        if (!s1.equals(""))
            packet += s1;
        send(out, packet);

    }

    public static void SEND_OR_DELETE_ITEM(JuegoCliente out, int id) {
        String packet = "OR" + id;
        send(out, packet);
    }

    public static void GAME_SEND_CHALLENGE_FIGHT(Pelea fight, int team, String str) {
        StringBuilder packet = new StringBuilder();
        packet.append("Gd").append(str);

        for (Peleador fighter : fight.getFighters(team)) {
            if (fighter.hasLeft())
                continue;
            if (fighter.getPlayer() == null
                    || !fighter.getPlayer().isOnline())
                continue;
            send(fighter.getPlayer(), packet.toString());
        }

    }

    public static void GAME_SEND_CHALLENGE_PERSO(Jugador p, String str) {
        send(p, "Gd" + str);

    }

    public static void GAME_SEND_Im_PACKET_TO_CHALLENGE(Pelea fight, int challenge, String str) {
        StringBuilder packet = new StringBuilder();
        packet.append("Im").append(str);
        for (Peleador fighter : fight.getFighters(challenge)) {
            if (fighter.hasLeft())
                continue;
            if (fighter.getPlayer() == null
                    || !fighter.getPlayer().isOnline())
                continue;
            send(fighter.getPlayer(), packet.toString());
        }

    }

    public static void GAME_SEND_Im_PACKET_TO_CHALLENGE_PERSO(Jugador player,
                                                              String str) {

        send(player, "Im" + str);

    }

    public static void GAME_SEND_MESSAGE_SERVER(Jugador out, String args) {
        String packet = "M1" + args;
        send(out, packet);

    }

    public static void GAME_SEND_WELCOME(Jugador perso) {
        send(perso, "TB");

    }

    public static void GAME_SEND_Eq_PACKET(Jugador Personnage, long Prix) {
        send(Personnage, "Eq1|1|" + Prix);
    }

    public static void GAME_SEND_INFO_HIGHLIGHT_PACKET(Jugador perso, String args) {
        String packet = "IH" + args;
        send(perso, packet);

    }

    public static void GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(final Pelea fight, final int teams) {
        StringBuilder packet = new StringBuilder();
        packet.append("GA").append(";").append("0");
        for (final Peleador f : fight.getFighters(teams)) {
            if (f.hasLeft() || f.getPlayer() == null || !f.getPlayer().isOnline())
                continue;
            send(f.getPlayer(), packet.toString());
        }

    }

    public static void SEND_MESSAGE_DECO(Jugador P, int MSG_ID, String args) {
        String packet = "M0" + MSG_ID + "|" + args;
        send(P, packet);
    }

    public static void SEND_MESSAGE_DECO_ALL(int MSG_ID, String args) {
        String packet = "M0" + MSG_ID + "|" + args;
        for (Jugador perso : Mundo.mundo.getOnlinePlayers())
            send(perso, packet);
    }

    public static void SEND_gA_PERCEPTEUR(Jugador perso, String str) {
        String packet = "gA" + str;
        send(perso, packet);
    }

    public static void SEND_Im1223_ALL(String str) {
        String packet = "Im1223;" + str;
        for (Jugador perso : Mundo.mundo.getOnlinePlayers())
            send(perso, packet);
    }

    public static void GAME_SEND_PERCO_INFOS_PACKET(Jugador perso,
                                                    Recaudador perco, String car) {
        send(perso, "gA" + car + perco.getFullName() + "|" + "-1" + "|" + Mundo.mundo.getMap(perco.getMap()).getX() + "|" + Mundo.mundo.getMap(perco.getMap()).getY());
    }

    public static void SEND_Wp_MENU_Prisme(Jugador perso) {
        String packet = "Wp" + perso.parsePrismesList();
        send(perso.getGameClient(), packet);
    }

    public static void SEND_Ww_CLOSE_Prisme(Jugador out) {
        String packet = "Ww";
        send(out, packet);
    }

    public static void GAME_SEND_am_ALIGN_PACKET_TO_SUBAREA(Jugador p, String str) {
        String packet = "am" + str;
        //Evite les putins de flood en console.
        if (p == null || p.getAccount() == null)
            return;
        send(p, packet);
    }

    public static void SEND_CB_BONUS_CONQUETE(Jugador pj, String str) {
        String packet = "CB" + str;
        send(pj, packet);
    }

    public static void SEND_Cb_BALANCE_CONQUETE(Jugador pj, String str) {
        String packet = "Cb" + str;
        send(pj, packet);
    }

    public static void SEND_GM_PRISME_TO_MAP(JuegoCliente out, Mapa Map) {// envia informacion de todos mercantes en 1 Map
        String packet = Map.getPrismeGMPacket();
        if (packet.equals("") || packet.isEmpty())
            return;
        send(out, packet);
    }

    public static void GAME_SEND_PRISME_TO_MAP(Mapa Map, Prisma Prisme) {
        String packet = Prisme.getGMPrisme();
        for (Jugador z : Map.getPlayers())
            send(z, packet);
    }

    public static void SEND_CP_INFO_DEFENSEURS_PRISME(Jugador perso, String str) {
        String packet = "CP" + str;
        send(perso, packet);
    }

    public static void SEND_Cp_INFO_ATTAQUANT_PRISME(Jugador perso, String str) {
        String packet = "Cp" + str;
        send(perso, packet);
    }

    public static void SEND_CIV_CLOSE_INFO_CONQUETE(Jugador pj) {
        String packet = "CIV";
        send(pj, packet);

    }

    public static void SEND_CW_INFO_WORLD_CONQUETE(Jugador pj, String str) {
        send(pj, "CW" + str);
    }

    public static void SEND_CIJ_INFO_JOIN_PRISME(Jugador pj, String str) {
        String packet = "CIJ" + str;
        send(pj, packet);
    }

    public static void GAME_SEND_aM_ALIGN_PACKET_TO_AREA(Jugador perso,
                                                         String str) {
        String packet = "aM" + str;
        send(perso, packet);
    }

    public static void SEND_GA_ACTION_TO_Map(Mapa Map, String gameActionID, int actionID, String s1, String s2) {
        StringBuilder packet = new StringBuilder();
        packet.append("GA").append(gameActionID).append(";").append(actionID).append(";").append(s1);
        if (!s2.equals(""))
            packet.append(";").append(s2);
        for (Jugador z : Map.getPlayers())
            send(z, packet.toString());
    }

    public static void SEND_CS_SURVIVRE_MESSAGE_PRISME(Jugador perso, String str) {
        String packet = "CS" + str;
        send(perso, packet);
    }

    public static void SEND_CD_MORT_MESSAGE_PRISME(Jugador perso, String str) {
        String packet = "CD" + str;
        send(perso, packet);
    }

    public static void SEND_CA_ATTAQUE_MESSAGE_PRISME(Jugador perso, String str) {
        String packet = "CA" + str;
        send(perso, packet);
    }

    public static void GAME_SEND_ACTION_TO_DOOR(Mapa map, int args, boolean open) {
        String packet = "";
        if (open)
            packet = "GDF|" + args + ";2";
        else
            packet = "GDF|" + args + ";4";
        for (Jugador z : map.getPlayers())
            send(z, packet);
    }

    public static void GAME_SEND_ACTION_TO_DOOR(Jugador p, int args, boolean open) {
        String packet = "";
        if (open)
            packet = "GDF|" + args + ";2";
        else if (!open)
            packet = "GDF|" + args + ";4";

        send(p, packet);
    }

    public static void GAME_UPDATE_CELL(Mapa map, String args) {
        String packet = "GDC" + args;
        for (Jugador z : map.getPlayers())
            send(z, packet);
    }

    public static void GAME_UPDATE_CELL(Jugador p, String args) {
        send(p, "GDC" + args);
    }

    public static void GAME_SEND_ACTION_TO_DOOR_FAST(Jugador perso, int args,
                                                     boolean open) {
        String packet = "";
        if (open)
            packet = "GDF|" + args + ";3";
        else if (!open)
            packet = "GDF|" + args + ";1";
        send(perso, packet);
    }

    public static void GAME_UPDATE_CELL_FAST(Jugador perso, String args) {
        String packet = "GDC" + args;
        send(perso, packet);
    }

    public static void GAME_SEND_ACTION_TO_DOOR_PEUR(Mapa map, boolean open) {
        String packet = "";
        if (open)
            packet = "GDF|294;2|309;2|324;2|339;2|323;2|338;2|353;2|337;2|352;2|367;2|336;2|351;2|366;2|381;2|365;2|380;2|395;2|379;2|394;2|409;2";
        else if (!open)
            packet = "GDF|294;4;0|336;4;0|309;4;0|324;4;0|339;4;0|323;4;0|338;4;0|353;4;0|337;4;0|352;4;0|367;4;0|351;4;0|366;4;0|381;4;0|365;4;0|380;4;0|395;4;0|379;4;0|394;4;0|409;4;0";
        for (Jugador z : map.getPlayers())
            send(z, packet);
    }

    public static void GAME_SEND_ACTION_TO_DOOR_FAST_PEUR(Jugador perso, boolean open) {
        String packet = "";
        if (open)
            packet = "GDF|294;3|309;3|324;3|339;3|323;3|338;3|353;3|337;3|352;3|367;3|336;3|351;3|366;3|381;3|365;3|380;3|395;3|379;3|394;3|409;3";
        else if (!open) {
            packet = "GDF|294;1;0|336;1;0|309;1;0|324;1;0|339;1;0|323;1;0|338;1;0|353;1;0|337;1;0|352;1;0|367;1;0|351;1;0|366;1;0|381;1;0|365;1;0|380;1;0|395;1;0|379;1;0|394;1;0|409;1;0";
        }
        send(perso, packet);
    }

    public static void GAME_SEND_ALE_PACKET(JuegoCliente out, String caract) {
        String packet = "AlE" + caract;
        send(out, packet);
    }

    public static void QuestList(JuegoCliente out, Jugador perso) {
        /*
         * Explication packet : QL + QuestID ; Finish ? 1 : 0 ;
         */
        String packet = "QL" + perso.getQuestGmPacket();
        send(out, packet);
    }

    public static void QuestGep(JuegoCliente out, Mision quest, Jugador perso) {
        /*
         * Explication packet : aQuestId | aObjectifCurrent |
         * aEtapeId,aFinish;aEtapeId,aFinish... | aPreviousObjectif |
         * aNextObjectif | aDialogId | aDialogParams
         */
        // String packet = "QS"+"3|6|289,0;421,0|";//TODO suite ...
        String packet = "QS" + quest.getGmQuestDataPacket(perso);
        //String packet = "QS181|343|745,0|342|344|3646|";
        send(out, packet);
    }

    public static void sendPacketToMap(Mapa map, String packet) {
        for (Jugador perso : map.getPlayers())
            send(perso, packet);
    }

    public static void sendPacketToMapGM(Mapa map, Npc npc) {
        for (Jugador perso : map.getPlayers())
            send(perso, "GM|" + npc.parse(true, perso));
    }
}