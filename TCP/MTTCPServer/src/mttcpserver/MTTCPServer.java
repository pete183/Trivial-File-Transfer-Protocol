package mttcpserver;

import java.net.*;
import java.io.*;

/**
 * MTTCPServer
 * Extends SocketConstants
 */
public class MTTCPServer extends SocketConstants {

    /**
     * main
     *
     * Runs the execute method which starts the connection
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new MTTCPServer().execute();

    }

    /**
     * execute
     *
     * Start the socket connection
     * Starts a Read Request Thread, if it receives a read request
     * Starts a Write Request Thread, if it receives a write request
     *
     * @throws IOException
     */
    public void execute() throws IOException{

        // the port number that the server socket will be bound to
        int portNumber = 10000;

        // The TCP ServerSocket object (master socket)
        ServerSocket masterSocket;
        Socket slaveSocket;

        masterSocket = new ServerSocket(portNumber);


        System.out.println("Server Started...");

        // Server always runs
        while(true){

            // Creates a slave socket from the master
            slaveSocket = masterSocket.accept();

            System.out.println("Accepted TCP connection from: " + slaveSocket.getInetAddress() + ", " + slaveSocket.getPort() + "...");
            System.out.println("Instantiating and starting new MTTCPServerThread object to handle the connection...");

            byte[] recvBuf = new byte[PACKET_LENGTH];

            // Waits for the clients packet
            InputStream stream = slaveSocket.getInputStream();
            byte[] receivedBuffer = new byte[516];
            stream.read(receivedBuffer);

            DeserialisePacket deserialisePacket = new DeserialisePacket(receivedBuffer);
            int opcode = deserialisePacket.getOpcode();

            // Opcode switch statement
            switch(Opcode.get(opcode)){
                case Read:
                    new ReadRequestThread(slaveSocket, receivedBuffer).run();
                    break;
                case Write:
                    new WriteRequestThread(slaveSocket, receivedBuffer).run();
                    break;
            }
        }
    }
}
