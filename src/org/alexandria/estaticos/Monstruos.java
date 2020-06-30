package org.alexandria.estaticos;

import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.configuracion.Constantes;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.juego.mundo.Mundo.Drop;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.EfectoHechizo;
import org.alexandria.estaticos.pelea.hechizo.Hechizo;
import org.alexandria.estaticos.cliente.Jugador.Caracteristicas;
import org.alexandria.otro.utilidad.Temporizador;

import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;

public class Monstruos {
    private final int id;
    private int gfxId;
    private int align;
    private String colors;
    private int ia = 0;
    private int minKamas;
    private int maxKamas;
    private final Map<Integer, MobGrade> grades = new HashMap<>();
    private final ArrayList<Drop> drops = new ArrayList<>();
    private boolean isCapturable;
    private int aggroDistance = 0;

    public Monstruos(int id, int gfxId, int align, String colors,
                     String thisGrades, String thisSpells, String thisStats,
                     String thisStatsInfos, String thisPdvs, String thisPoints,
                     String thisInit, int minKamas, int maxKamas, String thisXp, int ia,
                     boolean capturable, int aggroDistance) {
        this.id = id;
        this.gfxId = gfxId;
        this.align = align;
        this.colors = colors;
        this.minKamas = minKamas;
        this.maxKamas = maxKamas;
        this.ia = ia;
        this.isCapturable = capturable;
        this.aggroDistance = aggroDistance;
        int G = 1;

        for (int n = 0; n < 12; n++) {
            try {
                //Grades
                String[] split = thisGrades.split("\\|");
                String grade = split[n];
                String[] infos = grade.split("@");
                int level = Integer.parseInt(infos[0]);
                String resists = infos[1];
                //Stats
                String stats = thisStats.split("\\|")[n];
                //Spells
                String spells = "";
                if (!thisSpells.equalsIgnoreCase("||||")
                        && !thisSpells.equalsIgnoreCase("")
                        && !thisSpells.equalsIgnoreCase("-1")) {
                    spells = thisSpells.split("\\|")[n];
                    if (spells.equals("-1"))
                        spells = "";
                }
                //PDVMax//init
                int pdvmax = 1;
                int init = 1;

                try {
                    pdvmax = Integer.parseInt(thisPdvs.split("\\|")[n]);
                    init = Integer.parseInt(thisInit.split("\\|")[n]);
                } catch (Exception e) {
                    e.printStackTrace();
                    Mundo.mundo.logger.error("#1# Erreur lors du chargement du monstre (template) : "
                             + id);
                }
                //PA / PM
                int PA = 3;
                int PM = 3;
                int xp = 10;

                try {
                    String[] pts = thisPoints.split("\\|")[n].split(";");
                    try {
                        PA = Integer.parseInt(pts[0]);
                        PM = Integer.parseInt(pts[1]);
                        xp = Integer.parseInt(thisXp.split("\\|")[n]);
                    } catch (Exception e1) {
                        Mundo.mundo.logger.error("#2# Erreur lors du chargement du monstre (template) : "
                                + id);
                        e1.printStackTrace();
                    }
                } catch (Exception e) {
                    Mundo.mundo.logger.error("#3# Erreur lors du chargement du monstre (template) : "
                            + id);
                    e.printStackTrace();
                }
                grades.put(G, new MobGrade(this, G, level, PA, PM, resists, stats, thisStatsInfos, spells, pdvmax, init, xp, n));
                G++;
            } catch (Exception e) {
                // ok, pour les dopeuls ...
                //TODO: Enlever toutes les erreurs
            }
        }
    }

    public void setInfos(int gfxId, int align, String colors,
                         String thisGrades, String thisSpells, String thisStats,
                         String thisStatsInfos, String thisPdvs, String thisPoints,
                         String thisInit, int minKamas, int maxKamas, String thisXp, int ia,
                         boolean capturable, int aggroDistance) {
        this.gfxId = gfxId;
        this.align = align;
        this.colors = colors;
        this.minKamas = minKamas;
        this.maxKamas = maxKamas;
        this.ia = ia;
        this.isCapturable = capturable;
        this.aggroDistance = aggroDistance;
        int G = 1;
        grades.clear();
        for (int n = 0; n < 12; n++) {
            try {
                //Grades
                String grade = thisGrades.split("\\|")[n];
                String[] infos = grade.split("@");
                int level = Integer.parseInt(infos[0]);
                String resists = infos[1];
                //Stats
                String stats = thisStats.split("\\|")[n];
                //Spells
                String spells = thisSpells.split("\\|")[n];
                if (spells.equals("-1"))
                    spells = "";
                //PDVMax//init
                int pdvmax = 1;
                int init = 1;

                try {
                    pdvmax = Integer.parseInt(thisPdvs.split("\\|")[n]);
                    init = Integer.parseInt(thisInit.split("\\|")[n]);
                } catch (Exception e) {
                    e.printStackTrace();
                    Mundo.mundo.logger.error("#4# Erreur lors du chargement du monstre (template) : "
                            + id);
                }
                //PA / PM
                int PA = 3;
                int PM = 3;
                int xp = 10;

                try {
                    String[] pts = thisPoints.split("\\|")[n].split(";");
                    try {
                        PA = Integer.parseInt(pts[0]);
                        PM = Integer.parseInt(pts[1]);
                        xp = Integer.parseInt(thisXp.split("\\|")[n]);
                    } catch (Exception e1) {
                        Mundo.mundo.logger.error("#5# Erreur lors du chargement du monstre (template) : "
                                + id);
                        e1.printStackTrace();
                    }
                } catch (Exception e) {
                    Mundo.mundo.logger.error("#6# Erreur lors du chargement du monstre (template) : "
                            + id);
                    e.printStackTrace();
                }
                grades.put(G, new MobGrade(this, G, level, PA, PM, resists, stats, thisStatsInfos, spells, pdvmax, init, xp, n));
                G++;
            } catch (Exception e) {
                // ok pour les dopeuls
                e.printStackTrace();
            }
        }
    }

    public int getId() {
        return this.id;
    }

    public int getGfxId() {
        return this.gfxId;
    }

    public int getAlign() {
        return this.align;
    }

    public String getColors() {
        return this.colors;
    }

    public int getIa() {
        return this.ia;
    }

    public int getMinKamas() {
        return this.minKamas;
    }

    public int getMaxKamas() {
        return this.maxKamas;
    }

    public Map<Integer, MobGrade> getGrades() {
        return this.grades;
    }

    public void addDrop(Drop D) {
        this.drops.add(D);
    }

    public ArrayList<Drop> getDrops() {
        return this.drops;
    }

    public boolean isCapturable() {
        return this.isCapturable;
    }

    public int getAggroDistance() {
        return this.aggroDistance;
    }

    public MobGrade getGradeByLevel(int lvl) {
        if(this.getGrades() == null) return null;
        for (MobGrade grade : this.getGrades().values())
            if (grade != null && grade.getLevel() == lvl)
                return grade;
        return null;
    }

    public MobGrade getRandomGrade() {
        if(this.getGrades() == null) return null;
        int randomgrade = (int) (Math.random() * (6 - 1)) + 1, graderandom = 1;

        for (MobGrade grade : this.getGrades().values()) {
            if (grade != null && graderandom == randomgrade) return grade;
            else graderandom++;
        }

        return null;
    }

    public static class MobGroup {
        public static final MaestroCuerbok MAITRE_CORBAC = new MaestroCuerbok();

        private final int id;
        private int cellId;
        private int orientation = 2;
        private int align = -1;
        private short starBonus;
        private int aggroDistance = 0;
        private int subarea = -1;
        private boolean changeAgro = false;
        private boolean isFix = false;
        private boolean isExtraGroup = false;
        private final Map<Integer, MobGrade> mobs = new HashMap<>();
        private String condition = "";
        private Timer condTimer;
        private ArrayList<ObjetoJuego> objects;

        public MobGroup(int Aid, int Aalign, ArrayList<MobGrade> possibles,
                        Mapa Map, int cell, int fixSize, int minSize, int maxSize,
                        MobGrade extra) {

            id = Aid;
            align = Aalign;
            //Determinamo el nombre de un grupo de moobs
            int rand = 0;
            int nbr = 0;
            //region Nombre de monstre
            if (fixSize > 0 && fixSize < 9) {
                nbr = fixSize;
            } else if (minSize != -1 && maxSize != -1 && maxSize != 0
                    && (minSize < maxSize)) {
                if (minSize == 3 && maxSize == 8) {
                    rand = Formulas.getRandomValue(0, 99);
                    if (rand < 25) //3: 25%
                    {
                        nbr = 3;
                    } else if (rand < 48) //4:23%
                    {
                        nbr = 4;
                    } else if (rand < 51) //5:20%
                    {
                        nbr = 5;
                    } else if (rand < 85) //6:17%
                    {
                        nbr = 6;
                    } else if (rand < 95) //7:10%
                    {
                        nbr = 7;
                    } else
                    //8:5%
                    {
                        nbr = 8;
                    }
                } else if (minSize == 1 && maxSize == 3) { // 21 - normalement tout astrub
                    rand = Formulas.getRandomValue(0, 99);
                    if (rand < 40) //1: 40%
                    {
                        nbr = 1;
                    } else if (rand < 75)//2: 35%
                    {
                        nbr = 2;
                    } else
                    //3: 25%
                    {
                        nbr = 3;
                    }
                } else if (minSize == 1 && maxSize == 5) {
                    rand = Formulas.getRandomValue(0, 99);
                    if (rand < 30) //3: 30%
                    {
                        nbr = 1;
                    } else if (rand < 53) //4:23%
                    {
                        nbr = 2;
                    } else if (rand < 73) //5:20%
                    {
                        nbr = 3;
                    } else if (rand < 90) //6:17%
                    {
                        nbr = 4;
                    } else
                    //8:10%
                    {
                        nbr = 5;
                    }
                } else if (minSize == 1 && maxSize == 4) {
                    rand = Formulas.getRandomValue(0, 99);
                    if (rand < 35) //3: 35%
                    {
                        nbr = 1;
                    } else if (rand < 61) //4:26%
                    {
                        nbr = 2;
                    } else if (rand < 82) //5:21%
                    {
                        nbr = 3;
                    } else
                    //8:18%
                    {
                        nbr = 4;
                    }
                } else {
                    nbr = Formulas.getRandomValue(minSize, maxSize);
                }
            } else if (minSize == -1) {
                switch (maxSize) {
                    case 0:
                        return;
                    case 1:
                        nbr = 1;
                        break;
                    case 2:
                        nbr = Formulas.getRandomValue(1, 2); //1:50%	2:50%
                        break;
                    case 3:
                        nbr = Formulas.getRandomValue(1, 3); //1:33.3334%	2:33.3334%	3:33.3334%
                        break;
                    case 4:
                        rand = Formulas.getRandomValue(0, 99);
                        if (rand < 22) //1:22%
                            nbr = 1;
                        else if (rand < 48) //2:26%
                            nbr = 2;
                        else if (rand < 74) //3:26%
                            nbr = 3;
                        else
                            //4:26%
                            nbr = 4;
                        break;
                    case 5:
                        rand = Formulas.getRandomValue(0, 99);
                        if (rand < 15) //1:15%
                            nbr = 1;
                        else if (rand < 35) //2:20%
                            nbr = 2;
                        else if (rand < 60) //3:25%
                            nbr = 3;
                        else if (rand < 85) //4:25%
                            nbr = 4;
                        else
                            //5:15%
                            nbr = 5;
                        break;
                    case 6:
                        rand = Formulas.getRandomValue(0, 99);
                        if (rand < 10) //1:10%
                            nbr = 1;
                        else if (rand < 25) //2:15%
                            nbr = 2;
                        else if (rand < 45) //3:20%
                            nbr = 3;
                        else if (rand < 65) //4:20%
                            nbr = 4;
                        else if (rand < 85) //5:20%
                            nbr = 5;
                        else
                            //6:15%
                            nbr = 6;
                        break;
                    case 7:
                        rand = Formulas.getRandomValue(0, 99);
                        if (rand < 9) //1:9%
                            nbr = 1;
                        else if (rand < 20) //2:11%
                            nbr = 2;
                        else if (rand < 35) //3:15%
                            nbr = 3;
                        else if (rand < 55) //4:20%
                            nbr = 4;
                        else if (rand < 75) //5:20%
                            nbr = 5;
                        else if (rand < 91) //6:16%
                            nbr = 6;
                        else
                            //7:9%
                            nbr = 7;
                        break;
                    default:
                        rand = Formulas.getRandomValue(0, 99);
                        if (rand < 9) //1:9%
                            nbr = 1;
                        else if (rand < 20) //2:11%
                            nbr = 2;
                        else if (rand < 33) //3:13%
                            nbr = 3;
                        else if (rand < 50) //4:17%
                            nbr = 4;
                        else if (rand < 67) //5:17%
                            nbr = 5;
                        else if (rand < 80) //6:13%
                            nbr = 6;
                        else if (rand < 91) //7:11%
                            nbr = 7;
                        else
                            //8:9%
                            nbr = 8;
                        break;
                }
            } else {
                switch (minSize) {
                    case 1 -> {
                        rand = Formulas.getRandomValue(1, 8);
                        nbr = switch (rand) {
                            case 1 -> 1;
                            case 2 -> 2;
                            case 3 -> 3;
                            case 4 -> 4;
                            case 5 -> 5;
                            case 6 -> 6;
                            case 7 -> 7;
                            case 8 -> 8;
                            default -> nbr;
                        };
                    }
                    case 2 -> {
                        rand = Formulas.getRandomValue(2, 8);
                        nbr = switch (rand) {
                            case 2 -> 2;
                            case 3 -> 3;
                            case 4 -> 4;
                            case 5 -> 5;
                            case 6 -> 6;
                            case 7 -> 7;
                            case 8 -> 8;
                            default -> nbr;
                        };
                    }
                    case 3 -> {
                        rand = Formulas.getRandomValue(3, 8);
                        nbr = switch (rand) {
                            case 3 -> 3;
                            case 4 -> 4;
                            case 5 -> 5;
                            case 6 -> 6;
                            case 7 -> 7;
                            case 8 -> 8;
                            default -> nbr;
                        };
                    }
                    case 4 -> {
                        rand = Formulas.getRandomValue(4, 8);
                        nbr = switch (rand) {
                            case 4 -> 4;
                            case 5 -> 5;
                            case 6 -> 6;
                            case 7 -> 7;
                            case 8 -> 8;
                            default -> nbr;
                        };
                    }
                    case 5 -> {
                        rand = Formulas.getRandomValue(5, 8);
                        nbr = switch (rand) {
                            case 5 -> 5;
                            case 6 -> 6;
                            case 7 -> 7;
                            case 8 -> 8;
                            default -> nbr;
                        };
                    }
                    case 6 -> {
                        rand = Formulas.getRandomValue(6, 8);
                        nbr = switch (rand) {
                            case 6 -> 6;
                            case 7 -> 7;
                            case 8 -> 8;
                            default -> nbr;
                        };
                    }
                    case 7 -> {
                        rand = Formulas.getRandomValue(7, 8);
                        nbr = switch (rand) {
                            case 7 -> 7;
                            case 8 -> 8;
                            default -> nbr;
                        };
                    }
                    case 8 -> nbr = 8;
                    default -> {
                        rand = Formulas.getRandomValue(1, 8);
                        nbr = switch (rand) {
                            case 1 -> 1;
                            case 2 -> 2;
                            case 3 -> 3;
                            case 4 -> 4;
                            case 5 -> 5;
                            case 6 -> 6;
                            case 7 -> 7;
                            case 8 -> 8;
                            default -> nbr;
                        };
                    }
                }
            }
            //endregion
            int guid = -1;
            boolean haveSameAlign = false;

            if (extra != null) {
                isExtraGroup = true;
                nbr--;
                this.mobs.put(guid, extra);
                guid--;
            }
            //On v�rifie qu'il existe des monstres de l'alignement demand� pour �viter les boucles infinies
            for (MobGrade mob : possibles)
                if (mob.getTemplate().getAlign() == this.align) {
                    haveSameAlign = true;
                    break;
                }
            if (!haveSameAlign)
                return;//S'il n'y en a pas
            for (int a = 0; a < nbr; a++) {
                MobGrade Mob = null;
                do {
                    int random = Formulas.getRandomValue(0, possibles.size() - 1);//on prend un mob au hasard dans le tableau
                    Mob = possibles.get(random).getCopy();
                }
                while (Mob.getTemplate().getAlign() != this.align);

                this.mobs.put(guid, Mob);
                if (Mob.getTemplate().getAggroDistance() > this.aggroDistance)
                    this.aggroDistance = Mob.getTemplate().getAggroDistance();
                guid--;
            }

            this.cellId = (cell == -1 ? Map.getRandomFreeCellId() : cell);

            while (Map.containsForbiddenCellSpawn(this.cellId))
                this.cellId = Map.getRandomFreeCellId();
            if (this.cellId == 0)
                return;
            this.orientation = (Formulas.getRandomValue(0, 3) * 2) + 1;
            this.isFix = false;
            this.starBonus = 100;
        }

        public MobGroup(int id, int cellId, String groupData, String objects, short stars) {
            this.id = id;
            this.align = Constantes.ALINEAMIENTO_NEUTRAL;
            this.cellId = cellId;
            this.isFix = false;
            this.orientation = (Formulas.getRandomValue(0, 3) * 2) + 1;
            this.starBonus = stars;

            int guid = -1;

            for (String data : groupData.split(";")) {
                if (data.equalsIgnoreCase(""))
                    continue;
                String[] infos = data.split(",");

                try {
                    int idMonster = Integer.parseInt(infos[0]);
                    int min = Integer.parseInt(infos[1]);
                    int max = Integer.parseInt(infos[2]);
                    Monstruos m = Mundo.mundo.getMonstre(idMonster);
                    List<MobGrade> mgs = new ArrayList<>();
                    //on ajoute a la liste les grades possibles

                    for (MobGrade MG : m.getGrades().values())
                        if (MG.level >= min && MG.level <= max)
                            mgs.add(MG);
                    if (mgs.isEmpty())
                        continue;
                    //On prend un grade au hasard entre 0 et size -1 parmis les mobs possibles
                    this.mobs.put(guid, mgs.get(Formulas.getRandomValue(0, mgs.size() - 1)));
                    if (m.getAggroDistance() > this.aggroDistance)
                        this.aggroDistance = m.getAggroDistance();
                    guid--;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for(Entry<Integer, MobGrade> mob :this.mobs.entrySet())// kralamour
                if (mob.getValue().getTemplate().getId() == 423) {
                    this.orientation = 3;
                    break;
                }

            if(!objects.isEmpty()) {
                for (String value : objects.split(",")) {
                    final ObjetoJuego gameObject = Mundo.getGameObject(Integer.parseInt(value));
                    if (gameObject != null)
                        this.objects.add(gameObject);
                }
            }
        }

        public MobGroup(int id, int cellId, String groupData) {
            this.id = id;
            this.align = Constantes.ALINEAMIENTO_NEUTRAL;

            this.cellId = cellId;
            this.isFix = true;
            int guid = -1;
            boolean star = false;
            for (String data : groupData.split(";")) {
                if (data.equalsIgnoreCase(""))
                    continue;
                String[] infos = data.split(",");
                try {
                    int idMonster = Integer.parseInt(infos[0]);
                    int min = Integer.parseInt(infos[1]);
                    int max = Integer.parseInt(infos[2]);
                    Monstruos m = Mundo.mundo.getMonstre(idMonster);
                    List<MobGrade> mgs = new ArrayList<>();
                    //on ajoute a la liste les grades possibles

                    for (MobGrade MG : m.getGrades().values()) {
                        if (MG.getBaseXp() != 0)
                            star = true;
                        if (MG.level >= min && MG.level <= max)
                            mgs.add(MG);
                    }
                    if (mgs.isEmpty())
                        continue;
                    //On prend un grade au hasard entre 0 et size -1 parmis les mobs possibles
                    this.mobs.put(guid, mgs.get(Formulas.getRandomValue(0, mgs.size() - 1)));
                    if (m.getAggroDistance() > this.aggroDistance)
                        this.aggroDistance = m.getAggroDistance();
                    guid--;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.orientation = (Formulas.getRandomValue(0, 3) * 2) + 1;
            this.starBonus = 100;
        }

        public void setSubArea(int sa) {
            this.subarea = sa;
        }

        public void changeAgro() {
            if (!changeAgro) {
                if (this.haveMineur()) {
                    // 29 : sous-terrain
                    // 96 : exploitation mini�re d'astrub
                    // 31 : passage vers brakmar
                    if (this.subarea != 29 && this.subarea != 96
                            && this.subarea != 31) {
                        this.removeAgro(118);
                    }
                }
            }
            changeAgro = true;
        }

        public void removeAgro(int id) {
            this.aggroDistance = 0;
            for (Entry<Integer, MobGrade> e : this.mobs.entrySet()) {
                MobGrade mb = e.getValue();
                if (mb.template.getId() != id) {
                    if (mb.template.getAggroDistance() > this.aggroDistance) {
                        this.aggroDistance = mb.template.getAggroDistance();
                    }
                }
            }
        }

        public boolean haveMineur() {
            for (Entry<Integer, MobGrade> e : this.mobs.entrySet()) {
                MobGrade mb = e.getValue();
                if (mb.template.getId() == 118) {
                    return true;
                }
            }
            return false;
        }

        public int getId() {
            return this.id;
        }

        public int getCellId() {
            return this.cellId;
        }

        public void setCellId(int cellId) {
            this.cellId = cellId;
        }

        public int getOrientation() {
            return this.orientation;
        }

        public void setOrientation(int orientation) {
            this.orientation = orientation;
        }

        public int getAlignement() {
            return this.align;
        }

        public void addBonusEstrellas() {
            if(this.getStarBonus() >= 200) {
                this.starBonus = 200;
            } else {
                this.starBonus += 3;
            }
        }

        public int getStarBonus() {
            return this.starBonus == -1 ? 0 : this.starBonus;
        }

        public int getAggroDistance() {
            return this.aggroDistance;
        }

        public boolean isFix() {
            return this.isFix;
        }

        public void setIsFix(boolean isFix) {
            this.isFix = isFix;
        }

        public boolean getIsExtraGroup() {
            return this.isExtraGroup;
        }

        public Map<Integer, MobGrade> getMobs() {
            return this.mobs;
        }

        public MobGrade getMobGradeById(int id) {
            return this.mobs.get(id);
        }

        public String getCondition() {
            return this.condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public void startCondTimer() {
            this.condTimer = new Timer();
            condTimer.schedule(new TimerTask() {
                public void run() {
                    condition = "";
                }
            }, 60000 * 10);
        }

        public void stopConditionTimer() {
            try {
                this.condTimer.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public ArrayList<ObjetoJuego> getObjects() {
            if(this.objects == null && Configuracion.INSTANCE.getHEROIC())
                this.objects = new ArrayList<>();
            else if(!Configuracion.INSTANCE.getHEROIC())
                return new ArrayList<>();
            return objects;
        }

        public String parseGM() {
            StringBuilder mobIDs = new StringBuilder();
            StringBuilder mobGFX = new StringBuilder();
            StringBuilder mobLevels = new StringBuilder();
            StringBuilder colors = new StringBuilder();
            StringBuilder toreturn = new StringBuilder();
            long  totalExp = 0;

            boolean isFirst = true;
            if (this.mobs.isEmpty())
                return "";

            for (Entry<Integer, MobGrade> entry : this.mobs.entrySet()) {
                if (!isFirst) {
                    mobIDs.append(",");
                    mobGFX.append(",");
                    mobLevels.append(",");
                }
                mobIDs.append(entry.getValue().getTemplate().getId());
                mobGFX.append(entry.getValue().getTemplate().getGfxId()).append("^").append(entry.getValue().getSize());
                mobLevels.append(entry.getValue().getLevel());
                colors.append(entry.getValue().getTemplate().getColors()).append(";0,0,0,0;");
                isFirst = false;
            }
            toreturn.append("+").append(this.cellId).append(";").append(this.orientation).append(";");
            toreturn.append(getStarBonus());// bonus en pourcentage (�toile/20%) // Actuellement 1%/min
            toreturn.append(";").append(this.id).append(";").append(mobIDs).append(";-3;").append(mobGFX).append(";").append(mobLevels).append(";").append(colors);
            return toreturn.toString();
        }
    }

    public static class MobGrade {
        private static final int pSize = 2;
        private final Monstruos template;
        private final int grade;
        private final int level;
        private int pdv;
        private int pdvMax;
        private int inFightId;
        private int init;
        private final int pa;
        private final int pm;
        private final int size;
        private int baseXp = 10;
        private GameCase fightCell;
        private final ArrayList<EfectoHechizo> fightBuffs = new ArrayList<>();
        private Map<Integer, Integer> stats = new HashMap<>();
        private Map<Integer, Hechizo.SortStats> spells = new HashMap<>();
        private ArrayList<Integer> statsInfos = new ArrayList<>();

        public MobGrade(Monstruos template, int grade, int level, int pa, int pm, String resists, String stats, String statsInfos, String allSpells, int pdvMax, int aInit, int xp, int n) {
            this.size = 100 + n * pSize;
            this.template = template;
            this.grade = grade;
            this.level = level;
            this.pdvMax = pdvMax;
            this.pdv = pdvMax;
            this.pa = pa;
            this.pm = pm;
            this.baseXp = xp;
            this.init = aInit;
            this.stats.clear();
            this.spells.clear();

            String[] resist = resists.split(";"), stat = stats.split(","), statInfos = statsInfos.split(";");

            for (String str : statInfos)
                this.statsInfos.add(Integer.parseInt(str));

            try {
                if(resist.length > 3) {
                    this.stats.put(Constantes.STATS_ADD_RP_NEU, Integer.parseInt(resist[0]));
                    this.stats.put(Constantes.STATS_ADD_RP_TER, Integer.parseInt(resist[1]));
                    this.stats.put(Constantes.STATS_ADD_RP_FEU, Integer.parseInt(resist[2]));
                    this.stats.put(Constantes.STATS_ADD_RP_EAU, Integer.parseInt(resist[3]));
                    this.stats.put(Constantes.STATS_ADD_RP_AIR, Integer.parseInt(resist[4]));
                    this.stats.put(Constantes.STATS_ADD_AFLEE, Integer.parseInt(resist[5]));
                    this.stats.put(Constantes.STATS_ADD_MFLEE, Integer.parseInt(resist[6]));
                } else {
                    String[] split = resist[0].split(",");
                    this.stats.put(-1, Integer.parseInt(split[0]));
                    this.stats.put(-100, Integer.parseInt(split[1]));
                    this.stats.put(Constantes.STATS_ADD_AFLEE, Integer.parseInt(resist[1]));
                    this.stats.put(Constantes.STATS_ADD_MFLEE, Integer.parseInt(resist[2]));
                }

                this.stats.put(Constantes.STATS_ADD_FORC, Integer.parseInt(stat[0]));
                this.stats.put(Constantes.STATS_ADD_SAGE, Integer.parseInt(stat[1]));
                this.stats.put(Constantes.STATS_ADD_INTE, Integer.parseInt(stat[2]));
                this.stats.put(Constantes.STATS_ADD_CHAN, Integer.parseInt(stat[3]));
                this.stats.put(Constantes.STATS_ADD_AGIL, Integer.parseInt(stat[4]));
                this.stats.put(Constantes.STATS_ADD_DOMA, Integer.parseInt(statInfos[0]));
                this.stats.put(Constantes.STATS_ADD_PERDOM, Integer.parseInt(statInfos[1]));
                this.stats.put(Constantes.STATS_ADD_SOIN, Integer.parseInt(statInfos[2]));
                this.stats.put(Constantes.STATS_CREATURE, Integer.parseInt(statInfos[3]));
            } catch (Exception e) {
                Mundo.mundo.logger.error("#1# Erreur lors du chargement du grade du monstre (template) : " + template.getId());
                e.printStackTrace();
            }

            if (!allSpells.equalsIgnoreCase("")) {
                String[] spells = allSpells.split(";");

                for (String str : spells) {
                    if (str.equals("")) continue;
                    String[] spellInfo = str.split("@");
                    int id, lvl;

                    try {
                        id = Integer.parseInt(spellInfo[0]);
                        lvl = Integer.parseInt(spellInfo[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                    if (id == 0 || lvl == 0) continue;
                    Hechizo spell = Mundo.mundo.getSort(id);
                    if (spell == null) continue;
                    Hechizo.SortStats spellStats = spell.getStatsByLevel(lvl);
                    if (spellStats == null) continue;
                    this.spells.put(id, spellStats);
                }
            }
        }

        private MobGrade(Monstruos template, int grade, int level, int pdv,
                         int pdvMax, int pa, int pm,
                         Map<Integer, Integer> stats,
                         ArrayList<Integer> statsInfos,
                         Map<Integer, Hechizo.SortStats> spells, int xp, int n) {
            this.size = 100 + n * pSize;
            this.template = template;
            this.grade = grade;
            this.level = level;
            this.pdv = pdv;
            this.pdvMax = pdvMax;
            this.pa = pa;
            this.pm = pm;
            this.stats = stats;
            this.statsInfos = statsInfos;
            this.spells = spells;
            this.inFightId = -1;
            this.baseXp = xp;
        }

        public MobGrade getCopy() {
            Map<Integer, Integer> newStats = new HashMap<>(this.stats);
            int n = (this.size - 100) / pSize;
            return new MobGrade(this.template, this.grade, this.level, this.pdv, this.pdvMax, this.pa, this.pm, newStats, this.statsInfos, this.spells, this.baseXp, n);
        }

        public void refresh() {
            if (this.spells.isEmpty())
                return;
            StringBuilder spells = new StringBuilder();

            for (Entry<Integer, Hechizo.SortStats> entry : this.spells.entrySet()) {
                spells.append((spells.length() == 0) ? entry.getKey() + ","
                        + entry.getValue().getLevel() : ";" + entry.getKey()
                        + "," + entry.getValue().getLevel());
            }

            this.spells.clear();

            if (!spells.toString().equalsIgnoreCase("")) {
                for (String split : spells.toString().split(";")) {
                    int id = Integer.parseInt(split.split(",")[0]);
                    this.spells.put(id, Mundo.mundo.getSort(id).getStatsByLevel(Integer.parseInt(split.split(",")[1])));
                }
            }
        }

        public int getSize() {
            return this.size;
        }

        public Monstruos getTemplate() {
            return this.template;
        }

        public int getGrade() {
            return this.grade;
        }

        public int getLevel() {
            return this.level;
        }

        public int getPdv() {
            return this.pdv;
        }

        public void setPdv(int pdv) {
            this.pdv = pdv;
        }

        public int getPdvMax() {
            return this.pdvMax;
        }

        public int getInFightID() {
            return this.inFightId;
        }

        public void setInFightID(int i) {
            this.inFightId = i;
        }

        public int getInit() {
            return this.init;
        }

        public int getPa() {
            return this.pa;
        }

        public int getPm() {
            return this.pm;
        }

        public int getBaseXp() {
            return this.baseXp;
        }

        public GameCase getFightCell() {
            return this.fightCell;
        }

        public void setFightCell(GameCase cell) {
            this.fightCell = cell;
        }

        public ArrayList<EfectoHechizo> getBuffs() {
            return this.fightBuffs;
        }

        public Caracteristicas getStats() {
            if(this.getTemplate().getId() == 42 && !stats.containsKey(Constantes.STATS_CREATURE))
                stats.put(Constantes.STATS_CREATURE, 5);
            if(this.stats.get(-1) != null) {
                Map<Integer, Integer> stats = new HashMap<>(this.stats);
                stats.remove(-1); stats.remove(-100);

                int random = Formulas.getRandomValue(210, 214);
                int one = this.stats.get(-1), all = this.stats.get(-100);

                stats.put(Constantes.STATS_ADD_RP_NEU, (random == Constantes.STATS_ADD_RP_NEU ? one : all));
                stats.put(Constantes.STATS_ADD_RP_TER, (random == Constantes.STATS_ADD_RP_TER ? one : all));
                stats.put(Constantes.STATS_ADD_RP_FEU, (random == Constantes.STATS_ADD_RP_FEU ? one : all));
                stats.put(Constantes.STATS_ADD_RP_EAU, (random == Constantes.STATS_ADD_RP_EAU ? one : all));
                stats.put(Constantes.STATS_ADD_RP_AIR, (random == Constantes.STATS_ADD_RP_AIR ? one : all));

                return new Caracteristicas(stats);
            }
            return new Caracteristicas(this.stats);
        }

        public Map<Integer, Hechizo.SortStats> getSpells() {
            return this.spells;
        }

        public void modifStatByInvocator(final Peleador caster, int mobID) {
            if(mobID==116) {
                if(caster.getPlayer()!=null) {
                    double casterVit=caster.getPlayer().getMaxPdv();
                    pdv=(int)((pdvMax)+(casterVit*0.02));
                    pdvMax=pdv;
                }
                double casterWis= Objects.requireNonNull(caster.getPlayer()).getTotalStats().getEffect(Constantes.STATS_ADD_SAGE);
                if(casterWis<0)
                    casterWis=0;
                double casterAgi=caster.getPlayer().getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
                if(casterAgi<0)
                    casterAgi=0;
                double agili=stats.get(Constantes.STATS_ADD_AGIL)+(casterAgi*0.3);
                double sages=stats.get(Constantes.STATS_ADD_SAGE)+(casterWis*0.2);
                stats.put(Constantes.STATS_ADD_AGIL,(int)agili);
                stats.put(Constantes.STATS_ADD_SAGE,(int)sages);
            }else{
                if(caster.getPlayer()!=null) {
                    double casterVit=caster.getPlayer().getMaxPdv();
                    double modifier=((casterVit*pdvMax*0.15)/225);
                    if(modifier>800)
                        modifier=800;
                    pdv=(int)(pdvMax+modifier);
                    pdvMax=pdv;
                }
                double casterWis= Objects.requireNonNull(caster.getPlayer()).getTotalStats().getEffect(Constantes.STATS_ADD_SAGE);
                if(casterWis<0)
                    casterWis=0;
                double casterStr=caster.getPlayer().getTotalStats().getEffect(Constantes.STATS_ADD_FORC);
                if(casterStr<0)
                    casterStr=0;
                double casterInt=caster.getPlayer().getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
                if(casterInt<0)
                    casterInt=0;
                double casterCha=caster.getPlayer().getTotalStats().getEffect(Constantes.STATS_ADD_CHAN);
                if(casterCha<0)
                    casterCha=0;
                double casterAgi=caster.getPlayer().getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
                if(casterAgi<0)
                    casterAgi=0;
                double casterSummons=caster.getPlayer().getTotalStats().getEffect(Constantes.STATS_ADD_SUM);
                if(casterSummons<0)
                    casterSummons=0;
                double sages=stats.get(Constantes.STATS_ADD_SAGE)+(casterWis*0.2);
                double force=stats.get(Constantes.STATS_ADD_FORC)+(casterStr*0.5);
                double intel=stats.get(Constantes.STATS_ADD_INTE)+(casterInt*0.5);
                double chance=stats.get(Constantes.STATS_ADD_CHAN)+(casterCha*0.5);
                double agili=stats.get(Constantes.STATS_ADD_AGIL)+(casterAgi*0.5);
                double summons=1+(casterSummons*0.5);
                stats.put(Constantes.STATS_ADD_SAGE,(int)sages);
                stats.put(Constantes.STATS_ADD_FORC,(int)force);
                stats.put(Constantes.STATS_ADD_INTE,(int)intel);
                stats.put(Constantes.STATS_ADD_CHAN,(int)chance);
                stats.put(Constantes.STATS_ADD_AGIL,(int)agili);
                stats.put(Constantes.STATS_ADD_SUM,(int)summons);
            }
        }
    }

    public static class Bandidos {
        private static Bandidos bandits;
        private final ArrayList<Monstruos> monsters = new ArrayList<>();
        private final ArrayList<Mapa> maps = new ArrayList<>();
        private long time;
        private boolean isPop = false;

        public Bandidos(String mobs, String maps, long time) {
            if (!mobs.equalsIgnoreCase("")) {
                for (String mob : mobs.split(",")) {
                    Integer _mob = null;
                    try {
                        _mob = Integer.parseInt(mob);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (_mob == null)
                        continue;

                    Monstruos monstre = Mundo.mundo.getMonstre(_mob);
                    if (monstre == null) {
                        continue;
                    }
                    this.monsters.add(monstre);
                }
            }

            if (!maps.equalsIgnoreCase("")) {
                for (String map : maps.split(",")) {
                    Short _map = null;
                    try {
                        _map = Short.parseShort(map);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (_map == null)
                        continue;

                    Mapa _Map = Mundo.mundo.getMap(_map);
                    if (_Map == null)
                        continue;

                    this.maps.add(_Map);
                }
            }

            this.time = time;
            bandits = this;

            run();
        }

        public static Bandidos getBandits() {
            return bandits;
        }

        public static void run() {
            Bandidos bandit = getBandits();
            if (bandit.isPop) {
                Temporizador.addSiguiente(Bandidos::run, 1000 * 60 * 60, Temporizador.DataType.MAPA);
            } else {
                long time = bandit.getTime();
                long actuel = Instant.now().toEpochMilli();
                if (time <= 0) {
                    pop(bandit, actuel);
                } else {
                    int random = Formulas.getRandomValue(6, 18);
                    long timeRandom = 1000 * 60 * 60 * random; // Temps en MS d'heures entre les repops des bandits
                    if (time + timeRandom <= actuel) {
                        pop(bandit, actuel);
                    } else {
                        Temporizador.addSiguiente(Bandidos::run, 1000 * 60 * 60, Temporizador.DataType.MAPA);
                    }
                }
            }
        }

        public static void pop(final Bandidos bandit, final long actuel) {
            try {
                bandit.setTime(actuel);
                int nbMap = bandit.getMaps().size();
                int random = Formulas.getRandomValue(0, nbMap - 1);
                Mapa map = bandit.getMaps().get(random);
                StringBuilder groupData=new StringBuilder();
                for (Monstruos monstre : bandit.getMonsters()) {
                    Integer id = monstre.getId();
                    Integer lvl = monstre.getRandomGrade().getLevel();
                    if (groupData.toString().equalsIgnoreCase(""))
                        groupData.append(id).append(",").append(lvl).append(",").append(lvl);
                    else
                        groupData.append(";").append(id).append(",").append(lvl).append(",").append(lvl);
                }
                map.nextObjectId++;
                map.spawnNewGroup(false, map.getRandomFreeCellId(), groupData.toString(), "");
                Database.estaticos.getGangsterData().update(bandit);
            } catch (Exception e) {
                e.printStackTrace();
                Temporizador.addSiguiente(() -> pop(bandit, actuel), 60000, Temporizador.DataType.MAPA);
            }
        }

        public ArrayList<Monstruos> getMonsters() {
            return this.monsters;
        }

        public ArrayList<Mapa> getMaps() {
            return this.maps;
        }

        public long getTime() {
            return this.time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public void setPop(boolean isPop) {
            this.isPop = isPop;
        }
    }

    public static class MaestroCuerbok {
        /*
         * Subarea : 211 Group : 289,120,200;825,90,98;823,90,98;824,80,88
         */
        private short oldMap;
        private short map;

        public MaestroCuerbok() {
            repop((short) -1);
        }

        public void repop(short id) {
            if (this.oldMap == id)
                return;

            this.oldMap = id;

            ArrayList<Mapa> maps = new ArrayList<>(Mundo.mundo.getSubArea(211).getMaps());
            maps.remove(Mundo.mundo.getMap((short) 9589));
            maps.remove(Mundo.mundo.getMap((short) 9604));

            int index = Formulas.random.nextInt(maps.size());
            Mapa map = maps.get(index);

            while (map.getId() == id) {
                index = Formulas.random.nextInt(maps.size());
                map = maps.get(index);
            }

            this.map = map.getId();
            map.spawnGroupOnCommand(map.getRandomFreeCellId(), "289,120,200;825,90,98;823,90,98;824,80,88", true);
        }

        public int check() {
            return switch (this.map) {
                case 9590, 9594, 9596, 9600 -> 3188;
                case 9592, 9597, 9593, 9598 -> 3193;
                case 9599, 9591, 9595, 9603 -> 3191;
                case 9601, 9723, 9602, 9724 -> 3194;
                default -> -1;
            };
        }
    }
}