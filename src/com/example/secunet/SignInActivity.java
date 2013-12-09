package com.example.secunet;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.example.secunet.AgregarActivity.registrarDispositivo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SignInActivity extends Activity {
    private Parqueo MiParqueo;
    TelephonyManager telephonyManager;
	String IdTelefono;
	Boolean HayAlerta;
	TextView Username;
	TextView Password; 
	Button Iniciar;
	Intent intentCheck;
	Integer NuevoEstadoParqueo;
	
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        intentCheck = new Intent(getApplicationContext(), CheckActivity.class);
		Iniciar = (Button) findViewById(R.id.iniciar1);
        Username = (TextView)findViewById(R.id.username1);
        Password = (TextView)findViewById(R.id.password1);
        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        IdTelefono = telephonyManager.getDeviceId();

        Iniciar.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v){
        		String Nombre = Username.getText().toString(),
        				Clave = Password.getText().toString();
                if (Nombre.length() == 0) {
					Toast.makeText(getApplicationContext(), "Ingresa tu nombre de usuario", Toast.LENGTH_SHORT).show();
				}else if (Clave.length() == 0) {
					Toast.makeText(getApplicationContext(), "Ingresa tu contraseña", Toast.LENGTH_SHORT).show();
				}else if (Nombre.length() > 0 & Clave.length() > 0) {
					new iniciarSesion().execute();
				}
        	}
        });
        
        new buscarParqueoAsignado().execute();
    }
    
	public class buscarParqueoAsignado extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids){
            String response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_BYMAC);

            request.addProperty("MacAddress", IdTelefono);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
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
        }
    }
	
	public class actualizarParqueo extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids){
            String response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_CAMBIARESTADOPARQUEO);

            request.addProperty("idParqueo", MiParqueo.IdParqueo);
            request.addProperty("idEstado", NuevoEstadoParqueo);
            
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.debug = true;
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_CAMBIARESTADOPARQUEO, envelope);
                response = httpTransport.responseDump;
            }  catch (Exception exception)   {
                response = envelope.bodyIn.toString();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
			startActivity(intentCheck);
			finish(); 
        }
    }
	
    public class iniciarSesion extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids){
            Boolean response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_VERIFICARUSUARIOCLAVE);

            request.addProperty("_usuario", Username.getText().toString());
            request.addProperty("clave", Password.getText().toString());

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_VERIFICARUSUARIOCLAVE, envelope);
                response = Boolean.valueOf(envelope.getResponse().toString());
            }  catch (Exception exception){
                response = false;
            }

            return response;
        }

        @Override
        protected void onPostExecute(Boolean success){
            super.onPostExecute(success);
            if(success){
            	if (MiParqueo.idEstado == 1) {
            		NuevoEstadoParqueo = 2;
				}else if (MiParqueo.idEstado == 2) {
            		NuevoEstadoParqueo = 3;
				}
            	new actualizarParqueo().execute();
            }
            else{
            	Toast.makeText(getApplicationContext(), "Error de inicio de sesión", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
