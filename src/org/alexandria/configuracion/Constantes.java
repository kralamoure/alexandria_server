package org.alexandria.configuracion;

import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.estaticos.cliente.Jugador.Caracteristicas;
import org.alexandria.estaticos.Montura;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.objeto.ObjetoModelo;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.otro.utilidad.CaracteristicasRandom;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Constantes {
    //Limite de mapas
    public static final int LIMITE_DE_MAPAS = 30000;

    //Tiempo de turno (milisegundos)
    public static final int TIEMPO_INICIO_PELEA = 45000;
    public static final int TIEMPO_DE_TURNO = 30000;

    //Coordenadas de estatuas Fenix
    public static final String TODOS_LOS_FENIX = "-11;-54|2;-12|-41;-17|5;-9|25;-4|36;5|12;12|10;19|-10;13|-14;31|-43;0|-60;-3|-58;18|24;-43|27;-33";

    //Estados
    public static final int ESTADO_NEUTRAL = 0;
    public static final int ETAT_SAOUL = 1;
    public static final int ETAT_CAPT_AME = 2;
    public static final int ETAT_PORTEUR = 3;
    public static final int ETAT_PEUREUX = 4;
    public static final int ETAT_DESORIENTE = 5;
    public static final int ETAT_ENRACINE = 6;
    public static final int ETAT_PESANTEUR = 7;
    public static final int ETAT_PORTE = 8;
    public static final int ETAT_MOTIV_SYLVESTRE = 9;
    public static final int ETAT_APPRIVOISEMENT = 10;
    public static final int ETAT_CHEVAUCHANT = 11;
    public static final int FIGHT_TYPE_CHALLENGE = 0;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //D�fies
    public static final int FIGHT_TYPE_AGRESSION = 1;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Aggros
    public static final int FIGHT_TYPE_CONQUETE = 2;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Conquete
    public static final int FIGHT_TYPE_DOPEUL = 3;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Dopeuls de temple
    public static final int FIGHT_TYPE_PVM = 4;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //PvM
    public static final int FIGHT_TYPE_PVT = 5;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Percepteur
    public static final int FIGHT_STATE_INIT = 1;
    public static final int FIGHT_STATE_PLACE = 2;
    public static final int FIGHT_STATE_ACTIVE = 3;
    public static final int STATE_SOBER = 20;
    public static final int ESTADO_FIN_DE_PELEA = 4;
    //Items
    //Positions
    public static final int ITEM_POS_NO_EQUIPED = -1;
    public static final int ITEM_POS_AMULETTE = 0;
    public static final int ITEM_POS_ARME = 1;
    public static final int ITEM_POS_ANNEAU1 = 2;
    public static final int ITEM_POS_CEINTURE = 3;
    public static final int ITEM_POS_ANNEAU2 = 4;
    public static final int ITEM_POS_BOTTES = 5;
    public static final int ITEM_POS_COIFFE = 6;
    public static final int ITEM_POS_CAPE = 7;
    public static final int ITEM_POS_FAMILIER = 8;
    public static final int ITEM_POS_DOFUS1 = 9;
    public static final int ITEM_POS_DOFUS2 = 10;
    public static final int ITEM_POS_DOFUS3 = 11;
    public static final int ITEM_POS_DOFUS4 = 12;
    public static final int ITEM_POS_DOFUS5 = 13;
    public static final int ITEM_POS_DOFUS6 = 14;
    public static final int ITEM_POS_BOUCLIER = 15;
    public static final int ITEM_POS_DRAGODINDE = 16;
    //Objets dons, mutations, mal�diction, ..
    public static final int ITEM_POS_MUTATION = 20;
    public static final int ITEM_POS_ROLEPLAY_BUFF = 21;
    public static final int ITEM_POS_PNJ_SUIVEUR = 24;
    public static final int ITEM_POS_BENEDICTION = 23;
    public static final int ITEM_POS_MALEDICTION = 22;
    public static final int ITEM_POS_BONBON = 25;
    //Types
    public static final int ITEM_TYPE_AMULETTE = 1;
    public static final int ITEM_TYPE_ARC = 2;
    public static final int ITEM_TYPE_BAGUETTE = 3;
    public static final int ITEM_TYPE_BATON = 4;
    public static final int ITEM_TYPE_DAGUES = 5;
    public static final int ITEM_TYPE_EPEE = 6;
    public static final int ITEM_TYPE_MARTEAU = 7;
    public static final int ITEM_TYPE_PELLE = 8;
    public static final int ITEM_TYPE_ANNEAU = 9;
    public static final int ITEM_TYPE_CEINTURE = 10;
    public static final int ITEM_TYPE_BOTTES = 11;
    public static final int ITEM_TYPE_POTION = 12;
    public static final int ITEM_TYPE_PARCHO_EXP = 13;
    public static final int ITEM_TYPE_DONS = 14;
    public static final int ITEM_TYPE_RESSOURCE = 15;
    public static final int ITEM_TYPE_COIFFE = 16;
    public static final int ITEM_TYPE_CAPE = 17;
    public static final int ITEM_TYPE_FAMILIER = 18;
    public static final int ITEM_TYPE_HACHE = 19;
    public static final int ITEM_TYPE_OUTIL = 20;
    public static final int ITEM_TYPE_PIOCHE = 21;
    public static final int ITEM_TYPE_FAUX = 22;
    public static final int ITEM_TYPE_DOFUS = 23;
    public static final int ITEM_TYPE_QUETES = 24;
    public static final int ITEM_TYPE_DOCUMENT = 25;
    public static final int ITEM_TYPE_FM_POTION = 26;
    public static final int ITEM_TYPE_TRANSFORM = 27;
    public static final int ITEM_TYPE_BOOST_FOOD = 28;
    public static final int ITEM_TYPE_BENEDICTION = 29;
    public static final int ITEM_TYPE_MALEDICTION = 30;
    public static final int ITEM_TYPE_RP_BUFF = 31;
    public static final int ITEM_TYPE_PERSO_SUIVEUR = 32;
    public static final int ITEM_TYPE_PAIN = 33;
    public static final int ITEM_TYPE_CEREALE = 34;
    public static final int ITEM_TYPE_FLEUR = 35;
    public static final int ITEM_TYPE_PLANTE = 36;
    public static final int ITEM_TYPE_BIERE = 37;
    public static final int ITEM_TYPE_BOIS = 38;
    public static final int ITEM_TYPE_MINERAIS = 39;
    public static final int ITEM_TYPE_ALLIAGE = 40;
    public static final int ITEM_TYPE_POISSON = 41;
    public static final int ITEM_TYPE_BONBON = 42;
    public static final int ITEM_TYPE_POTION_OUBLIE = 43;
    public static final int ITEM_TYPE_POTION_METIER = 44;
    public static final int ITEM_TYPE_POTION_SORT = 45;
    public static final int ITEM_TYPE_FRUIT = 46;
    public static final int ITEM_TYPE_OS = 47;
    public static final int ITEM_TYPE_POUDRE = 48;
    public static final int ITEM_TYPE_COMESTI_POISSON = 49;
    public static final int ITEM_TYPE_PIERRE_PRECIEUSE = 50;
    public static final int ITEM_TYPE_PIERRE_BRUTE = 51;
    public static final int ITEM_TYPE_FARINE = 52;
    public static final int ITEM_TYPE_PLUME = 53;
    public static final int ITEM_TYPE_POIL = 54;
    public static final int ITEM_TYPE_ETOFFE = 55;
    public static final int ITEM_TYPE_CUIR = 56;
    public static final int ITEM_TYPE_LAINE = 57;
    public static final int ITEM_TYPE_GRAINE = 58;
    public static final int ITEM_TYPE_PEAU = 59;
    public static final int ITEM_TYPE_HUILE = 60;
    public static final int ITEM_TYPE_PELUCHE = 61;
    public static final int ITEM_TYPE_POISSON_VIDE = 62;
    public static final int ITEM_TYPE_VIANDE = 63;
    public static final int ITEM_TYPE_VIANDE_CONSERVEE = 64;
    public static final int ITEM_TYPE_QUEUE = 65;
    public static final int ITEM_TYPE_METARIA = 66;
    public static final int ITEM_TYPE_LEGUME = 68;
    public static final int ITEM_TYPE_VIANDE_COMESTIBLE = 69;
    public static final int ITEM_TYPE_TEINTURE = 70;
    public static final int ITEM_TYPE_EQUIP_ALCHIMIE = 71;
    public static final int ITEM_TYPE_OEUF_FAMILIER = 72;
    public static final int ITEM_TYPE_MAITRISE = 73;
    public static final int ITEM_TYPE_FEE_ARTIFICE = 74;
    public static final int ITEM_TYPE_PARCHEMIN_SORT = 75;
    public static final int ITEM_TYPE_PARCHEMIN_CARAC = 76;
    public static final int ITEM_TYPE_CERTIFICAT_CHANIL = 77;
    public static final int ITEM_TYPE_RUNE_FORGEMAGIE = 78;
    public static final int ITEM_TYPE_BOISSON = 79;
    public static final int ITEM_TYPE_OBJET_MISSION = 80;
    public static final int ITEM_TYPE_SAC_DOS = 81;
    public static final int ITEM_TYPE_BOUCLIER = 82;
    public static final int ITEM_TYPE_PIERRE_AME = 83;
    public static final int ITEM_TYPE_CLEFS = 84;
    public static final int ITEM_TYPE_PIERRE_AME_PLEINE = 85;
    public static final int ITEM_TYPE_POPO_OUBLI_PERCEP = 86;
    public static final int ITEM_TYPE_PARCHO_RECHERCHE = 87;
    public static final int ITEM_TYPE_PIERRE_MAGIQUE = 88;
    public static final int ITEM_TYPE_CADEAUX = 89;
    public static final int ITEM_TYPE_FANTOME_FAMILIER = 90;
    public static final int ITEM_TYPE_DRAGODINDE = 91;
    public static final int ITEM_TYPE_BOUFTOU = 92;
    public static final int ITEM_TYPE_OBJET_ELEVAGE = 93;
    public static final int ITEM_TYPE_OBJET_UTILISABLE = 94;
    public static final int ITEM_TYPE_PLANCHE = 95;
    public static final int ITEM_TYPE_ECORCE = 96;
    public static final int ITEM_TYPE_CERTIF_MONTURE = 97;
    public static final int ITEM_TYPE_RACINE = 98;
    public static final int ITEM_TYPE_FILET_CAPTURE = 99;
    public static final int ITEM_TYPE_SAC_RESSOURCE = 100;
    public static final int ITEM_TYPE_ARBALETE = 102;
    public static final int ITEM_TYPE_PATTE = 103;
    public static final int ITEM_TYPE_AILE = 104;
    public static final int ITEM_TYPE_OEUF = 105;
    public static final int ITEM_TYPE_OREILLE = 106;
    public static final int ITEM_TYPE_CARAPACE = 107;
    public static final int ITEM_TYPE_BOURGEON = 108;
    public static final int ITEM_TYPE_OEIL = 109;
    public static final int ITEM_TYPE_GELEE = 110;
    public static final int ITEM_TYPE_COQUILLE = 111;
    public static final int ITEM_TYPE_PRISME = 112;
    public static final int ITEM_TYPE_OBJET_VIVANT = 113;
    public static final int ITEM_TYPE_ARME_MAGIQUE = 114;
    public static final int ITEM_TYPE_FRAGM_AME_SHUSHU = 115;
    public static final int ITEM_TYPE_POTION_FAMILIER = 116;

    //Alineaciones
    public static final int ALINEAMIENTO_NEUTRAL = -1;
    public static final int ALINEAMIENTO_BONTARIANO = 1;
    public static final int ALINEAMIENTO_BRAKMARIANO = 2;
    public static final int ALINEAMIENTO_MERCENARIO = 3;

    //Elements
    public static final int ELEMENT_NULL = -1;
    public static final int ELEMENT_NEUTRE = 0;
    public static final int ELEMENT_TERRE = 1;
    public static final int ELEMENT_EAU = 2;
    public static final int ELEMENT_FEU = 3;
    public static final int ELEMENT_AIR = 4;

    //Clases o razas
    public static final int CLASE_FECA = 1;
    public static final int CLASE_OSAMODAS = 2;
    public static final int CLASE_ANUTROF = 3;
    public static final int CLASE_SRAM = 4;
    public static final int CLASE_XELOR = 5;
    public static final int CLASE_ZURCARAK = 6;
    public static final int CLASE_ANIRIPSA = 7;
    public static final int CLASE_YOPUKA = 8;
    public static final int CLASE_OCRA = 9;
    public static final int CLASE_SADIDA = 10;
    public static final int CLASE_SACROGRITO = 11;
    public static final int CLASE_PANDAWA = 12;

    //Sexo
    public static final int SEXO_MASCULINO = 0;
    public static final int SEXO_FEMENINO = 1;

    //GamePlay
    public static final int MAX_EFFECTS_ID = 1500;
    //Buff a v�rifier en d�but de tour
    public static final int[] BEGIN_TURN_BUFF = {91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 108};
    //Buff des Armes
    public static final int[] ARMES_EFFECT_IDS = {91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 108};
    //Buff a ne pas booster en cas de CC
    public static final int[] NO_BOOST_CC_IDS = {101};
    //Invocation Statiques
    public static final int[] STATIC_INVOCATIONS = {282, 556, 2750, 7000};                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    //Arbre et Cawotte s'tout :p
    //Verif d'Etat au lancement d'un sort {spellID,stateID}, � completer avant d'activer
    public static final int[][] STATE_REQUIRED = {{699, Constantes.ETAT_SAOUL}, {690, Constantes.ETAT_SAOUL}};
    //Buff d�clench� en cas de frappe
    public static final int[] ON_HIT_BUFFS = {9, 79, 107, 788, 606, 607, 608, 609, 611};
    //Effects
    public static final int STATS_ADD_PM2 = 78;
    public static final int STATS_REM_PA = 101;
    public static final int STATS_ADD_VIE = 110;
    public static final int STATS_ADD_PA = 111;
    public static final int STATS_MULTIPLY_DOMMAGE = 114;
    public static final int STATS_ADD_CC = 115;
    public static final int STATS_REM_PO = 116;
    public static final int STATS_ADD_PO = 117;
    public static final int STATS_ADD_FORC = 118;
    public static final int STATS_ADD_AGIL = 119;
    public static final int STATS_ADD_SUM=182;
    public static final int STATS_ADD_PA2 = 120;
    public static final int STATS_ADD_DOMA = 121;
    public static final int STATS_ADD_EC = 122;
    public static final int STATS_ADD_CHAN = 123;
    public static final int STATS_ADD_SAGE = 124;
    public static final int STATS_ADD_VITA = 125;
    public static final int STATS_ADD_INTE = 126;
    public static final int STATS_REM_PM = 127;
    public static final int STATS_ADD_PM = 128;
    public static final int STATS_ADD_PERDOM = 138;
    public static final int STATS_ADD_PDOM = 142;
    public static final int STATS_REM_DOMA = 145;
    public static final int STATS_REM_CHAN = 152;
    public static final int STATS_REM_VITA = 153;
    public static final int STATS_REM_AGIL = 154;
    public static final int STATS_REM_INTE = 155;
    public static final int STATS_REM_SAGE = 156;
    public static final int STATS_REM_FORC = 157;
    public static final int STATS_ADD_PODS = 158;
    public static final int STATS_REM_PODS = 159;
    public static final int STATS_ADD_AFLEE = 160;
    public static final int STATS_ADD_MFLEE = 161;
    public static final int STATS_REM_AFLEE = 162;
    public static final int STATS_REM_MFLEE = 163;
    public static final int STATS_ADD_MAITRISE = 165;
    public static final int STATS_REM_PA2 = 168;
    public static final int STATS_REM_PM2 = 169;
    public static final int STATS_REM_CC = 171;
    public static final int STATS_ADD_INIT = 174;
    public static final int STATS_REM_INIT = 175;
    public static final int STATS_ADD_PROS = 176;
    public static final int STATS_REM_PROS = 177;
    public static final int STATS_ADD_SOIN = 178;
    public static final int STATS_REM_SOIN = 179;
    public static final int STATS_CREATURE = 182;
    public static final int STATS_ADD_RP_TER = 210;
    public static final int STATS_ADD_RP_EAU = 211;
    public static final int STATS_ADD_RP_AIR = 212;
    public static final int STATS_ADD_RP_FEU = 213;
    public static final int STATS_ADD_RP_NEU = 214;
    public static final int STATS_REM_RP_TER = 215;
    public static final int STATS_REM_RP_EAU = 216;
    public static final int STATS_REM_RP_AIR = 217;
    public static final int STATS_REM_RP_FEU = 218;
    public static final int STATS_REM_RP_NEU = 219;
    public static final int STATS_RETDOM = 220;
    public static final int STATS_TRAPDOM = 225;
    public static final int STATS_TRAPPER = 226;
    public static final int STATS_ADD_R_FEU = 240;
    public static final int STATS_ADD_R_NEU = 241;
    public static final int STATS_ADD_R_TER = 242;
    public static final int STATS_ADD_R_EAU = 243;
    public static final int STATS_ADD_R_AIR = 244;
    public static final int STATS_REM_R_FEU = 245;
    public static final int STATS_REM_R_NEU = 246;
    public static final int STATS_REM_R_TER = 247;
    public static final int STATS_REM_R_EAU = 248;
    public static final int STATS_REM_R_AIR = 249;
    public static final int STATS_ADD_RP_PVP_TER = 250;
    public static final int STATS_ADD_RP_PVP_EAU = 251;
    public static final int STATS_ADD_RP_PVP_AIR = 252;
    public static final int STATS_ADD_RP_PVP_FEU = 253;
    public static final int STATS_ADD_RP_PVP_NEU = 254;
    public static final int STATS_REM_RP_PVP_TER = 255;
    public static final int STATS_REM_RP_PVP_EAU = 256;
    public static final int STATS_REM_RP_PVP_AIR = 257;
    public static final int STATS_REM_RP_PVP_FEU = 258;
    public static final int STATS_REM_RP_PVP_NEU = 259;
    public static final int STATS_ADD_R_PVP_TER = 260;
    public static final int STATS_ADD_R_PVP_EAU = 261;
    public static final int STATS_ADD_R_PVP_AIR = 262;
    public static final int STATS_ADD_R_PVP_FEU = 263;
    public static final int STATS_ADD_R_PVP_NEU = 264;
    public static final int STATS_ADD_PUSH = 1004;
    public static final int STATS_REM_PUSH = 1005;
    public static final int STATS_ADD_R_PUSH = 1006;
    public static final int STATS_REM_R_PUSH = 1007;
    public static final int STATS_ADD_ERO = 1009;
    public static final int STATS_REM_ERO = 1010;
    public static final int STATS_ADD_R_ERO = 1011;
    public static final int STATS_REM_R_ERO = 1012;
    //Effets ID & Buffs
    public static final int EFFECT_PASS_TURN = 140;

    public static final int STATS_FORGET_ONE_LEVEL_SPELL = 616;
    //Capture
    public static final int CAPTURE_MONSTRE = 623;
    //Familier
    public static final int STATS_PETS_PDV = 800;
    public static final int STATS_PETS_POIDS = 806;
    public static final int STATS_PETS_REPAS = 807;
    public static final int STATS_PETS_DATE = 808;
    public static final int STATS_PETS_EPO = 940;
    public static final int STATS_PETS_SOUL = 717;
    // Objet d'�levage
    public static final int STATS_RESIST = 812;
    // Other
    public static final int STATS_TURN = 811;
    public static final int STATS_EXCHANGE_IN = 983;
    public static final int STATS_CHANGE_BY = 985;
    public static final int STATS_BUILD_BY = 988;
    public static final int STATS_NAME_TRAQUE = 989;
    public static final int STATS_GRADE_TRAQUE = 961;
    public static final int STATS_ALIGNEMENT_TRAQUE = 960;
    public static final int STATS_NIVEAU_TRAQUE = 962;

    public static final int STATS_DATE = 805;
    public static final int STATS_NIVEAU = 962;
    public static final int STATS_NAME_DJ = 814;
    public static final int STATS_OWNER_1 = 987;//#4
    public static final int STATS_SIGNATURE = 988;
    public static final int ERR_STATS_XP = 1000;
    //ZAAPI <alignID,{mapID,mapID,...,mapID}>
    public static Map<Integer, String> ZAAPI = new HashMap<>();
    //ZAAP <mapID,cellID>
    public static Map<Integer, Integer> ZAAPS = new HashMap<>();
    //Valeur des droits de guilde
    public static int[] G_RIGHTS = new int[] {2, 4, 8, 16, 32, 64, 128, 256, 512, 4096, 8192, 16384};
    public static int G_BOOST = 2;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //G�rer les boost
    public static int G_RIGHT = 4;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //G�rer les droits
    public static int G_INVITE = 8;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Inviter de nouveaux membres
    public static int G_BAN = 16;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Bannir
    public static int G_ALLXP = 32;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //G�rer les r�partitions d'xp
    public static int G_HISXP = 256;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //G�rer sa r�partition d'xp
    public static int G_RANK = 64;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //G�rer les rangs
    public static int G_POSPERCO = 128;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Poser un percepteur
    public static int G_COLLPERCO = 512;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Collecter les percepteurs
    public static int G_USEENCLOS = 4096;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Utiliser les enclos
    public static int G_AMENCLOS = 8192;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Am�nager les enclos
    public static int G_OTHDINDE = 16384;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        //G�rer les montures des autres membres
    //Valeur des droits de maison
    public static int H_GBLASON = 2;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Afficher blason pour membre de la guilde
    public static int H_OBLASON = 4;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Afficher blason pour les autres
    public static int H_GNOCODE = 8;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Entrer sans code pour la guilde
    public static int H_OCANTOPEN = 16;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Entrer impossible pour les non-guildeux
    public static int C_GNOCODE = 32;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Coffre sans code pour la guilde
    public static int C_OCANTOPEN = 64;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Coffre impossible pour les non-guildeux
    public static int H_GREPOS = 256;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Guilde droit au repos
    public static int H_GTELE = 128;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Guilde droit a la TP
    // Nom des documents (swfs) : Documents d'avis de recherche
    public static String HUNT_DETAILS_DOC = "71_0706251229";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // PanMap d'explications
    public static String HUNT_FRAKACIA_DOC = "63_0706251124";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Frakacia Leukocythine
    public static String HUNT_AERMYNE_DOC = "100_0706251214";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            // Aermyne 'Braco' Scalptaras
    public static String HUNT_MARZWEL_DOC = "96_0706251201";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Marzwel le Gobelin
    public static String HUNT_BRUMEN_DOC = "68_0706251126";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Brumen Tinctorias
    public static String HUNT_MUSHA_DOC = "94_0706251138";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Musha l'Oni
    public static String HUNT_OGIVOL_DOC = "69_0706251058";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Ogivol Scarlacin
    public static String HUNT_PADGREF_DOC = "61_0802081743";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Padgref Demoel
    public static String HUNT_QILBIL_DOC = "67_0706251223";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Qil Bil
    public static String HUNT_ROK_DOC = "93_0706251135";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Rok Gnorok
    public static String HUNT_ZATOISHWAN_DOC = "98_0706251211";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Zatoïshwan
    public static String HUNT_LETHALINE_DOC = "65_0706251123";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Léthaline Sigisbul
    //public static String HUNT_NERVOES_DOC    = "64_0706251123";  // Nervoes Brakdoun
    public static String HUNT_FOUDUGLEN_DOC = "70_0706251122";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Fouduglen l'�cureuil

    // {(int)BorneId, (int)CellId, (str)SwfDocName, (int)MobId, (int)ItemFollow, (int)QuestId, (int)reponseID
    public static String[][] HUNTING_QUESTS = {{"1988", "234", HUNT_DETAILS_DOC, "-1", "-1", "-1", "-1"}, {"1986", "161", HUNT_LETHALINE_DOC, "-1", "-1", "-1", "-1"}, {"1985", "119", HUNT_MARZWEL_DOC, "554", "7353", "117", "2552"}, {"1986", "120", HUNT_PADGREF_DOC, "459", "6870", "29", "2108"}, {"1985", "149", HUNT_FRAKACIA_DOC, "460", "6871", "30", "2109"}, {"1986", "150", HUNT_QILBIL_DOC, "481", "6873", "32", "2111"}, {"1986", "179", HUNT_BRUMEN_DOC, "464", "6874", "33", "2112"}, {"1986", "180", HUNT_OGIVOL_DOC, "462", "6876", "35", "2114"}, {"1985", "269", HUNT_MUSHA_DOC, "552", "7352", "116", "2551"}, {"1986", "270", HUNT_FOUDUGLEN_DOC, "463", "6875", "34", "2113"}, {"1985", "299", HUNT_ROK_DOC, "550", "7351", "115", "2550"}, {"1986", "300", HUNT_AERMYNE_DOC, "446", "7350", "119", "2554"}, {"1985", "329", HUNT_ZATOISHWAN_DOC, "555", "7354", "118", "2553"},};

    public static int getQuestByMobSkin(int mobSkin) {
        for (String[] huntingQuest : HUNTING_QUESTS)
            if (Mundo.mundo.getMonstre(Integer.parseInt(huntingQuest[3])) != null
                    && Mundo.mundo.getMonstre(Integer.parseInt(huntingQuest[3])).getGfxId() == mobSkin)
                return Integer.parseInt(huntingQuest[5]);
        return -1;
    }

    public static int getSkinByHuntMob(int mobId) {
        for (String[] huntingQuest : HUNTING_QUESTS)
            if (Integer.parseInt(huntingQuest[3]) == mobId)
                return Mundo.mundo.getMonstre(mobId).getGfxId();
        return -1;
    }

    public static int getItemByHuntMob(int mobId) {
        for (String[] huntingQuest : HUNTING_QUESTS)
            if (Integer.parseInt(huntingQuest[3]) == mobId)
                return Integer.parseInt(huntingQuest[4]);
        return -1;
    }

    public static int getItemByMobSkin(int mobSkin) {
        for (String[] huntingQuest : HUNTING_QUESTS)
            if (Mundo.mundo.getMonstre(Integer.parseInt(huntingQuest[3])) != null
                    && Mundo.mundo.getMonstre(Integer.parseInt(huntingQuest[3])).getGfxId() == mobSkin)
                return Integer.parseInt(huntingQuest[4]);
        return -1;
    }

    public static String getDocNameByBornePos(int borneId, int cellid) {
        for (String[] huntingQuest : HUNTING_QUESTS)
            if (Integer.parseInt(huntingQuest[0]) == borneId
                    && Integer.parseInt(huntingQuest[1]) == cellid)
                return huntingQuest[2];
        return "";
    }

    public static short getClassStatueMap(int classID) {
        short pos = 10298;
        return switch (classID) {
            case 1 -> (short) 7398;
            case 2 -> (short) 7545;
            case 3 -> (short) 7442;
            case 4 -> (short) 7392;
            case 5 -> (short) 7332;
            case 6 -> (short) 7446;
            case 7 -> (short) 7361;
            case 8, 13 -> (short) 7427;
            case 9 -> (short) 7378;
            case 10 -> (short) 7395;
            case 11 -> (short) 7336;
            case 12 -> (short) 8035;
            default -> pos;
        };
    }

    public static int getClassStatueCell(int classID) {
        int pos = 314;
        return switch (classID) {
            case 1 -> 299;
            case 2 -> 311;
            case 3 -> 255;
            case 4, 13, 8 -> 282;
            case 5 -> 326;
            case 6 -> 300;
            case 7 -> 207;
            case 9 -> 368;
            case 10 -> 370;
            case 11 -> 197;
            case 12 -> 384;
            default -> pos;
        };
    }

    public static short getStartMap(int classID) {
        short pos = switch (classID) {
            case Constantes.CLASE_FECA -> (short) 10300;
            case Constantes.CLASE_OSAMODAS -> (short) 10284;
            case Constantes.CLASE_ANUTROF -> (short) 10299;
            case Constantes.CLASE_SRAM -> (short) 10285;
            case Constantes.CLASE_XELOR -> (short) 10298;
            case Constantes.CLASE_ZURCARAK -> (short) 10276;
            case Constantes.CLASE_ANIRIPSA -> (short) 10283;
            case Constantes.CLASE_YOPUKA -> (short) 10294;
            case Constantes.CLASE_OCRA -> (short) 10292;
            case Constantes.CLASE_SADIDA -> (short) 10279;
            case Constantes.CLASE_SACROGRITO -> (short) 10296;
            case Constantes.CLASE_PANDAWA -> (short) 10289;
            default -> (short) 10298;
        };

        return pos;
    }

    public static int getStartCell(int classID) {
        int pos = switch (classID) {
            case Constantes.CLASE_FECA -> 323;
            case Constantes.CLASE_OSAMODAS -> 372;
            case Constantes.CLASE_ANUTROF -> 271;
            case Constantes.CLASE_SRAM -> 263;
            case Constantes.CLASE_XELOR -> 300;
            case Constantes.CLASE_ZURCARAK -> 296;
            case Constantes.CLASE_ANIRIPSA -> 299;
            case Constantes.CLASE_YOPUKA -> 280;
            case Constantes.CLASE_OCRA -> 284;
            case Constantes.CLASE_SADIDA -> 254;
            case Constantes.CLASE_SACROGRITO -> 243;
            case Constantes.CLASE_PANDAWA -> 236;
            default -> 314;
        };
        return pos;
    }

    public static HashMap<Integer, Character> getStartSortsPlaces(int classID) {
        HashMap<Integer, Character> start = new HashMap<>();
        switch (classID) {
            case CLASE_FECA -> {
                start.put(3, 'b');//Attaque Naturelle
                start.put(6, 'c');//Armure Terrestre
                start.put(17, 'd');//Glyphe Agressif
            }
            case CLASE_SRAM -> {
                start.put(61, 'b');//Sournoiserie
                start.put(72, 'c');//Invisibilit�
                start.put(65, 'd');//Piege sournois
            }
            case CLASE_ANIRIPSA -> {
                start.put(125, 'b');//Mot Interdit
                start.put(128, 'c');//Mot de Frayeur
                start.put(121, 'd');//Mot Curatif
            }
            case CLASE_ZURCARAK -> {
                start.put(102, 'b');//Pile ou Face
                start.put(103, 'c');//Chance d'ecaflip
                start.put(105, 'd');//Bond du felin
            }
            case CLASE_OCRA -> {
                start.put(161, 'b');//Fleche Magique
                start.put(169, 'c');//Fleche de Recul
                start.put(164, 'd');//Fleche Empoisonn�e(ex Fleche chercheuse)
            }
            case CLASE_YOPUKA -> {
                start.put(143, 'b');//Intimidation
                start.put(141, 'c');//Pression
                start.put(142, 'd');//Bond
            }
            case CLASE_SADIDA -> {
                start.put(183, 'b');//Ronce
                start.put(200, 'c');//Poison Paralysant
                start.put(193, 'd');//La bloqueuse
            }
            case CLASE_OSAMODAS -> {
                start.put(34, 'b');//Invocation de tofu
                start.put(21, 'c');//Griffe Spectrale
                start.put(23, 'd');//Cri de l'ours
            }
            case CLASE_XELOR -> {
                start.put(82, 'b');//Contre
                start.put(81, 'c');//Ralentissement
                start.put(83, 'd');//Aiguille
            }
            case CLASE_PANDAWA -> {
                start.put(686, 'b');//Picole
                start.put(692, 'c');//Gueule de bois
                start.put(687, 'd');//Poing enflamm�
            }
            case CLASE_ANUTROF -> {
                start.put(51, 'b');//Lancer de Piece
                start.put(43, 'c');//Lancer de Pelle
                start.put(41, 'd');//Sac anim�
            }
            case CLASE_SACROGRITO -> {
                start.put(432, 'b');//Pied du Sacrieur
                start.put(431, 'c');//Chatiment Os�
                start.put(434, 'd');//Attirance
            }
        }
        return start;
    }

    public static HashMap<Integer, Hechizo.SortStats> getStartSorts(int classID) {
        HashMap<Integer, Hechizo.SortStats> start = new HashMap<>();
        switch (classID) {
            case CLASE_FECA -> {
                start.put(3, Mundo.mundo.getSort(3).getStatsByLevel(1));//Attaque Naturelle
                start.put(6, Mundo.mundo.getSort(6).getStatsByLevel(1));//Armure Terrestre
                start.put(17, Mundo.mundo.getSort(17).getStatsByLevel(1));//Glyphe Agressif
            }
            case CLASE_SRAM -> {
                start.put(61, Mundo.mundo.getSort(61).getStatsByLevel(1));//Sournoiserie
                start.put(72, Mundo.mundo.getSort(72).getStatsByLevel(1));//Invisibilit�
                start.put(65, Mundo.mundo.getSort(65).getStatsByLevel(1));//Piege sournois
            }
            case CLASE_ANIRIPSA -> {
                start.put(125, Mundo.mundo.getSort(125).getStatsByLevel(1));//Mot Interdit
                start.put(128, Mundo.mundo.getSort(128).getStatsByLevel(1));//Mot de Frayeur
                start.put(121, Mundo.mundo.getSort(121).getStatsByLevel(1));//Mot Curatif
            }
            case CLASE_ZURCARAK -> {
                start.put(102, Mundo.mundo.getSort(102).getStatsByLevel(1));//Pile ou Face
                start.put(103, Mundo.mundo.getSort(103).getStatsByLevel(1));//Chance d'ecaflip
                start.put(105, Mundo.mundo.getSort(105).getStatsByLevel(1));//Bond du felin
            }
            case CLASE_OCRA -> {
                start.put(161, Mundo.mundo.getSort(161).getStatsByLevel(1));//Fleche Magique
                start.put(169, Mundo.mundo.getSort(169).getStatsByLevel(1));//Fleche de Recul
                start.put(164, Mundo.mundo.getSort(164).getStatsByLevel(1));//Fleche Empoisonn�e(ex Fleche chercheuse)
            }
            case CLASE_YOPUKA -> {
                start.put(143, Mundo.mundo.getSort(143).getStatsByLevel(1));//Intimidation
                start.put(141, Mundo.mundo.getSort(141).getStatsByLevel(1));//Pression
                start.put(142, Mundo.mundo.getSort(142).getStatsByLevel(1));//Bond
            }
            case CLASE_SADIDA -> {
                start.put(183, Mundo.mundo.getSort(183).getStatsByLevel(1));//Ronce
                start.put(200, Mundo.mundo.getSort(200).getStatsByLevel(1));//Poison Paralysant
                start.put(193, Mundo.mundo.getSort(193).getStatsByLevel(1));//La bloqueuse
            }
            case CLASE_OSAMODAS -> {
                start.put(34, Mundo.mundo.getSort(34).getStatsByLevel(1));//Invocation de tofu
                start.put(21, Mundo.mundo.getSort(21).getStatsByLevel(1));//Griffe Spectrale
                start.put(23, Mundo.mundo.getSort(23).getStatsByLevel(1));//Cri de l'ours
            }
            case CLASE_XELOR -> {
                start.put(82, Mundo.mundo.getSort(82).getStatsByLevel(1));//Contre
                start.put(81, Mundo.mundo.getSort(81).getStatsByLevel(1));//Ralentissement
                start.put(83, Mundo.mundo.getSort(83).getStatsByLevel(1));//Aiguille
            }
            case CLASE_PANDAWA -> {
                start.put(686, Mundo.mundo.getSort(686).getStatsByLevel(1));//Picole
                start.put(692, Mundo.mundo.getSort(692).getStatsByLevel(1));//Gueule de bois
                start.put(687, Mundo.mundo.getSort(687).getStatsByLevel(1));//Poing enflamm�
            }
            case CLASE_ANUTROF -> {
                start.put(51, Mundo.mundo.getSort(51).getStatsByLevel(1));//Lancer de Piece
                start.put(43, Mundo.mundo.getSort(43).getStatsByLevel(1));//Lancer de Pelle
                start.put(41, Mundo.mundo.getSort(41).getStatsByLevel(1));//Sac anim�
            }
            case CLASE_SACROGRITO -> {
                start.put(432, Mundo.mundo.getSort(432).getStatsByLevel(1));//Pied du Sacrieur
                start.put(431, Mundo.mundo.getSort(431).getStatsByLevel(1));//Chatiment Forc�
                start.put(434, Mundo.mundo.getSort(434).getStatsByLevel(1));//Attirance
            }
        }
        return start;
    }

    public static int getReqPtsToBoostStatsByClass(int classID, int statID,
                                                   int val) {
        switch (statID) {
            case 11://Vitalidad
                return 1;
            case 12://Sabiduria
                return 3;
            case 10://Fuerza
                switch (classID) {
                    case CLASE_SACROGRITO:
                        return 3;

                    case CLASE_FECA:

                    case CLASE_XELOR:

                    case CLASE_OSAMODAS:

                    case CLASE_ANIRIPSA:
                        if (val < 50)
                            return 2;
                        if (val < 150)
                            return 3;
                        if (val < 250)
                            return 4;
                        return 5;

                    case CLASE_SRAM:

                    case CLASE_ZURCARAK:

                    case CLASE_YOPUKA:
                        if (val < 100)
                            return 1;
                        if (val < 200)
                            return 2;
                        if (val < 300)
                            return 3;
                        if (val < 400)
                            return 4;
                        return 5;

                    case CLASE_PANDAWA:
                        if (val < 50)
                            return 1;
                        if (val < 200)
                            return 2;
                        return 3;

                    case CLASE_SADIDA:
                        if (val < 50)
                            return 1;
                        if (val < 250)
                            return 2;
                        if (val < 300)
                            return 3;
                        if (val < 400)
                            return 4;
                        return 5;

                    case CLASE_OCRA:

                    case CLASE_ANUTROF:
                        if (val < 50)
                            return 1;
                        if (val < 150)
                            return 2;
                        if (val < 250)
                            return 3;
                        if (val < 350)
                            return 4;
                        return 5;
                }
                break;
            case 13://Suerte
                switch (classID) {
                    case CLASE_FECA:

                    case CLASE_XELOR:

                    case CLASE_SRAM:

                    case CLASE_YOPUKA:

                    case CLASE_ZURCARAK:

                    case CLASE_OCRA:

                    case CLASE_ANIRIPSA:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;

                    case CLASE_SACROGRITO:
                        return 3;

                    case CLASE_SADIDA:

                    case CLASE_OSAMODAS:
                        if (val < 100)
                            return 1;
                        if (val < 200)
                            return 2;
                        if (val < 300)
                            return 3;
                        if (val < 400)
                            return 4;
                        return 5;

                    case CLASE_PANDAWA:
                        if (val < 50)
                            return 1;
                        if (val < 200)
                            return 2;
                        return 3;

                    case CLASE_ANUTROF:
                        if (val < 100)
                            return 1;
                        if (val < 150)
                            return 2;
                        if (val < 230)
                            return 3;
                        if (val < 330)
                            return 4;
                        return 5;
                }
                break;
            case 14://Agilidad
                switch (classID) {
                    case CLASE_FECA:

                    case CLASE_XELOR:

                    case CLASE_SADIDA:

                    case CLASE_ANIRIPSA:

                    case CLASE_YOPUKA:

                    case CLASE_ANUTROF:

                    case CLASE_OSAMODAS:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;

                    case CLASE_SACROGRITO:
                        return 3;

                    case CLASE_SRAM:
                        if (val < 100)
                            return 1;
                        if (val < 200)
                            return 2;
                        if (val < 300)
                            return 3;
                        if (val < 400)
                            return 4;
                        return 5;

                    case CLASE_PANDAWA:
                        if (val < 50)
                            return 1;
                        if (val < 200)
                            return 2;
                        return 3;

                    case CLASE_ZURCARAK:

                    case CLASE_OCRA:
                        if (val < 50)
                            return 1;
                        if (val < 100)
                            return 2;
                        if (val < 150)
                            return 3;
                        if (val < 200)
                            return 4;
                        return 5;
                }
                break;
            case 15://Inteligencia
                switch (classID) {
                    case CLASE_XELOR:

                    case CLASE_FECA:

                    case CLASE_SADIDA:

                    case CLASE_ANIRIPSA:

                    case CLASE_OSAMODAS:
                        if (val < 100)
                            return 1;
                        if (val < 200)
                            return 2;
                        if (val < 300)
                            return 3;
                        if (val < 400)
                            return 4;
                        return 5;

                    case CLASE_SACROGRITO:
                        return 3;

                    case CLASE_SRAM:
                        if (val < 50)
                            return 2;
                        if (val < 150)
                            return 3;
                        if (val < 250)
                            return 4;
                        return 5;

                    case CLASE_ANUTROF:
                        if (val < 20)
                            return 1;
                        if (val < 60)
                            return 2;
                        if (val < 100)
                            return 3;
                        if (val < 140)
                            return 4;
                        return 5;

                    case CLASE_PANDAWA:
                        if (val < 50)
                            return 1;
                        if (val < 200)
                            return 2;
                        return 3;

                    case CLASE_YOPUKA:

                    case CLASE_ZURCARAK:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;

                    case CLASE_OCRA:
                        if (val < 50)
                            return 1;
                        if (val < 150)
                            return 2;
                        if (val < 250)
                            return 3;
                        if (val < 350)
                            return 4;
                        return 5;
                }
                break;
        }
        return 5;
    }

    public static void onLevelUpSpells(Jugador perso, int lvl) {
        switch (perso.getClasse()) {
            case CLASE_FECA -> {
                if (lvl == 3)
                    perso.learnSpell(4, 1, true, false, false);//Renvoie de sort
                if (lvl == 6)
                    perso.learnSpell(2, 1, true, false, false);//Aveuglement
                if (lvl == 9)
                    perso.learnSpell(1, 1, true, false, false);//Armure Incandescente
                if (lvl == 13)
                    perso.learnSpell(9, 1, true, false, false);//Attaque nuageuse
                if (lvl == 17)
                    perso.learnSpell(18, 1, true, false, false);//Armure Aqueuse
                if (lvl == 21)
                    perso.learnSpell(20, 1, true, false, false);//Immunit�
                if (lvl == 26)
                    perso.learnSpell(14, 1, true, false, false);//Armure Venteuse
                if (lvl == 31)
                    perso.learnSpell(19, 1, true, false, false);//Bulle
                if (lvl == 36)
                    perso.learnSpell(5, 1, true, false, false);//Tr�ve
                if (lvl == 42)
                    perso.learnSpell(16, 1, true, false, false);//Science du b�ton
                if (lvl == 48)
                    perso.learnSpell(8, 1, true, false, false);// falseur du b�ton
                if (lvl == 54)
                    perso.learnSpell(12, 1, true, false, false);//glyphe d'Aveuglement
                if (lvl == 60)
                    perso.learnSpell(11, 1, true, false, false);//T�l�portation
                if (lvl == 70)
                    perso.learnSpell(10, 1, true, false, false);//Glyphe Enflamm�
                if (lvl == 80)
                    perso.learnSpell(7, 1, true, false, false);//Bouclier F�ca
                if (lvl == 90)
                    perso.learnSpell(15, 1, true, false, false);//Glyphe d'Immobilisation
                if (lvl == 100)
                    perso.learnSpell(13, 1, true, false, false);//Glyphe de Silence
                if (lvl == 200)
                    perso.learnSpell(1901, 1, true, false, false);//Invocation de Dopeul F�ca
            }
            case CLASE_OSAMODAS -> {
                if (lvl == 3)
                    perso.learnSpell(26, 1, true, false, false);//B�n�diction Animale
                if (lvl == 6)
                    perso.learnSpell(22, 1, true, false, false);//D�placement F�lin
                if (lvl == 9)
                    perso.learnSpell(35, 1, true, false, false);//Invocation de Bouftou
                if (lvl == 13)
                    perso.learnSpell(28, 1, true, false, false);//Crapaud
                if (lvl == 17)
                    perso.learnSpell(37, 1, true, false, false);//Invocation de Prespic
                if (lvl == 21)
                    perso.learnSpell(30, 1, true, false, false);//Fouet
                if (lvl == 26)
                    perso.learnSpell(27, 1, true, false, false);//Piq�re Motivante
                if (lvl == 31)
                    perso.learnSpell(24, 1, true, false, false);//Corbeau
                if (lvl == 36)
                    perso.learnSpell(33, 1, true, false, false);//Griffe Cinglante
                if (lvl == 42)
                    perso.learnSpell(25, 1, true, false, false);//Soin Animal
                if (lvl == 48)
                    perso.learnSpell(38, 1, true, false, false);//Invocation de Sanglier
                if (lvl == 54)
                    perso.learnSpell(36, 1, true, false, false);//Frappe du Craqueleur
                if (lvl == 60)
                    perso.learnSpell(32, 1, true, false, false);//R�sistance Naturelle
                if (lvl == 70)
                    perso.learnSpell(29, 1, true, false, false);//Crocs du Mulou
                if (lvl == 80)
                    perso.learnSpell(39, 1, true, false, false);//Invocation de Bwork Mage
                if (lvl == 90)
                    perso.learnSpell(40, 1, true, false, false);//Invocation de Craqueleur
                if (lvl == 100)
                    perso.learnSpell(31, 1, true, false, false);//Invocation de Dragonnet Rouge
                if (lvl == 200)
                    perso.learnSpell(1902, 1, true, false, false);//Invocation de Dopeul Osamodas
            }
            case CLASE_ANUTROF -> {
                if (lvl == 3)
                    perso.learnSpell(49, 1, true, false, false);//Pelle Fantomatique
                if (lvl == 6)
                    perso.learnSpell(42, 1, true, false, false);//Chance
                if (lvl == 9)
                    perso.learnSpell(47, 1, true, false, false);//Bo�te de Pandore
                if (lvl == 13)
                    perso.learnSpell(48, 1, true, false, false);//Remblai
                if (lvl == 17)
                    perso.learnSpell(45, 1, true, false, false);//Cl� R�ductrice
                if (lvl == 21)
                    perso.learnSpell(53, 1, true, false, false);//Force de l'Age
                if (lvl == 26)
                    perso.learnSpell(46, 1, true, false, false);//D�sinvocation
                if (lvl == 31)
                    perso.learnSpell(52, 1, true, false, false);//Cupidit�
                if (lvl == 36)
                    perso.learnSpell(44, 1, true, false, false);//Roulage de Pelle
                if (lvl == 42)
                    perso.learnSpell(50, 1, true, false, false);//Maladresse
                if (lvl == 48)
                    perso.learnSpell(54, 1, true, false, false);//Maladresse de Masse
                if (lvl == 54)
                    perso.learnSpell(55, 1, true, false, false);//Acc�l�ration
                if (lvl == 60)
                    perso.learnSpell(56, 1, true, false, false);//Pelle du Jugement
                if (lvl == 70)
                    perso.learnSpell(58, 1, true, false, false);//Pelle Massacrante
                if (lvl == 80)
                    perso.learnSpell(59, 1, true, false, false);//Corruption
                if (lvl == 90)
                    perso.learnSpell(57, 1, true, false, false);//Pelle Anim�e
                if (lvl == 100)
                    perso.learnSpell(60, 1, true, false, false);//Coffre Anim�
                if (lvl == 200)
                    perso.learnSpell(1903, 1, true, false, false);//Invocation de Dopeul Enutrof
            }
            case CLASE_SRAM -> {
                if (lvl == 3)
                    perso.learnSpell(66, 1, true, false, false);//Poison insidieux
                if (lvl == 6)
                    perso.learnSpell(68, 1, true, false, false);//Fourvoiement
                if (lvl == 9)
                    perso.learnSpell(63, 1, true, false, false);//Coup Sournois
                if (lvl == 13)
                    perso.learnSpell(74, 1, true, false, false);//Double
                if (lvl == 17)
                    perso.learnSpell(64, 1, true, false, false);//Rep�rage
                if (lvl == 21)
                    perso.learnSpell(79, 1, true, false, false);//Pi�ge de Masse
                if (lvl == 26)
                    perso.learnSpell(78, 1, true, false, false);//Invisibilit� d'Autrui
                if (lvl == 31)
                    perso.learnSpell(71, 1, true, false, false);//Pi�ge Empoisonn�
                if (lvl == 36)
                    perso.learnSpell(62, 1, true, false, false);//Concentration de Chakra
                if (lvl == 42)
                    perso.learnSpell(69, 1, true, false, false);//Pi�ge d'Immobilisation
                if (lvl == 48)
                    perso.learnSpell(77, 1, true, false, false);//Pi�ge de Silence
                if (lvl == 54)
                    perso.learnSpell(73, 1, true, false, false);//Pi�ge r�pulsif
                if (lvl == 60)
                    perso.learnSpell(67, 1, true, false, false);//Peur
                if (lvl == 70)
                    perso.learnSpell(70, 1, true, false, false);//Arnaque
                if (lvl == 80)
                    perso.learnSpell(75, 1, true, false, false);//Pulsion de Chakra
                if (lvl == 90)
                    perso.learnSpell(76, 1, true, false, false);//Attaque Mortelle
                if (lvl == 100)
                    perso.learnSpell(80, 1, true, false, false);//Pi�ge Mortel
                if (lvl == 200)
                    perso.learnSpell(1904, 1, true, false, false);//Invocation de Dopeul Sram
            }
            case CLASE_XELOR -> {
                if (lvl == 3)
                    perso.learnSpell(84, 1, true, false, false);//Gelure
                if (lvl == 6)
                    perso.learnSpell(100, 1, true, false, false);//Sablier de X�lor
                if (lvl == 9)
                    perso.learnSpell(92, 1, true, false, false);//Rayon Obscur
                if (lvl == 13)
                    perso.learnSpell(88, 1, true, false, false);//T�l�portation
                if (lvl == 17)
                    perso.learnSpell(93, 1, true, false, false);//Fl�trissement
                if (lvl == 21)
                    perso.learnSpell(85, 1, true, false, false);//Flou
                if (lvl == 26)
                    perso.learnSpell(96, 1, true, false, false);//Poussi�re Temporelle
                if (lvl == 31)
                    perso.learnSpell(98, 1, true, false, false);//Vol du Temps
                if (lvl == 36)
                    perso.learnSpell(86, 1, true, false, false);//Aiguille Chercheuse
                if (lvl == 42)
                    perso.learnSpell(89, 1, true, false, false);//D�vouement
                if (lvl == 48)
                    perso.learnSpell(90, 1, true, false, false);//Fuite
                if (lvl == 54)
                    perso.learnSpell(87, 1, true, false, false);//D�motivation
                if (lvl == 60)
                    perso.learnSpell(94, 1, true, false, false);//Protection Aveuglante
                if (lvl == 70)
                    perso.learnSpell(99, 1, true, false, false);//Momification
                if (lvl == 80)
                    perso.learnSpell(95, 1, true, false, false);//Horloge
                if (lvl == 90)
                    perso.learnSpell(91, 1, true, false, false);//Frappe de X�lor
                if (lvl == 100)
                    perso.learnSpell(97, 1, true, false, false);//Cadran de X�lor
                if (lvl == 200)
                    perso.learnSpell(1905, 1, true, false, false);//Invocation de Dopeul X�lor
            }
            case CLASE_ZURCARAK -> {
                if (lvl == 3)
                    perso.learnSpell(109, 1, true, false, false);//Bluff
                if (lvl == 6)
                    perso.learnSpell(113, 1, true, false, false);//Perception
                if (lvl == 9)
                    perso.learnSpell(111, 1, true, false, false);//Contrecoup
                if (lvl == 13)
                    perso.learnSpell(104, 1, true, false, false);//Tr�fle
                if (lvl == 17)
                    perso.learnSpell(119, 1, true, false, false);//Tout ou rien
                if (lvl == 21)
                    perso.learnSpell(101, 1, true, false, false);//Roulette
                if (lvl == 26)
                    perso.learnSpell(107, 1, true, false, false);//Topkaj
                if (lvl == 31)
                    perso.learnSpell(116, 1, true, false, false);//Langue R�peuse
                if (lvl == 36)
                    perso.learnSpell(106, 1, true, false, false);//Roue de la Fortune
                if (lvl == 42)
                    perso.learnSpell(117, 1, true, false, false);//Griffe Invocatrice
                if (lvl == 48)
                    perso.learnSpell(108, 1, true, false, false);//Esprit F�lin
                if (lvl == 54)
                    perso.learnSpell(115, 1, true, false, false);//Odorat
                if (lvl == 60)
                    perso.learnSpell(118, 1, true, false, false);//R�flexes
                if (lvl == 70)
                    perso.learnSpell(110, 1, true, false, false);//Griffe Joueuse
                if (lvl == 80)
                    perso.learnSpell(112, 1, true, false, false);//Griffe de Ceangal
                if (lvl == 90)
                    perso.learnSpell(114, 1, true, false, false);//Rekop
                if (lvl == 100)
                    perso.learnSpell(120, 1, true, false, false);//Destin d'Ecaflip
                if (lvl == 200)
                    perso.learnSpell(1906, 1, true, false, false);//Invocation de Dopeul Ecaflip
            }
            case CLASE_ANIRIPSA -> {
                if (lvl == 3)
                    perso.learnSpell(124, 1, true, false, false);//Mot Soignant
                if (lvl == 6)
                    perso.learnSpell(122, 1, true, false, false);//Mot Blessant
                if (lvl == 9)
                    perso.learnSpell(126, 1, true, false, false);//Mot Stimulant
                if (lvl == 13)
                    perso.learnSpell(127, 1, true, false, false);//Mot de Pr�vention
                if (lvl == 17)
                    perso.learnSpell(123, 1, true, false, false);//Mot Drainant
                if (lvl == 21)
                    perso.learnSpell(130, 1, true, false, false);//Mot Revitalisant
                if (lvl == 26)
                    perso.learnSpell(131, 1, true, false, false);//Mot de R�g�n�ration
                if (lvl == 31)
                    perso.learnSpell(132, 1, true, false, false);//Mot d'Epine
                if (lvl == 36)
                    perso.learnSpell(133, 1, true, false, false);//Mot de Jouvence
                if (lvl == 42)
                    perso.learnSpell(134, 1, true, false, false);//Mot Vampirique
                if (lvl == 48)
                    perso.learnSpell(135, 1, true, false, false);//Mot de Sacrifice
                if (lvl == 54)
                    perso.learnSpell(129, 1, true, false, false);//Mot d'Amiti�
                if (lvl == 60)
                    perso.learnSpell(136, 1, true, false, false);//Mot d'Immobilisation
                if (lvl == 70)
                    perso.learnSpell(137, 1, true, false, false);//Mot d'Envol
                if (lvl == 80)
                    perso.learnSpell(138, 1, true, false, false);//Mot de Silence
                if (lvl == 90)
                    perso.learnSpell(139, 1, true, false, false);//Mot d'Altruisme
                if (lvl == 100)
                    perso.learnSpell(140, 1, true, false, false);//Mot de Reconstitution
                if (lvl == 200)
                    perso.learnSpell(1907, 1, true, false, false);//Invocation de Dopeul Eniripsa
            }
            case CLASE_YOPUKA -> {
                if (lvl == 3)
                    perso.learnSpell(144, 1, true, false, false);//Compulsion
                if (lvl == 6)
                    perso.learnSpell(145, 1, true, false, false);//Ep�e Divine
                if (lvl == 9)
                    perso.learnSpell(146, 1, true, false, false);//Ep�e du Destin
                if (lvl == 13)
                    perso.learnSpell(147, 1, true, false, false);//Guide de Bravoure
                if (lvl == 17)
                    perso.learnSpell(148, 1, true, false, false);//Amplification
                if (lvl == 21)
                    perso.learnSpell(154, 1, true, false, false);//Ep�e Destructrice
                if (lvl == 26)
                    perso.learnSpell(150, 1, true, false, false);//Couper
                if (lvl == 31)
                    perso.learnSpell(151, 1, true, false, false);//Souffle
                if (lvl == 36)
                    perso.learnSpell(155, 1, true, false, false);//Vitalit�
                if (lvl == 42)
                    perso.learnSpell(152, 1, true, false, false);//Ep�e du Jugement
                if (lvl == 48)
                    perso.learnSpell(153, 1, true, false, false);//Puissance
                if (lvl == 54)
                    perso.learnSpell(149, 1, true, false, false);//Mutilation
                if (lvl == 60)
                    perso.learnSpell(156, 1, true, false, false);//Temp�te de Puissance
                if (lvl == 70)
                    perso.learnSpell(157, 1, true, false, false);//Ep�e C�leste
                if (lvl == 80)
                    perso.learnSpell(158, 1, true, false, false);//Concentration
                if (lvl == 90)
                    perso.learnSpell(160, 1, true, false, false);//Ep�e de Iop
                if (lvl == 100)
                    perso.learnSpell(159, 1, true, false, false);//Col�re de Iop
                if (lvl == 200)
                    perso.learnSpell(1908, 1, true, false, false);//Invocation de Dopeul Iop
            }
            case CLASE_OCRA -> {
                if (lvl == 3)
                    perso.learnSpell(163, 1, true, false, false);//Fl�che Glac�e
                if (lvl == 6)
                    perso.learnSpell(165, 1, true, false, false);//Fl�che enflamm�e
                if (lvl == 9)
                    perso.learnSpell(172, 1, true, false, false);//Tir Eloign�
                if (lvl == 13)
                    perso.learnSpell(167, 1, true, false, false);//Fl�che d'Expiation
                if (lvl == 17)
                    perso.learnSpell(168, 1, true, false, false);//Oeil de Taupe
                if (lvl == 21)
                    perso.learnSpell(162, 1, true, false, false);//Tir Critique
                if (lvl == 26)
                    perso.learnSpell(170, 1, true, false, false);//Fl�che d'Immobilisation
                if (lvl == 31)
                    perso.learnSpell(171, 1, true, false, false);//Fl�che Punitive
                if (lvl == 36)
                    perso.learnSpell(166, 1, true, false, false);//Tir Puissant
                if (lvl == 42)
                    perso.learnSpell(173, 1, true, false, false);//Fl�che Harcelante
                if (lvl == 48)
                    perso.learnSpell(174, 1, true, false, false);//Fl�che Cinglante
                if (lvl == 54)
                    perso.learnSpell(176, 1, true, false, false);//Fl�che Pers�cutrice
                if (lvl == 60)
                    perso.learnSpell(175, 1, true, false, false);//Fl�che Destructrice
                if (lvl == 70)
                    perso.learnSpell(178, 1, true, false, false);//Fl�che Absorbante
                if (lvl == 80)
                    perso.learnSpell(177, 1, true, false, false);//Fl�che Ralentissante
                if (lvl == 90)
                    perso.learnSpell(179, 1, true, false, false);//Fl�che Explosive
                if (lvl == 100)
                    perso.learnSpell(180, 1, true, false, false);//Ma�trise de l'Arc
                if (lvl == 200)
                    perso.learnSpell(1909, 1, true, false, false);//Invocation de Dopeul Cra
            }
            case CLASE_SADIDA -> {
                if (lvl == 3)
                    perso.learnSpell(198, 1, true, false, false);//Sacrifice Poupesque
                if (lvl == 6)
                    perso.learnSpell(195, 1, true, false, false);//Larme
                if (lvl == 9)
                    perso.learnSpell(182, 1, true, false, false);//Invocation de la Folle
                if (lvl == 13)
                    perso.learnSpell(192, 1, true, false, false);//Ronce Apaisante
                if (lvl == 17)
                    perso.learnSpell(197, 1, true, false, false);//Puissance Sylvestre
                if (lvl == 21)
                    perso.learnSpell(189, 1, true, false, false);//Invocation de la Sacrifi�e
                if (lvl == 26)
                    perso.learnSpell(181, 1, true, false, false);//Tremblement
                if (lvl == 31)
                    perso.learnSpell(199, 1, true, false, false);//Connaissance des Poup�es
                if (lvl == 36)
                    perso.learnSpell(191, 1, true, false, false);//Ronce Multiples
                if (lvl == 42)
                    perso.learnSpell(186, 1, true, false, false);//Arbre
                if (lvl == 48)
                    perso.learnSpell(196, 1, true, false, false);//Vent Empoisonn�
                if (lvl == 54)
                    perso.learnSpell(190, 1, true, false, false);//Invocation de la Gonflable
                if (lvl == 60)
                    perso.learnSpell(194, 1, true, false, false);//Ronces Agressives
                if (lvl == 70)
                    perso.learnSpell(185, 1, true, false, false);//Herbe Folle
                if (lvl == 80)
                    perso.learnSpell(184, 1, true, false, false);//Feu de Brousse
                if (lvl == 90)
                    perso.learnSpell(188, 1, true, false, false);//Ronce Insolente
                if (lvl == 100)
                    perso.learnSpell(187, 1, true, false, false);//Invocation de la Surpuissante
                if (lvl == 200)
                    perso.learnSpell(1910, 1, true, false, false);//Invocation de Dopeul Sadida
            }
            case CLASE_SACROGRITO -> {
                if (lvl == 3)
                    perso.learnSpell(444, 1, true, false, false);//D�robade
                if (lvl == 6)
                    perso.learnSpell(449, 1, true, false, false);//D�tour
                if (lvl == 9)
                    perso.learnSpell(436, 1, true, false, false);//Assaut
                if (lvl == 13)
                    perso.learnSpell(437, 1, true, false, false);//Ch�timent Agile
                if (lvl == 17)
                    perso.learnSpell(439, 1, true, false, false);//Dissolution
                if (lvl == 21)
                    perso.learnSpell(433, 1, true, false, false);//Ch�timent Os�
                if (lvl == 26)
                    perso.learnSpell(443, 1, true, false, false);//Ch�timent Spirituel
                if (lvl == 31)
                    perso.learnSpell(440, 1, true, false, false);//Sacrifice
                if (lvl == 36)
                    perso.learnSpell(442, 1, true, false, false);//Absorption
                if (lvl == 42)
                    perso.learnSpell(441, 1, true, false, false);//Ch�timent Vilatesque
                if (lvl == 48)
                    perso.learnSpell(445, 1, true, false, false);//Coop�ration
                if (lvl == 54)
                    perso.learnSpell(438, 1, true, false, false);//Transposition
                if (lvl == 60)
                    perso.learnSpell(446, 1, true, false, false);//Punition
                if (lvl == 70)
                    perso.learnSpell(447, 1, true, false, false);//Furie
                if (lvl == 80)
                    perso.learnSpell(448, 1, true, false, false);//Ep�e Volante
                if (lvl == 90)
                    perso.learnSpell(435, 1, true, false, false);//Tansfert de Vie
                if (lvl == 100)
                    perso.learnSpell(450, 1, true, false, false);//Folie Sanguinaire
                if (lvl == 200)
                    perso.learnSpell(1911, 1, true, false, false);//Invocation de Dopeul Sacrieur
            }
            case CLASE_PANDAWA -> {
                if (lvl == 3)
                    perso.learnSpell(689, 1, true, false, false);//Epouvante
                if (lvl == 6)
                    perso.learnSpell(690, 1, true, false, false);//Souffle Alcoolis�
                if (lvl == 9)
                    perso.learnSpell(691, 1, true, false, false);//Vuln�rabilit� Aqueuse
                if (lvl == 13)
                    perso.learnSpell(688, 1, true, false, false);//Vuln�rabilit� Incandescente
                if (lvl == 17)
                    perso.learnSpell(693, 1, true, false, false);//Karcham
                if (lvl == 21)
                    perso.learnSpell(694, 1, true, false, false);//Vuln�rabilit� Venteuse
                if (lvl == 26)
                    perso.learnSpell(695, 1, true, false, false);//Stabilisation
                if (lvl == 31)
                    perso.learnSpell(696, 1, true, false, false);//Chamrak
                if (lvl == 36)
                    perso.learnSpell(697, 1, true, false, false);//Vuln�rabilit� Terrestre
                if (lvl == 42)
                    perso.learnSpell(698, 1, true, false, false);//Souillure
                if (lvl == 48)
                    perso.learnSpell(699, 1, true, false, false);//Lait de Bambou
                if (lvl == 54)
                    perso.learnSpell(700, 1, true, false, false);//Vague � Lame
                if (lvl == 60)
                    perso.learnSpell(701, 1, true, false, false);//Col�re de Zato�shwan
                if (lvl == 70)
                    perso.learnSpell(702, 1, true, false, false);//Flasque Explosive
                if (lvl == 80)
                    perso.learnSpell(703, 1, true, false, false);//Pandatak
                if (lvl == 90)
                    perso.learnSpell(704, 1, true, false, false);//Pandanlku
                if (lvl == 100)
                    perso.learnSpell(705, 1, true, false, false);//Lien Spiritueux
                if (lvl == 200)
                    perso.learnSpell(1912, 1, true, false, false);//Invocation de Dopeul Pandawa
            }
        }
    }

    public static int getGlyphColor(int spell) {
        //case 476://Blop
        return switch (spell) {
//Dopeul
//Rouge
//Enflamm�
            case 10, 2033 -> 4;
//Dopeul
//Aveuglement
            case 12, 2034 -> 3;
//Dopeul
//Bleu
//Silence
            case 13, 2035 -> 6;
//Dopeul
//Vert
//Immobilisation
            case 15, 2036 -> 5;
//Dopeul
//Aggressif
            case 17, 2037 -> 2;
//Karkargo
//Blanc
            case 3500, 3501, 949 -> 0;
            default -> 4;
        };
    }

    public static int getTrapsColor(int spell) {
        return switch (spell) {
//Sournois
            case 65 -> 7;
//Immobilisation
            case 69 -> 10;
//Dopeul
//Empoisonn�e
            case 71, 2068 -> 9;
//Repulsif
            case 73 -> 12;
//Dopeul
//Silence
            case 77, 2071 -> 11;
//Dopeul
//Masse
            case 79, 2072 -> 8;
//Mortel
            case 80 -> 13;
            default -> 7;
        };
    }

    public static Caracteristicas getMountStats(int color, int lvl) {
        Caracteristicas stats = new Caracteristicas();
        switch (color) {
            //Amande sauvage
            case 1:
                break;
            //Ebene
            case 3:
                stats.addOneStat(STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(STATS_ADD_AGIL, (int) (lvl / 1.25));//100/1.25 = 80
                break;
            //Rousse |
            case 10:
                stats.addOneStat(STATS_ADD_VITA, lvl); //100/1 = 100
                break;
            //Amande
            case 20:
                stats.addOneStat(STATS_ADD_INIT, lvl * 10); // 100*10 = 1000
                break;
            //Dor�e
            case 18:
                stats.addOneStat(STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(STATS_ADD_SAGE, (int) (lvl / 2.50)); // 100/2.50 = 40
                break;
            //Rousse-Amande
            case 38:
                stats.addOneStat(STATS_ADD_INIT, lvl * 5); // 100*5 = 500
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_CREATURE, lvl / 50); // 100/50 = 2
                break;
            //Rousse-Dor�e
            case 46:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_SAGE, lvl / 4); //100/4 = 25
                break;
            //Amande-Dor�e
            case 33:
                stats.addOneStat(STATS_ADD_INIT, lvl * 5);
                stats.addOneStat(STATS_ADD_SAGE, lvl / 4);
                stats.addOneStat(STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(STATS_CREATURE, lvl / 100); // 100/100 = 1
                break;
            //Indigo |
            case 17:
                stats.addOneStat(STATS_ADD_CHAN, (int) (lvl / 1.25));
                stats.addOneStat(STATS_ADD_VITA, lvl / 2);
                break;
            //Rousse-Indigo
            case 62:
                stats.addOneStat(STATS_ADD_VITA, (int) (lvl * 1.50)); // 100*1.50 = 150
                stats.addOneStat(STATS_ADD_CHAN, (int) (lvl / 1.65));
                break;
            //Rousse-Eb�ne
            case 12:
                stats.addOneStat(STATS_ADD_VITA, (int) (lvl * 1.50));
                stats.addOneStat(STATS_ADD_AGIL, (int) (lvl / 1.65));
                break;
            //Amande-Indigo
            case 36:
                stats.addOneStat(STATS_ADD_INIT, lvl * 5);
                stats.addOneStat(STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(STATS_ADD_CHAN, (int) (lvl / 1.65));
                stats.addOneStat(STATS_CREATURE, lvl / 100);
                break;
            //Pourpre | Stade 4
            case 19:
                stats.addOneStat(STATS_ADD_FORC, (int) (lvl / 1.25));
                stats.addOneStat(STATS_ADD_VITA, lvl / 2);
                break;
            //Orchid�e
            case 22:
                stats.addOneStat(STATS_ADD_INTE, (int) (lvl / 1.25));
                stats.addOneStat(STATS_ADD_VITA, lvl / 2);
                break;
            //Dor�e-Orchid�e |
            case 48:
                stats.addOneStat(STATS_ADD_VITA, (lvl));
                stats.addOneStat(STATS_ADD_SAGE, lvl / 4);
                stats.addOneStat(STATS_ADD_INTE, (int) (lvl / 1.65));
                break;
            //Indigo-Pourpre
            case 65:
                stats.addOneStat(STATS_ADD_VITA, (lvl));
                stats.addOneStat(STATS_ADD_CHAN, lvl / 2);
                stats.addOneStat(STATS_ADD_FORC, lvl / 2);
                break;
            //Ivoire-Orchid�e
            case 67:
                stats.addOneStat(STATS_ADD_VITA, (lvl));
                stats.addOneStat(STATS_ADD_PERDOM, lvl / 2);
                stats.addOneStat(STATS_ADD_INTE, lvl / 2);
                break;
            //Eb�ne-Pourpre
            case 54:
                stats.addOneStat(STATS_ADD_VITA, (lvl));
                stats.addOneStat(STATS_ADD_FORC, lvl / 2);
                stats.addOneStat(STATS_ADD_AGIL, lvl / 2);
                break;
            //Eb�ne-Orchid�e
            case 53:
                stats.addOneStat(STATS_ADD_VITA, (lvl));
                stats.addOneStat(STATS_ADD_AGIL, lvl / 2);
                stats.addOneStat(STATS_ADD_INTE, lvl / 2);
                break;
            //Pourpre-Orchid�e
            case 76:
                stats.addOneStat(STATS_ADD_VITA, (lvl));
                stats.addOneStat(STATS_ADD_INTE, lvl / 2);
                stats.addOneStat(STATS_ADD_FORC, lvl / 2);
                break;
            // Amande-Ebene	| Nami-start
            case 37:
                stats.addOneStat(STATS_ADD_INIT, lvl * 5);
                stats.addOneStat(STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(STATS_ADD_AGIL, (int) (lvl / 1.65));
                stats.addOneStat(STATS_CREATURE, lvl / 100);
                break;
            // Amande-Rousse
            case 44:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_SAGE, lvl / 4);
                stats.addOneStat(STATS_ADD_CHAN, (int) (lvl / 1.65));
                break;
            // Dor�e-Eb�ne
            case 42:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_SAGE, lvl / 4);
                stats.addOneStat(STATS_ADD_AGIL, (int) (lvl / 1.65));
                break;
            // Indigo-Eb�ne
            case 51:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_CHAN, lvl / 2);
                stats.addOneStat(STATS_ADD_AGIL, lvl / 2);
                break;
            // Rousse-Pourpre
            case 71:
                stats.addOneStat(STATS_ADD_VITA, (int) (lvl * 1.5));
                stats.addOneStat(STATS_ADD_FORC, (int) (lvl / 1.65));
                break;
            // Rousse-Orchid�e
            case 70:
                stats.addOneStat(STATS_ADD_VITA, (int) (lvl * 1.5));
                stats.addOneStat(STATS_ADD_INTE, (int) (lvl / 1.65));
                break;
            // Amande-Pourpre
            case 41:
                stats.addOneStat(STATS_ADD_INIT, lvl * 5);
                stats.addOneStat(STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(STATS_ADD_FORC, (int) (lvl / 1.65));
                stats.addOneStat(STATS_CREATURE, lvl / 100);
                break;
            // Amande-Orchid�e
            case 40:
                stats.addOneStat(STATS_ADD_INIT, lvl * 5);
                stats.addOneStat(STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(STATS_ADD_INTE, (int) (lvl / 1.65));
                stats.addOneStat(STATS_CREATURE, lvl / 100);
                break;
            // Dor�e-Pourpre
            case 49:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_SAGE, lvl / 4);
                stats.addOneStat(STATS_ADD_FORC, (int) (lvl / 1.65));
                break;
            // Ivoire
            case 16:
                stats.addOneStat(STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(STATS_ADD_PERDOM, lvl / 2);
                break;
            // Turquoise
            case 15:
                stats.addOneStat(STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(STATS_ADD_PROS, (int) (lvl / 1.25));
                break;
            //Rousse-Ivoire
            case 11:
                stats.addOneStat(STATS_ADD_VITA, lvl * 2); // 100*2 = 200
                stats.addOneStat(STATS_ADD_PERDOM, (int) (lvl / 2.5)); // = 40
                break;
            //Rousse-Turquoise
            case 69:
                stats.addOneStat(STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(STATS_ADD_PROS, (int) (lvl / 2.50));
                break;
            //Amande-Turquoise
            case 39:
                stats.addOneStat(STATS_ADD_INIT, lvl * 5);
                stats.addOneStat(STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(STATS_ADD_PROS, (int) (lvl / 2.50));
                stats.addOneStat(STATS_CREATURE, lvl / 100);
                break;
            //Dor�e-Ivoire
            case 45:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_PERDOM, (int) (lvl / 2.5));
                stats.addOneStat(STATS_ADD_SAGE, lvl / 4);
                break;
            //Dor�e-Turquoise
            case 47:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_PROS, (int) (lvl / 2.50));
                stats.addOneStat(STATS_ADD_SAGE, lvl / 4);
                break;
            //Indigo-Ivoire
            case 61:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_CHAN, (int) (lvl / 2.50));
                stats.addOneStat(STATS_ADD_PERDOM, (int) (lvl / 2.5));
                break;
            //Indigo-Turquoise
            case 63:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_CHAN, (int) (lvl / 1.65));
                stats.addOneStat(STATS_ADD_PROS, (int) (lvl / 2.5));
                break;
            //Eb�ne-Ivoire
            case 9:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_AGIL, (int) (lvl / 2.50));
                stats.addOneStat(STATS_ADD_PERDOM, (int) (lvl / 2.5));
                break;
            //Eb�ne-Turquoise
            case 52:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_AGIL, (int) (lvl / 1.65));
                stats.addOneStat(STATS_ADD_PROS, (int) (lvl / 2.50));
                break;
            //Pourpre-Ivoire
            case 68:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_FORC, (int) (lvl / 1.65));
                stats.addOneStat(STATS_ADD_PERDOM, (int) (lvl / 2.5));
                break;
            //Pourpre-Turquoise
            case 73:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_FORC, (int) (lvl / 1.65));
                stats.addOneStat(STATS_ADD_PROS, (int) (lvl / 2.50));
                break;
            //Orchid�e-Turquoise
            case 72:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_INTE, (int) (lvl / 1.65));
                stats.addOneStat(STATS_ADD_PROS, (int) (lvl / 2.5));
                break;
            //Ivoire-Turquoise
            case 66:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_PERDOM, (int) (lvl / 2.5));
                stats.addOneStat(STATS_ADD_PROS, (int) (lvl / 2.50));
                break;
            // Emeraude
            case 21:
                stats.addOneStat(STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(STATS_ADD_PM, lvl / 100);
                break;
            // Prune
            case 23:
                stats.addOneStat(STATS_ADD_VITA, lvl * 2); // 100*2 = 200
                stats.addOneStat(STATS_ADD_PO, lvl / 50);
                break;
            //Emeraude-Rousse
            case 57:
                stats.addOneStat(STATS_ADD_VITA, lvl * 3); // 100*3 = 300
                stats.addOneStat(STATS_ADD_PM, lvl / 100);
                break;
            //Rousse-Prune
            case 84:
                stats.addOneStat(STATS_ADD_VITA, lvl * 3);
                stats.addOneStat(STATS_ADD_PO, lvl / 100);
                break;
            //Amande-Emeraude
            case 35:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_PM, lvl / 100);
                stats.addOneStat(STATS_CREATURE, lvl / 100);
                stats.addOneStat(STATS_ADD_INIT, lvl * 5);
                break;
            //Amande-Prune
            case 77:
                stats.addOneStat(STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(STATS_ADD_INIT, lvl * 5);
                stats.addOneStat(STATS_ADD_PO, lvl / 100);
                stats.addOneStat(STATS_CREATURE, lvl / 100);
                break;
            //Dor�e-Emeraude
            case 43:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_SAGE, lvl / 4);
                stats.addOneStat(STATS_ADD_PM, lvl / 100);
                break;
            //Dor�e-Prune
            case 78:
                stats.addOneStat(STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(STATS_ADD_SAGE, lvl / 4);
                stats.addOneStat(STATS_ADD_PO, lvl / 100);
                break;
            //Indigo-Emeraude
            case 55:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_CHAN, (int) (lvl / 3.33));
                stats.addOneStat(STATS_ADD_PM, lvl / 100);
                break;
            //Indigo-Prune
            case 82:
                stats.addOneStat(STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(STATS_ADD_CHAN, (int) (lvl / 1.65));
                stats.addOneStat(STATS_ADD_PO, lvl / 100);
                break;
            //Eb�ne-Emeraude
            case 50:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_AGIL, (int) (lvl / 3.33));
                stats.addOneStat(STATS_ADD_PM, lvl / 100);
                break;
            //Eb�ne-Prune
            case 79:
                stats.addOneStat(STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(STATS_ADD_AGIL, (int) (lvl / 1.65));
                stats.addOneStat(STATS_ADD_PO, lvl / 100);
                break;
            //Pourpre-Emeraude
            case 60:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_FORC, (int) (lvl / 3.33));
                stats.addOneStat(STATS_ADD_PM, lvl / 100);
                break;
            //Pourpre-Prune
            case 87:
                stats.addOneStat(STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(STATS_ADD_FORC, (int) (lvl / 1.65));
                stats.addOneStat(STATS_ADD_PO, lvl / 100);
                break;
            //Orchid�e-Emeraude
            case 59:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_INTE, (int) (lvl / 3.33));
                stats.addOneStat(STATS_ADD_PM, lvl / 100);
                break;
            //Orchid�e-Prune
            case 86:
                stats.addOneStat(STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(STATS_ADD_INTE, (int) (lvl / 1.65));
                stats.addOneStat(STATS_ADD_PO, lvl / 100);
                break;
            //Ivoire-Emeraude
            case 56:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_PERDOM, (int) (lvl / 3.33));
                stats.addOneStat(STATS_ADD_PM, lvl / 100);
                break;
            //Ivoire-Prune
            case 83:
                stats.addOneStat(STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(STATS_ADD_PERDOM, (int) (lvl / 1.65));
                stats.addOneStat(STATS_ADD_PO, lvl / 100);
                break;
            //Turquoise-Emeraude
            case 58:
                stats.addOneStat(STATS_ADD_VITA, lvl);
                stats.addOneStat(STATS_ADD_PROS, (int) (lvl / 3.33));
                stats.addOneStat(STATS_ADD_PM, lvl / 100);
                break;
            //Turquoise-Prune
            case 85:
                stats.addOneStat(STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(STATS_ADD_PROS, (int) (lvl / 1.65));
                stats.addOneStat(STATS_ADD_PO, lvl / 100);
                break;
            //Emeraude-Prune
            case 80:
                stats.addOneStat(STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(STATS_ADD_PM, lvl / 100);
                stats.addOneStat(STATS_ADD_PO, lvl / 100);
                break;
            //Armure
            case 88:
            case 75:
                stats.addOneStat(STATS_ADD_PERDOM, lvl / 2);
                stats.addOneStat(STATS_ADD_RP_AIR, lvl / 20);
                stats.addOneStat(STATS_ADD_RP_EAU, lvl / 20);
                stats.addOneStat(STATS_ADD_RP_TER, lvl / 20);
                stats.addOneStat(STATS_ADD_RP_FEU, lvl / 20);
                stats.addOneStat(STATS_ADD_RP_NEU, lvl / 20);
                break;
        }
        return stats;
    }

    public static ObjetoModelo getParchoTemplateByMountColor(int color) {
        //Ammande sauvage
        //Ebene | Page 1
        //Rousse sauvage
        //Ebene-ivoire
        //Rousse
        //Ivoire-Rousse
        //Ebene-rousse
        //Turquoise
        //Ivoire
        //Indigo
        //Dor�e
        //Pourpre
        //Amande
        //Emeraude
        //Orchid�e
        //Prune
        //Amande-Dor�e
        //Amande-Ebene
        //Amande-Emeraude
        //Amande-Indigo
        //Amande-Ivoire
        //Amande-Rousse
        //Amande-Turquoise
        //Amande-Orchid�e
        //Amande-Pourpre
        //Dor�e-Eb�ne
        //Dor�e-Emeraude
        //Dor�e-Indigo
        //Dor�e-Ivoire
        //Dor�e-Rousse | Page 2
        //Dor�e-Turquoise
        //Dor�e-Orchid�e
        //Dor�e-Pourpre
        //Eb�ne-Emeraude
        //Eb�ne-Indigo
        //Eb�ne-Turquoise
        //Eb�ne-Orchid�e
        //Eb�ne-Pourpre
        //Emeraude-Indigo
        //Emeraude-Ivoire
        //Emeraude-Rousse
        //Emeraude-Turquoise
        //Emeraude-Orchid�e
        //Emeraude-Pourpre
        //Indigo-Ivoire
        //Indigo-Rousse
        //Indigo-Turquoise
        //Indigo-Orchid�e
        //Indigo-Pourpre
        //Ivoire-Turquoise
        //Ivoire-Ochid�e
        //Ivoire-Pourpre
        //Turquoise-Rousse
        //Ochid�e-Rousse
        //Pourpre-Rousse
        //Turquoise-Orchid�e
        //Turquoise-Pourpre
        //Dor�e sauvage
        //Squelette
        //Orchid�e-Pourpre
        //Prune-Amande
        //Prune-Dor�e
        //Prune-Eb�ne
        //Prune-Emeraude
        //Prune et Indigo
        //Prune-Ivoire
        //Prune-Rousse
        //Prune-Turquoise
        //Prune-Orchid�e
        //Prune-Pourpre
        //Armure
        return switch (color) {
            case 2 -> Mundo.mundo.getObjetoModelo(7807);
            case 3 -> Mundo.mundo.getObjetoModelo(7808);
            case 4 -> Mundo.mundo.getObjetoModelo(7809);
            case 9 -> Mundo.mundo.getObjetoModelo(7810);
            case 10 -> Mundo.mundo.getObjetoModelo(7811);
            case 11 -> Mundo.mundo.getObjetoModelo(7812);
            case 12 -> Mundo.mundo.getObjetoModelo(7813);
            case 15 -> Mundo.mundo.getObjetoModelo(7814);
            case 16 -> Mundo.mundo.getObjetoModelo(7815);
            case 17 -> Mundo.mundo.getObjetoModelo(7816);
            case 18 -> Mundo.mundo.getObjetoModelo(7817);
            case 19 -> Mundo.mundo.getObjetoModelo(7818);
            case 20 -> Mundo.mundo.getObjetoModelo(7819);
            case 21 -> Mundo.mundo.getObjetoModelo(7820);
            case 22 -> Mundo.mundo.getObjetoModelo(7821);
            case 23 -> Mundo.mundo.getObjetoModelo(7822);
            case 33 -> Mundo.mundo.getObjetoModelo(7823);
            case 34 -> Mundo.mundo.getObjetoModelo(7824);
            case 35 -> Mundo.mundo.getObjetoModelo(7825);
            case 36 -> Mundo.mundo.getObjetoModelo(7826);
            case 37 -> Mundo.mundo.getObjetoModelo(7827);
            case 38 -> Mundo.mundo.getObjetoModelo(7828);
            case 39 -> Mundo.mundo.getObjetoModelo(7829);
            case 40 -> Mundo.mundo.getObjetoModelo(7830);
            case 41 -> Mundo.mundo.getObjetoModelo(7831);
            case 42 -> Mundo.mundo.getObjetoModelo(7832);
            case 43 -> Mundo.mundo.getObjetoModelo(7833);
            case 44 -> Mundo.mundo.getObjetoModelo(7834);
            case 45 -> Mundo.mundo.getObjetoModelo(7835);
            case 46 -> Mundo.mundo.getObjetoModelo(7836);
            case 47 -> Mundo.mundo.getObjetoModelo(7837);
            case 48 -> Mundo.mundo.getObjetoModelo(7838);
            case 49 -> Mundo.mundo.getObjetoModelo(7839);
            case 50 -> Mundo.mundo.getObjetoModelo(7840);
            case 51 -> Mundo.mundo.getObjetoModelo(7841);
            case 52 -> Mundo.mundo.getObjetoModelo(7842);
            case 53 -> Mundo.mundo.getObjetoModelo(7843);
            case 54 -> Mundo.mundo.getObjetoModelo(7844);
            case 55 -> Mundo.mundo.getObjetoModelo(7845);
            case 56 -> Mundo.mundo.getObjetoModelo(7846);
            case 57 -> Mundo.mundo.getObjetoModelo(7847);
            case 58 -> Mundo.mundo.getObjetoModelo(7848);
            case 59 -> Mundo.mundo.getObjetoModelo(7849);
            case 60 -> Mundo.mundo.getObjetoModelo(7850);
            case 61 -> Mundo.mundo.getObjetoModelo(7851);
            case 62 -> Mundo.mundo.getObjetoModelo(7852);
            case 63 -> Mundo.mundo.getObjetoModelo(7853);
            case 64 -> Mundo.mundo.getObjetoModelo(7854);
            case 65 -> Mundo.mundo.getObjetoModelo(7855);
            case 66 -> Mundo.mundo.getObjetoModelo(7856);
            case 67 -> Mundo.mundo.getObjetoModelo(7857);
            case 68 -> Mundo.mundo.getObjetoModelo(7858);
            case 69 -> Mundo.mundo.getObjetoModelo(7859);
            case 70 -> Mundo.mundo.getObjetoModelo(7860);
            case 71 -> Mundo.mundo.getObjetoModelo(7861);
            case 72 -> Mundo.mundo.getObjetoModelo(7862);
            case 73 -> Mundo.mundo.getObjetoModelo(7863);
            case 74 -> Mundo.mundo.getObjetoModelo(7864);
            case 75 -> Mundo.mundo.getObjetoModelo(7865);
            case 76 -> Mundo.mundo.getObjetoModelo(7866);
            case 77 -> Mundo.mundo.getObjetoModelo(7867);
            case 78 -> Mundo.mundo.getObjetoModelo(7868);
            case 79 -> Mundo.mundo.getObjetoModelo(7869);
            case 80 -> Mundo.mundo.getObjetoModelo(7870);
            case 82 -> Mundo.mundo.getObjetoModelo(7871);
            case 83 -> Mundo.mundo.getObjetoModelo(7872);
            case 84 -> Mundo.mundo.getObjetoModelo(7873);
            case 85 -> Mundo.mundo.getObjetoModelo(7874);
            case 86 -> Mundo.mundo.getObjetoModelo(7875);
            case 87 -> Mundo.mundo.getObjetoModelo(7876);
            case 88 -> Mundo.mundo.getObjetoModelo(9582);
            default -> null;
        };
    }

    public static int getMountColorByParchoTemplate(int tID) {
        for (int a = 1; a < 100; a++)
            if (getParchoTemplateByMountColor(a) != null)
                if (Objects.requireNonNull(getParchoTemplateByMountColor(a)).getId() == tID)
                    return a;
        return -1;
    }

    public static boolean isValidPlaceForItem(ObjetoModelo template, int place) {
        if (template.getType() == 41 && place == ITEM_POS_DRAGODINDE)
            return true;

        switch (template.getType()) {
            case ITEM_TYPE_AMULETTE:
                if (place == ITEM_POS_AMULETTE)
                    return true;
                break;
            case 113:
                if ((template.getId() == 9233) && (place == 7))
                    return true;
                if ((template.getId() == 9234) && (place == 6))
                    return true;
                if ((template.getId() == 9255) && (place == 0))
                    return true;
                if ((template.getId() == 9256)
                        && ((place == 2) || (place == 4)))
                    return true;
                break;
            case 114: // tourmenteurs
                if (place == 1) // CaC
                    return true;
                break;
            case ITEM_TYPE_ARC:
            case ITEM_TYPE_BAGUETTE:
            case ITEM_TYPE_BATON:
            case ITEM_TYPE_DAGUES:
            case ITEM_TYPE_EPEE:
            case ITEM_TYPE_MARTEAU:
            case ITEM_TYPE_PELLE:
            case ITEM_TYPE_HACHE:
            case ITEM_TYPE_OUTIL:
            case ITEM_TYPE_PIOCHE:
            case ITEM_TYPE_FAUX:
            case ITEM_TYPE_PIERRE_AME:
            case ITEM_TYPE_FILET_CAPTURE:
                if (place == ITEM_POS_ARME)
                    return true;
                break;

            case ITEM_TYPE_ANNEAU:
                if (place == ITEM_POS_ANNEAU1 || place == ITEM_POS_ANNEAU2)
                    return true;
                break;

            case ITEM_TYPE_CEINTURE:
                if (place == ITEM_POS_CEINTURE)
                    return true;
                break;

            case ITEM_TYPE_BOTTES:
                if (place == ITEM_POS_BOTTES)
                    return true;
                break;

            case ITEM_TYPE_COIFFE:
                if (place == ITEM_POS_COIFFE)
                    return true;
                break;

            case ITEM_TYPE_CAPE:
            case ITEM_TYPE_SAC_DOS:
                if (place == ITEM_POS_CAPE)
                    return true;
                break;

            case ITEM_TYPE_FAMILIER:
                if (place == ITEM_POS_FAMILIER)
                    return true;
                break;

            case ITEM_TYPE_DOFUS:
                if (place == ITEM_POS_DOFUS1 || place == ITEM_POS_DOFUS2
                        || place == ITEM_POS_DOFUS3 || place == ITEM_POS_DOFUS4
                        || place == ITEM_POS_DOFUS5 || place == ITEM_POS_DOFUS6)
                    return true;
                break;

            case ITEM_TYPE_BOUCLIER:
                if (place == ITEM_POS_BOUCLIER)
                    return true;
                break;

            //Barre d'objets : Normalement le client bloque les items interdits
            case ITEM_TYPE_POTION:
            case ITEM_TYPE_PARCHO_EXP:
            case ITEM_TYPE_BOOST_FOOD:
            case ITEM_TYPE_PAIN:
            case ITEM_TYPE_BIERE:
            case ITEM_TYPE_POISSON:
            case ITEM_TYPE_BONBON:
            case ITEM_TYPE_COMESTI_POISSON:
            case ITEM_TYPE_VIANDE:
            case ITEM_TYPE_VIANDE_CONSERVEE:
            case ITEM_TYPE_VIANDE_COMESTIBLE:
            case ITEM_TYPE_TEINTURE:
            case ITEM_TYPE_MAITRISE:
            case ITEM_TYPE_BOISSON:
            case ITEM_TYPE_PIERRE_AME_PLEINE:
            case ITEM_TYPE_PARCHO_RECHERCHE:
            case ITEM_TYPE_CADEAUX:
            case ITEM_TYPE_OBJET_ELEVAGE:
            case ITEM_TYPE_OBJET_UTILISABLE:
            case ITEM_TYPE_PRISME:
            case ITEM_TYPE_FEE_ARTIFICE:
            case ITEM_TYPE_DONS:
                if (place >= 35 && place <= 48)
                    return true;
                break;
        }
        return false;
    }

	/*
     * public static boolean feedMount(int type) { for (Integer feed :
	 * Main.itemFeedMount) { if (type == feed) return true; } return false; }
	 */

    public static void tpCim(int idArea, Jugador perso) {
        switch (idArea) {
            case 45:
                perso.teleport((short) 10342, 222);
                break;

            case 0:
            case 5:
            case 29:
            case 39:
            case 40:
            case 43:
            case 44:
                perso.teleport((short) 1174, 279);
                break;

            case 3:
            case 4:
            case 6:
            case 18:
            case 25:
            case 27:
            case 41:

            case 42:
                perso.teleport((short) 8534, 196);
                break;

            case 2:
                perso.teleport((short) 420, 408);
                break;

            case 1:
                perso.teleport((short) 844, 370);
                break;

            case 7:
                perso.teleport((short) 4285, 572);
                break;

            case 8:
            case 14:
            case 15:
            case 16:
            case 32:
                perso.teleport((short) 4748, 133);
                break;

            case 11:
            case 12:
            case 13:
            case 33:
                perso.teleport((short) 5719, 196);
                break;

            case 19:
            case 22:
            case 23:
                perso.teleport((short) 7910, 381);
                break;

            case 20:
            case 21:
            case 24:
                perso.teleport((short) 8054, 115);
                break;

            case 28:
            case 34:
            case 35:
            case 36:
                perso.teleport((short) 9231, 257);
                break;

            case 30:
                perso.teleport((short) 9539, 128);
                break;

            case 31:
                if (perso.isGhost())
                    perso.teleport((short) 9558, 268);
                else
                    perso.teleport((short) 9558, 224);
                break;

            case 37:
                perso.teleport((short) 7796, 433);
                break;

            case 46:
                perso.teleport((short) 10422, 327);
                break;
            case 47:
                perso.teleport((short) 10590, 302);
                break;

            case 26:
                perso.teleport((short) 9398, 268);

            default:
                perso.teleport((short) 8534, 196);
                break;
        }
    }

    public static boolean isTaverne(Mapa map) {
        return switch (map.getId()) {
            case 7573, 7572, 7574, 465, 463, 6064, 461, 462, 5867, 6197, 6021, 6044, 8196, 6055, 8195, 1905, 1907, 6049 -> true;
            default -> false;
        };
    }

    public static int getLevelForChevalier(Jugador target) {
        int lvl = target.getLevel();
        if (lvl <= 50)
            return 50;
        if ((lvl <= 80) && (lvl > 50))
            return 80;
        if ((lvl <= 110) && (lvl > 80))
            return 110;
        if ((lvl <= 140) && (lvl > 110))
            return 140;
        if ((lvl <= 170) && (lvl > 140))
            return 170;
        if ((lvl <= 500) && (lvl > 170))
            return 200;
        return 200;
    }

    public static String getStatsOfCandy(int id, int turn) {
        String a = Mundo.mundo.getObjetoModelo(id).getStrTemplate();
        a += ",32b#64#0#" + Integer.toHexString(turn) + "#0d0+1;";
        return a;
    }

    public static String getStatsOfMascotte() {
        String a = Integer.toHexString(148) + "#0#0#0#0d0+1,";
        a += "32b#64#0#" + Integer.toHexString(1) + "#0d0+1;";
        return a;
    }


    public static String getStringColorDragodinde(int color) {
        return switch (color) {
// Dragodinde Amande Sauvage
            case 1 -> "16772045,-1,16772045";
// Dragodinde Ebène
            case 3 -> "1245184,393216,1245184";
// Dragodinde Rousse Sauvage
            case 6 -> "16747520,-1,16747520";
// Dragodinde Ebène et Ivoire
            case 9 -> "1182992,16777200,16777200";
// Dragodinde Rousse
            case 10 -> "16747520,-1,16747520";
// Dragodinde Ivoire et Rousse
            case 11 -> "16747520,16777200,16777200";
// Dragodinde Ebène et Rousse
            case 12 -> "16747520,1703936,1774084";
// Dragodinde Turquoise
            case 15 -> "4251856,-1,4251856";
// Dragodinde Ivoire
            case 16 -> "16777200,16777200,16777200";
// Dragodinde Indigo
            case 17 -> "4915330,-1,4915330";
// Dragodinde Dorée
            case 18 -> "16766720,16766720,16766720";
// Dragodinde Pourpre
            case 19 -> "14423100,-1,14423100";
// Dragodinde Amande
            case 20 -> "16772045,-1,16772045";
// Dragodinde Emeraude
            case 21 -> "3329330,-1,3329330";
// Dragodinde Orchidée
            case 22 -> "15859954,16777200,15859954";
// Dragodinde Prune
            case 23 -> "14524637,-1,14524637";
// Dragodinde Amande et Dorée
            case 33 -> "16772045,16766720,16766720";
// Dragodinde Amande et Ebène
            case 34 -> "16772045,1245184,1245184";
// Dragodinde Amande et Emeraude
            case 35 -> "16772045,3329330,3329330";
// Dragodinde Amande et Indigo
            case 36 -> "16772045,4915330,4915330";
// Dragodinde Amande et Ivoire
            case 37 -> "16772045,16777200,16777200";
// Dragodinde Amande et Rousse
            case 38 -> "16772045,16747520,16747520";
// Dragodinde Amande et Turquoise
            case 39 -> "16772045,4251856,4251856";
// Dragodinde Amande et Orchidée
            case 40 -> "16772045,15859954,15859954";
// Dragodinde Amande et Pourpre
            case 41 -> "16772045,14423100,14423100";
// Dragodinde Dorée et Ebène
            case 42 -> "1245184,16766720,16766720";
// Dragodinde Dorée et Emeraude
            case 43 -> "16766720,3329330,3329330";
// Dragodinde Dorée et Indigo
            case 44 -> "16766720,4915330,4915330";
// Dragodinde Dorée et Ivoire
            case 45 -> "16766720,16777200,16777200";
// Dragodinde Dorée et Rousse
            case 46 -> "16766720,16747520,16747520";
// Dragodinde Dorée et Turquoise
            case 47 -> "16766720,4251856,4251856";
// Dragodinde Dorée et Orchidée
            case 48 -> "16766720,15859954,15859954";
// Dragodinde Dorée et Pourpre
            case 49 -> "16766720,14423100,14423100";
// Dragodinde Ebène et Emeraude
            case 50 -> "1245184,3329330,3329330";
// Dragodinde Ebène et Indigo
            case 51 -> "4915330,4915330,1245184";
// Dragodinde Ebène et Turquoise
            case 52 -> "1245184,4251856,4251856";
// Dragodinde Ebène et Orchidée
            case 53 -> "15859954,0,0";
// Dragodinde Ebène et Pourpre
            case 54 -> "14423100,14423100,1245184";
// Dragodinde Emeraude et Indigo
            case 55 -> "3329330,4915330,4915330";
// Dragodinde Emeraude et Ivoire
            case 56 -> "3329330,16777200,16777200";
// Dragodinde Emeraude et Rousse
            case 57 -> "3329330,16747520,16747520";
// Dragodinde Emeraude et Turquoise
            case 58 -> "3329330,4251856,4251856";
// Dragodinde Emeraude et Orchidée
            case 59 -> "3329330,15859954,15859954";
// Dragodinde Emeraude et Pourpre
            case 60 -> "3329330,14423100,14423100";
// Dragodinde Indigo et Ivoire
            case 61 -> "4915330,16777200,16777200";
// Dragodinde Indigo et Rousse
            case 62 -> "4915330,16747520,16747520";
// Dragodinde Indigo et Turquoise
            case 63 -> "4915330,4251856,4251856";
// Dragodinde Indigo et Orchidée
            case 64 -> "4915330,15859954,15859954";
// Dragodinde Indigo et Pourpre
            case 65 -> "14423100,4915330,4915330";
// Dragodinde Ivoire et Turquoise
            case 66 -> "16777200,4251856,4251856";
// Dragodinde Ivoire et Orchidée
            case 67 -> "16777200,16731355,16711910";
// Dragodinde Ivoire et Pourpre
            case 68 -> "14423100,16777200,16777200";
// Dragodinde Ivoire et Rousse
            case 69 -> "4251856,16747520,16747520";
// Dragodinde Orchidée et Rousse
            case 70 -> "14315734,16747520,16747520";
// Dragodinde Pourpre et Rousse
            case 71 -> "14423100,16747520,16747520";
// Dragodinde Turquoise et Orchidée
            case 72 -> "15859954,4251856,4251856";
// Dragodinde Turquoise et Pourpre
            case 73 -> "14423100,4251856,4251856";
// Dragodinde Dorée et Rousse
            case 74 -> "16766720,16766720,16766720";
// Dragodinde Orchidée et Pourpre
            case 76 -> "14315734,14423100,14423100";
// Dragodinde Prune et Amande
            case 77 -> "14524637,16772045,16772045";
// Dragodinde Prune et Dorée
            case 78 -> "14524637,16766720,16766720";
// Dragodinde Prune et Ebène
            case 79 -> "14524637,1245184,1245184";
// Dragodinde Prune et Emeraude
            case 80 -> "14524637,3329330,3329330";
// Dragodinde Prune et Indigo
            case 82 -> "14524637,4915330,4915330";
// Dragodinde Prune et Ivoire
            case 83 -> "14524637,16777200,16777200";
// Dragodinde Prune et Rousse
            case 84 -> "14524637,16747520,16747520";
// Dragodinde Prune et Turquoise
            case 85 -> "14524637,4251856,4251856";
// Dragodinde Prune et Orchidée
            case 86 -> "14524637,15859954,15859954";
// Dragodinde Prune et Pourpre
            case 87 -> "14524637,14423100,14423100";
            default -> "-1,-1,-1";
        };
    }

    public static int getGeneration(int color) {
        return switch (color) {
// Amande
// Dorée
// Rousse
            case 10, 18, 20 -> 1;
// Dorée - Rousse
// Amande - Rousse
// Amande - Dorée
            case 33, 38, 46 -> 2;
// Indigo
// Ebène
            case 3, 17 -> 3;
// Ebène - Indigo
// Dorée - Ebène
// Dorée - Indigo
// Amande - Ebène
// Amande - Indigo
// Ebène - Rousse
// Indigo - Rousse
            case 62, 12, 36, 34, 44, 42, 51 -> 4;
// Orchidée
// Purpre
            case 19, 22 -> 5;
// Orchidée - Purpre
// Ebène - Orchidée
// Ebène - Purpre
// Indigo - Orchidée
// Indigo - Purpre
// Dorée - Orchidée
// Dorée - Purpre
// Amande - Orchidée
// Amande - Purpre
// Orchidée - Rousse
// Purpre - Rousse
            case 71, 70, 41, 40, 49, 48, 65, 64, 54, 53, 76 -> 6;
// Ivoire
// Turquoise
            case 15, 16 -> 7;
// Ivoire - Turquoise
// Orchidée - Turquoise
// Ivoire - Orchidée
// Turquoise - Purpre
// Ivoire - Purpre
// Ebène - Turquoise
// Ebène - Ivoire
// Indigo - Turquoise
// Indigo - Ivoire
// Dorée - Turquoise
// Dorée - Ivoire
// Amande - Turquoise
// Amande - Ivoire
// Turquoise - Rousse
// Ivoire - Rousse
            case 11, 69, 37, 39, 45, 47, 61, 63, 9, 52, 68, 73, 67, 72, 66 -> 8;
// Prune
// Emeraude
            case 21, 23 -> 9;
// Prune - Orchidée
// Prune - Turquoise
// Prune - Rousse
// Prune - Ivoire
// Prune - Indigo
// Prune - Emeraude
// Prune - Ebène
// Prune - Dorée
// Prune - Amande
// Emeraude - Purpre
// Emeraude - Orchidée
// Emeraude - Turquoise
// Emeraude - Ivoire
// Emeraude - Indigo
// Ebène - Emeraude
// Dorée - Emeraude
// Amande - Emeraude
// Emeraude - Rousse
            case 57, 35, 43, 50, 55, 56, 58, 59, 60, 77, 78, 79, 80, 82, 83, 84, 85, 86 -> 10;
            default -> 1;
        };
    }

    public static int colorToEtable(Jugador player, Montura mother, Montura father) {
        int color1, color2;
        int A = 0, B = 0, C = 0;

        String[] splitM = mother.getAncestors().split(","), splitF = father.getAncestors().split(",");
        CaracteristicasRandom<Integer> random = new CaracteristicasRandom<>();

        short i = 0;
        for(String str : splitM) {
            i++;
            if(str.equals("?")) continue;

            int pct = switch (i) {
                case 1, 2 -> 25;
                case 3, 4, 5, 6 -> 10;
                default -> 1;
            };

            random.add(pct, Integer.parseInt(str));
        }

        random.add(random.size() == 0 ? 100 : 33, mother.getColor());
        color1 = random.get();

        random = new CaracteristicasRandom<>();
        i = 0;
        for(String str : splitF) {
            i++;
            if(str.equals("?")) continue;

            int pct = switch (i) {
                case 1, 2 -> 25;
                case 3, 4, 5, 6 -> 10;
                default -> 1;
            };

            random.add(pct, Integer.parseInt(str));
        }

        random.add(random.size() == 0 ? 100 : 33, father.getColor());
        color2 = random.get();

        if(color1 == 75)
            color1 = 10;
        if(color2 == 75)
            color2 = 10;

        if (color1 > color2) {
            A = color2;// moins
            B = color1;// supérieur
        } else if (color1 <= color2) {
            A = color1;// moins
            B = color2;// supérieur
        }
        if (A == 10 && B == 18)
            C = 46; // Rousse y Dorée
        else if (A == 10 && B == 20)
            C = 38; // Rousse y Amande
        else if (A == 18 && B == 20)
            C = 33; // Amande y Dorée
        else if (A == 33 && B == 38)
            C = 17; // Indigo
        else if (A == 33 && B == 46)
            C = 3;// Ebène
        else if (A == 10 && B == 17)
            C = 62; // Rousse e Indigo
        else if (A == 10 && B == 3)
            C = 12; // Ebène y Rousse
        else if (A == 17 && B == 20)
            C = 36; // Amande - Indigo
        else if (A == 3 && B == 20)
            C = 34; // Amande - Ebène
        else if (A == 17 && B == 18)
            C = 44; // Dorée - Indigo
        else if (A == 3 && B == 18)
            C = 42; // Dorée - Ebène
        else if (A == 3 && B == 17)
            C = 51; // Ebène - Indigo
        else if (A == 38 && B == 51)
            C = 19; // Purpre
        else if (A == 46 && B == 51)
            C = 22; // Orchidée
        else if (A == 10 && B == 19)
            C = 71; // Purpre - Rousse
        else if (A == 10 && B == 22)
            C = 70; // Orchidée - Rousse
        else if (A == 19 && B == 20)
            C = 41; // Amande - Purpre
        else if (A == 20 && B == 22)
            C = 40; // Amande - Orchidée
        else if (A == 18 && B == 19)
            C = 49; // Dorée - Purpre
        else if (A == 18 && B == 22)
            C = 48; // Dorée - Orchidée
        else if (A == 17 && B == 19)
            C = 65; // Indigo - Purpre
        else if (A == 17 && B == 22)
            C = 64; // Indigo - Orchidée
        else if (A == 3 && B == 19)
            C = 54; // Ebène - Purpre
        else if (A == 3 && B == 22)
            C = 53; // Ebène - Orchidée
        else if (A == 19 && B == 22)
            C = 76; // Orchidée - Purpre
        else if (A == 53 && B == 76)
            C = 15; // Turquoise
        else if (A == 65 && B == 76)
            C = 16; // Ivoire
        else if (A == 10 && B == 16)
            C = 11; // Ivoire - Rousse
        else if (A == 10 && B == 15)
            C = 69; // Turquoise - Rousse
        else if (A == 16 && B == 20)
            C = 37; // Amande - Ivoire
        else if (A == 15 && B == 20)
            C = 39; // Amande - Turquoise
        else if (A == 16 && B == 18)
            C = 45; // Dorée - Ivoire
        else if (A == 15 && B == 18)
            C = 47; // Dorée - Turquoise
        else if (A == 16 && B == 17)
            C = 61; // Indigo - Ivoire
        else if (A == 15 && B == 17)
            C = 63; // Indigo - Turquoise
        else if (A == 3 && B == 16)
            C = 9; // Ebène - Ivoire
        else if (A == 3 && B == 15)
            C = 52; // Ebène - Turquoise
        else if (A == 16 && B == 19)
            C = 68; // Ivoire - Purpre
        else if (A == 15 && B == 19)
            C = 73; // Turquoise - Purpre
        else if (A == 16 && B == 22)
            C = 67; // Ivoire - Orchidée
        else if (A == 15 && B == 22)
            C = 72; // Orchidée - Turquoise
        else if (A == 15 && B == 16)
            C = 66; // Ivoire - Turquoise
        else if (A == 66 && B == 68)
            C = 21; // Emeraude
        else if (A == 66 && B == 72)
            C = 23; // Prune
        else if (A == 10 && B == 21)
            C = 57;// Emeraude - Rousse
        else if (A == 20 && B == 21)
            C = 35; // Amande - Emeraude
        else if (A == 18 && B == 21)
            C = 43; // Dorée - Emeraude
        else if (A == 3 && B == 21)
            C = 50; // Ebène - Emeraude
        else if (A == 17 && B == 21)
            C = 55; // Emeraude - Indigo
        else if (A == 16 && B == 21)
            C = 56; // Emeraude - Ivoire
        else if (A == 15 && B == 21)
            C = 58; // Emeraude - Turquoise
        else if (A == 21 && B == 22)
            C = 59; // Emeraude - Orchidée
        else if (A == 19 && B == 21)
            C = 60; // Emeraude - Purpre
        else if (A == 20 && B == 23)
            C = 77; // Prune - Amande
        else if (A == 18 && B == 23)
            C = 78; // Prune - Dorée
        else if (A == 3 && B == 23)
            C = 79; // Prune - Ebène
        else if (A == 21 && B == 23)
            C = 80; // Prune - Emeraude
        else if (A == 17 && B == 23)
            C = 82; // Prune - Indigo
        else if (A == 16 && B == 23)
            C = 83; // Prune - Ivoire
        else if (A == 10 && B == 23)
            C = 84; // Prune - Rousse
        else if (A == 15 && B == 23)
            C = 85; // Prune - Turquoise
        else if (A == 22 && B == 23)
            C = 86; // Prune - Orchidée
        else if (A == 19 && B == 23)
            C = 87; // Prune - Purpre
        else if (A == B)
            C = A = B;

        if(C == 0) {

            random = new CaracteristicasRandom<>();
            i = 0;
            for(String str : splitF) {
                i++;
                if(str.equals("?")) continue;

                int pct = switch (i) {
                    case 1, 2 -> 25;
                    case 3, 4, 5, 6 -> 10;
                    default -> 1;
                };

                random.add(pct, Integer.parseInt(str));
            }
            i = 0;
            for(String str : splitM) {
                i++;
                if(str.equals("?")) continue;

                int pct = switch (i) {
                    case 1, 2 -> 25;
                    case 3, 4, 5, 6 -> 10;
                    default -> 1;
                };

                random.add(pct, Integer.parseInt(str));
            }
            C = random.get();
            player.sendMessage("Merci de crier auprès du staff que C = 0, A = " + A + ", et B = " + B + ". Valeur finale : " + C + ". Message bien évidement sérieux.");

            return C;
        }
        random = new CaracteristicasRandom<>();
        random.add(33, A);
        random.add(33, B);
        random.add(33, C);
        return random.get();
    }

    public static int getParchoByIdPets(int id) {
        return switch (id) {
            case 10802 -> 10806;
            case 10107 -> 10135;
            case 10106 -> 10134;
            case 9795 -> 9810;
            case 9624 -> 9685;
            case 9623 -> 9684;
            case 9620 -> 9683;
            case 9619 -> 9682;
            case 9617 -> 9675;
            case 9594 -> 9598;
            case 8693 -> 8707;
            case 8677 -> 8684;
            case 8561 -> 8564;
            case 8211 -> 8544;
            case 8155 -> 8179;
            case 8154 -> 8178;
            case 8153 -> 8175;
            case 8151 -> 8176;
            case 8000 -> 8180;
            case 7911 -> 8526;
            case 7892 -> 7896;
            case 7891 -> 7895;
            case 7714 -> 8708;
            case 7713 -> 9681;
            case 7712 -> 9680;
            case 7711 -> 9679;
            case 7710 -> 9678;
            case 7709 -> 9677;
            case 7708 -> 9676;
            case 7707 -> 9674;
            case 7706 -> 8685;
            case 7705 -> 8889;
            case 7704 -> 8888;
            case 7703 -> 8421;
            case 7524 -> 8887;
            case 7522 -> 7535;
            case 7520 -> 7533;
            case 7519 -> 7534;
            case 7518 -> 7532;
            case 7415 -> 7419;
            case 7414 -> 7418;
            case 6978 -> 7417;
            case 6716 -> 7420;
            case 2077 -> 2098;
            case 2076 -> 2101;
            case 2075 -> 2100;
            case 2074 -> 2099;
            case 1748 -> 2102;
            case 1728 -> 1735;
            default -> -1;
        };
    }

    public static int getPetsByIdParcho(int id) {
        return switch (id) {
            case 10806 -> 10802;
            case 10135 -> 10107;
            case 10134 -> 10106;
            case 9810 -> 9795;
            case 9685 -> 9624;
            case 9684 -> 9623;
            case 9683 -> 9620;
            case 9682 -> 9619;
            case 9675 -> 9617;
            case 9598 -> 9594;
            case 8707 -> 8693;
            case 8684 -> 8677;
            case 8564 -> 8561;
            case 8544 -> 8211;
            case 8179 -> 8155;
            case 8178 -> 8154;
            case 8175 -> 8153;
            case 8176 -> 8151;
            case 8180 -> 8000;
            case 8526 -> 7911;
            case 7896 -> 7892;
            case 7895 -> 7891;
            case 8708 -> 7714;
            case 9681 -> 7713;
            case 9680 -> 7712;
            case 9679 -> 7711;
            case 9678 -> 7710;
            case 9677 -> 7709;
            case 9676 -> 7708;
            case 9674 -> 7707;
            case 8685 -> 7706;
            case 8889 -> 7705;
            case 8888 -> 7704;
            case 8421 -> 7703;
            case 8887 -> 7524;
            case 7535 -> 7522;
            case 7533 -> 7520;
            case 7534 -> 7519;
            case 7532 -> 7518;
            case 7419 -> 7415;
            case 7418 -> 7414;
            case 7417 -> 6978;
            case 7420 -> 6716;
            case 2098 -> 2077;
            case 2101 -> 2076;
            case 2100 -> 2075;
            case 2099 -> 2074;
            case 2102 -> 1748;
            case 1735 -> 1728;
            default -> -1;
        };
    }

    public static int getDoplonDopeul(int IDmob) {
        return switch (IDmob) {
            case 168 -> 10302;
            case 165 -> 10303;
            case 166 -> 10304;
            case 162 -> 10305;
            case 160 -> 10306;
            case 167 -> 10307;
            case 161 -> 10308;
            case 2691 -> 10309;
            case 455 -> 10310;
            case 169 -> 10311;
            case 163 -> 10312;
            case 164 -> 10313;
            default -> -1;
        };
    }

    public static int getIDdoplonByMapID(int IDmap) {
        return switch (IDmap) {
//Sram
            case 6926 -> 10312;
//Enutrof
            case 1470 -> 10305;
//Ecaflip (map de dessous, puisque l'autre n'est pas dans l'emu)
            case 1461 -> 10303;
//Sacrieur
            case 6949 -> 10310;
//Cra (map en bas dans la maison, celle dans haut n'est pas dans l'emu)
            case 1556 -> 10302;
//Iop
            case 1549 -> 10307;
//Xel
            case 1469 -> 10313;
//Eniripsa (dehors, puisque l'int�rieur n'est pas pr�sent dans l'emu)
            case 487 -> 10304;
//Osamodas (idem qu'eniripsa)
            case 490 -> 10308;
//Feca (idem ...)
            case 177 -> 10306;
//Sadida
            case 1466 -> 10311;
//Panda (idem que nini ...)
            case 8207 -> 10309;
            default -> -1;
        };
    }

    public static int getArmeSoin(int idArme) {
        return switch (idArme) {
            case 7172, 7182 -> 100;
            case 7156, 6539 -> 80;
            case 1355 -> 42;
            case 7040 -> 10;
            case 6519 -> 23;
            case 8118 -> 30;
            default -> -1;
        };
    }

    public static int getSectionByDopeuls(int id) {
        return switch (id) {
            case 160 -> 1;
            case 161 -> 2;
            case 162 -> 3;
            case 163 -> 4;
            case 164 -> 5;
            case 165 -> 6;
            case 166 -> 7;
            case 167 -> 8;
            case 168 -> 9;
            case 169 -> 10;
            case 455 -> 11;
            case 2691 -> 12;
            default -> -1;
        };
    }

    public static int getCertificatByDopeuls(int id) {
        return switch (id) {
            case 160 -> 10293;
            case 161 -> 10295;
            case 162 -> 10292;
            case 163 -> 10299;
            case 164 -> 10300;
            case 165 -> 10290;
            case 166 -> 10291;
            case 167 -> 10294;
            case 168 -> 10289;
            case 169 -> 10298;
            case 455 -> 10297;
            case 2691 -> 10296;
            default -> -1;
        };
    }

    public static boolean isCertificatDopeuls(int id) {
        return switch (id) {
            case 10293, 10295, 10292, 10299, 10300, 10290, 10291, 10294, 10289, 10298, 10297, 10296 -> true;
            default -> false;
        };
    }

    public static int getItemIdByMascotteId(int id) {
        return switch (id) {
//Croc blanc
            case 10118 -> 1498;
//Eni Hoube
            case 10078 -> 70;
//Terra Cogita
            case 10077 -> -1;
//Xephir�s
            case 10009 -> 90;
//Sabine
            case 9993 -> 71;
//Tacticien
            case 9096 -> 30;
//Exoram
            case 9061 -> 40;
//Titi gobelait
            case 8563 -> 1076;
//Petite Larve Dor�e
            case 7425 -> 1588;
//Zato�shwan
            case 7354 -> 1264;
//Marzwell Le Gobelin
            case 7353 -> 1076;
//Musha l'Oni
            case 7352 -> 1153;
//Rok Gnorok
            case 7351 -> 1248;
//Aermyne Braco Scalptaras
            case 7350 -> 1228;
//Poochan
            case 7062 -> 9001;
//Ogivol Scarlarcin
            case 6876 -> 1245;
//Fouduglen
            case 6875 -> 1249;
//Brumen Tinctorias
            case 6874 -> 70;
//Qil Bil
            case 6873 -> 1243;
//Nervoes Brakdoun
            case 6872 -> 50;
//Frakacia Leukocytine
            case 6871 -> 1247;
//Padgref Demo�l
            case 6870 -> 1246;
//Pleur Nycheuz
            case 6869 -> 9043;
//Livreur de Bi�re
            case 6832 -> -1;
//Soki
            case 6768 -> 9001;
//Larve Dor�e
            case 2272 -> 1577;
//Raaga
            case 2169 -> 1205;
//Colonel Lyeno
            case 2152 -> 1001;
//Trof Hapyus
            case 2134 -> 1205;
//Hou� Dapyus
            case 2132 -> 9004;
//Colonel Lyeno
            case 2130 -> 1001;
//Marcassin
            case 2082 -> 1208;
            default -> -1;
        };
    }

    public static boolean isIncarnationWeapon(int id) {
        return switch (id) {
            case 9544, 9545, 9546, 9547, 9548, 10133, 10127, 10126, 10125 -> true;
            default -> false;
        };
    }

    public static boolean isTourmenteurWeapon(int id) {
        return switch (id) {
            case 9544, 9545, 9546, 9547, 9548 -> true;
            default -> false;
        };
    }

    public static boolean isBanditsWeapon(int id) {
        return switch (id) {
            case 10133, 10127, 10126, 10125 -> true;
            default -> false;
        };
    }

    public static int getHechizosEspecialesClase(int clase) {
        return switch (clase) {
            case Constantes.CLASE_FECA -> 422;
            case Constantes.CLASE_OSAMODAS -> 420;
            case Constantes.CLASE_ANUTROF -> 425;
            case Constantes.CLASE_SRAM -> 416;
            case Constantes.CLASE_XELOR -> 424;
            case Constantes.CLASE_ZURCARAK -> 412;
            case Constantes.CLASE_ANIRIPSA -> 427;
            case Constantes.CLASE_YOPUKA -> 410;
            case Constantes.CLASE_OCRA -> 418;
            case Constantes.CLASE_SADIDA -> 426;
            case Constantes.CLASE_SACROGRITO -> 421;
            case Constantes.CLASE_PANDAWA -> 423;
            default -> 0;
        };
    }

    public static boolean isFlacGelee(int id) {
        return switch (id) {
            case 2430, 2431, 2432, 2433 -> true;
            default -> false;
        };
    }

    public static boolean isDoplon(int id) {
        return switch (id) {
            case 10302, 10303, 10304, 10305, 10306, 10307, 10308, 10309, 10310, 10311, 10312, 10313 -> true;
            default -> false;
        };
    }

    public static boolean isInMorphDonjon(int id) {
        return switch (id) {
            case 8716, 8718, 8719, 9121, 9122, 9123, 8979, 8980, 8981, 8982, 8983, 8984, 9716 -> true;
            default -> false;
        };
    }

    public static int[] getOppositeStats(int statsId) {
        if (statsId == 217)
            return new int[]{210, 211, 213, 214};
        else if (statsId == 216)
            return new int[]{210, 212, 213, 214};
        else if (statsId == 218)
            return new int[]{210, 211, 212, 214};
        else if (statsId == 219)
            return new int[]{210, 211, 212, 214};
        else if (statsId == 215)
            return new int[]{211, 212, 213, 214};
        return null;
    }

    public static int getNearestCellIdUnused(Jugador player) {
        final Mapa map = player.getCurMap();
        final int width = map.getW();
        final int cell = player.getCurCell().getId();
        final int[] cells = new int[] {cell - width, cell - width + 1, cell + width - 1, cell + width};
        int cellPosition = -1;

        for(int available : cells) {
            GameCase c = map.getCase(available);
            if (c != null && c.getDroppedItem(false) == null && c.getPlayers().isEmpty() && c.isWalkable(false) && c.getObject() == null) {
                return available;
            }
        }
        return -1;
    }

    //Bonus de armas por raza
    public static int getWeaponClassModifier(Jugador player) {
        int modifier=Configuracion.INSTANCE.getArmabonusbase();
        int weaponType=player.getObjetByPos(1).getModelo().getType();
        if(weaponType==5||weaponType==6) //dagger or sword
            modifier-=Configuracion.INSTANCE.getDaganerf();
        switch(player.getClasse())
        {
            case 1: //Feca
                if(weaponType==4) //staff
                    modifier+=Configuracion.INSTANCE.getPrimerarmabonus();
                if(weaponType==3) //wand
                    modifier+=Configuracion.INSTANCE.getSegundaarmabonus();
                break;
            case 2: //Osamodas
                if(weaponType==7) //hammer
                    modifier+=Configuracion.INSTANCE.getPrimerarmabonus();
                if(weaponType==4) //staff
                    modifier+=Configuracion.INSTANCE.getSegundaarmabonus();
                break;
            case 3: //Enutrof
                if(weaponType==8) //shovel
                    modifier+=Configuracion.INSTANCE.getPrimerarmabonus();
                if(weaponType==7) //hammer
                    modifier+=Configuracion.INSTANCE.getSegundaarmabonus();
                break;
            case 4: //Sram
                if(weaponType==5) //dagger
                    modifier+=Configuracion.INSTANCE.getPrimerarmabonus();
                if(weaponType==2) //bow
                    modifier+=Configuracion.INSTANCE.getSegundaarmabonus();
                break;
            case 5: //Xelor
                if(weaponType==7) //hammer
                    modifier+=Configuracion.INSTANCE.getPrimerarmabonus();
                if(weaponType==3) //wand
                    modifier+=Configuracion.INSTANCE.getSegundaarmabonus();
                break;
            case 6: //Ecaflip
                if(weaponType==6) //sword
                    modifier+=Configuracion.INSTANCE.getPrimerarmabonus();
                if(weaponType==5) //dagger
                    modifier+=Configuracion.INSTANCE.getSegundaarmabonus();
                break;
            case 7: //Eniripsa
                if(weaponType==3) //wand
                    modifier+=Configuracion.INSTANCE.getPrimerarmabonus();
                if(weaponType==4) //staff
                    modifier+=Configuracion.INSTANCE.getSegundaarmabonus();
                break;
            case 8: //Iop
                if(weaponType==6) //sword
                    modifier+=Configuracion.INSTANCE.getPrimerarmabonus();
                if(weaponType==19) //hammer
                    modifier+=Configuracion.INSTANCE.getSegundaarmabonus();
                break;
            case 9: //Cra
                if(weaponType==2) //bow
                    modifier+=Configuracion.INSTANCE.getPrimerarmabonus();
                if(weaponType==5) //dagger
                    modifier+=Configuracion.INSTANCE.getSegundaarmabonus();
                break;
            case 10: //Sadida
                if(weaponType==4) //staff
                    modifier+=Configuracion.INSTANCE.getPrimerarmabonus();
                if(weaponType==19) //wand
                    modifier+=Configuracion.INSTANCE.getSegundaarmabonus();
                break;
            case 11: //Sacrier
                break;
            case 12: //Pandawa
                if(weaponType==19) //axe
                    modifier+=Configuracion.INSTANCE.getPrimerarmabonus();
                if(weaponType==4) //staff
                    modifier+=Configuracion.INSTANCE.getSegundaarmabonus();
                break;
        }
        return modifier;
    }
}