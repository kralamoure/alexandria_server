package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA49 extends NecesitaHechizo {

    private byte attack = 0;
    private boolean boost = false;

    public IA49(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100, maxPo = 1;
            boolean action = false;

            Peleador E = Funcion.getInstance().getNearestAllnbrcasemax(this.fight, this.fighter, 0, 100);
            if(this.attack >= 1) E = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);

            for(Hechizo.SortStats spellStats : this.highests)
                if(spellStats.getMaxPO() > maxPo)
                    maxPo = spellStats.getMaxPO();

            Peleador L = Funcion.getInstance().getNearestAllnbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
            Peleador C = Funcion.getInstance().getNearestAllnbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;

            if(this.attack >= 1) {
                L = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
                C = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
            }

            if(C != null) if(C.isHide()) C = null;
            if(L != null) if(L.isHide()) L = null;
            if(maxPo == 1) L = null;

            if(this.fighter.getCurPm(this.fight) > 0 && L == null && C == null) {
                int value = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, E);
                if(value != 0) {
                    time = value;
                    action = true;
                    L = Funcion.getInstance().getNearestAllnbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
                    C = Funcion.getInstance().getNearestAllnbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                    if(this.attack >= 1) {
                        L = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
                        C = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                    }
                    if(maxPo == 1) L = null;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && !action && !this.boost) {
                if (Funcion.getInstance().buffIfPossible(this.fight, this.fighter, this.fighter, this.buffs)) {
                    time = 1000;
                    action = true;
                    boost = true;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && L != null && C == null && !action) {
                int value = Funcion.getInstance().attackAllIfPossible(this.fight, this.fighter, this.highests);
                if(value != 0) {
                    time = value;
                    action = true;
                    this.attack++;
                }
            } else if(this.fighter.getCurPa(this.fight) > 0 && C != null && !action) {
                int value = Funcion.getInstance().attackAllIfPossible(this.fight, this.fighter, this.cacs);
                if(value != 0) {
                    time = value;
                    action = true;
                    this.attack++;
                }
            }
            if(this.fighter.getCurPm(this.fight) > 0 && !action) {
                int value = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, E);
                if(value != 0) time = value;
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}