package depotagent;

import java.io.Serializable;
import java.util.Date;

public class Aktie implements Serializable{
	private static final long serialVersionUID = 664321562007519347L;

	private String wkn;
	private String name;
	private Date kaufDatum;
	private double kaufKurs;
	private double stoppKurs;
	private double aktuellerKurs;
	private int stueckZahl;
	
	public Aktie( int stueckZahl, String name, String wkn, Date buyDate,
			double buyRate, double aktuellerKurs, double stoppKurs) {
		this.stueckZahl = stueckZahl;
		this.name = name;
		this.wkn = wkn;
		this.kaufDatum = buyDate;
		this.kaufKurs = buyRate;
		this.aktuellerKurs = aktuellerKurs;
		this.stoppKurs = stoppKurs;
	}

	public Aktie( Aktie a){
		this( new Integer( a.stueckZahl), new String( a.getName()), new String( a.getWkn()), (Date)a.getKaufDatum().clone(),
				new Double( a.kaufKurs), new Double( a.aktuellerKurs), new Double( a.stoppKurs));
	}
	
	public String toString(){
		String nametab = "\t";
		if( name.length() < 16) nametab = "\t\t";
		return	name + nametab + wkn + "\t" + kaufDatum.toLocaleString()
			+ "\t" + kaufKurs + "\t" + aktuellerKurs + "\t" + stoppKurs;
	}
	
	public double getStoppKurs() {
		return stoppKurs;
	}

	public double getRelativePerformance(){
		return (((aktuellerKurs)/kaufKurs)-1)*100;
	}
	
	public void setStoppKurs(double stoppKurs) {
		this.stoppKurs = stoppKurs;
	}

	public double getAktuellerKurs() {
		return aktuellerKurs;
	}

	public void setAktuellerKurs(double aktuellerKurs) {
		this.aktuellerKurs = aktuellerKurs;
	}

	public void setWkn(String wkn) {
		this.wkn = wkn;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean equals( Aktie a) {
		return this.wkn.equals( a.wkn);
	}
	
	public double getActualRate() {
		return aktuellerKurs;
	}

	public String getWkn() {
		return wkn;
	}

	public String getName() {
		return name;
	}

	public double getBuyRate() {
		return kaufKurs;
	}
	
	public int getStueckZahl() {
		return stueckZahl;
	}

	public void setStueckZahl(int stueckZahl) {
		this.stueckZahl = stueckZahl;
	}
	
	public void setKaufKurs( double kk){
		this.kaufKurs = kk;
	}
	
	public double getKaufKurs() {
		return kaufKurs;
	}
	public Date getKaufDatum() {
		return kaufDatum;
	}

	public void setKaufDatum(Date kaufDatum) {
		this.kaufDatum = kaufDatum;
	}
	
}
