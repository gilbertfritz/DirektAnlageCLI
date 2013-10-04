package exceptions;

public class InvalidTANException extends Exception {

	public InvalidTANException( int tanNr, String tan){
		super( tanNr + tan + "Tan ist ungueltig");
	}
}
