package hbv.web;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;


public class MitgliedListeServlet extends HttpServlet{

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

      String Query="SELECT * from mitglied";

      PreparedStatement ps =con.prepareStatement(Query);
      PrintWriter pw= response.getWriter();

      ResultSet rs=ps.executeQuery();
      
      boolean first=true;
      
      pw.println("[");

      while(rs.next()){
      if(!first){
        pw.println(",");
      }
     
     pw.println("{");

     pw.println("\"id\":" + rs.getInt("Mitglied_id")+ ",");
     pw.println("\"vorname\":\"" + rs.getString("vorname")+ "\",");
     pw.println("\"nachname\":\"" + rs.getString("nachname")+ "\",");
     pw.println("\"adresse\":\"" + rs.getString("addresse")+ "\",");
     pw.println("\"email\":\"" + rs.getString("email")+ "\",");
     pw.println("\"datum\":\"" + rs.getTimestamp("erstellt_am")+ "\",");
     pw.println("\"stat\":\""+ rs.getString("stat") + "\"");


     pw.println("}");
     first=false;
   }
     pw.println("]");
      rs.close();
      ps.close();
      con.close();
 }catch(SQLException | NamingException e){
   e.printStackTrace();
   response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

   }

 }
}

