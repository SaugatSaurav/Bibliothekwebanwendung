package hbv.web;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.security.GeneralSecurityException;
import hbv.web.Passworthashing.hashingpassword;


import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class addmitarbeiterServlet extends HttpServlet{

protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
 
    String Vorname= request.getParameter("vname");
    String Nachname= request.getParameter("nname");
    String Addresse= request.getParameter("add");
    String Email= request.getParameter("email");
    String Password= request.getParameter("password");
    String Rolle= request.getParameter("rolle");
    int standortId = Integer.parseInt(request.getParameter("standort_id"));


     
        try {


        byte[] salt = hashingpassword.generateSalt();
        byte[] hashedPassword = hashingpassword.hashPassword(Password, salt);

        Context initCtx = new InitialContext();
        DataSource ds = (DataSource)initCtx.lookup("java:/comp/env/jdbc/mariadb");
        
        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();

        try(Connection connection = ds.getConnection()){
         String query="select * from Verwaltung where email=?";

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




        
      String Query="insert into Verwaltung(vorname,nachname,addresse,email,rolle,standort_id,password,salt) values (?,?,?,?,?,?,?,?)";
  

    try(PreparedStatement ps = connection.prepareStatement(Query)){
     ps.setString(1,Vorname);
     ps.setString(2,Nachname);
     ps.setString(3,Addresse);
     ps.setString(4,Email);
     ps.setString(5,Rolle);
     ps.setInt(6,standortId);
     ps.setString(7, hashingpassword.toHex(hashedPassword)); // Hash als Hex-String speichern
     ps.setString(8, hashingpassword.toHex(salt));


     int count= ps.executeUpdate();
    
     
  
     if(count>0){
   // response.setStatus(HttpServletResponse.SC_OK);
    pw.println("<div id='add-success'>");      
    pw.println("<h2>Mitarbeiter Erfolgreich Hinzugefügt</h2>"+"<br>");
    pw.println("<p>Sie können jetzt hinzugefügte Mitarbeiter ansehen</p>");
    pw.println("<a href='mitarbeiterliste.html'>"); 
    pw.println("<button type='button'>Ansehen</button></a></div>");
    
   
   
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
