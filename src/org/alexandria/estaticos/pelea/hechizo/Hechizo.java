package org.alexandria.estaticos.pelea.hechizo;

import org.alexandria.comunes.Camino;
import org.alexandria.comunes.Formulas;
import org.alexandria.configuracion.Constantes;
import org.alexandria.configuracion.MainServidor;
import org.alexandria.estaticos.juego.JuegoServidor;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.Retos;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;

import java.util.*;
import java.util.Map.Entry;

public class Hechizo {

    private String nombre;
    private int spellID;
    private int spriteID;
    private String spriteInfos;
    private Map<Integer, SortStats> sortStats = new HashMap<>();
    private ArrayList<Integer> effectTargets = new ArrayList<>();
    private ArrayList<Integer> CCeffectTargets = new ArrayList<>();
    private List<Byte> invalidStates;
    private List<Byte> neededStates;
    private int type, duration;

    public Hechizo(int aspellID, String aNombre, int aspriteID,
                   String aspriteInfos, String ET, int type, int duration) {
        spellID = aspellID;
        nombre = aNombre;
        spriteID = aspriteID;
        spriteInfos = aspriteInfos;
        this.duration = duration;
        if (ET.equalsIgnoreCase("0")) {
            effectTargets.add(0);
            CCeffectTargets.add(0);
        } else {
            String nET = ET.split(":")[0];
            String ccET = "";
            if (ET.split(":").length > 1)
                ccET = ET.split(":")[1];
            for (String num : nET.split(";")) {
                try {
                    effectTargets.add(Integer.parseInt(num));
                } catch (Exception e) {
                    // ok
                    effectTargets.add(0);
                }
            }
            for (String num : ccET.split(";")) {
                try {
                    CCeffectTargets.add(Integer.parseInt(num));
                } catch (Exception e) {
                    // ok
                    CCeffectTargets.add(0);
                }
            }
        }
        this.type = type;
    }

    public void setInfos(int aspriteID, String aspriteInfos, String ET, int type, int duration) {
        spriteID = aspriteID;
        spriteInfos = aspriteInfos;
        String nET = ET.split(":")[0];
        String ccET = "";
        this.type = type;
        this.duration = duration;
        if (ET.split(":").length > 1)
            ccET = ET.split(":")[1];
        effectTargets.clear();
        for (String num : nET.split(";")) {
            try {
                effectTargets.add(Integer.parseInt(num));
            } catch (Exception e) {
                // ok
                effectTargets.add(0);
            }
        }
        for (String num : ccET.split(";")) {
            try {
                CCeffectTargets.add(Integer.parseInt(num));
            } catch (Exception e) {
                // ok
                CCeffectTargets.add(0);
            }
        }
    }

    public ArrayList<Integer> getEffectTargets() {
        return effectTargets;
    }

    public int getSpriteID() {
        return spriteID;
    }

    public String getSpriteInfos() {
        return spriteInfos;
    }

    public int getSpellID() {
        return spellID;
    }

    public SortStats getStatsByLevel(int lvl) {
        return sortStats.get(lvl);
    }

    public String getNombre() {
        return nombre;
    }

    public Map<Integer, SortStats> getSortsStats() {
        return sortStats;
    }

    public void addSortStats(Integer lvl, SortStats stats) {
        if (sortStats.get(lvl) != null)
            sortStats.remove(lvl);
        sortStats.put(lvl, stats);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getDuration() {
        return duration;
    }

    public boolean hasInvalidState(Peleador fighter) {
        if (this.invalidStates != null) {
            for (Byte invalidState : this.invalidStates) {
                byte state = invalidState.byteValue();
                if (!fighter.haveState(state)) continue;
                return true;
            }
        }
        return false;
    }

    public boolean hasNeededState(Peleador fighter) {
        boolean ok = true;
        if (this.neededStates != null) {
            for (Byte neededState : this.neededStates) {
                byte state = neededState.byteValue();
                if (fighter.haveState(state)) continue;
                ok = false;
            }
        }
        return ok;
    }

    public static class SortStats {

        private int spellID;
        private int level;
        private int PACost;
        private int minPO;
        private int maxPO;
        private int TauxCC;
        private int TauxEC;
        private boolean isLineLaunch;
        private boolean hasLDV;
        private boolean isEmptyCell;
        private boolean isModifPO;
        private int maxLaunchbyTurn;
        private int maxLaunchbyByTarget;
        private int coolDown;
        private int reqLevel;
        private boolean isEcEndTurn;
        private ArrayList<EfectoHechizo> effects;
        private ArrayList<EfectoHechizo> CCeffects;
        private String porteeType;

        public SortStats(int AspellID, int Alevel, int cost, int minPO,
                         int maxPO, int tauxCC, int tauxEC, boolean isLineLaunch,
                         boolean hasLDV, boolean isEmptyCell, boolean isModifPO,
                         int maxLaunchbyTurn, int maxLaunchbyByTarget, int coolDown,
                         int reqLevel, boolean isEcEndTurn, String effects,
                         String ceffects, String typePortee) {
            //effets, effetsCC, PaCost, PO Min, PO Max, Taux CC, Taux EC, line, LDV, emptyCell, PO Modif, maxByTurn, maxByTarget, Cooldown, type, level, endTurn
            this.spellID = AspellID;
            this.level = Alevel;
            this.PACost = cost;
            this.minPO = minPO;
            this.maxPO = maxPO;
            this.TauxCC = tauxCC;
            this.TauxEC = tauxEC;
            this.isLineLaunch = isLineLaunch;
            this.hasLDV = hasLDV;
            this.isEmptyCell = isEmptyCell;
            this.isModifPO = isModifPO;
            this.maxLaunchbyTurn = maxLaunchbyTurn;
            this.maxLaunchbyByTarget = maxLaunchbyByTarget;
            this.coolDown = coolDown;
            this.reqLevel = reqLevel;
            this.isEcEndTurn = isEcEndTurn;
            this.effects = parseEffect(effects);
            this.CCeffects = parseEffect(ceffects);
            this.porteeType = typePortee;
        }

        private ArrayList<EfectoHechizo> parseEffect(String e) {
            ArrayList<EfectoHechizo> effets = new ArrayList<>();
            String[] splt = e.split("\\|");
            for (String a : splt) {
                try {
                    if (e.equals("-1"))
                        continue;
                    int id = Integer.parseInt(a.split(";", 2)[0]);
                    String args = a.split(";", 2)[1];
                    effets.add(new EfectoHechizo(id, args, spellID, level));
                } catch (Exception f) {
                    f.printStackTrace();
                    MainServidor.INSTANCE.stop("parseEffect spell");
                }
            }
            return effets;
        }

        public int getSpellID() {
            return spellID;
        }

        public Hechizo getSpell() {
            return Mundo.mundo.getSort(spellID);
        }

        public int getSpriteID() {
            return getSpell().getSpriteID();
        }

        public String getSpriteInfos() {
            return getSpell().getSpriteInfos();
        }

        public int getLevel() {
            return level;
        }

        public int getPACost() {
            return PACost;
        }

        public int getMinPO() {
            return minPO;
        }

        public int getMaxPO() {
            return maxPO;
        }

        public int getTauxCC() {
            return TauxCC;
        }

        public int getTauxEC() {
            return TauxEC;
        }

        public boolean isLineLaunch() {
            return isLineLaunch;
        }

        public boolean hasLDV() {
            return hasLDV;
        }

        public boolean isEmptyCell() {
            return isEmptyCell;
        }

        public boolean isModifPO() {
            return isModifPO;
        }

        public int getMaxLaunchbyTurn() {
            return maxLaunchbyTurn;
        }

        public int getMaxLaunchByTarget() {
            return maxLaunchbyByTarget;
        }

        public int getCoolDown() {
            return coolDown;
        }

        public int getReqLevel() {
            return reqLevel;
        }

        public boolean isEcEndTurn() {
            return isEcEndTurn;
        }

        public ArrayList<EfectoHechizo> getEffects() {
            return effects;
        }

        public ArrayList<EfectoHechizo> getCCeffects() {
            return CCeffects;
        }

        public String getPorteeType() {
            return porteeType;
        }

        public void applySpellEffectToFight(Pelea fight, Peleador perso, GameCase cell, ArrayList<GameCase> cells, boolean isCC) {
            // Seulement appell� par les pieges, or les sorts de piege
            ArrayList<EfectoHechizo> effets;
            if (isCC)
                effets = CCeffects;
            else
                effets = effects;
            JuegoServidor.a();
            int jetChance = Formulas.getRandomValue(0, 99);
            int curMin = 0;
            for (EfectoHechizo SE : effets) {
                if (SE.getChance() != 0 && SE.getChance() != 100)// Si pas 100%
                {
                    if (jetChance <= curMin || jetChance >= (SE.getChance() + curMin)) {
                        curMin += SE.getChance();
                        continue;
                    }
                    curMin += SE.getChance();
                }
                ArrayList<Peleador> cibles = EfectoHechizo.getTargets(SE, fight, cells);

                if ((fight.getType() != Constantes.FIGHT_TYPE_CHALLENGE)
                        && (fight.getAllChallenges().size() > 0)) {
                    for (Entry<Integer, Retos> c : fight.getAllChallenges().entrySet()) {
                        if (c.getValue() == null)
                            continue;
                        c.getValue().onFightersAttacked(cibles, perso, SE, this.getSpellID(), true);
                    }
                }

                SE.applyToFight(fight, perso, cell, cibles);
            }
        }

        public void applySpellEffectToFight(Pelea fight, Peleador perso,
                                            GameCase cell, boolean isCC, boolean isTrap) {
            ArrayList<EfectoHechizo> effets;
            if (isCC)
                effets = CCeffects;
            else
                effets = effects;
            JuegoServidor.a();
            int jetChance = 0;
            if (this.getSpell().getSpellID() == 101) //Si este es ruleta
            {
                jetChance = Formulas.getRandomValue(0, 50); //0 a 50% ruleta
                if (jetChance % 2 == 0)
                    jetChance++;
            } else if (this.getSpell().getSpellID() == 574) // Si c'est Ouverture hasardeuse fant�me
                jetChance = Formulas.getRandomValue(0, 96);
            else if (this.getSpell().getSpellID() == 574) // Si c'est Ouverture hasardeuse
                jetChance = Formulas.getRandomValue(0, 95);
            else
                jetChance = Formulas.getRandomValue(0, 99);
            int curMin = 0;
            int num = 0;
            for (EfectoHechizo SE : effets) {
                try {
                    if (fight.getState() >= Constantes.ESTADO_FIN_DE_PELEA)
                        return;
                    if (SE.getChance() != 0 && SE.getChance() != 100)// Si pas 100%
                    {
                        if (jetChance <= curMin
                                || jetChance >= (SE.getChance() + curMin)) {
                            curMin += SE.getChance();
                            num++;
                            continue;
                        }
                        curMin += SE.getChance();
                    }
                    int POnum = num * 2;
                    if (isCC) {
                        POnum += effects.size() * 2;// On zaap la partie du String des effets hors CC
                    }
                    ArrayList<GameCase> cells = Camino.getCellListFromAreaString(fight.getMap(), cell.getId(), perso.getCell().getId(), porteeType, POnum, isCC);
                    ArrayList<GameCase> finalCells = new ArrayList<>();
                    int TE = 0;
                    Hechizo S = Mundo.mundo.getSort(spellID);
                    // on prend le targetFlag corespondant au num de l'effet
                    if (S != null && S.getEffectTargets().size() > num)
                        TE = S.getEffectTargets().get(num);

                    for (GameCase C : cells) {
                        if (C == null)
                            continue;
                        Peleador F = C.getFirstFighter();
                        if (F == null)
                            continue;
                        // Ne touches pas les alli�s : 1
                        if (((TE & 1) == 1) && (F.getTeam() == perso.getTeam()))
                            continue;
                        // Ne touche pas le lanceur : 2
                        if ((((TE >> 1) & 1) == 1) && (F.getId() == perso.getId()))
                            continue;
                        // Ne touche pas les ennemies : 4
                        if ((((TE >> 2) & 1) == 1) && (F.getTeam() != perso.getTeam()))
                            continue;
                        // Ne touche pas les combatants (seulement invocations) : 8
                        if ((((TE >> 3) & 1) == 1) && (!F.isInvocation()))
                            continue;
                        // Ne touche pas les invocations : 16
                        if ((((TE >> 4) & 1) == 1) && (F.isInvocation()))
                            continue;
                        // N'affecte que le lanceur : 32
                        if ((((TE >> 5) & 1) == 1) && (F.getId() != perso.getId()))
                            continue;
                        // N'affecte que les alliés (pas le lanceur) : 64
                        if ((((TE >> 6) & 1) == 1) && (F.getTeam() != perso.getTeam() || F.getId() == perso.getId()))
                            continue;
                        // N'affecte PERSONNE : 1024
                        if ((((TE >> 10) & 1) == 1))
                            continue;
                        // Si pas encore eu de continue, on ajoute la case, tout le monde : 0
                        finalCells.add(C);
                    }
                    // Si le sort n'affecte que le lanceur et que le lanceur n'est
                    // pas dans la zone

                    if (((TE >> 5) & 1) == 1)
                        if (!finalCells.contains(perso.getCell()))
                            finalCells.add(perso.getCell());
                    ArrayList<Peleador> cibles = EfectoHechizo.getTargets(SE, fight, finalCells);

                    if ((fight.getType() != Constantes.FIGHT_TYPE_CHALLENGE)
                            && (fight.getAllChallenges().size() > 0)) {
                        for (Entry<Integer, Retos> c : fight.getAllChallenges().entrySet()) {
                            if (c.getValue() == null)
                                continue;
                            c.getValue().onFightersAttacked(cibles, perso, SE, this.getSpellID(), isTrap);
                        }
                    }
                    SE.applyToFight(fight, perso, cell, cibles);
                    num++;
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}