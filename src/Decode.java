

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import tools.Masking;
import tools.Security;


public class Decode {
	

	public static String readPassword(String prompt) {
		Masking et = new Masking(prompt);
		Thread mask = new Thread(et);
//		mask.start();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String password = "";
		try {
			password = in.readLine();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		et.stopMasking();
		return password;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String password = readPassword("Passwort: ");
			Security h = Security.getInstance("da.enc", password);
			if(args.length == 0 || args[0].equals("data")) {
				System.out.println("Depot: " + h.getDepotNr());
				System.out.println("PIN: " + h.getPIN());
				System.out.println("Identifier: " + h.getIdentifier());
				System.out.println("Email: " + h.getEmail());
				System.out.println("Email Account Nr: " + h.getEmailAccountNr());
				System.out.println("Email Passwort: " + h.getEmailPassword());
				System.out.println("SMTP Server: " + h.getSmtp());
				System.out.println("SMTP Port: " + h.getSmtpPort());
				System.out.println("POP Server: " + h.getPop());
				System.out.println("POP Port: " + h.getPopPort());
			}
			if (args.length == 1 && args[0].equals("-tan")) {
				System.out.println(h.getallTANs());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
