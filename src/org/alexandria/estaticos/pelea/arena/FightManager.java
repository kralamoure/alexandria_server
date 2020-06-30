package org.alexandria.estaticos.pelea.arena;

import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.configuracion.Configuracion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class FightManager {

    public static final ScheduledExecutorService scheduler;
    public static short[] maps = {7280, 7281, 7282, 7283, 7285, 7286};

    private static final List<DeathMatch> deathMatchs = Collections.synchronizedList(new ArrayList<>());
    private static final List<TeamMatch> kolizeums = Collections.synchronizedList(new ArrayList<>());

    static {
        if(Configuracion.INSTANCE.getTEAM_MATCH() || Configuracion.INSTANCE.getDEATH_MATCH()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
        } else {
            scheduler = null;
        }
    }

    public static void subscribeDeathMatch(Jugador player) {
        DeathMatch match = null;

        if (deathMatchs.isEmpty()) {
            new DeathMatch(player);
            return;
        } else {
            for (DeathMatch current : deathMatchs) {
                if (current.isAvailable(player)) {
                    match = current;
                }
            }
        }
        if (match == null) {
            new DeathMatch(player);
        } else {
            match.subscribe(player);
        }
    }

    public static synchronized void subscribeKolizeum(Jugador player, boolean group) {
        TeamMatch match = null;

        if (kolizeums.isEmpty()) {
            if (group) {
                new TeamMatch(player.getParty());
            } else {
                new TeamMatch(player);
            }
            return;
        } else {
            for (TeamMatch current : kolizeums) {
                if (current.isAvailable(player, group)) {
                    match = current;
                }
            }
        }
        if (match == null) {
            if (group) {
                new TeamMatch(player.getParty());
            } else {
                new TeamMatch(player);
            }
        } else if (group) {
            match.subscribe(player.getParty());
        } else {
            match.subscribe(player);
        }
    }

    static synchronized void addTeamMatch(TeamMatch match) {
        kolizeums.add(match);
    }

    public static synchronized void removeTeamMatch(TeamMatch match) {
        for (Jugador player : match.getAllPlayers()) {
            player.koliseo = null;
        }
        kolizeums.remove(match);
    }

    static synchronized void addDeathMatch(DeathMatch match) {
        deathMatchs.add(match);
    }

    public static synchronized void removeDeathMatch(DeathMatch match) {
        deathMatchs.remove(match);
    }
}
