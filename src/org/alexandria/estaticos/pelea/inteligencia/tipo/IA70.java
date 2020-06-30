package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.comunes.Camino;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.pelea.inteligencia.NecesitaHechizo;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 24/01/2017.
 */
public class IA70 extends NecesitaHechizo {

    public IA70(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            //12 PA - 5 PM
            // invocation corbac : 3 PA
            // sanction ténébreuse : 4 PA
            //carapaces d'ailes : 2 PA    Ne joue plus pendant 3 tours
            // Lien volatile : 2 PA   +CC
            int time = 100;
            boolean action = false;

            Hechizo.SortStats sanction = this.fighter.getMob().getSpells().get(627);
            Hechizo.SortStats lien = this.fighter.getMob().getSpells().get(628);
            Hechizo.SortStats invocation = this.fighter.getMob().getSpells().get(629);
            Hechizo.SortStats carapce = this.fighter.getMob().getSpells().get(630);

            if(!this.fighter.haveInvocation()) {
                int cell = Camino.getAvailableCellArround(this.fight, this.fighter.getCell().getId(), null);
                if(this.fight.canCastSpell1(this.fighter, invocation, this.fighter.getCell(), cell)) {
                    this.fight.tryCastSpell(this.fighter, invocation, cell);
                    time = 1500;
                    action = true;
                }
            }

            if (this.fight.canCastSpell1(this.fighter, lien, this.fighter.getCell(), this.fighter.getCell().getId()) && !action) {
                this.fight.tryCastSpell(this.fighter, lien, this.fighter.getCell().getId());
                time = 1000;
                action = true;
            }

            if (this.fight.canCastSpell1(this.fighter, carapce, this.fighter.getCell(), this.fighter.getCell().getId()) && !action) {
                this.fight.tryCastSpell(this.fighter, carapce, this.fighter.getCell().getId());
                time = 1500;
                action = true;
            }

            Peleador ennemy = Funcion.getInstance().getNearestEnnemy(this.fight, this.fighter);
            Peleador nearestEnnemy = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 3);//2 = po min 1 + 1;

            if(nearestEnnemy != null) if(nearestEnnemy.isHide()) nearestEnnemy = null;
            if(ennemy != null) if(ennemy.isHide()) ennemy = null;

            if(this.fighter.getCurPm(this.fight) > 0 && nearestEnnemy != null && !action) {
                Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, nearestEnnemy);
                time = 1000;
            } else if(this.fighter.getCurPm(this.fight) > 0 && nearestEnnemy == null && !action) {
                Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, ennemy);
                time = 1000;
            }

            if(this.fighter.getCurPa(this.fight) > 0 && nearestEnnemy != null && !action) {
                int value = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.cacs);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && ennemy != null && !action) {
                int value = Funcion.getInstance().attackIfPossible(this.fight, this.fighter, this.cacs);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0 && !action) {
                nearestEnnemy = Funcion.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 20);//2 = po min 1 + 1;
                Funcion.getInstance().moveNearIfPossible(this.fight, this.fighter, nearestEnnemy);
                time = 1000;
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}