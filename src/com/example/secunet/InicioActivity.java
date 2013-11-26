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
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class InicioActivity extends Activity{

    Intent intentMain;
    Intent intentRegistro;
    Intent intentAgregarACuenta;
    Intent intentCheck;
    WifiManager wifiManager;
    Button Registrar;
    ProgressDialog Cargando;
    NfcAdapter nfcAdapter;
    PendingIntent nfcPendingIntent;
    String CodigoTag;
    TelephonyManager telephonyManager;
	String IdTelefono; 
	Boolean PuedeIngresar;
	AlertDialog.Builder builderDialogRegistro;
	AlertDialog.Builder builderDialogInicio;
    AlertDialog dialogInicio;
    AlertDialog dialogRegistro;
    TextView EstadoInicial;
    RelativeLayout Botones;

    @Override
    protected void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio1);
        
        PuedeIngresar = false;
        Registrar = (Button) findViewById(R.id.btRegistrar);
        intentMain = new Intent(this, MainActivity.class);
        intentCheck = new Intent(this, CheckActivity.class);
        intentRegistro = new Intent(this, RegistrarActivity.class);
    	nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        IdTelefono = telephonyManager.getDeviceId();
        EstadoInicial = (TextView) findViewById(R.id.estado_inicial);
        Botones = (RelativeLayout)findViewById(R.id.botones);
        Botones.setVisibility(View.INVISIBLE);
        
        
        builderDialogRegistro = new AlertDialog.Builder(this);
        builderDialogRegistro.setMessage("Este dispositivo no está vinculado a una cuenta.").setTitle("Registro Necesario");
        builderDialogRegistro.setPositiveButton("Vincular", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialogRegistro.dismiss();
                InicioActivity.this.startActivity(intentRegistro);
			}
		});
        builderDialogRegistro.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialogRegistro.dismiss();
			}		
		});
        dialogRegistro = builderDialogRegistro.create();
        
        builderDialogInicio = new AlertDialog.Builder(this);
        builderDialogInicio.setMessage("Acerca tu teléfono al panel para iniciar...").setTitle("Hola!");
        builderDialogInicio.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialogInicio.dismiss();
            }
        });
        dialogInicio = builderDialogInicio.create();
        
        Registrar.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v){
                InicioActivity.this.startActivity(intentRegistro);
        	}
        });
        
        Cargando = ProgressDialog.show(InicioActivity.this, "Cargando", "Espere por favor...");

        new checkParqueado().execute();
    }
    
    public void mostrarDialogRegistro(){
    	dialogRegistro.show();
    }
    
    public void mostrarDialogInicio(){
        dialogInicio.show();
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
            if (PuedeIngresar) {
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
    
    public class checkParqueado extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids){
            Boolean response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_CHECKPARQUEADO);

            request.addProperty("MacAddress", IdTelefono);

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
            	new verificarDispositivoRegistrado().execute();
            }
        }
    }
    
    public class verificarDispositivoRegistrado extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids){
            Boolean response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_VERIFICARDISPOSITIVOREGISTRADO);

            request.addProperty("Imei", IdTelefono);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_VERIFICARDISPOSITIVOREGISTRADO, envelope);
                response = Boolean.valueOf(envelope.getResponse().toString());
            }  catch (Exception exception){
                response = false;
            }

            return response;
        }

        @Override
        protected void onPostExecute(Boolean dispositivo_existe){
            super.onPostExecute(dispositivo_existe);
            if(dispositivo_existe){
            	PuedeIngresar = true;
            	mostrarDialogInicio();
            	EstadoInicial.setText("Acerca tu dispositivo al panel para acceder...");
            }else {
            	mostrarDialogRegistro();
            	EstadoInicial.setText("Debes ser un usuario registrado para acceder...");
            	Botones.setVisibility(View.VISIBLE);
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
