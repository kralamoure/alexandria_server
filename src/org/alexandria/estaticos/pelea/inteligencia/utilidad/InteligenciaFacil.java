package org.alexandria.estaticos.pelea.inteligencia.utilidad;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.EfectoHechizo;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;

import java.util.ArrayList;
import java.util.List;

public abstract class InteligenciaFacil
extends InteligenciaAbstracta {
    protected byte flag = 0;
    protected short time = 0;
    protected List<Hechizo.SortStats> attacks;
    protected List<Hechizo.SortStats> friendBuffs;
    protected List<Hechizo.SortStats> enemyBuffs;
    protected List<Hechizo.SortStats> heals;
    protected List<Hechizo.SortStats> traps;
    protected List<Hechizo.SortStats> invocations;

    public InteligenciaFacil(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
        this.attacks = this.getListSpellOf(fighter, "ATTACK");
        this.friendBuffs = this.getListSpellOf(fighter, "FRIEND-BUFF");
        this.enemyBuffs = this.getListSpellOf(fighter, "ENEMY-BUFF");
        this.heals = this.getListSpellOf(fighter, "HEAL");
        this.traps = this.getListSpellOf(fighter, "TRAP");
        this.invocations = this.getListSpellOf(fighter, "INVOCATION");
    }

    protected void setNextParams(int flag, int count, int time) {
        this.flag = (byte)flag;
        this.count = (byte)count;
        this.time = (short)time;
    }

    protected Funcion get() {
        return Funcion.getInstance();
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            this.run();
            this.flag = (byte)(this.flag + 1);
            if (this.time == 0 && this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) {
                this.stop = true;
                this.time = 1000;
            }
            this.addNext(this::decrementCount, Integer.valueOf(this.time));
        } else {
            this.stop = true;
            this.time = 500;
        }
    }

    public abstract void run();

    private List<Hechizo.SortStats> getListSpellOf(Peleador fighter, String type) {
        ArrayList<Hechizo.SortStats> spells = new ArrayList<Hechizo.SortStats>();
        for (Hechizo.SortStats spell : fighter.getMob().getSpells().values()) {
            if (spells.contains(spell)) continue;
            block9 : switch (type) {
                case "ATTACK": {
                    if (spell.getSpell().getType() != 0) break;
                    spells.add(spell);
                    break;
                }
                case "FRIEND-BUFF": {
                    if (spell.getSpell().getType() != 1) break;
                    spells.add(spell);
                    break;
                }
                case "ENEMY-BUFF": {
                    if (spell.getSpell().getType() != 2) break;
                    spells.add(spell);
                    break;
                }
                case "HEAL": {
                    if (spell.getSpell().getType() != 3) break;
                    spells.add(spell);
                    break;
                }
                case "TP": {
                    if (spell.getSpell().getType() != 4) break;
                    spells.add(spell);
                    break;
                }
                case "TRAP": {
                    if (spell.getSpell().getType() != 5) break;
                    spells.add(spell);
                    break;
                }
                case "INVOCATION": {
                    for (EfectoHechizo effect : spell.getEffects()) {
                        if (effect.getEffectID() != 181) continue;
                        spells.add(spell);
                        break block9;
                    }
                    break;
                }
            }
        }
        return spells;
    }

    public List<Hechizo.SortStats> getAttacksSpells() {
        return this.attacks;
    }

    public List<Hechizo.SortStats> getFriendBuffsSpells() {
        return this.friendBuffs;
    }

    public List<Hechizo.SortStats> getHealsSpells() {
        return this.heals;
    }
}

