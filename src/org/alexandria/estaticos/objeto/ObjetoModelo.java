package org.alexandria.estaticos.objeto;

import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.estaticos.cliente.Jugador.Caracteristicas;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Constantes;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.objeto.entrada.PiedraAlma;
import org.alexandria.otro.Dopeul;
import org.alexandria.estaticos.pelea.hechizo.EfectoHechizo;
import org.alexandria.estaticos.Mascota.MascotaEntrada;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ObjetoModelo {

    private int id;
    private String strTemplate;
    private String name;
    private int type;
    private int level;
    private int pod;
    private int price;
    private int panoId;
    private String conditions;
    private int PACost, POmin, POmax, tauxCC, tauxEC,
            bonusCC;
    private boolean isTwoHanded;
    private long sold;
    private int avgPrice;
    private int points, newPrice;
    private ArrayList<ObjetoAccion> onUseActions;

    public String toString() {
        return id + "";
    }

    public ObjetoModelo(int id, String strTemplate, String name, int type,
                        int level, int pod, int price, int panoId, String conditions,
                        String armesInfos, int sold, int avgPrice, int points, int newPrice) {
        this.id = id;
        this.strTemplate = strTemplate;
        this.name = name;
        this.type = type;
        this.level = level;
        this.pod = pod;
        this.price = price;
        this.panoId = panoId;
        this.conditions = conditions;
        this.PACost = -1;
        this.POmin = 1;
        this.POmax = 1;
        this.tauxCC = 100;
        this.tauxEC = 2;
        this.bonusCC = 0;
        this.sold = sold;
        this.avgPrice = avgPrice;
        this.points = points;
        this.newPrice = newPrice;
        if(armesInfos.isEmpty()) return;
        try {
            String[] infos = armesInfos.split(";");
            PACost = Integer.parseInt(infos[0]);
            POmin = Integer.parseInt(infos[1]);
            POmax = Integer.parseInt(infos[2]);
            tauxCC = Integer.parseInt(infos[3]);
            tauxEC = Integer.parseInt(infos[4]);
            bonusCC = Integer.parseInt(infos[5]);
            isTwoHanded = infos[6].equals("1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setInfos(String strTemplate, String name, int type, int level, int pod, int price, int panoId, String conditions, String armesInfos, int sold, int avgPrice, int points, int newPrice) {
        this.strTemplate = strTemplate;
        this.name = name;
        this.type = type;
        this.level = level;
        this.pod = pod;
        this.price = price;
        this.panoId = panoId;
        this.conditions = conditions;
        this.PACost = -1;
        this.POmin = 1;
        this.POmax = 1;
        this.tauxCC = 100;
        this.tauxEC = 2;
        this.bonusCC = 0;
        this.sold = sold;
        this.avgPrice = avgPrice;
        this.points = points;
        this.newPrice = newPrice;
        try {
            String[] infos = armesInfos.split(";");
            PACost = Integer.parseInt(infos[0]);
            POmin = Integer.parseInt(infos[1]);
            POmax = Integer.parseInt(infos[2]);
            tauxCC = Integer.parseInt(infos[3]);
            tauxEC = Integer.parseInt(infos[4]);
            bonusCC = Integer.parseInt(infos[5]);
            isTwoHanded = infos[6].equals("1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStrTemplate() {
        return strTemplate;
    }

    public void setStrTemplate(String strTemplate) {
        this.strTemplate = strTemplate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPod() {
        return pod;
    }

    public void setPod(int pod) {
        this.pod = pod;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPanoId() {
        return panoId;
    }

    public void setPanoId(int panoId) {
        this.panoId = panoId;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public int getPACost() {
        return PACost;
    }

    public void setPACost(int pACost) {
        PACost = pACost;
    }

    public int getPOmin() {
        return POmin;
    }

    public void setPOmin(int pOmin) {
        POmin = pOmin;
    }

    public int getPOmax() {
        return POmax;
    }

    public void setPOmax(int pOmax) {
        POmax = pOmax;
    }

    public int getTauxCC() {
        return tauxCC;
    }

    public void setTauxCC(int tauxCC) {
        this.tauxCC = tauxCC;
    }

    public int getTauxEC() {
        return tauxEC;
    }

    public void setTauxEC(int tauxEC) {
        this.tauxEC = tauxEC;
    }

    public int getBonusCC() {
        return bonusCC;
    }

    public void setBonusCC(int bonusCC) {
        this.bonusCC = bonusCC;
    }

    public boolean isTwoHanded() {
        return isTwoHanded;
    }

    public void setTwoHanded(boolean isTwoHanded) {
        this.isTwoHanded = isTwoHanded;
    }

    public int getAvgPrice() {
        return avgPrice;
    }

    public long getSold() {
        return this.sold;
    }

    public int getPoints() {
        return this.points;
    }

    public void addAction(ObjetoAccion A) {
        if(this.onUseActions == null)
            this.onUseActions = new ArrayList<>();
        this.onUseActions.add(A);
    }

    public ArrayList<ObjetoAccion> getOnUseActions() {
        return onUseActions == null ? new ArrayList<>() : onUseActions;
    }

    public ObjetoJuego createNewCertificat(ObjetoJuego obj) {
        int id = Database.dinamicos.getObjectData().getNextId();
        ObjetoJuego item = null;
        if (getType() == Constantes.ITEM_TYPE_CERTIFICAT_CHANIL) {
            MascotaEntrada myPets = Mundo.mundo.getPetsEntry(obj.getId());
            Map<Integer, String> txtStat = new HashMap<>();
            Map<Integer, String> actualStat = obj.getTxtStat();
            if (actualStat.containsKey(Constantes.STATS_PETS_PDV))
                txtStat.put(Constantes.STATS_PETS_PDV, actualStat.get(Constantes.STATS_PETS_PDV));
            if (actualStat.containsKey(Constantes.STATS_PETS_DATE))
                txtStat.put(Constantes.STATS_PETS_DATE, myPets.getLastEatDate()
                        + "");
            if (actualStat.containsKey(Constantes.STATS_PETS_POIDS))
                txtStat.put(Constantes.STATS_PETS_POIDS, actualStat.get(Constantes.STATS_PETS_POIDS));
            if (actualStat.containsKey(Constantes.STATS_PETS_EPO))
                txtStat.put(Constantes.STATS_PETS_EPO, actualStat.get(Constantes.STATS_PETS_EPO));
            if (actualStat.containsKey(Constantes.STATS_PETS_REPAS))
                txtStat.put(Constantes.STATS_PETS_REPAS, actualStat.get(Constantes.STATS_PETS_REPAS));
            item = new ObjetoJuego(id, getId(), 1, Constantes.ITEM_POS_NO_EQUIPED, obj.getCaracteristicas(), new ArrayList<>(), new HashMap<>(), txtStat, 0);
            Mundo.mundo.removePetsEntry(obj.getId());
            Database.dinamicos.getPetData().delete(obj.getId());
        }
        return item;
    }

    public ObjetoJuego createNewFamilier(ObjetoJuego obj) {
        int id = Database.dinamicos.getObjectData().getNextId();
        Map<Integer, String> stats = new HashMap<>(obj.getTxtStat());

        ObjetoJuego object = new ObjetoJuego(id, getId(), 1, Constantes.ITEM_POS_NO_EQUIPED, obj.getCaracteristicas(), new ArrayList<>(), new HashMap<>(), stats, 0);

        long time = Instant.now().toEpochMilli();
        Mundo.mundo.addPetsEntry(new MascotaEntrada(id, getId(), time, 0, Integer.parseInt(stats.get(Constantes.STATS_PETS_PDV), 16), Integer.parseInt(stats.get(Constantes.STATS_PETS_POIDS), 16), !stats.containsKey(Constantes.STATS_PETS_EPO)));
        Database.dinamicos.getPetData().add(id, time, getId());
        return object;
    }

    public ObjetoJuego createNewBenediction(int turn) {
        int id = Database.dinamicos.getObjectData().getNextId();
        ObjetoJuego item = null;
        Caracteristicas stats = generateNewStatsFromTemplate(getStrTemplate(), true);
        stats.addOneStat(Constantes.STATS_TURN, turn);
        item = new ObjetoJuego(id, getId(), 1, Constantes.ITEM_POS_BENEDICTION, stats, new ArrayList<>(), new HashMap<>(), new HashMap<>(), 0);
        return item;
    }

    public ObjetoJuego createNewMalediction() {
        int id = Database.dinamicos.getObjectData().getNextId();
        Caracteristicas stats = generateNewStatsFromTemplate(getStrTemplate(), true);
        stats.addOneStat(Constantes.STATS_TURN, 1);
        return new ObjetoJuego(id, getId(), 1, Constantes.ITEM_POS_MALEDICTION, stats, new ArrayList<>(), new HashMap<>(), new HashMap<>(), 0);
    }

    public ObjetoJuego createNewRoleplayBuff() {
        int id = Database.dinamicos.getObjectData().getNextId();
        Caracteristicas stats = generateNewStatsFromTemplate(getStrTemplate(), true);
        stats.addOneStat(Constantes.STATS_TURN, 1);
        return new ObjetoJuego(id, getId(), 1, Constantes.ITEM_POS_ROLEPLAY_BUFF, stats, new ArrayList<>(), new HashMap<>(), new HashMap<>(), 0);
    }

    public ObjetoJuego createNewCandy(int turn) {
        int id = Database.dinamicos.getObjectData().getNextId();
        ObjetoJuego item = null;
        Caracteristicas stats = generateNewStatsFromTemplate(getStrTemplate(), true);
        stats.addOneStat(Constantes.STATS_TURN, turn);
        item = new ObjetoJuego(id, getId(), 1, Constantes.ITEM_POS_BONBON, stats, new ArrayList<>(), new HashMap<>(), new HashMap<>(), 0);
        return item;
    }

    public ObjetoJuego createNewFollowPnj(int turn) {
        int id = Database.dinamicos.getObjectData().getNextId();
        ObjetoJuego item = null;
        Caracteristicas stats = generateNewStatsFromTemplate(getStrTemplate(), true);
        stats.addOneStat(Constantes.STATS_TURN, turn);
        stats.addOneStat(148, 0);
        item = new ObjetoJuego(id, getId(), 1, Constantes.ITEM_POS_PNJ_SUIVEUR, stats, new ArrayList<>(), new HashMap<>(), new HashMap<>(), 0);
        return item;
    }

    public ObjetoJuego createNewItem(int qua, boolean useMax) {
        int id = Database.dinamicos.getObjectData().getNextId();
        ObjetoJuego item;
        if (getType() == Constantes.ITEM_TYPE_QUETES && (Constantes.isCertificatDopeuls(getId()) || getId() == 6653)) {
            Map<Integer, String> txtStat = new HashMap<>();
            txtStat.put(Constantes.STATS_DATE, Instant.now().toEpochMilli() + "");
            item = new ObjetoJuego(id, getId(), qua, Constantes.ITEM_POS_NO_EQUIPED, new Caracteristicas(false, null), new ArrayList<>(), new HashMap<>(), txtStat, 0);
        } else if (this.getId() == 10207) {
            item = new ObjetoJuego(id, getId(), qua, Constantes.ITEM_POS_NO_EQUIPED, new Caracteristicas(false, null), new ArrayList<>(), new HashMap<>(), Dopeul.generateStatsTrousseau(), 0);
        } else if (getType() == Constantes.ITEM_TYPE_FAMILIER) {
            //Reparamos mascotas NaN/NaN
            id= Database.dinamicos.getObjectData().getNextId();
            item = new ObjetoJuego(id, getId(), 1, Constantes.ITEM_POS_NO_EQUIPED, (useMax ? generateNewStatsFromTemplate(Mundo.mundo.getPets(this.getId()).getJet(), false) : new Caracteristicas(false, null)), new ArrayList<>(), new HashMap<>(), Mundo.mundo.getPets(getId()).generateNewtxtStatsForPets(), 0);
            //Agregar a PetsData en SQL
            long time = Instant.now().toEpochMilli();
            Mundo.mundo.addPetsEntry(new MascotaEntrada(id, getId(), time, 0, 10, 0, false));
            Database.dinamicos.getPetData().add(id, time, getId());
        } else if(getType() == Constantes.ITEM_TYPE_CERTIF_MONTURE) {
            item = new ObjetoJuego(id, getId(), qua, Constantes.ITEM_POS_NO_EQUIPED, generateNewStatsFromTemplate(getStrTemplate(), useMax), getEffectTemplate(getStrTemplate()), new HashMap<>(), new HashMap<>(), 0);
        } else {
            if (getType() == Constantes.ITEM_TYPE_OBJET_ELEVAGE) {
                item = new ObjetoJuego(id, getId(), qua, Constantes.ITEM_POS_NO_EQUIPED, new Caracteristicas(false, null), new ArrayList<>(), new HashMap<>(), getStringResistance(getStrTemplate()), 0);
            } else if (Constantes.isIncarnationWeapon(getId())) {
                Map<Integer, Integer> Stats = new HashMap<>();
                Stats.put(Constantes.ERR_STATS_XP, 0);
                Stats.put(Constantes.STATS_NIVEAU, 1);
                item = new ObjetoJuego(id, getId(), qua, Constantes.ITEM_POS_NO_EQUIPED, generateNewStatsFromTemplate(getStrTemplate(), useMax), getEffectTemplate(getStrTemplate()), Stats, new HashMap<>(), 0);
            } else {
                Map<Integer, String> Stat = new HashMap<>();
                switch (getType()) {
                    case 1, 2, 3, 4, 5, 6, 7, 8 -> {
                        String[] splitted = getStrTemplate().split(",");
                        for (String s : splitted) {
                            String[] stats = s.split("#");
                            int statID = Integer.parseInt(stats[0], 16);
                            if (statID == Constantes.STATS_RESIST) {
                                String ResistanceIni = stats[1];
                                Stat.put(statID, ResistanceIni);
                            }
                        }
                    }
                }
                item = new ObjetoJuego(id, getId(), qua, Constantes.ITEM_POS_NO_EQUIPED, generateNewStatsFromTemplate(getStrTemplate(), useMax), getEffectTemplate(getStrTemplate()), new HashMap<>(), Stat, 0);
                item.getSpellStats().addAll(this.getSpellStatsTemplate());
            }
        }
        return item;
    }

    public ObjetoJuego createNewItemWithoutDuplication(Collection<ObjetoJuego> objects, int qua, boolean useMax) {
        int id = -1;
        ObjetoJuego item;
        if (getType() == Constantes.ITEM_TYPE_QUETES && (Constantes.isCertificatDopeuls(getId()) || getId() == 6653)) {
            Map<Integer, String> txtStat = new HashMap<>();
            txtStat.put(Constantes.STATS_DATE, Instant.now().toEpochMilli() + "");
            item = new ObjetoJuego(id, getId(), qua, Constantes.ITEM_POS_NO_EQUIPED, new Caracteristicas(false, null), new ArrayList<>(), new HashMap<>(), txtStat, 0);
        } else if (this.getId() == 10207) {
            item = new ObjetoJuego(id, getId(), qua, Constantes.ITEM_POS_NO_EQUIPED, new Caracteristicas(false, null), new ArrayList<>(), new HashMap<>(), Dopeul.generateStatsTrousseau(), 0);
        } else if (getType() == Constantes.ITEM_TYPE_FAMILIER) {
            //Reparamos mascotas NaN/NaN
            id= Database.dinamicos.getObjectData().getNextId();
            item = new ObjetoJuego(id, getId(), 1, Constantes.ITEM_POS_NO_EQUIPED, (useMax ? generateNewStatsFromTemplate(Mundo.mundo.getPets(this.getId()).getJet(), false) : new Caracteristicas(false, null)), new ArrayList<>(), new HashMap<>(), Mundo.mundo.getPets(getId()).generateNewtxtStatsForPets(), 0);
            //Ajouter du Pets_data SQL et World
            long time = Instant.now().toEpochMilli();
            Mundo.mundo.addPetsEntry(new MascotaEntrada(id, getId(), time, 0, 10, 0, false));
            Database.dinamicos.getPetData().add(id, time, getId());
        } else if(getType() == Constantes.ITEM_TYPE_CERTIF_MONTURE) {
            item = new ObjetoJuego(id, getId(), qua, Constantes.ITEM_POS_NO_EQUIPED, generateNewStatsFromTemplate(getStrTemplate(), useMax), getEffectTemplate(getStrTemplate()), new HashMap<>(), new HashMap<>(), 0);
        } else {
            if (getType() == Constantes.ITEM_TYPE_OBJET_ELEVAGE) {
                item = new ObjetoJuego(id, getId(), qua, Constantes.ITEM_POS_NO_EQUIPED, new Caracteristicas(false, null), new ArrayList<>(), new HashMap<>(), getStringResistance(getStrTemplate()), 0);
            } else if (Constantes.isIncarnationWeapon(getId())) {
                Map<Integer, Integer> Stats = new HashMap<>();
                Stats.put(Constantes.ERR_STATS_XP, 0);
                Stats.put(Constantes.STATS_NIVEAU, 1);
                item = new ObjetoJuego(id, getId(), qua, Constantes.ITEM_POS_NO_EQUIPED, generateNewStatsFromTemplate(getStrTemplate(), useMax), getEffectTemplate(getStrTemplate()), Stats, new HashMap<>(), 0);
            } else {
                Map<Integer, String> Stat = new HashMap<>();
                switch (getType()) {
                    case 1, 2, 3, 4, 5, 6, 7, 8 -> {
                        String[] splitted = getStrTemplate().split(",");
                        for (String s : splitted) {
                            String[] stats = s.split("#");
                            int statID = Integer.parseInt(stats[0], 16);
                            if (statID == Constantes.STATS_RESIST) {
                                String ResistanceIni = stats[1];
                                Stat.put(statID, ResistanceIni);
                            }
                        }
                    }
                }
                item = new ObjetoJuego(id, getId(), qua, Constantes.ITEM_POS_NO_EQUIPED, generateNewStatsFromTemplate(getStrTemplate(), useMax), getEffectTemplate(getStrTemplate()), new HashMap<>(), Stat, 0);
                item.getSpellStats().addAll(this.getSpellStatsTemplate());
            }
        }

        for(ObjetoJuego object : objects)
            if(Mundo.mundo.getConditionManager().stackIfSimilar(object, item, true))
                return object;
        return item;
    }

    private Map<Integer, String> getStringResistance(String statsTemplate) {
        Map<Integer, String> Stat = new HashMap<>();
        String[] splitted = statsTemplate.split(",");

        for (String s : splitted) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            String ResistanceIni = stats[1];
            Stat.put(statID, ResistanceIni);
        }
        return Stat;
    }

    public ArrayList<String> getSpellStatsTemplate() {
        final ArrayList<String> spellStats = new ArrayList<>();

        if(!this.getStrTemplate().isEmpty()) {
            for (String stats : this.getStrTemplate().split(",")) {
                String[] split = stats.split("#");
                int id = Integer.parseInt(split[0], 16);

                if (id >= 281 && id <= 294) {
                    spellStats.add(stats);
                }
            }
        }
        return spellStats;
    }

    public Caracteristicas generateNewStatsFromTemplate(String statsTemplate,
                                                        boolean useMax) {
        Caracteristicas itemStats = new Caracteristicas(false, null);
        //Si stats Vides
        if (statsTemplate.equals("") || statsTemplate == null)
            return itemStats;

        String[] splitted = statsTemplate.split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            boolean follow = true;


            for (int a : Constantes.ARMES_EFFECT_IDS)
                if (a == statID) {
                    follow = false;
                    break;
                }
            if (!follow)//Si c'�tait un effet Actif d'arme
                continue;
            if (statID >= 281 && statID <= 294)
                continue;
            if (statID == Constantes.STATS_RESIST)
                continue;
            boolean isStatsInvalid = false;
            switch (statID) {
                case 110, 139, 605, 614 -> isStatsInvalid = true;
                case 615 -> itemStats.addOneStat(statID, Integer.parseInt(stats[3], 16));
            }
            if(isStatsInvalid)
                continue;
            String jet="";
            int value=1;
            if(this.getType() != 83 && stats.length >= 5)
            {
            try {
                jet = stats[4];
                value = Formulas.getRandomJet(jet);
                if (useMax) {
                    try {
                        //on prend le jet max
                        int min = Integer.parseInt(stats[1], 16);
                        int max = Integer.parseInt(stats[2], 16);
                        value = min;
                        if (max != 0)
                            value = max;
                    } catch (Exception e) {
                        e.printStackTrace();
                        value = Formulas.getRandomJet(jet);
                    }
                }
            } catch (Exception e) {
               System.err.println(statsTemplate + " : " + s + " : " + e.getMessage());
            }
            }
            itemStats.addOneStat(statID, value);
        }
        return itemStats;
    }

    private ArrayList<EfectoHechizo> getEffectTemplate(String statsTemplate) {
        ArrayList<EfectoHechizo> Effets = new ArrayList<>();
        if (statsTemplate.equals(""))
            return Effets;

        String[] splitted = statsTemplate.split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            for (int a : Constantes.ARMES_EFFECT_IDS) {
                if (a == statID) {
                    int id = statID;
                    String min = stats[1];
                    String max = stats[2];
                    String jet = stats[4];
                    String args = min + ";" + max + ";-1;-1;0;" + jet;
                    Effets.add(new EfectoHechizo(id, args, 0, -1));
                }
            }
            switch (statID) {
                case 110, 139, 605, 614 -> {
                    String min = stats[1];
                    String max = stats[2];
                    String jet = stats[4];
                    String args = min + ";" + max + ";-1;-1;0;" + jet;
                    Effets.add(new EfectoHechizo(statID, args, 0, -1));
                }
            }
        }
        return Effets;
    }

    public String parseItemTemplateStats() {
        return getId() + ";" + getStrTemplate() + (this.newPrice > 0 ? ";" + this.newPrice : "");
    }

    public void applyAction(Jugador player, Jugador target, int objectId, short cellId) {
        if (Mundo.getGameObject(objectId) == null) return;
        if (Mundo.getGameObject(objectId).getModelo().getType() == 85) {
            if (!PiedraAlma.isInArenaMap(player.getCurMap().getId()))
                return;

            PiedraAlma soulStone = (PiedraAlma) Mundo.getGameObject(objectId);

            player.getCurMap().spawnNewGroup(true, player.getCurCell().getId(), soulStone.parseGroupData(), "MiS=" + player.getId());
            GestorSalida.GAME_SEND_Im_PACKET(player, "022;" + 1 + "~" + Mundo.getGameObject(objectId).getModelo().getId());
            player.removeItem(objectId, 1, true, true);
        } else {
            for (ObjetoAccion action : this.getOnUseActions())
                action.apply(player, target, objectId, cellId);
        }
    }

    public synchronized void newSold(int amount, int price) {
        long oldSold = getSold();
        sold += amount;
        avgPrice = (int) ((getAvgPrice() * oldSold + price) / getSold());
    }
}