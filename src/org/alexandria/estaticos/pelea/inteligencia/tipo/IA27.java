package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo.SortStats;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA27 extends NecesitaHechizo {

    public IA27(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100, maxPo = 1;
            boolean action = false;
            Peleador E = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);

            for(SortStats spellStats : this.highests)
                if(spellStats != null && spellStats.getMaxPO() > maxPo)
                    maxPo = spellStats.getMaxPO();

            Peleador firstEnnemy = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1);
            Peleador secondEnnemy = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);

            if(maxPo == 1) firstEnnemy = null;
            if(secondEnnemy != null) if(secondEnnemy.isHide()) secondEnnemy = null;
            if(firstEnnemy != null) if(firstEnnemy.isHide()) firstEnnemy = null;

            if(this.fighter.getCurPm(this.fight) > 0 && firstEnnemy == null && secondEnnemy == null) {
                int num = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, E);
                if(num != 0) {
                    time = num;
                    action = true;
                    firstEnnemy = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1);
                    secondEnnemy = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);
                    if(maxPo == 1) firstEnnemy = null;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Funcion.getInstance().invocIfPossible(this.fight, this.fighter, this.invocations)) {
                    time = 2000;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && firstEnnemy != null && secondEnnemy == null && !action) {
                int num = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                if(num != 0) {
                    time = num;
                    action = true;
                }
            } else if(this.fighter.getCurPa(this.fight) > 0 && secondEnnemy != null && !action) {
                int num = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.cacs);
                if(num != 0) {
                    time = num;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && secondEnnemy != null && !action) {
                int num = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                if(num != 0) {
                    time = num;
                    action = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && !action) {
                int num = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, E);
                if(num != 0) time = num;
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}