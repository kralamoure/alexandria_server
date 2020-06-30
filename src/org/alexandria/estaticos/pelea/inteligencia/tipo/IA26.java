package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA26 extends InteligenciaAbstracta {

    public IA26(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Peleador ennemy = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);
            Funcion.getInstance().attackIfPossibleAll(this.fight, this.fighter, ennemy);

            if (!Funcion.getInstance().invocIfPossible(this.fight, this.fighter))
                if (!Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, ennemy))
                    if (!Funcion.getInstance().buffIfPossibleKitsou(this.fight, this.fighter, this.fighter))
                        if(Funcion.getInstance().attackIfPossibleAll(this.fight, this.fighter, ennemy) == 0)
                            Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter);

            addNext(this::decrementCount, 2000);
        } else {
            this.stop = true;
        }
    }
}