package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * UDPSocketServer
 * Extends SocketConstants
 */
public class UDPSocketServer extends SocketConstants {

    /**
     * Protected DatagramSocket
     * Initially set to null
     */
    protected DatagramSocket socket = null;


    /**
     * UDPSocketServer
     * Constructor - sets initial port to 9000
     * @throws SocketException
     * @throws IOException
     */
    public UDPSocketServer() throws SocketException, IOException {
        socket = new DatagramSocket(9000);
    }


    /**
     * execute
     * Generates a new TID for the server
     * Initialises a Read / Write thread to allow for multiple file transfers
     * @throws SocketException
     * @throws IOException
     */
    public void execute() throws SocketException, IOException {
        while(true){
            byte[] recvBuf = new byte[PACKET_LENGTH];
            DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(packet);

            DeserialisePacket deserialisePacket = new DeserialisePacket(packet);
            int opcode = deserialisePacket.getOpcode();

            int tid = generateTID(packet.getPort());

            switch(Opcode.get(opcode)){
                case Read:
                    new ReadRequestThread("ReadThread", tid, packet).run();
                    break;
                case Write:
                    new WriteRequestThread("WriteThread", tid, packet).run();
                    break;
            }
        }
    }


    /**
     * main
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new UDPSocketServer().execute();
        System.out.println("Time Server Started");
    }

}
