package org.alexandria.estaticos.pelea.inteligencia.utilidad;

public class Node {

    private int countG = 0, countF = 0, heristic = 0;
    private int cellId;
    private Node parent;

    public Node(int cellId, Node parent) {
        setCellId(cellId);
        setParent(parent);
    }

    public int getCountG() {
        return countG;
    }

    public void setCountG(int countG) {
        this.countG = countG;
    }

    public int getCountF() {
        return countF;
    }

    public void setCountF(int countF) {
        this.countF = countF;
    }

    public int getHeristic() {
        return heristic;
    }

    public void setHeristic(int heristic) {
        this.heristic = heristic;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public void setChild(Node child) {
    }

}
