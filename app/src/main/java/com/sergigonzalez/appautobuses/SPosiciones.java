package com.sergigonzalez.appautobuses;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SPosiciones extends AppCompatActivity implements View.OnClickListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sposiciones);
        Button btnFin = (Button) findViewById(R.id.btnFin);
        btnFin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        if (R.id.btnFin == view.getId())
        {
            Intent i = new Intent(this,MainActivity.class);
            stopService(new Intent(SPosiciones.this, Servicio.class));
            startActivity(i);
            System.exit(0);
        }
    }
}
