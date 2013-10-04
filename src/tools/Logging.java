package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import GUI.ConsoleGUI;

public class Logging {

	private static String logdir = "logging";
	
	public static void log( String msg){
		File dir = new File( logdir);
		if( !dir.exists()) dir.mkdir();
		
		Date d = new Date();
		String[] sarr = d.toLocaleString().split( "[. ]");
		String filename = logdir + "/log";
		for( int i = 0; i <= 2; i++){
			filename += "_" + sarr[2-i];
		}
		filename += ".txt";
		
		File file = new File( filename);
		Writer output;
		try {
			output = new BufferedWriter( new FileWriter( file, true));
			output.write( "\n" + sarr[3] + " -> " + msg);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void log( String msg, ConsoleGUI gui){
		log( msg);
		gui.addLogMsg( msg);
	}
}
