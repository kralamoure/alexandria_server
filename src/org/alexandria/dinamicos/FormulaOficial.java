package org.alexandria.dinamicos;

import org.alexandria.estaticos.cliente.Jugador;
import org.alexandria.configuracion.Configuracion;
import org.alexandria.configuracion.Constantes;
import org.alexandria.estaticos.Recaudador;
import org.alexandria.estaticos.juego.mundo.Mundo;
import org.alexandria.estaticos.pelea.Peleador;

import java.util.ArrayList;

public class FormulaOficial {

    public static long getXp(Object object, ArrayList<Peleador> winners,
                             long groupXp, byte nbonus, int star, int challenge, int lvlMax,
                             int lvlMin, int lvlLoosers, int lvlWinners) {
        if (lvlMin <= 0 || object == null)
            return 0;
        if (object instanceof Peleador) {
            Peleador fighter = (Peleador) object;
            Jugador player = fighter.getPlayer();

            if (winners.contains(fighter)) {
                if (lvlWinners <= 0)
                    return 0;

                double sagesse = fighter.getLvl() * 0.5 + fighter.getPlayer().getTotalStats()
                        .getEffect(Constantes.STATS_ADD_SAGE), nvGrpMonster = ((double) lvlMax / (double) lvlMin),
                        bonus = 1.0, rapport = ((double) lvlLoosers / (double) lvlWinners);

                if (winners.size() == 1)
                    rapport = 0.6;
                else if (rapport == 0)
                    return 0;
                else if (rapport <= 1.1 && rapport >= 0.9)
                    rapport = 1;
                else {
                    if (rapport > 1)
                        rapport = 1 / rapport;
                    if (rapport < 0.01)
                        rapport = 0.01;
                }

                int sizeGroupe = 0;
                for (Peleador f : winners) {
                    if (f.getPlayer() != null && !f.isInvocation()
                            && !f.isMob() && !f.isCollector() && !f.isDouble())
                        sizeGroupe++;
                }
                if (sizeGroupe < 1)
                    return 0;
                if (sizeGroupe > 8)
                    sizeGroupe = 8;

                if (nbonus > 8)
                    nbonus = 8;
                switch (nbonus) {
                    case 0, 1 -> bonus = 0.5;
                    case 2 -> bonus = 2.1;
                    case 3 -> bonus = 3.2;
                    case 4 -> bonus = 4.3;
                    case 5 -> bonus = 5.4;
                    case 6 -> bonus = 6.5;
                    case 7 -> bonus = 7.8;
                    case 8 -> bonus = 9;
                }
                if (nvGrpMonster == 0)
                    return 0;
                else if (nvGrpMonster < 3.0)
                    nvGrpMonster = 1;
                else
                    nvGrpMonster = 1 / nvGrpMonster;

                if (nvGrpMonster < 0)
                    nvGrpMonster = 0;
                else if (nvGrpMonster > 1)
                    nvGrpMonster = 1;

                return (long) (((1 + (sagesse / 100)) * (1 + (challenge / 100)) * (1 + (star / 100))
                        * (bonus + rapport) * (nvGrpMonster) * (groupXp / sizeGroupe))
                        * Configuracion.INSTANCE.getRATE_XP() * Mundo.mundo.getConquestBonus(fighter.getPlayer()));
            }
        } else if (object instanceof Recaudador) {
            Recaudador collector = (Recaudador) object;

            if (Mundo.mundo.getGuild(collector.getGuildId()) == null)
                return 0;

            if (lvlWinners <= 0)
                return 0;

            double sagesse = Mundo.mundo.getGuild(collector.getGuildId()).getLvl()
                    * 0.5
                    + Mundo.mundo.getGuild(collector.getGuildId()).getStats(Constantes.STATS_ADD_SAGE), nvGrpMonster = ((double) lvlMax / (double) lvlMin), bonus = 1.0, rapport = ((double) lvlLoosers / (double) lvlWinners);

            if (winners.size() == 1)
                rapport = 0.6;
            else if (rapport == 0)
                return 0;
            else if (rapport <= 1.1 && rapport >= 0.9)
                rapport = 1;
            else {
                if (rapport > 1)
                    rapport = 1 / rapport;
                if (rapport < 0.01)
                    rapport = 0.01;
            }

            int sizeGroupe = 0;
            for (Peleador f : winners) {
                if (f.getPlayer() != null && !f.isInvocation()
                        && !f.isMob() && !f.isCollector() && !f.isDouble())
                    sizeGroupe++;
            }
            if (sizeGroupe < 1)
                return 0;
            if (sizeGroupe > 8)
                sizeGroupe = 8;

            if (nbonus > 8)
                nbonus = 8;
            switch (nbonus) {
                case 0, 1 -> bonus = 0.5;
                case 2 -> bonus = 2.1;
                case 3 -> bonus = 3.2;
                case 4 -> bonus = 4.3;
                case 5 -> bonus = 5.4;
                case 6 -> bonus = 6.5;
                case 7 -> bonus = 7.8;
                case 8 -> bonus = 9;
            }
            if (nvGrpMonster == 0)
                return 0;
            else if (nvGrpMonster < 3.0)
                nvGrpMonster = 1;
            else
                nvGrpMonster = 1 / nvGrpMonster;

            if (nvGrpMonster < 0)
                nvGrpMonster = 0;
            else if (nvGrpMonster > 1)
                nvGrpMonster = 1;

            return (long) (((1 + ((sagesse + star + challenge) / 100))
                    * (bonus + rapport) * (nvGrpMonster) * (groupXp / sizeGroupe)) * Configuracion.INSTANCE.getRATE_XP());
        }
        return 0;
    }
}