package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo.SortStats;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA17 extends NecesitaHechizo {

    public IA17(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Peleador ennemy = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);
            int time = 100, maxPo = 1;
            boolean action = false;

            for(SortStats spellStats : this.highests)
                if(spellStats != null && spellStats.getMaxPO() > maxPo)
                    maxPo = spellStats.getMaxPO();

            Peleador target = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);

            if(target != null)
                if(target.isHide())
                    target = null;

            if(this.fighter.getCurPa(this.fight) > 0) {
                if (Funcion.getInstance().invocIfPossibleloin(this.fight, this.fighter, this.invocations)) {
                    time = 3000;
                    action = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && target == null) {
                int num = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, ennemy);
                if(num != 0) {
                    time = num;
                    action = true;
                    target = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && target == null) {
                int num = Funcion.getInstance().attackBondIfPossible(this.fight, this.fighter, ennemy);
                if(num != 0) {
                    time = num;
                    action = true;
                    target = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && target == null && !action) {
                int num = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                if(num != 0) {
                    time = num;
                    action = true;
                }
            } else if(this.fighter.getCurPa(this.fight) > 0 && target != null && !action) {
                int num = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.cacs);
                if(num != 0) {
                    time = num;
                    action = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && !action) {
                int num = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, ennemy);
                if(num != 0) time = num;
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}