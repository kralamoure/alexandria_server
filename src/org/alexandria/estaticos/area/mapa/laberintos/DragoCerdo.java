package org.alexandria.estaticos.area.mapa.laberintos;

import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;
import org.alexandria.otro.utilidad.Temporizador;

import java.util.concurrent.TimeUnit;

public class DragoCerdo {

    private static GameCase inside;
    private static GameCase outside;

    public static void initialize() {
        DragoCerdo.setOutside(null);
        DragoCerdo.setInside(null);
        initializeMap(9371, 413, 274, 262, 36);
        initializeMap(9372, 442, 320, 216, 22);
        initializeMap(9373, 414, 262, 144, 48);
        initializeMap(9374, 417, 262, 231, 51);
        initializeMap(9375, 413, 274, 262, 36); // Huiti�me
        initializeMap(9376, 413, 274, 262, 36);
        initializeMap(9377, 413, 274, 262, 36); // Dix-huiti�me
        initializeMap(9378, 413, 274, 262, 36);
        initializeMap(9379, 413, 274, 262, 36);
        initializeMap(9380, 442, 320, 216, 22);
        initializeMap(9381, 442, 320, 216, 22); // Douzi�me
        initializeMap(9382, 442, 320, 216, 22);
        initializeMap(9383, 442, 320, 216, 22);
        initializeMap(9384, 442, 320, 216, 22);
        initializeMap(9385, 414, 262, 144, 48);
        initializeMap(9386, 414, 262, 144, 48);
        initializeMap(9387, 414, 262, 144, 48); // Quatorzi�me
        initializeMap(9388, 414, 262, 144, 48);
        initializeMap(9389, 414, 262, 144, 48);
        initializeMap(9390, 417, 262, 231, 51);
        initializeMap(9391, 417, 262, 231, 51);
        initializeMap(9392, 417, 262, 231, 51);
        initializeMap(9393, 417, 262, 231, 51);
        initializeMap(9394, 417, 262, 231, 51);
        initializeMap(9395, 417, 262, 231, 51);
        if (DragoCerdo.outside == null) DragoCerdo.initializeExt();
        Temporizador.addSiguiente(DragoCerdo::checkOutside, 5, TimeUnit.MINUTES, Temporizador.DataType.MAPA);
    }

    private static void checkOutside() {
        Mapa actual = Mundo.mundo.getMap((short) 9375);
        if (actual.getCase(returnCell(actual, 413)).isLoS()) {
            Temporizador.addSiguiente(DragoCerdo::checkOutside, 5, TimeUnit.MINUTES, Temporizador.DataType.MAPA);
            return;
        }
        actual = Mundo.mundo.getMap((short) 9377);
        if (actual.getCase(returnCell(actual, 36)).isLoS()) {
            Temporizador.addSiguiente(DragoCerdo::checkOutside, 5, TimeUnit.MINUTES, Temporizador.DataType.MAPA);
            return;
        }
        actual = Mundo.mundo.getMap((short) 9381);
        if (actual.getCase(returnCell(actual, 216)).isLoS()) {
            Temporizador.addSiguiente(DragoCerdo::checkOutside, 5, TimeUnit.MINUTES, Temporizador.DataType.MAPA);
            return;
        }
        actual = Mundo.mundo.getMap((short) 9387);
        if (actual.getCase(returnCell(actual, 262)).isLoS()) {
            Temporizador.addSiguiente(DragoCerdo::checkOutside, 5, TimeUnit.MINUTES, Temporizador.DataType.MAPA);
            return;
        }
        DragoCerdo.initialize();
    }

    public static void close(Mapa map, GameCase cell) {
        if (map == null || cell == null) return;

        switch (cell.getId()) {
            case 320: // Gauche
                close(map, (short) 306);
                break;
            case 262: // Gauche
                switch (map.getId()) {
// haut
                    case 9371, 9374, 9390, 9375, 9391, 9376, 9392, 9377, 9378, 9393, 9379, 9394 -> close(map, (short) 248);
// bas
                    case 9373, 9385, 9386, 9388, 9389 -> close(map, (short) 277);
// bas
                    case 9387 -> {
                        if (isOutside(277))
                            setOutside(null);
                        close(map, (short) 277);
                    }
// haut
                    case 9395 -> {
                        if (isInside(262))
                            setInside(null);
                        close(map, (short) 248);
                    }
                }
                break;
            case 274: // Droite
                close(map, (short) 259);
                break;
            case 231: // Droite
                if (map.getId() == (short) 9395)
                    if (isInside(231))
                        setInside(null);
                close(map, (short) 216);
                break;
            case 216: // Droite
                if (map.getId() == (short) 9381)
                    if (isOutside(201))
                        setOutside(null);
                close(map, (short) 201);
                break;
            case 144: // Droite
                close(map, (short) 158);
                break;
            case 51: // Haut
                if (map.getId() == (short) 9395)
                    if (isInside(51))
                        setInside(null);
                close(map, (short) 65);
                break;
            case 48: // Haut
                close(map, (short) 63);
                break;
            case 36: // Haut
                if (map.getId() == (short) 9377)
                    if (isOutside(36))
                        setOutside(null);
                close(map, (short) 50);
                break;
            case 22: // Haut
                close(map, (short) 37);
                break;
            case 442: // Bas
                close(map, (short) 413);
                close(map, (short) 428);
                break;
            case 417: // Bas
                if (map.getId() == (short) 9395)
                    if (isInside(417))
                        setInside(null);
                close(map, (short) 402);
                break;
            case 414: // Bas
                close(map, (short) 399);
                break;
            case 413: // Bas
                if (map.getId() == (short) 9375)
                    if (isOutside(399))
                        setOutside(null);
                close(map, (short) 399);
                break;
        }
    }

    public static void open(Mapa map, GameCase cell) {
        if (map == null || cell == null) return;
        if (map.getId() == (short) 9395)
            if (inside != null)
                close(map, cell);

        switch (cell.getId()) {
            case 320: // Gauche
                open(map, (short) 306);
                break;
            case 262: // Gauche
                switch (map.getId()) {
                    case 9371: // haut
                    case 9374: // haut
                    case 9390: // haut
                    case 9375: // haut
                    case 9391: // haut
                    case 9376: // haut
                    case 9395: // haut
                    case 9392: // haut
                    case 9377: // haut
                    case 9378: // haut
                    case 9393: // haut
                    case 9379: // haut
                    case 9394: // haut
                        if (map.getId() == (short) 9395)
                            setInside(map.getCase(248));
                        open(map, (short) 248);
                        break;
                    case 9373: // bas
                    case 9385: // bas
                    case 9386: // bas
                    case 9388: // bas
                    case 9389: // bas
                        open(map, (short) 277);
                        break;
                    case 9387:
                        if (outside == null) {
                            setOutside(map.getCase(277));
                            open(map, (short) 277);
                        } else {
                            close(map, (short) 277);
                        }
                        break;
                }
                break;
            case 274: // Droite
                open(map, (short) 259);
                break;
            case 231: // Droite
                if (map.getId() == (short) 9395)
                    setInside(map.getCase(216));
                open(map, (short) 216);
                break;
            case 216: // Droite
                if (map.getId() == (short) 9381) {
                    if (outside == null) {
                        setOutside(map.getCase(201));
                        open(map, (short) 201);
                    } else {
                        close(map, (short) 201);
                    }
                } else
                    open(map, (short) 201);
                break;
            case 144: // Droite
                open(map, (short) 158);
                break;
            case 51: // Haut
                if (map.getId() == (short) 9395)
                    setInside(map.getCase(65));
                open(map, (short) 65);
                break;
            case 48: // Haut
                open(map, (short) 63);
                break;
            case 36: // Haut
                if (map.getId() == (short) 9377) {
                    if (outside == null) {
                        setOutside(map.getCase(50));
                        open(map, (short) 50);
                    } else {
                        close(map, (short) 50);
                    }
                } else
                    open(map, (short) 50);
                break;
            case 22: // Haut
                open(map, (short) 37);
                break;
            case 442: // Bas
                open(map, (short) 413);
                open(map, (short) 428);
                break;
            case 417: // Bas
                if (map.getId() == (short) 9395)
                    setInside(map.getCase(402));
                open(map, (short) 402);
                break;
            case 414: // Bas
                open(map, (short) 399);
                break;
            case 413: // Bas
                if (map.getId() == (short) 9375) {
                    if (outside == null) {
                        setOutside(map.getCase(399));
                        open(map, (short) 399);
                    } else {
                        close(map, (short) 399);
                    }
                } else
                    open(map, (short) 399);
                break;
        }
    }

    public static GameCase getLeftCell(Mapa map) {
        if (map == null)
            return null;
        return switch (map.getId()) {
            case 9372, 9380, 9381, 9382, 9383, 9384 -> map.getCase(320);
            case 9371, 9374, 9373, 9385, 9386, 9390, 9375, 9391, 9376, 9395, 9387, 9388, 9392, 9377, 9378, 9393, 9379, 9389, 9394 -> map.getCase(262);
            default -> null;
        };
    }

    public static GameCase getRightCell(Mapa map) {
        if (map == null)
            return null;
        return switch (map.getId()) {
            case 9371, 9375, 9376, 9378, 9377, 9379 -> map.getCase(274);
            case 9374, 9391, 9390, 9395, 9392, 9393, 9394 -> map.getCase(231);
            case 9372, 9380, 9381, 9382, 9383, 9384 -> map.getCase(216);
            case 9373, 9385, 9386, 9387, 9388, 9389 -> map.getCase(144);
            default -> null;
        };
    }

    public static GameCase getUpCell(Mapa map) {
        if (map == null)
            return null;
        return switch (map.getId()) {
            case 9374, 9391, 9395, 9390, 9392, 9393, 9394 -> map.getCase(51);
            case 9386, 9373, 9385, 9387, 9388, 9389 -> map.getCase(48);
            case 9371, 9375, 9376, 9377, 9378, 9379 -> map.getCase(36);
            case 9372, 9380, 9381, 9382, 9383, 9384 -> map.getCase(22);
            default -> null;
        };
    }

    public static GameCase getDownCell(Mapa map) {
        if (map == null)
            return null;
        return switch (map.getId()) {
            case 9372, 9384, 9380, 9381, 9382, 9383 -> map.getCase(442);
            case 9390, 9393, 9374, 9394, 9391, 9395, 9392 -> map.getCase(417);
            case 9373, 9389, 9385, 9386, 9387, 9388 -> map.getCase(414);
            case 9371, 9375, 9376, 9377, 9378, 9379 -> map.getCase(413);
            default -> null;
        };
    }

    private static void open(Mapa map, short cellId) {
        sendOpen(map, cellId);
        map.removeCase(cellId);
        map.getCases().add(new GameCase(map, cellId, true, true, -1));
    }

    private static void close(final Mapa map, final short cellId) {
        sendClose(map, cellId);
        map.removeCase(cellId);
        map.getCases().add(new GameCase(map, cellId, false, false, -1));
    }

    private static void sendOpen(Mapa map, int cellId) {
        GestorSalida.GAME_UPDATE_CELL(map, cellId + ";aaGaaaaaaa801;1");
        GestorSalida.GAME_SEND_ACTION_TO_DOOR(map, cellId, true);
    }

    private static void sendOpen(Jugador p, int cellId) {
        GestorSalida.GAME_UPDATE_CELL(p, cellId + ";aaGaaaaaaa801;1");
        GestorSalida.GAME_SEND_ACTION_TO_DOOR(p, cellId, true);
    }

    private static void sendClose(Mapa map, int cellId) {
        GestorSalida.GAME_UPDATE_CELL(map, cellId + ";aaaaaaaaaa801;1");
        GestorSalida.GAME_SEND_ACTION_TO_DOOR(map, cellId, false);
    }

    private static void sendClose(Jugador p, int cellId) {
        GestorSalida.GAME_UPDATE_CELL(p, cellId + ";aaaaaaaaaa801;1");
        GestorSalida.GAME_SEND_ACTION_TO_DOOR(p, cellId, false);
    }

    private static void initializeMap(int id, int c1, int c2, int c3, int c4) {
        // Ferme toutes les portes et ouvre une porte dans chaque salle.
        closeMap(id, c1, c2, c3, c4);
        Mapa map = Mundo.mundo.getMap((short) id);
        GameCase cell = randomCase(map, c1, c2, c3, c4);

        switch (id) {
// 13�me
            case 9395 -> DragoCerdo.open(map, cell);
// 8�me
            case 9375 -> {
                if (cell.getId() == 413) {
                    if (DragoCerdo.outside == null)
                        DragoCerdo.setOutside(map.getCase(returnCell(map, 413)));
                    else
                        while (cell.getId() == 413) cell = randomCase(map, c1, c2, c3, c4);
                }
                DragoCerdo.open(map, returnCell(map, cell.getId()));
            }
// 12�me
            case 9381 -> {
                if (cell.getId() == 216) {
                    if (DragoCerdo.outside == null)
                        DragoCerdo.setOutside(map.getCase(returnCell(map, 216)));
                    else
                        while (cell.getId() == 216) cell = randomCase(map, c1, c2, c3, c4);

                }
                DragoCerdo.open(map, returnCell(map, cell.getId()));
            }
// 14�me
            case 9387 -> {
                if (cell.getId() == 262) {
                    if (DragoCerdo.outside == null)
                        DragoCerdo.setOutside(map.getCase(returnCell(map, 262)));
                    else
                        while (cell.getId() == 262) cell = randomCase(map, c1, c2, c3, c4);
                }
                DragoCerdo.open(map, returnCell(map, cell.getId()));
            }
// 18�me
            case 9377 -> {
                if (cell.getId() == 36) {
                    if (DragoCerdo.outside == null)
                        DragoCerdo.setOutside(map.getCase(returnCell(map, 36)));
                    else
                        while (cell.getId() == 36) cell = randomCase(map, c1, c2, c3, c4);
                }
                DragoCerdo.open(map, returnCell(map, cell.getId()));
            }
            default -> DragoCerdo.open(map, cell);
        }
    }

    private static GameCase randomCase(Mapa map, int c1, int c2, int c3, int c4) {
        return switch (Formulas.getRandomValue(0, 3)) {
            case 0 -> map.getCase(c1);
            case 1 -> map.getCase(c2);
            case 2 -> map.getCase(c3);
            case 3 -> map.getCase(c4);
            default -> map.getCase(c1);
        };
    }

    private static void initializeExt() {
        Mapa map;
        GameCase cell;
        switch (Formulas.getRandomValue(0, 3)) {
// 9375 - 8�me
            case 0 -> {
                closeMap(9375, 413, 274, 262, 36);
                map = Mundo.mundo.getMap((short) 9375);
                cell = map.getCase(413);
                open(map, cell);
            }
// 9381 - 12�me
            case 1 -> {
                closeMap(9381, 442, 320, 216, 22);
                map = Mundo.mundo.getMap((short) 9381);
                cell = map.getCase(216);
                open(map, cell);
            }
// 9387 - 14�me
            case 2 -> {
                closeMap(9387, 414, 262, 144, 48);
                map = Mundo.mundo.getMap((short) 9387);
                cell = map.getCase(262);
                open(map, cell);
            }
// 9377 - 18�me
            case 3 -> {
                closeMap(9377, 413, 274, 262, 36);
                map = Mundo.mundo.getMap((short) 9377);
                cell = map.getCase(36);
                open(map, cell);
            }
        }
    }

    private static void closeMap(int id, int c1, int c2, int c3, int c4) {
        Mapa map = Mundo.mundo.getMap((short) id);
        close(map, map.getCase(c1));
        close(map, map.getCase(c2));
        close(map, map.getCase(c3));
        close(map, map.getCase(c4));
    }

    public static void sendPacketMap(Jugador perso) {
        Mapa map = perso.getCurMap();
        GameCase c1 = null, c2 = null, c3 = null, c4 = null;
        switch (map.getId()) {
            case 9371, 9375, 9376, 9377, 9378, 9379 -> {
                c1 = map.getCase(returnCell(map, 413));
                c2 = map.getCase(returnCell(map, 274));
                c3 = map.getCase(returnCell(map, 262));
                c4 = map.getCase(returnCell(map, 36));
            }
            case 9372, 9380, 9381, 9382, 9383, 9384 -> {
                c1 = map.getCase(returnCell(map, 442));
                c2 = map.getCase(returnCell(map, 320));
                c3 = map.getCase(returnCell(map, 216));
                c4 = map.getCase(returnCell(map, 22));
            }
            case 9373, 9385, 9386, 9387, 9388, 9389 -> {
                c1 = map.getCase(returnCell(map, 414));
                c2 = map.getCase(returnCell(map, 262));
                c3 = map.getCase(returnCell(map, 144));
                c4 = map.getCase(returnCell(map, 48));
            }
            case 9374, 9390, 9391, 9392, 9393, 9394, 9395 -> {
                c1 = map.getCase(returnCell(map, 417));
                c2 = map.getCase(returnCell(map, 262));
                c3 = map.getCase(returnCell(map, 231));
                c4 = map.getCase(returnCell(map, 51));
            }
        }

        if(c1 != null) {
            if (c1.isLoS()) sendOpen(perso, c1.getId());
            else sendClose(perso, c1.getId());
        }
        if(c2 != null) {
            if (c2.isLoS()) sendOpen(perso, c2.getId());
            else sendClose(perso, c2.getId());
        }
        if(c3 != null) {
            if (c3.isLoS()) sendOpen(perso, c3.getId());
            else sendClose(perso, c3.getId());
        }
        if(c4 != null) {
            if (c4.isLoS()) sendOpen(perso, c4.getId());
            else sendClose(perso, c4.getId());
        }
    }

    public static short returnCell(Mapa map, int cell) {
        switch (cell) {
            case 320: // Gauche
                return 306;
            case 262: // Gauche
                switch (map.getId()) {
                    case 9371: // haut
                    case 9374: // haut
                    case 9390: // haut
                    case 9375: // haut
                    case 9391: // haut
                    case 9376: // haut
                    case 9395: // haut
                    case 9392: // haut
                    case 9377: // haut
                    case 9378: // haut
                    case 9393: // haut
                    case 9379: // haut
                    case 9394: // haut
                        return 248;
                    case 9373: // bas
                    case 9385: // bas
                    case 9386: // bas
                    case 9388: // bas
                    case 9389: // bas
                    case 9387:
                        return 277;
                }
                break;
            case 274: // Droite
                return 259;
            case 231: // Droite
                return 216;
            case 216: // Droite
                return 201;
            case 144: // Droite
                return 158;
            case 51: // Haut
                return 65;
            case 48: // Haut
                return 63;
            case 36: // Haut
                return 50;
            case 22: // Haut
                return 37;
            case 442: // Bas
                return 428;
            case 417: // Bas
                return 402;
            case 414: // Bas
            case 413: // Bas
                return 399;
        }
        return -1;
    }

    private static void setOutside(GameCase cell) {
        DragoCerdo.outside = cell;
    }

    private static void setInside(GameCase cell) {
        DragoCerdo.inside = cell;
    }

    private static boolean isOutside(int cell) {
        return outside != null && (cell == outside.getId());
    }

    private static boolean isInside(int cell) {
        return inside != null && (cell == outside.getId());
    }
}