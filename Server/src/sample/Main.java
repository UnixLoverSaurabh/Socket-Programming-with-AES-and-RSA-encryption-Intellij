package sample;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main{
    //public static ArrayList<ObjectOutputStream> objectOutputStreams = new ArrayList<>();
    public static List<String> rowListUsername = new ArrayList<>();
    public static Map<ObjectOutputStream, Key> objectOutputStreams = new HashMap<ObjectOutputStream, Key>();

    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(5700)){
            System.out.println("Server running");
            while(true){
                Socket socket = serverSocket.accept();
                new Echoer(socket, rowListUsername).start();
            }
        }catch (IOException e){
            System.out.println("Server exception " + e.getMessage());
        }
    }
}
