package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public abstract class RequestThread extends SocketConstants{

    protected DatagramSocket socket = null;
    protected DatagramPacket packet = null;
    protected final int DATA_LENGTH = 512;

    public RequestThread(String name, int tid, DatagramPacket packet) throws SocketException {
        super(name + String.valueOf(tid));
        socket = new DatagramSocket(tid);
        this.packet = packet;
    }

}
