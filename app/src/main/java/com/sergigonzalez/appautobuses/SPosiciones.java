package com.sergigonzalez.appautobuses;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SPosiciones extends AppCompatActivity implements View.OnClickListener
{

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
        }
        servi.putExtra("matricula",matricula);
        startService(servi);
    }

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
