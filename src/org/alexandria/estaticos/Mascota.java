package org.alexandria.estaticos;

import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.configuracion.Constantes;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.objeto.ObjetoJuego;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Mascota {

    private final int idmodelo;
    private final int type;//0 ne mange rien, 1 mange des creatures, 2 mange des objets, 3 mange un groupe d'objet.
    private final String gap;//En heure 5,72 si type = 2 ou 3
    private final String statsUp;
    private final int max;
    private int maxStat;
    private final int gain;
    private final int deadtemplate;
    private final int epo;
    private final Map<Integer, ArrayList<Integer>> categ = new HashMap<>();    // si type 3 StatID|categID#categID;StatID2| ...
    private final Map<Integer, ArrayList<Integer>> template = new HashMap<>();    // si type 2 StatID|templateId#templateId#;StatID2| ...
    private final Map<Integer, ArrayList<Map<Integer, Integer>>> monster = new HashMap<>();    // si type 1 StatID|monsterID,qua#monsterID,qua;StatID2|monsterID,qua#monsterID,qua ...
    private final String jet;

    public Mascota(int Tid, int type, String gap, String statsUp, int max, int gain, int Dtemplate, int epo, String jet) {
        this.idmodelo = Tid;
        this.type = type;
        this.gap = gap;
        this.statsUp = statsUp;
        decompileStatsUpItem();
        this.max = max;
        this.gain = gain;
        this.deadtemplate = Dtemplate;
        this.epo = epo;
        this.jet = jet;
    }

    public int getIdmodelo() {
        return this.idmodelo;
    }

    public int getType() {
        return this.type;
    }

    public String getGap() {
        return this.gap;
    }

    public String getStatsUp() {
        return this.statsUp;
    }

    public int getMax() {
        return this.max;
    }

    public int getGain() {
        return this.gain;
    }

    public int getDeadTemplate() {
        return this.deadtemplate;
    }

    public int getEpo() {
        return this.epo;
    }

    public Map<Integer, ArrayList<Map<Integer, Integer>>> getMonsters() {
        return this.monster;
    }

    public int getNumbMonster(int StatID, int monsterID) {
        for (Entry<Integer, ArrayList<Map<Integer, Integer>>> ID : this.monster.entrySet()) {
            if (ID.getKey() == StatID) {
                for (Map<Integer, Integer> entry : ID.getValue()) {
                    for (Entry<Integer, Integer> monsterEntry : entry.entrySet()) {
                        if (monsterEntry.getKey() == monsterID) {
                            return monsterEntry.getValue();
                        }
                    }
                }
            }
        }
        return 0;
    }

    public void decompileStatsUpItem() {
        if (this.type == 3 || this.type == 2) {
            if (this.statsUp.contains(";"))//Plusieurs stats
            {
                for (String cut : this.statsUp.split(";"))//On coupe b2|41#49#62 puis 70|63#64
                {
                    String[] cut2 = cut.split("\\|");
                    int statsID = Integer.parseInt(cut2[0], 16);
                    ArrayList<Integer> ar = new ArrayList<>();

                    for (String categ : cut2[1].split("#")) {
                        int categID = Integer.parseInt(categ);
                        ar.add(categID);
                    }
                    if (this.type == 3)
                        this.categ.put(statsID, ar);
                    if (this.type == 2)
                        this.template.put(statsID, ar);
                }

            } else
            //Un seul stats
            {
                String[] cut2 = this.statsUp.split("\\|");//On coupe b2 puis 41#49#62
                int statsID = Integer.parseInt(cut2[0], 16);
                ArrayList<Integer> ar = new ArrayList<>();
                for (String categ : cut2[1].split("#")) {
                    int categID = Integer.parseInt(categ);
                    ar.add(categID);
                }
                if (this.type == 3)
                    this.categ.put(statsID, ar);
                if (this.type == 2)
                    this.template.put(statsID, ar);
            }
        } else if (this.type == 1) //StatID|monsterID,qua#monsterID,qua;StatID2|monsterID,qua#monsterID,qua
        {
            if (this.statsUp.contains(";"))//Plusieurs stats
            {
                for (String cut : this.statsUp.split(";"))//On coupe
                {
                    String[] cut2 = cut.split("\\|");
                    int statsID = Integer.parseInt(cut2[0], 16);
                    ArrayList<Map<Integer, Integer>> ar = new ArrayList<>();
                    for (String soustotal : cut2[1].split("#")) {
                        int monsterID = 0;
                        int qua = 0;
                        for (String Iqua : soustotal.split(",")) {
                            if (monsterID == 0) {
                                monsterID = Integer.parseInt(Iqua);
                            } else {
                                qua = Integer.parseInt(Iqua);
                                Map<Integer, Integer> Mqua = new HashMap<>();
                                Mqua.put(monsterID, qua);
                                ar.add(Mqua);
                                this.monster.put(statsID, ar);
                                monsterID = 0;
                            }
                        }
                    }
                }
            } else
            //Un seul stats 8a|64,50#65,50#68,50#72,50#96,50#97,40#99,40#179,40#182,10#181,10#180,1
            {
                String[] cut2 = this.statsUp.split("\\|");//On coupe 8a puis 64,50#65,50#68,50#72,50#96,50#97,40#99,40#179,40#182,10#181,10#180,1
                int statsID = Integer.parseInt(cut2[0], 16);
                ArrayList<Map<Integer, Integer>> ar = new ArrayList<>();
                for (String categ : cut2[1].split("#")) {
                    int monsterID = 0;
                    int qua = 0;
                    for (String Iqua : categ.split(",")) {
                        if (monsterID == 0) {
                            monsterID = Integer.parseInt(Iqua);
                        } else {
                            qua = Integer.parseInt(Iqua);
                            Map<Integer, Integer> Mqua = new HashMap<>();
                            Mqua.put(monsterID, qua);
                            ar.add(Mqua);
                            this.monster.put(statsID, ar);
                        }
                    }
                }
            }
        }
    }

    public boolean canEat(int Tid, int categID, int monsterId) {
        if (this.type == 1) {
            for (Map.Entry<Integer, ArrayList<Map<Integer, Integer>>> ID : this.monster.entrySet()) {
                for (Map<Integer, Integer> entry : ID.getValue()) {
                    for (Map.Entry<Integer, Integer> monsterEntry : entry.entrySet()) {
                        if (monsterEntry.getKey() != monsterId) continue;
                        return true;
                    }
                }
            }
            return false;
        }
        if (this.type == 2) {
            for (Map.Entry<Integer, ArrayList<Integer>> ID : this.template.entrySet()) {
                if (!ID.getValue().contains(Tid)) continue;
                return true;
            }
            return false;
        }
        if (this.type == 3) {
            for (Map.Entry<Integer, ArrayList<Integer>> ID : this.categ.entrySet()) {
                if (!ID.getValue().contains(categID)) continue;
                return true;
            }
            return false;
        }
        return false;
    }

    public int statsIdByEat(int Tid, int categID, int monsterId) {
        if (this.type == 1) {
            for (Map.Entry<Integer, ArrayList<Map<Integer, Integer>>> ID : this.monster.entrySet()) {
                for (Map<Integer, Integer> entry : ID.getValue()) {
                    for (Map.Entry<Integer, Integer> monsterEntry : entry.entrySet()) {
                        if (monsterEntry.getKey() != monsterId) continue;
                        return ID.getKey();
                    }
                }
            }
            return 0;
        }
        if (this.type == 2) {
            for (Map.Entry<Integer, ArrayList<Integer>> ID : this.template.entrySet()) {
                if (!ID.getValue().contains(Tid)) continue;
                return ID.getKey();
            }
            return 0;
        }
        if (this.type == 3) {
            for (Map.Entry<Integer, ArrayList<Integer>> ID : this.categ.entrySet()) {
                if (!ID.getValue().contains(categID)) continue;
                return ID.getKey();
            }
            return 0;
        }
        return 0;
    }

    public Map<Integer, String> generateNewtxtStatsForPets() {
        Map<Integer, String> txtStat = new HashMap<>();
        txtStat.put(Constantes.STATS_PETS_PDV, "a");
        txtStat.put(Constantes.STATS_PETS_DATE, "0");
        txtStat.put(Constantes.STATS_PETS_POIDS, "0");
        return txtStat;
    }

    public String getJet() {
        if(!this.jet.contains("\\|")) return jet;
        String[] split = this.jet.split("\\|");
        return split[Formulas.getRandomValue(1, split.length) - 1];
    }

    public int getMaxStat()
    {
        return maxStat;
    }

    public void setMaxStat(int maxStat)
    {
        this.maxStat = maxStat;
    }

    public static class MascotaEntrada {

        private final int objetoid;
        private final int modelo;
        private long lastEatDate;
        private int cantidadcomida;
        private int pdv;
        private int Poids;
        private int corpulencia;
        private final boolean isEupeoh;

        public MascotaEntrada(int Oid, int modelo, long lastEatDate, int cantidadcomida, int pdv, int corpulencia, boolean isEPO) {
            this.objetoid = Oid;
            this.modelo = modelo;
            this.lastEatDate = lastEatDate;
            this.cantidadcomida = cantidadcomida;
            this.pdv = pdv;
            this.corpulencia = corpulencia;
            getCurrentStatsPoids();
            this.isEupeoh = isEPO;
        }

        public int getObjetoid() {
            return this.objetoid;
        }

        public int getModelo() {
            return modelo;
        }

        public long getLastEatDate() {
            return this.lastEatDate;
        }

        public int getCantidadcomida() {
            return this.cantidadcomida;
        }

        public int getPdv() {
            return this.pdv;
        }

        public int getCorpulencia() {
            return this.corpulencia;
        }

        public boolean getIsEupeoh() {
            return this.isEupeoh;
        }

        public String parseLastEatDate() {
            String hexDate = "#";
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = formatter.format(this.lastEatDate);

            String[] split = date.split("\\s");

            String[] split0 = split[0].split("-");
            hexDate += Integer.toHexString(Integer.parseInt(split0[0])) + "#";
            int mois = Integer.parseInt(split0[1]) - 1;
            int jour = Integer.parseInt(split0[2]);
            hexDate += Integer.toHexString(Integer.parseInt((mois < 10 ? "0" + mois : mois)
                    + "" + (jour < 10 ? "0" + jour : jour)))
                    + "#";

            String[] split1 = split[1].split(":");
            String heure = split1[0] + split1[1];
            hexDate += Integer.toHexString(Integer.parseInt(heure));

            return hexDate;
        }

        public int parseCorpulence() {
            if (this.corpulencia > 0 || this.corpulencia < 0) {
                return 7;
            }
            return 0;
        }

        public int getCurrentStatsPoids() {
            ObjetoJuego obj = Mundo.getGameObject(this.objetoid);
            if (obj == null) {
                return 0;
            }
            int cumul = 0;
            for (Map.Entry<Integer, Integer> entry : obj.getCaracteristicas().getEffects().entrySet()) {
                if (entry.getKey() == Integer.parseInt("320", 16) || entry.getKey() == Integer.parseInt("326", 16) || entry.getKey() == Integer.parseInt("328", 16)) continue;
                if (entry.getKey() == Integer.parseInt("8a", 16)) {
                    cumul += 2 * entry.getValue();
                    continue;
                }
                if (entry.getKey() == Integer.parseInt("7c", 16)) {
                    cumul += 3 * entry.getValue();
                    continue;
                }
                if (entry.getKey() == Integer.parseInt("d2", 16) || entry.getKey() == Integer.parseInt("d3", 16) || entry.getKey() == Integer.parseInt("d4", 16) || entry.getKey() == Integer.parseInt("d5", 16) || entry.getKey() == Integer.parseInt("d6", 16)) {
                    cumul += 4 * entry.getValue();
                    continue;
                }
                if (entry.getKey() == Integer.parseInt("b2", 16) || entry.getKey() == Integer.parseInt("70", 16)) {
                    cumul += 8 * entry.getValue();
                    continue;
                }
                cumul += entry.getValue().intValue();
            }
            this.Poids = cumul;
            return this.Poids;
        }

        public int getMaxStat() {
            return Mundo.mundo.getPets(this.modelo).getMax();
        }

        public void looseFight(Jugador player) {
            ObjetoJuego obj = Mundo.getGameObject(this.objetoid);
            if (obj == null)
                return;
            Mascota pets = Mundo.mundo.getPets(obj.getModelo().getId());
            if (pets == null)
                return;

            this.pdv--;
            obj.getTxtStat().remove(Constantes.STATS_PETS_PDV);
            obj.getTxtStat().put(Constantes.STATS_PETS_PDV, Integer.toHexString((Math.max(this.pdv, 0))));

            if (this.pdv <= 0) {
                this.pdv = 0;
                obj.getTxtStat().remove(Constantes.STATS_PETS_PDV);
                obj.getTxtStat().put(Constantes.STATS_PETS_PDV, Integer.toHexString(0));//Mise a 0 des pdv

                if (pets.getDeadTemplate() == 0)// Si Pets DeadTemplate = 0 remove de l'item et pet entry
                {
                    Mundo.mundo.removeGameObject(obj.getId());
                    player.removeItem(obj.getId());
                    GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(player, obj.getId());
                    if (player.addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
                        Mundo.addGameObject(obj, true);
                } else {
                    obj.setModelo(pets.getDeadTemplate());
                    if (obj.getPosicion() == Constantes.ITEM_POS_FAMILIER) {
                        obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                        GestorSalida.GAME_SEND_OBJET_MOVE_PACKET(player, obj);
                    }
                }
                GestorSalida.GAME_SEND_Im_PACKET(player, "154");
            }
            GestorSalida.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(player, obj);
            Database.dinamicos.getPetData().update(this);
        }

        public void eat(Jugador p, int min, int max, int statsID, ObjetoJuego feed) {
            ObjetoJuego obj = Mundo.getGameObject(this.objetoid);
            if (obj == null)
                return;
            Mascota pets = Mundo.mundo.getPets(obj.getModelo().getId());
            if (pets == null)
                return;

            if (this.corpulencia <= 0)//Si il est maigrichon (X repas rat?s) on peu le nourrir plusieurs fois
            {
                //Update du petsEntry
                this.lastEatDate = Instant.now().toEpochMilli();
                this.corpulencia++;
                this.cantidadcomida++;
                //Update de l'item
                obj.getTxtStat().remove(Constantes.STATS_PETS_POIDS);
                obj.getTxtStat().put(Constantes.STATS_PETS_POIDS,Integer.toString(this.corpulencia));
                obj.getTxtStat().remove(Constantes.STATS_PETS_DATE);
                obj.getTxtStat().put(Constantes.STATS_PETS_DATE,this.getLastEatDate()+"");
                GestorSalida.GAME_SEND_Im_PACKET(p, "029");
                if (this.cantidadcomida >= 3) {
                    //Update de l'item
                    if ((this.getIsEupeoh() ? pets.getMax() * 1.1 : pets.getMax()) > this.getCurrentStatsPoids())//Si il est sous l'emprise d'EPO on augmente de +10% le jet maximum
                    {
                        if (obj.getCaracteristicas().getEffects().containsKey(statsID)) {
                            int value = obj.getCaracteristicas().getEffects().get(statsID)
                                    + Mundo.mundo.getPets(Mundo.getGameObject(this.objetoid).getModelo().getId()).getGain();
                            if (value > this.getMaxStat())
                                value = this.getMaxStat();
                            obj.getCaracteristicas().getEffects().remove(statsID);
                            obj.getCaracteristicas().addOneStat(statsID, value);
                        } else
                            obj.getCaracteristicas().addOneStat(statsID, pets.getGain());
                    }
                    this.cantidadcomida = 0;
                }
            } else if (((this.lastEatDate + (min * 3600000)) > Instant.now().toEpochMilli())
                    && this.corpulencia >= 0)//Si il n'est pas maigrichon, et on le nourri trop rapidement
            {
                //Update du petsEntry
                this.lastEatDate = Instant.now().toEpochMilli();
                this.corpulencia++;
                //Update de l'item
                obj.getTxtStat().remove(Constantes.STATS_PETS_POIDS);
                obj.getTxtStat().put(Constantes.STATS_PETS_POIDS,Integer.toString(this.corpulencia));
                obj.getTxtStat().remove(Constantes.STATS_PETS_DATE);
                obj.getTxtStat().put(Constantes.STATS_PETS_DATE,this.getLastEatDate()+"");
                if (corpulencia == 1) {
                    this.cantidadcomida++;
                    GestorSalida.GAME_SEND_Im_PACKET(p, "026");
                } else {
                    this.pdv--;
                    obj.getTxtStat().remove(Constantes.STATS_PETS_PDV);
                    obj.getTxtStat().put(Constantes.STATS_PETS_PDV, Integer.toHexString((Math.max(this.pdv, 0))));
                    GestorSalida.GAME_SEND_Im_PACKET(p, "027");
                }
                if (this.cantidadcomida >= 3) {
                    //Update de l'item
                    if ((this.getIsEupeoh() ? pets.getMax() * 1.1 : pets.getMax()) > this.getCurrentStatsPoids())//Si il est sous l'emprise d'EPO on augmente de +10% le jet maximum
                    {
                        if (obj.getCaracteristicas().getEffects().containsKey(statsID)) {
                            int value = obj.getCaracteristicas().getEffects().get(statsID)
                                    + Mundo.mundo.getPets(Mundo.getGameObject(this.objetoid).getModelo().getId()).getGain();
                            if (value > this.getMaxStat())
                                value = this.getMaxStat();
                            obj.getCaracteristicas().getEffects().remove(statsID);
                            obj.getCaracteristicas().addOneStat(statsID, value);
                        } else
                            obj.getCaracteristicas().addOneStat(statsID, pets.getGain());
                    }
                    this.cantidadcomida = 0;
                }
            } else if (((this.lastEatDate + (min * 3600000)) < Instant.now().toEpochMilli())
                    && this.corpulencia >= 0)//Si il n'est pas maigrichon, et que le temps minimal est ?coul?
            {
                //Update du petsEntry
                this.lastEatDate = Instant.now().toEpochMilli();
                obj.getTxtStat().remove(Constantes.STATS_PETS_DATE);
                obj.getTxtStat().put(Constantes.STATS_PETS_DATE,this.getLastEatDate()+"");

                if (statsID != 0)
                    this.cantidadcomida++;
                else
                    return;
                if (this.cantidadcomida >= 3) {
                    //Update de l'item
                    if ((this.getIsEupeoh() ? pets.getMax() * 1.1 : pets.getMax()) > this.getCurrentStatsPoids())//Si il est sous l'emprise d'EPO on augmente de +10% le jet maximum
                    {
                        if (obj.getCaracteristicas().getEffects().containsKey(statsID)) {
                            int value = obj.getCaracteristicas().getEffects().get(statsID)
                                    + Mundo.mundo.getPets(Mundo.getGameObject(this.objetoid).getModelo().getId()).getGain();
                            if (value > this.getMaxStat())
                                value = this.getMaxStat();
                            obj.getCaracteristicas().getEffects().remove(statsID);
                            obj.getCaracteristicas().addOneStat(statsID, value);
                        } else
                            obj.getCaracteristicas().addOneStat(statsID, pets.getGain());
                    }
                    this.cantidadcomida = 0;
                }
                GestorSalida.GAME_SEND_Im_PACKET(p, "032");
            }

            if (this.pdv <= 0) {
                this.pdv = 0;
                obj.getTxtStat().remove(Constantes.STATS_PETS_PDV);
                obj.getTxtStat().put(Constantes.STATS_PETS_PDV, Integer.toHexString((Math.max(this.pdv, 0))));//Mise a 0 des pdv
                if (pets.getDeadTemplate() == 0)// Si Pets DeadTemplate = 0 remove de l'item et pet entry
                {
                    Mundo.mundo.removeGameObject(obj.getId());
                    p.removeItem(obj.getId());
                    GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(p, obj.getId());
                } else {
                    obj.setModelo(pets.getDeadTemplate());

                    if (obj.getPosicion() == Constantes.ITEM_POS_FAMILIER) {
                        obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                        GestorSalida.GAME_SEND_OBJET_MOVE_PACKET(p, obj);
                    }
                }
                GestorSalida.GAME_SEND_Im_PACKET(p, "154");
            }
            if (obj.getTxtStat().containsKey(Constantes.STATS_PETS_REPAS)) {
                obj.getTxtStat().remove(Constantes.STATS_PETS_REPAS);
                obj.getTxtStat().put(Constantes.STATS_PETS_REPAS, Integer.toHexString(feed.getModelo().getId()));
            } else {
                obj.getTxtStat().put(Constantes.STATS_PETS_REPAS, Integer.toHexString(feed.getModelo().getId()));
            }
            GestorSalida.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(p, obj);
            Database.dinamicos.getObjectData().update(obj);
            Database.dinamicos.getPetData().update(this);
        }

        public void eatSouls(Jugador p, Map<Integer, Integer> souls) {
            ObjetoJuego obj = Mundo.getGameObject(this.objetoid);
            if (obj == null)
                return;
            Mascota pet = Mundo.mundo.getPets(obj.getModelo().getId());
            if (pet == null || pet.getType() != 1)
                return;
            //Ajout a l'item les SoulStats tu?s
            try {
                for (Entry<Integer, Integer> entry : souls.entrySet()) {
                    int soul = entry.getKey();
                    int count = entry.getValue();
                    if (pet.canEat(-1, -1, soul)) {
                        int statsID = pet.statsIdByEat(-1, -1, soul);
                        if (statsID == 0)
                            return;
                        int soulCount = (obj.getSoulStat().get(soul) != null ? obj.getSoulStat().get(soul) : 0);
                        if (soulCount > 0) {
                            obj.getSoulStat().remove(soul);
                            obj.getSoulStat().put(soul, count + soulCount);
                            this.lastEatDate = Instant.now().toEpochMilli();
                            obj.getTxtStat().remove(Constantes.STATS_PETS_DATE);
                            obj.getTxtStat().put(Constantes.STATS_PETS_DATE,this.getLastEatDate()+"");
                        } else {
                            obj.getSoulStat().put(soul, count);
                        }
                    }
                }
                //Re-Calcul des points gagn?s
                for (Entry<Integer, ArrayList<Map<Integer, Integer>>> ent : pet.getMonsters().entrySet()) {
                    for (Map<Integer, Integer> entry : ent.getValue()) {
                        for (Entry<Integer, Integer> monsterEntry : entry.entrySet()) {
                            if (pet.getNumbMonster(ent.getKey(), monsterEntry.getKey()) != 0) {
                                int pts = 0;
                                for (Entry<Integer, Integer> list : obj.getSoulStat().entrySet())
                                    pts += ((int) Math.floor(list.getValue() / pet.getNumbMonster(ent.getKey(), list.getKey())) * pet.getGain());
                                System.out.println(pts);
                                if (pts > 0) {
                                    if (pts > this.getMaxStat())
                                        pts = this.getMaxStat();
                                    if (obj.getCaracteristicas().getEffects().containsKey(ent.getKey())) {
                                        int nbr = obj.getCaracteristicas().getEffects().get(ent.getKey());
                                        if(nbr - pts > 0)
                                            pts += (nbr - pts);
                                        obj.getCaracteristicas().getEffects().remove(ent.getKey());
                                    }
                                    obj.getCaracteristicas().getEffects().put(ent.getKey(), pts);
                                }
                            }
                        }
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("Error : " + e.getMessage());
            }
            GestorSalida.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(p, obj);
            Database.dinamicos.getObjectData().update(obj);
            Database.dinamicos.getPetData().update(this);
        }

        public void updatePets(Jugador p, int max) {
            ObjetoJuego obj = Mundo.getGameObject(this.objetoid);
            if (obj == null)
                return;
            Mascota pets = Mundo.mundo.getPets(obj.getModelo().getId());
            if (pets == null)
                return;
            if (this.pdv <= 0
                    && obj.getModelo().getId() == pets.getDeadTemplate())
                return;//Ne le met pas a jour si deja mort

            if (this.lastEatDate + (max * 3600000) < Instant.now().toEpochMilli())//Oublier de le nourrir
            {
                //On calcul le nombre de repas oublier arrondi au sup?rieur :
                int nbrepas = (int) Math.floor((Instant.now().toEpochMilli() - this.lastEatDate)
                        / (max * 3600000));
                //Perte corpulence
                this.corpulencia = this.corpulencia - nbrepas;

                if (nbrepas != 0) {
                    obj.getTxtStat().remove(Constantes.STATS_PETS_POIDS);
                    obj.getTxtStat().put(Constantes.STATS_PETS_POIDS, Integer.toString(this.corpulencia));
                } else {
                    if(this.pdv>0)
                        GestorSalida.GAME_SEND_Im_PACKET(p,"025");
                }
            }

            if (this.pdv <= 0) {
                this.pdv = 0;
                obj.getTxtStat().remove(Constantes.STATS_PETS_PDV);
                obj.getTxtStat().put(Constantes.STATS_PETS_PDV, Integer.toHexString((Math.max(this.pdv, 0))));

                if (pets.getDeadTemplate() == 0)//Si Pets DeadTemplate = 0 remove de l'item et pet entry
                {
                    Mundo.mundo.removeGameObject(obj.getId());
                    p.removeItem(obj.getId());
                    GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(p, obj.getId());
                } else {
                    obj.setModelo(pets.getDeadTemplate());
                    if (obj.getPosicion() == Constantes.ITEM_POS_FAMILIER) {
                        obj.setPosicion(Constantes.ITEM_POS_NO_EQUIPED);
                        GestorSalida.GAME_SEND_OBJET_MOVE_PACKET(p, obj);
                    }
                }
                GestorSalida.GAME_SEND_Im_PACKET(p, "154");
            }
            GestorSalida.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(p, obj);
            Database.dinamicos.getObjectData().update(obj);
            Database.dinamicos.getPetData().update(this);
        }

        public void resurrection() {
            ObjetoJuego obj = Mundo.getGameObject(this.objetoid);
            if (obj == null)
                return;

            obj.setModelo(this.modelo);

            this.pdv = 1;
            this.corpulencia = 0;
            this.cantidadcomida = 0;
            this.lastEatDate = Instant.now().toEpochMilli();
            obj.getTxtStat().remove(Constantes.STATS_PETS_DATE);
            obj.getTxtStat().put(Constantes.STATS_PETS_DATE,this.getLastEatDate()+"");

            obj.getTxtStat().remove(Constantes.STATS_PETS_PDV);
            obj.getTxtStat().put(Constantes.STATS_PETS_PDV, Integer.toHexString(this.pdv));
            Database.dinamicos.getObjectData().update(obj);
            Database.dinamicos.getPetData().update(this);
        }

        public void restoreLife(Jugador p) {
            ObjetoJuego obj = Mundo.getGameObject(this.objetoid);
            if (obj == null)
                return;
            Mascota pets = Mundo.mundo.getPets(obj.getModelo().getId());
            if (pets == null)
                return;

            if (this.pdv >= 10) {
                //Il la mange pas de pdv en plus
                GestorSalida.GAME_SEND_Im_PACKET(p, "032");
            } else if (this.pdv < 10 && this.pdv > 0) {
                this.pdv++;

                obj.getTxtStat().remove(Constantes.STATS_PETS_PDV);
                obj.getTxtStat().put(Constantes.STATS_PETS_PDV, Integer.toHexString(this.pdv));

                GestorSalida.GAME_SEND_Im_PACKET(p, "032");
            } else {
                return;
            }
            Database.dinamicos.getObjectData().update(obj);
            Database.dinamicos.getPetData().update(this);
        }

        public void giveEpo(Jugador p) {
            ObjetoJuego obj = Mundo.getGameObject(this.objetoid);
            if (obj == null)
                return;
            Mascota pets = Mundo.mundo.getPets(obj.getModelo().getId());
            if (pets == null)
                return;
            if (this.isEupeoh)
                return;
            obj.getTxtStat().put(Constantes.STATS_PETS_EPO, Integer.toHexString(1));
            GestorSalida.GAME_SEND_Im_PACKET(p, "032");
            GestorSalida.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(p, obj);
            Database.dinamicos.getPetData().update(this);
        }
    }
}