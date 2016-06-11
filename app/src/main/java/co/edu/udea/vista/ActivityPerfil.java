package co.edu.udea.vista;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import co.edu.udea.modelo.informacionBasica.Respuesta;

public class ActivityPerfil extends Activity {

    private TextView TvLaboratorio;
    private TextView TvUbicacion;
    private TextView TvTelefono;
    private TextView TvNombre;
    private TextView TvEmail;
    private String token;
    private String idUsuario;
    private String nombreLaboratorio;
    private String telefono;
    private String ubicacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mi_perfil);
        iniciarComponentes();
        InformacionAdministrador informacionAdministrador = new InformacionAdministrador();
        informacionAdministrador.execute();
    }

    public void iniciarComponentes()
    {
        TvLaboratorio = (TextView)findViewById(R.id.texto_laboratorio);
        TvTelefono = (TextView)findViewById(R.id.texto_telefono);
        TvUbicacion = (TextView)findViewById(R.id.texto_ubicacion);
        TvNombre = (TextView) findViewById(R.id.texto_nombre);
        TvEmail = (TextView) findViewById(R.id.texto_email);
        Bundle bundle = getIntent().getExtras();
        token = bundle.getString("token");
        idUsuario = bundle.getString("idUsuario");
        nombreLaboratorio = bundle.getString("nombreLaboratorio");
        telefono = bundle.getString("telefono");
        ubicacion = bundle.getString("ubicacion");
    }


    private class InformacionAdministrador extends AsyncTask<String, String, Void> {

        private String  mensajaError = "";
        Respuesta respuesta;

        @Override
        protected Void doInBackground(String... params) {

            String urlServicio = "http://udea.dnetix.co/api/user/" + idUsuario + "?token=" + token;
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            try
            {
                url = new URL(urlServicio);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                String respuestaServicio = leerInformacionServicio(inputStream);
                JSONObject respJSON = new JSONObject(respuestaServicio);
                boolean statusOK = respJSON.getBoolean("status");
                if(!statusOK)
                {
                    mensajaError = "No se puede cargar la informacion del administador";
                    return null;
                }
                Gson gson = new Gson();
                respuesta = gson.fromJson(respuestaServicio, Respuesta.class);
            } catch (MalformedURLException e)
            {
                mensajaError = "URL del servicio invalida";
            } catch (SocketTimeoutException e)
            {
                mensajaError = "Time out en recuparacion de datos o conexion";
            } catch (IOException e)
            {
                mensajaError = "No se pudo leer correctamente la respuesta del servicio";
            }catch(JsonIOException e)
            {
                mensajaError = "Error al parsear JSON";
            }catch(JSONException e)
            {
                mensajaError = "Error leyendo JSON";
            }
            finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void resultado) {
            super.onPostExecute(resultado);
            if(mensajaError.equals(""))
            {
                TvNombre.setText(respuesta.getData().getNombres());
                TvEmail.setText(respuesta.getData().getEmail());
                TvLaboratorio.setText(nombreLaboratorio);
                TvUbicacion.setText(ubicacion);
                TvTelefono.setText(telefono);
            }
            else
            {
                Toast.makeText(ActivityPerfil.this, mensajaError, Toast.LENGTH_LONG).show();
                return;
            }
        }

        public String leerInformacionServicio(InputStream stream) throws IOException
        {
            BufferedReader r = new BufferedReader(new InputStreamReader(stream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null)
            {
                sb.append(line);
            }
            return sb.toString();
        }
    }

}
