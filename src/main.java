import java.util.Date;
import java.util.Stack;

import javax.sql.rowset.serial.SerialArray;

import GUI.ConsoleGUI;

import depotagent.*;

import tools.Logging;
import tools.Security;
import tools.Serialize;
import websitehandler.*;


public class main {
	public static void main( String args[]) throws Exception{

		//boolean isTest = true; //!!!!!!!!!!!!

		DepotAgentSimulation depotAgentSim = null;
		try{
			depotAgentSim = new DepotAgentSimulation( true);
		}catch( Exception e){
			e.printStackTrace();
			Logging.log( e.getMessage());
			return;
		}
		
		Logging.log( "Starte Simulation...");

		depotAgentSim.simuliere();
	
		
	/*
		AktienDepot d = Serialize.unserializeDepot( "test", 10000);
		System.out.println( d);
		
		AktienDepot ad = new AktienDepot( 10000);
		Aktie a = new Aktie( 29, "seas", "123", new Date(), 1.2, 1.2, 1.2);
		ad.addAktie( a);
		Serialize.serializeDepot( ad, "test");
	*/	
		

		
	/*
		DirektAnlageHandler dianh = new DirektAnlageHandler( true, "hello");
		System.out.println( dianh.loginOnly( true));
		System.out.println( "---------------------------------------------- KAUFEN --------");
		System.out.println( dianh.kaufenOnly( "SKYD00", 2, 1.0, _ABSBrokerHandler.BoersenPlatz.XETRA_FANKFURT, isTest).asText());
		System.out.println( "---------------------------------------------- LOGOUT --------");
		System.out.println( dianh.logoutOnly());
	*/
	}
}

	