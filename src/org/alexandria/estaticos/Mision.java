package org.alexandria.estaticos;

import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.configuracion.Constantes;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.estaticos.juego.accion.AccionIntercambiar;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.objeto.ObjetoModelo;
import org.alexandria.otro.Accion;
import org.alexandria.otro.utilidad.Doble;
import org.alexandria.estaticos.Npc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Mision {

    //region Static method
    private static final Map<Integer, Mision> questList = new HashMap<>();

    public static Map<Integer, Mision> getQuestList() {
        return questList;
    }

    public static Mision getQuestById(int id) {
        return questList.get(id);
    }

    public static void addQuest(Mision quest) {
        questList.put(quest.getId(), quest);
    }
    //endregion

    private final int id;
    private final ArrayList<MisionEtapa> questSteps = new ArrayList<>();
    private final ArrayList<MisionObjetivo> questObjectifList = new ArrayList<>();
    private NpcModelo npc = null;
    private final ArrayList<Accion> actions = new ArrayList<>();
    private final boolean delete;
    private Doble<Integer, Integer> condition = null;

    public Mision(int id, String steps, String objectifs, int npc, String action, String args, boolean delete, String condition) {
        this.id = id;
        this.delete = delete;
        try {
            if (!steps.equalsIgnoreCase("")) {
                String[] split = steps.split(";");

                if (split.length > 0) {
                    for (String qEtape : split) {
                        MisionEtapa q_Etape = MisionEtapa.getQuestStepById(Integer.parseInt(qEtape));
                        q_Etape.setQuestData(this);
                        questSteps.add(q_Etape);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (!objectifs.equalsIgnoreCase("")) {
                String[] split = objectifs.split(";");

                if (split.length > 0) {
                    for (String qObjectif : split) {
                        questObjectifList.add(MisionObjetivo.getQuestObjectifById(Integer.parseInt(qObjectif)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!condition.equalsIgnoreCase("")) {
            try {
                String[] split = condition.split(":");
                if (split.length > 0) {
                    this.condition = new Doble<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.npc = Mundo.mundo.getNPCTemplate(npc);
        try {
            if (!action.equalsIgnoreCase("") && !args.equalsIgnoreCase("")) {
                String[] arguments = args.split(";");
                int nbr = 0;
                for (String loc0 : action.split(",")) {
                    int actionId = Integer.parseInt(loc0);
                    String arg = arguments[nbr];
                    actions.add(new Accion(actionId, arg, -1 + "", null));
                    nbr++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Mundo.mundo.logger.error("Erreur avec l action et les args de la quete " + this.id + ".");
        }
    }

    public int getId() {
        return id;
    }

    public boolean isDelete() {
        return this.delete;
    }

    public NpcModelo getNpcTemplate() {
        return npc;
    }

    public ArrayList<MisionEtapa> getQuestSteps() {
        return questSteps;
    }

    private boolean haveRespectCondition(MisionJugador questPlayer, MisionEtapa questStep) {
        switch (questStep.getCondition()) {
            case "1": //Valider les etapes d'avant
                boolean loc2 = true;
                for (MisionEtapa step : this.questSteps) {
                    if (step != null && step.getId() != questStep.getId() && !questPlayer.isQuestStepIsValidate(step)) {
                        loc2 = false;
                    }
                }
                return loc2;

            case "0":
                return true;
        }
        return false;
    }

    public String getGmQuestDataPacket(Jugador player) {
        MisionJugador questPlayer = player.getQuestPersoByQuest(this);
        int loc1 = getObjectifCurrent(questPlayer);
        int loc2 = getObjectifPrevious(questPlayer);
        int loc3 = getNextObjectif(MisionObjetivo.getQuestObjectifById(getObjectifCurrent(questPlayer)));
        StringBuilder str = new StringBuilder();
        str.append(id).append("|");
        str.append(loc1 > 0 ? loc1 : "");
        str.append("|");

        StringBuilder str_prev = new StringBuilder();
        boolean loc4 = true;
        // Il y a une exeption dans le code ici pour la seconde �tape de papotage
        for (MisionEtapa qEtape : questSteps) {
            if (qEtape.getObjectif() != loc1)
                continue;
            if (!haveRespectCondition(questPlayer, qEtape))
                continue;
            if (!loc4)
                str_prev.append(";");
            str_prev.append(qEtape.getId());
            str_prev.append(",");
            str_prev.append(questPlayer.isQuestStepIsValidate(qEtape) ? 1 : 0);
            loc4 = false;
        }
        str.append(str_prev);
        str.append("|");
        str.append(loc2 > 0 ? loc2 : "").append("|");
        str.append(loc3 > 0 ? loc3 : "");
        if (npc != null) {
            str.append("|");
            str.append(npc.getInitQuestionId(player.getCurMap().getId())).append("|");
        }
        return str.toString();
    }

    public MisionEtapa getCurrentQuestStep(MisionJugador questPlayer) {
        for (MisionEtapa step : getQuestSteps()) {
            if (!questPlayer.isQuestStepIsValidate(step)) {
                return step;
            }
        }
        return null;
    }

    private int getObjectifCurrent(MisionJugador questPlayer) {
        for (MisionEtapa step : questSteps) {
            if (!questPlayer.isQuestStepIsValidate(step)) {
                return step.getObjectif();
            }
        }
        return 0;
    }

    private int getObjectifPrevious(MisionJugador questPlayer) {
        if (questObjectifList.size() == 1)
            return 0;
        else {
            int previous = 0;
            for (MisionObjetivo qObjectif : questObjectifList) {
                if (qObjectif.getId() == getObjectifCurrent(questPlayer)) return previous;
                else previous = qObjectif.getId();
            }
        }
        return 0;
    }

    private int getNextObjectif(MisionObjetivo questObjectif) {
        if (questObjectif == null)
            return 0;
        for (MisionObjetivo objectif : questObjectifList) {
            if (objectif.getId() == questObjectif.getId()) {
                int index = questObjectifList.indexOf(objectif);
                if (questObjectifList.size() <= index + 1)
                    return 0;
                return questObjectifList.get(index + 1).getId();
            }
        }
        return 0;
    }

    public void applyQuest(Jugador player) {
        if (this.condition != null) {
            if (this.condition.getPrimero() == 1) { // Niveau
                if (player.getLevel() < this.condition.getSegundo()) {
                    GestorSalida.GAME_SEND_MESSAGE(player, "Votre niveau est insuffisant pour apprendre la quête.");
                    return;
                }
            }
        }

        MisionJugador questPlayer = new MisionJugador(Database.dinamicos.getQuestPlayerData().getNextId(), id, false, player.getId(), "");
        player.addQuestPerso(questPlayer);
        GestorSalida.GAME_SEND_Im_PACKET(player, "054;" + this.id);
        Database.dinamicos.getQuestPlayerData().add(questPlayer);
        GestorSalida.GAME_SEND_MAP_NPCS_GMS_PACKETS(player.getGameClient(), player.getCurMap());

        if (!this.actions.isEmpty()) {
            for (Accion aAction : this.actions) {
                aAction.apply(player, player, -1, -1);
            }
        }

        Database.dinamicos.getPlayerData().update(player);
    }

    public void updateQuestData(Jugador player, boolean validation, int type) {
        MisionJugador questPlayer = player.getQuestPersoByQuest(this);
        for (MisionEtapa questStep : this.questSteps) {
            if (questStep.getValidationType() != type || questPlayer.isQuestStepIsValidate(questStep)) //On a d�j� valid� la questEtape on passe
                continue;
            if (questStep.getObjectif() != getObjectifCurrent(questPlayer) || !haveRespectCondition(questPlayer, questStep))
                continue;

            boolean refresh = false;

            if (validation)
                refresh = true;
            switch (questStep.getType()) {
                case 3://Donner item
                    if (player.getExchangeAction() != null && player.getExchangeAction().getType() ==
                            AccionIntercambiar.TALKING_WITH && player.getCurMap().getNpc((Integer) player
                            .getExchangeAction().getValue()).getTemplate().getId() == questStep.getNpc().getId()) {
                        for (Entry<Integer, Integer> entry : questStep.getItemNecessaryList().entrySet()) {
                            if (player.hasItemTemplate(entry.getKey(), entry.getValue())) { //Il a l'item et la quantit�
                                player.removeByTemplateID(entry.getKey(), entry.getValue()); //On supprime donc
                                refresh = true;
                            }
                        }
                    }
                    break;

                case 0:
                case 1://Aller voir %
                case 9://Retourner voir %
                    if (questStep.getCondition().equalsIgnoreCase("1")) { //Valider les questEtape avant
                        if (player.getExchangeAction() != null && player.getExchangeAction().getType() == AccionIntercambiar.TALKING_WITH && player.getCurMap().getNpc((Integer) player.getExchangeAction().getValue()).getTemplate().getId() == questStep.getNpc().getId()) {
                            if (haveRespectCondition(questPlayer, questStep)) {
                                refresh = true;
                            }
                        }
                    } else {
                        if (player.getExchangeAction() != null && player.getExchangeAction().getType() == AccionIntercambiar.TALKING_WITH && player.getCurMap().getNpc((Integer) player.getExchangeAction().getValue()).getTemplate().getId() == questStep.getNpc().getId())
                            refresh = true;
                    }
                    break;

                case 6: // monstres
                    for (Entry<Integer, Short> entry : questPlayer.getMonsterKill().entrySet())
                        if (entry.getKey() == questStep.getMonsterId() && entry.getValue() >= questStep.getQua()) {
                            refresh = true;
                            break;
                        }
                    break;

                case 10://Ramener prisonnier
                    if (player.getExchangeAction() != null && player.getExchangeAction().getType() == AccionIntercambiar.TALKING_WITH && player.getCurMap().getNpc((Integer) player.getExchangeAction().getValue()).getTemplate().getId() == questStep.getNpc().getId()) {
                        ObjetoJuego follower = player.getObjetByPos(Constantes.ITEM_POS_PNJ_SUIVEUR);
                        if (follower != null) {
                            Map<Integer, Integer> itemNecessaryList = questStep.getItemNecessaryList();
                            for (Entry<Integer, Integer> entry2 : itemNecessaryList.entrySet()) {
                                if (entry2.getKey() == follower.getModelo().getId()) {
                                    refresh = true;
                                    player.setMascotte(0);
                                }
                            }
                        }
                    }
                    break;
            }

            if (refresh) {
                MisionObjetivo ansObjectif = MisionObjetivo.getQuestObjectifById(getObjectifCurrent(questPlayer));
                questPlayer.setQuestStepValidate(questStep);
                GestorSalida.GAME_SEND_Im_PACKET(player, "055;" + id);
                if (haveFinish(questPlayer, ansObjectif)) {
                    GestorSalida.GAME_SEND_Im_PACKET(player, "056;" + id);
                    applyButinOfQuest(player, ansObjectif);
                    questPlayer.setFinish(true);
                } else {
                    if (getNextObjectif(ansObjectif) != 0) {
                        if (questPlayer.overQuestStep(ansObjectif))
                            applyButinOfQuest(player, ansObjectif);
                    }
                }
                Database.dinamicos.getPlayerData().update(player);
            }
        }
    }

    private boolean haveFinish(MisionJugador questPlayer, MisionObjetivo questObjectif) {
        return questPlayer.overQuestStep(questObjectif) && getNextObjectif(questObjectif) == 0;
    }

    private void applyButinOfQuest(Jugador player, MisionObjetivo questObjectif) {
        long xp; int kamas;

        if ((xp = questObjectif.getXp()) > 0) { //Xp a donner
            player.addXp(xp * ((int) Configuracion.INSTANCE.getRATE_XP()));
            GestorSalida.GAME_SEND_Im_PACKET(player, "08;" + (xp * ((int) Configuracion.INSTANCE.getRATE_XP())));
            GestorSalida.GAME_SEND_STATS_PACKET(player);
        }

        if (questObjectif.getObjects().size() > 0) { //Item a donner
            for (Entry<Integer, Integer> entry : questObjectif.getObjects().entrySet()) {
                ObjetoModelo template = Mundo.mundo.getObjetoModelo(entry.getKey());
                int quantity = entry.getValue();
                ObjetoJuego object = template.createNewItem(quantity, false);

                if (player.addObjet(object, true)) {
                    Mundo.addGameObject(object, true);
                }
                GestorSalida.GAME_SEND_Im_PACKET(player, "021;" + quantity + "~" + template.getId());
            }
        }

        if ((kamas = questObjectif.getKamas()) > 0) { //Kams a donner
            player.setKamas(player.getKamas() + (long) kamas);
            GestorSalida.GAME_SEND_Im_PACKET(player, "045;" + kamas);
            GestorSalida.GAME_SEND_STATS_PACKET(player);
        }

        if (getNextObjectif(questObjectif) != questObjectif.getId()) { //On passe au nouveau objectif on applique les actions
            for (Accion action : questObjectif.getActions()) {
                action.apply(player, null, 0, 0);
            }
        }
    }

    public static class MisionEtapa {

        //region Static function
        private static final Map<Integer, MisionEtapa> questStepList = new HashMap<>();

        public  static Map<Integer, MisionEtapa> getQuestStepList() {
            return questStepList;
        }

        public static MisionEtapa getQuestStepById(int id) {
            return questStepList.get(id);
        }

        public static void addQuestStep(MisionEtapa step) {
            questStepList.put(step.getId(), step);
        }
        //endregion

        private final int id;
        private final short type;
        private final int objectif;
        private Mision quest = null;
        private final Map<Integer, Integer> itemNecessary = new HashMap<>();//ItemId,Qua
        private NpcModelo npc = null;
        private int monsterId;
        private short qua;
        private String condition = null;
        private final int validationType;

        public MisionEtapa(int id, int type, int objectif, String items, int npc, String monsters, String condition, int validationType) {
            this.id = id;
            this.type = (short) type;
            this.objectif = objectif;
            this.npc = Mundo.mundo.getNPCTemplate(npc);
            this.condition = condition;
            this.validationType = validationType;

            try {
                if (!items.equalsIgnoreCase("")) {
                    String[] split = items.split(";");

                    if (split.length > 0) {
                        for (String data : split) {
                            String[] loc1 = data.split(",");
                            this.itemNecessary.put(Integer.parseInt(loc1[0]), Integer.parseInt(loc1[1]));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (monsters.contains(",") && !monsters.equals("0")) {
                    String[] loc0 = monsters.split(",");
                    this.setMonsterId(Integer.parseInt(loc0[0]));
                    this.setQua(Short.parseShort(loc0[1])); // Des qu�tes avec le truc vide ! ><
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            MisionObjetivo questObjectif = MisionObjetivo.getQuestObjectifById(this.objectif);
            if (questObjectif != null) {
                questObjectif.addQuestStep(this);
            }
        }

        public int getId() {
            return id;
        }

        public short getType() {
            return type;
        }

        int getObjectif() {
            return objectif;
        }

        public Mision getQuestData() {
            return quest;
        }

        void setQuestData(Mision aQuest) {
            quest = aQuest;
        }

        public Map<Integer, Integer> getItemNecessaryList() {
            return itemNecessary;
        }

        public NpcModelo getNpc() {
            return npc;
        }

        public String getCondition() {
            return condition;
        }

        public int getMonsterId() {
            return monsterId;
        }

        private void setMonsterId(int monsterId) {
            this.monsterId = monsterId;
        }

        public short getQua() {
            return qua;
        }

        public void setQua(short qua) {
            this.qua = qua;
        }

        public int getValidationType() {
            return validationType;
        }
    }

    public static class MisionJugador {

        private final int id;
        private Mision quest = null;
        private boolean finish;
        private final Jugador player;
        private final Map<Integer, MisionEtapa> stepsValidate = new HashMap<>();
        private final Map<Integer, Short> monsterKill = new HashMap<>();

        public MisionJugador(int id, int quest, boolean finish, int player, String steps) {
            this.id = id;
            this.quest = Mision.getQuestById(quest);
            this.finish = finish;
            this.player = Mundo.mundo.getPlayer(player);

            try {
                String[] split = steps.split(";");
                if (split.length > 0) {
                    for (String data : split) {
                        if (!data.equalsIgnoreCase("")) {
                            MisionEtapa step = MisionEtapa.getQuestStepById(Integer.parseInt(data));
                            this.stepsValidate.put(step.getId(), step);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public int getId() {
            return id;
        }

        public Mision getQuest() {
            return quest;
        }

        public boolean isFinish() {
            return finish;
        }

        public void setFinish(boolean finish) {
            this.finish = finish;
            if (this.getQuest() != null && this.getQuest().isDelete()) {
                if (this.player != null && this.player.getQuestPerso() != null && this.player.getQuestPerso().containsKey(this.getId())) {
                    this.player.delQuestPerso(this.getId());
                    this.removeQuestPlayer();
                }
            } else if(this.getQuest() == null) {
                if (this.player.getQuestPerso().containsKey(this.getId())) {
                    this.player.delQuestPerso(this.getId());
                    this.removeQuestPlayer();
                }
            }
        }

        public Jugador getPlayer() {
            return player;
        }

        public boolean isQuestStepIsValidate(MisionEtapa step) {
            return stepsValidate.containsKey(step.getId());
        }

        public void setQuestStepValidate(MisionEtapa step) {
            if (!stepsValidate.containsKey(step.getId()))
                stepsValidate.put(step.getId(), step);
        }

        public String getQuestStepString() {
            StringBuilder str = new StringBuilder();
            int nb = 0;
            for (MisionEtapa step : this.stepsValidate.values()) {
                nb++;
                str.append(step.getId());
                if (nb < this.stepsValidate.size())
                    str.append(";");
            }
            return str.toString();
        }

        public Map<Integer, Short> getMonsterKill() {
            return monsterKill;
        }

        public boolean overQuestStep(MisionObjetivo qObjectif) {
            int nbrQuest = 0;
            for (MisionEtapa step : this.stepsValidate.values()) {
                if (step.getObjectif() == qObjectif.getId())
                    nbrQuest++;
            }
            return qObjectif.getSizeUnique() == nbrQuest;
        }

        public boolean removeQuestPlayer() {
            return Database.dinamicos.getQuestPlayerData().delete(this.id);
        }
    }

    public static class MisionObjetivo {

        //region Static method
        private static final Map<Integer, MisionObjetivo> questObjectifList = new HashMap<>();

        public static Map<Integer, MisionObjetivo> getQuestObjectifList() {
            return questObjectifList;
        }

        static MisionObjetivo getQuestObjectifById(int id) {
            return questObjectifList.get(id);
        }

        public static void addQuestObjectif(MisionObjetivo questObjectif) {
            questObjectifList.put(questObjectif.getId(), questObjectif);
        }
        //endregion

        private final int id;
        private final int xp;
        private final int kamas;
        private final Map<Integer, Integer> objects = new HashMap<>();
        private final ArrayList<Accion> actions = new ArrayList<>();
        private final ArrayList<MisionEtapa> questSteps = new ArrayList<>();

        public MisionObjetivo(int id, int xp, int kamas, String objects, String actions) {
            this.id = id;
            this.xp = xp;
            this.kamas = kamas;
            try {
                if (!objects.equalsIgnoreCase("")) {
                    String[] split = objects.split(";");
                    if (split.length > 0) {
                        for (String loc1 : split) {
                            if (!loc1.equalsIgnoreCase("")) {
                                if (loc1.contains(",")) {
                                    String[] loc2 = loc1.split(",");
                                    this.objects.put(Integer.parseInt(loc2[0]), Integer.parseInt(loc2[1]));
                                } else {
                                    this.objects.put(Integer.parseInt(loc1), 1);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (actions != null && !actions.equalsIgnoreCase("")) {
                    String[] split = actions.split(";");
                    if (split.length > 0) {
                        for (String loc1 : split) {
                            String[] loc2 = loc1.split("\\|");
                            this.actions.add(new Accion(Integer.parseInt(loc2[0]), loc2[1], "-1", null));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public int getId() {
            return id;
        }

        public int getXp() {
            return xp;
        }

        public int getKamas() {
            return kamas;
        }

        public Map<Integer, Integer> getObjects() {
            return objects;
        }

        ArrayList<Accion> getActions() {
            return actions;
        }

        int getSizeUnique() {
            int cpt = 0;
            ArrayList<Integer> id = new ArrayList<>();
            for (MisionEtapa step : this.questSteps) {
                if (!id.contains(step.getId())) {
                    id.add(step.getId());
                    cpt++;
                }
            }
            return cpt;
        }

        void addQuestStep(MisionEtapa step) {
            if (!this.questSteps.contains(step)) {
                this.questSteps.add(step);
            }
        }
    }
}