package org.alexandria.estaticos;

import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.Camino;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.configuracion.Constantes;
import org.alexandria.estaticos.Mision.*;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.objeto.ObjetoModelo;
import org.alexandria.estaticos.oficio.OficioCaracteristicas;
import org.alexandria.otro.Accion;
import org.alexandria.otro.Dopeul;
import org.alexandria.otro.utilidad.Doble;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Npc {
    private final int id;
    private int cellId;
    private byte orientation;
    private final NpcModelo template;

    public Npc(int id, int cellId, byte orientation, NpcModelo template) {
        this.id = id;
        this.cellId = cellId;
        this.orientation = orientation;
        this.template = template;
    }

    public int getId() {
        return id;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(byte orientation) {
        this.orientation = orientation;
    }

    public NpcModelo getTemplate() {
        return this.template;
    }

    public String parse(boolean alter, Jugador p) {
        StringBuilder sock = new StringBuilder();
        sock.append((alter ? "~" : "+"));
        sock.append(this.cellId).append(";");
        sock.append(this.orientation).append(";");
        sock.append("0").append(";");
        sock.append(this.id).append(";");
        sock.append(this.template.getId()).append(";");
        sock.append("-4").append(";");//type = NPC
        sock.append(this.template.getGfxId()).append("^");

        if (this.template.getScaleX() == this.template.getScaleY())
            sock.append(this.template.getScaleY()).append(";");
        else
            sock.append(this.template.getScaleX()).append("x").append(this.template.getScaleY()).append(";");

        sock.append(this.template.getSex()).append(";");
        sock.append((this.template.getColor1() != -1 ? Integer.toHexString(this.template.getColor1()) : "-1")).append(";");
        sock.append((this.template.getColor2() != -1 ? Integer.toHexString(this.template.getColor2()) : "-1")).append(";");
        sock.append((this.template.getColor3() != -1 ? Integer.toHexString(this.template.getColor3()) : "-1")).append(";");
        sock.append(this.template.getAccessories()).append(";");

        Mision q = this.template.getQuest();
        MisionJugador questPlayer = q == null ? null : p.getQuestPersoByQuest(q);

        if (q == null)
            sock.append(-1).append(";");
        else if (questPlayer == null)
            sock.append(4).append(";");
        else
            sock.append(-1).append(";");

        sock.append(this.template.getCustomArtWork());
        return sock.toString();
    }

    public static class NpcMobil extends Npc {

        private final static ArrayList<NpcMobil> movables = new ArrayList<>();

        private final Mapa map;
        private short position = 0;
        private String[] path;

        public NpcMobil(int id, int cellid, byte orientation, short mapid, NpcModelo template) {
            super(id, cellid, orientation, template);
            this.map = Mundo.mundo.getMap(mapid);
            this.path = template.getPath().split(";");
            NpcMobil.movables.add(this);
        }

        private void move() {
            char dir = this.path[this.position].charAt(0);
            short nbr;

            if(dir == 'E') {
                nbr = Short.parseShort(this.path[this.position].substring(1));

                for(Jugador player : this.map.getPlayers())
                    player.send("eUK" + this.getId() + "|" + nbr);
            } else {
                nbr = Short.parseShort(String.valueOf(this.path[this.position].charAt(1)));

                int oldCell = this.getCellId(), cell = this.getCellId();

                for (short i = 0; i <= nbr; i++) {
                    cell = Camino.getCaseIDFromDirrection(cell, NpcMobil.getDirByChar(dir), this.map);
                    if (!this.map.getCase(cell).isWalkable(true)) break;
                    oldCell = cell;
                }

                String path;
                try {
                    path = Camino.getShortestStringPathBetween(this.map, this.getCellId(), oldCell, 25);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                if (path == null)
                    return;

                for (Jugador player : this.map.getPlayers())
                    GestorSalida.GAME_SEND_GA_PACKET(player.getGameClient(), "0", "1", String.valueOf(this.getId()), path);

                this.setCellId(oldCell);
            }

            this.position++;

            if(this.position == this.path.length) {
                this.path = (NpcMobil.getPath(this.path).equals(this.getTemplate().getPath()) ? NpcMobil.inverseOfPath(this.getTemplate().getPath()).split(";") : this.getTemplate().getPath().split(";"));
                this.position = 0;
            }
        }

        private static String inverseOfPath(String arg) {
            String[] split = arg.split(";");
            StringBuilder var = new StringBuilder();

            for(int i = split.length - 1; i >= 0; i--) {
                String loc0 = split[i];

                if(loc0.contains("R"))
                    continue;

                switch (loc0.charAt(0)) {
                    case 'H' -> loc0 = loc0.replace("H", "B");
                    case 'B' -> loc0 = loc0.replace("B", "H");
                    case 'G' -> loc0 = loc0.replace("G", "D");
                    case 'D' -> loc0 = loc0.replace("D", "G");
                }

                var.append((var.length() == 0) ? "" : ";").append(loc0);
            }
            return var.toString();
        }

        private static String getPath(String[] path) {
            StringBuilder original = new StringBuilder();

            for(String arg : path)
                original.append((original.length() == 0) ? "" : ";").append(arg);

            return original.toString();
        }

        private static char getDirByChar(char letter) {
            return switch (letter) {
                case 'H' -> 'f';
                case 'B' -> 'b';
                case 'G' -> 'd';
                case 'D' -> 'h';
                default -> '?';
            };
        }

        public static void moveAll() {
            NpcMobil.movables.forEach(NpcMobil::move);
        }
    }

    public static class NpcModelo {
        private int id, bonus, gfxId, scaleX, scaleY, sex, color1, color2, color3;
        private String accessories;
        private final String nombre;
        private int extraClip, customArtWork;
        private String path;
        private Mision quest;
        private byte informations;

        private final Map<Integer, Integer> initQuestions = new HashMap<>();
        private final ArrayList<ObjetoModelo> sales = new ArrayList<>();
        private final List<Doble<ArrayList<Doble<Integer, Integer>>, ArrayList<Doble<Integer, Integer>>>> exchanges = new ArrayList<>();

        public NpcModelo(int id, int bonus, int gfxId, int scaleX, int scaleY, int sex, int color1, int color2, int color3, String accessories, int extraClip, int customArtWork, String questions,
                         String sales, String quest, String exchanges, String path, byte informations, String nombre) {
            this.id = id;
            this.nombre = nombre;
            this.bonus = bonus;
            this.gfxId = gfxId;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.sex = sex;
            this.color1 = color1;
            this.color2 = color2;
            this.color3 = color3;
            this.accessories = accessories;
            this.extraClip = extraClip;
            this.customArtWork = customArtWork;
            this.path = path;
            this.informations = informations;

            if (!quest.equalsIgnoreCase(""))
                this.quest = Mision.getQuestById(Integer.parseInt(quest));

            if (questions.split("\\|").length > 1) {
                for (String question : questions.split("\\|")) {
                    try {
                        initQuestions.put(Integer.parseInt(question.split(",")[0]), Integer.parseInt(question.split(",")[1]));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Mundo.mundo.logger.error("#1# Erreur sur une question id sur le PNJ d'id : " + id);
                    }
                }
            } else {
                if (questions.equalsIgnoreCase("")) this.initQuestions.put(-1, -1);
                else this.initQuestions.put(-1, Integer.parseInt(questions));
            }

            if (!sales.equals("")) {
                for (String obj : sales.split(",")) {
                    try {
                        ObjetoModelo template = Mundo.mundo.getObjetoModelo(Integer.parseInt(obj));
                        if (template != null)
                            this.sales.add(template);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Mundo.mundo.logger.error("#2# Erreur sur un item en vente sur le PNJ d'id : " + id);
                    }
                }
            }

            if(!exchanges.equals("")) {
                try	{
                    for(String data : exchanges.split("~")) {
                        ArrayList<Doble<Integer, Integer>> gives = new ArrayList<>(), gets = new ArrayList<>();

                        String[] split = data.split("\\|");
                        String give = split[1], get = split[0];

                        for(String obj : give.split(",")) {
                            split = obj.split(":");
                            gives.add(new Doble<>(Integer.parseInt(split[0]), Integer.parseInt(split[1])));
                        }

                        for(String obj : get.split(",")) {
                            split = obj.split(":");
                            gets.add(new Doble<>(Integer.parseInt(split[0]), Integer.parseInt(split[1])));
                        }
                        this.exchanges.add(new Doble<>(gets, gives));
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    Mundo.mundo.logger.error("#3# Erreur sur l'exchanges sur le PNJ d'id : " + id);
                }
            }
        }

        public boolean updateApariencia(int gfxId, int scale, int sex, int color1, int color2, int color3, String accessories) {
            this.gfxId = gfxId;
            this.scaleX = scale;
            this.scaleY = scale;
            this.sex = sex;
            this.color1 = color1;
            this.color2 = color2;
            this.color3 = color3;
            this.accessories = accessories;
            return Database.estaticos.getNpcTemplateData().updateAperiencia(this);
        }

        public void setInfos(int id, int bonus, int gfxId, int scaleX, int scaleY, int sex, int color1, int color2, int color3, String accessories, int extraClip, int customArtWork, String questions,
                             String sales, String quest, String exchanges, String path, byte informations) {
            this.id = id;
            this.bonus = bonus;
            this.gfxId = gfxId;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.sex = sex;
            this.color1 = color1;
            this.color2 = color2;
            this.color3 = color3;
            this.accessories = accessories;
            this.extraClip = extraClip;
            this.customArtWork = customArtWork;
            this.path = path;
            this.informations = informations;
            this.sales.clear();
            this.initQuestions.clear();
            this.exchanges.clear();
            if (!quest.equalsIgnoreCase(""))
                this.quest = Mision.getQuestById(Integer.parseInt(quest));

            if (questions.split("\\|").length > 1) {
                for (String question : questions.split("\\|")) {
                    try {
                        initQuestions.put(Integer.parseInt(question.split(",")[0]), Integer.parseInt(question.split(",")[1]));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Mundo.mundo.logger.error("#2# Erreur sur une question id sur le PNJ d'id : " + this.id);
                    }
                }
            } else {
                if (questions.equalsIgnoreCase("")) this.initQuestions.put(-1, -1);
                else this.initQuestions.put(-1, Integer.parseInt(questions));
            }

            if (!sales.equals("")) {
                for (String obj : sales.split(",")) {
                    try {
                        ObjetoModelo template = Mundo.mundo.getObjetoModelo(Integer.parseInt(obj));
                        if (template != null)
                            this.sales.add(template);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Mundo.mundo.logger.error("#2# Erreur sur un item en vente sur le PNJ d'id : " + id);
                    }
                }
            }

            if(!exchanges.equals("")) {
                try	{
                    for(String data : exchanges.split("~")) {
                        ArrayList<Doble<Integer, Integer>> gives = new ArrayList<>(), gets = new ArrayList<>();

                        String[] split = data.split("\\|");
                        String give = split[1], get = split[0];

                        for(String obj : give.split(",")) {
                            split = obj.split(":");
                            gives.add(new Doble<>(Integer.parseInt(split[0]), Integer.parseInt(split[1])));
                        }

                        for(String obj : get.split(",")) {
                            split = obj.split(":");
                            gets.add(new Doble<>(Integer.parseInt(split[0]), Integer.parseInt(split[1])));
                        }
                        this.exchanges.add(new Doble<>(gets, gives));
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    Mundo.mundo.logger.error("#3# Erreur sur l'exchanges sur le PNJ d'id : " + id);
                }
            }
        }

        public int getBonus() {
            return bonus;
        }

        public String getNombre() {
            return nombre;
        }

        public int getId() {
            return id;
        }

        public int getGfxId() {
            return gfxId;
        }

        public int getScaleX() {
            return scaleX;
        }

        public int getScaleY() {
            return scaleY;
        }

        public int getSex() {
            return sex;
        }

        public int getColor1() {
            return color1;
        }

        public int getColor2() {
            return color2;
        }

        public int getColor3() {
            return color3;
        }

        public String getAccessories() {
            return accessories;
        }

        public int getExtraClip() {
            return extraClip;
        }

        public void setExtraClip(int extraClip) {
            this.extraClip = extraClip;
        }

        public int getCustomArtWork() {
            return customArtWork;
        }

        public String getPath() {
            return path;
        }

        public Mision getQuest() {
            return quest;
        }

        public void setQuest(Mision quest) {
            this.quest = quest;
        }

        public byte getInformations() {
            return informations;
        }

        public int getInitQuestionId(int id) {
            if (this.initQuestions.get(id) == null)
                for (Integer entry : this.initQuestions.values())
                    return entry;
            return this.initQuestions.get(id);
        }

        public String getItemVendorList() {
            StringBuilder items = new StringBuilder();
            if (this.sales.isEmpty()) return "";
            for (ObjetoModelo obj : this.sales)
                items.append(obj.parseItemTemplateStats()).append("|");
            return items.toString();
        }

        public ArrayList<ObjetoModelo> getAllItem() {
            return sales;
        }

        public boolean addItemVendor(ObjetoModelo template) {
            if (this.sales.contains(template))
                return false;
            this.sales.add(template);
            return true;
        }

        public boolean removeItemVendor(int id) {

            this.sales.removeIf(template -> template.getId() == id);

            return true;
        }

        public boolean haveItem(int id) {
            for (ObjetoModelo template : sales)
                if (template.getId() == id)
                    return true;
            return false;
        }

        public ArrayList<Doble<Integer,Integer>> checkGetObjects(ArrayList<Doble<Integer,Integer>> objects) {
            if(this.exchanges == null) return null;
            boolean ok;
            int multiple = 0, newMultiple = 0;

            for(Doble<ArrayList<Doble<Integer, Integer>>, ArrayList<Doble<Integer, Integer>>> entry0 : this.exchanges) {
                ok = true;
                for(Doble<Integer, Integer> entry1 : entry0.getPrimero()) {
                    boolean ok1 = false;

                    for(Doble<Integer, Integer> entry2 : objects) {
                        if (entry1.getPrimero() == Mundo.getGameObject(entry2.getPrimero()).getModelo().getId() && (int) (entry2.getSegundo()) % entry1.getSegundo() == 0) {
                            ok1 = true;
                            newMultiple = entry2.getSegundo() / entry1.getSegundo();


                            if(multiple == 0 || newMultiple == multiple) {
                                multiple = newMultiple;
                            } else {
                                ok1 = false;
                            }
                        }
                    }

                    if(!ok1) {
                        ok = false;
                        break;
                    }
                }

                final int fMultiple = multiple;

                if(ok && objects.size() == entry0.getPrimero().size()) {
                    if (fMultiple != 1) {
                        return entry0.getSegundo().stream().map(give -> new Doble<>(give.getPrimero(), give.getSegundo() * fMultiple))
                                .collect(Collectors.toCollection(ArrayList::new));
                    } else {
                        return entry0.getSegundo();
                    }
                }
            }
            return null;
        }
    }

    public static class NpcPregunta {

        private final int id;
        private final String answers;
        private String args;
        private final String condition;
        private final String falseQuestion;

        public NpcPregunta(int id, String answers, String args, String condition,
                           String falseQuestion) {
            this.id = id;
            this.answers = answers;
            this.args = args;
            this.condition = condition;
            this.falseQuestion = falseQuestion;
        }

        public int getId() {
            return id;
        }

        public String getAnwsers() {
            return answers;
        }

        public String getArgs() {
            return args;
        }

        public void setArgs(String args) {
            this.args = args;
        }

        public String conditionsReponse(Jugador player) {
            StringBuilder str = new StringBuilder();
            try {
                String[] split = this.answers.split(";");
                boolean first = true;
                if (split.length > 0) {
                    for (String loc1 : split) {
                        if (loc1.equalsIgnoreCase(""))
                            continue;

                        NpcRespuesta answer = Mundo.mundo.getAnswers().get(Integer.parseInt(loc1));

                        if (answer == null)
                            continue;

                        boolean ok = true;

                        for (Accion action : answer.getActions()) {
                            switch (action.getId()) {
                                case 15: // Si on donne une clef
                                    String args = action.getArgs();
                                    int clef = Integer.parseInt(args.split(",")[2]);
                                    if (!player.hasItemTemplate(clef, 1))
                                        ok = false;
                                    break;
                                case 16: // Si on montre une clef
                                    args = action.getArgs();
                                    clef = Integer.parseInt(args.split(",")[2]);
                                    if (!player.hasItemTemplate(clef, 1))
                                        ok = false;
                                    break;
                                case 6: // Si on apprend un m?tier
                                    int mId = Integer.parseInt(action.getArgs().split(",")[0]);
                                    int cId = Integer.parseInt(action.getArgs().split(",")[1]);
                                    if (player.getCurMap().getId() != (short) cId)
                                        ok = false;
                                    else if (player.totalJobBasic() > 2)
                                        ok = false;
                                    else if (player.getMetierByID(mId) != null)
                                        ok = false; // S'il a d?j? le m?tier, alors on d?gage
                                    break;

                                case 40: // Si on apprend une qu?te
                                    if (!player.getQuestPerso().isEmpty()) {
                                        for (MisionJugador QP : player.getQuestPerso().values()) {
                                            if (QP.getQuest().getId() == Integer.parseInt(action.getArgs()))
                                                ok = false; // S'il a la qu?te on d?gage
                                        }
                                    }
                                    break;

                                case 997:
                                    int mId2 = Integer.parseInt(action.getArgs().split(",")[0]);
                                    int cId2 = Integer.parseInt(action.getArgs().split(",")[1]);
                                    if (player.getCurMap().getId() != (short) cId2)
                                        ok = false;
                                    else if (player.getMetierByID(mId2) != null)
                                        ok = false; // S'il a d?j? le m?tier, alors on d?gage
                                    else if (player.totalJobFM() > 2)
                                        ok = false;
                                    else if (Mundo.mundo.getMetier(mId2).isMaging()) // Sinon si c'est un m?tier de FM
                                    {
                                        OficioCaracteristicas metier = player.getMetierByID(Mundo.mundo.getMetierByMaging(mId2)); // On r?cup?re le m?tier associ?
                                        if (metier != null) // S'il existe
                                        {
                                            if (metier.get_lvl() < 65)
                                                ok = false; // S'il n'a pas le niveau on d?gage
                                        } else
                                            ok = false; // S'il n'existe pas on d?gage
                                    }
                                    break;
                            }
                        }

                        if (!player.getQuestPerso().isEmpty() && ok) // par qu?te
                        {
                            for (MisionJugador QP : player.getQuestPerso().values()) {
                                if (QP.isFinish() || QP.getQuest() == null)
                                    continue;
                                for (MisionEtapa q : QP.getQuest().getQuestSteps()) {
                                    if (q == null)
                                        continue;
                                    if (QP.isQuestStepIsValidate(q))
                                        continue;
                                    if (q.getValidationType() == answer.getId()) {
                                        if (q.getType() == 3) { // Si on doit donner des items
                                            for (Map.Entry<Integer, Integer> _entry : q.getItemNecessaryList().entrySet()) {
                                                if (!player.hasItemTemplate(_entry.getKey(), _entry.getValue())) {
                                                    ok = false;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (ok) {
                            String[][] s = Constantes.HUNTING_QUESTS;
                            for (String[] strings : s) {
                                if (Integer.parseInt(strings[6]) == answer.getId()) // Si la r?ponse est une traque de monstres
                                {
                                    for (MisionJugador QP : player.getQuestPerso().values()) {
                                        boolean k = true;
                                        if (QP.getQuest().getId() == Integer.parseInt(strings[5])) // S'il a la qu?te
                                        {
                                            k = false;
                                            ObjetoJuego suiveur = player.getObjetByPos(Constantes.ITEM_POS_PNJ_SUIVEUR);
                                            if (suiveur != null) // S'il a un pnj suiveur
                                            {
                                                ok = suiveur.getModelo().getId() == Integer.parseInt(strings[4]);
                                                break;
                                            } else
                                                ok = false;
                                        }
                                        if (k)
                                            ok = false;
                                    }
                                }
                            }
                        }

                        if (ok) // En fonction des r?ponses
                        {
                            Mapa map = player.getCurMap();
                            Integer mobId;
                            int certificat = -1;

                            java.util.Map<Integer, Doble<Integer, Integer>> dopeuls = Accion.getDopeul();
                            switch (answer.getId()) {
                                case 4643:
                                    if (player.getALvl() > 10)
                                        ok = false;
                                    break;
                                case 4644:
                                    if (player.getALvl() <= 10 || player.getALvl() > 20)
                                        ok = false;
                                    break;
                                case 4645:
                                    if (player.getALvl() <= 20 || player.getALvl() > 30)
                                        ok = false;
                                    break;
                                case 4646:
                                    if (player.getALvl() <= 30 || player.getALvl() > 40)
                                        ok = false;
                                    break;
                                case 4647:
                                    if (player.getALvl() <= 40 || player.getALvl() > 50)
                                        ok = false;
                                    break;
                                case 4648:
                                    if (player.getALvl() <= 50 || player.getALvl() > 60)
                                        ok = false;
                                    break;
                                case 4649:
                                    if (player.getALvl() <= 60 || player.getALvl() > 70)
                                        ok = false;
                                    break;
                                case 4650:
                                    if (player.getALvl() <= 70 || player.getALvl() > 80)
                                        ok = false;
                                    break;
                                case 4651:
                                    if (player.getALvl() <= 80 || player.getALvl() > 90)
                                        ok = false;
                                    break;
                                case 4652:
                                    if (player.getALvl() <= 90)
                                        ok = false;
                                    break;
                                case 4639:
                                case 4637:
                                    if (player.get_align() != 2)
                                        ok = false;
                                    break;
                                case 4641:
                                case 4638:
                                    if (player.get_align() != 1)
                                        ok = false;
                                    break;
                                case 4653:
                                case 4655:
                                    if (!player.hasItemTemplate(9811, 1))
                                        ok = false;
                                    break;
                                case 4654:
                                    if (!player.hasItemTemplate(9812, 1))
                                        ok = false;
                                    break;
                                case 4656:
                                    if (!player.hasItemTemplate(9812, 1) || player.get_align() != 2)
                                        ok = false;
                                    break;
                                case 4657:
                                    if (!player.hasItemTemplate(9812, 1) || player.get_align() != 1)
                                        ok = false;
                                    break;
                                case 7453:
                                    if (!player.hasItemTemplate(10563, 1))
                                        ok = false;
                                    break;
                                case 2769:
                                    if (!player.hasItemTemplate(8077, 10)
                                            || !player.hasItemTemplate(8076, 10)
                                            || !player.hasItemTemplate(8075, 10)
                                            || !player.hasItemTemplate(8064, 10))
                                        ok = false;
                                    break;
                                case 2754:
                                    if (player.getCurMap().getId() != (short) 9717)
                                        ok = false;
                                    else if (player.hasSpell(414))
                                        ok = false;
                                    else if (!player.hasItemTemplate(7904, 50)
                                            || !player.hasItemTemplate(7903, 50))
                                        ok = false;
                                    break;
                                case 2962:
                                    if (player.getCurMap().getId() != (short) 10199)
                                        ok = false;
                                    break;
                                case 2963:
                                    if (player.getCurMap().getId() != (short) 10213)
                                        ok = false;
                                    break;
                                case 3355:
                                    Mision q = Mision.getQuestById(198);
                                    if (q != null)
                                        if (player.getQuestPersoByQuest(q) != null)
                                            ok = false;
                                    break;
                                case 528:
                                    if (player.hasItemTemplate(1469, 1))
                                        ok = false;
                                    else if (player.getMetierByID(26) != null)
                                        ok = false;
                                    break;
                                case 530:
                                    if (!player.hasItemTemplate(1469, 1))
                                        ok = false;
                                    break;
                                case 531:
                                    if (!player.hasItemTemplate(1470, 1))
                                        ok = false;
                                    break;
                                case 532:
                                    if (!player.hasItemTemplate(1471, 1))
                                        ok = false;
                                    break;
                                case 534:
                                    if (!player.hasItemTemplate(1472, 1))
                                        ok = false;
                                    break;
                                case 2047:
                                    boolean metier30 = true;
                                    for (Map.Entry<Integer, OficioCaracteristicas> entry : player.getMetiers().entrySet()) {
                                        if (entry.getValue().get_lvl() < 30) {
                                            metier30 = false;
                                            break;
                                        }
                                    }
                                    if (player.hasItemTemplate(2107, 1))
                                        ok = false;
                                    else if (!player.hasItemTemplate(2106, 1))
                                        ok = false;
                                    else if (player.getMetierByID(36) != null)
                                        ok = false;
                                    else if (!metier30)
                                        ok = false;
                                    break;
                                case 2037:
                                    if (player.hasItemTemplate(2106, 1))
                                        ok = false;
                                    else if (!player.hasItemTemplate(2107, 1))
                                        ok = false;
                                    break;
                                case 2013:
                                    if (player.hasItemTemplate(2106, 1))
                                        ok = false;
                                    else if (player.hasItemTemplate(2107, 1))
                                        ok = false;
                                    break;
                                case 1968:
                                    if (!player.hasItemTemplate(2039, 1))
                                        ok = false;
                                    else if (!player.hasItemTemplate(2041, 1))
                                        ok = false;
                                    break;
                                case 1962:
                                    if (player.hasItemTemplate(2039, 1))
                                        ok = false;
                                    break;
                                case 1967:
                                    if ((!player.hasItemTemplate(2039, 1))
                                            || (player.hasItemTemplate(2041, 1)))
                                        ok = false;
                                    break;
                                case 1509: // S'entrainer avec un dopeul
                                    if (dopeuls.containsKey((int) map.getId())) {
                                        mobId = dopeuls.get((int) map.getId()).getPrimero();
                                    } else
                                        break;

                                    certificat = Constantes.getCertificatByDopeuls(mobId);
                                    if (certificat == -1)
                                        break;

                                    if (player.hasItemTemplate(certificat, 1)) {
                                        String date = player.getItemTemplate(certificat, 1).getTxtStat().get(Constantes.STATS_DATE);
                                        long timeStamp = Long.parseLong(date.split("#")[3]);
                                        if (Instant.now().toEpochMilli() - timeStamp <= 86400000) {
                                            ok = false;
                                        }
                                    }
                                    break;

                                case 1419: // se renseigner avec un dopeul
                                    if (dopeuls.containsKey((int) map.getId())) {
                                        mobId = dopeuls.get((int) map.getId()).getPrimero();
                                    } else
                                        break;

                                    certificat = Constantes.getCertificatByDopeuls(mobId);
                                    if (certificat == -1)
                                        break;

                                    if (player.hasItemTemplate(certificat, 1)) {
                                        String date = player.getItemTemplate(certificat, 1).getTxtStat().get(Constantes.STATS_DATE);
                                        long timeStamp;
                                        try {
                                            timeStamp = Long.parseLong(date.split("#")[3]);
                                        } catch (Exception ignored) {
                                            break;
                                        }
                                        if (Instant.now().toEpochMilli() - timeStamp <= 86400000) {
                                            ok = false;
                                        }
                                    }
                                    break;

                                case 6772: // Combattre chaque dopeul
                                    if (!player.getQuestPerso().isEmpty()) {
                                        for (MisionJugador QP : player.getQuestPerso().values()) {
                                            if (QP.getQuest().getId() == 470) {
                                                ok = false;
                                                break;
                                            }
                                        }
                                    }
                                    break;

                                case 3627: // Donner les objets, mapid 10437
                                    if (!player.getQuestPerso().isEmpty()) {
                                        for (MisionJugador QP : player.getQuestPerso().values()) {
                                            if (QP.getQuest().getId() == 232) {
                                                ok = false;
                                                break;
                                            }
                                        }
                                        ok = !ok;
                                    } else {
                                        ok = false;
                                    }
                                    break;

                                case 6701: // Si on a d?j? le trousseau de clef
                                    if (player.hasItemTemplate(10207, 1))
                                        ok = false;
                                    else if (Dopeul.hasOneDoplon(player) == -1)
                                        ok = false;
                                    break;

                                case 6699: // Apprendre le sort de sa classe
                                    Mapa curMap = player.getCurMap();
                                    int idMap = Mundo.mundo.getTempleByClasse(player.getClasse());
                                    if (curMap.getId() == (short) idMap) // Si on est dans le temple de notre classe
                                    {
                                        // si on a le doplon de classe
                                        ok = player.hasItemTemplate(Dopeul.getDoplonByClasse(player.getClasse()), 1) && !player.hasSpell(Constantes.getHechizosEspecialesClase(player.getClasse()));
                                    } else
                                        ok = false;
                                    break; // Faire sur l'action id
                                case 6599://Oublier un sort

                                    break;
                                case 7326: // Reset caract?ristique
                                    Mapa curMap2 = player.getCurMap();
                                    int idMap2 = Mundo.mundo.getTempleByClasse(player.getClasse());
                                    if (curMap2.getId() == (short) idMap2) // Si on est dans le temple de notre classe
                                    {
                                        if (!player.hasItemTemplate(Dopeul.getDoplonByClasse(player.getClasse()), 1))
                                            ok = false; // si on a le doplon de classe
                                        if (player.hasItemTemplate(10601, 1))
                                            ok = false; // Si on a le certificat de restat
                                    } else
                                        ok = false;
                                    break; // Faire sur l'action id
                            }
                        }
                        if (ok) {
                            if (!first)
                                str.append(";");
                            str.append(answer.getId());
                            first = false;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return str.toString();
        }

        public String parse(Jugador player) {
            if (!this.condition.equals("")) {
                if (!Mundo.mundo.getConditionManager().validConditions(player, this.condition)) {
                    if (this.falseQuestion.contains("|")) {
                        return Mundo.mundo.getNPCQuestion(Integer.parseInt(this.falseQuestion.split("\\|")[0])).parse(player);
                    } else {
                        return Mundo.mundo.getNPCQuestion(Integer.parseInt(this.falseQuestion)).parse(player);
                    }
                }
            }

            StringBuilder str = new StringBuilder(String.valueOf(this.id));

            if (!this.args.equals("")) str.append(";").append(parseArgs(this.args, player));
            if (!this.answers.equals("")) {
                String arg = this.conditionsReponse(player);
                if (!arg.isEmpty()) str.append("|").append(arg);
            }

            if (player.getItemTemplate(10207) != null) {
                for (String i : player.getItemTemplate(10207).getTxtStat().values().toString().split(",")) {
                    Mapa map = player.getCurMap();
                    if (map == null) continue;

                    Npc npc = map.getNpc((Integer) player.getExchangeAction().getValue());
                    if (npc == null) continue;

                    NpcModelo template = npc.getTemplate();
                    if (template == null) continue;

                    if (Dopeul.parseConditionTrousseau(i.replace(" ", ""), template.getId(), map.getId())) {
                        for (String rep : this.getAnwsers().split(";")) {
                            if(rep.isEmpty()) continue;
                            NpcRespuesta answer = Mundo.mundo.getNpcAnswer(Integer.parseInt(rep));
                            if (answer == null) continue;

                            for (Accion action : answer.getActions()) {
                                if (action.getId() == 15) {
                                    str.append(str.toString().contains("|") ? ";6605" : "|6605");
                                    break;
                                }
                                if (action.getId() == 16) {
                                    str.append(str.toString().contains("|") ? ";6604" : "|6604");
                                    break;
                                }
                            }

                            if(str.toString().contains("6604") || str.toString().contains("6605")) break;
                        }
                    }
                }
            }

            return str.toString();
        }

        private String parseArgs(String args, Jugador player) {
            String arg = args;
            arg = arg.replace("[name]", player.getStringVar("name"));
            arg = arg.replace("[bankCost]", player.getStringVar("bankCost"));
            arg = arg.replace("[points]", player.getStringVar("points"));
            arg = arg.replace("[pointsVote]", player.getStringVar("pointsVote"));
            arg = arg.replace("[nbrOnline]", player.getStringVar("nbrOnline"));
            arg = arg.replace("[align]", player.getStringVar("align"));
            return arg;
        }
    }

    public static class NpcRespuesta {

        private final int id;
        private ArrayList<Accion> actions = new ArrayList<>();
        private Mision quest = null;

        public NpcRespuesta(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public ArrayList<Accion> getActions() {
            return actions;
        }

        public void setActions(ArrayList<Accion> actions) {
            this.actions = actions;
        }

        public void addAction(Accion action0) {
            ArrayList<Accion> actions = new ArrayList<>(this.actions);

            for (Accion action1 : actions)
                if (action1.getId() == action0.getId())
                    getActions().remove(action1);

            this.actions.add(action0);
        }

        public boolean apply(Jugador player) {
            boolean leave = true;
            for (Accion action : this.getActions())
                leave = action.apply(player, null, -1, -1);
            return leave;
        }

        public boolean isAnotherDialog() {
            for (Accion action : getActions())
                if (action.getId() == 1) //1 = Discours NPC
                    return true;
            return false;
        }

        public Mision getQuest() {
            return quest;
        }

        public void setQuest(Mision quest) {
            this.quest = quest;
        }
    }
}