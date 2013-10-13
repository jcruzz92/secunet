package com.parqueo.proyecto1android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.List;

public class InicioActivity extends Activity {

    private String MacAddress;
    Intent intentMain;
    Intent intentCheck;
    WifiManager wifiManager;
    List<WifiConfiguration> listaWifi;
    ProgressDialog Cargando;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        Cargando = ProgressDialog.show(InicioActivity.this, "Cargando", "Espere por favor...");

        /*
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
            while(!wifiManager.isWifiEnabled()){

            }
            //wifiManager.disconnect();
        }

        if (WiFiExiste()){
            conectarWiFi();
        }else{
            agregarWiFi();
            conectarWiFi();
       }

        while (!isConnected(InicioActivity.this)) {

        }*/

        intentMain = new Intent(InicioActivity.this, MainActivity.class);
        intentCheck = new Intent(InicioActivity.this, CheckActivity.class);
        getMacAddress();
        new checkParqueado().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.inicio, menu);
        return true;
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
                InicioActivity.this.startActivity(intentMain);
                finish();
            }
        }
    }
}
