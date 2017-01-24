package com.sergigonzalez.appautobuses;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String[] sentenciasInsert = {"INSERT INTO usuarios VALUES('4617DNO', 'supervaca')", "INSERT INTO usuarios VALUES('8357YNP', 'superconejo')"};
    private String consulta = "SELECT matricula, password FROM usuarios ";
    EditText editTextMatricula;
    EditText editTextPassword;
    Button buttonMainSend;
    SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BDAutobuses usdbh = new BDAutobuses(this, "DBHorarioDAM", null, 1);
        db = usdbh.getWritableDatabase();
        editTextMatricula = (EditText) findViewById(R.id.editText2);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonMainSend = (Button) findViewById(R.id.btnMainSend);
        buttonMainSend.setOnClickListener(this);
        anyadirDatos(db);
        if (isMyServiceRunning(SPosiciones.class)) {

        }
        //cerrarDB(db);
    }

    public void anyadirDatos(SQLiteDatabase db) {
        for (int i = 0; i < sentenciasInsert.length; i++) {
            db.execSQL(sentenciasInsert[i]);
        }
    }

    public void cerrarDB(SQLiteDatabase db) {
        db.close();
    }

    public boolean login(SQLiteDatabase db) {
        String matricula, password;
        matricula = editTextMatricula.getText().toString();
        password = editTextPassword.getText().toString();
        String consulta = "SELECT matricula, password FROM usuarios WHERE matricula='" + matricula + "' AND password='" + password + "'";
        Cursor c = db.rawQuery(consulta, null);
        if (c.getCount() == 0) {
            Toast.makeText(this, "Login fallido", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onClick(View view) {
        if (login(db)) {
            startService(new Intent(MainActivity.this, SPosiciones.class));
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
