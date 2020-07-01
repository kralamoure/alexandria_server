package org.alexandria.estaticos.evento;

import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.estaticos.evento.tipo.Evento;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.juego.planificador.Updatable;
import org.alexandria.otro.utilidad.Temporizador;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class GestorEvento extends Updatable {

    public final static int TOKEN = 50007, NPC = 16000;
    private final static GestorEvento singleton = new GestorEvento(Database.dinamicos.getEventData().load());

    public static GestorEvento getInstance() {
        return singleton;
    }

    public enum State {
        WAITING, INITIALIZE, PROCESSED, STARTED, FINISHED
    }

    private final Evento[] events;
    private State state = State.WAITING;
    private Evento current, lastest;
    private short count = 0;
    private final List<Jugador> participants = new ArrayList<>();

    private GestorEvento(Evento[] events) {
        super(60000);
        this.events = events;
    }

    public State getState() {
        return state;
    }

    public Evento getCurrentEvent() {
        return current;
    }

    public List<Jugador> getParticipants() {
        return participants;
    }

    public void startNewEvent() {
        Evento event = this.events[Formulas.random.nextInt(this.events.length)];

        if(event != null) {
            if(this.events.length > 1 && this.lastest != null && event.getEventId() == this.lastest.getEventId()) {
                this.startNewEvent();
                return;
            }

            event.prepare();
            this.lastTime = Instant.now().toEpochMilli();
            this.current = event;
            this.state = State.PROCESSED;
           //Mundo.mundo.sendMessageToAll("(<b>Infos</b>) : L'événement '<b>" + event.getEventName() + "</b>' vient de démarrer, <b>.event</b> pour vous inscrire.");
        } else {
            this.startNewEvent();
        }
    }

    private synchronized void startCurrentEvent() {
        if(this.state == State.STARTED)
            return;
        this.state = State.STARTED;

        if(!this.hasEnoughPlayers()) {
            this.count = 0;
            this.lastTime = Instant.now().toEpochMilli();
            this.state = State.PROCESSED;
        } else if(this.moveAllPlayersToEventMap(true)) {
            this.lastTime = Instant.now().toEpochMilli();
            Temporizador.addSiguiente(() -> this.current.perform(), 0, TimeUnit.SECONDS, Temporizador.DataType.CLIENTE);
        }
    }

    public void finishCurrentEvent() {
        this.participants.stream().filter(Objects::nonNull).forEach(player -> {
            player.teleportOldMap();
            player.setBlockMovement(false);
        });

        this.current.interrupt();
        this.lastest = this.current;
        this.current = null;
        this.lastTime = Instant.now().toEpochMilli();
        this.count = 0;
        this.state = State.WAITING;
    }

    public synchronized byte subscribe(final Jugador player) {
        if(this.current == null || this.state == State.WAITING) {
            return 0;
        } else {
            if(this.state == State.PROCESSED) {
                for (Jugador p : this.getParticipants()) {
                    if (player.getAccount() != null && p != null && p.getAccount() != null) {
                        if (player.getAccount().getCurrentIp().compareTo(p.getAccount().getCurrentIp()) == 0) {
                            GestorSalida.GAME_SEND_MESSAGE(player, "Impossible de rejoindre ce combat, vous êtes déjà dans le combat avec une même IP !");
                            return 1;
                        }
                    }
                }
                if (this.participants.size() >= this.current.getMaxPlayers()) {
                    player.sendMessage("(<b>Infos</b>) : L'événement '<b>" + this.current.getEventName() + "</b>' est déjà au complet.");
                } else if (this.participants.contains(player)) {
                    this.participants.remove(player);
                    player.sendMessage("(<b>Infos</b>) : Vous venez de vous désinscrire de l'événement '<b>" + this.current.getEventName() + "</b>'.");
                } else if (this.hasSameIP(player)) {
                    player.sendMessage("(<b>Infos</b>) : Vous avez déjà un membre de votre réseaux internet en jeu sur l'événement.");
                } else if (player.getParty() != null && player.getParty().getMaster() != null) {
                    player.sendMessage("(<b>Infos</b>) : Vous ne pouvez pas rejoindre un événement en étant en mode maître.");
                } else {
                    this.participants.add(player);
                    player.sendMessage("(<b>Infos</b>) : Vous venez de vous inscrire à l'événement '<b>" + this.current.getEventName() + "</b>'.");

                    if (this.participants.size() >= this.current.getMaxPlayers()) {
                        this.startCurrentEvent();
                    } else {
                        this.participants.forEach(target -> target.sendMessage("(<b>Infos</b>) : En attente de " +
                                (this.current.getMaxPlayers() - this.participants.size()) + " joueur(s)."));
                    }
                }
            } else {
                player.sendMessage("(<b>Infos</b>) : L'événement '<b>" + this.current.getEventName() + "</b>' a déjà démarrer.");
            }
        }
        return 1;
    }

    private boolean hasSameIP(Jugador player) {
        if(player != null && player.getAccount() != null) {
            final String ip = player.getAccount().getCurrentIp();

            if(ip.equals("127.0.0.1"))
                return false;
            for (Jugador target : this.participants) {
                if (target != null && target.getAccount() != null) {
                    return ip.equals(target.getAccount().getCurrentIp());
                }

            }
        }
        return false;
    }

    private boolean hasEnoughPlayers() {
        if(this.current == null)
            return false;
        short percent = (short) ((100 * this.participants.size()) / this.current.getMaxPlayers());
        return percent >= 30;
    }

    @Override
    public void update() {
        if(Configuracion.INSTANCE.getAUTO_EVENT() && this.verify()) {
            if (this.state == State.WAITING) {
                short result = (short) (Configuracion.INSTANCE.getTIME_PER_EVENT() - (++count));
                if (result == 0) {
                    this.count = 0;
                    this.lastTime = Instant.now().toEpochMilli();
                    this.state = State.INITIALIZE;
                    Temporizador.addSiguiente(this::startNewEvent, 0, TimeUnit.SECONDS, Temporizador.DataType.CLIENTE);
                } else if (result == 60 || result == 30 || result == 15 || result == 5) {
                    //Mundo.world.sendMessageToAll("(<b>Infos</b>) : Un <b>événement</b> va démarrer dans " + result + " minutes.");
                }
            } else if (this.state == State.PROCESSED) {
                short result = (short) ((this.hasEnoughPlayers() ? 5 : 10) - (++count));
                this.moveAllPlayersToEventMap(false);

                if (result <= 0) {
                    this.startCurrentEvent();
                } else if(result == 1 && this.hasEnoughPlayers()) {
                    for(Jugador player : this.participants) {
                        player.sendMessage("(<b>Infos</b>) : L'événement va commencer dans 1 minute.");
                    }
                }
            }
        }
    }

    @Override
    public Object get() {
        return lastTime;
    }

    private boolean moveAllPlayersToEventMap(boolean teleport) {
        boolean ok = true;
        final StringBuilder afk = teleport ? new StringBuilder("") : null;

        for(final Jugador player : this.participants) {
            if(player.getPelea() != null || !player.isOnline() || player.isGhost() || player.getDoAction()) {
                ok = false;
                this.participants.remove(player);
                player.sendMessage("La prochaine fois tâchez d'être disponible !");
                player.sendMessage("(<b>Infos</b>) : Vous venez d'être expulsé du jeu pour indisponibilité.");

                if(teleport) {
                    afk.append(afk.length() == 0 ? ("<b>" + player.getName() + "</b>") : (", <b>" + player.getName() + "</b>"));
                }
            }
        }

        if(!ok || !teleport) {
            if(teleport) {
                this.participants.forEach(player -> player.sendMessage("(<b>Infos</b> : Merci à " + afk.toString() + " expulsé pour inactivité."));
                Mundo.mundo.getOnlinePlayers().stream().filter(target -> !afk.toString().contains(target.getName()))
                        .forEach(target -> target.sendMessage("(<b>Infos</b> : Il vous reste 30 secondes pour vous inscrire à l'événement '<b>" + this.current.getEventName() + "</b>' (<b>.event</b>)."));
            }
            return false;
        }

        for(final Jugador player : this.participants) {
            if(player.getPelea() == null && player.isOnline() && !player.isGhost() && !player.getDoAction()) {
                player.setOldPosition();
                player.setBlockMovement(true);
                player.teleport(this.current.getMap().getId(), this.current.getEmptyCellForPlayer(player).getId());
                GestorSalida.GAME_SEND_eD_PACKET_TO_MAP(this.current.getMap(), player.getId(), 4);
            } else {
                ok = false;
                this.participants.remove(player);
                player.sendMessage("La prochaine fois tâchez d'être disponible !");
                player.sendMessage("(<b>Infos</b>) : Vous venez d'être expulsé du jeu pour indisponibilité.");
            }
        }

        return ok;
    }

    public static boolean isInEvent(Jugador player) {
        if(Configuracion.INSTANCE.getAUTO_EVENT() && GestorEvento.getInstance().getState() == State.STARTED)
            for(Jugador target : GestorEvento.getInstance().getParticipants())
                if(target.getId() == player.getId())
                    return true;
        return false;
    }
}
