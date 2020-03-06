package chat.server;

import network.TCPConnectionListener;
import sun.rmi.transport.tcp.TCPConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ChatServer implements TCPConnectionListener {
    public static void main(String[] args)
    {
        new ChatServer();
    }
    private final ArrayList<network.TCPConnection> connections = new ArrayList<>();

    private ChatServer ()
    {
        System.out.println("Server is running...");
        try(ServerSocket serverSocket = new ServerSocket(8189))
        {
            while(true) {
                try{
                    TCPConnection tcpConnection = new TCPConnection(this, serverSocket.accept());
                }catch (IOException e){
                    System.out.println("TCPConnection exeption: "+ e);
                }
            }
        }catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onConnectionReady(network.TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        sendToAllConnections("Client connected: "+ tcpConnection);
    }
    @Override
    public synchronized void onReceiveString(network.TCPConnection tcpConnection, String value) {
        sendToAllConnections(value);
    }

    @Override
    public synchronized void onDisconnect(network.TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnections("Client disconected: " +tcpConnection);
    }

    @Override
    public synchronized void onExection(network.TCPConnection tcpConnection, Exception e) {
        System.out.println("TPCConnection exeption: " + e);
    }
    private void  sendToAllConnections(String value)
    {
        System.out.println(value);
        final int cnt = connections.size();
        for(int i = 0; i < cnt; i++) {
            connections.get(i).sendString(value);
        }
    }
}
