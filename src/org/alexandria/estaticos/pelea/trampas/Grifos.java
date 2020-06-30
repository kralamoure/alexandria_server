package org.alexandria.estaticos.pelea.trampas;

import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Constantes;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.pelea.hechizo.Hechizo.SortStats;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;

public class Grifos {

    private final Peleador lanzador;
    private final GameCase celda;
    private final byte size;
    private final int hechizo;
    private final SortStats grifoHechizo;
    private byte duracion;
    private final Pelea pelea;
    private final int color;

    public Grifos(Pelea fight, Peleador lanzador, GameCase celda, byte size, SortStats grifoHechizo, byte duracion, int hechizo) {
        this.pelea = fight;
        this.lanzador = lanzador;
        this.celda = celda;
        this.hechizo = hechizo;
        this.size = size;
        this.grifoHechizo = grifoHechizo;
        this.duracion = duracion;
        this.color = Constantes.getGlyphColor(hechizo);
    }

    public Peleador getLanzador() {
        return this.lanzador;
    }

    public GameCase getCelda() {
        return this.celda;
    }

    public byte getSize() {
        return this.size;
    }

    public int getHechizo() {
        return this.hechizo;
    }

    public int decrementDuration() {
        //if(this.duration == -1) return -1;
        this.duracion--;
        return this.duracion;
    }

    public int getColor() {
        return this.color;
    }

    public void onGrifo(Peleador target) {
        if(this.hechizo == 3500 || this.hechizo == 3501) {//glyph pair/impair
            if(target.getMob() != null) {
                if(target.getMob().getTemplate().getId() == 1045) {
                    if(this.hechizo == 3500) {
                        target.addBuff(217, 400, duracion, 1, false, 1077, "", target, true);// - 400 air
                        target.addBuff(218, 400, duracion, 1, false, 1077, "", target, true);// - 400 feu
                        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(pelea, 7, 1077, lanzador.getId() + "", target.getId() + "," + "" + "," + 1);
                        this.pelea.getFighters(7).stream().filter(fighter -> fighter.getPlayer() != null && fighter.getPlayer().isOnline()).forEach(fighter -> {
                            fighter.getPlayer().send("GA;217;-100;" + target.getId() + ",400,1");
                            fighter.getPlayer().send("GA;218;-100;" + target.getId() + ",400,1");
                            fighter.getPlayer().sendMessage("Kimbo entra en el estado PAR.");
                        });
                    } else {
                        target.addBuff(215, 400, duracion, 1, false, 1077, "", target, true);// - 400 terre
                        target.addBuff(216, 400, duracion, 1, false, 1077, "", target, true);// - 400 eau

                        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(pelea, 7, 1077, lanzador.getId() + "", target.getId() + "," + "" + "," + 1);

                        this.pelea.getFighters(7).stream().filter(fighter -> fighter.getPlayer() != null && fighter.getPlayer().isOnline()).forEach(fighter -> {
                            fighter.getPlayer().send("GA;216;-100;" + target.getId() + ",400,1");
                            fighter.getPlayer().send("GA;215;-100;" + target.getId() + ",400,1");
                            fighter.getPlayer().sendMessage("Kimbo entra en el estado IMPAR.");
                        });
                    }
                } else {
                    this.pelea.onFighterDie(target, target);
                }
            } else {
                pelea.onFighterDie(target, target);
            }
        } else {
            Hechizo spell = Mundo.mundo.getSort(this.hechizo);

            for(Integer integer : spell.getEffectTargets())
                if(integer == 2 && target == this.lanzador)
                    return;

            String str = this.hechizo + "," + this.celda.getId() + ", 0, 1, 1," + this.lanzador.getId();
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this.pelea, 7, 307, target.getId() + "", str);
            this.grifoHechizo.applySpellEffectToFight(this.pelea, this.lanzador, target.getCell(), false, true);
            this.pelea.verifIfTeamAllDead();
        }
    }

    public void desaparecer() {
        GestorSalida.GAME_SEND_GDZ_PACKET_TO_FIGHT(this.pelea, 7, "-", this.celda.getId(), this.size, this.color);
        GestorSalida.GAME_SEND_GDC_PACKET_TO_FIGHT(this.pelea, 7, this.celda.getId());
    }
}