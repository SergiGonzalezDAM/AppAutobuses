package com.sergigonzalez.appautobuses;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextMatricula;
    private EditText editTextPassword;
    private Button buttonMainSend;
    private SQLiteDatabase db;
    protected final static int VERSIO_BBDD=2;
    final private int PERMISSION_LOCATION_REQUEST_CODE = 666;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BDAutobuses usdbh = new BDAutobuses(this, "DBHorarioDAM", null, VERSIO_BBDD);
        db = usdbh.getWritableDatabase();
        editTextMatricula = (EditText) findViewById(R.id.editText2);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonMainSend = (Button) findViewById(R.id.btnMainSend);
        buttonMainSend.setOnClickListener(this);
        if (isMyServiceRunning(SPosiciones.class)) {
            Intent i = new Intent(this, SPosiciones.class);
            startActivity(i);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    public void cerrarDB() {
        db.close();
    }

    public boolean login() {
        String matricula, password;
        matricula = editTextMatricula.getText().toString();
        password = editTextPassword.getText().toString();
        String consulta = "SELECT matricula, password FROM usuarios WHERE matricula='" + matricula + "' AND password='" + password + "'";
        Cursor c = db.rawQuery(consulta, null);
        if (c.getCount() == 0) {
            Toast.makeText(this, "Login fallido", Toast.LENGTH_SHORT).show();
            c.close();
            return false;
        } else {
            c.close();
            return true;
        }

    }

    @Override
    public void onClick(View view) {
        if (login()) {
            if (!Servicio.checkPermission(this)) {
                showPermissionDialog();
            } else {
                launch();
            }
        }
    }

    public void launch() {
        Intent i = new Intent(this, SPosiciones.class);
        i.putExtra("matricula", editTextMatricula.getText().toString());
        startActivity(i);
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

    private void showPermissionDialog() {
        if (!Servicio.checkPermission(this)) {
            Log.i("Main-Permisions", "Permisos incorrectos, solicitandolos...");
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_LOCATION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launch();
                } else {
                    Toast.makeText(MainActivity.this, "Permisos denegados. No se puede continuar", Toast.LENGTH_SHORT)
                            .show();
                    Log.e("Main-Permisions", "Usuario denego los permisos.");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
