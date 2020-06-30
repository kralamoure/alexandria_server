package org.alexandria.estaticos.evento;

import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;

public interface IEvento {

    void prepare();
    void perform();
    void execute();
    void close();

    boolean onReceivePacket(GestorEvento manager, Jugador player, String packet) throws Exception;
    GameCase getEmptyCellForPlayer(Jugador player);
}
