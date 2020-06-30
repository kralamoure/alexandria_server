package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA31 extends InteligenciaAbstracta {

    public IA31(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Peleador nearestEnnemy = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);
            Peleador longestEnnemy = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 3);

            if (!Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, nearestEnnemy))
                if(Funcion.getInstance().attackIfPossiblerat(this.fight, this.fighter, nearestEnnemy, longestEnnemy == null) == 0)
                    Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, nearestEnnemy);

            addNext(this::decrementCount, 800);
        } else {
            this.stop = true;
        }
    }
}