package server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * ReadRequestThread
 * Extends RequestThread
 */
public class ReadRequestThread extends RequestThread {

    /**
     * ReadRequestThread
     * Constructor
     *
     * @param name
     * @param tid
     * @param packet
     * @throws SocketException
     */
    public ReadRequestThread(String name, int tid, DatagramPacket packet) throws SocketException {
        super(name, tid, packet);
    }

    /**
     * run
     *
     * This method is called when we start a Thread object
     * Note that the ReadRequestThread extends the RequestThread object
     *
     * Opens the file which is received
     * If the file exists, sends file in blocks of 512 bytes
     * Waits for an Ack before sending the next packet
     *
     * If the file doesn't exists, sends an error message
     *
     * Close socket once the connection is finished
     */
    @Override
    public void run() {

        // Initialises variables
        boolean live = true;
        ByteArray wholeFile = new ByteArray();
        String readFileName;

        try {

            // Deserialises packet
            DeserialisePacket deserialisePacket = new DeserialisePacket(packet);
            SerialisePacket serialisePacket = new SerialisePacket();

            // Gets the initial read request filename
            readFileName = deserialisePacket.getFileName();

            File file = new File("./"+ readFileName);

            if(file.exists()){
                // Opens the file
                // Stores the file in bytes within a ByteArray
                wholeFile = new ByteArray();
                wholeFile.addBytes(Files.readAllBytes(file.toPath()));

                // Stores the first 512, or less if the file has less bytes into a byte[]
                int fileSendLength = (wholeFile.size() < DATA_LENGTH ) ? wholeFile.size() : (DATA_LENGTH);
                byte[] dataSend = new byte[fileSendLength];
                System.arraycopy(convertToBytes(wholeFile), 0, dataSend, 0, fileSendLength);

                // Creates a data buffer
                // Sends the packet with the data header
                byte[] sendFileData = serialisePacket.getDataBuffer(1, dataSend);
                sentPacket = setPacket(sendFileData, packet);
                socket.send(sentPacket);

            } else {

                // If the file does not exist
                // Send an error packet
                System.out.println(readFileName + " doesn't exist");

                byte[] sendFileData = serialisePacket.getErrorBuffer();
                sentPacket = setPacket(sendFileData, packet);
                socket.send(sentPacket);
                live = false;

            }


            while (live) {

                // Catches timeout and re-sends last packet
                try {
                    socket.receive(packet);
                } catch(SocketTimeoutException e){
                    socket.send(sentPacket);
                }

                // Opcode switch statement
                switch(Opcode.get(deserialisePacket.getOpcode())){
                    case Ack:
                        // Stores the next 512, or less if the file doesn't have 512 bytes left
                        int block = deserialisePacket.getBlockNumber();
                        int length = (wholeFile.size() - (block*DATA_LENGTH) < DATA_LENGTH) ? wholeFile.size() - (block * DATA_LENGTH) : (DATA_LENGTH);

                        // Sends a data packet to the client
                        if(length > 0){
                            byte[] smallFileData = new byte[DATA_LENGTH];

                            System.arraycopy(convertToBytes(wholeFile), block * DATA_LENGTH, smallFileData, 0, length);
                            byte[] sendFileData = serialisePacket.getDataBuffer(block+1, smallFileData);
                            sentPacket = setPacket(sendFileData, packet);
                            socket.send(sentPacket);

                        } else {
                            live = false;
                            byte[] sendFileData = serialisePacket.getDataBuffer(block+1, new byte[0]);
                            sentPacket = setPacket(sendFileData, packet);
                            socket.send(sentPacket);
                        }
                        break;

                }
            }
        } catch (IOException e) {
          System.err.println(e);
        }
        // Closes the socket connection
        socket.close();
    }
}
