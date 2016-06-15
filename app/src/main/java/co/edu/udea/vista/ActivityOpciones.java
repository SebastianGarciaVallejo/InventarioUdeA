package co.edu.udea.vista;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import co.edu.udea.modelo.utiles.IntermediarioActividades;
import co.edu.udea.modelo.login.Respuesta;

public class ActivityOpciones extends Activity implements View.OnClickListener{

    private ImageButton realizarPrestamoBtn;
    private ImageButton registrarDevolucionBtn;
    private ImageButton registrarElementoBtn;
    private ImageButton miInformacionBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opciones);
        iniciarComponentes();
        agregarListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_opciones, menu);
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
        realizarPrestamoBtn = (ImageButton)findViewById(R.id.realizarPrestamo);
        registrarDevolucionBtn = (ImageButton)findViewById(R.id.registrarDevolucion);
        registrarElementoBtn = (ImageButton)findViewById(R.id.registrarElemento);
        miInformacionBtn = (ImageButton)findViewById(R.id.miInformacion);
    }

    public void agregarListeners()
    {
        realizarPrestamoBtn.setOnClickListener(this);
        registrarDevolucionBtn.setOnClickListener(this);
        registrarElementoBtn.setOnClickListener(this);
        miInformacionBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        Respuesta respuesta= (Respuesta) IntermediarioActividades.getObjetoATransmitirEntreActividades();

        if(view.getId() == realizarPrestamoBtn.getId())
        {
            Intent intent = new Intent(this, ActivityLeerTarjeta.class );
            startActivity(intent);
        }
        else if(view.getId() == registrarDevolucionBtn.getId())
        {

        }
        else if(view.getId() == registrarElementoBtn.getId())
        {
            Intent intent = new Intent(this, ActivityLista.class );
            intent.putExtra("token", respuesta.getData().getToken());
            startActivity(intent);
        }
        else if(view.getId() == miInformacionBtn.getId())
        {
            Intent intent = new Intent(this, ActivityPerfil.class );
            intent.putExtra("idUsuario", respuesta.getData().getId());
            intent.putExtra("token", respuesta.getData().getToken());
            intent.putExtra("nombreLaboratorio", respuesta.getData().getListaLaboratorios().get(0).getNombre());
            intent.putExtra("telefono", respuesta.getData().getListaLaboratorios().get(0).getNumeroTelefonico());
            intent.putExtra("ubicacion", respuesta.getData().getListaLaboratorios().get(0).getUbicacion());
            startActivity(intent);
        }
    }
}