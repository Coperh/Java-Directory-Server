package coperh.Lab2;

import javafx.util.Pair;
import java.net.*;
import java.io.*;

public class Client extends DirectoryUser {

    /**
     * Fetches the server that runs the service
     * @param service the service that is being requested
     * @param port the port of the discover server
     * @return
     */
    public Pair<InetAddress, Integer> fetch(String service, int port){
        try{
            InetAddress directoryAddress = discover(port);
            System.out.println("Server Found");
            // setup socket
            Socket directorySocket = new Socket(directoryAddress, port);
            ObjectOutputStream objectOut = new ObjectOutputStream(directorySocket.getOutputStream());
            ObjectInputStream objectIn = new ObjectInputStream(directorySocket.getInputStream());
            // send in command
            objectOut.writeObject("request\n");
            String ack = (String)objectIn.readObject();
            System.out.println(ack);
            objectOut.writeObject("Math");
            // get request
            Advert advert = (Advert)objectIn.readObject();
            if (advert == null){
                return null;
            }
            objectIn.close();
            objectOut.close();
            directorySocket.close();

            InetAddress address = InetAddress.getByName(advert.host);
            return new Pair(address, advert.port);
        }
        catch(IOException e) { return null; }
        catch(ClassNotFoundException e){return null;}
    }

    public void maths(InetAddress address, int port){
        try {
            Socket mathSocket = new Socket(address, port);
            ObjectOutputStream objectOut = new ObjectOutputStream(mathSocket.getOutputStream());
            ObjectInputStream objectIn = new ObjectInputStream(mathSocket.getInputStream());

            System.out.println("Request Sent");
            objectOut.writeObject("Do Math");
            System.out.println("Response received: "+(String)objectIn.readObject());

            objectIn.close();
            objectOut.close();
            mathSocket.close();
        }
        catch(IOException e){System.out.println(e);}
        catch(ClassNotFoundException e){System.out.println(e);}
    }

    public static void main(String[] args){

        Client client = new Client();
        Pair<InetAddress, Integer> server = client.fetch("Math",3455);
        client.maths(server.getKey(), server.getValue());
    }
}
