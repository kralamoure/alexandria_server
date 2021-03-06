package org.alexandria.estaticos.pelea.inteligencia;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.otro.utilidad.Temporizador;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public abstract class InteligenciaAbstracta implements Inteligencia {

    static class DaemonFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    }

    ThreadFactory tf = new DaemonFactory();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(tf);

    protected Pelea fight;
    protected Peleador fighter;
    protected boolean stop;
    protected byte count;
    protected Thread thread;

    public InteligenciaAbstracta(Pelea fight, Peleador fighter, byte count) {
        this.fight = fight;
        this.fighter = fighter;
        this.count = count;
    }

    public Pelea getFight() {
        return fight;
    }

    public Peleador getFighter() {
        return fighter;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    @Override
    public void endTurn() {
        if (this.stop && !this.fighter.isDead()) {
            if (this.fighter.haveInvocation()) {
                this.addNext(() -> {
                            this.fight.endTurn(false, this.fighter);
                            this.executor.shutdownNow();
                        }
                        , 1000);
            } else {
                Temporizador.addSiguiente(() -> {
                            this.fight.endTurn(false, this.fighter);
                        }
                        , 250);

                this.executor.shutdownNow();
            }
        } else if (!this.fight.isFinish()) {
            this.addNext(this::endTurn, 1000);
        } else {
            this.executor.shutdownNow();
        }
    }

    protected void decrementCount() {
        this.count = (byte)(this.count - 1);
        try {
            this.apply();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addNext(Runnable runnable, Integer time) {
        this.executor.schedule(Temporizador.catchRunnable(runnable), (long) time, TimeUnit.MILLISECONDS);
    }

    }
