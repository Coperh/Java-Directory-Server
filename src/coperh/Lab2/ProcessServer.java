package coperh.Lab2;


import java.net.*;
import java.io.*;

/**
 * A class to represent a server that advertises on the directory server
 *
 * @author Conor Holden(117379801)
 *
 */
public class ProcessServer extends DirectoryUser {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private Socket directorySocket;
    private ObjectOutputStream objectOut;
    private ObjectInputStream objectIn;
    private Advert advert;
    private int serverPort = 6666;

    /**
     * Publishes the service on the directory Server
     * @param port port used by the directory server
     */
    public void publish(int port){
        try {
            InetAddress directoryAddress = discover(port);
            advert = new Advert("Math", "localhost",serverPort);
            // create connection
            directorySocket = new Socket(directoryAddress, port);
            objectOut = new ObjectOutputStream(directorySocket.getOutputStream());
            objectIn = new ObjectInputStream(directorySocket.getInputStream());
            // send in command
            objectOut.writeObject("register\n");
            String ack = (String)objectIn.readObject();
            System.out.println(ack);

            objectOut.writeObject(advert);
            String msg = (String)objectIn.readObject();
            System.out.println(msg);
            // close connection
            objectOut.close();
            objectIn.close();
            directorySocket.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    /**
     * Begins the process server
     */
    public void start(){
        try{
            System.out.println("Starting Server");
            serverSocket = new ServerSocket(6666);
            while(true){
                // setup connection
                clientSocket = serverSocket.accept();
                objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
                objectIn = new ObjectInputStream(clientSocket.getInputStream());

                String req = (String)objectIn.readObject();
                System.out.println("Request: " +req);
                // if it is a renewal notification from the directory
                if (req.equals("renewal\n")){
                    System.out.println("Renewing Lease");
                    objectOut.writeObject("renew\n");

                }
                else {
                    System.out.println("Response sent");
                    objectOut.writeObject("Response");
                }

                objectOut.close();
                objectIn.close();
                directorySocket.close();
                clientSocket.close();
            }
        }
        catch(IOException e){ System.out.println(e);}
        catch(ClassNotFoundException e){}
    }

    public static void main(String[] args){
        ProcessServer publisher = new ProcessServer();
        publisher.publish(3455);
        publisher.start();
    }
}
