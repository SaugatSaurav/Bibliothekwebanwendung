package hbv.web;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.security.GeneralSecurityException;
import hbv.web.Passworthashing.hashingpassword;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;


public class ManagementLoginServlet extends HttpServlet {

  protected void doPost(HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {

      String Email=request.getParameter("email");
      String Password=request.getParameter("password");

      
        

             try {
        Context initCtx = new InitialContext();
        DataSource ds = (DataSource)initCtx.lookup("java:/comp/env/jdbc/mariadb");

        Connection connection = ds.getConnection();
        
     //  String Query="select id, password,salt,rolle,standort_id from Verwaltung where email=?";
        

         String Query = """
         SELECT v.id, v.vorname, v.nachname, v.email, v.password, v.salt, v.rolle, v.standort_id, v.addresse, s.ort, 
                s.name, s.strasse, s.plz FROM Verwaltung v
                LEFT JOIN standort s ON v.standort_id = s.id
                WHERE v.email = ?
            """;

       PreparedStatement ps = connection.prepareStatement(Query);
        ps.setString(1,Email);

        ResultSet rs= ps.executeQuery();
        
       
        if(rs.next()){
            
           String storedHash = rs.getString("password");
           String storedSalt = rs.getString("salt");


        byte[] hashedPassword = hashingpassword.hashPassword(Password, hashingpassword.fromHex(storedSalt));



        if (hashingpassword.toHex(hashedPassword).equals(storedHash)) {

          //Geänderte
           int managementId = rs.getInt("id");
            String vorname = rs.getString("vorname");
            String nachname = rs.getString("nachname");
            String rolle = rs.getString("rolle");
            String email = rs.getString("email");
            String addresse = rs.getString("addresse");

           int standort_id= rs.getInt("standort_id");
           String standortName = rs.getString("name");
           String standortstrasse= rs.getString("strasse");
           int standortplz = rs.getInt("plz");
           String standortort= rs.getString("ort");

         

           HttpSession session = request.getSession(true);
           session.setAttribute("management_id", managementId);
            session.setAttribute("vorname", vorname);
                    session.setAttribute("nachname", nachname);
                    session.setAttribute("email", email);
                    session.setAttribute("rolle", rolle);
                    session.setAttribute("addresse", addresse);
           session.setAttribute("management_standort_id",standort_id);
           session.setAttribute("standortName", standortName + ", " + standortstrasse+ ", " + standortplz + ", " + standortort);


            //Bis hier Geänderte

           response.setContentType("text/plain");

         if ("Mitarbeiter".equals(rolle)) {

           //Original
         // HttpSession session= request.getSession();
         // session.setAttribute("mitarbeiter_standortId",standort_id);
         //Bis hier Original
          response.getWriter().println("Mitarbeiter");
         // System.out.println(StandortId);  

    }else{
       response.getWriter().println("Manager");
       
    }
        return;
         
        
       

     }else{
       response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
       return;
       }
  } else {
       response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
       return;
        }
 
}catch (GeneralSecurityException e) {
        throw new ServletException("Fehler beim Hashen des Passworts.", e);

    }catch(SQLException | NamingException ie){
      ie.printStackTrace();
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
  }
 }
}

