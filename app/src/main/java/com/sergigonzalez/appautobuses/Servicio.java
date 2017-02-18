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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Projecte APPAutobuses
 *
 * @author Abel Serrano, Sergi Gonazalez, Roger G.
 *         Created by ALUMNEDAM on 20/12/2016.
 */

public class Servicio extends Service
        implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener {
    private static final String LOGTAG = "android-localizacion";
    SQLiteDatabase db;
    private GoogleApiClient apiClient;
    private LocationRequest locRequest;
    private String matricula;

    private static final int TEMPS_ACTUALITZACIO_POSICIONES = 8000;
    private static final int MINIM_TEMPS_ACTUALITZACIO_POSICIONES = 5000;


    @Override

    public void onCreate() {
        //Construcción cliente API Google
        apiClient = new GoogleApiClient.Builder(this).addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        apiClient.connect();
        db = new BDAutobuses(this, "DBHorarioDAM", null, MainActivity.VERSIO_BBDD).getWritableDatabase();
        Log.d(LOGTAG, "Servicio inicidado");
    }

    @Override
    public int onStartCommand(Intent intenc, int flags, int idArranque) {
        Log.d(LOGTAG, "onStartCommand");
        if (intenc.hasExtra("matricula")) {
            matricula = intenc.getStringExtra("matricula");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        disableLocationUpdates();
        db.close();
        Log.i(LOGTAG, "Servicio detenido");
    }

    @Override
    public IBinder onBind(Intent intencion) {
        return null;
    }

    private void enableLocationUpdates() {

        if (checkPermission(this)) {
            locRequest = new LocationRequest();
            locRequest.setInterval(TEMPS_ACTUALITZACIO_POSICIONES);
            //No es capturaran posicions si no ha pasat el temps minim entre la recepcio de la ultima.
            locRequest.setFastestInterval(MINIM_TEMPS_ACTUALITZACIO_POSICIONES);
            locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Fallo de permisos", Toast.LENGTH_SHORT).show();
            Log.e(LOGTAG, "Fallo de permisos");
        }
    }

    /**
     * Comproba si l'usuari te els permisos adecuats per a l'execució de l'aplicacio.
     * @param context Context de l'aplicacio
     * @return boolean amb el resultat de la consulta de permisos.
     */
    public static boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Desactiva la actualitzacio d'ubicacions
     */
    private void disableLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                apiClient, this);
    }

    /**
     * Inicia la actualitzacio d'ubicacions
     */
    private void startLocationUpdates() {
        if (checkPermission(this)) {
            Toast.makeText(this, "Inicio de recepcion de ubicaciones", Toast.LENGTH_SHORT).show();
            Log.i(LOGTAG, "Inicio de recepción de ubicaciones");

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    apiClient, locRequest, this);
        } else {
            Toast.makeText(this, "Permisos insuficientes", Toast.LENGTH_SHORT).show();
            Log.e(LOGTAG, "Permisos insuficientes");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.e(LOGTAG, "Error grave al conectar con Google Play Services");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(LOGTAG, "Conectado correctamente a Google Play Services");
        enableLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(LOGTAG, "Se ha interrumpido la conexión con Google Play Services");
    }

    /**
     * En cambiar la ubicacio del dispositiu, inicia una AsyncTask que inserta la posicio a les BBDD
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.i(LOGTAG, "Recibida nueva ubicación!");
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String da = formatter.format(Calendar.getInstance().getTime());
        new TareaWSInsertarPosicion().execute(location.getLatitude(), location.getLongitude(), da);
    }

    class TareaWSInsertarPosicion extends AsyncTask<Object, Integer, Boolean> {


        /**
         * Les tasques que s'efectuen en Background. Primer, s'intenta inserir a la BBDD externa.
         * Si no s'aconsegueix inserir a la externa, al inserirla a l'interna s'especifica que no
         * s'ha inserit a l'externa.
         *
         * @param params Double posX, Double posY, String fecha
         * @return si s'ha aconseguit inserir en BBDD externa o no.
         */
        protected Boolean doInBackground(Object... params) {
            boolean insertadoEnDBexterna = insertarEnBDExterna(params);
            insertarenBDinterna(db, insertadoEnDBexterna, params);
            return insertadoEnDBexterna;
        }

        /**
         * Metode que s'executa al acabar la AsyncTask. Registra al log si la posicio ha estat
         * inserhida a la BBDD externa
         * @param result
         */
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Log.e(LOGTAG, "Fallo al insertar en BBDD extena");
            } else {
                Log.d(LOGTAG, "Posicion insertada en BBDD externa");
            }
        }

        /**
         * Inserta una posicio a la BBDD interna del telefon.
         * @param db
         * @param insertadoEnDBexterna Boolean que indica si la posicio s'ha pogut inserir a la externa
         * @param params               Double posX, Double posY, String fecha
         */
        private void insertarenBDinterna(SQLiteDatabase db, boolean insertadoEnDBexterna, Object... params) {
            String aux = "INSERT INTO posiciones VALUES(?,?,?,?,?)";
            SQLiteStatement sql = db.compileStatement(aux);
            sql.bindString(1, matricula);
            sql.bindDouble(2, (Double) params[0]);
            sql.bindDouble(3, (Double) params[1]);
            sql.bindString(4, (String) params[2]);
            sql.bindString(5, Boolean.toString(insertadoEnDBexterna));
            sql.execute();
        }

        /**
         * @param params Double posX, Double posY, String fecha
         * @return Boolean indicant si s'ha inserit a la BBDD externa o no.
         */
        private boolean insertarEnBDExterna(Object... params) {
            boolean insertadoEnDBexterna = true;
            OutputStreamWriter osw;
            try {
                URL url = new URL("http://192.168.1.9:8080/ServicioWeb/webresources/generic/insertarPosicion");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setReadTimeout(1000 /*milliseconds*/);
                conn.setConnectTimeout(500);
                conn.setRequestProperty("Content-Type", "application/json");
                osw = new OutputStreamWriter(conn.getOutputStream());
                osw.write(getStringJSON(params));
                osw.flush();
                osw.close();
                System.err.println(conn.getResponseMessage());
            } catch (java.io.IOException ex) {
                Log.e(LOGTAG, "Temps d'espera esgotat al iniciar la conexio amb la BBDD extena");
                insertadoEnDBexterna = false;
            } catch (org.json.JSONException ex) {
                Log.e(LOGTAG, "Error en la transformacio de l'objecte JSON: " + ex);
                insertadoEnDBexterna = false;
            }
            return insertadoEnDBexterna;
        }

        /**
         * Obte una String  (Objecte JSon) preparada per ser inserida a la nostra BBDD mitjançant un http put
         *
         * @param params Double posX, Double posY, String fecha
         * @return L'string preparat per ser insertat a la BBDD
         * @throws JSONException
         * @throws UnsupportedEncodingException
         */
        private String getStringJSON(Object... params) throws JSONException, UnsupportedEncodingException {
            JSONObject dato = new JSONObject();
            dato.put("matricula", matricula);
            dato.put("posX", params[0]);
            dato.put("posY", params[1]);
            dato.put("fecha", params[2]);
            Log.d(LOGTAG, "La posicion que se insertara es:" + dato.toString());
            return dato.toString();
        }

    }
}
