package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA44 extends NecesitaHechizo {

    public IA44(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100;
            boolean action = false;
            Peleador E = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);
            Peleador C = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
            Peleador A = Funcion.getInstance().getNearestAminoinvocnbrcasemax(this.fight, this.fighter, 0, 5);//2 = po min 1 + 1;
            Peleador A1 = Funcion.getInstance().getNearestAminoinvocnbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;

            if(A != null && (A.getPdv() * 100) / A.getPdvMax() > 90) A = null;
            if(A1 != null && (A1.getPdv() * 100) / A1.getPdvMax() > 90) A1 = null;
            if(C != null && C.isHide()) C = null;

            if(this.fighter.getCurPm(this.fight) > 0 && A != null && A1 == null) {
                int value = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, A);
                if(value != 0) {
                    time = value;
                    action = true;
                    C = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                    A1 = Funcion.getInstance().getNearestAminoinvocnbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                }
            } else if(this.fighter.getCurPm(this.fight) > 0 && C == null) {
                int value = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, E);
                if(value != 0) {
                    time = value;
                    action = true;
                    C = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && C != null && !action) {
                int value = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.cacs);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            } else if(this.fighter.getCurPa(this.fight) > 0 && A1 != null && !action) {
                int value = Funcion.getInstance().attackIfPossiblevisee(this.fight, this.fighter, A1, this.cacs);
                if(value != 0) {
                    time = value;
                    action = true;
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