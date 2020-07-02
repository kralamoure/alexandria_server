package org.alexandria.estaticos.cliente;

import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;
import org.alexandria.estaticos.area.mapa.Mapa.ObjetosInteractivos;
import org.alexandria.estaticos.Mascota.MascotaEntrada;
import org.alexandria.estaticos.Casas;
import org.alexandria.estaticos.Cercados;
import org.alexandria.estaticos.area.mapa.laberintos.DragoCerdo;
import org.alexandria.estaticos.area.mapa.laberintos.Minotot;
import org.alexandria.estaticos.cliente.otros.Restricciones;
import org.alexandria.estaticos.cliente.otros.Stalk;
import org.alexandria.estaticos.comandos.administracion.GrupoADM;
import org.alexandria.comunes.Camino;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.configuracion.Constantes;
import org.alexandria.configuracion.MainServidor;
import org.alexandria.configuracion.Reinicio;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.dinamicos.Inicio;
import org.alexandria.estaticos.Prisma;
import org.alexandria.estaticos.Recaudador;
import org.alexandria.estaticos.Mascota;
import org.alexandria.estaticos.Monstruos;
import org.alexandria.estaticos.Montura;
import org.alexandria.estaticos.evento.GestorEvento;
import org.alexandria.estaticos.Gremio;
import org.alexandria.estaticos.Gremio.GremioMiembros;
import org.alexandria.estaticos.juego.JuegoCliente;
import org.alexandria.estaticos.juego.JuegoServidor;
import org.alexandria.estaticos.juego.accion.AccionIntercambiar;
import org.alexandria.estaticos.juego.accion.AccionJuego;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.Mision;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.objeto.ObjetoModelo;
import org.alexandria.estaticos.objeto.ObjetoSet;
import org.alexandria.estaticos.oficio.Oficio;
import org.alexandria.estaticos.oficio.OficioAccion;
import org.alexandria.estaticos.oficio.OficioCaracteristicas;
import org.alexandria.estaticos.oficio.OficioConstantes;
import org.alexandria.otro.Accion;
import org.alexandria.otro.Dopeul;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.arena.DeathMatch;
import org.alexandria.estaticos.pelea.arena.TeamMatch;
import org.alexandria.estaticos.pelea.hechizo.EfectoHechizo;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.otro.utilidad.Doble;
import org.alexandria.estaticos.Mision.*;
import org.alexandria.otro.utilidad.Temporizador;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class Jugador {


    //Peleas especiales
    public TeamMatch koliseo;
    public DeathMatch deathMatch;


    public final Restricciones restricciones;
    public Caracteristicas caracteristicas;
    public boolean isNew = false;
    //Job
    //Disponibilit�
    public boolean _isAbsent = false;
    public boolean _isInvisible = false;
    //Double
    public boolean _isClone = false;
    //Suiveur - Suivi
    public Map<Integer, Jugador> follower = new HashMap<>();
    public Jugador follow = null;
    //Prison Alignement :
    public boolean isInEnnemyFaction;
    public long enteredOnEnnemyFaction;
    public boolean donjon;
    //Commande h�h�
    public int thatMap = -1;
    public int thatCell = -1;
    public boolean walkFast = false;
    public boolean getCases = false;
    public ArrayList<Integer> thisCases = new ArrayList<>();
    public boolean mpToTp = false;
    public boolean noall = false;
    private final int id;
    private String name;
    private int sexe;
    private int classe;
    private final int color1;
    private final int color2;
    private final int color3;
    private int level;
    private int energy;
    private long exp;
    private int curPdv;
    private int maxPdv;
    private final Caracteristicas statsParcho = new Caracteristicas();
    private long kamas;
    private int _spellPts;
    private int _capital;
    private int _size;
    private int gfxId;
    private int _orientation = 1;
    private Cuenta account;
    //PDV
    private int _accID;
    private boolean canAggro = true;
    //Emote
    private final List<Integer> emotes = new ArrayList<>();
    //Variables d'ali
    private byte _align = 0;
    private int _deshonor = 0;
    private int _honor = 0;
    private boolean _showWings = false;
    private int _aLvl = 0;
    private GremioMiembros _guildMember;
    private boolean _showFriendConnection;
    private String _canaux;
    private Pelea pelea;
    private boolean away;
    private Mapa curMap;
    private GameCase curCell;
    private boolean ready = false;
    private boolean isOnline = false;
    private Grupo party;
    private int duelId = -1;
    private final Map<Integer, EfectoHechizo> buffs = new HashMap<>();
    private Map<Integer, ObjetoJuego> objects = new HashMap<>();
    private String _savePos;
    private int _emoteActive = 0;
    private int savestat;
    private Casas _curHouse;
    //Invitation
    private int _inviting = 0;
    private final ArrayList<Integer> craftingType = new ArrayList<>();
    private final Map<Integer, OficioCaracteristicas> _metiers = new HashMap<>();
    //Enclos

    //Monture
    private Montura _mount;
    private int _mountXpGive = 0;
    private boolean _onMount = false;
    //Zaap
    private final ArrayList<Short> _zaaps = new ArrayList<>();
    //Sort
    private Map<Integer, Hechizo.SortStats> _sorts = new HashMap<>();
    private Map<Integer, Character> _sortsPlaces = new HashMap<>();
    //Titre
    private byte _title = 0;
    //Mariage
    private int wife = 0;
    private int _isOK = 0;
    //Fantome
    private boolean isGhost = false;
    private int _Speed = 0;
    //Marchand
    private boolean _seeSeller = false;
    private final Map<Integer, Integer> _storeItems = new HashMap<>();                    //<ObjID, Prix>
    //Metier
    private boolean _metierPublic = false;
    private boolean _livreArti = false;

    //Fight end
    private int hasEndFight = -1;
    private Accion endFightAction;
    private Monstruos.MobGroup hasMobGroup = null;
    //Item classe
    private final ArrayList<Integer> objectsClass = new ArrayList<>();
    private final Map<Integer, Doble<Integer, Integer>> objectsClassSpell = new HashMap<>();
    // Taverne
    private long timeTaverne = 0;
    //GA
    private AccionJuego _gameAction = null;
    //Name
    //Fight :
    private boolean _spec;
    //Traque
    private Stalk _traqued;
    private boolean doAction;
    //FullMorph Stats
    private boolean _morphMode = false;
    private int _morphId;
    private final Map<Integer, Hechizo.SortStats> _saveSorts = new HashMap<>();
    private final Map<Integer, Character> _saveSortsPlaces = new HashMap<>();
    private int _saveSpellPts;
    private int pa = 0, pm = 0, vitalite = 0, sagesse = 0, terre = 0, feu = 0, eau = 0, air = 0, initiative = 0;
    private boolean useStats = false;
    private boolean useCac = true;
    // Other ?
    private short oldMap = 0;
    private int oldCell = 0;
    private String _allTitle = "";
    private boolean isBlocked = false;
    private int action = -1;
    //Regen hp
    private boolean sitted;
    private int regenRate = 2000;
    private long regenTime = -1;                                                //-1 veut dire que la personne ne c'est jamais connecte
    private boolean isInPrivateArea = false;
    public Inicio start;
    private GrupoADM groupe;
    private boolean isInvisible = false;
    //Especiales
    private boolean _comandoPasarTurno;
    private Doble<ObjetosInteractivos, GameCase> inObjetoInteractivo =null;

    private final Map<Integer, MisionJugador> questList = new HashMap<>();
    private boolean changeName;
    public boolean afterFight = false;

    //Inactividad
    protected long lastPacketTime;

    public boolean getComandoPasarTurno() {
        return _comandoPasarTurno;
    }

    public ArrayList<Integer> getIsCraftingType() {
        return craftingType;
    }

    public Jugador(int id, String name, int groupe, int sexe, int classe,
                   int color1, int color2, int color3, long kamas, int pts,
                   int _capital, int energy, int level, long exp, int _size,
                   int _gfxid, byte alignement, int account,
                   Map<Integer, Integer> stats, byte seeFriend,
                   byte seeAlign, byte seeSeller, String canaux, short map, int cell,
                   String stuff, String storeObjets, int pdvPer, String spells,
                   String savePos, String jobs, int mountXp, int mount, int honor,
                   int deshonor, int alvl, String z, byte title, int wifeGuid,
                   String morphMode, String allTitle, String emotes, long prison,
                   boolean isNew, String parcho, long timeDeblo, boolean noall, String deadInformation, byte deathCount, long totalKills) {
        this.id = id;
        this.noall = noall;
        this.name = name;
        this.groupe = GrupoADM.getGrupoID(groupe);
        this.sexe = sexe;
        this.classe = classe;
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
        this.kamas = kamas;
        this._capital = _capital;
        this._align = alignement;
        this._honor = honor;
        this._deshonor = deshonor;
        this._aLvl = alvl;
        this.energy = energy;
        this.level = level;
        this.exp = exp;
        if (mount != -1)
            this._mount = Mundo.mundo.getMountById(mount);
        this._size = _size;
        this.gfxId = _gfxid;
        this._mountXpGive = mountXp;
        this.caracteristicas = new Caracteristicas(stats, true, this);
        this._accID = account;
        this.account = Mundo.mundo.getAccount(account);
        this._showFriendConnection = seeFriend == 1;
        this.wife = wifeGuid;
        this._metierPublic = false;
        this._title = title;
        this.changeName = false;
        this._allTitle = allTitle;
        this._seeSeller = seeSeller == 1;
        savestat = 0;
        this._canaux = canaux;
        this.curMap = Mundo.mundo.getMap(map);
        this._savePos = savePos;
        this.isNew = isNew;
        this.regenTime = Instant.now().toEpochMilli();
        Database.dinamicos.getQuestPlayerData().loadPerso(this);
        this.restricciones = Restricciones.get(this.id);
        this.timeTaverne = timeDeblo;
        try {
            String[] split = deadInformation.split(",");
            this.dead = Byte.parseByte(split[0]);
            this.deadTime = Long.parseLong(split[1]);
            this.deadType = Byte.parseByte(split[2]);
            this.killByTypeId = Long.parseLong(split[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.totalKills = totalKills;
        this.deathCount = deathCount;
        try {
            if (!emotes.isEmpty())
                for (String i : emotes.split(";"))
                    this.addStaticEmote(Integer.parseInt(i));
            if (!morphMode.equals("")) {
                if (morphMode.equals("0"))
                    morphMode = "0;0";
                String[] i = morphMode.split(";");
                _morphMode = i[0].equals("1");
                if (!i[1].equals(""))
                    _morphId = Integer.parseInt(i[1]);
            }
            if (_morphMode)
                this._saveSpellPts = pts;
            else
                this._spellPts = pts;
            if (prison != 0) {
                this.isInEnnemyFaction = true;
                this.enteredOnEnnemyFaction = prison;
            }
            this._showWings = this.get_align() != 0 && seeAlign == 1;
            if (curMap == null && Mundo.mundo.getMap((short) 7411) != null) {
                this.curMap = Mundo.mundo.getMap((short) 7411);
                this.curCell = curMap.getCase(311);
            } else if (curMap == null && Mundo.mundo.getMap((short) 7411) == null) {
                JuegoServidor.a();
                MainServidor.INSTANCE.stop("Player1");
                return;
            } else if (curMap != null) {
                this.curCell = curMap.getCase(cell);
                if (curCell == null) {
                    this.curMap = Mundo.mundo.getMap((short) 7411);
                    this.curCell = curMap.getCase(311);
                }
            }
            if (!z.equalsIgnoreCase("")) {
                for (String str : z.split(",")) {
                    try {
                        _zaaps.add(Short.parseShort(str));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!isNew && (curMap == null || curCell == null)) {
                MainServidor.INSTANCE.stop("Player2");
                return;
            }
            if (!stuff.equals("")) {
                if (stuff.charAt(stuff.length() - 1) == '|')
                    stuff = stuff.substring(0, stuff.length() - 1);
                Database.dinamicos.getObjectData().load(stuff.replace("|", ","));
            }
            for (String item : stuff.split("\\|")) {
                if (item.equals(""))
                    continue;
                String[] infos = item.split(":");

                int guid = 0;
                try {
                    guid = Integer.parseInt(infos[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

                ObjetoJuego obj = Mundo.getGameObject(guid);

                if (obj == null)
                    continue;
                objects.put(obj.getId(), obj);
            }
            try {
                if (parcho != null && !parcho.equalsIgnoreCase(""))
                    for (String stat : parcho.split(";"))
                        if (!stat.equalsIgnoreCase(""))
                            this.statsParcho.addOneStat(Integer.parseInt(stat.split(",")[0]), Integer.parseInt(stat.split(",")[1]));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!storeObjets.equals("")) {
                for (String _storeObjets : storeObjets.split("\\|")) {
                    String[] infos = _storeObjets.split(",");
                    int guid = 0;
                    int price = 0;
                    try {
                        guid = Integer.parseInt(infos[0]);
                        price = Integer.parseInt(infos[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                    ObjetoJuego obj = Mundo.getGameObject(guid);
                    if (obj == null)
                        continue;

                    _storeItems.put(obj.getId(), price);
                }
            }
            this.maxPdv = (this.level - 1) * 5 + 55
                    + getTotalStats().getEffect(Constantes.STATS_ADD_VITA)
                    + getTotalStats().getEffect(Constantes.STATS_ADD_VIE);
            if (this.curPdv <= 0)
                this.curPdv = 1;
            if (pdvPer > 100)
                this.curPdv = (this.maxPdv * 100 / 100);
            else
                this.curPdv = (this.maxPdv * pdvPer / 100);
            if (this.curPdv <= 0)
                this.curPdv = 1;
            parseSpells(spells);
            //Chargement des m�tiers
            if (!jobs.equals("")) {
                for (String aJobData : jobs.split(";")) {
                    String[] infos = aJobData.split(",");
                    try {
                        int jobID = Integer.parseInt(infos[0]);
                        long xp = Long.parseLong(infos[1]);
                        Oficio m = Mundo.mundo.getMetier(jobID);
                        OficioCaracteristicas SM = _metiers.get(learnJob(m));
                        SM.addXp(this, xp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (this.energy == 0)
                setGhost();
            else if (this.energy == -1)
                setFuneral();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Clone double
    public Jugador(int id, String name, int groupe, int sexe, int classe,
                   int color1, int color2, int color3, int level, int _size,
                   int _gfxid, Map<Integer, Integer> stats, String stuff,
                   int pdvPer, byte seeAlign, int mount, int alvl, byte alignement) {
        this.id = id;
        this.name = name;
        this.groupe = GrupoADM.getGrupoID(groupe);
        this.sexe = sexe;
        this.classe = classe;
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
        this.level = level;
        this._aLvl = alvl;
        this._size = _size;
        this.gfxId = _gfxid;
        this.caracteristicas = new Caracteristicas(stats, true, this);
        this.changeName = false;
        this.restricciones = null;
        this.set_isClone(true);

        for(String item : stuff.split("\\|"))
        {
            if(item.equals(""))
                continue;
            String[] infos=item.split(":");
            int guid=Integer.parseInt(infos[0]);
            ObjetoJuego obj = Mundo.getGameObject(guid);
            if(obj==null)
                continue;
            objects.put(obj.getId(),obj);
        }
        this.maxPdv = (this.level - 1) * 5 + 50 + getCaracteristicas().getEffect(Constantes.STATS_ADD_VITA);
        this.curPdv = (this.maxPdv * pdvPer) / 100;
        this._align = alignement;
        this._showWings = this.get_align() != 0 && seeAlign == 1;
        if (mount != -1)
            this._mount = Mundo.mundo.getMountById(mount);
    }

    public static Jugador CREATE_PERSONNAGE(String name, int sexe, int classe,
                                            int color1, int color2, int color3, Cuenta compte) {
        StringBuilder z = new StringBuilder();
        if (Configuracion.INSTANCE.getALL_ZAAP()) {
            for (Entry<Integer, Integer> i : Constantes.ZAAPS.entrySet()) {
                if (z.length() != 0)
                    z.append(",");
                z.append(i.getKey());
            }
        }
        if (classe > 12 || classe < 1)
            return null;
        if (sexe < 0 || sexe > 1)
            return null;
        int startMap = Configuracion.INSTANCE.getSTART_MAP();
        int startCell = Configuracion.INSTANCE.getSTART_CELL();

        Jugador perso = new Jugador(Database.dinamicos.getPlayerData().getNextId(), name, -1, sexe, classe, color1, color2, color3, Configuracion.INSTANCE.getStartKamas(), ((Configuracion.INSTANCE.getStartLevel() - 1)), ((Configuracion.INSTANCE.getStartLevel() - 1) * 5), 10000, Configuracion.INSTANCE.getStartLevel(), Mundo.mundo.getPersoXpMin(Configuracion.INSTANCE.getStartLevel()), 100, Integer.parseInt(classe
                + "" + sexe), (byte) 0, compte.getId(), new HashMap<>(), (byte) 1, (byte) 0, (byte) 0, "*#%!pi$:?", (startMap != 0 ? (short) startMap : Constantes.getStartMap(classe)), (startCell != 0 ? (short) startCell : Constantes.getStartCell(classe)),
                //(short)6824,
                //224,
                "", "", 100, "", (startMap != 0 ? (short) startMap : Constantes.getStartMap(classe))
                + ","
                + (startCell != 0 ? (short) startCell : Constantes.getStartCell(classe)), "", 0, -1, 0, 0, 0, z.toString(), (byte) 0, 0, "0;0", "", Configuracion.INSTANCE.getALL_EMOTE() ? "0;1;2;3;4;5;6;7;8;9;10;11;12;13;14;15;16;17;18;19;20;21" : "0", 0, true, "118,0;119,0;123,0;124,0;125,0;126,0", 0, false, "0,0,0,0", (byte) 0, 0);
        perso.emotes.add(1);
        perso._sorts = Constantes.getStartSorts(classe);
        for (int a = 1; a <= perso.getLevel(); a++)
            Constantes.onLevelUpSpells(perso, a);
        perso._sortsPlaces = Constantes.getStartSortsPlaces(classe);

        GestorSalida.GAME_SEND_WELCOME(perso);

        if (!Database.dinamicos.getPlayerData().add(perso))
            return null;
        Mundo.mundo.addPlayer(perso);
        if (Configuracion.INSTANCE.getSERVER_KEY().equals("jiva")) {
            for (ObjetoModelo t : Mundo.mundo.getItemSet(5).getItemTemplates()) {
                ObjetoJuego obj = t.createNewItem(1, true);
                if (perso.addObjet(obj, true))
                    Mundo.addGameObject(obj, true);
            }
            ObjetoModelo template = Mundo.mundo.getObjetoModelo(10207);
            if (template != null) {
                ObjetoJuego object = template.createNewItem(1, true);
                if (object != null) {
                    object.getTxtStat().clear();
                    object.getTxtStat().putAll(Dopeul.generateStatsTrousseau());
                    if (perso.addObjet(object, true))
                        Mundo.addGameObject(object, true);
                }
            }
        }
        return perso;
    }

    public static String getCompiledEmote(List<Integer> i) {
        int i2 = 0;
        for (Integer b : i) i2 += (2 << (b - 2));
        return i2 + "|0";
    }

    //CLONAGE
    public static Jugador ClonePerso(Jugador P, int id, int pdv) {
        HashMap<Integer, Integer> stats = new HashMap<>();
        stats.put(Constantes.STATS_ADD_VITA, pdv);
        stats.put(Constantes.STATS_ADD_FORC, P.getCaracteristicas().getEffect(Constantes.STATS_ADD_FORC));
        stats.put(Constantes.STATS_ADD_SAGE, P.getCaracteristicas().getEffect(Constantes.STATS_ADD_SAGE));
        stats.put(Constantes.STATS_ADD_INTE, P.getCaracteristicas().getEffect(Constantes.STATS_ADD_INTE));
        stats.put(Constantes.STATS_ADD_CHAN, P.getCaracteristicas().getEffect(Constantes.STATS_ADD_CHAN));
        stats.put(Constantes.STATS_ADD_AGIL, P.getCaracteristicas().getEffect(Constantes.STATS_ADD_AGIL));
        stats.put(Constantes.STATS_ADD_PA, P.getCaracteristicas().getEffect(Constantes.STATS_ADD_PA));
        stats.put(Constantes.STATS_ADD_PM, P.getCaracteristicas().getEffect(Constantes.STATS_ADD_PM));
        stats.put(Constantes.STATS_ADD_RP_NEU, P.getCaracteristicas().getEffect(Constantes.STATS_ADD_RP_NEU));
        stats.put(Constantes.STATS_ADD_RP_TER, P.getCaracteristicas().getEffect(Constantes.STATS_ADD_RP_TER));
        stats.put(Constantes.STATS_ADD_RP_FEU, P.getCaracteristicas().getEffect(Constantes.STATS_ADD_RP_FEU));
        stats.put(Constantes.STATS_ADD_RP_EAU, P.getCaracteristicas().getEffect(Constantes.STATS_ADD_RP_EAU));
        stats.put(Constantes.STATS_ADD_RP_AIR, P.getCaracteristicas().getEffect(Constantes.STATS_ADD_RP_AIR));
        stats.put(Constantes.STATS_ADD_AFLEE, P.getCaracteristicas().getEffect(Constantes.STATS_ADD_AFLEE));
        stats.put(Constantes.STATS_ADD_MFLEE, P.getCaracteristicas().getEffect(Constantes.STATS_ADD_MFLEE));

        byte showWings = 0;
        int alvl = 0;
        if (P.get_align() != 0 && P._showWings) {
            showWings = 1;
            alvl = P.getGrade();
        }
        int mountID = -1;
        if (P.getMount() != null) {
            mountID = P.getMount().getId();
        }

        Jugador Clone = new Jugador(id, P.getName(), (P.getGroupe() != null) ? P.getGroupe().getId() : -1, P.getSexe(), P.getClasse(), P.getColor1(), P.getColor2(), P.getColor3(), P.getLevel(), 100, P.getGfxId(), stats, "", 100, showWings, mountID, alvl, P.get_align());
        Clone.objects = new HashMap<>();
        Clone.objects.putAll(P.objects);
        Clone.set_isClone(true);
        if (P._onMount) {
            Clone._onMount = true;
        }
        return Clone;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        this.changeName = false;

        Database.dinamicos.getPlayerData().updateInfos(this);
        if (this.getGuildMember() != null)
            Database.estaticos.getGuildMemberData().update(this);
    }

    public GrupoADM getGroupe() {
        return this.groupe;
    }

    public void setGroupe(GrupoADM groupe, boolean reload) {
        this.groupe = groupe;
        if (reload)
            Database.dinamicos.getPlayerData().updateGroupe(this);
    }

    public boolean isInvisible() {
        return this.isInvisible;
    }

    public void setInvisible(boolean b) {
        this.isInvisible = b;
    }

    public int getSexe() {
        return this.sexe;
    }

    public void setSexe(int sexe) {
        this.sexe = sexe;
        this.setGfxId(10 * this.getClasse() + this.sexe);
    }

    public int getClasse() {
        return this.classe;
    }

    public void setClasse(int classe) {
        this.classe = classe;
    }

    public int getColor1() {
        return this.color1;
    }

    public int getColor2() {
        return this.color2;
    }

    public int getColor3() {
        return this.color3;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getEnergy() {
        return this.energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public long getExp() {
        return this.exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public int getCurPdv() {
        refreshLife(false);
        return this.curPdv;
    }

    public void setPdv(int pdv) {
        this.curPdv = pdv;
        if (this.curPdv >= this.maxPdv)
            this.curPdv = this.maxPdv;
        if (party != null)
            GestorSalida.GAME_SEND_PM_MOD_PACKET_TO_GROUP(party, this);
    }

    public int getMaxPdv() {
        return this.maxPdv;
    }

    public void setMaxPdv(int maxPdv) {
        this.maxPdv = maxPdv;
        GestorSalida.GAME_SEND_STATS_PACKET(this);
        if (party != null)
            GestorSalida.GAME_SEND_PM_MOD_PACKET_TO_GROUP(party, this);
    }

    public Caracteristicas getCaracteristicas() {
        if (useStats)
            return newStatsMorph();
        else
            return this.caracteristicas;
    }

    public Caracteristicas getStatsParcho() {
        return statsParcho;
    }

    public String parseStatsParcho() {
        StringBuilder parcho = new StringBuilder();
        for (Entry<Integer, Integer> i : statsParcho.getEffects().entrySet())
            parcho.append((parcho.length() == 0) ? i.getKey() + "," + i.getValue() : ";" + i.getKey() + "," + i.getValue());
        return parcho.toString();
    }

    public boolean getDoAction() {
        return doAction;
    }

    public void setDoAction(boolean b) {
        doAction = b;
    }

    public void setRoleplayBuff(int id) {
        int objTemplate = switch (id) {
            case 10673 -> 10844;
            case 10669 -> 10681;
            default -> 0;
        };
        if (objTemplate == 0)
            return;
        if (getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF) != null) {
            int guid = getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF).getId();
            GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            this.deleteItem(guid);
        }

        ObjetoJuego obj = Mundo.mundo.getObjetoModelo(objTemplate).createNewRoleplayBuff();
        this.addObjet(obj, false);
        Mundo.addGameObject(obj, true);
        GestorSalida.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
        GestorSalida.GAME_SEND_Ow_PACKET(this);
        GestorSalida.GAME_SEND_STATS_PACKET(this);
        Database.dinamicos.getPlayerData().update(this);
    }

    public void setBenediction(int id) {
        if (getObjetByPos(Constantes.ITEM_POS_BENEDICTION) != null) {
            int guid = getObjetByPos(Constantes.ITEM_POS_BENEDICTION).getId();
            GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            this.deleteItem(guid);
        }
        if (id == 0) {
            GestorSalida.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
            return;
        }
        int turn = 0;
        if (id == 10682) {
            turn = 20;
        } else {
            turn = 1;
        }

        ObjetoJuego obj = Mundo.mundo.getObjetoModelo(id).createNewBenediction(turn);
        this.addObjet(obj, false);
        Mundo.addGameObject(obj, true);
        GestorSalida.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
        GestorSalida.GAME_SEND_Ow_PACKET(this);
        GestorSalida.GAME_SEND_STATS_PACKET(this);
        Database.dinamicos.getPlayerData().update(this);
    }

    public void setMalediction(int id) {
        int objTemplate = 0;
        if (id == 10827) {
            objTemplate = 10838;
        } else {
            objTemplate = id;
        }
        if (objTemplate == 0) {
            GestorSalida.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
            return;
        }
        if (getObjetByPos(Constantes.ITEM_POS_MALEDICTION) != null) {
            int guid = getObjetByPos(Constantes.ITEM_POS_MALEDICTION).getId();
            GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            this.deleteItem(guid);
        }

        ObjetoJuego obj = Mundo.mundo.getObjetoModelo(objTemplate).createNewMalediction();
        this.addObjet(obj, false);
        Mundo.addGameObject(obj, true);
        if (this.getPelea() != null) {
            GestorSalida.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
            GestorSalida.GAME_SEND_Ow_PACKET(this);
            GestorSalida.GAME_SEND_STATS_PACKET(this);
            Database.dinamicos.getPlayerData().update(this);
        }
    }

    public void setMascotte(int id) {
        if (getObjetByPos(Constantes.ITEM_POS_PNJ_SUIVEUR) != null) {
            int guid = getObjetByPos(Constantes.ITEM_POS_PNJ_SUIVEUR).getId();
            GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            this.deleteItem(guid);
        }
        if (id == 0) {
            GestorSalida.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
            return;
        }

        ObjetoJuego obj = Mundo.mundo.getObjetoModelo(id).createNewFollowPnj(1);
        if (obj != null)
            if (this.addObjet(obj, false))
                Mundo.addGameObject(obj, true);

        GestorSalida.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
        GestorSalida.GAME_SEND_Ow_PACKET(this);
        GestorSalida.GAME_SEND_STATS_PACKET(this);
        Database.dinamicos.getPlayerData().update(this);
    }

    public void setCandy(int id) {
        if (getObjetByPos(Constantes.ITEM_POS_BONBON) != null) {
            int guid = getObjetByPos(Constantes.ITEM_POS_BONBON).getId();
            GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            this.deleteItem(guid);
        }
        int turn = switch (id) {
            case 8948, 8949, 8950, 8951, 8952, 8953, 8954, 8955 -> 5;
            case 10665 -> 20;
            default -> 30;
        };

        ObjetoJuego obj = Mundo.mundo.getObjetoModelo(id).createNewCandy(turn);
        this.addObjet(obj, false);
        Mundo.addGameObject(obj, true);
        GestorSalida.GAME_SEND_Ow_PACKET(this);
        GestorSalida.GAME_SEND_STATS_PACKET(this);
        Database.dinamicos.getPlayerData().update(this);
    }

    public void calculTurnCandy() {
        ObjetoJuego obj = getObjetByPos(Constantes.ITEM_POS_BONBON);
        if (obj != null) {
            obj.getCaracteristicas().addOneStat(Constantes.STATS_TURN, -1);
            if (obj.getCaracteristicas().getEffect(Constantes.STATS_TURN) <= 0) {
                GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getId());
                this.deleteItem(obj.getId());
            } else {
                GestorSalida.GAME_SEND_UPDATE_ITEM(this, obj);
            }
            Database.dinamicos.getObjectData().update(obj);
        }
        obj = getObjetByPos(Constantes.ITEM_POS_PNJ_SUIVEUR);
        if (obj != null) {
            obj.getCaracteristicas().addOneStat(Constantes.STATS_TURN, -1);
            if (obj.getCaracteristicas().getEffect(Constantes.STATS_TURN) <= 0) {
                GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getId());
                this.deleteItem(obj.getId());
            } else {
                GestorSalida.GAME_SEND_UPDATE_ITEM(this, obj);
            }
            Database.dinamicos.getObjectData().update(obj);
        }
        obj = getObjetByPos(Constantes.ITEM_POS_BENEDICTION);
        if (obj != null) {
            obj.getCaracteristicas().addOneStat(Constantes.STATS_TURN, -1);
            if (obj.getCaracteristicas().getEffect(Constantes.STATS_TURN) <= 0) {
                GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getId());
                this.deleteItem(obj.getId());
            } else {
                GestorSalida.GAME_SEND_UPDATE_ITEM(this, obj);
            }
            Database.dinamicos.getObjectData().update(obj);
        }
        obj = getObjetByPos(Constantes.ITEM_POS_MALEDICTION);
        if (obj != null) {
            obj.getCaracteristicas().addOneStat(Constantes.STATS_TURN, -1);
            if (obj.getCaracteristicas().getEffect(Constantes.STATS_TURN) <= 0) {
                gfxId = getClasse() * 10 + getSexe();
                if (this.getPelea() == null)
                    GestorSalida.GAME_SEND_ALTER_GM_PACKET(getCurMap(), this);
                GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getId());
                switch (obj.getModelo().getId()) {
                    case 8169, 8170 -> unsetFullMorph();
                }

                this.deleteItem(obj.getId());
            } else {
                GestorSalida.GAME_SEND_UPDATE_ITEM(this, obj);
            }
            Database.dinamicos.getObjectData().update(obj);
        }
        obj = getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF);
        if (obj != null) {
            obj.getCaracteristicas().addOneStat(Constantes.STATS_TURN, -1);
            if (obj.getCaracteristicas().getEffect(Constantes.STATS_TURN) <= 0) {
                gfxId = getClasse() * 10 + getSexe();
                GestorSalida.GAME_SEND_ALTER_GM_PACKET(getCurMap(), this);
                GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getId());
                this.deleteItem(obj.getId());
            } else {
                GestorSalida.GAME_SEND_UPDATE_ITEM(this, obj);
            }
            Database.dinamicos.getObjectData().update(obj);
        }
    }

    public boolean isSpec() {
        return _spec;
    }

    public void setSpec(boolean s) {
        this._spec = s;
    }

    public String getAllTitle() {
        _allTitle = Database.dinamicos.getPlayerData().loadTitles(this.getId());
        return _allTitle;
    }

    public void setAllTitle(String title) {
        getAllTitle();
        boolean erreur = false;
        if (title.equals(""))
            title = "0";
        if (_allTitle != null)
            for (String i : _allTitle.split(","))
                if (i.equals(title)) {
                    erreur = true;
                    break;
                }
        if (_allTitle == null && !erreur)
            _allTitle = title;
        else if (!erreur)
            _allTitle += "," + title;
        Database.dinamicos.getPlayerData().updateTitles(this.getId(), _allTitle);
    }

    public void setSpells(Map<Integer, Hechizo.SortStats> spells) {
        _sorts.clear();
        _sortsPlaces.clear();
        _sorts = spells;
        _sortsPlaces = Constantes.getStartSortsPlaces(this.getClasse());
    }

    public void teleportOldMap() {
        this.teleport(oldMap, oldCell);
    }

    public void setCurrentPositionToOldPosition() {
        this.curMap = Mundo.mundo.getMap(this.oldMap);
        this.curCell = this.curMap.getCase(this.oldCell);
    }

    public void setOldPosition() {
        this.oldMap = this.getCurMap().getId();
        this.oldCell = this.getCurCell().getId();
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public Grupo getParty() {
        return party;
    }

    public void setParty(Grupo party) {
        this.party = party;
    }

    public String parseSpellToDB() {
        StringBuilder sorts = new StringBuilder();

        if (_morphMode) {
            if (_saveSorts.isEmpty())
                return "";
            for (int key : _saveSorts.keySet()) {
                //3;1;a,4;3;b
                Hechizo.SortStats SS = _saveSorts.get(key);
                if (SS == null)
                    continue;
                sorts.append(SS.getSpellID()).append(";").append(SS.getLevel()).append(";");
                if (_saveSortsPlaces.get(key) != null)
                    sorts.append(_saveSortsPlaces.get(key));
                else
                    sorts.append("_");
                sorts.append(",");
            }
        } else {
            if (_sorts.isEmpty())
                return "";
            for (int key : _sorts.keySet()) {
                //3;1;a,4;3;b
                Hechizo.SortStats SS = _sorts.get(key);
                if (SS == null)
                    continue;
                sorts.append(SS.getSpellID()).append(";").append(SS.getLevel()).append(";");
                if (_sortsPlaces.get(key) != null)
                    sorts.append(_sortsPlaces.get(key));
                else
                    sorts.append("_");
                sorts.append(",");
            }
        }
        return sorts.substring(0, sorts.length() - 1);
    }

    private void parseSpells(String str) {
        if (!str.equalsIgnoreCase("")) {
            if (_morphMode) {
                String[] spells = str.split(",");
                _saveSorts.clear();
                _saveSortsPlaces.clear();
                for (String e : spells) {
                    try {
                        int id = Integer.parseInt(e.split(";")[0]);
                        int lvl = Integer.parseInt(e.split(";")[1]);
                        char place = e.split(";")[2].charAt(0);
                        learnSpell(id, lvl);
                        this._saveSortsPlaces.put(id, place);
                    } catch (NumberFormatException e1) {
                        e1.printStackTrace();
                    }
                }
            } else {
                String[] spells = str.split(",");
                _sorts.clear();
                _sortsPlaces.clear();
                for (String e : spells) {
                    try {
                        int id = Integer.parseInt(e.split(";")[0]);
                        int lvl = Integer.parseInt(e.split(";")[1]);
                        char place = e.split(";")[2].charAt(0);
                        if (!_morphMode)
                            learnSpell(id, lvl, false, false, false);
                        else
                            learnSpell(id, lvl, false, true, false);
                        _sortsPlaces.put(id, place);
                    } catch (NumberFormatException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    private void parseSpellsFullMorph(String str) {
        String[] spells = str.split(",");
        _sorts.clear();
        _sortsPlaces.clear();
        for (String e : spells) {
            try {
                int id = Integer.parseInt(e.split(";")[0]);
                int lvl = Integer.parseInt(e.split(";")[1]);
                char place = e.split(";")[2].charAt(0);
                if (!_morphMode)
                    learnSpell(id, lvl, false, false, false);
                else
                    learnSpell(id, lvl, false, true, false);
                _sortsPlaces.put(id, place);
            } catch (NumberFormatException e1) {
                e1.printStackTrace();
            }
        }
    }

    public String getSavePosition() {
        return _savePos;
    }

    public void set_savePos(String savePos) {
        _savePos = savePos;
    }

    public long getKamas() {
        return kamas;
    }

    public void setKamas(long l) {
        this.kamas = l;
    }

    public Map<Integer, EfectoHechizo> get_buff() {
        return buffs;
    }

    public Cuenta getAccount() {
        return account;
    }

    public void setAccount(Cuenta c) {
        account = c;
    }

    public int get_spellPts() {
        if (_morphMode)
            return _saveSpellPts;
        else
            return _spellPts;
    }

    public void set_spellPts(int pts) {
        if (_morphMode)
            _saveSpellPts = pts;
        else
            _spellPts = pts;
    }

    public Gremio getGuild() {
        if (_guildMember == null)
            return null;
        return _guildMember.getGuild();
    }

    public void setChangeName(boolean changeName) {
        this.changeName = changeName;
        if (changeName) this.send("AlEr");
    }

    public boolean isChangeName() {
        return changeName;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public int getDuelId() {
        return duelId;
    }

    public void setDuelId(int _duelid) {
        duelId = _duelid;
    }

    public Pelea getPelea() {
        return pelea;
    }

    public void setPelea(Pelea pelea) {
        refreshLife(false);
        if (pelea == null)
            GestorSalida.send(this, "ILS2000");
        else
            GestorSalida.send(this, "ILF0");
        this.sitted = false;
        this.pelea = pelea;
    }

    public boolean is_showFriendConnection() {
        return _showFriendConnection;
    }

    public boolean is_showWings() {
        return _showWings;
    }

    public boolean isShowSeller() {
        return _seeSeller;
    }

    public void setShowSeller(boolean is) {
        _seeSeller = is;
    }

    public String get_canaux() {
        return _canaux;
    }

    public GameCase getCurCell() {
        return curCell;
    }

    public void setCurCell(GameCase cell) {
        curCell = cell;
    }

    public int get_size() {
        return _size;
    }

    public void set_size(int _size) {
        this._size = _size;
    }

    public int getGfxId() {
        return gfxId;
    }

    public void setGfxId(int _gfxid) {
        if (this.getClasse() * 10 + this.getSexe() != _gfxid) {
            if (this.isOnMount())
                this.toogleOnMount();
            this.send("AR3K");
        } else {
            this.send("AR6bK");
        }
        gfxId = _gfxid;
    }

    public boolean isMorphMercenaire() {
        return (this.gfxId == 8009 || this.gfxId == 8006);
    }

    public Mapa getCurMap() {
        return curMap;
    }

    public void setCurMap(Mapa curMap) {
        this.curMap = curMap;
    }

    public boolean isAway() {
        return away;
    }

    public void setAway(boolean away) {
        this.away = away;
    }

    public boolean isSitted() {
        return sitted;
    }

    public void setSitted(boolean sitted) {
        if (this.sitted == sitted) {
            return;
        }
        this.sitted = sitted;
        refreshLife(false);
        regenRate = (sitted ? 1000 : 2000);
        GestorSalida.send(this, "ILS" + regenRate);
    }

    public int get_capital() {
        return _capital;
    }

    public void setSpellsPlace(boolean ok) {
        if (ok)
            _sortsPlaces = Constantes.getStartSortsPlaces(this.getClasse());
        else
            _sortsPlaces.clear();
        GestorSalida.GAME_SEND_SPELL_LIST(this);
    }

    public void learnSpell(int spell, int level, char pos) {
        if (Mundo.mundo.getSort(spell).getStatsByLevel(level) == null) {
            JuegoServidor.a();
            return;
        }

        if (!_sorts.containsKey(spell)) {
            _sorts.put(spell, Mundo.mundo.getSort(spell).getStatsByLevel(level));
            replace_SpellInBook(pos);
            _sortsPlaces.remove(spell);
            _sortsPlaces.put(spell, pos);
            GestorSalida.GAME_SEND_SPELL_LIST(this);
            GestorSalida.GAME_SEND_Im_PACKET(this, "03;" + spell);
        }
    }

    public boolean learnSpell(int spellID, int level, boolean save,
                              boolean send, boolean learn) {
        if (Mundo.mundo.getSort(spellID).getStatsByLevel(level) == null) {
            JuegoServidor.a();
            return false;
        }

        if (_sorts.containsKey(spellID) && learn) {
            GestorSalida.GAME_SEND_MESSAGE(this, "Tu posséde déjà ce sort.");
            return false;
        } else {
            _sorts.put(spellID, Mundo.mundo.getSort(spellID).getStatsByLevel(level));
            if (send) {
                GestorSalida.GAME_SEND_SPELL_LIST(this);
                GestorSalida.GAME_SEND_Im_PACKET(this, "03;" + spellID);
            }
            if (save)
                Database.dinamicos.getPlayerData().update(this);
            return true;
        }
    }

    public void learnSpell(int spellID, int level) {
        if (Mundo.mundo.getSort(spellID).getStatsByLevel(level) == null) {
            JuegoServidor.a();
            return;
        }

        if (_saveSorts.containsKey(spellID)) {
        } else {
            _saveSorts.put(spellID, Mundo.mundo.getSort(spellID).getStatsByLevel(level));
        }
    }

    public void unlearnSpell(int spell) {
        if (Mundo.mundo.getSort(spell) == null) {
            JuegoServidor.a();
            return;
        }

        _sorts.remove(spell);
        this._sortsPlaces.remove(spell);
        GestorSalida.GAME_SEND_SPELL_LIST(this);
        GestorSalida.GAME_SEND_STATS_PACKET(this);
        Database.dinamicos.getPlayerData().update(this);
    }

    public void unlearnSpell(Jugador perso, int spellID, int level,
                             int ancLevel, boolean save, boolean send) {
        int spellPoint = 1;
        if (ancLevel == 2)
            spellPoint = 1;
        if (ancLevel == 3)
            spellPoint = 2 + 1;
        if (ancLevel == 4)
            spellPoint = 3 + 3;
        if (ancLevel == 5)
            spellPoint = 4 + 6;
        if (ancLevel == 6)
            spellPoint = 5 + 10;

        if (Mundo.mundo.getSort(spellID).getStatsByLevel(level) == null) {
            JuegoServidor.a();
            return;
        }

        _sorts.put(spellID, Mundo.mundo.getSort(spellID).getStatsByLevel(level));
        if (send) {
            GestorSalida.GAME_SEND_SPELL_LIST(this);
            GestorSalida.GAME_SEND_Im_PACKET(this, "0154;" + "<b>" + ancLevel
                    + "</b>" + "~" + "<b>" + spellPoint + "</b>");
            addSpellPoint(spellPoint);
            GestorSalida.GAME_SEND_STATS_PACKET(perso);
        }
        if (save)
            Database.dinamicos.getPlayerData().update(this);
    }

    public boolean boostSpell(int spellID) {
        if (getSortStatBySortIfHas(spellID) == null)
            return false;
        int AncLevel = getSortStatBySortIfHas(spellID).getLevel();
        if (AncLevel == 6)
            return false;
        if (_spellPts >= AncLevel && Mundo.mundo.getSort(spellID).getStatsByLevel(AncLevel + 1).getReqLevel() <= this.getLevel()) {
            if (learnSpell(spellID, AncLevel + 1, true, false, false)) {
                _spellPts -= AncLevel;
                Database.dinamicos.getPlayerData().update(this);
                return true;
            } else {
                return false;
            }
        } else
        //Pas le niveau ou pas les Points
        {
            if (_spellPts < AncLevel)
                if (Mundo.mundo.getSort(spellID).getStatsByLevel(AncLevel + 1).getReqLevel() > this.getLevel())
                    return false;
        }
        return away;
    }

    public void boostSpellIncarnation() {
        for (Entry<Integer, Hechizo.SortStats> i : _sorts.entrySet()) {
            if (getSortStatBySortIfHas(i.getValue().getSpell().getSpellID()) == null)
                continue;
            if (learnSpell(i.getValue().getSpell().getSpellID(), i.getValue().getLevel() + 1, true, false, false))
                Database.dinamicos.getPlayerData().update(this);
        }
    }

    public boolean forgetSpell(int spellID) {
        if (getSortStatBySortIfHas(spellID) == null) {
            return false;
        }
        int AncLevel = getSortStatBySortIfHas(spellID).getLevel();
        if (AncLevel <= 1)
            return false;

        if (learnSpell(spellID, 1, true, false, false)) {
            _spellPts += Formulas.spellCost(AncLevel);
            Database.dinamicos.getPlayerData().update(this);
            return true;
        } else {
            return false;
        }
    }

    public void demorph() {
        if (this.getMorphMode()) {
            int morphID = this.getClasse() * 10 + this.getSexe();
            this.setGfxId(morphID);
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.getCurMap(), this.getId());
            GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(this.getCurMap(), this);
        }
    }

    public boolean getMorphMode() {
        return _morphMode;
    }

    public int getMorphId() {
        return _morphId;
    }

    public void setMorphId(int id) {
        this._morphId = id;
    }

    public void setFullMorph(int morphid, boolean isLoad, boolean join) {
        if (this.isOnMount()) this.toogleOnMount();
        if (_morphMode && !join)
            unsetFullMorph();
        if (this.isGhost) {
            GestorSalida.send(this, "Im1185");
            return;
        }

        Map<String, String> fullMorph = Mundo.mundo.getFullMorph(morphid);

        if (fullMorph == null) return;

        if (!join) {
            if (!_morphMode) {
                _saveSpellPts = _spellPts;
                _saveSorts.putAll(_sorts);
                _saveSortsPlaces.putAll(_sortsPlaces);
            }
            if (isLoad) {
                _saveSpellPts = _spellPts;
                _saveSorts.putAll(_sorts);
                _saveSortsPlaces.putAll(_sortsPlaces);
            }
        }

        _morphMode = true;
        _sorts.clear();
        _sortsPlaces.clear();
        _spellPts = 0;


        setGfxId(Integer.parseInt(fullMorph.get("gfxid")));
        if (this.pelea == null) GestorSalida.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
        parseSpellsFullMorph(fullMorph.get("spells"));
        setMorphId(morphid);

        if (this.getObjetByPos(Constantes.ITEM_POS_ARME) != null)
            if (Constantes.isIncarnationWeapon(this.getObjetByPos(Constantes.ITEM_POS_ARME).getModelo().getId()))
                for (int i = 0; i <= this.getObjetByPos(Constantes.ITEM_POS_ARME).getSoulStat().get(Constantes.STATS_NIVEAU); i++)
                    if (i == 10 || i == 20 || i == 30 || i == 40 || i == 50)
                        boostSpellIncarnation();
        if (this.pelea == null) {
            GestorSalida.GAME_SEND_ASK(this.getGameClient(), this);
            GestorSalida.GAME_SEND_SPELL_LIST(this);
        }


        if (fullMorph.get("vie") != null) {
            try {
                this.maxPdv = Integer.parseInt(fullMorph.get("vie"));
                this.setPdv(this.getMaxPdv());
                this.pa = Integer.parseInt(fullMorph.get("pa"));
                this.pm = Integer.parseInt(fullMorph.get("pm"));
                this.vitalite = Integer.parseInt(fullMorph.get("vitalite"));
                this.sagesse = Integer.parseInt(fullMorph.get("sagesse"));
                this.terre = Integer.parseInt(fullMorph.get("terre"));
                this.feu = Integer.parseInt(fullMorph.get("feu"));
                this.eau = Integer.parseInt(fullMorph.get("eau"));
                this.air = Integer.parseInt(fullMorph.get("air"));
                this.initiative = Integer.parseInt(fullMorph.get("initiative") + this.sagesse + this.terre + this.feu + this.eau + this.air);
                this.useStats = fullMorph.get("stats").equals("1");
                this.donjon = fullMorph.get("donjon").equals("1");
                this.useCac = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (this.pelea == null) GestorSalida.GAME_SEND_STATS_PACKET(this);
        if (!join)
            Database.dinamicos.getPlayerData().update(this);
    }

    public boolean isMorph() {
        return (this.gfxId != (this.getClasse() * 10 + this.getSexe()));
    }

    public boolean canCac() {
        return this.useCac;
    }

    public void unsetMorph() {
        this.setGfxId(this.getClasse() * 10 + this.getSexe());
        GestorSalida.GAME_SEND_ALTER_GM_PACKET(this.curMap, this);
        Database.dinamicos.getPlayerData().update(this);
    }

    public void unsetFullMorph() {
        if (!_morphMode)
            return;

        int morphID = this.getClasse() * 10 + this.getSexe();
        setGfxId(morphID);

        useStats = false;
        donjon = false;
        _morphMode = false;
        this.useCac = true;
        _sorts.clear();
        _sortsPlaces.clear();
        _spellPts = _saveSpellPts;
        _sorts.putAll(_saveSorts);
        _sortsPlaces.putAll(_saveSortsPlaces);
        parseSpells(parseSpellToDB());

        setMorphId(0);
        if (this.getPelea() == null) {
            GestorSalida.GAME_SEND_SPELL_LIST(this);
            GestorSalida.GAME_SEND_STATS_PACKET(this);
            GestorSalida.GAME_SEND_ALTER_GM_PACKET(this.curMap, this);
        }
        Database.dinamicos.getPlayerData().update(this);
    }

    public String parseSpellList() {
        StringBuilder packet = new StringBuilder();
        packet.append("SL");
        for (Hechizo.SortStats SS : _sorts.values()) {
            packet.append(SS.getSpellID()).append("~").append(SS.getLevel()).append("~").append(_sortsPlaces.get(SS.getSpellID())).append(";");
        }
        return packet.toString();
    }

    public void set_SpellPlace(int SpellID, char Place) {
        replace_SpellInBook(Place);
        _sortsPlaces.remove(SpellID);
        _sortsPlaces.put(SpellID, Place);
        Database.dinamicos.getPlayerData().update(this);
    }

    private void replace_SpellInBook(char Place) {
        for (int key : _sorts.keySet())
            if (_sortsPlaces.get(key) != null)
                if (_sortsPlaces.get(key).equals(Place))
                    _sortsPlaces.remove(key);
    }

    public Hechizo.SortStats getSortStatBySortIfHas(int spellID) {
        return _sorts.get(spellID);
    }

    public String parseALK() {
        StringBuilder perso = new StringBuilder();
        perso.append("|");
        perso.append(this.getId()).append(";");
        perso.append(this.getName()).append(";");
        perso.append(this.getLevel()).append(";");
        int gfx = this.gfxId;
        if (this.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF) != null)
            if (this.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF).getModelo().getId() == 10681)
                gfx = 8037;
        perso.append(gfx).append(";");
        int color1 = this.getColor1(), color2 = this.getColor2(), color3 = this.getColor3();
        if (this.getObjetByPos(Constantes.ITEM_POS_MALEDICTION) != null)
            if (this.getObjetByPos(Constantes.ITEM_POS_MALEDICTION).getModelo().getId() == 10838) {
                color1 = 16342021;
                color2 = 16342021;
                color3 = 16342021;
            }
        perso.append((color1 != -1 ? Integer.toHexString(color1) : "-1")).append(";");
        perso.append((color2 != -1 ? Integer.toHexString(color2) : "-1")).append(";");
        perso.append((color3 != -1 ? Integer.toHexString(color3) : "-1")).append(";");
        perso.append(getGMStuffString()).append(";");
        perso.append((this.isShowSeller() ? 1 : 0)).append(";");
        perso.append(Configuracion.INSTANCE.getSERVER_ID()).append(";");

        if (this.dead == 1 && Configuracion.INSTANCE.getHEROIC()) {
            perso.append(this.dead).append(";").append(this.deathCount);
        } else {
            perso.append(0);
        }
        return perso.toString();
    }

    public void remove() {
        Database.dinamicos.getPlayerData().delete(this);
    }

    public void OnJoinGame() {
        this.account.setCurrentPlayer(this);
        this.setOnline(true);
        this.changeName = false;
        if (this.account.getGameClient() == null)
            return;

        JuegoCliente client = this.account.getGameClient();

        if (this.isShowSeller()) {
            this.setShowSeller(false);
            Mundo.mundo.removeSeller(this.getId(), this.getCurMap().getId());
            GestorSalida.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(), this);
        }

        if (this._mount != null)
            GestorSalida.GAME_SEND_Re_PACKET(this, "+", this._mount);
        if (this.getClasse() * 10 + this.getSexe() != this.getGfxId())
            this.send("AR3K");

        GestorSalida.GAME_SEND_Rx_PACKET(this);
        GestorSalida.GAME_SEND_ASK(client, this);

        for (int a = 1; a < Mundo.mundo.getItemSetNumber(); a++)
            if (this.getNumbEquipedItemOfPanoplie(a) != 0)
                GestorSalida.GAME_SEND_OS_PACKET(this, a);

        if (this.pelea != null) GestorSalida.send(this, "ILF0");
        else GestorSalida.send(this, "ILS2000");

        if (this._metiers.size() > 0) {
            ArrayList<OficioCaracteristicas> list = new ArrayList<>(this._metiers.values());
            //packet JS
            GestorSalida.GAME_SEND_JS_PACKET(this, list);
            //packet JX
            GestorSalida.GAME_SEND_JX_PACKET(this, list);
            //Packet JO (Job Option)
            GestorSalida.GAME_SEND_JO_PACKET(this, list);
            ObjetoJuego obj = getObjetByPos(Constantes.ITEM_POS_ARME);
            if (obj != null)
                for (OficioCaracteristicas sm : list)
                    if (sm.getTemplate().isValidTool(obj.getModelo().getId()))
                        GestorSalida.GAME_SEND_OT_PACKET(account.getGameClient(), sm.getTemplate().getId());
        }

        GestorSalida.GAME_SEND_ALIGNEMENT(client, _align);
        GestorSalida.GAME_SEND_ADD_CANAL(client, _canaux + "^" + (this.getGroupe() != null ? "@" : ""));
        if (_guildMember != null)
            GestorSalida.GAME_SEND_gS_PACKET(this, _guildMember);
        GestorSalida.GAME_SEND_ZONE_ALLIGN_STATUT(client);
        GestorSalida.GAME_SEND_EMOTE_LIST(this, getCompiledEmote(this.emotes));
        GestorSalida.GAME_SEND_RESTRICTIONS(client);
        GestorSalida.GAME_SEND_Ow_PACKET(this);
        GestorSalida.GAME_SEND_SEE_FRIEND_CONNEXION(client, _showFriendConnection);
        GestorSalida.GAME_SEND_SPELL_LIST(this);
        this.account.sendOnline();

        //Messages de bienvenue
        GestorSalida.GAME_SEND_Im_PACKET(this, "189");
        if (!this.account.getLastConnectionDate().equals("") && !account.getLastIP().equals(""))
            GestorSalida.GAME_SEND_Im_PACKET(this, "0152;" + account.getLastConnectionDate() + "~" + account.getLastIP());

        GestorSalida.GAME_SEND_Im_PACKET(this, "0153;" + account.getCurrentIp());

        this.account.setLastIP(this.account.getCurrentIp());

        //Mise a jour du lastConnectionDate
        Date actDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd");
        String jour = dateFormat.format(actDate);
        dateFormat = new SimpleDateFormat("MM");
        String mois = dateFormat.format(actDate);
        dateFormat = new SimpleDateFormat("yyyy");
        String annee = dateFormat.format(actDate);
        dateFormat = new SimpleDateFormat("HH");
        String heure = dateFormat.format(actDate);
        dateFormat = new SimpleDateFormat("mm");
        String min = dateFormat.format(actDate);
        account.setLastConnectionDate(annee + "~" + mois + "~" + jour + "~"
                + heure + "~" + min);
        if (_guildMember != null)
            account.setLastConnectionDate(annee + "~" + mois + "~" + jour + "~"
                    + heure + "~" + min);
        //Affichage des prismes
        Mundo.mundo.showPrismes(this);

        //Actualizamos ultima conexion de la bdd
        Database.dinamicos.getAccountData().updateLastConnection(account);

        //Enviamos mensaje de bienvenida desde el lang
        GestorSalida.GAME_SEND_Im_PACKET(this, "1265;" + this.name + "~" + Configuracion.INSTANCE.getNAME());

        for (ObjetoJuego object : this.objects.values()) {
            if (object.getModelo().getType() == Constantes.ITEM_TYPE_FAMILIER) {
                MascotaEntrada p = Mundo.mundo.getPetsEntry(object.getId());
                Mascota pets = Mundo.mundo.getPets(object.getModelo().getId());

                if (p == null || pets == null) {
                    if (p != null && p.getPdv() > 0)
                        GestorSalida.GAME_SEND_Im_PACKET(this, "025");
                    continue;
                }
                if (pets.getType() == 0 || pets.getType() == 1)
                    continue;
                p.updatePets(this, Integer.parseInt(pets.getGap().split(",")[1]));
            } else if (object.getModelo().getId() == 10207) {
                String date = object.getTxtStat().get(Constantes.STATS_DATE);
                if (date != null) {
                    if (date.contains("#")) {
                        date = date.split("#")[3];
                    }
                    if (Instant.now().toEpochMilli() - Long.parseLong(date) > 604800000) {
                        object.getTxtStat().clear();
                        object.getTxtStat().putAll(Dopeul.generateStatsTrousseau());
                        GestorSalida.GAME_SEND_UPDATE_ITEM(this, object);
                    }
                }
            }
        }

        if (_morphMode)
            setFullMorph(_morphId, true, true);

        if (Configuracion.INSTANCE.getAUTO_REBOOT())
            this.send(Reinicio.toStr());
        if (MainServidor.INSTANCE.getFightAsBlocked())
            this.sendServerMessage("You can't fight until new order.");
        GestorEvento manager = GestorEvento.getInstance();
        if (manager.getCurrentEvent() != null && manager.getState() == GestorEvento.State.PROCESSED)
            this.sendMessage("(<b>Infos</b>) : L'événement '" + manager.getCurrentEvent().getName() + "' a démarrer, incrivez-vous à l'aide de <b>.event</b>.");

        if (Configuracion.INSTANCE.getMostrarenviados()) {
        Mundo.mundo.logger.info("El jugador " + this.getName() + " se ha conectado.");
        }

        if (this.getCurMap().getSubArea() != null) {
            if (this.getCurMap().getSubArea().getId() == 319 || this.getCurMap().getSubArea().getId() == 210)
                Temporizador.addSiguiente(() -> Minotot.sendPacketMap(this), 3, TimeUnit.SECONDS, Temporizador.DataType.CLIENTE);
            else if (this.getCurMap().getSubArea().getId() == 200)
                Temporizador.addSiguiente(() -> DragoCerdo.sendPacketMap(this), 3, TimeUnit.SECONDS, Temporizador.DataType.CLIENTE);
        }
        if (this.getEnergy() == 0) this.setGhost();
    }

    public void SetSeeFriendOnline(boolean bool) {
        _showFriendConnection = bool;
    }

    public void sendGameCreate() {
        this.setOnline(true);
        this.account.setCurrentPlayer(this);

        if (this.account.getGameClient() == null)
            return;

        JuegoCliente client = this.account.getGameClient();
        GestorSalida.GAME_SEND_GAME_CREATE(client, this.getName());
        GestorSalida.GAME_SEND_STATS_PACKET(this);
        Database.dinamicos.getPlayerData().updateLogged(this.id, 1);
        this.verifEquiped();

        if (this.needEndFight() == -1) {
            GestorSalida.GAME_SEND_MAPDATA(client, this.curMap.getId(), this.curMap.getDate(), this.curMap.getKey());
            GestorSalida.GAME_SEND_MAP_FIGHT_COUNT(client, this.getCurMap());
            if (this.getPelea() == null) this.curMap.addPlayer(this);
        } else {
            try {
                client.parsePacket("GI");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String parseToOa() {
        return "Oa" + this.getId() + "|" + getGMStuffString();
    }

    public String parseToGM() {
        StringBuilder str = new StringBuilder();
        if (pelea == null && curCell != null)// Hors combat
        {
            str.append(curCell.getId()).append(";").append(_orientation).append(";");
            str.append("0").append(";");//FIXME:?
            str.append(this.getId()).append(";").append(this.getName()).append(";").append(this.getClasse());
            str.append((this.get_title() > 0 ? ("," + this.get_title() + ";") : (";")));
            int gfx = gfxId;
            if (this.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF) != null)
                if (this.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF).getModelo().getId() == 10681)
                    gfx = 8037;
            str.append(gfx).append("^").append(_size);//gfxID^size
            if (this.getObjetByPos(Constantes.ITEM_POS_PNJ_SUIVEUR) != null)
                str.append(",").append(Constantes.getItemIdByMascotteId(this.getObjetByPos(Constantes.ITEM_POS_PNJ_SUIVEUR).getModelo().getId())).append("^100");
            str.append(";").append(this.getSexe()).append(";");
            str.append(_align).append(",");
            str.append("0").append(",");//FIXME:?
            str.append((_showWings ? getGrade() : "0")).append(",");
            str.append(this.getLevel() + this.getId());
            if (_showWings && _deshonor > 0) {
                str.append(",");
                str.append(_deshonor > 0 ? 1 : 0).append(';');
            } else {
                str.append(";");
            }
            int color1 = this.getColor1(), color2 = this.getColor2(), color3 = this.getColor3();
            if (this.getObjetByPos(Constantes.ITEM_POS_MALEDICTION) != null)
                if (this.getObjetByPos(Constantes.ITEM_POS_MALEDICTION).getModelo().getId() == 10838) {
                    color1 = 16342021;
                    color2 = 16342021;
                    color3 = 16342021;
                }

            str.append((color1 == -1 ? "-1" : Integer.toHexString(color1))).append(";");
            str.append((color2 == -1 ? "-1" : Integer.toHexString(color2))).append(";");
            str.append((color3 == -1 ? "-1" : Integer.toHexString(color3))).append(";");
            str.append(getGMStuffString()).append(";");
            if (hasEquiped(10054) || hasEquiped(10055) || hasEquiped(10056)
                    || hasEquiped(10058) || hasEquiped(10061)
                    || hasEquiped(10102)) {
                str.append(3).append(";");
                set_title(2);
            } else {
                if (get_title() == 2)
                    set_title(0);
                GrupoADM g = this.getGroupe();
                int level = this.getLevel();
                if (g != null)
                    if (!g.isJugador() || this.get_size() <= 0) // Si c'est un groupe non joueur ou que l'on est invisible on cache l'aura
                        level = 1;
                str.append((level > 99 ? (level > 199 ? (2) : (1)) : (0))).append(";");
            }
            str.append(";");//Emote
            str.append(";");//Emote timer
            if (this._guildMember != null
                    && this._guildMember.getGuild().haveTenMembers())
                str.append(this._guildMember.getGuild().getName()).append(";").append(this._guildMember.getGuild().getEmblem()).append(";");
            else
                str.append(";;");
            if (this.dead == 1 && !this.isGhost)
                str.append("-1");
            str.append(getSpeed()).append(";");//Restriction
            str.append((_onMount && _mount != null ? _mount.getStringColor(parsecolortomount()) : "")).append(";");
            str.append(this.isDead()).append(";");
        }
        return str.toString();
    }

    public String parseToMerchant() {
        StringBuilder str = new StringBuilder();
        str.append(curCell.getId()).append(";");
        str.append(_orientation).append(";");
        str.append("0").append(";");
        str.append(this.getId()).append(";");
        str.append(this.getName()).append(";");
        str.append("-5").append(";");//Merchant identifier
        str.append(gfxId).append("^").append(_size).append(";");
        int color1 = this.getColor1(), color2 = this.getColor2(), color3 = this.getColor3();
        if (this.getObjetByPos(Constantes.ITEM_POS_MALEDICTION) != null)
            if (this.getObjetByPos(Constantes.ITEM_POS_MALEDICTION).getModelo().getId() == 10838) {
                color1 = 16342021;
                color2 = 16342021;
                color3 = 16342021;
            }
        str.append((color1 == -1 ? "-1" : Integer.toHexString(color1))).append(";");
        str.append((color2 == -1 ? "-1" : Integer.toHexString(color2))).append(";");
        str.append((color3 == -1 ? "-1" : Integer.toHexString(color3))).append(";");
        str.append(getGMStuffString()).append(";");//acessories
        str.append((_guildMember != null ? _guildMember.getGuild().getName() : "")).append(";");//guildName
        str.append((_guildMember != null ? _guildMember.getGuild().getEmblem() : "")).append(";");//emblem
        str.append("0;");//offlineType
        return str.toString();
    }

    public String getGMStuffString() {
        StringBuilder str = new StringBuilder();

        ObjetoJuego object = getObjetByPos(Constantes.ITEM_POS_ARME);

        if (object != null)
            str.append(Integer.toHexString(object.getModelo().getId()));

        str.append(",");

        object = getObjetByPos(Constantes.ITEM_POS_COIFFE);

        if (object != null) {
            object.parseStatsString();

            Integer obvi = object.getCaracteristicas().getEffects().get(970);
            if (obvi == null) {
                str.append(Integer.toHexString(object.getModelo().getId()));
            } else {
                str.append(Integer.toHexString(obvi)).append("~16~").append(object.getObvijevanLook());
            }
        }

        str.append(",");

        object = getObjetByPos(Constantes.ITEM_POS_CAPE);

        if (object != null) {
            object.parseStatsString();

            Integer obvi = object.getCaracteristicas().getEffects().get(970);
            if (obvi == null) {
                str.append(Integer.toHexString(object.getModelo().getId()));
            } else {
                str.append(Integer.toHexString(obvi)).append("~17~").append(object.getObvijevanLook());
            }
        }

        str.append(",");

        object = getObjetByPos(Constantes.ITEM_POS_FAMILIER);

        if (object != null)
            str.append(Integer.toHexString(object.getModelo().getId()));

        str.append(",");

        object = getObjetByPos(Constantes.ITEM_POS_BOUCLIER);

        if (object != null)
            str.append(Integer.toHexString(object.getModelo().getId()));

        return str.toString();
    }

    public String getAsPacket() {
        refreshStats();
        refreshLife(true);
        StringBuilder ASData = new StringBuilder();
        ASData.append("As").append(xpString(",")).append("|");
        ASData.append(kamas).append("|").append(_capital).append("|").append(_spellPts).append("|");
        ASData.append(_align).append("~").append(_align).append(",").append(_aLvl).append(",").append(getGrade()).append(",").append(_honor).append(",").append(_deshonor).append(",").append((_showWings ? "1" : "0")).append("|");
        int pdv = this.curPdv;
        int pdvMax = this.maxPdv;
        if (pelea != null && !pelea.isFinish()) {
            Peleador f = pelea.getFighterByPerso(this);
            if (f != null) {
                pdv = f.getPdv();
                pdvMax = f.getPdvMax();
            }
        }
        Caracteristicas stats = this.getCaracteristicas(), sutffStats = this.getStuffStats(), donStats = this.getDonsStats(), buffStats = this.getBuffsStats(), totalStats = this.getTotalStats();

        ASData.append(pdv).append(",").append(pdvMax).append("|");
        ASData.append(this.getEnergy()).append(",10000|");
        ASData.append(getInitiative()).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_PROS) + sutffStats.getEffect(Constantes.STATS_ADD_PROS) + ((int) Math.ceil(totalStats.getEffect(Constantes.STATS_ADD_CHAN) / 10)) + buffStats.getEffect(Constantes.STATS_ADD_PROS) + ((int) Math.ceil(buffStats.getEffect(Constantes.STATS_ADD_CHAN) / 10))).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_PA)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_PA)).append(",").append(donStats.getEffect(Constantes.STATS_ADD_PA)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_PA)).append(",").append(totalStats.getEffect(Constantes.STATS_ADD_PA)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_PM)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_PM)).append(",").append(donStats.getEffect(Constantes.STATS_ADD_PM)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_PM)).append(",").append(totalStats.getEffect(Constantes.STATS_ADD_PM)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_FORC)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_FORC)).append(",").append(donStats.getEffect(Constantes.STATS_ADD_FORC)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_FORC)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_VITA)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_VITA)).append(",").append(donStats.getEffect(Constantes.STATS_ADD_VITA)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_VITA)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_SAGE)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_SAGE)).append(",").append(donStats.getEffect(Constantes.STATS_ADD_SAGE)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_SAGE)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_CHAN)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_CHAN)).append(",").append(donStats.getEffect(Constantes.STATS_ADD_CHAN)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_CHAN)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_AGIL)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_AGIL)).append(",").append(donStats.getEffect(Constantes.STATS_ADD_AGIL)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_AGIL)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_INTE)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_INTE)).append(",").append(donStats.getEffect(Constantes.STATS_ADD_INTE)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_INTE)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_PO)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_PO)).append(",").append(donStats.getEffect(Constantes.STATS_ADD_PO)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_PO)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_CREATURE)).append(",").append(sutffStats.getEffect(Constantes.STATS_CREATURE)).append(",").append(donStats.getEffect(Constantes.STATS_CREATURE)).append(",").append(buffStats.getEffect(Constantes.STATS_CREATURE)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_DOMA)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_DOMA)).append(",").append(donStats.getEffect(Constantes.STATS_ADD_DOMA)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_DOMA)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_PDOM)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_PDOM)).append(",").append(donStats.getEffect(Constantes.STATS_ADD_PDOM)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_PDOM)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_MAITRISE)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_MAITRISE)).append(",").append(donStats.getEffect(Constantes.STATS_ADD_MAITRISE)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_MAITRISE)).append("|");//ASData.append("0,0,0,0|");//Maitrise ?
        ASData.append(stats.getEffect(Constantes.STATS_ADD_PERDOM)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_PERDOM)).append(",").append(donStats.getEffect(Constantes.STATS_ADD_PERDOM)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_PERDOM)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_SOIN)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_SOIN)).append(",").append(donStats.getEffect(Constantes.STATS_ADD_SOIN)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_SOIN)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_TRAPDOM)).append(",").append(sutffStats.getEffect(Constantes.STATS_TRAPDOM)).append(",").append(donStats.getEffect(Constantes.STATS_TRAPDOM)).append(",").append(buffStats.getEffect(Constantes.STATS_TRAPDOM)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_TRAPPER)).append(",").append(sutffStats.getEffect(Constantes.STATS_TRAPPER)).append(",").append(donStats.getEffect(Constantes.STATS_TRAPPER)).append(",").append(buffStats.getEffect(Constantes.STATS_TRAPPER)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_RETDOM)).append(",").append(sutffStats.getEffect(Constantes.STATS_RETDOM)).append(",").append(donStats.getEffect(Constantes.STATS_RETDOM)).append(",").append(buffStats.getEffect(Constantes.STATS_RETDOM)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_CC)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_CC)).append(",").append(donStats.getEffect(Constantes.STATS_ADD_CC)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_CC)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_EC)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_EC)).append(",").append(donStats.getEffect(Constantes.STATS_ADD_EC)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_EC)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_AFLEE)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_AFLEE)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_AFLEE)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_AFLEE)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_MFLEE)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_MFLEE)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_MFLEE)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_MFLEE)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_R_NEU)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_R_NEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_NEU)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_NEU)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_RP_NEU)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_RP_NEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_NEU)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_NEU)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_R_PVP_NEU)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_R_PVP_NEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_PVP_NEU)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_PVP_NEU)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_RP_PVP_NEU)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_RP_PVP_NEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_PVP_NEU)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_PVP_NEU)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_R_TER)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_R_TER)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_TER)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_TER)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_RP_TER)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_RP_TER)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_TER)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_TER)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_R_PVP_TER)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_R_PVP_TER)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_PVP_TER)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_PVP_TER)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_RP_PVP_TER)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_RP_PVP_TER)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_PVP_TER)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_PVP_TER)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_R_EAU)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_R_EAU)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_EAU)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_EAU)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_RP_EAU)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_RP_EAU)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_EAU)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_EAU)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_R_PVP_EAU)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_R_PVP_EAU)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_PVP_EAU)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_PVP_EAU)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_RP_PVP_EAU)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_RP_PVP_EAU)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_PVP_EAU)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_PVP_EAU)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_R_AIR)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_R_AIR)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_AIR)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_AIR)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_RP_AIR)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_RP_AIR)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_AIR)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_AIR)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_R_PVP_AIR)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_R_PVP_AIR)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_PVP_AIR)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_PVP_AIR)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_RP_PVP_AIR)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_RP_PVP_AIR)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_PVP_AIR)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_PVP_AIR)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_R_FEU)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_R_FEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_FEU)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_FEU)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_RP_FEU)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_RP_FEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_FEU)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_FEU)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_R_PVP_FEU)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_R_PVP_FEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_PVP_FEU)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_R_PVP_FEU)).append("|");
        ASData.append(stats.getEffect(Constantes.STATS_ADD_RP_PVP_FEU)).append(",").append(sutffStats.getEffect(Constantes.STATS_ADD_RP_PVP_FEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_PVP_FEU)).append(",").append(buffStats.getEffect(Constantes.STATS_ADD_RP_PVP_FEU)).append("|");
        return ASData.toString();
    }

    public int getGrade() {
        if (_align == Constantes.ALINEAMIENTO_NEUTRAL)
            return 0;
        if (_honor >= 17500)
            return 10;
        for (int n = 1; n <= 10; n++)
            if (_honor < Mundo.mundo.getExpLevel(n).pvp)
                return n - 1;
        return 0;
    }

    public String xpString(String c) {
        if (!_morphMode) {
            return this.getExp() + c + Mundo.mundo.getPersoXpMin(this.getLevel()) + c + Mundo.mundo.getPersoXpMax(this.getLevel());
        } else {
            if (this.getObjetByPos(Constantes.ITEM_POS_ARME) != null)
                if (Constantes.isIncarnationWeapon(this.getObjetByPos(Constantes.ITEM_POS_ARME).getModelo().getId()))
                    if (this.getObjetByPos(Constantes.ITEM_POS_ARME).getSoulStat().get(Constantes.ERR_STATS_XP) != null)
                        return this.getObjetByPos(Constantes.ITEM_POS_ARME).getSoulStat().get(Constantes.ERR_STATS_XP)
                                + c
                                + Mundo.mundo.getBanditsXpMin(this.getObjetByPos(Constantes.ITEM_POS_ARME).getSoulStat().get(Constantes.STATS_NIVEAU))
                                + c
                                + Mundo.mundo.getBanditsXpMax(this.getObjetByPos(Constantes.ITEM_POS_ARME).getSoulStat().get(Constantes.STATS_NIVEAU));
        }
        return 1 + c + 1 + c + 1;
    }

    public int emoteActive() {
        return _emoteActive;
    }

    public void setEmoteActive(int emoteActive) {
        this._emoteActive = emoteActive;
    }

    public Caracteristicas getStuffStats() {
        if (this.useStats) return new Caracteristicas();

        Caracteristicas stats = new Caracteristicas(false, null);
        ArrayList<Integer> itemSetApplied = new ArrayList<>();

        for (ObjetoJuego gameObject : this.objects.values()) {
            byte position = (byte) gameObject.getPosicion();
            if (position != Constantes.ITEM_POS_NO_EQUIPED) {
                if (position >= 35 && position <= 48)
                    continue;

                stats = Caracteristicas.cumulStat(stats, gameObject.getCaracteristicas());
                int id = gameObject.getModelo().getPanoId();

                if (id > 0 && !itemSetApplied.contains(id)) {
                    itemSetApplied.add(id);
                    ObjetoSet objectSet = Mundo.mundo.getItemSet(id);
                    if (objectSet != null)
                        stats = Caracteristicas.cumulStat(stats, objectSet.getBonusStatByItemNumb(this.getNumbEquipedItemOfPanoplie(id)));
                }
            }
        }

        if (this._mount != null && this._onMount)
            stats = Caracteristicas.cumulStat(stats, this._mount.getStats());

        return stats;
    }

    public Caracteristicas getBuffsStats() {
        Caracteristicas stats = new Caracteristicas(false, null);
        if (this.pelea != null)
            if (this.pelea.getFighterByPerso(this) != null)
                for (EfectoHechizo entry : this.pelea.getFighterByPerso(this).getFightBuff())
                    stats.addOneStat(entry.getEffectID(), entry.getValue());

        for (Entry<Integer, EfectoHechizo> entry : buffs.entrySet())
            stats.addOneStat(entry.getValue().getEffectID(), entry.getValue().getValue());
        return stats;
    }

    public int get_orientation() {
        return _orientation;
    }

    public void set_orientation(int _orientation) {
        this._orientation = _orientation;
    }

    public int getInitiative() {
        if (!useStats) {
            int fact = 4;
            int maxPdv = this.maxPdv - 55;
            int curPdv = this.curPdv - 55;
            if (this.getClasse() == Constantes.CLASE_SACROGRITO)
                fact = 8;
            double coef = maxPdv / fact;

            coef += getStuffStats().getEffect(Constantes.STATS_ADD_INIT);
            coef += getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
            coef += getTotalStats().getEffect(Constantes.STATS_ADD_CHAN);
            coef += getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
            coef += getTotalStats().getEffect(Constantes.STATS_ADD_FORC);

            int init = 1;
            if (maxPdv != 0)
                init = (int) (coef * ((double) curPdv / (double) maxPdv));
            if (init < 0)
                init = 0;
            return init;
        } else {
            return this.initiative;
        }
    }

    public Caracteristicas getTotalStats() {
        Caracteristicas total = new Caracteristicas(false, null);
        if (!useStats) {
            total = Caracteristicas.cumulStat(total, this.getCaracteristicas());
            total = Caracteristicas.cumulStat(total, this.getStuffStats());
            total = Caracteristicas.cumulStat(total, this.getDonsStats());
            if (pelea != null)
                total = Caracteristicas.cumulStat(total, this.getBuffsStats());
        } else {
            return newStatsMorph();
        }
        return total;
    }

    public Caracteristicas getDonsStats() {
        Caracteristicas stats = new Caracteristicas(false, null);
        return stats;
    }

    public Caracteristicas newStatsMorph() {
        Caracteristicas stats = new Caracteristicas();
        stats.addOneStat(Constantes.STATS_ADD_PA, this.pa);
        stats.addOneStat(Constantes.STATS_ADD_PM, this.pm);
        stats.addOneStat(Constantes.STATS_ADD_VITA, this.vitalite);
        stats.addOneStat(Constantes.STATS_ADD_SAGE, this.sagesse);
        stats.addOneStat(Constantes.STATS_ADD_FORC, this.terre);
        stats.addOneStat(Constantes.STATS_ADD_INTE, this.feu);
        stats.addOneStat(Constantes.STATS_ADD_CHAN, this.eau);
        stats.addOneStat(Constantes.STATS_ADD_AGIL, this.air);
        stats.addOneStat(Constantes.STATS_ADD_INIT, this.initiative);
        stats.addOneStat(Constantes.STATS_ADD_PROS, 100);
        stats.addOneStat(Constantes.STATS_CREATURE, 1);
        this.useCac = false;
        return stats;
    }

    public int getPodUsed() {
        int pod = 0;

        for (Entry<Integer, ObjetoJuego> entry : objects.entrySet()) {
            pod += entry.getValue().getModelo().getPod()
                    * entry.getValue().getCantidad();
        }

        pod += parseStoreItemsListPods();
        return pod;
    }

    //Nuevo sistema de pods base en la config
    public int getMaximosPods() {
        Caracteristicas total = new Caracteristicas(false,null);
        total = Caracteristicas.cumulStat(total,this.getCaracteristicas());
        total = Caracteristicas.cumulStat(total,this.getStuffStats());
        total = Caracteristicas.cumulStat(total,this.getDonsStats());
        int pods = Configuracion.INSTANCE.getPodbase();
        pods +=this.getLevel()*5;
        pods +=total.getEffect(Constantes.STATS_ADD_PODS);
        pods +=total.getEffect(Constantes.STATS_ADD_FORC)*5;
        for (OficioCaracteristicas SM : _metiers.values())
        {
            pods+=SM.get_lvl()*5;
            if(SM.get_lvl()==100)
                pods+=1000;
        }
        if(pods < Configuracion.INSTANCE.getPodbase())
            pods = Configuracion.INSTANCE.getPodbase();
        return pods;
    }

    public void refreshLife(boolean refresh) {
        if (get_isClone())
            return;
        long time = (Instant.now().toEpochMilli() - regenTime);
        regenTime = Instant.now().toEpochMilli();
        if (pelea != null)
            return;
        if (regenRate == 0)
            return;
        if (this.curPdv > this.maxPdv) {
            this.curPdv = this.maxPdv - 1;
            if (!refresh)
                GestorSalida.GAME_SEND_STATS_PACKET(this);
            return;
        }

        int diff = (int) time / regenRate;
        if (diff >= 10 && this.curPdv < this.maxPdv && regenRate == 2000)
            GestorSalida.send(this, "ILF" + diff);

        setPdv(this.curPdv + diff);
    }

    public byte get_align() {
        return _align;
    }

    public int get_pdvper() {
        refreshLife(false);
        int pdvper = 100;
        pdvper = (100 * this.curPdv) / this.maxPdv;
        return Math.min(pdvper, 100);
    }

    public void useSmiley(String str) {
        try {
            int id = Integer.parseInt(str);
            Mapa map = curMap;
            if (pelea == null)
                GestorSalida.GAME_SEND_EMOTICONE_TO_MAP(map, this.getId(), id);
            else
                GestorSalida.GAME_SEND_EMOTICONE_TO_FIGHT(pelea, 7, this.getId(), id);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void boostStat(int stat, boolean capital) {
        int value = switch (stat) {
//Force
            case 10 -> this.getCaracteristicas().getEffect(Constantes.STATS_ADD_FORC);
//Chance
            case 13 -> this.getCaracteristicas().getEffect(Constantes.STATS_ADD_CHAN);
//Agilit�
            case 14 -> this.getCaracteristicas().getEffect(Constantes.STATS_ADD_AGIL);
//Intelligence
            case 15 -> this.getCaracteristicas().getEffect(Constantes.STATS_ADD_INTE);
            default -> 0;
        };
        int cout = Constantes.getReqPtsToBoostStatsByClass(this.getClasse(), stat, value);
        if (!capital)
            cout = 0;
        if (cout <= _capital) {
            switch (stat) {
                case 11://Vita
                    if (this.getClasse() != Constantes.CLASE_SACROGRITO)
                        this.getCaracteristicas().addOneStat(Constantes.STATS_ADD_VITA, 1);
                    else
                        this.getCaracteristicas().addOneStat(Constantes.STATS_ADD_VITA, capital ? 2 : 1);
                    break;
                case 12://Sage
                    this.getCaracteristicas().addOneStat(Constantes.STATS_ADD_SAGE, 1);
                    break;
                case 10://Force
                    this.getCaracteristicas().addOneStat(Constantes.STATS_ADD_FORC, 1);
                    break;
                case 13://Chance
                    this.getCaracteristicas().addOneStat(Constantes.STATS_ADD_CHAN, 1);
                    break;
                case 14://Agilit�
                    this.getCaracteristicas().addOneStat(Constantes.STATS_ADD_AGIL, 1);
                    break;
                case 15://Intelligence
                    this.getCaracteristicas().addOneStat(Constantes.STATS_ADD_INTE, 1);
                    break;
                default:
                    return;
            }
            _capital -= cout;
            GestorSalida.GAME_SEND_STATS_PACKET(this);
            Database.dinamicos.getPlayerData().update(this);
        }
    }

    public void boostStatFixedCount(int stat, int countVal) {
        for (int i = 0; i < countVal; i++) {
            int value = switch (stat) {
//Force
                case 10 -> this.getCaracteristicas().getEffect(Constantes.STATS_ADD_FORC);
//Chance
                case 13 -> this.getCaracteristicas().getEffect(Constantes.STATS_ADD_CHAN);
//Agilit�
                case 14 -> this.getCaracteristicas().getEffect(Constantes.STATS_ADD_AGIL);
//Intelligence
                case 15 -> this.getCaracteristicas().getEffect(Constantes.STATS_ADD_INTE);
                default -> 0;
            };
            int cout = Constantes.getReqPtsToBoostStatsByClass(this.getClasse(), stat, value);
            if (cout <= _capital) {
                switch (stat) {
                    case 11://Vita
                        if (this.getClasse() != Constantes.CLASE_SACROGRITO)
                            this.getCaracteristicas().addOneStat(Constantes.STATS_ADD_VITA, 1);
                        else
                            this.getCaracteristicas().addOneStat(Constantes.STATS_ADD_VITA, 2);
                        break;
                    case 12://Sage
                        this.getCaracteristicas().addOneStat(Constantes.STATS_ADD_SAGE, 1);
                        break;
                    case 10://Force
                        this.getCaracteristicas().addOneStat(Constantes.STATS_ADD_FORC, 1);
                        break;
                    case 13://Chance
                        this.getCaracteristicas().addOneStat(Constantes.STATS_ADD_CHAN, 1);
                        break;
                    case 14://Agilit�
                        this.getCaracteristicas().addOneStat(Constantes.STATS_ADD_AGIL, 1);
                        break;
                    case 15://Intelligence
                        this.getCaracteristicas().addOneStat(Constantes.STATS_ADD_INTE, 1);
                        break;
                    default:
                        return;
                }
                _capital -= cout;
            }
        }
        GestorSalida.GAME_SEND_STATS_PACKET(this);
        Database.dinamicos.getPlayerData().update(this);
    }

    public boolean isMuted() {
        return account.isMuted();
    }

    public String parseObjetsToDB() {
        StringBuilder str = new StringBuilder();
        if (objects.isEmpty())
            return "";
        for (Entry<Integer, ObjetoJuego> entry : objects.entrySet()) {
            ObjetoJuego obj = entry.getValue();
            if (obj == null)
                continue;
            str.append(obj.getId()).append("|");
        }

        return str.toString();
    }

    public boolean addObjet(ObjetoJuego newObj, boolean stackIfSimilar) {
        for (Entry<Integer, ObjetoJuego> entry : objects.entrySet()) {
            ObjetoJuego obj = entry.getValue();
            if (Mundo.mundo.getConditionManager().stackIfSimilar(obj, newObj, stackIfSimilar)) {
                obj.setCantidad(obj.getCantidad() + newObj.getCantidad());//On ajoute QUA item a la quantit� de l'objet existant
                if (isOnline)
                    GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
                return false;
            }
        }
        objects.put(newObj.getId(), newObj);
        GestorSalida.GAME_SEND_OAKO_PACKET(this, newObj);
        return true;
    }

    public void addObjet(ObjetoJuego newObj) {
        objects.put(newObj.getId(), newObj);
        GestorSalida.GAME_SEND_OAKO_PACKET(this, newObj);
    }

    public void addObject(ObjetoJuego newObj, boolean display) {
        this.objects.put(newObj.getId(), newObj);
        if (display) {
            GestorSalida.GAME_SEND_OAKO_PACKET(this, newObj);
        }
    }

    public Map<Integer, ObjetoJuego> getItems() {
        return objects;
    }

    public String parseItemToASK() {
        StringBuilder str = new StringBuilder();
        if (objects.isEmpty())
            return "";
        for (ObjetoJuego obj : objects.values()) {
            str.append(obj.parseItem());
        }
        return str.toString();
    }

    public String getItemsIDSplitByChar(String splitter) {
        StringBuilder str = new StringBuilder();
        if (objects.isEmpty())
            return "";
        for (int entry : objects.keySet()) {
            if (str.length() != 0)
                str.append(splitter);
            str.append(entry);
        }

        return str.toString();
    }

    public String getStoreItemsIDSplitByChar(String splitter) {
        StringBuilder str = new StringBuilder();
        if (_storeItems.isEmpty())
            return "";
        for (int entry : _storeItems.keySet()) {
            if (str.length() != 0)
                str.append(splitter);
            str.append(entry);
        }
        return str.toString();
    }

    public boolean hasItemGuid(int guid) {
        return objects.get(guid) != null && objects.get(guid).getCantidad() > 0;
    }

    public void sellItem(int guid, int qua) {
        if (qua <= 0)
            return;

        if (objects.get(guid).getCantidad() < qua)//Si il a moins d'item que ce qu'on veut Del
            qua = objects.get(guid).getCantidad();

        int prix = qua * (objects.get(guid).getModelo().getPrice() / 10);//Calcul du prix de vente (prix d'achat/10)
        int newQua = objects.get(guid).getCantidad() - qua;

        if (newQua <= 0)//Ne devrait pas etre <0, S'il n'y a plus d'item apres la vente
        {
            objects.remove(guid);
            Mundo.mundo.removeGameObject(guid);
            Database.dinamicos.getObjectData().delete(guid);
            GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
        } else
        //S'il reste des items apres la vente
        {
            objects.get(guid).setCantidad(newQua);
            GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, objects.get(guid));
        }
        kamas = kamas + prix;

        GestorSalida.GAME_SEND_STATS_PACKET(this);
        GestorSalida.GAME_SEND_Ow_PACKET(this);
        GestorSalida.GAME_SEND_ESK_PACKEt(this);
    }

    public void removeItem(int guid) {
        objects.remove(guid);
    }

    public void removeItem(int guid, int nombre, boolean send,
                           boolean deleteFromWorld) {
        ObjetoJuego obj = objects.get(guid);

        if (obj == null) return;

        if (nombre > obj.getCantidad())
            nombre = obj.getCantidad();

        if (obj.getCantidad() >= nombre) {
            int newQua = obj.getCantidad() - nombre;
            if (newQua > 0) {
                obj.setCantidad(newQua);
                if (send && isOnline)
                    GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
            } else {
                //on supprime de l'inventaire et du Monde
                objects.remove(obj.getId());
                if (deleteFromWorld)
                    Mundo.mundo.removeGameObject(obj.getId());
                //on envoie le packet si connect�
                if (send && isOnline)
                    GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getId());
            }
        }

        GestorSalida.GAME_SEND_Ow_PACKET(this);
    }

    public void deleteItem(int guid) {
        objects.remove(guid);
        Mundo.mundo.removeGameObject(guid);
    }

    public ObjetoJuego getObjetByPos(int pos) {
        if (pos == Constantes.ITEM_POS_NO_EQUIPED)
            return null;

        for (ObjetoJuego gameObject : this.objects.values()) {
            if (gameObject.getPosicion() == pos && pos == Constantes.ITEM_POS_FAMILIER) {
                if (gameObject.getTxtStat().isEmpty()) return null;
                else if (Mundo.mundo.getPetsEntry(gameObject.getId()) == null) return null;
            }
            if (gameObject.getPosicion() == pos) return gameObject;
        }

        return null;
    }

    //TODO: Delete s'te fonction.
    public ObjetoJuego getObjetByPos2(int pos) {
        if (pos == Constantes.ITEM_POS_NO_EQUIPED)
            return null;

        for (Entry<Integer, ObjetoJuego> entry : objects.entrySet()) {
            ObjetoJuego obj = entry.getValue();

            if (obj.getPosicion() == pos)
                return obj;
        }
        return null;
    }

    public void refreshStats() {
        double actPdvPer = (100 * (double) this.curPdv) / (double) this.maxPdv;
        if (!useStats)
            this.maxPdv = (this.getLevel() - 1) * 5 + 50 + getTotalStats().getEffect(Constantes.STATS_ADD_VITA);
        this.curPdv = (int) Math.round(maxPdv * actPdvPer / 100);
    }

    public boolean levelUp(boolean send, boolean addXp) {
        if (this.getLevel() == Mundo.mundo.getExpLevelSize())
            return false;
        this.level++;
        _capital += 5;
        _spellPts++;
        this.maxPdv += 5;
        this.setPdv(this.getMaxPdv());
        if (this.getLevel() == 100)
            this.getCaracteristicas().addOneStat(Constantes.STATS_ADD_PA, 1);
        Constantes.onLevelUpSpells(this, this.getLevel());
        if (addXp)
            this.exp = Mundo.mundo.getExpLevel(this.getLevel()).perso;
        if (send && isOnline) {
            GestorSalida.GAME_SEND_STATS_PACKET(this);
            GestorSalida.GAME_SEND_SPELL_LIST(this);
        }
        return true;
    }

    public boolean addXp(long winxp) {
        boolean up = false;
        this.exp += winxp;
        while (this.getExp() >= Mundo.mundo.getPersoXpMax(this.getLevel()) && this.getLevel() < Mundo.mundo.getExpLevelSize())
            up = levelUp(true, false);
        if (isOnline) {
            if (up)
                GestorSalida.GAME_SEND_NEW_LVL_PACKET(account.getGameClient(), this.getLevel());
            GestorSalida.GAME_SEND_STATS_PACKET(this);
        }
        return up;
    }

    public boolean levelUpIncarnations(boolean send, boolean addXp) {
        int level = this.getObjetByPos(Constantes.ITEM_POS_ARME).getSoulStat().get(Constantes.STATS_NIVEAU);

        if (level == 50)
            return false;

        level++;
        this.setPdv(this.getMaxPdv());
        GestorSalida.GAME_SEND_STATS_PACKET(this);

        switch (level) {
            case 10, 20, 30, 40, 50 -> boostSpellIncarnation();
        }

        if (send && isOnline) {
            GestorSalida.GAME_SEND_STATS_PACKET(this);
            GestorSalida.GAME_SEND_SPELL_LIST(this);
        }

        this.getObjetByPos(Constantes.ITEM_POS_ARME).getSoulStat().clear();
        this.getObjetByPos(Constantes.ITEM_POS_ARME).getSoulStat().put(Constantes.STATS_NIVEAU, level);
        this.getObjetByPos(Constantes.ITEM_POS_ARME);
        GestorSalida.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(this, this.getObjetByPos(Constantes.ITEM_POS_ARME));
        return true;
    }

    public boolean addXpIncarnations(long winxp) {
        boolean up = false;
        int level = this.getObjetByPos(Constantes.ITEM_POS_ARME).getSoulStat().get(Constantes.STATS_NIVEAU);
        long exp = this.getObjetByPos(Constantes.ITEM_POS_ARME).getSoulStat().get(Constantes.ERR_STATS_XP);
        exp += winxp;

        if (Constantes.isBanditsWeapon(this.getObjetByPos(Constantes.ITEM_POS_ARME).getModelo().getId())) {
            while (exp >= Mundo.mundo.getBanditsXpMax(level) && level < 50) {
                up = levelUpIncarnations(true, false);
                level = this.getObjetByPos(Constantes.ITEM_POS_ARME).getSoulStat().get(Constantes.STATS_NIVEAU);
            }
        } else if (Constantes.isTourmenteurWeapon(this.getObjetByPos(Constantes.ITEM_POS_ARME).getModelo().getId())) {
            while (exp >= Mundo.mundo.getTourmenteursXpMax(level) && level < 50) {
                up = levelUpIncarnations(true, false);
                level = this.getObjetByPos(Constantes.ITEM_POS_ARME).getSoulStat().get(Constantes.STATS_NIVEAU);
            }
        }
        if (isOnline)
            GestorSalida.GAME_SEND_STATS_PACKET(this);
        level = this.getObjetByPos(Constantes.ITEM_POS_ARME).getSoulStat().get(Constantes.STATS_NIVEAU);
        this.getObjetByPos(Constantes.ITEM_POS_ARME).getSoulStat().clear();
        this.getObjetByPos(Constantes.ITEM_POS_ARME).getSoulStat().put(Constantes.STATS_NIVEAU, level);
        this.getObjetByPos(Constantes.ITEM_POS_ARME).getSoulStat().put(Constantes.ERR_STATS_XP, (int) exp);
        return up;
    }

    public void addKamas(long l) {
        kamas += l;
    }

    public ObjetoJuego getSimilarItem(ObjetoJuego exGameObject) {
        if (exGameObject.getModelo().getId() == 8378)
            return null;

        for (ObjetoJuego gameObject : this.objects.values())
            if (gameObject.getModelo().getId() == exGameObject.getModelo().getId() && gameObject.getCaracteristicas().isSameStats(exGameObject.getCaracteristicas()) && gameObject.getId() != exGameObject.getId() && !Constantes.isIncarnationWeapon(exGameObject.getModelo().getId()) && exGameObject.getModelo().getType() != Constantes.ITEM_TYPE_CERTIFICAT_CHANIL && exGameObject.getModelo().getType() != Constantes.ITEM_TYPE_PIERRE_AME_PLEINE && gameObject.getModelo().getType() != Constantes.ITEM_TYPE_OBJET_ELEVAGE && gameObject.getModelo().getType() != Constantes.ITEM_TYPE_CERTIF_MONTURE && (exGameObject.getModelo().getType() != Constantes.ITEM_TYPE_QUETES || Constantes.isFlacGelee(gameObject.getModelo().getId())) && !Constantes.isCertificatDopeuls(gameObject.getModelo().getId()) && gameObject.getModelo().getType() != Constantes.ITEM_TYPE_FAMILIER && gameObject.getModelo().getType() != Constantes.ITEM_TYPE_OBJET_VIVANT && gameObject.getPosicion() == Constantes.ITEM_POS_NO_EQUIPED)
                return gameObject;

        return null;
    }

    public int learnJob(Oficio m) {
        for (Entry<Integer, OficioCaracteristicas> entry : _metiers.entrySet()) {
            if (entry.getValue().getTemplate().getId() == m.getId())//Si le joueur a d�j� le m�tier
                return -1;
        }
        int Msize = _metiers.size();
        if (Msize == 6)//Si le joueur a d�j� 6 m�tiers
            return -1;
        int pos = 0;
        if (OficioConstantes.isMageJob(m.getId())) {
            if (_metiers.get(5) == null)
                pos = 5;
            if (_metiers.get(4) == null)
                pos = 4;
            if (_metiers.get(3) == null)
                pos = 3;
        } else {
            if (_metiers.get(2) == null)
                pos = 2;
            if (_metiers.get(1) == null)
                pos = 1;
            if (_metiers.get(0) == null)
                pos = 0;
        }

        OficioCaracteristicas sm = new OficioCaracteristicas(pos, m, 1, 0);
        _metiers.put(pos, sm);//On apprend le m�tier lvl 1 avec 0 xp
        if (isOnline) {
            //on cr�er la listes des JobStats a envoyer (Seulement celle ci)
            ArrayList<OficioCaracteristicas> list = new ArrayList<>();
            list.add(sm);

            GestorSalida.GAME_SEND_Im_PACKET(this, "02;" + m.getId());
            //packet JS
            GestorSalida.GAME_SEND_JS_PACKET(this, list);
            //packet JX
            GestorSalida.GAME_SEND_JX_PACKET(this, list);
            //Packet JO (Job Option)
            GestorSalida.GAME_SEND_JO_PACKET(this, list);

            ObjetoJuego obj = getObjetByPos(Constantes.ITEM_POS_ARME);
            if (obj != null)
                if (sm.getTemplate().isValidTool(obj.getModelo().getId()))
                    GestorSalida.GAME_SEND_OT_PACKET(account.getGameClient(), m.getId());
        }
        return pos;
    }

    public void unlearnJob(int m) {
        _metiers.remove(m);
    }

    public void unequipedObjet(ObjetoJuego o) {
        o.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
        ObjetoModelo oTpl = o.getModelo();
        int idSetExObj = oTpl.getPanoId();
        if ((idSetExObj >= 81 && idSetExObj <= 92)
                || (idSetExObj >= 201 && idSetExObj <= 212)) {
            String[] stats = oTpl.getStrTemplate().split(",");
            for (String stat : stats) {
                String[] val = stat.split("#");
                String modifi = Integer.parseInt(val[0], 16) + ";"
                        + Integer.parseInt(val[1], 16) + ";0";
                GestorSalida.SEND_SB_SPELL_BOOST(this, modifi);
                this.removeObjectClassSpell(Integer.parseInt(val[1], 16));
            }
            this.removeObjectClass(oTpl.getId());
        }
        GestorSalida.GAME_SEND_OBJET_MOVE_PACKET(this, o);
        if (oTpl.getPanoId() > 0)
            GestorSalida.GAME_SEND_OS_PACKET(this, oTpl.getPanoId());
    }

    public void verifEquiped() {
        if (this.getMorphMode())
            return;
        ObjetoJuego arme = this.getObjetByPos(Constantes.ITEM_POS_ARME);
        ObjetoJuego bouclier = this.getObjetByPos(Constantes.ITEM_POS_BOUCLIER);
        if (arme != null) {
            if (arme.getModelo().isTwoHanded() && bouclier != null) {
                this.unequipedObjet(arme);
                GestorSalida.GAME_SEND_Im_PACKET(this, "119|44");
            } else if (!arme.getModelo().getConditions().equalsIgnoreCase("")
                    && !Mundo.mundo.getConditionManager().validConditions(this, arme.getModelo().getConditions())) {
                this.unequipedObjet(arme);
                GestorSalida.GAME_SEND_Im_PACKET(this, "119|44");
            }
        }
        if (bouclier != null) {
            if (!bouclier.getModelo().getConditions().equalsIgnoreCase("")
                    && !Mundo.mundo.getConditionManager().validConditions(this, bouclier.getModelo().getConditions())) {
                this.unequipedObjet(bouclier);
                GestorSalida.GAME_SEND_Im_PACKET(this, "119|44");
            }
        }
    }

    public boolean hasEquiped(int id) {
        for (Entry<Integer, ObjetoJuego> entry : objects.entrySet())
            if (entry.getValue().getModelo().getId() == id
                    && entry.getValue().getPosicion() != Constantes.ITEM_POS_NO_EQUIPED)
                return true;

        return false;
    }

    public int getInvitation() {
        return _inviting;
    }

    public void setInvitation(int target) {
        _inviting = target;
    }

    public String parseToPM() {
        StringBuilder str = new StringBuilder();
        str.append(this.getId()).append(";");
        str.append(this.getName()).append(";");
        str.append(gfxId).append(";");
        int color1 = this.getColor1(), color2 = this.getColor2(), color3 = this.getColor3();
        if (this.getObjetByPos(Constantes.ITEM_POS_MALEDICTION) != null)
            if (this.getObjetByPos(Constantes.ITEM_POS_MALEDICTION).getModelo().getId() == 10838) {
                color1 = 16342021;
                color2 = 16342021;
                color3 = 16342021;
            }
        str.append(color1).append(";");
        str.append(color2).append(";");
        str.append(color3).append(";");
        str.append(getGMStuffString()).append(";");
        str.append(this.curPdv).append(",").append(this.maxPdv).append(";");
        str.append(this.getLevel()).append(";");
        str.append(getInitiative()).append(";");
        str.append(getTotalStats().getEffect(Constantes.STATS_ADD_PROS)
                + ((int) Math.ceil(getTotalStats().getEffect(Constantes.STATS_ADD_CHAN) / 10))).append(";");
        str.append("0");//Side = ?
        return str.toString();
    }

    public int getNumbEquipedItemOfPanoplie(int panID) {
        int nb = 0;

        for (Entry<Integer, ObjetoJuego> i : objects.entrySet()) {
            //On ignore les objets non �quip�s
            if (i.getValue().getPosicion() == Constantes.ITEM_POS_NO_EQUIPED)
                continue;
            //On prend que les items de la pano demand�e, puis on augmente le nombre si besoin
            if (i.getValue().getModelo().getPanoId() == panID)
                nb++;
        }
        return nb;
    }

    public void startActionOnCell(AccionJuego GA) {
        int cellID = -1;
        int action = -1;
        try {
            cellID = Integer.parseInt(GA.args.split(";")[0]);
            action = Integer.parseInt(GA.args.split(";")[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cellID == -1 || action == -1)
            return;
        //Si case invalide

        if (!this.curMap.getCase(cellID).canDoAction(action))
            return;
        this.curMap.getCase(cellID).startAction(this, GA);
    }

    public void finishActionOnCell(AccionJuego GA) {
        int cellID = -1;
        try {
            cellID = Integer.parseInt(GA.args.split(";")[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cellID == -1)
            return;
        this.curMap.getCase(cellID).finishAction(this, GA);
    }

    public void teleportD(short newMapID, int newCellID) {
        if (this.getPelea() != null) return;
        this.curMap = Mundo.mundo.getMap(newMapID);
        this.curCell = Mundo.mundo.getMap(newMapID).getCase(newCellID);
        Database.dinamicos.getPlayerData().update(this);
    }

    public void teleportLaby(short newMapID, int newCellID) {
        if (this.getPelea() != null) return;
        JuegoCliente client = this.getGameClient();
        if (client == null)
            return;

        if (Mundo.mundo.getMap(newMapID) == null)
            return;

        if (Mundo.mundo.getMap(newMapID).getCase(newCellID) == null)
            return;

        GestorSalida.GAME_SEND_GA2_PACKET(client, this.getId());
        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.curMap, this.getId());

        if (this.getMount() != null)
            if (this.getMount().getFatigue() >= 220)
                this.getMount().setEnergy(this.getMount().getEnergy() - 1);

        if (this.curCell.getPlayers().contains(this))
            this.curCell.removePlayer(this);
        this.curMap = Mundo.mundo.getMap(newMapID);
        this.curCell = this.curMap.getCase(newCellID);

        GestorSalida.GAME_SEND_MAPDATA(client, newMapID, this.curMap.getDate(), this.curMap.getKey());
        this.curMap.addPlayer(this);

        if (!this.follower.isEmpty())// On met a jour la Map des personnages qui nous suivent
        {
            for (Jugador t : this.follower.values()) {
                if (t.isOnline())
                    GestorSalida.GAME_SEND_FLAG_PACKET(t, this);
                else
                    this.follower.remove(t.getId());
            }
        }
    }

    public void teleport(short newMapID, int newCellID) {
        if (this.getPelea() != null) return;
        JuegoCliente client = this.getGameClient();
        if (client == null)
            return;

        Mapa map = Mundo.mundo.getMap(newMapID);
        if (map == null) {
            JuegoServidor.a();
            return;
        }

        if (map.getCase(newCellID) == null) {
            JuegoServidor.a();
            return;
        }

        if (newMapID == this.curMap.getId()) {
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.curMap, this.getId());
            this.curCell.removePlayer(this);
            this.curCell = curMap.getCase(newCellID);
            this.curMap.addPlayer(this);
            GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(this.curMap, this);
            return;
        }
        this.setAway(false);
        boolean fullmorph = false;
        if (Constantes.isInMorphDonjon(this.curMap.getId()))
            if (!Constantes.isInMorphDonjon(newMapID))
                fullmorph = true;

        GestorSalida.GAME_SEND_GA2_PACKET(client, this.getId());
        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.curMap, this.getId());

        if (this.getMount() != null)
            if (this.getMount().getFatigue() >= 220)
                this.getMount().setEnergy(this.getMount().getEnergy() - 1);

        if (this.curCell.getPlayers().contains(this))
            this.curCell.removePlayer(this);
        this.curMap = map;
        this.curCell = this.curMap.getCase(newCellID);
        // Verification de la Map
        // Verifier la validit� du mountpark

        if (this.curMap.getMountPark() != null
                && this.curMap.getMountPark().getOwner() > 0
                && this.curMap.getMountPark().getGuild().getId() != -1) {
            if (Mundo.mundo.getGuild(this.curMap.getMountPark().getGuild().getId()) == null) {// Ne devrait  pas  arriver
                JuegoServidor.a();
                //FIXME : Map.MountPark.removeMountPark(curMap.getMountPark().getGuild().getId());
            }
        }

        // Verifier la validit� du Collector
        Recaudador col = Recaudador.getCollectorByMapId(this.curMap.getId());
        if (col != null) {
            if (Mundo.mundo.getGuild(col.getGuildId()) == null)// Ne devrait pas arriver
            {
                JuegoServidor.a();
                Recaudador.removeCollector(col.getGuildId());
            }
        }

        if (this.isInAreaNotSubscribe()) {
            if (!this.isInPrivateArea)
                GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.getGameClient(), 'S');
            this.isInPrivateArea = true;
        } else {
            this.isInPrivateArea = false;
        }

        GestorSalida.GAME_SEND_MAPDATA(client, newMapID, this.curMap.getDate(), this.curMap.getKey());
        this.curMap.addPlayer(this);

        if (fullmorph)
            this.unsetFullMorph();

        if (this.follower != null && !this.follower.isEmpty())// On met a jour la Map des personnages qui nous suivent
        {
            for (Jugador t : this.follower.values()) {
                if (t.isOnline())
                    GestorSalida.GAME_SEND_FLAG_PACKET(t, this);
                else
                    this.follower.remove(t.getId());
            }
        }

        if (this.getInHouse() != null)
            if (this.getInHouse().getMapId() == this.curMap.getId())
                this.setInHouse(null);

        if (map.getSubArea() != null) {
            if (map.getSubArea().getId() == 200) {
                Temporizador.addSiguiente(() -> DragoCerdo.sendPacketMap(this), 1000, Temporizador.DataType.MAPA);
            } else if (map.getSubArea().getId() == 210 || map.getSubArea().getId() == 319) {
                Temporizador.addSiguiente(() -> Minotot.sendPacketMap(this), 1000, Temporizador.DataType.MAPA);
            }
        }
    }

    public void teleport(Mapa map, int cell) {
        if (this.getPelea() != null) return;
        JuegoCliente PW = null;
        if (account.getGameClient() != null) {
            PW = account.getGameClient();
        }
        if (map == null) {
            JuegoServidor.a();
            return;
        }
        if (map.getCase(cell) == null) {
            JuegoServidor.a();
            return;
        }
        if (!cantTP()) {
            if (this.getCurMap().getSubArea() != null
                    && map.getSubArea() != null) {
                if (this.getCurMap().getSubArea().getId() == 165
                        && map.getSubArea().getId() == 165) {
                    if (this.hasItemTemplate(997, 1)) {
                        this.removeByTemplateID(997, 1);
                    } else {
                        GestorSalida.GAME_SEND_Im_PACKET(this, "14");
                        return;
                    }
                }
            }
        }

        boolean fullmorph = false;
        if (Constantes.isInMorphDonjon(curMap.getId()))
            if (!Constantes.isInMorphDonjon(map.getId()))
                fullmorph = true;

        if (map.getId() == curMap.getId()) {
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(curMap, this.getId());
            curCell.removePlayer(this);
            curCell = curMap.getCase(cell);
            curMap.addPlayer(this);
            GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(curMap, this);
            if (fullmorph)
                this.unsetFullMorph();
            return;
        }
        if (PW != null) {
            GestorSalida.GAME_SEND_GA2_PACKET(PW, this.getId());
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(curMap, this.getId());
        }
        if (this.getMount() != null)
            if (this.getMount().getFatigue() >= 220)
                this.getMount().setEnergy(this.getMount().getEnergy() - 1);
        curCell.removePlayer(this);
        curMap = map;
        curCell = curMap.getCase(cell);
        // Verification de la Map
        // Verifier la validit� du mountpark
        if (curMap.getMountPark() != null
                && curMap.getMountPark().getOwner() > 0
                && curMap.getMountPark().getGuild().getId() != -1) {
            if (Mundo.mundo.getGuild(curMap.getMountPark().getGuild().getId()) == null)// Ne devrait  pas  arriver
            {
                JuegoServidor.a();
                //FIXME : Map.MountPark.removeMountPark(curMap.getMountPark().getGuild().getId());
            }
        }
        // Verifier la validit� du Collector
        if (Recaudador.getCollectorByMapId(curMap.getId()) != null) {
            if (Mundo.mundo.getGuild(Objects.requireNonNull(Recaudador.getCollectorByMapId(curMap.getId())).getGuildId()) == null)// Ne devrait pas arriver
            {
                JuegoServidor.a();
                Recaudador.removeCollector(Objects.requireNonNull(Recaudador.getCollectorByMapId(curMap.getId())).getGuildId());
            }
        }

        if (PW != null) {
            GestorSalida.GAME_SEND_MAPDATA(PW, map.getId(), curMap.getDate(), curMap.getKey());
            curMap.addPlayer(this);
            if (fullmorph)
                this.unsetFullMorph();
        }

        if (!follower.isEmpty())// On met a jour la Map des personnages qui nous suivent
        {
            for (Jugador t : follower.values()) {
                if (t.isOnline())
                    GestorSalida.GAME_SEND_FLAG_PACKET(t, this);
                else
                    follower.remove(t.getId());
            }
        }
    }

    public void disconnectInFight() {
        //Si en groupe
        if (getParty() != null)
            getParty().leave(this);
        resetVars();
        Database.dinamicos.getPlayerData().update(this);
        set_isClone(true);
        Mundo.mundo.unloadPerso(this.getId());
    }

    public int getBankCost() {
        return account.getBanco().size();
    }

    public void openBank() {
        if (this.getExchangeAction() != null)
            return;
        if (this.getDeshonor() >= 1) {
            GestorSalida.GAME_SEND_Im_PACKET(this, "183");
            return;
        }

        final int cost = this.getBankCost();
        Database.dinamicos.getPlayerData().update(this);

        if (cost > 0) {
            final long kamas = this.getKamas();
            final long remaining = kamas - cost;
            final long bank = this.getAccount().getBankKamas();
            final long total = bank + kamas;
            if (remaining < 0) {
                if (bank >= cost) {
                    this.setBankKamas(bank - cost);
                } else if (total >= cost) {
                    this.setKamas(0);
                    this.setBankKamas(total - cost);
                    GestorSalida.GAME_SEND_STATS_PACKET(this);
                    GestorSalida.GAME_SEND_Im_PACKET(this, "020;" + kamas);
                } else {
                    GestorSalida.GAME_SEND_MESSAGE_SERVER(this, "10|" + cost);
                    return;
                }
            } else {
                this.setKamas(remaining);
                GestorSalida.GAME_SEND_STATS_PACKET(this);
                GestorSalida.GAME_SEND_Im_PACKET(this, "020;" + cost);
            }
        }
        GestorSalida.GAME_SEND_ECK_PACKET(this.getGameClient(), 5, "");
        GestorSalida.GAME_SEND_EL_BANK_PACKET(this);
        this.setAway(true);
        this.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.IN_BANK, 0));
    }

    public String getStringVar(String str) {
        return switch (str) {
            case "name" -> this.getName();
            case "bankCost" -> getBankCost() + "";
            case "points" -> this.getAccount().getPoints() + "";
            case "nbrOnline" -> JuegoServidor.getClients().size() + "";
            case "align" -> Mundo.mundo.getStatOfAlign();
            default -> "";
        };
    }

    public void refreshMapAfterFight() {
        GestorSalida.send(this, "ILS" + 2000);
        this.regenRate = 2000;
        this.curMap.addPlayer(this);
        if (this.account.getGameClient() != null)
            GestorSalida.GAME_SEND_STATS_PACKET(this);
        this.pelea = null;
        this.away = false;
    }

    public long getBankKamas() {
        return account.getBankKamas();
    }

    public void setBankKamas(long i) {
        account.setBankKamas(i);
        Database.estaticos.getBankData().update(account);
    }

    public String parseBankPacket() {
        StringBuilder packet = new StringBuilder();
        for (ObjetoJuego entry : account.getBanco())
            packet.append("O").append(entry.parseItem()).append(";");
        if (getBankKamas() != 0)
            packet.append("G").append(getBankKamas());
        return packet.toString();
    }

    public void addCapital(int pts) {
        _capital += pts;
    }

    public void addSpellPoint(int pts) {
        if (_morphMode)
            _saveSpellPts += pts;
        else
            _spellPts += pts;
    }

    public void addInBank(int guid, int qua) {
        if (qua <= 0)
            return;
        ObjetoJuego PersoObj = Mundo.getGameObject(guid);

        if (this.objects == null) return;

        if (objects.get(guid) == null) // Si le joueur n'a pas l'item dans son sac ...
            return;

        if (PersoObj.getPosicion() != Constantes.ITEM_POS_NO_EQUIPED) // Si c'est un item �quip� ...
            return;

        ObjetoJuego BankObj = getSimilarBankItem(PersoObj);
        int newQua = PersoObj.getCantidad() - qua;
        if (BankObj == null) // Ajout d'un nouvel objet dans la banque
        {
            if (newQua <= 0) // Ajout de toute la quantit� disponible
            {
                removeItem(PersoObj.getId()); // On enleve l'objet du sac du joueur
                account.getBanco().add(PersoObj); // On met l'objet du sac dans la banque, avec la meme quantit�
                String str = "O+" + PersoObj.getId() + "|"
                        + PersoObj.getCantidad() + "|"
                        + PersoObj.getModelo().getId() + "|"
                        + PersoObj.parseStatsString();
                GestorSalida.GAME_SEND_EsK_PACKET(this, str);
                GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
            } else
            //S'il reste des objets au joueur
            {
                PersoObj.setCantidad(newQua); //on modifie la quantit� d'item du sac
                BankObj = ObjetoJuego.getCloneObjet(PersoObj, qua); //On ajoute l'objet a la banque et au monde
                Mundo.addGameObject(BankObj, true);
                account.getBanco().add(BankObj);

                String str = "O+" + BankObj.getId() + "|"
                        + BankObj.getCantidad() + "|"
                        + BankObj.getModelo().getId() + "|"
                        + BankObj.parseStatsString();
                GestorSalida.GAME_SEND_EsK_PACKET(this, str); //Envoie des packets
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
            }
        } else
        // S'il y avait un item du meme template
        {
            if (newQua <= 0) //S'il ne reste pas d'item dans le sac
            {
                removeItem(PersoObj.getId()); //On enleve l'objet du sac du joueur
                Mundo.mundo.removeGameObject(PersoObj.getId()); //On enleve l'objet du monde
                BankObj.setCantidad(BankObj.getCantidad()
                        + PersoObj.getCantidad()); //On ajoute la quantit� a l'objet en banque
                String str = "O+" + BankObj.getId() + "|"
                        + BankObj.getCantidad() + "|"
                        + BankObj.getModelo().getId() + "|"
                        + BankObj.parseStatsString(); //on envoie l'ajout a la banque de l'objet
                GestorSalida.GAME_SEND_EsK_PACKET(this, str);
                GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, guid); //on envoie la supression de l'objet du sac au joueur
            } else
            //S'il restait des objets
            {
                PersoObj.setCantidad(newQua); //on modifie la quantit� d'item du sac
                BankObj.setCantidad(BankObj.getCantidad() + qua);
                String str = "O+" + BankObj.getId() + "|"
                        + BankObj.getCantidad() + "|"
                        + BankObj.getModelo().getId() + "|"
                        + BankObj.parseStatsString();
                GestorSalida.GAME_SEND_EsK_PACKET(this, str);
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
            }
        }
        GestorSalida.GAME_SEND_Ow_PACKET(this);

        Database.dinamicos.getPlayerData().update(this);
        Database.estaticos.getBankData().update(account);
    }

    private ObjetoJuego getSimilarBankItem(ObjetoJuego exGameObject) {
        for (ObjetoJuego gameObject : this.account.getBanco())
            if (Mundo.mundo.getConditionManager().stackIfSimilar(gameObject, exGameObject, true))
                return gameObject;
        return null;
    }

    public void removeFromBank(int guid, int qua) {
        if (qua <= 0)
            return;
        ObjetoJuego BankObj = Mundo.getGameObject(guid);

        //Si le joueur n'a pas l'item dans sa banque ...
        int index = account.getBanco().indexOf(BankObj);
        if (index == -1)
            return;

        ObjetoJuego PersoObj = getSimilarItem(BankObj);
        int newQua = BankObj.getCantidad() - qua;

        if (PersoObj == null)//Si le joueur n'avait aucun item similaire
        {
            //S'il ne reste rien en banque
            if (newQua <= 0) {
                //On retire l'item de la banque
                account.getBanco().remove(index);
                //On l'ajoute au joueur

                objects.put(guid, BankObj);


                //On envoie les packets
                GestorSalida.GAME_SEND_OAKO_PACKET(this, BankObj);
                String str = "O-" + guid;
                GestorSalida.GAME_SEND_EsK_PACKET(this, str);
            } else
            //S'il reste des objets en banque
            {
                //On cr�e une copy de l'item en banque
                PersoObj = ObjetoJuego.getCloneObjet(BankObj, qua);
                //On l'ajoute au monde
                Mundo.addGameObject(PersoObj, true);
                //On retire X objet de la banque
                BankObj.setCantidad(newQua);
                //On l'ajoute au joueur

                objects.put(PersoObj.getId(), PersoObj);


                //On envoie les packets
                GestorSalida.GAME_SEND_OAKO_PACKET(this, PersoObj);
                String str = "O+" + BankObj.getId() + "|"
                        + BankObj.getCantidad() + "|"
                        + BankObj.getModelo().getId() + "|"
                        + BankObj.parseStatsString();
                GestorSalida.GAME_SEND_EsK_PACKET(this, str);
            }
        } else {
            //S'il ne reste rien en banque
            if (newQua <= 0) {
                //On retire l'item de la banque
                account.getBanco().remove(index);
                Mundo.mundo.removeGameObject(BankObj.getId());
                //On Modifie la quantit� de l'item du sac du joueur
                PersoObj.setCantidad(PersoObj.getCantidad()
                        + BankObj.getCantidad());

                //On envoie les packets
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
                String str = "O-" + guid;
                GestorSalida.GAME_SEND_EsK_PACKET(this, str);
            } else
            //S'il reste des objets en banque
            {
                //On retire X objet de la banque
                BankObj.setCantidad(newQua);
                //On ajoute X objets au joueurs
                PersoObj.setCantidad(PersoObj.getCantidad() + qua);

                //On envoie les packets
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
                String str = "O+" + BankObj.getId() + "|"
                        + BankObj.getCantidad() + "|"
                        + BankObj.getModelo().getId() + "|"
                        + BankObj.parseStatsString();
                GestorSalida.GAME_SEND_EsK_PACKET(this, str);
            }
        }

        GestorSalida.GAME_SEND_Ow_PACKET(this);

        Database.dinamicos.getPlayerData().update(this);
        Database.estaticos.getBankData().update(account);
    }

    //Paketes cercados
    //Monsturas se desaparecen, corregido
    public void AbrirCercado() {
        if (this.getDeshonor() >= 5) {
            GestorSalida.GAME_SEND_Im_PACKET(this, "183");
            return;
        }
        if (this.getGuildMember() != null && this.curMap.getMountPark().getGuild() != null) {
            if (this.curMap.getMountPark().getGuild().getId() == this.getGuildMember().getGuild().getId()) {
                if (!this.getGuildMember().canDo(Constantes.G_USEENCLOS)) {
                    GestorSalida.GAME_SEND_Im_PACKET(this, "1101");
                    return;
                }
            }
        }

        Cercados mountPark = this.curMap.getMountPark();
        this.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.IN_MOUNTPARK, mountPark));
        this.away = true;

        StringBuilder packet = new StringBuilder();

        if(mountPark.getEtable().size()>0) //in shed
            for(Montura mount : mountPark.getEtable())
                if(mount.getOwner()==this.getId())
                {
                    packet.append(";");
                    packet.append(mount.parse());
                }
        packet.append("~");

        if(mountPark.getListOfRaising().size()>0) //in field
            for(Integer id : mountPark.getListOfRaising())
            {
                Montura mount = Mundo.mundo.getMountById(id);
                if(mount==null)
                    continue;

                if(mount.getOwner()==this.getId())
                {
                    packet.append(";");
                    packet.append(mount.parse());
                }
                else if(getGuildMember()!=null)
                    if(getGuildMember().canDo(Constantes.G_OTHDINDE)&&mountPark.getOwner()!=-1&&mountPark.getGuild()!=null)
                        if(mountPark.getGuild().getId() == this.getGuild().getId())
                        {
                            packet.append(";");
                            packet.append(mount.parse());
                        }
            }

        GestorSalida.GAME_SEND_ECK_PACKET(this,16,packet.toString());
        Temporizador.addSiguiente(() -> mountPark.getEtable().stream().filter(mount -> mount != null && mount.getSize() == 50 && mount.getOwner() == this.getId()).forEach(mount -> GestorSalida.GAME_SEND_Ee_PACKET_WAIT(this, '~', mount.parse())), 500, Temporizador.DataType.CLIENTE);
    }

    public void fullPDV() {
        this.setPdv(this.getMaxPdv());
        GestorSalida.GAME_SEND_STATS_PACKET(this);
    }

    public void warpToSavePos() {
        try {
            String[] infos = _savePos.split(",");
            this.teleport(Short.parseShort(infos[0]), Integer.parseInt(infos[1]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeByTemplateID(int tID, int count) {
        //Copie de la liste pour eviter les modif concurrentes

        ArrayList<ObjetoJuego> list = new ArrayList<>(objects.values());


        ArrayList<ObjetoJuego> remove = new ArrayList<>();
        int tempCount = count;

        //on verifie pour chaque objet
        for (ObjetoJuego obj : list) {
            //Si mauvais TemplateID, on passe
            if (obj.getModelo().getId() != tID)
                continue;

            if (obj.getCantidad() >= count) {
                int newQua = obj.getCantidad() - count;
                if (newQua > 0) {
                    obj.setCantidad(newQua);
                    if (isOnline)
                        GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
                } else {
                    //on supprime de l'inventaire et du Monde
                    objects.remove(obj.getId());
                    Mundo.mundo.removeGameObject(obj.getId());
                    //on envoie le packet si connect�
                    if (isOnline)
                        GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getId());
                }
                return;
            } else
            //Si pas assez d'objet
            {
                if (obj.getCantidad() >= tempCount) {
                    int newQua = obj.getCantidad() - tempCount;
                    if (newQua > 0) {
                        obj.setCantidad(newQua);
                        if (isOnline)
                            GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
                    } else
                        remove.add(obj);

                    for (ObjetoJuego o : remove) {
                        //on supprime de l'inventaire et du Monde

                        objects.remove(o.getId());

                        Mundo.mundo.removeGameObject(o.getId());
                        //on envoie le packet si connect�
                        if (isOnline)
                            GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, o.getId());
                    }
                } else {
                    // on r�duit le compteur
                    tempCount -= obj.getCantidad();
                    remove.add(obj);
                }
            }
        }
    }

    public ArrayList<Oficio> getJobs() {
        ArrayList<Oficio> list = new ArrayList<>();
        for (OficioCaracteristicas js : _metiers.values())
            if (js.getTemplate() != null)
                list.add(js.getTemplate());
        return (list.isEmpty() ? null : list);
    }

    public Map<Integer, OficioCaracteristicas> getMetiers() {
        return _metiers;
    }

    public void doJobAction(int actionID, ObjetosInteractivos object,
                            AccionJuego GA, GameCase cell) {
        OficioCaracteristicas SM = getMetierBySkill(actionID);
        if (SM == null) {
            switch (actionID) {
                case 151 -> {
                    new OficioAccion(151, 4, 0, true, 100, 0).startAction(this, object, GA, cell);
                    return;
                }
                case 121 -> {
                    new OficioAccion(121, 8, 0, true, 100, 0).startAction(this, object, GA, cell);
                    return;
                }
                case 110 -> {
                    new OficioAccion(110, 2, 0, true, 100, 0).startAction(this, object, GA, cell);
                    return;
                }
                case 22 -> {
                    new OficioAccion(22, 1, 0, true, 100, 0).startAction(this, object, GA, cell);
                    return;
                }
            }
            GestorSalida.GAME_SEND_MESSAGE(this, "Erreur stats job null.");
            return;
        }
        SM.startAction(actionID, this, object, GA, cell);
    }

    public void finishJobAction(int actionID, ObjetosInteractivos object,
                                AccionJuego GA, GameCase cell) {
        OficioCaracteristicas SM = getMetierBySkill(actionID);
        if (SM == null)
            return;
        SM.endAction(this, object, GA, cell);
    }

    public String parseJobData() {
        StringBuilder str = new StringBuilder();
        if (_metiers.isEmpty())
            return "";
        for (OficioCaracteristicas SM : _metiers.values()) {
            if (SM == null)
                continue;
            if (str.length() > 0)
                str.append(";");
            str.append(SM.getTemplate().getId()).append(",").append(SM.getXp());
        }
        return str.toString();
    }

    public int totalJobBasic() {
        int i = 0;

        for (OficioCaracteristicas SM : _metiers.values()) {
            // Si c'est un m�tier 'basic' :
            if (SM.getTemplate().getId() == 2 || SM.getTemplate().getId() == 11
                    || SM.getTemplate().getId() == 13
                    || SM.getTemplate().getId() == 14
                    || SM.getTemplate().getId() == 15
                    || SM.getTemplate().getId() == 16
                    || SM.getTemplate().getId() == 17
                    || SM.getTemplate().getId() == 18
                    || SM.getTemplate().getId() == 19
                    || SM.getTemplate().getId() == 20
                    || SM.getTemplate().getId() == 24
                    || SM.getTemplate().getId() == 25
                    || SM.getTemplate().getId() == 26
                    || SM.getTemplate().getId() == 27
                    || SM.getTemplate().getId() == 28
                    || SM.getTemplate().getId() == 31
                    || SM.getTemplate().getId() == 36
                    || SM.getTemplate().getId() == 41
                    || SM.getTemplate().getId() == 56
                    || SM.getTemplate().getId() == 58
                    || SM.getTemplate().getId() == 60
                    || SM.getTemplate().getId() == 65) {
                i++;
            }
        }
        return i;
    }

    public int totalJobFM() {
        int i = 0;

        for (OficioCaracteristicas SM : _metiers.values()) {
            // Si c'est une sp�cialisation 'FM' :
            if (SM.getTemplate().getId() == 43
                    || SM.getTemplate().getId() == 44
                    || SM.getTemplate().getId() == 45
                    || SM.getTemplate().getId() == 46
                    || SM.getTemplate().getId() == 47
                    || SM.getTemplate().getId() == 48
                    || SM.getTemplate().getId() == 49
                    || SM.getTemplate().getId() == 50
                    || SM.getTemplate().getId() == 62
                    || SM.getTemplate().getId() == 63
                    || SM.getTemplate().getId() == 64) {
                i++;
            }
        }
        return i;
    }

    public boolean canAggro() {
        return canAggro;
    }

    public void setCanAggro(boolean canAggro) {
        this.canAggro = canAggro;
    }

    public OficioCaracteristicas getMetierBySkill(int skID) {
        for (OficioCaracteristicas SM : _metiers.values())
            if (SM.isValidMapAction(skID))
                return SM;
        return null;
    }

    public String parseToFriendList(int guid) {
        StringBuilder str = new StringBuilder();
        str.append(";");
        str.append("?;");
        str.append(this.getName()).append(";");
        if (account.isFriendWith(guid)) {
            str.append(this.getLevel()).append(";");
            str.append(_align).append(";");
        } else {
            str.append("?;");
            str.append("-1;");
        }
        str.append(this.getClasse()).append(";");
        str.append(this.getSexe()).append(";");
        str.append(gfxId);
        return str.toString();
    }

    public String parseToEnemyList(int guid) {
        StringBuilder str = new StringBuilder();
        str.append(";");
        str.append("?;");
        str.append(this.getName()).append(";");
        if (account.isFriendWith(guid)) {
            str.append(this.getLevel()).append(";");
            str.append(_align).append(";");
        } else {
            str.append("?;");
            str.append("-1;");
        }
        str.append(this.getClasse()).append(";");
        str.append(this.getSexe()).append(";");
        str.append(gfxId);
        return str.toString();
    }

    public OficioCaracteristicas getMetierByID(int job) {
        for (OficioCaracteristicas SM : _metiers.values())
            if (SM.getTemplate().getId() == job)
                return SM;
        return null;
    }

    public boolean isOnMount() {
        return _onMount;
    }

    public void toogleOnMount() {
        if (_mount == null || this.isMorph() || this.getLevel() < 60)
            return;
        if (Configuracion.INSTANCE.getSubscription()) {
            GestorSalida.GAME_SEND_Im_PACKET(this, "1115");
            return;
        }
        if (this.getClasse() * 10 + this.getSexe() != this.getGfxId())
            return;
        if (this.getInHouse() != null) {
            GestorSalida.GAME_SEND_Im_PACKET(this, "1117");
            return;
        }
        if (!_onMount && _mount.isMontable() == 0) {
            GestorSalida.GAME_SEND_Re_PACKET(this, "Er", null);
            return;
        }

        if (_mount.getEnergy() < Formulas.calculEnergieLooseForToogleMount(_mount.getFatigue())) {
            GestorSalida.GAME_SEND_Im_PACKET(this, "1113");
            return;
        }

        if (!_onMount) {
            int EnergyoLose = _mount.getEnergy()
                    - Formulas.calculEnergieLooseForToogleMount(_mount.getFatigue());
            _mount.setEnergy(EnergyoLose);
        }

        _onMount = !_onMount;
        ObjetoJuego obj = getObjetByPos(Constantes.ITEM_POS_FAMILIER);

        if (_onMount && obj != null) {
            obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
            GestorSalida.GAME_SEND_OBJET_MOVE_PACKET(this, obj);
        }

        if (_mount.getEnergy() <= 0) {
            _mount.setEnergy(0);
            GestorSalida.GAME_SEND_Im_PACKET(this, "1114");
            return;
        }
        //on envoie les packets
        if (getPelea() != null && getPelea().getState() == 2) {
            GestorSalida.GAME_SEND_ALTER_FIGHTER_MOUNT(getPelea(), getPelea().getFighterByPerso(this), getId(), getPelea().getTeamId(getId()), getPelea().getOtherTeamId(getId()));
        } else {
            GestorSalida.GAME_SEND_ALTER_GM_PACKET(curMap, this);
        }
        GestorSalida.GAME_SEND_Re_PACKET(this, "+", _mount);
        GestorSalida.GAME_SEND_Rr_PACKET(this, _onMount ? "+" : "-");
        GestorSalida.GAME_SEND_STATS_PACKET(this);

    }

    public int getMountXpGive() {
        return _mountXpGive;
    }

    public Montura getMount() {
        return _mount;
    }

    public void setMount(Montura DD) {
        _mount = DD;
    }

    public void setMountGiveXp(int parseInt) {
        _mountXpGive = parseInt;
    }

    public void resetVars() {
        if (this.getExchangeAction() != null) {
            if (this.getExchangeAction().getValue() instanceof OficioAccion && ((OficioAccion) this.getExchangeAction().getValue()).getJobCraft() != null)
                ((OficioAccion) this.getExchangeAction().getValue()).getJobCraft().getJobAction().broke = true;
            this.setExchangeAction(null);
        }

        doAction = false;
        this.setGameAction(null);

        away = false;
        _emoteActive = 0;
        pelea = null;
        duelId = 0;
        ready = false;
        party = null;
        _inviting = 0;
        sitted = false;
        _onMount = false;
        _isClone = false;
        _isAbsent = false;
        _isInvisible = false;
        follower.clear();
        follow = null;
        _curHouse = null;
        isGhost = false;
        _livreArti = false;
        _spec = false;
        afterFight = false;
    }

    public void addChanel(String chan) {
        if (_canaux.contains(chan))
            return;
        _canaux += chan;
        GestorSalida.GAME_SEND_cC_PACKET(this, '+', chan);
    }

    public void removeChanel(String chan) {
        _canaux = _canaux.replace(chan, "");
        GestorSalida.GAME_SEND_cC_PACKET(this, '-', chan);
    }

    public void modifAlignement(int i) {
        _honor = 0;
        _deshonor = 0;
        _align = (byte) i;
        _aLvl = 1;
        GestorSalida.GAME_SEND_ZC_PACKET(this, i);
        GestorSalida.GAME_SEND_STATS_PACKET(this);
        if (getGuild() != null)
            Database.estaticos.getGuildMemberData().update(this);
    }

    public int getDeshonor() {
        return _deshonor;
    }

    public void setDeshonor(int deshonor) {
        _deshonor = deshonor;
    }

    public void setShowWings(boolean showWings) {
        _showWings = showWings;
    }

    public int get_honor() {
        return _honor;
    }

    public void set_honor(int honor) {
        _honor = honor;
    }

    public int getALvl() {
        return _aLvl;
    }

    public void setALvl(int a) {
        _aLvl = a;
    }

    public void toggleWings(char c) {
        if (_align == Constantes.ALINEAMIENTO_NEUTRAL)
            return;
        int hloose = _honor * 5 / 100;
        switch (c) {
            case '*' -> {
                GestorSalida.GAME_SEND_GIP_PACKET(this, hloose);
                return;
            }
            case '+' -> {
                setShowWings(true);
                GestorSalida.GAME_SEND_ALTER_GM_PACKET(this.curMap, this);
                Database.dinamicos.getPlayerData().update(this);
            }
            case '-' -> {
                setShowWings(false);
                _honor -= hloose;
                GestorSalida.GAME_SEND_ALTER_GM_PACKET(this.curMap, this);
                Database.dinamicos.getPlayerData().update(this);
            }
        }
        GestorSalida.GAME_SEND_STATS_PACKET(this);
    }

    public void addHonor(int winH) {
        if (_align == 0)
            return;
        int curGrade = getGrade();
        _honor += winH;
        if (_honor > 18000) _honor = 18000;
        GestorSalida.GAME_SEND_Im_PACKET(this, "080;" + winH);
        //Changement de grade
        if (getGrade() != curGrade) {
            GestorSalida.GAME_SEND_Im_PACKET(this, "082;" + getGrade());
        }
    }

    public void remHonor(int losePH) {
        if (_align == 0)
            return;
        int curGrade = getGrade();
        _honor -= losePH;
        GestorSalida.GAME_SEND_Im_PACKET(this, "081;" + losePH);
        //Changement de grade
        if (getGrade() != curGrade) {
            GestorSalida.GAME_SEND_Im_PACKET(this, "083;" + getGrade());
        }
    }

    public GremioMiembros getGuildMember() {
        return _guildMember;
    }

    public void setGuildMember(GremioMiembros _guild) {
        this._guildMember = _guild;
    }

    public int getAccID() {
        return _accID;
    }

    public String parseZaapList()//Pour le packet WC
    {
        String map = curMap.getId() + "";
        try {
            map = _savePos.split(",")[0];
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuilder str = new StringBuilder();
        str.append(map);

        int SubAreaID = curMap.getSubArea().area.getSuperArea();

        for (short i : _zaaps) {
            if (Mundo.mundo.getMap(i) == null)
                continue;
            if (Mundo.mundo.getMap(i).getSubArea().area.getSuperArea() != SubAreaID)
                continue;
            int cost = Formulas.calculZaapCost(curMap, Mundo.mundo.getMap(i));
            if (i == curMap.getId())
                cost = 0;
            str.append("|").append(i).append(";").append(cost);
        }
        return str.toString();
    }

    public String parsePrismesList() {
        String map = curMap.getId() + "";
        StringBuilder str = new StringBuilder(map + "");
        int SubAreaID = curMap.getSubArea().area.getSuperArea();
        for (Prisma Prisme : Mundo.mundo.AllPrisme()) {
            if (Prisme.getAlignement() != _align)
                continue;
            short MapID = Prisme.getMap();
            if (Mundo.mundo.getMap(MapID) == null)
                continue;
            if (Mundo.mundo.getMap(MapID).getSubArea().area.getSuperArea() != SubAreaID)
                continue;
            if (Prisme.getInFight() == 0 || Prisme.getInFight() == -2) {
                str.append("|").append(MapID).append(";*");
            } else {
                int costo = Formulas.calculZaapCost(curMap, Mundo.mundo.getMap(MapID));
                if (MapID == curMap.getId())
                    costo = 0;
                str.append("|").append(MapID).append(";").append(costo);
            }
        }
        return str.toString();
    }

    public void openZaapMenu() {
        if (this.pelea == null) {
            if (!verifOtomaiZaap())
                return;
            if (getDeshonor() >= 3) {
                GestorSalida.GAME_SEND_Im_PACKET(this, "183");
                return;
            }

            this.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.IN_ZAAPING, 0));
            verifAndAddZaap(curMap.getId());
            GestorSalida.GAME_SEND_WC_PACKET(this);
        }
    }

    public void verifAndAddZaap(short mapId) {
        if (!verifOtomaiZaap())
            return;
        if (!_zaaps.contains(mapId)) {
            _zaaps.add(mapId);
            GestorSalida.GAME_SEND_Im_PACKET(this, "024");
            Database.dinamicos.getPlayerData().update(this);
        }
    }

    public boolean verifOtomaiZaap() {
        return Configuracion.INSTANCE.getALL_ZAAP() || !(this.getCurMap().getId() == 10643 || this.getCurMap().getId() == 11210)
                || Mundo.mundo.getConditionManager().validConditions(this, "QT=231") && Mundo.mundo.getConditionManager().validConditions(this, "QT=232");
    }

    public void openPrismeMenu() {
        if (this.pelea == null) {
            if (getDeshonor() >= 3) {
                GestorSalida.GAME_SEND_Im_PACKET(this, "183");
                return;
            }

            this.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.IN_PRISM, 0));
            GestorSalida.SEND_Wp_MENU_Prisme(this);
        }
    }

    public void useZaap(short id) {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != AccionIntercambiar.IN_ZAAPING)
            return;//S'il n'a pas ouvert l'interface Zaap(hack?)
        if (pelea != null)
            return;//Si il combat
        if (!_zaaps.contains(id))
            return;//S'il n'a pas le zaap demand�(ne devrais pas arriver)
        int cost = Formulas.calculZaapCost(curMap, Mundo.mundo.getMap(id));
        if (kamas < cost)
            return;//S'il n'a pas les kamas (verif cot� client)
        short mapID = id;
        int SubAreaID = curMap.getSubArea().area.getSuperArea();
        int cellID = Mundo.mundo.getZaapCellIdByMapId(id);
        if (Mundo.mundo.getMap(mapID) == null) {
            JuegoServidor.a();
            GestorSalida.GAME_SEND_WUE_PACKET(this);
            return;
        }
        if (Mundo.mundo.getMap(mapID).getCase(cellID) == null) {
            JuegoServidor.a();
            GestorSalida.GAME_SEND_WUE_PACKET(this);
            return;
        }
        if (!Mundo.mundo.getMap(mapID).getCase(cellID).isWalkable(true)) {
            JuegoServidor.a();
            GestorSalida.GAME_SEND_WUE_PACKET(this);
            return;
        }
        if (Mundo.mundo.getMap(mapID).getSubArea().area.getSuperArea() != SubAreaID) {
            GestorSalida.GAME_SEND_WUE_PACKET(this);
            return;
        }
        if (id == 4263 && this.get_align() == 2)
            return;
        if (id == 5295 && this.get_align() == 1)
            return;
        kamas -= cost;
        teleport(mapID, cellID);
        GestorSalida.GAME_SEND_STATS_PACKET(this);//On envoie la perte de kamas
        GestorSalida.GAME_SEND_WV_PACKET(this);//On ferme l'interface Zaap
        this.setExchangeAction(null);
    }

    public void usePrisme(String packet) {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != AccionIntercambiar.IN_PRISM)
            return;
        int celdaID = 340;
        short MapID = 7411;
        for (Prisma Prisme : Mundo.mundo.AllPrisme()) {
            if (Prisme.getMap() == Short.parseShort(packet.substring(2))) {
                celdaID = Prisme.getCell();
                MapID = Prisme.getMap();
                break;
            }
        }
        int costo = Formulas.calculZaapCost(curMap, Mundo.mundo.getMap(MapID));
        if (MapID == curMap.getId())
            costo = 0;
        if (kamas < costo) {
            GestorSalida.GAME_SEND_MESSAGE(this, "Vous n'avez pas sufisamment de Kamas pour réaliser cette action.");
            return;
        }
        kamas -= costo;
        GestorSalida.GAME_SEND_STATS_PACKET(this);
        this.teleport(Short.parseShort(packet.substring(2)), celdaID);
        GestorSalida.SEND_Ww_CLOSE_Prisme(this);
        this.setExchangeAction(null);
    }

    public String parseZaaps() {
        StringBuilder str = new StringBuilder();
        boolean first = true;

        if (_zaaps.isEmpty())
            return "";
        for (int i : _zaaps) {
            if (!first)
                str.append(",");
            first = false;
            str.append(i);
        }
        return str.toString();
    }

    public String parsePrisme() {
        String str = "";
        Prisma Prisme = Mundo.mundo.getPrisme(curMap.getSubArea().prismId);
        if (Prisme == null)
            str = "-3";
        else if (Prisme.getInFight() == 0) {
            str = "0;" + Prisme.getTurnTime() + ";45000;7";
        } else {
            str = Prisme.getInFight() + "";
        }
        return str;
    }

    public void stopZaaping() {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != AccionIntercambiar.IN_ZAAPING)
            return;

        this.setExchangeAction(null);
        GestorSalida.GAME_SEND_WV_PACKET(this);
    }

    public void Zaapi_close() {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != AccionIntercambiar.IN_ZAPPI)
            return;
        this.setExchangeAction(null);
        GestorSalida.GAME_SEND_CLOSE_ZAAPI_PACKET(this);
    }

    public void Prisme_close() {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != AccionIntercambiar.IN_PRISM)
            return;
        this.setExchangeAction(null);
        GestorSalida.SEND_Ww_CLOSE_Prisme(this);
    }

    public void Zaapi_use(String packet) {
        if (this.getExchangeAction() == null || this.getExchangeAction().getType() != AccionIntercambiar.IN_ZAPPI)
            return;
        Mapa map = Mundo.mundo.getMap(Short.parseShort(packet.substring(2)));

        short cell = 100;
        if (map != null) {
            for (GameCase entry : map.getCases()) {
                ObjetosInteractivos obj = entry.getObject();
                if (obj != null) {
                    if (obj.getId() == 7031 || obj.getId() == 7030) {
                        cell = (short) (entry.getId() + 18);
                    }
                }
            }
            if (map.getSubArea() != null && (map.getSubArea().area.getId() == 7 || map.getSubArea().area.getId() == 11)) {
                int price = 20;
                if (this.get_align() == 1 || this.get_align() == 2)
                    price = 10;
                kamas -= price;
                GestorSalida.GAME_SEND_STATS_PACKET(this);
                if ((map.getSubArea().area.getId() == 7 && this.getCurMap().getSubArea().area.getId() == 7)
                        || (map.getSubArea().area.getId() == 11 && this.getCurMap().getSubArea().area.getId() == 11)) {
                    this.teleport(Short.parseShort(packet.substring(2)), cell);
                }
                GestorSalida.GAME_SEND_CLOSE_ZAAPI_PACKET(this);
                this.setExchangeAction(null);
            }
        }
    }

    public boolean hasItemTemplate(int i, int q) {
        for (ObjetoJuego obj : objects.values()) {
            if (obj.getPosicion() != Constantes.ITEM_POS_NO_EQUIPED)
                continue;
            if (obj.getModelo().getId() != i)
                continue;
            if (obj.getCantidad() >= q)
                return true;
        }
        return false;
    }

    public boolean hasItemType(int type) {
        for (ObjetoJuego obj : objects.values()) {
            if (obj.getPosicion() != Constantes.ITEM_POS_NO_EQUIPED)
                continue;
            if (obj.getModelo().getType() == type)
                return true;
        }

        return false;
    }

    public ObjetoJuego getItemTemplate(int i, int q) {
        for (ObjetoJuego obj : objects.values()) {
            if (obj.getPosicion() != Constantes.ITEM_POS_NO_EQUIPED)
                continue;
            if (obj.getModelo().getId() != i)
                continue;
            if (obj.getCantidad() >= q)
                return obj;
        }
        return null;
    }

    public ObjetoJuego getItemTemplate(int i) {

        for (ObjetoJuego obj : objects.values()) {
            if (obj.getModelo().getId() != i)
                continue;
            return obj;
        }

        return null;
    }

    public int getNbItemTemplate(int i) {
        for (ObjetoJuego obj : objects.values()) {
            if (obj.getModelo().getId() != i)
                continue;
            return obj.getCantidad();
        }
        return -1;
    }

    public boolean isDispo(Jugador sender) {
        return !_isAbsent && (!_isInvisible || account.isFriendWith(sender.getAccount().getId()));

    }

    public boolean get_isClone() {
        return _isClone;
    }

    public void set_isClone(boolean isClone) {
        _isClone = isClone;
    }

    public byte get_title() {
        return _title;
    }

    public void set_title(int i) {
        _title = (byte) i;
    }

    //FIN CLONAGE
    public void VerifAndChangeItemPlace() {
        boolean isFirstAM = true;
        boolean isFirstAN = true;
        boolean isFirstANb = true;
        boolean isFirstAR = true;
        boolean isFirstBO = true;
        boolean isFirstBOb = true;
        boolean isFirstCA = true;
        boolean isFirstCE = true;
        boolean isFirstCO = true;
        boolean isFirstDa = true;
        boolean isFirstDb = true;
        boolean isFirstDc = true;
        boolean isFirstDd = true;
        boolean isFirstDe = true;
        boolean isFirstDf = true;
        boolean isFirstFA = true;

        for (ObjetoJuego obj : objects.values()) {
            if (obj.getPosicion() == Constantes.ITEM_POS_NO_EQUIPED)
                continue;
            if (obj.getPosicion() == Constantes.ITEM_POS_AMULETTE) {
                if (isFirstAM) {
                    isFirstAM = false;
                } else {
                    obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosicion() == Constantes.ITEM_POS_ANNEAU1) {
                if (isFirstAN) {
                    isFirstAN = false;
                } else {
                    obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosicion() == Constantes.ITEM_POS_ANNEAU2) {
                if (isFirstANb) {
                    isFirstANb = false;
                } else {
                    obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosicion() == Constantes.ITEM_POS_ARME) {
                if (isFirstAR) {
                    isFirstAR = false;
                } else {
                    obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosicion() == Constantes.ITEM_POS_BOTTES) {
                if (isFirstBO) {
                    isFirstBO = false;
                } else {
                    obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosicion() == Constantes.ITEM_POS_BOUCLIER) {
                if (isFirstBOb) {
                    isFirstBOb = false;
                } else {
                    obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosicion() == Constantes.ITEM_POS_CAPE) {
                if (isFirstCA) {
                    isFirstCA = false;
                } else {
                    obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosicion() == Constantes.ITEM_POS_CEINTURE) {
                if (isFirstCE) {
                    isFirstCE = false;
                } else {
                    obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosicion() == Constantes.ITEM_POS_COIFFE) {
                if (isFirstCO) {
                    isFirstCO = false;
                } else {
                    obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosicion() == Constantes.ITEM_POS_DOFUS1) {
                if (isFirstDa) {
                    isFirstDa = false;
                } else {
                    obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosicion() == Constantes.ITEM_POS_DOFUS2) {
                if (isFirstDb) {
                    isFirstDb = false;
                } else {
                    obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosicion() == Constantes.ITEM_POS_DOFUS3) {
                if (isFirstDc) {
                    isFirstDc = false;
                } else {
                    obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosicion() == Constantes.ITEM_POS_DOFUS4) {
                if (isFirstDd) {
                    isFirstDd = false;
                } else {
                    obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosicion() == Constantes.ITEM_POS_DOFUS5) {
                if (isFirstDe) {
                    isFirstDe = false;
                } else {
                    obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosicion() == Constantes.ITEM_POS_DOFUS6) {
                if (isFirstDf) {
                    isFirstDf = false;
                } else {
                    obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                }
            } else if (obj.getPosicion() == Constantes.ITEM_POS_FAMILIER) {
                if (isFirstFA) {
                    isFirstFA = false;
                } else {
                    obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                }
            }
        }
    }

    //Mariage

    public Stalk get_traque() {
        return _traqued;
    }

    public void set_traque(Stalk traq) {
        _traqued = traq;
    }

    public void setWife(int id) {
        this.wife = id;
        Database.dinamicos.getPlayerData().update(this);
    }

    public String get_wife_friendlist() {
        Jugador wife = Mundo.mundo.getPlayer(this.wife);
        StringBuilder str = new StringBuilder();
        if (wife != null) {
            int color1 = wife.getColor1(), color2 = wife.getColor2(), color3 = wife.getColor3();
            if (wife.getObjetByPos(Constantes.ITEM_POS_MALEDICTION) != null)
                if (wife.getObjetByPos(Constantes.ITEM_POS_MALEDICTION).getModelo().getId() == 10838) {
                    color1 = 16342021;
                    color2 = 16342021;
                    color3 = 16342021;
                }
            str.append(wife.getName()).append("|").append(wife.getGfxId()).append("|").append(color1).append("|").append(color2).append("|").append(color3).append("|");
            if (!wife.isOnline()) {
                str.append("|");
            } else {
                str.append(wife.parse_towife()).append("|");
            }
        } else {
            str.append("|");
        }
        return str.toString();
    }

    public String parse_towife() {
        int f = 0;
        if (pelea != null) {
            f = 1;
        }
        return curMap.getId() + "|" + this.getLevel() + "|" + f;
    }

    public void meetWife(Jugador p)// Se teleporter selon les sacro-saintes autorisations du mariage.
    {
        if (p == null)
            return; // Ne devrait theoriquement jamais se produire.

        if (this.getPodUsed() >= this.getMaximosPods()) // Refuser la t�l�portation si on est full pods.
        {
            GestorSalida.GAME_SEND_Im_PACKET(this, "170");
            return;
        }

        int dist = (curMap.getX() - p.getCurMap().getX())
                * (curMap.getX() - p.getCurMap().getX())
                + (curMap.getY() - p.getCurMap().getY())
                * (curMap.getY() - p.getCurMap().getY());
        if (dist > 100 || p.getCurMap().getId() == this.getCurMap().getId())// La distance est trop grande...
        {
            if (p.getSexe() == 0)
                GestorSalida.GAME_SEND_Im_PACKET(this, "178");
            else
                GestorSalida.GAME_SEND_Im_PACKET(this, "179");
            return;
        }

        int cellPositiontoadd = Constantes.getNearestCellIdUnused(p);
        if (cellPositiontoadd == -1) {
            if (p.getSexe() == 0)
                GestorSalida.GAME_SEND_Im_PACKET(this, "141");
            else
                GestorSalida.GAME_SEND_Im_PACKET(this, "142");
            return;
        }

        teleport(p.getCurMap().getId(), cellPositiontoadd);
    }

    public void Divorce() {
        if (isOnline())
            GestorSalida.GAME_SEND_Im_PACKET(this, "047;"
                    + Mundo.mundo.getPlayer(wife).getName());

        wife = 0;
        Database.dinamicos.getPlayerData().update(this);
    }

    public int getWife() {
        return wife;
    }

    public void setisOK(int ok) {
        _isOK = ok;
    }

    public int getisOK() {
        return _isOK;
    }

    public List<ObjetoJuego> getEquippedObjects() {
        List<ObjetoJuego> objects = new ArrayList<>();
        this.objects.values().stream().filter(object -> object.getPosicion() != -1 && object.getPosicion() < 34).forEach(objects::add);
        return objects;
    }

    public void changeOrientation(int toOrientation) {
        if (this.get_orientation() == 0 || this.get_orientation() == 2
                || this.get_orientation() == 4 || this.get_orientation() == 6) {
            this.set_orientation(toOrientation);
            GestorSalida.GAME_SEND_eD_PACKET_TO_MAP(getCurMap(), this.getId(), toOrientation);
        }
    }

    /**
     * Heroic
     **/
    private byte dead = 0, deathCount = 0, deadType = 0;
    private long deadTime = 0, killByTypeId = 0, totalKills = 0;

    public byte isDead() {
        return dead;
    }

    public byte getDeathCount() {
        return deathCount;
    }

    public void increaseTotalKills() {
        this.totalKills++;
    }

    public long getTotalKills() {
        return totalKills;
    }

    public String getDeathInformation() {
        return dead + "," + deadTime + "," + deadType + "," + killByTypeId;
    }

    public void die(byte type, long id) {
        new ArrayList<>(this.getItems().values()).stream().filter(Objects::nonNull).forEach(object -> this.removeItem(object.getId(), object.getCantidad(), true, false));
        this.setFuneral();
        this.deathCount++;
        this.deadType = type;
        this.killByTypeId = id;
    }

    public void revive() {
        byte revive = Database.dinamicos.getPlayerData().canRevive(this);

        if (revive == 1) {
            this.curMap = Mundo.mundo.getMap((short) 7411);
            this.curCell = Mundo.mundo.getMap((short) 7411).getCase(311);
        } else {
            this.getCaracteristicas().addOneStat(125, -this.getCaracteristicas().getEffect(125));
            this.getCaracteristicas().addOneStat(124, -this.getCaracteristicas().getEffect(124));
            this.getCaracteristicas().addOneStat(118, -this.getCaracteristicas().getEffect(118));
            this.getCaracteristicas().addOneStat(123, -this.getCaracteristicas().getEffect(123));
            this.getCaracteristicas().addOneStat(119, -this.getCaracteristicas().getEffect(119));
            this.getCaracteristicas().addOneStat(126, -this.getCaracteristicas().getEffect(126));
            this.addCapital((this.getLevel() - 1) * 5 - this.get_capital());
            this.getStatsParcho().getEffects().clear();
            this._sorts = Constantes.getStartSorts(classe);
            this._sortsPlaces = Constantes.getStartSortsPlaces(classe);
            this.level = 1;
            this.exp = 0;
            this.curMap = Mundo.mundo.getMap(Constantes.getStartMap(this.classe));
            this.curCell = this.curMap.getCase(Constantes.getStartCell(this.classe));
        }
        this._honor = 0;
        this._deshonor = 0;
        this._align = 0;
        this.kamas = 0;
        this._metiers.clear();
        if (this._mount != null) {
            for (ObjetoJuego gameObject : this._mount.getObjects().values())
                Mundo.mundo.removeGameObject(gameObject.getId());
            this._mount.getObjects().clear();

            this.setMount(null);
            this.setMountGiveXp(0);
        }
        this.isGhost = false;
        this.dead = 0;
        this.setEnergy(10000);
        this.setGfxId(Integer.parseInt(this.getClasse() + "" + this.getSexe()));
        this.setCanAggro(true);
        this.setAway(false);
        this.setSpeed(0);

        Database.dinamicos.getPlayerData().setRevive(this);
    }

    /**
     * End heroic
     **/

    public boolean isGhost() {
        return this.isGhost;
    }

    public void setFuneral() {
        this.dead = 1;
        this.deadTime = Instant.now().toEpochMilli();
        this.setEnergy(-1);
        if (this.isOnMount())
            this.toogleOnMount();
        if (this.get_orientation() == 2) {
            this.set_orientation(1);
            GestorSalida.GAME_SEND_eD_PACKET_TO_MAP(this.getCurMap(), this.getId(), 1);
        }
        this.setGfxId(Integer.parseInt(this.getClasse() + "3"));
        GestorSalida.send(this, "AR3K");//Block l'orientation
        GestorSalida.send(this, "M112");//T'es mort!!! t'es mort!!! Mouhhahahahahaaaarg
        GestorSalida.GAME_SEND_ALTER_GM_PACKET(getCurMap(), this);
    }

    public void setGhost() {
        if (isOnMount())
            toogleOnMount();
        if (Configuracion.INSTANCE.getHEROIC()) {
            this.setGfxId(Integer.parseInt(this.getClasse() + "" + this.getSexe()));
            this.send("GO");
            return;
        }
        if (this.getEnergy() != 0)
            Constantes.tpCim(this.getCurMap().getSubArea().area.getId(), this);
        this.dead = 0;
        this.isGhost = true;
        this.setEnergy(0);
        setGfxId(8004);
        setCanAggro(false);
        setAway(true);
        setSpeed(-40);
        this.regenRate = 0;
        GestorSalida.send(this, "IH" + Constantes.TODOS_LOS_FENIX);
    }

    public void setAlive() {
        if (!this.isGhost)
            return;
        this.isGhost = false;
        this.dead = 0;
        this.setEnergy(1000);
        this.setPdv(1);
        this.setGfxId(Integer.parseInt(this.getClasse() + "" + this.getSexe()));
        this.setCanAggro(true);
        this.setAway(false);
        this.setSpeed(0);
        GestorSalida.GAME_SEND_MESSAGE(this, "Tu as gagné <b>1000</b> points d'énergie.", "009900");
        GestorSalida.GAME_SEND_STATS_PACKET(this);
        GestorSalida.GAME_SEND_ALTER_GM_PACKET(this.curMap, this);
        GestorSalida.send(this, "IH");
        GestorSalida.send(this, "AR6bk");//Block l'orientation
    }

    public Map<Integer, Integer> getStoreItems() {
        return _storeItems;
    }

    public int needEndFight() {
        return hasEndFight;
    }

    public Monstruos.MobGroup hasMobGroup() {
        return hasMobGroup;
    }

    public void setNeededEndFight(int hasEndFight, Monstruos.MobGroup group) {
        this.endFightAction = null;
        this.hasEndFight = hasEndFight;
        this.hasMobGroup = group;
    }

    public void setNeededEndFightAction(Accion endFightAction) {
        this.hasEndFight = -2;
        this.endFightAction = endFightAction;
    }

    public boolean castEndFightAction() {
        if (this.endFightAction != null) {
            this.endFightAction.apply(this, null, -1, -1);
            this.endFightAction = null;
        } else
            return true;
        return false;
    }

    public String parseStoreItemsList() {
        StringBuilder list = new StringBuilder();
        if (_storeItems.isEmpty())
            return "";
        for (Entry<Integer, Integer> obj : _storeItems.entrySet()) {
            ObjetoJuego O = Mundo.getGameObject(obj.getKey());
            if (O == null)
                continue;
            //O.getPoidOfBaseItem(O.getPlayerId());
            list.append(O.getId()).append(";").append(O.getCantidad()).append(";").append(O.getModelo().getId()).append(";").append(O.parseStatsString()).append(";").append(obj.getValue()).append("|");
        }

        return (list.length() > 0 ? list.toString().substring(0, list.length() - 1) : list.toString());
    }

    public int parseStoreItemsListPods() {
        if (_storeItems.isEmpty())
            return 0;
        int total = 0;
        for (Entry<Integer, Integer> obj : _storeItems.entrySet()) {
            ObjetoJuego O = Mundo.getGameObject(obj.getKey());
            if (O != null) {
                int qua = O.getCantidad();
                int poidBase1 = O.getModelo().getPod() * qua;
                total += poidBase1;
            }
        }
        return total;
    }

    public String parseStoreItemstoBD() {
        StringBuilder str = new StringBuilder();
        for (Entry<Integer, Integer> _storeObjets : _storeItems.entrySet()) {
            str.append(_storeObjets.getKey()).append(",").append(_storeObjets.getValue()).append("|");
        }

        return str.toString();
    }

    public void addInStore(int ObjID, int price, int qua) {
        ObjetoJuego PersoObj = Mundo.getGameObject(ObjID);
        //Si le joueur n'a pas l'item dans son sac ...
        if (PersoObj.isAttach()) return;
        if (_storeItems.get(ObjID) != null) {
            _storeItems.remove(ObjID);
            _storeItems.put(ObjID, price);
            GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
            return;
        }

        if (objects.get(ObjID) == null) {
            JuegoServidor.a();
            return;
        }

        //Si c'est un item �quip� ...
        if (PersoObj.getPosicion() != Constantes.ITEM_POS_NO_EQUIPED)
            return;

        ObjetoJuego SimilarObj = getSimilarStoreItem(PersoObj);
        int newQua = PersoObj.getCantidad() - qua;
        if (SimilarObj == null)//S'il n'y pas d'item du meme Template
        {
            //S'il ne reste pas d'item dans le sac
            if (newQua <= 0) {
                //On enleve l'objet du sac du joueur
                removeItem(PersoObj.getId());
                //On met l'objet du sac dans le store, avec la meme quantit�
                _storeItems.put(PersoObj.getId(), price);
                GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, PersoObj.getId());
                GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
            } else
            //S'il reste des objets au joueur
            {
                //on modifie la quantit� d'item du sac
                PersoObj.setCantidad(newQua);
                //On ajoute l'objet a la banque et au monde
                SimilarObj = ObjetoJuego.getCloneObjet(PersoObj, qua);
                Mundo.addGameObject(SimilarObj, true);
                _storeItems.put(SimilarObj.getId(), price);

                //Envoie des packets
                GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);

            }
        } else
        // S'il y avait un item du meme template
        {
            //S'il ne reste pas d'item dans le sac
            if (newQua <= 0) {
                //On enleve l'objet du sac du joueur
                removeItem(PersoObj.getId());
                //On enleve l'objet du monde
                Mundo.mundo.removeGameObject(PersoObj.getId());
                //On ajoute la quantit� a l'objet en banque
                SimilarObj.setCantidad(SimilarObj.getCantidad() + PersoObj.getCantidad());

                _storeItems.remove(SimilarObj.getId());
                _storeItems.put(SimilarObj.getId(), price);

                //on envoie l'ajout a la banque de l'objet
                GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
                //on envoie la supression de l'objet du sac au joueur
                GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, PersoObj.getId());
            } else
            //S'il restait des objets
            {
                //on modifie la quantit� d'item du sac
                PersoObj.setCantidad(newQua);
                SimilarObj.setCantidad(SimilarObj.getCantidad() + qua);

                _storeItems.remove(SimilarObj.getId());
                _storeItems.put(SimilarObj.getId(), price);

                GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);

            }
        }
        GestorSalida.GAME_SEND_Ow_PACKET(this);
        Database.dinamicos.getPlayerData().update(this);
    }

    private ObjetoJuego getSimilarStoreItem(ObjetoJuego exGameObject) {
        for (Integer id : _storeItems.keySet()) {
            ObjetoJuego gameObject = Mundo.getGameObject(id);
            if (Mundo.mundo.getConditionManager().stackIfSimilar(gameObject, exGameObject, true))
                return gameObject;
        }

        return null;
    }

    public void removeFromStore(int guid, int qua) {
        ObjetoJuego SimilarObj = Mundo.getGameObject(guid);
        //Si le joueur n'a pas l'item dans son store ...
        if (_storeItems.get(guid) == null) {
            JuegoServidor.a();
            return;
        }

        ObjetoJuego PersoObj = getSimilarItem(SimilarObj);
        int newQua = SimilarObj.getCantidad() - qua;
        if (PersoObj == null)//Si le joueur n'avait aucun item similaire
        {
            //S'il ne reste rien en store
            if (newQua <= 0) {
                //On retire l'item du store
                _storeItems.remove(guid);
                //On l'ajoute au joueur
                objects.put(guid, SimilarObj);

                //On envoie les packets
                GestorSalida.GAME_SEND_OAKO_PACKET(this, SimilarObj);
                GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
            }
        } else {
            //S'il ne reste rien en store
            if (newQua <= 0) {
                //On retire l'item de la banque
                _storeItems.remove(SimilarObj.getId());
                Mundo.mundo.removeGameObject(SimilarObj.getId());
                //On Modifie la quantit� de l'item du sac du joueur
                PersoObj.setCantidad(PersoObj.getCantidad()
                        + SimilarObj.getCantidad());
                //On envoie les packets
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
                GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
            }
        }
        GestorSalida.GAME_SEND_Ow_PACKET(this);
        Database.dinamicos.getPlayerData().update(this);
    }

    public void removeStoreItem(int guid) {
        _storeItems.remove(guid);
    }

    public void addStoreItem(int guid, int price) {
        _storeItems.put(guid, price);
    }

    public int getSpeed() {
        return _Speed;
    }

    public void setSpeed(int _Speed) {
        this._Speed = _Speed;
    }

    public int get_savestat() {
        return this.savestat;
    }

    public void set_savestat(int stat) {
        this.savestat = stat;
    }

    public boolean getMetierPublic() {
        return _metierPublic;
    }

    public void setMetierPublic(boolean b) {
        _metierPublic = b;
    }

    public boolean getLivreArtisant() {
        return _livreArti;
    }

    public void setLivreArtisant(boolean b) {
        _livreArti = b;
    }

    public boolean hasSpell(int spellID) {
        return (getSortStatBySortIfHas(spellID) != null);
    }

    public void leaveEnnemyFaction() {
        if (!isInEnnemyFaction)
            return;//pas en prison on fait pas la commande
        int pGrade = this.getGrade();
        long compar = Instant.now().toEpochMilli()
                - (enteredOnEnnemyFaction + 60000 * pGrade);

        switch (pGrade) {
            case 1:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'être libérer de prison après 1 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore " + restant / 1000 + " secondes en prison.");
                }
                break;
            case 2:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 2 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore " + restant / 1000 + " secondes en prison.");
                }
                break;
            case 3:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 3 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore "
                            + restant / 1000 + " secondes en prison.");
                }
                break;
            case 4:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 4 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore "
                            + restant / 1000 + " secondes en prison.");
                }
                break;
            case 5:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 5 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore "
                            + restant / 1000 + " secondes en prison.");
                }
                break;
            case 6:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 6 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore "
                            + restant / 1000 + " secondes en prison.");
                }
                break;
            case 7:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 7 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore "
                            + restant / 1000 + " secondes en prison.");
                }
                break;
            case 8:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 8 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore "
                            + restant / 1000 + " secondes en prison.");
                }
                break;
            case 9:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 9 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore "
                            + restant / 1000 + " secondes en prison.");
                }
                break;
            case 10:
                if (compar >= 0) {
                    leaveFaction();
                    this.sendMessage("Vous venez d'�tre lib�r� de prison apr�s 10 minutes d'attente.");
                } else {
                    long restant = -compar;
                    if (restant <= 1000)
                        restant = 1000;
                    this.sendMessage("Vous devez attendre encore "
                            + restant / 1000 + " secondes en prison.");
                }
                break;
        }
        Database.dinamicos.getPlayerData().update(this);
    }

    public void leaveEnnemyFactionAndPay(Jugador perso) {
        if (!isInEnnemyFaction)
            return;//pas en prison on fait pas la commande
        int pGrade = perso.getGrade();
        long curKamas = perso.getKamas();
        switch (pGrade) {
            case 1:
                if (curKamas < 1000) {
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 1000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 2:
                if (curKamas < 2000) {
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 2000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 3:
                if (curKamas < 3000) {
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 3000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 4:
                if (curKamas < 4000) {
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 4000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 5:
                if (curKamas < 5000) {
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 5000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 6:
                if (curKamas < 7000) {
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 7000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 7:
                if (curKamas < 9000) {
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 9000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 8:
                if (curKamas < 12000) {
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 12000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 9:
                if (curKamas < 16000) {
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 16000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
            case 10:
                if (curKamas < 25000) {
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu ne possédes que "
                            + curKamas
                            + "Kamas. Tu n'as pas assez d'argent pour sortir !", "009900");
                } else {
                    int countKamas = 25000;
                    long newKamas = curKamas - countKamas;
                    if (newKamas < 0)
                        newKamas = 0;
                    perso.setKamas(newKamas);
                    leaveFaction();
                    GestorSalida.GAME_SEND_MESSAGE(perso, "Tu viens de payer "
                            + countKamas
                            + "Kamas pour sortir. Il te reste maintenant "
                            + newKamas + "Kamas.", "009900");
                }
                break;
        }
        Database.dinamicos.getPlayerData().update(this);
        GestorSalida.GAME_SEND_STATS_PACKET(perso);
    }

    public void leaveFaction() {
        try {
            isInEnnemyFaction = false;
            enteredOnEnnemyFaction = 0;
            warpToSavePos();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void teleportWithoutBlocked(short newMapID, int newCellID)//Aucune condition genre <<en_prison>> etc
    {
        JuegoCliente PW = null;
        if (account.getGameClient() != null) {
            PW = account.getGameClient();
        }
        if (Mundo.mundo.getMap(newMapID) == null) {
            JuegoServidor.a();
            return;
        }
        if (Mundo.mundo.getMap(newMapID).getCase(newCellID) == null) {
            JuegoServidor.a();
            return;
        }
        if (PW != null) {
            GestorSalida.GAME_SEND_GA2_PACKET(PW, this.getId());
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(curMap, this.getId());
        }
        curCell.removePlayer(this);
        curMap = Mundo.mundo.getMap(newMapID);
        curCell = curMap.getCase(newCellID);

        //Verification de la Map
        //Verifier la validit� du mountpark
        if (curMap.getMountPark() != null
                && curMap.getMountPark().getOwner() > 0
                && curMap.getMountPark().getGuild().getId() != -1) {
            if (Mundo.mundo.getGuild(curMap.getMountPark().getGuild().getId()) == null)//Ne devrait pas arriver
            {
                JuegoServidor.a();
                Mapa.removeMountPark(curMap.getMountPark().getGuild().getId());
            }
        }
        //Verifier la validit� du Collector
        if (Recaudador.getCollectorByMapId(curMap.getId()) != null) {
            if (Mundo.mundo.getGuild(Objects.requireNonNull(Recaudador.getCollectorByMapId(curMap.getId())).getGuildId()) == null)//Ne devrait pas arriver
            {
                JuegoServidor.a();
                Recaudador.removeCollector(Objects.requireNonNull(Recaudador.getCollectorByMapId(curMap.getId())).getGuildId());
            }
        }

        if (PW != null) {
            GestorSalida.GAME_SEND_MAPDATA(PW, newMapID, curMap.getDate(), curMap.getKey());
            curMap.addPlayer(this);
        }

        if (!follower.isEmpty())//On met a jour la Map des personnages qui nous suivent
        {
            for (Jugador t : follower.values()) {
                if (t.isOnline())
                    GestorSalida.GAME_SEND_FLAG_PACKET(t, this);
                else
                    follower.remove(t.getId());
            }
        }
    }

    public void teleportFaction(int factionEnnemy) {
        short mapID = 0;
        int cellID = 0;
        enteredOnEnnemyFaction = Instant.now().toEpochMilli();
        isInEnnemyFaction = true;

        switch (factionEnnemy) {
//bonta
            case 1 -> {
                mapID = (short) 6164;
                cellID = 236;
            }
//brakmar
            case 2 -> {
                mapID = (short) 6171;
                cellID = 397;
            }
//Seriane
            case 3 -> {
                mapID = (short) 1002;
                cellID = 326;
            }
//neutre(WTF? XD)
            default -> {
                mapID = (short) 8534;
                cellID = 297;
            }
        }
        this.sendMessage("Vous êtes en prison !<br />\nVous devrez donc patientez quelques Minutes avant de pouvoir sortir.<br/>\nParlez au gardien de prison pour obtenir plus d'information.");
        if (this.getEnergy() <= 0) {
            if (isOnMount())
                toogleOnMount();
            this.isGhost = true;
            setGfxId(8004);
            setCanAggro(false);
            setAway(true);
            setSpeed(-40);
        }
        teleportWithoutBlocked(mapID, cellID);
        Database.dinamicos.getPlayerData().update(this);
    }

    public String parsecolortomount() {
        int color1 = this.getColor1(), color2 = this.getColor2(), color3 = this.getColor3();
        if (this.getObjetByPos(Constantes.ITEM_POS_MALEDICTION) != null)
            if (this.getObjetByPos(Constantes.ITEM_POS_MALEDICTION).getModelo().getId() == 10838) {
                color1 = 16342021;
                color2 = 16342021;
                color3 = 16342021;
            }
        return (color1 == -1 ? "" : Integer.toHexString(color1)) + ","
                + (color2 == -1 ? "" : Integer.toHexString(color2)) + ","
                + (color3 == -1 ? "" : Integer.toHexString(color3));
    }

    public boolean addObjetSimiler(ObjetoJuego objet, boolean hasSimiler, int oldID) {
        ObjetoModelo objModelo = objet.getModelo();
        if (objModelo.getType() == 85 || objModelo.getType() == 18)
            return false;
        if (hasSimiler) {
            for (Entry<Integer, ObjetoJuego> entry : objects.entrySet()) {
                ObjetoJuego obj = entry.getValue();
                if (obj.getPosicion() == -1 && obj.getId() != oldID
                        && obj.getModelo().getId() == objModelo.getId()
                        && obj.getCaracteristicas().isSameStats(objet.getCaracteristicas())
                        && Mundo.mundo.getConditionManager().stackIfSimilar(obj, objet, hasSimiler)) {
                    obj.setCantidad(obj.getCantidad() + objet.getCantidad());
                    GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
                    return true;
                }
            }
        }
        return false;
    }

    //region Objects class
    public Map<Integer, Doble<Integer, Integer>> getObjectsClassSpell() {
        return objectsClassSpell;
    }

    public void addObjectClassSpell(int spell, int effect, int value) {
        if (!objectsClassSpell.containsKey(spell)) {
            objectsClassSpell.put(spell, new Doble<>(effect, value));
        }
    }

    public void removeObjectClassSpell(int spell) {
        if (objectsClassSpell.containsKey(spell)) {
            objectsClassSpell.remove(spell);
        }
    }

    public void addObjectClass(int item) {
        if (!objectsClass.contains(item))
            objectsClass.add(item);
    }

    public void removeObjectClass(int item) {
        if (objectsClass.contains(item)) {
            int index = objectsClass.indexOf(item);
            objectsClass.remove(index);
        }
    }

    public void refreshObjectsClass() {
        for (int position = 2; position < 8; position++) {
            ObjetoJuego object = getObjetByPos(position);

            if (object != null) {
                ObjetoModelo template = object.getModelo();
                int set = object.getModelo().getPanoId();

                if (template != null && set >= 81 && set <= 92) {
                    String[] stats = object.getModelo().getStrTemplate().split(",");
                    for (String stat : stats) {
                        String[] split = stat.split("#");
                        int effect = Integer.parseInt(split[0], 16), spell = Integer.parseInt(split[1], 16);
                        int value = Integer.parseInt(split[3], 16);
                        if (effect == 289)
                            value = 1;
                        GestorSalida.SEND_SB_SPELL_BOOST(this, effect + ";" + spell + ";" + value);
                        addObjectClassSpell(spell, effect, value);
                    }

                    if (!this.objectsClass.contains(template.getId()))
                        this.objectsClass.add(template.getId());
                }
            }
        }
    }

    public int getValueOfClassObject(int spell, int effect) {
        if (this.objectsClassSpell.containsKey(spell)) {
            if (this.objectsClassSpell.get(spell).getPrimero() == effect) {
                return this.objectsClassSpell.get(spell).getSegundo();
            }
        }
        return 0;
    }
    //endregion

    public int storeAllBuy() {
        int total = 0;
        for (Entry<Integer, Integer> value : _storeItems.entrySet()) {
            ObjetoJuego O = Mundo.getGameObject(value.getKey());
            int multiple = O.getCantidad();
            int add = value.getValue() * multiple;
            total += add;
        }

        return total;
    }

    public void DialogTimer() {
        Temporizador.addSiguiente(() -> {
            if (this.getExchangeAction() == null || this.getExchangeAction().getType() != AccionIntercambiar.TRADING_WITH_COLLECTOR)
                return;
            if ((Integer) this.getExchangeAction().getValue() != 0) {
                Recaudador collector = Mundo.mundo.getCollector((Integer) this.getExchangeAction().getValue());
                if (collector == null)
                    return;
                collector.reloadTimer();
                for (Jugador z : Mundo.mundo.getGuild(collector.getGuildId()).getPlayers()) {
                    if (z == null)
                        continue;
                    if (z.isOnline()) {
                        GestorSalida.GAME_SEND_gITM_PACKET(z, Recaudador.parseToGuild(z.getGuild().getId()));
                        String str = "G" + collector.getFullName() + "|.|" + Mundo.mundo.getMap(collector.getMap()).getX() + "|" + Mundo.mundo.getMap(collector.getMap()).getY() + "|" + getName() + "|" + collector.getXp() + ";";

                        if (!collector.getLogObjects().equals(""))
                            str += collector.getLogObjects();

                        Jugador.this.getGuildMember().giveXpToGuild(collector.getXp());
                        GestorSalida.GAME_SEND_gT_PACKET(z, str);
                    }
                }
                getCurMap().RemoveNpc(collector.getId());
                GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(getCurMap(), collector.getId());
                collector.delCollector(collector.getId());
                Database.estaticos.getCollectorData().delete(collector.getId());
            }
            Database.dinamicos.getPlayerData().update(getAccount().getCurrentPlayer());
            GestorSalida.GAME_SEND_EV_PACKET(getGameClient());
            setAway(false);
        }, 5, TimeUnit.MINUTES, Temporizador.DataType.CLIENTE);
    }

    public long getTimeTaverne() {
        return timeTaverne;
    }

    public void setTimeTaverne(long timeTaverne) {
        this.timeTaverne = timeTaverne;
        Database.dinamicos.getPlayerData().updateTimeTaverne(this);
    }

    public AccionJuego getGameAction() {
        return _gameAction;
    }

    public void setGameAction(AccionJuego Action) {
        _gameAction = Action;
    }

    public int getAlignMap() {
        if (this.getCurMap().getSubArea() == null)
            return -1;
        if (this.getCurMap().getSubArea().getAlignement() == 0)
            return 1;
        if (this.getCurMap().getSubArea().getAlignement() == this.get_align())
            return 1;
        return -1;
    }

    public List<Integer> getEmotes() {
        return emotes;
    }

    public void addStaticEmote(int emote) {
        if (this.emotes.contains(emote))
            return;
        this.emotes.add(emote);
        if (!isOnline())
            return;
        GestorSalida.GAME_SEND_EMOTE_LIST(this, getCompiledEmote(getEmotes()));
        GestorSalida.GAME_SEND_STATS_PACKET(this);
        GestorSalida.send(this, "eA" + emote);
    }

    public String parseEmoteToDB() {
        StringBuilder str = new StringBuilder();
        boolean isFirst = true;
        for (int i : emotes) {
            if (isFirst)
                str.append(i).append("");
            else
                str.append(";").append(i);
            isFirst = false;
        }
        return str.toString();
    }

    public boolean getBlockMovement() {
        return this.isBlocked;
    }

    public void setBlockMovement(boolean b) {
        this.isBlocked = b;
    }

    public JuegoCliente getGameClient() {
        return this.getAccount().getGameClient();
    }

    public void send(String packet) {
        GestorSalida.send(this, packet);
    }

    public void sendMessage(String msg) {
        GestorSalida.GAME_SEND_MESSAGE(this, msg);
    }

    public void sendTypeMessage(String name, String msg) {
        this.send("Im116;(<b>" + name + "</b>)~" + msg);
    }

    public void sendServerMessage(String msg) {
        this.sendTypeMessage("Server", msg);
    }

    public boolean isSubscribe() {
        return !Configuracion.INSTANCE.getSubscription() || this.getAccount().isSubscribe();
    }

    public boolean isInAreaNotSubscribe() {
        boolean ok = Configuracion.INSTANCE.getSubscription();

        if (this.curMap == null)
            return false;
        switch (this.curMap.getId()) {
            case 6824:
            case 6825:
            case 6826:
                return false;
        }
        if (this.curMap.getSubArea() == null)
            return false;
        if (this.curMap.getSubArea().area == null)
            return false;
        if (this.curMap.getSubArea().area.getSuperArea() == 3
                || this.curMap.getSubArea().area.getSuperArea() == 4
                || this.curMap.getSubArea().area.getId() == 18)
            ok = false;

        return ok;
    }

    public boolean cantDefie() {
        return getCurMap().noDefie;
    }

    public boolean cantAgro() {
        return getCurMap().noAgro;
    }

    public boolean cantCanal() {
        return getCurMap().noCanal;
    }

    public boolean cantTP() {
        return this.isInPrison() || getCurMap().noTP || GestorEvento.isInEvent(this);
    }

    public boolean isInPrison() {
        if (this.curMap == null)
            return false;

        return switch (this.curMap.getId()) {
            case 666, 8726 -> true;
            default -> false;
        };
    }

    public void addQuestPerso(MisionJugador qPerso) {
        questList.put(qPerso.getId(), qPerso);
    }

    public void delQuestPerso(int key) {
        this.questList.remove(key);
    }

    public Map<Integer, MisionJugador> getQuestPerso() {
        return questList;
    }

    public MisionJugador getQuestPersoByQuest(Mision quest) {
        for (MisionJugador questPlayer : this.questList.values())
            if (questPlayer != null && questPlayer.getQuest().getId() == quest.getId())
                return questPlayer;
        return null;
    }

    //Comando Pasar Turno - Especial
    public void setComandoPasarTurno(boolean _comandoPasarTurno) {
        this._comandoPasarTurno = _comandoPasarTurno;
    }

    public MisionJugador getQuestPersoByQuestId(int id) {
        for (MisionJugador qPerso : questList.values())
            if (qPerso.getQuest().getId() == id)
                return qPerso;
        return null;
    }

    public String getQuestGmPacket() {
        StringBuilder packet = new StringBuilder();
        int nb = 0;
        packet.append("+");
        for (MisionJugador qPerso : questList.values()) {
            packet.append(qPerso.getQuest().getId()).append(";");
            packet.append(qPerso.isFinish() ? 1 : 0);
            if (nb < questList.size() - 1)
                packet.append("|");
            nb++;
        }
        return packet.toString();
    }

    public long getLastPacketTime() { return lastPacketTime; }

    public void refreshLastPacketTime() { lastPacketTime = Instant.now().toEpochMilli(); }

    public Casas getInHouse() {
        return _curHouse;
    }

    public void setInHouse(Casas h) {
        _curHouse = h;
    }

    public int getIsOnDialogAction() {
        return this.action;
    }

    public void setIsOnDialogAction(int action) {
        this.action = action;
    }

    private AccionIntercambiar<?> exchangeAction;

    public AccionIntercambiar<?> getExchangeAction() {
        return exchangeAction;
    }

    public synchronized void setExchangeAction(AccionIntercambiar<?> exchangeAction) {
        if(exchangeAction == null) this.setAway(false);
        this.exchangeAction = exchangeAction;
    }


    public Doble<ObjetosInteractivos, GameCase> getObjetoInteractivo()
    {
        return inObjetoInteractivo;
    }

    public void setInObjetoInteractivo(Doble<ObjetosInteractivos, GameCase> inObjetoInteractivo)
    {
        this.inObjetoInteractivo =inObjetoInteractivo;
    }

    public void refreshCraftSecure(boolean unequip) {
        for (Jugador player : this.getCurMap().getPlayers()) {
            ArrayList<Oficio> jobs = player.getJobs();

            if (jobs != null) {
                ObjetoJuego object = player.getObjetByPos(Constantes.ITEM_POS_ARME);

                if (object == null) {
                    if (unequip) {
                        for(Jugador target : this.getCurMap().getPlayers())
                            target.send("EW+" + player.getId() + "|");
                    }
                    continue;
                }

                String packet = "EW+" + player.getId() + "|";
                StringBuilder data = new StringBuilder();

                for (Oficio job : jobs) {
                    if (job.getSkills().isEmpty())
                        continue;
                    if (!job.isValidTool(object.getModelo().getId()))
                        continue;

                    for (GameCase cell : this.getCurMap().getCases()) {
                        if (cell.getObject() != null) {
                            if (cell.getObject().getTemplate() != null) {
                                int io = cell.getObject().getTemplate().getId();
                                ArrayList<Integer> skills = job.getSkills().get(io);

                                if (skills != null)
                                    for (int skill : skills)
                                        if (!data.toString().contains(String.valueOf(skill)))
                                            data.append((data.length() == 0) ? skill : ";" + skill);
                            }
                        }
                    }

                    /*if (!data.isEmpty())
                        break;*/
                }

                for(Jugador target : this.getCurMap().getPlayers())
                    target.send(packet + data);
            }
        }
    }

    public static class Caracteristicas {

        private Map<Integer, Integer> effects = new HashMap<>();

        public Caracteristicas(boolean addBases, Jugador player) {
            this.effects= new HashMap<>();
            if(!addBases)
                return;
            this.effects.put(Constantes.STATS_ADD_PA, player.getLevel() < 100 ? 6 : 7);
            this.effects.put(Constantes.STATS_ADD_PM, 3);
            this.effects.put(Constantes.STATS_ADD_PROS, player.getClasse() == Constantes.CLASE_ANUTROF ? 120 : 100);
            this.effects.put(Constantes.STATS_ADD_PODS, 1000);
            this.effects.put(Constantes.STATS_CREATURE, 1);
            this.effects.put(Constantes.STATS_ADD_INIT, 1);
        }

        public Caracteristicas(Map<Integer, Integer> stats, boolean addBases, Jugador player) {
            this.effects=stats;
            if(!addBases)
                return;
            this.effects.put(Constantes.STATS_ADD_PA, player.getLevel() < 100 ? 6 : 7);
            this.effects.put(Constantes.STATS_ADD_PM, 3);
            this.effects.put(Constantes.STATS_ADD_PROS, player.getClasse() == Constantes.CLASE_ANUTROF ? 120 : 100);
            this.effects.put(Constantes.STATS_ADD_PODS, 1000);
            this.effects.put(Constantes.STATS_CREATURE, 1);
            this.effects.put(Constantes.STATS_ADD_INIT, 1);
        }

        public Caracteristicas(boolean a) { // Parchotage
            this.effects.put(Constantes.STATS_ADD_VITA, 0);
            this.effects.put(Constantes.STATS_ADD_SAGE, 0);
            this.effects.put(Constantes.STATS_ADD_INTE, 0);
            this.effects.put(Constantes.STATS_ADD_FORC, 0);
            this.effects.put(Constantes.STATS_ADD_CHAN, 0);
            this.effects.put(Constantes.STATS_ADD_AGIL, 0);
        }

        public Caracteristicas(Map<Integer, Integer> stats)
        {
            this.effects=stats;
        }

        public Caracteristicas() { this.effects= new HashMap<>(); }

        public Map<Integer, Integer> getEffects() {
            return this.effects;
        }

        public int get(int id) {
            return this.effects.get(id) == null ? 0 : this.effects.get(id);
        }

        public void addOneStat(int id, int val) {
            if(id == 112) id = Constantes.STATS_ADD_DOMA;
            if (this.effects.get(id) == null || this.effects.get(id) == 0) {
                this.effects.put(id, val);
            } else {
                int newVal = (this.effects.get(id) + val);
                if(newVal <= 0) {
                    this.effects.remove(id);
                    return;
                } else this.effects.put(id, newVal);
            }
            this.effects.get(id);
        }

        public boolean isSameStats(Caracteristicas other) {
            for (Entry<Integer, Integer> entry : this.effects.entrySet()) {
                //Si la stat n'existe pas dans l'autre map
                if (other.getEffects().get(entry.getKey()) == null)
                    return false;
                //Si la stat existe mais n'a pas la m�me valeur
                if (other.getEffects().get(entry.getKey()).compareTo(entry.getValue()) != 0)
                    return false;
            }
            for (Entry<Integer, Integer> entry : other.getEffects().entrySet()) {
                //Si la stat n'existe pas dans l'autre map
                if (this.effects.get(entry.getKey()) == null)
                    return false;
                //Si la stat existe mais n'a pas la m�me valeur
                if (this.effects.get(entry.getKey()).compareTo(entry.getValue()) != 0)
                    return false;
            }
            return true;
        }

        public String parseToItemSetStats() {
            StringBuilder str = new StringBuilder();
            if (this.effects.isEmpty())
                return "";
            for (Entry<Integer, Integer> entry : this.effects.entrySet()) {
                if (str.length() > 0)
                    str.append(",");
                str.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(entry.getValue())).append("#0#0");
            }
            return str.toString();
        }

        public int getEffect(int id) {
            int val;
            if (this.effects.get(id) == null)
                val = 0;
            else
                val = this.effects.get(id);

            switch (id) {
                case Constantes.STATS_ADD_AFLEE:
                    if (this.effects.get(Constantes.STATS_REM_AFLEE) != null)
                        val -= getEffect(Constantes.STATS_REM_AFLEE);
                    if (this.effects.get(Constantes.STATS_ADD_SAGE) != null)
                        val += getEffect(Constantes.STATS_ADD_SAGE) / 4;
                    break;
                case Constantes.STATS_ADD_MFLEE:
                    if (this.effects.get(Constantes.STATS_REM_MFLEE) != null)
                        val -= getEffect(Constantes.STATS_REM_MFLEE);
                    if (this.effects.get(Constantes.STATS_ADD_SAGE) != null)
                        val += getEffect(Constantes.STATS_ADD_SAGE) / 4;
                    break;
                case Constantes.STATS_ADD_INIT:
                    if (this.effects.get(Constantes.STATS_REM_INIT) != null)
                        val -= this.effects.get(Constantes.STATS_REM_INIT);
                    break;
                case Constantes.STATS_ADD_AGIL:
                    if (this.effects.get(Constantes.STATS_REM_AGIL) != null)
                        val -= this.effects.get(Constantes.STATS_REM_AGIL);
                    break;
                case Constantes.STATS_ADD_FORC:
                    if (this.effects.get(Constantes.STATS_REM_FORC) != null)
                        val -= this.effects.get(Constantes.STATS_REM_FORC);
                    break;
                case Constantes.STATS_ADD_CHAN:
                    if (this.effects.get(Constantes.STATS_REM_CHAN) != null)
                        val -= this.effects.get(Constantes.STATS_REM_CHAN);
                    break;
                case Constantes.STATS_ADD_INTE:
                    if (this.effects.get(Constantes.STATS_REM_INTE) != null)
                        val -= this.effects.get(Constantes.STATS_REM_INTE);
                    break;
                case Constantes.STATS_ADD_PA:
                    if (this.effects.get(Constantes.STATS_ADD_PA2) != null)
                        val += this.effects.get(Constantes.STATS_ADD_PA2);
                    if (this.effects.get(Constantes.STATS_REM_PA) != null)
                        val -= this.effects.get(Constantes.STATS_REM_PA);
                    if (this.effects.get(Constantes.STATS_REM_PA2) != null)//Non esquivable
                        val -= this.effects.get(Constantes.STATS_REM_PA2);
                    break;
                case Constantes.STATS_ADD_PM:
                    if (this.effects.get(Constantes.STATS_ADD_PM2) != null)
                        val += this.effects.get(Constantes.STATS_ADD_PM2);
                    if (this.effects.get(Constantes.STATS_REM_PM) != null)
                        val -= this.effects.get(Constantes.STATS_REM_PM);
                    if (this.effects.get(Constantes.STATS_REM_PM2) != null)//Non esquivable
                        val -= this.effects.get(Constantes.STATS_REM_PM2);
                    break;
                case Constantes.STATS_ADD_PO:
                    if (this.effects.get(Constantes.STATS_REM_PO) != null)
                        val -= this.effects.get(Constantes.STATS_REM_PO);
                    break;
                case Constantes.STATS_ADD_VITA:
                    if (this.effects.get(Constantes.STATS_REM_VITA) != null)
                        val -= this.effects.get(Constantes.STATS_REM_VITA);
                    break;
                case Constantes.STATS_ADD_VIE:
                    val = Constantes.STATS_ADD_VIE;
                    break;
                case Constantes.STATS_ADD_DOMA:
                    if (this.effects.get(Constantes.STATS_REM_DOMA) != null)
                        val -= this.effects.get(Constantes.STATS_REM_DOMA);
                    break;
                case Constantes.STATS_ADD_PODS:
                    if (this.effects.get(Constantes.STATS_REM_PODS) != null)
                        val -= this.effects.get(Constantes.STATS_REM_PODS);
                    break;
                case Constantes.STATS_ADD_PROS:
                    if (this.effects.get(Constantes.STATS_REM_PROS) != null)
                        val -= this.effects.get(Constantes.STATS_REM_PROS);
                    break;
                case Constantes.STATS_ADD_R_TER:
                    if (this.effects.get(Constantes.STATS_REM_R_TER) != null)
                        val -= this.effects.get(Constantes.STATS_REM_R_TER);
                    break;
                case Constantes.STATS_ADD_R_EAU:
                    if (this.effects.get(Constantes.STATS_REM_R_EAU) != null)
                        val -= this.effects.get(Constantes.STATS_REM_R_EAU);
                    break;
                case Constantes.STATS_ADD_R_AIR:
                    if (this.effects.get(Constantes.STATS_REM_R_AIR) != null)
                        val -= this.effects.get(Constantes.STATS_REM_R_AIR);
                    break;
                case Constantes.STATS_ADD_R_FEU:
                    if (this.effects.get(Constantes.STATS_REM_R_FEU) != null)
                        val -= this.effects.get(Constantes.STATS_REM_R_FEU);
                    break;
                case Constantes.STATS_ADD_R_NEU:
                    if (this.effects.get(Constantes.STATS_REM_R_NEU) != null)
                        val -= this.effects.get(Constantes.STATS_REM_R_NEU);
                    break;
                case Constantes.STATS_ADD_RP_TER:
                    if (this.effects.get(Constantes.STATS_REM_RP_TER) != null)
                        val -= this.effects.get(Constantes.STATS_REM_RP_TER);
                    break;
                case Constantes.STATS_ADD_RP_EAU:
                    if (this.effects.get(Constantes.STATS_REM_RP_EAU) != null)
                        val -= this.effects.get(Constantes.STATS_REM_RP_EAU);
                    break;
                case Constantes.STATS_ADD_RP_AIR:
                    if (this.effects.get(Constantes.STATS_REM_RP_AIR) != null)
                        val -= this.effects.get(Constantes.STATS_REM_RP_AIR);
                    break;
                case Constantes.STATS_ADD_RP_FEU:
                    if (this.effects.get(Constantes.STATS_REM_RP_FEU) != null)
                        val -= this.effects.get(Constantes.STATS_REM_RP_FEU);
                    break;
                case Constantes.STATS_ADD_RP_NEU:
                    if (this.effects.get(Constantes.STATS_REM_RP_NEU) != null)
                        val -= this.effects.get(Constantes.STATS_REM_RP_NEU);
                    break;
                case Constantes.STATS_ADD_MAITRISE:
                    if (this.effects.get(Constantes.STATS_ADD_MAITRISE) != null)
                        val = this.effects.get(Constantes.STATS_ADD_MAITRISE);
                    break;
            }
            return val;
        }

        public static Caracteristicas cumulStat(Caracteristicas s1, Caracteristicas s2) {
            HashMap<Integer, Integer> effets = new HashMap<>();
            for (int a = 0; a <= Constantes.MAX_EFFECTS_ID; a++) {
                if (s1.effects.get(a) == null && s2.effects.get(a) == null)
                    continue;

                int som = 0;
                if (s1.effects.get(a) != null)
                    som += s1.effects.get(a);
                if (s2.effects.get(a) != null)
                    som += s2.effects.get(a);

                effets.put(a, som);
            }
            return new Caracteristicas(effets, false, null);
        }

        public static Caracteristicas cumulStatFight(Caracteristicas s1, Caracteristicas s2) {
            HashMap<Integer, Integer> effets = new HashMap<>();
            for (int a = 0; a <= Constantes.MAX_EFFECTS_ID; a++) {
                if ((s1.effects.get(a) == null || s1.effects.get(a) == 0)
                        && (s2.effects.get(a) == null || s2.effects.get(a) == 0))
                    continue;
                int som = 0;
                if (s1.effects.get(a) != null)
                    som += s1.effects.get(a);
                if (s2.effects.get(a) != null)
                    som += s2.effects.get(a);
                effets.put(a, som);
            }
            return new Caracteristicas(effets, false, null);
        }
    }

    public static class Grupo {

        private final Jugador chief;
        private Jugador master;
        private final ArrayList<Jugador> players = new ArrayList<>();

        public Grupo(Jugador p1, Jugador p2) {
            this.chief = p1;
            this.players.add(p1);
            this.players.add(p2);
        }

        public ArrayList<Jugador> getPlayers() {
            return this.players;
        }

        public Jugador getChief() {
            return this.chief;
        }

        public boolean isChief(int id) {
            return this.chief.getId() == id;
        }

        public Jugador getMaster() {
            return master;
        }

        public void setMaster(Jugador master) {
            this.master = master;
        }

        public void addPlayer(Jugador player) {
            this.players.add(player);
        }

        public void leave(Jugador player) {
            if (!this.players.contains(player)) return;

            player.follow = null;
            player.follower.clear();
            player.setParty(null);
            this.players.remove(player);

            for(Jugador member : this.players) {
                if(member.follow == player) member.follow = null;
                if(member.follower.containsKey(player.getId())) member.follower.remove(player.getId());
            }

            if (this.players.size() == 1) {
                this.players.get(0).setParty(null);
                if (this.players.get(0).getAccount() == null || this.players.get(0).getGameClient() == null)
                    return;
                GestorSalida.GAME_SEND_PV_PACKET(this.players.get(0).getGameClient(), "");
            } else {
                GestorSalida.GAME_SEND_PM_DEL_PACKET_TO_GROUP(this, player.getId());
            }
        }

        public void moveAllPlayersToMaster(final GameCase cell) {
            if(this.master != null) {
                this.players.stream().filter((follower1) -> isWithTheMaster(follower1, false)).forEach(follower -> follower.setBlockMovement(true));
                this.players.stream().filter((follower1) -> isWithTheMaster(follower1, false)).forEach(follower -> {
                    try {
                        final GameCase newCell = cell != null ? cell : this.master.getCurCell();
                        String path = Camino.getShortestStringPathBetween(this.master.getCurMap(), follower.getCurCell().getId(), newCell.getId(), 0);
                        if (path != null) {
                            follower.getCurCell().removePlayer(follower);
                            follower.setCurCell(newCell);
                            follower.getCurCell().addPlayer(follower);

                            GestorSalida.GAME_SEND_GA_PACKET_TO_MAP(follower.getCurMap(), "0", 1, String.valueOf(follower.getId()), path);
                        }
                    } catch (Exception ignored) {}
                });
                this.players.stream().filter((follower1) -> isWithTheMaster(follower1, false)).forEach(follower -> follower.setBlockMovement(false));
            }
        }

        public boolean isWithTheMaster(Jugador follower, boolean inFight) {
            return follower != null && !follower.getName().equals(this.master.getName()) &&  this.players.contains(follower) && follower.getGameClient()
                    != null && this.master.getCurMap().getId() == follower.getCurMap().getId() && (inFight ? follower.getPelea() == this.master.getPelea() : follower.getPelea() == null);
        }
    }
}