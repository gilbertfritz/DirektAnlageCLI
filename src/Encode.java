

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import tools.*;
import tools.Security;


public class Encode {


	public static String readPassword(String prompt) {
		Masking et = new Masking(prompt);
//		Thread mask = new Thread(et);
//		mask.start();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String password = "";
		try {
			password = in.readLine();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		//stop masking
//		et.stopMasking();
		//return the password entered by the user
		return password;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			File f = new File( "da.enc");
			String password = "";
			String newpassword = "";
			
			if(args.length == 0 || args[0].equals("data")) {
				if( f.exists()){
					password = readPassword("Altes Passwort: ");
					newpassword = readPassword("Neues Passwort: ");
				}
				else
					password = readPassword("Passwort f. neue Datei setzen: ");
				
				
				String depotNr= readPassword("Depot Nr: ");
				String pin = readPassword("PIN: ");
				String identifier = readPassword("Identifier: ");
				String email = readPassword("Email: ");
				String emailAccountNr = readPassword("Email Account Nr: ");
				String emailPassword = readPassword("Email Passwort: ");
				String smtp = readPassword("SMTP Server: ");
				String smtpPort = readPassword("SMTP Port: ");
				String pop = readPassword("POP Server: ");
				String popPort = readPassword("POP Port: ");


				if( f.exists()){
					Security h = Security.getInstance("da.enc", password);
					
					h.updateUserData( newpassword, depotNr, pin, identifier, email, emailAccountNr, emailPassword,
							smtp, smtpPort, pop, popPort);
					System.out.println("FILE UPDATED");
				}
				else{
					Security h = Security.getInstance(null, password);
					h.setDepotNr(depotNr);
					h.setIdentifier(identifier);
					h.setPIN(pin);
					h.setEmail(email);
					h.setEmailAccountNr(emailAccountNr);
					h.setEmailPassword(emailPassword);
					h.setPop(pop);
					h.setPopPort(popPort);
					h.setSmtp(smtp);
					h.setSmtpPort(smtpPort);

					h.saveToFile("da.enc");
					System.out.println("CREATED NEW FILE");
				}
			}
			else if (args.length == 1 && args[0].equals("-tan")) {
				password = readPassword("Passwort: ");
				Security h = Security.getInstance("da.enc", password);
				String nr = readPassword("Nummer: ");
				int inr = Integer.parseInt(nr);
				String tan = readPassword( inr + ": ");
				while (!tan.equals("")) {
					h.addTAN(inr, tan);
					inr++;
					tan = readPassword( inr + ": ");
				}
			}
			else if (args.length == 1 && args[0].equals("-tanr")) {
				password = readPassword("Passwort: ");
				Security h = Security.getInstance("da.enc", password);
				String nr = readPassword("Nummer: ");
				while (!nr.equals("")) {
					if( h.removeTAN( Integer.parseInt(nr)))
						System.out.println( "TAN removed");
					else System.out.println( "TAN number not found!");
					nr = readPassword("Nummer: ");
				}
			}
			else if (args.length == 1 && args[0].equals("-tanrall")) {
				password = readPassword("Passwort: ");
				Security h = Security.getInstance("da.enc", password);
				h.removeAllTANs();
			}
			else if (args.length == 1 && args[0].equals("-tanused")) {
				password = readPassword("Passwort: ");
				Security h = Security.getInstance("da.enc", password);
				String nr = readPassword("Nummer: ");
				while (!nr.equals("")) {
					if( h.markTanAs( "USED", Integer.parseInt(nr)))
						System.out.println( "TAN marked as used");
					else System.out.println( "TAN number not found!");
					nr = readPassword("Nummer: ");
				}
			}

			System.out.println( "ok");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
