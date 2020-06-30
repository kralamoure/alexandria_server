package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.comunes.Camino;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA28 extends InteligenciaAbstracta {

    private boolean tp = false, invoc = false;

    public IA28(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Peleador target = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 11);
            int time = 1000, dist = 1000;

            if(target == null) {
                for (Peleador t : Funcion.getInstance().getLowHpEnnemyList(this.fight, this.fighter).values()) {
                    if (t != null && !t.isHide()) {
                        int tDist = Camino.getDistanceBetweenTwoCase(this.fight.getMap(), this.fighter.getCell(), t.getCell());
                        if (tDist < dist) {
                            target = t;
                            dist = tDist;
                        }
                    }
                }
            }

            if(dist == 1000) {
                if (target != null) {
                    dist = Camino.getDistanceBetweenTwoCase(this.fight.getMap(), this.fighter.getCell(), target.getCell());
                } else {
                    target = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 50);
                    dist = Camino.getDistanceBetweenTwoCase(this.fight.getMap(), this.fighter.getCell(), target.getCell());
                }
            }

            if (!this.invoc && Funcion.getInstance().tryTurtleInvocation(this.fight, this.fighter)) {
                time = 1500;
                this.invoc = true;
            }

            if(!this.tp && Funcion.getInstance().TPIfPossiblesphinctercell(this.fight, this.fighter, target)) {
                this.tp = true;
                time = 400;
            }
            if(dist <= 5 && this.fighter.getCurPm(this.fight) >= dist && Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, target)) {
                time = 600;
            }
            if(!this.tp && Funcion.getInstance().TPIfPossiblesphinctercell(this.fight, this.fighter, target)) {
                this.tp = true;
                time = 400;
            }

            if(Funcion.getInstance().attackIfPossiblesphinctercell(this.fight, this.fighter, target) == 0) {
                time = 800;
            }

            this.addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}