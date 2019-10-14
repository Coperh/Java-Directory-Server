package coperh.Lab2;

import java.io.Serializable;

public class Advert implements Serializable {
    final String service;
    final String host;
    final int port;

    public Advert(String service, String host, int port){
        this.service = service;
        this.host = host;
        this.port = port;
    }
    @Override
    public String toString(){
        return service+" "+host+":"+port;
    }
}
