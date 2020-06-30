package org.alexandria.otro;

import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Constantes;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.estaticos.juego.accion.AccionIntercambiar;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.objeto.ObjetoModelo;
import org.alexandria.otro.utilidad.Doble;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Dopeul {

    private static final Map<Integer, Doble<Integer, Integer>> donjons = new HashMap<>();

    public static Map<Integer, Doble<Integer, Integer>> getDonjons() {
        return donjons;
    }

    public static void getReward(Jugador player, int type) {
        Mapa curMap = player.getCurMap();
        int idMap = Mundo.mundo.getTempleByClasse(player.getClasse());
        switch (type) {
            case 1://Sort sp�cial
                if (!player.hasItemTemplate(getDoplonByClasse(player.getClasse()), 1)) { // Si on a pas le doplon de classe
                    GestorSalida.GAME_SEND_Im_PACKET(player, "14");
                    return;
                } else if (curMap.getId() != (short) idMap) // Si on est pas dans le temple de notre classe
                {
                    GestorSalida.GAME_SEND_MESSAGE(player, "Tu n'es pas dans ton temple de classe !");
                    return;
                } else if (player.hasSpell(Constantes.getHechizosEspecialesClase(player.getClasse()))) // Si on a d�j� le sort
                {
                    GestorSalida.GAME_SEND_MESSAGE(player, "Tu as déjà appris le sort !");
                    return;
                }

                player.learnSpell(Constantes.getHechizosEspecialesClase(player.getClasse()), 1, true, true, true);
                removeObject(player, getDoplonByClasse(player.getClasse()), 1);
                break;

            case 2://Trousseau de cl�s
                if (player.hasItemTemplate(10207, 1)) {
                    GestorSalida.GAME_SEND_MESSAGE(player, "Tu possède déjà un Trousseau de clef !");
                    return;
                }
                int doplon = hasOneDoplon(player);
                if (doplon == -1) {
                    GestorSalida.GAME_SEND_Im_PACKET(player, "14");
                    return;
                }
                ObjetoJuego obj = Mundo.mundo.getObjetoModelo(10207).createNewItem(1, true);
                if (player.addObjet(obj, false))
                    Mundo.addGameObject(obj, true);
                removeObject(player, doplon, 1);
                break;

            case 3://Reset spell
                ArrayList<Integer> doplons = hasQuaDoplon(player, 7);

                if(doplons.contains(Dopeul.getDoplonByClasse(player.getClasse()))) {
                    removeObject(player, Dopeul.getDoplonByClasse(player.getClasse()), 7);
                } else {
                    doplons = Dopeul.hasQuaDoplon(player, 1);
                    if(doplons.size() == 12) {
                        for (int id : doplons) removeObject(player, id, 1);
                    } else {
                        GestorSalida.GAME_SEND_Im_PACKET(player, "14");
                        return;
                    }
                }

                player.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.FORGETTING_SPELL, 0));
                GestorSalida.GAME_SEND_FORGETSPELL_INTERFACE('+', player);
                break;

            case 4://Reset caract�ristiques
                if (!player.hasItemTemplate(getDoplonByClasse(player.getClasse()), 1)) {
                    GestorSalida.GAME_SEND_Im_PACKET(player, "14");
                    return;
                } else if (curMap.getId() != (short) idMap) // Si on est pas dans le temple de notre classe
                {
                    GestorSalida.GAME_SEND_MESSAGE(player, "Tu n'es pas dans ton temple de classe !");
                    return;
                } else if (player.hasItemTemplate(10601, 1)) {
                    GestorSalida.GAME_SEND_MESSAGE(player, "Tu ne peux pas te reconstituer plusieurs fois !");
                    return;
                }
                player.getCaracteristicas().addOneStat(125, -player.getCaracteristicas().getEffect(125));
                player.getCaracteristicas().addOneStat(124, -player.getCaracteristicas().getEffect(124));
                player.getCaracteristicas().addOneStat(118, -player.getCaracteristicas().getEffect(118));
                player.getCaracteristicas().addOneStat(123, -player.getCaracteristicas().getEffect(123));
                player.getCaracteristicas().addOneStat(119, -player.getCaracteristicas().getEffect(119));
                player.getCaracteristicas().addOneStat(126, -player.getCaracteristicas().getEffect(126));
                player.addCapital((player.getLevel() - 1) * 5
                        - player.get_capital());

                ObjetoModelo OT = Mundo.mundo.getObjetoModelo(10601); // On lui donne un certificat de restat
                ObjetoJuego obj2 = OT.createNewItem(1, false);
                if (player.addObjet(obj2, true)) //Si le joueur n'avait pas d'item similaire
                    Mundo.addGameObject(obj2, true);
                obj2.refreshStatsObjet("325" + Instant.now().toEpochMilli());
                GestorSalida.GAME_SEND_STATS_PACKET(player);
                removeObject(player, getDoplonByClasse(player.getClasse()), 1);
                break;

            case 5://Guildalogemme
                doplons = hasQuaDoplon(player, 1);
                if (doplons == null) {
                    GestorSalida.GAME_SEND_Im_PACKET(player, "14");
                    return;
                }
                obj = Mundo.mundo.getObjetoModelo(1575).createNewItem(1, true);
                if (player.addObjet(obj, false))
                    Mundo.addGameObject(obj, true);
                for (int id : doplons)
                    removeObject(player, id, 1);
                break;

            case 6://Parchemin de caract�ristique
                GestorSalida.GAME_SEND_MESSAGE(player, "Prochainement..");
                break;
        }
        GestorSalida.GAME_SEND_Ow_PACKET(player);
        Database.dinamicos.getPlayerData().update(player);
    }

    public static Integer getDoplonByClasse(int classe) {
        return switch (classe) {
            case Constantes.CLASE_FECA -> 10306;
            case Constantes.CLASE_OSAMODAS -> 10308;
            case Constantes.CLASE_ANUTROF -> 10305;
            case Constantes.CLASE_SRAM -> 10312;
            case Constantes.CLASE_XELOR -> 10313;
            case Constantes.CLASE_ZURCARAK -> 10303;
            case Constantes.CLASE_ANIRIPSA -> 10304;
            case Constantes.CLASE_YOPUKA -> 10307;
            case Constantes.CLASE_OCRA -> 10302;
            case Constantes.CLASE_SADIDA -> 10311;
            case Constantes.CLASE_SACROGRITO -> 10310;
            case Constantes.CLASE_PANDAWA -> 10309;
            default -> -1;
        };
    }

    public static int hasOneDoplon(Jugador perso) {
        if (perso.hasItemTemplate(10306, 1))
            return 10306;
        else if (perso.hasItemTemplate(10308, 1))
            return 10308;
        else if (perso.hasItemTemplate(10305, 1))
            return 10305;
        else if (perso.hasItemTemplate(10312, 1))
            return 10312;
        else if (perso.hasItemTemplate(10313, 1))
            return 10313;
        else if (perso.hasItemTemplate(10303, 1))
            return 10303;
        else if (perso.hasItemTemplate(10304, 1))
            return 10304;
        else if (perso.hasItemTemplate(10307, 1))
            return 10307;
        else if (perso.hasItemTemplate(10302, 1))
            return 10302;
        else if (perso.hasItemTemplate(10311, 1))
            return 10311;
        else if (perso.hasItemTemplate(10310, 1))
            return 10310;
        else if (perso.hasItemTemplate(10309, 1))
            return 10309;
        else
            return -1;
    }

    private static ArrayList<Integer> hasQuaDoplon(Jugador perso, int qua) {
        ArrayList<Integer> doplons = new ArrayList<>();

        if (perso.hasItemTemplate(10306, qua))
            doplons.add(10306);
        if (perso.hasItemTemplate(10308, qua))
            doplons.add(10308);
        if (perso.hasItemTemplate(10305, qua))
            doplons.add(10305);
        if (perso.hasItemTemplate(10312, qua))
            doplons.add(10312);
        if (perso.hasItemTemplate(10313, qua))
            doplons.add(10313);
        if (perso.hasItemTemplate(10303, qua))
            doplons.add(10303);
        if (perso.hasItemTemplate(10304, qua))
            doplons.add(10304);
        if (perso.hasItemTemplate(10307, qua))
            doplons.add(10307);
        if (perso.hasItemTemplate(10302, qua))
            doplons.add(10302);
        if (perso.hasItemTemplate(10311, qua))
            doplons.add(10311);
        if (perso.hasItemTemplate(10310, qua))
            doplons.add(10310);
        if (perso.hasItemTemplate(10309, qua))
            doplons.add(10309);
        return doplons;
    }

    private static void removeObject(Jugador perso, int id, int qua) {
        perso.removeByTemplateID(id, qua);
        GestorSalida.GAME_SEND_Ow_PACKET(perso);
        GestorSalida.GAME_SEND_Im_PACKET(perso, "022;" + qua + "~" + id);
    }

    public static boolean parseConditionTrousseau(String stats, int npc, int map) {
        Doble<Integer, Integer> couple = donjons.get(map);

        if (couple != null)
            return couple.getPrimero() == npc && Integer.toHexString(couple.getSegundo()).startsWith(stats);
        return false;
    }

    public static String generateStats() {
        StringBuilder stats = new StringBuilder();

        for (Doble<Integer, Integer> couple : donjons.values()) {
            if (!stats.toString().isEmpty())
                stats.append(",");
            stats.append(Integer.toHexString(couple.getSegundo()));
        }
        return stats.toString();
    }

    public static Map<Integer, String> generateStatsTrousseau() {
        Map<Integer, String> txtStat = new HashMap<>();
        txtStat.put(Constantes.STATS_NAME_DJ, generateStats());
        txtStat.put(Constantes.STATS_DATE, String.valueOf(Instant.now().toEpochMilli()));
        return txtStat;
    }
}