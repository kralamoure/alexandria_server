package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA33 extends NecesitaHechizo {

    public IA33(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            boolean action = false;
            int time = 100, maxPo = 1, maxPoBuff = 1;

            for(Hechizo.SortStats S : this.highests)
                if(S.getMaxPO() > maxPo)
                    maxPo = S.getMaxPO();
            for(Hechizo.SortStats S : this.buffs)
                if(S.getMaxPO() > maxPo)
                    maxPoBuff = S.getMaxPO();

            Peleador nearestEnnemy = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);
            Peleador ennemy1 = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
            Peleador ennemy2 = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
            Peleador ennemy3 = Funcion.getInstance().getNearestAminbrcasemax(this.fight, this.fighter, 0, maxPoBuff + 1);//2 = po min 1 + 1;

            if(maxPo == 1) ennemy1 = null;
            if(ennemy2 != null) if(ennemy2.isHide()) ennemy2 = null;
            if(ennemy1 != null) if(ennemy1.isHide()) ennemy1 = null;

            if(this.fighter.getCurPm(this.fight) > 0 && ennemy1 == null && ennemy2 == null) {
                int value = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, nearestEnnemy);
                if(value != 0) {
                    time = value;
                    action = true;
                    ennemy1 = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
                    ennemy2 = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                    ennemy3 = Funcion.getInstance().getNearestAminbrcasemax(this.fight, this.fighter, 0, maxPoBuff+1);//2 = po min 1 + 1;
                    if(maxPo == 1) ennemy1 = null;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Funcion.getInstance().invocIfPossible(this.fight, this.fighter, this.invocations)) {
                    time = 2000;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && !action && ennemy3 != null) {
                if (Funcion.getInstance().buffIfPossible(this.fight, this.fighter, ennemy3, this.buffs)) {
                    time = 2000;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && ennemy1 != null && ennemy2 == null && !action) {
                int value = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            } else if(this.fighter.getCurPa(this.fight) > 0 && ennemy2 != null && !action) {
                int value = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.cacs);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && ennemy2 != null && !action) {
                int value = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && !action) {
                int value = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, nearestEnnemy);
                if(value != 0) time = value;
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}