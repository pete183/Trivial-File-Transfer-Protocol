package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


/**
 * RequestThread
 * Extends SocketConstants
 */
public abstract class RequestThread extends SocketConstants{

    /**
     * Protected DatagramSocket initialised to null
     * Protected DatagramPacket initialised to null
     */
    protected DatagramSocket socket = null;
    protected DatagramPacket packet = null;



    /**
     * RequestThread
     * Abstract Constructor
     * Initialised new datagram socket with TID and set timeout
     * @param name
     * @param tid
     * @param packet
     * @throws SocketException
     */
    protected RequestThread(String name, int tid, DatagramPacket packet) throws SocketException {
        super(name + String.valueOf(tid));
        socket = new DatagramSocket(tid);
        socket.setSoTimeout(TIME_OUT);
        this.packet = packet;
    }

    /**
     * setPacket
     * @param data
     * @param packet
     * @return DatagramPacket
     */
    protected DatagramPacket setPacket(byte[] data, DatagramPacket packet){
        packet.setData(data);
        packet.setAddress(packet.getAddress());
        packet.setPort(packet.getPort());
        return packet;
    }



}
