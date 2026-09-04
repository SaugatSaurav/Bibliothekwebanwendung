package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.*;
import java.sql.*;

import javax.naming.*;
import javax.sql.*;

public class BuchListeServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");

        try {

            Context initCtx =new InitialContext();

            DataSource ds =(DataSource)initCtx.lookup("java:/comp/env/jdbc/mariadb");

           try(Connection connection =ds.getConnection()){
             
             BibliothekAutomatikService.aktualisiereGebuehrFehltageUndSperre(connection);


             HttpSession session = request.getSession(false);


        if (session==null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
          }

      
    Integer mitgliedId =
    (Integer) session.getAttribute("Mitglied_id");

if (mitgliedId != null) {

    String statusQuery =
        "SELECT stat FROM mitglied WHERE Mitglied_id = ?";

    try (PreparedStatement statusPs =
             connection.prepareStatement(statusQuery)) {

        statusPs.setInt(1, mitgliedId);

        try (ResultSet statusRs =
                 statusPs.executeQuery()) {

            if (statusRs.next()) {

                String status =
                    statusRs.getString("stat");

                session.setAttribute(
                    "status_mitglied",
                    status
                );

                if ("GESPERRT".equals(status)) {

                    response.setStatus(
                        HttpServletResponse.SC_FORBIDDEN
                    );

                  
                    return;
                }
            }
        }
    }
}


      
               Integer managementId = null;
               String rolle = null;
               Integer StandortId= null;

                if (session != null) {
                    
                    managementId = (Integer) session.getAttribute("management_id");
                    rolle = (String) session.getAttribute("rolle");     
                    StandortId = (Integer)session.getAttribute("management_standort_id");

                }
            
            String query;
            PreparedStatement ps;



                   if (rolle == null) {
                   // Mitglied sieht alle Bücher
                //   query ="SELECT b.id, b.titel, b.autor, b.isbn, b.erscheinungsjahr, b.genre FROM buch b";
                   query =
        "SELECT b.id, b.titel, b.autor, b.isbn,b.erscheinungsjahr, b.genre, SUM(CASE WHEN e.verfuegbar = 1 " +
        "AND e.zustand IN ('NEU','GUT') THEN 1 ELSE 0 END) AS verfuegbar_count FROM buch b " +
        "LEFT JOIN exemplar e ON b.id = e.buch_id GROUP BY b.id, b.titel, b.autor, " +
        "b.isbn, b.erscheinungsjahr, b.genre";


                ps = connection.prepareStatement(query);

          }
          else if("Manager".equals(rolle)){

              //  query ="SELECT b.* FROM buch b";
              query =
              "SELECT b.id, b.titel, b.autor, b.isbn, b.erscheinungsjahr, b.genre, " +
              "SUM(CASE WHEN e.zustand IN ('NEU','GUT') AND e.verfuegbar = 1 THEN 1 ELSE 0 END) AS verfuegbar_count, " +

            // "SUM(CASE WHEN e.zustand IN ('NEU','GUT') THEN 1 ELSE 0 END) AS verfuegbar_count, " +
              "COUNT(e.id) AS total_count " +
              "FROM buch b " +
              "LEFT JOIN exemplar e ON b.id = e.buch_id "+
              "GROUP BY b.id, b.titel, b.autor, b.isbn, b.erscheinungsjahr, b.genre";

                ps = connection.prepareStatement(query);

        }else{

               // query = "SELECT b.* FROM buch b JOIN buch_sichtbarkeit bs ON b.id = bs.buch_id " +
                 //              "WHERE bs.sichtbar_fuer_alle = TRUE OR bs.verwaltung_id = ?";

              
                 query =
                      "SELECT b.id, b.titel, b.autor, b.isbn, b.erscheinungsjahr, b.genre, " +
                       "SUM(CASE WHEN e.zustand IN ('NEU','GUT') AND e.verfuegbar = 1 THEN 1 ELSE 0 END) AS verfuegbar_count, " +
                  //     "SUM(CASE WHEN e.zustand IN ('NEU','GUT') THEN 1 ELSE 0 END) AS verfuegbar_count, " +
                       "COUNT(e.id) AS total_count " +
                       "FROM buch b " +
                       "JOIN buch_sichtbarkeit bs ON b.id = bs.buch_id " +
                       "JOIN Verwaltung v ON bs.verwaltung_id = v.id " +
                       "LEFT JOIN exemplar e ON b.id = e.buch_id  AND e.standort_id = ? " +                     
                       "WHERE bs.sichtbar_fuer_alle = TRUE OR bs.verwaltung_id = ? OR v.standort_id=? "+
                        "GROUP BY b.id, b.titel, b.autor, b.isbn, b.erscheinungsjahr, b.genre";
                      
                 ps = connection.prepareStatement(query);
                 ps.setInt(1, StandortId);
                 ps.setInt(2, managementId);
                 ps.setInt(3, StandortId);
             }
            
          

           try (ResultSet resultSet = ps.executeQuery()) {
            
          
            PrintWriter out = response.getWriter();

            out.print("[");

            boolean first = true;

            while(resultSet.next()) {

                if(!first) {
                    out.print(",");
                }

                out.print("{");

                out.print("\"id\":"+ resultSet.getInt("id")+ ",");

                out.print("\"titel\":\""+ resultSet.getString("titel")+ "\",");

                out.print("\"autor\":\""+ resultSet.getString("autor")+ "\",");

                out.print("\"isbn\":\""+ resultSet.getString("isbn")+ "\",");
                out.print("\"erscheinungsjahr\":\""+ resultSet.getString("erscheinungsjahr") + "\",");
              


                if(rolle==null){
                out.print("\"genre\":\""+ resultSet.getString("genre") +  "\",");     
                out.print("\"verfuegbar_count\":\"" + resultSet.getInt("verfuegbar_count")+ "\"");

                }


                if(rolle!=null){
                 out.print("\"genre\":\""+ resultSet.getString("genre")+ "\",");

                int verfuegbar = resultSet.getInt("verfuegbar_count");
                int total = resultSet.getInt("total_count");
                out.print("\"exemplar_count\":\"" + verfuegbar + "/" + total + "\"");

              //  out.print("\"count\":\""+ resultSet.getString("count_exemplar") + "\"");
            }
                out.print("}");

                first = false;
            }

            out.print("]");
          }
            ps.close();
        }
      } catch(Exception e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            e.printStackTrace();
        }
    }
}

