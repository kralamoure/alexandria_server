package org.alexandria.comunes.gestorsql.estaticos.datos

import com.zaxxer.hikari.HikariDataSource
import org.alexandria.comunes.gestorsql.estaticos.AbstractDAO
import org.alexandria.configuracion.MainServidor.stop
import org.alexandria.estaticos.Monstruos
import org.alexandria.estaticos.juego.mundo.Mundo
import java.sql.SQLException

class DatosMonstruos(dataSource: HikariDataSource?) :
    AbstractDAO<Monstruos>(dataSource!!) {
    override fun load(obj: Any?) {}
    override fun update(obj: Monstruos): Boolean {
        return false
    }

    fun load() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM monsters")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val id = rs.getInt("id")
                //if(id == 1044) continue;
                val gfxID = rs.getInt("gfxID")
                val align = rs.getInt("align")
                val colors = rs.getString("colors")
                val grades = rs.getString("grades")
                val spells = rs.getString("spells")
                val stats = rs.getString("stats")
                val statsInfos = rs.getString("statsInfos")
                val pdvs = rs.getString("pdvs")
                val pts = rs.getString("points")
                val inits = rs.getString("inits")
                val mK = rs.getInt("minKamas")
                val MK = rs.getInt("maxKamas")
                val IAType = rs.getInt("AI_Type")
                val xp = rs.getString("exps")
                val aggroDistance = rs.getInt("aggroDistance")
                val capturable = rs.getInt("capturable") == 1
                val monster = Monstruos(
                    id,
                    gfxID,
                    align,
                    colors,
                    grades,
                    spells,
                    stats,
                    statsInfos,
                    pdvs,
                    pts,
                    inits,
                    mK,
                    MK,
                    xp,
                    IAType,
                    capturable,
                    aggroDistance
                )
                Mundo.mundo.addMobTemplate(id, monster)
            }
        } catch (e: SQLException) {
            super.sendError("MonsterData load", e)
            stop("unknown")
        } finally {
            close(result)
        }
    }

    fun reload() {
        var result: Result? = null
        try {
            result = getData("SELECT * FROM monsters")
            val rs = result!!.resultSet
            while (rs!!.next()) {
                val id = rs.getInt("id")
                val gfxID = rs.getInt("gfxID")
                val align = rs.getInt("align")
                val colors = rs.getString("colors")
                val grades = rs.getString("grades")
                val spells = rs.getString("spells")
                val stats = rs.getString("stats")
                val statsInfos = rs.getString("statsInfos")
                val pdvs = rs.getString("pdvs")
                val pts = rs.getString("points")
                val inits = rs.getString("inits")
                val mK = rs.getInt("minKamas")
                val MK = rs.getInt("maxKamas")
                val IAType = rs.getInt("AI_Type")
                val xp = rs.getString("exps")
                val aggroDistance = rs.getInt("aggroDistance")
                val capturable = rs.getInt("capturable") == 1
                if (Mundo.mundo.getMonstre(id) == null) {
                    Mundo.mundo.addMobTemplate(
                        id,
                        Monstruos(
                            id,
                            gfxID,
                            align,
                            colors,
                            grades,
                            spells,
                            stats,
                            statsInfos,
                            pdvs,
                            pts,
                            inits,
                            mK,
                            MK,
                            xp,
                            IAType,
                            capturable,
                            aggroDistance
                        )
                    )
                } else {
                    Mundo.mundo.getMonstre(id).setInfos(
                        gfxID,
                        align,
                        colors,
                        grades,
                        spells,
                        stats,
                        statsInfos,
                        pdvs,
                        pts,
                        inits,
                        mK,
                        MK,
                        xp,
                        IAType,
                        capturable,
                        aggroDistance
                    )
                }
            }
        } catch (e: SQLException) {
            super.sendError("MonsterData reload", e)
        } finally {
            close(result)
        }
    }
}