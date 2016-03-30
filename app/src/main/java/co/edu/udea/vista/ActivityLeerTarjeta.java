package co.edu.udea.vista;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class ActivityLeerTarjeta extends Activity implements View.OnClickListener {

    private Button btnLeerTarjeta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarjeta);
        btnLeerTarjeta = (Button)findViewById(R.id.btnLeerTarjeta);
        btnLeerTarjeta.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_leer_tarjeta, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == btnLeerTarjeta.getId())
        {
            Intent leerEtiqueta = new Intent(this, ActivityLeerEtiqueta.class );
            startActivity(leerEtiqueta);
        }
    }
}
