package mttcpserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

/**
 * ReadRequestThread
 * Extends RequestThread
 */
public class ReadRequestThread extends RequestThread {

    /**
     * private initial buffer from the master socket
     */
    private byte[] initialBuffer;

    /**
     * ReadRequestThread
     * Constructor
     *
     * @param socket
     * @param receivedBuffer
     */
    public ReadRequestThread(Socket socket, byte[] receivedBuffer){
        super(socket);
        this.initialBuffer = receivedBuffer;
    }


    /**
     * run
     *
     * This method is called when we start a Thread object
     * Note that the ReadRequestThread extends the RequestThread object
     *
     * Opens the file which is received
     * If the file exists, sends file in blocks of 512 bytes
     * If the file doesn't exists, sends an error message
     *
     * Close socket once the connection is finished
     */
    @Override
    public void run() {
        try {

            // Stores the initial connection buffer e.g. filename
            byte[] receivedBuffer = initialBuffer;

            // Deserialises packet
            DeserialisePacket deserialisePacket = new DeserialisePacket(receivedBuffer);
            SerialisePacket serialisePacket = new SerialisePacket();

            // Gets the filename from the initial packet
            String readFileName = deserialisePacket.getFileName();

            File file = new File("./" + readFileName);

            if (file.exists()) {
                // Reads the file into a ByteArray
                ByteArray wholeFile = new ByteArray();
                wholeFile.addBytes(Files.readAllBytes(file.toPath()));


                // Sends the whole file in blocks of 512 bytes
                // If the last amount is less than 512 bytes, it only sends what is left
                int block = 1;
                int length = 0;
                do {
                    length = (wholeFile.size() - ((block -1) * DATA_LENGTH) < DATA_LENGTH) ? wholeFile.size() - ((block-1) * DATA_LENGTH) : (DATA_LENGTH);
                    byte[] dataSend = new byte[length];
                    System.arraycopy(convertToBytes(wholeFile), (block-1) * DATA_LENGTH, dataSend, 0, length);

                    byte[] sendFileData = serialisePacket.getDataBuffer(block, dataSend);

                    slaveSocket.getOutputStream().write(sendFileData);
                    block++;
                }while(length>=DATA_LENGTH);

            } else {
                // Sends an error code to the client if the file doesn't exist
                System.out.println(readFileName + " doesn't exist");
                byte[] sendFileData = serialisePacket.getErrorBuffer();
                slaveSocket.getOutputStream().write(sendFileData);
            }

            // Closes the slave socket connection
            slaveSocket.close();
            System.out.println("Slave is closed");
        } catch (IOException e) {
            System.err.println(e);
        }

    }

}
