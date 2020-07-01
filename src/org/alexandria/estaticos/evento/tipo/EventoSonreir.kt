package org.alexandria.estaticos.evento.tipo;

import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.Camino;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.estaticos.Npc;
import org.alexandria.estaticos.evento.GestorEvento;
import org.alexandria.estaticos.evento.RecompensaEvento;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.objeto.ObjetoModelo;
import org.alexandria.otro.utilidad.Doble;
import org.alexandria.otro.utilidad.Temporizador;
import org.alexandria.estaticos.area.mapa.Mapa.GameCase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventoSonreir extends Evento {

    private final List<Byte> emotes = new ArrayList<>();
    private final List<Doble<Jugador, List<Byte>>> answers = new ArrayList<>();
    private byte state = 0, count = 0;
    private final short[] cells = {239, 253, 225, 267, 211, 281, 197, 295, 183, 309, 169};
    private Npc animator;

    public EventoSonreir(byte id, byte maxPlayers, String name, String description, RecompensaEvento[] first) {
        super(id, maxPlayers, name, description, first);
        this.map = Mundo.mundo.getMap((short) 9862);
    }

    @Override
    public void prepare() {
        this.answers.clear();
        this.emotes.clear();
        this.state = this.count = 0;
        this.animator = this.map.addNpc(GestorEvento.NPC, (short) 221, 1);

        if (!this.map.getPlayers().isEmpty()) {
            GestorSalida.GAME_SEND_ADD_NPC_TO_MAP(this.map, this.animator);
        }

        Temporizador.addSiguiente(() -> {
            boolean ok = true;
            while (GestorEvento.getInstance().getState() == GestorEvento.State.INITIALIZE || GestorEvento.getInstance().getState() == GestorEvento.State.PROCESSED) {
                moveAnimatorToCellId(ok ? 137 : 221);
                wait(2500);
                ok = !ok;
            }
        }, 0, TimeUnit.SECONDS, Temporizador.DataType.CLIENTE);
    }

    @Override
    public void perform() {
        this.moveAnimatorToCellId(179);
        wait(1500);
        this.animator.setOrientation((byte) 1);
        GestorSalida.GAME_SEND_eD_PACKET_TO_MAP(this.map, this.animator.getId(), 1);
        wait(1000);
        GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(this.map, "", this.animator.getId(), "Event", "Bonjour à tous et bienvenue à l'évent Smiley !");
        wait(3000);
        GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(this.map, "", this.animator.getId(), "Event", "Avant de commencer, laissez moi vous expliquer les règles du jeu.");
        wait(4000);
        GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(this.map, "", this.animator.getId(), "Event", "L'objectif est de reproduire les smileys que j'utiliserai.");
        wait(5000);
        GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(this.map, "", this.animator.getId(), "Event", "Par exemple, si j'utilise ce smiley :");
        wait(1500);
        GestorSalida.GAME_SEND_EMOTICONE_TO_MAP(this.map, this.animator.getId(), 10);
        wait(2000);
        GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(this.map, "", this.animator.getId(), "Event", "Vous devrez utiliser ce smiley aussi.");
        wait(3500);
        GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(this.map, "", this.animator.getId(), "Event", "Attention, si vous vous trompez de smiley, vous serez éliminé(e).");
        wait(4000);
        GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(this.map, "", this.animator.getId(), "Event", "N'oubliez pas d'attendre que le chronomètre démarre avant d'utiliser un smiley.");
        wait(5500);
        GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(this.map, "", this.animator.getId(), "Event", "Vous êtes prêt(e)s ? C'est parti !");
        this.execute();
    }

    @Override
    public void execute() {
        this.count = 0;

        List<Jugador> participants = new ArrayList<>(GestorEvento.getInstance().getParticipants());
        int nbPlayers = participants.size();

        for(Jugador player : participants)
            if(player != null && player.isOnline())
                this.answers.add(new Doble<>(player, new ArrayList<>()));

        //239 cell emote pnj, 179 cell non emote
        while(nbPlayers > 1) {
            this.count++;

            this.moveAnimatorToCellId(134);
            wait(2000);

            this.emotes.add((byte) (Formulas.random.nextInt(14) + 1));

            for(byte e : this.emotes) {
                GestorSalida.GAME_SEND_EMOTICONE_TO_MAP(this.map, this.animator.getId(), e);
                wait(1500 - 100);
            }
            wait(1500);


            this.moveAnimatorToCellId(179);
            wait(1500);

            GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(this.map, "", this.animator.getId(), "Event", "Faites vos jeux !");
            wait(750);

            this.initializeTurn((short) (3000 + 1000 * this.count));
            wait(1500 + 650 * this.count);
            GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(this.map, "", this.animator.getId(), "Event", "Rien ne va plus !");
            wait(1500 + 650 * this.count);
            GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(this.map, "", this.animator.getId(), "Event", "Les jeux sont faits !");
            this.state = 0;

            for(Doble<Jugador, List<Byte>> pair : new ArrayList<>(this.answers)) {
                if(pair.getSegundo().size() == this.count) {
                    byte c = 0;
                    boolean kick = false;
                    for(Byte b1 : pair.getSegundo()) {
                        Byte b2 = this.emotes.get(c);
                        if(b2 == null) {
                            kick = true;
                            break;
                        } else if(b1.byteValue() != b2.byteValue()) {
                            kick = true;
                            break;
                        }
                        c++;
                    }
                    if(kick) {
                        this.kickPlayer(pair.getPrimero());
                        nbPlayers--;
                    } else {
                        pair.getSegundo().clear();
                        pair.getPrimero().sendMessage("(<b>Infos</b>) : Bien joué camarade !");
                    }
                } else {
                    this.kickPlayer(pair.getPrimero());
                    nbPlayers--;
                }
            }

            wait(1000);
            if(GestorEvento.getInstance().getParticipants().size() > 1) {
                GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(this.map, "", this.animator.getId(), "Event", "C'est parti pour le " + (this.count + 1) + "éme tours !");
            }
            wait(2000);
        }

        this.close();
    }

    @Override
    public void close() {
        if(!GestorEvento.getInstance().getParticipants().isEmpty()) {
            Jugador winner = GestorEvento.getInstance().getParticipants().get(0);

            GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(this.map, "", this.animator.getId(), "Event", "Félicitations à " + winner.getName() + " pour ça victoire !");
            winner.sendMessage("(<b>Infos</b>) : Vous venez de remporter 1 jeton !");
            ObjetoModelo template = Mundo.mundo.getObjetoModelo(GestorEvento.TOKEN);

            if(template != null) {
                ObjetoJuego object = template.createNewItem(1, false);

                if (object != null && winner.addObjet(object, true)) {
                    Mundo.addGameObject(object, true);
                }
            }
        } else {
            GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(this.map, "", this.animator.getId(), "Event", "Personne n'a gagnez, une autre fois.. peut être !");
        }

        wait(2000);
        this.moveAnimatorToCellId(344);
        wait(2500);
        this.map.removeNpcOrMobGroup(this.animator.getId());
        this.map.send("GM|-" + this.animator.getId());
        this.map.send("GV");
        GestorEvento.getInstance().finishCurrentEvent();
    }

    public GameCase getEmptyCellForPlayer(Jugador player) {
        return map.getCase(this.cells[count++]);
    }

    @Override
    public void kickPlayer(Jugador player) {
        GestorEvento.getInstance().getParticipants().remove(player);
        Iterator<Doble<Jugador, List<Byte>>> iterator = this.answers.iterator();

        while(iterator.hasNext()) {
            Doble<Jugador, List<Byte>> pair = iterator.next();

            if(pair.getPrimero().getId() == player.getId()) {
                this.map.send("GA;208;" + player.getId() + ";" + player.getCurCell().getId() + ",2916,11,8,1");
                player.sendMessage("(<b>Infos</b>) : Vous avez perdu.. Peut-être une autre fois !");
                player.teleportOldMap();
                player.setBlockMovement(false);
                iterator.remove();
                break;
            }
        }
    }

    @Override
    public boolean onReceivePacket(GestorEvento manager, Jugador player, String packet) throws Exception {
        if(packet.startsWith("BS") && this.state == 1) {
            byte emote = Byte.parseByte(packet.substring(2));
            for(Doble<Jugador, List<Byte>> pair : this.answers) {
                if(pair.getPrimero().getId() == player.getId()) {
                    pair.getSegundo().add(emote);
                    if(pair.getSegundo().size() == this.count)
                        player.sendMessage("(<b>Infos</b>) : Le compte est bon !");
                    break;
                }
            }

        }
        return false;
    }

    private void initializeTurn(short time) {
        this.state = 1;
        for (Jugador player : GestorEvento.getInstance().getParticipants()) {
            player.send("GTS" + player.getId() + "|" + time);
        }
    }

    private void moveAnimatorToCellId(int cellId) {
        String path;

        try {
            path = Camino.getShortestStringPathBetween(this.map, this.animator.getCellId(), cellId, 20);
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }

        if(path != null) {
            this.animator.setCellId(cellId);
            GestorSalida.GAME_SEND_GA_PACKET_TO_MAP(this.map, "0", 1, String.valueOf(this.animator.getId()), path);
        }
    }
}