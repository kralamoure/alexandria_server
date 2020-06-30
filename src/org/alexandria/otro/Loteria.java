package org.alexandria.otro;

import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.objeto.ObjetoJuego;

public class Loteria {

    public static void startLoterie(Jugador perso, int args) {
        switch (args) {
            case 1:
                if (perso.hasItemTemplate(15001, 1)) {
                    int objIdWin = getCadeau1();
                    //ObjectTemplate objWin = World.world.getObjTemplate(objIdWin);
                    //String objName = objWin.getName();
                    //SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(perso.getCurMap(), "", -5, "Roulette", "F�licitation "+perso.getName()+" ! Tu viens de gagn� : '"+objName+"'.");
                    perso.removeByTemplateID(15001, 1);
                    GestorSalida.GAME_SEND_Im_PACKET(perso, "022;" + 1 + "~"
                            + 15001);
                    GestorSalida.GAME_SEND_Im_PACKET(perso, "021;" + 1 + "~"
                            + objIdWin);
                    ObjetoJuego newObjAdded = Mundo.mundo.getObjetoModelo(objIdWin).createNewItem(1, false);
                    if (!perso.addObjetSimiler(newObjAdded, true, -1)) {
                        Mundo.addGameObject(newObjAdded, true);
                        perso.addObjet(newObjAdded);
                    }
                } else {
                    GestorSalida.GAME_SEND_Im_PACKET(perso, "14|43");
                }
                break;
            case 2:
                if (perso.hasItemTemplate(19072, 1)) {
                    int objIdWin = getCadeau2();
                    //ObjectTemplate objWin = World.world.getObjTemplate(objIdWin);
                    //String objName = objWin.getName();
                    //SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(perso.getCurMap(), "", -5, "Roulette", "F�licitation "+perso.getName()+" ! Tu viens de gagn� : '"+objName+"'.");
                    perso.removeByTemplateID(19072, 1);
                    GestorSalida.GAME_SEND_Im_PACKET(perso, "022;" + 1 + "~"
                            + 19072);
                    GestorSalida.GAME_SEND_Im_PACKET(perso, "021;" + 1 + "~"
                            + objIdWin);
                    ObjetoJuego newObjAdded = Mundo.mundo.getObjetoModelo(objIdWin).createNewItem(1, false);
                    if (!perso.addObjetSimiler(newObjAdded, true, -1)) {
                        Mundo.addGameObject(newObjAdded, true);
                        perso.addObjet(newObjAdded);
                    }
                } else {
                    GestorSalida.GAME_SEND_Im_PACKET(perso, "14|43");
                }
                break;
        }
    }

    public static void startLoteriePioute(Jugador perso) {
        int objIdWin = getCadeauPioute();
        GestorSalida.GAME_SEND_Im_PACKET(perso, "021;" + 1 + "~" + objIdWin);
        ObjetoJuego newObjAdded = Mundo.mundo.getObjetoModelo(objIdWin).createNewItem(1, false);
        if (!perso.addObjetSimiler(newObjAdded, true, -1)) {
            Mundo.addGameObject(newObjAdded, true);
            perso.addObjet(newObjAdded);
        }
    }

    public static int getCadeau1() {
        int Chance = Formulas.getRandomValue(1, 18);
        return switch (Chance) {
            case 1 -> 10338;
            case 2 -> 8899;
            case 3 -> 8903;
            case 4 -> 9339;
            case 5 -> 9348;
            case 6 -> 9500;
            case 7 -> 9583;
            case 8 -> 9889;
            case 9 -> 9893;
            case 10 -> 10150;
            case 11 -> 8817;
            case 12 -> 8912;
            case 13 -> 8983;
            case 14 -> 9353;
            case 15 -> 9354;
            case 16 -> 9356;
            case 17 -> 9358;
            case 18 -> 9184;
            default -> Chance;
        };
    }

    public static int getCadeau2() {
        int Chance = Formulas.getRandomValue(1, 26);
        return switch (Chance) {
            case 1 -> 9643;
            case 2 -> 9642;
            case 3 -> 9641;
            case 4 -> 9640;
            case 5 -> 9639;
            case 6 -> 9638;
            case 7 -> 9637;
            case 8 -> 9636;
            case 9 -> 9635;
            case 10 -> 8955;
            case 11 -> 8954;
            case 12 -> 8953;
            case 13 -> 8952;
            case 14 -> 8951;
            case 15 -> 8950;
            case 16 -> 8949;
            case 17 -> 8948;
            case 18 -> 7804;
            case 19 -> 7803;
            case 20 -> 7802;
            case 21 -> 2333;
            case 22 -> 2332;
            case 23 -> 992;
            case 24 -> 991;
            case 25 -> 990;
            case 26 -> 989;
            default -> Chance;
        };
    }

    public static int getCadeauPioute() {
        int Chance = Formulas.getRandomValue(1, 6);
        return switch (Chance) {
            case 1 -> 7708;
            case 2 -> 7709;
            case 3 -> 7710;
            case 4 -> 7711;
            case 5 -> 7712;
            case 6 -> 7713;
            default -> Chance;
        };
    }

    public static int getCadeauBworker() {
        int Chance = Formulas.getRandomValue(1, 8);
        return switch (Chance) {
            case 1 -> 6799;
            case 2 -> 6804;
            case 3 -> 6805;
            case 4 -> 6807;
            case 5 -> 6811;
            case 6 -> 6812;
            case 7 -> 6813;
            case 8 -> 6904;
            default -> Chance;
        };
    }
}