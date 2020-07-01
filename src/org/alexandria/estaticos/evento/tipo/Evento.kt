package org.alexandria.estaticos.evento.tipo;

import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.estaticos.evento.IEvento;
import org.alexandria.estaticos.evento.RecompensaEvento;

import java.time.Instant;

public abstract class Evento extends Thread implements IEvento {

    protected final byte id, maxPlayers;
    protected final String name;
    protected final String description;
    protected Mapa map;
    protected RecompensaEvento[] first, second, third;

    public Evento(byte id, byte maxPlayers, String name, String description, RecompensaEvento[] first) {
        super.setName("Event-" + name);
        super.setDaemon(true);
        super.start();
        this.id = id;
        this.maxPlayers = maxPlayers;
        this.name = name;
        this.description = description;
        this.first = first;
    }

    public Evento(byte id, byte maxPlayers, String name, String description, RecompensaEvento[] first, RecompensaEvento[] second, RecompensaEvento[] third) {
        this(id, maxPlayers, name, description, first);
        this.second = second;
        this.third = third;
    }

    public byte getEventId() {
        return id;
    }

    public byte getMaxPlayers() {
        return maxPlayers;
    }

    public String getEventName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Mapa getMap() {
        return map;
    }

    public RecompensaEvento[] getFirst() {
        return first;
    }

    public RecompensaEvento[] getSecond() {
        return second;
    }

    public RecompensaEvento[] getThird() {
        return third;
    }

    public static void wait(int time) {
        long newTime = Instant.now().toEpochMilli() + time;

        while (Instant.now().toEpochMilli() < newTime) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void kickPlayer(Jugador player);
}
