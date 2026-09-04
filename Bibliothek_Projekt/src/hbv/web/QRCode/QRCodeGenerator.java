package hbv.web.QRCode;

import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BarcodeQRCode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

public class QRCodeGenerator {

    public static Image generateQRCode(String data) {

        try {

            BarcodeQRCode qrCode =
                new BarcodeQRCode(
                    data,
                    250,
                    250,
                    null
                );

            Image qrImage =
                qrCode.getImage();

            qrImage.scaleAbsolute(
                100,
                100
            );

            return qrImage;

        } catch(Exception e) {

            throw new RuntimeException(
                "Fehler beim Erstellen des QR-Codes",
                e
            );
        }
    }


    /*
     * QR-Code als PNG für E-Mail-Anhang.
     */
    public static byte[] generateQRCodePng(
            String data) {

        try {

            BarcodeQRCode qrCode =
                new BarcodeQRCode(
                    data,
                    250,
                    250,
                    null
                );

            java.awt.Image awtImage =
                qrCode.createAwtImage(
                    Color.BLACK,
                    Color.WHITE
                );

            BufferedImage bufferedImage =
                new BufferedImage(
                    250,
                    250,
                    BufferedImage.TYPE_INT_RGB
                );

            Graphics2D graphics =
                bufferedImage.createGraphics();

            graphics.drawImage(
                awtImage,
                0,
                0,
                null
            );

            graphics.dispose();


            ByteArrayOutputStream output =
                new ByteArrayOutputStream();

            ImageIO.write(
                bufferedImage,
                "png",
                output
            );

            return output.toByteArray();

        } catch(Exception e) {

            throw new RuntimeException(
                "Fehler beim Erstellen "
                + "des QR-Code-PNG",
                e
            );
        }
    }
}
