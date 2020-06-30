package org.alexandria.estaticos.objeto;

import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.estaticos.cliente.Jugador.Caracteristicas;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Constantes;
import org.alexandria.configuracion.Logging;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.estaticos.Montura;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.objeto.entrada.FragmentosMagicos;
import org.alexandria.estaticos.oficio.OficioAccion;
import org.alexandria.estaticos.pelea.hechizo.EfectoHechizo;
import org.alexandria.otro.utilidad.Doble;
import org.alexandria.estaticos.Mascota.MascotaEntrada;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ObjetoJuego {

    protected ObjetoModelo modelo;
    protected int cantidad = 1;
    protected int posicion = Constantes.ITEM_POS_NO_EQUIPED;
    protected int id;
    protected int obvijevanPos;
    protected int obvijevanLook;
    protected int puit;
    private Caracteristicas Caracteristicas = new Caracteristicas();
    private ArrayList<EfectoHechizo> Effects = new ArrayList<>();
    private final ArrayList<String> SortStats = new ArrayList<>();
    private Map<Integer, String> txtStats = new HashMap<>();
    private Map<Integer, Integer> SoulStats = new HashMap<>();

    public byte modificaciones = -1;

    public ObjetoJuego(int id, int modelo, int cantidad, int posicion, String strStats, int puit) {
        this.id = id;
        this.modelo = Mundo.mundo.getObjetoModelo(modelo);
        this.cantidad = cantidad;
        this.posicion = posicion;
        this.puit = puit;

        Caracteristicas = new Caracteristicas();
        this.parseStringToStats(strStats, false);
    }

    public ObjetoJuego(int id) {
        this.id = id;
        this.modelo = Mundo.mundo.getObjetoModelo(8378);
        this.cantidad = 1;
        this.posicion = -1;
        this.puit = 0;
    }

    public ObjetoJuego(int id, int modelo, int cantidad, int posicion, Caracteristicas caracteristicas, ArrayList<EfectoHechizo> effects, Map<Integer, Integer> _SoulStat, Map<Integer, String> _txtStats, int puit) {
        this.id = id;
        this.modelo = Mundo.mundo.getObjetoModelo(modelo);
        this.cantidad = cantidad;
        this.posicion = posicion;
        this.Caracteristicas = caracteristicas;
        this.Effects = effects;
        this.SoulStats = _SoulStat;
        this.txtStats = _txtStats;
        this.obvijevanPos = 0;
        this.obvijevanLook = 0;
        this.puit = puit;
    }

    public static ObjetoJuego getCloneObjet(ObjetoJuego objeto, int cantidad) {
        Map<Integer, Integer> maps = new HashMap<>(objeto.getCaracteristicas().getEffects());
        Caracteristicas newStats = new Caracteristicas(maps);
        ObjetoJuego objetos = new ObjetoJuego(Database.dinamicos.getObjectData().getNextId(),objeto.getModelo().getId(),cantidad,Constantes.ITEM_POS_NO_EQUIPED,newStats,objeto.getEffects(),objeto.getSoulStat(),objeto.getTxtStat(),objeto.getPuit());
        objetos.modificaciones = 0;
        return objetos;
    }

    public int setId() {
        this.id = Database.dinamicos.getObjectData().getNextId();
        return this.getId();
    }

    public int getPuit() {
        return this.puit;
    }

    public void setPuit(int puit) {
        this.puit = puit;
    }

    public int getObvijevanPos() {
        return obvijevanPos;
    }

    public void setObvijevanPos(int pos) {
        obvijevanPos = pos;
        this.setModification();
    }

    public int getObvijevanLook() {
        return obvijevanLook;
    }

    public void setObvijevanLook(int look) {
        obvijevanLook = look;
        this.setModification();
    }

    public void setModification() {
        if(this.modificaciones == -1)
            this.modificaciones = 1;
    }

    public void parseStringToStats(String strStats, boolean save) {
        if(this.modelo != null && this.modelo.getId() == 7010) return;
        String dj1 = "";
        if (!strStats.equalsIgnoreCase("")) {
            for (String split : strStats.split(",")) {
                try {
                    if (split.equalsIgnoreCase(""))
                        continue;
                    if (split.substring(0, 3).equalsIgnoreCase("325") && (this.getModelo().getId() == 10207 || this.getModelo().getId() == 10601)) {
                        txtStats.put(Constantes.STATS_DATE, split.substring(3) + "");
                        continue;
                    }
                    if (split.substring(0, 3).equalsIgnoreCase("3dc")) {// Si c'est une rune de signature cr�e
                        txtStats.put(Constantes.STATS_SIGNATURE, split.split("#")[4]);
                        continue;
                    }
                    if (split.substring(0, 3).equalsIgnoreCase("3d9")) {// Si c'est une rune de signature modifi�
                        txtStats.put(Constantes.STATS_CHANGE_BY, split.split("#")[4]);
                        continue;
                    }

                    String[] stats = split.split("#");
                    int id = Integer.parseInt(stats[0], 16);

                    /*if(id == Constant.STATS_ADD_DOMA && template != null) {
                        int actual = Integer.parseInt(stats[1], 16);
                        int max = Formulas.getMaxJet(this.template.getStrTemplate());

                        if(actual - max > 5 && this.getTemplate().getType() != Constant.ITEM_TYPE_FAMILIER) {
                            Stats.addOneStat(id, max + 5);
                            continue;
                        } else if(this.getTemplate().getType() != Constant.ITEM_TYPE_FAMILIER) {
                            max = Integer.parseInt(World.world.getPets(this.template.getId()).getStatsMax());
                            if(actual > max) {
                                Stats.addOneStat(id, max);
                                continue;
                            }
                        }
                    }
                    if(id == 112) {
                        this.modification = 1;
                        continue;
                    }*/
                    if (id >= 281 && id <= 294) {
                        this.getSpellStats().add(split);
                        continue;
                    }
                    if (id == Constantes.STATS_PETS_DATE && this.getModelo().getType() == Constantes.ITEM_TYPE_CERTIFICAT_CHANIL) {
                        txtStats.put(id, split.substring(3));
                        continue;
                    }
                    if (id == Constantes.STATS_CHANGE_BY || id == Constantes.STATS_NAME_TRAQUE || id == Constantes.STATS_OWNER_1) {
                        txtStats.put(id, stats[4]);
                        continue;
                    }
                    if (id == Constantes.STATS_GRADE_TRAQUE || id == Constantes.STATS_ALIGNEMENT_TRAQUE || id == Constantes.STATS_NIVEAU_TRAQUE) {
                        txtStats.put(id, stats[3]);
                        continue;
                    }
                    if (id == Constantes.STATS_PETS_SOUL) {
                        SoulStats.put(Integer.parseInt(stats[1], 16), Integer.parseInt(stats[3], 16)); // put(id_monstre, nombre_tu�)
                        continue;
                    }
                    if (id == Constantes.STATS_NAME_DJ) {
                        dj1 += (!dj1.isEmpty() ? "," : "") + stats[3];
                        txtStats.put(Constantes.STATS_NAME_DJ, dj1);
                        continue;
                    }
                    if (id == 997 || id == 996) {
                        txtStats.put(id, stats[4]);
                        continue;
                    }
                    if (this.modelo != null && this.modelo.getId() == 77 && id == Constantes.STATS_PETS_DATE) {
                        txtStats.put(id, split.substring(3));
                        continue;
                    }
                    if (id == Constantes.STATS_DATE) {
                        txtStats.put(id, stats[3]);
                        continue;
                    }
                    //Si stats avec Texte (Signature, apartenance, etc)//FIXME
                    if (id != Constantes.STATS_RESIST && (!stats[3].equals("") && (!stats[3].equals("0") || id == Constantes.STATS_PETS_DATE || id == Constantes.STATS_PETS_PDV || id == Constantes.STATS_PETS_POIDS || id == Constantes.STATS_PETS_EPO || id == Constantes.STATS_PETS_REPAS))) {//Si le stats n'est pas vide et (n'est pas �gale � 0 ou est de type familier)
                        if (!(this.getModelo().getType() == Constantes.ITEM_TYPE_CERTIFICAT_CHANIL && id == Constantes.STATS_PETS_DATE)) {
                            txtStats.put(id, stats[3]);
                            continue;
                        }
                    }
                    if (id == Constantes.STATS_RESIST && this.getModelo() != null && this.getModelo().getType() == 93) {
                        txtStats.put(id, stats[4]);
                        continue;
                    }
                    if (id == Constantes.STATS_RESIST) {
                        txtStats.put(id, stats[4]);
                        continue;
                    }

                    boolean follow1 = true;
                    switch (id) {
                        case 110, 139, 605, 614 -> {
                            String min = stats[1];
                            String max = stats[2];
                            String jet = stats[4];
                            String args = min + ";" + max + ";-1;-1;0;" + jet;
                            Effects.add(new EfectoHechizo(id, args, 0, -1));
                            follow1 = false;
                        }
                    }
                    if (!follow1) {
                        continue;
                    }

                    boolean follow2 = true;
                    for (int a : Constantes.ARMES_EFFECT_IDS) {
                        if (a == id) {
                            Effects.add(new EfectoHechizo(id, stats[1] + ";" + stats[2] + ";-1;-1;0;" + stats[4], 0, -1));
                            follow2 = false;
                        }
                    }
                    if (!follow2)
                        continue;//Si c'�tait un effet Actif d'arme ou une signature

                    Caracteristicas.addOneStat(id, Integer.parseInt(stats[1], 16));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if(save)
            this.setModification();
    }

    public void addTxtStat(int i, String s) {
        txtStats.put(i, s);
        this.setModification();
    }

    public String getTraquedName() {
        for (Entry<Integer, String> entry : txtStats.entrySet()) {
            if (Integer.toHexString(entry.getKey()).compareTo("3dd") == 0) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Caracteristicas getCaracteristicas() {
        return Caracteristicas;
    }

    public void setCaracteristicas(Caracteristicas SS) {
        Caracteristicas = SS;
        this.setModification();
    }

    public ArrayList<String> getSpellStats() {
        return SortStats;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        if (cantidad <= 0)
            cantidad = 0;
        else if (cantidad >= 100_000)
            Logging.objetos.warn("Faille : Objet guid : " + id + " a dépassé 100 000 qua (" + cantidad + ") avec comme template : " + modelo.getName() + " (" + modelo.getId() + ")");

        this.cantidad = cantidad;
        this.setModification();
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.setModification();
        this.posicion = posicion;
    }

    public ObjetoModelo getModelo() {
        return modelo;
    }

    public void setModelo(int Tid) {
        this.setModification();
        this.modelo = Mundo.mundo.getObjetoModelo(Tid);
    }

    public int getId() {
        return id;
    }

    public Map<Integer, Integer> getSoulStat() {
        return SoulStats;
    }

    public Map<Integer, String> getTxtStat() {
        return txtStats;
    }

    public Montura setMountStats(Jugador player, Montura mount, boolean castrated) {
        if(mount == null)
            mount = new Montura(Constantes.getMountColorByParchoTemplate(this.getModelo().getId()), player.getId(), false);
        if(castrated) mount.setCastrated();

        this.clearStats();
        this.getCaracteristicas().addOneStat(995, - (mount.getId()));
        this.getTxtStat().put(996, player.getName());
        this.getTxtStat().put(997, mount.getName());
        this.setModification();
        return mount;
    }

    public void attachToPlayer(Jugador player) {
        this.getTxtStat().put(Constantes.STATS_OWNER_1, player.getName());
        GestorSalida.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(player, this);
        this.setModification();
    }

    public boolean isAttach() {
        boolean ok = this.getTxtStat().containsKey(Constantes.STATS_OWNER_1);

        if(ok) {
            Jugador player = Mundo.mundo.getPlayerByName(this.getTxtStat().get(Constantes.STATS_OWNER_1));
            if(player != null) player.send("BN");
        }

        return ok;
    }

    public String parseItem() {
        String posi = posicion == Constantes.ITEM_POS_NO_EQUIPED ? "" : Integer.toHexString(posicion);
        return Integer.toHexString(id) + "~"
                + Integer.toHexString(modelo.getId()) + "~"
                + Integer.toHexString(cantidad) + "~" + posi + "~"
                + parseStatsString() + ";";
    }

    public String parseStatsString() {
        if (this.getModelo().getType() == 83) //Si c'est une pierre d'�me vide
            return this.getModelo().getStrTemplate();

        final StringBuilder stats = new StringBuilder();
        boolean isFirst = true;

        // Panoplie de classe (81 à 92)
        if (this.getModelo().getPanoId() >= 81 && this.getModelo().getPanoId() <= 92) {
            for (String spell : this.SortStats) {
                if (!isFirst) {
                    stats.append(",");
                }
                stats.append(spell);
                isFirst = false;
            }
        }

        for (EfectoHechizo effect : this.Effects) {
            if (!isFirst)
                stats.append(",");
            String[] split = effect.getArgs().split(";");

            try {
                if (effect.getEffectID() == 614) {
                    stats.append(Integer.toHexString(effect.getEffectID())).append("#0#0#").append(split[0]).append("#").append(split[5]);
                } else {
                    stats.append(Integer.toHexString(effect.getEffectID())).append("#").append(split[0]).append("#").append(split[1]).append("#").append(split[1]).append("#").append(split[5]);
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            isFirst = false;
        }

        for (Entry<Integer, String> entry : txtStats.entrySet()) {
            if (!isFirst)
                stats.append(",");
            if (modelo.getType() == 77 || modelo.getType() == 90) {
                if (entry.getKey() == Constantes.STATS_PETS_PDV)
                    stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
                if (entry.getKey() == Constantes.STATS_PETS_EPO)
                    stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
                if (entry.getKey() == Constantes.STATS_PETS_REPAS)
                    stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
                if (entry.getKey() == Constantes.STATS_PETS_POIDS) {
                    int corpu = 0;
                    int corpulence = 0;
                    String c = entry.getValue();
                    if (c != null && !c.equalsIgnoreCase("")) {
                        try {
                            corpulence = Integer.parseInt(c);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (corpulence != 0)
                        corpu = 7;
                    stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(corpu)).append("#").append(corpulence > 0 ? corpu : 0).append("#").append(Integer.toHexString(corpu));
                }
                if (entry.getKey() == Constantes.STATS_PETS_DATE
                        && modelo.getType() == 77) {
                    if (entry.getValue().contains("#"))
                        stats.append(Integer.toHexString(entry.getKey())).append(entry.getValue());
                    else
                        stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(entry.getValue())));
                }
            } else if (entry.getKey() == Constantes.STATS_CHANGE_BY || entry.getKey() == Constantes.STATS_NAME_TRAQUE || entry.getKey() == Constantes.STATS_OWNER_1) {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
            } else if (entry.getKey() == Constantes.STATS_GRADE_TRAQUE || entry.getKey() == Constantes.STATS_ALIGNEMENT_TRAQUE || entry.getKey() == Constantes.STATS_NIVEAU_TRAQUE) {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue()).append("#0");
            } else if (entry.getKey() == Constantes.STATS_NAME_DJ) {
                if (entry.getValue().equals("0d0+0"))
                    continue;
                for (String i : entry.getValue().split(",")) {
                    stats.append(",").append(Integer.toHexString(entry.getKey())).append("#0#0#").append(i);
                }
                continue;
            } else if (entry.getKey() == Constantes.STATS_DATE) {
                String item = entry.getValue();
                if (item.contains("#")) {
                    String date = item.split("#")[3];
                    if (date != null && !date.equalsIgnoreCase(""))
                        stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(date)));
                } else
                    stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(item)));
            } else if (entry.getKey() == Constantes.CAPTURE_MONSTRE) {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue());
            } else if (entry.getKey() == Constantes.STATS_PETS_PDV
                    || entry.getKey() == Constantes.STATS_PETS_POIDS
                    || entry.getKey() == Constantes.STATS_PETS_DATE
                    || entry.getKey() == Constantes.STATS_PETS_REPAS) {
                MascotaEntrada p = Mundo.mundo.getPetsEntry(this.getId());
                if (p == null) {
                    if (entry.getKey() == Constantes.STATS_PETS_PDV)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append("a").append("#0#a");
                    if (entry.getKey() == Constantes.STATS_PETS_POIDS)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
                    if (entry.getKey() == Constantes.STATS_PETS_DATE)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
                    if (entry.getKey() == Constantes.STATS_PETS_REPAS)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
                } else {
                    if (entry.getKey() == Constantes.STATS_PETS_PDV)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(p.getPdv())).append("#0#").append(Integer.toHexString(p.getPdv()));
                    if (entry.getKey() == Constantes.STATS_PETS_POIDS)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(p.parseCorpulence()).append("#").append(p.getCorpulencia() > 0 ? p.parseCorpulence() : 0).append("#").append(p.parseCorpulence());
                    if (entry.getKey() == Constantes.STATS_PETS_DATE)
                        stats.append(Integer.toHexString(entry.getKey())).append(p.parseLastEatDate());
                    if (entry.getKey() == Constantes.STATS_PETS_REPAS)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
                    if (p.getIsEupeoh()
                            && entry.getKey() == Constantes.STATS_PETS_EPO)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(p.getIsEupeoh() ? 1 : 0)).append("#0#").append(Integer.toHexString(p.getIsEupeoh() ? 1 : 0));
                }
            } else if (entry.getKey() == Constantes.STATS_RESIST
                    && getModelo().getType() == 93) {
                stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(getResistanceMax(getModelo().getStrTemplate()))).append("#").append(entry.getValue()).append("#").append(Integer.toHexString(getResistanceMax(getModelo().getStrTemplate())));
            } else if (entry.getKey() == Constantes.STATS_RESIST) {
                stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(getResistanceMax(getModelo().getStrTemplate()))).append("#").append(entry.getValue()).append("#").append(Integer.toHexString(getResistanceMax(getModelo().getStrTemplate())));
            } else {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
            }
            isFirst = false;
        }

        for (Entry<Integer, Integer> entry : SoulStats.entrySet()) {
            if (!isFirst)
                stats.append(",");

            if (this.getModelo().getType() == 18)
                stats.append(Integer.toHexString(Constantes.STATS_PETS_SOUL)).append("#").append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#").append(Integer.toHexString(entry.getValue()));
            if (entry.getKey() == Constantes.STATS_NIVEAU)
                stats.append(Integer.toHexString(Constantes.STATS_NIVEAU)).append("#").append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#").append(Integer.toHexString(entry.getValue()));
            isFirst = false;
        }

        for (Entry<Integer, Integer> entry : Caracteristicas.getEffects().entrySet()) {

            int statID = entry.getKey();
            if ((getModelo().getPanoId() >= 81 && getModelo().getPanoId() <= 92)
                    || (getModelo().getPanoId() >= 201 && getModelo().getPanoId() <= 212)) {
                String[] modificable = modelo.getStrTemplate().split(",");
                int cantMod = modificable.length;
                for (String s : modificable) {
                    String[] mod = s.split("#");
                    if (Integer.parseInt(mod[0], 16) == statID) {
                        String jet = "0d0+" + Integer.parseInt(mod[1], 16);
                        if (!isFirst)
                            stats.append(",");
                        stats.append(mod[0]).append("#").append(mod[1]).append("#0#").append(mod[3]).append("#").append(jet);
                        isFirst = false;
                    }
                }
                continue;
            }

            if (!isFirst)
                stats.append(",");
            if(statID == 615) {
                stats.append(Integer.toHexString(statID)).append("#0#0#").append(Integer.toHexString(entry.getValue()));
            } else
            if ((statID == 970) || (statID == 971) || (statID == 972)
                    || (statID == 973) || (statID == 974)) {
                int jet = entry.getValue();
                if ((statID == 974) || (statID == 972) || (statID == 970))
                    stats.append(Integer.toHexString(statID)).append("#0#0#").append(Integer.toHexString(jet));
                else
                    stats.append(Integer.toHexString(statID)).append("#0#0#").append(jet);
                if (statID == 973)
                    setObvijevanPos(jet);
                if (statID == 972)
                    setObvijevanLook(jet);
            } else if (statID == Constantes.STATS_TURN) {
                String jet = "0d0+" + entry.getValue();
                stats.append(Integer.toHexString(statID)).append("#");
                stats.append("0#0#").append(Integer.toHexString(entry.getValue())).append("#").append(jet);
            } else {
                String jet = "0d0+" + entry.getValue();
                stats.append(Integer.toHexString(statID)).append("#");
                stats.append(Integer.toHexString(entry.getValue())).append("#0#0#").append(jet);
            }
            isFirst = false;
        }
        return stats.toString();
    }

    public String parseStatsStringSansUserObvi() {
        if (getModelo().getType() == 83) //Si c'est une pierre d'�me vide
            return getModelo().getStrTemplate();

        StringBuilder stats = new StringBuilder();
        boolean isFirst = true;

        if (this instanceof FragmentosMagicos) {
            FragmentosMagicos fragment = (FragmentosMagicos) this;
            for (Doble<Integer, Integer> couple : fragment.getRunes()) {
                stats.append((stats.toString().isEmpty() ? couple.getPrimero() : ";"
                        + couple.getPrimero())).append(":").append(couple.getSegundo());
            }
            return stats.toString();
        }
        for (Entry<Integer, String> entry : txtStats.entrySet()) {
            if (!isFirst)
                stats.append(",");
            if (modelo.getType() == 77) {
                if (entry.getKey() == Constantes.STATS_PETS_PDV)
                    stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
                if (entry.getKey() == Constantes.STATS_PETS_POIDS)
                    stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#").append(entry.getValue()).append("#").append(entry.getValue());
                if (entry.getKey() == Constantes.STATS_PETS_DATE) {
                    if (entry.getValue().contains("#"))
                        stats.append(Integer.toHexString(entry.getKey())).append(entry.getValue());
                    else
                        stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(entry.getValue())));
                }
                //stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(entry.getValue())));
            } else if (entry.getKey() == Constantes.STATS_DATE) {
                if (entry.getValue().contains("#"))
                    stats.append(Integer.toHexString(entry.getKey())).append(entry.getValue());
                else
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(Long.parseLong(entry.getValue()));
            } else if (entry.getKey() == Constantes.STATS_CHANGE_BY || entry.getKey() == Constantes.STATS_NAME_TRAQUE || entry.getKey() == Constantes.STATS_OWNER_1) {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
            } else if (entry.getKey() == Constantes.STATS_GRADE_TRAQUE || entry.getKey() == Constantes.STATS_ALIGNEMENT_TRAQUE || entry.getKey() == Constantes.STATS_NIVEAU_TRAQUE) {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue()).append("#0");
            } else if (entry.getKey() == Constantes.STATS_NAME_DJ) {
                for (String i : entry.getValue().split(","))
                    stats.append(",").append(Integer.toHexString(entry.getKey())).append("#0#0#").append(i);
            } else if (entry.getKey() == Constantes.CAPTURE_MONSTRE) {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue());
            } else if (entry.getKey() == Constantes.STATS_PETS_PDV
                    || entry.getKey() == Constantes.STATS_PETS_POIDS
                    || entry.getKey() == Constantes.STATS_PETS_DATE) {
                MascotaEntrada p = Mundo.mundo.getPetsEntry(this.getId());
                if (p == null) {
                    if (entry.getKey() == Constantes.STATS_PETS_PDV)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append("a").append("#0#a");
                    if (entry.getKey() == Constantes.STATS_PETS_POIDS)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
                    if (entry.getKey() == Constantes.STATS_PETS_DATE)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
                } else {
                    if (entry.getKey() == Constantes.STATS_PETS_PDV)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(p.getPdv())).append("#0#").append(Integer.toHexString(p.getPdv()));
                    if (entry.getKey() == Constantes.STATS_PETS_POIDS)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(p.parseCorpulence()).append("#").append(p.getCorpulencia() > 0 ? p.parseCorpulence() : 0).append("#").append(p.parseCorpulence());
                    if (entry.getKey() == Constantes.STATS_PETS_DATE)
                        stats.append(Integer.toHexString(entry.getKey())).append(p.parseLastEatDate());
                    if (p.getIsEupeoh()
                            && entry.getKey() == Constantes.STATS_PETS_EPO)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(p.getIsEupeoh() ? 1 : 0)).append("#0#").append(Integer.toHexString(p.getIsEupeoh() ? 1 : 0));
                }
            } else {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
            }
            isFirst = false;
        }
        // Panoplie de classe (81 à 92)
        if (this.getModelo().getPanoId() >= 81 && this.getModelo().getPanoId() <= 92) {
            for (String spell : this.SortStats) {
                if (!isFirst) {
                    stats.append(",");
                }
                stats.append(spell);
                isFirst = false;
            }
        }
        for (EfectoHechizo SE : Effects) {
            if (!isFirst)
                stats.append(",");

            String[] infos = SE.getArgs().split(";");
            try {
                stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(infos[0]).append("#").append(infos[1]).append("#0#").append(infos[5]);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            isFirst = false;
        }
        for (Entry<Integer, Integer> entry : SoulStats.entrySet()) {
            if (!isFirst)
                stats.append(",");
            stats.append(Integer.toHexString(Constantes.STATS_PETS_SOUL)).append("#").append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#").append(Integer.toHexString(entry.getValue()));
            isFirst = false;
        }
        for (Entry<Integer, Integer> entry : Caracteristicas.getEffects().entrySet()) {
            if (!isFirst)
                stats.append(",");

            if(entry.getKey() == 615) {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(Integer.toHexString(entry.getValue()));
            } else {
                String jet = "0d0+" + entry.getValue();
                stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(entry.getValue()));
                stats.append("#0#0#").append(jet);
            }
            isFirst = false;
        }
        return stats.toString();
    }

    public String parseToSave() {
        return parseStatsStringSansUserObvi();
    }

    public String obvijevanOCO_Packet(int pos) {
        String strPos = String.valueOf(pos);
        if (pos == -1)
            strPos = "";
        String upPacket = "OCO";
        upPacket = upPacket + Integer.toHexString(getId()) + "~";
        upPacket = upPacket + Integer.toHexString(getModelo().getId()) + "~";
        upPacket = upPacket + Integer.toHexString(getCantidad()) + "~";
        upPacket = upPacket + strPos + "~";
        upPacket = upPacket + parseStatsString();
        this.setModification();
        return upPacket;
    }

    public void obvijevanNourir(ObjetoJuego obj) {
        if (obj == null)
            return;
        for (Entry<Integer, Integer> entry : Caracteristicas.getEffects().entrySet()) {
            if (entry.getKey() != 974) // on ne boost que la stat de l'exp�rience de l'obvi
                continue;
            if (entry.getValue() > 500) // si le boost a une valeur sup�rieure � 500 (irr�aliste)
                return;
            entry.setValue(entry.getValue().intValue()
                    + obj.getModelo().getLevel() / 3);
        }
        this.setModification();
    }

    public void obvijevanChangeStat(int statID, int val) {
        for (Entry<Integer, Integer> entry : Caracteristicas.getEffects().entrySet()) {
            if (entry.getKey() != statID)
                continue;
            entry.setValue(val);
        }
        this.setModification();
    }

    public void removeAllObvijevanStats() {
        setObvijevanPos(0);
        Caracteristicas StatsSansObvi = new Caracteristicas();
        for (Entry<Integer, Integer> entry : Caracteristicas.getEffects().entrySet()) {
            int statID = entry.getKey();
            if ((statID == 970) || (statID == 971) || (statID == 972)
                    || (statID == 973) || (statID == 974))
                continue;
            StatsSansObvi.addOneStat(statID, entry.getValue());
        }
        Caracteristicas = StatsSansObvi;
        this.setModification();
    }

    public void removeAll_ExepteObvijevanStats() {
        setObvijevanPos(0);
        Caracteristicas StatsSansObvi = new Caracteristicas();
        for (Entry<Integer, Integer> entry : Caracteristicas.getEffects().entrySet()) {
            int statID = entry.getKey();
            if ((statID != 971) && (statID != 972) && (statID != 973)
                    && (statID != 974))
                continue;
            StatsSansObvi.addOneStat(statID, entry.getValue());
        }
        Caracteristicas = StatsSansObvi;
        this.setModification();
    }

    public String getObvijevanStatsOnly() {
        ObjetoJuego obj = getCloneObjet(this, 1);
        obj.removeAll_ExepteObvijevanStats();
        this.setModification();
        return obj.parseStatsStringSansUserObvi();
    }

	/* *********FM SYSTEM********* */

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
                //Si c'est un Effet Actif
                if (a == statID) {
                    follow = false;
                    break;
                }
            if (!follow)
                continue;//Si c'�tait un effet Actif d'arme

            String jet = "";
            int value = 1;
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
                e.printStackTrace();
            }
            itemStats.addOneStat(statID, value);
        }
        return itemStats;
    }

    public ArrayList<EfectoHechizo> getEffects() {
        return Effects;
    }

    public ArrayList<EfectoHechizo> getCritEffects() {
        ArrayList<EfectoHechizo> effets = new ArrayList<>();
        for (EfectoHechizo SE : Effects) {
            try {
                boolean boost = true;
                for (int i : Constantes.NO_BOOST_CC_IDS)
                    if (i == SE.getEffectID()) {
                        boost = false;
                        break;
                    }
                String[] infos = SE.getArgs().split(";");
                if (!boost) {
                    effets.add(SE);
                    continue;
                }
                int min = Integer.parseInt(infos[0], 16)
                        + (boost ? modelo.getBonusCC() : 0);
                int max = Integer.parseInt(infos[1], 16)
                        + (boost ? modelo.getBonusCC() : 0);
                String jet = "1d" + (max - min + 1) + "+" + (min - 1);
                //exCode: String newArgs = Integer.toHexString(min)+";"+Integer.toHexString(max)+";-1;-1;0;"+jet;
                //osef du minMax, vu qu'on se sert du jet pour calculer les d�gats
                String newArgs = "0;0;0;-1;0;" + jet;
                effets.add(new EfectoHechizo(SE.getEffectID(), newArgs, 0, -1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return effets;
    }

    public void clearStats() {
        //On vide l'item de tous ces effets
        Caracteristicas = new Caracteristicas();
        Effects.clear();
        txtStats.clear();
        SortStats.clear();
        SoulStats.clear();
        this.setModification();
    }

    public void refreshStatsObjet(String newsStats) {
        parseStringToStats(newsStats, true);
        this.setModification();
    }

    public int getResistance(String statsTemplate) {
        int Resistance = 0;

        String[] splitted = statsTemplate.split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            if (Integer.parseInt(stats[0], 16) == Constantes.STATS_RESIST) {
                Resistance = Integer.parseInt(stats[2], 16);
            }
        }
        return Resistance;
    }

    public int getResistanceMax(String statsTemplate) {
        int ResistanceMax = 0;

        String[] splitted = statsTemplate.split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            if (Integer.parseInt(stats[0], 16) == Constantes.STATS_RESIST) {
                ResistanceMax = Integer.parseInt(stats[1], 16);
            }
        }
        return ResistanceMax;
    }

    public int getRandomValue(String statsTemplate, int statsId) {
        if (statsTemplate.equals(""))
            return 0;

        String[] splitted = statsTemplate.split(",");
        int value = 0;
        for (String s : splitted) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            if (statID != statsId)
                continue;
            String jet;
            try {
                jet = stats[4];
                value = Formulas.getRandomJet(jet);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return value;
    }

    /** FM TOUT POURRI **/
    public String parseStringStatsEC_FM(ObjetoJuego obj, double poid, int carac) {
        StringBuilder stats = new StringBuilder();
        boolean first = false;
        double perte = 0.0;
        for (EfectoHechizo EH : obj.Effects) {
            if (first)
                stats.append(",");
            String[] infos = EH.getArgs().split(";");
            try {
                stats.append(Integer.toHexString(EH.getEffectID())).append("#").append(infos[0]).append("#").append(infos[1]).append("#0#").append(infos[5]);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            first = true;
        }
        Map<Integer, Integer> statsObj = new HashMap<>(obj.Caracteristicas.getEffects());
        ArrayList<Integer> keys = new ArrayList<>(obj.Caracteristicas.getEffects().keySet());
        Collections.shuffle(keys);
        int p = 0;
        int key = 0;
        if (keys.size() > 1) {
            for (Integer i : keys) // On cherche un OverFM
            {
                int value = statsObj.get(i);
                if (this.isOverFm(i, value)) {
                    key = i;
                    break;
                }
                p++;
            }
            if (key > 0) // On place l'OverFm en t�te de liste pour �tre niqu�
            {
                keys.remove(p);
                keys.add(p, keys.get(0));
                keys.remove(0);
                keys.add(0, key);
            }
        }
        for (Integer i : keys) {
            int newstats = 0;
            int statID = i;
            int value = statsObj.get(i);
            if (perte > poid || statID == carac) {
                newstats = value;
            } else if ((statID == 152) || (statID == 154) || (statID == 155)
                    || (statID == 157) || (statID == 116) || (statID == 153)) {
                float a = (float) (value * poid / 100.0D);
                if (a < 1.0F)
                    a = 1.0F;
                float chute = value + a;
                newstats = (int) Math.floor(chute);
                if (newstats > OficioAccion.getBaseMaxJet(obj.getModelo().getId(), Integer.toHexString(i)))
                    newstats = OficioAccion.getBaseMaxJet(obj.getModelo().getId(), Integer.toHexString(i));

            } else {
                if ((statID == 127) || (statID == 101))
                    continue;

                float chute;
                if (this.isOverFm(statID, value)) // Gros kick dans la gueulle de l'over FM
                    chute = (float) (value - value
                            * (poid - (int) Math.floor(perte)) * 2 / 100.0D);
                else
                    chute = (float) (value - value
                            * (poid - (int) Math.floor(perte)) / 100.0D);
                if ((chute / (float) value) < 0.75)
                    chute = ((float) value) * 0.75F; // On ne peut pas perdre plus de 25% d'une stat d'un coup

                double chutePwr = (value - chute)
                        * OficioAccion.getPwrPerEffet(statID);
                //int chutePwrFixe = (int) Math.floor(chutePwr);

                perte += chutePwr;

				/*
				 * if (obj.getPuit() > 0 && chutePwrFixe <= obj.getPuit()) //
				 * S'il y a un puit positif, on annule la baisse { perte +=
				 * chutePwr; chute = value; // On r�initialise
				 * obj.setPuit(obj.getPuit() - chutePwrFixe); // On descend le
				 * puit } else if (obj.getPuit() > 0) // Si le puit est positif,
				 * mais pas suffisant pour annuler { double pwr =
				 * obj.getPuit()/World.getPwrPerEffet(statID); // On calcule
				 * l'annulation possible de la chute chute += (int)
				 * Math.floor(pwr); // On l'a r�ajoute perte +=
				 * (value-chute)*World.getPwrPerEffet(statID); obj.setPuit(0);
				 * // On fixe le puit � 0 } else { perte += chutePwr; }
				 */

                newstats = (int) Math.floor(chute);
            }
            if (newstats < 1)
                continue;
            String jet = "0d0+" + newstats;
            if (first)
                stats.append(",");
            stats.append(Integer.toHexString(statID)).append("#").append(Integer.toHexString(newstats)).append("#0#0#").append(jet);
            first = true;
        }
        for (Entry<Integer, String> entry : obj.txtStats.entrySet()) {
            if (first)
                stats.append(",");
            stats.append(Integer.toHexString((entry.getKey()))).append("#0#0#0#").append(entry.getValue());
            first = true;
        }
        return stats.toString();
    }


    public String parseFMStatsString(String statsstr, ObjetoJuego obj, int add,
                                     boolean negatif) {
        StringBuilder stats = new StringBuilder();
        boolean isFirst = true;
        for (EfectoHechizo SE : obj.Effects) {
            if (!isFirst)
                stats.append(",");

            String[] infos = SE.getArgs().split(";");
            try {
                stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(infos[0]).append("#").append(infos[1]).append("#0#").append(infos[5]);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            isFirst = false;
        }

        for (Entry<Integer, Integer> entry : obj.Caracteristicas.getEffects().entrySet()) {
            if (!isFirst)
                stats.append(",");
            if (Integer.toHexString(entry.getKey()).compareTo(statsstr) == 0) {
                int newstats = 0;
                if (negatif) {
                    newstats = entry.getValue() - add;
                    if (newstats < 1)
                        continue;
                } else {
                    newstats = entry.getValue() + add;
                }
                String jet = "0d0+" + newstats;
                stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(entry.getValue() + add)).append("#0#0#").append(jet);
            } else {
                String jet = "0d0+" + entry.getValue();
                stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(entry.getValue())).append("#0#0#").append(jet);
            }
            isFirst = false;
        }

        for (Entry<Integer, String> entry : obj.txtStats.entrySet()) {
            if (!isFirst)
                stats.append(",");
            stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
            isFirst = false;
        }

        return stats.toString();
    }

    public boolean isOverFm(int stat, int val) {
        boolean trouve = false;
        String statsTemplate = "";
        statsTemplate = this.modelo.getStrTemplate();
        if (statsTemplate == null || statsTemplate.isEmpty())
            return false;
        String[] split = statsTemplate.split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            if (statID != stat)
                continue;

            trouve = true;
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
            if (val > value)
                return true;
        }
        return !trouve;
    }

    public boolean isSameStats(ObjetoJuego newObj) {
        boolean effects = true, check = false;
        for(EfectoHechizo effect0 : this.Effects) {
            for(EfectoHechizo effect1 : newObj.Effects) {
                if(effect0.getEffectID() == effect1.getEffectID() && effect0.getJet().equals(effect1.getJet())) {
                    check = true;
                    break;
                }
            }
            if(!check) {
                effects = false;
                break;
            }
        }
        check = false;
        for(String spellStats0 : this.SortStats) {
            for(String spellStats1 : newObj.SortStats) {
                if(spellStats0.equals(spellStats1)) {
                    check = true;
                    break;
                }
            }
            if(!check) {
                effects = false;
                break;
            }
        }
        check = false;
        for(String effect0 : this.txtStats.values()) {
            for(String effect1 : newObj.txtStats.values()) {
                if(effect0.equals(effect1)) {
                    check = true;
                    break;
                }
            }
            if(!check) {
                effects = false;
                break;
            }
        }
        check = false;
        for(Entry<Integer, Integer> effect0 : this.SoulStats.entrySet()) {
            for(Entry<Integer, Integer> effect1 : newObj.SoulStats.entrySet()) {
                if(effect0.getKey().intValue() == effect1.getKey().intValue() && effect0.getValue().intValue() == effect1.getValue().intValue()) {
                    check = true;
                    break;
                }
            }
            if(!check) {
                effects = false;
                break;
            }
        }
        return effects && this.getCaracteristicas().isSameStats(newObj.getCaracteristicas());
    }

    public boolean isOverFm2(int stat, int val)
    {
        boolean trouve = false;
        String statsTemplate = "";
        statsTemplate = this.modelo.getStrTemplate();
        if (statsTemplate == null || statsTemplate.isEmpty())
            return false;
        String[] split = statsTemplate.split(",");
        for (String s : split)
        {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            if (statID != stat)
                continue;

            trouve = true;
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
            try
            {
                jet = stats[4];
                value = Formulas.getRandomJet(jet);
                try
                {
                    int min = Integer.parseInt(stats[1], 16);
                    int max = Integer.parseInt(stats[2], 16);
                    value = min;
                    if (max != 0)
                        value = max;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    value = Formulas.getRandomJet(jet);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            if (val == value)
                return true;
        }
        if (!trouve)
            return true;
        return false;
    }
}
