package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA51 extends NecesitaHechizo {

    public IA51(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100;
            boolean action = false;

            Peleador ennemy = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);
            Peleador C = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
            Peleador A = Funcion.getInstance().getNearestAminbrcasemax(this.fight, this.fighter, 0, 5);//2 = po min 1 + 1;
            Peleador A1 = Funcion.getInstance().getNearestAminbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;

            if(C != null && C.isHide()) C = null;
            if(this.fighter.getCurPm(this.fight) > 0 && C == null && A == null) {
                int value = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, A);
                if(value != 0) {
                    time = value;
                    action = true;
                    C = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                    A = Funcion.getInstance().getNearestAminbrcasemax(this.fight, this.fighter, 0, 5);//2 = po min 1 + 1;
                    A1 = Funcion.getInstance().getNearestAminbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                }
            } else if(this.fighter.getCurPm(this.fight) > 0 && C == null) {
                int value = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, ennemy);
                if(value != 0) {
                    time = value;
                    action = true;
                    C = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                    A = Funcion.getInstance().getNearestAminbrcasemax(this.fight, this.fighter, 0, 5);//2 = po min 1 + 1;
                    A1 = Funcion.getInstance().getNearestAminbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && !action && A1 != null) {
                if (Funcion.getInstance().buffIfPossible(this.fight, this.fighter, A1, this.buffs)) {
                    time = 1000;
                    action = true;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && C != null && !action) {
                int value = Funcion.getInstance().attackIfPossibleBuveur(this.fight, this.fighter, C);
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