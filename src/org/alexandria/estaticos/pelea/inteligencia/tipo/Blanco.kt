package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;

/**
 * Created by Locos on 18/09/2015.
 */
public class Blanco extends InteligenciaAbstracta {

    public Blanco(Pelea fight, Peleador fighter) {
        super(fight, fighter, (byte) 1);
    }

    @Override
    public void apply() {
        this.stop = true;
    }
}
