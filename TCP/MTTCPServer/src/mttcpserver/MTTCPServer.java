package mttcpserver;

import java.net.*;
import java.io.*;

public class MTTCPServer extends SocketConstants {

    public static void main(String[] args) throws IOException {
        new MTTCPServer().execute();

    }

    public void execute() throws IOException{

        // the port number that the server socket will be bound to
        int portNumber = 10000;

        // The TCP ServerSocket object (master socket)
        ServerSocket masterSocket;
        Socket slaveSocket;

        masterSocket = new ServerSocket(portNumber);


        System.out.println("Server Started...");

        while(true){

            slaveSocket = masterSocket.accept();

            System.out.println("Accepted TCP connection from: " + slaveSocket.getInetAddress() + ", " + slaveSocket.getPort() + "...");
            System.out.println("Instantiating and starting new MTTCPServerThread object to handle the connection...");

            byte[] recvBuf = new byte[PACKET_LENGTH];

            InputStream stream = slaveSocket.getInputStream();
            byte[] recievedBuffer = new byte[516];
            int count = stream.read(recievedBuffer);

            DeserialisePacket deserialisePacket = new DeserialisePacket(recievedBuffer);
            int opcode = deserialisePacket.getOpcode();


            switch(Opcode.get(opcode)){
                case Read:
                    new ReadRequestThread(slaveSocket, recievedBuffer).run();
                    break;
                case Write:
                    new WriteRequestThread(slaveSocket, recievedBuffer).run();
                    break;
            }
        }
    }
}
