package org.alexandria.estaticos;

import org.alexandria.estaticos.area.Area;
import org.alexandria.estaticos.area.SubArea;
import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.estaticos.cliente.Jugador.Caracteristicas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Constantes;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;

import java.util.HashMap;

public class Prisma {

    private final int id;
    private final int alignement;
    private int level;
    private final short Map;
    private int cell;
    private final int dir;
    private final int name;
    private final int gfx;
    private int inFight;
    private int fightId;
    private int honor = 0;
    private int area = -1;
    private Pelea fight;
    private final java.util.Map<Integer, Integer> stats = new HashMap<>();

    public Prisma(int id, int alignement, int level, short Map, int cell,
                  int honor, int area) {
        this.id = id;
        this.alignement = alignement;
        this.level = level;
        this.Map = Map;
        this.cell = cell;
        this.dir = 1;
        if (alignement == 1) {
            this.name = 1111;
            this.gfx = 8101;
        } else {
            this.name = 1112;
            this.gfx = 8100;
        }
        this.inFight = -1;
        this.fightId = -1;
        this.honor = honor;
        this.area = area;
        this.fight = null;
    }

    public static void parseAttack(Jugador perso) {
        for (Prisma Prisme : Mundo.mundo.AllPrisme())
            if ((Prisme.inFight == 0 || Prisme.inFight == -2)
                    && perso.get_align() == Prisme.getAlignement())
                GestorSalida.SEND_Cp_INFO_ATTAQUANT_PRISME(perso, attackerOfPrisme(Prisme.id, Prisme.Map, Prisme.fightId));
    }

    public static void parseDefense(Jugador perso) {
        for (Prisma Prisme : Mundo.mundo.AllPrisme())
            if ((Prisme.inFight == 0 || Prisme.inFight == -2)
                    && perso.get_align() == Prisme.getAlignement())
                GestorSalida.SEND_CP_INFO_DEFENSEURS_PRISME(perso, defenderOfPrisme(Prisme.id, Prisme.Map, Prisme.fightId));
    }

    public static String attackerOfPrisme(int id, short MapId, int FightId) {
        StringBuilder str = new StringBuilder("+");
        str.append(Integer.toString(id, 36));
        Mapa gameMap = Mundo.mundo.getMap(MapId);
        if(gameMap != null) {
            for (Pelea fight : gameMap.getFights()) {
                if (fight.getId() == FightId) {
                    for (Peleador fighter : fight.getFighters(1)) {
                        if (fighter.getPlayer() == null)
                            continue;
                        str.append("|");
                        str.append(Integer.toString(fighter.getPlayer().getId(), 36)).append(";");
                        str.append(fighter.getPlayer().getName()).append(";");
                        str.append(fighter.getPlayer().getLevel()).append(";");
                        str.append("0;");
                    }
                }
            }
        }
        return str.toString();
    }

    public static String defenderOfPrisme(int id, short MapId, int FightId) {
        StringBuilder str = new StringBuilder("+");
        String stra = "";
        str.append(Integer.toString(id, 36));
        Mapa gameMap = Mundo.mundo.getMap(MapId);
        if(gameMap != null) {
            for (Pelea fight : gameMap.getFights()) {
                if (fight.getId() == FightId) {
                    for (Peleador fighter : fight.getFighters(2)) {
                        if (fighter.getPlayer() == null)
                            continue;
                        str.append("|");
                        str.append(Integer.toString(fighter.getPlayer().getId(), 36)).append(";");
                        str.append(fighter.getPlayer().getName()).append(";");
                        str.append(fighter.getPlayer().getGfxId()).append(";");
                        str.append(fighter.getPlayer().getLevel()).append(";");
                        str.append(Integer.toString(fighter.getPlayer().getColor1(), 36)).append(";");
                        str.append(Integer.toString(fighter.getPlayer().getColor2(), 36)).append(";");
                        str.append(Integer.toString(fighter.getPlayer().getColor3(), 36)).append(";");
                        if (fight.getFighters(2).size() > 7)
                            str.append("1;");
                        else
                            str.append("0;");
                    }
                    stra = str.substring(1);
                    stra = "-" + stra;
                    fight.setDefenders(stra);
                }
            }
        }
        return str.toString();
    }

    public int getId() {
        return this.id;
    }

    public int getAlignement() {
        return this.alignement;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int i) {
        this.level = i;
    }

    public short getMap() {
        return this.Map;
    }

    public int getCell() {
        return this.cell;
    }

    public void setCell(int i) {
        this.cell = i;
    }

    public int getInFight() {
        return this.inFight;
    }

    public void setInFight(int i) {
        this.inFight = i;
    }

    public int getFightId() {
        return this.fightId;
    }

    public void setFightId(int i) {
        this.fightId = i;
    }

    public int getTurnTime() {
        return 45000;
    }

    public int getHonor() {
        return this.honor;
    }

    public void addHonor(int i) {
        this.honor += i;
    }

    public int getGrade() {
        int g = 1;
        for (int n = 1; n <= 10; n++) {
            if (this.honor < Mundo.mundo.getExpLevel(n).pvp) {
                g = n - 1;
                break;
            }
        }
        return g;
    }

    public int getConquestArea() {
        return this.area;
    }

    public void setConquestArea(int i) {
        this.area = i;
    }

    public Pelea getFight() {
        return this.fight;
    }

    public void setFight(Pelea fight) { this.fight = fight; }

    public Caracteristicas getStats() {
        return new Caracteristicas(this.stats);
    }

    public void refreshStats() {
        int feu = 1000 + (500 * this.level);
        int intel = 1000 + (500 * this.level);
        int agi = 1000 + (500 * this.level);
        int sagesse = 1000 + (500 * this.level);
        int chance = 1000 + (500 * this.level);
        int resistance = 9 * this.level;
        this.stats.clear();
        this.stats.put(Constantes.STATS_ADD_FORC, feu);
        this.stats.put(Constantes.STATS_ADD_INTE, intel);
        this.stats.put(Constantes.STATS_ADD_AGIL, agi);
        this.stats.put(Constantes.STATS_ADD_SAGE, sagesse);
        this.stats.put(Constantes.STATS_ADD_CHAN, chance);
        this.stats.put(Constantes.STATS_ADD_RP_NEU, resistance);
        this.stats.put(Constantes.STATS_ADD_RP_FEU, resistance);
        this.stats.put(Constantes.STATS_ADD_RP_EAU, resistance);
        this.stats.put(Constantes.STATS_ADD_RP_AIR, resistance);
        this.stats.put(Constantes.STATS_ADD_RP_TER, resistance);
        this.stats.put(Constantes.STATS_ADD_AFLEE, resistance);
        this.stats.put(Constantes.STATS_ADD_MFLEE, resistance);
        this.stats.put(Constantes.STATS_ADD_PA, 6);
        this.stats.put(Constantes.STATS_ADD_PM, 0);
    }

    public int getX() {
        Mapa Map = Mundo.mundo.getMap(this.Map);
        return Map.getX();
    }

    public int getY() {
        Mapa Map = Mundo.mundo.getMap(this.Map);
        return Map.getY();
    }

    public SubArea getSubArea() {
        Mapa Map = Mundo.mundo.getMap(this.Map);
        return Map.getSubArea();
    }

    public Area getArea() {
        Mapa Map = Mundo.mundo.getMap(this.Map);
        return Map.getSubArea().getArea();
    }

    public String getGMPrisme() {
        if (this.inFight != -1)
            return "";
        String str = "GM|+";
        str += this.cell + ";";
        str += this.dir + ";0;" + this.id + ";" + this.name + ";-10;"
                + this.gfx + "^100;" + this.level + ";" + getGrade() + ";"
                + this.alignement;
        return str;
    }
}
