package com.datastructuresproyect.numberrecognizer.Utils;

import android.os.StrictMode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Communication {

    private Socket socket;
    private static Communication communication;

    private Communication(String ip) {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            if (socket != null)
                this.socket.close();
            this.socket = new Socket(ip, 8081);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static Communication connect(String ip){
        if(communication==null)
            communication = new Communication(ip);
        return communication;
    }

    public void sendByteArray(byte[] bytes){
        try {
            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
            dOut.writeInt(bytes.length);
            dOut.write(bytes);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public int receiveAnswer(){
        try {
            DataInputStream dIn = new DataInputStream(socket.getInputStream());
            return dIn.readInt();
        } catch (IOException e){
            e.printStackTrace();
        }
        return -1;
    }
}
