package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import javax.sql.*;
import javax.naming.*;
import hbv.web.QRCode.QRCodeGenerator;
import hbv.web.PDF.AusleihPdfGenerator;

public class MitgliedAusleihePdfServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("Mitglied_id") == null) {
               response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                 return;
        }

        int mitgliedId = (Integer) session.getAttribute("Mitglied_id");

        String ausgeliehenAm = request.getParameter("ausgeliehenAm");
        String zurueckgeben = request.getParameter("zurueckgeben");

        if (ausgeliehenAm == null ||zurueckgeben == null) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Ausleihdatum oder Rueckgabedatum fehlt.");
            return;
        }

        try {

            Context initCtx = new InitialContext();
            DataSource ds =(DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");
            try (Connection connection = ds.getConnection()) {

                String query =
                    "SELECT "
                    + "m.Mitglied_id AS mitglied_id, "
                    + "m.vorname AS vorname, "
                    + "m.nachname AS nachname, "
                    + "m.email AS email, "
                    + "m.addresse AS addresse, "
                    + "e.id AS exemplar_id, "
                    + "e.barcode AS barcode, "
                    + "e.regal AS regal, "
                    + "e.zustand AS zustand, "
                    + "b.titel AS titel, "
                    + "b.autor AS autor, "
                    + "b.genre AS genre, "
                    + "s.name AS standort, "
                    + "r.reserviert_am AS ausgeliehen_am, "
                    + "r.zurueckgeben AS zurueckgeben, "
                    + "r.gebuehr AS gebuehr, "
                    + "r.fehl_tage AS fehl_tage "
                    + "FROM reservierung r "
                    + "JOIN mitglied m ON r.mitglied_id = m.Mitglied_id "
                    + "JOIN exemplar e ON r.exemplar_id = e.id "
                    + "JOIN buch b ON e.buch_id = b.id "
                    + "JOIN standort s ON e.standort_id = s.id "
                    + "WHERE r.mitglied_id = ? "
                    + "AND r.status IN ('AUSGELIEHEN', 'RUECKGABE_ANGEFRAGT') "
                    + "AND r.reserviert_am = ? "
                    + "AND r.zurueckgeben = ? "
                    + "AND e.verfuegbar = 0 "
                    + "AND e.zustand IN ('NEU','GUT') "
                    + "ORDER BY e.id";

                String mitgliedName = "";
                String email = "";
                String addresse = "";

                List<AusleihPdfGenerator.ExemplarInfo> exemplare = new ArrayList<>();

                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setInt(1, mitgliedId);
                    ps.setDate(2, java.sql.Date.valueOf(ausgeliehenAm));
                    ps.setDate(3, java.sql.Date.valueOf(zurueckgeben));

                    try (ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {

                            mitgliedName =rs.getString("vorname")+ " " + rs.getString("nachname");
                            email =rs.getString("email");
                            addresse = rs.getString("addresse");

                            exemplare.add(
                                new AusleihPdfGenerator.ExemplarInfo(
                                    rs.getInt("exemplar_id"),
                                    rs.getString("titel"),
                                    rs.getString("autor"),
                                    rs.getString("barcode"),
                                    rs.getString("standort"),
                                    rs.getString("regal"),
                                    rs.getString("genre"),
                                    rs.getString("zustand"),
                                    rs.getDate("ausgeliehen_am").toString(),
                                    rs.getDate("zurueckgeben").toString(),
                                    rs.getBigDecimal("gebuehr").toString(),
                                    rs.getInt("fehl_tage")));
                        }
                    }
                }

                if (exemplare.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().println("Keine passenden Ausleihen gefunden.");
                    return;
                }

               String qrUrl = request.getScheme()
               + "://"
               + request.getServerName()
               + ":"
               + request.getServerPort()
               + request.getContextPath()
               + "/mitgliedAusleiheQrAnzeige"
               + "?mitgliedid="
               + mitgliedId
               + "&ausgeliehenAm="
               + ausgeliehenAm
               + "&zurueckgeben="
               + zurueckgeben;



                ByteArrayOutputStream pdf = AusleihPdfGenerator.generatePdf(
                        mitgliedId,
                        mitgliedName,
                        email,
                        addresse,
                        exemplare,
                        qrUrl);

                response.setContentType("application/pdf");
                response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=\"ausleihbeleg_"
                    + ausgeliehenAm
                    + ".pdf\"");

                response.setContentLength(pdf.size());
                response.getOutputStream().write(pdf.toByteArray());
                response.getOutputStream().flush();
            }

        } catch (SQLException | NamingException | IllegalArgumentException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().println("Fehler beim PDF-Erstellen: " + e.getMessage());
        }
    }
}
