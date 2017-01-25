package com.sergigonzalez.appautobuses;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;


public class Servicio extends Service{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("FUnciona");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Fuera");
    }
}
