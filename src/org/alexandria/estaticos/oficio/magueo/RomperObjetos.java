package org.alexandria.estaticos.oficio.magueo;

import org.alexandria.otro.utilidad.Doble;

import java.util.ArrayList;

public class RomperObjetos {

    private ArrayList<Doble<Integer, Integer>> objetos = new ArrayList<>();
    private int contador = 0;
    private boolean detenerse = false;

    public void setContador(int contador) {
        this.contador = contador;
    }

    public int getContador() {
        return contador;
    }

    public void setDetenerse(boolean detenerse) {
        this.detenerse = detenerse;
    }

    public boolean isDetenerse() {
        return detenerse;
    }

    public void setObjetos(ArrayList<Doble<Integer, Integer>> objetos) {
        this.objetos = objetos;
    }

    public ArrayList<Doble<Integer, Integer>> getObjetos() {
        return objetos;
    }

    public synchronized int addObjeto(int id, int cantidad) {
        Doble<Integer, Integer> couple = this.buscar(id);

        if (couple == null) {
            this.objetos.add(new Doble<>(id, cantidad));
            return cantidad;
        } else {
            couple.segundo += cantidad;
            return couple.getSegundo();
        }
    }

    public synchronized int RemoverObjeto(int id, int cantidad) {
        Doble<Integer, Integer> couple = this.buscar(id);

        if (couple != null) {
            if (cantidad > couple.getSegundo()) {
                this.objetos.remove(couple);
                return cantidad;
            } else {
                couple.segundo -= cantidad;
                if (couple.getSegundo() <= 0) {
                    this.objetos.remove(couple);
                    return 0;
                }
                return couple.getSegundo();
            }
        }
        return 0;
    }

    private Doble<Integer, Integer> buscar(int id) {
        for (Doble<Integer, Integer> couple : this.objetos)
            if (couple.getPrimero() == id)
                return couple;
        return null;
    }
}