package org.alexandria.estaticos.pelea.inteligencia;

import org.alexandria.estaticos.Monstruos.MobGrade;
import org.alexandria.estaticos.pelea.Pelea;
import org.alexandria.estaticos.pelea.Peleador;
import org.alexandria.estaticos.pelea.inteligencia.tipo.*;

public class InteligenciaHandler {

    public static void select(final Pelea fight, final Peleador fighter) {
        {
            Inteligencia ia = new Blanco(fight, fighter);
            MobGrade mobGrade = fighter.getMob();

            if (mobGrade == null) {
                if (fighter.isDouble())
                    ia = new IA5(fight, fighter, (byte) 5);
                else if (fighter.isCollector())
                    ia = new IA30(fight, fighter, (byte) 5);

                final Inteligencia finalIA = ia;
                ia.addNext(finalIA::endTurn, 2000);
            } else if (mobGrade.getTemplate() == null) {
                ia.setStop(true);
                ia.endTurn();
            } else {
                //region select ia
                ia = switch (mobGrade.getTemplate().getIa()) {
                    //IA BASIQUE attaque,pm,attaque,pm
                    case 1, 27 -> new IA27(fight, fighter, (byte) 4);
                    //IA Dragonnet rouge
                    case 2 -> new IA2(fight, fighter, (byte) 6);
                    //IA Bloqueuse : Avancer vers ennemis
                    case 5 -> new IA5(fight, fighter, (byte) 5);
                    //IA type invocations (Coffre anim�)
                    case 6 -> new IA6(fight, fighter, (byte) 5);
                    //IA Surpuissante : Invocation, Buff, Fuite
                    case 8 -> new IA8(fight, fighter, (byte) 4);
                    //IA La Fourbe : Attaque[], Fuite
                    case 9 -> new IA9(fight, fighter, (byte) 4);
                    //IA Tonneau : Attaque[], Soin si Etat port�e
                    case 10 -> new IA10(fight, fighter, (byte) 8);
                    //IA Tofus
                    case 12 -> new IA12(fight, fighter, (byte) 4);
                    //IA Tonneau : Attaque[], Soin si Etat port�e
                    case 14 -> new IA14(fight, fighter, (byte) 8);
                    //IA BASIQUE buff sois meme,attaque,pm,attaque,pm
                    case 15, 30 -> new IA30(fight, fighter, (byte) 4);
                    // IA Tanu : Tape, va vers l'ennemis, invocation
                    case 16 -> new IA16(fight, fighter, (byte) 8);
                    //IA KIMBO
                    case 17 -> new IA17(fight, fighter, (byte) 4);
                    //Disciple Kimbo
                    case 18 -> new IA18(fight, fighter, (byte) 4);
                    // IA Des Tynril
                    case 19 -> new IA19(fight, fighter, (byte) 4);
                    // IA Kaskargo
                    case 20 -> new IA20(fight, fighter, (byte) 4);
                    // IA Krala
                    case 21 -> new IA21(fight, fighter, (byte) 4);
                    // IA Rasboul
                    case 22 -> new IA22(fight, fighter, (byte) 4);
                    // IA Rasboul mineur
                    case 23 -> new IA23(fight, fighter, (byte) 3);
                    // IA Sac anim�e
                    case 24 -> new IA24(fight, fighter, (byte) 3);
                    // IA Sacrifier
                    case 25 -> new IA25(fight, fighter, (byte) 4);
                    //IA Kitsou
                    case 26 -> new IA26(fight, fighter, (byte) 4);
                    //IA sphincter cell
                    case 28 -> new IA28(fight, fighter, (byte) 5);
                    //IA Tortu
                    case 29 -> new IA29(fight, fighter, (byte) 4);
                    // rats degoutant
                    case 31 -> new IA31(fight, fighter, (byte) 3);
                    //IA ARCHER attaque,pm loin d'enemie,attaque,pmvers enemie
                    case 32 -> new IA32(fight, fighter, (byte) 4);
                    //IA BASIQUE buff allier,attaque,pm,attaque,pm
                    case 33 -> new IA33(fight, fighter, (byte) 4);
                    //IA GLOUTO attaque tout le monde ,pm,attaque attaque tout le monde,pm
                    case 34 -> new IA34(fight, fighter, (byte) 4);
                    //IA BASIQUE ABraknyde heal sois meme,attaque,pm,attaque,pm
                    case 35 -> new IA35(fight, fighter, (byte) 4);
                    //IA BASIQUE attaque,Bond,pm,attaque,pm
                    case 36 -> new IA36(fight, fighter, (byte) 4);
                    //IA BASIQUE Branche soignante heal amis,attaque,pm,attaque,pm
                    case 37 -> new IA37(fight, fighter, (byte) 4);
                    //IA BASIQUE buffallier si pas denemie a porter,attaque,pm,attaque,pm
                    case 38 -> new IA38(fight, fighter, (byte) 4);
                    //IA Corbac aprivoiser attaque,pm en ligne de vue droite,attaque,pm fuite
                    case 39 -> new IA39(fight, fighter, (byte) 8);
                    //IA Buveur et momie koalak buff,attaque,pm,attaque,pm
                    case 40 -> new IA40(fight, fighter, (byte) 4);
                    //IA Wobot
                    case 41 -> new IA41(fight, fighter, (byte) 4);
                    //IA Gonflable
                    case 42 -> new IA42(fight, fighter, (byte) 6);
                    //IA Bloqueuse
                    case 43 -> new IA43(fight, fighter, (byte) 4);
                    //IA Chaton ecaflip
                    case 44 -> new IA44(fight, fighter, (byte) 4);
                    //IA
                    case 45 -> new IA45(fight, fighter, (byte) 6);
                    //IA lapino
                    case 46 -> new IA46(fight, fighter, (byte) 6);
                    //IA coffre animer
                    case 47 -> new IA47(fight, fighter, (byte) 4);
                    //IA Sanglier
                    case 48 -> new IA48(fight, fighter, (byte) 4);
                    //IA Chaferfu lancier
                    case 49 -> new IA49(fight, fighter, (byte) 6);
                    //IA Gourlo le terrible
                    case 50 -> new IA50(fight, fighter, (byte) 6);
                    //IA Workette
                    case 51 -> new IA51(fight, fighter, (byte) 6);
                    //IA avance Heal et buff allier plus fuite
                    case 52 -> new IA52(fight, fighter, (byte) 6);
                    //IA Peki Peki invisible apres 3 attaque et fuite
                    case 53 -> new IA53(fight, fighter, (byte) 8);
                    //IA Bworkmage
                    case 54 -> new IA54(fight, fighter, (byte) 8);
                    //IA dopeul feca
                    case 55 -> new IA55(fight, fighter, (byte) 8);
                    //IA Chene mou
                    case 56 -> new IA56(fight, fighter, (byte) 8);
                    //IA dopeul Osamodas
                    case 57 -> new IA57(fight, fighter, (byte) 8);
                    //IA rn
                    case 58 -> new IA58(fight, fighter, (byte) 8);
                    //IA dopeul enutrof
                    case 59 -> new IA59(fight, fighter, (byte) 8);
                    //IA dopeul sram
                    case 60 -> new IA60(fight, fighter, (byte) 8);
                    //IA dopeul xelor
                    case 61 -> new IA61(fight, fighter, (byte) 8);
                    //IA dopeul ecflip
                    case 62 -> new IA62(fight, fighter, (byte) 8);
                    //IA dopeul eniripsa
                    case 63 -> new IA63(fight, fighter, (byte) 8);
                    //IA dopeul iop
                    case 64 -> new IA64(fight, fighter, (byte) 8);
                    //IA dopeul cra
                    case 65 -> new IA65(fight, fighter, (byte) 8);
                    //IA dopeul sadida
                    case 66 -> new IA66(fight, fighter, (byte) 8);
                    //IA dopeul Sacrieur
                    case 67 -> new IA67(fight, fighter, (byte) 8);
                    //IA dopeul pandawa
                    case 68 -> new IA68(fight, fighter, (byte) 8);
                    //IA Trooll
                    case 69 -> new IA69(fight, fighter, (byte) 8);
                    //IA Maitre corbac
                    case 70 -> new IA70(fight, fighter, (byte) 6);
                    //IA Ougah
                    case 71 -> new IA71(fight, fighter, (byte) 4);
                    default -> ia;
                };
                //endregion
            }

            final Inteligencia finalIA = ia;
            ia.addNext(() -> {
                finalIA.apply();
                finalIA.addNext(finalIA::endTurn, 1000);
            },0);
        }
    }
}