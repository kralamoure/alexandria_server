package org.alexandria.estaticos.oficio;

import org.alexandria.estaticos.area.mapa.Mapa.GameCase;
import org.alexandria.estaticos.area.mapa.Mapa.ObjetosInteractivos;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.Camino;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.configuracion.Constantes;
import org.alexandria.configuracion.Logging;
import org.alexandria.estaticos.Monstruos;
import org.alexandria.estaticos.juego.accion.AccionIntercambiar;
import org.alexandria.estaticos.juego.accion.AccionJuego;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.objeto.ObjetoModelo;
import org.alexandria.estaticos.pelea.hechizo.EfectoHechizo;
import org.alexandria.otro.utilidad.Doble;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class OficioAccion {

    public Map<Integer, Integer> ingredients = new TreeMap<>(), lastCraft = new TreeMap<>();
    public Jugador player;
    public String data = "";
    public boolean broke = false;
    public boolean broken = false;
    public boolean isRepeat = false;
    private final int id;
    private int min = 1;
    private int max = 1;
    private final boolean isCraft;
    private int chan = 100;
    private int time = 0;
    private int xpWin = 0;
    private OficioCaracteristicas SM;
    private OficioCraft jobCraft;
    public OficioCraft oldJobCraft;
    private int reConfigingRunes = -1;

    public OficioAccion(int sk, int min, int max, boolean craft, int arg, int xpWin) {
        this.id = sk;
        this.min = min;
        this.max = max;
        this.isCraft = craft;
        this.xpWin = xpWin;
        if (craft) this.chan = arg;
        else this.time = arg;
    }

    public int getId() {
        return this.id;
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public boolean isCraft() {
        return this.isCraft;
    }

    public int getChance() {
        return this.chan;
    }

    public int getTime() {
        return this.time;
    }

    public int getXpWin() {
        return this.xpWin;
    }

    public OficioCaracteristicas getJobStat() {
        return this.SM;
    }

    public OficioCraft getJobCraft() {
        return this.jobCraft;
    }

    public void setJobCraft(OficioCraft jobCraft) {
        this.jobCraft = jobCraft;
    }

    public void startCraft(Jugador P) {
        if(P.getObjetoInteractivo()!=null)
        {
            P.getObjetoInteractivo().getPrimero().setState(OficioConstantes.IOBJECT_STATE_EMPTYING);
            GestorSalida.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(),P.getObjetoInteractivo().getSegundo());
        }
        this.jobCraft = new OficioCraft(this,P);
    }

    public void startAction(Jugador P, ObjetosInteractivos IO, AccionJuego GA, GameCase cell, OficioCaracteristicas SM) {
        this.SM = SM;
        this.player = P;

        if (P.getObjetByPos(Constantes.ITEM_POS_ARME) != null && SM.getTemplate().getId() == 36) {
            if (Mundo.mundo.getMetier(36).isValidTool(P.getObjetByPos(Constantes.ITEM_POS_ARME).getModelo().getId())) {
                int dist = Camino.getDistanceBetween(P.getCurMap(), P.getCurCell().getId(), cell.getId());
                int distItem = OficioConstantes.getDistCanne(P.getObjetByPos(Constantes.ITEM_POS_ARME).getModelo().getId());
                if (distItem < dist) {
                    GestorSalida.GAME_SEND_MESSAGE(P, "Vous êtes trop loin pour pouvoir pécher ce poisson !");
                    GestorSalida.GAME_SEND_GA_PACKET(P.getGameClient(), "", "0", "", "");
                    P.setExchangeAction(null);
                    P.setDoAction(false);
                    return;
                }
            }
        }
        if (!this.isCraft) {
            P.getGameClient().action = Instant.now().toEpochMilli();
            IO.setInteractive(false);
            IO.setState(OficioConstantes.IOBJECT_STATE_EMPTYING);
            GestorSalida.GAME_SEND_GA_PACKET_TO_MAP(P.getCurMap(), "" + GA.getId(), 501, P.getId() + "", cell.getId() + "," + this.time);
            GestorSalida.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(), cell);
        } else {
            P.setAway(true);
            IO.setState(OficioConstantes.IOBJECT_STATE_EMPTYING);
            P.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.CRAFTING, this));
            GestorSalida.GAME_SEND_ECK_PACKET(P, 3, this.min + ";" + this.id);
            GestorSalida.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(), cell);
        }
    }

    public void startAction(Jugador P, ObjetosInteractivos IO, AccionJuego GA, GameCase cell) {
        this.player = P;
        P.setAway(true);
        IO.setState(OficioConstantes.IOBJECT_STATE_EMPTYING);//FIXME trouver la bonne valeur
        P.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.CRAFTING, this));
        GestorSalida.GAME_SEND_ECK_PACKET(P, 3, this.min + ";" + this.id);//this.min => Nbr de Case de l'interface
        GestorSalida.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(), cell);
    }

    public void endAction(Jugador player, ObjetosInteractivos IO, AccionJuego GA, GameCase cell) {
        if(!this.isCraft && player.getGameClient().action != 0) {
            if(Instant.now().toEpochMilli() - player.getGameClient().action < this.time - 500) {
                player.getGameClient().kick();//FIXME: Ajouté le ban si aucune plainte.
                return;
            }
        }

        player.setDoAction(false);
        if (IO == null)
            return;
        if (!this.isCraft) {
            IO.setState(OficioConstantes.IOBJECT_STATE_EMPTY);
            IO.disable();
            GestorSalida.GAME_SEND_GDF_PACKET_TO_MAP(player.getCurMap(),cell);
            int qua = (this.max > this.min ? Formulas.getRandomValue(this.min, this.max) : this.min);

            if (SM.getTemplate().getId() == 36) {
                if (qua > 0)
                    SM.addXp(player, (long) (this.getXpWin() * Configuracion.INSTANCE.getRATE_JOB() * Mundo.mundo.getConquestBonus(player)));
            } else
                SM.addXp(player, (long) (this.getXpWin() * Configuracion.INSTANCE.getRATE_JOB() * Mundo.mundo.getConquestBonus(player)));

            int tID = OficioConstantes.getObjectByJobSkill(this.id);

            if (SM.getTemplate().getId() == 36 && qua > 0) {
                if (Formulas.getRandomValue(1, 1000) <= 2) {
                    int _tID = OficioConstantes.getPoissonRare(tID);
                    if (_tID != -1) {
                        ObjetoModelo _T = Mundo.mundo.getObjetoModelo(_tID);
                        if (_T != null) {
                            ObjetoJuego _O = _T.createNewItem(qua, false);
                            if (player.addObjet(_O, true))
                                Mundo.addGameObject(_O, true);
                        }
                    }
                }
            }


            ObjetoModelo T = Mundo.mundo.getObjetoModelo(tID);
            if (T == null)
                return;
            ObjetoJuego O = T.createNewItem(qua, false);

            if (player.addObjet(O, true))
                Mundo.addGameObject(O, true);
            GestorSalida.GAME_SEND_IQ_PACKET(player, player.getId(), qua);
            GestorSalida.GAME_SEND_Ow_PACKET(player);

            if (player.getMetierBySkill(this.id).get_lvl() >= 30 && Formulas.getRandomValue(1, 40) > 39) {
                for (int[] protector : OficioConstantes.JOB_PROTECTORS) {
                    if (tID == protector[1]) {
                        int monsterLvl = OficioConstantes.getProtectorLvl(player.getLevel());
                        player.getCurMap().startFightVersusProtectors(player, new Monstruos.MobGroup(player.getCurMap().nextObjectId, cell.getId(), protector[0] + "," + monsterLvl + "," + monsterLvl));
                        break;
                    }
                }
            }
        }
        player.setAway(false);
    }

    private int addCraftObject(Jugador player, ObjetoJuego newObj) {
        for (Entry<Integer, ObjetoJuego> entry : player.getItems().entrySet()) {
            ObjetoJuego obj = entry.getValue();
            if (obj.getModelo().getId() == newObj.getModelo().getId() && obj.getTxtStat().equals(newObj.getTxtStat())
                    && obj.getCaracteristicas().isSameStats(newObj.getCaracteristicas()) && obj.getPosicion() == Constantes.ITEM_POS_NO_EQUIPED) {
                obj.setCantidad(obj.getCantidad() + newObj.getCantidad());//On ajoute QUA item a la quantit� de l'objet existant
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(player, obj);
                return obj.getId();
            }
        }

        player.getItems().put(newObj.getId(), newObj);
        GestorSalida.GAME_SEND_OAKO_PACKET(player, newObj);
        Mundo.addGameObject(newObj, true);
        return -1;
    }

    public void addIngredient(Jugador player, int id, int quantity) {
        int oldQuantity = this.ingredients.get(id) == null ? 0 : this.ingredients.get(id);
        if(quantity < 0) if(- quantity > oldQuantity) return;

        this.ingredients.remove(id);
        oldQuantity += quantity;

        if (oldQuantity > 0) {
            this.ingredients.put(id, oldQuantity);
            GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(player, 'O', "+", id + "|" + oldQuantity);
        } else {
            GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(player, 'O', "-", id + "");
        }
    }

    public byte sizeList(Map<Jugador, ArrayList<Doble<Integer, Integer>>> list) {
        byte size = 0;

        for (ArrayList<Doble<Integer, Integer>> entry : list.values()) {
            for (Doble<Integer, Integer> couple : entry) {
                ObjetoJuego object = Mundo.getGameObject(couple.getPrimero());
                if (object != null) {
                    ObjetoModelo objectTemplate = object.getModelo();
                    if (objectTemplate != null && objectTemplate.getId() != 7508) size++;
                }
            }
        }
        return size;
    }

    public void putLastCraftIngredients() {
        if (this.player == null || this.lastCraft == null || !this.ingredients.isEmpty()) return;

        this.ingredients.clear();
        this.ingredients.putAll(this.lastCraft);
        this.ingredients.entrySet().stream().filter(e -> Mundo.getGameObject(e.getKey()) != null)
                .filter(e -> !(Mundo.getGameObject(e.getKey()).getCantidad() < e.getValue()))
                .forEach(e -> GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player, 'O', "+", e.getKey() + "|" + e.getValue()));
    }

    public void resetCraft() {
        this.ingredients.clear();
        this.lastCraft.clear();
        this.oldJobCraft = null;
        this.jobCraft = null;
    }

    public boolean craftPublicMode(Jugador crafter, Jugador receiver, Map<Jugador, ArrayList<Doble<Integer, Integer>>> list) {
        if (!this.isCraft) return false;

        this.player = crafter;
        OficioCaracteristicas SM = this.player.getMetierBySkill(this.id);
        boolean signed = false;

        if (this.id == 1 || this.id == 113 || this.id == 115 || this.id == 116 || this.id == 117 || this.id == 118 || this.id == 119 || this.id == 120 || (this.id >= 163 && this.id <= 169)) {
            this.SM = SM;
            return this.craftMaging(isRepeat, receiver, list);
        }

        Map<Integer, Integer> items = new HashMap<>();

        for (Entry<Jugador, ArrayList<Doble<Integer, Integer>>> entry : list.entrySet()) {
            Jugador player = entry.getKey();
            Map<Integer, Integer> playerItems=new HashMap<>();

            for(Doble<Integer, Integer> couple : entry.getValue())
                playerItems.put(couple.getPrimero(),couple.getSegundo());

            for(Entry<Integer, Integer> e : playerItems.entrySet()) {
                if(!player.hasItemGuid(e.getKey())) {
                    GestorSalida.GAME_SEND_Ec_PACKET(player,"EI");
                    GestorSalida.GAME_SEND_Ec_PACKET(this.player,"EI");
                    return false;
                }

                ObjetoJuego gameObject = Mundo.getGameObject(e.getKey());
                if (gameObject == null) {
                    GestorSalida.GAME_SEND_Ec_PACKET(player, "EI");
                    GestorSalida.GAME_SEND_Ec_PACKET(this.player, "EI");
                    return false;
                }
                if (gameObject.getCantidad() < e.getValue()) {
                    GestorSalida.GAME_SEND_Ec_PACKET(player, "EI");
                    GestorSalida.GAME_SEND_Ec_PACKET(this.player, "EI");
                    return false;
                }

                int newQua = gameObject.getCantidad() - e.getValue();

                if (newQua < 0)
                    return false;

                if (newQua == 0) {
                    player.removeItem(e.getKey());
                    Mundo.mundo.removeGameObject(e.getKey());
                    GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(player, e.getKey());
                } else {
                    gameObject.setCantidad(newQua);
                    GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(player, gameObject);
                }

                items.put(gameObject.getModelo().getId(),e.getValue());
            }
        }

        GestorSalida.GAME_SEND_Ow_PACKET(this.player);


        //Rune de signature
        if (items.containsKey(7508))
            if (SM.get_lvl() == 100)
                signed = true;

        items.remove(7508);
        int template = Mundo.mundo.getObjectByIngredientForJob(SM.getTemplate().getListBySkill(this.id), items);

        if (template == -1 || !SM.getTemplate().canCraft(this.id, template)) {
            GestorSalida.GAME_SEND_Ec_PACKET(this.player, "EI");
            receiver.send("EcEI");
            GestorSalida.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");
            items.clear();
            return false;
        }

        boolean success=true;

        if (Configuracion.INSTANCE.getMostrarenviados()) {
        Logging.crafeo.info(this.player.getName() + " à crafter avec " + (success ? "SUCCES" : "ECHEC") + " l'item " + template + " (" + Mundo.mundo.getObjetoModelo(template).getName() + ") pour " + receiver.getName());
        }

        if (!success) {
            GestorSalida.GAME_SEND_Ec_PACKET(this.player, "EF");
            GestorSalida.GAME_SEND_Ec_PACKET(receiver, "EF");
            GestorSalida.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-" + template);
            GestorSalida.GAME_SEND_Im_PACKET(this.player, "0118");
        } else {
            ObjetoJuego newObj = Mundo.mundo.getObjetoModelo(template).createNewItem(1, false);
            if (signed) newObj.addTxtStat(988, this.player.getName());
            int guid = this.addCraftObject(receiver, newObj);
            if(guid == -1) guid = newObj.getId();
            String stats = newObj.parseStatsString();

            this.player.send("ErKO+" + guid + "|1|" + template + "|" + stats);
            receiver.send("ErKO+" + guid + "|1|" + template + "|" + stats);
            this.player.send("EcK;" + template + ";T" + receiver.getName() + ";" + stats);
            receiver.send("EcK;" + template + ";B" + crafter.getName() + ";" + stats);

            GestorSalida.GAME_SEND_Ow_PACKET(this.player);
            GestorSalida.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+" + template);
        }

        int winXP = Formulas.calculXpWinCraft(SM.get_lvl(), this.ingredients.size()) * Configuracion.INSTANCE.getRATE_JOB();
        if (SM.getTemplate().getId() == 28 && winXP == 1)
            winXP = 10;
        if (success) {
            SM.addXp(this.player, winXP);
            ArrayList<OficioCaracteristicas> SMs = new ArrayList<>();
            SMs.add(SM);
            GestorSalida.GAME_SEND_JX_PACKET(this.player, SMs);
        }

        this.ingredients.clear();
        return success;
    }

    public synchronized void craft(boolean isRepeat) {
        if (!this.isCraft) return;

        if (this.id == 1 || this.id == 113 || this.id == 115 || this.id == 116 || this.id == 117 || this.id == 118 || this.id == 119 || this.id == 120 || (this.id >= 163 && this.id <= 169)) {
            this.craftMaging(isRepeat, null, null);
            return;
        }

        Map<Integer, Integer> items = new HashMap<>();
        //on retire les items mis en ingr�dients
        for (Entry<Integer, Integer> e : this.ingredients.entrySet()) {
            if (!this.player.hasItemGuid(e.getKey())) {
                GestorSalida.GAME_SEND_Ec_PACKET(this.player, "EI");
                return;
            }

            ObjetoJuego obj = Mundo.getGameObject(e.getKey());

            if (obj == null) {
                GestorSalida.GAME_SEND_Ec_PACKET(this.player, "EI");
                return;
            }
            if (obj.getCantidad() < e.getValue()) {
                GestorSalida.GAME_SEND_Ec_PACKET(this.player, "EI");
                return;
            }

            int newQua = obj.getCantidad() - e.getValue();
            if (newQua < 0) return;

            if (newQua == 0) {
                this.player.removeItem(e.getKey());
                Mundo.mundo.removeGameObject(e.getKey());
                GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this.player, e.getKey());
            } else {
                //Actualiza en tiempo real las cantidades de los ingrediente
                final int change = obj.getCantidad()-newQua;
                obj.setCantidad(newQua);
                GestorSalida.GAME_SEND_Em_PACKET(this.player,"KO+"+e.getKey()+"|-"+change+"|"+obj.getModelo().getId()+"|"+obj.parseStatsString().replace(";","#"));
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,obj);
            }

            items.put(obj.getModelo().getId(), e.getValue());
        }

        boolean signed = false;

        if (items.containsKey(7508)) {
            signed = true;
            items.remove(7508);
        }

        GestorSalida.GAME_SEND_Ow_PACKET(this.player);

        boolean isUnjobSkill = this.getJobStat() == null;

        if (!isUnjobSkill) {
            OficioCaracteristicas SM = this.player.getMetierBySkill(this.id);
            int templateId = Mundo.mundo.getObjectByIngredientForJob(SM.getTemplate().getListBySkill(this.id), items);
            //Recette non existante ou pas adapt� au m�tier
            if (templateId == -1 || !SM.getTemplate().canCraft(this.id, templateId)) {
                GestorSalida.GAME_SEND_Ec_PACKET(this.player, "EI");
                GestorSalida.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");
                this.ingredients.clear();
                return;
            }

            boolean success=true;

            if(!success)
            {
                GestorSalida.GAME_SEND_Ec_PACKET(this.player,"EF");
                GestorSalida.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"-"+templateId);
                GestorSalida.GAME_SEND_Im_PACKET(this.player,"0118");
            }
            else
            {
                ObjetoJuego newObj=Mundo.mundo.getObjetoModelo(templateId).createNewItemWithoutDuplication(this.player.getItems().values(),1,false);

                int guid = newObj.getId();
                if(guid==-1)
                { // Don't exist
                    guid = newObj.setId();
                    this.player.getItems().put(guid, newObj);
                    GestorSalida.GAME_SEND_OAKO_PACKET(this.player, newObj);
                    Mundo.addGameObject(newObj, true);
                } else {
                    newObj.setCantidad(newObj.getCantidad() + 1);
                    GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, newObj);
                }

                GestorSalida.GAME_SEND_Ow_PACKET(this.player);
                if (signed) newObj.addTxtStat(988, this.player.getName());
                GestorSalida.GAME_SEND_Em_PACKET(this.player, "KO+" + guid + "|1|" + templateId + "|" + newObj.parseStatsString().replace(";", "#"));
                GestorSalida.GAME_SEND_Ec_PACKET(this.player, "K;" + templateId);
                GestorSalida.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+" + templateId);
            }

            int winXP = 0;
            if (success)
                winXP = Formulas.calculXpWinCraft(SM.get_lvl(), this.ingredients.size()) * Configuracion.INSTANCE.getRATE_JOB();
            else if (!SM.getTemplate().isMaging())
                winXP = Formulas.calculXpWinCraft(SM.get_lvl(), this.ingredients.size()) * Configuracion.INSTANCE.getRATE_JOB();

            if (winXP > 0) {
                SM.addXp(this.player, winXP);
                ArrayList<OficioCaracteristicas> SMs = new ArrayList<>();
                SMs.add(SM);
                GestorSalida.GAME_SEND_JX_PACKET(this.player, SMs);
            }
        } else {
            final int templateId = Mundo.mundo.getObjectByIngredientForJob(Mundo.mundo.getMetier(this.id).getListBySkill(this.id), items);

            if (templateId == -1 || !Mundo.mundo.getMetier(this.id).canCraft(this.id, templateId)) {
                GestorSalida.GAME_SEND_Ec_PACKET(this.player, "EI");
                GestorSalida.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");
                this.ingredients.clear();
                return;
            }

            ObjetoJuego newObj = Mundo.mundo.getObjetoModelo(templateId).createNewItemWithoutDuplication(this.player.getItems().values(), 1, false);
            int guid = newObj.getId();

            if(guid == -1) { // Don't exist
                guid = newObj.setId();
                this.player.getItems().put(guid, newObj);
                GestorSalida.GAME_SEND_OAKO_PACKET(this.player, newObj);
                Mundo.addGameObject(newObj, true);
            } else {
                newObj.setCantidad(newObj.getCantidad() + 1);
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, newObj);
            }

            if (signed) newObj.addTxtStat(988, this.player.getName());

            GestorSalida.GAME_SEND_Ow_PACKET(this.player);
            GestorSalida.GAME_SEND_Em_PACKET(this.player, "KO+" + guid + "|1|" + templateId + "|" + newObj.parseStatsString().replace(";", "#"));
            GestorSalida.GAME_SEND_Ec_PACKET(this.player, "K;" + templateId);
            GestorSalida.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+" + templateId);
        }
        this.lastCraft.clear();
        this.lastCraft.putAll(this.ingredients);
        this.ingredients.clear();

        if(!isRepeat)
        {
            if(this.player.getObjetoInteractivo()!=null)
            {
                this.player.getObjetoInteractivo().getPrimero().setState(OficioConstantes.IOBJECT_STATE_FULL);
                GestorSalida.GAME_SEND_GDF_PACKET_TO_MAP(this.player.getCurMap(),this.player.getObjetoInteractivo().getSegundo());
            }
            this.oldJobCraft=this.jobCraft;
            this.jobCraft=null;
        }
    }

    /* ********FM TOUT POURRI*************/
     private synchronized boolean craftMaging(boolean isRepeat, Jugador receiver, Map<Jugador, ArrayList<Doble<Integer, Integer>>> items) {
        boolean isSigningRune = false;
        ObjetoJuego objectFm = null, signingRune = null, runeOrPotion = null;
        int lvlElementRune = 0, statId = -1, lvlQuaStatsRune = 0, statsAdd = 0, deleteID = -1, poid = 0, idRune = 0;
        boolean bonusRune = false;
        String statsObjectFm = "-1";

        final boolean secure = items != null && receiver != null;
        final Map<Integer, Integer> ingredients = items == null ? this.ingredients : new HashMap<>();

        if(items != null) {
            for(Entry<Jugador, ArrayList<Doble<Integer, Integer>>> entry : items.entrySet()) {
                for(Doble<Integer, Integer> couple : entry.getValue()) {
                    ingredients.put(couple.getPrimero(), couple.getSegundo());
                }
            }
        }

        for (int id : ingredients.keySet()) {
            ObjetoJuego object = Mundo.getGameObject(id);

            if(object == null) {
                if(!this.player.hasItemGuid(id) || (secure && !this.player.hasItemGuid(id) && !receiver.hasItemGuid(id))) {
                    GestorSalida.GAME_SEND_Ec_PACKET(this.player, "EI");
                    GestorSalida.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");
                    ingredients.clear();
                    return false;
                }
            }

            int template = object.getModelo().getId();
            if (object.getModelo().getType() == 78)
                idRune = id;

            //region gros switch rune
            //region on s'en tape
            //endregion
            switch (template) {
                case 1333, 1345, 1343 -> {
                    statId = 99;
                    lvlElementRune = object.getModelo().getLevel();
                    runeOrPotion = object;
                }
                case 1335, 1346, 1341 -> {
                    statId = 96;
                    lvlElementRune = object.getModelo().getLevel();
                    runeOrPotion = object;
                }
                case 1337, 1347, 1342 -> {
                    statId = 98;
                    lvlElementRune = object.getModelo().getLevel();
                    runeOrPotion = object;
                }
                case 1338, 1348, 1340 -> {
                    statId = 97;
                    lvlElementRune = object.getModelo().getLevel();
                    runeOrPotion = object;
                }
                case 1519 -> {
                    runeOrPotion = object;
                    statsObjectFm = "76";
                    statsAdd = 1;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1521 -> {
                    runeOrPotion = object;
                    statsObjectFm = "7c";
                    statsAdd = 1;
                    poid = 6;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1522 -> {
                    runeOrPotion = object;
                    statsObjectFm = "7e";
                    statsAdd = 1;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1523 -> {
                    runeOrPotion = object;
                    statsObjectFm = "7d";
                    statsAdd = 3;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1524 -> {
                    runeOrPotion = object;
                    statsObjectFm = "77";
                    statsAdd = 1;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1525 -> {
                    runeOrPotion = object;
                    statsObjectFm = "7b";
                    statsAdd = 1;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1545 -> {
                    runeOrPotion = object;
                    statsObjectFm = "76";
                    statsAdd = 3;
                    poid = 3;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1546 -> {
                    runeOrPotion = object;
                    statsObjectFm = "7c";
                    statsAdd = 3;
                    poid = 18;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1547 -> {
                    runeOrPotion = object;
                    statsObjectFm = "7e";
                    statsAdd = 3;
                    poid = 3;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1548 -> {
                    runeOrPotion = object;
                    statsObjectFm = "7d";
                    statsAdd = 10;
                    poid = 10;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1549 -> {
                    runeOrPotion = object;
                    statsObjectFm = "77";
                    statsAdd = 3;
                    poid = 3;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1550 -> {
                    runeOrPotion = object;
                    statsObjectFm = "7b";
                    statsAdd = 3;
                    poid = 10;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1551 -> {
                    runeOrPotion = object;
                    statsObjectFm = "76";
                    statsAdd = 10;
                    poid = 10;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1552 -> {
                    runeOrPotion = object;
                    statsObjectFm = "7c";
                    statsAdd = 10;
                    poid = 50;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1553 -> {
                    runeOrPotion = object;
                    statsObjectFm = "7e";
                    statsAdd = 10;
                    poid = 10;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1554 -> {
                    runeOrPotion = object;
                    statsObjectFm = "7d";
                    statsAdd = 30;
                    poid = 10;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1555 -> {
                    runeOrPotion = object;
                    statsObjectFm = "77";
                    statsAdd = 10;
                    poid = 10;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1556 -> {
                    runeOrPotion = object;
                    statsObjectFm = "7b";
                    statsAdd = 10;
                    poid = 10;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1557 -> {
                    runeOrPotion = object;
                    statsObjectFm = "6f";
                    statsAdd = 1;
                    poid = 100;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 1558 -> {
                    runeOrPotion = object;
                    statsObjectFm = "80";
                    statsAdd = 1;
                    poid = 90;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7433 -> {
                    runeOrPotion = object;
                    statsObjectFm = "73";
                    statsAdd = 1;
                    poid = 30;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7434 -> {
                    runeOrPotion = object;
                    statsObjectFm = "b2";
                    statsAdd = 1;
                    poid = 20;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7435 -> {
                    runeOrPotion = object;
                    statsObjectFm = "79";
                    statsAdd = 1;
                    poid = 20;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7436 -> {
                    runeOrPotion = object;
                    statsObjectFm = "8a";
                    statsAdd = 1;
                    poid = 2;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7437 -> {
                    runeOrPotion = object;
                    statsObjectFm = "dc";
                    statsAdd = 1;
                    poid = 2;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7438 -> {
                    runeOrPotion = object;
                    statsObjectFm = "75";
                    statsAdd = 1;
                    poid = 50;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7442 -> {
                    runeOrPotion = object;
                    statsObjectFm = "b6";
                    statsAdd = 1;
                    poid = 30;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7443 -> {
                    runeOrPotion = object;
                    statsObjectFm = "9e";
                    statsAdd = 10;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7444 -> {
                    runeOrPotion = object;
                    statsObjectFm = "9e";
                    statsAdd = 30;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7445 -> {
                    runeOrPotion = object;
                    statsObjectFm = "9e";
                    statsAdd = 100;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7446 -> {
                    runeOrPotion = object;
                    statsObjectFm = "e1";
                    statsAdd = 1;
                    poid = 15;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7447 -> {
                    runeOrPotion = object;
                    statsObjectFm = "e2";
                    statsAdd = 1;
                    poid = 2;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7448 -> {
                    runeOrPotion = object;
                    statsObjectFm = "ae";
                    statsAdd = 10;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7449 -> {
                    runeOrPotion = object;
                    statsObjectFm = "ae";
                    statsAdd = 30;
                    poid = 3;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7450 -> {
                    runeOrPotion = object;
                    statsObjectFm = "ae";
                    statsAdd = 100;
                    poid = 10;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7451 -> {
                    runeOrPotion = object;
                    statsObjectFm = "b0";
                    statsAdd = 1;
                    poid = 5;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7452 -> {
                    runeOrPotion = object;
                    statsObjectFm = "f3";
                    statsAdd = 1;
                    poid = 4;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7453 -> {
                    runeOrPotion = object;
                    statsObjectFm = "f2";
                    statsAdd = 1;
                    poid = 4;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7454 -> {
                    runeOrPotion = object;
                    statsObjectFm = "f1";
                    statsAdd = 1;
                    poid = 4;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7455 -> {
                    runeOrPotion = object;
                    statsObjectFm = "f0";
                    statsAdd = 1;
                    poid = 4;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7456 -> {
                    runeOrPotion = object;
                    statsObjectFm = "f4";
                    statsAdd = 1;
                    poid = 4;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7457 -> {
                    runeOrPotion = object;
                    statsObjectFm = "d5";
                    statsAdd = 1;
                    poid = 5;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7458 -> {
                    runeOrPotion = object;
                    statsObjectFm = "d4";
                    statsAdd = 1;
                    poid = 5;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7459 -> {
                    runeOrPotion = object;
                    statsObjectFm = "d2";
                    statsAdd = 1;
                    poid = 5;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7460 -> {
                    runeOrPotion = object;
                    statsObjectFm = "d6";
                    statsAdd = 1;
                    poid = 5;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7560 -> {
                    runeOrPotion = object;
                    statsObjectFm = "d3";
                    statsAdd = 1;
                    poid = 5;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 8379 -> {
                    runeOrPotion = object;
                    statsObjectFm = "7d";
                    statsAdd = 10;
                    poid = 10;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 10662 -> {
                    runeOrPotion = object;
                    statsObjectFm = "b0";
                    statsAdd = 3;
                    poid = 15;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 7508 -> {
                    isSigningRune = true;
                    signingRune = object;
                }
                case 11118 -> {
                    bonusRune = true;
                    runeOrPotion = object;
                    statsObjectFm = "76";
                    statsAdd = 15;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 11119 -> {
                    bonusRune = true;
                    runeOrPotion = object;
                    statsObjectFm = "7c";
                    statsAdd = 15;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 11120 -> {
                    bonusRune = true;
                    runeOrPotion = object;
                    statsObjectFm = "7e";
                    statsAdd = 15;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 11121 -> {
                    bonusRune = true;
                    runeOrPotion = object;
                    statsObjectFm = "7d";
                    statsAdd = 45;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 11122 -> {
                    bonusRune = true;
                    runeOrPotion = object;
                    statsObjectFm = "77";
                    statsAdd = 15;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 11123 -> {
                    bonusRune = true;
                    runeOrPotion = object;
                    statsObjectFm = "7b";
                    statsAdd = 15;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 11124 -> {
                    bonusRune = true;
                    runeOrPotion = object;
                    statsObjectFm = "b0";
                    statsAdd = 10;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 11125 -> {
                    bonusRune = true;
                    runeOrPotion = object;
                    statsObjectFm = "73";
                    statsAdd = 3;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 11126 -> {
                    bonusRune = true;
                    runeOrPotion = object;
                    statsObjectFm = "b2";
                    statsAdd = 5;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 11127 -> {
                    bonusRune = true;
                    runeOrPotion = object;
                    statsObjectFm = "70";
                    statsAdd = 5;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 11128 -> {
                    bonusRune = true;
                    runeOrPotion = object;
                    statsObjectFm = "8a";
                    statsAdd = 10;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 11129 -> {
                    bonusRune = true;
                    runeOrPotion = object;
                    statsObjectFm = "dc";
                    statsAdd = 5;
                    poid = 1;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                case 10057 -> {
                    bonusRune = true;
                    runeOrPotion = object;
                    statsObjectFm = "31b";
                    statsAdd = 1;
                    poid = 0;
                    lvlQuaStatsRune = object.getModelo().getLevel();
                }
                default -> {
                    int type = object.getModelo().getType();
                    if ((type >= 1 && type <= 11) || (type >= 16 && type <= 22) || type == 81 || type == 102 || type == 114 || object.getModelo().getPACost() > 0) {
                        final Jugador player = this.player.hasItemGuid(object.getId()) ? this.player : receiver;
                        objectFm = object;
                        GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK_FM(player.getGameClient(), 'O', "+", objectFm.getId() + "|" + 1);
                        deleteID = id;
                        ObjetoJuego newObj = ObjetoJuego.getCloneObjet(objectFm, 1); // Cr�ation d'un clone avec un nouveau identifiant

                        if (objectFm.getCantidad() > 1) { // S'il y avait plus d'un objet
                            int newQuant = objectFm.getCantidad() - 1; // On supprime celui que l'on a ajout�
                            objectFm.setCantidad(newQuant);
                            GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(player, objectFm);
                        } else {
                            Mundo.mundo.removeGameObject(id);

                            player.removeItem(id);
                            GestorSalida.GAME_SEND_DELETE_STATS_ITEM_FM(player, id);
                        }
                        objectFm = newObj; // Tout neuf avec un nouveau identifiant
                        break;
                    }
                }
            }
            //endregion
        }

        //region Calcul formule
        double poid2 = getPwrPerEffet(Integer.parseInt(statsObjectFm, 16));
        if (poid2 > 0.0)
            poid = statsAdd * ((int) poid2);

        if (SM == null || objectFm == null || runeOrPotion == null) {
            if (objectFm != null) {
                Mundo.addGameObject(objectFm, true);
                this.player.addObjet(objectFm);
            }

            if(receiver != null)
                GestorSalida.GAME_SEND_Ec_PACKET(receiver, "EI");
            GestorSalida.GAME_SEND_Ec_PACKET(this.player, "EI");
            GestorSalida.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-");

            ingredients.clear();
            return false;
        }
        if (deleteID != -1) {
            this.ingredients.remove(deleteID);
        }

        final ObjetoModelo template = objectFm.getModelo();
        ArrayList<Integer> chances = new ArrayList<>();

        int chance, lvlJob = SM.get_lvl(), currentWeightTotal = 1, pwrPerte;
        int objTemplateID = template.getId();
        String statStringObj = objectFm.parseStatsString();

        if (lvlElementRune > 0 && lvlQuaStatsRune == 0) {
            chance = Formulas.calculChanceByElement(lvlJob, template.getLevel(), lvlElementRune);
            if (chance > 100 - (lvlJob / 20))
                chance = 100 - (lvlJob / 20);
            if (chance < (lvlJob / 20))
                chance = (lvlJob / 20);
            chances.add(0, chance);
            chances.add(1, 0);
            chances.add(2, 100 - chance);
        } else if (lvlQuaStatsRune > 0 && lvlElementRune == 0) {
            int currentWeightStats = 1;
            if (!statStringObj.isEmpty()) {
                currentWeightTotal = currentTotalWeigthBase(statStringObj, objectFm); // Poids total de l'objet : PWRg
                currentWeightStats = currentWeithStats(objectFm, statsObjectFm); // Poids � ajouter : PWRcarac
            }

            int currentTotalBase = WeithTotalBase(objTemplateID); // Poids maximum de l'objet : PWRmax
            int currentMinBase = WeithTotalBaseMin(objTemplateID);

            if (currentTotalBase < 0)
                currentTotalBase = 0;
            if (currentWeightStats < 0)
                currentWeightStats = 0;
            if (currentWeightTotal < 0)
                currentWeightTotal = 0;

            float coef = 1;
            int baseStats = viewBaseStatsItem(objectFm, statsObjectFm), currentStats = viewActualStatsItem(objectFm, statsObjectFm);

            if (baseStats == 1 && currentStats == 1 || baseStats == 1 && currentStats == 0) {
                coef = 1.0f;
            } else if (baseStats == 2 && currentStats == 2) {
                coef = 0.50f;
            } else if (baseStats == 0 && currentStats == 0 || baseStats == 0 && currentStats == 1) {
                coef = 0.25f;
            }

            float x = 1;
            boolean canFM = true;
            int statMax = getStatBaseMaxs(objectFm.getModelo(), statsObjectFm), actualJet = getActualJet(objectFm, statsObjectFm);

            if (actualJet > statMax) {
                x = 0.8F;
                int overPerEffect = (int) getOverPerEffet(Integer.parseInt(statsObjectFm, 16));
                //if (statMax == 0)
                if (actualJet >= (statMax + overPerEffect))
                    canFM = false;
                if(Integer.parseInt(statsObjectFm, 16) == 111) {
                    if(objectFm.isOverFm2(111, 1))
                        if(!canFM)
                            canFM = true;
                } else if(Integer.parseInt(statsObjectFm, 16) == 128) {
                    if(objectFm.isOverFm2(128, 1))
                        if(!canFM)
                            canFM = true;
                }
            }
            //Solucion al inclemento de PA en los Gelanillos
            if (lvlJob < (int) Math.floor(template.getLevel() / 2))
                canFM = false; // On rate le FM si le m�tier n'est pas suffidant

            int diff = (int)Math.abs((float)currentTotalBase * 1.3f - (float)currentWeightTotal);
            if (canFM) {
                chances = Formulas.chanceFM(currentTotalBase, currentMinBase, currentWeightTotal, currentWeightStats, poid, diff, coef, statMax, OficioAccion.getStatBaseMins(objectFm.getModelo(), statsObjectFm), OficioAccion.currentStats(objectFm, statsObjectFm), x, bonusRune, statsAdd);
            } else {
                chances.add(0, 0);
                chances.add(1, 0);
            }
        }

        int aleatoryChance = Formulas.getRandomValue(1, 100), SC = chances.get(0), SN = chances.get(1);
        boolean successC = (aleatoryChance <= SC), successN = (aleatoryChance <= (SC + SN));

        if(objectFm.getPuit() >= statsAdd) {
            if(runeOrPotion.getModelo().getId() != 1558 && runeOrPotion.getModelo().getId() != 1557 && runeOrPotion.getModelo().getId() != 7438) {
                if(Formulas.getRandomValue(1, 2) == 1)
                    successC = true;
            }
        }
        if(objectFm.isOverFm(111, 0) && runeOrPotion.getModelo().getId() == 1557 || objectFm.isOverFm(128, 0) && runeOrPotion.getModelo().getId() == 1558) {
            successC = false;
            successN = false;
        }
        if (successC || successN) {
            int winXP = Formulas.calculXpWinFm(objectFm.getModelo().getLevel(), poid) * Configuracion.INSTANCE.getRATE_JOB();
            if (winXP > 0) {
                SM.addXp(this.player, winXP);
                ArrayList<OficioCaracteristicas> SMs = new ArrayList<>();
                SMs.add(SM);
                GestorSalida.GAME_SEND_JX_PACKET(this.player, SMs);
            }
        }
        //endregion

        //region succès critique
        if (successC) {
            int coef = 0;
            pwrPerte = 0;

            if (lvlElementRune == 1) coef = 50;
            else if (lvlElementRune == 25) coef = 65;
            else if (lvlElementRune == 50) coef = 85;
            if (isSigningRune)
                objectFm.addTxtStat(985, this.player.getName());

            if (lvlElementRune > 0 && lvlQuaStatsRune == 0) {
                for (EfectoHechizo effect : objectFm.getEffects()) {
                    if (effect.getEffectID() != 100)
                        continue;
                    String[] infos = effect.getArgs().split(";");
                    try {
                        int min = Integer.parseInt(infos[0], 16);
                        int max = Integer.parseInt(infos[1], 16);
                        int newMin = (min * coef) / 100;
                        int newMax = (max * coef) / 100;
                        if (newMin == 0)
                            newMin = 1;
                        String newRange = "1d" + (newMax - newMin + 1) + "+" + (newMin - 1);
                        String newArgs = Integer.toHexString(newMin) + ";" + Integer.toHexString(newMax) + ";-1;-1;0;" + newRange;
                        effect.setArgs(newArgs);
                        effect.setEffectID(statId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (lvlQuaStatsRune > 0 && lvlElementRune == 0) {
                boolean negative = false;
                int currentStats = viewActualStatsItem(objectFm, statsObjectFm);

                if (currentStats == 2) {
                    if (statsObjectFm.compareTo("7b") == 0) {
                        statsObjectFm = "98";
                        negative = true;
                    }
                    if (statsObjectFm.compareTo("77") == 0) {
                        statsObjectFm = "9a";
                        negative = true;
                    }
                    if (statsObjectFm.compareTo("7e") == 0) {
                        statsObjectFm = "9b";
                        negative = true;
                    }
                    if (statsObjectFm.compareTo("76") == 0) {
                        statsObjectFm = "9d";
                        negative = true;
                    }
                    if (statsObjectFm.compareTo("7c") == 0) {
                        statsObjectFm = "9c";
                        negative = true;
                    }
                    if (statsObjectFm.compareTo("7d") == 0) {
                        statsObjectFm = "99";
                        negative = true;
                    }
                }

                if (statStringObj.isEmpty()) {
                    String statsStr = statsObjectFm + "#" + Integer.toHexString(statsAdd) + "#0#0#0d0+" + statsAdd;
                    objectFm.clearStats();
                    objectFm.parseStringToStats(statsStr, false);
                } else {
                    String statsStr;
                    if (currentStats == 1 || currentStats == 2)
                        statsStr = objectFm.parseFMStatsString(statsObjectFm, objectFm, statsAdd, negative);
                    else
                        statsStr = objectFm.parseFMStatsString(statsObjectFm, objectFm, statsAdd, negative) + "," + statsObjectFm + "#" + Integer.toHexString(statsAdd) + "#0#0#0d0+" + statsAdd;

                    objectFm.clearStats();
                    objectFm.parseStringToStats(statsStr, false);
                }
            }

            String data = objectFm.getId() + "|1|" + objectFm.getModelo().getId() + "|" + objectFm.parseStatsString();

            if (!this.isRepeat)
                this.reConfigingRunes = -1;
            if (this.reConfigingRunes != 0 || this.broken)
                if(receiver == null)
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.player, 'O', "+", data);

            this.data = data;
            GestorSalida.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+" + objTemplateID);
            if(!secure) {
                GestorSalida.GAME_SEND_Ec_PACKET(this.player, "K;" + objTemplateID);
            }
        }
        //endregion
        //region Succès neutre
        else if (successN) {
            pwrPerte = 0;
            if (isSigningRune) {
                objectFm.addTxtStat(985, this.player.getName());
            }

            boolean negative = false;
            int currentStats = viewActualStatsItem(objectFm, statsObjectFm);

            if (currentStats == 2) {
                if (statsObjectFm.compareTo("7b") == 0) {
                    statsObjectFm = "98";
                    negative = true;
                }
                if (statsObjectFm.compareTo("77") == 0) {
                    statsObjectFm = "9a";
                    negative = true;
                }
                if (statsObjectFm.compareTo("7e") == 0) {
                    statsObjectFm = "9b";
                    negative = true;
                }
                if (statsObjectFm.compareTo("76") == 0) {
                    statsObjectFm = "9d";
                    negative = true;
                }
                if (statsObjectFm.compareTo("7c") == 0) {
                    statsObjectFm = "9c";
                    negative = true;
                }
                if (statsObjectFm.compareTo("7d") == 0) {
                    statsObjectFm = "99";
                    negative = true;
                }
            }
            if (statStringObj.isEmpty()) {
                String statsStr = statsObjectFm + "#" + Integer.toHexString(statsAdd) + "#0#0#0d0+" + statsAdd;
                objectFm.clearStats();
                objectFm.parseStringToStats(statsStr, false);
            } else {
                String statsStr;

                if (objectFm.getPuit() <= 0) {// EC en premier s'il n'y a pas de puits
                    statsStr = objectFm.parseStringStatsEC_FM(objectFm, statsAdd, runeOrPotion.getModelo().getId());
                    objectFm.clearStats();
                    objectFm.parseStringToStats(statsStr, false);
                    pwrPerte = currentWeightTotal - currentTotalWeigthBase(statsStr, objectFm);
                }
                if (currentStats == 1 || currentStats == 2)
                    statsStr = objectFm.parseFMStatsString(statsObjectFm, objectFm, statsAdd, negative);
                else
                    statsStr = objectFm.parseFMStatsString(statsObjectFm, objectFm, statsAdd, negative) + "," + statsObjectFm + "#" + Integer.toHexString(statsAdd) + "#0#0#0d0+" + statsAdd;
                objectFm.clearStats();
                objectFm.parseStringToStats(statsStr, false);
            }

            String data = objectFm.getId() + "|1|" + objectFm.getModelo().getId() + "|" + objectFm.parseStatsString();
            if (!this.isRepeat)
                this.reConfigingRunes = -1;
            if (this.reConfigingRunes != 0 || this.broken)
                if(receiver == null)
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.player, 'O', "+", data);

            this.data = data;
            GestorSalida.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "+" + objTemplateID);

            if (pwrPerte > 0) {
                GestorSalida.GAME_SEND_Ec_PACKET(this.player, "EF");
                GestorSalida.GAME_SEND_Im_PACKET(this.player, "0194");
            } else {
                GestorSalida.GAME_SEND_Ec_PACKET(this.player, "K;" + objTemplateID);
            }
        }
        //endregion
        //region Echec critique
        else {// EC
            pwrPerte = 0;

            if (!statStringObj.isEmpty()) {
                String statsStr = objectFm.parseStringStatsEC_FM(objectFm, statsAdd, -1);
                objectFm.clearStats();
                objectFm.parseStringToStats(statsStr, false);
                pwrPerte = currentWeightTotal - currentTotalWeigthBase(statsStr, objectFm);
            }

            String data = objectFm.getId() + "|1|" + objectFm.getModelo().getId() + "|" + objectFm.parseStatsString();
            if (!this.isRepeat)
                this.reConfigingRunes = -1;
            if (this.reConfigingRunes != 0 || this.broken)
                if(receiver == null)
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.player, 'O', "+", data);

            this.data = data;
            GestorSalida.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(), this.player.getId(), "-" + objTemplateID);
            GestorSalida.GAME_SEND_Ec_PACKET(this.player, "EF");

            if (pwrPerte > 0)
                GestorSalida.GAME_SEND_Im_PACKET(this.player, "0117");
            else
                GestorSalida.GAME_SEND_Im_PACKET(this.player, "0183");
        }
        //endregion



        objectFm.setPuit((objectFm.getPuit() + pwrPerte) - poid);
        int newQuantity = ingredients.get(idRune) == null ? 0 : ingredients.get(idRune) - 1;

        if (objectFm != null) {
            Mundo.addGameObject(objectFm, true);
            if(receiver == null) {
                this.player.addObjet(objectFm);
            } else {
                receiver.addObjet(objectFm);
            }
        }

        if(receiver == null) {
            this.decrementObjectQuantity(this.player, signingRune);
            this.decrementObjectQuantity(this.player, runeOrPotion);
            this.player.send("EmKO-" + objectFm.getId() + "|1|");
            this.ingredients.clear();
            this.player.send("EMKO+" + objectFm.getId() + "|1");
            this.ingredients.put(objectFm.getId(), 1);

            if (newQuantity >= 1) {
                this.player.send("EMKO+" + idRune + "|" + newQuantity);
                this.ingredients.put(idRune, newQuantity);
            } else {
                this.player.send("EMKO-" + idRune);
            }
        } else {
            if(items != null) {
                for(Entry<Jugador, ArrayList<Doble<Integer, Integer>>> entry : items.entrySet()) {
                    final Jugador player = entry.getKey();
                    for(Doble<Integer, Integer> couple : entry.getValue()) {
                        if(signingRune != null && signingRune.getId() == couple.getPrimero())
                            this.decrementObjectQuantity(player, signingRune);
                        if(runeOrPotion.getId() == couple.getPrimero())
                            this.decrementObjectQuantity(player, runeOrPotion);
                        //player.send("EMKO-" + couple.first);

                    }
                }
            }

            String stats = objectFm.parseStatsString();
            this.player.send("ErKO+" + objectFm.getId() + "|1|" + template + "|" + stats);
            receiver.send("ErKO+" + objectFm.getId() + "|1|" + template + "|" + stats);
            this.player.send("EcK;" + template + ";T" + receiver.getName() + ";" + stats);
            receiver.send("EcK;" + template + ";B" + this.player.getName() + ";" + stats);

            if(!successC) {
                receiver.send("EcEF");
            }
        }

        this.lastCraft.clear();
        this.lastCraft.putAll(this.ingredients);

        GestorSalida.GAME_SEND_Ow_PACKET(this.player);
        if (!isRepeat) this.setJobCraft(null);
        return true;
    }

    //region usefull function for fm
    private void decrementObjectQuantity(Jugador player, ObjetoJuego object) {
        if (object != null) {
            int newQua = object.getCantidad() - 1;
            if (newQua <= 0) {
                player.removeItem(object.getId(), object.getCantidad(), true, true);
                GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(player, object.getId());
            } else {
                object.setCantidad(newQua);
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(player, object);
            }
        }
    }

    public static int getStatBaseMaxs(ObjetoModelo objMod, String statsModif) {
        String[] split = objMod.getStrTemplate().split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            if (stats[0].toLowerCase().compareTo(statsModif.toLowerCase()) > 0) {
            } else if (stats[0].toLowerCase().compareTo(statsModif.toLowerCase()) == 0) {
                int max = Integer.parseInt(stats[2], 16);
                if (max == 0)
                    max = Integer.parseInt(stats[1], 16);
                return max;
            }
        }
        return 0;
    }

    public static int getStatBaseMins(ObjetoModelo objMod, String statsModif) {
        String[] split = objMod.getStrTemplate().split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            if (stats[0].toLowerCase().compareTo(statsModif.toLowerCase()) > 0) {
            } else if (stats[0].toLowerCase().compareTo(statsModif.toLowerCase()) == 0) {
                return Integer.parseInt(stats[1], 16);
            }
        }
        return 0;
    }

    public static int WeithTotalBaseMin(int objTemplateID) {
        int weight = 0;
        int alt = 0;
        String statsTemplate = "";
        statsTemplate = Mundo.mundo.getObjetoModelo(objTemplateID).getStrTemplate();
        if (statsTemplate == null || statsTemplate.isEmpty())
            return 0;
        String[] split = statsTemplate.split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            boolean sig = true;
            for (int a : Constantes.ARMES_EFFECT_IDS)
                if (a == statID) {
                    sig = false;
                    break;
                }
            if (!sig)
                continue;
            String jet = "";
            int value = 1;
            try {
                jet = stats[4];
                value = Formulas.getRandomJet(jet);
                try {
                    int min = Integer.parseInt(stats[1], 16);
                    value = min;
                } catch (Exception e) {
                    value = Formulas.getRandomJet(jet);
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            int statX = 1;
            if (statID == 125 || statID == 158 || statID == 174) {
                statX = 1;
            } else if (statID == 118 || statID == 126 || statID == 119
                    || statID == 123) {
                statX = 2;
            } else if (statID == 138 || statID == 666 || statID == 226
                    || statID == 220) // de
            // da�os,Trampas %
            {
                statX = 3;
            } else if (statID == 124 || statID == 176) {
                statX = 5;
            } else if (statID == 240 || statID == 241 || statID == 242
                    || statID == 243 || statID == 244)

            {
                statX = 7;
            } else if (statID == 210 || statID == 211 || statID == 212
                    || statID == 213 || statID == 214)

            {
                statX = 8;
            } else if (statID == 225) {
                statX = 15;
            } else if (statID == 178 || statID == 121) {
                statX = 20;
            } else if (statID == 115 || statID == 182) {
                statX = 30;
            } else if (statID == 117) {
                statX = 50;
            } else if (statID == 128) {
                statX = 90;
            } else if (statID == 111) {
                statX = 100;
            }
            weight = value * statX;
            alt += weight;
        }
        return alt;
    }

    public static int WeithTotalBase(int objTemplateID) {
        int weight = 0;
        int alt = 0;
        String statsTemplate = "";
        statsTemplate = Mundo.mundo.getObjetoModelo(objTemplateID).getStrTemplate();
        if (statsTemplate == null || statsTemplate.isEmpty())
            return 0;
        String[] split = statsTemplate.split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            boolean sig = true;
            for (int a : Constantes.ARMES_EFFECT_IDS)
                if (a == statID) {
                    sig = false;
                    break;
                }
            if (!sig)
                continue;
            String jet = "";
            int value = 1;
            try {
                jet = stats[4];
                value = Formulas.getRandomJet(jet);
                try {
                    int min = Integer.parseInt(stats[1], 16);
                    int max = Integer.parseInt(stats[2], 16);
                    value = min;
                    if (max != 0)
                        value = max;
                } catch (Exception e) {
                    e.printStackTrace();
                    value = Formulas.getRandomJet(jet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            int statX = 1;
            if (statID == 125 || statID == 158 || statID == 174) {
                statX = 1;
            } else if (statID == 118 || statID == 126 || statID == 119
                    || statID == 123) {
                statX = 2;
            } else if (statID == 138 || statID == 666 || statID == 226
                    || statID == 220) // de
            // da�os,Trampas %
            {
                statX = 3;
            } else if (statID == 124 || statID == 176) {
                statX = 5;
            } else if (statID == 240 || statID == 241 || statID == 242
                    || statID == 243 || statID == 244)

            {
                statX = 7;
            } else if (statID == 210 || statID == 211 || statID == 212
                    || statID == 213 || statID == 214)

            {
                statX = 8;
            } else if (statID == 225) {
                statX = 15;
            } else if (statID == 178 || statID == 121) {
                statX = 20;
            } else if (statID == 115 || statID == 182) {
                statX = 30;
            } else if (statID == 117) {
                statX = 50;
            } else if (statID == 128) {
                statX = 90;
            } else if (statID == 111) {
                statX = 100;
            }
            weight = value * statX;
            alt += weight;
        }
        return alt;
    }

    public static int currentWeithStats(ObjetoJuego obj, String statsModif) {
        for (Entry<Integer, Integer> entry : obj.getCaracteristicas().getEffects().entrySet()) {
            int statID = entry.getKey();
            if (Integer.toHexString(statID).toLowerCase().compareTo(statsModif.toLowerCase()) > 0) {
            } else if (Integer.toHexString(statID).toLowerCase().compareTo(statsModif.toLowerCase()) == 0) {
                int statX = 1;
                int coef = 1;
                int BaseStats = viewBaseStatsItem(obj, Integer.toHexString(statID));
                if (BaseStats == 2) {
                    coef = 3;
                } else if (BaseStats == 0) {
                    coef = 8;
                }
                if (statID == 125 || statID == 158 || statID == 174) {
                    statX = 1;
                } else if (statID == 118 || statID == 126 || statID == 119
                        || statID == 123)

                {
                    statX = 2;
                } else if (statID == 138 || statID == 666 || statID == 226
                        || statID == 220) // da�os,Trampas
                // %
                {
                    statX = 3;
                } else if (statID == 124 || statID == 176) {
                    statX = 5;
                } else if (statID == 240 || statID == 241 || statID == 242
                        || statID == 243 || statID == 244)

                {
                    statX = 7;
                } else if (statID == 210 || statID == 211 || statID == 212
                        || statID == 213 || statID == 214) {
                    statX = 8;
                } else if (statID == 225) {
                    statX = 15;
                } else if (statID == 178 || statID == 121) {
                    statX = 20;
                } else if (statID == 115 || statID == 182) {
                    statX = 30;
                } else if (statID == 117) {
                    statX = 50;
                } else if (statID == 128) {
                    statX = 90;
                } else if (statID == 111) {
                    statX = 100;
                }
                int Weight = entry.getValue() * statX * coef;
                return Weight;
            }
        }
        return 0;
    }

    public static int currentStats(ObjetoJuego obj, String statsModif) {
        for (Entry<Integer, Integer> entry : obj.getCaracteristicas().getEffects().entrySet()) {
            int statID = entry.getKey();
            if (Integer.toHexString(statID).toLowerCase().compareTo(statsModif.toLowerCase()) > 0) {
            } else if (Integer.toHexString(statID).toLowerCase().compareTo(statsModif.toLowerCase()) == 0) {
                return entry.getValue();
            }
        }
        return 0;
    }

    public static int currentTotalWeigthBase(String statsModelo, ObjetoJuego obj) {
        if (statsModelo.equalsIgnoreCase(""))
            return 0;
        int Weigth = 0;
        int Alto = 0;
        String[] split = statsModelo.split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            if (statID == 985 || statID == 988)
                continue;
            boolean xy = false;
            for (int a : Constantes.ARMES_EFFECT_IDS)
                if (a == statID) {
                    xy = true;
                    break;
                }
            if (xy)
                continue;
            String jet = "";
            int qua = 1;
            try {
                jet = stats[4];
                qua = Formulas.getRandomJet(jet);
                try {
                    int min = Integer.parseInt(stats[1], 16);
                    int max = Integer.parseInt(stats[2], 16);
                    qua = min;
                    if (max != 0)
                        qua = max;
                } catch (Exception e) {
                    e.printStackTrace();
                    qua = Formulas.getRandomJet(jet);
                }
            } catch (Exception e) {
                // Ok :/
            }
            int statX = 1;
            int coef = 1;
            int statsBase = viewBaseStatsItem(obj, stats[0]);
            if (statsBase == 2) {
                coef = 3;
            } else if (statsBase == 0) {
                coef = 2;
            }
            if (statID == 125 || statID == 158 || statID == 174) {
                statX = 1;
            } else if (statID == 118 || statID == 126 || statID == 119
                    || statID == 123) {
                statX = 2;
            } else if (statID == 138 || statID == 666 || statID == 226
                    || statID == 220) // de
            // da�os,Trampas %
            {
                statX = 3;
            } else if (statID == 124 || statID == 176) {
                statX = 5;
            } else if (statID == 240 || statID == 241 || statID == 242
                    || statID == 243 || statID == 244) {
                statX = 7;
            } else if (statID == 210 || statID == 211 || statID == 212
                    || statID == 213 || statID == 214)

            {
                statX = 8;
            } else if (statID == 225) {
                statX = 15;
            } else if (statID == 178 || statID == 121) {
                statX = 20;
            } else if (statID == 115 || statID == 182) {
                statX = 30;
            } else if (statID == 117) {
                statX = 50;
            } else if (statID == 128) {
                statX = 90;
            } else if (statID == 111) {
                statX = 100;
            }
            Weigth = qua * statX * coef;
            Alto += Weigth;
        }
        return Alto;
    }

    public static int getBaseMaxJet(int templateID, String statsModif) {
        ObjetoModelo t = Mundo.mundo.getObjetoModelo(templateID);
        String[] splitted = t.getStrTemplate().split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            if (stats[0].compareTo(statsModif) > 0)//Effets n'existe pas de base
            {
            } else if (stats[0].compareTo(statsModif) == 0)//L'effet existe bien !
            {
                int max = Integer.parseInt(stats[2], 16);
                if (max == 0)
                    max = Integer.parseInt(stats[1], 16);//Pas de jet maximum on prend le minimum
                return max;
            }
        }
        return 0;
    }

    public static int getActualJet(ObjetoJuego obj, String statsModif) {
        for (Entry<Integer, Integer> entry : obj.getCaracteristicas().getEffects().entrySet()) {
            if (Integer.toHexString(entry.getKey()).compareTo(statsModif) > 0)//Effets inutiles
            {
            } else if (Integer.toHexString(entry.getKey()).compareTo(statsModif) == 0)//L'effet existe bien !
            {
                int JetActual = entry.getValue();
                return JetActual;
            }
        }
        return 0;
    }

    public static byte viewActualStatsItem(ObjetoJuego obj, String stats)//retourne vrai si le stats est actuellement sur l'item
    {
        if (!obj.parseStatsString().isEmpty()) {
            for (Entry<Integer, Integer> entry : obj.getCaracteristicas().getEffects().entrySet()) {
                if (Integer.toHexString(entry.getKey()).compareTo(stats) > 0)//Effets inutiles
                {
                    if (Integer.toHexString(entry.getKey()).compareTo("98") == 0
                            && stats.compareTo("7b") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("9a") == 0
                            && stats.compareTo("77") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("9b") == 0
                            && stats.compareTo("7e") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("9d") == 0
                            && stats.compareTo("76") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("74") == 0
                            && stats.compareTo("75") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("99") == 0
                            && stats.compareTo("7d") == 0) {
                        return 2;
                    } else {
                    }
                } else if (Integer.toHexString(entry.getKey()).compareTo(stats) == 0)//L'effet existe bien !
                {
                    return 1;
                }
            }
            return 0;
        } else {
            return 0;
        }
    }

    public static byte viewBaseStatsItem(ObjetoJuego obj, String ItemStats)//retourne vrai si le stats existe de base sur l'item
    {

        String[] splitted = obj.getModelo().getStrTemplate().split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            if (stats[0].compareTo(ItemStats) > 0)//Effets n'existe pas de base
            {
                if (stats[0].compareTo("98") == 0
                        && ItemStats.compareTo("7b") == 0) {
                    return 2;
                } else if (stats[0].compareTo("9a") == 0
                        && ItemStats.compareTo("77") == 0) {
                    return 2;
                } else if (stats[0].compareTo("9b") == 0
                        && ItemStats.compareTo("7e") == 0) {
                    return 2;
                } else if (stats[0].compareTo("9d") == 0
                        && ItemStats.compareTo("76") == 0) {
                    return 2;
                } else if (stats[0].compareTo("74") == 0
                        && ItemStats.compareTo("75") == 0) {
                    return 2;
                } else if (stats[0].compareTo("99") == 0
                        && ItemStats.compareTo("7d") == 0) {
                    return 2;
                } else {
                }
            } else if (stats[0].compareTo(ItemStats) == 0)//L'effet existe bien !
            {
                return 1;
            }
        }
        return 0;
    }

    public static double getPwrPerEffet(int effect) {
        double r = switch (effect) {
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
        return r;
    }

    public static double getOverPerEffet(int effect) {
        double r = switch (effect) {
            case Constantes.STATS_ADD_PA -> 0.0;
            case Constantes.STATS_ADD_PM2, Constantes.STATS_ADD_VIE, Constantes.STATS_ADD_VITA, Constantes.STATS_ADD_PODS -> 404.0;
            case Constantes.STATS_MULTIPLY_DOMMAGE -> 0.0;
            case Constantes.STATS_ADD_CC, Constantes.STATS_CREATURE -> 3.0;
            case Constantes.STATS_ADD_PO -> 0.0;
            case Constantes.STATS_ADD_FORC, Constantes.STATS_ADD_AGIL, Constantes.STATS_ADD_INTE, Constantes.STATS_ADD_CHAN -> 101.0;
            case Constantes.STATS_ADD_PA2 -> 0.0;
            case Constantes.STATS_ADD_DOMA, Constantes.STATS_ADD_SOIN -> 5.0;
            case Constantes.STATS_ADD_EC -> 0.0;
            case Constantes.STATS_ADD_SAGE, Constantes.STATS_ADD_PROS -> 33.0;
            case Constantes.STATS_ADD_PM -> 0.0;
            case Constantes.STATS_ADD_PERDOM, Constantes.STATS_ADD_PDOM, Constantes.STATS_ADD_R_PVP_NEU, Constantes.STATS_ADD_R_PVP_FEU, Constantes.STATS_ADD_R_PVP_AIR, Constantes.STATS_ADD_R_PVP_EAU, Constantes.STATS_ADD_R_PVP_TER, Constantes.STATS_ADD_R_AIR, Constantes.STATS_ADD_R_EAU, Constantes.STATS_ADD_R_TER, Constantes.STATS_ADD_R_NEU, Constantes.STATS_ADD_R_FEU, Constantes.STATS_TRAPPER -> 50.0;
            case Constantes.STATS_ADD_AFLEE -> 0.0;
            case Constantes.STATS_ADD_MFLEE -> 0.0;
            case Constantes.STATS_ADD_INIT -> 1010.0;
            case Constantes.STATS_ADD_RP_TER, Constantes.STATS_ADD_RP_PVP_EAU, Constantes.STATS_ADD_RP_PVP_TER, Constantes.STATS_ADD_RP_NEU, Constantes.STATS_ADD_RP_FEU, Constantes.STATS_ADD_RP_AIR, Constantes.STATS_ADD_RP_EAU -> 16.0;
            case Constantes.STATS_TRAPDOM -> 6.0;
            case Constantes.STATS_ADD_RP_PVP_AIR -> 16.0;
            case Constantes.STATS_ADD_RP_PVP_FEU -> 16.0;
            case Constantes.STATS_ADD_RP_PVP_NEU -> 16.0;
            default -> 0.0;
        };
        return r;
    }
    //endregion
    /* *********************/

    //region Old craft with new formulas
    /*private synchronized void craftMaging(boolean isReapeat, int repeat) {
        GameClient.leaveExchange(this.player);
        if(this.player != null) return;
        GameObject gameObject = null, runeObject = null, potionObject = null, signingObject = null;

        //region Vérification de craft
        /* Type : 26 = potion pour les cac
           Type : 78 = rune
           Signature : Type 50 ou Id 7508 */

        /*for(int id : this.ingredients.keySet()) {
            GameObject object = World.world.getGameObject(id);
            int type = object.getTemplate().getType();

            if(gameObject == null && this.isAvailableObject(this.getJobStat().getTemplate().getId(), type))
                gameObject = object;
            else if(runeObject == null && type == 78)
                runeObject = object;
            else if(potionObject == null && type == 26)
                potionObject = object;
            else if(signingObject == null && object.getTemplate().getId() == 7508)
                signingObject = object;
        }

        if(gameObject == null || (runeObject == null && potionObject == null)) {
            GameClient.leaveExchange(this.player);
            return;
        }
        //endregion Vérification de craft

        /* Poids max : 100 si > EC à 100%
           EXO : Si ça dépasse la valeur de la stats originale ou si elle n'existe pas */
        /*if(runeObject != null) {
            Rune runeTemplate = Rune.getRuneById(runeObject.getTemplate().getId()); // On trouve le template de la rune qu'on souhaite appliqué à l'item

            if (runeTemplate == null) { // Si elle n'existe pas..
                //Ne devrait pas arriver.
                return;
            }

            //region Initialisation des variables principales
            String[] originalSplitStats = gameObject.getTemplate().getStrTemplate().split(","), actualObjectSplitStats = gameObject.parseStatsString().split(","); // Liste toutes les stats originale de l'objet
            byte originalSplitLenghtStats = (byte) originalSplitStats.length, containsPo = 0; // Taille de la liste des stats originale
            String concernedOriginalJet = null, concernedActualJet = null; // Jet originale concerner
            float PWRGmin = 0, PWRGactual = 0, PWRGmax = 0;

            for (String jet : originalSplitStats) { // On fait une iteration de chaque ligne de l'objet originale
                if(jet.isEmpty()) continue;
                int id = Short.parseShort(jet.split("#")[0], 16);

                if (id == runeTemplate.getCharacteristic()) // Si l'ID de la stats est égale à l'ID de la stats de la rune
                    concernedOriginalJet = jet; // On met la ligne concerner a jour
                if (id == Constant.STATS_ADD_PO)
                    containsPo = Byte.parseByte(jet.split("\\+")[1]);
            }
            for (String jet : actualObjectSplitStats) { // On fait une iteration de chaque ligne de l'objet actuel
                if(jet.isEmpty()) continue;
                short id = Short.parseShort(jet.split("#")[0], 16);

                if (id == Constant.STATS_CHANGE_BY || id == Constant.STATS_BUILD_BY) continue;

                Rune rune = Rune.getRuneByCharacteristicAndByWeight(id);
                if(rune != null) PWRGactual += this.getPWR(rune, jet, (byte) 1);

                if (id == runeTemplate.getCharacteristic()) // Si l'ID de la stats est égale à l'ID de la stats de la rune
                    concernedActualJet = String.valueOf(gameObject.getStats().getEffect(Integer.parseInt(jet.split("#")[0], 16))); // On met la ligne concerner a jour
            }
            //endregion Initialisation des variables principales

            //region Début des calculs des PWR & PWRG
            float PWRmin = concernedOriginalJet == null ? 0 : this.getPWR(runeTemplate, concernedOriginalJet, (byte) 0),
                    PWRactual = concernedActualJet == null ? 0 : this.getPWR(runeTemplate, concernedActualJet, (byte) 1),
                    PWRmax = concernedOriginalJet == null ? 0 : this.getPWR(runeTemplate, concernedOriginalJet, (byte) 2);

            for (String jet : originalSplitStats) {
                short id = Short.parseShort(jet.split("#")[0], 16);
                Rune rune = Rune.getRuneByCharacteristicAndByWeight(id);

                if(rune == null) continue;

                PWRGmin += this.getPWR(rune, jet, (byte) 0);
                PWRGmax += this.getPWR(rune, jet, (byte) 2);
            }
            //endregion Début des calculs des PWR & PWRG

            //region Réussite normal
            byte factorJet = 30, factorObject = 50, successLevel = 5;

            float stateOfJet = ((PWRactual + runeTemplate.getWeight() - PWRmin) * 100 / (PWRmax - PWRmin)),

                    PWGGActualMin = PWRGactual - PWRGmin < 0 ? -(PWRGactual - PWRGmin) : PWRGactual - PWRGmin,

                    stateOfObject = (float) Math.ceil(((PWRGactual - PWRGmin)* 100 / (PWRGmax - PWRGmin))),
                            //Math.ceil((PWGGActualMin) * 100 / (PWGGActualMin)),
                    //((PWGGActualMin <= 0 ? PWRGmin - PWRGactual : PWGGActualMin) * 100 /
                    //((PWRGmax - PWRGmin) == 0 ? 1 : (PWRGmax - PWRGmin))),
                    successJet = 1, successObject = 0;
            byte criticSuccess = 1, neutralSuccess = 50, criticFail = 1;

            //region Réussite exotique
            if (PWRactual > PWRmax)
                PWRactual = PWRmax + 2 * (PWRactual - PWRmax);

            if ((PWRactual + runeTemplate.getWeight() > PWRmax && PWRactual > 101)
                    || (gameObject.getStats().get(Constant.STATS_ADD_PA) >= 1 && runeTemplate.getWeight() == 100)
                    || (gameObject.getStats().get(Constant.STATS_ADD_PM) >= 1 && runeTemplate.getWeight() == 90)
                        || (gameObject.getStats().get(Constant.STATS_ADD_PO) >= containsPo && runeTemplate.getWeight() == 51)) {
                criticSuccess = 0;
                neutralSuccess = 0;
                criticFail = 100;
            } else if(PWRactual == 0 && PWRmax == 0 && runeTemplate.getWeight() > 50) {
                criticSuccess = 1;
                neutralSuccess = 0;
                criticFail = 99;
            } else if (PWRactual + runeTemplate.getWeight() > PWRmax && PWRactual < 101 && PWRactual + runeTemplate.getWeight() > 101) {
                criticSuccess = 1;
                neutralSuccess = 0;
                criticFail = 99;
            } else {
                byte exotic = 0;
                if (PWRactual + runeTemplate.getWeight() > PWRmax) {
                    factorJet = 40;
                    factorObject = 54;
                    successLevel = 5;
                    stateOfJet = 100;
                    exotic = 1;
                } else if (PWRmin == 0) {
                    factorJet = 40;
                    factorObject = 54;
                    successLevel = 5;
                    stateOfJet = 100;
                    exotic = 1;
                }
                //endregion Réussite exotique

                if (PWRmax - PWRmin == 0 && originalSplitLenghtStats == 1 && runeTemplate.getWeight() == 100) {
                    criticSuccess = 70;
                    neutralSuccess = 10;
                    criticFail = 20;
                } else {
                    if (PWRmax - PWRmin == 0 && originalSplitLenghtStats > 1)
                        stateOfJet = runeTemplate.getWeight();

                    if (stateOfJet >= 80 && stateOfJet <= 100)
                        successJet = factorJet * stateOfJet / 100;
                    else if (stateOfJet > 100)//Faux
                        successJet = factorJet * stateOfJet / 100;
                    else
                        successJet = stateOfJet / 4;

                    if (stateOfObject >= 50 && stateOfObject <= 100)
                        successObject = factorObject * stateOfObject / 100;
                    else
                        successObject = stateOfObject;

                    criticSuccess = (byte) Math.ceil(100 - (successJet + successObject + successLevel));

                    criticSuccess = criticSuccess < 0 ? 0 : criticSuccess;

                    if (criticSuccess > 50)
                        neutralSuccess = (byte) (100 - criticSuccess);
                    else if (criticSuccess < 25)
                        neutralSuccess = (byte) (50 - (25 - criticSuccess));

                    criticFail = (byte) (100 - (neutralSuccess + criticSuccess));

                    //region Limite
                    if(exotic == 0) {//normal
                        byte[] chances = runeTemplate.getChance();

                        if (criticSuccess > chances[0]) {
                            criticSuccess = chances[0];
                            neutralSuccess = chances[1];
                            criticFail = chances[2];
                        }
                    } else {//exotic
                        if (criticSuccess > 32) {
                            criticSuccess = 32;
                            neutralSuccess = 50;
                            criticFail = 18;
                        }
                    }
                    //endregion
                }
            }
            //endregion Réussite normal

            //region Perte neutre et critique
            RandomStats<Byte> randomStats = new RandomStats<>();
            randomStats.add((int) criticSuccess, (byte) 0);
            randomStats.add((int) neutralSuccess, (byte) 1);
            randomStats.add((int) criticFail, (byte) 2);
            byte result = randomStats.get();
            this.player.sendMessage("SC: " + criticSuccess + " | SN: "+ neutralSuccess + " | EC: " + criticFail + " | R: " + result);

            // success critique
            if (result == 0) {
                //TODO: Créer la fonction pour ajouter la rune à l'objet
                int newQuantity = this.ingredients.get(runeObject.getPlayerId()) - 1;
                this.player.removeByTemplateID(runeObject.getTemplate().getId(), 1);

                int winXP = Formulas.calculXpWinFm(gameObject.getTemplate().getLevel(), (int) Math.floor(runeTemplate.getWeight())) * Config.INSTANCE.getRATE_JOB;
                if (winXP > 0) this.SM.addXp(this.player, winXP);
                this.player.send("JX|" + this.SM.getTemplate().getId() + ";" + this.SM.get_lvl() + ";" + this.SM.getXpString(";") + ";");

                GameObject newObject = GameObject.getCloneObjet(gameObject, 1);
                this.player.removeItem(gameObject.getPlayerId(), 1, true, gameObject.getQuantity() == 1);

                if (signingObject != null) {
                    this.player.removeByTemplateID(signingObject.getTemplate().getId(), 1);
                    if (newObject.getTxtStat().containsKey(985))
                        newObject.getTxtStat().remove(985);
                    newObject.addTxtStat(985, this.player.getName());
                }

                newObject.getStats().addOneStat(runeTemplate.getCharacteristic(), runeTemplate.getBonus());

                if (this.player.addObjetWithOAKO(newObject, false))
                    World.world.addGameObject(newObject, true);

                SocketManager.GAME_SEND_Ow_PACKET(this.player);

                this.player.send("EmKO+" + newObject.getPlayerId() + "|1|" + newObject.getTemplate().getId() + "|" + newObject.parseStatsString());
                this.player.send("IO" + this.player.getId() + "|+" + newObject.getTemplate().getId()); // Icon tête joueur :  +/-
                this.player.send("EcK;" + newObject.getTemplate().getId());//Vous avez crée...

                this.ingredients.clear();
                this.player.send("EMKO+" + newObject.getPlayerId() + "|1");
                this.ingredients.put(newObject.getPlayerId(), 1);

                if (newQuantity >= 1) {
                    this.player.send("EMKO+" + runeObject.getPlayerId() + "|" + newQuantity);
                    this.ingredients.put(runeObject.getPlayerId(), newQuantity);
                }

                this.oldJobCraft = this.jobCraft;
                if (!isReapeat) this.setJobCraft(null);
                return;
            }

            // success neutre
            // echec critique
            float PWRloose = runeTemplate.getWeight();

            if (Formulas.getRandomValue(0, 1) == 1) {
                if (PWRloose > gameObject.getPuit()) {
                    gameObject.setPuit((int) Math.ceil(gameObject.getPuit() - PWRloose));
                } else if (PWRloose < gameObject.getPuit()) {
                    PWRloose = PWRloose - gameObject.getPuit();
                    gameObject.setPuit(0);
                }
            }

            for(int i : gameObject.getStats().getEffects().values())
                System.out.print(i + " - ");
            System.out.println("");

            ArrayList<String> listActual = new ArrayList<>();
            Collections.addAll(listActual, actualObjectSplitStats);

            //Formule calcul normal
            while (PWRloose > 0 && listActual.size() > 0) {
                if(listActual.get(0).isEmpty()) break;
                Map<Short, String> hashMapOriginal = new HashMap<>();

                listActual.clear();
                Collections.addAll(listActual, actualObjectSplitStats);
                for (String jet : originalSplitStats) hashMapOriginal.put(Short.parseShort(jet.split("#")[0], 16), jet);

                for (String jetActual : listActual) {
                    if(jetActual.isEmpty()) continue;
                    short id = Short.parseShort(jetActual.split("#")[0], 16);

                    if (id == Constant.STATS_CHANGE_BY || id == Constant.STATS_BUILD_BY) continue;

                    Rune runeForActualJet = Rune.getRuneByCharacteristic(id);

                    if (runeForActualJet == null) continue;

                    float PWRactualAct = this.getPWR(runeForActualJet, jetActual, (byte) 1);

                    for (String jetOriginal : hashMapOriginal.values()) {
                        Rune runeForOriginalJet = Rune.getRuneByCharacteristic(Short.parseShort(jetOriginal.split("#")[0], 16));

                        if (runeForOriginalJet == null) continue;
                        // success neutre
                        if (result == 1 && runeForActualJet.getCharacteristic() == runeForOriginalJet.getCharacteristic())
                            continue;

                        float PWRoriginalMax = this.getPWR(runeForOriginalJet, jetOriginal, (byte) 2);

                        if (PWRactualAct > PWRoriginalMax) {
                            float newPWRloose = PWRloose - Formulas.getRandomValue(1, (int) Math.ceil(PWRloose));
                            if(newPWRloose == 0 || gameObject.getStats().getEffects().get(((int) id)) == null) continue;
                            float newPWRSelectedJet = PWRactualAct - newPWRloose;
                            System.out.print("old: " +gameObject.getStats().get(id));
                            int loose = (int) -(gameObject.getStats().get(id) - Math.floor(newPWRSelectedJet / runeForActualJet.getWeight()));
                            gameObject.getStats().addOneStat(id, loose);
                            PWRloose = newPWRloose;
                            break;
                        }
                    }
                }

                for (String jet : actualObjectSplitStats) {
                    if(jet.isEmpty()) continue;
                    short id = Short.parseShort(jet.split("#")[0], 16);

                    if (id == Constant.STATS_CHANGE_BY || id == Constant.STATS_BUILD_BY) continue;

                    Rune rune = Rune.getRuneByCharacteristic(id);

                    if (rune == null) continue;


                    // success neutre
                    float PWRselectedJet = this.getPWR(rune, jet, (byte) 1);
                    if (result == 1 && runeTemplate.getCharacteristic() == rune.getCharacteristic()) continue;
                    if(gameObject.getStats().getEffects().get(((int) id)) == null) continue;
                    //TODO: Ou aussi voir la formule PWRligne / PWRrunes

                    if (Formulas.getRandomValue(1, 100) >= PWRselectedJet) {
                        if(PWRloose == 1) {
                            float newPWRSelectedJet = PWRselectedJet - PWRloose;
                            int loose = (int) (-(gameObject.getStats().get(id) - Math.floor(newPWRSelectedJet / rune.getWeight())));
                            gameObject.getStats().addOneStat(id, loose);
                            PWRloose = 0;
                        } else {
                            float newPWRloose = PWRloose - Formulas.getRandomValue(1, (int) Math.ceil(PWRloose));
                            if (newPWRloose == 0) continue;
                            float newPWRSelectedJet = PWRselectedJet - newPWRloose;
                            System.out.print("old: " + gameObject.getStats().get(id));
                            int loose = (int) (-(gameObject.getStats().get(id) - Math.floor(newPWRSelectedJet / rune.getWeight())));
                            gameObject.getStats().addOneStat(id, loose);
                            PWRloose = newPWRloose;
                        }
                    } else if(PWRselectedJet >= 100 && rune.getWeight() > 80) {
                        gameObject.getStats().addOneStat(id, - gameObject.getStats().get(id));
                        PWRloose = 0;
                    }
                }
            }
            if (PWRloose < 0) gameObject.setPuit((int) Math.ceil(PWRloose * (-1)));

            int newQuantity = this.ingredients.get(runeObject.getPlayerId()) - 1;
            this.player.removeByTemplateID(runeObject.getTemplate().getId(), 1);

            GameObject newObject = GameObject.getCloneObjet(gameObject, 1);

            for(int i : newObject.getStats().getEffects().values())
                System.out.print(i + " - ");
            System.out.println("");


            this.player.removeItem(gameObject.getPlayerId(), 1, true, gameObject.getQuantity() == 1);

            if (signingObject != null)
                this.player.removeByTemplateID(signingObject.getTemplate().getId(), 1);

            if(result == 1) { // succes neutre
                if (signingObject != null) {
                    if (newObject.getTxtStat().containsKey(985))
                        newObject.getTxtStat().remove(985);
                    newObject.addTxtStat(985, this.player.getName());
                }

                newObject.getStats().addOneStat(runeTemplate.getCharacteristic(), runeTemplate.getBonus());
                this.player.send("Im0194");//La magie n\'a pas parfaitement fonctionné..
            } else {
                this.player.send("Im0117");//La magie n'opère pas..
            }

            if (this.player.addObjetWithOAKO(newObject, false))
                World.world.addGameObject(newObject, true);

            SocketManager.GAME_SEND_Ow_PACKET(this.player);

            this.player.send("EmKO+" + newObject.getPlayerId() + "|1|" + newObject.getTemplate().getId() + "|" + newObject.parseStatsString());
            this.player.send("IO" + this.player.getId() + "|-" + newObject.getTemplate().getId()); // Icon tête joueur :  +/-


            this.ingredients.clear();
            this.player.send("EMKO+" + newObject.getPlayerId() + "|1");
            this.ingredients.put(newObject.getPlayerId(), 1);

            if (newQuantity >= 1) {
                this.player.send("EMKO+" + runeObject.getPlayerId() + "|" + newQuantity);
                this.ingredients.put(runeObject.getPlayerId(), newQuantity);
            } else {
                this.player.send("EMKO-" + runeObject.getPlayerId());
            }

            this.oldJobCraft = this.jobCraft;
            if (!isReapeat) this.setJobCraft(null);
        } else if(potionObject != null) {
            
        }
        //endregion
    }

    private float getPWR(Rune rune, String jet, byte type) {
        float weight = rune == null ? 1 : Rune.getRuneByCharacteristicAndByWeight(rune.getCharacteristic()).getWeight();
        System.out.println("W : " + weight);
        switch(type) {
            case 0:// min
                return weight * Formulas.getMinJet(jet.split("#")[4]);
            case 1:// actual
                return weight * Short.parseShort(jet.split("\\+")[jet.split("\\+").length - 1]);
            case 2:// max
                return weight * Formulas.getMaxJet(jet.split("#")[4]);
        }
        return 0;
    }

    private boolean isAvailableObject(int jobId, int type) {
        switch(jobId) {
            case 62://Cordomage
                return type == 10 || type == 11;
            case 63://Joaillomage
                return type == 1 || type == 9;
            case 64://Costumage
                return type == 16 || type == 17;
            case 43://Forgemage de Dagues
                return type == 5;
            case 44://Forgemage d'Epées
                return type == 6;
            case 45://Forgemage de Marteaux
                return type == 7;
            case 46://Forgemage de Pelles
                return type == 8;
            case 47://Forgemage de Haches
                return type == 19;
            case 48://Sculptemage d'Arcs
                return type == 2;
            case 49://Sculptemage de Baguettes
                return type == 3;
            case 50://Sculptemage de Bâtons
                return type == 4;
        }
        return false;
    }*/
    //endregion Old craft with new formulas
}