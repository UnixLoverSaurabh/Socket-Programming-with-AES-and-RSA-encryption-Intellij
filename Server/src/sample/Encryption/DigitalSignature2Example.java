package sample.Encryption;

import java.nio.charset.StandardCharsets;
import java.security.*;

// This example uses the digital signature features to generate and
// verify a signature much more easily than the previous example
public class DigitalSignature2Example {

    public static void main (String[] args) throws Exception {
        byte[] plainText = "Saurabh".getBytes(StandardCharsets.UTF_8);
        //
        // generate an RSA keypair
        System.out.println( "\nStart generating RSA key" );
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);

        KeyPair key = keyGen.generateKeyPair();
        System.out.println( "Finish generating RSA key" );
        //
        // get a signature object using the MD5 and RSA combo
        // and sign the plaintext with the private key,
        // listing the provider along the way

        Signature sig = Signature.getInstance("MD5WithRSA");    //Creates the Signature object.
        sig.initSign(key.getPrivate());                         //Initializes the Signature object.
        sig.update(plainText);                                  // Calculates the signature with a plaintext string.
        byte[] signature = sig.sign();

        System.out.println( "\nSignature:" );
        System.out.println( new String(signature, StandardCharsets.UTF_8) );
        //
        // verify the signature with the public key
        System.out.println( "\nStart signature verification" );
        Signature sig2 = Signature.getInstance("MD5WithRSA");
        sig2.initVerify(key.getPublic());                        // Verifies the signature.
        sig2.update(plainText);
        try {
            if (sig2.verify(signature)) {
                System.out.println( "Signature verified" );
            } else System.out.println( "Signature failed" );
        } catch (SignatureException se) {
            System.out.println( "Signature failed" );
        }
    }
}
