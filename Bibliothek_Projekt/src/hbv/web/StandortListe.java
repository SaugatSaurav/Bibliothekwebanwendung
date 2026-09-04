package hbv.web;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;


public class StandortListe extends HttpServlet{

 protected void doGet(HttpServletRequest request ,HttpServletResponse response) throws IOException{
 
  

   try{

     HttpSession session = request.getSession(false);
         if (session==null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
          }



            String rolle = null;
            Integer standortId = null;

            if (session != null) {
                rolle = (String) session.getAttribute("rolle");
                standortId = (Integer) session.getAttribute("management_standort_id");

            }





        Context initCtx = new InitialContext();
        DataSource ds = (DataSource)initCtx.lookup("java:/comp/env/jdbc/mariadb");
        Connection con=ds.getConnection();



      response.setContentType("application/json;charset=UTF-8");

      //Extra
      String buchid = request.getParameter("buchid");




      String query;
      PreparedStatement ps;

 //Extra all
      if (buchid != null && !buchid.isEmpty()) {

    String rolleVomErsteller = null;

    String rolleQuery =
        "SELECT v.rolle " +
        "FROM buch_sichtbarkeit bs " +
        "JOIN Verwaltung v ON bs.verwaltung_id = v.id " +
        "WHERE bs.buch_id = ?";

    try (PreparedStatement rollePs = con.prepareStatement(rolleQuery)) {
        rollePs.setInt(1, Integer.parseInt(buchid));

        try (ResultSet rolleRs = rollePs.executeQuery()) {
            if (rolleRs.next()) {
                rolleVomErsteller = rolleRs.getString("rolle");
            }
        }
    }

     if ("Manager".equals(rolleVomErsteller)) {

        query = "SELECT * FROM standort";
        ps = con.prepareStatement(query);

    } else {

        query =
            "SELECT s.* " +
            "FROM buch_sichtbarkeit bs " +
            "JOIN Verwaltung v ON bs.verwaltung_id = v.id " +
            "JOIN standort s ON v.standort_id = s.id " +
            "WHERE bs.buch_id = ?";

        ps = con.prepareStatement(query);
        ps.setInt(1, Integer.parseInt(buchid));
    }

}

else if ("Mitarbeiter".equals(rolle) && standortId != null) {
                query = "SELECT * FROM standort WHERE id = ?";
                ps = con.prepareStatement(query);
                ps.setInt(1, standortId);
            } else {
                query = "SELECT * FROM standort";
                ps = con.prepareStatement(query);
            }




//Upto here



      PrintWriter pw= response.getWriter();

      ResultSet rs=ps.executeQuery();
      
      boolean first=true;
      
      pw.println("[");

      while(rs.next()){
      if(!first){
        pw.println(",");
      }
     
     pw.println("{");

     pw.println("\"id\":" + rs.getInt("id")+ ",");
     pw.println("\"name\":\"" + rs.getString("name")+ "\",");
     pw.println("\"strasse\":\"" + rs.getString("strasse")+ "\",");
     pw.println("\"plz\":\"" + rs.getInt("plz")+ "\",");
     pw.println("\"ort\":\""+ rs.getString("ort") + "\"");

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

