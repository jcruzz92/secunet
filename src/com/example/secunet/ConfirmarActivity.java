package com.example.secunet;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.widget.TextView;

public class ConfirmarActivity extends Activity {
    NfcAdapter nfcAdapter;
    PendingIntent nfcPendingIntent;
    private Parqueo MiParqueo;
    private TextView LabelPark;
    private TextView LabelIndicaciones;
    private TextView TextoMiParqueo;
    Boolean Parqueado = false;
    AlertDialog.Builder builderDialogEstado2;
    AlertDialog.Builder builderDialogEstado1;
    AlertDialog dialogEstado2;
    AlertDialog dialogEstado1;
    AlertDialog.Builder builderDialogClave;
    AlertDialog dialogClave;
    AlertDialog.Builder builderDialogAlarma;
    AlertDialog dialogAlarma;
    Intent intentCheck;
    TelephonyManager telephonyManager;
	String IdTelefono; 
	Boolean HayAlerta;
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmar);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
       
        LabelPark = (TextView) findViewById(R.id.labelPrk);
        LabelIndicaciones = (TextView) findViewById(R.id.lbIndicaciones);
        TextoMiParqueo = (TextView) findViewById(R.id.lbMiParqueo);
        intentCheck = new Intent(this, CheckActivity.class);
        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        IdTelefono = telephonyManager.getDeviceId();
        HayAlerta = false;
        
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
        builderDialogClave.setCancelable(false);
    	builderDialogClave.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialogClave.dismiss();
				new actualizarParqueo().execute();
			}
		});
    	builderDialogClave.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialogClave.dismiss();
				dialogAlarma.show();
            	new generarAlarma().execute();
			}
		});
        
    	builderDialogEstado1 = new AlertDialog.Builder(this);
    	builderDialogEstado1.setMessage("Se ha ocupado tu parqueo sin autorización. ¿Has sido tú?").setTitle("Importante!");
    	builderDialogEstado1.setCancelable(false);
    	builderDialogEstado1.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.dismiss();
            	//TODO: Pedir clave.
        		dialogClave.show();
            }
        });
    	builderDialogEstado1.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.dismiss();
            	dialogAlarma.show();
            	new generarAlarma().execute();
            }
        });
    	
    	builderDialogEstado2 = new AlertDialog.Builder(this);
    	builderDialogEstado2.setMessage("Se ha desocupado tu parqueo sin aviso. ¿Has sido tú?").setTitle("Importante!");
    	builderDialogEstado2.setCancelable(false);
    	builderDialogEstado2.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.dismiss();
        		dialogClave.show();
            }
        });
    	builderDialogEstado2.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.dismiss();
            	dialogAlarma.show();
            	new generarAlarma().execute();
            }
        });
        
    	dialogAlarma = builderDialogAlarma.create();
		dialogClave = builderDialogClave.create();
		dialogEstado1 = builderDialogEstado1.create();
		dialogEstado2 = builderDialogEstado2.create();
        
        MiParqueo = new Parqueo();
        
        new buscarParqueoAsignado().execute();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.check, menu);
        return true;
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
            if (MiParqueo.Notificacion) {
                if (MiParqueo.idEstado == 1) { //asignado
                	Parqueado = false;
                	LabelPark.setText("Tu Parqueo Asignado es:");
                	LabelIndicaciones.setText("Cuando llegues a tu parqueo, acerca tu dispositivo al panel indicado para confirmar que te has parqueado.");
                	dialogEstado1.show(); //se ocupo sin avisar
                }else if (MiParqueo.idEstado == 2) { //parqueado
                	Parqueado = true;
                	LabelPark.setText("Estás Parqueado en:");
                	LabelIndicaciones.setText("Antes de retirarte, acerca tu dispositivo nuevamente al panel indicado. Con esto liberarás el parqueo y otros podrán usarlo.");
                    dialogEstado2.show(); //se libero sin avisar
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
			}else {
				ConfirmarActivity.this.startActivity(intentCheck);
				finish(); 
			}
            TextoMiParqueo.setText("Parqueo " + MiParqueo.IdParqueo + ", " + MiParqueo.Piso);
        }
    }

    public class generarAlarma extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids){
            String response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_CREARALERTA);

            if (MiParqueo.idEstado == 1) {
            	request.addProperty("Descripcion", "Se ha ocupado el parqueo " + MiParqueo.IdParqueo + " sin autorización, atender cuanto antes.");
                request.addProperty("idParqueo", MiParqueo.IdParqueo);
			}else if (MiParqueo.idEstado == 2) {
				request.addProperty("Descripcion", "Se ha liberado el parqueo " + MiParqueo.IdParqueo + " sin autorización, atender cuanto antes.");
	            request.addProperty("idParqueo", MiParqueo.IdParqueo);	
			}

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
            new actualizarParqueo().execute();
            HayAlerta = true;
        }
    }

    public class actualizarParqueo extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids){
            String response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_CAMBIARESTADOPARQUEO);

            request.addProperty("idParqueo", MiParqueo.IdParqueo);
            if (HayAlerta) {
            	if (MiParqueo.idEstado == 1) {
                	request.addProperty("idEstado", "5");
    			}else if (MiParqueo.idEstado == 2) {
    				request.addProperty("idEstado", "6");
    			}
			}else {
				if (MiParqueo.idEstado == 1) {
                	request.addProperty("idEstado", "2");
    			}else if (MiParqueo.idEstado == 2) {
    				request.addProperty("idEstado", "3");
    			}
			}

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
            HayAlerta = false;
			ConfirmarActivity.this.startActivity(intentCheck);
			finish(); 
        }
    }
    
    public void suscribe(String idParqueo){
    	if (!idParqueo.equals("c-1")) {
           	Parse.initialize(this, "NJE50gi9UOxCggYxSO2gVFyMkNVQy0w14mZNdcFI", "iMZgZ2mzfCJMw8wlyuhqNy89gDFkf6KVtqmyaCgF"); 
            PushService.subscribe(this, idParqueo, ConfirmarActivity.class);
            ParseInstallation.getCurrentInstallation().saveInBackground();
		}
    }
}
