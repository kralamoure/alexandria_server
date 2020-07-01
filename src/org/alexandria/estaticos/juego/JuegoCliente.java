package org.alexandria.estaticos.juego;

import org.apache.mina.core.session.IoSession;
import org.alexandria.estaticos.*;
import org.alexandria.estaticos.Mercadillo.*;
import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;
import org.alexandria.estaticos.area.mapa.Mapa.ObjetosInteractivos;
import org.alexandria.estaticos.area.mapa.entrada.*;
import org.alexandria.estaticos.area.mapa.Mapa.PuertasInteractivas;
import org.alexandria.estaticos.cliente.Cuenta;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.estaticos.cliente.Jugador.Grupo;
import org.alexandria.estaticos.comandos.ComandosAdministrador;
import org.alexandria.estaticos.comandos.ComandosJugadores;
import org.alexandria.estaticos.comandos.administracion.UsuarioAdministrador;
import org.alexandria.comunes.Camino;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.configuracion.Constantes;
import org.alexandria.configuracion.Logging;
import org.alexandria.configuracion.MainServidor;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.estaticos.Recaudador;
import org.alexandria.estaticos.Intercambio.*;
import org.alexandria.estaticos.Npc;
import org.alexandria.estaticos.evento.GestorEvento;
import org.alexandria.estaticos.evento.tipo.Evento;
import org.alexandria.estaticos.Gremio.GremioMiembros;
import org.alexandria.estaticos.juego.accion.AccionIntercambiar;
import org.alexandria.estaticos.juego.accion.AccionJuego;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.objeto.ObjetoModelo;
import org.alexandria.estaticos.objeto.entrada.FragmentosMagicos;
import org.alexandria.estaticos.objeto.entrada.PiedraAlma;
import org.alexandria.estaticos.oficio.Oficio;
import org.alexandria.estaticos.oficio.OficioAccion;
import org.alexandria.estaticos.oficio.OficioCaracteristicas;
import org.alexandria.estaticos.oficio.OficioConstantes;
import org.alexandria.estaticos.oficio.magueo.RomperObjetos;
import org.alexandria.estaticos.oficio.magueo.Runas;
import org.alexandria.otro.Accion;
import org.alexandria.otro.Dopeul;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.otro.utilidad.GeneradorNombres;
import org.alexandria.otro.utilidad.Doble;
import org.alexandria.otro.utilidad.Temporizador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.alexandria.estaticos.Mascota.MascotaEntrada;
import org.alexandria.estaticos.Mision.*;
import org.alexandria.estaticos.Npc.*;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class JuegoCliente {

    private final IoSession session;
    private Cuenta account;
    private Jugador player;
    private boolean walk = false;
    Logger logger;
    private UsuarioAdministrador adminUser;
    private final Map<Integer, AccionJuego> actions = new HashMap<>();
    public long timeLastTradeMsg = 0, timeLastRecrutmentMsg = 0, timeLastAlignMsg = 0, timeLastChatMsg = 0, timeLastIncarnamMsg = 0, action = 0, timeLastAct = 0;

    private String preparedKeys;
    private int PingPromedio = 0;
    public JuegoCliente(IoSession session) {
        this.session = session;
        this.session.write("HG");
        String IP = ((InetSocketAddress) (this.getSession().getRemoteAddress())).getAddress().getHostAddress();
        logger = LoggerFactory.getLogger(IP);
    }
    
    public IoSession getSession() {
        return session;
    }

    public Jugador getPlayer() {
        return this.player;
    }

    public Cuenta getAccount() {
        return account;
    }

    public String getPreparedKeys(){
        return preparedKeys;
    }

    public void parsePacket(String packet) throws InterruptedException {
        if(player != null) {
            player.refreshLastPacketTime();
            if (this.player.isChangeName()) {
                this.changeName(packet);
                return;
            }
        }
        if (packet.length() > 3 && packet.substring(0, 4).equalsIgnoreCase("ping")) {
            GestorSalida.GAME_SEND_PONG(this);
            return;
        }
        if(packet.length() > 4 && packet.substring(0,5).equalsIgnoreCase("qping"))
        {
            GestorSalida.GAME_SEND_QPONG(this);
            return;
        }

        if(Configuracion.INSTANCE.getAUTO_EVENT()) {
            GestorEvento manager = GestorEvento.getInstance();
            if(manager.getState() == GestorEvento.State.STARTED) {
                Evento event = manager.getCurrentEvent();
                try {
                    if (event != null && event.onReceivePacket(manager, this.player, packet)) {
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage("Une erreur s'est produite lors d'une action en rapport avec l'événement.");
                }
            }
        }

        switch (packet.charAt(0)) {
            case 'A' -> parseAccountPacket(packet);
            case 'B' -> parseBasicsPacket(packet);
            case 'C' -> parseConquestPacket(packet);
            case 'c' -> parseChanelPacket(packet);
            case 'D' -> parseDialogPacket(packet);
            case 'd' -> parseDocumentPacket(packet);
            case 'E' -> parseExchangePacket(packet);
            case 'e' -> parseEnvironementPacket(packet);
            case 'F' -> parseFrienDDacket(packet);
            case 'f' -> parseFightPacket(packet);
            case 'G' -> parseGamePacket(packet);
            case 'g' -> parseGuildPacket(packet);
            case 'h' -> parseHousePacket(packet);
            case 'i' -> parseEnemyPacket(packet);
            case 'J' -> parseJobOption(packet);
            case 'K' -> parseHouseKodePacket(packet);
            case 'O' -> parseObjectPacket(packet);
            case 'P' -> parseGroupPacket(packet);
            case 'R' -> parseMountPacket(packet);
            case 'Q' -> parseQuestData(packet);
            case 'S' -> parseSpellPacket(packet);
            case 'T' -> parseFoireTroll(packet);
            case 'W' -> parseWaypointPacket(packet);
        }
    }

    //Pakete de cuenta
    private void parseAccountPacket(String packet) {
        switch (packet.charAt(1)) {
            case 'A':
                addCharacter(packet);
                break;
            case 'B':
                boost(packet);
                break;
            case 'D':
                deleteCharacter(packet);
                break;
            case 'f':
                getQueuePosition();
                break;
            case 'g':
                getGifts(packet.substring(2));
                break;
            case 'G':
                attributeGiftToCharacter(packet.substring(2));
                break;
            case 'i':
                sendIdentity(packet);
                break;
            case 'k':
                //setKeyIndex(Byte.parseByte(packet.substring(2), 16));
                break;
            case 'L':
                getCharacters(/*(packet.length() == 2)*/);
                break;
            case 'R':
                retry(Integer.parseInt(packet.substring(2)));
                break;
            case 'S':
                setCharacter(packet);
                break;
            case 'T':
                sendTicket(packet);
                break;
            case 'V':
                requestRegionalVersion();
                break;
            case 'P':
                String name = GeneradorNombres.nameGenerator
                        .compose((int)(
                                Math.random() * 3 +
                                        Formulas.getRandomValue(1, 5)));
                GestorSalida.send(this, "APK" + name);
                break;
        }
    }

    private void addCharacter(String packet) {
        String[] infos = packet.substring(2).split("\\|");
        if (Database.dinamicos.getPlayerData().exist(infos[0])) {
            GestorSalida.GAME_SEND_NAME_ALREADY_EXIST(this);
            return;
        }
        //Validation du nom du this.playernnage
        boolean isValid = true;
        String name = infos[0].toLowerCase();
        //V�rifie d'abord si il contient des termes d�finit
        if (name.length() > 20 || name.length() < 3 || name.contains("modo")
                || name.contains("admin") || name.contains("putain")
                || name.contains("administrateur") || name.contains("puta")) {
            isValid = false;
        }

        //Si le nom passe le test, on v�rifie que les caract�re entr� sont correct.
        if (isValid) {
            int tiretCount = 0;
            char exLetterA = ' ';
            char exLetterB = ' ';
            for (char curLetter : name.toCharArray()) {
                if (!((curLetter >= 'a' && curLetter <= 'z') || curLetter == '-')) {
                    isValid = false;
                    break;
                }
                if (curLetter == exLetterA && curLetter == exLetterB) {
                    isValid = false;
                    break;
                }
                if (curLetter >= 'a' && curLetter <= 'z') {
                    exLetterA = exLetterB;
                    exLetterB = curLetter;
                }
                if (curLetter == '-') {
                    if (tiretCount >= 1) {
                        isValid = false;
                        break;
                    } else {
                        tiretCount++;
                    }
                }
            }
        }
        //Si le nom est invalide
        if (!isValid) {
            GestorSalida.GAME_SEND_NAME_ALREADY_EXIST(this);
            return;
        }
        if (this.account.getPlayers().size() >= 5) {
            GestorSalida.GAME_SEND_CREATE_PERSO_FULL(this);
            return;
        }
        if (this.account.createPlayer(infos[0], Integer.parseInt(infos[2]), Integer.parseInt(infos[1]), Integer.parseInt(infos[3]), Integer.parseInt(infos[4]), Integer.parseInt(infos[5]))) {
            GestorSalida.GAME_SEND_CREATE_OK(this);
            GestorSalida.GAME_SEND_PERSO_LIST(this, this.account.getPlayers(), this.account.getSubscribeRemaining());
        } else {
            GestorSalida.GAME_SEND_CREATE_FAILED(this);
        }
    }

    private void boost(String packet) {
        try {
            int stat = -1;
            if (this.player.getMorphMode()) {
                this.player.sendMessage("Vous êtes incarné, vous ne pouvez donc pas vous ajoutez de point de caractéristique !");
                return;
            }

            if (packet.substring(2).contains(";")) {
                stat = Integer.parseInt(packet.substring(2).split(";")[0]);
                if (stat > 0) {
                    int code = 0;
                    code = Integer.parseInt(packet.substring(2).split(";")[1]);
                    if (code < 0)
                        return;
                    if (this.player.get_capital() < code)
                        code = this.player.get_capital();
                    this.player.boostStatFixedCount(stat, code);
                }
            } else {
                stat = Integer.parseInt(packet.substring(2).split("/u000A")[0]);
                this.player.boostStat(stat, true);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void deleteCharacter(String packet) {
        String[] split = packet.substring(2).split("\\|");
        int GUID = Integer.parseInt(split[0]);
        String answer = split.length > 1 ? split[1] : "";
        if (this.account.getPlayers().containsKey(GUID) && !this.account.getPlayers().get(GUID).isOnline()) {
            if (this.account.getPlayers().get(GUID).getLevel() < 20 || (this.account.getPlayers().get(GUID).getLevel() >= 20 && answer.equals(this.account.getAnswer().replace(" ", "%20")))) {
                this.account.deletePlayer(GUID);
                GestorSalida.GAME_SEND_PERSO_LIST(this, this.account.getPlayers(), this.account.getSubscribeRemaining());
            } else {
                GestorSalida.GAME_SEND_DELETE_PERSO_FAILED(this);
            }
        } else {
            GestorSalida.GAME_SEND_DELETE_PERSO_FAILED(this);
        }
    }

    private void getQueuePosition() {
        GestorSalida.MULTI_SEND_Af_PACKET(this, 1, 1, 1, 1);
        //SocketManager.MULTI_SEND_Af_PACKET(this, this.queuePlace.getPlace(), QueueThreadPool.executor.getQueue().size(), 0, 1);
    }

    private void getGifts(String packet) {
        switch(packet.toUpperCase()) {

        }

        String gifts = Database.estaticos.getGiftData().getByAccount(this.account.getId());
        if (gifts == null)
            return;
        if (!gifts.isEmpty()) {
            StringBuilder data = new StringBuilder();
            int item = -1;
            for (String object : gifts.split(";")) {
                int id = Integer.parseInt(object.split(",")[0]), qua = Integer.parseInt(object.split(",")[1]);

                if (data.length() == 0) data = new StringBuilder("1~" + Integer.toString(id, 16) + "~" + Integer.toString(qua, 16) + "~~" + Mundo.mundo.getObjetoModelo(id).getStrTemplate());
                else data.append(";1~").append(Integer.toString(id, 16)).append("~").append(Integer.toString(qua, 16)).append("~~").append(Mundo.mundo.getObjetoModelo(id).getStrTemplate());
                if (item == -1) item = id;
            }
            GestorSalida.GAME_SEND_Ag_PACKET(this, item, data.toString());
        }
    }

    private void attributeGiftToCharacter(String packet) {
        String[] infos = packet.split("\\|");

        int template = Integer.parseInt(infos[0]);
        Jugador player = Mundo.mundo.getPlayer(Integer.parseInt(infos[1]));

        if (player == null)
            return;

        String gifts = Database.estaticos.getGiftData().getByAccount(this.account.getId());

        if (gifts.isEmpty())
            return;

        for (String data : gifts.split(";")) {
            String[] split = data.split(",");
            int id = Integer.parseInt(split[0]);

            if (id == template) {
                int qua = Integer.parseInt(split[1]), jp = Integer.parseInt(split[2]);
                ObjetoJuego obj;

                if (qua == 1) {
                    obj = Mundo.mundo.getObjetoModelo(template).createNewItem(qua, (jp == 1));
                    if (player.addObjet(obj, true))
                        Mundo.addGameObject(obj, true);
                    if(obj.getModelo().getType() == Constantes.ITEM_TYPE_CERTIF_MONTURE)
                        obj.setMountStats(player, null, true).setToMax();
                    String str1 = id + "," + qua + "," + jp, str2 = id + ","
                            + qua + "," + jp + ";", str3 = ";" + id + "," + qua
                            + "," + jp;

                    gifts = gifts.replace(str2, "").replace(str3, "").replace(str1, "");
                } else {
                    obj = Mundo.mundo.getObjetoModelo(template).createNewItem(1, (jp == 1));
                    if (player.addObjet(obj, true))
                        Mundo.addGameObject(obj, true);
                    if(obj.getModelo().getType() == Constantes.ITEM_TYPE_CERTIF_MONTURE)
                        obj.setMountStats(player, null, true).setToMax();

                    String str1 = id + "," + qua + "," + jp, str2 = id + ","
                            + qua + "," + jp + ";", str3 = ";" + id + "," + qua
                            + "," + jp;

                    String cstr1 = id + "," + (qua - 1) + "," + jp, cstr2 = id
                            + "," + (qua - 1) + "," + jp + ";", cstr3 = ";"
                            + id + "," + (qua - 1) + "," + jp;

                    gifts = gifts.replace(str2, cstr2).replace(str3, cstr3).replace(str1, cstr1);
                }
                Database.estaticos.getGiftData().update(player.getAccID(), gifts);
            }
        }

        Database.dinamicos.getPlayerData().update(player);

        if (gifts.isEmpty())
            player.send("AG");
        else {
            this.getGifts("");
            player.send("AG");
        }
    }

    private void sendIdentity(String packet) {}

    private void getCharacters() {
        this.account.setGameClient(this);
        for (Jugador player : this.account.getPlayers().values()) {
            if (player != null)
                if (player.getPelea() != null && player.getPelea().getFighterByPerso(player) != null) {
                    this.player = player;
                    this.player.OnJoinGame();
                    return;
                }
        }

        GestorSalida.GAME_SEND_PERSO_LIST(this, this.account.getPlayers(), this.account.getSubscribeRemaining());
    }

    private void retry(int id) {
        final Jugador player = this.account.getPlayers().get(id);

        if(player != null) {
            player.revive();
            GestorSalida.GAME_SEND_PERSO_LIST(this, this.account.getPlayers(), this.account.getSubscribeRemaining());
        } else {
            this.getSession().write("BN");
        }
    }

    private void setCharacter(String packet) {
        int id = Integer.parseInt(packet.substring(2));

        if (this.account.getPlayers().get(id) != null) {
            this.player = this.account.getPlayers().get(id);
            this.logger = LoggerFactory.getLogger(this.player.getName());
            if (this.player != null) {
                if(this.player.isDead() == 1 && Configuracion.INSTANCE.getHEROIC())
                    this.getSession().write("BN");
                else
                    this.player.OnJoinGame();
                return;
            }
        }
        GestorSalida.GAME_SEND_PERSO_SELECTION_FAILED(this);
    }

    private void sendTicket(String packet) {
        try {
            int id = Integer.parseInt(packet.substring(2));

            this.account = JuegoServidor.getAndDeleteWaitingAccount(id);

            if (this.account == null) {
                GestorSalida.GAME_SEND_ATTRIBUTE_FAILED(this);
                this.kick();
            } else {
                logger = LoggerFactory.getLogger(this.account.getName());
                String ip = this.session.getRemoteAddress().toString().substring(1).split(":")[0];
                
                if(this.account.getGameClient() != null)
                    this.account.getGameClient().kick();

                this.account.setGameClient(this);
                this.account.setCurrentIp(ip);
                Database.dinamicos.getAccountData().setLogged(this.account.getId(), 1);

                logger.info("new account connected {} ", this.account.getName() + " > " + ip);

                if(Configuracion.INSTANCE.getENCRYPT_PACKET()){
                    //String key = generateKey();
                    this.getSession().write("ATK18fd8ad4a38cdd0432248a76f8f148ceb");
                    this.preparedKeys = Mundo.mundo.getCryptManager().prepareKey("8fd8ad4a38cdd0432248a76f8f148ceb");
                } else {
                    this.getSession().write("ATK0");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            GestorSalida.GAME_SEND_ATTRIBUTE_FAILED(this);
            this.kick();
        }
    }

    private void requestRegionalVersion() {
        GestorSalida.GAME_SEND_AV0(this);
    }
    //Pakete de cuentas

    //Paketes basicos
    private void parseBasicsPacket(String packet) throws InterruptedException {
        switch (packet.charAt(1)) {
            case 'A'://Console
                authorisedCommand(packet);
                break;
            case 'D':
                getDate();
                break;
            case 'M':
                tchat(packet);
                break;
            case 'W': // Whois
                whoIs(packet);
                break;
            case 'S':
                this.player.useSmiley(packet.substring(2));
                break;
            case 'Y':
                chooseState(packet);
                break;
            case 'a':
                if (packet.charAt(2) == 'M')
                    goToMap(packet);
                break;
            case 'p': //Sistema de ping promedio
                setPingPromedio(packet);
                break;
        }
    }

    private void authorisedCommand(String packet) throws InterruptedException {
        if (this.adminUser == null) this.adminUser = new ComandosAdministrador(this.player);
        if (this.player.getGroupe() == null || this.getPlayer() == null) {
            this.getAccount().getGameClient().kick();
            return;
        }

        if (Configuracion.INSTANCE.getMostrarenviados()) {
        Logging.comandos.info(this.getAccount().getCurrentIp() + " : " + this.getAccount().getName() + " > " + this.getPlayer().getName() + " > " + packet.substring(2));
        }
        this.adminUser.apply(packet);
    }

    private void getDate() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());

        this.send("BD" + calendar.get(Calendar.YEAR) + "|" + calendar.get(Calendar.MONTH) + "|" + calendar.get(Calendar.DAY_OF_MONTH));
        this.send("BT" + (calendar.getTime().getTime() + 3600000));
    }

    private void tchat(String packet) {
        String msg;
        String lastMsg = "";

        if (this.player.getAccount() != null && this.player.isMuted()) {
            short remaining = (short) ((this.getAccount().getMuteTime() - Instant.now().toEpochMilli()) / 60000);
            this.player.send("Im117;" + this.getAccount().getMutePseudo() + "~" + remaining);
            return;
        }

        if (this.player.getCurMap() != null) {
            if (this.player.getCurMap().isMute() && this.player.getGroupe() == null) {
                this.player.sendServerMessage("The map is currently mute.");
                return;
            }
        }

        packet = packet.replace("<", "");
        packet = packet.replace(">", "");
        if (packet.length() < 6)
            return;

        //case '�':// Unknow
        //    break;
        switch (packet.charAt(2)) {
//Defaut
            case '*' -> {
                if (Instant.now().toEpochMilli() - timeLastChatMsg < 500) {
                    this.send("M10");
                    return;
                }
                timeLastChatMsg = Instant.now().toEpochMilli();
                if (!this.player.get_canaux().contains(packet.charAt(2) + ""))
                    return;
                msg = packet.split("\\|", 2)[1];
                if (ComandosJugadores.analyse(this.player, msg)) {
                    this.player.send("BN");
                    return;
                }
                if (msg.equals(lastMsg)) {
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "184");
                    return;
                }
                if (this.player.isSpec() && this.player.getPelea() != null) {
                    int team = this.player.getPelea().getTeamId(this.player.getId());
                    if (team == -1)
                        return;
                    GestorSalida.GAME_SEND_cMK_PACKET_TO_FIGHT(this.player.getPelea(), team, "#", this.player.getId(), this.player.getName(), msg);
                    return;
                }
                if (Configuracion.INSTANCE.getMostrarenviados()) {
                Logging.chats.info("Default" + this.player.getName() + " > Map " + this.player.getCurMap().getId() + " > " + msg);
                }
                if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF) != null)
                    if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF).getModelo().getId() == 10844)
                        msg = Formulas.translateMsg(msg);
                if (this.player.getPelea() == null)
                    GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(this.player.getCurMap(), "", this.player.getId(), this.player.getName(), msg);
                else
                    GestorSalida.GAME_SEND_cMK_PACKET_TO_FIGHT(this.player.getPelea(), 7, "", this.player.getId(), this.player.getName(), msg);
            }
            //Canal Incarnam
            case '^' -> {
                msg = packet.split("\\|", 2)[1];
                long x;
                if ((x = Instant.now().toEpochMilli() - timeLastIncarnamMsg) < 30000) {
                    x = (30000 - x) / 1000;//Chat antiflood
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "0115;" + ((int) Math.ceil(x) + 1));
                    return;
                }
                if (msg.equals(lastMsg)) {
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "184");
                    return;
                }
                if (this.player.getLevel() > 150)
                    return;
                timeLastIncarnamMsg = Instant.now().toEpochMilli();
                msg = packet.split("\\|", 2)[1];
                lastMsg = msg;
                if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF) != null)
                    if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF).getModelo().getId() == 10844)
                        msg = Formulas.translateMsg(msg);
                GestorSalida.GAME_SEND_cMK_PACKET_INCARNAM_CHAT(this.player, "^", this.player.getId(), this.player.getName(), msg);
            }
            //Canal Equipo
            case '#' -> {
                if (!this.player.get_canaux().contains(packet.charAt(2) + ""))
                    return;
                if (this.player.getPelea() != null) {
                    msg = packet.split("\\|", 2)[1];
                    int team = this.player.getPelea().getTeamId(this.player.getId());
                    if (team == -1)
                        return;
                    if (Configuracion.INSTANCE.getMostrarenviados()) {
                    Logging.chats.info("Team" + this.player.getName() + " > " + this.player.getPelea() + " > " + msg);
                    }
                    if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF) != null)
                        if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF).getModelo().getId() == 10844)
                            msg = Formulas.translateMsg(msg);
                    GestorSalida.GAME_SEND_cMK_PACKET_TO_FIGHT(this.player.getPelea(), team, "#", this.player.getId(), this.player.getName(), msg);
                }
            }
            //Canal grupo
            case '$' -> {
                if (!this.player.get_canaux().contains(packet.charAt(2) + ""))
                    return;
                if (this.player.getParty() == null)
                    break;
                msg = packet.split("\\|", 2)[1];
                if (Configuracion.INSTANCE.getMostrarenviados()) {
                Logging.chats.info(("Grupo" + this.player.getName() + " > " + this.player.getParty() + " > " + msg));
                }
                if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF) != null)
                    if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF).getModelo().getId() == 10844)
                        msg = Formulas.translateMsg(msg);
                GestorSalida.GAME_SEND_cMK_PACKET_TO_GROUP(this.player.getParty(), "$", this.player.getId(), this.player.getName(), msg);
            }
//Canal commerce
            case ':' -> {
                if (!this.player.get_canaux().contains(packet.charAt(2) + ""))
                    return;
                long l;
                if (this.player.isInAreaNotSubscribe()) {
                    GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(), 'S');
                    return;
                }
                if (this.player.cantCanal()) {
                    GestorSalida.GAME_SEND_MESSAGE(this.player, "Vous n'avez pas la permission de parler dans ce canal !", "B9121B");
                } else if (this.player.isInPrison()) {
                    GestorSalida.GAME_SEND_MESSAGE(this.player, "Vous êtes en prison, impossible de parler dans ce canal !", "B9121B");
                } else {
                    if (this.player.getGroupe() == null) {
                        if ((l = Instant.now().toEpochMilli() - timeLastTradeMsg) < 50000) {
                            l = (50000 - l) / 1000;//On calcul la diff�rence en secondes
                            GestorSalida.GAME_SEND_Im_PACKET(this.player, "0115;"
                                    + ((int) Math.ceil(l) + 1));
                            return;
                        }
                    }
                    timeLastTradeMsg = Instant.now().toEpochMilli();
                    msg = packet.split("\\|", 2)[1];
                    if (Configuracion.INSTANCE.getMostrarenviados()) {
                    Logging.chats.info(("Intercambio" + this.player.getName() + " > " + msg));
                    }
                    if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF) != null)
                        if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF).getModelo().getId() == 10844)
                            msg = Formulas.translateMsg(msg);
                    GestorSalida.GAME_SEND_cMK_PACKET_TO_ALL(this.player, ":", this.player.getId(), this.player.getName(), msg);
                }
            }
            //Canal Admin
            case '@' -> {
                if (this.player.getGroupe() == null)
                    return;
                msg = packet.split("\\|", 2)[1];
                if (Configuracion.INSTANCE.getMostrarenviados()) {
                Logging.chats.info(("Admin " + this.player.getName() + " > " + msg));
                }
                GestorSalida.GAME_SEND_cMK_PACKET_TO_ADMIN("@", this.player.getId(), this.player.getName(), msg);
            }
            //Canal reclutamiento
            case '?' -> {
                if (!this.player.get_canaux().contains(packet.charAt(2) + ""))
                    return;
                if (this.player.isInAreaNotSubscribe()) {
                    GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(), 'S');
                    return;
                }
                long j;
                if (this.player.cantCanal()) {
                    GestorSalida.GAME_SEND_MESSAGE(this.player, "Vous n'avez pas la permission de parler dans ce canal !", "B9121B");
                } else if (this.player.isInPrison()) {
                    GestorSalida.GAME_SEND_MESSAGE(this.player, "Vous êtes en prison, impossible de parler dans ce canal !", "B9121B");
                } else {
                    if (this.player.getGroupe() == null) {
                        if ((j = Instant.now().toEpochMilli()
                                - timeLastRecrutmentMsg) < 40000) {
                            j = (40000 - j) / 1000;//On calcul la diff�rence en secondes
                            GestorSalida.GAME_SEND_Im_PACKET(this.player, "0115;"
                                    + ((int) Math.ceil(j) + 1));
                            return;
                        }
                    }
                    timeLastRecrutmentMsg = Instant.now().toEpochMilli();
                    msg = packet.split("\\|", 2)[1];
                    if (Configuracion.INSTANCE.getMostrarenviados()) {
                    Logging.chats.info(("Reclutamiento" + this.player.getName() + " > " + msg));
                    }
                    if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF) != null)
                        if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF).getModelo().getId() == 10844)
                            msg = Formulas.translateMsg(msg);
                    GestorSalida.GAME_SEND_cMK_PACKET_TO_ALL(this.player, "?", this.player.getId(), this.player.getName(), msg);
                }
            }
            //Canal gremio
            case '%' -> {
                if (!this.player.get_canaux().contains(packet.charAt(2) + ""))
                    return;
                if (this.player.getGuild() == null)
                    return;
                msg = packet.split("\\|", 2)[1];
                if (Configuracion.INSTANCE.getMostrarenviados()) {
                Logging.chats.info(("Gremio" + this.player.getName() + " > " + this.player.getGuild().getName() + " > " + msg));
                }
                if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF) != null)
                    if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF).getModelo().getId() == 10844)
                        msg = Formulas.translateMsg(msg);
                GestorSalida.GAME_SEND_cMK_PACKET_TO_GUILD(this.player.getGuild(), "%", this.player.getId(), this.player.getName(), msg);
            }
            //Canal alineamiento
            case '!' -> {
                if (!this.player.get_canaux().contains(packet.charAt(2) + ""))
                    return;
                if (this.player.isInAreaNotSubscribe()) {
                    GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(), 'S');
                    return;
                }
                if (this.player.get_align() == 0)
                    return;
                if (this.player.getDeshonor() >= 1) {
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "183");
                    return;
                }
                long k;
                if ((k = Instant.now().toEpochMilli() - timeLastAlignMsg) < 30000) {
                    k = (30000 - k) / 1000;//On calcul la diff�rence en secondes
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "0115;" + ((int) Math.ceil(k) + 1));
                    return;
                }
                timeLastAlignMsg = Instant.now().toEpochMilli();
                msg = packet.split("\\|", 2)[1];
                if (Configuracion.INSTANCE.getMostrarenviados()) {
                Logging.chats.info(("Alineacion" + this.player.getName() + " > " + msg));
                }
                if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF) != null)
                    if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF).getModelo().getId() == 10844)
                        msg = Formulas.translateMsg(msg);
                GestorSalida.GAME_SEND_cMK_PACKET_TO_ALIGN("!", this.player.getId(), this.player.getName(), msg, this.player);
            }
            default -> {
                String nom = packet.substring(2).split("\\|")[0];
                msg = packet.split("\\|", 2)[1];
                if (!(nom.length() <= 1)) {
                    Jugador target = Mundo.mundo.getPlayerByName(nom);
                    if (target == null || target.getAccount() == null || target.getGameClient() == null) {
                        GestorSalida.GAME_SEND_CHAT_ERROR_PACKET(this, nom);
                        return;
                    }
                    if (target.getAccount().isEnemyWith(this.player.getAccount().getId()) || !target.isDispo(this.player)) {
                        GestorSalida.GAME_SEND_Im_PACKET(this.player, "114;" + target.getName());
                        return;
                    }
                    if (msg.equals(lastMsg)) {
                        GestorSalida.GAME_SEND_Im_PACKET(this.player, "184");
                        return;
                    }
                    if (this.player.getGroupe() == null && target.isInvisible()) {
                        GestorSalida.GAME_SEND_CHAT_ERROR_PACKET(this, nom);
                        return;
                    }
                    if (target.mpToTp) {
                        if (this.player.getPelea() != null)
                            return;
                        this.player.thatMap = this.player.getCurMap().getId();
                        this.player.thatCell = this.player.getCurCell().getId();
                        this.player.teleport(target.getCurMap().getId(), target.getCurCell().getId());
                        return;
                    }

                    if (Configuracion.INSTANCE.getMostrarenviados()) {
                    Logging.chats.info(("Privado" + this.player.getName() + " à " + target.getName() + " > " + msg));
                    }
                    if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF) != null)
                        if (this.player.getObjetByPos(Constantes.ITEM_POS_ROLEPLAY_BUFF).getModelo().getId() == 10844)
                            msg = Formulas.translateMsg(msg);

                    GestorSalida.GAME_SEND_cMK_PACKET(target, "F", this.player.getId(), this.player.getName(), msg);
                    GestorSalida.GAME_SEND_cMK_PACKET(this.player, "T", target.getId(), target.getName(), msg);

                    if (target.getAccount().isMuted())
                        this.send("Im0168;" + target.getName() + "~" + target.getAccount().getMuteTime());
                }
            }
        }
    }

    private void whoIs(String packet) {
        packet = packet.substring(2);
        Jugador player = Mundo.mundo.getPlayerByName(packet);
        if (player == null) {
            if (packet.isEmpty())
                GestorSalida.GAME_SEND_BWK(this.player, this.player.getAccount().getPseudo()
                        + "|1|"
                        + this.player.getName()
                        + "|"
                        + (this.player.getCurMap().getSubArea() != null ? this.player.getCurMap().getSubArea().area.getId() : "-1"));
            else
                this.player.send("PIEn" + packet);

        } else {
            if (!player.isOnline()) {
                this.player.send("PIEn" + player.getName());
                return;
            }
            if (this.player.getAccount().isFriendWith(player.getId()))
                GestorSalida.GAME_SEND_BWK(this.player, player.getAccount().getPseudo()
                        + "|1|"
                        + player.getName()
                        + "|"
                        + (player.getCurMap().getSubArea() != null ? player.getCurMap().getSubArea().area.getId() : "-1"));
            else if (player == this.player)
                GestorSalida.GAME_SEND_BWK(this.player, this.player.getAccount().getPseudo()
                        + "|1|"
                        + this.player.getName()
                        + "|"
                        + (this.player.getCurMap().getSubArea() != null ? this.player.getCurMap().getSubArea().area.getId() : "-1"));
            else
                GestorSalida.GAME_SEND_BWK(this.player, player.getAccount().getPseudo()
                        + "|1|" + player.getName() + "|-1");
        }
    }

    private void chooseState(String packet) {
        switch (packet.charAt(2)) {
            case 'A': //Absent
                if (this.player._isAbsent) {
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "038");
                    this.player._isAbsent = false;
                } else {
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "037");
                    this.player._isAbsent = true;
                }
                break;
            case 'I': //Invisible
                if (this.player._isInvisible) {
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "051");
                    this.player._isInvisible = false;
                } else {
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "050");
                    this.player._isInvisible = true;
                }
                break;
        }
    }

    // T�l�portation de MJ
    private void goToMap(String packet) {
        if (this.player.getGroupe() == null)
            return;
        if (this.player.getGroupe().isJugador())
            return;

        String datas = packet.substring(3);
        if (datas.isEmpty())
            return;
        int MapX = Integer.parseInt(datas.split(",")[0]);
        int MapY = Integer.parseInt(datas.split(",")[1]);
        ArrayList<Mapa> i = Mundo.mundo.getMapByPosInArrayPlayer(MapX, MapY, this.player);
        Mapa map = null;
        if (i.size() <= 0)
            return;
        else if (i.size() > 1)
            map = i.get(Formulas.getRandomValue(0, i.size() - 1));
        else if (i.size() == 1)
            map = i.get(0);
        if (map == null)
            return;
        int CellId = map.getRandomFreeCellId();
        if (map.getCase(CellId) == null)
            return;
        if (this.player.getPelea() != null)
            return;

        this.player.teleport(map.getId(), CellId);
    }
    //Fin pakete basico

    //Paketes de conquista
    private void parseConquestPacket(String packet) {
        switch (packet.charAt(1)) {
            case 'b' -> requestBalance();
            case 'B' -> getAlignedBonus();
            case 'W' -> worldInfos(packet);
            case 'I' -> prismInfos(packet);
            case 'F' -> prismFight(packet);
        }
    }

    public void requestBalance() {
        GestorSalida.SEND_Cb_BALANCE_CONQUETE(this.player, Mundo.mundo.getBalanceWorld(this.player.get_align())
                + ";"
                + Mundo.mundo.getBalanceArea(this.player.getCurMap().getSubArea().area, this.player.get_align()));
    }

    public void getAlignedBonus() {
        double porc = Mundo.mundo.getBalanceWorld(this.player.get_align());
        double porcN = Math.rint((this.player.getGrade() / 2.5) + 1);
        GestorSalida.SEND_CB_BONUS_CONQUETE(this.player, porc + "," + porc + ","
                + porc + ";" + porcN + "," + porcN + "," + porcN + ";" + porc
                + "," + porc + "," + porc);
    }

    private void worldInfos(String packet) {
        switch (packet.charAt(2)) {
            case 'J', 'V' -> {
                GestorSalida.SEND_CW_INFO_WORLD_CONQUETE(this.player, Mundo.mundo.PrismesGeoposition(1));
                GestorSalida.SEND_CW_INFO_WORLD_CONQUETE(this.player, Mundo.mundo.PrismesGeoposition(2));
            }
        }
    }

    private void prismInfos(String packet) {
        if (packet.charAt(2) == 'J' || packet.charAt(2) == 'V') {
            if (packet.charAt(2) == 'J') {
                Prisma prism = Mundo.mundo.getPrisme(this.player.getCurMap().getSubArea().prismId);
                if (prism != null) {
                    Prisma.parseAttack(this.player);
                    Prisma.parseDefense(this.player);
                }
                GestorSalida.SEND_CIJ_INFO_JOIN_PRISME(this.player, this.player.parsePrisme());
            }
        }
    }

    private void prismFight(String packet) {
        if (packet.charAt(2) == 'J') {
            if (this.player.isInPrison())
                return;

            final int PrismeID = this.player.getCurMap().getSubArea().prismId;
            Prisma prism = Mundo.mundo.getPrisme(PrismeID);

            if (prism == null)
                return;

            int FightID = -1, cellID = -1;
            short MapID = -1;
            try {
                FightID = prism.getFightId();
                MapID = prism.getMap();
                cellID = prism.getCell();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (PrismeID == -1 || FightID == -1 || MapID == -1 || cellID == -1)
                return;
            if (this.player.getPelea() != null || prism.getAlignement() != this.player.get_align() || this.player.isDead() == 1 || Mundo.mundo.getMap(MapID) == null) {
                GestorSalida.GAME_SEND_BN(this.player);
                return;
            }

            final short map = MapID;
            final int cell = cellID;
            final Pelea fight = Mundo.mundo.getMap(map).getFight(FightID);

            if (fight == null) {
                GestorSalida.GAME_SEND_BN(this.player);
                return;
            }

            if (this.player.getCurMap().getId() != MapID) {
                this.player.setCurMap(this.player.getCurMap());
                this.player.setCurCell(this.player.getCurCell());
                this.player.teleport(map, cell);
            }

            Temporizador.addSiguiente(() -> {
                fight.joinPrismFight(this.player, (fight.getInit0().isPrisme() ? fight.getInit0() : fight.getInit1()).getTeam());
                Mundo.mundo.getOnlinePlayers().stream().filter(Objects::nonNull).filter(player -> player.get_align() == player.get_align()).forEach(Prisma::parseDefense);
            }, 2, TimeUnit.SECONDS, Temporizador.DataType.CLIENTE);
        }
    }
    // Fin paketes de conquista

    //Paketes de chats
    private void parseChanelPacket(String packet) {
        if (packet.charAt(1) == 'C') {//Changement des Canaux
            subscribeChannels(packet);
        }
    }

    private void subscribeChannels(String packet) {
        String chan = packet.charAt(3) + "";
        switch (packet.charAt(2)) {
            //Ajthis du Canal
            case '+' -> this.player.addChanel(chan);
            //Desactivation du canal
            case '-' -> this.player.removeChanel(chan);
        }
        Database.dinamicos.getPlayerData().update(this.player);
    }
    //Fin paketes de chat

    //Pakete de dialogo
    private void parseDialogPacket(String packet) {
        switch (packet.charAt(1)) {
            //Demande de l'initQuestion
            case 'C' -> create(packet);
            //R�ponse du joueur
            case 'R' -> response(packet);
            //Fin du dialog
            case 'V' -> leave();
        }

        final Grupo party = this.player.getParty();

        if(party != null && this.player.getPelea() == null && party.getMaster() != null && party.getMaster().getName().equals(this.player.getName())) {
            Temporizador.addSiguiente(() -> party.getPlayers().stream().filter((follower1) -> party.isWithTheMaster(follower1, false))
                    .forEach(follower -> follower.getGameClient().parseDialogPacket(packet)), 0, TimeUnit.SECONDS, Temporizador.DataType.CLIENTE);
        }
    }

    private void create(String packet) {
        try {
            if (this.player.isInAreaNotSubscribe() || this.player.getExchangeAction() != null) {
                GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(), 'S');
                return;
            }

            int id = Integer.parseInt(packet.substring(2).split((char) 0x0A + "")[0]);
            Recaudador collector = Mundo.mundo.getCollector(id);

            if (collector != null && collector.getMap() == this.player.getCurMap().getId()) {
                GestorSalida.GAME_SEND_DCK_PACKET(this, id);
                GestorSalida.GAME_SEND_QUESTION_PACKET(this, Mundo.mundo.getGuild(collector.getGuildId()).parseQuestionTaxCollector());
                return;
            }

            Npc npc = this.player.getCurMap().getNpc(id);
            if (npc == null) return;

            GestorSalida.GAME_SEND_DCK_PACKET(this, id);
            int questionId = npc.getTemplate().getInitQuestionId(this.player.getCurMap().getId());

            NpcPregunta question = Mundo.mundo.getNPCQuestion(questionId);

            if (question == null) {
                GestorSalida.GAME_SEND_END_DIALOG_PACKET(this);
                return;
            }

            if (npc.getTemplate().getId() == 870) {
                Mision quest = Mision.getQuestById(185);
                if (quest != null) {
                    MisionJugador questPlayer = this.player.getQuestPersoByQuest(quest);
                    if (questPlayer != null) {
                        if (questPlayer.isFinish()) {
                            GestorSalida.GAME_SEND_END_DIALOG_PACKET(this);
                            return;
                        }
                    }
                }
            } else if (npc.getTemplate().getId() == 891) {
                Mision quest = Mision.getQuestById(200);
                if (quest != null) 
                    if (this.player.getQuestPersoByQuest(quest) == null)
                        quest.applyQuest(this.player);
            } else if (npc.getTemplate().getId() == 925 && this.player.getCurMap().getId() == (short) 9402) {
                Mision quest = Mision.getQuestById(231);
                if (quest != null) {
                    MisionJugador questPlayer = this.player.getQuestPersoByQuest(quest);
                    if (questPlayer != null) {
                        if (questPlayer.isFinish()) {
                            question = Mundo.mundo.getNPCQuestion(4127);
                            if (question == null) {
                                GestorSalida.GAME_SEND_END_DIALOG_PACKET(this);
                                return;
                            }
                        }
                    }
                }
            } else if (npc.getTemplate().getId() == 577 && this.player.getCurMap().getId() == (short) 7596) {
                if (this.player.hasItemTemplate(2106, 1))
                    question = Mundo.mundo.getNPCQuestion(2407);
            } else if (npc.getTemplate().getId() == 1041 && this.player.getCurMap().getId() == (short) 10255 && questionId == 5516) {
                if (this.player.get_align() == 1) {// bontarien
                    if (this.player.getSexe() == 0)
                        question = Mundo.mundo.getNPCQuestion(5519);
                    else
                        question = Mundo.mundo.getNPCQuestion(5520);
                } else if (this.player.get_align() == 2) {// brakmarien
                    if (this.player.getSexe() == 0)
                        question = Mundo.mundo.getNPCQuestion(5517);
                    else
                        question = Mundo.mundo.getNPCQuestion(5518);
                } else { // Neutre ou mercenaire
                    question = Mundo.mundo.getNPCQuestion(5516);
                }
            }

            AccionIntercambiar<Integer> exchangeAction = new AccionIntercambiar<>(AccionIntercambiar.TALKING_WITH, id);
            this.player.setExchangeAction(exchangeAction);

            GestorSalida.GAME_SEND_QUESTION_PACKET(this, question.parse(this.player));

            for (MisionJugador questPlayer :  new ArrayList<>(this.player.getQuestPerso().values())) {
                boolean loc1 = false;
                for (MisionEtapa questStep : questPlayer.getQuest().getQuestSteps())
                    if (questStep.getNpc() != null && questStep.getNpc().getId() == this.player.getCurMap().getNpc(exchangeAction.getValue()).getTemplate().getId())
                        loc1 = true;

                Mision quest = questPlayer.getQuest();
                if (quest == null || questPlayer.isFinish()) continue;
                NpcModelo npcTemplate = quest.getNpcTemplate();
                if (npcTemplate == null && !loc1) continue;

                quest.updateQuestData(this.player, false, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void response(String packet) {
        String[] infos = packet.substring(2).split("\\|");
        try {
            AccionIntercambiar<?> checkExchangeAction = this.player.getExchangeAction();
            if (checkExchangeAction == null || checkExchangeAction.getType() != AccionIntercambiar.TALKING_WITH) return;

            AccionIntercambiar<Integer> exchangeAction = (AccionIntercambiar<Integer>) this.player.getExchangeAction();
            if (this.player.getCurMap().getNpc(exchangeAction.getValue()) == null) return;

            int answerId = Integer.parseInt(infos[1]);
            NpcPregunta question = Mundo.mundo.getNPCQuestion(Integer.parseInt(infos[0]));
            NpcRespuesta answer = Mundo.mundo.getNpcAnswer(answerId);

            if (question == null || answer == null) {
                this.player.setIsOnDialogAction(-1);
                GestorSalida.GAME_SEND_END_DIALOG_PACKET(this);
                return;
            }

            try {
                if (!this.player.getQuestPerso().isEmpty()) {
                    for (MisionJugador QP : this.player.getQuestPerso().values()) {
                        if (QP.isFinish() || QP.getQuest() == null
                                || QP.getQuest().getNpcTemplate() == null)
                            continue;
                        ArrayList<MisionEtapa> QEs = QP.getQuest().getQuestSteps();
                        for (MisionEtapa qe : QEs) {
                            if (qe == null)
                                continue;
                            if (QP.isQuestStepIsValidate(qe))
                                continue;

                            NpcModelo npc = qe.getNpc();
                            NpcModelo curNpc = this.player.getCurMap().getNpc(exchangeAction.getValue()).getTemplate();

                            if (npc == null || curNpc == null)
                                continue;
                            if (npc.getId() == curNpc.getId())
                                QP.getQuest().updateQuestData(this.player, false, answerId);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (answerId == 6604 || answerId == 6605) {
                String stats = "", statsReplace = "";
                if (this.player.hasItemTemplate(10207, 1))
                    stats = this.player.getItemTemplate(10207).getTxtStat().get(Constantes.STATS_NAME_DJ);
                try {
                    for(String answer0 : question.getAnwsers().split(";")) {
                        for (Accion action : Mundo.mundo.getNpcAnswer(Integer.parseInt(answer0)).getActions()) {
                            if ((action.getId() == 15 || action.getId() == 16) && this.player.hasItemTemplate(10207, 1)) {
                                for (String i : stats.split(",")) {
                                    Mapa map = this.player.getCurMap();
                                    if (map != null) {
                                        Npc npc = map.getNpc(exchangeAction.getValue());
                                        if (npc != null && npc.getTemplate() != null && Dopeul.parseConditionTrousseau(i.replace(" ", ""), npc.getTemplate().getId(), map.getId())) {
                                            this.player.teleport(Short.parseShort(action.getArgs().split(",")[0]), Integer.parseInt(action.getArgs().split(",")[1]));
                                            statsReplace = i;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }

                if (answerId == 6605 && !statsReplace.isEmpty()) {
                    StringBuilder newStats = new StringBuilder();
                    for (String i : stats.split(","))
                        if (!i.equals(statsReplace))
                            newStats.append((newStats.length() == 0) ? i : "," + i);
                    this.player.getItemTemplate(10207).getTxtStat().remove(Constantes.STATS_NAME_DJ);
                    this.player.getItemTemplate(10207).getTxtStat().put(Constantes.STATS_NAME_DJ, newStats.toString());
                }
                GestorSalida.GAME_SEND_UPDATE_ITEM(this.player, this.player.getItemTemplate(10207));
            } else if (answerId == 4628) {
                if (this.player.hasItemTemplate(9487, 1)) {
                    String date = this.player.getItemTemplate(9487, 1).getTxtStat().get(Constantes.STATS_DATE);
                    long timeStamp = Long.parseLong(date);
                    if (Instant.now().toEpochMilli() - timeStamp <= 1209600000) {
                        new Accion(1, "5522", "", Mundo.mundo.getMap((short) 10255)).apply(this.player, null, -1, -1);
                        return;
                    }
                }
                new Accion(1, "5521", "", Mundo.mundo.getMap((short) 10255)).apply(this.player, null, -1, -1);
                return;
            }

            boolean leave = answer.apply(this.player);

            if (!answer.isAnotherDialog()) {
                if (this.player.getIsOnDialogAction() == 1) {
                    this.player.setIsOnDialogAction(-1);
                } else {
                    if (leave) {
                        GestorSalida.GAME_SEND_END_DIALOG_PACKET(this);
                        if(this.player.getExchangeAction() != null && this.player.getExchangeAction().getType() == AccionIntercambiar.TALKING_WITH)
                            this.player.setExchangeAction(null);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.player.setIsOnDialogAction(-1);
            this.player.setExchangeAction(null);
            GestorSalida.GAME_SEND_END_DIALOG_PACKET(this);
        }
    }

    private void leave() {
        this.player.setAway(false);
        this.walk = false;
        if (this.player.getExchangeAction() != null && this.player.getExchangeAction().getType() == AccionIntercambiar.TALKING_WITH)
            this.player.setExchangeAction(null);
        GestorSalida.GAME_SEND_END_DIALOG_PACKET(this);
    }
    //Fin pakete de dialogo

    //Paketes de documentos
    private void parseDocumentPacket(String packet) {
        if (packet.charAt(1) == 'V') {
            this.player.send("dV");
        }
    }
    //Paketes de documentos - fin

   //Paketes de intercambio
    private synchronized void parseExchangePacket(String packet) {
        if (this.player.isDead() == 1)
            return;
        switch (packet.charAt(1)) {
//Accepter demande d'�change
            case 'A' -> accept();
//Achat
            case 'B' -> buy(packet);
//Demande prix moyen + cat�gorie
            case 'H' -> bigStore(packet);
//Ok
            case 'K' -> ready();
//jobAction : Refaire le craft pr�cedent
            case 'L' -> replayCraft();
//Move (Ajthiser//retirer un objet a l'�change)
            case 'M' -> movementItemOrKamas(packet);
            case 'P' -> movementItemOrKamasDons(packet.substring(2));
//Mode marchand (demande de la taxe)
            case 'q' -> askOfflineExchange();
//Mode marchand (Si valider apr�s la taxe)
            case 'Q' -> offlineExchange();
//Rides => Monture
            case 'r' -> putInInventory(packet);
//Etable => Enclos
            case 'f' -> putInMountPark(packet);
//liste d'achat NPC
            case 'R' -> request(packet);
//Vente
            case 'S' -> sell(packet);
//Livre artisant
            case 'J' -> bookOfArtisant(packet);
//Metier public
            case 'W' -> setPublicMode(packet);
//Fin de l'�change
            case 'V' -> leaveExchange(this.player);
        }
    }

    private void accept() {
        AccionIntercambiar<?> checkExchangeAction = this.player.getExchangeAction();

        if (MainServidor.INSTANCE.getTradeAsBlocked() || this.player.isDead() == 1 || checkExchangeAction == null || !(checkExchangeAction.getValue() instanceof Integer) || (checkExchangeAction.getType() != AccionIntercambiar.TRADING_WITH_PLAYER && checkExchangeAction.getType() != AccionIntercambiar.CRAFTING_SECURE_WITH))
            return;

        AccionIntercambiar<Integer> exchangeAction = (AccionIntercambiar<Integer>) this.player.getExchangeAction();
        Jugador target = Mundo.mundo.getPlayer(exchangeAction.getValue());
        if(target == null) return;

        checkExchangeAction = target.getExchangeAction();

        if (target.isDead() == 1 || checkExchangeAction == null || !(checkExchangeAction.getValue() instanceof Integer) || (checkExchangeAction.getType() != AccionIntercambiar.TRADING_WITH_PLAYER && checkExchangeAction.getType() != AccionIntercambiar.CRAFTING_SECURE_WITH))
            return;

        int type = this.player.getIsCraftingType().get(0);

        switch (type) {
// Echange PlayerVsPlayer
            case 1 -> {
                GestorSalida.GAME_SEND_EXCHANGE_CONFIRM_OK(this, 1);
                GestorSalida.GAME_SEND_EXCHANGE_CONFIRM_OK(target.getGameClient(), 1);
                IntercambioJugador exchange = new IntercambioJugador(target, this.player);
                AccionIntercambiar newExchangeAction = new AccionIntercambiar<>(AccionIntercambiar.TRADING_WITH_PLAYER, exchange);
                this.player.setExchangeAction(newExchangeAction);
                target.setExchangeAction(newExchangeAction);
                this.player.getIsCraftingType().clear();
                target.getIsCraftingType().clear();
            }
            case 12, 13 -> {
                Jugador player1 = (target.getIsCraftingType().get(0) == 12 ? target : this.player);
                Jugador player2 = (target.getIsCraftingType().get(0) == 13 ? target : this.player);
                CraftSeguro craftSecure = new CraftSeguro(player1, player2);
                GestorSalida.GAME_SEND_ECK_PACKET(this, type, craftSecure.getMaxCase() + ";" + this.player.getIsCraftingType().get(1));
                GestorSalida.GAME_SEND_ECK_PACKET(target.getGameClient(), target.getIsCraftingType().get(0), craftSecure.getMaxCase() + ";" + this.player.getIsCraftingType().get(1));
                AccionIntercambiar newExchangeAction = new AccionIntercambiar<>(AccionIntercambiar.CRAFTING_SECURE_WITH, craftSecure);
                this.player.setExchangeAction(newExchangeAction);
                target.setExchangeAction(newExchangeAction);
            }
        }
    }

    private void buy(String packet) {
        String[] infos = packet.substring(2).split("\\|");

        AccionIntercambiar<?> checkExchangeAction = this.player.getExchangeAction();
        if(checkExchangeAction == null || !(checkExchangeAction.getValue() instanceof Integer) || (checkExchangeAction.getType() != AccionIntercambiar.TRADING_WITH_OFFLINE_PLAYER && checkExchangeAction.getType() != AccionIntercambiar.TRADING_WITH_NPC)) return;

        AccionIntercambiar<Integer> exchangeAction = (AccionIntercambiar<Integer>) this.player.getExchangeAction();

        if (exchangeAction.getType() == AccionIntercambiar.TRADING_WITH_OFFLINE_PLAYER) {
            Jugador seller = Mundo.mundo.getPlayer(exchangeAction.getValue());
            if (seller != null && seller != this.player) {
                int itemID = 0;
                int qua = 0;
                int price = 0;
                try {
                    itemID = Integer.parseInt(infos[0]);
                    qua = Integer.parseInt(infos[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                    if (!seller.getStoreItems().containsKey(itemID) || qua <= 0) {
                        GestorSalida.GAME_SEND_BUY_ERROR_PACKET(this);
                        return;
                    }
                    price = seller.getStoreItems().get(itemID) * qua;
                    int price2 = seller.getStoreItems().get(itemID);
                    ObjetoJuego itemStore = Mundo.getGameObject(itemID);
                    if (itemStore == null)
                        return;
                    if (price > this.player.getKamas())
                        return;
                    if (qua <= 0 || qua > 100000)
                        return;
                    if (qua > itemStore.getCantidad())
                        qua = itemStore.getCantidad();
                    if (qua == itemStore.getCantidad()) {
                        seller.getStoreItems().remove(itemStore.getId());
                        this.player.addObjet(itemStore, true);
                    } else if (itemStore.getCantidad() > qua) {
                        seller.getStoreItems().remove(itemStore.getId());
                        itemStore.setCantidad(itemStore.getCantidad() - qua);
                        seller.addStoreItem(itemStore.getId(), price2);

                        ObjetoJuego clone = ObjetoJuego.getCloneObjet(itemStore, qua);
                        if (this.player.addObjet(clone, true))
                            Mundo.addGameObject(clone, false);
                    } else {
                        GestorSalida.GAME_SEND_BUY_ERROR_PACKET(this);
                        return;
                    }

                    //remove kamas
                    this.player.addKamas(-price);
                    //add seller kamas
                    seller.addKamas(price);
                    Database.dinamicos.getPlayerData().update(seller);
                    //send packets
                    GestorSalida.GAME_SEND_STATS_PACKET(this.player);
                    GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(seller, this.player);
                    GestorSalida.GAME_SEND_BUY_OK_PACKET(this);
                    if (seller.getStoreItems().isEmpty()) {
                        if (Mundo.mundo.getSeller(seller.getCurMap().getId()) != null
                                && Mundo.mundo.getSeller(seller.getCurMap().getId()).contains(seller.getId())) {
                            Mundo.mundo.removeSeller(seller.getId(), seller.getCurMap().getId());
                            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(seller.getCurMap(), seller.getId());
                            leaveExchange(this.player);
                        }
                    }
            }
        } else {

            try {
                int id = Integer.parseInt(infos[0]), qua = Integer.parseInt(infos[1]);

                if (qua <= 0 || qua > 100000)
                    return;

                ObjetoModelo template = Mundo.mundo.getObjetoModelo(id);
                Npc npc = this.player.getCurMap().getNpc(exchangeAction.getValue());

                if (template == null) {
                    GestorSalida.GAME_SEND_BUY_ERROR_PACKET(this);
                    return;
                }
                if (template.getType() == 18 && qua > 1) {
                    this.player.sendMessage("Merci de n'acheter qu'un seul familier à la fois !");
                    return;
                }
                if (npc == null)
                    return;

                NpcModelo npcTemplate = npc.getTemplate();

                if (!npcTemplate.haveItem(id)) {
                    GestorSalida.GAME_SEND_BUY_ERROR_PACKET(this);
                    return;
                }

                boolean attachObject = (npcTemplate.getInformations() & 0x2) == 2;
                ObjetoJuego object = null;
                
                if (template.getPoints() > 0 && (npcTemplate.getInformations() & 0x4) == 4) {
                    int value = template.getPoints() * qua, points = this.account.getPoints();

                    if (points < value) {
                        GestorSalida.GAME_SEND_MESSAGE(this.player, "Vous n'avez pas assez de points pour acheter cet article, vous avez actuellement " + points + "  points boutique et vous manquent " + (value - points) + " points pour pouvoir l'acheter.");
                        GestorSalida.GAME_SEND_BUY_ERROR_PACKET(this);
                        return;
                    }

                    this.account.setPoints(points - value);
                    object = template.createNewItem(qua, (npcTemplate.getInformations() & 0x1) == 1);
                    
                    if (this.player.addObjet(object, true)) Mundo.addGameObject(object, true);
                    if (attachObject) object.attachToPlayer(this.player);

                    GestorSalida.GAME_SEND_BUY_OK_PACKET(this);
                    GestorSalida.GAME_SEND_STATS_PACKET(this.player);
                    GestorSalida.GAME_SEND_Ow_PACKET(this.player);
                    GestorSalida.GAME_SEND_MESSAGE(this.player, "Il te reste : " + (points - value) + " points boutique !");
                } else if (template.getPoints() == 0) {
                    int price = template.getPrice() * qua;
                    if (price < 0) return;

                    if (this.player.getKamas() < price) {
                        GestorSalida.GAME_SEND_BUY_ERROR_PACKET(this);
                        return;
                    }

                    object = template.createNewItem(qua, (npcTemplate.getInformations() & 0x1) == 1);
                    
                    this.player.setKamas(this.player.getKamas() - price);
                    if (this.player.addObjet(object, true)) Mundo.addGameObject(object, true);
                    if (attachObject) object.attachToPlayer(this.player);
                    GestorSalida.GAME_SEND_BUY_OK_PACKET(this);
                    GestorSalida.GAME_SEND_STATS_PACKET(this.player);
                    GestorSalida.GAME_SEND_Ow_PACKET(this.player);
                }
                
                if (object != null && template.getType() == Constantes.ITEM_TYPE_CERTIF_MONTURE) {
                    Montura mount = new Montura(Constantes.getMountColorByParchoTemplate(object.getModelo().getId()), this.getPlayer().getId(), false);
                    object.clearStats();
                    object.getCaracteristicas().addOneStat(995, -(mount.getId()));
                    object.getTxtStat().put(996, this.getPlayer().getName());
                    object.getTxtStat().put(997, mount.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
                GestorSalida.GAME_SEND_BUY_ERROR_PACKET(this);
            }
        }
    }

    private void bigStore(String packet) {
        if (this.player.getExchangeAction() == null || this.player.getExchangeAction().getType() != AccionIntercambiar.AUCTION_HOUSE_BUYING || this.player.getPelea() != null || this.player.isAway())
            return;
        AccionIntercambiar<Integer> exchangeAction = (AccionIntercambiar<Integer>) this.player.getExchangeAction();
        int templateID;
        switch (packet.charAt(2)) {
//Confirmation d'achat
            case 'B' -> {
                String[] info = packet.substring(3).split("\\|");//ligneID|amount|price
                Mercadillo curHdv = Mundo.mundo.getHdv(Math.abs(exchangeAction.getValue()));
                int ligneID = Integer.parseInt(info[0]);
                byte amount = Byte.parseByte(info[1]);
                MercadilloLinea hL = curHdv.getLine(ligneID);
                if (hL == null) {
                    GestorSalida.GAME_SEND_MESSAGE(this.player, "[1] Une erreur est survenue lors de la confirmation d'achat. Veuillez contactacter un administrateur.");
                    return;
                }
                MercadilloEntrada hE = hL.doYouHave(amount, Integer.parseInt(info[2]));
                if (hE == null) {
                    // Intervient lorsque un client ach�te plusieurs fois la m�me ressource.
                    // Par exemple une pyrute � 45'000k trois fois. Au bout d'un moment elle monte � 100'000k, mais le client
                    // voit toujours 45'000k. Il doit il y avoir un manque de paquet envoy�. La 4�me avait bugg�.
                    GestorSalida.GAME_SEND_MESSAGE(this.player, "[2 - Template '"
                            + hL.getTemplateId()
                            + "'] Une erreur est survenue lors de la confirmation d'achat. Veuillez contactacter un administrateur.");
                    return;
                }
                Integer owner = hE.getOwner();
                if (owner == null) {
                    GestorSalida.GAME_SEND_MESSAGE(this.player, "[3 - Template '"
                            + hL.getTemplateId()
                            + "'] Cet objet n'a pas de propriétaire. Contactez un administrateur.");
                    return;
                }
                if (owner == this.player.getAccount().getId()) {
                    GestorSalida.GAME_SEND_MESSAGE(this.player, "Tu ne peux pas acheter ton propre objet.");
                    return;
                }
                if (curHdv.buyItem(ligneID, amount, Integer.parseInt(info[2]), this.player)) {

                    GestorSalida.GAME_SEND_EHm_PACKET(this.player, "-", ligneID + "");//Enleve la ligne
                    if (curHdv.getLine(ligneID) != null
                            && !curHdv.getLine(ligneID).isEmpty())
                        GestorSalida.GAME_SEND_EHm_PACKET(this.player, "+", curHdv.getLine(ligneID).parseToEHm());//R�ajthise la ligne si elle n'est pas vide
                    this.player.refreshStats();
                    GestorSalida.GAME_SEND_Ow_PACKET(this.player);
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "068");//Envoie le message "Lot achet�"
                } else {
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "172");//Envoie un message d'erreur d'achat
                }
            }
//Demande listage d'un template (les prix)
            case 'l' -> {
                templateID = Integer.parseInt(packet.substring(3));
                try {
                    GestorSalida.GAME_SEND_EHl(this.player, Mundo.mundo.getHdv(Math.abs(exchangeAction.getValue())), templateID);
                } catch (NullPointerException e)//Si erreur il y a, retire le template de la liste chez le client
                {
                    e.printStackTrace();
                    GestorSalida.GAME_SEND_EHM_PACKET(this.player, "-", templateID + "");
                }
            }
//Demande des prix moyen
            case 'P' -> {
                templateID = Integer.parseInt(packet.substring(3));
                GestorSalida.GAME_SEND_EHP_PACKET(this.player, templateID);
            }
//Demande des template de la cat�gorie
            case 'T' -> {
                int categ = Integer.parseInt(packet.substring(3));
                String allTemplate = Mundo.mundo.getHdv(Math.abs(exchangeAction.getValue())).parseTemplate(categ);
                GestorSalida.GAME_SEND_EHL_PACKET(this.player, categ, allTemplate);
            }
//search
            case 'S' -> {
                String[] infos = packet.substring(3).split("\\|");//type | templateId
                int id = Integer.parseInt(infos[1]), category = Integer.parseInt(infos[0]);
                Mercadillo hdv = Mundo.mundo.getHdv(Math.abs(exchangeAction.getValue()));
                String templates = Mundo.mundo.getHdv(Math.abs(exchangeAction.getValue())).parseTemplate(category);
                if (templates.isEmpty()) {
                    this.player.send("EHS");
                } else {
                    this.player.send("EHSK");
                    GestorSalida.GAME_SEND_EHL_PACKET(this.player, category, templates);
                    GestorSalida.GAME_SEND_EHP_PACKET(this.player, id);
                    try {
                        GestorSalida.GAME_SEND_EHl(this.player, Mundo.mundo.getHdv(Math.abs(exchangeAction.getValue())), id);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        GestorSalida.GAME_SEND_EHM_PACKET(this.player, "-", String.valueOf(id));
                    }
                }
            }
        }
    }

    private void ready() {
        if(this.player.getExchangeAction() == null) return;

        AccionIntercambiar<?> exchangeAction = this.player.getExchangeAction();
        Object value = exchangeAction.getValue();

        if (exchangeAction.getType() == AccionIntercambiar.CRAFTING && value instanceof OficioAccion) {
            if (((OficioAccion) value).isCraft()) {
                ((OficioAccion) value).startCraft(this.player);
            }
        }

        if (exchangeAction.getType() == AccionIntercambiar.TRADING_WITH_NPC_EXCHANGE && value instanceof IntercambioJugador.NpcExchange)
            ((IntercambioJugador.NpcExchange) value).toogleOK(false);

        if (exchangeAction.getType() == AccionIntercambiar.TRADING_WITH_NPC_PETS && value instanceof IntercambioJugador.NpcExchangePets)
            ((IntercambioJugador.NpcExchangePets) value).toogleOK(false);

        if (exchangeAction.getType() == AccionIntercambiar.TRADING_WITH_NPC_PETS_RESURRECTION && value instanceof IntercambioJugador.NpcRessurectPets)
            ((IntercambioJugador.NpcRessurectPets) value).toogleOK(false);

        if ((exchangeAction.getType() == AccionIntercambiar.TRADING_WITH_PLAYER || exchangeAction.getType() == AccionIntercambiar.CRAFTING_SECURE_WITH) && value instanceof Intercambio)
            if (((Intercambio) value).toogleOk(this.player.getId()))
                ((Intercambio) value).apply();

        if (exchangeAction.getType() == AccionIntercambiar.BREAKING_OBJECTS && value instanceof RomperObjetos) {
            if (((RomperObjetos) value).getObjetos().isEmpty())
                return;

            FragmentosMagicos fragment = new FragmentosMagicos(Database.dinamicos.getObjectData().getNextId(), "");

            for (Doble<Integer, Integer> couple : ((RomperObjetos) value).getObjetos()) {
                ObjetoJuego object = this.player.getItems().get(couple.getPrimero());

                if (object == null || couple.getSegundo() < 1 || object.getCantidad() < couple.getSegundo()) {
                    this.player.send("Ea3");
                    break;
                }

                for (int k = couple.getSegundo(); k > 0; k--) {
                    int type = object.getModelo().getType();
                    if (type > 11 && type < 16 && type > 23 && type != 81 && type != 82)
                        continue;
                    for (Map.Entry<Integer, Integer> entry1 : object.getCaracteristicas().getEffects().entrySet()) {
                        int jet = entry1.getValue();
                        for (Runas rune : Runas.runes) {
                            if (entry1.getKey() == rune.getCharacteristic()) {
                                if (rune.getId() == 1557 || rune.getId() == 1558 || rune.getId() == 7438) {
                                    double puissance = 1.5 * (Math.pow(object.getModelo().getLevel(), 2.0) / Math.pow(rune.getWeight(), (5.0 / 4.0))) + ((jet - 1) / rune.getWeight()) * (66.66 - 1.5 * (Math.pow(object.getModelo().getLevel(), 2.0) / Math.pow(rune.getWeight(), (55.0 / 4.0))));
                                    int chance = (int) Math.ceil(puissance);

                                    if (chance > 66) chance = 66;
                                    else if (chance <= 0) chance = 1;
                                    if (Formulas.getRandomValue(1, 100) <= chance)
                                        fragment.addRune(rune.getId());
                                } else {
                                    double val = (double) rune.getBonus();
                                    if (rune.getId() == 7451 || rune.getId() == 10662) val *= 3.0;

                                    double tauxGetMin = Mundo.mundo.getTauxObtentionIntermediaire(val, true, (val != 30)), tauxGetMax = (tauxGetMin / (2.0 / 3.0)) / 0.9;
                                    int tauxMax = (int) Math.ceil(tauxGetMax), tauxGet = (int) Math.ceil(tauxGetMin), tauxMin = 2 * (tauxMax - tauxGet) - 2;

                                    if (rune.getId() == 7433 || rune.getId() == 7434 || rune.getId() == 7435 || rune.getId() == 7441)
                                        tauxMax++;
                                    if (jet < tauxMin) continue;

                                    for (int i = jet; i > 0; i -= tauxMax) {
                                        int j = 0;
                                        j = Math.min(i, tauxMax);
                                        if (j == tauxMax) fragment.addRune(rune.getId());
                                        else if (Formulas.getRandomValue(1, 100) < (100 * (tauxMax - j) / (tauxMax - tauxMin)))
                                            fragment.addRune(rune.getId());
                                    }
                                }
                            }
                        }
                    }
                }

                if (couple.getSegundo() == object.getCantidad()) {
                    this.player.deleteItem(object.getId());
                    Mundo.mundo.removeGameObject(object.getId());
                    GestorSalida.SEND_OR_DELETE_ITEM(this, object.getId());
                } else {
                    object.setCantidad(object.getCantidad() - couple.getSegundo());
                    GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, object);
                }
            }

            Mundo.addGameObject(fragment, true);
            this.player.addObjet(fragment);
            GestorSalida.GAME_SEND_Ec_PACKET(this.player, "K;8378");
            GestorSalida.GAME_SEND_Ow_PACKET(this.player);
            GestorSalida.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+8378");
            this.player.startActionOnCell(this.player.getGameAction());
            ((RomperObjetos) value).getObjetos().clear();
        }
    }

    private void replayCraft() {
        if (this.player.getExchangeAction() != null && this.player.getExchangeAction().getType() == AccionIntercambiar.CRAFTING)
            if (((OficioAccion) this.player.getExchangeAction().getValue()).getJobCraft() == null)
                ((OficioAccion) this.player.getExchangeAction().getValue()).putLastCraftIngredients();
    }

    private synchronized void movementItemOrKamas(String packet) {
        if(this.player.getExchangeAction() == null) return;
        if(packet.contains("NaN")) {
            this.player.sendMessage("Error : StartExchange : (" + this.player.getExchangeAction().getType() + ") : " + packet + "\nA envoyer à un administreur.");
            return;
        }
        switch(this.player.getExchangeAction().getType()) {
            case AccionIntercambiar.TRADING_WITH_ME:
                if (packet.charAt(2) == 'O') {//Objets
                    if (packet.charAt(3) == '+') {
                        String[] infos = packet.substring(4).split("\\|");
                        try {
                            int guid = Integer.parseInt(infos[0]);
                            int qua = Integer.parseInt(infos[1]);
                            int price = Integer.parseInt(infos[2]);

                            ObjetoJuego obj = this.player.getItems().get(guid);
                            if (obj == null)
                                return;
                            if (qua <= 0 || obj.isAttach())
                                return;
                            if (price <= 0)
                                return;

                            if (qua > obj.getCantidad())
                                qua = obj.getCantidad();
                            this.player.addInStore(obj.getId(), price, qua);
                        } catch (NumberFormatException e) {
                            Mundo.mundo.logger.error("Error Echange Store '" + packet + "' => " + e.getMessage());
                            e.printStackTrace();
                            return;
                        }
                    } else {
                        String[] infos = packet.substring(4).split("\\|");
                        try {
                            int guid = Integer.parseInt(infos[0]);
                            int qua = Integer.parseInt(infos[1]);

                            if (qua <= 0)
                                return;
                            ObjetoJuego obj = Mundo.getGameObject(guid);
                            if (obj == null)
                                return;
                            if (qua < 0)
                                return;
                            if (qua > obj.getCantidad())
                                return;
                            if (qua < obj.getCantidad())
                                qua = obj.getCantidad();
                            this.player.removeFromStore(obj.getId(), qua);
                        } catch (NumberFormatException e) {
                            Mundo.mundo.logger.error("Error Echange Store '" + packet + "' => " + e.getMessage());
                            e.printStackTrace();
                            return;
                        }
                    }
                }
                break;

            case AccionIntercambiar.TRADING_WITH_COLLECTOR:
                Recaudador Collector = Mundo.mundo.getCollector((Integer) this.player.getExchangeAction().getValue());
                if (Collector == null || Collector.getInFight() > 0)
                    return;
                switch (packet.charAt(2)) {
                    case 'G'://Kamas
                        if (packet.charAt(3) == '-') //On retire
                        {
                            long P_Kamas = -1;
                            try {
                                P_Kamas = Integer.parseInt(packet.substring(4));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                Mundo.mundo.logger.error("Error Echange CC '" + packet + "' => " + e.getMessage());
                            }
                            if (P_Kamas < 0)
                                return;
                            if (Collector.getKamas() >= P_Kamas) {//Faille non connu ! :p
                                long P_Retrait = Collector.getKamas() - P_Kamas;
                                Collector.setKamas(Collector.getKamas() - P_Kamas);
                                if (P_Retrait < 0) {
                                    P_Retrait = 0;
                                    P_Kamas = Collector.getKamas();
                                }
                                Collector.setKamas(P_Retrait);
                                this.player.addKamas(P_Kamas);
                                GestorSalida.GAME_SEND_STATS_PACKET(this.player);
                                GestorSalida.GAME_SEND_EsK_PACKET(this.player, "G"
                                        + Collector.getKamas());
                            }
                        }
                        break;
                    case 'O'://Objets
                        if (packet.charAt(3) == '-') //On retire
                        {
                            String[] infos = packet.substring(4).split("\\|");
                            int guid = 0;
                            int qua = 0;
                            try {
                                guid = Integer.parseInt(infos[0]);
                                qua = Integer.parseInt(infos[1]);
                            } catch (NumberFormatException e) {
                                // ok
                                return;
                            }

                            if (guid <= 0 || qua <= 0)
                                return;

                            ObjetoJuego obj = Mundo.getGameObject(guid);
                            if (obj == null)
                                return;

                            if (Collector.haveObjects(guid)) {
                                Collector.removeFromCollector(this.player, guid, qua);
                            }
                            Collector.addLogObjects(guid, obj);
                        }
                        break;
                }
                Database.dinamicos.getGuildData().update(this.player.getGuild());
                break;
            case AccionIntercambiar.BREAKING_OBJECTS:
                final RomperObjetos breakingObject = ((RomperObjetos) this.player.getExchangeAction().getValue());

                if (packet.charAt(2) == 'O') {
                    if (packet.charAt(3) == '+') {
                        if (breakingObject.getObjetos().size() >= 8)
                            return;

                        String[] infos = packet.substring(4).split("\\|");

                        try {
                            int id = Integer.parseInt(infos[0]), qua = Integer.parseInt(infos[1]);

                            if (!this.player.hasItemGuid(id))
                                return;

                            ObjetoJuego object = this.player.getItems().get(id);

                            if (object == null)
                                return;
                            if (qua < 1)
                                return;
                            if (qua > object.getCantidad())
                                qua = object.getCantidad();

                            int type = object.getModelo().getType();
                            if (type > 11 && type < 16 && type > 23 && type != 81 && type != 82)
                                return;

                            GestorSalida.SEND_EMK_MOVE_ITEM(this, 'O', "+", id + "|" + breakingObject.addObjeto(id, qua));
                        } catch (NumberFormatException e) {
                            Mundo.mundo.logger.error("Error Echange CC '" + packet + "' => " + e.getMessage());
                            e.printStackTrace();
                            return;
                        }
                    } else if (packet.charAt(3) == '-') {
                        String[] infos = packet.substring(4).split("\\|");
                        try {
                            int id = Integer.parseInt(infos[0]);
                            int qua = Integer.parseInt(infos[1]);

                            ObjetoJuego object = Mundo.getGameObject(id);

                            if (object == null)
                                return;
                            if (qua < 1)
                                return;

                            final int quantity = breakingObject.RemoverObjeto(id, qua);

                            if (quantity <= 0)
                                GestorSalida.SEND_EMK_MOVE_ITEM(this, 'O', "-", id + "");
                            else
                                GestorSalida.SEND_EMK_MOVE_ITEM(this, 'O', "+", id + "|" + quantity);
                        } catch (NumberFormatException e) {
                            Mundo.mundo.logger.error("Error Echange CC '" + packet + "' => " + e.getMessage());
                            e.printStackTrace();
                            return;
                        }
                    }
                } else if(packet.charAt(2) == 'R') {
                    final int count = Integer.parseInt(packet.substring(3));
                    breakingObject.setContador(count);
                    Temporizador.addSiguiente(() -> this.recursiveBreakingObject(breakingObject, 0, count), 0, Temporizador.DataType.CLIENTE);
                } else if(packet.charAt(2) == 'r') {
                    breakingObject.setDetenerse(true);
                }
                break;
            case AccionIntercambiar.IN_MOUNT:
                Montura mount = this.player.getMount();
                if (mount == null) return;
                // Objet
                if (packet.charAt(2) == 'O') {
                    int id = 0;
                    int cant = 0;
                    try {
                        id = Integer.parseInt(packet.substring(4).split("\\|")[0]);
                        cant = Integer.parseInt(packet.substring(4).split("\\|")[1]);
                    } catch (Exception e) {
                        Mundo.mundo.logger.error("Error Echange DD '" + packet + "' => " + e.getMessage());
                        e.printStackTrace();
                        return;
                    }
                    if (id == 0 || cant <= 0)
                        return;
                    if (Mundo.getGameObject(id) == null) {
                        GestorSalida.GAME_SEND_MESSAGE(this.player, "Erreur 1 d'inventaire de dragodinde : l'objet n'existe pas !");
                        return;
                    }
                    switch (packet.charAt(3)) {
                        case '+':
                            mount.addObject(id, cant, this.player);
                            break;
                        case '-':
                            mount.removeObject(id, cant, this.player);
                            break;
                        case ',':
                            break;
                    }
                }
                break;

            case AccionIntercambiar.TRADING_WITH_NPC_EXCHANGE:
                switch (packet.charAt(2)) {
                    case 'O'://Objet ?
                        if (packet.charAt(3) == '+') {
                            String[] infos = packet.substring(4).split("\\|");
                            try {
                                int guid = Integer.parseInt(infos[0]);
                                int qua = Integer.parseInt(infos[1]);
                                int quaInExch = ((IntercambioJugador.NpcExchange) this.player.getExchangeAction().getValue()).getQuaItem(guid, false);

                                if (!this.player.hasItemGuid(guid)) return;
                                ObjetoJuego obj = this.player.getItems().get(guid);
                                if (obj == null) return;

                                if (qua > obj.getCantidad() - quaInExch)
                                    qua = obj.getCantidad() - quaInExch;
                                if (qua <= 0)
                                    return;

                                ((IntercambioJugador.NpcExchange) this.player.getExchangeAction().getValue()).addItem(guid, qua);
                            } catch (NumberFormatException e) {
                                Mundo.mundo.logger.error("Error Echange NPC '" + packet + "' => " + e.getMessage());
                                e.printStackTrace();
                                return;
                            }
                        } else {
                            String[] infos = packet.substring(4).split("\\|");
                            try {
                                int guid = Integer.parseInt(infos[0]);
                                int qua = Integer.parseInt(infos[1]);

                                if (qua <= 0)
                                    return;
                                if (!this.player.hasItemGuid(guid))
                                    return;

                                ObjetoJuego obj = Mundo.getGameObject(guid);
                                if (obj == null)
                                    return;
                                if (qua > ((IntercambioJugador.NpcExchange) this.player.getExchangeAction().getValue()).getQuaItem(guid, false))
                                    return;

                                ((IntercambioJugador.NpcExchange) this.player.getExchangeAction().getValue()).removeItem(guid, qua);
                            } catch (NumberFormatException e) {
                                Mundo.mundo.logger.error("Error Echange NPC '" + packet + "' => " + e.getMessage());
                                e.printStackTrace();
                                return;
                            }
                        }
                        break;
                    case 'G'://Kamas
                        try {
                            long numb = Integer.parseInt(packet.substring(3));
                            if (this.player.getKamas() < numb)
                                numb = this.player.getKamas();
                            ((IntercambioJugador.NpcExchange) this.player.getExchangeAction().getValue()).setKamas(false, numb);
                        } catch (NumberFormatException e) {
                            Mundo.mundo.logger.error("Error Echange NPC '" + packet + "' => " + e.getMessage());
                            e.printStackTrace();
                            return;
                        }
                        break;
                }
                break;
            case AccionIntercambiar.TRADING_WITH_NPC_PETS:
                switch (packet.charAt(2)) {
                    case 'O'://Objet ?
                        if (packet.charAt(3) == '+') {
                            String[] infos = packet.substring(4).split("\\|");
                            try {
                                int guid = Integer.parseInt(infos[0]);
                                int qua = Integer.parseInt(infos[1]);
                                int quaInExch = ((IntercambioJugador.NpcExchangePets) this.player.getExchangeAction().getValue()).getQuaItem(guid, false);

                                if (!this.player.hasItemGuid(guid))
                                    return;
                                ObjetoJuego obj = this.player.getItems().get(guid);
                                if (obj == null)
                                    return;

                                if (qua > obj.getCantidad() - quaInExch)
                                    qua = obj.getCantidad() - quaInExch;

                                if (qua <= 0)
                                    return;

                                ((IntercambioJugador.NpcExchangePets) this.player.getExchangeAction().getValue()).addItem(guid, qua);
                            } catch (NumberFormatException e) {
                                Mundo.mundo.logger.error("Error Echange Pets '" + packet + "' => " + e.getMessage());
                                e.printStackTrace();
                                return;
                            }
                        } else {
                            String[] infos = packet.substring(4).split("\\|");
                            try {
                                int guid = Integer.parseInt(infos[0]);
                                int qua = Integer.parseInt(infos[1]);

                                if (qua <= 0)
                                    return;
                                if (!this.player.hasItemGuid(guid))
                                    return;

                                ObjetoJuego obj = Mundo.getGameObject(guid);
                                if (obj == null)
                                    return;
                                if (qua > ((IntercambioJugador.NpcExchangePets) this.player.getExchangeAction().getValue()).getQuaItem(guid, false))
                                    return;

                                ((IntercambioJugador.NpcExchangePets) this.player.getExchangeAction().getValue()).removeItem(guid, qua);
                            } catch (NumberFormatException e) {
                                Mundo.mundo.logger.error("Error Echange Pets '" + packet + "' => " + e.getMessage());
                                e.printStackTrace();
                                return;
                            }
                        }
                        break;
                    case 'G'://Kamas
                        try {
                            long numb = Integer.parseInt(packet.substring(3));
                            if (numb < 0)
                                return;
                            if (this.player.getKamas() < numb)
                                numb = this.player.getKamas();
                            ((IntercambioJugador.NpcExchangePets) this.player.getExchangeAction().getValue()).setKamas(false, numb);
                        } catch (NumberFormatException e) {
                            Mundo.mundo.logger.error("Error Echange Pets '" + packet + "' => " + e.getMessage());
                            e.printStackTrace();
                            return;
                        }
                        break;
                }
                break;

            case AccionIntercambiar.TRADING_WITH_NPC_PETS_RESURRECTION:
                switch (packet.charAt(2)) {
                    case 'O'://Objet ?
                        if (packet.charAt(3) == '+') {
                            String[] infos = packet.substring(4).split("\\|");
                            try {

                                int guid = Integer.parseInt(infos[0]);
                                int qua = Integer.parseInt(infos[1]);
                                int quaInExch = ((IntercambioJugador.NpcRessurectPets) this.player.getExchangeAction().getValue()).getQuaItem(guid, false);

                                if (!this.player.hasItemGuid(guid))
                                    return;
                                ObjetoJuego obj = Mundo.getGameObject(guid);
                                if (obj == null)
                                    return;

                                if (qua > obj.getCantidad() - quaInExch)
                                    qua = obj.getCantidad() - quaInExch;

                                if (qua <= 0)
                                    return;

                                ((IntercambioJugador.NpcRessurectPets) this.player.getExchangeAction().getValue()).addItem(guid, qua);
                            } catch (NumberFormatException e) {
                                Mundo.mundo.logger.error("Error Echange RPets '" + packet + "' => " + e.getMessage());
                                e.printStackTrace();
                                return;
                            }
                        } else {
                            String[] infos = packet.substring(4).split("\\|");
                            try {
                                int guid = Integer.parseInt(infos[0]);
                                int qua = Integer.parseInt(infos[1]);

                                if (qua <= 0)
                                    return;
                                if (!this.player.hasItemGuid(guid))
                                    return;

                                ObjetoJuego obj = Mundo.getGameObject(guid);
                                if (obj == null)
                                    return;
                                if (qua > ((IntercambioJugador.NpcRessurectPets) this.player.getExchangeAction().getValue()).getQuaItem(guid, false))
                                    return;

                                ((IntercambioJugador.NpcRessurectPets) this.player.getExchangeAction().getValue()).removeItem(guid, qua);
                            } catch (NumberFormatException e) {
                                Mundo.mundo.logger.error("Error Echange RPets '" + packet + "' => " + e.getMessage());
                                e.printStackTrace();
                                return;
                            }
                        }
                        break;
                    case 'G'://Kamas
                        try {
                            long numb = Integer.parseInt(packet.substring(3));
                            if (numb < 0)
                                return;
                            if (this.player.getKamas() < numb)
                                numb = this.player.getKamas();
                            ((IntercambioJugador.NpcRessurectPets) this.player.getExchangeAction().getValue()).setKamas(false, numb);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            return;
                        }
                        break;
                }
                break;

            case AccionIntercambiar.AUCTION_HOUSE_SELLING:
                switch (packet.charAt(3)) {
//Retirer un objet de l'HDV
                    case '-' -> {
                        int count = 0,
                                cheapestID = 0;
                        try {
                            cheapestID = Integer.parseInt(packet.substring(4).split("\\|")[0]);
                            count = Integer.parseInt(packet.substring(4).split("\\|")[1]);
                        } catch (Exception e) {
                            Mundo.mundo.logger.error("Error Echange HDV '" + packet + "' => " + e.getMessage());
                            e.printStackTrace();
                            return;
                        }
                        if (count <= 0)
                            return;
                        this.player.getAccount().recoverItem(cheapestID);//Retire l'objet de la liste de vente du compte
                        GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this, '-', "", cheapestID
                                + "");
                    }
//Mettre un objet en vente
                    case '+' -> {
                        if (Integer.parseInt(packet.substring(4).split("\\|")[1]) > 127) {
                            GestorSalida.GAME_SEND_MESSAGE(this.player, "Vous avez atteins la limite maximum du nombre d'objet.");
                            return;
                        }
                        int itmID,
                                price = 0;
                        byte amount = 0;
                        try {
                            itmID = Integer.parseInt(packet.substring(4).split("\\|")[0]);
                            amount = Byte.parseByte(packet.substring(4).split("\\|")[1]);
                            price = Integer.parseInt(packet.substring(4).split("\\|")[2]);
                        } catch (Exception e) {
                            Mundo.mundo.logger.error("Error Echange HDV '" + packet + "' => " + e.getMessage());
                            // Arrive quand price n'est pas dans le pacquet. C'est que le joueur ne veut pas mettre dans un hdv, mais dans autre chose ... Un paquet qui est MO+itmID|qt�
                            // Peeut-�tre apr�sa voir utilis� le concasseur ...
                            e.printStackTrace();
                            GestorSalida.GAME_SEND_MESSAGE(this.player, "Une erreur s'est produite lors de la mise en vente de votre objet. Veuillez vous reconnectez pour corriger l'erreur. Personnage "
                                    + this.getPlayer().getName()
                                    + " et paquet " + packet + ".");
                            return;
                        }
                        if (amount <= 0 || price <= 0)
                            return;
                        if (packet.substring(1).split("\\|")[2].equals("0")
                                || packet.substring(2).split("\\|")[2].equals("0")
                                || packet.substring(3).split("\\|")[2].equals("0"))
                            return;
                        Mercadillo curHdv = Mundo.mundo.getHdv(Math.abs((Integer) this.player.getExchangeAction().getValue()));
                        curHdv.getHdvId();
                        int taxe = (int) (price * (curHdv.getTaxe() / 100));
                        if (taxe < 0)
                            return;
                        if (!this.player.hasItemGuid(itmID))//V�rifie si le this.playernnage a bien l'item sp�cifi� et l'argent pour payer la taxe
                            return;
                        if (this.player.getAccount().countHdvEntries(curHdv.getHdvId()) >= curHdv.getMaxAccountItem()) {
                            GestorSalida.GAME_SEND_Im_PACKET(this.player, "058");
                            return;
                        }
                        if (this.player.getKamas() < taxe) {
                            GestorSalida.GAME_SEND_Im_PACKET(this.player, "176");
                            return;
                        }
                        ObjetoJuego obj = Mundo.getGameObject(itmID);//R�cup�re l'item
                        if (obj.isAttach()) return;
                        this.player.addKamas(taxe * -1);//Retire le montant de la taxe au this.playernnage
                        GestorSalida.GAME_SEND_STATS_PACKET(this.player);//Met a jour les kamas du client
                        int qua = (amount == 1 ? 1 : (amount == 2 ? 10 : 100));
                        if (qua > obj.getCantidad())//S'il veut mettre plus de cette objet en vente que ce qu'il poss�de
                            return;
                        int rAmount = (int) (Math.pow(10, amount) / 10);
                        int newQua = (obj.getCantidad() - rAmount);
                        if (newQua <= 0)//Si c'est plusieurs objets ensemble enleve seulement la quantit� de mise en vente
                        {
                            this.player.removeItem(itmID);//Enl�ve l'item de l'inventaire du this.playernnage
                            GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this.player, itmID);//Envoie un packet au client pour retirer l'item de son inventaire
                        } else {
                            obj.setCantidad(obj.getCantidad() - rAmount);
                            GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, obj);
                            ObjetoJuego newObj = ObjetoJuego.getCloneObjet(obj, rAmount);
                            Mundo.addGameObject(newObj, true);
                            obj = newObj;
                        }
                        MercadilloEntrada toAdd = new MercadilloEntrada(Mundo.mundo.getNextObjectHdvId(), price, amount, this.player.getAccount().getId(), obj);
                        curHdv.addEntry(toAdd, false); //Ajthise l'entry dans l'HDV
                        GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this, '+', "", toAdd.parseToEmK()); //Envoie un packet pour ajthiser l'item dans la fenetre de l'HDV du client
                        GestorSalida.GAME_SEND_HDVITEM_SELLING(this.player);
                        Database.dinamicos.getPlayerData().update(this.player);
                    }
                }
                break;

            case AccionIntercambiar.CRAFTING:
                //Si pas action de craft, on s'arrete la
                if (!((OficioAccion) this.player.getExchangeAction().getValue()).isCraft())
                    return;

                if (packet.charAt(2) == 'O' && ((OficioAccion) this.player.getExchangeAction().getValue()).getJobCraft() == null) {
                    packet = packet.replace("-", ";-").replace("+", ";+").substring(4);

                    for(String part : packet.split(";")) {
                        try {
                            char c = part.charAt(0);
                            String[] infos = part.substring(1).split("\\|");
                            int id = Integer.parseInt(infos[0]), quantity = Integer.parseInt(infos[1]);

                            if (quantity <= 0) return;
                            if (c == '+') {
                                if (!this.player.hasItemGuid(id))
                                    return;

                                ObjetoJuego obj = this.player.getItems().get(id);

                                if (obj == null)
                                    return;
                                if (obj.getCantidad() < quantity)
                                    quantity = obj.getCantidad();

                                ((OficioAccion) this.player.getExchangeAction().getValue()).addIngredient(this.player, id, quantity);
                            } else if (c == '-') {
                                ((OficioAccion) this.player.getExchangeAction().getValue()).addIngredient(this.player, id, -quantity);
                            }
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (packet.charAt(2) == 'R') {
                    if (((OficioAccion) this.player.getExchangeAction().getValue()).getJobCraft() == null) {
                        ((OficioAccion) this.player.getExchangeAction().getValue()).setJobCraft(((OficioAccion) this.player.getExchangeAction().getValue()).oldJobCraft);
                    }
                    ((OficioAccion) this.player.getExchangeAction().getValue()).getJobCraft().setAction(Integer.parseInt(packet.substring(3)));
                } else if (packet.charAt(2) == 'r') {
                    if (this.player.getExchangeAction().getValue() != null) {
                        if (((OficioAccion) this.player.getExchangeAction().getValue()).getJobCraft() != null) {
                            ((OficioAccion) this.player.getExchangeAction().getValue()).broken = true;
                        }
                    }
                }
                break;

            case AccionIntercambiar.IN_BANK:
                switch (packet.charAt(2)) {
//Kamas
                    case 'G' -> {
                        if (MainServidor.INSTANCE.getTradeAsBlocked())
                            return;
                        long kamas = 0;
                        try {
                            kamas = Integer.parseInt(packet.substring(3));
                        } catch (Exception e) {
                            Mundo.mundo.logger.error("Error Echange Banque '" + packet + "' => " + e.getMessage());
                            e.printStackTrace();
                            return;
                        }
                        if (kamas == 0)
                            return;
                        if (kamas > 0)//Si On ajoute des kamas a la banque
                        {
                            if (this.player.getKamas() < kamas)
                                kamas = this.player.getKamas();
                            this.player.setBankKamas(this.player.getBankKamas() + kamas);//On ajthise les kamas a la banque
                            this.player.setKamas(this.player.getKamas() - kamas);//On retire les kamas du this.playernnage
                            GestorSalida.GAME_SEND_STATS_PACKET(this.player);
                            GestorSalida.GAME_SEND_EsK_PACKET(this.player, "G"
                                    + this.player.getBankKamas());
                        } else {
                            kamas = -kamas;//On repasse en positif
                            if (this.player.getBankKamas() < kamas)
                                kamas = this.player.getBankKamas();
                            this.player.setBankKamas(this.player.getBankKamas() - kamas);//On retire les kamas de la banque
                            this.player.setKamas(this.player.getKamas() + kamas);//On ajthise les kamas du this.playernnage
                            GestorSalida.GAME_SEND_STATS_PACKET(this.player);
                            GestorSalida.GAME_SEND_EsK_PACKET(this.player, "G"
                                    + this.player.getBankKamas());
                        }
                    }
//Objet
                    case 'O' -> {
                        if (MainServidor.INSTANCE.getTradeAsBlocked())
                            return;
                        int guid = 0;
                        int qua = 0;
                        try {
                            guid = Integer.parseInt(packet.substring(4).split("\\|")[0]);
                            qua = Integer.parseInt(packet.substring(4).split("\\|")[1]);
                        } catch (Exception e) {
                            Mundo.mundo.logger.error("Error Echange Banque '" + packet + "' => " + e.getMessage());
                            e.printStackTrace();
                            return;
                        }
                        if (guid == 0 || qua <= 0)
                            return;
                        switch (packet.charAt(3)) {
//Ajouter a la banque
                            case '+' -> this.player.addInBank(guid, qua);
//Retirer de la banque
                            case '-' -> {
                                ObjetoJuego object = Mundo.getGameObject(guid);
                                if (object != null) {
                                    if (object.getTxtStat().containsKey(Constantes.STATS_OWNER_1)) {
                                        Jugador player = Mundo.mundo.getPlayerByName(object.getTxtStat().get(Constantes.STATS_OWNER_1));
                                        if (player != null) {
                                            if (!player.getName().equals(this.player.getName()))
                                                return;
                                        }
                                    }
                                    this.player.removeFromBank(guid, qua);
                                }
                            }
                        }
                    }
                }
                break;

            case AccionIntercambiar.IN_TRUNK:
                if (MainServidor.INSTANCE.getTradeAsBlocked())
                    return;
                Cofres t = (Cofres) this.player.getExchangeAction().getValue();

                switch (packet.charAt(2)) {
//Kamas
                    case 'G' -> {
                        long kamas = 0;
                        try {
                            kamas = Integer.parseInt(packet.substring(3));
                        } catch (Exception e) {
                            Mundo.mundo.logger.error("Error Echange Coffre '" + packet + "' => " + e.getMessage());
                            e.printStackTrace();
                            return;
                        }
                        if (kamas == 0)
                            return;
                        if (kamas > 0)//Si On ajthise des kamas au coffre
                        {
                            if (this.player.getKamas() < kamas)
                                kamas = this.player.getKamas();
                            t.setKamas(t.getKamas() + kamas);//On ajthise les kamas au coffre
                            this.player.setKamas(this.player.getKamas() - kamas);//On retire les kamas du this.playernnage
                            GestorSalida.GAME_SEND_STATS_PACKET(this.player);
                        } else {
                            kamas = -kamas;//On repasse en positif
                            if (t.getKamas() < kamas)
                                kamas = t.getKamas();
                            t.setKamas(t.getKamas() - kamas);//On retire les kamas de la banque
                            this.player.setKamas(this.player.getKamas() + kamas);//On ajthise les kamas du this.playernnage
                            GestorSalida.GAME_SEND_STATS_PACKET(this.player);
                        }
                        Mundo.mundo.getOnlinePlayers().stream().filter(player -> player.getExchangeAction() != null &&
                                player.getExchangeAction().getType() == AccionIntercambiar.IN_TRUNK &&
                                ((Cofres) this.player.getExchangeAction().getValue()).getId() == ((Cofres) player.getExchangeAction().getValue()).getId())
                                .forEach(P -> GestorSalida.GAME_SEND_EsK_PACKET(P, "G" + t.getKamas()));
                        Database.estaticos.getTrunkData().update(t);
                    }
//Objet
                    case 'O' -> {
                        int guid = 0;
                        int qua = 0;
                        try {
                            guid = Integer.parseInt(packet.substring(4).split("\\|")[0]);
                            qua = Integer.parseInt(packet.substring(4).split("\\|")[1]);
                        } catch (Exception e) {
                            Mundo.mundo.logger.error("Error Echange Coffre '" + packet + "' => " + e.getMessage());
                            e.printStackTrace();
                            return;
                        }
                        if (guid == 0 || qua <= 0)
                            return;
                        switch (packet.charAt(3)) {
//Ajthiser a la banque
                            case '+' -> t.addInTrunk(guid, qua, this.player);
//Retirer de la banque
                            case '-' -> t.removeFromTrunk(guid, qua, this.player);
                        }
                    }
                }
                break;

            case AccionIntercambiar.CRAFTING_SECURE_WITH:
            case AccionIntercambiar.TRADING_WITH_PLAYER:
                switch (packet.charAt(2)) {
                    case 'O'://Objet ?
                        if (packet.charAt(3) == '+') {
                            for(String arg : packet.substring(4).split("\\+")) {
                                String[] infos = arg.split("\\|");
                                try {
                                    int guid = Integer.parseInt(infos[0]);
                                    int qua = Integer.parseInt(infos[1]);
                                    int quaInExch = ((IntercambioJugador) this.player.getExchangeAction().getValue()).getQuaItem(guid, this.player.getId());

                                    if (!this.player.hasItemGuid(guid))
                                        return;
                                    ObjetoJuego obj = this.player.getItems().get(guid);
                                    if (obj == null)
                                        return;
                                    if (qua > obj.getCantidad() - quaInExch)
                                        qua = obj.getCantidad() - quaInExch;

                                    if (qua <= 0 || obj.isAttach())
                                        return;

                                    ((IntercambioJugador) this.player.getExchangeAction().getValue()).addItem(guid, qua, this.player.getId());
                                } catch (NumberFormatException e) {
                                    this.player.sendMessage("Error : PlayerExchange : " + packet + "\n" + e.getMessage());
                                    e.printStackTrace();
                                    return;
                                }
                            }
                        } else {
                            String[] infos = packet.substring(4).split("\\|");
                            try {
                                int guid = Integer.parseInt(infos[0]);
                                int qua = Integer.parseInt(infos[1]);

                                if (qua <= 0)
                                    return;
                                if (!this.player.hasItemGuid(guid))
                                    return;

                                ObjetoJuego obj = this.player.getItems().get(guid);
                                if (obj == null)
                                    return;
                                if (qua > ((IntercambioJugador) this.player.getExchangeAction().getValue()).getQuaItem(guid, this.player.getId()))
                                    return;

                                ((IntercambioJugador) this.player.getExchangeAction().getValue()).removeItem(guid, qua, this.player.getId());
                            } catch (NumberFormatException e) {
                                this.player.sendMessage("Error : PlayerExchange : " + packet + "\n" + e.getMessage());
                                e.printStackTrace();
                                return;
                            }
                        }
                        break;
                    case 'G'://Kamas
                        try {
                            if(packet.substring(3).contains("NaN")) return;
                            long numb = Integer.parseInt(packet.substring(3));
                            if (this.player.getKamas() < numb)
                                numb = this.player.getKamas();
                            if (numb < 0)
                                return;
                            ((IntercambioJugador) this.player.getExchangeAction().getValue()).setKamas(this.player.getId(), numb);
                        } catch (NumberFormatException e) {
                            Mundo.mundo.logger.error("Error Echange PvP '" + packet + "' => " + e.getMessage());
                            e.printStackTrace();
                            return;
                        }
                        break;
                }
                break;
        }
    }

    private void recursiveBreakingObject(RomperObjetos breakingObject, final int i, int count) {
        if (breakingObject.isDetenerse() || !(i < count)) {
            if (breakingObject.isDetenerse()) this.player.send("Ea2");
            else this.player.send("Ea1");
            breakingObject.setDetenerse(false);
            return;
        }

        Temporizador.addSiguiente(() -> {
            this.player.send("EA" + (breakingObject.getContador() - i));
            ArrayList<Doble<Integer, Integer>> objects = new ArrayList<>(breakingObject.getObjetos());
            this.ready();
            breakingObject.setObjetos(objects);
            this.recursiveBreakingObject(breakingObject, i + 1, count);
        }, 1000, TimeUnit.MILLISECONDS, Temporizador.DataType.CLIENTE);
    }

    private synchronized void movementItemOrKamasDons(String packet) {
        if (this.player.getExchangeAction() != null && this.player.getExchangeAction().getType() == AccionIntercambiar.CRAFTING_SECURE_WITH) {
            if (((CraftSeguro) this.player.getExchangeAction().getValue()).getNeeder() == this.player) {
                byte type = Byte.parseByte(String.valueOf(packet.charAt(0)));
                switch (packet.charAt(1)) {
                    case 'O' -> {
                        String[] split = packet.substring(3).split("\\|");
                        boolean adding = packet.charAt(2) == '+';
                        int guid = Integer.parseInt(split[0]), quantity = Integer.parseInt(split[1]);
                        ((CraftSeguro) this.player.getExchangeAction().getValue()).setPayItems(type, adding, guid, quantity);
                    }
                    case 'G' -> ((CraftSeguro) this.player.getExchangeAction().getValue()).setPayKamas(type, Integer.parseInt(packet.substring(2)));
                }
            }
        }
    }

    private void askOfflineExchange() {
        if (this.player.getExchangeAction() != null || this.player.getPelea() != null || this.player.isAway())
            return;
        if (this.player.parseStoreItemsList().isEmpty()) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "123");
            return;
        }
        if (PiedraAlma.isInArenaMap(this.player.getCurMap().getId()) || this.player.getCurMap().noMarchand) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "113");
            return;
        }
        if (this.player.getCurMap().getId() == 33 || this.player.getCurMap().getId() == 38 || this.player.getCurMap().getId() == 4601 || this.player.getCurMap().getId() == 4259 || this.player.getCurMap().getId() == 8036 || this.player.getCurMap().getId() == 10301) {
            if (this.player.getCurMap().getStoreCount() >= 25) {
                GestorSalida.GAME_SEND_Im_PACKET(this.player, "125;25");
                return;
            }
        } else if (this.player.getCurMap().getStoreCount() >= 6) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "125;6");
            return;
        }
        for (Map.Entry<Integer, Integer> entry : this.player.getStoreItems().entrySet()) {
            if (entry.getValue() <= 0) {
                this.kick();
                return;
            }
        }


        long taxe = this.player.storeAllBuy() / 1000;

        if (taxe < 0) {
            this.kick();
            return;
        }

        GestorSalida.GAME_SEND_Eq_PACKET(this.player, taxe);
    }

    private void offlineExchange() {
        if (PiedraAlma.isInArenaMap(this.player.getCurMap().getId()) || this.player.getCurMap().noMarchand) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "113");
            return;
        }
        if (this.player.getCurMap().getId() == 33 || this.player.getCurMap().getId() == 38 || this.player.getCurMap().getId() == 4601 || this.player.getCurMap().getId() == 4259 || this.player.getCurMap().getId() == 8036 || this.player.getCurMap().getId() == 10301) {
            if (this.player.getCurMap().getStoreCount() >= 25) {
                GestorSalida.GAME_SEND_Im_PACKET(this.player, "125;25");
                return;
            }
        } else if (this.player.getCurMap().getStoreCount() >= 6) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "125;6");
            return;
        }
        long taxe = this.player.storeAllBuy() / 1000;
        if (this.player.getKamas() < taxe) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "176");
            return;
        }
        if (taxe < 0) {
            GestorSalida.GAME_SEND_MESSAGE(this.player, "Erreur de mode marchand, la somme est négatif.");
            return;
        }
        int orientation = Formulas.getRandomValue(1, 3);
        this.player.setKamas(this.player.getKamas() - taxe);
        this.player.set_orientation(orientation);
        Mapa map = this.player.getCurMap();
        this.player.setShowSeller(true);
        Mundo.mundo.addSeller(this.player);
        this.kick();
        map.getPlayers().stream().filter(player -> player != null && player.isOnline()).forEach(player -> GestorSalida.GAME_SEND_MERCHANT_LIST(player, player.getCurMap().getId()));
    }

    private synchronized void putInInventory(String packet) {
        if(this.player.getExchangeAction() != null && this.player.getExchangeAction().getType() == AccionIntercambiar.IN_MOUNTPARK) {
            int id = -1;
            Cercados park = this.player.getCurMap().getMountPark();

            try { id = Integer.parseInt(packet.substring(3)); } catch (Exception ignored) {}

            switch (packet.charAt(2)) {
                case 'C':// Certificats -> Etable
                    if(id == -1 || !this.player.hasItemGuid(id))
                        return;
                    if(park.hasEtableFull(this.player.getId())) {
                        this.player.send("Im1105");
                        return;
                    }

                    ObjetoJuego object = Mundo.getGameObject(id);
                    Montura mount = Mundo.mundo.getMountById(- object.getCaracteristicas().getEffect(995));

                    if(mount == null)
                        return;

                    mount.setOwner(this.player.getId());
                    this.player.removeItem(id);
                    Mundo.mundo.removeGameObject(id);

                    if(!park.getEtable().contains(mount))
                        park.getEtable().add(mount);

                    GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this.player, object.getId());

                    Database.dinamicos.getMountData().update(mount);
                    Database.dinamicos.getPlayerData().update(this.player);
                    GestorSalida.GAME_SEND_Ee_PACKET(this.player, mount.getSize() == 50 ? '~' : '+', mount.parse());
                    break;

                case 'c':// Etable -> Certificats
                    mount = Mundo.mundo.getMountById(id);

                    if(!park.getEtable().contains(mount) || mount == null)
                        return;

                    park.getEtable().remove(mount);
                    mount.setOwner(this.player.getId());

                    object = Objects.requireNonNull(Constantes.getParchoTemplateByMountColor(mount.getColor())).createNewItem(1, false);
                    object.setMountStats(this.player, mount, false);

                    Mundo.addGameObject(object, true);
                    this.player.addObjet(object);

                    GestorSalida.GAME_SEND_Ee_PACKET(this.player, '-', mount.getId() + "");
                    Database.dinamicos.getMountData().update(mount);
                    Database.dinamicos.getPlayerData().update(this.player);
                    break;

                case 'g':// Equiper une dinde
                    mount = Mundo.mundo.getMountById(id);

                    if(!park.getEtable().contains(mount) || mount == null) {
                        GestorSalida.GAME_SEND_Im_PACKET(this.player, "1104");
                        return;
                    }
                    if(this.player.getMount() != null) {
                        GestorSalida.GAME_SEND_BN(this);
                        return;
                    }
                    if(mount.getFecundatedDate() != -1) {
                        GestorSalida.GAME_SEND_BN(this);
                        return;
                    }

                    mount.setOwner(this.player.getId());
                    park.getEtable().remove(mount);
                    this.player.setMount(mount);

                    GestorSalida.GAME_SEND_Re_PACKET(this.player, "+", mount);
                    GestorSalida.GAME_SEND_Ee_PACKET(this.player, '-', mount.getId() + "");
                    GestorSalida.GAME_SEND_Rx_PACKET(this.player);
                    Database.dinamicos.getMountData().update(mount);
                    Database.dinamicos.getPlayerData().update(this.player);
                    break;

                case 'p':// Equipe -> Etable
                    if(this.player.getMount() != null && this.player.getMount().getId() == id) {
                        if(park.hasEtableFull(this.player.getId())) {
                            this.player.send("Im1105");
                            return;
                        }

                        mount = this.player.getMount();
                        if(mount.getObjects().size() == 0) {
                            if(this.player.isOnMount())
                                this.player.toogleOnMount();

                            if(!park.getEtable().contains(mount))
                                park.getEtable().add(mount);

                            mount.setOwner(this.player.getId());
                            this.player.setMount(null);

                            Database.dinamicos.getMountData().update(mount);
                            GestorSalida.GAME_SEND_Ee_PACKET(this.player, mount.getSize() == 50 ? '~' : '+', mount.parse());
                            GestorSalida.GAME_SEND_Re_PACKET(this.player, "-", null);
                            GestorSalida.GAME_SEND_Rx_PACKET(this.player);
                        } else {
                            GestorSalida.GAME_SEND_Im_PACKET(this.player, "1106");
                        }
                        Database.dinamicos.getMountData().update(mount);
                        Database.dinamicos.getPlayerData().update(this.player);
                    }
                    break;
            }
            Database.dinamicos.getMountParkData().update(park);
        }
    }

    private synchronized void putInMountPark(String packet) {
        if(this.player.getExchangeAction() != null && this.player.getExchangeAction().getType() == AccionIntercambiar.IN_MOUNTPARK) {
            int id = -1;
            Cercados park = this.player.getCurMap().getMountPark();
            try { id = Integer.parseInt(packet.substring(3));	} catch (Exception ignored) {}

            switch (packet.charAt(2)) {
// Enclos -> Etable
                case 'g' -> {
                    if (park.hasEtableFull(this.player.getId())) {
                        this.player.send("Im1105");
                        return;
                    }
                    Montura mount = Mundo.mundo.getMountById(id);
                    if (!park.getEtable().contains(mount))
                        park.getEtable().add(mount);
                    park.delRaising(mount.getId());
                    mount.setOwner(this.player.getId());
                    this.player.getCurMap().getMountPark().delRaising(id);
                    GestorSalida.GAME_SEND_Ef_MOUNT_TO_ETABLE(this.player, '-', mount.getId() + "");
                    GestorSalida.GAME_SEND_Ee_PACKET(this.player, mount.getSize() == 50 ? '~' : '+', mount.parse());
                    GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.player.getCurMap(), id);
                    mount.setMapId((short) -1);
                    mount.setCellId(-1);
                    Database.dinamicos.getMountData().update(mount);
                    Database.dinamicos.getPlayerData().update(this.player);
                }
// Etable -> Enclos
                case 'p' -> {
                    if (this.player.getMount() != null) {
                        if (this.player.getMount().getObjects().size() != 0) {
                            GestorSalida.GAME_SEND_Im_PACKET(this.player, "1106");
                            return;
                        }
                    }
                    if (park.hasEnclosFull(this.player.getId())) {
                        this.player.send("Im1107");
                        return;
                    }
                    if (this.player.getMount() != null && this.player.getMount().getId() == id) {
                        if (this.player.isOnMount())
                            this.player.toogleOnMount();
                        if (this.player.isOnMount())
                            return;
                        this.player.setMount(null);
                    }
                    Montura mount = Mundo.mundo.getMountById(id);
                    mount.setOwner(this.player.getId());
                    mount.setMapId(park.getMap().getId());
                    mount.setCellId(park.getPlaceOfSpawn());
                    park.getEtable().remove(mount);
                    park.addRaising(id);
                    GestorSalida.GAME_SEND_Ef_MOUNT_TO_ETABLE(this.player, '+', mount.parse());
                    GestorSalida.GAME_SEND_Ee_PACKET(this.player, '-', mount.getId() + "");
                    GestorSalida.GAME_SEND_GM_MOUNT_TO_MAP(park.getMap(), mount);
                    Database.dinamicos.getMountData().update(mount);
                    Database.dinamicos.getPlayerData().update(this.player);
                }
            }
            Database.dinamicos.getMountParkData().update(park);
        }
    }

    private void request(String packet) {
        if (this.player.getExchangeAction() != null && this.player.getExchangeAction().getType() != AccionIntercambiar.AUCTION_HOUSE_BUYING && this.player.getExchangeAction().getType() != AccionIntercambiar.AUCTION_HOUSE_SELLING) {
            GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this, 'O');
            return;
        }

        if (packet.substring(2, 4).equals("13") && this.player.getExchangeAction() == null) { // Craft s�curis� : celui qui n'a pas le job ( this.player ) souhaite invit� player
            try {
                String[] split = packet.split("\\|");
                int id = Integer.parseInt(split[1]);
                int skill = Integer.parseInt(split[2]);

                Jugador player = Mundo.mundo.getPlayer(id);

                if (player == null) {
                    GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this, 'E');
                    return;
                }
                if (player.getCurMap() != this.player.getCurMap() || !player.isOnline()) {
                    GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this, 'E');
                    return;
                }
                if (player.isAway() || this.player.isAway()) {
                    GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this, 'O');
                    return;
                }

                ArrayList<Oficio> jobs = player.getJobs();

                if (jobs == null)
                    return;

                ObjetoJuego object = player.getObjetByPos(Constantes.ITEM_POS_ARME);

                if (object == null) {
                    this.player.send("BN");
                    return;
                }
                boolean ok = false;

                for (Oficio job : jobs) {
                    if (job.getSkills().isEmpty())
                        continue;
                    if (!job.isValidTool(object.getModelo().getId()))
                        continue;

                    for (GameCase cell : this.player.getCurMap().getCases()) {
                        if (cell.getObject() != null) {
                            if (cell.getObject().getTemplate() != null) {
                                int io = cell.getObject().getTemplate().getId();
                                ArrayList<Integer> skills = job.getSkills().get(io);

                                if (skills != null) {
                                    for (int arg : skills) {
                                        if (arg == skill
                                                && Camino.getDistanceBetween(player.getCurMap(), player.getCurCell().getId(), cell.getId()) < 4) {
                                            ok = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (ok)
                        break;
                }

                if (!ok) {
                    this.player.send("ERET");
                    return;
                }

                AccionIntercambiar<Integer> exchangeAction = new AccionIntercambiar<>(AccionIntercambiar.CRAFTING_SECURE_WITH, id);
                this.player.setExchangeAction(exchangeAction);
                AccionIntercambiar<Integer> exchangeAction1 = new AccionIntercambiar<>(AccionIntercambiar.CRAFTING_SECURE_WITH, this.player.getId());
                player.setExchangeAction(exchangeAction1);

                this.player.getIsCraftingType().add(13);
                player.getIsCraftingType().add(12);
                this.player.getIsCraftingType().add(skill);
                player.getIsCraftingType().add(skill);

                GestorSalida.GAME_SEND_EXCHANGE_REQUEST_OK(this, this.player.getId(), id, 12);
                GestorSalida.GAME_SEND_EXCHANGE_REQUEST_OK(player.getGameClient(), this.player.getId(), id, 12);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return;
        } else if (packet.substring(2, 4).equals("12") && this.player.getExchangeAction() == null) { // Craft s�curis� : celui qui � le job ( this.player ) souhaite invit� player
            try {
                String[] split = packet.split("\\|");
                int id = Integer.parseInt(split[1]);
                int skill = Integer.parseInt(split[2]);

                Jugador player = Mundo.mundo.getPlayer(id);

                if (player == null) {
                    GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this, 'E');
                    return;
                }
                if (player.getCurMap() != this.player.getCurMap() || !player.isOnline()) {
                    GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this, 'E');
                    return;
                }
                if (player.isAway() || this.player.isAway()) {
                    GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this, 'O');
                    return;
                }

                ArrayList<Oficio> jobs = this.player.getJobs();
                if (jobs == null) return;

                ObjetoJuego object = this.player.getObjetByPos(Constantes.ITEM_POS_ARME);
                if (object == null) return;

                boolean ok = false;

                for (Oficio job : jobs) {
                    if (job.getSkills().isEmpty() || !job.isValidTool(object.getModelo().getId())) continue;
                    for (GameCase cell : this.player.getCurMap().getCases()) {
                        if (cell.getObject() != null) {
                            if (cell.getObject().getTemplate() != null) {
                                int io = cell.getObject().getTemplate().getId();
                                ArrayList<Integer> skills = job.getSkills().get(io);

                                if (skills != null) {
                                    for (int arg : skills) {
                                        if (arg == skill && Camino.getDistanceBetween(this.player.getCurMap(), this.player.getCurCell().getId(), cell.getId()) < 4) {
                                            ok = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (ok) break;
                }

                if (!ok) {
                    this.player.sendMessage("Tu es trop loin de l'atelier.");
                    return;
                }

                AccionIntercambiar<Integer> exchangeAction = new AccionIntercambiar<>(AccionIntercambiar.CRAFTING_SECURE_WITH, id);
                this.player.setExchangeAction(exchangeAction);
                exchangeAction = new AccionIntercambiar<>(AccionIntercambiar.CRAFTING_SECURE_WITH, this.player.getId());
                player.setExchangeAction(exchangeAction);

                this.player.getIsCraftingType().add(12);
                player.getIsCraftingType().add(13);
                this.player.getIsCraftingType().add(skill);
                player.getIsCraftingType().add(skill);

                GestorSalida.GAME_SEND_EXCHANGE_REQUEST_OK(this, this.player.getId(), id, 12);
                GestorSalida.GAME_SEND_EXCHANGE_REQUEST_OK(player.getGameClient(), this.player.getId(), id, 13);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return;
        } else if (packet.substring(2, 4).equals("11")) {//Ouverture HDV achat
            if(this.player.getExchangeAction() != null) leaveExchange(this.player);
            if (this.player.getDeshonor() >= 5) {
                GestorSalida.GAME_SEND_Im_PACKET(this.player, "183");
                return;
            }

            Mercadillo hdv = Mundo.mundo.getHdv(this.player.getCurMap().getId());
            if (hdv != null) {
                String info = "1,10,100;" + hdv.getStrCategory() + ";" + hdv.parseTaxe() + ";" + hdv.getLvlMax() + ";" + hdv.getMaxAccountItem() + ";-1;" + hdv.getSellTime();
                GestorSalida.GAME_SEND_ECK_PACKET(this.player, 11, info);
                AccionIntercambiar<Integer> exchangeAction = new AccionIntercambiar<>(AccionIntercambiar.AUCTION_HOUSE_BUYING, -this.player.getCurMap().getId()); //R�cup�re l'ID de la map et rend cette valeur n�gative
                this.player.setExchangeAction(exchangeAction);
            }
            return;
        } else if (packet.substring(2, 4).equals("15") && this.player.getExchangeAction() == null) {
            Montura mount = this.player.getMount();

            if(mount != null) {
                AccionIntercambiar<Integer> exchangeAction = new AccionIntercambiar<>(AccionIntercambiar.IN_MOUNT, mount.getId());
                this.player.setExchangeAction(exchangeAction);

                GestorSalida.GAME_SEND_ECK_PACKET(this, 15, String.valueOf(mount.getId()));
                GestorSalida.GAME_SEND_EL_MOUNT_PACKET(this.player, mount);
                GestorSalida.GAME_SEND_Ew_PACKET(this.player, mount.getActualPods(), mount.getMaxPods());
            }
            return;
        } else if (packet.substring(2, 4).equals("17") && this.player.getExchangeAction() == null) {//Ressurection famillier
            int id = Integer.parseInt(packet.substring(5));

            if (this.player.getCurMap().getNpc(id) != null) {
                IntercambioJugador.NpcRessurectPets ech = new IntercambioJugador.NpcRessurectPets(this.player, this.player.getCurMap().getNpc(id).getTemplate());
                AccionIntercambiar<IntercambioJugador.NpcRessurectPets> exchangeAction = new AccionIntercambiar<>(AccionIntercambiar.TRADING_WITH_NPC_PETS_RESURRECTION, ech);
                this.player.setExchangeAction(exchangeAction);
                GestorSalida.GAME_SEND_ECK_PACKET(this.player, 9, String.valueOf(id));
            }
        } else if (packet.substring(2, 4).equals("10")) {//Ouverture HDV vente
            if(this.player.getExchangeAction() != null) leaveExchange(this.player);
            if (this.player.getDeshonor() >= 5) {
                GestorSalida.GAME_SEND_Im_PACKET(this.player, "183");
                return;
            }

            Mercadillo hdv = Mundo.mundo.getHdv(this.player.getCurMap().getId());
            if (hdv != null) {
                String infos = "1,10,100;" + hdv.getStrCategory() + ";" + hdv.parseTaxe() + ";" + hdv.getLvlMax() + ";" + hdv.getMaxAccountItem() + ";-1;" + hdv.getSellTime();
                GestorSalida.GAME_SEND_ECK_PACKET(this.player, 10, infos);
                AccionIntercambiar<Integer> exchangeAction = new AccionIntercambiar<>(AccionIntercambiar.AUCTION_HOUSE_SELLING, - Mundo.mundo.changeHdv(this.player.getCurMap().getId()));
                this.player.setExchangeAction(exchangeAction);
                GestorSalida.GAME_SEND_HDVITEM_SELLING(this.player);
            }
            return;
        }
        if (this.player.getExchangeAction() != null) {
            GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this, 'O');
            return;
        }
        switch (packet.charAt(2)) {
            case '0'://Si NPC
                int id = Integer.parseInt(packet.substring(4));
                Npc npc = this.player.getCurMap().getNpc(id);

                if (npc != null) {
                    AccionIntercambiar<Integer> exchangeAction = new AccionIntercambiar<>(AccionIntercambiar.TRADING_WITH_NPC, id);
                    this.player.setExchangeAction(exchangeAction);

                    GestorSalida.GAME_SEND_ECK_PACKET(this, 0, String.valueOf(id));
                    GestorSalida.GAME_SEND_ITEM_VENDOR_LIST_PACKET(this, npc);
                }
                break;
            case '1'://Si joueur
                try {
                    id = Integer.parseInt(packet.substring(4));
                    Jugador target = Mundo.mundo.getPlayer(id);

                    if (target == null || target.getCurMap() != this.player.getCurMap() || !target.isOnline()) {
                        GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this, 'E');
                        return;
                    }
                    if (target.isAway() || this.player.isAway() || target.getExchangeAction() != null || this.player.getExchangeAction() != null) {
                        GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this, 'O');
                        return;
                    }
                    if (target.getGroupe() != null && this.player.getGroupe() == null) {
                        if (!target.getGroupe().isJugador()) {
                            GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this, 'E');
                            return;
                        }
                    }
                    AccionIntercambiar<Integer> exchangeAction = new AccionIntercambiar<>(AccionIntercambiar.TRADING_WITH_PLAYER, id);
                    this.player.setExchangeAction(exchangeAction);
                    exchangeAction = new AccionIntercambiar<>(AccionIntercambiar.TRADING_WITH_PLAYER, this.player.getId());
                    target.setExchangeAction(exchangeAction);

                    this.player.getIsCraftingType().add(1);
                    target.getIsCraftingType().add(1);

                    GestorSalida.GAME_SEND_EXCHANGE_REQUEST_OK(this, this.player.getId(), id, 1);
                    GestorSalida.GAME_SEND_EXCHANGE_REQUEST_OK(target.getGameClient(), this.player.getId(), id, 1);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                break;
            case '2'://Npc Exchange
                id = Integer.parseInt(packet.substring(4));
                if (this.player.getCurMap().getNpc(id) != null) {
                    IntercambioJugador.NpcExchange ech = new IntercambioJugador.NpcExchange(this.player, this.player.getCurMap().getNpc(id).getTemplate());

                    AccionIntercambiar<IntercambioJugador.NpcExchange> exchangeAction = new AccionIntercambiar<>(AccionIntercambiar.TRADING_WITH_NPC_EXCHANGE, ech);
                    this.player.setExchangeAction(exchangeAction);
                    GestorSalida.GAME_SEND_ECK_PACKET(this.player, 2, String.valueOf(id));
                }
                break;
            case '4'://StorePlayer
                id = Integer.parseInt(packet.split("\\|")[1]);

                Jugador seller = Mundo.mundo.getPlayer(id);
                if (seller == null || !seller.isShowSeller() || seller.getCurMap() != this.player.getCurMap()) return;

                AccionIntercambiar<Integer> exchangeAction = new AccionIntercambiar<>(AccionIntercambiar.TRADING_WITH_OFFLINE_PLAYER, id);
                this.player.setExchangeAction(exchangeAction);

                GestorSalida.GAME_SEND_ECK_PACKET(this.player, 4, String.valueOf(seller.getId()));
                GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(seller, this.player);
                break;
            case '6'://StoreItems
                exchangeAction = new AccionIntercambiar<>(AccionIntercambiar.TRADING_WITH_ME, this.player.getId());
                this.player.setExchangeAction(exchangeAction);

                GestorSalida.GAME_SEND_ECK_PACKET(this.player, 6, "");
                GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(this.player, this.player);
                break;
            case '8'://Si Collector
                Recaudador collector = Mundo.mundo.getCollector(Integer.parseInt(packet.substring(4)));
                if (collector == null || collector.getInFight() > 0 || collector.getExchange() || collector.getGuildId() != this.player.getGuild().getId() || collector.getMap() != this.player.getCurMap().getId())
                    return;
                if (!this.player.getGuildMember().canDo(Constantes.G_COLLPERCO)) {
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "1101");
                    return;
                }
                collector.setExchange(true);
                exchangeAction = new AccionIntercambiar<>(AccionIntercambiar.TRADING_WITH_COLLECTOR, collector.getId());
                this.player.setExchangeAction(exchangeAction);
                this.player.DialogTimer();

                GestorSalida.GAME_SEND_ECK_PACKET(this, 8, String.valueOf(collector.getId()));
                GestorSalida.GAME_SEND_ITEM_LIST_PACKET_PERCEPTEUR(this, collector);
                break;
            case '9'://D�pos�/Retir� un familier
                id = Integer.parseInt(packet.substring(4));

                if (this.player.getCurMap().getNpc(id) != null) {
                    IntercambioJugador.NpcExchangePets ech = new IntercambioJugador.NpcExchangePets(this.player, this.player.getCurMap().getNpc(id).getTemplate());
                    this.player.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.TRADING_WITH_NPC_PETS, ech));
                    GestorSalida.GAME_SEND_ECK_PACKET(this.player, 9, String.valueOf(id));
                }
                break;
        }
    }

    private void sell(String packet) {
        try {
            String[] infos = packet.substring(2).split("\\|");
            int id = Integer.parseInt(infos[0]), quantity = Integer.parseInt(infos[1]);

            if (!this.player.hasItemGuid(id)) {
                GestorSalida.GAME_SEND_SELL_ERROR_PACKET(this);
                return;
            }

            this.player.sellItem(id, quantity);
        } catch (Exception e) {
            e.printStackTrace();
            GestorSalida.GAME_SEND_SELL_ERROR_PACKET(this);
        }
    }

    private void bookOfArtisant(String packet) {
        if (packet.charAt(2) == 'F') {
            int Metier = Integer.parseInt(packet.substring(3));
            int cant = 0;
            for (Jugador artissant : Mundo.mundo.getOnlinePlayers()) {
                if (artissant.getMetiers().isEmpty())
                    continue;
                String send = "";
                int id = artissant.getId();
                String name = artissant.getName();
                String color = artissant.getColor1() + ","
                        + artissant.getColor2() + ","
                        + artissant.getColor3();
                String accesoire = artissant.getGMStuffString();
                int sex = artissant.getSexe();
                int map = artissant.getCurMap().getId();
                int inJob = (map == 8731 || map == 8732) ? 1 : 0;
                int classe = artissant.getClasse();
                for (OficioCaracteristicas SM : artissant.getMetiers().values()) {
                    if (SM.getTemplate().getId() != Metier)
                        continue;
                    cant++;
                    send = "+" + SM.getTemplate().getId() + ";" + id + ";"
                            + name + ";" + SM.get_lvl() + ";" + map + ";"
                            + inJob + ";" + classe + ";" + sex + ";"
                            + color + ";" + accesoire + ";"
                            + SM.getOptBinValue() + ","
                            + SM.getSlotsPublic();
                    GestorSalida.SEND_EJ_LIVRE(this.player, send);
                }
            }
            if (cant == 0)
                GestorSalida.GAME_SEND_MESSAGE(this.player, "Dans ces moments il n'y a pas d'artisan disponible du métier que tu cherches.");
        }
    }

    private void setPublicMode(String packet) {
        switch (packet.charAt(2)) {
            case '+' -> {
                this.player.setMetierPublic(true);
                StringBuilder metier = new StringBuilder();
                boolean first = false;
                for (OficioCaracteristicas SM : this.player.getMetiers().values()) {
                    GestorSalida.SEND_Ej_LIVRE(this.player, "+"
                            + SM.getTemplate().getId());
                    if (first)
                        metier.append(";");
                    metier.append(OficioConstantes.actionMetier(SM.getTemplate().getId()));
                    first = true;
                }
                GestorSalida.SEND_EW_METIER_PUBLIC(this.player, "+");
                GestorSalida.SEND_EW_METIER_PUBLIC(this.player, "+" + this.player.getId()
                        + "|" + metier);
            }
            case '-' -> {
                this.player.setMetierPublic(false);
                for (OficioCaracteristicas metiers : this.player.getMetiers().values()) {
                    GestorSalida.SEND_Ej_LIVRE(this.player, "-"
                            + metiers.getTemplate().getId());
                }
                GestorSalida.SEND_EW_METIER_PUBLIC(this.player, "-");
                GestorSalida.SEND_EW_METIER_PUBLIC(this.player, "-" + this.player.getId());
            }
        }
    }

    public static void leaveExchange(Jugador player) {
        AccionIntercambiar<?> exchangeAction = player.getExchangeAction();

        if (exchangeAction == null)
            return;

        switch(exchangeAction.getType()) {
            case AccionIntercambiar.TRADING_WITH_PLAYER:
                if(exchangeAction.getValue() instanceof Integer) {
                    Jugador target = Mundo.mundo.getPlayer((Integer) exchangeAction.getValue());
                    if(target != null && target.getExchangeAction() != null && target.getExchangeAction().getType() == AccionIntercambiar.TRADING_WITH_PLAYER) {
                        target.send("EV");
                        target.setExchangeAction(null);
                    }
                } else {
                    ((IntercambioJugador) exchangeAction.getValue()).cancel();
                }
                break;
            case AccionIntercambiar.TRADING_WITH_NPC_PETS:
                ((IntercambioJugador.NpcExchangePets) exchangeAction.getValue()).cancel();
                break;
            case AccionIntercambiar.TRADING_WITH_NPC_EXCHANGE:
                ((IntercambioJugador.NpcExchange) exchangeAction.getValue()).cancel();
                break;
            case AccionIntercambiar.CRAFTING_SECURE_WITH:
                if(exchangeAction.getValue() instanceof Integer) {
                    Jugador target = Mundo.mundo.getPlayer((Integer) exchangeAction.getValue());
                    if(target != null && target.getExchangeAction() != null && target.getExchangeAction().getType() == AccionIntercambiar.CRAFTING_SECURE_WITH) {
                        target.send("EV");
                        target.setExchangeAction(null);
                    }
                } else {
                    ((CraftSeguro) exchangeAction.getValue()).cancel();
                }
                break;
            case AccionIntercambiar.CRAFTING:
                player.send("EV");
                player.setDoAction(false);
                ((OficioAccion) exchangeAction.getValue()).resetCraft();
                break;

            case AccionIntercambiar.BREAKING_OBJECTS:
                ((RomperObjetos) exchangeAction.getValue()).setDetenerse(true);
                player.send("EV");
                break;
            case AccionIntercambiar.TRADING_WITH_NPC:
            case AccionIntercambiar.IN_MOUNT:
                player.send("EV");
                break;
            case AccionIntercambiar.IN_MOUNTPARK:
                player.send("EV");
                ArrayList<ObjetoJuego> objects = new ArrayList<>();
                for(ObjetoJuego object : player.getItems().values()) {
                    Montura mount = Mundo.mundo.getMountById(- object.getCaracteristicas().getEffect(995));

                    if(mount == null && object.getModelo().getType() == Constantes.ITEM_TYPE_CERTIF_MONTURE)
                        objects.add(object);
                }
                for(ObjetoJuego object : objects)
                    player.removeItem(object.getId(), object.getCantidad(), true, true);
                break;

            case AccionIntercambiar.IN_TRUNK:
                ((Cofres) exchangeAction.getValue()).setPlayer(null);
                player.send("EV");
                break;

            case AccionIntercambiar.TRADING_WITH_COLLECTOR:
                Recaudador collector = Mundo.mundo.getCollector((Integer) exchangeAction.getValue());
                if (collector == null) return;
                for (Jugador loc : Mundo.mundo.getGuild(collector.getGuildId()).getPlayers()) {
                    if (loc != null && loc.isOnline()) {
                        GestorSalida.GAME_SEND_gITM_PACKET(loc, Recaudador.parseToGuild(loc.getGuild().getId()));
                        String str = "";
                        str += "G" + collector.getFullName();
                        str += "|.|" + Mundo.mundo.getMap(collector.getMap()).getX() + "|" + Mundo.mundo.getMap(collector.getMap()).getY() + "|";
                        str += player.getName() + "|" + (collector.getXp());
                        if (!collector.getLogObjects().equals("")) str += collector.getLogObjects();
                        GestorSalida.GAME_SEND_gT_PACKET(loc, str);
                    }
                }
                player.getGuildMember().giveXpToGuild(collector.getXp());
                player.getCurMap().RemoveNpc(collector.getId());
                GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(), collector.getId());
                collector.reloadTimer();
                collector.delCollector(collector.getId());
                player.send("EV");
                Database.estaticos.getCollectorData().delete(collector.getId());
                break;

            default:
                player.setLivreArtisant(false);
                player.send("EV");
                break;
        }


        player.setExchangeAction(null);
        Database.dinamicos.getPlayerData().update(player);
    }

    /**
     * Emote Packet *
     */
    private void parseEnvironementPacket(String packet) {
        switch (packet.charAt(1)) {
//Change direction
            case 'D' -> setDirection(packet);
//Emote
            case 'U' -> useEmote(packet);
        }
    }

    private void setDirection(String packet) {
        try {
            if (this.player.getPelea() != null || this.player.isDead() == 1)
                return;
            int dir = Integer.parseInt(packet.substring(2));
            if (dir > 7 || dir < 0)
                return;
            this.player.set_orientation(dir);
            GestorSalida.GAME_SEND_eD_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), dir);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void useEmote(String packet) {
        final int emote = Integer.parseInt(packet.substring(2));
        if (emote == -1)
            return;
        if (this.player == null)
            return;
        if (this.player.getPelea() != null)
            return;//Pas d'�mote en combat
        if (!this.player.getEmotes().contains(emote))
            return;
        if (emote != 1 || emote != 19 && this.player.isSitted())
            this.player.setSitted(false);

        switch (emote) {
// s'asseoir
            case 20, 19, 1 -> this.player.setSitted(!this.player.isSitted());
        }

        if (this.player.emoteActive() == 1 || this.player.emoteActive() == 19 || this.player.emoteActive() == 21)
            this.player.setEmoteActive(0);
        else
            this.player.setEmoteActive(emote);

        Cercados MP = this.player.getCurMap().getMountPark();
        GestorSalida.GAME_SEND_eUK_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), this.player.emoteActive());
        if((emote == 2 || emote == 4 || emote == 3 || emote == 6 || emote == 8 || emote == 10) && MP != null)
        {
            final ArrayList<Montura> mounts = new ArrayList<>();
            for(Integer id : MP.getListOfRaising())  {
                Montura mount = Mundo.mundo.getMountById(id);
                if(mount != null)
                    if(mount.getOwner() == this.player.getId())
                        mounts.add(mount);
            }
            final Jugador player = this.player;
            if(mounts.isEmpty()) return;
            final Montura mount = mounts.get(Formulas.getRandomValue(0, mounts.size() - 1));
            if(mounts.size() > 0) {
                int cells = switch (emote) {
                    case 2, 4 -> 1;
                    case 3, 8 -> Formulas.getRandomValue(2, 3);
                    case 6, 10 -> Formulas.getRandomValue(4, 7);
                    default -> 0;
                };

                mount.moveMounts(player, cells, !(emote == 2 || emote == 3 || emote == 10));
            }
        }
    }

    /**
     * Friend Packet *
     */
    private void parseFrienDDacket(String packet) {
        switch (packet.charAt(1)) {
            case 'A'://Ajthiser
                addFriend(packet);
                break;
            case 'D'://Effacer un ami
                removeFriend(packet);
                break;
            case 'L'://Liste
                GestorSalida.GAME_SEND_FRIENDLIST_PACKET(this.player);
                break;
            case 'O':
                switch (packet.charAt(2)) {
                    case '-' -> {
                        this.player.SetSeeFriendOnline(false);
                        GestorSalida.GAME_SEND_BN(this.player);
                    }
                    case '+' -> {
                        this.player.SetSeeFriendOnline(true);
                        GestorSalida.GAME_SEND_BN(this.player);
                    }
                }
                break;
            case 'J': //Wife
                joinWife(packet);
                break;
        }
    }

    private void addFriend(String packet) {
        if (this.player == null)
            return;
        int guid = -1;
        switch (packet.charAt(2)) {
//Nom de this.player
            case '%' -> {
                packet = packet.substring(3);
                Jugador P = Mundo.mundo.getPlayerByName(packet);
                if (P == null || !P.isOnline())//Si P est nul, ou si P est nonNul et P offline
                {
                    GestorSalida.GAME_SEND_FA_PACKET(this.player, "Ef");
                    return;
                }
                guid = P.getAccID();
            }
//Pseudo
            case '*' -> {
                packet = packet.substring(3);
                Cuenta C = Mundo.mundo.getAccountByPseudo(packet);
                if (C == null || !C.isOnline()) {
                    GestorSalida.GAME_SEND_FA_PACKET(this.player, "Ef");
                    return;
                }
                guid = C.getId();
            }
            default -> {
                packet = packet.substring(2);
                Jugador Pr = Mundo.mundo.getPlayerByName(packet);
                if (Pr == null || !Pr.isOnline())//Si P est nul, ou si P est nonNul et P offline
                {
                    GestorSalida.GAME_SEND_FA_PACKET(this.player, "Ef");
                    return;
                }
                guid = Pr.getAccount().getId();
            }
        }
        if (guid == -1) {
            GestorSalida.GAME_SEND_FA_PACKET(this.player, "Ef");
            return;
        }
        account.addFriend(guid);
    }

    private void removeFriend(String packet) {
        if (this.player == null)
            return;
        int guid = -1;
        switch (packet.charAt(2)) {
//Nom de this.player
            case '%' -> {
                packet = packet.substring(3);
                Jugador P = Mundo.mundo.getPlayerByName(packet);
                if (P == null)//Si P est nul, ou si P est nonNul et P offline
                {
                    GestorSalida.GAME_SEND_FD_PACKET(this.player, "Ef");
                    return;
                }
                guid = P.getAccID();
            }
//Pseudo
            case '*' -> {
                packet = packet.substring(3);
                Cuenta C = Mundo.mundo.getAccountByPseudo(packet);
                if (C == null) {
                    GestorSalida.GAME_SEND_FD_PACKET(this.player, "Ef");
                    return;
                }
                guid = C.getId();
            }
            default -> {
                packet = packet.substring(2);
                Jugador Pr = Mundo.mundo.getPlayerByName(packet);
                if (Pr == null || !Pr.isOnline())//Si P est nul, ou si P est nonNul et P offline
                {
                    GestorSalida.GAME_SEND_FD_PACKET(this.player, "Ef");
                    return;
                }
                guid = Pr.getAccount().getId();
            }
        }
        if (guid == -1 || !account.isFriendWith(guid)) {
            GestorSalida.GAME_SEND_FD_PACKET(this.player, "Ef");
            return;
        }
        account.removeFriend(guid);
    }

    private void joinWife(String packet) {
        Jugador Wife = Mundo.mundo.getPlayer(this.player.getWife());
        if (Wife == null)
            return;
        if (!Wife.isOnline()) {
            if (Wife.getSexe() == 0)
                GestorSalida.GAME_SEND_Im_PACKET(this.player, "140");
            else
                GestorSalida.GAME_SEND_Im_PACKET(this.player, "139");

            GestorSalida.GAME_SEND_FRIENDLIST_PACKET(this.player);
            return;
        }
        switch (packet.charAt(2)) {
            case 'S'://Teleportation
                // TP Mariage : mettre une condition de donjon ...
                if (Wife.getCurMap().noTP || Wife.getCurMap().haveMobFix()) {
                    GestorSalida.GAME_SEND_MESSAGE(this.player, "Une barrière magique vous empêche de rejoindre votre conjoint.");
                    return;
                }
                if (this.player.getPelea() != null)
                    return;
                else
                    this.player.meetWife(Wife);
                break;
            case 'C'://Suivre le deplacement
                if (packet.charAt(3) == '+')//Si lancement de la traque
                {
                    if (this.player.follow != null)
                        this.player.follow.follower.remove(this.player.getId());
                    GestorSalida.GAME_SEND_FLAG_PACKET(this.player, Wife);
                    this.player.follow = Wife;
                    Wife.follower.put(this.player.getId(), this.player);
                } else
                //On arrete de suivre
                {
                    GestorSalida.GAME_SEND_DELETE_FLAG_PACKET(this.player);
                    this.player.follow = null;
                    Wife.follower.remove(this.player.getId());
                }
                break;
        }
    }

    /**
     * Fight Packet *
     */
    private void parseFightPacket(String packet) {
        try {
            switch (packet.charAt(1)) {
                case 'D'://D�tails d'un combat (liste des combats)
                    int key = -1;
                    try {
                        key = Integer.parseInt(packet.substring(2).replace(0x0
                                + "", ""));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (key == -1)
                        return;
                    GestorSalida.GAME_SEND_FIGHT_DETAILS(this, this.player.getCurMap().getFight(key));
                    break;

                case 'H'://Aide
                    if (this.player.getPelea() == null)
                        return;
                    this.player.getPelea().toggleHelp(this.player.getId());
                    break;
                case 'L'://Lister les combats
                    GestorSalida.GAME_SEND_FIGHT_LIST_PACKET(this, this.player.getCurMap());
                    break;
                case 'N'://Bloquer le combat
                    if (this.player.getPelea() == null)
                        return;
                    this.player.getPelea().toggleLockTeam(this.player.getId());
                    break;
                case 'P'://Seulement le groupe
                    if (this.player.getPelea() == null || this.player.getParty() == null)
                        return;
                    this.player.getPelea().toggleOnlyGroup(this.player.getId());
                    break;
                case 'S'://Bloquer les specs
                    if (this.player.getPelea() != null)
                        this.player.getPelea().toggleLockSpec(this.player);
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Game Packet *
     */
    private void parseGamePacket(String packet) {
        switch (packet.charAt(1)) {
            case 'A':
                if (this.player != null)
                    sendActions(packet);
                break;
            case 'C':
                if (this.player != null)
                    this.player.sendGameCreate();
                break;
            case 'd':
                showMonsterTarget(packet);
                break;
            case 'f':
                setFlag(packet);
                break;
            case 'F':
                this.player.setGhost();
                break;
            case 'I':
                getExtraInformations();
                break;
            case 'K':
                actionAck(packet);
                break;
            case 'P'://PvP Toogle
                this.player.toggleWings(packet.charAt(2));
                break;
            case 'p':
                setPlayerPosition(packet);
                break;
            case 'Q':
                leaveFight(packet);
                break;
            case 'R':
                readyFight(packet);
                break;
            case 't':
                if (this.player.getPelea() != null)
                    this.player.getPelea().playerPass(this.player);
                break;
        }
    }

    private synchronized void sendActions(String packet) {
        if (this.player.getDoAction()) {
            GestorSalida.GAME_SEND_GA_PACKET(this, "", "0", "", "");
            return;
        }
        int actionID;
        try {
            actionID = Integer.parseInt(packet.substring(2, 5));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
        int nextGameActionID = 0;

        if (actions.size() > 0) {
            //On prend le plus haut GameActionID + 1
            nextGameActionID = (Integer) (actions.keySet().toArray()[actions.size() - 1]) + 1;
        }
        AccionJuego GA = new AccionJuego(nextGameActionID, actionID, packet);

        switch (actionID) {
            case 1://Deplacement
                GameCase oldCase = this.player.getCurCell();

                this.gameParseDeplacementPacket(GA);

                final Grupo party = this.player.getParty();

                if(party != null && this.player.getPelea() == null && party.getMaster() != null && party.getMaster().getName().equals(this.player.getName())) {
                    Temporizador.addSiguiente(() -> party.getPlayers().stream()
                            .filter((follower1) -> party.isWithTheMaster(follower1, false))
                            .forEach(follower -> {
                                if(follower.getCurCell().getId() != oldCase.getId())
                                    follower.teleport(follower.getCurMap().getId(), oldCase.getId());
                                follower.getGameClient().sendActions(packet);
                            }), 0, TimeUnit.MILLISECONDS, Temporizador.DataType.CLIENTE);
                }
                break;

            case 34://Get quest on sign.
                gameCheckSign(packet);

            case 300://Sort
                gameTryCastSpell(packet);
                break;

            case 303://Attaque CaC
                gameTryCac(packet);
                break;

            case 500://Action Sur Map
                gameAction(GA);
                this.player.setGameAction(GA);
                break;

            case 507://Panneau int�rieur de la maison
                houseAction(packet);
                break;

            case 512:
                if (this.player.get_align() == Constantes.ALINEAMIENTO_NEUTRAL)
                    return;
                this.player.openPrismeMenu();
                break;

            case 618://Mariage oui
                this.player.setisOK(Integer.parseInt(packet.substring(5, 6)));
                GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(this.player.getCurMap(), "", this.player.getId(), this.player.getName(), "Oui");
                Jugador boy = (Jugador) this.player.getCurMap().getCase(282).getPlayers().toArray()[0], girl = (Jugador) this.player.getCurMap().getCase(297).getPlayers().toArray()[0];

                if (girl.getisOK() > 0 && boy.getisOK() > 0)
                    Mundo.mundo.wedding(girl, boy, 1);
                else
                    Mundo.mundo.priestRequest(boy, girl, this.player == boy ? girl : boy);
                break;
            case 619://Mariage non
                this.player.setisOK(0);
                GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(this.player.getCurMap(), "", this.player.getId(), this.player.getName(), "Non");
                boy = (Jugador) this.player.getCurMap().getCase(282).getPlayers().toArray()[0];
                girl = (Jugador) this.player.getCurMap().getCase(297).getPlayers().toArray()[0];

                Mundo.mundo.wedding(girl, boy, 0);
                break;

            case 900://Demande Defie
                if (MainServidor.INSTANCE.getFightAsBlocked())
                    return;
                gameAskDuel(packet);
                break;

            case 901://Accepter Defie
                if (MainServidor.INSTANCE.getFightAsBlocked())
                    return;
                gameAcceptDuel(packet);
                break;

            case 902://Refus/Anuler Defie
                if (MainServidor.INSTANCE.getFightAsBlocked())
                    return;
                gameCancelDuel(packet);
                break;

            case 903://Rejoindre combat
                gameJoinFight(packet);
                break;

            case 906://Agresser
                if (MainServidor.INSTANCE.getFightAsBlocked())
                    return;
                gameAggro(packet);
                break;

            case 909://Collector
                if (MainServidor.INSTANCE.getFightAsBlocked())
                    return;
                long calcul = Instant.now().toEpochMilli() - Configuracion.INSTANCE.getStartTime();
                if(calcul < 600000) {
                    this.player.sendMessage("Vous devez attendre encore " + ((600000 - calcul) / 60000) + " minute(s).");
                    return;
                }
                gameCollector(packet);
                break;

            case 912:// ataque Prisme
                if (MainServidor.INSTANCE.getFightAsBlocked())
                    return;
                calcul = Instant.now().toEpochMilli() - Configuracion.INSTANCE.getStartTime();
                if(calcul < 600000) {
                    this.player.sendMessage("Vous devez attendre encore " + ((600000 - calcul) / 60000) + " minute(s).");
                    return;
                }
                gamePrism(packet);
                break;
        }
    }

    private void gameParseDeplacementPacket(AccionJuego GA) {
        String path = GA.getPacket().substring(5);

        if (this.player.getPelea() == null) {
            if (this.player.getBlockMovement()) {
                GestorSalida.GAME_SEND_GA_PACKET(this, "", "0", "", "");
                removeAction(GA);
                return;
            }
            if (this.player.isDead() == 1) {
                GestorSalida.GAME_SEND_BN(this.player);
                removeAction(GA);
                return;
            }
            if (this.player.getDoAction()) {
                GestorSalida.GAME_SEND_GA_PACKET(this, "", "0", "", "");
                removeAction(GA);
                return;
            }
            if (this.player.getMount() != null && !this.player.isGhost()) {
                if (!this.player.getMorphMode() && (this.player.getPodUsed() > this.player.getMaximosPods() || this.player.getMount().getActualPods() > this.player.getMount().getMaxPods())) {
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "112");
                    GestorSalida.GAME_SEND_GA_PACKET(this, "", "0", "", "");
                    removeAction(GA);
                    return;
                }
            }
            if (this.player.getPodUsed() > this.player.getMaximosPods() && !this.player.isGhost()
                    && !this.player.getMorphMode()) {
                GestorSalida.GAME_SEND_Im_PACKET(this.player, "112");
                GestorSalida.GAME_SEND_GA_PACKET(this, "", "0", "", "");
                removeAction(GA);
                return;
            }
            //Si d�placement inutile
            GameCase targetCell = this.player.getCurMap().getCase(Mundo.mundo.getCryptManager().codigoceldaID(path.substring(path.length() - 2)));

            if(this.player.getCurMap().getId() == 6824 && this.player.start != null && targetCell.getId() == 325 && !this.player.start.leave) {
                this.player.start.leave = true;
                GestorSalida.GAME_SEND_GA_PACKET(this, "", "0", "", "");
                removeAction(GA);
                return;
            }
            if (!targetCell.isWalkable(false)) {
                GestorSalida.GAME_SEND_GA_PACKET(this, "", "0", "", "");
                removeAction(GA);
                return;
            }

            AtomicReference<String> pathRef = new AtomicReference<>(path);
            int result = Camino.isValidPath(this.player.getCurMap(), this.player.getCurCell().getId(), pathRef, null, this.player, targetCell.getId());

            if (result <= -9999) {
                result += 10000;
                GA.tp = true;
            }
            if (result == 0) {
                if (targetCell.getObject() != null) {
                    if (Configuracion.INSTANCE.getMostrarenviados()) {
                    logger.info("Objeto interactivo {} en la celda {} se ha detenido", targetCell.getObject().getId(), targetCell.getId());
                    }
                    ObjetosInteractivos.getActionIO(this.player, targetCell, targetCell.getObject().getId());
                    ObjetosInteractivos.getSignIO(this.player, targetCell.getId(), targetCell.getObject().getId());
                    GestorSalida.GAME_SEND_GA_PACKET(this, "", "0", "", "");
                    removeAction(GA);
                    return;
                }
                GestorSalida.GAME_SEND_GA_PACKET(this, "", "0", "", "");
                removeAction(GA);
                return;
            }
            if (result != -1000 && result < 0)
                result = -result;

            //On prend en compte le nouveau path
            path = pathRef.get();
            //Si le path est invalide
            if (result == -1000)
                path = Mundo.mundo.getCryptManager().getHashedValueByInt(this.player.get_orientation()) + Mundo.mundo.getCryptManager().idceldaCodigo(this.player.getCurCell().getId());

            //On sauvegarde le path dans la variable
            GA.args = path;
            if (this.player.walkFast) {
                this.player.getCurCell().removePlayer(this.player);
                GestorSalida.GAME_SEND_BN(this);
                //On prend la case cibl�e
                GameCase nextCell = this.player.getCurMap().getCase(Mundo.mundo.getCryptManager().codigoceldaID(path.substring(path.length() - 2)));
                targetCell = this.player.getCurMap().getCase(Mundo.mundo.getCryptManager().codigoceldaID(GA.getPacket().substring(GA.getPacket().length() - 2)));

                //On d�finie la case et on ajthise le this.playernnage sur la case
                this.player.setCurCell(nextCell);
                this.player.set_orientation(Mundo.mundo.getCryptManager().getIntByHashedValue(path.charAt(path.length() - 3)));
                this.player.getCurCell().addPlayer(this.player);
                if (!this.player.isGhost())
                    this.player.setAway(false);
                this.player.getCurMap().onPlayerArriveOnCell(this.player, this.player.getCurCell().getId());
                if (targetCell.getObject() != null) {
                    if (Configuracion.INSTANCE.getMostrarenviados()) {
                    logger.info("Objeto interactivo {} en la celda {} ejecuta la accion", targetCell.getObject().getId(), targetCell.getId());
                    }
                    ObjetosInteractivos.getActionIO(this.player, targetCell, targetCell.getObject().getId());
                    ObjetosInteractivos.getSignIO(this.player, targetCell.getId(), targetCell.getObject().getId());
                }
                GestorSalida.GAME_SEND_GA_PACKET(this, "", "0", "", "");
                removeAction(GA);
                GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.player.getCurMap(), this.player.getId());
                GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(this.player.getCurMap(), this.player);
                return;
            } else {
                GestorSalida.GAME_SEND_GA_PACKET_TO_MAP(this.player.getCurMap(), "" + GA.getId(), 1, this.player.getId() + "", "a" + Mundo.mundo.getCryptManager().idceldaCodigo(this.player.getCurCell().getId()) + path);
            }

            this.addAction(GA);
            this.player.setSitted(false);
            this.player.setAway(true);
        } else {
            final Peleador fighter = this.player.getPelea().getFighterByPerso(this.player);
            if (fighter != null) {
                GA.args = path;
                this.player.getPelea().cast(this.player.getPelea().getFighterByPerso(this.player), () -> this.player.getPelea().onFighterDeplace(fighter, GA));
            }
        }
    }

    private void gameCheckSign(String packet) {
        Mision quest = Mision.getQuestById(Integer.parseInt(packet.substring(5)));
        MisionJugador questPlayer = this.player.getQuestPersoByQuest(quest);
        if (questPlayer == null)
            quest.applyQuest(this.player); // S'il n'a pas la qu�te
        else
            GestorSalida.GAME_SEND_MESSAGE(this.player, "Vous avez déjà appris la quête.");
    }

    private void gameTryCastSpell(String packet) {
        try {
            String[] split = packet.split(";");

            if(packet.contains("undefined") || split == null || split.length != 2)
                return;

            final int id = Integer.parseInt(split[0].substring(5)), cellId = Integer.parseInt(split[1]);
            final Pelea fight = this.player.getPelea();

            if (fight != null) {
                Hechizo.SortStats SS = this.player.getSortStatBySortIfHas(id);

                if (SS != null)
                    if(this.player.getPelea().getCurAction().isEmpty())
                        this.player.getPelea().cast(this.player.getPelea().getFighterByPerso(this.player), () -> this.player.getPelea().tryCastSpell(this.player.getPelea().getFighterByPerso(this.player), SS, cellId));
            }
        } catch (NumberFormatException e) {
            System.err.println(packet + "\n" + e);
        }
    }

    private void gameTryCac(String packet) {
        try {
            if(packet.contains("undefined")) return;
            final int cell = Integer.parseInt(packet.substring(5));
            if (this.player.getPelea() != null && this.player.getPelea().getCurAction().isEmpty())
                this.player.getPelea().cast(this.player.getPelea().getFighterByPerso(this.player), () -> this.player.getPelea().tryCaC(this.player, cell));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void gameAction(AccionJuego GA) {
        String packet = GA.getPacket().substring(5);
        int cellID = -1;
        int actionID = -1;

        try {
            cellID = Integer.parseInt(packet.split(";")[0]);
            actionID = Integer.parseInt(packet.split(";")[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (walk) {
            actions.put(-1, GA);
            return;
        }

        //Si packet invalide, ou cellule introuvable
        if (cellID == -1 || actionID == -1 || this.player == null || this.player.getCurMap() == null || this.player.getCurMap().getCase(cellID) == null)
            return;

        GA.args = cellID + ";" + actionID;
        this.player.getGameClient().addAction(GA);
        if (this.player.isDead() == 0)
            this.player.startActionOnCell(GA);
    }

    private void houseAction(String packet) {
        int actionID = Integer.parseInt(packet.substring(5));
        Casas h = this.player.getInHouse();
        if (h == null)
            return;
        switch (actionID) {
//V�rouiller maison
            case 81 -> h.lock(this.player);
//Acheter maison
            case 97 -> h.ComprarCasa(this.player);
//Modifier prix de vente
            case 98, 108 -> h.VenderCasa(this.player);
        }
    }

    private void gameAskDuel(String packet) {
        if (this.player.getCurMap().getPlaces().equalsIgnoreCase("|")) {
            GestorSalida.GAME_SEND_DUEL_Y_AWAY(this, this.player.getId());
            return;
        }
        try {
            if (this.player.cantDefie())
                return;
            int guid = Integer.parseInt(packet.substring(5));
            if (this.player.isAway() || this.player.getPelea() != null || this.player.isDead() == 1) {
                GestorSalida.GAME_SEND_DUEL_Y_AWAY(this, this.player.getId());
                return;
            }
            Jugador Target = Mundo.mundo.getPlayer(guid);
            if (Target == null)
                return;
            if (this.player.isInAreaNotSubscribe()) {
                GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(), 'S');
                return;
            }
            if (Target.isInAreaNotSubscribe()) {
                GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(), 'S');
                return;
            }

            if (Target.isAway() || Target.getPelea() != null || Target.getCurMap().getId() != this.player.getCurMap().getId() || Target.isDead() == 1 || Target.getExchangeAction() != null || this.player.getExchangeAction() != null) {
                GestorSalida.GAME_SEND_DUEL_E_AWAY(this, this.player.getId());
                return;
            }
            this.player.setDuelId(guid);
            this.player.setAway(true);
            Mundo.mundo.getPlayer(guid).setDuelId(this.player.getId());
            Mundo.mundo.getPlayer(guid).setAway(true);
            GestorSalida.GAME_SEND_MAP_NEW_DUEL_TO_MAP(this.player.getCurMap(), this.player.getId(), guid);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void gameAcceptDuel(String packet) {
        if (this.player.cantDefie())
            return;
        int guid = -1;
        try {
            guid = Integer.parseInt(packet.substring(5));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
        if (this.player.getDuelId() != guid || this.player.getDuelId() == -1
                || this.player.isDead() == 1)
            return;
        GestorSalida.GAME_SEND_MAP_START_DUEL_TO_MAP(this.player.getCurMap(), this.player.getDuelId(), this.player.getId());
        Pelea fight = this.player.getCurMap().newFight(Mundo.mundo.getPlayer(this.player.getDuelId()), this.player, Constantes.FIGHT_TYPE_CHALLENGE);
        Jugador player = Mundo.mundo.getPlayer(this.player.getDuelId());
        this.player.setPelea(fight);
        this.player.setAway(false);
        player.setPelea(fight);
        player.setAway(false);
    }

    private void gameCancelDuel(String packet) {
        try {
            if (this.player.getDuelId() == -1)
                return;
            GestorSalida.GAME_SEND_CANCEL_DUEL_TO_MAP(this.player.getCurMap(), this.player.getDuelId(), this.player.getId());
            Jugador player = Mundo.mundo.getPlayer(this.player.getDuelId());
            player.setAway(false);
            player.setDuelId(-1);
            this.player.setAway(false);
            this.player.setDuelId(-1);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void gameJoinFight(String packet) {
        if (this.player.getPelea() != null)
            return;
        if (this.player.isDead() == 1)
            return;

        String[] infos = packet.substring(5).split(";");
        if (infos.length == 1) {
            try {
                Pelea F = this.player.getCurMap().getFight(Integer.parseInt(infos[0]));
                if (F != null)
                    F.joinAsSpectator(this.player);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                int guid = Integer.parseInt(infos[1]);
                if (this.player.isAway()) {
                    GestorSalida.GAME_SEND_GA903_ERROR_PACKET(this, 'o', guid);
                    return;
                }
                Jugador player = Mundo.mundo.getPlayer(guid);
                Pelea fight = null;

                if (player == null) {
                    Prisma prism = Mundo.mundo.getPrisme(guid);
                    if(prism != null)
                        fight = prism.getFight();
                } else {
                    fight = player.getPelea();
                }
                if (fight == null)
                    return;
                if (fight.getState() > 2) {
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "191");
                    return;
                }
                if (this.player.isInAreaNotSubscribe()) {
                    GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(), 'S');
                    return;
                }

                if(fight.getPrism() != null)
                    fight.joinPrismFight(this.player, (fight.getTeam0().containsKey(guid) ? 0 : 1));
                else
                    fight.joinFight(this.player, guid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void gameAggro(String packet) {
        try {
            if (this.player == null)
                return;
            if (this.player.getPelea() != null)
                return;
            if (this.player.isGhost())
                return;
            if (this.player.isDead() == 1)
                return;
            if (this.player.isInAreaNotSubscribe()) {
                GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(), 'S');
                return;
            }
            if (this.player.cantAgro())
                return;
            int id = Integer.parseInt(packet.substring(5));
            Jugador target = Mundo.mundo.getPlayer(id);
            if (target == null || !target.isOnline()
                    || target.getPelea() != null
                    || target.getCurMap().getId() != this.player.getCurMap().getId()
                    || target.get_align() == this.player.get_align()
                    || this.player.getCurMap().getPlaces().equalsIgnoreCase("|")
                    || !target.canAggro() || target.isDead() == 1)
                return;
            /*if(target.getAccount().getCurrentIp().equals(this.getAccount().getCurrentIp())) {
                this.player.sendMessage("Vous ne pouvez pas aggresser votre propre personnage.");
                return;
            }*/
            if (this.player.restricciones.aggros.containsKey(target.getAccount().getCurrentIp())) {
                if ((Instant.now().toEpochMilli() - this.player.restricciones.aggros.get(target.getAccount().getCurrentIp())) < 1000 * 60 * 60) {
                    GestorSalida.GAME_SEND_MESSAGE(this.player, "Il faut que tu attende encore "
                            + (((Instant.now().toEpochMilli() - this.player.restricciones.aggros.get(target.getAccount().getCurrentIp())) / 60) / 1000)
                            + " minute(s).");
                    return;
                } else {
                    this.player.restricciones.aggros.remove(target.getAccount().getCurrentIp());
                }
            }

            this.player.restricciones.aggros.put(target.getAccount().getCurrentIp(), Instant.now().toEpochMilli());

            if (target.isInAreaNotSubscribe()) {
                GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(target.getGameClient(), 'S');
                return;
            }
            if (target.get_align() == 0) {
                this.player.setDeshonor(this.player.getDeshonor() + 1);
                GestorSalida.GAME_SEND_Im_PACKET(this.player, "084;1");
            }
            this.player.toggleWings('+');
            GestorSalida.GAME_SEND_GA_PACKET_TO_MAP(this.player.getCurMap(), "", 906, this.player.getId() + "", id + "");
            this.player.getCurMap().newFight(this.player, target, Constantes.FIGHT_TYPE_AGRESSION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gameCollector(String packet) {
        try {
            if (this.player == null)
                return;
            if (this.player.getPelea() != null)
                return;
            if (this.player.getExchangeAction() != null || this.player.isDead() == 1 || this.player.isAway())
                return;

            int id = Integer.parseInt(packet.substring(5));
            Recaudador target = Mundo.mundo.getCollector(id);

            if (target == null || target.getInFight() > 0)
                return;
            if (this.player.getCurMap().getId() != target.getMap())
                return;
            if (target.getExchange()) {
                GestorSalida.GAME_SEND_Im_PACKET(this.player, "1180");
                return;
            }

            GestorSalida.GAME_SEND_GA_PACKET_TO_MAP(this.player.getCurMap(), "", 909, this.player.getId() + "", id + "");
            this.player.getCurMap().startFightVersusPercepteur(this.player, target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gamePrism(String packet) {
        try {
            if (this.player.isGhost())
                return;
            if (this.player.getPelea() != null)
                return;
            if (this.player.getExchangeAction() != null)
                return;
            if (this.player.get_align() == 0)
                return;
            if (this.player.isDead() == 1)
                return;
            int id = Integer.parseInt(packet.substring(5));
            Prisma Prisme = Mundo.mundo.getPrisme(id);
            if ((Prisme.getInFight() == 0 || Prisme.getInFight() == -2))
                return;
            GestorSalida.SEND_GA_ACTION_TO_Map(this.player.getCurMap(), "", 909, this.player.getId()
                    + "", id + "");
            this.player.getCurMap().startFightVersusPrisme(this.player, Prisme);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMonsterTarget(String packet) {
        int chalID = 0;
        chalID = Integer.parseInt(packet.split("i")[1]);
        if (chalID != 0 && this.player.getPelea() != null) {
            Pelea fight = this.player.getPelea();
            if (fight.getAllChallenges().containsKey(chalID))
                fight.getAllChallenges().get(chalID).showCibleToPerso(this.player);
        }
    }

    private void setFlag(String packet) {
        if (this.player == null)
            return;
        if (this.player.getPelea() == null)
            return;
        int cellID = -1;
        try {
            cellID = Integer.parseInt(packet.substring(2));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cellID == -1)
            return;
        this.player.getPelea().showCaseToTeam(this.player.getId(), cellID);
    }

    private void getExtraInformations() {
        try {
            if (this.player != null && this.player.needEndFight() != -1) {
                if (player.castEndFightAction())
                    player.getCurMap().applyEndFightAction(player);
                player.setNeededEndFight(-1, null);
            } else {
                sendExtraInformations();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendExtraInformations() {
        try {
            if(this.player == null) return;
            if (this.player.getPelea() != null && !this.player.getPelea().isFinish()) {
                //Only Collector
                GestorSalida.GAME_SEND_MAP_GMS_PACKETS(this.player.getPelea().getMap(), this.player);
                GestorSalida.GAME_SEND_GDK_PACKET(this);
                if (this.player.getPelea().playerReconnect(this.player))
                    return;
            }

            //Maisons
            Mundo.mundo.getHouseManager().load(this.player, this.player.getCurMap().getId());
            //Objets sur la Map
            GestorSalida.GAME_SEND_MAP_GMS_PACKETS(this.player.getCurMap(), this.player);
            GestorSalida.GAME_SEND_MAP_MOBS_GMS_PACKETS(this.player.getGameClient(), this.player.getCurMap());
            GestorSalida.GAME_SEND_MAP_NPCS_GMS_PACKETS(this, this.player.getCurMap());
            GestorSalida.GAME_SEND_MAP_PERCO_GMS_PACKETS(this, this.player.getCurMap());
            GestorSalida.GAME_SEND_MAP_OBJECTS_GDS_PACKETS(this, this.player.getCurMap());
            GestorSalida.GAME_SEND_GDK_PACKET(this);
            GestorSalida.GAME_SEND_MAP_FIGHT_COUNT(this, this.player.getCurMap());
            GestorSalida.SEND_GM_PRISME_TO_MAP(this, this.player.getCurMap());
            GestorSalida.GAME_SEND_MERCHANT_LIST(this.player, this.player.getCurMap().getId());
            //Les drapeau de combats
            Pelea.FightStateAddFlag(this.player.getCurMap(), this.player);
            //Enclos
            GestorSalida.GAME_SEND_Rp_PACKET(this.player, this.player.getCurMap().getMountPark());
            //objet dans l'enclos
            GestorSalida.GAME_SEND_GDO_OBJECT_TO_MAP(this, this.player.getCurMap());
            GestorSalida.GAME_SEND_GM_MOUNT(this, this.player.getCurMap(), true);
            //items au sol
            this.player.getCurMap().sendFloorItems(this.player);
            //Porte int�ractif
            PuertasInteractivas.show(this.player);
            //Prisme
            Mundo.mundo.showPrismes(this.player);
            this.player.refreshCraftSecure(false);
            this.player.afterFight = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actionAck(String packet) {
        int id = -1;
        String[] infos = packet.substring(3).split("\\|");
        try {
            id = Integer.parseInt(infos[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (id == -1)
            return;
        AccionJuego GA = actions.get(id);

        if (GA == null)
            return;
        boolean isOk = packet.charAt(2) == 'K';
        switch (GA.getActionId()) {
            case 1://Deplacement
                if (isOk) {
                    if (this.player.getPelea() == null) {
                        assert this.player.getCurCell() != null;

                        this.player.getCurCell().removePlayer(this.player);
                        GestorSalida.GAME_SEND_BN(this);
                        String path = GA.args;
                        //On prend la case cibl�e

                        GameCase nextCell = this.player.getCurMap().getCase(Mundo.mundo.getCryptManager().codigoceldaID(path.substring(path.length() - 2)));
                        GameCase targetCell = this.player.getCurMap().getCase(Mundo.mundo.getCryptManager().codigoceldaID(GA.getPacket().substring(GA.getPacket().length() - 2)));

                        //FIXME: Anti cheat engine speedhack

                        //On d�finie la case et on ajoute le personnage sur la case
                        this.player.setCurCell(nextCell);
                        this.player.set_orientation(Mundo.mundo.getCryptManager().getIntByHashedValue(path.charAt(path.length() - 3)));
                        this.player.getCurCell().addPlayer(this.player);
                        if (!this.player.isGhost())
                            this.player.setAway(false);
                        this.player.getCurMap().onPlayerArriveOnCell(this.player, this.player.getCurCell().getId());
                        if (targetCell.getObject() != null) {
                            if (Configuracion.INSTANCE.getMostrarenviados()) {
                            logger.info("Object Interactif {} sur la cell {} apres action", targetCell.getObject().getId(), targetCell.getId());
                            }
                            ObjetosInteractivos.getActionIO(this.player, targetCell, targetCell.getObject().getId());
                            ObjetosInteractivos.getSignIO(this.player, targetCell.getId(), targetCell.getObject().getId());
                        }

                        if (GA.tp) {
                            GA.tp = false;
                            this.player.teleport((short) 9864, 265);
                            return;
                        }
                    } else {
                        this.player.getPelea().onGK(this.player);
                        return;
                    }
                } else {
                    final Grupo party = this.player.getParty();

                    if(party != null && this.player.getPelea() == null && party.getMaster() != null && party.getMaster().getName().equals(this.player.getName())) {
                        Temporizador.addSiguiente(() -> party.getPlayers().stream()
                                .filter((follower1) -> party.isWithTheMaster(follower1, false))
                                .forEach(follower -> follower.getGameClient().actionAck(packet)), 0, TimeUnit.MILLISECONDS, Temporizador.DataType.CLIENTE);
                    }
                    //Si le joueur s'arrete sur une case
                    int newCellID = -1;
                    try {
                        newCellID = Integer.parseInt(infos[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    if (newCellID == -1)
                        return;

                    String path = GA.args;
                    this.player.getCurCell().removePlayer(this.player);
                    this.player.setCurCell(this.player.getCurMap().getCase(newCellID));
                    this.player.set_orientation(Mundo.mundo.getCryptManager().getIntByHashedValue(path.charAt(path.length() - 3)));
                    this.player.getCurCell().addPlayer(this.player);
                    GestorSalida.GAME_SEND_BN(this);
                    if (GA.tp) {
                        GA.tp = false;
                        this.player.teleport((short) 9864, 265);
                        return;
                    }
                }
                break;

            case 500://Action Sur Map
                this.player.finishActionOnCell(GA);
                this.player.setGameAction(null);
                break;

        }
        removeAction(GA);
    }

    private void setPlayerPosition(String packet) {
        if (this.player.getPelea() == null)
            return;
        try {
            int cell = Integer.parseInt(packet.substring(2));
            this.player.getPelea().exchangePlace(this.player, cell);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void leaveFight(String packet) {
        int id = -1;

        if (!packet.substring(2).isEmpty()) {
            try {
                id = Integer.parseInt(packet.substring(2));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Pelea fight = this.player.getPelea();

        if (fight == null)
            return;

        if (id > 0) {
            Jugador target = Mundo.mundo.getPlayer(id);
            //On ne quitte pas un joueur qui : est null, ne combat pas, n'est pas de �a team.
            if (target == null || target.getPelea() == null)
                return;
            if(target.getPelea().getTeamId(target.getId()) != this.player.getPelea().getTeamId(this.player.getId()))
                return;

            if ((fight.getInit0() != null && target == fight.getInit0().getPlayer()) || (fight.getInit1() != null && target == fight.getInit1().getPlayer()) || target == this.player)
                return;

            fight.leftFight(this.player, target);
        } else {
            fight.leftFight(this.player, null);
        }
    }

    private void readyFight(final String packet) {
        if (this.player.getPelea() == null)
            return;
        if (this.player.getPelea().getState() != Constantes.FIGHT_STATE_PLACE)
            return;

        this.player.setReady(packet.substring(2).equalsIgnoreCase("1"));
        this.player.getPelea().verifIfAllReady();
        GestorSalida.GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(this.player.getPelea(), 3, this.player.getId(), packet.substring(2).equalsIgnoreCase("1"));

        final Grupo party = this.player.getParty();

        if(party != null && party.getMaster() != null && party.getMaster().getName().equals(this.player.getName())) {
            Temporizador.addSiguiente(() -> party.getPlayers().stream()
                    .filter(follower -> party.isWithTheMaster(follower, true))
                    .forEach(follower -> follower.getGameClient().readyFight(packet)), 1, TimeUnit.SECONDS, Temporizador.DataType.CLIENTE);
        }
    }
    //Fin paketes de juego

    //Pakete de gremio
    private void parseGuildPacket(String packet) {
        switch (packet.charAt(1)) {
//Stats
            case 'B' -> boostCaracteristique(packet);
//Sorts
            case 'b' -> boostSpellGuild(packet);
//Creation
            case 'C' -> createGuild(packet);
//T�l�portation enclo de guilde
            case 'f' -> teleportToGuildFarm(packet.substring(2));
//Retirer Collector
            case 'F' -> removeTaxCollector(packet.substring(2));
//T�l�portation maison de guilde
            case 'h' -> teleportToGuildHouse(packet.substring(2));
//Poser un Collector
            case 'H' -> placeTaxCollector();
//Infos
            case 'I' -> getInfos(packet.charAt(2));
//Join
            case 'J' -> invitationGuild(packet.substring(2));
//Kick
            case 'K' -> banToGuild(packet.substring(2));
//Promote
            case 'P' -> changeMemberProfil(packet.substring(2));
//attaque sur Collector
            case 'T' -> joinOrLeaveTaxCollector(packet.substring(2));
//Ferme le panneau de cr�ation de guilde
            case 'V' -> leavePanelGuildCreate();
        }
    }

    private void boostCaracteristique(String packet) {
        if (this.player.getGuild() == null)
            return;
        Gremio G = this.player.getGuild();
        if (!this.player.getGuildMember().canDo(Constantes.G_BOOST))
            return;
        switch (packet.charAt(2)) {
//Prospec
            case 'p' -> {
                if (G.getCapital() < 1)
                    return;
                if (G.getStats(176) >= 500)
                    return;
                G.setCapital(G.getCapital() - 1);
                G.upgradeStats(176, 1);
            }
//Sagesse
            case 'x' -> {
                if (G.getCapital() < 1)
                    return;
                if (G.getStats(124) >= 400)
                    return;
                G.setCapital(G.getCapital() - 1);
                G.upgradeStats(124, 1);
            }
//Pod
            case 'o' -> {
                if (G.getCapital() < 1)
                    return;
                if (G.getStats(158) >= 5000)
                    return;
                G.setCapital(G.getCapital() - 1);
                G.upgradeStats(158, 20);
            }
//Nb Collector
            case 'k' -> {
                if (G.getCapital() < 10)
                    return;
                if (G.getNbCollectors() >= 50)
                    return;
                G.setCapital(G.getCapital() - 10);
                G.setNbCollectors(G.getNbCollectors() + 1);
            }
        }
        Database.dinamicos.getGuildData().update(G);
        GestorSalida.GAME_SEND_gIB_PACKET(this.player, this.player.getGuild().parseCollectorToGuild());
    }

    private void boostSpellGuild(String packet) {
        if (this.player.getGuild() == null)
            return;
        Gremio G2 = this.player.getGuild();
        if (!this.player.getGuildMember().canDo(Constantes.G_BOOST))
            return;
        int spellID = Integer.parseInt(packet.substring(2));
        if (G2.getSpells().containsKey(spellID)) {
            if (G2.getCapital() < 5)
                return;
            G2.setCapital(G2.getCapital() - 5);
            G2.boostSpell(spellID);
            Database.dinamicos.getGuildData().update(G2);
            GestorSalida.GAME_SEND_gIB_PACKET(this.player, this.player.getGuild().parseCollectorToGuild());
        } else {
            JuegoServidor.a();
        }
    }

    private void createGuild(String packet) {
        if (this.player == null)
            return;
        if (this.player.getGuild() != null || this.player.getGuildMember() != null) {
            GestorSalida.GAME_SEND_gC_PACKET(this.player, "Ea");
            return;
        }
        if (this.player.getPelea() != null || this.player.isAway())
            return;
        try {
            String[] infos = packet.substring(2).split("\\|");
            //base 10 => 36
            String bgID = Integer.toString(Integer.parseInt(infos[0]), 36);
            String bgCol = Integer.toString(Integer.parseInt(infos[1]), 36);
            String embID = Integer.toString(Integer.parseInt(infos[2]), 36);
            String embCol = Integer.toString(Integer.parseInt(infos[3]), 36);
            String name = infos[4];
            if (Mundo.mundo.guildNameIsUsed(name)) {
                GestorSalida.GAME_SEND_gC_PACKET(this.player, "Ean");
                return;
            }

            //Validation du nom de la guilde
            String tempName = name.toLowerCase();
            boolean isValid = true;
            //V�rifie d'abord si il contient des termes d�finit
            if (tempName.length() > 20 || tempName.contains("mj")
                    || tempName.contains("modo") || tempName.contains("fuck")
                    || tempName.contains("admin")) {
                isValid = false;
            }
            //Si le nom passe le test, on v�rifie que les caract�re entr� sont correct.
            if (isValid) {
                int tiretCount = 0;
                for (char curLetter : tempName.toCharArray()) {
                    if (!((curLetter >= 'a' && curLetter <= 'z') || curLetter >= 'A' && curLetter <= 'Z' || curLetter == '-')) {
                        if(curLetter == '\'') continue;
                        isValid = false;
                        break;
                    }
                    if (curLetter == '-') {
                        if (tiretCount >= 2) {
                            isValid = false;
                            break;
                        } else {
                            tiretCount++;
                        }
                    }
                    if (curLetter == ' ') {
                        if (tiretCount >= 2) {
                            isValid = false;
                            break;
                        } else {
                            tiretCount++;
                        }
                    }
                }
            }
            //Si le nom est invalide
            if (!isValid) {
                GestorSalida.GAME_SEND_gC_PACKET(this.player, "Ean");
                return;
            }
            //FIN de la validation
            StringBuilder emblem = new StringBuilder();
            emblem.append(bgID).append(",").append(bgCol).append(",").append(embID).append(",").append(embCol);//9,6o5nc,2c,0;
            if (Mundo.mundo.guildEmblemIsUsed(emblem.toString())) {
                GestorSalida.GAME_SEND_gC_PACKET(this.player, "Eae");
                return;
            }
            if (this.player.getCurMap().getId() == 2196)//Temple de cr�ation de guilde
            {
                if (!this.player.hasItemTemplate(1575, 1))//Guildalogemme
                {
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "14");
                    return;
                }
                this.player.removeByTemplateID(1575, 1);
            }
            Gremio G = new Gremio(name, emblem.toString());
            GremioMiembros gm = G.addNewMember(this.player);
            gm.setAllRights(1, (byte) 0, 1, this.player);//1 => Meneur (Tous droits)
            this.player.setGuildMember(gm);//On ajthise le meneur
            Mundo.mundo.addGuild(G, true);
            Database.estaticos.getGuildMemberData().update(this.player);
            //Packets
            GestorSalida.GAME_SEND_gS_PACKET(this.player, gm);
            GestorSalida.GAME_SEND_gC_PACKET(this.player, "K");
            GestorSalida.GAME_SEND_gV_PACKET(this.player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void teleportToGuildFarm(String packet) {
        if (this.player.getGuild() == null) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "1135");
            return;
        }
        if (this.player.getPelea() != null || this.player.isAway())
            return;
        short MapID = Short.parseShort(packet);
        Cercados MP = Mundo.mundo.getMap(MapID).getMountPark();
        if (MP.getGuild().getId() != this.player.getGuild().getId()) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "1135");
            return;
        }
        int CellID = Mundo.mundo.getEncloCellIdByMapId(MapID);
        if (this.player.hasItemTemplate(9035, 1)) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "022;1~9035");
            this.player.removeByTemplateID(9035, 1);
            this.player.teleport(MapID, CellID);
        } else {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "1159");
        }
    }

    private void removeTaxCollector(String packet) {
        if (this.player.getGuild() == null || this.player.getPelea() != null
                || this.player.isAway())
            return;
        if (!this.player.getGuildMember().canDo(Constantes.G_POSPERCO))
            return;//On peut le retirer si on a le droit de le poser
        int idCollector = Integer.parseInt(packet);
        Recaudador collector = Mundo.mundo.getCollector(idCollector);
        if (collector == null || collector.getInFight() > 0)
            return;
        collector.reloadTimer();
        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.player.getCurMap(), idCollector);
        Database.estaticos.getCollectorData().delete(collector.getId());
        collector.delCollector(collector.getId());
        for (Jugador z : this.player.getGuild().getPlayers()) {
            if (z.isOnline()) {
                GestorSalida.GAME_SEND_gITM_PACKET(z, Recaudador.parseToGuild(z.getGuild().getId()));
                String str = "" +
                        "R" + collector.getFullName() + "|" +
                        collector.getMap() + "|" +
                        Mundo.mundo.getMap(collector.getMap()).getX() + "|" + Mundo.mundo.getMap(collector.getMap()).getY() + "|" + this.player.getName();
                GestorSalida.GAME_SEND_gT_PACKET(z, str);
            }
        }
    }

    private void teleportToGuildHouse(String packet) {
        if (this.player.getGuild() == null) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "1135");
            return;
        }

        if (this.player.getPelea() != null || this.player.isAway())
            return;
        int HouseID = Integer.parseInt(packet);
        Casas h = Mundo.mundo.getHouses().get(HouseID);
        if (h == null)
            return;
        if (this.player.getGuild().getId() != h.getGuildId()) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "1135");
            return;
        }
        if (!h.canDo(Constantes.H_GTELE)) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "1136");
            return;
        }
        if (this.player.hasItemTemplate(8883, 1)) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "022;1~8883");
            this.player.removeByTemplateID(8883, 1);
            this.player.teleport((short) h.getHouseMapId(), h.getHouseCellId());
        } else {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "1137");
        }
    }

    private void placeTaxCollector() {
        final Gremio guild = this.player.getGuild();
        final Mapa map = this.player.getCurMap();

        if (guild == null || this.player.getPelea() != null || this.player.isAway() || !this.player.getGuildMember().canDo(Constantes.G_POSPERCO) || !guild.haveTenMembers())
            return;
        if (this.player.isInAreaNotSubscribe()) {
            GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(), 'S');
            return;
        }

        short price = (short) (1000 + 10 * guild.getLvl());//Calcul du prix du Collector

        if (this.player.getKamas() < price) {//Kamas insuffisants
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "182");
            return;
        }
        if (Recaudador.getCollectorByGuildId(map.getId()) > 0) {//La Map poss�de un Collector
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "1168;1");
            return;
        }
        if (map.getPlaces().length() < 5 || PiedraAlma.isInArenaMap(map.getId()) || map.noCollector) {//La map ne poss�de pas de "places"
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "113");
            return;
        }

        if (Recaudador.countCollectorGuild(guild.getId()) >= guild.getNbCollectors())
            return;

        if (Mundo.mundo.getDelayCollectors().get(map.getId()) != null) {
            long time = Mundo.mundo.getDelayCollectors().get(map.getId());

            if ((Instant.now().toEpochMilli() - time) < (((10 * guild.getLvl()) * 60) * 1000)) {
                this.player.send("Im1167;" + ((((((10 * guild.getLvl()) * 60) * 1000) - (Instant.now().toEpochMilli() - time)) / 1000) / 60));
                return;
            }
            Mundo.mundo.getDelayCollectors().remove(map.getId());
        }

        if(map.getSubArea() != null) {
            final byte[] quit = {0};
            Mundo.mundo.getCollectors().values().stream().filter(collector -> collector != null && collector.getGuildId() == guild.getId()).forEach(collector -> {
                Mapa curMap = Mundo.mundo.getMap(collector.getMap());
                if (curMap.getSubArea() != null && curMap.getSubArea().getId() == map.getSubArea().getId()) {
                    this.player.send("Im1168;1");
                    quit[0] = 1;
                }
            });
            if(quit[0] == 1) return;
        }

        Mundo.mundo.getDelayCollectors().put(map.getId(), Instant.now().toEpochMilli());
        this.player.setKamas(this.player.getKamas() - price);

        if (this.player.getKamas() <= 0)
            this.player.setKamas(0);


        short n1 = (short) (Formulas.getRandomValue(1, 129)), n2 = (short) (Formulas.getRandomValue(1, 227));
        int id = Database.estaticos.getCollectorData().getId();

        Recaudador collector = new Recaudador(id, map.getId(), this.player.getCurCell().getId(), (byte) 3, guild.getId(), n1, n2, this.player, Instant.now().toEpochMilli(), "", 0, 0);
        Mundo.mundo.addCollector(collector);
        GestorSalida.GAME_SEND_ADD_PERCO_TO_MAP(map);
        GestorSalida.GAME_SEND_STATS_PACKET(this.player);
        Database.estaticos.getCollectorData().add(id, map.getId(), guild.getId(), this.player.getId(), Instant.now().toEpochMilli(), this.player.getCurCell().getId(), 3, n1, n2);

        for (Jugador player : guild.getPlayers()) {
            if (player != null && player.isOnline()) {
                GestorSalida.GAME_SEND_gITM_PACKET(player, Recaudador.parseToGuild(player.getGuild().getId()));
                String str = "" +
                        "S" + collector.getFullName() + "|" +
                        collector.getMap() + "|" +
                        Mundo.mundo.getMap(collector.getMap()).getX() + "|" + Mundo.mundo.getMap(collector.getMap()).getY() + "|" + this.player.getName();
                GestorSalida.GAME_SEND_gT_PACKET(player, str);
            }
        }
    }

    private void getInfos(char c) {
        switch (c) {
//Collector
            case 'B' -> GestorSalida.GAME_SEND_gIB_PACKET(this.player, this.player.getGuild().parseCollectorToGuild());
//Enclos
            case 'F' -> GestorSalida.GAME_SEND_gIF_PACKET(this.player, Mundo.mundo.parseMPtoGuild(this.player.getGuild().getId()));
//General
            case 'G' -> GestorSalida.GAME_SEND_gIG_PACKET(this.player, this.player.getGuild());
//House
            case 'H' -> GestorSalida.GAME_SEND_gIH_PACKET(this.player, Mundo.mundo.getHouseManager().parseHouseToGuild(this.player));
//Members
            case 'M' -> GestorSalida.GAME_SEND_gIM_PACKET(this.player, this.player.getGuild(), '+');
//Collector
            case 'T' -> {
                GestorSalida.GAME_SEND_gITM_PACKET(this.player, Recaudador.parseToGuild(this.player.getGuild().getId()));
                Recaudador.parseAttaque(this.player, this.player.getGuild().getId());
                Recaudador.parseDefense(this.player, this.player.getGuild().getId());
            }
        }
    }

    private void invitationGuild(String packet) {
        switch (packet.charAt(0)) {
            case 'R'://Nom this.player
                Jugador P = Mundo.mundo.getPlayerByName(packet.substring(1));
                if (P == null || this.player.getGuild() == null) {
                    GestorSalida.GAME_SEND_gJ_PACKET(this.player, "Eu");
                    return;
                }
                if (!P.isOnline()) {
                    GestorSalida.GAME_SEND_gJ_PACKET(this.player, "Eu");
                    return;
                }
                if (P.isAway()) {
                    GestorSalida.GAME_SEND_gJ_PACKET(this.player, "Eo");
                    return;
                }
                if (P.getGuild() != null) {
                    GestorSalida.GAME_SEND_gJ_PACKET(this.player, "Ea");
                    return;
                }
                if (!this.player.getGuildMember().canDo(Constantes.G_INVITE)) {
                    GestorSalida.GAME_SEND_gJ_PACKET(this.player, "Ed");
                    return;
                }
                if (this.player.getGuild().getPlayers().size() >= (40 + this.player.getGuild().getLvl()))//Limite membres max
                {
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "155;"
                            + (40 + this.player.getGuild().getLvl()));
                    return;
                }
                this.player.setInvitation(P.getId());
                P.setInvitation(this.player.getId());

                GestorSalida.GAME_SEND_gJ_PACKET(this.player, "R"
                        + packet.substring(1));
                GestorSalida.GAME_SEND_gJ_PACKET(P, "r" + this.player.getId() + "|"
                        + this.player.getName() + "|" + this.player.getGuild().getName());
                break;
            case 'E'://ou Refus
                if (packet.substring(1).equalsIgnoreCase(this.player.getInvitation()
                        + "")) {
                    Jugador p = Mundo.mundo.getPlayer(this.player.getInvitation());
                    if (p == null)
                        return;//Pas cens� arriver
                    GestorSalida.GAME_SEND_gJ_PACKET(p, "Ec");
                }
                break;
            case 'K'://Accepte
                if (packet.substring(1).equalsIgnoreCase(this.player.getInvitation()
                        + "")) {
                    Jugador p = Mundo.mundo.getPlayer(this.player.getInvitation());
                    if (p == null)
                        return;//Pas cens� arriver
                    Gremio G = p.getGuild();
                    GremioMiembros GM = G.addNewMember(this.player);
                    Database.estaticos.getGuildMemberData().update(this.player);
                    this.player.setGuildMember(GM);
                    this.player.setInvitation(-1);
                    p.setInvitation(-1);
                    //if (G.getId() == 1)
                       // this.player.modifAlignement(3);
                    //Packet
                    GestorSalida.GAME_SEND_gJ_PACKET(p, "Ka" + this.player.getName());
                    GestorSalida.GAME_SEND_gS_PACKET(this.player, GM);
                    GestorSalida.GAME_SEND_gJ_PACKET(this.player, "Kj");
                }
                break;
        }
    }

    private void banToGuild(String name) {
        if (this.player.getGuild() == null)
            return;
        Jugador P = Mundo.mundo.getPlayerByName(name);
        int guid = -1, guildId = -1;
        Gremio toRemGuild;
        GremioMiembros toRemMember;
        if (P == null) {
            int[] infos = Database.estaticos.getGuildMemberData().isPersoInGuild(name);
            guid = infos[0];
            guildId = infos[1];
            if (guildId < 0 || guid < 0)
                return;
            toRemGuild = Mundo.mundo.getGuild(guildId);
            toRemMember = toRemGuild.getMember(guid);
        } else {
            toRemGuild = P.getGuild();
            if (toRemGuild == null)//La guilde du this.playernnage n'est pas charger ?
            {
                toRemGuild = Mundo.mundo.getGuild(this.player.getGuild().getId());//On prend la guilde du this.player qui l'�jecte
            }
            toRemMember = toRemGuild.getMember(P.getId());
            if (toRemMember == null)
                return;//Si le membre n'est pas dans la guilde.
            if (toRemMember.getGuild().getId() != this.player.getGuild().getId())
                return;//Si guilde diff�rente
        }
        //si pas la meme guilde
        if (toRemGuild.getId() != this.player.getGuild().getId()) {
            GestorSalida.GAME_SEND_gK_PACKET(this.player, "Ea");
            return;
        }
        //S'il n'a pas le droit de kick, et que ce n'est pas lui m�me la cible
        if (!this.player.getGuildMember().canDo(Constantes.G_BAN)
                && this.player.getGuildMember().getPlayerId() != toRemMember.getPlayerId()) {
            GestorSalida.GAME_SEND_gK_PACKET(this.player, "Ed");
            return;
        }
        //Si diff�rent : Kick
        if (this.player.getGuildMember().getPlayerId() != toRemMember.getPlayerId()) {
            if (toRemMember.getRank() == 1) //S'il veut kicker le meneur
                return;

            toRemGuild.removeMember(toRemMember.getPlayer());
            if (P != null)
                P.setGuildMember(null);
            if (toRemGuild.getId() == 1)
                toRemMember.getPlayer().modifAlignement(0);
            GestorSalida.GAME_SEND_gK_PACKET(this.player, "K" + this.player.getName()
                    + "|" + name);
            if (P != null)
                GestorSalida.GAME_SEND_gK_PACKET(P, "K" + this.player.getName());
        } else
        //si quitter
        {
            Gremio G = this.player.getGuild();
            if (this.player.getGuildMember().getRank() == 1
                    && G.getPlayers().size() > 1) //Si le meneur veut quitter la guilde mais qu'il reste d'autre joueurs
            {
                GestorSalida.GAME_SEND_MESSAGE(this.player, "Vous devez mettre un autre meneur pour devoir quitter la guilde !");
                return;
            }
            G.removeMember(this.player);
            this.player.setGuildMember(null);
            //if (G.getId() == 1)
                //this.player.modifAlignement(0);
            //S'il n'y a plus this.playernne
            if (G.getPlayers().isEmpty())
                Mundo.mundo.removeGuild(G.getId());
            GestorSalida.GAME_SEND_gK_PACKET(this.player, "K" + name + "|" + name);
        }
    }

    private void changeMemberProfil(String packet) {
        if (this.player.getGuild() == null)
            return; //Si le this.playernnage envoyeur n'a m�me pas de guilde

        String[] infos = packet.split("\\|");

        int guid = Integer.parseInt(infos[0]);
        int rank = Integer.parseInt(infos[1]);
        byte xpGive = Byte.parseByte(infos[2]);
        int right = Integer.parseInt(infos[3]);

        Jugador p = Mundo.mundo.getPlayer(guid); //Cherche le this.playernnage a qui l'on change les droits dans la m�moire
        GremioMiembros toChange;
        GremioMiembros changer = this.player.getGuildMember();

        //R�cup�ration du this.playernnage � changer, et verification de quelques conditions de base
        if (p == null) //Arrive lorsque le this.playernnage n'est pas charg� dans la m�moire
        {
            int guildId = Database.estaticos.getGuildMemberData().isPersoInGuild(guid); //R�cup�re l'id de la guilde du this.playernnage qui n'est pas dans la m�moire

            if (guildId < 0)
                return; //Si le this.playernnage � qui les droits doivent �tre modifi� n'existe pas ou n'a pas de guilde

            if (guildId != this.player.getGuild().getId()) //Si ils ne sont pas dans la m�me guilde
            {
                GestorSalida.GAME_SEND_gK_PACKET(this.player, "Ed");
                return;
            }
            toChange = Mundo.mundo.getGuild(guildId).getMember(guid);
        } else {
            if (p.getGuild() == null)
                return; //Si la this.playernne � qui changer les droits n'a pas de guilde
            if (this.player.getGuild().getId() != p.getGuild().getId()) //Si ils ne sont pas de la meme guilde
            {
                GestorSalida.GAME_SEND_gK_PACKET(this.player, "Ea");
                return;
            }

            toChange = p.getGuildMember();
        }

        //V�rifie ce que le this.playernnage changeur � le droit de faire

        if (changer.getRank() == 1) //Si c'est le meneur
        {
            if (changer.getPlayerId() == toChange.getPlayerId()) //Si il se modifie lui m�me, reset tthis sauf l'XP
            {
                rank = -1;
                right = -1;
            } else
            //Si il modifie un autre membre
            {
                if (rank == 1) //Si il met un autre membre "Meneur"
                {
                    changer.setAllRights(2, (byte) -1, 29694, this.player); //Met le meneur "Bras droit" avec tthis les droits

                    //D�fini les droits � mettre au nouveau meneur
                    rank = 1;
                    xpGive = -1;
                    right = 1;
                }
            }
        } else
        //Sinon, c'est un membre normal
        {
            if (toChange.getRank() == 1) //S'il veut changer le meneur, reset tthis sauf l'XP
            {
                rank = -1;
                right = -1;
            } else
            //Sinon il veut changer un membre normal
            {
                if (!changer.canDo(Constantes.G_RANK) || rank == 1) //S'il ne peut changer les rang ou qu'il veut mettre meneur
                    rank = -1; //"Reset" le rang

                if (!changer.canDo(Constantes.G_RIGHT) || right == 1) //S'il ne peut changer les droits ou qu'il veut mettre les droits de meneur
                    right = -1; //"Reset" les droits

                if (!changer.canDo(Constantes.G_HISXP)
                        && !changer.canDo(Constantes.G_ALLXP)
                        && changer.getPlayerId() == toChange.getPlayerId()) //S'il ne peut changer l'XP de this.playernne et qu'il est la cible
                    xpGive = -1; //"Reset" l'XP
            }

            if (!changer.canDo(Constantes.G_ALLXP) && !changer.equals(toChange)) //S'il n'a pas le droit de changer l'XP des autres et qu'il n'est pas la cible
                xpGive = -1; //"Reset" L'XP
        }
        toChange.setAllRights(rank, xpGive, right, this.player);
        GestorSalida.GAME_SEND_gS_PACKET(this.player, this.player.getGuildMember());
        if (p != null && p.getId() != this.player.getId())
            GestorSalida.GAME_SEND_gS_PACKET(p, p.getGuildMember());
    }

    private void joinOrLeaveTaxCollector(String packet) {
        int id = -1;
        String CollectorID = Integer.toString(Integer.parseInt(packet.substring(1)), 36);
        try {
            id = Integer.parseInt(CollectorID);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Recaudador collector = Mundo.mundo.getCollector(id);
        boolean fail = this.player.isDead() == 1 || collector == null || collector.getInFight() <= 0;

        if(collector != null) {
            switch (packet.charAt(0)) {
                case 'J'://Rejoindre
                    if(fail = collector.addDefenseFight(this.player)) {
                        Mundo.mundo.getGuild(collector.getGuildId()).getPlayers().stream().filter(player -> player != null && player.isOnline()).forEach(player -> Recaudador.parseDefense(player, collector.getGuildId()));
                    }
                    break;
                case 'V'://Leave
                    if(fail = collector.delDefenseFight(this.player)) {
                        Mundo.mundo.getGuild(collector.getGuildId()).getPlayers().stream().filter(player -> player != null && player.isOnline()).forEach(player -> player.send("gITP-" + collector.getId() + "|" + Integer.toString(player.getId(), 36)));
                    }
                    break;
            }
        }
        if (!fail) {
            GestorSalida.GAME_SEND_BN(this.player);
        }
    }

    private void leavePanelGuildCreate() {
        GestorSalida.GAME_SEND_gV_PACKET(this.player);
    }

    /**
     * Housse Packet *
     */
    private void parseHousePacket(String packet) {
        switch (packet.charAt(1)) {
//Acheter la maison
            case 'B' -> {
                packet = packet.substring(2);
                Mundo.mundo.getHouseManager().buy(this.player);
            }
//Maison de guilde
            case 'G' -> {
                packet = packet.substring(2);
                if (packet.isEmpty())
                    packet = null;
                Mundo.mundo.getHouseManager().parseHG(this.player, packet);
            }
//Quitter/Expulser de la maison
            case 'Q' -> {
                packet = packet.substring(2);
                Mundo.mundo.getHouseManager().leave(this.player, packet);
            }
//Modification du prix de vente
            case 'S' -> {
                packet = packet.substring(2);
                Mundo.mundo.getHouseManager().sell(this.player, packet);
            }
//Fermer fenetre d'achat
            case 'V' -> Mundo.mundo.getHouseManager().closeBuy(this.player);
        }
    }

    /**
     * Enemy Packet *
     */
    private void parseEnemyPacket(String packet) {
        switch (packet.charAt(1)) {
//Ajthiser
            case 'A' -> addEnemy(packet);
//Delete
            case 'D' -> removeEnemy(packet);
//Liste
            case 'L' -> GestorSalida.GAME_SEND_ENEMY_LIST(this.player);
        }
    }

    private void addEnemy(String packet) {
        if (this.player == null)
            return;
        int guid = -1;
        switch (packet.charAt(2)) {
//Nom de this.player
            case '%' -> {
                packet = packet.substring(3);
                Jugador P = Mundo.mundo.getPlayerByName(packet);
                if (P == null) {
                    GestorSalida.GAME_SEND_FD_PACKET(this.player, "Ef");
                    return;
                }
                guid = P.getAccID();
            }
//Pseudo
            case '*' -> {
                packet = packet.substring(3);
                Cuenta C = Mundo.mundo.getAccountByPseudo(packet);
                if (C == null) {
                    GestorSalida.GAME_SEND_FD_PACKET(this.player, "Ef");
                    return;
                }
                guid = C.getId();
            }
            default -> {
                packet = packet.substring(2);
                Jugador Pr = Mundo.mundo.getPlayerByName(packet);
                if (Pr == null || !Pr.isOnline()) {
                    GestorSalida.GAME_SEND_FD_PACKET(this.player, "Ef");
                    return;
                }
                guid = Pr.getAccount().getId();
            }
        }
        if (guid == -1) {
            GestorSalida.GAME_SEND_FD_PACKET(this.player, "Ef");
            return;
        }
        account.addEnemy(packet, guid);
    }

    private void removeEnemy(String packet) {
        if (this.player == null)
            return;
        int guid = -1;
        switch (packet.charAt(2)) {
//Nom de this.player
            case '%' -> {
                packet = packet.substring(3);
                Jugador P = Mundo.mundo.getPlayerByName(packet);
                if (P == null) {
                    GestorSalida.GAME_SEND_FD_PACKET(this.player, "Ef");
                    return;
                }
                guid = P.getAccID();
            }
//Pseudo
            case '*' -> {
                packet = packet.substring(3);
                Cuenta C = Mundo.mundo.getAccountByPseudo(packet);
                if (C == null) {
                    GestorSalida.GAME_SEND_FD_PACKET(this.player, "Ef");
                    return;
                }
                guid = C.getId();
            }
            default -> {
                packet = packet.substring(2);
                Jugador Pr = Mundo.mundo.getPlayerByName(packet);
                if (Pr == null || !Pr.isOnline()) {
                    GestorSalida.GAME_SEND_FD_PACKET(this.player, "Ef");
                    return;
                }
                guid = Pr.getAccount().getId();
            }
        }
        if (guid == -1 || !account.isEnemyWith(guid)) {
            GestorSalida.GAME_SEND_FD_PACKET(this.player, "Ef");
            return;
        }
        account.removeEnemy(guid);
    }
    //Paketes enemigos

    //Opciones de oficio
    private void parseJobOption(String packet) {
        if (packet.charAt(1) == 'O') {
            String[] infos = packet.substring(2).split("\\|");
            int pos = Integer.parseInt(infos[0]);
            int option = Integer.parseInt(infos[1]);
            int slots = Integer.parseInt(infos[2]);
            OficioCaracteristicas SM = this.player.getMetiers().get(pos);
            if (SM == null)
                return;
            SM.setOptBinValue(option);
            SM.setSlotsPublic(slots);
            GestorSalida.GAME_SEND_JO_PACKET(this.player, SM);
        }
    }
    //Fin opciones de oficio

    //Codigos de casas
    private void parseHouseKodePacket(String packet) {
        switch (packet.charAt(1)) {
            //Fermer fenetre du code
            case 'V' -> Mundo.mundo.getHouseManager().closeCode(this.player);
            //Envoi du code
            case 'K' -> sendKey(packet);
        }
    }

    private void sendKey(String packet) {
        switch (packet.charAt(2)) {
            case '0'://Envoi du code || Boost
                packet = packet.substring(4);
                if (this.player.get_savestat() > 0) {
                    try {
                        int code = 0;
                        code = Integer.parseInt(packet);
                        if (code < 0)
                            return;
                        if (this.player.get_capital() < code)
                            code = this.player.get_capital();
                        this.player.boostStatFixedCount(this.player.get_savestat(), code);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        this.player.set_savestat(0);
                        GestorSalida.GAME_SEND_KODE(this.player, "V");
                    }
                } else if (this.player.getExchangeAction() != null && this.player.getExchangeAction().getType() == AccionIntercambiar.IN_TRUNK) {
                    Cofres.open(this.player, packet, false);
                } else {
                    if (this.player.getInHouse() != null)
                        this.player.getInHouse().open(this.player, packet, false);
                }
                break;
            case '1'://Changement du code
                if (this.player.getExchangeAction() != null && this.player.getExchangeAction().getType() == AccionIntercambiar.IN_TRUNK)
                    Cofres.lock(this.player, packet.substring(4));
                else
                    Mundo.mundo.getHouseManager().lockIt(this.player, packet.substring(4));
                break;
        }
    }
    //Fin codigos de casas

    //Pakete de objetos
    private void parseObjectPacket(String packet) {
        switch (packet.charAt(1)) {
            case 'd' -> destroyObject(packet);
            case 'D' -> dropObject(packet);
            case 'M' -> movementObject(packet);
            case 'U' -> useObject(packet);
            case 'x' -> dissociateObvi(packet);
            case 'f' -> feedObvi(packet);
            case 's' -> setSkinObvi(packet);
        }
    }

    private void destroyObject(String packet) {
        String[] infos = packet.substring(2).split("\\|");
        try {
            int guid = Integer.parseInt(infos[0]);
            int qua = 1;
            try {
                qua = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ObjetoJuego obj = this.player.getItems().get(guid);
            if (obj == null || !this.player.hasItemGuid(guid) || qua <= 0
                    || this.player.getPelea() != null || this.player.isAway()) {
                //SocketManager.GAME_SEND_DELETE_OBJECT_FAILED_PACKET(this);
                return;
            }
            if (obj.getPosicion() != Constantes.ITEM_POS_NO_EQUIPED)
                return;
            if (qua > obj.getCantidad())
                qua = obj.getCantidad();
            int newQua = obj.getCantidad() - qua;
            if (newQua <= 0) {
                this.player.removeItem(guid);
                Mundo.mundo.removeGameObject(guid);
                Database.dinamicos.getObjectData().delete(guid);
                GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this.player, guid);
            } else {
                obj.setCantidad(newQua);
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, obj);
            }
            GestorSalida.GAME_SEND_STATS_PACKET(this.player);
            GestorSalida.GAME_SEND_Ow_PACKET(this.player);
        } catch (Exception e) {
            e.printStackTrace();
            GestorSalida.GAME_SEND_DELETE_OBJECT_FAILED_PACKET(this);
        }
    }

    private void dropObject(String packet) {
        if (this.player.getExchangeAction() != null) return;

        int guid = -1;
        int qua = -1;
        try {
            guid = Integer.parseInt(packet.substring(2).split("\\|")[0]);
            qua = Integer.parseInt(packet.split("\\|")[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (guid == -1 || qua <= 0 || !this.player.hasItemGuid(guid)
                || this.player.getPelea() != null || this.player.isAway())
            return;
        ObjetoJuego obj = this.player.getItems().get(guid);

        if(obj.isAttach()) return;

        int cellPosition = Constantes.getNearestCellIdUnused(this.player);

        if (cellPosition < 0) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "1145");
            return;
        }
        if (obj.getPosicion() != Constantes.ITEM_POS_NO_EQUIPED) {
            obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
            GestorSalida.GAME_SEND_OBJET_MOVE_PACKET(this.player, obj);
            if (obj.getPosicion() == Constantes.ITEM_POS_ARME
                    || obj.getPosicion() == Constantes.ITEM_POS_COIFFE
                    || obj.getPosicion() == Constantes.ITEM_POS_FAMILIER
                    || obj.getPosicion() == Constantes.ITEM_POS_CAPE
                    || obj.getPosicion() == Constantes.ITEM_POS_BOUCLIER
                    || obj.getPosicion() == Constantes.ITEM_POS_NO_EQUIPED)
                GestorSalida.GAME_SEND_ON_EQUIP_ITEM(this.player.getCurMap(), this.player);
        }
        if (qua >= obj.getCantidad()) {
            this.player.removeItem(guid);
            this.player.getCurMap().getCase(cellPosition).addDroppedItem(obj);
            obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
            GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this.player, guid);
        } else {
            obj.setCantidad(obj.getCantidad() - qua);
            ObjetoJuego obj2 = ObjetoJuego.getCloneObjet(obj, qua);
            obj2.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
            this.player.getCurMap().getCase(cellPosition).addDroppedItem(obj2);
            GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, obj);
        }
        if (Configuracion.INSTANCE.getMostrarenviados()) {
        Logging.objetos.info("Dropeo: " + this.player.getName() + " a jeté [" + obj.getModelo().getId() + "@" + obj.getId() + ";" + qua + "]");
        }
        GestorSalida.GAME_SEND_Ow_PACKET(this.player);
        GestorSalida.GAME_SEND_GDO_PACKET_TO_MAP(this.player.getCurMap(), '+', this.player.getCurMap().getCase(cellPosition).getId(), obj.getModelo().getId(), 0);
        GestorSalida.GAME_SEND_STATS_PACKET(this.player);
    }

    private synchronized void movementObject(String packet) {
        String[] infos = packet.substring(2).split("" + (char) 0x0A)[0].split("\\|");
        try {
            int quantity = 1, id = Integer.parseInt(infos[0]), position = Integer.parseInt(infos[1]);
            try {
                quantity = Integer.parseInt(infos[2]);
            } catch (Exception ignored) {}

            ObjetoJuego object = Mundo.getGameObject(id);
            if (!this.player.hasItemGuid(id) || object == null)
                return;
            if (this.player.getPelea() != null)
                if (this.player.getPelea().getState() > Constantes.FIGHT_STATE_ACTIVE)
                    return;

            if(this.player.deathMatch != null && this.player.getEquippedObjects().size() == 1) {
                boolean bool = false;
                for(ObjetoJuego clone : this.player.getEquippedObjects())
                    if (object.getId() == clone.getId()) {
                        bool = true;
                        break;
                    }
                if(bool) {
                    this.player.sendMessage("Vous ne pouvez pas enlever cet objet en recherche d'un deathmatch.");
                    return;
                }
            }

            /* Pet subscribe **/
            if (position == Constantes.ITEM_POS_FAMILIER && !this.player.isSubscribe()) {
                GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this, 'S');
                return;
            }
            /* End pet subscribe **/

            /* Feed mount **/
            if ((position == Constantes.ITEM_POS_DRAGODINDE) && (this.player.getMount() != null)) {
                if (object.getModelo().getType() == 41) {
                    if (object.getCantidad() > 0) {
                        if (quantity > object.getCantidad())
                            quantity = object.getCantidad();
                        if (object.getCantidad() - quantity > 0) {
                            int newQua = object.getCantidad() - quantity;
                            object.setCantidad(newQua);
                            GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, object);
                        } else {
                            this.player.deleteItem(id);
                            Mundo.mundo.removeGameObject(id);
                            GestorSalida.SEND_OR_DELETE_ITEM(this, id);
                        }
                    }
                    this.player.getMount().aumEnergy(5000 * quantity);
                    GestorSalida.GAME_SEND_Re_PACKET(this.player, "+", this.player.getMount());
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "0105");
                    return;
                }
                GestorSalida.GAME_SEND_Im_PACKET(this.player, "190");
                return;
            }
            /* End feed mount **/

            /* Feed pet **/
            if (position == Constantes.ITEM_POS_FAMILIER && object.getModelo().getType() != Constantes.ITEM_TYPE_FAMILIER && this.player.getObjetByPos(position) != null) {
                ObjetoJuego pets = this.player.getObjetByPos(position);
                Mascota p = Mundo.mundo.getPets(pets.getModelo().getId());
                if (p == null)
                    return;
                if (p.getEpo() == object.getModelo().getId()) {
                    MascotaEntrada pet = Mundo.mundo.getPetsEntry(pets.getId());
                    if (pet != null && p.getEpo() == object.getModelo().getId())
                        pet.giveEpo(this.player);
                    return;
                }
                if (object.getModelo().getId() != 2239 && !p.canEat(object.getModelo().getId(), object.getModelo().getType(), -1)) {
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "153");
                    return;
                }

                int min = 0, max = 0;
                try {
                    min = Integer.parseInt(p.getGap().split(",")[0]);
                    max = Integer.parseInt(p.getGap().split(",")[1]);
                } catch (Exception e) {
                    // ok
                }

                MascotaEntrada MyPets = Mundo.mundo.getPetsEntry(pets.getId());
                if (MyPets == null)
                    return;
                if (p.getType() == 2 || p.getType() == 3
                        || object.getModelo().getId() == 2239) {
                    if (object.getCantidad() - 1 > 0) {//Si il en reste
                        object.setCantidad(object.getCantidad() - 1);
                        GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, object);
                    } else {
                        Mundo.mundo.removeGameObject(object.getId());
                        this.player.removeItem(object.getId());
                        GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this.player, object.getId());
                    }

                    if (object.getModelo().getId() == 2239)
                        MyPets.restoreLife(this.player);
                    else
                        MyPets.eat(this.player, min, max, p.statsIdByEat(object.getModelo().getId(), object.getModelo().getType(), -1), object);

                    GestorSalida.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(this.player, pets);
                    GestorSalida.GAME_SEND_Ow_PACKET(this.player);
                    this.player.refreshStats();
                    GestorSalida.GAME_SEND_ON_EQUIP_ITEM(this.player.getCurMap(), this.player);
                    GestorSalida.GAME_SEND_STATS_PACKET(this.player);
                    if (this.player.getParty() != null)
                        GestorSalida.GAME_SEND_PM_MOD_PACKET_TO_GROUP(this.player.getParty(), this.player);
                }
                return;
            /* End feed pet **/
            } else {
                ObjetoModelo template = object.getModelo();
                int set = template.getPanoId();

                if (set >= 81 && set <= 92 && position != Constantes.ITEM_POS_NO_EQUIPED) {
                    String[] stats = template.getStrTemplate().split(",");

                    for (String stat : stats) {
                        String[] split = stat.split("#");
                        int effect = Integer.parseInt(split[0], 16),spell = Integer.parseInt(split[1], 16);
                        int value = Integer.parseInt(split[3], 16);
                        if(effect == 289)
                            value = 1;
                        GestorSalida.SEND_SB_SPELL_BOOST(this.player, effect + ";" + spell + ";" + value);
                        this.player.addObjectClassSpell(spell, effect, value);
                    }
                    this.player.addObjectClass(template.getId());
                }
                if (set >= 81 && set <= 92 && position == Constantes.ITEM_POS_NO_EQUIPED) {
                    String[] stats = template.getStrTemplate().split(",");

                    for (String stat : stats) {
                        String[] split = stat.split("#");
                        int effect = Integer.parseInt(split[0], 16),spell = Integer.parseInt(split[1], 16);
                        GestorSalida.SEND_SB_SPELL_BOOST(this.player, effect + ";" + spell + ";0");
                        this.player.removeObjectClassSpell(Integer.parseInt(split[1], 16));
                    }
                    this.player.removeObjectClass(template.getId());
                }
                if (!Constantes.isValidPlaceForItem(object.getModelo(), position) && position != Constantes.ITEM_POS_NO_EQUIPED && object.getModelo().getType() != 113)
                    return;

                if (!object.getModelo().getConditions().equalsIgnoreCase("") && !Mundo.mundo.getConditionManager().validConditions(this.player, object.getModelo().getConditions())) {
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "119|44"); // si le this.player ne v�rifie pas les conditions diverses
                    return;
                }
                if ((position == Constantes.ITEM_POS_BOUCLIER && this.player.getObjetByPos(Constantes.ITEM_POS_ARME) != null) || (position == Constantes.ITEM_POS_ARME && this.player.getObjetByPos(Constantes.ITEM_POS_BOUCLIER) != null)) {
                    if (this.player.getObjetByPos(Constantes.ITEM_POS_ARME) != null) {
                        if (this.player.getObjetByPos(Constantes.ITEM_POS_ARME).getModelo().isTwoHanded()) {
                            GestorSalida.GAME_SEND_Im_PACKET(this.player, "119|44"); // si le this.player ne v�rifie pas les conditions diverses
                            return;
                        }
                    } else {
                        if (object.getModelo().isTwoHanded()) {
                            GestorSalida.GAME_SEND_Im_PACKET(this.player, "119|44"); // si le this.player ne v�rifie pas les conditions diverses
                            return;
                        }
                    }

                }

                if (object.getModelo().getLevel() > this.player.getLevel()) {// si le this.player n'a pas le level
                    GestorSalida.GAME_SEND_OAEL_PACKET(this);
                    return;
                }

                //On ne peut �quiper 2 items de panoplies identiques, ou 2 Dofus identiques
                if (position != Constantes.ITEM_POS_NO_EQUIPED && (object.getModelo().getPanoId() != -1 || object.getModelo().getType() == Constantes.ITEM_TYPE_DOFUS) && this.player.hasEquiped(object.getModelo().getId()))
                    return;

                // FIN DES VERIFS

                ObjetoJuego exObj = this.player.getObjetByPos2(position);//Objet a l'ancienne position
                int objGUID = object.getModelo().getId();
                // CODE OBVI
                if (object.getModelo().getType() == 113) {
                    if (exObj == null) {// si on place l'obvi sur un emplacement vide
                        GestorSalida.send(this.player, "Im1161");
                        return;
                    }
                    if (exObj.getObvijevanPos() != 0) {// si il y a d�j� un obvi
                        GestorSalida.GAME_SEND_BN(this.player);
                        return;
                    }
                    exObj.setObvijevanPos(object.getObvijevanPos()); // L'objet qui �tait en place a maintenant un obvi
                    Database.dinamicos.getObvejivanData().add(object, exObj);
                    this.player.removeItem(object.getId(), 1, false, false); // on enl�ve l'existance de l'obvi en lui-m�me
                    GestorSalida.send(this.player, "OR" + object.getId()); // on le pr�cise au client.
                    Database.dinamicos.getObjectData().delete(object.getId());

                    exObj.refreshStatsObjet(object.parseStatsStringSansUserObvi() + ",3ca#" + Integer.toHexString(objGUID) + "#0#0#0d0+" + objGUID);

                    GestorSalida.send(this.player, exObj.obvijevanOCO_Packet(position));
                    GestorSalida.GAME_SEND_ON_EQUIP_ITEM(this.player.getCurMap(), this.player); // Si l'obvi �tait cape ou coiffe : packet au client
                    // S'il y avait plusieurs objets
                    if (object.getCantidad() > 1) {
                        if (quantity > object.getCantidad())
                            quantity = object.getCantidad();

                        if (object.getCantidad() - quantity > 0)//Si il en reste
                        {
                            int newItemQua = object.getCantidad() - quantity;
                            ObjetoJuego newItem = ObjetoJuego.getCloneObjet(object, newItemQua);
                            this.player.addObjet(newItem, false);
                            Mundo.addGameObject(newItem, true);
                            object.setCantidad(quantity);
                            GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, object);
                        }
                    } else {
                        Mundo.mundo.removeGameObject(object.getId());
                    }
                    Database.dinamicos.getPlayerData().update(this.player);
                    return; // on s'arr�te l� pour l'obvi
                } // FIN DU CODE OBVI

                if (exObj != null)//S'il y avait d�ja un objet sur cette place on d�s�quipe
                {
                    ObjetoJuego obj2;
                    ObjetoModelo exObjTpl = exObj.getModelo();
                    int idSetExObj = exObj.getModelo().getPanoId();
                    if ((obj2 = this.player.getSimilarItem(exObj)) != null)//On le poss�de deja
                    {
                        obj2.setCantidad(obj2.getCantidad()
                                + exObj.getCantidad());
                        GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, obj2);
                        Mundo.mundo.removeGameObject(exObj.getId());
                        this.player.removeItem(exObj.getId());
                        GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this.player, exObj.getId());
                    } else
                    //On ne le poss�de pas
                    {
                        exObj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                        if ((idSetExObj >= 81 && idSetExObj <= 92)
                                || (idSetExObj >= 201 && idSetExObj <= 212)) {
                            String[] stats = exObjTpl.getStrTemplate().split(",");
                            for (String stat : stats) {
                                String[] val = stat.split("#");
                                String modifi = Integer.parseInt(val[0], 16)
                                        + ";" + Integer.parseInt(val[1], 16)
                                        + ";0";
                                GestorSalida.SEND_SB_SPELL_BOOST(this.player, modifi);
                                this.player.removeObjectClassSpell(Integer.parseInt(val[1], 16));
                            }
                            this.player.removeObjectClass(exObjTpl.getId());
                        }
                        GestorSalida.GAME_SEND_OBJET_MOVE_PACKET(this.player, exObj);
                    }
                    if (this.player.getObjetByPos(Constantes.ITEM_POS_ARME) == null)
                        GestorSalida.GAME_SEND_OT_PACKET(this, -1);

                    //Si objet de panoplie
                    if (exObj.getModelo().getPanoId() > 0)
                        GestorSalida.GAME_SEND_OS_PACKET(this.player, exObj.getModelo().getPanoId());
                } else {
                    ObjetoJuego obj2;
                    //On a un objet similaire
                    if ((obj2 = this.player.getSimilarItem(object)) != null) {
                        if (quantity > object.getCantidad())
                            quantity = object.getCantidad();

                        obj2.setCantidad(obj2.getCantidad() + quantity);
                        GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, obj2);

                        if (object.getCantidad() - quantity > 0)//Si il en reste
                        {
                            object.setCantidad(object.getCantidad() - quantity);
                            GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, object);
                        } else
                        //Sinon on supprime
                        {
                            Mundo.mundo.removeGameObject(object.getId());
                            this.player.removeItem(object.getId());
                            GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this.player, object.getId());
                        }
                    } else
                    //Pas d'objets similaires
                    {
                        if (object.getPosicion() > 16) {
                            int oldPos = object.getPosicion();
                            object.setPosicion(position);
                            GestorSalida.GAME_SEND_OBJET_MOVE_PACKET(this.player, object);

                            if (object.getCantidad() > 1) {
                                if (quantity > object.getCantidad())
                                    quantity = object.getCantidad();

                                if (object.getCantidad() - quantity > 0) {//Si il en reste
                                    ObjetoJuego newItem = ObjetoJuego.getCloneObjet(object, object.getCantidad()
                                            - quantity);
                                    newItem.setPosicion(oldPos);

                                    object.setCantidad(quantity);
                                    GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, object);

                                    if (this.player.addObjet(newItem, false))
                                        Mundo.addGameObject(newItem, true);
                                }
                            }
                        } else {
                            object.setPosicion(position);
                            GestorSalida.GAME_SEND_OBJET_MOVE_PACKET(this.player, object);

                            if (object.getCantidad() > 1) {
                                if (quantity > object.getCantidad())
                                    quantity = object.getCantidad();

                                if (object.getCantidad() - quantity > 0) {//Si il en reste
                                    int newItemQua = object.getCantidad() - quantity;
                                    ObjetoJuego newItem = ObjetoJuego.getCloneObjet(object, newItemQua);
                                    if (this.player.addObjet(newItem, false))
                                        Mundo.addGameObject(newItem, true);
                                    object.setCantidad(quantity);
                                    GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, object);
                                }
                            }
                        }
                    }
                }
                if (position == Constantes.ITEM_POS_ARME) {
                    //Incarnation
                    switch (object.getModelo().getId()) {
// Tourmenteur ténebres
                        case 9544 -> this.player.setFullMorph(1, false, false);
// Tourmenteur feu
                        case 9545 -> this.player.setFullMorph(5, false, false);
// Tourmenteur feuille
                        case 9546 -> this.player.setFullMorph(4, false, false);
// Tourmenteur gthiste
                        case 9547 -> this.player.setFullMorph(3, false, false);
// Tourmenteur terre
                        case 9548 -> this.player.setFullMorph(2, false, false);
// Bandit Archer
                        case 10125 -> this.player.setFullMorph(7, false, false);
// Bandit Fine Lame
                        case 10126 -> this.player.setFullMorph(6, false, false);
// Bandit Baroudeur
                        case 10127 -> this.player.setFullMorph(8, false, false);
// Bandit Ensorcelleur
                        case 10133 -> this.player.setFullMorph(9, false, false);
                    }
                } else {// Tourmenteur ; on d�morphe
                    if (Constantes.isIncarnationWeapon(object.getModelo().getId()))
                        this.player.unsetFullMorph();
                }

                if (object.getModelo().getId() == 2157) {
                    if (position == Constantes.ITEM_POS_COIFFE) {
                        this.player.setGfxId((this.player.getSexe() == 1) ? 8009 : 8006);
                        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.player.getCurMap(), this.player.getId());
                        GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(this.player.getCurMap(), this.player);
                        GestorSalida.GAME_SEND_MESSAGE(this.player, "Vous avez été transformé en mercenaire.");
                    } else if (position == Constantes.ITEM_POS_NO_EQUIPED) {
                        this.player.setGfxId(this.player.getClasse() * 10 + this.player.getSexe());
                        GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.player.getCurMap(), this.player.getId());
                        GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(this.player.getCurMap(), this.player);
                        GestorSalida.GAME_SEND_MESSAGE(this.player, "Vous n'êtes plus mercenaire.");
                    }
                }
                if (object.getModelo().getId() != 2157 && this.player.isMorphMercenaire() && position == Constantes.ITEM_POS_COIFFE) {
                    this.player.setGfxId(this.player.getClasse() * 10 + this.player.getSexe());
                    GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.player.getCurMap(), this.player.getId());
                    GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(this.player.getCurMap(), this.player);
                    GestorSalida.GAME_SEND_MESSAGE(this.player, "Vous n'êtes plus mercenaire.");
                }

                this.player.refreshStats();
                GestorSalida.GAME_SEND_STATS_PACKET(this.player);

                if (this.player.getParty() != null)
                    GestorSalida.GAME_SEND_PM_MOD_PACKET_TO_GROUP(this.player.getParty(), this.player);

                if (position == Constantes.ITEM_POS_ARME || position == Constantes.ITEM_POS_COIFFE || position == Constantes.ITEM_POS_FAMILIER || position == Constantes.ITEM_POS_CAPE || position == Constantes.ITEM_POS_BOUCLIER || position == Constantes.ITEM_POS_NO_EQUIPED)
                    GestorSalida.GAME_SEND_ON_EQUIP_ITEM(this.player.getCurMap(), this.player);

                //Si familier
                if (position == Constantes.ITEM_POS_FAMILIER && this.player.isOnMount())
                    this.player.toogleOnMount();
                //Verif pour les thisils de m�tier
                if (position == Constantes.ITEM_POS_NO_EQUIPED && this.player.getObjetByPos(Constantes.ITEM_POS_ARME) == null)
                    GestorSalida.GAME_SEND_OT_PACKET(this, -1);
                if (position == Constantes.ITEM_POS_ARME && this.player.getObjetByPos(Constantes.ITEM_POS_ARME) != null)
                    this.player.getMetiers().entrySet().stream().filter(e -> e.getValue().getTemplate().isValidTool(this.player.getObjetByPos(Constantes.ITEM_POS_ARME).getModelo().getId())).forEach(e -> GestorSalida.GAME_SEND_OT_PACKET(this, e.getValue().getTemplate().getId()));
                //Si objet de panoplie
                if (object.getModelo().getPanoId() > 0)
                    GestorSalida.GAME_SEND_OS_PACKET(this.player, object.getModelo().getPanoId());
                if (this.player.getPelea() != null)
                    GestorSalida.GAME_SEND_ON_EQUIP_ITEM_FIGHT(this.player, this.player.getPelea().getFighterByPerso(this.player), this.player.getPelea());
            }

            // Start craft secure show/hide
            if (position == Constantes.ITEM_POS_ARME || (position == Constantes.ITEM_POS_NO_EQUIPED && object.getModelo().getPACost() > 0)) {
                this.player.refreshCraftSecure(true);
            }
            // End craft secure show/hide
            if(this.player.getPelea() != null) {
                Peleador target = this.player.getPelea().getFighterByPerso(this.player);
                this.player.getPelea().getFighters(7).stream().filter(fighter -> fighter != null && fighter.getPlayer() != null).forEach(fighter -> fighter.getPlayer().send(this.player.getCurMap().getFighterGMPacket(this.player)));
                target.setPdv(this.player.getCurPdv());
                target.setPdvMax(this.player.getMaxPdv());
                GestorSalida.GAME_SEND_STATS_PACKET(this.player);
            }

            this.player.verifEquiped();
            Database.dinamicos.getPlayerData().update(this.player);
        } catch (Exception e) {
            e.printStackTrace();
            GestorSalida.GAME_SEND_DELETE_OBJECT_FAILED_PACKET(this);
        }
    }

    private void useObject(String packet) {
        int guid = -1;
        int targetGuid = -1;
        short cellID = -1;
        Jugador target = null;
        try {
            String[] infos = packet.substring(2).split("\\|");
            guid = Integer.parseInt(infos[0]);
            try {
                targetGuid = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }
            try {
                cellID = Short.parseShort(infos[2]);
            } catch (Exception e) {
                // ok
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //Si le joueur n'a pas l'objet
        if (Mundo.mundo.getPlayer(targetGuid) != null)
            target = Mundo.mundo.getPlayer(targetGuid);
        if (!this.player.hasItemGuid(guid) || this.player.isAway())
            return;
        if (target != null && target.isAway())
            return;
        ObjetoJuego obj = this.player.getItems().get(guid);
        if (obj == null)
            return;
        ObjetoModelo T = obj.getModelo();
        if (T.getLevel() > this.player.getLevel() || (!obj.getModelo().getConditions().equalsIgnoreCase("") && !Mundo.mundo.getConditionManager().validConditions(this.player, obj.getModelo().getConditions()))) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "119|43");
            return;
        }
        T.applyAction(this.player, target, guid, cellID);
        if (T.getType() == Constantes.ITEM_TYPE_PAIN
                || T.getType() == Constantes.ITEM_TYPE_VIANDE_COMESTIBLE) {
            if (target != null)
                GestorSalida.GAME_SEND_eUK_PACKET_TO_MAP(target.getCurMap(), target.getId(), 17);
            else
                GestorSalida.GAME_SEND_eUK_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), 17);
        } else if (T.getType() == Constantes.ITEM_TYPE_BIERE) {
            if (target != null)
                GestorSalida.GAME_SEND_eUK_PACKET_TO_MAP(target.getCurMap(), target.getId(), 18);
            else
                GestorSalida.GAME_SEND_eUK_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), 18);
        }
    }

    private void dissociateObvi(String packet) {
        int guid = -1;
        int pos = -1;
        try {
            guid = Integer.parseInt(packet.substring(2).split("\\|")[0]);
            pos = Integer.parseInt(packet.split("\\|")[1]);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if ((guid == -1) || (!this.player.hasItemGuid(guid)))
            return;
        ObjetoJuego obj = this.player.getItems().get(guid);
        int idOBVI = Database.dinamicos.getObvejivanData().getAndDelete(obj, true);

        if (idOBVI == -1) {
            switch (obj.getModelo().getType()) {
                case 1 -> idOBVI = 9255;
                case 9 -> idOBVI = 9256;
                case 16 -> idOBVI = 9234;
                case 17 -> idOBVI = 9233;
                default -> {
                    GestorSalida.GAME_SEND_MESSAGE(this.player, "Erreur d'obvijevan numero: 4. Merci de nous le signaler si le probleme est grave.", "000000");
                    return;
                }
            }
        }

        ObjetoModelo t = Mundo.mundo.getObjetoModelo(idOBVI);
        ObjetoJuego obV = t.createNewItem(1, true);
        String obviStats = obj.getObvijevanStatsOnly();
        if (obviStats.equals("")) {
            GestorSalida.GAME_SEND_MESSAGE(this.player, "Erreur d'obvijevan numero: 3. Merci de nous le signaler si le probleme est grave.", "000000");
            return;
        }
        obV.clearStats();
        obV.refreshStatsObjet(obviStats);
        if (this.player.addObjet(obV, true))
            Mundo.addGameObject(obV, true);
        obj.removeAllObvijevanStats();
        GestorSalida.send(this.player, obj.obvijevanOCO_Packet(pos));
        GestorSalida.GAME_SEND_ON_EQUIP_ITEM(this.player.getCurMap(), this.player);
        Database.dinamicos.getPlayerData().update(this.player);
    }

    private void feedObvi(String packet) {
        int guid = -1;
        int pos = -1;
        int victime = -1;
        try {
            guid = Integer.parseInt(packet.substring(2).split("\\|")[0]);
            pos = Integer.parseInt(packet.split("\\|")[1]);
            victime = Integer.parseInt(packet.split("\\|")[2]);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if ((guid == -1) || (!this.player.hasItemGuid(guid)))
            return;
        ObjetoJuego obj = this.player.getItems().get(guid);
        ObjetoJuego objVictime = Mundo.getGameObject(victime);
        obj.obvijevanNourir(objVictime);

        int qua = objVictime.getCantidad();
        if (qua <= 1) {
            this.player.removeItem(objVictime.getId());
            GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this.player, objVictime.getId());
        } else {
            objVictime.setCantidad(qua - 1);
            GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, objVictime);
        }
        GestorSalida.send(this.player, obj.obvijevanOCO_Packet(pos));
        Database.dinamicos.getPlayerData().update(this.player);
    }

    private void setSkinObvi(String packet) {
        int guid = -1;
        int pos = -1;
        int val = -1;
        try {
            guid = Integer.parseInt(packet.substring(2).split("\\|")[0]);
            pos = Integer.parseInt(packet.split("\\|")[1]);
            val = Integer.parseInt(packet.split("\\|")[2]);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if ((guid == -1) || (!this.player.hasItemGuid(guid)))
            return;
        ObjetoJuego obj = this.player.getItems().get(guid);
        if ((val >= 21) || (val <= 0))
            return;

        obj.obvijevanChangeStat(972, val);
        GestorSalida.send(this.player, obj.obvijevanOCO_Packet(pos));
        if (pos != -1)
            GestorSalida.GAME_SEND_ON_EQUIP_ITEM(this.player.getCurMap(), this.player);
    }

    /**
     * Group Packet *
     */
    private void parseGroupPacket(String packet) {
        switch (packet.charAt(1)) {
//Accepter invitation
            case 'A' -> acceptInvitation();
//Suivre membre du groupe PF+GUID
            case 'F' -> followMember(packet);
//Suivez le tous PG+GUID
            case 'G' -> followAllMember(packet);
//inviation
            case 'I' -> inviteParty(packet);
//Refuse
            case 'R' -> refuseInvitation();
//Quitter
            case 'V' -> leaveParty(packet);
//Localisation du groupe
            case 'W' -> whereIsParty();
        }
    }

    private void acceptInvitation() {
        if (this.player == null || this.player.getInvitation() == 0)
            return;

        Jugador target = Mundo.mundo.getPlayer(this.player.getInvitation());

        if (target == null)
            return;

        Grupo party = target.getParty();

        if (party == null) {
            party = new Grupo(target, this.player);
            GestorSalida.GAME_SEND_GROUP_CREATE(this, party);
            GestorSalida.GAME_SEND_PL_PACKET(this, party);
            GestorSalida.GAME_SEND_GROUP_CREATE(target.getGameClient(), party);
            GestorSalida.GAME_SEND_PL_PACKET(target.getGameClient(), party);
            target.setParty(party);
            GestorSalida.GAME_SEND_ALL_PM_ADD_PACKET(target.getGameClient(), party);
        } else {
            GestorSalida.GAME_SEND_GROUP_CREATE(this, party);
            GestorSalida.GAME_SEND_PL_PACKET(this, party);
            GestorSalida.GAME_SEND_PM_ADD_PACKET_TO_GROUP(party, this.player);
            party.addPlayer(this.player);
        }

        this.player.setParty(party);
        GestorSalida.GAME_SEND_ALL_PM_ADD_PACKET(this, party);
        GestorSalida.GAME_SEND_PR_PACKET(target);
    }

    private void followMember(String packet) {
        Grupo g = this.player.getParty();
        if (g == null)
            return;
        int pGuid = -1;
        try {
            pGuid = Integer.parseInt(packet.substring(3));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
        if (pGuid == -1)
            return;
        Jugador P = Mundo.mundo.getPlayer(pGuid);
        if (P == null || !P.isOnline())
            return;
        if (packet.charAt(2) == '+')//Suivre
        {
            if (this.player.follow != null)
                this.player.follow.follower.remove(this.player.getId());
            GestorSalida.GAME_SEND_FLAG_PACKET(this.player, P);
            GestorSalida.GAME_SEND_PF(this.player, "+" + P.getId());
            this.player.follow = P;
            P.follower.put(this.player.getId(), this.player);
            P.send("Im052;" + this.player.getName());
        } else if (packet.charAt(2) == '-')//Ne plus suivre
        {
            GestorSalida.GAME_SEND_DELETE_FLAG_PACKET(this.player);
            GestorSalida.GAME_SEND_PF(this.player, "-");
            this.player.follow = null;
            P.follower.remove(this.player.getId());
            P.send("Im053;" + this.player.getName());
        }
    }

    private void followAllMember(String packet) {
        Grupo g2 = this.player.getParty();
        if (g2 == null)
            return;
        int pGuid2 = -1;
        try {
            pGuid2 = Integer.parseInt(packet.substring(3));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }

        if (pGuid2 == -1)
            return;
        Jugador P2 = Mundo.mundo.getPlayer(pGuid2);
        if (P2 == null || !P2.isOnline())
            return;
        if (packet.charAt(2) == '+')//Suivre
        {
            for (Jugador T : g2.getPlayers()) {
                if (T.getId() == P2.getId())
                    continue;
                if (T.follow != null)
                    T.follow.follower.remove(this.player.getId());
                GestorSalida.GAME_SEND_FLAG_PACKET(T, P2);
                GestorSalida.GAME_SEND_PF(T, "+" + P2.getId());
                T.follow = P2;
                P2.follower.put(T.getId(), T);
                P2.send("Im0178");
            }
        } else if (packet.charAt(2) == '-')//Ne plus suivre
        {
            for (Jugador T : g2.getPlayers()) {
                if (T.getId() == P2.getId())
                    continue;
                GestorSalida.GAME_SEND_DELETE_FLAG_PACKET(T);
                GestorSalida.GAME_SEND_PF(T, "-");
                T.follow = null;
                P2.follower.remove(T.getId());
                P2.send("Im053;" + T.getName());
            }
        }
    }

    private void inviteParty(String packet) {
        if (this.player == null)
            return;

        String name = packet.substring(2);
        Jugador target = Mundo.mundo.getPlayerByName(name);

        if (target == null || !target.isOnline()) {
            GestorSalida.GAME_SEND_GROUP_INVITATION_ERROR(this, "n" + name);
            return;
        }
        if (target.getParty() != null) {
            GestorSalida.GAME_SEND_GROUP_INVITATION_ERROR(this, "a" + name);
            return;
        }
        if (target.getGroupe() != null && this.player.getGroupe() == null) {
            if (!target.getGroupe().isJugador()) {
                GestorSalida.GAME_SEND_MESSAGE(this.player, "Vous n'avez pas la permission d'inviter ce joueur en groupe.");
                return;
            }
        }
        if (this.player.getParty() != null && this.player.getParty().getPlayers().size() == 8) {
            GestorSalida.GAME_SEND_GROUP_INVITATION_ERROR(this, "f");
            return;
        }

        target.setInvitation(this.player.getId());
        this.player.setInvitation(target.getId());
        GestorSalida.GAME_SEND_GROUP_INVITATION(this, this.player.getName(), name);
        GestorSalida.GAME_SEND_GROUP_INVITATION(target.getGameClient(), this.player.getName(), name);
    }

    private void refuseInvitation() {
        if (this.player == null || this.player.getInvitation() == 0)
            return;

        Jugador player = Mundo.mundo.getPlayer(this.player.getInvitation());

        if (player != null) {
            player.setInvitation(0);
            GestorSalida.GAME_SEND_PR_PACKET(player);
        }

        this.player.setInvitation(0);
    }

    private void leaveParty(String packet) {
        if (this.player == null)
            return;
        Grupo g = this.player.getParty();
        if (g == null)
            return;
        if (packet.length() == 2)//Si aucun guid est sp�cifi�, alors c'est que le joueur quitte
        {
            g.leave(this.player);
            GestorSalida.GAME_SEND_PV_PACKET(this, "");
            GestorSalida.GAME_SEND_IH_PACKET(this.player, "");
        } else if (g.isChief(this.player.getId()))//Sinon, c'est qu'il kick un joueur du groupe
        {
            int guid = -1;
            try {
                guid = Integer.parseInt(packet.substring(2));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return;
            }

            if (guid == -1)
                return;
            Jugador t = Mundo.mundo.getPlayer(guid);
            g.leave(t);
            GestorSalida.GAME_SEND_PV_PACKET(t.getGameClient(), ""
                    + this.player.getId());
            GestorSalida.GAME_SEND_IH_PACKET(t, "");
        }
    }

    private void whereIsParty() {
        if (this.player == null)
            return;
        Grupo g = this.player.getParty();
        if (g == null)
            return;
        StringBuilder str = new StringBuilder();
        str.append("");
        boolean isFirst = true;
        for (Jugador GroupP : this.player.getParty().getPlayers()) {
            if (!isFirst)
                str.append("|");
            str.append(GroupP.getCurMap().getX()).append(";").append(GroupP.getCurMap().getY()).append(";").append(GroupP.getCurMap().getId()).append(";").append("2").append(";").append(GroupP.getId()).append(";").append(GroupP.getName());
            isFirst = false;
        }
        GestorSalida.GAME_SEND_IH_PACKET(this.player, str.toString());
    }

    /**
     * MountPark Packet *
     */
    private void parseMountPacket(String packet) {
        switch (packet.charAt(1)) {
//Achat d'un enclos
            case 'b' -> buyMountPark(packet);
//Demande Description en Item.
            case 'd' -> dataMount(packet, true);
//Demande Decription en Enclo.
            case 'p' -> dataMount(packet, false);
//Lib�re la monture
            case 'f' -> killMount(packet);
//Change le nom
            case 'n' -> renameMount(packet.substring(2));
//Monter sur la dinde
            case 'r' -> rideMount();
//Vendre l'enclo
            case 's' -> sellMountPark(packet);
//Fermeture panneau d'achat
            case 'v' -> GestorSalida.GAME_SEND_R_PACKET(this.player, "v");
//Change l'xp donner a la dinde
            case 'x' -> setXpMount(packet);
//Castrer la dinde
            case 'c' -> castrateMount();
// retirer objet de l'etable
            case 'o' -> removeObjectInMountPark(packet);
        }
    }

    private void buyMountPark(String packet) {
        GestorSalida.GAME_SEND_R_PACKET(this.player, "v");//Fermeture du panneau
        Cercados MP = this.player.getCurMap().getMountPark();
        Jugador Seller = Mundo.mundo.getPlayer(MP.getOwner());
        if (MP.getOwner() == -1) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "196");
            return;
        }
        if (MP.getPrice() == 0) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "197");
            return;
        }
        if (this.player.getGuild() == null) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "1135");
            return;
        }
        if (this.player.getGuildMember().getRank() != 1) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "198");
            return;
        }
       /* if((Instant.now().toEpochMilli() - this.player.getGuild().getDate()) <= 1209600000L) {
            this.player.send("Im1103");
            return;
        }*/
        byte enclosMax = (byte) Math.floor(this.player.getGuild().getLvl() / 10);
        byte TotalEncloGuild = (byte) Mundo.mundo.totalMPGuild(this.player.getGuild().getId());
        if (TotalEncloGuild >= enclosMax) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "1103");
            return;
        }
        if (this.player.getKamas() < MP.getPrice()) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "182");
            return;
        }
        long NewKamas = this.player.getKamas() - MP.getPrice();
        this.player.setKamas(NewKamas);
        if (Seller != null) {
            long NewSellerBankKamas = Seller.getBankKamas() + MP.getPrice();
            Seller.setBankKamas(NewSellerBankKamas);
            if (Seller.isOnline()) {
                GestorSalida.GAME_SEND_MESSAGE(this.player, "Vous venez de vendre votre enclos ! "
                        + MP.getPrice() + ".");
            }
        }
        MP.setPrice(0);//On vide le prix
        MP.setOwner(this.player.getId());
        MP.setGuild(this.player.getGuild());
        Database.estaticos.getMountParkData().update(MP);
        Database.dinamicos.getPlayerData().update(this.player);
        //On rafraichit l'enclo
        for (Jugador z : this.player.getCurMap().getPlayers()) {
            GestorSalida.GAME_SEND_Rp_PACKET(z, MP);
        }
    }

    private void dataMount(String packet, boolean b) {
        int id = Integer.parseInt(packet.substring(2).split("\\|")[0]);

        if (id != 0) {
            Montura mount = Mundo.mundo.getMountById((b ? -1 : 1) * id);
            if (mount != null) GestorSalida.GAME_SEND_MOUNT_DESCRIPTION_PACKET(this.player, mount);
        }
    }

    private void killMount(String packet) {
        if (this.player.getMount().getObjects().size() != 0) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "1106");
            return;
        }

        if (this.player.getMount() != null && this.player.isOnMount())
            this.player.toogleOnMount();
        GestorSalida.GAME_SEND_Re_PACKET(this.player, "-", this.player.getMount());
        Database.dinamicos.getMountData().delete(this.player.getMount().getId());
        Mundo.mundo.removeMount(this.player.getMount().getId());
        this.player.setMount(null);
    }

    private void renameMount(String name) {
        if (this.player.getMount() == null)
            return;
        this.player.getMount().setName(name);
        Database.dinamicos.getMountData().update(this.player.getMount());
        GestorSalida.GAME_SEND_Rn_PACKET(this.player, name);
    }

    private void rideMount() {
        if (!this.player.isSubscribe()) {
            GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(this, 'S');
            return;
        }

        this.player.toogleOnMount();
    }

    private void sellMountPark(String packet) {
        GestorSalida.GAME_SEND_R_PACKET(this.player, "v");//Fermeture du panneau
        int price = Integer.parseInt(packet.substring(2));
        Cercados MP1 = this.player.getCurMap().getMountPark();
        if (!MP1.getEtable().isEmpty() || !MP1.getListOfRaising().isEmpty()) {
            GestorSalida.GAME_SEND_MESSAGE(this.player, "Impossible de vendre un enclos plein.");
            return;
        }
        if (MP1.getOwner() == -1) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "194");
            return;
        }
        if (MP1.getOwner() != this.player.getId()) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "195");
            return;
        }
        MP1.setPrice(price);
        Database.estaticos.getMountParkData().update(MP1);
        Database.dinamicos.getPlayerData().update(this.player);
        //On rafraichit l'enclo
        for (Jugador z : this.player.getCurMap().getPlayers()) {
            GestorSalida.GAME_SEND_Rp_PACKET(z, MP1);
        }
    }

    private void setXpMount(String packet) {
        try {
            int xp = Integer.parseInt(packet.substring(2));
            if (xp < 0)
                xp = 0;
            if (xp > 90)
                xp = 90;
            this.player.setMountGiveXp(xp);
            GestorSalida.GAME_SEND_Rx_PACKET(this.player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void castrateMount() {
        if (this.player.getMount() == null) {
            GestorSalida.GAME_SEND_Re_PACKET(this.player, "Er", null);
            return;
        }
        this.player.getMount().setCastrated();
        GestorSalida.GAME_SEND_Re_PACKET(this.player, "+", this.player.getMount());
    }

    private void removeObjectInMountPark(String packet) {
        int cell = Integer.parseInt(packet.substring(2));
        Mapa map = this.player.getCurMap();
        if (map.getMountPark() == null)
            return;
        Cercados MP = map.getMountPark();

        if (this.player.getGuild() == null) {
            GestorSalida.GAME_SEND_BN(this);
            return;
        }
        if (!this.player.getGuildMember().canDo(8192)) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "193");
            return;
        }

        int item = MP.getCellAndObject().get(cell);
        ObjetoModelo t = Mundo.mundo.getObjetoModelo(item);
        ObjetoJuego obj = t.createNewItem(1, false); // creation de l'item au stats incorrecte

        int statNew = 0;// on vas chercher la valeur de la resistance de l'item
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : MP.getObjDurab().entrySet()) {
            if (entry.getKey().equals(cell)) {
                for (Map.Entry<Integer, Integer> entry2 : entry.getValue().entrySet())
                    statNew = entry2.getValue();
            }
        }
        obj.getTxtStat().remove(812); //on retire les stats "32c"
        obj.addTxtStat(812, Integer.toHexString(statNew));// on ajthis les bonnes stats

        if (this.player.addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
            Mundo.addGameObject(obj, true);
        if (MP.delObject(cell))
            GestorSalida.SEND_GDO_PUT_OBJECT_MOUNT(map, cell + ";0;0"); // on retire l'objet de la map
    }

    /**
     * Quest Packet *
     */
    private void parseQuestData(String packet) {
        switch (packet.charAt(1)) {
            case 'L' -> GestorSalida.QuestList(this, this.player);
            case 'S' -> {
                int QuestID = Integer.parseInt(packet.substring(2));
                Mision quest = Mision.getQuestById(QuestID);
                GestorSalida.QuestGep(this, quest, this.player);
            }
        }
    }

    /**
     * Spell Packet *
     */
    private void parseSpellPacket(String packet) {
        switch (packet.charAt(1)) {
            case 'B' -> boostSpell(packet);
//Oublie de sort
            case 'F' -> forgetSpell(packet);
            case 'M' -> moveToUsed(packet);
        }
    }

    private void boostSpell(String packet) {
        try {
            int id = Integer.parseInt(packet.substring(2));
            if (this.player.boostSpell(id)) {
                GestorSalida.GAME_SEND_SPELL_UPGRADE_SUCCED(this, id, this.player.getSortStatBySortIfHas(id).getLevel());
                GestorSalida.GAME_SEND_STATS_PACKET(this.player);
            } else {
                GestorSalida.GAME_SEND_SPELL_UPGRADE_FAILED(this);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            GestorSalida.GAME_SEND_SPELL_UPGRADE_FAILED(this);
        }
    }

    private void forgetSpell(String packet) {
        if (this.player.getExchangeAction() == null || this.player.getExchangeAction().getType() != AccionIntercambiar.FORGETTING_SPELL)
            return;
        int id = Integer.parseInt(packet.substring(2));
        if(id == -1)
            this.player.setExchangeAction(null);
        if (this.player.forgetSpell(id)) {
            GestorSalida.GAME_SEND_SPELL_UPGRADE_SUCCED(this, id, this.player.getSortStatBySortIfHas(id).getLevel());
            GestorSalida.GAME_SEND_STATS_PACKET(this.player);
            this.player.setExchangeAction(null);
        }
    }

    private void moveToUsed(String packet) {
        try {
            int SpellID = Integer.parseInt(packet.substring(2).split("\\|")[0]);
            int Position = Integer.parseInt(packet.substring(2).split("\\|")[1]);
            Hechizo.SortStats Spell = this.player.getSortStatBySortIfHas(SpellID);
            if (Spell != null) {
                this.player.set_SpellPlace(SpellID, Mundo.mundo.getCryptManager().getHashedValueByInt(Position));
            }
            GestorSalida.GAME_SEND_BN(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Waypoint Packet *
     */
    private void parseWaypointPacket(String packet) {
        switch (packet.charAt(1)) {
//Use
            case 'U' -> waypointUse(packet);
//use zaapi
            case 'u' -> zaapiUse(packet);
            case 'p' -> prismUse(packet);
//Quitter
            case 'V' -> waypointLeave();
//quitter zaapi
            case 'v' -> zaapiLeave();
            case 'w' -> prismLeave();
        }
    }

    private void waypointUse(String packet) {
        try {
            final short id = Short.parseShort(packet.substring(2));
            final Grupo party = this.player.getParty();

            if(party != null && this.player.getPelea() == null && party.getMaster() != null && party.getMaster().getName().equals(this.player.getName())) {
                party.getPlayers().stream().filter((follower1) -> party.isWithTheMaster(follower1, false) && follower1.getExchangeAction() == null).forEach(follower -> {
                    follower.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.IN_ZAAPING, null));
                    follower.useZaap(id);
                });
            }

            this.player.useZaap(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void zaapiUse(final String packet) {
        if (this.player.getDeshonor() >= 2) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "183");
            return;
        }
        final Grupo party = this.player.getParty();

        if(party != null && this.player.getPelea() == null && party.getMaster() != null && party.getMaster().getName().equals(this.player.getName())) {
            party.getPlayers().stream().filter((follower1) -> party.isWithTheMaster(follower1, false) && follower1.getExchangeAction() == null).forEach(follower -> {
                follower.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.IN_ZAPPI, null));
                follower.getGameClient().zaapiUse(packet);
            });
        }

        this.player.Zaapi_use(packet);
    }

    private void prismUse(String packet) {
        if (this.player.getDeshonor() >= 2) {
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "183");
            return;
        }
        this.player.usePrisme(packet);
    }

    private void waypointLeave() {
        this.player.stopZaaping();
    }

    private void zaapiLeave() {
        this.player.Zaapi_close();
    }

    private void prismLeave() {
        this.player.Prisme_close();
    }

    private void parseFoireTroll(String packet) {
        if(this.player.getExchangeAction() == null || this.player.getExchangeAction().getType() != AccionIntercambiar.IN_TUTORIAL)
            return;
        String[] param = packet.split("\\|");
        Tutoriales tutorial = (Tutoriales) this.player.getExchangeAction().getValue();
        this.player.setExchangeAction(null);
        if (packet.charAt(1) == 'V') {
            if (packet.charAt(2) != '0' && packet.charAt(2) != '4')
                try {
                    int index = Integer.parseInt(packet.charAt(2) + "") - 1;
                    tutorial.reward.get(index).apply(this.player, null, -1, (short) -1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            try {
                Accion end = tutorial.end;
                if (end != null && this.player != null)
                    end.apply(this.player, null, -1, (short) -1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            assert this.player != null;
            this.player.setAway(false);
            try {
                this.player.set_orientation(Byte.parseByte(param[2]));
                this.player.setCurCell(this.player.getCurMap().getCase(Short.parseShort(param[1])));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //Fin otros

    public void kick() {
        if(this.session.isConnected())
            this.session.closeNow();
    }

    public void disconnect() {
        if (this.account != null && this.player != null)
            this.account.disconnect(this.player);
    }

    public void addAction(AccionJuego GA) {
        actions.put(GA.getId(), GA);
        if (GA.getActionId() == 1)
            walk = true;

        if (Configuracion.INSTANCE.getMostrarenviados()) {
        logger.debug("Juego > Crea la accion ID: " + GA.getId());
        logger.debug("Juego > Paquete: " + GA.getPacket());
        }
    }

    public synchronized void removeAction(AccionJuego GA) {
        if (GA.getActionId() == 1)
            walk = false;
        if (Configuracion.INSTANCE.getMostrarenviados()) {
        logger.debug("Juego >  Elimina la accion ID: " + GA.getId());
        }
        actions.remove(GA.getId());

        if (actions.get(-1) != null && GA.getActionId() == 1)//Si la queue est pas vide
        {
            //et l'actionID remove = Deplacement
            //int cellID = -1;
            String packet = actions.get(-1).getPacket().substring(5);
            int cell = Integer.parseInt(packet.split(";")[0]);
            ArrayList<Integer> list = null;
            try {
                list = Camino.getAllCaseIdAllDirrection(cell, this.player.getCurMap());
                //cellID = Pathfinding.getNearestCellAroundGA(this.player.getCurMap(), cell, this.player.getCurCell().getId(), null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //cellID == this.player.getCurCell().getId()
            if ((list != null && list.contains((int) this.player.getCurCell().getId())) || distPecheur())// et on verrifie si le joueur = cellI
                this.player.getGameClient().gameAction(actions.get(-1));// On renvois comme demande
                //Risqu� mais bon pas le choix si on veut pas �tre emmerder avec les bl�s. Parser le bon type ?
                //this.player.getGameClient().gameAction(actions.getWaitingAccount(-1));// On renvois comme demande
            actions.remove(-1);
        }
    }

    private boolean distPecheur() {
        try {
            String packet = actions.get(-1).getPacket().substring(5);
            OficioCaracteristicas SM = this.player.getMetierBySkill(Integer.parseInt(packet.split(";")[1]));
            if (SM == null)
                return false;
            if (SM.getTemplate() == null)
                return false;
            if (SM.getTemplate().getId() != 36)
                return false;
            int dis = Camino.getDistanceBetween(this.player.getCurMap(), Integer.parseInt(packet.split(";")[0]), this.player.getCurCell().getId());
            int dist = OficioConstantes.getDistCanne(this.player.getObjetByPos(Constantes.ITEM_POS_ARME).getModelo().getId());
            if (dis <= dist)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public void changeName(String packet) {
        if(!this.player.hasItemTemplate(10860, 1)) {
            this.player.send("AlEr");
            this.player.sendMessage("Désolé, vous ne possédez pas la potion.");
            return;
        }

        final String name = packet;
        boolean isValid = true;

        if (name.length() > 20 || name.length() < 3 || name.contains("modo") || name.contains("admin") || name.contains("putain") || name.contains("administrateur") || name.contains("puta"))
            isValid = false;
        if (isValid) {
            int tiretCount = 0;
            char exLetterA = ' ';
            char exLetterB = ' ';
            for (char curLetter : name.toCharArray()) {
                if (!(((curLetter >= 'a' && curLetter <= 'z') || (curLetter >= 'A' && curLetter <= 'Z')) || curLetter == '-')) {
                    isValid = false;
                    break;
                }
                if (curLetter == exLetterA && curLetter == exLetterB) {
                    isValid = false;
                    break;
                }
                if (curLetter >= 'a' && curLetter <= 'z') {
                    exLetterA = exLetterB;
                    exLetterB = curLetter;
                }
                if (curLetter == '-') {
                    if (tiretCount >= 1) {
                        isValid = false;
                        break;
                    } else {
                        tiretCount++;
                    }
                }
            }
        }

        if(Database.dinamicos.getPlayerData().exist(name) || !isValid) {
            this.player.send("AlEs");
            return;
        }

        this.player.setName(name);
        this.player.send("AlEr");
        this.player.removeByTemplateID(10860, 1);
        GestorSalida.GAME_SEND_ALTER_GM_PACKET(this.player.getCurMap(), this.player);
    }

    private void setPingPromedio(String packet)
    {
        String splitPacket=packet.substring(2);
        String[] packetArray=splitPacket.split("\\|");
        this.PingPromedio=Integer.parseInt(packetArray[0]);
    }

    public int getPingPromedio()
    {
        return PingPromedio;
    }

    public void send(String packet) {
        try {
            if (Configuracion.INSTANCE.getENCRYPT_PACKET() && this.preparedKeys != null)
                packet = Mundo.mundo.getCryptManager().cryptMessage(packet, this.preparedKeys);
            this.getSession().write(packet);
        } catch(Exception e) {
            logger.warn("Send fail : " + packet);
            e.printStackTrace();
        }
    }
}
