package org.alexandria.estaticos.oficio.magueo;

import org.alexandria.configuracion.Constantes;
import org.alexandria.estaticos.juego.mundo.Mundo;

import java.util.ArrayList;
import java.util.List;

public class Runas {

    public final static List<Runas> runes = new ArrayList<>();

    public static Runas getRuneById(int id) {
        for(Runas rune : runes)
            if(rune.getId() == id)
                return rune;
        return null;
    }

    public static Runas getRuneByCharacteristic(short stat) {
        for(Runas rune : runes)
            if(rune.getCharacteristic() == stat)
                return rune;
        return null;
    }

    public static Runas getRuneByCharacteristicAndByWeight(short stat) {
        Runas valid = null;
        float weight = 999;
        for(Runas rune : runes) {
            if (rune.getCharacteristic() == stat && weight > rune.getWeight()) {
                weight = rune.getWeight();
                valid = rune;
            }
        }
        return valid;
    }

    private final short id;
    private short characteristic;
    private final float weight;
    private final byte bonus;

    public Runas(short id, float weight, byte bonus) {
        this.id = id;
        this.weight = weight;
        this.bonus = bonus;
        this.setCharacteristic();
        Runas.runes.add(this);
    }

    public short getId() {
        return id;
    }

    private void setCharacteristic() {
        this.characteristic = Short.parseShort(Mundo.mundo.getObjetoModelo(this.id).getStrTemplate().split("#")[0], 16);
        if(this.characteristic == 112)
            this.characteristic = Constantes.STATS_ADD_DOMA;
    }

    public short getCharacteristic() {
        return characteristic;
    }

    public float getWeight() {
        return weight;
    }

    public byte getBonus() {
        return bonus;
    }

    public byte[] getChance() {
        return this.weight <= 1 ? new byte[] {66, 34, 0} : new byte[] {43, 50, 7};
    }
}