package co.edu.udea.vista;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity implements View.OnClickListener{

    private EditText editTextUsuario;
    private EditText editTextContrasena;
    private Button   btnIngresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciarComponentes();
        agregarListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void iniciarComponentes()
    {
        editTextUsuario = (EditText)findViewById(R.id.etUsuario);
        editTextContrasena = (EditText)findViewById(R.id.etContrasena);
        btnIngresar = (Button)findViewById(R.id.btnIngresar);
    }

    public void agregarListeners()
    {
        btnIngresar.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == btnIngresar.getId())
        {
            Toast.makeText(MainActivity.this, "Se toco boton ingresar", Toast.LENGTH_LONG).show();
            String usuario = editTextUsuario.getText().toString();
            String contrasena = editTextContrasena.getText().toString();
            Intent leerTarjeta = new Intent(this, ActivityLeerTarjeta.class );
            startActivity(leerTarjeta);
            /*Intent i = new Intent(this, Actividad2.class);
            i.putExtra("direccion", et1.getText().toString());
            startActivity(i);
             Bundle bundle=getIntent().getExtras();*/
        }
    }
}
