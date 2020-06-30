package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA22 extends InteligenciaAbstracta {

    public IA22(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Peleador ennemy = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);

            if (Funcion.getInstance().IfPossibleRasboulvulner(this.fight, this.fighter, this.fighter) == 0)
                if (Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter) == 0)
                    if (Funcion.getInstance().tpIfPossibleRasboul(this.fight, this.fighter, ennemy) == 0)
                        Funcion.getInstance().invocIfPossible(this.fight, this.fighter);

            addNext(this::decrementCount, 1000);
        } else {
            this.stop = true;
        }
    }
}