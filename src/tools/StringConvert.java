package tools;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringConvert {
	
	//zb 3.000 zu int 3000
	public static int stringWithCommaToInt( String str){
		String s = "";
		for( int i = 0; i < str.length(); i++){
			if( str.charAt( i) != '.') s += str.charAt( i);
		}

		if( !s.trim().equals( "")){
			Integer i = new Integer( s);
			return i.intValue();
		}
		else{
			return -1;
		}
	}

	//zb 3.000,23 zu double 3000,23
	public static double stringWithCommaPointToDouble( String str){
		String s = "";
		for( int i = 0; i < str.length(); i++){
			if( str.charAt( i) == ',') s += '.';
			else if( str.charAt( i) != '.') s += str.charAt( i);
		}
		
		if( !s.trim().equals("")){
			Double d = new Double( s);
			return d.doubleValue();
		}
		else return -1;
	}

	//-----------------------------------
	//converts a string of this format "1,00[EUROSYMBOL]" into double
	public static double euroStrToDouble( String commaStr)
	{
		char c[] = commaStr.toCharArray();
		String pointStr = "";
		for( int i = 0; i < c.length-1; i++)
		{
			if( c[i] == ',') pointStr += ".";
			else pointStr += c[i];				
		}
		
		if( !pointStr.trim().equals( "")){
			Double d = new Double( pointStr);
			return d.doubleValue();
		}
		else return -1;
	}	

	//--------------------------------
	//converts a string to a date object
	public static Date stringToDate( String dateStr, String dateFormat)
	{
		Date date = new Date();
		
		try
		{
			DateFormat df = new SimpleDateFormat( dateFormat);
			date = df.parse( dateStr);
		}catch( ParseException e)
		{
			return new Date();
		}
		
		return date;
	}
}
