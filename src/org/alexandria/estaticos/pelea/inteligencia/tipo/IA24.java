package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA24 extends InteligenciaAbstracta {

    public IA24(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Peleador friend = Funcion.getInstance().getNearestFriendNoInvok(this.fight, this.fighter);

            if (!Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, friend))
                if (!Funcion.getInstance().buffIfPossible(this.fight, this.fighter, friend))
                    Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter);

            addNext(this::decrementCount, 800);
        } else {
            this.stop = true;
        }
    }
}