package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo.SortStats;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA14 extends InteligenciaAbstracta {

    public IA14(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (this.count > 0 && this.fighter.canPlay() && !this.stop) {
            if (!this.fighter.haveInvocation()) {
                if (!Funcion.getInstance().invocIfPossible(this.fight, this.fighter)) {
                    Peleador target = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);
                    int value = Funcion.getInstance().moveToAttackIfPossible(this.fight, this.fighter), cellId = value - (value / 1000) * 1000;
                    SortStats spellStats = this.fighter.getMob().getSpells().get(value / 1000);

                    if (this.fight.canCastSpell1(this.fighter, spellStats, this.fighter.getCell(), cellId)) this.fight.tryCastSpell(this.fighter, spellStats, cellId);
                    else Funcion.getInstance().moveNearIfPossible(fight, this.fighter, target);
                }
            } else {
                Peleador target = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);
                int value = Funcion.getInstance().moveToAttackIfPossible(this.fight, this.fighter), cellId = value - (value / 1000) * 1000;
                SortStats spellStats = this.fighter.getMob().getSpells().get(value / 1000);

                if (this.fight.canCastSpell1(this.fighter, spellStats, this.fighter.getCell(), cellId)) this.fight.tryCastSpell(this.fighter, spellStats, cellId);
                else Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, target);
            }

            addNext(this::decrementCount, 1000);
        } else {
            this.stop = true;
        }
    }
}