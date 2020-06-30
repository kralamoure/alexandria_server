package org.alexandria.comunes;

import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.configuracion.Constantes;
import org.alexandria.estaticos.juego.JuegoServidor;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.pelea.trampas.Grifos;
import org.alexandria.estaticos.pelea.trampas.Trampas;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Camino {

    private static Integer nSteps = 0;

    //String a StringBuilder
    public static int isValidPath(Mapa map, int cellID, AtomicReference<String> pathRef, Pelea fight, Jugador perso, int targetCell)
    {
        synchronized(nSteps)
        {
            nSteps=0;
            int newPos=cellID;
            int Steps=0;
            String path=pathRef.get();
            StringBuilder newPath=new StringBuilder();
            for(int i=0;i<path.length();i+=3)
            {
                String SmallPath=path.substring(i,i+3);
                char dir=SmallPath.charAt(0);
                int dirCaseID=Mundo.mundo.getCryptManager().codigoceldaID(SmallPath.substring(1));
                nSteps=0;
                //Si en combat et Si Pas d�but du path, on v�rifie tacle
                if(fight!=null&&i!=0&&getEnemyFighterArround(newPos,map,fight)!=null)
                {
                    pathRef.set(newPath.toString());
                    return Steps;
                }
                //Si en combat, et pas au d�but du path
                if(fight!=null&&i!=0)
                {
                    for(Trampas p : fight.getAllTraps())
                    {
                        int dist=getDistanceBetween(map,p.getCelda().getId(),newPos);
                        if(dist<=p.getSize())
                        {
                            //on arrete le d�placement sur la 1ere case du piege
                            pathRef.set(newPath.toString());
                            return Steps;
                        }
                    }
                }

                String[] aPathInfos=ValidSinglePath(newPos,SmallPath,map,fight,perso,targetCell).split(":");
                if(aPathInfos[0].equalsIgnoreCase("stop"))
                {
                    newPos=Integer.parseInt(aPathInfos[1]);
                    Steps+=nSteps;
                    newPath.append(dir).append(Mundo.mundo.getCryptManager().idceldaCodigo(newPos));
                    pathRef.set(newPath.toString());
                    return -Steps;
                }
                else if(aPathInfos[0].equalsIgnoreCase("ok"))
                {
                    newPos=dirCaseID;
                    Steps+=nSteps;
                }
                else if(aPathInfos[0].equalsIgnoreCase("stoptp"))
                {
                    newPos=Integer.parseInt(aPathInfos[1]);
                    Steps+=nSteps;
                    newPath.append(dir).append(Mundo.mundo.getCryptManager().idceldaCodigo(newPos));
                    pathRef.set(newPath.toString());
                    return -Steps-10000;
                }
                else
                {
                    pathRef.set(newPath.toString());
                    return -1000;
                }
                newPath.append(dir).append(Mundo.mundo.getCryptManager().idceldaCodigo(newPos));
            }
            pathRef.set(newPath.toString());
            return Steps;
        }
    }

    public static boolean getcasebetwenenemie(int cellId, Mapa map, Pelea fight, Peleador F)
    {
        char[] dirs = {'b', 'd', 'f', 'h'};
        for (char dir : dirs)
        {
            GameCase cell = map.getCase(GetCaseIDFromDirrection(cellId, dir, map, false));
            if (cell == null)
                continue;
            Peleador f = cell.getFirstFighter();

            if (f != null && f.getTeam2() != F.getTeam2())
                return true;
        }
        return false;
    }

    public static boolean isCACwithEnnemy(Peleador fighter,
                                          ArrayList<Peleador> Ennemys) {
        for (Peleador f : Ennemys)
            if (isNextTo(fighter.getFight().getMap(), fighter.getCell().getId(), f.getCell().getId()))
                return true;
        return false;
    }

    //Arreglo interrupir por invisibilidad enemiga
    public static ArrayList<Peleador> getEnemyFighterArround(int cellID, Mapa map, Pelea fight)
    {
        char[] dirs= { 'b', 'd', 'f', 'h' };
        ArrayList<Peleador> enemy= new ArrayList<>();

        for(char dir : dirs)
        {
            GameCase cell=map.getCase((short)GetCaseIDFromDirrection(cellID,dir,map,false));
            if(cell!=null)
            {
                Peleador f=cell.getFirstFighter();
                if(f!=null)
                {
                    if(f.getFight()!=fight)
                        continue;
                    if(f.getTeam()!=fight.getFighterByOrdreJeu().getTeam()&&!f.isHide())
                        enemy.add(f);
                }
            }
        }
        if(enemy.size()==0||enemy.size()==4)
            return null;

        return enemy;
    }

    public static boolean isNextTo(Mapa map, int cell1, int cell2) {
        boolean result = false;
        if (cell1 + 14 == cell2)
            result = true;
        else if (cell1 + 15 == cell2)
            result = true;
        else
            result = cell1 - 14 == cell2 || cell1 - 15 == cell2;
        return result;
    }

    public static String ValidSinglePath(int CurrentPos, String Path, Mapa map,
                                         Pelea fight, Jugador perso, int targetCell) {
        nSteps = 0;
        char dir = Path.charAt(0);
        int dirCaseID = Mundo.mundo.getCryptManager().codigoceldaID(Path.substring(1)), check = ("353;339;325;311;297;283;269;255;241;227;213;228;368;354;340;326;312;298;284;270;256;242;243;257;271;285;299;313;327;341;355;369;383".contains(String.valueOf(targetCell)) ? 1 : 0);

        if (fight != null && fight.isOccuped(dirCaseID))
            return "no:";

        if(perso != null) {
            if (perso.getCases)
                if (!perso.thisCases.contains(CurrentPos))
                    perso.thisCases.add(CurrentPos);
        }
        // int oldPos = CurrentPos;
        int lastPos = CurrentPos, oldPos = CurrentPos;

        for (nSteps = 1; nSteps <= 64; nSteps++) {
            if (GetCaseIDFromDirrection(lastPos, dir, map, fight != null) == dirCaseID) {
                if (fight != null && fight.isOccuped(dirCaseID))
                    return "stop:" + lastPos;
                GameCase cell = map.getCase(dirCaseID);
                if(map.getId() == 2019) {
                    if (cell.getId() == 297 && ((cell.getPlayers() != null && cell.getPlayers().size() > 0) || Objects.requireNonNull(perso).getSexe() == 0))
                        return "stop:" + oldPos;
                    if (cell.getId() == 282 && ((cell.getPlayers() != null && cell.getPlayers().size() > 0) || Objects.requireNonNull(perso).getSexe() == 1))
                        return "stop:" + oldPos;
                }
                if (cell.isWalkable(true, fight != null, targetCell)) {
                    return "ok:";
                } else {
                    nSteps--;
                    return ("stop:" + lastPos);
                }
            } else {
                lastPos = GetCaseIDFromDirrection(lastPos, dir, map, fight != null);
            }

            if (fight == null) {
                assert perso != null;
                if (perso.getCurMap().getId() == 9588) {
                    String cell = "353;339;325;311;297;283;269;255;241;227;213;228;368;354;340;326;312;298;284;270;256;242;243;257;271;285;299;313;327;341;355;369;383";
                    if (cell.contains(String.valueOf(lastPos)))
                        check++;
                    if (check > 1)
                        return "stoptp:" + lastPos;
                }
                try {
                    if (perso.getCases)
                        if (!perso.thisCases.contains(lastPos))
                            perso.thisCases.add(lastPos);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (lastPos < 0)
                    continue;
                GameCase _case = map.getCase(lastPos);
                if (_case == null)
                    continue;
                if(map.getId() == 2019) {
                    if(_case.getId() == 297 && ((_case.getPlayers() != null && _case.getPlayers().size() > 0) || perso.getSexe() == 0))
                        return "stop:" + oldPos;
                    if(_case.getId() == 282 && ((_case.getPlayers() != null && _case.getPlayers().size() > 0) || perso.getSexe() == 1))
                        return "stop:" + oldPos;
                }
                if (_case.getOnCellStopAction())
                    return "stop:" + lastPos;
                if (map.isAggroByMob(perso, lastPos))
                    return "stop:" + lastPos;
                if(!map.getCase(lastPos).isWalkable(true, false, targetCell))
                    return "stop:" + oldPos;
                oldPos = lastPos;
            } else {
                if (fight.isOccuped(lastPos))
                    return "no:";
                if (getEnemyFighterArround(lastPos, map, fight) != null)//Si ennemie proche
                    return "stop:" + lastPos;
                for (Trampas p : fight.getAllTraps()) {
                    if (getDistanceBetween(map, p.getCelda().getId(), lastPos) <= p.getSize()) {//on arrete le d�placement sur la 1ere case du piege
                        return "stop:" + lastPos;
                    }
                }
            }
        }
        return "no:";
    }

    public static ArrayList<Integer> getListCaseFromFighter(Pelea fight,
                                                            Peleador fighter, int cellStart, ArrayList<Hechizo.SortStats> SS) {
        int bestPo = 0;
        if (SS != null) {
            for (Hechizo.SortStats sort : SS) {
                if (sort.getMaxPO() > bestPo)
                    bestPo = sort.getMaxPO();
            }
        }
        int pmNumber = fighter.getCurPm(fight);
        /*
		 * if(fighter != fight.getCurFighter()) pmNumber = fighter.getPm();
		 */
        int cellNumber = Formulas.countCell(pmNumber + 1);
        int _loc1_ = 0;
        int _loc3_ = 0;
        char[] dirs = {'b', 'd', 'f', 'h'};
        ArrayList<Integer> cellT = new ArrayList<>();
        ArrayList<Integer> cellY = new ArrayList<>();
        cellT.add(cellStart);
        if (fighter.getCurPm(fight) <= 0)
            return cellT;
        ArrayList<Integer> cell = new ArrayList<>();
        //int distanceMin = bestPo + 4;
        while (_loc1_++ < cellNumber) {
            int _loc2_ = 0;
            if (cellT.size() <= _loc3_ || cellT.isEmpty()) {
                //Fini de tout boucl�
                cell.addAll(cellT);
                cellT.clear();
                cellT.addAll(cellY);
                cellY.clear();
                _loc3_ = 0;
            }

            if (cellT.isEmpty())
                return cell;

            _loc2_ = cellT.get(_loc3_);
            for (char dir : dirs) {
                int _loc4_ = Camino.getCaseIDFromDirrection(_loc2_, dir, fight.getMapOld());
                if(fight.getMap() == null) continue;
                duplicacion(fight, cellT, cellY, cell, _loc4_);
            }
            _loc3_++;
        }
        return cell;
    }

    private static void duplicacion(Pelea fight, ArrayList<Integer> cellT, ArrayList<Integer> cellY, ArrayList<Integer> cell, int _loc4_) {
        if (_loc4_ < 0 || fight.getMap().getCase(_loc4_) == null
                || cell.contains(_loc4_) || cellT.contains(_loc4_)
                || cellY.contains(_loc4_))
            return;
        if (haveFighterOnThisCell(_loc4_, fight)
                || !fight.getMapOld().getCase(_loc4_).isWalkable(true, true, -1))
            return;
        cellY.add(_loc4_);
    }

    public static ArrayList<Integer> getListCaseFromFighter(Pelea fight,
                                                            Peleador fighter, ArrayList<Hechizo.SortStats> SS, Peleador nearest) {
        int bestPo = 0;
        for (Hechizo.SortStats sort : SS) {
            if (sort.getMaxPO() > bestPo)
                bestPo = sort.getMaxPO();
        }
        int cellNumber = Formulas.countCell(fighter.getCurPm(fight) + 1);
        int _loc1_ = 0;
        int _loc3_ = 0;
        char[] dirs = {'b', 'd', 'f', 'h'};
        ArrayList<Integer> cellT = new ArrayList<>();
        ArrayList<Integer> cellY = new ArrayList<>();
        cellT.add(fighter.getCell().getId());
        ArrayList<Integer> cell = new ArrayList<>();
        while (_loc1_++ < cellNumber) {
            int _loc2_ = 0;
            if (cellT.size() <= _loc3_ || cellT.isEmpty()) {
                //Fini de tout boucl�
                cell.addAll(cellT);
                cellT.clear();
                cellT.addAll(cellY);
                cellY.clear();
                _loc3_ = 0;
            }
            if (cellT.isEmpty())
                return cell;
            _loc2_ = cellT.get(_loc3_);
            for (char dir : dirs) {
                int _loc4_ = (short) Camino.getCaseIDFromDirrection(_loc2_, dir, fight.getMapOld());
                duplicacion(fight, cellT, cellY, cell, _loc4_);
            }
            _loc3_++;
        }
        return cell;
    }

    public static ArrayList<Integer> getAllCaseIdAllDirrection(int caseId, Mapa map) {
        ArrayList<Integer> list = new ArrayList<>();
        char[] dir = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        int _c = -1;
        for (char d : dir) {
            _c = GetCaseIDFromDirrection(caseId, d, map, false);
            if (_c > 0)
                list.add(_c);
        }
        return list;
    }

    public static int GetCaseIDFromDirrection(int CaseID, char Direction,
                                              Mapa map, boolean Combat) {
        if (map == null)
            return -1;
        return switch (Direction) {
            case 'a' -> Combat ? -1 : CaseID + 1;
            case 'b' -> CaseID + map.getW();
            case 'c' -> Combat ? -1 : CaseID + (map.getW() * 2 - 1);
            case 'd' -> CaseID + (map.getW() - 1);
            case 'e' -> Combat ? -1 : CaseID - 1;
            case 'f' -> CaseID - map.getW();
            case 'g' -> Combat ? -1 : CaseID - (map.getW() * 2 - 1);
            case 'h' -> CaseID - map.getW() + 1;
            default -> -1;
        };
    }

    public static int getDistanceBetween(Mapa map, int id1, int id2) {
        if (id1 == id2)
            return 0;
        if (map == null)
            return 0;

        int diffX = Math.abs(getCellXCoord(map, id1) - getCellXCoord(map, id2));
        int diffY = Math.abs(getCellYCoord(map, id1) - getCellYCoord(map, id2));
        return (diffX + diffY);
    }

    public static Peleador getEnemyAround(int cellId, Mapa map, Pelea fight) {
        char[] dirs = {'b', 'd', 'f', 'h'};
        for (char dir : dirs) {
            GameCase cell = map.getCase(GetCaseIDFromDirrection(cellId, dir, map, false));
            if (cell == null)
                continue;
            Peleador f = cell.getFirstFighter();

            if (f != null)
                if (f.getFight() == fight)
                    if (f.getTeam() != fight.getFighterByOrdreJeu().getTeam())
                        return f;
        }
        return null;
    }

    public static int newCaseAfterPush(Pelea fight, GameCase CCase, GameCase TCase, int value) {
        // Si c'est les memes case, il n'y a pas a bouger
        boolean onTrap = false;
        if (CCase.getId() == TCase.getId())
            return 0;
        Mapa map = fight.getMap();
        char c = getDirBetweenTwoCase(CCase.getId(), TCase.getId(), map, true);
        int id = TCase.getId();
        if (value < 0) {
            c = getOpositeDirection(c);
            value = -value;
        }
        for (int a = 0; a < value; a++) {
            int nextCase = GetCaseIDFromDirrection(id, c, map, true);

            for (Trampas p : fight.getAllTraps()) {
                int dist = Camino.getDistanceBetween(map, p.getCelda().getId(), nextCase);
                if (dist <= p.getSize())
                    onTrap = true;
            }

            if (map.getCase(nextCase) != null && map.getCase(nextCase).isWalkable(true)
                    && map.getCase(nextCase).getFighters().isEmpty())
                id = nextCase;
            else
                return -(value - a);
            if (onTrap) {
                System.out.println(value - a);
                return id;
            }
        }

        if (id == TCase.getId())
            id = 0;
        return id;
    }

    //Correcion del retroceso a celda central
    public static int newCaseAfterPush(Pelea fight, GameCase currentCell, GameCase targetCell, int value, boolean piege)
    {
        Mapa map=fight.getMap();

        if(currentCell.getId()==targetCell.getId())
            return 0;
        char dir=getDirBetweenTwoCase(currentCell.getId(),targetCell.getId(),map,true);
        int id=targetCell.getId();

        if(value<0)
        {
            dir=getOpositeDirection(dir);
            value=-value;
        }

        if(dir==0x00)
            return 0;
        boolean b=false;
        for(int a=0;a<value;a++)
        {
            int nextCase=GetCaseIDFromDirrection(id,dir,map,true);

            for(Trampas trap : fight.getAllTraps())
            {
                if(getDistanceBetweenTwoCase(map,trap.getCelda(),map.getCase(nextCase))<=trap.getSize())
                {
                    id=nextCase;
                    b=true;
                }
            }

            if(b)
                break;

            if(map.getCase(nextCase)!=null&&map.getCase(nextCase).isWalkable(false)&&map.getCase(nextCase).getFighters().isEmpty())
                id=nextCase;
            else
                return -(value-a);
        }

        if(id==targetCell.getId())
            return 0;
        return id;
    }

    public static int getDistanceBetweenTwoCase(Mapa map, GameCase c1, GameCase c2) {
        int dist = 0;
        if (c1 == null || c2 == null) {
            return dist;
        }
        if (c1.getId() == c2.getId())
            return dist;
        int id = c1.getId();
        char c = getDirBetweenTwoCase(c1.getId(), c2.getId(), map, true);

        while (c2 != map.getCase(id)) {
            id = GetCaseIDFromDirrection(id, c, map, true);
            if (map.getCase(id) == null) {
                return dist;
            }
            dist++;
        }
        return dist;
    }

    public static char getOpositeDirection(char c) {
        return switch (c) {
            case 'a' -> 'e';
            case 'b' -> 'f';
            case 'c' -> 'g';
            case 'd' -> 'h';
            case 'e' -> 'a';
            case 'f' -> 'b';
            case 'g' -> 'c';
            case 'h' -> 'd';
            default -> (char) 0x00;
        };
    }

    public static int getNearenemycontremur(Mapa map, int startCell,
                                            int endCell, ArrayList<GameCase> forbidens)
    {
        //On prend la cellule autour de la cible, la plus proche
        int dist = 1000;
        int cellID = startCell;
        if (forbidens == null)
            forbidens = new ArrayList<>();
        char[] dirs = {'b', 'd', 'f', 'h'};
        GameCase hd = null, bg = null, hg = null, bd = null;
        for (char d : dirs)
        {
            if(d == 'b')//En Haut à Droite.
            {
                int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                hd = map.getCase(c);
            }
            else if(d == 'f')//En Bas à Gauche.
            {
                int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                bg = map.getCase(c);
            }
            else if(d == 'd')//En Haut à Gauche.
            {
                int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                hg = map.getCase(c);
            }
            else {
                int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                bd = map.getCase(c);
            }
        }

        GameCase[] tab = {hd,bg,hg,bd};
        for(GameCase c : tab)
        {
            if(c == null)
                continue;
            if(c == hd)
            {
                if(!c.isWalkable(false) && bg != null || c.getFirstFighter() != null && bg != null)
                {
                    // On cherche la distance entre
                    int dis = Camino.getDistanceBetween(map, endCell, bg.getId());
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && !forbidens.contains(bg))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = bg.getId();
                    }
                }
            }
            else if(c == bg)
            {
                if(!c.isWalkable(false) && hd != null || c.getFirstFighter() != null && hd != null)
                {
                    // On cherche la distance entre
                    int dis = Camino.getDistanceBetween(map, endCell, hd.getId());
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && !forbidens.contains(hd))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = hd.getId();
                    }
                }
            }
            else if(c == bd)
            {
                if(!c.isWalkable(false) && hg != null || c.getFirstFighter() != null && hg != null)
                {
                    // On cherche la distance entre
                    int dis = Camino.getDistanceBetween(map, endCell, hg.getId());
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && !forbidens.contains(hg))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = hg.getId();
                    }
                }
            }
            else if(c == hg)
            {
                if(!c.isWalkable(false) && bd != null || c.getFirstFighter() != null && bd != null)
                {
                    // On cherche la distance entre
                    int dis = Camino.getDistanceBetween(map, endCell, bd.getId());
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && !forbidens.contains(bd))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = bd.getId();
                    }
                }
            }
        }

        //On renvoie -1 si pas trouvé
        return cellID == startCell ? -1 : cellID;
    }

    public static int getCaseBetweenEnemy(int cellId, Mapa map, Pelea fight)
    {
        if(map == null) return 0;
        char[] dirs = {'f', 'd', 'b', 'h'};
        for (char dir : dirs)
        {
            int id = GetCaseIDFromDirrection(cellId, dir, map, false);
            GameCase cell = map.getCase(id);
            if (cell == null)
                continue;
            Peleador f = cell.getFirstFighter();

            if (f == null && cell.isWalkable(false))
                return cell.getId();
        }
        return 0;
    }

    public static int getAvailableCellArround(Pelea fight, int cellId, List<Integer> cellsUnavailable) {
        if(fight == null) return 0;
        char[] dirs = {'f', 'd', 'b', 'h'};

        for (char dir : dirs) {
            int id = GetCaseIDFromDirrection(cellId, dir, fight.getMap(), false);
            GameCase cell = fight.getMap().getCase(id);

            if (cell != null) {
                Peleador fighter = cell.getFirstFighter();
                if (fighter == null && cell.isWalkable(false)) {
                    if(cellsUnavailable != null && cellsUnavailable.contains(cell.getId()))
                        continue;
                    return cell.getId();
                }
            }
        }
        return 0;
    }

    public static int getNearestligneGA(Pelea fight, int startCell,
                                        int endCell, ArrayList<GameCase> forbidens, int distmin)
    {

        Mapa map = fight.getMap();
        //Copie du tableau
        ArrayList<Grifos> glyphs = new ArrayList<>(fight.getAllGlyphs());
        int dist = 1000;
        //On prend la cellule autour de la cible, la plus proche
        int cellID = startCell;
        if (forbidens == null)
            forbidens = new ArrayList<>();
        char[] dirs = {'b', 'd', 'f', 'h'};
        for (char d : dirs)
        {

            int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
            if (map.getCase(c) == null)
                continue;
            int dis = Camino.getDistanceBetween(map, endCell, c);
            int dis2 = Camino.getDistanceBetween(map, startCell, c);
            // Si la distance est strictement inférieur à 1000 et que la case
            // est marchable et que personne ne
            // se trouve dessus et que la case n'est pas interdite
            if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                    && map.getCase(c).getFirstFighter() == null
                    && !forbidens.contains(map.getCase(c)))
            {
                boolean ok1 = true;
                for(Grifos g : glyphs)
                {
                    if(Camino.getDistanceBetween(map,c , g.getCelda().getId()) <= g.getSize() && g.getHechizo() != 476)
                        ok1 = false;
                }

                if(!ok1)
                    continue;
                // On crée la distance
                dist = dis;
                // On modifie la cellule
                cellID = c;
            }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                    && map.getCase(c).getFirstFighter() == null
                    && !forbidens.contains(map.getCase(c)))
            {
                boolean ok1 = true;
                for(Grifos g : glyphs)
                {
                    if(Camino.getDistanceBetween(map,c , g.getCelda().getId()) <= g.getSize() && g.getHechizo() != 476)
                        ok1 = false;
                }

                if(!ok1)
                    continue;
                // On crée la distance
                dist = dis;
                // On modifie la cellule
                cellID = c;
            }
            boolean ok = false;
            while(!ok)
            {
                int h = Camino.GetCaseIDFromDirrection(c, d, map, true);
                if (map.getCase(h) == null)
                    ok = true;
                dis = Camino.getDistanceBetween(map, endCell, c);
                dis2 = Camino.getDistanceBetween(map, startCell, c);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    boolean ok1 = true;
                    for(Grifos g : glyphs)
                    {
                        if(Camino.getDistanceBetween(map,c , g.getCelda().getId()) <= g.getSize() && g.getHechizo() != 476)
                            ok1 = false;
                    }

                    if(!ok1)
                        continue;
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    boolean ok1 = true;
                    for(Grifos g : glyphs)
                    {
                        if(Camino.getDistanceBetween(map,c , g.getCelda().getId()) <= g.getSize() && g.getHechizo() != 476)
                            ok1 = false;
                    }

                    if(!ok1)
                        continue;
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }
                c = h;
            }


        }

        return cellID == startCell ? -1 : cellID;
    }

    public static Peleador getNearestligneenemy(Mapa map, int startCell,
                                                Peleador f, int dist)
    {
        //On prend la cellule autour de la cible, la plus proche
        Peleador E = null;
        char[] dirs = {'b', 'd', 'f', 'h'};
        int endCell = f.getCell().getId();
        for (char d : dirs)
        {
            int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
            if (map.getCase(c) == null)
                continue;
            int dis = Camino.getDistanceBetween(map, endCell, c);
            // Si la distance est strictement inférieur à 1000 et que la case
            // est marchable et que personne ne
            // se trouve dessus et que la case n'est pas interdite
            if (dis < dist && map.getCase(c).getFirstFighter() != null)
            {
                if(map.getCase(c).getFirstFighter().getTeam2() != f.getTeam2())
                    E = map.getCase(c).getFirstFighter();
            }
            boolean ok = false;
            while(!ok)
            {
                int h = Camino.GetCaseIDFromDirrection(c, d, map, true);
                if (map.getCase(h) != null)
                {
                    dis = Camino.getDistanceBetween(map, endCell, h);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && map.getCase(c).getFirstFighter() != null)
                    {
                        if(map.getCase(c).getFirstFighter().getTeam2() != f.getTeam2())
                            E = map.getCase(c).getFirstFighter();
                    }
                }else
                    ok = true;
                c = h;
            }


        }
        //On renvoie null si pas trouvé
        return E;
    }

    public static int getNearenemycontremur2(Mapa map, int startCell, int endCell, ArrayList<GameCase> forbidens, Peleador F) {
        //On prend la cellule autour de la cible, la plus proche
        int dist = 1000;
        int cellID = startCell;
        if (forbidens == null)
            forbidens = new ArrayList<>();
        char[] dirs = {'b', 'd', 'f', 'h'};
        char perso = ' ';
        for (char d : dirs) {
            int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
            if (map.getCase(c) == null)
                continue;
            if(map.getCase(c) == F.getCell())
                perso = d;
        }

        for (char d : dirs) {
            if(getOpositeDirection(perso) == d) {
                int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null) continue;
                if(!map.getCase(c).isWalkable(false) || map.getCase(c).getFirstFighter() != null) {
                    int dis = Camino.getDistanceBetween(map, endCell, map.getCase(c).getId());
                    if (dis < dist && !forbidens.contains(map.getCase(c)) && F.getCell() != map.getCase(c)) {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = map.getCase(c).getId();
                    }
                }
            }
        }

        //On renvoie -1 si pas trouvé
        return cellID == startCell ? -1 : cellID;
    }

    public static int getRandomcelllignepomax(Mapa map, int startCell,
                                              int endCell, ArrayList<GameCase> forbidens, int distmin)
    {
        int dist = 1000;
        //On prend la cellule autour de la cible, la plus proche
        int cellID = startCell;
        if (forbidens == null)
            forbidens = new ArrayList<>();
        char[] dirs = {'b', 'd', 'f', 'h'};
        for (char d : dirs)
        {
            if(d == 'b')//En Haut à Droite.
            {

                int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                int dis = Camino.getDistanceBetween(map, endCell, c);
                int dis2 = Camino.getDistanceBetween(map, startCell, c);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }
                boolean ok = false;
                while(!ok)
                {
                    int h = Camino.GetCaseIDFromDirrection(c, d, map, true);
                    if (map.getCase(h) == null)
                        ok = true;
                    dis = Camino.getDistanceBetween(map, endCell, c);
                    dis2 = Camino.getDistanceBetween(map, startCell, c);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                            && map.getCase(c).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(c)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = c;
                    }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                            && map.getCase(c).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(c)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = c;
                    }
                    c = h;
                }

            }
            else if(d == 'f')//En Bas à Gauche.
            {
                int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                int dis = Camino.getDistanceBetween(map, endCell, c);
                int dis2 = Camino.getDistanceBetween(map, startCell, c);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }
                boolean ok = false;
                while(!ok)
                {
                    int h = Camino.GetCaseIDFromDirrection(c, d, map, true);
                    if (map.getCase(h) == null)
                        ok = true;
                    dis = Camino.getDistanceBetween(map, endCell, c);
                    dis2 = Camino.getDistanceBetween(map, startCell, c);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                            && map.getCase(c).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(c)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = c;
                    }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                            && map.getCase(c).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(c)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = c;
                    }
                    c = h;
                }
            }
            else if(d == 'd')//En Haut à Gauche.
            {
                int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                int dis = Camino.getDistanceBetween(map, endCell, c);
                int dis2 = Camino.getDistanceBetween(map, startCell, c);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }
                boolean ok = false;
                while(!ok)
                {
                    int h = Camino.GetCaseIDFromDirrection(c, d, map, true);
                    if (map.getCase(h) == null)
                        ok = true;
                    dis = Camino.getDistanceBetween(map, endCell, c);
                    dis2 = Camino.getDistanceBetween(map, startCell, c);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                            && map.getCase(c).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(c)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = c;
                    }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                            && map.getCase(c).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(c)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = c;
                    }
                    c = h;
                }
            }
            else {
                int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                int dis = Camino.getDistanceBetween(map, endCell, c);
                int dis2 = Camino.getDistanceBetween(map, startCell, c);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }
                boolean ok = false;
                while(!ok)
                {
                    int h = Camino.GetCaseIDFromDirrection(c, d, map, true);
                    if (map.getCase(h) == null)
                        ok = true;
                    dis = Camino.getDistanceBetween(map, endCell, c);
                    dis2 = Camino.getDistanceBetween(map, startCell, c);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                            && map.getCase(c).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(c)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = c;
                    }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                            && map.getCase(c).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(c)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = c;
                    }
                    c = h;
                }
            }
        }

        return cellID == startCell ? -1 : cellID;
    }

    public static int getNearestCellDiagGA(Mapa map, int startCell,
                                           int endCell, ArrayList<GameCase> forbidens)
    {
        //On prend la cellule autour de la cible, la plus proche
        int dist = 1000;
        int cellID = startCell;
        if (forbidens == null)
            forbidens = new ArrayList<>();
        char[] dirs = {'b', 'd', 'f', 'h'};
        GameCase hd = null;
        GameCase bg = null;
        GameCase hg = null;
        GameCase bd = null;
        for (char d : dirs)
        {
            if(d == 'b')//En Haut à Droite.
            {
                int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                hd = map.getCase(c);
            }
            else if(d == 'f')//En Bas à Gauche.
            {
                int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                bg = map.getCase(c);
            }
            else if(d == 'd')//En Haut à Gauche.
            {
                int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                hg = map.getCase(c);
            }
            else {
                int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                bd = map.getCase(c);
            }
        }

        GameCase[] tab = {hd,bg,hg,bd};
        for(GameCase c : tab)
        {
            if(c == null)
                continue;
            if(c == hd)//En Haut à Droite.
            {
                if(hd.getFirstFighter() == null && hd.blockLoS())
                {
                    int p = Camino.GetCaseIDFromDirrection(c.getId(), 'b', map, true);
                    if (map.getCase(p) == null)
                        continue;
                    // On cherche la distance entre
                    int dis = Camino.getDistanceBetween(map, endCell, p);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && map.getCase(p).isWalkable(true, true, -1)
                            && map.getCase(p).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(p)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = p;
                    }
                }
                int p = Camino.GetCaseIDFromDirrection(c.getId(), 'h', map, true);
                if (map.getCase(p) == null)
                    continue;
                // On cherche la distance entre
                int dis = Camino.getDistanceBetween(map, endCell, p);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && map.getCase(p).isWalkable(true, true, -1)
                        && map.getCase(p).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(p)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = p;
                }

                int m = Camino.GetCaseIDFromDirrection(c.getId(), 'd', map, true);
                if (map.getCase(m) == null)
                    continue;
                // On cherche la distance entre
                dis = Camino.getDistanceBetween(map, endCell, m);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && map.getCase(m).isWalkable(true, true, -1)
                        && map.getCase(m).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(m)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = m;
                }
            }
            else if(c == bg)//En Bas à Gauche.
            {
                if(bg.getFirstFighter() == null && bg.blockLoS())
                {
                    int p = Camino.GetCaseIDFromDirrection(c.getId(), 'f', map, true);
                    if (map.getCase(p) == null)
                        continue;
                    // On cherche la distance entre
                    int dis = Camino.getDistanceBetween(map, endCell, p);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && map.getCase(p).isWalkable(true, true, -1)
                            && map.getCase(p).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(p)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = p;
                    }
                }
                int p = Camino.GetCaseIDFromDirrection(c.getId(), 'h', map, true);
                if (map.getCase(p) == null)
                    continue;
                // On cherche la distance entre
                int dis = Camino.getDistanceBetween(map, endCell, p);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && map.getCase(p).isWalkable(true, true, -1)
                        && map.getCase(p).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(p)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = p;
                }

                int m = Camino.GetCaseIDFromDirrection(c.getId(), 'd', map, true);
                if (map.getCase(m) == null)
                    continue;
                // On cherche la distance entre
                dis = Camino.getDistanceBetween(map, endCell, m);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && map.getCase(m).isWalkable(true, true, -1)
                        && map.getCase(m).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(m)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = m;
                }
            }
            else if(c == hg)//En Haut à Gauche.
            {
                if(hg.getFirstFighter() == null && hg.blockLoS())
                {
                    int p = Camino.GetCaseIDFromDirrection(c.getId(), 'd', map, true);
                    if (map.getCase(p) == null)
                        continue;
                    // On cherche la distance entre
                    int dis = Camino.getDistanceBetween(map, endCell, p);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && map.getCase(p).isWalkable(true, true, -1)
                            && map.getCase(p).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(p)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = p;
                    }
                }
            }
            else if(c == bd)//En Haut à Gauche.
            {
                if(bd.getFirstFighter() == null && bd.blockLoS())
                {
                    int p = Camino.GetCaseIDFromDirrection(c.getId(), 'h', map, true);
                    if (map.getCase(p) == null)
                        continue;
                    // On cherche la distance entre
                    int dis = Camino.getDistanceBetween(map, endCell, p);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && map.getCase(p).isWalkable(true, true, -1)
                            && map.getCase(p).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(p)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = p;
                    }
                }
            }
        }
        return cellID == startCell ? -1 : cellID;
    }

    public static boolean casesAreInSameLine(Mapa map, int c1, int c2, char dir, int max) {
        if (c1 == c2)
            return true;

        if (dir != 'z')//Si la direction est d�finie
        {
            for (int a = 0; a < max; a++) {
                if (GetCaseIDFromDirrection(c1, dir, map, true) == c2)
                    return true;
                if (GetCaseIDFromDirrection(c1, dir, map, true) == -1)
                    break;
                c1 = GetCaseIDFromDirrection(c1, dir, map, true);
            }
        } else
        //Si on doit chercher dans toutes les directions
        {
            char[] dirs = {'b', 'd', 'f', 'h'};
            for (char d : dirs) {
                int c = c1;
                for (int a = 0; a < max; a++) {
                    if (GetCaseIDFromDirrection(c, d, map, true) == c2)
                        return true;
                    c = GetCaseIDFromDirrection(c, d, map, true);
                }
            }
        }
        return false;
    }

    public static ArrayList<Peleador> getCiblesByZoneByWeapon(Pelea fight,
                                                              int type, GameCase cell, int castCellID) {
        ArrayList<Peleador> cibles = new ArrayList<>();
        char c = getDirBetweenTwoCase(castCellID, cell.getId(), fight.getMap(), true);
        if (c == 0) {
            //On cible quand meme le fighter sur la case
            if (cell.getFirstFighter() != null)
                cibles.add(cell.getFirstFighter());
            return cibles;
        }

        //Cases devant celle ou l'on vise
        switch (type) {
            case Constantes.ITEM_TYPE_MARTEAU -> {
                Peleador f = getFighter2CellBefore(castCellID, c, fight.getMap());
                if (f != null)
                    cibles.add(f);
                Peleador g = get1StFighterOnCellFromDirection(fight.getMap(), castCellID, (char) (c - 1));
                if (g != null)
                    cibles.add(g);//Ajoute case a gauche
                Peleador h = get1StFighterOnCellFromDirection(fight.getMap(), castCellID, (char) (c + 1));
                if (h != null)
                    cibles.add(h);//Ajoute case a droite
                Peleador i = cell.getFirstFighter();
                if (i != null)
                    cibles.add(i);
            }
            case Constantes.ITEM_TYPE_BATON -> {
                int dist = Camino.getDistanceBetween(fight.getMap(), cell.getId(), castCellID);
                int newCell = Camino.getCaseIDFromDirrection(castCellID, c, fight.getMap());
                Peleador j = get1StFighterOnCellFromDirection(fight.getMap(), (dist > 1 ? newCell : castCellID), (char) (c - 1));
                if (j != null)
                    cibles.add(j);//Ajoute case a gauche
                Peleador k = get1StFighterOnCellFromDirection(fight.getMap(), (dist > 1 ? newCell : castCellID), (char) (c + 1));
                if (k != null)
                    cibles.add(k);//Ajoute case a droite
                Peleador l = cell.getFirstFighter();
                if (l != null)
                    cibles.add(l);//Ajoute case cible
            }
            case Constantes.ITEM_TYPE_PIOCHE, Constantes.ITEM_TYPE_EPEE, Constantes.ITEM_TYPE_FAUX, Constantes.ITEM_TYPE_DAGUES, Constantes.ITEM_TYPE_BAGUETTE, Constantes.ITEM_TYPE_PELLE, Constantes.ITEM_TYPE_ARC, Constantes.ITEM_TYPE_HACHE, Constantes.ITEM_TYPE_OUTIL -> {
                Peleador m = cell.getFirstFighter();
                if (m != null)
                    cibles.add(m);
            }
        }
        return cibles;
    }

    private static Peleador get1StFighterOnCellFromDirection(Mapa map, int id,
                                                             char c) {
        if (c == (char) ('a' - 1))
            c = 'h';
        if (c == (char) ('h' + 1))
            c = 'a';
        return map.getCase(GetCaseIDFromDirrection(id, c, map, false)).getFirstFighter();
    }

    private static Peleador getFighter2CellBefore(int CellID, char c, Mapa map) {
        int new2CellID = GetCaseIDFromDirrection(GetCaseIDFromDirrection(CellID, c, map, false), c, map, false);
        return map.getCase(new2CellID).getFirstFighter();
    }

    public static char getDirBetweenTwoCase(int cell1ID, int cell2ID, Mapa map,
                                            boolean Combat) {
        ArrayList<Character> dirs = new ArrayList<>();
        dirs.add('b');
        dirs.add('d');
        dirs.add('f');
        dirs.add('h');
        if (!Combat) {
            dirs.add('a');
            dirs.add('b');
            dirs.add('c');
            dirs.add('d');
        }
        for (char c : dirs) {
            int cell = cell1ID;
            for (int i = 0; i <= 64; i++) {
                if (GetCaseIDFromDirrection(cell, c, map, Combat) == cell2ID)
                    return c;
                cell = GetCaseIDFromDirrection(cell, c, map, Combat);
            }
        }
        return 0;
    }

    public static ArrayList<GameCase> getCellListFromAreaString(Mapa map, int cellID, int castCellID, String zoneStr, int PONum, boolean isCC) {
        ArrayList<GameCase> cases = new ArrayList<>();
        int c = PONum;
        if (map.getCase(cellID) == null)
            return cases;
        cases.add(map.getCase(cellID));

        int taille = Mundo.mundo.getCryptManager().getIntByHashedValue(zoneStr.charAt(c + 1));
        switch (zoneStr.charAt(c)) {
            case 'C':// Cercle
                for (int a = 0; a < taille; a++) {
                    char[] dirs = {'b', 'd', 'f', 'h'};
                    // modifications
                    // concurrentes
                    // on �vite les
                    ArrayList<GameCase> cases2 = new ArrayList<>(cases);
                    for (GameCase aCell : cases2) {
                        for (char d : dirs) {
                            GameCase cell = map.getCase(Camino.GetCaseIDFromDirrection(aCell.getId(), d, map, true));
                            if (cell == null)
                                continue;
                            if (!cases.contains(cell))
                                cases.add(cell);
                        }
                    }
                }
                break;

            case 'X':// Croix
                char[] dirs = {'b', 'd', 'f', 'h'};
                for (char d : dirs) {
                    int cID = cellID;
                    for (int a = 0; a < taille; a++) {
                        cases.add(map.getCase(GetCaseIDFromDirrection(cID, d, map, true)));
                        cID = GetCaseIDFromDirrection(cID, d, map, true);
                    }
                }
                break;

            case 'L':// Ligne
                char dir = Camino.getDirBetweenTwoCase(castCellID, cellID, map, true);
                for (int a = 0; a < taille; a++) {
                    cases.add(map.getCase(GetCaseIDFromDirrection(cellID, dir, map, true)));
                    cellID = GetCaseIDFromDirrection(cellID, dir, map, true);
                }
                break;

            case 'P':// Player?

                break;

            default:
                JuegoServidor.a();
                break;
        }
        return cases;
    }

    public static int getCellXCoord(Mapa map, int cellID) {
        if (map == null)
            return 0;
        int w = map.getW();
        return ((cellID - (w - 1) * getCellYCoord(map, cellID)) / w);
    }

    public static int getCellYCoord(Mapa map, int cellID) {
        int w = map.getW();
        int loc5 = cellID / ((w * 2) - 1);
        int loc6 = cellID - loc5 * ((w * 2) - 1);
        int loc7 = loc6 % w;
        return (loc5 - loc7);
    }

    public static boolean checkLoS(Mapa map, int cell1, int cell2) {

        int dist = getDistanceBetween(map, cell1, cell2);
        ArrayList<Integer> los = new ArrayList<>();
        if (dist > 2)
            los = getLoS(cell1, cell2);
        if (los != null && dist > 2) {
            for (int i : los) {
                if (i != cell1 && i != cell2 && !map.getCase(i).blockLoS()) {
                    return true;
                }
            }
        }
        if (dist > 2) {
            int cell = getNearestCellAround(map, cell2, cell1, null);
            return cell != -1 && !map.getCase(cell).blockLoS();
        }
        return false;
    }

    public static int getNearestCellAround(Mapa map, int startCell, int endCell,
                                           ArrayList<GameCase> forbidens) {
        if (map == null)
            return -1;
        // On prend la cellule autour de la cible, la plus proche
        int dist = 1000;
        int cellID = startCell;
        if (forbidens == null)
            forbidens = new ArrayList<>();
        char[] dirs = {'b', 'd', 'f', 'h'};
        for (char d : dirs) {

            // On cherche la celluleID correspondant � la direction associ�
            int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
            if (map.getCase(c) == null)
                continue;
            // On cherche la distance entre
            int dis = Camino.getDistanceBetween(map, endCell, c);
            // Si la distance est strictement inf�rieur � 1000 et que la case
            // est marchable et que personne ne
            // se trouve dessus et que la case n'est pas interdite
            if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                    && map.getCase(c).getFirstFighter() == null
                    && !forbidens.contains(map.getCase(c))) {
                // On cr�e la distance
                dist = dis;
                // On modifie la cellule
                cellID = c;
            }
        }
        // On renvoie -1 si pas trouv�
        return cellID == startCell ? -1 : cellID;
    }

    public static int getNearestCellAroundGA(Mapa map, int startCell,
                                             int endCell, ArrayList<GameCase> forbidens) {
        //On prend la cellule autour de la cible, la plus proche
        int dist = 1000;
        int cellID = startCell;
        if (forbidens == null)
            forbidens = new ArrayList<>();
        char[] dirs = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        for (char d : dirs) {
            int c = Camino.GetCaseIDFromDirrection(startCell, d, map, true);
            int dis = Camino.getDistanceBetween(map, endCell, c);
            if (map.getCase(c) == null)
                continue;
            if (dis < dist && map.getCase(c).isWalkable(true)
                    && map.getCase(c).getFirstFighter() == null
                    && !forbidens.contains(map.getCase(c))) {
                dist = dis;
                cellID = c;
            }
        }

        //On renvoie -1 si pas trouv�
        return cellID == startCell ? -1 : cellID;
    }

    public static ArrayList<GameCase> getShortestPathBetween(Mapa map, int start,
                                                             int dest, int distMax) {
        ArrayList<GameCase> curPath = new ArrayList<>();
        ArrayList<GameCase> curPath2 = new ArrayList<>();
        ArrayList<GameCase> closeCells = new ArrayList<>();
        int limit = 1000;
        //int oldCaseID = start;
        GameCase curCase = map.getCase(start);
        int stepNum = 0;
        boolean stop = false;

        while (stepNum++ <= limit) {
            int nearestCell = getNearestCellAround(map, curCase.getId(), dest, closeCells);
            if (nearestCell == -1) {
                closeCells.add(curCase);
                if (curPath.size() > 0) {
                    curPath.remove(curPath.size() - 1);
                    if (curPath.size() > 0)
                        curCase = curPath.get(curPath.size() - 1);
                    else
                        curCase = map.getCase(start);
                } else {
                    curCase = map.getCase(start);
                }
            } else if (distMax == 0 && nearestCell == dest) {
                curPath.add(map.getCase(dest));
                break;
            } else if (distMax > Camino.getDistanceBetween(map, nearestCell, dest)) {
                curPath.add(map.getCase(dest));
                break;
            } else
            //on continue
            {
                curCase = map.getCase(nearestCell);
                closeCells.add(curCase);
                curPath.add(curCase);
            }
        }

        curCase = map.getCase(start);
        closeCells.clear();
        if (!curPath.isEmpty()) {
            closeCells.add(curPath.get(0));
        }

        while (stepNum++ <= limit) {
            int nearestCell = getNearestCellAround(map, curCase.getId(), dest, closeCells);
            if (nearestCell == -1) {
                closeCells.add(curCase);
                if (curPath2.size() > 0) {
                    curPath2.remove(curPath2.size() - 1);
                    if (curPath2.size() > 0)
                        curCase = curPath2.get(curPath2.size() - 1);
                    else
                        curCase = map.getCase(start);
                } else
                //Si retour a zero
                {
                    curCase = map.getCase(start);
                }
            } else if (distMax == 0 && nearestCell == dest) {
                curPath2.add(map.getCase(dest));
                break;
            } else if (distMax > Camino.getDistanceBetween(map, nearestCell, dest)) {
                curPath2.add(map.getCase(dest));
                break;
            } else
            //on continue
            {
                curCase = map.getCase(nearestCell);
                closeCells.add(curCase);
                curPath2.add(curCase);
            }
        }

        if ((curPath2.size() < curPath.size() && curPath2.size() > 0)
                || curPath.isEmpty())
            curPath = curPath2;
        return curPath;
    }

    public static String getShortestStringPathBetween(Mapa map, int start,
                                                      int dest, int distMax) {
        if (start == dest)
            return null;
        ArrayList<GameCase> path = getShortestPathBetween(map, start, dest, distMax);
        StringBuilder pathstr = new StringBuilder();
        int curCaseID = start;
        char curDir = '\000';
        for (GameCase c : path) {
            char d = getDirBetweenTwoCase(curCaseID, c.getId(), map, true);
            if (d == 0)
                return null;
            if (curDir != d) {
                if (path.indexOf(c) != 0)
                    pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
                pathstr.append(d);
                curDir = d;
            }
            curCaseID = c.getId();
        }
        if (curCaseID != start) {
            pathstr.append(Mundo.mundo.getCryptManager().idceldaCodigo(curCaseID));
        }
        if (pathstr.length() == 0)
            return null;
        return "a" + Mundo.mundo.getCryptManager().idceldaCodigo(start) + pathstr;
    }

    public static boolean isBord1(int id) {
        int[] bords = {1, 30, 59, 88, 117, 146, 175, 204, 233, 262, 291, 320, 349, 378, 407, 436, 465, 15, 44, 73, 102, 131, 160, 189, 218, 247, 276, 305, 334, 363, 392, 421, 450, 479};
        ArrayList<Integer> test = new ArrayList<>();
        for (int i : bords) {
            test.add(i);
        }

        return test.contains(id);
    }

    public static boolean isBord2(int id) {
        int[] bords = {16, 45, 74, 103, 132, 161, 190, 219, 248, 277, 306, 335, 364, 393, 422, 451, 29, 58, 87, 116, 145, 174, 203, 232, 261, 290, 319, 348, 377, 406, 435, 464};
        ArrayList<Integer> test = new ArrayList<>();
        for (int i : bords) {
            test.add(i);
        }

        return test.contains(id);
    }

    public static ArrayList<Integer> getLoS(int cell1, int cell2) {
        ArrayList<Integer> Los = new ArrayList<>();
        int cell = cell1;
        boolean next = false;
        int[] dir1 = {1, -1, 29, -29, 15, 14, -15, -14};

        for (int i : dir1) {
            Los.clear();
            cell = cell1;
            Los.add(cell);
            next = false;
            while (!next) {
                cell += i;
                Los.add(cell);
                if (isBord2(cell) || isBord1(cell) || cell <= 0 || cell >= 480)
                    next = true;
                if (cell == cell2) {
                    return Los;
                }
            }
        }
        return null;
    }

    public static boolean checkLoS(Mapa map, int cell1, int cell2,
                                   Peleador fighter, boolean isPeur) {
        if (fighter != null && fighter.getPlayer() != null) // on ne rev�rifie pas (en plus du client) pour les joueurs
            return true;
        ArrayList<Integer> CellsToConsider = new ArrayList<>();
        CellsToConsider = getLoSBotheringIDCases(map, cell1, cell2);
        if (CellsToConsider == null) {
            return true;
        }
        for (Integer cellID : CellsToConsider) {
            if (map.getCase(cellID) != null)
                if (!map.getCase(cellID).blockLoS()
                        || (!map.getCase(cellID).isWalkable(false) && isPeur)) {
                    return false;
                }
        }
        return true;
    }

    private static ArrayList<Integer> getLoSBotheringIDCases(Mapa map, int cellID1, int cellID2) {
        ArrayList<Integer> toReturn = new ArrayList<>();
        int consideredCell1 = cellID1;
        int consideredCell2 = cellID2;
        char dir = 'b';
        int diffX = 0;
        int diffY = 0;
        int compteur = 0;
        ArrayList<Character> dirs = new ArrayList<>();
        dirs.add('b');
        dirs.add('d');
        dirs.add('f');
        dirs.add('h');

        while (getDistanceBetween(map, consideredCell1, consideredCell2) > 2
                && compteur < 300) {
            diffX = getCellXCoord(map, consideredCell1)
                    - getCellXCoord(map, consideredCell2);
            diffY = getCellYCoord(map, consideredCell1)
                    - getCellYCoord(map, consideredCell2);
            if (Math.abs(diffX) > Math.abs(diffY)) { // si il ya une plus grande diff�rence pour la premi�re coordonn�e
                if (diffX > 0)
                    dir = 'f';
                else
                    dir = 'b';
                consideredCell1 = GetCaseIDFromDirrection(consideredCell1, dir, map, true); // on avance le chemin d'obstacles possibles
                consideredCell2 = GetCaseIDFromDirrection(consideredCell2, getOpositeDirection(dir), map, true); // des deux c�t�s
                toReturn.add(consideredCell1); // la liste des cases potentiellement obstacles
                toReturn.add(consideredCell2); // la liste des cases potentiellement obstacles
            } else if (Math.abs(diffX) < Math.abs(diffY)) { // si il y a une plus grand diff�rence pour la seconde
                if (diffY > 0) // d�termine dans quel sens
                    dir = 'h';
                else
                    dir = 'd';
                consideredCell1 = GetCaseIDFromDirrection(consideredCell1, dir, map, true); // on avance le chemin d'obstacles possibles
                consideredCell2 = GetCaseIDFromDirrection(consideredCell2, getOpositeDirection(dir), map, true); // des deux c�t�s
                toReturn.add(consideredCell1); // la liste des cases potentiellement obstacles
                toReturn.add(consideredCell2); // la liste des cases potentiellement obstacles
            } else {
                if (compteur == 0) // si on est en diagonale parfaite
                    return getLoSBotheringCasesInDiagonal(map, cellID1, cellID2, diffX, diffY);
                if (dir == 'f' || dir == 'b') // on change la direction dans le cas o� on se retrouve en diagonale
                    if (diffY > 0)
                        dir = 'h';
                    else
                        dir = 'd';
                else if (diffX > 0)
                    dir = 'f';
                else
                    dir = 'b';
                consideredCell1 = GetCaseIDFromDirrection(consideredCell1, dir, map, true); // on avance le chemin d'obstacles possibles
                consideredCell2 = GetCaseIDFromDirrection(consideredCell2, getOpositeDirection(dir), map, true); // des deux c�t�s
                toReturn.add(consideredCell1); // la liste des cases potentiellement obstacles
                toReturn.add(consideredCell2); // la liste des cases potentiellement obstacles
            }
            compteur++;
        }
        if (getDistanceBetween(map, consideredCell1, consideredCell2) == 2) {
            dir = 0;
            diffX = getCellXCoord(map, consideredCell1)
                    - getCellXCoord(map, consideredCell2);
            diffY = getCellYCoord(map, consideredCell1)
                    - getCellYCoord(map, consideredCell2);
            if (diffX == 0)
                if (diffY > 0)
                    dir = 'h';
                else
                    dir = 'd';
            if (diffY == 0)
                if (diffX > 0)
                    dir = 'f';
                else
                    dir = 'b';
            if (dir != 0)
                toReturn.add(GetCaseIDFromDirrection(consideredCell1, dir, map, true));
        }
        return toReturn;
    }

    private static ArrayList<Integer> getLoSBotheringCasesInDiagonal(Mapa map,
                                                                     int cellID1, int cellID2, int diffX, int diffY) {
        ArrayList<Integer> toReturn = new ArrayList<>();
        char dir = 'a';
        if (diffX > 0 && diffY > 0)
            dir = 'g';
        if (diffX > 0 && diffY < 0)
            dir = 'e';
        if (diffX < 0 && diffY > 0)
            dir = 'a';
        if (diffX < 0 && diffY < 0)
            dir = 'c';
        int consideredCell = cellID1, compteur = 0;
        while (consideredCell != -1 && compteur < 100) {
            consideredCell = GetCaseIDFromDirrection(consideredCell, dir, map, true);
            if (consideredCell == cellID2)
                return toReturn;
            toReturn.add(consideredCell);
            compteur++;
        }
        return toReturn;
    }

    public static ArrayList<Peleador> getFightersAround(int cellID, Mapa map, Pelea fight) {
        char[] dirs = {'b', 'd', 'f', 'h'};
        ArrayList<Peleador> fighters = new ArrayList<>();

        for (char dir : dirs) {
            GameCase gameCase = map.getCase(GetCaseIDFromDirrection(cellID, dir, map, false));
            if(gameCase == null) continue;
            Peleador f = gameCase.getFirstFighter();
            if (f != null)
                fighters.add(f);
        }
        return fighters;
    }

    public static char getDirEntreDosCeldas(Mapa map, int id1, int id2) {
        if (id1 == id2)
            return 0;
        if (map == null)
            return 0;
        int difX = (getCellXCoord(map, id1) - getCellXCoord(map, id2));
        int difY = (getCellYCoord(map, id1) - getCellYCoord(map, id2));
        int difXabs = Math.abs(difX);
        int difYabs = Math.abs(difY);
        if (difXabs > difYabs) {
            if (difX > 0)
                return 'f';
            else
                return 'b';
        } else {
            if (difY > 0)
                return 'h';
            else
                return 'd';
        }
    }

    public static int getCellArroundByDir(int cellId, char dir,
                                          Mapa map) {
        if (map == null)
            return -1;

        return switch (dir) {
//En Haut � Droite.
            case 'b' -> cellId + map.getW();
//En Haut � Gauche.
            case 'd' -> cellId + (map.getW() - 1);
//En Bas � Gauche.
            case 'f' -> cellId - map.getW();
//En Bas � Droite.
            case 'h' -> cellId - map.getW() + 1;
            default -> -1;
        };
    }

    public static GameCase checkIfCanPushEntity(Pelea fight, int startCell,
                                                int endCell, char direction) {
        Mapa map = fight.getMap();
        GameCase cell = map.getCase(getCellArroundByDir(startCell, direction, map));
        GameCase oldCell = cell;
        GameCase actualCell = cell;

        while (actualCell.getId() != endCell) {
            actualCell = map.getCase(getCellArroundByDir(actualCell.getId(), direction, map));
            if (!actualCell.getFighters().isEmpty()
                    || !actualCell.isWalkable(true))
                return oldCell;

            for (Trampas trap : fight.getAllTraps()) {

                if (Camino.getDistanceBetween(fight.getMap(), trap.getCelda().getId(), actualCell.getId()) <= trap.getSize())
                    return actualCell;
            }

            oldCell = actualCell;
        }

        return null;
    }

    public static boolean haveFighterOnThisCell(int cell, Pelea fight) {
        for (Peleador f : fight.getFighters(3)) {
            if (f.getCell().getId() == cell && !f.isDead())
                return true;
        }
        return false;
    }

    public static int getCaseIDFromDirrection(int CaseID, char Direccion,
                                              Mapa map) {
        // mag.get_w() = te da el ancho del mapa
        return switch (Direccion) {
// diagonal derecha abajo
            case 'b' -> CaseID + map.getW();
// diagonal izquierda abajo
            case 'd' -> CaseID + (map.getW() - 1);
// diagonal izquierda arriba
            case 'f' -> CaseID - map.getW();
// diagonal derecha arriba
            case 'h' -> CaseID - map.getW() + 1;
            default -> -1;
        };
    }

    public static boolean cellArroundCaseIDisOccuped(Pelea fight, int cell) {
        char[] dirs = {'b', 'd', 'f', 'h'};
        ArrayList<Integer> Cases = new ArrayList<>();

        for (char dir : dirs) {
            int caseID = Camino.GetCaseIDFromDirrection(cell, dir, fight.getMap(), true);
            Cases.add(caseID);
        }
        int ha = 0;
        for (Integer aCase : Cases) {
            if (fight.getMap().getCase(aCase).getFirstFighter() != null)
                ha++;
        }
        return ha != 4;

    }
}

