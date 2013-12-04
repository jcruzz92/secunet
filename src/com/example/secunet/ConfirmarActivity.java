package com.example.secunet;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.widget.TextView;

public class ConfirmarActivity extends Activity {
    private Parqueo MiParqueo;
    private TextView LabelPark;
    private TextView LabelIndicaciones;
    private TextView TextoMiParqueo;
    Boolean Parqueado = false;
    AlertDialog.Builder builderDialog;
    AlertDialog dialog;
    AlertDialog.Builder builderDialogClave;
    AlertDialog dialogClave;
    AlertDialog.Builder builderDialogAlarma;
    AlertDialog dialogAlarma;
    Intent intentCheck;
    TelephonyManager telephonyManager;
	String IdTelefono; 
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmar);

        LabelPark = (TextView) findViewById(R.id.labelPrk);
        LabelIndicaciones = (TextView) findViewById(R.id.lbIndicaciones);
        TextoMiParqueo = (TextView) findViewById(R.id.lbMiParqueo);
        intentCheck = new Intent(this, CheckActivity.class);
        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        IdTelefono = telephonyManager.getDeviceId();
        
        builderDialogAlarma = new AlertDialog.Builder(this);
        builderDialogAlarma.setMessage("Se ha notificado al departamento de seguridad");
    	builderDialogAlarma.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				ConfirmarActivity.this.startActivity(intentCheck);
				finish(); 
			}
		});

    	builderDialogClave = new AlertDialog.Builder(this);
        builderDialogClave.setMessage("Ingresa tus credenciales").setTitle("Inicio de sesión");
    	builderDialogClave.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialogClave.dismiss();
			}
		});
    	builderDialogClave.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialogClave.dismiss();
				dialogAlarma.show();
            	new generarAlarma().execute();
			}
		});
        
        builderDialog = new AlertDialog.Builder(this);
        builderDialog.setMessage("Se ha desocupado tu parqueo sin aviso. ¿Has sido tú?").setTitle("Importante!");
        builderDialog.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.dismiss();
        		dialogClave.show();
            }
        });
        builderDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.dismiss();
            	dialogAlarma.show();
            	new generarAlarma().execute();
            }
        });
        
    	dialogAlarma = builderDialogAlarma.create();
		dialogClave = builderDialogClave.create();
        dialog = builderDialog.create();
        
        MiParqueo = new Parqueo();
        
        new buscarParqueoAsignado().execute();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.check, menu);
        return true;
    }
    
//    public String getMacAddress() {
//        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        WifiInfo info = manager.getConnectionInfo();
//        return info.getMacAddress();
//    }
    
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
            if (MiParqueo.idEstado == 1) {//asignado
            	Parqueado = false;
            	LabelPark.setText("Tu Parqueo Asignado es:");
            	LabelIndicaciones.setText("Cuando llegues a tu parqueo, acerca tu dispositivo al panel indicado para registrar que te has parqueado y listo.");
			}else if (MiParqueo.idEstado == 2) { //parqueado
            	Parqueado = true;
            	LabelPark.setText("Estás Parqueado en:");
            	LabelIndicaciones.setText("Antes de retirarte, acerca tu dispositivo nuevamente al panel indicado. Con esto liberarás el parqueo y otros podrán usarlo.");
			}else if (MiParqueo.idEstado == 3) {//liberado pero sigue asignado a ti
            	Parqueado = false;
            	LabelPark.setText("Liberaste el Parqueo:");
            	LabelIndicaciones.setText("Dirígete a la salida más cercaca...");
			}else if (MiParqueo.idEstado == 5) {//ocupado sin confirmar usuario
            	Parqueado = false;
            	LabelPark.setText("Tu parqueo se ha ocupado sin autorización");
            	LabelIndicaciones.setText("Se ha notificado al departamento de seguridad...");
			}else if (MiParqueo.idEstado == 6) {//liberado sin autoriacion
            	Parqueado = false;
            	LabelPark.setText("Se ha desocupado tu parqueo sin autorización, se ha notificado al departamento de seguridad...");
            	LabelIndicaciones.setText("Dirígete a la salida más cercaca...");
			}
            TextoMiParqueo.setText("Parqueo " + MiParqueo.IdParqueo + ", " + MiParqueo.Piso);
            dialog.show();
        }
    }

    public class generarAlarma extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids){
            String response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_CREARALERTA);

            request.addProperty("Descripcion", "El parqueo " + MiParqueo.IdParqueo + " ha generado una alarma, atender cuanto antes!");
            request.addProperty("idParqueo", MiParqueo.IdParqueo);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.debug = true;
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_CREARALERTA, envelope);
                response = httpTransport.responseDump;
            }  catch (Exception exception)   {
                response = envelope.bodyIn.toString();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            
        }
    }

}
