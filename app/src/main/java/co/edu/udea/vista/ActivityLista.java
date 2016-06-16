package co.edu.udea.vista;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.util.ArrayList;

import co.edu.udea.modelo.listado.ListaArticulosAdaptador;
import co.edu.udea.modelo.listado.ListaArticulosEntrada;
import co.edu.udea.modelo.listado.Respuesta;

public class ActivityLista extends Activity {

    private ListView lista;
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado);
        iniciarComponentes();
        ListarArtículos listaArticulos = new ListarArtículos();
        listaArticulos.execute();
    }

    public void iniciarComponentes()
    {
        Bundle bundle = getIntent().getExtras();
        token = bundle.getString("token");
    }

    private class ListarArtículos extends AsyncTask<String, String, Void> {

        private String  mensajaError = "";
        Respuesta respuesta;
        Integer numeroArticulos = 0;

        @Override
        protected Void doInBackground(String... params) {

            String urlServicio = "http://udea.dnetix.co/api/products/" + "?token=" + token;
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
                    mensajaError = "No se puede cargar la informacion";
                    return null;
                }
                Gson gson = new Gson();
                respuesta = gson.fromJson(respuestaServicio, Respuesta.class);
                numeroArticulos = respuesta.getData().size();
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
                ArrayList<ListaArticulosEntrada> datos = new ArrayList<ListaArticulosEntrada>();
                for(int i=0; i<numeroArticulos;i++){
                    datos.add(new ListaArticulosEntrada(R.drawable.ic_launcher, respuesta.getData().get(i).getNombres(), respuesta.getData().get(i).getDescripcion()));
                }
                lista = (ListView) findViewById(R.id.ListView_listado);
                lista.setAdapter(new ListaArticulosAdaptador(ActivityLista.super.getApplicationContext(), R.layout.activity_entrada, datos){
                    @Override
                    public void onEntrada(Object entrada, View view) {
                        if (entrada != null) {
                            TextView texto_superior_entrada = (TextView) view.findViewById(R.id.textView_superior);
                            if (texto_superior_entrada != null)
                                texto_superior_entrada.setText(((ListaArticulosEntrada) entrada).get_textoEncima());

                            TextView texto_inferior_entrada = (TextView) view.findViewById(R.id.textView_inferior);
                            if (texto_inferior_entrada != null)
                                texto_inferior_entrada.setText(((ListaArticulosEntrada) entrada).get_textoDebajo());

                            ImageView imagen_entrada = (ImageView) view.findViewById(R.id.imageView_imagen);
                            if (imagen_entrada != null)
                                imagen_entrada.setImageResource(((ListaArticulosEntrada) entrada).get_idImagen());
                        }
                    }
                });

                lista.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                        ListaArticulosEntrada elegido = (ListaArticulosEntrada) pariente.getItemAtPosition(posicion);
                        CharSequence texto = "Seleccionado: " + elegido.get_textoDebajo();
                        Toast toast = Toast.makeText(ActivityLista.this, texto, Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
            }
            else
            {
                Toast.makeText(ActivityLista.this, mensajaError, Toast.LENGTH_LONG).show();
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

