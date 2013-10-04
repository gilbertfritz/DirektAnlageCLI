package websitehandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.views.AbstractView;

import tools.Security;
import tools.StringConvert;


import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLSelectElement;

import depotagent.AktienDepot;
import depotagent.PairKassaDepot;
import exceptions.InvalidBuyOrderException;
import exceptions.InvalidLoginException;
import exceptions.InvalidTANException;

public class DirektAnlageHandler extends _ABSBrokerHandler{

	private final WebClient webClient;
	private final String dataFilename = "da.enc";
	private final String dataPassword;
	
	public DirektAnlageHandler( boolean debug, String pswd) {
		webClient = new WebClient( BrowserVersion.INTERNET_EXPLORER_8);
		webClient.setJavaScriptEnabled( false);
		webClient.setCssEnabled( false);

		dataPassword = pswd;
	}

	public HtmlPage login() throws Exception {
		Security s = Security.getInstance( dataFilename, dataPassword);
		final String DEPOTNR = s.getDepotNr();
		final String PIN = s.getPIN();
		final String ID = s.getIdentifier();
		
		//load the page	
		final HtmlPage page = webClient
			.getPage( "https://internettrader.direktanlage.at/htmltrader");
	
		// Get the form that we are dealing with and within that form, 
	    // find the submit button and the field that we want to change.
	    final HtmlForm form = page.getFormByName("admlogon_customer");
	    final HtmlImageInput button = form.getInputByName("submitButton");
	    final HtmlTextInput textFieldDNr = form.getInputByName("customerNo");
	    final HtmlPasswordInput textFieldPin = form.getInputByName("pin");
	    final HtmlPasswordInput textFieldId = form.getInputByName("identifier");

	    // Change the value of the text field
	    textFieldDNr.setValueAttribute( DEPOTNR);
	    textFieldPin.setValueAttribute( PIN);
	    textFieldId.setValueAttribute( ID);

	    // Now submit the form by clicking the button and get back the second page.
	    final HtmlPage page2 = (HtmlPage) button.click();
	    if( page2.asText().contains( "Fehler"))
	    	throw new InvalidLoginException();
	    	
		return page2;
	}
	
	public HtmlPage kaufen( String wkn, int stueckzahl, double stoppkurs, boolean isTest) throws Exception{
		
		HtmlPage page = webClient
		.getPage( "https://internettrader.direktanlage.at/htmltrader/orderbuysecuritylist.do?source=menu");
		
		HtmlForm form = page.getFormByName( "orderbuy");
		final HtmlTextInput wknField = form.getInputByName( "securityFilter");
		HtmlImageInput button = form.getInputByName( "refreshButton");
		wknField.setValueAttribute( wkn);
		page = (HtmlPage)button.click();
		 //risikohinweis zustimmen
		page = webClient
		.getPage( "https://internettrader.direktanlage.at/htmltrader/orderbuy_logwagrisknote.do?flag=true");		
		//boerse auswaehlen
		HtmlAnchor a = null;
		boolean term = false;
		for( int i = 0; i < boersenPriori.size() && !term; i++){
			term = true;
			try{
				String str = boerseToLinkStr( boersenPriori.get( i));
				a = page.getAnchorByHref( "/htmltrader/orderbuyaccountlist.do?key=" +
						str + "&source=orderbuyexchangelist");
			}catch( ElementNotFoundException e){
				term = false;
			}
		}
		if( term == false) throw new InvalidBuyOrderException( wkn, stueckzahl, stoppkurs,
				"Das Wertpapier wurde auf keiner der gelisteten Boersen gefunden.");
		page = a.click();
		
		//stueckzahl und kaufoptionen angeben
		form = page.getFormByName( "orderbuy");
		HtmlTextInput stueckzahlField = form.getInputByName( "numbersText");
		stueckzahlField.setValueAttribute( ( new Integer( stueckzahl)).toString());
		HtmlSelect selOrderart = (HtmlSelect)form.getByXPath( "./table[8]/tbody/tr[3]/td[3]/select").get( 0);
		HtmlOption option;
		HtmlTextInput stoppField;
		if( stoppkurs == 0.0){
			option = selOrderart.getOptionByText( "Market");
		}
		else{
			option = selOrderart.getOptionByText( "Stop Market");
			stoppField = form.getInputByName( "stopText");
			stoppField.setValueAttribute( ( new Double( stoppkurs)).toString());
		}
		selOrderart.setSelectedAttribute( option, true);

		HtmlSelect selGuelitgkeit = (HtmlSelect)form.getByXPath( "./table[8]/tbody/tr[5]/td[3]/select").get( 0);
		HtmlOption option2 = selGuelitgkeit.getOptionByText( "Ultimo laufende Woche");
		selGuelitgkeit.setSelectedAttribute( option2, true);
		
		button = form.getInputByName( "calculateButton");
		page = (HtmlPage)button.click();
		
		//gab es fehler in der order?
		form = page.getFormByName( "orderbuy");
		HtmlImageInput itanbutton = null;
		try{
			itanbutton = form.getInputByName( "releaseTypeButton_2");
		}catch( ElementNotFoundException e){}
		
		if( itanbutton == null){//ein fehler ist aufgetreten
			//war stueckzahl zu hoch? wenn ja einfach mit kleinerer stueckzahl kaufen!
			stueckzahlField = form.getInputByName( "numbersText");
			stoppField = form.getInputByName( "stopText");
			int s = (int)StringConvert.stringWithCommaPointToDouble( stueckzahlField.getText());
			int st = (int)StringConvert.stringWithCommaPointToDouble( stoppField.getText());
			if( s < stueckzahl){
				button = form.getInputByName( "calculateButton");
				page = (HtmlPage) button.click();
				form = page.getFormByName( "orderbuy");
				itanbutton = form.getInputByName( "releaseTypeButton_2");
			}
			else throw new InvalidBuyOrderException( wkn, stueckzahl, stoppkurs,
					"Moegliche Ursachen: der Stoppkurs koennte unter dem aktuellen Kurs liegen," +
					"es koennte sich jedoch auch die Website geaendert haben, oder ein unbekannter Fehler.");
		}
		page = (HtmlPage) itanbutton.click();
		//itan eingeben
		form = page.getFormByName( "orderbuy");
		HtmlTableDataCell td = (HtmlTableDataCell)form.getByXPath( "./table[9]/tbody/tr/td[5]").get( 0);
		int tanNr = new Integer( td.asText().split( " ")[4]);
		Security s = Security.getInstance( "da.enc", "hello");
		String tan = s.getTAN( tanNr);
		HtmlPasswordInput taninput = form.getInputByName( "releaseCode");
		taninput.setValueAttribute( tan);
		HtmlImageInput submitbutton = form.getInputByName( "submitButton");
		
		//bestaetigen
		if( !isTest){
			page = (HtmlPage) submitbutton.click();
		
			if( page.asText().contains( "Der eingegebene Freigabe-Code")){
				s.markTanAs("INVALID", tanNr);
				throw new InvalidTANException( tanNr, tan);
			}
			else{
				s.markTanAs("USED", tanNr);
			}
		}		
		
		return page;
	}
	
	public PairKassaDepot getKassaDepotWert() throws Exception{
		HtmlPage page = this.webClient.getPage( "https://internettrader.direktanlage.at/htmltrader/viewoverview.do?source=menu");
		HtmlForm form = page.getFormByName( "viewoverview");
		HtmlTableCell tcdepot = (HtmlTableCell)form.getByXPath( "./table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr[8]/td[4]").get( 0);
		HtmlTableCell tckassa = (HtmlTableCell)form.getByXPath( "./table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr[9]/td[4]").get( 0);

		PairKassaDepot pkd = new PairKassaDepot(
				StringConvert.stringWithCommaPointToDouble( tckassa.asText()),
				StringConvert.stringWithCommaPointToDouble( tcdepot.asText()));
		
		return pkd;
	}

	public HtmlPage logout() throws Exception{
		final HtmlPage p = webClient.getPage(
				"https://internettrader.direktanlage.at/htmltrader/admlogon_customer_init.do");
		return p;
	}
	
	public boolean verkaufen( String wkn){
		return true;
	}

	public AktienDepot getDepot() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	private String boerseToLinkStr( _ABSBrokerHandler.BoersenPlatz b){
		String boestr = "AA"; //xetra frankfurt
		switch( b){
			case WIEN_XETRA: boestr = "19";break;
			case XETRA_FANKFURT: boestr = "3";break;
			case FRANKFURT: boestr = "75";break;
			case HAMBURG: boestr = "76";break;
			case MUENCHEN: boestr = "87";break;
			default: boestr = "BB";
		}
		return boestr;
	}

	private String boerseToIdentStr( _ABSBrokerHandler.BoersenPlatz b){
		String boestr = "XETR"; //xetra frankfurt
		switch( b){
			case WIEN_XETRA: boestr = "XVIE5";break;
			case XETRA_FANKFURT: boestr = "XETR";break;
			case FRANKFURT: boestr = "XFRA";break;
			case HAMBURG: boestr = "XHAM";break;
			case MUENCHEN: boestr = "XMUN";break;
			default: boestr = "XETR";
		}
		return boestr;
	}
	
	private boolean isLoggedIn(){
		return true;
	}
	
	private boolean isLoggedOut(){
		return false;
	}

}
