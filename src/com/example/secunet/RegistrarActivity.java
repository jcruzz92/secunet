package com.example.secunet;

import javax.xml.datatype.Duration;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.example.secunet.InicioActivity.verificarDispositivoRegistrado;

import android.R.layout;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class RegistrarActivity extends Activity {

    Intent intentInicio;
    Intent intentVincular;
	TextView Username;
	TextView Password;
	TextView Password2;
	TextView Correo;
	Button Registrar;
	Button IniciarSesion;
	TelephonyManager telephonyManager;
	String IdTelefono; 
	CheckBox AceptaTerminos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        Username = (TextView)findViewById(R.id.username);
        Password = (TextView)findViewById(R.id.password);
        Password2 = (TextView)findViewById(R.id.password2);
        Correo = (TextView)findViewById(R.id.correo);
        AceptaTerminos = (CheckBox) findViewById(R.id.acepta_terminos);
        intentInicio = new Intent(this, InicioActivity.class);
        intentVincular = new Intent(this, AgregarActivity.class);
        
        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        IdTelefono = telephonyManager.getDeviceId();

        Registrar = (Button)findViewById(R.id.registrar);
        IniciarSesion = (Button)findViewById(R.id.iniciar_sesion);
        
        Registrar.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v){
        		String Nombre = Username.getText().toString(),
        				Clave = Password.getText().toString(), 
        				Clave2 = Password2.getText().toString(), 
        				Mail = Correo.getText().toString();
                if (Nombre.length() == 0) {
					Toast.makeText(getApplicationContext(), "Ingresa un nombre de usuario", Toast.LENGTH_SHORT).show();
				}else if (Nombre.length() < 5) {
                	Toast.makeText(getApplicationContext(), "Nombre de usuario muy corto", Toast.LENGTH_SHORT).show();
				}else if (Nombre.length() > 28) {
					Toast.makeText(getApplicationContext(), "Nombre de usuario muy largo", Toast.LENGTH_SHORT).show();
				}else if (Clave.length() == 0) {
					Toast.makeText(getApplicationContext(), "Ingresa una contraseña", Toast.LENGTH_SHORT).show();
				}else if (Clave.length() < 5) {
                	Toast.makeText(getApplicationContext(), "La contraseña debe ser mayor de 4 caracteres", Toast.LENGTH_SHORT).show();
				}else if (!Clave.equals(Clave2)) {
					Toast.makeText(getApplicationContext(), "Las dos contraseñas deben iguales", Toast.LENGTH_SHORT).show();
				}else if (Mail.length() == 0) {
					Toast.makeText(getApplicationContext(), "Ingresa un correo electrónico", Toast.LENGTH_SHORT).show();
				}else if (!isEmailValid(Mail)) {
					Toast.makeText(getApplicationContext(), "Correo inválido", Toast.LENGTH_SHORT).show();
				}else if (!AceptaTerminos.isChecked()) {
					Toast.makeText(getApplicationContext(), "Debe aceptar los términos para registrarse", Toast.LENGTH_SHORT).show();
				}else {
					new registrarUsuario().execute();
				}
        	}
        });
        IniciarSesion.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v){
        		Toast.makeText(getApplicationContext(), "Abrir form Iniciar Sesión", Toast.LENGTH_SHORT).show();
        	}
        });
	}
	
    public class registrarUsuario extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids){
            Boolean response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_REGISTRARNUEVOUSUARIO);

            request.addProperty("Username", Username.getText().toString());
            request.addProperty("Clave", Password.getText().toString());
            request.addProperty("Correo", Correo.getText().toString());
            request.addProperty("Imei", IdTelefono);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_REGISTRARNUEVOUSUARIO, envelope);
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
            	Toast.makeText(getApplicationContext(), "Registro completado!", Toast.LENGTH_LONG).show();
            	RegistrarActivity.this.startActivity(intentInicio);
            	finish();
            }
            else{
            	Toast.makeText(getApplicationContext(), "Nombre de usuario no disponible.", Toast.LENGTH_SHORT).show();
            }
        }
    }
	
	public boolean isEmailValid(String email){
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}
}
