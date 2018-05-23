package mttcpserver;

import java.net.Socket;

/**
 * RequestThread
 * Extends SocketConstants
 */
public class RequestThread extends SocketConstants {

    /**
     * protected slaveSocket variable
     */
    protected Socket slaveSocket = null;



    /**
     * RequestThread
     * Constructor
     *
     * Links the slave socket from the main MTTCPServer to the RequestThread
     *
     * @param socket
     */
    public RequestThread(Socket socket){
        super("MTTCPServerThread");
        this.slaveSocket = socket;
    }
}
