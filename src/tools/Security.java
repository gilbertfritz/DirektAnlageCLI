package tools;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


/**
 * Aufbau der Datei:
 *   PIN:pin
 *   DEPOT_NR:depotNr
 *   IDENTIFIER:identifier
 *   EMAIL:email
 *   EMAIL_ACCOUNT_NR:emailAccountNr
 *   EMAIL_PASSWORD:emailPassword
 *   POP:pop
 *   POP_PORT:popPort
 *   SMTP:smtp
 *   SMTP_PORT:smtpPort
 *   TAN_SECTION
 *   nr:tan
 *   nr:tan
 *   nr:tan
 *   ...
 */

public class Security {

	private static Security instance = null;

	private String filename;
	private String password;
	private String pin;
	private String depotNr;
	private String identifier;
	private String email;
	private String emailAccountNr;
	private String emailPassword;
	private String smtp;
	private String smtpPort;
	private String pop;
	private String popPort;
	private String tans;

	private static byte[] getBytesFromFile(File file) throws Exception {
		FileInputStream is = new FileInputStream(file);
	    byte[] bytes = new byte[(int)file.length()];
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length
	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }
	    if (offset < bytes.length) {
	        throw new Exception("Datei kann nicht vollständig gelesen werden: " + file.getName());
	    }
	    is.close();
	    return bytes;
	}

	private static String decryptFile(String filename, String password) throws Exception {
	    byte[] raw = new byte[16];
	    char[] c = password.toCharArray();
	    for (int i = 0; i < raw.length; i++)
	    	if (i < c.length)
	    		raw[i] = (byte) c[i];
	    	else
	    		raw[i] = 0;
	    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
	    Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.DECRYPT_MODE, skeySpec);
	    return new String(cipher.doFinal(getBytesFromFile(new File(filename))));
	}

	private static boolean encryptFile(String filename, String password, String data) throws Exception {
	    byte[] raw = new byte[16];
	    char[] c = password.toCharArray();
	    for (int i = 0; i < raw.length; i++)
	    	if (i < c.length)
	    		raw[i] = (byte) c[i];
	    	else
	    		raw[i] = 0;
	    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
	    Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
	    c = data.toCharArray();
	    byte[] d = new byte[c.length];
	    for (int i = 0; i < c.length; i++)
	    	d[i] = (byte) c[i];
	    byte[] enc = cipher.doFinal(d);
	    FileOutputStream fos = new FileOutputStream(filename);	    
	    fos.write(enc);
	    fos.close();
	    return true;
	}

	private Security(String filename, String password) throws Exception {
		this.filename = filename;
		this.password = password;
		if (filename != null) {
			String data = decryptFile(filename, password);
			BufferedReader r = new BufferedReader(new StringReader(data));
			String line = r.readLine();
			while (line != null) {
				if (line.startsWith("PIN:")) pin = line.substring(line.indexOf(':') + 1);
				else if (line.startsWith("DEPOT_NR:")) depotNr = line.substring(line.indexOf(':') + 1);
				else if (line.startsWith("IDENTIFIER:")) identifier = line.substring(line.indexOf(':') + 1);
				else if (line.startsWith("EMAIL:")) email = line.substring(line.indexOf(':') + 1);
				else if (line.startsWith("EMAIL_ACCOUNT_NR:")) emailAccountNr = line.substring(line.indexOf(':') + 1);
				else if (line.startsWith("EMAIL_PASSWORD:")) emailPassword = line.substring(line.indexOf(':') + 1);
				else if (line.startsWith("POP:")) pop = line.substring(line.indexOf(':') + 1);
				else if (line.startsWith("POP_PORT:")) popPort = line.substring(line.indexOf(':') + 1);
				else if (line.startsWith("SMTP:")) smtp = line.substring(line.indexOf(':') + 1);
				else if (line.startsWith("SMTP_PORT:")) smtpPort = line.substring(line.indexOf(':') + 1);
				line = r.readLine();
			}
			tans = "";
			tans += getallTANs();
		}
	}

	public static Security getInstance(String filename, String password) throws Exception {
		if (instance == null)
			instance = new Security(filename, password);
		return instance;
	}

	public boolean addTAN(int nr, String tan) throws Exception {
		String decrypted = decryptFile(filename, password);
		if (decrypted.length() == 0)
			throw new Exception("Fehler beim Entschlüsseln der Datei!");

		BufferedReader r = new BufferedReader(new StringReader(decrypted));
		String write = "";
		boolean found = false;
		String line = r.readLine();
		while (line != null) {
			if (line.startsWith("" + nr + ":")){
				write += nr + ":" + tan + ":FREE\n";
				found = true;
			}
			else write += line + "\n";
			line = r.readLine();
		}
		if( !found)
			write += nr + ":" + tan + ":FREE\n";
				
		encryptFile(filename, password, write);
		return false;
	}

	public String getTAN(int nr) throws Exception {
		String data = decryptFile(filename, password);
		BufferedReader r = new BufferedReader(new StringReader(data));
		String line = r.readLine();
		while (line != null) {
			if (line.startsWith("" + nr + ":")){
				return line.split( ":")[1];
			}
			line = r.readLine();
		}

		throw new Exception("TAN unbekannt!");
	}

	public boolean markTanAs(String mark, int nr) throws Exception{
		String decrypted = decryptFile(filename, password);
		if (decrypted.length() == 0)
			throw new Exception("Fehler beim Entschlüsseln der Datei!");

		BufferedReader r = new BufferedReader(new StringReader(decrypted));
		String write = "";
		boolean found = false;
		String line = r.readLine();
		while (line != null) {
			if (!line.startsWith("" + nr + ":")){
				write += line + "\n";
				
			}
			else{
				String sarr[] = line.split(":");
				write += sarr[0] + ":" + sarr[1] + ":" + mark + "\n";
				found = true;
			}
			line = r.readLine();
		}
		if( !found){
			return false;
		}
		else{
			encryptFile(filename, password, write);
			return true;
		}		
	}
	
	public String getallTANs() throws Exception {
		String result = "";
		String data = decryptFile(filename, password);
		BufferedReader r = new BufferedReader(new StringReader(data));
		String line = r.readLine();
		int tansection = 0;
		while (line != null) {
			if( tansection == 0 && line.startsWith( "TAN_SECTION")) tansection = 1;
			else if( tansection == 1) tansection = 2;
			if( tansection == 2) result += line + "\n";
			line = r.readLine();
		}
		return result;
	}
	
	public boolean removeTAN(int nr) throws Exception {
		String decrypted = decryptFile(filename, password);
		if (decrypted.length() == 0)
			throw new Exception("Fehler beim Entschlüsseln der Datei!");

		BufferedReader r = new BufferedReader(new StringReader(decrypted));
		String write = "";
		boolean found = false;
		String line = r.readLine();
		while (line != null) {
			if (!line.startsWith("" + nr + ":")){
				write += line + "\n";
				
			}
			else found = true;
			line = r.readLine();
		}
		if( !found){
			return false;
		}
		else{
			encryptFile(filename, password, write);
			return true;
		}
	}

	public void removeAllTANs() throws Exception {
		String result = "";
		String data = decryptFile(filename, password);
		BufferedReader r = new BufferedReader(new StringReader(data));
		String line = r.readLine();
		boolean tansection = false;
		while (line != null && !tansection) {
			result += line + "\n";
			if( line.startsWith( "TAN_SECTION")) tansection = true;
			line = r.readLine();
		}
		encryptFile(filename, password, result);		
	}
	
	
	public void setPIN(String pin) {
		this.pin = pin;
	}

	public String getPIN() {
		return pin;
	}

	public void setDepotNr(String depotNr) {
		this.depotNr = depotNr;
	}

	public String getDepotNr() {
		return depotNr;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void updateUserData( String newpassword, String depotNr, String pin, String identifier, String email, String emailAccountNr,
			String emailPassword, String smtp, String smtpPort, String pop, String popPort) throws Exception{
			String data = "PIN:" + pin + "\n" + "DEPOT_NR:" + depotNr + "\n" + "IDENTIFIER:" + identifier + "\n";
			data += "EMAIL:" + email + "\n" + "EMAIL_ACCOUNT_NR:" + emailAccountNr + "\n" + "EMAIL_PASSWORD:" + emailPassword + "\n" + "POP:" + pop + "\n" + "POP_PORT:" + popPort + "\n" + "SMTP:" + smtp + "\n" + "SMTP_PORT:" + smtpPort + "\n";
			data += "TAN_SECTION\n" + tans;
			
			encryptFile(filename, newpassword, data);
	}
	public boolean saveToFile(String filename) throws Exception {
		String data = "PIN:" + pin + "\n" + "DEPOT_NR:" + depotNr + "\n" + "IDENTIFIER:" + identifier + "\n";
		data += "EMAIL:" + email + "\n" + "EMAIL_ACCOUNT_NR:" + emailAccountNr + "\n" + "EMAIL_PASSWORD:" + emailPassword + "\n" + "POP:" + pop + "\n" + "POP_PORT:" + popPort + "\n" + "SMTP:" + smtp + "\n" + "SMTP_PORT:" + smtpPort + "\n";
		data += "TAN_SECTION\n";
		return encryptFile(filename, password, data);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmailPassword() {
		return emailPassword;
	}

	public void setEmailPassword(String emailPassword) {
		this.emailPassword = emailPassword;
	}

	public String getSmtp() {
		return smtp;
	}

	public void setSmtp(String smtp) {
		this.smtp = smtp;
	}

	public String getPop() {
		return pop;
	}

	public void setPop(String pop) {
		this.pop = pop;
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}

	public String getPopPort() {
		return popPort;
	}

	public void setPopPort(String popPort) {
		this.popPort = popPort;
	}

	public String getEmailAccountNr() {
		return emailAccountNr;
	}

	public void setEmailAccountNr(String emailAccountNr) {
		this.emailAccountNr = emailAccountNr;
	}
}
