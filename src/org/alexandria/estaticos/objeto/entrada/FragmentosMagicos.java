package org.alexandria.estaticos.objeto.entrada;

import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.otro.utilidad.Doble;

import java.util.ArrayList;

public class FragmentosMagicos extends ObjetoJuego {

    private final ArrayList<Doble<Integer, Integer>> runes;

    public FragmentosMagicos(int Guid, String runes) {
        super(Guid);
        this.runes = new ArrayList<>();

        if (!runes.isEmpty()) {
            for (String rune : runes.split(";")) {
                String[] split = rune.split(":");
                this.runes.add(new Doble<>(Integer.parseInt(split[0]), Integer.parseInt(split[1])));
            }
        }
    }

    public ArrayList<Doble<Integer, Integer>> getRunes() {
        return runes;
    }

    public void addRune(int id) {
        Doble<Integer, Integer> rune = this.search(id);

        if (rune == null)
            this.runes.add(new Doble<>(id, 1));
        else
            rune.segundo += 1;
    }

    public Doble<Integer, Integer> search(int id) {
        for (Doble<Integer, Integer> couple : this.runes)
            if (couple.getPrimero() == id)
                return couple;
        return null;
    }
}