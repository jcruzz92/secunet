package com.example.secunet;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.ListActivity;

/**
 * Created by Jorge Luis on 7/15/13.
 */
public class WS_Info {
    public static class GlobalParameters extends ListActivity{
    	public static Boolean HayLocales = false;
    	public static Boolean HayParqueos = false;
    	public static Boolean HayParqueoUnico = false;
    	public static Boolean HayParqueoUnicoAleatorio = false;

    	public static final String IP = "192.168.1.36";
//    	public static final String IP = "10.0.0.6";
//    	public static final String IP = "172.20.10.10";

        public static final String SOAP_ACTION_CHECKPARQUEADO = "http://proyecto.org/VerificarSiEstaParqueado";
        public static final String SOAP_ACTION_PARQUEOALEATORIO = "http://proyecto.org/CualquierParqueo";
        public static final String SOAP_ACTION_PARQUEOSLIBRES = "http://proyecto.org/ParqueosLibres";
        public static final String SOAP_ACTION_LOCALES = "http://proyecto.org/Locales";
        public static final String SOAP_ACTION_PARQUEOMASCERCANO = "http://proyecto.org/ParqueoMasCercano";
        public static final String SOAP_ACTION_SETPARQUEO = "http://proyecto.org/AsignarParqueo";
        public static final String SOAP_ACTION_BYMAC = "http://proyecto.org/ParqueoPorMAC";
        public static final String SOAP_ACTION_LIBERARPARQUEO = "http://proyecto.org/LiberarParqueo";
        public static final String SOAP_ACTION_PARQUEARSE = "http://proyecto.org/VerificarParquear";
        public static final String SOAP_ACTION_PARQUEOSPORPISO = "http://proyecto.org/ParqueosPorPiso";
        public static final String SOAP_ACTION_DESOCUPARPARQUEO = "http://proyecto.org/DesocuparParqueo";
        public static final String SOAP_ACTION_SALIRPARQUEO = "http://proyecto.org/SalirParqueo";
        public static final String SOAP_ACTION_ENTRARPARQUEO = "http://proyecto.org/EntrarParqueo";

        public static final String OPERATION_NAME_CHECKPARQUEADO = "VerificarSiEstaParqueado";
        public static final String OPERATION_NAME_PARQUEOALEATORIO = "CualquierParqueo";
        public static final String OPERATION_NAME_PARQUEOSLIBRES = "ParqueosLibres";
        public static final String OPERATION_NAME_PARQUEOMASCERCANO = "ParqueoMasCercano";
        public static final String OPERATION_NAME_LOCALES = "Locales";
        public static final String OPERATION_NAME_SETPARQUEO = "AsignarParqueo";
        public static final String OPERATION_NAME_BYMAC = "ParqueoPorMAC";
        public static final String OPERATION_NAME_LIBERARPARQUEO = "LiberarParqueo";
        public static final String OPERATION_NAME_PARQUEARSE = "VerificarParquear";
        public static final String OPERATION_NAME_PARQUEOSPORPISO = "ParqueosPorPiso";
        public static final String OPERATION_NAME_DESOCUPARPARQUEO = "DesocuparParqueo";
        public static final String OPERATION_NAME_SALIRPARQUEO = "SalirParqueo";
        public static final String OPERATION_NAME_ENTRARPARQUEO = "EntrarParqueo";

        public static final String WSDL_TARGET_NAMESPACE = "http://proyecto.org/";

        //public static final String SOAP_ADDRESS = "http://192.168.43.155/ProyectoWebService/WebServiceProyecto.asmx";
        public static final String SOAP_ADDRESS = "http://"+ IP +"/ProyectoWebService/WebServiceProyecto.asmx";

        static final String KEY_LOCAL = "Local"; // parent node
        static final String KEY_NOMRBEPISO = "NombrePiso";
        static final String KEY_CODIGO = "Codigo";
        static final String KEY_NOMBRE = "Nombre";
        static final String KEY_PISO = "Piso";

        static final String KEY_PARQUEO = "Parqueo"; // parent node
        static final String KEY_IDPARQUEO = "IdParqueo";
        static final String KEY_ESTADOPARQUEO = "Estado";
        static final String KEY_PISOPARQUEO = "Piso";
        static final String KEY_IDESTADOPARQUEO = "idEstado";
//        static final String KEY_LADOPARQUEO = "Lado";
//        static final String KEY_PESOPARQUEO = "Peso";

        public static ArrayList<Local> ParsearLocales(String XML){
            ArrayList<Local> Locales = new ArrayList<Local>();

            XMLParser parser = new XMLParser();
            Document doc = parser.getDomElement(XML); // getting DOM element
            NodeList nl = doc.getElementsByTagName(KEY_LOCAL);
            
            // looping through all item nodes <item>
            
            if (nl.getLength() > 0){
            	HayLocales = true;
        		for(int i = 0; i < nl.getLength(); i++) {
                    Locales.add(new Local());
	                Element e = (Element) nl.item(i);
	                // adding each child node to HashMap key => value
	                Locales.get(i).Codigo = parser.getValue(e, KEY_CODIGO);
	                Locales.get(i).Nombre = parser.getValue(e, KEY_NOMBRE);
	                Locales.get(i).Nivel = parser.getValue(e, KEY_PISO);
	                Locales.get(i).NombreNivel = parser.getValue(e, KEY_NOMRBEPISO);
	            }
            }else {
				HayLocales = false;
			}
            return Locales;
        }

        public static ArrayList<Parqueo> ParsearParqueos(String XML){
            ArrayList<Parqueo> Parqueos = new ArrayList<Parqueo>();

            XMLParser parser = new XMLParser();
            Document doc = parser.getDomElement(XML); // getting DOM element
            NodeList nl = doc.getElementsByTagName(KEY_PARQUEO);

            // looping through all item nodes <item>
            if (nl.getLength() > 0) {
            	HayParqueos = true;
				for(int i = 0; i < nl.getLength(); i++) {
		            Parqueos.add(new Parqueo());
	                Element e = (Element) nl.item(i);
	                // adding each child node to HashMap key => value
	                Parqueos.get(i).IdParqueo = parser.getValue(e, KEY_IDPARQUEO);
	                Parqueos.get(i).Estado = parser.getValue(e, KEY_ESTADOPARQUEO);
	                Parqueos.get(i).Piso = parser.getValue(e, KEY_PISOPARQUEO);
	                Parqueos.get(i).idEstado = Integer.parseInt(parser.getValue(e, KEY_IDESTADOPARQUEO));
	                //Parqueos.get(i).Peso = parser.getValue(e, KEY_PESOPARQUEO);
				}
			}
            else{
            	HayParqueos = false;
            }
            return Parqueos;
        }

        public static Parqueo ParsearParqueoUnico(String XML){
            Parqueo ParqueoCercano = new Parqueo();
            ParqueoCercano.IdParqueo = "~";

            XMLParser parser = new XMLParser();
            Document doc = parser.getDomElement(XML); // getting DOM element
            NodeList nl = doc.getElementsByTagName(KEY_PARQUEO);
            
            if (nl.getLength() > 0) {
            	HayParqueoUnico = true;
				Element e = (Element) nl.item(0);
	            ParqueoCercano.IdParqueo = parser.getValue(e, KEY_IDPARQUEO);
	            ParqueoCercano.Estado = parser.getValue(e, KEY_ESTADOPARQUEO);
	            ParqueoCercano.Piso = parser.getValue(e, KEY_PISOPARQUEO);
	            ParqueoCercano.idEstado = Integer.parseInt(parser.getValue(e, KEY_IDESTADOPARQUEO));
			}else {
				HayParqueoUnico = false;
			}
            
            //ParqueoCercano.Lado = parser.getValue(e, KEY_LADOPARQUEO);
            //ParqueoCercano.Peso = parser.getValue(e, KEY_PESOPARQUEO);

            return ParqueoCercano;
        }
        
        public static String bytesToHexString(byte[] src) {
            StringBuilder stringBuilder = new StringBuilder("0x");
            if (src == null || src.length <= 0) {
                return null;
            }

            char[] buffer = new char[2];
            for (int i = 0; i < src.length; i++) {
                buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);  
                buffer[1] = Character.forDigit(src[i] & 0x0F, 16);  
                System.out.println(buffer);
                stringBuilder.append(buffer);
            }

            return stringBuilder.toString();
        }
        
    }
}
