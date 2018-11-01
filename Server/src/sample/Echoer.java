package sample;

import sample.Decryption.MessageDecryption;
import sample.Encryption.EncryptRSAwithSignature;
import sample.Encryption.MessageEncryption;
import sample.Messages.AESkeyAndSignature;
import sample.Messages.Message;
import sample.Messages.Table1;
import javax.crypto.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.security.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static sample.Main.objectOutputStreams;

public class Echoer extends Thread {
    private Socket socket;
    private List<String> rowListUsername;
    private String sessionClientUsername;

    public Echoer(Socket socket, List<String> rowListUsername){
        this.socket = socket;
        this.rowListUsername= rowListUsername;
    }

    private void tellEveryOne(String s1) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        for (Map.Entry<ObjectOutputStream, Key> entry : objectOutputStreams.entrySet()) {
            ObjectOutputStream temp = entry.getKey();
            Key key = entry.getValue();
            MessageEncryption messEn = new MessageEncryption(s1, key);
            try {
                temp.writeObject(messEn.getMessage());
                temp.flush();
            } catch (Exception e) {
                System.err.println("TellEveryOne " + e);
            }
        }
    }

    private void tellEveryOne(Table1 table1) {
        for (Map.Entry<ObjectOutputStream, Key> entry : objectOutputStreams.entrySet()) {
            ObjectOutputStream temp = entry.getKey();
            Key key = entry.getValue();
            try {
                temp.writeObject(table1);
                temp.flush();
            } catch (Exception e) {
                System.err.println("TellEveryOne " + e);
            }
        }
    }

    private void sendNewUserList() {
        for (Map.Entry<ObjectOutputStream, Key> entry : objectOutputStreams.entrySet()) {
            ObjectOutputStream temp = entry.getKey();
            Key key = entry.getValue();
            try {
                System.out.println("List of online users" + rowListUsername + " to socket " + temp);
                temp.writeObject(rowListUsername);
                temp.flush();
            } catch (Exception e) {
                System.err.println("TellEveryOne " + e);
            }
        }
    }

    @Override
    public void run() {
        ObjectOutputStream output = null;
        try {
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());

            // generate an RSA key
            System.out.println("\nStart generating RSA key");
            KeyPairGenerator keyGenRSA = KeyPairGenerator.getInstance("RSA");
            keyGenRSA.initialize(1024);
            KeyPair keyRSA = keyGenRSA.generateKeyPair();
            PrivateKey keyRSAPrivate = keyRSA.getPrivate();
            PublicKey keyRSAPublic = keyRSA.getPublic();
            System.out.println("Finish generating RSA key");

            // First Server send the own public key to the client
            output.writeObject(keyRSAPublic);
            output.flush();
            System.out.println("Public key (RSA)of server has been sent to client");

            // Receive Client public key(RSA)
            PublicKey publicKeyClient = null;
            try {
                publicKeyClient = (PublicKey) input.readObject();
                System.out.println(publicKeyClient);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            // get a AES private key  (Generates the key)
            System.out.println("\nStart generating AES key");
            KeyGenerator keyGenAES = KeyGenerator.getInstance("AES");
            keyGenAES.init(128);    //In AES cipher block size is 128 bits
            SecretKey key = keyGenAES.generateKey();
            System.out.println("Finish generating AES key" + key);

            EncryptRSAwithSignature encryptRSAwithSignature = new EncryptRSAwithSignature(key.getEncoded(), publicKeyClient, keyRSAPrivate);
            AESkeyAndSignature aeSkeyAndSignature = new AESkeyAndSignature(encryptRSAwithSignature.getCipherKeyAES(), encryptRSAwithSignature.getSignature());
            // Now send the AES common key
            output.writeObject(aeSkeyAndSignature);
            output.flush();
            System.out.println("Common key (AES) has been sent to client");

            while (true) {
                String broadCastOnlineUsers;
                Table1 table1;
                String switchTo = (String) input.readObject();
                switch (switchTo) {
                    case "Message":
                        Message message = null;
                        MessageDecryption mess;
                        String plainMessage = "";
                        try {
                            message = (Message) input.readObject();
                            mess = new MessageDecryption(message.getMessage(), key);
                            plainMessage = mess.getMessage();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Received client input : ");
                        assert message != null;
                        System.out.println(plainMessage + " FROM using AES " + message.getFrom());

                        List<String> messageForThisOne = new ArrayList<>();
                        String broadCastMessage = message.getFrom() + " : " + plainMessage;
                        messageForThisOne.add(broadCastMessage);

//                        MessageEncryption messEnc = new MessageEncryption("AES is working well here", key);
//                        Message messageSend = new Message(messEnc.getMessage(), "server", message.getFrom());
//                        output.writeObject(messageSend);
//                        output.flush();
                        table1 = new Table1(messageForThisOne);
                        tellEveryOne(table1);
                        break;

                    case "logoutme":
                        String removeUser = (String) input.readObject();
                        System.out.println(removeUser + " want to log out");

                        objectOutputStreams.remove(output);
                        rowListUsername.remove(sessionClientUsername);

                        broadCastOnlineUsers = "****** " + sessionClientUsername + " Logged out at " + (new Date()) + " ******";
                        tellEveryOne(broadCastOnlineUsers);

                        sendNewUserList();
                        break;

                    case "loginme":
                        String newUser = (String) input.readObject();
                        this.sessionClientUsername = newUser;
                        System.out.println("User " + newUser + " want to log in");

                        objectOutputStreams.put(output, key);
                        rowListUsername.add(sessionClientUsername);

                        broadCastOnlineUsers = "****** " + sessionClientUsername + " Logged in at " + (new Date()) + " ******";
                        tellEveryOne(broadCastOnlineUsers);
                        sendNewUserList();
                        break;
                    case "files":
                        File imgFile = new File("testFile");
                        byte[] content = (byte[]) input.readObject();
                        MessageDecryption messageDecryptionImage = new MessageDecryption(content, key);
                        Files.write(imgFile.toPath(), messageDecryptionImage.getMessageImage());
                        broadCastOnlineUsers = "######## " + sessionClientUsername + " sent file at " + (new Date()) + " ########";
                        tellEveryOne(broadCastOnlineUsers);
                        break;
                    default:
                        System.out.println("Ooops how's this possible" + switchTo);
                }
            }
        } catch (IOException e) {
            System.out.println("Ooops : " + e.getMessage());
            rowListUsername.remove(sessionClientUsername);
            objectOutputStreams.remove(output);
            sessionClientUsername = "";
            e.printStackTrace();
        } catch (NoSuchAlgorithmException | ClassNotFoundException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error in closing socket");
            }
        }
    }
}