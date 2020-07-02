package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.comunes.Camino;
import org.alexandria.comunes.Formulas;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

import java.util.ArrayList;
import java.util.Collection;

public class IARecaudador extends InteligenciaAbstracta {
        private byte flag = 0;
        private boolean moved = false;
        private Collection<Hechizo.SortStats> spells;

        public IARecaudador(Pelea fight, Peleador fighter, byte b) {
            super(fight, fighter, b);
            this.spells = Mundo.mundo.getGuild(this.fighter.getCollector().getGuildId()).getSpells().values();
        }

        @Override
        public void apply() {
            if (!this.stop && this.fighter.canPlay() && this.count > 0) {
                int time = 0;
                Peleador enemy = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);
                if (enemy == null) {
                    time = Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter);
                } else {
                    switch (this.flag) {
                        case 0: {
                            int[] buffs = new int[] { 461, 451, 452, 453, 454 };
                            Hechizo.SortStats spell = this.getBestSpell(buffs, this.fighter);
                            if (spell == null
                                    || this.fight.tryCastSpell(this.fighter, spell, this.fighter.getCell().getId()) != 0)
                                break;
                            time = 1500;
                            if (this.getBestSpell(buffs, this.fighter) == null)
                                break;
                            this.flag = -1;
                            this.count = 6;
                            break;
                        }
                        case 1: {
                            Hechizo.SortStats spell;
                            Peleador target = this.getFightersForDebuffing();
                            if (target == null || (spell = this.getBestSpell(new int[] { 460 }, this.fighter)) == null)
                                break;
                            if (Funcion.getInstance().moveToAttack(this.fight, this.fighter, target, spell)) {
                                time = 2000;
                                this.flag = 0;
                                this.count = 5;
                                break;
                            }
                            if (this.fight.tryCastSpell(this.fighter, spell, target.getCell().getId()) != 0)
                                break;
                            time = 1500;
                            break;
                        }
                        case 2: {
                            int[] attacks = new int[] { 458, 456, 457, 458, 462 };
                            Peleador target = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);
                            Hechizo.SortStats spell = this.getBestSpell(attacks, target);
                            if (spell == null)
                                break;
                            if (Funcion.getInstance().moveToAttack(this.fight, this.fighter, target, spell)) {
                                time = 2000;
                                this.flag = 1;
                                this.count = 4;
                                break;
                            }
                            if (spell.getSpell().getSpellID() == 458 || spell.isLineLaunch()) {
                                int cell = Funcion.getInstance().getBestTargetZone(this.fight, this.fighter, spell,
                                        enemy.getCell().getId(), spell.isLineLaunch());
                                int nbTarget = cell / 1000;
                                if (nbTarget <= 1 || this.fight.tryCastSpell(this.fighter, spell, cell -= nbTarget * 1000) != 0)
                                    break;
                                time = 3000;
                                break;
                            }
                            if (this.fight.tryCastSpell(this.fighter, spell, target.getCell().getId()) != 0)
                                break;
                            time = 3000;
                            break;
                        }
                        case 3: {
                            Hechizo.SortStats spell = this.getBestSpell(new int[] { 459 }, this.fighter);
                            if (spell == null
                                    || this.fight.tryCastSpell(this.fighter, spell, this.fighter.getCell().getId()) != 0)
                                break;
                            time = 1500;
                            break;
                        }
                        case 4: {
                            if (Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter) <= 0)
                                break;
                            time = 2500;
                        }
                    }
                    this.flag = (byte) (this.flag + 1);
                }
                this.addNext(this::decrementCount, time);
            } else {
                this.stop = true;
            }
        }

        private Hechizo.SortStats getBestSpell(int[] wantedSpells, Peleador target) {
            for (Hechizo.SortStats spell : this.spells) {
                for (int wanted : wantedSpells) {
                    if (wanted != spell.getSpell().getSpellID()
                            || !this.fight.canLaunchSpell(this.fighter, spell, target.getCell()))
                        continue;
                    return spell;
                }
            }
            return null;
        }

        private Peleador getFightersForDebuffing() {
            ArrayList<Peleador> fightersForDebuffing = new ArrayList<Peleador>();
            for (Peleador temp : this.getFight().getFighters(7)) {
                if (temp.isDead() || temp.getFightBuff() == null || temp.getFightBuff().size() <= 1
                        || temp.getTeam() == this.fighter.getTeam() || Camino.getDistanceBetween(this.fight.getMap(),
                        temp.getCell().getId(), this.fighter.getCell().getId()) > 12)
                    continue;
                fightersForDebuffing.add(temp);
            }
            if (fightersForDebuffing.isEmpty()) {
                return null;
            }
            return (Peleador) fightersForDebuffing.get(Formulas.random.nextInt(fightersForDebuffing.size()));
        }
}
