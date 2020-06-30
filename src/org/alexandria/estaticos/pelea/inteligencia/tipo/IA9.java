package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo.SortStats;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA9 extends InteligenciaAbstracta {

    public IA9(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (this.count > 0 && this.fighter.canPlay() && !this.stop) {
            Peleador target = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);
            if (target == null) return;

            int value = Funcion.getInstance().moveToAttackIfPossible(this.fight, this.fighter), cellId = value - (value / 1000) * 1000;
            SortStats spellStats = this.fighter.getMob().getSpells().get(value / 1000);

            if (cellId != -1) {
                if (this.fight.canCastSpell1(this.fighter, spellStats, this.fighter.getCell(), cellId))
                    this.fight.tryCastSpell(this.fighter, spellStats, cellId);
            } else if (Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter) != 0) {
                this.stop = true;
            }

            addNext(this::decrementCount, 800);
        } else {
            this.stop = true;
        }
    }
}