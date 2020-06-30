package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo.SortStats;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

public class IA12 extends NecesitaHechizo {

    private byte attack = 0;
    private boolean boost = false;

    public IA12(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Peleador ennemy = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);
            int PA = this.fighter.getCurPa(this.fight), PM = this.fighter.getCurPm(this.fight), time = 100, maxPo = 1;
            boolean action = false;

            if (this.fighter.getMob().getPa() < PA) this.boost = true;

            for (SortStats spellStats : this.highests)
                if (spellStats != null && spellStats.getMaxPO() > maxPo)
                    maxPo = spellStats.getMaxPO();

            Peleador target = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 3);

            if (target != null)
                if (target.isHide())
                    target = null;

            if (PM > 0 && target == null && this.attack == 0 || PM > 0 && target == null && this.attack == 1 && this.boost) {
                int num = Funcion.getInstance().movediagIfPossible(this.fight, this.fighter, ennemy);
                if (num != 0) {
                    time = num;
                    action = true;
                    target = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 3);
                }
            }

            PA = this.fighter.getCurPa(this.fight);
            PM = this.fighter.getCurPm(this.fight);

            if (PA > 0 && target != null && !action) {
                int num = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                if (num != 0) {
                    time = num;
                    action = true;
                    this.attack++;
                }
            }

            if (PM > 0 && !action && this.attack > 0) {
                int num = Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter);
                if (num != 0) time = num;
            }

            if (this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0)
                this.stop = true;

            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}