package org.alexandria.estaticos.oficio;

import org.alexandria.estaticos.area.mapa.Mapa.GameCase;
import org.alexandria.estaticos.area.mapa.Mapa.ObjetosInteractivos;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.estaticos.juego.accion.AccionJuego;
import org.alexandria.estaticos.juego.mundo.Mundo;

import java.util.ArrayList;

public class OficioCaracteristicas {

    private final int id;
    private final Oficio template;
    private int lvl;
    private long xp;
    private ArrayList<OficioAccion> posActions = new ArrayList<>();
    private boolean isCheap = false;
    private boolean freeOnFails = false;
    private boolean noRessource = false;
    private OficioAccion curAction;
    private int slotsPublic, position;

    public OficioCaracteristicas(int id, Oficio tp, int lvl, long xp) {
        this.id = id;
        this.template = tp;
        this.lvl = lvl;
        this.xp = xp;
        this.posActions = OficioConstantes.getPosActionsToJob(tp.getId(), lvl);
    }

    public int getId() {
        return this.id;
    }

    public Oficio getTemplate() {
        return this.template;
    }

    public int get_lvl() {
        return this.lvl;
    }

    public long getXp() {
        return this.xp;
    }

    public int getSlotsPublic() {
        return this.slotsPublic;
    }

    public void setSlotsPublic(int slots) {
        this.slotsPublic = slots;
    }

    public int getPosition() {
        return this.position;
    }

    public OficioAccion getJobActionBySkill(int skill) {
        for (OficioAccion JA : this.posActions)
            if (JA.getId() == skill)
                return JA;
        return null;
    }

    public void startAction(int id, Jugador P, ObjetosInteractivos IO, AccionJuego GA, GameCase cell) {
        for (OficioAccion JA : this.posActions) {
            if (JA.getId() == id) {
                this.curAction = JA;
                JA.startAction(P, IO, GA, cell, this);
                return;
            }
        }
    }

    public void endAction(Jugador P, ObjetosInteractivos IO, AccionJuego GA, GameCase cell) {
        if (this.curAction == null)
            return;

        this.curAction.endAction(P, IO, GA, cell);
        this.curAction = null;
        ArrayList<OficioCaracteristicas> list = new ArrayList<>();
        list.add(this);
        GestorSalida.GAME_SEND_JX_PACKET(P, list);
    }

    public void addXp(Jugador P, long xp) {
        if (this.lvl > 99)
            return;
        int exLvl = this.lvl;
        this.xp += xp;

        while (this.xp >= Mundo.mundo.getExpLevel(this.lvl + 1).metier && this.lvl < 100)
            levelUp(P, false);

        if (this.lvl > exLvl && P.isOnline()) {
            ArrayList<OficioCaracteristicas> list = new ArrayList<>();
            list.add(this);

            GestorSalida.GAME_SEND_JS_PACKET(P, list);
            GestorSalida.GAME_SEND_JN_PACKET(P, this.template.getId(), this.lvl);
            GestorSalida.GAME_SEND_STATS_PACKET(P);
            GestorSalida.GAME_SEND_Ow_PACKET(P);
            GestorSalida.GAME_SEND_JO_PACKET(P, list);
        }
    }

    public String getXpString(String s) {
        return Mundo.mundo.getExpLevel(this.lvl).metier + s + this.xp + s + Mundo.mundo.getExpLevel((this.lvl < 100 ? this.lvl + 1 : this.lvl)).metier;
    }

    public void levelUp(Jugador P, boolean send) {
        this.lvl++;
        this.posActions = OficioConstantes.getPosActionsToJob(this.template.getId(), this.lvl);

        if (send) {
            //on cr�er la listes des JobStats a envoyer (Seulement celle ci)
            ArrayList<OficioCaracteristicas> list = new ArrayList<>();
            list.add(this);
            GestorSalida.GAME_SEND_JS_PACKET(P, list);
            GestorSalida.GAME_SEND_STATS_PACKET(P);
            GestorSalida.GAME_SEND_Ow_PACKET(P);
            GestorSalida.GAME_SEND_JN_PACKET(P, this.template.getId(), this.lvl);
            GestorSalida.GAME_SEND_JO_PACKET(P, list);
        }
    }

    public String parseJS() {
        StringBuilder str = new StringBuilder();
        str.append("|").append(this.template.getId()).append(";");
        boolean first = true;
        for (OficioAccion JA : this.posActions) {
            if (!first)
                str.append(",");
            else
                first = false;
            str.append(JA.getId()).append("~").append(JA.getMin()).append("~");
            if (JA.isCraft())
                str.append("0~0~").append(JA.getChance());
            else
                str.append(JA.getMax()).append("~0~").append(JA.getTime());
        }
        return str.toString();
    }

    public int getOptBinValue() {
        int nbr = 0;
        nbr += (this.isCheap ? 1 : 0);
        nbr += (this.freeOnFails ? 2 : 0);
        nbr += (this.noRessource ? 4 : 0);
        return nbr;
    }

    public void setOptBinValue(int bin) {
        this.isCheap = false;
        this.freeOnFails = false;
        this.noRessource = false;
        this.noRessource = (bin & 4) == 4;
        this.freeOnFails = (bin & 2) == 2;
        this.isCheap = (bin & 1) == 1;
    }

    public boolean isValidMapAction(int id) {
        for (OficioAccion JA : this.posActions)
            if (JA.getId() == id)
                return true;
        return false;
    }
}
