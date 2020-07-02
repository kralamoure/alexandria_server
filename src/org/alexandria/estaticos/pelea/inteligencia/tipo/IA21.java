package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

import java.util.ArrayList;
import java.util.List;

public class IA21 extends InteligenciaAbstracta {

    private List<Peleador> firstAttack = new ArrayList<Peleador>();
    private List<Peleador> secondAttack = new ArrayList<Peleador>();

    public IA21(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Funcion.getInstance().invoctantaIfPossible(this.fight, this.fighter);
            if (this.fighter.haveState(31) && this.fighter.haveState(32) && this.fighter.haveState(33)
                    && this.fighter.haveState(34)) {
                Hechizo.SortStats spell = Funcion.getInstance().findSpell(this.fighter, 1106);
                this.fight.forceCastSpellMob(this.fighter, spell, this.fighter.getCell().getId());
            }
            Peleador nearEnnemy = Funcion.getInstance().getEnnemyWithDistance(this.fight, this.fighter, 1, 8,
                    this.firstAttack);
            Peleador farEnnemy = Funcion.getInstance().getEnnemyWithDistance(this.fight, this.fighter, 3, 60,
                    this.secondAttack);
            if (nearEnnemy != null && this.attackNearIfPossible(this.fight, this.fighter, nearEnnemy) == 0) {
                this.firstAttack.add(nearEnnemy);
            }
            if (farEnnemy != null && this.attackFarIfPossible(this.fight, this.fighter, farEnnemy) == 0) {
                this.secondAttack.add(farEnnemy);
            }
            if (!this.fighter.haveState(7)) {
                Hechizo.SortStats spell = Funcion.getInstance().findSpell(this.fighter, 1279);
                this.fight.tryCastSpell(this.fighter, spell, this.fighter.getCell().getId());
            }
            this.addNext(this::decrementCount, 1000);
        } else {
            this.stop = true;
        }
    }

    public int attackNearIfPossible(Pelea fight, Peleador fighter, Peleador target) {
        if (fight == null || fighter == null || target == null) {
            return 10;
        }
        Hechizo.SortStats spell = Funcion.getInstance().findSpell(fighter, 1104);
        return fight.tryCastSpell(fighter, spell, target.getCell().getId());
    }

    public int attackFarIfPossible(Pelea fight, Peleador fighter, Peleador target) {
        if (fight == null || fighter == null || target == null) {
            return 10;
        }
        Hechizo.SortStats spell = Funcion.getInstance().findSpell(fighter, 1105);
        return fight.tryCastSpell(fighter, spell, target.getCell().getId());
    }
}