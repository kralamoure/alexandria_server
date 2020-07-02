package org.alexandria.estaticos.pelea;

import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.Camino;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.estaticos.juego.JuegoCliente;
import org.alexandria.estaticos.pelea.hechizo.EfectoHechizo;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Retos {

    private final int Type;
    private final int xpWin;
    private final int dropWin;
    private int Arg = 0;
    private boolean challengeAlive = false, challengeWin = false;
    private String looseBy = "";
    private StringBuilder Args = new StringBuilder();
    private StringBuilder lastActions = new StringBuilder();
    private final Pelea fight;
    private Peleador target;
    private final List<Peleador> _ordreJeu = new ArrayList<>();

    public Retos(Pelea fight, int Type, int xp, int drop) {
        this.challengeAlive = true;
        this.fight = fight;
        this.Type = Type;
        this.xpWin = xp;
        this.dropWin = drop;
        this._ordreJeu.clear();
        this._ordreJeu.addAll(fight.getOrderPlaying());
    }

    public int getType() {
        return this.Type;
    }

    public boolean getAlive() {
        return challengeAlive;
    }

    public int getXp() {
        return xpWin;
    }

    public int getDrop() {
        return dropWin;
    }

    public boolean getWin() {
        return challengeWin;
    }

    public boolean loose() {
        return !looseBy.isEmpty();
    }

    public String getPacketEndFight() {
        return (this.challengeWin ? "OK" + Type : "KO" + Type);
    }

    private void challengeWin() {
        challengeWin = true;
        challengeAlive = false;
        GestorSalida.GAME_SEND_CHALLENGE_FIGHT(fight, 1, "OK" + Type);
    }

    public void challengeLoose(Peleador fighter) {
        String name = "";
        if (fighter != null && fighter.getPlayer() != null)
            name = fighter.getPlayer().getName();
        looseBy = name;
        challengeWin = false;
        challengeAlive = false;
        GestorSalida.GAME_SEND_CHALLENGE_FIGHT(fight, 7, "KO" + Type);
        GestorSalida.GAME_SEND_Im_PACKET_TO_CHALLENGE(fight, 1, "0188;" + name);
    }

    public void challengeSpecLoose(Jugador player) {
        GestorSalida.GAME_SEND_CHALLENGE_PERSO(player, "KO" + Type);
        GestorSalida.GAME_SEND_Im_PACKET_TO_CHALLENGE_PERSO(player, "0188;" + looseBy);
    }

    public String parseToPacket() {
        StringBuilder packet = new StringBuilder();
        packet.append(Type).append(";").append(target != null ? "1" : "0").append(";").append(target != null ? (Integer.valueOf(target.getId())) : "").append(";").append(xpWin).append(";0;").append(dropWin).append(";0;");
        if (!challengeAlive) {
            if (challengeWin)
                packet.append("").append(Type);
            else
                packet.append("").append(Type);
        }
        return packet.toString();
    }

    public void showCibleToPerso(Jugador p) {
        if (!challengeAlive || target == null || target.getCell() == null || p == null)
            return;
        ArrayList<JuegoCliente> Pws = new ArrayList<>();
        Pws.add(p.getGameClient());
        GestorSalida.GAME_SEND_FIGHT_SHOW_CASE(Pws, target.getId(), target.getCell().getId());
    }

    public void showCibleToFight() {
        if (!challengeAlive || target == null || target.getCell() == null)
            return;
        ArrayList<JuegoCliente> Pws = new ArrayList<>();
        for (Peleador fighter : fight.getFighters(1)) {
            if (fighter.hasLeft())
                continue;
            if (fighter.getPlayer() == null || !fighter.getPlayer().isOnline())
                continue;
            Pws.add(fighter.getPlayer().getGameClient());
        }
        GestorSalida.GAME_SEND_FIGHT_SHOW_CASE(Pws, target.getId(), target.getCell().getId());
    }

    public void fightStart() {//D�finit les cibles au d�but du combat
        if (!challengeAlive)
            return;
        switch (Type) {
            case 3, 4, 32, 35 -> {
                if (target == null && _ordreJeu.size() > 0)//Si aucun cible n'est choise on en choisie une
                {
                    List<Peleador> Choix = new ArrayList<>(_ordreJeu);
                    Collections.shuffle(Choix);//M�lange l'ArrayList
                    for (Peleador f : Choix) {
                        if (f.getPlayer() != null)
                            continue;
                        if (f.getMob() != null && f.getTeam2() == 2 && !f.isDead() && !f.isInvocation())
                            target = f;
                    }
                }
                showCibleToFight();//On le montre a tous les joueurs
            }
            case 10 -> {
                int levelMin = 2000;
                for (Peleador fighter : fight.getFighters(2))//La cible sera le niveau le plus faible
                {
                    if (fighter.isInvocation())
                        continue;
                    if (fighter.getPlayer() == null && fighter.getMob() != null && fighter.getLvl() < levelMin && fighter.getInvocator() == null) {
                        levelMin = fighter.getLvl();
                        target = fighter;
                    }
                }
                if (target != null)
                    showCibleToFight();
            }//Ordonn�
            case 25 -> {
                int levelMax = 0;
                for (Peleador fighter : fight.getFighters(2))//la cible sera le niveau le plus �lev�
                {
                    if (fighter.isInvocation() || fighter.isDouble())
                        continue;
                    if (fighter.getPlayer() == null && fighter.getMob() != null && fighter.getInvocator() == null && fighter.getLvl() > levelMax) {
                        levelMax = fighter.getLvl();
                        this.target = fighter;
                    }
                }
                if (target != null)
                    showCibleToFight();
            }
        }
    }

    public void fightEnd() {//V�rifie la validit� des challenges en fin de combat (si n�cessaire)
        if (!challengeAlive)
            return;
        switch (Type) {
            case 44://Partage
            case 46://Chacun son monstre
                for (Peleador fighter : fight.getFighters(1)) {
                    if (!Args.toString().contains(String.valueOf(fighter.getId()))) {
                        challengeLoose(fighter);
                        return;
                    }
                }
                break;
        }
        challengeWin();
    }

    public void onFighterDie(Peleador fighter) {
        if (!challengeAlive)
            return;
        switch (Type) {
            case 33: // survivant
            case 49: // Prot�gez vos mules
                if (fighter.getPlayer() != null)
                    challengeLoose(fight.getFighterByOrdreJeu());
                break;
            case 44://Partage
                if (fighter.getPlayer() != null)
                    if (!Args.toString().contains(String.valueOf(fighter.getId())))
                        challengeLoose(fighter);
                break;
        }
    }

    public void onFighterAttacked(Peleador caster, Peleador target) {
        if (!challengeAlive)
            return;
        switch (Type) {
            case 17:// Intouchable
                if (target.getTeam() == 0 && !target.isInvocation()) {
                    if (target.getBuff(9) == null) // Si d�robade
                        challengeLoose(target);
                }
                break;
            case 31: // Focus
                if (caster.getTeam() == 0 && target.getTeam() == 1 && !caster.isInvocation()) {
                    if (Args.toString().isEmpty())
                        Args.append(target.getId());
                    else if (!Args.toString().contains("" + target.getId()))
                        challengeLoose(caster);
                }
                break;
            case 47: // Contamination
                if (target.getTeam() == 0 && !target.isInvocation() && !Args.toString().contains(";" + target.getId() + ","))
                    Args.append(";").append(target.getId()).append(",3;");
                break;
        }
    }

    public void onFightersAttacked(ArrayList<Peleador> targets, Peleador caster, EfectoHechizo SE, int spell, boolean isTrap) {
        int effectID = SE.getEffectID();
        if (!challengeAlive)
            return;
        String DamagingEffects = "|82|85|86|87|88|89|91|92|93|94|95|96|97|98|99|100|141|671|672|1014|1015|";
        String HealingEffects = "|108|";
        String MPEffects = "|77|127|169|";
        String APEffects = "|84|101|";
        String OPEffects = "|116|320|";
        switch (Type) {
            case 18: // Incurable
                if (caster.getTeam() == 0 && !caster.isInvocation() && HealingEffects.contains("|" + effectID + "|"))
                    targets.stream().filter(fighter -> fighter.getTeam() == 0).forEach(fighter -> challengeLoose(caster));
                break;
            case 19: // Mains propres
                if (caster.getTeam() != 0)
                    return;
                if (caster.isInvocation())
                    return;
                if (SE.getTurn() > 0)
                    return;
                if (isTrap)
                    return;

                for (Peleador target : targets) {
                    if (target.getTeam() == 1 && !target.isInvocation()) {
                        if (DamagingEffects.contains("|" + effectID + "|"))
                            challengeLoose(caster);
                        break;
                    }
                }

                break;
            case 20: // El�mentaire
                if (caster.getTeam() == 0 && !caster.isInvocation() && DamagingEffects.contains("|" + effectID + "|") && effectID != 141) {
                    switch (spell) {
                        case 126://Mot stimulant
                        case 149://Mutilation
                        case 106://Roue de la fortune
                        case 111://Contrecoup
                        case 108://Esprit f�lin
                        case 435://Transfert de vie
                        case 135://Mot de sacrifice
                        case 123://Mot drainant
                            return;
                    }
                    if (Arg == 0) {
                        Arg = effectID;
                        break;
                    }
                    if (Arg != effectID) {
                        String eau = "85 91 96 1014", terre = "86 92 97 1015", air = "87 93 98", feu = "88 94 99", neutre = "89 95 100";
                        if (eau.contains(String.valueOf(Arg)) && eau.contains(String.valueOf(effectID))) {
                            break;
                        } else if (terre.contains(String.valueOf(Arg)) && terre.contains(String.valueOf(effectID))) {
                            break;
                        } else if (air.contains(String.valueOf(Arg)) && air.contains(String.valueOf(effectID))) {
                            break;
                        } else if (feu.contains(String.valueOf(Arg)) && feu.contains(String.valueOf(effectID))) {
                            break;
                        } else if (neutre.contains(String.valueOf(Arg)) && neutre.contains(String.valueOf(effectID))) {
                            break;
                        }
                        challengeLoose(caster);
                        break;
                    }
                }
                break;
            case 21: // Circulez !
                if (caster.getTeam() == 0 && !caster.isInvocation() && MPEffects.contains("|" + effectID + "|")) {
                    for (Peleador target : targets) {
                        if (target.getTeam() == 1) {
                            challengeLoose(caster);
                            break;
                        }
                    }
                }
                break;
            case 22: // Le temps qui court !
                if (caster.getTeam() == 0 && !caster.isInvocation() && APEffects.contains("|" + effectID + "|")) {
                    for (Peleador target : targets) {
                        if (target.getTeam() == 1) {
                            challengeLoose(caster);
                            break;
                        }
                    }
                }
                break;
            case 23: // Perdu de vue !
                if (caster.getTeam() == 0 && !caster.isInvocation() && OPEffects.contains("|" + effectID + "|")) {
                    for (Peleador target : targets) {
                        if (target.getTeam() == 1) {
                            challengeLoose(caster);
                            break;
                        }
                    }
                }
                break;
            case 32: // Elitiste
            case 34: // Impr鶩sible
                if (caster.getTeam() == 0 && DamagingEffects.contains("|" + effectID + "|")) {
                    for (Peleador target : targets) {
                        if (this.target != null) {
                            if (target.getTeam() == 1) {
                                if (this.target.getId() != target.getId() && !target.isInvocation())
                                    challengeLoose(caster);
                            }
                        }
                    }
                }
                break;
            case 38: // Blitzkrieg
                if (caster.getTeam() == 0 && !caster.isInvocation() && DamagingEffects.contains("|" + effectID + "|")) {
                    for (Peleador target : targets) {
                        if (target.getTeam() == 1 && !target.isInvocation()) {
                            StringBuilder id = new StringBuilder();
                            id.append(";").append(target.getId()).append(",");
                            if (!this.Args.toString().contains(id.toString())) {
                                id.append(caster.getId());
                                this.Args.append(id.toString());
                            }
                        }
                    }
                }
                break;
            case 43: // Abn�gation
                if (caster.getTeam() == 0 && !caster.isInvocation() && HealingEffects.contains("|" + effectID + "|"))
                    for (Peleador target : targets)
                        if (target.getId() == caster.getId())
                            challengeLoose(caster);
                break;
            case 45: // Duel
            case 46: // Chacun son monstre
                if (caster.getTeam() == 0 && !caster.isInvocation() && DamagingEffects.contains("|" + effectID + "|")) {
                    for (Peleador target : targets) {
                        if (target.getTeam() == 1 && !caster.isInvocation()) {
                            if (!Args.toString().contains(";" + target.getId() + ","))
                                Args.append(";").append(target.getId()).append(",").append(caster.getId()).append(";");
                            else if (Args.toString().contains(";" + target.getId() + ",") && !Args.toString().contains(";" + target.getId() + "," + caster.getId() + ";"))
                                challengeLoose(target);
                        }
                    }
                }
                break;
        }
    }

    //v2.8 - Random Contract Killer target
    public void onMobDie(Peleador mob, Peleador killer) {
        if (mob.getMob() == null || mob.getPlayer() != null || mob.getTeam() != 1)
            return;
        if (mob.isInvocation() && mob.getInvocator().getPlayer() != null)
            return;
        boolean isKiller = (killer.getId() != mob.getId());

        if (!challengeAlive)
            return;

        switch (Type) {
            case 3: // D�sign� Volontaire
                if (target == null)
                    return;
                if (mob.isInvocation())
                    return;

                if (mob.getInvocator() != null)
                    if (mob.getInvocator().getId() == target.getId())
                        return;

                if (target.getId() != mob.getId()) {
                    challengeLoose(fight.getFighterByOrdreJeu());
                } else {
                    challengeWin();
                }
                target = null;
                break;

            case 4: // Sursis
                if (target == null)
                    return;

                if (target.getId() == mob.getId() && !fight.verifIfTeamIsDead()) {
                    challengeLoose(fight.getFighterByOrdreJeu());
                }
                break;

            case 28: // Ni Pioutes ni Soumises
                if (isKiller && killer.getPlayer() != null)
                    if (killer.getPlayer().getSexe() == 0) {
                        challengeLoose(fight.getFighterByOrdreJeu());
                    }
                break;

            case 29: // Ni Pious ni Soumis
                if (isKiller && killer.getPlayer() != null) {
                    if (killer.getPlayer().getSexe() == 1) {
                        challengeLoose(fight.getFighterByOrdreJeu());
                    }
                }
                break;

            case 31: // Focus
                if (mob.getLevelUp())
                    break;
                if (Args.toString().contains("" + mob.getId()))
                    Args = new StringBuilder();
                else if (!mob.isInvocation())
                    challengeLoose(killer);
                break;

            case 32: // Elitiste
                if (target.getId() == mob.getId())
                    challengeWin();
                break;

            case 34: // Impr�visible
                target = null;
                break;
            case 42: // Deux pour le prix d'un
                if (mob.isInvocation() || killer.isInvocation())
                    return;
                if (Args.length() == 0) {
                    Args.append(killer.getId());
                } else {
                    Args.append(";").append(killer.getId());
                }
                break;
            case 44: // Partage
            case 46: // Chacun son monstre
                if (isKiller && !mob.isInvocation())
                    if (Args.length() == 0) {
                        Args.append(killer.getId());
                    } else {
                        Args.append(";").append(killer.getId());
                    }
                break;
            case 30: // Les petits d'abord
            case 48: // Les mules d'abord
                if (mob.isInvocation() || mob.isDouble())
                    return;
                if (mob.getId() != killer.getId()) {
                    int lvlMin = 5000;
                    for (Peleador f : fight.getFighters2(1)) {
                        if (f.isInvocation())
                            continue;
                        if (f.getLvl() < lvlMin)
                            lvlMin = f.getLvl();
                    }
                    if (killer.getLvl() > lvlMin)
                        challengeLoose(fight.getFighterByOrdreJeu());
                }
                break;

            case 35: //Contract killer
                if (target == null)
                    return;
                if (target.getId() != mob.getId() && killer.getPlayer() != null) //wrong target killed
                {
                    if (!mob.isInvocation())
                        challengeLoose(fight.getFighterByOrdreJeu());
                } else {
                    try {
                        target = null;
                        ArrayList<Peleador> fighters = new ArrayList<>(fight.getFighters(2));
                        //remove unavailable targets from new target selector
                        fighters.removeIf(f -> f.isInvocation() || f.isDead() || f.getPlayer() != null);
                        Collections.shuffle(fighters); //randomly shuffle
                        for (Peleador f : fighters) {
                            if (!f.isInvocation() && !f.isDead() && f.getPlayer() == null) {
                                target = f;
                                break;
                            }
                        }
                        showCibleToFight();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case 10: // Cruel
                if (target == null)
                    return;
                if (target.isInvocation() || target.isDouble() || mob.getPlayer() != null)
                    return;
                if (target.getId() != mob.getId() && target.getLvl() != mob.getLvl() && killer.getPlayer() != null) {
                    if (mob.getLvl() > target.getLvl() && !mob.isInvocation())
                        challengeLoose(fight.getFighterByOrdreJeu());
                } else {
                    try {
                        int levelMin = 2000;
                        for (Peleador fighter : fight.getFighters(2)) {
                            if (fighter.isInvocation() || fighter.isDouble() || fighter.getPlayer() != null || fighter.isDead())
                                continue;
                            if (fighter.getPlayer() == null && fighter.getLvl() < levelMin) {
                                levelMin = fighter.getLvl();
                                target = fighter;
                            }
                        }
                        if (target != null)
                            showCibleToFight();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case 25: // Ordonn�
                if (target == null)
                    return;
                if (mob.isInvocation() || mob.isDouble() || mob.getPlayer() != null)
                    return;

                if (target.getId() != mob.getId() && killer.getPlayer() != null) {
                    if (mob.getLvl() < target.getLvl())
                        challengeLoose(fight.getFighterByOrdreJeu());
                } else {
                    try {
                        int levelMax = 0;
                        for (Peleador fighter : fight.getFighters(2)) {
                            if (fighter.isInvocation() || fighter.isDouble() || fighter.getPlayer() != null || fighter.isDead())
                                continue;
                            if (fighter.getLvl() > levelMax) {
                                levelMax = fighter.getLvl();
                                target = fighter;
                            }
                        }
                        if (target != null)
                            showCibleToFight();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public void onPlayerMove(Peleador fighter) {
        if (!challengeAlive)
            return;
        if (Type == 1) { // Zombie
            if (this.fight.getCurFighterUsedPm() > 1) // Si l'on a utilis� plus d'un PM
                challengeLoose(fight.getFighterByOrdreJeu());
        }
    }

    public void onPlayerAction(Peleador fighter, int actionID) {
        if (!challengeAlive || fighter.getTeam() == 1)
            return;
        StringBuilder action = new StringBuilder();
        action.append(";").append(fighter.getId());
        action.append(",").append(actionID).append(";");
        switch (Type) {
            // Econome
            case 6, 5 -> {
                if (lastActions.toString().contains(action.toString()))
                    challengeLoose(fight.getFighterByOrdreJeu());
                lastActions.append(action.toString());
            }
            // Born�
            case 24 -> {
                if (!lastActions.toString().contains(action.toString()) && lastActions.toString().contains(";" + fighter.getId() + ","))
                    challengeLoose(fight.getFighterByOrdreJeu());
                lastActions.append(action.toString());
            }
        }
    }

    public void onPlayerCac(Peleador fighter)
    {

        if (!challengeAlive)
            return;

        switch (Type) {
            // Mystique
            case 11 -> challengeLoose(fight.getFighterByOrdreJeu());
            // Econome
            case 6, 5 -> {
                StringBuilder action = new StringBuilder();
                action.append(";").append(fighter.getId());
                action.append(",").append("cac").append(";");
                if (lastActions.toString().contains(action.toString()))
                    challengeLoose(fight.getFighterByOrdreJeu());
                lastActions.append(action.toString());
            }
        }
    }

    public void onPlayerSpell (Peleador fighter, Hechizo.SortStats spellStats)
    {
        if (!challengeAlive)
            return;
        if (fighter.getPlayer() == null)
            return;
        switch (Type) {
            case 9: // Barbare
                challengeLoose(fight.getFighterByOrdreJeu());
                break;
            case 14: // Casino Royal (sort #101)
                if (fighter.getPlayer() != null)
                    if (spellStats.getSpellID() == 101)
                        Args = new StringBuilder("cast");
                break;
        }
    }

    public void onPlayerStartTurn (Peleador fighter)
    {
        if (!challengeAlive)
            return;
        switch (Type) {
            case 2: // Statue
                if (fighter.getPlayer() == null)
                    return;
                Arg = fighter.getCell().getId();
                break;
            case 6: // Versatile
                lastActions = new StringBuilder();
                break;
            case 14: // Casino Royal (sort #101)
                if (fighter.getPlayer() != null)
                    if (fighter.canLaunchSpell(101))
                        Args = new StringBuilder("ok");
                    else
                        Args = new StringBuilder("cant");
                break;
            case 34: // Impr�visible
                if (fighter.getTeam() == 1)
                    return;
                try {
                    int noBoucle = 0, GUID = 0;
                    target = null;
                    while (target == null) {
                        if (_ordreJeu.size() > 0) {
                            GUID = Formulas.getRandomValue(0, _ordreJeu.size() - 1);
                            Peleador f = _ordreJeu.get(GUID);
                            if (f.getPlayer() == null && !f.isDead())
                                target = f;
                            noBoucle++;
                            if (noBoucle > 150)
                                return;
                        }
                    }
                    showCibleToFight();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 38: // Blitzkrieg
                if (fighter.getTeam() == 1 && Args.toString().contains(";" + fighter.getId() + ",")) {
                    if (fighter.isDead())
                        return;

                    int id = 0;

                    for (String string : this.Args.toString().split(";")) {
                        if (string.contains("" + fighter.getId())) {
                            for (String test : string.split(","))
                                id = Integer.parseInt(test);
                            break;
                        }
                    }

                    for (Peleador target : this.fight.getFighters(1))
                        if (target.getId() == id)
                            if (fighter.getPdv() != fighter.getPdvMax())
                                challengeLoose(target);
                }
                break;
            case 47: // Contamination
                if (fighter.getTeam() == 0) {
                    String str = ";" + fighter.getId() + ",";
                    if (Args.toString().contains(str + "1;"))
                        challengeLoose(fighter);
                    else if (Args.toString().contains(str + "2;"))
                        Args.append(str).append("1;");
                    else if (Args.toString().contains(str + "3;"))
                        Args.append(str).append("2;");
                }
                break;
        }
    }

    public void onPlayerEndTurn (Peleador fighter) {
        if (!challengeAlive)
            return;

        boolean hasFailed = false;
        ArrayList<Peleador> fighters = Camino.getFightersAround(fighter.getCell().getId(), fight.getMap());

        switch (Type) {
            case 1: // Zombie
                if (this.fight.getCurFighterUsedPm() <= 0) // Si l'on a pas boug�
                    challengeLoose(fighter);
                break;

            case 2: // Statue
                if (fighter.getPlayer() != null)
                    if (fighter.getCell().getId() != Arg)
                        challengeLoose(fighter);
                break;

            case 7: // Jardinier (sort #367)
                if (fighter.getPlayer() != null)
                    if (fighter.canLaunchSpell(367))
                        challengeLoose(fighter);
                break;

            case 8: // Nomade
                if (this.fight.getCurFighterPm() != 0)
                    challengeLoose(fighter);
                break;

            case 12: // Fossoyeur (sort #373)
                if (fighter.getPlayer() != null)
                    if (fighter.canLaunchSpell(373))
                        challengeLoose(fighter);
                break;

            case 14: // Casino Royal (sort #101)
                if (fighter.getPlayer() != null)
                    if (Args.toString().equals("ok"))
                        challengeLoose(fighter);
                break;

            case 15: // Araknophile (sort #370)
                if (fighter.getPlayer() != null)
                    if (fighter.canLaunchSpell(370))
                        challengeLoose(fighter);
                break;

            case 36: // Hardi
                hasFailed = true;
                if (!fighters.isEmpty())
                    for (Peleador f : fighters)
                        if (f.getTeam() != fighter.getTeam()) {
                            hasFailed = false;
                            break;
                        }
                break;

            case 37: // Collant
                hasFailed = true;
                if (!fighters.isEmpty())
                    for (Peleador f : fighters)
                        if (f.getTeam() == fighter.getTeam()) {
                            hasFailed = false;
                            break;
                        }
                break;

            case 39: // Anachor�te
                if (!fighters.isEmpty())
                    fighters.stream().filter(f -> f.getTeam() == fighter.getTeam()).forEach(f -> challengeLoose(fighter));
                break;

            case 40: // Pusillanime
                if (!fighters.isEmpty())
                    fighters.stream().filter(f -> f.getTeam() != fighter.getTeam()).forEach(f -> challengeLoose(fighter));
                break;

            case 41: // P�tulant
                if (this.fight.getCurFighterPa() != 0)
                    challengeLoose(fighter);
                break;

            case 42: // Deux pour le prix d'un
                if (Args.length() > 0)
                    if (!(Args.toString().split(";").length % 2 == 0))
                        hasFailed = true;
                Args = new StringBuilder();
                break;
        }
        if (hasFailed)
            challengeLoose(fighter);
    }
}
