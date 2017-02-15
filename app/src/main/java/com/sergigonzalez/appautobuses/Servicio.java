package com.sergigonzalez.appautobuses;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ALUMNEDAM on 20/12/2016.
 */

public class Servicio extends Service
        implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener
{
    private GoogleApiClient apiClient;
    private LocationRequest locRequest;
    private static final String LOGTAG = "android-localizacion";
    private String matricula;
    @Override

    public void onCreate()
    {
        //Construcción cliente API Google
        apiClient = new GoogleApiClient.Builder(this).addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        apiClient.connect();
        Log.i(LOGTAG, "Servicio inicidado");
    }


    @Override
    public int onStartCommand(Intent intenc, int flags, int idArranque)
    {
        Log.i(LOGTAG, "onStartCommand");
        if(intenc.hasExtra("matricula")){
            matricula=intenc.getStringExtra("matricula");
        }
        return START_STICKY;
    }



    @Override
    public void onDestroy()
    {
        disableLocationUpdates();
        Log.i(LOGTAG, "Servicio detenido");
    }

    @Override
    public IBinder onBind(Intent intencion)
    {
        return null;
    }

    private void enableLocationUpdates() {

        //Hemos comprobado en el main que los permisos son correctos.

        locRequest = new LocationRequest();
        locRequest.setInterval(2000);
        locRequest.setFastestInterval(1000);
        locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        startLocationUpdates();

/*
        LocationSettingsRequest locSettingsRequest =
                new LocationSettingsRequest.Builder()
                        .addLocationRequest(locRequest)
                        .build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        apiClient, locSettingsRequest);

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(LOGTAG, "Configuración correcta");
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(LOGTAG, "Se requiere actuación del usuario");
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(LOGTAG, "No se puede cumplir la configuración de ubicación necesaria");
                        break;
                    default:
                        Log.e(LOGTAG, "Error en la comprobacion de configuracion de permisos.");
                }
            }
        });*/
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
        }
        else{Toast.makeText(this, "Permisos insuficientes", Toast.LENGTH_SHORT).show();
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
        BDAutobuses usdbh = new BDAutobuses(this, "DBHorarioDAM", null, 1);
        SQLiteDatabase db = usdbh.getWritableDatabase();
        String aux="INSERT INTO posiciones VALUES(?,?,?,?)";
        SQLiteStatement sql=db.compileStatement(aux);
        sql.bindString(1,matricula);
        sql.bindDouble(2,location.getLatitude());
        sql.bindDouble(3,location.getLongitude());
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String da=formatter.format(Calendar.getInstance().getTime());
        sql.bindString(4,da);
        sql.execute();
        db.close();
    }

    public static boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

}
