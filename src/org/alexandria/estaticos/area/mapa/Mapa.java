package org.alexandria.estaticos.area.mapa;

import org.alexandria.otro.utilidad.Temporizador;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.alexandria.estaticos.area.SubArea;
import org.alexandria.estaticos.area.mapa.laberintos.Minotot;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.estaticos.*;
import org.alexandria.estaticos.Mision.*;
import org.alexandria.estaticos.cliente.Jugador.Grupo;
import org.alexandria.comunes.Camino;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.*;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.estaticos.Recaudador;
import org.alexandria.estaticos.Monstruos;
import org.alexandria.estaticos.Npc;
import org.alexandria.estaticos.juego.accion.AccionIntercambiar;
import org.alexandria.estaticos.juego.accion.AccionJuego;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.juego.planificador.Updatable;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.oficio.OficioConstantes;
import org.alexandria.estaticos.oficio.magueo.RomperObjetos;
import org.alexandria.otro.Accion;
import org.alexandria.otro.Dopeul;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.arena.DeathMatch;
import org.alexandria.estaticos.pelea.arena.TeamMatch;
import org.alexandria.otro.utilidad.Doble;
import org.alexandria.estaticos.Npc.*;

import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Mapa {

    public static final Map<String, ArrayList<ObjetoJuego>> fixMobGroupObjects = new HashMap<>();
    public static final Updatable updatable = new Updatable(30000) {
        private final ArrayList<RespawnGroup> groups = new ArrayList<>();

        @Override
        public void update() {
            if(!this.groups.isEmpty()) {
                long time = Instant.now().toEpochMilli(), random = Formulas.getRandomValue(120000, 300000);

                for(RespawnGroup respawnGroup : new ArrayList<>(this.groups)) {
                    if(respawnGroup.cell != -1) {
                        Map<String, String> data = Mundo.mundo.getGroupFix(respawnGroup.map.id, respawnGroup.cell);

                        if(time - respawnGroup.lastTime > Long.parseLong(data.get("timer"))) {
                            respawnGroup.map.addStaticGroup(respawnGroup.cell, data.get("groupData"), true);
                            this.groups.remove(respawnGroup);
                        }
                    } else if(time - respawnGroup.lastTime > random) {
                        respawnGroup.map.spawnGroup(-1, 1, true, -1);
                        this.groups.remove(respawnGroup);
                    }
                }
            }

            if(this.verify()) {
                if(Configuracion.INSTANCE.getAUTO_REBOOT()) {
                    if (Reinicio.check()) {
                        if ((Instant.now().toEpochMilli() - Configuracion.INSTANCE.getStartTime()) > 60000) {
                            for (Jugador player : Mundo.mundo.getOnlinePlayers()) player.send(this.toString());
                            try { Thread.sleep(5000); } catch (Exception ignored) {}
                            MainServidor.INSTANCE.stop("Automatic restart");
                        }
                    }
                }

                //Movimiento de dragopavos en cercados y reaccion a emotes TimerWaitingPlus
                Temporizador.addSiguiente(() -> {
                    //Lista de mapas de jugadores on para mover moobs
                    ArrayList<Short> mapas = new ArrayList<>();
                    for(Jugador player: Mundo.mundo.getOnlinePlayers()){
                        Mapa map = player.getCurMap();
                        if(!mapas.contains(map.id)){
                            map.onMapMonsterDeplacement();
                            if (map.getMountPark() != null) map.getMountPark().startMoveMounts();
                            mapas.add(map.id);
                        }
                    }
                    Mundo.mundo.getCollectors().values().forEach(Recaudador::moveOnMap);
                }, 0, TimeUnit.SECONDS, Temporizador.DataType.MAPA);
                    NpcMobil.moveAll();

                for(Jugador jugador : Mundo.mundo.getOnlinePlayers())
                {
                    if (jugador.getLastPacketTime() + Configuracion.INSTANCE.getIdletiempo() < Instant.now().toEpochMilli())
                    {

                        if(jugador.getAccount().getGameClient() != null && jugador.isOnline())
                        {
                            Mundo.mundo.logger.debug("Se ha desconectado a :" + jugador.getName() + " por inactividad");
                            GestorSalida.MESSAGE_BOX(jugador.getAccount().getGameClient(),"01|");
                            jugador.getAccount().getGameClient().kick();
                        }
                    }
                }
            }
        }

        @Override
        public ArrayList<RespawnGroup> get() {
            return groups;
        }
    };

    public int nextObjectId = -1;
    public boolean noMarchand = false, noCollector = false, noPrism = false, noTP = false, noDefie = false, noAgro = false, noCanal = false;
    private final short id;
    private String date;
    private final String key;
    private String placesStr;
    private final byte w;
    private final byte h;
    private byte X = 0;
    private byte Y = 0;
    private byte maxGroup = 3;
    private byte maxSize;
    private byte minSize;
    private byte fixSize;
    private int maxTeam = 0;
    private boolean isMute = false;
    private SubArea subArea;
    private Cercados mountPark;
    private CeldaCacheImplementar cellCache;
    private List<GameCase> cases = new ArrayList<>();
    private List<Pelea> fights = new ArrayList<>();
    private ArrayList<Monstruos.MobGrade> mobPossibles = new ArrayList<>();
    private final Map<Integer, Monstruos.MobGroup> mobGroups = new HashMap<>();
    private final Map<Integer, Monstruos.MobGroup> fixMobGroups = new HashMap<>();
    private final Map<Integer, Npc> npcs = new HashMap<>();
    private final Map<Integer, ArrayList<Accion>> endFightAction = new HashMap<>();
    private final Map<Integer, Integer> mobExtras = new HashMap<>();

    public Mapa(short id, String date, byte w, byte h, String key, String places, String dData, String monsters, String mapPos, byte maxGroup, byte fixSize, byte minSize, byte maxSize, String forbidden, byte sniffed) {
        this.id = id;
        this.date = date;
        this.w = w;
        this.h = h;
        this.key = key;
        this.placesStr = places;
        this.maxGroup = maxGroup;
        this.maxSize = maxSize;
        this.minSize = minSize;
        this.fixSize = fixSize;
        this.cases = Mundo.mundo.getCryptManager().decompileMapData(this, dData, sniffed);

        try {
            if (!places.equalsIgnoreCase("") && !places.equalsIgnoreCase("|"))
                this.maxTeam = (places.split("\\|")[1].length() / 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String[] mapInfos = mapPos.split(",");
            this.X = Byte.parseByte(mapInfos[0]);
            this.Y = Byte.parseByte(mapInfos[1]);
            int subArea = Integer.parseInt(mapInfos[2]);

            if (subArea == 0 && id == 32) {
                this.subArea = Mundo.mundo.getSubArea(subArea);
                if (this.subArea != null) this.subArea.addMap(this);
            } else if (subArea != 0) {
                this.subArea = Mundo.mundo.getSubArea(subArea);
                if (this.subArea != null) this.subArea.addMap(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            MainServidor.INSTANCE.stop("GameMap1");
        }

        try {
            String[] split = forbidden.split(";");
            noMarchand = split[0].equals("1");
            noCollector = split[1].equals("1");
            noPrism = split[2].equals("1");
            noTP = split[3].equals("1");
            noDefie = split[4].equals("1");
            noAgro = split[5].equals("1");
            noCanal = split[6].equals("1");
        } catch (Exception ignored) {}

        String unique = "";
        if(monsters.contains("@")) {
            String[] split = monsters.split("@");
            unique = split[0];
            monsters = split[1];
        }

        for (String mob : monsters.split("\\|")) {
            if (mob.equals("")) continue;
            int id1, lvl;
            try {
                id1 = Integer.parseInt(mob.split(",")[0]);
                lvl = Integer.parseInt(mob.split(",")[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                continue;
            }
            if (id1 == 0 || lvl == 0)
                continue;
            if (Mundo.mundo.getMonstre(id1) == null)
                continue;
            if (Mundo.mundo.getMonstre(id1).getGradeByLevel(lvl) == null)
                continue;
            if (Configuracion.INSTANCE.getHALLOWEEN()) {
                switch (id1) {
                    case 98://Tofu
                        if (Mundo.mundo.getMonstre(794) != null)
                            if (Mundo.mundo.getMonstre(794).getGradeByLevel(lvl) != null)
                                id1 = 794;
                        break;
                    case 101://Bouftou
                        if (Mundo.mundo.getMonstre(793) != null)
                            if (Mundo.mundo.getMonstre(793).getGradeByLevel(lvl) != null)
                                id1 = 793;
                        break;
                }
            }

            boolean pass = false;
            for(Monstruos.MobGrade grade : this.mobPossibles) {
                if(unique.contains(String.valueOf(grade.getTemplate().getId())) && id1 == grade.getTemplate().getId()) {
                    pass = true;
                    break;
                }
            }
            if(!pass) {
                this.mobPossibles.add(Mundo.mundo.getMonstre(id1).getGradeByLevel(lvl));
            }
        }
    }

    public Mapa(short id, String date, byte w, byte h, String key, String places) {
        this.id = id;
        this.date = date;
        this.w = w;
        this.h = h;
        this.key = key;
        this.placesStr = places;
        this.cases = new ArrayList<>();
    }

    public Mapa(short id, String date, byte w, byte h, String key,
                String places, byte x, byte y, byte maxGroup, byte fixSize,
                byte minSize, byte maxSize) {
        this.id = id;
        this.date = date;
        this.w = w;
        this.h = h;
        this.key = key;
        this.placesStr = places;
        this.X = x;
        this.Y = y;
        this.maxGroup = maxGroup;
        this.maxSize = maxSize;
        this.minSize = minSize;
        this.fixSize = fixSize;
    }

    public void setCellCache(CeldaCacheImplementar cache) {
        this.cellCache = cache;
    }

    public CeldaCacheImplementar getCellCache() {
        return this.cellCache;
    }

    public static void removeMountPark(int guildId) {
        try {
            Mundo.mundo.getMountPark().values().stream().filter(park -> park.getGuild() != null).filter(park -> park.getGuild().getId() == guildId).forEach(park -> {
                if (!park.getListOfRaising().isEmpty()) {
                    for (Integer id : new ArrayList<>(park.getListOfRaising())) {
                        if (Mundo.mundo.getMountById(id) == null) {
                            park.delRaising(id);
                            continue;
                        }
                        Mundo.mundo.removeMount(id);
                        Database.dinamicos.getMountData().delete(id);
                    }
                    park.getListOfRaising().clear();
                }
                if (!park.getEtable().isEmpty()) {
                    for (Montura mount : new ArrayList<>(park.getEtable())) {
                        if (mount == null) continue;
                        Mundo.mundo.removeMount(mount.getId());
                        Database.dinamicos.getMountData().delete(mount.getId());
                    }
                    park.getEtable().clear();
                }

                park.setOwner(0);
                park.setGuild(null);
                park.setPrice(3000000);
                Database.estaticos.getMountParkData().update(park);

                for (Jugador p : park.getMap().getPlayers())
                    GestorSalida.GAME_SEND_Rp_PACKET(p, park);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getObjResist(Jugador perso, int cellid, int itemID) {
        Cercados MP = perso.getCurMap().getMountPark();
        StringBuilder packets = new StringBuilder();
        if (MP == null || MP.getObject().size() == 0)
            return 0;
        for (Entry<Integer, Map<Integer, Integer>> entry : MP.getObjDurab().entrySet()) {
            for (Entry<Integer, Integer> entry2 : entry.getValue().entrySet()) {
                if (cellid == entry.getKey())
                    packets.append(entry.getKey()).append(";").append(entry2.getValue()).append(";").append(entry2.getKey());
            }
        }
        int cell, durability, durabilityMax;
        try {
            String[] infos = packets.toString().split(";");
            cell = Integer.parseInt(infos[0]);
            if (itemID == 7798 || itemID == 7605 || itemID == 7606 || itemID == 7625 || itemID == 7628 || itemID == 7634) {
                durability = Integer.parseInt(infos[1]);
            } else {
                durability = Integer.parseInt(infos[1]) - 1;
            }
            durabilityMax = Integer.parseInt(infos[2]);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

        if (durability <= 0) {
            //if (MP.delObject(cell)) {
                durability = 0;
                Map<Integer, Integer> InDurab = new HashMap<>();
                InDurab.put(durabilityMax, durability);
                MP.getObjDurab().put(cell, InDurab);
                GestorSalida.SEND_GDO_PUT_OBJECT_MOUNT(perso.getCurMap(), cell
                        + ";" + itemID + ";1;" + durability + ";" + durabilityMax);
                return 0;
            //}
        } else {
            Map<Integer, Integer> InDurab = new HashMap<>();
            InDurab.put(durabilityMax, durability);
            MP.getObjDurab().put(cell, InDurab);
            GestorSalida.SEND_GDO_PUT_OBJECT_MOUNT(perso.getCurMap(), cell
                    + ";" + itemID + ";1;" + durability + ";" + durabilityMax);
        }
        return durabilityMax;
    }

    public static int getObjResist(Cercados MP, int cellid, int itemID) {
        StringBuilder packets = new StringBuilder();
        if (MP == null || MP.getObject().size() == 0)
            return 0;
        for (Entry<Integer, Map<Integer, Integer>> entry : MP.getObjDurab().entrySet()) {
            for (Entry<Integer, Integer> entry2 : entry.getValue().entrySet()) {
                if (cellid == entry.getKey())
                    packets.append(entry.getKey()).append(";").append(entry2.getValue()).append(";").append(entry2.getKey());
            }
        }
        String[] infos = packets.toString().split(";");
        int cell = Integer.parseInt(infos[0]), durability;
        if (itemID == 7798 || itemID == 7605 || itemID == 7606
                || itemID == 7625 || itemID == 7628 || itemID == 7634) {
            durability = Integer.parseInt(infos[1]);
        } else {
            durability = Integer.parseInt(infos[1]) - 1;
        }
        int durabilityMax = Integer.parseInt(infos[2]);

        if (durability <= 0) {
            //if (MP.delObject(cell)) {
            durability = 0;
            Map<Integer, Integer> InDurab = new HashMap<>();
            InDurab.put(durabilityMax, durability);
            MP.getObjDurab().put(cell, InDurab);
            GestorSalida.SEND_GDO_PUT_OBJECT_MOUNT(MP.getMap(), cell
                    + ";" + itemID + ";1;" + durability + ";" + durabilityMax);
            return 0;
            //}
        } else {
            Map<Integer, Integer> InDurab = new HashMap<>();
            InDurab.put(durabilityMax, durability);
            MP.getObjDurab().put(cell, InDurab);
            GestorSalida.SEND_GDO_PUT_OBJECT_MOUNT(MP.getMap(), cell + ";"
                    + itemID + ";1;" + durability + ";" + durabilityMax);
        }
        return durabilityMax;
    }

    public void setInfos(String date, String monsters, String mapPos,
                         byte maxGroup, byte fixSize, byte minSize, byte maxSize,
                         String forbidden) {
        this.date = date;
        this.mobPossibles.clear();

        try {
            String[] split = forbidden.split(";");
            noMarchand = split[0].equals("1");
            noCollector = split[1].equals("1");
            noPrism = split[2].equals("1");
            noTP = split[3].equals("1");
            noDefie = split[4].equals("1");
            noAgro = split[5].equals("1");
            noCanal = split[6].equals("1");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String unique = "";
        if(monsters.contains("@")) {
            String[] split = monsters.split("@");
            unique = split[0];
            monsters = split[1];
        }

        for (String mob : monsters.split("\\|")) {
            if (mob.equals(""))
                continue;
            int id1, lvl;
            try {
                id1 = Integer.parseInt(mob.split(",")[0]);
                lvl = Integer.parseInt(mob.split(",")[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                continue;
            }
            if (id1 == 0 || lvl == 0)
                continue;
            if (Mundo.mundo.getMonstre(id1) == null)
                continue;
            if (Mundo.mundo.getMonstre(id1).getGradeByLevel(lvl) == null)
                continue;
            boolean pass = false;
            for(Monstruos.MobGrade grade : this.mobPossibles) {
                if(unique.contains(String.valueOf(grade.getTemplate().getId())) && id1 == grade.getTemplate().getId()) {
                    pass = true;
                    break;
                }
            }
            if(!pass) {
                this.mobPossibles.add(Mundo.mundo.getMonstre(id1).getGradeByLevel(lvl));
            }
        }
        try {
            String[] mapInfos = mapPos.split(",");
            this.X = Byte.parseByte(mapInfos[0]);
            this.Y = Byte.parseByte(mapInfos[1]);
            int subArea = Integer.parseInt(mapInfos[2]);
            if (subArea == 0 && id == 32) {
                this.subArea = Mundo.mundo.getSubArea(subArea);
                if (this.subArea != null)
                    this.subArea.addMap(this);
            } else if (subArea != 0) {
                this.subArea = Mundo.mundo.getSubArea(subArea);
                if (this.subArea != null)
                    this.subArea.addMap(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            MainServidor.INSTANCE.stop("GameMap2");
        }
        this.maxGroup = maxGroup;
        this.maxSize = maxSize;
        this.minSize = minSize;
        this.fixSize = fixSize;
    }

    public void addMobExtra(Integer id, Integer chances) {
        this.mobExtras.put(id, chances);
    }

    public void setGs(byte maxGroup, byte minSize, byte fixSize, byte maxSize) {
        this.maxGroup = maxGroup;
        this.maxSize = maxSize;
        this.minSize = minSize;
        this.fixSize = fixSize;
    }

    public ArrayList<Monstruos.MobGrade> getMobPossibles() {
        return this.mobPossibles;
    }

    public void setMobPossibles(String monsters) {
        if (monsters == null || monsters.equals(""))
            return;

        this.mobPossibles = new ArrayList<>();

        for (String mob : monsters.split("\\|")) {
            if (mob.equals(""))
                continue;
            int id1, lvl;
            try {
                id1 = Integer.parseInt(mob.split(",")[0]);
                lvl = Integer.parseInt(mob.split(",")[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                continue;
            }
            if (id1 == 0 || lvl == 0)
                continue;
            if (Mundo.mundo.getMonstre(id1) == null)
                continue;
            if (Mundo.mundo.getMonstre(id1).getGradeByLevel(lvl) == null)
                continue;
            if (Configuracion.INSTANCE.getHALLOWEEN()) {
                switch (id1) {
                    case 98://Tofu
                        if (Mundo.mundo.getMonstre(794) != null)
                            if (Mundo.mundo.getMonstre(794).getGradeByLevel(lvl) != null)
                                id1 = 794;
                        break;
                    case 101://Bouftou
                        if (Mundo.mundo.getMonstre(793) != null)
                            if (Mundo.mundo.getMonstre(793).getGradeByLevel(lvl) != null)
                                id1 = 793;
                        break;
                }
            }

            this.mobPossibles.add(Mundo.mundo.getMonstre(id1).getGradeByLevel(lvl));
        }
    }

    public byte getMaxSize() {
        return this.maxSize;
    }

    public byte getMinSize() {
        return this.minSize;
    }

    public byte getFixSize() {
        return this.fixSize;
    }

    public short getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public byte getW() {
        return w;
    }

    public byte getH() {
        return h;
    }

    public String getKey() {
        return key;
    }

    public String getPlaces() {
        return placesStr;
    }

    public void setPlaces(String place) {
        this.placesStr = place;
    }

    public List<GameCase> getCases() {
        return cases;
    }

    private void setCases(List<GameCase> cases) {
        this.cases = cases;
    }

    public GameCase getCase(int id) {
        for(GameCase gameCase : this.cases)
            if(gameCase.getId() == (id))
                return gameCase;
        return null;
    }

    public void removeCase(int id) {
        Iterator<GameCase> iterator = this.cases.iterator();

        while(iterator.hasNext()) {
            GameCase gameCase = iterator.next();
            if(gameCase != null && gameCase.getId() == id) {
                iterator.remove();
                break;
            }
        }
    }

    public Pelea newFight(Jugador init1, Jugador init2, int type) {
        if (init1.getPelea() != null || init2.getPelea() != null)
            return null;
        int id = 1;
        if(this.fights == null)
            this.fights = new ArrayList<>();
        if (!this.fights.isEmpty())
            id = ((Pelea) (this.fights.toArray()[this.fights.size() - 1])).getId() + 1;
        Pelea f = new Pelea(type, id, this, init1, init2);
        this.fights.add(f);
        GestorSalida.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);
        return f;
    }

    public void removeFight(int id) {
        if(this.fights != null) {
            Iterator<Pelea> iterator = this.getFights().iterator();
            while(iterator.hasNext()) {
                Pelea fight = iterator.next();
                if(fight != null && fight.getId() == id) {
                    iterator.remove();
                    break;
                }
            }

            if(this.fights.isEmpty()) this.fights = null;
        }
    }

    public int getNbrFight() {
        return fights == null ? 0 : this.fights.size();
    }

    public Pelea getFight(int id) {
        final Pelea[] fight = {null};

        if(this.fights != null)
            this.fights.stream().filter(all -> all.getId() == id).forEach(selected -> fight[0] = selected);
        
        return fight[0];
    }

    public List<Pelea> getFights() {
        if(this.fights == null)
            return new ArrayList<>();
        return fights;
    }

    public Map<Integer, Monstruos.MobGroup> getMobGroups() {
        return this.mobGroups;
    }

    public void removeNpcOrMobGroup(int id) {
        this.npcs.remove(id);
        this.mobGroups.remove(id);
    }

    public Npc addNpc(int npcID, int cellID, int dir) {
        NpcModelo temp = Mundo.mundo.getNPCTemplate(npcID);
        if (temp == null)
            return null;
        if (getCase(cellID) == null)
            return null;
        Npc npc;
        if(temp.getPath().isEmpty())
            npc = new Npc(this.nextObjectId, cellID, (byte) dir, temp);
        else
            npc = new NpcMobil(this.nextObjectId, cellID, (byte) dir, this.id, temp);
        this.npcs.put(this.nextObjectId, npc);
        this.nextObjectId--;
        return npc;
    }

    public Map<Integer, Npc> getNpcs() {
        return this.npcs;
    }

    public Npc getNpc(int id) {
        return this.npcs.get(id);
    }

    public void RemoveNpc(int id) {
        this.npcs.remove(id);
    }

    public void applyEndFightAction(Jugador player) {
        if (this.endFightAction.get(player.needEndFight()) == null)
            return;
        if (this.id ==  8545) {
            if (player.getCurCell().getId() <= 193 && player.getCurCell().getId() != 186 && player.getCurCell().getId() != 187 && player.getCurCell().getId() != 173 && player.getCurCell().getId() != 172 && player.getCurCell().getId() != 144 && player.getCurCell().getId() != 158) {
                for (Accion A : this.endFightAction.get(player.needEndFight())) {
                    A.apply(player, null, -1, -1);
                }
            } else {
                for (Accion A : this.endFightAction.get(player.needEndFight())) {
                    A.setArgs("8547,214");
                    A.apply(player, null, -1, -1);
                }
            }
        } else {
            for (Accion A : this.endFightAction.get(player.needEndFight()))
                A.apply(player, null, -1, -1);
        }
        player.setNeededEndFight(-1, null);
    }

    public boolean hasEndFightAction(int type) {
        return this.endFightAction.get(type) != null;
    }

    public void addEndFightAction(int type, Accion A) {
        this.endFightAction.computeIfAbsent(type, k -> new ArrayList<>()); // On retire l'action si elle existait d�j�
        delEndFightAction(type, A.getId());
        this.endFightAction.get(type).add(A);
    }

    public void delEndFightAction(int type, int aType) {
        if (this.endFightAction.get(type) != null)
            new ArrayList<>(this.endFightAction.get(type)).stream().filter(A -> A.getId() == aType).forEach(A -> this.endFightAction.get(type).remove(A));
    }

    public void delAllEndFightAction() {
        this.endFightAction.clear();
    }

    public int getX() {
        return this.X;
    }

    public int getY() {
        return this.Y;
    }

    public SubArea getSubArea() {
        return this.subArea;
    }

    public Cercados getMountPark() {
        return this.mountPark;
    }

    public void setMountPark(Cercados mountPark) {
        this.mountPark = mountPark;
    }

    public int getMaxGroupNumb() {
        return this.maxGroup;
    }

    public int getMaxTeam() {
        return this.maxTeam;
    }

    public boolean containsForbiddenCellSpawn(int id) {
        if(this.mountPark != null)
            return this.mountPark.getCellAndObject().containsKey(id);
        return false;
    }

    public Mapa getMapCopy() {
        List<GameCase> cases = new ArrayList<>();

        Mapa map = new Mapa(id, date, w, h, key, placesStr);

        for (GameCase gameCase : this.cases) {
            if (map.getId() == 8279) {
                switch (gameCase.getId()) {
                    case 187:
                    case 170:
                    case 156:
                    case 142:
                    case 128:
                    case 114:
                    case 100:
                    case 86:
                        continue;
                }
            }

            cases.add(new GameCase(map, gameCase.getId(), gameCase.isWalkable(true, true, -1), gameCase.isLoS(), (gameCase.getObject() == null ? -1 : gameCase.getObject().getId())));
        }
        map.setCases(cases);
        return map;
    }

    public Mapa getMapCopyIdentic() {
        Mapa map = new Mapa(id, date, w, h, key, placesStr, X, Y, maxGroup, fixSize, minSize, maxSize);
        List<GameCase> cases = this.cases.stream().map(entry -> new GameCase(map, entry.getId(), entry.isWalkable(false), entry.isLoS(), (entry.getObject() == null ? -1 : entry.getObject().getId()))).collect(Collectors.toList());
        map.setCases(cases);
        return map;
    }

    public void addPlayer(Jugador perso) {
        GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(this, perso);
        perso.getCurCell().addPlayer(perso);
        if (perso.getEnergy() > 0) {
            if (perso.getEnergy() >= 10000)
                return;
            if (Constantes.isTaverne(this) && perso.getTimeTaverne() == 0) {
                perso.setTimeTaverne(Instant.now().toEpochMilli());
            } else if (perso.getTimeTaverne() != 0) {
                int gain = (int) ((Instant.now().toEpochMilli() - perso.getTimeTaverne()) / 1000);
                if(gain >= 10000) gain = 10000 - perso.getEnergy();
                perso.setEnergy(perso.getEnergy() + gain);
                if (perso.getEnergy() >= 10000) perso.setEnergy(10000);
                GestorSalida.GAME_SEND_Im_PACKET(perso, "092;" + gain);
                GestorSalida.GAME_SEND_STATS_PACKET(perso);
                perso.setTimeTaverne(0);
            }
        }
    }

    public ArrayList<Jugador> getPlayers() {
        ArrayList<Jugador> player = new ArrayList<>();
        for (GameCase c : cases)
            player.addAll(new ArrayList<>(c.getPlayers()));
        return player;
    }

    public void sendFloorItems(Jugador perso) {
        this.cases.stream().filter(c -> c.getDroppedItem(false) != null).forEach(c -> GestorSalida.GAME_SEND_GDO_PACKET(perso, '+', c.getId(), c.getDroppedItem(false).getModelo().getId(), 0));
    }

    public void delAllDropItem() {
        for (GameCase gameCase : this.cases) {
            GestorSalida.GAME_SEND_GDO_PACKET_TO_MAP(this, '-', gameCase.getId(), 0, 0);
            gameCase.clearDroppedItem();
        }
    }

    public int getStoreCount() {
        if (Mundo.mundo.getSeller(this.getId()) == null)
            return 0;
        return Mundo.mundo.getSeller(this.getId()).size();
    }

    public boolean haveMobFix() {
        return this.fixMobGroups.size() > 0;
    }

    public boolean isPossibleToPutMonster() {
        return !this.cases.isEmpty() && this.maxGroup > 0 && this.mobPossibles.size() > 0;
    }

    public boolean loadExtraMonsterOnMap(int idMob) {
        if (Mundo.mundo.getMonstre(idMob) == null)
            return false;
        Monstruos.MobGrade grade = Mundo.mundo.getMonstre(idMob).getRandomGrade();
        int cell = this.getRandomFreeCellId();

        Monstruos.MobGroup group = new Monstruos.MobGroup(this.nextObjectId, Constantes.ALINEAMIENTO_NEUTRAL, this.mobPossibles, this, cell, this.fixSize, this.maxSize, this.maxSize, grade);
        if (group.getMobs().isEmpty())
            return false;
        this.mobGroups.put(this.nextObjectId, group);
        this.nextObjectId--;
        return true;
    }

    public void loadMonsterOnMap() {
        if (maxGroup == 0)
            return;
        spawnGroup(Constantes.ALINEAMIENTO_NEUTRAL, this.maxGroup, false, -1);//Spawn des groupes d'alignement neutre
        spawnGroup(Constantes.ALINEAMIENTO_BONTARIANO, 1, false, -1);//Spawn du groupe de gardes bontarien s'il y a
        spawnGroup(Constantes.ALINEAMIENTO_BRAKMARIANO, 1, false, -1);//Spawn du groupe de gardes brakmarien s'il y a
    }

    public void mute() {
        this.isMute = !this.isMute;
    }

    public boolean isMute() {
        return this.isMute;
    }

    public boolean isAggroByMob(Jugador perso, int cell) {
        if (placesStr.equalsIgnoreCase("|"))
            return false;
        if (perso.getCurMap().getId() != id || !perso.canAggro())
            return false;
        for (Monstruos.MobGroup group : this.mobGroups.values()) {
            if (perso.get_align() == 0 && group.getAlignement() > 0)
                continue;
            if (perso.get_align() == 1 && group.getAlignement() == 1)
                continue;
            if (perso.get_align() == 2 && group.getAlignement() == 2)
                continue;

            if (this.subArea != null) {
                group.setSubArea(this.subArea.getId());
                group.changeAgro();
            }
            if (Camino.getDistanceBetween(this, cell, group.getCellId()) <= group.getAggroDistance()
                    && group.getAggroDistance() > 0)//S'il y aggro
            {
                if (Mundo.mundo.getConditionManager().validConditions(perso, group.getCondition()))
                    return true;
            }
        }
        return false;
    }

    public void spawnAfterTimeGroup() {
        ((ArrayList<RespawnGroup>) updatable.get()).add(new RespawnGroup(this, -1, Instant.now().toEpochMilli()));
    }

    public void spawnAfterTimeGroupFix(final int cell) {
        ((ArrayList<RespawnGroup>) updatable.get()).add(new RespawnGroup(this, cell, Instant.now().toEpochMilli()));
    }

    private static class RespawnGroup {

        private final Mapa map;
        private final int cell;
        private final long lastTime;

        public RespawnGroup(Mapa map, int cell, long lastTime) {
            this.map = map;
            this.cell = cell;
            this.lastTime = lastTime;
        }
    }

    public void spawnGroup(int align, int nbr, boolean log, int cellID) {
        if (nbr < 1)
            return;
        if (this.mobGroups.size() + this.fixMobGroups.size() >= this.maxGroup)
            return;
        for (int a = 1; a <= nbr; a++) {
            // mobExtras
            ArrayList<Monstruos.MobGrade> mobPoss = new ArrayList<>(this.mobPossibles);
            if (!this.mobExtras.isEmpty()) {
                for (Entry<Integer, Integer> entry : this.mobExtras.entrySet()) {
                    if (entry.getKey() == 499) // Si c'est un minotoboule de nowel
                        if (!Configuracion.INSTANCE.getNOEL()) // Si ce n'est pas nowel
                            continue;
                    int random = Formulas.getRandomValue(0, 99);
                    while (entry.getValue() > random) {
                        Monstruos mob = Mundo.mundo.getMonstre(entry.getKey());
                        if (mob == null)
                            continue;
                        Monstruos.MobGrade mobG = mob.getRandomGrade();
                        if (mobG == null)
                            continue;
                        mobPoss.add(mobG);
                        if (entry.getKey() == 422 || entry.getKey() == 499) // un seul DDV / Minotoboule
                            break;
                        random = Formulas.getRandomValue(0, 99);
                    }
                }
            }

            while (this.mobGroups.get(this.nextObjectId) != null || this.npcs.get(this.nextObjectId) != null)
                this.nextObjectId--;

            Monstruos.MobGroup group = new Monstruos.MobGroup(this.nextObjectId, align, mobPoss, this, cellID, this.fixSize, this.minSize, this.maxSize, null);

            if (group.getMobs().isEmpty())
                continue;
            this.mobGroups.put(this.nextObjectId, group);
            if (log)
                GestorSalida.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);
            this.nextObjectId--;
        }
    }

    public void respawnGroup(Monstruos.MobGroup group) {
        this.mobGroups.put(group.getId(), group);
        GestorSalida.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);
    }

    public void spawnGroupWith(Monstruos m) {
        while (this.mobGroups.get(this.nextObjectId) != null || this.npcs.get(this.nextObjectId) != null)
            this.nextObjectId--;
        Monstruos.MobGrade _m = null;
        while (_m == null)
            _m = m.getRandomGrade();
        int cell = this.getRandomFreeCellId();
        while (this.containsForbiddenCellSpawn(cell))
            cell = this.getRandomFreeCellId();

        Monstruos.MobGroup group = new Monstruos.MobGroup(this.nextObjectId, -1, this.mobPossibles, this, cell, this.fixSize, this.minSize, this.maxSize, _m);
        group.setIsFix(false);
        this.mobGroups.put(this.nextObjectId, group);
        GestorSalida.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);
        this.nextObjectId--;
    }

    public void spawnNewGroup(boolean timer, int cellID, String groupData, String condition) {
        while (this.mobGroups.get(this.nextObjectId) != null || this.npcs.get(this.nextObjectId) != null)
        this.nextObjectId--;
        while (this.containsForbiddenCellSpawn(cellID))
            cellID = this.getRandomFreeCellId();

        Monstruos.MobGroup group = new Monstruos.MobGroup(this.nextObjectId, cellID, groupData);
        if (group.getMobs().isEmpty())
            return;
        this.mobGroups.put(this.nextObjectId, group);
        group.setCondition(condition);
        group.setIsFix(false);
        GestorSalida.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);
        this.nextObjectId--;
        if (timer)
            group.startCondTimer();
    }

    public void spawnGroupOnCommand(int cellID, String groupData, boolean send) {
        while (this.mobGroups.get(this.nextObjectId) != null || this.npcs.get(this.nextObjectId) != null)
            this.nextObjectId--;
        Monstruos.MobGroup group = new Monstruos.MobGroup(this.nextObjectId, cellID, groupData);
        if (group.getMobs().isEmpty())
            return;
        this.mobGroups.put(this.nextObjectId, group);
        group.setIsFix(false);
        if (send)
            GestorSalida.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);

        this.nextObjectId--;
    }

    public void addStaticGroup(int cellID, String groupData, boolean b) {
        while (this.mobGroups.get(this.nextObjectId) != null || this.npcs.get(this.nextObjectId) != null)
            this.nextObjectId--;
        Monstruos.MobGroup group = new Monstruos.MobGroup(this.nextObjectId, cellID, groupData);

        if (group.getMobs().isEmpty())
            return;
        this.mobGroups.put(this.nextObjectId, group);
        this.nextObjectId--;
        this.fixMobGroups.put(-1000 + this.nextObjectId, group);
        if (b)
            GestorSalida.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);
    }

    public void refreshSpawns() {
        for (int id : this.mobGroups.keySet()) {
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(this, id);
        }
        this.mobGroups.clear();
        this.mobGroups.putAll(this.fixMobGroups);
        for (Monstruos.MobGroup mg : this.fixMobGroups.values())
            GestorSalida.GAME_SEND_MAP_MOBS_GM_PACKET(this, mg);

        spawnGroup(Constantes.ALINEAMIENTO_NEUTRAL, this.maxGroup, true, -1);//Spawn des groupes d'alignement neutre
        spawnGroup(Constantes.ALINEAMIENTO_BONTARIANO, 1, true, -1);//Spawn du groupe de gardes bontarien s'il y a
        spawnGroup(Constantes.ALINEAMIENTO_BRAKMARIANO, 1, true, -1);//Spawn du groupe de gardes brakmarien s'il y a
    }

    public String getGMsPackets() {
        StringBuilder packet = new StringBuilder();
        cases.stream().filter(Objects::nonNull).forEach(cell -> cell.getPlayers().stream().filter(Objects::nonNull).forEach(player -> packet.append("GM|+").append(player.parseToGM()).append('\u0000')));
        return packet.toString();
    }

    public String getFightersGMsPackets(Pelea fight) {
        StringBuilder packet = new StringBuilder("GM");
        for (GameCase cell : this.cases)
            cell.getFighters().stream().filter(fighter -> fighter.getFight() == fight)
                    .forEach(fighter -> packet.append("|").append(fighter.getGmPacket('+', false)));
        return packet.toString();
    }

    public String getFighterGMPacket(Jugador player) {
        Peleador target = player.getPelea().getFighterByPerso(player);
        for (GameCase cell : this.cases)
            for(Peleador fighter : cell.getFighters())
                if(fighter.getFight() == player.getPelea() && fighter == target)
                    return "GM|" + fighter.getGmPacket('~', false);
        return "";
    }

    public String getMobGroupGMsPackets() {
        if (this.mobGroups.isEmpty())
            return "";

        StringBuilder packet = new StringBuilder();
        packet.append("GM|");
        boolean isFirst = true;
        for (Monstruos.MobGroup entry : this.mobGroups.values()) {
            String GM = entry.parseGM();
            if (GM.equals(""))
                continue;

            if (!isFirst)
                packet.append("|");

            packet.append(GM);
            isFirst = false;
        }
        return packet.toString();
    }

    public String getPrismeGMPacket() {
        String str = "";
        Collection<Prisma> prisms = Mundo.mundo.AllPrisme();
        if (prisms != null) {
            for (Prisma prism : prisms) {
                if (prism.getMap() == this.id) {
                    str = prism.getGMPrisme();
                    break;
                }
            }
        }
        return str;
    }

    public String getNpcsGMsPackets(Jugador p) {
        if (this.npcs.isEmpty())
            return "";

        StringBuilder packet = new StringBuilder();
        packet.append("GM|");
        boolean isFirst = true;
        for (Entry<Integer, Npc> entry : this.npcs.entrySet()) {
            String GM = entry.getValue().parse(false, p);
            if (GM.equals(""))
                continue;

            if (!isFirst)
                packet.append("|");

            packet.append(GM);
            isFirst = false;
        }
        return packet.toString();
    }

    public String getObjectsGDsPackets() {
        StringBuilder packet = new StringBuilder("GDF");
        this.cases.stream().filter(gameCase -> gameCase.getObject() != null)
                .forEach(gameCase -> packet.append("|").append(gameCase.getId()).append(";").append(gameCase.getObject().getState())
                        .append(";").append((gameCase.getObject().isInteractive() ? "1" : "0")));
        return packet.toString();
    }

    public void newDeathmatch(final Jugador init1, final Jugador init2, final DeathMatch type) {
        if (init1.getPelea() != null || init2.getPelea() != null) {
            return;
        }
        int id = 1;
        if (!this.fights.isEmpty()) {
            id = (int) this.fights.toArray()[this.fights.size() - 1] + 1;
        }
        final Pelea f = new Pelea(type, id, this, init1, init2);
        this.fights.add(f);
        GestorSalida.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);
    }

    public void newKolizeum(final TeamMatch type) {
        int id = 1;
        if (!this.fights.isEmpty()) {
            id = (int) this.fights.toArray()[this.fights.size() - 1] + 1;
        }
        final Pelea f = new Pelea(type, id, this);
        this.fights.add(f);
    }

    public void startFightVersusMonstres(Jugador player, Monstruos.MobGroup group) {
        if (player.getPelea() != null)
            return;
        if (player.isInAreaNotSubscribe()) {
            GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(player.getGameClient(), 'S');
            return;
        }
        if (this.placesStr.isEmpty() || this.placesStr.equals("|")) {
            player.sendMessage("Poste sur le forum dans la catégorie adéquat avec l'id de la map (/mapid dans le tchat) afin de pouvoir y mettre les cellules de combat. Merci.");
            return;
        }
        if (MainServidor.INSTANCE.getFightAsBlocked())
            return;
        if (player.isDead() == 1)
            return;
        if (player.get_align() == 0 && group.getAlignement() > 0)
            return;
        if (player.get_align() == 1 && group.getAlignement() == 1)
            return;
        if (player.get_align() == 2 && group.getAlignement() == 2)
            return;
        if (!player.canAggro())
            return;
        if(player.afterFight)
            return;
        if (!group.getCondition().equals(""))
            if (!Mundo.mundo.getConditionManager().validConditions(player, group.getCondition())) {
                GestorSalida.GAME_SEND_Im_PACKET(player, "119");
                return;
            }

        final Grupo party = player.getParty();

        if(party != null && party.getMaster() != null && !party.getMaster().getName().equals(player.getName()) && party.isWithTheMaster(player, false)) return;

        int id = 1;
        if(this.fights == null)
            this.fights = new ArrayList<>();
        if (!this.fights.isEmpty())
            id = ((Pelea) (this.fights.toArray()[this.fights.size() - 1])).getId() + 1;

        this.mobGroups.remove(group.getId());
        Pelea fight = new Pelea(id, this, player, group);
        this.fights.add(fight);
        GestorSalida.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);

        if(party != null && party.getMaster() != null && party.getMaster().getName().equals(player.getName())) {
            Temporizador.addSiguiente(() ->  party.getPlayers().stream().filter((follower) -> party.isWithTheMaster(follower,false)).forEach(follower -> {
                if(fight.getPrism()!=null)
                    fight.joinPrismFight(follower,(fight.getTeam0().containsKey(player.getId()) ? 0 : 1));
                else
                    fight.joinFight(follower,player.getId());
            }), 1, TimeUnit.SECONDS, Temporizador.DataType.CLIENTE);
        }
    }

    public void startFightVersusProtectors(Jugador player, Monstruos.MobGroup group) {
        if (MainServidor.INSTANCE.getFightAsBlocked() || player == null || player.getPelea() != null || player.isDead() == 1 || !player.canAggro())
            return;
        if (player.isInAreaNotSubscribe()) {
            GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(player.getGameClient(), 'S');
            return;
        }

        int id = 1;

        if (this.placesStr.isEmpty() || this.placesStr.equals("|")) {
            player.sendMessage("Poste sur le forum dans la catégorie adéquat avec l'id de la map (/mapid dans le tchat) afin de pouvoir y mettre les cellules de combat. Merci.");
            return;
        }
        if(this.fights == null)
            this.fights = new ArrayList<>();
        if (!this.fights.isEmpty())
            id = ((Pelea) (this.fights.toArray()[this.fights.size() - 1])).getId() + 1;
        this.fights.add(new Pelea(id, this, player, group, Constantes.FIGHT_TYPE_PVM));
        GestorSalida.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);
    }

    public void startFigthVersusDopeuls(Jugador perso, Monstruos.MobGroup group)//RaZoR
    {
        if (perso.getPelea() != null)
            return;
        if (perso.isInAreaNotSubscribe()) {
            GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(perso.getGameClient(), 'S');
            return;
        }
        int id = 1;
        if (perso.isDead() == 1)
            return;
        if (!perso.canAggro())
            return;
        if(this.fights == null)
            this.fights = new ArrayList<>();
        if (!this.fights.isEmpty())
            id = ((Pelea) (this.fights.toArray()[this.fights.size() - 1])).getId() + 1;
        this.fights.add(new Pelea(id, this, perso, group, Constantes.FIGHT_TYPE_DOPEUL));
        GestorSalida.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);
    }

    public void startFightVersusPercepteur(Jugador perso, Recaudador perco) {
        if (perso.getPelea() != null)
            return;
        if (perso.isInAreaNotSubscribe()) {
            GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(perso.getGameClient(), 'S');
            return;
        }
        if (MainServidor.INSTANCE.getFightAsBlocked())
            return;
        if (perso.isDead() == 1)
            return;
        if (!perso.canAggro())
            return;
        int id = 1;
        if(this.fights == null)
            this.fights = new ArrayList<>();
        if (!this.fights.isEmpty())
            id = ((Pelea) (this.fights.toArray()[this.fights.size() - 1])).getId() + 1;

        this.fights.add(new Pelea(id, this, perso, perco));
        GestorSalida.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);
    }

    public void startFightVersusPrisme(Jugador perso, Prisma Prisme) {
        if (perso.getPelea() != null)
            return;
        if (perso.isInAreaNotSubscribe()) {
            GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(perso.getGameClient(), 'S');
            return;
        }
        if (MainServidor.INSTANCE.getFightAsBlocked())
            return;
        if (perso.isDead() == 1)
            return;
        if (!perso.canAggro())
            return;
        int id = 1;
        if (!this.fights.isEmpty())
            id = ((Pelea) (this.fights.toArray()[this.fights.size() - 1])).getId() + 1;
        this.fights.add(new Pelea(id, this, perso, Prisme));
        GestorSalida.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);
    }

    public int getRandomFreeCellId() {
        ArrayList<Integer> freecell = new ArrayList<>();

        for (GameCase entry : cases) {
            if (entry == null)
                continue;
            if (!entry.isWalkable(true))
                continue;
            if (entry.getObject() != null)
                continue;
            if(this.id == 8279) {
                switch(entry.getId()) {
                    case 86:
                    case 100:
                    case 114:
                    case 128:
                    case 142:
                    case 156:
                    case 170:
                    case 184:
                    case 198:
                        continue;
                }
            }
            if (this.mountPark != null)
                if (this.mountPark.getCellOfObject().contains((int) entry.getId()))
                    continue;

            boolean ok = true;
            if (this.mobGroups != null)
                for (Monstruos.MobGroup mg : this.mobGroups.values())
                    if (mg != null)
                        if (mg.getCellId() == entry.getId())
                            ok = false;
            if (this.npcs != null)
                for (Npc npc : this.npcs.values())
                    if (npc != null)
                        if (npc.getCellId() == entry.getId())
                            ok = false;

            if (!ok || !entry.getPlayers().isEmpty())
                continue;
            freecell.add(entry.getId());
        }

        if (freecell.isEmpty())
            return -1;
        return freecell.get(Formulas.getRandomValue(0, freecell.size() - 1));
    }

    public int getRandomNearFreeCellId(int cellid)//obtenir une cell al�atoire et proche
    {
        ArrayList<Integer> freecell = new ArrayList<>();
        ArrayList<Integer> cases = new ArrayList<>();

        cases.add((cellid + 1));
        cases.add((cellid - 1));
        cases.add((cellid + 2));
        cases.add((cellid - 2));
        cases.add((cellid + 14));
        cases.add((cellid - 14));
        cases.add((cellid + 15));
        cases.add((cellid - 15));
        cases.add((cellid + 16));
        cases.add((cellid - 16));
        cases.add((cellid + 27));
        cases.add((cellid - 27));
        cases.add((cellid + 28));
        cases.add((cellid - 28));
        cases.add((cellid + 29));
        cases.add((cellid - 29));
        cases.add((cellid + 30));
        cases.add((cellid - 30));
        cases.add((cellid + 31));
        cases.add((cellid - 31));
        cases.add((cellid + 42));
        cases.add((cellid - 42));
        cases.add((cellid + 43));
        cases.add((cellid - 43));
        cases.add((cellid + 44));
        cases.add((cellid - 44));
        cases.add((cellid + 45));
        cases.add((cellid - 45));
        cases.add((cellid + 57));
        cases.add((cellid - 57));
        cases.add((cellid + 58));
        cases.add((cellid - 58));
        cases.add((cellid + 59));
        cases.add((cellid - 59));

        for (int entry : cases) {
            GameCase gameCase = this.getCase(entry);
            if (gameCase == null)
                continue;
            if(gameCase.getOnCellStopAction())
                continue;
            //Si la case n'est pas marchable
            if (!gameCase.isWalkable(true))
                continue;
            //Si la case est prise par un groupe de monstre
            boolean ok = true;
            for (Entry<Integer, Monstruos.MobGroup> mgEntry : this.mobGroups.entrySet())
                if (mgEntry.getValue().getCellId() == gameCase.getId()) {
                    ok = false;
                    break;
                }
            if (!ok)
                continue;
            //Si la case est prise par un npc
            ok = true;
            for (Entry<Integer, Npc> npcEntry : this.npcs.entrySet())
                if (npcEntry.getValue().getCellId() == gameCase.getId()) {
                    ok = false;
                    break;
                }
            if (!ok)
                continue;
            //Si la case est prise par un joueur
            if (!gameCase.getPlayers().isEmpty())
                continue;
            //Sinon
            freecell.add(gameCase.getId());
        }
        if (freecell.isEmpty())
            return -1;
        int rand = Formulas.getRandomValue(0, freecell.size() - 1);
        return freecell.get(rand);
    }

    public void onMapMonsterDeplacement() {
        if (getMobGroups().size() == 0)
            return;
        int RandNumb = Formulas.getRandomValue(1, getMobGroups().size());
        int i = 0;
        for (Monstruos.MobGroup group : getMobGroups().values()) {
            if(group.isFix() && this.id != 8279)
                continue;
            if (this.id == 8279) {// W:15   H:17
                final int cell1 = group.getCellId();
                final GameCase cell2 = this.getCase((cell1 - 15)), cell3 = this.getCase((cell1 - 15 + 1));
                final GameCase cell4 = this.getCase((cell1 + 15 - 1)),
                        cell5 = this.getCase((cell1 + 15));
                boolean case2 = (cell2 != null && (cell2.isWalkable(true) && (cell2.getPlayers().isEmpty())));
                boolean case3 = (cell3 != null && (cell3.isWalkable(true) && (cell3.getPlayers().isEmpty())));
                boolean case4 = (cell4 != null && (cell4.isWalkable(true) && (cell4.getPlayers().isEmpty())));
                boolean case5 = (cell5 != null && (cell5.isWalkable(true) && (cell5.getPlayers().isEmpty())));
                ArrayList<Boolean> array = new ArrayList<>();
                array.add(case2);
                array.add(case3);
                array.add(case4);
                array.add(case5);

                int count = 0;
                for (boolean bo : array)
                    if (bo)
                        count++;

                if (count == 0)
                    return;
                if (count == 1) {
                    GameCase newCell = (case2 ? cell2 : (case3 ? cell3 : (case4 ? cell4 : cell5)));
                    GameCase nextCell = null;
                    if (newCell == null)
                        return;

                    if (newCell.equals(cell2)) {
                        if (checkCell(newCell.getId() - 15)) {
                            nextCell = this.getCase(newCell.getId() - 15);
                            if (this.checkCell(nextCell.getId() - 15)) {
                                nextCell = this.getCase(nextCell.getId() - 15);
                            }
                        }
                    } else if (newCell.equals(cell3)) {
                        if (this.checkCell(newCell.getId() - 15 + 1)) {
                            nextCell = this.getCase(newCell.getId() - 15 + 1);
                            if (this.getCase(nextCell.getId() - 15 + 1) != null) {
                                nextCell = this.getCase(nextCell.getId() - 15 + 1);
                            }
                        }
                    } else if (newCell.equals(cell4)) {
                        if (this.checkCell(newCell.getId() + 15 - 1)) {
                            nextCell = this.getCase(newCell.getId() + 15 - 1);
                            if (this.checkCell(nextCell.getId() + 15 - 1)) {
                                nextCell = this.getCase(nextCell.getId() + 15 - 1);
                            }
                        }
                    } else if (newCell.equals(cell5)) {
                        if (this.checkCell(newCell.getId() + 15)) {
                            nextCell = this.getCase(newCell.getId() + 15);
                            if (this.checkCell(nextCell.getId() + 15)) {
                                nextCell = this.getCase(nextCell.getId() + 15);
                            }
                        }
                    }

                    String pathstr;
                    try {
                        assert nextCell != null;
                        pathstr = Camino.getShortestStringPathBetween(this, group.getCellId(), nextCell.getId(), 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    if (pathstr == null)
                        return;
                    group.setCellId(nextCell.getId());
                    for (Jugador z : getPlayers())
                        GestorSalida.GAME_SEND_GA_PACKET(z.getGameClient(), "0", "1", group.getId()
                                + "", pathstr);
                } else {
                    if (group.isFix())
                        continue;
                    i++;
                    if (i != RandNumb)
                        continue;

                    int cell = -1;
                    while (cell == -1 || cell == 383 || cell == 384
                            || cell == 398 || cell == 369)
                        cell = getRandomNearFreeCellId(group.getCellId());
                    String pathstr;
                    try {
                        pathstr = Camino.getShortestStringPathBetween(this, group.getCellId(), cell, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    if (pathstr == null)
                        return;
                    group.setCellId(cell);
                    for (Jugador z : getPlayers())
                        GestorSalida.GAME_SEND_GA_PACKET(z.getGameClient(), "0", "1", group.getId() + "", pathstr);
                }
            } else {
                if (group.isFix())
                    continue;
                i++;
                if (i != RandNumb)
                    continue;
                int cell = getRandomNearFreeCellId(group.getCellId());
                String pathstr;
                try {
                    pathstr = Camino.getShortestStringPathBetween(this, group.getCellId(), cell, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                if (pathstr == null)
                    return;
                group.setCellId(cell);
                for (Jugador z : getPlayers())
                    GestorSalida.GAME_SEND_GA_PACKET(z.getGameClient(), "0", "1", group.getId()
                            + "", pathstr);
            }

        }
    }

    public boolean checkCell(int id) {
        return this.getCase(id - 15) != null && this.getCase(id - 15).isWalkable(true);
    }

    public String getObjects() {
        if (this.mountPark == null || this.mountPark.getObject().size() == 0)
            return "";
        StringBuilder packets = new StringBuilder("GDO+");
        boolean first = true;
        for (Entry<Integer, Map<Integer, Integer>> entry : this.mountPark.getObject().entrySet()) {
            for (Entry<Integer, Integer> entry2 : entry.getValue().entrySet()) {
                if (!first)
                    packets.append("|");
                int cellidDurab = entry.getKey();
                packets.append(entry.getKey()).append(";").append(entry2.getKey()).append(";1;").append(getObjDurable(cellidDurab));
                first = false;
            }
        }
        return packets.toString();
    }

    public String getObjDurable(int CellID) {
        StringBuilder packets = new StringBuilder();
        for (Entry<Integer, Map<Integer, Integer>> entry : this.mountPark.getObjDurab().entrySet()) {
            for (Entry<Integer, Integer> entry2 : entry.getValue().entrySet()) {
                if (CellID == entry.getKey())
                    packets.append(entry2.getValue()).append(";").append(entry2.getKey());
            }
        }
        return packets.toString();
    }

    public boolean cellSideLeft(int cell) {
        int ladoIzq = this.w;
        for (int i = 0; i < this.w; i++) {
            if (cell == ladoIzq)
                return true;
            ladoIzq = ladoIzq + (this.w * 2) - 1;
        }
        return false;
    }

    public boolean cellSideRight(int cell) {
        int ladoDer = 2 * (this.w - 1);
        for (int i = 0; i < this.w; i++) {
            if (cell == ladoDer)
                return true;
            ladoDer = ladoDer + (this.w * 2) - 1;
        }
        return false;
    }

    public boolean cellSide(int cell1, int cell2) {
        if (cellSideLeft(cell1))
            if (cell2 == cell1 + (this.w - 1) || cell2 == cell1 - this.w)
                return true;
        if (cellSideRight(cell1))
            return cell2 == cell1 + this.w || cell2 == cell1 - (this.w - 1);
        return false;
    }

    public String getGMOfMount(boolean ok) {
        if(this.mountPark == null || this.mountPark.getListOfRaising().size() == 0)
            return "";

        ArrayList<Montura> mounts = new ArrayList<>();

        for(Integer id : this.mountPark.getListOfRaising()) {
            Montura mount = Mundo.mundo.getMountById(id);

            if(mount != null)
                if(this.getPlayer(mount.getOwner()) != null || this.mountPark.getGuild() != null)
                    mounts.add(mount);
        }

        if (ok)
            for(Jugador target : this.getPlayers())
                GestorSalida.GAME_SEND_GM_MOUNT(target.getGameClient(), this, false);

        return this.getGMOfMount(mounts);
    }

    public String getGMOfMount(ArrayList<Montura> mounts) {
        if(this.mountPark == null || this.mountPark.getListOfRaising().size() == 0)
            return "";
        StringBuilder packets = new StringBuilder();
        packets.append("GM|+");
        boolean first = true;
        for(Montura mount : mounts) {
            String GM = mount.parseToGM();
            if(GM == null || GM.equals(""))
                continue;
            if(!first)
                packets.append("|+");
            packets.append(GM);
            first = false;
        }

        return packets.toString();
    }

    public Jugador getPlayer(int id) {
        for(GameCase cell : cases)
            for(Jugador player : cell.getPlayers())
                if(player != null)
                    if(player.getId() == id)
                        return player;
        return null;
    }

    public void onPlayerArriveOnCell(Jugador player, int id) {
        GameCase cell = this.getCase(id);


        if (cell == null)
            return;
        synchronized (cell) {
            ObjetoJuego obj = cell.getDroppedItem(true);
            if (obj != null && !MainServidor.INSTANCE.getMapAsBlocked()) {
                /*Logging.objects.debug("Object", "GetInOnTheFloor : {} a ramassé [{}@{}*{}]",
                        player.getName(), obj.getModelo().getId(), obj.getId(), obj.getCantidad());*/
                if (player.addObjet(obj, true))
                    Mundo.addGameObject(obj, true);
                GestorSalida.GAME_SEND_GDO_PACKET_TO_MAP(this, '-', id, 0, 0);
                GestorSalida.GAME_SEND_Ow_PACKET(player);
            }
            if (obj != null && MainServidor.INSTANCE.getMapAsBlocked())
                GestorSalida.GAME_SEND_MESSAGE(player, "L'Administrateur à bloqué temporairement l'accès de récolte des objets aux sols.");
        }

        PuertasInteractivas.check(player, this);
        this.getCase(id).applyOnCellStopActions(player);
        if (this.placesStr.equalsIgnoreCase("|"))
            return;
        if (player.getCurMap().getId() != this.id || !player.canAggro())
            return;

        for (Monstruos.MobGroup group : this.mobGroups.values()) {
            if (Camino.getDistanceBetween(this, id, group.getCellId()) <= group.getAggroDistance()) {//S'il y aggr
                startFightVersusMonstres(player, group);
                return;
            }
        }
    }

    public void send(String packet) {
        this.getPlayers().stream().filter(Objects::nonNull).forEach(player -> player.send(packet));
    }

    public static class GameCase {

        private final int id;
        private boolean walkable = true, loS = true;

        private List<Jugador> players;
        private ArrayList<Peleador> fighters;
        private ArrayList<Accion> onCellStop;
        private ObjetosInteractivos object;
        private ObjetoJuego droppedItem;

        public GameCase(Mapa map, int id, boolean walkable, boolean loS, int objId) {
            this.id = id;
            this.walkable = walkable;
            this.loS = loS;
            final byte ancho = map.getW();
            final int _loc5 = (int) Math.floor(id / (ancho * 2 - 1));
            final int _loc6 = id - (_loc5 * (ancho * 2 - 1));
            final int _loc7 = _loc6 % ancho;
            byte coordY = (byte) (_loc5 - _loc7);
            // es en plano inclinado, solo Y es negativo partiendo del 0 arriba negativo, abajo positivo
            byte coordX = (byte) ((id - (ancho - 1) * coordY) / ancho);
            if (objId != -1)
                this.object = new ObjetosInteractivos(objId, map, this);
        }


        public int getId() {
            return id;
        }

        public boolean isWalkable(boolean useObject) {
            if (this.object != null && useObject)
                return this.walkable && this.object.isWalkable();
            return this.walkable;
        }

        public boolean isWalkable(boolean useObject, boolean inFight, int targetCell) {
            if(this.object != null && useObject) {
                if((inFight || this.getId() != targetCell) && this.object.getTemplate() != null) {
                    switch(this.object.getTemplate().getId()) {
                        case 7515:
                        case 7511:
                        case 7517:
                        case 7512:
                        case 7513:
                        case 7516:
                        case 7550:
                        case 7518:
                        case 7534:
                        case 7535:
                        case 7533:
                        case 7551:
                        case 7514:
                            return this.walkable;
                        case 6763:
                        case 6766:
                        case 6767:
                        case 6772:
                            return false;
                    }
                }
                return this.walkable && this.object.isWalkable();
            }
            return this.walkable;
        }

        public boolean isLoS() {
            return this.loS;
        }

        public boolean blockLoS() {
            if (this.fighters == null)
                return this.loS;
            boolean hide = true;
            for (Peleador fighter : this.fighters)
                if (!fighter.isHide())
                    hide = false;
            return this.loS && hide;
        }

        public void addPlayer(Jugador player) {
            if (this.players == null)
                this.players = new ArrayList<>();
            if(!this.players.contains(player))
                this.players.add(player);
        }

        public void removePlayer(Jugador player) {
            if (this.players != null) {
                if(this.players.contains(player))
                    this.players.remove(player);
                if (this.players.isEmpty()) this.players = null;
            }
        }

        public List<Jugador> getPlayers() {
            if (this.players == null)
                return new ArrayList<>();
            return players;
        }

        public void addFighter(Peleador fighter) {
            if (this.fighters == null)
                this.fighters = new ArrayList<>();
            if(!this.fighters.contains(fighter))
                this.fighters.add(fighter);
        }

        public void removeFighter(Peleador fighter) {
            if (this.fighters != null) {
                if(this.fighters.contains(fighter))
                    this.fighters.remove(fighter);
                if (this.fighters.isEmpty()) this.fighters = null;
            }
        }

        public ArrayList<Peleador> getFighters() {
            if (this.fighters == null)
                return new ArrayList<>();
            return fighters;
        }

        public Peleador getFirstFighter() {
            if(this.fighters != null) for(Peleador fighter : this.fighters) return fighter; // return this.fighters.get(0);o
            return null;
        }

        public void addOnCellStopAction(int id, String args, String cond, Mapa map) {
            if (this.onCellStop == null)
                this.onCellStop = new ArrayList<>();
            this.onCellStop.add(new Accion(id, args, cond, map));
        }

        public void applyOnCellStopActions(Jugador perso) {
            if (this.onCellStop != null)
                for (Accion action : this.onCellStop)
                    action.apply(perso, null, -1, -1);
        }

        public boolean getOnCellStopAction() {
            return this.onCellStop != null;
        }

        public ArrayList<Accion> getOnCellStop() {
            return onCellStop;
        }

        public void setOnCellStop(ArrayList<Accion> onCellStop) {
            this.onCellStop = onCellStop;
        }
        public void clearOnCellAction() {
            this.onCellStop = null;
        }

        public ObjetosInteractivos getObject() {
            return this.object;
        }

        public void addDroppedItem(ObjetoJuego obj) {
            this.droppedItem = obj;
        }

        public ObjetoJuego getDroppedItem(boolean delete) {
            ObjetoJuego obj = this.droppedItem;
            if (delete)
                this.droppedItem = null;
            return obj;
        }

        public void clearDroppedItem() {
            this.droppedItem = null;
        }

        public boolean canDoAction(int id) {
            if (this.object == null)
                return false;
            switch (id) {
                //Atelier des F�es
                case 151:
                    return this.object.getId() == 7028;

                //Fontaine jouvence
                case 62:
                    return this.object.getId() == 7004;
                //Moudre et egrenner - Paysan
                case 122:
                case 47:
                    return this.object.getId() == 7007;
                //Faucher Bl�
                case 45:
                    if (this.object.getId() == 7511) {//Bl�
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Faucher Orge
                case 53:
                    if (this.object.getId() == 7515) {//Orge
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;

                //Faucher Avoine
                case 57:
                    if (this.object.getId() == 7517) {//Avoine
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Faucher Houblon
                case 46:
                    if (this.object.getId() == 7512) {//Houblon
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Faucher Lin
                case 50:
                case 68:
                    if (this.object.getId() == 7513) {//Lin
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Faucher Riz
                case 159:
                    if (this.object.getId() == 7550) {//Riz
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Faucher Seigle
                case 52:
                    if (this.object.getId() == 7516) {//Seigle
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Faucher Malt
                case 58:
                    if (this.object.getId() == 7518) {//Malt
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Faucher Chanvre - Cueillir Chanvre
                case 69:
                case 54:
                    if (this.object.getId() == 7514) {//Chanvre
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Scier - Bucheron
                case 101:
                    return this.object.getId() == 7003;
                //Couper Fr�ne
                case 6:
                    if (this.object.getId() == 7500) {//Fr�ne
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Couper Ch�taignier
                case 39:
                    if (this.object.getId() == 7501) {//Ch�taignier
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Couper Noyer
                case 40:
                    if (this.object.getId() == 7502) {//Noyer
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Couper Ch�ne
                case 10:
                    if (this.object.getId() == 7503) {//Ch�ne
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Couper Oliviolet
                case 141:
                    if (this.object.getId() == 7542) {//Oliviolet
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Couper Bombu
                case 139:
                    if (this.object.getId() == 7541) {//Bombu
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Couper Erable
                case 37:
                    if (this.object.getId() == 7504) {//Erable
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Couper Bambou
                case 154:
                    if (this.object.getId() == 7553) {//Bambou
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Couper If
                case 33:
                    if (this.object.getId() == 7505) {//If
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Couper Merisier
                case 41:
                    if (this.object.getId() == 7506) {//Merisier
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Couper Eb�ne
                case 34:
                    if (this.object.getId() == 7507) {//Eb�ne
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Couper Kalyptus
                case 174:
                    if (this.object.getId() == 7557) {//Kalyptus
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Couper Charme
                case 38:
                    if (this.object.getId() == 7508) {//Charme
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Couper Orme
                case 35:
                    if (this.object.getId() == 7509) {//Orme
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Couper Bambou Sombre
                case 155:
                    if (this.object.getId() == 7554) {//Bambou Sombre
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Couper Bambou Sacr�
                case 158:
                    if (this.object.getId() == 7552) {//Bambou Sacr�
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Puiser
                case 102:
                    if (this.object.getId() == 7519) {//Puits
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Polir
                case 48:
                    return this.object.getId() == 7005;
                //Tas de patate
                case 42:
                    return this.object.getId() == 7510;
                //Moule/Fondre - Mineur
                case 32:
                    return this.object.getId() == 7002;
                case 22:
                    return this.object.getId() == 7006;
                //Miner Fer
                case 24:
                    if (this.object.getId() == 7520) {//Miner
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Miner Cuivre
                case 25:
                    if (this.object.getId() == 7522) {//Miner
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Miner Bronze
                case 26:
                    if (this.object.getId() == 7523) {//Miner
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Miner Kobalte
                case 28:
                    if (this.object.getId() == 7525) {//Miner
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Miner Manga
                case 56:
                    if (this.object.getId() == 7524) {//Miner
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Miner Sili
                case 162:
                    if (this.object.getId() == 7556) {//Miner
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Miner Etain
                case 55:
                    if (this.object.getId() == 7521) {//Miner
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Miner Argent
                case 29:
                    if (this.object.getId() == 7526) {//Miner
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Miner Bauxite
                case 31:
                    if (this.object.getId() == 7528) {//Miner
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Miner Or
                case 30:
                    if (this.object.getId() == 7527) {//Miner
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Miner Dolomite
                case 161:
                    if (this.object.getId() == 7555) {//Miner
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Fabriquer potion - Alchimiste
                case 23:
                    return this.object.getId() == 7019;
                //Cueillir Tr�fle
                case 71:
                    if (this.object.getId() == 7533) {//Tr�fle
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Cueillir Menthe
                case 72:
                    if (this.object.getId() == 7534) {//Menthe
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Cueillir Orchid�e
                case 73:
                    if (this.object.getId() == 7535) {// Orchid�e
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Cueillir Edelweiss
                case 74:
                    if (this.object.getId() == 7536) {//Edelweiss
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Cueillir Graine de Pandouille
                case 160:
                    if (this.object.getId() == 7551) {//Graine de Pandouille
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Vider - P�cheur
                case 133:
                    return this.object.getId() == 7024;
                //P�cher Petits poissons de mer
                case 128:
                    if (this.object.getId() == 7530) {//Petits poissons de mer
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //P�cher Petits poissons de rivi�re
                case 124:
                    if (this.object.getId() == 7529) {//Petits poissons de rivi�re
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //P�cher Pichon
                case 136:
                    if (this.object.getId() == 7544) {//Pichon
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //P�cher Ombre Etrange
                case 140:
                    if (this.object.getId() == 7543) {//Ombre Etrange
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //P�cher Poissons de rivi�re
                case 125:
                    if (this.object.getId() == 7532) {//Poissons de rivi�re
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //P�cher Poissons de mer
                case 129:
                    if (this.object.getId() == 7531) {//Poissons de mer
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //P�cher Gros poissons de rivi�re
                case 126:
                    if (this.object.getId() == 7537) {//Gros poissons de rivi�re
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //P�cher Gros poissons de mers
                case 130:
                    if (this.object.getId() == 7538) {//Gros poissons de mers
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //P�cher Poissons g�ants de rivi�re
                case 127:
                    if (this.object.getId() == 7539) {//Poissons g�ants de rivi�re
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //P�cher Poissons g�ants de mer
                case 131:
                    if (this.object.getId() == 7540) {//Poissons g�ants de mer
                        return this.object.getState() == OficioConstantes.IOBJECT_STATE_FULL;
                    }
                    return false;
                //Boulanger
                case 109://Pain
                case 27://Bonbon
                    return this.object.getId() == 7001;
                //Poissonier
                case 135://Faire un poisson (mangeable)
                    return this.object.getId() == 7022;
                //Chasseur
                case 134:
                    return this.object.getId() == 7023;
                //Boucher
                case 132:
                    return this.object.getId() == 7025;
                case 157:
                    return (this.object.getId() == 7030 || this.object.getId() == 7031);
                case 44://Sauvegarder le Zaap
                case 114://Utiliser le Zaap
                    //Zaaps
                    return switch (this.object.getId()) {
                        case 7000, 7026, 7029, 4287 -> true;
                        default -> false;
                    };

                case 175://Acc�der
                case 176://Acheter
                case 177://Vendre
                case 178://Modifier le prix de vente
                    //Enclos
                    return switch (this.object.getId()) {
                        case 6763, 6766, 6767, 6772 -> true;
                        default -> false;
                    };

                case 179://Levier
                    return this.object.getId() == 7045;
                //Se rendre � incarnam
                case 183:
                    return switch (this.object.getId()) {
                        case 1845, 1853, 1854, 1855, 1856, 1857, 1858, 1859, 1860, 1861, 1862, 2319 -> true;
                        default -> false;
                    };

                //Enclume magique
                case 1:
                case 113:
                case 115:
                case 116:
                case 117:
                case 118:
                case 119:
                case 120:
                    return this.object.getId() == 7020;

                //Enclume
                case 19:
                case 143:
                case 145:
                case 144:
                case 142:
                case 146:
                case 67:
                case 21:
                case 65:
                case 66:
                case 20:
                case 18:
                    return this.object.getId() == 7012;

                //Costume Mage
                case 167:
                case 165:
                case 166:
                    return this.object.getId() == 7036;

                //Coordo Mage
                case 164:
                case 163:
                    return this.object.getId() == 7037;

                //Joai Mage
                case 168:
                case 169:
                    return this.object.getId() == 7038;

                //Bricoleur
                case 171:
                case 182:
                    return this.object.getId() == 7039;

                //Forgeur Bouclier
                case 156:
                    return this.object.getId() == 7027;

                //Coordonier
                case 13:
                case 14:
                    return this.object.getId() == 7011;

                //Tailleur (Dos)
                case 123:
                case 64:
                    return this.object.getId() == 7015;

                //Sculteur
                case 17:
                case 16:
                case 147:
                case 148:
                case 149:
                case 15:
                    return this.object.getId() == 7013;
                //TODO: R�par�
                //Tailleur (Haut)
                case 63:
                    return (this.object.getId() == 7014 || this.object.getId() == 7016);
                //Atelier : Cr�er Amu // Anneau
                case 11:
                case 12:
                    return (this.object.getId() >= 7008 && this.object.getId() <= 7010);
                //Maison
                case 81://V�rouiller
                case 84://Acheter
                case 97://Entrer
                case 98://Vendre
                case 108://Modifier le prix de vente
                    return (this.object.getId() >= 6700 && this.object.getId() <= 6776);
                //Coffre
                case 104://Ouvrir
                case 105://Code
                    return (this.object.getId() == 7350
                            || this.object.getId() == 7351 || this.object.getId() == 7353);
                case 170://Livre des artisants.
                    return this.object.getId() == 7035;
                case 121:
                case 181:
                    return this.object.getId() == 7021;
                case 110:
                    return this.object.getId() == 7018;
                case 153:
                    return this.object.getId() == 7352;

                default:
                    return false;
            }
        }

        public void startAction(final Jugador player, AccionJuego GA) {
            if(player.getExchangeAction() != null) return;
            int actionID = -1;
            short CcellID = -1;
            try {
                actionID = Integer.parseInt(GA.args.split(";")[1]);
                CcellID = Short.parseShort(GA.args.split(";")[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (actionID == -1) {
                GestorSalida.GAME_SEND_MESSAGE(player, "Erreur action id null.");
                return;
            }
            if (player.getDoAction()) {
                GestorSalida.GAME_SEND_MESSAGE(player, "Vous avez déjà une action en cours. Signaler si le problème persiste.");
                return;
            }
            if (OficioConstantes.isJobAction(actionID) && player.getPelea() == null) {
                if (player.getPodUsed() > player.getMaximosPods()) {
                    GestorSalida.GAME_SEND_Im_PACKET(player, "112");
                    return;
                }
                if (player.getMount() != null) {
                    if (player.getMount().getActualPods() > player.getMount().getMaxPods()) {
                        GestorSalida.GAME_SEND_Im_PACKET(player, "112");
                        return;
                    }
                }
                player.setDoAction(true);
                player.doJobAction(actionID, this.object, GA, this);
                return;
            }
            switch (actionID) {
                case 62://Fontaine de jouvence
                    if (player.getLevel() > 5)
                        return;
                    GestorSalida.GAME_SEND_MESSAGE(player, "La magie opère et t'offre ta santé au maximum..");
                    player.fullPDV();
                    break;

                case 42://Tas de patate
                    if (!this.object.isInteractive())
                        return;//Si l'objet est utilis�
                    if (this.object.getState() != OficioConstantes.IOBJECT_STATE_FULL)
                        return;//Si le puits est vide
                    this.object.setState(OficioConstantes.IOBJECT_STATE_EMPTYING);
                    this.object.setInteractive(false);
                    GestorSalida.GAME_SEND_GA_PACKET_TO_MAP(player.getCurMap(), ""
                            + GA.getId(), 501, player.getId() + "", this.id + ","
                            + this.object.getUseDuration() + ","
                            + this.object.getUnknowValue());
                    GestorSalida.GAME_SEND_GDF_PACKET_TO_MAP(player.getCurMap(), this);

                    Temporizador.addSiguiente(() -> {
                        this.getObject().setState(OficioConstantes.IOBJECT_STATE_EMPTY);
                        this.getObject().setInteractive(false);
                        this.getObject().disable();
                        GestorSalida.GAME_SEND_GDF_PACKET_TO_MAP(player.getCurMap(), this);
                        int qua = Formulas.getRandomValue(1, 5);//On a entre 1 et 10 eaux
                        ObjetoJuego obj = Mundo.mundo.getObjetoModelo(537).createNewItem(qua, false);
                        if (player.addObjet(obj, true))
                            Mundo.addGameObject(obj, true);
                        GestorSalida.GAME_SEND_IQ_PACKET(player, player.getId(), qua);
                    }, this.getObject().getUseDuration(), Temporizador.DataType.MAPA);
                    break;

                case 44://Sauvegarder pos
                    if (!player.verifOtomaiZaap())
                        return;
                    short map = player.getCurMap().getId();
                    String str = map + "," + Mundo.mundo.getZaapCellIdByMapId(map);
                    player.set_savePos(str);
                    GestorSalida.GAME_SEND_Im_PACKET(player, "06");
                    break;

                case 102://Puiser
                    if (!this.getObject().isInteractive())
                        return;//Si l'objet est utilis�
                    if (this.getObject().getState() != OficioConstantes.IOBJECT_STATE_FULL)
                        return;//Si le puits est vide
                    this.getObject().setState(OficioConstantes.IOBJECT_STATE_EMPTYING);
                    this.getObject().setInteractive(false);
                    GestorSalida.GAME_SEND_GA_PACKET_TO_MAP(player.getCurMap(), ""
                            + GA.getId(), 501, player.getId() + "", this.id + ","
                            + this.getObject().getUseDuration() + ","
                            + this.getObject().getUnknowValue());
                    GestorSalida.GAME_SEND_GDF_PACKET_TO_MAP(player.getCurMap(), this);

                    Temporizador.addSiguiente(() -> {
                        this.getObject().setState(OficioConstantes.IOBJECT_STATE_EMPTY);
                        this.getObject().setInteractive(false);
                        this.getObject().disable();
                        GestorSalida.GAME_SEND_GDF_PACKET_TO_MAP(player.getCurMap(), this);
                        int qua = Formulas.getRandomValue(1, 10);//On a entre 1 et 10 eaux
                        ObjetoJuego obj = Mundo.mundo.getObjetoModelo(311).createNewItem(qua, false);
                        if (player.addObjet(obj, true))
                            Mundo.addGameObject(obj, true);
                        GestorSalida.GAME_SEND_IQ_PACKET(player, player.getId(), qua);
                    }, this.getObject().getUseDuration(), Temporizador.DataType.MAPA);
                    break;

                case 114://Utiliser (zaap)
                    player.openZaapMenu();
                    player.getGameClient().removeAction(GA);
                    break;

                case 157: //Zaapis
                    StringBuilder ZaapiList = new StringBuilder();
                    String[] Zaapis;
                    int count = 0;
                    int price = 20;

                    if (player.getCurMap().getSubArea().area.getId() == 7 && (player.get_align() == 1 || player.get_align() == 0 || player.get_align() == 3))//Ange, Neutre ou S�rianne
                    {
                        Zaapis = Constantes.ZAAPI.get(Constantes.ALINEAMIENTO_BONTARIANO).split(",");
                        if (player.get_align() == 1)
                            price = 10;
                    } else if (player.getCurMap().getSubArea().area.getId() == 11
                            && (player.get_align() == 2 || player.get_align() == 0 || player.get_align() == 3))//D�mons, Neutre ou S�rianne
                    {
                        Zaapis = Constantes.ZAAPI.get(Constantes.ALINEAMIENTO_BRAKMARIANO).split(",");
                        if (player.get_align() == 2)
                            price = 10;
                    } else {
                        Zaapis = Constantes.ZAAPI.get(Constantes.ALINEAMIENTO_NEUTRAL).split(",");
                    }

                    if (Zaapis.length > 0) {
                        for (String s : Zaapis) {
                            if (count == Zaapis.length)
                                ZaapiList.append(s).append(";").append(price);
                            else
                                ZaapiList.append(s).append(";").append(price).append("|");
                            count++;
                        }
                        player.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.IN_ZAPPI, 0));
                        GestorSalida.GAME_SEND_ZAAPI_PACKET(player, ZaapiList.toString());
                    }
                    break;
                case 175://Acceder a un enclos
                    final Cercados park = player.getCurMap().getMountPark();
                    if (park == null)
                        return;

                    try {
                        park.getEtable().stream().filter(Objects::nonNull).forEach(mount -> mount.checkBaby(player));
                        park.getListOfRaising().stream().filter(integer -> Mundo.mundo.getMountById(integer) != null).forEach(integer -> Mundo.mundo.getMountById(integer).checkBaby(player));
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    if(park.getGuild() != null)
                        for(Jugador target : park.getGuild().getPlayers())
                            if(target != null && target.getExchangeAction() != null && target.getExchangeAction().getType() == AccionIntercambiar.IN_MOUNTPARK && target.getCurMap().getId() == player.getCurMap().getId()) {
                                player.send("Im120");
                                return;
                            }

                    player.AbrirCercado();
                    break;
                case 176://Achat enclo
                    Cercados MP = player.getCurMap().getMountPark();
                    if (MP.getOwner() == -1)//Public
                    {
                        GestorSalida.GAME_SEND_Im_PACKET(player, "196");
                        return;
                    }
                    if (MP.getPrice() == 0)//Non en vente
                    {
                        GestorSalida.GAME_SEND_Im_PACKET(player, "197");
                        return;
                    }
                    if (player.getGuild() == null)//Pas de guilde
                    {
                        GestorSalida.GAME_SEND_Im_PACKET(player, "1135");
                        return;
                    }
                    if (player.getGuildMember().getRank() != 1)//Non meneur
                    {
                        GestorSalida.GAME_SEND_Im_PACKET(player, "198");
                        return;
                    }
                    GestorSalida.GAME_SEND_R_PACKET(player, "D" + MP.getPrice()
                            + "|" + MP.getPrice());
                    break;
                case 177://Vendre enclo
                case 178://Modifier prix de vente
                    Cercados MP1 = player.getCurMap().getMountPark();
                    if (MP1.getOwner() == -1) {
                        GestorSalida.GAME_SEND_Im_PACKET(player, "194");
                        return;
                    }
                    if (MP1.getOwner() != player.getId()) {
                        GestorSalida.GAME_SEND_Im_PACKET(player, "195");
                        return;
                    }
                    GestorSalida.GAME_SEND_R_PACKET(player, "D" + MP1.getPrice()
                            + "|" + MP1.getPrice());
                    break;
                case 183://Retourner sur Incarnam
                    if (player.getLevel() > 15) {
                        GestorSalida.GAME_SEND_Im_PACKET(player, "1127");
                        player.getGameClient().removeAction(GA);
                        return;
                    }
                    short mapID = Constantes.getStartMap(player.getClasse());
                    int cellID = Constantes.getStartCell(player.getClasse());
                    player.teleport(mapID, cellID);
                    player.getGameClient().removeAction(GA);
                    break;
                case 81://V�rouiller maison
                    Casas house = Mundo.mundo.getHouseManager().getHouseIdByCoord(player.getCurMap().getId(), CcellID);
                    if (house == null)
                        return;
                    player.setInHouse(house);
                    house.lock(player);
                    break;
                case 84://Rentrer dans une maison
                    house = Mundo.mundo.getHouseManager().getHouseIdByCoord(player.getCurMap().getId(), CcellID);
                    if (house == null)
                        return;

                    Mapa mapHouse = Mundo.mundo.getMap((short) house.getHouseMapId());
                    if (mapHouse == null) {
                        Logging.error.warn("map house id {} wasn't found", house.getHouseMapId());
                        GestorSalida.GAME_SEND_MESSAGE(player, "La maison est cassée.. Contactez un administrateur sur le forum.");
                        return;
                    }
                    GameCase caseHouse = mapHouse.getCase(house.getHouseCellId());
                    if (caseHouse == null || !caseHouse.isWalkable(true)) {
                        Logging.error.warn("case {} in map house id {} wasn't found", house.getHouseCellId(),
                                house.getHouseMapId());
                        GestorSalida.GAME_SEND_MESSAGE(player, "La maison est cassée.. Contactez un administrateur sur le forum.");
                        return;
                    }
                    if (player.isOnMount()) {
                        GestorSalida.GAME_SEND_Im_PACKET(player, "1118");
                        return;
                    }
                    house.enter(player);
                    player.setInHouse(house);
                    break;
                case 97://Acheter maison
                    house = Mundo.mundo.getHouseManager().getHouseIdByCoord(player.getCurMap().getId(), CcellID);
                    if (house == null)
                        return;
                    player.setInHouse(house);
                    house.ComprarCasa(player);
                    break;

                case 104://Ouvrir coffre priv�
                    Cofres trunk = Cofres.getTrunkIdByCoord(player.getCurMap().getId(), CcellID);

                    if (trunk == null) {
                        trunk = new Cofres(Database.dinamicos.getTrunkData().getNextId(), player.getInHouse().getId(), player.getCurMap().getId(), CcellID);
                        trunk.setOwnerId(player.getInHouse().getOwnerId());
                        trunk.setKey("-");
                        trunk.setKamas(0);
                        Database.dinamicos.getTrunkData().insert(trunk);
                        Mundo.mundo.addTrunk(trunk);
                    }
                    if(player.getInHouse() != null && trunk.getOwnerId() != player.getAccID() && trunk.getHouseId() == player.getInHouse().getId() && player.getId() == player.getInHouse().getOwnerId()) {
                        trunk.setOwnerId(player.getId());
                        Database.estaticos.getTrunkData().update(player, player.getInHouse());
                    }

                    trunk.enter(player);
                    break;
                case 105://V�rouiller coffre
                    Cofres t = Cofres.getTrunkIdByCoord(player.getCurMap().getId(), CcellID);

                    if (t == null)
                        return;
                    t.Lock(player);
                    break;
                case 153:
                    trunk = Cofres.getTrunkIdByCoord(player.getCurMap().getId(), CcellID);

                    if(trunk != null) {
                        if (trunk.getPlayer() != null) {
                            player.send("Im120");
                            return;
                        }
                        player.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.IN_TRUNK, trunk));
                        Cofres.open(player, "-", true);
                    }
                    break;
                case 98://Vendre
                case 108://Modifier prix de vente
                    Casas h4 = Mundo.mundo.getHouseManager().getHouseIdByCoord(player.getCurMap().getId(), CcellID);
                    if (h4 == null)
                        return;
                    player.setInHouse(h4);
                    h4.VenderCasa(player);
                    break;
                case 170: //Libro de los artesanos
                    player.setLivreArtisant(true);
                    player.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.CRAFTING_BOOK, new RomperObjetos()));
                    GestorSalida.GAME_SEND_ECK_PACKET(player, 14, "2;11;13;14;15;16;17;18;19;20;24;25;26;27;28;31;33;36;41;43;44;45;46;47;48;49;50;56;58;62;63;64;65");
                    break;
                case 181: //Rompedor de objetos
                    GestorSalida.SEND_GDF_PERSO(player, CcellID, 3, 1);
                    GestorSalida.GAME_SEND_ECK_PACKET(player, 3, "8;181");
                    player.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.BREAKING_OBJECTS, new RomperObjetos()));
                    break;
            }
            player.getGameClient().removeAction(GA);
        }

        public void finishAction(Jugador perso, AccionJuego GA) {
            int actionID = -1;
            try {
                actionID = Integer.parseInt(GA.args.split(";")[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (actionID == -1)
                return;

            if (OficioConstantes.isJobAction(actionID)) {
                perso.finishJobAction(actionID, this.object, GA, this);
                perso.setDoAction(false);
                return;
            }
            perso.setDoAction(false);
            switch (actionID) {
                case 44://Sauvegarder a un zaap
                case 81://V�rouiller maison
                case 84://ouvrir maison
                case 97://Acheter maison.
                case 98://Vendre
                case 104://Ouvrir coffre
                case 105://Code coffre
                case 108://Modifier prix de vente
                case 157://Zaapi
                case 121://Briser une ressource
                case 181://Concasseur
                case 110:
                case 153:
                case 183:
                    break;
                case 42://Tas de patate
                    if (this.object == null)
                        return;
                    this.object.setState(OficioConstantes.IOBJECT_STATE_EMPTY);
                    this.object.setInteractive(false);
                    this.object.disable();
                    GestorSalida.GAME_SEND_GDF_PACKET_TO_MAP(perso.getCurMap(), this);
                    int qua = Formulas.getRandomValue(1, 5);//On a entre 1 et 5 patates
                    ObjetoJuego obj = Mundo.mundo.getObjetoModelo(537).createNewItem(qua, false);
                    if (perso.addObjet(obj, true))
                        Mundo.addGameObject(obj, true);
                    GestorSalida.GAME_SEND_IQ_PACKET(perso, perso.getId(), qua);
                    break;
                case 102://Puiser
                    if (this.object == null)
                        return;

                    this.object.setState(OficioConstantes.IOBJECT_STATE_EMPTY);
                    this.object.setInteractive(false);
                    this.object.disable();
                    GestorSalida.GAME_SEND_GDF_PACKET_TO_MAP(perso.getCurMap(), this);
                    qua = Formulas.getRandomValue(1, 10);//On a entre 1 et 10 eaux
                    obj = Mundo.mundo.getObjetoModelo(311).createNewItem(qua, false);
                    if (perso.addObjet(obj, true))
                        Mundo.addGameObject(obj, true);
                    GestorSalida.GAME_SEND_IQ_PACKET(perso, perso.getId(), qua);
                    break;
            }
        }
    }

    public static class ObjetosInteractivos {

        public final static Updatable updatable = new Updatable(0) {
            private final ArrayList<ObjetosInteractivos> queue = new ArrayList<>();

            @Override
            public void update() {
                if(this.queue.isEmpty()) return;
                long time = Instant.now().toEpochMilli();
                new ArrayList<>(this.queue).stream().filter(interactiveObject -> interactiveObject.getTemplate() != null && time - interactiveObject.lastTime >
                        interactiveObject.getTemplate().getRespawnTime()).forEach(interactiveObject -> {
                    interactiveObject.enable();
                    this.queue.remove(interactiveObject);
                });
            }

            @Override
            public ArrayList<ObjetosInteractivos> get() {
                return queue;
            }
        };

        private final int id;
        private int state;
        private final Mapa map;
        private final GameCase cell;
        private boolean interactive = true;
        private final boolean walkable;
        private long lastTime = 0;
        private InteractiveObjectTemplate template;

        public ObjetosInteractivos(int id, final Mapa iMap, GameCase iCell) {
            this.id = id;
            this.map = iMap;
            this.cell = iCell;
            this.state = OficioConstantes.IOBJECT_STATE_FULL;
            this.template = Mundo.mundo.getIOTemplate(this.id);
            this.walkable = this.getTemplate() != null && this.getTemplate().isWalkable() && this.state == OficioConstantes.IOBJECT_STATE_FULL;
        }

        public static void getActionIO(final Jugador player, GameCase cell, int id) {
            switch(id) {
                case 7041:
                case 7042:
                case 7043:
                case 7044:
                case 7045:
                case 1748:
                    if(PuertasInteractivas.tryActivate(player, cell))
                        return;
                    break;
            }
            switch (id) {
                case 1524:
                case 542://Statue Phoenix.
                    if (player.isGhost()) {
                        player.setAlive();
                        Mision q = Mision.getQuestById(190);
                        if (q != null) {
                            MisionJugador qp = player.getQuestPersoByQuest(q);
                            if (qp != null) {
                                MisionEtapa qe = q.getCurrentQuestStep(qp);
                                if (qe != null)
                                    if(qe.getId() == 783)
                                        q.updateQuestData(player, true, qe.getValidationType());
                            }
                        }
                    }
                    break;

                case 684://Portillon donjon squelette.
                    if (player.hasItemTemplate(10207, 1)) {
                        String stats, statsReplace = "";
                        ObjetoJuego object = player.getItemTemplate(10207);
                        stats = object.getTxtStat().get(Constantes.STATS_NAME_DJ);
                        try {
                            for (String i : stats.split(",")) {
                                if (Dopeul.parseConditionTrousseau(i.replace(" ", ""), -1, player.getCurMap().getId())) {
                                    player.teleport((short) 2110, 118);
                                    statsReplace = i;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!statsReplace.isEmpty()) {
                            StringBuilder newStats = new StringBuilder();
                            for (String i : stats.split(","))
                                if (!i.equals(statsReplace))
                                    newStats.append((newStats.length() == 0) ? i : "," + i);
                            object.getTxtStat().remove(Constantes.STATS_NAME_DJ);
                            object.getTxtStat().put(Constantes.STATS_NAME_DJ, newStats.toString());
                            GestorSalida.GAME_SEND_UPDATE_ITEM(player, player.getItemTemplate(10207));
                            break;
                        }
                    }

                    if (!player.hasItemTemplate(1570, 1)) {
                        GestorSalida.GAME_SEND_MESSAGE(player, "Vous ne possedez pas la clef nécessaire.", "009900");
                    } else {
                        player.removeByTemplateID(1570, 1);
                        GestorSalida.GAME_SEND_Im_PACKET(player, "022;" + 1 + "~"
                                + 1570);
                        player.teleport((short) 2110, 118);
                    }
                    break;

                case 1330://Pierre de kwak
                    player.getCurMap().startFightVersusProtectors(player, new Monstruos.MobGroup(player.getCurMap().nextObjectId, cell.getId(), getKwakere(player.getCurMap().getId())
                            + "," + 40 + "," + 40));
                    break;

                case 1679:
                    player.warpToSavePos();
                    break;

                case 3000://Ep�e Crocoburio
                    if (player.hasEquiped(1718)
                            && player.hasEquiped(1719)
                            && player.hasEquiped(1720)
                            && player.getCaracteristicas().getEffect(Constantes.STATS_ADD_VITA) == 120
                            && player.getCaracteristicas().getEffect(Constantes.STATS_ADD_SAGE) == 0
                            && player.getCaracteristicas().getEffect(Constantes.STATS_ADD_FORC) == 60
                            && player.getCaracteristicas().getEffect(Constantes.STATS_ADD_INTE) == 50
                            && player.getCaracteristicas().getEffect(Constantes.STATS_ADD_AGIL) == 0
                            && player.getCaracteristicas().getEffect(Constantes.STATS_ADD_CHAN) == 0) {
                        GestorSalida.GAME_SEND_ACTION_TO_DOOR(player.getCurMap(), 237, true);
                        GestorSalida.GAME_SEND_MESSAGE(player, "Le crocoburio a été désactivé.");
                    /*perso.getWaiter().addNext(new Runnable()
					{
						public void run()
						{
							perso.setFullMorph(10, false, false);
						}
					}, 3000);*/
                    } else {
                        GestorSalida.GAME_SEND_Im_PACKET(player, "119");
                    }

                    break;

                case 7546://Foire au troll
                case 7547:
                    GestorSalida.send(player, "GDF|" + cell.getId() + ";3");
                    break;

                case 1324:// Plot Rouge des �motes
                    switch (player.getCurMap().getId()) {
                        case 2196 -> {
                            if (player.isAway())
                                return;
                            if (player.getGuild() != null
                                    || player.getGuildMember() != null
                                    && player.hasItemTemplate(1575, 1)) {
                                player.removeByTemplateID(1575, 1);
                                GestorSalida.GAME_SEND_gC_PACKET(player, "Ea");
                                GestorSalida.GAME_SEND_Im_PACKET(player, "14");
                                return;
                            }
                            GestorSalida.GAME_SEND_gn_PACKET(player);
                        }
                        //Emote Faire signe
                        case 2037 -> player.addStaticEmote(2);
                        //Emote Applaudir
                        case 2025 -> player.addStaticEmote(3);
                        //Emote Se mettre en Col�re
                        case 2039 -> player.addStaticEmote(4);
                        //Emote Peur
                        case 2047 -> player.addStaticEmote(5);
                        //Emote Montrer son Arme
                        case 8254 -> player.addStaticEmote(6);
                        //Emote Saluer
                        case 2099 -> player.addStaticEmote(9);
                        //Emote Croiser les bras
                        case 8539 -> player.addStaticEmote(14);
                    }
                    break;
                case 1694://Village brigandin tire �olienne
                    GestorSalida.GAME_SEND_GA_PACKET(player.getGameClient(), "", "2", player.getId()
                            + "", "4");
                    player.teleport((short) 6848, 390);
                    break;
                case 1695://Village brigandin tire �olienne
                    GestorSalida.GAME_SEND_GA_PACKET(player.getGameClient(), "", "2", player.getId()
                            + "", "3");
                    player.teleport((short) 6844, 268);
                    break;
                case 7041: // Bas
                    GestorSalida.GAME_SEND_ACTION_TO_DOOR(player.getCurMap(), cell.getId(), true);
                    Minotot.ouvrirBas(player.getCurMap());
                    break;
                case 7042: // Haut
                    GestorSalida.GAME_SEND_ACTION_TO_DOOR(player.getCurMap(), cell.getId(), true);
                    Minotot.ouvrirHaut(player.getCurMap());
                    break;
                case 7043: // Gauche
                    GestorSalida.GAME_SEND_ACTION_TO_DOOR(player.getCurMap(), cell.getId(), true);
                    Minotot.ouvrirGauche(player.getCurMap());
                    break;
                case 7044: // Droite
                    GestorSalida.GAME_SEND_ACTION_TO_DOOR(player.getCurMap(), cell.getId(), true);
                    Minotot.ouvrirDroite(player.getCurMap());
                    break;
                default:
                    break;
            }
        }

        public static void getSignIO(Jugador perso, int cell, int id) {
            switch (perso.getCurMap().getId()) {
                case 7460:
                    for (String[] hunt : Constantes.HUNTING_QUESTS) {
                        if (Integer.parseInt(hunt[1]) == cell && Integer.parseInt(hunt[0]) == id) {
                            GestorSalida.send(perso, "dCK" + hunt[2]);
                            break;
                        }
                    }
                    break;

                case 7411:
                    if (id == 1531 && cell == 230)
                        GestorSalida.send(perso, "dCK139_0612131303");
                    break;

                case 7543:
                    if (id == 1528 && cell == 262)
                        GestorSalida.send(perso, "dCK75_0603101710");
                    if (id == 1533 && cell == 169)
                        GestorSalida.send(perso, "dCK74_0603101709");
                    if (id == 1528 && cell == 169)
                        GestorSalida.send(perso, "dCK73_0706211414");
                    break;

                case 7314:
                    if (id == 1531 && cell == 93)
                        GestorSalida.send(perso, "dCK78_0706221019");
                    if (id == 1532 && cell == 256)
                        GestorSalida.send(perso, "dCK76_0603091219");
                    if (id == 1533 && cell == 415)
                        GestorSalida.send(perso, "dCK77_0603091218");
                    break;

                case 7417:
                    if (id == 1532 && cell == 264)
                        GestorSalida.send(perso, "dCK79_0603101711");
                    if (id == 1528 && cell == 211)
                        GestorSalida.send(perso, "dCK80_0510251009");
                    if (id == 1532 && cell == 212)
                        GestorSalida.send(perso, "dCK77_0603091218");
                    if (id == 1529 && cell == 212)
                        GestorSalida.send(perso, "dCK81_0510251010");
                    break;

                case 2698:
                    if (id == 1531 && cell == 93)
                        GestorSalida.send(perso, "dCK51_0706211150");
                    if (id == 1528 && cell == 109)
                        GestorSalida.send(perso, "dCK41_0706221516");
                    break;

                case 2814:

                case 4493:
                    if (id == 1533 && cell == 415)
                        GestorSalida.send(perso, "dCK43_0706201719");
                    if (id == 1532 && cell == 326)
                        GestorSalida.send(perso, "dCK50_0706211149");
                    if (id == 1529 && cell == 325)
                        GestorSalida.send(perso, "dCK41_0706221516");
                    break;

                case 3087:
                    if (id == 1529 && cell == 89)
                        GestorSalida.send(perso, "dCK41_0706221516");
                    break;

                case 3018:
                    if (id == 1530 && cell == 354)
                        GestorSalida.send(perso, "dCK52_0706211152");
                    if (id == 1532 && cell == 256)
                        GestorSalida.send(perso, "dCK50_0706211149");
                    if (id == 1528 && cell == 255)
                        GestorSalida.send(perso, "dCK41_0706221516");
                    break;

                case 3433:
                    if (id == 1533 && cell == 282)
                        GestorSalida.send(perso, "dCK53_0706211407");
                    if (id == 1531 && cell == 179)
                        GestorSalida.send(perso, "dCK50_0706211149");
                    if (id == 1529 && cell == 178)
                        GestorSalida.send(perso, "dCK41_0706221516");
                    break;

                case 4876:
                    if (id == 1532 && cell == 316)
                        GestorSalida.send(perso, "dCK54_0706211408");
                    if (id == 1531 && cell == 283)
                        GestorSalida.send(perso, "dCK51_0706211150");
                    if (id == 1530 && cell == 282)
                        GestorSalida.send(perso, "dCK52_0706211152");
                    break;
            }
        }

        private static int getKwakere(int i) {
            return switch (i) {
                case 2072 -> 270;
                case 2071 -> 269;
                case 2067 -> 272;
                case 2068 -> 271;
                default -> 269;
            };
        }

        public int getId() {
            return this.id;
        }

        public int getState() {
            return this.state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public boolean isInteractive() {
            return this.interactive;
        }

        public void setInteractive(boolean interactive) {
            this.interactive = interactive;
        }

        public int getUseDuration() {
            int duration = 1500;
            if (this.getTemplate() != null)
                duration = this.getTemplate().getDuration();
            return duration;
        }

        public int getUnknowValue() {
            int unk = 4;
            if (this.getTemplate() != null)
                unk = this.getTemplate().getUnk();
            return unk;
        }

        public boolean isWalkable() {
            return this.walkable;
        }

        public InteractiveObjectTemplate getTemplate() {
            return template;
        }

        public void setTemplate(InteractiveObjectTemplate template) {
            this.template = template;
        }

        private void enable() {
            this.state = OficioConstantes.IOBJECT_STATE_FULLING;
            this.interactive = true;
            GestorSalida.GAME_SEND_GDF_PACKET_TO_MAP(this.map, this.cell);
            this.state = OficioConstantes.IOBJECT_STATE_FULL;
        }

        public void disable() {
            this.lastTime = Instant.now().toEpochMilli();
            @SuppressWarnings("unchecked")
            ArrayList<ObjetosInteractivos> array = (ArrayList<ObjetosInteractivos>) ObjetosInteractivos.updatable.get();
            array.add(this);
        }

        public static class InteractiveObjectTemplate {

            private final int id;
            private final int respawnTime;
            private final int duration;
            private final int unk;
            private final boolean walkable;

            public InteractiveObjectTemplate(int id, int respawnTime, int duration, int unk, boolean walkable) {
                this.id = id;
                this.respawnTime = respawnTime;
                this.duration = duration;
                this.unk = unk;
                this.walkable = walkable;
            }

            public int getId() {
                return id;
            }

            public boolean isWalkable() {
                return walkable;
            }

            int getRespawnTime() {
                return respawnTime;
            }

            int getDuration() {
                return duration;
            }

            int getUnk() {
                return unk;
            }
        }
    }

    public static class PuertasInteractivas {

        private static final ArrayList<PuertasInteractivas> interactiveDoors = new ArrayList<>();

        private final ArrayList<Short> maps = new ArrayList<>();
        private final Map<Short, ArrayList<Short>> doorsEnable = new HashMap<>();
        private final Map<Short, ArrayList<Short>> doorsDisable = new HashMap<>();
        private final Map<Short, ArrayList<Short>> cellsEnable = new HashMap<>();
        private final Map<Short, ArrayList<Short>> cellsDisable = new HashMap<>();
        private final Map<Short, ArrayList<Doble<Short, String>>> requiredCells = new HashMap<>();

        private Doble<Short, Short> button;
        private short time = 30;
        private boolean state = false;

        public PuertasInteractivas(String maps, String doorsEnable, String doorsDisable, String cellsEnable, String cellsDisable, String requiredCells, String button, short time) {
            for(String map : maps.split(",")) this.maps.add(Short.parseShort(map));

            if(!doorsEnable.isEmpty()) this.stock(this.doorsEnable, doorsEnable);
            if(!doorsDisable.isEmpty()) this.stock(this.doorsDisable, doorsDisable);
            if(!cellsEnable.isEmpty()) this.stock(this.cellsEnable, cellsEnable);
            if(!cellsDisable.isEmpty()) this.stock(this.cellsDisable, cellsDisable);

            if(!requiredCells.isEmpty()) {
                for (String data : requiredCells.split("@")) {
                    String[] split = data.split(":");
                    short map = Short.parseShort(split[0]);
                    String cells = split[1];

                    for (String cell : cells.split(";")) {
                        split = cell.split(",");
                        if (!this.requiredCells.containsKey(map))
                            this.requiredCells.put(map, new ArrayList<>());
                        this.requiredCells.get(map).add(new Doble<>(Short.parseShort(split[0]), split.length > 1 ? split[1] : null));
                    }
                }
            }

            if(!button.equals("-1")) {
                String[] split = button.split(",");
                this.button = new Doble<>(Short.parseShort(split[0]), Short.parseShort(split[1]));
            }

            this.time = time;
            PuertasInteractivas.interactiveDoors.add(this);
        }

        private void stock(Map<Short, ArrayList<Short>> arrayListMap, String value) {
            for(String data : value.split("@")) {
                String[] split = data.split(":");
                short map = Short.parseShort(split[0]);
                String cells = split[1];

                for(String cell : cells.split(",")) {
                    if(!arrayListMap.containsKey(map))
                        arrayListMap.put(map, new ArrayList<>());
                    arrayListMap.get(map).add(Short.parseShort(cell));
                }
            }
        }

        public static boolean tryActivate(Jugador player, GameCase gameCase) {
            for(PuertasInteractivas interactiveDoor : PuertasInteractivas.interactiveDoors) {
                if (interactiveDoor.button != null && player.getCurMap().getId() == interactiveDoor.button.getPrimero() && gameCase.getId() == interactiveDoor.button.getSegundo()) {
                    interactiveDoor.check(player);
                    return true;
                }
            }
            return false;
        }

        public static void show(Jugador player) {
            PuertasInteractivas.interactiveDoors.stream().filter(interactiveDoor -> interactiveDoor.state).forEach(interactiveDoor -> {
                interactiveDoor.setState(interactiveDoor.cellsEnable, true, false, player);
                interactiveDoor.setState(interactiveDoor.cellsDisable, false, false, player);
                interactiveDoor.setState(interactiveDoor.doorsEnable, true, true, player);
                interactiveDoor.setState(interactiveDoor.doorsDisable, false, true, player);
            });
        }

        public static void check(Jugador player, Mapa gameMap) {
            try {
                for (PuertasInteractivas interactiveDoor : PuertasInteractivas.interactiveDoors)
                    if (interactiveDoor.maps.contains(gameMap.getId()))
                        if (interactiveDoor.button == null && interactiveDoor.check(player))
                            break;
            } catch(Exception e) { e.printStackTrace(); }
        }

        public synchronized boolean check(Jugador player) {
            if(this.state) return false;
            boolean ok = true;

            for(Entry<Short, ArrayList<Doble<Short, String>>> requiredCells : this.requiredCells.entrySet()) {
                final Mapa gameMap = Mundo.mundo.getMap(requiredCells.getKey());
                if(gameMap == null) continue;
                boolean loc = false;
                for(Doble<Short, String> couple : requiredCells.getValue()) {
                    GameCase gameCase = gameMap.getCase(couple.getPrimero());
                    if (gameCase == null) continue;

                    if (player.getCurMap().getId() == 1884) {
                        if (gameCase.getPlayers().size() > 0) {
                            loc = true;
                            ok = true;
                        }
                    }
                    if(loc) break;

                    if (couple.getSegundo() != null) {
                        if (!PuertasInteractivas.Condition.isValid(player, gameCase, couple.getSegundo())) {
                            ok = false;
                            break;
                        }
                    } else if (gameCase.getPlayers().size() == 0) {
                        ok = false;
                        break;
                    }
                }

                if(!ok) break;
            }

            if(ok) {
                this.open();
                Temporizador.addSiguiente(this::close, this.time, TimeUnit.SECONDS, Temporizador.DataType.MAPA);
            }
            return ok;
        }

        private void open() {
            if(this.state) return;

            this.setState(this.cellsEnable, true, false, null);
            this.setState(this.cellsDisable, false, false, null);
            this.setState(this.doorsEnable, true, true, null);
            this.setState(this.doorsDisable, false, true, null);
            this.state = true;
        }

        private void close() {
            if(!this.state) return;

            this.setState(this.cellsEnable, false, false, null);
            this.setState(this.cellsDisable, true, false, null);
            this.setState(this.doorsEnable, false, true, null);
            this.setState(this.doorsDisable, true, true, null);
            this.state = false;
        }

        //String a StringBuilder
        private void setState(Map<Short, ArrayList<Short>> arrayListMap, boolean active, boolean doors, Jugador player) {
            StringBuilder packet = new StringBuilder();
            packet.append("GDF");

            for(Entry<Short, ArrayList<Short>> entry : arrayListMap.entrySet()) {
                Mapa gameMap = Mundo.mundo.getMap(entry.getKey());
                if(gameMap == null) continue;
                if(player != null && player.getCurMap() != null && player.getCurMap().getId() != gameMap.getId()) continue;

                for(short cell : entry.getValue()) {
                    if(doors)
                        packet.append(this.setStateDoor(cell, active, player != null));
                    else
                        this.setStateCell(gameMap, cell, active, player);
                }

                if(player != null)
                    player.send(packet.toString());
                else for (Jugador target : gameMap.getPlayers())
                    target.send(packet.toString());
            }
        }

        private String setStateDoor(int cell, boolean active, boolean fast) {
            return "|" + cell + (!fast ? (active ? ";2" : ";4") : (active ? ";3" : ";1"));
        }

        private void setStateCell(Mapa gameMap, short cell, boolean active, Jugador player) {
            String packet = "GDC" + cell;
            GameCase gameCase = gameMap.getCase(cell), temporaryCell;
            gameMap.removeCase(cell);

            if(active) {
                temporaryCell = new GameCase(gameMap, cell, true, true, -1);
                temporaryCell.setOnCellStop(gameCase.getOnCellStop());
                gameMap.getCases().add(temporaryCell);
                packet += ";aaGaaaaaaa801;1";
            } else {
                temporaryCell = new GameCase(gameMap, cell, false, false, -1);
                temporaryCell.setOnCellStop(gameCase.getOnCellStop());
                gameMap.getCases().add(temporaryCell);
                packet += ";aaaaaaaaaa801;1";
            }

            if(player != null)
                player.send(packet);
            else for (Jugador target : gameMap.getPlayers())
                target.send(packet);
        }

        private static class Condition {

            public static boolean isValid(Jugador player, GameCase gameCase, String request) {
                JEP jep = new JEP();
                request = request.replace("&", "&&").replace("=", "==").replace("|", "||").replace("!", "!=").replace("~", "==");
                try {
                    // Item template cell
                    ObjetoJuego object = gameCase.getDroppedItem(false);
                    jep.addVariable("ITC", object != null ? object.getModelo().getId() : -1);

                    //Mob Group Cell
                    if(request.contains("MGC")) request = PuertasInteractivas.Condition.parseMGC(player, request);
                    //Parse request..
                    Node node = jep.parse(request);
                    Object result = jep.evaluate(node);
                    return result != null && Boolean.parseBoolean(result.toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return false;
            }

            private static String parseMGC(Jugador player, String request) {
                String[] data = request.split("==")[1].split("-");
                for(Monstruos.MobGroup mobGroup : player.getCurMap().getMobGroups().values())
                    for(String id : data)
                        if (mobGroup.getCellId() == Short.parseShort(id))
                            return "1==1";
                return "1==0";
            }
        }
    }
}
