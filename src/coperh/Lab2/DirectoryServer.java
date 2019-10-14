package coperh.Lab2;


import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.net.*;
import java.io.*;

/**
 * A class to represent a directory service system
 */
public class DirectoryServer {

    private static HashMap<String, ArrayList<Advert>> DIRECTORY = new HashMap<>();
    private int port;

    public DirectoryServer(int port){
        this.port = port;
    }

    /**
     * Starts the TCP server and the UDP Server
     */
    public void start(){
        try {
            // start discovery service
            System.out.println("Starting DiscoveryServer");
            new DiscoveryService(port).start();

            System.out.println("Starting Directory Server");
            new DirectoryService(port).start();

        }
        catch(IOException e){
            System.out.println("Network Error");
        }
    }

    public static void main(String[] args){
        DirectoryServer server = new DirectoryServer(3455);
        server.start();
    }


    /**
     * Server responsible for directory registers and queries
     */
    private static class DirectoryService extends Thread{
            private int port;

            private DirectoryService(int port){
                this.port = port;
            }

            public void run (){
                try{
                    ServerSocket serverSocket;
                    while(true) {
                        serverSocket = new ServerSocket(this.port);
                        new ClientHandler(serverSocket.accept()).start();
                        serverSocket.close();
                    }
                }
                catch( IOException e){System.out.println("Network Error");}
            }
    }

    /**
     * Handles directory service clients
     */
    private static class ClientHandler extends Thread{

        private Socket clientSocket;
        private ObjectOutputStream objectOut;
        private ObjectInputStream objectIn;

        private ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        /**
         * Registers Publishers in the
         * @throws ClassNotFoundException
         * @throws IOException
         */
        private void register() throws ClassNotFoundException,  IOException{
            Advert advert = (Advert)objectIn.readObject();
            String  service = advert.service;
            String  address = advert.host;
            if (DIRECTORY.get(service) == null){
                DIRECTORY.put(service, new ArrayList<Advert>());
            }
            DIRECTORY.get(service).add(advert);
            System.out.println("Registered: "+advert);
            objectOut.writeObject("Register Confirmed");
        }

        /**
         * Handles requests from clients for servers in directory
         * @throws ClassNotFoundException
         * @throws IOException
         */
        private void request() throws ClassNotFoundException, IOException{
            String request = (String)objectIn.readObject();

            if (DIRECTORY.get(request) == null) {
                System.out.println("Service does not exist: " + request);
                objectOut.writeObject(null);
            }
            else{
                System.out.println("Object Found: " + request);
                Advert advert = DIRECTORY.get(request).get(0);
                objectOut.writeObject(advert);
            }
        }

        public void run(){
            try {
                System.out.println( "Connection Accepted from: " + clientSocket.getInetAddress());

                objectIn = new ObjectInputStream(clientSocket.getInputStream());
                objectOut = new ObjectOutputStream(clientSocket.getOutputStream());

                String command = (String)objectIn.readObject();

                switch(command) {
                    case "register\n":
                        System.out.println("command: "+ command);
                        objectOut.writeObject("Command Accepted: "+ command);
                        register();
                        break;
                    case "request\n":
                        System.out.println("command: "+command);
                        objectOut.writeObject("Command Accepted: "+ command);
                        request();
                        break;
                    default:
                        System.out.println("Invalid command: "+command);
                        objectOut.writeObject("invalid\n");
                        break;
                }


                objectIn.close();
                objectOut.close();
                clientSocket.close();
            }
            catch(IOException e){
                System.out.println("Network Error");
            }
            catch(ClassNotFoundException e){ }
        }
    }

    /**
     * Defines a server responsible for discovery requests
     */
    private class DiscoveryService extends Thread{

        private DatagramSocket socket;
        private byte[] buffer = new byte[256];

        private DiscoveryService(int port) throws IOException{
            socket = new DatagramSocket(port);
        }

        public void run(){
            try{
                while(true){
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    // listen for broadcasts;
                    socket.receive(packet);

                    // get sender details
                    InetAddress sender = packet.getAddress();
                    int port = packet.getPort();


                    String received = new String(packet.getData(), 0, packet.getLength());
                    if(received.equals("discovery\n")){

                        System.out.println("Discovery Request from: "+sender);

                        byte[] response = ("discovered\n").getBytes();
                        DatagramPacket server_details = new DatagramPacket(response, response.length, sender, port);
                        socket.send(server_details);
                    }
                }
            }
            catch(IOException e){
                System.out.println("Network Error");
                socket.close();
            }
        }
    }
}
