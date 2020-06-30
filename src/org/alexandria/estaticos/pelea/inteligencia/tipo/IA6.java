package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA6 extends InteligenciaAbstracta {

    public IA6(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            if (!Funcion.getInstance().invocIfPossible(this.fight, this.fighter)) {
                Peleador friend = Funcion.getInstance().getNearestFriend(this.fight, this.fighter);
                Peleador ennemy = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);

                if (!Funcion.getInstance().HealIfPossible(this.fight, this.fighter, false)) {
                    if (!Funcion.getInstance().buffIfPossible(this.fight, this.fighter, friend)) {
                        if (!Funcion.getInstance().buffIfPossible(this.fight, this.fighter, this.fighter)) {
                            if (!Funcion.getInstance().HealIfPossible(this.fight, this.fighter, true)) {
                                int attack = Funcion.getInstance().attackIfPossibleAll(fight, this.fighter, ennemy);

                                if (attack != 0) {
                                    if (attack == 5) this.stop = true;
                                    if (Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter) != 0) this.stop = true;
                                }
                            }
                        }
                    }
                }
            }

            addNext(this::decrementCount, 1000);
        } else {
            this.stop = true;
        }
    }
}