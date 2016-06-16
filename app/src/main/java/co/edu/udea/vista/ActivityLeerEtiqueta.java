package co.edu.udea.vista;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

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


public class ActivityLeerEtiqueta extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private Button btnLeerArticulo;
    private Button btnFinalizarPrestamo;
    private NfcAdapter mNfcAdapter;
    private PendingIntent nfcPendingIntent;
    private Spinner spinner;
    private String cedulaEstudiante;
    private String idLaboratorios[];
    private String laboratorios[];
    private String idLabo;
    private String token;
    private String idTag;
    final protected static char[] ArregloHexadecimal = "0123456789ABCDEF".toCharArray();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leer_etiqueta);
        btnLeerArticulo = (Button)findViewById(R.id.btnLeerArticulo);
        btnFinalizarPrestamo = (Button)findViewById(R.id.btnFinalizarPrestamo);
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        btnFinalizarPrestamo.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        token = bundle.getString("token");
        cedulaEstudiante = bundle.getString("cedula");
        laboratorios = bundle.getStringArray("laboratorios");
        idLaboratorios = bundle.getStringArray("idLaboratorios");
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, laboratorios);
        adaptador.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adaptador);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null)
        {
            Toast.makeText(this, "Este dispositivo no soporta NFC.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!mNfcAdapter.isEnabled())
        {
            btnLeerArticulo.setText("NFC deshabilitado");
        }
        /* Se crea un PendingIntent (Intento pendiente) que será asociado a esta actividad
        * Cuando se detecte un intento, asignará los detalles del tag detectado y se lo asigna
        * a esta actividad.
        */
        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_leer_etiqueta, menu);
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
    public void onResume()
    {
        super.onResume();
        if(mNfcAdapter==null){return;}
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] writeTagFilters = new IntentFilter[] {tagDetected};
        mNfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, writeTagFilters, null);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if(mNfcAdapter==null) {return;}
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        Toast.makeText(this, "Cedula Estudiante: " + cedulaEstudiante, Toast.LENGTH_LONG).show();
        setIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))
        {
            try
            {
                Tag tagDeIntento = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                idTag = convertirArregloBytesAStringHexadecimal(tagDeIntento.getId());
            }catch (Exception e)
            {
                e.printStackTrace();
                Toast.makeText(this, "Error obteniendo información del TAG", Toast.LENGTH_LONG).show();
            }
        }
        if(idTag.equals(""))
        {
            Toast.makeText(this, "No se obtuvo informacón al leer el TAG", Toast.LENGTH_LONG).show();
        }
        else
        {
            if(idTag != null && idTag != "")
            {
                spinner.setVisibility(View.VISIBLE);
                btnFinalizarPrestamo.setEnabled(true);
                btnLeerArticulo.setText("SELECCIONE EL LABORATORIO DONDE SE ESTA REALIZANDO EL PRESTAMO");
                Toast.makeText(this, "tag: " + idTag, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(View view)
    {
        if(view.getId() == btnFinalizarPrestamo.getId())
        {
            if(idLabo != null)
            {
                Toast.makeText(this, "idLabo: " + idLabo, Toast.LENGTH_LONG).show();
                RegistrarPrestamo registrarPrestamo = new RegistrarPrestamo(this);
                registrarPrestamo.execute();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int posicion, long l)
    {
        idLabo = idLaboratorios[posicion];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

    private String convertirArregloBytesAStringHexadecimal(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++)
        {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = ArregloHexadecimal[v >>> 4];
            hexChars[(j * 2) + 1] = ArregloHexadecimal[v & 0x0F];
        }
        return new String(hexChars);
    }


    private class RegistrarPrestamo extends AsyncTask<String, String, Void> {

        private String  mensajaError = "";
        Context context;

        public RegistrarPrestamo(Context context) {
            this.context = context;
        }
        @Override
        protected Void doInBackground(String... params) {

            String urlServicio = "http://udea.dnetix.co/api/lend?token" + token;
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
                        .appendQueryParameter("user_id", cedulaEstudiante)
                        .appendQueryParameter("product_id", idTag)
                        .appendQueryParameter("laboratory_id",idLabo);

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
            if("".equals(mensajaError))
            {
                Toast.makeText(ActivityLeerEtiqueta.this, "Prestamo registrado correctamente", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(ActivityLeerEtiqueta.this, mensajaError, Toast.LENGTH_LONG).show();
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
