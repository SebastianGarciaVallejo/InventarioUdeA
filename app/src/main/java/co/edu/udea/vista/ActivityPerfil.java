package co.edu.udea.vista;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import co.edu.udea.modelo.informacionBasica.Respuesta;


public class ActivityPerfil extends Activity implements View.OnClickListener {

    private TextView TvLaboratorio;
    private TextView TvUbicacion;
    private TextView TvTelefono;
    private TextView TvNombre;
    private TextView TvEmail;
    private TextView EdContrasenaActual;
    private TextView EdContrasenaNueva;
    private TextView EdContrasenaConfirmar;
    private Button btnAceptar;
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
        InformacionAdministrador informacionAdministrador = new InformacionAdministrador(this);
        informacionAdministrador.execute();
    }

    public void iniciarComponentes()
    {
        TvLaboratorio = (TextView)findViewById(R.id.texto_laboratorio);
        TvTelefono = (TextView)findViewById(R.id.texto_telefono);
        TvUbicacion = (TextView)findViewById(R.id.texto_ubicacion);
        TvNombre = (TextView) findViewById(R.id.texto_nombre);
        TvEmail = (TextView) findViewById(R.id.texto_email);
        EdContrasenaActual = (EditText)findViewById(R.id.etContrasenaActual);
        EdContrasenaNueva = (EditText)findViewById(R.id.etContrasenaNueva);
        EdContrasenaConfirmar = (EditText)findViewById(R.id.etContrasenaConfirmar);
        btnAceptar = (Button)findViewById(R.id.btnAceptar);
        btnAceptar.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        token = bundle.getString("token");
        idUsuario = bundle.getString("idUsuario");
        nombreLaboratorio = bundle.getString("nombreLaboratorio");
        telefono = bundle.getString("telefono");
        ubicacion = bundle.getString("ubicacion");
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == btnAceptar.getId())
        {
            String contrasenaNueva = EdContrasenaNueva.getText().toString();
            String contrasenaActual = EdContrasenaActual.getText().toString();
            String contrasenaConfimar = EdContrasenaConfirmar.getText().toString();

            if("".equals(contrasenaActual))
            {
                mostrarMensaje("Por favor ingrese su contraseña actual.", this);
                return;
            }
            if("".equals(contrasenaNueva))
            {
                mostrarMensaje("Por favor ingrese su nueva contraseña.", this);
                return;
            }
            if("".equals(contrasenaConfimar))
            {
                mostrarMensaje("Por favor confirme su nueva contraseña.", this);
                return;
            }
            if(contrasenaActual.equals(contrasenaNueva))
            {
                mostrarMensaje("La nueva contraseña no puede ser igual a la actual.", this);
                return;
            }
            if(!contrasenaNueva.equals(contrasenaConfimar))
            {
                mostrarMensaje("Error: la confirmación es incorrecta", this);
                return;
            }
            RegistrarCambioContrasena registrarCambioContrasena = new RegistrarCambioContrasena(this);
            registrarCambioContrasena.execute(contrasenaActual, contrasenaNueva);
        }
    }

    public void mostrarMensaje(String mensaje, Context context)
    {
        Toast.makeText(context, mensaje , Toast.LENGTH_LONG).show();
    }

    private class InformacionAdministrador extends AsyncTask<String, String, Void> {

        private String  mensajaError = "";
        Respuesta respuesta;
        Context context;

        public InformacionAdministrador(Context context) {
            this.context = context;
        }

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
                mostrarMensaje(mensajaError,context);
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

    private class RegistrarCambioContrasena extends AsyncTask<String, String, Void> {

        private String  mensajaError = "";
        Context context;

        public RegistrarCambioContrasena(Context context) {
            this.context = context;
        }
        @Override
        protected Void doInBackground(String... params) {

            String contrasenaActual = params[0];
            String contaseñaNueva = params[1];
            String urlServicio = "http://udea.dnetix.co/api/password/change?token=" + token;
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            try
            {
                url = new URL(urlServicio);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("old_password", contrasenaActual)
                        .appendQueryParameter("new_password", contaseñaNueva);

                String query = builder.build().getEncodedQuery();
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                outputStream.close();
                httpURLConnection.connect();

                InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                String respuestaServicio = leerInformacionServicio(inputStream);
                JSONObject respJSON = new JSONObject(respuestaServicio);
                boolean statusOK = respJSON.getBoolean("status");
                if(!statusOK)
                {
                    mensajaError = respJSON.getString("error");
                }
            } catch (MalformedURLException e)
            {
                mensajaError = "URL del servicio de invalida";
            } catch (SocketTimeoutException e)
            {
                mensajaError = "Time out en recuparacion de datos o conexion";
            } catch (IOException e)
            {
                mensajaError = "No se pudo leer correctamente respuesta del servicio";
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
            if(!"".equals(mensajaError))
            {
                mostrarMensaje(mensajaError,context);
            }
            else
            {
                mostrarMensaje("Contraseña actualizada correctamente.",context);
                EdContrasenaNueva.setText("");
                EdContrasenaActual.setText("");
                EdContrasenaConfirmar.setText("");
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
