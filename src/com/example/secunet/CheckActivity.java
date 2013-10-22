package com.example.secunet;

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
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import org.ndeftools.*;
import org.ndeftools.externaltype.AndroidApplicationRecord;

import java.util.List;
import java.util.Locale;

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

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check1);

    	nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        TextoMiParqueo = (TextView) findViewById(R.id.lbMiParqueo);
        LabelPark = (TextView) findViewById(R.id.labelPrk);
        LabelIndicaciones = (TextView) findViewById(R.id.textView1);
        Repetir = (Button) findViewById(R.id.btRepetir);
        Liberar = (Button) findViewById(R.id.btLiberarParqueo);
        intentParqueoLibre = new Intent(CheckActivity.this, ParqueoLibreActivity.class);
        MiParqueo = new Parqueo();
        MacAddress = getMacAddress();
        
        Repetir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakWords("Su parqueo asignado es el " + TextoMiParqueo.getText().toString());
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
            CodigoTag = bytesToHexString(elTag.getId());
            idTag.setText(bytesToHexString(elTag.getId()));
            
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
            new parquearse().execute();
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
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
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
            TextoMiParqueo.setText("Parqueo " + MiParqueo.IdParqueo + ", " + MiParqueo.Piso);
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
            	LabelPark.setText("Esta parqueado en:");
            	LabelIndicaciones.setVisibility(View.INVISIBLE);
			}
            else{
            	///error
            }
        }
    }
}
