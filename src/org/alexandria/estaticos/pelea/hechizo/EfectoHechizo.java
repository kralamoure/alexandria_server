package org.alexandria.estaticos.pelea.hechizo;

import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.Camino;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.configuracion.Constantes;
import org.alexandria.estaticos.Monstruos;
import org.alexandria.estaticos.Monstruos.MobGrade;
import org.alexandria.estaticos.juego.JuegoServidor;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.Hechizo.SortStats;
import org.alexandria.estaticos.pelea.trampas.Grifos;
import org.alexandria.estaticos.pelea.trampas.Trampas;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class EfectoHechizo {

	private final int durationFixed;
	private int effectID;
	private int turns = 0;
	private String jet = "0d0+0";
	private int chance = 100;
	private String args;
	private int value = 0;
	private Peleador caster = null;
	private int spell = 0;
	private int spellLvl = 1;
	private boolean debuffable = true;
	private int duration = 0;
	private GameCase cell = null;
	private final boolean aoe = false;
	private final boolean isTrap = false;

	public EfectoHechizo(int aID, String aArgs, int aSpell, int aSpellLevel) {
		effectID = aID;
		args = aArgs;
		spell = aSpell;
		spellLvl = aSpellLevel;
		durationFixed = 0;
		try {
			value = Integer.parseInt(args.split(";")[0]);
			turns = Integer.parseInt(args.split(";")[3]);
			chance = Integer.parseInt(args.split(";")[4]);
			jet = args.split(";")[5];
		} catch (Exception ignored) {
		}
	}

	public EfectoHechizo(int id, int value2, int aduration, int turns2, boolean debuff, Peleador aCaster, String args2, int aspell) {
		effectID = id;
		value = value2;
		turns = turns2;
		debuffable = debuff;
		caster = aCaster;
		duration = aduration;
		this.durationFixed = duration;
		args = args2;
		spell = aspell;
		try {
			jet = args.split(";")[5];
		} catch (Exception ignored) {
		}
	}

	public static ArrayList<Peleador> getTargets(EfectoHechizo SE, Pelea fight, ArrayList<GameCase> cells) {
		ArrayList<Peleador> cibles = new ArrayList<>();
		for (GameCase aCell : cells) {
			if (aCell == null) continue;
			Peleador f = aCell.getFirstFighter();
			if (f == null) continue;
			cibles.add(f);
		}
		return cibles;
	}

	public static int applyOnHitBuffs(int finalDommage, Peleador target, Peleador caster, Pelea fight, int elementId) {
		for (int id : Constantes.ON_HIT_BUFFS) {
			for (EfectoHechizo buff : target.getBuffsByEffectID(id)) {
				switch (id) {
					case 114:
						if (buff.spell == 521) finalDommage = finalDommage * 2;
						break;
					case 138:
						if (buff.getSpell() == 1039) {
							int stats = 0;
							if (elementId == Constantes.ELEMENT_AIR)
								stats = 217;
							else if (elementId == Constantes.ELEMENT_EAU)
								stats = 216;
							else if (elementId == Constantes.ELEMENT_FEU)
								stats = 218;
							else if (elementId == Constantes.ELEMENT_NEUTRE)
								stats = 219;
							else if (elementId == Constantes.ELEMENT_TERRE)
								stats = 215;
							int val = target.getBuff(stats).getValue();
							int turns = target.getBuff(stats).getTurn();
							int duration = target.getBuff(stats).getDurationFixed();
							String args = target.getBuff(stats).getArgs();
							for (int i : Constantes.getOppositeStats(stats))
								target.addBuff(i, val, turns, duration, true, buff.getSpell(), args, caster, true);
							target.addBuff(stats, val, duration, turns, true, buff.getSpell(), args, caster, true);
						}
						break;
					case 9://Derobade
						//Si pas au cac (distance == 1)
						int d = Camino.getDistanceBetween(fight.getMap(), target.getCell().getId(), caster.getCell().getId());
						if (d > 1) continue;
						int chan = buff.getValue();
						int c = Formulas.getRandomValue(0, 99);
						if (c + 1 >= chan) continue;//si le deplacement ne s'applique pas
						int nbrCase = 0;
						try {
							nbrCase = Integer.parseInt(buff.getArgs().split(";")[1]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (nbrCase == 0) continue;
						int exCase = target.getCell().getId();
						int newCellID = Camino.newCaseAfterPush(fight, caster.getCell(), target.getCell(), nbrCase, false);
						if (newCellID < 0)//S'il a été bloqué
						{
							int a = -newCellID;
							a = nbrCase - a;
							newCellID = Camino.newCaseAfterPush(fight, caster.getCell(), target.getCell(), a, false);
							if (newCellID == 0)
								continue;
							if (fight.getMap().getCase(newCellID) == null)
								continue;
						}
						target.getCell().getFighters().clear();
						target.setCell(fight.getMap().getCase(newCellID));
						target.getCell().addFighter(target);
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 5, target.getId() + "", target.getId() + "," + newCellID);

						ArrayList<Trampas> P = (new ArrayList<>());
						P.addAll(fight.getAllTraps());
						for (Trampas p : P) {
							int dist = Camino.getDistanceBetween(fight.getMap(), p.getCelda().getId(), target.getCell().getId());
							//on active le piege
							if (dist <= p.getSize()) p.onTrampa(target);
						}
						//si le joueur a bouger
						if (exCase != newCellID)
							finalDommage = 0;
						break;

					case 79://chance éca
						try {
							String[] infos = buff.getArgs().split(";");
							int coefDom = Integer.parseInt(infos[0]);
							int coefHeal = Integer.parseInt(infos[1]);
							int chance = Integer.parseInt(infos[2]);
							int jet = Formulas.getRandomValue(0, 99);

							if (jet < chance)//Soin
							{
								finalDommage = -(finalDommage * coefHeal);
								if (-finalDommage > (target.getPdvMax() - target.getPdv()))
									finalDommage = -(target.getPdvMax() - target.getPdv());
							} else//Dommage
								finalDommage = finalDommage * coefDom;
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;

					case 107://renvoie Dom
						if (target.getId() == caster.getId()) break;

						if (caster.hasBuff(765))//sacrifice
						{
							if (caster.getBuff(765) != null && !caster.getBuff(765).getCaster().isDead()) {
								buff.applyEffect_765B(fight, caster);
								caster = caster.getBuff(765).getCaster();
							}
						}

						String[] args = buff.getArgs().split(";");
						float coef = 1 + (target.getTotalStats().getEffect(Constantes.STATS_ADD_SAGE) / 100);
						int renvoie = 0;
						try {
							if (Integer.parseInt(args[1]) != -1) {
								renvoie = (int) (coef * Formulas.getRandomValue(Integer.parseInt(args[0]), Integer.parseInt(args[1])));
							} else {
								renvoie = (int) (coef * Integer.parseInt(args[0]));
							}
						} catch (Exception e) {
							return finalDommage;
						}
						if (renvoie > finalDommage) renvoie = finalDommage;
						finalDommage -= renvoie;
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 107, "-1", target.getId() + "," + renvoie);
						if (renvoie > caster.getPdv()) renvoie = caster.getPdv();
						if (finalDommage < 0) finalDommage = 0;
						caster.removePdv(caster, renvoie);
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId() + "", caster.getId() + ",-" + renvoie);
						break;
					case 606://Chatiment (acncien)
						int stat = buff.getValue();
						int jet = Formulas.getRandomJet(buff.getJet());
						target.addBuff(stat, jet, -1, -1, false, buff.getSpell(), buff.getArgs(), caster, true);
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, stat, caster.getId() + "", target.getId() + "," + jet + "," + -1);
						break;
					case 607://Chatiment (acncien)
                    case 608:
                    case 609:
                    case 611:
                        stat = buff.getValue();
						jet = Formulas.getRandomJet(buff.getJet());
						target.addBuff(stat, jet, -1, -1, false, buff.getSpell(), buff.getArgs(), caster, true);
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, stat, caster.getId() + "", target.getId() + "," + jet + "," + -1);
						break;
                    case 788://Chatiments
						int taux = (caster.getPlayer() == null ? 1 : 2), gain = finalDommage / taux, max = 0;
						stat = buff.getValue();

						try {
							max = Integer.parseInt(buff.getArgs().split(";")[1]);
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}

						//on retire au max possible la valeur déjà gagné sur le chati
						int oldValue = (target.getChatiValue().get(stat) == null ? 0 : target.getChatiValue().get(stat));
						max -= oldValue;
						//Si gain trop grand, on le reduit au max
						if (gain > max) gain = max;
						//On met a jour les valeurs des chatis
						int newValue = oldValue + gain;

						if (stat == 125) {
							GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, stat, caster.getId() + "", target.getId() + "," + gain + "," + 5);
							target.setPdv(target.getPdv() + gain);
							if(target.getPlayer() != null) GestorSalida.GAME_SEND_STATS_PACKET(target.getPlayer());
						} else {
							target.getChatiValue().put(stat, newValue);
							target.addBuff(stat, gain, 5, 1, false, buff.getSpell(), buff.getArgs(), caster, true);
							GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, stat, caster.getId() + "", target.getId() + "," + gain + "," + 5);
						}
						target.getChatiValue().put(stat, newValue);
						break;

					default:
						break;
				}
			}
		}

		return finalDommage;
	}

	public int getDuration() {
		return duration;
	}

	public int getTurn() {
		return turns;
	}

	public void setTurn(int turn) {
		this.turns = turn;
	}

	public boolean isDebuffabe() {
		return debuffable;
	}

	public int getEffectID() {
		return effectID;
	}

	public void setEffectID(int id) {
		effectID = id;
	}

	public String getJet() {
		return jet;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int i) {
		value = i;
	}

	public int getChance() {
		return chance;
	}

	public String getArgs() {
		return args;
	}

	public void setArgs(String newArgs) {
		args = newArgs;
	}

	public int getMaxMinSpell(Peleador fighter, int value) {
		int val = value;
		if (fighter.hasBuff(782)) {
			int max = Integer.parseInt(args.split(";")[1]);
			if (max == -1)
				max = Integer.parseInt(args.split(";")[0]);
			val = max;
		} else if (fighter.hasBuff(781))
			val = Integer.parseInt(args.split(";")[0]);
		return val;
	}

	public int decrementDuration() {
		duration -= 1;
		return duration;
	}

	public void applyBeginingBuff(Pelea fight, Peleador fighter) {
		ArrayList<Peleador> targets = new ArrayList<>();
		targets.add(fighter);
		this.turns = -1;
		this.applyToFight(fight, this.caster, targets, false);
	}

	public void applyToFight(Pelea fight, Peleador perso, GameCase Cell, ArrayList<Peleador> cibles) {
		cell = Cell;
		applyToFight(fight, perso, cibles, false);
	}

	private int getDurationFixed() {
		return this.durationFixed;
	}

	public Peleador getCaster() {
		return caster;
	}

	public int getSpell() {
		return spell;
	}

	public void applyToFight(Pelea fight, Peleador acaster, ArrayList<Peleador> cibles, boolean isCaC) {
		try {
			if (turns != -1)//Si ce n'est pas un buff qu'on applique en début de tour
				turns = Integer.parseInt(args.split(";")[3]);
		} catch (NumberFormatException ignored) {}
		caster = acaster;
		try {
			jet = args.split(";")[5];
		} catch (Exception ignored) {}

		if (caster.getPlayer() != null) {
			Jugador perso = caster.getPlayer();
			if (perso.getObjectsClassSpell().containsKey(spell)) {
				int modi = 0;
				if (effectID == 108)
					modi = perso.getValueOfClassObject(spell, 284);
				else if (effectID >= 91 && effectID <= 100)
					modi = perso.getValueOfClassObject(spell, 283);
				String jeta = jet.split("\\+")[0];
				int bonus = Integer.parseInt(jet.split("\\+")[1]) + modi;
				jet = jeta + "+" + bonus;
			}
		}

		switch (effectID) {
			case 4://Fuite/Bond du félin/ Bond du iop / téléport
				applyEffect_4(fight, cibles);
				break;
			case 5://Repousse de X case
				applyEffect_5(cibles, fight);
				break;
			case 6://Attire de X case
				applyEffect_6(cibles, fight);
				break;
			case 8://Echange les place de 2 joueur
				applyEffect_8(cibles, fight);
				break;
			case 9://Esquive une attaque en reculant de 1 case
				applyEffect_9(cibles, fight);
				break;
			case 50://Levantar un Personaje
				LevantarPersonaje(fight);
				break;
			case 51://Lanzar un personaje
				LanzarPersonaje(fight);
				break;
			case 77://Vol de PM
				applyEffect_77(cibles, fight);
				break;
			case 78://Bonus PM
				applyEffect_78(cibles, fight);
				break;
			case 79:// + X chance(%) dommage subis * Y sinon soigné de dommage *Z
				applyEffect_79(cibles, fight);
				break;
			case 81:// Cura, PDV devueltos
				applyEffect_81(cibles, fight);
				break;
			case 82://Vol de Vie fixe
				applyEffect_82(cibles, fight);
				break;
			case 84://Vol de PA
				applyEffect_84(cibles, fight);
				break;
			case 85://Dommage Eau %vie
				applyEffect_85(cibles, fight);
				break;
			case 86://Dommage Terre %vie
				applyEffect_86(cibles, fight);
				break;
			case 87://Dommage Air %vie
				applyEffect_87(cibles, fight);
				break;
			case 88://Dommage feu %vie
				applyEffect_88(cibles, fight);
				break;
			case 89://Dommage neutre %vie
				applyEffect_89(cibles, fight);
				break;
			case 90://Donne X% de sa vie
				applyEffect_90(cibles, fight);
				break;
			case 91://Vol de Vie Eau
				applyEffect_91(cibles, fight, isCaC);
				break;
			case 92://Vol de Vie Terre
				applyEffect_92(cibles, fight, isCaC);
				break;
			case 93://Vol de Vie Air
				applyEffect_93(cibles, fight, isCaC);
				break;
			case 94://Vol de Vie feu
				applyEffect_94(cibles, fight, isCaC);
				break;
			case 95://Vol de Vie neutre
				applyEffect_95(cibles, fight, isCaC);
				break;
			case 96://Dommage Eau
				applyEffect_96(cibles, fight, isCaC);
				break;
			case 97://Dommage Terre
				applyEffect_97(cibles, fight, isCaC);
				break;
			case 98://Dommage Air
				applyEffect_98(cibles, fight, isCaC);
				break;
			case 99://Dommage feu
				applyEffect_99(cibles, fight, isCaC);
				break;
			case 100://Dommage neutre
				applyEffect_100(cibles, fight, isCaC);
				break;
			case 101://Retrait PA
				applyEffect_101(cibles, fight);
				break;
			case 105://Dommages réduits de X
				applyEffect_105(cibles, fight);
				break;
			case 106://Renvoie de sort
				applyEffect_106(cibles, fight);
				break;
			case 107://Renvoie de dom
				applyEffect_107(cibles, fight);
				break;
			case 108://Soin
				applyEffect_108(cibles, fight, isCaC);
				break;
			case 109://Dommage pour le lanceur
				applyEffect_109(fight);
				break;
			case 110://+ X vie
				applyEffect_110(cibles, fight);
				break;
			case 111://+ X PA
				applyEffect_111(cibles, fight);
				break;
			case 112://+Dom
				applyEffect_112(cibles, fight);
				break;
			case 114://Multiplie les dommages par X
				applyEffect_114(cibles, fight);
				break;
			case 115://+Cc
				applyEffect_115(cibles, fight);
				break;
			case 116://Malus PO
				applyEffect_116(cibles, fight);
				break;
			case 117://Bonus PO
				applyEffect_117(cibles, fight);
				break;
			case 118://Bonus force
				applyEffect_118(cibles, fight);
				break;
			case 119://Bonus Agilité
				applyEffect_119(cibles, fight);
				break;
			case 120://Bonus PA
				applyEffect_120(cibles, fight);
				break;
			case 121://+Dom
				applyEffect_121(cibles, fight);
				break;
			case 122://+EC
				applyEffect_122(cibles, fight);
				break;
			case 123://+Chance
				applyEffect_123(cibles, fight);
				break;
			case 124://+Sagesse
				applyEffect_124(cibles, fight);
				break;
			case 125://+Vitalité
				applyEffect_125(cibles, fight);
				break;
			case 126://+Intelligence
				applyEffect_126(cibles, fight);
				break;
			case 127://Retrait PM
				applyEffect_127(cibles, fight);
				break;
			case 128://+PM
				applyEffect_128(cibles, fight);
				break;
			case 130://Vol de kamas
				applyEffect_130(fight, cibles);
				break;
			case 131://Poison : X Pdv  par PA
				applyEffect_131(cibles, fight);
				break;
			case 132://Enleve les envoutements
				applyEffect_132(cibles, fight);
				break;
			case 138://%dom
				applyEffect_138(cibles, fight);
				break;
			case 140://Passer le tour
				applyEffect_140(cibles, fight);
				break;
			case 141://Tue la cible
				applyEffect_141(fight, cibles);
				break;
			case 142://Dommages physique
				applyEffect_142(fight, cibles);
				break;
			case 143:// PDV rendu
				applyEffect_143(cibles, fight);
				break;
			case 144:// - Dommages (pas bosté)
				applyEffect_144(fight, cibles);
			case 145://Malus Dommage
				applyEffect_145(fight, cibles);
				break;
			case 149://Change l'apparence
				applyEffect_149(fight, cibles);
				break;
			case 150://Invisibilité
				applyEffect_150(fight, cibles);
				break;
			case 152:// - Chance
				applyEffect_152(fight, cibles);
				break;
			case 153:// - Vita
				applyEffect_153(fight, cibles);
				break;
			case 154:// - Agi
				applyEffect_154(fight, cibles);
				break;
			case 155:// - Intel
				applyEffect_155(fight, cibles);
				break;
			case 156:// - Sagesse
				applyEffect_156(fight, cibles);
				break;
			case 157:// - Force
				applyEffect_157(fight, cibles);
				break;
			case 160:// + Esquive PA
				applyEffect_160(fight, cibles);
				break;
			case 161:// + Esquive PM
				applyEffect_161(fight, cibles);
				break;
			case 162:// - Esquive PA
				applyEffect_162(fight, cibles);
				break;
			case 163:// - Esquive PM
				applyEffect_163(fight, cibles);
				break;
			case 164:// Daños reducidos en x%
				applyEffect_164(cibles, fight);
				break;
			case 165:// Maîtrises
				applyEffect_165(fight, cibles);
				break;
			case 168://Perte PA non esquivable
				applyEffect_168(cibles, fight);
				break;
			case 169://Perte PM non esquivable
				applyEffect_169(cibles, fight);
				break;
			case 171://Malus CC
				applyEffect_171(fight, cibles);
				break;
			case 176:// + prospection
				applyEffect_176(cibles, fight);
				break;
			case 177:// - prospection
				applyEffect_177(cibles, fight);
				break;
			case 178:// + soin
				applyEffect_178(cibles, fight);
				break;
			case 179:// - soin
				applyEffect_179(cibles, fight);
				break;
			case 180://Double du sram
				applyEffect_180(fight);
				break;
			case 181://Invoque une créature
				applyEffect_181(fight);
				break;
			case 182://+ Crea Invoc
				applyEffect_182(fight, cibles);
				break;
			case 183://Resist Magique
				applyEffect_183(fight, cibles);
				break;
			case 184://Resist Physique
				applyEffect_184(fight, cibles);
				break;
			case 185://Invoque une creature statique
				applyEffect_185(fight);
				break;
			case 186://Diminue les dommages %
				applyEffect_186(fight, cibles);
				break;
			case 202://Perception
				applyEffect_202(fight, cibles);
				break;
			case 210://Resist % terre
				applyEffect_210(fight, cibles);
				break;
			case 211://Resist % eau
				applyEffect_211(fight, cibles);
				break;
			case 212://Resist % air
				applyEffect_212(fight, cibles);
				break;
			case 213://Resist % feu
				applyEffect_213(fight, cibles);
				break;
			case 214://Resist % neutre
				applyEffect_214(fight, cibles);
				break;
			case 215://Faiblesse % terre
				applyEffect_215(fight, cibles);
				break;
			case 216://Faiblesse % eau
				applyEffect_216(fight, cibles);
				break;
			case 217://Faiblesse % air
				applyEffect_217(fight, cibles);
				break;
			case 218://Faiblesse % feu
				applyEffect_218(fight, cibles);
				break;
			case 219://Faiblesse % neutre
				applyEffect_219(fight, cibles);
				break;
			case 220:// Renvoie dommage
				applyEffect_220(cibles, fight);
				break;
			case 265://Reduit les Dom de X
				applyEffect_265(fight, cibles);
				break;
			case 266://Vol Chance
				applyEffect_266(fight, cibles);
				break;
			case 267://Vol vitalité
				applyEffect_267(fight, cibles);
				break;
			case 268://Vol agitlité
				applyEffect_268(fight, cibles);
				break;
			case 269://Vol intell
				applyEffect_269(fight, cibles);
				break;
			case 270://Vol sagesse
				applyEffect_270(fight, cibles);
				break;
			case 271://Vol force
				applyEffect_271(fight, cibles);
				break;
			case 293://Augmente les dégâts de base du sort X de Y
				applyEffect_293(fight);
				break;
			case 320://Vol de PO
				applyEffect_320(fight, cibles);
				break;
			case 400://Créer un  piège
				applyEffect_400(fight);
				break;
			case 401://Créer une glyphe
				applyEffect_401(fight);
				break;
			case 402://Glyphe des Blop
				applyEffect_402(fight);
				break;
			case 666://Pas d'effet complémentaire
				break;
			case 671://Dommages : X% de la vie de l'attaquant (neutre)
				applyEffect_671(cibles, fight);
				break;
			case 672://Dommages : X% de la vie de l'attaquant (neutre)
				applyEffect_672(cibles, fight);
				break;
			case 765://Sacrifice
				applyEffect_765(cibles, fight);
				break;
			case 776://Enleve %vita pendant l'attaque
				applyEffect_776(cibles, fight);
				break;
			case 780://laisse spirituelle
				applyEffect_780(fight);
				break;
			case 781://Minimize les effets aléatoires
				applyEffect_781(cibles, fight);
				break;
			case 782://Maximise les effets aléatoires
				applyEffect_782(cibles, fight);
				break;
			case 783://Pousse jusqu'a la case visé
				applyEffect_783(cibles, fight);
				break;
			case 784://Raulebaque
				applyEffect_784(cibles, fight);
				break;
			case 786://Soin pendant l'attaque
				applyEffect_786(cibles, fight);
				break;
			case 787://Change etat
				applyEffect_787(cibles, fight);
				break;
			case 788://Chatiment de X sur Y tours
				applyEffect_788(cibles, fight);
				break;
			case 950://Etat X
				applyEffect_950(fight, cibles);
				break;
			case 951://Enleve l'Etat X
				applyEffect_951(fight, cibles);
				break;
			case 1000: // Glyph pair
				applyEffect_1000(fight, cibles);
				break;
			case 1001: // Glyph impair
				applyEffect_1001(fight, cibles);
				break;
			case 1002: // Glyph kaskargo
				applyEffect_1002(fight, cibles);
				break;
			default:
				JuegoServidor.a();
				break;
		}
	}


	private void applyEffect_4(Pelea fight, ArrayList<Peleador> cibles) {
		final Peleador portador = caster.getHoldedBy();
		if (turns > 1)
			return;//Olol bondir 3 tours apres ?

		if (cell.isWalkable(true) && !fight.isOccuped(cell.getId()))//Si la case est prise, on va �viter que les joueurs se montent dessus *-*
		{
			//Eliminamos estado portado y portador cuando teletransportan
			if (caster.haveState(Constantes.ETAT_PORTE) || caster.haveState(Constantes.ETAT_PORTEUR)) {
			caster.setState(Constantes.ETAT_PORTE,0,caster.getId());
			portador.setState(Constantes.ETAT_PORTEUR,0,portador.getId());
			}

			caster.getCell().getFighters().clear();
			caster.setCell(cell);
			caster.getCell().addFighter(caster);

			VerificarTrampa(fight, caster);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, caster.getId() + "", caster.getId() + "," + cell.getId());
		}
	}

	private void applyEffect_5(ArrayList<Peleador> cibles, Pelea fight) {
		if(cibles.size() == 1 && spell == 120 || spell == 310)
			if(!cibles.get(0).isDead())
				caster.setOldCible(cibles.get(0));

		if(turns<=0)
		{
            switch (spell) {
                //Flecha flaminguera, trampa repulsiva, flecha dispersion
                case 73, 418, 151, 165 -> cibles = this.trierCibles(cibles, fight);
            }

			for(Peleador target : cibles)
			{
				boolean next=false;
				if(target.getMob()!=null)
					for(int i : Constantes.STATIC_INVOCATIONS)
						if(i==target.getMob().getTemplate().getId())
							next=true;

				if(target.haveState(6)||next)
					continue;

				GameCase cell=this.cell;

				if(target.getCell().getId()==this.cell.getId()||spell==73)
					cell=caster.getCell();

				int newCellId=Camino.newCaseAfterPush(fight,cell,target.getCell(),value,spell==73);
				if(newCellId==0)
					return;
				if(newCellId<0)
				{

					int a=-newCellId;
					int finalDmg=Formulas.pushDamage(-newCellId,(caster.isInvocation() ? caster.getInvocator().getLvl() : caster.getLvl()),caster.getTotalStats().getEffect(Constantes.STATS_ADD_PUSH),caster.getTotalStats().getEffect(Constantes.STATS_REM_PUSH),target.getTotalStats().getEffect(Constantes.STATS_ADD_R_PUSH),target.getTotalStats().getEffect(Constantes.STATS_REM_R_PUSH));

					if(finalDmg<1)
						finalDmg=1;
					if(finalDmg>target.getPdv())
						finalDmg=target.getPdv();

					if(target.hasBuff(184))
					{
						finalDmg=finalDmg-target.getBuff(184).getValue();//Réduction physique
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,105,caster.getId()+"",target.getId()+","+target.getBuff(184).getValue());
					}
					if(target.hasBuff(105))
					{
						finalDmg=finalDmg-target.getBuff(105).getValue();//Immu
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,105,caster.getId()+"",target.getId()+","+target.getBuff(105).getValue());
					}
					if(finalDmg>0)
					{
						if(finalDmg>200)
							finalDmg=Formulas.getRandomValue(189,211);
						target.removePdv(caster,finalDmg);
						target.removePdvMax((int)Math.floor(finalDmg*(Configuracion.INSTANCE.getErosion()+caster.getTotalStats().getEffect(Constantes.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constantes.STATS_REM_ERO)-target.getTotalStats().getEffect(Constantes.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constantes.STATS_REM_R_ERO)))/100);
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+",-"+finalDmg);
						if(target.getPdv()<=0)
						{
							fight.onFighterDie(target,caster);
							if(target.canPlay()&&target.getPlayer()!=null)
								fight.endTurn(false);
							else if(target.canPlay())
								target.setCanPlay(false);
							return;
						}
					}
					a=value-a;
					newCellId=Camino.newCaseAfterPush(fight,caster.getCell(),target.getCell(),a,spell==73);

					char dir=Camino.getDirBetweenTwoCase(cell.getId(),target.getCell().getId(),fight.getMap(),true);
					GameCase nextCase=fight.getMap().getCase(Camino.GetCaseIDFromDirrection(target.getCell().getId(),dir,fight.getMap(),true));

					if(nextCase!=null&&nextCase.getFirstFighter()!=null)
					{
						Peleador wallTarget=nextCase.getFirstFighter();
						finalDmg=finalDmg/2;
						if(finalDmg<1)
							finalDmg=1;
						if(finalDmg>0)
						{
							wallTarget.removePdv(caster,finalDmg);
							target.removePdvMax((int)Math.floor(finalDmg*(Configuracion.INSTANCE.getErosion()+caster.getTotalStats().getEffect(Constantes.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constantes.STATS_REM_ERO)-target.getTotalStats().getEffect(Constantes.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constantes.STATS_REM_R_ERO)))/100);
							GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",wallTarget.getId()+",-"+finalDmg);
							if(wallTarget.getPdv()<=0)
								fight.onFighterDie(wallTarget,caster);
						}
					}

					if(newCellId==0)
						continue;
					if(fight.getMap().getCase(newCellId)==null)
						continue;
				}

				target.getCell().getFighters().clear();
				target.setCell(fight.getMap().getCase(newCellId));
				target.getCell().addFighter(target);

				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,5,caster.getId()+"",target.getId()+","+newCellId);
				VerificarTrampa(fight,target);
			}
		}
	}

	private void applyEffect_6(ArrayList<Peleador> cibles, Pelea fight) {
		if (turns <= 0) {
			for (Peleador target : cibles) {
				if (target.getMob() != null)
					if (282 == target.getMob().getTemplate().getId() ||
							556 == target.getMob().getTemplate().getId()
							|| 2750 == target.getMob().getTemplate().getId()
							|| 7000 == target.getMob().getTemplate().getId())
						continue;
				if (target.haveState(6))
					continue;
				GameCase eCell = cell;
				//Si meme case
				if (target.getCell().getId() == cell.getId()) {
					//on prend la cellule caster
					eCell = caster.getCell();
				}
				int newCellID = Camino.newCaseAfterPush(fight, eCell, target.getCell(), -value);
				if (newCellID == 0)
					continue;

				if (newCellID < 0)//S'il a �t� bloqu�
				{
					int a = -(value + newCellID);
					newCellID = Camino.newCaseAfterPush(fight, caster.getCell(), target.getCell(), a);
					if (newCellID == 0)
						continue;
					if (fight.getMap().getCase(newCellID) == null)
						continue;
				}

				target.getCell().getFighters().clear();
				target.setCell(fight.getMap().getCase(newCellID));
				target.getCell().addFighter(target);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 5, caster.getId() + "", target.getId() + "," + newCellID);

				ArrayList<Trampas> P = (new ArrayList<>());
				P.addAll(fight.getAllTraps());
				for (Trampas p : P) {
					int dist = Camino.getDistanceBetween(fight.getMap(), p.getCelda().getId(), target.getCell().getId());
					//on active le piege
					if (dist <= p.getSize())
						p.onTrampa(target);
				}
			}
		}
	}

	private void applyEffect_8(ArrayList<Peleador> cibles, Pelea fight) {
		if (cibles.isEmpty())
			return;
		Peleador target = cibles.get(0);
		if (target == null)
			return;//ne devrait pas arriver
		if (target.haveState(6))
			return;//Stabilisation
		switch (spell) {
			case 438://Transpo
				//si les 2 joueurs ne sont pas dans la meme team, on ignore
				if (target.getTeam() != caster.getTeam())
					return;
				break;

			case 445://Coop
				//si les 2 joueurs sont dans la meme team, on ignore
				if (target.getTeam() == caster.getTeam())
					return;
				break;

			case 449://D�tour
			default:
				break;
		}
		//on enleve les persos des cases
		target.getCell().getFighters().clear();
		caster.getCell().getFighters().clear();
		//on retient les cases
		GameCase exTarget = target.getCell();
		GameCase exCaster = caster.getCell();
		//on �change les cases
		target.setCell(exCaster);
		caster.setCell(exTarget);
		//on ajoute les fighters aux cases
		target.getCell().addFighter(target);
		caster.getCell().addFighter(caster);
		ArrayList<Trampas> P = (new ArrayList<>());
		P.addAll(fight.getAllTraps());
		for (Trampas p : P) {
			int dist = Camino.getDistanceBetween(fight.getMap(), p.getCelda().getId(), target.getCell().getId());
			int dist2 = Camino.getDistanceBetween(fight.getMap(), p.getCelda().getId(), caster.getCell().getId());
			//on active le piege
			if (dist <= p.getSize())
				p.onTrampa(target);
			else if (dist2 <= p.getSize())
				p.onTrampa(caster);
		}
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, caster.getId()
				+ "", target.getId() + "," + exCaster.getId());
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, caster.getId()
				+ "", caster.getId() + "," + exTarget.getId());

	}

	private void applyEffect_9(ArrayList<Peleador> cibles, Pelea fight) {
		for (Peleador target : cibles) {
			target.addBuff(effectID, value, turns, 1, true, spell, args, caster, true);
		}
	}

	private void LevantarPersonaje(Pelea fight) {
		Peleador target = cell.getFirstFighter();

		//Evitamos que levanten a un pj muerto o en algun estado
		if(target==null || target.isDead() || target.haveState(Constantes.ETAT_PORTEUR) || target.haveState(Constantes.ETAT_PORTE) || target.haveState(Constantes.ETAT_ENRACINE))
		return;
		if (target.getMob() != null)
			for (int i : Constantes.STATIC_INVOCATIONS)
				if (i == target.getMob().getTemplate().getId())
					return;
		if (target.haveState(6)) return;//Stabilisation

		//on enleve le porté de sa case
		target.getCell().getFighters().clear();
		//on lui définie sa nouvelle case
		target.setCell(caster.getCell());

		//on applique les états
		target.setState(Constantes.ETAT_PORTE, -1 , target.getId());
		caster.setState(Constantes.ETAT_PORTEUR, -1 , caster.getId());
		//on lie les 2 Fighter
		target.setHoldedBy(caster);
		caster.setIsHolding(target);

		if(target.haveState(Constantes.STATE_SOBER))
		{
			target.setHadSober(true);
			target.setState(Constantes.STATE_SOBER,0,caster.getId()); //duration 0, remove state
		}

		//on envoie les packets
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,target.getId()+"",target.getId()+","+Constantes.ETAT_PORTE+",1");
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",caster.getId()+","+Constantes.ETAT_PORTEUR+",1");
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 50, caster.getId() + "", "" + target.getId());
		fight.setCurAction("");
	}

	private void LanzarPersonaje(final Pelea fight) {
		if(!cell.isWalkable(true)||cell.getFighters().size()>0)
			return;
		Peleador target=caster.getIsHolding();
		if(target==null)
			return;

		if(!target.isDead()) {
			caster.getCell().removeFighter(target);
			target.setCell(cell);
			target.getCell().addFighter(target);
			target.setState(Constantes.ETAT_PORTE,0,caster.getId()); //duration 0, remove state
			target.setHoldedBy(null);
			if(target.getHadSober()) {
				target.setHadSober(false);
				target.setState(Constantes.STATE_SOBER,-1,caster.getId()); //infinite duration
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,target.getId()+"",target.getId()+","+Constantes.STATE_SOBER+",1");
			}
		}

		caster.setState(Constantes.ETAT_PORTEUR,0,caster.getId()); //duration 0, remove state
		caster.setIsHolding(null);
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,51, String.valueOf(this.caster.getId()), String.valueOf(this.cell.getId()));
		VerificarTrampa(fight,target);
		fight.setCurAction("");
		}

	private void applyEffect_77(ArrayList<Peleador> cibles, Pelea fight) {
		int value = 1;
		try {
			value = Integer.parseInt(args.split(";")[0]);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		int num = 0;
		for (Peleador target : cibles) {
			int val = Formulas.getPointsLost('m', value, caster, target);

			if (val < value)
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 309, caster.getId() + "", target.getId() + "," + (value - val));
			if (val < 1)
				continue;

			target.addBuff(Constantes.STATS_REM_PM, val, turns == 0 ? 1 : turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_REM_PM, caster.getId() + "", target.getId() + ",-" + val + "," + turns);
			num += val;
		}
		if (num != 0) {
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_ADD_PM, caster.getId() + "", caster.getId() + "," + num + "," + turns);
			caster.addBuff(Constantes.STATS_ADD_PM, num, 1, 0, true, spell, args, caster, false);
			//Gain de PM pendant le tour de jeu
			if (caster.canPlay())
				caster.setCurPm(fight, num);
		}
	}

	private void applyEffect_78(ArrayList<Peleador> cibles, Pelea fight)//Bonus PA
	{
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_79(ArrayList<Peleador> cibles, Pelea fight) {
		if (turns < 1)
			return;//Je vois pas comment, vraiment ...
		else {
			for (Peleador target : cibles) {
				target.addBuff(effectID, -1, turns, 0, true, spell, args, caster, true);//on applique un buff
			}
		}
	}

	private void applyEffect_81(ArrayList<Peleador> cibles, Pelea fight) {// healcion
		if (turns <= 0) {
			String[] jet = args.split(";");
			int heal = 0;
			if (jet.length < 6) {
				heal = 1;
			} else {
				heal = Formulas.getRandomJet(jet[5]);
			}
			int heal2 = heal;
			for (Peleador cible : cibles) {
				if (cible.isDead())
					continue;
				if (spell == 521)// ruse kistoune
					if (cible.getTeam2() != caster.getTeam2())
						continue;
				heal = getMaxMinSpell(cible, heal);
				int pdvMax = cible.getPdvMax();
				int healFinal = Formulas.calculFinalHealCac(caster, heal, false);
				if ((healFinal + cible.getPdv()) > pdvMax)
					healFinal = pdvMax - cible.getPdv();
				if (healFinal < 1)
					healFinal = 0;
				cible.removePdv(caster, -healFinal);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 108, caster.getId()
						+ "", cible.getId() + "," + healFinal);
				heal = heal2;
			}
		} else {
			for (Peleador cible : cibles) {
				if (cible.isDead())
					continue;
				cible.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);
			}
		}
	}

	private void applyEffect_82(ArrayList<Peleador> cibles, Pelea fight) {
		if (turns <= 0) {
			for (Peleador target : cibles) {
				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}

				int dmg = Formulas.getRandomJet(args.split(";")[5]);
				//si la cible a le buff renvoie de sort et que le sort peut etre renvoyer
				if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spell != 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId() + "", target.getId() + ",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				//int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constant.ELEMENT_NULL, dmg, false, false, spell);
				int finalDommage = dmg;
				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_NULL);//S'il y a des buffs sp�ciaux
				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);
				//Vol de vie
				int heal = (int) (-finalDommage) / 2;
				if ((caster.getPdv() + heal) > caster.getPdvMax())
					heal = caster.getPdvMax() - caster.getPdv();
				caster.removePdv(caster, -heal);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
						+ "", caster.getId() + "," + heal);

				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, target);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else {
			for (Peleador target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
			}
		}
	}

	private void applyEffect_84(ArrayList<Peleador> cibles, Pelea fight) {
		int value = 1;
		try {
			value = Integer.parseInt(args.split(";")[0]);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		int num = 0;
		for (Peleador target : cibles) {
			int val = Formulas.getPointsLost('a', value, caster, target);
			if (val < value)
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 308, caster.getId() + "", target.getId() + "," + (value - val));

			if (val < 1)
				continue;

			target.addBuff(Constantes.STATS_REM_PA, val, turns == 0 ? 1 : turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_REM_PA, caster.getId() + "", target.getId() + ",-" + val + "," + turns);
			num += val;
		}
		if (num != 0) {
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_ADD_PA, caster.getId() + "", caster.getId() + "," + num + "," + turns);
			caster.addBuff(Constantes.STATS_ADD_PA, num, 1, 0, true, spell, args, caster, false);
			//Gain de PA pendant le tour de jeu
			if (caster.canPlay())
				caster.setCurPa(fight, num);
		}
	}

	private void applyEffect_85(ArrayList<Peleador> cibles, Pelea fight) {
		if (turns <= 0) {
			for (Peleador target : cibles) {

				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}
				//si la cible a le buff renvoie de sort
				if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spell != 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId() + "", target.getId() + ",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				int resP = target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_EAU);
				int resF = target.getTotalStats().getEffect(Constantes.STATS_ADD_R_EAU);
				if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
				{
					resP += target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_PVP_EAU);
					resF += target.getTotalStats().getEffect(Constantes.STATS_ADD_R_PVP_EAU);
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);//%age de pdv inflig�
				int val = caster.getPdv() / 100 * dmg;//Valeur des d�gats
				//retrait de la r�sist fixe
				val -= resF;
				int reduc = (int) (((float) val) / (float) 100) * resP;//Reduc %resis
				val -= reduc;
				if (val < 0)
					val = 0;

				val = applyOnHitBuffs(val, target, caster, fight, Constantes.ELEMENT_NULL);//S'il y a des buffs sp�ciaux

				if (val > target.getPdv())
					val = target.getPdv();//Target va mourrir
				target.removePdv(caster, val);
				int cura = val;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura);
				}
				val = -(val);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + val);
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else {
			for (Peleador target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
			}
		}
	}

	private void applyEffect_86(ArrayList<Peleador> cibles, Pelea fight) {
		if (turns <= 0) {
			for (Peleador target : cibles) {

				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}
				//si la cible a le buff renvoie de sort
				if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl
						&& spell != 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId()
							+ "", target.getId() + ",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				int resP = target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_TER);
				int resF = target.getTotalStats().getEffect(Constantes.STATS_ADD_R_TER);
				if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
				{
					resP += target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_PVP_TER);
					resF += target.getTotalStats().getEffect(Constantes.STATS_ADD_R_PVP_TER);
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);//%age de pdv inflig�
				int val = caster.getPdv() / 100 * dmg;//Valeur des d�gats
				//retrait de la r�sist fixe
				val -= resF;
				int reduc = (int) (((float) val) / (float) 100) * resP;//Reduc %resis
				val -= reduc;
				if (val < 0)
					val = 0;

				val = applyOnHitBuffs(val, target, caster, fight, Constantes.ELEMENT_NULL);//S'il y a des buffs sp�ciaux

				if (val > target.getPdv())
					val = target.getPdv();//Target va mourrir
				target.removePdv(caster, val);
				int cura = val;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura);
				}
				val = -(val);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + val);
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else {
			for (Peleador target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
			}
		}
	}

	private void applyEffect_87(ArrayList<Peleador> cibles, Pelea fight) {
		if (turns <= 0) {
			for (Peleador target : cibles) {

				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}
				//si la cible a le buff renvoie de sort
				if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl
						&& spell != 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId()
							+ "", target.getId() + ",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				if (spell == 1009)
					if (LanzarHechizo.haveEffectTarget(fight.getTeam0(), target, 108) <= 0)
						continue;

				int resP = target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_AIR);
				int resF = target.getTotalStats().getEffect(Constantes.STATS_ADD_R_AIR);
				if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
				{
					resP += target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_PVP_AIR);
					resF += target.getTotalStats().getEffect(Constantes.STATS_ADD_R_PVP_AIR);
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);//%age de pdv inflig�
				int val = caster.getPdv() / 100 * dmg;//Valeur des d�gats
				//retrait de la r�sist fixe
				val -= resF;
				int reduc = (int) (((float) val) / (float) 100) * resP;//Reduc %resis
				val -= reduc;
				if (val < 0)
					val = 0;

				val = applyOnHitBuffs(val, target, caster, fight, Constantes.ELEMENT_NULL);//S'il y a des buffs sp�ciaux

				if (val > target.getPdv())
					val = target.getPdv();//Target va mourrir
				target.removePdv(caster, val);
				int cura = val;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura);
				}
				val = -(val);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + val);
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else {
			for (Peleador target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
			}
		}
	}

	private void applyEffect_88(ArrayList<Peleador> cibles, Pelea fight) {
		if (turns <= 0) {
			for (Peleador target : cibles) {
				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}
				//si la cible a le buff renvoie de sort
				if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl
						&& spell != 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId()
							+ "", target.getId() + ",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				int resP = target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_FEU);
				int resF = target.getTotalStats().getEffect(Constantes.STATS_ADD_R_FEU);
				if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
				{
					resP += target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_PVP_FEU);
					resF += target.getTotalStats().getEffect(Constantes.STATS_ADD_R_PVP_FEU);
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);//%age de pdv inflig�
				int val = caster.getPdv() / 100 * dmg;//Valeur des d�gats
				//retrait de la r�sist fixe
				val -= resF;
				int reduc = (int) (((float) val) / (float) 100) * resP;//Reduc %resis
				val -= reduc;
				if (val < 0)
					val = 0;

				val = applyOnHitBuffs(val, target, caster, fight, Constantes.ELEMENT_NULL);//S'il y a des buffs sp�ciaux

				if (val > target.getPdv())
					val = target.getPdv();//Target va mourrir
				target.removePdv(caster, val);
				int cura = val;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura);
				}
				val = -(val);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + val);
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else {
			for (Peleador target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
			}
		}
	}

	private void applyEffect_89(ArrayList<Peleador> cibles, Pelea fight) {
		if (turns <= 0) {
			for (Peleador target : cibles) {

				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}
				//si la cible a le buff renvoie de sort
				if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl
						&& spell != 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId()
							+ "", target.getId() + ",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				int resP = target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_NEU);
				int resF = target.getTotalStats().getEffect(Constantes.STATS_ADD_R_NEU);
				if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
				{
					resP += target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_PVP_NEU);
					resF += target.getTotalStats().getEffect(Constantes.STATS_ADD_R_PVP_NEU);
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);//%age de pdv inflig�
				int val = caster.getPdv() / 100 * dmg;//Valeur des d�gats
				//retrait de la r�sist fixe
				val -= resF;
				int reduc = (int) (((float) val) / (float) 100) * resP;//Reduc %resis
				val -= reduc;
				int armor = 0;
				for (EfectoHechizo SE : target.getBuffsByEffectID(105)) {
					int intell = target.getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
					int carac = target.getTotalStats().getEffect(Constantes.STATS_ADD_FORC);
					int value = SE.getValue();
					int a = value
							* (100 + (int) (intell / 2) + (int) (carac / 2))
							/ 100;
					armor += a;
				}
				if (armor > 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId()
							+ "", target.getId() + "," + armor);
					val = val - armor;
				}
				if (val < 0)
					val = 0;

				val = applyOnHitBuffs(val, target, caster, fight, Constantes.ELEMENT_NULL);//S'il y a des buffs sp�ciaux

				if (val > target.getPdv())
					val = target.getPdv();//Target va mourrir
				target.removePdv(caster, val);
				int cura = val;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura);
				}
				val = -(val);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + val);
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else {
			for (Peleador target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
			}
		}
	}

	private void applyEffect_90(ArrayList<Peleador> cibles, Pelea fight) {
		if (turns <= 0)//Si Direct
		{
			int pAge = Formulas.getRandomJet(args.split(";")[5]);
			int val = pAge * (caster.getPdv() / 100);
			//Calcul des Doms recus par le lanceur
			int finalDommage = applyOnHitBuffs(val, caster, caster, fight, Constantes.ELEMENT_NULL);//S'il y a des buffs sp�ciaux

			if (finalDommage > caster.getPdv())
				finalDommage = caster.getPdv();//Caster va mourrir
			caster.removePdv(caster, finalDommage);
			finalDommage = -(finalDommage);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
					+ "", caster.getId() + "," + finalDommage);

			//Application du soin
			for (Peleador target : cibles) {
				if ((val + target.getPdv()) > target.getPdvMax())
					val = target.getPdvMax() - target.getPdv();//Target va mourrir
				target.removePdv(caster, -val);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + ",+" + val);
			}
			if (caster.getPdv() <= 0)
				fight.onFighterDie(caster, caster);
		} else {
			for (Peleador target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
			}
		}
	}

	private void applyEffect_91(ArrayList<Peleador> cibles, Pelea fight,
								boolean isCaC)//vole eau
	{
		if (isCaC) {
			for (Peleador target : cibles) {
				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}

				int dmg = Formulas.getRandomJet(args.split(";")[5]);
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_EAU, dmg, false, true, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_EAU);//S'il y a des buffs sp�ciaux

				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);
				int heal = (int) (-finalDommage) / 2;
				if ((caster.getPdv() + heal) > caster.getPdvMax())
					heal = caster.getPdvMax() - caster.getPdv();
				caster.removePdv(caster, -heal);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
						+ "", caster.getId() + "," + heal);
				if (target.getMob() != null)
					verifmobs(fight, target, 91, (-finalDommage));
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else if (turns <= 0) {
			if (caster.isHide())
				caster.unHide(spell);
			for (Peleador target : cibles) {

				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}
				//si la cible a le buff renvoie de sort
				if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl
						&& spell != 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId()
							+ "", target.getId() + ",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_EAU, dmg, false, false, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_EAU);//S'il y a des buffs sp�ciaux
				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);
				int heal = (int) (-finalDommage) / 2;
				if ((caster.getPdv() + heal) > caster.getPdvMax())
					heal = caster.getPdvMax() - caster.getPdv();
				caster.removePdv(caster, -heal);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
						+ "", caster.getId() + "," + heal);
				if (target.getMob() != null)
					verifmobs(fight, target, 91, (-finalDommage));
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, target);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else {
			for (Peleador target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
			}
		}
	}

	private void applyEffect_92(ArrayList<Peleador> cibles, Pelea fight,
								boolean isCaC)//vole terre
	{
		if (caster.isHide())
			caster.unHide(spell);
		if (isCaC) {
			for (Peleador target : cibles) {
				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}

				int dmg = Formulas.getRandomJet(args.split(";")[5]);
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_TERRE, dmg, false, true, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_TERRE);//S'il y a des buffs sp�ciaux

				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);
				int heal = (int) (-finalDommage) / 2;
				if ((caster.getPdv() + heal) > caster.getPdvMax())
					heal = caster.getPdvMax() - caster.getPdv();
				caster.removePdv(caster, -heal);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
						+ "", caster.getId() + "," + heal);
				if (target.getMob() != null)
					verifmobs(fight, target, 92, (-finalDommage));
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else if (turns <= 0) {
			for (Peleador target : cibles) {
				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}
				//si la cible a le buff renvoie de sort
				if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl
						&& spell != 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId()
							+ "", target.getId() + ",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_TERRE, dmg, false, false, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_TERRE);//S'il y a des buffs sp�ciaux
				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);
				int heal = (int) (-finalDommage) / 2;
				if ((caster.getPdv() + heal) > caster.getPdvMax())
					heal = caster.getPdvMax() - caster.getPdv();
				caster.removePdv(caster, -heal);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
						+ "", caster.getId() + "," + heal);
				if (target.getMob() != null)
					verifmobs(fight, target, 92, (-finalDommage));
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, target);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else {
			for (Peleador target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
			}
		}
	}

	private void applyEffect_93(ArrayList<Peleador> cibles, Pelea fight,
								boolean isCaC)//vole air
	{
		if (caster.isHide())
			caster.unHide(spell);
		if (isCaC) {
			for (Peleador target : cibles) {
				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}

				int dmg = Formulas.getRandomJet(args.split(";")[5]);
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_AIR, dmg, false, true, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_AIR);//S'il y a des buffs sp�ciaux

				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);
				int heal = (int) (-finalDommage) / 2;
				if ((caster.getPdv() + heal) > caster.getPdvMax())
					heal = caster.getPdvMax() - caster.getPdv();
				caster.removePdv(caster, -heal);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
						+ "", caster.getId() + "," + heal);
				if (target.getMob() != null)
					verifmobs(fight, target, 93, (-finalDommage));
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else if (turns <= 0) {
			for (Peleador target : cibles) {
				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}
				//si la cible a le buff renvoie de sort
				if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl
						&& spell != 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId()
							+ "", target.getId() + ",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_AIR, dmg, false, false, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_AIR);//S'il y a des buffs sp�ciaux
				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);

				int heal = (int) (-finalDommage) / 2;
				if ((caster.getPdv() + heal) > caster.getPdvMax())
					heal = caster.getPdvMax() - caster.getPdv();
				caster.removePdv(caster, -heal);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
						+ "", caster.getId() + "," + heal);
				if (target.getMob() != null)
					verifmobs(fight, target, 93, (-finalDommage));
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, target);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else {
			for (Peleador target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
			}
		}
	}

	private void applyEffect_94(ArrayList<Peleador> cibles, Pelea fight,
								boolean isCaC) {
		if (caster.isHide())
			caster.unHide(spell);
		if (isCaC)//CaC feu
		{
			for (Peleador target : cibles) {
				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}

				int dmg = Formulas.getRandomJet(args.split(";")[5]);
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_FEU, dmg, false, true, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_FEU);//S'il y a des buffs sp�ciaux

				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);
				int heal = (int) (-finalDommage) / 2;
				if ((caster.getPdv() + heal) > caster.getPdvMax())
					heal = caster.getPdvMax() - caster.getPdv();
				caster.removePdv(caster, -heal);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
						+ "", caster.getId() + "," + heal);
				if (target.getMob() != null)
					verifmobs(fight, target, 94, (-finalDommage));
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else if (turns <= 0) {
			for (Peleador target : cibles) {

				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}
				//si la cible a le buff renvoie de sort
				if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl
						&& spell != 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId()
							+ "", target.getId() + ",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_FEU, dmg, false, false, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_FEU);//S'il y a des buffs sp�ciaux
				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);
				int heal = (int) (-finalDommage) / 2;
				if ((caster.getPdv() + heal) > caster.getPdvMax())
					heal = caster.getPdvMax() - caster.getPdv();
				caster.removePdv(caster, -heal);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
						+ "", caster.getId() + "," + heal);
				if (target.getMob() != null)
					verifmobs(fight, target, 94, (-finalDommage));
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, target);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else {
			for (Peleador target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
			}
		}
	}

	private void applyEffect_95(ArrayList<Peleador> cibles, Pelea fight,
								boolean isCaC) {
		if (caster.isHide())
			caster.unHide(spell);
		if (isCaC)//CaC Eau
		{
			for (Peleador target : cibles) {
				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}

				int dmg = Formulas.getRandomJet(args.split(";")[5]);
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_NEUTRE, dmg, false, true, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_NEUTRE);//S'il y a des buffs sp�ciaux

				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);

				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);
				int heal = (int) (-finalDommage) / 2;
				if ((caster.getPdv() + heal) > caster.getPdvMax())
					heal = caster.getPdvMax() - caster.getPdv();
				caster.removePdv(caster, -heal);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
						+ "", caster.getId() + "," + heal);
				if (target.getMob() != null)
					verifmobs(fight, target, 95, (-finalDommage));
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else if (turns <= 0) {
			for (Peleador target : cibles) {

				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}
				//si la cible a le buff renvoie de sort
				if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl
						&& spell != 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId()
							+ "", target.getId() + ",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_NEUTRE, dmg, false, false, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_NEUTRE);//S'il y a des buffs sp�ciaux
				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);

				int heal = (int) (-finalDommage) / 2;
				if ((caster.getPdv() + heal) > caster.getPdvMax())
					heal = caster.getPdvMax() - caster.getPdv();
				caster.removePdv(caster, -heal);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
						+ "", caster.getId() + "," + heal);
				if (target.getMob() != null)
					verifmobs(fight, target, 95, (-finalDommage));
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, target);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else {
			for (Peleador target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
			}
		}
	}

	private void applyEffect_96(ArrayList<Peleador> cibles, Pelea fight,
								boolean isCaC)//dmg eau
	{
		if (isCaC)//CaC Eau
		{
			if (caster.isHide())
				caster.unHide(spell);
			for (Peleador target : cibles) {
				if (caster.isMob() && (caster.getTeam2() == target.getTeam2())
						&& !caster.isInvocation())
					continue; // Les monstres de s'entretuent pas
				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}

				int dmg = Formulas.getRandomJet(args.split(";")[5]);

				//Si le sort est boost� par un buff sp�cifique
				for (EfectoHechizo SE : caster.getBuffsByEffectID(293)) {
					if (SE.getValue() == spell) {
						int add = -1;
						try {
							add = Integer.parseInt(SE.getArgs().split(";")[2]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (add <= 0)
							continue;
						dmg += add;
					}
				}
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_EAU, dmg, false, true, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_EAU);//S'il y a des buffs sp�ciaux

				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				int cura = finalDommage;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura);
				}
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);
				if (target.getMob() != null)
					verifmobs(fight, target, 96, cura);
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else if (turns <= 0) {
			if (caster.isHide())
				caster.unHide(spell);
			for (Peleador target : cibles) {
				if (caster.isMob() && (caster.getTeam2() == target.getTeam2())
						&& !caster.isInvocation())
					continue; // Les monstres de s'entretuent pas

				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}
				//si la cible a le buff renvoie de sort
				if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl
						&& spell != 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId()
							+ "", target.getId() + ",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);

				//Si le sort est boost� par un buff sp�cifique
				for (EfectoHechizo SE : caster.getBuffsByEffectID(293)) {
					if (SE.getValue() == spell) {
						int add = -1;
						try {
							add = Integer.parseInt(SE.getArgs().split(";")[2]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (add <= 0)
							continue;
						dmg += add;
					}
				}

				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_EAU, dmg, false, false, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_EAU);//S'il y a des buffs sp�ciaux

				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				int cura = finalDommage;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura);
				}
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);
				if (target.getMob() != null)
					verifmobs(fight, target, 96, cura);
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else {
			for (Peleador target : cibles)
				target.addBuff(effectID, 0, turns, 1, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_97(ArrayList<Peleador> cibles, Pelea fight, boolean isCaC) {
		if (isCaC) {
			if (this.caster.isHide()) {
				this.caster.unHide(this.spell);
			}
			Iterator<Peleador> iterator = cibles.iterator();
			while (iterator.hasNext()) {
				Peleador target = iterator.next();
				if (this.caster.isMob() && this.spell != 946 && this.caster.getTeam2() == target.getTeam2() && !this.caster.isInvocation()) continue;
				if (target.hasBuff(765) && target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead()) {
					this.applyEffect_765B(fight, target);
					target = target.getBuff(765).getCaster();
				}
				int dmg = Formulas.getRandomJet(this.caster, target, this.args.split(";")[5]);
				for (EfectoHechizo SE : this.caster.getBuffsByEffectID(293)) {
					if (SE.getValue() != this.spell) continue;
					int add = -1;
					try {
						add = Integer.parseInt(SE.getArgs().split(";")[2]);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					if (add <= 0) continue;
					dmg += add;
				}
				int finalDommage = Formulas.calculFinalDommage(fight, this.caster, target, 1, dmg, false, true, this.spell);
				if ((finalDommage = applyOnHitBuffs(finalDommage, target, this.caster, fight, 1)) > target.getPdv()) {
					finalDommage = target.getPdv();
				}
				target.removePdv(this.caster, finalDommage);
				this.checkLifeTree(target, finalDommage);
				finalDommage = - finalDommage;
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, "" + this.caster.getId() + "", "" + target.getId() + "," + finalDommage);
				if (target.getMob() != null) {
					this.verifmobs(fight, target, 97, finalDommage);
				}
				if (target.getPdv() > 0) continue;
				fight.onFighterDie(target, this.caster);
				if (target.canPlay() && target.getPlayer() != null) {
					fight.endTurn(false);
					continue;
				}
				if (!target.canPlay()) continue;
				target.setCanPlay(false);
			}
		} else if (this.turns <= 0) {
			if (this.caster.isHide()) {
				this.caster.unHide(this.spell);
			}
			Iterator<Peleador> iterator = cibles.iterator();
			while (iterator.hasNext()) {
				Peleador target = iterator.next();
				if (this.spell != 946 && this.caster.isMob() && this.caster.getTeam2() == target.getTeam2() && !this.caster.isInvocation()) continue;
				if (target.hasBuff(765) && target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead()) {
					this.applyEffect_765B(fight, target);
					target = target.getBuff(765).getCaster();
				}
				if (target.hasBuff(106) && target.getBuffValue(106) >= this.spellLvl && this.spell != 0 && !this.isPoison()) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, "" + target.getId() + "", "" + target.getId() + ",1");
					target = this.caster;
				}
				int dmg = Formulas.getRandomJet(this.caster, target, this.args.split(";")[5]);
				if (this.caster.hasBuff(293) || this.caster.haveState(300)) {
					if (this.caster.haveState(300)) {
						this.caster.setState(300, 0, caster.getId());
					}
					for (EfectoHechizo SE : this.caster.getBuffsByEffectID(293)) {
						if (SE == null || SE.getValue() != this.spell) continue;
						int add = -1;
						try {
							add = Integer.parseInt(SE.getArgs().split(";")[2]);
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						if (add <= 0) continue;
						dmg += add;
					}
				}
				int finalDommage = Formulas.calculFinalDommage(fight, this.caster, target, 1, dmg, false, false, this.spell);
				if ((finalDommage = applyOnHitBuffs(finalDommage, target, this.caster, fight, 1)) > target.getPdv()) {
					finalDommage = target.getPdv();
				}
				target.removePdv(this.caster, finalDommage);
				this.checkLifeTree(target, finalDommage);
				finalDommage = - finalDommage;
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, "" + this.caster.getId() + "", "" + target.getId() + "," + finalDommage);
				if (target.getMob() != null) {
					this.verifmobs(fight, target, 97, finalDommage);
				}
				if (target.getPdv() > 0) continue;
				fight.onFighterDie(target, this.caster);
				if (target.trapped) continue;
				if (target.canPlay() && target.getPlayer() != null) {
					fight.endTurn(false);
					continue;
				}
				if (!target.canPlay()) continue;
				target.setCanPlay(false);
			}
		} else {
			if (this.spell == 470) {
				for (Peleador target : cibles) {
					if (target.getTeam() == this.caster.getTeam()) continue;
					target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);
				}
			}
			for (Peleador target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);
			}
		}
	}

	private void applyEffect_98(ArrayList<Peleador> cibles, Pelea fight, boolean isCaC)//dmg air
	{
		if (isCaC)//CaC Air
		{
			if (caster.isHide())
				caster.unHide(spell);
			for (Peleador target : cibles) {
				if (caster.isMob() && (caster.getTeam2() == target.getTeam2())
						&& !caster.isInvocation())
					continue; // Les monstres de s'entretuent pas
				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}

				int dmg = Formulas.getRandomJet(args.split(";")[5]);

				//Si le sort est boost� par un buff sp�cifique
				for (EfectoHechizo SE : caster.getBuffsByEffectID(293)) {
					if (SE.getValue() == spell) {
						int add = -1;
						try {
							add = Integer.parseInt(SE.getArgs().split(";")[2]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (add <= 0)
							continue;
						dmg += add;
					}
				}

				// applyEffect_142

				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_AIR, dmg, false, true, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_AIR);//S'il y a des buffs sp�ciaux

				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				int cura = finalDommage;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura);
				}
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);
				if (target.getMob() != null)
					verifmobs(fight, target, 98, cura);
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else if (turns <= 0) {
			if (caster.isHide())
				caster.unHide(spell);
			for (Peleador target : cibles) {
				if (caster.isMob() && (caster.getTeam2() == target.getTeam2())
						&& !caster.isInvocation())
					continue; // Les monstres de s'entretuent pas

				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}
				//si la cible a le buff renvoie de sort
				if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl
						&& spell != 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId()
							+ "", target.getId() + ",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);

				//Si le sort est boost� par un buff sp�cifique
				for (EfectoHechizo SE : caster.getBuffsByEffectID(293)) {
					if (SE.getValue() == spell) {
						int add = -1;
						try {
							add = Integer.parseInt(SE.getArgs().split(";")[2]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (add <= 0)
							continue;
						dmg += add;
					}
				}

				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_AIR, dmg, false, false, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_AIR);//S'il y a des buffs sp�ciaux

				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir

				target.removePdv(caster, finalDommage);
				int cura = finalDommage;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura);
				}
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);
				if (target.getMob() != null)
					verifmobs(fight, target, 98, cura);

				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else {
			for (Peleador target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
			}
		}
	}

	private void applyEffect_99(ArrayList<Peleador> cibles, Pelea fight,
								boolean isCaC)//dmg feu
	{
		if (caster.isHide())
			caster.unHide(spell);

		if (isCaC)//CaC Feu
		{
			for (Peleador target : cibles) {
				if (caster.isMob() && (caster.getTeam2() == target.getTeam2())
						&& !caster.isInvocation())
					continue; // Les monstres de s'entretuent pas
				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}

				int dmg = Formulas.getRandomJet(args.split(";")[5]);

				//Si le sort est boost� par un buff sp�cifique
				for (EfectoHechizo SE : caster.getBuffsByEffectID(293)) {
					if (SE.getValue() == spell) {
						int add = -1;
						try {
							add = Integer.parseInt(SE.getArgs().split(";")[2]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (add <= 0)
							continue;
						dmg += add;
					}
				}
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_FEU, dmg, false, true, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_FEU);//S'il y a des buffs sp�ciaux

				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				int cura = finalDommage;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura);
				}
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);
				if (target.getMob() != null)
					verifmobs(fight, target, 99, cura);
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else if (turns <= 0) {
			for (Peleador target : cibles) {
				if (caster.isMob() && (caster.getTeam2() == target.getTeam2())
						&& !caster.isInvocation())
					continue; // Les monstres de s'entretuent pas
				if (spell == 36 && target == caster)//Frappe du Craqueleur ne tape pas l'osa
				{
					continue;
				}

				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}
				//si la cible a le buff renvoie de sort
				if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl
						&& spell != 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId()
							+ "", target.getId() + ",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);

				//Si le sort est boost� par un buff sp�cifique
				for (EfectoHechizo SE : caster.getBuffsByEffectID(293)) {
					if (SE.getValue() == spell) {
						int add = -1;
						try {
							add = Integer.parseInt(SE.getArgs().split(";")[2]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (add <= 0)
							continue;
						dmg += add;
					}
				}

				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_FEU, dmg, false, false, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_FEU);//S'il y a des buffs sp�ciaux

				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				int cura = finalDommage;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura);
				}
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);
				if (target.getMob() != null)
					verifmobs(fight, target, 99, cura);
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else {
			for (Peleador target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
			}
		}
	}

	private void applyEffect_100(ArrayList<Peleador> cibles, Pelea fight,
								 boolean isCaC) {
		if (caster.isHide())
			caster.unHide(spell);
		if (fight.getType() == 7)
			return;
		if (isCaC)//CaC Neutre
		{
			for (Peleador target : cibles) {
				if (caster.isMob() && (caster.getTeam2() == target.getTeam2())
						&& !caster.isInvocation())
					continue; // Les monstres de s'entretuent pas
				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}

				int dmg = Formulas.getRandomJet(args.split(";")[5]);

				//Si le sort est boost� par un buff sp�cifique
				for (EfectoHechizo SE : caster.getBuffsByEffectID(293)) {
					if (SE.getValue() == spell) {
						int add = -1;
						try {
							add = Integer.parseInt(SE.getArgs().split(";")[2]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (add <= 0)
							continue;
						dmg += add;
					}
				}
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_NEUTRE, dmg, false, true, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_NEUTRE);//S'il y a des buffs sp�ciaux

				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				int cura = finalDommage;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura);
				}
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);
				if (target.getMob() != null)
					verifmobs(fight, target, 100, (-finalDommage));
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else if (turns <= 0) {
			for (Peleador target : cibles) {
				if (caster.isMob() && (caster.getTeam2() == target.getTeam2())
						&& !caster.isInvocation())
					continue; // Les monstres de s'entretuent pas

				if (target.hasBuff(765))//sacrifice
				{
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}
				//si la cible a le buff renvoie de sort
				if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl
						&& spell != 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId()
							+ "", target.getId() + ",1"); // le lanceur devient donc la cible
					target = caster;
				}
				if (spell == 2000 && caster.isInvocation()) {
					target.setState(7, 1, target.getId());
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
							+ "", target.getId() + "," + 7 + ",1");
					target.addBuff(950, 1, 1, 1, false, spell, args, target, true);
				}

				int dmg = Formulas.getRandomJet(args.split(";")[5]);

				//Si le sort est boost� par un buff sp�cifique
				for (EfectoHechizo SE : caster.getBuffsByEffectID(293)) {
					if (SE.getValue() == spell) {
						int add = -1;
						try {
							add = Integer.parseInt(SE.getArgs().split(";")[2]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (add <= 0)
							continue;
						dmg += add;
					}
				}

				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constantes.ELEMENT_NEUTRE, dmg, false, false, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constantes.ELEMENT_NEUTRE);//S'il y a des buffs sp�ciaux

				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				int cura = finalDommage;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura);
				}
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage);
				if (target.getMob() != null)
					verifmobs(fight, target, 100, (-finalDommage));
				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, caster);
					if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);
				}
			}
		} else {
			for (Peleador target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
			}
		}
	}

	private void applyEffect_101(ArrayList<Peleador> cibles, Pelea fight) {

		if (spell == 470) {
			for (Peleador target : cibles) {
				if (target.getTeam() == caster.getTeam())
					continue;

				if (target.hasBuff(788)) {
					if (target.getBuff(788) != null)
						if (target.getBuff(788).getValue() == 101) {
							EfectoHechizo SE = target.getBuff(788);
							target.addBuff(111, value, SE.getDurationFixed(), 0, true, target.getBuff(788).getSpell(), args, target, true);
							GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 101, target.getId()
									+ "", target.getId() + ",+" + value);
						}
				}
				int retrait = Formulas.getPointsLost('a', value, caster, target);
				if ((value - retrait) > 0)
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 308, caster.getId()
							+ "", target.getId() + "," + (value - retrait));
				if (retrait > 0) {
					target.addBuff(effectID, retrait, 1, 1, false, spell, args, caster, true);//m
					if (turns <= 1 || duration <= 1)
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 101, target.getId()
								+ "", target.getId() + ",-" + retrait);
				}

				if (fight.getFighterByOrdreJeu() == target)
					fight.setCurFighterPa(fight.getCurFighterPa() - retrait);

				if (target.getMob() != null) {
					if (target.getMob().getTemplate().getId() == 1071)// si Rasboul
					{
						target.addBuff(111, value, turns, 1, true, spell, args, target, true);
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 111, target.getId()
								+ "", target.getId() + ",+" + value);
					}
				}
			}
			return;
		}
		if (turns <= 0) {
			for (Peleador target : cibles) {
				if (target.hasBuff(788)) {
					if (target.getBuff(788) != null)
						if (target.getBuff(788).getValue() == 101) {
							target.addBuff(111, value, turns, 1, true, target.getBuff(788).getSpell(), args, target, true);
							GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 101, target.getId() + "", target.getId() + ",+" + value);
						}
				}
				int remove = Formulas.getPointsLost('a', value, caster, target);
				if ((value - remove) > 0)
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 308, caster.getId() + "", target.getId() + "," + (value - remove));
				if (remove > 0) {
					target.addBuff(Constantes.STATS_REM_PA, remove, 1, 1, false, spell, args, caster, true);
					if (turns <= 1 || duration <= 1)
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 101, target.getId() + "", target.getId() + ",-" + remove);
				}

				if (fight.getFighterByOrdreJeu() == target)
					fight.setCurFighterPa(fight.getCurFighterPa() - remove);

				if (target.getMob() != null)
					this.verifmobs(fight, target, 101, 0);
			}
		} else {
			if (cibles.size() > 0)
				for (Peleador target : cibles) {
					if (target.hasBuff(788)) {
						if (target.getBuff(788) != null)
							if (target.getBuff(788).getValue() == 101) {
								EfectoHechizo SE = target.getBuff(788);
								target.addBuff(111, value, SE.getDurationFixed(), 0, true, target.getBuff(788).getSpell(), args, target, true);
								GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 101, target.getId() + "", target.getId() + ",+" + value);
							}
					}
					int retrait = Formulas.getPointsLost('a', value, caster, target);
					if ((value - retrait) > 0)
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 308, caster.getId() + "", target.getId() + "," + (value - retrait));
					if (retrait > 0) {
						target.addBuff(effectID, retrait, 1, 1, false, spell, args, caster, true);//m
						if (turns <= 1 || duration <= 1)
							GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 101, target.getId() + "", target.getId() + ",-" + retrait);
					}

					if (fight.getFighterByOrdreJeu() == target)
						fight.setCurFighterPa(fight.getCurFighterPa() - retrait);

					if (target.getMob() != null) {
						if (target.getMob().getTemplate().getId() == 1071)// si Rasboul
						{
							target.addBuff(111, value, turns, 1, true, spell, args, target, true);
							GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 111, target.getId() + "", target.getId() + ",+" + value);
						}
					}
				}
		}


	}

	private void applyEffect_105(ArrayList<Peleador> cibles, Pelea fight) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
		}
	}

	private void applyEffect_106(ArrayList<Peleador> cibles, Pelea fight) {
		int val = -1;
		try {
			val = Integer.parseInt(args.split(";")[1]);//Niveau de sort max
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (val == -1)
			return;

		this.duration = turns;
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
		}
	}

	private void applyEffect_107(ArrayList<Peleador> cibles, Pelea fight) {
		if (turns < 1)
			return;//Je vois pas comment, vraiment ...
		else {
			for (Peleador target : cibles)
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_108(ArrayList<Peleador> cibles, Pelea fight, boolean isCaC) {// healcion
		if (spell == 441) return;
		if (isCaC) return;
		if (turns <= 0) {
			String[] jet = args.split(";");
			int heal = 0;
			if (jet.length < 6) {
				heal = 1;
			} else {
				heal = Formulas.getRandomJet(jet[5]);
			}
			int heal2 = heal;
			for (Peleador cible : cibles) {
				if (cible.isDead())
					continue;
				if (caster.hasBuff(178))
					heal += caster.getBuffValue(178);
				if (caster.hasBuff(179))
					heal = heal - caster.getBuffValue(179);
				heal = getMaxMinSpell(cible, heal);
				int pdvMax = cible.getPdvMax();
				int healFinal = Formulas.calculFinalHealCac(caster, heal, isCaC);
				if ((healFinal + cible.getPdv()) > pdvMax)
					healFinal = pdvMax - cible.getPdv();
				if (healFinal < 1)
					healFinal = 0;
				cible.removePdv(caster, -healFinal);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 108, caster.getId() + "", cible.getId() + "," + healFinal);
				heal = heal2;
			}
		} else {
			cibles.stream().filter(target -> !target.isDead()).forEach(target -> target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true));
		}
	}

	private void applyEffect_109(Pelea fight)//Dommage pour le lanceur (fixes)
	{
		if (turns <= 0) {
			int dmg = Formulas.getRandomJet(args.split(";")[5]);
			int finalDommage = Formulas.calculFinalDommage(fight, caster, caster, Constantes.ELEMENT_NULL, dmg, false, false, spell);

			finalDommage = applyOnHitBuffs(finalDommage, caster, caster, fight, Constantes.ELEMENT_NULL);//S'il y a des buffs sp�ciaux
			if (finalDommage > caster.getPdv())
				finalDommage = caster.getPdv();//Caster va mourrir
			caster.removePdv(caster, finalDommage);
			finalDommage = -(finalDommage);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
					+ "", caster.getId() + "," + finalDommage);

			if (caster.getPdv() <= 0) {
				fight.onFighterDie(caster, caster);

			}
		} else {
			caster.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_110(ArrayList<Peleador> cibles, Pelea fight) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_111(ArrayList<Peleador> cibles, Pelea fight) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		boolean repetibles = false;
		int lostPA = 0;
		for (Peleador target : cibles) {
			if (this.spell == 89 && target.getTeam() != this.caster.getTeam() || this.spell == 101 && target != this.caster) continue;
			if (this.spell == 115) {
				if (!repetibles) {
					lostPA = Formulas.getRandomJet(this.caster, target, this.jet);
					if (lostPA == -1) continue;
					this.value = lostPA;
				}
				target.addBuff(effectID, value, turns, turns, true, spell, args, caster, true);
				repetibles = true;
				if (target.canPlay() && target == this.caster) {
					target.setCurPa(fight, this.value);
				}
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, this.effectID, "" + this.caster.getId() + "", "" + target.getId() + "," + this.value + "," + this.turns);
				continue;
			}

			if (this.spell == 101) {
				this.turns = 0;
			}
			if (this.spell == 521 && target.getTeam2() != this.caster.getTeam2()) continue;
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			//Gain de PA pendant le tour de jeu
			if (target.canPlay() && target == caster)
				target.setCurPa(fight, val);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_112(ArrayList<Peleador> cibles, Pelea fight) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		if (duration < 1)
			duration = 1;
		if (spell == 1090) {
			caster.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", caster.getId() + "," + val + "," + turns);
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_114(ArrayList<Peleador> cibles, Pelea fight) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		boolean modif = false;
		/* (spell) {
			case 521:
				for (Fighter target : cibles) {
					if (target.getTeam2() != caster.getTeam2())
						continue;
					target.addBuff(effectID, 2, turns, 1, true, spell, args, caster, true);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
							+ "", target.getId() + "," + 2 + "," + turns);
				}
				modif = true;
				break;
			case 542:
				for (Fighter target : cibles) {
					target.addBuff(effectID, 2, turns, 1, true, spell, args, caster, true);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
							+ "", target.getId() + "," + 2 + "," + turns);
				}
				modif = true;
				break;
		}*/

		if (!modif)
			for (Peleador target : cibles) {
				target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
						+ "", target.getId() + "," + val + "," + turns);
			}
	}

	private void applyEffect_115(ArrayList<Peleador> cibles, Pelea fight) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_116(ArrayList<Peleador> cibles, Pelea fight)//Malus PO
	{
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_117(ArrayList<Peleador> cibles, Pelea fight)//Bonus PO
	{
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
			//Gain de PO pendant le tour de jeu
			if (target.canPlay() && target == caster)
				target.getTotalStats().addOneStat(Constantes.STATS_ADD_PO, val);
		}
	}

	private void applyEffect_118(ArrayList<Peleador> cibles, Pelea fight)//Bonus Force
	{
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		if (spell == 52)//cupiditer
			cibles = fight.getFighters(3);

		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId() + "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_119(ArrayList<Peleador> cibles, Pelea fight)//Bonus Agilit�
	{
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_120(ArrayList<Peleador> cibles, Pelea fight)//Bonus PA
	{
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		caster.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
		caster.setCurPa(fight, val);
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
				+ "", caster.getId() + "," + val + "," + turns);
	}

	private void applyEffect_121(ArrayList<Peleador> cibles, Pelea fight) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_122(ArrayList<Peleador> cibles, Pelea fight) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_123(ArrayList<Peleador> cibles, Pelea fight) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_124(ArrayList<Peleador> cibles, Pelea fight) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_125(ArrayList<Peleador> cibles, Pelea fight) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) return;



		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_126(ArrayList<Peleador> cibles, Pelea fight) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		if (spell == 52)//cupiditer
			cibles = fight.getFighters(3);

		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_127(ArrayList<Peleador> cibles, Pelea fight) {
		if (this.spell == 907) {
			for (Peleador target : cibles) {
				final int remove = Formulas.getPointsLost('m', value, caster, target);
				if ((value - remove) > 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 309, caster.getId()
							+ "", target.getId() + "," + (value - remove));
				}
				target.setCurPm(fight, -remove);
				if (remove > 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 127, target.getId() + "", target.getId() + ",-" + remove);
					if (target.getMob() != null) this.verifmobs(fight, target, 127, 0);
				}
			}
		} else if (turns <= 0) {
			for (Peleador target : cibles) {
				int retrait = Formulas.getPointsLost('m', value, caster, target);
				if ((value - retrait) > 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 309, caster.getId()
							+ "", target.getId() + "," + (value - retrait));
				}
				if (retrait > 0) {
					target.addBuff(Constantes.STATS_REM_PM, retrait, 1, 1, false, spell, args, caster, true);
					if (turns <= 1 || duration <= 1)
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 127, target.getId()
								+ "", target.getId() + ",-" + retrait);
					if (target.getMob() != null)
						this.verifmobs(fight, target, 127, 0);
				}

			}
		} else {
			for (Peleador target : cibles) {
				int retrait = Formulas.getPointsLost('m', value, caster, target);
				if ((value - retrait) > 0)
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 309, caster.getId()
							+ "", target.getId() + "," + (value - retrait));
				if (retrait > 0) {
					if (spell == 136)//Mot d'immobilisation
					{
						target.addBuff(effectID, retrait, turns, turns, false, spell, args, caster, true);
					} else {
						target.addBuff(effectID, retrait, 1, 1, false, spell, args, caster, true);
					}
					if (turns <= 1 || duration <= 1)
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 127, target.getId()
								+ "", target.getId() + ",-" + retrait);
				}
				if (retrait > 0)
					if (target.getMob() != null)
						this.verifmobs(fight, target, 127, 0);
			}
		}
	}

	private void applyEffect_128(ArrayList<Peleador> cibles, Pelea fight) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		boolean repetibles = false;
		int lostPM = 0;
		for (Peleador target : cibles) {
			if (spell == 115) {// odorat
				if (!repetibles) {
					lostPM = Formulas.getRandomJet(jet);
					if (lostPM == -1)
						continue;
					value = lostPM;
				}
				target.addBuff(effectID, value, turns, turns, true, spell, args, caster, true);
				repetibles = true;
				if (target.canPlay() && target == caster)
					target.setCurPm(fight, val);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
						+ "", target.getId() + "," + val + "," + turns);
				continue;
			} else if (spell == 521)// ruse kistoune
				if (target.getTeam2() != caster.getTeam2())
					continue;

			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			//Gain de PM pendant le tour de jeu
			if (target.canPlay() && target == caster)
				target.setCurPm(fight, val);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_130(Pelea fight, ArrayList<Peleador> cibles) {
		if (turns <= 0) {
			for (Peleador target : cibles) {
				int kamas = Formulas.getRandomJet(args.split(";")[5]);
				if (caster.getPlayer() == null) break;
				if (target.getPlayer() != null) {
					target.getPlayer().addKamas(-kamas);
					if (target.getPlayer().getKamas() < 0)
						target.getPlayer().setKamas(0);
				}
				if (target.getMob() != null) {
                    if (target.getMob().getTemplate().getId() == 494) {
                    } else {
                        caster.getPlayer().addKamas(kamas);
                    }
				} else {
					caster.getPlayer().addKamas(kamas);
				}

				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 130, caster.getId() + "", kamas + "");
			}
		} else {
			for (Peleador target : cibles)
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_131(ArrayList<Peleador> cibles, Pelea fight) {
		for (Peleador target : cibles) {
			target.addBuff(effectID, value, turns, 1, true, spell, args, caster, true);
		}
	}

	private void applyEffect_132(ArrayList<Peleador> cibles, Pelea fight) {
		for (Peleador target : cibles) {
			target.debuff();
			if (target.isHide()) target.unHide(spell);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 132, caster.getId() + "", target.getId() + "");
		}
	}

	private void applyEffect_138(ArrayList<Peleador> cibles, Pelea fight) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_140(ArrayList<Peleador> cibles, Pelea fight) {
		for (Peleador target : cibles) {
			target.addBuff(effectID, 0, 1, 0, true, spell, args, caster, true);
		}
	}

	private void applyEffect_141(Pelea fight, ArrayList<Peleador> cibles) {
		for (Peleador target : cibles) {
			if (target.hasBuff(765))//sacrifice
			{
				if (target.getBuff(765) != null
						&& !target.getBuff(765).getCaster().isDead()) {
					applyEffect_765B(fight, target);
					target = target.getBuff(765).getCaster();
				}
			}
			fight.onFighterDie(target, target);
		}
	}

	private void applyEffect_142(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_143(ArrayList<Peleador> cibles, Pelea fight) {
		if (spell == 470) {
			String[] jet = args.split(";");
			int heal = 0;
			if (jet.length < 6) {
				heal = 1;
			} else {
				heal = Formulas.getRandomJet(jet[5]);
			}
			int dmg2 = heal;
			for (Peleador cible : cibles) {
				if (cible.getTeam() != caster.getTeam())
					continue;
				if (cible.isDead())
					continue;
				heal = getMaxMinSpell(cible, heal);
				int healFinal = Formulas.calculFinalHealCac(caster, heal, false);
				if (spell == 450) {
					healFinal = heal;
				}
				if ((healFinal + cible.getPdv()) > cible.getPdvMax())
					healFinal = cible.getPdvMax() - cible.getPdv();
				if (healFinal < 1)
					healFinal = 0;
				cible.removePdv(caster, -healFinal);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 108, caster.getId()
						+ "", cible.getId() + "," + healFinal);
				heal = dmg2;
			}
			return;
		}
		if (turns <= 0) {
			String[] jet = args.split(";");
			int heal = 0;
			if (jet.length < 6) {
				heal = 1;
			} else {
				heal = Formulas.getRandomJet(jet[5]);
			}
			int dmg2 = heal;
			for (Peleador cible : cibles) {
				if (cible.isDead())
					continue;
				heal = getMaxMinSpell(cible, heal);
				int healFinal = Formulas.calculFinalHealCac(caster, heal, false);
				if (spell == 450) {
					healFinal = heal;
				}
				if ((healFinal + cible.getPdv()) > cible.getPdvMax())
					healFinal = cible.getPdvMax() - cible.getPdv();
				if (healFinal < 1)
					healFinal = 0;
				cible.removePdv(caster, -healFinal);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 108, caster.getId()
						+ "", cible.getId() + "," + healFinal);
				heal = dmg2;
			}
		} else {
			for (Peleador cible : cibles) {
				if (cible.isDead())
					continue;
				cible.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);
			}
		}
	}

	private void applyEffect_144(Pelea pelea, ArrayList<Peleador> objetivos) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1)
			return;
		int val2 = val;
		for (Peleador objetivo : objetivos) {
			val = getMaxMinSpell(objetivo, val);
			objetivo.addBuff(145, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(pelea, 7, 145, caster.getId()
					+ "", objetivo.getId() + "," + val + "," + turns);
			val = val2;
		}
	}

	private void applyEffect_145(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_149(Pelea fight, ArrayList<Peleador> cibles) {
		int id = -1;

		try {
			id = Integer.parseInt(args.split(";")[2]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (Peleador target : cibles) {
			if (target.isDead())
				continue;
			if (spell == 686)
				if (target.getPlayer() != null
						&& target.getPlayer().getSexe() == 1
						|| target.getMob() != null
						&& target.getMob().getTemplate().getId() == 547)
					id = 8011;
			if (id == -1)
				id = target.getDefaultGfx();

			target.addBuff(effectID, id, turns, 1, true, spell, args, caster, true);
			int defaut = target.getDefaultGfx();
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + defaut + "," + id + ","
					+ (target.canPlay() ? turns + 1 : turns));
		}
	}

	private void applyEffect_150(Pelea fight, ArrayList<Peleador> cibles) {
		if (turns == 0)
			return;

		if (spell == 547 || spell == 546 || spell == 548 || spell == 525) {
			caster.addBuff(effectID, 0, 3, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", caster.getId() + "," + (3 - 1));
			return;
		}

		for (Peleador target : cibles) {
			target.addBuff(effectID, 0, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId() + "", target.getId() + "," + (turns - 1));
		}
	}

	private void applyEffect_152(Pelea pelea, ArrayList<Peleador> objetivos) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1)
			return;
		int val2 = val;
		for (Peleador objetivo : objetivos) {
			val = getMaxMinSpell(objetivo, val);
			objetivo.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(pelea, 7, effectID, caster.getId()
					+ "", objetivo.getId() + "," + val + "," + turns);
			val = val2;
		}
	}

	private void applyEffect_153(Pelea pelea, ArrayList<Peleador> objetivos) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) return;

		int val2 = val;
		for (Peleador objetivo : objetivos) {
			val = getMaxMinSpell(objetivo, val);
			objetivo.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(pelea, 7, effectID, caster.getId()
					+ "", objetivo.getId() + "," + val + "," + turns);
			val = val2;
		}
	}

	private void applyEffect_154(Pelea pelea, ArrayList<Peleador> objetivos) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) return;

		int val2 = val;
		for (Peleador objetivo : objetivos) {
			val = getMaxMinSpell(objetivo, val);
			objetivo.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(pelea, 7, effectID, caster.getId()
					+ "", objetivo.getId() + "," + val + "," + turns);
			val = val2;
		}
	}

	private void applyEffect_155(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}


	private void applyEffect_156(Pelea pelea, ArrayList<Peleador> objetivos) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) return;

		int val2 = val;
		for (Peleador objetivo : objetivos) {
			val = getMaxMinSpell(objetivo, val);
			objetivo.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(pelea, 7, effectID, caster.getId()
					+ "", objetivo.getId() + "," + val + "," + turns);
			val = val2;
		}
	}

	private void applyEffect_157(Pelea pelea, ArrayList<Peleador> objetivos) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) return;

		int val2 = val;
		for (Peleador objetivo : objetivos) {
			val = getMaxMinSpell(objetivo, val);
			objetivo.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(pelea, 7, effectID, caster.getId()
					+ "", objetivo.getId() + "," + val + "," + turns);
			val = val2;
		}
	}

	private void applyEffect_160(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_161(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_162(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_163(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		if (cibles.isEmpty() && spell == 310 && caster.getOldCible() != null) {
			caster.getOldCible().addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getOldCible().getId()
					+ "", caster.getOldCible().getId() + "," + turns);
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_164(ArrayList<Peleador> objetivos, Pelea pelea) {
		int val = value;
		if (val == -1) return;

		for (Peleador objetivo : objetivos) {
			objetivo.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
		}
	}

	private void applyEffect_165(Pelea fight, ArrayList<Peleador> cibles) {
		int value = -1;
		try {
			value = Integer.parseInt(args.split(";")[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (value == -1)
			return;
		caster.addBuff(effectID, value, turns, 1, true, spell, args, caster, true);
	}

	private void applyEffect_168(ArrayList<Peleador> cibles, Pelea fight) {// - PA, no esquivables
		if (turns <= 0) {
			for (Peleador cible : cibles) {
				if (cible.isDead())
					continue;
				cible.addBuff(effectID, value, 1, 1, true, spell, args, caster, true);
				if (turns <= 1 || duration <= 1) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 168, cible.getId()
							+ "", cible.getId() + ",-" + value);
				}
				if (fight.getFighterByOrdreJeu() == cible)
					fight.setCurFighterPa(fight.getCurFighterPa() - value);
				if (cible.getMob() != null)
					verifmobs(fight, cible, 168, 0);
			}
		} else {
			boolean repetibles = false;
			int lostPA = 0;

			for (Peleador cible : cibles) {
				if (cible.isDead())
					continue;
				if (spell == 197 || spell == 112 ||spell == 630) { // potencia silvestre, garra - ceangal (critico)
					cible.addBuff(effectID, value, turns, turns, true, spell, args, caster, true);
				} else if (spell == 115) {// Odorat
					if (!repetibles) {
						lostPA = Formulas.getRandomJet(jet);
						if (lostPA == -1)
							continue;
						value = lostPA;
					}
					cible.addBuff(effectID, value, turns, turns, true, spell, args, caster, true);
					repetibles = true;
				} else {
					cible.addBuff(effectID, value, 1, 1, true, spell, args, caster, true);
				}
				if (turns <= 1 || duration <= 1)
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 168, cible.getId()
							+ "", cible.getId() + ",-" + value);

				if (fight.getFighterByOrdreJeu() == cible)
					fight.setCurFighterPa(fight.getCurFighterPa() - value);

				if (cible.getMob() != null)
					verifmobs(fight, cible, 168, 0);
			}
		}
	}

	private void applyEffect_169(ArrayList<Peleador> cibles, Pelea fight) { // - PM, no esquivables

		if (spell == 686 && caster.haveState(1))//anti bug saoul
			return;
		if (turns <= 0) {
			for (Peleador cible : cibles) {
				if (cible.isDead())
					continue;
				cible.addBuff(effectID, value, 1, 1, true, spell, args, caster, true);
				if (turns <= 1 || duration <= 1)
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 169, cible.getId() + "", cible.getId() + ",-" + value);
				if (cible.getMob() != null)
					verifmobs(fight, cible, 169, 0);
			}
		} else {
			if (cibles.isEmpty() && spell == 120 && caster.getOldCible() != null) {
				caster.getOldCible().addBuff(effectID, value, turns, 1, false, spell, args, caster, true);
				if (turns <= 1 || duration <= 1)
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 169, caster.getOldCible().getId() + "", caster.getOldCible().getId() + ",-" + value);
			}
			boolean repetibles = false;
			int lostPM = 0;
			for (Peleador cible : cibles) {
				if (cible.isDead())
					continue;
				if (spell == 192) {// zarza tranquilizadora
					cible.addBuff(effectID, value, turns, 0, true, spell, args, caster, true);
				} else if (spell == 115) {// odorat
					if (!repetibles) {
						lostPM = Formulas.getRandomJet(jet);
						if (lostPM == -1)
							continue;
						value = lostPM;
					}
					cible.addBuff(effectID, value, turns, turns, true, spell, args, caster, true);
					repetibles = true;
				} else if (spell == 197 || spell == 630) {// portencia sivelstre
					cible.addBuff(effectID, value, turns, turns, true, spell, args, caster, true);
				} else if (spell == 686) {// picole
					cible.addBuff(effectID, value, turns, turns, true, spell, args, caster, true);
				} else {
					cible.addBuff(effectID, value, 1, 1, true, spell, args, caster, true);
				}
				if (turns <= 1 || duration <= 1)
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 169, cible.getId() + "", cible.getId() + ",-" + value);
				if (cible.getMob() != null)
					verifmobs(fight, cible, 168, 0);
			}
		}
	}

	private void applyEffect_171(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId() + "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_176(ArrayList<Peleador> objetivos, Pelea pelea) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			return;
		}
		for (Peleador objetivo : objetivos) {
			objetivo.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(pelea, 7, Constantes.STATS_ADD_PROS, caster.getId()
					+ "", objetivo.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_177(ArrayList<Peleador> objetivos, Pelea pelea) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			return;
		}
		for (Peleador objetivo : objetivos) {
			objetivo.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(pelea, 7, Constantes.STATS_REM_PROS, caster.getId()
					+ "", objetivo.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_178(ArrayList<Peleador> objetivos, Pelea pelea) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			return;
		}
		int val2 = val;
		for (Peleador objetivo : objetivos) {
			val = getMaxMinSpell(objetivo, val);
			objetivo.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(pelea, 7, effectID, caster.getId()
					+ "", objetivo.getId() + "," + val + "," + turns);
			val = val2;
		}
	}

	private void applyEffect_179(ArrayList<Peleador> objetivos, Pelea pelea) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			return;
		}
		int val2 = val;
		for (Peleador objetivo : objetivos) {
			val = getMaxMinSpell(objetivo, val);
			objetivo.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(pelea, 7, effectID, caster.getId()
					+ "", objetivo.getId() + "," + val + "," + turns);
			val = val2;
		}
	}

	private void applyEffect_180(Pelea fight)//invocation
	{
		int cell = this.cell.getId();
		if (!this.cell.getFighters().isEmpty())
			return;
		int id = fight.getNextLowerFighterGuid();
		Jugador clone = Jugador.ClonePerso(caster.getPlayer(), -id - 10000, (caster.getPlayer().getMaxPdv() - ((caster.getLvl() - 1) * 5 + 50)));
		clone.setPelea(fight);

		Peleador fighter = new Peleador(fight, clone);
		fighter.fullPdv();
		fighter.setTeam(caster.getTeam());
		fighter.setInvocator(caster);

		fight.getMap().getCase(cell).addFighter(fighter);
		fighter.setCell(fight.getMap().getCase(cell));

		fight.getOrderPlaying().add((fight.getOrderPlaying().indexOf(caster) + 1), fighter);
		fight.addFighterInTeam(fighter, caster.getTeam());

		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 180, caster.getId() + "", fighter.getGmPacket('+', true).substring(3));
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", fight.getGTL());

		VerificarTrampa(fight, fighter);
	}

	private void applyEffect_181(Pelea fight)//invocation
	{
		int cell = this.cell.getId();

		if (!this.cell.getFighters().isEmpty())
			return;

		int id = -1, level = -1;

		try {
			String mobs = args.split(";")[0], levels = args.split(";")[1];

			if (mobs.contains(":")) {
				String[] split = mobs.split(":");
				id = Integer.parseInt(split[Formulas.getRandomValue(0, split.length - 1)]);
			} else {
				id = Integer.parseInt(mobs);
			}

			if (levels.contains(":")) {
				String[] split = levels.split(":");
				level = Integer.parseInt(split[Formulas.getRandomValue(0, split.length - 1)]);
			} else {
				level = Integer.parseInt(levels);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		MobGrade MG = null;
		Monstruos monster = Mundo.mundo.getMonstre(id);
		if(monster == null) return;
		try {
			MG = monster.getGradeByLevel(level);
			if(MG == null) {
				MG = monster.getRandomGrade();
				if(MG == null) return;
			}
			MG = MG.getCopy();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (id == -1 || level == -1 || MG == null)
			return;

		MG.setInFightID(fight.getNextLowerFighterGuid());
		if (caster.getPlayer() != null)
			MG.modifStatByInvocator(caster, id); // Augmenter les statistiques uniquement pour les invocations de personnages
		Peleador F = new Peleador(fight, MG);
		F.setTeam(caster.getTeam());
		F.setInvocator(caster);
		fight.getMap().getCase(cell).addFighter(F);
		F.setCell(fight.getMap().getCase(cell));
		fight.getOrderPlaying().add((fight.getOrderPlaying().indexOf(caster) + 1), F);
		fight.addFighterInTeam(F, caster.getTeam());
		String gm = F.getGmPacket('+', true).substring(3);
		String gtl = fight.getGTL();
		try {
			if(this.caster.getMob()!=null)
				Thread.sleep(1000);
		} catch(Exception e) {
			e.printStackTrace();
		}

		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,181,caster.getId()+"",gm);
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,caster.getId()+"",gtl);
		caster.nbrInvoc++;
		//Verificamos trampas - Eliminamos ArrayList anterior
		VerificarTrampa(fight,F);
	}

	private void applyEffect_182(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_183(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_184(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_185(Pelea fight) {
		int monster = -1, level = -1;

		try {
			monster = Integer.parseInt(args.split(";")[0]);
			level = Integer.parseInt(args.split(";")[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		MobGrade mobGrade;

		try {
			mobGrade = Mundo.mundo.getMonstre(monster).getGradeByLevel(level).getCopy();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		if (monster == -1 || level == -1 || mobGrade == null)
			return;
		if (monster == 556 && this.caster.getPlayer() != null)
			mobGrade.modifStatByInvocator(this.caster, 556);

		int id = fight.getNextLowerFighterGuid();
		mobGrade.setInFightID(id);

		Peleador fighter = new Peleador(fight, mobGrade);
		fighter.setTeam(this.caster.getTeam());
		fighter.setInvocator(this.caster);

		fight.getMap().getCase(this.cell.getId()).addFighter(fighter);
		fighter.setCell(fight.getMap().getCase(this.cell.getId()));
		fight.addFighterInTeam(fighter, this.caster.getTeam());
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 181, this.caster.getId() + "", fighter.getGmPacket('+', true).substring(3));
	}

	private void applyEffect_186(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		int val2 = val;
		for (Peleador f : cibles) {
			val = getMaxMinSpell(f, val);
			f.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", f.getId() + "," + val + "," + turns);
			val = val2;
		}
	}

	private void applyEffect_202(Pelea fight, ArrayList<Peleador> cibles) {
		if (spell == 113 || spell == 64) {
			// unhide des personnages
			for (Peleador target : cibles) {
				if (target.isHide()) {
					if (target != caster)
						target.unHide(spell);
				}
			}
			// unhide des pi�ges
			for (Trampas p : fight.getAllTraps()) {
				p.setIsUnHide(caster);
				p.aparecer(caster);
			}
		}
	}

	private void applyEffect_210(Pelea fight, ArrayList<Peleador> cibles) {
		if (spell == 686 && caster.haveState(1))//anti bug saoul
		{
			int pa = 1;
			if (this.spellLvl == 5)
				pa = 2;
			else if (this.spellLvl == 4)
				pa = 3;
			else if (this.spellLvl == 3 || this.spellLvl == 2)
				pa = 4;
			else if (this.spellLvl == 1)
				pa = 5;

			caster.addBuff(111, pa, -1, 1, true, spell, args, caster, true);
			//Gain de PA pendant le tour de jeu
			caster.setCurPa(fight, pa);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 111, caster.getId()
					+ "", caster.getId() + "," + pa + "," + -1);
			return;
		}
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_211(Pelea fight, ArrayList<Peleador> cibles) {
		if (spell == 686 && caster.haveState(1))//anti bug saoul
			return;
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_212(Pelea fight, ArrayList<Peleador> cibles) {
		if (spell == 686 && caster.haveState(1))//anti bug saoul
			return;
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_213(Pelea fight, ArrayList<Peleador> cibles) {
		if (spell == 686 && caster.haveState(1))//anti bug saoul
			return;
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_214(Pelea fight, ArrayList<Peleador> cibles) {
		if (spell == 686 && caster.haveState(1))//anti bug saoul
			return;
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}

	}

	private void applyEffect_215(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_216(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_217(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_218(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_219(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_220(ArrayList<Peleador> objetivos, Pelea pelea) {
		if (turns < 1)
			return;
		else {
			for (Peleador objetivo : objetivos)
				objetivo.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);
		}
	}

	private void applyEffect_265(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			JuegoServidor.a();
			return;
		}
		for (Peleador target : cibles) {
			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_266(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		int vol = 0;
		for (Peleador target : cibles) {
			target.addBuff(Constantes.STATS_REM_CHAN, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_REM_CHAN, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
			vol += val;
		}
		if (vol == 0)
			return;
		//on ajoute le buff
		caster.addBuff(Constantes.STATS_ADD_CHAN, vol, turns, 1, true, spell, args, caster, true);
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_ADD_CHAN, caster.getId()
				+ "", caster.getId() + "," + vol + "," + turns);
	}

	private void applyEffect_267(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		int vol = 0;
		for (Peleador target : cibles) {
			target.addBuff(Constantes.STATS_REM_VITA, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_REM_VITA, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
			vol += val;
		}
		if (vol == 0)
			return;
		//on ajoute le buff
		caster.addBuff(Constantes.STATS_ADD_VITA, vol, turns, 1, true, spell, args, caster, true);
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_ADD_VITA, caster.getId()
				+ "", caster.getId() + "," + vol + "," + turns);
	}

	private void applyEffect_268(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		int vol = 0;
		for (Peleador target : cibles) {
			target.addBuff(Constantes.STATS_REM_AGIL, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_REM_AGIL, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
			vol += val;
		}
		if (vol == 0)
			return;
		//on ajoute le buff
		caster.addBuff(Constantes.STATS_ADD_AGIL, vol, turns, 1, true, spell, args, caster, true);
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_ADD_AGIL, caster.getId()
				+ "", caster.getId() + "," + vol + "," + turns);
	}

	private void applyEffect_269(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		int vol = 0;
		for (Peleador target : cibles) {
			target.addBuff(Constantes.STATS_REM_INTE, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_REM_INTE, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
			vol += val;
		}
		if (vol == 0)
			return;
		//on ajoute le buff
		caster.addBuff(Constantes.STATS_ADD_INTE, vol, turns, 1, true, spell, args, caster, true);
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_ADD_INTE, caster.getId()
				+ "", caster.getId() + "," + vol + "," + turns);
	}

	private void applyEffect_270(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		int vol = 0;
		for (Peleador target : cibles) {
			target.addBuff(Constantes.STATS_REM_SAGE, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_REM_SAGE, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
			vol += val;
		}
		if (vol == 0)
			return;
		//on ajoute le buff
		caster.addBuff(Constantes.STATS_ADD_SAGE, vol, turns, 1, true, spell, args, caster, true);
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_ADD_SAGE, caster.getId()
				+ "", caster.getId() + "," + vol + "," + turns);
	}

	private void applyEffect_271(Pelea fight, ArrayList<Peleador> cibles) {
		int val = Formulas.getRandomJet(jet);
		int vol = 0;
		for (Peleador target : cibles) {
			target.addBuff(Constantes.STATS_REM_FORC, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_REM_FORC, caster.getId()
					+ "", target.getId() + "," + val + "," + turns);
			vol += val;
		}
		if (vol == 0)
			return;
		//on ajoute le buff
		caster.addBuff(Constantes.STATS_ADD_FORC, vol, turns, 1, true, spell, args, caster, true);
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_ADD_FORC, caster.getId()
				+ "", caster.getId() + "," + vol + "," + turns);
	}

	private void applyEffect_293(Pelea fight) {
		caster.addBuff(effectID, value, turns, 1, false, spell, args, caster, true);
		caster.setState(300, turns + 1, caster.getId());
	}

	private void applyEffect_320(Pelea fight, ArrayList<Peleador> cibles) {
		int value = 1;
		try {
			value = Integer.parseInt(args.split(";")[0]);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		int num = 0;
		for (Peleador target : cibles) {
			target.addBuff(Constantes.STATS_REM_PO, value, turns, 0, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_REM_PO, caster.getId()
					+ "", target.getId() + "," + value + "," + turns);
			num += value;
		}
		if (num != 0) {
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constantes.STATS_ADD_PO, caster.getId()
					+ "", caster.getId() + "," + num + "," + turns);
			caster.addBuff(Constantes.STATS_ADD_PO, num, 1, 0, true, spell, args, caster, true);
			//Gain de PO pendant le tour de jeu
			if (caster.canPlay())
				caster.getTotalStats().addOneStat(Constantes.STATS_ADD_PO, num);
		}
	}

	private void applyEffect_400(Pelea fight) {
		if (!cell.isWalkable(true))
			return;//Si case pas marchable
		if (cell.getFirstFighter() != null && !cell.getFirstFighter().isHide())
			return;//Si la case est prise par un joueur

		//Si la case est prise par le centre d'un piege
		for (Trampas p : fight.getAllTraps())
			if (p.getCelda().getId() == cell.getId())
				return;

		String[] infos = args.split(";");
		int spellID = Short.parseShort(infos[0]);
		int level = Byte.parseByte(infos[1]);
		String po = Mundo.mundo.getSort(spell).getStatsByLevel(spellLvl).getPorteeType();
		byte size = (byte) Mundo.mundo.getCryptManager().getIntByHashedValue(po.charAt(1));
		SortStats TS = Mundo.mundo.getSort(spellID).getStatsByLevel(level);
		Trampas g = new Trampas(fight, caster, cell, size, TS, spell);
		fight.getAllTraps().add(g);
		int unk = g.getColor();
		int team = caster.getTeam() + 1;

		StringBuilder str2 = new StringBuilder();

		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, team, 999, caster.getId() + "", "GDZ" + '+' + cell.getId() + ';' + size + ';' + unk);
		str2.append("GDC").append(cell.getId()).append(';').append("Haaaaaaaaz3005").append(';');
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, team, 999, caster.getId() + "", str2.toString());
	}

	private void applyEffect_401(Pelea fight) {
		if (!cell.isWalkable(false))
			return;//Si case pas marchable
		if (cell.getFirstFighter() != null)
			return;//Si la case est prise par un joueur

		String[] infos = args.split(";");
		int spellID = Short.parseShort(infos[0]);
		int level = Byte.parseByte(infos[1]);
		byte duration = Byte.parseByte(infos[3]);
		String po = Mundo.mundo.getSort(spell).getStatsByLevel(spellLvl).getPorteeType();
		byte size = (byte) Mundo.mundo.getCryptManager().getIntByHashedValue(po.charAt(1));
		SortStats TS = Mundo.mundo.getSort(spellID).getStatsByLevel(level);
		Grifos g = new Grifos(fight, caster, cell, size, TS, duration, spell);
		fight.getAllGlyphs().add(g);
		int unk = g.getColor();
		String str = "GDZ+" + cell.getId() + ";" + size + ";" + unk;
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", str);
		str = "GDC" + cell.getId() + ";Haaaaaaaaa3005;";
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", str);
	}

	private void applyEffect_402(Pelea fight) {
		if (!cell.isWalkable(true))
			return;//Si case pas marchable

		String[] infos = args.split(";");
		int spellID = Short.parseShort(infos[0]);
		int level = Byte.parseByte(infos[1]);
		byte duration = Byte.parseByte(infos[3]);
		String po = Mundo.mundo.getSort(spell).getStatsByLevel(spellLvl).getPorteeType();
		byte size = (byte) Mundo.mundo.getCryptManager().getIntByHashedValue(po.charAt(1));
		SortStats TS = Mundo.mundo.getSort(spellID).getStatsByLevel(level);
		Grifos g = new Grifos(fight, caster, cell, size, TS, duration, spell);
		fight.getAllGlyphs().add(g);
		int unk = g.getColor();
		String str = "GDZ+" + cell.getId() + ";" + size + ";" + unk;
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId()
				+ "", str);
		str = "GDC" + cell.getId() + ";Haaaaaaaaa3005;";
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId()
				+ "", str);
	}

	private void applyEffect_671(ArrayList<Peleador> cibles, Pelea fight) {
		if (turns <= 0) {
			for(Peleador target : cibles) {
				if (target.hasBuff(765)) {
					if (target.getBuff(765) != null
							&& !target.getBuff(765).getCaster().isDead()) {
						applyEffect_765B(fight, target);
						target = target.getBuff(765).getCaster();
					}
				}
				if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spell != 0) {
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId() + "", target.getId() + ",1");
					target = caster;
				}
				int resP = target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_NEU), resF = target.getTotalStats().getEffect(Constantes.STATS_ADD_R_NEU);

				if (target.getPlayer() != null) {
					resP += target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_PVP_NEU);
					resF += target.getTotalStats().getEffect(Constantes.STATS_ADD_R_PVP_NEU);
				}

				int dmg = Formulas.getRandomJet(args.split(";")[5]);// % de pdv
				dmg = getMaxMinSpell(target, dmg);
				int val = caster.getPdv() / 100 * dmg;// Valor de da�os
				val -= resF;
				int reduc = (int) (((float) val) / (float) 100) * resP;// Reduc
				// %resis
				val -= reduc;
				if (val < 0)
					val = 0;
				val = applyOnHitBuffs(val, target, caster, fight, Constantes.ELEMENT_NULL);
				if (val > target.getPdv())
					val = target.getPdv();
				target.removePdv(caster, val);
				int cura = val;
				if (target.hasBuff(786) && target.getBuff(786) != null) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura);
				}
				val = -(val);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + val);
				if (target.getPdv() <= 0)
					fight.getDeadList().remove(target);
			}
		} else {
			caster.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);
		}
	}

	private void applyEffect_672(ArrayList<Peleador> cibles, Pelea fight) {
		//Punition
		//Formule de barge ? :/ Clair que ca punie ceux qui veulent l'utiliser x_x
		double val = ((double) Formulas.getRandomJet(jet) / (double) 100);
		int pdvMax = caster.getPdvMaxOutFight();
		double pVie = (double) caster.getPdv() / (double) caster.getPdvMax();
		double rad = (double) 2 * Math.PI * (double) (pVie - 0.5);
		double cos = Math.cos(rad);
		double taux = (Math.pow((cos + 1), 2)) / (double) 4;
		double dgtMax = val * pdvMax;
		int dgt = (int) (taux * dgtMax);

		for (Peleador target : cibles) {

			if (target.hasBuff(765))//sacrifice
			{
				if (target.getBuff(765) != null
						&& !target.getBuff(765).getCaster().isDead()) {
					applyEffect_765B(fight, target);
					target = target.getBuff(765).getCaster();
				}
			}
			//si la cible a le buff renvoie de sort
			if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl) {
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId()
						+ "", target.getId() + ",1");
				//le lanceur devient donc la cible
				target = caster;
			}
			//Debug bonus por armas principales en razas
			int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constantes.ELEMENT_NEUTRE,dgt,false,true,spell);
			finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constantes.ELEMENT_NEUTRE); //S'il y a des buffs sp�ciaux

			int resi = target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_NEU);
			int retir = 0;
			if (resi > 2) {
				retir = (finalDommage * resi) / 100;
				finalDommage = finalDommage - retir;
			}
			if (resi < -2) {
				retir = ((-finalDommage) * (-resi)) / 100;
				finalDommage = finalDommage + retir;
			}
			if (finalDommage > target.getPdv())
				finalDommage = target.getPdv();//Target va mourrir
			target.removePdv(caster, finalDommage);
			target.removePdvMax((int)Math.floor(finalDommage*(Configuracion.INSTANCE.getErosion()+caster.getTotalStats().getEffect(Constantes.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constantes.STATS_REM_ERO)-target.getTotalStats().getEffect(Constantes.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constantes.STATS_REM_R_ERO)))/100);
			finalDommage=-(finalDommage);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId() + "", target.getId() + "," + finalDommage);

			if (target.getPdv() <= 0) {
				fight.onFighterDie(target, target);
				if (target.canPlay() && target.getPlayer() != null)
					fight.endTurn(false);
				else if (target.canPlay())
					target.setCanPlay(false);
			}
		}
	}

	private void applyEffect_765(ArrayList<Peleador> cibles, Pelea fight) {
		for (Peleador target : cibles) {
			target.addBuff(effectID, 0, turns, 1, true, spell, args, caster, true);
		}
	}

	private void applyEffect_765B(Pelea fight, Peleador target) {
		Peleador sacrified = target.getBuff(765).getCaster();
		//Peleador portador = caster.getHoldedBy();
		//Peleador objetivo = cell.getFirstFighter();

		GameCase cell1 = sacrified.getCell();
		GameCase cell2 = target.getCell();

		/*//Si el objetivos entra en estado transportado e intercambiamos lugares
		if (sacrified.haveState(Constantes.ETAT_PORTEUR) || sacrified.haveState(Constantes.ETAT_PORTE)) {

			target.setState(Constantes.ETAT_PORTE,-1,target.getId());
			sacrified.setState(Constantes.ETAT_PORTE, 0, sacrified.getId());

			//Enviamos los paquetes
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,target.getId()+"",target.getId()+","+Constantes.ETAT_PORTE+",1");
			}*/

		sacrified.getCell().getFighters().clear();
		target.getCell().getFighters().clear();
		sacrified.setCell(cell2);
		sacrified.getCell().addFighter(sacrified);
		target.setCell(cell1);
		target.getCell().addFighter(target);
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, target.getId() + "", target.getId() + "," + cell1.getId());
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, sacrified.getId() + "", sacrified.getId() + "," + cell2.getId());
	}

	private void applyEffect_776(ArrayList<Peleador> objetivos, Pelea pelea) {
		int val = Formulas.getRandomJet(jet);
		if (val == -1) {
			return;
		}
		for (Peleador objetivo : objetivos) {
			objetivo.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(pelea, 7, effectID, caster.getId()
					+ "", objetivo.getId() + "," + val + "," + turns);
		}
	}

	private void applyEffect_780(Pelea fight) {
		Peleador target = null;

		for(int i=fight.getDeadList().size()-1;i>=0;i--)
		{
			Peleador fighter=fight.getDeadList().get(i).getSegundo();
			if(!fighter.hasLeft()&&fighter.getTeam()==caster.getTeam())
			{
				target=fighter;
				break;
			}
		}
		if (target == null) return;

		fight.addFighterInTeam(target, target.getTeam());
		target.setIsDead(false);
		target.getFightBuff().clear();
		if (target.isInvocation())
			//Cambio de posicion de la invocacion para evitar bug por orden de jugadores
			fight.getOrderPlaying().add((fight.getOrderPlaying().indexOf(caster) + 1), target);

		target.setCell(cell);
		target.getCell().addFighter(target);

		target.fullPdv();
		int percent = (100 - value) * target.getPdvMax() / 100;
		target.removePdv(caster, percent);

		String gm = target.getGmPacket('+', true).substring(3);
		String gtl = fight.getGTL();
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 181, target.getId() + "", gm);
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, target.getId() + "", gtl);
		if (!target.isInvocation())
			GestorSalida.GAME_SEND_STATS_PACKET(target.getPlayer());

		fight.removeDead(target);
		VerificarTrampa(fight, target);
	}

	private void applyEffect_781(ArrayList<Peleador> cibles, Pelea fight) {
		for (Peleador target : cibles) {
			target.addBuff(effectID, value, turns, 1, debuffable, spell, args, caster, true);
		}
	}

	private void applyEffect_782(ArrayList<Peleador> cibles, Pelea fight) {
		for (Peleador target : cibles) {
			target.addBuff(effectID, value, turns, 1, debuffable, spell, args, caster, true);
		}
	}

	private void applyEffect_783(ArrayList<Peleador> cibles, Pelea fight) {
		//Pousse jusqu'a la case visée
		GameCase ccase = caster.getCell();
		//On calcule l'orientation entre les 2 cases
		char dir = Camino.getDirBetweenTwoCase(ccase.getId(), cell.getId(), fight.getMap(), true);
		//On calcule l'id de la case a coté du lanceur dans la direction obtenue
		int tcellID = Camino.GetCaseIDFromDirrection(ccase.getId(), dir, fight.getMap(), true);
		//on prend la case corespondante
		GameCase tcase = fight.getMap().getCase(tcellID);

		if (tcase == null)
			return;
		//S'il n'y a personne sur la case, on arrete
		if (tcase.getFighters().isEmpty())
			return;
		//On prend le Fighter ciblé
		Peleador target = tcase.getFirstFighter();
		//On verifie qu'il peut aller sur la case ciblé en ligne droite
		int c1 = tcellID, limite = 0;
		if (target.getMob() != null)
			for (int i : Constantes.STATIC_INVOCATIONS)
				if (i == target.getMob().getTemplate().getId())
					return;

        while (Camino.GetCaseIDFromDirrection(c1, dir, fight.getMap(), true) != cell.getId()) {
            if (Camino.GetCaseIDFromDirrection(c1, dir, fight.getMap(), true) == -1)
                return;
            c1 = Camino.GetCaseIDFromDirrection(c1, dir, fight.getMap(), true);
            limite++;
            if (limite > 50)
                return;
        }
		GameCase newCell = Camino.checkIfCanPushEntity(fight, ccase.getId(), cell.getId(), dir);

		if (newCell != null)
			cell = newCell;

		target.getCell().getFighters().clear();
		target.setCell(cell);
		target.getCell().addFighter(target);

		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 5, caster.getId() + "", target.getId() + "," + cell.getId());
		VerificarTrampa(fight,target);
	}

	private void applyEffect_784(ArrayList<Peleador> cibles, Pelea fight) {
		Map<Integer, GameCase> origPos = fight.getRholBack(); // les positions de début de combat

		ArrayList<Peleador> list = fight.getFighters(3); // on copie la liste des fighters
		for (int i = 1; i < list.size(); i++)   // on boucle si tout le monde est à la place
			if (!list.isEmpty())                 // d'un autre
				for (Peleador F : list) {
					if (F == null || F.isDead() || !origPos.containsKey(F.getId())) {
						continue;
					}
					if (F.getCell().getId() == origPos.get(F.getId()).getId()) {
						continue;
					}
					if (origPos.get(F.getId()).getFirstFighter() == null) {
						F.getCell().getFighters().clear();
						F.setCell(origPos.get(F.getId()));
						F.getCell().addFighter(F);
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, F.getId() + "", F.getId() + "," + F.getCell().getId());
					}
				}
	}

	private void applyEffect_786(ArrayList<Peleador> objetivos, Pelea pelea) {
		for (Peleador objetivo : objetivos)
			objetivo.addBuff(effectID, value, turns, 1, true, spell, args, caster, true);
	}

	private void applyEffect_787(ArrayList<Peleador> objetivos, Pelea pelea) {
		int hechizoID = -1;
		int hechizoNivel = -1;
		try {
			hechizoID = Integer.parseInt(args.split(";")[0]);
			hechizoNivel = Integer.parseInt(args.split(";")[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Hechizo hechizo = Mundo.mundo.getSort(hechizoID);
		ArrayList<EfectoHechizo> EH = hechizo.getStatsByLevel(hechizoNivel).getEffects();
		for (EfectoHechizo eh : EH) {
			for (Peleador objetivo : objetivos) {
				objetivo.addBuff(eh.effectID, eh.value, 1, 1, true, eh.spell, eh.args, caster, true);
			}
		}
	}

	private void applyEffect_788(ArrayList<Peleador> cibles, Pelea fight) {
		for (Peleador target : cibles) {
			target.addBuff(effectID, value, turns, 1, false, spell, args, target, true);
		}
	}

	private void applyEffect_950(Pelea fight, ArrayList<Peleador> cibles) {
		int id = -1;
		try {
			id = Integer.parseInt(args.split(";")[2]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (id == -1)
			return;
		if (id == 31 || id == 32 || id == 33 || id == 34) {
			turns = 2;
			for (Entry<Integer, Peleador> entry : fight.getTeam1().entrySet()) {
				Peleador mob = entry.getValue();
				mob.setState(id, turns, mob.getId());
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
						+ "", mob.getId() + "," + id + ",1");
				mob.addBuff(effectID, value, turns, 1, false, spell, args, mob, true);
			}

			if (id == 34)
				for (Peleador target : cibles)
					target.addBuff(140, 0, 1, 0, true, 1102, "", caster, true);
		}

		for (Peleador target : cibles) {
			if (spell == 139 && target.getTeam() != caster.getTeam())//Mot d'altruisme on saute les ennemis ?
			{
				continue;
			}
			if (turns <= 0) {
				target.setState(id, turns, target.getId());
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
						+ "", target.getId() + "," + id + ",1");
			} else {
				target.setState(id, turns, target.getId());
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
						+ "", target.getId() + "," + id + ",1");
				target.addBuff(effectID, value, turns, 1, false, spell, args, target, true);
			}
			if (spell == 686) {
				target.unHide(686);
			}

		}
	}

	private void applyEffect_951(Pelea fight, ArrayList<Peleador> cibles) {
		int id = -1;
		try {
			id = Integer.parseInt(args.split(";")[2]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (id == -1)
			return;

		for (Peleador target : cibles) {
			//Si la cible n'a pas l'�tat
			if (!target.haveState(id))
				continue;
			//on enleve l'�tat
			target.setState(id, 0, target.getId());
			//on envoie le packet
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId() + "", target.getId() + "," + id + ",0");
		}
	}

	private void applyEffect_1000(Pelea fight, ArrayList<Peleador> cibles) {
		String[] infos = args.split(";");
		int spellID = Short.parseShort(infos[0]);
		int level = Byte.parseByte(infos[1]);
		byte duration = 1;
		SortStats TS = Mundo.mundo.getSort(spellID).getStatsByLevel(level);
		GameCase celll = null;
		int casenbr = 0;
		boolean quatorze = false;
		for (GameCase entry : fight.getMap().getCases()) {
			casenbr = casenbr + 1;

			if (casenbr == 14 && quatorze) {
				quatorze = false;
				casenbr = 0;
			}
			if (casenbr == 15) {
				quatorze = true;
				casenbr = 0;
			}
			if (quatorze)
				continue;
			celll = entry;
			if (celll == null)
				continue;
			switch (celll.getId()) {
				case 28:
				case 57:
				case 86:
				case 115:
				case 144:
				case 173:
				case 202:
				case 231:
				case 260:
				case 289:
				case 318:
				case 347:
				case 376:
				case 405:
				case 434:
				case 463:
					continue;
			}
			if (!celll.isWalkable(false))
				continue;
			Grifos g = new Grifos(fight, caster, celll, (byte) 0, TS, duration, spell);
			fight.getAllGlyphs().add(g);

			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", "GDZ+" + celll.getId() + ";" + 0 + ";" + g.getColor());
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", "GDC" + celll.getId() + ";Haaaaaaaaa3005;");
		}
	}

	private void applyEffect_1001(Pelea fight, ArrayList<Peleador> cibles) {
		String[] infos = args.split(";");
		int spellID = Short.parseShort(infos[0]);
		int level = Byte.parseByte(infos[1]);
		byte duration = 1;
		SortStats TS = Mundo.mundo.getSort(spellID).getStatsByLevel(level);
		GameCase celll = null;
		int casenbr = 0;
		boolean quatorze = false;
		for (GameCase entry : fight.getMap().getCases()) {
			casenbr = casenbr + 1;

			if (casenbr == 14 && quatorze) {
				quatorze = false;
				casenbr = 0;
			}
			if (casenbr == 15) {
				quatorze = true;
				casenbr = 0;
			}
			if (!quatorze)
				continue;
			celll = entry;
			if (celll == null)
				continue;
			if (!celll.isWalkable(false))
				continue;
			Grifos g = new Grifos(fight, caster, celll, (byte) 0, TS, duration, spell);
			fight.getAllGlyphs().add(g);
			int unk = g.getColor();
			String str = "GDZ+" + celll.getId() + ";" + 0 + ";" + unk;
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", str);
			str = "GDC" + celll.getId() + ";Haaaaaaaaa3005;";
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", str);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", str);
			str = "GDC" + celll.getId() + ";Haaaaaaaaa3005;";
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", str);
		}
	}

	private void applyEffect_1002(Pelea fight, ArrayList<Peleador> cibles) {
		String[] infos = args.split(";");
		int spellID = Short.parseShort(infos[0]);
		int level = Byte.parseByte(infos[1]);
		byte duration = 100;
		SortStats TS = Mundo.mundo.getSort(spellID).getStatsByLevel(level);

		if (cell.isWalkable(true) && !fight.isOccuped(cell.getId())) {
			caster.getCell().getFighters().clear();
			caster.setCell(cell);
			caster.getCell().addFighter(caster);
			new ArrayList<>(fight.getAllTraps()).stream().filter(trap -> Camino.getDistanceBetween(fight.getMap(), trap.getCelda().getId(), caster.getCell().getId()) <= trap.getSize()).forEach(trap -> trap.onTrampa(caster));
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, caster.getId() + "", caster.getId() + "," + cell.getId());
		}

		Grifos g = new Grifos(fight, caster, cell, (byte) 0, TS, duration, spell);
		fight.getAllGlyphs().add(g);
		int unk = g.getColor();
		String str = "GDZ+" + cell.getId() + ";" + 0 + ";" + unk;
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", str);
		str = "GDC" + cell.getId() + ";Haaaaaaaaa3005;";
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", str);
	}

	private ArrayList<Peleador> trierCibles(ArrayList<Peleador> cibles, Pelea fight) {
		ArrayList<Peleador> array = new ArrayList<>();
		int max = -1;
		int distance;

		for (Peleador f : cibles) {
			distance = Camino.getDistanceBetween(fight.getMap(), this.cell.getId(), f.getCell().getId());
			if (distance > max)
				max = distance;
		}

		for (int i = max; i >= 0; i--) {
			Iterator<Peleador> it = cibles.iterator();
			while (it.hasNext()) {
				Peleador f = it.next();
				distance = Camino.getDistanceBetween(fight.getMap(), this.cell.getId(), f.getCell().getId());
				if (distance == i) {
					array.add(f);
					it.remove();
				}
			}
		}

		return array;
	}

	public void verifmobs(Pelea fight, Peleador target, int effet, int cura) {
		switch (target.getMob().getTemplate().getId()) {
			case 232://meulou
				if (target.hasBuff(112)) {
					target.addBuff(112, 5, -1, 1, true, spell, args, caster, true);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 112, caster.getId()
							+ "", target.getId() + "," + 5 + "," + -1);
				}
				break;
			case 233:
				if (effet == 168 || effet == 101) {
					target.addBuff(128, 1, 2, 1, true, spell, args, target, true);
					//Gain de PM pendant le tour de jeu
					target.setCurPm(fight, 1);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
							+ "", target.getId() + "," + 1 + "," + 2);
				}
				if (effet == 169 || effet == 127)//rall pm don pa
				{
					target.addBuff(111, 1, 2, 1, true, spell, args, target, true);
					//Gain de PA pendant le tour de jeu
					target.setCurPa(fight, 1);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
							+ "", target.getId() + "," + 1 + "," + 2);
				}
				if (effet == 100 || effet == 97) {
					int healFinal = 200;
					if ((healFinal + caster.getPdv()) > caster.getPdvMax())
						healFinal = caster.getPdvMax() - caster.getPdv();
					if (healFinal < 1)
						healFinal = 0;
					caster.removePdv(caster, healFinal);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 108, caster.getId()
							+ "", target.getId() + "," + healFinal);

				}
				break;
			case 2750:
				int healFinal = cura;
				if ((healFinal + caster.getPdv()) > caster.getPdvMax())
					healFinal = caster.getPdvMax() - caster.getPdv();
				if (healFinal < 1)
					healFinal = 0;
				caster.removePdv(caster, -healFinal);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 108, caster.getId()
						+ "", caster.getId() + "," + healFinal);
				break;
			case 1045://kimbo
				if (effet == 99 || effet == 98 || effet == 94 || effet == 93) {
					target.setState(30, 1, target.getId());//etat pair
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
							+ "", target.getId() + "," + 30 + ",1");
					if (target.haveState(29)) {
						target.setState(29, 0, target.getId());//etat 29 impair
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
								+ "", target.getId() + "," + 29 + ",0");
					}
				} else if (effet == 97 || effet == 96 || effet == 92 || effet == 91) {
					target.setState(29, 1, target.getId());//etat etat si pair
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
							+ "", target.getId() + "," + 29 + ",1");
					if (target.haveState(30)) {
						target.setState(30, 0, target.getId());//etat 29 si pair
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
								+ "", target.getId() + "," + 30 + ",0");
					}
				}
				break;
			case 423://kralamour
				if (effet == 99 || effet == 94) {
					target.setState(37, 1, target.getId());//etat tersiaire feu
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
							+ "", target.getId() + "," + 37 + ",1");
					if (target.haveState(38))//secondaire terre
					{
						target.setState(38, 0, target.getId());
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
								+ "", target.getId() + "," + 38 + ",0");
					}
					if (target.haveState(36))//quanternaire eau
					{
						target.setState(36, 0, target.getId());
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
								+ "", target.getId() + "," + 36 + ",0");
					}
					if (target.haveState(35))//primaire air
					{
						target.setState(35, 0, target.getId());
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
								+ "", target.getId() + "," + 35 + ",0");
					}
				} else if (effet == 98 || effet == 93) {
					target.setState(35, 1, target.getId());//etat primaire air
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
							+ "", target.getId() + "," + 35 + ",1");
					if (target.haveState(38))//secondaire terre
					{
						target.setState(38, 0, target.getId());
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
								+ "", target.getId() + "," + 38 + ",0");
					}
					if (target.haveState(36))//quanternaire eau
					{
						target.setState(36, 0, target.getId());
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
								+ "", target.getId() + "," + 36 + ",0");
					}
					if (target.haveState(37))//tersiaire feu
					{
						target.setState(37, 0, target.getId());
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
								+ "", target.getId() + "," + 37 + ",0");
					}
				} else if (effet == 97 || effet == 92) {
					target.setState(38, 1, target.getId());//etat secondaire terre
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
							+ "", target.getId() + "," + 38 + ",1");
					if (target.haveState(35))//primaire air
					{
						target.setState(35, 0, target.getId());
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
								+ "", target.getId() + "," + 35 + ",0");
					}
					if (target.haveState(36))//quanternaire eau
					{
						target.setState(36, 0, target.getId());
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
								+ "", target.getId() + "," + 36 + ",0");
					}
					if (target.haveState(37))//tersiaire feu
					{
						target.setState(37, 0, target.getId());
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
								+ "", target.getId() + "," + 37 + ",0");
					}
				} else if (effet == 96 || effet == 91) {
					target.setState(36, 1, target.getId());//etat quanternaire eau
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
							+ "", target.getId() + "," + 36 + ",1");
					if (target.haveState(35))//primaire air
					{
						target.setState(35, 0, target.getId());
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
								+ "", target.getId() + "," + 35 + ",0");
					}
					if (target.haveState(38))//secondaire terre
					{
						target.setState(38, 0, target.getId());
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
								+ "", target.getId() + "," + 38 + ",0");
					}
					if (target.haveState(37))//tersiaire feu
					{
						target.setState(37, 0, target.getId());
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
								+ "", target.getId() + "," + 37 + ",0");
					}
				}

				break;
			case 1071://Rasboul
				if (effet == 99 || effet == 94) {
					if (target.hasBuff(214)) {
						target.addBuff(218, 50, 4, 1, false, 1039, "", target, true);// - 50 feu
						target.addBuff(210, 50, 4, 1, false, 1039, "", target, true);// + 50 terre
						target.addBuff(211, 50, 4, 1, false, 1039, "", target, true);// + 50 eau
						target.addBuff(212, 50, 4, 1, false, 1039, "", target, true);// + 50 air
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 1039, caster.getId()
								+ "", target.getId() + "," + "" + "," + 1);
					}
				} else if (effet == 98 || effet == 93) {
					if (target.hasBuff(214)) {
						target.addBuff(217, 50, 4, 1, false, 1039, "", target, true);// - 50 air
						target.addBuff(210, 50, 4, 1, false, 1039, "", target, true);// + 50 terre
						target.addBuff(211, 50, 4, 1, false, 1039, "", target, true);// + 50 eau
						target.addBuff(213, 50, 4, 1, false, 1039, "", target, true);// + 50 feu
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 1039, caster.getId()
								+ "", target.getId() + "," + "" + "," + 1);
					}
				} else if (effet == 97 || effet == 92) {
					if (target.hasBuff(214)) {
						target.addBuff(215, 50, 4, 1, false, 1039, "", target, true);// - 50 terre
						target.addBuff(212, 50, 4, 1, false, 1039, "", target, true);// + 50 air
						target.addBuff(211, 50, 4, 1, false, 1039, "", target, true);// + 50 eau
						target.addBuff(213, 50, 4, 1, false, 1039, "", target, true);// + 50 feu
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 1039, caster.getId()
								+ "", target.getId() + "," + "" + "," + 1);
					}
				} else if (effet == 96 || effet == 91) {
					if (target.hasBuff(214)) {
						target.addBuff(216, 50, 4, 1, false, 1039, "", target, true);// - 50 eau
						target.addBuff(212, 50, 4, 1, false, 1039, "", target, true);// + 50 air
						target.addBuff(210, 50, 4, 1, false, 1039, "", target, true);// + 50 terre
						target.addBuff(213, 50, 4, 1, false, 1039, "", target, true);// + 50 feu
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 1039, caster.getId()
								+ "", target.getId() + "," + "" + "," + 1);
					}
				}
				if (effet == 101) {
					target.addBuff(111, value, 1, 1, true, spell, args, target, true);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 111, target.getId()
							+ "", target.getId() + ",+" + value);
				}
				break;
		}
	}

	public static void VerificarTrampa(Pelea pelea, Peleador peleador) {
		new ArrayList<>(pelea.getAllTraps()).stream().filter(trap -> Camino.getDistanceBetween(pelea.getMap(),trap.getCelda().getId(),peleador.getCell().getId())<=trap.getSize()).forEach(trap -> trap.onTrampa(peleador));
	}

	private void checkLifeTree(Peleador target, int value) {
		if (target.hasBuff(786)) {
			if (value + this.caster.getPdv() > this.caster.getPdvMax()) {
				value = this.caster.getPdvMax() - this.caster.getPdv();
			}
			this.caster.removePdv(this.caster, - value);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(target.getFight(), 7, 100, "" + target.getId() + "", "" + this.caster.getId() + ",+" + value);
		}
	}

	public boolean isPoison() {
		return this.spell == 66 || this.spell == 164 || this.spell == 71 || this.spell == 196 || this.spell == 219 || this.spell == 181 || this.spell == 200;
	}
}