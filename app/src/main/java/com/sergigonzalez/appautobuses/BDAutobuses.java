package com.sergigonzalez.appautobuses;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Projecte APPAutobuses
 * @author Abel Serrano, Sergi Gonazalez, Roger G.
 * Created by ALUMNEDAM on 20/12/2016.
 */

class BDAutobuses extends SQLiteOpenHelper {
    /**
     * Creamos unas array de String insertando las sentencias SQL
     */
    private String[] sentenciasTablas = {"CREATE TABLE usuarios (matricula VARCHAR2(7),password VARCHAR2(40))", "CREATE TABLE posiciones (matricula VARCHAR2(7)," +
            "posX NUMBER, posY NUMBER, fecha String, enBBDDexterna boolean)"};
    private String[] sentenciasInsert = {"INSERT INTO usuarios VALUES('4617DNO', 'supervaca')", "INSERT INTO usuarios VALUES('8357YNP', 'superconejo')"};

    BDAutobuses(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * En el onCreate decimos que si la base de datos tiene contenido, recorremos los array ejecutando las sentencias.
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        if (db != null) {
            //Creamos tablas
            for (String sentenciasTabla : sentenciasTablas) {
                db.execSQL(sentenciasTabla);
            }
            //Insertamos usuarios
            for (String aSentenciasInsert : sentenciasInsert) {
                db.execSQL(aSentenciasInsert);
            }
        }
    }

    /**
     * En el caso de que actualicemos la base de datos y cambiemos la versión, eliminaremos la base de datos y la crearemos de nuevo
     * @param db
     * @param i
     * @param i1
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        if (db != null) {
            String borrarTaulaUsuaris = "DROP TABLE usuarios";
            String borrarTaulaPosicions = "DROP TABLE posiciones";
            db.execSQL(borrarTaulaPosicions);
            db.execSQL(borrarTaulaUsuaris);

            for (String sentenciasTabla : sentenciasTablas) {
                db.execSQL(sentenciasTabla);
            }
            //Insertamos usuarios
            for (String aSentenciasInsert : sentenciasInsert) {
                db.execSQL(aSentenciasInsert);
            }
        }
    }
}
