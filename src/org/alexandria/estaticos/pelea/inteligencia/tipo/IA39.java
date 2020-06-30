package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA39 extends NecesitaHechizo {

    private byte attack = 0;

    public IA39(Pelea fight, Peleador fighter, byte count) {
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

            Peleador C = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
            Peleador L = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
            Peleador L2 = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 2, 5);
            Peleador L3 = Funcion.getInstance().getNearEnnemylignenbrcasemax(this.fight, this.fighter, 0, maxPo);

            if(maxPo == 1) L = null;
            if(C != null) if(C.isHide()) C = null;
            if(L != null) if(L.isHide()) L = null;
            if(this.fighter.getCurPa(this.fight) > 0 && L3 != null && C == null && !action) {
                int value = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                if(value != 0) {
                    this.attack++;
                    time = value;
                    action = true;
                }
            }
            if(this.fighter.getCurPm(this.fight) > 0 && C == null || this.attack == 1 && this.fighter.getCurPm(this.fight) > 0) {
                int value = Funcion.getInstance().moveenfaceIfPossible(this.fight, this.fighter, ennemy, maxPo + 1);
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
                    time = 1000;
                    action = true;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Funcion.getInstance().buffIfPossible(this.fight, this.fighter, this.fighter, this.buffs)) {
                    time = 1000;
                    action = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && L != null && C == null && !action) {
                int value = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                if(value != 0) {
                    this.attack++;
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

            if(this.fighter.getCurPa(this.fight) > 0 && C != null && !action) {
                int value = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                if(value != 0) {
                    this.attack++;
                    time = value;
                    action = true;
                } else if(this.fighter.getCurPm(this.fight) > 0) {
                    value = Funcion.getInstance().moveenfaceIfPossible(this.fight, this.fighter, L2, maxPo + 1);
                    if(value != 0) {
                        time = value;
                        action = true;
                    }
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && !action && C != null) {
                int value = Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter);
                if(value != 0) time = value;
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}