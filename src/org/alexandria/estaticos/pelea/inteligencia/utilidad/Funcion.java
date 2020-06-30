package org.alexandria.estaticos.pelea.inteligencia.utilidad;

import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;
import org.alexandria.comunes.Camino;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Constantes;
import org.alexandria.estaticos.juego.accion.AccionJuego;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.EfectoHechizo;
import org.alexandria.estaticos.pelea.hechizo.Hechizo.SortStats;
import org.alexandria.estaticos.pelea.hechizo.LanzarHechizo;
import org.alexandria.estaticos.pelea.trampas.Grifos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Funcion {

    private final static Funcion instance = new Funcion();

    public static Funcion getInstance() {
        return instance;
    }
    
    public int attackIfPossiblerat(Pelea fight, Peleador fighter, Peleador target, boolean loin)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SortStats SS = null;
        for(Map.Entry<Integer, SortStats> entry : fighter.getMob().getSpells().entrySet())
        {
            SortStats a = entry.getValue();
            if(a.getSpellID() == 489 && loin)
                SS = a;
            if(a.getSpellID() == 646 && !loin)
                SS = a;
        }
        if (target == null)
            return 666;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public boolean TPIfPossiblesphinctercell(Pelea fight, Peleador fighter, Peleador target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null || target == null)
            return false;

        SortStats spell = null;
        for(SortStats s : fighter.getMob().getSpells().values()) {
            if(s.getSpellID() == 1016)
                spell = s;
        }

        if(spell != null) {
            int cell = getMaxCellForTP(fight, fighter, target, spell.getMaxPO());
            if(fight.canCastSpell1(fighter, spell, fight.getMap().getCase(cell), -1)) {
                fight.tryCastSpell(fighter, spell, cell);
                return true;
            } else {
                byte count = 0;
                List<Integer> cells = new ArrayList<>();

                while (count != 4) {
                    int nearestCell = Camino.getAvailableCellArround(fight, target.getCell().getId(), cells);

                    if (nearestCell == 0)
                        break;
                    if (fight.canCastSpell1(fighter, spell, fight.getMap().getCase(nearestCell), -1)) {
                        fight.tryCastSpell(fighter, spell, nearestCell);
                        return true;
                    } else {
                        cells.add(nearestCell);
                    }
                    count++;
                }
            }

        }

        return false;
    }

    public int attackIfPossiblesphinctercell(Pelea fight, Peleador fighter, Peleador target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return -1;
        SortStats SS = null;
        for(Map.Entry<Integer, SortStats> entry : fighter.getMob().getSpells().entrySet())
        {
            SortStats a = entry.getValue();
            if(a.getSpellID() == 1017)
                SS = a;
        }
        if (target == null)
            return 666;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public boolean tryTurtleInvocation(Pelea fight, Peleador fighter) {
        if (fight == null || fighter == null)
            return false;

        SortStats spell = null;
        for (SortStats s : fighter.getMob().getSpells().values()) {
            if (s.getSpellID() == 1018) {
                spell = s;
                break;
            }
        }

        if (spell != null) {
            for (Peleador target : fight.getFighters(3)) {
                if (target.getTeam() == fighter.getTeam()) continue;
                List<Integer> cells = new ArrayList<>();
                int nearestCell = Camino.getAvailableCellArround(fight, target.getCell().getId(), cells);

                if (nearestCell == 0)
                    break;
                if (fight.canCastSpell1(fighter, spell, fight.getMap().getCase(nearestCell), -1)) {
                    fight.tryCastSpell(fighter, spell, nearestCell);
                    return true;
                } else {
                    cells.add(nearestCell);
                }
            }
        }

        List<SortStats> spells = new ArrayList<>();
        spells.add(spell);
        return Funcion.instance.invocIfPossible(fight, fighter, spells);
    }

    public Peleador getNearest(Pelea fight, Peleador fighter)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        Peleador curF = null;
        for (Peleador f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            int d = Camino.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
            if (d < dist)
            {
                dist = d;
                curF = f;
            }
        }
        return curF;
    }

    public int attackIfPossibleAll(Pelea fight, Peleador fighter, Peleador target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SortStats SS = getBestSpellForTarget(fight, fighter, target, fighter.getCell().getId());
        if (target == null)
            return 0;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public int moveToAttackIfPossibleAll(Pelea fight, Peleador fighter)
    {
        if (fight == null || fighter == null)
            return -1;
        Peleador target = getNearest(fight, fighter);
        int distMin = Camino.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), target.getCell().getId());
        ArrayList<SortStats> sorts = getLaunchableSort(fighter, fight, distMin);
        if (sorts == null)
            return -1;
        ArrayList<Integer> cells = Camino.getListCaseFromFighter(fight, fighter, fighter.getCell().getId(), sorts);
        if (cells == null)
            return -1;
        int CellDest = 0;
        SortStats bestSS = null;
        int[] bestInvok = {1000, 0, 0, 0, -1};
        int[] bestFighter = {1000, 0, 0, 0, -1};
        int targetCell = -1;
        for (int i : cells)
        {
            for (SortStats S : sorts)
            {
                if (fight.canCastSpell1(fighter, S, target.getCell(), i))
                {
                    int dist = Camino.getDistanceBetween(fight.getMapOld(), fighter.getCell().getId(), target.getCell().getId());
                    if (!Camino.isNextTo(fighter.getFight().getMap(), fighter.getCell().getId(), target.getCell().getId()))
                    {
                        if (target.isInvocation())
                        {
                            if (dist < bestInvok[0])
                            {
                                bestInvok[0] = dist;
                                bestInvok[1] = i;
                                bestInvok[2] = 1;
                                bestInvok[3] = 1;
                                bestInvok[4] = target.getCell().getId();
                                bestSS = S;
                            }

                        }
                        else
                        {
                            if (dist < bestFighter[0])
                            {
                                bestFighter[0] = dist;
                                bestFighter[1] = i;
                                bestFighter[2] = 1;
                                bestFighter[3] = 0;
                                bestFighter[4] = target.getCell().getId();
                                bestSS = S;
                            }

                        }
                    }
                    else
                    {
                        if (dist < bestFighter[0])
                        {
                            bestFighter[0] = dist;
                            bestFighter[1] = i;
                            bestFighter[2] = 1;
                            bestFighter[3] = 0;
                            bestFighter[4] = target.getCell().getId();
                            bestSS = S;
                        }
                    }
                }
            }
        }
        if (bestFighter[1] != 0)
        {
            CellDest = bestFighter[1];
            targetCell = bestFighter[4];
        }
        else if (bestInvok[1] != 0)
        {
            CellDest = bestInvok[1];
            targetCell = bestInvok[4];
        }
        else
            return -1;
        if (CellDest == 0)
            return -1;
        if (CellDest == fighter.getCell().getId())
            return targetCell + bestSS.getSpellID() * 1000;
        ArrayList<GameCase> path = new IACamino(fight.getMapOld(), fight, fighter.getCell().getId(), CellDest).getShortestPath(-1);
        if (path == null)
            return -1;
        StringBuilder pathstr = new StringBuilder();
        try
        {
            int curCaseID = fighter.getCell().getId();
            int curDir = 0;
            path.add(fight.getMapOld().getCase(CellDest));
            for (GameCase c : path)
            {
                if (curCaseID == c.getId())
                    continue; // Emp�che le d == 0
                char d = Camino.getDirBetweenTwoCase(curCaseID, c.getId(), fight.getMap(), true);
                if (d == 0)
                    return -1;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (path.indexOf(c) != 0)
                        pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
                    pathstr.append(d);
                }
                curCaseID = c.getId();
            }
            if (curCaseID != fighter.getCell().getId())
                pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        AccionJuego GA = new AccionJuego(0, 1, "");
        GA.args = pathstr.toString();
        fight.onFighterDeplace(fighter, GA);

        return targetCell + bestSS.getSpellID() * 1000;
    }

    public int attackIfPossiblesacrifier(Pelea fight, Peleador fighter, Peleador target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SortStats SS = null;
        for(Map.Entry<Integer, SortStats> entry : fighter.getMob().getSpells().entrySet())
        {
            SortStats a = entry.getValue();
            if(a.getSpellID() == 233)
                SS = a;
        }
        if (target == null)
            return 666;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public int IfPossibleRasboulvulner(Pelea fight, Peleador fighter, Peleador target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SortStats SS = null;
        for(Map.Entry<Integer, SortStats> entry : fighter.getMob().getSpells().entrySet()) {
            SortStats a = entry.getValue();
            if(a.getSpellID() == 1039)
                SS = a;
        }
        if (target == null)
            return 666;
        if(fighter.getPa() < 14)
            return 0;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());

        if (attack != 0) {
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 1039, target.getId() + "", target.getId() + ",+" + 1);
            return attack;
        }
        return 0;
    }

    public void invoctantaIfPossible(Pelea fight, Peleador fighter)
    {
        if (fight == null || fighter == null)
            return;
        if (fighter.nbInvocation() >= 4)
            return;
        Peleador nearest = getNearestEnnemy(fight, fighter);
        if (nearest == null)
            return;
        int nearestCell = fighter.getCell().getId();
        int limit = 30;
        int _loc0_ = 0;
        SortStats spell = null;
        if (fighter.haveState(36))
        {
            spell = Mundo.mundo.getSort(1110).getStatsByLevel(5);
            fighter.setState(36, 0, fighter.getId());
        }
        if (fighter.haveState(37))
        {
            spell = Mundo.mundo.getSort(1109).getStatsByLevel(5);
            fighter.setState(37, 0, fighter.getId());
        }
        if (fighter.haveState(38))
        {
            spell = Mundo.mundo.getSort(1108).getStatsByLevel(5);
            fighter.setState(38, 0, fighter.getId());
        }
        if (fighter.haveState(35))
        {
            spell = Mundo.mundo.getSort(1107).getStatsByLevel(5);
            fighter.setState(35, 0, fighter.getId());
        }
        while (_loc0_++ < limit)
        {
            nearestCell = Camino.getNearestCellAround(fight.getMap(), nearestCell, nearest.getCell().getId(), null);
        }
        if (nearestCell == -1)
            return;
        if (spell == null)
            return;
        int invoc = fight.tryCastSpell(fighter, spell, nearestCell);
        if (invoc != 0) {
        }
    }

    public void buffIfPossibleKrala(Pelea fight, Peleador fighter, Peleador target)
    {
        if (fight == null || fighter == null)
            return;
        if (target == null)
            return;
        SortStats SS = null;
        if (fighter.haveState(31) && fighter.haveState(32) && fighter.haveState(33) && fighter.haveState(34))
        {
            SS = Mundo.mundo.getSort(1106).getStatsByLevel(5);
        }
        if (SS == null)
            return;
        int buff = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (buff != 0) {
        }
    }

    public boolean buffIfPossibleKitsou(Pelea fight, Peleador fighter, Peleador target)
    {
        if (fight == null || fighter == null)
            return false;
        if (target == null)
            return false;
        SortStats SS = null;
        SS = Mundo.mundo.getSort(521).getStatsByLevel(5);
        if (SS == null)
            return false;
        int buff = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (buff != 0)
            return false;
        return true;
    }
    public boolean buffIfPossibleTortu(Pelea fight, Peleador fighter, Peleador target)
    {
        if (fight == null || fighter == null)
            return false;
        if (target == null)
            return false;
        SortStats SS = null;
        for(Map.Entry<Integer, SortStats> entry : fighter.getMob().getSpells().entrySet())
        {
            SortStats a = entry.getValue();
            if(a.getSpellID() == 1019)
                SS = a;
        }
        if (SS == null)
            return false;
        int buff = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (buff != 0)
            return false;
        return true;
    }

    public int tpIfPossibleTynril(Pelea fight, Peleador fighter, Peleador target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SortStats SS = null;
        for(Map.Entry<Integer, SortStats> entry : fighter.getMob().getSpells().entrySet())
        {
            SortStats a = entry.getValue();
            if(a.getSpellID() == 1060)
                SS = a;
        }
        if (target == null)
            return 666;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public int pmgongon(Pelea fight, Peleador fighter, Peleador target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SortStats SS = null;
        for(Map.Entry<Integer, SortStats> entry : fighter.getMob().getSpells().entrySet())
        {
            SortStats a = entry.getValue();
            if(a.getSpellID() == 284)
                SS = a;
        }
        if (target == null)
            return 0;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public int tpIfPossibleRasboul(Pelea fight, Peleador fighter, Peleador target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SortStats SS = null;
        for(Map.Entry<Integer, SortStats> entry : fighter.getMob().getSpells().entrySet())
        {
            SortStats a = entry.getValue();
            if(a.getSpellID() == 1041)
                SS = a;
        }
        if (target == null)
            return 666;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public boolean HealIfPossiblefriend(Pelea fight, Peleador f, Peleador target)//boolean pour choisir entre auto-soin ou soin alli�
    {
        if (fight == null || f == null|| target == null)
            return false;
        if (f.isDead())
            return false;
        SortStats SS = null;


        Peleador curF = null;
        int PDVPERmin = 100;
        SortStats curSS = null;
        for (Peleador F : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (F == f)
                continue;
            if (F.isDead())
                continue;
            if (F.getTeam() == f.getTeam())
            {
                int PDVPER = (F.getPdv() * 100) / F.getPdvMax();
                if (PDVPER < PDVPERmin && PDVPER < 95)
                {
                    int infl = 0;
                    if (f.isCollector())
                    {
                        for (Map.Entry<Integer, SortStats> ss : Mundo.mundo.getGuild(f.getCollector().getGuildId()).getSpells().entrySet())
                        {
                            if (ss.getValue() == null)
                                continue;
                            if (infl < calculInfluenceHeal(ss.getValue())
                                    && calculInfluenceHeal(ss.getValue()) != 0
                                    && fight.canCastSpell1(f, ss.getValue(), F.getCell(), -1))//Si le sort est plus interessant
                            {
                                infl = calculInfluenceHeal(ss.getValue());
                                curSS = ss.getValue();
                            }
                        }
                    }
                    else
                    {
                        for (Map.Entry<Integer, SortStats> ss : f.getMob().getSpells().entrySet())
                        {
                            if (infl < calculInfluenceHeal(ss.getValue())
                                    && calculInfluenceHeal(ss.getValue()) != 0
                                    && fight.canCastSpell1(f, ss.getValue(), F.getCell(), -1))//Si le sort est plus interessant
                            {
                                infl = calculInfluenceHeal(ss.getValue());
                                curSS = ss.getValue();
                            }
                        }
                    }
                    if (curSS != SS && curSS != null)
                    {
                        curF = F;
                        SS = curSS;
                        PDVPERmin = PDVPER;
                    }
                }
            }
        }
        target = curF;
        if (target == null)
            return false;
        if (target.isFullPdv())
            return false;
        if (SS == null)
            return false;
        int heal = fight.tryCastSpell(f, SS, target.getCell().getId());
        if (heal != 0)
            return false;

        return true;
    }

    public int tpIfPossibleKaskargo(Pelea fight, Peleador fighter, Peleador target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SortStats SS = null;
        for(Map.Entry<Integer, SortStats> entry : fighter.getMob().getSpells().entrySet())
        {
            SortStats a = entry.getValue();
            if(a.getSpellID() == 445)
                SS = a;
        }
        if (target == null)
            return 666;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public int attackIfPossibleKaskargo(Pelea fight, Peleador fighter, Peleador target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null) return 0;

        SortStats spellStat = null;
        int cell = Camino.getCaseBetweenEnemy(fighter.getCell().getId(), fight.getMap(), fight);

        for (SortStats spellStats : fighter.getMob().getSpells().values())
            if(spellStats.getSpellID() == 949) {
                spellStat = spellStats; break; }

        int i = 10;
        while(i > 0) {
            for(Grifos glyph : fight.getAllGlyphs())
                if(glyph != null && glyph.getCelda().getId() == cell)
                    cell = Camino.getCaseBetweenEnemy(fighter.getCell().getId(), fight.getMap(), fight);
            i--;
        }

        if (target == null) return 666;
        int attack = fight.tryCastSpell(fighter, spellStat, cell);
        if (attack != 0) return attack;
        return 0;
    }

    public int attackIfPossiblePeki(Pelea fight, Peleador fighter, Peleador target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null || target == null)
            return 0;
        SortStats SS = null;
        int cell = 0;
        for (Map.Entry<Integer, SortStats> S : fighter.getMob().getSpells().entrySet())
        {
            int cellID = target.getCell().getId();
            if(S.getValue().getSpellID() == 1280)
            {
                cell = cellID;
                SS = S.getValue();
            }
        }
        int attack = fight.tryCastSpell(fighter, SS, cell);

        if (attack != 0)
            return 2000;
        return 0;
    }

    public int attackIfPossibleRN(Pelea fight, Peleador fighter, Peleador target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null || target == null)
            return 0;
        SortStats SS = null;
        int cell = 0;
        for (Map.Entry<Integer, SortStats> S : fighter.getMob().getSpells().entrySet())
        {
            int cellID = target.getCell().getId();
            if(S.getValue().getSpellID() == 1006)
            {
                cell = cellID;
                SS = S.getValue();
            }
        }
        if(!fight.canCastSpell1(fighter, SS, fight.getMap().getCase(cell), -1))
            return 0;
        int attack = fight.tryCastSpell(fighter, SS, cell);

        if (attack != 0)
            return 2000;
        return 0;
    }

    public int attackIfPossibleBuveur(Pelea fight, Peleador fighter, Peleador target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SortStats SS = null;
        int cell = 0;
        for (Map.Entry<Integer, SortStats> S : fighter.getMob().getSpells().entrySet())
        {
            int cellID = fighter.getCell().getId();
            if(S.getValue().getSpellID() == 808)
            {
                cell = cellID;
                SS = S.getValue();
            }
        }
        if (target == null)
            return 666;
        int attack = fight.tryCastSpell(fighter, SS, cell);

        if (attack != 0)
            return 800;
        return 0;
    }

    public int attackIfPossibleWobot(Pelea fight, Peleador fighter)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SortStats SS = null;
        int cell = 0;
        for (Map.Entry<Integer, SortStats> S : fighter.getMob().getSpells().entrySet())
        {
            int cellID = Camino.getCaseBetweenEnemy(fighter.getCell().getId(), fight.getMap(), fight);
            if(S.getValue().getSpellID() == 335)
            {
                cell = cellID;
                SS = S.getValue();
            }
        }
        int attack = fight.tryCastSpell(fighter, SS, cell);

        if (attack != 0)
            return 800;
        return 0;
    }
    public void attackIfPossibleTynril(Pelea fight, Peleador fighter, Peleador target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return;
        SortStats SS = null;
        if (target == null)
            return;
        if(fighter.getMob().getTemplate().getId() == 1087) {//ahuri  /faiblesse terre
            if(target.hasBuff(215)) {
                SS = findSpell(fighter, 1059);
            } else {
                SS = findSpell(fighter, 1058);
            }
        } else if(fighter.getMob().getTemplate().getId() == 1085) {//deconcerter  /faiblesse eau
            if(target.hasBuff(216)) {
                SS = findSpell(fighter, 1059);
            } else {
                SS = findSpell(fighter, 1058);
            }
        } else if(fighter.getMob().getTemplate().getId() == 1072) {//consterner  /faiblesse air
            if(target.hasBuff(217)) {
                SS = findSpell(fighter, 1059);
            } else {
                SS = findSpell(fighter, 1058);
            }
        } else if(fighter.getMob().getTemplate().getId() == 1086) {//perfide  /faiblesse feu
            if(target.hasBuff(218)) {
                SS = findSpell(fighter, 1059);
            } else {
                SS = findSpell(fighter, 1058);
            }
        }

        if(fight.canCastSpell1(fighter, SS, target.getCell(), -1)) {
            fight.tryCastSpell(fighter, SS, target.getCell().getId());
        }
    }

    public SortStats findSpell(Peleador fighter, int id) {
        for(SortStats spell : fighter.getMob().getSpells().values()) {
            if(spell != null && spell.getSpellID() == id)
                return spell;
        }
        return null;
    }

    public boolean moveNearIfPossible(Pelea fight, Peleador F, Peleador T)
    {
        if (fight == null)
            return false;
        if (F == null)
            return false;
        if (T == null)
            return false;
        if (F.getCurPm(fight) <= 0)
            return false;
        Mapa map = fight.getMap();
        if (map == null)
            return false;
        GameCase cell = F.getCell();
        if (cell == null)
            return false;
        GameCase cell2 = T.getCell();
        if (cell2 == null)
            return false;
        if (Camino.isNextTo(map, cell.getId(), cell2.getId()))
            return false;

        int cellID = Camino.getNearestCellAround(map, cell2.getId(), cell.getId(), null);
        //On demande le chemin plus court
        //Mais le chemin le plus court ne prend pas en compte les bords de map.
        if (cellID == -1)
        {
            Map<Integer, Peleador> ennemys = getLowHpEnnemyList(fight, F);
            for (Map.Entry<Integer, Peleador> target : ennemys.entrySet())
            {
                int cellID2 = Camino.getNearestCellAround(map, target.getValue().getCell().getId(), cell.getId(), null);
                if (cellID2 != -1)
                {
                    cellID = cellID2;
                    break;
                }
            }
        }
        ArrayList<GameCase> path = new IACamino(fight.getMapOld(), fight, cell.getId(), cell2.getId()).getShortestPath(-1);
        if (path == null || path.isEmpty())
            return false;
        ArrayList<GameCase> finalPath = new ArrayList<>();
        for (int a = 0; a < F.getCurPm(fight); a++)
        {
            if (path.size() == a)
                break;
            finalPath.add(path.get(a));
        }
        StringBuilder pathstr = new StringBuilder();
        try
        {
            int curCaseID = cell.getId();
            int curDir = 0;
            for (GameCase c : finalPath)
            {
                char d = Camino.getDirBetweenTwoCase(curCaseID, c.getId(), map, true);
                if (d == 0)
                    return false;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (finalPath.indexOf(c) != 0)
                        pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
                    pathstr.append(d);
                }
                curCaseID = c.getId();
            }
            if (curCaseID != cell.getId())
                pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        AccionJuego GA = new AccionJuego(0, 1, "");
        GA.args = pathstr.toString();
        boolean result = fight.onFighterDeplace(F, GA);

        return result;
    }

    public int getMaxCellForTP(Pelea fight, Peleador F, Peleador T, int dist) {
        if (fight == null || F == null || T == null || dist < 1)
            return -1;

        Mapa map = fight.getMap();
        GameCase cell = F.getCell(), cell2 = T.getCell();

        if (map == null || cell == null || cell2 == null || Camino.isNextTo(map, cell.getId(), cell2.getId()))
            return -1;

        ArrayList<GameCase> path = new IACamino(fight.getMapOld(), fight, cell.getId(), cell2.getId()).getShortestPath(-1);

        if (path == null || path.isEmpty())
            return -1;

        int cellId = -1;

        for (int a = 0; a < dist; a++) {
            if (path.size() == a) break;
            cellId = path.get(a).getId();
        }

        return cellId;
    }

    public int attackIfPossibleDiscipleimpair(Pelea fight, Peleador fighter, Peleador target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SortStats SS = null;
        for(Map.Entry<Integer, SortStats> entry : fighter.getMob().getSpells().entrySet())
        {
            SortStats a = entry.getValue();
            if(a.getSpellID() == 3501)
                SS = a;
        }
        if (target == null)
            return 666;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public int attackBondIfPossible(Pelea fight, Peleador fighter, Peleador target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        int cell = 0;
        SortStats SS2 = null;

        if(target == null)
            return 0;
        for (Map.Entry<Integer, SortStats> S : fighter.getMob().getSpells().entrySet())
        {
            int cellID = Camino.getCaseBetweenEnemy(target.getCell().getId(), fight.getMap(), fight);
            boolean effet4 = false;
            boolean effet6 = false;

            for(EfectoHechizo f : S.getValue().getEffects())
            {
                if(f.getEffectID() == 4)
                    effet4 = true;
                if(f.getEffectID() == 6)
                {
                    effet6 = true;
                    effet4 = true;
                }
            }
            if(!effet4)
                continue;
            if(!effet6)
            {
                cell = cellID;
                SS2 = S.getValue();
            }else
            {
                cell = target.getCell().getId();
                SS2 = S.getValue();
            }
        }
        if (cell >= 15 && cell <= 463 && SS2 != null)
        {
            int attack = fight.tryCastSpell(fighter, SS2, cell);
            if (attack != 0)
                return SS2.getSpell().getDuration();
        }
        else
        {
            if (target == null || SS2 == null)
                return 0;
            int attack = fight.tryCastSpell(fighter, SS2, cell);
            if (attack != 0)
                return SS2.getSpell().getDuration();
        }
        return 0;
    }

    public int attackIfPossibleDisciplepair(Pelea fight, Peleador fighter, Peleador target)
    {// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
        if (fight == null || fighter == null) return 0;
        SortStats SS = null;

        for(SortStats spellStats : fighter.getMob().getSpells().values())
            if(spellStats.getSpellID() == 3500)
                SS = spellStats;

        if (target == null) return 666;

        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());

        if (attack != 0) return attack;
        return 0;
    }

    public int moveFarIfPossible(Pelea fight, Peleador F)
    {
        if (fight == null || F == null)
            return 0;
        if (fight.getMap() == null)
            return 0;
        int nbrcase = 0;
        //On cr�er une liste de distance entre ennemi et de cellid, nous permet de savoir si un ennemi est coll� a nous
        int[] dist = {1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000};
        int[] cell = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < 10; i++)//on repete 10 fois pour les 10 joueurs ennemis potentielle
        {
            for (Peleador f : fight.getFighters(3))
            {

                if (f.isDead())
                    continue;
                if (f == F || f.getTeam() == F.getTeam())
                    continue;
                int cellf = f.getCell().getId();
                if (cellf == cell[0] || cellf == cell[1] || cellf == cell[2]
                        || cellf == cell[3] || cellf == cell[4]
                        || cellf == cell[5] || cellf == cell[6]
                        || cellf == cell[7] || cellf == cell[8]
                        || cellf == cell[9])
                    continue;
                int d = 0;
                d = Camino.getDistanceBetween(fight.getMap(), F.getCell().getId(), f.getCell().getId());
                if (d < dist[i])
                {
                    dist[i] = d;
                    cell[i] = cellf;
                }
                if (dist[i] == 1000)
                {
                    dist[i] = 0;
                    cell[i] = F.getCell().getId();
                }
            }
        }
        //if(dist[0] == 0)return false;//Si ennemi "coll�"

        int[] dist2 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int PM = F.getCurPm(fight), caseDepart = F.getCell().getId(), destCase = F.getCell().getId();
        ArrayList<Integer> caseUse = new ArrayList<>();
        caseUse.add(caseDepart); // On ne revient pas a sa position de d�part
        for (int i = 0; i <= PM; i++)//Pour chaque PM on analyse la meilleur case a prendre. C'est a dire la plus �liogn�e de tous.
        {
            if (destCase > 0)
                caseDepart = destCase;
            int curCase = caseDepart;

           //En +15
            curCase += 15;
            int infl = 0, inflF = 0;
            for (int a = 0; a < 10 && dist[a] != 0; a++)
            {
                dist2[a] = Camino.getDistanceBetween(fight.getMap(), curCase, cell[a]);//pour chaque ennemi on calcul la nouvelle distance depuis cette nouvelle case (curCase)
                if (dist2[a] > dist[a])//Si la cellule (curCase) demander et plus distante que la pr�cedente de l'ennemi alors on dirrige le mouvement vers elle
                    infl++;
            }

            if (infl > inflF
                    && curCase >= 15
                    && curCase <= 463
                    && testCotes(destCase, curCase)
                    && fight.getMap().getCase(curCase).isWalkable(false, true, -1)
                    && fight.getMap().getCase(curCase).getFighters().isEmpty()
                    && !caseUse.contains(curCase))//Si l'influence (infl) est la plus forte en comparaison avec inflF on garde la case si celle-ci est valide
            {
                inflF = infl;
                destCase = curCase;
            }
            //En +15

            //En +14
            curCase = caseDepart + 14;
            infl = 0;

            for (int a = 0; a < 10 && dist[a] != 0; a++)
            {
                dist2[a] = Camino.getDistanceBetween(fight.getMap(), curCase, cell[a]);
                if (dist2[a] > dist[a])
                    infl++;
            }

            if (infl > inflF
                    && curCase >= 15
                    && curCase <= 463
                    && testCotes(destCase, curCase)
                    && fight.getMap().getCase(curCase).isWalkable(false, true, -1)
                    && fight.getMap().getCase(curCase).getFighters().isEmpty()
                    && !caseUse.contains(curCase))
            {
                inflF = infl;
                destCase = curCase;
            }
            //En +14

            //En -15
            curCase = caseDepart - 15;
            infl = 0;
            for (int a = 0; a < 10 && dist[a] != 0; a++)
            {
                dist2[a] = Camino.getDistanceBetween(fight.getMap(), curCase, cell[a]);
                if (dist2[a] > dist[a])
                    infl++;
            }

            if (infl > inflF
                    && curCase >= 15
                    && curCase <= 463
                    && testCotes(destCase, curCase)
                    && fight.getMap().getCase(curCase).isWalkable(false, true, -1)
                    && fight.getMap().getCase(curCase).getFighters().isEmpty()
                    && !caseUse.contains(curCase))
            {
                inflF = infl;
                destCase = curCase;
            }
            //En -15

            //En -14
            curCase = caseDepart - 14;
            infl = 0;
            for (int a = 0; a < 10 && dist[a] != 0; a++)
            {
                dist2[a] = Camino.getDistanceBetween(fight.getMap(), curCase, cell[a]);
                if (dist2[a] > dist[a])
                    infl++;
            }

            if (infl > inflF
                    && curCase >= 15
                    && curCase <= 463
                    && testCotes(destCase, curCase)
                    && fight.getMap().getCase(curCase).isWalkable(false, true, -1)
                    && fight.getMap().getCase(curCase).getFighters().isEmpty()
                    && !caseUse.contains(curCase))
            {
                inflF = infl;
                destCase = curCase;
            }
            //En -14
            caseUse.add(destCase);
        }
        if (destCase < 15
                || destCase > 463
                || destCase == F.getCell().getId()
                || !fight.getMap().getCase(destCase).isWalkable(false, true, -1))
            return 0;

        if (F.getPm() <= 0)
            return 0;
        ArrayList<GameCase> path = new IACamino(fight.getMap(), fight, F.getCell().getId(), destCase).getShortestPath(-1);
        if (path == null)
            return 0;
        ArrayList<GameCase> finalPath = new ArrayList<>();
        for (int a = 0; a < F.getCurPm(fight); a++)
        {
            if (path.size() == a)
                break;
            finalPath.add(path.get(a));
        }
        StringBuilder pathstr = new StringBuilder();
        try
        {
            int curCaseID = F.getCell().getId();
            int curDir = 0;
            for (GameCase c : finalPath)
            {
                char d = Camino.getDirBetweenTwoCase(curCaseID, c.getId(), fight.getMap(), true);
                if (d == 0)
                    return 0;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (finalPath.indexOf(c) != 0)
                        pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
                    pathstr.append(d);
                }
                curCaseID = c.getId();

                nbrcase = nbrcase + 1;
            }
            if (curCaseID != F.getCell().getId())
                pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        AccionJuego GA = new AccionJuego(0, 1, "");
        GA.args = pathstr.toString();

        if(!fight.onFighterDeplace(F, GA))
            return 0;

        return nbrcase * 500;
    }

    public boolean testCotes(int cellWeAre, int cellWego)//Nous permet d'interdire le d�placement du bord vers des cellules hors map
    {
        if (cellWeAre == 15 || cellWeAre == 44 || cellWeAre == 73
                || cellWeAre == 102 || cellWeAre == 131 || cellWeAre == 160
                || cellWeAre == 189 || cellWeAre == 218 || cellWeAre == 247
                || cellWeAre == 276 || cellWeAre == 305 || cellWeAre == 334
                || cellWeAre == 363 || cellWeAre == 392 || cellWeAre == 421
                || cellWeAre == 450)
        {
            if (cellWego == cellWeAre + 14 || cellWego == cellWeAre - 15)
                return false;
        }
        if (cellWeAre == 28 || cellWeAre == 57 || cellWeAre == 86
                || cellWeAre == 115 || cellWeAre == 144 || cellWeAre == 173
                || cellWeAre == 202 || cellWeAre == 231 || cellWeAre == 260
                || cellWeAre == 289 || cellWeAre == 318 || cellWeAre == 347
                || cellWeAre == 376 || cellWeAre == 405 || cellWeAre == 434
                || cellWeAre == 463)
        {
            if (cellWego == cellWeAre + 15 || cellWego == cellWeAre - 14)
                return false;
        }

        if (cellWeAre >= 451 && cellWeAre <= 462)
        {
            if (cellWego == cellWeAre + 15 || cellWego == cellWeAre + 14)
                return false;
        }
        if (cellWeAre >= 16 && cellWeAre <= 27)
        {
            if (cellWego == cellWeAre - 15 || cellWego == cellWeAre - 14)
                return false;
        }
        return true;
    }

    public boolean invocIfPossible(Pelea fight, Peleador fighter)
    {
        if (fight == null || fighter == null)
            return false;
        if (fighter.nbInvocation() >= fighter.getTotalStats().getEffect(Constantes.STATS_CREATURE))
            return false;
        Peleador nearest = getNearestEnnemy(fight, fighter);
        if (nearest == null)
            return false;
        int nearestCell = fighter.getCell().getId();
        int limit = 30;
        int _loc0_ = 0;
        SortStats spell = null;
        while ((spell = getInvocSpell(fight, fighter, nearestCell)) == null
                && _loc0_++ < limit)
        {
            nearestCell = Camino.getCaseBetweenEnemy(fighter.getCell().getId(), fight.getMap(), fight);
        }
        if (nearestCell == -1)
            return false;
        if (spell == null)
            return false;
        int invoc = fight.tryCastSpell(fighter, spell, nearestCell);
        if (invoc != 0)
            return false;
        return true;
    }

    public boolean invocIfPossible(Pelea fight, Peleador fighter, List<SortStats> Spelllist)
    {
        if (fight == null || fighter == null)
            return false;
        if (fighter.nbInvocation() >= fighter.getTotalStats().getEffect(Constantes.STATS_CREATURE))
            return false;
        Peleador nearest = getNearestEnnemy(fight, fighter);
        if (nearest == null)
            return false;
        int nearestCell = fighter.getCell().getId();
        int limit = 10;
        int _loc0_ = 0;
        SortStats spell = null;
        while ((spell = getInvocSpellDopeul(fight, fighter, nearestCell, Spelllist)) == null
                && _loc0_++ < limit)
        {
            nearestCell = Camino.getCaseBetweenEnemy(fighter.getCell().getId(), fight.getMap(), fight);
        }
        if (nearestCell == -1)
            return false;
        if (spell == null)
            return false;
        int invoc = fight.tryCastSpell(fighter, spell, nearestCell);
        if (invoc != 0)
            return false;
        return true;
    }

    public boolean invocIfPossibleloin(Pelea fight, Peleador fighter, List<SortStats> Spelllist)
    {
        if (fight == null || fighter == null)
            return false;
        if (fighter.nbInvocation() >= fighter.getTotalStats().getEffect(Constantes.STATS_CREATURE))
            return false;
        Peleador nearest = getNearestEnnemy(fight, fighter);
        if (nearest == null)
            return false;
        int nearestCell = fighter.getCell().getId();
        int limit = 10;
        int _loc0_ = 0;
        SortStats spell = null;
        while ((spell = getInvocSpellDopeul(fight, fighter, nearestCell, Spelllist)) == null
                && _loc0_++ < limit)
        {
            nearestCell = Camino.getNearestCellAround(fight.getMap(),
                    nearestCell, nearest.getCell().getId(), null);
        }
        if (nearestCell == -1)
            return false;
        if (spell == null)
            return false;
        int invoc = fight.tryCastSpell(fighter, spell, nearestCell);
        if (invoc != 0)
            return false;
        return true;
    }

    public SortStats getInvocSpell(Pelea fight, Peleador fighter, int nearestCell)
    {
        if (fight == null || fighter == null)
            return null;
        if (fighter.getMob() == null)
            return null;
        if (fight.getMap() == null)
            return null;
        if (fight.getMap().getCase(nearestCell) == null)
            return null;
        for (Map.Entry<Integer, SortStats> SS : fighter.getMob().getSpells().entrySet()) {
            if (!fight.canCastSpell1(fighter, SS.getValue(), fight.getMap().getCase(nearestCell), -1))
                continue;
            for (EfectoHechizo SE : SS.getValue().getEffects())
                if (SE.getEffectID() == 181)
                    return SS.getValue();
        }
        return null;
    }

    public SortStats getInvocSpellDopeul(Pelea fight, Peleador fighter, int nearestCell, List<SortStats> Spelllist)
    {
        if (fight == null || fighter == null)
            return null;
        if (fighter.getMob() == null)
            return null;
        if (fight.getMap() == null)
            return null;
        if (fight.getMap().getCase(nearestCell) == null)
            return null;
        for (SortStats SS : Spelllist)
        {
            if (!fight.canCastSpell1(fighter, SS, fight.getMap().getCase(nearestCell), -1))
                continue;
            for (EfectoHechizo SE : SS.getEffects())
            {
                if (SE.getEffectID() == 181)
                    return SS;
            }
        }
        return null;
    }

    public int HealIfPossible(Pelea fight, Peleador f, boolean autoSoin, int PDVPERmin)//boolean pour choisir entre auto-soin ou soin alli�
    {
        if (fight == null || f == null)
            return 0;
        if (f.isDead())
            return 0;
        if (autoSoin && (f.getPdv() * 100) / f.getPdvMax() > 95)
            return 0;
        Peleador target = null;
        SortStats SS = null;
        if (autoSoin)
        {
            int PDVPER = (f.getPdv() * 100) / f.getPdvMax();
            if (PDVPER < PDVPERmin && PDVPER < 95)
            {
                target = f;
                SS = getHealSpell(fight, f, target);
            }
        }
        else
        //s�lection joueur ayant le moins de pv
        {
            Peleador curF = null;
            //int PDVPERmin = 100;
            SortStats curSS = null;
            for (Peleador F : fight.getFighters(3))
            {
                if (f.isDead())
                    continue;
                if (F == f)
                    continue;
                if (F.isDead())
                    continue;
                if (F.getTeam() == f.getTeam())
                {
                    int PDVPER = (F.getPdv() * 100) / F.getPdvMax();
                    if (PDVPER < PDVPERmin && PDVPER < 95)
                    {
                        int infl = 0;
                        if (f.isCollector())
                        {
                            for (Map.Entry<Integer, SortStats> ss : Mundo.mundo.getGuild(f.getCollector().getGuildId()).getSpells().entrySet())
                            {
                                if (ss.getValue() == null)
                                    continue;
                                if (infl < calculInfluenceHeal(ss.getValue())
                                        && calculInfluenceHeal(ss.getValue()) != 0
                                        && fight.canCastSpell1(f, ss.getValue(), F.getCell(), -1))//Si le sort est plus interessant
                                {
                                    infl = calculInfluenceHeal(ss.getValue());
                                    curSS = ss.getValue();
                                }
                            }
                        }
                        else
                        {
                            for (Map.Entry<Integer, SortStats> ss : f.getMob().getSpells().entrySet())
                            {
                                if (infl < calculInfluenceHeal(ss.getValue())
                                        && calculInfluenceHeal(ss.getValue()) != 0
                                        && fight.canCastSpell1(f, ss.getValue(), F.getCell(), -1))//Si le sort est plus interessant
                                {
                                    infl = calculInfluenceHeal(ss.getValue());
                                    curSS = ss.getValue();
                                }
                            }
                        }
                        if (curSS != SS && curSS != null)
                        {
                            curF = F;
                            SS = curSS;
                            PDVPERmin = PDVPER;
                        }
                    }
                }
            }
            target = curF;
        }
        if (target == null)
            return 0;
        if (target.isFullPdv())
            return 0;
        if (SS == null)
            return 0;
        int heal = fight.tryCastSpell(f, SS, target.getCell().getId());
        if (heal != 0)
            return SS.getSpell().getDuration();

        return 0;
    }

    public int HealIfPossible(Pelea fight, Peleador f)//boolean pour choisir entre auto-soin ou soin alli�
    {
        if (fight == null || f == null)
            return 0;
        if (f.isDead())
            return 0;
        Peleador target = null;
        SortStats SS = null;
        target = f;
        SS = Mundo.mundo.getSort(587).getStatsByLevel(f.getLvl());
        if (SS == null)
            return 0;
        int heal = fight.tryCastSpell(f, SS, target.getCell().getId());
        if (heal != 0)
            return SS.getSpell().getDuration();
        return 0;
    }

    public int HealIfPossible(Pelea fight, Peleador f , Peleador A)//boolean pour choisir entre auto-soin ou soin alli�
    {
        if (fight == null || f == null || A == null)
            return 0;
        if (f.isDead())
            return 0;
        SortStats SS = null;
        SS = Mundo.mundo.getSort(210).getStatsByLevel(f.getLvl());
        if (SS == null)
            return 0;
        int heal = fight.tryCastSpell(f, SS, A.getCell().getId());
        if (heal != 0)
            return SS.getSpell().getDuration();
        return 0;
    }


    public boolean HealIfPossible(Pelea fight, Peleador f, boolean autoSoin)//boolean pour choisir entre auto-soin ou soin alli�
    {
        if (fight == null || f == null)
            return false;
        if (f.isDead())
            return false;
        if (autoSoin && (f.getPdv() * 100) / f.getPdvMax() > 95)
            return false;
        Peleador target = null;
        SortStats SS = null;
        if (autoSoin)
        {
            target = f;
            SS = getHealSpell(fight, f, target);
        }
        else
        //s�lection joueur ayant le moins de pv
        {
            Peleador curF = null;
            int PDVPERmin = 100;
            SortStats curSS = null;
            for (Peleador F : fight.getFighters(3))
            {
                if (f.isDead())
                    continue;
                if (F == f)
                    continue;
                if (F.isDead())
                    continue;
                if (F.getTeam() == f.getTeam())
                {
                    int PDVPER = (F.getPdv() * 100) / F.getPdvMax();
                    if (PDVPER < PDVPERmin && PDVPER < 95)
                    {
                        int infl = 0;
                        if (f.isCollector())
                        {
                            for (Map.Entry<Integer, SortStats> ss : Mundo.mundo.getGuild(f.getCollector().getGuildId()).getSpells().entrySet())
                            {
                                if (ss.getValue() == null)
                                    continue;
                                if (infl < calculInfluenceHeal(ss.getValue())
                                        && calculInfluenceHeal(ss.getValue()) != 0
                                        && fight.canCastSpell1(f, ss.getValue(), F.getCell(), -1))//Si le sort est plus interessant
                                {
                                    infl = calculInfluenceHeal(ss.getValue());
                                    curSS = ss.getValue();
                                }
                            }
                        }
                        else
                        {
                            for (Map.Entry<Integer, SortStats> ss : f.getMob().getSpells().entrySet())
                            {
                                if (infl < calculInfluenceHeal(ss.getValue())
                                        && calculInfluenceHeal(ss.getValue()) != 0
                                        && fight.canCastSpell1(f, ss.getValue(), F.getCell(), -1))//Si le sort est plus interessant
                                {
                                    infl = calculInfluenceHeal(ss.getValue());
                                    curSS = ss.getValue();
                                }
                            }
                        }
                        if (curSS != SS && curSS != null)
                        {
                            curF = F;
                            SS = curSS;
                            PDVPERmin = PDVPER;
                        }
                    }
                }
            }
            target = curF;
        }
        if (target == null)
            return false;
        if (target.isFullPdv())
            return false;
        if (SS == null)
            return false;
        int heal = fight.tryCastSpell(f, SS, target.getCell().getId());
        if (heal != 0)
            return false;

        return true;
    }

    public boolean buffIfPossible(Pelea fight, Peleador fighter, Peleador target)
    {
        if (fight == null || fighter == null)
            return false;
        if (target == null)
            return false;
        SortStats SS = getBuffSpell(fight, fighter, target);
        if (SS == null)
            return false;
        int buff = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (buff != 0)
            return false;
        return true;
    }

    public SortStats getBuffSpell(Pelea fight, Peleador F, Peleador T)
    {
        if (fight == null || F == null)
            return null;
        int infl = -1500000;
        SortStats ss = null;
        if (F.isCollector())
        {
            for (Map.Entry<Integer, SortStats> SS : Mundo.mundo.getGuild(F.getCollector().getGuildId()).getSpells().entrySet())
            {
                if (SS.getValue() == null)
                    continue;
                if (infl < calculInfluence(SS.getValue(), F, T)
                        && calculInfluence(SS.getValue(), F, T) > 0
                        && fight.canCastSpell1(F, SS.getValue(), T.getCell(), -1))//Si le sort est plus interessant
                {
                    infl = calculInfluence(SS.getValue(), F, T);
                    ss = SS.getValue();
                }
            }
        }
        else
        {
            for (Map.Entry<Integer, SortStats> SS : F.getMob().getSpells().entrySet())
            {
                int inf = calculInfluence(SS.getValue(), F, T);
                if (infl < inf
                        && SS.getValue().getSpell().getType() == 1
                        && fight.canCastSpell1(F, SS.getValue(), T.getCell(), -1))//Si le sort est plus interessant
                {
                    infl = calculInfluence(SS.getValue(), F, T);
                    ss = SS.getValue();
                }
            }
        }
        return ss;
    }

    public boolean buffIfPossible(Pelea fight, Peleador fighter, Peleador target, List<SortStats> Spelllist)
    {
        if (fight == null || fighter == null)
            return false;
        if (target == null)
            return false;
        SortStats SS = getBuffSpellDopeul(fight, fighter, target, Spelllist);
        if (SS == null)
            return false;
        int buff = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (buff != 0)
            return true;
        return false;
    }

    public SortStats getBuffSpellDopeul(Pelea fight, Peleador F, Peleador T, List<SortStats> Spelllist)
    {
        if (fight == null || F == null)
            return null;
        int infl = -1500000;
        SortStats ss = null;
        for (SortStats SS : Spelllist)
        {
            int inf = calculInfluence(SS, F, T);

            if (infl < inf
                    && SS.getSpell().getType() == 1
                    && fight.canCastSpell1(F, SS, T.getCell(), -1))//Si le sort est plus interessant
            {
                infl = calculInfluence(SS, F, T);
                ss = SS;
            }
        }
        return ss;
    }

    public SortStats getHealSpell(Pelea fight, Peleador F, Peleador T)
    {
        if (fight == null || F == null)
            return null;
        int infl = 0;
        SortStats ss = null;
        if (F.isCollector())
        {
            for (Map.Entry<Integer, SortStats> SS : Mundo.mundo.getGuild(F.getCollector().getGuildId()).getSpells().entrySet())
            {
                if (SS.getValue() == null)
                    continue;
                if (infl < calculInfluenceHeal(SS.getValue())
                        && calculInfluenceHeal(SS.getValue()) != 0
                        && fight.canCastSpell1(F, SS.getValue(), T.getCell(), -1))//Si le sort est plus interessant
                {
                    infl = calculInfluenceHeal(SS.getValue());
                    ss = SS.getValue();
                }
            }
        }
        else
        {
            for (Map.Entry<Integer, SortStats> SS : F.getMob().getSpells().entrySet())
            {
                if (SS.getValue() == null)
                    continue;
                if (infl < calculInfluenceHeal(SS.getValue())
                        && calculInfluenceHeal(SS.getValue()) != 0
                        && fight.canCastSpell1(F, SS.getValue(), T.getCell(), -1))//Si le sort est plus interessant
                {
                    infl = calculInfluenceHeal(SS.getValue());
                    ss = SS.getValue();
                }
            }
        }
        return ss;
    }

    public int moveautourIfPossible(Pelea fight, Peleador F, Peleador T)
    {
        if (fight == null)
            return 0;
        if (F == null)
            return 0;
        if (T == null)
            return 0;
        if (F.getCurPm(fight) <= 0)
            return 0;
        Mapa map = fight.getMap();
        if (map == null)
            return 0;
        GameCase cell = F.getCell();
        if (cell == null)
            return 0;
        GameCase cell2 = T.getCell();
        if (cell2 == null)
            return 0;
        if (Camino.isNextTo(map, cell.getId(), cell2.getId()))
            return 0;
        int nbrcase = 0;

        int cellID = Camino.getNearestCellAroundGA(map, cell2.getId(), cell.getId(), null);
        //On demande le chemin plus court
        //Mais le chemin le plus court ne prend pas en compte les bords de map.
        if (cellID == -1)
        {
            Map<Integer, Peleador> ennemys = getLowHpEnnemyList(fight, F);
            for (Map.Entry<Integer, Peleador> target : ennemys.entrySet())
            {
                int cellID2 = Camino.getNearestCellAroundGA(map, target.getValue().getCell().getId(), cell.getId(), null);
                if (cellID2 != -1)
                {
                    cellID = cellID2;
                    break;
                }
            }
        }
        ArrayList<GameCase> path = new IACamino(fight.getMapOld(), fight, cell.getId(), cellID).getShortestPath(-1);
        if (path == null || path.isEmpty())
            return 0;

        ArrayList<GameCase> finalPath = new ArrayList<>();
        for (int a = 0; a < F.getCurPm(fight); a++)
        {
            if (path.size() == a)
                break;
            finalPath.add(path.get(a));
        }
        StringBuilder pathstr = new StringBuilder();
        try
        {
            int curCaseID = cell.getId();
            int curDir = 0;
            for (GameCase c : finalPath)
            {
                char d = Camino.getDirBetweenTwoCase(curCaseID, c.getId(), map, true);
                if (d == 0)
                    return 0;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (finalPath.indexOf(c) != 0)
                        pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
                    pathstr.append(d);
                }
                curCaseID = c.getId();

                nbrcase = nbrcase + 1;
            }
            if (curCaseID != cell.getId())
                pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        AccionJuego GA = new AccionJuego(0, 1, "");
        GA.args = pathstr.toString();
        if(!fight.onFighterDeplace(F, GA))
            return 0;

        return nbrcase * 500;
    }



    public int moveIfPossiblecontremur(Pelea fight, Peleador F, Peleador T)
    {
        if (fight == null)
            return 0;
        if (F == null)
            return 0;
        if (T == null)
            return 0;
        if (F.getCurPm(fight) <= 0)
            return 0;
        Mapa map = fight.getMap();
        if (map == null)
            return 0;
        GameCase cell = F.getCell();
        if (cell == null)
            return 0;
        GameCase cell2 = T.getCell();
        if (cell2 == null)
            return 0;
        if (Camino.isNextTo(map, cell.getId(), cell2.getId()))
            return 0;
        int nbrcase = 0;

        int cellID = Camino.getNearenemycontremur(map, cell2.getId(), cell.getId(), null);
        //On demande le chemin plus court
        //Mais le chemin le plus court ne prend pas en compte les bords de map.
        if (cellID == -1)
        {
            Map<Integer, Peleador> ennemys = getLowHpEnnemyList(fight, F);
            for (Map.Entry<Integer, Peleador> target : ennemys.entrySet())
            {
                int cellID2 = Camino.getNearestCellAroundGA(map, target.getValue().getCell().getId(), cell.getId(), null);
                if (cellID2 != -1)
                {
                    cellID = cellID2;
                    break;
                }
            }
        }
        ArrayList<GameCase> path = new IACamino(fight.getMapOld(), fight, cell.getId(), cellID).getShortestPath(-1);
        if (path == null || path.isEmpty())
            return 0;

        ArrayList<GameCase> finalPath = new ArrayList<>();
        for (int a = 0; a < F.getCurPm(fight); a++)
        {
            if (path.size() == a)
                break;
            finalPath.add(path.get(a));
        }
        StringBuilder pathstr = new StringBuilder();
        try
        {
            int curCaseID = cell.getId();
            int curDir = 0;
            for (GameCase c : finalPath)
            {
                char d = Camino.getDirBetweenTwoCase(curCaseID, c.getId(), map, true);
                if (d == 0)
                    return 0;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (finalPath.indexOf(c) != 0)
                        pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
                    pathstr.append(d);
                }
                curCaseID = c.getId();

                nbrcase = nbrcase + 1;
            }
            if (curCaseID != cell.getId())
                pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        AccionJuego GA = new AccionJuego(0, 1, "");
        GA.args = pathstr.toString();
        if(!fight.onFighterDeplace(F, GA))
            return 0;

        return nbrcase * 500;
    }

    public int movediagIfPossible(Pelea fight, Peleador F, Peleador T)
    {
        if (fight == null)
            return 0;
        if (F == null)
            return 0;
        if (T == null)
            return 0;
        if (F.getCurPm(fight) <= 0)
            return 0;
        Mapa map = fight.getMap();
        if (map == null)
            return 0;
        GameCase cell = F.getCell();
        if (cell == null)
            return 0;
        GameCase cell2 = T.getCell();
        if (cell2 == null)
            return 0;
        if (Camino.isNextTo(map, cell.getId(), cell2.getId()))
            return 0;
        int nbrcase = 0;

        int cellID = Camino.getNearestCellDiagGA(map, cell2.getId(), cell.getId(), null);
        //On demande le chemin plus court
        //Mais le chemin le plus court ne prend pas en compte les bords de map.
        if (cellID == -1)
        {
            Map<Integer, Peleador> ennemys = getLowHpEnnemyList(fight, F);
            for (Map.Entry<Integer, Peleador> target : ennemys.entrySet())
            {
                int cellID2 = Camino.getNearestCellDiagGA(map, target.getValue().getCell().getId(), cell.getId(), null);
                if (cellID2 != -1)
                {
                    cellID = cellID2;
                    break;
                }
            }
        }
        ArrayList<GameCase> path = new IACamino(fight.getMapOld(), fight, cell.getId(), cellID).getShortestPath(-1);
        if (path == null || path.isEmpty())
            return 0;

        ArrayList<GameCase> finalPath = new ArrayList<>();
        for (int a = 0; a < F.getCurPm(fight); a++)
        {
            if (path.size() == a)
                break;
            finalPath.add(path.get(a));
        }
        StringBuilder pathstr = new StringBuilder();
        try
        {
            int curCaseID = cell.getId();
            int curDir = 0;
            for (GameCase c : finalPath)
            {
                char d = Camino.getDirBetweenTwoCase(curCaseID, c.getId(), map, true);
                if (d == 0)
                    return 0;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (finalPath.indexOf(c) != 0)
                        pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
                    pathstr.append(d);
                }
                curCaseID = c.getId();

                nbrcase = nbrcase + 1;
            }
            if (curCaseID != cell.getId())
                pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        AccionJuego GA = new AccionJuego(0, 1, "");
        GA.args = pathstr.toString();
        if(!fight.onFighterDeplace(F, GA))
            return 0;

        return nbrcase * 500;
    }

    public int moveenfaceIfPossible(Pelea fight, Peleador F, Peleador T, int dist)
    {
        if (fight == null)
            return 0;
        if (F == null)
            return 0;
        if (T == null)
            return 0;
        if (F.getCurPm(fight) <= 0)
            return 0;
        Mapa map = fight.getMap();
        if (map == null)
            return 0;
        GameCase cell = F.getCell();
        if (cell == null)
            return 0;
        GameCase cell2 = T.getCell();
        if (cell2 == null)
            return 0;
        if (Camino.isNextTo(map, cell.getId(), cell2.getId()))
            return 0;
        int nbrcase = 0;

        int cellID = Camino.getNearestligneGA(fight, cell2.getId(), cell.getId(), null, dist);
        //On demande le chemin plus court
        //Mais le chemin le plus court ne prend pas en compte les bords de map.
        if (cellID == -1)
        {
            Map<Integer, Peleador> ennemys = getLowHpEnnemyList(fight, F);
            for (Map.Entry<Integer, Peleador> target : ennemys.entrySet())
            {
                int cellID2 = Camino.getNearestligneGA(fight, target.getValue().getCell().getId(), cell.getId(), null, dist);
                if (cellID2 != -1)
                {
                    cellID = cellID2;
                    break;
                }
            }
        }
        ArrayList<GameCase> path = new IACamino(fight.getMapOld(), fight, cell.getId(), cellID).getShortestPath(0);//0pour en ligne
        if (path == null || path.isEmpty())
            return 0;

        ArrayList<GameCase> finalPath = new ArrayList<>();
        boolean ligneok = false;
        for (int a = 0; a < F.getCurPm(fight); a++)
        {
            if (path.size() == a)
                break;
            if(ligneok)
                break;
            if(Camino.casesAreInSameLine(fight.getMap(), path.get(a).getId(), T.getCell().getId(), 'z', 70))
                ligneok = true;
            finalPath.add(path.get(a));
        }
        StringBuilder pathstr = new StringBuilder();
        try
        {
            int curCaseID = cell.getId();
            int curDir = 0;
            for (GameCase c : finalPath)
            {
                char d = Camino.getDirBetweenTwoCase(curCaseID, c.getId(), map, true);
                if (d == 0)
                    return 0;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (finalPath.indexOf(c) != 0)
                        pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
                    pathstr.append(d);
                }
                curCaseID = c.getId();

                nbrcase = nbrcase + 1;
            }
            if (curCaseID != cell.getId())
                pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        AccionJuego GA = new AccionJuego(0, 1, "");
        GA.args = pathstr.toString();
        if(!fight.onFighterDeplace(F, GA))
            return 0;

        return nbrcase * 500;
    }

    public int movecacIfPossible(Pelea fight, Peleador F, Peleador T)
    {
        if (fight == null)
            return 0;
        if (F == null)
            return 0;
        if (T == null)
            return 0;
        if (F.getCurPm(fight) <= 0)
            return 0;
        Mapa map = fight.getMap();
        if (map == null)
            return 0;
        GameCase cell = F.getCell();
        if (cell == null)
            return 0;
        GameCase cell2 = T.getCell();
        if (cell2 == null)
            return 0;
        if (Camino.isNextTo(map, cell.getId(), cell2.getId()))
            return 0;
        int nbrcase = 0;

        int cellID = Camino.getNearestCellAround(map, cell2.getId(), cell.getId(), null);
        //On demande le chemin plus court
        //Mais le chemin le plus court ne prend pas en compte les bords de map.
        if (cellID == -1)
        {
            Map<Integer, Peleador> ennemys = getLowHpEnnemyList(fight, F);
            for (Map.Entry<Integer, Peleador> target : ennemys.entrySet())
            {
                int cellID2 = Camino.getNearestCellAround(map, target.getValue().getCell().getId(), cell.getId(), null);
                if (cellID2 != -1)
                {
                    cellID = cellID2;
                    break;
                }
            }
        }
        ArrayList<GameCase> path = new IACamino(fight.getMapOld(), fight, cell.getId(), cell2.getId()).getShortestPath(-1);
        if (path == null || path.isEmpty())
            return 0;

        ArrayList<GameCase> finalPath = new ArrayList<>();
        for (int a = 0; a < F.getCurPm(fight); a++)
        {
            if (path.size() == a)
                break;
            finalPath.add(path.get(a));
        }
        StringBuilder pathstr = new StringBuilder();
        try
        {
            int curCaseID = cell.getId();
            int curDir = 0;
            for (GameCase c : finalPath)
            {
                char d = Camino.getDirBetweenTwoCase(curCaseID, c.getId(), map, true);
                if (d == 0)
                    return 0;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (finalPath.indexOf(c) != 0)
                        pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
                    pathstr.append(d);
                }
                curCaseID = c.getId();

                nbrcase = nbrcase + 1;
            }
            if (curCaseID != cell.getId())
                pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        AccionJuego GA = new AccionJuego(0, 1, "");
        GA.args = pathstr.toString();
        if(!fight.onFighterDeplace(F, GA))
            return 0;

        return nbrcase * 500;
    }

    public Peleador getNearestFriendInvoc(Pelea fight, Peleador fighter)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        Peleador curF = null;
        for (Peleador f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            if (f.getTeam2() == fighter.getTeam2() && f.isInvocation())//Si c'est un ami et si c'est une invocation
            {
                int d = Camino.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < dist)
                {
                    dist = d;
                    curF = f;
                }
            }
        }
        return curF;
    }

    public Peleador getNearestFriendNoInvok(Pelea fight, Peleador fighter)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        Peleador curF = null;
        for (Peleador f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            if (f.getTeam2() == fighter.getTeam2() && !f.isInvocation())//Si c'est un ami et si c'est une invocation
            {
                int d = Camino.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < dist)
                {
                    dist = d;
                    curF = f;
                }
            }
        }
        return curF;
    }

    public Peleador getNearestFriend(Pelea fight, Peleador fighter)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        Peleador curF = null;
        for (Peleador f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            if (f.getTeam() == fighter.getTeam())//Si c'est un ami
            {
                int d = Camino.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < dist)
                {
                    dist = d;
                    curF = f;
                }
            }
        }
        return curF;
    }

    public Peleador getNearestEnnemy(Pelea fight, Peleador fighter)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        Peleador curF = null;
        for (Peleador f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f.getTeam2() != fighter.getTeam2())//Si c'est un ennemis
            {
                int d = Camino.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < dist)
                {
                    dist = d;
                    curF = f;
                }
            }
        }
        return curF;
    }

    public Peleador getNearestEnnemynbrcasemax(Pelea fight, Peleador fighter, int distmin, int distmax)
    {
        if (fight == null || fighter == null)
            return null;
        Peleador curF = null;
        for (Peleador f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if(f.getMob() != null && f.getMob().getTemplate() != null) {
                boolean ok = false;
                for (int i : Constantes.STATIC_INVOCATIONS)
                    if (i == f.getMob().getTemplate().getId())
                        ok = true;
                if(ok) continue;
            }

            if (f.getTeam2() != fighter.getTeam2())//Si c'est un ennemis
            {
                int d = Camino.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < distmax)
                {
                    if (d > distmin)
                    {
                        distmax = d;
                        curF = f;
                    }
                }
            }
        }
        return curF;
    }

    public Peleador getNearEnnemylignenbrcasemax(Pelea fight, Peleador fighter, int distmin, int distmax)
    {
        if (fight == null || fighter == null)
            return null;
        Peleador curF = null;

        for (Peleador f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if(f.getTeam2() != fighter.getTeam2())
                curF = Camino.getNearestligneenemy(fight.getMap(), fighter.getCell().getId(), f, distmax);


        }
        return curF;
    }

    public Peleador getNearestEnnemymurnbrcasemax(Pelea fight, Peleador fighter, int distmin, int distmax)
    {
        if (fight == null || fighter == null)
            return null;
        Peleador curF = null;
        for (Peleador f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f.getTeam2() != fighter.getTeam2())//Si c'est un ennemis
            {
                int d = Camino.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < distmax)
                {
                    if (d > distmin)
                    {
                        if(Camino.getNearenemycontremur2(fight.getMap(), f.getCell().getId(), fighter.getCell().getId(), null, fighter) == -1)
                            continue;
                        distmax = d;
                        curF = f;
                    }
                }
            }
        }
        if(curF == null)
        {
            for (Peleador f : fight.getFighters(3))
            {
                if (f.isDead())
                    continue;
                if (f.getTeam2() != fighter.getTeam2())//Si c'est un ennemis
                {
                    int d = Camino.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                    if (d < distmax)
                    {
                        if (d > distmin)
                        {
                            distmax = d;
                            curF = f;
                        }
                    }
                }
            }
        }
        return curF;
    }

    public Peleador getNearestAllnbrcasemax(Pelea fight, Peleador fighter, int distmin, int distmax)
    {
        if (fight == null || fighter == null)
            return null;
        Peleador curF = null;
        for (Peleador f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;

            int d = Camino.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
            if (d < distmax)
            {
                if (d > distmin)
                {
                    distmax = d;
                    curF = f;
                }
            }
        }
        return curF;
    }

    public Peleador getNearestAminbrcasemax(Pelea fight, Peleador fighter, int distmin, int distmax)
    {
        if (fight == null || fighter == null)
            return null;
        Peleador curF = null;
        for (Peleador f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f.getTeam2() == fighter.getTeam2())//Si c'est un ennemis
            {
                int d = Camino.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < distmax)
                {
                    if (d > distmin)
                    {
                        distmax = d;
                        curF = f;
                    }
                }
            }
        }
        return curF;
    }

    public Peleador getNearestAminoinvocnbrcasemax(Pelea fight, Peleador fighter, int distmin, int distmax)
    {
        if (fight == null || fighter == null)
            return null;
        Peleador curF = null;
        for (Peleador f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f.getTeam2() == fighter.getTeam2() && !f.isInvocation())//Si c'est un ennemis
            {
                int d = Camino.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < distmax)
                {
                    if (d > distmin)
                    {
                        distmax = d;
                        curF = f;
                    }
                }
            }
        }
        return curF;
    }

    public Peleador getNearestinvocateurnbrcasemax(Pelea fight, Peleador fighter, int distmin, int distmax)
    {
        if (fight == null || fighter == null)
            return null;
        Peleador curF = null;
        for (Peleador f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f.getTeam2() == fighter.getTeam2() && !f.isInvocation() && f == fighter.getInvocator())//Si c'est un ennemis
            {
                int d = Camino.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < distmax)
                {
                    if (d > distmin)
                    {
                        distmax = d;
                        curF = f;
                    }
                }
            }
        }
        if(curF == null)
            for (Peleador f : fight.getFighters(3))
            {
                if (f.isDead())
                    continue;
                if (f.getTeam2() == fighter.getTeam2() && !f.isInvocation())//Si c'est un ennemis
                {
                    int d = Camino.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                    if (d < distmax)
                    {
                        if (d > distmin)
                        {
                            distmax = d;
                            curF = f;
                        }
                    }
                }
            }
        return curF;
    }

    public Peleador getNearestInvocnbrcasemax(Pelea fight, Peleador fighter, int distmin, int distmax)
    {
        if (fight == null || fighter == null)
            return null;
        Peleador curF = null;
        for (Peleador f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f.getTeam2() == fighter.getTeam2() && f.isInvocation())//Si c'est un ennemis
            {
                int d = Camino.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < distmax)
                {
                    if (d > distmin)
                    {
                        distmax = d;
                        curF = f;
                    }
                }
            }
        }
        return curF;
    }

    public Map<Integer, Peleador> getLowHpEnnemyList(Pelea fight, Peleador fighter)
    {
        if (fight == null || fighter == null)
            return null;
        Map<Integer, Peleador> list = new HashMap<>();
        Map<Integer, Peleador> ennemy = new HashMap<>();
        for (Peleador f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            if (f.getTeam2() != fighter.getTeam2())
            {
                ennemy.put(f.getId(), f);
            }
        }
        int i = 0, i2 = ennemy.size();
        int curHP = 10000;
        Peleador curEnnemy = null;

        while (i < i2)
        {
            curHP = 200000;
            curEnnemy = null;
            for (Map.Entry<Integer, Peleador> t : ennemy.entrySet())
            {
                if (t.getValue().getPdv() < curHP)
                {
                    curHP = t.getValue().getPdv();
                    curEnnemy = t.getValue();
                }
            }
            assert curEnnemy != null;
            list.put(curEnnemy.getId(), curEnnemy);
            ennemy.remove(curEnnemy.getId());
            i++;
        }
        return list;
    }

    public int attackIfPossible(Pelea fight, Peleador fighter, List<SortStats> Spell)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        Map<Integer, Peleador> ennemyList = getLowHpEnnemyList(fight, fighter);
        SortStats SS = null;
        Peleador target = null;
        for (Map.Entry<Integer, Peleador> t : ennemyList.entrySet())
        {
            SS = getBestSpellForTargetDopeul(fight, fighter, t.getValue(), fighter.getCell().getId(), Spell);

            if (SS != null)
            {
                target = t.getValue();
                break;
            }
        }
        int curTarget = 0, cell = 0;
        SortStats SS2 = null;

        for (SortStats S : Spell)
        {
            int targetVal = getBestTargetZone(fight, fighter, S, fighter.getCell().getId(), false);
            if (targetVal == -1 || targetVal == 0)
                continue;
            int nbTarget = targetVal / 1000;
            int cellID = targetVal - nbTarget * 1000;
            if (nbTarget > curTarget)
            {
                curTarget = nbTarget;
                cell = cellID;
                SS2 = S;
            }
        }
        if (curTarget > 0 && cell >= 15 && cell <= 463 && SS2 != null)
        {
            int attack = fight.tryCastSpell(fighter, SS2, cell);
            if (attack != 0)
                return SS2.getSpell().getDuration();
        }
        else
        {
            if (target == null || SS == null)
                return 0;
            int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
            if (attack != 0)
                return SS.getSpell().getDuration();
        }
        return 0;
    }

    public int attackIfPossibleglyph(Pelea fight, Peleador fighter, Peleador f, List<SortStats> Spell)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null || f == null)
            return 0;
        int curTarget = 0, cell = 0;
        SortStats SS2 = null;
        for (SortStats S : Spell)
        {
            if(Camino.casesAreInSameLine(fight.getMap(), fighter.getCell().getId(), f.getCell().getId(), 'z', 70))
            {

                cell = Camino.newCaseAfterPush(fight, fighter.getCell(), f.getCell(), -1, false);
                if(fight.canCastSpell1(fighter, S, fight.getMap().getCase(cell), -1))
                {
                    SS2 = S;
                    curTarget = 100;
                }
            }

            if(S.getSpellID() == 2037)
            {
                cell = Camino.getCaseBetweenEnemy(f.getCell().getId(), fight.getMap(), fight);
                SS2 = S;
                if(fight.canCastSpell1(fighter, SS2, fight.getMap().getCase(cell), -1))
                    curTarget = 100;
            }
        }
        if (curTarget > 0 && cell >= 15 && cell <= 463 && SS2 != null)
        {
            int attack = fight.tryCastSpell(fighter, SS2, cell);
            if (attack != 0)
                return SS2.getSpell().getDuration();
        }
        return 0;
    }

    public int attackIfPossibleCM1(Pelea fight, Peleador fighter, List<SortStats> Spell)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        int curTarget = 0, cell = 0;
        SortStats SS2 = null;
        Map<Integer, Peleador> ennemyList = getLowHpEnnemyList(fight, fighter);
        for (SortStats S : Spell)
        {
            if(S.getSpellID() == 483)
                continue;

            if(S.getSpellID() == 977)
                for (Map.Entry<Integer, Peleador> f : ennemyList.entrySet())
                    if(f.getValue() != null)
                        if(!f.getValue().isInvocation())
                            if(Camino.casesAreInSameLine(fight.getMap(), fighter.getCell().getId(), f.getValue().getCell().getId(), 'z', 12))
                            {

                                cell = fighter.getCell().getId();
                                if(fight.canCastSpell1(fighter, S, fight.getMap().getCase(cell), -1))
                                {
                                    SS2 = S;
                                    curTarget = 100;
                                }
                            }
            if(S.getSpellID() == 484)
                for (Map.Entry<Integer, Peleador> f : ennemyList.entrySet())
                    if(f.getValue() != null)
                        if(!f.getValue().isInvocation())
                            if(Camino.casesAreInSameLine(fight.getMap(), fighter.getCell().getId(), f.getValue().getCell().getId(), 'z', 4))
                            {

                                cell = fighter.getCell().getId();
                                if(fight.canCastSpell1(fighter, S, fight.getMap().getCase(cell), -1))
                                {
                                    SS2 = S;
                                    curTarget = 100;
                                }
                            }
        }
        if (curTarget > 0 && cell >= 15 && cell <= 463 && SS2 != null)
        {
            int attack = fight.tryCastSpell(fighter, SS2, cell);
            if (attack != 0)
                return SS2.getSpell().getDuration();
        }
        return 0;
    }

    public int attackIfPossiblevisee(Pelea fight, Peleador fighter, Peleador target, List<SortStats> Spell)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SortStats SS = null;

        SS = getBestSpellForTargetDopeul(fight, fighter, target, fighter.getCell().getId(), Spell);
        if (target == null || SS == null)
            return 0;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return SS.getSpell().getDuration();
        return 0;
    }

    public int attackAllIfPossible(Pelea fight, Peleador fighter, List<SortStats> Spell)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        Peleador ennemy = getNearestAllnbrcasemax(fight, fighter, 0, 2);
        SortStats SS = null;
        Peleador target = null;
        SS = getBestSpellForTargetDopeul(fight, fighter, ennemy, fighter.getCell().getId(), Spell);

        if (SS != null)
        {
            target = ennemy;
        }
        int curTarget = 0, cell = 0;
        SortStats SS2 = null;

        for (SortStats S : Spell)
        {
            int targetVal = getBestTargetZone(fight, fighter, S, fighter.getCell().getId(), false);
            if (targetVal == -1 || targetVal == 0)
                continue;
            int nbTarget = targetVal / 1000;
            int cellID = targetVal - nbTarget * 1000;
            if (nbTarget > curTarget)
            {
                curTarget = nbTarget;
                cell = cellID;
                SS2 = S;
            }
        }
        if (curTarget > 0 && cell >= 15 && cell <= 463 && SS2 != null)
        {
            int attack = fight.tryCastSpell(fighter, SS2, cell);
            if (attack != 0)
                return SS2.getSpell().getDuration();
        }
        else
        {
            if (target == null || SS == null)
                return 0;
            int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
            if (attack != 0)
                return SS.getSpell().getDuration();
        }
        return 0;
    }

    public int moveToAction(Pelea fight, Peleador current, Peleador target, short action, ArrayList<Integer> noSpell, int index)
    {
        if (fight == null || current == null)
            return 0;
        Map<Integer, Peleador> ennemyList = getLowHpEnnemyList(fight, current);

        if (current.getCurPm(fight) <= 0)
            return 2;

        boolean canAttack = false;
        ArrayList<GameCase> path = new IACamino(fight.getMapOld(), fight, current.getCell().getId(), getNearestEnnemy(fight, current).getCell().getId()).getShortestPath(-1);
        int caseLaunch = -1;
        int newCase = -1;
        int bestNbTarget = 0;
        int _loc1_ = 1;
        int targetVal = 0;
        int nbTarget = 0;
        int cellID = -1;
        GameCase newCell = current.getCell();
        SortStats bestSort = null;
        do
        {
            if (newCell != null)
            {
                for (Map.Entry<Integer, Peleador> t : ennemyList.entrySet())
                {
                    bestSort = getBestSpellForTarget(fight, current, t.getValue(), current.getCell().getId());
                    if (bestSort != null)
                    {
                        target = t.getValue();
                        break;
                    }
                }
                if (target == null)
                    continue;
                for (SortStats SS : current.getMob().getSpells().values())
                {
                    targetVal = getBestTargetZone(fight, current, SS, newCell.getId(), false);
                    if (targetVal != 0)
                    {
                        nbTarget = targetVal / 1000;
                        cellID = targetVal - nbTarget * 1000;
                    }
                    else
                    {
                        cellID = target.getCell().getId();
                        nbTarget = 1;
                    }
                    if (fight.canCastSpell1(current, SS, fight.getMapOld().getCase(cellID), newCell.getId()))
                    {
                        if (nbTarget > bestNbTarget)
                        {
                            //canAttack = true;
                            bestSort = SS;
                            caseLaunch = cellID;
                            bestNbTarget = nbTarget;
                            newCase = newCell.getId();
                        }
                    }
                }
            }
            newCell = path.get(_loc1_ - 1);
        }
        while (_loc1_++ < path.size() && _loc1_ <= current.getCurPm(fight)
                && !canAttack);

        if (caseLaunch != -1)
            canAttack = true;
		else if (newCase == -1 && index == 1)
            return 3;

        boolean result = true;
        assert current.getCell() != null;
        if (newCase != current.getCell().getId())
        {
            StringBuilder pathstr = new StringBuilder();
            try
            {
                int curCaseID = current.getCell().getId();
                int curDir = 0;
                for (GameCase c : path)
                {
                    if (curCaseID == c.getId())
                        continue; // Emp�che le d == 0
                    char d = Camino.getDirBetweenTwoCase(curCaseID, c.getId(), fight.getMap(), true);
                    if (d == 0)
                        return 0;// Ne devrait pas arriver :O
                    if (curDir != d)
                    {
                        if (path.indexOf(c) != 0)
                            pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
                        pathstr.append(d);
                    }
                    curCaseID = c.getId();
                    if (c.getId() == newCase)
                        break;
                }
                if (curCaseID != current.getCell().getId())
                    pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            // Cr�ation d'une GameAction
            AccionJuego GA = new AccionJuego(0, 1, "");
            GA.args = pathstr.toString();
            result = fight.onFighterDeplace(current, GA);
        }
        if (result && canAttack)
        {
            if (fight.canCastSpell1(current, bestSort, fight.getMapOld().getCase(caseLaunch), current.getCell().getId()))
            {
                fight.tryCastSpell(current, bestSort, caseLaunch);
                return 1;
            }
        }
        else if (result && !canAttack)
            return 1;

        return 3;
    }

    public int moveToAttackIfPossible(Pelea fight, Peleador fighter)
    {
        if (fight == null || fighter == null)
            return -1;
        Mapa m = fight.getMap();
        if (m == null)
            return -1;

        GameCase _c = fighter.getCell();
        if (_c == null)
            return -1;

        Peleador ennemy = getNearestEnnemy(fight, fighter);
        if (ennemy == null)
            return -1;

        int distMin = Camino.getDistanceBetween(m, _c.getId(), ennemy.getCell().getId());
        ArrayList<SortStats> sorts = getLaunchableSort(fighter, fight, distMin);
        if (sorts == null)
            return -1;
        ArrayList<Integer> cells = Camino.getListCaseFromFighter(fight, fighter, fighter.getCell().getId(), sorts);
        if (cells == null)
            return -1;
        ArrayList<Peleador> targets = getPotentialTarget(fight, fighter, sorts);
        if (targets == null)
            return -1;
        int CellDest = 0;
        SortStats bestSS = null;
        int[] bestInvok = {1000, 0, 0, 0, -1};
        int[] bestFighter = {1000, 0, 0, 0, -1};
        int targetCell = -1;
        for (int i : cells)
        {
            for (SortStats S : sorts)
            {
                int targetVal = getBestTargetZone(fight, fighter, S, i, false);
                if (targetVal > 0)
                {
                    int nbTarget = targetVal / 1000;
                    int cellID = targetVal - nbTarget * 1000;
                    if (fight.getMapOld().getCase(cellID) != null
                            && nbTarget > 0)
                    {
                        if (fight.canCastSpell1(fighter, S, fight.getMapOld().getCase(cellID), i))
                        {
                            int dist = Camino.getDistanceBetween(fight.getMapOld(), fighter.getCell().getId(), i);
                            if (dist < bestFighter[0]
                                    || bestFighter[2] < nbTarget)
                            {

                                bestFighter[0] = dist;
                                bestFighter[1] = i;
                                bestFighter[2] = nbTarget;
                                bestFighter[4] = cellID;
                                bestSS = S;
                            }
                        }
                    }
                }
                else
                {
                    for (Peleador T : targets)
                    {
                        if (fight.canCastSpell1(fighter, S, T.getCell(), i))
                        {
                            int dist = Camino.getDistanceBetween(fight.getMapOld(), fighter.getCell().getId(), T.getCell().getId());
                            if (!Camino.isCACwithEnnemy(fighter, targets))
                            {
                                if (T.isInvocation())
                                {
                                    if (dist < bestInvok[0])
                                    {
                                        bestInvok[0] = dist;
                                        bestInvok[1] = i;
                                        bestInvok[2] = 1;
                                        bestInvok[3] = 1;
                                        bestInvok[4] = T.getCell().getId();
                                        bestSS = S;
                                    }

                                }
                                else
                                {
                                    if (dist < bestFighter[0])
                                    {
                                        bestFighter[0] = dist;
                                        bestFighter[1] = i;
                                        bestFighter[2] = 1;
                                        bestFighter[3] = 0;
                                        bestFighter[4] = T.getCell().getId();
                                        bestSS = S;
                                    }

                                }
                            }
                            else
                            {
                                if (dist < bestFighter[0])
                                {
                                    bestFighter[0] = dist;
                                    bestFighter[1] = i;
                                    bestFighter[2] = 1;
                                    bestFighter[3] = 0;
                                    bestFighter[4] = T.getCell().getId();
                                    bestSS = S;
                                }
                            }
                        }
                        //}
                    }
                }
            }
        }
        if (bestFighter[1] != 0)
        {
            CellDest = bestFighter[1];
            targetCell = bestFighter[4];
        }
        else if (bestInvok[1] != 0)
        {
            CellDest = bestInvok[1];
            targetCell = bestInvok[4];
        }
        else
            return -1;
        if (CellDest == 0)
            return -1;
        if (CellDest == fighter.getCell().getId())
            return targetCell + bestSS.getSpellID() * 1000;
        ArrayList<GameCase> path = new IACamino(fight.getMapOld(), fight, fighter.getCell().getId(), CellDest).getShortestPath(-1);
        if (path == null)
            return -1;
        StringBuilder pathstr = new StringBuilder();
        try
        {
            int curCaseID = fighter.getCell().getId();
            int curDir = 0;
            path.add(fight.getMapOld().getCase(CellDest));
            for (GameCase c : path)
            {
                if (curCaseID == c.getId())
                    continue; // Emp�che le d == 0
                char d = Camino.getDirBetweenTwoCase(curCaseID, c.getId(), m, true);
                if (d == 0)
                    return -1;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (path.indexOf(c) != 0)
                        pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
                    pathstr.append(d);
                }
                curCaseID = c.getId();
            }
            if (curCaseID != fighter.getCell().getId())
                pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        AccionJuego GA = new AccionJuego(0, 1, "");
        GA.args = pathstr.toString();
        fight.onFighterDeplace(fighter, GA);

        return targetCell + bestSS.getSpellID() * 1000;
    }

    public ArrayList<SortStats> getLaunchableSort(Peleador fighter, Pelea fight, int distMin)
    {
        if (fight == null || fighter == null)
            return null;
        ArrayList<SortStats> sorts = new ArrayList<>();
        if (fighter.getMob() == null)
            return null;
        for (Map.Entry<Integer, SortStats> S : fighter.getMob().getSpells().entrySet())
        {
            if (S.getValue().getSpellID() == 479)
                continue;
            if (S.getValue().getPACost() > fighter.getCurPa(fight))//si PA insuffisant
                continue;
            //if(S.getValue().getMaxPO() + fighter.getCurPM(fight) < distMin && S.getValue().getMaxPO() != 0)// si po max trop petite
            //continue;
            if (!LanzarHechizo.cooldownGood(fighter, S.getValue().getSpellID()))// si cooldown ok
                continue;
            if (S.getValue().getMaxLaunchbyTurn()
                    - LanzarHechizo.getNbLaunch(fighter, S.getValue().getSpellID()) <= 0
                    && S.getValue().getMaxLaunchbyTurn() > 0)// si nb tours ok
                continue;
            if (S.getValue().getSpell().getType() != 0)// si sort pas d'attaque
                continue;
            sorts.add(S.getValue());
        }
        ArrayList<SortStats> finalS = TriInfluenceSorts(fighter, sorts, fight);

        return finalS;
    }

    public ArrayList<SortStats> TriInfluenceSorts(Peleador fighter, ArrayList<SortStats> sorts, Pelea fight)
    {
        if (fight == null || fighter == null)
            return null;
        if (sorts == null)
            return null;

        ArrayList<SortStats> finalSorts = new ArrayList<>();
        Map<Integer, SortStats> copie = new HashMap<>();
        for (SortStats S : sorts)
        {
            copie.put(S.getSpellID(), S);
        }

        int curInfl = 0;
        int curID = 0;

        while (copie.size() > 0)
        {
            curInfl = -1;
            curID = 0;
            for (Map.Entry<Integer, SortStats> S : copie.entrySet())
            {
                int infl = getInfl(fight, S.getValue());
                if (infl > curInfl)
                {
                    curID = S.getValue().getSpellID();
                    curInfl = infl;
                }
            }
            finalSorts.add(copie.get(curID));
            copie.remove(curID);
        }

        return finalSorts;
    }

    public ArrayList<Peleador> getPotentialTarget(Pelea fight, Peleador fighter, ArrayList<SortStats> sorts)
    {
        if (fight == null || fighter == null)
            return null;
        ArrayList<Peleador> targets = new ArrayList<>();
        int distMax = 0;
        for (SortStats S : sorts)
        {
            if (S.getMaxPO() > distMax)
                distMax = S.getMaxPO();
        }
        distMax += fighter.getCurPm(fight) + 3;
        Map<Integer, Peleador> potentialsT = getLowHpEnnemyList(fight, fighter);
        for (Map.Entry<Integer, Peleador> T : potentialsT.entrySet())
        {
            int dist = Camino.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), T.getValue().getCell().getId());
            if (dist < distMax)
            {

                targets.add(T.getValue());
            }
        }
        return targets;
    }

    public int getInfl(Pelea fight, SortStats SS)
    {
        if (fight == null)
            return 0;
        int inf = 0;
        for (EfectoHechizo SE : SS.getEffects())
        {
            switch (SE.getEffectID()) {
                case 96, 97, 98, 99 -> inf += 500 * Formulas.getMiddleJet(SE.getJet());
                default -> inf += Formulas.getMiddleJet(SE.getJet());
            }
        }
        return inf;
    }

    public SortStats getBestSpellForTarget(Pelea fight, Peleador F, Peleador T, int launch)
    {
        if (fight == null || F == null || T == null)
            return null;
        int inflMax = 0;
        SortStats ss = null;
        if (F.isCollector())
        {
            for (Map.Entry<Integer, SortStats> SS : Mundo.mundo.getGuild(F.getCollector().getGuildId()).getSpells().entrySet())
            {
                if (SS.getValue() == null)
                    continue;
                int curInfl = 0, Infl1 = 0, Infl2 = 0;
                int PA = 6;
                int[] usedPA = {0, 0};
                if (!fight.canCastSpell1(F, SS.getValue(), F.getCell(), T.getCell().getId()))
                    continue;
                curInfl = calculInfluence(SS.getValue(), F, T);
                if (curInfl == 0)
                    continue;
                if (curInfl > inflMax)
                {
                    ss = SS.getValue();
                    usedPA[0] = ss.getPACost();
                    Infl1 = curInfl;
                    inflMax = Infl1;
                }



                for (Map.Entry<Integer, SortStats> SS2 : Mundo.mundo.getGuild(F.getCollector().getGuildId()).getSpells().entrySet())
                {
                    if (SS2.getValue() == null)
                        continue;
                    if ((PA - usedPA[0]) < SS2.getValue().getPACost())
                        continue;
                    if (!fight.canCastSpell1(F, SS2.getValue(), F.getCell(), T.getCell().getId()))
                        continue;
                    curInfl = calculInfluence(SS2.getValue(), F, T);
                    if (curInfl == 0)
                        continue;
                    if ((Infl1 + curInfl) > inflMax)
                    {
                        ss = SS.getValue();
                        usedPA[1] = SS2.getValue().getPACost();
                        Infl2 = curInfl;
                        inflMax = Infl1 + Infl2;
                    }
                    for (Map.Entry<Integer, SortStats> SS3 : Mundo.mundo.getGuild(F.getCollector().getGuildId()).getSpells().entrySet())
                    {
                        if (SS3.getValue() == null)
                            continue;
                        if ((PA - usedPA[0] - usedPA[1]) < SS3.getValue().getPACost())
                            continue;
                        if (!fight.canCastSpell1(F, SS3.getValue(), F.getCell(), T.getCell().getId()))
                            continue;
                        curInfl = calculInfluence(SS3.getValue(), F, T);
                        if (curInfl == 0)
                            continue;
                        if ((curInfl + Infl1 + Infl2) > inflMax)
                        {
                            ss = SS.getValue();
                            inflMax = curInfl + Infl1 + Infl2;
                        }
                    }
                }
            }
        }
        else
        {
            for (Map.Entry<Integer, SortStats> SS : F.getMob().getSpells().entrySet())
            {
                if(SS == null)
                    continue;
                if (SS.getValue().getSpell().getType() != 0)
                    continue;
                int curInfl = 0, Infl1 = 0, Infl2 = 0;
                int PA = F.getMob().getPa();
                int[] usedPA = {0, 0};
                if (!fight.canCastSpell1(F, SS.getValue(), T.getCell(), launch))
                    continue;
                curInfl = getInfl(fight, SS.getValue());
                //if(curInfl == 0)continue;
                if (curInfl > inflMax)
                {
                    ss = SS.getValue();
                    usedPA[0] = ss.getPACost();
                    Infl1 = curInfl;
                    inflMax = Infl1;
                }

                for (Map.Entry<Integer, SortStats> SS2 : F.getMob().getSpells().entrySet())
                {
                    if (SS2.getValue().getSpell().getType() != 0)
                        continue;
                    if ((PA - usedPA[0]) < SS2.getValue().getPACost())
                        continue;
                    if (!fight.canCastSpell1(F, SS2.getValue(), T.getCell(), launch))
                        continue;
                    curInfl = getInfl(fight, SS2.getValue());
                    //if(curInfl == 0)continue;
                    if ((Infl1 + curInfl) > inflMax)
                    {
                        ss = SS.getValue();
                        usedPA[1] = SS2.getValue().getPACost();
                        Infl2 = curInfl;
                        inflMax = Infl1 + Infl2;
                    }
                    for (Map.Entry<Integer, SortStats> SS3 : F.getMob().getSpells().entrySet())
                    {
                        if (SS3.getValue().getSpell().getType() != 0)
                            continue;
                        if ((PA - usedPA[0] - usedPA[1]) < SS3.getValue().getPACost())
                            continue;
                        if (!fight.canCastSpell1(F, SS3.getValue(), T.getCell(), launch))
                            continue;

                        curInfl = getInfl(fight, SS3.getValue());
                        //if(curInfl == 0)continue;
                        if ((curInfl + Infl1 + Infl2) > inflMax)
                        {
                            ss = SS.getValue();
                            inflMax = curInfl + Infl1 + Infl2;
                        }
                    }
                }
            }
        }
        return ss;
    }

    public SortStats getBestSpellForTargetDopeul(Pelea fight, Peleador F, Peleador T, int launch, List<SortStats> listspell)
    {
        if (fight == null || F == null)
            return null;
        int inflMax = 0;
        SortStats ss = null;


        for (SortStats SS : listspell)
        {
            if (SS.getSpell().getType() != 0)
                continue;
            int curInfl = 0, Infl1 = 0, Infl2 = 0;
            int PA = F.getMob().getPa();
            int[] usedPA = {0, 0};
            if (!fight.canCastSpell1(F, SS, T.getCell(), launch))
                continue;
            curInfl = getInfl(fight, SS);
            if(curInfl == 0)continue;
            if (curInfl > inflMax)
            {
                ss = SS;
                usedPA[0] = ss.getPACost();
                Infl1 = curInfl;
                inflMax = Infl1;
            }

            for (SortStats SS2 : listspell)
            {
                if (SS2.getSpell().getType() != 0)
                    continue;
                if ((PA - usedPA[0]) < SS2.getPACost())
                    continue;
                if (!fight.canCastSpell1(F, SS2, T.getCell(), launch))
                    continue;
                curInfl = getInfl(fight, SS2);
                if(curInfl == 0)continue;
                if ((Infl1 + curInfl) > inflMax)
                {
                    ss = SS;
                    usedPA[1] = SS2.getPACost();
                    Infl2 = curInfl;
                    inflMax = Infl1 + Infl2;
                }
                for (SortStats SS3 : listspell)
                {
                    if (SS3.getSpell().getType() != 0)
                        continue;
                    if ((PA - usedPA[0] - usedPA[1]) < SS3.getPACost())
                        continue;
                    if (!fight.canCastSpell1(F, SS3, T.getCell(), launch))
                        continue;

                    curInfl = getInfl(fight, SS3);
                    if(curInfl == 0)continue;
                    if ((curInfl + Infl1 + Infl2) > inflMax)
                    {
                        ss = SS;
                        inflMax = curInfl + Infl1 + Infl2;
                    }
                }
            }
        }
        return ss;
    }

    public SortStats getBestSpellForTargetDopeulglyph(Pelea fight, Peleador F, Peleador T, Map<Integer, SortStats> listspell)
    {
        if (fight == null || F == null)
            return null;
        int inflMax = 0;
        SortStats ss = null;
        int launch = Camino.getRandomcelllignepomax(fight.getMap(), F.getCell().getId(), T.getCell().getId(), null, 5);

        for (Map.Entry<Integer, SortStats> SS : listspell.entrySet())
        {
            if (SS.getValue().getSpell().getType() != 0)
                continue;
            int curInfl = 0, Infl1 = 0, Infl2 = 0;
            int PA = F.getMob().getPa();
            int[] usedPA = {0, 0};
            if (!fight.canCastSpell1(F, SS.getValue(), T.getCell(), launch))
                continue;
            curInfl = getInfl(fight, SS.getValue());
            if(curInfl == 0)continue;
            if (curInfl > inflMax)
            {
                ss = SS.getValue();
                usedPA[0] = ss.getPACost();
                Infl1 = curInfl;
                inflMax = Infl1;
            }

            for (Map.Entry<Integer, SortStats> SS2 : listspell.entrySet())
            {
                if (SS2.getValue().getSpell().getType() != 0)
                    continue;
                if ((PA - usedPA[0]) < SS2.getValue().getPACost())
                    continue;
                if (!fight.canCastSpell1(F, SS2.getValue(), T.getCell(), launch))
                    continue;
                curInfl = getInfl(fight, SS2.getValue());
                if(curInfl == 0)continue;
                if ((Infl1 + curInfl) > inflMax)
                {
                    ss = SS.getValue();
                    usedPA[1] = SS2.getValue().getPACost();
                    Infl2 = curInfl;
                    inflMax = Infl1 + Infl2;
                }
                for (Map.Entry<Integer, SortStats> SS3 : listspell.entrySet())
                {
                    if (SS3.getValue().getSpell().getType() != 0)
                        continue;
                    if ((PA - usedPA[0] - usedPA[1]) < SS3.getValue().getPACost())
                        continue;
                    if (!fight.canCastSpell1(F, SS3.getValue(), T.getCell(), launch))
                        continue;

                    curInfl = getInfl(fight, SS3.getValue());
                    if(curInfl == 0)continue;
                    if ((curInfl + Infl1 + Infl2) > inflMax)
                    {
                        ss = SS.getValue();
                        inflMax = curInfl + Infl1 + Infl2;
                    }
                }
            }
        }
        return ss;
    }

    public int getBestTargetZone(Pelea fight, Peleador fighter, SortStats spell, int launchCell, boolean line)
    {
        if (fight == null || fighter == null)
            return 0;
        if (spell.getPorteeType().isEmpty()
                || (spell.getPorteeType().charAt(0) == 'P' && spell.getPorteeType().charAt(1) == 'a')
                || spell.isLineLaunch() && !line)
        {
            return 0;
        }
        ArrayList<GameCase> possibleLaunch = new ArrayList<>();
        int CellF = -1;
        if (spell.getMaxPO() != 0)
        {
            char arg1 = 'C';
            char[] table = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v'};
            char arg2 = 'a';
            if (spell.getMaxPO() > 20)
            {
                arg2 = 'u';
            }
            else
            {
                arg2 = table[spell.getMaxPO()];
            }
            String args = arg1 + Character.toString(arg2);
            possibleLaunch = Camino.getCellListFromAreaString(fight.getMap(), launchCell, launchCell, args, 0, false);
        }
        else
        {
            possibleLaunch.add(fight.getMap().getCase(launchCell));
        }

        if (possibleLaunch == null)
        {
            return -1;
        }
        int nbTarget = 0;
        for (GameCase cell : possibleLaunch)
        {
            try
            {
                if (!fight.canCastSpell1(fighter, spell, cell, launchCell))
                    continue;
                int curTarget = 0;
                ArrayList<GameCase> cells = Camino.getCellListFromAreaString(fight.getMap(), cell.getId(), launchCell, spell.getPorteeType(), 0, false);
                for (GameCase c : cells)
                {
                    if (c == null)
                        continue;
                    if (c.getFirstFighter() == null)
                        continue;
                    if (c.getFirstFighter().getTeam2() != fighter.getTeam2())
                        curTarget++;
                }
                if (curTarget > nbTarget)
                {
                    nbTarget = curTarget;
                    CellF = cell.getId();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        if (nbTarget > 0 && CellF != -1)
            return CellF + nbTarget * 1000;
        else
            return 0;
    }

    public int calculInfluenceHeal(SortStats ss)
    {
        int inf = 0;
        for (EfectoHechizo SE : ss.getEffects())
        {
            if (SE.getEffectID() != 108)
                return 0;
            inf += 100 * Formulas.getMiddleJet(SE.getJet());
        }

        return inf;
    }

    public int calculInfluence(SortStats ss, Peleador C, Peleador T)
    {
        int infTot = 0;
        for (EfectoHechizo SE : ss.getEffects())
        {
            int inf = 0;
            switch (SE.getEffectID())
            {
                case 5:
                    inf = 500 * Formulas.getMiddleJet(SE.getJet());
                    break;
                case 89:
                    inf = 200 * Formulas.getMiddleJet(SE.getJet());
                    break;
                case 91:
                case 95:
                case 94:
                case 93:
                case 92:
                    inf = 150 * Formulas.getMiddleJet(SE.getJet());
                    break;
                case 96:
                case 100:
                case 99:
                case 98:
                case 97:
                    inf = 100 * Formulas.getMiddleJet(SE.getJet());
                    break;
                case 101:
                case 169:
                case 168:
                case 127:
                    inf = 1000 * Formulas.getMiddleJet(SE.getJet());
                    break;
                case 84:
                case 77:
                    inf = 1500 * Formulas.getMiddleJet(SE.getJet());
                    break;
                case 111:
                case 128:
                    inf = -1000 * Formulas.getMiddleJet(SE.getJet());
                    break;
                case 121:
                    inf = -100 * Formulas.getMiddleJet(SE.getJet());
                    break;
                case 131:
                case 219:
                case 218:
                case 217:
                case 216:
                case 215:
                    inf = 300 * Formulas.getMiddleJet(SE.getJet());
                    break;
                case 132:
                    inf = 2000;
                    break;
                case 138:
                    inf = -50 * Formulas.getMiddleJet(SE.getJet());
                    break;
                case 150:
                    inf = -2000;
                    break;
                case 210:
                    inf = -300 * Formulas.getMiddleJet(SE.getJet());
                    break;
                case 211:
                    inf = -300 * Formulas.getMiddleJet(SE.getJet());
                    break;
                case 212:
                    inf = -300 * Formulas.getMiddleJet(SE.getJet());
                    break;
                case 213:
                    inf = -300 * Formulas.getMiddleJet(SE.getJet());
                    break;
                case 214:
                    inf = -300 * Formulas.getMiddleJet(SE.getJet());
                    break;
                case 265:
                    inf = -250 * Formulas.getMiddleJet(SE.getJet());
                case 765://Sacrifice
                    inf = -1000;
                    break;
                case 786://Arbre de vie
                    inf = -1000;
                    break;
                case 106: // Renvoie de sort
                    inf = -900;
                    break;
            }

            if (C.getTeam() == T.getTeam())
                infTot -= inf;
            else
                infTot += inf;
        }
        return infTot;
    }

}
