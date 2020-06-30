package org.alexandria.estaticos.pelea.trampas;

import org.alexandria.comunes.Camino;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Constantes;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo.SortStats;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;

import java.util.ArrayList;

public class Trampas {

    private final Peleador lanzador;
    private final GameCase celda;
    private final byte size;
    private final int hechizo;
    private final SortStats trampahechizo;
    private final Pelea pelea;
    private final int color;
    private boolean isUnHide = true;
    private int teamUnHide = -1;

    public Trampas(Pelea pelea, Peleador lanzador, GameCase celda, byte size, SortStats trampahechizo, int hechizo) {
        this.pelea = pelea;
        this.lanzador = lanzador;
        this.celda = celda;
        this.hechizo = hechizo;
        this.size = size;
        this.trampahechizo = trampahechizo;
        this.color = Constantes.getTrapsColor(hechizo);
    }

    public GameCase getCelda() {
        return this.celda;
    }

    public byte getSize() {
        return this.size;
    }

    public Peleador getLanzador() {
        return this.lanzador;
    }

    public void setIsUnHide(Peleador f) {
        this.isUnHide = true;
        this.teamUnHide = f.getTeam();
    }

    public int getColor() {
        return this.color;
    }

    public void desaparecer() {
        StringBuilder str = new StringBuilder();
        StringBuilder str2 = new StringBuilder();
        StringBuilder str3 = new StringBuilder();
        StringBuilder str4 = new StringBuilder();

        int team = this.lanzador.getTeam() + 1;
        str.append("GDZ-").append(this.celda.getId()).append(";").append(this.size).append(";").append(this.color);
        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this.pelea, team, 999, this.lanzador.getId()
                + "", str.toString());
        str2.append("GDC").append(this.celda.getId());
        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this.pelea, team, 999, this.lanzador.getId()
                + "", str2.toString());
        if (this.isUnHide) {
            int team2 = this.teamUnHide + 1;
            str3.append("GDZ-").append(this.celda.getId()).append(";").append(this.size).append(";").append(this.color);
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this.pelea, team2, 999, this.lanzador.getId()
                    + "", str3.toString());
            str4.append("GDC").append(this.celda.getId());
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this.pelea, team2, 999, this.lanzador.getId() + "", str4.toString());
        }
    }

    public void aparecer(Peleador f) {
        StringBuilder str = new StringBuilder();
        StringBuilder str2 = new StringBuilder();

        int team = f.getTeam() + 1;
        str.append("GDZ+").append(this.celda.getId()).append(";").append(this.size).append(";").append(this.color);
        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this.pelea, team, 999, this.lanzador.getId()
                + "", str.toString());
        str2.append("GDC").append(this.celda.getId()).append(";Haaaaaaaaz3005;");
        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this.pelea, team, 999, this.lanzador.getId()
                + "", str2.toString());
    }

    public void onTrampa(Peleador target) {
        if (target.isDead()) return;
        this.pelea.getAllTraps().remove(this);//On efface le pieges
        desaparecer();//On d�clenche ses effets

        //String a StringBuilder
        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this.pelea, 7, 306, target.getId() + "", String.valueOf(this.hechizo) + ',' + this.celda.getId() + ',' + trampahechizo.getSpriteInfos() + ',' + this.lanzador.getId());

        ArrayList<GameCase> cells = new ArrayList<>();
        cells.add(this.celda);
        //on ajoute les cases
        for (int a = 0; a < this.size; a++) {
            char[] dirs = {'b', 'd', 'f', 'h'};
            //on �vite les modifications concurrentes
            ArrayList<GameCase> cases2 = new ArrayList<>(cells);
            for (GameCase aCell : cases2) {
                if(aCell == null) continue;
                for (char d : dirs) {
                    GameCase cell = this.pelea.getMap().getCase(Camino.GetCaseIDFromDirrection(aCell.getId(), d, this.pelea.getMap(), true));
                    if (cell == null)
                        continue;
                    if (!cells.contains(cell))
                        cells.add(cell);
                }
            }
        }
        Peleador fakeCaster;
        if (this.lanzador.getPlayer() == null)
            fakeCaster = new Peleador(this.pelea, this.lanzador.getMob());
        else
            fakeCaster = new Peleador(this.pelea, this.lanzador.getPlayer());
        fakeCaster.setCell(this.celda);
        this.trampahechizo.applySpellEffectToFight(this.pelea, fakeCaster, target.getCell(), cells, false);
        this.pelea.verifIfTeamAllDead();
    }
}