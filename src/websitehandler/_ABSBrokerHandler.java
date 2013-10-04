package websitehandler;

import java.util.ArrayList;
import java.util.LinkedList;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import depotagent.Aktie;
import depotagent.AktienDepot;
import depotagent.PairKassaDepot;

public abstract class _ABSBrokerHandler {

	//ACHTUNG: die angegebene Boersen muessen alle "Market", "Stop Market" und "Ultimo laufende Woche" unterstuetzen
	public static enum BoersenPlatz{ WIEN_XETRA, XETRA_FANKFURT, FRANKFURT, HAMBURG, MUENCHEN};
	protected LinkedList<BoersenPlatz> boersenPriori;
	
	public abstract HtmlPage login() throws Exception;
	public abstract HtmlPage logout() throws Exception;
	public abstract AktienDepot getDepot() throws Exception;
	public abstract HtmlPage kaufen( String wkn, int stueckzahl, double stoppkurs, boolean isTest) throws Exception;
	public abstract boolean verkaufen( String wkn);
	public abstract PairKassaDepot getKassaDepotWert() throws Exception;
	
	public _ABSBrokerHandler(){
		boersenPriori = new LinkedList<BoersenPlatz>();
		setPriori();
	}
	
	/**
	 * beim kauf wird versucht an der boerse mit hoechster priotiaet zu kaufen. 
	 */
	private void setPriori(){
		boersenPriori.add( BoersenPlatz.WIEN_XETRA);
		boersenPriori.add( BoersenPlatz.XETRA_FANKFURT);
		boersenPriori.add( BoersenPlatz.FRANKFURT);
		boersenPriori.add( BoersenPlatz.HAMBURG);
		boersenPriori.add( BoersenPlatz.MUENCHEN);
	}

}
