package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


public class UDPSocketServer extends SocketConstants {

    protected DatagramSocket socket = null;


    public UDPSocketServer() throws SocketException, IOException {
        socket = new DatagramSocket(9000);
    }


    public void execute() throws SocketException, IOException {
        while(true){
            byte[] recvBuf = new byte[516];
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



    public static void main(String[] args) throws IOException {
        new UDPSocketServer().execute();
        System.out.println("Time Server Started");
    }

}
