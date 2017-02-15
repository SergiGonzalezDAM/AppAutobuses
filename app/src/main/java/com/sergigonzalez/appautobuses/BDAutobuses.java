package com.sergigonzalez.appautobuses;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ALUMNEDAM on 11/01/2017.
 */

class BDAutobuses extends SQLiteOpenHelper {

    private String[] sentenciasTablas = {"CREATE TABLE usuarios (matricula VARCHAR2(7),password VARCHAR2(40))", "CREATE TABLE posiciones (matricula VARCHAR2(7)," +
            "posX NUMBER, posY NUMBER, fecha String)"};
    private String[] sentenciasInsert = {"INSERT INTO usuarios VALUES('4617DNO', 'supervaca')", "INSERT INTO usuarios VALUES('8357YNP', 'superconejo')"};

    BDAutobuses(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

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
