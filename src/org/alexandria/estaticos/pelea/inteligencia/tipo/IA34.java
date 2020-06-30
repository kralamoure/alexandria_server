package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA34 extends NecesitaHechizo {

    private int attack = 0;

    public IA34(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100, maxPo = 1;
            boolean action = false;

            Peleador ennemy0 = Funcion.getInstance().getNearestAllnbrcasemax(this.fight, this.fighter, 0, 100);
            if(this.attack >= 1) ennemy0 = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);

            for(Hechizo.SortStats spellStats : this.highests)
                if(spellStats != null && spellStats.getMaxPO() > maxPo)
                    maxPo = spellStats.getMaxPO();

            Peleador ennemy1 = Funcion.getInstance().getNearestAllnbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
            Peleador ennemy2 = Funcion.getInstance().getNearestAllnbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;

            if(this.attack >= 1) {
                ennemy1 = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
                ennemy2 = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
            }

            if(ennemy2 != null) if(ennemy2.isHide()) ennemy2 = null;
            if(ennemy1 != null) if(ennemy1.isHide()) ennemy1 = null;
            if(maxPo == 1) ennemy1 = null;

            if(this.fighter.getCurPm(this.fight) > 0 && ennemy1 == null && ennemy2 == null) {
                int value = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, ennemy0);
                if(value != 0) {
                    time = value;
                    action = true;
                    ennemy1 = Funcion.getInstance().getNearestAllnbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
                    ennemy2 = Funcion.getInstance().getNearestAllnbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                    if(this.attack >= 1) {
                        ennemy1 = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
                        ennemy2 = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                    }
                    if(maxPo == 1) ennemy1 = null;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Funcion.getInstance().invocIfPossible(this.fight, this.fighter, this.invocations)) {
                    time = 2000;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && ennemy1 != null && ennemy2 == null && !action) {
                int value = Funcion.getInstance().attackAllIfPossible(this.fight, this.fighter, this.highests);
                if(value != 0) {
                    time = value;
                    action = true;
                    this.attack++;
                }
            } else if(this.fighter.getCurPa(this.fight) > 0 && ennemy2 != null && !action) {
                int value = Funcion.getInstance().attackAllIfPossible(this.fight, this.fighter, this.cacs);
                if(value != 0) {
                    time = value;
                    action = true;
                    this.attack++;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && !action) {
                int value = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, ennemy0);
                if(value != 0) time = value;
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}