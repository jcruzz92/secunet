package com.example.secunet;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.widget.Toast;

public class AgregarActivity extends Activity {

    Intent intentInicio;
	TextView Username;
	TextView Password;
	TelephonyManager telephonyManager;
	String IdTelefono; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_agregar);
        Username = (TextView)findViewById(R.id.username);
        Password = (TextView)findViewById(R.id.password);
        intentInicio = new Intent(this, InicioActivity.class);

        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        IdTelefono = telephonyManager.getDeviceId();
	}

    public class registrarDispositivo extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids){
            Boolean response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_REGISTRARNUEVODISPOSITIVO);

            request.addProperty("Username", Username.getText().toString());
            request.addProperty("Clave", Password.getText().toString());
            request.addProperty("Imei", IdTelefono);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_REGISTRARNUEVODISPOSITIVO, envelope);
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
            	Toast.makeText(getApplicationContext(), "Registro del dispositivo completado!", Toast.LENGTH_LONG).show();
            	AgregarActivity.this.startActivity(intentInicio);
            	finish();
            }
            else{
            	Toast.makeText(getApplicationContext(), "Error de inicio de sesión", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
