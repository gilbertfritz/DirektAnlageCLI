package depotagent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import websitehandler.*;

public class AktienListe implements Serializable{
	private static final long serialVersionUID = -568716173373839327L;

	protected ArrayList<Aktie> aktienList;

	//-----------
	//Konstruktor fuer lokales-depot
	public AktienListe(){
		aktienList = new ArrayList<Aktie>();
	}
	
	public AktienListe( ArrayList<Aktie> al){
		aktienList = (ArrayList<Aktie>)al.clone();
	}
	
	//-----------
	//fuege aktie hinzu
	public void addAktie( Aktie a){
		aktienList.add( a);
	}

	//-----------
	//vergleicht anhand wkn
	//return[0] = Aktien in otherDepot aber nicht in this
	//return[1] = Aktien nicht in otherDepot aber in this
	public AktienListe[] vergleicheDepot( AktienListe otherDepot){
		AktienListe gekauft = new AktienListe();
		for( int i = 0; i < otherDepot.size(); i++){
			gekauft.addAktie( new Aktie(otherDepot.getAktie( i)));
		}
		AktienListe verkauft = new AktienListe();		
		for( int i = 0; i < this.size(); i++){
			verkauft.addAktie( new Aktie( this.getAktie( i)));
		}

		boolean found = true;
		while( found){
			found = false;
			for( int i = 0; i < gekauft.size() && !found; i++){
				for( int j = 0; j < verkauft.size() && !found; j++){
					if( gekauft.getAktie( i).getWkn().equals( verkauft.getAktie( j).getWkn())){
						found = true;
						gekauft.removeAktie( i);
						verkauft.removeAktie( j);
					}
				}//for
			}//for
		}//while
		
		AktienListe[] ret = new AktienListe[2];		
		ret[0] = gekauft;
		ret[1] = verkauft;
		return ret;
	}
	
	//-----------
	//vergleicht anhand wkn unter beruecksichtigung der sperrliste
	//return[0] = Aktien in otherDepot aber nicht in this und nicht in sperrliste
	//return[1] = Aktien nicht in otherDepot aber in this und nicht in sperrliste
	public AktienListe[] vergleicheDepot( AktienListe otherDepot, SperrListe sl){
		AktienListe[] ret = new AktienListe[2];
		
		AktienListe[] temp = vergleicheDepot( otherDepot);
		ret[0] = temp[0].vergleicheDepot( sl)[1]; 
		ret[1] = temp[1];
		
		return ret;
	}

	public AktienListe[] vergleicheDepotOrderlists( AktienListe otherDepot, AktienListe orderKaufen,
			AktienListe orderVerkaufen, AktienListe orderErteilenKaufen){
		
		AktienListe[] thisother = vergleicheDepot( otherDepot);
		
		AktienListe thiskaufen = thisother[0].vergleicheDepot( orderKaufen)[1];
		thiskaufen = thiskaufen.vergleicheDepot( orderErteilenKaufen)[1];

		AktienListe thisverkaufen = thisother[1].vergleicheDepot( orderVerkaufen)[1]; 
		
		AktienListe[] ret = new AktienListe[2];
		ret[0] = thiskaufen;
		ret[1] = thisverkaufen;
		
		return ret;
	}
	
	//-------------------------
	//ueberschreibt aktuelle kurse und stoppkurse mit kursen aus otherDepot
	public void synchKurse( AktienDepot otherDepot){
		for( int i = 0; i < otherDepot.size(); i++){
			for( int j = 0; j < this.size(); j++){
				if( otherDepot.getAktie( i).equals( this.getAktie( j))){
					Aktie a = otherDepot.getAktie( i);
					this.getAktie( j).setStoppKurs( a.getStoppKurs());
					this.getAktie( j).setAktuellerKurs( a.getActualRate());
				}
			}
		}
	}
	
	
	public void removeAktie( String wkn){
		for( int i = 0; i < aktienList.size(); i++){
			if( aktienList.get( i).getWkn().equals( wkn)){
				aktienList.remove( i);
				return;
			}
		}
		
		System.out.println( "WARNUNG Depot::removeAktie fehlgeschlagen");
	}
	
	public void removeAktie( int idx){
		this.aktienList.remove( idx);
	}

	
	public void updateRatesFromYahoo( int timeout){
		for( Aktie a : aktienList){
			try{
				a.setAktuellerKurs( YahooHandler.getActualRate( a.getWkn(), timeout));
			}catch( Exception e){
			}
		}
	}
	
	public String toString(){
		String ret = "";
		for( int i = 0; i < aktienList.size(); i++){
			ret += aktienList.get( i).toString() + "\n";
		}
		return ret;
	}
	
	public ArrayList<Aktie> getAktienList(){
		return aktienList;
	}
	
	public int size(){
		return aktienList.size();
	}
	
	//---------
	//gibt aktie a.d. Stelle idx zurueck
	public Aktie getAktie( int idx){
		return aktienList.get( idx);
	}
	
	//---------
	//gibt aktie mit der jeweiligen wkn zurueck
	public Aktie getAktie( String wkn){
		for( int i = 0; i < this.size(); i++){
			if( aktienList.get( i).getWkn().equals( wkn))
				return aktienList.get( i);
		}
		return null;
	}

	public double getDepotWert(){
		double d = 0.0;
		for( Aktie a : aktienList){
			d += a.getActualRate()*a.getStueckZahl();
		}
		return d;
	}
}
/*	
//-----------
//vergleicht anhand wkn und stueckzahl (fuer TEILVERKAEUFE / ZUKAEUFE)
//return[0] = Aktien in otherDepot aber nicht in this
//return[1] = Aktien nicht in otherDepot aber in this
public AktienListe[] vergleicheDepot( AktienListe otherDepot){
	AktienListe gekauft = new AktienListe();
	for( int i = 0; i < otherDepot.size(); i++){
		gekauft.addAktie( new Aktie(otherDepot.getAktie( i)));
	}
	AktienListe verkauft = new AktienListe();		
	for( int i = 0; i < this.size(); i++){
		verkauft.addAktie( new Aktie( this.getAktie( i)));
	}

	boolean found = true;
	while( found){
		found = false;
		for( int i = 0; i < gekauft.size() && !found; i++){
			for( int j = 0; j < verkauft.size() && !found; j++){
				if( gekauft.getAktie( i).getWkn().equals( verkauft.getAktie( j).getWkn())){
					found = true;
					if( gekauft.getAktie( i).getStueckZahl() > verkauft.getAktie( j).getStueckZahl()){
						gekauft.getAktie( i).setStueckZahl( gekauft.getAktie( i).getStueckZahl() - verkauft.getAktie( j).getStueckZahl());
						verkauft.removeAktie( j);
					}
					else if( gekauft.getAktie( i).getStueckZahl() < verkauft.getAktie( j).getStueckZahl()){
						verkauft.getAktie( i).setStueckZahl( verkauft.getAktie( i).getStueckZahl() - verkauft.getAktie( j).getStueckZahl());
						gekauft.removeAktie( i);
					}
					else if( gekauft.getAktie( i).getStueckZahl() == verkauft.getAktie( j).getStueckZahl()){
						gekauft.removeAktie( i);
						verkauft.removeAktie( j);
					}
				}
			}//for
		}//for
	}//while
	
	AktienListe[] ret = new AktienListe[2];		
	ret[0] = gekauft;
	ret[1] = verkauft;
	return ret;
}
*/
