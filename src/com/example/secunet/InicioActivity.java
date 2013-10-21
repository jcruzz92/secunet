package com.example.secunet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;

import org.ndeftools.*;
import org.ndeftools.externaltype.AndroidApplicationRecord;

import java.util.List;

public class InicioActivity extends Activity {

    private String MacAddress;
    Intent intentMain;
    Intent intentCheck;
    WifiManager wifiManager;
    Button Entrar;
    List<WifiConfiguration> listaWifi;
    ProgressDialog Cargando;
    NfcAdapter nfcAdapter;
    PendingIntent nfcPendingIntent;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState){
    	nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        
    	Parse.initialize(this,"NJE50gi9UOxCggYxSO2gVFyMkNVQy0w14mZNdcFI","iMZgZ2mzfCJMw8wlyuhqNy89gDFkf6KVtqmyaCgF");
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio1);
        Entrar = (Button) findViewById(R.id.btBuscar);
        PushService.setDefaultPushCallback(InicioActivity.this, InicioActivity.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();
        PushService.subscribe(InicioActivity.this,"global", MainActivity.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();
        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        getMacAddress();
        intentMain = new Intent(InicioActivity.this, MainActivity.class);
        intentCheck = new Intent(InicioActivity.this, CheckActivity.class);
        
        Entrar.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        	    InicioActivity.this.startActivity(intentMain);
                finish();
        	}
        });
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.dismiss();
            }
        });
        builder.setPositiveButton("Buscar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                InicioActivity.this.startActivity(intentMain);
                finish();
            }
        });
        
        builder.setMessage("Toca el botón buscar para iniciar...").setTitle("Hola!");
        dialog = builder.create();
        
        Cargando = ProgressDialog.show(InicioActivity.this, "Cargando", "Espere por favor...");
        
        new checkParqueado().execute();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        disableForegroundMode();
    }
    
    @Override
    public void onResume(){
        super.onResume();
        enableForegroundMode();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.inicio, menu);
        return true;
    }

    @Override
    public void onNewIntent(Intent intent) { // this method is called when an NFC tag is scanned

        // check for NFC related actions
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {   
        	dialog.dismiss();
            Toast.makeText(this, "Tag Encontrado", Toast.LENGTH_SHORT).show();
        	
            Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            Tag elTag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            TextView textView = (TextView) findViewById(R.id.leido);
            textView.setText(bytesToHexString(elTag.getId()));
            
            if (messages != null) { 
                // parse to records
                for (int i = 0; i < messages.length; i++) {
                    try {
                        List<Record> records = new Message((NdefMessage)messages[i]);

                        for(int k = 0; k < records.size(); k++) {
                            Record record = records.get(k);
                            if(record instanceof AndroidApplicationRecord) {
                                AndroidApplicationRecord aar = (AndroidApplicationRecord) record;
                                TextView textView1 = (TextView) findViewById(R.id.idTag);
                                textView1.setText( "El Paquete es " + aar.getPackageName());
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Problema parseando el mensaje", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }    
    }
    
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }

        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);  
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);  
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }

        return stringBuilder.toString();
    }
    
    public void enableForegroundMode() {
        // foreground mode gives the current active application priority for reading scanned tags
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED); // filter for tags
        IntentFilter[] writeTagFilters = new IntentFilter[] {tagDetected};
        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, writeTagFilters, null);
    }

	public void disableForegroundMode() {
	    nfcAdapter.disableForegroundDispatch(this);
	}
    
    public Boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return networkInfo == null ? false : networkInfo.getState() == NetworkInfo.State.CONNECTED;
    }

    public Boolean WiFiExiste(){
        listaWifi = wifiManager.getConfiguredNetworks();

        for (WifiConfiguration i : listaWifi){
            if(i.SSID != null && i.SSID.equals(WS_Info.GlobalParameters.networkSSID)){
                return true;
            }
        }
        return false;
    }

    public void agregarWiFi(){
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID =  WS_Info.GlobalParameters.networkSSID;
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiManager.addNetwork(conf);
    }

    public void conectarWiFi(){
        listaWifi = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : listaWifi){
            if(i.SSID != null && i.SSID.equals(WS_Info.GlobalParameters.networkSSID)){
                //wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                break;
            }
        }
    }

    public void getMacAddress() {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        MacAddress = info.getMacAddress();
    }

    public class checkParqueado extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids){
            Boolean response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_CHECKPARQUEADO);

            request.addProperty("MacAddress", MacAddress);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_CHECKPARQUEADO, envelope);
                response = Boolean.valueOf(envelope.getResponse().toString());
            }  catch (Exception exception){
                response = false;
            }

            return response;
        }

        @Override
        protected void onPostExecute(Boolean esta_parqueado){
            super.onPostExecute(esta_parqueado);
            Cargando.dismiss();
            if(esta_parqueado){
                InicioActivity.this.startActivity(intentCheck);
                finish();
            }
            else{
                dialog.show();
            }
        }
    }


}
