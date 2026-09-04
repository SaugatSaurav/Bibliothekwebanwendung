package hbv.web.Passworthashing;


import java.util.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class hashingpassword {

    // Methode zum Generieren eines zufälligen Salts
    public static byte[] generateSalt() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        byte[] salt = new byte[16]; // 16Bytes 
        random.nextBytes(salt);
        return salt;
    }

    // Methode zum Hashen des Passworts mit PBKDF2
    public static byte[] hashPassword(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 210000, 512); // 210000 Iterationen, 512 Bits Hash
        SecretKey key = secretKeyFactory.generateSecret(spec);
        spec.clearPassword(); // Löschen des Passworts aus dem Speicher
        return key.getEncoded();
    }

    // Methode zum Konvertieren von Bytes in einen Hex-String
    public static String toHex(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }

    // Methode zum Konvertieren eines Hex-Strings zurück in Bytes
    public static byte[] fromHex(String hex) {
        return HexFormat.of().parseHex(hex);
    }
}
