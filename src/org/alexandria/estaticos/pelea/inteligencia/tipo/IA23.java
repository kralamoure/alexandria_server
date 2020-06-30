package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA23 extends InteligenciaAbstracta {

    public IA23(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Peleador ennemy = Funcion.getInstance().getNearestFriendNoInvok(this.fight, this.fighter);

            if (!Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, ennemy))
                Funcion.getInstance().HealIfPossible(this.fight, this.fighter, false);

            addNext(this::decrementCount, 500);
        } else {
            this.stop = true;
        }
    }
}