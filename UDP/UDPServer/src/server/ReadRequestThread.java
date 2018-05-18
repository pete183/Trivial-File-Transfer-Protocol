package server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
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
     * Runs overridden thread method
     */
    @Override
    public void run() {
        boolean live = true;
        ByteArray wholeFile = new ByteArray();
        String readFileName;

        try {
            DeserialisePacket deserialisePacket = new DeserialisePacket(packet);
            SerialisePacket serialisePacket = new SerialisePacket();
            readFileName = deserialisePacket.getFileName();

            File file = new File("./"+ readFileName);

            if(file.exists()){
                wholeFile = new ByteArray();
                wholeFile.addBytes(Files.readAllBytes(file.toPath()));

                int fileSendLength = (wholeFile.size() < DATA_LENGTH ) ? wholeFile.size() : (DATA_LENGTH);

                byte[] dataSend = new byte[fileSendLength];
                System.arraycopy(convertToBytes(wholeFile), 0, dataSend, 0, fileSendLength);


                byte[] sendFileData = serialisePacket.getDataBuffer(1, dataSend);
                socket.send(setPacket(sendFileData, packet));

            } else {
                System.out.println(readFileName + " doesn't exist");

                byte[] sendFileData = serialisePacket.getErrorBuffer();
                socket.send(setPacket(sendFileData, packet));
                live = false;

            }


            while (live) {
                socket.receive(packet);
                switch(Opcode.get(deserialisePacket.getOpcode())){
                    case Ack:
                        int block = deserialisePacket.getBlockNumber();
                        int length = (wholeFile.size() - (block*DATA_LENGTH) < DATA_LENGTH) ? wholeFile.size() - (block * DATA_LENGTH) : (DATA_LENGTH);
                        if(length > 0){
                            byte[] smallFileData = new byte[DATA_LENGTH];

                            System.arraycopy(convertToBytes(wholeFile), block * DATA_LENGTH, smallFileData, 0, length);
                            byte[] sendFileData = serialisePacket.getDataBuffer(block+1, smallFileData);

                            socket.send(setPacket(sendFileData, packet));

                        } else {
                            live = false;
                            byte[] sendFileData = serialisePacket.getDataBuffer(block+1, new byte[0]);
                            socket.send(setPacket(sendFileData, packet));
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
