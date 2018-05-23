package mttcpserver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * WriteRequestThread
 * Extends RequestThread
 */
public class WriteRequestThread extends RequestThread {


    /**
     * private initial buffer byte[]
     */
    private byte[] initialBuffer;

    /**
     * WriteRequestThread
     * Constructor
     *
     * @param socket
     * @param receivedBuffer
     */
    public WriteRequestThread(Socket socket, byte[] receivedBuffer){
        super(socket);
        this.initialBuffer = receivedBuffer;
    }

    /**
     * run
     *
     * This method is called when we start a Thread object
     * Note that the WriteRequestThread extends the RequestThread object
     *
     * Receives file packets of max 512 bytes
     * On the last packet sent, the file is saved
     *
     * Close socket once the connection is finished
     */
    @Override
    public void run() {
        try {

            // Received buffer from the initial connection
            byte[] receivedBuffer = initialBuffer;

            DeserialisePacket deserialisePacket = new DeserialisePacket(receivedBuffer);
            String fileName = deserialisePacket.getFileName();
            ByteArray receiveBuffer = new ByteArray();

            boolean packetLoop = true;

            // Loop through the received files and add the data to a byte array
            do{
                InputStream stream = slaveSocket.getInputStream();
                byte[] bigBuffer = new byte[516];
                int count = stream.read(bigBuffer);
                receivedBuffer = new byte[count];
                System.arraycopy(bigBuffer, 0, receivedBuffer, 0, count);
                deserialisePacket = new DeserialisePacket(receivedBuffer);

                if (count < PACKET_LENGTH) {
                    packetLoop = false;
                }
                receiveBuffer.addBytes(deserialisePacket.getData());
                if (!packetLoop) {
                    // Save the bytes to a file
                    try (FileOutputStream fos = new FileOutputStream(fileName)) {

                        byte[] fileBytesArray = new byte[receiveBuffer.size()];
                        for (int i = 0; i < receiveBuffer.size(); i++) {
                            fileBytesArray[i] = receiveBuffer.get(i);
                        }
                        fos.write(fileBytesArray);
                        fos.close();
                        System.out.println("File has been saved to client's directory as " + fileName);
                    }
                }
            }while(packetLoop);


            // Closes the slave socket connection
            slaveSocket.close();
            System.out.println("Slave socket has closed");
        } catch (IOException e) {
            System.err.println(e);
        }

    }

}
