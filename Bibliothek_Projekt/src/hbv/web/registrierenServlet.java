package hbv.web;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.security.GeneralSecurityException;
import hbv.web.Passworthashing.hashingpassword;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class registrierenServlet extends HttpServlet{

protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
 
    String Vorname= request.getParameter("vname");
    String Nachname= request.getParameter("nname");
    String Addresse= request.getParameter("add");
    String Email= request.getParameter("email");
    String Password= request.getParameter("password");
    


    try{
        byte[] salt = hashingpassword.generateSalt();
        byte[] hashedPassword = hashingpassword.hashPassword(Password, salt);
     
        
        // Naming Context
        Context initCtx = new InitialContext();
        DataSource ds = (DataSource)initCtx.lookup("java:/comp/env/jdbc/mariadb");
        PrintWriter pw = response.getWriter();

        try(Connection connection = ds.getConnection()){ 
         String query="select * from mitglied where email=?";
        
         try(PreparedStatement ps=connection.prepareStatement(query)){
         ps.setString(1,Email);

         ResultSet rs=ps.executeQuery();
         if(rs.next()){

           response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.setContentType("text/plain");
            pw.write("Diese E-Mail-Adresse ist bereits registriert!");
            return;
                    
         }
       }
        



      String Query="insert into mitglied(vorname,nachname,addresse, email,password,salt) values (?,?,?,?,?,?)";
  

     try(PreparedStatement ps = connection.prepareStatement(Query)){
     ps.setString(1,Vorname);
     ps.setString(2,Nachname);
     ps.setString(3,Addresse);
     ps.setString(4,Email);
    // ps.setString(5,Password);
     ps.setString(5, hashingpassword.toHex(hashedPassword)); // Hash als Hex-String speichern
     ps.setString(6, hashingpassword.toHex(salt));


     int count= ps.executeUpdate();
    
     
 if(count > 0) {

    try {

        MailService.sendRegistrationMail(
            getServletContext(),
            Email,
            Vorname
        );

        response.setContentType(
            "text/html;charset=UTF-8"
        );

        pw.println(
            "<div class='registration-success'>"
        );

        pw.println(
            "<h2>Registrierung erfolgreich!</h2>"
        );

        pw.println(
            "<h3>"
            + "Ihr Konto wurde erfolgreich erstellt."
            + "</h3>"
        );

        pw.println(
            "<p>"
            + "Eine Bestätigungs-E-Mail wurde "
            + "an Ihre E-Mail-Adresse gesendet."
            + "</p>"
        );

        pw.println(
            "<p>"
            + "Bitte prüfen Sie auch Ihren Spam-Ordner."
            + "</p>"
        );

        pw.println(
            "<a href='mitglied_login.html'>"
        );

        pw.println(
            "<button type='button'>"
            + "Einloggen"
            + "</button>"
        );

        pw.println("</a>");
        pw.println("</div>");

    }catch (Exception e) {

    e.printStackTrace();

    pw.println("<div class='registration-success'>");

    pw.println(
        "<h2>Registrierung erfolgreich!</h2>"
    );

    pw.println(
        "<p>Ihr Konto wurde erfolgreich erstellt und gespeichert.</p>"
    );

    pw.println(
        "<p>Die Bestätigungs-E-Mail konnte leider nicht gesendet werden.</p>"
    );

    pw.println(
        "<p><strong>Mail-Fehler:</strong> "
        + e.getClass().getName()
        + "</p>"
    );

    pw.println(
        "<p><strong>Fehlermeldung:</strong> "
        + e.getMessage()
        + "</p>"
    );

    pw.println(
        "<p>Sie müssen sich nicht erneut registrieren. "
        + "Sie können sich bereits mit Ihrer E-Mail "
        + "und Ihrem Passwort einloggen.</p>"
    );

    pw.println(
        "<a href='mitglied_login.html'>"
        + "<button type='button'>Einloggen</button>"
        + "</a>"
    );

    pw.println("</div>");
}






 }
 }
}

} catch(GeneralSecurityException e){
    throw new ServletException("Fehler beim Hashen des Passworts.", e);

}catch(SQLException | NamingException e){
  e.printStackTrace();
  response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

   }
     
 }
}
