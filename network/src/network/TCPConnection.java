package network;

import java.io.*;
import java.net.Socket;

public class TCPConnection {
    private final Socket socket;
    private final Thread rxThread;
    private final TCPConnectionListener eventListner;
    private final BufferedReader in;
    private final BufferedWriter out;

    public TCPConnection(TCPConnectionListener eventListner, String ipAddr, int port) throws  IOException
    {
        this(eventListner, new Socket(ipAddr, port));
    }

    public TCPConnection(TCPConnectionListener eventListner, Socket socket) throws IOException {
        this.eventListner = eventListner;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                Character.getName(Integer.parseInt("UTF-8"))));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),
                Character.getName(Integer.parseInt("UTF-8"))));
        rxThread = new Thread(new Runnable() {
        @Override
                public void run() {
            try {
                eventListner.onConnectionReady(TCPConnection.this);
                while(!rxThread.isInterrupted())
                {
                    String msg = in.readLine();
                    eventListner.onReceiveString(TCPConnection.this, msg );
                }
            } catch (IOException e) {
                eventListner.onExection(TCPConnection.this, e);
            } finally {
                eventListner.onDisconnect(TCPConnection.this);
            }
        }
    });
    rxThread.start();

    }
    public void sentString(String value)
    {
        try {
            out.write(value + "\r\n");
            out.flush();
        } catch (IOException e) {
            eventListner.onExection(TCPConnection.this, e);
            disconnect();
        }
    }
    public void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListner.onExection(TCPConnection.this, e);
        }
    }
    @Override
    public String toString()
    {
        return "TCPConnection: " +  socket.getInetAddress() + ": "+socket.getPort();
    }
}
