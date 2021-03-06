package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.configuracion.Constantes;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo.SortStats;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA10 extends InteligenciaAbstracta {

    public IA10(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (count > 0 && this.fighter.canPlay() && !this.stop) {
            Peleador target = Funcion.getInstance().getNearestEnnemy(fight, this.fighter);
            if (target == null) return;

            int value = Funcion.getInstance().moveToAttackIfPossible(fight, this.fighter), cellId = value - (value / 1000) * 1000;
            SortStats spellStats = this.fighter.getMob().getSpells().get(value / 1000);

            if (cellId != -1) {
                if (fight.canCastSpell1(this.fighter, spellStats, this.fighter.getCell(), cellId))
                    fight.tryCastSpell(this.fighter, spellStats, cellId);
            } else if (this.fighter.haveState(Constantes.ETAT_PORTE)) {
                if (!Funcion.getInstance().HealIfPossible(fight, this.fighter, true))
                    if (!Funcion.getInstance().HealIfPossible(fight, this.fighter, false))
                        this.stop = true;
            } else {
                this.stop = true;
            }

            this.addNext(this::decrementCount, 800);
        } else {
            this.stop = true;
        }
    }
}