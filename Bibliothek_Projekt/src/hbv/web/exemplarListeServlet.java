package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class exemplarListeServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("application/json;charset=UTF-8");

       String id = request.getParameter("buchid");
 
        try {
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

            try(Connection connection = ds.getConnection()){
         
            /*PreparedStatement ps;
            String query;

           if (id==null || id.isEmpty()) {

                  query=   "SELECT e.id AS id, b.id AS buchid, b.titel AS titel, b.autor AS autor, " +
                     "e.barcode AS barcode, s.name AS standort, e.regal AS regal, " +
                     "b.genre AS genre, e.zustand AS zustand " +
                     "FROM exemplar e " +
                     "JOIN buch b ON e.buch_id = b.id " +
                     "JOIN standort s ON e.standort_id = s.id ";


                    ps = connection.prepareStatement(query);
           }else{
                     int buchid=Integer.parseInt(id);

                 
               query=   "SELECT e.id AS id, b.id AS buchid, b.titel AS titel, b.autor AS autor, " +
                     "e.barcode AS barcode, s.name AS standort, e.regal AS regal, " +
                     "b.genre AS genre, e.zustand AS zustand " +
                     "FROM exemplar e " +
                     "JOIN buch b ON e.buch_id = b.id " +
                     "JOIN standort s ON e.standort_id = s.id "+
                     "WHERE b.id=?";

           
                
                           ps = connection.prepareStatement(query);
                            
                             ps.setInt(1, buchid);
           }*/


         HttpSession session = request.getSession(false);
        if (session==null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
          }

    Integer managementId = null;
    String rolle = null;
    Integer StandortId = null;

    if (session != null) {
       managementId = (Integer) session.getAttribute("management_id");
       rolle = (String) session.getAttribute("rolle");
       StandortId = (Integer)session.getAttribute("management_standort_id");
   }

     PreparedStatement ps;
     String query;

    if (id == null || id.isEmpty()) {

       if ("Manager".equals(rolle)) {

        query =
            "SELECT e.id AS id, b.id AS buchid, b.titel AS titel, b.autor AS autor, " +
            "e.barcode AS barcode, s.name AS standort, e.regal AS regal, " +
            "b.genre AS genre, e.zustand AS zustand, e.verfuegbar AS verfuegbar " +
            "FROM exemplar e " +
            "JOIN buch b ON e.buch_id = b.id " +
            "JOIN standort s ON e.standort_id = s.id";

        ps = connection.prepareStatement(query);

    } else {

        query =
            "SELECT e.id AS id, b.id AS buchid, b.titel AS titel, b.autor AS autor, " +
            "e.barcode AS barcode, s.name AS standort, e.regal AS regal, " +
            "b.genre AS genre, e.zustand AS zustand, e.verfuegbar AS verfuegbar " +
            "FROM exemplar e " +
            "JOIN buch b ON e.buch_id = b.id " +
            "JOIN standort s ON e.standort_id = s.id " +
            "JOIN buch_sichtbarkeit bs ON b.id = bs.buch_id " +
            "JOIN Verwaltung v ON bs.verwaltung_id = v.id " +
            "WHERE (bs.sichtbar_fuer_alle = TRUE OR bs.verwaltung_id = ? OR v.standort_id = ?) AND e.standort_id = ?";

        ps = connection.prepareStatement(query);
        ps.setInt(1, managementId);
        ps.setInt(2,StandortId);
        ps.setInt(3,StandortId);

    }

} else {

    int buchid = Integer.parseInt(id);

    if ("Manager".equals(rolle)) {

        query =
            "SELECT e.id AS id, b.id AS buchid, b.titel AS titel, b.autor AS autor, " +
            "e.barcode AS barcode, s.name AS standort, e.regal AS regal, " +
            "b.genre AS genre, e.zustand AS zustand, e.verfuegbar AS verfuegbar " +
            "FROM exemplar e " +
            "JOIN buch b ON e.buch_id = b.id " +
            "JOIN standort s ON e.standort_id = s.id " +
            "WHERE b.id = ?";

        ps = connection.prepareStatement(query);
        ps.setInt(1, buchid);

    } else {

        query =
            "SELECT e.id AS id, b.id AS buchid, b.titel AS titel, b.autor AS autor, " +
            "e.barcode AS barcode, s.name AS standort, e.regal AS regal, " +
            "b.genre AS genre, e.zustand AS zustand, e.verfuegbar AS verfuegbar " +
            "FROM exemplar e " +
            "JOIN buch b ON e.buch_id = b.id " +
            "JOIN standort s ON e.standort_id = s.id " +
            "JOIN buch_sichtbarkeit bs ON b.id = bs.buch_id " +
            "JOIN Verwaltung v ON bs.verwaltung_id = v.id " +
            "WHERE b.id = ? AND (bs.sichtbar_fuer_alle = TRUE OR bs.verwaltung_id = ? OR v.standort_id = ?) AND e.standort_id = ?";

        ps = connection.prepareStatement(query);
        ps.setInt(1, buchid);
        ps.setInt(2, managementId);
        ps.setInt(3,StandortId);
        ps.setInt(4,StandortId);


    }
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
                           out.print("\"buchid\":" + rs.getInt("buchid") + ",");
                           out.print("\"titel\":\"" + rs.getString("titel") + "\",");
                           out.print("\"autor\":\"" + rs.getString("autor") + "\",");
                           out.print("\"barcode\":\"" + rs.getString("barcode") + "\",");
                           out.print("\"standort\":\"" + rs.getString("standort") + "\",");
                           out.print("\"regal\":\"" + rs.getString("regal") + "\",");
                           out.print("\"genre\":\"" + rs.getString("genre") + "\",");
                           out.print("\"zustand\":\"" + rs.getString("zustand") + "\",");
                           out.print("\"verfuegbar\":\"" + rs.getInt("verfuegbar") + "\"");



                      //     out.print("\"zustand\":\"" + rs.getString("zustand") + "\"");
                               out.print("}");
                            first = false;
                        }

                        out.print("]");
                        ps.close();
                    }
        }
            

         catch (SQLException | NamingException | NumberFormatException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
