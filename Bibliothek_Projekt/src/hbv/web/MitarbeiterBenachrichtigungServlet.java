package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

import javax.sql.*;
import javax.naming.*;

import hbv.web.PDF.AusleihPdfGenerator;
import hbv.web.QRCode.QRCodeGenerator;


public class MitarbeiterBenachrichtigungServlet extends HttpServlet {


      protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType(
            "application/json;charset=UTF-8"
        );

        HttpSession session =
            request.getSession(false);


        /*
         * Prüfen, ob Mitarbeiter/Manager eingeloggt ist.
         */
        if (session == null ||
            session.getAttribute("management_standort_id") == null) {

            response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
            );

            return;
        }


        int standortId =
            (Integer) session.getAttribute(
                "management_standort_id"
            );


        try {

            Context initCtx =
                new InitialContext();


            DataSource ds =
                (DataSource) initCtx.lookup(
                    "java:/comp/env/jdbc/mariadb"
                );


            Connection connection =
                ds.getConnection();


            /*
             * Gebühren, Fehltage usw. aktualisieren.
             */
            BibliothekAutomatikService
                .aktualisiereGebuehrFehltageUndSperre(
                    connection
                );


                       String query =
                "SELECT " +
                "m.Mitglied_id AS mitglied_id, " +
                "m.vorname AS vorname, " +
                "m.nachname AS nachname, " +
                "e.id AS exemplar_id, " +
                "b.titel AS titel, " +
                "b.autor AS autor, " +
                "e.barcode AS barcode, " +
                "s.name AS standort, " +
                "e.regal AS regal " +

                "FROM reservierung r " +

                "JOIN mitglied m " +
                "ON r.mitglied_id = m.Mitglied_id " +

                "JOIN exemplar e " +
                "ON r.exemplar_id = e.id " +

                "JOIN buch b " +
                "ON e.buch_id = b.id " +

                "JOIN standort s " +
                "ON e.standort_id = s.id " +

                "WHERE r.status = 'RESERVIERT' " +
                "AND e.verfuegbar = 3 " +
                "AND e.standort_id = ? " +

                "ORDER BY m.Mitglied_id, r.reserviert_am";


            PreparedStatement ps =
                connection.prepareStatement(
                    query
                );


            ps.setInt(
                1,
                standortId
            );


            ResultSet rs =
                ps.executeQuery();


            PrintWriter out =
                response.getWriter();


            out.print("[");

            boolean first = true;


            while (rs.next()) {

                if (!first) {
                    out.print(",");
                }


                String mitgliedName =
                    rs.getString("vorname")
                    + " "
                    + rs.getString("nachname");


                out.print("{");


                out.print(
                    "\"mitgliedId\":"
                    + rs.getInt("mitglied_id")
                    + ","
                );


                out.print(
                    "\"mitgliedName\":\""
                    + jsonText(mitgliedName)
                    + "\","
                );


                out.print(
                    "\"exemplarId\":"
                    + rs.getInt("exemplar_id")
                    + ","
                );


                out.print(
                    "\"titel\":\""
                    + jsonText(
                        rs.getString("titel")
                    )
                    + "\","
                );


                out.print(
                    "\"autor\":\""
                    + jsonText(
                        rs.getString("autor")
                    )
                    + "\","
                );


                out.print(
                    "\"barcode\":\""
                    + jsonText(
                        rs.getString("barcode")
                    )
                    + "\","
                );


                out.print(
                    "\"standort\":\""
                    + jsonText(
                        rs.getString("standort")
                    )
                    + "\","
                );


                out.print(
                    "\"regal\":\""
                    + jsonText(
                        rs.getString("regal")
                    )
                    + "\""
                );


                out.print("}");


                first = false;
            }


            out.print("]");


            rs.close();
            ps.close();
            connection.close();


        } catch (SQLException | NamingException e) {

            e.printStackTrace();

            response.setStatus(
                HttpServletResponse
                    .SC_INTERNAL_SERVER_ERROR
            );
        }
    }



      protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {


        response.setContentType(
            "text/plain;charset=UTF-8"
        );


        PrintWriter out =
            response.getWriter();


        HttpSession session =
            request.getSession(false);


        /*
         * Prüfen, ob Mitarbeiter eingeloggt ist.
         */
        if (session == null ||
            session.getAttribute(
                "management_standort_id"
            ) == null) {

            response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
            );

            out.println(
                "Nicht eingeloggt."
            );

            return;
        }


        /*
         * Mitglied-ID aus Request holen.
         */
        String mitgliedIdText =
            request.getParameter(
                "mitgliedid"
            );


        if (mitgliedIdText == null ||
            mitgliedIdText.trim().isEmpty()) {

            response.setStatus(
                HttpServletResponse.SC_BAD_REQUEST
            );

            out.println(
                "Mitglied-ID fehlt."
            );

            return;
        }


        int mitgliedId;


        try {

            mitgliedId =
                Integer.parseInt(
                    mitgliedIdText
                );

        } catch (NumberFormatException e) {

            response.setStatus(
                HttpServletResponse.SC_BAD_REQUEST
            );

            out.println(
                "Ungültige Mitglied-ID."
            );

            return;
        }


        /*
         * Standort des eingeloggten Mitarbeiters.
         */
        int standortId =
            (Integer) session.getAttribute(
                "management_standort_id"
            );


        Connection connection = null;


        try {

            Context initCtx =
                new InitialContext();


            DataSource ds =
                (DataSource) initCtx.lookup(
                    "java:/comp/env/jdbc/mariadb"
                );


            connection =
                ds.getConnection();


            connection.setAutoCommit(
                false
            );

            String selectQuery =
                "SELECT " +

                "m.vorname AS vorname, " +
                "m.nachname AS nachname, " +
                "m.email AS email, " +
                "m.addresse AS addresse, " +

                "e.id AS exemplar_id, " +
                "e.barcode AS barcode, " +
                "e.regal AS regal, " +
                "e.zustand AS zustand, " +

                "b.titel AS titel, " +
                "b.autor AS autor, " +
                "b.genre AS genre, " +

                "s.name AS standort, " +

                "COALESCE(r.gebuehr, 0) AS gebuehr, " +
                "COALESCE(r.fehl_tage, 0) AS fehl_tage " +

                "FROM reservierung r " +

                "JOIN mitglied m " +
                "ON r.mitglied_id = m.Mitglied_id " +

                "JOIN exemplar e " +
                "ON r.exemplar_id = e.id " +

                "JOIN buch b " +
                "ON e.buch_id = b.id " +

                "JOIN standort s " +
                "ON e.standort_id = s.id " +

                "WHERE r.mitglied_id = ? " +

                "AND r.status = 'RESERVIERT' " +

                "AND e.verfuegbar = 3 " +

                "AND e.standort_id = ? " +

                "ORDER BY e.id";


            /*
             * Daten für PDF und E-Mail.
             */
            String vorname = "";
            String nachname = "";
            String email = "";
            String addresse = "";


                      LocalDate ausgeliehenAm =
                LocalDate.now();


                      LocalDate rueckgabe =
                ausgeliehenAm.plusWeeks(4);


                       List<AusleihPdfGenerator.ExemplarInfo>
                exemplare =
                    new ArrayList<>();


            try (
                PreparedStatement selectPs =
                    connection.prepareStatement(
                        selectQuery
                    )
            ) {

                selectPs.setInt(
                    1,
                    mitgliedId
                );


                selectPs.setInt(
                    2,
                    standortId
                );


                try (
                    ResultSet rs =
                        selectPs.executeQuery()
                ) {

                    while (rs.next()) {


                        /*
                         * Mitgliedsdaten.
                         */
                        vorname =
                            rs.getString(
                                "vorname"
                            );


                        nachname =
                            rs.getString(
                                "nachname"
                            );


                        email =
                            rs.getString(
                                "email"
                            );


                        addresse =
                            rs.getString(
                                "addresse"
                            );


                          exemplare.add(

                            new AusleihPdfGenerator
                                .ExemplarInfo(

                                rs.getInt(
                                    "exemplar_id"
                                ),

                                rs.getString(
                                    "titel"
                                ),

                                rs.getString(
                                    "autor"
                                ),

                                rs.getString(
                                    "barcode"
                                ),

                                rs.getString(
                                    "standort"
                                ),

                                rs.getString(
                                    "regal"
                                ),

                                rs.getString(
                                    "genre"
                                ),

                                rs.getString(
                                    "zustand"
                                ),

                                ausgeliehenAm
                                    .toString(),

                                rueckgabe
                                    .toString(),

                                rs.getBigDecimal(
                                    "gebuehr"
                                ).toString(),

                                rs.getInt(
                                    "fehl_tage"
                                )
                            )
                        );
                    }
                }
            }


            /*
             * Wenn keine reservierten Bücher
             * gefunden wurden:
             */
            if (exemplare.isEmpty()) {

                connection.rollback();


                response.setStatus(
                    HttpServletResponse.SC_CONFLICT
                );


                out.println(
                    "Keine reservierten Exemplare gefunden."
                );


                return;
            }


            /*
             * Sicherheitsprüfung E-Mail.
             */
            if (email == null ||
                email.trim().isEmpty()) {

                connection.rollback();


                response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
                );


                out.println(
                    "Das Mitglied hat keine E-Mail-Adresse."
                );


                return;
            }



          
            String updateQuery =
                "UPDATE reservierung r " +

                "JOIN exemplar e " +
                "ON r.exemplar_id = e.id " +

                "SET r.status = 'AUSGELIEHEN', " +

                "r.reserviert_am = CURDATE(), " +

                "r.zurueckgeben = " +
                "DATE_ADD(CURDATE(), INTERVAL 4 WEEK), " +

                "e.verfuegbar = 0 " +

                "WHERE r.mitglied_id = ? " +

                "AND r.status = 'RESERVIERT' " +

                "AND e.verfuegbar = 3 " +

                "AND e.standort_id = ?";


            int count;


            try (
                PreparedStatement ps =
                    connection.prepareStatement(
                        updateQuery
                    )
            ) {

                ps.setInt(
                    1,
                    mitgliedId
                );


                ps.setInt(
                    2,
                    standortId
                );


                count =
                    ps.executeUpdate();
            }



                      if (count > 0) {


                /*
                 * Datenbank zuerst speichern.
                 */
                connection.commit();



               
                try {


                      String qrUrl =
                        "https://informatik.hs-bremerhaven.de/"
                        + "docker-sausubedi-java/"
                        + "mitgliedAusleiheQrAnzeige"
                        + "?mitgliedid="
                        + mitgliedId
                        + "&ausgeliehenAm="
                        + ausgeliehenAm
                        + "&zurueckgeben="
                        + rueckgabe;



                ByteArrayOutputStream pdf =
                        AusleihPdfGenerator.generatePdf(

                            mitgliedId,

                            vorname
                                + " "
                                + nachname,

                            email,

                            addresse,

                            exemplare,

                            qrUrl
                        );



              byte[] qrPng =
                        QRCodeGenerator
                            .generateQRCodePng(
                                qrUrl
                            );



                 MailService.sendAusleihMail(

                        getServletContext(),

                        email,

                        vorname,

                        pdf.toByteArray(),

                        qrPng
                    );



                    /*
                     * Alles erfolgreich.
                     */
                    response.setStatus(
                        HttpServletResponse.SC_OK
                    );


                    out.println(
                        count
                        + " Exemplare wurden ausgeliehen. "
                        + "Die Bestätigungs-E-Mail wurde "
                        + "an "
                        + email
                        + " gesendet."
                    );


                } catch (Exception mailException) {


                 mailException.printStackTrace();

                    response.setStatus(
                        HttpServletResponse.SC_OK
                    );


                    out.println(
                        count
                        + " Exemplare wurden ausgeliehen. "
                        + "Die E-Mail konnte jedoch "
                        + "nicht gesendet werden."
                    );
                }


            } else {


                /*
                 * Es wurde nichts ausgeliehen.
                 */
                connection.rollback();


                response.setStatus(
                    HttpServletResponse.SC_CONFLICT
                );


                out.println(
                    "Keine Benachrichtigung"
                );
            }


        } catch (
            SQLException |
            NamingException e
        ) {


            e.printStackTrace();


            /*
             * Bei Datenbankfehler rollback.
             */
            if (connection != null) {

                try {

                    connection.rollback();

                } catch (SQLException ex) {

                    ex.printStackTrace();
                }
            }


            response.setStatus(
                HttpServletResponse
                    .SC_INTERNAL_SERVER_ERROR
            );


            out.println(
                "Serverfehler beim Ausleihen."
            );


        } finally {


                      if (connection != null) {

                try {

                    connection.close();

                } catch (SQLException e) {

                    e.printStackTrace();
                }
            }
        }
    }



       private String jsonText(
            String text) {


        if (text == null) {
            return "";
        }


        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", " ")
            .replace("\r", " ");
    }
}
