package org.alexandria.estaticos;

import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.comunes.Formulas;
import org.alexandria.comunes.GestorSalida;
import org.alexandria.comunes.gestorsql.Database;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.configuracion.Constantes;
import org.alexandria.configuracion.Logging;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.objeto.ObjetoJuego;
import org.alexandria.estaticos.objeto.ObjetoModelo;
import org.alexandria.estaticos.oficio.OficioAccion;
import org.alexandria.estaticos.oficio.OficioCaracteristicas;
import org.alexandria.estaticos.oficio.OficioConstantes;
import org.alexandria.otro.utilidad.Doble;
import org.alexandria.estaticos.Mascota.MascotaEntrada;
import org.alexandria.estaticos.Npc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Intercambio {

    protected final Jugador player1, player2;
    protected long kamas1 = 0, kamas2 = 0;
    protected ArrayList<Doble<Integer, Integer>> items1 = new ArrayList<>(),
            items2 = new ArrayList<>();
    protected boolean ok1, ok2;

    public Intercambio(Jugador player1, Jugador player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public static Doble<Integer, Integer> getCoupleInList(
            ArrayList<Doble<Integer, Integer>> items, int id) {
        for (Doble<Integer, Integer> couple : items)
            if (couple.getPrimero() == id)
                return couple;
        return null;
    }

    public abstract boolean toogleOk(int id);

    public abstract void apply();

    public abstract void cancel();

    public static class IntercambioJugador extends Intercambio {

        public IntercambioJugador(Jugador player1, Jugador player2) {
            super(player1, player2);
        }

        private boolean isPodsOK(byte i) {
            if (this instanceof CraftSeguro)
                return true;

            int newpods = 0;
            int oldpods = 0;
            if (i == 1) {
                int podsmax = this.player1.getMaximosPods();
                int pods = this.player1.getPodUsed();
                for (Doble<Integer, Integer> couple : items2) {
                    if (couple.getSegundo() == 0)
                        continue;
                    ObjetoJuego obj = Mundo.getGameObject(couple.getPrimero());
                    newpods += obj.getModelo().getPod() * couple.getSegundo();
                }
                if (newpods == 0) {
                    return true;
                }
                for (Doble<Integer, Integer> couple : items1) {
                    if (couple.getSegundo() == 0)
                        continue;
                    ObjetoJuego obj = Mundo.getGameObject(couple.getPrimero());
                    oldpods += obj.getModelo().getPod() * couple.getSegundo();
                }
                if ((newpods + pods - oldpods) > podsmax) {
                    // Erreur 70
                    // 1 + 70 => 170
                    GestorSalida.GAME_SEND_Im_PACKET(this.player1, "170");
                    return false;
                }
            } else {
                int podsmax = this.player2.getMaximosPods();
                int pods = this.player2.getPodUsed();
                for (Doble<Integer, Integer> couple : items1) {
                    if (couple.getSegundo() == 0)
                        continue;
                    ObjetoJuego obj = Mundo.getGameObject(couple.getPrimero());
                    newpods += obj.getModelo().getPod() * couple.getSegundo();
                }
                if (newpods == 0) {
                    return true;
                }
                for (Doble<Integer, Integer> couple : items2) {
                    if (couple.getSegundo() == 0)
                        continue;
                    ObjetoJuego obj = Mundo.getGameObject(couple.getPrimero());
                    oldpods += obj.getModelo().getPod() * couple.getSegundo();
                }
                if ((newpods + pods - oldpods) > podsmax) {
                    GestorSalida.GAME_SEND_Im_PACKET(this.player2, "170");
                    return false;
                }
            }
            return true;
        }

        public synchronized long getKamas(int guid) {
            int i = 0;
            if (this.player1.getId() == guid)
                i = 1;
            else if (this.player2.getId() == guid)
                i = 2;

            if (i == 1)
                return kamas1;
            else if (i == 2)
                return kamas2;
            return 0;
        }

        public synchronized boolean toogleOk(int guid) {
            byte i = (byte) (this.player1.getId() == guid ? 1 : 2);
            if (this.isPodsOK(i)) {
                if (i == 1) {
                    ok1 = !ok1;
                    GestorSalida.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(), ok1, guid);
                    GestorSalida.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(), ok1, guid);
                } else if (i == 2) {
                    ok2 = !ok2;
                    GestorSalida.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(), ok2, guid);
                    GestorSalida.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(), ok2, guid);
                }
                return (ok1 && ok2);
            }
            return false;
        }

        public synchronized void setKamas(int guid, long k) {
            ok1 = false;
            ok2 = false;

            int i = 0;
            if (this.player1.getId() == guid)
                i = 1;
            else if (this.player2.getId() == guid)
                i = 2;
            duplicacion();
            if (k < 0)
                return;
            if (i == 1) {
                kamas1 = k;
                GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player1, 'G', "", k
                        + "");
                GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player2.getGameClient(), 'G', "", k
                        + "");
            } else if (i == 2) {
                kamas2 = k;
                GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player1.getGameClient(), 'G', "", k
                        + "");
                GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player2, 'G', "", k
                        + "");
            }
        }

        public synchronized void cancel() {
            if (this.player1.getAccount() != null)
                if (this.player1.getGameClient() != null)
                    GestorSalida.GAME_SEND_EV_PACKET(this.player1.getGameClient());
            if (this.player2.getAccount() != null)
                if (this.player2.getGameClient() != null)
                    GestorSalida.GAME_SEND_EV_PACKET(this.player2.getGameClient());
            this.player1.setExchangeAction(null);
            this.player2.setExchangeAction(null);
        }

        public synchronized void apply() {
            StringBuilder str = new StringBuilder();
            try {
                str.append(this.player1.getName()).append(" : ");
                for (Doble<Integer, Integer> couple1 : items1) {
                    str.append(", [").append(Mundo.getGameObject(couple1.getPrimero()).getModelo().getId()).append("@").append(couple1.getPrimero()).append(";").append(couple1.getSegundo()).append("]");
                }
                str.append(" avec ").append(kamas1).append(" K.\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                str.append("Avec ").append(this.player2.getName());
                for (Doble<Integer, Integer> couple2 : items2) {
                    str.append(", [").append(Mundo.getGameObject(couple2.getPrimero()).getModelo().getId()).append("@").append(couple2.getPrimero()).append(";").append(couple2.getSegundo()).append("]");
                }
                str.append(" avec ").append(kamas2).append(" K.");
                if (Configuracion.INSTANCE.getMostrarenviados()) {
                    Logging.objetos.info("Intercambio: " + str);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Gestion des Kamas
            this.player1.addKamas((-kamas1 + kamas2));
            this.player2.addKamas((-kamas2 + kamas1));
            for (Doble<Integer, Integer> couple : items1) // Les items du player vers le player2
            {
                if (couple.getSegundo() == 0)
                    continue;
                if (Mundo.getGameObject(couple.getPrimero()) == null)
                    continue;
                if (Mundo.getGameObject(couple.getPrimero()).getPosicion() != Constantes.ITEM_POS_NO_EQUIPED)
                    continue;
                if (!this.player1.hasItemGuid(couple.getPrimero()))//Si le player n'a pas l'item (Ne devrait pas arriver : wpepro)
                {
                    couple.segundo = 0;//On met la quantit? a 0 pour ?viter les problemes
                    continue;
                }
                ObjetoJuego obj = Mundo.getGameObject(couple.getPrimero());
                if ((obj.getCantidad() - couple.getSegundo()) < 1)//S'il ne reste plus d'item apres l'?change
                {
                    this.player1.removeItem(couple.getPrimero());
                    couple.segundo = obj.getCantidad();
                    GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this.player1, couple.getPrimero());
                    if (!this.player2.addObjet(obj, true))//Si le joueur avait un item similaire
                        Mundo.mundo.removeGameObject(couple.getPrimero());//On supprime l'item inutile
                } else {
                    obj.setCantidad(obj.getCantidad() - couple.getSegundo());
                    GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player1, obj);
                    ObjetoJuego newObj = ObjetoJuego.getCloneObjet(obj, couple.getSegundo());
                    if (this.player2.addObjet(newObj, true))//Si le joueur n'avait pas d'item similaire
                        Mundo.addGameObject(newObj, true);//On ajoute l'item au World
                }
            }
            for (Doble<Integer, Integer> couple : items2) {
                if (couple.getSegundo() == 0)
                    continue;
                if (Mundo.getGameObject(couple.getPrimero()) == null)
                    continue;
                if (Mundo.getGameObject(couple.getPrimero()).getPosicion() != Constantes.ITEM_POS_NO_EQUIPED)
                    continue;
                if (!this.player2.hasItemGuid(couple.getPrimero()))//Si le player n'a pas l'item (Ne devrait pas arriver)
                {
                    couple.segundo = 0;//On met la quantit? a 0 pour ?viter les problemes
                    continue;
                }
                this.giveObject(couple, Mundo.getGameObject(couple.getPrimero()));
            }
            //Fin
            this.player1.setExchangeAction(null);
            this.player2.setExchangeAction(null);
            GestorSalida.GAME_SEND_Ow_PACKET(this.player1);
            GestorSalida.GAME_SEND_Ow_PACKET(this.player2);
            GestorSalida.GAME_SEND_STATS_PACKET(this.player1);
            GestorSalida.GAME_SEND_STATS_PACKET(this.player2);
            GestorSalida.GAME_SEND_EXCHANGE_VALID(this.player1.getGameClient(), 'a');
            GestorSalida.GAME_SEND_EXCHANGE_VALID(this.player2.getGameClient(), 'a');
            Database.dinamicos.getPlayerData().update(this.player1);
            Database.dinamicos.getPlayerData().update(this.player2);
        }

        protected void giveObject(Doble<Integer, Integer> couple, ObjetoJuego object) {
            if(object == null) return;
            if ((object.getCantidad() - couple.getSegundo()) < 1) {
                this.player2.removeItem(couple.getPrimero());
                couple.segundo = object.getCantidad();
                GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this.player2, couple.getPrimero());
                if (!this.player1.addObjet(object, true)) Mundo.mundo.removeGameObject(couple.getPrimero());
            } else {
                object.setCantidad(object.getCantidad() - couple.getSegundo());
                GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player2, object);
                ObjetoJuego newObj = ObjetoJuego.getCloneObjet(object, couple.getSegundo());
                if (this.player1.addObjet(newObj, true)) Mundo.addGameObject(newObj, true);
            }
        }

        public synchronized void addItem(int guid, int qua, int pguid) {
            ok1 = false;
            ok2 = false;

            ObjetoJuego obj = Mundo.getGameObject(guid);
            int i = 0;

            if (this.player1.getId() == pguid)
                i = 1;
            if (this.player2.getId() == pguid)
                i = 2;

            if (qua == 1)
                qua = 1;
            String str = guid + "|" + qua;
            if (obj == null)
                return;
            if (obj.getPosicion() != Constantes.ITEM_POS_NO_EQUIPED)
                return;

            if (this instanceof CraftSeguro) {
                ArrayList<ObjetoModelo> tmp = new ArrayList<>();
                for (Doble<Integer, Integer> couple : this.items1) {
                    ObjetoJuego _tmp = Mundo.getGameObject(couple.getPrimero());
                    if (_tmp == null)
                        continue;
                    if (!tmp.contains(_tmp.getModelo()))
                        tmp.add(_tmp.getModelo());
                }
                for (Doble<Integer, Integer> couple : this.items2) {
                    ObjetoJuego _tmp = Mundo.getGameObject(couple.getPrimero());
                    if (_tmp == null)
                        continue;
                    if (!tmp.contains(_tmp.getModelo()))
                        tmp.add(_tmp.getModelo());
                }

                if (!tmp.contains(obj.getModelo())) {
                    if (tmp.size() + 1 > ((CraftSeguro) this).getMaxCase()) {
                        GestorSalida.GAME_SEND_MESSAGE((this.player1.getId() == pguid) ? this.player1 : this.player2, "Impossible d'ajouter plus d'ingrédients.", "B9121B");
                        return;
                    }
                }
            }

            String add = "|" + obj.getModelo().getId() + "|"
                    + obj.parseStatsString();
            duplicacion();
            if (i == 1) {
                Doble<Integer, Integer> couple = getCoupleInList(items1, guid);
                if (couple != null) {
                    couple.segundo += qua;
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player1, 'O', "+", ""
                            + guid + "|" + couple.getSegundo());
                    GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player2.getGameClient(), 'O', "+", ""
                            + guid + "|" + couple.getSegundo() + add);
                    return;
                }
                GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player1, 'O', "+", str);
                GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player2.getGameClient(), 'O', "+", str
                        + add);
                items1.add(new Doble<>(guid, qua));
            } else if (i == 2) {
                Doble<Integer, Integer> couple = getCoupleInList(items2, guid);
                if (couple != null) {
                    couple.segundo += qua;
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player2, 'O', "+", ""
                            + guid + "|" + couple.getSegundo());
                    GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player1.getGameClient(), 'O', "+", ""
                            + guid + "|" + couple.getSegundo() + add);
                    return;
                }
                GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player2, 'O', "+", str);
                GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player1.getGameClient(), 'O', "+", str
                        + add);
                items2.add(new Doble<>(guid, qua));
            }
        }

        private void duplicacion() {
            GestorSalida.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(), ok1, this.player1.getId());
            GestorSalida.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(), ok1, this.player1.getId());
            GestorSalida.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(), ok2, this.player2.getId());
            GestorSalida.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(), ok2, this.player2.getId());
        }

        public synchronized void removeItem(int guid, int qua, int pguid) {
            int i = 0;
            if (this.player1.getId() == pguid)
                i = 1;
            else if (this.player2.getId() == pguid)
                i = 2;
            ok1 = false;
            ok2 = false;

            duplicacion();

            ObjetoJuego object = Mundo.getGameObject(guid);
            if (object == null) return;
            String add = "|" + object.getModelo().getId() + "|" + object.parseStatsString();

            if (i == 1) {
                Doble<Integer, Integer> couple = getCoupleInList(items1, guid);

                if(couple == null) return;
                int newQua = couple.getSegundo() - qua;

                if (newQua < 1) {
                    items1.remove(couple);
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player1, 'O', "-", "" + guid);
                    GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player2.getGameClient(), 'O', "-", "" + guid);
                } else {
                    couple.segundo = newQua;
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player1, 'O', "+", "" + guid + "|" + newQua);
                    GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player2.getGameClient(), 'O', "+", "" + guid + "|" + newQua + add);
                }
            } else if (i == 2) {
                Doble<Integer, Integer> couple = getCoupleInList(items2, guid);

                if(couple == null) return;
                int newQua = couple.getSegundo() - qua;

                if (newQua < 1) {
                    items2.remove(couple);
                    GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player1.getGameClient(), 'O', "-", "" + guid);
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player2, 'O', "-", "" + guid);
                } else {
                    couple.segundo = newQua;
                    GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player1.getGameClient(), 'O', "+", "" + guid + "|" + newQua + add);
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player2, 'O', "+", "" + guid + "|" + newQua);
                }
            }
        }

        public synchronized int getQuaItem(int itemID, int playerGuid) {
            ArrayList<Doble<Integer, Integer>> items;
            if (this.player1.getId() == playerGuid)
                items = items1;
            else
                items = items2;
            for (Doble<Integer, Integer> curCoupl : items)
                if (curCoupl.getPrimero() == itemID)
                    return curCoupl.getSegundo();
            return 0;
        }

        //Otros intercambios
        public static class NpcExchange {
            private final Jugador player;
            private NpcModelo npc;
            private long kamas1 = 0;
            private long kamas2 = 0;
            private final ArrayList<Doble<Integer,Integer>> items1 = new ArrayList<>();
            private final ArrayList<Doble<Integer,Integer>> items2 = new ArrayList<>();
            private boolean ok1;
            private boolean ok2;

            public NpcExchange(Jugador p, NpcModelo n) {
                this.player = p;
                this.setNpc(n);
            }

            public synchronized long getKamas(boolean b) {
                if(b)return this.kamas2;
                return this.kamas1;
            }

            public synchronized void toogleOK(boolean paramBoolean) {
                if(paramBoolean) {
                    this.ok2 = (!this.ok2);
                    GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
                } else {
                    this.ok1 = (!this.ok1);
                    GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok1, this.player.getId());
                }
                if((this.ok2) && (this.ok1))
                    apply();
            }

            public synchronized void setKamas(boolean ok, long kamas) {
                if(kamas < 0L)
                    return;
                this.ok2 = (this.ok1 = false);
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok1, this.player.getId());
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
                if(ok) {
                    this.kamas2 = kamas;
                    GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(), 'G', "", String.valueOf(kamas));
                    putAllGiveItem();
                    return;
                }
                if(kamas > this.player.getKamas())
                    return;
                this.kamas1 = kamas;
                GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player, 'G', "", String.valueOf(kamas));
                putAllGiveItem();
            }

            public synchronized void cancel() {
                if((this.player.getAccount() != null) && (this.player.getGameClient() != null))
                    GestorSalida.GAME_SEND_EV_PACKET(this.player.getGameClient());
                this.player.setExchangeAction(null);
            }

            public synchronized void apply() {
                for(Doble<Integer, Integer> couple : items1) {
                    if(couple.getSegundo() == 0)continue;
                    if(Mundo.getGameObject(couple.getPrimero()).getPosicion() != Constantes.ITEM_POS_NO_EQUIPED)continue;
                    if(!this.player.hasItemGuid(couple.getPrimero())) {
                        couple.segundo = 0;//On met la quantité a 0 pour éviter les problemes
                        continue;
                    }
                    ObjetoJuego obj = Mundo.getGameObject(couple.getPrimero());
                    if((obj.getCantidad() - couple.getSegundo()) < 1) {
                        this.player.removeItem(couple.getPrimero());
                        Mundo.mundo.removeGameObject(Mundo.getGameObject(couple.getPrimero()).getId());
                        couple.segundo = obj.getCantidad();
                        GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this.player, couple.getPrimero());
                    } else {
                        obj.setCantidad(obj.getCantidad()-couple.getSegundo());
                        GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, obj);
                    }
                }

                for(Doble<Integer, Integer> couple1 : items2) {
                    if(couple1.getSegundo() == 0)continue;
                    if(Mundo.mundo.getObjetoModelo(couple1.getPrimero()) == null)continue;
                    ObjetoJuego obj1 = Mundo.mundo.getObjetoModelo(couple1.getPrimero()).createNewItem(couple1.getSegundo(), false);
                    if(this.player.addObjet(obj1, true))
                        Mundo.addGameObject(obj1, true);
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "021;" + couple1.getSegundo() + "~" + couple1.getPrimero());
                }
                this.player.setExchangeAction(null);
                GestorSalida.GAME_SEND_EXCHANGE_VALID(this.player.getGameClient(), 'a');
                Database.dinamicos.getPlayerData().update(this.player);
            }

            public synchronized void addItem(int obj, int qua) {
                if(qua <= 0)return;
                if(Mundo.getGameObject(obj) == null)return;
                this.ok1 = (this.ok2 = false);
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok1, this.player.getId());
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
                String str = obj + "|" + qua;
                Doble<Integer,Integer> couple = getCoupleInList(items1, obj);
                if(couple != null) {
                    couple.segundo += qua;
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player, 'O', "+", ""+obj+"|"+couple.getSegundo());
                    putAllGiveItem();
                    return;
                }
                GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player, 'O', "+", str);
                items1.add(new Doble<>(obj, qua));
                putAllGiveItem();
            }

            public synchronized void removeItem(int guid, int qua) {
                if(qua < 0)return;
                this.ok1 = (this.ok2 = false);
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok1, this.player.getId());
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
                if(Mundo.getGameObject(guid) == null)return;
                Doble<Integer,Integer> couple = getCoupleInList(items1,guid);
                int newQua = couple.getSegundo() - qua;
                if(newQua <1) {
                    items1.remove(couple);
                    putAllGiveItem();
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player, 'O', "-", ""+guid);
                } else {
                    couple.segundo = newQua;
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player, 'O', "+", ""+guid+"|"+newQua);
                    putAllGiveItem();
                }
            }

            public synchronized int getQuaItem(int obj, boolean b) {
                ArrayList<Doble<Integer, Integer>> list;
                if(b)
                    list = this.items2;
                else
                    list = this.items1;
                for(Doble<Integer, Integer> item: list)
                    if(item.getPrimero() == obj)
                        return item.getSegundo();
                return 0;
            }

            public synchronized void clearItems() {
                if(this.items2.isEmpty()) return;
                for(Doble<Integer, Integer> i: items2)
                    GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(), 'O', "-", i.getPrimero()+"");
                this.kamas2 = 0;
                this.items2.clear();
                if(this.ok2) {
                    this.ok2 = false;
                    GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
                }
            }

            private synchronized Doble<Integer, Integer> getCoupleInList(ArrayList<Doble<Integer, Integer>> items, int guid) {
                for(Doble<Integer, Integer> couple : items)
                    if(couple.getPrimero() == guid)
                        return couple;
                return null;
            }

            public synchronized void putAllGiveItem() {
                ArrayList<Doble<Integer,Integer>> objects = this.npc.checkGetObjects(this.items1);

                if(objects != null) {
                    this.clearItems();
                    for(Doble<Integer, Integer> object : objects) {
                        if(object.getSegundo() == -1) {
                            int kamas = object.getPrimero();

                            if(kamas == -1) {
                                for(Doble<Integer, Integer> pepite : this.items1)
                                    if(Mundo.getGameObject(pepite.getPrimero()).getModelo().getId() == 1)
                                        this.kamas2 += Integer.parseInt(Mundo.getGameObject(pepite.getPrimero()).getTxtStat().get(990).substring(9, 13)) * pepite.getSegundo();

                                GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(), 'G', "", String.valueOf(this.kamas2));
                                continue;
                            }

                            this.kamas2 += kamas;
                            GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(), 'G', "", String.valueOf(this.kamas2));
                            continue;
                        }
                        String str = object.getPrimero() + "|" + object.getSegundo() + "|" + object.getPrimero() + "|" + Mundo.mundo.getObjetoModelo(object.getPrimero()).getStrTemplate();
                        this.items2.add(new Doble<>(object.getPrimero(), object.getSegundo()));
                        GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(), 'O', "+", str);
                    }
                    if(!this.ok2) {
                        this.ok2 = true;
                        GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
                    }
                } else {
                    this.clearItems();
                }
            }

            public NpcModelo getNpc() {
                return npc;
            }

            public void setNpc(NpcModelo npc) {
                this.npc = npc;
            }
        }

        public static class NpcExchangePets {
            private final Jugador player;
            private NpcModelo npc;
            private final ArrayList<Doble<Integer, Integer>> items1 = new ArrayList<>();
            private final ArrayList<Doble<Integer, Integer>> items2 = new ArrayList<>();
            private boolean ok1;
            private boolean ok2;

            public NpcExchangePets(Jugador p, NpcModelo n) {
                this.player = p;
                this.npc = n;
            }

            public synchronized void toogleOK(boolean paramBoolean) {
                if (paramBoolean) {
                    this.ok2 = (!this.ok2);
                    GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
                } else {
                    this.ok1 = (!this.ok1);
                    GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok1, this.player.getId());
                }
                if ((this.ok2) && (this.ok1))
                    apply();
            }

            public synchronized void setKamas(boolean paramBoolean, long paramLong) {
                if (paramLong < 0L)
                    return;
                this.ok2 = (this.ok1 = false);
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok1, this.player.getId());
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
                if (paramBoolean) {
                    GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(), 'G', "", paramLong
                            + "");
                    return;
                }
                if (paramLong > this.player.getKamas())
                    return;
                GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player, 'G', "", paramLong
                        + "");
            }

            public synchronized void cancel() {
                if ((this.player.getAccount() != null) && (this.player.getGameClient() != null))
                    GestorSalida.GAME_SEND_EV_PACKET(this.player.getGameClient());
                this.player.setExchangeAction(null);
            }

            public synchronized void apply() {
                ObjetoJuego objetToChange = null;
                for (Doble<Integer, Integer> couple : items1) {
                    if (couple.getSegundo() == 0)
                        continue;
                    if (Mundo.getGameObject(couple.getPrimero()).getPosicion() != Constantes.ITEM_POS_NO_EQUIPED)
                        continue;
                    if (!player.hasItemGuid(couple.getPrimero()))//Si le player n'a pas l'item (Ne devrait pas arriver)
                    {
                        couple.segundo = 0;//On met la quantit? a 0 pour ?viter les problemes
                        continue;
                    }
                    ObjetoJuego obj = Mundo.getGameObject(couple.getPrimero());
                    objetToChange = obj;
                    if ((obj.getCantidad() - couple.getSegundo()) < 1)//S'il ne reste plus d'item apres l'?change
                    {
                        player.removeItem(couple.getPrimero());
                        couple.segundo = obj.getCantidad();
                        GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(player, couple.getPrimero());
                    } else {
                        obj.setCantidad(obj.getCantidad() - couple.getSegundo());
                        GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(player, obj);
                    }
                }

                for (Doble<Integer, Integer> couple1 : items2) {
                    if (couple1.getSegundo() == 0)
                        continue;
                    if (Mundo.mundo.getObjetoModelo(couple1.getPrimero()) == null)
                        continue;
                    assert objetToChange != null;
                    if (Mundo.getGameObject(objetToChange.getId()) == null)
                        continue;
                    ObjetoJuego obj1 = null;
                    if (Mundo.mundo.getObjetoModelo(couple1.getPrimero()).getType() == 18)
                        obj1 = Mundo.mundo.getObjetoModelo(couple1.getPrimero()).createNewFamilier(objetToChange);
                    if (Mundo.mundo.getObjetoModelo(couple1.getPrimero()).getType() == 77)
                        obj1 = Mundo.mundo.getObjetoModelo(couple1.getPrimero()).createNewCertificat(objetToChange);

                    if (obj1 == null)
                        continue;
                    if (this.player.addObjet(obj1, true))
                        Mundo.addGameObject(obj1, true);
                    GestorSalida.GAME_SEND_Im_PACKET(this.player, "021;"
                            + couple1.getSegundo() + "~" + couple1.getPrimero());
                }
                assert objetToChange != null;
                Mundo.mundo.removeGameObject(objetToChange.getId());
                this.player.setExchangeAction(null);
                GestorSalida.GAME_SEND_EXCHANGE_VALID(this.player.getGameClient(), 'a');
                GestorSalida.GAME_SEND_Ow_PACKET(this.player);
                Database.dinamicos.getPlayerData().update(this.player);
            }

            public synchronized void addItem(int obj, int qua) {
                if (qua <= 0)
                    return;
                if (Mundo.getGameObject(obj) == null)
                    return;
                this.ok1 = (this.ok2 = false);
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok1, this.player.getId());
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
                String str = obj + "|" + qua;
                Doble<Integer, Integer> couple = getCoupleInList(items1, obj);
                if (couple != null) {
                    couple.segundo += qua;
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(player, 'O', "+", ""
                            + obj + "|" + couple.getSegundo());
                    return;
                }
                GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(player, 'O', "+", str);
                items1.add(new Doble<>(obj, qua));
                if (verifIfAlonePets() || verifIfAloneParcho()) {
                    if (items1.size() == 1) {
                        int id = -1;
                        ObjetoJuego objet = null;
                        for (Doble<Integer, Integer> i : items1) {
                            if (Mundo.getGameObject(i.getPrimero()) == null)
                                continue;
                            objet = Mundo.getGameObject(i.getPrimero());
                            if (Mundo.getGameObject(i.getPrimero()).getModelo().getType() == 18) {
                                id = Constantes.getParchoByIdPets(Mundo.getGameObject(i.getPrimero()).getModelo().getId());
                            } else if (Mundo.getGameObject(i.getPrimero()).getModelo().getType() == 77) {
                                id = Constantes.getPetsByIdParcho(Mundo.getGameObject(i.getPrimero()).getModelo().getId());
                            }
                        }
                        if (id == -1)
                            return;
                        String str1 = id + "|" + 1 + "|" + id + "|"
                                + objet.parseStatsString();
                        this.items2.add(new Doble<>(id, 1));
                        GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(), 'O', "+", str1);
                        this.ok2 = true;
                        GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
                    } else {
                        clearNpcItems();
                        this.ok2 = false;
                        GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
                    }
                } else {
                    clearNpcItems();
                    this.ok2 = false;
                    GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
                }
            }

            public synchronized void removeItem(int guid, int qua) {
                if (qua < 0)
                    return;
                this.ok1 = (this.ok2 = false);
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok1, this.player.getId());
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
                if (Mundo.getGameObject(guid) == null)
                    return;
                Doble<Integer, Integer> couple = getCoupleInList(items1, guid);
                int newQua = couple.getSegundo() - qua;
                if (newQua < 1)//Si il n'y a pu d'item
                {
                    items1.remove(couple);
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player, 'O', "-", ""
                            + guid);
                } else {
                    couple.segundo = newQua;
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.player, 'O', "+", ""
                            + guid + "|" + newQua);
                }
                if (verifIfAlonePets()) {
                    if (items1.size() == 1) {
                        int id = -1;
                        ObjetoJuego objet = null;
                        for (Doble<Integer, Integer> i : items1) {
                            if (Mundo.getGameObject(i.getPrimero()) == null)
                                continue;
                            objet = Mundo.getGameObject(i.getPrimero());
                            if (Mundo.getGameObject(i.getPrimero()).getModelo().getType() == 18) {
                                id = Constantes.getParchoByIdPets(Mundo.getGameObject(i.getPrimero()).getModelo().getId());
                            } else if (Mundo.getGameObject(i.getPrimero()).getModelo().getType() == 77) {
                                id = Constantes.getPetsByIdParcho(Mundo.getGameObject(i.getPrimero()).getModelo().getId());
                            }
                        }
                        if (id == -1)
                            return;
                        String str = id + "|" + 1 + "|" + id + "|"
                                + objet.parseStatsString();
                        this.items2.add(new Doble<>(id, 1));
                        GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(), 'O', "+", str);
                        this.ok2 = true;
                        GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
                    } else {
                        clearNpcItems();
                        this.ok2 = false;
                        GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
                    }
                } else {
                    clearNpcItems();
                    this.ok2 = false;
                    GestorSalida.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
                }
            }

            public boolean verifIfAlonePets() {
                for (Doble<Integer, Integer> i : items1)
                    if (Mundo.getGameObject(i.getPrimero()).getModelo().getType() != 18)
                        return false;
                return true;
            }

            public boolean verifIfAloneParcho() {
                for (Doble<Integer, Integer> i : items1)
                    if (Mundo.getGameObject(i.getPrimero()).getModelo().getType() != 77)
                        return false;
                return true;
            }

            public synchronized void clearNpcItems() {
                for (Doble<Integer, Integer> i : items2)
                    GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(), 'O', "-", i.getPrimero()
                            + "");
                this.items2.clear();
            }

            private synchronized Doble<Integer, Integer> getCoupleInList(
                    ArrayList<Doble<Integer, Integer>> items, int guid) {
                for (Doble<Integer, Integer> couple : items)
                    if (couple.getPrimero() == guid)
                        return couple;
                return null;
            }

            public synchronized int getQuaItem(int obj, boolean b) {
                ArrayList<Doble<Integer, Integer>> list;
                if (b)
                    list = this.items2;
                else
                    list = this.items1;

                for (Doble<Integer, Integer> item : list)
                    if (item.getPrimero() == obj)
                        return item.getSegundo();
                return 0;
            }

            public NpcModelo getNpc() {
                return npc;
            }

            public void setNpc(NpcModelo npc) {
                this.npc = npc;
            }
        }

        public static class NpcRessurectPets {
            private final Jugador perso;
            private NpcModelo npc;
            private long kamas1 = 0;
            private long kamas2 = 0;
            private final ArrayList<Doble<Integer, Integer>> items1 = new ArrayList<>();
            private final ArrayList<Doble<Integer, Integer>> items2 = new ArrayList<>();
            private boolean ok1;
            private boolean ok2;

            public NpcRessurectPets(Jugador p, NpcModelo n) {
                this.perso = p;
                this.npc = n;
            }

            public synchronized long getKamas(boolean b) {
                if (b)
                    return this.kamas2;
                return this.kamas1;
            }

            public synchronized void toogleOK(boolean paramBoolean) {
                if (paramBoolean) {
                    this.ok2 = (!this.ok2);
                    GestorSalida.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(), this.ok2);
                } else {
                    this.ok1 = (!this.ok1);
                    GestorSalida.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(), this.ok1, this.perso.getId());
                }
                if ((this.ok2) && (this.ok1))
                    apply();
            }

            public synchronized void setKamas(boolean paramBoolean, long paramLong) {
                if (paramLong < 0L)
                    return;
                this.ok2 = (this.ok1 = false);
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(), this.ok1, this.perso.getId());
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(), this.ok2);
                if (paramBoolean) {
                    this.kamas2 = paramLong;
                    GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.perso.getGameClient(), 'G', "", paramLong
                            + "");
                    return;
                }
                if (paramLong > this.perso.getKamas())
                    return;
                this.kamas1 = paramLong;
                GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.perso, 'G', "", paramLong
                        + "");
            }

            public synchronized void cancel() {
                if ((this.perso.getAccount() != null)
                        && (this.perso.getGameClient() != null))
                    GestorSalida.GAME_SEND_EV_PACKET(this.perso.getGameClient());
                this.perso.setExchangeAction(null);
            }

            public synchronized void apply() {
                for (Doble<Integer, Integer> item : items1) {
                    ObjetoJuego object = Mundo.getGameObject(item.getPrimero());
                    if (object.getModelo().getId() == 8012) {
                        if ((object.getCantidad() - item.getSegundo()) < 1) {
                            perso.removeItem(item.getPrimero());
                            item.segundo = object.getCantidad();
                            GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(perso, item.getPrimero());
                        } else {
                            object.setCantidad(object.getCantidad() - item.getSegundo());
                            GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(perso, object);
                        }
                    } else {
                        MascotaEntrada pet = Mundo.mundo.getPetsEntry(item.getPrimero());
                        if (pet != null) {
                            pet.resurrection();
                            GestorSalida.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(this.perso, object);
                        }
                    }
                }
                this.perso.setExchangeAction(null);
                GestorSalida.GAME_SEND_EXCHANGE_VALID(this.perso.getGameClient(), 'a');
                GestorSalida.GAME_SEND_Ow_PACKET(this.perso);
                Database.dinamicos.getPlayerData().update(this.perso);
            }

            public synchronized void addItem(int obj, int qua) {
                if (qua <= 0)
                    return;
                if (Mundo.getGameObject(obj) == null)
                    return;
                this.ok1 = (this.ok2 = false);
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(), this.ok1, this.perso.getId());
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(), this.ok2);
                String str = obj + "|" + qua;
                Doble<Integer, Integer> couple = getCoupleInList(items1, obj);
                if (couple != null) {
                    couple.segundo += qua;
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(perso, 'O', "+", ""
                            + obj + "|" + couple.getSegundo());
                    return;
                }
                GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(perso, 'O', "+", str);
                items1.add(new Doble<>(obj, qua));
                if (verification()) {
                    if (items1.size() == 2) {
                        int id = -1;
                        ObjetoJuego objet = null;

                        for (Doble<Integer, Integer> i : items1) {
                            objet = Mundo.getGameObject(i.getPrimero());

                            if (objet == null)
                                continue;
                            if (objet.getModelo().getType() == 90) {
                                id = Mundo.mundo.getPetsEntry(i.getPrimero()).getModelo();
                                break;
                            }
                        }

                        if (id == -1 || objet == null)
                            return;
                        String str1 = id + "|" + 1 + "|" + id + "|"
                                + objet.parseStatsString();
                        this.items2.add(new Doble<>(id, 1));
                        GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.perso.getGameClient(), 'O', "+", str1);
                        this.ok2 = true;
                        GestorSalida.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(), this.ok2);
                    } else {
                        clearNpcItems();
                        this.ok2 = false;
                        GestorSalida.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(), this.ok2);
                    }
                } else {
                    clearNpcItems();
                    this.ok2 = false;
                    GestorSalida.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(), this.ok2);
                }
            }

            public synchronized void removeItem(int guid, int qua) {
                if (qua < 0)
                    return;
                this.ok1 = (this.ok2 = false);
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(), this.ok1, this.perso.getId());
                GestorSalida.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(), this.ok2);
                if (Mundo.getGameObject(guid) == null)
                    return;
                Doble<Integer, Integer> couple = getCoupleInList(items1, guid);
                int newQua = couple.getSegundo() - qua;
                if (newQua < 1)//Si il n'y a pu d'item
                {
                    items1.remove(couple);
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.perso, 'O', "-", ""
                            + guid);
                } else {
                    couple.segundo = newQua;
                    GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(this.perso, 'O', "+", ""
                            + guid + "|" + newQua);
                }
                if (verification()) {
                    if (items1.size() == 2) {
                        int id = -1;
                        ObjetoJuego objet = null;

                        for (Doble<Integer, Integer> i : items1) {
                            objet = Mundo.getGameObject(i.getPrimero());

                            if (objet == null)
                                continue;
                            if (objet.getModelo().getType() == 90) {
                                id = Mundo.mundo.getPetsEntry(i.getPrimero()).getModelo();
                                break;
                            }
                        }

                        if (id == -1 || objet == null)
                            return;

                        String str = id + "|" + 1 + "|" + id + "|"
                                + objet.parseStatsString();
                        this.items2.add(new Doble<>(id, 1));
                        GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.perso.getGameClient(), 'O', "+", str);
                        this.ok2 = true;
                        GestorSalida.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(), this.ok2);
                    } else {
                        clearNpcItems();
                        this.ok2 = false;
                        GestorSalida.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(), this.ok2);
                    }
                } else {
                    clearNpcItems();
                    this.ok2 = false;
                    GestorSalida.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(), this.ok2);
                }
            }

            public boolean verification() {
                boolean verif = true;
                for (Doble<Integer, Integer> item : items1) {
                    ObjetoJuego object = Mundo.getGameObject(item.getPrimero());
                    if ((object.getModelo().getId() != 8012 && object.getModelo().getType() != 90)
                            || item.getSegundo() > 1)
                        verif = false;
                }
                return verif;
            }

            public synchronized void clearNpcItems() {
                for (Doble<Integer, Integer> i : items2)
                    GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.perso.getGameClient(), 'O', "-", i.getPrimero()
                            + "");
                this.items2.clear();
            }

            private synchronized Doble<Integer, Integer> getCoupleInList(
                    ArrayList<Doble<Integer, Integer>> items, int guid) {
                for (Doble<Integer, Integer> couple : items)
                    if (couple.getPrimero() == guid)
                        return couple;
                return null;
            }

            public synchronized int getQuaItem(int obj, boolean b) {
                ArrayList<Doble<Integer, Integer>> list;
                if (b)
                    list = this.items2;
                else
                    list = this.items1;

                for (Doble<Integer, Integer> item : list)
                    if (item.getPrimero() == obj)
                        return item.getSegundo();
                return 0;
            }

            public NpcModelo getNpc() {
                return npc;
            }

            public void setNpc(NpcModelo npc) {
                this.npc = npc;
            }
        }
        //Fin otros intercambios
    }

    public static class CraftSeguro extends IntercambioJugador {

        private long payKamas = 0;
        private long payIfSuccessKamas = 0;
        private int maxCase = 9;

        private final ArrayList<Doble<Integer, Integer>> payItems = new ArrayList<>();
        private final ArrayList<Doble<Integer, Integer>> payItemsIfSuccess = new ArrayList<>();

        public CraftSeguro(Jugador player1, Jugador player2) {
            super(player1, player2);
            OficioCaracteristicas job = this.player1.getMetierBySkill(this.player1.getIsCraftingType().get(1));
            boolean maging = job.getTemplate().isMaging();
            int nb = maging ? 3 : OficioConstantes.getTotalCaseByJobLevel(job.get_lvl());
            this.maxCase = nb;
        }

        public Jugador getNeeder() {
            return player2;
        }

        public int getMaxCase() {
            return maxCase;
        }

        public synchronized void apply() {
            OficioCaracteristicas jobStat = this.player1.getMetierBySkill(this.player1.getIsCraftingType().get(1));

            if (jobStat == null)
                return;

            OficioAccion jobAction = jobStat.getJobActionBySkill(this.player1.getIsCraftingType().get(1));

            if (jobAction == null)
                return;

            Map<Jugador, ArrayList<Doble<Integer, Integer>>> items = new HashMap<>();
            items.put(this.player1, this.items1);
            items.put(this.player2, this.items2);

            int sizeList = jobAction.sizeList(items);

            boolean success = jobAction.craftPublicMode(this.player1, this.player2, items);

            this.player1.addKamas(payKamas + (success ? payIfSuccessKamas : 0));
            this.player2.addKamas(-payKamas - (success ? payIfSuccessKamas : 0));


            if (success) this.giveObjects(this.payItems, this.payItemsIfSuccess);
            else this.giveObjects(this.payItems);

            int winXP = 0;
            if (success)
                winXP = Formulas.calculXpWinCraft(jobStat.get_lvl(), sizeList) * Configuracion.INSTANCE.getRATE_JOB();
            else if (!jobStat.getTemplate().isMaging())
                winXP = Formulas.calculXpWinCraft(jobStat.get_lvl(), sizeList) * Configuracion.INSTANCE.getRATE_JOB();

            if (winXP > 0) {
                jobStat.addXp(this.player1, winXP);
                ArrayList<OficioCaracteristicas> SMs = new ArrayList<>();
                SMs.add(jobStat);
                GestorSalida.GAME_SEND_JX_PACKET(this.player1, SMs);
            }

            GestorSalida.GAME_SEND_STATS_PACKET(this.player1);
            GestorSalida.GAME_SEND_STATS_PACKET(this.player2);

            this.payIfSuccessKamas = 0;
            this.payKamas = 0;
            this.payItems.clear();
            this.payItemsIfSuccess.clear();
            this.items1.clear();
            this.items2.clear();
            this.ok1 = false;
            this.ok2 = false;
            GestorSalida.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(), ok1, this.player1.getId());
            GestorSalida.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(), ok1, this.player1.getId());
            GestorSalida.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(), ok2, this.player2.getId());
            GestorSalida.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(), ok2, this.player2.getId());

        }

        @SafeVarargs
        private void giveObjects(ArrayList<Doble<Integer, Integer>>... arrays) {
            for(ArrayList<Doble<Integer, Integer>> array : arrays) {
                for (Doble<Integer, Integer> couple : array) {
                    if (couple.getSegundo() == 0)
                        continue;

                    ObjetoJuego object = Mundo.getGameObject(couple.getPrimero());

                    if (object == null)
                        continue;
                    if (object.getPosicion() != Constantes.ITEM_POS_NO_EQUIPED)
                        continue;
                    if (!this.player2.hasItemGuid(couple.getPrimero())) {
                        couple.segundo = 0;
                        continue;
                    }

                    this.giveObject(couple, object);
                }
            }
        }

        public synchronized void cancel() {
            this.send("EV");
            this.player1.getIsCraftingType().clear();
            this.player2.getIsCraftingType().clear();
            this.player1.setExchangeAction(null);
            this.player2.setExchangeAction(null);
        }

        public void setPayKamas(byte type, long kamas) {
            if (kamas < 0)
                return;
            if (this.player2.getKamas() < kamas)
                kamas = this.player2.getKamas();

            this.ok1 = false;
            this.ok2 = false;
            GestorSalida.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(), ok1, this.player1.getId());
            GestorSalida.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(), ok1, this.player1.getId());
            GestorSalida.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(), ok2, this.player2.getId());
            GestorSalida.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(), ok2, this.player2.getId());

            switch (type) {
                // Pay
                case 1 -> {
                    if (this.payIfSuccessKamas > 0 && ((kamas + this.payIfSuccessKamas) > this.player2.getKamas()))
                        kamas -= this.payIfSuccessKamas;
                    this.payKamas = kamas;
                    this.send("Ep1;G" + this.payKamas);
                }
                // PayIfSuccess
                case 2 -> {
                    if (this.payKamas > 0 && ((kamas + this.payKamas) > this.player2.getKamas()))
                        kamas -= this.payKamas;
                    this.payIfSuccessKamas = kamas;
                    this.send("Ep2;G" + this.payIfSuccessKamas);
                }
            }
        }

        public void setPayItems(byte type, boolean adding, int guid, int quantity) {
            ObjetoJuego object = Mundo.getGameObject(guid);

            if (object == null)
                return;
            if (object.getPosicion() != Constantes.ITEM_POS_NO_EQUIPED || object.isAttach())
                return;

            this.ok1 = false;
            this.ok2 = false;
            GestorSalida.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(), ok1, this.player1.getId());
            GestorSalida.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(), ok1, this.player1.getId());
            GestorSalida.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(), ok2, this.player2.getId());
            GestorSalida.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(), ok2, this.player2.getId());

            if (adding) {
                this.addItem(object, quantity, type);
            } else {
                this.removeItem(object, quantity, type);
            }
        }

        private void addItem(ObjetoJuego object, int quantity, byte type) {
            if (object.getCantidad() < quantity)
                quantity = object.getCantidad();

            ArrayList<Doble<Integer, Integer>> items = (type == 1 ? this.payItems : this.payItemsIfSuccess);
            Doble<Integer, Integer> couple = getCoupleInList(items, object.getId());
            String add = "|" + object.getModelo().getId() + "|" + object.parseStatsString();

            if (couple != null) {
                couple.segundo += quantity;
                this.player2.send("Ep" + type + ";O+" + object.getId() + "|" + couple.getSegundo());
                this.player1.send("Ep" + type + ";O+" + object.getId() + "|" + couple.getSegundo() + add);
                return;
            }

            items.add(new Doble<>(object.getId(), quantity));
            this.player2.send("Ep" + type + ";O+" + object.getId() + "|" + quantity);
            this.player1.send("Ep" + type + ";O+" + object.getId() + "|" + quantity + add);
        }

        private void removeItem(ObjetoJuego object, int quantity, byte type) {
            ArrayList<Doble<Integer, Integer>> items = (type == 1 ? this.payItems : this.payItemsIfSuccess);
            Doble<Integer, Integer> couple = getCoupleInList(items, object.getId());

            if(couple == null) return;
            int newQua = couple.getSegundo() - quantity;

            if (newQua < 1) {
                items.remove(couple);
                this.player1.send("Ep" + type + ";O-" + object.getId());
                this.player2.send("Ep" + type + ";O-" + object.getId());
            } else {
                couple.segundo = newQua;
                this.player2.send("Ep" + type + ";O+" + object.getId() + "|" + newQua);
                this.player1.send("Ep" + type + ";O+" + object.getId() + "|" + newQua + "|" + object.getModelo().getId() + "|" + object.parseStatsString());
            }
        }

        private void send(String packet) {
            this.player1.send(packet);
            this.player2.send(packet);
        }
    }
}