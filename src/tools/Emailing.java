package tools;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import com.sun.mail.util.MailSSLSocketFactory;

public class Emailing {

	private Emailing() {}

	private static Emailing instance = null;

	public static Emailing getInstance() {
		if (instance == null)
			instance = new Emailing();
		return instance;
	}

	public void sendEmail(String subject, String body) throws Exception {
		Message message = new MimeMessage(getSession());
		message.addRecipient(RecipientType.TO, new InternetAddress(Security.getInstance(null, null).getEmail()));
		message.addFrom(new InternetAddress[] { new InternetAddress(Security.getInstance(null, null).getEmail()) });
		message.setSubject(subject);
		message.setContent(body, "text/plain");
		Transport.send(message);
	}

	private Session getSession() throws Exception {
		Authenticator authenticator = new Authenticator();
		Properties properties = new Properties();
		properties.setProperty("mail.smtp.submitter", authenticator.getPasswordAuthentication().getUserName());
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.host", Security.getInstance(null, null).getSmtp());
		properties.setProperty("mail.smtp.port", Security.getInstance(null, null).getSmtpPort());
		properties.setProperty("mail.smtp.ssl.enable", "true");
		return Session.getInstance(properties, authenticator);
	}

	private class Authenticator extends javax.mail.Authenticator {
		private PasswordAuthentication authentication;

		public Authenticator() throws Exception {
			String username = Security.getInstance(null, null).getEmailAccountNr();
			String password = Security.getInstance(null, null).getEmailPassword();
			authentication = new PasswordAuthentication(username, password);
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			return authentication;
		}
	}
}
