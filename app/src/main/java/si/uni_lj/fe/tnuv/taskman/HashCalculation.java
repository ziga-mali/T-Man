package si.uni_lj.fe.tnuv.taskman;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashCalculation {
    public static String calculateMD5(String input) {
        try {
            // Create MD5 hashing instance
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Convert input string to bytes and hash
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert the byte array to hexadecimal representation
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
