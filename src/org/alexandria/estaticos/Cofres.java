package org.alexandria.estaticos;

import org.alexandria.estaticos.cliente.Cuenta;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Constantes;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.estaticos.juego.accion.AccionIntercambiar;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.objeto.ObjetoJuego;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Cofres {

    private int id;
    private int houseId;
    private short mapId;
    private int cellId;
    private String key;
    private int ownerId;
    private long kamas;
    private Jugador player = null;
    private Map<Integer, ObjetoJuego> object = new HashMap<>();

    public Cofres(int id, int houseId, short mapId, int cellId) {
        this.id = id;
        this.houseId = houseId;
        this.mapId = mapId;
        this.cellId = cellId;
    }

    public static void closeCode(Jugador P) {
        GestorSalida.GAME_SEND_KODE(P, "V");
    }

    public static Cofres getTrunkIdByCoord(int map_id, int cell_id) {
        for (Entry<Integer, Cofres> trunk : Mundo.mundo.getTrunks().entrySet())
            if (trunk.getValue().getMapId() == map_id && trunk.getValue().getCellId() == cell_id)
                return trunk.getValue();
        return null;
    }

    public static void lock(Jugador P, String packet) {
        Cofres t = (Cofres) P.getExchangeAction().getValue();
        if (t == null)
            return;
        if (t.isTrunk(P, t)) {
            Database.estaticos.getTrunkData().updateCode(P, t, packet); //Change le code
            t.setKey(packet);
            closeCode(P);
        } else {
            closeCode(P);
        }
        P.setExchangeAction(null);
    }

    public static void open(Jugador P, String packet, boolean isTrunk) {//Ouvrir un coffre
        Cofres t = (Cofres) P.getExchangeAction().getValue();
        if (t == null)
            return;
        if (packet.compareTo(t.getKey()) == 0 || isTrunk)//Si c'est chez lui ou que le mot de passe est bon
        {
            t.player = P;
            GestorSalida.GAME_SEND_ECK_PACKET(P.getGameClient(), 5, "");
            GestorSalida.GAME_SEND_EL_TRUNK_PACKET(P, t);
            closeCode(P);
            P.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.IN_TRUNK, t));
        } else if (packet.compareTo(t.getKey()) != 0)//Mauvais code
        {
            GestorSalida.GAME_SEND_KODE(P, "KE");
            closeCode(P);
            P.setExchangeAction(null);
        }
    }

    public static ArrayList<Cofres> getTrunksByHouse(Casas h) {
        ArrayList<Cofres> trunks = Mundo.mundo.getTrunks().values().stream().filter(cofres -> cofres.getHouseId() == h.getId()).collect(Collectors.toCollection(ArrayList::new));
        return trunks;
    }

    public void setObjects(String object) {
        for (String item : object.split("\\|")) {
            if (item.equals(""))
                continue;
            String[] infos = item.split(":");
            int guid = Integer.parseInt(infos[0]);

            ObjetoJuego obj = Mundo.getGameObject(guid);
            if (obj == null)
                continue;
            this.object.put(obj.getId(), obj);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHouseId() {
        return houseId;
    }

    public void setHouseId(int houseId) {
        this.houseId = houseId;
    }

    public short getMapId() {
        return mapId;
    }

    public void setMapId(short mapId) {
        this.mapId = mapId;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public long getKamas() {
        return kamas;
    }

    public void setKamas(long kamas) {
        this.kamas = kamas;
    }

    public Jugador getPlayer() {
        return player;
    }

    public void setPlayer(Jugador player) {
        this.player = player;
    }

    public Map<Integer, ObjetoJuego> getObject() {
        return object;
    }

    public void setObject(Map<Integer, ObjetoJuego> object) {
        this.object = object;
    }

    public void Lock(Jugador P) {
        GestorSalida.GAME_SEND_KODE(P, "CK1|8");
    }

    public void enter(Jugador player) {
        if (player.getPelea() != null || player.getExchangeAction() != null)
            return;

        Casas house = Mundo.mundo.getHouse(getHouseId());

        if(house.getOwnerId() == player.getAccID() && this.getOwnerId() != player.getAccID())
            this.setOwnerId(player.getAccID());
        if (this.getOwnerId() == player.getAccID() ||(player.getGroupe() != null)|| (player.getGuild() != null && player.getGuild().getId() == house.getGuildId() && house.canDo(Constantes.C_GNOCODE))) {
            player.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.IN_TRUNK, this));
            open(player, "-", true);
        } else if (player.getGuild() == null && house.canDo(Constantes.C_OCANTOPEN))
            GestorSalida.GAME_SEND_MESSAGE(player, "Ce coffre ne peut être ouvert que par les membres de la guilde !");
        else if (this.getOwnerId() > 0)
            GestorSalida.GAME_SEND_KODE(player, "CK0|8");
    }

    public boolean isTrunk(Jugador P, Cofres t)//Savoir si c'est son coffre
    {
        return t.getOwnerId() == P.getAccID();
    }

    public String parseToTrunkPacket() {
        StringBuilder packet = new StringBuilder();
        for (ObjetoJuego obj : this.object.values())
            packet.append("O").append(obj.parseItem()).append(";");
        if (getKamas() != 0)
            packet.append("G").append(getKamas());
        return packet.toString();
    }

    public void addInTrunk(int guid, int qua, Jugador P) {
        if (qua <= 0)
            return;
        if (((Cofres) P.getExchangeAction().getValue()).getId() != getId())
            return;
        if (this.object.size() >= 10000) // Le plus grand c'est pour si un admin ajoute des objets via la bdd...
        {
            GestorSalida.GAME_SEND_MESSAGE(P, "Le nombre d'objets maximal de ce coffre à été atteint !");
            return;
        }

        ObjetoJuego PersoObj = Mundo.getGameObject(guid);
        if (PersoObj == null)
            return;
        if(PersoObj.isAttach()) return;
        //Si le joueur n'a pas l'item dans son sac ...
        if (P.getItems().get(guid) == null)
            return;
        StringBuilder str = new StringBuilder();
        str.append("");

        //Si c'est un item �quip� ...
        if (PersoObj.getPosicion() != Constantes.ITEM_POS_NO_EQUIPED)
            return;

        ObjetoJuego TrunkObj = getSimilarTrunkItem(PersoObj);
        int newQua = PersoObj.getCantidad() - qua;
        if (TrunkObj == null)//S'il n'y pas d'item du meme Template
        {
            //S'il ne reste pas d'item dans le sac
            if (newQua <= 0) {
                //On enleve l'objet du sac du joueur
                P.removeItem(PersoObj.getId());
                //On met l'objet du sac dans le coffre, avec la meme quantit�
                this.object.put(PersoObj.getId(), PersoObj);
                str.append("O").append("+").append(PersoObj.getId()).append("|").append(PersoObj.getCantidad())
                   .append("|").append(PersoObj.getModelo().getId()).append("|")
                   .append(PersoObj.parseStatsString());
                GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(P, guid);
            } else
            //S'il reste des objets au joueur
            {
                //on modifie la quantit� d'item du sac
                PersoObj.setCantidad(newQua);
                //On ajoute l'objet au coffre et au monde
                TrunkObj = ObjetoJuego.getCloneObjet(PersoObj, qua);
                Mundo.addGameObject(TrunkObj, true);
                this.object.put(TrunkObj.getId(), TrunkObj);
                //Envoie des packets
                str.append("O").append("+").append(TrunkObj.getId()).append("|").append(TrunkObj.getCantidad())
                        .append("|").append(TrunkObj.getModelo().getId()).append("|")
                        .append(TrunkObj.parseStatsString());
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P, PersoObj);
            }
        } else
        // S'il y avait un item du meme template
        {
            //S'il ne reste pas d'item dans le sac
            if (newQua <= 0) {
                //On enleve l'objet du sac du joueur
                P.removeItem(PersoObj.getId());
                //On enleve l'objet du monde
                Mundo.mundo.removeGameObject(PersoObj.getId());
                //On ajoute la quantit� a l'objet dans le coffre
                TrunkObj.setCantidad(TrunkObj.getCantidad()
                        + PersoObj.getCantidad());
                //on envoie l'ajout au coffre de l'objet
                str.append("O").append("+").append(TrunkObj.getId()).append("|").append(TrunkObj.getCantidad())
                        .append("|").append(TrunkObj.getModelo().getId()).append("|")
                        .append(TrunkObj.parseStatsString());
                //on envoie la supression de l'objet du sac au joueur
                GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(P, guid);
            } else
            //S'il restait des objets
            {
                //on modifie la quantit� d'item du sac
                PersoObj.setCantidad(newQua);
                TrunkObj.setCantidad(TrunkObj.getCantidad() + qua);
                str.append("O").append("+").append(TrunkObj.getId()).append("|").append(TrunkObj.getCantidad())
                        .append("|").append(TrunkObj.getModelo().getId()).append("|")
                        .append(TrunkObj.parseStatsString());
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P, PersoObj);
            }
        }

        for (Jugador perso : P.getCurMap().getPlayers())
            if (perso.getExchangeAction() != null && perso.getExchangeAction().getType() == AccionIntercambiar.IN_TRUNK && getId() == ((Cofres) perso.getExchangeAction().getValue()).getId())
                GestorSalida.GAME_SEND_EsK_PACKET(perso, str.toString());

        GestorSalida.GAME_SEND_Ow_PACKET(P);
        Database.estaticos.getTrunkData().update(this);
        Database.dinamicos.getPlayerData().update(P);
    }

    public void removeFromTrunk(int guid, int qua, Jugador P) {
        if (qua <= 0)
            return;
        if (((Cofres) P.getExchangeAction().getValue()).getId() != getId())
            return;
        ObjetoJuego TrunkObj = Mundo.getGameObject(guid);
        if (TrunkObj == null)
            return;
        //Si le joueur n'a pas l'item dans son coffre

        if (this.object.get(guid) == null)
            return;

        ObjetoJuego PersoObj = P.getSimilarItem(TrunkObj);
        String str = "";
        int newQua = TrunkObj.getCantidad() - qua;

        if (PersoObj == null)//Si le joueur n'avait aucun item similaire
        {
            //S'il ne reste rien dans le coffre
            if (newQua <= 0) {
                //On retire l'item du coffre

                this.object.remove(guid);
                //On l'ajoute au joueur
                P.getItems().put(guid, TrunkObj);

                //On envoie les packets
                GestorSalida.GAME_SEND_OAKO_PACKET(P, TrunkObj);
                str = "O-" + guid;
            } else
            //S'il reste des objets dans le coffre
            {
                //On cr�e une copy de l'item dans le coffre
                PersoObj = ObjetoJuego.getCloneObjet(TrunkObj, qua);
                //On l'ajoute au monde
                Mundo.addGameObject(PersoObj, true);
                //On retire X objet du coffre
                TrunkObj.setCantidad(newQua);
                //On l'ajoute au joueur
                P.getItems().put(PersoObj.getId(), PersoObj);

                //On envoie les packets
                GestorSalida.GAME_SEND_OAKO_PACKET(P, PersoObj);
                str = "O+" + TrunkObj.getId() + "|" + TrunkObj.getCantidad()
                        + "|" + TrunkObj.getModelo().getId() + "|"
                        + TrunkObj.parseStatsString();
            }
        } else {
            //S'il ne reste rien dans le coffre
            if (newQua <= 0) {
                //On retire l'item du coffre

                this.object.remove(TrunkObj.getId());

                Mundo.mundo.removeGameObject(TrunkObj.getId());
                //On Modifie la quantit� de l'item du sac du joueur
                PersoObj.setCantidad(PersoObj.getCantidad()
                        + TrunkObj.getCantidad());
                //On envoie les packets
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P, PersoObj);
                str = "O-" + guid;
            } else
            //S'il reste des objets dans le coffre
            {
                //On retire X objet du coffre
                TrunkObj.setCantidad(newQua);
                //On ajoute X objets au joueurs
                PersoObj.setCantidad(PersoObj.getCantidad() + qua);
                //On envoie les packets
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P, PersoObj);
                str = "O+" + TrunkObj.getId() + "|" + TrunkObj.getCantidad()
                        + "|" + TrunkObj.getModelo().getId() + "|"
                        + TrunkObj.parseStatsString();
            }
        }

        for (Jugador perso : P.getCurMap().getPlayers())
            if (perso.getExchangeAction() != null && perso.getExchangeAction().getType() == AccionIntercambiar.IN_TRUNK && getId() == ((Cofres) perso.getExchangeAction().getValue()).getId())
                GestorSalida.GAME_SEND_EsK_PACKET(perso, str);

        GestorSalida.GAME_SEND_Ow_PACKET(P);
        Database.estaticos.getTrunkData().update(this);
        Database.dinamicos.getPlayerData().update(P);
    }

    private ObjetoJuego getSimilarTrunkItem(ObjetoJuego obj) {
        for (ObjetoJuego object : this.object.values())
            if(Mundo.mundo.getConditionManager().stackIfSimilar(object, obj, true))
                return object;
        return null;
    }

    public String parseTrunkObjetsToDB() {
        StringBuilder str = new StringBuilder();
        for (Entry<Integer, ObjetoJuego> entry : this.object.entrySet()) {
            ObjetoJuego obj = entry.getValue();
            str.append(obj.getId()).append("|");
        }
        return str.toString();
    }

    public void moveTrunkToBank(Cuenta Cbank) {
        for (Entry<Integer, ObjetoJuego> obj : this.object.entrySet())
            Cbank.getBanco().add(obj.getValue());
        this.object.clear();
        Database.estaticos.getTrunkData().update(this);
        Database.estaticos.getBankData().update(Cbank);
    }
}