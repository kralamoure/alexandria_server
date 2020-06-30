package org.alexandria.estaticos.comandos;

import org.alexandria.estaticos.Casas;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.estaticos.cliente.Jugador.Grupo;
import org.alexandria.comunes.Camino;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.configuracion.Constantes;
import org.alexandria.estaticos.juego.JuegoServidor;
import org.alexandria.estaticos.juego.accion.AccionIntercambiar;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.pelea.arena.FightManager;
import org.alexandria.estaticos.pelea.arena.TeamMatch;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ComandosJugadores {

    public final static String canal = "General";
    public static boolean canalMute = false;

    public static boolean analyse(Jugador jugador, String msg) {
        if (msg.charAt(0) == '.' && msg.charAt(1) != '.') {

            //Comandos .staff
            if  (comando(msg, "staff")) {
                 GestorSalida.GAME_SEND_Im_PACKET(jugador, "1244");
                 boolean vide = true;

                 for (Jugador target : Mundo.mundo.getOnlinePlayers()) {
                     if (target == null) continue;
                     if (target.getGroupe() == null || target.isInvisible()) continue;

                     jugador.sendMessage("- <b><a href='asfunction:onHref,ShowPlayerPopupMenu," + target.getName() + "'>[" + target.getGroupe().getNombre() + "] " + target.getName() + "</a></b>");
                     vide = false;
                 }
                 if (vide)
                     GestorSalida.GAME_SEND_Im_PACKET(jugador, "1245");
                 return true;

             //Comando .micasa
            } else if (comando(msg, ".micasa")) {
                StringBuilder message = new StringBuilder();
                if (!msg.contains("all")) {
                    message = new StringBuilder("L'id de la maison la plus proche est : ");
                    short lstDist = 999;
                    Casas nearest = null;
                    for (Casas house : Mundo.mundo.getHouses().values()) {
                        if (house.getMapId() == jugador.getCurMap().getId()) {
                            short dist = (short) Camino.getDistanceBetween(jugador.getCurMap(), house.getCellId(), jugador.getCurCell().getId());
                            if (dist < lstDist) {
                                nearest = house;
                                lstDist = dist;
                            }
                        }
                    }
                    if (nearest != null) message.append(nearest.getId());
                } else {
                    for (Casas house : Mundo.mundo.getHouses().values()) {
                        if (house.getMapId() == jugador.getCurMap().getId()) {
                            message.append("Maison ").append(house.getId()).append(" | cellId : ").append(house.getId());
                        }
                    }
                    if (message.length() == 0) message = new StringBuilder("Aucune maison sur cet carte.");
                }
                jugador.sendMessage(message.toString());
                return true;

                //Comando .deblo
            } else if (comando(msg, "deblo")) {
            if (jugador.cantTP())
                return true;
            if (jugador.getPelea() != null)
                return true;
            if (jugador.getCurCell().isWalkable(true)) {
                GestorSalida.GAME_SEND_Im_PACKET(jugador, "0216;");
                return true;
            }
            jugador.teleport(jugador.getCurMap().getId(), jugador.getCurMap().getRandomFreeCellId());
            return true;

                //Comando .astrub
            } else if (Configuracion.INSTANCE.getAstrub() && comando(msg, "astrub")) {
                if (jugador.getPelea() != null) {
                    GestorSalida.GAME_SEND_Im_PACKET(jugador, "1269");
                    return true;
                }
                jugador.teleport((short) 7411, 311);
                return true;

                //Comando .pvp
            } else if (Configuracion.INSTANCE.getPvp() && comando(msg, "pvp")) {
                if (jugador.getPelea() != null) {
                    GestorSalida.GAME_SEND_Im_PACKET(jugador, "1269");
                    return true;
                }
                jugador.teleport((short) 952, 165);
                return true;

                //Comando .azra
            } else if (Configuracion.INSTANCE.getAzra() && comando(msg, "azra")) {
                if (jugador.getPelea() != null) {
                    GestorSalida.GAME_SEND_Im_PACKET(jugador, "1269");
                    return true;
                }
                jugador.teleport((short) 10853, 283);
                return true;

            //Comando .infos
            } else if (comando(msg, "infos")) {
            long uptime= Instant.now().toEpochMilli() - Configuracion.INSTANCE.getStartTime();
            final int jour=(int)(uptime/86400000L);
            uptime%=86400000L;
            final int hour=(int)(uptime/3600000L);
            uptime%=3600000L;
            final int min=(int)(uptime/60000L);
            uptime%=60000L;
            int sec=(int)(uptime/1000L);
            int nbPlayer = JuegoServidor.getClients().size();
            int nbPlayerIp = JuegoServidor.getPlayersNumberByIp();

            GestorSalida.GAME_SEND_Im_PACKET(jugador, "0233;" + Configuracion.INSTANCE.getNAME() + "~" + Configuracion.INSTANCE.getUrl());
            GestorSalida.GAME_SEND_Im_PACKET(jugador, "0217;" + jour + "~" + hour + "~" + min + "~" + sec + "~" + nbPlayer + "~" + nbPlayerIp + "~" + Configuracion.INSTANCE.getRATE_XP() + "~" + Configuracion.INSTANCE.getRATE_DROP() + "~" + Configuracion.INSTANCE.getRATE_JOB());
            return true;

            //Comando .grupo
            } else if (comando(msg, "grupo")) {
                if (jugador.isInPrison() || jugador.getPelea() != null)
                    return true;

                Mundo.mundo.getOnlinePlayers().stream().filter(p -> !p.equals(jugador) && p.getParty() == null && p.getAccount().getCurrentIp().equals(jugador.getAccount().getCurrentIp()) && p.getPelea() == null && !p.isInPrison()).forEach(p -> {
                    if(jugador.getParty() == null) {
                        Grupo party = new Grupo(jugador, p);
                        GestorSalida.GAME_SEND_GROUP_CREATE(jugador.getGameClient(), party);
                        GestorSalida.GAME_SEND_PL_PACKET(jugador.getGameClient(), party);
                        GestorSalida.GAME_SEND_GROUP_CREATE(p.getGameClient(), party);
                        GestorSalida.GAME_SEND_PL_PACKET(p.getGameClient(), party);
                        jugador.setParty(party);
                        p.setParty(party);
                        GestorSalida.GAME_SEND_ALL_PM_ADD_PACKET(jugador.getGameClient(), party);
                        GestorSalida.GAME_SEND_ALL_PM_ADD_PACKET(p.getGameClient(), party);
                    } else {
                        GestorSalida.GAME_SEND_GROUP_CREATE(p.getGameClient(), jugador.getParty());
                        GestorSalida.GAME_SEND_PL_PACKET(p.getGameClient(), jugador.getParty());
                        GestorSalida.GAME_SEND_PM_ADD_PACKET_TO_GROUP(jugador.getParty(), p);
                        jugador.getParty().addPlayer(p);
                        p.setParty(jugador.getParty());
                        GestorSalida.GAME_SEND_ALL_PM_ADD_PACKET(p.getGameClient(), jugador.getParty());
                        GestorSalida.GAME_SEND_PR_PACKET(p);
                    }
                });
                return true;

                //Comando .banco
            } else if (Configuracion.INSTANCE.getBanco() && comando(msg, "banco")) {
                if (jugador.isInPrison() || jugador.getPelea() != null)
                    return true;
                jugador.openBank();
                return true;

            //Comando .transferir
            }else if (comando(msg, "transferir")) {
            if (jugador.isInPrison() || jugador.getPelea() != null )
                return true;
            if(jugador.getExchangeAction() == null || jugador.getExchangeAction().getType() != AccionIntercambiar.IN_BANK) {
                GestorSalida.GAME_SEND_Im_PACKET(jugador, "1246;");
                return true;
            }

            GestorSalida.GAME_SEND_Im_PACKET(jugador, "0221;");
            int count = 0;

            for (ObjetoJuego objeto : new ArrayList<>(jugador.getItems().values())) {
                if (objeto == null || objeto.getModelo() == null || !objeto.getModelo().getStrTemplate().isEmpty())
                    continue;
                switch (objeto.getModelo().getType()) {
                    case Constantes.ITEM_TYPE_OBJET_VIVANT:case Constantes.ITEM_TYPE_PRISME:
                    case Constantes.ITEM_TYPE_FILET_CAPTURE:case Constantes.ITEM_TYPE_CERTIF_MONTURE:
                    case Constantes.ITEM_TYPE_OBJET_UTILISABLE:case Constantes.ITEM_TYPE_OBJET_ELEVAGE:
                    case Constantes.ITEM_TYPE_CADEAUX:case Constantes.ITEM_TYPE_PARCHO_RECHERCHE:
                    case Constantes.ITEM_TYPE_PIERRE_AME:case Constantes.ITEM_TYPE_BOUCLIER:
                    case Constantes.ITEM_TYPE_SAC_DOS:case Constantes.ITEM_TYPE_OBJET_MISSION:
                    case Constantes.ITEM_TYPE_BOISSON:case Constantes.ITEM_TYPE_CERTIFICAT_CHANIL:
                    case Constantes.ITEM_TYPE_FEE_ARTIFICE:case Constantes.ITEM_TYPE_MAITRISE:
                    case Constantes.ITEM_TYPE_POTION_SORT:case Constantes.ITEM_TYPE_POTION_METIER:
                    case Constantes.ITEM_TYPE_POTION_OUBLIE:case Constantes.ITEM_TYPE_BONBON:
                    case Constantes.ITEM_TYPE_PERSO_SUIVEUR:case Constantes.ITEM_TYPE_RP_BUFF:
                    case Constantes.ITEM_TYPE_MALEDICTION:case Constantes.ITEM_TYPE_BENEDICTION:
                    case Constantes.ITEM_TYPE_TRANSFORM:case Constantes.ITEM_TYPE_DOCUMENT:
                    case Constantes.ITEM_TYPE_QUETES:
                        break;
                    default:
                        count++;
                        jugador.addInBank(objeto.getId(), objeto.getCantidad());
                        break;
                }
            }
            GestorSalida.GAME_SEND_Im_PACKET(jugador, "0222;" + count);
            return true;

            //Comando .koli
            }else if (Configuracion.INSTANCE.getTEAM_MATCH() && comando(msg, "koli")) {
                if (jugador.koliseo != null) {
                    if (jugador.getParty() != null) {
                        if (jugador.getParty().isChief(jugador.getId())) {
                            jugador.koliseo.unsubscribe(jugador.getParty());
                            return true;
                        }
                        jugador.koliseo.unsubscribe(jugador);
                        jugador.sendMessage("Vous venez de vous désincrire de la file d'attente.");
                    } else {
                        jugador.koliseo.unsubscribe(jugador);
                        jugador.sendMessage("Vous venez de vous désincrire de la file d'attente.");
                    }
                    return true;
                } else {
                    if (jugador.getParty() != null) {
                        if (jugador.getParty().getPlayers().size() < 2) {
                            jugador.setParty(null);
                            GestorSalida.GAME_SEND_PV_PACKET(jugador.getGameClient(), "");
                            ComandosJugadores.analyse(jugador, ".kolizeum");
                            return true;
                        }
                        if (!jugador.getParty().isChief(jugador.getId())) {
                            jugador.sendMessage("Vous ne pouvez pas inscrire votre groupe, vous n'en êtes pas le chef.");
                            return true;
                        } else if (jugador.getParty().getPlayers().size() != TeamMatch.PER_TEAM) {
                            jugador.sendMessage("Pour vous inscrire, vous devez être exactement " + TeamMatch.PER_TEAM
                                    + " joueurs dans votre groupe.");
                            return true;
                        }
                        FightManager.subscribeKolizeum(jugador, true);
                    } else {
                        FightManager.subscribeKolizeum(jugador, false);
                    }
                }
                return true;

                //Comando .death
            } else  if (Configuracion.INSTANCE.getDEATH_MATCH() && comando(msg, "death")) {
                if(jugador.cantTP()) return true;
                if (jugador.deathMatch != null) {
                    FightManager.removeDeathMatch(jugador.deathMatch);
                    jugador.deathMatch = null;
                    jugador.sendMessage("Vous venez de vous désincrire de la file d'attente.");
                } else {
                    if(jugador.getEquippedObjects().size() == 0) {
                        jugador.sendMessage("Vous devez avoir des objets équipés.");
                    } else {
                        FightManager.subscribeDeathMatch(jugador);
                    }
                }
                return true;

            //Comando .esclavo
            } else if (comando(msg, "jefe") || comando(msg, "maitre") || comando(msg, "esclavo")) {
                if(jugador.cantTP()) return true;

                final Grupo party = jugador.getParty();

                if (party == null) {
                    GestorSalida.GAME_SEND_Im_PACKET(jugador, "1251;");
                    return true;
                }

                final List<Jugador> players = jugador.getParty().getPlayers();

                if (!party.getChief().getName().equals(jugador.getName())) {
                    GestorSalida.GAME_SEND_Im_PACKET(jugador, "1252;");
                    return true;
                }

                if (msg.length() <= 8 && party.getMaster() != null) {
                    GestorSalida.GAME_SEND_Im_PACKET(jugador, "0226;");
                    players.stream().filter(follower -> follower != party.getMaster())
                            .forEach(follower ->
                   GestorSalida.GAME_SEND_Im_PACKET(follower, "0227;" + party.getMaster().getName()));
                    party.setMaster(null);
                    return true;
                }

                Jugador target = jugador;

                if (msg.length() > 8) {
                    String name = msg.substring(8, msg.length() - 1);
                    target = Mundo.mundo.getPlayerByName(name);
                }

                if (target == null) {
                    GestorSalida.GAME_SEND_Im_PACKET(jugador, "1253;");
                    return true;
                }
                if (target.getParty() == null || !target.getParty().getPlayers().contains(jugador)) {
                    GestorSalida.GAME_SEND_Im_PACKET(jugador, "1254;");
                    return true;
                }

                party.setMaster(target);

                for (Jugador follower : players)
                    if(follower != target)
                        GestorSalida.GAME_SEND_Im_PACKET(follower, "0227;" + target.getName());

                party.moveAllPlayersToMaster(null);
                GestorSalida.GAME_SEND_Im_PACKET(target, "0228;");
                return true;

            //Comando .comandos
        } else if (comando(msg, "comandos")) {
            GestorSalida.GAME_SEND_Im_PACKET(jugador, "1260;");
            return true;

            //Comando .pass
    } else if (comando(msg, "pass")) {
            jugador.setComandoPasarTurno(!jugador.getComandoPasarTurno());
            if (jugador.getComandoPasarTurno()) {
                GestorSalida.GAME_SEND_Im_PACKET(jugador, "0236;");
            } else {
                GestorSalida.GAME_SEND_Im_PACKET(jugador, "0237;");
                return true;
            }
        }
     }else if (comando(msg, "za")) {
            if (jugador.getGroupe() == null) {
                return false;
            }
            String[] infos = msg.split(" ",2);
            String prefix = "<b>"+Configuracion.INSTANCE.getNAME()+"</b>";
            if (infos.length > 1) {
                String suffix = infos[1];
                if (suffix.contains("<") && (!suffix.contains(">") || !suffix.contains("</"))) // S'il n'y a pas de balise fermante
                    suffix = suffix.replace("<", "").replace(">", "");
                if (suffix.contains("<") && suffix.contains(">") && !suffix.contains("</")) // S'il n'y a pas de balise fermante
                    suffix = suffix.replace("<", "").replace(">", "");
                GestorSalida.GAME_SEND_Im_PACKET_TO_ALL("116;" + prefix + "~" + suffix);
            }
            return true;
        }else if (comando(msg, "z")) {
            if (jugador.getGroupe() == null) {
                return false;
            }
            String[] infos = msg.split(" ",2);
            String prefix = "<b><a href='asfunction:onHref,ShowPlayerPopupMenu," + jugador.getName() + "'>[" + jugador.getGroupe().getNombre() + "] " + jugador.getName() + "</a></b>";
            if (infos.length > 1) {
                String suffix = infos[1];
                if (suffix.contains("<") && (!suffix.contains(">") || !suffix.contains("</"))) // S'il n'y a pas de balise fermante
                    suffix = suffix.replace("<", "").replace(">", "");
                if (suffix.contains("<") && suffix.contains(">") && !suffix.contains("</")) // S'il n'y a pas de balise fermante
                    suffix = suffix.replace("<", "").replace(">", "");
                GestorSalida.GAME_SEND_Im_PACKET_TO_ALL("116;" + prefix + "~" + suffix);
            }
            return true;
        }
        return false;
    }

    private static boolean comando(String mensaje, String comando) {
        return mensaje.length() > comando.length() && mensaje.substring(1, comando.length() + 1).equalsIgnoreCase(comando);
    }
}