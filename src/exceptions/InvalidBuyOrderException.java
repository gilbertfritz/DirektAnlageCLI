package exceptions;

public class InvalidBuyOrderException extends Exception{
	
	public InvalidBuyOrderException( String wkn, int stueckzahl, double stoppkurs, String msg) {
		super(msg);
		//logg
		//email
	}

}
