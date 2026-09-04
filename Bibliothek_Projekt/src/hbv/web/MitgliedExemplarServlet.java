package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class MitgliedExemplarServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

  
            response.setContentType("application/json;charset=UTF-8");
            HttpSession session = request.getSession(false); 
            
           if (session == null ||session.getAttribute("Mitglied_id") == null) {
              response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
              return;
            }

             int mitgliedId= (Integer)session.getAttribute("Mitglied_id");

            String buchId=request.getParameter("buchid");

        try {
          Context initCtx = new InitialContext();
          DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

          Connection connection = ds.getConnection();
          BibliothekAutomatikService.aktualisiereGebuehrFehltageUndSperre(connection);

          
          String status = BibliothekAutomatikService.holeMitgliedStatus(connection, mitgliedId);
          session.setAttribute("status_mitglied",status);

         if ("GESPERRT".equals(status)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
           connection.close();
           return;
       }



         
            String query;
            PreparedStatement ps;

          if(buchId!=null && !buchId.isEmpty()){
            Integer buchid=Integer.parseInt(buchId);


      query= "SELECT e.id, b.titel AS titel, b.autor AS autor, e.barcode AS barcode, s.name AS standort, e.regal AS regal, " +
        "b.genre AS genre,  e.zustand AS zustand, e.verfuegbar AS verfuegbar " +
        "FROM exemplar e " +
        "JOIN buch b ON e.buch_id = b.id " +
        "JOIN standort s ON e.standort_id = s.id WHERE e.buch_id=? AND e.verfuegbar = 1 AND e.zustand IN ('NEU','GUT')";

         ps = connection.prepareStatement(query);
         ps.setInt(1,buchid);
          }else{
        
          
   
             query =
                    "SELECT e.id, b.titel AS titel, b.autor AS autor, " +
                    "e.barcode AS barcode, s.name AS standort, e.regal AS regal, " +
                    "b.genre AS genre, e.zustand AS zustand, e.verfuegbar AS verfuegbar " +
                    "FROM reservierung r " +
                    "JOIN exemplar e ON r.exemplar_id = e.id " +
                    "JOIN buch b ON e.buch_id = b.id " +
                    "JOIN standort s ON e.standort_id = s.id " +
                    "WHERE r.mitglied_id = ? " +
                    "AND r.status = 'RESERVIERT' " +
                    "AND e.verfuegbar = 3";
                


            ps = connection.prepareStatement(query);

            ps.setInt(1, mitgliedId);
          }


                    ResultSet rs = ps.executeQuery();

                        PrintWriter out = response.getWriter();

                        out.print("[");

                        boolean first = true;

                        while (rs.next()) {

                            if (!first) {
                                out.print(",");
                            }

                                out.print("{");

                           
                           out.print("\"id\":" + rs.getInt("id") + ",");
                           out.print("\"titel\":\"" + rs.getString("titel") + "\",");
                           out.print("\"autor\":\"" + rs.getString("autor") + "\",");
                           out.print("\"barcode\":\"" + rs.getString("barcode") + "\",");
                           out.print("\"standort\":\"" + rs.getString("standort") + "\",");
                           out.print("\"regal\":\"" + rs.getString("regal") + "\",");
                           out.print("\"genre\":\"" + rs.getString("genre") + "\",");
                           out.print("\"zustand\":\"" + rs.getString("zustand") + "\",");
                           out.print("\"verfuegbar\":\"" + rs.getInt("verfuegbar") + "\"");


                            out.print("}");
                            first = false;
                        }

                        out.print("]");
                        ps.close();
                        rs.close();
                        connection.close();
                    }catch (SQLException | NamingException | NumberFormatException e) {
                         e.printStackTrace();
                         response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
