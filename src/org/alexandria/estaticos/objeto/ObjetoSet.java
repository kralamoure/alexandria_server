package org.alexandria.estaticos.objeto;

import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.cliente.Jugador.Caracteristicas;

import java.util.ArrayList;

public class ObjetoSet {

    private final int id;
    private final ArrayList<Caracteristicas> effects = new ArrayList<>();
    private final ArrayList<ObjetoModelo> itemTemplates = new ArrayList<>();

    public ObjetoSet(int id, String items, String bonuses) {
        this.id = id;

        for (String str : items.split(",")) {
            try {
                ObjetoModelo obj = Mundo.mundo.getObjetoModelo(Integer.parseInt(str.trim()));
                if (obj == null)
                    continue;
                this.itemTemplates.add(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.effects.add(new Caracteristicas());

        for (String str : bonuses.split(";")) {
            Caracteristicas S = new Caracteristicas();
            for (String str2 : str.split(",")) {
                if (!str2.equalsIgnoreCase("")) {
                    try {
                        String[] infos = str2.split(":");
                        int stat = Integer.parseInt(infos[0]);
                        int value = Integer.parseInt(infos[1]);
                        //on ajoute a la stat
                        S.addOneStat(stat, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            this.effects.add(S);
        }
    }

    public int getId() {
        return this.id;
    }

    public Caracteristicas getBonusStatByItemNumb(int numb) {
        if (numb > this.effects.size())
            return new Caracteristicas();
        return effects.get(numb - 1);
    }

    public ArrayList<ObjetoModelo> getItemTemplates() {
        return this.itemTemplates;
    }
}