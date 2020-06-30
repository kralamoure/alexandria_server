package org.alexandria.estaticos.pelea.inteligencia.tipo;

import org.alexandria.comunes.GestorSalida;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaAbstracta;
import org.alexandria.estaticos.pelea.inteligencia.utilidad.Funcion;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA18 extends InteligenciaAbstracta {

    public IA18(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            Peleador kimbo = null;
            boolean kPair = false, kImpair = false, dPair = false, dImpair = false;

            if(this.fighter.haveState(29)) dImpair = true;
            if(this.fighter.haveState(30)) dPair = true;

            for(Peleador fighter : this.fight.getTeam1().values()) {
                if(fighter.getMob() !=  null) {
                    System.out.println(fighter.getMob().getTemplate().getId());
                    if(fighter.getMob().getTemplate().getId() == 1045) {
                        if(fighter.haveState(30)) {
                            fighter.setState(30, 0, fighter.getId());
                            kPair = true;
                            this.fighter.setState(30, 1, fighter.getId());
                        }
                        if(fighter.haveState(29)) {
                            fighter.setState(29, 0, fighter.getId());
                            kImpair = true;
                            this.fighter.setState(29, 1, fighter.getId());
                        }
                        kimbo = fighter;
                    }
                }
            }

            if(kimbo == null) {
                for (Peleador fighter : this.fight.getTeam0().values()) {
                    if (fighter.getMob() != null) {
                        System.out.println(fighter.getMob().getTemplate().getId());
                        if (fighter.getMob().getTemplate().getId() == 1045) {
                            if (fighter.haveState(30)) {
                                fighter.setState(30, 0, fighter.getId());
                                kPair = true;
                                this.fighter.setState(30, 1, fighter.getId());
                            }
                            if (fighter.haveState(29)) {
                                fighter.setState(29, 0, fighter.getId());
                                kImpair = true;
                                this.fighter.setState(29, 1, fighter.getId());
                            }
                            kimbo = fighter;
                        }
                    }
                }
            }

            if(kImpair && dImpair) {
                this.fighter.setState(29, 0, fighter.getId());
                int attack = Funcion.getInstance().attackIfPossibleDisciplepair(this.fight, this.fighter, kimbo);

                if (attack != 0) {
                    this.fight.getAllGlyphs().stream().filter(glyph -> glyph.getCelda().getId() == this.fighter.getCell().getId()).forEach(glyph -> {
                        this.fighter.addBuff(128, 1, 1, 1, true, 3500, "", this.fighter, true);
                        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, 78, this.fighter.getId() + "", this.fighter.getId() + "," + "" + "," + 1);
                    });
                    Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter);
                }
            } else if(kPair && dPair) {
                this.fighter.setState(30, 0, fighter.getId());
                int attack = Funcion.getInstance().attackIfPossibleDiscipleimpair(this.fight, this.fighter, kimbo);

                if (attack != 0) {
                    this.fight.getAllGlyphs().stream().filter(entry -> entry.getCelda().getId() == this.fighter.getCell().getId()).forEach(entry -> {
                        this.fighter.addBuff(128, 1, 1, 1, true, 3500, "", this.fighter, true);
                        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, 78, this.fighter.getId() + "", this.fighter.getId() + "," + "" + "," + 1);
                    });
                    Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter);
                }
            } else if(kPair) {
                int attack = Funcion.getInstance().attackIfPossibleDisciplepair(this.fight, this.fighter, kimbo);

                if (attack != 0) {
                    this.fight.getAllGlyphs().stream().filter(entry -> entry.getCelda().getId() == this.fighter.getCell().getId()).forEach(entry -> {
                        this.fighter.addBuff(128, 1, 1, 1, true, 3500, "", this.fighter, true);
                        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, 78, this.fighter.getId() + "", this.fighter.getId() + "," + "" + "," + 1);
                    });
                    Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter);
                }
            } else if(kImpair) {
                int attack = Funcion.getInstance().attackIfPossibleDiscipleimpair(this.fight, this.fighter, kimbo);

                if (attack != 0) {
                    this.fight.getAllGlyphs().stream().filter(entry -> entry.getCelda().getId() == this.fighter.getCell().getId()).forEach(entry -> {
                        this.fighter.addBuff(128, 1, 1, 1, true, 3500, "", this.fighter, true);
                        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight, 7, 78, this.fighter.getId() + "", this.fighter.getId() + "," + "" + "," + 1);
                    });
                    Funcion.getInstance().moveFarIfPossible(this.fight, this.fighter);
                }
            }else {
                this.stop = true;
            }

            addNext(this::decrementCount, 1000);
        } else {
            this.stop = true;
        }
    }
}