package org.alexandria.estaticos;

import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.Camino;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Constantes;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.pelea.Pelea;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;

public class Recaudador {

    private final int id;
    private final short map;
    private int cell;
    private final byte orientation;
    private int guildId = 0;
    private short N1 = 0;
    private short N2 = 0;
    private byte inFight = 0;
    private int inFightId = -1;
    private long kamas = 0;
    private long xp = 0;
    private boolean inExchange = false;
    private Jugador poseur = null;
    private final long date;
    //Timer
    private long timeTurn=45000;
    //Los logs
    private final java.util.Map<Integer, ObjetoJuego> logObjects = new HashMap<>();
    private final java.util.Map<Integer, ObjetoJuego> objects = new HashMap<>();
    //Defensa
    private final java.util.Map<Integer, Jugador> defenserId = new HashMap<>();

    public Recaudador(int id, short map, int cell, byte orientation,
                      int aGuildID, short N1, short N2, Jugador poseur, long date,
                      String items, long kamas, long xp) {
        this.id = id;
        this.map = map;
        this.cell = cell;
        this.orientation = orientation;
        this.guildId = aGuildID;
        this.N1 = N1;
        this.N2 = N2;
        this.poseur = poseur;
        this.date = date;
        for (String item : items.split("\\|")) {
            if (item.equals(""))
                continue;
            String[] infos = item.split(":");
            int itemId = Integer.parseInt(infos[0]);
            ObjetoJuego obj = Mundo.getGameObject(itemId);
            if (obj == null)
                continue;
            this.objects.put(obj.getId(), obj);
        }
        this.xp = xp;
        this.kamas = kamas;
    }

    public String getFullName() {
        return Integer.toString(this.getN1(), 36) + "," + Integer.toString(this.getN2(), 36);
    }

    public static String parseGM(Mapa map) {
        StringBuilder sock = new StringBuilder();
        sock.append("GM|");
        boolean isFirst = true;
        for (java.util.Map.Entry<Integer, Recaudador> Collector : Mundo.mundo.getCollectors().entrySet()) {
            Recaudador c = Collector.getValue();
            if (c == null)
                continue;
            if (c.inFight > 0)
                continue;//On affiche pas le Collector si il est en combat
            if (c.map == map.getId()) {
                Gremio G = Mundo.mundo.getGuild(c.guildId);
                if (G == null) {
                    c.reloadTimer();
                    Database.estaticos.getCollectorData().delete(c.getId());
                    Mundo.mundo.getCollectors().remove(c.getId());
                    continue;
                }
                if (!isFirst)
                    sock.append("|");
                sock.append("+");
                sock.append(c.cell).append(";");
                sock.append(c.orientation).append(";");
                sock.append("0").append(";");
                sock.append(c.id).append(";");
                sock.append(Integer.toString(c.N1, 36)).append(",").append(Integer.toString(c.N2, 36)).append(";");
                sock.append("-6").append(";");
                sock.append("6000^100;");
                sock.append(G.getLvl()).append(";");
                sock.append(G.getName()).append(";").append(G.getEmblem());
                isFirst = false;
            }
        }
        return sock.toString();
    }

    public void moveOnMap() {
        if(this.getInFight() > 0) return;

        Mapa map = Mundo.mundo.getMap(this.map);
        int cell = map.getRandomNearFreeCellId(this.cell);
        String path;

        try {
            path = Camino.getShortestStringPathBetween(map, this.cell, cell, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (path != null) {
            this.cell = cell;
            for (Jugador player : map.getPlayers()) {
                GestorSalida.GAME_SEND_GA_PACKET(player.getGameClient(), "0", "1", this.getId() + "", path);
            }
        }
    }

    public static String parseToGuild(int GuildID) {
        /*
         * 44705000000000
         * 1486217399798
         * gITM +
         * id; -10000
         * N1, 14
         * N2, 26
         * Owner, Lcoos
         * startDate, date collector poser
         * lastHName, dernier r�colteur
         * lastHD, date a laquel le perco a �t� r�colt�
         * nextHD; date a laquel le perco pourra �tre r�colt�
         * mapid;
         * state; 0 r�colte, 1 attaque, 2 combat, � la fin du timer, passe en combat automatiquement
         * time; temps en ms quand le perco a �t� lanc�
         * maxTimer;temps en ms quand le combat se lance
         * numbPlayer: 1-7
         *
         * les dates au dessus aucune conversion, juste un timestamp � mettre ?
         * ouaip impec, grand merci :) !
         *
         * TEST : gITM+-10000;14,26,poney,0,tagada,55,6400000000000;5q6;1;
         * 1000000000000;2400000000000;7
         */

        // id du poseur
        // date quand on pose
        StringBuilder packet = new StringBuilder();
        boolean isFirst = true;
        for (java.util.Map.Entry<Integer, Recaudador> Collector : Mundo.mundo.getCollectors().entrySet()) {
            if (Collector.getValue().getGuildId() == GuildID) {
                Mapa map = Mundo.mundo.getMap(Collector.getValue().getMap());
                if (isFirst)
                    packet.append("+");
                if (!isFirst)
                    packet.append("|");

                Recaudador perco = Collector.getValue();
                int inFight = Collector.getValue().getInFight();
                String name = "";
                if (Collector.getValue().getPoseur() != null) {
                    name = Collector.getValue().getPoseur().getName();
                }

                packet.append(perco.getId()); // id
                packet.append(";");
                packet.append(Integer.toString(perco.N1, 36)); // nameId1
                packet.append(",");
                packet.append(Integer.toString(perco.N2, 36)); // nameId2

                packet.append(",");
                packet.append(name); // callerName
                packet.append(",");
                packet.append(perco.date); // startDate
                packet.append(",");
                packet.append(""); // lastHName
                packet.append(",");
                packet.append("-1"); // lastHD
                packet.append(",");
                packet.append((perco.date
                        + Mundo.mundo.getGuild(GuildID).getLvl() * 600000)); // nextHD
                packet.append(";");

                packet.append(Integer.toString(map.getId(), 36));
                packet.append(",");
                packet.append(map.getX());
                packet.append(",");
                packet.append(map.getY());
                packet.append(";");

                packet.append(inFight);
                packet.append(";");

                if (inFight == 1) {
                    if (map.getFight(Collector.getValue().get_inFightID()) == null) {
                        packet.append("45000");//TimerActuel
                        packet.append(";");
                    } else {
                        final Pelea fight = map.getFight(Collector.getValue().get_inFightID());
                        long start = Instant.now().toEpochMilli() - fight.getLaunchTime();
                        if(start > 45000) start = 45000;
                        packet.append(45000 - start);//TimerActuel si combat
                        packet.append(";");
                    }

                    packet.append("45000");//TimerInit
                    packet.append(";");

                    int numcase = (Mundo.mundo.getMap(Collector.getValue().getMap()).getMaxTeam() - 1);
                    if (numcase > 7)
                        numcase = 7;
                    packet.append(numcase);//Nombre de place maximum : En fonction de la map moins celle du Collector
                    packet.append(";");
                } else {
                    packet.append("0;");
                    packet.append("45000;");
                    packet.append("7;");
                }
                isFirst = false;
            }
        }
        if (packet.length() == 0)
            packet = new StringBuilder("null");

        return packet.toString();

    }

    public static int getCollectorByGuildId(int id) {
        for (java.util.Map.Entry<Integer, Recaudador> Collector : Mundo.mundo.getCollectors().entrySet())
            if (Collector.getValue().getMap() == id)
                return Collector.getValue().getGuildId();
        return 0;
    }

    public static Recaudador getCollectorByMapId(short id) {
        for (java.util.Map.Entry<Integer, Recaudador> Collector : Mundo.mundo.getCollectors().entrySet())
            if (Collector.getValue().getMap() == id)
                return Mundo.mundo.getCollectors().get(Collector.getValue().getId());
        return null;
    }

    public static int countCollectorGuild(int GuildID) {
        int i = 0;
        for (java.util.Map.Entry<Integer, Recaudador> Collector : Mundo.mundo.getCollectors().entrySet())
            if (Collector.getValue().getGuildId() == GuildID)
                i++;
        return i;
    }

    public static void parseAttaque(Jugador perso, int guildID) {
        for (java.util.Map.Entry<Integer, Recaudador> Collector : Mundo.mundo.getCollectors().entrySet())
            if (Collector.getValue().getInFight() > 0 && Collector.getValue().getGuildId() == guildID)
                GestorSalida.GAME_SEND_gITp_PACKET(perso, parseAttaqueToGuild(Collector.getValue().getId(), Collector.getValue().getMap(), Collector.getValue().get_inFightID()));
    }

    public static void parseDefense(Jugador perso, int guildID) {
        for (java.util.Map.Entry<Integer, Recaudador> Collector : Mundo.mundo.getCollectors().entrySet())
            if (Collector.getValue().getInFight() > 0 && Collector.getValue().getGuildId() == guildID)
                GestorSalida.GAME_SEND_gITP_PACKET(perso, parseDefenseToGuild(Collector.getValue()));
    }

    public static String parseAttaqueToGuild(int id, short map, int fightId) {
        StringBuilder str = new StringBuilder();
        str.append("+").append(id);
        Mapa gameMap = Mundo.mundo.getMap(map);

        if(gameMap != null) {
            gameMap.getFights().stream().filter(fight -> fight.getId() == fightId).forEach(fight -> fight.getFighters(1).stream().filter(f -> f.getPlayer() != null).forEach(f -> {
                str.append("|");
                str.append(Integer.toString(f.getPlayer().getId(), 36)).append(";");
                str.append(f.getPlayer().getName()).append(";");
                str.append(f.getPlayer().getLevel()).append(";");
                str.append("0;");
            }));
        }
        return str.toString();
    }

    public static String parseDefenseToGuild(Recaudador collector) {
        StringBuilder str = new StringBuilder();
        str.append("+").append(collector.getId());

        for (Jugador player : collector.getDefenseFight().values()) {
            if (player == null)
                continue;
            str.append("|");
            str.append(Integer.toString(player.getId(), 36)).append(";");
            str.append(player.getName()).append(";");
            str.append(player.getGfxId()).append(";");
            str.append(player.getLevel()).append(";");
            str.append(Integer.toString(player.getColor1(), 36)).append(";");
            str.append(Integer.toString(player.getColor2(), 36)).append(";");
            str.append(Integer.toString(player.getColor3(), 36)).append(";");
        }
        return str.toString();
    }

    public static void removeCollector(int GuildID) {
        for (java.util.Map.Entry<Integer, Recaudador> Collector : Mundo.mundo.getCollectors().entrySet()) {
            if (Collector.getValue().getGuildId() == GuildID) {
                Mundo.mundo.getCollectors().remove(Collector.getKey());
                for (Jugador p : Mundo.mundo.getMap(Collector.getValue().getMap()).getPlayers()) {
                    GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(p.getCurMap(), Collector.getValue().getId());//Suppression visuelle
                }
                Collector.getValue().reloadTimer();
                Database.estaticos.getCollectorData().delete(Collector.getKey());
            }
        }
    }

    public void reloadTimer() {
        if(Mundo.mundo.getGuild(getGuildId()) == null)
            return;
        Long time = Mundo.mundo.getDelayCollectors().get(this.getMap());
        if (time != null)
            return;
        Mundo.mundo.getDelayCollectors().put(this.getMap(), this.getDate());
    }

    public long getDate() {
        return this.date;
    }

    public Jugador getPoseur() {
        return this.poseur;
    }

    public void setPoseur(Jugador poseur) {
        this.poseur = poseur;
    }

    public int getId() {
        return this.id;
    }

    public short getMap() {
        return this.map;
    }

    public int getCell() {
        return this.cell;
    }

    public void setCell(int cell) {
        this.cell = cell;
    }

    public int getGuildId() {
        return this.guildId;
    }

    public int getN1() {
        return this.N1;
    }

    public int getN2() {
        return this.N2;
    }

    public int getInFight() {
        return this.inFight;
    }

    public void setInFight(byte inFight) {
        this.inFight = inFight;
    }

    public int get_inFightID() {
        return this.inFightId;
    }

    public void set_inFightID(int inFightId) {
        this.inFightId = inFightId;
    }

    public long getKamas() {
        return kamas;
    }

    public void setKamas(long kamas) {
        this.kamas = kamas;
    }

    public long getXp() {
        return this.xp;
    }

    public void setXp(long xp) {
        this.xp = xp;
    }

    public long getTurnTimer()
    {
        return this.timeTurn;
    }

    public void setTimeTurn(long timeTurn)
    {
        this.timeTurn=timeTurn;
    }

    public void removeTimeTurn(int timeTurn)
    {
        this.timeTurn-=timeTurn;
    }

    public boolean getExchange() {
        return this.inExchange;
    }

    public void setExchange(boolean inExchange) {
        this.inExchange = inExchange;
    }

    public void addLogObjects(int id, ObjetoJuego obj) {
        this.logObjects.put(id, obj);
    }

    public String getLogObjects() {
        if (this.logObjects.isEmpty())
            return "";
        StringBuilder str = new StringBuilder();

        for (ObjetoJuego obj : this.logObjects.values())
            str.append(";").append(obj.getModelo().getId()).append(",").append(obj.getCantidad());

        return str.toString();
    }

    public java.util.Map<Integer, ObjetoJuego> getOjects() {
        return this.objects;
    }

    public boolean haveObjects(int id) {
        return this.objects.get(id) != null;
    }

    public int getPodsTotal() {
        int pod = 0;
        for (ObjetoJuego object : this.objects.values())
            if (object != null)
                pod += object.getModelo().getPod() * object.getCantidad();
        return pod;
    }

    public int getMaxPod() {
        return Mundo.mundo.getGuild(this.getGuildId()).getStats(Constantes.STATS_ADD_PODS);
    }

    public boolean addObjet(ObjetoJuego newObj) {
        for (java.util.Map.Entry<Integer, ObjetoJuego> entry : this.objects.entrySet()) {
            ObjetoJuego obj = entry.getValue();
            if (Mundo.mundo.getConditionManager().stackIfSimilar(obj, newObj, true)) {
                obj.setCantidad(obj.getCantidad() + newObj.getCantidad());//On ajoute QUA item a la quantit� de l'objet existant
                return false;
            }
        }
        this.objects.put(newObj.getId(), newObj);
        return true;
    }

    public void removeObjet(int id) {
        this.objects.remove(id);
    }

    public void delCollector(int id) {
        for (ObjetoJuego obj : this.objects.values())
            Mundo.mundo.removeGameObject(obj.getId());
        Mundo.mundo.getCollectors().remove(id);
    }

    public String getItemCollectorList() {
        StringBuilder items = new StringBuilder();
        if (!this.objects.isEmpty())
            for (ObjetoJuego obj : this.objects.values())
                items.append("O").append(obj.parseItem()).append(";");
        if (this.kamas != 0)
            items.append("G").append(this.kamas);
        return items.toString();
    }

    public String parseItemCollector() {
        StringBuilder items = new StringBuilder();
        for (ObjetoJuego obj : this.objects.values())
            items.append(obj.getId()).append("|");
        return items.toString();
    }

    public void removeFromCollector(Jugador P, int id, int qua) {
        if (qua <= 0)
            return;
        ObjetoJuego CollectorObj = Mundo.getGameObject(id);
        ObjetoJuego PersoObj = P.getSimilarItem(CollectorObj);
        int newQua = CollectorObj.getCantidad() - qua;
        if (PersoObj == null)//Si le joueur n'avait aucun item similaire
        {
            //S'il ne reste rien
            if (newQua <= 0) {
                //On retire l'item
                removeObjet(id);
                //On l'ajoute au joueur
                P.addObjet(CollectorObj);
                //On envoie les packets
                String str = "O-" + id;
                GestorSalida.GAME_SEND_EsK_PACKET(P, str);
            } else
            //S'il reste des this.objects
            {
                //On cr�e une copy de l'item
                PersoObj = ObjetoJuego.getCloneObjet(CollectorObj, qua);
                //On l'ajoute au monde
                Mundo.addGameObject(PersoObj, true);
                //On retire X objet
                CollectorObj.setCantidad(newQua);
                //On l'ajoute au joueur
                P.addObjet(PersoObj);

                //On envoie les packets
                String str = "O+" + CollectorObj.getId() + "|"
                        + CollectorObj.getCantidad() + "|"
                        + CollectorObj.getModelo().getId() + "|"
                        + CollectorObj.parseStatsString();
                GestorSalida.GAME_SEND_EsK_PACKET(P, str);
            }
        } else {
            //S'il ne reste rien
            if (newQua <= 0) {
                //On retire l'item
                this.removeObjet(id);
                Mundo.mundo.removeGameObject(CollectorObj.getId());
                //On Modifie la quantit� de l'item du sac du joueur
                PersoObj.setCantidad(PersoObj.getCantidad()
                        + CollectorObj.getCantidad());

                //On envoie les packets
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P, PersoObj);
                String str = "O-" + id;
                GestorSalida.GAME_SEND_EsK_PACKET(P, str);
            } else
            //S'il reste des this.objects
            {
                //On retire X objet
                CollectorObj.setCantidad(newQua);
                //On ajoute X this.objects
                PersoObj.setCantidad(PersoObj.getCantidad() + qua);

                //On envoie les packets
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P, PersoObj);
                String str = "O+" + CollectorObj.getId() + "|"
                        + CollectorObj.getCantidad() + "|"
                        + CollectorObj.getModelo().getId() + "|"
                        + CollectorObj.parseStatsString();
                GestorSalida.GAME_SEND_EsK_PACKET(P, str);
            }
        }
        GestorSalida.GAME_SEND_Ow_PACKET(P);
        Database.dinamicos.getPlayerData().update(P);
    }

    public synchronized boolean addDefenseFight(Jugador player) {
        if (!(player.getPelea() == null && !player.isAway() && !player.isInPrison() && player.getExchangeAction() == null))
            return false;

        for (Jugador p : this.getDefenseFight().values()) {
            if (player.getAccount() != null && p != null && p.getAccount() != null) {
                if (player.getAccount().getCurrentIp().compareTo(p.getAccount().getCurrentIp()) == 0) {
                    GestorSalida.GAME_SEND_MESSAGE(player, "Impossible de rejoindre ce combat, vous êtes déjà dans le combat avec une même IP !");
                    return false;
                }
            }
        }

        if (this.defenserId.size() >= Mundo.mundo.getMap(getMap()).getMaxTeam()) {
            return false;
        } else {
            this.defenserId.put(player.getId(), player);
            return true;
        }
    }

    public synchronized boolean delDefenseFight(Jugador P) {
        if (this.defenserId.containsKey(P.getId())) {
            this.defenserId.remove(P.getId());
            return true;
        }
        return false;
    }

    public void clearDefenseFight() {
        this.defenserId.clear();
    }

    public java.util.Map<Integer, Jugador> getDefenseFight() {
        return this.defenserId;
    }

    public Collection<ObjetoJuego> getDrops() {
        return this.objects.values();
    }
}