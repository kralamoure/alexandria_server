package org.alexandria.estaticos;

import org.alexandria.estaticos.cliente.Cuenta;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Constantes;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.estaticos.juego.accion.AccionIntercambiar;
import org.alexandria.estaticos.juego.mundo.Mundo;

import java.util.Map;
import java.util.TreeMap;

public class Casas {
    private final int id;
    private final short mapId;
    private final int cellId;
    private int ownerId;
    private int sale;
    private int guildId;
    private int guildRights;
    private int access;
    private String key;
    private final int houseMapId;
    private final int houseCellId;
    //Droits de chaques maisons
    private final Map<Integer, Boolean> haveRight = new TreeMap<>();

    public Casas(int id, short mapId, int cellId, int houseMapId, int houseCellId) {
        this.id = id;
        this.mapId = mapId;
        this.cellId = cellId;
        this.houseMapId = houseMapId;
        this.houseCellId = houseCellId;
    }

    public void open(Jugador P, String packet, boolean isHome)//Ouvrir une maison ;o
    {
        if ((!this.canDo(Constantes.H_OCANTOPEN) && (packet.compareTo(this.getKey()) == 0))
                || isHome)//Si c'est chez lui ou que le mot de passe est bon
        {
            P.teleport((short) this.getHouseMapId(), this.getHouseCellId());
            Mundo.mundo.getHouseManager().closeCode(P);
        } else if ((packet.compareTo(this.getKey()) != 0)
                || this.canDo(Constantes.H_OCANTOPEN))//Mauvais code
        {
            GestorSalida.GAME_SEND_KODE(P, "KE");
            GestorSalida.GAME_SEND_KODE(P, "V");
        }
    }
    public void setGuildRightsWithParse(int guildRights) {
        this.guildRights = guildRights;
        parseIntToRight(guildRights);
    }

    public int getId() {
        return this.id;
    }

    public short getMapId() {
        return this.mapId;
    }

    public int getCellId() {
        return this.cellId;
    }

    public int getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(int id) {
        this.ownerId = id;
    }

    public int getSale() {
        return this.sale;
    }

    public void setSale(int price) {
        this.sale = price;
    }

    public int getGuildId() {
        return this.guildId;
    }

    public void setGuildId(int guildId) {
        this.guildId = guildId;
    }

    public int getGuildRights() {
        return this.guildRights;
    }

    public void setGuildRights(int guildRights) {
        this.guildRights = guildRights;
    }

    public int getAccess() {
        return this.access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getHouseMapId() {
        return this.houseMapId;
    }

    public int getHouseCellId() {
        return this.houseCellId;
    }

    public void enter(Jugador P) {//Entrer dans la maison
        if (P.getPelea() != null || P.getExchangeAction() != null)
            return;
        if (this.getOwnerId() == P.getAccID() || (P.getGroupe() != null) || (P.getGuild() != null && P.getGuild().getId() == this.getGuildId() && canDo(Constantes.H_GNOCODE)))//C'est sa maison ou m�me guilde + droits entrer sans pass
            open(P, "-", true);
        else if (this.getOwnerId() > 0) //Une personne autre la acheter, il faut le code pour rentrer
            GestorSalida.GAME_SEND_KODE(P, "CK0|8");//8 �tant le nombre de chiffre du code
        else if (this.getOwnerId() == 0)//Maison non acheter, mais achetable, on peut rentrer sans code
            open(P, "-", false);
    }

    public void ComprarCasa(Jugador P)//Comprar una casa
    {
        Casas h = P.getInHouse();
        GestorSalida.GAME_SEND_hOUSE(P, "CK" + h.getId() + "|" + h.getSale() //ID + Precio
        );
    }

    public void VenderCasa(Jugador P)//Vender una casa
    {
        Casas h = P.getInHouse();
        if (isHouse(P, h)) {
            GestorSalida.GAME_SEND_hOUSE(P, "CK" + h.getId() + "|" + h.getSale() //ID + Precio
            );
        }
    }

    public boolean isHouse(Jugador P, Casas h)//Savoir si c'est sa maison
    {
        return h.getOwnerId() == P.getAccID();
    }

    public void lock(Jugador P) {
        P.setExchangeAction(new AccionIntercambiar<Casas>(AccionIntercambiar.LOCK_HOUSE, this));
        GestorSalida.GAME_SEND_KODE(P, "CK1|8");
    }

    public boolean canDo(int rightValue) {
        return haveRight.get(rightValue);
    }

    private void initRight() {
        haveRight.put(Constantes.H_GBLASON, false);
        haveRight.put(Constantes.H_OBLASON, false);
        haveRight.put(Constantes.H_GNOCODE, false);
        haveRight.put(Constantes.H_OCANTOPEN, false);
        haveRight.put(Constantes.C_GNOCODE, false);
        haveRight.put(Constantes.C_OCANTOPEN, false);
        haveRight.put(Constantes.H_GREPOS, false);
        haveRight.put(Constantes.H_GTELE, false);
    }

    public void parseIntToRight(int total) {
        if (haveRight.isEmpty()) {
            initRight();
        }
        if (total == 1)
            return;

        if (haveRight.size() > 0) //Si les droits contiennent quelque chose -> Vidage (M�me si le HashMap supprimerais les entr�es doublon lors de l'ajout)
            haveRight.clear();

        initRight(); //Remplissage des droits

        Integer[] mapKey = this.haveRight.keySet().toArray(new Integer[this.haveRight.size()]);
        block0 : while (total > 0) {
            for (int i = this.haveRight.size() - 1; i < this.haveRight.size(); --i) {
                int map = mapKey[i];
                if (map > total) continue;
                total ^= map;
                this.haveRight.put(map, true);
                continue block0;
            }
        }
    }


public static class GestorCasas {

        public Casas getHouseIdByCoord(int map_id, int cell_id) {
            for (Map.Entry<Integer, Casas> house : Mundo.mundo.getHouses().entrySet()){
                if (house.getValue().getMapId() != map_id || house.getValue().getCellId() != cell_id) continue;
                    return house.getValue();
            }
            return null;
        }

        public void load(Jugador player, int newMapID) {
            Mundo.mundo.getHouses().entrySet().stream().filter(house -> house.getValue().getMapId() == newMapID).forEach(house -> {
                StringBuilder packet = new StringBuilder();
                packet.append("P").append(house.getValue().getId()).append("|");
                if (house.getValue().getOwnerId() > 0) {
                    Cuenta C = Mundo.mundo.getAccount(house.getValue().getOwnerId());
                    if (C == null)//Ne devrait pas arriver
                        packet.append("undefined;");
                    else
                        packet.append(Mundo.mundo.getAccount(house.getValue().getOwnerId()).getPseudo()).append(";");
                } else {
                    packet.append(";");
                }

                if (house.getValue().getSale() > 0)//Si prix > 0
                    packet.append("1");//Achetable
                else
                    packet.append("0");//Non achetable

                if (house.getValue().getGuildId() > 0) //Maison de guilde
                {
                    Gremio G = Mundo.mundo.getGuild(house.getValue().getGuildId());
                    if (G != null) {
                        String Gname = G.getName();
                        String Gemblem = G.getEmblem();
                        if (G.getPlayers().size() < 10 && G.getId() > 2)//Ce n'est plus une maison de guilde
                        {
                            Database.estaticos.getHouseData().updateGuild(house.getValue(), 0, 0);
                        } else {
                            //Affiche le blason pour les membre de guilde OU Affiche le blason pour les non membre de guilde
                            if (player.getGuild() != null
                                    && player.getGuild().getId() == house.getValue().getGuildId()
                                    && house.getValue().canDo(Constantes.H_GBLASON))//meme guilde
                            {
                                packet.append(";").append(Gname).append(";").append(Gemblem);
                            } else if (house.getValue().canDo(Constantes.H_OBLASON))//Pas de guilde/guilde-diff�rente
                            {
                                packet.append(";").append(Gname).append(";").append(Gemblem);
                            }
                        }
                    }
                }
                GestorSalida.GAME_SEND_hOUSE(player, packet.toString());

                if (house.getValue().getOwnerId() == player.getAccID()) {
                    StringBuilder packet1 = new StringBuilder();
                    packet1.append("L+|").append(house.getValue().getId()).append(";").append(house.getValue().getAccess()).append(";");

                    if (house.getValue().getSale() <= 0) {
                        packet1.append("0;").append(house.getValue().getSale());
                    } else if (house.getValue().getSale() > 0) {
                        packet1.append("1;").append(house.getValue().getSale());
                    }
                    GestorSalida.GAME_SEND_hOUSE(player, packet1.toString());
                }
            });
        }

        public void buy(Jugador player)//Acheter une maison
        {
            Casas house = player.getInHouse();

            if (Mundo.mundo.getHouseManager().alreadyHaveHouse(player)) {
                GestorSalida.GAME_SEND_Im_PACKET(player, "132;1");
                return;
            }

            if (player.getKamas() < house.getSale())
                return;

            player.setKamas(player.getKamas() - house.getSale());

            int kamas = 0;
            for (Cofres trunk : Cofres.getTrunksByHouse(house)) {
                if (house.getOwnerId() > 0)
                    trunk.moveTrunkToBank(Mundo.mundo.getAccount(house.getOwnerId()));//D�placement des items vers la banque

                kamas += trunk.getKamas();
                trunk.setKamas(0);//Retrait kamas
                trunk.setKey("-");//ResetPass
                trunk.setOwnerId(player.getAccID());//ResetOwner
                Database.estaticos.getTrunkData().update(trunk);
            }

            //Ajoute des kamas dans la banque du vendeur
            if (house.getOwnerId() > 0) {
                Cuenta seller = Mundo.mundo.getAccount(house.getOwnerId());
                seller.setBankKamas(seller.getBankKamas() + house.getSale() + kamas);

                if (seller.getCurrentPlayer() != null)//FIXME: change the packet (Im)
                    GestorSalida.GAME_SEND_MESSAGE(seller.getCurrentPlayer(), "Une maison vous appartenant à été vendue " + house.getSale() + " kamas.");
                Database.dinamicos.getAccountData().update(seller);
            }

            closeBuy(player);
            GestorSalida.GAME_SEND_STATS_PACKET(player);
            Database.estaticos.getHouseData().buy(player, house);

            for (Jugador viewer : player.getCurMap().getPlayers())
                Mundo.mundo.getHouseManager().load(viewer, viewer.getCurMap().getId());

            Database.dinamicos.getPlayerData().update(player);
        }

        public void sell(Jugador P, String packet)//Vendre une maison
        {
            Casas h = P.getInHouse();
            int price = Integer.parseInt(packet);
            if (h.isHouse(P, h)) {
                GestorSalida.GAME_SEND_hOUSE(P, "V");
                GestorSalida.GAME_SEND_hOUSE(P, "SK" + h.getId() + "|" + price);
                //Vente de la maison
                Database.estaticos.getHouseData().sell(h, price);
                //Rafraichir la map apr�s la mise en vente
                for (Jugador z : P.getCurMap().getPlayers())
                    load(z, z.getCurMap().getId());
            }
        }

        public void closeCode(Jugador P) {
            GestorSalida.GAME_SEND_KODE(P, "V");
            P.setInHouse(null);
        }

        public void closeBuy(Jugador P) {
            GestorSalida.GAME_SEND_hOUSE(P, "V");
        }

        public void lockIt(Jugador P, String packet) {
            Casas h = P.getInHouse();
            if (h.isHouse(P, h)) {
                Database.estaticos.getHouseData().updateCode(P, h, packet);//Change le code
                closeCode(P);
            } else {
                closeCode(P);
            }
        }

        public String parseHouseToGuild(Jugador P) {
            boolean isFirst = true;
            StringBuilder packet = new StringBuilder("+");
            for (Map.Entry<Integer, Casas> house : Mundo.mundo.getHouses().entrySet()) {
                if (house.getValue().getGuildId() == P.getGuild().getId()
                        && house.getValue().getGuildRights() > 0) {
                    String name = "";
                    int id = house.getValue().getOwnerId();
                    if (id != -1) {
                        Cuenta a = Mundo.mundo.getAccount(id);
                        if (a != null) {
                            name = a.getPseudo();
                        }
                    }
                    if (isFirst) {
                        packet.append(house.getKey()).append(";");
                        if (Mundo.mundo.getPlayer(house.getValue().getOwnerId()) == null)
                            packet.append(name).append(";");
                        else
                            packet.append(Mundo.mundo.getPlayer(house.getValue().getOwnerId()).getAccount().getPseudo()).append(";");
                        packet.append(Mundo.mundo.getMap((short) house.getValue().getHouseMapId()).getX()).append(",").append(Mundo.mundo.getMap((short) house.getValue().getHouseMapId()).getY()).append(";");
                        packet.append("0;");
                        packet.append(house.getValue().getGuildRights());
                        isFirst = false;
                    } else {
                        packet.append("|");
                        packet.append(house.getKey()).append(";");
                        if (Mundo.mundo.getPlayer(house.getValue().getOwnerId()) == null)
                            packet.append(name).append(";");
                        else
                            packet.append(Mundo.mundo.getPlayer(house.getValue().getOwnerId()).getAccount().getPseudo()).append(";");
                        packet.append(Mundo.mundo.getMap((short) house.getValue().getHouseMapId()).getX()).append(",").append(Mundo.mundo.getMap((short) house.getValue().getHouseMapId()).getY()).append(";");
                        packet.append("0;");
                        packet.append(house.getValue().getGuildRights());
                    }
                }
            }
            return packet.toString();
        }

        public boolean alreadyHaveHouse(Jugador P) {
            for (Map.Entry<Integer, Casas> house : Mundo.mundo.getHouses().entrySet()){
                if (house.getValue().getOwnerId() != P.getAccID()) continue;
                return true;
            }
            return false;
        }

        public void parseHG(Jugador P, String packet) {
            Casas h = P.getInHouse();
            if (P.getGuild() == null)
                return;
            if (packet != null) {
                if (packet.charAt(0) == '+') {
                    //Ajoute en guilde
                    byte HouseMaxOnGuild = (byte) Math.floor(P.getGuild().getLvl() / 10);
                    if (houseOnGuild(P.getGuild().getId()) >= HouseMaxOnGuild && P.getGuild().getId() > 2) {
                        P.send("Im1151");
                        return;
                    }
                    if (P.getGuild().getPlayers().size() < 10 && P.getGuild().getId() > 2) {
                        return;
                    }
                    Database.estaticos.getHouseData().updateGuild(h, P.getGuild().getId(), 0);
                    parseHG(P, null);
                } else if (packet.charAt(0) == '-') {
                    //Retire de la guilde
                    Database.estaticos.getHouseData().updateGuild(h, 0, 0);
                    parseHG(P, null);
                } else {
                    Database.estaticos.getHouseData().updateGuild(h, h.getGuildId(), Integer.parseInt(packet));
                    h.parseIntToRight(Integer.parseInt(packet));
                }
            } else if (packet == null) {
                if (h.getGuildId() <= 0) {
                    GestorSalida.GAME_SEND_hOUSE(P, "G" + h.getId());
                } else if (h.getGuildId() > 0) {
                    GestorSalida.GAME_SEND_hOUSE(P, "G" + h.getId() + ";"
                            + P.getGuild().getName() + ";"
                            + P.getGuild().getEmblem() + ";" + h.getGuildRights());
                }
            }
        }

        public byte houseOnGuild(int GuildID) {
            byte i = 0;
            for (Map.Entry<Integer, Casas> house : Mundo.mundo.getHouses().entrySet()){
                if (house.getValue().getGuildId() != GuildID) continue;
                i = (byte)(i + 1);
            }
            return i;
        }

        public void leave(Jugador player, String packet) {
            Casas h = player.getInHouse();
            if (!h.isHouse(player, h))
                return;
            int Pguid = Integer.parseInt(packet);
            Jugador Target = Mundo.mundo.getPlayer(Pguid);
            if (Target == null || (Target.getGroupe() != null) || !Target.isOnline() || Target.getPelea() != null
                    || Target.getCurMap().getId() != player.getCurMap().getId())
                return;
            Target.teleport(h.getMapId(), h.getCellId());
            GestorSalida.GAME_SEND_Im_PACKET(Target, "018;" + player.getName());
        }

        public Casas getHouseByPerso(Jugador player)  {
            for (Map.Entry<Integer, Casas> house : Mundo.mundo.getHouses().entrySet())
                if (house.getValue().getOwnerId() == player.getAccID())
                    return house.getValue();
            return null;
        }

        public void removeHouseGuild(int guildId) {
            Mundo.mundo.getHouses().entrySet().stream().filter(h -> h.getValue().getGuildId() == guildId).forEach(h -> {
                h.getValue().setGuildRights(0);
                h.getValue().setGuildId(0);
            });
            Database.estaticos.getHouseData().removeGuild(guildId); //Supprime les maisons de guilde
        }
    }
}