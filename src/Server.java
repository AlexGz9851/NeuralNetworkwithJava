// Luis Iván Morett Arévalo		   A01634417
// Jesús Alejandro González Sánchez A00820225 
// Server
// Profesor: Gerardo Salinas
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server {
    private ServerSocket serverSocket;
    private ArrayList<Socket> sockets;
    private ArrayList<Connection> conectionsList ;

    
	public Server() {
        sockets = new ArrayList<>();
        conectionsList = new ArrayList<Connection>();
        try
        {
            serverSocket = new ServerSocket(8081);
            System.out.println("Server Started!");
            while(true)
            {
            	Socket newConnection = serverSocket.accept();
                sockets.add(newConnection);
                System.out.println("New conection accepted!");

                
                Connection conection = new Connection(newConnection);
                conectionsList.add(conection);
                conection.start();
            }
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
	}

	
    public static void main(String[] args) {
    	Server s = new Server();
    }
}