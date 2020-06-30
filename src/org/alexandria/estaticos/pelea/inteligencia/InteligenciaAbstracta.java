package org.alexandria.estaticos.pelea.inteligencia;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class InteligenciaAbstracta implements Inteligencia {

    private final ScheduledExecutorService executor;

    protected Pelea fight;
    protected Peleador fighter;
    protected boolean stop;
    protected byte count;

    public InteligenciaAbstracta(Pelea fight, Peleador fighter, byte count) {
        this.fight = fight;
        this.fighter = fighter;
        this.count = count;
        this.executor = Executors.newSingleThreadScheduledExecutor( r -> {
            Thread thread = new Thread(r);
            thread.setName(InteligenciaAbstracta.class.getName());
            return thread;
        });
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

    public void endTurn() {
        if (this.stop && !this.fighter.isDead()) {
            if (this.fighter.haveInvocation()) {
                this.addNext(() -> {
                    this.fight.endTurn(false, this.fighter);
                    this.executor.shutdownNow();
                }, 1000);
            } else {
                this.fight.endTurn(false,this.fighter);
                this.executor.shutdownNow();
            }
        } else {
            if(!this.fight.isFinish())
                this.addNext(this::endTurn, 500);
            else
                this.executor.shutdownNow();
        }
    }

    protected void decrementCount() {
        this.count--;
        this.apply();
    }

    public void addNext(Runnable runnable, Integer time) { executor.schedule(runnable,time,TimeUnit.MILLISECONDS); }
}
