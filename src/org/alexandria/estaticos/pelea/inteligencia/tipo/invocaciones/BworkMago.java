package org.alexandria.estaticos.pelea.inteligencia.tipo.invocaciones;

import org.alexandria.comunes.Camino;
import org.alexandria.comunes.Formulas;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

import java.util.ArrayList;

public class BworkMago
extends NecesitaHechizo {
    private byte flag = 0;

    public BworkMago(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 0;
            Peleador enemy = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter, true);
            if (enemy == null) {
                time = Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter);
            } else {
                switch (this.flag) {
                    case 0 -> {
                        Peleador fighter;
                        Hechizo.SortStats spell;
                        ArrayList<Peleador> fighters = Camino.getEnemyFighterArround(this.fighter.getCell().getId(), this.fight.getMap(), this.fight, false);
                        if (fighters == null || fighters.isEmpty() || (fighter = fighters.get(Formulas.random.nextInt(fighters.size()))) == null || (spell = Funcion.getInstance().getSpellByPo(this.fighter, 1)) == null || Funcion.getInstance().tryCastSpell(this.fight, this.fighter, enemy, spell.getSpell().getSpellID()) != 0)
                            break;
                        if (fighters.size() > 1) {
                            this.flag = -1;
                            this.count = 5;
                        }
                        time = 1500;
                        break;
                    }
                    case 1 -> {
                        Hechizo.SortStats spell = Funcion.getInstance().getSpellByPo(this.fighter, 666);
                        int cell = Funcion.getInstance().getBestTargetZone(this.fight, this.fighter, spell, enemy.getCell().getId(), false);
                        int nbTarget = cell / 1000;
                        cell -= nbTarget * 1000;
                        if (nbTarget == 1) {
                            cell = enemy.getCell().getId();
                        }
                        if (spell == null || spell.getPACost() > this.fighter.getCurPa(this.fight) || !Funcion.getInstance().moveToAttack(this.fight, this.fighter, this.fight.getMap().getCase(cell), spell))
                            break;
                        time = 2000;
                        break;
                    }
                    case 2 -> {
                        Hechizo.SortStats spell = Funcion.getInstance().getSpellByPo(this.fighter, 666);
                        int cell = Funcion.getInstance().getBestTargetZone(this.fight, this.fighter, spell, enemy.getCell().getId(), false);
                        int nbTarget = cell / 1000;
                        cell -= nbTarget * 1000;
                        if (nbTarget == 1) {
                            cell = enemy.getCell().getId();
                        }
                        if (spell != null) {
                            if (this.fight.tryCastSpell(this.fighter, spell, cell) == 0) {
                                this.count = 4;
                                this.flag = 0;
                                time = 2000;
                            } else if (this.fight.tryCastSpell(this.fighter, spell, enemy.getCell().getId()) == 0) {
                                this.count = 4;
                                this.flag = 0;
                                time = 2000;
                            }
                        }
                        if (time == 2000) break;
                        if (spell != null && Funcion.getInstance().tryCastSpell(this.fight, this.fighter, enemy, spell.getSpell().getSpellID()) == 0) {
                            this.count = 4;
                            this.flag = 0;
                            time = 2000;
                            break;
                        }
                        if (spell == null || Funcion.getInstance().tryCastSpell(this.fight, this.fighter, Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter, true), spell.getSpell().getSpellID()) != 0)
                            break;
                        this.count = 4;
                        this.flag = 0;
                        time = 2000;
                        break;
                    }
                    case 3 -> {
                        if (Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter) <= 0) break;
                        time = 2500;
                    }
                }
                this.flag = (byte)(this.flag + 1);
            }
            this.addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}

