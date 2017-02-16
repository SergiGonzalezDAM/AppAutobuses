package com.sergigonzalez.appautobuses;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Created by ALUMNEDAM on 20/12/2016.
 */

public class Servicio extends Service
        implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener {
    private GoogleApiClient apiClient;
    private LocationRequest locRequest;
    private static final String LOGTAG = "android-localizacion";
    private String matricula;
    private TareaWSInsertarPosicion tareaWSInsertarPosicion;

    @Override

    public void onCreate() {
        //Construcción cliente API Google
        apiClient = new GoogleApiClient.Builder(this).addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        apiClient.connect();
        tareaWSInsertarPosicion = new TareaWSInsertarPosicion();
        Log.i(LOGTAG, "Servicio inicidado");
    }


    @Override
    public int onStartCommand(Intent intenc, int flags, int idArranque) {
        Log.i(LOGTAG, "onStartCommand");
        if (intenc.hasExtra("matricula")) {
            matricula = intenc.getStringExtra("matricula");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        disableLocationUpdates();
        Log.i(LOGTAG, "Servicio detenido");
    }

    @Override
    public IBinder onBind(Intent intencion) {
        return null;
    }

    private void enableLocationUpdates() {

        if (checkPermission(this)) {
            locRequest = new LocationRequest();
            locRequest.setInterval(2000);
            locRequest.setFastestInterval(1000);
            locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Fallo de permisos", Toast.LENGTH_SHORT).show();
            Log.i(LOGTAG, "Fallo de permisos");
        }
    }

    private void disableLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                apiClient, this);
    }

    private void startLocationUpdates() {
        if (checkPermission(this)) {
            Toast.makeText(this, "Inicio de recepcion de ubicaciones", Toast.LENGTH_SHORT).show();
            Log.i(LOGTAG, "Inicio de recepción de ubicaciones");

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    apiClient, locRequest, this);
        } else {
            Toast.makeText(this, "Permisos insuficientes", Toast.LENGTH_SHORT).show();
            System.out.println("Permisos insuficientes");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(LOGTAG, "Error grave al conectar con Google Play Services");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(LOGTAG, "Conectado correctamente a Google Play Services");
        enableLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(LOGTAG, "Se ha interrumpido la conexión con Google Play Services");
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.i(LOGTAG, "Recibida nueva ubicación!");
        BDAutobuses usdbh = new BDAutobuses(this, "DBHorarioDAM", null, MainActivity.VERSIO_BBDD);
        SQLiteDatabase db = usdbh.getWritableDatabase();
        String aux = "INSERT INTO posiciones VALUES(?,?,?,?)";
        SQLiteStatement sql = db.compileStatement(aux);
        sql.bindString(1, matricula);
        sql.bindDouble(2, location.getLatitude());
        sql.bindDouble(3, location.getLongitude());
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String da = formatter.format(Calendar.getInstance().getTime());
        sql.bindString(4, da);
        sql.execute();
        db.close();
        tareaWSInsertarPosicion.execute(matricula, location.getLatitude(), location.getLongitude(), da);
    }

    public static boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    class TareaWSInsertarPosicion extends AsyncTask<String, Integer, Boolean> {

        protected Boolean doInBackground(String... params) {

            boolean resul = true;

            HttpClient httpClient = new DefaultHttpClient();

            HttpPost post = new HttpPost("http://localhost:8080/ServicioWeb/webresources/generic/insertarPosicion");
            post.setHeader("content-type", "application/json");

            try {
                //Construimos el objeto posicion en formato JSON
                JSONObject dato = new JSONObject();
                dato.put("matricula", params[0]);
                dato.put("posX", Double.parseDouble(params[1]));
                dato.put("posY", Double.parseDouble(params[2]));
                dato.put("fecha", params[3]);
                StringEntity entity = new StringEntity(dato.toString());
                post.setEntity(entity);

                HttpResponse resp = httpClient.execute(post);
                String respStr = EntityUtils.toString(resp.getEntity());

                if (!respStr.equals("true"))
                    resul = false;
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                resul = false;
            }
            return resul;
        }

        protected void onPostExecute(Boolean result) {

            if (!result) {
                Log.i("Servicio", "No ha funcionado la insercion");
            } else {
                Log.i("Servicio", "Ha funcionado la insercion");
            }
        }


    }
}
