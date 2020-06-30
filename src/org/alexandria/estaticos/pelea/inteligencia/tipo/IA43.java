package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.comunes.Camino;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA43 extends NecesitaHechizo {

    public IA43(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100;
            boolean action = false;

            Peleador E = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, 50);// pomax +1;
            Peleador L = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, 5);// pomax +1;

            if(Camino.getcasebetwenenemie(this.fighter.getCell().getId(), this.fight.getMap(), this.fight, this.fighter)) {
                action = true;
                this.stop = true;
            }
            if(L != null && L.isHide())
                L = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, 10);// pomax +1;
            if(this.fighter.getCurPm(this.fight) > 0 && L != null && !action) {
                int value = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, L);
                if(value != 0) {
                    time = value;
                    Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, 5);// pomax +1;
                }
            } else if(this.fighter.getCurPm(this.fight) > 0 && L == null && !action) {
                int value = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, E);
                if(value != 0) {
                    time = value;
                    Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, 5);// pomax +1;
                }
            }

            if(this.fighter.getCurPm(this.fight) == 0) this.stop = true;

            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}