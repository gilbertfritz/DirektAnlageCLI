package depotagent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import websitehandler.*;

public class AktienDepot extends AktienListe implements Serializable{
	private static final long serialVersionUID = -568716173373839327L;

	private double kassaWert;
	private final double auferlegungsWert; 

	//-----------
	//Konstruktor fuer lokales-depot
	public AktienDepot( double auferlegungsWert){
		this.auferlegungsWert = auferlegungsWert;
	}
	
	public AktienDepot( AktienDepot d){
		super( d.getAktienList());
		this.auferlegungsWert = d.auferlegungsWert;
		this.kassaWert = d.getKassaWert();
	}
	
	//gibt informationen vom musterdepot aus
	public String toString(){
		String ret = "";
		for( int i = 0; i < aktienList.size(); i++){
			ret += aktienList.get( i).toString() + "\n";
		}
		return ret;
	}
	
	public PairKassaDepot getKassaDepot(){
		return new PairKassaDepot( this.kassaWert, this.getDepotWert());
	}
	
	public double getDepotWert(){
		double d = 0.0;
		for( Aktie a : aktienList){
			d += a.getActualRate()*a.getStueckZahl();
		}
		return d;
	}
	
	public double getKassaWert() {
		return kassaWert;
	}

	public void setKassaWert(double kassaWert) {
		this.kassaWert = kassaWert;
	}
	
	public double getGesamtWert(){
		return kassaWert+getDepotWert();
	}
	
	public double getRelativePerformance(){
		return (((kassaWert+getDepotWert())/auferlegungsWert)-1)*100;
	}
	
	public double getAbsolutePerformance(){
		return (kassaWert+getDepotWert())-auferlegungsWert;
	}
}
