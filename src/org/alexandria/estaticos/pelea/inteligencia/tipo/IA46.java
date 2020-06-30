package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA46 extends NecesitaHechizo {

    private boolean boost = false, heal = false;

    public IA46(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100, maxPo = 1;
            boolean action = false;
            Peleador A = Funcion.getInstance().getNearestFriend(this.fight, this.fighter);

            for(Hechizo.SortStats spellStats : this.highests)
                if(spellStats.getMaxPO() > maxPo)
                    maxPo = spellStats.getMaxPO();

            Peleador L = Funcion.getInstance().getNearestAminoinvocnbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
            if(this.fighter.getCurPa(this.fight) > 0 && (L != null || A != null) && !this.boost) {
                if (Funcion.getInstance().buffIfPossible(this.fight, this.fighter, (L == null ? A : L), this.buffs)) {
                    time = 1000;
                    action = true;
                    this.boost = true;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && !action && !this.heal) {
                if (Funcion.getInstance().HealIfPossible(this.fight, this.fighter, true, 50) != 0) {
                    time = 2000;
                    action = true;
                    this.heal = true;
                }
            }


            if(L != null && (L.getPdv() * 100) / L.getPdvMax() > 99)
                    L = Funcion.getInstance().getNearestAminoinvocnbrcasemax(this.fight, this.fighter, 1, maxPo);
            if(L != null) if(L.isHide()) L = null;

            if(this.fighter.getCurPm(this.fight) > 0 && L == null) {
                int value = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, A);
                if(value != 0) {
                    time = value;
                    action = true;
                    L = Funcion.getInstance().getNearestAminoinvocnbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
                    if(maxPo == 1) L = null;
                }
            }
            if(this.fighter.getCurPm(this.fight) > 0 && !action && L != null && !this.heal) {
                int value = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, A);
                if(value != 0) {
                    time = value;
                    action = true;
                    L = Funcion.getInstance().getNearestinvocateurnbrcasemax(this.fight, this.fighter, 1, maxPo + 1);// pomax +1;
                    if(maxPo == 1) L = null;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && !action && !this.heal) {
                if (Funcion.getInstance().HealIfPossible(this.fight, this.fighter, true, 50) != 0) {
                    time = 2000;
                    action = true;
                    this.heal = true;
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Funcion.getInstance().HealIfPossible(this.fight, this.fighter, false, 99) != 0) {
                    time = 2000;
                    action = true;
                    this.heal = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && !action && this.heal || this.fighter.getCurPm(this.fight) > 0 && !action && this.boost) {
                int value = Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter);
                if(value != 0) time = value;
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0 || this.heal && this.boost && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}