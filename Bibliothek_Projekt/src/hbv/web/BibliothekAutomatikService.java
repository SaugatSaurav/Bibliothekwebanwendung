package hbv.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class BibliothekAutomatikService {

    public static void aktualisiereGebuehrFehltageUndSperre(Connection connection) throws SQLException {


           String updateGebuehr =
            "UPDATE reservierung SET " +
            "fehl_tage = LEAST(GREATEST(DATEDIFF(CURDATE(), zurueckgeben), 0), 14), " +
            "gebuehr = LEAST(GREATEST(DATEDIFF(CURDATE(), zurueckgeben), 0), 14) * 0.10 " +
            "WHERE status = 'AUSGELIEHEN'";


               /*Zum Testen
               String updateGebuehr = """
            UPDATE reservierung
            SET fehl_tage = LEAST(
                    GREATEST(
                        TIMESTAMPDIFF(
                            HOUR,
                            TIMESTAMP(zurueckgeben),
                            NOW()
                        ),
                        0
                    ),
                    10
                ),
                gebuehr = LEAST(
                    GREATEST(
                        TIMESTAMPDIFF(
                            HOUR,
                            TIMESTAMP(zurueckgeben),
                            NOW()
                        ),
                        0
                    ),
                    10
                ) * 0.10
            WHERE status = 'AUSGELIEHEN'
            """;  */

        try (PreparedStatement ps = connection.prepareStatement(updateGebuehr)) {
            ps.executeUpdate();
        }



            String sperrenQuery =
            "UPDATE mitglied m SET m.stat = 'GESPERRT' " +
            "WHERE EXISTS (" +
            "SELECT 1 FROM reservierung r " +
            "WHERE r.mitglied_id = m.Mitglied_id " +
            "AND r.status = 'AUSGELIEHEN' " +
            "AND r.fehl_tage >= 14" +
            ")";

          /* Zum Testen 
            String sperrenQuery = """
            UPDATE mitglied m
            SET m.stat = 'GESPERRT'
            WHERE EXISTS (
                SELECT 1
                FROM reservierung r
                WHERE r.mitglied_id = m.Mitglied_id
                  AND r.status = 'AUSGELIEHEN'
                  AND r.fehl_tage >= 10
            )
            """;*/

        try (PreparedStatement ps = connection.prepareStatement(sperrenQuery)) {
            ps.executeUpdate();
        }


           String freigebenQuery = """
            UPDATE reservierung r
            JOIN exemplar e ON r.exemplar_id = e.id
            JOIN mitglied m ON r.mitglied_id = m.Mitglied_id
            SET r.status = 'ZURUECKGEGEBEN',
                r.zurueckgeben = CURDATE(),
                e.verfuegbar = 1
            WHERE m.stat = 'GESPERRT'
              AND r.status IN ('RESERVIERT', 'WARENKORB')
              AND e.verfuegbar = 3
            """;

      try (PreparedStatement ps = connection.prepareStatement(freigebenQuery)) {
            ps.executeUpdate();
        }
  }



  public static String holeMitgliedStatus(Connection connection, int mitgliedId) throws SQLException {

    String query = "SELECT stat FROM mitglied WHERE Mitglied_id = ?";

    try (PreparedStatement ps =connection.prepareStatement(query)) {
        ps.setInt(1, mitgliedId);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("stat");
            }
        }
    }
    return null;
 }
}
