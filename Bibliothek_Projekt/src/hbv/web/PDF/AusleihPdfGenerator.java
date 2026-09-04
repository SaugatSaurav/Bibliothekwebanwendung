package hbv.web.PDF;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.ByteArrayOutputStream;
import java.util.List;

import hbv.web.QRCode.QRCodeGenerator;

public class AusleihPdfGenerator {

    public static class ExemplarInfo {

        public int exemplarId;
        public String titel;
        public String autor;
        public String barcode;
        public String standort;
        public String regal;
        public String genre;
        public String zustand;
        public String ausgeliehenAm;
        public String rueckgabedatum;
        public String gebuehr;
        public int fehlTage;

        public ExemplarInfo(
                int exemplarId,
                String titel,
                String autor,
                String barcode,
                String standort,
                String regal,
                String genre,
                String zustand,
                String ausgeliehenAm,
                String rueckgabedatum,
                String gebuehr,
                int fehlTage) {

            this.exemplarId = exemplarId;
            this.titel = titel;
            this.autor = autor;
            this.barcode = barcode;
            this.standort = standort;
            this.regal = regal;
            this.genre = genre;
            this.zustand = zustand;
            this.ausgeliehenAm = ausgeliehenAm;
            this.rueckgabedatum = rueckgabedatum;
            this.gebuehr = gebuehr;
            this.fehlTage = fehlTage;
        }
    }


    public static ByteArrayOutputStream generatePdf(
            int mitgliedId,
            String mitgliedName,
            String email,
            String addresse,
            List<ExemplarInfo> exemplare,
            String qrUrl) {

       Document document = new Document(PageSize.A4, 15, 15, 15, 15);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {

            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);

            Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);

            Font textFont = new Font(Font.FontFamily.HELVETICA, 11);

            Font smallFont = new Font(Font.FontFamily.HELVETICA, 8);


            /*
             Titel
             */
            Paragraph title =  new Paragraph("Ausleihbeleg / Bibliothek", titleFont);

            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Dies ist der Beleg fuer die ausgeliehenen Exemplare.", textFont));

            document.add(Chunk.NEWLINE);
            document.add(new LineSeparator());
            document.add(Chunk.NEWLINE);


            /*
              Mitgliedsdaten
             */
            document.add(new Paragraph("Mitgliedsdaten", sectionFont));

            document.add(new Paragraph("Mitglied-ID: " + mitgliedId, textFont));

            document.add(new Paragraph("Name: " + safe(mitgliedName), textFont));

            document.add(new Paragraph("E-Mail: " + safe(email), textFont));

            document.add(new Paragraph("Adresse: " + safe(addresse), textFont));

            document.add(Chunk.NEWLINE);
            document.add(new LineSeparator());
            document.add(Chunk.NEWLINE);


            /*
              Ausleihdaten
             */
            document.add(new Paragraph("Ausleihdaten", sectionFont));

            if (!exemplare.isEmpty()) {

                ExemplarInfo erstes = exemplare.get(0);

                document.add(new Paragraph("Ausgeliehen am: "+ safe(erstes.ausgeliehenAm),textFont));
                document.add(new Paragraph("Rueckgabedatum: "+ safe(erstes.rueckgabedatum),textFont));
            }

            document.add(Chunk.NEWLINE);


            /*
              Tabelle mit allen Exemplaren
             */
            PdfPTable table = new PdfPTable(10);

            table.setWidthPercentage(90);

            table.setWidths(new float[] {
                    1.2f, 2.0f, 2.0f, 1.6f, 1.8f,
                    1.5f, 1.4f, 1.4f, 1.6f, 1.6f
                }
            );

            addHeader(table, "Exemplar-ID");
            addHeader(table, "Titel");
            addHeader(table, "Autor");
            addHeader(table, "Barcode");
            addHeader(table, "Standort");
            addHeader(table, "Genre");
            addHeader(table, "Regal");
            addHeader(table, "Zustand");
            addHeader(table, "Gebuehr");
            addHeader(table, "Fehltage");

            for (ExemplarInfo ex : exemplare) {

                addCell(table, String.valueOf(ex.exemplarId), smallFont);
                addCell(table, safe(ex.titel), smallFont);
                addCell(table, safe(ex.autor), smallFont);
                addCell(table, safe(ex.barcode), smallFont);
                addCell(table, safe(ex.standort), smallFont);
                addCell(table, safe(ex.genre), smallFont);
                addCell(table, safe(ex.regal), smallFont);
                addCell(table, safe(ex.zustand), smallFont);
                addCell(table, safe(ex.gebuehr) + " EUR", smallFont);

                if (ex.fehlTage >= 14) {
                    addCell(table,ex.fehlTage + " (Ueberfaellig)",smallFont);

                } else {
                    addCell(table,String.valueOf(ex.fehlTage),smallFont);
                }
            }

            document.add(table);

            document.add(Chunk.NEWLINE);
            document.add(new LineSeparator());
            document.add(Chunk.NEWLINE);


            /*
             QR-Code
             Der QR-Code-Text wird komplett in QRCodeGenerator.java gebaut.
             */
            document.add(new Paragraph("QR-Code", sectionFont));

           Image qrImage = QRCodeGenerator.generateQRCode(qrUrl);
            qrImage.setAlignment(Element.ALIGN_CENTER);

            document.add(qrImage);
            document.add(Chunk.NEWLINE);


            /*
              Hinweise
             */
            document.add(new Paragraph("Hinweis: Bitte geben Sie die Exemplare bis zum Rueckgabedatum zurueck.",textFont));

            document.add(new Paragraph("Bei spaeter Rueckgabe koennen Gebuehren entstehen.", textFont));

            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Mit freundlichen Gruessen\nIhr Bibliothek-Team",textFont));

            document.close();

            return outputStream;

        } catch (Exception e) {

            throw new RuntimeException("Fehler beim Erstellen des PDFs",e);
        }
    }


    private static void addHeader(PdfPTable table, String text) {

        Font font =new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD);

        PdfPCell cell =new PdfPCell(new Phrase(text, font));

        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        cell.setPadding(5);

        table.addCell(cell);
    }


    private static void addCell(PdfPTable table,String text,Font font) {

        PdfPCell cell =new PdfPCell(new Phrase(safe(text), font));

        cell.setHorizontalAlignment(Element.ALIGN_LEFT);

        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        cell.setPadding(5);

        table.addCell(cell);
    }


    private static String safe(String text) {

        if (text == null) {
            return "";
        }

        return text;
    }
}
