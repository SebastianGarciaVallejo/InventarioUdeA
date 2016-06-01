package co.edu.udea.vista;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import co.edu.udea.modelo.login.Respuesta;


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

    public void comprobarConexionInternet(View view) {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (!(networkInfo != null && networkInfo.isConnected()))
        {
            Toast.makeText(MainActivity.this, "Por favor encienda su conexión a internet", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == btnIngresar.getId())
        {
            String usuario = editTextUsuario.getText().toString();
            String contrasena = editTextContrasena.getText().toString();
            if(fueronLosCamposIngresados(usuario, contrasena))
            {
                ValidarUsuarioRegistrado validarUsuarioRegistrado = new ValidarUsuarioRegistrado(this);
                validarUsuarioRegistrado.execute(usuario, contrasena);
            }
        }
    }

    public boolean fueronLosCamposIngresados(String usuario, String contrasena)
    {
        String mensaje = "";
        if("".equals(usuario) && "".equals(contrasena))
        {
            mensaje = "Por favor ingrese usuario y contraseña";
        }
        else if("".equals(usuario))
        {
            mensaje = "Por favor ingrese su usuario";
        }
        else if("".equals(contrasena))
        {
            mensaje = "Por favor ingrese la contraseña";
        }
        if(!"".equals(mensaje))
        {
            Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private class ValidarUsuarioRegistrado extends AsyncTask<String, String, Void> {

        private boolean esAdministrador;
        private String  mensajaError = "";
        Respuesta       respuesta;
        Context         context;

        public ValidarUsuarioRegistrado(Context context) {
            this.context = context;
        }
        @Override
        protected Void doInBackground(String... params) {

            String usuario = params[0];
            String contrasena = params[1];
            String urlServicio = "http://udea.dnetix.co/api/auth";
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
                        .appendQueryParameter("username", usuario)
                        .appendQueryParameter("password", contrasena);

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
                    mensajaError = "Usuario o contraseña incorrecto";
                    esAdministrador = false;
                    return null;
                }
                esAdministrador = true;
                Gson gson = new Gson();
                respuesta = gson.fromJson(respuestaServicio, Respuesta.class);
            } catch (MalformedURLException e)
            {
                mensajaError = "URL del servicio de login invalida";
            } catch (SocketTimeoutException e)
            {
                mensajaError = "Time out en recuparacion de datos o conexion";
            } catch (IOException e)
            {
                mensajaError = "No se pudo leer correctamente el servicio de login";
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
            if(esAdministrador)
            {
                Intent intent = new Intent(context, ActivityLeerTarjeta.class);
                //intent.putExtra("DatosRespuesta", respuesta);
                context.startActivity(intent);
                ((Activity)context).finish();
            }
            else
            {
                Toast.makeText(MainActivity.this, mensajaError, Toast.LENGTH_LONG).show();
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
