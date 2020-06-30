package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA48 extends NecesitaHechizo {

    public IA48(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100;
            boolean action = false;
            Peleador E = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);
            Peleador C = Funcion.getInstance().getNearestEnnemymurnbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;

            if(C != null && C.isHide()) C = null;

            if(this.fighter.getCurPm(this.fight) > 0 && C == null) {
                int value = Funcion.getInstance().moveIfPossiblecontremur(this.fight, this.fighter, E);
                if(value != 0) {
                    time = value;
                    action = true;
                    C = Funcion.getInstance().getNearestEnnemymurnbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Funcion.getInstance().invocIfPossible(this.fight, this.fighter, this.invocations)) {
                    time = 2000;
                    action = true;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && C != null && !action) {
                int value = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.cacs);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            }
            if(this.fighter.getCurPm(this.fight) > 0 && !action) {
                int value = Funcion.getInstance().moveIfPossiblecontremur(this.fight, this.fighter, E);
                if(value != 0) time = value;
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}