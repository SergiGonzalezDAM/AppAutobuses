package com.sergigonzalez.appautobuses;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private String[] sentenciasInsert = {"INSERT INTO usuarios VALUES('4617DNO', supervaca)", "INSERT INTO usuarios VALUES('8357YNP', superconejo)"};
    private String consulta ="SELECT matricula, password FROM usuarios ";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BDAutobuses usdbh = new BDAutobuses(this, "DBHorarioDAM", null, 1);
        SQLiteDatabase db = usdbh.getWritableDatabase();
        anyadirDatos(db);



        cerrarDB(db);

    }

    public void anyadirDatos(SQLiteDatabase db) {
        for (int i = 0; i < sentenciasInsert.length; i++) {
            db.execSQL(sentenciasInsert[i]);
        }
    }

    public void cerrarDB(SQLiteDatabase db) {
        db.close();
    }
    public boolean consulta(SQLiteDatabase db){

    }
}
