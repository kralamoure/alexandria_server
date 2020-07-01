package org.alexandria.estaticos.comandos.administracion;

import org.alexandria.comunes.gestorsql.Database;

import java.util.ArrayList;
import java.util.List;

public class GrupoADM {

    private final static List<GrupoADM> grupo = new ArrayList<>();

    private final int id;
    private final String nombre;
    private final boolean isJugador;
    private List<Comandos> comandos = new ArrayList<>();

    public GrupoADM(int id, String nombre, boolean isJugador, String comandos) {
        this.id = id;
        this.nombre = nombre;
        this.isJugador = isJugador;

        if (comandos.equalsIgnoreCase("all")) {
            this.comandos = Comandos.comandos;
        } else {
            if (comandos.contains(",")) {
                for (String str : comandos.split(","))
                    this.comandos.add(Comandos.getComandobyID(Integer.parseInt(str)));
            } else {
                this.comandos.add(Comandos.getComandobyID(Integer.parseInt(comandos)));
            }
        }

        GrupoADM.grupo.add(this);
    }

    public int getId() {
        return this.id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public boolean isJugador() {
        return isJugador;
    }

    public List<Comandos> getComandos() {
        return comandos;
    }

    public boolean haveCommand(String name) {
        for (Comandos command : this.comandos)
            if (command.getArgumento()[0].equalsIgnoreCase(name))
                return true;
        return false;
    }

    public static void reload() {
        GrupoADM.grupo.clear();
        Database.dinamicos.getGroupData().load(null);
    }

    public static GrupoADM getGrupoID(int id) {
        for(GrupoADM group : GrupoADM.grupo)
            if(group.id == id)
                return group;
        return null;
    }

    public static List<GrupoADM> getGrupo() {
        return grupo;
    }
}
