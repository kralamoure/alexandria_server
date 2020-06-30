package org.alexandria.estaticos.pelea.hechizo;

import org.alexandria.estaticos.pelea.Peleador;

import java.util.Map;

public class LanzarHechizo {

    private Peleador target = null;
    private final Hechizo.SortStats spellStats;
    private int cooldown = 0;

    public LanzarHechizo(Peleador t, Hechizo.SortStats SS, Peleador caster) {
        this.target = t;
        this.spellStats = SS;
        if (caster.getType() == 1
                && caster.getPlayer().getObjectsClassSpell().containsKey(SS.getSpellID())) {
            int modi = caster.getPlayer().getValueOfClassObject(SS.getSpellID(), 286);
            this.cooldown = SS.getCoolDown() - modi;
        } else {
            this.cooldown = SS.getCoolDown();
        }
    }

    public static boolean cooldownGood(Peleador fighter, int id) {
        for (LanzarHechizo S : fighter.getLaunchedSorts()) {
            if (S.getSpellId() == id && S.getCooldown() > 0)
                return false;
        }
        return true;
    }

    public static int getNbLaunch(Peleador fighter, int id) {
        int nb = 0;
        for (LanzarHechizo S : fighter.getLaunchedSorts())
            if (S.getSpellId() == id)
                nb++;
        return nb;
    }

    public static int getNbLaunchTarget(Peleador fighter, Peleador target, int id) {
        if (target == null)
            return 0;

        int nb = 0;
        for (LanzarHechizo S : fighter.getLaunchedSorts())
            if (S.target != null)
                if (S.getSpellId() == id && S.target.getId() == target.getId())
                    nb++;
        return nb;
    }

    public static int haveEffectTarget(Map<Integer, Peleador> f, Peleador target, int id) {
        if (target == null) return 0;
        int nb = 0;
        for(Peleador m : f.values())
            if(m != null)
                for (LanzarHechizo S : m.getLaunchedSorts())
                    if (S.target != null && S.target.getId() == target.getId())
                        for(EfectoHechizo e : S.spellStats.getEffects())
                            if(e.getEffectID() == id)
                                nb++;
        return nb;
    }

    public Peleador getTarget() {
        return this.target;
    }

    public int getSpellId() {
        return spellStats.getSpellID();
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void actuCooldown() {
        this.cooldown--;
    }

}