package depotagent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;

public class SperrListe extends AktienListe{
	
	private String filename;
	
	public SperrListe( String filename) throws IOException {
		this.filename = filename;
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String linestr;

		while((linestr = in.readLine()) != null) {
			if( linestr.trim().equals("")) continue;   // skip empty lines
				
				Aktie a = new Aktie( 0, "sperrliste", linestr.trim(), new Date(), 0.0, 0.0, 0.0);
				this.aktienList.add( a);
		}
	}
	
	public void writeAktienListeToFile(){
		File file = new File( filename);
		Writer output;
		try {
			output = new BufferedWriter( new FileWriter( file, false));
			for( Aktie a : this.aktienList){
				output.write( a.getWkn() + "\n");
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
