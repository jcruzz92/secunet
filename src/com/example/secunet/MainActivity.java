package com.example.secunet;

import java.util.ArrayList;
import java.util.Locale;

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
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
//import android.nfc.Tag;

public class MainActivity extends Activity  implements View.OnClickListener, TextToSpeech.OnInitListener{

    private Spinner ListadoLocales;
    private Spinner ListadoParqueos;
    private Button SeleccionarParqueoManualmente;
    private Button AsignarParqueo;
    private Button AsignarCualquiera;
    private TextView LabelParqueoMasCerca;
    private LinearLayout SeccionManual;
//    private LinearLayout SeccionAuto;
    NfcAdapter nfcAdapter;
    PendingIntent nfcPendingIntent;
    private ProgressDialog Cargando;
    TelephonyManager telephonyManager;
	String IdTelefono; 

    ArrayList<Parqueo> ParqueosManual;
    Parqueo ParqueoAleatorio;
    Parqueo ParqueoManual;
    Parqueo ParqueoAuto;
    private int IdPiso;
    private int IdEstado;
    AlertDialog.Builder builderWiFi;
    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;
    Intent intent;
    Intent intentInterface;
//	private String CodigoTag;

    @Override
    protected void onCreate(Bundle savedInstanceState){
    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        builder.setMessage("DESEA ESTE PARQUEO?");

        intent = new Intent(MainActivity.this, CheckActivity.class);
        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        IdTelefono = telephonyManager.getDeviceId();

        LabelParqueoMasCerca = (TextView) findViewById(R.id.lbAutoParqueo);
        ListadoLocales = (Spinner) findViewById(R.id.spLocales);
        ListadoParqueos = (Spinner) findViewById(R.id.spParqueos);
        AsignarParqueo = (Button) findViewById(R.id.btAsignar);
        AsignarCualquiera = (Button)findViewById(R.id.btCualquiera);
        ParqueosManual = new ArrayList<Parqueo>();
 
        SeleccionarParqueoManualmente = (Button) findViewById(R.id.btManual);
        SeccionManual = (LinearLayout) findViewById(R.id.PARQUEO_MANUAL);
//        SeccionAuto = (LinearLayout) findViewById(R.id.PARQUEO_AUTO);

        ParqueoAleatorio = new Parqueo();
        ParqueoAleatorio.IdParqueo = "~";
        ParqueoManual = new Parqueo();
        ParqueoAuto = new Parqueo();

        SeccionManual.setVisibility(View.GONE);
        new buscarlocales().execute();
        new buscarParqueoAleatorio().execute();
        
        intentInterface = new Intent(MainActivity.this, ParqueoInterfaceActivity.class);
        
        SeleccionarParqueoManualmente.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity.this.startActivity(intentInterface);
			}
		});

        ListadoLocales.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            	if (WS_Info.GlobalParameters.HayLocales) {
					IdPiso = Integer.valueOf(String.valueOf(view.getTag()));
	                new buscarParqueosPorPiso().execute();
	                new buscarParqueoMasCercano().execute();
				}
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ListadoParqueos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
            	if (WS_Info.GlobalParameters.HayParqueos) {
					ParqueoManual = (Parqueo)view.getTag();
				}                
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView){

            }
        });

        AsignarParqueo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	if (WS_Info.GlobalParameters.HayParqueos) {
            		builder.setMessage("Desea este parqueo?");
                    builder .setCancelable(false)
                            .setPositiveButton("Si", new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int id){
                                    String words;
                                    new asignarParqueo().execute(false);
                                    words = "Dirijase al " + ParqueoAuto.Piso + ", parqueo " + ParqueoAuto.IdParqueo.toString() ;
                                    speakWords(words);
                                    MainActivity.this.startActivity(intent);
                                    finish();finish();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    final AlertDialog alertdialog = builder.create();
                    alertdialog.show();
				} else {
					builder.setMessage("No hay parqueos disponibles...");
                    builder .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int id){
                                	dialog.cancel();
                                }
                            });
                    final AlertDialog alertdialog = builder.create();
                    alertdialog.show();
				}
            }
        });

        AsignarCualquiera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
            	if (ParqueoAleatorio.IdParqueo != "~") {
            		builder.setMessage("Desea el parqueo " + ParqueoAleatorio.IdParqueo + ", " + ParqueoAleatorio.Piso + "?");
                    builder .setCancelable(false)
                            .setPositiveButton("Si", new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int id){
                                    MainActivity.this.startActivity(intent);
                                    finish();
                                    String words;
                                    new asignarParqueo().execute(false);
                                    words = "Dirijase al " + ParqueoAleatorio.Piso + ", parqueo " + ParqueoAleatorio.IdParqueo.toString() ;
                                    speakWords(words);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    final AlertDialog alertdialog = builder.create();
                    alertdialog.show();
				} else {
					builder.setMessage("No hay parqueos disponibles...");
                    builder .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int id){
                                	dialog.cancel();
                                }
                            });
                    final AlertDialog alertdialog = builder.create();
                    alertdialog.show();
				}
            }
        });
    }

    public void showSimpleDialog(String Texto){
    	
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
//            Tag elTag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//            CodigoTag = WS_Info.GlobalParameters.bytesToHexString(elTag.getId());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
    public void onInit(int initStatus){
        if (initStatus == TextToSpeech.SUCCESS){
            myTTS.setLanguage(new Locale("spa", "ESP"));
        }
        else if (initStatus == TextToSpeech.ERROR){
            Toast.makeText(this, "Error al cargar comandos de voz", Toast.LENGTH_LONG).show();
        }
    }

    public class buscarlocales extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            Cargando = ProgressDialog.show(MainActivity.this, "Cargando", "Espere por favor...");
        }

        @Override
        protected String doInBackground(Void... voids) {
            String response;
            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_LOCALES);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.debug = true;
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_LOCALES, envelope);
                response = httpTransport.responseDump;
            }  catch (Exception exception)   {
                response = envelope.bodyIn.toString();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            Cargando.dismiss();
            super.onPostExecute(s);
            ArrayList<Local> newlista = WS_Info.GlobalParameters.ParsearLocales(s);
            MyAdapterLocales adapter = new MyAdapterLocales(newlista);
            ListadoLocales.setAdapter(adapter);
        }
    }

    private class MyAdapterLocales implements SpinnerAdapter {
        ArrayList<Local> data;
        public MyAdapterLocales(ArrayList<Local> data){
            this.data = data;
        }

        /**
         * Returns the Size of the ArrayList
         */
        @Override
        public int getCount(){
            return data.size();
        }

        /**
         * Returns one Element of the ArrayList
         * at the specified position.
         */
        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return android.R.layout.simple_spinner_dropdown_item;
        }

        /**
         * Returns the View that is shown when a element was
         * selected.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView v = new TextView(getApplicationContext());
            if (WS_Info.GlobalParameters.HayLocales) {
				v.setTextColor(Color.BLACK);
				v.setTextSize(18);
	            v.setText(data.get(position).Nombre);
	            v.setTag(Integer.parseInt(data.get(position).Nivel));
			} else {
	            v.setText("No hay locales disponibles...");
			}
            
            return v;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            // TODO Auto-generated method stub
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            // TODO Auto-generated method stub
        }

        /**
         * The Views which are shown in when the arrow is clicked
         * (In this case, I used the same as for the "getView"-method.
         */
        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            return this.getView(position, convertView, parent);
        }
    }

    public class buscarParqueosPorPiso extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids){
            String response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_PARQUEOSLIBRES);

            request.addProperty("idPiso", IdPiso);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.debug = true;
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_PARQUEOSLIBRES, envelope);
                response = httpTransport.responseDump;

            }  catch (Exception exception){
                response = envelope.bodyIn.toString();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            ParqueosManual = WS_Info.GlobalParameters.ParsearParqueos(s);
            MyAdapterParqueos adapter = new MyAdapterParqueos(ParqueosManual);
            ListadoParqueos.setAdapter(adapter);
        }
    }

    private class MyAdapterParqueos implements SpinnerAdapter {

        ArrayList<Parqueo> data;
        public MyAdapterParqueos(ArrayList<Parqueo> data){
            this.data = data;
        }

        /**
         * Returns the Size of the ArrayList
         */
        @Override
        public int getCount() {
            return data.size();
        }

        /**
         * Returns one Element of the ArrayList
         * at the specified position.
         */
        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return android.R.layout.simple_spinner_dropdown_item;
        }

        /*
         * Returns the View that is shown when a element was
         * selected.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView v = new TextView(getApplicationContext());
            if (WS_Info.GlobalParameters.HayParqueos) {
            	v.setTextColor(Color.BLACK);
                v.setText(data.get(position).Piso + " - " + data.get(position).IdParqueo);
                v.setTag(data.get(position));
			}
            return v;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            // TODO Auto-generated method stub

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            // TODO Auto-generated method stub

        }

        /**
         * The Views which are shown in when the arrow is clicked
         * (In this case, I used the same as for the "getView"-method.
         */
        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            return this.getView(position, convertView, parent);
        }
    }

    public class asignarParqueo extends AsyncTask<Boolean, Void, String> {
        @Override
        protected String doInBackground(Boolean... cualquiera) {
            String response = null;
            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_SETPARQUEO);

            IdEstado = 1;

            if (cualquiera[0]){
                request.addProperty("idParqueo", ParqueoAleatorio.IdParqueo);
            }
            else{
                request.addProperty("idParqueo", ParqueoAuto.IdParqueo);
            }

            request.addProperty("macAddress", IdTelefono);
            request.addProperty( "idEstado", IdEstado);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.debug = true;
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_SETPARQUEO, envelope);
                response = httpTransport.responseDump;
            }
            catch (Exception exception)   {
                response = envelope.bodyIn.toString();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            new buscarParqueosPorPiso().execute();
            new buscarParqueoMasCercano().execute();
            new buscarParqueoAleatorio().execute();
        }
    }

    public class buscarParqueoMasCercano extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids){
            String response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_PARQUEOMASCERCANO);

            request.addProperty("idPiso", IdPiso);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.debug = true;
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_PARQUEOMASCERCANO, envelope);
                response = httpTransport.responseDump;

            }  catch (Exception exception)   {
                response = envelope.bodyIn.toString();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ParqueoAuto = WS_Info.GlobalParameters.ParsearParqueoUnico(s);
            if (WS_Info.GlobalParameters.HayParqueoUnico) {
				LabelParqueoMasCerca.setText("Parqueo " + ParqueoAuto.IdParqueo + ", " + ParqueoAuto.Piso);
			} else {
				LabelParqueoMasCerca.setText("No hay parqueos disponibles...");
			}
            
        }
    }

    public class buscarParqueoAleatorio extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids){
            String response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_PARQUEOALEATORIO);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.debug = true;
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_PARQUEOALEATORIO, envelope);
                response = httpTransport.responseDump;

            }  catch (Exception exception)   {
                response = envelope.bodyIn.toString();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ParqueoAleatorio = WS_Info.GlobalParameters.ParsearParqueoUnico(s);
        }
    }
}






