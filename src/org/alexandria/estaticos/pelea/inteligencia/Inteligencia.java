package org.alexandria.estaticos.pelea.inteligencia;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;

public interface Inteligencia {

    Pelea getFight();
    Peleador getFighter();
    boolean isStop();
    void setStop(boolean stop);
    void addNext(Runnable var1, Integer var2);
    void apply();
    void endTurn();
}
