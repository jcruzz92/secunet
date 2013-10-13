package com.example.secunet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;

import java.util.ArrayList;
import java.util.Locale;

//Esta es una prueba de commits para Jorge
public class MainActivity extends Activity  implements View.OnClickListener, TextToSpeech.OnInitListener{

    private Spinner ListadoLocales;
    private Spinner ListadoParqueos;
    private Switch SeleccionarParqueoManualmente;
    private Button AsignarParqueo;
    private Button AsignarCualquiera;
    private TextView LabelParqueoMasCerca;
    private LinearLayout SeccionManual;
    private LinearLayout SeccionAuto;
    private ProgressDialog Cargando;

    ArrayList<Parqueo> ParqueosManual;
    Parqueo ParqueoAleatorio;
    Parqueo ParqueoManual;
    Parqueo ParqueoAuto;
    private int IdPiso;
    private String MacAddress;
    private int IdEstado;
    private boolean Parqueado;

    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState){
    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("DESEA ESTE PARQUEO?");

        intent = new Intent(MainActivity.this, CheckActivity.class);
        final AlertDialog.Builder builderWiFi = new AlertDialog.Builder(this);
        builderWiFi.setMessage("Debe conectarse a nuestra red Wi-Fi. ¿Conectar?");
        MacAddress = getMacAddress();

        /*
        try{
        } catch (Exception exception){
            builderWiFi .setCancelable(false)
            .setPositiveButton("Si", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int id){
                    //TODO: Activar Wi-Fi y Conectarse al AP
                }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            final AlertDialog alertdialog = builder.create();
            alertdialog.show();
        }*/

        LabelParqueoMasCerca = (TextView) findViewById(R.id.lbAutoParqueo);
        ListadoLocales = (Spinner) findViewById(R.id.spLocales);
        ListadoParqueos = (Spinner) findViewById(R.id.spParqueos);
        AsignarParqueo = (Button) findViewById(R.id.btAsignar);
        AsignarCualquiera = (Button)findViewById(R.id.btCualquiera);
        ParqueosManual = new ArrayList<Parqueo>();

        SeleccionarParqueoManualmente = (Switch) findViewById(R.id.elegirMiParqueo);
        SeccionManual = (LinearLayout) findViewById(R.id.PARQUEO_MANUAL);
        SeccionAuto = (LinearLayout) findViewById(R.id.PARQUEO_AUTO);

        ParqueoAleatorio = new Parqueo();
        ParqueoManual = new Parqueo();
        ParqueoAuto = new Parqueo();

        SeccionManual.setVisibility(View.GONE);
        new buscarlocales().execute();
        new buscarParqueoAleatorio().execute();

        SeleccionarParqueoManualmente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(SeleccionarParqueoManualmente.isChecked()){
                SeccionManual.setVisibility(View.VISIBLE);
                SeccionAuto.setVisibility(View.GONE);
            }
            else{
                SeccionManual.setVisibility(View.GONE);
                SeccionAuto.setVisibility(View.VISIBLE);
            }
                }
        });

        ListadoLocales.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                IdPiso = Integer.valueOf(String.valueOf(view.getTag()));
                new buscarParqueosPorPiso().execute();
                new buscarParqueoMasCercano().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ListadoParqueos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
                ParqueoManual = (Parqueo)view.getTag();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView){

            }
        });

        AsignarParqueo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.setMessage("Desea este parqueo?");
                MacAddress = getMacAddress();
                builder .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                MainActivity.this.startActivity(intent);
                                finish();
                                String words;
                                new asignarParqueo().execute(false);
                                if(SeleccionarParqueoManualmente.isChecked()){
                                    words = "Dirijase al " + ParqueoManual.Piso + ", parqueo " + ParqueoManual.IdParqueo.toString() ;
                                }else{
                                    words = "Dirijase al " + ParqueoAuto.Piso + ", parqueo " + ParqueoAuto.IdParqueo.toString() ;
                                }
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

            }
        });

        AsignarCualquiera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                builder.setMessage("Desea el parqueo " + ParqueoAleatorio.IdParqueo + ", " + ParqueoAleatorio.Piso + "?");
                MacAddress = getMacAddress();
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
            }
        });
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
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
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

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
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
            v.setTextColor(Color.WHITE);
            v.setText(data.get(position).Nombre);
            v.setTextSize(20);
            v.setTag(Integer.parseInt(data.get(position).Nivel));
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
        protected String doInBackground(Void... voids) {
            String response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_PARQUEOSLIBRES);

            PropertyInfo property_idPiso = new PropertyInfo();
            property_idPiso.type = PropertyInfo.INTEGER_CLASS;
            property_idPiso.name = "idPiso";

            request.addProperty(property_idPiso, IdPiso);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.debug = true;
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_PARQUEOSLIBRES, envelope);
                response = httpTransport.responseDump;

            }  catch (Exception exception)   {
                response = httpTransport.responseDump;
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
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
            v.setTextColor(Color.WHITE);
            v.setText(data.get(position).Piso + " - " + data.get(position).IdParqueo + " - " + data.get(position).Estado);
            v.setTextSize(20);
            v.setTag(data.get(position));
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
                if(SeleccionarParqueoManualmente.isChecked()){
                    request.addProperty("idParqueo", ParqueoManual.IdParqueo);
                }
                else{
                    request.addProperty("idParqueo", ParqueoAuto.IdParqueo);
                }
            }

            request.addProperty("macAddress", MacAddress);
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
            LabelParqueoMasCerca.setText("Parqueo " + ParqueoAuto.IdParqueo + ", Nivel " + ParqueoAuto.Piso + ": " + ParqueoAuto.Estado);
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






