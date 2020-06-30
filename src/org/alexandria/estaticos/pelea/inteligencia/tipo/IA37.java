package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA37 extends NecesitaHechizo {

    public IA37(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100, maxPo = 1;
            boolean action = false;
            Peleador ennemy = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);

            for(Hechizo.SortStats spellStats : this.highests)
                if(spellStats.getMaxPO() > maxPo)
                    maxPo = spellStats.getMaxPO();

            Peleador L = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
            Peleador C = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;

            if(maxPo == 1) L = null;
            if(C != null) if(C.isHide()) C = null;
            if(L != null) if(L.isHide()) L = null;

            if(this.fighter.getCurPm(this.fight) > 0 && L == null && C == null) {
                int value = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, ennemy);
                if(value != 0) {
                    time = value;
                    action = true;
                    L = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
                    C = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                    if(maxPo == 1) L = null;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Funcion.getInstance().invocIfPossible(this.fight, this.fighter, this.invocations)) {
                    time = 2000;
                    action = true;
                }
            }

            int percentPdv = (this.fighter.getPdv() * 100) / this.fighter.getPdvMax();

            if(this.fighter.getCurPa(this.fight) > 0 && !action && percentPdv < 50 && percentPdv < 95) {
                if (Funcion.getInstance().HealIfPossible(this.fight, this.fighter, true, 50) != 0) {
                    time = 400;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Funcion.getInstance().HealIfPossible(this.fight, this.fighter, false, 80) != 0) {
                    time = 400;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && L != null && C == null && !action) {
                int value = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            } else if(this.fighter.getCurPa(this.fight) > 0 && C != null && !action) {
                int value = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.cacs);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && !action) {
                int value = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, ennemy);
                if(value != 0) time = value;
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}