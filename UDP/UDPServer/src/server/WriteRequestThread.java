package server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * ReadRequestThread
 * Extends RequestThread
 */
public class WriteRequestThread extends RequestThread {

    /**
     * WriteRequestThread
     * Constructor
     * @param name
     * @param tid
     * @param packet
     * @throws SocketException
     */
    public WriteRequestThread(String name, int tid, DatagramPacket packet) throws SocketException {
        super(name, tid, packet);
    }

    /**
     * run
     *
     * This method is called when we start a Thread object
     * Note that the WriteRequestThread extends the RequestThread object
     *
     * Receives file packets of max 512 bytes
     * Sends an ack back to client
     *
     * On the last packet sent, the file is saved
     *
     * Close socket once the connection is finished
     */
    @Override
    public void run() {
        boolean live = true;
        ByteArray wholeFile = new ByteArray();
        String writeFileName;

        try {

            // Deserialises packet
            DeserialisePacket deserialisePacket = new DeserialisePacket(packet);
            SerialisePacket serialisePacket = new SerialisePacket();

            // Gets the initial read request filename
            writeFileName = deserialisePacket.getFileName();

            // Sends an ack packet back to client
            byte[] sendFileData = serialisePacket.getAckBuffer(0);
            byte[] senderBuffer = new byte[PACKET_LENGTH];
            System.arraycopy(sendFileData, 0, senderBuffer, 0, sendFileData.length);
            sentPacket = setPacket(senderBuffer, packet);
            socket.send(sentPacket);


            while (live) {


                // Catches timeout and re-sends last packet
                try {
                    socket.receive(packet);
                } catch(SocketTimeoutException e){
                    socket.send(sentPacket);
                }

                // Opcode switch statement
                switch(Opcode.get(deserialisePacket.getOpcode())){
                    case Data:

                        // Adds the whole file to a ByteArray
                        wholeFile.addBytes(deserialisePacket.getData());

                        // If its the last packet
                        // Save file to server
                        if (packet.getLength() - 4 < DATA_LENGTH) {
                            try (FileOutputStream fos = new FileOutputStream(writeFileName)) {

                                byte[] fileBytesArray = new byte[wholeFile.size()];
                                for (int i = 0; i < wholeFile.size(); i++) {
                                    fileBytesArray[i] = wholeFile.get(i);
                                }
                                fos.write(fileBytesArray);
                                fos.close();
                                System.out.println("File has been saved to server's directory as " + writeFileName);

                            }
                            live = false;
                        } else {

                            // Create an ack
                            // Send the ack buffer within packet
                            byte[] sendBuffer = serialisePacket.getAckBuffer(deserialisePacket.getBlockNumber());

                            senderBuffer = new byte[PACKET_LENGTH];
                            System.arraycopy(sendBuffer, 0, senderBuffer, 0, sendBuffer.length);
                            sentPacket = setPacket(senderBuffer, packet);
                            socket.send(sentPacket);
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }

        // Closes socket connection
        socket.close();

    }
}
