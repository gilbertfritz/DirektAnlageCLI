package GUI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.LinkedList;

import tools.Logging;

import depotagent.*;

public class ConsoleGUI {

	private final String FILENAME = "/mnt/ftp/hl/WEBSITES/depotagent/output2.html";
	private int width;
	private int height;
	private DepotAgentSimulation dAS;
	private LinkedList<String> msgLog;
	
	public ConsoleGUI( DepotAgentSimulation dAS, int width, int height) {
		this.dAS = dAS;
		this.width = width;
		this.height = height;
		this.msgLog = new LinkedList<String>();
	}
	
	public String refresh( boolean toHtml){

		String screen = "";
		screen += printLine();
		screen += writeTextCenter( "DepotAgent                      letztes update: " + (new Date()).toLocaleString());
		screen += printLine();
		screen += printEmptyLine();
		
		screen += printAktienDepot( dAS.depotLokEigen, "EIGENES DEPOT");screen += printEmptyLine();
		
		if( dAS.orderKaufen.size() != 0){
			screen += printAktienListe( dAS.orderKaufen, "KAUF ORDERS");screen += printEmptyLine();
		}
		if( dAS.orderVerkaufen.size() != 0){
			screen += printAktienListe( dAS.orderVerkaufen, "VERKAUF ORDERS");screen += printEmptyLine();
		}
		if( dAS.orderErteilenKaufen.size() != 0){
			screen += printAktienListe( dAS.orderErteilenKaufen, "KAUF ORDERS ZU ERTEILEN");screen += printEmptyLine();
		}

		screen += printLine();
		screen += printEmptyLine();
		screen += printAktienDepot( dAS.depotLokAktionaer, "DER AKTIONAER MUSTERDEPOT");screen += printEmptyLine();
		screen += printEmptyLine();
		screen += printLine();
		
		screen += printEmptyLine();
		screen += writeTextCenter( "-- Log --");
		screen += printEmptyLine();
		screen += printMsgLog( screen);
		
		screen += finishScreen( screen);
		
		if( toHtml) toHtml( FILENAME, screen);
		return screen;
	} 
	
	public void addLogMsg( String msg){
		this.msgLog.add( "# " + (new Date()).toLocaleString().split( " ")[1] + "  " + msg + " *");
		if( msgLog.size() >= 100){
			msgLog.removeLast();
		}
	}
	
	private String printMsgLog( String sofar){
		String r = "";
		int i = (sofar.split( "\n")).length-1;
		int idx = this.msgLog.size()-1;
		for( i=i; i < height-1 && idx >= 0; i++){
			r += writeText( 3, msgLog.get( idx));
			idx--;
		}
		
		return r;
	}
	
	private String killTabs( String line){
		String r = "";
		String[] spl = line.split( "\t");
		for( int i = 0; i < spl.length; i++){
			r += spl[i];
			if( i != spl.length-1) r += "     ";
		}
		return r;
	}
	
	private String printAktienDepot( AktienDepot ad, String name){
		String r = "";
		r += writeText( 3, writeTextInSpace( "-- " + name.toUpperCase() + " --", 34)
				+ "                               KAUF     AKT.     STOP     PERF.");
		r += writeText( 3, "Gesamt: " + writeTextInSpace( ad.getGesamtWert()+"", 8)
				+ "   Kassa: " + writeTextInSpace( ad.getKassaWert()+"", 8)
				+ "   Depot: " + writeTextInSpace( ad.getDepotWert()+"", 8));
		r += writeText( 3, "Rel.Perf.: " + writeTextInSpace( ad.getRelativePerformance()+"",6)
				+ "%   Abs.Perf.: " + writeTextInSpace( ad.getAbsolutePerformance()+"", 6));
		r += printEmptyLine();
		
		if( ad.size() == 0){
			r += writeText( 3, "#Noch keine Aktien im Depot.*");
		}
		
		for( Aktie a : ad.getAktienList()){
			String astr = new String();
			astr += writeTextInSpace( a.getStueckZahl()+"", 6) + "   ";
			astr += writeTextInSpace( a.getName(), 20) + "   ";
			astr += writeTextInSpace( a.getWkn(), 8) + "   ";
			astr += a.getKaufDatum().toLocaleString() + "   ";
			astr += writeTextInSpace( a.getKaufKurs()+"", 6) + "   ";
			astr += writeTextInSpace( a.getAktuellerKurs()+"", 6) + "   ";
			astr += writeTextInSpace( a.getStoppKurs()+"", 6) + "   ";
			astr += writeTextInSpace( a.getRelativePerformance()+" %", 6);
			
			r += writeText( 3, astr);
		}
	
		return r;
	}

	
	private String printAktienListe( AktienListe al, String name){
		String r = "";
		r += writeText( 3, writeTextInSpace( "-- " + name.toUpperCase() + " --", 34)
				+ "                               KAUF     AKT      STOP");
		for( Aktie a : al.getAktienList()){
			String astr = new String();
			astr += writeTextInSpace( a.getStueckZahl()+"", 6) + "   ";
			astr += writeTextInSpace( a.getName(), 20) + "   ";
			astr += writeTextInSpace( a.getWkn(), 8) + "   ";
			astr += a.getKaufDatum().toLocaleString() + "   ";
			astr += writeTextInSpace( a.getKaufKurs()+"", 6) + "   ";
			astr += writeTextInSpace( a.getAktuellerKurs()+"", 6) + "   ";
			astr += writeTextInSpace( a.getStoppKurs()+"", 6) + "   ";
			
			r += writeText( 3, astr);
		}
		return r;
	}
	
	private String writeTextInSpace( String text, int spacesize){
		String r = text;
		if( r.length() > spacesize){
			r = r.substring( 0, spacesize);
		}else{		
			for( int i = r.length(); i < spacesize; i++) r += " ";
		}
		
		return r;
	}
	
	private String printLine(){
		String r = "";
		r += "+";for( int i = 0; i < width-2; i++) r += "-"; r += "+\n";
		
		return r;
	}

	private String printEmptyLine(){
		String r = "";
		r += "|";for( int i = 0; i < width-2; i++) r += " "; r += "|\n";
		
		return r;
	}
	
	private String finishScreen( String sofar){
		String r = "";
		int i = (sofar.split( "\n")).length-1;
		for( i=i; i < height; i++){
			r += "|";for( int j = 0; j < width-2; j++) r += " "; r += "|\n";
		}
		r += printLine();
		r = r.substring( 0, r.length()-1);
		return r;
	}
	
	private String writeText( int xOffset, String text){
		String r = "|";
		for( int i = 0; i < xOffset-1; i++) r += " ";
		r += text;
		r += finishLine( r);
		return r;
	}
	
	private String writeTextCenter( String text){
		String r = "";
		int offset = (width - text.length())/2;
		r += writeText( offset, text);
		return r;
	}
	
	private String finishLine( String sofar){
		String r = "";
		int rest = width - sofar.length();
		if( rest > 0){
			for( int i = 0; i < rest-1; i++){
				r += " ";
			}
			r += "|\n";
		}
		return r;
	}
	
	private void toHtml( String filename, String screen){
		String out = "";
		for( int i = 0; i < screen.length(); i++){
			if( screen.charAt(i) == ' ') out += "&nbsp;";
			else if( screen.charAt(i) == '\n') out += "<br />";
			else if( screen.charAt(i) == '#') out += "&nbsp;<font color=\"GREY\">";
			else if( screen.charAt(i) == '*') out += "&nbsp;</font><font color=\"BLACK\">";
			else out += screen.charAt( i);
		}
		
		File file = new File( filename);
		Writer output;
		try {
			output = new BufferedWriter( new FileWriter( file, false));
			output.write( "<html><body><font size=\"2\" face=\"Monospace\"><br /><div align=center><p>");
			output.write( out);
			output.write( "</p></div></font></body></html>");
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
			Logging.log( e.getMessage());
			addLogMsg( "Fehler beim Schreiben der HTML!");
		}
	}
}
