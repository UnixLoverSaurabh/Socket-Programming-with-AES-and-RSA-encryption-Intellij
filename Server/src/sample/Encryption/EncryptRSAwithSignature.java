package sample.Encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class EncryptRSAwithSignature {
    private byte[] cipherKeyAES;
    private byte[] signature;

    public EncryptRSAwithSignature(byte[] plainText, PublicKey publicKey, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, SignatureException {
        // get an RSA cipher object
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        // encrypt the plaintext using the public key
        System.out.println( "\nStart encryption" );
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] cipherText = cipher.doFinal(plainText);
        System.out.println( "Finish encryption: " );
        System.out.println( new String(cipherText, StandardCharsets.UTF_8) );

        Signature sig = Signature.getInstance("MD5WithRSA");    //Creates the Signature object.
        sig.initSign(privateKey);                         //Initializes the Signature object.
        sig.update(cipherText);                                  // Calculates the signature with a plaintext string.
        byte[] signature = sig.sign();
        System.out.println( "\nSignature:" );
        System.out.println( new String(signature, StandardCharsets.UTF_8) );
        this.signature = signature;
        this.cipherKeyAES = cipherText;

        System.out.println("Finish encryption: ");
    }

    public byte[] getCipherKeyAES() {
        return cipherKeyAES;
    }

    public byte[] getSignature() {
        return signature;
    }
}
