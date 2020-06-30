package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.comunes.Camino;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA19 extends InteligenciaAbstracta {

    private boolean tp = false;

    public IA19(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Peleador friend = Funcion.getInstance().getNearestFriend(this.fight, this.fighter);
            Peleador ennemy = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);

            int dist1 = Camino.getDirBetweenTwoCase(friend.getCell().getId(), ennemy.getCell().getId(), this.fight.getMap(), true);
            int dist2 = Camino.getDirBetweenTwoCase(this.fighter.getCell().getId(), ennemy.getCell().getId(), this.fight.getMap(), true);

            for (Peleador t : this.fight.getFighters(3)) {
                if (t != null && t.getTeam() == this.fighter.getTeam()) {
                    int tDist = Camino.getDistanceBetweenTwoCase(this.fight.getMap(), t.getCell(), ennemy.getCell());
                    if (dist2 > tDist && dist1 > tDist) {
                        dist1 = tDist;
                        friend = t;
                    }
                }
            }

            boolean needTp = dist2 > dist1;

            if(dist2 <= 3) {
                needTp = false;
                this.tp = true;
            }

            if (needTp && !this.tp && Funcion.getInstance().tpIfPossibleTynril(this.fight, this.fighter, friend) == 0) {
                this.tp = true;
            } else if(!needTp) {
                Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, ennemy);
                dist1 = -5;
            }

            if (this.fighter.getCurPm(this.fight) > 0 && dist1 != -5) {
                int dist = Camino.getDirBetweenTwoCase(this.fighter.getCell().getId(), ennemy.getCell().getId(), this.fight.getMap(), true);
                if(dist > 1) {
                    Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, ennemy);
                }
            }

            if (!Funcion.getInstance().HealIfPossiblefriend(fight, this.fighter, friend)) {
                Funcion.getInstance().attackIfPossibleTynril(this.fight, this.fighter, ennemy);
            }
            this.addNext(this::decrementCount, 1000);
        } else {
            this.stop = true;
        }
    }
}