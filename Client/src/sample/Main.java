package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import sample.Decryption.DecryptRSAwithSignature;
import sample.Decryption.MessageDecryptionRSA;
import sample.Messages.AESkeyAndSignature;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.Arrays;

public class Main extends Application {

    private static ObjectOutputStream stringToEcho;
    private static ObjectInputStream echoes;
    private static Key key;
    private static PublicKey publicKeyServer;
    private static PrivateKey keyRSAPrivate;
    private static Socket socket;
    public final static String UPDATE_USERS = "updateuserslist:";
    public static String sessionUsername = null;
    public static Key getKey() {
        return key;
    }

    public static ObjectOutputStream getStringToEcho() {
        return stringToEcho;
    }

    public static ObjectInputStream getEchoes() {
        return echoes;
    }

    public static PublicKey getPublicKeyServer() {
        return publicKeyServer;
    }

    public static PrivateKey getKeyRSAPrivate() {
        return keyRSAPrivate;
    }

    public static Socket getSocket() {
        return socket;
    }

    public static void setSocketNull() {
        socket = null;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/sample/fxml/chatting.fxml"));
        primaryStage.setTitle("Welcome you.");
        primaryStage.getIcons().add(new Image("/sample/icons/icon.png"));
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();
    }
    public static void main(String[] args) {
        try {
            socket = new Socket("localhost", 5700);
            stringToEcho = new ObjectOutputStream(socket.getOutputStream());
            echoes = new ObjectInputStream(socket.getInputStream());

            // First receive the public key (RSA) of server
            try {
                publicKeyServer = (PublicKey) echoes.readObject();
                System.out.println("Public key of server received");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


            // generate an RSA key
            System.out.println("\nStart generating RSA key");
            KeyPairGenerator keyGenRSA = KeyPairGenerator.getInstance("RSA");
            keyGenRSA.initialize(1024);
            KeyPair keyRSA = keyGenRSA.generateKeyPair();
            keyRSAPrivate = keyRSA.getPrivate();
            PublicKey keyRSAPublic = keyRSA.getPublic();
            System.out.println("Finish generating RSA key");

            stringToEcho.writeObject(keyRSAPublic);
            stringToEcho.flush();
            System.out.println("Public key (RSA)of server has been sent to server");


            // Then receive the common key (AES) sent by server
            try {
                AESkeyAndSignature aeSkeyAndSignature = (AESkeyAndSignature) echoes.readObject();
                //MessageDecryptionRSA decryptedAESkey = new MessageDecryptionRSA(aeSkeyAndSignature.getCipherKeyAES(), keyRSAPrivate);
                DecryptRSAwithSignature decryptRSAwithSignature = new DecryptRSAwithSignature(aeSkeyAndSignature.getCipherKeyAES(), keyRSAPrivate, publicKeyServer, aeSkeyAndSignature.getSignature());
                key = decryptRSAwithSignature.getKey();
                System.out.println("Common key(AES) from server received" + key);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("Client Error: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        launch(args);
    }
}
