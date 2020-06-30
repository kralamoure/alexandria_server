package org.alexandria.estaticos.pelea;

import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.estaticos.cliente.Jugador.Caracteristicas;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Constantes;
import org.alexandria.estaticos.Prisma;
import org.alexandria.estaticos.Recaudador;
import org.alexandria.estaticos.Monstruos;
import org.alexandria.estaticos.Gremio;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.pelea.hechizo.EfectoHechizo;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.pelea.hechizo.LanzarHechizo;
import org.alexandria.otro.utilidad.Doble;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Peleador implements Comparable<Peleador> {

    private final Logger logger;
    public int nbrInvoc;
    public boolean inLancer = false;
    public boolean isStatique = false;
    private int id = 0;
    private boolean canPlay = false;
    private final Pelea fight;
    private int type = 0;                                // 1 : Personnage, 2 : Mob, 5 : Perco
    private Monstruos.MobGrade mob = null;
    private Jugador perso = null;
    private Jugador _double = null;
    private Recaudador collector = null;
    private Prisma prism = null;
    private int team = -2;
    private GameCase cell;
    private int pdvMax;
    private int pdv;
    private boolean isDead;
    private boolean hasLeft;
    private final int gfxId;
    private Peleador isHolding;
    private Peleador holdedBy;
    private Peleador oldCible = null;
    private Peleador invocator;
    private boolean levelUp = false;
    private boolean isDeconnected = false;
    private int turnRemaining = 0;
    private int nbrDisconnection = 0;
    private boolean isTraqued = false;
    private Caracteristicas stats;
    private final Map<Integer, Integer> state = new HashMap<>();
    private final ArrayList<EfectoHechizo> fightBuffs = new ArrayList<>();
    private final Map<Integer, Integer> chatiValue = new HashMap<>();
    private final ArrayList<LanzarHechizo> launchedSpell = new ArrayList<>();
    public Doble<Byte, Long> killedBy;
    private boolean hadSober = false;
    private boolean justTrapped = false;

    public Peleador(Pelea f, Monstruos.MobGrade mob) {
        this.fight = f;
        this.type = 2;
        this.mob = mob;
        setId(mob.getInFightID());
        this.pdvMax = mob.getPdvMax();
        this.pdv = mob.getPdv();
        this.gfxId = getDefaultGfx();
        logger = LoggerFactory.getLogger("FighterMob."+mob.getInFightID());
    }

    public Peleador(Pelea f, Jugador player) {
        this.fight = f;
        if (player._isClone) {
            this.type = 10;
            setDouble(player);
        } else {
            this.type = 1;
            this.perso = player;
        }
        setId(player.getId());
        this.pdvMax = player.getMaxPdv();
        this.pdv = player.getCurPdv();
        this.gfxId = getDefaultGfx();
        logger = LoggerFactory.getLogger("FighterPlayer." + player.getName());
    }

    public Peleador(Pelea f, Recaudador collector) {
        this.fight = f;
        this.type = 5;
        setCollector(collector);
        setId(-1);
        this.pdvMax = (Mundo.mundo.getGuild(collector.getGuildId()).getLvl() * 100);
        this.pdv = (Mundo.mundo.getGuild(collector.getGuildId()).getLvl() * 100);
        this.gfxId = 6000;
        logger = LoggerFactory.getLogger("FighterCollector." + collector.getFullName());
    }

    public Peleador(Pelea Fight, Prisma Prisme) {
        this.fight = Fight;
        this.type = 7;
        setPrism(Prisme);
        setId(-1);
        this.pdvMax = Prisme.getLevel() * 10000;
        this.pdv = Prisme.getLevel() * 10000;
        this.gfxId = Prisme.getAlignement() == 1 ? 8101 : 8100;
        Prisme.refreshStats();
        logger = LoggerFactory.getLogger("FighterPrism." + prism.getAlignement());
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean canPlay() {
        return this.canPlay;
    }

    public void setCanPlay(boolean canPlay) {
        this.canPlay = canPlay;
    }

    public Pelea getFight() {
        return this.fight;
    }

    public int getType() {
        return this.type;
    }

    public Monstruos.MobGrade getMob() {
        if (this.type == 2)
            return this.mob;
        return null;
    }

    public boolean isMob() {
        return (this.mob != null);
    }

    public boolean getComandoPasarTurno() {
        if (getPlayer() != null) {
            return getPlayer().getComandoPasarTurno();
        }
        return false;
    }

    public Jugador getPlayer() {
        if (this.type == 1)
            return this.perso;
        return null;
    }

    public Jugador getDouble() {
        return _double;
    }

    public boolean isDouble() {
        return (this._double != null);
    }

    public void setDouble(Jugador _double) {
        this._double = _double;
    }

    public Recaudador getCollector() {
        if (this.type == 5)
            return this.collector;
        return null;
    }

    public boolean isCollector() {
        return (this.collector != null);
    }

    public void setCollector(Recaudador collector) {
        this.collector = collector;
    }

    public Prisma getPrism() {
        if (this.type == 7)
            return this.prism;
        return null;
    }

    public void setPrism(Prisma prism) {
        this.prism = prism;
    }

    public boolean isPrisme() {
        return (this.prism != null);
    }

    public int getTeam() {
        return this.team;
    }

    public void setTeam(int i) {
        this.team = i;
    }

    public int getTeam2() {
        return this.fight.getTeamId(getId());
    }

    public int getOtherTeam() {
        return this.fight.getOtherTeamId(getId());
    }

    public GameCase getCell() {
        return this.cell;
    }

    public void setCell(GameCase cell) {
        this.cell = cell;
    }

    public int getPdvMax() {
        return this.pdvMax + getBuffValue(Constantes.STATS_ADD_VITA);
    }

    public void removePdvMax(int pdv) {
        this.pdvMax = this.pdvMax - pdv;
        if (this.pdv > this.pdvMax)
            this.pdv = this.pdvMax;
    }

    public int getPdv() {
        return (this.pdv + getBuffValue(Constantes.STATS_ADD_VITA));
    }

    public void setPdvMax(int pdvMax) {
        this.pdvMax = pdvMax;
    }

    public void setPdv(int pdv) {
        this.pdv = pdv;
        if(this.pdv > this.pdvMax)
            this.pdv = this.pdvMax;
    }

    public void removePdv(Peleador caster, int pdv) {
        if (pdv > 0)
            this.getFight().getAllChallenges().values().stream().filter(Objects::nonNull).forEach(challenge -> challenge.onFighterAttacked(caster, this));
        this.pdv -= pdv;
    }

    public void fullPdv() {
        this.pdv = this.pdvMax;
    }

    public boolean isFullPdv() {
        return this.pdv == this.pdvMax;
    }

    public boolean isDead() {
        return this.isDead;
    }

    public void setIsDead(boolean isDead) {
        this.isDead = isDead;
    }

    public boolean hasLeft() {
        return this.hasLeft;
    }

    public void setLeft(boolean hasLeft) {
        this.hasLeft = hasLeft;
    }

    public Peleador getIsHolding() {
        return this.isHolding;
    }

    public void setIsHolding(Peleador isHolding) {
        this.isHolding = isHolding;
    }

    public Peleador getHoldedBy() {
        return this.holdedBy;
    }

    public void setHoldedBy(Peleador holdedBy) {
        this.holdedBy = holdedBy;
    }

    public Peleador getOldCible() {
        return this.oldCible;
    }

    public void setOldCible(Peleador cible) {
        this.oldCible = cible;
    }

    public Peleador getInvocator() {
        return this.invocator;
    }

    public void setInvocator(Peleador invocator) {
        this.invocator = invocator;
    }

    public boolean isInvocation() {
        return (this.invocator != null);
    }

    public boolean getLevelUp() {
        return this.levelUp;
    }

    public void setLevelUp(boolean levelUp) {
        this.levelUp = levelUp;
    }

    public void Disconnect() {
        if (this.isDeconnected)
            return;
        this.isDeconnected = true;
        this.turnRemaining = 20;
        this.nbrDisconnection++;
    }

    public void Reconnect() {
        this.isDeconnected = false;
        this.turnRemaining = 0;
    }

    public boolean isDeconnected() {
        return !this.hasLeft && this.isDeconnected;
    }

    public int getTurnRemaining() {
        return this.turnRemaining;
    }

    public void setTurnRemaining() {
        this.turnRemaining--;
    }

    public int getNbrDisconnection() {
        return this.nbrDisconnection;
    }

    public boolean getTraqued() {
        return this.isTraqued;
    }

    public void setTraqued(boolean isTraqued) {
        this.isTraqued = isTraqued;
    }

    public void setState(int id, int t, int casterId) {
        if(t!=0) {
            if(state.get(id)!=null) { //Si el peleador esta en algun estado
                if(state.get(id)==-1||state.get(id)>t) //Evitamos bucle de infinidad de estado
                    return;
                else {
                    state.remove(id);
                    state.put(id,t);
                }
            } else {
                state.put(id,t);
            }
        } else {
            this.state.remove(id);
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950, String.valueOf(casterId), this.getId() +","+id+",0");
        }
    }

    public int getState(int id) {
        return this.state.get(id) != null ? this.state.get(id) : -1;
    }

    public boolean haveState(int id) {
        return this.state.get(id) != null && this.state.get(id) != 0;
    }

    public void sendState(Jugador p) {
        if (p.getAccount() != null && p.getGameClient() != null)
            for (Entry<Integer, Integer> state : this.state.entrySet())
                GestorSalida.GAME_SEND_GA_PACKET(p.getGameClient(), 7 + "", 950 + "", getId() + "", getId() + "," + state.getKey() + ",1");
    }

    public boolean haveInvocation() {
        for (Entry<Integer, Peleador> entry : this.getFight().getTeam(this.getTeam2()).entrySet()) {
            Peleador f = entry.getValue();
            if (f.isInvocation())
                if (f.getInvocator() == this)
                    return true;
        }
        return false;
    }

    public int nbInvocation() {
        int i = 0;
        for (Entry<Integer, Peleador> entry : this.getFight().getTeam(this.getTeam2()).entrySet()) {
            Peleador f = entry.getValue();
            if (f.isInvocation() && !f.isStatique)
                if (f.getInvocator() == this)
                    i++;
        }
        return i;
    }

    public ArrayList<EfectoHechizo> getFightBuff() {
        return this.fightBuffs;
    }

    private Caracteristicas getFightBuffStats() {
        Caracteristicas stats = new Caracteristicas();
        for (EfectoHechizo entry : this.fightBuffs)
            stats.addOneStat(entry.getEffectID(), entry.getValue());
        return stats;
    }

    public int getBuffValue(int id) {
        int value = 0;
        for (EfectoHechizo entry : this.fightBuffs)
            if (entry.getEffectID() == id)
                value += entry.getValue();
        return value;
    }

    public EfectoHechizo getBuff(int id) {
        for (EfectoHechizo entry : this.fightBuffs)
            if (entry.getEffectID() == id && entry.getDuration() > 0)
                return entry;
        return null;
    }



    public ArrayList<EfectoHechizo> getBuffsByEffectID(int effectID) {
        return this.fightBuffs.stream().filter(buff -> buff.getEffectID() == effectID).collect(Collectors.toCollection(ArrayList::new));
    }

    public Caracteristicas getTotalStatsLessBuff() {
        Caracteristicas stats = new Caracteristicas(new HashMap<>());
        if (this.type == 1)
            stats = this.perso.getTotalStats();
        if (this.type == 2)
            if(this.stats == null)
                this.stats = this.mob.getStats();
        if (this.type == 5)
            stats = Mundo.mundo.getGuild(getCollector().getGuildId()).getStatsFight();
        if (this.type == 7)
            stats = getPrism().getStats();
        if (this.type == 10)
            stats = getDouble().getTotalStats();
        return stats;
    }

    public boolean hasBuff(int id) {
        for (EfectoHechizo entry : this.fightBuffs)
            if (entry.getEffectID() == id && entry.getDuration() > 0)
                return true;
        return false;
    }

    public void addBuff(int id, int val, int duration, int turns, boolean debuff, int spellID, String args, Peleador caster, boolean addingTurnIfCanPlay) {
        if(this.mob != null)
            for(int id1 : Constantes.STATIC_INVOCATIONS)
                if (id1 == this.mob.getTemplate().getId())
                    return;

        switch (spellID) {
            case 99, 5, 20, 127, 89, 126, 115, 192, 4, 1, 6, 14, 18, 7, 284, 197, 704, 168, 45, 159, 171, 167, 511, 513 -> debuff = true;
            case 431, 433, 437, 443, 441 -> debuff = false;
        }

        if(id == 606 || id == 607 || id == 608 || id == 609 || id == 611 || id == 125 || id == 114 ||
                (spellID == 197 && (id == 149 || id == 169 || id == 183 || id == 184 || id == 168 || id == 108))) {
            debuff = true;
        }


        //Si c'est le jouer actif qui s'autoBuff, on ajoute 1 a la durée
        this.fightBuffs.add(new EfectoHechizo(id,val,(addingTurnIfCanPlay && this.canPlay?duration+1:duration),turns,debuff,caster,args,spellID));
        logger.debug("Ajout du Buff "+id+" sur le personnage fighter ("+this.getId()+") val : "+val+" duration : "+duration+" turns : "+turns+" debuff : "+debuff+" spellid : "+spellID+" args : "+args+" !");

        // de X sur Y tours
        switch (id) {
            //Renvoie de sort
            case 6 -> GestorSalida.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, id, getId(), -1, val + "", "10", "", duration, spellID);
            //Chance éca
            case 79 -> {
                val = Integer.parseInt(args.split(";")[0]);
                String valMax = args.split(";")[1];
                String chance = args.split(";")[2];
                GestorSalida.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, id, getId(), val, valMax, chance, "", duration, spellID);
            }
            case 606, 607, 608, 609, 611 -> {
                String jet = args.split(";")[5];
                int min = Formulas.getMinJet(jet);
                int max = Formulas.getMaxJet(jet);
                GestorSalida.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, id, getId(), min, "" + max, "" + max, "", duration, spellID);
            }
            //Fait apparaitre message le temps de buff sacri Chatiment de X sur Y tours
            case 788 -> {
                val = Integer.parseInt(args.split(";")[1]);
                String valMax2 = args.split(";")[2];
                if (Integer.parseInt(args.split(";")[0]) == 108)
                    return;
                GestorSalida.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, id, getId(), val, "" + val, "" + valMax2, "", duration, spellID);
            }
            //MIN
            case 98, 107, 100, 108, 165, 781, 782 -> {
                val = Integer.parseInt(args.split(";")[0]);
                String valMax1 = args.split(";")[1];
                if (valMax1.compareTo("-1") == 0 || spellID == 82 || spellID == 94)
                    GestorSalida.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, id, getId(), val, "", "", "", duration, spellID);
                else if (valMax1.compareTo("-1") != 0)
                    GestorSalida.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, id, getId(), val, valMax1, "", "", duration, spellID);
            }
            default -> GestorSalida.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight, 7, id, getId(), val, "", "", "", duration, spellID);
        }
    }

    public void debuff() {
        Iterator<EfectoHechizo> it = this.fightBuffs.iterator();
        while (it.hasNext()) {
            EfectoHechizo spellEffect = it.next();

            switch (spellEffect.getSpell()) {
                case 437:
                case 431:
                case 433:
                case 443:
                case 441://Châtiments
                    continue;
                case 197://Puissance sylvestre
                case 52://Cupidité
                case 228://Etourderie mortelle (DC)
                    it.remove();
                    continue;
            }

            if (spellEffect.isDebuffabe()) it.remove();
            //On envoie les Packets si besoin
            switch (spellEffect.getEffectID()) {
                case Constantes.STATS_ADD_PA, Constantes.STATS_ADD_PA2 -> GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, 101, getId()
                        + "", getId() + ",-" + spellEffect.getValue());
                case Constantes.STATS_ADD_PM, Constantes.STATS_ADD_PM2 -> GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, 127, getId()
                        + "", getId() + ",-" + spellEffect.getValue());
            }
        }
        ArrayList<EfectoHechizo> array = new ArrayList<>(this.fightBuffs);
        if (!array.isEmpty()) {
            this.fightBuffs.clear();
            array.stream().filter(Objects::nonNull).forEach(spellEffect -> this.addBuff(spellEffect.getEffectID(), spellEffect.getValue(), spellEffect.getDuration(), spellEffect.getTurn(), spellEffect.isDebuffabe(), spellEffect.getSpell(), spellEffect.getArgs(), this, true));
        }

        if (this.perso != null && !this.hasLeft) // Envoie les stats au joueurs
            GestorSalida.GAME_SEND_STATS_PACKET(this.perso);
    }

    public void refreshEndTurnBuff() {
        Iterator<EfectoHechizo> it = this.fightBuffs.iterator();
        while (it.hasNext()) {
            EfectoHechizo entry = it.next();
            if (entry == null || entry.getCaster().isDead)
                continue;
            if (entry.decrementDuration() == 0) {
                it.remove();
                switch (entry.getEffectID()) {
                    case 108:
                        if (entry.getSpell() == 441) {
                            //Baisse des pdvs max
                            this.pdvMax = (this.pdvMax - entry.getValue());

                            //Baisse des pdvs actuel
                            int pdv = 0;
                            if (this.pdv - entry.getValue() <= 0) {
                                pdv = 0;
                                this.fight.onFighterDie(this, this.holdedBy);
                                this.fight.verifIfTeamAllDead();
                            } else
                                pdv = (this.pdv - entry.getValue());
                            this.pdv = pdv;
                        }
                        break;

                    case 150://Invisibilit�
                        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, 150, entry.getCaster().getId() + "", getId() + ",0");
                        break;

                    case 950:
                        String args = entry.getArgs();
                        int id = -1;
                        try {
                            id = Integer.parseInt(args.split(";")[2]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (id == -1)
                            return;
                        setState(id, 0, entry.getCaster().getId());
                        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, 950, entry.getCaster().getId()
                                + "", entry.getCaster().getId() + "," + id
                                + ",0");
                        break;
                }
            }
        }
    }

    public void initBuffStats() {
        if (this.type == 1)
            this.fightBuffs.addAll(new ArrayList<>(this.perso.get_buff().values()));
    }

    public void applyBeginningTurnBuff(Pelea fight) {
        for (int effectID : Constantes.BEGIN_TURN_BUFF) {
            ArrayList<EfectoHechizo> buffs = new ArrayList<>(this.fightBuffs);
            buffs.stream().filter(entry -> entry.getEffectID() == effectID).forEach(entry -> entry.applyBeginingBuff(fight, this));
        }
    }

    public ArrayList<LanzarHechizo> getLaunchedSorts() {
        return this.launchedSpell;
    }

    public void refreshLaunchedSort() {
        ArrayList<LanzarHechizo> copie = new ArrayList<>(this.launchedSpell);

        int i = 0;
        for (LanzarHechizo S : copie) {
            S.actuCooldown();
            if (S.getCooldown() <= 0) {
                this.launchedSpell.remove(i);
                i--;
            }
            i++;
        }
    }

    public void addLaunchedSort(Peleador target, Hechizo.SortStats sort, Peleador fighter) {
        LanzarHechizo launched = new LanzarHechizo(target, sort, fighter);
        this.launchedSpell.add(launched);
    }

    public Caracteristicas getTotalStats() {
        Caracteristicas stats = new Caracteristicas(new HashMap<>());
        if (this.type == 1)
            stats = this.perso.getTotalStats();
        if (this.type == 2)
            stats = this.mob.getStats();
        if (this.type == 5)
            stats = Mundo.mundo.getGuild(getCollector().getGuildId()).getStatsFight();
        if (this.type == 7)
            stats = this.getPrism().getStats();
        if (this.type == 10)
            stats = this.getDouble().getTotalStats();

        if(this.type != 1)
            stats = Caracteristicas.cumulStatFight(stats, getFightBuffStats());

        return stats;
    }

    public int getMaitriseDmg(int id) {
        int value = 0;
        for (EfectoHechizo entry : this.fightBuffs)
            if (entry.getSpell() == id)
                value += entry.getValue();
        return value;
    }

    public boolean getSpellValueBool(int id) {
        for (EfectoHechizo entry : this.fightBuffs)
            if (entry.getSpell() == id)
                return true;
        return false;
    }

    public boolean testIfCC(int tauxCC) {
        if (tauxCC < 2)
            return false;
        int agi = getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
        if (agi < 0)
            agi = 0;
        tauxCC -= getTotalStats().getEffect(Constantes.STATS_ADD_CC);
        tauxCC = (int) ((tauxCC * 2.9901) / Math.log(agi + 12));//Influence de l'agi
        if (tauxCC < 2)
            tauxCC = 2;
        int jet = Formulas.getRandomValue(1, tauxCC);
        return (jet == tauxCC);
    }

    public boolean testIfCC(int porcCC, Hechizo.SortStats sSort, Peleador fighter) {
        Jugador perso = fighter.getPlayer();
        if (porcCC < 2)
            return false;
        int agi = getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
        if (agi < 0)
            agi = 0;
        porcCC -= getTotalStats().getEffect(Constantes.STATS_ADD_CC);
        if (fighter.getType() == 1
                && perso.getObjectsClassSpell().containsKey(sSort.getSpellID())) {
            int modi = perso.getValueOfClassObject(sSort.getSpellID(), 287);
            porcCC -= modi;
        }
        porcCC = (int) ((porcCC * 2.9901) / Math.log(agi + 12));
        if (porcCC < 2)
            porcCC = 2;
        int jet = Formulas.getRandomValue(1, porcCC);
        return (jet == porcCC);
    }

    public int getInitiative() {
        if (this.type == 1)
            return this.perso.getInitiative();
        if (this.type == 2)
            return this.mob.getInit();
        if (this.type == 5)
            return Mundo.mundo.getGuild(getCollector().getGuildId()).getLvl();
        if (this.type == 7)
            return 0;
        if (this.type == 10)
            return getDouble().getInitiative();
        return 0;
    }

    public int getPa() {
        return switch (this.type) {
            case 1, 10 -> getTotalStats().getEffect(Constantes.STATS_ADD_PA);
            case 2 -> getTotalStats().getEffect(Constantes.STATS_ADD_PA)
                    + this.mob.getPa();
            case 5, 7 -> getTotalStats().getEffect(Constantes.STATS_ADD_PM) + 6;
            default -> 0;
        };
    }

    public int getPm() {
        return switch (this.type) {
            //Personaje
            case 1 -> getTotalStats().getEffect(Constantes.STATS_ADD_PM);
            //Monster
            case 2 -> getTotalStats().getEffect(Constantes.STATS_ADD_PM) + this.mob.getPm();
            //Recaudador
            case 5 -> getTotalStats().getEffect(Constantes.STATS_ADD_PM) + 4;
            //Prisma
            case 7 -> getTotalStats().getEffect(Constantes.STATS_ADD_PM);
            //Clon
            case 10 -> getTotalStats().getEffect(Constantes.STATS_ADD_PM);
            default -> 0;
        };
    }

    public int getPros() {
        switch (this.type) {
            case 1: // personnage
                return (getTotalStats().getEffect(Constantes.STATS_ADD_PROS) + Math.round(getTotalStats().getEffect(Constantes.STATS_ADD_CHAN) / 10) + Math.round(getBuffValue(Constantes.STATS_ADD_CHAN) / 10));
            case 2: // mob
                if (this.isInvocation()) // Si c'est un coffre anim�, la chance est �gale � 1000*(1+lvlinvocateur/100)
                    return (getTotalStats().getEffect(Constantes.STATS_ADD_PROS) + (1000 * (1 + this.getInvocator().getLvl() / 100)) / 10);
                else
                    return (getTotalStats().getEffect(Constantes.STATS_ADD_PROS) + Math.round(getBuffValue(Constantes.STATS_ADD_CHAN) / 10));
        }
        return 0;
    }

    public int getCurPa(Pelea fight) {
        return fight.getCurFighterPa();
    }

    public void setCurPa(Pelea fight, int pa) {
        fight.setCurFighterPa(fight.getCurFighterPa() + pa);
    }

    public int getCurPm(Pelea fight) {
        return fight.getCurFighterPm();
    }

    public void setCurPm(Pelea fight, int pm) {
        fight.setCurFighterPm(fight.getCurFighterPm() + pm);
    }

    public boolean canLaunchSpell(int spellID) {
        return this.getPlayer().hasSpell(spellID) && LanzarHechizo.cooldownGood(this, spellID);
    }

    public void unHide(int spellid) {
       //on retire le buff invi
        if (spellid != -1)// -1 : CAC
        {
            switch (spellid) {
                case 66:
                case 71:
                case 181:
                case 196:
                case 200:
                case 219:
                    return;
            }
        }
        ArrayList<EfectoHechizo> buffs = new ArrayList<>(getFightBuff());
        for (EfectoHechizo SE : buffs) {
            if (SE.getEffectID() == 150)
                getFightBuff().remove(SE);
        }
        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, 150, getId()
                + "", getId() + ",0");
        //On actualise la position
        GestorSalida.GAME_SEND_GIC_PACKET_TO_FIGHT(this.fight, 7, this);
    }

    public boolean isHide() {
        return hasBuff(150);
    }

    public int getPdvMaxOutFight() {
        if (this.perso != null)
            return this.perso.getMaxPdv();
        if (this.mob != null)
            return this.mob.getPdvMax();
        return 0;
    }

    public Map<Integer, Integer> getChatiValue() {
        return this.chatiValue;
    }

    public int getDefaultGfx() {
        if (this.perso != null)
            return this.perso.getGfxId();
        if (this.mob != null)
            return this.mob.getTemplate().getGfxId();
        return 0;
    }

    public int getLvl() {
        if (this.type == 1)
            return this.perso.getLevel();
        if (this.type == 2)
            return this.mob.getLevel();
        if (this.type == 5)
            return Mundo.mundo.getGuild(getCollector().getGuildId()).getLvl();
        if (this.type == 7)
            return getPrism().getLevel();
        if (this.type == 10)
            return getDouble().getLevel();
        return 0;
    }

    public String xpString(String str) {
        if (this.perso != null) {
            int max = this.perso.getLevel() + 1;
            if (max > Mundo.mundo.getExpLevelSize())
                max = Mundo.mundo.getExpLevelSize();
            return Mundo.mundo.getExpLevel(this.perso.getLevel()).perso + str
                    + this.perso.getExp() + str + Mundo.mundo.getExpLevel(max).perso;
        }
        return "0" + str + "0" + str + "0";
    }

    public String getPacketsName() {
        if (this.type == 1)
            return this.perso.getName();
        if (this.type == 2)
            return this.mob.getTemplate().getId() + "";
        if (this.type == 5)
            return this.getCollector().getFullName();
        if (this.type == 7)
            return (getPrism().getAlignement() == 1 ? 1111 : 1112) + "";
        if (this.type == 10)
            return getDouble().getName();

        return "";
    }

    public String getGmPacket(char c, boolean withGm) {
        StringBuilder str = new StringBuilder();
        str.append(withGm ? "GM|" : "").append(c);
        str.append(getCell().getId()).append(";");
        str.append("1;0;");//1; = Orientation
        str.append(getId()).append(";");
        str.append(getPacketsName()).append(";");

        switch (this.type) {
            //Personaje
            case 1 -> {
                str.append(this.perso.getClasse()).append(";");
                str.append(this.perso.getGfxId()).append("^").append(this.perso.get_size()).append(";");
                str.append(this.perso.getSexe()).append(";");
                str.append(this.perso.getLevel()).append(";");
                str.append(this.perso.get_align()).append(",");
                str.append("0").append(",");
                str.append((this.perso.is_showWings() ? this.perso.getGrade() : "0")).append(",");
                str.append(this.perso.getLevel() + this.perso.getId());
                if (this.perso.is_showWings() && this.perso.getDeshonor() > 0) {
                    str.append(",");
                    str.append(this.perso.getDeshonor() > 0 ? 1 : 0).append(';');
                } else {
                    str.append(";");
                }
                int color1 = this.perso.getColor1(),
                        color2 = this.perso.getColor2(),
                        color3 = this.perso.getColor3();
                if (this.perso.getObjetByPos(Constantes.ITEM_POS_MALEDICTION) != null)
                    if (this.perso.getObjetByPos(Constantes.ITEM_POS_MALEDICTION).getModelo().getId() == 10838) {
                        color1 = 16342021;
                        color2 = 16342021;
                        color3 = 16342021;
                    }
                str.append((color1 == -1 ? "-1" : Integer.toHexString(color1))).append(";");
                str.append((color2 == -1 ? "-1" : Integer.toHexString(color2))).append(";");
                str.append((color3 == -1 ? "-1" : Integer.toHexString(color3))).append(";");
                str.append(this.perso.getGMStuffString()).append(";");
                str.append(getPdv()).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_PA)).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_PM)).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_NEU)).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_TER)).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_FEU)).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_EAU)).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_AIR)).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_AFLEE)).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_MFLEE)).append(";");
                str.append(this.team).append(";");
                if (this.perso.isOnMount() && this.perso.getMount() != null)
                    str.append(this.perso.getMount().getStringColor(this.perso.parsecolortomount()));
                str.append(";");
            }
            //Monster
            case 2 -> {
                str.append("-2;");
                str.append(this.mob.getTemplate().getGfxId()).append("^").append(this.mob.getSize()).append(";");
                str.append(this.mob.getGrade()).append(";");
                str.append(this.mob.getTemplate().getColors().replace(",", ";")).append(";");
                str.append("0,0,0,0;");
                str.append(this.getPdvMax()).append(";");
                str.append(this.mob.getPa()).append(";");
                str.append(this.mob.getPm()).append(";");
                str.append(this.team);
            }
            //Recaudador
            case 5 -> {
                str.append("-6;");//Perco
                str.append("6000^100;");//GFXID^Size
                Gremio G = Mundo.mundo.getGuild(this.collector.getGuildId());
                str.append(G.getLvl()).append(";");
                str.append("1;");
                str.append("2;4;");
                str.append((int) Math.floor(G.getLvl() / 2)).append(";").append((int) Math.floor(G.getLvl() / 2)).append(";").append((int) Math.floor(G.getLvl() / 2)).append(";").append((int) Math.floor(G.getLvl() / 2)).append(";").append((int) Math.floor(G.getLvl() / 2)).append(";").append((int) Math.floor(G.getLvl() / 2)).append(";").append((int) Math.floor(G.getLvl() / 2)).append(";");//R�sistances
                str.append(this.team);
            }
            //Prisma
            case 7 -> {
                str.append("-2;");
                str.append(getPrism().getAlignement() == 1 ? 8101 : 8100).append("^100;");
                str.append(getPrism().getLevel()).append(";");
                str.append("-1;-1;-1;");
                str.append("0,0,0,0;");
                str.append(this.getPdvMax()).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_PA)).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_PM)).append(";");
                str.append(getTotalStats().getEffect(214)).append(";");
                str.append(getTotalStats().getEffect(210)).append(";");
                str.append(getTotalStats().getEffect(213)).append(";");
                str.append(getTotalStats().getEffect(211)).append(";");
                str.append(getTotalStats().getEffect(212)).append(";");
                str.append(getTotalStats().getEffect(160)).append(";");
                str.append(getTotalStats().getEffect(161)).append(";");
                str.append(this.team);
            }
            //Doble
            case 10 -> {
                str.append(getDouble().getClasse()).append(";");
                str.append(getDouble().getGfxId()).append("^").append(getDouble().get_size()).append(";");
                str.append(getDouble().getSexe()).append(";");
                str.append(getDouble().getLevel()).append(";");
                str.append(getDouble().get_align()).append(",");
                str.append("1,");//TODO
                str.append((getDouble().is_showWings() ? getDouble().getALvl() : "0")).append(",");
                str.append(getDouble().getId()).append(";");
                str.append((getDouble().getColor1() == -1 ? "-1" : Integer.toHexString(getDouble().getColor1()))).append(";");
                str.append((getDouble().getColor2() == -1 ? "-1" : Integer.toHexString(getDouble().getColor2()))).append(";");
                str.append((getDouble().getColor3() == -1 ? "-1" : Integer.toHexString(getDouble().getColor3()))).append(";");
                str.append(getDouble().getGMStuffString()).append(";");
                str.append(getPdv()).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_PA)).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_PM)).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_NEU)).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_TER)).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_FEU)).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_EAU)).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_AIR)).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_AFLEE)).append(";");
                str.append(getTotalStats().getEffect(Constantes.STATS_ADD_MFLEE)).append(";");
                str.append(this.team).append(";");
                if (getDouble().isOnMount() && getDouble().getMount() != null)
                    str.append(getDouble().getMount().getStringColor(getDouble().parsecolortomount()));
                str.append(";");
            }
        }

        return str.toString();
    }

    public boolean getHadSober()
    {
        return hadSober;
    }

    public void setHadSober(boolean hadSober)
    {
        this.hadSober=hadSober;
    }

    public boolean getJustTrapped()
    {
        return justTrapped;
    }

    public void setJustTrapped(boolean justTrapped)
    {
        this.justTrapped=justTrapped;
    }

    @Override
    public int compareTo(Peleador t) {
        return ((this.getPros() > t.getPros() && !this.isInvocation()) ? 1 : 0);
    }
}