package org.alexandria.estaticos.pelea.inteligencia.tipo.invocaciones;


import org.alexandria.comunes.Camino;
import org.alexandria.comunes.Formulas;
import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

import java.util.ArrayList;
import java.util.List;

public class DragonitoRojo
extends NecesitaHechizo {
    private byte flag = 0;
    private List<Peleador> fighters = new ArrayList<Peleador>();

    public DragonitoRojo(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            try {
                int time = 0;
                Hechizo.SortStats spell = Funcion.getInstance().getSpellByPo(this.fighter, 666);
                Peleador enemy = Funcion.getInstance().getEnnemyWithDistance(this.fight, this.fighter, 0, 666, this.fighters);
                Peleador nearest = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter, true);
                if (nearest != enemy && nearest != null && enemy != null && this.fight.canCastSpell1(this.fighter, spell, this.fighter.getCell(), nearest.getCell().getId())) {
                    enemy = nearest;
                } else if (enemy == nearest) {
                    this.fighters.add(enemy);
                    Peleador temp = Funcion.getInstance().getEnnemyWithDistance(this.fight, this.fighter, 0, 666, this.fighters);
                    if (temp != null) {
                        enemy = temp;
                    }
                    this.fighters.remove(enemy);
                }
                switch (this.flag) {
                    case 0 -> {
                        Hechizo.SortStats spell1;
                        Peleador target = this.getFightersForDebuffing();
                        if (target == null || (spell1 = Funcion.getInstance().getSpellByPo(this.fighter, 3)) == null || !this.fight.canLaunchSpell(this.fighter, spell1, target.getCell()))
                            break;
                        if (Funcion.getInstance().tryCastSpell(this.fight, this.fighter, target, spell1.getSpell().getSpellID()) == 0) {
                            time = 1500;
                            break;
                        }
                        if (Funcion.getInstance().moveenfaceIfPossible(this.fight, this.fighter, target, 3) > 0) {
                            time = 2000;
                            this.flag = -1;
                            this.count = 6;
                            break;
                        }
                        if (!Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, target)) break;
                        time = 2000;
                        this.flag = -1;
                        this.count = 6;
                        break;
                    }
                    case 1 -> {
                        if (spell == null || spell.getPACost() > this.fighter.getCurPa(this.fight) || enemy == null)
                            break;
                        if (Funcion.getInstance().tryCastSpell(this.fight, this.fighter, enemy, spell.getSpell().getSpellID()) == 0) {
                            this.fighters.add(enemy);
                            time = 1500;
                            this.flag = 0;
                            this.count = 5;
                            break;
                        }
                        Mapa.GameCase cell1 = this.fighter.getCell();
                        Mapa.GameCase cell2 = enemy.getCell();
                        char dir = Camino.getDirBetweenTwoCase(cell1.getId(), cell2.getId(), this.fight.getMap(), true);
                        if (!Camino.casesAreInSameLine(this.fight.getMap(), cell1.getId(), cell2.getId(), dir, spell.getMaxPO())) {
                            if (!Funcion.getInstance().moveToAttack(this.fight, this.fighter, enemy, spell)) break;
                            time = 2000;
                            break;
                        }
                        if (!Funcion.getInstance().moveToAttack(this.fight, this.fighter, enemy, spell)) break;
                        time = 2000;
                        break;
                    }
                    case 2 -> {
                        if (spell == null || enemy == null) break;
                        int distance = Camino.getDistanceBetween(this.fight.getMap(), this.fighter.getCell().getId(), enemy.getCell().getId());
                        if (spell.getMaxPO() >= distance || !Funcion.getInstance().moveToAttack(this.fight, this.fighter, enemy, spell))
                            break;
                        time = 2000;
                        break;
                    }
                    case 3 -> {
                        if (spell != null && Funcion.getInstance().tryCastSpell(this.fight, this.fighter, enemy, spell.getSpell().getSpellID()) == 0) {
                            this.fighters.add(enemy);
                            time = 1500;
                            this.flag = 0;
                            this.count = 5;
                            break;
                        }
                        time = Funcion.getInstance().moveautourIfPossible(this.fight, this.fighter, enemy);
                        break;
                    }
                    case 4 -> {
                        if (!this.fighters.isEmpty() && Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter) > 0) {
                            time = 2000;
                            break;
                        }
                        if (!Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, enemy)) break;
                        time = 2000;
                        this.count = 5;
                        this.flag = 0;
                    }
                }
                this.flag = (byte)(this.flag + 1);
                this.addNext(this::decrementCount, time);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.stop = true;
        }
    }

    public Peleador getFightersForDebuffing() {
        ArrayList<Peleador> fightersForDebuffing = new ArrayList<Peleador>();
        for (Peleador temp : this.getFight().getFighters(7)) {
            if (temp.isHide() || temp.isDead() || temp.getFightBuff() == null || temp.getFightBuff().size() <= 1 || temp.getTeam() == this.fighter.getTeam() || Camino.getDistanceBetween(this.fight.getMap(), temp.getCell().getId(), this.fighter.getCell().getId()) > 6) continue;
            fightersForDebuffing.add(temp);
        }
        if (fightersForDebuffing.isEmpty()) {
            return null;
        }
        return (Peleador)fightersForDebuffing.get(Formulas.random.nextInt(fightersForDebuffing.size()));
    }
}

