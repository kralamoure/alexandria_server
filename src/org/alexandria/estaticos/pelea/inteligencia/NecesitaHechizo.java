package org.alexandria.estaticos.pelea.inteligencia;

import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.hechizo.EfectoHechizo;
import org.alexandria.estaticos.pelea.hechizo.Hechizo.SortStats;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class NecesitaHechizo extends InteligenciaAbstracta {

    protected List<SortStats> buffs, glyphs, invocations, cacs, highests;

    public NecesitaHechizo(Pelea fight, Peleador fighter, byte count) {
        super(fight, fighter, count);

        this.buffs = NecesitaHechizo.getListSpellOf(fighter, "BUFF");
        this.glyphs = NecesitaHechizo.getListSpellOf(fighter, "GLYPH");
        this.invocations = NecesitaHechizo.getListSpellOf(fighter, "INVOCATION");
        this.cacs = NecesitaHechizo.getListSpellOf(fighter, "CAC");
        this.highests = NecesitaHechizo.getListSpellOf(fighter, "HIGHEST");
    }

    private static List<SortStats> getListSpellOf(Peleador fighter, String type) {
        final List<SortStats> spells = new ArrayList<>();

        for(SortStats spell : fighter.getMob().getSpells().values()) {
            if(spells.contains(spell)) continue;
            switch(type) {
                case "BUFF":
                    if(spell.getSpell().getType() == 1) spells.add(spell);
                    break;
                case "GLYPH":
                    if(spell.getSpell().getType() == 4) spells.add(spell);
                    break;
                case "INVOCATION":
                    spells.addAll(spell.getEffects().stream().filter(spellEffect -> spellEffect.getEffectID() == 181).map(spellEffect -> spell).collect(Collectors.toList()));
                    break;
                case "CAC":
                    if(spell.getSpell().getType() == 0) {
                        boolean effect = false;
                        for(EfectoHechizo spellEffect : spell.getEffects())
                            if (spellEffect.getEffectID() == 4 || spellEffect.getEffectID() == 6) {
                                effect = true;
                                break;
                            }
                        if(!effect && spell.getMaxPO() < 3) spells.add(spell);
                    }
                    break;
                case "HIGHEST":
                    if(spell.getSpell().getType() == 0) {
                        boolean effect = false;
                        for(EfectoHechizo spellEffect : spell.getEffects())
                            if (spellEffect.getEffectID() == 4 || spellEffect.getEffectID() == 6) {
                                effect = true;
                                break;
                            }
                        if(effect && spell.getSpellID() != 805) continue;
                        if(spell.getMaxPO() > 1) spells.add(spell);
                    }
                    break;
            }
        }
        return spells;
    }
}
