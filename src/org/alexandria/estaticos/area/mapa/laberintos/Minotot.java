package org.alexandria.estaticos.area.mapa.laberintos;

import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.otro.utilidad.Temporizador;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;

import java.util.concurrent.TimeUnit;

public class Minotot {

    private static final long time = 10;
    private static short demi = -1;
    private static short momi = -1;

    public static void initialize() {
        closeAll();
        initializeBoss();
    }

    public static void demi() {
        Temporizador.addSiguiente(Minotot::spawnDemi, 10, TimeUnit.MINUTES, Temporizador.DataType.MAPA);
    }

    public static void momi() {
        Temporizador.addSiguiente(Minotot::spawnDemi, 10, TimeUnit.MINUTES, Temporizador.DataType.MAPA);
    }

    private static void initializeBoss() {
        spawnDemi();
        spawnMomi();
    }

    private static void spawnDemi() {
        demi = chooseRandomMap();
        Mundo.mundo.getMap(demi).spawnGroupWith(Mundo.mundo.getMonstre(832));
        Mundo.mundo.logger.trace("   > The 'Deminoboule' was added on the map id " + demi + ".");
    }

    private static void spawnMomi() {
        momi = chooseRandomMap();
        Mundo.mundo.getMap(momi).spawnGroupWith(Mundo.mundo.getMonstre(831));
        Mundo.mundo.logger.trace("   > The 'Minotoror' was added on the map id " + momi + ".");
    }

    private static short chooseRandomMap() {
        short map = switch (Formulas.getRandomValue(0, 7)) {
            case 0 -> (short) 9575;
            case 1 -> (short) 9576;
            case 2 -> (short) 9577;
            case 3 -> (short) 9556;
            case 4 -> (short) 9560;
            case 5 -> (short) 9561;
            case 6 -> (short) 9562;
            case 7 -> (short) 9563;
            default -> (short) 0;
        };
        if((demi != -1 && map == demi) || (momi != -1 && map == momi)) return chooseRandomMap();
        return map;
    }

    public static void ouvrirHaut(Mapa map) {
        closeAll();
        switch (map.getId()) {
// 11ï¿½me
            case 9555 -> {
                openTimer(Mundo.mundo.getMap((short) 9574), 51); // 6ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9553), 428); // 1ï¿½re
            }
// 12ï¿½me
            case 9556 -> {
                openTimer(Mundo.mundo.getMap((short) 9575), 94); // 7ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9564), 429); // 2ï¿½me
            }
// 13ï¿½me
            case 9557 -> {
                openTimer(Mundo.mundo.getMap((short) 9576), 67); // 8ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9571), 428); // 3ï¿½me
            }
// 14ï¿½me
            case 9558 -> {
                openTimer(Mundo.mundo.getMap((short) 9577), 50); // 9ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9572), 443); // 4ï¿½me
            }
// 15ï¿½me
            case 9559 -> {
                openTimer(Mundo.mundo.getMap((short) 9554), 138); // 10ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9573), 440); // 5ï¿½me
            }
// 16ï¿½me
            case 9560 -> {
                openTimer(Mundo.mundo.getMap((short) 9555), 79); // 11ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9574), 425); // 6ï¿½me
            }
// 17ï¿½me
            case 9561 -> {
                openTimer(Mundo.mundo.getMap((short) 9556), 64); // 12ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9575), 427); // 7ï¿½me
            }
// 18ï¿½me
            case 9562 -> {
                openTimer(Mundo.mundo.getMap((short) 9557), 51); // 13ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9576), 441); // 8ï¿½me
            }
// 19ï¿½me
            case 9563 -> {
                openTimer(Mundo.mundo.getMap((short) 9558), 77); // 14ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9577), 426); // 9ï¿½me
            }
// 20ï¿½me
            case 9565 -> {
                openTimer(Mundo.mundo.getMap((short) 9559), 65); // 15ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9554), 431); // 10ï¿½me
            }
// 21ï¿½me
            case 9566 -> {
                openTimer(Mundo.mundo.getMap((short) 9560), 52); // 16ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9555), 440); // 11ï¿½me
            }
// 22ï¿½me
            case 9567 -> {
                openTimer(Mundo.mundo.getMap((short) 9561), 80); // 17ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9556), 428); // 12ï¿½me
            }
// 23ï¿½me
            case 9568 -> {
                openTimer(Mundo.mundo.getMap((short) 9562), 52); // 18ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9557), 413); // 13ï¿½me
            }
// 24ï¿½me
            case 9569 -> {
                openTimer(Mundo.mundo.getMap((short) 9563), 52); // 19ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9558), 427); // 14ï¿½me
            }
// 25ï¿½me
            case 9570 -> {
                openTimer(Mundo.mundo.getMap((short) 9565), 51); // 20ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9559), 427); // 15ï¿½me
            }
        }
    }

    public static void ouvrirBas(Mapa map) {
        closeAll();
        switch (map.getId()) {
// 1ï¿½re
            case 9553 -> {
                openTimer(Mundo.mundo.getMap((short) 9574), 425); // 6ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9555), 79); // 11ï¿½me
            }
// 2ï¿½me
            case 9564 -> {
                openTimer(Mundo.mundo.getMap((short) 9575), 427); // 7ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9556), 64); // 12ï¿½me
            }
// 3ï¿½me
            case 9571 -> {
                openTimer(Mundo.mundo.getMap((short) 9576), 441); // 8ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9557), 51); // 13ï¿½me
            }
// 4ï¿½me
            case 9572 -> {
                openTimer(Mundo.mundo.getMap((short) 9577), 426); // 9ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9558), 77); // 14ï¿½me
            }
// 5ï¿½me
            case 9573 -> {
                openTimer(Mundo.mundo.getMap((short) 9554), 431); // 10ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9559), 65); // 15ï¿½me
            }
// 6ï¿½me
            case 9574 -> {
                openTimer(Mundo.mundo.getMap((short) 9555), 440); // 11ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9560), 52); // 16ï¿½me
            }
// 7ï¿½me
            case 9575 -> {
                openTimer(Mundo.mundo.getMap((short) 9556), 428); // 12ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9561), 80); // 17ï¿½me
            }
// 8ï¿½me
            case 9576 -> {
                openTimer(Mundo.mundo.getMap((short) 9557), 413); // 13ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9562), 52); // 18ï¿½me
            }
// 9ï¿½me
            case 9577 -> {
                openTimer(Mundo.mundo.getMap((short) 9558), 427); // 14ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9563), 52); // 19ï¿½me
            }
// 10ï¿½me
            case 9554 -> {
                openTimer(Mundo.mundo.getMap((short) 9559), 427); // 15ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9565), 51); // 20ï¿½me
            }
// 11ï¿½me
            case 9555 -> {
                openTimer(Mundo.mundo.getMap((short) 9560), 428); // 16ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9566), 51); // 21ï¿½me
            }
// 12ï¿½me
            case 9556 -> {
                openTimer(Mundo.mundo.getMap((short) 9561), 441); // 17ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9567), 37); // 22ï¿½me
            }
// 13ï¿½me
            case 9557 -> {
                openTimer(Mundo.mundo.getMap((short) 9562), 429); // 18ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9568), 51); // 23ï¿½me
            }
// 14ï¿½me
            case 9558 -> {
                openTimer(Mundo.mundo.getMap((short) 9563), 429); // 19ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9569), 64); // 24ï¿½me
            }
// 15ï¿½me
            case 9559 -> {
                openTimer(Mundo.mundo.getMap((short) 9565), 414); // 20ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9570), 51); // 25ï¿½me
            }
        }
    }

    public static void ouvrirGauche(Mapa map) {
        closeAll();
        switch (map.getId()) {
// 3ï¿½me
            case 9571 -> {
                openTimer(Mundo.mundo.getMap((short) 9564), 335); // 2ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9553), 288); // 1ï¿½re
            }
// 4ï¿½me
            case 9572 -> {
                openTimer(Mundo.mundo.getMap((short) 9571), 277); // 3ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9564), 259); // 2ï¿½me
            }
// 5ï¿½me
            case 9573 -> {
                openTimer(Mundo.mundo.getMap((short) 9572), 263); // 4ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9571), 331); // 3ï¿½me
            }
// 8ï¿½me
            case 9576 -> {
                openTimer(Mundo.mundo.getMap((short) 9575), 335); // 7ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9574), 273); // 6ï¿½me
            }
// 9ï¿½me
            case 9577 -> {
                openTimer(Mundo.mundo.getMap((short) 9576), 306); // 8ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9575), 331); // 7ï¿½me
            }
// 10ï¿½me
            case 9554 -> {
                openTimer(Mundo.mundo.getMap((short) 9577), 364); // 9ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9576), 317); // 8ï¿½me
            }
// 13ï¿½me
            case 9557 -> {
                openTimer(Mundo.mundo.getMap((short) 9556), 306); // 12ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9555), 332); // 11ï¿½me
            }
// 14ï¿½me
            case 9558 -> {
                openTimer(Mundo.mundo.getMap((short) 9557), 306); // 13ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9556), 332); // 12ï¿½me
            }
// 15ï¿½me
            case 9559 -> {
                openTimer(Mundo.mundo.getMap((short) 9558), 277); // 14ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9557), 230); // 13ï¿½me
            }
// 18ï¿½me
            case 9562 -> {
                openTimer(Mundo.mundo.getMap((short) 9561), 306); // 17ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9560), 302); // 16ï¿½me
            }
// 19ï¿½me
            case 9563 -> {
                openTimer(Mundo.mundo.getMap((short) 9562), 320); // 18ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9561), 317); // 17ï¿½me
            }
// 20ï¿½me
            case 9565 -> {
                openTimer(Mundo.mundo.getMap((short) 9563), 292); // 19ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9562), 303); // 18ï¿½me
            }
// 23ï¿½me
            case 9568 -> {
                openTimer(Mundo.mundo.getMap((short) 9567), 277); // 22ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9566), 332); // 21ï¿½me
            }
// 24ï¿½me
            case 9569 -> {
                openTimer(Mundo.mundo.getMap((short) 9568), 277); // 23ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9567), 346); // 22ï¿½me
            }
// 25ï¿½me
            case 9570 -> {
                openTimer(Mundo.mundo.getMap((short) 9569), 291); // 24ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9568), 346); // 23ï¿½me
            }
        }
    }

    public static void ouvrirDroite(Mapa map) {
        closeAll();
        switch (map.getId()) {
// 1ï¿½re
            case 9553 -> {
                openTimer(Mundo.mundo.getMap((short) 9564), 259); // 2ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9571), 277); // 3ï¿½me
            }
// 2ï¿½me
            case 9564 -> {
                openTimer(Mundo.mundo.getMap((short) 9571), 331); // 3ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9572), 263); // 4ï¿½me
            }
// 3ï¿½me
            case 9571 -> {
                openTimer(Mundo.mundo.getMap((short) 9572), 346); // 4ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9573), 219); // 5ï¿½me
            }
// 6ï¿½me
            case 9574 -> {
                openTimer(Mundo.mundo.getMap((short) 9575), 331); // 7ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9576), 306); // 8ï¿½me
            }
// 7ï¿½me
            case 9575 -> {
                openTimer(Mundo.mundo.getMap((short) 9576), 317); // 8ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9577), 364); // 9ï¿½me
            }
// 8ï¿½me
            case 9576 -> {
                openTimer(Mundo.mundo.getMap((short) 9577), 390); // 9ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9554), 306); // 10ï¿½me
            }
// 11ï¿½me
            case 9555 -> {
                openTimer(Mundo.mundo.getMap((short) 9556), 332); // 12ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9557), 306); // 13ï¿½me
            }
// 12ï¿½me
            case 9556 -> {
                openTimer(Mundo.mundo.getMap((short) 9557), 230); // 13ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9558), 277); // 14ï¿½me
            }
// 13ï¿½me
            case 9557 -> {
                openTimer(Mundo.mundo.getMap((short) 9558), 361); // 14ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9559), 277); // 15ï¿½me
            }
// 16ï¿½me
            case 9560 -> {
                openTimer(Mundo.mundo.getMap((short) 9561), 317); // 17ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9562), 320); // 18ï¿½me
            }
// 17ï¿½me
            case 9561 -> {
                openTimer(Mundo.mundo.getMap((short) 9562), 303); // 18ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9563), 292); // 19ï¿½me
            }
// 18ï¿½me
            case 9562 -> {
                openTimer(Mundo.mundo.getMap((short) 9563), 288); // 19ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9565), 262); // 20ï¿½me
            }
// 21ï¿½me
            case 9566 -> {
                openTimer(Mundo.mundo.getMap((short) 9567), 346); // 22ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9568), 277); // 23ï¿½me
            }
// 22ï¿½me
            case 9567 -> {
                openTimer(Mundo.mundo.getMap((short) 9568), 346); // 23ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9569), 291); // 24ï¿½me
            }
// 23ï¿½me
            case 9568 -> {
                openTimer(Mundo.mundo.getMap((short) 9569), 317); // 24ï¿½me
                openTimer(Mundo.mundo.getMap((short) 9570), 306); // 25ï¿½me
            }
        }
    }


    private static void closeAll() {
        close(Mundo.mundo.getMap((short) 9553), (short) 428);
        close(Mundo.mundo.getMap((short) 9553), (short) 288);

        close(Mundo.mundo.getMap((short) 9554), (short) 431);
        close(Mundo.mundo.getMap((short) 9554), (short) 306);
        close(Mundo.mundo.getMap((short) 9554), (short) 138);

        close(Mundo.mundo.getMap((short) 9555), (short) 440);
        close(Mundo.mundo.getMap((short) 9555), (short) 332);
        close(Mundo.mundo.getMap((short) 9555), (short) 79);

        close(Mundo.mundo.getMap((short) 9556), (short) 428);
        close(Mundo.mundo.getMap((short) 9556), (short) 306);
        close(Mundo.mundo.getMap((short) 9556), (short) 332);
        close(Mundo.mundo.getMap((short) 9556), (short) 64);

        close(Mundo.mundo.getMap((short) 9557), (short) 413);
        close(Mundo.mundo.getMap((short) 9557), (short) 306);
        close(Mundo.mundo.getMap((short) 9557), (short) 230);
        close(Mundo.mundo.getMap((short) 9557), (short) 51);

        close(Mundo.mundo.getMap((short) 9558), (short) 427);
        close(Mundo.mundo.getMap((short) 9558), (short) 277);
        close(Mundo.mundo.getMap((short) 9558), (short) 361);
        close(Mundo.mundo.getMap((short) 9558), (short) 77);

        close(Mundo.mundo.getMap((short) 9559), (short) 427);
        close(Mundo.mundo.getMap((short) 9559), (short) 277);
        close(Mundo.mundo.getMap((short) 9559), (short) 65);

        close(Mundo.mundo.getMap((short) 9560), (short) 428);
        close(Mundo.mundo.getMap((short) 9560), (short) 302);
        close(Mundo.mundo.getMap((short) 9560), (short) 52);

        close(Mundo.mundo.getMap((short) 9561), (short) 441);
        close(Mundo.mundo.getMap((short) 9561), (short) 306);
        close(Mundo.mundo.getMap((short) 9561), (short) 317);
        close(Mundo.mundo.getMap((short) 9561), (short) 80);

        close(Mundo.mundo.getMap((short) 9562), (short) 429);
        close(Mundo.mundo.getMap((short) 9562), (short) 320);
        close(Mundo.mundo.getMap((short) 9562), (short) 303);
        close(Mundo.mundo.getMap((short) 9562), (short) 52);

        close(Mundo.mundo.getMap((short) 9563), (short) 429);
        close(Mundo.mundo.getMap((short) 9563), (short) 292);
        close(Mundo.mundo.getMap((short) 9563), (short) 288);
        close(Mundo.mundo.getMap((short) 9563), (short) 52);

        close(Mundo.mundo.getMap((short) 9564), (short) 429);
        close(Mundo.mundo.getMap((short) 9564), (short) 335);
        close(Mundo.mundo.getMap((short) 9564), (short) 259);

        close(Mundo.mundo.getMap((short) 9565), (short) 414);
        close(Mundo.mundo.getMap((short) 9565), (short) 262);
        close(Mundo.mundo.getMap((short) 9565), (short) 51);

        close(Mundo.mundo.getMap((short) 9566), (short) 332);
        close(Mundo.mundo.getMap((short) 9566), (short) 51);

        close(Mundo.mundo.getMap((short) 9567), (short) 277);
        close(Mundo.mundo.getMap((short) 9567), (short) 346);
        close(Mundo.mundo.getMap((short) 9567), (short) 37);

        close(Mundo.mundo.getMap((short) 9568), (short) 277);
        close(Mundo.mundo.getMap((short) 9568), (short) 346);
        close(Mundo.mundo.getMap((short) 9568), (short) 51);

        close(Mundo.mundo.getMap((short) 9569), (short) 291);
        close(Mundo.mundo.getMap((short) 9569), (short) 317);
        close(Mundo.mundo.getMap((short) 9569), (short) 64);

        close(Mundo.mundo.getMap((short) 9570), (short) 306);
        close(Mundo.mundo.getMap((short) 9570), (short) 51);

        close(Mundo.mundo.getMap((short) 9571), (short) 428);
        close(Mundo.mundo.getMap((short) 9571), (short) 277);
        close(Mundo.mundo.getMap((short) 9571), (short) 331);

        close(Mundo.mundo.getMap((short) 9572), (short) 443);
        close(Mundo.mundo.getMap((short) 9572), (short) 263);
        close(Mundo.mundo.getMap((short) 9572), (short) 346);

        close(Mundo.mundo.getMap((short) 9573), (short) 440);
        close(Mundo.mundo.getMap((short) 9573), (short) 219);

        close(Mundo.mundo.getMap((short) 9574), (short) 425);
        close(Mundo.mundo.getMap((short) 9574), (short) 273);
        close(Mundo.mundo.getMap((short) 9574), (short) 51);

        close(Mundo.mundo.getMap((short) 9575), (short) 427);
        close(Mundo.mundo.getMap((short) 9575), (short) 335);
        close(Mundo.mundo.getMap((short) 9575), (short) 331);
        close(Mundo.mundo.getMap((short) 9575), (short) 94);

        close(Mundo.mundo.getMap((short) 9576), (short) 441);
        close(Mundo.mundo.getMap((short) 9576), (short) 306);
        close(Mundo.mundo.getMap((short) 9576), (short) 317);
        close(Mundo.mundo.getMap((short) 9576), (short) 67);

        close(Mundo.mundo.getMap((short) 9577), (short) 426);
        close(Mundo.mundo.getMap((short) 9577), (short) 364);
        close(Mundo.mundo.getMap((short) 9577), (short) 390);
        close(Mundo.mundo.getMap((short) 9577), (short) 50);
    }

    @SuppressWarnings("unused")
    private static void openAll() {
        open(Mundo.mundo.getMap((short) 9553), (short) 428);
        open(Mundo.mundo.getMap((short) 9553), (short) 288);

        open(Mundo.mundo.getMap((short) 9554), (short) 431);
        open(Mundo.mundo.getMap((short) 9554), (short) 306);
        open(Mundo.mundo.getMap((short) 9554), (short) 138);

        open(Mundo.mundo.getMap((short) 9555), (short) 440);
        open(Mundo.mundo.getMap((short) 9555), (short) 332);
        open(Mundo.mundo.getMap((short) 9555), (short) 79);

        open(Mundo.mundo.getMap((short) 9556), (short) 428);
        open(Mundo.mundo.getMap((short) 9556), (short) 306);
        open(Mundo.mundo.getMap((short) 9556), (short) 332);
        open(Mundo.mundo.getMap((short) 9556), (short) 64);

        open(Mundo.mundo.getMap((short) 9557), (short) 413);
        open(Mundo.mundo.getMap((short) 9557), (short) 306);
        open(Mundo.mundo.getMap((short) 9557), (short) 230);
        open(Mundo.mundo.getMap((short) 9557), (short) 51);

        open(Mundo.mundo.getMap((short) 9558), (short) 427);
        open(Mundo.mundo.getMap((short) 9558), (short) 277);
        open(Mundo.mundo.getMap((short) 9558), (short) 361);
        open(Mundo.mundo.getMap((short) 9558), (short) 77);

        open(Mundo.mundo.getMap((short) 9559), (short) 427);
        open(Mundo.mundo.getMap((short) 9559), (short) 277);
        open(Mundo.mundo.getMap((short) 9559), (short) 65);

        open(Mundo.mundo.getMap((short) 9560), (short) 428);
        open(Mundo.mundo.getMap((short) 9560), (short) 302);
        open(Mundo.mundo.getMap((short) 9560), (short) 52);

        open(Mundo.mundo.getMap((short) 9561), (short) 441);
        open(Mundo.mundo.getMap((short) 9561), (short) 306);
        open(Mundo.mundo.getMap((short) 9561), (short) 317);
        open(Mundo.mundo.getMap((short) 9561), (short) 80);

        open(Mundo.mundo.getMap((short) 9562), (short) 429);
        open(Mundo.mundo.getMap((short) 9562), (short) 320);
        open(Mundo.mundo.getMap((short) 9562), (short) 303);
        open(Mundo.mundo.getMap((short) 9562), (short) 52);

        open(Mundo.mundo.getMap((short) 9563), (short) 429);
        open(Mundo.mundo.getMap((short) 9563), (short) 292);
        open(Mundo.mundo.getMap((short) 9563), (short) 288);
        open(Mundo.mundo.getMap((short) 9563), (short) 52);

        open(Mundo.mundo.getMap((short) 9564), (short) 429);
        open(Mundo.mundo.getMap((short) 9564), (short) 335);
        open(Mundo.mundo.getMap((short) 9564), (short) 259);

        open(Mundo.mundo.getMap((short) 9565), (short) 414);
        open(Mundo.mundo.getMap((short) 9565), (short) 262);
        open(Mundo.mundo.getMap((short) 9565), (short) 51);

        open(Mundo.mundo.getMap((short) 9566), (short) 332);
        open(Mundo.mundo.getMap((short) 9566), (short) 51);

        open(Mundo.mundo.getMap((short) 9567), (short) 277);
        open(Mundo.mundo.getMap((short) 9567), (short) 346);
        open(Mundo.mundo.getMap((short) 9567), (short) 37);

        open(Mundo.mundo.getMap((short) 9568), (short) 277);
        open(Mundo.mundo.getMap((short) 9568), (short) 346);
        open(Mundo.mundo.getMap((short) 9568), (short) 51);

        open(Mundo.mundo.getMap((short) 9569), (short) 291);
        open(Mundo.mundo.getMap((short) 9569), (short) 317);
        open(Mundo.mundo.getMap((short) 9569), (short) 64);

        open(Mundo.mundo.getMap((short) 9570), (short) 306);
        open(Mundo.mundo.getMap((short) 9570), (short) 51);

        open(Mundo.mundo.getMap((short) 9571), (short) 428);
        open(Mundo.mundo.getMap((short) 9571), (short) 277);
        open(Mundo.mundo.getMap((short) 9571), (short) 331);

        open(Mundo.mundo.getMap((short) 9572), (short) 443);
        open(Mundo.mundo.getMap((short) 9572), (short) 263);
        open(Mundo.mundo.getMap((short) 9572), (short) 346);

        open(Mundo.mundo.getMap((short) 9573), (short) 440);
        open(Mundo.mundo.getMap((short) 9573), (short) 219);

        open(Mundo.mundo.getMap((short) 9574), (short) 425);
        open(Mundo.mundo.getMap((short) 9574), (short) 273);
        open(Mundo.mundo.getMap((short) 9574), (short) 51);

        open(Mundo.mundo.getMap((short) 9575), (short) 427);
        open(Mundo.mundo.getMap((short) 9575), (short) 335);
        open(Mundo.mundo.getMap((short) 9575), (short) 331);
        open(Mundo.mundo.getMap((short) 9575), (short) 94);

        open(Mundo.mundo.getMap((short) 9576), (short) 441);
        open(Mundo.mundo.getMap((short) 9576), (short) 306);
        open(Mundo.mundo.getMap((short) 9576), (short) 317);
        open(Mundo.mundo.getMap((short) 9576), (short) 67);

        open(Mundo.mundo.getMap((short) 9577), (short) 426);
        open(Mundo.mundo.getMap((short) 9577), (short) 364);
        open(Mundo.mundo.getMap((short) 9577), (short) 390);
        open(Mundo.mundo.getMap((short) 9577), (short) 50);
    }

    private static void openTimer(final Mapa map, final int cellId) {
        if (map.getCase(cellId).isWalkable(false)) // Elle est d�j� ouverte
            return;
        open(map, (short) cellId);
        Temporizador.addSiguiente(() -> close(map, (short) cellId), time, TimeUnit.MINUTES, Temporizador.DataType.MAPA);
    }

    private static void open(Mapa map, short cellId) {
        sendOpen(map, cellId);
        map.removeCase(cellId);
        map.getCases().add(new GameCase(map, cellId, true, true, -1));
    }

    private static void close(Mapa map, short cellId) {
        if (!map.getCase(cellId).isWalkable(false)) // Elle est d�j� ferm�.
            return;
        sendClose(map, cellId);
        map.removeCase(cellId);
        map.getCases().add(new GameCase(map, cellId, false, false, -1));
    }

    private static void sendOpen(Mapa map, int cellId) {
        GestorSalida.GAME_UPDATE_CELL(map, cellId + ";aaGaaaaaaa801;1");
        GestorSalida.GAME_SEND_ACTION_TO_DOOR(map, cellId, true);
    }

    private static void sendOpen(Jugador p, int cellId) {
        GestorSalida.GAME_UPDATE_CELL(p, cellId + ";aaGaaaaaaa801;1");
        GestorSalida.GAME_SEND_ACTION_TO_DOOR(p, cellId, true);
    }

    private static void sendClose(Mapa map, int cellId) {
        GestorSalida.GAME_UPDATE_CELL(map, cellId + ";aaaaaaaaaa801;1");
        GestorSalida.GAME_SEND_ACTION_TO_DOOR(map, cellId, false);
    }

    private static void sendClose(Jugador p, int cellId) {
        GestorSalida.GAME_UPDATE_CELL(p, cellId + ";aaaaaaaaaa801;1");
        GestorSalida.GAME_SEND_ACTION_TO_DOOR(p, cellId, false);
    }

    public static void sendPacketMap(Jugador perso) {
        Mapa map = perso.getCurMap();
        GameCase c1 = null; // bas
        GameCase c2 = null; // gauche
        GameCase c3 = null; // droite
        GameCase c4 = null; // haut
        switch (map.getId()) {
            case 9553 -> {
                c1 = map.getCase(428);
                c3 = map.getCase(288);
            }
            case 9554 -> {
                c1 = map.getCase(431);
                c2 = map.getCase(306);
                c4 = map.getCase(138);
            }
            case 9555 -> {
                c1 = map.getCase(440);
                c3 = map.getCase(332);
                c4 = map.getCase(79);
            }
            case 9556 -> {
                c1 = map.getCase(428);
                c2 = map.getCase(306);
                c3 = map.getCase(332);
                c4 = map.getCase(64);
            }
            case 9557 -> {
                c1 = map.getCase(413);
                c2 = map.getCase(306);
                c3 = map.getCase(230);
                c4 = map.getCase(51);
            }
            case 9558 -> {
                c1 = map.getCase(427);
                c2 = map.getCase(277);
                c3 = map.getCase(361);
                c4 = map.getCase(77);
            }
            case 9559 -> {
                c1 = map.getCase(427);
                c2 = map.getCase(277);
                c4 = map.getCase(65);
            }
            case 9560 -> {
                c1 = map.getCase(428);
                c3 = map.getCase(302);
                c4 = map.getCase(52);
            }
            case 9561 -> {
                c1 = map.getCase(441);
                c2 = map.getCase(306);
                c3 = map.getCase(317);
                c4 = map.getCase(80);
            }
            case 9562 -> {
                c1 = map.getCase(429);
                c2 = map.getCase(320);
                c3 = map.getCase(303);
                c4 = map.getCase(52);
            }
            case 9563 -> {
                c1 = map.getCase(429);
                c2 = map.getCase(292);
                c3 = map.getCase(288);
                c4 = map.getCase(52);
            }
            case 9564 -> {
                c1 = map.getCase(429);
                c2 = map.getCase(335);
                c3 = map.getCase(259);
            }
            case 9565 -> {
                c1 = map.getCase(414);
                c2 = map.getCase(262);
                c4 = map.getCase(51);
            }
            case 9566 -> {
                c3 = map.getCase(332);
                c4 = map.getCase(51);
            }
            case 9567 -> {
                c2 = map.getCase(277);
                c3 = map.getCase(346);
                c4 = map.getCase(37);
            }
            case 9568 -> {
                c2 = map.getCase(277);
                c3 = map.getCase(346);
                c4 = map.getCase(51);
            }
            case 9569 -> {
                c2 = map.getCase(291);
                c3 = map.getCase(317);
                c4 = map.getCase(64);
            }
            case 9570 -> {
                c2 = map.getCase(306);
                c4 = map.getCase(51);
            }
            case 9571 -> {
                c1 = map.getCase(428);
                c2 = map.getCase(277);
                c3 = map.getCase(331);
            }
            case 9572 -> {
                c1 = map.getCase(443);
                c2 = map.getCase(263);
                c3 = map.getCase(346);
            }
            case 9573 -> {
                c1 = map.getCase(440);
                c2 = map.getCase(219);
            }
            case 9574 -> {
                c1 = map.getCase(425);
                c3 = map.getCase(273);
                c4 = map.getCase(51);
            }
            case 9575 -> {
                c1 = map.getCase(427);
                c2 = map.getCase(335);
                c3 = map.getCase(331);
                c4 = map.getCase(94);
            }
            case 9576 -> {
                c1 = map.getCase(441);
                c2 = map.getCase(306);
                c3 = map.getCase(317);
                c4 = map.getCase(67);
            }
            case 9577 -> {
                c1 = map.getCase(426);
                c2 = map.getCase(364);
                c3 = map.getCase(390);
                c4 = map.getCase(50);
            }
        }

        if (c1 != null) {
            if (c1.isWalkable(false))
                sendOpen(perso, c1.getId());
            else
                sendClose(perso, c1.getId());
        }

        if (c2 != null) {
            if (c2.isWalkable(false))
                sendOpen(perso, c2.getId());
            else
                sendClose(perso, c2.getId());
        }

        if (c3 != null) {
            if (c3.isWalkable(false))
                sendOpen(perso, c3.getId());
            else
                sendClose(perso, c3.getId());
        }

        if (c4 != null) {
            if (c4.isWalkable(false))
                sendOpen(perso, c4.getId());
            else
                sendClose(perso, c4.getId());
        }
    }

    public static boolean isValidMap(Mapa map) {
        if(map == null) return false;
        return switch (map.getId()) {
            case 9553, 9564, 9571, 9572, 9573, 9574, 9575, 9576, 9577, 9554, 9555, 9556, 9557, 9558, 9559, 9560, 9561, 9562, 9563, 9565, 9566, 9567, 9568, 9569, 9570 -> true;
            default -> false;
        };
    }
}
