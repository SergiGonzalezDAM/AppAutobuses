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

/**
 * Projecte APPAutobuses
 * @author Abel Serrano, Sergi Gonazalez, Roger G.
 * Created by ALUMNEDAM on 20/12/2016.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Declaramos las variables globales de los tipos creados en el layout(EditText,...), creamos
     * el objecto de SQLLiteDataBase para poder acceder a las base de datos interna, además creamos
     * una versión de la bd y los permisos de localización
     */
    private EditText editTextMatricula;
    private EditText editTextPassword;
    private Button buttonMainSend;
    private SQLiteDatabase db;
    protected final static int VERSIO_BBDD=3;
    final private int PERMISSION_LOCATION_REQUEST_CODE = 666;

    /**
     * Instanciamosla variables creadas anteriormente y a la hora de crear la base de datos, this(activity), DBHorarioDAM(nombre),
     * null(se deja por defecto), VERSIO_BBDD(la versión de la base de datos)Entonces, en el caso de que la
     * aplicación se haya cerrado de forma incorrecta y el servicio de segundo plano esté ejecutándose,
     * saltará directamente al segundo layout que veremos más tarde.
     * @param savedInstanceState
     */
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

    /**
     * El método se inicia en el caso de que por algún factor se cierre la aplicación, entonces cerraremos la conexión con la base de datos.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    /**
     *Obtenemos los datos del Activity y realizamos una consulta, en el caso de que la consulta
     * devuelva algo cerramos el cursor y devolvemos true, en caso contrario mensaje por pantalla con un Toast y false
     * @return true o false, depende de la situación
     */
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

    /**
     * Cuando le demos click al botón, primero verificará el login, en el caso de que sea correcto, verificará permisos, en el caso de que sea correcto
     * lanzará un aviso pidiendo permisos, en caso negativo lanzará el activity siguiente.
     * @param view
     */
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


    /**
     * Starts the second activity. Note that second activity attemps to create a new
     * Service if not running, because of that, we need to make sure before executing this method
     * that we have the necessary permisions needed by the service.
     */
    public void launch() {
        Intent i = new Intent(this, SPosiciones.class);
        i.putExtra("matricula", editTextMatricula.getText().toString());
        startActivity(i);
    }

    /**
     * Ths method checks if the Service passed as parameter is running or not.
     * @param serviceClass
     * @return
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * If we don't have the required permisions, starts a dialog asking for them.
     */
    private void showPermissionDialog() {
        if (!Servicio.checkPermission(this)) {
            Log.i("Main-Permisions", "Permisos incorrectos, solicitandolos...");
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION_REQUEST_CODE);
        }
    }

    /**
     * Checks the result of the Request for Permisions dialog if the permisions are ok, executes the launch() method.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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
