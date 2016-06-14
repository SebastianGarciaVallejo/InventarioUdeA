package co.edu.udea.vista;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class ActivityLeerEtiqueta extends Activity implements View.OnClickListener {

    private Button btnLeerArticulo;
    private TextView salida;
    private NfcAdapter mNfcAdapter;
    private PendingIntent nfcPendingIntent;
    final protected static char[] ArregloHexadecimal = "0123456789ABCDEF".toCharArray();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leer_etiqueta);
        btnLeerArticulo = (Button)findViewById(R.id.btnLeerArticulo);
        salida = (TextView)findViewById(R.id.salidaArticulo);
        btnLeerArticulo.setOnClickListener(this);
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
        else
        {
            btnLeerArticulo.setText("NFC Habilitado");
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
    public void onClick(View view) {

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
        setIntent(intent);
        String id= "";
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))
        {
            try
            {
                Tag tagDeIntento = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                id = convertirArregloBytesAStringHexadecimal(tagDeIntento.getId());
            }catch (Exception e)
            {
                e.printStackTrace();
                Toast.makeText(this, "Error obteniendo información del TAG", Toast.LENGTH_LONG).show();
            }
        }
        if(id.equals(""))
        {
            Toast.makeText(this, "No se obtuvo informacón al leer el TAG", Toast.LENGTH_LONG).show();
        }
        else
        {
            salida.setText(id);
            // Ejecutar servicio
        }
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

}
