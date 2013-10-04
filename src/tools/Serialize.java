package tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import depotagent.*;

public class Serialize
{
	//----------------------------
	//serialisiert Musterdepot
	public static void serializeDepot( AktienDepot d, String serializename)
	{
		try
	      {
	         FileOutputStream fileOut =
	         new FileOutputStream( serializename + ".ser");
	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject( d);
	         out.close();
	         fileOut.close();
	      }catch( IOException i)
	      {
	          i.printStackTrace();
	      }
	}
	
	//----------------------------
	//unserialisiert Musterdepot
	public static AktienDepot unserializeDepot( String serializename, double auferlegungsWert)
	{
		AktienDepot d = null;
        try
        {
           FileInputStream fileIn = new FileInputStream( serializename + ".ser");
           ObjectInputStream in = new ObjectInputStream(fileIn);
           d = (AktienDepot) in.readObject();
           in.close();
           fileIn.close();
       }catch(IOException i)
       {
    	   Logging.log( "Deserialiserung fehlgeschlagen: " + serializename + "; neues Depot erstellt.");
    	   AktienDepot ad = new AktienDepot( auferlegungsWert);
    	   ad.setKassaWert( auferlegungsWert);
    	   return ad;
       }catch(ClassNotFoundException c)
       {
           System.out.println("ZU DESERIALISIERENDE KLASSE NICHT GEFUNDEN!");
           c.printStackTrace();
       }
       
       
       return d;
	}
}
