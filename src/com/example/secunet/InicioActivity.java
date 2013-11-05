package com.example.secunet;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class InicioActivity extends Activity{
	
    Intent intentMain;
    Intent intentCheck;
    WifiManager wifiManager;
    Button Registrar;
    ProgressDialog Cargando;
    AlertDialog dialog;
    NfcAdapter nfcAdapter;
    PendingIntent nfcPendingIntent;
    String CodigoTag;
    AlertDialog.Builder builderDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio1);
        
        Registrar = (Button) findViewById(R.id.btRegistrar);
        intentMain = new Intent(this, MainActivity.class);
        intentCheck = new Intent(this, CheckActivity.class);
    	nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

//        Parse.initialize(this,"NJE50gi9UOxCggYxSO2gVFyMkNVQy0w14mZNdcFI","iMZgZ2mzfCJMw8wlyuhqNy89gDFkf6KVtqmyaCgF");
//        PushService.setDefaultPushCallback(this, InicioActivity.class);
//        ParseInstallation.getCurrentInstallation().saveInBackground();
//        PushService.subscribe(this, "global", InicioActivity.class);
//        ParseInstallation.getCurrentInstallation().saveInBackground();
        
        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        builderDialog = new AlertDialog.Builder(this);
        builderDialog.setMessage("Acerca tu teléfono al panel para iniciar...").setTitle("Hola!");
        builderDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.dismiss();
            }
        });
        dialog = builderDialog.create();
        
        Registrar.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v){
        		builderDialog.setMessage("Formulario Registro... (Pendiente)").setTitle("Registro");
        		builderDialog.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
        			public void onClick(DialogInterface dialog, int id) {
        				dialog.dismiss();
        			}
        		});

                dialog = builderDialog.create();
        		dialog.show();
        	}
        });
        
        Cargando = ProgressDialog.show(InicioActivity.this, "Cargando", "Espere por favor...");

        new checkParqueado().execute();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.inicio, menu);
        return true;
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
    public void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag elTag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            CodigoTag = WS_Info.GlobalParameters.bytesToHexString(elTag.getId());
            if (true) {//TODO: validar que se pueda entrar al parqueo..
                new entrarParqueo().execute();
			}
        }    
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
    
    public String getMacAddress() {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        return info.getMacAddress();
    }

    public class checkParqueado extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids){
            Boolean response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_CHECKPARQUEADO);

            request.addProperty("MacAddress", getMacAddress());

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
    
    public class entrarParqueo extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids){
            Boolean response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_ENTRARPARQUEO);

            request.addProperty("idNFC", CodigoTag);
//            request.addProperty("MacAddress", getMacAddress());
            
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.debug = true;
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_ENTRARPARQUEO, envelope);
                response = Boolean.valueOf(envelope.getResponse().toString());
            }  catch (Exception exception)   {
                response = false;
            }
            return response;
        }

        @Override
        protected void onPostExecute(Boolean s){
            super.onPostExecute(s);
            if (s) {
                startActivity(intentMain);
                finish();
        	}
            else{
            	Toast.makeText(getApplicationContext(), "Esta no es la entrada...", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
