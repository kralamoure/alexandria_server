package org.alexandria.otro.utilidad;

import org.alexandria.estaticos.juego.mundo.Mundo;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Temporizador {

    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);

    public static ScheduledFuture<?> addSiguiente(Runnable run, long time, TimeUnit unit) {
        return scheduler.schedule(Temporizador.catchRunnable(run), time, unit);
    }

    public static ScheduledFuture<?> addSiguiente(Runnable run, long time) {
        return Temporizador.addSiguiente(run, time, TimeUnit.MILLISECONDS);
    }

    public static void addSiguiente(Runnable run, long time, TimeUnit unit, DataType scheduler) {
        Temporizador.scheduler.schedule(run, time, unit);
    }

    public static void addSiguiente(Runnable run, long time, DataType scheduler) {
        Temporizador.addSiguiente(run, time, TimeUnit.MILLISECONDS, scheduler);
    }

    public static void update() { }

    private static int getNumberOfThread() {
        int fight = Temporizador.getNumberOfFight();
        int player = Mundo.mundo.getOnlinePlayers().size();
        return (fight + player) / 30;
    }

    private static int getNumberOfFight() {
        int[] fights = new int[]{0};
        Mundo.mundo.getMapa().forEach(map -> {
                    int[] arrn = fights;
                    arrn[0] = arrn[0] + map.getFights().size();
                }
        );
        return fights[0];
    }

    public static Runnable catchRunnable(Runnable run) {
        return () -> {
            try {
                run.run();
            }
            catch (Exception e) {
                e.printStackTrace();
                System.err.println(e.getCause().getMessage());
            }
        }
                ;
    }

    public enum DataType {
        MAPA,
        CLIENTE,
        PELEA
    }
}
