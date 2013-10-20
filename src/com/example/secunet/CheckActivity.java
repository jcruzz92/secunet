package com.example.secunet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.util.Locale;

public class CheckActivity extends Activity implements  View.OnClickListener, TextToSpeech.OnInitListener{

    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;
    private TextView TextoMiParqueo;
    private String MacAddress;
    private Parqueo MiParqueo;
    private Button Repetir;
    private Button Liberar;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check1);
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        TextoMiParqueo = (TextView) findViewById(R.id.lbMiParqueo);
        Repetir = (Button) findViewById(R.id.btRepetir);
        Liberar = (Button) findViewById(R.id.btLiberarParqueo);
        intent = new Intent(CheckActivity.this, ParqueoLibreActivity.class);

        Repetir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakWords("Su parqueo asignado es el " + TextoMiParqueo.getText().toString());
            }
        });
        
        Liberar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CheckActivity.this.startActivity(intent);
				finish();
				new liberarParqueoAsignado().execute();
			}
		});

        MiParqueo = new Parqueo();
        MacAddress = getMacAddress();
        new buscarParqueoAsignado().execute();
    }

    @Override 
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.check, menu);
        return true;
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
}
