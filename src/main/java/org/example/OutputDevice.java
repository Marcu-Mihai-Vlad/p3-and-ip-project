package org.example;

import java.io.IOException;
import java.io.OutputStream;

public class OutputDevice {
    OutputStream os;

    public OutputDevice(OutputStream os) {
        this.os = os;
    }

    public void writeMessage(String message) {
        try{
            os.write(message.getBytes());
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}