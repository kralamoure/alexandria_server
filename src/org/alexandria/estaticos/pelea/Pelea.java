package org.alexandria.estaticos.pelea;

import org.alexandria.estaticos.area.SubArea;
import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;
import org.alexandria.estaticos.area.mapa.laberintos.Minotot;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.estaticos.cliente.Jugador.Grupo;
import org.alexandria.estaticos.cliente.otros.Stalk;
import org.alexandria.comunes.Camino;
import org.alexandria.comunes.Formulas;
import org.alexandria.estaticos.Mascota.MascotaEntrada;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.configuracion.Constantes;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.dinamicos.FormulaOficial;
import org.alexandria.estaticos.Prisma;
import org.alexandria.estaticos.Recaudador;
import org.alexandria.estaticos.Monstruos;
import org.alexandria.estaticos.Monstruos.Bandidos;
import org.alexandria.estaticos.Montura;
import org.alexandria.estaticos.Gremio;
import org.alexandria.estaticos.juego.JuegoCliente;
import org.alexandria.estaticos.juego.accion.AccionJuego;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.juego.mundo.Mundo.Drop;
import org.alexandria.estaticos.Mision;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.objeto.ObjetoModelo;
import org.alexandria.estaticos.objeto.entrada.PiedraAlma;
import org.alexandria.estaticos.oficio.OficioConstantes;
import org.alexandria.otro.Accion;
import org.alexandria.estaticos.pelea.arena.DeathMatch;
import org.alexandria.estaticos.pelea.arena.FightManager;
import org.alexandria.estaticos.pelea.arena.TeamMatch;
import org.alexandria.estaticos.pelea.hechizo.EfectoHechizo;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.pelea.hechizo.LanzarHechizo;
import org.alexandria.estaticos.pelea.inteligencia.InteligenciaHandler;
import org.alexandria.estaticos.pelea.trampas.Grifos;
import org.alexandria.estaticos.pelea.trampas.Trampas;
import org.alexandria.otro.utilidad.Doble;
import org.alexandria.otro.utilidad.Temporizador;
import org.alexandria.estaticos.Mision.*;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Pelea {

    private int id, state = 0, guildId = -1, type = -1; //type/state -> byte
    private int st1, st2;
    private int curPlayer, captWinner = -1;
    private int curFighterPa, curFighterPm;
    private int curFighterUsedPa, curFighterUsedPm;
    private final Map<Integer, Peleador> team0 = new HashMap<>();
    private final Map<Integer, Peleador> team1 = new HashMap<>();
    private final ArrayList<Doble<Integer, Peleador>> deadList= new ArrayList<>();
    private final Map<Integer, Jugador> viewer = new HashMap<>();
    private ArrayList<GameCase> start0 = new ArrayList<>();
    private ArrayList<GameCase> start1 = new ArrayList<>();
    private final Map<Integer, Retos> allChallenges = new HashMap<>();
    private final Map<Integer, GameCase> rholBack = new HashMap<>();
    private final List<Grifos> allGlyphs = new ArrayList<>();
    private final List<Trampas> allTraps = new ArrayList<>();
    private List<Peleador> orderPlaying = new ArrayList<>();
    private final ArrayList<Peleador> capturer = new ArrayList<>(8);
    private final ArrayList<Peleador> trainer = new ArrayList<>(8);
    private long launchTime = 0, startTime = 0;
    private boolean locked0 = false, locked1 = false;
    private boolean onlyGroup0 = false, onlyGroup1 = false;
    private boolean help0 = false, help1 = false;
    private boolean viewerOk = true;
    private boolean haveKnight = false;
    private boolean isBegin = false;
    private boolean checkTimer = false;
    private boolean finish = false;
    private boolean collectorProtect = false;
    private String curAction = "";
    private Monstruos.MobGroup mobGroup;
    private DeathMatch deathMatch;
    private TeamMatch kolizeum;
    private Recaudador collector;
    private Prisma prism;
    private Mapa map, mapOld;
    private Peleador init0, init1;
    private PiedraAlma fullSoul;
    private Turno turn;
    private String defenders = "";
    private int trainerWinner = -1;
    private int nextId = -100;
    //Espectador entra en PVM
    public int startGuid=-1;
    public ArrayList<Doble<Peleador, ArrayList<EfectoHechizo>>> buffsToAdd=new ArrayList<>();

    public Pelea(int type, int id, Mapa map, Jugador perso, Jugador init2) {
        launchTime = Instant.now().toEpochMilli();
        setType(type); // 0: D�fie (4: Pvm) 1:PVP (5:Perco)
        setId(id);
        setMap(map.getMapCopy());
        setMapOld(map);
        setInit0(new Peleador(this, perso));
        setInit1(new Peleador(this, init2));
        getTeam0().put(perso.getId(), getInit0());
        getTeam1().put(init2.getId(), getInit1());

        GestorSalida.GAME_SEND_GDF_PACKET_TO_FIGHT(perso, this.getMap().getCases());
        // on disable le timer de regen cot� client
        if (getType() != Constantes.FIGHT_TYPE_CHALLENGE)
            scheduleTimer(45);
        int cancelBtn = getType() == Constantes.FIGHT_TYPE_CHALLENGE ? 1 : 0;
        long time = getType() == Constantes.FIGHT_TYPE_CHALLENGE ? 0 : Constantes.TIEMPO_INICIO_PELEA;
        GestorSalida.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this, 7, 2, cancelBtn, 1, 0, time, getType());
        if (init2.get_align() == 0 && (map.getSubArea() != null && map.getSubArea().getAlignement() > 0))
            setHaveKnight();

        int morph = perso.getGfxId();
        if (morph == 1109 || morph == 1046 || morph == 9001) {
            perso.unsetFullMorph();
            GestorSalida.GAME_SEND_ALTER_GM_PACKET(perso.getCurMap(), perso);
        }

        this.start0 = Mundo.mundo.getCryptManager().parseStartCell(getMap(), 0);
        this.start1 = Mundo.mundo.getCryptManager().parseStartCell(getMap(), 1);
        GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this, 1, getMap().getPlaces(), 0);
        GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this, 2, getMap().getPlaces(), 1);
        setSt1(0);
        setSt2(1);

        getInit0().setCell(getRandomCell(this.start0));
        getInit1().setCell(getRandomCell(this.start1));

        getInit0().getPlayer().getCurCell().removePlayer(getInit0().getPlayer());
        getInit1().getPlayer().getCurCell().removePlayer(getInit1().getPlayer());

        getInit0().getCell().addFighter(getInit0());
        getInit1().getCell().addFighter(getInit1());
        getInit0().getPlayer().setPelea(this);
        getInit0().setTeam(0);
        getInit1().getPlayer().setPelea(this);
        getInit1().setTeam(1);
        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPlayer().getCurMap(), getInit0().getId());
        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit1().getPlayer().getCurMap(), getInit1().getId());
        if (getType() == 1) {
            GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), 0, getInit0().getId(), getInit1().getId(), getInit0().getPlayer().getCurCell().getId(), "0;"
                    + getInit0().getPlayer().get_align(), getInit1().getPlayer().getCurCell().getId(), "0;"
                    + getInit1().getPlayer().get_align());
        } else {
            GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), 0, getInit0().getId(), getInit1().getId(), getInit0().getPlayer().getCurCell().getId(), "0;-1", getInit1().getPlayer().getCurCell().getId(), "0;-1");
        }
        GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), getInit0().getId(), getInit0());
        GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), getInit1().getId(), getInit1());

        GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this, 7, getMap());

        setState(Constantes.FIGHT_STATE_PLACE);
    }

    public Pelea(final TeamMatch match, final int id, final Mapa map) {
        this.kolizeum = match;
        this.launchTime = Instant.now().toEpochMilli();
        this.setType(Constantes.FIGHT_TYPE_CHALLENGE);
        this.setId(id);
        this.setMap(map.getMapCopy());
        this.setMapOld(map);

        for (Jugador player : match.getTeam(true)) {
            player.setOldPosition();
            Peleador fighter = new Peleador(this, player);
            this.getTeam0().put(player.getId(), fighter);
            GestorSalida.GAME_SEND_GDF_PACKET_TO_FIGHT(player, this.getMap().getCases());

            if (this.getInit0() == null) {
                this.setInit0(fighter);
            }
        }

        for (Jugador player : match.getTeam(false)) {
            player.setOldPosition();
            this.getTeam1().put(player.getId(), new Peleador(this, player));
            GestorSalida.GAME_SEND_GDF_PACKET_TO_FIGHT(player, this.getMap().getCases());
        }

        this.scheduleTimer(45);
        GestorSalida.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this, 7, 2, 0, 1, 0, 45000, this.getType());
        this.start0 = Mundo.mundo.getCryptManager().parseStartCell(this.getMap(), 0);
        this.start1 = Mundo.mundo.getCryptManager().parseStartCell(this.getMap(), 1);
        GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this, 1, this.getMap().getPlaces(), 0);
        GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this, 2, this.getMap().getPlaces(), 1);
        this.setSt1(0);
        this.setSt2(1);

        for (Jugador player : match.getAllPlayers()) {
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, String.valueOf(player.getId()), player.getId() + "," + 8 + ",0");
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, String.valueOf(player.getId()), player.getId() + "," + 3 + ",0");
        }

        for (Peleador fighter : getTeam0().values()) {
            fighter.setCell(this.getRandomCell(this.start0));
            fighter.getPlayer().getCurCell().removePlayer(fighter.getPlayer());
            fighter.getCell().addFighter(fighter);
            fighter.getPlayer().setPelea(this);
            fighter.setTeam(0);
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(fighter.getPlayer().getCurMap(), fighter.getId());
        }

        for (Peleador fighter : getTeam1().values()) {
            fighter.setCell(this.getRandomCell(this.start1));
            fighter.getPlayer().getCurCell().removePlayer(fighter.getPlayer());
            fighter.getCell().addFighter(fighter);
            fighter.getPlayer().setPelea(this);
            fighter.setTeam(1);
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(fighter.getPlayer().getCurMap(), fighter.getId());
        }

        for (Peleador fighter : getTeam0().values()) {
            GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(fighter.getPlayer().getCurMap(), fighter.getId(),
                    fighter);
        }

        for (Peleador fighter : getTeam1().values()) {
            GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(fighter.getPlayer().getCurMap(), fighter.getId(),
                    fighter);
        }

        GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this, 7, this.getMap());
        this.setState(2);
    }

    public Pelea(final DeathMatch match, final int id, final Mapa map, final Jugador perso,
                 final Jugador init2) {
        this.deathMatch = match;
        this.launchTime = Instant.now().toEpochMilli();
        this.setType(Constantes.FIGHT_TYPE_CHALLENGE);
        this.setId(id);
        this.setMap(map.getMapCopy());
        this.setMapOld(map);
        this.setInit0(new Peleador(this, perso));
        this.setInit1(new Peleador(this, init2));
        this.getTeam0().put(perso.getId(), this.getInit0());
        this.getTeam1().put(init2.getId(), this.getInit1());
        GestorSalida.GAME_SEND_GDF_PACKET_TO_FIGHT(perso, this.getMap().getCases());
        this.scheduleTimer(45);
        GestorSalida.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this, 7, 2, 0, 1, 0, 45000, this.getType());

        this.start0 = Mundo.mundo.getCryptManager().parseStartCell(this.getMap(), 0);
        this.start1 = Mundo.mundo.getCryptManager().parseStartCell(this.getMap(), 1);
        GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this, 1, this.getMap().getPlaces(), 0);
        GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this, 2, this.getMap().getPlaces(), 1);
        this.setSt1(0);
        this.setSt2(1);
        this.getInit0().setCell(this.getRandomCell(this.start0));
        this.getInit1().setCell(this.getRandomCell(this.start1));
        this.getInit0().getPlayer().getCurCell().removePlayer(this.getInit0().getPlayer());
        this.getInit1().getPlayer().getCurCell().removePlayer(this.getInit1().getPlayer());
        this.getInit0().getCell().addFighter(this.getInit0());
        this.getInit1().getCell().addFighter(this.getInit1());
        this.getInit0().getPlayer().setPelea(this);
        this.getInit0().setTeam(0);
        this.getInit1().getPlayer().setPelea(this);
        this.getInit1().setTeam(1);
        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.getInit0().getPlayer().getCurMap(),
                this.getInit0().getId());
        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.getInit1().getPlayer().getCurMap(),
                this.getInit1().getId());
        if (this.getType() == 1) {
            GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(this.getInit0().getPlayer().getCurMap(), 0,
                    this.getInit0().getId(), this.getInit1().getId(),
                    this.getInit0().getPlayer().getCurCell().getId(),
                    "0;" + this.getInit0().getPlayer().get_align(),
                    this.getInit1().getPlayer().getCurCell().getId(),
                    "0;" + this.getInit1().getPlayer().get_align());
        } else {
            GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(this.getInit0().getPlayer().getCurMap(), 0,
                    this.getInit0().getId(), this.getInit1().getId(),
                    this.getInit0().getPlayer().getCurCell().getId(), "0;-1",
                    this.getInit1().getPlayer().getCurCell().getId(), "0;-1");
        }
        GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(this.getInit0().getPlayer().getCurMap(),
                this.getInit0().getId(), this.getInit0());
        GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(this.getInit0().getPlayer().getCurMap(),
                this.getInit1().getId(), this.getInit1());
        GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this, 7, this.getMap());
        this.setState(2);
    }

    public Pelea(int id, Mapa map, Jugador perso, Monstruos.MobGroup group) {
        launchTime = Instant.now().toEpochMilli();
        setCheckTimer(true);
        setMobGroup(group);
        demorph(perso);
        setType(Constantes.FIGHT_TYPE_PVM); // (0: D�fie) 4: Pvm (1:PVP) (5:Perco)
        setId(id);
        setMap(map.getMapCopy());
        setMapOld(map);
        setInit0(new Peleador(this, perso));
        getTeam0().put(perso.getId(), getInit0());
        for (Entry<Integer, Monstruos.MobGrade> entry : group.getMobs().entrySet()) {
            entry.getValue().setInFightID(entry.getKey());
            Peleador mob = new Peleador(this, entry.getValue());
            getTeam1().put(entry.getKey(), mob);
            if (entry.getValue().getTemplate().getId() == 832) // D�minoboule
                Minotot.demi();
            else if (entry.getValue().getTemplate().getId() == 831) // Mominotoror
                Minotot.momi();
        }

        GestorSalida.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this, 1, 2, 0, 1, 0, 45000, getType());
        GestorSalida.GAME_SEND_GDF_PACKET_TO_FIGHT(perso, this.getMap().getCases());
        // on disable le timer de regen cot� client

        scheduleTimer(45);

        this.start0 = Mundo.mundo.getCryptManager().parseStartCell(getMap(), 0);
        this.start1 = Mundo.mundo.getCryptManager().parseStartCell(getMap(), 1);
        GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this, 1, getMap().getPlaces(), 0);
        setSt1(0);
        setSt2(1);
        List<Entry<Integer, Peleador>> e = new ArrayList<>(getTeam1().entrySet());
        for (Entry<Integer, Peleador> entry : e) {
            Peleador f = entry.getValue();
            GameCase cell = getRandomCell(getStart1());
            if (cell == null) {
                getTeam1().remove(f.getId());
                continue;
            }
            f.setCell(cell);
            f.getCell().addFighter(f);
            f.setTeam(1);
            f.fullPdv();
        }
        getInit0().setCell(getRandomCell(getStart0()));

        if(getInit0().getPlayer().getCurCell() != null)
            getInit0().getPlayer().getCurCell().removePlayer(getInit0().getPlayer());

        getInit0().getCell().addFighter(getInit0());

        getInit0().getPlayer().setPelea(this);
        getInit0().setTeam(0);
        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPlayer().getCurMap(), getInit0().getId());
        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPlayer().getCurMap(), group.getId());

        int c = Camino.getNearestCellAround(getInit0().getPlayer().getCurMap(), getInit0().getPlayer().getCurCell().getId(), group.getCellId(), new ArrayList<>());
        if (c < 0)
            c = getInit0().getPlayer().getCurCell().getId();

        GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), 4, getInit0().getId(), group.getId(), c, "0;-1", group.getCellId(), "1;-1");
        GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), getInit0().getId(), getInit0());
        for (Peleador f : getTeam1().values())
            GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), group.getId(), f);
        GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this, 7, getMap());
        setState(Constantes.FIGHT_STATE_PLACE);
    }

    public Pelea(int id, Mapa map, Jugador perso, Monstruos.MobGroup group, int type) {
        launchTime = Instant.now().toEpochMilli();
        setMobGroup(group);
        setType(type); // (0: D�fie) 4: Pvm (1:PVP) (5:Perco)
        setId(id);
        setMap(map.getMapCopy());
        setMapOld(map);
        demorph(perso);
        setInit0(new Peleador(this, perso));
        getTeam0().put(perso.getId(), getInit0());
        for (Entry<Integer, Monstruos.MobGrade> entry : group.getMobs().entrySet()) {
            entry.getValue().setInFightID(entry.getKey());
            Peleador mob = new Peleador(this, entry.getValue());
            getTeam1().put(entry.getKey(), mob);
            if (entry.getValue().getTemplate().getId() == 832) // D�minoboule
                Minotot.demi();
            else if (entry.getValue().getTemplate().getId() == 831) // Mominotoror
                Minotot.momi();
        }

        if (perso.getCurPdv() >= perso.getMaxPdv()) {
            int pdvMax = perso.getMaxPdv();
            perso.setPdv(pdvMax);
        }

        GestorSalida.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this, 1, 2, 0, 1, 0, 45000, getType());
        GestorSalida.GAME_SEND_GDF_PACKET_TO_FIGHT(perso, this.getMap().getCases());
        // on disable le timer de regen cot� client

        scheduleTimer(45);

        this.start0 = Mundo.mundo.getCryptManager().parseStartCell(getMap(), 0);
        this.start1 = Mundo.mundo.getCryptManager().parseStartCell(getMap(), 1);
        GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this, 1, getMap().getPlaces(), 0);
        setSt1(0);
        setSt2(1);

        List<Entry<Integer, Peleador>> e = new ArrayList<>(getTeam1().entrySet());
        for (Entry<Integer, Peleador> entry : e) {
            Peleador f = entry.getValue();
            GameCase cell = getRandomCell(getStart1());
            if (cell == null) {
                getTeam1().remove(f.getId());
                continue;
            }

            f.setCell(cell);
            f.getCell().addFighter(f);
            f.setTeam(1);
            f.fullPdv();
        }
        getInit0().setCell(getRandomCell(getStart0()));

        getInit0().getPlayer().getCurCell().removePlayer(getInit0().getPlayer());

        getInit0().getCell().addFighter(getInit0());

        getInit0().getPlayer().setPelea(this);
        getInit0().setTeam(0);
        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPlayer().getCurMap(), getInit0().getId());
        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPlayer().getCurMap(), group.getId());
        GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), getInit0().getId(), getInit0());
        for (Peleador f : getTeam1().values())
            GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), group.getId(), f);
        GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this, 7, getMap());
        setState(Constantes.FIGHT_STATE_PLACE);
    }

    public Pelea(int id, Mapa map, Jugador perso, Recaudador perco) {
        if (perso.getPelea() != null)
            return;
        launchTime = Instant.now().toEpochMilli();
        setGuildId(perco.getGuildId());
        perco.setInFight((byte) 1);
        perco.set_inFightID((byte) id);

        demorph(perso);

        setType(Constantes.FIGHT_TYPE_PVT); // (0: D�fie) (4: Pvm) (1:PVP) 5:Perco
        setId(id);
        setMap(map.getMapCopy());
        setMapOld(map);
        setInit0(new Peleador(this, perso));
        setCollector(perco);
        // on disable le timer de regen cot� client

        getTeam0().put(perso.getId(), getInit0());

        Peleador percoF = new Peleador(this, perco);
        getTeam1().put(-1, percoF);

        GestorSalida.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this, 1, 2, 0, 1, 0, 45000, getType()); // timer de combat
        GestorSalida.GAME_SEND_GDF_PACKET_TO_FIGHT(perso, this.getMap().getCases());
        scheduleTimer(45);

        if (Formulas.random.nextBoolean()) {
            this.start0 = Mundo.mundo.getCryptManager().parseStartCell(getMap(), 0);
            this.start1 = Mundo.mundo.getCryptManager().parseStartCell(getMap(), 1);
            GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this, 1, getMap().getPlaces(), 0);
            setSt1(0);
            setSt2(1);
        } else {
            this.start0 = Mundo.mundo.getCryptManager().parseStartCell(getMap(), 1);
            this.start1 = Mundo.mundo.getCryptManager().parseStartCell(getMap(), 0);
            setSt1(1);
            setSt2(0);
            GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this, 1, getMap().getPlaces(), 1);
        }

        List<Entry<Integer, Peleador>> e = new ArrayList<>(getTeam1().entrySet());
        for (Entry<Integer, Peleador> entry : e) {
            Peleador f = entry.getValue();
            GameCase cell = getRandomCell(this.start1);
            if (cell == null) {
                getTeam1().remove(f.getId());
                continue;
            }

           f.setCell(cell);
            f.getCell().addFighter(f);
            f.setTeam(1);
            f.fullPdv();

        }
        getInit0().setCell(getRandomCell(this.start0));

        getInit0().getPlayer().getCurCell().removePlayer(getInit0().getPlayer());

        getInit0().getCell().addFighter(getInit0());

        getInit0().getPlayer().setPelea(this);
        getInit0().setTeam(0);

        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPlayer().getCurMap(), getInit0().getId());
        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPlayer().getCurMap(), perco.getId());

        int c = Camino.getNearestCellAround(getInit0().getPlayer().getCurMap(), getInit0().getPlayer().getCurCell().getId(), perco.getCell(), new ArrayList<>());
        if (c < 0)
            c = getInit0().getPlayer().getCurCell().getId();

        GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), 5, getInit0().getId(), perco.getId(), c, "0;-1", perco.getCell(), "3;-1");
        GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), getInit0().getId(), getInit0());

        for (Peleador f : getTeam1().values())
            GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), perco.getId(), f);

        GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this, 7, getMap());
        setState(Constantes.FIGHT_STATE_PLACE);

        String str = "";
        if (this.getCollector() != null)
            str = "A" + this.getCollector().getFullName() + "|.|" + Mundo.mundo.getMap(getCollector().getMap()).getX() + "|" + Mundo.mundo.getMap(getCollector().getMap()).getY();

        for (Jugador z : Mundo.mundo.getGuild(getGuildId()).getPlayers()) {
            if (z == null)
                continue;
            if (z.isOnline()) {
                GestorSalida.GAME_SEND_gITM_PACKET(z, Recaudador.parseToGuild(z.getGuild().getId()));
                Recaudador.parseAttaque(z, getGuildId());
                Recaudador.parseDefense(z, getGuildId());
                GestorSalida.SEND_gA_PERCEPTEUR(z, str);
            }
        }
    }


    public Pelea(int id, Mapa Map, Jugador perso, Prisma Prisme) {
        launchTime = Instant.now().toEpochMilli();
        Prisme.setInFight((byte) 0);
        Prisme.setFight(this);
        Prisme.setFightId(id);
        demorph(perso);
        setType(Constantes.FIGHT_TYPE_CONQUETE); // (0: Desafio) (4: Pvm) (1:PVP)
        // 5:Perco
        setId(id);
        setMap(Map.getMapCopy());
        setMapOld(Map);
        setInit0(new Peleador(this, perso));
        setPrism(Prisme);

        getTeam0().put(perso.getId(), getInit0());
        Peleador lPrisme = new Peleador(this, Prisme);
        setInit1(lPrisme);
        getTeam1().put(-1, lPrisme);
        GestorSalida.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this, 1, 2, 0, 1, 0, 60000, getType());
        GestorSalida.GAME_SEND_GDF_PACKET_TO_FIGHT(perso, this.getMap().getCases());
        scheduleTimer(60);

        if (Formulas.random.nextBoolean()) {
            this.start0 = Mundo.mundo.getCryptManager().parseStartCell(getMap(), 0);
            this.start1 = Mundo.mundo.getCryptManager().parseStartCell(getMap(), 1);
            GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this, 1, getMap().getPlaces(), 0);
            setSt1(0);
            setSt2(1);
        } else {
            this.start0 = Mundo.mundo.getCryptManager().parseStartCell(getMap(), 1);
            this.start1 = Mundo.mundo.getCryptManager().parseStartCell(getMap(), 0);
            setSt1(1);
            setSt2(0);
            GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this, 1, getMap().getPlaces(), 1);
        }

        List<Entry<Integer, Peleador>> e = new ArrayList<>(getTeam1().entrySet());
        for (Entry<Integer, Peleador> entry : e) {
            Peleador f = entry.getValue();
            GameCase cell = getRandomCell(getStart1());
            if (cell == null) {
                getTeam1().remove(f.getId());
                continue;
            }

            f.setCell(cell);
            f.getCell().addFighter(f);
            f.setTeam(1);
            f.fullPdv();
        }
        getInit0().setCell(getRandomCell(getStart0()));
        getInit0().getPlayer().getCurCell().removePlayer(getInit0().getPlayer());
        getInit0().getCell().addFighter(getInit0());
        getInit0().getPlayer().setPelea(this);
        getInit0().setTeam(0);
        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPlayer().getCurMap(), getInit0().getId());
        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPlayer().getCurMap(), Prisme.getId());

        int c = Camino.getNearestCellAround(getInit0().getPlayer().getCurMap(), getInit0().getPlayer().getCurCell().getId(), Prisme.getCell(), new ArrayList<>());
        if (c < 0)
            c = getInit0().getPlayer().getCurCell().getId();

        GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), 0, getInit0().getId(), Prisme.getId(), c, "0;"
                + getInit0().getPlayer().get_align(), Prisme.getCell(), "0;"
                + Prisme.getAlignement());
        GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), getInit0().getId(), getInit0());
        for (Peleador f : getTeam1().values())
            GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), Prisme.getId(), f);
        GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this, 7, getMap());
        setState(Constantes.FIGHT_STATE_PLACE);
        String str = "";
        if (getPrism() != null)
            str = Prisme.getMap() + "|" + Prisme.getX() + "|" + Prisme.getY();
        for (Jugador z : Mundo.mundo.getOnlinePlayers()) {
            if (z == null)
                continue;
            if (z.get_align() != Prisme.getAlignement())
                continue;
            GestorSalida.SEND_CA_ATTAQUE_MESSAGE_PRISME(z, str);
        }
    }

    public static void FightStateAddFlag(Mapa map, Jugador player) {
        map.getFights().stream().filter(fight -> fight.state == Constantes.FIGHT_STATE_PLACE).forEach(fight -> {
            if (fight.type == Constantes.FIGHT_TYPE_CHALLENGE) {
                GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(player, fight.init0.getPlayer().getCurMap(), 0, fight.init0.getId(), fight.init1.getId(), fight.init0.getPlayer().getCurCell().getId(), "0;-1", fight.init1.getPlayer().getCurCell().getId(), "0;-1");
                GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player, fight.init0.getPlayer().getCurMap(), fight.init0.getId(), fight.init0);
                GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player, fight.init1.getPlayer().getCurMap(), fight.init1.getId(), fight.init1);
            } else if (fight.type == Constantes.FIGHT_TYPE_AGRESSION) {
                GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(player, fight.init0.getPlayer().getCurMap(), 0, fight.init0.getId(), fight.init1.getId(), fight.init0.getPlayer().getCurCell().getId(), "0;" + fight.init0.getPlayer().get_align(), fight.init1.getPlayer().getCurCell().getId(), "0;" + fight.init1.getPlayer().get_align());
                GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player, fight.init0.getPlayer().getCurMap(), fight.init0.getId(), fight.init0);
                GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player, fight.init1.getPlayer().getCurMap(), fight.init1.getId(), fight.init1);
            } else if (fight.type == Constantes.FIGHT_TYPE_PVM) {
                GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(player, fight.init0.getPlayer().getCurMap(), 4, fight.init0.getId(), fight.mobGroup.getId(), (fight.init0.getPlayer().getCurCell().getId() + 1), "0;-1", fight.mobGroup.getCellId(), "1;-1");
                GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player, fight.init0.getPlayer().getCurMap(), fight.init0.getId(), fight.init0);
                for (Entry<Integer, Peleador> F : fight.team1.entrySet())
                    GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player, fight.map, fight.getMobGroup().getId(), F.getValue());
            } else if (fight.type == Constantes.FIGHT_TYPE_PVT) {
                GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(player, fight.init0.getPlayer().getCurMap(), 5, fight.init0.getId(), fight.collector.getId(), (fight.init0.getPlayer().getCurCell().getId() + 1), "0;-1", fight.collector.getCell(), "3;-1");
                GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player, fight.init0.getPlayer().getCurMap(), fight.init0.getId(), fight.init0);
                for (Entry<Integer, Peleador> F : fight.team1.entrySet())
                    GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player, fight.map, fight.getCollector().getId(), F.getValue());
            } else if (fight.type == Constantes.FIGHT_TYPE_CONQUETE) {
                GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(player, fight.init0.getPlayer().getCurMap(), 0, fight.init0.getId(), fight.prism.getId(), fight.init0.getPlayer().getCurCell().getId(), "0;" + fight.init0.getPlayer().get_align(), fight.prism.getCell(), "0;" + fight.prism.getAlignement());
                GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player, fight.init0.getPlayer().getCurMap(), fight.init0.getId(), fight.init0);
                for (Entry<Integer, Peleador> F : fight.team1.entrySet())
                    GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player, fight.map, fight.getPrism().getId(), F.getValue());
            }
        });
    }

    public int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    public int getState() {
        return state;
    }

    void setState(int state) {
        this.state = state;
    }

    private int getGuildId() {
        return guildId;
    }

    private void setGuildId(int guildId) {
        this.guildId = guildId;
    }

    public int getType() {

        return type;
    }

    void setType(int type) {
        this.type = type;
    }

    private int getSt1() {
        return st1;
    }

    private void setSt1(int st1) {
        this.st1 = st1;
    }

    private int getSt2() {
        return st2;
    }

    private void setSt2(int st2) {
        this.st2 = st2;
    }

    public int getCurPlayer() {
        return curPlayer;
    }

    private void setCurPlayer(int curPlayer) {
        this.curPlayer = curPlayer;
    }

    private int getCaptWinner() {
        return captWinner;
    }

    private void setCaptWinner(int captWinner) {
        this.captWinner = captWinner;
    }

    public int getCurFighterPa() {
        return curFighterPa;
    }

    public void setCurFighterPa(int curFighterPa) {
        this.curFighterPa = curFighterPa;
    }

    int getCurFighterPm() {
        return curFighterPm;
    }

    void setCurFighterPm(int curFighterPm) {
        this.curFighterPm = curFighterPm;
    }

    private int getCurFighterUsedPa() {
        return curFighterUsedPa;
    }

    private void setCurFighterUsedPa() {
        this.curFighterUsedPa = 0;
    }

    int getCurFighterUsedPm() {
        return curFighterUsedPm;
    }

    private void setCurFighterUsedPm() {
        this.curFighterUsedPm = 0;
    }

    public Map<Integer, Peleador> getTeam(int team) {
        return switch (team) {
            case 1 -> team0;
            case 2 -> team1;
            default -> team0;
        };
    }

    public Map<Integer, Peleador> getTeam0() {
        return team0;
    }

    public Map<Integer, Peleador> getTeam1() {
        return team1;
    }

    public ArrayList<Doble<Integer, Peleador>> getDeadList() {
        return deadList;
    }

    public void removeDead(Peleador objetivo) {
        deadList.remove(new Doble<>(objetivo.getId(), objetivo));
    }

    Map<Integer, Jugador> getViewer() {
        return viewer;
    }

    ArrayList<GameCase> getStart0() {
        return start0;
    }

    ArrayList<GameCase> getStart1() {
        return start1;
    }

    public Map<Integer, Retos> getAllChallenges() {
        return allChallenges;
    }

    public Map<Integer, GameCase> getRholBack() {
        return rholBack;
    }

    public List<Grifos> getAllGlyphs() {
        return allGlyphs;
    }

    public List<Trampas> getAllTraps() {
        return allTraps;
    }

    ArrayList<Peleador> getCapturer() {
        return capturer;
    }

    ArrayList<Peleador> getTrainer() {
        return trainer;
    }

    long getStartTime() {
        return startTime;
    }

    void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getLaunchTime() {
        return launchTime;
    }

    boolean isLocked0() {
        return locked0;
    }

    void setLocked0(boolean locked0) {
        this.locked0 = locked0;
    }

    boolean isLocked1() {
        return locked1;
    }

    void setLocked1(boolean locked1) {
        this.locked1 = locked1;
    }

    boolean isOnlyGroup0() {
        return onlyGroup0;
    }

    void setOnlyGroup0(boolean onlyGroup0) {
        this.onlyGroup0 = onlyGroup0;
    }

    boolean isOnlyGroup1() {
        return onlyGroup1;
    }

    void setOnlyGroup1(boolean onlyGroup1) {
        this.onlyGroup1 = onlyGroup1;
    }

    boolean isHelp0() {
        return help0;
    }

    void setHelp0(boolean help0) {
        this.help0 = help0;
    }

    boolean isHelp1() {
        return help1;
    }

    void setHelp1(boolean help1) {
        this.help1 = help1;
    }

    boolean isViewerOk() {
        return viewerOk;
    }

    void setViewerOk(boolean viewerOk) {
        this.viewerOk = viewerOk;
    }

    boolean isHaveKnight() {
        return haveKnight;
    }

    void setHaveKnight() {
        this.haveKnight = true;
    }

    public boolean isBegin() {
        return isBegin;
    }

    void setBegin() {
        this.isBegin = true;
    }

    boolean isCheckTimer() {
        return checkTimer;
    }

    private void setCheckTimer(boolean checkTimer) {
        this.checkTimer = checkTimer;
    }

    public String getCurAction() {
        return curAction;
    }

    public void setCurAction(String curAction) {
        this.curAction = curAction;
    }

    Monstruos.MobGroup getMobGroup() {
        return mobGroup;
    }

    void setMobGroup(Monstruos.MobGroup mobGroup) {
        this.mobGroup = mobGroup;
    }

    Recaudador getCollector() {
        return collector;
    }

    void setCollector(Recaudador collector) {
        this.collector = collector;
    }

    public Prisma getPrism() {
        return prism;
    }

    void setPrism(Prisma prism) {
        this.prism = prism;
    }

    public Mapa getMap() {
        return map;
    }

    void setMap(Mapa map) {
        this.map = map;
    }

    public Mapa getMapOld() {
        return mapOld;
    }

    void setMapOld(Mapa mapOld) {
        this.mapOld = mapOld;
    }

    public Peleador getInit0() {
        return init0;
    }

    void setInit0(Peleador init0) {
        this.init0 = init0;
    }

    public Peleador getInit1() {
        return init1;
    }

    void setInit1(Peleador init1) {
        this.init1 = init1;
    }

    PiedraAlma getFullSoul() {
        return fullSoul;
    }

    void setFullSoul(PiedraAlma fullSoul) {
        this.fullSoul = fullSoul;
    }

    String getDefenders() {
        return defenders;
    }

    public void setDefenders(String defenders) {
        this.defenders = defenders;
    }

    int getTrainerWinner() {
        return trainerWinner;
    }

    void setTrainerWinner(int trainerWinner) {
        this.trainerWinner = trainerWinner;
    }

    public boolean isFinish() {
        return finish;
    }

    public int getTeamId(int guid) {
        if (getTeam0().containsKey(guid))
            return 1;
        if (getTeam1().containsKey(guid))
            return 2;
        if (getViewer().containsKey(guid))
            return 4;
        return -1;
    }

    public int getOtherTeamId(int guid) {
        if (getTeam0().containsKey(guid))
            return 2;
        if (getTeam1().containsKey(guid))
            return 1;
        return -1;
    }

    void scheduleTimer(int time) {
        Temporizador.addSiguiente(() -> {
            if(!this.isBegin) {
                if (this.getState() != Constantes.FIGHT_STATE_ACTIVE)
                    this.startFight();
            }
        }, time, TimeUnit.SECONDS, Temporizador.DataType.PELEA);
    }

    private void demorph(Jugador p) {
        if (!p.getMorphMode() && p.isMorph() && (p.getGroupe() == null) && (p.getMorphId() != 8006 && p.getMorphId() != 8007 && p.getMorphId() != 8009))
            p.unsetMorph();
    }

    public void startFight() {
        this.launchTime = -1;
        this.startTime = Instant.now().toEpochMilli();
        if (this.collector != null && !this.collectorProtect) {
            ArrayList<Jugador> protectors = new ArrayList<>(collector.getDefenseFight().values());
            for (Jugador player : protectors) {
                if (player.getPelea() == null && !player.isAway()) {
                    player.setOldPosition();

                    if (player.getCurMap().getId() != this.getMapOld().getId()) {
                        player.teleport(this.getMapOld().getId(), this.collector.getCell());
                    }

                    Temporizador.addSiguiente(() -> this.joinCollectorFight(player, collector.getId()), 1000);
                } else {
                    GestorSalida.GAME_SEND_MESSAGE(player, "Vous n'avez pas pu rejoindre le combat du percepteur suite à votre indisponibilité.");
                    collector.delDefenseFight(player);
                }
                player.send("gITP-" + collector.getId() + "|" + Integer.toString(player.getId(), 36));
            }

            this.collectorProtect = true;
            this.scheduleTimer(15);
            return;
        }

        if (getState() >= Constantes.FIGHT_STATE_ACTIVE)
            return;

        if (this.getType() == Constantes.FIGHT_TYPE_PVM) {
            if (this.getMobGroup().isFix() && isCheckTimer() && this.getMapOld().getId() != 6826 && this.getMapOld().getId() != 10332 && this.getMapOld().getId() != 7388)
                this.getMapOld().spawnAfterTimeGroupFix(this.getMobGroup().getCellId());// Respawn d'un groupe fix
            if(!Configuracion.INSTANCE.getHEROIC())
                if (!this.getMobGroup().isFix() && this.isCheckTimer())
                    this.getMapOld().spawnAfterTimeGroup();// Respawn d'un groupe
        }

        if (getType() == Constantes.FIGHT_TYPE_CONQUETE) {
            getPrism().setInFight(-2);
            for (Jugador z : Mundo.mundo.getOnlinePlayers()) {
                if (z == null)
                    continue;
                if (z.get_align() == getPrism().getAlignement()) {
                    Prisma.parseAttack(z);
                    Prisma.parseDefense(z);
                }
            }
        }

        if (getType() == Constantes.FIGHT_TYPE_PVT && this.getCollector() != null)
            getCollector().setInFight((byte) 2);

        setState(Constantes.FIGHT_STATE_ACTIVE);
        setStartTime(Instant.now().toEpochMilli());
        GestorSalida.GAME_SEND_GAME_REMFLAG_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), getInit0().getId());

        for(Peleador fighter : this.getFighters(3)) {
            Jugador player = fighter.getPlayer();

            if(player != null) {
                player.refreshObjectsClass();
            }
        }

        if (isHaveKnight() && getType() == Constantes.FIGHT_TYPE_AGRESSION)
            addChevalier();

        setCheckTimer(false);
        GestorSalida.GAME_SEND_GIC_PACKETS_TO_FIGHT(this, 7);
        GestorSalida.GAME_SEND_GS_PACKET_TO_FIGHT(this, 7);
        initOrderPlaying();
        setCurPlayer(-1);
        GestorSalida.GAME_SEND_GTL_PACKET_TO_FIGHT(this, 7);
        GestorSalida.GAME_SEND_GTM_PACKET_TO_FIGHT(this, 7);

        if (getType() == Constantes.FIGHT_TYPE_PVM
                || getType() == Constantes.FIGHT_TYPE_DOPEUL) {
            boolean hasMale = false, hasFemale = false, hasDisciple = false;
            boolean hasCawotte = false, hasChafer = false, hasRoulette = false, hasArakne = false, hasArround = false;
            boolean severalEnnemies, severalAllies, bothSexes, EvenEnnemies, MoreEnnemies, ecartLvlPlayer = false;
            int hasBoss = -1;

            if (this.getTeam0().size() > 1) {
                int lowLvl1 = 201, lowLvl2 = 201;

                for (Peleador fighter : getTeam0().values())
                    if (fighter.getLvl() < lowLvl1)
                        lowLvl1 = fighter.getLvl();
                for (Peleador fighter : getTeam0().values())
                    if (fighter.getLvl() < lowLvl2
                            && fighter.getLvl() > lowLvl1)
                        lowLvl2 = fighter.getLvl();
                if (lowLvl2 - lowLvl1 > 10)
                    ecartLvlPlayer = true;
            }

            for (Peleador f : getTeam0().values()) {
                Jugador player = f.getPlayer();
                if (f.getPlayer() != null) {
                    switch (player.getClasse()) {
                        case Constantes.CLASE_OSAMODAS, Constantes.CLASE_FECA, Constantes.CLASE_SADIDA, Constantes.CLASE_XELOR, Constantes.CLASE_SRAM -> hasDisciple = true;
                    }

                    player.setOldPosition();

                    if (player.hasSpell(367))
                        hasCawotte = true;
                    if (player.hasSpell(373))
                        hasChafer = true;
                    if (player.hasSpell(101))
                        hasRoulette = true;
                    if (player.hasSpell(370))
                        hasArakne = true;
                    if (player.getSexe() == 0)
                        hasMale = true;
                    if (player.getSexe() == 1)
                        hasFemale = true;
                }
            }

            String boss = "58 85 86 107 113 121 147 173 180 225 226 230 232 251 252 257 289 295 374 375 377 382 404 423 430 457 478 568 605 612 669 670"
                    + " 673 675 677 681 780 792 797 799 800 827 854 926 939 940 943 1015 1027 1045 1051 1071 1072 1085 1086 1087 1159 1184 1185 1186 1187 1188";

            for (Peleador fighter : getTeam1().values()) {
                if (fighter.getMob() != null) {
                    if (fighter.getMob().getTemplate() != null) {
                        if (boss.contains(String.valueOf(fighter.getMob().getTemplate().getId())))
                            hasBoss = fighter.getMob().getTemplate().getId();
                        for (Peleador fighter2 : getTeam0().values())
                            if (Camino.getDistanceBetween(this.getMap(), fighter2.getCell().getId(), fighter.getCell().getId()) >= 5)
                                hasArround = true;

                    }
                }
            }

            for (Peleador fighter : getTeam1().values()) {
                if (fighter.getMob() != null) {
                    if (fighter.getMob().getTemplate() != null) {
                        hasArround = switch (fighter.getMob().getTemplate().getId()) {
// TouchParak
                            case 98, 111, 120, 382, 473, 794, 796, 800, 801, 803, 805, 806, 807, 808, 841, 847, 868, 970, 171, 200, 666, 582 -> false;
                            default -> hasArround;
                        };
                    }
                }
            }

            severalEnnemies = (getTeam1().size() >= 2);
            severalAllies = (getTeam0().size() >= 2);
            bothSexes = (!(!hasMale || !hasFemale));
            EvenEnnemies = (getTeam1().size() % 2 == 0);
            MoreEnnemies = (getTeam1().size() >= getTeam0().size());

            String challenges = Mundo.mundo.getChallengeFromConditions(severalEnnemies, severalAllies, bothSexes, EvenEnnemies, MoreEnnemies, hasCawotte, hasChafer, hasRoulette, hasArakne, hasBoss, ecartLvlPlayer, hasArround, hasDisciple, (this.getTeam0().size() != 1));
            String[] chalInfo;

            int challengeID, challengeXP, challengeDP, bonusGroupe;
            int challengeNumber = ((this.getMapOld().hasEndFightAction(this.getType()) || PiedraAlma.isInArenaMap(this.getMapOld().getId())) ? 2 : 1);

            for (String chalInfos : Mundo.mundo.getRandomChallenge(challengeNumber, challenges)) {
                chalInfo = chalInfos.split(",");
                challengeID = Integer.parseInt(chalInfo[0]);
                challengeXP = Integer.parseInt(chalInfo[1]);
                challengeDP = Integer.parseInt(chalInfo[2]);
                bonusGroupe = Integer.parseInt(chalInfo[3]);
                bonusGroupe *= getTeam1().size();
                getAllChallenges().put(challengeID, new Retos(this, challengeID, challengeXP + bonusGroupe, challengeDP + bonusGroupe));
            }
            for (Entry<Integer, Retos> c : getAllChallenges().entrySet()) {
                if (c.getValue() == null)
                    continue;
                c.getValue().fightStart();
                GestorSalida.GAME_SEND_CHALLENGE_FIGHT(this, 1, c.getValue().parseToPacket());
            }
        }
        //Retos

        for (Peleador F : getFighters(3)) {
            Jugador player = F.getPlayer();
            if (player != null)
                if (player.isOnMount())
                    GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, player.getId() + "", player.getId() + "," + Constantes.ETAT_CHEVAUCHANT + ",1");
        }

        this.startTurn();
        this.getFighters(3).stream().filter(Objects::nonNull).forEach(F -> getRholBack().put(F.getId(), F.getCell()));
        this.setBegin();
    }

    public void leftFight(Jugador playerCaster, Jugador playerTarget) {
        if (playerCaster == null)
            return;

        final Peleador caster = this.getFighterByPerso(playerCaster), target = (playerTarget != null ? this.getFighterByPerso(playerTarget) : null);

        if (caster != null) {
            switch (getType()) {
                case Constantes.FIGHT_TYPE_CHALLENGE:
                case Constantes.FIGHT_TYPE_AGRESSION:
                case Constantes.FIGHT_TYPE_PVM:
                case Constantes.FIGHT_TYPE_PVT:
                case Constantes.FIGHT_TYPE_CONQUETE:
                case Constantes.FIGHT_TYPE_DOPEUL:
                    if (this.getState() >= Constantes.FIGHT_STATE_ACTIVE) {
                        if(!this.isBegin && target == null) return;
                        this.onFighterDie(caster, caster);
                        caster.setLeft(true);

                        if (this.getFighterByOrdreJeu() != null && this.getFighterByOrdreJeu().getId() == caster.getId())
                            endTurn(false, caster);

                        final Jugador player = caster.getPlayer();
                        player.setDuelId(-1);
                        player.setReady(false);
                        player.setPelea(null);
                        player.setAway(false);
                        this.verifIfTeamAllDead();

                        if(!this.finish) {
                            this.onPlayerLoose(caster);
                            GestorSalida.GAME_SEND_GV_PACKET(caster.getPlayer());
                        }
                    } else if (getState() == Constantes.FIGHT_STATE_PLACE) {
                        boolean isValid = false;
                        if (target != null) {
                            if (getInit0() != null && getInit0().getPlayer() != null && caster.getPlayer().getId() == getInit0().getPlayer().getId())
                                isValid = true;
                            if (getInit1() != null && getInit1().getPlayer() != null && caster.getPlayer().getId() == getInit1().getPlayer().getId())
                                isValid = true;
                        }

                        if (isValid) {// Celui qui fait l'action a lancer le combat et leave un autre personnage
                            if ((target.getTeam() == caster.getTeam()) && (target.getId() != caster.getId())) {
                                GestorSalida.GAME_SEND_ON_FIGHTER_KICK(this, target.getPlayer().getId(), getTeamId(target.getId()));

                                if (getType() == Constantes.FIGHT_TYPE_AGRESSION || getType() == Constantes.FIGHT_TYPE_CHALLENGE || getType() == Constantes.FIGHT_TYPE_PVT || getType() == Constantes.FIGHT_TYPE_CONQUETE || getType() == Constantes.FIGHT_TYPE_DOPEUL)
                                    GestorSalida.GAME_SEND_ON_FIGHTER_KICK(this, target.getPlayer().getId(), getOtherTeamId(target.getId()));

                                final Jugador player = target.getPlayer();
                                player.setDuelId(-1);
                                player.setReady(false);
                                player.setPelea(null);
                                player.setAway(false);

                                if (player.isOnline())
                                    GestorSalida.GAME_SEND_GV_PACKET(player);

                                // On le supprime de la team
                                if (this.getTeam0().containsKey(target.getId())) {
                                    target.getCell().removeFighter(target);
                                    this.getTeam0().remove(target.getId());
                                } else if (this.getTeam1().containsKey(target.getId())) {
                                    target.getCell().removeFighter(target);
                                    this.getTeam1().remove(target.getId());
                                }

                                for (Jugador player1 : this.getMapOld().getPlayers())
                                    FightStateAddFlag(getMapOld(), player1);
                            }
                        } else if (target == null) {// Il leave de son plein gr� donc (target = null)
                            boolean isValid2 = false;
                            if (this.getInit0() != null && this.getInit0().getPlayer() != null && caster.getPlayer().getId() == this.getInit0().getPlayer().getId())
                                isValid2 = true;
                            if (this.getInit1() != null && this.getInit1().getPlayer() != null && caster.getPlayer().getId() == this.getInit1().getPlayer().getId())
                                isValid2 = true;

                            if (isValid2) {// Soit il a lancer le combat => annulation du combat
                                for (Peleador fighter : this.getFighters(caster.getTeam2())) {
                                    final Jugador player = fighter.getPlayer();
                                    player.setDuelId(-1);
                                    player.setReady(false);
                                    player.setPelea(null);
                                    player.setAway(false);
                                    fighter.setLeft(true);

                                    if (caster.getPlayer().getId() != fighter.getPlayer().getId()) {// Celui qui a join le fight revient sur la map
                                        if (player.isOnline())
                                            GestorSalida.GAME_SEND_GV_PACKET(player);
                                    } else {// Celui qui a fait le fight meurt + perte honor
                                        if (this.getType() == Constantes.FIGHT_TYPE_AGRESSION || getType() == Constantes.FIGHT_TYPE_PVM || getType() == Constantes.FIGHT_TYPE_PVT || getType() == Constantes.FIGHT_TYPE_CONQUETE) {
                                            int looseEnergy = Formulas.getLoosEnergy(player.getLevel(), getType() == 1, getType() == 5), totalEnergy = player.getEnergy() - looseEnergy;
                                            if (totalEnergy < 0) totalEnergy = 0;

                                            player.setEnergy(totalEnergy);
                                            player.setMascotte(0);

                                            if (player.isOnline())
                                                GestorSalida.GAME_SEND_Im_PACKET(player, "034;" + looseEnergy);

                                            if (caster.getPlayer().getObjetByPos(Constantes.ITEM_POS_FAMILIER) != null) {
                                                ObjetoJuego obj = caster.getPlayer().getObjetByPos(Constantes.ITEM_POS_FAMILIER);
                                                if (obj != null) {
                                                    MascotaEntrada pets = Mundo.mundo.getPetsEntry(obj.getId());
                                                    if (pets != null)
                                                        pets.looseFight(caster.getPlayer());
                                                }
                                            }

                                            if (this.getType() == Constantes.FIGHT_TYPE_AGRESSION || getType() == Constantes.FIGHT_TYPE_CONQUETE) {
                                                int honor = player.get_honor() - 500;
                                                if (honor < 0) honor = 0;
                                                player.set_honor(honor);
                                                if (player.isOnline())
                                                    GestorSalida.GAME_SEND_Im_PACKET(player, "076;" + honor);
                                            }

                                            final int energy = totalEnergy;

                                            if (energy == 0) {
                                                if (getType() == Constantes.FIGHT_TYPE_AGRESSION) {
                                                    for (Peleador enemy : (this.getTeam1().containsValue(caster) ? this.getTeam0() : this.getTeam1()).values()) {
                                                        if (enemy.getPlayer() != null) {
                                                            if (enemy.getPlayer().get_traque().getTraque() == caster.getPlayer()) {
                                                                player.teleportFaction(enemy.getPlayer().get_align());
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    player.setFuneral();
                                                } else {
                                                    player.setFuneral();
                                                }
                                            } else {
                                                if (getType() == Constantes.FIGHT_TYPE_AGRESSION) {
                                                    for (Peleador enemy : (this.getTeam1().containsValue(caster) ? this.getTeam0() : this.getTeam1()).values()) {
                                                        if (enemy.getPlayer() != null) {
                                                            if (enemy.getPlayer().get_traque().getTraque() == caster.getPlayer()) {
                                                                player.teleportFaction(enemy.getPlayer().get_align());
                                                                break;
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    if (player.isOnline()) {
                                                        String[] split = player.getSavePosition().split(",");
                                                        player.teleport(Short.parseShort(split[0]), Integer.parseInt(split[1]));
                                                    } else {
                                                        player.setNeededEndFightAction(new Accion(1001, player.getSavePosition(), "", null));
                                                    }
                                                }
                                                player.setPdv(1);
                                            }
                                        }

                                        if (player.isOnline())
                                            GestorSalida.GAME_SEND_GV_PACKET(player);
                                    }
                                }

                                if (getType() == Constantes.FIGHT_TYPE_AGRESSION || getType() == Constantes.FIGHT_TYPE_CHALLENGE || getType() == Constantes.FIGHT_TYPE_PVT || getType() == Constantes.FIGHT_TYPE_CONQUETE) {
                                    for (Peleador f : this.getFighters(caster.getOtherTeam())) {
                                        if (f.getPlayer() == null)
                                            continue;
                                        final Jugador player = f.getPlayer();

                                        player.setDuelId(-1);
                                        player.setReady(false);
                                        player.setPelea(null);
                                        player.setAway(false);

                                        if (player.isOnline())
                                            GestorSalida.GAME_SEND_GV_PACKET(player);
                                    }
                                }

                                this.setState(4);// Nous assure de ne pas d�marrer le combat
                                Mundo.mundo.getMap(this.getMap().getId()).removeFight(this.getId());
                                GestorSalida.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(Mundo.mundo.getMap(this.getMap().getId()));
                                GestorSalida.GAME_SEND_GAME_REMFLAG_PACKET_TO_MAP(this.getMapOld(), this.getInit0().getId());

                                if (getType() == Constantes.FIGHT_TYPE_PVT) {
                                    // FIXME
                                    Mundo.mundo.getGuild(getGuildId()).getPlayers().stream().filter(player -> player != null && player.isOnline()).forEach(player -> {
                                        GestorSalida.GAME_SEND_gITM_PACKET(player, Recaudador.parseToGuild(player.getGuild().getId()));
                                        GestorSalida.GAME_SEND_MESSAGE(player, "Votre percepteur remporte la victioire.");
                                    });

                                    this.getCollector().setInFight((byte) 0);
                                    this.getCollector().set_inFightID((byte) -1);

                                    Mundo.mundo.getMap(this.getCollector().getMap()).getPlayers().stream().filter(Objects::nonNull)
                                            .forEach(player -> GestorSalida.GAME_SEND_MAP_PERCO_GMS_PACKETS(player.getGameClient(), player.getCurMap()));
                                }
                                setMap(null);
                                this.orderPlaying = null;
                            } else {// Soit il a rejoin le combat => Left de lui seul
                                GestorSalida.GAME_SEND_ON_FIGHTER_KICK(this, caster.getPlayer().getId(), getTeamId(caster.getId()));

                                if (getType() == Constantes.FIGHT_TYPE_AGRESSION || getType() == Constantes.FIGHT_TYPE_CHALLENGE || getType() == Constantes.FIGHT_TYPE_PVT || getType() == Constantes.FIGHT_TYPE_CONQUETE)
                                    GestorSalida.GAME_SEND_ON_FIGHTER_KICK(this, caster.getPlayer().getId(), getOtherTeamId(caster.getId()));

                                final Jugador player = caster.getPlayer();
                                player.setDuelId(-1);
                                player.setReady(false);
                                player.setPelea(null);
                                player.setAway(false);
                                caster.setLeft(true);
                                caster.hasLeft();

                                if (getType() == Constantes.FIGHT_TYPE_AGRESSION || getType() == Constantes.FIGHT_TYPE_PVM || getType() == Constantes.FIGHT_TYPE_PVT || getType() == Constantes.FIGHT_TYPE_CONQUETE || getType() == Constantes.FIGHT_TYPE_DOPEUL) {
                                    int loosEnergy = Formulas.getLoosEnergy(player.getLevel(), getType() == 1, getType() == 5), totalEnergy = player.getEnergy() - loosEnergy;
                                    if (totalEnergy < 0) totalEnergy = 0;

                                    player.setEnergy(totalEnergy);
                                    player.setMascotte(0);

                                    if (player.isOnline())
                                        GestorSalida.GAME_SEND_Im_PACKET(player, "034;" + loosEnergy);
                                    if (caster.getPlayer().getObjetByPos(Constantes.ITEM_POS_FAMILIER) != null) {
                                        ObjetoJuego obj = caster.getPlayer().getObjetByPos(Constantes.ITEM_POS_FAMILIER);
                                        if (obj != null) {
                                            MascotaEntrada pets = Mundo.mundo.getPetsEntry(obj.getId());
                                            if (pets != null)
                                                pets.looseFight(caster.getPlayer());
                                        }
                                    }

                                    if (getType() == Constantes.FIGHT_TYPE_AGRESSION || getType() == Constantes.FIGHT_TYPE_CONQUETE) {
                                        int honor = player.get_honor() - 500;
                                        if (honor < 0)
                                            honor = 0;
                                        player.set_honor(honor);
                                        if (player.isOnline())
                                            GestorSalida.GAME_SEND_Im_PACKET(player, "076;" + honor);
                                    }

                                    final int energy = totalEnergy;

                                    if (energy == 0) {
                                        if (getType() == Constantes.FIGHT_TYPE_AGRESSION) {
                                            for (Peleador enemy : (this.getTeam1().containsValue(caster) ? this.getTeam0() : this.getTeam1()).values()) {
                                                if (enemy.getPlayer() != null) {
                                                    if (enemy.getPlayer().get_traque().getTraque() == caster.getPlayer()) {
                                                        player.teleportFaction(enemy.getPlayer().get_align());
                                                        break;
                                                    }
                                                }
                                            }
                                            player.setFuneral();
                                        } else {
                                            player.setFuneral();
                                        }
                                    } else {
                                        if (getType() == Constantes.FIGHT_TYPE_AGRESSION) {
                                            for (Peleador enemy : (this.getTeam1().containsValue(caster) ? this.getTeam0() : this.getTeam1()).values()) {
                                                if (enemy.getPlayer() != null) {
                                                    if (enemy.getPlayer().get_traque().getTraque() == caster.getPlayer()) {
                                                        player.teleportFaction(enemy.getPlayer().get_align());
                                                        break;
                                                    }
                                                }
                                            }
                                        } else {
                                            if (getType() != Constantes.FIGHT_TYPE_PVT)
                                                player.setNeededEndFightAction(new Accion(1001, player.getSavePosition(), "", null));
                                            else if (!player.getCurMap().hasEndFightAction(0))
                                                player.setNeededEndFightAction(new Accion(1001, player.getSavePosition(), "", null));
                                        }
                                        player.setPdv(1);
                                    }
                                }

                                if (player.isOnline())
                                    GestorSalida.GAME_SEND_GV_PACKET(player);

                                // On le supprime de la team
                                if (this.getTeam0().containsKey(caster.getId())) {
                                    caster.getCell().removeFighter(caster);
                                    this.getTeam0().remove(caster.getId());
                                } else if (getTeam1().containsKey(caster.getId())) {
                                    caster.getCell().removeFighter(caster);
                                    this.getTeam1().remove(caster.getId());
                                }
                                for (Jugador player1 : this.getMapOld().getPlayers())
                                    FightStateAddFlag(getMapOld(), player1);
                            }
                        }
                    }
                    break;
            }
            if (target == null) {
                if (caster.getPlayer().getMorphMode())
                    if (caster.getPlayer().donjon)
                        caster.getPlayer().unsetFullMorph();

                if (this.getTeam0().containsKey(caster.getId())) {
                    caster.getCell().removeFighter(caster);
                    this.getTeam0().remove(caster.getId());
                } else if (getTeam1().containsKey(caster.getId())) {
                    caster.getCell().removeFighter(caster);
                    this.getTeam1().remove(caster.getId());
                }
            }
            if (target != null) {
                if (this.getTeam0().containsKey(target.getId())) {
                    target.getCell().removeFighter(target);
                    this.getTeam0().remove(target.getId());
                } else if (getTeam1().containsKey(target.getId())) {
                    target.getCell().removeFighter(target);
                    this.getTeam1().remove(target.getId());
                }
            }
        } else {
            GestorSalida.GAME_SEND_GV_PACKET(playerCaster);
            this.getViewer().remove(playerCaster.getId());
            playerCaster.setPelea(null);
            playerCaster.setAway(false);
        }
    }

    public void endFight(boolean b) {
        if (this.launchTime > 1)
            return;
        if (b) {
            for (Peleador caster : getTeam1().values()) {
                try {
                    if (caster == null)
                        continue;
                    caster.setIsDead(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            verifIfTeamAllDead();
        } else {
            for (Peleador caster : getTeam0().values()) {
                try {
                    if (caster == null)
                        continue;
                    caster.setIsDead(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            verifIfTeamAllDead();
        }
    }

    void startTurn() {
        if (verifyStillInFight())
            verifIfTeamAllDead();

        if (getState() >= Constantes.ESTADO_FIN_DE_PELEA)
            return;

        setCurPlayer(getCurPlayer() + 1);
        setCurAction("");

        if (getCurPlayer() >= this.getOrderPlayingSize())
            setCurPlayer(0);

        Peleador current = this.getFighterByOrdreJeu();

        setCurFighterPa(current.getPa());
        setCurFighterPm(current.getPm());
        setCurFighterUsedPa();
        setCurFighterUsedPm();

        if (current.hasLeft() || current.isDead()) {
            this.endTurn(false, current);
            return;
        }

        current.applyBeginningTurnBuff(this);

        if(current.isDead() && current.isInvocation()) {
            endTurn(false, this.getFighterByOrdreJeu());
            return;
        }

        if (getState() == Constantes.ESTADO_FIN_DE_PELEA)
            return;

        if (current.getPdv() <= 0) {
            onFighterDie(current, getInit0());
            endTurn(false, current);
            return;
        }
        // On actualise les sorts launch
        current.refreshLaunchedSort();
        // reset des Max des Chatis
        current.getChatiValue().clear();

        if (current.isDead() && !current.isInvocation()) {
            endTurn(false, current);
            return;
        }
        if (current.getPlayer() != null)
            GestorSalida.GAME_SEND_STATS_PACKET(current.getPlayer());

        if (current.hasBuff(Constantes.EFFECT_PASS_TURN) || current.getComandoPasarTurno()) {
            endTurn(false, current);
            return;
        }

        GestorSalida.GAME_SEND_GAMETURNSTART_PACKET_TO_FIGHT(this, 7, current.getId(), Constantes.TIEMPO_DE_TURNO);
        current.setCanPlay(true);
        this.turn = new Turno(this, current);

        // Gestion des glyphes
        ArrayList<Grifos> glyphs = new ArrayList<>(this.getAllGlyphs());// Copie du tableau

        for (Grifos glyph : glyphs) {
            if (glyph.getLanzador().getId() == current.getId()) {
                if (glyph.decrementDuration() == 0) {
                    getAllGlyphs().remove(glyph);
                    glyph.desaparecer();
                    continue;
                }
            }

            if (Camino.getDistanceBetween(getMap(), current.getCell().getId(), glyph.getCelda().getId()) <= glyph.getSize() && glyph.getHechizo() != 476)
                glyph.onGrifo(current);
        }


        if ((getType() == Constantes.FIGHT_TYPE_PVM) && (getAllChallenges().size() > 0) && !current.isInvocation() && !current.isDouble() && !current.isCollector()
                || getType() == Constantes.FIGHT_TYPE_DOPEUL && (getAllChallenges().size() > 0) && !current.isInvocation() && !current.isDouble() && !current.isCollector())
            for (Retos challenge : this.getAllChallenges().values())
                if(challenge != null)
                    challenge.onPlayerStartTurn(current);

        if (current.isDeconnected()) {
            current.setTurnRemaining();
            if (current.getTurnRemaining() <= 0) {
                if (current.getPlayer() != null) {
                    leftFight(current.getPlayer(), null);
                    current.getPlayer().disconnectInFight();
                } else {
                    onFighterDie(current, current);
                    current.setLeft(true);
                }
            } else {
                GestorSalida.GAME_SEND_Im_PACKET_TO_FIGHT(this, 7, "0162;" + current.getPacketsName() + "~" + current.getTurnRemaining());
                this.endTurn(false, current);
                return;
            }
        }

        if (current.getPlayer() == null || current.getDouble() != null || current.getCollector() != null)
            InteligenciaHandler.select(this, current);
    }

    public synchronized void endTurn(boolean onAction, Peleador f) {
        final Peleador current = this.getFighterByOrdreJeu();
        if (current != null)
            if (f == current)
                this.endTurn(onAction);
    }

    public synchronized void endTurn(final boolean onAction) {
        final Peleador current = this.getFighterByOrdreJeu();
        if (current == null)
            return;

        try {
            if (getState() >= Constantes.ESTADO_FIN_DE_PELEA)
                return;
            if (this.turn != null)
                this.turn.stop();

            if (current.hasLeft() || current.isDead()) {
                startTurn();
                return;
            }

            if (!this.getCurAction().equals("")) {
                Temporizador.addSiguiente(() -> this.endTurn(onAction, current), 100);
                return;
            }

            if(current.getState(Constantes.ETAT_PORTEUR) == 0)
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 950, current.getId() + "", current.getId() + "," + Constantes.ETAT_PORTE + ",0");


            GestorSalida.GAME_SEND_GAMETURNSTOP_PACKET_TO_FIGHT(this, 7, current.getId());
            current.setCanPlay(false);
            setCurAction("");

            if(onAction)
                Temporizador.addSiguiente(() -> this.newTurn(current), 2100);
            else
                this.newTurn(current);
        } catch (NullPointerException e) {
            e.printStackTrace();
            this.endTurn(false);
        }
    }

    private void newTurn(Peleador current) {
        // Si empoisonn� (Cr�er une fonction applyEndTurnbuff si
        // d'autres effets existent)
        for (EfectoHechizo SE : current.getBuffsByEffectID(131)) {
            int pas = SE.getValue();
            int val = -1;
            try {
                val = Integer.parseInt(SE.getArgs().split(";")[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (val == -1)
                continue;

            int nbr = (int) Math.floor((double) getCurFighterUsedPa()
                    / (double) pas);
            int dgt = val * nbr;
            // Si poison paralysant
            if (SE.getSpell() == 200) {
                int inte = SE.getCaster().getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
                if (inte < 0)
                    inte = 0;
                int pdom = SE.getCaster().getTotalStats().getEffect(Constantes.STATS_ADD_PERDOM);
                if (pdom < 0)
                    pdom = 0;
                // on applique le boost
                dgt = ((100 + inte + pdom) / 100) * dgt;
            }
            if (current.hasBuff(184)) {
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 105, current.getId() + "", current.getId() + "," + current.getBuff(184).getValue());
                dgt = dgt - current.getBuff(184).getValue();// R�duction physique
            }
            if (current.hasBuff(105)) {
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 105, current.getId() + "", current.getId() + "," + current.getBuff(105).getValue());
                dgt = dgt - current.getBuff(105).getValue();// Immu
            }

            if (dgt <= 0)
                continue;
            if (dgt > current.getPdv())
                dgt = current.getPdv();// va mourrir

            current.removePdv(current, dgt);
            dgt = -(dgt);
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 100, SE.getCaster().getId() + "", current.getId() + "," + dgt);
        }
        // Copie du tableau
        ArrayList<Grifos> glyphs = new ArrayList<>(getAllGlyphs());
        for (Grifos g : glyphs) {
            if (getState() >= Constantes.ESTADO_FIN_DE_PELEA)
                return;
            // Si dans le glyphe
            int dist = Camino.getDistanceBetween(getMap(), current.getCell().getId(), g.getCelda().getId());
            if (dist <= g.getSize() && g.getHechizo() == 476)// 476 a effet en fin de tour, alors le joueur est dans le glyphe
                g.onGrifo(current);
        }

        if (current.getPdv() <= 0)
            onFighterDie(current, getInit0());

        if ((getType() == Constantes.FIGHT_TYPE_PVM) && (getAllChallenges().size() > 0) && !current.isInvocation()
                && !current.isDouble() && !current.isCollector() && current.getTeam() == 0 || getType() == Constantes.FIGHT_TYPE_DOPEUL
                && (getAllChallenges().size() > 0) && !current.isInvocation() && !current.isDouble() && !current.isCollector() && current.getTeam() == 0) {
            for (Entry<Integer, Retos> c : getAllChallenges().entrySet()) {
                if (c.getValue() == null)
                    continue;
                c.getValue().onPlayerEndTurn(current);
            }
        }
        setCurFighterUsedPa();
        setCurFighterUsedPm();
        setCurFighterPa(current.getTotalStats().getEffect(Constantes.STATS_ADD_PA));
        setCurFighterPm(current.getTotalStats().getEffect(Constantes.STATS_ADD_PM));
        current.refreshEndTurnBuff();
        if (current.getPlayer() != null)
            if (current.getPlayer().isOnline())
                GestorSalida.GAME_SEND_STATS_PACKET(current.getPlayer());

        GestorSalida.GAME_SEND_GTM_PACKET_TO_FIGHT(this, 7);
        GestorSalida.GAME_SEND_GTR_PACKET_TO_FIGHT(this, 7, current.getId());
        // Timer d'une seconde � la fin du tour
        this.startTurn();
    }

    public void playerPass(Jugador player) {
        final Peleador fighter = getFighterByPerso(player);
        if (fighter != null)
            if (fighter.canPlay() && this.getCurAction().isEmpty())
                this.endTurn(false, fighter);
    }

    public void joinFight(Jugador perso, int guid) {
        long timeRestant = Constantes.TIEMPO_INICIO_PELEA
                - (Instant.now().toEpochMilli() - launchTime);
        Peleador currentJoin = null;

        if (perso.isDead() == 1)
            return;
        if (isBegin())
            return;
        if (perso.getPelea() != null)
            return;

        if (getTeam0().containsKey(guid)) {
            GameCase cell = getRandomCell(getStart0());
            if (cell == null)
                return;
            if (getType() == Constantes.FIGHT_TYPE_AGRESSION || this.getType() == Constantes.FIGHT_TYPE_PVT) {
                boolean multiIp = false;
                for (Peleador f : getTeam0().values())
                    if (perso.getAccount().getCurrentIp().compareTo(f.getPlayer().getAccount().getCurrentIp()) == 0)
                        multiIp = true;
                if (multiIp) {
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Impossible de rejoindre ce combat, vous êtes déjà dans le combat avec une même IP !");
                    return;
                }
            }
            if (isOnlyGroup0()) {
                Grupo g = getInit0().getPlayer().getParty();
                if (g != null) {
                    if (!g.getPlayers().contains(perso)) {
                        GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(), 'f', guid);
                        return;
                    }
                }
            }
            if (getType() == Constantes.FIGHT_TYPE_AGRESSION) {
                if (perso.get_align() == Constantes.ALINEAMIENTO_NEUTRAL) {
                    GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(), 'f', guid);
                    return;
                }
                if (getInit0().getPlayer().get_align() != perso.get_align()) {
                    GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(), 'f', guid);
                    return;
                }
            }
            if (getType() == Constantes.FIGHT_TYPE_CONQUETE) {
                if (perso.get_align() == Constantes.ALINEAMIENTO_NEUTRAL) {
                    GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(), 'a', guid);
                    return;
                }
                if (getInit0().getPrism().getAlignement() != perso.get_align()) {
                    GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(), 'a', guid);
                    return;
                }
                perso.toggleWings('+');
            }
            if (getGuildId() > -1 && perso.getGuild() != null) {
                if (getGuildId() == perso.getGuild().getId()) {
                    GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(), 'f', guid);
                    return;
                }
            }
            if (isLocked0()) {
                GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(), 'f', guid);
                return;
            }
            if (this.getTeam0().size() >= 8 || this.start0.size() == this.getTeam0().size())
                return;
            if (getType() == Constantes.FIGHT_TYPE_CHALLENGE)
                GestorSalida.GAME_SEND_GJK_PACKET(perso, 2, 1, 1, 0, timeRestant, getType());
            else
                GestorSalida.GAME_SEND_GJK_PACKET(perso, 2, 0, 1, 0, timeRestant, getType());

            GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET(perso.getGameClient(), getMap().getPlaces(), getSt1());
            /*GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.getId() + "", perso.getId() + "," + Constantes.ETAT_PORTE + ",0");
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.getId() + "", perso.getId() + "," + Constantes.ETAT_PORTEUR + ",0");*/
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(), perso.getId());
            GestorSalida.GAME_SEND_GDF_PACKET_TO_FIGHT(perso, this.getMap().getCases());
            Peleador f = new Peleador(this, perso);
            currentJoin = f;
            f.setTeam(0);
            getTeam0().put(perso.getId(), f);
            perso.setPelea(this);
            f.setCell(cell);
            f.getCell().addFighter(f);
        } else if (getTeam1().containsKey(guid)) {
            GameCase cell = getRandomCell(getStart1());
            if (cell == null)
                return;
            if (getType() == Constantes.FIGHT_TYPE_AGRESSION || this.getType() == Constantes.FIGHT_TYPE_PVT) {
                boolean multiIp = false;
                for (Peleador f : getTeam1().values())
                    if (perso.getAccount().getCurrentIp().compareTo(f.getPlayer().getAccount().getCurrentIp()) == 0)
                        multiIp = true;
                if (multiIp) {
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Impossible de rejoindre ce combat, vous êtes déjà dans le combat avec une même IP !");
                    return;
                }
            }
            if (isOnlyGroup1()) {
                Grupo g = getInit1().getPlayer().getParty();
                if (g != null) {
                    if (!g.getPlayers().contains(perso)) {
                        GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(), 'f', guid);
                        return;
                    }
                }
            }
            if (getType() == Constantes.FIGHT_TYPE_AGRESSION) {
                if (perso.get_align() == Constantes.ALINEAMIENTO_NEUTRAL) {
                    GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(), 'f', guid);
                    return;
                }
                if (getInit1().getPlayer().get_align() != perso.get_align()) {
                    GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(), 'f', guid);
                    return;
                }
            }
            if (getType() == Constantes.FIGHT_TYPE_CONQUETE) {
                if (perso.get_align() == Constantes.ALINEAMIENTO_NEUTRAL) {
                    GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(), 'a', guid);
                    return;
                }
                if (getInit1().getPrism().getAlignement() != perso.get_align()) {
                    GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(), 'a', guid);
                    return;
                }
                perso.toggleWings('+');
            }
            if (getGuildId() > -1 && perso.getGuild() != null) {
                if (getGuildId() == perso.getGuild().getId()) {
                    GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(), 'f', guid);
                    return;
                }
            }
            if (isLocked1()) {
                GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(), 'f', guid);
                return;
            }
            if (this.getTeam1().size() >= 8 || this.start1.size() == this.getTeam0().size())
                return;
            if (getType() == Constantes.FIGHT_TYPE_CHALLENGE)
                GestorSalida.GAME_SEND_GJK_PACKET(perso, 2, 1, 1, 0, 0, getType());
            else
                GestorSalida.GAME_SEND_GJK_PACKET(perso, 2, 0, 1, 0, 0, getType());

            GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET(perso.getGameClient(), getMap().getPlaces(), getSt2());
            /*GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.getId() + "", perso.getId() + "," + Constantes.ETAT_PORTE + ",0");
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.getId() + "", perso.getId() + "," + Constantes.ETAT_PORTEUR + ",0");
            */
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(), perso.getId());

            Peleador f = new Peleador(this, perso);
            currentJoin = f;
            f.setTeam(1);
            getTeam1().put(perso.getId(), f);
            perso.setPelea(this);
            f.setCell(cell);
            f.getCell().addFighter(f);
        }

        demorph(perso);

        if(currentJoin == null) return;
        perso.getCurCell().removePlayer(perso);

        GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(perso.getCurMap(), (currentJoin.getTeam() == 0 ? getInit0() : getInit1()).getId(), currentJoin);
        GestorSalida.GAME_SEND_FIGHT_PLAYER_JOIN(this, 7, currentJoin);
        GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS(this, getMap(), perso);

        if (getCollector() != null) {
            Mundo.mundo.getGuild(getGuildId()).getPlayers().stream().filter(Jugador::isOnline).forEach(z -> {
                Recaudador.parseAttaque(z, getGuildId());
                Recaudador.parseDefense(z, getGuildId());
            });
        }
        if (getPrism() != null)
            Mundo.mundo.getOnlinePlayers().stream().filter(Objects::nonNull).filter(z -> z.get_align() == getPrism().getAlignement()).forEach(z -> Prisma.parseAttack(perso));
    }

    private synchronized void joinCollectorFight(final Jugador player,
                                    final int collector) {
        final GameCase cell = getRandomCell(getStart1());

        if (cell == null)
            return;

        GestorSalida.GAME_SEND_GJK_PACKET(player, 2, 0, 1, 0, 0, getType());
        GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET(player.getGameClient(), getMap().getPlaces(), getSt2());
        /*GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, player.getId() + "", player.getId() + "," + Constantes.ETAT_PORTE + ",0");
        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, player.getId() + "", player.getId() + "," + Constantes.ETAT_PORTEUR + ",0");*/
        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(), player.getId());

        Peleador f = new Peleador(this, player);
        f.setTeam(1);
        getTeam1().put(player.getId(), f);
        player.setPelea(this);
        f.setCell(cell);
        f.getCell().addFighter(f);
        player.getCurCell().removePlayer(player);

        GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(player.getCurMap(), collector, f);
        GestorSalida.GAME_SEND_FIGHT_PLAYER_JOIN(this, 7, f);
        GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS(this, getMap(), player);
        GestorSalida.GAME_SEND_GDF_PACKET_TO_FIGHT(player, this.getMap().getCases());
    }

    public void joinPrismFight(final Jugador player, final int team) {
        final GameCase cell = getRandomCell((team == 1 ? this.start1 : this.start0));

        if (cell == null)
            return;

        int prismTeam = (this.getTeam0().containsKey(this.getPrism().getId()) ? 0 : 1);

        if (prismTeam == team) {
            if (player.get_align() != this.getPrism().getAlignement())
                return;
        } else {
            if (player.get_align() == this.getPrism().getAlignement())
                return;
        }

        GestorSalida.GAME_SEND_GJK_PACKET(player, 2, 0, 1, 0, 0, getType());
        GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET(player.getGameClient(), getMap().getPlaces(), getSt2());
       /* GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, player.getId() + "", player.getId() + "," + Constantes.ETAT_PORTE + ",0");
        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, player.getId() + "", player.getId() + "," + Constantes.ETAT_PORTEUR + ",0");*/
        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(), player.getId());

        Peleador f = new Peleador(this, player);
        f.setTeam(team);
        this.getTeam(team + 1).put(player.getId(), f);
        player.setPelea(this);
        f.setCell(cell);
        demorph(player);
        f.getCell().addFighter(f);
        player.getCurCell().removePlayer(player);

        GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(player.getCurMap(), ((Peleador) this.getTeam(team + 1).values().toArray()[0]).getId(), f);
        GestorSalida.GAME_SEND_FIGHT_PLAYER_JOIN(this, 7, f);
        GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS(this, getMap(), player);
        GestorSalida.GAME_SEND_GDF_PACKET_TO_FIGHT(player, this.getMap().getCases());
    }

    public void joinAsSpectator(Jugador p) {
        final Peleador current = this.getFighterByOrdreJeu();
        if (current == null)
            return;

        if (!isBegin() || p.getPelea() != null) {
            GestorSalida.GAME_SEND_Im_PACKET(p, "157");
            return;
        }
        if (p.getGroupe() == null) {
            if (!isViewerOk() || getState() != Constantes.FIGHT_STATE_ACTIVE) {
                GestorSalida.GAME_SEND_Im_PACKET(p, "157");
                return;
            }
        }
        demorph(p);
        p.getCurCell().removePlayer(p);
        GestorSalida.GAME_SEND_GJK_PACKET(p, getState(), 0, 0, 1, 0, getType());
        GestorSalida.GAME_SEND_GS_PACKET(p);
        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(p.getCurMap(), p.getId());
        GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS(this, getMap(), p);
        GestorSalida.GAME_SEND_GAMETURNSTART_PACKET(p, current.getId(), Constantes.TIEMPO_DE_TURNO);
        GestorSalida.GAME_SEND_GTL_PACKET(p, this);

        getViewer().put(p.getId(), p);
        p.setSpec(true);
        p.setPelea(this);

        ArrayList<Peleador> all = new ArrayList<>();
        all.addAll(this.getTeam0().values());
        all.addAll(this.getTeam1().values());
        all.stream().filter(Peleador::isHide).forEach(f -> GestorSalida.GAME_SEND_GA_PACKET(this, p, 150, f.getId() + "", f.getId() + ",4"));
        if (p.getGroupe() == null)
            GestorSalida.GAME_SEND_Im_PACKET_TO_FIGHT(this, 7, "036;" + p.getName());
        if ((getType() == Constantes.FIGHT_TYPE_PVM) && (getAllChallenges().size() > 0) || getType() == Constantes.FIGHT_TYPE_DOPEUL && getAllChallenges().size() > 0) {
            for (Entry<Integer, Retos> c : getAllChallenges().entrySet()) {
                if (c.getValue() == null)
                    continue;
                GestorSalida.GAME_SEND_CHALLENGE_PERSO(p, c.getValue().parseToPacket());
                if (c.getValue().loose())
                    c.getValue().challengeSpecLoose(p);
            }
        }
    }

    public void toggleLockTeam(int guid) {
        if (getInit0() != null && getInit0().getId() == guid) {
            setLocked0(!isLocked0());
            GestorSalida.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), isLocked0() ? '+' : '-', 'A', guid);
            GestorSalida.GAME_SEND_Im_PACKET_TO_FIGHT(this, 1, isLocked0() ? "095" : "096");
        } else if (getInit1() != null && getInit1().getId() == guid) {
            setLocked1(!isLocked1());
            GestorSalida.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit1().getPlayer().getCurMap(), isLocked1() ? '+' : '-', 'A', guid);
            GestorSalida.GAME_SEND_Im_PACKET_TO_FIGHT(this, 2, isLocked1() ? "095" : "096");
        }
    }


    /**
     * Must be called when the spec policy change
     *
     * @param player player who asked for the lock
     */
    public synchronized void toggleLockSpec(Jugador player) {

        //Check that the player is the initiator of one of the two teams
        if (getInit0() != null && getInit0().getId() == player.getId()
                || getInit1() != null && getInit1().getId() == player.getId()) {
            setViewerOk(!isViewerOk());
            GestorSalida.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), isViewerOk() ? '+' : '-', 'S', getInit0().getId());
            GestorSalida.GAME_SEND_Im_PACKET_TO_FIGHT(this, 7, isViewerOk() ? "039" : "040");
        }

        //Kick spectators
        Iterator<Jugador> it = getViewer().values().iterator();
        while (it.hasNext()) {
            Jugador spectator = it.next();
            if (spectator.getGroupe() == null) {
                GestorSalida.GAME_SEND_GV_PACKET(spectator);
                spectator.setPelea(null);
                spectator.setSpec(false);
                spectator.setAway(false);
                it.remove();
            }
        }
    }

    public void toggleOnlyGroup(int guid) {
        if (getInit0() != null && getInit0().getId() == guid) {
            setOnlyGroup0(!isOnlyGroup0());
            GestorSalida.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), isOnlyGroup0() ? '+' : '-', 'P', guid);
            GestorSalida.GAME_SEND_Im_PACKET_TO_FIGHT(this, 1, isOnlyGroup0() ? "093" : "094");
        } else if (getInit1() != null && getInit1().getId() == guid) {
            setOnlyGroup1(!isOnlyGroup1());
            GestorSalida.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit1().getPlayer().getCurMap(), isOnlyGroup1() ? '+' : '-', 'P', guid);
            GestorSalida.GAME_SEND_Im_PACKET_TO_FIGHT(this, 2, isOnlyGroup1() ? "095" : "096");
        }
    }

    public void toggleHelp(int guid) {
        if (getInit0() != null && getInit0().getId() == guid) {
            setHelp0(!isHelp0());
            GestorSalida.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), isHelp0() ? '+' : '-', 'H', guid);
            GestorSalida.GAME_SEND_Im_PACKET_TO_FIGHT(this, 1, isHelp0() ? "0103" : "0104");
        } else if (getInit1() != null && getInit1().getId() == guid) {
            setHelp1(!isHelp1());
            GestorSalida.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit1().getPlayer().getCurMap(), isHelp1() ? '+' : '-', 'H', guid);
            GestorSalida.GAME_SEND_Im_PACKET_TO_FIGHT(this, 2, isHelp1() ? "0103" : "0104");
        }
    }

    public void showCaseToTeam(int guid, int cellID) {
        int teams = getTeamId(guid) - 1;
        if (teams == 4)// Les spectateurs ne montrent pas.
            return;
        ArrayList<JuegoCliente> PWs = new ArrayList<>();
        if (teams == 0) {
            PWs.addAll(getTeam0().values().stream().filter(peleador -> peleador.getPlayer() != null
                    && peleador.getPlayer().getGameClient() != null).map(peleador -> peleador.getPlayer().getGameClient()).collect(Collectors.toList()));
        } else if (teams == 1) {
            PWs.addAll(getTeam1().values().stream().filter(peleador -> peleador.getPlayer() != null
                    && peleador.getPlayer().getGameClient() != null).map(peleador -> peleador.getPlayer().getGameClient()).collect(Collectors.toList()));
        }
        GestorSalida.GAME_SEND_FIGHT_SHOW_CASE(PWs, guid, cellID);
    }

    private void showCaseToAll(int guid, int cellID) {
        ArrayList<JuegoCliente> PWs = new ArrayList<>();
        for (Entry<Integer, Peleador> e : getTeam0().entrySet()) {
            if (e.getValue().getPlayer() != null && e.getValue().getPlayer().getGameClient() != null)
                PWs.add(e.getValue().getPlayer().getGameClient());
        }
        for (Entry<Integer, Peleador> e : getTeam1().entrySet()) {
            if (e.getValue().getPlayer() != null
                    && e.getValue().getPlayer().getGameClient() != null)
                PWs.add(e.getValue().getPlayer().getGameClient());
        }
        for (Entry<Integer, Jugador> e : getViewer().entrySet()) {
            PWs.add(e.getValue().getGameClient());
        }
        GestorSalida.GAME_SEND_FIGHT_SHOW_CASE(PWs, guid, cellID);
    }

    private void initOrderPlaying() {
        int j = 0;
        int k = 0;
        int start0 = 0;
        int start1 = 0;
        int curMaxIni0 = 0;
        int curMaxIni1 = 0;
        Peleador curMax0 = null;
        Peleador curMax1 = null;
        boolean team1_ready = false;
        boolean team2_ready = false;

        do {
            if (!team1_ready) {
                team1_ready = true;
                Map<Integer, Peleador> team = getTeam0();
                for (Entry<Integer, Peleador> entry : team.entrySet()) {
                    if (this.haveFighterInOrdreJeu(entry.getValue()))
                        continue;
                    team1_ready = false;

                    if (entry.getValue().getInitiative() >= curMaxIni0) {
                        curMaxIni0 = entry.getValue().getInitiative();
                        curMax0 = entry.getValue();
                    }
                    if (curMaxIni0 > start0)
                        start0 = curMaxIni0;
                }
            }
            if (!team2_ready) {
                team2_ready = true;
                for (Entry<Integer, Peleador> entry : getTeam1().entrySet()) {
                    if (this.haveFighterInOrdreJeu(entry.getValue()))
                        continue;
                    team2_ready = false;
                    if (entry.getValue().getInitiative() >= curMaxIni1) {
                        curMaxIni1 = entry.getValue().getInitiative();
                        curMax1 = entry.getValue();
                    }
                    if (curMaxIni1 > start1)
                        start1 = curMaxIni1;
                }
            }
            if (curMax1 == null && curMax0 == null) {
                return;
            }
            if (start0 > start1) {
                if (getFighters(1).size() > j) {
                    this.orderPlaying.add(curMax0);
                    j++;
                }
                if (getFighters(2).size() > k) {
                    this.orderPlaying.add(curMax1);
                    k++;
                }
            } else {
                if (getFighters(2).size() > j) {
                    this.orderPlaying.add(curMax1);
                    j++;
                }
                if (getFighters(1).size() > k) {
                    this.orderPlaying.add(curMax0);
                    k++;
                }
            }

            curMaxIni0 = 0;
            curMaxIni1 = 0;
            curMax0 = null;
            curMax1 = null;
        }
        while (this.getOrderPlayingSize() != getFighters(3).size());
    }

    public void tryCaC(Jugador perso, int cellID) {
        final Peleador current = this.getFighterByOrdreJeu();
        if (current == null)
            return;

        Peleador caster = getFighterByPerso(perso);

        if (caster == null)
            return;
        if (current.getId() != caster.getId())// Si ce n'est pas a lui de jouer
            return;
        if (!perso.canCac()) {
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 305, perso.getId() + "", "");// Echec Critique Cac
            endTurn(false, current);
            return;
        }
        if ((getType() == Constantes.FIGHT_TYPE_PVM) && (getAllChallenges().size() > 0) || getType() == Constantes.FIGHT_TYPE_DOPEUL && getAllChallenges().size() > 0) {
            for (Entry<Integer, Retos> c : getAllChallenges().entrySet()) {
                if (c.getValue() == null)
                    continue;
                c.getValue().onPlayerCac(current);
            }
        }
        if (perso.getObjetByPos(Constantes.ITEM_POS_ARME) == null) {
            tryCastSpell(caster, Mundo.mundo.getSort(0).getStatsByLevel(1), cellID);
        } else {
            ObjetoJuego arme = perso.getObjetByPos(Constantes.ITEM_POS_ARME);
            // Pierre d'�mes = EC
            if (arme.getModelo().getType() == 83) {
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 305, perso.getId() + "", "");// Echec Critique Cac
                this.endTurn(false, current);
                return;
            }

            int PACost = arme.getModelo().getPACost();

            if (getCurFighterPa() < PACost) {
                GestorSalida.GAME_SEND_Im_PACKET(perso, "1170;" + getCurFighterPa() + "~" + PACost);
                return;
            }

            int dist = Camino.getDistanceBetween(getMap(), caster.getCell().getId(), cellID);
            int MaxPO = arme.getModelo().getPOmax();
            int MinPO = arme.getModelo().getPOmin();

            if (dist < MinPO || dist > MaxPO) {
                GestorSalida.GAME_SEND_Im_PACKET(perso, "1171;" + MinPO + "~" + MaxPO + "~" + dist);
                return;
            }

            boolean isEc = arme.getModelo().getTauxEC() != 0 && Formulas.getRandomValue(1, arme.getModelo().getTauxEC()) == arme.getModelo().getTauxEC();

            if (isEc) {
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 305, perso.getId() + "", "");// Echec Critique Cac
                endTurn(false, current);
            } else {
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 303, perso.getId() + "", cellID + "");
                boolean isCC = caster.testIfCC(arme.getModelo().getTauxCC());
                if (isCC) {
                    GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 301, perso.getId() + "", "0");
                }

                // Si le joueur est invisible
                if (caster.isHide())
                    caster.unHide(-1);

                ArrayList<EfectoHechizo> effets;

                if (isCC)
                    effets = arme.getCritEffects();
                else
                    effets = arme.getEffects();
                ArrayList<Peleador> cibles = Camino.getCiblesByZoneByWeapon(this, arme.getModelo().getType(), getMap().getCase(cellID), caster.getCell().getId());

                for (EfectoHechizo SE : effets) {
                    try {
                        if (getState() != Constantes.FIGHT_STATE_ACTIVE)
                            break;

                        SE.setTurn(0);
                        if (this.getType() != Constantes.FIGHT_TYPE_CHALLENGE && this.getAllChallenges().size() > 0) {
                            this.getAllChallenges().values().stream().filter(Objects::nonNull)
                                    .forEach(challenge -> challenge.onFightersAttacked(cibles, caster, SE, -1, false));
                        }
                        SE.applyToFight(this, caster, cibles, true);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                /*
                 * 7172 Baguette Rhon
                 * 7156 Marteau Ronton
                 * 1355 Arc Hidsad
                 * 7182 Racine H�couanone
                 * 7040 Arc de Kuri
                 * 6539 Pelle Gicque
                 * 6519 Baguette de Kouartz
                 * 8118 Baguette du Scarabosse Dor�
                 */
                int idArme = arme.getModelo().getId(), basePdvSoin = 1, pdvSoin;
                if (idArme == 7172 || idArme == 7156 || idArme == 1355 || idArme == 7182 || idArme == 7040 || idArme == 6539 || idArme == 6519 || idArme == 8118) {
                    pdvSoin = Constantes.getArmeSoin(idArme);
                    if (pdvSoin != -1) {
                        if (isCC) {
                            basePdvSoin = basePdvSoin + arme.getModelo().getBonusCC();
                            pdvSoin = pdvSoin + arme.getModelo().getBonusCC();
                        }
                        int intel = perso.getCaracteristicas().getEffect(Constantes.STATS_ADD_INTE) + perso.getStuffStats().getEffect(Constantes.STATS_ADD_INTE) + perso.getDonsStats().getEffect(Constantes.STATS_ADD_INTE) + perso.getBuffsStats().getEffect(Constantes.STATS_ADD_INTE);
                        int soins = perso.getCaracteristicas().getEffect(Constantes.STATS_ADD_SOIN) + perso.getStuffStats().getEffect(Constantes.STATS_ADD_SOIN) + perso.getDonsStats().getEffect(Constantes.STATS_ADD_SOIN) + perso.getBuffsStats().getEffect(Constantes.STATS_ADD_SOIN);
                        int minSoin = basePdvSoin * (100 + intel) / 100 + soins;
                        int maxSoin = pdvSoin * (100 + intel) / 100 + soins;

                        for (Peleador target : cibles) {
                            if (target == null) continue;
                            int finalSoin = Formulas.getRandomValue(minSoin, maxSoin);
                            if ((finalSoin + target.getPdv()) > target.getPdvMax())
                                finalSoin = target.getPdvMax() - target.getPdv();// Target

                            target.removePdv(target, -finalSoin);
                            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 100, target.getId() + "", target.getId() + ",+" + finalSoin);
                        }
                    }
                }
                setCurFighterPa(getCurFighterPa() - PACost);
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 102, perso.getId() + "", perso.getId() + ",-" + PACost);
                verifIfTeamAllDead();
            }
        }
    }

    public int forceCastSpellMob(Peleador fighter, Hechizo.SortStats spell, int cell) {
        boolean isEc;
        Peleador current = this.getFighterByOrdreJeu();
        GameCase Cell = this.getMap().getCase(cell);
        if (!this.canCastSpellMob(fighter, spell, Cell, -1)) {
            return 10;
        }
        boolean bl = isEc = spell.getTauxEC() != 0 && Formulas.getRandomValue(1, spell.getTauxEC()) == spell.getTauxEC();
        if (isEc) {
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 302, "" + fighter.getId() + "", "" + spell.getSpellID() + "");
        } else {
            if (!(this.getType() == 0 || this.getAllChallenges().size() <= 0 || current.isInvocation() || current.isDouble() || current.isCollector())) {
                this.getAllChallenges().values().stream().filter(challenge -> challenge != null).forEach(challenge -> {
                            challenge.onPlayerAction(current, spell.getSpellID());
                            if (spell.getSpell().getSpellID() != 0) {
                                challenge.onPlayerSpell(current, spell);
                            }
                        }
                );
            }
            boolean isCC = fighter.testIfCC(spell.getTauxCC(), spell, fighter);
            String sort = "" + spell.getSpellID() + "," + cell + "," + spell.getSpriteID() + "," + spell.getLevel() + "," + spell.getSpriteInfos();
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 300, "" + fighter.getId() + "", sort);
            if (isCC) {
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 301, "" + fighter.getId() + "", sort);
            }
            if (fighter.isHide()) {
                if (spell.getSpellID() == 0) {
                    fighter.unHide(cell);
                } else {
                    this.showCaseToAll(fighter.getId(), fighter.getCell().getId());
                }
            }
            spell.applySpellEffectToFight(this, fighter, Cell, isCC, false);
        }
        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 102, "" + fighter.getId() + "", "" + fighter.getId() + ",-" + spell.getPACost());
        if (!isEc) {
            fighter.addLaunchedSort(Cell.getFirstFighter(), spell, fighter);
        }
        if (isEc && spell.isEcEndTurn()) {
            Temporizador.addSiguiente(() -> {
                        this.setCurAction("");
                    }
                    , 500);
            if (fighter.getMob() != null || fighter.isInvocation()) {
                return 5;
            }
            this.endTurn(false, current);
            return 5;
        }
        this.verifIfTeamAllDead();
        Temporizador.addSiguiente(() -> {
                    this.setCurAction("");
                    if (fighter.getPlayer() != null) {
                        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 102, "" + fighter.getId() + "", "" + fighter.getId() + ",-0");
                    }
                }
                , 1000);
        return 0;
    }

    public synchronized int tryCastSpell(Peleador fighter, Hechizo.SortStats spell, int cell) {
        final Peleador current = this.getFighterByOrdreJeu();

        if (current == null || spell == null || !this.getCurAction().isEmpty() || current != fighter)
            return 10;

        Jugador player = fighter.getPlayer();
        GameCase Cell = getMap().getCase(cell);
        setCurAction("casting");

        if (this.canCastSpell1(fighter, spell, Cell, -1)) {
            if (fighter.getPlayer() != null)
                GestorSalida.GAME_SEND_STATS_PACKET(fighter.getPlayer()); // envoi des stats du lanceur
            if (fighter.getType() == 1 && player.getObjectsClassSpell().containsKey(spell.getSpellID())) {
                int value = player.getValueOfClassObject(spell.getSpellID(), 285);
                this.setCurFighterPa(getCurFighterPa() - (spell.getPACost() - value));
                this.curFighterUsedPa += spell.getPACost() - value;
            } else {
                setCurFighterPa(getCurFighterPa() - spell.getPACost());
                this.curFighterUsedPa += spell.getPACost();
            }

            boolean isEc = spell.getTauxEC() != 0 && Formulas.getRandomValue(1, spell.getTauxEC()) == spell.getTauxEC();

            if (isEc) {
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 302, fighter.getId() + "", spell.getSpellID() + ""); // envoi de  l'EC
            } else {
                if (this.getType() != Constantes.FIGHT_TYPE_CHALLENGE && this.getAllChallenges().size() > 0 && !current.isInvocation() && !current.isDouble() && !current.isCollector()) {
                    this.getAllChallenges().values().stream().filter(Objects::nonNull)
                            .forEach(challenge -> {
                                challenge.onPlayerAction(current, spell.getSpellID());
                                if (spell.getSpell().getSpellID() != 0)
                                    challenge.onPlayerSpell(current, spell);
                            });
                }

                boolean isCC = fighter.testIfCC(spell.getTauxCC(), spell, fighter);
                String sort = spell.getSpellID() + "," + cell + "," + spell.getSpriteID() + "," + spell.getLevel() + "," + spell.getSpriteInfos();
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 300, fighter.getId() + "", sort); // xx lance le sort

                if (isCC)
                    GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 301, fighter.getId() + "", sort); // CC !
                if (fighter.isHide()) // Si le joueur est invi, on montre la case
                {
                    if (spell.getSpellID() == 0)// Si le coup est Coup de Poing alors on refait apparaitre le personnage
                        fighter.unHide(cell);
                    else
                        showCaseToAll(fighter.getId(), fighter.getCell().getId());
                }
                spell.applySpellEffectToFight(this, fighter, Cell, isCC, false); // on applique les effets de l'arme
            }
            // le client ne peut continuer sans l'envoi de ce packet qui annonce le co�t en PA
            if (fighter.getType() == 1 && player.getObjectsClassSpell().containsKey(spell.getSpellID())) {
                int value = player.getValueOfClassObject(spell.getSpellID(), 285);
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 102, fighter.getId() + "", fighter.getId() + ",-" + (spell.getPACost() - value));
            } else {
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 102, fighter.getId() + "", fighter.getId() + ",-" + spell.getPACost());
            }
            if (!isEc)
                fighter.addLaunchedSort(Cell.getFirstFighter(), spell, fighter);

            if ((isEc && spell.isEcEndTurn())) {
                try {
                    Thread.sleep(300); //Fallo Critico - Reduccion delay
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                setCurAction("");
                if (fighter.getMob() != null || fighter.isInvocation()) {
                    return 5;
                } else {
                    endTurn(false, current);
                    return 5;
                }
            }
        } else if (fighter.getMob() != null || fighter.isInvocation()) {
            Temporizador.addSiguiente(() -> this.setCurAction(""), 600);
            return 10;
        }

        this.verifIfTeamAllDead();
            Temporizador.addSiguiente(() -> {
                this.setCurAction("");
                if (fighter.getPlayer() != null) {
                    GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 102, fighter.getId() + "", fighter.getId() + ",-0");
                }
                    }
                    , 1000);
        return 0;
    }

    public boolean canCastSpell1(Peleador caster, Hechizo.SortStats spell, GameCase cell, int targetCell) {
        final Peleador current = this.getFighterByOrdreJeu();
        //boolean hasModification;
        if (current == null)
            return false;

        int casterCell = targetCell <= -1 ? caster.getCell().getId() : targetCell;
        Jugador player = caster.getPlayer();

        if (spell == null) {
            if (player != null) {
                GestorSalida.GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(this, 7);
                GestorSalida.GAME_SEND_Im_PACKET(player, "1169");
                GestorSalida.GAME_SEND_GAF_PACKET_TO_FIGHT(this, 7, 0, player.getId());
            }
            return false;
        }

        if (current.getId() != caster.getId()) {
            if (player != null)
                GestorSalida.GAME_SEND_Im_PACKET(player, "1175");
            return false;
        }

        int usedPA;

        if (caster.getType() == 1 && player.getObjectsClassSpell().containsKey(spell.getSpellID())) {
            int modi = player.getValueOfClassObject(spell.getSpellID(), 285);
            usedPA = spell.getPACost() - modi;
        } else {
            usedPA = spell.getPACost();
        }

        if (getCurFighterPa() < usedPA) {
            if (player != null)
                GestorSalida.GAME_SEND_Im_PACKET(player, "1170;" + getCurFighterPa() + "~" + spell.getPACost());
            return false;
        }

        if (cell == null) {
            if (player != null)
                GestorSalida.GAME_SEND_Im_PACKET(player, "1172");
            return false;
        }

        if (caster.getType() == 1 && player.getObjectsClassSpell().containsKey(spell.getSpellID())) {
            int modi = player.getValueOfClassObject(spell.getSpellID(), 288);
            boolean modif = modi == 1;
            if (spell.isLineLaunch() && !modif && !Camino.casesAreInSameLine(getMap(), casterCell, cell.getId(), 'z', 70)) {
                GestorSalida.GAME_SEND_Im_PACKET(player, "1173");
                return false;
            }
        } else if (spell.isLineLaunch() && !Camino.casesAreInSameLine(getMap(), casterCell, cell.getId(), 'z', 70)) {
            if (player != null)
                GestorSalida.GAME_SEND_Im_PACKET(player, "1173");
            return false;
        }

        char dir = Camino.getDirBetweenTwoCase(casterCell, cell.getId(), getMap(), true);
        if (spell.getSpellID() == 67) {
            if (!Camino.checkLoS(getMap(), Camino.GetCaseIDFromDirrection(casterCell, dir, getMap(), true), cell.getId(), null, true)) {
                if (player != null)
                    GestorSalida.GAME_SEND_Im_PACKET(player, "1174");
                return false;
            }
        }

        if (caster.getType() == 1 && player.getObjectsClassSpell().containsKey(spell.getSpellID())) {
            int modi = player.getValueOfClassObject(spell.getSpellID(), 289);
            boolean modif = modi == 1;
            if (spell.hasLDV() && Camino.checkLoS(getMap(), casterCell, cell.getId()) && !modif) {
                GestorSalida.GAME_SEND_Im_PACKET(player, "1174");
                return false;
            }
        } else if (spell.hasLDV() && Camino.checkLoS(getMap(), casterCell, cell.getId())) {
            if (player != null)
                GestorSalida.GAME_SEND_Im_PACKET(player, "1174");
            return false;
        }

        /*boolean bl = hasModification = player != null && player.getObjectsClassSpell().containsKey(spell.getSpellID());
        if (caster.getType() == 1 && hasModification) {
            boolean modif;
            int modi4 = player.getValueOfClassObject(spell.getSpellID(), 289);
            boolean bl2 = modif = modi4 == 1;
            if (spell.hasLDV() && !Formulas.checkLos(this.getMap(), (short)casterCell, (short)cell.getId()) && !modif) {
                GestorSalida.GAME_SEND_Im_PACKET(player, "1174");
                return false;
            }
        }
        if (!hasModification && spell.hasLDV() && !Formulas.checkLos(this.getMap(), (short)casterCell, (short)cell.getId())) {
            if (player != null) {
                GestorSalida.GAME_SEND_Im_PACKET(player, "1174");
            }
            return false;
        }*/

        int dist = Camino.getDistanceBetween(getMap(), casterCell, cell.getId());
        int maxAlc = spell.getMaxPO();
        int minAlc = spell.getMinPO();
        // + porté
        if (caster.getType() == 1 && player.getObjectsClassSpell().containsKey(spell.getSpellID())) {
            int modi = player.getValueOfClassObject(spell.getSpellID(), 281);
            maxAlc = maxAlc + modi;
        }// porté modifiable

        if (caster.getType() == 1 && player.getObjectsClassSpell().containsKey(spell.getSpellID())) {
            int modi = player.getValueOfClassObject(spell.getSpellID(), 282);
            boolean modif = modi == 1;
            if (spell.isModifPO() || modif) {
                maxAlc += caster.getTotalStats().getEffect(117);
                if (maxAlc <= minAlc)
                    maxAlc = minAlc + 1;
            }
        } else if (spell.isModifPO()) {
            maxAlc += caster.getTotalStats().getEffect(117);
            if (maxAlc <= minAlc)
                maxAlc = minAlc + 1;
        }

        if (maxAlc < minAlc)
            maxAlc = minAlc;
        if (dist < minAlc || dist > maxAlc) {
            if (player != null)
                GestorSalida.GAME_SEND_Im_PACKET(player, "1171;" + minAlc + "~" + maxAlc + "~" + dist);
            return false;
        }

        if (!LanzarHechizo.cooldownGood(caster, spell.getSpellID())) {
            return false;
        }

        int numLunch = spell.getMaxLaunchbyTurn();

        if (caster.getType() == 1 && player.getObjectsClassSpell().containsKey(spell.getSpellID()))
            numLunch += player.getValueOfClassObject(spell.getSpellID(), 290);

        if (numLunch - LanzarHechizo.getNbLaunch(caster, spell.getSpellID()) <= 0 && numLunch > 0) {
            return false;
        }

        Peleador t = cell.getFirstFighter();
        int numLunchT = spell.getMaxLaunchByTarget();

        if (caster.getType() == 1 && player.getObjectsClassSpell().containsKey(spell.getSpellID()))
            numLunchT += player.getValueOfClassObject(spell.getSpellID(), 291);

        return !(numLunchT - LanzarHechizo.getNbLaunchTarget(caster, t, spell.getSpellID()) <= 0 && numLunchT > 0);
    }

    public boolean canCastSpellMob(Peleador caster, Hechizo.SortStats spell, GameCase cell, int targetCell) {
        int casterCell;
        Peleador current = this.getFighterByOrdreJeu();
        if (current == null || spell == null || current.getId() != caster.getId()) {
            return false;
        }
        if (spell.getSpell().hasInvalidState(caster) || this.getCurFighterPa() < spell.getPACost() || cell == null) {
            return false;
        }
        int n = casterCell = targetCell <= -1 ? caster.getCell().getId() : targetCell;
        if (spell.isLineLaunch() && !Camino.casesAreInSameLine(this.getMap(), casterCell, cell.getId(), 'z', 70)) {
            return false;
        }
        if (spell.hasLDV() /*&& !Formulas.checkLos(this.getMap(), (short)casterCell, (short)cell.getId())*/) {
            return false;
        }
        int dist = Camino.getDistanceBetween(this.getMap(), casterCell, cell.getId());
        int maxAlc = spell.getMaxPO();
        int minAlc = spell.getMinPO();
        if (spell.isModifPO() && (maxAlc += caster.getTotalStats().getEffect(117)) <= minAlc) {
            maxAlc = minAlc + 1;
        }
        if (maxAlc < minAlc) {
            maxAlc = minAlc;
        }
        if (dist < minAlc || dist > maxAlc) {
            return false;
        }
        if (!LanzarHechizo.cooldownGood(caster, spell.getSpellID())) {
            return false;
        }
        int numLunch = spell.getMaxLaunchbyTurn();
        if (numLunch - LanzarHechizo.getNbLaunch(caster, spell.getSpellID()) <= 0 && numLunch > 0) {
            return false;
        }
        if (!this.checkKrakenState(caster, spell)) {
            return false;
        }
        Peleador t = cell.getFirstFighter();
        int numLunchT = spell.getMaxLaunchByTarget();
        return numLunchT - LanzarHechizo.getNbLaunchTarget(caster, t, spell.getSpellID()) > 0 || numLunchT <= 0;
    }

    private boolean checkKrakenState(Peleador caster, Hechizo.SortStats spell) {
        switch (spell.getSpellID()) {
            case 1106 -> {
                return caster.haveState(31) && caster.haveState(32) && caster.haveState(33) && caster.haveState(34);
            }
            case 1097 -> {
                return caster.haveState(31);
            }
            case 1098 -> {
                return caster.haveState(32);
            }
            case 1099 -> {
                return caster.haveState(33);
            }
        }
        return true;
    }

    public boolean canLaunchSpell(Peleador caster, Hechizo.SortStats spell, GameCase cell) {
        if (spell == null || spell.getSpell().hasInvalidState(caster) || !spell.getSpell().hasNeededState(caster)) {
            return false;
        }
        if (this.getCurFighterPa() < spell.getPACost()) {
            return false;
        }
        if (!LanzarHechizo.cooldownGood(caster, spell.getSpellID())) {
            return false;
        }
        int numLunch = spell.getMaxLaunchbyTurn();
        if (numLunch - LanzarHechizo.getNbLaunch(caster, spell.getSpellID()) <= 0 && numLunch > 0) {
            return false;
        }
        if (cell == null) {
            return true;
        }
        Peleador t = cell.getFirstFighter();
        int numLunchT = spell.getMaxLaunchByTarget();
        return numLunchT - LanzarHechizo.getNbLaunchTarget(caster, t, spell.getSpellID()) > 0 || numLunchT <= 0;
    }

    public boolean onFighterDeplace(Peleador fighter, AccionJuego GA) {
        final Peleador current = this.getFighterByOrdreJeu();
        if (current == null)
            return false;

        String path = GA.args;
        if (path.equals(""))
            return false;
        if (this.getOrderPlayingSize() <= getCurPlayer())
            return false;
        if (current.getId() != fighter.getId() || this.getState() != Constantes.FIGHT_STATE_ACTIVE)
            return false;

        Peleador targetTacle = Camino.getEnemyAround(fighter.getCell().getId(), getMap(), this);
        this.setCurAction("deplace");

        if (targetTacle != null && !fighter.haveState(6) && !fighter.haveState(8)) {
            int esquive = Formulas.getTacleChance(fighter, targetTacle);
            int rand = Formulas.getRandomValue(0, 99);

            if (rand > esquive) {
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, GA.getId(), "104", fighter.getId() + ";", "");
                int pierdePA = getCurFighterPa() * esquive / 100;
                if (pierdePA < 0)
                    pierdePA = -pierdePA;
                if (getCurFighterPm() < 0)
                    setCurFighterPm(0);
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, GA.getId(), "129", fighter.getId() + "", fighter.getId() + ",-" + getCurFighterPm());
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, GA.getId(), "102", fighter.getId() + "", fighter.getId() + ",-" + pierdePA);
                setCurFighterPm(0);
                setCurFighterPa(getCurFighterPa() - pierdePA);
                this.setCurAction("");
                return false;
            }
        }
        AtomicReference<String> pathRef = new AtomicReference<>(path);
        int nStep = Camino.isValidPath(getMap(), fighter.getCell().getId(), pathRef, this, null, -1);
        String newPath = pathRef.get();

        if (nStep > getCurFighterPm() || nStep == -1000) {
            if (fighter.getPlayer() != null)
                GestorSalida.GAME_SEND_GA_PACKET(fighter.getPlayer().getGameClient(), "", "0", "", "");
            this.setCurAction("");
            return false;
        }
        setCurFighterPm(getCurFighterPm() - nStep);
        this.curFighterUsedPm += nStep;

        int nextCellID = Mundo.mundo.getCryptManager().codigoceldaID(newPath.substring(newPath.length() - 2));
        // les monstres n'ont pas de GAS//GAF
        if (current.getPlayer() != null)
            GestorSalida.GAME_SEND_GAS_PACKET_TO_FIGHT(this, 7, current.getId());

        // Si le joueur n'est pas invisible
        if (!current.isHide()) {
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, GA.getId(), "1", current.getId() + "", "a" + Mundo.mundo.getCryptManager().idceldaCodigo(fighter.getCell().getId()) + newPath);
        } else {
            if (current.getPlayer() != null) {
                // On envoie le path qu'au joueur qui se d�place
                JuegoCliente out = current.getPlayer().getGameClient();
                GestorSalida.GAME_SEND_GA_PACKET(out, GA.getId() + "", "1", current.getId() + "", "a" + Mundo.mundo.getCryptManager().idceldaCodigo(fighter.getCell().getId()) + newPath);
            }
        }

        // Si port�
        final Peleador po = current.getHoldedBy();

        if(po!=null) {
            // si le joueur va bouger
            if((short)nextCellID!=po.getCell().getId()) {
                // on retire les �tats
                po.setState(Constantes.ETAT_PORTEUR,0,po.getId());
                current.setState(Constantes.ETAT_PORTE,0,po.getId());
                // on retire d� lie les 2 fighters
                po.setIsHolding(null);
                current.setHoldedBy(null);
                // La nouvelle case sera d�finie plus tard dans le code. On
                // envoie les packets !
            }
        }

        current.getCell().getFighters().clear();
        current.setCell(getMap().getCase(nextCellID));
        current.getCell().addFighter(current);

        if (po != null)// m�me erreur que tant�t, bug ou plus de fighter sur la
            // case
            po.getCell().addFighter(po);
        if (nStep < 0) {
            nStep = nStep * (-1);
        }

        setCurAction("GA;129;" + current.getId() + ";" + current.getId() + ",-" + nStep);
        // Si porteur
        final Peleador po2=current.getIsHolding();

        if(po2!=null&&current.haveState(Constantes.ETAT_PORTEUR)&&po2.haveState(Constantes.ETAT_PORTE)) {
            // on d�place le port� sur la case
            po2.setCell(current.getCell());
        }

        if(fighter.getPlayer()==null) {
            try{
                Thread.sleep((int)(400+(100*Math.sqrt(nStep))));
            } catch(final Exception ignored) {
            }
            this.setCurAction("");
            EfectoHechizo.VerificarTrampa(this,fighter);
            return true;
        }

        if ((getType() == Constantes.FIGHT_TYPE_PVM) && (getAllChallenges().size() > 0) && !current.isInvocation() && !current.isDouble() && !current.isCollector() || (getType() == Constantes.FIGHT_TYPE_DOPEUL) && (getAllChallenges().size() > 0) && !current.isInvocation() && !current.isDouble() && !current.isCollector())
            this.getAllChallenges().values().stream().filter(Objects::nonNull).forEach(c -> c.onPlayerMove(fighter));

        if(fighter.getPlayer()!=null)
            EfectoHechizo.VerificarTrampa(this,fighter);
        Objects.requireNonNull(fighter.getPlayer()).getGameClient().addAction(GA);
        return true;
    }

    public void removeCarry(Peleador target)
    {
        Peleador carry=target.getHoldedBy();
        carry.setState(Constantes.ETAT_PORTEUR,0,target.getId()); //duration 0, remove state
        carry.setIsHolding(null);
        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,51,carry.getId()+"",carry.getCell().getId()+"");
    }

    public void onFighterDie(Peleador target, Peleador caster) {
        if(Configuracion.INSTANCE.getHEROIC()) {
            Jugador player = caster.getPlayer(), deadPlayer = target.getPlayer();

            if(deadPlayer != null) {
                byte type = caster.isMob() ? (byte) 2 : player == deadPlayer ? (byte) -1 : (byte) 1;
                long id = type == 1 ? player.getId() : type == 2 ? caster.getMob().getTemplate().getId() : 0;
                target.killedBy = new Doble<>(type, id);
            }
            if (player != null && target != caster && deadPlayer != null)
                player.increaseTotalKills();
        }

        target.setIsDead(true);
        if(!target.hasLeft())
            this.getDeadList().add(new Doble<>(target.getId(), target));

        final Peleador current = this.getFighterByOrdreJeu();
        if (current == null)
            return;

        GestorSalida.GAME_SEND_FIGHT_PLAYER_DIE_TO_FIGHT(this,7,target.getId());
        target.getCell().getFighters().clear();// Supprime tout causait bug si port�/porteur
        if(target.haveState(Constantes.ETAT_PORTEUR))
        {
            Peleador f=target.getIsHolding();
            f.setCell(f.getCell());
            f.getCell().addFighter(f);// Le bug venait par manque de ceci, il ni avait plus de firstFighter
            f.setState(Constantes.ETAT_PORTE,0,caster.getId());// J'ajoute ceci quand m�me pour signaler qu'ils ne sont plus en �tat port�/porteur
            target.setState(Constantes.ETAT_PORTEUR,0,caster.getId());
            f.setHoldedBy(null);
            target.setIsHolding(null);
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,950,f.getId()+"",f.getId()+","+Constantes.ETAT_PORTE+",0");
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,950,target.getId()+"",target.getId()+","+Constantes.ETAT_PORTEUR+",0");
        }

        if(target.haveState(Constantes.ETAT_PORTE))
            Temporizador.addSiguiente(() -> removeCarry(target),3000, Temporizador.DataType.PELEA); //timer so mob only gets dropped after already despawning

        if ((this.getType() == Constantes.FIGHT_TYPE_PVM) && (this.getAllChallenges().size() > 0) || this.getType() == Constantes.FIGHT_TYPE_DOPEUL && this.getAllChallenges().size() > 0)
            this.getAllChallenges().values().stream().filter(Objects::nonNull).forEach(challenge -> challenge.onFighterDie(target));

        if (target.getTeam() == 0) {
            HashMap<Integer, Peleador> team = new HashMap<>(this.getTeam0());

            for (Peleador entry : team.values()) {
                if (entry.getInvocator() == null)
                    continue;
                if (entry.getPdv() == 0 || entry.isDead())
                    continue;

                if (entry.getInvocator().getId() == target.getId()) {
                    this.onFighterDie(entry, caster);

                    try {
                        if(this.getOrderPlaying() != null) {
                            int index = this.getOrderPlaying().indexOf(entry);
                            if (index != -1)
                                this.getOrderPlaying().remove(index);
                        }
                        if (this.getTeam0().containsKey(entry.getId()))
                            this.getTeam0().remove(entry.getId());
                        else if (this.getTeam1().containsKey(entry.getId()))
                            this.getTeam1().remove(entry.getId());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 999, target.getId() + "", this.getGTL());
                }
            }
        } else if (target.getTeam() == 1) {
            HashMap<Integer, Peleador> team = new HashMap<>(this.getTeam1());

            for (Peleador fighter : team.values()) {
                if (fighter.getInvocator() == null)
                    continue;
                if (fighter.getPdv() == 0 || fighter.isDead())
                    continue;
                if (fighter.getInvocator().getId() == target.getId()) {// si il a �t� invoqu� par le joueur mort
                    fighter.setLevelUp(true);
                    this.onFighterDie(fighter, caster);

                    if (this.getOrderPlaying() != null && !this.getOrderPlaying().isEmpty()) {
                        try {
                            int index = this.getOrderPlaying().indexOf(fighter);
                            if (index != -1)
                                this.getOrderPlaying().remove(index);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (this.getTeam0().containsKey(fighter.getId()))
                        this.getTeam0().remove(fighter.getId());
                    else if (this.getTeam1().containsKey(fighter.getId()))
                        this.getTeam1().remove(fighter.getId());
                    GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 999, target.getId() + "", getGTL());
                }
            }
        }
        if (target.getMob() != null) {
            try {
                if (target.isInvocation() && !target.isStatique) {
                    target.getInvocator().nbrInvoc--;
                    // Il ne peut plus jouer, et est mort on revient au joueur
                    // pr�cedent pour que le startTurn passe au suivant
                    if (!target.canPlay() && current.getId() == target.getId()) {
                        this.setCurPlayer(getCurPlayer() - 1);
                        this.endTurn(false, current);
                    }
                    // Il peut jouer, et est mort alors on passe son tour
                    // pour que l'autre joue, puis on le supprime de l'index
                    // sans probl�mes
                    if (target.canPlay() && current.getId() == target.getId()) {
                        this.setCurAction("");
                        this.endTurn(false, current);
                    }
                    if (this.getOrderPlaying() != null && !this.getOrderPlaying().isEmpty()) {
                        int index = this.getOrderPlaying().indexOf(target);
                        // Si le joueur courant a un index plus �lev�, on le
                        // diminue pour �viter le outOfBound
                        if (index != -1) {
                            if (getCurPlayer() > index && getCurPlayer() > 0)
                                this.setCurPlayer(getCurPlayer() - 1);
                            this.getOrderPlaying().remove(index);
                        }

                        if (this.getCurPlayer() < 0)
                            return;
                        if (this.getTeam0().containsKey(target.getId()))
                            this.getTeam0().remove(target.getId());
                        else if (this.getTeam1().containsKey(target.getId()))
                            this.getTeam1().remove(target.getId());
                        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 999, target.getId() + "", this.getGTL());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if ((getType() == Constantes.FIGHT_TYPE_PVM || getType() == Constantes.FIGHT_TYPE_DOPEUL) && getAllChallenges().size() > 0)
            this.getAllChallenges().values().stream().filter(Objects::nonNull).forEach(challenge -> challenge.onMobDie(target, caster));

        new ArrayList<>(this.getAllGlyphs()).stream().filter(glyph -> glyph.getLanzador().getId() == target.getId()).forEach(glyph -> {
            GestorSalida.GAME_SEND_GDZ_PACKET_TO_FIGHT(this, 7, "-", glyph.getCelda().getId(), glyph.getSize(), 4);
            GestorSalida.GAME_SEND_GDC_PACKET_TO_FIGHT(this, 7, glyph.getCelda().getId());
            this.getAllGlyphs().remove(glyph);
        });

        new ArrayList<>(this.getAllTraps()).stream().filter(trap -> trap.getLanzador().getId() == target.getId()).forEach(trap -> {
            trap.desaparecer();
            this.getAllTraps().remove(trap);
        });

        if (target.canPlay() && current.getId() == target.getId() && !current.hasLeft()) // java.lang.NullPointerException
            this.endTurn(false, current);
        if (target.isCollector()) {// Le percepteur viens de mourrir on met fin au
            this.getFighters(target.getTeam2()).stream().filter(f -> !f.isDead()).forEach(f -> {
                this.onFighterDie(f, target);
                this.verifIfTeamAllDead();
            });
        }
        if (target.isPrisme()) {
            this.getFighters(target.getTeam2()).stream().filter(f -> !f.isDead()).forEach(f -> {
                this.onFighterDie(f, target);
                this.verifIfTeamAllDead();
            });
        }

        for (Peleador fighter : getFighters(3)) {
            ArrayList<EfectoHechizo> newBuffs = new ArrayList<>();
            for (EfectoHechizo entry : fighter.getFightBuff()) {
                switch (entry.getSpell()) {
                    case 431, 433, 437, 441, 443 -> {
                        newBuffs.add(entry);
                        continue;
                    }
                }
                if (entry.getCaster().getId() != target.getId())
                    newBuffs.add(entry);
            }
            fighter.getFightBuff().clear();
            fighter.getFightBuff().addAll(newBuffs);
        }
        GestorSalida.GAME_SEND_GTL_PACKET_TO_FIGHT(this, 7);
        this.verifIfTeamAllDead();
    }

    public ArrayList<Peleador> getFighters(int teams) {// Entre 0 et 7, binaire([spec][t2][t1]).
        ArrayList<Peleador> fighters = new ArrayList<>();

        if (teams - 4 >= 0) {
            fighters.addAll(new ArrayList<>(this.getViewer().values()).stream().filter(Objects::nonNull).map(player -> new Peleador(this, player)).collect(Collectors.toList()));
            teams -= 4;
        }
        if (teams - 2 >= 0) {
            new ArrayList<>(this.getTeam1().values()).stream().filter(Objects::nonNull).forEach(fighters::add);
            teams -= 2;
        }
        if (teams - 1 >= 0)
            new ArrayList<>(this.getTeam0().values()).stream().filter(Objects::nonNull).forEach(fighters::add);
        return fighters;
    }

    ArrayList<Peleador> getFighters2(int teams) {
        ArrayList<Peleador> fighters = new ArrayList<>();

        if (teams == 0)
            fighters.addAll(getViewer().values().stream().map(jugador -> new Peleador(this, jugador)).collect(Collectors.toList()));
        if (teams == 2)
            fighters.addAll(getTeam1().values().stream().collect(Collectors.toList()));
        if (teams == 1)
            fighters.addAll(getTeam0().values().stream().collect(Collectors.toList()));
        return fighters;
    }

    public Peleador getFighterByPerso(Jugador player) {
        Peleador fighter = null;
        if (this.getTeam0().get(player.getId()) != null)
            fighter = this.getTeam0().get(player.getId());
        if (this.getTeam1().get(player.getId()) != null)
            fighter = this.getTeam1().get(player.getId());
        return fighter;
    }

    private GameCase getRandomCell(List<GameCase> cells) {
        GameCase cell;

        if (cells.isEmpty())
            return null;

        int limit = 0;
        do {
            int id = Formulas.random.nextInt(cells.size());
            cell = cells.get(id);
            limit++;
        }
        while ((cell == null || !cell.getFighters().isEmpty()) && limit < 80);
        if (limit == 80) {
            return null;
        }
        return cell;
    }

    public synchronized void exchangePlace(Jugador perso, int cell) {
        Peleador fighter = getFighterByPerso(perso);
        assert fighter != null;
        int team = fighter.getTeam();
        if (collector != null && this.collectorProtect && collector.getDefenseFight() != null && !collector.getDefenseFight().containsValue(perso))
            return;
        boolean valid1 = false, valid2 = false;

        for (int a = 0; a < getStart0().size(); a++)
            if (getStart0().get(a).getId() == cell) {
                valid1 = true;
                break;
            }
        for (int a = 0; a < getStart1().size(); a++)
            if (getStart1().get(a).getId() == cell) {
                valid2 = true;
                break;
            }
        if (getState() != 2 || isOccuped(cell) || perso.isReady() || (team == 0 && !valid1) || (team == 1 && !valid2))
            return;
        fighter.getCell().getFighters().clear();
        fighter.setCell(getMap().getCase(cell));
        getMap().getCase(cell).addFighter(fighter);
        GestorSalida.GAME_SEND_FIGHT_CHANGE_PLACE_PACKET_TO_FIGHT(this, 3, getMap(), perso.getId(), cell);
    }

    public boolean isOccuped(int cell) {
        return getMap().getCase(cell) == null || getMap().getCase(cell).getFighters().size() > 0;
    }

    public int getNextLowerFighterGuid() {
        return nextId--;
    }

    public void addFighterInTeam(Peleador f, int team) {
        if (team == 0)
            getTeam0().put(f.getId(), f);
        else if (team == 1)
            getTeam1().put(f.getId(), f);
    }

    private void addChevalier() {
        StringBuilder groupData = new StringBuilder();
        int a = 0;
        for (Peleador F : getTeam0().values()) {
            if (F.getPlayer() == null)
                continue;
            if (getTeam1().size() > getTeam0().size())
                continue;
            groupData.append("394,").append(Constantes.getLevelForChevalier(F.getPlayer())).append(",").append(Constantes.getLevelForChevalier(F.getPlayer()));
            if (a < getTeam0().size() - 1)
                groupData.append(";");
            a++;
        }
        setMobGroup(new Monstruos.MobGroup(getMapOld().nextObjectId, getInit0().getPlayer().getCurCell().getId(), groupData.toString()));
        for (Entry<Integer, Monstruos.MobGrade> entry : getMobGroup().getMobs().entrySet()) {
            entry.getValue().setInFightID(entry.getKey());
            getTeam1().put(entry.getKey(), new Peleador(this, entry.getValue()));
        }
        List<Entry<Integer, Peleador>> e = new ArrayList<>(getTeam1().entrySet());
        for (Entry<Integer, Peleador> entry : e) {
            if (entry.getValue().getPlayer() != null)
                continue;
            Peleador f = entry.getValue();
            GameCase cell = getRandomCell(getStart1());
            if (cell == null) {
                getTeam1().remove(f.getId());
            } else {
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, f.getId()
                        + "", f.getId() + "," + Constantes.ETAT_PORTE + ",0");
                GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, f.getId()
                        + "", f.getId() + "," + Constantes.ETAT_PORTEUR + ",0");
                f.setCell(cell);
                f.getCell().addFighter(f);
                f.setTeam(1);
                GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), getMobGroup().getId(), entry.getValue());
                GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit1().getPlayer().getCurMap(), getMobGroup().getId(), entry.getValue());
            }
        }
        GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this, 7, getMap());
    }

    public boolean playerDisconnect(Jugador player, boolean verif) {
        // True si entre en mode d�connexion en combat, false sinon
        if(this.getState() == Constantes.FIGHT_STATE_INIT || this.getState() == Constantes.FIGHT_STATE_PLACE) {
            player.setReady(true);
            player.getPelea().verifIfAllReady();
            GestorSalida.GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(player.getPelea(), 3, player.getId(), true);
            return true;
        }

        final Peleador current = this.getFighterByOrdreJeu();
        if (current == null)
            return false;

        Peleador f = getFighterByPerso(player);
        if (f == null)
            return false;
        if(player.start != null) {
            this.endFight(true);
            return true;
        }
        if (getState() == Constantes.FIGHT_STATE_INIT
                || getState() == Constantes.ESTADO_FIN_DE_PELEA) {
            if (!verif)
                leftFight(player, null);
            return false;
        }
        if (f.getNbrDisconnection() >= 5) {
            if (!verif) {
                leftFight(player, null);
                for (Peleador e : this.getFighters(7)) {
                    if (e.getPlayer() == null || !e.getPlayer().isOnline())
                        continue;
                    GestorSalida.GAME_SEND_MESSAGE(e.getPlayer(), f.getPacketsName()
                            + " s'est déconnecté plus de 5 fois dans le même combat, nous avons décidé de lui faire abandonner.", "A00000");
                }
            }
            return false;
        }
        if (!verif) {
            if (!isBegin()) {
                player.setReady(true);
                player.getPelea().verifIfAllReady();
                GestorSalida.GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(player.getPelea(), 3, player.getId(), true);
            }
        }
        if (!verif) {
            if (!player.getPelea().getFighterByPerso(player).isDeconnected())
                GestorSalida.GAME_SEND_Im_PACKET_TO_FIGHT(this, 7, "1182;" + f.getPacketsName() + "~20");
            f.Disconnect();
        }
        if (current.getId() == f.getId())
            endTurn(false, current);
        return true;
    }

    public boolean playerReconnect(final Jugador player) {
        final Peleador current = this.getFighterByOrdreJeu();
        if (current == null)
            return false;

        final Peleador f = getFighterByPerso(player);
        if (f == null)
            return false;
        if (getState() == Constantes.FIGHT_STATE_INIT)
            return false;
        f.Reconnect();
        if (getState() == Constantes.ESTADO_FIN_DE_PELEA)
            return false;
        // Si combat en cours on envois des im
        ArrayList<Peleador> all = new ArrayList<>();
        all.addAll(this.getTeam0().values());
        all.addAll(this.getTeam1().values());
        all.stream().filter(fighter -> fighter != null && fighter.isHide()).forEach(f1 -> GestorSalida.GAME_SEND_GA_PACKET(this, player, 150, f1.getId() + "", f1.getId() + ",4"));

        GestorSalida.GAME_SEND_Im_PACKET_TO_FIGHT(this, 7, "1184;" + f.getPacketsName());

        if (getState() == Constantes.FIGHT_STATE_ACTIVE)
            GestorSalida.GAME_SEND_GJK_PACKET(player, getState(), 0, 0, 0, 0, getType());// Join Fight => getState(), pas d'anulation...
        else {
            if (getType() == Constantes.FIGHT_TYPE_CHALLENGE)
                GestorSalida.GAME_SEND_GJK_PACKET(player, 2, 1, 1, 0, 0, getType());
            else
                GestorSalida.GAME_SEND_GJK_PACKET(player, 2, 0, 1, 0, 0, getType());
        }

        GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player, getMap(), (f.getTeam() == 0 ? getInit0() : getInit1()).getId(), f);// Indication de la team
        GestorSalida.GAME_SEND_STATS_PACKET(player);

        GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS(this, getMap(), player);

        if (getState() == Constantes.FIGHT_STATE_PLACE) {
            GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET(player.getGameClient(), getMap().getPlaces(), getSt1());
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, player.getId() + "", player.getId() + "," + Constantes.ETAT_PORTE + ",0");
            GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, player.getId() + "", player.getId() + "," + Constantes.ETAT_PORTEUR + ",0");
        } else {
            GestorSalida.GAME_SEND_GS_PACKET(player);// D�but du jeu
            GestorSalida.GAME_SEND_GTL_PACKET(player, this);// Liste des tours
            GestorSalida.GAME_SEND_GAMETURNSTART_PACKET(player, current.getId(), (int) (Instant.now().toEpochMilli() - launchTime));

            if (this.getType() == Constantes.FIGHT_TYPE_PVM || this.getType() == Constantes.FIGHT_TYPE_DOPEUL && this.getAllChallenges().size() > 0) {
                this.getAllChallenges().values().stream().filter(challenge -> challenge != null && challenge.loose())
                        .forEach(challenge -> {
                            GestorSalida.GAME_SEND_CHALLENGE_PERSO(player, challenge.parseToPacket());
                            if(challenge.loose())
                                challenge.challengeSpecLoose(player);
                        });
            }
            for (Peleador f1 : getFighters(3)) {
                f1.sendState(player);
            }
        }
        return true;
    }

    public void verifIfAllReady() {
        boolean val = true;
        if (getType() == Constantes.FIGHT_TYPE_DOPEUL) {
            for (Peleador f : getTeam0().values()) {
                if (f == null || f.getPlayer() == null)
                    continue;
                Jugador perso = f.getPlayer();
                if (!perso.isReady())
                    val = false;
            }
            if (val)
                startFight();
            return;
        }

        for (int a = 0; a < getTeam0().size(); a++)
            if (!getTeam0().get(getTeam0().keySet().toArray()[a]).getPlayer().isReady())
                val = false;

        if (getType() != 4 && getType() != 5 && getType() != 7
                && getType() != Constantes.FIGHT_TYPE_CONQUETE)
            for (int a = 0; a < getTeam1().size(); a++)
                if (!getTeam1().get(getTeam1().keySet().toArray()[a]).getPlayer().isReady())
                    val = false;

        if (getType() == 5 || getType() == 2)
            val = false;
        if (val)
            startFight();
    }

    private boolean verifyStillInFight()// Return true si au moins un joueur est encore dans le combat
    {
        for (Peleador f : getTeam0().values()) {
            if (f.isCollector())
                return false;
            if (f.isInvocation() || f.isDead() || f.getPlayer() == null
                    || f.getMob() != null || f.getDouble() != null
                    || f.hasLeft())
                continue;
            if (f.getPlayer() != null
                    && f.getPlayer().getPelea() != null
                    && f.getPlayer().getPelea().getId() == this.getId()) // Si il n'est plus dans ce combat
                return false;
        }
        for (Peleador f : getTeam1().values()) {
            if (f.isCollector())
                return false;
            if (f.isInvocation() || f.isDead() || f.getPlayer() == null
                    || f.getMob() != null || f.getDouble() != null
                    || f.hasLeft())
                continue;
            if (f.getPlayer() != null
                    && f.getPlayer().getPelea() != null
                    && f.getPlayer().getPelea().getId() == this.getId()) // Si il n'est plus dans ce combat
                return false;
        }
        return true;
    }

    boolean verifIfTeamIsDead() {
        boolean finish = true;
        for (Entry<Integer, Peleador> entry : getTeam1().entrySet()) {
            if (entry.getValue().isInvocation())
                continue;
            if (!entry.getValue().isDead()) {
                finish = false;
                break;
            }
        }
        return finish;
    }

    public void verifIfTeamAllDead() {
        if (getState() >= Constantes.ESTADO_FIN_DE_PELEA)
            return;

        boolean team0 = true, team1 = true;

        for (Peleador fighter : getTeam0().values()) {
            if (fighter.isInvocation())
                continue;
            if (!fighter.isDead()) {
                team0 = false;
                break;
            }
        }

        for (Peleador fighter : getTeam1().values()) {
            if (fighter.isInvocation())
                continue;
            if (!fighter.isDead()) {
                team1 = false;
                break;
            }
        }

        if ((team0 || team1 || verifyStillInFight()) && !finish) {
            this.finish = true;

            final Map<Integer, Peleador> copyTeam0 = new HashMap<>();
            final Map<Integer, Peleador> copyTeam1 = new HashMap<>();
            for (Entry<Integer, Peleador> entry : this.getTeam0().entrySet()) {
                if (entry.getValue().getMob() != null)
                    if (entry.getValue().getMob().getTemplate().getId() == 375)
                        Bandidos.getBandits().setPop(false);
                copyTeam0.put(entry.getKey(), entry.getValue());
            }

            for (Entry<Integer, Peleador> entry : this.getTeam1().entrySet()) {
                if (entry.getValue().getMob() != null)
                    if (entry.getValue().getMob().getTemplate().getId() == 375)
                        Bandidos.getBandits().setPop(false);
                copyTeam1.put(entry.getKey(), entry.getValue());
            }

            final boolean winners = team0;

            final ArrayList<Peleador> fighters = new ArrayList<>();
            fighters.addAll(copyTeam0.values());
            fighters.addAll(copyTeam1.values());
            this.turn.stop();
            this.turn = null;
            try {
                StringBuilder challenges = new StringBuilder();
                if ((getType() == Constantes.FIGHT_TYPE_PVM && getAllChallenges().size() > 0)
                        || (getType() == Constantes.FIGHT_TYPE_DOPEUL && getAllChallenges().size() > 0)) {
                    for (Retos challenge : getAllChallenges().values()) {
                        if (challenge != null) {
                            challenge.fightEnd();
                            challenges.append((challenges.length() == 0) ? challenge.getPacketEndFight() : "," + challenge.getPacketEndFight());
                        }
                    }
                }

                this.setState(Constantes.ESTADO_FIN_DE_PELEA);

                final ArrayList<Peleador> winTeam = new ArrayList<>(), looseTeam = new ArrayList<>();

                if (winners) {
                    looseTeam.addAll(copyTeam0.values());
                    winTeam.addAll(copyTeam1.values());
                } else {
                    winTeam.addAll(copyTeam0.values());
                    looseTeam.addAll(copyTeam1.values());
                }

                if (Constantes.FIGHT_TYPE_PVM == this.getType() && this.getMapOld().hasEndFightAction(this.getType())) {
                    for (Peleador fighter : winTeam) {
                        Jugador player = fighter.getPlayer();
                        if (player == null)
                            continue;

                        player.setPelea(null);
                        if (fighter.isDeconnected()) {
                            player.setNeededEndFight(this.getType(), this.getMobGroup());
                            player.getCurMap().applyEndFightAction(player);
                            player.setNeededEndFight(-1, null);
                        } else {
                            player.setNeededEndFight(this.getType(), this.getMobGroup());
                        }
                    }
                }

                final String packet = this.getGE(winners ? 2 : 1);

                for (Peleador fighter : fighters) {
                    Jugador player = fighter.getPlayer();
                    if (player != null) {
                        player.setPelea(null);
                        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.getMap(), fighter.getId());
                    }
                }

                this.setCurPlayer(-1);

                switch (this.getType()) {
                    case Constantes.FIGHT_TYPE_CHALLENGE:
                    case Constantes.FIGHT_TYPE_AGRESSION:
                    case Constantes.FIGHT_TYPE_CONQUETE:
                        for (Peleador fighter : copyTeam1.values()) {
                            Jugador player = fighter.getPlayer();

                            if (player != null) {
                                player.setDuelId(-1);
                                player.setReady(false);
                            }
                        }
                        break;
                }

                for (Peleador fighter : this.getTeam0().values()) {
                    Jugador player = fighter.getPlayer();

                    if (player != null) {
                        player.setDuelId(-1);
                        player.setReady(false);
                    }
                }

                for (Peleador fighter : this.getFighters(3))
                    fighter.getFightBuff().clear();

                this.getMapOld().removeFight(this.getId());
                GestorSalida.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(Mundo.mundo.getMap(this.getMap().getId()));

                final String str = (this.getPrism() != null ? this.getPrism().getMap() + "|" + this.getPrism().getX() + "|" + this.getPrism().getY() : "");

                this.setMap(null);
                this.orderPlaying = null;

                for (Peleador fighter : winTeam) {
                    if (fighter.getCollector() != null) {
                        Mundo.mundo.getGuild(getGuildId()).getPlayers().stream().filter(Objects::nonNull).filter(Jugador::isOnline).forEach(player -> {
                            GestorSalida.GAME_SEND_gITM_PACKET(player, Recaudador.parseToGuild(player.getGuild().getId()));
                            GestorSalida.GAME_SEND_PERCO_INFOS_PACKET(player, fighter.getCollector(), "S");
                        });

                        fighter.getCollector().setInFight((byte) 0);
                        fighter.getCollector().set_inFightID((byte) -1);
                        fighter.getCollector().clearDefenseFight();

                        this.getMapOld().getPlayers().stream().filter(Objects::nonNull)
                                .forEach(player -> GestorSalida.GAME_SEND_MAP_PERCO_GMS_PACKETS(player.getGameClient(), player.getCurMap()));
                    }
                    if (fighter.getPrism() != null) {
                        Mundo.mundo.getOnlinePlayers().stream().filter(Objects::nonNull).filter(player -> player.get_align() == getPrism().getAlignement())
                                .forEach(player -> GestorSalida.SEND_CS_SURVIVRE_MESSAGE_PRISME(player, str));

                        fighter.getPrism().setInFight(-1);
                        fighter.getPrism().setFightId(-1);

                        this.getMapOld().getPlayers().stream().filter(Objects::nonNull)
                                .forEach(player -> GestorSalida.SEND_GM_PRISME_TO_MAP(player.getGameClient(), player.getCurMap()));
                    }

                    if (fighter.isInvocation())
                        continue;
                    if (fighter.hasLeft())
                        continue;

                    this.onPlayerWin(fighter, looseTeam);
                }
                //Fin ganadores

                //Perdedores
                for (Peleador fighter : looseTeam) {
                    if (fighter.getCollector() != null) {
                        Mundo.mundo.getGuild(getGuildId()).getPlayers().stream().filter(Objects::nonNull).filter(Jugador::isOnline).forEach(player -> {
                            GestorSalida.GAME_SEND_gITM_PACKET(player, Recaudador.parseToGuild(player.getGuild().getId()));
                            GestorSalida.GAME_SEND_PERCO_INFOS_PACKET(player, fighter.getCollector(), "D");
                        });

                        this.getMapOld().RemoveNpc(fighter.getCollector().getId());
                        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(getMapOld(), fighter.getCollector().getId());
                        fighter.getCollector().reloadTimer();
                        this.getCollector().delCollector(fighter.getCollector().getId());
                        Database.estaticos.getCollectorData().delete(fighter.getCollector().getId());
                    }

                    if (fighter.getPrism() != null) {
                        SubArea subarea = this.getMapOld().getSubArea();

                        for (Jugador player : Mundo.mundo.getOnlinePlayers()) {
                            if (player == null)
                                continue;

                            if (player.get_align() == 0) {
                                GestorSalida.GAME_SEND_am_ALIGN_PACKET_TO_SUBAREA(player, subarea.getId() + "|-1|1");
                                continue;
                            }

                            if (player.get_align() == getPrism().getAlignement())
                                GestorSalida.SEND_CD_MORT_MESSAGE_PRISME(player, str);

                            GestorSalida.GAME_SEND_am_ALIGN_PACKET_TO_SUBAREA(player, subarea.getId() + "|-1|0");

                            if (getPrism().getConquestArea() != -1) {
                                GestorSalida.GAME_SEND_aM_ALIGN_PACKET_TO_AREA(player, subarea.area.getId() + "|-1");
                                subarea.area.prismId = 0;
                                subarea.area.setAlignement(0);
                            }
                            GestorSalida.GAME_SEND_am_ALIGN_PACKET_TO_SUBAREA(player, subarea.getId() + "|0|1");
                        }
                        final int id = fighter.getPrism().getId();
                        subarea.prismId = 0;
                        subarea.setAlignement(0);
                        this.getMapOld().RemoveNpc(id);
                        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(getMapOld(), id);
                        Mundo.mundo.removePrisme(id);
                        Database.estaticos.getPrismData().delete(id);
                    }

                    if (fighter.getMob() != null)
                        continue;
                    if (fighter.isInvocation())
                        continue;

                    this.onPlayerLoose(fighter);
                }

                for (Jugador player : this.getViewer().values()) {
                    player.refreshMapAfterFight();
                    player.setSpec(false);
                    player.send(packet);

                    if (player.getAccount().isBanned())
                        player.getGameClient().kick();
                }

                for (Peleador fighter : fighters) {
                    Jugador player = fighter.getPlayer();
                    if (player != null) {
                        if (this.isBegin()) {
                            if (player.getCurMap().getId() == 8357 && player.hasItemTemplate(7373, 1) && player.hasItemTemplate(7374, 1) && player.hasItemTemplate(7375, 1) && player.hasItemTemplate(7376, 1) && player.hasItemTemplate(7377, 1) && player.hasItemTemplate(7378, 1)) {
                                player.removeByTemplateID(7373, 1);
                                player.removeByTemplateID(7374, 1);
                                player.removeByTemplateID(7375, 1);
                                player.removeByTemplateID(7376, 1);
                                player.removeByTemplateID(7377, 1);
                                player.removeByTemplateID(7378, 1);
                            }
                            player.send(packet);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                for (Peleador fighter : fighters) {
                    Jugador player = fighter.getPlayer();
                    if (player != null) {
                        player.setDuelId(-1);
                        player.setReady(false);
                        player.setPelea(null);
                        GestorSalida.GAME_SEND_GV_PACKET(player);
                    }
                }
            }

            for (Peleador fighter : fighters) {
                Jugador player = fighter.getPlayer();

                if (player == null)
                    continue;

                if (player.getPelea() != null)
                    player.setPelea(null);

                player.refreshLife(false);

                if (!player.getCurCell().isWalkable(true))
                    player.teleport(player.getCurMap(), player.getCurMap().getRandomFreeCellId());
                if (player.getAccount().isBanned())
                    player.getGameClient().kick();
                if (fighter.isDeconnected())
                    player.getAccount().disconnect(player);
                if(player.getMorphMode())
                    GestorSalida.GAME_SEND_SPELL_LIST(player);
            }
        }

    }

    void onPlayerWin(Peleador fighter, ArrayList<Peleador> looseTeam) {
        Jugador player = fighter.getPlayer();

        if (player == null)
            return;

        player.afterFight = true;

        ObjetoJuego weapon = player.getObjetByPos(Constantes.ITEM_POS_ARME);
        if (weapon != null) {
            if (weapon.getTxtStat().containsKey(Constantes.STATS_RESIST)) {
                int statNew = Integer.parseInt(weapon.getTxtStat().get(Constantes.STATS_RESIST), 16) - 1;
                if (statNew <= 0) {
                    GestorSalida.send(player, "Im160");
                    player.removeItem(weapon.getId(), 1, true, true);
                } else {
                    weapon.getTxtStat().remove(Constantes.STATS_RESIST); // on retire les stats "32c"
                    weapon.addTxtStat(Constantes.STATS_RESIST, Integer.toHexString(statNew));// on ajout les bonnes stats
                    GestorSalida.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(player, weapon);
                }
            }
        }

        if (this.getType() != Constantes.FIGHT_TYPE_CHALLENGE) {
            if (fighter.getPdv() <= 0)
                player.setPdv(1);
            else
                player.setPdv(fighter.getPdv());

            if(fighter.getLevelUp()) player.fullPDV();
        }

        if (this.getType() == 2)
            if (this.getPrism() != null)
                GestorSalida.SEND_CP_INFO_DEFENSEURS_PRISME(player, this.getDefenders());

        if (this.getType() == Constantes.FIGHT_TYPE_PVT)
            if (player.getGuildMember() != null)
                if (this.getCollector().getGuildId() == player.getGuildMember().getGuild().getId())
                    player.teleportOldMap();

        if (this.getType() == Constantes.FIGHT_TYPE_PVM) {
            ObjetoJuego obj = player.getObjetByPos(Constantes.ITEM_POS_FAMILIER);
            if (obj != null) {
                Map<Integer, Integer> souls = new HashMap<>();

                for (Peleador f : looseTeam) {
                    if (f.getMob() == null)
                        continue;

                    int id = f.getMob().getTemplate().getId();

                    if (!souls.isEmpty() && souls.containsKey(id))
                        souls.put(id, souls.get(id) + 1);
                    else
                        souls.put(id, 1);
                }
                if (!souls.isEmpty()) {
                    MascotaEntrada pet = Mundo.mundo.getPetsEntry(obj.getId());
                    if (pet != null)
                        pet.eatSouls(player, souls);
                }
            }
        }
    }

    void onPlayerLoose(Peleador fighter) {
        final Jugador player = fighter.getPlayer();

        if (player == null)
            return;

        if (player.getMorphMode() && player.donjon)
            player.unsetFullMorph();

        ObjetoJuego arme = player.getObjetByPos(Constantes.ITEM_POS_ARME);

        if (arme != null) {
            if (arme.getTxtStat().containsKey(Constantes.STATS_RESIST)) {
                int statNew = Integer.parseInt(arme.getTxtStat().get(Constantes.STATS_RESIST), 16) - 1;
                if (statNew <= 0) {
                    GestorSalida.send(player, "Im160");
                    player.removeItem(arme.getId(), 1, true, true);
                } else {
                    arme.getTxtStat().remove(Constantes.STATS_RESIST); // on retire les stats "32c"
                    arme.addTxtStat(Constantes.STATS_RESIST, Integer.toHexString(statNew));// on ajout les bonnes stats
                    GestorSalida.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(player, arme);
                }
            }
        }

        if (player.getObjetByPos(Constantes.ITEM_POS_FAMILIER) != null && this.getType() != Constantes.FIGHT_TYPE_CHALLENGE) {
            ObjetoJuego obj = player.getObjetByPos(Constantes.ITEM_POS_FAMILIER);
            if (obj != null) {
                MascotaEntrada pets = Mundo.mundo.getPetsEntry(obj.getId());
                if (pets != null)
                    pets.looseFight(player);
            }
        }

        if (player.getObjetByPos(Constantes.ITEM_POS_PNJ_SUIVEUR) != null)
            player.setMascotte(0);

        if (this.getType() == 2)
            if (this.getPrism() != null)
                GestorSalida.SEND_CP_INFO_DEFENSEURS_PRISME(player, this.getDefenders());

        if (this.getType() == Constantes.FIGHT_TYPE_AGRESSION || this.getType() == Constantes.FIGHT_TYPE_CONQUETE) {
            int honor = player.get_honor() - 500;
            if (honor < 0) honor = 0;
            player.set_honor(honor);
        }

        if (this.getType() != Constantes.FIGHT_TYPE_CHALLENGE) {
            int loose = Formulas.getLoosEnergy(player.getLevel(), getType() == 1, getType() == 5);
            int energy = player.getEnergy() - loose;

            player.setEnergy((Math.max(energy, 0)));

            if (player.isOnline())
                GestorSalida.GAME_SEND_Im_PACKET(player, "034;" + loose);
            if(Configuracion.INSTANCE.getHEROIC()) {
                if(fighter.killedBy != null)
                    player.die(fighter.killedBy.getPrimero(),fighter.killedBy.getSegundo());
            } else {
                if (energy <= 0) {
                    if (this.getType() == Constantes.FIGHT_TYPE_AGRESSION && fighter.getTraqued()) {
                        if (getTeam1().containsValue(fighter))
                            player.teleportFaction(this.getAlignementOfTraquer(this.getTeam0().values(), player));
                        else
                            player.teleportFaction(this.getAlignementOfTraquer(this.getTeam1().values(), player));
                        player.setEnergy(1);
                    } else {
                        player.setFuneral();
                    }
                } else {
                    if (this.getType() == Constantes.FIGHT_TYPE_AGRESSION && fighter.getTraqued()) {
                        if (getTeam1().containsValue(fighter))
                            player.teleportFaction(this.getAlignementOfTraquer(this.getTeam0().values(), player));
                        else
                            player.teleportFaction(this.getAlignementOfTraquer(this.getTeam1().values(), player));
                    } else {
                        if (player.getCurMap() != null && player.getCurMap().getSubArea() != null && (player.getCurMap().getSubArea().getId() == 319 || player.getCurMap().getSubArea().getId() == 210)) {
                            player.setNeededEndFightAction(new Accion(1001, "9558,224", "", null));
                            player.teleportLaby((short) 9558, 224);
                            Temporizador.addSiguiente(() -> {
                                Minotot.sendPacketMap(player); // Retarde le paquet sinon les portes sont ferm�s. Le paquet de GameInformation doit faire chier ce p�d�
                                player.setPdv(1);
                            }, 3500, Temporizador.DataType.CLIENTE);
                        } else {
                            player.setNeededEndFightAction(new Accion(1001, player.getSavePosition(), "", null));
                            player.setPdv(1);
                        }
                    }
                }
            }
        }
    }

    int getAlignementOfTraquer(Collection<Peleador> fighters,
                               Jugador player) {
        for (Peleador fighter : fighters)
            if (fighter.getPlayer() != null)
                if (fighter.getPlayer().get_traque().getTraque() == player)
                    return (int) fighter.getPlayer().get_align();
        return 0;
    }

    public void onGK(Jugador player) {
        final Peleador current = this.getFighterByOrdreJeu();
        if (current == null)
            return;
        if (getCurAction().equals("") || current.getId() != player.getId() || getState() != Constantes.FIGHT_STATE_ACTIVE)
            return;

        GestorSalida.GAME_SEND_GAMEACTION_TO_FIGHT(this, 7, this.getCurAction());
        GestorSalida.GAME_SEND_GAF_PACKET_TO_FIGHT(this, 7, 2, current.getId());

        if(!current.getJustTrapped() && current.getFight() != null) {
            EfectoHechizo.VerificarTrampa(current.getFight(),current);
        }

        this.setCurAction("");
    }

    public String getGE(int win) {
        int type = Constantes.FIGHT_TYPE_CHALLENGE;

        if (this.getType() == Constantes.FIGHT_TYPE_AGRESSION || getType() == Constantes.FIGHT_TYPE_CONQUETE)
            type = 1;
        if (this.getType() == Constantes.FIGHT_TYPE_PVT)
            type = Constantes.FIGHT_TYPE_CHALLENGE;

        final StringBuilder packet = new StringBuilder();

        packet.append("GE").append(Instant.now().toEpochMilli() - getStartTime());
        if (getType() == Constantes.FIGHT_TYPE_PVM && getMobGroup() != null)
            packet.append(';').append(getMobGroup().getStarBonus());
        packet.append("|").append(this.getInit0().getId()).append("|").append(type).append("|");

        ArrayList<Peleador> winners = new ArrayList<>(), loosers = new ArrayList<>();

        Iterator iterator = this.getTeam0().entrySet().iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            Peleador fighter = (Peleador) entry.getValue();

            if(fighter.isInvocation() && fighter.getMob() != null && fighter.getMob().getTemplate().getId() != 285) iterator.remove();
            if(fighter.isDouble()) iterator.remove();
        }

        iterator = this.getTeam1().entrySet().iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            Peleador fighter = (Peleador) entry.getValue();

            if(fighter.isInvocation() && fighter.getMob() != null && fighter.getMob().getTemplate().getId() != 285) iterator.remove();
            if(fighter.isDouble()) iterator.remove();
        }

        if (win == 1) {
            winners.addAll(this.getTeam0().values());
            loosers.addAll(this.getTeam1().values());
        } else {
            winners.addAll(this.getTeam1().values());
            loosers.addAll(this.getTeam0().values());
        }
        
        if(this.kolizeum != null) {
            for (Peleador f : winners) {
                packet.append("2;").append(f.getId()).append(";").append(f.getPacketsName()).append(";")
                        .append(f.getLvl()).append(";").append((f.getPdv() == 0 || f.hasLeft()) ? 1 : 0)
                        .append(";").append(f.xpString(";")).append(";0;;;").append(TeamMatch.OBJECT)
                        .append("~").append(TeamMatch.QUANTITY).append(";").append(TeamMatch.KAMAS).append("|");

                f.getPlayer().setCurrentPositionToOldPosition();
                f.getPlayer().sendMessage("Vous venez de gagner " + TeamMatch.KAMAS + " kamas et 3 kolizetons suite à votre victoire au Kolizeum !");
                f.getPlayer().addKamas(TeamMatch.KAMAS);
                f.getPlayer().addObjet(Mundo.mundo.getObjetoModelo(TeamMatch.OBJECT).createNewItem(TeamMatch.QUANTITY, true), true);
            }

            for (Peleador f : loosers) {
                f.getPlayer().setCurrentPositionToOldPosition();

                packet.append("0;").append(f.getId()).append(";").append(f.getPacketsName()).append(";").append(f.getLvl())
                        .append(";1").append(";").append(f.xpString(";")).append(";;;;|");
                f.getPlayer().sendMessage("Vous venez de perdre le Kolizeum, vous gagnerez la prochaine fois !");
            }

            FightManager.removeTeamMatch(kolizeum);
            return packet.toString();
        } else if(this.deathMatch != null) {
            for (Peleador f : winners) {
                this.deathMatch.finish(f.getPlayer());
                f.getPlayer().setCurrentPositionToOldPosition();
                packet.append("2;").append(f.getId()).append(";").append(f.getPacketsName()).append(";")
                        .append(f.getLvl()).append(";").append((f.getPdv() == 0 || f.hasLeft()) ? 1 : 0)
                        .append(";").append(f.xpString(";")).append(";0;;;").append(deathMatch.winObject.getModelo().getId())
                        .append("~").append(1).append(";").append(0).append("|");
            }

            for (Peleador f : loosers) {

                f.getPlayer().setCurrentPositionToOldPosition();
                packet.append("0;").append(f.getId()).append(";").append(f.getPacketsName()).append(";").append(f.getLvl())
                        .append(";1").append(";").append(f.xpString(";")).append(";;;;|");
            }
            return packet.toString();
        }
        
        try {
            /* Var heroic mod **/
            boolean team = false;

            long totalXP = 0;
            for (Peleador F : loosers) {
                if (F.getMob() != null)
                    totalXP += F.getMob().getBaseXp();
                if(F.getPlayer() != null)
                    team = true;
            }

            /* Capture d'�mes **/
            boolean mobCapturable = true;
            for (Peleador fighter : loosers) {
                if(fighter.getMob() == null || fighter.getMob().getTemplate() == null || !fighter.getMob().getTemplate().isCapturable()) {
                    mobCapturable = false;
                }
                if(fighter.getMob() != null && fighter.getMob().getTemplate() != null) {
                    for (int[] protector : OficioConstantes.JOB_PROTECTORS) {
                        if(protector[0] == fighter.getMob().getTemplate().getId()) {
                            mobCapturable = false;
                        }
                    }
                }
            }

            if (mobCapturable && !PiedraAlma.isInArenaMap(this.getMapOld().getId())) {
                boolean isFirst = true;
                int maxLvl = 0;
                StringBuilder stats = new StringBuilder();

                for (Peleador fighter : loosers) {
                    if(fighter.isInvocation() || fighter.getInvocator() != null)
                        continue;
                    stats.append(isFirst ? "" : "|").append(fighter.getMob().getTemplate().getId()).append(",").append(fighter.getLvl());
                    isFirst = false;
                    if (fighter.getLvl() > maxLvl)
                        maxLvl = fighter.getLvl();
                }

                this.setFullSoul(new PiedraAlma(Database.dinamicos.getObjectData().getNextId(), 1, 7010, Constantes.ITEM_POS_NO_EQUIPED, stats.toString())); // Cr�e la pierre d'�me
                winners.stream().filter(F -> !F.isInvocation() && F.haveState(Constantes.ETAT_CAPT_AME)).forEach(F -> getCapturer().add(F));

                if (this.getCapturer().size() > 0 && !PiedraAlma.isInArenaMap(this.getMapOld().getId())) // S'il y a des captureurs
                {
                    for (int i = 0; i < this.getCapturer().size(); i++) {
                        try {
                            Peleador f = this.getCapturer().get(Formulas.getRandomValue(0, this.getCapturer().size() - 1)); // R�cup�re un captureur au hasard dans la liste
                            if(f != null && f.getPlayer() != null) {
                                if(f.getPlayer().getObjetByPos(Constantes.ITEM_POS_ARME) == null || !(f.getPlayer().getObjetByPos(Constantes.ITEM_POS_ARME).getModelo().getType() == Constantes.ITEM_TYPE_PIERRE_AME)) {
                                    this.getCapturer().remove(f);
                                    continue;
                                }
                                Doble<Integer, Integer> playerSoulStone = Formulas.decompPierreAme(f.getPlayer().getObjetByPos(Constantes.ITEM_POS_ARME));// R�cup�re les stats de la pierre �quipp�

                                if (playerSoulStone.getSegundo() < maxLvl) {// Si la pierre est trop faible
                                    this.getCapturer().remove(f);
                                    continue;
                                }
                                if (Formulas.getRandomValue(1, 100) <= Formulas.totalCaptChance(playerSoulStone.getPrimero(), f.getPlayer())) {// Si le joueur obtiens la capture Retire la pierre vide au personnage et lui envoie ce changement
                                    int emptySoulStone = f.getPlayer().getObjetByPos(Constantes.ITEM_POS_ARME).getId();
                                    f.getPlayer().deleteItem(emptySoulStone);
                                    GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(f.getPlayer(), emptySoulStone);
                                    this.setCaptWinner(f.getId());
                                    break;
                                }
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            /* Capture d'�mes **/

            /* Quest **/
            if (this.getType() == Constantes.FIGHT_TYPE_PVM || this.getType() == Constantes.FIGHT_TYPE_DOPEUL) {
                for (Peleador fighter : winners) {
                    Jugador player = fighter.getPlayer();
                    if (player == null) continue;

                    if (!player.getQuestPerso().isEmpty()) {
                        for (Peleador ennemy : loosers) {
                            if (ennemy.getMob() == null) continue;
                            if (ennemy.getMob().getTemplate() == null) continue;

                            for (MisionJugador questP : player.getQuestPerso().values()) {
                                if(questP == null) continue;
                                Mision quest = questP.getQuest();
                                if(quest == null) continue;
                                quest.getQuestSteps().stream().filter(qEtape -> !questP.isQuestStepIsValidate(qEtape) && (qEtape.getType() == 0 || qEtape.getType() == 6)).filter(qEtape -> qEtape.getMonsterId() == ennemy.getMob().getTemplate().getId()).forEach(qEtape -> {
                                    try {
                                        player.getQuestPersoByQuest(qEtape.getQuestData()).getMonsterKill().put(ennemy.getMob().getTemplate().getId(), (short) 1);
                                        qEtape.getQuestData().updateQuestData(player, false, 2);
                                    } catch(Exception e) {
                                        e.printStackTrace();
                                        player.sendMessage("Report to an admin : " + e.getMessage());
                                    }
                                });
                            }
                        }
                    }
                }
            }
            /* Apprivoisement **/
            boolean amande = false, rousse = false, doree = false;

            for (Peleador fighter : loosers) {
                try {
                    if (fighter.getMob().getTemplate().getId() == 171)
                        amande = true;
                    if (fighter.getMob().getTemplate().getId() == 200)
                        rousse = true;
                    if (fighter.getMob().getTemplate().getId() == 666)
                        doree = true;
                } catch (Exception e) {
                    amande = false;
                    rousse = false;
                    doree = false;
                    break;
                }
            }
            if (amande || rousse || doree) {
                winners.stream().filter(fighter -> !fighter.isInvocation() && fighter.haveState(Constantes.ETAT_APPRIVOISEMENT)).forEach(F -> getTrainer().add(F));
                if (getTrainer().size() > 0) {
                    for (int i = 0; i < getTrainer().size(); i++) {
                        try {
                            Peleador f = getTrainer().get(Formulas.getRandomValue(0, getTrainer().size() - 1)); // R�cup�re un captureur au hasard dans la liste
                            Jugador player = f.getPlayer();
                            if (player.getObjetByPos(Constantes.ITEM_POS_ARME) == null || !(player.getObjetByPos(Constantes.ITEM_POS_ARME).getModelo().getType() == Constantes.ITEM_TYPE_FILET_CAPTURE)) {
                                getTrainer().remove(f);
                                continue;
                            }
                            int chance = Formulas.getRandomValue(1, 100), appriChance = Formulas.totalAppriChance(amande, rousse, doree, player);
                            if (chance <= appriChance) {
                                // Retire le filet au personnage et lui envoie ce changement
                                int filet = player.getObjetByPos(Constantes.ITEM_POS_ARME).getId();
                                player.deleteItem(filet);
                                GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(player, filet);
                                setTrainerWinner(f.getId());
                                break;
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            int memberGuild = 0;

            if (this.getType() == Constantes.FIGHT_TYPE_PVT && win == 1)
                for (Peleador i : winners)
                    if (i.getPlayer() != null)
                        if (i.getPlayer().getGuildMember() != null)
                            memberGuild++;

            int lvlLoosers = 0, lvlWinners = 0, lvlMaxLooser = 0, lvlMax, lvlMin, challXp = 0;
            byte nbbonus = 0;
            for (Retos c : getAllChallenges().values())
                if (c != null && c.getWin())
                    challXp += c.getXp();

            for (Peleador entry : loosers)
                lvlLoosers += entry.getLvl();

            for (Peleador entry : winners) {
                lvlWinners += entry.getLvl();
                if (entry.getLvl() > lvlMaxLooser
                        && entry.getPlayer() != null)
                    lvlMaxLooser = entry.getLvl();
            }
            if (lvlLoosers > lvlWinners) {
                lvlMax = lvlLoosers;
                lvlMin = lvlWinners;
            } else {
                lvlMax = lvlWinners;
                lvlMin = lvlLoosers;
            }
            for (Peleador entry : winners)
                if (entry.getLvl() > (lvlMaxLooser / 3)
                        && entry.getPlayer() != null)
                    nbbonus += 1;

            if (lvlWinners <= 0)
                lvlWinners = 1;

            Map<Integer, Integer> mobs = new HashMap<>();
            loosers.stream().filter(mob -> mob.getMob() != null).forEach(mob -> {
                if (mobs.get(mob.getMob().getTemplate().getId()) != null)
                    mobs.put(mob.getMob().getTemplate().getId(), mobs.get(mob.getMob().getTemplate().getId()) + 1); // Quantite
                else
                    mobs.put(mob.getMob().getTemplate().getId(), 1);
            });

            Collections.sort(winners);
            Map<Integer, StringBuilder> gains = new HashMap<>();

            //Inicio Drop
            // Calcul the total prospecting.
            int totalProspecting = 0;
            double challengeFactor = 0, starFactor = this.getMobGroup() != null ? (this.getMobGroup().getStarBonus() / 100) + 1 : 1;

            for (Peleador fighter : winners)
                if(fighter != null && !fighter.isDouble())
                    if (!fighter.isInvocation() || (fighter.getMob() != null && fighter.getMob().getTemplate() != null && fighter.getMob().getTemplate().getId() == 285))
                        totalProspecting += fighter.getPros();

            if (starFactor < 1) starFactor = 1;
            if (totalProspecting < 0) totalProspecting = 0;
            // Calcul the total challenge percent.
            if (this.getType() == Constantes.FIGHT_TYPE_PVM && this.getAllChallenges().size() > 0)
                for (Retos challenge : this.getAllChallenges().values())
                    if (challenge.getWin()) challengeFactor += challenge.getDrop();
            if (challengeFactor < 1) challengeFactor = 1;
            challengeFactor = 1 + (challengeFactor / 100);

            ArrayList<Drop> dropsPlayers = new ArrayList<>(), dropsMeats = new ArrayList<>();
            Collection<ObjetoJuego> dropsCollector = null;
            Doble<Integer, Integer> kamas;

            if (this.getType() == Constantes.FIGHT_TYPE_PVT && win == 1) {
                int kamasCollector = (int) Math.ceil(collector.getKamas() / winners.size());
                kamas = new Doble<>(kamasCollector, kamasCollector);
                dropsCollector = this.getCollector().getDrops();
            } else {
                int minKamas = 0, maxKamas = 0;
                for (Peleador fighter : loosers) {
                    if (!fighter.isInvocation() && fighter.getMob() != null && !fighter.isDouble()) {
                        minKamas += fighter.getMob().getTemplate().getMinKamas();
                        maxKamas += fighter.getMob().getTemplate().getMaxKamas();

                        for (Drop drop1 : fighter.getMob().getTemplate().getDrops()) {
                            if (drop1.getAction() == 1) {
                                Drop drop = drop1.copy(fighter.getMob().getGrade());
                                if (drop == null) continue;
                                dropsMeats.add(drop);
                            } else {
                                Drop drop;
                                if (drop1.getCeil() <= totalProspecting && fighter.getMob() != null) {
                                    drop = drop1.copy(fighter.getMob().getGrade());
                                    if (drop == null) continue;
                                    dropsPlayers.add(drop);
                                }
                            }
                        }
                    }
                }

                kamas = new Doble<>(minKamas, maxKamas);
            }
            // Sort fighter by prospecting.
            ArrayList<Peleador> temporary1 = new ArrayList<>();
            Peleador higherFighter = null;
            while (temporary1.size() < winners.size()) {
                int currentProspecting = -1;
                for (Peleador fighter : winners) {
                    if (fighter.getTotalStats().getEffect(Constantes.STATS_ADD_PROS) > currentProspecting && !temporary1.contains(fighter)) {
                        higherFighter = fighter;
                        currentProspecting = fighter.getTotalStats().getEffect(Constantes.STATS_ADD_PROS);
                    }
                }
                temporary1.add(higherFighter);
            }
            winners.clear();
            winners.addAll(temporary1);
            final NumberFormat formatter = new DecimalFormat("#0.000");
            //Fin drop

            // Hablar
            Jugador curPlayer = null;
            boolean stalk = false;
            int quantity = 2;
            if (this.getType() == Constantes.FIGHT_TYPE_AGRESSION) {
                boolean isAlone = true;

                for (Peleador fighter : winners)
                    if (!fighter.isInvocation())
                        curPlayer = fighter.getPlayer();

                for (Peleador fighter : winners)
                    if (fighter.getPlayer() != curPlayer && !fighter.isInvocation())
                        isAlone = false;

                if (isAlone) {
                    for (Peleador fighter : loosers) {
                        if (!fighter.isInvocation() && curPlayer != null && curPlayer.get_traque() != null && curPlayer.get_traque().getTraque() == fighter.getPlayer()) {
                            GestorSalida.GAME_SEND_MESSAGE(curPlayer, "Thomas Sacre : Contrat fini, reviens me voir pour récuperer ta récompense.", "000000");
                            curPlayer.get_traque().setTime(-2);
                            stalk = true;
                            fighter.setTraqued(true);

                            Stalk stalkTarget = fighter.getPlayer().get_traque();

                            if (stalkTarget != null)
                                if (stalkTarget.getTraque() == curPlayer)
                                    quantity = 4;

                            ObjetoJuego object = Mundo.mundo.getObjetoModelo(10275).createNewItem(quantity, false);
                            if (curPlayer.addObjet(object, true))
                                Mundo.addGameObject(object, true);
                            kamas = new Doble<>(1000 * quantity, 1000 * quantity);
                            curPlayer.addKamas(1000 * quantity);
                        }
                    }
                }

                if (!stalk) {
                    Jugador traqued = null;
                    curPlayer = null;

                    for (Peleador fighter : loosers)
                        if (fighter.getPlayer() != null)
                            if (fighter.getPlayer().get_traque() != null)
                                traqued = fighter.getPlayer().get_traque().getTraque();

                    if (traqued != null)
                        for (Peleador fighter : winners)
                            if (fighter.getPlayer() == traqued)
                                curPlayer = traqued;

                    if (curPlayer != null) {
                        kamas = new Doble<>(1000 * quantity, 1000 * quantity);
                        curPlayer.addKamas(1000 * quantity);
                        ObjetoJuego object = Mundo.mundo.getObjetoModelo(10275).createNewItem(quantity, false);
                        if (curPlayer.addObjet(object, true))
                            Mundo.addGameObject(object, true);
                        stalk = true;
                    }
                }
            }
            //Hablar

            //Heroico
            Map<Jugador, String> list = null;
            ArrayList<ObjetoJuego> objects = null;

            if(Configuracion.INSTANCE.getHEROIC()) {
                switch(this.getType()) {
                    case Constantes.FIGHT_TYPE_AGRESSION:
                        final ArrayList<ObjetoJuego> objects1 = new ArrayList<>();

                        for (final Peleador fighter : loosers) {
                            final Jugador player = fighter.getPlayer();
                            if (player != null) objects1.addAll(player.getItems().values());
                        }

                        list = Pelea.give(objects1, winners);
                        break;
                    case Constantes.FIGHT_TYPE_PVM:
                        try {
                            final Monstruos.MobGroup group = this.getMobGroup();

                            if (team) { // Players have loose the fight, mob win the fight
                                objects = new ArrayList<>();
                                for (final Peleador fighter : loosers) {
                                    final Jugador player = fighter.getPlayer();
                                    if (player != null)
                                        objects.addAll(player.getItems().values());
                                }

                                if(group.isFix()) {
                                    String infos = this.getMapOld().getId() + "," + group.getCellId();
                                    if(Mapa.fixMobGroupObjects.get(infos) != null) {
                                        objects.addAll(Mapa.fixMobGroupObjects.get(infos));
                                        Mapa.fixMobGroupObjects.remove(infos);
                                        Mapa.fixMobGroupObjects.put(infos, objects);
                                    } else {
                                        Mapa.fixMobGroupObjects.put(infos, objects);
                                        Database.estaticos.getHeroicMobsGroups().insertFix(this.getMapOld().getId(), group, objects);
                                    }
                                } else {
                                    group.getObjects().addAll(objects);
                                    this.getMapOld().respawnGroup(group);
                                    Database.estaticos.getHeroicMobsGroups().insert(this.getMapOld().getId(), group, objects);
                                }
                            } else { // mob loose..
                                list = Pelea.give(group.isFix() ? Mapa.fixMobGroupObjects.get(this.getMapOld().getId() + "," + group.getCellId()) : group.getObjects(), winners);
                                if(!group.isFix()) this.getMapOld().spawnAfterTimeGroup();
                            }
                        } catch(Exception e) { e.printStackTrace(); }
                        break;
                }
            }

            for (Peleador i : winners) {
                if(i.isInvocation() && i.getMob() != null && i.getMob().getTemplate().getId() != 285)
                    continue;
                if(i.isDouble())
                    continue;

                final Jugador player = i.getPlayer();

                if (player != null && getType() != Constantes.FIGHT_TYPE_CHALLENGE)
                    player.calculTurnCandy();
                if (getType() == Constantes.FIGHT_TYPE_PVT || getType() == Constantes.FIGHT_TYPE_PVM || getType() == Constantes.FIGHT_TYPE_CHALLENGE || getType() == Constantes.FIGHT_TYPE_DOPEUL) {
                    StringBuilder drops = new StringBuilder();
                    long xpPlayer = 0, xpGuild = 0, xpMount = 0;
                    int winKamas;

                    AtomicReference<Long> XP = new AtomicReference<>();
                    if (player != null) {
                        xpPlayer = FormulaOficial.getXp(i, winners, totalXP, nbbonus, (getMobGroup() != null ? getMobGroup().getStarBonus() : 0), challXp, lvlMax, lvlMin, lvlLoosers, lvlWinners);
                        XP.set(xpPlayer);

                        if (this.getType() == Constantes.FIGHT_TYPE_PVT && win == 1) {
                            if (player != null && memberGuild != 0)
                                if (player.getGuildMember() != null)
                                    xpGuild = (int) Math.floor(this.getCollector().getXp() / memberGuild);
                        } else
                            xpGuild = Formulas.getGuildXpWin(i, XP);

                        if (player.isOnMount()) {
                            xpMount = Formulas.getMountXpWin(i, XP);
                            player.getMount().addXp(xpMount);
                            GestorSalida.GAME_SEND_Re_PACKET(player, "+", player.getMount());
                        }
                    }


                    winKamas = (int) ((this.getType() == Constantes.FIGHT_TYPE_PVT && win == 1) ?
                            Math.floor(kamas.getPrimero() / winners.size()) : Formulas.getKamasWin(i, winners, kamas.getPrimero(),kamas.getSegundo()));
                    Map<Integer, Integer> objectsWon = new HashMap<>(), itemWon2 = new HashMap<>();
                    if (this.getType() == Constantes.FIGHT_TYPE_PVT && win == 1 && dropsCollector != null) {
                        int objectPerPlayer = (int) Math.floor(dropsCollector.size() / winners.size()), counter = 0;
                        ArrayList<ObjetoJuego> temporary2 = new ArrayList<>(dropsCollector);
                        Collections.shuffle(temporary2);

                        for (ObjetoJuego object : temporary2) {
                            if (counter <= objectPerPlayer) {
                                objectsWon.put(object.getModelo().getId(), object.getCantidad());
                                dropsCollector.remove(object);
                                Mundo.mundo.removeGameObject(object.getId());
                                counter++;
                            }
                        }
                    } else {
                        ArrayList<Drop> temporary3 = new ArrayList<>(dropsPlayers);
                        temporary3.addAll(Mundo.mundo.getEtherealWeapons(i.isInvocation() ? i.getInvocator().getLvl() : i.getLvl()).stream().map(objectTemplate ->
                              new Drop(objectTemplate.getId(), 0.001, 0)).collect(Collectors.toList()));
                        Collections.shuffle(temporary3);

                        for (Drop drop : temporary3) {
                            double prospecting = i.getPros() / 100.0;
                            if (prospecting < 1) prospecting = 1;


                            final double jet = Double.parseDouble(formatter.format(Math.random() * 100).replace(',', '.')),
                                    chance = Double.parseDouble(formatter.format(drop.getLocalPercent() * prospecting * Mundo.mundo.getConquestBonus(player) * challengeFactor * starFactor * Configuracion.INSTANCE.getRATE_DROP()).replace(',', '.'));
                            boolean ok = false;

                            if (drop.getAction() == 4) {
                                if (player != null && Mundo.mundo.getConditionManager().validConditions(player, "QE=" + drop.getCondition()))
                                    ok = true;
                            }
                            if (jet < chance || ok) {
                                ObjetoModelo objectTemplate = Mundo.mundo.getObjetoModelo(drop.getObjectId());

                                if (objectTemplate == null)
                                    continue;

                                quantity = 1;
                                boolean itsOk = false, unique = false;
                                switch (drop.getAction()) {
                                    case -2:
                                        unique = true;
                                        itsOk = true;
                                        break;
                                    case -1:// All items without condition.
                                        itsOk = true;
                                        break;

                                    case 1:// Is meat so..
                                        break;

                                    case 2:// Verification of the condition (MAP)
                                        for (String id : drop.getCondition().split(","))
                                            if (id.equals(String.valueOf(getMap().getId()))) {
                                                itsOk = true;
                                                break;
                                            }
                                        break;

                                    case 3:// Alignement
                                        if (this.getMapOld().getSubArea() == null)
                                            break;
                                        switch (drop.getCondition()) {
                                            case "0":
                                            case "2":
                                                if (this.getMapOld().getSubArea().getAlignement() == 2)
                                                    itsOk = true;
                                                break;
                                            case "1":
                                                if (this.getMapOld().getSubArea().getAlignement() == 1)
                                                    itsOk = true;
                                                break;
                                            case "3":
                                                if (this.getMapOld().getSubArea().getAlignement() == 3)
                                                    itsOk = true;
                                                break;
                                            default:
                                                itsOk = true;
                                                break;
                                        }
                                        break;

                                    case 4: // Quete
                                        if (Mundo.mundo.getConditionManager().validConditions(player, "QE=" + drop.getCondition()))
                                            itsOk = true;
                                        break;

                                    case 5: // Dropable une seule fois
                                        if (player == null) break;
                                        if (player.getNbItemTemplate(objectTemplate.getId()) > 0) break;
                                        itsOk = true;
                                        break;

                                    case 6: // Avoir l'objet
                                        if (player == null) break;
                                        int item = Integer.parseInt(drop.getCondition());
                                        if (item == 2039) {
                                            if (this.getMap().getId() == (short) 7388) {
                                                if (player.hasItemTemplate(item, 1))
                                                    itsOk = true;
                                            } else
                                                itsOk = false;
                                        } else if (player.hasItemTemplate(item, 1))
                                            itsOk = true;
                                        break;

                                    case 7:// Verification of the condition (MAP) mais pas plusieurs fois
                                        if (player == null) break;
                                        if (player.hasItemTemplate(objectTemplate.getId(), 1))
                                            break;
                                        for (String id : drop.getCondition().split(",")) {
                                            if (id.equals(String.valueOf(this.getMap().getId()))) {
                                                itsOk = true;
                                                break;
                                            }
                                        }
                                        break;

                                    case 8:// Win a specific quantity
                                        String[] split = drop.getCondition().split(",");
                                        quantity = Formulas.getRandomValue(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                                        itsOk = true;
                                        break;

                                    case 9:// Relique minotoror
                                        if (player != null && Minotot.isValidMap(player.getCurMap()))
                                            itsOk = true;
                                        break;

                                    case 999:// Drop for collector
                                        itsOk = true;
                                        break;

                                    default:
                                        itsOk = true;
                                        break;
                                }
                                if (itsOk) {
                                    objectsWon.put(objectTemplate.getId(), (objectsWon.get(objectTemplate.getId()) == null ? quantity : (objectsWon.get(objectTemplate.getId())) + quantity));
                                    if (unique) dropsPlayers.remove(drop);
                                }
                            }
                        }
                        if (player != null) {
                            ArrayList<Drop> temporary = new ArrayList<>(dropsMeats);
                            Collections.shuffle(temporary);

                            ObjetoJuego weapon = player.getObjetByPos(Constantes.ITEM_POS_ARME);
                            boolean ok = weapon != null && weapon.getCaracteristicas().getEffect(795) == 1;

                            if(ok) {
                                for (Drop drop : temporary) {
                                    final double jet = Double.parseDouble(formatter.format(Math.random() * 100).replace(',', '.')),
                                            chance = Double.parseDouble(formatter.format(drop.getLocalPercent() * (i.getPros() / 100.0)).replace(',', '.'));

                                    if (jet < chance) {
                                        ObjetoModelo objectTemplate = Mundo.mundo.getObjetoModelo(drop.getObjectId());

                                        if (drop.getAction() == 1 && objectTemplate != null && player.getMetierByID(41) != null && player.getMetierByID(41).get_lvl() >= drop.getLevel())
                                            itemWon2.put(objectTemplate.getId(), (itemWon2.get(objectTemplate.getId()) == null ? 0 : itemWon2.get(objectTemplate.getId())) + 1);
                                    }
                                }
                            }
                        }
                    }
                    if (player != null || (i.getMob() != null && i.getMob().getTemplate().getId() == 285)) {
                        if (player != null) {
                            if (this.getTrainerWinner() != -1 && i.getId() == this.getTrainerWinner() && player.getMount() == null) {
                                int color = Formulas.getCouleur(amande, rousse, doree);

                                Montura mount = new Montura(color, i.getId(), true);
                                player.setMount(mount);
                                GestorSalida.GAME_SEND_Re_PACKET(player, "+", mount);
                                GestorSalida.GAME_SEND_Rx_PACKET(player);
                                GestorSalida.GAME_SEND_STATS_PACKET(player);
                                if (drops.length() > 0) drops.append(",");
                                switch (color) {
                                    case 20 -> drops.append("7807~1");
                                    case 10 -> drops.append("7809~1");
                                    case 18 -> drops.append("7864~1");
                                }
                            }
                            if (i.getId() == this.getCaptWinner() && this.getFullSoul() != null) {
                                if (drops.length() > 0)
                                    drops.append(",");
                                drops.append(this.getFullSoul().getModelo().getId()).append("~").append(1);
                                if (player.addObjet(this.getFullSoul(), false))
                                    Mundo.addGameObject(this.getFullSoul(), true);
                            }
                            if(list != null) {
                                String value = list.get(i.getPlayer());
                                if(value != null && !value.isEmpty())
                                    drops.append((drops.length() == 0) ? "" : ",").append(value);
                            }
                        }

                        for (Entry<Integer, Integer> entry : objectsWon.entrySet()) {
                            ObjetoModelo objectTemplate = Mundo.mundo.getObjetoModelo(entry.getKey());

                            if(player == null && i.getInvocator() == null) break;
                            if (objectTemplate == null || i.isDouble()) continue;
                            if (drops.length() > 0) drops.append(",");

                            drops.append(entry.getKey()).append("~").append(entry.getValue());

                            Jugador target = player != null ? player : i.getInvocator().getPlayer();

                            if (objectTemplate.getType() == 32 && player != null) {
                                player.setMascotte(entry.getKey());
                            } else {
                                ObjetoJuego newObj = Mundo.mundo.getObjetoModelo(objectTemplate.getId()).createNewItemWithoutDuplication(target.getItems().values(), entry.getValue(), false);
                                int guid = newObj.getId();//FIXME: Ne pas recrée un item pour l'empiler après

                                if (guid == -1) { // Don't exist
                                    guid = newObj.setId();
                                    target.getItems().put(guid, newObj);
                                    GestorSalida.GAME_SEND_OAKO_PACKET(target, newObj);
                                    Mundo.addGameObject(newObj, true);
                                } else {
                                    ObjetoJuego object = target.getItems().get(guid);

                                    if(object != null) {
                                        object.setCantidad(object.getCantidad() + entry.getValue());
                                        GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(target, object);
                                    }
                                }
                            }
                        }

                        for (Entry<Integer, Integer> entry : itemWon2.entrySet()) {
                            ObjetoModelo objectTemplate = Mundo.mundo.getObjetoModelo(entry.getKey());

                            if(player == null && i.getInvocator().getPlayer() == null) break;
                            if (objectTemplate == null) continue;
                            if (drops.length() > 0) drops.append(",");

                            drops.append(entry.getKey()).append("~").append(entry.getValue());

                            Jugador target = player != null ? player : i.getInvocator().getPlayer();
                            ObjetoJuego newObj = Mundo.mundo.getObjetoModelo(objectTemplate.getId()).createNewItemWithoutDuplication(target.getItems().values(), entry.getValue(), false);
                            int guid = newObj.getId();//FIXME: Ne pas recrée un item pour l'empiler après

                            if(guid == -1) { // Don't exist
                                guid = newObj.setId();
                                target.getItems().put(guid, newObj);
                                GestorSalida.GAME_SEND_OAKO_PACKET(target, newObj);
                                Mundo.addGameObject(newObj, true);
                            } else {
                                ObjetoJuego object = target.getItems().get(guid);
                                if(object != null) {
                                    object.setCantidad(object.getCantidad() + entry.getValue());
                                    GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(target, object);
                                }
                            }
                        }
                        if (this.getType() == Constantes.FIGHT_TYPE_DOPEUL) {
                            for (Peleador F : loosers) {
                                Monstruos.MobGrade mob = F.getMob();
                                Monstruos m = mob.getTemplate();
                                if (m == null)
                                    continue;
                                int IDmob = m.getId();
                                if (drops.length() > 0)
                                    drops.append(",");
                                drops.append(Constantes.getCertificatByDopeuls(IDmob)).append("~1");
                                // Certificat :
                                ObjetoModelo OT2 = Mundo.mundo.getObjetoModelo(Constantes.getCertificatByDopeuls(IDmob));
                                if(OT2 != null) {
                                    ObjetoJuego obj2 = OT2.createNewItem(1, false);
                                    if (player.addObjet(obj2, true))// Si le joueur n'avait pas d'item similaire
                                        Mundo.addGameObject(obj2, true);
                                    obj2.refreshStatsObjet("325#0#0#" + Instant.now().toEpochMilli());
                                    Database.dinamicos.getPlayerData().update(player);
                                    GestorSalida.GAME_SEND_Ow_PACKET(player);
                                }
                            }
                        }
                        if (this.getType() == Constantes.FIGHT_TYPE_PVM && player != null) {
                            int bouftou = 0, tofu = 0;

                            for (Monstruos.MobGrade mob : getMobGroup().getMobs().values()) {
                                switch (mob.getTemplate().getId()) {
                                    case 793:
                                        bouftou++;
                                        break;
                                    case 794:
                                        tofu++;
                                        break;
                                    case 289:
                                        if (player.getCurMap().getSubArea().getId() == 211)
                                            Monstruos.MobGroup.MAITRE_CORBAC.repop(player.getCurMap().getId());
                                        break;
                                }
                            }

                            if (Configuracion.INSTANCE.getHALLOWEEN()) {
                                if ((bouftou > 0 || tofu > 0) && !player.hasEquiped(976)) {
                                    if (bouftou > tofu) {
                                        drops.append(drops.length() > 0 ? "," : "").append("8169~1");
                                        player.setMalediction(8169);
                                        player.setFullMorph(Formulas.getRandomValue(16, 20), false, false);
                                    } else if (tofu > bouftou) {
                                        drops.append(drops.length() > 0 ? "," : "").append("8170~1");
                                        player.setMalediction(8170);
                                        player.setFullMorph(Formulas.getRandomValue(21, 25), false, false);
                                    } else {
                                        switch (Formulas.getRandomValue(1, 2)) {
                                            case 1 -> {
                                                drops.append(drops.length() > 0 ? "," : "").append("8169~1");
                                                player.setMalediction(8169);
                                                player.setFullMorph(Formulas.getRandomValue(16, 20), false, false);
                                            }
                                            case 2 -> {
                                                drops.append(drops.length() > 0 ? "," : "").append("8170~1");
                                                player.setMalediction(8170);
                                                player.setFullMorph(Formulas.getRandomValue(21, 25), false, false);
                                            }
                                        }
                                    }
                                }
                            }

                            if (player.getCurMap().getId() == 8984) {
                                ObjetoJuego obj = Mundo.mundo.getObjetoModelo(8012).createNewItem(1, false);
                                if (player.addObjet(obj, true))
                                    Mundo.addGameObject(obj, true);
                                drops.append(drops.length() > 0 ? "," : "").append("8012~1");
                            }
                        }
                        //Fin Drop

                        if (player != null) {
                            xpPlayer = XP.get();
                            if (xpPlayer != 0) {
                                if (player.getMorphMode()) {
                                    ObjetoJuego obj = player.getObjetByPos(Constantes.ITEM_POS_ARME);
                                    if (obj != null)
                                        if (Constantes.isIncarnationWeapon(obj.getModelo().getId()))
                                            if (player.addXpIncarnations(xpPlayer))
                                                i.setLevelUp(true);
                                } else if (player.addXp(xpPlayer))
                                    i.setLevelUp(true);
                            }

                            if (winKamas != 0)
                                player.addKamas(winKamas);
                            if (xpGuild > 0 && player.getGuildMember() != null)
                                player.getGuildMember().giveXpToGuild(xpGuild);
                        }
                        if (winKamas != 0 && i.isInvocation() && !i.isDouble() && i.getInvocator().getPlayer() != null)
                            i.getInvocator().getPlayer().addKamas(winKamas);
                    }

                    StringBuilder p = new StringBuilder();
                    p.append("2;");
                    p.append(i.getId()).append(";");
                    p.append(i.getPacketsName()).append(";");
                    p.append(i.getLvl()).append(";");
                    p.append((i.isDead() ? "1" : "0")).append(";");
                    p.append(i.xpString(";")).append(";");
                    p.append((xpPlayer == 0 ? "" : xpPlayer)).append(";");
                    p.append((xpGuild == 0 ? "" : xpGuild)).append(";");
                    p.append((xpMount == 0 ? "" : xpMount)).append(";");
                    p.append(drops).append(";");// Drop
                    p.append((winKamas == 0 ? "" : winKamas)).append("|");
                    gains.put(i.getId(), p);
                } else {
                    // Si c'est un neutre, on ne gagne pas de points
                    int winH = 0, winD = 0;

                    if (this.getType() == Constantes.FIGHT_TYPE_AGRESSION) {
                        if (i.isInvocation() || i.isPrisme() || i.isMob() || i.isDouble())
                            continue;

                        if (this.getType() == Constantes.FIGHT_TYPE_AGRESSION) {
                            if (getInit1().getPlayer().get_align() != 0 && getInit0().getPlayer().get_align() != 0) {
                                if (getInit1().getPlayer().getAccount().getCurrentIp().compareTo(getInit0().getPlayer().getAccount().getCurrentIp()) != 0)
                                    winH = Formulas.calculHonorWin(winners, loosers, i);
                                if (player.getDeshonor() > 0)
                                    winD = -1;
                            }
                        } else if (this.getType() == Constantes.FIGHT_TYPE_CONQUETE)
                            winH = Formulas.calculHonorWin(winners, loosers, i);

                        if (player.get_align() != 0) {
                            if (player.get_honor() + winH < 0)
                                winH = -player.get_honor();
                            player.addHonor(winH);
                            player.setDeshonor(player.getDeshonor() + winD);
                        }

                        int maxHonor = Mundo.mundo.getExpLevel(player.getGrade() + 1).pvp;
                        if (maxHonor == -1) maxHonor = Mundo.mundo.getExpLevel(player.getGrade()).pvp;

                        StringBuilder temporary = new StringBuilder();
                        temporary.append("2;").append(i.getId()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";").append((i.isDead() ? "1" : "0")).append(";");
                        temporary.append(player.get_align() != Constantes.ALINEAMIENTO_NEUTRAL ? Mundo.mundo.getExpLevel(player.getGrade()).pvp : 0).append(";");
                        temporary.append(player.get_honor()).append(";");
                        temporary.append(player.get_align() != Constantes.ALINEAMIENTO_NEUTRAL ? maxHonor : 0).append(";");
                        temporary.append(winH).append(";");
                        temporary.append(player.getGrade()).append(";");
                        temporary.append(player.getDeshonor()).append(";");
                        temporary.append(winD);
                        temporary.append(";");
                        temporary.append(stalk ? "10275~" + quantity : "");
                        if(Configuracion.INSTANCE.getHEROIC() && list != null) {
                            String value;
                            if((value = list.get(player)) != null)
                                if(!value.isEmpty())
                                    temporary.append(stalk ? "," : "").append(value);
                        }
                        temporary.append(";").append(Formulas.getRandomValue(kamas.getPrimero(),kamas.getSegundo())).append(";0;0;0;0|");
                        temporary.append(";;0;0;0;0;0|");
                        gains.put(i.getId(), temporary);
                    } else if (this.getType() == Constantes.FIGHT_TYPE_CONQUETE) {
                        if (player != null) {
                            if (player.get_honor() + winH < 0)
                                winH = -player.get_honor();
                            player.addHonor(winH);
                            if (player.getDeshonor() - winD < 0)
                                winD = 0;
                            player.setDeshonor(player.getDeshonor() - winD);
                            int maxHonor = Mundo.mundo.getExpLevel(player.getGrade() + 1).pvp;
                            if (maxHonor == -1)
                                maxHonor = Mundo.mundo.getExpLevel(player.getGrade()).pvp;

                            StringBuilder temporary = new StringBuilder();
                            temporary.append("2;").append(i.getId()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";").append((i.isDead() ? "1" : "0")).append(";");
                            temporary.append(player.get_align() != Constantes.ALINEAMIENTO_NEUTRAL ? Mundo.mundo.getExpLevel(player.getGrade()).pvp : 0).append(";");
                            temporary.append(player.get_honor()).append(";");
                            temporary.append(player.get_align() != Constantes.ALINEAMIENTO_NEUTRAL ? maxHonor : 0).append(";");
                            temporary.append(winH).append(";");
                            temporary.append(player.getGrade()).append(";");
                            temporary.append(player.getDeshonor()).append(";");
                            temporary.append(winD);
                            temporary.append(";;0;0;0;0;0|");
                            gains.put(i.getId(), temporary);
                        } else {
                            final Prisma prism = i.getPrism();
                            winH = winH * 5;
                            if (prism.getHonor() + winH < 0) winH = -prism.getHonor();
                            winH *= 3;
                            prism.addHonor(winH);

                            int maxHonor = Mundo.mundo.getExpLevel(prism.getLevel() + 1).pvp;
                            if (maxHonor == -1)
                                maxHonor = Mundo.mundo.getExpLevel(prism.getLevel()).pvp;

                            StringBuilder temporary = new StringBuilder();
                            temporary.append("2;").append(i.getId()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";").append((i.isDead() ? "1" : "0")).append(";");
                            temporary.append(Mundo.mundo.getExpLevel(prism.getLevel()).pvp).append(";");
                            temporary.append(prism.getHonor()).append(";");
                            temporary.append(maxHonor).append(";");
                            temporary.append(winH).append(";");
                            temporary.append(prism.getLevel()).append(";");
                            temporary.append("0;0;;0;0;0;0;0|");

                            gains.put(i.getId(), temporary);
                        }
                    }
                }
            }

            Collections.shuffle(winners);
            Map<Integer, Integer> invoks = new HashMap<>();

            winners.stream().filter(i -> i.isInvocation() && i.getMob() != null).filter(i -> i.getMob().getTemplate().getId() == 285).forEach(i -> invoks.put(i.getId(), i.getInvocator().getId()));

            if (invoks != null && invoks.size() > 0)
                for (Entry<Integer, Integer> entry : invoks.entrySet())
                    winners = this.deplace(winners, entry.getValue(), entry.getKey());

            winners.stream().filter(fighter -> !(fighter.isInvocation() && fighter.getMob() != null && fighter.getMob().getTemplate().getId() != 285)).filter(fighter -> !fighter.isDouble() && gains.get(fighter.getId()) != null).forEach(fighter -> packet.append(gains.get(fighter.getId()).toString()));

            for (Peleador i : loosers) {
                if(i.isInvocation() && i.getMob() != null && i.getMob().getTemplate().getId() != 285)
                    continue;
                if(i.isDouble())
                    continue;

                final Jugador player = i.getPlayer();

                if (player != null && this.getType() != Constantes.FIGHT_TYPE_CHALLENGE)
                    player.calculTurnCandy();
                if (this.getType() != Constantes.FIGHT_TYPE_AGRESSION && this.getType() != Constantes.FIGHT_TYPE_CONQUETE) {
                    StringBuilder temporary = new StringBuilder();
                    if (i.getPdv() == 0 || i.hasLeft() || i.isDead())
                        temporary.append("0;").append(i.getId()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";1").append(";").append(i.xpString(";")).append(";;;;|");
                    else
                        temporary.append("0;").append(i.getId()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";0").append(";").append(i.xpString(";")).append(";;;;|");
                    packet.append(temporary);
                } else {
                    // Si c'est un neutre, on ne gagne pas de points
                    int winH = 0;
                    int winD = 0;
                    if (this.getType() == Constantes.FIGHT_TYPE_AGRESSION) {
                        if (getInit1().getPlayer().get_align() != 0 && getInit0().getPlayer().get_align() != 0)
                            if (getInit1().getPlayer().getAccount().getCurrentIp().compareTo(getInit0().getPlayer().getAccount().getCurrentIp()) != 0)
                                winH = Formulas.calculHonorWin(winners, loosers, i);

                        if (player == null)
                            continue;
                        if (player.get_align() != 0) {
                            player.remHonor(player.get_honor() + winH < 0 ? -player.get_honor() : -winH);
                            if (player.getDeshonor() - winD < 0)
                                winD = 0;
                            player.setDeshonor(player.getDeshonor() - winD);
                        }

                        int maxHonor = Mundo.mundo.getExpLevel(player.getGrade() + 1).pvp;
                        if (maxHonor == -1)
                            maxHonor = Mundo.mundo.getExpLevel(player.getGrade()).pvp;

                        packet.append("0;").append(i.getId()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";").append((i.isDead() ? "1" : "0")).append(";");
                        packet.append(player.get_align() != Constantes.ALINEAMIENTO_NEUTRAL ? Mundo.mundo.getExpLevel(player.getGrade()).pvp : 0).append(";");
                        packet.append(player.get_honor()).append(";");
                        packet.append(player.get_align() != Constantes.ALINEAMIENTO_NEUTRAL ? maxHonor : 0).append(";");
                        packet.append(winH).append(";");
                        packet.append(player.getGrade()).append(";");
                        packet.append(player.getDeshonor()).append(";");
                        packet.append(winD);
                        packet.append(";;0;0;0;0;0|");
                    } else if (this.getType() == Constantes.FIGHT_TYPE_CONQUETE) {
                        winH = Formulas.calculHonorWin(winners, loosers, i);

                        if (player != null) {
                            winH = 0;
                            if (player.getDeshonor() - winD < 0)
                                winD = 0;
                            int maxHonor = Mundo.mundo.getExpLevel(player.getGrade() + 1).pvp;
                            if (maxHonor == -1)
                                maxHonor = Mundo.mundo.getExpLevel(player.getGrade()).pvp;

                            player.setDeshonor(player.getDeshonor() - winD);
                            packet.append("0;").append(i.getId()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";").append((i.isDead() ? "1" : "0")).append(";");
                            packet.append(player.get_align() != Constantes.ALINEAMIENTO_NEUTRAL ? Mundo.mundo.getExpLevel(player.getGrade()).pvp : 0).append(";");
                            packet.append(player.get_honor()).append(";");
                            packet.append(player.get_align() != Constantes.ALINEAMIENTO_NEUTRAL ? maxHonor : 0).append(";");
                            packet.append(winH).append(";");
                            packet.append(player.getGrade()).append(";");
                            packet.append(player.getDeshonor()).append(";");
                            packet.append(winD);
                            packet.append(";;0;0;0;0;0|");
                        } else {
                            Prisma prism = i.getPrism();

                            if (prism.getHonor() + winH < 0)
                                winH = -prism.getHonor();
                            int maxHonor = Mundo.mundo.getExpLevel(prism.getLevel() + 1).pvp;
                            if (maxHonor == -1)
                                maxHonor = Mundo.mundo.getExpLevel(prism.getLevel()).pvp;

                            prism.addHonor(winH);
                            packet.append("0;").append(i.getId()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";").append((i.isDead() ? "1" : "0")).append(";");
                            packet.append(Mundo.mundo.getExpLevel(prism.getLevel()).pvp).append(";");
                            packet.append(prism.getHonor()).append(";");
                            packet.append(maxHonor).append(";");
                            packet.append(winH).append(";");
                            packet.append(prism.getLevel()).append(";");
                            packet.append("0;0;;0;0;0;0;0|");
                        }
                    }
                }
            }
            if (Recaudador.getCollectorByMapId(getMap().getId()) != null && getType() == Constantes.FIGHT_TYPE_PVM) {
                Recaudador collector = Recaudador.getCollectorByMapId(getMap().getId());

                long winxp = FormulaOficial.getXp(collector, winners, totalXP, nbbonus, (getMobGroup() != null ? getMobGroup().getStarBonus() : 0), challXp, lvlMax, lvlMin, lvlLoosers, lvlWinners) / 10;
                long winkamas = (int) Math.floor(Formulas.getKamasWinPerco(kamas.getPrimero(),kamas.getSegundo()));

                collector.setXp(collector.getXp() + winxp);
                collector.setKamas(collector.getKamas() + winkamas);
                Gremio guild = Mundo.mundo.getGuild(collector.getGuildId());

                packet.append("5;").append(collector.getId()).append(";").append(collector.getFullName()).append(";").append(Mundo.mundo.getGuild(collector.getGuildId()).getLvl()).append(";0;");
                packet.append(guild.getLvl()).append(";");
                packet.append(guild.getXp()).append(";");
                packet.append(Mundo.mundo.getGuildXpMax(guild.getLvl())).append(";");
                packet.append(";");// XpGagner
                packet.append(winxp).append(";");// XpGuilde
                packet.append(";");// Monture

                StringBuilder drops = new StringBuilder();
                ArrayList<Drop> temporary = new ArrayList<>(dropsPlayers);
                Collections.shuffle(temporary);
                Map<Integer, Integer> objectsWon = new HashMap<>();

                if (collector.getPodsTotal() < collector.getMaxPod()) {
                    for (Drop drop : temporary) {
                        final double jet = Double.parseDouble(formatter.format(Math.random() * 100).replace(',', '.')),
                                chance = (int) (drop.getLocalPercent() * (Mundo.mundo.getGuild(collector.getGuildId()).getStats(Constantes.STATS_ADD_PROS) / 100.0));

                        if (jet < chance) {
                            ObjetoModelo objectTemplate = Mundo.mundo.getObjetoModelo(drop.getObjectId());

                            if (objectTemplate == null)
                                continue;

                            boolean itsOk = false, unique = false;
                            switch (drop.getAction()) {
                                case -2:
                                    unique = true;
                                    itsOk = true;
                                    break;
                                case -1:// All items without condition.
                                    itsOk = true;
                                    break;

                                case 1:// Is meat so..
                                    break;

                                case 2:// Verification of the condition ( MAP )
                                    for (String id : drop.getCondition().split(","))
                                        if (id.equals(getMap().getId() + "")) {
                                            itsOk = true;
                                            break;
                                        }
                                    break;

                                case 3:// Alignement
                                    if (this.getMapOld().getSubArea() == null)
                                        break;
                                    switch (drop.getCondition()) {
                                        case "0":
                                        case "2":
                                            if (this.getMapOld().getSubArea().getAlignement() == 2)
                                                itsOk = true;
                                            break;
                                        case "1":
                                            if (this.getMapOld().getSubArea().getAlignement() == 1)
                                                itsOk = true;
                                            break;
                                        case "3":
                                            if (this.getMapOld().getSubArea().getAlignement() == 3)
                                                itsOk = true;
                                            break;

                                        default:
                                            itsOk = true;
                                            break;
                                    }
                                    break;

                                case 4:
                                    if (objectTemplate.getId() == 2553)//Gros boulet
                                        itsOk = true;
                                    break;

                                case 5:
                                    itsOk = false;
                                    break;

                                case 6: // Les percepteurs ne font pas de qu�tes
                                case 7:
                                    break;

                                default:
                                    itsOk = true;
                                    break;
                            }

                            if (itsOk) {
                                objectsWon.put(objectTemplate.getId(), (objectsWon.get(objectTemplate.getId()) == null ? 0 : objectsWon.get(objectTemplate.getId())) + 1);

                                if (unique)
                                    dropsPlayers.remove(drop);
                            }
                        }
                    }

                    for (Entry<Integer, Integer> entry : objectsWon.entrySet()) {
                        ObjetoModelo objectTemplate = Mundo.mundo.getObjetoModelo(entry.getKey());

                        if (objectTemplate == null || collector.getPodsTotal() + objectTemplate.getPod() * entry.getValue() >= collector.getMaxPod()) continue;
                        if (drops.length() > 0) drops.append(",");

                        drops.append(entry.getKey()).append("~").append(entry.getValue());

                        ObjetoJuego newObj = Mundo.mundo.getObjetoModelo(objectTemplate.getId()).createNewItemWithoutDuplication(collector.getOjects().values(), entry.getValue(), false);
                        int guid = newObj.getId();//FIXME: Ne pas recrée un item pour l'empiler après

                        if (guid == -1) { // Don't exist
                            guid = newObj.setId();
                            collector.getOjects().put(guid, newObj);
                            Mundo.addGameObject(newObj, true);
                        } else {
                            newObj.setCantidad(newObj.getCantidad() + entry.getValue());
                        }
                    }
                }
                packet.append(drops).append(";");// Drop
                packet.append(winkamas).append("|");

                Database.estaticos.getCollectorData().update(collector);
            }
            return packet.toString();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred when server went to give the 'GE' packet : " + e.getMessage() + " " + e.getLocalizedMessage());
        }
        return "";
    }

    ArrayList<Peleador> deplace(ArrayList<Peleador> TEAM1,
                                Integer Invocator, Integer Invocation) {
        int k = 0;
        int p = 0;
        int j = 0;
        int s = TEAM1.size() - 1;
        boolean b = true;
        Peleador invok = null;
        for (Peleador i : TEAM1) {
            if (i.getId() == Invocation) {
                invok = i;
                b = false;
            }
            if (!b && invok != i) {
                TEAM1.set((k - 1), i);
            }
            k++;
        }
        TEAM1.set(s, invok);
        k = 0;
        b = true;
        for (Peleador i : TEAM1) {
            if (i.getId() == Invocator) {
                p = k;
                b = false;
            }
            if (!b && i.getId() != Invocator) {
                j++;
                if (k < s)
                    TEAM1.set((s - j + 1), TEAM1.get(s - j));
            }
            k++;
        }
        TEAM1.set(p + 1, invok);
        return TEAM1;
    }

    //String a StringBuilder
    public String getGTL() {
        StringBuilder packet = new StringBuilder();
        packet.append("GTL");
        if (this.orderPlaying != null)
            for (Peleador f : this.orderPlaying)
                if(!f.isDead())
                    packet.append('|').append(f.getId());
        return packet.toString() + (char) 0x00;
    }

    public String parseFightInfos() {
        StringBuilder infos = new StringBuilder();
        infos.append(getId()).append(";");
        long time = startTime + TimeZone.getDefault().getRawOffset();
        infos.append((getStartTime() == 0 ? "-1" : time)).append(";");
        // Team1
        infos.append("0,");// 0 car toujours joueur :)
        // Team2
        // Team2
        // Team2
        // Team2
        // Team2
        // Team2
        switch (getType()) {
            case Constantes.FIGHT_TYPE_CHALLENGE -> {
                infos.append("0,");
                infos.append(this.getTeamSizeWithoutInvocation(this.getTeam0().values())).append(";");
                infos.append("0,");
                infos.append("0,");
                infos.append(this.getTeamSizeWithoutInvocation(this.getTeam1().values())).append(";");
            }
            case Constantes.FIGHT_TYPE_AGRESSION -> {
                infos.append(getInit0().getPlayer().get_align()).append(",");
                infos.append(getTeam0().size()).append(";");
                infos.append("0,");
                infos.append(getInit1().getPlayer().get_align()).append(",");
                infos.append(this.getTeamSizeWithoutInvocation(this.getTeam1().values())).append(";");
            }
            case Constantes.FIGHT_TYPE_CONQUETE -> {
                infos.append(getInit0().getPlayer().get_align()).append(",");
                infos.append(this.getTeamSizeWithoutInvocation(this.getTeam0().values())).append(";");
                infos.append("0,");
                infos.append(getPrism().getAlignement()).append(",");
                infos.append(this.getTeamSizeWithoutInvocation(this.getTeam1().values())).append(";");
            }
            case Constantes.FIGHT_TYPE_PVM, Constantes.FIGHT_TYPE_DOPEUL -> {
                infos.append("0,");
                infos.append(this.getTeamSizeWithoutInvocation(this.getTeam0().values())).append(";");
                infos.append("1,");
                if (getTeam0().isEmpty())
                    infos.append("0,");
                else
                    infos.append(getTeam1().get(getTeam1().keySet().toArray()[0]).getMob().getTemplate().getAlign()).append(",");
                infos.append(this.getTeamSizeWithoutInvocation(this.getTeam1().values())).append(";");
            }
            case Constantes.FIGHT_TYPE_PVT -> {
                infos.append("0,");
                infos.append(this.getTeamSizeWithoutInvocation(this.getTeam0().values())).append(";");
                infos.append("3,");
                infos.append("0,");
                infos.append(this.getTeamSizeWithoutInvocation(this.getTeam1().values())).append(";");
            }
        }
        return infos.toString();
    }

    int getTeamSizeWithoutInvocation(Collection<Peleador> fighters) {
        int i = 0;
        for(Peleador fighter : fighters) if(!fighter.isInvocation()) i++;
        return i;
    }

    public Peleador getFighterByOrdreJeu() {
        if (this.orderPlaying == null)
            return null;
        if (this.curPlayer >= this.orderPlaying.size())
            this.curPlayer = this.orderPlaying.size() - 1;
        if (this.curPlayer < 0)
            this.curPlayer = 0;
        if (this.orderPlaying.size() <= 0)
            return null;
        Peleador current = null;
        try {
            current = this.orderPlaying.get(this.curPlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return current;
    }

    int getOrderPlayingSize() {
        if (this.orderPlaying == null)
            return 0;
        return Math.max(this.orderPlaying.size(), 0);
    }

    boolean haveFighterInOrdreJeu(Peleador f) {
        return this.orderPlaying != null && f != null && this.orderPlaying.contains(f);
    }

    public List<Peleador> getOrderPlaying() {
        return this.orderPlaying;
    }

    public void cast(Peleador fighter, Runnable runnable) {
        if(this.turn != null && Instant.now().toEpochMilli() - this.turn.getStartTime() >= 30000) return;
        GestorSalida.GAME_SEND_GAS_PACKET_TO_FIGHT(this, 7, fighter.getId());
        try { runnable.run(); } catch(Exception e) { e.printStackTrace(); }
        GestorSalida.GAME_SEND_GAF_PACKET_TO_FIGHT(this, 7, 0, fighter.getId());
    }

    public static Map<Jugador, String> give(ArrayList<ObjetoJuego> objects, ArrayList<Peleador> winners) {
        final Map<Jugador, String> list = new HashMap<>();

        if(Configuracion.INSTANCE.getHEROIC()) {
            final ArrayList<Jugador> players = new ArrayList<>();

            new ArrayList<>(winners).stream().filter(Objects::nonNull).forEach(fighter -> {
                final Jugador player = fighter.getPlayer();

                if (player != null) {
                    players.add(player);
                    list.put(player, "");
                }
            });

            if (players.size() > 0 && objects != null && !objects.isEmpty()) {
                byte count = -1;
                ObjetoJuego object;

                Iterator<ObjetoJuego> iterator = objects.iterator();
                while (iterator.hasNext()) {
                    object = objects.iterator().next();

                    if (object == null) {
                        iterator.remove();
                        continue;
                    }

                    count++;
                    final Jugador player = players.get(count);

                    if (player != null) {
                        object.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                        player.addObjet(object, true);
                        String value = list.get(player);
                        value += (value.isEmpty() ? "" : ",") + object.getModelo().getId() + "~" + object.getCantidad();
                        list.remove(player);
                        list.put(player, value);
                        objects.remove(object);
                    }
                    if (count >= players.size() - 1)
                        count = -1;

                }
            }
        }
        return list;
    }

    public class Turno implements Runnable {

        private final Pelea pelea;
        private final Peleador peleador;
        private final long inicio;
        private boolean detener = false;

        public Turno(Pelea pelea, Peleador peleador) {
            this.pelea = pelea;
            this.peleador = peleador;
            Temporizador.addSiguiente(this, Constantes.TIEMPO_DE_TURNO + 2000, TimeUnit.MILLISECONDS, Temporizador.DataType.PELEA);
            this.inicio = Instant.now().toEpochMilli();
        }

        public long getStartTime() {
            return inicio;
        }

        public void stop() {
            this.detener = true;
        }

        @Override
        public void run() {
            if (this.detener || this.peleador.isDead()) {
                this.stop();
                return;
            }

            if (this.pelea.getOrderPlaying() == null) {
                this.stop();
                return;
            }

            if (this.pelea.getOrderPlaying().get(this.pelea.getCurPlayer()) == null) {
                this.stop();
                return;
            }

            if (this.pelea.getOrderPlaying().get(this.pelea.getCurPlayer()) != this.peleador) {
                this.stop();
                return;
            }
            this.pelea.endTurn(false);
        }
    }
}