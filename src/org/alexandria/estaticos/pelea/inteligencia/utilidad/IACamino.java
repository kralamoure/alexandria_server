package org.alexandria.estaticos.pelea.inteligencia.utilidad;

import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.comunes.Camino;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class IACamino {

    private final java.util.Map<Integer, Node> openList = new HashMap<>();
    private final java.util.Map<Integer, Node> closeList = new LinkedHashMap<>();
    private Mapa map;
    private Pelea fight;
    private int cellStart;
    private int cellEnd;

    public IACamino(Mapa map, Pelea fight, int cellStart, int cellEnd) {
        setMap(map);
        setFight(fight);
        setCellStart(cellStart);
        setCellEnd(cellEnd);
    }

    public ArrayList<GameCase> getShortestPath(int value) {
        Node nodeStart = new Node(getCellStart(), null);
        openList.put(getCellStart(), nodeStart);
        while (!openList.isEmpty() && (!closeList.containsKey(getCellEnd()))) {
            char[] dirs = {'b', 'd', 'f', 'h'};
            Node nodeCurrent = bestNode();
            if (nodeCurrent.cellId == getCellEnd()
                    && !Camino.cellArroundCaseIDisOccuped(getFight(), nodeCurrent.cellId))
                return getPath();
            addListClose(nodeCurrent);
            for (int loc0 = 0; loc0 < 4; loc0++) {
                int cell = Camino.getCaseIDFromDirrection(nodeCurrent.cellId, dirs[loc0], getMap());
                Node node = new Node(cell, nodeCurrent);
                if (getMap().getCase(cell) == null)
                    continue;
                if (!getMap().getCase(cell).isWalkable(true, true, -1)
                        && cell != getCellEnd())
                    continue;
                if (Camino.haveFighterOnThisCell(cell, getFight())
                        && cell != getCellEnd())
                    continue;
                if (closeList.containsKey(cell))
                    continue;
                if (openList.containsKey(cell)) {
                    if (openList.get(cell).countG > getCostG(node)) {
                        nodeCurrent.setChild(openList.get(cell));
                        openList.get(cell).parent = nodeCurrent;
                        openList.get(cell).countG = getCostG(node);
                        openList.get(cell).heristic = Camino.getDistanceBetween(getMap(), cell, getCellEnd()) * 10;
                        openList.get(cell).countF = openList.get(cell).countG
                                + openList.get(cell).heristic;
                    }
                } else {
                    if (value == 0)
                        if (Camino.casesAreInSameLine(getMap(), cell, getCellEnd(), dirs[loc0], 70))
                            node.countF = (node.countG + node.heristic) - 10;
                    openList.put(cell, node);
                    nodeCurrent.setChild(node);
                    node.parent = nodeCurrent;
                    node.countG = getCostG(node);
                    node.heristic = Camino.getDistanceBetween(getMap(), cell, getCellEnd()) * 10;
                    node.countF = node.countG + node.heristic;
                }
            }
        }
        return getPath();
    }

    private ArrayList<GameCase> getPath() {
        Node current = getLastNode(closeList);
        if (current == null)
            return null;
        ArrayList<GameCase> path = new ArrayList<>();
        java.util.Map<Integer, GameCase> path0 = new HashMap<>();
        for (int index = closeList.size(); current.cellId != getCellStart(); index--) {
            if (current.cellId == getCellStart())
                continue;
            path0.put(index, getMap().getCase(current.cellId));
            current = current.parent;

        }
        int index = -1;
        while (path.size() != path0.size()) {
            index++;
            if (path0.get(index) == null)
                continue;
            path.add(path0.get(index));
        }
        return path;
    }

    private Node getLastNode(java.util.Map<Integer, Node> list) {
        Node node = null;
        for (Entry<Integer, Node> entry : list.entrySet()) {
            node = entry.getValue();
        }
        return node;
    }

    private Node bestNode() {
        int bestCountF = 150000;
        Node bestNode = null;
        for (Node node : openList.values()) {
            if (node.countF < bestCountF) {
                bestCountF = node.countF;
                bestNode = node;
            }
        }
        return bestNode;
    }

    private void addListClose(Node node) {
        openList.remove(node.cellId);
        if (!closeList.containsKey(node.cellId))
            closeList.put(node.cellId, node);
    }

    private int getCostG(Node node) {
        int costG = 0;
        while (node.cellId == getCellStart()) {
            node = node.parent;
            costG += 10;
        }
        return costG;
    }

    public Mapa getMap() {
        return map;
    }

    public void setMap(Mapa map) {
        this.map = map;
    }

    public Pelea getFight() {
        return fight;
    }

    public void setFight(Pelea fight) {
        this.fight = fight;
    }

    public int getCellStart() {
        return cellStart;
    }

    public void setCellStart(int cellStart) {
        this.cellStart = cellStart;
    }

    public int getCellEnd() {
        return cellEnd;
    }

    public void setCellEnd(int cellEnd) {
        this.cellEnd = cellEnd;
    }

}
