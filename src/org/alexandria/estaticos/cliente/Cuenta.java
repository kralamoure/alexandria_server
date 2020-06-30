package org.alexandria.estaticos.cliente;

import org.alexandria.estaticos.comandos.administracion.GrupoADM;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.estaticos.juego.JuegoCliente;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.Mercadillo.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Cuenta {
    private int id;
    private String name;
    private final String pseudo;
    private final String answer;
    private String currentIp = "";
    private String lastIP = "";
    private String lastConnectionDate = "";
    private int points;
    private long muteTime = 0;
    private String mutePseudo = "";
    private boolean banned = false;
    private long subscriber = 1;
    private long bankKamas = 0;
    private Jugador currentPlayer;
    private JuegoCliente gameClient;
    private byte state;
    private String lastVoteIP;
    private long heureVote;
    private final List<ObjetoJuego> bank = new ArrayList<>();
    private final List<Integer> friends = new ArrayList<>();
    private final List<Integer> enemys = new ArrayList<>();
    private final Map<Integer, ArrayList<MercadilloEntrada>> hdvsItems;

    public Cuenta(int guid, String name, String pseudo,
                  String answer, boolean banned,
                  String lastIp, String lastConnectionDate, String friends,
                  String enemy, int points, long subscriber, long muteTime, String mutePseudo,
                  String lastVoteIP, String heureVote) {
        this.id = guid;
        this.name = name;
        this.pseudo = pseudo;
        this.answer = answer;
        this.banned = banned;
        this.lastIP = lastIp;
        this.lastConnectionDate = lastConnectionDate;
        this.hdvsItems = Mundo.mundo.getMyItems(guid);
        this.points = points;
        this.subscriber = subscriber;
        this.muteTime = muteTime;
        this.mutePseudo = mutePseudo;
        this.lastVoteIP = lastVoteIP;

        if (heureVote.equalsIgnoreCase("")) this.heureVote = 0;
        else this.heureVote = Long.parseLong(heureVote);

        //Chargement de la liste d'amie
        if(!friends.equalsIgnoreCase("")) {
            for (String f : friends.split(";")) {
                try {
                    this.friends.add(Integer.parseInt(f));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //Chargement de la liste d'Enemy
        if (!enemy.equalsIgnoreCase("")) {
            for (String e : enemy.split(";")) {
                try {
                    this.enemys.add(Integer.parseInt(e));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        //Chargement de la banque
        String bank = Database.estaticos.getBankData().get(guid);
        if (bank == null) {
            Database.estaticos.getBankData().add(guid);
        } else {
            this.bankKamas = Integer.parseInt(bank.split("@")[0]);
            String allItem = "";
            try {
                allItem = bank.split("@")[1];
            } catch (Exception ignored) {
            }
            if (!allItem.equals("")) {
                for (String item : allItem.split("\\|")) {
                    if (!item.equals("")) {
                        ObjetoJuego obj = Mundo.getGameObject(Integer.parseInt(item));
                        if (obj != null)
                            this.bank.add(obj);
                    }
                }
            }
        }
        if (!Database.estaticos.getGiftData().existByAccount(guid))
            Database.estaticos.getGiftData().create(guid);
    }

    public long getHeureVote() {
        return this.heureVote;
    }

    public String getLastVoteIP() {
        return this.lastVoteIP;
    }

    public int getId() {
        return id;
    }

    public void setId(int i) {
        id = i;
    }

    public String getName() {
        return name;
    }

    public void setName(String i) {
        name = i;
    }

    public String getPseudo() {
        return pseudo;
    }

    public String getAnswer() {
        return answer;
    }

    public String getCurrentIp() {
        return currentIp;
    }

    public void setCurrentIp(String i) {
        currentIp = i;
    }

    public String getLastIP() {
        return lastIP;
    }

    public void setLastIP(String i) {
        lastIP = i;
    }

    public String getLastConnectionDate() {
        return lastConnectionDate;
    }

    public void setLastConnectionDate(String i) {
        lastConnectionDate = i;
    }

    public int getPoints() {
        points = Database.dinamicos.getAccountData().loadPoints(name);
        return points;
    }

    public void setPoints(int i) {
        points = i;
        Database.dinamicos.getAccountData().updatePoints(id, points);
    }

    public void addPoints(int i) {
        this.getPoints();
        points += i;
        Database.dinamicos.getAccountData().updatePoints(id, points);
    }

    public void mute(short minutes, String pseudo) {
        if (minutes <= 0) return;
        muteTime = Instant.now().toEpochMilli() + minutes * 60000;
        mutePseudo = pseudo;
        Database.dinamicos.getAccountData().update(this);
        if (this.currentPlayer != null) this.currentPlayer.send("Im117;" + pseudo + "~" + minutes);
    }

    public void unMute() {
        if (muteTime == 0) return;
        muteTime = 0;
        mutePseudo = "";
        Database.dinamicos.getAccountData().update(this);
    }

    public boolean isMuted() {
        if (muteTime == 0)
            return false;
        if (muteTime >= Instant.now().toEpochMilli())
            return true;
        muteTime = 0;
        mutePseudo = "";
        Database.dinamicos.getAccountData().update(this);
        return false;
    }

    public long getMuteTime() {
        if (!isMuted())
            return 0;
        return muteTime;
    }

    public String getMutePseudo() {
        if (!isMuted())
            return "";
        return mutePseudo;
    }

    public List<ObjetoJuego> getBanco() {
        return bank;
    }

    public String pasar_objeto_al_banco() {
        StringBuilder str = new StringBuilder();
        if (this.bank.isEmpty())
            return "";
        for (ObjetoJuego gameObject : this.bank)
            str.append(gameObject.getId()).append("|");
        return str.toString();
    }

    public long getBankKamas() {
        return this.bankKamas;
    }

    public void setBankKamas(long i) {
        this.bankKamas = i;
        Database.estaticos.getBankData().update(this);
    }

    public JuegoCliente getGameClient() {
        return this.gameClient;
    }

    public void setGameClient(JuegoCliente t) {
        this.gameClient = t;
    }

    public Map<Integer, Jugador> getPlayers() {
        Map<Integer, Jugador> players = new HashMap<>();
        new CopyOnWriteArrayList<>(Mundo.mundo.getJugador()).stream().filter(Objects::nonNull).filter(player -> player.getAccount() != null)
                .filter(player -> player.getAccount().getId() == this.getId()).forEach(player -> {
            if (player.getGameClient() == null)
                player.setAccount(this);
            players.put(player.getId(), player);
        });
        return players;
    }

    public Jugador getCurrentPlayer() {
        return this.currentPlayer;
    }

    public void setCurrentPlayer(Jugador player) {
        this.currentPlayer = player;
    }

    public boolean isBanned() {
        return this.banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public boolean isOnline() {
        return this.gameClient != null;
    }

    public void setState(int state) {
        this.state = (byte) state;
        Database.dinamicos.getAccountData().update(this);
    }

    public byte getState() {
        return state;
    }

    public void setSubscribe() {
        this.subscriber = Database.dinamicos.getAccountData().getSubscribe(this.id);
    }

    public long getSubscribeRemaining() {
        if (!Configuracion.INSTANCE.getSubscription())
            return 525600L;
        long remaining = this.subscriber - Instant.now().toEpochMilli();
        return Math.max(remaining, 0L);
    }

    public boolean isSubscribe() {
        if (!Configuracion.INSTANCE.getSubscription())
            return true;
        long remaining = this.subscriber - Instant.now().toEpochMilli();
        return remaining > 0L;
    }

    public boolean isSubscribeWithoutCondition() {
        long remaining = this.subscriber - Instant.now().toEpochMilli();
        return remaining > 0L;
    }

    public boolean createPlayer(String name, int sexe, int classe, int color1, int color2, int color3) {
        Jugador perso = Jugador.CREATE_PERSONNAGE(name, sexe, classe, color1, color2, color3, this);
        return perso != null;
    }

    public void deletePlayer(int guid) {
        if (this.getPlayers().containsKey(guid))
            Mundo.mundo.removePlayer(this.getPlayers().get(guid));
    }

    public void sendOnline() {
        for (int id : this.friends) {
            Jugador player = Mundo.mundo.getPlayer(id);
            if (player != null && player.is_showFriendConnection() && player.isOnline() && player.getAccount().isFriendWith(this.id))
                GestorSalida.GAME_SEND_FRIEND_ONLINE(this.currentPlayer, player);
        }
    }

    public void addFriend(int id) {
        if (this.id == id) {
            GestorSalida.GAME_SEND_FA_PACKET(this.currentPlayer, "Ey");
            return;
        }

        Cuenta account = Mundo.mundo.getAccount(id);

        if (account == null) {
            GestorSalida.GAME_SEND_MESSAGE(this.currentPlayer, "Le compte n'existe pas.");
            return;
        }

        Jugador player = account.getCurrentPlayer(); // Il est arriv� que le personnage soit null alors que ... non !

        if (player == null) {
            GestorSalida.GAME_SEND_MESSAGE(this.currentPlayer, "Le joueur n'existe pas.");
            return;
        }

        GrupoADM group = player.getGroupe();

        if (group != null && !group.isJugador()) {
            GestorSalida.GAME_SEND_MESSAGE(this.currentPlayer, "Impossible d'ajouter un membre du staff en ami.");
            return;
        }
        if (!this.friends.contains(id)) {
            this.friends.add(id);
            GestorSalida.GAME_SEND_FA_PACKET(this.currentPlayer, "K" + account.getPseudo() + player.parseToFriendList(id));
            Database.dinamicos.getAccountData().update(this);
        } else {
            GestorSalida.GAME_SEND_FA_PACKET(this.currentPlayer, "Ea");
        }
    }

    public void removeFriend(int id) {
        if (this.friends.contains(id)) {
            this.friends.removeIf(integer -> integer == id);
            Database.dinamicos.getAccountData().update(this);
        }
        GestorSalida.GAME_SEND_FD_PACKET(this.currentPlayer, "K");
    }

    public boolean isFriendWith(int id) {
        return friends.contains(id);
    }

    public String parseFriendListToDB() {
        StringBuilder str = new StringBuilder();
        for (int i : this.friends) {
            if (!str.toString().equalsIgnoreCase(""))
                str.append(";");
            str.append(i);
        }
        return str.toString();
    }

    public String parseFriendList() {
        StringBuilder str = new StringBuilder();
        if (this.friends.isEmpty())
            return "";
        for (int i : this.friends) {
            Cuenta C = Mundo.mundo.getAccount(i);
            if (C == null)
                continue;
            str.append("|").append(C.getPseudo());
            //on s'arrete la si aucun perso n'est connect�
            if (!C.isOnline())
                continue;
            Jugador P = C.getCurrentPlayer();
            if (P == null)
                continue;
            str.append(P.parseToFriendList(id));
        }
        return str.toString();
    }

    public void addEnemy(String packet, int guid) {
        if (this.id == guid) {
            GestorSalida.GAME_SEND_FA_PACKET(this.currentPlayer, "Ey");
            return;
        }
        if (!this.enemys.contains(guid)) {
            this.enemys.add(guid);
            Jugador Pr = Mundo.mundo.getPlayerByName(packet);
            GestorSalida.GAME_SEND_ADD_ENEMY(this.currentPlayer, Pr);
            Database.dinamicos.getAccountData().update(this);
        } else
            GestorSalida.GAME_SEND_iAEA_PACKET(this.currentPlayer);
    }

    public void removeEnemy(int id) {
        if (this.enemys.contains(id)) {
            this.enemys.removeIf(integer -> integer == id);
            Database.dinamicos.getAccountData().update(this);
        }
        GestorSalida.GAME_SEND_iD_COMMANDE(this.currentPlayer, "K");
    }

    public boolean isEnemyWith(int id) {
        return enemys.contains(id);
    }

    public String parseEnemyListToDB() {
        StringBuilder str = new StringBuilder();
        for (int i : this.enemys) {
            if (!str.toString().equalsIgnoreCase(""))
                str.append(";");
            str.append(i);
        }
        return str.toString();
    }

    public String parseEnemyList() {
        StringBuilder str = new StringBuilder();
        if (this.enemys.isEmpty())
            return "";
        for (int i : this.enemys) {
            Cuenta C = Mundo.mundo.getAccount(i);
            if (C == null)
                continue;
            str.append("|").append(C.getPseudo());
            //on s'arrete la si aucun perso n'est connect�
            if (!C.isOnline())
                continue;
            Jugador P = C.getCurrentPlayer();
            if (P == null)
                continue;
            str.append(P.parseToEnemyList(id));
        }
        return str.toString();
    }

    public void recoverItem(int lineId) {
        if (this.currentPlayer == null || this.currentPlayer.getExchangeAction() == null)
            return;
        if ((Integer) this.currentPlayer.getExchangeAction().getValue() >= 0)
            return;

        int hdvID = Math.abs((Integer) this.currentPlayer.getExchangeAction().getValue());//R�cup�re l'ID de l'HDV

        MercadilloEntrada entry = null;
        try {
            ArrayList<MercadilloEntrada> entries = this.hdvsItems.get(hdvID);
            if (entries == null || entries.isEmpty())
                return;
            for (MercadilloEntrada tempEntry : entries) {//Boucle dans la liste d'entry de l'HDV pour trouver un entry avec le meme cheapestID que sp�cifi�
                if (tempEntry.getLineId() == lineId) {//Si la boucle trouve un objet avec le meme cheapestID, arrete la boucle
                    entry = tempEntry;
                    break;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return;
        }

        if (entry == null)//Si entry == null cela veut dire que la boucle s'est effectu� sans trouver d'item avec le meme cheapestID
            return;
        if(entry.buy)
            return;

        this.hdvsItems.get(hdvID).remove(entry);//Retire l'item de la liste des objets a vendre du compte
        ObjetoJuego obj = entry.getGameObject();

        if (this.currentPlayer.addObjetSimiler(obj, true, -1)) {
            Mundo.mundo.removeGameObject(obj.getId());
        } else {
            this.currentPlayer.addObjet(obj);
        }
        Database.estaticos.getHdvObjectData().delete(entry.getGameObject().getId());
        Mundo.mundo.getHdv(hdvID).delEntry(entry);//Retire l'item de l'HDV

        Database.dinamicos.getPlayerData().update(this.currentPlayer);
    }

    public MercadilloEntrada[] getHdvEntries(int id) {
        if (this.hdvsItems.get(id) == null) return new MercadilloEntrada[1];
        MercadilloEntrada[] entries = new MercadilloEntrada[this.hdvsItems.get(id).size()];

        for (int i = 0; i < this.hdvsItems.get(id).size(); i++)
            entries[i] = this.hdvsItems.get(id).get(i);
        return entries;
    }

    public int countHdvEntries(int id) {
        ArrayList<MercadilloEntrada> hdvEntry = this.hdvsItems.get(id);
        return hdvEntry == null ? 0 : hdvEntry.size();
    }

    public void resetAllChars() {
        for (Jugador player : this.getPlayers().values()) {
            if (player.getPelea() != null) {
                if (player.getParty() != null)
                    player.getParty().leave(player);
                player.setOnline(true);
            }

            if (player.getExchangeAction() != null)
                JuegoCliente.leaveExchange(player);
            if (player.getParty() != null)
                player.getParty().leave(player);
            if (player.getCurCell() != null)
                player.getCurCell().removePlayer(player);
            if (player.getCurMap() != null && player.isOnline())
                GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(), player.getId());

            player.setOnline(false);
        }
    }

    public void disconnect(Jugador player) {
        Database.dinamicos.getAccountData().setLogged(this.getId(), 0);
        Database.dinamicos.getPlayerData().updateAllLogged(this.getId(), 0);
        Database.dinamicos.getPlayerData().update(player);

        if (player.getExchangeAction() != null)
            JuegoCliente.leaveExchange(player);
        if (player.getParty() != null)
            player.getParty().leave(player);
        if (player.getMount() != null)
            Database.dinamicos.getMountData().update(player.getMount());
        if (player.getPelea() != null) {
            if (player.getPelea().playerDisconnect(player, false)) {
                Database.dinamicos.getPlayerData().update(player);
                return;
            }
        }
        this.currentPlayer = null;
        this.gameClient = null;
        this.currentIp = "";

        for (Jugador character : this.getPlayers().values())
            Database.dinamicos.getPlayerData().update(character);

        player.resetVars();
        this.resetAllChars();
        Database.dinamicos.getAccountData().update(this);
        if (Configuracion.INSTANCE.getMostrarenviados()) {
        Mundo.mundo.logger.info("El jugador " + player.getName() + " se ha desconectado.");
        }
    }

    public void updateVote(String hour, String ip) {
        if (hour.equalsIgnoreCase("")) this.heureVote = 0;
        else this.heureVote = Long.parseLong(hour);
        this.lastVoteIP = ip;
    }
}