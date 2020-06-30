package org.alexandria.estaticos.comandos;

import org.alexandria.estaticos.area.SubArea;
import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;
import org.alexandria.estaticos.Cercados;
import org.alexandria.estaticos.cliente.Cuenta;
import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.estaticos.comandos.administracion.Comandos;
import org.alexandria.estaticos.comandos.administracion.GrupoADM;
import org.alexandria.estaticos.comandos.administracion.UsuarioAdministrador;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.configuracion.Constantes;
import org.alexandria.configuracion.MainServidor;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.estaticos.Recaudador;
import org.alexandria.estaticos.Monstruos;
import org.alexandria.estaticos.Monstruos.MobGrade;
import org.alexandria.estaticos.Monstruos.MobGroup;
import org.alexandria.estaticos.Montura;
import org.alexandria.estaticos.Npc;
import org.alexandria.intercambio.IntercambioCliente;
import org.alexandria.estaticos.juego.JuegoCliente;
import org.alexandria.estaticos.juego.JuegoServidor;
import org.alexandria.estaticos.juego.accion.AccionIntercambiar;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.juego.mundo.MundoGuardado;
import org.alexandria.estaticos.Mision;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.objeto.ObjetoModelo;
import org.alexandria.estaticos.objeto.ObjetoSet;
import org.alexandria.estaticos.oficio.OficioCaracteristicas;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Retos;
import org.alexandria.estaticos.Mascota.MascotaEntrada;
import org.alexandria.estaticos.Mision.*;
import org.alexandria.estaticos.Npc.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class ComandosAdministrador extends UsuarioAdministrador {

    public ComandosAdministrador(Jugador player) {
        super(player);
    }

    public void apply(String packet) {
        String msg = packet.substring(2);
        String[] infos = msg.split(" ");

        if (infos.length == 0) return;
        String command = infos[0];

        try {
            GrupoADM groupe = this.getJugador().getGroupe();
            if (groupe == null) {
                this.getCliente().kick();
                return;
            }
            if (!groupe.haveCommand(command)) {
                this.sendMessage("Comando invalido!");
                return;
            }

            this.command(command, infos, msg);
        } catch (Exception ignored) {}
    }

    public void command(String command, String[] infos, String msg) {
        if (command.equalsIgnoreCase("CHALL")) {
            Retos challenge = new Retos(this.getJugador().getPelea(), Integer.parseInt(infos[1]), 0, 0);
            this.getJugador().getPelea().getAllChallenges().put(Integer.parseInt(infos[1]), challenge);
            challenge.fightStart();
            GestorSalida.GAME_SEND_CHALLENGE_FIGHT(this.getJugador().getPelea(), 1, challenge.parseToPacket());
            return;
        } else if (command.equalsIgnoreCase("HELP")) {
            String cmd = infos.length == 2 ? infos[1] : "";

            if (cmd.equalsIgnoreCase("")) {
                this.sendMessage("\nVous avez actuellement le groupe GM " + this.getJugador().getGroupe().getNombre() + ".\nCommandes disponibles :\n");
                for (Comandos commande : this.getJugador().getGroupe().getComandos()) {
                    String args = (commande.getArgumento()[1] != null && !commande.getArgumento()[1].equalsIgnoreCase("")) ? (" + " + commande.getArgumento()[1]) : ("");
                    String desc = (commande.getArgumento()[2] != null && !commande.getArgumento()[2].equalsIgnoreCase("")) ? (commande.getArgumento()[2]) : ("");
                    this.sendMessage("<u>" + commande.getArgumento()[0] + args + "</u> - " + desc);
                }
            } else {
                this.sendMessage("\nVous avez actuellement le groupe GM " + this.getJugador().getGroupe().getNombre() + ".\nCommandes recherches :\n");
                for (Comandos commande : this.getJugador().getGroupe().getComandos()) {
                    if (commande.getArgumento()[0].contains(cmd.toUpperCase())) {
                        String args = (commande.getArgumento()[1] != null && !commande.getArgumento()[1].equalsIgnoreCase("")) ? (" + " + commande.getArgumento()[1]) : ("");
                        String desc = (commande.getArgumento()[2] != null && !commande.getArgumento()[2].equalsIgnoreCase("")) ? (commande.getArgumento()[2]) : ("");
                        this.sendMessage("<u>" + commande.getArgumento()[0] + args + "</u> - " + desc);
                    }
                }
            }
            return;
        } else if (command.equalsIgnoreCase("ONLINE")) {
            Jugador perso = this.getJugador();
            if (infos.length > 1) {//Si un nom de perso est specifie
                try {
                    perso = Mundo.mundo.getPlayerByName(infos[1]);
                } catch (Exception e) {
                    // ok
                }
                if (perso == null) {
                    this.sendMessage("Le personnage n'a pas ete trouve");
                    return;
                }
            }
            if (perso.getGameClient() != null)
                perso.getGameClient().kick();
            perso.setOnline(false);
            perso.resetVars();
            Database.dinamicos.getPlayerData().update(perso);
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(), perso.getId());
            Mundo.mundo.unloadPerso(perso);
            Database.dinamicos.getPlayerData().load(perso.getId());
            Mundo.mundo.ReassignAccountToChar(perso.getAccount());
            String str = "Le joueur " + perso.getName() + " a ete reinitialise de ces variables.";
            this.sendMessage(str);
            return;
        } else if (command.equalsIgnoreCase("ANAME")) {
            infos = msg.split(" ", 2);
            String prefix = "<b><a href='asfunction:onHref,ShowPlayerPopupMenu," + this.getJugador().getName() + "'>[" + this.getJugador().getGroupe().getNombre() + "] " + this.getJugador().getName() + "</a></b>";
            if(infos.length > 1) {
                String suffix = infos[1];
                if (suffix.contains("<") && (!suffix.contains(">") || !suffix.contains("</"))) // S'il n'y a pas de balise fermante
                    suffix = suffix.replace("<", "").replace(">", "");
                if (suffix.contains("<") && suffix.contains(">") && !suffix.contains("</")) // S'il n'y a pas de balise fermante
                    suffix = suffix.replace("<", "").replace(">", "");
                GestorSalida.GAME_SEND_Im_PACKET_TO_ALL("116;" + prefix + "~" + suffix);
            }
            return;
        } else if (command.equalsIgnoreCase("GONAME")
                || command.equalsIgnoreCase("JOIN")
                || command.equalsIgnoreCase("GON")) {
            Jugador P = Mundo.mundo.getPlayerByName(infos[1]);
            if (P == null) {
                String str = "Le personnage de destination n'existe pas.";
                this.sendMessage(str);
                return;
            }
            short mapID = P.getCurMap().getId();
            int cellID = P.getCurCell().getId();

            Jugador perso = this.getJugador();
            if (infos.length > 2)//Si un nom de perso est specifie
            {
                perso = Mundo.mundo.getPlayerByName(infos[2]);
                if (perso == null) {
                    String str = "Le personnage e teleporter n'a pas ete trouve.";
                    this.sendMessage(str);
                    return;
                }
                if (perso.getPelea() != null) {
                    String str = "La cible e teleporter est en combat.";
                    this.sendMessage(str);
                    return;
                }
            }
            perso.teleport(mapID, cellID);
            String str = "Le joueur " + perso.getName()
                    + " a ete teleporte vers " + P.getName() + ".";
            this.sendMessage(str);
            return;
        } else if (command.equalsIgnoreCase("KICKFIGHT")) {
            Jugador P = Mundo.mundo.getPlayerByName(infos[1]);
            if (P == null || P.getPelea() == null) {
                this.sendMessage("Le personnage n'a pas ete trouve ou il n'est pas en combat.");
                return;
            }
            GestorSalida.GAME_SEND_GV_PACKET(P);
            if (P.getPelea() != null) {
                P.getPelea().leftFight(P, null);
                P.setPelea(null);
            }
            GestorSalida.GAME_SEND_GV_PACKET(P);
            this.sendMessage("Le personnage "
                    + P.getName() + " a ete expulse de son combat.");
            return;
        } else if (command.equalsIgnoreCase("DEBUG")) {
            Jugador perso = this.getJugador();
            if (infos.length > 1)//Si un nom de perso est specifie
            {
                perso = Mundo.mundo.getPlayerByName(infos[1]);
                if (perso == null) {
                    String str = "Le personnage n'a pas ete trouve.";
                    this.sendMessage(str);
                    return;
                }
                if (perso.getPelea() != null) {
                    String str = "La cible est en combat.";
                    this.sendMessage(str);
                    return;
                }
            } else {
                return;
            }
            perso.warpToSavePos();
            String str = "Le joueur " + perso.getName()
                    + " a ete teleporte e son point de sauvegarde.";
            this.sendMessage(str);
            return;
        } else if (command.equalsIgnoreCase("JOBLEFT")) {
            Jugador perso = this.getJugador();
            try {
                perso = Mundo.mundo.getPlayerByName(infos[1]);
            } catch (Exception e) {
                // ok
            }
            if (perso == null)
                perso = this.getJugador();
            perso.setDoAction(false);
            perso.setExchangeAction(null);
            this.sendMessage("L'action de metier e ete annule.");
            return;
        } else if (command.equalsIgnoreCase("WHO")) {
            String mess = "\n<u>Liste des joueurs en ligne :</u>";
            this.sendMessage(mess);
            int i = 0;

            for (Jugador player : Mundo.mundo.getOnlinePlayers()) {
                if (i == 30)
                    break;
                if (player == null)
                    continue;
                i++;
                mess = player.getName() + " (" + player.getId() + ") ";
                mess += returnClasse(player.getClasse());
                mess += " ";
                mess += (player.getSexe() == 0 ? "M" : "F") + " ";
                mess += player.getLevel() + " ";
                mess += player.getCurMap().getId() + "("
                        + player.getCurMap().getX() + "/"
                        + player.getCurMap().getY() + ") ";
                mess += player.getPelea() == null ? "" : "Combat ";
                mess += player.getAccount().getCurrentIp();

                this.sendMessage(mess);
            }

            if (JuegoServidor.getClients().size() - 30 > 0) {
                mess = "Et " + (JuegoServidor.getClients().size() - 30)
                        + " autres personnages";
                this.sendMessage(mess);
            }
            mess = "\n";
            this.sendMessage(mess);
            return;
        } else if (command.equalsIgnoreCase("WHOALL")) {
            String mess = "\n<u>Liste des joueurs en ligne :</u>";
            this.sendMessage(mess);
            for (JuegoCliente client : JuegoServidor.getClients()) {
                Jugador player = client.getPlayer();

                if (player == null)
                    continue;

                mess = player.getName() + " (" + player.getId() + ") ";
                mess += returnClasse(player.getClasse());
                mess += " ";
                mess += (player.getSexe() == 0 ? "M" : "F") + " ";
                mess += player.getLevel() + " ";
                mess += player.getCurMap().getId() + "("
                        + player.getCurMap().getX() + "/"
                        + player.getCurMap().getY() + ") ";
                mess += player.getPelea() == null ? "" : "Combat ";
                mess += player.getAccount().getCurrentIp();

                this.sendMessage(mess);
            }
            mess = "\n";
            this.sendMessage(mess);
            return;
        } else if (command.equalsIgnoreCase("WHOFIGHT")) {
            String mess = "";
            this.sendMessage("\n<u>Liste des joueurs en ligne et en combat :</u>");
            for (JuegoCliente client : JuegoServidor.getClients()) {
                Jugador player = client.getPlayer();

                if (player == null)
                    continue;

                if (player.getPelea() == null)
                    continue;

                mess = player.getName() + " (" + player.getId() + ") ";
                mess += returnClasse(player.getClasse());
                mess += " ";
                mess += (player.getSexe() == 0 ? "M" : "F") + " ";
                mess += player.getLevel() + " ";
                mess += player.getCurMap().getId() + "("
                        + player.getCurMap().getX() + "/"
                        + player.getCurMap().getY() + ") ";
                mess += player.getPelea() == null ? "" : "Combat ";
                mess += player.getAccount().getCurrentIp();

                this.sendMessage(mess);
            }
            if (mess.equalsIgnoreCase("")) {
                this.sendMessage("Aucun joueur en combat.");
            } else {
                mess = "\n";
                this.sendMessage(mess);
            }
            return;
        } else if (command.equalsIgnoreCase("NAMEGO")
                || command.equalsIgnoreCase("NGO")) {
            Jugador perso = Mundo.mundo.getPlayerByName(infos[1]);
            if (perso == null) {
                String str = "Le personnage e teleporter n'existe pas.";
                this.sendMessage(str);
                return;
            }
            if (perso.getPelea() != null) {
                String str = "Le personnage e teleporter est en combat.";
                this.sendMessage(str);
                return;
            }
            Jugador P = this.getJugador();
            if (infos.length > 2)//Si un nom de perso est specifie
            {
                P = Mundo.mundo.getPlayerByName(infos[2]);
                if (P == null) {
                    String str = "Le personnage de destination n'a pas ete trouve.";
                    this.sendMessage(str);
                    return;
                }
            }
            if (P.isOnline()) {
                short mapID = P.getCurMap().getId();
                int cellID = P.getCurCell().getId();
                perso.teleport(mapID, cellID);
                String str = "Le joueur " + perso.getName()
                        + " a ete teleporte vers " + P.getName() + ".";
                this.sendMessage(str);
            } else {
                String str = "Le joueur " + P.getName()
                        + " n'est pas en ligne.";
                this.sendMessage(str);
            }
            return;
        } else if (command.equalsIgnoreCase("TP")) {
            short mapID = -1;
            int cellID = -1;
            try {
                mapID = Short.parseShort(infos[1]);
            } catch (Exception e) {
                // ok
            }
            try{
                cellID = Integer.parseInt(infos[2]);
            }catch (Exception e) {
                // ok
            }

            if (mapID == -1 || Mundo.mundo.getMap(mapID) == null) {
                String str = "";
                if (mapID == -1 || Mundo.mundo.getMap(mapID) == null)
                    str = "MapID invalide.";
                this.sendMessage(str);
                return;
            }
            if (Mundo.mundo.getMap(mapID).getCase(cellID) == null) {
                cellID = Mundo.mundo.getMap(mapID).getRandomFreeCellId();
            }
            Jugador perso = this.getJugador();
            if (infos.length > 3)//Si un nom de perso est specifie
            {
                perso = Mundo.mundo.getPlayerByName(infos[3]);
                if (perso == null || perso.getPelea() != null) {
                    String str = "Le personnage n'a pas ete trouve ou est en combat";
                    this.sendMessage(str);
                    return;
                }
                if(!perso.isOnline()) {
                    perso.setCurMap(Mundo.mundo.getMap(mapID));
                    perso.setCurCell(Mundo.mundo.getMap(mapID).getCase(cellID));
                }
            }
            perso.teleport(mapID, cellID);
            String str = "Le joueur " + perso.getName() + " a ete teleporte.";
            this.sendMessage(str);
            return;
        } else if (command.equalsIgnoreCase("SIZE")) {
            int size = -1;
            try {
                size = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }
            if (size == -1) {
                String str = "Taille invalide.";
                this.sendMessage(str);
                return;
            }
            Jugador perso = this.getJugador();
            if (infos.length > 2)//Si un nom de perso est specifie
            {
                perso = Mundo.mundo.getPlayerByName(infos[2]);
                if (perso == null) {
                    String str = "Le personnage n'a pas ete trouve.";
                    this.sendMessage(str);
                    return;
                }
            }
            perso.set_size(size);
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(), perso.getId());
            GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(perso.getCurMap(), perso);
            String str = "La taille du joueur " + perso.getName()
                    + " a ete modifiee.";
            this.sendMessage(str);
            return;
        } else if (command.equalsIgnoreCase("FREEZE")) {
            Jugador perso = this.getJugador();
            if (infos.length > 1) {
                perso = Mundo.mundo.getPlayerByName(infos[1]);
            }
            if (perso == null) {
                String mess = "Le personnage n'existe pas.";
                this.sendMessage(mess);
                return;
            }
            if (perso.getBlockMovement())
                this.sendMessage("Le joueur n'est plus bloque.");
            else
                this.sendMessage("Le joueur est bloque.");
            perso.setBlockMovement(!perso.getBlockMovement());
            return;
        } else if (command.equalsIgnoreCase("BLOCKMAP")) {
            int i = -1;
            try {
                i = Short.parseShort(infos[1]);
            } catch (Exception e) {
                // ok
            }
            if (i == 0) {
                MainServidor.INSTANCE.setMapAsBlocked(false);
                this.sendMessage("Map deblocke.");
            } else if (i == 1) {
                MainServidor.INSTANCE.setMapAsBlocked(true);
                this.sendMessage("Map blocke.");
            } else {
                this.sendMessage("Aucune information.");
            }
            return;
        } else if (command.equalsIgnoreCase("BLOCKFIGHT")) {
            int i = -1;
            try {
                i = Short.parseShort(infos[1]);
            } catch (Exception e) {
                // ok
            }
            if (i == 0) {
                MainServidor.INSTANCE.setFightAsBlocked(false);
                for(Jugador player : Mundo.mundo.getOnlinePlayers())
                    player.sendServerMessage("You can't fight until new order.");
                this.sendMessage("Les combats ont etes debloques.");
            } else if (i == 1) {
                for(Jugador player : Mundo.mundo.getOnlinePlayers())
                    player.sendServerMessage("You can't fight until new order.");
                this.sendMessage("Les combats ont etes bloques.");
            } else {
                this.sendMessage("Aucune information.");
            }
            return;
        } else if (command.equalsIgnoreCase("MUTE")) {
            Jugador player;
            String name;
            short time;

            try {
                name = infos[1];

                if(name.equals("*")) {
                    ComandosJugadores.canalMute = !ComandosJugadores.canalMute;
                    this.sendSuccessMessage("The main channel has been " + (ComandosJugadores.canalMute ? "closed." : "opened."));
                    return;
                }

                time = Short.parseShort(infos[2]);
            } catch (Exception e) {
                this.sendErrorMessage("The name/time you've enter is/are invalid ! (max time : " + Short.MAX_VALUE + " in minutes)");
                return;
            }

            player = Mundo.mundo.getPlayerByName(name);

            if (player == null || time <= 0) {
                this.sendErrorMessage("The player wasn't found or the time is negative. (max time : " + Short.MAX_VALUE + " in minutes)");
                return;
            }

            if (player.getAccount() == null) {
                this.sendErrorMessage("The account of the player wasn't found, please verify the name.");
                return;
            }

            player.getAccount().mute(time, this.getJugador().getName());
            this.sendSuccessMessage("You've mute the player " + player.getName() + " for " + time + "minute(s) effective for all players of this account !");

            if (!player.isOnline())
                this.sendErrorMessage("The player is not online, are you sure it is the correct player ?");
            return;
        } else if (command.equalsIgnoreCase("MUTEIP")) {
            Jugador player;
            String name;
            short time;

            try {
                name = infos[1];
                time = Short.parseShort(infos[2]);
            } catch (Exception e) {
                this.sendErrorMessage("The name/time you've enter is/are invalid ! (max time : " + Short.MAX_VALUE + " in minutes)");
                return;
            }

            player = Mundo.mundo.getPlayerByName(name);

            if (player == null || time <= 0) {
                this.sendErrorMessage("The player wasn't found or the time is negative. (max time : " + Short.MAX_VALUE + " in minutes)");
                return;
            }

            if (player.getAccount() == null) {
                this.sendErrorMessage("The account of the player wasn't found, please verify the name.");
                return;
            }

            String ip = player.getAccount().getLastIP();

            if (ip.equalsIgnoreCase("")) {
                this.sendErrorMessage("Sorry but the server don't have any IP of this account, verify another account please.");
                return;
            }

            Mundo.mundo.getAccountsByIp(ip).values().stream().filter(account -> account != null && account.getLastIP().equalsIgnoreCase(ip)).forEach(account -> {
                account.mute(time, this.getJugador().getName());
                if (account.getCurrentPlayer() != null)
                    this.sendMessage("You've mute the account " + account.getName() + ".");
            });

            this.sendSuccessMessage("All the accounts of the IP (" + ip + ") have been mute for " + time + " minute(s) successfully !");
            return;
        } else if (command.equalsIgnoreCase("UNMUTEIP")) {
            Jugador player;
            String name;

            try {
                name = infos[1];
            } catch (Exception e) {
                this.sendErrorMessage("The name/time you've enter is/are invalid ! (max time : " + Short.MAX_VALUE + " in minutes)");
                return;
            }

            player = Mundo.mundo.getPlayerByName(name);

            if (player == null) {
                this.sendErrorMessage("The player wasn't found or the time is negative. (max time : " + Short.MAX_VALUE + " in minutes)");
                return;
            }

            if (player.getAccount() == null) {
                this.sendErrorMessage("The account of the player wasn't found, please verify the name.");
                return;
            }

            String ip = player.getAccount().getLastIP();

            if (ip.equalsIgnoreCase("")) {
                this.sendErrorMessage("Sorry but the server don't have any IP of this account, verify another account please.");
                return;
            }

            Mundo.mundo.getAccountsByIp(ip).values().stream().filter(account -> account != null && account.getLastIP().equalsIgnoreCase(ip)).forEach(account -> {
                account.unMute();
                if (account.getCurrentPlayer() != null)
                    this.sendMessage("The account " + account.getName() + " is free to talk.");
            });

            this.sendSuccessMessage("All the accounts of the IP (" + ip + ") are free to talk successfully !");
            return;
        } else if (command.equalsIgnoreCase("UNMUTE")) {
            Jugador player;
            String name;

            try {
                name = infos[1];
            } catch (Exception e) {
                this.sendErrorMessage("The name/time you've enter is/are invalid ! (max time : " + Short.MAX_VALUE + " in minutes)");
                return;
            }

            player = Mundo.mundo.getPlayerByName(name);

            if (player == null) {
                this.sendErrorMessage("The player wasn't found or the time is negative. (max time : " + Short.MAX_VALUE + " in minutes)");
                return;
            }

            if (player.getAccount() == null) {
                this.sendErrorMessage("The account of the player wasn't found, please verify the name.");
                return;
            }

            player.getAccount().unMute();
            this.sendSuccessMessage("You've unmute the player " + player.getName() + " effective for all players of this account !");

            if (!player.isOnline())
                this.sendErrorMessage("The player is not online, are you sure it is the correct player ?");
            return;
        } else if (command.equalsIgnoreCase("MUTEMAP")) {
            if (this.getJugador().getCurMap() == null)
                return;
            this.getJugador().getCurMap().mute();
            String mess = "";
            if (this.getJugador().getCurMap().isMute())
                mess = "Vous venez de muter la MAP.";
            else
                mess = "Vous venez de demuter la MAP.";
            this.sendMessage(mess);
            return;
        } else if (command.equalsIgnoreCase("KICK")) {
            /*
            1 : Tu es resté trop longtemps inactif.
            2 : Ton personnage a atteint le niveau maximum autorisé
            3 : Pour des raisons de maintenance, le serveur va être coupé d'ici quelques minutes.
            4 : Votre connexion a été coupée pour des raisons de maintenance.
            5 : Retry connection (Oui ou Non)
            6 : Le nombre d'objets pour cet inventaire est déjà atteint.
            7 : Cette opération n'est pas autorisée ici.
            8 : Cet objetn 'est plus disponible.
             */

            Jugador player;
            String name, reason = "";

            try {
                name = infos[1];
            } catch (Exception ignored) {
                this.sendErrorMessage("You need to give the name of the player !");
                return;
            }

            try {
                reason = msg.substring(infos[0].length() + infos[1].length() + 1);
            } catch (Exception ignored) {}

            player = Mundo.mundo.getPlayerByName(name);

            if (player == null) {
                this.sendErrorMessage("The name of the player is invalid or non-existent !");
                return;
            }

            if (player.isOnline()) {
                if (reason.isEmpty()) {
                    player.send("M018|" + this.getJugador().getName() + ";");
                } else {
                    player.send("M018|" + this.getJugador().getName() + ";<br>" + reason);
                }
                player.getGameClient().kick();
                this.sendSuccessMessage("The player have been kicked successfully.");
            } else {
                this.sendErrorMessage("The player isn't connected, verify the name please.");
            }
            return;
        } else if (command.equalsIgnoreCase("JAIL")) {
            short mapID = 666;
            int cellID = getCellJail();
            if (cellID == -1 || Mundo.mundo.getMap(mapID) == null) {
                String str = "MapID ou cellID invalide.";
                if (cellID == -1)
                    str = "cellID invalide.";
                else
                    str = "MapID invalide.";
                this.sendMessage(str);
                return;
            }
            if (Mundo.mundo.getMap(mapID).getCase(cellID) == null) {
                String str = "cellID invalide.";
                this.sendMessage(str);
                return;
            }
            try {
                if (infos.length > 1)//Si un nom de perso est specifie
                {
                    Jugador perso = Mundo.mundo.getPlayerByName(infos[1]);
                    if (perso.getGroupe() != null) {
                        String str = "Il est interdit d'emprisonner un personnage ayant des droits.";
                        this.sendMessage(str);
                        return;
                    }
                    if (perso.getPelea() != null) {
                        String str = "Le personnage n'a pas ete trouve ou est en combat.";
                        this.sendMessage(str);
                        return;
                    }
                    if (perso.isOnline())
                        perso.teleport(mapID, cellID);
                    else
                        perso.teleportD(mapID, cellID);
                    String str = "Le joueur " + perso.getName()
                            + " a ete teleporte emprisonne.";
                    this.sendMessage(str);
                }
            } catch (Exception e) {
                this.sendMessage("Introuvable.");
                // ok
                return;
            }
            return;
        } else if (command.equalsIgnoreCase("UNJAIL")) {
            Jugador perso = Mundo.mundo.getPlayerByName(infos[1]);
            if (perso == null || perso.getPelea() != null) {
                String str = "Le personnage n'a pas ete trouve ou est en combat.";
                this.sendMessage(str);
                return;
            }
            if (perso.isInPrison())//Si un nom de perso est specifie
            {
                perso.warpToSavePos();
                String str = "Le joueur " + perso.getName()
                        + " a ete teleporte e son point de sauvegarde.";
                this.sendMessage(str);
                return;
            }
            return;
        } else if (command.equalsIgnoreCase("BAN")) {
            Jugador player = Mundo.mundo.getPlayerByName(infos[1]);
            short days = 0;

            try {
                days = Short.parseShort(infos[2]);
            } catch(Exception ignored) {
                this.sendMessage("You've not enter a day value (the time while the account is banned), the default value is unlimited.");
            }

            if (player == null) {
                this.sendErrorMessage("The player was not found, verify the name please.");
                return;
            }
            if (player.getAccount() == null)
                Database.dinamicos.getAccountData().load(player.getAccID());
            if (player.getAccount() == null) {
                this.sendErrorMessage("The account of the player was not found, contact a supervisor.");
                return;
            }

            player.getAccount().setBanned(true);
            Database.dinamicos.getAccountData().updateBannedTime(player.getAccount(), Instant.now().toEpochMilli() + 86400000 * days);

            if (player.getPelea() == null) {
                if (player.getGameClient() != null)
                    player.getGameClient().kick();
            } else {
                GestorSalida.send(player, "Im1201;" + this.getJugador().getName());
            }
            this.sendSuccessMessage("You've kick and ban the player " + player.getName() + "(Acc: " + player.getAccount().getName() + ") for " + (days == 0 ? "unlimited" : days) + " day(s).");
            return;
        } else if (command.equalsIgnoreCase("BANACCOUNT")) {
            String mess = "Le compte est introuvable";
            String A = "";
            try {
                A = infos[1];
            } catch (Exception e) {
                // ok
            }
            if (A.equalsIgnoreCase("")) {
                this.sendMessage("Il faut le nom de compte.");
                return;
            }
            for (Cuenta account : Mundo.mundo.getCuenta()) {
                if (account == null)
                    continue;
                if (!account.getName().equalsIgnoreCase(A))
                    continue;
                account.setBanned(true);
                Database.dinamicos.getAccountData().update(account);
                mess = "Vous avez banni le compte " + A;
                Jugador p = account.getCurrentPlayer();
                if (p != null) {
                    if (p.isOnline()) {
                        mess += " dont le joueur est " + p.getName();
                        if (p.getPelea() == null) {
                            if (p.getGameClient() != null)
                                p.getGameClient().kick();
                        } else {
                            GestorSalida.send(p, "Im1201;"
                                    + this.getJugador().getName());
                        }
                    }
                }
            }
            this.sendMessage(mess
                    + ".");
            return;
        } else if (command.equalsIgnoreCase("BANBYID")) {
            int ID = -1;
            String mess = "Aucun personnage n'a ete trouve.";
            try {
                ID = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }

            if (ID <= 0) {
                this.sendMessage("Une IP est necessaire.");
                return;
            }
            for (Jugador player : Mundo.mundo.getJugador()) {
                if (player == null)
                    continue;
                if (player.getId() == ID) {
                    if (player.getAccount() == null)
                        Database.dinamicos.getAccountData().load(player.getAccID());
                    if (player.getAccount() == null) {
                        this.sendMessage("Le personnage n'a pas de compte.");
                        if (player.getGameClient() != null)
                            player.getGameClient().kick();
                        return;
                    }
                    player.getAccount().setBanned(true);
                    Database.dinamicos.getAccountData().update(player.getAccount());
                    if (player.getPelea() == null) {
                        if (player.getGameClient() != null)
                            player.getGameClient().kick();
                    } else {
                        GestorSalida.send(player, "Im1201;"
                                + this.getJugador().getName());
                    }
                    mess = "Vous avez banni " + player.getName() + ".";
                }
            }
            this.sendMessage(mess);
            return;
        } else if (command.equalsIgnoreCase("BANBYIP")) {
            String IP = "";
            try {
                IP = infos[1];
            } catch (Exception e) {
                // ok
            }

            if (IP.equalsIgnoreCase("")) {
                this.sendMessage("Une IP est necessaire.");
                return;
            }
            for (Entry<Integer, Cuenta> entry : Mundo.mundo.getAccountsByIp(IP).entrySet()) {
                Cuenta a = entry.getValue();
                if (a == null)
                    continue;
                if (!a.getLastIP().equalsIgnoreCase(IP))
                    continue;

                a.setBanned(true);
                Database.dinamicos.getAccountData().update(a);
                this.sendMessage("Le compte "
                        + a.getName() + " a ete banni.");
                if (a.isOnline()) {
                    JuegoCliente gc = a.getGameClient();
                    if (gc == null)
                        continue;
                    this.sendMessage("Le joueur "
                            + gc.getPlayer().getName() + " a ete kick.");
                    gc.kick();
                }
            }
            IntercambioCliente.INSTANCE.send("SB" + IP);
            if (Database.dinamicos.getBanIpData().add(IP))
                this.sendMessage("L'IP "
                        + IP + " a ete banni.");
            return;
        } else if (command.equalsIgnoreCase("BANIP")) {
            Jugador P = null;
            try {
                P = Mundo.mundo.getPlayerByName(infos[1]);
            } catch (Exception e) {
                // ok
            }

            if (P == null) {
                this.sendMessage("Le personnage n'a pas ete trouve.");
                return;
            }
            String IP = P.getAccount().getLastIP();
            if (IP.equalsIgnoreCase("")) {
                this.sendMessage("L'IP est invalide.");
                return;
            }
            Database.dinamicos.getBanIpData().delete(IP);
            Database.dinamicos.getBanIpData().add(IP);
            for (Entry<Integer, Cuenta> entry : Mundo.mundo.getAccountsByIp(IP).entrySet()) {
                Cuenta a = entry.getValue();
                if (a == null)
                    continue;
                if (!a.getLastIP().equalsIgnoreCase(IP))
                    continue;

                a.setBanned(true);
                Database.dinamicos.getAccountData().update(a);
                this.sendMessage("Le compte "
                        + a.getName() + " a ete banni.");
                if (a.isOnline()) {
                    JuegoCliente gc = a.getGameClient();
                    if (gc == null)
                        continue;
                    this.sendMessage("Le joueur "
                            + gc.getPlayer().getName() + " a ete kick.");
                    gc.kick();
                }
            }
            IntercambioCliente.INSTANCE.send("SB" + IP);
            if (Database.dinamicos.getBanIpData().add(IP))
                this.sendMessage("L'IP "
                        + IP + " a ete banni.");
            return;
        } else if (command.equalsIgnoreCase("SHOWITEM")) {
            Jugador perso = this.getJugador();
            String name = null;
            try {
                name = infos[1];
            } catch (Exception e) {
                // ok
            }

            perso = Mundo.mundo.getPlayerByName(name);
            if (perso == null) {
                String mess = "Le personnage n'a pas ete trouve.";
                this.sendMessage(mess);
                return;
            }
            String mess = "==========\n"
                    + "Liste d'items sur le personnage :\n";
            this.sendMessage(mess);
            for (Entry<Integer, ObjetoJuego> entry : perso.getItems().entrySet()) {
                mess = entry.getValue().getId() + " || "
                        + entry.getValue().getModelo().getName() + " || "
                        + entry.getValue().getCantidad();
                this.sendMessage(mess);
            }

            this.sendMessage("Le personnage possede : "
                    + perso.getKamas() + " Kamas.\n");
            mess = "==========";
            this.sendMessage(mess);
            return;
        } else if (command.equalsIgnoreCase("SHOWBANK")) {
            Jugador perso = this.getJugador();
            String name = null;
            try {
                name = infos[1];
            } catch (Exception e) {
                // ok
            }

            perso = Mundo.mundo.getPlayerByName(name);
            if (perso == null) {
                String mess = "Le personnage n'a pas ete trouve.";
                this.sendMessage(mess);
                return;
            }
            Cuenta cBank = perso.getAccount();
            String mess = "==========\n" + "Liste d'items dans la banque :";
            this.sendMessage(mess);
            for (ObjetoJuego entry : cBank.getBanco()) {
                mess = entry.getId() + " || "
                        + entry.getModelo().getName() + " || "
                        + entry.getCantidad();
                this.sendMessage(mess);
            }
            this.sendMessage("Le personnage possede : "
                    + cBank.getBankKamas() + " Kamas en banque.");
            mess = "==========";
            this.sendMessage(mess);
            return;
        } else if (command.equalsIgnoreCase("SHOWSTORE")) {
            Jugador perso = this.getJugador();
            String name = null;
            try {
                name = infos[1];
            } catch (Exception e) {
                // ok
            }

            perso = Mundo.mundo.getPlayerByName(name);
            if (perso == null) {
                String mess = "Le personnage n'a pas ete trouve.";
                this.sendMessage(mess);
                return;
            }
            String mess = "==========\n" + "Liste d'items dans le Store :";
            this.sendMessage(mess);
            for (Entry<Integer, Integer> obj : perso.getStoreItems().entrySet()) {
                ObjetoJuego entry = Mundo.getGameObject(obj.getKey());
                mess = entry.getId() + " || " + entry.getModelo().getName()
                        + " || " + entry.getCantidad();
                this.sendMessage(mess);
            }

            mess = "==========";
            this.sendMessage(mess);
            return;
        } else if (command.equalsIgnoreCase("SHOWMOUNT")) {
            Jugador perso = this.getJugador();
            String name = null;
            try {
                name = infos[1];
            } catch (Exception e) {
                // ok
            }

            perso = Mundo.mundo.getPlayerByName(name);
            if (perso == null) {
                String mess = "Le personnage n'a pas ete trouve.";
                this.sendMessage(mess);
                return;
            }
            String mess = "==========\n" + "Liste d'items dans la banque :";
            this.sendMessage(mess);
            if(perso.getMount() != null) {
                for (Entry<Integer, ObjetoJuego> entry : perso.getMount().getObjects().entrySet()) {
                    mess = entry.getValue().getId() + " || "
                            + entry.getValue().getModelo().getName() + " || "
                            + entry.getValue().getCantidad();
                    this.sendMessage(mess);
                }
            }
            mess = "==========";
            this.sendMessage(mess);
            return;
        } else if (command.equalsIgnoreCase("BLOCKTRADE")) {
            int i = -1;
            try {
                i = Short.parseShort(infos[1]);
            } catch (Exception e) {
                // ok
            }

            if (i == 0) {
                MainServidor.INSTANCE.setTradeAsBlocked(false);
                this.sendMessage("Les échanges ont été débloqués.");
            } else if (i == 1) {
                MainServidor.INSTANCE.setTradeAsBlocked(true);
                this.sendMessage("Tous les échanges sont bloqués.");
            } else {
                this.sendMessage("Aucune information.");
            }
            return;
        } else if (command.equalsIgnoreCase("ERASEALLMAP")) {
            for (Mapa map : Mundo.mundo.getMapa())
                map.delAllDropItem();
            this.sendMessage("Tous les objets sur toutes les maps ont été supprimés.");
            return;
        } else if (command.equalsIgnoreCase("ERASEMAP")) {
            this.getJugador().getCurMap().delAllDropItem();
            this.sendMessage("Les objets de la map ont été supprimés.");
            return;
        } else if (command.equalsIgnoreCase("MORPH")) {
            int morphID = -9;
            try {
                morphID = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }

            if (morphID == -9) {
                String str = "MorphID invalide.";
                this.sendMessage(str);
                return;
            }
            Jugador target = this.getJugador();
            if (infos.length > 2)//Si un nom de perso est specifie
            {
                target = Mundo.mundo.getPlayerByName(infos[2]);
                if (target == null) {
                    String str = "Le personnage n'a pas ete trouve.";
                    this.sendMessage(str);
                    return;
                }
            }
            if (morphID == -1) {
                morphID = target.getClasse() * 10 + target.getSexe();
                target.setGfxId(morphID);
                GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.getCurMap(), target.getId());
                GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(target.getCurMap(), target);
                String str = "Le joueur " + target.getName()
                        + " a son apparence originale.";
                this.sendMessage(str);
            } else {
                target.setGfxId(morphID);
                GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.getCurMap(), target.getId());
                GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(target.getCurMap(), target);
                String str = "Le joueur " + target.getName()
                        + " a ete transforme.";
                this.sendMessage(str);
            }
            return;
        } else if (command.equalsIgnoreCase("DEMORPHALL")) {
            for (Jugador player : Mundo.mundo.getOnlinePlayers()) {
                player.setGfxId(player.getClasse() * 10 + player.getSexe());
            }
            this.sendMessage("Tous les joueurs connectes ont leur apparence originale.");
            return;
        } else if (command.equalsIgnoreCase("ADDHONOR")) {
            int honor = 0;
            try {
                honor = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }

            Jugador perso = this.getJugador();
            if (infos.length > 2)//Si un nom de perso est specifie
            {
                perso = Mundo.mundo.getPlayerByName(infos[2]);
                if (perso == null) {
                    String str = "Le personnage n'a pas ete trouve.";
                    this.sendMessage(str);
                    return;
                }
            }
            String str = "Vous avez ajoute " + honor + " points d'honneur e "
                    + perso.getName() + ".";
            if (perso.get_align() != Constantes.ALINEAMIENTO_MERCENARIO) {
                str = "Le joueur n'est pas mercenaire ... l'action a ete annulee.";
                this.sendMessage(str);
                return;
            }
            perso.addHonor(honor);
            this.sendMessage(str);
            return;
        } else if (command.equalsIgnoreCase("HONOR")) {
            int honor = 0;
            try {
                honor = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }

            Jugador perso = this.getJugador();
            if (infos.length > 2)//Si un nom de perso est specifie
            {
                perso = Mundo.mundo.getPlayerByName(infos[2]);
                if (perso == null) {
                    String str = "Le personnage n'a pas ete trouve";
                    this.sendMessage(str);
                    return;
                }
            }
            String str = "Vous avez ajoute " + honor + " points d'honneur e "
                    + perso.getName() + ".";
            if (perso.get_align() == Constantes.ALINEAMIENTO_NEUTRAL) {
                str = "Le joueur est neutre ... l'action a ete annulee.";
                this.sendMessage(str);
                return;
            }
            perso.addHonor(honor);
            this.sendMessage(str);
            return;
        } else if (command.equalsIgnoreCase("NOAGRO")) {
            Jugador perso = this.getJugador();
            String name = null;
            try {
                name = infos[1];
            } catch (Exception e) {
                // ok
            }

            perso = Mundo.mundo.getPlayerByName(name);
            if (perso == null) {
                String mess = "Le personnage n'existe pas.";
                this.sendMessage(mess);
                return;
            }
            perso.setCanAggro(!perso.canAggro());
            String mess = perso.getName();
            if (perso.canAggro())
                mess += " peut maintenant etre aggresse.";
            else
                mess += " ne peut plus etre agresse.";
            this.sendMessage(mess);
            if (!perso.isOnline()) {
                mess = "Le personnage " + perso.getName()
                        + " n'etait pas connecte.";
                this.sendMessage(mess);
            }
            return;
        } else if (command.equalsIgnoreCase("WHOIS")) {
            String name = "";
            Jugador perso = null;
            try {
                name = infos[1];
            } catch (Exception e) {
                // ok
            }

            if (name.equals(""))
                return;

            perso = Mundo.mundo.getPlayerByName(name);
            if (perso == null) {
                String mess = "Le personnage n'existe pas.";
                this.sendMessage(mess);
                return;
            } else if (perso.getAccount().getLastIP().equalsIgnoreCase("")) {
                String mess = "Aucune IP.";
                this.sendMessage(mess);
                return;
            }
            java.util.Map<Integer, Cuenta> accounts = Mundo.mundo.getAccountsByIp(perso.getAccount().getLastIP());
            StringBuilder mess = new StringBuilder("Whois sur le joueur : " + name + "\n");
            mess.append("Derniere IP : ").append(perso.getAccount().getLastIP()).append("\n");
            int i = 1;
            for (Entry<Integer, Cuenta> entry : accounts.entrySet()) {
                StringBuilder persos = new StringBuilder();
                Cuenta a = entry.getValue();
                if (a == null)
                    continue;
                for (Entry<Integer, Jugador> entry2 : a.getPlayers().entrySet()) {
                    perso = entry2.getValue();
                    if (perso != null) {
                        if (persos.toString().equalsIgnoreCase(""))
                            persos.append(perso.getName()).append((perso.getGroupe() != null) ? ":"
                                    + perso.getGroupe().getNombre() : "");
                        else
                            persos.append(", ").append(perso.getName()).append((perso.getGroupe() != null) ? ":"
                                    + perso.getGroupe().getNombre() : "");
                    }
                }
                if (!persos.toString().equalsIgnoreCase("")) {
                    mess.append("[").append(i).append("] ").append(a.getName()).append(" - ").append(persos).append((a.isBanned()) ? " : banni" : "").append("\n");
                    i++;
                }
            }
            this.sendMessage(mess.toString());
            return;
        } else if (command.equalsIgnoreCase("CLEANFIGHT")) {
            this.getJugador().getCurMap().getFights().clear();
            this.sendMessage("Tous les combats de la map ont etes supprimes.");
            return;
        } else if (command.equalsIgnoreCase("ETATSERVER")) {
            int etat = 1;
            try {
                etat = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }
            JuegoServidor.INSTANCE.setState(etat);
            this.sendMessage("Vous avez change l'etat du serveur en "
                    + etat + ".");
            return;
        } else if (command.equalsIgnoreCase("MPTOTP")) {
            this.getJugador().mpToTp = !this.getJugador().mpToTp;
            String mess = "";
            if (this.getJugador().mpToTp)
                mess = "Vous venez d'activer le MP to TP.";
            else
                mess = "Vous venez de desactiver le MP to TP.";
            this.sendMessage(mess);
            return;
        } else if (command.equalsIgnoreCase("RETURNTP")) {
            for (Jugador perso : Mundo.mundo.getOnlinePlayers()) {
                if (perso.thatMap == -1 || perso.getPelea() != null)
                    continue;
                perso.teleport((short) perso.thatMap, perso.thatCell);
                perso.thatMap = -1;
                perso.thatCell = -1;
            }
            this.sendMessage("Vous venez de renvoyer tous les joueurs e leur ancienne position.");
            return;
        } else if (command.equalsIgnoreCase("GETCASES")) {
            if (this.getJugador().getCases) {
                this.sendMessage("Le getCases viens d'etre disable :");
                StringBuilder i = new StringBuilder();
                for (Integer c : this.getJugador().thisCases)
                    i.append(";").append(c);
                this.sendMessage(i.substring(1));
                this.getJugador().thisCases.clear();
            } else
                this.sendMessage("Le getCases viens d'etre active. Deplacez-vous sur la map pour capturer les cellules.");
            this.getJugador().getCases = !this.getJugador().getCases;
            return;
        } else if (command.equalsIgnoreCase("WALKFAST")) {
            if (this.getJugador().walkFast)
                this.sendMessage("La marche instantanne viens d'etre disable.");
            else
                this.sendMessage("La marche instantanne viens d'etre active.");
            this.getJugador().walkFast = !this.getJugador().walkFast;
            return;
        } else if (command.equalsIgnoreCase("LISTMAP")) {
            StringBuilder data = new StringBuilder();
            ArrayList<Mapa> i = Mundo.mundo.getMapByPosInArray(this.getJugador().getCurMap().getX(), this.getJugador().getCurMap().getY());
            for (Mapa map : i)
                data.append(map.getId()).append(" | ");
            this.sendMessage(data.toString());
            return;
        } else if (command.equalsIgnoreCase("DELINVENTORY")) {
            Jugador perso = null;
            infos = msg.split(" ", 3);
            try {
                perso = Mundo.mundo.getPlayerByName(infos[1]);
            } catch (Exception e) {
                // ok
            }

            if (perso == null) {
                this.sendMessage("Le nom du personnage est incorrect.");
                return;
            }
            int i = 0;

            ArrayList<ObjetoJuego> list = new ArrayList<>(perso.getItems().values());
            for (ObjetoJuego obj : list) {
                int guid = obj.getId();
                GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(perso, guid);
                perso.deleteItem(guid);
                i++;
            }

            this.sendMessage("Vous venez de supprimer "
                    + i + " objets au joueur " + perso.getName() + ".");
            return;
        } else if (command.equalsIgnoreCase("RMOBS")) {
            this.getJugador().getCurMap().refreshSpawns();
            String mess = "Les spawns de monstres sur la map ont etes rafraichit.";
            this.sendMessage(mess);
            return;
        } else if (command.equalsIgnoreCase("DELJOB")) {
            Jugador perso = this.getJugador();
            infos = msg.split(" ", 3);
            int job = -1;
            try {
                job = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }

            try {
                perso = Mundo.mundo.getPlayerByName(infos[2]);
            } catch (Exception e) {
                // ok
            }

            if (perso == null) {
                this.sendMessage("Le nom du personnage est incorrect.");
                return;
            }
            if (job < 1)
                return;
            OficioCaracteristicas jobStats = perso.getMetierByID(job);
            if (jobStats == null)
                return;
            perso.unlearnJob(jobStats.getId());
            GestorSalida.GAME_SEND_STATS_PACKET(perso);
            Database.dinamicos.getPlayerData().update(perso);
            GestorSalida.GAME_SEND_MESSAGE(perso, "Vous venez de désapprendre un métier, veuillez vous reconnecter.");
            this.sendMessage("Vous avez supprimé le métier "
                    + job + " sur le personnage " + perso.getName() + ".");
            return;
        } else if (command.equalsIgnoreCase("ADDTRIGGER")) {
            String args = "";
            try {
                args = infos[1];
            } catch (Exception e) {
                // ok
            }

            if (args.equals("")) {
                String str = "Valeur invalide.";
                this.sendMessage(str);
                return;
            }

            this.getJugador().getCurCell().addOnCellStopAction(0, args, "-1", null);
            boolean success = Database.estaticos.getScriptedCellData().update(this.getJugador().getCurMap().getId(), this.getJugador().getCurCell().getId(), 0, 1, args, "-1");
            String str = "";
            if (success)
                str = "Le trigger a ete ajoute.";
            else
                str = "Le trigger n'a pas ete ajoute.";
            this.sendMessage(str);
            return;
        } else if (command.equalsIgnoreCase("DELTRIGGER")) {
            int cellID = -1;
            try {
                cellID = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }

            if (cellID == -1
                    || this.getJugador().getCurMap().getCase(cellID) == null) {
                String str = "CellID invalide.";
                this.sendMessage(str);
                return;
            }
            this.getJugador().getCurMap().getCase(cellID).clearOnCellAction();
            boolean success = Database.estaticos.getScriptedCellData().delete(this.getJugador().getCurMap().getId(), cellID);
            String str = "";
            if (success)
                str = "Le trigger a ete retire.";
            else
                str = "Le trigger n'a pas ete retire.";
            this.sendMessage(str);
            return;
        } else if (command.equalsIgnoreCase("SAVETHAT")) {
            this.getJugador().thatMap = this.getJugador().getCurMap().getId();
            this.getJugador().thatCell = this.getJugador().getCurCell().getId();
            this.sendMessage("Vous avez sauvegarde la map "
                    + this.getJugador().thatMap
                    + " et la cellule "
                    + this.getJugador().thatCell + ".");
            return;
        } else if (command.equalsIgnoreCase("APPLYTHAT")) {
            if (this.getJugador().thatMap == -1 || this.getJugador().thatCell == -1) {
                this.sendMessage("Impossible d'ajouter le trigger, veuillez utiliser la commande SAVETHAT avant.");
                return;
            }
            this.getJugador().getCurCell().addOnCellStopAction(0, this.getJugador().thatMap + "," + this.getJugador().thatCell, "-1", null);
            Database.estaticos.getScriptedCellData().update(this.getJugador().getCurMap().getId(), this.getJugador().getCurCell().getId(), 0, 1, this.getJugador().thatMap + "," + this.getJugador().thatCell, "-1");
            this.sendMessage("REPLACE INTO `scripted_cells` VALUES ('" + this.getJugador().getCurMap().getId() + "', '" + this.getJugador().getCurCell().getId() + "','0','1','" + this.getJugador().thatMap + "," + this.getJugador().thatCell + "','-1');" +
                    "\nVous avez applique le trigger.");
            this.getJugador().thatMap = -1;
            this.getJugador().thatCell = -1;

            return;
        } else if (command.equalsIgnoreCase("STRIGGER")) {
            this.getJugador().thatMap = this.getJugador().getCurMap().getId();
            this.getJugador().thatCell = this.getJugador().getCurCell().getId();
            this.sendMessage("Vous avez sauvegarde la map "
                    + this.getJugador().thatMap
                    + " et la cellule "
                    + this.getJugador().thatCell + ".");
            return;
        } else if (command.equalsIgnoreCase("APTRIGGER")) {
            if (this.getJugador().thatMap == -1
                    || this.getJugador().thatCell == -1) {
                this.sendMessage("Impossible d'ajouter le trigger, veuillez utiliser la commande STRIGGER avant.");
                return;
            }
            Mundo.mundo.getMap((short) this.getJugador().thatMap).getCase(this.getJugador().thatCell).addOnCellStopAction(0, this.getJugador().getCurMap().getId()
                    + "," + this.getJugador().getCurCell().getId(), "-1", null);
            Database.estaticos.getScriptedCellData().update(this.getJugador().thatMap, this.getJugador().thatCell, 0, 1, this.getJugador().getCurMap().getId()
                    + "," + this.getJugador().getCurCell().getId(), "-1");
            this.getJugador().thatMap = -1;
            this.getJugador().thatCell = -1;
            this.sendMessage("Vous avez applique le trigger.");
            return;
        } else if (command.equalsIgnoreCase("INFOS")) {
            long uptime = Instant.now().toEpochMilli() - Configuracion.INSTANCE.getStartTime();
            int day = (int) (uptime / (1000 * 3600 * 24));
            uptime %= (1000 * 3600 * 24);
            int hour = (int) (uptime / (1000 * 3600));
            uptime %= (1000 * 3600);
            int min = (int) (uptime / (1000 * 60));
            uptime %= (1000 * 60);
            int sec = (int) (uptime / (1000));

            StringBuilder message = new StringBuilder("\n<u><b>Informacion de sistema global PrivatEMU:</b></u>\n\n<u>Uptime :</u> " + day + "j " + hour + "h " + min + "m " + sec + "s.\n");
            message.append("Jugadores Online: ").append(JuegoServidor.getClients().size()).append("\n");
            message.append("IP Unicas conectadas: ").append(JuegoServidor.getPlayersNumberByIp()).append("\n");
            message.append("Clientes Online: ").append(JuegoServidor.getClients().size()).append("\n");


            int mb = 1024 * 1024;
            Runtime instance = Runtime.getRuntime();

            message.append("\n<u>Utilizacion de estadisticas:</u>");
            message.append("\nRAM Total: ").append(instance.totalMemory() / mb).append(" Mo.");
            message.append("\nRAM Libre: ").append(instance.freeMemory() / mb).append(" Mo.");
            message.append("\nRAM Usada: ").append((instance.totalMemory() - instance.freeMemory()) / mb).append(" Mo.");
            message.append("\nRAM Maxima: ").append(instance.maxMemory() / mb).append(" Mo.");
            message.append("\n\n<u>Procesadores disponibles:</u> ").append(instance.availableProcessors());
            Set<Thread> list = Thread.getAllStackTraces().keySet();
            int news = 0, running = 0, blocked = 0, waiting = 0, sleeping = 0, terminated = 0;
            for(Thread thread : list) {
                switch (thread.getState()) {
                    case NEW, TERMINATED -> news++;
                    case RUNNABLE -> running++;
                    case BLOCKED -> blocked++;
                    case WAITING -> waiting++;
                    case TIMED_WAITING -> sleeping++;
                }
            }

            message.append("\n\n<u>Informations of ").append(list.size()).append(" threads :</u> ");
            message.append("\nNEW           : ").append(news);
            message.append("\nRUNNABLE      : ").append(running);
            message.append("\nBLOCKED       : ").append(blocked);
            message.append("\nWAITING       : ").append(waiting);
            message.append("\nTIMED_WAITING : ").append(sleeping);
            message.append("\nTERMINATED    : ").append(terminated);

            this.sendMessage(message + "\n");

            if(infos.length > 1) {
                message = new StringBuilder("List of all threads :\n");
                for(Thread thread : list)
                    message.append("- ").append(thread.getId()).append(" -> ").append(thread.getName()).append(" -> ").append(thread.getState().name().toUpperCase()).append(thread.isDaemon() ? " (Daemon)" : "").append(".\n");
                this.sendMessage(message.toString());
            }
            return;
        } else if (command.equalsIgnoreCase("STARTFIGHT")) {
            if (this.getJugador().getPelea() == null) {
                this.sendMessage("Vous devez etre dans un combat.");
                return;
            }
            this.getJugador().getPelea().startFight();
            this.sendMessage("Le combat a ete demarre.");
            return;
        } else if (command.equalsIgnoreCase("ENDFIGHT")) {
            if (this.getJugador().getPelea() == null) {
                this.sendMessage("Le combat n'existe pas.");
                return;
            }
            int i = -1;
            try {
                i = Short.parseShort(infos[1]);
            } catch (Exception e) {
                // ok
            }

            if (i == 0) {
                this.getJugador().getPelea().endFight(false);
                this.sendMessage("L'equipe des joueurs meurent !");
            } else if (i == 1) {
                this.getJugador().getPelea().endFight(true);
                this.sendMessage("L'equipe des monstres meurent !");
            } else {
                this.sendMessage("Aucune information.");
            }
            return;
        } else if (command.equalsIgnoreCase("ENDFIGHTALL")) {
            try {
                for (JuegoCliente client : JuegoServidor.getClients()) {
                    Jugador player = client.getPlayer();
                    if (player == null)
                        continue;
                    Pelea f = player.getPelea();
                    if (f == null)
                        continue;
                    try {
                        if (f.getLaunchTime() > 1)
                            continue;
                        f.endFight(true);
                        this.sendMessage("Le combat de "
                                + player.getName() + " a ete termine.");
                    } catch (Exception e) {
                        // ok
                        this.sendMessage("Le combat de "
                                + player.getName() + " a deje ete termine.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.sendMessage("Erreur lors de la commande endfightall : "
                        + e.getMessage() + ".");
            } finally {
                this.sendMessage("Tous les combats ont ete termines.");
            }
            return;
        } else if (command.equalsIgnoreCase("MAPINFO")) {
            String mess = "==========\n" + "Liste des PNJs de la Map :";
            this.sendMessage(mess);
            Mapa map = this.getJugador().getCurMap();
            for (Entry<Integer, Npc> entry : map.getNpcs().entrySet()) {
                mess = entry.getKey()
                        + " | "
                        + entry.getValue().getTemplate().getNombre()
                        + " | "
                        + entry.getValue().getTemplate().getId()
                        + " | "
                        + entry.getValue().getCellId()
                        + " | "
                        + entry.getValue().getTemplate().getInitQuestionId(this.getJugador().getCurMap().getId());
                this.sendMessage(mess);
            }
            mess = "Liste des groupes de monstres :";
            this.sendMessage(mess);
            for (Entry<Integer, MobGroup> entry : map.getMobGroups().entrySet()) {
                mess = entry.getKey() + " | " + entry.getValue().getCellId()
                        + " | " + entry.getValue().getAlignement() + " | "
                        + entry.getValue().getMobs().size();
                this.sendMessage(mess);
            }
            mess = "==========";
            this.sendMessage(mess);
            return;
        } else if (command.equalsIgnoreCase("UNBANIP")) {
            Jugador perso = null;
            try {
                perso = Mundo.mundo.getPlayerByName(infos[1]);
            } catch (Exception e) {
                // ok
            }
            if (perso == null) {
                this.sendMessage("Le nom du personnage n'est pas bon.");
                return;
            }
            if (Database.dinamicos.getBanIpData().delete(perso.getAccount().getCurrentIp())) {
                this.sendMessage("L'IP a ete debanni.");
                return;
            }
            return;
        } else if (command.equalsIgnoreCase("UNBAN")) {
            Jugador P = Mundo.mundo.getPlayerByName(infos[1]);
            if (P == null) {
                this.sendMessage("Personnage non trouve.");
                return;
            }
            if (P.getAccount() == null)
                Database.dinamicos.getAccountData().load(P.getAccID());
            if (P.getAccount() == null) {
                this.sendMessage("Le personnage n'a pas de compte.");
                return;
            }
            P.getAccount().setBanned(false);
            Database.dinamicos.getAccountData().update(P.getAccount());
            this.sendMessage("Vous avez debanni "
                    + P.getName() + ".");
            return;
        } else if (command.equalsIgnoreCase("EXIT")) {
            this.sendMessage("Lancement du reboot.");
            MainServidor.INSTANCE.getRunnables().add(() -> MainServidor.INSTANCE.stop("Exit by administrator"));
            return;
        } else  if (command.equalsIgnoreCase("SETMAX")) {
            short i = Short.parseShort(infos[1]);
            this.sendMessage("Le maximum de joueur a été fixer à : " + Configuracion.INSTANCE.getMaxonline());
            return;
        } else if (command.equalsIgnoreCase("SAVE") && !Configuracion.INSTANCE.isSaving()) {
            MundoGuardado.cast(1);
            String mess = "Sauvegarde lancee!";
            this.sendMessage(mess);
            return;
        } else if (command.equalsIgnoreCase("LEVEL")) {
            int count = 0;
            try {
                count = Integer.parseInt(infos[1]);
                if (count < 1)
                    count = 1;
                if (count > Mundo.mundo.getExpLevelSize())
                    count = Mundo.mundo.getExpLevelSize();
                Jugador perso = this.getJugador();
                if (infos.length == 3)//Si le nom du perso est specifie
                {
                    String name = infos[2];
                    perso = Mundo.mundo.getPlayerByName(name);
                    if (perso == null)
                        perso = this.getJugador();
                }
                if (perso.getLevel() < count) {

                    while (perso.getLevel() < count)
                        perso.levelUp(false, true);
                    GestorSalida.GAME_SEND_NEW_LVL_PACKET(perso.getGameClient(), perso.getLevel());
                }
                String mess = "Vous avez fixe le niveau de " + perso.getName() + " e " + count + ".";
                this.sendMessage(mess);
            } catch (Exception e) {
                // ok
                this.sendMessage("Valeur incorecte.");
                return;
            }
            return;
        } else if (command.equalsIgnoreCase("KAMAS")) {
            int count = 0;
            try {
                count = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
                this.sendMessage("Valeur incorecte.");
                return;
            }
            if (count == 0) {
                this.sendMessage("Valeur inutile.");
                return;
            }
            Jugador perso = this.getJugador();
            if (infos.length == 3)//Si le nom du perso est specifie
            {
                String name = infos[2];
                perso = Mundo.mundo.getPlayerByName(name);
                if (perso == null)
                    perso = this.getJugador();
            }
            long curKamas = perso.getKamas();
            long newKamas = curKamas + count;
            if (newKamas < 0)
                newKamas = 0;
            if (newKamas > 1000000000)
                newKamas = 1000000000;
            perso.setKamas(newKamas);
            if (perso.isOnline())
                GestorSalida.GAME_SEND_STATS_PACKET(perso);
            String mess = "Vous avez ";
            mess += (count < 0 ? "retire" : "ajoute") + " ";
            mess += Math.abs(count) + " kamas e " + perso.getName() + ".";
            this.sendMessage(mess);
            return;
        } else if (command.equalsIgnoreCase("ITEMSET")) {
            int tID = 0;
            try {
                tID = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }

            ObjetoSet IS = Mundo.mundo.getItemSet(tID);
            if (tID == 0 || IS == null) {
                String mess = "La panoplie " + tID + " n'existe pas.";
                this.sendMessage(mess);
                return;
            }
            boolean useMax = false;
            if (infos.length == 3)
                useMax = infos[2].equals("MAX");//Si un jet est specifie

            for (ObjetoModelo t : IS.getItemTemplates()) {
                ObjetoJuego obj = t.createNewItem(1, useMax);
                if (this.getJugador().addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
                    Mundo.addGameObject(obj, true);
            }
            String str = "Creation de la panoplie " + tID + " reussie";
            if (useMax)
                str += " avec des stats maximums";
            str += ".";
            this.sendMessage(str);
            return;
        } else if (command.equalsIgnoreCase("ITEM") || command.equalsIgnoreCase("!getitem")) {
            int tID = 0;
            try {
                tID = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }

            if (tID == 0) {
                String mess = "Le template " + tID + " n'existe pas.";
                this.sendMessage(mess);
                return;
            }
            int qua = 1;
            if (infos.length == 3)//Si une quantite est specifiee
            {
                try {
                    qua = Integer.parseInt(infos[2]);
                } catch (Exception e) {
                    // ok
                }
            }
            boolean useMax = false;
            if (infos.length == 4)//Si un jet est specifie
            {
                if (infos[3].equalsIgnoreCase("MAX"))
                    useMax = true;
            }
            ObjetoModelo t = Mundo.mundo.getObjetoModelo(tID);
            if (t == null) {
                String mess = "Le template " + tID + " n'existe pas.";
                this.sendMessage(mess);
                return;
            }
            if (t.getType() == Constantes.ITEM_TYPE_OBJET_ELEVAGE
                    && (t.getStrTemplate().isEmpty() || t.getStrTemplate().equalsIgnoreCase(""))) {
                this.sendMessage("Impossible de creer l'item d'elevage. Le StrTemplate ("
                        + tID + ") est vide.");
                return;
            }
            if (qua < 1)
                qua = 1;
            ObjetoJuego obj = t.createNewItem(qua, useMax);

            if(t.getType() == Constantes.ITEM_TYPE_CERTIF_MONTURE) {
                //obj.setMountStats(this.getPlayer(), null);
                Montura mount = new Montura(Constantes.getMountColorByParchoTemplate(obj.getModelo().getId()), this.getJugador().getId(), false);
                obj.clearStats();
                obj.getCaracteristicas().addOneStat(995, - (mount.getId()));
                obj.getTxtStat().put(996, this.getJugador().getName());
                obj.getTxtStat().put(997, mount.getName());
                mount.setToMax();
            }
            if (this.getJugador().addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
                Mundo.addGameObject(obj, true);
            String str = "Creation de l'item " + tID + " reussie";
            if (useMax)
                str += " avec des stats maximums";
            str += ".";
            this.sendMessage(str);
            GestorSalida.GAME_SEND_Ow_PACKET(this.getJugador());
            return;
        } else if (command.equalsIgnoreCase("SPELLPOINT")) {
            int pts = -1;
            try {
                pts = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }

            if (pts == -1) {
                String str = "Valeur invalide.";
                this.sendMessage(str);
                return;
            }
            Jugador perso = this.getJugador();
            if (infos.length > 2)//Si un nom de perso est specifie
            {
                perso = Mundo.mundo.getPlayerByName(infos[2]);
                if (perso == null) {
                    String str = "Le personnage n'a pas ete trouve.";
                    this.sendMessage(str);
                    return;
                }
            }
            perso.addSpellPoint(pts);
            GestorSalida.GAME_SEND_STATS_PACKET(perso);
            String str = "Vous avez ajoute " + pts + " points de sorts e "
                    + perso.getName() + ".";
            this.sendMessage(str);
            return;
        } else if(command.equalsIgnoreCase("UTILITY")) {
            switch(infos[1].toUpperCase()) {
                case "COLLECTOR":
                    if ("GET".equals(infos[2].toUpperCase())) {
                        Recaudador collector = Mundo.mundo.getCollector(Integer.parseInt(infos[3]));
                        if (collector == null || collector.getInFight() > 0 || collector.getExchange() || collector.getMap() != this.getJugador().getCurMap().getId())
                            return;
                        collector.setExchange(true);
                        GestorSalida.GAME_SEND_ECK_PACKET(this.getCliente(), 8, collector.getId() + "");
                        GestorSalida.GAME_SEND_ITEM_LIST_PACKET_PERCEPTEUR(this.getCliente(), collector);
                        this.getJugador().setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.TRADING_WITH_COLLECTOR, collector.getId()));
                        this.getJugador().DialogTimer();
                    } else {
                        final StringBuilder message = new StringBuilder("All id of collectors present on the map " + this.getJugador().getCurMap().getId() + " :<br>");
                        Mundo.mundo.getCollectors().values().stream().filter(collector1 -> collector1.getMap() == this.getJugador().getCurMap().getId()).forEach(collector1 ->
                                message.append("> ").append(collector1.getId()).append(" | ").append(collector1.getDate()).append(" | ")
                                        .append(Mundo.mundo.getGuild(collector1.getGuildId()) != null ? Mundo.mundo.getGuild(collector1.getGuildId()).getName() : "Unknow").append("<br>"));
                        this.sendMessage(message.toString());
                    }
                    break;
                case "RECEIVE":
                    try {
                        this.getJugador().getGameClient().parsePacket(infos[2]);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        this.sendErrorMessage("You've fail the structure of the command. Please retry.");
                        break;
                    }
                    this.sendSuccessMessage("You send to server this packet : " + infos[2]);
                    break;
                case "DEBUG":

                    break;
            }
        } else if (command.equalsIgnoreCase("LSPELL")) {
            int spell = -1;
            try {
                spell = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }

            if (spell == -1) {
                String str = "Valeur invalide.";
                this.sendMessage(str);
                return;
            }
            Jugador perso = this.getJugador();
            if (infos.length > 2)//Si un nom de perso est specifie
            {
                perso = Mundo.mundo.getPlayerByName(infos[2]);
                if (perso == null) {
                    String str = "Le personnage n'a pas ete trouve.";
                    this.sendMessage(str);
                    return;
                }
            }
            perso.learnSpell(spell, 1, true, true, true);
            String str = "Le sort " + spell + " a ete appris e "
                    + perso.getName() + ".";
            this.sendMessage(str);
            return;
        } else if (command.equalsIgnoreCase("CAPITAL")) {
            int pts = -1;
            try {
                pts = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }

            if (pts == -1) {
                String str = "Valeur invalide.";
                this.sendMessage(str);
                return;
            }
            Jugador perso = this.getJugador();
            if (infos.length > 2)//Si un nom de perso est specifie
            {
                perso = Mundo.mundo.getPlayerByName(infos[2]);
                if (perso == null) {
                    String str = "Le personnage n'a pas ete trouve.";
                    this.sendMessage(str);
                    return;
                }
            }
            perso.addCapital(pts);
            GestorSalida.GAME_SEND_STATS_PACKET(perso);
            String str = "Vous avez ajoute " + pts + " points de capital e "
                    + perso.getName() + ".";
            this.sendMessage(str);
            return;
        } else if (command.equalsIgnoreCase("ALIGN")) {
            byte align = -1;
            try {
                align = Byte.parseByte(infos[1]);
            } catch (Exception e) {
                // ok
            }

            if (align < Constantes.ALINEAMIENTO_NEUTRAL
                    || align > Constantes.ALINEAMIENTO_MERCENARIO) {
                String str = "Valeur invalide.";
                this.sendMessage(str);
                return;
            }
            Jugador perso = this.getJugador();
            if (infos.length > 2)//Si un nom de perso est specifie
            {
                perso = Mundo.mundo.getPlayerByName(infos[2]);
                if (perso == null) {
                    String str = "Le personnage n'a pas ete trouve.";
                    this.sendMessage(str);
                    return;
                }
            }
            perso.modifAlignement(align);
            String a = "";
            if (align == 0)
                a = "neutre";
            else if (align == 1)
                a = "bontarien";
            else if (align == 2)
                a = "brakmarien";
            else if (align == 3)
                a = "serianne";
            String str = "L'alignement du joueur a ete modifie en " + a + ".";
            this.sendMessage(str);
            return;
        } else if (command.equalsIgnoreCase("LIFE")) {
            int count = 0;
            try {
                count = Integer.parseInt(infos[1]);
                if (count < 0)
                    count = 0;
                if (count > 100)
                    count = 100;
                Jugador perso = this.getJugador();
                if (infos.length == 3)//Si le nom du perso est specifie
                {
                    String name = infos[2];
                    perso = Mundo.mundo.getPlayerByName(name);
                    if (perso == null)
                        perso = this.getJugador();
                }
                int newPDV = perso.getMaxPdv() * count / 100;
                perso.setPdv(newPDV);
                if (perso.isOnline())
                    GestorSalida.GAME_SEND_STATS_PACKET(perso);
                String mess = "Vous avez fixe le pourcentage de vitalite de "
                        + perso.getName() + " e " + count + "%.";
                this.sendMessage(mess);
            } catch (Exception e) {
                // ok
                this.sendMessage("Valeur incorecte.");
                return;
            }
            return;
        } else if (command.equalsIgnoreCase("XPJOB")) {
            int job = -1;
            int xp = -1;
            try {
                job = Integer.parseInt(infos[1]);
                xp = Integer.parseInt(infos[2]);
            } catch (Exception e) {
                // ok
            }

            if (job == -1 || xp < 0) {
                String str = "Valeurs invalides.";
                this.sendMessage(str);
                return;
            }
            Jugador perso = this.getJugador();
            if (infos.length > 3)//Si un nom de perso est specifie
            {
                perso = Mundo.mundo.getPlayerByName(infos[3]);
                if (perso == null) {
                    String str = "Le personnage n'a pas ete trouv.e";
                    this.sendMessage(str);
                    return;
                }
            }
            OficioCaracteristicas SM = perso.getMetierByID(job);
            if (SM == null) {
                String str = "Le joueur ne possede pas le metier demande.";
                this.sendMessage(str);
                return;
            }
            SM.addXp(perso, xp);
            ArrayList<OficioCaracteristicas> SMs = new ArrayList<>();
            SMs.add(SM);
            GestorSalida.GAME_SEND_JX_PACKET(perso, SMs);
            String str = "Vous avez ajoute " + xp
                    + " points d'experience au metier " + job + " de "
                    + perso.getName() + ".";
            this.sendMessage(str);
            return;
        } else if (command.equalsIgnoreCase("LJOB")) {
            int job = -1;
            try {
                job = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }

            if (job == -1 || Mundo.mundo.getMetier(job) == null) {
                String str = "Valeur invalide.";
                this.sendMessage(str);
                return;
            }
            Jugador perso = this.getJugador();
            if (infos.length > 2)//Si un nom de perso est specifie
            {
                perso = Mundo.mundo.getPlayerByName(infos[2]);
                if (perso == null) {
                    String str = "Le personnage n'a pas ete trouve.";
                    this.sendMessage(str);
                    return;
                }
            }
            perso.learnJob(Mundo.mundo.getMetier(job));
            String str = "Le metier " + job + " a ete appris e "
                    + perso.getName() + ".";
            this.sendMessage(str);
            return;
        } else if (command.equalsIgnoreCase("UNLSPELL")) {
            Jugador perso = this.getJugador();
            if (infos.length > 2)//Si un nom de perso est specifie
            {
                perso = Mundo.mundo.getPlayerByName(infos[2]);
                if (perso == null) {
                    String str = "Le personnage n'a pas ete trouve.";
                    this.sendMessage(str);
                    return;
                }
            }
            perso.setExchangeAction(new AccionIntercambiar<>(AccionIntercambiar.FORGETTING_SPELL, 0));
            GestorSalida.GAME_SEND_FORGETSPELL_INTERFACE('+', perso);
            return;
        } else if (command.equalsIgnoreCase("SPAWN")) {
            String Mob = null;
            try {
                Mob = infos[1];
            } catch (Exception e) {
                // ok
            }

            if (Mob == null) {
                this.sendMessage("Les parametres sont invalides.");
                return;
            }
            this.getJugador().getCurMap().spawnGroupOnCommand(this.getJugador().getCurCell().getId(), Mob, true);
            this.sendMessage("Vous avez ajoute un groupe de monstres.");
            return;
        } else if (command.equalsIgnoreCase("SHUTDOWN")) {
            int time = 30, OffOn = 0;
            try {
                OffOn = Integer.parseInt(infos[1]);
                time = Integer.parseInt(infos[2]);
            } catch (Exception e) {
                // ok
            }

            if (OffOn == 1 && this.isTimerStart())// demande de demarer le reboot
            {
                this.sendMessage("Un reboot est déjà programmé.");
            } else if (OffOn == 1 && !this.isTimerStart()) {
                if (time <= 15) {
                    for(Jugador player : Mundo.mundo.getOnlinePlayers()) {
                        player.sendServerMessage("The reboot has been stopped. Now, you can fight.");
                        player.send("M13");
                    }
                    MainServidor.INSTANCE.setFightAsBlocked(true);
                }
                this.setTimer(createTimer(time));
                this.getTimer().start();
                this.setTimerStart(true);
                String timeMSG = "minutes";
                if (time <= 1)
                    timeMSG = "minute";
                GestorSalida.GAME_SEND_Im_PACKET_TO_ALL("115;" + time + " " + timeMSG);
                this.sendMessage("Reboot programmé.");
            } else if (OffOn == 0 && this.isTimerStart()) {
                this.getTimer().stop();
                this.setTimerStart(false);
                for(Jugador player : Mundo.mundo.getOnlinePlayers())
                    player.sendServerMessage("You can't fight until new order.");
                MainServidor.INSTANCE.setFightAsBlocked(true);
                this.sendMessage("Reboot arrêté.");
            } else if (OffOn == 0 && !this.isTimerStart()) {
                this.sendMessage("Aucun reboot n'est lancé.");
            }
            return;
        } else if (command.equalsIgnoreCase("LINEM")) {
            StringBuilder line = new StringBuilder("|");
            for(String split : infos[1].split(",")) {
                int id = Integer.parseInt(split);
                Monstruos monster = Mundo.mundo.getMonstre(id);


                for (MobGrade mobGrade : monster.getGrades().values())
                    line.append(monster.getId()).append(",").append(mobGrade.getLevel()).append("|");
            }
            this.sendMessage(line.toString());
            return;
        } else if (command.equalsIgnoreCase("ENERGIE")) {
            try {
                Jugador perso = this.getJugador();
                String name = null;
                name = infos[2];
                perso = Mundo.mundo.getPlayerByName(name);
                int jet = Integer.parseInt(infos[1]);
                int EnergyTotal = perso.getEnergy() + jet;
                if (EnergyTotal > 10000)
                    EnergyTotal = 10000;
                perso.setEnergy(EnergyTotal);
                GestorSalida.GAME_SEND_STATS_PACKET(perso);
                this.sendMessage("Vous avez fixe l'energie de "
                        + perso.getName() + " e " + EnergyTotal + ".");
                return;
            } catch (Exception ignored) {

            }
            return;
        } else if (command.equalsIgnoreCase("RES")) {
            Jugador perso = this.getJugador();
            perso = Mundo.mundo.getPlayerByName(infos[1]);
            if (perso == null) {
                String mess = "Le personnage n'existe pas.";
                this.sendMessage(mess);
                return;
            }
            if (perso.getPelea() != null) {
                this.sendMessage("Le personnage est en combat.");
                return;
            }
            if (perso.isOnline()) {
                this.sendMessage("Vous avez ramene e la vie " + perso.getName() + ".");
                perso.setAlive();
            } else
                this.sendMessage("Le personnage n'est pas connecte.");
            return;
        } else if (command.equalsIgnoreCase("KICKALL")) {
            this.sendMessage("Tout le monde va etre kicke.");
            JuegoServidor.INSTANCE.kickAll(true);
            return;
        } else if (command.equalsIgnoreCase("RESET")) {
            Jugador perso = this.getJugador();
            if (infos.length > 1) {
                perso = Mundo.mundo.getPlayerByName(infos[1]);
            }
            if (perso == null) {
                String mess = "Le personnage n'existe pas.";
                this.sendMessage(mess);
                return;
            }
            perso.getCaracteristicas().addOneStat(125, -perso.getCaracteristicas().getEffect(125));
            perso.getCaracteristicas().addOneStat(124, -perso.getCaracteristicas().getEffect(124));
            perso.getCaracteristicas().addOneStat(118, -perso.getCaracteristicas().getEffect(118));
            perso.getCaracteristicas().addOneStat(123, -perso.getCaracteristicas().getEffect(123));
            perso.getCaracteristicas().addOneStat(119, -perso.getCaracteristicas().getEffect(119));
            perso.getCaracteristicas().addOneStat(126, -perso.getCaracteristicas().getEffect(126));
            perso.getStatsParcho().getEffects().clear();
            perso.addCapital((perso.getLevel() - 1) * 5 - perso.get_capital());
            GestorSalida.GAME_SEND_STATS_PACKET(perso);
            this.sendMessage("Vous avez restat "
                    + perso.getName() + ".");
            return;
        }else if (command.equalsIgnoreCase("RESETALL")) {
            for (Jugador perso: Mundo.mundo.getJugador()) {
                perso.getCaracteristicas().addOneStat(125, -perso.getCaracteristicas().getEffect(125));
                perso.getCaracteristicas().addOneStat(124, -perso.getCaracteristicas().getEffect(124));
                perso.getCaracteristicas().addOneStat(118, -perso.getCaracteristicas().getEffect(118));
                perso.getCaracteristicas().addOneStat(123, -perso.getCaracteristicas().getEffect(123));
                perso.getCaracteristicas().addOneStat(119, -perso.getCaracteristicas().getEffect(119));
                perso.getCaracteristicas().addOneStat(126, -perso.getCaracteristicas().getEffect(126));
                perso.getStatsParcho().getEffects().clear();
                perso.addCapital((perso.getLevel() - 1) * 5 - perso.get_capital());
                GestorSalida.GAME_SEND_STATS_PACKET(perso);

            }
            this.sendMessage("Vous avez restat tout le monde");
            return;
        } else if (command.equalsIgnoreCase("RENAMEPERSO")) {
            Jugador perso = this.getJugador();
            if (infos.length > 1)
                perso = Mundo.mundo.getPlayerByName(infos[1]);
            if (perso == null) {
                String mess = "Le personnage n'existe pas.";
                this.sendMessage(mess);
                return;
            }
            if(Mundo.mundo.getPlayerByName(infos[2]) != null) {
                String mess = "Le personnage " + infos[2] + " existe déjà.";
                this.sendMessage(mess);
                return;
            }
            String name = perso.getName();
            perso.setName(infos[2]);
            Database.dinamicos.getPlayerData().update(perso);
            GestorSalida.GAME_SEND_STATS_PACKET(perso);
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(), perso.getId());
            GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(perso.getCurMap(), perso);
            this.sendMessage("Vous avez renomme "
                    + name + " en " + perso.getName() + ".");
            return;
        } else if (command.equalsIgnoreCase("RENAMEGUILDE")) {
            String ancName = "";
            String newName = "";
            int idGuild = -1;
            if (infos.length > 1)
                ancName = infos[1];
            newName = infos[2];
            idGuild = Mundo.mundo.getGuildByName(ancName);
            if (idGuild == -1) {
                String mess = "La guilde n'existe pas.";
                this.sendMessage(mess);
                return;
            }
            Mundo.mundo.getGuild(idGuild).setName(newName);
            this.sendMessage("Vous avez renomme la guilde en "
                    + newName + ".");
            return;
        } else if (command.equalsIgnoreCase("A")) {
            infos = msg.split(" ", 2);
            String prefix = "<b>Server</b>";
            GestorSalida.GAME_SEND_Im_PACKET_TO_ALL("116;" + prefix + "~"
                    + infos[1]);
            this.sendMessage("Vous avez envoye un message e tout le serveur.");
            return;
        } else if (command.equalsIgnoreCase("MOVEMOB")) {
            this.getJugador().getCurMap().onMapMonsterDeplacement();
            this.sendMessage("Vous avez deplace un groupe de monstres.");
            return;
        } else if (command.equalsIgnoreCase("ALLGIFTS")) {
            int template = -1, quantity = 0, jp = 0;

            try {
                template = Integer.parseInt(infos[1]);
                quantity = Integer.parseInt(infos[2]);
                jp = Integer.parseInt(infos[3]);
            } catch (Exception e) {
                // ok
                this.sendMessage("Parametre incorrect : ALLGIFTS [templateid] [quantity] [jp= 1 ou 0]");
                return;
            }

            String gift = template + "," + quantity + "," + jp;

            for (Cuenta account : Mundo.mundo.getCuenta()) {
                String gifts = Database.estaticos.getGiftData().getByAccount(account.getId());
                if (gifts.isEmpty()) {
                    Database.estaticos.getGiftData().update(account.getId(), gift);
                } else {
                    Database.estaticos.getGiftData().update(account.getId(), gifts
                            + ";" + gift);
                }
            }
            this.sendMessage(Mundo.mundo.getCuenta().size()
                    + " ont reeu le cadeau : " + gift + ".");
            return;
        } else if (command.equalsIgnoreCase("GIFTS")) {
            String name = "";
            int template = -1, quantity = 0, jp = 0;

            try {
                name = infos[1];
                template = Integer.parseInt(infos[2]);
                quantity = Integer.parseInt(infos[3]);
                jp = Integer.parseInt(infos[4]);
            } catch (Exception e) {
                // ok
                this.sendMessage("Parametre incorrect : GIFTS [account] [templateid] [quantity] [jp= 1 ou 0]");
                return;
            }

            Jugador player = Mundo.mundo.getPlayerByName(name);

            if (player == null) {
                this.sendMessage("Personnage inexistant.");
                return;
            }

            String gift = template + "," + quantity + "," + jp;
            String gifts = Database.estaticos.getGiftData().getByAccount(player.getAccount().getId());
            if (gifts.isEmpty()) {
                Database.estaticos.getGiftData().update(player.getAccount().getId(), gift);
            } else {
                Database.estaticos.getGiftData().update(player.getAccount().getId(), gifts
                        + ";" + gift);
            }
            this.sendMessage(name
                    + " a reeu le cadeau : " + gift + ".");
            return;
        } else if (command.equalsIgnoreCase("SHOWPOINTS")) {
            Jugador perso = this.getJugador();
            if (infos.length > 1)
                perso = Mundo.mundo.getPlayerByName(infos[1]);
            if (perso == null) {
                String mess = "Le personnage n'existe pas.";
                this.sendMessage(mess);
                return;
            }
            this.sendMessage(perso.getName()
                    + " possede "
                    + perso.getAccount().getPoints()
                    + " points boutique.");
            return;
        } else if (command.equalsIgnoreCase("ADDNPC")) {
            int id = 0;
            try {
                id = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }

            if (id == 0 || Mundo.mundo.getNPCTemplate(id) == null) {
                String str = "NpcID invalide.";
                this.sendMessage(str);
                return;
            }

            Npc npc = this.getJugador().getCurMap().addNpc(id, this.getJugador().getCurCell().getId(), this.getJugador().get_orientation());
            GestorSalida.GAME_SEND_ADD_NPC_TO_MAP(this.getJugador().getCurMap(), npc);
            String str = "Le PNJ a ete ajoute";
            if (this.getJugador().get_orientation() == 0
                    || this.getJugador().get_orientation() == 2
                    || this.getJugador().get_orientation() == 4
                    || this.getJugador().get_orientation() == 6)
                str += " mais est invisible (orientation diagonale invalide)";
            str += ".";
            if (Database.estaticos.getNpcData().addOnMap(this.getJugador().getCurMap().getId(), id, this.getJugador().getCurCell().getId(), this.getJugador().get_orientation(), false))
                this.sendMessage(str);
            else
                this.sendMessage("Erreur lors de la sauvegarde de la position.");
            return;
        } else if (command.equalsIgnoreCase("DELNPC")) {
            int id = 0;
            try {
                id = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }

            Npc npc = this.getJugador().getCurMap().getNpc(id);
            if (id == 0 || npc == null) {
                String str = "Npc GUID invalide.";
                this.sendMessage(str);
                return;
            }
            int exC = npc.getCellId();
            //on l'efface de la map
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.getJugador().getCurMap(), id);
            this.getJugador().getCurMap().removeNpcOrMobGroup(id);

            String str = "Le PNJ a ete supprime.";
            if (Database.estaticos.getNpcData().delete(this.getJugador().getCurMap().getId(), exC))
                this.sendMessage(str);
            else
                this.sendMessage("Erreur lors de la sauvegarde de la position.");
            return;
        } else if (command.equalsIgnoreCase("UPDATENPC")) {
            int id = 0;
            try {
                id = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }
            Jugador apariencia = this.getJugador();
            try {
                apariencia = Mundo.mundo.getPlayerByName(infos[2]);
            } catch (Exception ignored) {
            }

            NpcModelo npcModelo = Mundo.mundo.getNPCTemplate(id);
            if (id == 0 || npcModelo == null) {
                String str = "Npc Modelo GUID invalide.";
                this.sendMessage(str);
                return;
            }
            if (npcModelo.updateApariencia(apariencia.getGfxId(), apariencia.get_size(), apariencia.getSexe(),
                    apariencia.getColor1(), apariencia.getColor2(), apariencia.getColor3(),
                    apariencia.getGMStuffString())) {
                this.sendMessage("NPC actualizado y salvado en la BD");
            } else {
                this.sendMessage("NPC actualizado pero no se guardo en la BD");
            }
            return;
        } else if (command.equalsIgnoreCase("SETSTATS")) {
            int obj = -1;
            String stats = "";
            try {
                obj = Integer.parseInt(infos[1]);
                stats = infos[2];
            } catch (Exception e) {
                // ok
            }
            if (obj == -1 || stats.equals("")) {
                this.sendMessage("Les parametres sont invalides.");
                return;
            }
            ObjetoJuego object = Mundo.getGameObject(obj);
            if (object == null) {
                this.sendMessage("L'objet n'existe pas.");
                return;
            }
            if (stats.equals("-1")) {
                object.clearStats();
                GestorSalida.GAME_SEND_UPDATE_ITEM(this.getJugador(), object);
            } else {
                object.refreshStatsObjet(stats);
                GestorSalida.GAME_SEND_UPDATE_ITEM(this.getJugador(), object);
            }
            this.sendMessage("L'objet a ete modifie avec succes.");
            return;
        } else if (command.equalsIgnoreCase("ADDCELLPARK")) {
            if (this.getJugador().getCurMap().getMountPark() == null) {
                this.sendMessage("Pas d'enclos sur votre map.");
                return;
            }
            this.getJugador().getCurMap().getMountPark().addCellObject(this.getJugador().getCurCell().getId());
            Database.dinamicos.getMountParkData().update(this.getJugador().getCurMap().getMountPark());
            this.sendMessage("Vous avez ajoute la cellule e l'enclos.");
            return;
        } else if (command.equalsIgnoreCase("O")) {
            Cercados mp = this.getJugador().getCurMap().getMountPark();

            for (GameCase c : this.getJugador().getCurMap().getCases()) {
                if (c.getObject() != null) {
                    switch (c.getObject().getTemplate().getId()) {
                        case 6766, 6767, 6763, 6772 -> {
                            mp.setDoor(c.getId());
                            this.sendMessage("Vous avez ajoute une porte e l'enclos.");
                            return;
                        }
                    }
                }
            }
            this.sendMessage("Vous ne vous situez pas sur la porte.");
        } else if (command.equalsIgnoreCase("A1")) {
            this.getJugador().getCurMap().getMountPark().setMountCell(this.getJugador().getCurCell().getId());
            this.sendMessage("Vous avez modifie la cellule de spawn de l'enclos.");
        } else if (command.equalsIgnoreCase("B1")) {
            this.getJugador().getCases = true;
            this.sendMessage("Vous avez active le getCases.");
        } else if (command.equalsIgnoreCase("C1")) {
            this.getJugador().getCases = false;
            this.getJugador().getCurMap().getMountPark().setCellObject(this.getJugador().thisCases);
            this.getJugador().thisCases.clear();
            Database.dinamicos.getMountParkData().update(this.getJugador().getCurMap().getMountPark());
            this.sendMessage("Vous avez applique les nouvelles cases e l'enclos.");
        } else if (command.equalsIgnoreCase("RELOADDROP")) {
            Mundo.mundo.reloadDrops();
            this.sendMessage("Le rechargement des drops a ete effectue.");
            return;
        } else if (command.equalsIgnoreCase("RELOADENDFIGHT")) {
            Mundo.mundo.reloadEndFightActions();
            this.sendMessage("Le rechargement des endfights a ete effectue.");
            return;
        } else if (command.equalsIgnoreCase("RELOADHOUSE")) {
            Mundo.mundo.reloadHouses();
            this.sendMessage("Le rechargement des maisons a ete effectue.");
            return;
        } else if (command.equalsIgnoreCase("RELOADCOFFRE")) {
            Mundo.mundo.reloadTrunks();
            this.sendMessage("Le rechargement des coffres a ete effectue.");
            return;
        } else if (command.equalsIgnoreCase("RELOADACTION")) {
            Mundo.mundo.reloadObjectsActions();
            this.sendMessage("Le rechargement des actions a ete effectue.");
            return;
        } else if (command.equalsIgnoreCase("RELOADMAP")) {
            Mundo.mundo.reloadMaps();
            this.sendMessage("Le rechargement des maps a ete effectue.");
            return;
        } else if (command.equalsIgnoreCase("RELOADMOUNTPARK")) {
            int i = Integer.parseInt(infos[1]);
            Mundo.mundo.reloadMountParks(i);
            this.sendMessage("Le rechargement de l'enclos "
                    + i + " a ete effectue.");
            return;
        } else if (command.equalsIgnoreCase("RELOADNPC")) {
            try {
                Mundo.mundo.reloadNpcs();
            } catch(Exception e) {
                e.printStackTrace();
                this.sendMessage(e.getMessage());
            }
            this.sendMessage("Le rechargement des Npcs a ete effectue.");
            return;
        } else if (command.equalsIgnoreCase("RELOADSPELL")) {
            Mundo.mundo.reloadSpells();
            this.sendMessage("Le rechargement des sorts a ete effectue.");
            return;
        } else if (command.equalsIgnoreCase("RELOADITEM")) {
            Mundo.mundo.reloadItems();
            this.sendMessage("Le rechargement des items a ete effectue.");
            return;
        } else if (command.equalsIgnoreCase("RELOADMONSTER")) {
            Mundo.mundo.reloadMonsters();
            this.sendMessage("Le rechargement des monstres a ete effectue.");
            return;
        } else if (command.equalsIgnoreCase("RELOADQUEST")) {
            Mundo.mundo.reloadQuests();
            this.sendMessage("Le rechargement des quetes a ete effectue.");
            return;
        } else if (command.equalsIgnoreCase("RELOADADMIN")) {
            Comandos.reload();
            GrupoADM.reload();
            Mundo.mundo.reloadPlayerGroup();
            this.sendMessage("Le rechargement des commandes et des groupes ont etes effectues.");
            return;
        } else if (command.equalsIgnoreCase("CONVERT")) {
            try {
                this.sendMessage(Long.toHexString(Long.parseLong(infos[1])));
                this.sendMessage(Long.parseLong(infos[1], 16) + "");
            } catch (Exception e) {
                this.sendMessage(Long.parseLong(infos[1], 16) + "");
            }
            return;
        } else if (command.equalsIgnoreCase("LISTTYPE")) {
            StringBuilder s = new StringBuilder();
            for (ObjetoModelo obj : Mundo.mundo.getObjTemplates())
                if (obj.getType() == Integer.parseInt(infos[1]))
                    s.append(obj.getId()).append(",");
            this.sendMessage(s.toString());
            return;
        } else if (command.equalsIgnoreCase("EMOTE")) {
            Jugador perso = this.getJugador();
            byte emoteId = 0;
            try {
                emoteId = Byte.parseByte(infos[1]);
                perso = Mundo.mundo.getPlayerByName(infos[2]);
            } catch (Exception e) {
                // ok
            }
            if (perso == null)
                perso = this.getJugador();
            this.getJugador().addStaticEmote(emoteId);
            this.sendMessage("L'emote "
                    + emoteId
                    + " a ete ajoute au joueur "
                    + perso.getName()
                    + ".");
            return;
        } else if (command.equalsIgnoreCase("DELNPCITEM")) {
            int npcGUID = 0;
            int itmID = -1;
            try {
                npcGUID = Integer.parseInt(infos[1]);
                itmID = Integer.parseInt(infos[2]);
            } catch (Exception e) {
                // ok
            }

            Mapa map = this.getJugador().getCurMap();
            Npc npc = map.getNpc(npcGUID);
            NpcModelo npcTemplate = null;
            if (npc == null)
                npcTemplate = Mundo.mundo.getNPCTemplate(npcGUID);
            else
                npcTemplate = npc.getTemplate();
            if (npcGUID == 0 || itmID == -1 || npcTemplate == null) {
                String str = "NpcGUID ou itemID invalide.";
                this.sendMessage(str);
                return;
            }
            String str = "";
            if (npcTemplate.removeItemVendor(itmID))
                str = "L'objet a ete retire.";
            else
                str = "L'objet n'a pas ete retire.";
            this.sendMessage(str);
            Database.estaticos.getNpcTemplateData().update(npcTemplate);
            return;
        } else if (command.equalsIgnoreCase("ADDNPCITEM")) {
            int npcGUID = 0;
            int itmID = -1;
            try {
                npcGUID = Integer.parseInt(infos[1]);
                itmID = Integer.parseInt(infos[2]);
            } catch (Exception e) {
                // ok
            }

            Mapa map = this.getJugador().getCurMap();
            Npc npc = map.getNpc(npcGUID);
            NpcModelo npcTemplate = null;
            if (npc == null)
                npcTemplate = Mundo.mundo.getNPCTemplate(npcGUID);
            else
                npcTemplate = npc.getTemplate();
            ObjetoModelo item = Mundo.mundo.getObjetoModelo(itmID);
            if (npcGUID == 0 || itmID == -1 || npcTemplate == null
                    || item == null) {
                String str = "NpcGUID ou itemID invalide.";
                this.sendMessage(str);
                return;
            }
            String str = "";
            if (npcTemplate.addItemVendor(item))
                str = "L'objet a ete rajoute.";
            else
                str = "L'objet n'a pas ete rajoute.";
            this.sendMessage(str);
            Database.estaticos.getNpcTemplateData().update(npcTemplate);
            return;
        } else if (command.equalsIgnoreCase("LISTEXTRA")) {
            StringBuilder mess = new StringBuilder("Liste des Extra Monstres :");
            for (Entry<Integer, Mapa> i : Mundo.mundo.getExtraMonsterOnMap().entrySet())
                mess.append("\n- ").append(i.getKey()).append(" est sur la map : ").append(i.getValue().getId());
            if (Mundo.mundo.getExtraMonsterOnMap().size() <= 0)
                mess = new StringBuilder("Aucun Extra Monstres existe.");
            this.sendMessage(mess.toString());
            return;
        } else if (command.equalsIgnoreCase("CREATEGUILD")) {
            Jugador perso = this.getJugador();
            if (infos.length > 1) {
                perso = Mundo.mundo.getPlayerByName(infos[1]);
            }
            if (perso == null) {
                String mess = "Le personnage n'existe pas.";
                this.sendMessage(mess);
                return;
            }

            if (!perso.isOnline()) {
                String mess = "Le personnage " + perso.getName()
                        + " n'est pas connecte.";
                this.sendMessage(mess);
                return;
            }
            if (perso.getGuild() != null || perso.getGuildMember() != null) {
                String mess = "Le personnage " + perso.getName()
                        + " possede deje une guilde.";
                this.sendMessage(mess);
                return;
            }
            GestorSalida.GAME_SEND_gn_PACKET(perso);
            String mess = perso.getName()
                    + ": Panneau de creation de guilde ouvert.";
            this.sendMessage(mess);
            return;
        } else if (command.equalsIgnoreCase("SEND")) {
            GestorSalida.send(this.getCliente(), msg.substring(5));
            this.sendMessage("Le paquet a ete envoye : "
                    + msg.substring(5));
            return;
        } else if (command.equalsIgnoreCase("SENDTOMAP")) {
            GestorSalida.sendPacketToMap(this.getJugador().getCurMap(), infos[1]);
            this.sendMessage("Le paquet a ete envoye : "
                    + msg.substring(10));
            return;
        } else if (command.equalsIgnoreCase("SENDTO")) {
            Jugador perso = null;
            try {
                perso = Mundo.mundo.getPlayerByName(infos[1]);
            } catch (Exception e) {
                // ok
            }
            if (perso == null) {
                this.sendMessage("Le nom du personnage est incorrect.");
                return;
            }
            GestorSalida.send(Mundo.mundo.getPlayerByName(infos[1]), msg.substring(8 + infos[1].length()));
            this.sendMessage("Le paquet a ete envoye : "
                    + msg.substring(8 + infos[1].length()) + " e " + infos[1] + ".");
            return;
        } else if (command.equalsIgnoreCase("TITRE")) {
            Jugador perso = this.getJugador();
            byte TitleID = 0;
            try {
                TitleID = Byte.parseByte(infos[1]);
                perso = Mundo.mundo.getPlayerByName(infos[2]);
            } catch (Exception e) {
                // ok
            }

            if (perso == null) {
                perso = this.getJugador();
            }

            perso.set_title(TitleID);
            this.sendMessage("Vous avez modifie le titre de "
                    + perso.getName() + ".");
            Database.dinamicos.getPlayerData().update(perso);
            if (perso.getPelea() == null)
                GestorSalida.GAME_SEND_ALTER_GM_PACKET(perso.getCurMap(), perso);
            return;
        } else if (command.equalsIgnoreCase("POINTS")) {
            int count = 0;
            try {
                count = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
                this.sendMessage("Valeur incorrecte.");
                return;
            }
            if (count == 0) {
                this.sendMessage("Valeur inutile.");
                return;
            }
            Jugador perso = this.getJugador();
            if (infos.length == 3)//Si le nom du perso est specifie
            {
                String name = infos[2];
                perso = Mundo.mundo.getPlayerByName(name);
                if (perso == null)
                    perso = this.getJugador();
            }
            int pointtotal = perso.getAccount().getPoints() + count;
            if (pointtotal < 0)
                pointtotal = 0;
            if (pointtotal > 50000)
                pointtotal = 50000;
            perso.getAccount().setPoints(pointtotal);
            if (perso.isOnline())
                GestorSalida.GAME_SEND_STATS_PACKET(perso);
            String mess = "Vous venez de donner " + count
                    + " points boutique e " + perso.getName() + ".";
            this.sendMessage(mess);
            return;
        } else if (command.equalsIgnoreCase("ITEMTYPE")) {
            int type = 0;
            try {
                type = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }

            for (ObjetoModelo obj : Mundo.mundo.getObjTemplates()) {
                if (obj.getType() == type) {
                    ObjetoJuego addObj = obj.createNewItem(1, true);
                    if (this.getJugador().addObjet(addObj, true))//Si le joueur n'avait pas d'item similaire
                        Mundo.addGameObject(addObj, true);
                }
            }
            this.sendMessage("Vous avez tous les objets de type "
                    + type + " dans votre inventaire.");
            return;
        } else if (command.equalsIgnoreCase("FULLMORPH")) {
            this.getJugador().setFullMorph(10, false, false);
            this.sendMessage("Vous avez ete transforme en crocoburio.");
            return;
        } else if (command.equalsIgnoreCase("UNFULLMORPH")) {
            String pseudo = "";
            try {
                pseudo = infos[1];
            } catch (Exception e) {
                // ok
            }
            Jugador p = Mundo.mundo.getPlayerByName(pseudo);
            if (p == null)
                p = this.getJugador();
            p.unsetFullMorph();
            this.sendMessage("Vous avez transforme dans la forme originale "
                    + p.getName() + ".");
            return;
        } else if (command.equalsIgnoreCase("PETSRES")) {
            int objID = 1;
            try {
                objID = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }

            MascotaEntrada p = Mundo.mundo.getPetsEntry(objID);
            if (p == null) {
                this.sendMessage("Le familier n'existe pas.");
                return;
            }
            p.resurrection();
            this.sendMessage("Vous avez ressuscite le familier.");
            GestorSalida.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(this.getJugador(), Mundo.getGameObject(objID));
            return;
        } else if (command.equalsIgnoreCase("SETGROUPE")) {
            int id;
            try {
                id = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                this.sendErrorMessage("The group you've specified is invalid (it's a number).");
                return;
            }

            GrupoADM group = GrupoADM.getGrupoID(id);

            if(id == -1) {
                if (infos.length > 2) {
                    Jugador player = Mundo.mundo.getPlayerByName(infos[2]);
                    if (player != null) {
                        player.setGroupe(null, true);
                        player.send("BAIC");
                        Database.dinamicos.getPlayerData().updateGroupe(id, infos[2]);
                        this.sendMessage("The player " + infos[2] + " has been remove to his group admin successfully.");
                    }
                } else {
                    this.sendMessage("No player specified, can't change anything.");
                }
            } else
            if(group == null) {
                this.sendErrorMessage("The group you've specified is invalid :");
                for(GrupoADM gp : GrupoADM.getGrupo()) {
                    this.sendMessage("-> " + gp.getId() + " - " + gp.getNombre());
                }
            } else {
                if (infos.length > 2) {
                    Jugador player = Mundo.mundo.getPlayerByName(infos[2]);
                    if (player != null) {
                        player.setGroupe(group, true);
                        player.send("BAIO");
                        Database.dinamicos.getPlayerData().updateGroupe(id, infos[2]);
                        this.sendMessage("The player " + infos[2] + " has been assigned to group " + group.getNombre() + " successfully.");
                    }
                } else {
                    this.sendMessage("No player specified, can't change anything.");
                }
            }
            return;
        } else  if (command.equalsIgnoreCase("SETFREEPLACE")) {
            //GameServer.freePlace = Integer.parseInt(infos[1]);
            this.sendMessage("");
            return;
        } else if (command.equalsIgnoreCase("SHOWRIGHTGROUPE")) {
            int groupe = -1;
            String cmd = "";
            try {
                groupe = Integer.parseInt(infos[1]);
                cmd = infos[2];
            } catch (Exception e) {
                // ok
            }

            GrupoADM g = null;
            if (groupe > 0)
                g = GrupoADM.getGrupoID(groupe);

            if (g == null) {
                String str = "Le groupe est invalide.";
                this.sendMessage(str);
                return;
            }

            List<Comandos> c = g.getComandos();

            if (cmd.equalsIgnoreCase("")) {
                this.sendMessage("\nCommandes disponibles pour le groupe "
                        + g.getNombre() + " :\n");
                for (Comandos co : c) {
                    String args = (co.getArgumento()[1] != null && !co.getArgumento()[1].equalsIgnoreCase("")) ? (" + " + co.getArgumento()[1]) : ("");
                    String desc = (co.getArgumento()[2] != null && !co.getArgumento()[2].equalsIgnoreCase("")) ? (co.getArgumento()[2]) : ("");
                    this.sendMessage("<u>"
                            + co.getArgumento()[0]
                            + args
                            + "</u> - "
                            + desc);
                }
            } else {
                this.sendMessage("\nCommandes recherches pour le groupe "
                        + g.getNombre() + " :\n");
                for (Comandos co : c) {
                    if (co.getArgumento()[0].contains(cmd.toUpperCase())) {
                        String args = (co.getArgumento()[1] != null && !co.getArgumento()[1].equalsIgnoreCase("")) ? (" + " + co.getArgumento()[1]) : ("");
                        String desc = (co.getArgumento()[2] != null && !co.getArgumento()[2].equalsIgnoreCase("")) ? (co.getArgumento()[2]) : ("");
                        this.sendMessage("<u>"
                                + co.getArgumento()[0]
                                + args
                                + "</u> - "
                                + desc);
                    }
                }
            }
            return;
        } else if (command.equalsIgnoreCase("INV")) {
            int size = this.getJugador().get_size();
            Jugador perso = this.getJugador();
            if (size == 0) {
                if (perso.getGfxId() == 8008)
                    perso.set_size(150);
                else
                    perso.set_size(100);
                perso.setInvisible(false);
                GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(), perso.getId());
                GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(perso.getCurMap(), perso);
                this.sendMessage("Vous etes visible.");
            } else {
                perso.setInvisible(true);
                perso.set_size(0);
                GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(), perso.getId());
                GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(perso.getCurMap(), perso);
                this.sendMessage("Vous etes invisible.");
            }
            return;
        } else if (command.equalsIgnoreCase("INCARNAM")) {
            Jugador perso = this.getJugador();
            perso.teleport((short) 10292, 284);
            this.sendMessage("Vous avez ete teleporte e Incarnam.");
            return;
        } else if (command.equalsIgnoreCase("ASTRUB")) {
            Jugador perso = this.getJugador();
            perso.teleport((short) 7411, 311);
            this.sendMessage("Vous avez ete teleporte e Astrub.");
            return;
        } else if (command.equalsIgnoreCase("DELQUEST")) {
            int id = -1;
            String perso = "";
            try {
                id = Integer.parseInt(infos[1]);
                perso = infos[2];
            } catch (Exception e) {
                // ok
            }

            if (id == -1 || perso.equalsIgnoreCase("")) {
                this.sendMessage("Un des parametres est invalide.");
                return;
            }
            Jugador p = Mundo.mundo.getPlayerByName(perso);
            Mision q = Mision.getQuestById(id);
            if (p == null || q == null) {
                this.sendMessage("La quete ou le joueur est introuvable.");
                return;
            }
            MisionJugador qp = p.getQuestPersoByQuest(q);
            if (qp == null) {
                this.sendMessage("Le personnage n'a pas la quete.");
                return;
            }
            p.delQuestPerso(qp.getId());
            if (qp.removeQuestPlayer()) {
                Database.dinamicos.getPlayerData().update(p);
                this.sendMessage("La quete a ete supprime sur le personnage " + perso + ".");
            } else
                this.sendMessage("Un probleme est survenu.");
            return;
        } else if (command.equalsIgnoreCase("ADDQUEST")) {
            int id = -1;
            String perso = "";
            try {
                id = Integer.parseInt(infos[1]);
                perso = infos[2];
            } catch (Exception e) {
                // ok
            }

            if (id == -1 || perso.equalsIgnoreCase("")) {
                this.sendMessage("Un des parametres est invalide.");
                return;
            }
            Jugador p = Mundo.mundo.getPlayerByName(perso);
            Mision q = Mision.getQuestById(id);
            if (p == null || q == null) {
                this.sendMessage("La quete ou le joueur est introuvable.");
                return;
            }
            MisionJugador qp = p.getQuestPersoByQuest(q);
            if (qp != null) {
                this.sendMessage("Le personnage a deje la quete.");
                return;
            }
            q.applyQuest(p);
            qp = p.getQuestPersoByQuest(q);
            if (qp == null) {
                this.sendMessage("Une erreur est survenue.");
                return;
            }
            this.sendMessage("La quete a ete ajoute sur le personnage "
                    + perso + ".");
            return;
        } else if (command.equalsIgnoreCase("FINISHQUEST")) {
            int id = -1;
            String perso = "";
            try {
                id = Integer.parseInt(infos[1]);
                perso = infos[2];
            } catch (Exception e) {
                // ok
            }

            if (id == -1 || perso.equalsIgnoreCase("")) {
                this.sendMessage("Un des parametres est invalide.");
                return;
            }
            Jugador p = Mundo.mundo.getPlayerByName(perso);
            Mision q = Mision.getQuestById(id);
            if (p == null || q == null) {
                this.sendMessage("La quete ou le joueur est introuvable.");
                return;
            }
            MisionJugador qp = p.getQuestPersoByQuest(q);
            if (qp == null) {
                this.sendMessage("Le personnage n'a pas la quete.");
                return;
            }
            for (MisionEtapa e : q.getQuestSteps()) {
                q.updateQuestData(p, true, e.getValidationType());
            }
            Database.dinamicos.getPlayerData().update(p);
            this.sendMessage("La quete a ete termine sur le personnage "
                    + perso + ".");
            return;
        } else if (command.equalsIgnoreCase("SKIPQUEST")) {
            int id = -1;
            String perso = "";
            try {
                id = Integer.parseInt(infos[1]);
                perso = infos[2];
            } catch (Exception e) {
                // ok
            }

            if (id == -1 || perso.equalsIgnoreCase("")) {
                this.sendMessage("Un des parametres est invalide.");
                return;
            }
            Jugador p = Mundo.mundo.getPlayerByName(perso);
            Mision q = Mision.getQuestById(id);
            if (p == null || q == null) {
                this.sendMessage("La quete ou le joueur est introuvable.");
                return;
            }
            MisionJugador qp = p.getQuestPersoByQuest(q);
            if (qp == null) {
                this.sendMessage("Le personnage n'a pas la quete.");
                return;
            }
            for (MisionEtapa e : q.getQuestSteps()) {
                if (qp.isQuestStepIsValidate(e))
                    continue;

                q.updateQuestData(p, true, e.getValidationType());
                break;
            }
            Database.dinamicos.getPlayerData().update(p);
            this.sendMessage("La quete est passe e l'etape suivante sur le personnage "
                    + perso + ".");
            return;
        } else if (command.equalsIgnoreCase("ITEMQUEST")) {
            int id = -1;
            try {
                id = Integer.parseInt(infos[1]);
            } catch (Exception e) {
                // ok
            }

            if (id == -1) {
                this.sendMessage("Le parametre est invalide.");
                return;
            }
            Mision q = Mision.getQuestById(id);
            if (q == null) {
                this.sendMessage("La quete est introuvable.");
                return;
            }

            for (MisionEtapa e : q.getQuestSteps()) {
                for (Entry<Integer, Integer> entry : e.getItemNecessaryList().entrySet()) {
                    ObjetoModelo objT = Mundo.mundo.getObjetoModelo(entry.getKey());
                    int qua = entry.getValue();
                    ObjetoJuego obj = objT.createNewItem(qua, false);
                    if (this.getJugador().addObjet(obj, true))
                        Mundo.addGameObject(obj, true);
                    GestorSalida.GAME_SEND_Im_PACKET(this.getJugador(), "021;"
                            + qua + "~" + objT.getId());
                    if (objT.getType() == 32) // Si le drop est une mascotte, on l'ajoute ! :)
                    {
                        this.getJugador().setMascotte(entry.getKey());
                    }
                }
            }
            this.sendMessage("Vous avez reeu tous les items necessaire e la quete.");
            return;
        } else if (command.equalsIgnoreCase("SHOWFIGHTPOS")) {
            StringBuilder mess = new StringBuilder("Liste des StartCell [teamID][cellID]:");
            this.sendMessage(mess.toString());
            String places = this.getJugador().getCurMap().getPlaces();
            if (places.indexOf('|') == -1 || places.length() < 2) {
                mess = new StringBuilder("Les places n'ont pas ete definies");
                this.sendMessage(mess.toString());
                return;
            }
            String team0 = "", team1 = "";
            String[] p = places.split("\\|");
            try {
                team0 = p[0];
            } catch (Exception e) {
                // ok
            }

            try {
                team1 = p[1];
            } catch (Exception e) {
                // ok
            }

            mess = new StringBuilder("Team 0 : ");
            for (int a = 0; a <= team0.length() - 2; a += 2) {
                String code = team0.substring(a, a + 2);
                mess.append(Mundo.mundo.getCryptManager().codigoceldaID(code)).append(",");
            }
            this.sendMessage(mess.toString());
            mess = new StringBuilder("Team 1 : ");
            for (int a = 0; a <= team1.length() - 2; a += 2) {
                String code = team1.substring(a, a + 2);
                mess.append(Mundo.mundo.getCryptManager().codigoceldaID(code)).append(",");
            }
            this.sendMessage(mess.toString());
        } else if (command.equalsIgnoreCase("ADDFIGHTPOS")) {
            int team = -1;
            int cell = -1;
            try {
                team = Integer.parseInt(infos[1]);
                cell = Integer.parseInt(infos[2]);
            } catch (Exception e) {
                // ok
            }

            if (team < 0 || team > 1) {
                String str = "Team ou cellID incorects";
                this.sendMessage(str);
                return;
            }
            if (cell < 0
                    || this.getJugador().getCurMap().getCase(cell) == null
                    || !this.getJugador().getCurMap().getCase(cell).isWalkable(true)) {
                cell = this.getJugador().getCurCell().getId();
            }
            String places = this.getJugador().getCurMap().getPlaces();
            String[] p = places.split("\\|");
            boolean already = false;
            String team0 = "", team1 = "";
            try {
                team0 = p[0];
            } catch (Exception e) {
                // ok
            }

            try {
                team1 = p[1];
            } catch (Exception e) {
                // ok
            }

            for (int a = 0; a <= team0.length() - 2; a += 2)
                if (cell == Mundo.mundo.getCryptManager().codigoceldaID(team0.substring(a, a + 2)))
                    already = true;
            for (int a = 0; a <= team1.length() - 2; a += 2)
                if (cell == Mundo.mundo.getCryptManager().codigoceldaID(team1.substring(a, a + 2)))
                    already = true;
            if (already) {
                this.sendMessage("La case est deja dans la liste");
                return;
            }
            if (team == 0)
                team0 += Mundo.mundo.getCryptManager().idceldaCodigo(cell);
            else team1 += Mundo.mundo.getCryptManager().idceldaCodigo(cell);
            String newPlaces = team0 + "|" + team1;
            this.getJugador().getCurMap().setPlaces(newPlaces);
            if (!Database.estaticos.getMapData().update(this.getJugador().getCurMap()))
                return;
            this.sendMessage("Les places ont ete modifiees ("
                    + newPlaces + ")");
        } else if (command.equalsIgnoreCase("DELFIGHTPOS")) {
            int cell = -1;
            try {
                cell = Integer.parseInt(infos[2]);
            } catch (Exception e) {
                // ok
            }

            if (cell < 0 || this.getJugador().getCurMap().getCase(cell) == null) {
                cell = this.getJugador().getCurCell().getId();
            }
            String places = this.getJugador().getCurMap().getPlaces();
            String[] p = places.split("\\|");
            StringBuilder newPlaces = new StringBuilder();
            String team0 = "", team1 = "";
            try {
                team0 = p[0];
            } catch (Exception e) {
                // ok
            }

            try {
                team1 = p[1];
            } catch (Exception e) {
                // ok
            }

            for (int a = 0; a <= team0.length() - 2; a += 2) {
                String c = p[0].substring(a, a + 2);
                if (cell == Mundo.mundo.getCryptManager().codigoceldaID(c))
                    continue;
                newPlaces.append(c);
            }
            newPlaces.append("|");
            for (int a = 0; a <= team1.length() - 2; a += 2) {
                String c = p[1].substring(a, a + 2);
                if (cell == Mundo.mundo.getCryptManager().codigoceldaID(c))
                    continue;
                newPlaces.append(c);
            }
            this.getJugador().getCurMap().setPlaces(newPlaces.toString());
            if (!Database.estaticos.getMapData().update(this.getJugador().getCurMap()))
                return;
            this.sendMessage("Les places ont ete modifiees ("
                    + newPlaces + ")");
        } else if (command.equalsIgnoreCase("DELALLFIGHTPOS")) {
            this.getJugador().getCurMap().setPlaces("");
            if (!Database.estaticos.getMapData().update(this.getJugador().getCurMap()))
                return;
            this.sendMessage("Les places ont ete mis a zero !");
        } else if (command.equalsIgnoreCase("ADDMOBSUBAREA")) {
            String monsters = "";
            String mess = "";
            if (infos.length > 1)
                monsters = infos[1];
            else {
                mess = "Il manque le premier argument.";
                this.sendMessage(mess);
                return;
            }

            Jugador perso = this.getJugador();
            Mapa map = perso.getCurMap();

            SubArea subArea = map.getSubArea();
            ArrayList<Mapa> maps = subArea.getMaps();
            int i = 0;
            int y = 0;
            for (Mapa m : maps) {
                if (m.getPlaces().equalsIgnoreCase("")
                        || m.getPlaces().equalsIgnoreCase("|")) {

                    m.setMobPossibles("");
                    Database.estaticos.getMapData().updateMonster(m, "");
                    y++;
                } else {
                    m.setMobPossibles(monsters);
                    Database.estaticos.getMapData().updateMonster(m, monsters);
                    i++;
                }
                m.refreshSpawns();
            }

            mess = i + " maps ont etes modifies et refresh. " + y
                    + "maps ont etes modifies sans monstres et refresh.";
            this.sendMessage(mess);
        } else if (command.equalsIgnoreCase("GSMOBSUBAREA")) {
            byte maxGroup = 0;
            byte minSize = 0;
            byte fixSize = 0;
            byte maxSize = 0;
            byte def = -1;
            String mess = "";
            if (infos.length > 4) {
                maxGroup = Byte.parseByte(infos[1]);
                minSize = Byte.parseByte(infos[2]);
                fixSize = Byte.parseByte(infos[3]);
                maxSize = Byte.parseByte(infos[4]);
            } else {
                mess = "Il manque les arguments.";
                this.sendMessage(mess);
                return;
            }

            Jugador perso = this.getJugador();
            Mapa map = perso.getCurMap();

            SubArea subArea = map.getSubArea();
            ArrayList<Mapa> maps = subArea.getMaps();
            int i = 0;
            int y = 0;
            for (Mapa m : maps) {
                if (m.getPlaces().equalsIgnoreCase("")
                        || m.getPlaces().equalsIgnoreCase("|")) {
                    m.setGs(def, def, def, def);
                    Database.estaticos.getMapData().updateGs(m);
                    y++;
                } else {
                    m.setGs(maxGroup, minSize, fixSize, maxSize);
                    Database.estaticos.getMapData().updateGs(m);
                    i++;
                }
                m.refreshSpawns();
            }

            mess = i + " maps ont etes modifies et refresh. " + y
                    + " maps ont etes modifies e -1 partout et refresh.";
            this.sendMessage(mess);
        } else if (command.equalsIgnoreCase("FINDEXTRAMONSTER")) {
            java.util.Map<Integer, java.util.Map<String, java.util.Map<String, Integer>>> extras = Mundo.mundo.getExtraMonsters();

            for (Entry<Integer, java.util.Map<String, java.util.Map<String, Integer>>> entry : extras.entrySet()) {
                Integer idMob = entry.getKey();
                for (Mapa map : Mundo.mundo.getMapa())
                    map.getMobPossibles().stream().filter(mob -> mob.getTemplate().getId() == idMob).forEach(mob -> this.sendMessage("Map avec extraMonster : " + map.getId() + " -> " + idMob + "."));
            }
            this.sendMessage("Recherche termine et affiche en console.");
        } else if (command.equalsIgnoreCase("GETAREA")) {
            int subArea = -1, area = -1, superArea = -1;
            try {
                subArea = this.getJugador().getCurMap().getSubArea().getId();
                area = this.getJugador().getCurMap().getSubArea().getArea().getId();
                superArea = this.getJugador().getCurMap().getSubArea().getArea().getSuperArea();
            } catch (Exception e) {
                // ok
            }
            this.sendMessage("subArea : "
                    + subArea
                    + "\nArea : "
                    + area
                    + "\nsuperArea : "
                    + superArea);
        } else {
            this.sendMessage("Comando invalido!");
        }
    }

    private static int getCellJail() {
        return switch (Formulas.random.nextInt(4) + 1) {
            case 1 -> 148;
            case 2 -> 156;
            case 3 -> 380;
            case 4 -> 388;
            default -> 148;
        };
    }

    private static String returnClasse(int id) {
        return switch (id) {
            case Constantes.CLASE_FECA -> "Fec";
            case Constantes.CLASE_OSAMODAS -> "Osa";
            case Constantes.CLASE_ANUTROF -> "Enu";
            case Constantes.CLASE_SRAM -> "Sra";
            case Constantes.CLASE_XELOR -> "Xel";
            case Constantes.CLASE_ZURCARAK -> "Eca";
            case Constantes.CLASE_ANIRIPSA -> "Eni";
            case Constantes.CLASE_YOPUKA -> "Iop";
            case Constantes.CLASE_OCRA -> "Cra";
            case Constantes.CLASE_SADIDA -> "Sad";
            case Constantes.CLASE_SACROGRITO -> "Sac";
            case Constantes.CLASE_PANDAWA -> "Pan";
            default -> "Unk";
        };
    }
}