package org.alexandria.estaticos.oficio;

import org.alexandria.comunes.Formulas;

import java.util.ArrayList;

public class OficioConstantes {

    //Jobs
    public static final int JOB_BUCHERON = 2;
    public static final int JOB_F_EPEE = 11;
    public static final int JOB_S_ARC = 13;
    public static final int JOB_F_MARTEAU = 14;
    public static final int JOB_CORDONIER = 15;
    public static final int JOB_BIJOUTIER = 16;
    public static final int JOB_F_DAGUE = 17;
    public static final int JOB_S_BATON = 18;
    public static final int JOB_S_BAGUETTE = 19;
    public static final int JOB_F_PELLE = 20;
    public static final int JOB_MINEUR = 24;
    public static final int JOB_BOULANGER = 25;
    public static final int JOB_ALCHIMISTE = 26;
    public static final int JOB_TAILLEUR = 27;
    public static final int JOB_PAYSAN = 28;
    public static final int JOB_F_HACHES = 31;
    public static final int JOB_PECHEUR = 36;
    public static final int JOB_CHASSEUR = 41;
    public static final int JOB_FM_DAGUE = 43;
    public static final int JOB_FM_EPEE = 44;
    public static final int JOB_FM_MARTEAU = 45;
    public static final int JOB_FM_PELLE = 46;
    public static final int JOB_FM_HACHES = 47;
    public static final int JOB_SM_ARC = 48;
    public static final int JOB_SM_BAGUETTE = 49;
    public static final int JOB_SM_BATON = 50;
    public static final int JOB_BOUCHER = 56;
    public static final int JOB_POISSONNIER = 58;
    public static final int JOB_F_BOUCLIER = 60;
    public static final int JOB_CORDOMAGE = 62;
    public static final int JOB_JOAILLOMAGE = 63;
    public static final int JOB_COSTUMAGE = 64;
    public static final int JOB_BRICOLEUR = 65;
    //INTERACTIVE OBJET
    public static final int IOBJECT_STATE_FULL = 1;
    public static final int IOBJECT_STATE_EMPTYING = 2;
    public static final int IOBJECT_STATE_EMPTY = 3;
    public static final int IOBJECT_STATE_EMPTY2 = 4;
    public static final int IOBJECT_STATE_FULLING = 5;

    //Action de M�tier {skillID,objetRecolt�,objSp�cial}
    public static final int[][] JOB_ACTION = {
            //Bucheron
            {101}, {6, 303}, {39, 473}, {40, 476}, {10, 460}, {141, 2357}, {139, 2358}, {37, 471}, {154, 7013}, {33, 461}, {41, 474}, {34, 449}, {174, 7925}, {155, 7016}, {38, 472}, {35, 470}, {158, 7014},
            //Mineur
            {48}, {32}, {24, 312}, {25, 441}, {26, 442}, {28, 443}, {56, 445}, {162, 7032}, {55, 444}, {29, 350}, {31, 446}, {30, 313}, {161, 7033},
            //P�cheur
            {133},
            //Rivi�re
            {124, 1782, 1844, 603},  // Petit poissons (riv)
            {125, 1844, 603, 1847, 1794}, // Poisson (mer)
            {126, 603, 1847, 1794, 1779}, // Gros poisson (riv)
            {127, 1847, 1794, 1779, 1801}, // Poisson g�ant (riv)
            //Mer
            {128, 598, 1757, 1750}, // Petit Poissons (mer)
            {129, 1757, 1805, 600}, // Poisson (mer)
            {130, 1805, 1750, 1784, 600}, // Gros poisson (mer)
            {131, 600, 1805, 602, 1784}, // Poisson g�ant (mer)
            //OTHER
            {136, 2187}, {140, 1759}, {140, 1799},
            //Alchi
            {23}, {68, 421}, {69, 428}, {71, 395}, {72, 380}, {73, 593}, {74, 594}, {160, 7059},
            //Paysan
            {122}, {47}, {45, 289}, {53, 400}, {57, 533}, {46, 401}, {50, 423}, {52, 532}, {159, 7018}, {58, 405}, {54, 425},
            //Boulanger
            {109}, {27},
            //Poissonier
            {135},
            //Boucher
            {134},
            //Chasseur
            {132},
            //Tailleur
            {64}, {123}, {63},
            //Bijoutier
            {11}, {12},
            //Cordonnier
            {13}, {14},
            //Forgeur Ep�e
            {145}, {20},
            //Forgeur Marteau
            {144}, {19},
            //Forgeur Dague
            {142}, {18},
            //Forgeur Pelle
            {146}, {21},
            //Forgeur Hache
            {65}, {143},
            //Forgemage de Hache
            {115},
            //Forgemage de dagues
            {1},
            //Forgemage de marteau
            {116},
            //Forgemage d'�p�e
            {113},
            //Forgemage Pelle
            {117},
            //SculpteMage baton
            {120},
            //Sculptemage de baguette
            {119},
            //Sculptemage d'arc
            {118},
            //Costumage
            {165}, {166}, {167},
            //Cordomage
            {163}, {164},
            //Joyaumage
            {169}, {168},
            //Bricoleur
            {171}, {182},
            //Sculpteur de Arc
            {15}, {149},
            //Sculpteur de Baton
            {17}, {147},
            //Sculpteur de Baguette
            {16}, {148},
            //Forgeur de bouclier
            {156},
            //F�e d'artifice
            {151},
            //Etabli moon
            {110},
            //Briseur de ressource
            {121},
            //Etabli a patate
            {22}

    };

    public static final int[][] JOB_PROTECTORS = //{protectorId, itemId}
            {{782, 472}, {684, 289}, {684, 2018}, {685, 400}, {685, 2032}, {686, 533}, {686, 1671}, {687, 401}, {687, 2021}, {688, 423}, {688, 2026}, {689, 532}, {689, 2029}, {690, 7018}, {691, 405}, {692, 425}, {692, 2035}, {693, 312}, {694, 441}, {695, 442}, {696, 443}, {697, 445}, {698, 444}, {699, 7032}, {700, 350}, {701, 446}, {702, 313}, {703, 7033}, {704, 421}, {705, 428}, {706, 395}, {707, 380}, {708, 593}, {709, 594}, {710, 7059}, {711, 303}, {712, 473}, {713, 476}, {714, 460}, {715, 2358}, {716, 2357}, {717, 471}, {718, 461}, {719, 7013}, {720, 7925}, {721, 474}, {722, 449}, {723, 7016}, {724, 470}, {725, 7014}, {726, 1782}, {726, 1790}, {727, 607}, {727, 1844}, {727, 1846}, {728, 603}, {729, 598}, {730, 1757}, {730, 1759}, {731, 1750}, {732, 1847}, {732, 1749}, {733, 1794}, {733, 1796}, {734, 1805}, {734, 1807}, {735, 600}, {735, 1799}, {736, 1779}, {736, 1792}, {737, 1784}, {737, 1788}, {738, 1801}, {738, 1803}, {739, 602}, {739, 1853}};

    public static int getTotalCaseByJobLevel(int lvl) {
        if (lvl < 10) return 2;
        if (lvl == 100) return 9;
        return (lvl / 20) + 3;
    }

    public static int getChanceForMaxCase(int lvl) {
        if(lvl == 100)
            return 99;
        if (lvl < 10)
            return 50;
        return 54 + ((lvl / 10) - 1) * 5;
    }

    public static boolean isJobAction(int a) {
        for (int[] aJOB_ACTION : JOB_ACTION)
            if (aJOB_ACTION[0] == a)
                return true;
        return false;
    }

    public static int getObjectByJobSkill(int skID) {
        for (int[] aJOB_ACTION : JOB_ACTION) {
            if (aJOB_ACTION[0] == skID) {
                if (aJOB_ACTION.length > 2) {
                    return aJOB_ACTION[Formulas.getRandomValue(1, aJOB_ACTION.length - 1)];
                } else if (aJOB_ACTION.length > 1)
                    return aJOB_ACTION[1];
            }
        }
        return -1;
    }

    public static int getChanceByNbrCaseByLvl(int lvl, int nbr) {
        if (nbr <= getTotalCaseByJobLevel(lvl) - 2)
            return 100;//99.999... normalement, mais osef
        return getChanceForMaxCase(lvl);
    }

    public static boolean isMageJob(int id) {
        return (id > 42 && id < 51) || (id > 61 && id < 65);
    }

    public static String actionMetier(int oficio) {
        return switch (oficio) {
            case 62 -> "163;164";
            case 63 -> "169;168";
            case 64 -> "165;166;167";
            case 45 -> "116";
            case 46 -> "117";
            case 67 -> "115";
            case 43 -> "1";
            case 44 -> "113";
            case 48 -> "118";
            case 49 -> "119";
            case 50 -> "120";
            default -> "";
        };
    }

    public static int getProtectorLvl(int lvl) {
        if (lvl < 40)
            return 10;
        if (lvl < 80)
            return 20;
        if (lvl < 120)
            return 30;
        if (lvl < 160)
            return 40;
        if (lvl < 200)
            return 50;
        return 50;
    }

    public static ArrayList<OficioAccion> getPosActionsToJob(int tID, int lvl) {
        ArrayList<OficioAccion> list = new ArrayList<>();
        int timeWin = lvl * 100, dropWin = lvl / 5, bonus = lvl == 100 ? 5 : 0, min = 1 + bonus;

        //Faire Anneau
        //Faire Amullette
        //Faire Sac
        //Faire Cape
        //Faire Chapeau
        //Forger Bouclier
        //Faire clef
        //Faire objet brico
        //Faire botte
        //Faire ceinture
        //Sculter Arc
        //ReSculter Arc
        //Sculter Baton
        //ReSculter Baton
        //Sculter Baguette
        //ReSculter Baguette
        //FM Bottes
        //FM Ceinture
        //FM Anneau
        //FM  Amullette
        //FM Chapeau
        //FM Cape
        //FM Sac
        //Forger Ep�e
        //Reforger Ep�e
        //Forger Dague
        //Reforger Dague
        //Forger Marteau
        //Reforger Marteau
        //Forger Pelle
        //Reforger Pelle
        //Forger Hache
        //Reforger Hache
        //Reforger une hache
        //Reforger une dague
        //Reforger une �p�e
        //Reforger une marteau
        //Reforger une pelle
        //Resculpter un arc
        //Resculpter un baton
        //Resculpter une baguette
        //Pr�parer
        //Pr�parer une Viande
        //Preparer un Poisson
        //Cuir le Pain
        //Faire des Bonbons
        //Miner Fer
        //Fondre
        //Polir
        //P�cher Ombre Etrange
        //P�cher Pichon
        //P�cher Petits poissons de rivi�re
        //P�cher Petits poissons de mer
        //Vider
        //Cueillir Lin
        //Fabriquer une Potion
        //Couper Fr�ne
        //Scie
        //Faucher bl�
        //Moudre
        //Egrener 100% 1 case tout le temps ?
        switch (tID) {
            case JOB_BIJOUTIER -> {
                list.add(new OficioAccion(11, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
                list.add(new OficioAccion(12, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
            }
            case JOB_TAILLEUR -> {
                list.add(new OficioAccion(64, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
                list.add(new OficioAccion(123, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
                list.add(new OficioAccion(63, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
            }
            case JOB_F_BOUCLIER -> list.add(new OficioAccion(156, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
            case JOB_BRICOLEUR -> {
                list.add(new OficioAccion(171, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
                list.add(new OficioAccion(182, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
            }
            case JOB_CORDONIER -> {
                list.add(new OficioAccion(13, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
                list.add(new OficioAccion(14, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
            }
            case JOB_S_ARC -> {
                list.add(new OficioAccion(15, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
                list.add(new OficioAccion(149, 3, 0, true, getChanceForMaxCase(lvl), -1));
            }
            case JOB_S_BATON -> {
                list.add(new OficioAccion(17, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
                list.add(new OficioAccion(147, 3, 0, true, getChanceForMaxCase(lvl), -1));
            }
            case JOB_S_BAGUETTE -> {
                list.add(new OficioAccion(16, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
                list.add(new OficioAccion(148, 3, 0, true, getChanceForMaxCase(lvl), -1));
            }
            case JOB_CORDOMAGE -> {
                list.add(new OficioAccion(163, 3, 0, true, lvl, 0));
                list.add(new OficioAccion(164, 3, 0, true, lvl, 0));
            }
            case JOB_JOAILLOMAGE -> {
                list.add(new OficioAccion(169, 3, 0, true, lvl, 0));
                list.add(new OficioAccion(168, 3, 0, true, lvl, 0));
            }
            case JOB_COSTUMAGE -> {
                list.add(new OficioAccion(165, 3, 0, true, lvl, 0));
                list.add(new OficioAccion(167, 3, 0, true, lvl, 0));
                list.add(new OficioAccion(166, 3, 0, true, lvl, 0));
            }
            case JOB_F_EPEE -> {
                list.add(new OficioAccion(20, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
                list.add(new OficioAccion(145, 3, 0, true, getChanceForMaxCase(lvl), -1));
            }
            case JOB_F_DAGUE -> {
                list.add(new OficioAccion(142, 3, 0, true, getChanceForMaxCase(lvl), -1));
                list.add(new OficioAccion(18, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
            }
            case JOB_F_MARTEAU -> {
                list.add(new OficioAccion(19, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
                list.add(new OficioAccion(144, 3, 0, true, getChanceForMaxCase(lvl), -1));
            }
            case JOB_F_PELLE -> {
                list.add(new OficioAccion(21, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
                list.add(new OficioAccion(146, 3, 0, true, getChanceForMaxCase(lvl), -1));
            }
            case JOB_F_HACHES -> {
                list.add(new OficioAccion(65, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
                list.add(new OficioAccion(143, 3, 0, true, getChanceForMaxCase(lvl), -1));
            }
            case JOB_FM_HACHES -> list.add(new OficioAccion(115, 3, 0, true, lvl, 0));
            case JOB_FM_DAGUE -> list.add(new OficioAccion(1, 3, 0, true, lvl, 0));
            case JOB_FM_EPEE -> list.add(new OficioAccion(113, 3, 0, true, lvl, 0));
            case JOB_FM_MARTEAU -> list.add(new OficioAccion(116, 3, 0, true, lvl, 0));
            case JOB_FM_PELLE -> list.add(new OficioAccion(117, 3, 0, true, lvl, 0));
            case JOB_SM_ARC -> list.add(new OficioAccion(118, 3, 0, true, lvl, 0));
            case JOB_SM_BATON -> list.add(new OficioAccion(120, 3, 0, true, lvl, 0));
            case JOB_SM_BAGUETTE -> list.add(new OficioAccion(119, 3, 0, true, lvl, 0));
            case JOB_CHASSEUR -> list.add(new OficioAccion(132, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
            case JOB_BOUCHER -> list.add(new OficioAccion(134, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
            case JOB_POISSONNIER -> list.add(new OficioAccion(135, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
            case JOB_BOULANGER -> {
                list.add(new OficioAccion(27, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
                list.add(new OficioAccion(109, 3, 0, true, 100, -1));
            }
            case JOB_MINEUR -> {
                if (lvl > 99) {
                    //Miner Dolomite
                    list.add(new OficioAccion(161, min, -18 + dropWin, false, 12000 - timeWin, 60));
                }
                if (lvl > 79) {
                    //Miner Or
                    list.add(new OficioAccion(30, min, -14 + dropWin, false, 12000 - timeWin, 55));
                }
                if (lvl > 69) {
                    //Miner Bauxite
                    list.add(new OficioAccion(31, min, -12 + dropWin, false, 12000 - timeWin, 50));
                }
                if (lvl > 59) {
                    //Miner Argent
                    list.add(new OficioAccion(29, min, -10 + dropWin, false, 12000 - timeWin, 40));
                }
                if (lvl > 49) {
                    //Miner Etain
                    list.add(new OficioAccion(55, min, -8 + dropWin, false, 12000 - timeWin, 35));
                    //Miner Silicate
                    list.add(new OficioAccion(162, min, -8 + dropWin, false, 12000 - timeWin, 35));
                }
                if (lvl > 39) {
                    //Miner Mangan�se
                    list.add(new OficioAccion(56, min, -6 + dropWin, false, 12000 - timeWin, 30));
                }
                if (lvl > 29) {
                    //Miner Kobalte
                    list.add(new OficioAccion(28, min, -4 + dropWin, false, 12000 - timeWin, 25));
                }
                if (lvl > 19) {
                    //Miner Bronze
                    list.add(new OficioAccion(26, min, -2 + dropWin, false, 12000 - timeWin, 20));
                }
                if (lvl > 9) {
                    //Miner Cuivre
                    list.add(new OficioAccion(25, min, dropWin, false, 12000 - timeWin, 15));
                }
                list.add(new OficioAccion(24, min, 2 + dropWin, false, 12000 - timeWin, 10));
                list.add(new OficioAccion(32, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
                list.add(new OficioAccion(48, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
            }
            case JOB_PECHEUR -> {
                if (lvl > 74) {
                    //P�cher Poissons g�ants de mer
                    list.add(new OficioAccion(131, 0, 1, false, 12000 - timeWin, 35));
                }
                if (lvl > 69) {
                    //P�cher Poissons g�ants de rivi�re
                    list.add(new OficioAccion(127, 0, 1, false, 12000 - timeWin, 35));
                }
                if (lvl > 49) {
                    //P�cher Gros poissons de mers
                    list.add(new OficioAccion(130, 0, 1, false, 12000 - timeWin, 30));
                }
                if (lvl > 39) {
                    //P�cher Gros poissons de rivi�re
                    list.add(new OficioAccion(126, 0, 1, false, 12000 - timeWin, 25));
                }
                if (lvl > 19) {
                    //P�cher Poissons de mer
                    list.add(new OficioAccion(129, 0, 1, false, 12000 - timeWin, 20));
                }
                if (lvl > 9) {
                    //P�cher Poissons de rivi�re
                    list.add(new OficioAccion(125, 0, 1, false, 12000 - timeWin, 15));
                }
                list.add(new OficioAccion(140, 0, 1, false, 12000 - timeWin, 50));
                list.add(new OficioAccion(136, 1, 1, false, 12000 - timeWin, 5));
                list.add(new OficioAccion(124, 0, 1, false, 12000 - timeWin, 10));
                list.add(new OficioAccion(128, 0, 1, false, 12000 - timeWin, 10));
                list.add(new OficioAccion(133, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
            }
            case JOB_ALCHIMISTE -> {
                if (lvl > 49) {
                    //Cueillir Graine de Pandouille
                    list.add(new OficioAccion(160, min, -8 + dropWin, false, 12000 - timeWin, 35));
                    //Cueillir Edelweiss
                    list.add(new OficioAccion(74, min, -8 + dropWin, false, 12000 - timeWin, 35));
                }
                if (lvl > 39) {
                    //Cueillir Orchid�e
                    list.add(new OficioAccion(73, min, -6 + dropWin, false, 12000 - timeWin, 30));
                }
                if (lvl > 29) {
                    //Cueillir Menthe
                    list.add(new OficioAccion(72, min, -4 + dropWin, false, 12000 - timeWin, 25));
                }
                if (lvl > 19) {
                    //Cueillir Tr�fle
                    list.add(new OficioAccion(71, min, -2 + dropWin, false, 12000 - timeWin, 20));
                }
                if (lvl > 9) {
                    //Cueillir Chanvre
                    list.add(new OficioAccion(69, min, dropWin, false, 12000 - timeWin, 15));
                }
                list.add(new OficioAccion(68, min, 2 + dropWin, false, 12000 - timeWin, 10));
                list.add(new OficioAccion(23, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
            }
            case JOB_BUCHERON -> {
                if (lvl > 99) {
                    //Couper Bambou Sacr�
                    list.add(new OficioAccion(158, min, -18 + dropWin, false, 12000 - timeWin, 75));
                }
                if (lvl > 89) {
                    //Couper Orme
                    list.add(new OficioAccion(35, min, -16 + dropWin, false, 12000 - timeWin, 70));
                }
                if (lvl > 79) {
                    //Couper Charme
                    list.add(new OficioAccion(38, min, -14 + dropWin, false, 12000 - timeWin, 65));
                    //Couper Bambou Sombre
                    list.add(new OficioAccion(155, min, -14 + dropWin, false, 12000 - timeWin, 65));
                }
                if (lvl > 74) {
                    //Couper Kalyptus
                    list.add(new OficioAccion(174, min, -13 + dropWin, false, 12000 - timeWin, 55));
                }
                if (lvl > 69) {
                    //Couper Eb�ne
                    list.add(new OficioAccion(34, min, -12 + dropWin, false, 12000 - timeWin, 50));
                }
                if (lvl > 59) {
                    //Couper Merisier
                    list.add(new OficioAccion(41, min, -10 + dropWin, false, 12000 - timeWin, 45));
                }
                if (lvl > 49) {
                    //Couper If
                    list.add(new OficioAccion(33, min, -8 + dropWin, false, 12000 - timeWin, 40));
                    //Couper Bambou
                    list.add(new OficioAccion(154, min, -8 + dropWin, false, 12000 - timeWin, 40));
                }
                if (lvl > 39) {
                    //Couper Erable
                    list.add(new OficioAccion(37, min, -6 + dropWin, false, 12000 - timeWin, 35));
                }
                if (lvl > 34) {
                    //Couper Bombu
                    list.add(new OficioAccion(139, min, -5 + dropWin, false, 12000 - timeWin, 30));
                    //Couper Oliviolet
                    list.add(new OficioAccion(141, min, -5 + dropWin, false, 12000 - timeWin, 30));
                }
                if (lvl > 29) {
                    //Couper Ch�ne
                    list.add(new OficioAccion(10, min, -4 + dropWin, false, 12000 - timeWin, 25));
                }
                if (lvl > 19) {
                    //Couper Noyer
                    list.add(new OficioAccion(40, min, -2 + dropWin, false, 12000 - timeWin, 20));
                }
                if (lvl > 9) {
                    //Couper Ch�taignier
                    list.add(new OficioAccion(39, min, dropWin, false, 12000 - timeWin, 15));
                }
                list.add(new OficioAccion(6, min, 2 + dropWin, false, 12000 - timeWin, 10));
                list.add(new OficioAccion(101, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
            }
            case JOB_PAYSAN -> {
                if (lvl > 69) {
                    //Faucher Chanvre
                    list.add(new OficioAccion(54, min, -12 + dropWin, false, 12000 - timeWin, 45));
                }
                if (lvl > 59) {
                    //Faucher Malt
                    list.add(new OficioAccion(58, min, -10 + dropWin, false, 12000 - timeWin, 40));
                }
                if (lvl > 49) {
                    //Faucher Riz
                    list.add(new OficioAccion(159, min, -8 + dropWin, false, 12000 - timeWin, 35));
                    //Faucher Seigle
                    list.add(new OficioAccion(52, min, -8 + dropWin, false, 12000 - timeWin, 35));
                }
                if (lvl > 39) {
                    //Faucher Lin
                    list.add(new OficioAccion(50, min, -6 + dropWin, false, 12000 - timeWin, 30));
                }
                if (lvl > 29) {
                    //Faucher Houblon
                    list.add(new OficioAccion(46, min, -4 + dropWin, false, 12000 - timeWin, 25));
                }
                if (lvl > 19) {
                    //Faucher Avoine
                    list.add(new OficioAccion(57, min, -2 + dropWin, false, 12000 - timeWin, 20));
                }
                if (lvl > 9) {
                    //Faucher Orge
                    list.add(new OficioAccion(53, min, dropWin, false, 12000 - timeWin, 15));
                }
                list.add(new OficioAccion(45, min, 2 + dropWin, false, 12000 - timeWin, 10));
                list.add(new OficioAccion(47, getTotalCaseByJobLevel(lvl), 0, true, getChanceForMaxCase(lvl), -1));
                list.add(new OficioAccion(122, 1, 0, true, 100, 10));
            }
        }
        return list;
    }

    public static int getDistCanne(int temp) {
        return switch (temp) {
//1 to 2
            case 8541, 6661, 596 -> 2;
//1 to 3
            case 1866 -> 3;
//1 to 4
            case 1865, 1864 -> 4;
//1 to 5
            case 1867, 2188 -> 5;
//1 to 6
            case 1863, 1862 -> 6;
//1 to 7
            case 1868 -> 7;
//1 to 8
            case 1861, 1860 -> 8;
//1 to 9
            case 2366 -> 9;
            default -> 0;
        };
    }

    public static int getPoissonRare(int tID) {
        return switch (tID) {
// Greu
            case 598 -> 1786;
// Krala
            case 600 -> 1799;
// Requin
            case 602 -> 1853;
// Poisson Chaton
            case 603 -> 1762;
// Poisson Pan�
            case 1750 -> 1754;
// Crabe Sourimi
            case 1757 -> 1759;
// Bar
            case 1779 -> 1779;
// Goujon
            case 1785 -> 1790;
// Raie
            case 1784 -> 1788;
// Carpe
            case 1794 -> 1796;
// Perche
            case 1801 -> 1803;
// Sardine
            case 1805 -> 1807;
// Truite
            case 1844 -> 1846;
// Brochet
            case 1847 -> 1849;
            default -> -1;
        };
    }
}