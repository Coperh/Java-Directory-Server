package coperh.Lab2;

import java.net.*;
import java.io.*;


/**
 * A class to represent a generic Directory service user
 *
 * @author Conor Holden(117379801)
 */
public abstract class  DirectoryUser {

    private byte[] buffer = new byte[256];


    /**
     * Broadcasts a message to find the directory server
     * @param port port number used by the directory server
     * @return the address of the directory server
     */
    public InetAddress discover(int port){
        try{
            DatagramSocket discoveryClient = new DatagramSocket();

            System.out.println("Discovering Server");
            // broadcast message
            byte[] message =  "discovery\n".getBytes();
            DatagramPacket packet = new DatagramPacket(message, message.length,InetAddress.getByName("255.255.255.255"),port);
            discoveryClient.setBroadcast(true);
            discoveryClient.send(packet);

            // get response
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            discoveryClient.receive(response);
            String received = new String(response.getData(), 0, response.getLength());
            if(received.equals("discovered\n")){
                return response.getAddress();
            }
            return null;
        }
        catch(IOException e){
            System.out.println("Network Error");
            return null;
        }
    }
}
