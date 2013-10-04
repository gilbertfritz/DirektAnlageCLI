package websitehandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import tools.StringConvert;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;


public class YahooHandler {

	public static double getActualRate( String wkn, int timeout) throws Exception {
		
		final WebClient webClient = new WebClient( BrowserVersion.INTERNET_EXPLORER_8);
		webClient.setJavaScriptEnabled( false);
		webClient.setCssEnabled( false);
		webClient.setTimeout( timeout);
		
		//load the page	
		HtmlPage page = webClient
			.getPage( "http://de.finance.yahoo.com/lookup?s=" + wkn);
		
		HtmlDivision div = (HtmlDivision)page.getElementById( "yfi_sym_results");
		HtmlTableCell td = (HtmlTableCell)div.getByXPath( "./table/tbody/tr/td").get( 3);
		
		return StringConvert.stringWithCommaPointToDouble( td.asText());
	}	
}
