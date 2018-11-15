//Luis Iván Morett Arévalo		   A01634417
//Jesús Alejandro González Sánchez A00820225 
//EDD Gp2
//Profesor: Gerardo Salinas.

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;



public class Connection extends Thread{
    
    private final Socket socket;
    private String user;
    private Network net;
    
    private double[] img;
    int salida;
    byte[] entrada;
   
    public Connection(Socket socket)
    {
        this.socket = socket;
        this.net=Network.getInstance();
    }
   
    @Override
    public void run()
    {
    	try {
    		//aqui se conecta con la red neuronal, se lee archivo y asi.
    		DataInputStream dIn = new DataInputStream(socket.getInputStream());
    		int length = dIn.readInt();                    // read length of incoming message
    		if(length>0) {
    		    entrada = new byte[length];
    		    dIn.readFully(entrada, 0, entrada.length); // read the message
        		while(net.isBusy()) {
        			try {
    					Thread.sleep(30);
    				} catch (InterruptedException e) {}
        		}
        		img=new double[entrada.length];
        		for(int i=0;i<entrada.length;i++) {
        			img[i]= (double) entrada[i];
        		}
        		salida=net.evaluate(img);
        		this.send(salida);
    		}
    	}catch(Exception ex) {
    		try {this.send(10);}//10: Error status.
    		catch(IOException ee) {}

    	}
		this.logOut();
		this.closeSocket();	
    	
    	
    }
   
   
    private void send(int y) throws UnsupportedEncodingException, IOException {
    	DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
    	dOut.writeInt(y); 
    }
    private void logOut()
    {
        this.closeSocket();
    }
    
    private void closeSocket() {
    	try {
	    	this.socket.close();
	        this.finalize();
	        System.out.println("Socket cerrado: " + this.socket.getInetAddress());
    	}
	    catch(IOException ex)
	    {
	        System.out.println(ex.getMessage());
	    } 
	    catch (Throwable ex) {
	        System.out.println(ex.getMessage());
	    } 
    }
}

