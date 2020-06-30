package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.comunes.Camino;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 24/01/2017.
 */
public class IA71 extends NecesitaHechizo {

    public IA71(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100, maxPo = 1;
            Peleador nearestEnnemy = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);

            for(Hechizo.SortStats S : this.highests)
                if(S.getMaxPO() > maxPo)
                    maxPo = S.getMaxPO();

            Peleador ennemy1 = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 8);// pomax +1;
            Peleador ennemy2 = this.getNearestLowerHpEnemy();// low hp enemy

            if(this.fighter.getCurPa(this.fight) > 0) {
                if (Funcion.getInstance().HealIfPossible(this.fight, this.fighter, true, 40) != 0) {
                    time = 1000;
                }
            }

            if(ennemy1 == null) {
                Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, nearestEnnemy);
            }

            if(this.fighter.getCurPa(this.fight) > 0) {
                if (Funcion.getInstance().invocIfPossible(this.fight, this.fighter, this.invocations)) {
                    time = 1000;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0) {
                if (Funcion.getInstance().buffIfPossible(this.fight, this.fighter, this.fighter, this.buffs)) {
                    time = 1200;
                    if(this.fighter.getCurPa(this.fight) > 0) {
                        if (Funcion.getInstance().buffIfPossible(this.fight, this.fighter, this.fighter, this.buffs)) {
                            time = 1200;
                        }
                    }
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0 && ennemy2 != null) {
                int value = Funcion.getInstance().attackIfPossibleAll(this.fight, this.fighter, ennemy2);
                if(value != 0) {
                    time = value;
                }
            } else if(this.fighter.getCurPa(this.fight) > 0 && ennemy1 != null) {
                int value = Funcion.getInstance().attackIfPossibleAll(this.fight, this.fighter, ennemy1);
                if(value != 0) {
                    time = value;
                }
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            this.addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }

    private Peleador getNearestLowerHpEnemy() {
        for(Peleador fighter : Funcion.getInstance().getLowHpEnnemyList(this.fight, this.fighter).values())
            if(fighter != null && Camino.getDistanceBetweenTwoCase(this.fight.getMap(), this.fighter.getCell(), fighter.getCell()) < 8)
                return fighter;
        return null;
    }
}