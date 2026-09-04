package hbv.web;

import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import javax.naming.*;
import javax.sql.*;

public class StatistikServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session= request.getSession(false);

        if(session==null){

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        try {
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");
            Connection con = ds.getConnection();

            out.print("{");

            // 1. Genre
            out.print("\"genre\":[");
            String genreQuery = "SELECT genre, COUNT(*) anzahl FROM buch GROUP BY genre";
            PreparedStatement ps1 = con.prepareStatement(genreQuery);
            ResultSet rs1 = ps1.executeQuery();
            boolean first = true;
            while (rs1.next()) {
                if (!first) out.print(",");
                out.print("{\"genre\":\"" + rs1.getString("genre") + "\",");
                out.print("\"anzahl\":" + rs1.getInt("anzahl") + "}");
                first = false;
            }
            out.print("],");
            rs1.close();
            ps1.close();

            // 2. Monate
            out.print("\"monate\":[");
            String monateQuery =
              //  "SELECT MONTHNAME(reserviert_am) monat, COUNT(*) anzahl " +
              //  "FROM reservierung GROUP BY MONTH(reserviert_am) ORDER BY MONTH(reserviert_am)";
    "SELECT DATE_FORMAT(reserviert_am, '%H:%i') AS monat, " +
    "COUNT(*) AS anzahl " +
    "FROM reservierung " +
    "GROUP BY DATE_FORMAT(reserviert_am, '%H:%i') " +
    "ORDER BY DATE_FORMAT(reserviert_am, '%H:%i')";

              PreparedStatement ps2 = con.prepareStatement(monateQuery);
            ResultSet rs2 = ps2.executeQuery();
            first = true;
            while (rs2.next()) {
                if (!first) out.print(",");
                out.print("{\"monat\":\"" + rs2.getString("monat") + "\",");
                out.print("\"anzahl\":" + rs2.getInt("anzahl") + "}");
                first = false;
            }
            out.print("],");
            rs2.close();
            ps2.close();

            // 3. Standorte
            out.print("\"standorte\":[");
            String standortQuery =
                "SELECT s.ort name, COUNT(DISTINCT b.id) buecher, COUNT(e.id) exemplare " +
                "FROM standort s " +
                "LEFT JOIN exemplar e ON s.id = e.standort_id " +
                "LEFT JOIN buch b ON b.id = e.buch_id " +
                "GROUP BY s.id";
            PreparedStatement ps3 = con.prepareStatement(standortQuery);
            ResultSet rs3 = ps3.executeQuery();
            first = true;
            while (rs3.next()) {
                if (!first) out.print(",");
                out.print("{\"name\":\"" + rs3.getString("name") + "\",");
                out.print("\"buecher\":" + rs3.getInt("buecher") + ",");
                out.print("\"exemplare\":" + rs3.getInt("exemplare") + "}");
                first = false;
            }
            out.print("],");
            rs3.close();
            ps3.close();

            // 4. Top 10
            out.print("\"top\":[");
            String topQuery =
                "SELECT b.titel, COUNT(r.id) anzahl " +
                "FROM reservierung r " +
                "JOIN exemplar e ON r.exemplar_id = e.id " +
                "JOIN buch b ON b.id = e.buch_id " +
                "GROUP BY b.id ORDER BY anzahl DESC LIMIT 10";
            PreparedStatement ps4 = con.prepareStatement(topQuery);
            ResultSet rs4 = ps4.executeQuery();
            first = true;
            while (rs4.next()) {
                if (!first) out.print(",");
                out.print("{\"titel\":\"" + rs4.getString("titel") + "\",");
                out.print("\"anzahl\":" + rs4.getInt("anzahl") + "}");
                first = false;
            }
            out.print("]");

            out.print("}");

            rs4.close();
            ps4.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
