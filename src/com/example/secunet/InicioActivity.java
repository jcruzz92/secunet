package com.example.secunet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;

public class InicioActivity extends Activity{
    Intent intentMain;
    Intent intentCheck;
    WifiManager wifiManager;
    Button Entrar;
    ProgressDialog Cargando;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio1);
        
        Entrar = (Button) findViewById(R.id.btBuscar);
        intentMain = new Intent(InicioActivity.this, MainActivity.class);
        intentCheck = new Intent(InicioActivity.this, CheckActivity.class);

        Parse.initialize(this,"NJE50gi9UOxCggYxSO2gVFyMkNVQy0w14mZNdcFI","iMZgZ2mzfCJMw8wlyuhqNy89gDFkf6KVtqmyaCgF");
        PushService.setDefaultPushCallback(this, InicioActivity.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();
        PushService.subscribe(this, "global", InicioActivity.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();
        
        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        Entrar.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v){
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.inicio, menu);
        return true;
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
}
