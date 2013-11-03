package com.example.secunet;

import java.util.ArrayList;
import java.util.Locale;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Text;





import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ParqueoInterfaceActivity extends Activity implements View.OnClickListener, TextToSpeech.OnInitListener{
ImageButton Parqueo1;
ImageButton Parqueo2;
ImageButton Parqueo3;
ImageButton Parqueo4;
TextView txtImage;
Intent intent;
Parqueo ParqueoManual;
public Parqueo ParqueoUno;
public Parqueo ParqueoDos;
public Parqueo ParqueoTres;
public Parqueo ParqueoCuatro;
private String MacAddress;
Parqueo park;
int numero;
private int IdEstado;
private TextToSpeech myTTS;
private int MY_DATA_CHECK_CODE = 0;

ArrayList<Parqueo> ParqueosManual;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parqueoslibresinterface);
		ArrayList<String> Parqueos = new ArrayList<String>();
		Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
		Parqueo1 = (ImageButton)findViewById(R.id.spot1);
		Parqueo1.setTag(R.drawable.available);
		Parqueo2 = (ImageButton)findViewById(R.id.spot2);
		Parqueo3 = (ImageButton)findViewById(R.id.spot3);
		Parqueo4 = (ImageButton)findViewById(R.id.spot4);
		txtImage = (TextView)findViewById(R.id.lbIndicaciones);
		ParqueosManual = new ArrayList<Parqueo>();
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		intent = new Intent(ParqueoInterfaceActivity.this, CheckActivity.class);
		MacAddress = getMacAddress();
		 ParqueoManual = new Parqueo();
		//txtImage = (TextView)findViewById(R.id.txtImage);
		//txtImage.setText(Parqueo1.getTag().toString());
		new buscarParqueosPorPiso().execute();
		
		
		Parqueo1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ParqueoManual = ParqueoUno;
				((ImageButton) v).setImageResource(R.drawable.occupied1);
				builder.setMessage("Desea este parqueo?");
                MacAddress = getMacAddress();
                builder .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                ParqueoInterfaceActivity.this.startActivity(intent);
                                finish();
                                String words;
                                new asignarParqueo().execute(false);
                                
                                    words = "Dirijase al " + ParqueoUno.Piso + ", parqueo " + ParqueoManual.IdParqueo.toString() ;
                                
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

		Parqueo2.setOnClickListener(new OnClickListener() {
			
			@Override
			
			public void onClick(View v) {
				ParqueoManual = ParqueoDos;
				// TODO Auto-generated method stub
				((ImageButton) v).setImageResource(R.drawable.occupied2);
				builder.setMessage("Desea este parqueo?");
                MacAddress = getMacAddress();
                builder .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                ParqueoInterfaceActivity.this.startActivity(intent);
                                finish();
                                String words;
                                new asignarParqueo().execute(false);
                                
                                    words = "Dirijase al " + ParqueoManual.Piso + ", parqueo " + ParqueoManual.IdParqueo.toString() ;
                                
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

		Parqueo3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ParqueoManual = ParqueoTres;
				((ImageButton) v).setImageResource(R.drawable.occupied3);
				builder.setMessage("Desea este parqueo?");
                MacAddress = getMacAddress();
                builder .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                ParqueoInterfaceActivity.this.startActivity(intent);
                                finish();
                                String words;
                                new asignarParqueo().execute(false);
                                
                                    words = "Dirijase al " + ParqueoManual.Piso + ", parqueo " + ParqueoManual.IdParqueo.toString() ;
                                
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

		Parqueo4.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ParqueoManual = ParqueoCuatro;
				// TODO Auto-generated method stub
				((ImageButton) v).setImageResource(R.drawable.occupied4);
				builder.setMessage("Desea este parqueo?");
                MacAddress = getMacAddress();
                builder .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                ParqueoInterfaceActivity.this.startActivity(intent);
                                finish();
                                String words;
                                new asignarParqueo().execute(false);
                                
                                    words = "Dirijase al " + ParqueoManual.Piso + ", parqueo " + ParqueoManual.IdParqueo.toString() ;
                                
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
	   
	   
	
	public class buscarParqueosPorPiso extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            String response;

            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_PARQUEOSPORPISO);

            request.addProperty("idPiso", 1);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_Info.GlobalParameters.SOAP_ADDRESS);

            try {
                httpTransport.debug = true;
                httpTransport.call(WS_Info.GlobalParameters.SOAP_ACTION_PARQUEOSPORPISO, envelope);
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
            ParqueoUno = ParqueosManual.get(0);
            ParqueoDos = ParqueosManual.get(1);
            ParqueoTres = ParqueosManual.get(2);
            ParqueoCuatro = ParqueosManual.get(3);
          
           
    			if(ParqueoUno.Estado.trim().equals("Libre")){
    				Parqueo1.setImageResource(R.drawable.available1);
    			}
    			if(ParqueoDos.Estado.trim().equals("Libre")){
    				Parqueo2.setImageResource(R.drawable.available2);
    			}
    			if(ParqueoTres.Estado.trim().equals("Libre")){
    				Parqueo3.setImageResource(R.drawable.available3);
    			}
    			if(ParqueoCuatro.Estado.trim().equals("Libre")){
    				Parqueo4.setImageResource(R.drawable.available4);
    			}
    		
            
        }
    }
	
	
	public class asignarParqueo extends AsyncTask<Boolean, Void, String> {
        @Override
        protected String doInBackground(Boolean... cualquiera) {
            String response = null;
            SoapObject request = new SoapObject(WS_Info.GlobalParameters.WSDL_TARGET_NAMESPACE, WS_Info.GlobalParameters.OPERATION_NAME_SETPARQUEO);
            String Parqueo;
            
            IdEstado = 1;

            if (cualquiera[0]){
                
            }
            else{
                    request.addProperty("idParqueo", ParqueoManual.IdParqueo);
                
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

        }
    }


	@Override
	public void onInit(int initStatus) {
		// TODO Auto-generated method stub
		if (initStatus == TextToSpeech.SUCCESS){
            myTTS.setLanguage(new Locale("spa", "ESP"));
        }
        else if (initStatus == TextToSpeech.ERROR){
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}


}
