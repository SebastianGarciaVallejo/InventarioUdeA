package co.edu.udea.vista;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;

import co.edu.udea.modelo.login.Data;
import co.edu.udea.modelo.login.IntermediarioActividades;
import co.edu.udea.modelo.login.Laboratorio;
import co.edu.udea.modelo.login.Respuesta;

/**
 * Created by landrea.velez on 01/06/2016.
 */
public class ActivityPerfil extends Activity {

    private TextView laboratorio;
    private TextView ubicacion;
    private TextView telefono;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mi_perfil);
        iniciarComponentes();
        Respuesta respuesta= (Respuesta) IntermediarioActividades.getObjetoATransmitirEntreActividades();

        Data datos = respuesta.getData();
        ArrayList<Laboratorio> arrL = datos.getListaLaboratorios();
        String nombreLab = arrL.get(0).getNombre() ;
        String telefonoLab = arrL.get(0).getNumeroTelefonico();
        String ubicaLab = arrL.get(0).getUbicacion();

        laboratorio.setText(nombreLab);
        telefono.setText(telefonoLab);
        ubicacion.setText(ubicaLab);
    }

    public void iniciarComponentes()
    {
        laboratorio = (TextView)findViewById(R.id.texto_laboratorio);
        telefono = (TextView)findViewById(R.id.texto_telefono);
        ubicacion = (TextView)findViewById(R.id.texto_ubicacion);

    }
}
