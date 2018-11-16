package com.datastructuresproyect.numberrecognizer.Utils;

import android.os.StrictMode;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Communication {

    private Socket socket;
    private static Communication communication;

    private Communication(String ip) {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

    }

    public static Communication connect(String ip){
        if(communication==null)
            communication = new Communication(ip);
        setSocket(ip);
        return communication;
    }

    public void sendIntArray(int[] data){
        try {
            ObjectOutputStream dOut = new ObjectOutputStream(socket.getOutputStream());
            dOut.writeInt(data.length);
            dOut.writeObject(data);
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

    public void close(){
        try {
            if(socket!=null && !socket.isClosed())
                this.socket.close();
            this.socket = null;
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void setSocket(String ip){
        try {
            if(communication.socket!=null && !communication.socket.isClosed())
                communication.socket.close();
            communication.socket = new Socket(ip, 8081);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
