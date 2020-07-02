package org.alexandria.comunes;

import org.alexandria.estaticos.area.mapa.CellCacheImpl;
import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.configuracion.Constantes;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.EfectoHechizo;
import org.alexandria.otro.utilidad.Doble;
import org.alexandria.estaticos.Gremio.GremioMiembros;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class Formulas {

    public final static Random random = new Random();

    public static int countCell(int i) {
        if (i > 64)
            i = 64;
        return 2 * (i) * (i + 1);
    }

    public static int getRandomValue(int i1, int i2) {
        if (i2 < i1)
            return 0;
        return (random.nextInt((i2 - i1) + 1)) + i1;
    }

    public static int pushDamage(int pushedCells, int lvlPusher, int pushDam, int negPushDam, int pushRes, int negPushRes)
    {
        return (8+Formulas.getRandomJet("1d8+0")*lvlPusher/50)*pushedCells+pushDam-negPushDam-pushRes+negPushRes;
    }

    public static int getMinJet(String jet) {
        int num = 0;
        try {
            int des = Integer.parseInt(jet.split("d")[0]);
            int add = Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
            num = des + add;
            return num;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int getMaxJet(String jet) {
        int num = 0;
        try {
            int des = Integer.parseInt(jet.split("d")[0]);
            int faces = Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
            int add = Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
            for (int a = 0; a < des; a++) {
                num += faces;
            }
            num += add;
            return num;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int getRandomJet(String jet)//1d5+6
    {
        int num = 0;
        try {
            int des = Integer.parseInt(jet.split("d")[0]);
            int faces = Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
            int add = Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
            for (int a = 0; a < des; a++)
                num += getRandomValue(1, faces);
            num += add;
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }
        //Evitamos el -1 y con ello superponer personajes despues del ataque
        return num;
    }

    public static int getRandomJet(Peleador caster, Peleador target, String jet)//1d5+6
    {
        try {
            if(target != null)
                if(target.hasBuff(782))
                    return Formulas.getMaxJet(jet);
            if(caster != null)
                if(caster.hasBuff(781))
                    return Formulas.getMinJet(jet);
            int num = 0, des, faces, add;

            des = Integer.parseInt(jet.split("d")[0]);
            faces = Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
            add = Integer.parseInt(jet.split("d")[1].split("\\+")[1]);

            if (faces == 0 && add == 0) {
                num = getRandomValue(0, des);
            } else {
                for (int a = 0; a < des; a++) {
                    num += getRandomValue(1, faces);
                }
            }
            num += add;
            return num;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int getMiddleJet(String jet)//1d5+6
    {
        try {
            int num = 0;
            int des = Integer.parseInt(jet.split("d")[0]);
            int faces = Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
            int add = Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
            num += ((1 + faces) / 2) * des;//on calcule moyenne
            num += add;
            return num;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getTacleChance(Peleador fight, Peleador fighter) {
        int agiTacleur = fight.getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
        int agiEnemi = fighter.getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
        int div = agiTacleur + agiEnemi + 50;
        if (div == 0)
            div = 1;
        int esquive = 300 * (agiTacleur + 25) / div - 100;
        return esquive;
    }

    public static int calculFinalHealCac(Peleador healer, int rank, boolean isCac) {
        int intel = healer.getTotalStats().getEffect(126);
        int heals = healer.getTotalStats().getEffect(178);
        if (intel < 0)
            intel = 0;
        float a=1;

        //Bonus de arma por clase
        if(isCac)
            if(healer.getPlayer()!=null&&healer.getPlayer().getObjetByPos(1)!=null)
            {
                float i=0; //Bonus maitrise
                float j=Constantes.getWeaponClassModifier(healer.getPlayer());
                int ArmeType=healer.getPlayer().getObjetByPos(1).getModelo().getType();
                if((healer.getSpellValueBool(392))&&ArmeType==2)//ARC
                    i=healer.getMaitriseDmg(392);
                if((healer.getSpellValueBool(390))&&ArmeType==4)//BATON
                    i=healer.getMaitriseDmg(390);
                if((healer.getSpellValueBool(391))&&ArmeType==6)//EPEE
                    i=healer.getMaitriseDmg(391);
                if((healer.getSpellValueBool(393))&&ArmeType==7)//MARTEAUX
                    i=healer.getMaitriseDmg(393);
                if((healer.getSpellValueBool(394))&&ArmeType==3)//BAGUETTE
                    i=healer.getMaitriseDmg(394);
                if((healer.getSpellValueBool(395))&&ArmeType==5)//DAGUES
                    i=healer.getMaitriseDmg(395);
                if((healer.getSpellValueBool(396))&&ArmeType==8)//PELLE
                    i=healer.getMaitriseDmg(396);
                if((healer.getSpellValueBool(397))&&ArmeType==19)//HACHE
                    i=healer.getMaitriseDmg(397);
                a=(((100+i)/100)*(j/100));
            }

        return (int)(a*(rank*((100.00+intel)/100)+heals));
    }

    public static int calculXpWinCraft(int lvl, int numCase) {
        if (lvl == 100)
            return 0;
        switch (numCase) {
            case 1 -> {
                if (lvl < 40)
                    return 1;
                return 0;
            }
            case 2 -> {
                if (lvl < 60)
                    return 10;
                return 0;
            }
            case 3 -> {
                if (lvl > 9 && lvl < 80)
                    return 25;
                return 0;
            }
            case 4 -> {
                if (lvl > 19)
                    return 50;
                return 0;
            }
            case 5 -> {
                if (lvl > 39)
                    return 100;
                return 0;
            }
            case 6 -> {
                if (lvl > 59)
                    return 250;
                return 0;
            }
            case 7 -> {
                if (lvl > 79)
                    return 500;
                return 0;
            }
            case 8 -> {
                if (lvl > 99)
                    return 1000;
                return 0;
            }
        }
        return 0;
    }

    public static int calculXpWinFm(int lvl, int poid) {
        if (lvl <= 1) {
            if (poid <= 10)
                return 10;
            else if (poid <= 50)
                return 25;
            else
                return 50;
        }
        if (lvl <= 25) {
            if (poid <= 10)
                return 10;
            else
                return 50;
        } else if (lvl <= 50) {
            if (poid <= 1)
                return 10;
            if (poid <= 10)
                return 25;
            if (poid <= 50)
                return 50;
            else
                return 100;
        } else if (lvl <= 75) {
            if (poid <= 3)
                return 25;
            if (poid <= 10)
                return 50;
            if (poid <= 50)
                return 100;
            else
                return 250;
        } else if (lvl <= 100) {
            if (poid <= 3)
                return 50;
            if (poid <= 10)
                return 100;
            if (poid <= 50)
                return 250;
            else
                return 500;
        } else if (lvl <= 125) {
            if (poid <= 3)
                return 100;
            if (poid <= 10)
                return 250;
            if (poid <= 50)
                return 500;
            else
                return 1000;
        } else if (lvl <= 150) {
            if (poid <= 10)
                return 250;
            else
                return 1000;
        } else if (lvl <= 175) {
            if (poid <= 1)
                return 250;
            if (poid <= 10)
                return 500;
            else
                return 1000;
        } else {
            if (poid <= 1)
                return 500;
            else
                return 1000;
        }
    }

    public static int calculXpLooseCraft(int lvl, int numCase) {
        if (lvl == 100)
            return 0;
        switch (numCase) {
            case 1 -> {
                if (lvl < 40)
                    return 1;
                return 0;
            }
            case 2 -> {
                if (lvl < 60)
                    return 5;
                return 0;
            }
            case 3 -> {
                if (lvl > 9 && lvl < 80)
                    return 12;
                return 0;
            }
            case 4 -> {
                if (lvl > 19)
                    return 25;
                return 0;
            }
            case 5 -> {
                if (lvl > 39)
                    return 50;
                return 0;
            }
            case 6 -> {
                if (lvl > 59)
                    return 125;
                return 0;
            }
            case 7 -> {
                if (lvl > 79)
                    return 250;
                return 0;
            }
            case 8 -> {
                if (lvl > 99)
                    return 500;
                return 0;
            }
        }
        return 0;
    }

    public static int calculHonorWin(ArrayList<Peleador> winner,
                                     ArrayList<Peleador> looser, Peleador F) {
        float totalGradeWin = 0;
        float totalLevelWin = 0;
        float totalGradeLoose = 0;
        float totalLevelLoose = 0;
        boolean Prisme = false;
        int fighters = 0;
        for (Peleador f : winner) {
            if (f.getPlayer() == null && f.getPrism() == null)
                continue;
            if (f.getPlayer() != null) {
                totalLevelWin += f.getLvl();
                totalGradeWin += f.getPlayer().getGrade();
            } else {
                Prisme = true;
                totalLevelWin += (f.getPrism().getLevel() * 15 + 80);
                totalGradeWin += f.getPrism().getLevel();
            }
        }
        for (Peleador f : looser) {
            if (f.getPlayer() == null && f.getPrism() == null)
                continue;
            if (f.getPlayer() != null) {
                totalLevelLoose += f.getLvl();
                totalGradeLoose += f.getPlayer().getGrade();
                fighters++;
            } else {
                Prisme = true;
                totalLevelLoose += (f.getPrism().getLevel() * 15 + 80);
                totalGradeLoose += f.getPrism().getLevel();
            }
        }
        if (!Prisme)
            if (totalLevelWin - totalLevelLoose > 15 * fighters)
                return 0;
        int base = (int) (100 * ((totalGradeLoose * totalLevelLoose) / (totalGradeWin * totalLevelWin)))
                / winner.size();
        if (Prisme && base <= 0)
            return 100;
        if (looser.contains(F))
            base = -base;
        return base * Configuracion.INSTANCE.getRATE_HONOR();
    }

    public static int calculFinalDommage(Pelea fight, Peleador caster, Peleador target, int statID, int jet, boolean isHeal, boolean isCaC, int spellid) {
        int value = calculFinalDommagee(fight, caster, target, statID, jet, isHeal, spellid);
        return Math.max(value, 0);
    }

    public static int calculFinalDommagee(Pelea fight, Peleador caster, Peleador target, int statID, int jet, boolean isHeal, int spellid) {
        // if (target.hasBuff(788) && target.getBuff(788).getValue() == 101)
        float a = 1;//Calcul
        float num = 0;
        float statC = 0, domC = 0, perdomC = 0, resfT = 0, respT = 0, mulT = 1;
        int multiplier = 0;
        if (!isHeal) {
            domC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_DOMA);
            perdomC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_PERDOM);
            multiplier = caster.getTotalStats().getEffect(Constantes.STATS_MULTIPLY_DOMMAGE);
            if (caster.hasBuff(114))
                mulT = caster.getBuffValue(114);
        } else
            domC=caster.getTotalStats().getEffect(Constantes.STATS_ADD_SOIN);

        //Debug de golpes
        if(caster.getPlayer() !=null && spellid == 0 && caster.getPlayer().getObjetByPos(1) == null)
        {
            jet=Formulas.getRandomJet("1d4+1");
            statID=0;
            isHeal=false;
            boolean isCaC = false;
        }

        //on ajoute les dom Physique
        //Ajout de la resist Physique
        //on ajout les dom Physique
        //Ajout de la resist Physique
        //Ajout de la resist Magique
        //Ajout de la resist Magique
        //Ajout de la resist Magique
        switch (statID) {
//Fixe
            case Constantes.ELEMENT_NULL -> {
                statC = 0;
                resfT = 0;
                respT = 0;
                respT = 0;
                mulT = 1;
            }
//neutre
            case Constantes.ELEMENT_NEUTRE -> {
                statC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_FORC);
                resfT = target.getTotalStats().getEffect(Constantes.STATS_ADD_R_NEU);
                respT = target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_NEU);
                if (caster.getPlayer() != null)//Si c'est un joueur
                {
                    respT += target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_PVP_NEU);
                    resfT += target.getTotalStats().getEffect(Constantes.STATS_ADD_R_PVP_NEU);
                }
                domC += caster.getTotalStats().getEffect(142);
                resfT = target.getTotalStats().getEffect(184);
            }
//force
            case Constantes.ELEMENT_TERRE -> {
                statC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_FORC);
                resfT = target.getTotalStats().getEffect(Constantes.STATS_ADD_R_TER);
                respT = target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_TER);
                if (caster.getPlayer() != null)//Si c'est un joueur
                {
                    respT += target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_PVP_TER);
                    resfT += target.getTotalStats().getEffect(Constantes.STATS_ADD_R_PVP_TER);
                }
                domC += caster.getTotalStats().getEffect(142);
                resfT = target.getTotalStats().getEffect(184);
            }
//chance
            case Constantes.ELEMENT_EAU -> {
                statC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_CHAN);
                resfT = target.getTotalStats().getEffect(Constantes.STATS_ADD_R_EAU);
                respT = target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_EAU);
                if (caster.getPlayer() != null)//Si c'est un joueur
                {
                    respT += target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_PVP_EAU);
                    resfT += target.getTotalStats().getEffect(Constantes.STATS_ADD_R_PVP_EAU);
                }
                resfT = target.getTotalStats().getEffect(183);
            }
//intell
            case Constantes.ELEMENT_FEU -> {
                statC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
                resfT = target.getTotalStats().getEffect(Constantes.STATS_ADD_R_FEU);
                respT = target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_FEU);
                if (caster.getPlayer() != null)//Si c'est un joueur
                {
                    respT += target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_PVP_FEU);
                    resfT += target.getTotalStats().getEffect(Constantes.STATS_ADD_R_PVP_FEU);
                }
                resfT = target.getTotalStats().getEffect(183);
            }
//agilit�
            case Constantes.ELEMENT_AIR -> {
                statC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
                resfT = target.getTotalStats().getEffect(Constantes.STATS_ADD_R_AIR);
                respT = target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_AIR);
                if (caster.getPlayer() != null)//Si c'est un joueur
                {
                    respT += target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_PVP_AIR);
                    resfT += target.getTotalStats().getEffect(Constantes.STATS_ADD_R_PVP_AIR);
                }
                resfT = target.getTotalStats().getEffect(183);
            }
        }
        //On bride la resistance a 50% si c'est un joueur
        if (target.getMob() == null && respT > 50)
            respT = 50;

        if (statC < 0)
            statC = 0;

        if(caster.getPlayer() != null && spellid == 0 && caster.getPlayer().getObjetByPos(1) != null)
        {
            float i = 0; //Bonus maitrise
            float j = Constantes.getWeaponClassModifier(caster.getPlayer());
            int ArmeType=caster.getPlayer().getObjetByPos(1).getModelo().getType();

            if ((caster.getSpellValueBool(392)) && ArmeType == 2)//ARC
            {
                i = caster.getMaitriseDmg(392);
            }
            if ((caster.getSpellValueBool(390)) && ArmeType == 4)//BATON
            {
                i = caster.getMaitriseDmg(390);
            }
            if ((caster.getSpellValueBool(391)) && ArmeType == 6)//EPEE
            {
                i = caster.getMaitriseDmg(391);
            }
            if ((caster.getSpellValueBool(393)) && ArmeType == 7)//MARTEAUX
            {
                i = caster.getMaitriseDmg(393);
            }
            if ((caster.getSpellValueBool(394)) && ArmeType == 3)//BAGUETTE
            {
                i = caster.getMaitriseDmg(394);
            }
            if ((caster.getSpellValueBool(395)) && ArmeType == 5)//DAGUES
            {
                i = caster.getMaitriseDmg(395);
            }
            if ((caster.getSpellValueBool(396)) && ArmeType == 8)//PELLE
            {
                i = caster.getMaitriseDmg(396);
            }
            if ((caster.getSpellValueBool(397)) && ArmeType == 19)//HACHE
            {
                i = caster.getMaitriseDmg(397);
            }
            a = (((100 + i) / 100) * (j / 100));
        }

        num = a * mulT * (jet * ((100 + statC + perdomC + (multiplier * 100)) / 100))
                + domC;//d�gats bruts
        //Poisons
        if (spellid != -1) {
            switch (spellid) {
                case 66 -> {
                    statC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
                    num = (jet * ((100 + statC + perdomC + (multiplier * 100)) / 100)) + domC;
                    if (target.hasBuff(105) && spellid != 71) {
                        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId() + "", target.getId() + "," + target.getBuff(105).getValue());
                        int value = (int) num - target.getBuff(105).getValue();
                        return Math.max(value, 0);
                    }
                    if (target.hasBuff(184) && spellid != 71) {
                        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId() + "", target.getId() + "," + target.getBuff(184).getValue());
                        int value = (int) num - target.getBuff(184).getValue();
                        return Math.max(value, 0);
                    }
                    return (int) num;
                }
                case 71, 196, 219 -> {
                    statC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_FORC);
                    num = (jet * ((100 + statC + perdomC + (multiplier * 100)) / 100)) + domC;
                    if (target.hasBuff(105) && spellid != 71) {
                        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId() + "", target.getId() + "," + target.getBuff(105).getValue());
                        int value = (int) num - target.getBuff(105).getValue();
                        return Math.max(value, 0);
                    }
                    if (target.hasBuff(184) && spellid != 71) {
                        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId() + "", target.getId() + "," + target.getBuff(184).getValue());
                        int value = (int) num - target.getBuff(184).getValue();
                        return Math.max(value, 0);
                    }
                    return (int) num;
                }
                case 181, 200 -> {
                    statC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
                    num = (jet * ((100 + statC + perdomC + (multiplier * 100)) / 100)) + domC;
                    if (target.hasBuff(105) && spellid != 71) {
                        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId() + "", target.getId() + "," + target.getBuff(105).getValue());
                        int value = (int) num - target.getBuff(105).getValue();
                        return Math.max(value, 0);
                    }
                    if (target.hasBuff(184) && spellid != 71) {
                        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId() + "", target.getId() + "," + target.getBuff(184).getValue());
                        int value = (int) num - target.getBuff(184).getValue();
                        return Math.max(value, 0);
                    }
                    return (int) num;
                }
            }
        }
        //Renvoie
        if (caster.getId() != target.getId()) {
            int renvoie = target.getTotalStatsLessBuff().getEffect(Constantes.STATS_RETDOM);
            if (renvoie > 0 && !isHeal) {
                if (renvoie > num)
                    renvoie = (int) num;
                num -= renvoie;
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 107, "-1", target.getId()
                        + "," + renvoie);
                if (renvoie > caster.getPdv())
                    renvoie = caster.getPdv();
                if (num < 1)
                    num = 0;

                if (caster.getPdv() <= renvoie) {
                    caster.removePdv(caster, renvoie);
                    fight.onFighterDie(caster, caster);
                } else
                    caster.removePdv(caster, renvoie);

                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
                        + "", caster.getId() + ",-" + renvoie);
            }
        }
        int reduc = (int) ((num / (float) 100) * respT);//Reduc %resis
        if (!isHeal)
            num -= reduc;
        int armor = getArmorResist(target, statID);
        if (!isHeal)
            num -= armor;
        if (!isHeal)
            if (armor > 0)
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId()
                        + "", target.getId() + "," + armor);
        if (!isHeal)
            num -= resfT;//resis fixe
        //d�gats finaux
        if (num < 1)
            num = 0;
        //Perte de 10% des PDV MAX par points de degat 10 PDV = 1PDV max en moins
        if (target.getPlayer() != null)
            target.removePdvMax((int) Math.floor(num / 10));
        // D�but Formule pour les MOBs
        if (caster.getPlayer() == null && !caster.isCollector()) {
            if (caster.getMob().getTemplate().getId() == 116)//Sacrifi� Dommage = PDV*2
            {
                return (int) ((num / 25) * caster.getPdvMax());
            } else {
                int niveauMob = caster.getLvl();
                double CalculCoef = ((niveauMob * 0.5) / 100);
                int Multiplicateur = (int) Math.ceil(CalculCoef);
                return (int) num * Multiplicateur;
            }
        }
        return (int) num;
    }

    public static int calculZaapCost(Mapa map1, Mapa map2) {
        return 10 * (Math.abs(map2.getX() - map1.getX())
                + Math.abs(map2.getY() - map1.getY()) - 1);
    }

    private static int getArmorResist(Peleador target, int statID) {
        int armor = 0;
        for (EfectoHechizo SE : target.getBuffsByEffectID(265)) {
            Peleador fighter;

            //Si pas element feu, on ignore l'armure
            //Les stats du f�ca sont prises en compte
            //Si pas element terre/neutre, on ignore l'armure
            //Les stats du f�ca sont prises en compte
            //Si pas element air, on ignore l'armure
            //Les stats du f�ca sont prises en compte
            //Si pas element eau, on ignore l'armure
            //Les stats du f�ca sont prises en compte
            switch (SE.getSpell()) {
//Armure incandescente
                case 1 -> {
                    if (statID != Constantes.ELEMENT_FEU)
                        continue;
                    fighter = SE.getCaster();
                }
//Armure Terrestre
                case 6 -> {
                    if (statID != Constantes.ELEMENT_TERRE
                            && statID != Constantes.ELEMENT_NEUTRE)
                        continue;
                    fighter = SE.getCaster();
                }
//Armure Venteuse
                case 14 -> {
                    if (statID != Constantes.ELEMENT_AIR)
                        continue;
                    fighter = SE.getCaster();
                }
//Armure aqueuse
                case 18 -> {
                    if (statID != Constantes.ELEMENT_EAU)
                        continue;
                    fighter = SE.getCaster();
                }
//Dans les autres cas on prend les stats de la cible et on ignore l'element de l'attaque
                default -> fighter = target;
            }
            int intell = fighter.getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
            int carac = switch (statID) {
                case Constantes.ELEMENT_AIR -> fighter.getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
                case Constantes.ELEMENT_FEU -> fighter.getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
                case Constantes.ELEMENT_EAU -> fighter.getTotalStats().getEffect(Constantes.STATS_ADD_CHAN);
                case Constantes.ELEMENT_NEUTRE, Constantes.ELEMENT_TERRE -> fighter.getTotalStats().getEffect(Constantes.STATS_ADD_FORC);
                default -> 0;
            };
            int value = SE.getValue();
            int a = value * (100 + intell / 2 + carac / 2)
                    / 100;
            armor += a;
        }
        for (EfectoHechizo SE : target.getBuffsByEffectID(105)) {
            int intell = target.getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
            int carac = switch (statID) {
                case Constantes.ELEMENT_AIR -> target.getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
                case Constantes.ELEMENT_FEU -> target.getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
                case Constantes.ELEMENT_EAU -> target.getTotalStats().getEffect(Constantes.STATS_ADD_CHAN);
                case Constantes.ELEMENT_NEUTRE, Constantes.ELEMENT_TERRE -> target.getTotalStats().getEffect(Constantes.STATS_ADD_FORC);
                default -> 0;
            };
            int value = SE.getValue();
            int a = value * (100 + intell / 2 + carac / 2)
                    / 100;
            armor += a;
        }
        return armor;
    }

    public static int getPointsLost(char z, int value, Peleador caster, Peleador target) {
        float esquiveC = z == 'a' ? caster.getTotalStats().getEffect(Constantes.STATS_ADD_AFLEE) : caster.getTotalStats().getEffect(Constantes.STATS_ADD_MFLEE);
        float esquiveT = z == 'a' ? target.getTotalStats().getEffect(Constantes.STATS_ADD_AFLEE) : target.getTotalStats().getEffect(Constantes.STATS_ADD_MFLEE);
        float ptsMax = z == 'a' ? target.getTotalStatsLessBuff().getEffect(Constantes.STATS_ADD_PA) : target.getTotalStatsLessBuff().getEffect(Constantes.STATS_ADD_PM);

        int retrait = 0;

        for (int i = 0; i < value; i++) {
            if (ptsMax == 0 && target.getMob() != null) {
                ptsMax = z == 'a' ? target.getMob().getPa() : target.getMob().getPm();
            }

            float pts = z == 'a' ? target.getPa() : target.getPm();
            float ptsAct = pts - retrait;

            if (esquiveT <= 0)
                esquiveT = 1;
            if (esquiveC <= 0)
                esquiveC = 1;

            float a = esquiveC / esquiveT;
            float b = (ptsAct / ptsMax);

            float pourcentage = a * b * 50;
            int chance = (int) Math.ceil(pourcentage);

            if (chance < 0)
                chance = 0;
            if (chance > 100)
                chance = 100;

            int jet = getRandomValue(0, 99);
            if (jet < chance) {
                retrait++;
            }
        }
        return retrait;
    }

    public static long getGuildXpWin(Peleador perso, AtomicReference<Long> xpWin) {
        if (perso.getPlayer() == null)
            return 0;
        if (perso.getPlayer().getGuildMember() == null)
            return 0;

        GremioMiembros gm = perso.getPlayer().getGuildMember();

        double xp = (double) xpWin.get(),
                Lvl = perso.getLvl(),
                LvlGuild = perso.getPlayer().getGuild().getLvl(),
                pXpGive = (double) gm.getXpGive() / 100;

        double maxP = xp * pXpGive * 0.10; //Le maximum donn� � la guilde est 10% du montant pr�lev� sur l'xp du combat
        double diff = Math.abs(Lvl - LvlGuild); //Calcul l'�cart entre le niveau du personnage et le niveau de la guilde
        double toGuild;
        if (diff >= 70) {
            toGuild = maxP * 0.10; //Si l'�cart entre les deux level est de 70 ou plus, l'experience donn�e a la guilde est de 10% la valeur maximum de don
        } else if (diff >= 31 && diff <= 69) {
            toGuild = maxP - ((maxP * 0.10) * (Math.floor((diff + 30) / 10)));
        } else if (diff >= 10 && diff <= 30) {
            toGuild = maxP - ((maxP * 0.20) * (Math.floor(diff / 10)));
        } else
        //Si la diff�rence est [0,9]
        {
            toGuild = maxP;
        }
        xpWin.set((long) (xp - xp * pXpGive));
        return Math.round(toGuild);
    }

    public static long getMountXpWin(Peleador perso, AtomicReference<Long> xpWin) {
        if (perso.getPlayer() == null)
            return 0;
        if (perso.getPlayer().getMount() == null)
            return 0;

        int diff = Math.abs(perso.getLvl()
                - perso.getPlayer().getMount().getLevel());

        double coeff = 0;
        double xp = (double) xpWin.get();
        double pToMount = (double) perso.getPlayer().getMountXpGive() / 100 + 0.2;

        if (diff >= 0 && diff <= 9)
            coeff = 0.1;
        else if (diff >= 10 && diff <= 19)
            coeff = 0.08;
        else if (diff >= 20 && diff <= 29)
            coeff = 0.06;
        else if (diff >= 30 && diff <= 39)
            coeff = 0.04;
        else if (diff >= 40 && diff <= 49)
            coeff = 0.03;
        else if (diff >= 50 && diff <= 59)
            coeff = 0.02;
        else if (diff >= 60 && diff <= 69)
            coeff = 0.015;
        else
            coeff = 0.01;

        if (pToMount > 0.2)
            xpWin.set((long) (xp - (xp * (pToMount - 0.2))));

        return Math.round(xp * pToMount * coeff);
    }

    public static int getKamasWin(Peleador i, ArrayList<Peleador> winners,
                                  int maxk, int mink) {
        maxk++;
        int rkamas = (int) (Math.random() * (maxk - mink)) + mink;
        return rkamas * Configuracion.INSTANCE.getRATE_KAMAS();
    }

    public static int getKamasWinPerco(int maxk, int mink) {
        maxk++;
        int rkamas = (int) (Math.random() * (maxk - mink)) + mink;
        return rkamas * Configuracion.INSTANCE.getRATE_KAMAS();
    }

    public static Doble<Integer, Integer> decompPierreAme(ObjetoJuego toDecomp) {
        Doble<Integer, Integer> toReturn;
        String[] stats = toDecomp.parseStatsString().split("#");
        int lvlMax = Integer.parseInt(stats[3], 16);
        int chance = Integer.parseInt(stats[1], 16);
        toReturn = new Doble<>(chance, lvlMax);

        return toReturn;
    }

    public static int totalCaptChance(int pierreChance, Jugador p) {
        int sortChance = switch (p.getSortStatBySortIfHas(413).getLevel()) {
            case 1 -> 1;
            case 2 -> 3;
            case 3 -> 6;
            case 4 -> 10;
            case 5 -> 15;
            case 6 -> 25;
            default -> 0;
        };

        return sortChance + pierreChance;
    }

    public static int spellCost(int nb) {
        int total = 0;
        for (int i = 1; i < nb; i++) {
            total += i;
        }

        return total;
    }

    public static int getLoosEnergy(int lvl, boolean isAgression,
                                    boolean isPerco) {

        int returned = 5 * lvl;
        if (isAgression)
            returned *= (7.0 / 4);
        if (isPerco)
            returned *= (3.0 / 2);
        return returned;
    }

    public static int totalAppriChance(boolean Amande, boolean Rousse,
                                       boolean Doree, Jugador p) {
        int sortChance = 0;
        int ddChance = 0;
        switch (p.getSortStatBySortIfHas(414).getLevel()) {
            case 1 -> sortChance = 15;
            case 2 -> sortChance = 20;
            case 3 -> sortChance = 25;
            case 4 -> sortChance = 30;
            case 5 -> sortChance = 35;
            case 6 -> sortChance = 45;
        }
        if (Amande || Rousse)
            ddChance = 15;
        if (Doree)
            ddChance = 5;
        return sortChance + ddChance;
    }

    public static int getCouleur(boolean Amande, boolean Rousse, boolean Doree) {
        int Couleur = 0;
        if (Amande && !Rousse && !Doree)
            return 20;
        if (Rousse && !Amande && !Doree)
            return 10;
        if (Doree && !Amande && !Rousse)
            return 18;

        if (Amande && Rousse && !Doree) {
            int Chance = Formulas.getRandomValue(1, 2);
            if (Chance == 1)
                return 20;
            if (Chance == 2)
                return 10;
        }
        if (Amande && !Rousse && Doree) {
            int Chance = Formulas.getRandomValue(1, 2);
            if (Chance == 1)
                return 20;
            if (Chance == 2)
                return 18;
        }
        if (!Amande && Rousse && Doree) {
            int Chance = Formulas.getRandomValue(1, 2);
            if (Chance == 1)
                return 18;
            if (Chance == 2)
                return 10;
        }
        if (Amande && Rousse && Doree) {
            int Chance = Formulas.getRandomValue(1, 3);
            if (Chance == 1)
                return 20;
            if (Chance == 2)
                return 10;
            if (Chance == 3)
                return 18;
        }
        return Couleur;
    }

    public static int calculEnergieLooseForToogleMount(int pts) {
        if (pts <= 170)
            return 4;
        if (pts >= 171 && pts < 180)
            return 5;
        if (pts >= 180 && pts < 200)
            return 6;
        if (pts >= 200 && pts < 210)
            return 7;
        if (pts >= 210 && pts < 220)
            return 8;
        if (pts >= 220 && pts < 230)
            return 10;
        if (pts >= 230 && pts <= 240)
            return 12;
        return 10;
    }

    public static int getLvlDopeuls(int lvl)//Niveau du dopeul � combattre
    {
        if (lvl < 20)
            return 20;
        if (lvl > 19 && lvl < 40)
            return 40;
        if (lvl > 39 && lvl < 60)
            return 60;
        if (lvl > 59 && lvl < 80)
            return 80;
        if (lvl > 79 && lvl < 100)
            return 100;
        if (lvl > 99 && lvl < 120)
            return 120;
        if (lvl > 119 && lvl < 140)
            return 140;
        if (lvl > 139 && lvl < 160)
            return 160;
        if (lvl > 159 && lvl < 180)
            return 180;
        if (lvl > 180)
            return 200;
        return 200;
    }

    public static int calculChanceByElement(int lvlJob, int lvlObject,
                                            int lvlRune) {
        int K = 1;
        if (lvlRune == 1)
            K = 100;
        else if (lvlRune == 25)
            K = 175;
        else if (lvlRune == 50)
            K = 350;
        return lvlJob * 100 / (K + lvlObject);
    }

    public static ArrayList<Integer> chanceFM(int WeightTotalBase,
                                                        int WeightTotalBaseMin, int currentWeithTotal,
                                                        int currentWeightStats, int weight, int diff, float coef,
                                                        int maxStat, int minStat, int actualStat, float x,
                                                        boolean bonusRune, int statsAdd) {
        ArrayList<Integer> chances = new ArrayList<>();
        float c = 1, m1 = (maxStat - (actualStat + statsAdd)), m2 = (maxStat - minStat);
        if ((1 - (m1 / m2)) > 1.0)
            c = (1 - ((1 - (m1 / m2)) / 2)) / 2;
        else if ((1 - (m1 / m2)) > 0.8)
            c = 1 - ((1 - (m1 / m2)) / 2);
        if (c < 0)
            c = 0;
        // la variable c reste � 1 si le jet ne depasse pas 80% sinon il diminue tr�s fortement. Si le jet d�passe 100% alors il diminue encore plus.

        int moyenne = (int) Math.floor(WeightTotalBase
                - ((WeightTotalBase - WeightTotalBaseMin) / 2));

        float mStat = ((float) moyenne / (float) currentWeithTotal); // Si l'item est un bon jet dans l'ensemble, diminue les chances sinon l'inverse.

        if (mStat > 1.2)
            mStat = 1.2F;
        float a = ((((((WeightTotalBase + diff) * coef) * mStat) * c) * x));
        float b = (float) (Math.sqrt(currentWeithTotal + currentWeightStats) + weight);
        if (b < 1.0)
            b = 1.0F;

        int p1 = (int) Math.floor(a / b); // Succes critique
        int p2 = 0; // Succes neutre
        int p3 = 0; // Echec critique
        if (bonusRune)
            p1 += 20;
        if (p1 < 1) {
            p1 = 1;
            p2 = 0;
            p3 = 99;
        } else if (p1 > 100) {
            p1 = 66;
            p2 = 34;
        } else if (p1 > 66)
            p1 = 66;

        if (p2 == 0 && p3 == 0) {
            p2 = (int) Math.floor(a
                    / (Math.sqrt(currentWeithTotal + currentWeightStats)));
            if (p2 > (100 - p1))
                p2 = (100 - p1);
            if (p2 > 50)
                p2 = 50;
        }
        chances.add(0, p1);
        chances.add(1, p2);
        chances.add(2, p3);
        return chances;
    }

    public static String convertToDate(long time) {
        String hexDate = "#";
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = formatter.format(time);

        String[] split = date.split("\\s");

        String[] split0 = split[0].split("-");
        hexDate += Integer.toHexString(Integer.parseInt(split0[0])) + "#";
        int mois = Integer.parseInt(split0[1]) - 1;
        int jour = Integer.parseInt(split0[2]);
        hexDate += Integer.toHexString(Integer.parseInt((mois < 10 ? "0" + mois : mois)
                + "" + (jour < 10 ? "0" + jour : jour)))
                + "#";

        String[] split1 = split[1].split(":");
        String heure = split1[0] + split1[1];
        hexDate += Integer.toHexString(Integer.parseInt(heure));
        return hexDate;
    }

    public static int getXpStalk(int lvl) {
        return switch (lvl) {
            case 50, 51, 52, 53, 54, 55, 56, 57, 58, 59 -> 65000;
            case 60, 61, 62, 63, 64, 65, 66, 67, 68, 69 -> 90000;
            case 70, 71, 72, 73, 74, 75, 76, 77, 78, 79 -> 120000;
            case 80, 81, 82, 83, 84, 85, 86, 87, 88, 89 -> 160000;
            case 90, 91, 92, 93, 94, 95, 96, 97, 98, 99 -> 210000;
            case 100, 101, 102, 103, 104, 105, 106, 107, 108, 109 -> 270000;
            case 110, 111, 112, 113, 114, 115, 116, 117, 118, 119 -> 350000;
            case 120, 121, 122, 123, 124, 125, 126, 127, 128, 129 -> 440000;
            case 130, 131, 132, 133, 134, 135, 136, 137, 138, 139 -> 540000;
            case 140, 141, 142, 143, 144, 145, 146, 147, 148, 149 -> 650000;
            case 150, 151, 152, 153, 154 -> 760000;
            case 155, 156, 157, 158, 159 -> 880000;
            case 160, 161, 162, 163, 164 -> 1000000;
            case 165, 166, 167, 168, 169 -> 1130000;
            case 170, 171, 172, 173, 174 -> 1300000;
            case 175, 176, 177, 178, 179 -> 1500000;
            case 180, 181, 182, 183, 184 -> 1700000;
            case 185, 186, 187, 188, 189 -> 2000000;
            case 190, 191, 192, 193, 194 -> 2500000;
            case 195, 196, 197, 198, 199, 200 -> 3000000;
            default -> 65000;
        };
    }

    public static String translateMsg(String msg) {
        String alpha = "a b c d e f g h i j k l n o p q r s t u v w x y z é è à ç & û â ê ô î ä ë ü ï ö";
        for (String i : alpha.split(" "))
            msg = msg.replace(i, "m");
        alpha = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z Ë Ü Ä Ï Ö Â Ê Û Î Ô";
        for (String i : alpha.split(" "))
            msg = msg.replace(i, "H");
        return msg;
    }

    public static boolean checkLos(Mapa map, short castID, short targetID) {
        if (map == null || castID == targetID) {
            return true;
        }
        CellCacheImpl cache = map.getCellCache();
        if (cache == null) {
            return false;
        }
        float curX = cache.getOrthX(castID);
        float curY = cache.getOrthY(castID);
        int dstX = cache.getOrthX(targetID);
        int dstY = cache.getOrthY(targetID);
        int offX = dstX - (int)curX;
        int offY = dstY - (int)curY;
        float steps = cache.getCellsDistance(castID, targetID);
        steps = Math.max(1.0f, steps);
        curX = (float)((double)curX + 0.5);
        curY = (float)((double)curY + 0.5);
        int t = 0;
        while ((float)t < steps) {
            Mapa.GameCase cell;
            int xFloored = (int)Math.floor(curX);
            int yFloored = (int)Math.floor(curY);
            short cellId = (short)cache.getOrthCellID(xFloored, yFloored);
            if ((curX != (float)xFloored || curY != (float)yFloored) && cellId != targetID && cellId != castID && (cell = map.getCase(cellId)) != null) {
                Peleador fighter = cell.getFirstFighter();
                if (!cell.isLoS() || fighter != null && !fighter.isHide()) {
                    return false;
                }
            }
            ++t;
            curX += (float)offX / steps;
            curY += (float)offY / steps;
        }
        return true;
    }
}