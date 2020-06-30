package org.alexandria.estaticos.evento.tipo;

import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.estaticos.Npc;
import org.alexandria.estaticos.evento.GestorEvento;
import org.alexandria.estaticos.evento.RecompensaEvento;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;

import java.util.ArrayList;
import java.util.List;


public class EventoEncontrarme extends Evento {

    private final static List<FindMeRow> findMeRows = new ArrayList<>();

    public EventoEncontrarme(byte id, byte maxPlayers, String name, String description, RecompensaEvento[] first) {
        super(id, maxPlayers, name, description, first);
    }

    @Override
    public void prepare() {
        Npc animator = this.map.addNpc(16000, (short) 221, 1);

    }

    @Override
    public void perform() {

    }

    @Override
    public void execute() {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean onReceivePacket(GestorEvento manager, Jugador player, String packet) throws Exception {
        return false;
    }

    @Override
    public GameCase getEmptyCellForPlayer(Jugador player) {
        return null;
    }

    @Override
    public void kickPlayer(Jugador player) {

    }

    public static class FindMeRow {
        private final short map;
        private final short cell;
        private final String[] indices;
        private byte actual = 0;

        public FindMeRow(short map, short cell, String[] indices) {
            this.map = map;
            this.cell = cell;
            this.indices = indices;
            EventoEncontrarme.findMeRows.add(this);
        }

        public short getMap() {
            return map;
        }

        public short getCell() {
            return cell;
        }

        public String getNextIndice() {
            if(this.actual > this.indices.length - 1) return null;
            String indice = this.indices[this.actual];
            this.actual++;
            return indice;
        }
    }
}
