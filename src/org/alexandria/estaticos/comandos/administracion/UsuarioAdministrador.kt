package org.alexandria.estaticos.comandos.administracion;

import org.alexandria.estaticos.cliente.Cuenta;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.MainServidor;
import org.alexandria.estaticos.juego.JuegoCliente;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class UsuarioAdministrador {

    private final Cuenta cuenta;
    private final Jugador jugador;
    private final JuegoCliente cliente;

    private boolean timerStart = false;
    private Timer timer;

    public UsuarioAdministrador(Jugador jugador) {
        this.cuenta = jugador.getAccount();
        this.jugador = jugador;
        this.cliente = jugador.getAccount().getGameClient();
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public JuegoCliente getCliente() {
        return cliente;
    }

    public boolean isTimerStart() {
        return timerStart;
    }

    public void setTimerStart(boolean timerStart) {
        this.timerStart = timerStart;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public Timer createTimer(final int timer) {
        ActionListener action = new ActionListener() {
            int time = timer;

            public void actionPerformed(ActionEvent event) {
                time = time - 1;
                if (time == 1)
                    GestorSalida.GAME_SEND_Im_PACKET_TO_ALL("115;" + time + " minute");
                else
                    GestorSalida.GAME_SEND_Im_PACKET_TO_ALL("115;" + time + " minutes");
                if (time <= 0) MainServidor.INSTANCE.stop("Shutdown by an administrator");
            }
        };
        return new Timer(60000, action);
    }

    public void sendMessage(String message) {
        this.jugador.send("BAT0" + message);
    }

    public void sendErrorMessage(String message) {
        this.jugador.send("BAT1" + message);
    }

    public void sendSuccessMessage(String message) {
        this.jugador.send("BAT2" + message);
    }

    public abstract void apply(String packet);
}