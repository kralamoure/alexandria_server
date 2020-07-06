package org.alexandria.estaticos;

import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.estaticos.cliente.Jugador.Caracteristicas;
import org.alexandria.comunes.Camino;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.configuracion.Constantes;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.juego.planificador.Updatable;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.otro.utilidad.Temporizador;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Montura {

    public final static Updatable updatable = new Updatable(3600000) {
        @Override
        public void update() {
            if(this.verify()) {
                for (Montura mount : Mundo.mundo.getMounts().values()) {
                    if (mount.getFatigue() <= 0) continue;
                    mount.setFatigue(mount.getFatigue() - 10);
                    if (mount.getFatigue() < 0) mount.setFatigue(0);
                }
            }
        }

        @Override
        public Object get() {
            return null;
        }
    };

	private int id;
	private int color;
	private final int sex;
	private int size;
	private String name;
	private int level;
	private long exp;
	
	private int owner;
	private short mapId;
	private int cellId, orientation;
	
	private int fatigue, energy, reproduction;

	private int amour, endurance, maturity;
	private int state;
	private final int savage;
	
	private String ancestors = "?,?,?,?,?,?,?,?,?,?,?,?,?,?";
	
	private long fecundatedDate = -1;
    private int couple;
	
	private Caracteristicas stats = new Caracteristicas();
	private final java.util.Map<Integer, ObjetoJuego> objects = new HashMap<>();
	private final List<Integer> capacitys = new ArrayList<>(2);

	public Montura(int color, int owner, boolean savage) {
		this.id = Database.dinamicos.getMountData().getNextId();
		this.color = color;
		this.sex = Formulas.getRandomValue(0, 1);
		this.level = savage?0:100;
		this.exp = savage?0:Mundo.mundo.getExpLevel(this.level).mount;
		this.name = Configuracion.INSTANCE.getNAME();
		this.fatigue = 0;
		this.energy = savage?0:getMaxEnergy();
		this.reproduction = ((color == 75 || color == 88) ? -1 : 0);
		this.maturity = savage?0:getMaxMaturity();
		this.state = 0;
		this.stats = Constantes.getMountStats(this.color, this.level);
		this.ancestors = "?,?,?,?,?,?,?,?,?,?,?,?,?,?";
		this.size = 100;
		this.owner = owner;
		this.cellId = -1;
		this.mapId = -1;
		this.orientation = 1;
		this.savage = (savage ? 1 : 0);
		
		Mundo.mundo.addMount(this);
        Database.dinamicos.getMountData().add(this);
	}

	public Montura(int color, Montura mother, Montura father) {
		this.id = Database.dinamicos.getMountData().getNextId();
		this.color = color;
		this.sex = Formulas.getRandomValue(0, 1);
		this.level = 1;
		this.exp = 0;
		this.name = Configuracion.INSTANCE.getNAME();
		this.fatigue = 0;
		this.energy = getMaxEnergy();
		this.reproduction = 0;
		this.maturity = 0;
		this.state = Formulas.getRandomValue(-10000, 10000);
		this.stats = Constantes.getMountStats(this.color, this.level);

		String[] fatherStr = father.ancestors.split(","), motherStr = mother.ancestors.split(",");
		String firstFather = fatherStr[0] + "," + fatherStr[1],
				firstMother = motherStr[0] + "," + motherStr[1],
				secondFather = fatherStr[2] + "," + fatherStr[3] + "," + fatherStr[4] + "," + fatherStr[5],
				secondMother = motherStr[2] + "," + motherStr[3] + "," + motherStr[4] + "," + motherStr[5];
		
		this.ancestors = father.getColor() + "," + mother.getColor() + "," + firstFather + "," + firstMother + "," + secondFather + "," + secondMother;
		
		if(Formulas.getRandomValue(0, 20) == 0)
			this.capacitys.add(Formulas.getRandomValue(1, 8));
		
		if(!father.getCapacitys().isEmpty() || !mother.getCapacitys().isEmpty()) {
			if(Formulas.getRandomValue(0, 10) == 0) {
				if(Formulas.getRandomValue(0, 1) == 0)
					if(!father.getCapacitys().isEmpty())
						this.capacitys.add(father.getCapacitys().get(Formulas.getRandomValue(0, father.getCapacitys().size() - 1)));
				else
					if(!mother.getCapacitys().isEmpty())
						this.capacitys.add(mother.getCapacitys().get(Formulas.getRandomValue(0, mother.getCapacitys().size() - 1)));
			}
		}

		this.cellId = -1;
		this.mapId = -1;
		this.owner = mother.getOwner();
		this.size = 50;
		this.orientation = 1;
		this.fecundatedDate = -1;
		this.couple = -1;
		this.savage = 0;
		Mundo.mundo.addMount(this);
        Database.dinamicos.getMountData().add(this);
	}
	
	public Montura(int id, int color, int sexe, int amour, int endurance, int level, long exp, String name, int fatigue,
				   int energy, int reproduction, int maturity, int state, String objects, String ancestors, String capacitys, int size,
				   int cellId, short mapId, int owner, int orientation, long fecundatedHour, int couple, int savage) {
		this.id = id;
		this.color = color;
		this.sex = sexe;
		this.amour = amour;
		this.endurance = endurance;
		this.level = level;
		this.exp = exp;
		this.name = name;
		this.fatigue = fatigue;
		this.energy = energy;
		this.reproduction = reproduction;
		this.maturity = maturity;
		this.state = state;
		this.ancestors = ancestors;
		this.stats = Constantes.getMountStats(this.color, this.level);
		this.size = size;
		this.cellId = cellId;
		this.mapId = mapId;
		this.owner = owner;
		this.orientation = orientation;
		this.fecundatedDate = fecundatedHour;
		this.couple = couple;
		this.savage = savage;

		for(String str : objects.split(";")) {
			if(str.isEmpty()) continue;
			try {
				ObjetoJuego gameObject = Mundo.getGameObject(Integer.parseInt(str));
				if(gameObject != null)
					this.objects.put(gameObject.getId(), gameObject);
			} catch (Exception e) {
			 	e.printStackTrace();
			}
		}

        for(String str : capacitys.split(",", 2))
            if(str != null)
                try { this.capacitys.add(Integer.parseInt(str)); }catch (Exception e) { e.printStackTrace(); }
    }

	private static int checkCanKen(Cercados park, Montura mount, int cellTest, int action) {
		if(park.getListOfRaising().size() > 1) {
			for(Integer arg : park.getListOfRaising()) {
				Montura mountArg = Mundo.mundo.getMountById(arg);
				if(mountArg == null) continue;
                if(mountArg.getSex() !=	mount.getSex() && mountArg.isFecund() != 0 && mount.isFecund() != 0 && mountArg.getCellId() == cellTest) {
					if(mountArg.getReproduction() < 20 && mount.getReproduction() < 20 && !mountArg.isCastrated() && !mount.isCastrated()) {
						if(mountArg.getSex() == 1) {
							mountArg.fecundatedDate = Instant.now().toEpochMilli();
							mountArg.setCouple(mount.id);
							mountArg.resAmor(7500);
							mountArg.resEndurance(7500);
							mount.resAmor(7500);
							mount.resEndurance(7500);
							mount.aumReproduction();
							if (mount.getSavage() == 1) {
								park.getListOfRaising().remove(mount.id);
								park.getMap().send("GM|-" + mount.getId());
								Jugador player = Mundo.mundo.getPlayer(mount.getOwner());
								if (player != null && player.isOnline()) {
									player.send("Im0111;~" + park.getMap().getX() + "," + park.getMap().getY());
									GestorSalida.GAME_SEND_Ee_PACKET(player, '-', String.valueOf(mount.getId()));
								}
								mount.setMapId((short) -1);
								mount.setCellId(-1);
								mount.setOwner(-1);
							}
						} else if(mount.getSex() == 1) {
							mount.fecundatedDate = Instant.now().toEpochMilli();
							mount.setCouple(mountArg.getId());
							mount.resAmor(7500);
							mount.resEndurance(7500);
							mountArg.resAmor(7500);
							mountArg.resEndurance(7500);
							mountArg.aumReproduction();
							if (mountArg.getSavage() == 1) {
								park.getListOfRaising().remove(mountArg.id);
								park.getMap().send("GM|-" + mountArg.getId());
								Jugador player = Mundo.mundo.getPlayer(mountArg.getOwner());
								if (player != null && player.isOnline()) {
									player.send("Im0111;~" + park.getMap().getX() + "," + park.getMap().getY());
									GestorSalida.GAME_SEND_Ee_PACKET(player, '-', String.valueOf(mountArg.getId()));
								}
								mountArg.setMapId((short) -1);
								mountArg.setCellId(-1);
								mountArg.setOwner(-1);
							}
						}
						return 4;
					}
				}
			}
		}
		return action;
	}

	public synchronized void checkBaby(Jugador player) {
        if(this.fecundatedDate == -1) return;
		int time = Generacion.getTimeGestation(Constantes.getGeneration(this.getColor()));
        int actualHours = (int) ((Instant.now().toEpochMilli() - this.getFecundatedDate()) / 3600000) + 1;

        if(time < actualHours && actualHours < time + 24 * 7) {
            boolean coupleReprod = Mundo.mundo.getMountById(this.getCouple()) != null && Mundo.mundo.getMountById(this.getCouple()).getCapacitys().contains(3);
            boolean reproductrice = this.getCapacitys().contains(3) || coupleReprod;
			int offspring = 1 + (reproductrice ? 1 : 0), value = Formulas.getRandomValue(0, 16), max = 3 + (reproductrice ? 1 : 0);

			if(value >= 5 && value <= 10)
				offspring = (reproductrice ? 3 : 2);
			else if(value < 1)
				offspring = max;
			if(this.getCapacitys().contains(3) && coupleReprod)
				offspring *= 2;

			GestorSalida.GAME_SEND_Im_PACKET(player, "1111;" + offspring);
			Montura father = Mundo.mundo.getMountById(this.getCouple());
			for(int i = 0; i < offspring; i++) {
                int color = Constantes.colorToEtable(player, this, (father == null ? this : father));
				Montura baby = new Montura(color, this, (father == null ? this : father));
                player.getCurMap().getMountPark().getEtable().add(baby);
			}

            this.aumReproduction();
            this.setFecundatedDate(-1);

            if (player.getCurMap().getMountPark().hasEtableFull(player.getId()))
				player.send("Im1105");
			if(father != null && father.getSavage() == 1) {
                Database.dinamicos.getMountData().delete(father.getId());
				Mundo.mundo.removeMount(father.getId());
			}
			if(this.getSavage() == 1) {
                Database.dinamicos.getMountData().delete(this.getId());
				Mundo.mundo.removeMount(this.getId());
				((Cercados) player.getExchangeAction().getValue()).delRaising(this.getId());
               	player.send("Im0112; " + this.getName());
			}
		} else if(actualHours >= time + 24 * 7) {
			GestorSalida.GAME_SEND_Im_PACKET(player, "1112");
			this.aumReproduction();
			this.resAmor(7500);
			this.resEndurance(7500);
			this.setFecundatedDate(-1);
		}
	}

	//region getter/setter
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getSex() {
		return sex;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public long getExp() {
		return exp;
	}

	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
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

	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

    public int getFatigue() {
        return fatigue;
    }

	private synchronized void setFatigue(int fatigue) {
        this.fatigue = fatigue;
    }

    public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}

	public int getReproduction() {
		return reproduction;
	}

	public int getAmour() {
		return amour;
	}

	public int getEndurance() {
		return endurance;
	}

	public int getMaturity() {
		return maturity;
	}

	public int getState() {
		if(this.state > 10000) this.state = 10000;
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getSavage() {
		return savage;
	}

	public String getAncestors() {
		return ancestors;
	}

	public long getFecundatedDate() {
		return fecundatedDate;
	}

	private void setFecundatedDate(int fecundatedDate) {
		if(this.reproduction != -1)
			this.fecundatedDate = fecundatedDate;
	}

	public int getCouple() {
		return couple;
	}

	public void setCouple(int couple) {
		this.couple = couple;
	}

	public Caracteristicas getStats() {
		return stats;
	}
	//endregion getter/setter
	
	public java.util.Map<Integer, ObjetoJuego> getObjects() {
		return objects;
	}
	
	public List<Integer> getCapacitys() {
		return capacitys;
	}

	public String getStringColor(String color) {
		String secondColor = "";
		if(this.capacitys.contains(9))
			secondColor = "," + color;
		if(this.color == 75)
			secondColor = "," + Constantes.getStringColorDragodinde(Formulas.getRandomValue(1, 87));
		return this.color + secondColor;
	}
	
	public int isMontable() {
        int mountable = ((this.maturity < this.getMaxMaturity() || this.fatigue == 240 || this.savage == 1) ? 0 : 1);
        if(mountable == 1 && this.size == 50)
            this.size = 100;
		return mountable;
	}

	private int isFecund() {
		if(this.reproduction != -1 && this.amour >= 7500 && this.endurance >= 7500 && this.maturity == this.getMaxMaturity() && (this.savage == 1 || (this.savage == 0 && this.level >= 5)))
			return 10;
		return 0;
	}
	
	public void setCastrated() {
		this.reproduction = -1;
	}

	private boolean isCastrated() {
		return this.reproduction == -1;
	}
	
	public int getActualPods() {
		int pods = 0;
		for(ObjetoJuego gameObject : this.objects.values())
			pods += gameObject.getModelo().getPod() * gameObject.getCantidad();
		return pods;
	}
	
	public int getMaxPods() {
		return Generacion.getPods(Constantes.getGeneration(this.color), this.level);
	}
	
	public void addXp(long amount) {
		this.exp += amount;
		while(this.exp >= Mundo.mundo.getExpLevel(this.level+1).mount && this.level < 100)
			this.addLvl();
        Database.dinamicos.getMountData().update(this);
	}

	private void addLvl() {
		this.level++;
		this.stats = Constantes.getMountStats(this.color, this.level);
	}

	private void stateMale() {
		this.state -= 2;
		if(this.state < -10000) this.state = -10000;
	}

	private void stateFemale() {
		this.state += 2;
		if(this.state < -10000)	this.state = -10000;
	}

	private void setMaxEnergy() {
		this.energy = this.getMaxEnergy();
        Database.dinamicos.getMountData().update(this);
	}
	
	private int getMaxEnergy() {
		return Generacion.getEnergy(Constantes.getGeneration(this.color));
	}

	private void setMaxMaturity() {
		this.maturity = this.getMaxMaturity();
	}

	private int getMaxMaturity() {
		return Generacion.getMaturity(Constantes.getGeneration(this.color));
	}

	private void aumFatige() {
		this.fatigue += 1;
		if(this.fatigue > 240) this.fatigue = 240;
        Database.dinamicos.getMountData().update(this);
	}
	
	private void aumEndurance(int endurance) {
		this.endurance += (endurance / 100) * this.getBonusFatigue() * Generacion.getLearningRate(Constantes.getGeneration(this.color));
		if(this.capacitys.contains(5)) this.endurance += 1;
		if(this.endurance > 10000) this.endurance = 10000;
        Database.dinamicos.getMountData().update(this);
	}
	
	private void aumMaturity(int Resist) {
		if(this.maturity < this.getMaxMaturity()) {
			this.maturity += (Resist / 100) * this.getBonusFatigue();
			if(this.capacitys.contains(7))
				this.maturity += Resist / 100;
			if(this.size < 100) {
                Mapa map = Mundo.mundo.getMap(this.mapId);
				if((this.getMaxMaturity() / this.maturity) <= 1) {
					this.size = 100;
					GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(map, this.id);
					GestorSalida.GAME_SEND_GM_MOUNT_TO_MAP(map, this);
					return;
				} else
				if(this.size < 75 && (this.getMaxMaturity() / this.maturity) == 2) {
					this.size = 75;
					GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(map, this.id);
					GestorSalida.GAME_SEND_GM_MOUNT_TO_MAP(map, this);
					return;
				} else
				if(this.size < 50 && (this.getMaxMaturity() / this.maturity) == 3) {
					this.size = 50;
					GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(map, this.id);
					GestorSalida.GAME_SEND_GM_MOUNT_TO_MAP(map, this);
					return;
				}
			}
		}
		if(this.maturity > this.getMaxMaturity()) this.setMaxMaturity();
	}
	
	private void aumAmor(int amour) {
		this.amour += (amour / 100) * this.getBonusFatigue();
		if(this.capacitys.contains(6)) this.amour += amour / 500;
		if(this.amour > 10000) this.amour = 10000;
	}
	
	private void aumState(int state) {
		this.state += (state / 100) * this.getBonusFatigue();
		if(this.state > 10000) this.state = 10000;
	}

	public void aumEnergy(int energy) {
		this.energy += (energy / 500) * this.getBonusFatigue();
		if(this.capacitys.contains(1)) this.energy += energy / 500;
		if(this.energy > this.getMaxEnergy()) this.setMaxEnergy();
	}
	
	private void aumReproduction() {
		if(this.reproduction != -1)	this.reproduction += 1;
	}
	
	private void resFatige() {
		this.fatigue -= 20;
		if(this.fatigue < 0) this.fatigue = 0;
	}
	
	private void resAmor(int amor) {
		this.amour -= amor * this.getBonusFatigue();
		if(this.amour < 0) this.amour = 0;
	}
	
	private void resEndurance(int endurance) {
		this.endurance -= endurance * this.getBonusFatigue();
		if(this.endurance < 0) this.endurance = 0;
	}

	private void resState(int state) {
		this.state -= (state / 100) * this.getBonusFatigue();
		if(this.state < -10000)	this.state = -10000;
	}

    public void setToMax() {
        this.amour = 10000;
        this.endurance = 10000;
		this.energy = this.getMaxEnergy();
        this.setMaxMaturity();
    }

	private double getBonusFatigue() {
		if(this.fatigue > 160 && this.fatigue <= 170)
			return 1.15;
		if(this.fatigue > 170 && this.fatigue <= 180)
			return 1.30;
		if(this.fatigue > 180 && this.fatigue <= 200)
			return 1.50;
		if(this.fatigue > 200 && this.fatigue <= 210)
			return 1.80;
		if(this.fatigue > 210 && this.fatigue <= 220)
			return 2.10;
		if(this.fatigue > 220 && this.fatigue <= 230)
			return 2.50;
		if(this.fatigue > 230 && this.fatigue <= 239)
			return 3.00;
		return fatigue == 240 ? 0 : 1;
	}
	
	public synchronized void moveMounts(Jugador player, int cellules, boolean remove) {
		int action = 0;
		if(player == null)
			return;
		if(player.getCurCell().getId() == this.cellId)
			return;
		String path = "";
        Mapa map = player.getCurMap();
		if(map.getMountPark() == null)
			return;
		Cercados MP = map.getMountPark();
		char dir;
		int azar = Formulas.getRandomValue(1, 10);
		dir = Camino.getDirEntreDosCeldas(map, this.cellId, player.getCurCell().getId());
		if(remove)
			dir = Camino.getOpositeDirection(dir);
		int cell = this.cellId;
		int cellTest = this.cellId;
		for(int i = 0; i < cellules; i++) 
		{
			cellTest = Camino.GetCaseIDFromDirrection(cellTest, dir, map.getMountPark().getMap(), false);
			if(map.getCase(cellTest) == null)
				return;
            if(MP.getCellAndObject().containsKey(cellTest) && (this.fatigue >= 240 || this.isFecund() == 10))
                break;
			if(MP.getCellAndObject().containsKey(cellTest))
			{
				int item = MP.getCellAndObject().get(cellTest);
				// liste objet elevage
				if(item == 7755 || item == 7756 || item == 7757 || item == 7758 || item == 7759 || item == 7760 || item == 7761 || item == 7762 || item == 7763 || item == 7764 || item == 7765 || item == 7766 || item == 7767 || item == 7768 || item == 7769 || item == 7770 || item == 7771 || item == 7772 || item == 7773 || item == 7774 || item == 7625 || item == 7626 || item == 7627 || item == 7629)
				{// Baffeur
					resState(Mapa.getObjResist(player, cellTest, item));
					if(this.sex == 0)
					{
						this.stateMale();
					}else 
					{
						this.stateFemale();
					}
					aumFatige();
				}else 	
				if(item == 7775 || item == 7776 || item == 7777 || item == 7778 || item == 7779 || item == 7780 || item == 7781 || item == 7782 || item == 7783 || item == 7784 || item == 7785 || item == 7786 || item == 7787 || item == 7788 || item == 7789 || item == 7790 || item == 7791 || item == 7792 || item == 7793 || item == 7794 || item == 7795 || item == 7796 || item == 7797 || item == 7798)
				{//Foudroyeur
					if(this.state < 0)
						this.aumEndurance(Mapa.getObjResist(player, cellTest, item));
					if(this.sex == 0)
					{
						this.stateMale();
					}else 
					{
						this.stateFemale();
					}
					aumFatige();
				}else
				if(item == 7606 || item == 7607 || item == 7608 || item == 7609 || item == 7610 || item == 7611 || item == 7612 || item == 7613 || item == 7614 || item == 7615 || item == 7616 || item == 7617 || item == 7618 || item == 7619 || item == 7620 || item == 7621 || item == 7683 || item == 7684 || item == 7685 || item == 7686 || item == 7687 || item == 7688 || item == 7689 || item == 7690) 
				{// Mangeoire
					resFatige();
					this.aumEnergy(Mapa.getObjResist(player, cellTest, item));
					if(this.sex == 0)
					{
						this.stateMale();
					}else 
					{
						this.stateFemale();
					}
				}else 
				if(item == 7634 || item == 7635 || item == 7636 || item == 7637 || item == 7691 || item == 7692 || item == 7693 || item == 7694 || item == 7695 || item == 7696 || item == 7697 || item == 7698 || item == 7699 || item == 7700) 
				{// Dragofesse
					if(this.state > 0)
						this.aumAmor(Mapa.getObjResist(player, cellTest, item));
					if(this.sex == 0)
					{
						this.stateMale();
					}else 
					{
						this.stateFemale();
					}
					aumFatige();
				}else 
				if(item == 7628 || item == 7622 || item == 7623 || item == 7624 || item == 7733 || item == 7734 || item == 7735 || item == 7736 || item == 7737 || item == 7738 || item == 7739 || item == 7740 || item == 7741 || item == 7742 || item == 7743 || item == 7744 || item == 7745 || item == 7746)
				{// Caresseur
					aumState(Mapa.getObjResist(player, cellTest, item));
					if(this.sex == 0)	
					{
						this.stateMale();
					}else 
					{
						this.stateFemale();
					}
					aumFatige();
				}else 
				if(item == 7590 || item == 7591 || item == 7592 || item == 7593 || item == 7594 || item == 7595 || item == 7596 || item == 7597 || item == 7598 || item == 7599 || item == 7600 || item == 7601 || item == 7602 || item == 7603 || item == 7604 || item == 7605 || item == 7673 || item == 7674 || item == 7675 || item == 7676 || item == 7677 || item == 7678 || item == 7679 || item == 7682)
				{// Abreuvoir
					if(this.state <= 2000 && this.state >= -2000)
						this.aumMaturity(Mapa.getObjResist(player, cellTest, item));
					if(this.sex == 0)	
					{
						this.stateMale();
					}else 
					{
						this.stateFemale();
					}
					aumFatige();
				}
				if(item != 7590 && item != 7591 && item != 7592 && item != 7593 && item != 7594 && item != 7595 && item != 7596 && item != 7597
				&& item != 7598 && item != 7599 && item != 7600 && item != 7601 && item != 7602 && item != 7603 && item != 7604 && item != 7605
				&& item != 7673 && item != 7674 && item != 7675 && item != 7676 && item != 7677 && item != 7678 && item != 7679 && item != 7682
				&& item != 7606 && item != 7607 && item != 7608 && item != 7609 && item != 7610 && item != 7611 && item != 7612 && item != 7613
				&& item != 7614 && item != 7615 && item != 7616 && item != 7617 && item != 7618 && item != 7619 && item != 7620 && item != 7621
				&& item != 7683 && item != 7684 && item != 7685 && item != 7686 && item != 7687 && item != 7688 && item != 7689 && item != 7690
				&& item != 7755 && item != 7756 && item != 7757 && item != 7758 && item != 7759 && item != 7760 && item != 7761 && item != 7762
				&& item != 7763 && item != 7764 && item != 7765 && item != 7766 && item != 7767 && item != 7768 && item != 7769 && item != 7770
				&& item != 7771 && item != 7772 && item != 7773 && item != 7774 && item != 7625 && item != 7626 && item != 7627 && item != 7629
				&& item != 7628 && item != 7622 && item != 7623 && item != 7624 && item != 7733 && item != 7734 && item != 7735 && item != 7736
				&& item != 7737 && item != 7738 && item != 7739 && item != 7740 && item != 7741 && item != 7742 && item != 7743 && item != 7744
				&& item != 7745 && item != 7746 && item != 7634 && item != 7635 && item != 7636 && item != 7637 && item != 7691 && item != 7692
				&& item != 7693 && item != 7694 && item != 7695 && item != 7696 && item != 7697 && item != 7698 && item != 7699 && item != 7700
				&& item != 7775 && item != 7776 && item != 7777 && item != 7778 && item != 7779 && item != 7780 && item != 7781 && item != 7782
				&& item != 7783 && item != 7784 && item != 7785 && item != 7786 && item != 7787 && item != 7788 && item != 7789 && item != 7790
				&& item != 7791 && item != 7792 && item != 7793 && item != 7794 && item != 7795 && item != 7796 && item != 7797 && item != 7798)
				{

				}
				break;
			}
			if(map.getCase(cellTest).isWalkable(true) && MP.getDoor() != cellTest && !map.cellSide(cell, cellTest))
			{
				cell = cellTest;
				path += dir + Mundo.mundo.getCryptManager().idceldaCodigo(cell);
			}else
			{
				break;
			}
		}
		if(cell == this.cellId) 
		{
			this.orientation = Mundo.mundo.getCryptManager().getIntByHashedValue(dir);
			GestorSalida.GAME_SEND_eD_PACKET_TO_MAP(map, this.id, this.orientation);
			GestorSalida.SEND_GDE_FRAME_OBJECT_EXTERNAL(map, cellTest + ";4");
			GestorSalida.GAME_SEND_eUK_PACKET_TO_MAP(map, this.id, action);
			return;
		}
		if(azar == 5)
			action = 8;
		int nb = Montura.checkCanKen(MP, this, cellTest, action);
        if(nb == 4) action = 4;
		GestorSalida.GAME_SEND_GA_PACKET_TO_MAP(map, "" + 0, 1, this.id + "", "a" + Mundo.mundo.getCryptManager().idceldaCodigo(this.cellId) + path);
		this.cellId = cell;
		this.orientation = Mundo.mundo.getCryptManager().getIntByHashedValue(dir);
		int ID = this.id;

        final int finalCell = cellTest, finalAction = action;

		Temporizador.addSiguiente(() -> {
			GestorSalida.SEND_GDE_FRAME_OBJECT_EXTERNAL(map, finalCell + ";4");
			if(finalAction != 0)
				GestorSalida.GAME_SEND_eUK_PACKET_TO_MAP(map, ID, finalAction);
		}, action == 4 ? 2500 : 1500, Temporizador.DataType.MAPA);
	}
	
	public synchronized void moveMountsAuto(char direction, int cellules, boolean remove) 
	{
		int action = 0;
		StringBuilder path = new StringBuilder();
        Mapa map = Mundo.mundo.getMap(this.mapId);
		if(map == null)
			return;
		if(map.getMountPark() == null)
			return;
		Cercados MP = map.getMountPark();
		char dir = direction;
		int random = Formulas.getRandomValue(1, 10);
		int cell = this.cellId;
		int cellTest = this.cellId;
		for(int i = 0; i < cellules; i++) 
		{
			cellTest = Camino.getCellArroundByDir(cellTest, dir, Mundo.mundo.getMap(this.mapId));
			if(map.getCase(cellTest) == null)
				return;
			if(MP.getCellAndObject().containsKey(cellTest) && (this.fatigue >= 240 || this.isFecund() == 10))
				break;
			if(MP.getCellAndObject().containsKey(cellTest) && this.fatigue < 240) 
			{
				int item = MP.getCellAndObject().get(cellTest);
				if(item == 7755 || item == 7756 || item == 7757 || item == 7758 || item == 7759 || item == 7760 || item == 7761 || item == 7762 || item == 7763 || item == 7764 || item == 7765 || item == 7766 || item == 7767 || item == 7768 || item == 7769 || item == 7770 || item == 7771 || item == 7772 || item == 7773 || item == 7774 || item == 7625 || item == 7626 || item == 7627 || item == 7629)// 
				{	
					resState(Mapa.getObjResist(MP, cellTest, item));
					if(this.sex == 0)	
					{
						this.stateMale();
					}else 
					{
						this.stateFemale();
					}
					aumFatige();
				}else
				if(item == 7775 || item == 7776 || item == 7777 || item == 7778 || item == 7779 || item == 7780 || item == 7781 || item == 7782 || item == 7783 || item == 7784 || item == 7785 || item == 7786 || item == 7787 || item == 7788 || item == 7789 || item == 7790 || item == 7791 || item == 7792 || item == 7793 || item == 7794 || item == 7795 || item == 7796 || item == 7797 || item == 7798)
				{// Baffeur
					if(this.state < 0)
						this.aumEndurance(Mapa.getObjResist(MP, cellTest, item));
					if(this.sex == 0)
					{
						this.stateMale();
					}else 
					{
						this.stateFemale();
					}
					aumFatige();
				}else
				if(item == 7606 || item == 7607 || item == 7608 || item == 7609 || item == 7610 || item == 7611 || item == 7612 || item == 7613 || item == 7614 || item == 7615 || item == 7616 || item == 7617 || item == 7618 || item == 7619 || item == 7620 || item == 7621 || item == 7683 || item == 7684 || item == 7685 || item == 7686 || item == 7687 || item == 7688 || item == 7689 || item == 7690)
				{// Foudroyeur
					aumFatige();
					this.aumEnergy(Mapa.getObjResist(MP, cellTest, item));
					if(this.sex == 0)	
					{
						this.stateMale();
					}else
					{
						this.stateFemale();
					}
				}else 
				if(item == 7634 || item == 7635 || item == 7636 || item == 7637 || item == 7691 || item == 7692 || item == 7693 || item == 7694 || item == 7695 || item == 7696 || item == 7697 || item == 7698 || item == 7699 || item == 7700)
				{// Mangeoire
					if(this.state > 0)
						this.aumAmor(Mapa.getObjResist(MP, cellTest, item));
					if(this.sex == 0)	
					{
						this.stateMale();
					}else 
					{
						this.stateFemale();
					}
					aumFatige();
				}else 
				if(item == 7628 || item == 7622 || item == 7623 || item == 7624 || item == 7733 || item == 7734 || item == 7735 || item == 7736 || item == 7737 || item == 7738 || item == 7739 || item == 7740 || item == 7741 || item == 7742 || item == 7743 || item == 7744 || item == 7745 || item == 7746)
				{// Dragofesse
					aumState(Mapa.getObjResist(MP, cellTest, item));
					if(this.sex == 0)	
					{
						this.stateMale();
					}else 
					{
						this.stateFemale();
					}
					aumFatige();
				}else 
				if(item == 7590 || item == 7591 || item == 7592 || item == 7593 || item == 7594 || item == 7595 || item == 7596 || item == 7597 || item == 7598 || item == 7599 || item == 7600 || item == 7601 || item == 7602 || item == 7603 || item == 7604 || item == 7605 || item == 7673 || item == 7674 || item == 7675 || item == 7676 || item == 7677 || item == 7678 || item == 7679 || item == 7682)		
				{// Abreuvoir
					if(this.state <= 2000 && this.state >= -2000)
						this.aumMaturity(Mapa.getObjResist(MP, cellTest, item));
					if(this.sex == 0)	
					{
						this.stateMale();
					}else 
					{
						this.stateFemale();
					}
					aumFatige();
				}
				break;
			}
			if(map.getCase(cellTest).isWalkable(false) && MP.getDoor() != cellTest && !map.cellSide(cell, cellTest)) 
			{
				cell = cellTest;
				path.append(dir).append(Mundo.mundo.getCryptManager().idceldaCodigo(cell));
			}else
			{
				break;
			}
		}
		if(cell == this.cellId) 
		{
			this.orientation = Mundo.mundo.getCryptManager().getIntByHashedValue(dir);
			GestorSalida.GAME_SEND_eD_PACKET_TO_MAP(map, this.id, this.orientation);
			GestorSalida.SEND_GDE_FRAME_OBJECT_EXTERNAL(map, cellTest + ";4");
			GestorSalida.GAME_SEND_eUK_PACKET_TO_MAP(map, this.id, action);
			return;
		}
		if(random == 5)
			action = 8;
		int id = this.id;
		action = Montura.checkCanKen(MP, this, cellTest, action);
		GestorSalida.GAME_SEND_GA_ACTION_TO_MAP(map, "" + 0, 1, this.id + "", "a" + Mundo.mundo.getCryptManager().idceldaCodigo(this.cellId) + path.toString());
		this.cellId = cell;
		this.orientation = Mundo.mundo.getCryptManager().getIntByHashedValue(dir);

        final int finalCell = cellTest, finalAction = action;

        if(map.getPlayers().size() > 0) {
			Jugador player = null;
			for(Jugador target : map.getPlayers())
				player = target;
			if (player != null)
				Temporizador.addSiguiente(() -> {
					GestorSalida.SEND_GDE_FRAME_OBJECT_EXTERNAL(map, finalCell + ";4");
					if (finalAction != 0) GestorSalida.GAME_SEND_eUK_PACKET_TO_MAP(map, id, finalAction);
				}, 2000, Temporizador.DataType.MAPA);
        }
    }
	
	public void addObject(int guid, int qua, Jugador P) {
		if(qua <= 0)
			return;
		ObjetoJuego playerObj = Mundo.getGameObject(guid);
		if(playerObj == null) return;
		//Si le joueur n'a pas l'item dans son sac ...
		if(P.getItems().get(guid) == null)
		{
			return;
		}
		
		String str = "";
		
		//Si c'est un item equipe ...
		if(playerObj.getPosicion() != Constantes.ITEM_POS_NO_EQUIPED)return;
		
		ObjetoJuego TrunkObj = this.getSimilarObject(playerObj);
		int newQua = playerObj.getCantidad() - qua;
		if(TrunkObj == null)//S'il n'y pas d'item du meme Template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0)
			{
				//On enleve l'objet du sac du joueur
				P.removeItem(playerObj.getId());
				//On met l'objet du sac dans le coffre, avec la meme quantite
				this.objects.put(playerObj.getId() ,playerObj);
				str = "O+"+playerObj.getId()+"|"+playerObj.getCantidad()+"|"+playerObj.getModelo().getId()+"|"+playerObj.parseStatsString();
				GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(P, guid);
			}else//S'il reste des objets au joueur
			{
				//on modifie la quantite d'item du sac
				playerObj.setCantidad(newQua);
				//On ajoute l'objet au coffre et au monde
				TrunkObj = ObjetoJuego.getCloneObjet(playerObj, qua);
				Mundo.addGameObject(TrunkObj, true);
				this.objects.put(TrunkObj.getId() ,TrunkObj);
				
				//Envoie des packets
				str = "O+"+TrunkObj.getId()+"|"+TrunkObj.getCantidad()+"|"+TrunkObj.getModelo().getId()+"|"+TrunkObj.parseStatsString();
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P, playerObj);
			}
		}else // S'il y avait un item du meme template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0)
			{
				//On enleve l'objet du sac du joueur
				P.removeItem(playerObj.getId());
				//On enleve l'objet du monde
				Mundo.mundo.removeGameObject(playerObj.getId());
				//On ajoute la quantite a l'objet dans le coffre
				TrunkObj.setCantidad(TrunkObj.getCantidad() + playerObj.getCantidad());
				//On envoie l'ajout au coffre de l'objet
			    str = "O+"+TrunkObj.getId()+"|"+TrunkObj.getCantidad()+"|"+TrunkObj.getModelo().getId()+"|"+TrunkObj.parseStatsString();
				//On envoie la supression de l'objet du sac au joueur
				GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(P, guid);
			}else //S'il restait des objets
			{
				//On modifie la quantite d'item du sac
				playerObj.setCantidad(newQua);
				TrunkObj.setCantidad(TrunkObj.getCantidad() + qua);
				str = "O+"+TrunkObj.getId()+"|"+TrunkObj.getCantidad()+"|"+TrunkObj.getModelo().getId()+"|"+TrunkObj.parseStatsString();
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P, playerObj);
			}
		}

		GestorSalida.GAME_SEND_Ew_PACKET(P, this.getActualPods(), this.getMaxPods());
		GestorSalida.GAME_SEND_EL_MOUNT_PACKET(P, this);
	}
	
	public void removeObject(int guid, int qua, Jugador P) {
		if(qua <= 0)
			return;
		ObjetoJuego TrunkObj = Mundo.getGameObject(guid);
		//Si le joueur n'a pas l'item dans son coffre
		if(this.objects.get(guid) == null)
		{
			return;
		}
		
		ObjetoJuego playerObj = P.getSimilarItem(TrunkObj);
		String str = "";
		int newQua = TrunkObj.getCantidad() - qua;
		
		if(playerObj == null)//Si le joueur n'avait aucun item similaire
		{
			//S'il ne reste rien dans le coffre
			if(newQua <= 0)
			{
				//On retire l'item du coffre
				this.objects.remove(guid);
				//On l'ajoute au joueur
				P.getItems().put(guid, TrunkObj);
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OAKO_PACKET(P,TrunkObj);
				GestorSalida.GAME_SEND_Ew_PACKET(P, this.getActualPods(), this.getMaxPods());
				str = "O-"+guid;
			}else //S'il reste des objets dans le coffre
			{
				//On cree une copy de l'item dans le coffre
				playerObj = ObjetoJuego.getCloneObjet(TrunkObj, qua);
				//On l'ajoute au monde
				Mundo.addGameObject(playerObj, true);
				//On retire X objet du coffre
				TrunkObj.setCantidad(newQua);
				//On l'ajoute au joueur
				P.getItems().put(playerObj.getId(), playerObj);
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OAKO_PACKET(P,playerObj);
				GestorSalida.GAME_SEND_Ew_PACKET(P, this.getActualPods(), this.getMaxPods());
				str = "O+"+TrunkObj.getId()+"|"+TrunkObj.getCantidad()+"|"+TrunkObj.getModelo().getId()+"|"+TrunkObj.parseStatsString();
			}
		}else // Le joueur avait deja� un item similaire
		{
			//S'il ne reste rien dans le coffre
			if(newQua <= 0)
			{
				//On retire l'item du coffre
				this.objects.remove(TrunkObj.getId());
				Mundo.mundo.removeGameObject(TrunkObj.getId());
				//On Modifie la quantite de l'item du sac du joueur
				playerObj.setCantidad(playerObj.getCantidad() + TrunkObj.getCantidad());
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P, playerObj);
				GestorSalida.GAME_SEND_Ew_PACKET(P, this.getActualPods(), this.getMaxPods());
				str = "O-"+guid;
			}else//S'il reste des objets dans le coffre
			{
				//On retire X objet du coffre
				TrunkObj.setCantidad(newQua);
				//On ajoute X objets au joueurs
				playerObj.setCantidad(playerObj.getCantidad() + qua);
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P,playerObj);
				GestorSalida.GAME_SEND_Ew_PACKET(P, this.getActualPods(), this.getMaxPods());
				str = "O+"+TrunkObj.getId()+"|"+TrunkObj.getCantidad()+"|"+TrunkObj.getModelo().getId()+"|"+TrunkObj.parseStatsString();

			}
		}

		GestorSalida.GAME_SEND_EsK_PACKET(P, str);
	}
	
	private ObjetoJuego getSimilarObject(ObjetoJuego obj) {
		for(ObjetoJuego gameObject : this.objects.values())
			if(gameObject.getModelo().getType() != 85)
				if(gameObject.getModelo().getId() == obj.getModelo().getId() && gameObject.getCaracteristicas().isSameStats(obj.getCaracteristicas()))
					return gameObject;
		return null;
	}
	
	private String convertStatsToString() {
		StringBuilder stats = new StringBuilder();
		for(java.util.Map.Entry<Integer, Integer> entry : this.stats.getEffects().entrySet()) {
			if(entry.getValue() <= 0)
				continue;
			if(stats.length() > 0)
				stats.append(",");
			stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(entry.getValue())).append("#0#0");
		}
		return stats.toString();
	}
		
	public String parse() {
        return this.id + ":" + this.color + ":" + this.ancestors + ":" + ",," + this.parseCapacitysToString() + ":" + this.name + ":" + this.sex + ":" + this.parseExp() + ":" + this.level + ":" + this.isMontable() + ":" + this.getMaxPods() + ":" + this.savage + ":" + this.endurance + ",10000:" + this.maturity + "," + this.getMaxMaturity() + ":" + this.energy + "," + this.getMaxEnergy() + ":" + this.state + ",-10000,10000:" + this.amour + ",10000:" + (this.fecundatedDate == -1 ? this.fecundatedDate : ((Instant.now().toEpochMilli() - this.fecundatedDate) / 3600000) + 1) + ":" + this.isFecund() + ":" + this.convertStatsToString() + ":" + this.fatigue + ",240:" + this.reproduction + ",20:";
	}
	
	public String parseToGM() {
		StringBuilder str = new StringBuilder();
		str.append("GM|+");
		str.append(this.cellId).append(";");
		str.append(this.orientation).append(";0;").append(this.id).append(";").append(this.name).append(";-9;");
		str.append((this.color == 88) ? 7005 : 7002);
		str.append("^").append(this.size).append(";");
		if(Mundo.mundo.getPlayer(this.owner) == null)
			str.append("Sans Maitre");
		else
			str.append(Mundo.mundo.getPlayer(this.owner).getName());
		str.append(";").append(this.level).append(";").append(this.color);
		return str.toString();
	}
	
	public String parseToMountObjects() {
		StringBuilder packet = new StringBuilder();
		for(ObjetoJuego obj : this.objects.values())
			packet.append("O").append(obj.parseItem()).append(";");
		return packet.toString();
	}
	
	public String parseObjectsToString() {
		StringBuilder str=new StringBuilder();
		for(ObjetoJuego gameObject : this.objects.values())
			str.append(str.toString().isEmpty() ? "" : ";").append(gameObject.getId());
		return str.toString();
	}
	
	public String parseCapacitysToString() {
		StringBuilder str=new StringBuilder();
		for(int capacity : this.capacitys)
			str.append(str.toString().isEmpty() ? "" : ",").append(capacity);
		return (str.toString().isEmpty() ? "0" : str.toString());
	}
	
	private String parseExp() {
		//Si la montura tiene lvl 0 se le suma 1 para que no de errores nulos
		if (this.level == 0)
			this.level = this.level + 1;
		//Una vez sumado, continuamos
		return this.exp + "," + Mundo.mundo.getExpLevel(this.level).mount + "," + Mundo.mundo.getExpLevel(this.level + 1).mount;
	}

	public static class Generacion {

		public static int getPods(int generation, int level) {
			return (100 + 50 * generation - 1) + (5 + 5 * (generation / 2)) * level;
		}

		public static int getEnergy(int generation) {
			return (1000 + 100 * generation - 1) + (10 + 5 * (generation / 2));
		}

		public static int getMaturity(int generation) {
			return generation * 1000;
		}

		public static short getTimeGestation(int generation) {
			return (short) (Configuracion.INSTANCE.getGestacionmontura() * generation);
		}

		public static short getLearningRate(int generation) {
			return switch (generation) {
				default -> (short) 100;
				case 2, 3, 4 -> (short) 80;
				case 5, 6 -> (short) 60;
				case 7, 8 -> (short) 40;
				case 9, 10 -> (short) 20;
			};
		}
	}
}
