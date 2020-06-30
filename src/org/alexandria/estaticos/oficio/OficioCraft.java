package org.alexandria.estaticos.oficio;

import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.otro.utilidad.Temporizador;

import java.util.concurrent.TimeUnit;

public class OficioCraft {

    private final OficioAccion jobAction;
    private int time = 0;
    private boolean itsOk = true;

    OficioCraft(OficioAccion jobAction, Jugador player) {
        this.jobAction=jobAction;

        Temporizador.addSiguiente(() -> {
            if (itsOk) jobAction.craft(false);
        }, Configuracion.INSTANCE.getOficiosDelay(), TimeUnit.MILLISECONDS, Temporizador.DataType.MAPA);
        Temporizador.addSiguiente(() -> {
            if (!itsOk) repeat(time, time, player);
        }, Configuracion.INSTANCE.getOficiosDelay(), TimeUnit.MILLISECONDS, Temporizador.DataType.MAPA);
    }

    public OficioAccion getJobAction() {
        return jobAction;
    }

    public void setAction(int time) {
        this.time = time;
        this.jobAction.broken = false;
        this.itsOk = false;
    }

    private void repeat(final int time1, final int time2, final Jugador player) {
        final int j = time1 - time2;
        this.jobAction.player = player;
        this.jobAction.isRepeat = true;

        if (!this.check(player, j, time2) || time2 <= 0) {
            this.end();
        } else {
            Temporizador.addSiguiente(() -> this.repeat(time1, (time2 - 1), player), Configuracion.INSTANCE.getOficiosDelay(), TimeUnit.MILLISECONDS, Temporizador.DataType.MAPA);
        }
    }

    private boolean check(final Jugador player, int j, int time2) {
        if (this.jobAction.broke || this.jobAction.broken || player.getExchangeAction() == null || !player.isOnline()) {
            if (player.getExchangeAction() == null)
                this.jobAction.broken = true;
            if (player.isOnline())
                GestorSalida.GAME_SEND_Ea_PACKET(this.jobAction.player, this.jobAction.broken ? "2" : "4");
            return false;
        } else {
            GestorSalida.GAME_SEND_EA_PACKET(this.jobAction.player, String.valueOf(time2));
            this.jobAction.craft(this.jobAction.isRepeat);
            this.jobAction.ingredients.clear();
            this.jobAction.ingredients.putAll(this.jobAction.lastCraft);
            return true;
        }
    }

    //Objetos desaparecen - solucion
    private void end() {
        GestorSalida.GAME_SEND_Ea_PACKET(this.jobAction.player,"1");
        if(!(this.jobAction.getId()==1||this.jobAction.getId()==113||this.jobAction.getId()==115||this.jobAction.getId()==116||this.jobAction.getId()==117||this.jobAction.getId()==118||this.jobAction.getId()==119||this.jobAction.getId()==120||(this.jobAction.getId()>=163&&this.jobAction.getId()<=169)))
            this.jobAction.ingredients.clear();
        if(!this.jobAction.data.isEmpty())
            GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.jobAction.player,'O',"+",this.jobAction.data);
        this.jobAction.isRepeat=false;
        this.jobAction.setJobCraft(null);

        if(this.jobAction.player.getObjetoInteractivo()!=null)
        {
            this.jobAction.player.getObjetoInteractivo().getPrimero().setState(OficioConstantes.IOBJECT_STATE_FULL);
            GestorSalida.GAME_SEND_GDF_PACKET_TO_MAP(this.jobAction.player.getCurMap(),this.jobAction.player.getObjetoInteractivo().getSegundo());
        }
    }
}