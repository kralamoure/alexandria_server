package org.alexandria.estaticos.area;

import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.juego.mundo.Mundo;

import java.util.ArrayList;

public class SubArea {
    public static int bontarians = 0, brakmarians = 0;

    private final int id;
    private final Area area;
    private int alignement, prismId;
    private boolean conquistable;
    private final ArrayList<Mapa> maps = new ArrayList<>();

    public SubArea(int id, int area) {
        this.id = id;
        this.area = Mundo.mundo.getArea(area);
    }

    public int getId() {
        return id;
    }

    public Area getArea() {
        return area;
    }

    public int getAlignement() {
        return alignement;
    }

    public void setAlignement(int alignement) {
        if (this.alignement == 1 && alignement == -1)
            bontarians--;
        else if (this.alignement == 2 && alignement == -1)
            brakmarians--;
        else if (this.alignement == -1 && alignement == 1)
            bontarians++;
        else if (this.alignement == -1 && alignement == 2)
            brakmarians++;
        this.alignement = alignement;
    }

    public int getPrismId() {
        return prismId;
    }

    public void setPrismId(int prism) {
        this.prismId = prism;
    }

    public boolean getConquistable() {
        return conquistable;
    }

    public void setConquistable(int conquistable) {
        this.conquistable = conquistable == 0;
    }

    public ArrayList<Mapa> getMaps() {
        return maps;
    }

    public void addMap(Mapa Map) {
        this.maps.add(Map);
    }
}