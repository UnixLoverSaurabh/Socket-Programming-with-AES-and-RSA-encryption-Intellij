package sample.Decryption;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

public class MessageDecryptionRSA {
    private SecretKey key;

    public MessageDecryptionRSA(byte[] cipherText, PrivateKey privateKey) {
        try {
            // get an RSA cipher object
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

            // decrypt the ciphertext using the private key
            System.out.println( "\nStart decryption using RSA:" );
            final long startTime = System.nanoTime();
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] newPlainText = cipher.doFinal(cipherText);
            System.out.println( "Finish decryption using RSA: " );
            final long duration = System.nanoTime() - startTime;

            System.out.println("It took " + duration + " nanosecond to decrypt the message ");

            key = new SecretKeySpec( newPlainText, "AES" );

        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    public SecretKey getKey() {
        return key;
    }
}
