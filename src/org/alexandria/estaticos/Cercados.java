package org.alexandria.estaticos;

import org.alexandria.estaticos.area.mapa.Mapa;
import org.alexandria.comunes.Formulas;
import org.alexandria.estaticos.juego.mundo.Mundo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Cercados {

    private int owner;
    private int size;
    private Gremio guild;
    private Mapa map;
    private int cell = -1;
    private int price;
    private final int priceBase;
    private int placeOfSpawn;
    private int maxObject;
    private int door;
    private ArrayList<Integer> cellOfObject = new ArrayList<>();
    private final java.util.Map<Integer, Integer> cellAndObject = new HashMap<>();
    private final java.util.Map<Integer, java.util.Map<Integer, Integer>> objDurab = new HashMap<>();
    private final java.util.Map<Integer, java.util.Map<Integer, Integer>> breedingObject = new HashMap<>();
    private final CopyOnWriteArrayList<Integer> raising = new CopyOnWriteArrayList<>();
    private final ArrayList<Montura> etable = new ArrayList<>();

    public Cercados(Mapa map, int cellid, int size, int priceBase, int placeOfSpawn, int door, String cellOfObject, int maxObject) {
        this.map = map;
        this.cell = cellid;
        this.size = size;
        this.priceBase = priceBase;
        this.placeOfSpawn = placeOfSpawn;
        this.door = door;
        this.maxObject = maxObject;

        if(!cellOfObject.isEmpty()) {
            for(String cases : cellOfObject.split(";")) {
                int cellId = Integer.parseInt(cases);
                if(cellId > 0)
                    this.cellOfObject.add(cellId);
            }
        }
    }

    public void setData(int owner, int guild, int price, String raising, String objects, String objDurab, String etable) {
        this.owner = owner;
        this.guild = Mundo.mundo.getGuild(guild);
        this.price = price;
        if(!objects.isEmpty())
        {
            for(String object: objects.split("\\|"))
            {
                String[] infos = object.split(";");
                int cellId = Integer.parseInt(infos[0]);
                int objectId = Integer.parseInt(infos[1]);
                int proprietor = Integer.parseInt(infos[2]);
                java.util.Map<Integer, Integer> other = new HashMap<>();
                other.put(objectId, proprietor);
                this.cellAndObject.put(cellId, objectId);
                this.breedingObject.put(cellId, other);
            }
        }
        //chargement de la liste des dragodinde dans l'Ã©table
        for(String i: raising.split(";"))
        {
            try {
                Montura DD = Mundo.mundo.getMountById(Integer.parseInt(i));
                if(DD != null)
                    this.etable.add(DD);
            } catch (Exception ignored) {
            }
        }
        if(!objDurab.isEmpty())
        {
            for(String object: objDurab.split("\\|"))
            {
                String[] infos = object.split(";");
                int cellId = Integer.parseInt(infos[0]);
                int durability = Integer.parseInt(infos[1]);
                int durabilityMax = Integer.parseInt(infos[2]);
                java.util.Map<Integer, Integer> inDurab = new HashMap<>();
                inDurab.put(durability, durabilityMax);
                this.objDurab.put(cellId, inDurab);
            }
        }
        if(!etable.isEmpty())
            for(String dd: etable.split(";")) {
                try {
                    this.raising.add(Integer.parseInt(dd));
                    Montura mount = Mundo.mundo.getMountById(Integer.parseInt(dd));
                    mount.setMapId(this.map.getId());
                    mount.setCellId(mount.getCellId());
                } catch(Exception ignored) {
                }
            }
        if(this.map != null)
            this.map.setMountPark(this);
        for(String firstCut: etable.split(";"))//PosseseurID,DragoID;PosseseurID2,DragoID2;PosseseurID,DragoID3
        {
            try	{
                String[] secondCut = firstCut.split(",");
                Montura DD = Mundo.mundo.getMountById(Integer.parseInt(secondCut[1]));
                if(DD == null)
                    continue;
                this.raising.add(Integer.parseInt(secondCut[1]), Integer.parseInt(secondCut[0]));
            }catch(Exception ignored) {
            }
        }
    }

    public void setInfos(Mapa map, int cellid, int size, int placeOfSpawn, int door, String cellOfObject, int maxObject) {
        this.map = map;
        this.cell = cellid;
        this.size = size;
        this.placeOfSpawn = placeOfSpawn;
        this.door = door;
        this.maxObject = maxObject;

        if(!cellOfObject.isEmpty()) {
            for(String cases : cellOfObject.split(";")) {
                int cellId = Integer.parseInt(cases);
                if(cellId > 0)
                    this.cellOfObject.add(cellId);
            }
        }
    }

    public int getPriceBase() {
        return priceBase;
    }

    public void setDoor(int id) {
        this.door = id;
    }

    public int getMountcell() {
        return placeOfSpawn;
    }

    public void setMountCell(int id) {
        this.placeOfSpawn = id;
    }

    public void setCellObject(ArrayList<Integer> array) {
        this.cellOfObject = (ArrayList<Integer>) array.clone();
    }

    public void setInfos(int owner, Mapa map, int cell, int size, int guild, int price, int placeOfSpawn, String raising, int door, String cellOfObject, int maxObject, String objects, String objDurab, String etable)
    {

        this.owner = owner;
        this.size = size;
        this.guild = Mundo.mundo.getGuild(guild);
        this.map.setMountPark(null);
        this.map = map;
        if(this.map != null)
            this.map.setMountPark(this);
        this.cell = cell;
        this.price = price;
        this.placeOfSpawn = placeOfSpawn;
        this.door = door;
        this.maxObject = maxObject;

        this.cellAndObject.clear();
        this.breedingObject.clear();
        if(!objects.isEmpty())
        {
            for(String object: objects.split("\\|")) {
                String[] infos = object.split(";");
                int cellId = Integer.parseInt(infos[0]);
                int objectId = Integer.parseInt(infos[1]);
                int proprietor = Integer.parseInt(infos[2]);
                java.util.Map<Integer, Integer> other = new HashMap<>();
                other.put(objectId, proprietor);
                this.cellAndObject.put(cellId, objectId);
                this.breedingObject.put(cellId, other);
            }
        }
        //chargement de la liste des dragodinde dans l'Ã©table
        this.etable.clear();
        for(String i: etable.split(";")) {
            try {
                Montura DD = Mundo.mundo.getMountById(Integer.parseInt(i));
                if(DD != null)
                    this.etable.add(DD);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.objDurab.clear();
        if(!objDurab.isEmpty()) {
            for(String object: objDurab.split("\\|")) {
                String[] infos = object.split(";");
                int cellId = Integer.parseInt(infos[0]);
                int durability = Integer.parseInt(infos[1]);
                int durabilityMax = Integer.parseInt(infos[2]);
                java.util.Map<Integer, Integer> inDurab = new HashMap<>();
                inDurab.put(durability, durabilityMax);
                this.objDurab.put(cellId, inDurab);
            }
        }
        this.cellOfObject.clear();
        if(!cellOfObject.isEmpty()) {
            for(String cases : cellOfObject.split(";")) {
                int cellId = Integer.parseInt(cases);
                if(cellId <= 0)
                    continue;
                this.cellOfObject.add(cellId);
            }
        }
        this.raising.clear();
        if(!raising.isEmpty()) {
            String[] dragodinde = raising.split(";");
            for(String dd: dragodinde)
                this.raising.add(Integer.parseInt(dd));
        }
        this.raising.clear();
        for(String firstCut: raising.split(";"))//PosseseurID,DragoID;PosseseurID2,DragoID2;PosseseurID,DragoID3
            {
            try	{
                String[] secondCut = firstCut.split(",");
                Montura DD = Mundo.mundo.getMountById(Integer.parseInt(secondCut[1]));
                if(DD == null)
                    continue;
                this.raising.add(Integer.parseInt(secondCut[1]), Integer.parseInt(secondCut[0]));
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public int getOwner() {
        return this.owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public int getSize() {
        return this.size;
    }

    public Gremio getGuild() {
        return this.guild;
    }

    public void setGuild(Gremio guild) {
        this.guild = guild;
    }

    public Mapa getMap() {
        return this.map;
    }

    public int getCell() {
        return this.cell;
    }

    public int getPrice() {
        return this.price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPlaceOfSpawn() {
        return this.placeOfSpawn;
    }

    public int getMaxObject() {
        return this.maxObject;
    }

    public int getDoor() {
        return this.door;
    }

    public ArrayList<Integer> getCellOfObject() {
        return this.cellOfObject;
    }

    public boolean hasEtableFull(int id) {
        if(this.getOwner() == -1) {
            int i = 0;
            for(Montura mount : this.getEtable())
                if(mount.getOwner() == id)
                    i++;
            return i >= 100;
        } else {
            return this.getEtable().size() >= 100;
        }
    }

    public boolean hasEnclosFull(int id) {
        if(this.getOwner() == -1) {
            int i = 0;
            for(int mountId : this.getListOfRaising())
                if(mountId == id)
                    i++;
            return i >= this.getSize();
        } else {
            return this.getListOfRaising().size() >= this.getSize();
        }
    }

    public void addCellObject(int cell) {
        if(this.cellOfObject.contains(cell))
            return;
        if(cell <= 0)
            return;
        this.cellOfObject.add(cell);
    }

    public String parseStringCellObject() {
        StringBuilder cell = new StringBuilder();
        cell.append("");
        boolean first = true;
        for(Integer i: this.cellOfObject)
        {
            if(first)
                cell.append(i);
            else
                cell.append(";").append(i);
            first = false;
        }
        return cell.toString();
    }

    public java.util.Map<Integer, Integer> getCellAndObject() {
        return this.cellAndObject;
    }

    public void addObject(int cell, int object, int owner, int durability, int durabilityMax) {
        if(this.breedingObject.containsKey(cell)) {
            this.breedingObject.remove(cell);
            this.cellAndObject.remove(cell);
        }
        java.util.Map<Integer, Integer> other = new HashMap<>();
        other.put(object, owner);

        java.util.Map<Integer, Integer> inDurab = new HashMap<>();
        inDurab.put(durability, durabilityMax);

        this.cellAndObject.put(cell, object);
        this.breedingObject.put(cell, other);
        this.objDurab.put(cell, inDurab);
    }

    public boolean delObject(int cell) {
        if(!this.breedingObject.containsKey(cell) && !this.objDurab.containsKey(cell))
            return false;
        this.objDurab.remove(cell);
        this.breedingObject.remove(cell);
        this.cellAndObject.remove(cell);
        return true;
    }

    public java.util.Map<Integer, java.util.Map<Integer, Integer>> getObjDurab() {
        return this.objDurab;
    }

    public java.util.Map<Integer, java.util.Map<Integer, Integer>> getObject() {
        return this.breedingObject;
    }

    public void addRaising(int id) {
        this.raising.add(id);
    }

    public void delRaising(int id) {
        if(this.raising.contains(id))
            this.raising.remove(this.raising.indexOf(id));
    }

    public CopyOnWriteArrayList<Integer> getListOfRaising() {
        return this.raising;
    }

    public ArrayList<Montura> getEtable() {
        return this.etable;
    }

    public synchronized void startMoveMounts() {
        if(this.raising.size() > 0) {
            char[] directions = { 'b', 'd', 'f', 'h' };
            for(Integer id : this.raising) {
                Montura mount = Mundo.mundo.getMountById(id);
                if(mount != null) {
                    mount.moveMountsAuto(directions[Formulas.getRandomValue(0, 3)], 3, false);
                }
            }
        }
    }

    public String getStringObject() {
        StringBuilder str = new StringBuilder();
        boolean first = false;

        if(this.breedingObject.size() == 0)
            return str.toString();

        for(java.util.Map.Entry<Integer, java.util.Map<Integer, Integer>> entry : this.breedingObject.entrySet()) {
            if(first)
                str.append("|");
            str.append(entry.getKey());

            for(java.util.Map.Entry<Integer, Integer> entry2 : entry.getValue().entrySet())
                str.append(";").append(entry2.getKey()).append(";").append(entry2.getValue());
            first = true;
        }
        return str.toString();
    }

    public String getStringObjDurab() {
        StringBuilder str = new StringBuilder();
        boolean first = false;

        if(this.objDurab.size() == 0)
            return str.toString();

        for(java.util.Map.Entry<Integer, java.util.Map<Integer, Integer>> entry : this.objDurab.entrySet()) {
            if(first)
                str.append("|");
            str.append(entry.getKey());
            for(java.util.Map.Entry<Integer, Integer> entry2 : entry.getValue().entrySet())
            {
                str.append(";").append(entry2.getKey()).append(";").append(entry2.getValue());
            }
            first = true;
        }
        return str.toString();
    }

    public String parseRaisingToString() {
        StringBuilder str = new StringBuilder();
        boolean first = true;

        if(this.raising.size() == 0)
            return "";

        for(Integer id : this.raising) {
            if(!first) str.append(";");
            str.append(id);
            first = false;
        }
        return str.toString();
    }

    public String parseEtableToString() {
        StringBuilder str = new StringBuilder();
        for(Montura mount : this.etable) {
            if(!str.toString().equalsIgnoreCase(""))
                str.append(";");
            str.append(mount.getId());
        }
        return str.toString();
    }
}
