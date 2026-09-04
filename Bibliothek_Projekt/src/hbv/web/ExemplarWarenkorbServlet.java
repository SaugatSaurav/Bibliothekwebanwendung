package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class ExemplarWarenkorbServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/plain;charset=UTF-8");

          String exemplarid = request.getParameter("exemplarid");
          int exemplarId = Integer.parseInt(exemplarid);

          HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("Mitglied_id") == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
          }

          int mitgliedId = (Integer) session.getAttribute("Mitglied_id");

            try {
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

            try (Connection connection = ds.getConnection()) {

                String query =
                    "INSERT INTO reservierung(exemplar_id, mitglied_id, zurueckgeben,status) " +
                    "VALUES (?, ?, DATE_ADD(CURDATE(), INTERVAL 4 WEEK),'WARENKORB')";
                  //   "VALUES (?, ?, DATE_ADD(CURDATE(), INTERVAL 1 DAY),'WARENKORB')";


                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setInt(1, exemplarId);
                    ps.setInt(2, mitgliedId);
                    ps.executeUpdate();
                }

                
                  String Query=  "UPDATE exemplar SET verfuegbar = 3 " +
                    "WHERE id = ? AND verfuegbar = 1";

                try (PreparedStatement ps2 = connection.prepareStatement(Query)) {
                    ps2.setInt(1, exemplarId);

                    int count = ps2.executeUpdate();

                    if (count > 0) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().println("Reserviert");
                    } else {
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                        response.getWriter().println("Dieses Exemplar ist schon reserviert oder nicht mehr verfügbar.");
                    }
                }
            }

        } catch (SQLException | NamingException | NumberFormatException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }



protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

    response.setContentType("application/json;charset=UTF-8");

       HttpSession session = request.getSession(false);
        if (session == null ||
            session.getAttribute("Mitglied_id") == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
          }


   
  
    int mitgliedId = (Integer) session.getAttribute("Mitglied_id");

    try {
        Context initCtx = new InitialContext();
        DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

        try(Connection con = ds.getConnection()){


            BibliothekAutomatikService.aktualisiereGebuehrFehltageUndSperre(con);


              String status = BibliothekAutomatikService.holeMitgliedStatus(con,mitgliedId);
                session.setAttribute("status_mitglied",status);
                  
                if ("GESPERRT".equals(status)) {

                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().println("Sie sind gesperrt und alle Exemplar in Warenkorb sind wieder verfügbar..");
                    return;
                }



        String query =
            "SELECT e.id, b.titel AS titel, b.autor AS autor, " +
            "e.barcode AS barcode, s.name AS standort, e.regal AS regal, " +
            "b.genre AS genre, r.status AS status " +
            "FROM reservierung r " +
            "JOIN exemplar e ON r.exemplar_id = e.id " +
            "JOIN buch b ON e.buch_id = b.id " +
            "JOIN standort s ON e.standort_id = s.id " +
            "WHERE r.mitglied_id = ? AND r.status = 'WARENKORB'";

        try(PreparedStatement ps = con.prepareStatement(query)){
            ps.setInt(1, mitgliedId);

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
            out.print("\"stat\":\"" + rs.getString("status") + "\"");
            out.print("}");

            first = false;
        }

        out.print("]");

        rs.close();
       
      }
     } 
    }catch (Exception e) {
        e.printStackTrace();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
