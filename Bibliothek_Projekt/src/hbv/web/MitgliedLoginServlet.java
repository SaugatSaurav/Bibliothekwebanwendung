package hbv.web;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.security.GeneralSecurityException;
import hbv.web.Passworthashing.hashingpassword;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;


public class MitgliedLoginServlet extends HttpServlet {

  protected void doPost(HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
      response.setContentType("text/html");
      PrintWriter pw = response.getWriter();

      String Email=request.getParameter("email");
      String Password=request.getParameter("password");
 
       
        try {
     
        Context initCtx = new InitialContext();
        DataSource ds = (DataSource)initCtx.lookup("java:/comp/env/jdbc/mariadb");

     
        Connection connection = ds.getConnection();
        
        String Query="select * from mitglied where email=?"; 
        PreparedStatement ps = connection.prepareStatement(Query);
        ps.setString(1,Email);
       
        ResultSet rs=ps.executeQuery();
        
        if(rs.next()){

           String storedHash = rs.getString("password");
           String storedSalt = rs.getString("salt");
           byte[] hashedPassword = hashingpassword.hashPassword(Password, hashingpassword.fromHex(storedSalt));

               
         if (hashingpassword.toHex(hashedPassword).equals(storedHash)) {
            int mitgliedId = rs.getInt("Mitglied_id");
            String vorname = rs.getString("vorname");
            String nachname = rs.getString("nachname");
            String addresse = rs.getString("addresse");
            String email = rs.getString("email");
            String stat = rs.getString("stat");
            String Datum = rs.getString("erstellt_am");

          

           HttpSession session = request.getSession(true);
           session.setAttribute("Mitglied_id", mitgliedId);
           session.setAttribute("username_mitglied", vorname + " " + nachname);
           session.setAttribute("email_mitglied", email);
           session.setAttribute("status_mitglied", stat);
           session.setAttribute("addresse_mitglied", addresse);
           session.setAttribute("datum_mitglied",Datum);

           response.setStatus(HttpServletResponse.SC_OK);
         return;
     }else{
       response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
       return;
   }
  }else {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
} 
 }catch (GeneralSecurityException e) {
        throw new ServletException("Fehler beim Hashen des Passworts.", e);

    }catch(SQLException | NamingException ie){
      ie.printStackTrace();
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      
  }
 }
}

