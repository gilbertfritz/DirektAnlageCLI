package websitehandler;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;

import tools.*;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;

import depotagent.*;


public class DerAktionaerMDHandler implements _IFMDHandler{

	private final double auferlegungsWert;
	
	public DerAktionaerMDHandler( boolean debug, double auferlegungsWert) throws Exception{
		this.auferlegungsWert = auferlegungsWert;
	}
	
	private HtmlPage getDepotPage() throws Exception {
		
		final WebClient webClient = new WebClient( BrowserVersion.INTERNET_EXPLORER_8);
		webClient.setJavaScriptEnabled( false);
		webClient.setCssEnabled( false);
		
		//load the page	
		final HtmlPage page = webClient
			.getPage( "http://www.deraktionaer.de/xist4c/web/Online-Real-Depot_id_2882_.htm");

		//locally
	//	final HtmlPage page = webClient
	//	.getPage( "file:///mnt/c/Programme/Java/Workspace/MusterDepotAgent/test.htm");
		
		return page;
	}

	public AktienDepot extractDepot() throws Exception {
		HtmlPage depotPage = getDepotPage();
		HtmlTable depotTable = (HtmlTable)depotPage.getByXPath( "//table[@class='emDepotDetailDataTable']").get(0);
		ArrayList<HtmlTableRow> depotTr = (ArrayList<HtmlTableRow>)depotTable.getByXPath( "./tbody/tr");
		
		AktienDepot d = new AktienDepot( auferlegungsWert);
		for( int i = 1; i < depotTr.size()-5; i++){
			ArrayList<HtmlTableCell> aktienTc = (ArrayList<HtmlTableCell>)depotTr.get( i).getByXPath( "./td");
			String stoppkurs = aktienTc.get( 7).asText();
			double sk = 0.0;
			if( !stoppkurs.isEmpty()){
				sk = StringConvert.euroStrToDouble( stoppkurs);
			}
			
			Aktie a = new Aktie(
					StringConvert.stringWithCommaToInt( aktienTc.get( 0).asText()), //stueckzahl
					aktienTc.get( 2).asText(), //name
					aktienTc.get( 3).asText(), //wkn
					StringConvert.stringToDate( aktienTc.get( 4).asText(), "dd.MM.yyyy"),//kaufdatum
					StringConvert.euroStrToDouble( aktienTc.get( 5).asText()), //kaufkurs
					StringConvert.euroStrToDouble( aktienTc.get( 6).asText()), //aktuellerkurs
					sk ); //stoppkurs
			
			if( a.getStueckZahl() != -1)
				d.addAktie( a);
		}

		//extract kassawert
		d.setKassaWert(	StringConvert.stringWithCommaPointToDouble(
				((HtmlElement)depotTr.get( depotTr.size()-4).getByXPath( "./td").get( 2)).asText()));
		return d;
	}

}
