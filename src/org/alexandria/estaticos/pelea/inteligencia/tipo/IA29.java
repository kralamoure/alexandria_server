package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA29 extends InteligenciaAbstracta {

    public IA29(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Peleador ennemy = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);

            if(!Funcion.getInstance().buffIfPossibleTortu(this.fight, this.fighter, this.fighter))
                Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, ennemy);
            Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, ennemy);

            addNext(this::decrementCount, 1000);
        } else {
            this.stop = true;
        }
    }
}