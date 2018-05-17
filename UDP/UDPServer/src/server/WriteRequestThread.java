package server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;

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
     * Runs overridden thread method
     */
    @Override
    public void run() {
        boolean live = true;
        ByteArray wholeFile = new ByteArray();
        String writeFileName;

        try {
            DeserialisePacket deserialisePacket = new DeserialisePacket(packet);
            SerialisePacket serialisePacket = new SerialisePacket();

            writeFileName = deserialisePacket.getFileName();
            byte[] sendFileData = serialisePacket.getAckBuffer(1);
            byte[] senderBuffer = new byte[PACKET_LENGTH];
            System.arraycopy(sendFileData, 0, senderBuffer, 0, sendFileData.length);
            socket.send(setPacket(senderBuffer, packet));


            while (live) {
                socket.receive(packet);

                switch(Opcode.get(deserialisePacket.getOpcode())){
                    case Data:
                        wholeFile.addBytes(deserialisePacket.getData());

                        if (packet.getData()[packet.getData().length - 1] == 0) {
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
                            byte[] sendBuffer = serialisePacket.getAckBuffer(deserialisePacket.getBlockNumber());

                            senderBuffer = new byte[PACKET_LENGTH];
                            System.arraycopy(sendBuffer, 0, senderBuffer, 0, sendBuffer.length);
                            socket.send(setPacket(senderBuffer, packet));
                        }
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        socket.close();

    }
}
