package hbv.web;

import java.io.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;


public class MitarbeiterListeServlet extends HttpServlet{

 protected void doGet(HttpServletRequest request ,HttpServletResponse response) throws IOException{
 
  

  
   try{
   Context initCtx = new InitialContext();
       DataSource ds = (DataSource)initCtx.lookup("java:/comp/env/jdbc/mariadb");
        Connection con=ds.getConnection();

         HttpSession session = request.getSession(false);
        if (session==null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
          }

      response.setContentType("application/json;charset=UTF-8");
      response.setCharacterEncoding("UTF-8");

       List<String> mitarbeiterListe = new ArrayList<>();

      String Query="SELECT v.id, v.vorname, v.nachname, v.addresse, v.email, v.rolle, v.erstellt_am, s.name "
     + "FROM Verwaltung v JOIN standort s ON v.standort_id = s.id";
      PreparedStatement ps =con.prepareStatement(Query);
      PrintWriter pw= response.getWriter();

      ResultSet rs=ps.executeQuery();
      
   while(rs.next()){


         String mitarbeiterJson =
        "{"
        + "\"id\":" + rs.getInt("id") + ","
        + "\"vorname\":\"" + rs.getString("vorname") + "\","
        + "\"nachname\":\"" + rs.getString("nachname") + "\","
        + "\"adresse\":\"" + rs.getString("addresse") + "\","
        + "\"email\":\"" + rs.getString("email") + "\","
        + "\"rolle\":\"" + rs.getString("rolle") + "\","
        + "\"datum\":\"" + rs.getTimestamp("erstellt_am") + "\","
        + "\"ort\":\"" + rs.getString("name") + "\""
        + "}";
      
    mitarbeiterListe.add(mitarbeiterJson);

}
      pw.print("[");
      pw.print(String.join(",", mitarbeiterListe));
      pw.print("]");
      rs.close();
      ps.close();
      con.close();
 }catch(SQLException | NamingException e){
   e.printStackTrace();
   response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

   }

 }
}

