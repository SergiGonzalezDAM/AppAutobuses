package com.sergigonzalez.appautobuses;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ALUMNEDAM on 11/01/2017.
 */

public class BDAutobuses extends SQLiteOpenHelper {

    private String[] sentenciasTablas = {"CREATE TABLE usuarios (matricula VARCHAR2(7),password VARCHAR2(40))", "CREATE TABLE posiciones (matricula VARCHAR2(7)," +
            "posX INTEGER, posY INTEGER posZ INTEGER"};

    public BDAutobuses(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (db != null) {
            //Creamos tablas
            for (int i = 0; i < sentenciasTablas.length; i++) {
                db.execSQL(sentenciasTablas[i]);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}