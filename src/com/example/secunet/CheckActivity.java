package com.example.secunet;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.ndeftools.Message;
import org.ndeftools.Record;
import org.ndeftools.externaltype.AndroidApplicationRecord;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CheckActivity extends Activity implements  View.OnClickListener, TextToSpeech.OnInitListener{
    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;
    private TextView TextoMiParqueo;
    private TextView LabelPark;
    private TextView LabelIndicaciones;
    private String MacAddress;
    private Parqueo MiParqueo;
    private Button Repetir;
    private Button Liberar;
    Intent intentParqueoLibre;
    NfcAdapter nfcAdapter;
    PendingIntent nfcPendingIntent;
    String CodigoTag;
    Boolean Parqueado = false;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check1);

        intent = new Intent(this, InicioActivity.class);
    	nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        TextoMiParqueo = (TextView) findViewById(R.id.lbMiParqueo);
        LabelPark = (TextView) findViewById(R.id.labelPrk);
        LabelIndicaciones = (TextView) findViewById(R.id.lbIndicaciones);
        Repetir = (Button) findViewById(R.id.btRepetir);
        Liberar = (Button) findViewById(R.id.btLiberarParqueo);
        intentParqueoLibre = new Intent(CheckActivity.this, ParqueoLibreActivity.class);
        MiParqueo = new Parqueo();
        MacAddress = getMacAddress();
        
        Repetir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakWords("Su parqueo asignado es el " + TextoMiParqueo.getText().toString() + "...");
            }
        });

        Liberar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
				CheckActivity.this.startActivity(intentParqueoLibre);
				finish();
				new liberarParqueoAsignado().execute();
			}
		});

//        Liberar.setVisibility(View.GONE);
        new buscarParqueoAsignado().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.check, menu);
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
            Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            Tag elTag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            TextView idTag = (TextView) findViewById(R.id.leido);
            CodigoTag = WS_Info.GlobalParameters.bytesToHexString(elTag.getId());
            idTag.setText(CodigoTag);

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

            if (MiParqueo.idEstado == 1) {//asignado
            	new parquearse().execute();
        	}
            else if (MiParqueo.idEstado == 2) { //parqueado
            	new desparquear().execute();
            }
            else if (MiParqueo.idEstado == 3) {//desocupado
            	new salirParqueo().execute();
			}
        }   
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                myTTS = new TextToSpeech(this, this );
            }
            else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
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
    
    private void speakWords(String speech) {
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    public String getMacAddress() {
        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        return info.getMacAddress();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS){
            myTTS.setLanguage(new Locale("spa", "ESP"));
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(getApplicationContext(), "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    public class buscarParqueoAsignado extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids){
            String response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_BYMAC);

            MacAddress = getMacAddress();
            request.addProperty("MacAddress", MacAddress);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.debug = true;
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_BYMAC, envelope);
                response = httpTransport.responseDump;
            }  catch (Exception exception)   {
                response = envelope.bodyIn.toString();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            MiParqueo = WS_Info.GlobalParameters.ParsearParqueoUnico(s);
            if (MiParqueo.idEstado == 1) {//asignado
            	Parqueado = false;
            	LabelPark.setText("Tu Parqueo Asignado es:");
            	LabelIndicaciones.setText("Cuando llegues a tu parqueo, acerca tu dispositivo al panel indicado para registrar que te has parqueado y listo.");
			}
            else if (MiParqueo.idEstado == 2) { //parqueado
            	Parqueado = true;
            	LabelPark.setText("Estás Parqueado en:");
            	LabelIndicaciones.setText("Antes de retirarte, acerca tu dispositivo nuevamente al panel indicado. Con esto liberarás el parqueo y otros podrán usarlo.");
			}
            else if (MiParqueo.idEstado == 3) {//desocupado
            	Parqueado = false;
            	LabelPark.setText("Liberaste el Parqueo:");
            	LabelIndicaciones.setText("Dirígete a la salida más cercaca...");
			}
            TextoMiParqueo.setText("Parqueo " + MiParqueo.IdParqueo + ", " + MiParqueo.Piso);
            suscribe("c" + MiParqueo.IdParqueo);
        }
    }

    public class liberarParqueoAsignado extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids){
            String response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_LIBERARPARQUEO);

            request.addProperty("idParqueo", MiParqueo.IdParqueo);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.debug = true;
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_LIBERARPARQUEO, envelope);
                response = httpTransport.responseDump;

            }  catch (Exception exception)   {
                response = "";
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
        }
    }

    public class parquearse extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids){
            Boolean response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_PARQUEARSE);

            request.addProperty("idParqueo", MiParqueo.IdParqueo);
            request.addProperty("idTag", CodigoTag);
            request.addProperty("MacAddress", getMacAddress());
            
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.debug = true;
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_PARQUEARSE, envelope);
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
            	Parqueado = true;
            	MiParqueo.idEstado = 2;
            	LabelPark.setText("Estás Parqueado en:");
            	LabelIndicaciones.setText("Antes de retirarte, acerca tu dispositivo nuevamente al panel indicado. Con esto liberarás el parqueo y otros podrán usarlo.");
            	speakWords("Te has parqueado correctamente!");
            }
            else{
            	Toast.makeText(getApplicationContext(), "Parqueo incorrecto, verifica tu ubicación...", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    public class desparquear extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids){
            Boolean response;
            
            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_DESOCUPARPARQUEO);
            
            request.addProperty("MacAddress", getMacAddress());
            request.addProperty("idNFC", CodigoTag);
            
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.debug = true;
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_DESOCUPARPARQUEO, envelope);
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
            	Parqueado = false;
            	MiParqueo.idEstado = 3;
            	LabelPark.setText("Liberaste el Parqueo:");
            	LabelIndicaciones.setText("Dirígete a la salida más cercana...");
            	speakWords("Dirígete a la salida más cercana!");
//            	unsuscribe();
			}
            else{
            	Toast.makeText(getApplicationContext(), "Intentas liberar un parqueo que no se te ha asignado...", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    public class salirParqueo extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids){
            Boolean response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_SALIRPARQUEO);

            request.addProperty("idNFC", CodigoTag);
            request.addProperty("MacAddress", getMacAddress());
            
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.debug = true;
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_SALIRPARQUEO, envelope);
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
            	Parqueado = false;
                startActivity(intent);
                finish();
                unsuscribe();
//            	speakWords("Gracias por visitarnos, conduce con cuidado!");
        	}
            else{
            	Toast.makeText(getApplicationContext(), "Esta no es la salida...", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    public void suscribe(String idParqueo){
       	Parse.initialize(this, "NJE50gi9UOxCggYxSO2gVFyMkNVQy0w14mZNdcFI", "iMZgZ2mzfCJMw8wlyuhqNy89gDFkf6KVtqmyaCgF"); 
        PushService.subscribe(this, idParqueo, ConfirmarActivity.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
    
    public void unsuscribe (){
		Parse.initialize(this, "NJE50gi9UOxCggYxSO2gVFyMkNVQy0w14mZNdcFI", "iMZgZ2mzfCJMw8wlyuhqNy89gDFkf6KVtqmyaCgF"); 
		PushService.setDefaultPushCallback(this, ConfirmarActivity.class);
		final Set<String> setOfAllSubscriptions = PushService.getSubscriptions(this);
		final String[] allSubscriptions = setOfAllSubscriptions.toArray(new String[0]); 
		for(int k=0; k<allSubscriptions.length; k++)
		{
			PushService.unsubscribe(this, allSubscriptions[k]);
		}
		ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
