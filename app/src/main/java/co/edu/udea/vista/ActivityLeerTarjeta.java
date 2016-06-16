package co.edu.udea.vista;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import co.edu.udea.modelo.login.Laboratorio;
import co.edu.udea.modelo.login.Respuesta;
import co.edu.udea.modelo.utiles.IntermediarioActividades;

public class ActivityLeerTarjeta extends Activity  {

    private Button btnLeerTarjeta;
    private NfcAdapter mNfcAdapter;
    private PendingIntent nfcPendingIntent;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarjeta);
        btnLeerTarjeta = (Button)findViewById(R.id.btnLeerTarjeta);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null)
        {
            Toast.makeText(this, "Este dispositivo no soporta NFC.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!mNfcAdapter.isEnabled())
        {
            btnLeerTarjeta.setText("NFC deshabilitado");
        }
        /* Se crea un PendingIntent (Intento pendiente) que será asociado a esta actividad
        * Cuando se detecte un intento, asignará los detalles del tag detectado y se lo asigna
        * a esta actividad.
        */
        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_activity_leer_tarjeta, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
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
        setIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))
        {
            // Se obtiene el Tag del intento detectado.
            Tag tagDeIntento = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            //String nombreCompleto = leerTag(tagDeIntento,17);
            String auxiliarCedula = leerTag(tagDeIntento,18);


            if(auxiliarCedula != null)
            {
                String cedula = "";
                for (short indice = 0; indice < auxiliarCedula.length(); indice++)
                {
                    char caracter = auxiliarCedula.charAt(indice);
                    if (esNumerico(caracter))
                    {
                        cedula += caracter;
                    }
                }
                Respuesta respuesta =  (Respuesta) IntermediarioActividades.getObjetoATransmitirEntreActividades();
                int tamanoArregloLaboratorio = respuesta.getData().getListaLaboratorios().size();
                int indice = 0;
                String laboratorios[] = new String[tamanoArregloLaboratorio];
                String idLaboratorios[] = new String[tamanoArregloLaboratorio];

                for (Laboratorio labo : respuesta.getData().getListaLaboratorios()) {
                    laboratorios[indice] = labo.getNombre();
                    idLaboratorios[indice] = labo.getId();
                    indice++;
                }
                Bundle bundle = getIntent().getExtras();
                token = bundle.getString("token");
                Intent intentTag = new Intent(this, ActivityLeerEtiqueta.class);
                intentTag.putExtra("token", token);
                intentTag.putExtra("cedula", cedula);
                intentTag.putExtra("laboratorios", laboratorios);
                intentTag.putExtra("idLaboratorios", idLaboratorios);
                startActivity(intentTag);
            }
        }
    }

    private static boolean esNumerico(char caracter){
        try {
            Integer.parseInt(String.valueOf(caracter));
            return true;
        } catch (NumberFormatException ex){
            return false;
        }
    }

    public String leerTag(Tag tag, int sector){
        //Tipo de tecnología de Tag que vamos a leer
        MifareClassic mifare = MifareClassic.get(tag);

        try {
            // Se habilita la Lectura/Escritura del Tag
            mifare.connect();

            int numeroSectores = mifare.getSectorCount();
            if(numeroSectores != 40)
            {
                Toast.makeText(this, "El tag NFC que se intenta leer no es una TIP", Toast.LENGTH_LONG).show();
                return null;
            }
            byte[] tipoClaveAcceso = tipoDeClaveDeAccesoPorDefecto(mifare, sector);
            String informacionLeida = "";
            int indiceBloque = 0;
            boolean sectorHabilitado = mifare.authenticateSectorWithKeyA(sector, tipoClaveAcceso);
            if(sectorHabilitado)
            {
                indiceBloque = mifare.sectorToBlock(sector);
                byte[] payload1 = mifare.readBlock(indiceBloque);
                informacionLeida += new String(payload1, Charset.forName("US-ASCII"));

                if(sector == 17)
                {
                    byte[] payload2 = mifare.readBlock(indiceBloque + 1);
                    byte[] payload3 = mifare.readBlock(indiceBloque + 2);
                    informacionLeida += new String(payload2, Charset.forName("US-ASCII"));
                    informacionLeida += new String(payload3, Charset.forName("US-ASCII"));
                }
            }
            else
            {
                Toast.makeText(this, "No se tienen los permisos necesarios para leer la informacion.", Toast.LENGTH_LONG).show();
                return null;
            }
            return removeAccents(informacionLeida);
        }
        catch (IOException e)
        {
            Log.e("ActivityLeerTarjeta", "IOException mientras se leían datos de MifareUltralight Tag...", e);
        } finally
        {
            if (mifare != null)
            {
                try
                {
                    mifare.close();
                }
                catch (IOException e)
                {
                    Log.e("ActivityLeerTarjeta", " + Error cerrando tag...", e);
                }
            }
        }
        return null;
    }

    public byte[] tipoDeClaveDeAccesoPorDefecto(MifareClassic mifare, int sector)
    {
        byte[] tipoClaveAcceso = MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY;

        try {
            if(mifare.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT))
            {
                tipoClaveAcceso = MifareClassic.KEY_DEFAULT;
            }
            else if(mifare.authenticateSectorWithKeyA(sector, MifareClassic.KEY_NFC_FORUM))
            {
                tipoClaveAcceso = MifareClassic.KEY_NFC_FORUM;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return tipoClaveAcceso;
    }

    private static Map<Character, Character> MAP_NORM;
    public static String removeAccents(String value)
    {
        if (MAP_NORM == null || MAP_NORM.size() == 0)
        {
            MAP_NORM = new HashMap<Character, Character>();
            MAP_NORM.put('À', 'A');
            MAP_NORM.put('Á', 'A');
            MAP_NORM.put('Â', 'A');
            MAP_NORM.put('Ã', 'A');
            MAP_NORM.put('Ä', 'A');
            MAP_NORM.put('È', 'E');
            MAP_NORM.put('É', 'E');
            MAP_NORM.put('Ê', 'E');
            MAP_NORM.put('Ë', 'E');
            MAP_NORM.put('Í', 'I');
            MAP_NORM.put('Ì', 'I');
            MAP_NORM.put('Î', 'I');
            MAP_NORM.put('Ï', 'I');
            MAP_NORM.put('Ù', 'U');
            MAP_NORM.put('Ú', 'U');
            MAP_NORM.put('Û', 'U');
            MAP_NORM.put('Ü', 'U');
            MAP_NORM.put('Ò', 'O');
            MAP_NORM.put('Ó', 'O');
            MAP_NORM.put('Ô', 'O');
            MAP_NORM.put('Õ', 'O');
            MAP_NORM.put('Ö', 'O');
            MAP_NORM.put('Ñ', 'N');
            MAP_NORM.put('Ç', 'C');
            MAP_NORM.put('ª', 'A');
            MAP_NORM.put('º', 'O');
            MAP_NORM.put('§', 'S');
            MAP_NORM.put('³', '3');
            MAP_NORM.put('²', '2');
            MAP_NORM.put('¹', '1');
            MAP_NORM.put('à', 'a');
            MAP_NORM.put('á', 'a');
            MAP_NORM.put('â', 'a');
            MAP_NORM.put('ã', 'a');
            MAP_NORM.put('ä', 'a');
            MAP_NORM.put('è', 'e');
            MAP_NORM.put('é', 'e');
            MAP_NORM.put('ê', 'e');
            MAP_NORM.put('ë', 'e');
            MAP_NORM.put('í', 'i');
            MAP_NORM.put('ì', 'i');
            MAP_NORM.put('î', 'i');
            MAP_NORM.put('ï', 'i');
            MAP_NORM.put('ù', 'u');
            MAP_NORM.put('ú', 'u');
            MAP_NORM.put('û', 'u');
            MAP_NORM.put('ü', 'u');
            MAP_NORM.put('ò', 'o');
            MAP_NORM.put('ó', 'o');
            MAP_NORM.put('ô', 'o');
            MAP_NORM.put('õ', 'o');
            MAP_NORM.put('ö', 'o');
            MAP_NORM.put('ñ', 'n');
            MAP_NORM.put('ç', 'c');
        }

        if (value == null)
        {
            return "";
        }

        StringBuilder sb = new StringBuilder(value);

        for(int i = 0; i < value.length(); i++)
        {
            Character c = MAP_NORM.get(sb.charAt(i));
            if(c != null)
            {
                sb.setCharAt(i, c.charValue());
            }
        }
        return sb.toString();
    }

}
