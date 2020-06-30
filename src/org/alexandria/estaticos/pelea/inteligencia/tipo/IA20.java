package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA20 extends InteligenciaAbstracta {

    private byte attack = 0;

    public IA20(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Peleador nearestEnnemy = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 5);
            Peleador highestEnnemy = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 30);
            int attack = 0, tp = 0;
            boolean action = false;

            List<Short> cells = this.fight.getAllGlyphs().stream().filter(glyph -> glyph != null && glyph.getLanzador().getId() == this.fighter.getId()).map(glyph -> (short) glyph.getCelda().getId()).collect(Collectors.toList());

            if(nearestEnnemy == null)
                if(Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, highestEnnemy))
                    action = true;
            if(this.attack == 0 && !action)
                attack = Funcion.getInstance().attackIfPossibleKaskargo(this.fight, this.fighter, this.fighter);

            if(attack != 0) {
                this.attack++;
                action = true;
            }


            if(!action && !cells.isEmpty()) {
                if(cells.contains((short) this.fighter.getCell().getId()))
                    tp = Funcion.getInstance().tpIfPossibleKaskargo(this.fight, this.fighter, nearestEnnemy);
            }
            if(tp != 0) action = true;
            if(!action) Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, highestEnnemy);

            addNext(this::decrementCount, 1000);
        } else {
            this.stop = true;
        }
    }
}