package depotagent;

import java.util.ArrayList;
import java.util.Date;

import GUI.ConsoleGUI;

import tools.Logging;
import tools.Serialize;
import websitehandler.DerAktionaerMDHandler;
import websitehandler.DirektAnlageHandler;
import websitehandler.BrokerSimHandler;
import websitehandler.*;

public class DepotAgentSimulation {

	private class Order extends Aktie{//nur fuer simulation: in wirklichkeit macht das direktanlage
		public long timeInMillis;
		public double erteilungsKurs;
		Order( Aktie a, long timeInMillis){
			super( a);
			this.timeInMillis = timeInMillis;
			this.erteilungsKurs = a.getActualRate();
		}
	}
	
	public static final String SERIALIZE_MEINDEPOT = "simulate_eigenlokal";
	public static final int KAUFEN = 0;
	public static final int VERKAUFEN = 1;

	private final long ORDERTIME = 1000*60*12;
	private final long CHECKMDINTERVALL = 1000*60*2;
	private final long CHECKORDERINTERVALL = 1000*60;
	private final long WEBSITEUPDATEINTERVALL = 1000*60*5;

	
	private final double AUFERLEGUNGSWERT_AKTIONAER = 20000.0;
	private final double AUFERLEGUNGSWERT_EIGEN = 10000.0;
	
	private final boolean debug;
	
	public AktienDepot depotOnlAktionaer;
	public AktienDepot depotOnlEigen;
	public AktienDepot depotLokAktionaer;
	public AktienDepot depotLokEigen;
	
	public AktienListe orderKaufen;
	public AktienListe orderVerkaufen;
	public AktienListe orderErteilenKaufen;
	public AktienListe orderErteilenVerkaufen;
	public SperrListe sperrliste;

	private DerAktionaerMDHandler handlerAktionaer;
	private BrokerSimHandler brokerSimHandler;
	
	private ConsoleGUI gui;

	public DepotAgentSimulation( boolean debug) throws Exception{

		this.debug = debug;
		sperrliste = new SperrListe( "sperrliste.txt");
		orderKaufen = new AktienListe();
		orderVerkaufen = new AktienListe();
		orderErteilenKaufen = new AktienListe();
		orderErteilenVerkaufen = new AktienListe();
		depotLokAktionaer = new AktienDepot( AUFERLEGUNGSWERT_AKTIONAER);
		depotLokAktionaer.setKassaWert( AUFERLEGUNGSWERT_AKTIONAER);
		
		handlerAktionaer = new DerAktionaerMDHandler( debug, AUFERLEGUNGSWERT_AKTIONAER);
		brokerSimHandler = new BrokerSimHandler( AUFERLEGUNGSWERT_EIGEN);
		gui = new ConsoleGUI( this, 107, 59);
		//gui = new ConsoleGUI( this, 107, 65);
		
		//hole eigenes depot
		try{
			depotLokEigen = brokerSimHandler.getDepot();
		}catch( Exception e){
			throw new Exception( "Fehler: Eigenes Depot konnte nicht erreicht werden");
		}
	}

	
	public void simuliere(){
		final long SLEEPTIME = 5000;
		long startTime = System.currentTimeMillis();
		boolean firsttime = true;
		
		int count = 0;
		while( true){
			boolean print = false;
			
			if( !firsttime){
				try{Thread.sleep( SLEEPTIME);}//schlafe 5 sekunden
				catch( Exception e){};
			}
			
			long currentTime = System.currentTimeMillis();
			//--- MD PRUEFEN UND ORDERS AUFGEBEN --
			if( ( currentTime - startTime)%CHECKMDINTERVALL > CHECKMDINTERVALL - SLEEPTIME || firsttime){
				count++;
				checkMusterDepot( count);
				print = true;
			}
			firsttime = false;
			//--- ORDERS AUFGEBEN ---
			if( orderErteilenKaufen.size() > 0){
				kaufOrderErteilen();
				print = true;
			}
			if( orderErteilenVerkaufen.size() > 0){
				verkaufOrderErteilen( orderErteilenVerkaufen);
				print = true;
			}
			//--- ORDERS AUSFUEHREN ---
			if( ( currentTime - startTime)%CHECKORDERINTERVALL > CHECKORDERINTERVALL - SLEEPTIME){
				checkOrders();
				print = true;
			}

			if( print){
				//--- WEBSITE UPDATEN ---
				if( ( currentTime - startTime)%WEBSITEUPDATEINTERVALL > WEBSITEUPDATEINTERVALL - SLEEPTIME){
					System.out.println( gui.refresh( true));
				}
				else{
					System.out.println( gui.refresh( false));
				}
			}
		}//while
	}
	
	private void checkMusterDepot( int count){
		try{
			depotOnlAktionaer = handlerAktionaer.extractDepot();
		}catch( Exception e){
			Logging.log( "Fehler beim parsen des Musterdepots", gui);
			return;
		}
		if( depotOnlAktionaer.size() == 0) return;

		AktienListe[] diffMDLokOnl =
			depotLokAktionaer.vergleicheDepotOrderlists( depotOnlAktionaer, orderKaufen, orderVerkaufen, orderErteilenKaufen);
		//egal ob aenderung, lokales musterdepot updaten (kurse, stoppkurse)
		depotLokAktionaer = new AktienDepot( depotOnlAktionaer);
		
		//kurse des eigenen depots auf neuesten standbringen
		depotLokAktionaer.updateRatesFromYahoo( 4000);
		depotLokEigen.synchKurse( depotLokAktionaer);

		//wenn keine aenderung continue
		if( diffMDLokOnl[KAUFEN].size() == 0 && diffMDLokOnl[VERKAUFEN].size() == 0){
			Logging.log( "keine veraenderung");
			return; 
		}
		Logging.log( "Veraenderung festgestellt.", gui);
		if( diffMDLokOnl[KAUFEN].size() != 0) Logging.log( "NEU:\n" + diffMDLokOnl[KAUFEN]);
		if( diffMDLokOnl[VERKAUFEN].size() != 0) Logging.log( "NICHT MEHR:\n" + diffMDLokOnl[VERKAUFEN]);
		Logging.log( "Musterdepot aktuell:\n" + depotOnlAktionaer);
		
		//lokales MD mit eigenem Depot vergleichen( sperrliste wird berueckstichtigt!)
		AktienListe[] diffMDEigen =
			depotLokEigen.vergleicheDepot( depotLokAktionaer, sperrliste);
		//schaun ob veraenderungen bereits in den orders sind
		orderErteilenKaufen =
			diffMDEigen[KAUFEN].vergleicheDepot( orderKaufen)[1];
		orderErteilenVerkaufen =
			diffMDEigen[VERKAUFEN].vergleicheDepot( orderVerkaufen)[1];
	
		if( orderErteilenKaufen.size() == 0 && orderErteilenVerkaufen.size() == 0){
			Logging.log( "alle Aenderungen sind gesperrt oder bereits vorhanden.", gui);
			return;
		}
		if( orderErteilenKaufen.size() != 0) Logging.log( "Neue Kauforders werden aufgegeben.", gui);
		if( orderErteilenVerkaufen.size() != 0) Logging.log( "Neue Verkauforders werden aufgegeben.", gui);
	}

	//order ausfuehren wenn 15 min von der auftragerteilung vergangen sind
	private void checkOrders(){
		long currentTime = System.currentTimeMillis();
		//kauforders ausfuehren
		ArrayList<Aktie> lCopy = (ArrayList<Aktie>)orderKaufen.getAktienList().clone();
		for( int i = 0; i < lCopy.size(); i++){
			Order o = (Order)orderKaufen.getAktie( lCopy.get( i).getWkn());
			if( currentTime - o.timeInMillis > ORDERTIME){
				double k = 0.0;
				try{
					k = YahooHandler.getActualRate( o.getWkn(), 10000);
				}catch( Exception e){
					k = o.getActualRate();
				}finally{
					final double SPESEN = 7.95 + ( o.getStueckZahl() * k * 0.00195);
					o.setAktuellerKurs( k);
					o.setKaufKurs( k);
					depotLokEigen.setKassaWert( depotLokEigen.getKassaWert() - o.getStueckZahl() * k - SPESEN);
				}
				depotLokEigen.addAktie( new Aktie( o));
				orderKaufen.removeAktie( o.getWkn());

				Logging.log( "Kauforder ausgefuehrt: " + o.getStueckZahl() + " " + o.getWkn() + " im Wert von " + o.getStueckZahl()*o.getActualRate()
						+ "; SPESEN: " + (7.95 + ( o.getStueckZahl() * o.getActualRate() * 0.00195)), gui);
				Logging.log( depotLokEigen.toString());
				
				//nur fuer simulation: in echt wird das depot von direktanlage geholt
				try{
					Serialize.serializeDepot( depotLokEigen, this.SERIALIZE_MEINDEPOT);
				}catch( Exception e){
					e.printStackTrace();
					Logging.log( e.getMessage());
				}
			}
		}
		//verkauforder ausfuehren
		for( int i = 0; i < orderVerkaufen.size(); i++){
			Order o = (Order)orderVerkaufen.getAktie( i);
			if( currentTime - o.timeInMillis > ORDERTIME){
				Aktie aEigen = depotLokEigen.getAktie( o.getWkn()); 
				double k = 0;
				try{
					k = YahooHandler.getActualRate( aEigen.getWkn(), 10000);
				}catch( Exception e){
					k = aEigen.getActualRate();
				}finally{
					depotLokEigen.setKassaWert( depotLokEigen.getKassaWert() + aEigen.getStueckZahl() * k);
				}

				depotLokEigen.setKassaWert( depotLokEigen.getKassaWert() - 7.95);
				depotLokEigen.removeAktie( aEigen.getWkn());
				orderVerkaufen.removeAktie( aEigen.getWkn());
				sperrliste.writeAktienListeToFile();
				Logging.log( "Verkaufsorder wurde ausgefuehrt: " + aEigen.getWkn() + "; SPESEN: 7.95", gui);

				//nur fuer simulation: in echt wird das depot von direktanlage geholt
				try{
					Serialize.serializeDepot( depotLokEigen, this.SERIALIZE_MEINDEPOT);
				}catch( Exception e){
					Logging.log( e.getMessage());
				}
			}
		}
	}

	//verkauforder erteilen (teilverkaeufe werden NICHT beruecksichtigt!!!)
	private void verkaufOrderErteilen( AktienListe orderErteilenVerkaufen){
		ArrayList<Aktie> orderErteilenVerkaufenCopy = (ArrayList<Aktie>)orderErteilenVerkaufen.getAktienList().clone();
		for( Aktie a : orderErteilenVerkaufenCopy){
			this.orderVerkaufen.addAktie( new Order( new Aktie( a), System.currentTimeMillis()));
			this.orderErteilenVerkaufen.removeAktie( a.getWkn());
			Logging.log( "Verkauforder erteilt: " + a.getWkn() + "; jetziger Kurs: " + a.getActualRate(), gui);
			
			sperrliste.addAktie( a);
		}
	}
	
	//kauforder erteilen
	private void kaufOrderErteilen(){
		ArrayList<Aktie> orderErteilenKaufenCopy = (ArrayList<Aktie>)orderErteilenKaufen.getAktienList().clone();
		for( Aktie a : orderErteilenKaufenCopy){
			int stueckzahl = berechneEigenStkKaufen( a.getStueckZahl());
			orderErteilenKaufen.getAktienList().remove( 0);
			a.setStueckZahl( stueckzahl);
			a.setKaufDatum( new Date());
			a.setKaufKurs( 0.0);
			orderKaufen.aktienList.add( new Order( new Aktie( a), System.currentTimeMillis()));
			
			Logging.log( "Kauforder erteilt: " + stueckzahl + " " + a.getWkn() + "; jetziger kurs: " + a.getActualRate(), gui);
		}
	}

	protected int berechneEigenStkKaufen( int MDStk){
		//berechne den wert der aktien im musterdepot, die eigen noch nicht hat
		AktienListe al = depotLokEigen.vergleicheDepot( depotLokAktionaer)[0];
		double wertEigenHatNicht = al.getDepotWert();
		
		//virtuelle kassa des musterdepots
		double vMD = depotLokAktionaer.getKassaWert() + wertEigenHatNicht;
		
		return (int)((double)MDStk*( depotLokEigen.getKassaWert()/vMD));
	}
}

