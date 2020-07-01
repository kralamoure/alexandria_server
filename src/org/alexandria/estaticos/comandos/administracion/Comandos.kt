package org.alexandria.estaticos.comandos.administracion;

import org.alexandria.comunes.gestorsql.Database;

import java.util.ArrayList;
import java.util.List;

public class Comandos {

    public static List<Comandos> comandos = new ArrayList<>();

    private final int id;
    private final String[] argumento = new String[3];

    public Comandos(int id, String comando, String argumento, String descripcion) {
        this.id = id;
        this.argumento[0] = comando;
        this.argumento[1] = argumento == null ? "" : argumento;
        this.argumento[2] = descripcion == null ? "" : descripcion;

        Comandos.comandos.add(this);
    }

    public int getId() {
        return id;
    }

    public String[] getArgumento() {
        return argumento;
    }

    public static Comandos getComandobyID(int id) {
        for(Comandos command : Comandos.comandos)
            if(command.id == id)
                return command;
        return null;
    }

    public static void reload() {
        Comandos.comandos.clear();
        Database.dinamicos.getCommandData().load(null);
    }
}
