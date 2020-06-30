package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo.SortStats;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA8 extends NecesitaHechizo {

    public IA8(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Peleador ennemy = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);
            boolean action = false;
            int PA = 0, PM = this.fighter.getCurPm(this.fight), maxPo = 1, time = 100;

            for(SortStats spellStats : this.buffs)
                if(spellStats != null && spellStats.getMaxPO() > maxPo)
                    maxPo = spellStats.getMaxPO();

            Peleador target = Funcion.getInstance().getNearestInvocnbrcasemax(this.fight, this.fighter, 0, maxPo);//2 = po min 1 + 1;

            if(PM > 0 && target == null) {
                int num = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, ennemy);
                if(num != 0) {
                    time = num;
                    action = true;
                    target = Funcion.getInstance().getNearestInvocnbrcasemax(this.fight, this.fighter, 0, maxPo);//2 = po min 1 + 1;
                }
            }

            PA = this.fighter.getCurPa(this.fight);
            PM = this.fighter.getCurPm(this.fight);

            if(PA > 0 && !action) {
                if (Funcion.getInstance().invocIfPossibleloin(this.fight, this.fighter, this.invocations)) {
                    time = 400;
                    action = true;
                }
            }
            if(PA > 0 && !action && target != null) {
                if (Funcion.getInstance().buffIfPossible(this.fight, this.fighter, target, this.buffs)) {
                    time = 400;
                    action = true;
                }
            }

            if(PM > 0 && !action) {
                int num = Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter);
                if(num != 0) time = num;
            }

            if(this.fighter.getCurPa(fight) == 0 && this.fighter.getCurPm(fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}