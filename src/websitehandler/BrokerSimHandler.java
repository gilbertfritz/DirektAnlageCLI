package websitehandler;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import tools.Serialize;
import depotagent.AktienDepot;
import depotagent.DepotAgentSimulation;
import depotagent.PairKassaDepot;

/*
 * Simuliert einen Broker, wie zb DirektAnlage
 */
public class BrokerSimHandler extends _ABSBrokerHandler {

	private final double auferlegungswert;
	
	public BrokerSimHandler( double auferlegungswert){
		this.auferlegungswert = auferlegungswert;
	}
	
	public AktienDepot getPredefinedDepot(){
		AktienDepot d = new AktienDepot( auferlegungswert);
		//Aktie a = new Aktie( ...)
		//d.addAktie( a);
		return d;
	}
	
	public AktienDepot getDepot() throws Exception{
		 AktienDepot d = Serialize.unserializeDepot( DepotAgentSimulation.SERIALIZE_MEINDEPOT, auferlegungswert);
		 return d;
	}

	@Override
	public HtmlPage login() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HtmlPage logout() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HtmlPage kaufen(String wkn, int stueckzahl, double stoppkurs,
			boolean isTest) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verkaufen(String wkn) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PairKassaDepot getKassaDepotWert() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
