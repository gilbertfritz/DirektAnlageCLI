import tools.Emailing;
import tools.Security;


public class TestEmail {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Security h;
		try {
			h = Security.getInstance(null, null);
			 h.setEmail( "");
             h.setEmailAccountNr( "");
             h.setEmailPassword( "");
             h.setPop("");
             h.setSmtp("");
             h.setSmtpPort("");
			Emailing.getInstance().sendEmail("test email", "this is a test email from java client");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
