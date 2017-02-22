package com.sergigonzalez.appautobuses;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Projecte APPAutobuses
 * @author Abel Serrano, Sergi Gonazalez, Roger G.
 * Created by ALUMNEDAM on 20/12/2016.
 */

public class SPosiciones extends AppCompatActivity implements View.OnClickListener
{
    /**
     * Iniciamos el activity recogiendo los datos que nos haya llegado del Main, inicializamos
     * los datos del Main y del layout, en el caso de que venga del Main el activity savedInstanceState será null por lo que
     * entrará en el if y recogerá los datos, además, pasará a Servicio.class los datos de la matricula.
     * sa
     *
     */
    String matricula;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sposiciones);
        Button btnFin = (Button) findViewById(R.id.btnFin);
        btnFin.setOnClickListener(this);
        Intent servi = new Intent(SPosiciones.this, Servicio.class);
        if (savedInstanceState == null) {
            Intent extras = getIntent();
            matricula=extras.getStringExtra("matricula");
            servi.putExtra("matricula",matricula);
            startService(servi);
        }

    }

    /**
     * Cuando le demos al botón
     * @param view
     */
    @Override
    public void onClick(View view)
    {
        if (R.id.btnFin == view.getId())
        {
            stopService(new Intent(SPosiciones.this, Servicio.class));
            finish();
        }
    }
}
