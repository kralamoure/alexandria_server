package org.alexandria.estaticos;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.estaticos.cliente.Jugador.Caracteristicas;
import org.alexandria.configuracion.Constantes;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Gremio {

    private final int id;
    private long xp;
    private final long date;
    private String name = "", emblem = "";
    private int lvl, capital = 0, nbCollectors = 0;
    private final Map<Integer, GremioMiembros> members = new TreeMap<>();
    private final Map<Integer, Hechizo.SortStats> spells = new HashMap<>(); // <Id, Level>
    private final Map<Integer, Integer> stats = new HashMap<>(); // <Effect, Quantity>
    private final Map<Integer, Integer> statsFight=new HashMap<>();


    public Gremio(String name, String emblem) {
        this.id = Database.dinamicos.getGuildData().getNextId();
        this.name = name;
        this.emblem = emblem;
        this.lvl = 1;
        this.xp = 0;
        this.date = Instant.now().toEpochMilli();
        this.decompileSpell("462;0|461;0|460;0|459;0|458;0|457;0|456;0|455;0|454;0|453;0|452;0|451;0|");
        this.decompileStats("176;100|158;1000|124;100|");
    }

    public Gremio(int id, String name, String emblem, int lvl, long xp, int capital, int nbCollectors, String sorts, String stats, long date) {
        this.id = id;
        this.name = name;
        this.emblem = emblem;
        this.xp = xp;
        this.lvl = lvl;
        this.capital = capital;
        this.nbCollectors = nbCollectors;
        this.date = date;
        this.decompileSpell(sorts);
        this.decompileStats(stats);
    }

    public void addMember(int id, int r, byte pXp, long x, int ri) {
        Jugador player = Mundo.mundo.getPlayer(id);
        if (player == null) return;
        GremioMiembros guildMember = new GremioMiembros(player, this, r, x, pXp, ri);
        this.members.put(id, guildMember);
        player.setGuildMember(guildMember);
    }

    public GremioMiembros addNewMember(Jugador player) {
        GremioMiembros guildMember = new GremioMiembros(player, this, 0, 0, (byte) 0, 0);
        this.members.put(player.getId(), guildMember);
        player.setGuildMember(guildMember);
        return guildMember;
    }

    public int getId() {
        return this.id;
    }

    public int getNbCollectors() {
        return this.nbCollectors;
    }

    public void setNbCollectors(int nbr) {
        this.nbCollectors = nbr;
    }

    public int getCapital() {
        return this.capital;
    }

    public void setCapital(int nbr) {
        this.capital = nbr;
    }

    public Map<Integer, Hechizo.SortStats> getSpells() {
        return this.spells;
    }

    public Map<Integer, Integer> getStats() {
        return stats;
    }

    public long getDate() {
        return date;
    }

    public Caracteristicas getStatsFight()
    {
        return new Caracteristicas(this.statsFight);
    }

    public void boostSpell(int id) {
        Hechizo.SortStats SS = this.spells.get(id);
        if (SS != null && SS.getLevel() == 5)
            return;
        this.spells.put(id, ((SS == null) ? Mundo.mundo.getSort(id).getStatsByLevel(1) : Mundo.mundo.getSort(id).getStatsByLevel(SS.getLevel() + 1)));
    }

    public void unBoostSpell(int id) {
        Hechizo.SortStats SS = this.spells.get(id);
        if (SS != null) {
            this.capital += 5 * SS.getLevel();
            this.spells.put(id, null);
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmblem() {
        return this.emblem;
    }

    public long getXp() {
        return this.xp;
    }

    public int getLvl() {
        return this.lvl;
    }

    public boolean haveTenMembers() {
        return this.id == 1 || this.id == 2 || (this.members.size() >= 10);
    }

    public List<Jugador> getPlayers() {
        return this.members.values().stream().filter(guildMember -> guildMember.getPlayer() != null).map(GremioMiembros::getPlayer).collect(Collectors.toList());
    }

    public GremioMiembros getMember(int id) {
        GremioMiembros guildMember = this.members.get(id);
        if(guildMember == null)
            Database.estaticos.getGuildMemberData().load(id);
        return this.members.get(id) == null ? this.members.get(id) : guildMember;
    }

    public void removeMember(Jugador player) {
        Casas house = Mundo.mundo.getHouseManager().getHouseByPerso(player);
        if (house != null)
            if (Mundo.mundo.getHouseManager().houseOnGuild(this.id) > 0)
                Database.estaticos.getHouseData().updateGuild(house, 0, 0);

        this.members.remove(player.getId());
        Database.estaticos.getGuildMemberData().delete(player.getId());
    }

    public void addXp(long xp) {
        this.xp += xp;
        while (this.xp >= Mundo.mundo.getGuildXpMax(this.lvl) && this.lvl < 200) this.levelUp();
    }

    private void levelUp() {
        this.lvl++;
        this.capital += 5;
    }

    private void decompileSpell(String spells) {
        for (String split : spells.split("\\|"))
            this.spells.put(Integer.parseInt(split.split(";")[0]), Mundo.mundo.getSort(Integer.parseInt(split.split(";")[0])).getStatsByLevel(Integer.parseInt(split.split(";")[1])));
    }

    public String compileSpell() {
        if (this.spells.isEmpty())
            return "";

        StringBuilder toReturn = new StringBuilder();
        boolean isFirst = true;

        for (Entry<Integer, Hechizo.SortStats> curSpell : this.spells.entrySet()) {
            if (!isFirst)
                toReturn.append("|");
            toReturn.append(curSpell.getKey()).append(";").append(((curSpell.getValue() == null) ? 0 : curSpell.getValue().getLevel()));
            isFirst = false;
        }

        return toReturn.toString();
    }

    private void decompileStats(String statsStr) {
        for (String split : statsStr.split("\\|"))
            this.stats.put(Integer.parseInt(split.split(";")[0]), Integer.parseInt(split.split(";")[1]));
    }

    public String compileStats() {
        if (this.stats.isEmpty())
            return "";

        StringBuilder toReturn = new StringBuilder();
        boolean isFirst = true;

        for (Entry<Integer, Integer> curStats : this.stats.entrySet()) {
            if (!isFirst)
                toReturn.append("|");

            toReturn.append(curStats.getKey()).append(";").append(curStats.getValue());

            isFirst = false;
        }

        return toReturn.toString();
    }

    public void upgradeStats(int id, int add) {
        this.stats.put(id, (this.stats.get(id) + add));
    }

    public int resetStats(int id) {
        int quantity = this.stats.get(id);
        this.stats.put(id, 0);
        return quantity;
    }

    public int getStats(int id) {
        return stats.get(id);
    }

    //region Parse packet
    public String parseCollectorToGuild() {
        return getNbCollectors() + "|" + Recaudador.countCollectorGuild(getId()) + "|" + 100 * getLvl() + "|" + getLvl() + "|" + getStats(158) + "|" + getStats(176) + "|" + getStats(124) + "|" + getNbCollectors() + "|" + getCapital() + "|" + (1000 + (10 * getLvl())) + "|" + compileSpell();
    }

    public String parseQuestionTaxCollector() {
        return "1" + ';' + getName() + ',' + getStats(Constantes.STATS_ADD_PODS) + ',' + getStats(Constantes.STATS_ADD_PROS) + ',' + getStats(Constantes.STATS_ADD_SAGE) + ',' + getNbCollectors();
    }

    public String parseMembersToGM() {
        StringBuilder str = new StringBuilder();
        for (GremioMiembros GM : this.members.values()) {
            String online = "0";
            if (GM.getPlayer() != null)
                if (GM.getPlayer().isOnline())
                    online = "1";
            if (str.length() != 0)
                str.append("|");
            str.append(GM.getPlayerId()).append(";");
            str.append(GM.getPlayer().getName()).append(";");
            str.append(GM.getPlayer().getLevel()).append(";");
            str.append(GM.getPlayer().getGfxId()).append(";");
            str.append(GM.getRank()).append(";");
            str.append(GM.getXpGave()).append(";");
            str.append(GM.getXpGive()).append(";");
            str.append(GM.getRights()).append(";");
            str.append(online).append(";");
            str.append(GM.getPlayer().get_align()).append(";");
            str.append(GM.getHoursFromLastCo());
        }
        return str.toString();
    }

    public class GremioMiembros {

        private final Jugador player;
        private final Gremio guild;
        private int rank = 0;
        private byte xpGive = 0;
        private long xpGave = 0;
        private int rights = 0;
        private final Map<Integer, Boolean> haveRights = new TreeMap<>();

        GremioMiembros(Jugador player, Gremio guild, int rank, long xpGave, byte xpGive, int rights) {
            this.player = player;
            this.guild = guild;
            this.rank = rank;
            this.xpGave = xpGave;
            this.xpGive = xpGive;
            this.rights = rights;
            this.parseIntToRight(this.rights);
        }

        public Jugador getPlayer() {
            return player;
        }

        public int getPlayerId() {
            return player.getId();
        }

        public Gremio getGuild() {
            return guild;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int i) {
            this.rank = i;
        }

        public long getXpGave() {
            return xpGave;
        }

        public int getXpGive() {
            return xpGive;
        }

        public void giveXpToGuild(long xp) {
            this.xpGave += xp;
            this.guild.addXp(xp);
        }

        public String parseRights() {
            return Integer.toString(this.rights, 36);
        }

        public int getRights() {
            return rights;
        }

        int getHoursFromLastCo() {
            String[] split = player.getAccount().getLastConnectionDate().split("~");
            LocalDate localDate = new LocalDate(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            return Days.daysBetween(localDate, new LocalDate()).getDays() * 24;
        }

        public boolean canDo(int rightValue) {
            return this.rights == 1 ? true : haveRights.get(rightValue);
        }

        public void setAllRights(int rank, byte xp, int right, Jugador perso) {
            if (rank == -1) rank = this.rank;
            if (xp < 0) xp = this.xpGive;
            if (xp > 90) xp = 90;
            if (right == -1) right = this.rights;

            this.rank = rank;
            this.xpGive = xp;

            if (right != this.rights && right != 1) //V?rifie si les droits sont pareille ou si des droits de meneur; pour ne pas faire la conversion pour rien
                this.parseIntToRight(right);
            this.rights = right;

            Database.estaticos.getGuildMemberData().update(perso);
        }

        private void initRights() {
            for(int right : Constantes.G_RIGHTS) {
                this.haveRights.put(right, false);
            }
        }

        private void parseIntToRight(int total) {
            if (this.haveRights.isEmpty()) {
                this.initRights();
            }
            if (total != 1) {
                if (this.haveRights.size() > 0)//Si les droits contiennent quelque chose -> Vidage (M?me si le HashMap supprimerais les entr?es doublon lors de l'ajout)
                    this.haveRights.clear();
                initRights();//Remplissage des droits

                Integer[] array = this.haveRights.keySet().toArray(new Integer[0]); //R?cup?re les clef de map dans un tableau d'Integer

                while (total > 0) {
                    int i = this.haveRights.size() - 1;
                    while (i < this.haveRights.size()) {
                        if (array[i] <= total) {
                            total ^= array[i];
                            this.haveRights.put(array[i], true);
                            break;
                        }
                        i--;
                    }
                }
            }
        }
    }
}