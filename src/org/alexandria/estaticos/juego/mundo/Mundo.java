package org.alexandria.estaticos.juego.mundo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.alexandria.estaticos.Npc.*;
import org.alexandria.estaticos.area.Area;
import org.alexandria.estaticos.area.SubArea;
import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.area.mapa.entrada.*;
import org.alexandria.estaticos.area.mapa.laberintos.DragoCerdo;
import org.alexandria.estaticos.area.mapa.laberintos.Minotot;
import org.alexandria.estaticos.cliente.Cuenta;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.estaticos.cliente.Jugador.Caracteristicas;
import org.alexandria.comunes.Condiciones;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorEncriptador;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.configuracion.Constantes;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.estaticos.*;
import org.alexandria.estaticos.Recaudador;
import org.alexandria.estaticos.Mascota.MascotaEntrada;
import org.alexandria.estaticos.Monstruos;
import org.alexandria.estaticos.juego.JuegoServidor;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.objeto.ObjetoModelo;
import org.alexandria.estaticos.objeto.ObjetoSet;
import org.alexandria.estaticos.objeto.entrada.FragmentosMagicos;
import org.alexandria.estaticos.objeto.entrada.PiedraAlma;
import org.alexandria.estaticos.oficio.Oficio;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.otro.utilidad.Doble;
import org.alexandria.otro.utilidad.Temporizador;
import org.alexandria.estaticos.Mercadillo.*;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Mundo {

    public final static Mundo mundo = new Mundo();

    public Logger logger = (Logger) LoggerFactory.getLogger(Mundo.class);

    private final Map<Integer, Cuenta> cuenta = new HashMap<>();
    private final Map<Integer, Jugador> jugador = new HashMap<>();
    private final Map<Short, Mapa> mapa = new HashMap<>();
    private static final Map<Integer, ObjetoJuego> objetos = new ConcurrentHashMap<>();

    private final Map<Integer, ExpLevel> experiencia = new HashMap<>();
    private final Map<Integer, Hechizo> hechizos = new HashMap<>();
    private static final Map<Integer, ObjetoModelo> ObjTemplates = new HashMap<>();
    private final Map<Integer, Monstruos> MobTemplates = new HashMap<>();
    private final Map<Integer, NpcModelo> npcsTemplate = new HashMap<>();
    private final Map<Integer, NpcPregunta> questions = new HashMap<>();
    private final Map<Integer, NpcRespuesta> answers = new HashMap<>();
    private final Map<Integer, Mapa.ObjetosInteractivos.InteractiveObjectTemplate> IOTemplate = new HashMap<>();
    private final Map<Integer, Montura> Dragodindes = new HashMap<>();
    private final Map<Integer, Area> areas = new HashMap<>();
    private final Map<Integer, SubArea> subAreas = new HashMap<>();
    private final Map<Integer, Oficio> Jobs = new HashMap<>();
    private final Map<Integer, ArrayList<Doble<Integer, Integer>>> Crafts = new HashMap<>();
    private final Map<Integer, ObjetoSet> ItemSets = new HashMap<>();
    private final Map<Integer, Gremio> Guildes = new HashMap<>();
    private final Map<Integer, Mercadillo> Hdvs = new HashMap<>();
    private final Map<Integer, Map<Integer, ArrayList<MercadilloEntrada>>> hdvsItems = new HashMap<>();
    private final Map<Integer, Animaciones> Animations = new HashMap<>();
    private final Map<Short, Cercados> MountPark = new HashMap<>();
    private final Map<Integer, Cofres> Trunks = new HashMap<>();
    private final Map<Integer, Recaudador> collectors = new HashMap<>();
    private final Map<Integer, Casas> Houses = new HashMap<>();
    private final Map<Short, Collection<Integer>> Seller = new HashMap<>();
    private final StringBuilder Challenges = new StringBuilder();
    private final Map<Integer, Prisma> Prismes = new HashMap<>();
    private final Map<Integer, Map<String, String>> fullmorphs = new HashMap<>();
    private final Map<Integer, Mascota> Pets = new HashMap<>();
    private final Map<Integer, MascotaEntrada> PetsEntry = new HashMap<>();
    private final Map<String, Map<String, String>> mobsGroupsFix = new HashMap<>();
    private final Map<Integer, Map<String, Map<String, Integer>>> extraMonstre = new HashMap<>();
    private final Map<Integer, Mapa> extraMonstreOnMap = new HashMap<>();
    private final Map<Integer, Tutoriales> Tutorial = new HashMap<>();
    private final Map<Short, Long> delayCollectors = new HashMap<>();

    public Map<Short, Long> getDelayCollectors() {
        return delayCollectors;
    }

    private final Casas.GestorCasas houseManager = new Casas.GestorCasas();

    public Casas.GestorCasas getHouseManager() {
        return houseManager;
    }

    private final GestorEncriptador cryptManager = new GestorEncriptador();

    public GestorEncriptador getCryptManager() {
        return cryptManager;
    }

    private final Condiciones conditionManager = new Condiciones();

    public Condiciones getConditionManager() {
        return conditionManager;
    }

    public int getNumberOfThread() {
        int fight = getNumberOfFight();
        int player = getOnlinePlayers().size();
        return (fight + player) / 30;
    }

    public int getNumberOfFight() {
        final int[] fights = {0};
        this.mapa.values().forEach(map -> fights[0] += map.getFights().size());
        return fights[0];
    }

    private int nextObjectHdvId, nextLineHdvId;

    //region Accounts data
    public void addAccount(Cuenta account) {
        cuenta.put(account.getId(), account);
    }

    public Cuenta getAccount(int id) {
        return cuenta.get(id);
    }

    public Collection<Cuenta> getCuenta() {
        return cuenta.values();
    }

    public Map<Integer, Cuenta> getAccountsByIp(String ip) {
        Map<Integer, Cuenta> newAccounts = new HashMap<>();
        cuenta.values().stream().filter(account -> account.getLastIP().equalsIgnoreCase(ip)).forEach(account -> newAccounts.put(newAccounts.size(), account));
        return newAccounts;
    }

    public Cuenta getAccountByPseudo(String pseudo) {
        for (Cuenta account : cuenta.values())
            if (account.getPseudo().equals(pseudo))
                return account;
        return null;
    }
    //endregion

    //region Players data
    public Collection<Jugador> getJugador() {
        return jugador.values();
    }

    public void addPlayer(Jugador player) {
        jugador.put(player.getId(), player);
    }

    public Jugador getPlayerByName(String name) {
        for (Jugador player : jugador.values())
            if (player.getName().equalsIgnoreCase(name))
                return player;
        return null;
    }

    public Jugador getPlayer(int id) {
        return jugador.get(id);
    }

    public List<Jugador> getOnlinePlayers() {
        return jugador.values().stream().filter(player -> player.isOnline() && player.getGameClient() != null).collect(Collectors.toList());
    }
    //endregion

    //region Maps data
    public Collection<Mapa> getMapa() {
        return mapa.values();
    }

    public Mapa getMap(short id) {
        return mapa.get(id);
    }

    public void addMap(Mapa map) {
        if(map.getSubArea() != null && map.getSubArea().area.getId() == 42 && !Configuracion.INSTANCE.getNOEL())
            return;
        mapa.put(map.getId(), map);
    }
    //endregion

    //region Objects data
    public CopyOnWriteArrayList<ObjetoJuego> getGameObjects() {
        return new CopyOnWriteArrayList<>(objetos.values());
    }

    public static void addGameObject(ObjetoJuego gameObject, boolean saveSQL) {
        if (gameObject != null) {
            objetos.put(gameObject.getId(), gameObject);
            if (saveSQL)
                gameObject.modificaciones = 0;
        }
    }

    public static ObjetoJuego getGameObject(int guid) {
        return objetos.get(guid);
    }

    public void removeGameObject(int id) {
        objetos.remove(id);
        Database.dinamicos.getObjectData().delete(id);
    }
    //endregion

    public Map<Integer, Hechizo> getHechizos() {
        return hechizos;
    }

    public Map<Integer, ObjetoModelo> getObjectsTemplates() {
        return ObjTemplates;
    }

    public Map<Integer, NpcRespuesta> getAnswers() {
        return answers;
    }

    public Map<Integer, Montura> getMounts() {
        return Dragodindes;
    }

    public Map<Integer, Area> getAreas() {
        return areas;
    }

    public Map<Integer, SubArea> getSubAreas() {
        return subAreas;
    }

    public Map<Integer, Gremio> getGuilds() {
        return Guildes;
    }

    public Map<Short, Cercados> getMountparks() {
        return MountPark;
    }

    public Map<Integer, Cofres> getTrunks() {
        return Trunks;
    }

    public Map<Integer, Recaudador> getCollectors() {
        return collectors;
    }

    public Map<Integer, Casas> getHouses() {
        return Houses;
    }

    public Map<Integer, Prisma> getPrisms() {
        return Prismes;
    }

    public Map<Integer, Map<String, Map<String, Integer>>> getExtraMonsters() {
        return extraMonstre;
    }
    //Fin

    public void createWorld() {
        logger.info("Cargando datos...");
        long time = Instant.now().toEpochMilli();

        Database.dinamicos.getServerData().loggedZero();
        logger.info("Reinicio de los personajes logueados.");

        Database.dinamicos.getWorldEntityData().load(null);
        logger.info("Cargando las ID maximas del juego.");

        Database.dinamicos.getCommandData().load(null);
        logger.info("The administration commands were loaded successfully.");

        Database.dinamicos.getGroupData().load(null);
        logger.info("The administration groups were loaded successfully.");

        Database.dinamicos.getPubData().load(null);
        logger.info("The pubs were loaded successfully.");

        Database.estaticos.getFullMorphData().load();
        logger.info("The incarnations were loaded successfully.");

        Database.estaticos.getExtraMonsterData().load();
        logger.info("The extra-monsters were loaded successfully.");

        Database.estaticos.getExperienceData().load();
        logger.info("The experiences were loaded successfully.");

        Database.estaticos.getSpellData().load();
        logger.info("The spells were loaded successfully.");

        Database.estaticos.getMonsterData().load();
        logger.info("The monsters were loaded successfully.");

        Database.estaticos.getObjectTemplateData().load();
        logger.info("The template objects were loaded successfully.");

        Database.dinamicos.getObjectData().load();
        logger.info("The objects were loaded successfully.");

        Database.estaticos.getNpcTemplateData().load();
        logger.info("The non-player characters were loaded successfully.");

        Database.estaticos.getNpcQuestionData().load();
        logger.info("The n-p-c questions were loaded successfully.");

        Database.estaticos.getNpcAnswerData().load();
        logger.info("The n-p-c answers were loaded successfully.");

        Database.estaticos.getQuestObjectiveData().load();
        logger.info("The quest goals were loaded successfully.");

        Database.estaticos.getQuestStepData().load();
        logger.info("The quest steps were loaded successfully.");

        Database.estaticos.getQuestData().load();
        logger.info("The quests data were loaded successfully.");

        Database.estaticos.getNpcTemplateData().loadQuest();
        logger.info("The adding of quests on non-player characters was done successfully.");

        Database.estaticos.getPrismData().load();
        logger.info("The prisms were loaded successfully.");

        Database.estaticos.getAreaData().load();
        logger.info("The dynamics areas data were loaded successfully.");

        Database.estaticos.getSubAreaData().load();
        logger.info("The dynamics sub-areas data were loaded successfully.");

        Database.estaticos.getInteractiveDoorData().load();
        logger.info("The templates of interactive doors were loaded successfully.");

        Database.estaticos.getInteractiveObjectData().load();
        logger.info("The templates of interactive objects were loaded successfully.");

        Database.estaticos.getCraftData().load();
        logger.info("The crafts were loaded successfully.");

        Database.estaticos.getJobData().load();
        logger.info("The jobs were loaded successfully.");

        Database.estaticos.getObjectSetData().load();
        logger.info("The panoplies were loaded successfully.");

        Database.estaticos.getMapData().load();
        logger.info("The maps were loaded successfully.");

        Database.estaticos.getScriptedCellData().load();
        logger.info("The scripted cells were loaded successfully.");

        Database.estaticos.getEndFightActionData().load();
        logger.info("The end fight actions were loaded successfully.");

        Database.estaticos.getNpcData().load();
        logger.info("The placement of non-player character were done successfully.");

        Database.estaticos.getObjectActionData().load();
        logger.info("The action of objects were loaded successfully.");

        Database.estaticos.getDropData().load();
        logger.info("The drops were loaded successfully.");

        logger.info("The mounts were loaded successfully.");

        Database.estaticos.getAnimationData().load();
        logger.info("The animations were loaded successfully.");

        Database.dinamicos.getAccountData().load();
        logger.info("The accounts were loaded successfully.");

        Database.dinamicos.getPlayerData().load();
        logger.info("The players were loaded successfully.");

        Database.estaticos.getGuildMemberData().load();
        logger.info("The guilds and guild members were loaded successfully.");

        Database.dinamicos.getPetData().load();
        logger.info("The pets were loaded successfully.");

        Database.estaticos.getPetTemplateData().load();
        logger.info("The templates of pets were loaded successfully.");

        Database.estaticos.getTutorialData().load();
        logger.info("The tutorials were loaded successfully.");

        Database.dinamicos.getMountParkData().load();
        logger.info("The statics parks of the mounts were loaded successfully.");
        Database.estaticos.getMountParkData().load();
        logger.info("The dynamics parks of the mounts were loaded successfully.");

        Database.estaticos.getCollectorData().load();
        logger.info("The collectors were loaded successfully.");

        Database.dinamicos.getHouseData().load();
        logger.info("The statics houses were loaded successfully.");
        Database.estaticos.getHouseData().load();
        logger.info("The dynamics houses were loaded successfully.");

        Database.dinamicos.getTrunkData().load();
        logger.info("The statics trunks were loaded successfully.");
        Database.estaticos.getTrunkData().load();
        logger.info("The dynamics trunks were loaded successfully.");

        Database.estaticos.getZaapData().load();
        logger.info("The zaaps were loaded successfully.");

        Database.estaticos.getZaapiData().load();
        logger.info("The zappys were loaded successfully.");

        Database.estaticos.getChallengeData().load();
        logger.info("The challenges were loaded successfully.");

        Database.estaticos.getHdvData().load();
        logger.info("The hotels of sales were loaded successfully.");

        Database.estaticos.getHdvObjectData().load();
        logger.info("The objects of hotels were loaded successfully.");

        Database.estaticos.getDungeonData().load();
        logger.info("The dungeons were loaded successfully.");

        Database.estaticos.getRuneData().load(null);
        logger.info("The runes were loaded successfully.");

        loadExtraMonster();
        logger.info("The adding of extra-monsters on the maps were done successfully.");

        loadMonsterOnMap();
        logger.info("The adding of mobs groups on the maps were done successfully.");

        Database.estaticos.getGangsterData().load();
        logger.info("The adding of gangsters on the maps were done successfully.");

        logger.info("Initialization of the dungeon : Dragon Pig.");
        DragoCerdo.initialize();
        logger.info("Initialization of the dungeon : Labyrinth of the Minotoror.");
        Minotot.initialize();

        Database.dinamicos.getServerData().updateTime(time);
        logger.info("All data was loaded successfully at "
        + new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.FRANCE).format(new Date()) + " in "
                + new SimpleDateFormat("mm", Locale.FRANCE).format((Instant.now().toEpochMilli() - time)) + " min "
                + new SimpleDateFormat("ss", Locale.FRANCE).format((Instant.now().toEpochMilli() - time)) + " s.");
        logger.setLevel(Level.ALL);
    }

    public void addExtraMonster(int idMob, String superArea,
                                       String subArea, int chances) {
        Map<String, Map<String, Integer>> map = new HashMap<>();
        Map<String, Integer> _map = new HashMap<>();
        _map.put(subArea, chances);
        map.put(superArea, _map);
        extraMonstre.put(idMob, map);
    }

    public Map<Integer, Mapa> getExtraMonsterOnMap() {
        return extraMonstreOnMap;
    }

    public void loadExtraMonster() {
        ArrayList<Mapa> mapPossible = new ArrayList<>();
        for (Entry<Integer, Map<String, Map<String, Integer>>> i : extraMonstre.entrySet()) {
            try {
                Map<String, Map<String, Integer>> map = i.getValue();

                for (Entry<String, Map<String, Integer>> areaChances : map.entrySet()) {
                    Integer chances = null;
                    for (Entry<String, Integer> _e : areaChances.getValue().entrySet()) {
                        Integer _c = _e.getValue();
                        if (_c != null && _c != -1)
                            chances = _c;
                    }
                    if (!areaChances.getKey().equals("")) {// Si la superArea n'est pas null
                        for (String ar : areaChances.getKey().split(",")) {
                            Area Area = areas.get(Integer.parseInt(ar));
                            for (Mapa Map : Area.getMaps()) {
                                if (Map == null)
                                    continue;
                                if (Map.haveMobFix())
                                    continue;
                                if (!Map.isPossibleToPutMonster())
                                    continue;

                                if (chances != null)
                                    Map.addMobExtra(i.getKey(), chances);
                                else if (!mapPossible.contains(Map))
                                    mapPossible.add(Map);
                            }
                        }
                    }
                    if (areaChances.getValue() != null) // Si l'area n'est pas null
                    {
                        for (Entry<String, Integer> area : areaChances.getValue().entrySet()) {
                            String areas = area.getKey();
                            for (String sub : areas.split(",")) {
                                SubArea subArea = null;
                                try {
                                    subArea = subAreas.get(Integer.parseInt(sub));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (subArea == null)
                                    continue;
                                for (Mapa Map : subArea.maps) {
                                    if (Map == null)
                                        continue;
                                    if (Map.haveMobFix())
                                        continue;
                                    if (!Map.isPossibleToPutMonster())
                                        continue;

                                    if (chances != null)
                                        Map.addMobExtra(i.getKey(), chances);
                                    if (!mapPossible.contains(Map))
                                        mapPossible.add(Map);
                                }
                            }
                        }
                    }
                }
                if (mapPossible.size() <= 0) {
                    throw new Exception(" no maps was found for the extra monster " + i.getKey() +".");
                } else {
                    Mapa randomMap;
                    if (mapPossible.size() == 1)
                        randomMap = mapPossible.get(0);
                    else
                        randomMap = mapPossible.get(Formulas.getRandomValue(0, mapPossible.size() - 1));
                    if (randomMap == null)
                        throw new Exception("the random map is null.");
                    if (getMonstre(i.getKey()) == null)
                        throw new Exception("the monster template of the extra monster is invalid (id : " + i.getKey() + ").");
                    if (randomMap.loadExtraMonsterOnMap(i.getKey()))
                        extraMonstreOnMap.put(i.getKey(), randomMap);
                    else
                        throw new Exception("a empty mobs group or invalid monster.");
                }

                mapPossible.clear();
            } catch(Exception e) {
                e.printStackTrace();
                mapPossible.clear();
                logger.error("An error occurred when the server try to put extra-monster caused by : " + e.getMessage());
            }
        }
    }

    public Map<String, String> getGroupFix(int map, int cell) {
        return mobsGroupsFix.get(map + ";" + cell);
    }

    public void addGroupFix(String str, String mob, int Time) {
        mobsGroupsFix.put(str, new HashMap<>());
        mobsGroupsFix.get(str).put("groupData", mob);
        mobsGroupsFix.get(str).put("timer", Time + "");
    }

    public void loadMonsterOnMap() {
        Database.estaticos.getHeroicMobsGroups().load();
        mapa.values().stream().filter(Objects::nonNull).forEach(map -> {
            try {
                map.loadMonsterOnMap();
            } catch (Exception e) {
                logger.error("An error occurred when the server try to put monster on the map id " + map.getId() + ".");
            }
        });
    }

    public Area getArea(int areaID) {
        return areas.get(areaID);
    }


    public SubArea getSubArea(int areaID) {
        return subAreas.get(areaID);
    }

    public void addArea(Area area) {
        areas.put(area.getId(), area);
    }



    public void addSubArea(SubArea SA) {
        subAreas.put(SA.getId(), SA);
    }

    public String getSousZoneStateString() {
        StringBuilder str = new StringBuilder();
        boolean first = false;
        for (SubArea subarea : subAreas.values()) {
            if (!subarea.getConquistable())
                continue;
            if (first)
                str.append("|");
            str.append(subarea.getId()).append(";").append(subarea.getAlignement());
            first = true;
        }
        return str.toString();
    }

    public void addNpcAnswer(NpcRespuesta rep) {
        answers.put(rep.getId(), rep);
    }

    public NpcRespuesta getNpcAnswer(int guid) {
        return answers.get(guid);
    }

    public double getBalanceArea(Area area, int alignement) {
        int cant = 0;
        for (SubArea subarea : subAreas.values()) {
            if (subarea.area == area
                    && subarea.getAlignement() == alignement)
                cant++;
        }
        if (cant == 0)
            return 0;
        return Math.rint((1000 * cant / (area.subAreas.size())) / 10);
    }

    public double getBalanceWorld(int alignement) {
        int cant = 0;
        for (SubArea subarea : subAreas.values()) {
            if (subarea.getAlignement() == alignement)
                cant++;
        }
        if (cant == 0)
            return 0;
        return Math.rint((10 * cant / 4) / 10);
    }

    public double getConquestBonus(Jugador player) {
        if(player == null) return 1;
        if(player.get_align() == 0) return 1;
        final double factor = 1 + (getBalanceWorld(player.get_align()) * Math.rint((player.getGrade() / 2.5) + 1)) / 100;
        if(factor < 1) return 1;
        return factor;
    }

    public int getExpLevelSize() {
        return experiencia.size();
    }

    public void addExpLevel(int lvl, ExpLevel exp) {
        experiencia.put(lvl, exp);
    }



    public void addNPCQuestion(NpcPregunta quest) {
        questions.put(quest.getId(), quest);
    }

    public NpcPregunta getNPCQuestion(int guid) {
        return questions.get(guid);
    }

    public NpcModelo getNPCTemplate(int guid) {
        return npcsTemplate.get(guid);
    }

    public void addNpcTemplate(NpcModelo temp) {
        npcsTemplate.put(temp.getId(), temp);
    }



    public void removePlayer(Jugador player) {
        if (player.getGuild() != null) {
            if (player.getGuild().getPlayers().size() <= 1) {
                removeGuild(player.getGuild().getId());
            } else if (player.getGuildMember().getRank() == 1) {
                int curMaxRight = 0;
                Jugador leader = null;

                for (Jugador newLeader : player.getGuild().getPlayers())
                    if (newLeader != player && newLeader.getGuildMember().getRights() < curMaxRight)
                        leader = newLeader;

                player.getGuild().removeMember(player);
                if(leader != null)
                    leader.getGuildMember().setRank(1);
            } else {
                player.getGuild().removeMember(player);
            }
        }
        if(player.getWife() != 0) {
            Jugador wife = getPlayer(player.getWife());

            if(wife != null) {
                wife.setWife(0);
            }
        }
        player.remove();
        unloadPerso(player.getId());
        jugador.remove(player.getId());
    }

    public void unloadPerso(Jugador perso) {
        unloadPerso(perso.getId());//UnLoad du perso+item
        jugador.remove(perso.getId());
    }

    public long getPersoXpMin(int _lvl) {
        if (_lvl > getExpLevelSize())
            _lvl = getExpLevelSize();
        if (_lvl < 1)
            _lvl = 1;
        return experiencia.get(_lvl).perso;
    }

    public long getPersoXpMax(int _lvl) {
        if (_lvl >= getExpLevelSize())
            _lvl = (getExpLevelSize() - 1);
        if (_lvl <= 1)
            _lvl = 1;
        return experiencia.get(_lvl + 1).perso;
    }

    public long getTourmenteursXpMax(int _lvl) {
        if (_lvl >= getExpLevelSize())
            _lvl = (getExpLevelSize() - 1);
        if (_lvl <= 1)
            _lvl = 1;
        return experiencia.get(_lvl + 1).tourmenteurs;
    }

    public long getBanditsXpMin(int _lvl) {
        if (_lvl > getExpLevelSize())
            _lvl = getExpLevelSize();
        if (_lvl < 1)
            _lvl = 1;
        return experiencia.get(_lvl).bandits;
    }

    public long getBanditsXpMax(int _lvl) {
        if (_lvl >= getExpLevelSize())
            _lvl = (getExpLevelSize() - 1);
        if (_lvl <= 1)
            _lvl = 1;
        return experiencia.get(_lvl + 1).bandits;
    }

    public void addSort(Hechizo sort) {
        hechizos.put(sort.getSpellID(), sort);
    }

    public Hechizo getSort(int id) {
        return hechizos.get(id);
    }

    public void addObjTemplate(ObjetoModelo obj) {
        ObjTemplates.put(obj.getId(), obj);
    }

    public ObjetoModelo getObjetoModelo(int id) {
        return ObjTemplates.get(id);
    }

    public ArrayList<ObjetoModelo> getEtherealWeapons(int level) {
        ArrayList<ObjetoModelo> array = new ArrayList<>();
        final int levelMin = (Math.max(level - 5, 0)), levelMax = level + 5;
        getObjectsTemplates().values().stream().filter(objectTemplate -> objectTemplate != null && objectTemplate.getStrTemplate().contains("32c#")
                && (levelMin < objectTemplate.getLevel() && objectTemplate.getLevel() < levelMax) && objectTemplate.getType() != 93).forEach(array::add);
        return array;
    }

    public void addMobTemplate(int id, Monstruos mob) {
        MobTemplates.put(id, mob);
    }

    public Monstruos getMonstre(int id) {
        return MobTemplates.get(id);
    }

    public Collection<Monstruos> getMonstres() {
        return MobTemplates.values();
    }


    public String getStatOfAlign() {
        int ange = 0;
        int demon = 0;
        int total = 0;
        for (Jugador i : getJugador()) {
            if (i == null)
                continue;
            if (i.get_align() == 1)
                ange++;
            if (i.get_align() == 2)
                demon++;
            total++;
        }
        ange = ange / total;
        demon = demon / total;
        if (ange > demon)
            return "Les Brâkmarien sont actuellement en minorité, je peux donc te proposer de rejoindre les rangs Brâkmarien ?";
        else if (demon > ange)
            return "Les Bontarien sont actuellement en minorité, je peux donc te proposer de rejoindre les rangs Bontarien ?";
        else if (demon == ange)
            return " Aucune milice est actuellement en minorité, je peux donc te proposer de rejoindre aléatoirement une milice ?";
        return "Undefined";
    }





    public void addIOTemplate(Mapa.ObjetosInteractivos.InteractiveObjectTemplate IOT) {
        IOTemplate.put(IOT.getId(), IOT);
    }

    public Montura getMountById(int id) {

        Montura mount = Dragodindes.get(id);
        if(mount == null) {
            Database.dinamicos.getMountData().load(id);
            mount = Dragodindes.get(id);
        }
        return mount;
    }

    public void addMount(Montura mount) {
        Dragodindes.put(mount.getId(), mount);
    }

    public void removeMount(int id) {
        Dragodindes.remove(id);
    }

    public void addTutorial(Tutoriales tutorial) {
        Tutorial.put(tutorial.id, tutorial);
    }

    public Tutoriales getTutorial(int id) {
        return Tutorial.get(id);
    }

    public ExpLevel getExpLevel(int lvl) {
        return experiencia.get(lvl);
    }

    public Mapa.ObjetosInteractivos.InteractiveObjectTemplate getIOTemplate(int id) {
        return IOTemplate.get(id);
    }

    public Oficio getMetier(int id) {
        return Jobs.get(id);
    }

    public void addJob(Oficio metier) {
        Jobs.put(metier.getId(), metier);
    }

    public void addCraft(int id, ArrayList<Doble<Integer, Integer>> m) {
        Crafts.put(id, m);
    }

    public ArrayList<Doble<Integer, Integer>> getCraft(int i) {
        return Crafts.get(i);
    }

    public void addFullMorph(int morphID, String name, int gfxID, String spells, String[] args) {
        if (fullmorphs.get(morphID) != null)
            return;

        fullmorphs.put(morphID, new HashMap<>());

        fullmorphs.get(morphID).put("name", name);
        fullmorphs.get(morphID).put("gfxid", gfxID + "");
        fullmorphs.get(morphID).put("spells", spells);
        if (args != null) {
            fullmorphs.get(morphID).put("vie", args[0]);
            fullmorphs.get(morphID).put("pa", args[1]);
            fullmorphs.get(morphID).put("pm", args[2]);
            fullmorphs.get(morphID).put("vitalite", args[3]);
            fullmorphs.get(morphID).put("sagesse", args[4]);
            fullmorphs.get(morphID).put("terre", args[5]);
            fullmorphs.get(morphID).put("feu", args[6]);
            fullmorphs.get(morphID).put("eau", args[7]);
            fullmorphs.get(morphID).put("air", args[8]);
            fullmorphs.get(morphID).put("initiative", args[9]);
            fullmorphs.get(morphID).put("stats", args[10]);
            fullmorphs.get(morphID).put("donjon", args[11]);
        }
    }

    public Map<String, String> getFullMorph(int morphID) {
        return fullmorphs.get(morphID);
    }

    public int getObjectByIngredientForJob(ArrayList<Integer> list,
                                                  Map<Integer, Integer> ingredients) {
        if (list == null)
            return -1;
        for (int tID : list) {
            ArrayList<Doble<Integer, Integer>> craft = getCraft(tID);
            if (craft == null)
                continue;
            if (craft.size() != ingredients.size())
                continue;
            boolean ok = true;
            for (Doble<Integer, Integer> c : craft) {
                if (!((ingredients.get(c.getPrimero()) + " ").equals(c.getSegundo() + " "))) //si ingredient non pr�sent ou mauvaise quantit�
                    ok = false;
            }
            if (ok)
                return tID;
        }
        return -1;
    }



    public void addItemSet(ObjetoSet itemSet) {
        ItemSets.put(itemSet.getId(), itemSet);
    }

    public ObjetoSet getItemSet(int tID) {
        return ItemSets.get(tID);
    }

    public int getItemSetNumber() {
        return ItemSets.size();
    }

    public ArrayList<Mapa> getMapByPosInArray(int mapX, int mapY) {
        ArrayList<Mapa> i = new ArrayList<>();
        for (Mapa map : mapa.values())
            if (map.getX() == mapX && map.getY() == mapY)
                i.add(map);
        return i;
    }

    public ArrayList<Mapa> getMapByPosInArrayPlayer(int mapX, int mapY, Jugador player) {
        return mapa.values().stream().filter(map -> map != null && map.getSubArea() != null && player.getCurMap().getSubArea() != null).filter(map -> map.getX() == mapX && map.getY() == mapY && map.getSubArea().area.getSuperArea() == player.getCurMap().getSubArea().area.getSuperArea()).collect(Collectors.toCollection(ArrayList::new));
    }

    public void addGuild(Gremio g, boolean save) {
        Guildes.put(g.getId(), g);
        if (save)
            Database.dinamicos.getGuildData().add(g);
    }

    public boolean guildNameIsUsed(String name) {
        for (Gremio g : Guildes.values())
            if (g.getName().equalsIgnoreCase(name))
                return true;
        return false;
    }

    public boolean guildEmblemIsUsed(String emb) {
        for (Gremio g : Guildes.values()) {
            if (g.getEmblem().equals(emb))
                return true;
        }
        return false;
    }

    public Gremio getGuild(int i) {
        Gremio guild = Guildes.get(i);
        if(guild == null) {
            Database.dinamicos.getGuildData().load(i);
            guild = Guildes.get(i);
        }
        return guild;
    }

    public int getGuildByName(String name) {
        for (Gremio g : Guildes.values()) {
            if (g.getName().equalsIgnoreCase(name))
                return g.getId();
        }
        return -1;
    }

    public long getGuildXpMax(int _lvl) {
        if (_lvl >= 200)
            _lvl = 199;
        if (_lvl <= 1)
            _lvl = 1;
        return experiencia.get(_lvl + 1).guilde;
    }

    public void ReassignAccountToChar(Cuenta account) {
        Database.dinamicos.getPlayerData().loadByAccountId(account.getId());
        jugador.values().stream().filter(player -> player.getAccID() == account.getId()).forEach(player -> player.setAccount(account));
    }

    public int getZaapCellIdByMapId(short i) {
        for (Entry<Integer, Integer> zaap : Constantes.ZAAPS.entrySet()) {
            if (zaap.getKey() == i)
                return zaap.getValue();
        }
        return -1;
    }

    public int getEncloCellIdByMapId(short i) {
        Mapa map = getMap(i);
        if(map != null && map.getMountPark() != null && map.getMountPark().getCell() > 0)
            return map.getMountPark().getCell();
        return -1;
    }

    public void delDragoByID(int getId) {
        Dragodindes.remove(getId);
    }

    public void removeGuild(int id) {
        this.getHouseManager().removeHouseGuild(id);
        Mapa.removeMountPark(id);
        Recaudador.removeCollector(id);
        Guildes.remove(id);
        Database.estaticos.getGuildMemberData().deleteAll(id);
        Database.dinamicos.getGuildData().delete(id);
    }

    public void unloadPerso(int g) {
        Jugador toRem = jugador.get(g);
        if (!toRem.getItems().isEmpty())
            for (Entry<Integer, ObjetoJuego> curObj : toRem.getItems().entrySet())
                objetos.remove(curObj.getKey());

    }

    public ObjetoJuego newObjet(int id, int template, int qua, int pos, String stats, int puit) {
        if (getObjetoModelo(template) == null) {
            return null;
        }

        if (template == 8378) {
            return new FragmentosMagicos(id, stats);
        } else if (getObjetoModelo(template).getType() == 85) {
            return new PiedraAlma(id, qua, template, pos, stats);
        } else if (getObjetoModelo(template).getType() == 24 && (Constantes.isCertificatDopeuls(getObjetoModelo(template).getId()) || getObjetoModelo(template).getId() == 6653)) {
            try {
                Map<Integer, String> txtStat = new HashMap<>();
                txtStat.put(Constantes.STATS_DATE, stats.substring(3) + "");
                return new ObjetoJuego(id, template, qua, Constantes.ITEM_POS_NO_EQUIPED, new Caracteristicas(false, null), new ArrayList<>(), new HashMap<>(), txtStat, puit);
            } catch (Exception e) {
                e.printStackTrace();
                return new ObjetoJuego(id, template, qua, pos, stats, 0);
            }
        } else {
            return new ObjetoJuego(id, template, qua, pos, stats, 0);
        }
    }

    //Mercadillos unificados - Brakmar, Bonta y Astrub
    public Map<Integer, Integer> getChangeHdv() {
        Map<Integer, Integer> changeHdv = new HashMap<>();
        changeHdv.put(8753, 8759); //Mercadillo de los animales
        changeHdv.put(4607, 4271); //Mercadillo de los alquimistas
        changeHdv.put(7516, 4271); //Astrub - Joyeros
        changeHdv.put(4622, 4216); //Mercadillo de los joyeros
        changeHdv.put(7514, 4216); //Astrub - Joyeros
        changeHdv.put(4627, 4232); //Mercadillo de las manitas
        changeHdv.put(5112, 4178); //Mercadillo de los leñadores
        changeHdv.put(7289, 4178); //Astrub - Leñadores
        changeHdv.put(4562, 4183); //Mercadillo de los zapateros
        changeHdv.put(7602, 4183); //Astrub - Zapateros
        changeHdv.put(8754, 8760); //Mercadillo de los documentos
        changeHdv.put(5317, 4098); //Mercadillo de los herreros
        changeHdv.put(7511, 4098); //Astrub - Herreros
        changeHdv.put(4615, 4247); //Mercadillo de los pescadores
        changeHdv.put(7348, 4247); //Astrub - Pescadores
        changeHdv.put(7501, 4247); //Astrub - Pescaderos
        changeHdv.put(4646, 4262); //Mercadillo de los recursos
        changeHdv.put(7413, 4262); //Astrub - Recursos
        changeHdv.put(8756, 8757); //Mercadillo de los forjamagos
        changeHdv.put(4618, 4174); //Mercadillo de los escultores
        changeHdv.put(7512, 4174); //Astrub - Escultores
        changeHdv.put(4588, 4172); //Mercadillo de los sastres
        changeHdv.put(7513, 4172); //Astrub - Sastres
        changeHdv.put(8482, 10129); //Mercadillo de las almas
        changeHdv.put(4595, 4287); //Mercadillo de los carniceros
        changeHdv.put(7350, 4287); //Astrub - Carniceros
        changeHdv.put(7515, 4287); //Astrub - Cazadores
        changeHdv.put(4630, 2221); //Mercadillo de los panaderos
        changeHdv.put(7510, 2221); //Astrub - Panaderos
        changeHdv.put(5311, 4179); //Mercadillo de los mineros
        changeHdv.put(7443, 4179); //Astrub - Mineros
        changeHdv.put(4629, 4299); //Mercadillo de los campesinos
        changeHdv.put(7397, 4299); //Astrub - Campesinos
        return changeHdv;
    }

    // Utilis� deux fois. Pour tous les modes HDV dans la fonction getHdv ci-dessous et dans le mode Vente de GameClient.java
    public int changeHdv(int map) {
        Map<Integer, Integer> changeHdv = getChangeHdv();
        if (changeHdv.containsKey(map)) {
            map = changeHdv.get(map);
        }
        return map;
    }

    public Mercadillo getHdv(int map) {
        return Hdvs.get(changeHdv(map));
    }

    public synchronized int getNextObjectHdvId() {
        nextObjectHdvId++;
        return nextObjectHdvId;
    }

    public synchronized void setNextObjectHdvId(int id) {
        nextObjectHdvId = id;
    }

    public synchronized int getNextLineHdvId() {
        nextLineHdvId++;
        return nextLineHdvId;
    }

    public void addHdvItem(int compteID, int hdvID, MercadilloEntrada toAdd) {
        //Si le compte n'est pas dans la memoire
        hdvsItems.computeIfAbsent(compteID, k -> new HashMap<>()); //Ajout du compte cl�:compteID et un nouveau Map<hdvID,items<>>
        hdvsItems.get(compteID).computeIfAbsent(hdvID, k -> new ArrayList<>());
        hdvsItems.get(compteID).get(hdvID).add(toAdd);
    }

    public void removeHdvItem(int compteID, int hdvID, MercadilloEntrada toDel) {
        hdvsItems.get(compteID).get(hdvID).remove(toDel);
    }

    public void addHdv(Mercadillo toAdd) {
        Hdvs.put(toAdd.getHdvId(), toAdd);
    }

    public Map<Integer, ArrayList<MercadilloEntrada>> getMyItems(
            int compteID) {
        //Si le compte n'est pas dans la memoire
        hdvsItems.computeIfAbsent(compteID, k -> new HashMap<>());//Ajout du compte cl�:compteID et un nouveau Map<hdvID,items
        return hdvsItems.get(compteID);
    }

    public Collection<ObjetoModelo> getObjTemplates() {
        return ObjTemplates.values();
    }

    public void priestRequest(Jugador boy, Jugador girl, Jugador asked) {
        if(boy.getSexe() == 0 && girl.getSexe() == 1) {
            final Mapa map = boy.getCurMap();
            if (boy.getWife() != 0) {// 0 : femme | 1 = homme
                boy.setBlockMovement(false);
                GestorSalida.GAME_SEND_MESSAGE_TO_MAP(map, boy.getName() + " est déjà marier !", Configuracion.INSTANCE.getColorMessage());
                return;
            }
            if (girl.getWife() != 0) {
                boy.setBlockMovement(false);
                GestorSalida.GAME_SEND_MESSAGE_TO_MAP(map, girl.getName() + " est déjà marier !", Configuracion.INSTANCE.getColorMessage());
                return;
            }
            GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(map, "", -1, "Prêtre", asked.getName() + " acceptez-vous d'épouser " + (asked.getSexe() == 1 ? girl : boy).getName() + " ?");
            GestorSalida.GAME_SEND_WEDDING(map, 617, (boy == asked ? boy.getId() : girl.getId()), (boy == asked ? girl.getId() : boy.getId()), -1);
        }
    }


    public void wedding(Jugador boy, Jugador girl, int isOK) {
        if (isOK > 0) {
            GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(boy.getCurMap(), "", -1, "Prêtre", "Je déclare "
                    + boy.getName() + " et " + girl.getName() + " unis par les liens sacrés du mariage.");
            boy.setWife(girl.getId());
            girl.setWife(boy.getId());
        } else {
            GestorSalida.GAME_SEND_Im_PACKET_TO_MAP(boy.getCurMap(), "048;" + boy.getName() + "~" + girl.getName());
        }
        boy.setisOK(0);
        boy.setBlockMovement(false);
        girl.setisOK(0);
        girl.setBlockMovement(false);
    }

    public Animaciones getAnimation(int AnimationId) {
        return Animations.get(AnimationId);
    }

    public void addAnimation(Animaciones animation) {
        Animations.put(animation.getId(), animation);
    }

    public void addHouse(Casas house) {
        Houses.put(house.getId(), house);
    }

    public Casas getHouse(int id) {
        return Houses.get(id);
    }

    public void addCollector(Recaudador Collector) {
        collectors.put(Collector.getId(), Collector);
    }

    public Recaudador getCollector(int CollectorID) {
        return collectors.get(CollectorID);
    }

    public void addTrunk(Cofres trunk) {
        Trunks.put(trunk.getId(), trunk);
    }

    public Cofres getTrunk(int id) {
        return Trunks.get(id);
    }

    public void addMountPark(Cercados mp) {
        MountPark.put(mp.getMap().getId(), mp);
    }

    public Map<Short, Cercados> getMountPark() {
        return MountPark;
    }

    public String parseMPtoGuild(int GuildID) {
        Gremio G = getGuild(GuildID);
        byte enclosMax = (byte) Math.floor(G.getLvl() / 10);
        StringBuilder packet = new StringBuilder();
        packet.append(enclosMax);

        for (Entry<Short, Cercados> mp : MountPark.entrySet()) {
            if (mp.getValue().getGuild() != null
                    && mp.getValue().getGuild().getId() == GuildID) {
                packet.append("|").append(mp.getValue().getMap().getId()).append(";").append(mp.getValue().getSize()).append(";").append(mp.getValue().getMaxObject());// Nombre d'objets pour le dernier
                if (mp.getValue().getListOfRaising().size() > 0) {
                    packet.append(";");
                    boolean primero = false;
                    for (Integer id : mp.getValue().getListOfRaising()) {
                        Montura dd = getMountById(id);
                        if (dd != null) {
                            if (primero)
                                packet.append(",");
                            packet.append(dd.getColor()).append(",").append(dd.getName()).append(",");
                            if (getPlayer(dd.getOwner()) == null)
                                packet.append("Sans maitre");
                            else
                                packet.append(getPlayer(dd.getOwner()).getName());
                            primero = true;
                        }
                    }
                }
            }
        }
        return packet.toString();
    }

    public int totalMPGuild(int GuildID) {
        int i = 0;
        for (Entry<Short, Cercados> mp : MountPark.entrySet())
            if (mp.getValue().getGuild() != null && mp.getValue().getGuild().getId() == GuildID)
                i++;
        return i;
    }

    public void addChallenge(String chal) {
        if (!Challenges.toString().isEmpty())
            Challenges.append(";");
        Challenges.append(chal);
    }

    public synchronized void addPrisme(Prisma Prisme) {
        Prismes.put(Prisme.getId(), Prisme);
    }

    public Prisma getPrisme(int id) {
        return Prismes.get(id);
    }

    public void removePrisme(int id) {
        Prismes.remove(id);
    }

    public Collection<Prisma> AllPrisme() {
        if (Prismes.size() > 0)
            return Prismes.values();
        return null;
    }

    public String PrismesGeoposition(int alignement) {
        StringBuilder str = new StringBuilder();
        boolean first = false;
        int subareas = 0;
        for (SubArea subarea : subAreas.values()) {
            if (!subarea.getConquistable())
                continue;
            if (first)
                str.append(";");
            str.append(subarea.getId()).append(",").append(subarea.getAlignement() == 0 ? -1 : subarea.getAlignement()).append(",0,");
            if (getPrisme(subarea.prismId) == null)
                str.append(0 + ",1");
            else
                str.append(subarea.prismId == 0 ? 0 : getPrisme(subarea.prismId).getMap()).append(",1");
            first = true;
            subareas++;
        }
        if (alignement == 1)
            str.append("|").append(Area.bontarians);
        else if (alignement == 2)
            str.append("|").append(Area.brakmarians);
        str.append("|").append(areas.size()).append("|");
        first = false;
        for (Area area : areas.values()) {
            if (area.getAlignement() == 0)
                continue;
            if (first)
                str.append(";");
            str.append(area.getId()).append(",").append(area.getAlignement()).append(",1,").append(area.prismId == 0 ? 0 : 1);
            first = true;
        }
        if (alignement == 1)
            str.insert(0, Area.bontarians + "|" + subareas + "|"
                    + (subareas - (SubArea.bontarians + SubArea.brakmarians)) + "|");
        else if (alignement == 2)
            str.insert(0, Area.brakmarians + "|" + subareas + "|"
                    + (subareas - (SubArea.bontarians + SubArea.brakmarians)) + "|");
        return str.toString();
    }

    public void showPrismes(Jugador perso) {
        for (SubArea subarea : subAreas.values()) {
            if (subarea.getAlignement() == 0)
                continue;
            GestorSalida.GAME_SEND_am_ALIGN_PACKET_TO_SUBAREA(perso, subarea.getId()
                    + "|" + subarea.getAlignement() + "|1");
        }
    }

    public synchronized int getNextIDPrisme() {
        int max = -102;
        for (int a : Prismes.keySet())
            if (a < max)
                max = a;
        return max - 3;
    }

    public void addPets(Mascota pets) {
        Pets.put(pets.getIdmodelo(), pets);
    }

    public Mascota getPets(int Tid) {
        return Pets.get(Tid);
    }

    public Collection<Mascota> getPets() {
        return Pets.values();
    }

    public void addPetsEntry(MascotaEntrada pets) {
        PetsEntry.put(pets.getObjetoid(), pets);
    }

    public MascotaEntrada getPetsEntry(int guid) {
        return PetsEntry.get(guid);
    }

    public void removePetsEntry(int guid) {
        PetsEntry.remove(guid);
    }

    public String getChallengeFromConditions(boolean sevEnn,
                                                    boolean sevAll, boolean bothSex, boolean EvenEnn, boolean MoreEnn,
                                                    boolean hasCaw, boolean hasChaf, boolean hasRoul, boolean hasArak,
                                                    int isBoss, boolean ecartLvlPlayer, boolean hasArround,
                                                    boolean hasDisciple, boolean isSolo) {
        StringBuilder toReturn = new StringBuilder();
        boolean isFirst = true, isGood = false;
        int cond;

        for (String chal : Challenges.toString().split(";")) {
            if (!isFirst && isGood)
                toReturn.append(";");
            isGood = true;
            int id = Integer.parseInt(chal.split(",")[0]);
            cond = Integer.parseInt(chal.split(",")[4]);
            //Necessite plusieurs ennemis
            if (((cond & 1) == 1) && !sevEnn)
                isGood = false;
            //Necessite plusieurs allies
            if ((((cond >> 1) & 1) == 1) && !sevAll)
                isGood = false;
            //Necessite les deux sexes
            if ((((cond >> 2) & 1) == 1) && !bothSex)
                isGood = false;
            //Necessite un nombre pair d'ennemis
            if ((((cond >> 3) & 1) == 1) && !EvenEnn)
                isGood = false;
            //Necessite plus d'ennemis que d'allies
            if ((((cond >> 4) & 1) == 1) && !MoreEnn)
                isGood = false;
            //Jardinier
            if (!hasCaw && (id == 7))
                isGood = false;
            //Fossoyeur
            if (!hasChaf && (id == 12))
                isGood = false;
            //Casino Royal
            if (!hasRoul && (id == 14))
                isGood = false;
            //Araknophile
            if (!hasArak && (id == 15))
                isGood = false;
            //Les mules d'abord
            if (!ecartLvlPlayer && (id == 48))
                isGood = false;
            //Contre un boss de donjon
            if (isBoss != -1 && id == 5)
                isGood = false;
            //Hardi
            if (!hasArround && id == 36)
                isGood = false;
            //Mains propre
            if (!hasDisciple && id == 19)
                isGood = false;

            switch (id) {
                case 47:
                case 46:
                case 45:
                case 44:
                    if (isSolo)
                        isGood = false;
                    break;
            }

            switch (isBoss) {
                case 1045://Kimbo
                    isGood = switch (id) {
                        case 37, 8, 1, 2 -> false;
                        default -> isGood;
                    };
                    break;
                case 1072://Tynril
                case 1085://Tynril
                case 1086://Tynril
                case 1087://Tynril
                    isGood = switch (id) {
                        case 36, 20 -> false;
                        default -> isGood;
                    };
                    break;
                case 1071://Rasboul Majeur
                    isGood = switch (id) {
                        case 9, 22, 17, 47 -> false;
                        default -> isGood;
                    };
                    break;
                case 780://Skeunk
                    isGood = switch (id) {
                        case 35, 25, 4, 32, 3, 31, 34 -> false;
                        default -> isGood;
                    };
                    break;
                case 113://DC
                    isGood = switch (id) {
                        case 12, 15, 7, 41 -> false;
                        default -> isGood;
                    };
                    break;
                case 612://Maitre pandore
                    isGood = switch (id) {
                        case 20, 37 -> false;
                        default -> isGood;
                    };
                    break;
                case 478://Bworker
                case 568://Tanukoui san
                case 940://Rat blanc
                    if (id == 20) {
                        isGood = false;
                    }
                    break;
                case 1188://Blop multi
                    isGood = switch (id) {
                        case 20, 46, 44 -> false;
                        default -> isGood;
                    };
                    break;

                case 865://Grozila
                case 866://Grasmera
                    isGood = switch (id) {
                        case 31, 32 -> false;
                        default -> isGood;
                    };
                    break;

            }
            if (isGood)
                toReturn.append(chal);
            isFirst = false;
        }
        return toReturn.toString();
    }

    public void verifyClone(Jugador p) {
        if (p.getCurCell() != null && p.getPelea() == null) {
            if (p.getCurCell().getPlayers().contains(p)) {
                p.getCurCell().removePlayer(p);
                Database.dinamicos.getPlayerData().update(p);
            }
        }
        if (p.isOnline())
            Database.dinamicos.getPlayerData().update(p);
    }

    public ArrayList<String> getRandomChallenge(int nombreChal,
                                                       String challenges) {
        String MovingChals = ";1;2;8;36;37;39;40;";// Challenges de d�placements incompatibles
        boolean hasMovingChal = false;
        String TargetChals = ";3;4;10;25;31;32;34;35;38;42;";// ceux qui ciblent
        boolean hasTargetChal = false;
        String SpellChals = ";5;6;9;11;19;20;24;41;";// ceux qui obligent � caster sp�cialement
        boolean hasSpellChal = false;
        String KillerChals = ";28;29;30;44;45;46;48;";// ceux qui disent qui doit tuer
        boolean hasKillerChal = false;
        String HealChals = ";18;43;";// ceux qui emp�chent de soigner
        boolean hasHealChal = false;

        int compteur = 0, i;
        ArrayList<String> toReturn = new ArrayList<>();
        String chal;
        while (compteur < 100 && toReturn.size() < nombreChal) {
            compteur++;
            i = Formulas.getRandomValue(1, challenges.split(";").length);
            chal = challenges.split(";")[i - 1];// challenge au hasard dans la liste

            if (!toReturn.contains(chal))// si le challenge n'y etait pas encore
            {
                if (MovingChals.contains(";" + chal.split(",")[0] + ";"))// s'il appartient a une liste
                    if (!hasMovingChal)// et qu'aucun de la liste n'a ete choisi deja
                    {
                        hasMovingChal = true;
                        toReturn.add(chal);
                        continue;
                    } else
                        continue;
                if (TargetChals.contains(";" + chal.split(",")[0] + ";"))
                    if (!hasTargetChal) {
                        hasTargetChal = true;
                        toReturn.add(chal);
                        continue;
                    } else
                        continue;
                if (SpellChals.contains(";" + chal.split(",")[0] + ";"))
                    if (!hasSpellChal) {
                        hasSpellChal = true;
                        toReturn.add(chal);
                        continue;
                    } else
                        continue;
                if (KillerChals.contains(";" + chal.split(",")[0] + ";"))
                    if (!hasKillerChal) {
                        hasKillerChal = true;
                        toReturn.add(chal);
                        continue;
                    } else
                        continue;
                if (HealChals.contains(";" + chal.split(",")[0] + ";"))
                    if (!hasHealChal) {
                        hasHealChal = true;
                        toReturn.add(chal);
                        continue;
                    } else
                        continue;
                toReturn.add(chal);
            }
            compteur++;
        }
        return toReturn;
    }

    public Recaudador getCollectorByMap(int id) {

        for (Entry<Integer, Recaudador> Collector : getCollectors().entrySet()) {
            Mapa map = getMap(Collector.getValue().getMap());
            if (map.getId() == id) {
                return Collector.getValue();
            }
        }
        return null;
    }

    public void reloadPlayerGroup() {
        JuegoServidor.getClients().stream().filter(client -> client != null && client.getPlayer() != null).forEach(client -> Database.dinamicos.getPlayerData().reloadGroup(client.getPlayer()));
    }

    public void reloadDrops() {
        Database.estaticos.getDropData().reload();
    }

    public void reloadEndFightActions() {
        Database.estaticos.getEndFightActionData().reload();
    }

    public void reloadNpcs() {
        Database.estaticos.getNpcTemplateData().reload();
        questions.clear();
        Database.estaticos.getNpcQuestionData().load();
        answers.clear();
        Database.estaticos.getNpcAnswerData().load();
    }

    public void reloadHouses() {
        Houses.clear();
        Database.dinamicos.getHouseData().load();
        Database.estaticos.getHouseData().load();
    }

    public void reloadTrunks() {
        Trunks.clear();
        Database.dinamicos.getTrunkData().load();
        Database.estaticos.getTrunkData().load();
    }

    public void reloadMaps() {
        Database.estaticos.getMapData().reload();
    }

    public void reloadMountParks(int i) {
        Database.dinamicos.getMountParkData().reload(i);
        Database.estaticos.getMountParkData().reload(i);
    }

    public void reloadMonsters() {
        Database.estaticos.getMonsterData().reload();
    }

    public void reloadQuests() {
        Database.estaticos.getQuestData().load();
    }

    public void reloadObjectsActions() {
        Database.estaticos.getObjectActionData().reload();
    }

    public void reloadSpells() {
        Database.estaticos.getSpellData().load();
    }

    public void reloadItems() {
        Database.estaticos.getObjectTemplateData().load();
    }

    public void addSeller(Jugador player) {
        if (player.getStoreItems().isEmpty())
            return;

        short map = player.getCurMap().getId();

        if (Seller.get(map) == null) {
            ArrayList<Integer> players = new ArrayList<>();
            players.add(player.getId());
            Seller.put(map, players);
        } else {
            ArrayList<Integer> players = new ArrayList<>();
            players.add(player.getId());
            players.addAll(Seller.get(map));
            Seller.remove(map);
            Seller.put(map, players);
        }
    }

    public Collection<Integer> getSeller(short map) {
        return Seller.get(map);
    }

    public void removeSeller(int player, short map) {
        if(getSeller(map) != null)
            Seller.get(map).remove(player);
    }

    public double getPwrPerEffet(int effect) {
        return switch (effect) {
            case Constantes.STATS_ADD_PA, Constantes.STATS_ADD_PA2, Constantes.STATS_MULTIPLY_DOMMAGE -> 100.0;
            case Constantes.STATS_ADD_PM2, Constantes.STATS_ADD_PM -> 90.0;
            case Constantes.STATS_ADD_VIE, Constantes.STATS_ADD_PODS, Constantes.STATS_ADD_VITA -> 0.25;
            case Constantes.STATS_ADD_CC, Constantes.STATS_CREATURE -> 30.0;
            case Constantes.STATS_ADD_PO -> 51.0;
            case Constantes.STATS_ADD_FORC -> 1.0;
            case Constantes.STATS_ADD_AGIL -> 1.0;
            case Constantes.STATS_ADD_DOMA -> 20.0;
            case Constantes.STATS_ADD_EC -> 1.0;
            case Constantes.STATS_ADD_CHAN -> 1.0;
            case Constantes.STATS_ADD_SAGE -> 3.0;
            case Constantes.STATS_ADD_INTE -> 1.0;
            case Constantes.STATS_ADD_PERDOM -> 2.0;
            case Constantes.STATS_ADD_PDOM -> 2.0;
            case Constantes.STATS_ADD_AFLEE -> 1.0;
            case Constantes.STATS_ADD_MFLEE -> 1.0;
            case Constantes.STATS_ADD_INIT -> 0.1;
            case Constantes.STATS_ADD_PROS -> 3.0;
            case Constantes.STATS_ADD_SOIN -> 20.0;
            case Constantes.STATS_ADD_RP_TER -> 6.0;
            case Constantes.STATS_ADD_RP_EAU -> 6.0;
            case Constantes.STATS_ADD_RP_AIR -> 6.0;
            case Constantes.STATS_ADD_RP_FEU -> 6.0;
            case Constantes.STATS_ADD_RP_NEU -> 6.0;
            case Constantes.STATS_TRAPDOM -> 15.0;
            case Constantes.STATS_TRAPPER -> 2.0;
            case Constantes.STATS_ADD_R_FEU -> 2.0;
            case Constantes.STATS_ADD_R_NEU -> 2.0;
            case Constantes.STATS_ADD_R_TER -> 2.0;
            case Constantes.STATS_ADD_R_EAU -> 2.0;
            case Constantes.STATS_ADD_R_AIR -> 2.0;
            case Constantes.STATS_ADD_RP_PVP_TER -> 6.0;
            case Constantes.STATS_ADD_RP_PVP_EAU -> 6.0;
            case Constantes.STATS_ADD_RP_PVP_AIR -> 6.0;
            case Constantes.STATS_ADD_RP_PVP_FEU -> 6.0;
            case Constantes.STATS_ADD_RP_PVP_NEU -> 6.0;
            case Constantes.STATS_ADD_R_PVP_TER -> 2.0;
            case Constantes.STATS_ADD_R_PVP_EAU -> 2.0;
            case Constantes.STATS_ADD_R_PVP_AIR -> 2.0;
            case Constantes.STATS_ADD_R_PVP_FEU -> 2.0;
            case Constantes.STATS_ADD_R_PVP_NEU -> 2.0;
            default -> 0.0;
        };
    }

    public double getOverPerEffet(int effect) {
        double r = switch (effect) {
            case Constantes.STATS_ADD_PA -> 0.0;
            case Constantes.STATS_ADD_PM2, Constantes.STATS_ADD_VIE, Constantes.STATS_ADD_VITA, Constantes.STATS_ADD_PODS -> 404.0;
            case Constantes.STATS_MULTIPLY_DOMMAGE -> 0.0;
            case Constantes.STATS_ADD_CC, Constantes.STATS_CREATURE -> 3.0;
            case Constantes.STATS_ADD_PO -> 0.0;
            case Constantes.STATS_ADD_FORC, Constantes.STATS_ADD_AGIL, Constantes.STATS_ADD_CHAN, Constantes.STATS_ADD_INTE -> 101.0;
            case Constantes.STATS_ADD_PA2 -> 0.0;
            case Constantes.STATS_ADD_DOMA, Constantes.STATS_ADD_SOIN -> 5.0;
            case Constantes.STATS_ADD_EC -> 0.0;
            case Constantes.STATS_ADD_SAGE, Constantes.STATS_ADD_PROS -> 33.0;
            case Constantes.STATS_ADD_PM -> 0.0;
            case Constantes.STATS_ADD_PERDOM, Constantes.STATS_ADD_PDOM, Constantes.STATS_ADD_R_PVP_NEU, Constantes.STATS_ADD_R_PVP_FEU, Constantes.STATS_ADD_R_PVP_AIR, Constantes.STATS_ADD_R_PVP_EAU, Constantes.STATS_ADD_R_PVP_TER, Constantes.STATS_ADD_R_AIR, Constantes.STATS_ADD_R_EAU, Constantes.STATS_ADD_R_TER, Constantes.STATS_ADD_R_NEU, Constantes.STATS_ADD_R_FEU, Constantes.STATS_TRAPPER -> 50.0;
            case Constantes.STATS_ADD_AFLEE -> 0.0;
            case Constantes.STATS_ADD_MFLEE -> 0.0;
            case Constantes.STATS_ADD_INIT -> 1010.0;
            case Constantes.STATS_ADD_RP_TER, Constantes.STATS_ADD_RP_PVP_NEU, Constantes.STATS_ADD_RP_PVP_FEU, Constantes.STATS_ADD_RP_PVP_AIR, Constantes.STATS_ADD_RP_PVP_EAU, Constantes.STATS_ADD_RP_PVP_TER, Constantes.STATS_ADD_RP_NEU, Constantes.STATS_ADD_RP_FEU, Constantes.STATS_ADD_RP_AIR, Constantes.STATS_ADD_RP_EAU -> 16.0;
            case Constantes.STATS_TRAPDOM -> 6.0;
            default -> 0.0;
        };
        return r;
    }

    public double getTauxObtentionIntermediaire(double bonus, boolean b1, boolean b2) {
        double taux = bonus;
        // 100.0 + 2*(30.0 + 2*10.0) => true true
        // 30.0 + 2*(10.0 + 2*3.0) => true false
        // 10.0 + 2*(3.0 + 2*1.0) => true true
        if (b1) {
            if (bonus == 100.0)
                taux += 2.0 * getTauxObtentionIntermediaire(30.0, true, b2);
            if (bonus == 30.0)
                taux += 2.0 * getTauxObtentionIntermediaire(10.0, (!b2), b2); // Si b2 est false alors on calculera 2*3.0 dans 10.0
            if (bonus == 10.0)
                taux += 2.0 * getTauxObtentionIntermediaire(3.0, (b2), b2); // Si b2 est true alors on calculera apr�s
            else if (bonus == 3.0)
                taux += 2.0 * getTauxObtentionIntermediaire(1.0, false, b2);
        }

        return taux;
    }

    public int getMetierByMaging(int idMaging) {
        int mId = switch (idMaging) {
// FM Dagues
            case 43 -> 17;
// FM Ep�es
            case 44 -> 11;
// FM Marteaux
            case 45 -> 14;
// FM Pelles
            case 46 -> 20;
// FM Haches
            case 47 -> 31;
// FM Arcs
            case 48 -> 13;
// FM Baguettes
            case 49 -> 19;
// FM B�tons
            case 50 -> 18;
// Cordo
            case 62 -> 15;
// Jaillo
            case 63 -> 16;
// Costu
            case 64 -> 27;
            default -> -1;
        };
        return mId;
    }

    public int getTempleByClasse(int classe) {
        return switch (classe) {
            //Feca
            case Constantes.CLASE_FECA -> 1554;
            //Osamodas
            case Constantes.CLASE_OSAMODAS -> 1546;
            //Anutrof
            case Constantes.CLASE_ANUTROF -> 1470;
            //Sram
            case Constantes.CLASE_SRAM -> 6926;
            //Xelor
            case Constantes.CLASE_XELOR -> 1469;
            //Zurkarak
            case Constantes.CLASE_ZURCARAK -> 1544;
            //Eniripsa
            case Constantes.CLASE_ANIRIPSA -> 6928;
            //Yopuka
            case Constantes.CLASE_YOPUKA -> 1549;
            //Ocra
            case Constantes.CLASE_OCRA -> 1558;
            //Sadida
            case Constantes.CLASE_SADIDA -> 1466;
            //Sacrogrito
            case Constantes.CLASE_SACROGRITO -> 6949;
            //Pandawa
            case Constantes.CLASE_PANDAWA -> 8490;
            default -> -1;
        };
    }

    public void sendMessageToAll(String message) {
        Temporizador.addSiguiente(() -> Mundo.mundo.getOnlinePlayers().stream()
                        .filter(player -> player != null && player.getGameClient() != null && player.isOnline())
                        .forEach(player -> player.sendMessage(message)),
                0, TimeUnit.SECONDS, Temporizador.DataType.CLIENTE);
    }

    public static class Drop {
        private final int objectId;
        private final int ceil;
        private final int action;
        private final int level;
        private final String condition;
        private ArrayList<Double> percents;
        private double localPercent;

        public Drop(int objectId, ArrayList<Double> percents, int ceil, int action, int level, String condition) {
            this.objectId = objectId;
            this.percents = percents;
            this.ceil = ceil;
            this.action = action;
            this.level = level;
            this.condition = condition;
        }

        public Drop(int objectId, double percent, int ceil) {
            this.objectId = objectId;
            this.localPercent = percent;
            this.ceil = ceil;
            this.action = -1;
            this.level = -1;
            this.condition = "";
        }

        public int getObjectId() {
            return objectId;
        }

        public int getCeil() {
            return ceil;
        }

        public int getAction() {
            return action;
        }

        public int getLevel() {
            return level;
        }

        public String getCondition() {
            return condition;
        }

        public double getLocalPercent() {
            return localPercent;
        }

        public Drop copy(int grade) {
            Drop drop = new Drop(this.objectId, null, this.ceil, this.action, this.level, this.condition);
            if(this.percents == null) return null;
            if(this.percents.isEmpty()) return null;
            try {
                if (this.percents.get(grade - 1) == null) return null;
                drop.localPercent = this.percents.get(grade - 1);
            } catch(IndexOutOfBoundsException ignored) { return null; }
            return drop;
        }
    }

    public static class ExpLevel {
        public long perso;
        public int metier;
        public int mount;
        public int pvp;
        public long guilde;
        public long tourmenteurs;
        public long bandits;

        public ExpLevel(long c, int m, int d, int p, long t, long b) {
            perso = c;
            metier = m;
            this.mount = d;
            pvp = p;
            guilde = perso * 10;
            tourmenteurs = t;
            bandits = b;
        }
    }
}
