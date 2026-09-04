package hbv.web;

import jakarta.servlet.ServletContext;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import javax.mail.util.ByteArrayDataSource;

import javax.activation.DataHandler;

import java.util.Properties;

public class MailService {

    private static Session createSession(ServletContext context) {

        String mailUser = context.getInitParameter("mailuser");
        String mailPassword = context.getInitParameter("mailpassword");

        if (mailUser == null || mailUser.trim().isEmpty()) {
            throw new IllegalStateException(
                "mailuser ist nicht in web.xml konfiguriert."
            );
        }

        if (mailPassword == null || mailPassword.trim().isEmpty()) {
            throw new IllegalStateException(
                "mailpassword ist nicht in web.xml konfiguriert."
            );
        }

        Properties properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        return Session.getInstance(
            properties,
            new Authenticator() {

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {

                    return new PasswordAuthentication(
                        mailUser,
                        mailPassword
                    );
                }
            }
        );
    }

    public static void sendRegistrationMail(
            ServletContext context,
            String empfaenger,
            String vorname)
            throws MessagingException {

        String mailUser = context.getInitParameter("mailuser");

        Session session = createSession(context);

        MimeMessage message = new MimeMessage(session);

        message.setFrom(
            new InternetAddress(mailUser)
        );

        message.setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse(empfaenger)
        );

        message.setSubject(
            "Registrierung im Bibliothekssystem",
            "UTF-8"
        );

        String loginUrl =
            "https://informatik.hs-bremerhaven.de/"
            + "docker-sausubedi-java/"
            + "mitglied_login.html";

        String html =
            "<html>"
            + "<body>"
            + "<p>Hallo "
            + escapeHtml(vorname)
            + ",</p>"
            + "<p>"
            + "Ihr Konto wurde erfolgreich erstellt. "
            + "Jetzt können Sie sich mit Ihrer E-Mail "
            + "und Ihrem Passwort einloggen."
            + "</p>"
            + "<p>"
            + "Klicken Sie hier, um sich einzuloggen: "
            + "<a href=\""
            + loginUrl
            + "\">Bibliothekssystem</a>"
            + "</p>"
            + "<p>"
            + "Mit freundlichen Grüßen,<br>"
            + "Ihr Team"
            + "</p>"
            + "</body>"
            + "</html>";

        message.setContent(
            html,
            "text/html; charset=UTF-8"
        );

        Transport.send(message);
    }

    public static void sendAusleihMail(
            ServletContext context,
            String empfaenger,
            String vorname,
            byte[] pdfBytes,
            byte[] qrBytes)
            throws MessagingException {

        String mailUser = context.getInitParameter("mailuser");

        Session session = createSession(context);

        MimeMessage message = new MimeMessage(session);

        message.setFrom(
            new InternetAddress(mailUser)
        );

        message.setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse(empfaenger)
        );

        message.setSubject(
            "Ihre Ausleihe wurde bestätigt",
            "UTF-8"
        );

        MimeBodyPart textPart = new MimeBodyPart();

        String text =
            "Hallo "
            + vorname
            + ",\n\n"
            + "Ihre Ausleihe wurde erfolgreich bestätigt."
            + "\n\n"
            + "Im Anhang finden Sie Ihren "
            + "Ausleihbeleg als PDF sowie "
            + "den QR-Code."
            + "\n\n"
            + "Bitte beachten Sie das Rückgabedatum "
            + "auf Ihrem Ausleihbeleg."
            + "\n\n"
            + "Mit freundlichen Grüßen,\n"
            + "Ihr Team";

        textPart.setText(
            text,
            "UTF-8"
        );

        MimeBodyPart pdfPart = new MimeBodyPart();

        ByteArrayDataSource pdfData =
            new ByteArrayDataSource(
                pdfBytes,
                "application/pdf"
            );

        pdfPart.setDataHandler(
            new DataHandler(pdfData)
        );

        pdfPart.setFileName(
            "Ausleihbeleg.pdf"
        );

        MimeBodyPart qrPart = new MimeBodyPart();

        ByteArrayDataSource qrData =
            new ByteArrayDataSource(
                qrBytes,
                "image/png"
            );

        qrPart.setDataHandler(
            new DataHandler(qrData)
        );

        qrPart.setFileName(
            "QRCode.png"
        );

        Multipart multipart =
            new MimeMultipart();

        multipart.addBodyPart(textPart);
        multipart.addBodyPart(pdfPart);
        multipart.addBodyPart(qrPart);

        message.setContent(multipart);

        Transport.send(message);
    }

    private static String escapeHtml(String text) {

        if (text == null) {
            return "";
        }

        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}
