package mttcpserver;

import java.net.Socket;


public class RequestThread extends SocketConstants {

    protected Socket slaveSocket = null;

    public RequestThread(Socket socket){
        super("MTTCPServerThread");
        this.slaveSocket = socket;
    }
}
